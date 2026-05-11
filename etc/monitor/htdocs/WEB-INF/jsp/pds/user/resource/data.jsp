<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/resource/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
</style>

	<div class="row g-3">
		<div class="col-lg-6">
			<div class="card">
				<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
					<i class="bi bi-globe text-primary"></i>
					<span class="fw-semibold">Web Resource: <c:out value="${resource.path}" /></span>
					<auth:if basePathKey="resource.basepath" paths="/edit/insert_form">
					<auth:then>
					<div class="d-flex gap-1 ms-auto flex-shrink-0">
						<a href='<bean:message key="resource.basepath"/>/edit/insert_form'
						   class="btn btn-sm btn-outline-success" title="Create new web resource"><i class="bi bi-plus-circle"></i></a>
						<c:if test="${not empty resource.id}">
						<a href='<bean:message key="resource.basepath"/>/edit/update_form/${resource.id}'
						   class="btn btn-sm btn-outline-primary" title="Edit this web resource"><i class="bi bi-pencil"></i></a>
						<a href='<bean:message key="resource.basepath"/>/edit/delete_form/${resource.id}'
						   class="btn btn-sm btn-outline-danger" title="Delete this web resource"><i class="bi bi-trash"></i></a>
						</c:if>
					</div>
					</auth:then>
					</auth:if>
				</div>
				<div class="card-body py-0">
					<div class="field-grid">
						<div class="field-row"><div class="field-label">Path</div><div class="field-value"><span class="val-code">${resource.path}</span></div></div>
						<auth:if basePathKey="accesscontrol.basepath" paths="/detailer">
							<auth:then>
								<div class="field-row"><div class="field-label">Access Detailer</div><div class="field-value"><auth:link styleClass="menuitem"
									basePathKey="accesscontrol.basepath"
									href="/detailer?page=${resource.id}" imageKey="icon.small.text"
									imageTitleKey="ecpds.user.detailer" ignoreAccessControl="true" /></div></div>
							</auth:then>
						</auth:if>
					</div>
				</div>
			</div>
		</div>
		<div class="col-lg-6">
			<div class="card assoc-card" style="max-width:480px">
			  <div class="card-header">
			    <i class="bi bi-folder text-secondary"></i>
			    <strong>Associated Web Categories</strong>
			  </div>
			  <div class="card-body p-2">
			    <c:choose>
			      <c:when test="${empty resource.categories}">
			        <p class="text-muted small mb-0"><em>No web categories assigned.</em></p>
			      </c:when>
			      <c:otherwise>
			        <div class="d-flex flex-wrap">
			          <c:forEach var="category" items="${resource.categories}">
			            <span class="assoc-chip">
			              <a href="/do/user/category/${category.id}" title="${category.description}" class="text-decoration-none text-body">${category.name}</a>
			            </span>
			          </c:forEach>
			        </div>
			      </c:otherwise>
			    </c:choose>
			  </div>
			</div>
		</div>
	</div>
</c:if>
