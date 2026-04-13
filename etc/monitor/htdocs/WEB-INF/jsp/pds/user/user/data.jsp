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
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
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
					<tr style="display:none">
						<th>Properties</th>
						<td><pre id="properties">
								<c:out value="${user.userData}" />
							</pre> <textarea id="properties" name="properties"
								style="display: none;"></textarea></td>
					</tr>
				</table></td>
			<td width="25"></td>
			<td valign="top">
				<div class="card assoc-card mt-2">
				  <div class="card-header">
				    <i class="bi bi-folder text-secondary"></i>
				    <strong>Associated Web Categories</strong>
				  </div>
				  <div class="card-body p-2">
				    <c:choose>
				      <c:when test="${empty user.categories}">
				        <p class="text-muted small mb-0"><em>No web categories assigned.</em></p>
				      </c:when>
				      <c:otherwise>
				        <div class="d-flex flex-wrap">
				          <c:forEach var="category" items="${user.categories}">
				            <span class="assoc-chip">
				              <a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}" class="text-decoration-none text-body">${category.name}</a>
				            </span>
				          </c:forEach>
				        </div>
				      </c:otherwise>
				    </c:choose>
				  </div>
				</div></td>
		</tr>

	</table>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		makeResizable(editorProperties);
	</script>
</c:if>
