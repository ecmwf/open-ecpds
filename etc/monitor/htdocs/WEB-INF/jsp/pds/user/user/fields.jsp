<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />

<style>
#userData {
	width: 550px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}
</style>

<script>
	function validate(path, message) {
	    confirmationDialog({
	        title: "Please Confirm",
	        message: message,     // HTML allowed by default
	        onConfirm: function () {
	            window.location = path;
	        },
	        onCancel: function () {
	            // Nothing needed -- simply don't navigate
	        }
	    });
	}
</script>

<div class="row g-3">
	<div class="col-lg-6">
		<div class="card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-person-badge text-primary"></i>
				<span class="fw-semibold">
					<c:choose>
						<c:when test="${isInsert == 'true'}">Create Web User</c:when>
						<c:otherwise>Edit Web User</c:otherwise>
					</c:choose>
				</span>
			</div>
			<div class="card-body">
				<div class="d-flex flex-column gap-2">
					<c:if test="${isInsert != 'true'}">
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Web Login</label></div>
							<div class="col-sm-8"><c:out value="${userActionForm.uid}" /> <html:hidden property="uid" /></div>
						</div>
					</c:if>
					<c:if test="${isInsert == 'true'}">
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Web Login</label></div>
							<div class="col-sm-8">
								<div class="d-flex align-items-center gap-2">
									<input id="uid" name="uid" type="text" class="form-control form-control-sm"
										pattern="[A-Za-z0-9]+(\.[A-Za-z0-9]+)*"
										title="Only letters and digits, optionally separated by a single '.'"
										oninput="validatePatternInput(this, 'uid-feedback'); _checkLoginExists(this.value, 'uid-exists-msg', 'user')">
									<span id="uid-feedback"></span>
								</div>
								<div id="uid-feedback-msg" class="invalid-feedback" style="display:none">
									Must start and end with a letter or digit; single <code>.</code> separators only (e.g. <code>john.doe</code>).
								</div>
								<div id="uid-exists-msg" style="display:none" class="small mt-1"></div>
							</div>
						</div>
					</c:if>
					<div class="row g-2 align-items-center">
						<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Password</label></div>
						<div class="col-sm-8">
							<div class="d-flex align-items-center gap-2">
								<input id="password" name="password" type="password" class="form-control form-control-sm"
									value="${userActionForm.password}">
								<button type="button" id="buttonPassword" name="buttonPassword"
									class="btn btn-sm btn-outline-secondary" onclick="generatePassword(); return false">Generate</button>
							</div>
						</div>
					</div>
					<div class="row g-2 align-items-center">
						<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Comment</label></div>
						<div class="col-sm-8"><html:text property="name" styleClass="form-control form-control-sm" /></div>
					</div>
					<div class="row g-2 align-items-center">
						<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Enabled</label></div>
						<div class="col-sm-8">
							<div class="form-check form-switch mb-0">
								<html:checkbox property="active" styleClass="form-check-input" />
							</div>
						</div>
					</div>
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
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Presets</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item" href="javascript:validate('/do/user/user/edit/update/<c:out value="${userActionForm.id}"/>/setAdminCategories/all','Set Admin user web categories for <c:out value="${userActionForm.id}"/>? This will replace all current web categories.')">
                <i class="bi bi-shield-fill text-warning me-1"></i>Admin Users
              </a>
            </li>
            <li>
              <a class="dropdown-item" href="javascript:validate('/do/user/user/edit/update/<c:out value="${userActionForm.id}"/>/setMonitorCategories/all','Set Monitor user web categories for <c:out value="${userActionForm.id}"/>? This will replace all current web categories.')">
                <i class="bi bi-eye text-info me-1"></i>Monitor Users
              </a>
            </li>
            <c:if test="${not empty userActionForm.categories}">
            <li><hr class="dropdown-divider"></li>
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('/do/user/user/edit/update/<c:out value="${userActionForm.id}"/>/deleteAllCategories/all','Remove ALL web categories for <c:out value="${userActionForm.id}"/>? The user may be unable to log in until categories are restored.')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
            </c:if>
          </ul>
        </div>
      </div>
      <div class="card-body p-2">
      <%-- Check if any assigned category is a destination category (ends with ' operations') --%>
      <c:set var="hasDestinationCategory" value="false"/>
      <c:set var="hasMonitorCategories" value="false"/>
      <c:set var="hasOtherCategories" value="false"/>
      <c:forEach var="category" items="${userActionForm.categories}">
        <c:choose>
          <c:when test="${fn:endsWith(category.name, ' operations')}">
            <c:set var="hasDestinationCategory" value="true"/>
          </c:when>
          <c:when test="${category.name == 'mstate' || category.name == 'monitoring' || category.name == 'transfers' || category.name == 'requirements'}">
            <c:set var="hasMonitorCategories" value="true"/>
          </c:when>
          <c:otherwise>
            <c:set var="hasOtherCategories" value="true"/>
          </c:otherwise>
        </c:choose>
      </c:forEach>
      <c:if test="${hasMonitorCategories && !hasDestinationCategory && !hasOtherCategories}">
        <div class="alert alert-warning py-2 px-3 mb-2 small" role="alert">
          <div class="d-flex align-items-start gap-2 mb-1">
            <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
            <strong>No destination assigned.</strong>
          </div>
          <div class="text-muted mb-2">Monitor users must have at least one destination category. Select one or more destinations below and click <em>Add Selected</em>.</div>
          <c:set var="hasDestinationOptions" value="false"/>
          <c:forEach var="column" items="${userActionForm.categoryOptions}">
            <c:if test="${fn:endsWith(column.name, ' operations')}"><c:set var="hasDestinationOptions" value="true"/></c:if>
          </c:forEach>
          <c:if test="${hasDestinationOptions}">
            <input type="text" id="destSearch" class="form-control form-control-sm mb-1" placeholder="Search destinations..." oninput="filterDestList(this)">
            <div id="destList" style="max-height:160px;overflow-y:auto;border:1px solid #dee2e6;border-radius:4px;background:#fff">
              <c:forEach var="column" items="${userActionForm.categoryOptions}">
                <c:if test="${fn:endsWith(column.name, ' operations')}">
                  <c:set var="destName" value="${fn:substring(column.name, 0, fn:length(column.name) - 11)}"/>
                  <label class="dest-item d-flex align-items-center gap-2 px-2 py-1 m-0" style="cursor:pointer">
                    <input type="checkbox" class="form-check-input flex-shrink-0 m-0" value="${column.id}" onchange="updateDestAddBtn()">
                    <c:out value="${destName}"/>
                  </label>
                </c:if>
              </c:forEach>
            </div>
            <div class="mt-2 d-flex gap-2">
              <button type="button" class="btn btn-sm btn-warning" id="destAddBtn" disabled onclick="addSelectedDestinations('${userActionForm.id}')">
                <i class="bi bi-plus-lg"></i> Add Selected
              </button>
              <button type="button" class="btn btn-sm btn-outline-secondary" onclick="toggleAllDest(this)">Select All</button>
            </div>
          </c:if>
        </div>
      </c:if>
      <c:choose>
        <c:when test="${empty userActionForm.categories}">
          <div class="alert alert-danger d-flex align-items-start gap-2 py-2 px-3 mb-2 small" role="alert">
            <i class="bi bi-x-octagon-fill flex-shrink-0 mt-1"></i>
            <div><strong>No web categories assigned.</strong> This user will not be able to log in to the portal until at least one category is added. Use the <i class="bi bi-chevron-down"></i> preset dropdown above to quickly assign the <strong>Admin Users</strong> or <strong>Monitor Users</strong> category set.</div>
          </div>
        </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="category" items="${userActionForm.categories}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty category.description}"><span title="${category.description}">${category.name}</span></c:when>
                    <c:otherwise>${category.name}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="user.basepath"/>/edit/update/<c:out value="${userActionForm.id}"/>/deleteCategory/<c:out value="${category.id}"/>','<bean:message key="ecpds.user.deleteCategory.warning" arg0="${category.name}" arg1="${userActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="catChooser">
          <c:choose>
            <c:when test="${empty userActionForm.categoryOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No web categories available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search categories...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${userActionForm.categoryOptions}">
                  <a href="/do/user/user/edit/update/${userActionForm.id}/addCategory/${column.id}"
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

