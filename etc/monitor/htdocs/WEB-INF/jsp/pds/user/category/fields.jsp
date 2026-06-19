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
										oninput="validatePatternInput(this, 'name-feedback'); _checkCategoryExists(this.value)">
									<span id="name-feedback"></span>
								</div>
								<div id="name-exists-msg" style="display:none" class="small mt-1"></div>
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
<div class="row g-3" style="max-width:480px">

  <%-- Web Resources --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-files text-secondary"></i>
        <strong>Web Resources</strong>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#resourceChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty categoryActionForm.resources}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="category.basepath"/>/edit/update/<c:out value="${categoryActionForm.id}"/>/deleteAllResources/all','Remove ALL web resources from category <c:out value="${categoryActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
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

<script>
var _checkCategoryTimer = null;
function _checkCategoryExists(value) {
  clearTimeout(_checkCategoryTimer);
  var $msg = $('#name-exists-msg');
  var $submit = $('button[type="submit"]').first();
  $msg.hide();
  $submit.prop('disabled', false);
  if (!value || value.length < 1) return;
  _checkCategoryTimer = setTimeout(function() {
    $.getJSON('/do/user/category?json=checkId&id=' + encodeURIComponent(value), function(data) {
      if (data.exists) {
        $msg.html('<i class="bi bi-x-circle-fill text-danger me-1"></i><span class="text-danger">Category <strong>' + $('<span>').text(value).html() + '</strong> already exists.</span>').show();
        $submit.prop('disabled', true);
      } else {
        $msg.html('<i class="bi bi-check-circle-fill text-success me-1"></i><span class="text-success">Available.</span>').show();
        $submit.prop('disabled', false);
      }
    });
  }, 400);
}
</script>
