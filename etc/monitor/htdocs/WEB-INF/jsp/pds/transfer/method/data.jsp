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
			<auth:if basePathKey="method.basepath" paths="/edit/insert_form">
			<auth:then>
			<div class="d-flex gap-1 ms-auto flex-shrink-0">
				<a href='<bean:message key="method.basepath"/>/edit/insert_form'
				   class="btn btn-sm btn-outline-success" title="Create new transfer method"><i class="bi bi-plus-circle"></i></a>
				<c:if test="${not empty method.id}">
				<a href='<bean:message key="method.basepath"/>/edit/update_form/${method.id}'
				   class="btn btn-sm btn-outline-primary" title="Edit this transfer method"><i class="bi bi-pencil"></i></a>
				<a href='<bean:message key="method.basepath"/>/edit/delete_form/${method.id}'
				   class="btn btn-sm btn-outline-danger" title="Delete this transfer method"><i class="bi bi-trash"></i></a>
				</c:if>
			</div>
			</auth:then>
			</auth:if>
		</div>
		<div class="card-body pb-2">
			<div class="row g-3">
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Name</div>
					<div class="fw-medium"><c:out value="${method.name}" /></div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Value</div>
					<div class="fw-medium"><c:out value="${method.value}" /></div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Transfer Module</div>
					<div class="fw-medium"><a href="<bean:message key="module.basepath"/>/${method.ecTransModule.id}">${method.ecTransModule.name}</a></div>
				</div>
				<div class="col-sm-6">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Comment</div>
					<div class="fw-medium"><c:out value="${method.comment}" /></div>
				</div>
				<div class="col-sm-6">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Flags</div>
					<div class="d-flex flex-wrap gap-3">
						<div>
							<span class="text-muted small me-1">Restrict</span>
							<c:if test="${method.restrict}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
							<c:if test="${!method.restrict}"><i class="bi bi-x-circle-fill text-secondary" title="No"></i></c:if>
						</div>
						<div>
							<span class="text-muted small me-1">Resolve</span>
							<c:if test="${method.resolve}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
							<c:if test="${!method.resolve}"><i class="bi bi-x-circle-fill text-secondary" title="No"></i></c:if>
						</div>
						<div>
							<span class="text-muted small me-1">Enabled</span>
							<c:if test="${method.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
							<c:if test="${!method.active}"><i class="bi bi-x-circle-fill text-secondary" title="No"></i></c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

</c:if>

