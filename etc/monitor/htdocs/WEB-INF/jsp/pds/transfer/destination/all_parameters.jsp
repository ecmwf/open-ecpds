<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>


<c:set var="desStatus" value="${destination.formattedStatus}" />
<c:set var="desStatusBase" value="${fn:contains(desStatus, '-') ? fn:substringBefore(desStatus, '-') : desStatus}"/>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-geo-alt text-primary"></i>
		<span class="fw-semibold">Identity</span>
	</div>
	<div class="card-body py-2">
		<div class="field-row"><div class="field-label">Type</div><div class="field-value"><jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_type_badge.jsp"/></div></div>
		<c:if test="${not empty destination.comment}">
		<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><span class="val-code">${destination.comment}</span></div></div>
		</c:if>
		<div class="field-row"><div class="field-label">Transfer Group <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If no Dissemination Host is active then this field specifies in which Transfer Group the queued files should be stored" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${not empty destination.transferGroupName}"><span class="val-code">${destination.transferGroupName}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
		<div class="field-row"><div class="field-label">Country</div><div class="field-value"><c:choose><c:when test="${not empty destination.country}"><span><c:if test="${not empty destination.countryIso}"><img src="/flag-icons/flags/4x3/${fn:toLowerCase(destination.countryIso)}.svg" alt="${destination.countryIso}" title="${destination.countryIso}" class="me-1" style="height:12px;width:auto" onerror="this.style.display='none'"/></c:if>${destination.country.name}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
		<div class="field-row"><div class="field-label">Owner <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Only for the record" tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${not empty destination.ecUserName}"><span class="val-code">${destination.ecUserName}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
	</div>
</div>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-send text-primary"></i>
		<span class="fw-semibold d-inline-flex align-items-center gap-1">Delivery
			<button class="btn btn-sm p-0 border-0 text-secondary" style="line-height:1;font-size:0.85rem"
				data-bs-toggle="collapse" data-bs-target="#destDeliveryInfoPanel" aria-expanded="false"
				title="What is a destination?"><i class="bi bi-info-circle"></i></button>
		</span>
	</div>
	<div class="collapse" id="destDeliveryInfoPanel">
		<div class="px-3 pt-2 pb-3 border-bottom small" style="background:var(--bs-secondary-bg)">
			A destination should be understood as a place where data transfers are queued and processed in order to deliver
			data to a unique remote place, hence the name &lsquo;destination&rsquo;. It specifies the information the Data
			Dissemination service needs to disseminate the content of a data file to a particular remote site.
			<br><br>
			Each destination implements a transfer scheduler with its own configuration parameters, which can be fine-tuned
			to meet the remote site&rsquo;s needs. These settings make it possible to control various things, such as how to
			organise the data transmission by using data transfer priorities and parallel transmissions, or how to deal with
			transmission errors with a fully customisable retry mechanism.
			<br><br>
			In addition, a destination can be associated with a list of dissemination hosts, with a primary host indicating
			the main target system where to deliver the data, and a list of fall-back hosts to switch to if for some reason
			the primary host is unavailable.
		</div>
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

<c:if test="${not empty destination.proxyHostsAndPriorities}">
<div class="card border-0 shadow-sm mb-3">
	<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
		<i class="bi bi-hdd-network text-primary"></i>
		<span class="fw-semibold">Proxy Host(s)</span>
	</div>
	<div class="card-body p-0">
		<table class="table table-sm table-hover mb-0 small">
			<thead class="table-light">
				<tr>
					<th>Name</th>
					<th class="text-center">Priority</th>
					<th class="text-center">Status</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="pair" items="${destination.proxyHostsAndPriorities}">
				<tr>
					<td><a href="/do/transfer/host/${pair.name.name}">${pair.name.nickName}</a></td>
					<td class="text-center"><span class="val-num">${pair.value}</span></td>
					<td class="text-center">
						<c:choose>
							<c:when test="${pair.name.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Active</span></c:when>
							<c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Inactive</span></c:otherwise>
						</c:choose>
					</td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
</c:if>

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
			<div class="field-row"><div class="field-label">Show In Monitor <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is monitored in the monitoring display." tabindex="0"></i></div><div class="field-value"><c:if test="${destination.showInMonitors}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:if><c:if test="${!destination.showInMonitors}"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No <i class="bi bi-eye-slash ms-1" title="Not shown in Monitor Display" style="font-size:0.78rem;"></i></span></c:if></div></div>
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
					<button class="accordion-button collapsed" id="paramsAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccProperties" aria-expanded="false" aria-controls="paramsAccProperties">
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
					<button class="accordion-button collapsed" id="paramsAccJavascriptBtn" type="button" data-bs-toggle="collapse" data-bs-target="#paramsAccJavascript" aria-expanded="false" aria-controls="paramsAccJavascript">
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
     aria-labelledby="paramsHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
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

	checkEachLine(editorProperties, 'paramsAccPropertiesBtn');

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
		checkEachLine(editorProperties, 'paramsAccPropertiesBtn');
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

	editorJavascript.getSession().on('changeAnnotation', function() {
		applyAnnotationMarkers(editorJavascript, 'paramsAccJavascriptBtn');
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
