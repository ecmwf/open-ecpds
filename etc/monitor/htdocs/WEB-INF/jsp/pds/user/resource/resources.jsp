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
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-key text-primary"></i>
    <span class="fw-semibold">Web Resources</span>
    <div class="ms-auto d-flex align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="resourcesSearch" class="form-control" placeholder="Search resources..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="resourcesPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <a href="<bean:message key="resource.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
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
</div>
</div>
</div>
<script>
$(document).ready(function() {
    var table = $('#resourcesTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('resourcesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('resourcesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#resourcesPageLen').val(_len);
    $('#resourcesPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('resourcesPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#resourcesSearch').on('keyup', function() { table.search(this.value).draw(); });
});
</script>
</c:if>
