<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<style>
.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
	margin-top: 8px;
	margin-bottom: 4px;
}
.acc-help-btn {
	position: absolute; top: 50%; right: 3rem;
	transform: translateY(-50%);
	color: var(--bs-secondary-color); font-size: 0.9rem; line-height: 1;
	cursor: pointer; z-index: 10;
	transition: color 0.15s;
}
.acc-help-btn:hover { color: var(--bs-primary); }
.acc-help-btn.acc-help-active { color: var(--bs-primary); }
</style>

<c:set var="desStatus" value="${destination.formattedStatus}" />
<c:set var="desStatusBase" value="${fn:contains(desStatus, '-') ? fn:substringBefore(desStatus, '-') : desStatus}"/>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-send text-primary"></i>
		<span class="fw-semibold">Delivery</span>
	</div>
	<div class="card-body py-0">
		<div class="field-grid">
			<div class="field-row"><div class="field-label">On Host Failure <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="In case of error on a data transmission then try the next host in the list and stick to it if it works or restart with the first host in the list?" tabindex="0"></i></div><div class="field-value">${destination.onHostFailureText}</div></div>
			<div class="field-row"><div class="field-label">If Target Exists</div><div class="field-value">${destination.ifTargetExistText}</div></div>
			<div class="field-row"><div class="field-label">Delete From Spool</div><div class="field-value">${destination.keepInSpoolText}</div></div>
			<c:if test="${not empty destination.filterName}">
			<div class="field-row"><div class="field-label">Data Compression <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If requested data files are compressed in the queue if there is enough time before transmission (otherwise files are compressed on the fly)" tabindex="0"></i></div><div class="field-value"><jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/><jsp:param name="showName" value="true"/></jsp:include></div></div>
			</c:if>
			<c:if test="${not empty destination.hostForSource.nickName}">
			<div class="field-row"><div class="field-label">Host For Sources <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If the data file is not found on the data mover then specify which host to use in order to retrieve the file from the source" tabindex="0"></i></div><div class="field-value"><span class="val-code">${destination.hostForSource.nickName}</span></div></div>
			</c:if>
		</div>
	</div>
</div>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-sliders text-primary"></i>
		<span class="fw-semibold">Limits</span>
	</div>
	<div class="card-body py-0">
		<div class="field-grid">
			<div class="field-row"><div class="field-label">Max Connections <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Maximum number of parallel connections authorized at a time on all the hosts of the Destination" tabindex="0"></i></div><div class="field-value"><span class="val-num">${destination.maxConnections}</span></div></div>
			<div class="field-row"><div class="field-label">Retry Count <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the Destination is hold after a consecutive number of unsuccessful transfers (a manual restart will be necessary)" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.retryCount <= 0}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-infinity me-1"></i>No limit</span></c:when><c:otherwise><span class="val-num">${destination.retryCount}</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry with the Primary Host if the transmission is failing on all the Backup Hosts" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${empty destination.formattedRetryFrequency}">Immediate</c:when><c:otherwise><span class="val-num">${destination.formattedRetryFrequency}</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Max Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is delayed after a consecutive number of unsuccessful attempts" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.maxStart == 0}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-infinity me-1"></i>No limit</span></c:when><c:otherwise><span class="val-num">${destination.maxStart}</span></c:otherwise></c:choose></div></div>
			<c:if test="${destination.maxStart != 0}">
			<div class="field-row"><div class="field-label">Start Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay mentioned in the previous parameter (Max Start)." tabindex="0"></i></div><div class="field-value"><span class="val-num">${destination.formattedStartFrequency}</span></div></div>
			</c:if>
			<div class="field-row"><div class="field-label">Max Requeue <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is tagged as failed after a consecutive number of unsuccessful transmissions (a manual requeue will be necessary)" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.maxRequeue == 0}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-infinity me-1"></i>No limit</span></c:when><c:otherwise><span class="val-num">${destination.maxRequeue}</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Max Pending <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum number of queued files which can exists at a single time in the Destination (new attempt of queueing files are rejected)" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.maxPending == 0}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-infinity me-1"></i>No limit</span></c:when><c:otherwise><span class="val-num">${destination.maxPending}</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Max File Size <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum size for a file in the queue (attempt of queueing bigger files are rejected)" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.maxFileSize <= 0}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-infinity me-1"></i>No limit</span></c:when><c:when test="${destination.maxFileSize % 1073741824 == 0}"><span class="val-num">${destination.maxFileSize / 1073741824} GB</span></c:when><c:when test="${destination.maxFileSize % 1048576 == 0}"><span class="val-num">${destination.maxFileSize / 1048576} MB</span></c:when><c:when test="${destination.maxFileSize % 1024 == 0}"><span class="val-num">${destination.maxFileSize / 1024} KB</span></c:when><c:otherwise><span class="val-num">${destination.maxFileSize} B</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Reset Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination is successfully using a backup host for more than this duration, it will restart." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${empty destination.formattedResetFrequency}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-slash-circle me-1"></i>Disabled</span></c:when><c:otherwise><span class="val-num">${destination.formattedResetFrequency}</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Max Inactivity <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination has no dissemination activity for more than this duration, a problem will be shown on the monitoring." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${empty destination.formattedMaxInactivity}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-slash-circle me-1"></i>Disabled</span></c:when><c:otherwise><span class="val-num">${destination.formattedMaxInactivity}</span></c:otherwise></c:choose></div></div>
		</div>
	</div>
