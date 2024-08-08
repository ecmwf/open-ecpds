<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/datafile/transfergroup/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${transfergroup.name}" /></td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><c:out value="${transfergroup.comment}" /></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${transfergroup.active}">yes</c:if> <c:if
					test="${!transfergroup.active}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<tr>
			<th>Replicate</th>
			<td><c:if test="${transfergroup.replicate}">yes</c:if> <c:if
					test="${!transfergroup.replicate}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<tr>
			<th>Min. Replication Count</th>
			<td>${transfergroup.minReplicationCount}</td>
		</tr>
		<tr>
			<th>Volume Count</th>
			<td>${transfergroup.volumeCount}</td>
		</tr>
		<tr>
			<th>Filter</th>
			<td><c:if test="${transfergroup.filter}">yes</c:if> <c:if
					test="${!transfergroup.filter}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<tr>
			<th>Min. Filtering Count</th>
			<td>${transfergroup.minFilteringCount}</td>
		</tr>
		<tr>
			<th>Backup</th>
			<td><c:if test="${transfergroup.backup}">yes</c:if> <c:if
					test="${!transfergroup.backup}">
					<font color="red">no</font>
				</c:if></td>
		</tr>
		<c:set var="hostForBackup" value="${transfergroup.hostForBackup}" />
		<c:if test="${transfergroup.backup}">
			<c:if test="${not empty hostForBackup}">
				<tr>
					<th>Host For Backup</th>
					<td><a href="/do/transfer/host/${hostForBackup.name}">${hostForBackup.nickName}</a></td>
				</tr>
			</c:if>
		</c:if>

		<c:set var="clusterName" value="${transfergroup.clusterName}" />
		<c:if test="${not empty clusterName}">
			<tr>
				<th>Cluster Name</th>
				<td><c:out value="${clusterName}" /></td>
			</tr>
			<tr>
				<th>Cluster Weight</th>
				<td><c:out value="${transfergroup.clusterWeight}" /></td>
			</tr>
		</c:if>

	</table>

	<display:table id="server" name="${transfergroup.transferServers}"
		requestURI="" pagesize="50" defaultsort="1" sort="list"
		class="listing">
		<display:column title="Name" sortable="true">
			<a href="/do/datafile/transferserver/${server.name}">${server.name}</a>
		</display:column>
		<display:column property="host" />
		<display:column property="port" />
		<display:column property="active" sortable="true" />
		<display:column property="replicate" />
		<display:caption>Transfer Servers</display:caption>
	</display:table>

</c:if>

