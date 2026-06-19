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

	<style>
#properties {
	width: 550px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}
	</style>

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
	</div>

	<div style="display:none">
		<pre id="properties"><c:out value="${user.userData}" /></pre>
		<textarea id="properties" name="properties" style="display: none;"></textarea>
	</div>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		makeResizable(editorProperties);
	</script>
</c:if>
