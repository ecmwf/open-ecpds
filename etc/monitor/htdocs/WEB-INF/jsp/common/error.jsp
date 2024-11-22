<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<!-- common/error.jsp -->

<script>
	// If the path is of monitoring or a destination page, try to reload it.
	var monitoring = '<bean:message key="monitoring.basepath"/>';
	var destination = '<bean:message key="destination.basepath"/>';
	var path = window.location.pathname;

	if ((path.substring(0, monitoring.length) === monitoring) || 
		((path.substring(0, destination.length) === destination) && (path.indexOf('/', destination.length + 1) < 0))) {
		
		// Reload the page at specified intervals
		setTimeout(function() {
			window.location.reload(true);
		}, 30000);

		setTimeout(function() {
			window.location.reload(true);
		}, 60000);

		setTimeout(function() {
			window.location.reload(true);
		}, 300000);
	} 
</script>

<p/>
<%
Exception e = (Exception) request.getAttribute("ecmwf.request.exception");
String message = e != null ? e.getMessage() : "";
%>

<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  <%=message%>
</div>

<!-- common/error.jsp -->