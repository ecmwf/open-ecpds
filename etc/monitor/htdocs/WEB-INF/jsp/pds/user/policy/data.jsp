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
				<div class="card assoc-card mt-2">
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
				              <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}" class="text-decoration-none text-dark">${destination.name}</a>
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
							<div class="accordion" id="policyViewOptionsAccordion">
								<div class="accordion-item">
									<h2 class="accordion-header" id="policyViewAccHeadProperties">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyViewAccProperties" aria-expanded="false" aria-controls="policyViewAccProperties">
											Properties
										</button>
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
								<div class="accordion-item">
									<h2 class="accordion-header" id="policyViewAccHeadHelp">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyViewAccHelp" aria-expanded="false" aria-controls="policyViewAccHelp">
											Help
										</button>
									</h2>
									<div id="policyViewAccHelp" class="accordion-collapse collapse" aria-labelledby="policyViewAccHeadHelp" data-bs-parent="#policyViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div id="policyViewHelpContent" class="scrollable-tab"></div>
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
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});
		
		// Get the completions from the bean!      		
    	var completions = [
    		${policy.completions}
    	];
		
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#policyViewHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties);
        
		// Add a click event listener to the properties editor
    	editorProperties.addEventListener("changeSelection", function (event) {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	});
		
		makeResizable(editorProperties);

		document.getElementById('policyViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		var policyViewHelpBtn = document.querySelector('button[data-bs-target="#policyViewAccHelp"]');
		if (policyViewHelpBtn) {
			policyViewHelpBtn.addEventListener('click', function() {
				setTimeout(function() {
					if (!document.getElementById('policyViewAccHelp').classList.contains('show')) return;
					var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
					line = line.trim();
					if (line && !line.startsWith('#') && !line.startsWith('//')) {
						var eqIdx = line.indexOf('=');
						var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
						if (paramName) scrollHelpToParam('policyViewHelpContent', paramName);
					}
				}, 400);
			});
		}

		window.addEventListener('resize', function() {
			editorProperties.resize(true);
		});
	</script>

</c:if>
