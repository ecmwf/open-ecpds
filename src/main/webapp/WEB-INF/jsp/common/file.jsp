<%@ page import="java.io.*" %><%@ page import="ecmwf.web.ECMWFException" %><%@ page import="ecmwf.web.services.content.Content" %><%

    // The first tags are in a single line to avoid sending carriage returns to the 
    // OutputStream before getting it (which would result in a Tomcat warning, and that being lucky ;-))


       try {
       
       	  Content content = (Content)(request.getAttribute("content"));
          response.setContentType(content.getContentType());
          if (content.getContentType().startsWith("image/"))
          	  response.addHeader("Content-Disposition","inline; filename="+content.getName());	
          else
	          response.addHeader("Content-Disposition","attachment; filename="+content.getName());
          InputStream is = content.getInputStream();
          OutputStream o = response.getOutputStream();

          byte[] buf = new byte[32 * 1024]; // 32k buffer
          int nRead = 0;
          while( (nRead=is.read(buf)) != -1 ) {
            o.write(buf, 0, nRead);
          }
          o.flush();
          o.close();// *important* to ensure no more jsp output
          is.close();
          return; 
      } catch (ECMWFException e) {
          response.setContentType("text/plain");
          Writer o2 = response.getWriter();
          o2.write("File not found in storage area: "+e.getFullMessage());
          o2.flush();
          o2.close();         
      }

%>