<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/user/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<style>
#properties {
	width: 550px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}
</style>

	<table>
		<tr>
			<td valign="top"><br>
				<table class="fields">
					<tr>
						<th>Web Login</th>
						<td><c:out value="${user.uid}" /></td>
					</tr>
					<tr>
						<th>Comment</th>
						<td><c:out value="${user.commonName}" /></td>
					</tr>
					<tr>
						<th>Enabled</th>
						<td><c:if test="${user.active}">yes</c:if> <c:if
								test="${!user.active}">
								<font color="red">no</font>
							</c:if></td>
					</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>
					<tr>
						<th>Properties</th>
						<td><pre id="properties">
								<c:out value="${user.userData}" />
							</pre> <textarea id="properties" name="properties"
								style="display: none;"></textarea></td>
					</tr>
				</table></td>
			<td width="25"></td>
			<td valign="top"><display:table id="category"
					name="${user.categories}" requestURI="" class="listing">
					<display:column title="Name">
						<a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a>
					</display:column>
					<display:column property="description" />
					<display:caption>Associated Web Categories</display:caption>
				</display:table></td>
		</tr>

	</table>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		makeResizable(editorProperties);
	</script>
</c:if>
