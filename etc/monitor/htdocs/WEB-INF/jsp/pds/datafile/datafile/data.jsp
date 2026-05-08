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
<c:when test="${datafile.deleteOriginal}"><i class="bi bi-check-circle-fill text-success"></i></c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Deleted</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.deleted}"><i class="bi bi-check-circle-fill text-warning"></i></c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Removed</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.removed}"><i class="bi bi-check-circle-fill text-danger"></i></c:when>
<c:otherwise><i class="bi bi-x-circle-fill text-secondary"></i></c:otherwise>
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
<div class="card-body">
<div class="row g-3">
<div class="col-sm-1">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">ID</div>
<div>${datafile.id}</div>
</div>
<div class="col-sm-11">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Source</div>
<div class="text-break font-monospace" style="font-size:0.85rem">${datafile.formattedOriginal}</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Checksum</div>
<div class="font-monospace text-break" style="font-size:0.85rem">
<c:choose>
<c:when test="${datafile.checksum == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.checksum}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-8">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Storage Path</div>
<div class="text-break font-monospace" style="font-size:0.85rem">
<c:choose>
<c:when test="${datafile.storagePath == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.storagePath}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Group By</div>
<div>
<c:choose>
<c:when test="${datafile.groupBy == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.groupBy}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Additional Info</div>
<div>
<c:choose>
<c:when test="${datafile.formattedCaller == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.formattedCaller}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Index</div>
<div>
<c:choose>
<c:when test="${datafile.index == 0}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.index} file(s)</c:otherwise>
</c:choose>
</div>
</div>
</div>
</div>
</div>

<%-- Card: Timing --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock text-primary"></i>
<span class="fw-semibold">Timing</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Product Date</div>
<div><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Earliest</div>
<div><content:content name="datafile.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Timestep</div>
<div>${datafile.timeStep}</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Generation Date</div>
<div><content:content name="datafile.productGenerationTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Latest</div>
<div><content:content name="datafile.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Arrival Date</div>
<div><content:content name="datafile.arrivedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Predicted</div>
<div><content:content name="datafile.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</div>
</div>
</div>
</div>
</div>

<%-- Card: Acquisition --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-cloud-download text-primary"></i>
<span class="fw-semibold">Acquisition</span>
</div>
<div class="card-body">
<div class="row g-3">
<c:if test="${datafile.groupBy == null}">
<div class="col-sm-6">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Pushed To</div>
<div>
<c:choose>
<c:when test="${datafile.getHost == null}">
<span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-pushed</span>
</c:when>
<c:otherwise>
${datafile.getHost}
<span class="text-muted small">(in ${datafile.getDuration} + ${datafile.getProtocolOverhead} overhead)</span>
</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-6">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Retrieved From</div>
<div>
<c:choose>
<c:when test="${datafile.getHost == null}">
<span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-retrieved</span>
</c:when>
<c:otherwise>
${datafile.getHost}
<span class="text-muted small">(in ${datafile.getDuration} + ${datafile.getProtocolOverhead} overhead)</span>
</c:otherwise>
</c:choose>
</div>
</div>
</c:if>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Remote Host</div>
<div>
<c:choose>
<c:when test="${datafile.remoteHost == null}">
<span class="text-danger"><i class="bi bi-question-circle me-1"></i>unknown</span>
</c:when>
<c:otherwise>${datafile.remoteHost}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Submitted From</div>
<div>
<c:choose>
<c:when test="${datafile.ecauthHost == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.ecauthHost}</c:otherwise>
</c:choose>
</div>
</div>
<div class="col-sm-4">
<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Submitted By</div>
<div>
<c:choose>
<c:when test="${datafile.ecauthUser == null}"><span class="text-muted">[n/a]</span></c:when>
<c:otherwise>${datafile.ecauthUser}</c:otherwise>
</c:choose>
</div>
</div>
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
<th>Sched. Time</th>
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
