<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<table class="fields">

	<html:hidden property="id" />

	<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <div class="form-info-banner">
            <i class="bi bi-collection text-primary flex-shrink-0"></i>
            Create a new Transfer Group to organise transfer servers.
        </div>
    </c:when>
    <c:otherwise>
        <div class="form-info-banner">
            <i class="bi bi-collection text-primary flex-shrink-0"></i>
            Edit the Transfer Group configuration.
        </div>
    </c:otherwise>
</c:choose>


	<tiles:useAttribute id="actionFormName" name="action.form.name"
		classname="java.lang.String" />

	<logic:match name="isInsert" value="true">
		<tr>
			<th>Name</th>
			<td>
				<div class="d-flex align-items-center gap-2">
					<input id="name" name="name" type="text"
						pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
						title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. group-1)"
						oninput="validatePatternInput(this, 'name-feedback')">
					<span id="name-feedback"></span>
				</div>
			</td>
		</tr>
	</logic:match>
	<logic:notMatch name="isInsert" value="true">
		<tr>
			<th>Name</th>
			<td><c:out value="${requestScope[actionFormName].name}" /> <html:hidden property="name" /></td>
		</tr>
	</logic:notMatch>

	<tr>
		<th>Comment</th>
		<td><html:text property="comment" styleClass="comment" /></td>
	</tr>
	<tr>
		<th>Enabled</th>
		<td><html:checkbox property="active" /></td>
	</tr>
	<tr>
		<th>Replicate</th>
		<td><html:checkbox property="replicate" /></td>
	</tr>
	<tr>
		<th>Min. Replication Count</th>
		<td><html:text property="minReplicationCount"
				styleClass="comment" /></td>
	</tr>
	<tr>
		<th>Volume Count</th>
		<td><html:text property="volumeCount" styleClass="comment" /></td>
	</tr>
	<tr>
		<th>Filter</th>
		<td><html:checkbox property="filter" /></td>
	</tr>
	<tr>
		<th>Min. Filtering Count</th>
		<td><html:text property="minFilteringCount" styleClass="comment" /></td>
	</tr>
	<tr>
		<th>Backup</th>
		<td><html:checkbox property="backup" /></td>
	</tr>

	<tr>
		<th>Host For Backup</th>
		<td><bean:define id="hosts" name="transferGroupActionForm"
				property="hostForBackupOptions" type="java.util.Collection" /> <html:select
				property="hostForBackupName">
				<html:options collection="hosts" property="name"
					labelProperty="nickName" />
			</html:select></td>
	</tr>

	<tr>
		<th>Cluster Name</th>
		<td><html:text property="clusterName" styleClass="comment" /></td>
	</tr>
	<tr>
		<th>Cluster Weight</th>
		<td><html:text property="clusterWeight" styleClass="comment" /></td>
	</tr>

</table>

<c:if test="${isInsert != 'true'}">
<p class="fw-bold mb-1 mt-2">Transfer Servers <a
		href="/do/datafile/transferserver/edit/insert_form?transferGroupName=${transferGroupActionForm.id}"><content:icon
			key="icon.small.insert" titleKey="button.insert"
			altKey="button.insert" writeFullTag="true" /></a></p>
<table id="tgFieldsServersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
	<thead class="table-light">
		<tr>
			<th>Name</th>
			<th class="text-center">Actions</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach var="server" items="${transferGroupActionForm.transferServers}">
		<tr>
			<td>${server.name}</td>
			<td class="buttons text-center">
				<auth:link styleClass="menuitem"
					href="/do/datafile/transferserver/edit/update_form/${server.id}"
					imageKey="icon.small.update" imageTitleKey="button.update"
					imageAltKey="button.update" />
				<auth:link styleClass="menuitem"
					href="/do/datafile/transferserver/edit/delete_form/${server.id}"
					imageKey="icon.small.delete" imageTitleKey="button.delete"
					imageAltKey="button.delete" />
			</td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<script>
$(document).ready(function() {
	$('#tgFieldsServersTable').DataTable({
		paging:    false,
		searching: false,
		ordering:  true,
		info:      false,
		columnDefs: [{ orderable: false, targets: -1 }]
	});
});
</script>
</c:if>

<script>
</script>
