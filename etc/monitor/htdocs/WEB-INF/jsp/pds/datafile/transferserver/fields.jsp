<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
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
			<td><c:out value="${requestScope[actionFormName].name}" /> <html:hidden
					property="name" /></td>
		</tr>
	</logic:notMatch>

	<tr>
		<th>Transfer Group</th>
		<td><bean:define id="groups" name="transferServerActionForm"
				property="transferGroupOptions" type="java.util.Collection" /> <html:select
				property="transferGroupName">
				<html:options collection="groups" property="name"
					labelProperty="name" />
			</html:select></td>
	</tr>

	<tr>
		<th>Hostname</tg>
		<td><input title="DNS name" property="host" id="host" name="host"
			type="text" value="${requestScope[actionFormName].host}">&nbsp;(please
			use letters, digits, '-' and '.' only)</td>
	</tr>
	<tr>
		<th>Host For Replication</th>
		<td><bean:define id="hosts" name="transferServerActionForm"
				property="hostForReplicationOptions" type="java.util.Collection" />
			<html:select property="hostForReplicationName">
				<html:options collection="hosts" property="name"
					labelProperty="nickName" />
			</html:select></td>
	</tr>

	<tr>
		<th>Port</th>
		<td><html:text property="port" /></td>
	</tr>
	<tr>
		<th>Check</th>
		<td><html:checkbox property="check" /></td>
	</tr>
	<tr>
		<th>Enabled</th>
		<td><html:checkbox property="active" /></td>
	</tr>
	<tr>
		<th>Replicate</th>
		<td><html:checkbox property="replicate" /></td>
	</tr>

</table>

<script>
	$('#name').on('input', function() {
			const regex = /^[a-zA-Z0-9_-]+$/;
			const $this = $(this);
			const value = $this.val();
			if (!regex.test(value)) {
				// Remove all invalid characters
				$this.val(value.replace(/[^a-zA-Z0-9_-]/g, ''));
			}
	});
	$('#host').on('input', function() {
		const regex = /^[a-zA-Z0-9-.]+$/;
		const $this = $(this);
		const value = $this.val();
		if (!regex.test(value)) {
			// Remove all invalid characters
			$this.val(value.replace(/[^a-zA-Z0-9-.]/g, ''));
		}
	});
</script>
