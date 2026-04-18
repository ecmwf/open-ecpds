<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty users}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Web Users found.
    <a href="<bean:message key="user.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty users}">
<table id="usersWebTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Web Login</th>
            <th>Comment</th>
            <th class="text-center">Enabled</th>
            <th>Categories</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="user" items="${users}">
        <tr>
            <td><a href="<bean:message key="user.basepath"/>/${user.id}">${user.id}</a></td>
            <td>${user.commonName}</td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:if test="${user.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!user.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td>
                <c:forEach var="category" items="${user.categories}">
                    <a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}">${category.name}</a>&nbsp;
                </c:forEach>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/user/user/edit/update_form/${user.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/user/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    var createUrl = '<bean:message key="user.basepath"/>/edit/insert_form';
    var table = $('#usersWebTable').DataTable({
        paging:     true,
        pageLength: 25,
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: "<'d-flex align-items-center gap-2 mb-3'<'count-slot'>l<'ms-auto d-flex align-items-center gap-2'f<'users-web-create-slot'>>>t<'d-flex align-items-center mt-2'i<'ms-auto'p>>"
    });
    $('.count-slot').html('<span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>' + table.rows().count() + '</strong> user(s)</span>');
    $('.users-web-create-slot').html(
        '<a href="' + createUrl + '" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>'
    );
});
</script>
</c:if>
