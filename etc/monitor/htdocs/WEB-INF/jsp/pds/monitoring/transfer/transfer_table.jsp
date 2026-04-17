<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>

<auth:if basePathKey="destination.basepath" paths="/${destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
  <div class="alert">
    Error retrieving object by key <- Problem searching by key '${destination.name}' <- Destination not found: {${destination.name}}
  </div>
</c:if>

<c:if test="${authorized == 'true'}">

<tiles:insert name="date.select">
	<tiles:put name="show_chart_button">true</tiles:put>
</tiles:insert>

<br/>

<c:if test="${empty datatransfers}">
<div class="alert">
  No Data Transfers found based on these criteria!
</div>
</c:if>

<c:if test="${!empty datatransfers}">
<table id="transferMonTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Target</th>
            <th>TS</th>
            <th>Scheduled</th>
            <th>Target Time</th>
            <th>Predicted</th>
            <th>Finished</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="transfer" items="${datatransfers}">
        <c:catch var="_ex"><c:set var="_trSt" value="${transfer.transferStatus}"/></c:catch>
        <c:if test="${not empty _ex}"><c:set var="_trSt" value="-1"/></c:if>
        <c:catch var="_ex"><c:set var="_fmtSt" value="${transfer.formattedStatus}"/></c:catch>
        <c:if test="${not empty _ex}"><c:set var="_fmtSt" value=""/></c:if>
        <tr>
            <td><a href="/do/transfer/data/${transfer.id}">${transfer.target}</a></td>
            <td>${transfer.dataFile.timeStep}</td>
            <td><content:content name="transfer.scheduledTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td><content:content name="transfer.transferTargetTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td><content:content name="transfer.transferPredictedTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td><b><content:content name="transfer.finishTime" dateFormatKey="date.format.time" ignoreNull="true"/></b></td>
            <td>
                <span class="mon-letter mon-letter-s${_trSt lt 0 ? '0' : _trSt}" title="Transfer Status ${_trSt}">t</span>&nbsp;(${_fmtSt})
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#transferMonTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false
    });
});
</script>
</c:if>

</c:if>
