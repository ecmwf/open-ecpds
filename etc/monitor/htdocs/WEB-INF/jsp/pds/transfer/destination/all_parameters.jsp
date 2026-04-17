<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
	padding: 8px;
	position: relative;
}
table.fields {
	width: 100%;
	min-width: 600px;
}
table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}
</style>

<c:set var="desStatus" value="${destination.formattedStatus}" />
<c:set var="desStatusBase" value="${fn:contains(desStatus, '-') ? fn:substringBefore(desStatus, '-') : desStatus}"/>
<div class="dest-page-header mb-3">
	<div class="d-flex align-items-center gap-2 flex-wrap mb-1">
		<c:if test="${not destination.active}"><i class="bi bi-slash-circle-fill text-danger" title="Destination is disabled" style="font-size:0.9rem;align-self:center;"></i></c:if>
		<a class="dest-page-name text-decoration-none" href="/do/transfer/destination/${destination.name}"<c:if test="${not destination.active}"> style="text-decoration:line-through !important;color:var(--bs-secondary-color)"</c:if>>${destination.name}</a>
		<c:if test="${destination.id != destination.name}">
			<code class="dest-page-id">${destination.id}</code>
		</c:if>
		<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_flag.jsp"/>
		<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_type_badge.jsp"/>
		<c:choose>
			<c:when test="${desStatusBase == 'Idle'}">
				<span class="badge bg-secondary fs-status" title="${desStatus}">${desStatus}</span>
			</c:when>
			<c:when test="${desStatusBase == 'Running'}">
				<span class="badge bg-success fs-status" title="${desStatus}">${desStatus}</span>
			</c:when>
			<c:when test="${desStatusBase == 'Restarting' or desStatusBase == 'Resending'}">
				<span class="badge bg-info text-dark fs-status" title="${desStatus}">${desStatus}</span>
			</c:when>
			<c:when test="${desStatusBase == 'Waiting' or desStatusBase == 'Retrying' or desStatusBase == 'Interrupted'}">
				<span class="badge bg-warning text-dark fs-status" title="${desStatus}">${desStatus}</span>
			</c:when>
			<c:when test="${desStatusBase == 'Initialized' or desStatusBase == 'Stopped' or desStatusBase == 'NoHosts' or desStatusBase == 'Failed'}">
				<span class="badge bg-danger fs-status" title="${desStatus}">${desStatus}</span>
			</c:when>
			<c:otherwise>
				<span class="badge bg-secondary fs-status" title="${desStatus}">${desStatus}</span>
			</c:otherwise>
		</c:choose>
		<c:if test="${not destination.showInMonitors}">
			<i class="bi bi-eye-slash text-muted" title="Not shown in Monitor Display" style="font-size:0.85rem;"></i>
		</c:if>
		<c:if test="${not empty destination.filterName and destination.filterName ne 'none'}">
			<jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/></jsp:include>
		</c:if>
	</div>
	<c:if test="${not empty destination.comment}">
		<p class="dest-page-comment">${destination.comment}</p>
	</c:if>
	<c:if test="${not empty destination.ecUserName}">
		<p class="mb-0 small text-muted">
			<i class="bi bi-person-fill me-1"></i><c:choose>
				<c:when test="${not empty destination.userMail}"><a href="mailto:${destination.userMail}" class="text-muted">${destination.ecUserName}</a></c:when>
				<c:otherwise>${destination.ecUserName}</c:otherwise>
			</c:choose>
		</p>
	</c:if>
</div>

