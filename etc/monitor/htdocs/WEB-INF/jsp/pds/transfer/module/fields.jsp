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
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-puzzle text-primary flex-shrink-0"></i>
            Create a new Transfer Module plugin.
        </div>
        </td></tr>
    </c:when>
    <c:otherwise>
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-puzzle text-primary flex-shrink-0"></i>
            Edit the Transfer Module configuration.
        </div>
        </td></tr>
    </c:otherwise>
</c:choose>

	<logic:match name="isInsert" value="true">
		<tr>
			<th>Name</th>
			<td>
				<div class="d-flex align-items-center gap-2">
					<input id="name" name="name" type="text"
						pattern="[a-zA-Z0-9]+"
						title="Letters and digits only (e.g. FtpModule)"
						oninput="validatePatternInput(this, 'name-feedback')">
					<span id="name-feedback"></span>
				</div>
				<div id="name-feedback-msg" class="invalid-feedback" style="display:none">
					Letters and digits only (e.g. <code>FtpModule</code>).
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
		<th>Class Name</th>
		<td>
			<div class="d-flex align-items-center gap-2">
				<input id="classe" name="classe" type="text"
					pattern="[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)*"
					title="Fully qualified class name (e.g. com.example.FtpModule)"
					value="<c:out value='${requestScope[actionFormName].classe}' />"
					oninput="validatePatternInput(this, 'classe-feedback')">
				<span id="classe-feedback"></span>
			</div>
			<div id="classe-feedback-msg" class="invalid-feedback" style="display:none">
				Fully qualified class name using letters, digits and <code>.</code> separators (e.g. <code>com.example.FtpModule</code>).
			</div>
		</td>
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
