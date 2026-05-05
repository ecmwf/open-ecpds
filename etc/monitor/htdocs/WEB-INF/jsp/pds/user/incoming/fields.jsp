<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<tiles:useAttribute id="actionFormName" name="action.form.name" classname="java.lang.String" />

<style>
.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px lightgray;
	border-radius: 4px;
	margin-top: 8px;
	margin-bottom: 4px;
}

table.fields {
	width: 100%;
	min-width: 600px;
}
table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}
.acc-help-btn {
    position: absolute; top: 50%; right: 3rem;
    transform: translateY(-50%);
    color: var(--bs-secondary-color); font-size: 0.9rem; line-height: 1;
    cursor: pointer; z-index: 10;
    transition: color 0.15s;
}
.acc-help-btn:hover { color: var(--bs-primary); }
.acc-help-btn.acc-help-active { color: var(--bs-primary); }
</style>

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

<c:choose>
    <c:when test="${isInsert == 'true'}">
    <div class="form-info-banner" style="margin-left:0; margin-bottom:0.5rem">
        <i class="bi bi-person-plus text-primary flex-shrink-0"></i>
        Create a new Data User account.
    </div>
    </c:when>
    <c:otherwise>
    <div class="form-info-banner" style="margin-left:0; margin-bottom:0.5rem">
        <i class="bi bi-person-plus text-primary flex-shrink-0"></i>
        Edit the Data User account.
    </div>
    </c:otherwise>
</c:choose>
<table border=0 style="width:100%; margin-bottom:1.25rem">
	<tr>
		<td style="width:1%;white-space:nowrap;vertical-align:top">
			<table class="fields" style="margin-left:1.5rem">
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Data Login</th>
						<td>${incomingUserActionForm.id}<html:hidden property="id" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Data Login <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Login name for this Data User. Use letters, digits, '_' and '.' only (e.g. john.doe_1)" tabindex="0"></i></th>
						<td>
							<div class="d-flex align-items-center gap-2">
								<input id="id" name="id" type="text" required
									pattern="[a-zA-Z0-9._]+"
									oninput="validatePatternInput(this, 'id-feedback')">
								<span id="id-feedback"></span>
							</div>
						</td>
					</tr>
				</c:if>
				<tr>
					<th>Comment <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="A short description or note for this user." tabindex="0"></i></th>
					<td><html:text property="comment" /></td>
				</tr>
				<tr>
					<th>Country <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Country associated with this user (used to display the corresponding flag)." tabindex="0"></i></th>
					<td><c:set var="countries" value="${incomingUserActionForm.countryOptions}" />
						<html:select property="countryIso" styleId="incomingCountryIso">
							<html:options collection="countries" property="iso" labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When disabled, this user cannot connect." tabindex="0"></i></th>
					<td><html:checkbox property="active" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>TOTP authentication <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Enable Time-based One-Time Password (TOTP) authentication. When enabled, the password field is not used." tabindex="0"></i></th>
					<td><html:checkbox property="isSynchronized" styleId="isSynchronized" onclick="handleTOTPClick(this)" /></td>
				</tr>
				<tr id="passwordRow">
					<th>Or password <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Password for authentication when TOTP is disabled. Use 'Generate' to create a secure random password." tabindex="0"></i></th>
					<td>
						<div class="d-flex align-items-center gap-2">
							<input type="password" id="password" name="password"
								value="${incomingUserActionForm.password}" />
							<button type="button" id="buttonPassword" name="buttonPassword"
								onclick="generatePassword(); return false">Generate</button>
						</div>
					</td>
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

  <%-- Data Policies --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-shield-check text-secondary"></i>
        <strong>Data Policies</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#policyChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty incomingUserActionForm.incomingPolicies}">
            <p class="text-muted small mb-2"><em>No data policies assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="policy" items="${incomingUserActionForm.incomingPolicies}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty policy.comment}"><span title="${policy.comment}">${policy.id}</span></c:when>
                    <c:otherwise>${policy.id}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deletePolicy/<c:out value="${policy.id}"/>','<bean:message key="ecpds.incoming.deletePolicy.warning" arg0="${policy.id}" arg1="${incomingUserActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="policyChooser">
          <c:choose>
            <c:when test="${empty incomingUserActionForm.incomingPolicyOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No data policies available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search policies...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${incomingUserActionForm.incomingPolicyOptions}">
                  <a href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addPolicy/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.id}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Destinations --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-geo-alt text-secondary"></i>
        <strong>Destinations</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#destChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty incomingUserActionForm.destinations}">
            <p class="text-muted small mb-2"><em>No destinations assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="destination" items="${incomingUserActionForm.destinations}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty destination.comment}"><span title="${destination.comment}">${destination.name}</span></c:when>
                    <c:otherwise>${destination.name}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deleteDestination/<c:out value="${destination.name}"/>','<bean:message key="ecpds.incoming.deleteDestination.warning" arg0="${destination.name}" arg1="${incomingUserActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="destChooser">
          <c:choose>
            <c:when test="${empty incomingUserActionForm.destinationOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No destinations available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search destinations...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${incomingUserActionForm.destinationOptions}">
                  <a href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addDestination/${column.name}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.value}">${column.name}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Permissions --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-gear text-secondary"></i>
        <strong>Permissions</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#operationChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty incomingUserActionForm.operations}">
            <p class="text-muted small mb-2"><em>No permissions assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="operation" items="${incomingUserActionForm.operations}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty operation.comment}"><span title="${operation.comment}">${operation.id}</span></c:when>
                    <c:otherwise>${operation.id}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deleteOperation/<c:out value="${operation.name}"/>','<bean:message key="ecpds.incoming.deleteOperation.warning" arg0="${operation.name}" arg1="${incomingUserActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="operationChooser">
          <c:choose>
            <c:when test="${empty incomingUserActionForm.operationOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No permissions available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search permissions...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${incomingUserActionForm.operationOptions}">
                  <a href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addOperation/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.id}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-plug text-secondary"></i>
        <strong>Current Sessions</strong>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty incomingUserActionForm.incomingUser.incomingConnections}">
            <p class="text-muted small mb-0"><em>No active sessions.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap">
              <c:forEach var="mySession" items="${incomingUserActionForm.incomingUser.incomingConnections}">
                <span class="assoc-chip">
                  <span title="Mover: ${mySession.dataMoverName} | Duration: ${mySession.formatedDuration}">${mySession.protocol} · ${mySession.remoteIpAddress}</span>
                  <a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/closeSession/<c:out value="${mySession.id}"/>','<bean:message key="ecpds.incoming.disconnectOperation.warning" arg0="${mySession.login}" arg1="${mySession.dataMoverName}"/>')" title="Disconnect"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

