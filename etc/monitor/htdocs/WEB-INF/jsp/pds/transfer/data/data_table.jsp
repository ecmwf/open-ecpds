<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:var(--bs-tertiary-bg,#f8f9fa); color:var(--bs-body-color); border-left:4px solid var(--bs-border-color,#dee2e6);">
<i class="bi bi-arrow-left-right text-success flex-shrink-0"></i>
Data Transfer: <strong><c:out value="${datatransfer.id}"/></strong>
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
        A data transfer is linked to a unique data file and represents a transfer request for its content, together with
        any related information (e.g. schedule, priority, progress, status, rate, errors, history). A single data file
        can be linked to several data transfers as many remote sites might be interested in obtaining the same products
        from the <strong><%=System.getProperty("monitor.title")%></strong>.
    </div>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Transfer ID</div><div class="field-value"><span class="val-code">${datatransfer.id}</span></div></div>
<div class="field-row"><div class="field-label">Data File ID</div><div class="field-value"><auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><a href="<bean:message key="datafile.basepath"/>/${datatransfer.dataFileId}"><span class="val-code">${datatransfer.dataFileId}</span></a></auth:then><auth:else><span class="val-code">${datatransfer.dataFileId}</span></auth:else></auth:if></div></div>
<div class="field-row"><div class="field-label"><c:out value="${datatransfer.destination.typeText}"/> Destination</div><div class="field-value"><a href="<bean:message key="destination.basepath"/>/<c:out value="${datatransfer.destination.name}"/>"><c:out value="${datatransfer.destination.name}"/></a><span class="text-muted small ms-1">(<c:out value="${datatransfer.destination.formattedStatus}"/>)</span></div></div>
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