<table class="fields">

	<tr>
		<th>On Host Failure <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="In case of error on a data transmission then try the next host in the list and stick to it if it works or restart with the first host in the list?" tabindex="0"></i></th>
		<td>${destination.onHostFailureText}</td>
	</tr>
	<tr>
		<th>If Target Exists</th>
		<td>${destination.ifTargetExistText}</td>
	</tr>
	<tr>
		<th>Delete From Spool</th>
		<td>${destination.keepInSpoolText}</td>
	</tr>


	<tr><td colspan="2">&nbsp;</td></tr>

	<tr>
		<th>Max Connections <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Maximum number of parallel connections authorized at a time on all the hosts of the Destination" tabindex="0"></i></th>
		<td>${destination.maxConnections}</td>
	</tr>
	<tr>
		<th>Retry Count <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the Destination is hold after a consecutive number of unsuccessful transfers (a manual restart will be necessary)" tabindex="0"></i></th>
		<td><c:choose><c:when test="${destination.retryCount <= 0}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.retryCount}</c:otherwise></c:choose></td>
	</tr>
	<tr>
		<th>Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry with the Primary Host if the transmission is failing on all the Backup Hosts" tabindex="0"></i></th>
		<td><c:choose><c:when test="${empty destination.formattedRetryFrequency}">Immediate</c:when><c:otherwise>${destination.formattedRetryFrequency}</c:otherwise></c:choose></td>
	</tr>
	<tr>
		<th>Max Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is delayed after a consecutive number of unsuccessful attempts" tabindex="0"></i></th>
		<td><c:choose><c:when test="${destination.maxStart == 0}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.maxStart}</c:otherwise></c:choose></td>
	</tr>
	<c:if test="${destination.maxStart != 0}">
	<tr>
		<th>Start Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay mentioned in the previous parameter (Max Start)." tabindex="0"></i></th>
		<td>${destination.formattedStartFrequency}</td>
	</tr>
	</c:if>
	<tr>
		<th>Max Requeue <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is tagged as failed after a consecutive number of unsuccessful transmissions (a manual requeue will be necessary)" tabindex="0"></i></th>
		<td><c:choose><c:when test="${destination.maxRequeue == 0}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.maxRequeue}</c:otherwise></c:choose></td>
	</tr>
	<tr>
		<th>Max Pending <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum number of queued files which can exists at a single time in the Destination (new attempt of queueing files are rejected)" tabindex="0"></i></th>
		<td><c:choose><c:when test="${destination.maxPending == 0}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.maxPending}</c:otherwise></c:choose></td>
	</tr>
	<tr>
		<th>Max File Size <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum size for a file in the queue (attempt of queueing bigger files are rejected)" tabindex="0"></i></th>
		<td>
		<c:choose>
			<c:when test="${destination.maxFileSize <= 0}"><span class="text-muted">Disabled</span></c:when>
			<c:when test="${destination.maxFileSize % 1073741824 == 0}">${destination.maxFileSize / 1073741824} GB</c:when>
			<c:when test="${destination.maxFileSize % 1048576 == 0}">${destination.maxFileSize / 1048576} MB</c:when>
			<c:when test="${destination.maxFileSize % 1024 == 0}">${destination.maxFileSize / 1024} KB</c:when>
			<c:otherwise>${destination.maxFileSize} B</c:otherwise>
		</c:choose>
	</td>
	</tr>
	<tr>
		<th>Reset Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination is successfully using a backup host for more than this duration, it will restart." tabindex="0"></i></th>
		<td><c:choose><c:when test="${empty destination.formattedResetFrequency}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.formattedResetFrequency}</c:otherwise></c:choose></td>
	</tr>
	<tr>
		<th>Max Inactivity <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination has no dissemination activity for more than this duration, a problem will be shown on the monitoring." tabindex="0"></i></th>
		<td><c:choose><c:when test="${empty destination.formattedMaxInactivity}"><span class="text-muted">Disabled</span></c:when><c:otherwise>${destination.formattedMaxInactivity}</c:otherwise></c:choose></td>
	</tr>

	<tr><td colspan="2">&nbsp;</td></tr>

	<tr>
		<th>Group By Date <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set then incoming ftp/sftp users will see the files grouped into date directories" tabindex="0"></i></th>
		<td><c:if test="${destination.groupByDate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.groupByDate}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<c:if test="${destination.groupByDate}">
	<tr>
		<th>Date Format <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the format of the date to display for each directory (Java SimpleDateFormat pattern)" tabindex="0"></i></th>
		<td>${destination.dateFormat}</td>
	</tr>
	</c:if>

	<c:if test="${not empty destination.filterName or not empty destination.hostForSource.nickName}">
	<tr><td colspan="2">&nbsp;</td></tr>
	<c:if test="${not empty destination.filterName}">
	<tr>
		<th>Data Compression <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If requested data files are compressed in the queue if there is enough time before transmission (otherwise files are compressed on the fly)" tabindex="0"></i></th>
		<td><jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/><jsp:param name="showName" value="true"/></jsp:include></td>
	</tr>
	</c:if>
	<c:if test="${not empty destination.hostForSource.nickName}">
	<tr>
		<th>Host For Sources <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If the data file is not found on the data mover then specify which host to use in order to retrieve the file from the source" tabindex="0"></i></th>
		<td>${destination.hostForSource.nickName}</td>
	</tr>
	</c:if>
	</c:if>

	<tr><td colspan="2">&nbsp;</td></tr>

	<tr>
		<th>Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Email address used when sending notifications" tabindex="0"></i></th>
		<td><c:choose><c:when test="${empty destination.userMail}"><span class="text-muted fst-italic">none</span></c:when><c:otherwise>${destination.userMail}</c:otherwise></c:choose></td>
	</tr>
	<c:if test="${not empty destination.userMail}">
	<tr>
		<th>Mail on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a change is made to the Destination or its related Hosts." tabindex="0"></i></th>
		<td><c:if test="${destination.mailOnUpdate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.mailOnUpdate}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Mail on Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer starts for this Destination." tabindex="0"></i></th>
		<td><c:if test="${destination.mailOnStart}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.mailOnStart}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Mail on End <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has completed successfully for this Destination." tabindex="0"></i></th>
		<td><c:if test="${destination.mailOnEnd}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.mailOnEnd}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Mail on Error <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has failed for this Destination." tabindex="0"></i></th>
		<td><c:if test="${destination.mailOnError}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.mailOnError}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	</c:if>

	<tr><td colspan="2">&nbsp;</td></tr>

	<tr>
		<th>Restart on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Automatically restart the Destination if a change is detected on one of the host configuration" tabindex="0"></i></th>
		<td><c:if test="${destination.stopIfDirty}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.stopIfDirty}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Acquisition <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the Acquisition Scheduler to use this Destination for Data Discovery and Retrieval (at least one Acquisition host must be defined)" tabindex="0"></i></th>
		<td><c:if test="${destination.acquisition}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.acquisition}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is considered by the transfer scheduler; otherwise, no data transfers will be scheduled, even if there are pending requests in the queue. Similarly, any acquisition host will be disregarded." tabindex="0"></i></th>
		<td><c:if test="${destination.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
		<th>Show In Monitors <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is monitored in the monitoring display." tabindex="0"></i></th>
		<td><c:if test="${destination.showInMonitors}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!destination.showInMonitors}"><i class="bi bi-x-circle-fill text-danger" title="No"></i> <i class="bi bi-eye-slash text-muted ms-1" title="Not shown in Monitor Display" style="font-size:0.78rem;"></i></c:if></td>
	</tr>

	<tr><td colspan="2">&nbsp;</td></tr>
	<tr id="params-options-row">
		<th style="vertical-align:top;padding-top:0.5rem">Options</th>
		<td>
			<div class="accordion" id="paramsOptionsAccordion" style="min-width:860px;max-width:860px">
			<div class="accordion-item">
				<h2 class="accordion-header" id="paramsAccHeadProperties">
					<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccProperties" aria-expanded="false" aria-controls="paramsAccProperties">
						Properties
					</button>
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
			<div class="accordion-item">
				<h2 class="accordion-header" id="paramsAccHeadHelp">
					<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccHelp" aria-expanded="false" aria-controls="paramsAccHelp">
						Help
					</button>
				</h2>
				<div id="paramsAccHelp" class="accordion-collapse collapse" aria-labelledby="paramsAccHeadHelp" data-bs-parent="#paramsOptionsAccordion">
					<div class="accordion-body p-2">
						<div id="paramsHelpContent" class="scrollable-tab"></div>
					</div>
				</div>
			</div>
		</div>
		</td>
	</tr>