<div style="display:none">
	<pre id="userData"><c:out value="${userActionForm.userData}" /></pre>
	<textarea id="userData" name="userData" style="display: none;"></textarea>
	<button type="button" onclick="formatSource(editorProperties); return false">Format</button>
	<button type="button" onclick="clearSource(editorProperties); return false">Clear</button>
</div>

<script>
	var editorProperties = getEditorProperties(false, false, "userData", "crystal");

	var textareaProperties = $('textarea[name="userData"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	makeResizable(editorProperties);

	function filterDestList(inp) {
		var q = inp.value.toLowerCase();
		document.querySelectorAll('#destList .dest-item').forEach(function(el) {
			el.style.display = el.textContent.toLowerCase().indexOf(q) === -1 ? 'none' : '';
		});
	}

	function updateDestAddBtn() {
		var btn = document.getElementById('destAddBtn');
		if (btn) btn.disabled = document.querySelectorAll('#destList input[type=checkbox]:checked').length === 0;
	}

	function toggleAllDest(btn) {
		var checkboxes = document.querySelectorAll('#destList .dest-item input[type=checkbox]');
		var allChecked = Array.from(checkboxes).every(function(cb) { return cb.checked || cb.closest('.dest-item').style.display === 'none'; });
		checkboxes.forEach(function(cb) {
			if (cb.closest('.dest-item').style.display !== 'none') cb.checked = !allChecked;
		});
		btn.textContent = allChecked ? 'Select All' : 'Deselect All';
		updateDestAddBtn();
	}

	function addSelectedDestinations(userId) {
		var ids = Array.from(document.querySelectorAll('#destList input[type=checkbox]:checked')).map(function(cb) { return cb.value; });
		if (ids.length > 0) location.href = '/do/user/user/edit/update/' + userId + '/addCategories/' + ids.join(',');
	}

    function copyGenPass(btn) {
        navigator.clipboard.writeText(document.getElementById('_genPass').value).then(function() {
            btn.innerHTML = '<i class="bi bi-check-lg"></i> Copied!';
        });
    }

    function generatePassword() {
    	var pass = getPassword();
    	$("#password").val(pass);
    	confirmationDialog({
    	    title: 'Password Generated',
    	    message: '<p class="mb-2 small">Please save this password &mdash; it will not be shown again:</p>' +
    	             '<div class="input-group input-group-sm">' +
    	             '<input type="text" class="form-control font-monospace" id="_genPass" value="' + pass + '" readonly>' +
    	             '<button class="btn btn-outline-secondary" type="button" onclick="copyGenPass(this)">' +
    	             '<i class="bi bi-clipboard"></i> Copy</button>' +
    	             '</div>' +
    	             '<p class="text-muted mt-2 mb-0 small">Click <strong>Process</strong> to save it.</p>',
    	    confirmText: 'OK',
    	    cancelText: 'Dismiss',
    	    showLoading: false,
    	    onConfirm: function() {},
    	    onCancel: function() {}
    	});
    }

    $('#uid').on('input', function () {
        const msg = document.getElementById('uid-feedback-msg');
        if (msg) msg.style.display = this.validity.valid || !this.value ? 'none' : 'block';
    });

    var _checkLoginTimer = null;
    function _checkLoginExists(value, msgId, type) {
        clearTimeout(_checkLoginTimer);
        var $msg = $('#' + msgId);
        var $submit = $('button[type="submit"]').first();
        $msg.hide();
        $submit.prop('disabled', false);
        if (!value || value.length < 1) return;
        _checkLoginTimer = setTimeout(function() {
            var url = type === 'incoming' ? '/do/user/incoming/list?json=checkId&id=' : '/do/user/user/list?json=checkId&id=';
            $.getJSON(url + encodeURIComponent(value), function(data) {
                if (data.exists) {
                    $msg.html('<i class="bi bi-x-circle-fill text-danger me-1"></i><span class="text-danger">Login <strong>' + $('<span>').text(value).html() + '</strong> is already taken.</span>').show();
                    $submit.prop('disabled', true);
                } else {
                    $msg.html('<i class="bi bi-check-circle-fill text-success me-1"></i><span class="text-success">Available.</span>').show();
                    $submit.prop('disabled', false);
                }
            });
        }, 400);
    }
</script>
