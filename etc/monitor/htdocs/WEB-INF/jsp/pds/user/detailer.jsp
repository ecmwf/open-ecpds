<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>

<%-- Resource path header --%>
<div class="d-flex align-items-center gap-2 mb-3 px-2 py-2 rounded"
     style="background:rgba(13,110,253,0.05); border-left:4px solid #0d6efd; font-size:0.85rem; color:var(--bs-body-color);">
    <i class="bi bi-link-45deg text-primary flex-shrink-0"></i>
    <span>Resource path: <code class="fw-semibold">${resource.path}</code></span>
</div>

<%-- Categories this resource belongs to --%>
<div class="d-flex align-items-center gap-2 mb-2 mt-3">
    <i class="bi bi-folder2-open text-secondary"></i>
    <span class="fw-semibold" style="font-size:0.78rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--bs-secondary-color);">Belongs to Categories</span>
</div>

<table id="detailerCatTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
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
<script>
$(document).ready(function() {
    $('#detailerCatTable').DataTable({ paging: false, searching: false, ordering: true, info: false });
});
</script>

<%-- Users with access --%>
<div class="d-flex align-items-center gap-2 mb-2 mt-4">
    <i class="bi bi-person-check-fill text-success"></i>
    <span class="fw-semibold" style="font-size:0.78rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--bs-secondary-color);">Users with Access</span>
</div>

<table id="detailerUsersWithTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>UID</th>
            <th>Name</th>
            <th>Categories</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="userWith" items="${users}">
        <tr>
            <td><a href="<bean:message key="user.basepath"/>/${userWith.id}">${userWith.id}</a></td>
            <td>${userWith.commonName}</td>
            <td>
                <c:forEach var="cat" items="${userWith.categories}">
                    <a href="<bean:message key="category.basepath"/>/${cat.id}" title="${cat.description}"
                       class="badge bg-primary text-decoration-none me-1" style="width:auto">${cat.name}</a>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#detailerUsersWithTable').DataTable({ paging: false, searching: true, ordering: true, info: false, columnDefs: [{ orderable: false, targets: -1 }] });
});
</script>

<%-- Users without access --%>
<div class="d-flex align-items-center gap-2 mb-2 mt-4">
    <i class="bi bi-person-x-fill text-danger"></i>
    <span class="fw-semibold" style="font-size:0.78rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--bs-secondary-color);">Users without Access</span>
</div>

<table id="detailerUsersNoTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>UID</th>
            <th>Name</th>
            <th>Categories</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="userNo" items="${usersNo}">
        <tr>
            <td><a href="<bean:message key="user.basepath"/>/${userNo.id}">${userNo.id}</a></td>
            <td>${userNo.commonName}</td>
            <td>
                <c:forEach var="cat" items="${userNo.categories}">
                    <a href="<bean:message key="category.basepath"/>/${cat.id}" title="${cat.description}"
                       class="badge bg-primary text-decoration-none me-1" style="width:auto">${cat.name}</a>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#detailerUsersNoTable').DataTable({ paging: false, searching: true, ordering: true, info: false, columnDefs: [{ orderable: false, targets: -1 }] });
});
</script>

<%-- Back button: ref must be a local path (starts with /) to prevent open redirect --%>
<div class="mt-4">
<c:choose>
  <c:when test="${not empty param.ref and fn:startsWith(param.ref, '/')}">
    <a href="${fn:escapeXml(param.ref)}" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Back
    </a>
  </c:when>
  <c:otherwise>
    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="history.back()">
        <i class="bi bi-arrow-left"></i> Back
    </button>
  </c:otherwise>
</c:choose>
</div>

