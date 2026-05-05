<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

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
	            // Nothing needed — simply don't navigate
	        }
	    });
	}
</script>

<c:choose>
    <c:when test="${isInsert == 'true'}">
    <div class="form-info-banner" style="margin-left:0; margin-bottom:0.5rem">
        <i class="bi bi-person-badge text-primary flex-shrink-0"></i>
        Create a new User account.
    </div>
    </c:when>
    <c:otherwise>
    <div class="form-info-banner" style="margin-left:0; margin-bottom:0.5rem">
        <i class="bi bi-person-badge text-primary flex-shrink-0"></i>
        Edit the User account details.
    </div>
    </c:otherwise>
</c:choose>
<table>

	<tr>
		<td style="width:1%;white-space:nowrap;vertical-align:top">

			<table class="fields">
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Web Login</th>
						<td><c:out value="${userActionForm.uid}" /> <html:hidden
								property="uid" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Web Login</th>
						<td>
							<div class="d-flex align-items-center gap-2">
								<input id="uid" name="uid" type="text"
									pattern="[A-Za-z0-9]+(\.[A-Za-z0-9]+)*"
									title="Only letters and digits, optionally separated by a single '.'"
									oninput="validatePatternInput(this, 'uid-feedback')">
								<span id="uid-feedback"></span>
							</div>
							<div id="uid-feedback-msg" class="invalid-feedback" style="display:none">
								Must start and end with a letter or digit; single <code>.</code> separators only (e.g. <code>john.doe</code>).
							</div>
						</td>
					</tr>
				</c:if>
				<tr>
					<th>Password</th>
					<td>
						<div class="d-flex align-items-center gap-2">
							<input id="password" name="password" type="password"
								value="${userActionForm.password}">
							<button type="button" id="buttonPassword" name="buttonPassword"
								onclick="generatePassword(); return false">Generate</button>
						</div>
					</td>
				</tr>
				<tr>
					<th>Comment</th>
					<td><html:text property="name" /></td>
				</tr>
				<tr>
					<th>Enabled</th>
					<td><html:checkbox property="active" /></td>
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
<div class="row g-3" style="max-width:480px">

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
          <c:when test="${empty userActionForm.categories}">
            <p class="text-muted small mb-2"><em>No web categories assigned.</em></p>
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
</c:if></td>
	</tr>

	<tr>
		<td colspan="3">
			<table class="fields">
			</table>
				<tr style="display:none">
					<th>Properties</th>
					<td colspan="2"><pre id="userData">
							<c:out value="${userActionForm.userData}" />
						</pre> <textarea id="userData" name="userData" style="display: none;"></textarea>
						<button type="button"
							onclick="formatSource(editorProperties); return false">Format</button>
						<button type="button"
							onclick="clearSource(editorProperties); return false">Clear</button>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<script>
	var editorProperties = getEditorProperties(false, false, "userData", "crystal");

	var textareaProperties = $('textarea[name="userData"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	makeResizable(editorProperties);

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
    	    message: '<p class="mb-2 small">Please save this password \u2014 it will not be shown again:</p>' +
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
</script>