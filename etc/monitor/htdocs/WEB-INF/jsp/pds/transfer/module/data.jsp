<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/module/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-puzzle text-primary"></i>
			<span class="fw-semibold">Transfer Module: <c:out value="${module.name}"/></span>
			<div class="d-flex gap-1 ms-auto flex-shrink-0">
				<auth:if basePathKey="module.basepath" paths="/edit/insert_form">
				<auth:then>
				<a href='<bean:message key="module.basepath"/>/edit/insert_form'
				   class="btn btn-sm btn-outline-success" title="Create new transfer module"><i class="bi bi-plus-circle"></i></a>
				<c:if test="${not empty module.id}">
				<a href='<bean:message key="module.basepath"/>/edit/update_form/${module.id}'
				   class="btn btn-sm btn-outline-primary" title="Edit this transfer module"><i class="bi bi-pencil"></i></a>
				<a href='<bean:message key="module.basepath"/>/edit/delete_form/${module.id}'
				   class="btn btn-sm btn-outline-danger" title="Delete this transfer module"><i class="bi bi-trash"></i></a>
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
				<div class="field-row"><div class="field-label">Name</div><div class="field-value"><span class="val-code"><c:out value="${module.name}" /></span></div></div>
				<div class="field-row"><div class="field-label">Class Name</div><div class="field-value"><span class="val-code"><c:out value="${module.classe}" /></span></div></div>
				<div class="field-row"><div class="field-label">Class Path</div><div class="field-value"><c:choose><c:when test="${fn:length(module.archive) gt 0}"><span class="val-code"><c:out value="${module.archive}" /></span></c:when><c:otherwise><span class="text-danger fst-italic">default</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${module.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			</div>
		</div>
	</div>

	<c:if test="${not empty moduleGuide}">
		<jsp:include page="${moduleGuide}"/>
	</c:if>

</c:if>
