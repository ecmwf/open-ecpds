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
.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px lightgray;
	border-radius: 4px;
	padding: 8px;
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
</style>

	<table border="0">
		<tr>
			<td valign="top"><br>
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
				<div class="row g-2 mt-1" style="max-width:480px">

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
				                  <a href="<bean:message key="policy.basepath"/>/${policy.id}" title="${policy.comment}" class="text-decoration-none text-dark">${policy.id}</a>
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
				                  <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}" class="text-decoration-none text-dark">${destination.name}</a>
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
							<div class="accordion" id="incomingViewOptionsAccordion">
								<div class="accordion-item">
									<h2 class="accordion-header" id="incomingViewAccHeadProperties">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingViewAccProperties" aria-expanded="false" aria-controls="incomingViewAccProperties">
											Properties
										</button>
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
								<div class="accordion-item">
									<h2 class="accordion-header" id="incomingViewAccHeadHelp">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#incomingViewAccHelp" aria-expanded="false" aria-controls="incomingViewAccHelp">
											Help
										</button>
									</h2>
									<div id="incomingViewAccHelp" class="accordion-collapse collapse" aria-labelledby="incomingViewAccHeadHelp" data-bs-parent="#incomingViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div id="incomingViewHelpContent" class="scrollable-tab"></div>
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

	<script>
		var editorProperties = getEditorProperties(true, false, "userData", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});
		
		// Get the completions from the bean!      		
    	var completions = [
    		${incoming.completions}
    	];
		
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#incomingViewHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties);
        
		// Add a click event listener to the properties editor
    	editorProperties.addEventListener("changeSelection", function (event) {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	});

    	var editorAuthorizedSSHKeys = getEditorProperties(true, false, "authorizedSSHKeys", "text");
		editorAuthorizedSSHKeys.setOptions({minLines: 10, maxLines: 20});

		document.getElementById('incomingViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		document.getElementById('incomingViewAccSSHKeys').addEventListener('shown.bs.collapse', function() {
			editorAuthorizedSSHKeys.resize(true);
		});
		var incomingViewHelpBtn = document.querySelector('button[data-bs-target="#incomingViewAccHelp"]');
		if (incomingViewHelpBtn) {
			incomingViewHelpBtn.addEventListener('click', function() {
				setTimeout(function() {
					if (!document.getElementById('incomingViewAccHelp').classList.contains('show')) return;
					var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
					line = line.trim();
					if (line && !line.startsWith('#') && !line.startsWith('//')) {
						var eqIdx = line.indexOf('=');
						var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
						if (paramName) scrollHelpToParam('incomingViewHelpContent', paramName);
					}
				}, 400);
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

