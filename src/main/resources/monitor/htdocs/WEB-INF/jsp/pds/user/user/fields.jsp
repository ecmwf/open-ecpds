<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />

<style>
#userData {
	width: 550px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}
</style>

<script>
	function validate(path, message) {
		if (confirm(message)) {
			window.location = path
		}
	}
	function hideChoosers(layerName) {
		if (layerName != 'categoryChooser')
			hide('categoryChooser');
	}
</script>

<table>

	<tr>
		<td valign="top">

			<table class="fields">
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Web Login</th>
						<td><c:out value="${userActionForm.uid}" /> <html:hidden
								property="uid" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Web Login</th>
						<td><input id="uid" name="uid" type="text">&nbsp;(please
							use letters, digits and '.' only)</td>
					</tr>
				</c:if>
				<tr>
					<th>Comment</th>
					<td><html:text property="name" /></td>
				</tr>
				<tr>
					<th>Enabled</th>
					<td><html:checkbox property="active" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Password</th>
					<td><input id="password" name="password" type="password"
						value="${userActionForm.password}">
						<button type="button" id="buttonPassword" name="buttonPassword"
							onclick="generatePassword(); return false">Generate</button>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Properties</th>
					<td colspan="2"><pre id="userData">
							<c:out value="${userActionForm.userData}" />
						</pre> <textarea id="userData" name="userData" style="display: none;"></textarea>
						<button type="button"
							onclick="formatSource(editorProperties); return false">Format</button>
						<button type="button"
							onclick="clearSource(editorProperties); return false">Clear</button>
					</td>
				</tr>
			</table>

		</td>

		<td width="25"></td>

		<td valign="top"><c:if test="${isInsert != 'true'}">
				<%
				boolean odd;
				%>
				<div id="categoryChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Choose a Web Category to Add</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column" items="${userActionForm.categoryOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/user/edit/update/${userActionForm.id}/addCategory/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.description}">${column.name}</td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
			</c:if></td>
	</tr>

	<tr>
		<td><c:if test="${isInsert != 'true'}">
				<display:table id="category" name="${userActionForm.categories}"
					requestURI="" class="listing">
					<display:column property="name" sortable="true" title="Name" />
					<display:column property="description" sortable="false"
						title="Description" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="user.basepath"/>/edit/update/<c:out value="${userActionForm.id}"/>/deleteCategory/<c:out value="${category.id}"/>','<bean:message key="ecpds.user.deleteCategory.warning" arg0="${category.name}" arg1="${userActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Associated Web Categories <a
							href="#" onClick="toggle_in_place(event,'categoryChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
			</c:if></td>

	</tr>
</table>

<script>
	var editorProperties = getEditorProperties(false, false, "userData", "crystal");

	var textareaProperties = $('textarea[name="userData"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	makeResizable(editorProperties);

    function generatePassword() {
    	$("#password").val(getPassword());
    }
    
    $('#uid').bind('keypress', function (event) {
		return checkKeyIsMatching(event, "^[a-zA-Z0-9.]+$");
    });
</script>