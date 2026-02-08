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

<script>
	function validate(path, message) {
	    confirmationDialog({
	        title: "Please Confirm",
	        message: message,     // HTML allowed by default
	        onConfirm: function () {
	            window.location = path;
	        },
	        onCancel: function () {
	            // Nothing needed — simply don't navigate
	        }
	    });
	}
	function hideChoosers(layerName) {
		if (layerName != 'operationChooser')
			hide('operationChooser');
		if (layerName != 'destinationChooser')
			hide('destinationChooser');
		if (layerName != 'policyChooser')
			hide('policyChooser');
	}
</script>

<tiles:useAttribute id="actionFormName" name="action.form.name"
	classname="java.lang.String" />

<table border=0>
	<tr>
		<td valign="top">
			<table class="fields">
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Data Login</th>
						<td>${incomingUserActionForm.id}<html:hidden property="id" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Data Login</th>
						<td><input id="id" name="id" type="text">&nbsp;(please
							use letters, digits, '_' and '.' only)</td>
					</tr>
				</c:if>
				<tr>
					<th>Comment</th>
					<td><html:text property="comment" /></td>
				</tr>
				<tr>
					<th>Country</th>
					<td><c:set var="countries"
							value="${incomingUserActionForm.countryOptions}" /> <html:select
							property="countryIso">
							<html:options collection="countries" property="iso"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Enabled</th>
					<td><html:checkbox property="active" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>TOTP authentication</th>
					<td><html:checkbox property="isSynchronized"
							styleId="isSynchronized" onclick="handleTOTPClick(this)" /></td>
				</tr>
				<tr>
					<th>Or password</th>
					<td><input type="password" id="password" name="password"
						value="${incomingUserActionForm.password}" />
						<button type="button" id="buttonPassword" name="buttonPassword"
							onclick="generatePassword(); return false" />Generate
						</button></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Options</th>
					<td colspan="2">
						<div id="tabs">
							<ul>
								<li><a href="#tabs-1">Properties</a></li>
								<li><a href="#tabs-2">Authorized SSH Keys</a></li>
								<li><a href="#tabs-3">Help</a></li>
							</ul>
							<div id="tabs-1">
								<pre id="userData">
									<c:out value="${requestScope[actionFormName].userData}" />
								</pre>
								<textarea id="userData" name="userData" style="display: none;"></textarea>
								<button type="button"
									onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button"
									onclick="clearSource(editorProperties); return false">Clear</button>
							</div>
							<div id="tabs-2">
								<pre id="authorizedSSHKeys">
									<c:out
										value="${requestScope[actionFormName].authorizedSSHKeys}" />
								</pre>
								<textarea id="authorizedSSHKeys" name="authorizedSSHKeys"
									style="display: none;"></textarea>
								<button type="button"
									onclick="clearSource(editorAuthorizedSSHKeys); return false">Clear</button>
							</div>
							<div id="tabs-3" class="scrollable-tab">
							</div>
						</div>
					</td>
				</tr>
			</table>
		</td>
		<td width="25"></td>
		<td valign="top"><c:if test="${isInsert != 'true'}">
				<%
				boolean odd;
				%>
				<div id="policyChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Choose a Data Policy to Add</caption>
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
								items="${incomingUserActionForm.incomingPolicyOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addPolicy/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.id}</td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="destinationChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Choose a Destination to Add</caption>
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
								items="${incomingUserActionForm.destinationOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addDestination/${column.name}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.value}">${column.name}</td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="operationChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Choose a Permission to Add</caption>
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
								items="${incomingUserActionForm.operationOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/incoming/edit/update/${incomingUserActionForm.id}/addOperation/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.id}</td>
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
				<display:table id="policy"
					name="${incomingUserActionForm.incomingPolicies}" requestURI=""
					class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="policy">
							<caption style="white-space: nowrap;">No Associated Data Policies <a href="#"
								onClick="hideChoosers('policyChooser');toggle_in_place(event,'policyChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<display:column property="id" sortable="true" title="Name" />
					<display:column property="comment" sortable="true" title="Comment" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deletePolicy/<c:out value="${policy.id}"/>','<bean:message key="ecpds.incoming.deletePolicy.warning" arg0="${policy.id}" arg1="${incomingUserActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Associated Data Policies <a
							href="#"
							onClick="hideChoosers('policyChooser');toggle_in_place(event,'policyChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
			</c:if></td>
	</tr>
	<tr>
		<td><c:if test="${isInsert != 'true'}">
				<display:table id="destination"
					name="${incomingUserActionForm.destinations}" requestURI=""
					class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="destination">
							<caption style="white-space: nowrap;">No Associated Destinations <a href="#"
								onClick="hideChoosers('destinationChooser');toggle_in_place(event,'destinationChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<display:column property="name" sortable="true" title="Name" />
					<display:column property="comment" sortable="true" title="Comment" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deleteDestination/<c:out value="${destination.name}"/>','<bean:message key="ecpds.incoming.deleteDestination.warning" arg0="${destination.name}" arg1="${incomingUserActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Associated Destinations <a
							href="#"
							onClick="hideChoosers('destinationChooser');toggle_in_place(event,'destinationChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
			</c:if></td>
	</tr>
	<tr>
		<td><c:if test="${isInsert != 'true'}">
				<display:table id="operation"
					name="${incomingUserActionForm.operations}" requestURI=""
					class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="operation">
							<caption style="white-space: nowrap;">No Associated Permissions <a href="#"
								onClick="hideChoosers('operationChooser');toggle_in_place(event,'operationChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<display:column property="id" sortable="true" title="Name" />
					<display:column property="comment" sortable="true" title="Comment" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/deleteOperation/<c:out value="${operation.name}"/>','<bean:message key="ecpds.incoming.deleteOperation.warning" arg0="${operation.name}" arg1="${incomingUserActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Associated Permissions <a
							href="#"
							onClick="hideChoosers('operationChooser');toggle_in_place(event,'operationChooser','');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
			</c:if></td>
	<tr>
		<td><c:if test="${isInsert != 'true'}">
				<display:table id="mySession"
					name="${incomingUserActionForm.incomingUser.incomingConnections}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="mySession">
							<caption style="white-space: nowrap;">
								No Current Sessions
							</caption>
						</table>
					</display:setProperty>
					<display:column title="Mover Name">${mySession.dataMoverName}</display:column>
					<display:column title="Protocol">${mySession.protocol}</display:column>
					<display:column title="Remote Address">${mySession.remoteIpAddress}</display:column>
					<display:column title="Duration">${mySession.formatedDuration}</display:column>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="incoming.basepath"/>/edit/update/<c:out value="${incomingUserActionForm.id}"/>/closeSession/<c:out value="${mySession.id}"/>','<bean:message key="ecpds.incoming.disconnectOperation.warning" arg0="${mySession.login}"
   	arg1="${mySessiony.dataMoverName}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.disconnect"
								altKey="button.disconnect" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Current Sessions</display:caption>
				</display:table>
			</c:if></td>

	</tr>
