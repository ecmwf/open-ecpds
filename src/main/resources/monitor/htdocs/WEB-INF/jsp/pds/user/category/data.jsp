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

	<table>
		<tr>
			<td valign="top"><display:table id="resource"
					name="${category.accessibleResources}" requestURI="" sort="list"
					class="listing">
					<display:column title="Path" sortable="true">
						<a href="/do/user/resource/${resource.id}">${resource.path}</a>
					</display:column>
					<display:caption>Associated Web Resources</display:caption>
				</display:table></td>

			<td valign="top"><display:table id="user"
					name="${category.usersWithProfile}" requestURI="" sort="list"
					class="listing">
					<display:column title="Uid" sortable="true">
						<a href="/do/user/user/${user.id}">${user.id}</a>
					</display:column>
					<display:column title="Name" sortable="true">
						<!-- ${user.commonName} -->
						<a href="/do/user/user/${user.id}">${user.commonName}</a>
					</display:column>
					<display:caption>Associated Web Users</display:caption>
				</display:table></td>

		</tr>
	</table>

</c:if>

