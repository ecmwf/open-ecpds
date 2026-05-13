<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:importAttribute name="isDelete" ignore="true"/>
<c:if test="${not empty isDelete}">
<tiles:insert page="./pds/datafile/datafile/warning.jsp"/>
</c:if>
<c:if test="${empty isDelete}">

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
<i class="bi bi-file-earmark-text text-primary flex-shrink-0"></i>
Data File: <strong><c:out value="${datafile.id}"/></strong>
</div>

<%-- Stat bar --%>
<div class="row g-2 mb-3">
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Size</div>
<div class="fw-bold fs-6">${datafile.formattedSize}</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Delete Original</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.deleteOriginal}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Deleted</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.deleted}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Removed</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.removed}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
</div>

<%-- Card: File Info --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-file-earmark-code text-primary"></i>
<span class="fw-semibold">File Info</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">ID</div><div class="field-value"><span class="val-code">${datafile.id}</span></div></div>
<div class="field-row"><div class="field-label">Source</div><div class="field-value"><span class="val-code text-break d-inline-block">${datafile.formattedOriginal}</span></div></div>
<div class="field-row"><div class="field-label">Checksum</div><div class="field-value"><c:choose><c:when test="${not empty datafile.checksum}"><span class="val-code text-break d-inline-block">${datafile.checksum}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Storage Path</div><div class="field-value"><c:choose><c:when test="${not empty datafile.storagePath}"><span class="val-code text-break d-inline-block">${datafile.storagePath}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Group By</div><div class="field-value"><c:choose><c:when test="${not empty datafile.groupBy}"><span class="val-code">${datafile.groupBy}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Additional Info</div><div class="field-value"><c:choose><c:when test="${not empty datafile.formattedCaller}"><span class="val-code">${datafile.formattedCaller}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Index</div><div class="field-value"><c:choose><c:when test="${datafile.index != 0}"><span class="val-num">${datafile.index} file(s)</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Card: Timing --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock text-primary"></i>
<span class="fw-semibold">Times (UTC)</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Product Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.productTime}"><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Earliest</div><div class="field-value"><c:choose><c:when test="${not empty datafile.earliestTime}"><content:content name="datafile.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Timestep</div><div class="field-value"><span class="val-num">${datafile.timeStep}</span></div></div>
<div class="field-row"><div class="field-label">Generation Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.productGenerationTime}"><content:content name="datafile.productGenerationTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Latest</div><div class="field-value"><c:choose><c:when test="${not empty datafile.latestTime}"><content:content name="datafile.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Arrival Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.arrivedTime}"><content:content name="datafile.arrivedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Predicted</div><div class="field-value"><c:choose><c:when test="${not empty datafile.predictedTime}"><content:content name="datafile.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Card: Acquisition --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-cloud-download text-primary"></i>
<span class="fw-semibold">Acquisition</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<c:if test="${datafile.groupBy == null}">
<div class="field-row"><div class="field-label">Pushed To</div><div class="field-value"><c:choose><c:when test="${datafile.getHost == null}"><span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-pushed</span></c:when><c:otherwise><span class="val-code">${datafile.getHost}</span> <span class="text-muted small">(in <span class="val-num">${datafile.getDuration}</span> + <span class="val-num">${datafile.getProtocolOverhead}</span> overhead)</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Retrieved From</div><div class="field-value"><c:choose><c:when test="${datafile.getHost == null}"><span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-retrieved</span></c:when><c:otherwise><span class="val-code">${datafile.getHost}</span> <span class="text-muted small">(in <span class="val-num">${datafile.getDuration}</span> + <span class="val-num">${datafile.getProtocolOverhead}</span> overhead)</span></c:otherwise></c:choose></div></div>
</c:if>
<div class="field-row"><div class="field-label">Remote Host</div><div class="field-value"><c:choose><c:when test="${not empty datafile.remoteHost}"><span class="val-code">${datafile.remoteHost}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Submitted From</div><div class="field-value"><c:choose><c:when test="${not empty datafile.ecauthHost}"><span class="val-code">${datafile.ecauthHost}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Submitted By</div><div class="field-value"><c:choose><c:when test="${not empty datafile.ecauthUser}"><span class="val-code">${datafile.ecauthUser}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Metadata table --%>
<p class="fw-bold mb-1 mt-3">Meta Data for ${datafile.id}</p>
<c:if test="${empty datafile.metaData}">
<div class="alert alert-info mt-1">No Meta Data for ${datafile.id}</div>
</c:if>
<c:if test="${not empty datafile.metaData}">
<table id="metadataTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
<tr>
<th>Name</th>
<th>Value</th>
</tr>
</thead>
<tbody>
<c:forEach var="metadata" items="${datafile.metaData}">
<tr>
<td><a href="/do/datafile/metadata/attribute/${metadata.name}">${metadata.name}</a></td>
<td>${metadata.value}</td>
</tr>
</c:forEach>
</tbody>
</table>
<script>
$(document).ready(function() {
$('#metadataTable').DataTable({ paging: false, searching: false, ordering: true, info: false });
});
</script>
</c:if>

<%-- Transfers table --%>
<p class="fw-bold mb-1 mt-3">Transfers for this datafile</p>
<table id="datafileTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
<tr>
<th>Destination</th>
<th>Transfer Host</th>
<th title="Scheduled Time (UTC)">Sched. Time</th>
<th>Target</th>
<th>%</th>
<th>Mbits/s</th>
<th>Prior</th>
</tr>
</thead>
<tbody>
<c:forEach var="transfer" items="${datafile.dataTransfers}">
<c:set var="nickName" value="${transfer.hostNickName}" />
<jsp:useBean id="nickName" type="java.lang.String" />
<tr>
<td><a title="${transfer.destination.comment}" href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a></td>
<td>
<c:if test='<%="".equals(nickName)%>'>
<span class="text-muted">[not-transferred]</span>
</c:if>
<c:if test="<%=nickName.length()>0%>">
<c:if test="${transfer.transferServerName == null}">
<a href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
</c:if>
<c:if test="${transfer.transferServerName != null}">
<a title="Transmitted through ${transfer.transferServerName}" href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
</c:if>
</c:if>
</td>
<td><content:content name="transfer.scheduledTime" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
<td><a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}"><c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if></a></td>
<td>${transfer.progress}</td>
<td>
<c:if test="${transfer.transferRate != 0}">
<a style="text-decoration:none" title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
</c:if>
<c:if test="${transfer.transferRate == 0}">
<c:if test="${transfer.size != 0}">
<span class="text-muted">[n/a]</span>
</c:if>
<c:if test="${transfer.size == 0}">
<span class="text-muted" title="Empty file">[n/a]</span>
</c:if>
</c:if>
</td>
<td>${transfer.priority}</td>
</tr>
</c:forEach>
</tbody>
</table>
<script>
$(document).ready(function() {
$('#datafileTransfersTable').DataTable({
paging:    true,
pageLength: 25,
searching: true,
ordering:  true,
info:      true,
order:     [[2, 'asc']]
});
});
</script>
</c:if>
