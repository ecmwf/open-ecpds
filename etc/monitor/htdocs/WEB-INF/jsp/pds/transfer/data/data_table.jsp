<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="dest-page-header mb-3">
<div class="d-flex align-items-center gap-2 flex-wrap mb-1">
<i class="bi bi-arrow-left-right text-success flex-shrink-0"></i>
<span class="fw-semibold">Data Transfer:&nbsp;<strong><c:out value="${datatransfer.id}"/></strong></span>

<%-- Desktop: full icon bar, hidden on mobile --%>
<div id="_dtIconBar" class="d-none d-sm-flex gap-2 align-items-center ms-auto">
<auth:if basePathKey="datatransfer.basepath" paths="">
<auth:then>
<a href='<bean:message key="datatransfer.basepath"/>' class="btn btn-sm btn-outline-secondary" title="All Data Transfers"><i class="bi bi-arrow-left"></i></a>
</auth:then>
</auth:if>
<auth:if basePathKey="destination.basepath" paths="/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}">
<auth:then>
<div class="d-flex gap-1 align-items-center" style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;">
<c:choose>
<c:when test="${!datatransfer.deleted}">
<a href='<bean:message key="destination.basepath"/>/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}'
   class="btn btn-sm btn-outline-danger" title="Delete this Data Transfer" data-label="Delete"><i class="bi bi-trash"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-danger" disabled title="Already deleted" data-label="Delete"><i class="bi bi-trash"></i></button>
</c:otherwise>
</c:choose>
<c:choose>
<c:when test="${datatransfer.canBeDownloaded}">
<a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/download/${datatransfer.id}'
   class="btn btn-sm btn-outline-primary" title="Download"><i class="bi bi-cloud-download"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-primary" disabled title="Not available for download" data-label="Download"><i class="bi bi-cloud-download"></i></button>
</c:otherwise>
</c:choose>
<div class="d-flex gap-1 align-items-center" style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;">
<c:choose>
<c:when test="${not empty showScheduleNow}">
<a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/scheduleNow/${datatransfer.id}'
   class="btn btn-sm btn-outline-secondary dt-op-btn" title="Schedule Now"><i class="bi bi-calendar-check"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-secondary" disabled title="Schedule Now not available" data-label="Schedule Now"><i class="bi bi-calendar-check"></i></button>
</c:otherwise>
</c:choose>
<c:choose>
<c:when test="${datatransfer.canBeRequeued}">
<a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/requeue/${datatransfer.id}'
   class="btn btn-sm btn-outline-success dt-op-btn" title="Requeue"><i class="bi bi-arrow-repeat"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-success" disabled title="Requeue not available" data-label="Requeue"><i class="bi bi-arrow-repeat"></i></button>
</c:otherwise>
</c:choose>
<c:choose>
<c:when test="${datatransfer.statusCode == 'FETC'}">
<a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/interrupt/${datatransfer.id}'
   class="btn btn-sm btn-outline-warning dt-op-btn" title="Interrupt Retrieval"><i class="bi bi-stop-circle"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-warning" disabled title="Interrupt not available" data-label="Interrupt Retrieval"><i class="bi bi-stop-circle"></i></button>
</c:otherwise>
</c:choose>
</div>
</div>
</auth:then>
</auth:if>
</div><%-- end #_dtIconBar --%>

<%-- Mobile: ⋯ dropdown, hidden on sm+ --%>
<div class="d-sm-none ms-auto">
    <div class="dropdown">
        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                id="_dtActionsToggle" data-bs-toggle="dropdown" aria-expanded="false"
                title="Actions">
            <i class="bi bi-three-dots"></i>
        </button>
        <ul class="dropdown-menu dropdown-menu-end" id="_dtActionsMenu" aria-labelledby="_dtActionsToggle"></ul>
    </div>
