<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:set var="aliasedFrom" value="${destination.aliasedFrom}" scope="page"/>

<c:if test="${empty aliasedFrom}">
<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
  <i class="bi bi-info-circle-fill"></i>
  <span>Destination <strong><c:out value="${destination.name}" /></strong> is not aliased from any destination.</span>
</div>
</c:if>

<c:if test="${not empty aliasedFrom}">

<p class="fw-bold mb-1 mt-2">Destination ${destination.name} is Aliased From the following Destination(s):</p>
<table id="aliasFromTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Status</th>
            <th>Rules</th>
            <th>Comment</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="alias" items="${aliasedFrom}">
        <tr>
            <td><a href="/do/transfer/destination/${alias.name}">${alias.name}</a></td>
            <td>${alias.typeText}</td>
            <td>${alias.formattedStatus}</td>
            <td>${alias.dataAlias}</td>
            <td>${alias.comment}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#aliasFromTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false
    });
});
</script>

</c:if>
