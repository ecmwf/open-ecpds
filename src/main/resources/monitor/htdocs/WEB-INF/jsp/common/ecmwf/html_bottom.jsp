</html>

<%
	// Profiling
	java.util.Date after = new java.util.Date();
	java.util.Date before = (java.util.Date)request.getAttribute("jsp_date_before");
	org.apache.commons.logging.LogFactory.getLog(this.getClass()).info("Time taken for JSP ("+this.getClass().getName()+"): "+((after.getTime()-before.getTime())/1000.0)+" seconds.");
%>
