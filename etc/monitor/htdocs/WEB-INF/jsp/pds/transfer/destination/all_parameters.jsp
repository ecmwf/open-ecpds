<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
#properties {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

#javascript {
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
}
</style>

<table class="fields">

	<tr>
		<th>Owner</th>
		<td>${destination.ecUserName}</td>
	</tr>
	<tr>
		<th>Mail</th>
		<td>${destination.userMail}</td>
	</tr>
	<tr>
		<th>Name</th>
		<td>${destination.name}</td>
	</tr>
	<tr>
		<th>Type</th>
		<td>${destination.typeText}</td>
	</tr>
	<tr>
		<th>Comment</th>
		<td>${destination.comment}</td>
	</tr>
	<tr>
		<th>On Host Failure</th>
		<td>${destination.onHostFailureText}</td>
	</tr>
	<tr>
		<th>If Target Exists</th>
		<td>${destination.ifTargetExistText}</td>
	</tr>
	<tr>
		<th>Delete From Spool</th>
		<td>${destination.keepInSpoolText}</td>
	</tr>
	<tr>
		<th>Country</th>
		<td>${destination.country.name}</td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

	<tr>
		<th>Filter</th>
		<td>${destination.filterName}</td>
	</tr>
	<tr>
		<th>Host For Source</th>
		<td>${destination.hostForSource.nickName}</td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

	<tr>
		<th>Group By Date</th>
		<td><c:if test="${destination.groupByDate}">yes</c:if> <c:if
				test="${!destination.groupByDate}">no</c:if></td>
	</tr>
	<tr>
		<th>Date Format</th>
		<td>${destination.dateFormat}</td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

	<tr>
		<th>Max Connections</th>
		<td>${destination.maxConnections}</td>
	</tr>
	<tr>
		<th>Retry Count</th>
		<td>${destination.retryCount}</td>
	</tr>
	<tr>
		<th>Retry Frequency</th>
		<td>${destination.formattedRetryFrequency}</td>
	</tr>
	<tr>
		<th>Max Start</th>
		<td>${destination.maxStart}</td>
	</tr>
	<tr>
		<th>Max Requeue</th>
		<td>${destination.maxRequeue}</td>
	</tr>
	<tr>
		<th>Max Pending</th>
		<td>${destination.maxPending}</td>
	</tr>
	<tr>
		<th>Max File Size</th>
		<td>${destination.maxFileSize}</td>
	</tr>
	<tr>
		<th>Start Frequency</th>
		<td>${destination.formattedStartFrequency}</td>
	</tr>
	<tr>
		<th>Reset Frequency</th>
		<td>${destination.formattedResetFrequency}</td>
	</tr>
	<tr>
		<th>Max Inactivity</th>
		<td>${destination.formattedMaxInactivity}</td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

	<tr>
		<th>Mail on Update</th>
		<td><c:if test="${destination.mailOnUpdate}">yes</c:if> <c:if
				test="${!destination.mailOnUpdate}">no</c:if></td>
	</tr>
	<tr>
		<th>Mail on Start</th>
		<td><c:if test="${destination.mailOnStart}">yes</c:if> <c:if
				test="${!destination.mailOnStart}">no</c:if></td>
	</tr>
	<tr>
		<th>Mail on End</th>
		<td><c:if test="${destination.mailOnEnd}">yes</c:if> <c:if
				test="${!destination.mailOnEnd}">no</c:if></td>
	</tr>
	<tr>
		<th>Mail on Error</th>
		<td><c:if test="${destination.mailOnError}">yes</c:if> <c:if
				test="${!destination.mailOnError}">no</c:if></td>
	</tr>
	<tr>
		<th>Restart If Dirty</th>
		<td><c:if test="${destination.stopIfDirty}">yes</c:if> <c:if
				test="${!destination.stopIfDirty}">no</c:if></td>
	</tr>
	<tr>
		<th>Acquisition</th>
		<td><c:if test="${destination.acquisition}">yes</c:if> <c:if
				test="${!destination.acquisition}">no</c:if></td>
	</tr>
	<tr>
		<th>Enabled</th>
		<td><c:if test="${destination.active}">yes</c:if> <c:if
				test="${!destination.active}">
				<font color="red">no</font>
			</c:if></td>
	</tr>
	<tr>
		<th>Show In Monitors</th>
		<td><c:if test="${destination.showInMonitors}">yes</c:if> <c:if
				test="${!destination.showInMonitors}">
				<font color="red">no</font>
			</c:if></td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

	<tr>
		<th>Options</th>
		<td>
			<div id="tabs">
				<ul>
					<li><a href="#tabs-1">Properties</a></li>
					<li><a href="#tabs-2">JavaScript</a></li>
					<li><a href="#tabs-3">Help</a></li>
				</ul>
				<div id="tabs-1">
					<pre id="properties">
						<c:out value="${destination.properties}" />
					</pre>
					<textarea id="properties" name="properties" style="display: none;"></textarea>
				</div>
				<div id="tabs-2">
					<pre id="javascript">
						<c:out value="${destination.javascript}" />
					</pre>
					<textarea id="javascript" name="javascript" style="display: none;"></textarea>
				</div>
				<div id="tabs-3" class="scrollable-tab">
				</div>
			</div>
		</td>
	</tr>

	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>

	<tr>
		<td colspan="2"></td>
	</tr>

</table>

<script>
	var editorProperties = getEditorProperties(true, false, "properties", "crystal");
	
	// Get the completions from the bean!      		
    var completions = [
    	${destination.completions}
    ];
	
	// Lets' populate the help tab!
	$(document).ready(function() {
		$('#tabs-3').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
	});

    // Call the function to process each line
    checkEachLine(editorProperties);

	// Add a click event listener to the properties editor
    editorProperties.addEventListener("changeSelection", function (event) {
    	editorProperties.session.setAnnotations(
    		getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    });
	
	var editorJavascript = getEditorProperties(true, false, "javascript", "javascript");

	makeResizable(editorProperties);
	makeResizable(editorJavascript);
	$("#tabs").tabs();
	$("#tabs").tabs("option", "active", 0);
</script>