</div>
<script>
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        var bar  = document.getElementById('_dtIconBar');
        var menu = document.getElementById('_dtActionsMenu');
        if (!bar || !menu) return;
        function addItem(el) {
            var li   = document.createElement('li');
            var item = document.createElement('a');
            var dis  = el.disabled || el.classList.contains('disabled');
            item.className = 'dropdown-item' + (dis ? ' disabled text-muted' : '');
            if (!dis && el.classList.contains('dt-op-btn')) item.classList.add('dt-op-btn');
            item.href = el.getAttribute('href') || '#';
            if (dis) item.setAttribute('aria-disabled', 'true');
            var ic = el.querySelector('i[class]');
            if (ic) {
                var icon = document.createElement('i');
                icon.className = ic.className + ' me-2';
                item.appendChild(icon);
            }
            item.appendChild(document.createTextNode(el.getAttribute('data-label') || el.title || el.textContent.trim()));
            li.appendChild(item);
            menu.appendChild(li);
        }
        function addDivider() {
            if (menu.children.length === 0) return;
            var li = document.createElement('li');
            li.innerHTML = '<hr class="dropdown-divider m-1">';
            menu.appendChild(li);
        }
        Array.from(bar.children).forEach(function(child) {
            if (child.tagName === 'A' || child.tagName === 'BUTTON') {
                addItem(child);
            } else if (child.tagName === 'DIV' && child.querySelector('a, button')) {
                addDivider();
                Array.from(child.querySelectorAll('a, button')).forEach(addItem);
            }
        });
    });
})();
</script>
<%-- AJAX handler for Requeue / Schedule Now / Interrupt: stay on this page --%>
<script>
(function() {
    document.addEventListener('click', function(e) {
        var el = e.target.closest('a.dt-op-btn');
        if (!el) return;
        e.preventDefault();
        var url = el.getAttribute('href');
        if (!url || url === '#') return;
        var origHTML = el.innerHTML;
        var origTitle = el.title;
        el.classList.add('disabled');
        el.style.pointerEvents = 'none';
        el.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span>';
        fetch(url, { credentials: 'same-origin' })
            .then(function(r) {
                el.innerHTML = '<i class="bi bi-check-circle-fill text-success"></i>';
                el.title = 'Done \u2014 reloading\u2026';
                setTimeout(function() { window.location.reload(); }, 800);
            })
            .catch(function(err) {
                el.innerHTML = origHTML;
                el.title = origTitle;
                el.classList.remove('disabled');
                el.style.pointerEvents = '';
                alert('Operation failed: ' + (err.message || err));
            });
    });
})();
</script>
</div>
</div>

<%-- Stat strip --%>
<div class="row g-2 mb-3" style="align-items:stretch;">
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100 d-flex flex-column justify-content-center">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Progress<span id="dt-live-dot" style="display:none"> <span class="badge bg-primary" style="font-size:0.55rem;vertical-align:middle;animation:dtLivePulse 1.5s ease-in-out infinite"><i class="bi bi-broadcast me-1"></i>Live</span></span></div>
<div class="d-flex align-items-center gap-2 px-3">
<div id="dt-progress-bar-wrap" class="progress flex-grow-1" style="height:0.75rem" role="progressbar"
     aria-valuenow="${datatransfer.progress}" aria-valuemin="0" aria-valuemax="100">
  <c:choose>
    <c:when test="${datatransfer.progress >= 100}">
      <div class="progress-bar bg-success" style="width:100%"></div>
    </c:when>
    <c:when test="${datatransfer.progress > 0}">
      <div class="progress-bar" style="width:${datatransfer.progress}%"></div>
    </c:when>
    <c:otherwise>
      <div class="progress-bar bg-secondary" style="width:0%"></div>
    </c:otherwise>
  </c:choose>
</div><%-- /progress-bar-wrap --%>
<span id="dt-progress-pct" class="fw-bold flex-shrink-0" style="font-size:0.8rem;min-width:2.8rem;">${datatransfer.progress}%</span>
</div><%-- /flex row --%>
</div><%-- /card --%>
</div><%-- /col --%>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100 d-flex flex-column justify-content-center">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Duration</div>
<div id="dt-duration-val" class="fw-bold fs-6"><c:choose><c:when test="${not empty datatransfer.formattedDuration}">${datatransfer.formattedDuration}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100 d-flex flex-column justify-content-center">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Rate</div>
<div id="dt-rate-val" class="fw-bold fs-6">
<c:if test="${datatransfer.transferRate > 0}">
<a style="text-decoration:none" title="Rate: ${datatransfer.formattedTransferRate}">${datatransfer.formattedTransferRateInMBitsPerSeconds} Mbits/s</a>
</c:if>
<c:if test="${datatransfer.transferRate <= 0}"><span class="text-muted">&mdash;</span></c:if>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100 d-flex flex-column justify-content-center">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Priority</div>
<div class="fw-bold fs-6">${datatransfer.priority}</div>
</div>
</div>
</div>

