<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<tiles:useAttribute id="actionFormName" name="action.form.name" classname="java.lang.String" />

<script>
	function validate(path, message) {
	    confirmationDialog({
	        title: "Please Confirm",
	        message: message,
	        onConfirm: function () { window.location = path; },
	        onCancel: function () {}
	    });
	}
	function confirmCloseAll(path, count) {
	    confirmationDialog({
	        title: "Close All Sessions",
	        message: "Are you sure you want to close all " + count + " active session" + (count !== 1 ? "s" : "") + "?",
	        confirmText: "Close All",
	        showLoading: true,
	        onConfirm: function () { window.location = path; }
	    });
	}
</script>

<div class="row g-3">
  <%-- Column 1: Edit Data User form --%>
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-person-plus text-primary"></i>
        <span class="fw-semibold">
          <c:choose>
            <c:when test="${isInsert == 'true'}">Create Data User</c:when>
            <c:otherwise>Edit Data User</c:otherwise>
          </c:choose>
        </span>
      </div>
      <div class="card-body">
        <div class="d-flex flex-column gap-2">
          <c:if test="${isInsert != 'true'}">
            <div class="row g-2 align-items-center">
              <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Data Login</label></div>
              <div class="col-sm-8">${incomingUserActionForm.id}<html:hidden property="id" /></div>
            </div>
          </c:if>
          <c:if test="${isInsert == 'true'}">
            <div class="row g-2 align-items-center">
              <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Data Login <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Login name for this Data User. Use letters, digits, '_' and '.' only (e.g. john.doe_1)" tabindex="0"></i></label></div>
              <div class="col-sm-8">
                <div class="d-flex align-items-center gap-2">
                  <input id="id" name="id" type="text" required class="form-control form-control-sm"
                    pattern="[a-zA-Z0-9._]+"
                    oninput="validatePatternInput(this, 'id-feedback'); _checkLoginExists(this.value, 'id-exists-msg', 'incoming')">
                  <span id="id-feedback"></span>
                </div>
                <div id="id-exists-msg" style="display:none" class="small mt-1"></div>
              </div>
            </div>
          </c:if>
          <div class="row g-2 align-items-center">
            <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Comment <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="A short description or note for this user." tabindex="0"></i></label></div>
            <div class="col-sm-8"><html:text property="comment" styleClass="form-control form-control-sm" /></div>
          </div>
          <div class="row g-2 align-items-center">
            <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Country <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Country associated with this user (used to display the corresponding flag)." tabindex="0"></i></label></div>
            <div class="col-sm-8"><c:set var="countries" value="${incomingUserActionForm.countryOptions}" />
              <div class="d-flex align-items-center gap-2"><html:select property="countryIso" styleId="incomingCountryIso" styleClass="form-select form-select-sm flex-grow-1" style="min-width:0">
                <html:options collection="countries" property="iso" labelProperty="name" />
              </html:select></div></div>
          </div>
          <div class="row g-2 align-items-center">
            <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When disabled, this user cannot connect." tabindex="0"></i></label></div>
            <div class="col-sm-8"><div class="form-check form-switch mb-0"><html:checkbox property="active" styleClass="form-check-input" /></div></div>
          </div>
          <div class="row g-2 align-items-center">
            <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">TOTP authentication <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Enable Time-based One-Time Password (TOTP) authentication. When enabled, the password field is not used." tabindex="0"></i></label></div>
            <div class="col-sm-8"><div class="form-check form-switch mb-0"><html:checkbox property="isSynchronized" styleId="isSynchronized" onclick="handleTOTPClick(this)" styleClass="form-check-input" /></div></div>
          </div>
          <div class="row g-2 align-items-center" id="passwordRow">
            <div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted mb-0">Or password <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Password for authentication when TOTP is disabled. Use 'Generate' to create a secure random password." tabindex="0"></i></label></div>
            <div class="col-sm-8">
              <div class="d-flex align-items-center gap-2">
                <input type="password" id="password" name="password" class="form-control form-control-sm"
                  value="${incomingUserActionForm.password}" />
                <button type="button" id="buttonPassword" name="buttonPassword"
                  class="btn btn-sm btn-outline-secondary" onclick="generatePassword(); return false">Generate</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <%-- Column 2: Associations (update only) --%>
  <c:if test="${isInsert != 'true'}">
  <div class="col-lg-6">

