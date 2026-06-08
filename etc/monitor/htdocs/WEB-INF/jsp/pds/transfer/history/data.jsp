<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
<i class="bi bi-clock-history text-primary flex-shrink-0"></i>
Transfer History Entry: <strong><c:out value="${item.id}"/></strong>
</div>

<%-- Card: Event Details --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-info-circle text-primary"></i>
<span class="fw-semibold">Event Details</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Event Time</div><div class="field-value"><c:choose><c:when test="${not empty item.date}"><content:content name="item.date" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Status</div><div class="field-value">
<c:choose>
  <c:when test="${item.status == 'DONE'}"><span class="badge bg-success" title="${item.formattedStatus}">${item.formattedStatus}</span></c:when>
  <c:when test="${item.status == 'EXEC' or item.status == 'FETC' or item.status == 'INIT'}"><span class="badge bg-primary" title="${item.formattedStatus}">${item.formattedStatus}</span></c:when>
  <c:when test="${item.status == 'RETR' or item.status == 'WAIT' or item.status == 'SCHE' or item.status == 'HOLD'}"><span class="badge bg-warning text-dark" title="${item.formattedStatus}">${item.formattedStatus}</span></c:when>
  <c:when test="${item.status == 'FAIL'}"><span class="badge bg-danger" title="${item.formattedStatus}">${item.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary" title="${item.formattedStatus}">${item.formattedStatus}</span></c:otherwise>
</c:choose>
</div></div>
<div class="field-row"><div class="field-label">Error</div><div class="field-value"><c:choose><c:when test="${item.error}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Sent</div><div class="field-value"><span class="val-num" title="${item.formattedSent}">${item.sent} bytes</span></div></div>
<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty item.formattedComment}">${item.formattedComment}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Card: Transfer Reference --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-arrow-left-right text-primary"></i>
<span class="fw-semibold">Transfer Reference</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Data Transfer</div><div class="field-value"><div class="d-flex flex-wrap align-items-baseline gap-2"><a href="<bean:message key="datatransfer.basepath"/>/${item.dataTransfer.id}"><span class="val-code">${item.dataTransfer.id}</span></a><c:if test="${not empty item.dataTransfer.target}"><span class="val-code text-break">${item.dataTransfer.target}</span></c:if></div></div></div>
<div class="field-row"><div class="field-label">Data File</div><div class="field-value"><div class="d-flex flex-wrap align-items-baseline gap-2"><auth:if basePathKey="datafile.basepath" paths="/${item.dataTransfer.dataFileId}"><auth:then><a href="<bean:message key="datafile.basepath"/>/${item.dataTransfer.dataFileId}"><span class="val-code">${item.dataTransfer.dataFileId}</span></a></auth:then><auth:else><span class="val-code">${item.dataTransfer.dataFileId}</span></auth:else></auth:if><c:if test="${not empty item.dataTransfer.dataFile.original}"><span class="val-code text-break">${item.dataTransfer.dataFile.original}</span></c:if></div></div></div>
<div class="field-row"><div class="field-label">Transfer Host</div><div class="field-value"><c:choose><c:when test="${not empty item.hostName}"><a href="<bean:message key="host.basepath"/>/${item.hostName}"><c:out value="${not empty item.hostNickName ? item.hostNickName : item.hostName}"/></a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<c:if test="${not empty item.dataTransfer.destinationName and not empty item.date}">
<div class="mt-3">
    <a href="/do/transfer/history?destinationName=<c:out value="${item.dataTransfer.destinationName}"/>&date=<content:content name="item.date" dateFormatKey="date.format.iso" ignoreNull="true"/>"
       class="btn btn-outline-primary">
        <i class="bi bi-arrow-left me-1"></i>Full Transfer History
    </a>
</div>
</c:if>
