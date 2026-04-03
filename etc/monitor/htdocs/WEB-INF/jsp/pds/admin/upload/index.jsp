<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<div class="d-flex align-items-center gap-2 mb-3 px-1 py-2 rounded"
     style="background:rgba(13,110,253,0.06); font-size:0.83rem; color:#495057; border-left:3px solid #0d6efd;">
    <i class="bi bi-upload text-primary ms-1 flex-shrink-0"></i>
    Use this form to upload a text file to a Transfer Host.
</div>

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