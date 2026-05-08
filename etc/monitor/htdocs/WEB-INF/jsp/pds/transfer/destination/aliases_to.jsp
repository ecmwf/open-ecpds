<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:set var="aliases" value="${destination.aliases}" scope="page"/>

<c:if test="${empty aliases}">
<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
  <i class="bi bi-info-circle-fill"></i>
  <span>No alias defined on destination <strong><c:out value="${destination.name}"/></strong>.</span>
</div>
</c:if>

<c:if test="${not empty aliases}">

<p class="fw-bold mb-1 mt-2">Destination ${destination.name} has Aliases To the following Destination(s):</p>
<table id="aliasToTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
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
    <c:forEach var="alias" items="${aliases}">
        <tr>
            <td><a href="/do/transfer/destination/${alias.name}">${alias.name}</a></td>
            <td>${alias.typeText}</td>
            <td>
<c:set var="_sb" value="${fn:contains(alias.formattedStatus, '-') ? fn:substringBefore(alias.formattedStatus, '-') : alias.formattedStatus}"/>
<c:choose>
  <c:when test="${_sb == 'Running'}"><span class="badge bg-success">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Restarting' or _sb == 'Resending'}"><span class="badge bg-info text-dark">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Waiting' or _sb == 'Retrying' or _sb == 'Interrupted'}"><span class="badge bg-warning text-dark">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Initialized' or _sb == 'Stopped' or _sb == 'NoHosts' or _sb == 'Failed'}"><span class="badge bg-danger">${alias.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary">${alias.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
            <td>${alias.dataAlias}</td>
            <td>${alias.comment}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#aliasToTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false
    });
});
</script>

</c:if>
