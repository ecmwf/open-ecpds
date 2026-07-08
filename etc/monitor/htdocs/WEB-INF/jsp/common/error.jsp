<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

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

<%!
private static String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
}
%>
<%
Exception e        = (Exception) request.getAttribute("ecmwf.request.exception");
Integer statusCode = (Integer)   request.getAttribute("javax.servlet.error.status_code");
String  requestUri = (String)    request.getAttribute("javax.servlet.error.request_uri");
String  servletMsg = (String)    request.getAttribute("javax.servlet.error.message");

String message    = null;
String alertClass = "alert-warning";
String iconClass  = "bi-exclamation-triangle-fill";
boolean is404     = statusCode != null && statusCode == 404;

if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
    message = esc(e.getMessage());
} else if (servletMsg != null && !servletMsg.trim().isEmpty()) {
    message = esc(servletMsg);
}

if (statusCode != null) {
    if (statusCode == 404) {
        alertClass = "alert-warning";
        iconClass  = "bi-question-circle-fill";
        if (message == null || message.equals("Invalid path was requested")) {
            message = "Page not found" + (requestUri != null ? ": <code>" + esc(requestUri) + "</code>" : ".");
        }
    } else if (statusCode == 401 || statusCode == 403) {
        alertClass = "alert-danger";
        iconClass  = "bi-shield-exclamation";
        if (message == null) {
            message = "Access denied" + (requestUri != null ? " &mdash; you are not authorised to view <code>" + esc(requestUri) + "</code>." : ".");
        }
    } else if (statusCode == 500) {
        alertClass = "alert-danger";
        iconClass  = "bi-bug-fill";
        if (message == null) message = "An internal server error occurred. Please try again or contact the system administrator.";
    }
}
if (message == null) {
    message = "An unexpected error occurred" + (requestUri != null ? " while accessing <code>" + esc(requestUri) + "</code>" : "") + ".";
}
%>

<div class="alert <%=alertClass%> d-flex align-items-start gap-2 mt-3" role="alert">
  <i class="bi <%=iconClass%> flex-shrink-0 mt-1" style="font-size:1.1rem;"></i>
  <div><%=message%></div>
</div>

