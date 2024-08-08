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

	<display:table id="category" name="${resource.categories}"
		requestURI="" class="listing">
		<display:column title="Name">
			<a href="/do/user/category/${category.id}">${category.name}</a>
		</display:column>
		<display:column property="description" />
		<display:caption>Associated Web Categories</display:caption>
	</display:table>
</c:if>
