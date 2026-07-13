<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>
<script>
	function validate(path, message) {
	    confirmationDialog({
	        title: "Please Confirm",
	        message: message,
	        onConfirm: function () { window.location = path; }
	    });
	}
</script>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/incoming/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	
	<div class="row g-3">
		<div class="col-lg-6">
			<div class="card">
				<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
					<i class="bi bi-person-fill-gear text-primary"></i>
					<span class="fw-semibold">Data User: <c:out value="${incoming.id}" /></span>
					<auth:if basePathKey="incoming.basepath" paths="/edit/insert_form">
					<auth:then>
					<div class="d-flex gap-1 ms-auto flex-shrink-0 align-items-center">
						<a href='<bean:message key="incoming.basepath"/>'
						   class="btn btn-sm btn-outline-secondary" title="Back to Data Users list"><i class="bi bi-arrow-left"></i></a>
						<span class="border-start mx-1" style="height:1.4em;"></span>
						<a href='<bean:message key="incoming.basepath"/>/edit/insert_form'
						   class="btn btn-sm btn-outline-success" title="Create new data user"><i class="bi bi-plus-circle"></i></a>
						<c:if test="${not empty incoming.id}">
						<a href='<bean:message key="incoming.basepath"/>/edit/update_form/${incoming.id}'
						   class="btn btn-sm btn-outline-primary" title="Edit this data user"><i class="bi bi-pencil"></i></a>
						<a href='<bean:message key="incoming.basepath"/>/edit/delete_form/${incoming.id}'
						   class="btn btn-sm btn-outline-danger" title="Delete this data user"><i class="bi bi-trash"></i></a>
						<span class="border-start mx-1" style="height:1.4em;"></span>
						<a href="/do/user/incoming/portaltraffic/${incoming.id}"
						   class="btn btn-sm btn-outline-secondary" title="Portal Traffic"><i class="bi bi-graph-up-arrow"></i></a>
						</c:if>
					</div>
					</auth:then>
					</auth:if>
				</div>
				<div class="card-body py-0">
					<div class="field-grid">
						<div class="field-row"><div class="field-label">Data Login</div><div class="field-value"><span class="val-code">${incoming.id}</span></div></div>
						<div class="field-row"><div class="field-label">Portal Service</div><div class="field-value"><c:choose>
							<c:when test="${incoming.portalService == 'open-access'}"><span class="badge rounded-pill bg-warning-subtle text-warning-emphasis border border-warning-subtle fw-normal"><i class="bi bi-unlock-fill me-1"></i>Open Access</span></c:when>
							<c:when test="${incoming.portalService == 'self-service'}"><span class="badge rounded-pill bg-info-subtle text-info-emphasis border border-info-subtle fw-normal"><i class="bi bi-person-plus-fill me-1"></i>Self-Service</span></c:when>
							<c:otherwise><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-normal"><i class="bi bi-lock-fill me-1"></i>Standard Login</span></c:otherwise>
						</c:choose></div></div>
						<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty incoming.comment}">${incoming.comment}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
						<div class="field-row"><div class="field-label">Country</div><div class="field-value"><c:choose><c:when test="${not empty incoming.country and not empty incoming.country.iso and not empty incoming.country.name}"><span class="d-inline-flex align-items-center gap-1"><c:choose><c:when test="${incoming.country.iso == 'ex'}"><i class="bi bi-globe" title="${incoming.country.name}" style="font-size:1.1em;"></i></c:when><c:when test="${fn:length(incoming.country.iso) == 2}"><span class="fi fi-${fn:toLowerCase(incoming.country.iso)}" title="${incoming.country.name}" style="font-size:1.1em;border-radius:2px;"></span></c:when></c:choose><span>${incoming.country.name}</span></span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
						<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${incoming.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
						<c:if test="${incoming.portalService == 'standard-login' or empty incoming.portalService}">
						<div class="field-row"><div class="field-label">TOTP authentication</div><div class="field-value"><c:choose><c:when test="${incoming.isSynchronized}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
		<div class="col-lg-6">
			<div class="row g-2">

			  <div class="col-sm-6">
			    <div class="card assoc-card h-100">
			      <div class="card-header">
			        <i class="bi bi-shield-check text-secondary"></i>
			        <strong>Data Policies</strong>
			      </div>
			      <div class="card-body p-2">
			        <c:choose>
			          <c:when test="${empty incoming.associatedIncomingPolicies}">
			            <p class="text-muted small mb-0"><em>No data policies assigned.</em></p>
			          </c:when>
			          <c:otherwise>
			            <div class="d-flex flex-wrap">
			              <c:forEach var="policy" items="${incoming.associatedIncomingPolicies}">
			                <span class="assoc-chip">
			                  <a href="<bean:message key="policy.basepath"/>/${policy.id}" title="${policy.comment}" class="text-decoration-none text-body">${policy.id}</a>
			                </span>
			              </c:forEach>
			            </div>
			          </c:otherwise>
			        </c:choose>
			      </div>
			    </div>
			  </div>

			  <div class="col-sm-6">
			    <div class="card assoc-card h-100">
			      <div class="card-header">
			        <i class="bi bi-gear text-secondary"></i>
			        <strong>Permissions</strong>
			      </div>
			      <div class="card-body p-2">
			        <c:choose>
			          <c:when test="${empty incoming.associatedOperations}">
			            <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-0 small" role="alert">
			              <i class="bi bi-exclamation-triangle-fill flex-shrink-0" style="margin-top:0.1em"></i>
			              <span><strong>No permissions assigned.</strong> All login attempts for this user will be denied until at least one permission is added.</span>
			            </div>
			          </c:when>
			          <c:otherwise>
			            <div class="d-flex flex-wrap">
			              <c:forEach var="operation" items="${incoming.associatedOperations}">
			                <span class="assoc-chip">
			                  <span title="${operation.comment}">${operation.name}</span>
			                </span>
			              </c:forEach>
			            </div>
			          </c:otherwise>
			        </c:choose>
			      </div>
			    </div>
			  </div>

			  <div class="col-12">
			    <div class="card assoc-card">
			      <div class="card-header">
			        <i class="bi bi-geo-alt text-secondary"></i>
			        <strong>Destinations</strong>
			      </div>
			      <div class="card-body p-2">
			        <%-- Check whether any associated policy provides at least one destination --%>
			        <c:set var="policyHasDestinations" value="false"/>
			        <c:forEach var="p" items="${incoming.associatedIncomingPolicies}">
			          <c:if test="${not empty p.associatedDestinations}">
			            <c:set var="policyHasDestinations" value="true"/>
			          </c:if>
			        </c:forEach>
			        <c:choose>
			          <c:when test="${empty incoming.associatedDestinations and not policyHasDestinations}">
			            <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-0 small" role="alert">
			              <i class="bi bi-exclamation-triangle-fill flex-shrink-0" style="margin-top:0.1em"></i>
			              <span><strong>No destinations assigned.</strong> All login attempts for this user will be denied until at least one destination is added here or via an associated Data Policy.</span>
			            </div>
			          </c:when>
			          <c:when test="${empty incoming.associatedDestinations and policyHasDestinations}">
			            <div class="alert alert-info d-flex align-items-start gap-2 py-2 px-3 mb-0 small" role="alert">
			              <i class="bi bi-info-circle-fill flex-shrink-0 mt-1"></i>
			              <span>No destinations assigned directly, but access is granted through the associated Data <c:choose><c:when test="${fn:length(incoming.associatedIncomingPolicies) gt 1}">Policies</c:when><c:otherwise>Policy</c:otherwise></c:choose>.</span>
			            </div>
			          </c:when>
			          <c:otherwise>
			            <div class="d-flex flex-wrap">
			              <c:forEach var="destination" items="${incoming.associatedDestinations}">
			                <span class="assoc-chip">
			                  <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}" class="text-decoration-none text-body">${destination.name}</a>
			                </span>
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

