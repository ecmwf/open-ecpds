<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty resources}">
<div class="alert alert-info mt-2">No Web Resources found.</div>
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
    $('#resourcesTable').DataTable({
        paging:    false,
        searching: true,
        ordering:  true,
        info:      false,
        columnDefs: [{ orderable: false, targets: -1 }]
    });
});
</script>
</c:if>