<%-- Card: Identity --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-info-circle text-primary"></i>
<span class="fw-semibold d-inline-flex align-items-center gap-1">Transfer Details
    <button class="btn btn-link p-0 ms-1" style="font-size:0.85rem;line-height:1;vertical-align:middle;color:var(--bs-secondary-color);"
            data-bs-toggle="collapse" data-bs-target="#dtDetailsInfoPanel"
            aria-expanded="false" aria-controls="dtDetailsInfoPanel"
            title="What is a data transfer?">
        <i class="bi bi-info-circle"></i>
    </button>
</span>
</div>
<div class="collapse" id="dtDetailsInfoPanel">
    <div class="px-3 pt-2 pb-3 border-bottom small" style="background:var(--bs-secondary-bg)">
        A data transfer is linked to a unique data file and represents a request to move its content, either
        by disseminating it to a remote destination or by retrieving it from a remote source. It carries
        related information such as schedule, priority, progress, status, transfer rate, errors, and history.
        A single data file can be linked to several data transfers, for example when multiple destinations
        share the same product or when aliases are in use.
    </div>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Transfer ID</div><div class="field-value"><span class="val-code">${datatransfer.id}</span></div></div>
<div class="field-row"><div class="field-label">Data File ID</div><div class="field-value"><auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><a href="<bean:message key="datafile.basepath"/>/${datatransfer.dataFileId}"><span class="val-code">${datatransfer.dataFileId}</span></a></auth:then><auth:else><span class="val-code">${datatransfer.dataFileId}</span></auth:else></auth:if></div></div>
<div class="field-row"><div class="field-label"><c:out value="${datatransfer.destination.typeText}"/> Destination</div><div class="field-value">
<a href="<bean:message key="destination.basepath"/>/<c:out value="${datatransfer.destination.name}"/>"><c:out value="${datatransfer.destination.name}"/></a>
<c:set var="_dts" value="${datatransfer.destination.formattedStatus}"/>
<c:set var="_dtsb" value="${fn:contains(_dts, '-') ? fn:substringBefore(_dts, '-') : _dts}"/>
<c:choose>
  <c:when test="${_dtsb == 'Running'}"><span class="badge bg-success ms-1 fs-status">${_dts}</span></c:when>
  <c:when test="${_dtsb == 'Restarting' or _dtsb == 'Resending'}"><span class="badge bg-info text-dark ms-1 fs-status">${_dts}</span></c:when>
  <c:when test="${_dtsb == 'Waiting' or _dtsb == 'Retrying' or _dtsb == 'Interrupted'}"><span class="badge bg-warning text-dark ms-1 fs-status">${_dts}</span></c:when>
  <c:when test="${_dtsb == 'Initialized' or _dtsb == 'Stopped' or _dtsb == 'NoHosts' or _dtsb == 'Failed'}"><span class="badge bg-danger ms-1 fs-status">${_dts}</span></c:when>
  <c:otherwise><span class="badge bg-secondary ms-1 fs-status">${_dts}</span></c:otherwise>
</c:choose>
</div></div>
<div class="field-row"><div class="field-label">Status</div><div id="dt-status-val" class="field-value">
<auth:if basePathKey="nonmemberstate.basepath" paths="">
<auth:then><c:set var="_dtStatus" value="${datatransfer.detailedStatus}"/></auth:then>
<auth:else><c:set var="_dtStatus" value="${datatransfer.memberStateDetailedStatus}"/></auth:else>
</auth:if>
<c:choose>
<c:when test="${datatransfer.deleted}">
  <span class="badge bg-danger" title="Deleted">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'DONE'}">
  <span class="badge bg-success" title="${_dtStatus}">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'EXEC' or datatransfer.statusCode == 'FETC' or datatransfer.statusCode == 'INIT'}">
  <span class="badge bg-primary" title="${_dtStatus}">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'RETR' or datatransfer.statusCode == 'WAIT' or datatransfer.statusCode == 'SCHE' or datatransfer.statusCode == 'HOLD'}">
  <span class="badge bg-warning text-dark" title="${_dtStatus}">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'FAIL'}">
  <span class="badge bg-danger" title="${_dtStatus}">${_dtStatus}</span>
