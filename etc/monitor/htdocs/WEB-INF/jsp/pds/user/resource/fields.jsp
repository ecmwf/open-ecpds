<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
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

<table>

<tr>
<td style="width:1%;white-space:nowrap;vertical-align:top">

<table class="fields">
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <div class="form-info-banner">
            <i class="bi bi-link-45deg text-primary flex-shrink-0"></i>
            Create a new Resource URL mapping for access control.
        </div>
    </c:when>
    <c:otherwise>
        <div class="form-info-banner">
            <i class="bi bi-link-45deg text-primary flex-shrink-0"></i>
            Edit the Resource URL mapping.
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${isInsert != 'true'}">
<tr><th>Path</th><td>${resourceActionForm.path}<html:hidden property="path"/></td></tr>
</c:if>
<c:if test="${isInsert == 'true'}">
<tr><th>Path</th><td>
	<div class="d-flex align-items-center gap-2">
		<input id="path" name="path" type="text"
			pattern="/[a-zA-Z0-9_./-]*"
			title="Path must start with '/' and contain only letters, digits, '_', '-', '.' and '/' (e.g. /do/transfer/destination/edit/)"
			oninput="validatePatternInput(this, 'path-feedback')">
		<span id="path-feedback"></span>
	</div>
</td></tr>
</c:if>

</table>

</td>

<td colspan="2" style="vertical-align:top;padding-left:1rem"><c:if test="${isInsert != 'true'}">
<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-card .card-header .ms-auto { margin-left:auto !important; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
.assoc-chip a { color:#6c757d; text-decoration:none; line-height:1; }
.assoc-chip a:hover { color:#dc3545; }
.assoc-chooser-item { color:#212529; font-size:.82rem; transition:background .15s; }
.assoc-chooser-item:hover { background:#e9ecef; }
.assoc-empty { display:flex; align-items:center; gap:.35rem; color:#856404; background:#fff3cd; border:1px solid #ffc107; border-radius:.25rem; font-size:.8rem; padding:.3rem .5rem; margin:0; }
</style>
<div class="row g-3 mt-0" style="max-width:480px">

  <%-- Web Categories --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-folder2 text-secondary"></i>
        <strong>Web Categories</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#catChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
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
</c:if></td>
</tr>
</table>
