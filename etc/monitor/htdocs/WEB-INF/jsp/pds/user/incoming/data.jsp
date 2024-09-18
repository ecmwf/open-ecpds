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
	<tiles:insert page="./pds/user/incoming/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

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

#authorizedSSHKeys {
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
}
</style>

	<table border="0">
		<tr>
			<td valign="top"><br>
				<table class="fields">
					<tr>
						<th>Data Login</th>
						<td>${incoming.id}</td>
					</tr>
					<tr>
						<th>Comment</th>
						<td>${incoming.comment}</td>
					</tr>
					<tr>
						<th>Country</th>
						<td>${incoming.country.name}</td>
					</tr>
					<tr>
						<th>Enabled</th>
						<td><c:if test="${incoming.active}">yes</c:if> <c:if
								test="${!incoming.active}">
								<font color="red">no</font>
							</c:if></td>
					</tr>
					
					<tr>
						<td>&nbsp;</td>
					</tr>					
					
					<tr>
						<th>TOTP authentication</th>
						<td><c:if test="${incoming.isSynchronized}">yes</c:if> <c:if
								test="${!incoming.isSynchronized}">no</c:if></td>
					</tr>

					<tr>
						<td>&nbsp;</td>
					</tr>

					<tr>
						<th>Options</th>
						<td>
							<div id="tabs">
								<ul>
									<li><a href="#tabs-1">Properties</a></li>
									<li><a href="#tabs-2">Authorized SSH Keys</a></li>
									<li><a href="#tabs-3">Help</a></li>
								</ul>
								<div id="tabs-1">
									<pre id="userData">
										<c:out value="${incoming.properties}" />
									</pre>
									<textarea id="userData" name="userData" style="display: none;"></textarea>
								</div>
								<div id="tabs-2">
									<pre id="authorizedSSHKeys">
										<c:out value="${incoming.authorizedSSHKeys}" />
									</pre>
									<textarea id="authorizedSSHKeys" name="authorizedSSHKeys"
										style="display: none;"></textarea>
								</div>
								<div id="tabs-3" class="scrollable-tab">
								</div>
							</div>
						</td>
					</tr>
				</table></td>
			<td width="25"></td>
			<td valign="top"><display:table id="policy"
					name="${incoming.associatedIncomingPolicies}" requestURI=""
					class="listing">
					<display:column title="Name">
						<a href="<bean:message key="policy.basepath"/>/${policy.id}">${policy.id}</a>
					</display:column>
					<display:column property="comment" />
					<display:caption>Associated Data Policies</display:caption>
				</display:table> <display:table id="destination"
					name="${incoming.associatedDestinations}" requestURI=""
					class="listing">
					<display:column title="Name">
						<a
							href="<bean:message key="destination.basepath"/>/${destination.name}">${destination.name}</a>
					</display:column>
					<display:column property="comment" />
					<display:caption>Associated Destinations</display:caption>
				</display:table> <display:table id="operation"
					name="${incoming.associatedOperations}" requestURI=""
					class="listing">
					<display:column title="Name">${operation.name}</display:column>
					<display:column property="comment" />
					<display:caption>Associated Permissions</display:caption>
				</display:table> <display:table id="incomingSession"
					name="${incoming.incomingConnections}" requestURI=""
					class="listing">
					<display:column title="Mover Name">${incomingSession.dataMoverName}</display:column>
					<display:column title="Protocol">${incomingSession.protocol}</display:column>
					<display:column title="Remote Address">${incomingSession.remoteIpAddress}</display:column>
					<display:column title="Duration">${incomingSession.formatedDuration}</display:column>
					<display:caption>Current Sessions</display:caption>
				</display:table></td>
		</tr>
	</table>

	<script>
		var editorProperties = getEditorProperties(true, false, "userData", "crystal");
		
		// Get the completions from the bean!      		
    	var completions = [
    		${incoming.completions}
    	];
		
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#tabs-3').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
    	});

    	// Call the function to process each line
    	checkEachLine(editorProperties);
        
		// Add a click event listener to the properties editor
    	editorProperties.addEventListener("changeSelection", function (event) {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	});

    	var editorAuthorizedSSHKeys = getEditorProperties(true, false, "authorizedSSHKeys", "text");

		var textareaAuthorizedSSHKeys = $('textarea[name="authorizedSSHKeys"]');
		textareaAuthorizedSSHKeys.closest('form').submit(
				function() {
					textareaAuthorizedSSHKeys.val(editorAuthorizedSSHKeys
							.getSession().getValue());
				});

		makeResizable(editorProperties);
		makeResizable(editorAuthorizedSSHKeys);

		$("#tabs").tabs();
		$("#tabs").tabs("option", "active", 0);
	</script>
</c:if>

