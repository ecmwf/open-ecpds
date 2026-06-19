<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:useAttribute name="isInsert" classname="java.lang.String"/>

<script>
	function validate(path, message) {
	    confirmationDialog({
	        title: "Please Confirm",
	        message: message,
	        onConfirm: function () { window.location = path; },
	        onCancel: function () {}
	    });
	}
</script>

<div class="row g-3">
<div class="col-lg-6">
<div class="card">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-link-45deg text-primary"></i>
<span class="fw-semibold">
<c:choose>
<c:when test="${isInsert == 'true'}">Create Web Resource</c:when>
<c:otherwise>Edit Web Resource</c:otherwise>
</c:choose>
</span>
</div>
<div class="card-body">
<div class="d-flex flex-column gap-2">
<c:if test="${isInsert != 'true'}">
<div class="row g-2 align-items-center">
<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Path</label></div>
<div class="col-sm-8">${resourceActionForm.path}<html:hidden property="path"/></div>
</div>
</c:if>
<c:if test="${isInsert == 'true'}">
<div class="row g-2 align-items-center">
<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Path</label></div>
<div class="col-sm-8">
	<div class="d-flex align-items-center gap-2">
		<input id="path" name="path" type="text" class="form-control form-control-sm"
			pattern="/[a-zA-Z0-9_./-]*"
			title="Path must start with '/' and contain only letters, digits, '_', '-', '.' and '/' (e.g. /do/transfer/destination/edit/)"
			oninput="validatePatternInput(this, 'path-feedback')">
		<span id="path-feedback"></span>
	</div>
</div>
</div>
</c:if>
</div>
</div>
</div>
</div>
<c:if test="${isInsert != 'true'}">
<div class="col-lg-6">
<div class="row g-3" style="max-width:480px">

  <%-- Web Categories --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-folder2 text-secondary"></i>
        <strong>Web Categories</strong>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#catChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty resourceActionForm.categories}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="resource.basepath"/>/edit/update/<c:out value="${resourceActionForm.id}"/>/deleteAllCategories/all','Remove ALL web categories from resource <c:out value="${resourceActionForm.path}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty resourceActionForm.categories}">
            <p class="text-muted small mb-2"><em>No web categories assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="category" items="${resourceActionForm.categories}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty category.description}"><span title="${category.description}">${category.name}</span></c:when>
                    <c:otherwise>${category.name}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="resource.basepath"/>/edit/update/<c:out value="${resourceActionForm.id}"/>/deleteCategory/<c:out value="${category.id}"/>','<bean:message key="ecpds.resource.deleteCategory.warning" arg0="${category.name}" arg1="${resourceActionForm.path}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="catChooser">
          <c:choose>
            <c:when test="${empty resourceActionForm.categoryOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No web categories available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search categories...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${resourceActionForm.categoryOptions}">
                  <a href="/do/user/resource/edit/update/${resourceActionForm.id}/addCategory/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.description}">${column.name}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

</div>
</div>
</c:if>
</div>