</c:when>
<c:otherwise>
  <span class="badge bg-secondary" title="${_dtStatus}">${_dtStatus}</span>
</c:otherwise>
</c:choose>
</div></div>
<div class="field-row"><div class="field-label">Dissemination Host</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.hostName}"><a href="/do/transfer/host/${datatransfer.hostName}"><c:choose><c:when test="${not empty datatransfer.hostNickName}">${datatransfer.hostNickName}</c:when><c:otherwise>${datatransfer.hostName}</c:otherwise></c:choose></a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Data Mover</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.transferServerName}"><auth:if basePathKey="transferserver.basepath" paths="/${datatransfer.transferServerName}"><auth:then><a href="/do/datafile/transferserver/${datatransfer.transferServerName}">${datatransfer.transferServerName}</a></auth:then><auth:else><span class="val-code">${datatransfer.transferServerName}</span></auth:else></auth:if></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">On Proxy</div><div class="field-value"><c:choose><c:when test="${datatransfer.proxy}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Target</div><div class="field-value"><c:choose><c:when test="${datatransfer.deleted}"><span class="val-code text-danger text-break d-inline-block">${datatransfer.target}</span></c:when><c:otherwise><span class="val-code text-break d-inline-block">${datatransfer.target}</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Sent</div><div class="field-value"><span id="dt-sent-val" class="val-num" title="Sent: ${datatransfer.formattedSent}">${datatransfer.sent} bytes</span></div></div>
<c:if test="${not empty showFileSize}">
<div class="field-row"><div class="field-label">Size</div><div class="field-value"><span class="val-num" title="Size: ${datatransfer.formattedSize}">${datatransfer.size} bytes</span></div></div>
</c:if>
</div>
</div>
</div>

<%-- Card: File Info (auth-gated) --%>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-file-earmark-code text-primary"></i>
<span class="fw-semibold">File Info</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Product</div><div class="field-value"><c:choose><c:when test="${not empty datafile.metaTime or not empty datafile.metaStream or not empty datafile.metaType or not empty datafile.metaTarget}"><span class="val-code">${datafile.metaTime}-${datafile.metaStream} ${datafile.metaType} (${datafile.metaTarget})</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Prod Time</div><div class="field-value"><c:choose><c:when test="${not empty datafile.productTime}"><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Expired</div><div class="field-value"><c:choose><c:when test="${datatransfer.expired}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Deleted</div><div class="field-value"><c:choose><c:when test="${datatransfer.deleted}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Replicated</div><div class="field-value"><c:choose><c:when test="${datatransfer.replicated}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Backup</div><div class="field-value"><c:choose><c:when test="${datatransfer.backup}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.formattedComment}">${datatransfer.formattedComment}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Original</div><div class="field-value"><span class="val-code text-break d-inline-block">${datafile.formattedOriginal}</span></div></div>
</div>
</div>
</div>
</auth:then>
</auth:if>

