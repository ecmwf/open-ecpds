<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/user/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="row g-3">
		<div class="col-lg-6">
			<div class="card">
				<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
					<i class="bi bi-person text-primary"></i>
					<span class="fw-semibold">Web User: <c:out value="${user.uid}" /></span>
					<auth:if basePathKey="user.basepath" paths="/edit/insert_form">
					<auth:then>
					<div class="d-flex gap-1 ms-auto flex-shrink-0 align-items-center">
						<a href='<bean:message key="user.basepath"/>'
						   class="btn btn-sm btn-outline-secondary" title="Back to Web Users list"><i class="bi bi-arrow-left"></i></a>
						<span class="border-start mx-1" style="height:1.4em;"></span>
						<a href='<bean:message key="user.basepath"/>/edit/insert_form'
						   class="btn btn-sm btn-outline-success" title="Create new web user"><i class="bi bi-plus-circle"></i></a>
						<c:if test="${not empty user.id}">
						<a href='<bean:message key="user.basepath"/>/edit/update_form/${user.id}'
						   class="btn btn-sm btn-outline-primary" title="Edit this web user"><i class="bi bi-pencil"></i></a>
						<a href='<bean:message key="user.basepath"/>/edit/delete_form/${user.id}'
						   class="btn btn-sm btn-outline-danger" title="Delete this web user"><i class="bi bi-trash"></i></a>
						</c:if>
					</div>
					</auth:then>
					</auth:if>
				</div>
				<div class="card-body py-0">
					<div class="field-grid">
						<div class="field-row"><div class="field-label">Web Login</div><div class="field-value"><span class="val-code"><c:out value="${user.uid}" /></span></div></div>
						<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty user.commonName}"><c:out value="${user.commonName}" /></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
						<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${user.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
					</div>
				</div>
			</div>
		</div>
		<div class="col-lg-6">
			<div class="card assoc-card">
			  <div class="card-header">
			    <i class="bi bi-folder text-secondary"></i>
			    <strong>Associated Web Categories</strong>
			  </div>
			  <div class="card-body p-2">
			    <c:choose>
			      <c:when test="${empty user.categories}">
			        <p class="text-muted small mb-0"><em>No web categories assigned.</em></p>
			      </c:when>
			      <c:otherwise>
			        <div class="d-flex flex-wrap">
			          <c:forEach var="category" items="${user.categories}">
			            <span class="assoc-chip">
			              <a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}" class="text-decoration-none text-body">${category.name}</a>
			            </span>
			          </c:forEach>
			        </div>
			      </c:otherwise>
			    </c:choose>
			  </div>
			</div>
		</div>

		<%-- Options card: full-width below both columns --%>
		<div class="col-12">
			<div class="card">
				<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
					<i class="bi bi-sliders text-warning"></i>
					<span class="fw-semibold">Options</span>
				</div>
				<div class="card-body p-2">
					<div class="accordion" id="webUserViewOptionsAccordion">
					<div class="accordion-item">
						<h2 class="accordion-header" id="webUserViewAccHeadProperties" style="position:relative;">
							<button class="accordion-button collapsed" id="webUserViewAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#webUserViewAccProperties" aria-expanded="false" aria-controls="webUserViewAccProperties">
								Properties
							</button>
							<span role="button" tabindex="0" class="acc-help-btn" id="webUserViewPropsHelpBtn"
								onclick="openWebUserViewHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openWebUserViewHelp();" title="Open properties reference">
								<i class="bi bi-question-circle"></i>
							</span>
						</h2>
						<div id="webUserViewAccProperties" class="accordion-collapse collapse" aria-labelledby="webUserViewAccHeadProperties" data-bs-parent="#webUserViewOptionsAccordion">
						<div class="accordion-body p-2">
							<pre id="properties" class="ace-panel"><c:out value="${user.userData}" /></pre>
						</div>
						</div>
					</div>
					</div>
				</div>
			</div>
		</div>

</div>

	<%-- Help offcanvas panel --%>
	<div class="offcanvas offcanvas-end" tabindex="-1" id="webUserViewHelpOffcanvas"
	     aria-labelledby="webUserViewHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
		<div class="offcanvas-header border-bottom py-2 px-3">
			<h6 class="offcanvas-title mb-0 fw-semibold" id="webUserViewHelpOffcanvasLabel">
				<i class="bi bi-book me-2 text-warning"></i>Properties Reference
			</h6>
			<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
		</div>
		<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
			<div id="webUserViewHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
			<div id="webUserViewHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
		</div>
	</div>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});

		var completions = [
			${user.completions}
		];

		$(document).ready(function() {
			$('#webUserViewHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Web User'));
			var navEl = document.querySelector('#webUserViewHelpContent .help-nav');
			if (navEl) document.getElementById('webUserViewHelpNav').appendChild(navEl);
		});

		// Call the function to process each line
		checkEachLine(editorProperties, 'webUserViewAccPropertiesBtn');

		function _scrollWebUserViewHelpToCursor() {
			var row = editorProperties.selection.getCursor().row;
			var line = editorProperties.session.getLine(row) || '';
			line = line.trim();
			if (line && !line.startsWith('#') && !line.startsWith('//')) {
				var eqIdx = line.indexOf('=');
				var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
				if (paramName) scrollHelpToParam('webUserViewHelpContent', paramName);
			}
		}

		editorProperties.addEventListener("changeSelection", function (event) {
			checkEachLine(editorProperties, 'webUserViewAccPropertiesBtn');
			var _oc = document.getElementById('webUserViewHelpOffcanvas');
			if (_oc && _oc.classList.contains('show')) _scrollWebUserViewHelpToCursor();
		});

		document.getElementById('webUserViewAccProperties').addEventListener('shown.bs.collapse', function() {
			setTimeout(function() { editorProperties.resize(true); }, 50);
		});

		window.openWebUserViewHelp = function() {
			var el = document.getElementById('webUserViewHelpOffcanvas');
			if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
		};

		makeResizable(editorProperties);
	</script>
</c:if>
