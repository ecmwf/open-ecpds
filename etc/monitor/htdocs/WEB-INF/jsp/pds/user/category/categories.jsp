<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty categories}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Web Categories found.
    <a href="<bean:message key="category.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty categories}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-collection text-primary"></i>
    <span class="fw-semibold">Web Categories</span>
    <div class="ms-auto d-flex align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="categoriesSearch" class="form-control" placeholder="Search categories..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="categoriesPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <a href="<bean:message key="category.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
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
</div>
</div>
</div>
<script>
$(document).ready(function() {
    var table = $('#categoriesTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('categoriesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('categoriesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#categoriesPageLen').val(_len);
    $('#categoriesPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('categoriesPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#categoriesSearch').on('keyup', function() { table.search(this.value).draw(); });
});
</script>
</c:if>
