<%@ page import="java.io.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.nio.file.Path" %>
<%@ page import="ecmwf.web.ECMWFException" %>
<%@ page import="ecmwf.web.services.content.Content" %>

<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="ecmwf.ecpds.master.plugin.http.HttpPlugin" %>

<%!
    private static final Logger _log = LogManager.getLogger(HttpPlugin.class);

    private void debugHeader(javax.servlet.http.HttpServletResponse resp, String name, String value) {
        try { resp.setHeader("X-Download-Debug-" + name, String.valueOf(value)); } catch (Throwable ignore) {}
    }

    private void writeError(javax.servlet.http.HttpServletResponse response, String msg) throws IOException {
        response.reset();
        response.setContentType("text/plain; charset=UTF-8");
        response.setStatus(javax.servlet.http.HttpServletResponse.SC_OK); // iframe-compatible
        response.getWriter().write("##DOWNLOAD_ERROR## " + msg);
    }
%>

<%
    Content content = (Content) request.getAttribute("content");
    Long expectedSize = (Long) request.getAttribute("size");
    String directError = (String) request.getAttribute("downloadError");
    boolean debug = "1".equals(request.getParameter("debug"));

    if (_log.isDebugEnabled()) {
        _log.debug("[DOWNLOAD-JSP] start uri={} query={} expectedSize={} contentPresent={} debug={}",
                request.getRequestURI(), request.getQueryString(), expectedSize, (content != null), debug);
    }

    debugHeader(response, "Expected-Size", String.valueOf(expectedSize));
    debugHeader(response, "Content-Present", String.valueOf(content != null));

    if (directError != null) {
        writeError(response, directError);
        return;
    }

    if (content == null) {
        writeError(response, "No content provided.");
        return;
    }

    if (expectedSize == null || expectedSize < 0) {
        writeError(response, "Invalid or missing file size metadata.");
        return;
    }

    Path tmpFile = null;

    try {

        // ---------------- DEBUG MODE ----------------
        if (debug) {
            long probed = 0L;
            try (InputStream is = content.getInputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) {
                    probed += r;
                    if (probed > (1024 * 1024)) break;
                }
            }

            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().printf(
                "##DOWNLOAD_DEBUG##%nExpected-Size: %d%nProbed-Readable-Bytes (capped): %d%nWould-Mismatch: %s%n",
                expectedSize, probed, (expectedSize != probed));
            return;
        }

        // ---------------- BUFFER TO TEMP FILE ----------------
        tmpFile = Files.createTempFile("download-", ".bin");

        long total = 0L;
        boolean readOk = false;

        try (InputStream is = content.getInputStream();
             OutputStream fos = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {

            byte[] buf = new byte[64 * 1024];
            int r;
            while ((r = is.read(buf)) != -1) {
                fos.write(buf, 0, r);
                total += r;
            }
            fos.flush();
            readOk = true;

        } catch (Exception ex) {
            _log.error("[DOWNLOAD-JSP] Exception while reading upstream stream", ex);
        }

        debugHeader(response, "Bytes-Buffered", String.valueOf(total));

        if (!readOk) {
            writeError(response, "Upstream stream failed while reading. Bytes received: " + total);
            return;
        }

        if (total != expectedSize) {
            _log.error("[DOWNLOAD-JSP] Size mismatch: expected={} actual={}", expectedSize, total);
            writeError(response,
                "File size mismatch: expected " + expectedSize + " bytes but received " + total + " bytes.");
            return;
        }

        // ---------------- SEND RESPONSE ----------------

        final String ct = content.getContentType();
        response.reset();
        response.setContentType(ct != null ? ct : "application/octet-stream");

        String asciiName = content.getName().replaceAll("[^\\x20-\\x7E]", "_");
        String encodedName = URLEncoder.encode(content.getName(), StandardCharsets.UTF_8.toString());
        String disposition = response.getContentType().startsWith("image/") ? "inline" : "attachment";

        String cd = disposition
                + "; filename=\"" + asciiName + "\""
                + "; filename*=UTF-8''" + encodedName;

        response.setHeader("Content-Disposition", cd);
        response.setContentLengthLong(total);

        try (InputStream fis = new BufferedInputStream(Files.newInputStream(tmpFile));
             OutputStream os = response.getOutputStream()) {

            byte[] buf = new byte[64 * 1024];
            int r;
            while ((r = fis.read(buf)) != -1) {
                os.write(buf, 0, r);
            }
            os.flush();
        }

        if (_log.isDebugEnabled()) {
            _log.debug("[DOWNLOAD-JSP] Download sent successfully. size={}", total);
        }

    } catch (ECMWFException e) {
        _log.error("[DOWNLOAD-JSP] ECMWFException: {}", e.getFullMessage(), e);
        writeError(response, e.getFullMessage());

    } catch (IOException e) {
        _log.error("[DOWNLOAD-JSP] IOException: {}", e.getMessage(), e);
        writeError(response, e.getMessage());

    } finally {
        // ---------------- GUARANTEED CLEANUP ----------------
        if (tmpFile != null) {
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException deleteEx) {
                _log.warn("[DOWNLOAD-JSP] Failed to delete temp file {}", tmpFile, deleteEx);
            }
        }
    }
%>