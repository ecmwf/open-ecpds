<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty categories}">
<div class="alert alert-info mt-2">No Web Categories found.</div>
</c:if>

<c:if test="${not empty categories}">
<table id="categoriesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Resources</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="category" items="${categories}">
        <tr>
            <td><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></td>
            <td>${category.description}</td>
            <td>
                <c:set var="lastCatIndex" value="0"/>
                <c:forEach var="resource" items="${category.accessibleResources}" end="10" varStatus="status">
                    <a href="<bean:message key="resource.basepath"/>/${resource.id}">${resource.path}</a>&nbsp;
                    <c:set var="lastCatIndex" value="${status.index}"/>
                </c:forEach>
                <c:if test="${lastCatIndex >= 10}">....</c:if>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/user/category/edit/update_form/${category.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/category/edit/delete_form/${category.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#categoriesTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false,
        columnDefs: [{ orderable: false, targets: -1 }]
    });
});
</script>
</c:if>
