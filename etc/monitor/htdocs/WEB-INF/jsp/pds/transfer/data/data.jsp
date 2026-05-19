<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<c:set var="authorized" value="true" />
</auth:then>
<auth:else>
<auth:if basePathKey="destination.basepath" paths="/${datatransfer.destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>
</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
<div class="alert alert-danger mt-2">
<i class="bi bi-exclamation-triangle-fill me-2"></i>
Error retrieving object by key &mdash; DataTransfer not found: <code>${datatransfer.id}</code>
</div>
</c:if>

<c:if test="${authorized == 'true'}">

<jsp:include page="./pds/transfer/data/data_table.jsp"/>

<%-- Transfer History --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<c:set var="transferHistory" value="${datatransfer.transferHistory}"/>
<c:set var="canSeeHistoryDetail" value="true"/>
</auth:then>
<auth:else>
<c:set var="transferHistory" value="${datatransfer.transferHistoryAfterScheduledTime}"/>
</auth:else>
</auth:if>

<p class="fw-bold mb-1 mt-3">Transfer History</p>
<c:if test="${historyItemsSize == '0'}">
<div class="alert alert-info mt-1">No Transfer History available for this Data Transfer</div>
</c:if>
<c:if test="${historyItemsSize != '0'}">
<table id="transferHistoryTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
<tr>
<th>Err</th>
<th title="Event Time (UTC)">Event Time</th>
<th>Status</th>
<th>Transfer Host</th>
<th>Comment</th>
<th></th>
</tr>
</thead>
<tbody>
<c:forEach var="history" items="${historyItems}">
<tr>
<td>
<c:choose>
<c:when test="${history.error}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Err</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>OK</span></c:otherwise>
</c:choose>
</td>
<td data-order="${not empty history.date ? history.date.time : 0}">
<c:if test="${not empty canSeeHistoryDetail}">
<a href="<bean:message key="transferhistory.basepath"/>/${history.id}"><content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/></a>
</c:if>
<c:if test="${empty canSeeHistoryDetail}">
<content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
</td>
<td>
<c:choose>
  <c:when test="${history.status == 'DONE'}"><span class="badge bg-success">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'EXEC' or history.status == 'FETC'}"><span class="badge bg-primary">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'INIT'}"><span class="badge bg-info text-dark">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'RETR' or history.status == 'STOP' or history.status == 'INTR'}"><span class="badge bg-warning text-dark">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'FAIL'}"><span class="badge bg-danger">${history.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary">${history.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
<td>
<c:if test="${history.hostName != null}">
<a href="<bean:message key="host.basepath"/>/${history.hostName}">${history.hostNickName}</a>
</c:if>
<c:if test="${history.hostName == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>${history.formattedComment}</td>
<td>${history.id}</td>
</tr>
</c:forEach>
</tbody>
</table>
<script>
$(document).ready(function() {
$('#transferHistoryTable').DataTable({
paging:    true,
pageLength: 25,
searching: false,
ordering:  true,
info:      true,
order:     [[1, 'desc'], [5, 'desc']],
columnDefs: [{ targets: 5, visible: false, searchable: false, type: 'num' }]
});
});
</script>
</c:if>

<%-- Older Transfers with Same Identity --%>
<p class="fw-bold mb-1 mt-3">All Data Transfers with the same identity. &nbsp;<em>(${datatransfer.identity})</em></p>
<table id="olderTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
<tr>
<th>Destination</th>
<th>Transfer Host</th>
<th title="Scheduled Time (UTC)">Sched. Time</th>
<th title="Start Time (UTC)">Start Time</th>
<th title="Finish Time (UTC)">Finish Time</th>
<th>Target</th>
<th>TS</th>
<th>%</th>
<th>Mbits/s</th>
<th>Status</th>
<th>Prior</th>
</tr>
</thead>
<tbody>
<c:forEach var="transfer" items="${datatransfer.olderTransfersForSameDataFile}">
<c:set var="nickName" value="${transfer.hostNickName}" />
<jsp:useBean id="nickName" type="java.lang.String" />
<tr>
<td><a href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a></td>
<td>
<c:if test='<%="".equals(nickName)%>'>
<i class="bi bi-x-circle text-warning" title="Not transferred to remote host"></i>
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
<td>
<c:if test="${transfer.startTime != null}">
<content:content name="transfer.startTime" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
<c:if test="${transfer.startTime == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:if test="${transfer.realFinishTime != null}">
<content:content name="transfer.realFinishTime" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
<c:if test="${transfer.realFinishTime == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:if test="${transfer.id != datatransfer.id}">
<a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}">
<c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if>
</a>
</c:if>
<c:if test="${transfer.id == datatransfer.id}">
<span title="Size: ${transfer.formattedSize}" class="fw-semibold">
<c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if>
</span>
</c:if>
</td>
<td>${transfer.dataFile.timeStep}</td>
<td>${transfer.progress}</td>
<td>
<c:if test="${transfer.transferRate != 0}">
<a style="text-decoration:none" title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
</c:if>
<c:if test="${transfer.transferRate == 0}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:choose>
  <c:when test="${transfer.deleted}"><span class="badge bg-danger" title="Deleted">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'DONE'}"><span class="badge bg-success">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'EXEC' or transfer.statusCode == 'FETC'}"><span class="badge bg-primary">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'INIT'}"><span class="badge bg-info text-dark">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'RETR' or transfer.statusCode == 'STOP' or transfer.statusCode == 'INTR'}"><span class="badge bg-warning text-dark">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'FAIL'}"><span class="badge bg-danger">${transfer.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary">${transfer.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
<td>${transfer.priority}</td>
</tr>
</c:forEach>
</tbody>
</table>
<script>
$(document).ready(function() {
$('#olderTransfersTable').DataTable({
paging:    true,
pageLength: 25,
searching: true,
ordering:  true,
info:      true,
order:     [[2, 'desc']]
});
});
</script>

</c:if>
