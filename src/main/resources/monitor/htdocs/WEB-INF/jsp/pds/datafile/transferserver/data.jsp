<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/datafile/transferserver/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${transferserver.name}" /></td>
		</tr>
		<tr>
			<th>Hostname</th>
			<td><c:out value="${transferserver.host}" /></td>
		</tr>
		<tr>
			<th>Port</th>
			<td><c:out value="${transferserver.port}" /></td>
		</tr>
		<tr>
			<th>Group</th>
			<td><a
				href="/do/datafile/transfergroup/${transferserver.transferGroupName}">${transferserver.transferGroupName}</a></td>
		</tr>

		<c:set var="hostForReplication"
			value="${transferserver.hostForReplication}" />
		<c:if test="${transferserver.replicate}">
			<c:if test="${not empty hostForReplication}">
				<tr>
					<th>Host For Replication</th>
					<td><a href="/do/transfer/host/${hostForReplication.name}">${hostForReplication.nickName}</a></td>
				</tr>
			</c:if>
		</c:if>

		<tr>
			<th>Check</th>
			<td><c:if test="${transferserver.check}">yes</c:if> <c:if
					test="${!transferserver.check}">no</c:if></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${transferserver.active}">yes</c:if> <c:if
					test="${!transferserver.active}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<tr>
			<th>Replicate</th>
			<td><c:if test="${transferserver.replicate}">yes</c:if> <c:if
					test="${!transferserver.replicate}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<tr>
			<th>Last Update</th>
			<td><content:content name="transferserver.lastUpdateDate" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
		</tr>
	</table>

</c:if>
