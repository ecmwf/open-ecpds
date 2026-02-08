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
		if (layerName != 'destinationChooser')
			hide('destinationChooser');
	}
</script>

<style>
#data {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

.scrollable-tab {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
  	overflow-y: scroll;
</style>

<table border=0>
	<tr>
		<td valign="top">
			<table class="fields">
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Name</th>
						<td>${incomingPolicyActionForm.id}<html:hidden property="id" /></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Name</th>
						<td><input id="id" name="id" type="text">&nbsp;(please
							use letters, digits, '_' and '.' only)</td>
					</tr>
				</c:if>
				<tr>
					<th>Comment</th>
					<td><html:text property="comment" /></td>
				</tr>
				<tr>
					<th>Enabled</th>
					<td><html:checkbox property="active" /></td>
				</tr>
				<tr>
					<th>Options</th>
					<td colspan="2">
						<div id="tabs">
							<ul>
								<li><a href="#tabs-1">Properties</a></li>
								<li><a href="#tabs-2">Help</a></li>
							</ul>
							<div id="tabs-1">
								<pre id="data">
									<c:out value="${incomingPolicyActionForm.data}" />
								</pre>
								<textarea id="data" name="data" style="display: none;"></textarea>
								<button type="button"
									onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button"
									onclick="clearSource(editorProperties); return false">Clear</button>
							</div>
							<div id="tabs-2" class="scrollable-tab">
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
								items="${incomingPolicyActionForm.destinationOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/user/policy/edit/update/${incomingPolicyActionForm.id}/addDestination/${column.name}"><img
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
			</c:if></td>
	</tr>
	<tr>
		<td><c:if test="${isInsert != 'true'}">
				<display:table id="destination"
					name="${incomingPolicyActionForm.destinations}" requestURI=""
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
					<display:column property="id" sortable="true" title="Name" />
					<display:column property="comment" sortable="true" title="Comment" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="policy.basepath"/>/edit/update/<c:out value="${incomingPolicyActionForm.id}"/>/deleteDestination/<c:out value="${destination.name}"/>','<bean:message key="ecpds.policy.deleteDestination.warning" arg0="${destination.name}" arg1="${incomingPolicyActionForm.id}"/>')"><content:icon
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
</table>
<script>
	var editorProperties = getEditorProperties(false, true, "data", "crystal");
	
	// Get the completions from the bean!      		
    var completions = [
    	${incomingPolicyActionForm.completions}
    ];
	
	// Lets' populate the help tab!
	$(document).ready(function() {
		$('#tabs-2').html(getHelpHtmlContent(completions, 'Available Options for this Data Policy'));
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

	var textareaProperties = $('textarea[name="data"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	makeResizable(editorProperties);
	
	$("#tabs").tabs();
	$("#tabs").tabs("option", "active", 0);

	$('#id').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9_.]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
</script>
