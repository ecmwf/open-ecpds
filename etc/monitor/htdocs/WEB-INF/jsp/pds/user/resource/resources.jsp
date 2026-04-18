<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty resources}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Web Resources found.
    <a href="<bean:message key="resource.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty resources}">
<table id="resourcesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Resource Path</th>
            <th>Categories</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="resource" items="${resources}">
        <tr>
            <td><a href="/do/user/resource/${resource.id}">${resource.path}</a></td>
            <td>
                <c:forEach var="category" items="${resource.categories}">
                    <a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}">${category.name}</a>&nbsp;
                </c:forEach>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" basePathKey="accesscontrol.basepath" href="/detailer?page=${resource.id}" imageKey="icon.small.text" imageTitleKey="ecpds.user.detailer"/>
                <auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/update_form/${resource.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/delete_form/${resource.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    var createUrl = '<bean:message key="resource.basepath"/>/edit/insert_form';
    var table = $('#resourcesTable').DataTable({
        paging:     true,
        pageLength: 25,
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: "<'d-flex align-items-center gap-2 mb-3'<'count-slot'>l<'ms-auto d-flex align-items-center gap-2'f<'resources-create-slot'>>>t<'d-flex align-items-center mt-2'i<'ms-auto'p>>"
    });
    $('.count-slot').html('<span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>' + table.rows().count() + '</strong> resource(s)</span>');
    $('.resources-create-slot').html(
        '<a href="' + createUrl + '" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>'
    );
});
</script>
</c:if>
