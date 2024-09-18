<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<h2>Use this Form to upload a text file to a Transfer Host</h2>

<!-- <html:form action="/admin/upload"> -->
<form name="uploadActionForm" action="/do/admin/upload" method="post">

	<table class="fields">
		<tr>
			<th>Transfer Host</th>
			<td><bean:define id="hosts" name="uploadActionForm"
					property="hostOptions" type="java.util.Collection" /> <html:select
					property="host">
					<html:options collection="hosts" property="name"
						labelProperty="nickName" />
				</html:select></td>
		</tr>
		<tr>
			<th>Target File Name</th>
			<td><html:text property="target" /></td>
		</tr>
		<tr>
			<th>From Position</th>
			<td><html:text property="fromPos" /></td>
		</tr>
		<tr>
			<th>File Content</th>
			<td><html:textarea property="text" /></td>
		</tr>
	</table>
	</br>
	<button type="submit">Launch Upload</button>
	</html:form>