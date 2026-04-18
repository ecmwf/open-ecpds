<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />

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

<html:hidden property="id" />

<table>
	<tr>
		<td style="width:1%;white-space:nowrap;vertical-align:top">

			<table class="fields">
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-folder2 text-primary flex-shrink-0"></i>
            Create a new Category to group access control resources.
        </div>
        </td></tr>
    </c:when>
    <c:otherwise>
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-folder2 text-primary flex-shrink-0"></i>
            Edit the Category details.
        </div>
        </td></tr>
    </c:otherwise>
</c:choose>

				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Name</th>
						<td><c:out value="${categoryActionForm.name}" /> <html:hidden property="name" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Name</th>
						<td>
							<div class="d-flex align-items-center gap-2">
								<input id="name" name="name" type="text"
									pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. admin-users)"
									oninput="validatePatternInput(this, 'name-feedback')">
								<span id="name-feedback"></span>
							</div>
						</td>
					</tr>
				</c:if>

				<tr>
					<th>Description</th>
					<td><html:text property="description" /></td>
				</tr>
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

  <%-- Web Resources --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-files text-secondary"></i>
        <strong>Web Resources</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#resourceChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty categoryActionForm.resources}">
            <p class="text-muted small mb-2"><em>No web resources assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="resource" items="${categoryActionForm.resources}">
                <span class="assoc-chip">
                  ${resource.path}
                  <a href="javascript:validate('<bean:message key="category.basepath"/>/edit/update/<c:out value="${categoryActionForm.id}"/>/deleteResource/<c:out value="${resource.id}"/>','<bean:message key="ecpds.category.deleteResource.warning" arg0="${resource.id}" arg1="${categoryActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="resourceChooser">
          <c:choose>
            <c:when test="${empty categoryActionForm.resourceOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No web resources available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search resources...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${categoryActionForm.resourceOptions}">
                  <a href="/do/user/category/edit/update/${categoryActionForm.id}/addResource/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span>${column.path}</span>
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
