<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/method/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${method.name}" /></td>
		</tr>
		<tr>
			<th>Value</th>
			<td><c:out value="${method.value}" /></td>
		</tr>
		<tr>
			<th>Transfer Module</th>
			<td><a
				href="<bean:message key="module.basepath"/>/${method.ecTransModule.id}">${method.ecTransModule.name}</a></td>
		</tr>
		<tr>
			<th>Restrict</th>
			<td><c:if test="${method.restrict}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!method.restrict}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
		</tr>
		<tr>
			<th>Resolve</th>
			<td><c:if test="${method.resolve}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!method.resolve}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><c:out value="${method.comment}" /></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${method.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!method.active}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
	</table>

</c:if>

