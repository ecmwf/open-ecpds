<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/policy/warning.jsp" />
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
.acc-help-btn {
    position: absolute; top: 50%; right: 3rem;
    transform: translateY(-50%);
    color: var(--bs-secondary-color); font-size: 0.9rem; line-height: 1;
    cursor: pointer; z-index: 10;
    transition: color 0.15s;
}
.acc-help-btn:hover { color: var(--bs-primary); }
.acc-help-btn.acc-help-active { color: var(--bs-primary); }
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

	<div class="form-info-banner" style="margin-left:0">
		<i class="bi bi-shield-check text-primary flex-shrink-0"></i>
		Data Policy: <strong><c:out value="${policy.id}"/></strong>
	</div>

	<table border="0">
		<tr>
			<td valign="top">
				<table class="fields">
					<tr>
						<th>Name</th>
						<td>${policy.id}</td>
					</tr>
					<tr>
						<th>Comment</th>
						<td>${policy.comment}</td>
					</tr>
					<tr>
						<th>Enabled</th>
						<td><c:if test="${policy.active}">yes</c:if> <c:if
								test="${!policy.active}">
								<font color="red">no</font>
							</c:if></td>
					</tr>
				</table></td>
			<td width="25"></td>
			<td valign="top">
				<div class="card assoc-card">
				  <div class="card-header">
				    <i class="bi bi-geo-alt text-secondary"></i>
				    <strong>Associated Destinations</strong>
				  </div>
				  <div class="card-body p-2">
				    <c:choose>
				      <c:when test="${empty policy.associatedDestinations}">
				        <p class="text-muted small mb-0"><em>No destinations assigned.</em></p>
				      </c:when>
				      <c:otherwise>
				        <div class="d-flex flex-wrap">
				          <c:forEach var="destination" items="${policy.associatedDestinations}">
				            <span class="assoc-chip">
				              <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}" class="text-decoration-none text-body">${destination.name}</a>
				            </span>
				          </c:forEach>
				        </div>
				      </c:otherwise>
				    </c:choose>
				  </div>
				</div></td>
		</tr>
		<tr>
			<td colspan="3">
				<table class="fields">
					<tr>
						<th>Options</th>
						<td colspan="2">
							<div class="accordion" id="policyViewOptionsAccordion" style="min-width:860px;max-width:860px">
								<div class="accordion-item">
									<h2 class="accordion-header" id="policyViewAccHeadProperties" style="position:relative;">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyViewAccProperties" aria-expanded="false" aria-controls="policyViewAccProperties">
											Properties
										</button>
										<span role="button" tabindex="0" class="acc-help-btn" id="policyViewPropsHelpBtn"
											onclick="openPolicyViewHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openPolicyViewHelp();" title="Open properties reference">
											<i class="bi bi-question-circle"></i>
										</span>
									</h2>
									<div id="policyViewAccProperties" class="accordion-collapse collapse" aria-labelledby="policyViewAccHeadProperties" data-bs-parent="#policyViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div class="ace-panel">
												<pre id="properties"><c:out value="${policy.properties}" /></pre>
												<textarea id="properties" name="properties" style="display: none;"></textarea>
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
<div class="offcanvas offcanvas-end" tabindex="-1" id="policyViewHelpOffcanvas"
     aria-labelledby="policyViewHelpOffcanvasLabel" style="width:min(480px,42vw);">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="policyViewHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="policyViewHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="policyViewHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});
		
		// Get the completions from the bean!      		
    	var completions = [
    		${policy.completions}
    	];
		
    	$(document).ready(function() {
    		$('#policyViewHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    		var navEl = document.querySelector('#policyViewHelpContent .help-nav');
    		if (navEl) document.getElementById('policyViewHelpNav').appendChild(navEl);
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties);
        
		function _scrollPolicyViewHelpToCursor() {
			var row = editorProperties.selection.getCursor().row;
			var line = editorProperties.session.getLine(row) || '';
			line = line.trim();
			if (line && !line.startsWith('#') && !line.startsWith('//')) {
				var eqIdx = line.indexOf('=');
				var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
				if (paramName) scrollHelpToParam('policyViewHelpContent', paramName);
			}
		}

    	editorProperties.addEventListener("changeSelection", function (event) {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
			var _oc = document.getElementById('policyViewHelpOffcanvas');
			if (_oc && _oc.classList.contains('show')) _scrollPolicyViewHelpToCursor();
    	});
		
		makeResizable(editorProperties);

		document.getElementById('policyViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		window.openPolicyViewHelp = function() {
			var el = document.getElementById('policyViewHelpOffcanvas');
			if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
		};
		var _policyViewOffcanvasEl = document.getElementById('policyViewHelpOffcanvas');
		if (_policyViewOffcanvasEl) {
			_policyViewOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
				var btn = document.getElementById('policyViewPropsHelpBtn');
				if (btn) btn.classList.add('acc-help-active');
			});
			_policyViewOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
				_scrollPolicyViewHelpToCursor();
			});
			_policyViewOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
				var btn = document.getElementById('policyViewPropsHelpBtn');
				if (btn) btn.classList.remove('acc-help-active');
			});
		}

		window.addEventListener('resize', function() {
			editorProperties.resize(true);
		});
	</script>

</c:if>