</div>

</c:if></td>
	</tr>

	<tr>
		<td colspan="3" style="padding:0 0 0 1.5rem">
			<div class="d-flex align-items-stretch" id="options-row">
				<div id="options-label" style="flex:0 0 auto;background:var(--bs-tertiary-bg);border-right:2px solid var(--bs-border-color);font-weight:600;white-space:nowrap;padding:0.4rem 0.6rem;text-align:right;font-size:10pt">Options</div>
				<div style="flex:1;min-width:0;padding:0.4rem 0.6rem">
					<div class="accordion" id="incomingOptionsAccordion" style="min-width:860px;max-width:860px">
					<div class="accordion-item">
						<h2 class="accordion-header" id="incomingAccHeadProperties" style="position:relative;">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingAccProperties" aria-expanded="false" aria-controls="incomingAccProperties">
								Properties
							</button>
							<span role="button" tabindex="0" class="acc-help-btn" id="incomingPropsHelpBtn"
								onclick="openIncomingHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openIncomingHelp();" title="Open properties reference">
								<i class="bi bi-question-circle"></i>
							</span>
						</h2>
						<div id="incomingAccProperties" class="accordion-collapse collapse" aria-labelledby="incomingAccHeadProperties" data-bs-parent="#incomingOptionsAccordion">
						<div class="accordion-body p-2">
							<pre id="userData" class="ace-panel"><c:out value="${requestScope[actionFormName].userData}" /></pre>
							<textarea id="userData" name="userData" style="display: none;"></textarea>
							<div class="d-flex align-items-center gap-2 mt-2">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorProperties); return false">Clear</button>
								<small class="text-muted ms-auto"><i class="bi bi-keyboard"></i> Ctrl+Space for completions</small>
							</div>
						</div>
						</div>
					</div>
					<div class="accordion-item">
						<h2 class="accordion-header" id="incomingAccHeadSSHKeys">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingAccSSHKeys" aria-expanded="false" aria-controls="incomingAccSSHKeys">
								Authorized SSH Keys
							</button>
						</h2>
						<div id="incomingAccSSHKeys" class="accordion-collapse collapse" aria-labelledby="incomingAccHeadSSHKeys" data-bs-parent="#incomingOptionsAccordion">
						<div class="accordion-body p-2">
							<pre id="authorizedSSHKeys" class="ace-panel"><c:out value="${requestScope[actionFormName].authorizedSSHKeys}" /></pre>
							<textarea id="authorizedSSHKeys" name="authorizedSSHKeys" style="display: none;"></textarea>
							<div class="mt-2">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorAuthorizedSSHKeys); return false">Clear</button>
							</div>
						</div>
						</div>
					</div>
				</div>
				</div>
			</div>
		</td>
	</tr>

