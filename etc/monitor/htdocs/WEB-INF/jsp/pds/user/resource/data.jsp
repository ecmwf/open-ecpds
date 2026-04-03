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
	<tiles:insert page="./pds/user/resource/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">
		<tr>
			<th>Path</th>
			<td class="identifier">${resource.path}</td>
		</tr>

		<auth:if basePathKey="accesscontrol.basepath" paths="/detailer">
			<auth:then>
				<tr>
					<th>Access Detailer</th>
					<td class="identifier"><auth:link styleClass="menuitem"
							basePathKey="accesscontrol.basepath"
							href="/detailer?page=${resource.id}" imageKey="icon.small.text"
							imageTitleKey="ecpds.user.detailer" ignoreAccessControl="true" /></td>
				</tr>
			</auth:then>
		</auth:if>
	</table>

<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
</style>

	<div class="card assoc-card mt-2" style="max-width:480px">
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
	              <a href="/do/user/category/${category.id}" title="${category.description}" class="text-decoration-none text-dark">${category.name}</a>
	            </span>
	          </c:forEach>
	        </div>
	      </c:otherwise>
	    </c:choose>
	  </div>
	</div>
</c:if>