<div class="row g-3">

  <%-- Data Policies + Permissions side-by-side --%>
  <div class="col-sm-6">
    <div class="card assoc-card h-100">
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

  <%-- Permissions --%>
  <div class="col-sm-6">
    <div class="card assoc-card h-100">
      <div class="card-header">
        <i class="bi bi-gear text-secondary"></i>
        <strong>Permissions</strong>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#operationChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Presets</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item" href="/do/user/incoming/edit/update/<c:out value="${incomingUserActionForm.id}"/>/addAllOperations/all">
                <i class="bi bi-check2-all text-success me-1"></i>Add All
              </a>
            </li>
            <li>
              <a class="dropdown-item" href="javascript:validate('/do/user/incoming/edit/update/<c:out value="${incomingUserActionForm.id}"/>/setReadOnlyOperations/all','Set read-only permissions (dir, get, mtime, size) for user <c:out value="${incomingUserActionForm.id}"/>? This will replace all current permissions.')">
                <i class="bi bi-eye text-info me-1"></i>Read Only
              </a>
            </li>
            <c:if test="${not empty incomingUserActionForm.operations}">
            <li><hr class="dropdown-divider"></li>
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('/do/user/incoming/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deleteAllOperations/all','Remove ALL permissions for user <c:out value="${incomingUserActionForm.id}"/>? The user will be unable to login until permissions are restored.')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
            </c:if>
          </ul>
        </div>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty incomingUserActionForm.operations}">
            <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-2 small" role="alert">
              <i class="bi bi-exclamation-triangle-fill flex-shrink-0" style="margin-top:0.1em"></i>
              <span><strong>No permissions assigned.</strong> All login attempts for this user will be denied until at least one permission is added. Use the <i class="bi bi-chevron-down"></i> preset dropdown above to quickly assign <strong>Add All</strong> or <strong>Read Only</strong> permissions.</span>
            </div>
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
        <%-- Check whether any associated policy provides at least one destination --%>
        <c:set var="policyHasDestinations" value="false"/>
        <c:forEach var="p" items="${incomingUserActionForm.incomingPolicies}">
          <c:if test="${not empty p.associatedDestinations}">
            <c:set var="policyHasDestinations" value="true"/>
          </c:if>
        </c:forEach>
        <c:choose>
          <c:when test="${empty incomingUserActionForm.destinations and not policyHasDestinations}">
            <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-2 small" role="alert">
              <i class="bi bi-exclamation-triangle-fill flex-shrink-0" style="margin-top:0.1em"></i>
              <span><strong>No destinations assigned.</strong> All login attempts for this user will be denied until at least one destination is added here or via an associated Data Policy.</span>
            </div>
          </c:when>
          <c:when test="${empty incomingUserActionForm.destinations and policyHasDestinations}">
            <div class="alert alert-info d-flex align-items-start gap-2 py-2 px-3 mb-2 small" role="alert">
              <i class="bi bi-info-circle-fill flex-shrink-0 mt-1"></i>
              <span>No destinations assigned directly, but access is granted through the associated Data <c:choose><c:when test="${fn:length(incomingUserActionForm.incomingPolicies) gt 1}">Policies</c:when><c:otherwise>Policy</c:otherwise></c:choose>.</span>
            </div>
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

</div><%-- closes inner associations row --%>

  </div><%-- closes col-lg-6 for associations --%>
  </c:if><%-- closes isInsert != 'true' wrapper for column 2 --%>

</div><%-- closes outer row g-3 --%>

