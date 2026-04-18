<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty ectransmodules}">
    <div class="d-flex align-items-center alert alert-info mt-2 gap-2">
        No Transfer Modules found.
        <auth:link basePathKey="module.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</c:if>

<c:if test="${not empty ectransmodules}">
<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(ectransmodules)}</strong> transfer module(s)</span>
    <auth:link basePathKey="module.basepath" href="/edit/insert_form"
               styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
</div>
<table id="modulesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Class Name</th>
            <th>Class Path</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${ectransmodules}">
        <tr>
            <td><a href="<bean:message key="module.basepath"/>/${row.id}">${row.name}</a></td>
            <td><code>${row.classe}</code></td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(row.archive) gt 0}"><code>${row.archive}</code></c:when>
                    <c:otherwise><span class="text-muted fst-italic">default</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${row.active ? 1 : 0}">
                <c:if test="${row.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!row.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/update_form/${row.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#modulesTable').DataTable({
        paging:    false,
        searching: false,
        ordering:  true,
        info:      false,
        columnDefs: [{ orderable: false, targets: -1 }]
    });
});
</script>
</c:if>