</div>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-envelope text-primary"></i>
		<span class="fw-semibold">Directory &amp; Notifications</span>
	</div>
	<div class="card-body py-0">
		<div class="field-grid">
			<div class="field-row"><div class="field-label">Group By Date <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set then incoming ftp/sftp users will see the files grouped into date directories" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.groupByDate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<c:if test="${destination.groupByDate}">
			<div class="field-row"><div class="field-label">Date Format <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the format of the date to display for each directory (Java SimpleDateFormat pattern)" tabindex="0"></i></div><div class="field-value"><span class="val-code">${destination.dateFormat}</span></div></div>
			</c:if>
			<div class="field-row"><div class="field-label">Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Email address used when sending notifications" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${empty destination.userMail}"><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:when><c:otherwise><span class="val-code">${destination.userMail}</span></c:otherwise></c:choose></div></div>
			<c:if test="${not empty destination.userMail}">
			<div class="field-row"><div class="field-label">Mail on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a change is made to the Destination or its related Hosts." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.mailOnUpdate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Mail on Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer starts for this Destination." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.mailOnStart}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Mail on End <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has completed successfully for this Destination." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.mailOnEnd}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Mail on Error <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has failed for this Destination." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.mailOnError}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			</c:if>
		</div>
	</div>
</div>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-toggles text-primary"></i>
		<span class="fw-semibold">Flags</span>
	</div>
	<div class="card-body py-0">
		<div class="field-grid">
			<div class="field-row"><div class="field-label">Restart on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Automatically restart the Destination if a change is detected on one of the host configuration" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.stopIfDirty}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Acquisition <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the Acquisition Scheduler to use this Destination for Data Discovery and Retrieval (at least one Acquisition host must be defined)" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${destination.acquisition}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			<div class="field-row"><div class="field-label">Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is considered by the transfer scheduler; otherwise, no data transfers will be scheduled, even if there are pending requests in the queue. Similarly, any acquisition host will be disregarded." tabindex="0"></i></div><div class="field-value"><c:if test="${destination.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:if><c:if test="${!destination.active}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:if></div></div>
			<div class="field-row"><div class="field-label">Show In Monitors <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is monitored in the monitoring display." tabindex="0"></i></div><div class="field-value"><c:if test="${destination.showInMonitors}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:if><c:if test="${!destination.showInMonitors}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No <i class="bi bi-eye-slash ms-1" title="Not shown in Monitor Display" style="font-size:0.78rem;"></i></span></c:if></div></div>
		</div>
	</div>
</div>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-code-square text-primary"></i>
		<span class="fw-semibold">Options</span>
	</div>
	<div class="card-body p-2">
		<div class="accordion" id="paramsOptionsAccordion">
			<div class="accordion-item">
				<h2 class="accordion-header" id="paramsAccHeadProperties" style="position:relative;">
					<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccProperties" aria-expanded="false" aria-controls="paramsAccProperties">
						Properties
					</button>
					<span role="button" tabindex="0" class="acc-help-btn" id="paramsPropsHelpBtn"
						onclick="openParamsHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openParamsHelp();" title="Open properties reference">
						<i class="bi bi-question-circle"></i>
					</span>
				</h2>
				<div id="paramsAccProperties" class="accordion-collapse collapse" aria-labelledby="paramsAccHeadProperties" data-bs-parent="#paramsOptionsAccordion">
					<div class="accordion-body p-2">
						<pre id="properties" class="ace-panel"><c:out value="${destination.properties}" /></pre>
						<textarea id="properties" name="properties" style="display: none;"></textarea>
					</div>
				</div>
			</div>
			<div class="accordion-item">
				<h2 class="accordion-header" id="paramsAccHeadJavascript">
					<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccJavascript" aria-expanded="false" aria-controls="paramsAccJavascript">
						JavaScript
					</button>
				</h2>
				<div id="paramsAccJavascript" class="accordion-collapse collapse" aria-labelledby="paramsAccHeadJavascript" data-bs-parent="#paramsOptionsAccordion">
					<div class="accordion-body p-2">
						<pre id="javascript" class="ace-panel"><c:out value="${destination.javascript}" /></pre>
						<textarea id="javascript" name="javascript" style="display: none;"></textarea>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="paramsHelpOffcanvas"
     aria-labelledby="paramsHelpOffcanvasLabel" style="width:min(480px,42vw);">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="paramsHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="paramsHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="paramsHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

<script>
	var editorProperties = getEditorProperties(true, false, "properties", "crystal");
	editorProperties.setOptions({minLines: 10, maxLines: 20});

	var completions = [
		${destination.completions}
	];

	$(document).ready(function() {
		$('#paramsHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
		var navEl = document.querySelector('#paramsHelpContent .help-nav');
		if (navEl) document.getElementById('paramsHelpNav').appendChild(navEl);
	});

	checkEachLine(editorProperties);

	function _scrollParamsHelpToCursor() {
		var row = editorProperties.selection.getCursor().row;
		var line = editorProperties.session.getLine(row) || '';
		line = line.trim();
		if (line && !line.startsWith('#') && !line.startsWith('//')) {
			var eqIdx = line.indexOf('=');
			var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
			if (paramName) scrollHelpToParam('paramsHelpContent', paramName);
		}
	}

	editorProperties.addEventListener("changeSelection", function (event) {
		editorProperties.session.setAnnotations(
			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
		var _oc = document.getElementById('paramsHelpOffcanvas');
		if (_oc && _oc.classList.contains('show')) _scrollParamsHelpToCursor();
	});

	var editorJavascript = getEditorProperties(true, false, "javascript", "javascript");

	document.getElementById('paramsAccProperties').addEventListener('shown.bs.collapse', function() {
		editorProperties.resize(true);
	});
	document.getElementById('paramsAccJavascript').addEventListener('shown.bs.collapse', function() {
		editorJavascript.resize(true);
	});

	window.openParamsHelp = function() {
		var el = document.getElementById('paramsHelpOffcanvas');
		if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
	};
	var _paramsOffcanvasEl = document.getElementById('paramsHelpOffcanvas');
	if (_paramsOffcanvasEl) {
		_paramsOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
			var btn = document.getElementById('paramsPropsHelpBtn');
			if (btn) btn.classList.add('acc-help-active');
		});
		_paramsOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
			_scrollParamsHelpToCursor();
		});
		_paramsOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
			var btn = document.getElementById('paramsPropsHelpBtn');
			if (btn) btn.classList.remove('acc-help-active');
		});
	}

	makeResizable(editorProperties);
	makeResizable(editorJavascript);

	window.addEventListener('resize', function() {
		editorProperties.resize(true);
		editorJavascript.resize(true);
	});
</script>
