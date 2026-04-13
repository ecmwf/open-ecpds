<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty methods}">
    <div class="alert alert-info mt-2">No Transfer Methods found.</div>
</c:if>

<c:if test="${not empty methods}">
<table id="methodsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Comment</th>
            <th class="text-center">Restrict</th>
            <th class="text-center">Resolve</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${methods}">
        <tr>
            <td><a href="<bean:message key="method.basepath"/>/${row.id}">${row.name}</a></td>
            <td>${row.comment}</td>
            <td class="text-center" data-order="${row.restrict ? 1 : 0}">
                <c:if test="${row.restrict}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!row.restrict}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td class="text-center" data-order="${row.resolve ? 1 : 0}">
                <c:if test="${row.resolve}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!row.resolve}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td class="text-center" data-order="${row.active ? 1 : 0}">
                <c:if test="${row.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!row.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/transfer/method/edit/update_form/${row.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/transfer/method/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#methodsTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false,
        columnDefs: [{ orderable: false, targets: -1 }]
    });
});
</script>
</c:if>
