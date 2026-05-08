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
			<auth:if basePathKey="module.basepath" paths="/edit/insert_form">
			<auth:then>
			<div class="d-flex gap-1 ms-auto flex-shrink-0">
				<a href='<bean:message key="module.basepath"/>/edit/insert_form'
				   class="btn btn-sm btn-outline-success" title="Create new transfer module"><i class="bi bi-plus-circle"></i></a>
				<c:if test="${not empty module.id}">
				<a href='<bean:message key="module.basepath"/>/edit/update_form/${module.id}'
				   class="btn btn-sm btn-outline-primary" title="Edit this transfer module"><i class="bi bi-pencil"></i></a>
				<a href='<bean:message key="module.basepath"/>/edit/delete_form/${module.id}'
				   class="btn btn-sm btn-outline-danger" title="Delete this transfer module"><i class="bi bi-trash"></i></a>
				</c:if>
			</div>
			</auth:then>
			</auth:if>
		</div>
		<div class="card-body pb-2">
			<div class="row g-3">
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Name</div>
					<div class="fw-medium"><c:out value="${module.name}" /></div>
				</div>
				<div class="col-sm-8">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Class Name</div>
					<div class="fw-medium font-monospace"><c:out value="${module.classe}" /></div>
				</div>
				<div class="col-sm-8">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Class Path</div>
					<div class="fw-medium">
						<c:choose>
							<c:when test="${fn:length(module.archive) gt 0}"><c:out value="${module.archive}" /></c:when>
							<c:otherwise><span class="text-danger fst-italic">default</span></c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Enabled</div>
					<div>
						<c:if test="${module.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
						<c:if test="${!module.active}"><i class="bi bi-x-circle-fill text-secondary" title="No"></i></c:if>
					</div>
				</div>
			</div>
		</div>
	</div>

</c:if>

