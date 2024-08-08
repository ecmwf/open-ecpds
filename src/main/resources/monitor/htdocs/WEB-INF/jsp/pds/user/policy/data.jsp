<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/user/policy/warning.jsp" />
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

.scrollable-tab {
	width: 550px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
  	overflow-y: scroll;
</style>

	<table border="0">
		<tr>
			<td valign="top"><br>
				<table class="fields">
					<tr>
						<th>Name</th>
						<td>${policy.id}</td>
					</tr>
					<tr>
						<th>Comment</th>
						<td>${policy.comment}</td>
					</tr>
					<tr>
						<th>Enabled</th>
						<td><c:if test="${policy.active}">yes</c:if> <c:if
								test="${!policy.active}">
								<font color="red">no</font>
							</c:if></td>
					</tr>
					<tr>
						<th>Options</th>
						<td>
							<div id="tabs">
								<ul>
									<li><a href="#tabs-1">Properties</a></li>
									<li><a href="#tabs-2">Help</a></li>
								</ul>
								<div id="tabs-1">
									<pre id="properties">
										<c:out value="${policy.properties}" />
									</pre>
									<textarea id="properties" name="properties" style="display: none;"></textarea>
								</div>
								<div id="tabs-2" class="scrollable-tab">
								</div>
							</div>
						</td>
					</tr>
				</table></td>
			<td width="25"></td>
			<td valign="top"><display:table id="destination"
					name="${policy.associatedDestinations}" requestURI=""
					class="listing">
					<display:column title="Name">
						<a
							href="<bean:message key="destination.basepath"/>/${destination.name}">${destination.name}</a>
					</display:column>
					<display:column property="comment" />
					<display:caption>Associated Destinations</display:caption>
				</display:table></td>
		</tr>

	</table>

	<script>
		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		
		// Get the completions from the bean!      		
    	var completions = [
    		${policy.completions}
    	];
		
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#tabs-2').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties);
        
		// Add a click event listener to the properties editor
    	editorProperties.addEventListener("changeSelection", function (event) {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	});
		
		makeResizable(editorProperties);
		
		$("#tabs").tabs();
		$("#tabs").tabs("option", "active", 0);
	</script>

</c:if>
