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
<div class="fw-bold fs-6">${datatransfer.formattedDuration}</div>
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
<div class="card-body">
<div class="row g-3">
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Transfer ID</div>
<div>${datatransfer.id}</div>
</div>
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Data File ID</div>
<div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then><a href="<bean:message key="datafile.basepath"/>/${datatransfer.dataFileId}">${datatransfer.dataFileId}</a></auth:then>
<auth:else>${datatransfer.dataFileId}</auth:else>
</auth:if>
</div>
</div>
<div class="col-sm-5">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em"><c:out value="${datatransfer.destination.typeText}"/> Destination</div>
<div>
<a href="<bean:message key="destination.basepath"/>/<c:out value="${datatransfer.destination.name}"/>"><c:out value="${datatransfer.destination.name}"/></a>
<span class="text-muted small ms-1">(<c:out value="${datatransfer.destination.formattedStatus}"/>)</span>
</div>
</div>
<div class="col-sm-3">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Status</div>
<div>
<%-- Resolve display text for member-state vs full user --%>
<auth:if basePathKey="nonmemberstate.basepath" paths="">
<auth:then><c:set var="_dtStatus" value="${datatransfer.detailedStatus}"/></auth:then>
<auth:else><c:set var="_dtStatus" value="${datatransfer.memberStateDetailedStatus}"/></auth:else>
</auth:if>
<%-- Colour-coded badge by status code --%>
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
  <%-- SCHE (Preset), HOLD (StandBy), WAIT (Queued) --%>
  <span class="badge bg-secondary">${_dtStatus}</span>
</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Dissemination Host</div>
<div><a href="/do/transfer/host/${datatransfer.hostName}">${datatransfer.hostNickName}</a></div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Data Mover</div>
<div>
<auth:if basePathKey="transferserver.basepath" paths="/${datatransfer.transferServerName}">
<auth:then><a href="/do/datafile/transferserver/${datatransfer.transferServerName}">${datatransfer.transferServerName}</a></auth:then>
<auth:else>${datatransfer.transferServerName}</auth:else>
</auth:if>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">On Proxy</div>
<div>
<c:choose>
<c:when test="${datatransfer.proxy}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-12">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Target</div>
<div class="text-break font-monospace" style="font-size:0.85rem">
<c:choose>
<c:when test="${datatransfer.deleted}"><span class="text-danger">${datatransfer.target}</span></c:when>
<c:otherwise>${datatransfer.target}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Sent</div>
<div title="Sent: ${datatransfer.formattedSent}">${datatransfer.sent} bytes</div>
</div>
<c:if test="${not empty showFileSize}">
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Size</div>
<div title="Size: ${datatransfer.formattedSize}">${datatransfer.size} bytes</div>
</div>
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
<div class="card-body">
<div class="row g-3">
<div class="col-sm-3">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Product</div>
<div>${datafile.metaTime}-${datafile.metaStream} ${datafile.metaType} (${datafile.metaTarget})</div>
</div>
<div class="col-sm-3">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Prod Time</div>
<div><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Expired</div>
<div>
<c:choose>
<c:when test="${datatransfer.expired}"><i class="bi bi-x-circle-fill text-danger"></i> Yes</c:when>
<c:otherwise><i class="bi bi-check-circle-fill text-success"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Deleted</div>
<div>
<c:choose>
<c:when test="${datatransfer.deleted}"><i class="bi bi-x-circle-fill text-danger"></i> Yes</c:when>
<c:otherwise><i class="bi bi-check-circle-fill text-success"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Replicated</div>
<div>
<c:choose>
<c:when test="${datatransfer.replicated}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-2">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Backup</div>
<div>
<c:choose>
<c:when test="${datatransfer.backup}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-12">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Comment</div>
<div>${datatransfer.formattedComment}</div>
</div>
<div class="col-12">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Original</div>
<div class="text-break font-monospace" style="font-size:0.85rem">${datafile.formattedOriginal}</div>
</div>
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
<span class="fw-semibold">Timing</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em"><c:out value="${schedTimeTitle}"/></div>
<div><content:content name="datatransfer.scheduledTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">ASAP</div>
<div>
<c:choose>
<c:when test="${datatransfer.asap}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i> No</c:otherwise>
</c:choose>
</div>
</div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Earliest</div>
<div><content:content name="datatransfer.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
</auth:then>
</auth:if>

<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Start Time</div>
<div><content:content name="datatransfer.startTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Retry Time</div>
<div>
<c:choose>
<c:when test="${not empty datatransfer.retryTime && datatransfer.startTime != datatransfer.retryTime}">
<content:content name="datatransfer.retryTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>
<span class="text-muted small ms-1 fw-semibold">(S:${datatransfer.startCount}, R:${datatransfer.requeueCount})</span>
</c:when>
<c:otherwise><span class="text-muted">No retries</span></c:otherwise>
</c:choose>
</div>
</div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Latest</div>
<div><content:content name="datatransfer.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
</auth:then>
</auth:if>

<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em"><c:out value="${finishTimeTitle}"/></div>
<div><content:content name="datatransfer.realFinishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Expiry Date</div>
<div><content:content name="datatransfer.expiryDate" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Predicted</div>
<div><content:content name="datatransfer.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Target Time</div>
<div><content:content name="datatransfer.targetTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
</auth:then>
</auth:if>

<c:if test="${not empty refinished}">
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Last Finish Time</div>
<div><content:content name="datatransfer.finishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
</c:if>
<c:if test="${not empty requeued}">
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Real Sch. Time</div>
<div><content:content name="datatransfer.queueTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></div>
</div>
</c:if>
</div>
</div>
</div>