<%-- Card: Timing --%>
<%-- Detect schedule / finish variants --%>
<c:set var="schedTimeTitle" value="Sch. Time"/>
<c:if test="${datatransfer.scheduledTime != datatransfer.queueTime}">
<c:set var="requeued" value="value"/>
<c:set var="schedTimeTitle" value="Initial Sch. Time"/>
</c:if>
<c:set var="finishTimeTitle" value="Finish Time"/>
<c:if test="${not empty datatransfer.realFinishTime && datatransfer.realFinishTime != datatransfer.finishTime}">
<c:set var="refinished" value="value"/>
<c:set var="finishTimeTitle" value="First Finish Time"/>
</c:if>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock text-primary"></i>
<span class="fw-semibold">Times (UTC)</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label"><c:out value="${schedTimeTitle}"/></div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.scheduledTime}"><content:content name="datatransfer.scheduledTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">ASAP</div><div class="field-value"><c:choose><c:when test="${datatransfer.asap}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><div class="field-row"><div class="field-label">Earliest</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.earliestTime}"><content:content name="datatransfer.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div></auth:then></auth:if>
<div class="field-row"><div class="field-label">Start Time</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.startTime}"><content:content name="datatransfer.startTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Retry Time</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.retryTime && datatransfer.startTime != datatransfer.retryTime}"><content:content name="datatransfer.retryTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/><span class="text-muted small ms-1 fw-semibold">(S:<span class="val-num">${datatransfer.startCount}</span>, R:<span class="val-num">${datatransfer.requeueCount}</span>)</span></c:when><c:otherwise><span class="text-muted">No retries</span></c:otherwise></c:choose></div></div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><div class="field-row"><div class="field-label">Latest</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.latestTime}"><content:content name="datatransfer.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div></auth:then></auth:if>
<div class="field-row"><div class="field-label"><c:out value="${finishTimeTitle}"/></div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.realFinishTime}"><content:content name="datatransfer.realFinishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Expiry Date</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.expiryDate}"><content:content name="datatransfer.expiryDate" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><div class="field-row"><div class="field-label">Predicted</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.predictedTime}"><content:content name="datatransfer.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div><div class="field-row"><div class="field-label">Target Time</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.targetTime}"><content:content name="datatransfer.targetTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div></auth:then></auth:if>
<c:if test="${not empty refinished}"><div class="field-row"><div class="field-label">Last Finish Time</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.finishTime}"><content:content name="datatransfer.finishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div></c:if>
<c:if test="${not empty requeued}"><div class="field-row"><div class="field-label">Real Sch. Time</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.queueTime}"><content:content name="datatransfer.queueTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div></c:if>
</div>
</div>
</div>

<%-- Card: Network Statistics (TCP socket statistics per connection) --%>
<c:if test="${not empty transferStatistics}">
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-diagram-3 text-primary"></i>
<span class="fw-semibold">Network Statistics</span>
<span class="badge rounded-pill bg-secondary ms-1">${fn:length(transferStatistics)}</span>
<button class="btn btn-link btn-sm ms-auto p-0 text-secondary" type="button"
        data-bs-toggle="collapse" data-bs-target="#nst-collapse" aria-expanded="true">
  <i class="bi bi-chevron-up" id="nst-chevron"></i>
</button>
</div>
<div id="nst-collapse" class="collapse show">
<div class="card-body pt-2 pb-1">
<%-- Summary row --%>
<c:set var="_nstTotalSent" value="0"/>
<c:set var="_nstTotalRecv" value="0"/>
<c:set var="_nstMaxRate" value="0"/>
<c:set var="_nstMinStart" value="0"/>
<c:set var="_nstMaxEnd" value="0"/>
<c:set var="_nstHasSent" value="false"/>
<c:set var="_nstHasRecv" value="false"/>
<c:forEach var="ts" items="${transferStatistics}" varStatus="loop">
  <c:if test="${not empty ts.bytesSent}">
    <c:set var="_nstTotalSent" value="${_nstTotalSent + ts.bytesSent}"/>
    <c:set var="_nstHasSent" value="true"/>
  </c:if>
  <c:if test="${not empty ts.bytesReceived}">
    <c:set var="_nstTotalRecv" value="${_nstTotalRecv + ts.bytesReceived}"/>
    <c:set var="_nstHasRecv" value="true"/>
  </c:if>
  <c:if test="${not empty ts.deliveryRateBps and ts.deliveryRateBps > _nstMaxRate}">
    <c:set var="_nstMaxRate" value="${ts.deliveryRateBps}"/>
  </c:if>
  <c:if test="${loop.first or ts.startTime < _nstMinStart}">
    <c:set var="_nstMinStart" value="${ts.startTime}"/>
  </c:if>
  <c:if test="${loop.first or ts.endTime > _nstMaxEnd}">
    <c:set var="_nstMaxEnd" value="${ts.endTime}"/>
  </c:if>
