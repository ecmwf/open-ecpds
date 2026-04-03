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
			<td><c:if test="${transfergroup.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.active}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Replicate</th>
			<td><c:if test="${transfergroup.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.replicate}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
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
			<td><c:if test="${transfergroup.filter}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.filter}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Min. Filtering Count</th>
			<td>${transfergroup.minFilteringCount}</td>
		</tr>
		<tr>
			<th>Backup</th>
			<td><c:if test="${transfergroup.backup}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.backup}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
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

	<p class="fw-bold mb-1 mt-2">Transfer Servers</p>
	<display:table id="server" name="${transfergroup.transferServers}"
		requestURI="" pagesize="50" defaultsort="1" sort="list"
		class="listing">
		<display:column title="Name" sortable="true">
			<a href="/do/datafile/transferserver/${server.name}">${server.name}</a>
		</display:column>
		<display:column property="host" />
		<display:column property="port" />
		<display:column sortable="true" title="Active">
			<c:if test="${transferserver.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
			<c:if test="${!transferserver.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
		</display:column>
		<display:column title="Replicate">
			<c:if test="${transferserver.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
			<c:if test="${!transferserver.replicate}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
		</display:column>
	</display:table>

</c:if>