</table>

<script>
	var editorProperties = getEditorProperties(false, true, "userData", "crystal");

	// Get the completions from the bean!      		
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
	
	// Lets' populate the help tab!
	$(document).ready(function() {
		$('#tabs-3').html(getHelpHtmlContent(completions, 'Available Options for this Data User'));
	});
    
	// Create a custom completer
	var customCompleter = {
  		getCompletions: function(editor, session, pos, prefix, callback) {
      		// Get the current line of text
      		var line = session.getLine(editor.getCursorPosition().row);

   			completions.forEach(function(completion) {
      			completion.value = completion.caption + ' = ""';
    		});
    		
      		var matchingCompletions = completions.filter(function(completion) {
      			return !checkIfExist(editor, completion.value) && (line.length === 0 || completion.value.startsWith(line));
    		}).map(function(completion) {
      			completion.value = prefix + completion.value.substring(line.length);
      			return completion;
    		});

      		if (matchingCompletions.length > 0) {
        		callback(null, matchingCompletions);
      		} else {
        		callback(null, []);
      		}
    	}
	};

	// Set the custom completer for the editor
	editorProperties.completers = [customCompleter];
	   	
	// Add a click event listener to the properties editor
    editorProperties.addEventListener("changeSelection", function (event) {
    	editorProperties.session.setAnnotations(
    		getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	checkEachLine(editorProperties);
    });
    
	// Track changes in the editor's content
	editorProperties.getSession().on("change", function(e) {
  		// Check if the change was due to completion
  		if (e.action === "insert" && e.lines.length == 1 && e.lines[0] !== '"' && e.lines[0].endsWith('"')) {
    		setTimeout(function() {
				// Move the cursor between the double quotes
    			editorProperties.moveCursorTo(e.end.row, e.end.column - 1);
    			editorProperties.selection.clearSelection();
    		}, 0);
  		}
	});
	
	var textareaProperties = $('textarea[name="userData"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	var editorAuthorizedSSHKeys = getEditorProperties(false, false, "authorizedSSHKeys", "text");

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
		
	function handleTOTPClick(cb) {
		$("#password").prop('readonly', cb.checked);
		$("#buttonPassword").prop('disabled', cb.checked);
	}

	handleTOTPClick(document.getElementById("isSynchronized"));

    function generatePassword() {
    	$("#password").val(getPassword());
    }

    $('#id').bind('keypress', function (event) {
        var regex = new RegExp("^[a-zA-Z0-9._]+$");
        var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
        if (!regex.test(key)) {
            event.preventDefault();
            return false;
        }
    });
</script>
