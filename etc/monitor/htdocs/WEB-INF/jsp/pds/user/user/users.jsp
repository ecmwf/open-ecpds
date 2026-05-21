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
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Web Users</span>
    <div class="ms-auto d-flex align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="usersWebSearch" class="form-control" placeholder="Search users..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="usersWebPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <a href="<bean:message key="user.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
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
                <c:choose>
                    <c:when test="${user.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
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
</div>
</div>
</div>
<script>
$(document).ready(function() {
    var table = $('#usersWebTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#usersWebPageLen').val(_len);
    $('#usersWebPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('usersWebPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#usersWebSearch').on('keyup', function() { table.search(this.value).draw(); });
});
</script>
</c:if>
