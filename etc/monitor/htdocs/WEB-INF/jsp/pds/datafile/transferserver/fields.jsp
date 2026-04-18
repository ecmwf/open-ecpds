<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<table class="fields">

	<html:hidden property="id" />

	<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-server text-primary flex-shrink-0"></i>
            Register a new Transfer Server for data file processing.
        </div>
        </td></tr>
    </c:when>
    <c:otherwise>
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-server text-primary flex-shrink-0"></i>
            Edit the Transfer Server configuration.
        </div>
        </td></tr>
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
						title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. server-1)"
						oninput="validatePatternInput(this, 'name-feedback')">
					<span id="name-feedback"></span>
				</div>
			</td>
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
		<th>Hostname <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="DNS name" tabindex="0"></i></th>
		<td>
			<div class="d-flex align-items-center gap-2">
				<input id="host" name="host" type="text"
					value="${requestScope[actionFormName].host}"
					oninput="validateHostInput(this)">
				<span id="hostFeedback"></span>
			</div>
		</td>
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
		<td>
			<div class="d-flex align-items-center gap-2">
				<input type="number" id="port" name="port"
					min="1" max="65535"
					value="${requestScope[actionFormName].port}"
					title="Valid port number (1-65535)"
					style="width:100px"
					oninput="validatePatternInput(this, 'port-feedback')">
				<span id="port-feedback"></span>
			</div>
		</td>
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
</script>
