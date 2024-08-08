<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />

<script>
	function validate(path, message) {
		if (confirm(message)) {
			window.location = path
		}
	}
	function hideChoosers(layerName) {
		if (layerName != 'resourceChooser')
			hide('resourceChooser');
	}
</script>

<html:hidden property="id" />

<table>
	<tr>
		<td valign="top">

			<table class="fields">

				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Name</th>
						<td><c:out value="${categoryActionForm.name}" /> <html:hidden property="name" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Name</th>
						<td><html:text property="name" /></td>
					</tr>
				</c:if>

				<tr>
					<th>Description</th>
					<td><html:text property="description" /></td>
				</tr>
			</table>

		</td>

		<td width="25"></td>

		<td valign="top"><c:if test="${isInsert != 'true'}">
				<%
					boolean odd;
				%>
				<div id="resourceChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Choose a Web Resource to Add</caption>
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
							<c:forEach var="column"
								items="${categoryActionForm.resourceOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/category/edit/update/${categoryActionForm.id}/addResource/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td>${column.path}</td>
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
				<display:table id="resource" name="${categoryActionForm.resources}"
					requestURI="" class="listing">
					<display:column property="path" sortable="true" title="Name" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="category.basepath"/>/edit/update/<c:out value="${categoryActionForm.id}"/>/deleteResource/<c:out value="${resource.id}"/>','<bean:message key="ecpds.category.deleteResource.warning" arg0="${resource.id}" arg1="${categoryActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Associated Web Resources <a
							href="#" onClick="toggle_in_place(event,'resourceChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
			</c:if></td>
	</tr>
</table>