<% if (is404) { %>
<p class="text-muted mt-3 mb-2" style="font-size:0.9rem;">
  <i class="bi bi-compass me-1"></i>Here are the sections available to you:
</p>

<div class="row g-3">

  <%-- Data Storage --%>
  <auth:if basePathKey="datafile.basepath" paths="/">
    <auth:then>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
      <div class="home-section-card">
        <div class="home-section-hdr" style="background:#e8f4fd;">
          <a href="/do/datafile" class="home-section-hdr-link"><i class="bi bi-database text-primary"></i> Data Storage</a>
        </div>
        <ul class="home-menu">
          <auth:link basePathKey="datafile.basepath" href="" wrappingTags="li"><i class="bi bi-file-earmark-text"></i>Data Files</auth:link>
          <auth:link basePathKey="metadata.basepath" href="" wrappingTags="li"><i class="bi bi-tags"></i>Meta Data</auth:link>
          <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="li"><i class="bi bi-collection"></i>Transfer Groups</auth:link>
          <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="li"><i class="bi bi-server"></i>Data Movers</auth:link>
          <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="li"><i class="bi bi-speedometer2"></i>Retrieval Rates</auth:link>
          <auth:link basePathKey="moverdownloads.basepath" href="" wrappingTags="li"><i class="bi bi-grid-3x3"></i>Download Activity</auth:link>
          <auth:link basePathKey="datarates.basepath" href="" wrappingTags="li"><i class="bi bi-bar-chart-line"></i>Data Rates</auth:link>
          <auth:link basePathKey="portaltraffic.basepath" href="" wrappingTags="li"><i class="bi bi-graph-up-arrow"></i>Portal Traffic</auth:link>
        </ul>
      </div>
    </div>
    </auth:then>
  </auth:if>

  <%-- Transmission --%>
  <auth:if basePathKey="datatransfer.basepath" paths="/">
    <auth:then>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
      <div class="home-section-card">
        <div class="home-section-hdr" style="background:#e9f7ef;">
          <a href="/do/transfer" class="home-section-hdr-link"><i class="bi bi-send"></i> Transmission</a>
        </div>
        <ul class="home-menu">
          <auth:link basePathKey="datatransfer.basepath" href="" wrappingTags="li"><i class="bi bi-arrow-left-right"></i>Data Transfers</auth:link>
          <auth:link basePathKey="destination.basepath" href="?destinationType=-1" wrappingTags="li"><i class="bi bi-geo-alt"></i>Destinations</auth:link>
          <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
              <auth:link basePathKey="destination.basepath" href="?destinationType=-2" wrappingTags="li"><i class="bi bi-broadcast"></i>Dissemination</auth:link>
              <auth:link basePathKey="destination.basepath" href="?destinationType=-3" wrappingTags="li"><i class="bi bi-cloud-download"></i>Acquisition</auth:link>
            </auth:then>
          </auth:if>
          <auth:link basePathKey="host.basepath" href="" wrappingTags="li"><i class="bi bi-pc-display"></i>Transfer Hosts</auth:link>
          <auth:link basePathKey="transferhistory.basepath" href="" wrappingTags="li"><i class="bi bi-clock-history"></i>Transfer History</auth:link>
          <auth:link basePathKey="method.basepath" href="" wrappingTags="li"><i class="bi bi-diagram-3"></i>Transfer Methods</auth:link>
          <auth:link basePathKey="module.basepath" href="" wrappingTags="li"><i class="bi bi-puzzle"></i>Transfer Modules</auth:link>
        </ul>
      </div>
    </div>
    </auth:then>
  </auth:if>

  <%-- Access Control --%>
  <auth:if basePathKey="user.basepath" paths="/">
    <auth:then>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
      <div class="home-section-card">
        <div class="home-section-hdr" style="background:#fff8e6;">
          <a href="/do/user" class="home-section-hdr-link"><i class="bi bi-shield-lock text-warning"></i> Access Control</a>
        </div>
        <ul class="home-menu">
          <auth:link basePathKey="user.basepath" href="" wrappingTags="li"><i class="bi bi-people"></i>Web Users</auth:link>
          <auth:link basePathKey="category.basepath" href="" wrappingTags="li"><i class="bi bi-folder"></i>Web Categories</auth:link>
          <auth:link basePathKey="resource.basepath" href="" wrappingTags="li"><i class="bi bi-files"></i>Web Resources</auth:link>
          <auth:link basePathKey="event.basepath" href="" wrappingTags="li"><i class="bi bi-journal-text"></i>Web Events Log</auth:link>
          <auth:link basePathKey="incoming.basepath" href="" wrappingTags="li"><i class="bi bi-person-badge"></i>Data Users</auth:link>
          <auth:link basePathKey="policy.basepath" href="" wrappingTags="li"><i class="bi bi-shield-check"></i>Data Policies</auth:link>
          <auth:link basePathKey="history.basepath" href="" wrappingTags="li"><i class="bi bi-list-ul"></i>Data Events Log</auth:link>
        </ul>
      </div>
    </div>
    </auth:then>
  </auth:if>

  <%-- Admin Tasks --%>
  <auth:if basePathKey="admin.basepath" paths="/">
    <auth:then>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
      <div class="home-section-card">
        <div class="home-section-hdr" style="background:#f3f4f6;">
          <a href="/do/admin" class="home-section-hdr-link"><i class="bi bi-gear text-secondary"></i> Admin Tasks</a>
        </div>
        <ul class="home-menu">
          <auth:link basePathKey="admin.basepath" href="/filter" wrappingTags="li"><i class="bi bi-file-zip"></i>Compress Files</auth:link>
          <auth:link basePathKey="admin.basepath" href="/requeue" wrappingTags="li"><i class="bi bi-hourglass-split"></i>Outstanding Transfers</auth:link>
          <auth:link basePathKey="admin.basepath" href="/upload" wrappingTags="li"><i class="bi bi-upload"></i>Upload Files</auth:link>
          <auth:link basePathKey="admin.feedback.basepath" href="" wrappingTags="li"><i class="bi bi-chat-left-text"></i>User Feedback</auth:link>
        </ul>
      </div>
    </div>
    </auth:then>
  </auth:if>

  <%-- Monitoring --%>
  <auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
      <div class="home-section-card">
        <div class="home-section-hdr" style="background:#e8f7f7;">
          <auth:if basePathKey="monitoring.basepath" paths="/">
            <auth:then><a href="/do/monitoring" class="home-section-hdr-link"><i class="bi bi-eye text-info"></i> Monitoring</a></auth:then>
            <auth:else><span class="home-section-hdr-link"><i class="bi bi-eye text-info"></i> Monitoring</span></auth:else>
          </auth:if>
        </div>
        <ul class="home-menu">
          <auth:link basePathKey="monitoring.basepath" href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&" wrappingTags="li"><i class="bi bi-broadcast-pin"></i>Dissemination</auth:link>
          <auth:link basePathKey="monitoring.basepath" href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&" wrappingTags="li"><i class="bi bi-cloud-arrow-down"></i>Acquisition</auth:link>
        </ul>
      </div>
    </div>
    </auth:then>
  </auth:if>

</div>
<% } %>

<!-- common/error.jsp -->