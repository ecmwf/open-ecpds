<%@ page buffer="none" trimDirectiveWhitespaces="true" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="ecmwf.web.ECMWFException" %>
<%@ page import="ecmwf.web.services.content.Content" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="ecmwf.ecpds.master.plugin.http.HttpPlugin" %>
<%!
    private static final Logger _log = LogManager.getLogger(HttpPlugin.class);

    private void writeError(javax.servlet.http.HttpServletResponse response, String msg) throws IOException {
        response.reset();
        response.setContentType("text/plain; charset=UTF-8");
        response.setStatus(javax.servlet.http.HttpServletResponse.SC_OK); // iframe-compatible
        response.getWriter().write("##DOWNLOAD_ERROR## " + msg);
    }
%><%
    Content content = (Content) request.getAttribute("content");
    Long expectedSize = (Long) request.getAttribute("size");
    String directError = (String) request.getAttribute("downloadError");
    boolean debug = "1".equals(request.getParameter("debug"));

    if (_log.isDebugEnabled()) {
        _log.debug("[DOWNLOAD-JSP] start uri={} query={} expectedSize={} contentPresent={} debug={}",
                request.getRequestURI(), request.getQueryString(), expectedSize, (content != null), debug);
    }

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
                "##DOWNLOAD_DEBUG##%nExpected-Size: %d%nProbed-Readable-Bytes (capped): %d%n",
                expectedSize, probed);
            return;
        }

        // ---------------- STREAM ON-THE-FLY ----------------
        final String ct = content.getContentType();
        response.reset();
        response.setContentType(ct != null ? ct : "application/octet-stream");

        String asciiName = content.getName().replaceAll("[^\\x20-\\x7E]", "_");
        String encodedName = URLEncoder.encode(content.getName(), StandardCharsets.UTF_8.toString());
        String disposition = response.getContentType().startsWith("image/") ? "inline" : "attachment";
        response.setHeader("Content-Disposition",
                disposition + "; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName);
        response.setContentLengthLong(expectedSize);

        long total = 0L;
        try (InputStream is = new BufferedInputStream(content.getInputStream());
             OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[64 * 1024];
            int r;
            while ((r = is.read(buf)) != -1) {
                os.write(buf, 0, r);
                total += r;
            }
            os.flush();
        }

        if (_log.isDebugEnabled()) {
            _log.debug("[DOWNLOAD-JSP] Download sent. expected={} actual={}", expectedSize, total);
        }
        if (total != expectedSize) {
            _log.warn("[DOWNLOAD-JSP] Size mismatch after streaming: expected={} actual={}", expectedSize, total);
        }

    } catch (ECMWFException e) {
        _log.error("[DOWNLOAD-JSP] ECMWFException: {}", e.getFullMessage(), e);
        if (!response.isCommitted()) writeError(response, e.getFullMessage());

    } catch (IOException e) {
        _log.error("[DOWNLOAD-JSP] IOException: {}", e.getMessage(), e);
        if (!response.isCommitted()) writeError(response, e.getMessage());
    }
%>