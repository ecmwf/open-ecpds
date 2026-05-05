<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/incoming/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

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
	min-width: 400px;
}
table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
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

	<div class="form-info-banner" style="margin-left:0">
		<i class="bi bi-person-fill-gear text-primary flex-shrink-0"></i>
		Data User: <strong><c:out value="${incoming.id}"/></strong>
	</div>

	<table border="0">
		<tr>
			<td valign="top">
				<table class="fields">
					<tr>
						<th>Data Login</th>
						<td>${incoming.id}</td>
					</tr>
					<tr>
						<th>Comment</th>
						<td>${incoming.comment}</td>
					</tr>
					<tr>
						<th>Country</th>
						<td><span class="fi fi-${fn:toLowerCase(incoming.country.iso)} me-1" title="${incoming.country.name}" style="font-size:1.1em;vertical-align:middle"></span>${incoming.country.name}</td>
					</tr>
					<tr>
						<th>Enabled</th>
						<td><c:if test="${incoming.active}">yes</c:if> <c:if
								test="${!incoming.active}">
								<font color="red">no</font>
							</c:if></td>
					</tr>
					
					<tr>
						<td>&nbsp;</td>
					</tr>

					<tr>
						<th>TOTP authentication</th>
						<td><c:if test="${incoming.isSynchronized}">yes</c:if> <c:if
								test="${!incoming.isSynchronized}">no</c:if></td>
					</tr>

				</table></td>
			<td width="25"></td>
			<td valign="top">
				<div class="row g-2" style="max-width:480px">

				  <div class="col-12">
				    <div class="card assoc-card">
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

				  <div class="col-12">
				    <div class="card assoc-card">
				      <div class="card-header">
				        <i class="bi bi-geo-alt text-secondary"></i>
				        <strong>Destinations</strong>
				      </div>
				      <div class="card-body p-2">
				        <c:choose>
				          <c:when test="${empty incoming.associatedDestinations}">
				            <p class="text-muted small mb-0"><em>No destinations assigned.</em></p>
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

				  <div class="col-12">
				    <div class="card assoc-card">
				      <div class="card-header">
				        <i class="bi bi-gear text-secondary"></i>
				        <strong>Permissions</strong>
				      </div>
				      <div class="card-body p-2">
				        <c:choose>
				          <c:when test="${empty incoming.associatedOperations}">
				            <p class="text-muted small mb-0"><em>No permissions assigned.</em></p>
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
				        <i class="bi bi-plug text-secondary"></i>
				        <strong>Current Sessions</strong>
				      </div>
				      <div class="card-body p-2">
				        <c:choose>
				          <c:when test="${empty incoming.incomingConnections}">
				            <p class="text-muted small mb-0"><em>No active sessions.</em></p>
				          </c:when>
				          <c:otherwise>
				            <div class="d-flex flex-wrap">
				              <c:forEach var="incomingSession" items="${incoming.incomingConnections}">
				                <span class="assoc-chip">
				                  <span title="Mover: ${incomingSession.dataMoverName} | Duration: ${incomingSession.formatedDuration}">${incomingSession.protocol} · ${incomingSession.remoteIpAddress}</span>
				                </span>
				              </c:forEach>
				            </div>
				          </c:otherwise>
				        </c:choose>
				      </div>
				    </div>
				  </div>

				</div>
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<table class="fields">
					<tr>
						<th>Options</th>
						<td colspan="2">
							<div class="accordion" id="incomingViewOptionsAccordion" style="min-width:860px;max-width:860px">
								<div class="accordion-item">
									<h2 class="accordion-header" id="incomingViewAccHeadProperties" style="position:relative;">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingViewAccProperties" aria-expanded="false" aria-controls="incomingViewAccProperties">
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
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="incomingViewHelpOffcanvas"
     aria-labelledby="incomingViewHelpOffcanvasLabel" style="width:min(480px,42vw);">
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
    	checkEachLine(editorProperties);
        
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
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
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

