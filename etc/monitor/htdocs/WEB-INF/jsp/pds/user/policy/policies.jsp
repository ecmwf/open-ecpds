<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty policies}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Data Policies found.
    <a href="<bean:message key="policy.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty policies}">
<table id="policiesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Associated Destinations</th>
            <th class="text-center">Enabled</th>
            <th>Comment</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="policy" items="${policies}">
        <tr>
            <td><a href="<bean:message key="policy.basepath"/>/${policy.id}">${policy.id}</a></td>
            <td>
                <c:forEach var="destination" items="${policy.associatedDestinations}">
                    <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}">${destination.name}</a>&nbsp;
                </c:forEach>
            </td>
            <td class="text-center" data-order="${policy.active ? 1 : 0}">
                <c:if test="${policy.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!policy.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td>${policy.comment}</td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/user/policy/edit/update_form/${policy.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/policy/edit/delete_form/${policy.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    var createUrl = '<bean:message key="policy.basepath"/>/edit/insert_form';
    var table = $('#policiesTable').DataTable({
        paging:     true,
        pageLength: 25,
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: "<'d-flex align-items-center gap-2 mb-3'<'count-slot'>l<'ms-auto d-flex align-items-center gap-2'f<'policies-create-slot'>>>t<'d-flex align-items-center mt-2'i<'ms-auto'p>>"
    });
    $('.count-slot').html('<span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>' + table.rows().count() + '</strong> policy(ies)</span>');
    $('.policies-create-slot').html(
        '<a href="' + createUrl + '" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>'
    );
});
</script>
</c:if>
