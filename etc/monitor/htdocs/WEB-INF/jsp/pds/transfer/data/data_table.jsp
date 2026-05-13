<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
<i class="bi bi-arrow-left-right text-primary flex-shrink-0"></i>
Data Transfer: <strong><c:out value="${datatransfer.id}"/></strong>
</div>

<%-- Stat strip --%>
<div class="row g-2 mb-3">
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100 d-flex flex-column justify-content-center">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Progress</div>
<div class="fw-bold fs-6 mb-1">${datatransfer.progress}%</div>
<div class="px-3">
<div class="progress" style="height:6px" role="progressbar"
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
</div>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Duration</div>
<div class="fw-bold fs-6"><c:choose><c:when test="${not empty datatransfer.formattedDuration}">${datatransfer.formattedDuration}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Rate</div>
<div class="fw-bold fs-6">
<c:if test="${datatransfer.transferRate > 0}">
<a style="text-decoration:none" title="Rate: ${datatransfer.formattedTransferRate}">${datatransfer.formattedTransferRateInMBitsPerSeconds} Mbits/s</a>
</c:if>
<c:if test="${datatransfer.transferRate <= 0}"><span class="text-muted">&mdash;</span></c:if>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Priority</div>
<div class="fw-bold fs-6">${datatransfer.priority}</div>
</div>
</div>
</div>

<%-- Card: Identity --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-info-circle text-primary"></i>
<span class="fw-semibold">Transfer Details</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Transfer ID</div><div class="field-value"><span class="val-code">${datatransfer.id}</span></div></div>
<div class="field-row"><div class="field-label">Data File ID</div><div class="field-value"><auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}"><auth:then><a href="<bean:message key="datafile.basepath"/>/${datatransfer.dataFileId}"><span class="val-code">${datatransfer.dataFileId}</span></a></auth:then><auth:else><span class="val-code">${datatransfer.dataFileId}</span></auth:else></auth:if></div></div>
<div class="field-row"><div class="field-label"><c:out value="${datatransfer.destination.typeText}"/> Destination</div><div class="field-value"><a href="<bean:message key="destination.basepath"/>/<c:out value="${datatransfer.destination.name}"/>"><c:out value="${datatransfer.destination.name}"/></a><span class="text-muted small ms-1">(<c:out value="${datatransfer.destination.formattedStatus}"/>)</span></div></div>
<div class="field-row"><div class="field-label">Status</div><div class="field-value">
<auth:if basePathKey="nonmemberstate.basepath" paths="">
<auth:then><c:set var="_dtStatus" value="${datatransfer.detailedStatus}"/></auth:then>
<auth:else><c:set var="_dtStatus" value="${datatransfer.memberStateDetailedStatus}"/></auth:else>
</auth:if>
<c:choose>
<c:when test="${datatransfer.deleted}">
  <span class="badge bg-danger" title="Deleted">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'DONE'}">
  <span class="badge bg-success">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'EXEC' or datatransfer.statusCode == 'FETC'}">
  <span class="badge bg-primary">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'INIT'}">
  <span class="badge bg-info text-dark">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'RETR' or datatransfer.statusCode == 'STOP' or datatransfer.statusCode == 'INTR'}">
  <span class="badge bg-warning text-dark">${_dtStatus}</span>
</c:when>
<c:when test="${datatransfer.statusCode == 'FAIL'}">
  <span class="badge bg-danger">${_dtStatus}</span>
</c:when>
<c:otherwise>
  <span class="badge bg-secondary">${_dtStatus}</span>
</c:otherwise>
</c:choose>
</div></div>
<div class="field-row"><div class="field-label">Dissemination Host</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.hostName}"><a href="/do/transfer/host/${datatransfer.hostName}"><c:choose><c:when test="${not empty datatransfer.hostNickName}">${datatransfer.hostNickName}</c:when><c:otherwise>${datatransfer.hostName}</c:otherwise></c:choose></a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Data Mover</div><div class="field-value"><c:choose><c:when test="${not empty datatransfer.transferServerName}"><auth:if basePathKey="transferserver.basepath" paths="/${datatransfer.transferServerName}"><auth:then><a href="/do/datafile/transferserver/${datatransfer.transferServerName}">${datatransfer.transferServerName}</a></auth:then><auth:else><span class="val-code">${datatransfer.transferServerName}</span></auth:else></auth:if></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">On Proxy</div><div class="field-value"><c:choose><c:when test="${datatransfer.proxy}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Target</div><div class="field-value"><c:choose><c:when test="${datatransfer.deleted}"><span class="val-code text-danger text-break d-inline-block">${datatransfer.target}</span></c:when><c:otherwise><span class="val-code text-break d-inline-block">${datatransfer.target}</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Sent</div><div class="field-value"><span class="val-num" title="Sent: ${datatransfer.formattedSent}">${datatransfer.sent} bytes</span></div></div>
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
