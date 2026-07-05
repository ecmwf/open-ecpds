<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/host/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

<style>
.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px lightgray;
	border-radius: 4px;
	padding: 8px;
}
table.fields {
	width: 100%;
	min-width: 600px;
}
table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}
#dir {
	width: 100%;
	min-width: 300px;
	height: 375px;
	resize: vertical;
	overflow: hidden;
}
.progress-terminal { border-radius: 4px; overflow: hidden; margin-top: 2px; }
.progress-terminal-hdr {
    background: #2d2d2d; color: #ccc; padding: 0.25rem 0.75rem;
    font-size: 0.75rem; border-bottom: 1px solid #444;
    display: flex; align-items: center; gap: 0.4rem;
}
.progress-terminal-body {
    background: #1e1e1e; color: #d4d4d4;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 0.75rem; line-height: 1.7;
    padding: 0.5rem 0.75rem;
    height: 200px; min-height: 80px; max-height: 500px;
    overflow-y: auto; resize: vertical;
    white-space: pre-wrap; word-break: break-word;
}
.progress-terminal-body font[color="black"],
.progress-terminal-body font[color="#000000"] { color: #6c9abd; }
.progress-terminal-body font[color="red"]   { color: #f88; }
.progress-terminal-body font[color="green"] { color: #8f8; }
.progress-terminal-body a { color: #79b8ff; text-decoration: none; }
.progress-terminal-body a:hover { text-decoration: underline; }
.progress-terminal-btn { color: rgba(255,255,255,0.5); border-color: #555; }
/* Light theme overrides */
[data-bs-theme=light] .progress-terminal-hdr { background: #f0f2f4; color: #24292f; border-bottom-color: #d0d7de; }
[data-bs-theme=light] .progress-terminal-body { background: #f6f8fa; color: #24292f; }
[data-bs-theme=light] .progress-terminal-body font[color="black"],
[data-bs-theme=light] .progress-terminal-body font[color="#000000"] { color: #0550ae; }
[data-bs-theme=light] .progress-terminal-body font[color="red"]   { color: #cf222e; }
[data-bs-theme=light] .progress-terminal-body font[color="green"] { color: #1a7f37; }
[data-bs-theme=light] .progress-terminal-body a { color: #0969da; }
[data-bs-theme=light] .progress-terminal-btn { color: #57606a; border-color: #d0d7de; }
[data-bs-theme=light] .progress-terminal-btn:hover { color: #24292f; }
/* Accordion header with inline help trigger */
</style>

	<c:set var="authorized" value="false" />

	<auth:if basePathKey="destination.basepath" paths="/">
		<auth:then>
			<c:set var="authorized" value="true" />
		</auth:then>
	</auth:if>

	<auth:if basePathKey="transferhistory.basepath" paths="/">
		<auth:then>
		</auth:then>
		<auth:else>

			<c:forEach var="destination" items="${host.destinations}">
				<auth:if basePathKey="destination.basepath"
					paths="/${destination.name}">
					<auth:then>
						<c:set var="authorized" value="true" />
					</auth:then>
				</auth:if>
			</c:forEach>

		</auth:else>
	</auth:if>

	<c:if test="${authorized == 'false'}">
		<br />
		<div class="alert">
			Error getting object <- Problem searching by key '${host.name}' <-
			Host not found: {${host.name}}
		</div>
	</c:if>

	<c:if test="${authorized == 'true'}">

		<tiles:importAttribute name="isDelete" ignore="true" />
		<c:if test="${not empty isDelete}">
			<tiles:insert page="./pds/transfer/host/warning.jsp" />
		</c:if>
		<c:if test="${empty isDelete}">
			<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>
			<c:if test="${!(isRestrictedUser == 'true' && host.type == 'Proxy')}">
			<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-tag text-primary"></i>
<span class="fw-semibold">Identity</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Hostname/IP</div><div class="field-value"><span class="val-code">${host.host}</span></div></div>
<div class="field-row"><div class="field-label">Label</div><div class="field-value"><c:choose><c:when test="${not empty host.networkCode or not empty host.networkName}"><span class="val-code">${host.networkCode}:${host.networkName}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Login</div><div class="field-value"><c:choose><c:when test="${not empty host.login}"><span class="val-code">${host.login}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Transfer Group</div><div class="field-value"><c:choose><c:when test="${not empty host.transferGroupName}"><auth:link basePathKey="transfergroup.basepath" href="/${host.transferGroupName}" alternativeText="${host.transferGroupName}">${host.transferGroupName}</auth:link></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Passwd</div><div class="field-value"><span class="val-code">****</span></div></div>
<div class="field-row"><div class="field-label">Max Connections</div><div class="field-value"><span class="val-num">${host.maxConnections}</span></div></div>
<div class="field-row"><div class="field-label">Filter Name</div><div class="field-value"><c:choose><c:when test="${not empty host.filterName}"><jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${host.filterName}"/><jsp:param name="showName" value="true"/></jsp:include></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>
</c:if>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-geo-alt text-primary"></i>
<span class="fw-semibold">Location</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Location Source</div><div class="field-value"><c:choose><c:when test="${host.automaticLocation}"><span class="badge rounded-pill border fw-normal bg-info-subtle text-info-emphasis"><i class="bi bi-robot me-1"></i>Automatic</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-body-secondary"><i class="bi bi-pencil me-1"></i>Manual</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Estimated Location</div><div class="field-value"><c:choose><c:when test="${not host.automaticLocation}"><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">n/a &mdash; manually set</span></c:when><c:when test="${not empty host.geoIpLocation}"><c:set var="_geoParts" value="${fn:split(host.geoIpLocation, '/')}"/><c:set var="_geoPart0" value="${fn:trim(_geoParts[0])}"/><c:set var="_geoPart1" value="${fn:trim(_geoParts[1])}"/><c:set var="_geoIso" value="${fn:toLowerCase(fn:length(_geoPart0) == 2 ? _geoPart0 : _geoPart1)}"/><span class="d-inline-flex align-items-center gap-1"><c:if test="${fn:length(_geoIso) == 2}"><span class="fi fi-${_geoIso}" title="${host.geoIpLocation}" style="font-size:1.1em;border-radius:2px;"></span></c:if><span>${host.geoIpLocation}</span></span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Latitude (&deg;)</div><div class="field-value"><span class="val-num">${host.latitude}</span></div></div>
<div class="field-row"><div class="field-label">Longitude (&deg;)</div><div class="field-value"><span class="val-num">${host.longitude}</span></div></div>
</div>
</div>
</div>

<c:if test="${!(isRestrictedUser == 'true' && host.type == 'Proxy')}">
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" id="hostViewDirCardHeader" style="background:var(--bs-secondary-bg)">
<i class="bi bi-folder2-open text-primary"></i>
<span class="fw-semibold">Directory</span>
<auth:if basePathKey="host.basepath" paths="/edit/insert_form">
<auth:then>
<span class="ms-auto" style="position:relative;display:inline-block">
<button type="button" id="testDir" name="testDir" class="btn btn-sm btn-outline-secondary">Test on Server</button>
</span>
</auth:then>
</auth:if>
</div>
<div class="card-body">
<div id='dirType'>
<input type='radio' id='istext' name='dirType' />Plain Text <input
type='radio' id='isjs' name='dirType' />JavaScript <input
type='radio' id='ispython' name='dirType' />Python
</div>
<div class="ace-panel">
<pre id="dir">
<c:out value="${host.dir}" />
</pre> <textarea id="dir" name="dir" style="display: none;"></textarea>
</div>
</div>
</div>
</c:if>

<c:if test="${!(isRestrictedUser == 'true' && host.type == 'Proxy')}">
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-sliders text-primary"></i>
<span class="fw-semibold">Options</span>
</div>
<div class="card-body">
<div class="accordion" id="hostViewOptionsAccordion">
<div class="accordion-item">
<h2 class="accordion-header" id="hostViewAccHeadProperties" style="position:relative;">
<button class="accordion-button collapsed" id="hostViewAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#hostViewAccProperties" aria-expanded="false" aria-controls="hostViewAccProperties">
Properties
</button>
<span role="button" tabindex="0" class="acc-help-btn" id="hostPropsHelpBtn"
onclick="openHostHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openHostHelp();" title="Open properties reference">
<i class="bi bi-question-circle"></i>
</span>
</h2>
<div id="hostViewAccProperties" class="accordion-collapse collapse" aria-labelledby="hostViewAccHeadProperties" data-bs-parent="#hostViewOptionsAccordion">
<div class="accordion-body p-2">
<div class="ace-panel">
<pre id="properties"><c:out value="${host.properties}" /></pre>
<textarea id="properties" name="properties" style="display: none;"></textarea>
</div>
</div>
</div>
</div>
<div class="accordion-item">
<h2 class="accordion-header" id="hostViewAccHeadJavascript">
<button class="accordion-button collapsed" id="hostViewAccJavascriptBtn" type="button" data-bs-toggle="collapse" data-bs-target="#hostViewAccJavascript" aria-expanded="false" aria-controls="hostViewAccJavascript">
JavaScript
</button>
</h2>
<div id="hostViewAccJavascript" class="accordion-collapse collapse" aria-labelledby="hostViewAccHeadJavascript" data-bs-parent="#hostViewOptionsAccordion">
<div class="accordion-body p-2">
<div class="ace-panel">
<pre id="javascript"><c:out value="${host.javascript}" /></pre>
<textarea id="javascript" name="javascript" style="display: none;"></textarea>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</c:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>

<c:if test="${host.type == 'Acquisition'}">
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-terminal text-primary"></i>
<span class="fw-semibold">Acquisition Console</span>
<span id="acqStatusBadge" class="ms-1"></span>
<div class="ms-auto">
<a href='<bean:message key="host.basepath"/>/edit/getOutput/view/${host.id}'
   class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
   style="font-size:0.7rem;" title="Open Acquisition Console">
<i class="bi bi-box-arrow-up-right me-1"></i>Open Console
</a>
</div>
</div>
<c:if test="${not empty acquisitionNote}">
<div class="d-flex align-items-start gap-2 px-3 py-2" style="background:rgba(255,193,7,0.12);border-bottom:1px solid rgba(255,193,7,0.3);font-size:0.82rem;color:var(--bs-warning-text-emphasis,#664d03)">
  <i class="bi bi-exclamation-triangle-fill mt-1" style="flex-shrink:0;color:#f0ad4e"></i>
  <span><strong>Run Now is disabled:</strong> <c:out value="${acquisitionNote}" escapeXml="false"/>. The Acquisition Scheduler requires at least one associated destination to be active and running.</span>
</div>
</c:if>
<div class="card-body p-0">
<div class="progress-terminal" style="width:100%;border-radius:0 0 var(--bs-card-border-radius) var(--bs-card-border-radius)">
<div class="progress-terminal-body" id="acqProgressPreview"
     style="height:90px;min-height:0;max-height:90px;resize:none;border-radius:0 0 var(--bs-card-border-radius) var(--bs-card-border-radius)">${host.formattedLastOutput}</div>
</div>
</div>
</div>
<script>
(function() {
    var hostId = '<c:out value="${host.id}"/>';
    document.addEventListener('DOMContentLoaded', function() {
        var b = document.getElementById('acqProgressPreview');
        if (b) b.scrollTop = b.scrollHeight;
        fetch('/do/transfer/host/' + encodeURIComponent(hostId) + '?json=acquisitionRunning')
            .then(function(r) { return r.ok ? r.json() : null; })
            .then(function(data) {
                var el = document.getElementById('acqStatusBadge');
                if (!el) return;
                if (data && data.running) {
                    el.innerHTML = '<span class="badge bg-primary ms-1" style="font-size:0.65rem;vertical-align:middle">'
                        + '<span class="spinner-border spinner-border-sm me-1" style="width:.5rem;height:.5rem;vertical-align:middle"></span>Running</span>';
                } else {
                    el.innerHTML = '<span class="badge bg-secondary ms-1" style="font-size:0.65rem;vertical-align:middle">Idle</span>';
                }
            })
            .catch(function() {});
    });
}());
</script>
</c:if>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-bar-chart text-primary"></i>
<span class="fw-semibold">Statistics</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Retry Frequency</div><div class="field-value"><c:choose><c:when test="${not empty host.formattedRetryFrequency}"><span class="val-num">${host.formattedRetryFrequency}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Total Data Sent</div><div class="field-value"><a style="text-decoration:none" title="Sent: ${host.formattedSent}"><span class="val-num">${host.sent} bytes</span></a></div></div>
<div class="field-row"><div class="field-label">Connections</div><div class="field-value"><span class="val-num">${host.connections}</span></div></div>
<div class="field-row"><div class="field-label">Total Time Taken</div><div class="field-value"><c:choose><c:when test="${not empty host.formattedDuration}"><span class="val-num">${host.formattedDuration}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Retry Count</div><div class="field-value"><span class="val-num">${host.retryCount}</span></div></div>
<div class="field-row"><div class="field-label">Bandwidth</div><div class="field-value"><a style="text-decoration:none" title="Bandwidth: ${host.formattedBandWidth}"><span class="val-num">${host.formattedBandWidthInMBitsPerSeconds} Mbits/s</span></a></div></div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-bell text-primary"></i>
<span class="fw-semibold">Monitoring</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Check</div><div class="field-value"><c:choose><c:when test="${host.check}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<c:if test="${not empty host.userMail}">
<div class="field-row"><div class="field-label">Mail On Success</div><div class="field-value"><c:choose><c:when test="${host.mailOnSuccess}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
</c:if>
<c:if test="${host.check}">
<div class="field-row"><div class="field-label">Check Time</div><div class="field-value"><c:choose><c:when test="${not empty host.checkTime}"><span class="date">${host.checkTime}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</c:if>
<c:if test="${not empty host.userMail}">
<div class="field-row"><div class="field-label">Mail On Error</div><div class="field-value"><c:choose><c:when test="${host.mailOnError}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
</c:if>
<c:if test="${host.check}">
<div class="field-row"><div class="field-label">Check Frequency</div><div class="field-value"><c:choose><c:when test="${not empty host.formattedCheckFrequency}"><span class="val-num">${host.formattedCheckFrequency}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Notify Once</div><div class="field-value"><c:choose><c:when test="${host.notifyOnce}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
</c:if>
<c:if test="${host.type == 'Acquisition'}">
<div class="field-row"><div class="field-label">Acquisition Time</div><div class="field-value"><c:choose><c:when test="${not empty host.acquisitionTime}"><span class="date">${host.acquisitionTime}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Acquisition Frequency</div><div class="field-value"><c:choose><c:when test="${not empty host.formattedAcquisitionFrequency}"><span class="val-num">${host.formattedAcquisitionFrequency}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<auth:if basePathKey="transferhistory.basepath" paths="/"><auth:then>
<div class="field-row"><div class="field-label">Valid</div><div class="field-value"><c:choose><c:when test="${host.valid}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
</auth:then></auth:if>
</c:if>
<c:if test="${not empty host.userMail}">
<div class="field-row"><div class="field-label">Owner Mail</div><div class="field-value"><span class="val-code">${host.userMail}</span></div></div>
</c:if>
</div>
</div>
</div>

</auth:then>
</auth:if>


			<c:if test="${host.type != 'Replication' && host.type != 'Source' && host.type != 'Backup'}">
				<%-- Count how many destinations the current user is authorised to see --%>
				<c:set var="visibleDestCount" value="0" />
				<c:forEach var="destination" items="${host.destinations}">
				  <auth:if basePathKey="destination.basepath" paths="/${destination.name}">
				    <auth:then>
				      <c:set var="visibleDestCount" value="${visibleDestCount + 1}" />
				    </auth:then>
				  </auth:if>
				</c:forEach>
				<div class="card assoc-card mt-3" style="max-width:480px">
				  <div class="card-header">
				    <i class="bi bi-geo-alt text-secondary"></i>
				    <strong>Destination(s) using this Host</strong>
				  </div>
				  <div class="card-body p-2">
				    <c:choose>
				      <c:when test="${visibleDestCount == 0}">
				        <p class="text-muted small mb-0"><em>No destinations assigned.</em></p>
				      </c:when>
				      <c:otherwise>
				        <div class="d-flex flex-wrap">
				          <c:forEach var="destination" items="${host.destinations}">
				            <auth:if basePathKey="destination.basepath" paths="/${destination.name}">
				              <auth:then>
				                <span class="assoc-chip">
				                  <a href="<bean:message key="destination.basepath"/>/${destination.id}" title="${destination.comment}" class="text-decoration-none text-body">${destination.name}</a>
				                </span>
				              </auth:then>
				            </auth:if>
				          </c:forEach>
				        </div>
				      </c:otherwise>
				    </c:choose>
				  </div>
				</div>
			</c:if>
		</c:if>
	</c:if>

	<%-- HTTP/MQTT Module Configuration Guide (Acquisition hosts only) --%>
	<c:if test="${not empty moduleGuide}"><jsp:include page="${moduleGuide}"/></c:if>

	<%-- Help offcanvas panel --%>
	<div class="offcanvas offcanvas-end" tabindex="-1" id="hostHelpOffcanvas"
	     aria-labelledby="hostHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
		<div class="offcanvas-header border-bottom py-2 px-3">
			<h6 class="offcanvas-title mb-0 fw-semibold" id="hostHelpOffcanvasLabel">
				<i class="bi bi-book me-2 text-primary"></i>Properties Reference
			</h6>
			<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
		</div>
		<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
			<div id="hostViewHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
			<div id="hostViewHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
		</div>
	</div>

	<script>		
		var editorDir = getEditorProperties(true, false, "dir", "toml");
		makeResizable(editorDir);

		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});
		
		// Get the completions from the bean!
		var hostType = "${host.type}";
		var transferMethodName = '${host.transferMethodName}';
		var transferModuleName = '${host.transferMethod.ecTransModuleName}';
		var transferModuleNames = [${host.transferModuleNames}];
    	var completions = [
    		${host.completions}
    	];
    	
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#hostViewHelpContent').html(getHelpHtmlContent(
    				completions.filter(function(item) {
    	           		var moduleName = item.caption.split(".")[0];
    	       	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
    	       	    		return false;
    	       	    	} else {
    	          			if ((!["Acquisition", "Source"].includes(hostType) && moduleName === "retrieval")
    	          					|| (!["Dissemination", "Proxy"].includes(hostType) && moduleName === "upload")
    	          					|| (hostType !== "Acquisition"  && moduleName === "acquisition")
    	          					|| (hostType !== "Proxy" && moduleName === "proxy")) {
    	           	    		return false;
    	          			} else {
    	          				return true;
    	          			}
    	       	    	}
    	    	    }), 'Available Options for Host of Type ' + hostType + ' with Transfer Method ' + transferMethodName))
    		/* Move the group nav pills to the fixed slot above the scrollable area */
    		var navEl = document.querySelector('#hostViewHelpContent .help-nav');
    		if (navEl) document.getElementById('hostViewHelpNav').appendChild(navEl);
    	});
    	
    	// Overwrite the original method to deal with the specificities of the host
    	function getAnnotations(aceEditor, row) {
        	const pattern = /^[^\s.]+\.[^\s.]+\s*=(.*)$/;
      		var currentLine = aceEditor.session.getLine(row).replace(/^\t+/, '');
        	for (var j = 0; j < completions.length; j++) {
        		var tipObject = completions[j];
        		var withoutSpace = currentLine.replace(/\s/g, "");
           		if (withoutSpace === tipObject.caption || withoutSpace.startsWith(tipObject.caption + "=")) {
           		    var moduleName = tipObject.caption.split(".")[0];
           	    	var tipsText;
           	    	var tipsType;
           	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
           	    		tipsText = "Ignored as \"" + moduleName + "\" is not the selected transfer method. The selected transfer method is \"" + transferModuleName + "\".";
           	    		tipsType = "warning"
           	    	} else {
              			if ((!["Acquisition", "Source"].includes(hostType) && moduleName === "retrieval")
	          					|| (!["Dissemination", "Proxy"].includes(hostType) && moduleName === "upload")
              					|| (hostType !== "Acquisition"  && moduleName === "acquisition")
              					|| (hostType !== "Proxy" && moduleName === "proxy")) {
                   	    	tipsText = "Ignored as \"" + moduleName + "\" parameters are not valid for " + hostType + " hosts.";
               	    		tipsType = "warning";
              			} else {
               	    		var error = checkValueForType(tipObject.type, tipObject.choices, currentLine);
               	    		if (error != null) {
               	    			tipsText = error;
               	    			tipsType = "error";
               	    		} else {
               	    			tipsText = tipObject.tips;
               	    			tipsType = "info";
               	    		}
              			}
           	    	}
            		return [{
               			row: row,
               		    column: 0,
               		    text: tipsText,
               			type: tipsType,
               	   	 	module: moduleName
               		}];
            	}
        	}
        	// If no match found let's put a tips if it looks like a parameter!
        	if (currentLine.length > 0 && pattern.test(currentLine) && !currentLine.startsWith('#')) {
        		return [{
            		row: row,
                	column: 0,
                	text: "This option is not recognized.",
                	type: "error"
            	}];
        	}
        	return [];
   		}
    	
    	// Call the function to process each line
    	checkEachLine(editorProperties, 'hostViewAccPropertiesBtn');
    	
		// Add a click event listener to the properties editor
    	editorProperties.addEventListener("changeSelection", function (event) {
    		checkEachLine(editorProperties, 'hostViewAccPropertiesBtn');
    		/* Live-track help panel when offcanvas is open */
    		var _oc = document.getElementById('hostHelpOffcanvas');
    		if (_oc && _oc.classList.contains('show')) {
    			_scrollHelpToCursor();
    		}
    	});

		var editorJavascript = getEditorProperties(true, false, "javascript", "javascript");
		editorJavascript.setOptions({minLines: 10, maxLines: 20});

		document.getElementById('hostViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		document.getElementById('hostViewAccJavascript').addEventListener('shown.bs.collapse', function() {
			editorJavascript.resize(true);
		});

		/* Help offcanvas */
		function _scrollHelpToCursor() {
			var row = editorProperties.selection.getCursor().row;
			var line = editorProperties.session.getLine(row) || '';
			line = line.trim();
			if (line && !line.startsWith('#') && !line.startsWith('//')) {
				var eqIdx = line.indexOf('=');
				var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
				if (paramName) scrollHelpToParam('hostViewHelpContent', paramName);
			}
		}
		window.openHostHelp = function() {
			var el = document.getElementById('hostHelpOffcanvas');
			if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
		};
		var _helpOffcanvasEl = document.getElementById('hostHelpOffcanvas');
		if (_helpOffcanvasEl) {
			_helpOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
				var btn = document.getElementById('hostPropsHelpBtn');
				if (btn) btn.classList.add('acc-help-active');
			});
			_helpOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
				_scrollHelpToCursor();
			});
			_helpOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
				var btn = document.getElementById('hostPropsHelpBtn');
				if (btn) btn.classList.remove('acc-help-active');
			});
		}

		makeResizable(editorProperties);
		makeResizable(editorJavascript);

		editorJavascript.getSession().on('changeAnnotation', function() {
			applyAnnotationMarkers(editorJavascript, 'hostViewAccJavascriptBtn');
		});
		editorDir.getSession().on('changeAnnotation', function() {
			applyAnnotationMarkers(editorDir, 'hostViewDirCardHeader');
		});

		window.addEventListener('resize', function() {
			editorDir.resize(true);
			editorProperties.resize(true);
			editorJavascript.resize(true);
		});

		$('#istext').prop('disabled', true);
		$('#isjs').prop('disabled', true);
		$('#ispython').prop('disabled', true);

		var dirType = getEditorType(editorDir);
		$('#is' + dirType).prop('checked', true);
		$('#is' + dirType).prop('disabled', false);
		var dirMode = (dirType === "js" ? "javascript" : dirType === "text" ? "toml" : dirType);
		editorDir.session.setMode("ace/mode/" + dirMode);
		if (dirMode === "python") {
			checkPythonSyntax(editorDir, 'hostViewDirCardHeader');
		} else if (dirMode !== "javascript") {
			editorDir.session.clearAnnotations();
			applyAnnotationMarkers(editorDir, 'hostViewDirCardHeader');
		}

		// Wire up the Test on Server button in view mode
		(function() {
			var btn = document.getElementById('testDir');
			if (!btn) return;
			var hostId = '<c:out value="${host.id}"/>';
			btn.addEventListener('click', function(e) {
				e.preventDefault();
				try {
					if (document.getElementById('istext') && document.getElementById('istext').checked) {
						testDirTextOnServer(editorDir, hostId);
					} else {
						var lang = document.getElementById('ispython') && document.getElementById('ispython').checked ? 'python' : 'js';
						testSourceServerPreflight(editorDir, hostId, lang);
					}
				} catch(err) {
					showToast('Test error: ' + err.message, 'danger');
				}
			});
		})();

	</script>

</c:if>