</table>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="incomingHelpOffcanvas"
     aria-labelledby="incomingHelpOffcanvasLabel" style="width:min(480px,42vw);">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="incomingHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="incomingHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="incomingHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

<script>
	var editorProperties = getEditorProperties(false, true, "userData", "crystal");
	editorProperties.setOptions({minLines: 10, maxLines: 20});

	var completions = [
    	${requestScope[actionFormName].completions}
    ];

	$(document).ready(function() {
		$('#incomingHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
		var navEl = document.querySelector('#incomingHelpContent .help-nav');
		if (navEl) document.getElementById('incomingHelpNav').appendChild(navEl);

		// Country flag image next to select
		(function() {
			var VALID_ISO = new Set(['AC','AD','AE','AF','AG','AI','AL','AM','AO','AQ','AR','AS','AT','AU','AW','AX','AZ','BA','BB','BD','BE','BF','BG','BH','BI','BJ','BL','BM','BN','BO','BQ','BR','BS','BT','BV','BW','BY','BZ','CA','CC','CD','CF','CG','CH','CI','CK','CL','CM','CN','CO','CP','CR','CU','CV','CW','CX','CY','CZ','DE','DG','DJ','DK','DM','DO','DZ','EA','EE','EG','EH','ER','ES','ET','EU','EW','FI','FJ','FK','FM','FO','FR','FX','GA','GB','GD','GE','GF','GG','GH','GI','GL','GM','GN','GP','GQ','GR','GS','GT','GU','GW','GY','HK','HM','HN','HR','HT','HU','IC','ID','IE','IL','IM','IN','IO','IQ','IR','IS','IT','JE','JM','JO','JP','KE','KG','KH','KI','KM','KN','KP','KR','KW','KY','KZ','LA','LB','LC','LI','LK','LR','LS','LT','LU','LV','LY','MA','MC','MD','ME','MF','MG','MH','MK','ML','MM','MN','MO','MP','MQ','MR','MS','MT','MU','MV','MW','MX','MY','MZ','NA','NC','NE','NF','NG','NI','NL','NO','NP','NR','NU','NZ','OM','PA','PE','PF','PG','PH','PK','PL','PM','PN','PR','PS','PT','PW','PY','QA','RE','RO','RS','RU','RW','SA','SB','SC','SD','SE','SG','SH','SI','SJ','SK','SL','SM','SN','SO','SR','SS','ST','SV','SX','SY','SZ','TA','TC','TD','TF','TG','TH','TJ','TK','TL','TM','TN','TO','TP','TR','TT','TV','TW','TZ','UA','UG','UK','UM','UN','US','UY','UZ','VA','VC','VE','VG','VI','VN','VU','WF','WS','XK','YE','YT','ZA','ZM','ZR','ZW']);
			var $sel = $('#incomingCountryIso');
			var $flag = $('<span class="fi ms-2" style="font-size:1.3em;vertical-align:middle"></span>');
			$sel.after($flag);
			function updateFlag() {
				var iso = ($sel.val() || '').toUpperCase();
				if (VALID_ISO.has(iso)) {
					$flag.attr('class', 'fi fi-' + iso.toLowerCase() + ' ms-2').css('display', 'inline-block');
				} else {
					$flag.hide();
				}
			}
			$sel.on('change', updateFlag);
			updateFlag();
		})();
	});

	var customCompleter = {
  		getCompletions: function(editor, session, pos, prefix, callback) {
      		var line = session.getLine(editor.getCursorPosition().row);
   			completions.forEach(function(completion) {
      			completion.value = completion.caption + ' = ""';
    		});
      		var matchingCompletions = completions.filter(function(completion) {
      			return !checkIfExist(editor, completion.value) && (line.length === 0 || completion.value.startsWith(line));
    		}).map(function(completion) {
      			completion.value = prefix + completion.value.substring(line.length);
      			return completion;
    		});
      		if (matchingCompletions.length > 0) {
        		callback(null, matchingCompletions);
      		} else {
        		callback(null, []);
      		}
    	}
	};

	editorProperties.completers = [customCompleter];

	function _scrollIncomingHelpToCursor() {
		var row = editorProperties.selection.getCursor().row;
		var line = editorProperties.session.getLine(row) || '';
		line = line.trim();
		if (line && !line.startsWith('#') && !line.startsWith('//')) {
			var eqIdx = line.indexOf('=');
			var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
			if (paramName) scrollHelpToParam('incomingHelpContent', paramName);
		}
	}

	editorProperties.addEventListener("changeSelection", function (event) {
    	editorProperties.session.setAnnotations(
    		getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	checkEachLine(editorProperties);
		var _oc = document.getElementById('incomingHelpOffcanvas');
		if (_oc && _oc.classList.contains('show')) _scrollIncomingHelpToCursor();
    });

	editorProperties.getSession().on("change", function(e) {
  		if (e.action === "insert" && e.lines.length == 1 && e.lines[0] !== '"' && e.lines[0].endsWith('"')) {
    		setTimeout(function() {
				editorProperties.moveCursorTo(e.end.row, e.end.column - 1);
    			editorProperties.selection.clearSelection();
    		}, 0);
  		}
	});

	var textareaProperties = $('textarea[name="userData"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	var editorAuthorizedSSHKeys = getEditorProperties(false, false, "authorizedSSHKeys", "text");
	editorAuthorizedSSHKeys.setOptions({minLines: 10, maxLines: 20});

	document.getElementById('incomingAccProperties').addEventListener('shown.bs.collapse', function() {
		setTimeout(function() { editorProperties.resize(true); }, 50);
	});
	document.getElementById('incomingAccSSHKeys').addEventListener('shown.bs.collapse', function() {
		setTimeout(function() { editorAuthorizedSSHKeys.resize(true); }, 50);
	});
	window.openIncomingHelp = function() {
		var el = document.getElementById('incomingHelpOffcanvas');
		if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
	};
	var _incomingOffcanvasEl = document.getElementById('incomingHelpOffcanvas');
	if (_incomingOffcanvasEl) {
		_incomingOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
			var btn = document.getElementById('incomingPropsHelpBtn');
			if (btn) btn.classList.add('acc-help-active');
		});
		_incomingOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
			_scrollIncomingHelpToCursor();
		});
		_incomingOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
			var btn = document.getElementById('incomingPropsHelpBtn');
			if (btn) btn.classList.remove('acc-help-active');
		});
	}

	var textareaAuthorizedSSHKeys = $('textarea[name="authorizedSSHKeys"]');
	textareaAuthorizedSSHKeys.closest('form').submit(function() {
		textareaAuthorizedSSHKeys.val(editorAuthorizedSSHKeys.getSession().getValue());
	});

	makeResizable(editorProperties);
	makeResizable(editorAuthorizedSSHKeys);

	window.addEventListener('resize', function() {
		editorProperties.resize(true);
		editorAuthorizedSSHKeys.resize(true);
	});

	function handleTOTPClick(cb) {
		document.getElementById('passwordRow').style.display = cb.checked ? 'none' : '';
	}

	handleTOTPClick(document.getElementById("isSynchronized"));

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

    $('#id').bind('keypress', function (event) {
        var regex = new RegExp("^[a-zA-Z0-9._]+$");
        var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
        if (!regex.test(key)) {
            event.preventDefault();
            return false;
        }
    });
</script>


