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
	<tiles:insert page="./pds/user/category/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">

		<tr>
			<th>Name</th>
			<td><c:out value="${category.name}" /></td>
		</tr>
		<tr>
			<th>Description</th>
			<td><c:out value="${category.description}" /></td>
		</tr>

	</table>

<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
</style>

	<div class="row g-2 mt-2" style="max-width:700px">
	  <div class="col-12 col-md-6">
	    <div class="card assoc-card">
	      <div class="card-header">
	        <i class="bi bi-globe text-secondary"></i>
	        <strong>Associated Web Resources</strong>
	      </div>
	      <div class="card-body p-2">
	        <c:choose>
	          <c:when test="${empty category.accessibleResources}">
	            <p class="text-muted small mb-0"><em>No web resources assigned.</em></p>
	          </c:when>
	          <c:otherwise>
	            <div class="d-flex flex-wrap">
	              <c:forEach var="resource" items="${category.accessibleResources}">
	                <span class="assoc-chip">
	                  <a href="/do/user/resource/${resource.id}" class="text-decoration-none text-body">${resource.path}</a>
	                </span>
	              </c:forEach>
	            </div>
	          </c:otherwise>
	        </c:choose>
	      </div>
	    </div>
	  </div>
	  <div class="col-12 col-md-6">
	    <div class="card assoc-card">
	      <div class="card-header">
	        <i class="bi bi-person text-secondary"></i>
	        <strong>Associated Web Users</strong>
	      </div>
	      <div class="card-body p-2">
	        <c:choose>
	          <c:when test="${empty category.usersWithProfile}">
	            <p class="text-muted small mb-0"><em>No web users assigned.</em></p>
	          </c:when>
	          <c:otherwise>
	            <div class="d-flex flex-wrap">
	              <c:forEach var="user" items="${category.usersWithProfile}">
	                <span class="assoc-chip">
	                  <a href="/do/user/user/${user.id}" title="${user.commonName}" class="text-decoration-none text-body">${user.id}</a>
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

