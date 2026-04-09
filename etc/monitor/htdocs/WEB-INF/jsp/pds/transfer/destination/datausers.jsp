<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:if test="${empty incomingUsers}">
    <div class="alert alert-info d-flex align-items-center gap-2 mt-3">
        <i class="bi bi-info-circle-fill"></i>
        <span>No data users are associated with this destination.</span>
    </div>
</c:if>

<c:if test="${not empty incomingUsers}">
<table id="destUsersTable" class="table table-sm table-hover align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Data Login</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="user" items="${incomingUsers}">
        <tr>
            <td><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></td>
            <td>${user.comment}</td>
            <td>${user.country.name}</td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:if test="${user.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!user.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#destUsersTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false
    });
});
</script>
</c:if>

<div class="mt-3">
    <auth:if basePathKey="incoming.basepath" paths="/">
        <auth:then>
            <a href="<bean:message key="incoming.basepath"/>?destinationNameForSearch=<c:out value="${destination.id}"/>"
               class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-box-arrow-up-right"></i> Manage in Data Users
            </a>
        </auth:then>
    </auth:if>
</div>
