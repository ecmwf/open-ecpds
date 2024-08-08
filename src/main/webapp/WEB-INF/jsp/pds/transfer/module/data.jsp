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
	<tiles:insert page="./pds/transfer/module/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${module.name}" /></td>
		</tr>
		<tr>
			<th>Class Name</th>
			<td><c:out value="${module.classe}" /></td>
		</tr>
		<tr>
			<th>Class Path</th>
			<td><c:if test="${fn:length(module.archive) gt 0}">
					<c:out value="${module.archive}" />
				</c:if> <c:if test="${fn:length(module.archive) eq 0}">
					<i><font color="red">default</font></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${module.active}">yes</c:if> <c:if
					test="${!module.active}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
	</table>

</c:if>

