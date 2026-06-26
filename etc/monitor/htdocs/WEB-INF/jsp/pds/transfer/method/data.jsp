<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/method/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-plug text-primary"></i>
			<span class="fw-semibold">Transfer Method: <c:out value="${method.name}"/></span>
			<div class="d-flex gap-1 ms-auto flex-shrink-0">
				<auth:if basePathKey="method.basepath" paths="/edit/insert_form">
				<auth:then>
				<a href='<bean:message key="method.basepath"/>/edit/insert_form'
				   class="btn btn-sm btn-outline-success" title="Create new transfer method"><i class="bi bi-plus-circle"></i></a>
				<c:if test="${not empty method.id}">
				<a href='<bean:message key="method.basepath"/>/edit/update_form/${method.id}'
				   class="btn btn-sm btn-outline-primary" title="Edit this transfer method"><i class="bi bi-pencil"></i></a>
				<a href='<bean:message key="method.basepath"/>/edit/delete_form/${method.id}'
				   class="btn btn-sm btn-outline-danger" title="Delete this transfer method"><i class="bi bi-trash"></i></a>
				</c:if>
				</auth:then>
				</auth:if>
				<span class="border-start mx-1" style="height:1.5rem;"></span>
				<c:choose>
					<c:when test="${not empty moduleGuide}">
						<button class="btn btn-sm btn-outline-info" type="button"
						        data-bs-toggle="offcanvas" data-bs-target="#moduleGuideOffcanvas"
						        title="Configuration Guide"><i class="bi bi-book"></i></button>
					</c:when>
					<c:otherwise>
						<button class="btn btn-sm btn-outline-secondary disabled" type="button"
						        title="No configuration guide available" disabled><i class="bi bi-book"></i></button>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Name</div><div class="field-value"><span class="val-code"><c:out value="${method.name}" /></span></div></div>
				<div class="field-row"><div class="field-label">Value</div><div class="field-value"><span class="val-code"><c:out value="${method.value}" /></span></div></div>
				<div class="field-row"><div class="field-label">Transfer Module</div><div class="field-value"><c:choose><c:when test="${not empty method.ecTransModule and not empty method.ecTransModule.id}"><a href="<bean:message key="module.basepath"/>/${method.ecTransModule.id}">${method.ecTransModule.name}</a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty method.comment}"><c:out value="${method.comment}" /></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Restrict</div><div class="field-value"><c:choose><c:when test="${method.restrict}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Resolve</div><div class="field-value"><c:choose><c:when test="${method.resolve}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${method.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			</div>
		</div>
	</div>

	<c:if test="${not empty moduleGuide}">
		<jsp:include page="${moduleGuide}"/>
	</c:if>

</c:if>

