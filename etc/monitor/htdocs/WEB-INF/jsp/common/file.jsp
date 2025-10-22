<%@ page import="java.io.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="ecmwf.web.ECMWFException" %>
<%@ page import="ecmwf.web.services.content.Content" %>
<%
Content content = (Content) request.getAttribute("content");
if (content == null) {
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.getWriter().write("No content provided.");
    return;
}

try {
    response.setContentType(content.getContentType());

    // Encode filename for modern browsers (UTF-8)
    String encodedName = URLEncoder.encode(content.getName(), StandardCharsets.UTF_8.toString());

    // Fallback ASCII filename for older browsers
    String asciiName = content.getName().replaceAll("[^\\x20-\\x7E]", "_");

    String disposition = content.getContentType().startsWith("image/") ? "inline" : "attachment";

    response.setHeader("Content-Disposition",
        disposition + "; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName
    );

    try (InputStream is = content.getInputStream();
         OutputStream os = response.getOutputStream()) {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
    }

    return; // stop JSP from adding any extra output

} catch (ECMWFException e) {
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    try (Writer w = response.getWriter()) {
        w.write("File not found in storage area: " + e.getFullMessage());
    }
} catch (IOException e) {
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    try (Writer w = response.getWriter()) {
        w.write("Error sending file: " + e.getMessage());
    }
}
%>