<div class="mt-3">
	<div class="card assoc-card">
		<c:set var="sessionCount" value="${fn:length(incoming.incomingConnections)}"/>
		<div class="card-header d-flex align-items-center gap-2">
			<i class="bi bi-plug text-secondary"></i>
			<strong>Current Sessions</strong>
			<span class="badge rounded-pill ${sessionCount > 0 ? 'bg-success' : 'bg-secondary'}" title="${sessionCount} active session${sessionCount != 1 ? 's' : ''}">${sessionCount}</span>
			<auth:if basePathKey="incoming.basepath" paths="/edit/update">
			<auth:then>
			<c:if test="${not empty incoming.incomingConnections}">
			<button type="button" class="btn btn-sm btn-outline-danger ms-auto"
			        onclick="confirmCloseAll('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incoming.id}"/>/closeAllSessions/all', ${sessionCount})">
			  <i class="bi bi-x-circle"></i> Close All
			</button>
			</c:if>
			</auth:then>
			</auth:if>
		</div>
		<c:choose>
			<c:when test="${empty incoming.incomingConnections}">
				<div class="card-body p-2">
					<p class="text-muted small mb-0"><em>No active sessions.</em></p>
				</div>
			</c:when>
			<c:otherwise>
				<div class="table-responsive">
					<table id="incomingSessionsTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
						<thead class="table-warning">
							<tr>
								<th>Session ID</th>
								<th>Protocol</th>
								<th>Remote IP</th>
								<th>Data Mover</th>
								<th>Start Time (UTC)</th>
								<th>Duration</th>
								<auth:if basePathKey="incoming.basepath" paths="/edit/update">
								<auth:then><th class="text-center no-sort">Action</th></auth:then>
								</auth:if>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="s" items="${incoming.incomingConnections}">
								<tr>
									<td><code>${s.id}</code></td>
									<td><span class="badge bg-secondary-subtle text-secondary-emphasis border">${s.protocol}</span></td>
									<td>${s.remoteIpAddress}</td>
									<td>${s.dataMoverName}</td>
									<td data-order="${s.startTime}"><span class="ic-ts" data-ts="${s.startTime}">${s.startTime}</span></td>
									<td>${s.formatedDuration}</td>
									<auth:if basePathKey="incoming.basepath" paths="/edit/update">
									<auth:then>
									<td class="text-center">
										<a href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incoming.id}"/>/closeSession/<c:out value="${s.id}"/>','<bean:message key="ecpds.incoming.disconnectOperation.warning" arg0="${s.login}" arg1="${s.dataMoverName}"/>')"
										   class="btn btn-sm btn-outline-danger py-0 px-1" title="Disconnect session">
											<i class="bi bi-x-lg"></i>
										</a>
									</td>
									</auth:then>
									</auth:if>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
				<script>
				$(function() {
					document.querySelectorAll('.ic-ts').forEach(function(el) {
						var ts = parseInt(el.getAttribute('data-ts'), 10);
						if (ts) el.textContent = new Date(ts).toISOString().replace('T', ' ').substring(0, 19);
					});
					if ($.fn.DataTable) {
						$('#incomingSessionsTable').DataTable({
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

<div class="mt-3">
	<div class="card">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-sliders text-primary"></i>
			<span class="fw-semibold">Options</span>
		</div>
		<div class="card-body p-2">
			<div class="accordion" id="incomingViewOptionsAccordion">
				<div class="accordion-item">
					<h2 class="accordion-header" id="incomingViewAccHeadProperties" style="position:relative;">
						<button class="accordion-button collapsed" id="incomingViewAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#incomingViewAccProperties" aria-expanded="false" aria-controls="incomingViewAccProperties">
							Properties
						</button>
						<span role="button" tabindex="0" class="acc-help-btn" id="incomingViewPropsHelpBtn"
							onclick="openIncomingViewHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openIncomingViewHelp();" title="Open properties reference">
							<i class="bi bi-question-circle"></i>
						</span>
					</h2>
					<div id="incomingViewAccProperties" class="accordion-collapse collapse" aria-labelledby="incomingViewAccHeadProperties" data-bs-parent="#incomingViewOptionsAccordion">
						<div class="accordion-body p-2">
							<div class="ace-panel">
								<pre id="userData"><c:out value="${incoming.properties}" /></pre>
								<textarea id="userData" name="userData" style="display: none;"></textarea>
							</div>
						</div>
					</div>
				</div>
				<div class="accordion-item">
					<h2 class="accordion-header" id="incomingViewAccHeadSSHKeys">
						<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingViewAccSSHKeys" aria-expanded="false" aria-controls="incomingViewAccSSHKeys">
							Authorized SSH Keys
						</button>
					</h2>
					<div id="incomingViewAccSSHKeys" class="accordion-collapse collapse" aria-labelledby="incomingViewAccHeadSSHKeys" data-bs-parent="#incomingViewOptionsAccordion">
						<div class="accordion-body p-2">
							<div class="ace-panel">
								<pre id="authorizedSSHKeys"><c:out value="${incoming.authorizedSSHKeys}" /></pre>
								<textarea id="authorizedSSHKeys" name="authorizedSSHKeys" style="display: none;"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="incomingViewHelpOffcanvas"
     aria-labelledby="incomingViewHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="incomingViewHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="incomingViewHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="incomingViewHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

	<script>
		var editorProperties = getEditorProperties(true, false, "userData", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});

		// Get the completions from the bean!      		
    	var completions = [
    		${incoming.completions}
    	];

    	$(document).ready(function() {
    		$('#incomingViewHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    		var navEl = document.querySelector('#incomingViewHelpContent .help-nav');
    		if (navEl) document.getElementById('incomingViewHelpNav').appendChild(navEl);
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties, 'incomingViewAccPropertiesBtn');

		function _scrollIncomingViewHelpToCursor() {
			var row = editorProperties.selection.getCursor().row;
			var line = editorProperties.session.getLine(row) || '';
			line = line.trim();
			if (line && !line.startsWith('#') && !line.startsWith('//')) {
				var eqIdx = line.indexOf('=');
				var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
				if (paramName) scrollHelpToParam('incomingViewHelpContent', paramName);
			}
		}

    	editorProperties.addEventListener("changeSelection", function (event) {
    		checkEachLine(editorProperties, 'incomingViewAccPropertiesBtn');
			var _oc = document.getElementById('incomingViewHelpOffcanvas');
			if (_oc && _oc.classList.contains('show')) _scrollIncomingViewHelpToCursor();
    	});

    	var editorAuthorizedSSHKeys = getEditorProperties(true, false, "authorizedSSHKeys", "text");
		editorAuthorizedSSHKeys.setOptions({minLines: 10, maxLines: 20});

		document.getElementById('incomingViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		document.getElementById('incomingViewAccSSHKeys').addEventListener('shown.bs.collapse', function() {
			editorAuthorizedSSHKeys.resize(true);
		});
		window.openIncomingViewHelp = function() {
			var el = document.getElementById('incomingViewHelpOffcanvas');
			if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
		};
		var _incomingViewOffcanvasEl = document.getElementById('incomingViewHelpOffcanvas');
		if (_incomingViewOffcanvasEl) {
			_incomingViewOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
				var btn = document.getElementById('incomingViewPropsHelpBtn');
				if (btn) btn.classList.add('acc-help-active');
			});
			_incomingViewOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
				_scrollIncomingViewHelpToCursor();
			});
			_incomingViewOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
				var btn = document.getElementById('incomingViewPropsHelpBtn');
				if (btn) btn.classList.remove('acc-help-active');
			});
		}

		makeResizable(editorProperties);
		makeResizable(editorAuthorizedSSHKeys);

		window.addEventListener('resize', function() {
			editorProperties.resize(true);
			editorAuthorizedSSHKeys.resize(true);
		});
	</script>
</c:if>