</c:forEach>
<c:set var="_nstSpan" value="${_nstMaxEnd - _nstMinStart}"/>
<c:if test="${_nstSpan le 0}"><c:set var="_nstSpan" value="1"/></c:if>
<div class="d-flex flex-wrap gap-3 mb-3 small text-muted">
  <span><i class="bi bi-arrow-up-circle text-primary me-1"></i>Total sent:
    <c:choose>
      <c:when test="${_nstHasSent}"><strong class="text-body" id="nst-total-sent" data-bytes="${_nstTotalSent}">${_nstTotalSent} B</strong></c:when>
      <c:otherwise><strong class="text-body">&#8212;</strong></c:otherwise>
    </c:choose>
  </span>
  <span><i class="bi bi-arrow-down-circle text-success me-1"></i>Total recv:
    <c:choose>
      <c:when test="${_nstHasRecv}"><strong class="text-body" id="nst-total-recv" data-bytes="${_nstTotalRecv}">${_nstTotalRecv} B</strong></c:when>
      <c:otherwise><strong class="text-body">&#8212;</strong></c:otherwise>
    </c:choose>
  </span>
  <span><i class="bi bi-speedometer2 text-warning me-1"></i>Peak delivery: <strong class="text-body" id="nst-max-rate" data-bps="${_nstMaxRate}">${_nstMaxRate} bps</strong></span>
  <span><i class="bi bi-clock text-secondary me-1"></i>Wall time: <strong class="text-body">${_nstMaxEnd - _nstMinStart} ms</strong></span>
</div>
<%-- Per-connection timeline bars --%>
<div class="nst-timeline mb-2" style="position:relative;overflow-x:auto;">
<c:forEach var="ts" items="${transferStatistics}" varStatus="loop">
<c:set var="_nstLeft" value="${(ts.startTime - _nstMinStart) * 100 / _nstSpan}"/>
<c:set var="_nstWidth" value="${(ts.endTime - ts.startTime) * 100 / _nstSpan}"/>
<c:if test="${_nstWidth lt 1}"><c:set var="_nstWidth" value="1"/></c:if>
<c:set var="_nstRateRel" value="0"/>
<c:if test="${not empty ts.deliveryRateBps and _nstMaxRate gt 0}">
  <c:set var="_nstRateRel" value="${ts.deliveryRateBps * 100 / _nstMaxRate}"/>
</c:if>
<c:choose>
  <c:when test="${_nstRateRel ge 75}"><c:set var="_nstBarColor" value="#198754"/></c:when>
  <c:when test="${_nstRateRel ge 40}"><c:set var="_nstBarColor" value="#0d6efd"/></c:when>
  <c:otherwise><c:set var="_nstBarColor" value="#6c757d"/></c:otherwise>
</c:choose>
<div style="position:relative;height:28px;background:var(--bs-tertiary-bg);border-radius:4px;margin-bottom:4px;overflow:hidden;"
     title="Conn ${loop.index+1}: ${ts.remoteAddress} | ${ts.durationMs}ms | RTT: ${ts.rttMs}ms | Sent: ${ts.bytesSent}B | Rate: ${ts.deliveryRateBps}bps">
  <div style="position:absolute;left:${_nstLeft}%;width:${_nstWidth}%;height:100%;background:${_nstBarColor};opacity:0.8;border-radius:3px;">
  </div>
  <div style="position:absolute;left:${_nstLeft}%;padding:0 6px;line-height:28px;font-size:11px;color:#fff;white-space:nowrap;overflow:hidden;max-width:${_nstWidth}%;">
    <c:out value="${ts.remoteAddress}"/>
  </div>
</div>
</c:forEach>
<div style="display:flex;justify-content:space-between;font-size:10px;color:var(--bs-secondary-color);margin-top:2px;">
  <span>0 ms</span><span>${_nstMaxEnd - _nstMinStart} ms</span>
</div>
</div>
<%-- Per-connection detail table --%>
<div class="table-responsive">
<table class="table table-sm table-hover mb-0" style="font-size:0.8rem;">
<thead class="table-light">
<tr>
  <th>#</th><th>Remote</th><th>Duration</th><th>RTT (ms)</th>
  <th>Sent (B)</th><th>Rcvd (B)</th><th>Delivery Rate</th><th>cwnd</th><th>Segs out/in</th>
