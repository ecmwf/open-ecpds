<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
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
		<th>Class Name</th>
		<td><input id="classe" name="classe" type="text"
			value="<c:out value='${requestScope[actionFormName].classe}' />">&nbsp;(please
			use letters, digits and '.' only)</td>
	</tr>
	<tr>
		<th>Class Path</th>
		<td><html:text property="archive" /></td>
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
				return checkKeyIsMatching(event, "^[a-zA-Z0-9]+$");
			});
	$('#classe').bind(
			'keypress',
			function(event) {
				return checkKeyIsMatching(event, "^[a-zA-Z0-9.]+$");
			});
</script>
