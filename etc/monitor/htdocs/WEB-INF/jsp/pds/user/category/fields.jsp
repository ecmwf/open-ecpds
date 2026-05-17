<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
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

<div class="row g-3">
	<div class="col-lg-6">
		<div class="card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-folder2 text-primary"></i>
				<span class="fw-semibold">
					<c:choose>
						<c:when test="${isInsert == 'true'}">Create Web Category</c:when>
						<c:otherwise>Edit Web Category</c:otherwise>
					</c:choose>
				</span>
			</div>
			<div class="card-body">
				<html:hidden property="id" />
				<div class="d-flex flex-column gap-2">
					<c:if test="${isInsert != 'true'}">
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Name</label></div>
							<div class="col-sm-8"><c:out value="${categoryActionForm.name}" /> <html:hidden property="name" /></div>
						</div>
					</c:if>
					<c:if test="${isInsert == 'true'}">
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Name</label></div>
							<div class="col-sm-8">
								<div class="d-flex align-items-center gap-2">
									<input id="name" name="name" type="text" class="form-control form-control-sm"
										pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
										title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. admin-users)"
										oninput="validatePatternInput(this, 'name-feedback')">
									<span id="name-feedback"></span>
								</div>
							</div>
						</div>
					</c:if>
					<div class="row g-2 align-items-center">
						<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Description</label></div>
						<div class="col-sm-8"><html:text property="description" styleClass="form-control form-control-sm" /></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<c:if test="${isInsert != 'true'}">
	<div class="col-lg-6">
<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:var(--bs-tertiary-bg); font-size:.85rem; }
.assoc-card .card-header .ms-auto { margin-left:auto !important; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:var(--bs-secondary-bg); border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
.assoc-chip a { color:var(--bs-secondary-color); text-decoration:none; line-height:1; }
.assoc-chip a:hover { color:#dc3545; }
.assoc-chooser-item { color:var(--bs-body-color); font-size:.82rem; transition:background .15s; }
.assoc-chooser-item:hover { background:var(--bs-secondary-bg); }
.assoc-empty { display:flex; align-items:center; gap:.35rem; color:#856404; background:#fff3cd; border:1px solid #ffc107; border-radius:.25rem; font-size:.8rem; padding:.3rem .5rem; margin:0; }
</style>
<div class="row g-3" style="max-width:480px">

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
	</div>
	</c:if>
</div>
