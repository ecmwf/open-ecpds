<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<table class="fields">

	<html:hidden property="id" />

	<tiles:useAttribute name="isInsert" classname="java.lang.String" />
	<tiles:useAttribute id="actionFormName" name="action.form.name"
		classname="java.lang.String" />

	<logic:match name="isInsert" value="true">
		<tr>
			<th>Name</th>
			<td><input id="name" name="name" type="text">&nbsp;(please
				use letters, digits, '_' and '-' only)</td>
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

<display:table id="server"
	name="${transferGroupActionForm.transferServers}" requestURI=""
	class="listing">
	<display:column property="name" sortable="true" />
	<display:column>
		<table width="10">
			<tr>
				<td><auth:link styleClass="menuitem"
						href="/do/datafile/transferserver/edit/update_form/${server.id}"
						imageKey="icon.small.update" imageTitleKey="button.update"
						imageAltKey="button.update" /></td>
				<td><auth:link styleClass="menuitem"
						href="/do/datafile/transferserver/edit/delete_form/${server.id}"
						imageKey="icon.small.delete" imageTitleKey="button.delete"
						imageAltKey="button.delete" /></td>
			</tr>
		</table>
	</display:column>
	<display:caption>Transfer Servers <a
			href="/do/datafile/transferserver/edit/insert_form?transferGroupName=${transferGroupActionForm.id}"><content:icon
				key="icon.small.insert" titleKey="button.insert"
				altKey="button.insert" writeFullTag="true" /></a>
	</display:caption>
</display:table>

<script>
	$('#name').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9_-]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
</script>