</table>

<script>
	var editorProperties = getEditorProperties(true, false, "properties", "crystal");
	editorProperties.setOptions({minLines: 10, maxLines: 20});

	var completions = [
		${destination.completions}
	];

	$(document).ready(function() {
		$('#paramsHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
	});

	checkEachLine(editorProperties);

	editorProperties.addEventListener("changeSelection", function (event) {
		editorProperties.session.setAnnotations(
			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
	});

	var editorJavascript = getEditorProperties(true, false, "javascript", "javascript");

	document.getElementById('paramsAccProperties').addEventListener('shown.bs.collapse', function() {
		editorProperties.resize(true);
	});
	document.getElementById('paramsAccJavascript').addEventListener('shown.bs.collapse', function() {
		editorJavascript.resize(true);
	});

	var paramsHelpBtn = document.querySelector('button[data-bs-target="#paramsAccHelp"]');
	if (paramsHelpBtn) {
		paramsHelpBtn.addEventListener('click', function() {
			setTimeout(function() {
				if (!document.getElementById('paramsAccHelp').classList.contains('show')) return;
				var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
				line = line.trim();
				if (line && !line.startsWith('#') && !line.startsWith('//')) {
					var eqIdx = line.indexOf('=');
					var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
					if (paramName) scrollHelpToParam('paramsHelpContent', paramName);
				}
			}, 400);
		});
	}

	makeResizable(editorProperties);
	makeResizable(editorJavascript);
</script>