</tr>
</thead>
<tbody>
<c:forEach var="ts" items="${transferStatistics}" varStatus="loop">
<tr>
  <td class="text-muted">${loop.index+1}</td>
  <td><span class="val-code small">${empty ts.remoteAddress ? '&#8212;' : ts.remoteAddress}</span></td>
  <td><span class="val-num">${ts.durationMs}</span> ms</td>
  <td><span class="val-num">${empty ts.rttMs ? '&#8212;' : ts.rttMs}</span></td>
  <td><span class="val-num">${empty ts.bytesSent ? '&#8212;' : ts.bytesSent}</span></td>
  <td><span class="val-num">${empty ts.bytesReceived ? '&#8212;' : ts.bytesReceived}</span></td>
  <td><c:choose><c:when test="${not empty ts.deliveryRateBps}">
    <span class="nst-rate" data-bps="${ts.deliveryRateBps}">${ts.deliveryRateBps} bps</span>
  </c:when><c:otherwise>&#8212;</c:otherwise></c:choose></td>
  <td>${empty ts.cwnd ? '&#8212;' : ts.cwnd}</td>
  <td>${empty ts.segsOut ? '&#8212;' : ts.segsOut} / ${empty ts.segsIn ? '&#8212;' : ts.segsIn}</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
</div>
</div>
</div>
</c:if>

<c:if test="${not empty transferStatistics}">
<script>
(function() {
    function fmtBytes(b) {
        if (b >= 1073741824) return (b/1073741824).toFixed(2) + ' GiB';
        if (b >= 1048576) return (b/1048576).toFixed(2) + ' MiB';
        if (b >= 1024) return (b/1024).toFixed(1) + ' KiB';
        return b + ' B';
    }
    function fmtBps(b) {
        if (b >= 1000000000) return (b/1000000000).toFixed(2) + ' Gbps';
        if (b >= 1000000) return (b/1000000).toFixed(2) + ' Mbps';
        if (b >= 1000) return (b/1000).toFixed(1) + ' Kbps';
        return b + ' bps';
    }
    var el;
    el = document.getElementById('nst-total-sent');
    if (el) { var b=parseInt(el.dataset.bytes); if(!isNaN(b)) el.textContent=fmtBytes(b); }
    el = document.getElementById('nst-total-recv');
    if (el) { var b=parseInt(el.dataset.bytes); if(!isNaN(b)) el.textContent=fmtBytes(b); }
    el = document.getElementById('nst-max-rate');
    if (el) { var b=parseInt(el.dataset.bps); if(!isNaN(b)) el.textContent=fmtBps(b); }
    document.querySelectorAll('.nst-rate').forEach(function(s) {
        var b=parseInt(s.dataset.bps); if(!isNaN(b)) s.textContent=fmtBps(b);
    });
    // Collapse chevron toggle
    var col = document.getElementById('nst-collapse');
    if (col) col.addEventListener('hidden.bs.collapse', function() {
        var ch = document.getElementById('nst-chevron');
        if (ch) { ch.classList.remove('bi-chevron-up'); ch.classList.add('bi-chevron-down'); }
    });
    if (col) col.addEventListener('shown.bs.collapse', function() {
        var ch = document.getElementById('nst-chevron');
        if (ch) { ch.classList.remove('bi-chevron-down'); ch.classList.add('bi-chevron-up'); }
    });
})();
</script>
</c:if>

