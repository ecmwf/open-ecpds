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
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.4rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
	</style>

	<div class="row g-3">
		<div class="col-lg-6">
			<div class="card">
				<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
					<i class="bi bi-person text-primary"></i>
					<span class="fw-semibold">Web User: <c:out value="${user.uid}" /></span>
					<auth:if basePathKey="user.basepath" paths="/edit/insert_form">
					<auth:then>
					<div class="d-flex gap-1 ms-auto flex-shrink-0">
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
				<div class="card-body">
					<div class="d-flex flex-column gap-2">
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><span class="text-muted small fw-semibold text-uppercase">Web Login</span></div>
							<div class="col-sm-8"><c:out value="${user.uid}" /></div>
						</div>
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><span class="text-muted small fw-semibold text-uppercase">Comment</span></div>
							<div class="col-sm-8"><c:out value="${user.commonName}" /></div>
						</div>
						<div class="row g-2 align-items-center">
							<div class="col-sm-4"><span class="text-muted small fw-semibold text-uppercase">Enabled</span></div>
							<div class="col-sm-8">
								<c:if test="${user.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
								<c:if test="${!user.active}"><i class="bi bi-x-circle-fill text-secondary" title="No"></i></c:if>
							</div>
						</div>
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
