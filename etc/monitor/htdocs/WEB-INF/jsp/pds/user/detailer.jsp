<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
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

<display:table name="${categories}" id="category" requestURI="" sort="list" class="listing">
    <display:column title="Name" sortable="true"><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></display:column>
    <display:column property="description" title="Description" sortable="true"/>
</display:table>

<%-- Users with access --%>
<div class="d-flex align-items-center gap-2 mb-2 mt-4">
    <i class="bi bi-person-check-fill text-success"></i>
    <span class="fw-semibold" style="font-size:0.78rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--bs-secondary-color);">Users with Access</span>
</div>

<display:table name="${users}" id="userWith" requestURI="" sort="list" class="listing">
    <display:column title="UID" sortable="true"><a href="<bean:message key="user.basepath"/>/${userWith.id}">${userWith.id}</a></display:column>
    <display:column title="Name" sortable="true">${userWith.commonName}</display:column>
    <display:column title="Categories" sortable="false">
        <c:forEach var="cat" items="${userWith.categories}">
            <a href="<bean:message key="category.basepath"/>/${cat.id}" title="${cat.description}"
               class="badge bg-primary text-decoration-none me-1" style="width:auto">${cat.name}</a>
        </c:forEach>
    </display:column>
</display:table>

<%-- Users without access --%>
<div class="d-flex align-items-center gap-2 mb-2 mt-4">
    <i class="bi bi-person-x-fill text-danger"></i>
    <span class="fw-semibold" style="font-size:0.78rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--bs-secondary-color);">Users without Access</span>
</div>

<display:table name="${usersNo}" id="userNo" requestURI="" sort="list" class="listing">
    <display:column title="UID" sortable="true"><a href="<bean:message key="user.basepath"/>/${userNo.id}">${userNo.id}</a></display:column>
    <display:column title="Name" sortable="true">${userNo.commonName}</display:column>
    <display:column title="Categories" sortable="false">
        <c:forEach var="cat" items="${userNo.categories}">
            <a href="<bean:message key="category.basepath"/>/${cat.id}" title="${cat.description}"
               class="badge bg-primary text-decoration-none me-1" style="width:auto">${cat.name}</a>
        </c:forEach>
    </display:column>
</display:table>

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

