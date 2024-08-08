<%@ page import="java.io.*" %><jsp:useBean id="image" scope="request" type="java.awt.image.BufferedImage" /><jsp:useBean id="contentType" scope="request" type="java.lang.String" /><%
    // The first tags are in a single line to avoid sending carriage returns to the
    // OutputStream before getting it (which would result in a Tomcat warning, and that being lucky ;-))
    try {
	final OutputStream os = response.getOutputStream();
	if (contentType.equals("image/png")) {
		response.setContentType(contentType);
		javax.imageio.ImageIO.write(image,"png",os);
	}  else {
		response.setContentType("text/plain");
		os.write(("No conversion for content-type: " + contentType).getBytes());
	}
    } catch (IOException e) {
    }
%>