<%-- Live progress polling (only active when status is Transferring / Fetching / Arriving) --%>
<style>
@keyframes dtLivePulse { 0%,100% { opacity:1; } 50% { opacity:0.4; } }
</style>
<script>
(function() {
    var _dtStatusCode = '<c:out value="${datatransfer.statusCode}"/>';
    var _dtTransferId = '<c:out value="${datatransfer.id}"/>';
    var _dtActiveStates = ['EXEC', 'FETC', 'INIT'];
    if (_dtActiveStates.indexOf(_dtStatusCode) === -1) return;

    // Show the live indicator
    document.getElementById('dt-live-dot').style.display = '';

    var _dtPollFailures = 0;
    var _dtMaxFailures  = 5;
    var _dtInterval     = null;

    function _dtUpdateProgressBar(pct) {
        var wrap = document.getElementById('dt-progress-bar-wrap');
        if (!wrap) return;
        var bar = wrap.querySelector('.progress-bar');
        if (!bar) return;
        bar.style.width = Math.min(100, Math.max(0, pct)) + '%';
        bar.classList.remove('bg-success', 'bg-secondary');
        if (pct >= 100) bar.classList.add('bg-success');
        else if (pct <= 0) bar.classList.add('bg-secondary');
        wrap.setAttribute('aria-valuenow', pct);
    }

    function _dtPoll() {
        $.getJSON('/do/transfer/data/progress/' + _dtTransferId)
         .done(function(data) {
             _dtPollFailures = 0;
             // Restore the Live badge if it was showing a retry warning
             var $dot = $('#dt-live-dot');
             $dot.find('.badge')
                 .removeClass('bg-warning text-dark bg-secondary')
                 .addClass('bg-primary')
                 .css('animation', '')
                 .html('<i class="bi bi-broadcast me-1"></i>Live');

             // Update progress card
             var pct = data.progress || 0;
             var $pct = $('#dt-progress-pct');
             if ($pct.length) $pct.text(pct + '%');
             _dtUpdateProgressBar(pct);

             // Update duration card
             var $dur = $('#dt-duration-val');
             if ($dur.length) {
                 $dur.html(data.formattedDuration
                     ? data.formattedDuration
                     : '<span class="text-muted">&mdash;</span>');
             }

             // Update rate card
             var $rate = $('#dt-rate-val');
             if ($rate.length) {
                 if (data.transferRate > 0) {
                     $rate.html('<a style="text-decoration:none" title="Rate: '
                         + $('<span>').text(data.formattedTransferRate).html() + '">'
                         + data.formattedTransferRateInMBitsPerSeconds + ' Mbits/s</a>');
                 } else {
                     $rate.html('<span class="text-muted">&mdash;</span>');
                 }
             }

             // Update sent field
             var $sent = $('#dt-sent-val');
             if ($sent.length) {
                 $sent.attr('title', 'Sent: ' + data.formattedSent)
                      .text(data.sent + ' bytes');
             }

             // Stop polling when no longer transferring
             if (_dtActiveStates.indexOf(data.statusCode) === -1) {
                 clearInterval(_dtInterval);
                 document.getElementById('dt-live-dot').style.display = 'none';
             }

             // Update status badge (always, not just when stopped)
             var $statusVal = $('#dt-status-val');
             if ($statusVal.length && data.formattedStatus) {
                 var label = data.formattedStatus;
                 var sc    = data.statusCode;
                 var cls   = 'bg-secondary';
                 if (sc === 'DONE') cls = 'bg-success';
                 else if (sc === 'EXEC' || sc === 'FETC' || sc === 'INIT') cls = 'bg-primary';
                 else if (sc === 'RETR' || sc === 'WAIT' || sc === 'SCHE' || sc === 'HOLD') cls = 'bg-warning text-dark';
                 else if (sc === 'FAIL') cls = 'bg-danger';
                 $statusVal.html('<span class="badge ' + cls + '" title="' + label + '">' + label + '</span>');
             }
         })
         .fail(function(jqXHR) {
             _dtPollFailures++;
             var $dot = $('#dt-live-dot');
             // After first failure show a warning state on the badge
             if (_dtPollFailures === 1) {
                 $dot.find('.badge')
                     .removeClass('bg-primary')
                     .addClass('bg-warning text-dark')
                     .html('<i class="bi bi-exclamation-triangle-fill me-1"></i>Retrying…');
             }
             if (_dtPollFailures >= _dtMaxFailures) {
                 clearInterval(_dtInterval);
                 $dot.find('.badge')
                     .removeClass('bg-primary bg-warning text-dark')
                     .addClass('bg-secondary')
                     .css('animation', 'none')
                     .html('<i class="bi bi-wifi-off me-1"></i>Live updates unavailable');
             }
         });
    }

    // Poll every 3 seconds
    _dtInterval = setInterval(_dtPoll, 3000);
})();
</script>
