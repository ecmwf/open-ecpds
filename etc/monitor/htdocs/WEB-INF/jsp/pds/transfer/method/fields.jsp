<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<table class="fields">

	<tiles:useAttribute id="actionFormName" name="action.form.name"
		classname="java.lang.String" />
	<tiles:useAttribute name="isInsert" classname="java.lang.String" />

	<logic:match name="isInsert" value="true">
		<tr>
			<th>Name</th>
			<td><input id="name" name="name" type="text">&nbsp;(please
				use letters and digits only)</td>
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
		<th>Value</th>
		<td><html:text property="value" /></td>
	</tr>

	<tr>
		<th>Transfer Module</th>
		<td><bean:define id="methods" name="transferMethodActionForm"
				property="ecTransModuleOptions" type="java.util.Collection	" /> <html:select
				property="ecTransModuleName">
				<html:options collection="methods" property="name"
					labelProperty="name" />
			</html:select></td>
	</tr>

	<tr>
		<th>Restrict</th>
		<td><html:checkbox property="restrict" /></td>
	</tr>
	<tr>
		<th>Resolve</th>
		<td><html:checkbox property="resolve" /></td>
	</tr>
	<tr>
		<th>Comment</th>
		<td><html:text property="comment" /></td>
	</tr>
	<tr>
		<th>Enabled</th>
		<td><html:checkbox property="active" /></td>
	</tr>

</table>

<script>
	$('#name').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
</script>
