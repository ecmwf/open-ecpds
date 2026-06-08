<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>

<div class="d-flex flex-column gap-3">

<%-- Resource path header --%>
<div class="d-flex align-items-center gap-2 px-2 py-2 rounded"
     style="background:rgba(13,110,253,0.05); border-left:4px solid #0d6efd; color:var(--bs-body-color);">
    <i class="bi bi-link-45deg text-primary flex-shrink-0"></i>
    <span>Resource path: <code class="fw-semibold">${resource.path}</code></span>
</div>

<%-- Categories this resource belongs to --%>
<div class="card assoc-card">
    <div class="card-header">
        <i class="bi bi-folder2-open text-secondary"></i>
        <strong>Categories</strong>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" id="detailerCatSearch" class="form-control" placeholder="Search..." style="min-width:150px">
            </div>
            <div class="input-group flex-nowrap input-group-sm" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="detailerCatPageSize" class="form-select form-select-sm" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                </select>
            </div>
        </div>
    </div>
    <div class="table-responsive">
        <table id="detailerCatTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
            <thead class="table-light">
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="category" items="${categories}">
                <tr>
                    <td><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></td>
                    <td>${category.description}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<script>
$(document).ready(function() {
    var dtCat = $('#detailerCatTable').DataTable({
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        paging: true, searching: true, ordering: true, info: true
    });
    $('#detailerCatSearch').on('input', function() {
        dtCat.search(this.value).draw();
    });
    $('#detailerCatPageSize').on('change', function() {
        dtCat.page.len(this.value).draw();
    });
});
</script>

<%-- Users with access --%>
<div class="card assoc-card">
    <div class="card-header">
        <i class="bi bi-person-check-fill text-success"></i>
        <strong>Users with Access</strong>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" id="detailerUserSearch" class="form-control" placeholder="Search..." style="min-width:150px">
            </div>
            <div class="input-group flex-nowrap input-group-sm" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="detailerUserPageSize" class="form-select form-select-sm" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                </select>
            </div>
        </div>
    </div>
    <div class="table-responsive">
        <table id="detailerUsersWithTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
            <thead class="table-light">
                <tr>
                    <th>UID</th>
                    <th>Name</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="userWith" items="${users}">
                <tr>
                    <td><a href="<bean:message key="user.basepath"/>/${userWith.id}">${userWith.id}</a></td>
                    <td>${userWith.commonName}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<script>
$(document).ready(function() {
    var dt = $('#detailerUsersWithTable').DataTable({
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        paging: true, searching: true, ordering: true, info: true
    });
    $('#detailerUserSearch').on('input', function() {
        dt.search(this.value).draw();
    });
    $('#detailerUserPageSize').on('change', function() {
        dt.page.len(this.value).draw();
    });
});
</script>

</div>

<%-- Back button: ref must be a local path (starts with /) to prevent open redirect --%>
<div class="mt-3">
<c:choose>
  <c:when test="${not empty param.ref and fn:startsWith(param.ref, '/')}">
    <a href="${fn:escapeXml(param.ref)}" class="btn btn-outline-primary">
        <i class="bi bi-arrow-left me-1"></i>Back
    </a>
  </c:when>
  <c:otherwise>
    <button type="button" class="btn btn-outline-primary" onclick="history.back()">
        <i class="bi bi-arrow-left me-1"></i>Back
    </button>
  </c:otherwise>
</c:choose>
</div>

