<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select">
	<tiles:put name="show_chart_button">true</tiles:put>
</tiles:insert>

<br/>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>

<c:if test="${empty datatransfers}">
<div class="alert">
  No Data Transfers found based on these criteria!
</div>
</c:if>

<c:if test="${!empty datatransfers}">
<table id="arrivalTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Original</th>
            <th>TS</th>
            <th>Target</th>
            <th>Predicted</th>
            <th>Arrival</th>
            <th>Scheduled</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="transfer" items="${datatransfers}">
        <c:catch var="_ex"><c:set var="_arrSt" value="${transfer.arrivalStatus}"/></c:catch>
        <c:if test="${not empty _ex}"><c:set var="_arrSt" value="-1"/></c:if>
        <tr>
            <td><a href="/do/transfer/data/${transfer.id}">${transfer.target}</a></td>
            <td>${transfer.dataFile.timeStep}</td>
            <td><content:content name="transfer.arrivalTargetTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td><content:content name="transfer.arrivalPredictedTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td><b><content:content name="transfer.dataFile.arrivedTime" dateFormatKey="date.format.time" ignoreNull="true"/></b></td>
            <td><content:content name="transfer.scheduledTime" dateFormatKey="date.format.time" ignoreNull="true"/></td>
            <td>
                <span class="mon-letter mon-letter-s${_arrSt lt 0 ? '0' : _arrSt}" title="Arrival Status ${_arrSt}">a</span>&nbsp;(${_arrSt})
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#arrivalTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false
    });
});
</script>
</c:if>

</auth:then>
</auth:if>