<c:if test="${isInsert != 'true'}">
<div class="mt-3">
	<c:set var="sessionCount" value="${fn:length(incomingUserActionForm.incomingUser.incomingConnections)}"/>
	<div class="card assoc-card">
		<div class="card-header d-flex align-items-center gap-2">
			<i class="bi bi-plug text-secondary"></i>
			<strong>Current Sessions</strong>
			<span class="badge rounded-pill ${sessionCount > 0 ? 'bg-success' : 'bg-secondary'}" title="${sessionCount} active session${sessionCount != 1 ? 's' : ''}">${sessionCount}</span>
			<c:if test="${not empty incomingUserActionForm.incomingUser.incomingConnections}">
			<button type="button" class="btn btn-sm btn-outline-danger ms-auto"
			        onclick="confirmCloseAll('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/closeAllSessions/all', ${sessionCount})">
			  <i class="bi bi-x-circle"></i> Close All
			</button>
			</c:if>
		</div>
		<c:choose>
			<c:when test="${empty incomingUserActionForm.incomingUser.incomingConnections}">
				<div class="card-body p-2">
					<p class="text-muted small mb-0"><em>No active sessions.</em></p>
				</div>
			</c:when>
			<c:otherwise>
				<div class="table-responsive">
					<table id="incomingSessionsEditTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
						<thead class="table-warning">
							<tr>
								<th>Session ID</th>
								<th>Protocol</th>
								<th>Remote IP</th>
								<th>Data Mover</th>
								<th>Start Time (UTC)</th>
								<th>Duration</th>
								<th class="text-center no-sort">Action</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="mySession" items="${incomingUserActionForm.incomingUser.incomingConnections}">
								<tr>
									<td><code>${mySession.id}</code></td>
									<td><span class="badge bg-secondary-subtle text-secondary-emphasis border">${mySession.protocol}</span></td>
									<td>${mySession.remoteIpAddress}</td>
									<td>${mySession.dataMoverName}</td>
									<td data-order="${mySession.startTime}"><span class="ic-ts" data-ts="${mySession.startTime}">${mySession.startTime}</span></td>
									<td>${mySession.formatedDuration}</td>
									<td class="text-center">
										<a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/closeSession/<c:out value="${mySession.id}"/>','<bean:message key="ecpds.incoming.disconnectOperation.warning" arg0="${mySession.login}" arg1="${mySession.dataMoverName}"/>')"
										   class="btn btn-sm btn-outline-danger py-0 px-1" title="Disconnect session">
											<i class="bi bi-x-lg"></i>
										</a>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
				<script>
				$(function() {
					document.querySelectorAll('#incomingSessionsEditTable .ic-ts').forEach(function(el) {
						var ts = parseInt(el.getAttribute('data-ts'), 10);
						if (ts) el.textContent = new Date(ts).toISOString().replace('T', ' ').substring(0, 19);
					});
					if ($.fn.DataTable) {
						$('#incomingSessionsEditTable').DataTable({
							paging: false, searching: false, ordering: true,
							order: [[4, 'desc']],
							scrollY: '220px', scrollCollapse: true,
							dom: 't',
							columnDefs: [{ orderable: false, targets: [0, 6] }]
						});
					}
				});
				</script>
			</c:otherwise>
		</c:choose>
	</div>
</div>
</c:if>

<div class="mt-3">
	<div class="card">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-sliders text-primary"></i>
			<span class="fw-semibold">Options</span>
		</div>
		<div class="card-body p-2">
			<%-- Quota summary badges --%>
			<c:set var="uploadQuota" value="${requestScope[actionFormName].portalUploadQuota}"/>
			<c:set var="downloadQuota" value="${requestScope[actionFormName].portalDownloadQuota}"/>
			<c:if test="${not empty uploadQuota || not empty downloadQuota}">
			<div class="d-flex flex-wrap gap-2 mb-2 px-1">
			  <c:if test="${not empty uploadQuota}">
			    <span class="badge rounded-pill bg-warning text-dark" title="Upload quota limit">
			      <i class="bi bi-cloud-upload me-1"></i>Upload limit: <c:out value="${uploadQuota}"/>
			    </span>
			  </c:if>
			  <c:if test="${not empty downloadQuota}">
			    <span class="badge rounded-pill bg-info text-dark" title="Download quota limit">
			      <i class="bi bi-cloud-download me-1"></i>Download limit: <c:out value="${downloadQuota}"/>
			    </span>
			  </c:if>
			</div>
			</c:if>
			<div class="accordion" id="incomingOptionsAccordion">
			<div class="accordion-item">
				<h2 class="accordion-header" id="incomingAccHeadProperties" style="position:relative;">
					<button class="accordion-button collapsed" id="incomingAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#incomingAccProperties" aria-expanded="false" aria-controls="incomingAccProperties">
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
</div>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="incomingHelpOffcanvas"
     aria-labelledby="incomingHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
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
			var $flag = $('<span class="fi" style="font-size:1.3em;vertical-align:middle;flex-shrink:0;"></span>');
			$sel.after($flag);
			function updateFlag() {
				var iso = ($sel.val() || '').toUpperCase();
				if (VALID_ISO.has(iso)) {
					$flag.attr('class', 'fi fi-' + iso.toLowerCase()).css('display', 'inline-block');
				} else if (iso === 'EX') {
					$flag.attr('class', 'bi bi-globe').css('display', 'inline-block');
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
    	checkEachLine(editorProperties, 'incomingAccPropertiesBtn');
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
  		checkEachLine(editorProperties, 'incomingAccPropertiesBtn');
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
	checkEachLine(editorProperties, 'incomingAccPropertiesBtn');

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

    $('#id').bind('keypress', function (event) {
        var regex = new RegExp("^[a-zA-Z0-9._]+$");
        var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
        if (!regex.test(key)) {
            event.preventDefault();
            return false;
        }
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
