<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>

<style>
#dir {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

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

#maxConnectionsHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#retryCountHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#retryFrequencyHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
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

<table class="fields" border=0>

	<tiles:useAttribute id="actionFormName" name="action.form.name"
		classname="java.lang.String" />
	<tiles:useAttribute name="isInsert" classname="java.lang.String" />

	<c:if test="${isInsert != 'true'}">
		<tr>
			<th>Id</th>
			<td colspan="2">${requestScope[actionFormName].name}<html:hidden
					property="name" /></td>
		</tr>
	</c:if>

	<tr>
		<th>Comment</th>
		<td><html:text property="comment" /></td>
	<tr>
		<th>Transfer Protocol</th>
		<td><select
			title="Select the Transfer Protocol to connect to the remote site"
			id="transferMethod" name="transferMethod">
				<c:forEach var="method"
					items="${requestScope[actionFormName].transferMethodOptions}">
					<c:choose>
						<c:when
							test="${method.name == requestScope[actionFormName].transferMethod}">
							<option value="${method.name}" selected="selected">${method.name}
								(${method.ecTransModuleName}.*)</option>
						</c:when>
						<c:otherwise>
							<option value="${method.name}">${method.name}
								(${method.ecTransModuleName}.*)</option>
						</c:otherwise>
					</c:choose>
				</c:forEach>
		</select></td>
	</tr>

	<tr>
		<th>Hostname/IP</th>
		<td><input title="DNS name or IP address" property="host"
			id="host" name="host" type="text"
			value="${requestScope[actionFormName].host}">&nbsp;(please
			use letters, digits, '-' and '.' only)</td>
	</tr>

	<tr>
		<th>Nickname</th>
		<td><input title="As it will appear on the Destination page"
			property="nickName" id="nickName" name="nickName" type="text"
			value="${requestScope[actionFormName].nickName}">&nbsp;(please
			use letters, digits, '_' and '-' only)</td>
	</tr>
	<tr>
		<th>Login</th>
		<td><html:text property="login" /></td>
	</tr>
	<tr>
		<th>Password</th>
		<td><html:text property="passwd" /></td>
	</tr>

	<c:if test="${isInsert == 'true'}">
		<tr>
			<th>Type</th>
			<td><select title="Select the Host Type" id="type" name="type">
					<c:forEach var="hostType"
						items="${requestScope[actionFormName].typeOptions}">
						<c:choose>
							<c:when
								test="${hostType.name == requestScope[actionFormName].type}">
								<option value="${hostType.name}" selected="selected">${hostType.name}</option>
							</c:when>
							<c:otherwise>
								<option value="${hostType.name}">${hostType.name}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
			</select></td>
		</tr>
	</c:if>

	<c:if test="${isInsert == 'false'}">
		<tr>
			<th>Type</th>
			<td>${requestScope[actionFormName].type}</td>
			<html:hidden property="type" />
		</tr>
	</c:if>


	<tr>
		<th>Label</th>
		<td><bean:define id="networks" name="hostActionForm"
				property="networkOptions" type="java.util.Collection" /> <html:select
				title="This is only used for the monitoring display (shown in the Host column)"
				property="networkCode">
				<html:options collection="networks" property="name"
					labelProperty="value" />
			</html:select></td>
	</tr>

	<tr>
		<th>Transfer Group</th>
		<td><bean:define id="groups" name="hostActionForm"
				property="transferGroupOptions" type="java.util.Collection" /> <html:select
				title="From which Data Movers this host can be accessed?"
				property="transferGroup">
				<html:options collection="groups" property="name"
					labelProperty="name" />
			</html:select>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Max Connections</th>
		<td>
			<div id="maxConnectionsSlider" style="width: 210px; margin: 6px;">
				<input type="hidden" name="maxConnections" id="maxConnections">
				<div
					title="Maximum number of parallel connections authorized at a time on this specific host"
					id="maxConnectionsHandle" class="ui-slider-handle"></div>
			</div>
		</td>
	</tr>

	<tr>
		<th>Retry Count</th>
		<td>
			<div id="retryCountSlider" style="width: 210px; margin: 6px;">
				<input type="hidden" name="retryCount" id="retryCount">
				<div
					title="Maximum number of consecutive unsuccessful transfers before to move to the next host and declare this host not valid"
					id="retryCountHandle" class="ui-slider-handle"></div>
			</div>
		</td>
	</tr>

	<tr>
		<th>Retry Frequency</th>
		<td>
			<div id="retryFrequencySlider" style="width: 210px; margin: 6px;">
				<input type="hidden" name="retryFrequency" id="retryFrequency">
				<div
					title="Time in seconds to wait before to retry a failed transfer on this host"
					id="retryFrequencyHandle" class="ui-slider-handle"></div>
			</div>
		</td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Data Compression</th>
		<td><bean:define id="filters" name="hostActionForm"
				property="filterNameOptions" type="java.util.Collection	" /> <html:select
				property="filterName">
				<html:options collection="filters" property="name"
					labelProperty="name" />
			</html:select></td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<!--
<tr><th>Directory</th><td colspan="2"><pre><c:out value="${requestScope[actionFormName].dir}" escapeXml="true"/></pre></td></tr>
-->

	<tr>
		<th>Directory</th>
		<td colspan="2">
			<div id='dirType'>
				<input type='radio' id='istext' name='dirType' />Plain Text <input
					type='radio' id='isjs' name='dirType' />JavaScript <input
					type='radio' id='ispython' name='dirType' />Python
			</div> <pre id="dir">
				<c:out value="${requestScope[actionFormName].dir}" />
			</pre> <textarea id="dir" name="dir" style="display: none;"></textarea>
			<button type="button" id="formatDir" name="formatDir"
				onclick="formatSource(editorDir); return false">Format</button>
			<button type="button" id="testDir" name="testDir"
				onclick="testSource(editorDir); return false">Test</button>
			<button type="button" onclick="clearSource(editorDir); return false">Clear</button>

			<select name="dirParametersAcq" id="dirParametersAcq" size="1">
				<option disabled selected>Insert parameter at cursor</option>
				<optgroup label="Host">
					<option>$host[name]</option>
					<option>$host[comment]</option>
					<option>$host[host]</option>
					<option>$host[login]</option>
					<option>$host[passwd]</option>
					<option>$host[userMail]</option>
					<option>$host[networkCode]</option>
					<option>$host[networkName]</option>
					<option>$host[nickname]</option>
				</optgroup>
				<optgroup label="Transfer Method">
					<option>$transferMethod[name]</option>
					<option>$transferMethod[comment]</option>
				</optgroup>
		</select> <select name="dirParametersDiss" id="dirParametersDiss" size="1">
				<option disabled selected>Insert parameter at cursor</option>
				<optgroup label="Country">
					<option>$country[name]</option>
					<option>$country[iso]</option>
				</optgroup>
				<optgroup label="Data File">
					<option>$dataFile[timeStep]</option>
					<option>$dataFile[arrivedTime]</option>
					<option>$dataFile[id]</option>
					<option>$dataFile[original]</option>
					<option>$dataFile[source]</option>
					<option>$dataFile[formatSize]</option>
					<option>$dataFile[size]</option>
					<option>$dataFile[timeBase]</option>
					<option>$dataFile[timeFile]</option>
					<option>$dataFile[metaTime]</option>
					<option>$dataFile[metaStream]</option>
					<option>$dataFile[checksum]</option>
				</optgroup>
				<optgroup label="Data Transfer">
					<option>$dataTransfer[target]</option>
					<option>$dataTransfer[id]</option>
					<option>$dataTransfer[comment]</option>
					<option>$dataTransfer[identity]</option>
					<option>$dataTransfer[priority]</option>
					<option>$dataTransfer[scheduled]</option>
					<option>$dataTransfer[statusCode]</option>
					<option>$dataTransfer[name]</option>
					<option>$dataTransfer[path]</option>
					<option>$dataTransfer[parent]</option>
				</optgroup>
				<optgroup label="Destination">
					<option>$destination[name]</option>
					<option>$destination[comment]</option>
					<option>$destination[userMail]</option>
				</optgroup>
				<optgroup label="ECtrans Module">
					<option>$ectransModule[name]</option>
				</optgroup>
				<optgroup label="Host">
					<option>$host[name]</option>
					<option>$host[comment]</option>
					<option>$host[host]</option>
					<option>$host[login]</option>
					<option>$host[passwd]</option>
					<option>$host[userMail]</option>
					<option>$host[networkCode]</option>
					<option>$host[networkName]</option>
					<option>$host[nickname]</option>
				</optgroup>
				<optgroup label="Transfer Group">
					<option>$transferGroup[name]</option>
					<option>$transferGroup[comment]</option>
				</optgroup>
				<optgroup label="Transfer Method">
					<option>$transferMethod[name]</option>
					<option>$transferMethod[comment]</option>
				</optgroup>
				<optgroup label="Transfer Server">
					<option>$transferServer[name]</option>
					<option>$transferServer[host]</option>
					<option>$transferServer[port]</option>
					<option>$moverName</option>
				</optgroup>
		</select>
		</td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Options</th>
		<td colspan="2">
			<div id="tabs">
				<ul>
					<li><a href="#tabs-1">Properties</a></li>
					<li><a href="#tabs-2">JavaScript</a></li>
					<li><a href="#tabs-3">Help</a></li>
				</ul>
				<div id="tabs-1">
					<pre id="properties">
						<c:out value="${requestScope[actionFormName].properties}" />
					</pre>
					<textarea id="properties" name="properties" style="display: none;"></textarea>
					<button type="button"
						onclick="formatSource(editorProperties); return false">Format</button>
					<button type="button"
						onclick="clearSource(editorProperties); return false">Clear</button>
				</div>
				<div id="tabs-2">
					<pre id="javascript">
						<c:out value="${requestScope[actionFormName].javascript}" />
					</pre>
					<textarea id="javascript" name="javascript" style="display: none;"></textarea>
					<button type="button"
						onclick="formatSource(editorJavascript); return false">Format</button>
					<button type="button"
						onclick="testSource(editorJavascript); return false">Test</button>
					<button type="button"
						onclick="clearSource(editorJavascript); return false">Clear</button>
				</div>
				<div id="tabs-3" class="scrollable-tab"></div>
			</div>
		</td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Automatic Location</th>
		<td colspan="2"><html:checkbox
				title="Try to get the latitude/longitude from the IP address"
				property="automaticLocation" /></td>
	</tr>
	<tr>
		<th>Latitude</th>
		<td colspan="2"><html:text property="latitude" /></td>
	</tr>
	<tr>
		<th>Longitude</th>
		<td colspan="2"><html:text property="longitude" /></td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Check</th>
		<td colspan="2"><html:checkbox property="check" />
			<content:icon key="icon.help.medium"
				title="Allow automatically checking the host by transferring a test file on a regular schedule."
				writeFullTag="true" /></td>
	</tr>
	<tr>
		<th>Notify Once</th>
		<td colspan="2"><html:checkbox property="notifyOnce" />
			<content:icon key="icon.help.medium"
				title="Only notify once for consecutive errors." writeFullTag="true" /></td>
	</tr>
	<tr>
		<th>Check Frequency</th>
		<td colspan="2"><html:text property="checkFrequency" />
			<content:icon key="icon.help.medium"
				title="Define the delay in ms between two checks."
				writeFullTag="true" /></td>
	</tr>
	<tr>
		<th>Check Filename</th>
		<td colspan="2"><html:text property="checkFilename" />
			<content:icon key="icon.help.medium"
				title="Define the name for the temporary test file."
				writeFullTag="true" /></td>
	</tr>
	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Acquisition Frequency</th>
		<td colspan="2"><html:text
				title="If this host is for acquisition then define the delay in ms between two listings of remote files"
				property="acquisitionFrequency" /></td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Owner</th>
		<td><bean:define id="users" name="hostActionForm"
				property="ownerOptions" type="java.util.Collection" /> <html:select
				title="For the record" property="owner">
				<html:options collection="users" property="name"
					labelProperty="comment" />
			</html:select></td>
	</tr>
	<tr>
		<th>Mail Address</th>
		<td><html:text
				title="Email address used when sending notifications"
				property="userMail" /></td>
	</tr>
	<tr>
		<th>Mail on Success</th>
		<td colspan="2"><html:checkbox property="mailOnSuccess" /></td>
	</tr>
	<tr>
		<th>Mail on Error</th>
		<td colspan="2"><html:checkbox property="mailOnError" /></td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Enabled</th>
		<td colspan="2"><html:checkbox property="active" /></td>
	</tr>

</table>

<script>
	var editorDir = getEditorProperties(false, false, "dir", "toml");
	var editorProperties = getEditorProperties(false, true, "properties", "crystal");
	
	// Get the completions from the bean!
	var transferModuleNames = [${requestScope[actionFormName].transferModuleNames}];
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
    
	$(document).ready(function() {
		populateHelpTab();
	});
	
	function isInvalidModuleName(moduleName) {
		var hostType = getHostType();
		return ((!["Acquisition", "Source"].includes(hostType) && moduleName === "retrieval")
  			|| (!["Dissemination", "Proxy", "Backup", "Replication"].includes(hostType) && moduleName === "upload")
  			|| (hostType !== "Acquisition"  && moduleName === "acquisition")
  			|| (hostType !== "Proxy" && moduleName === "proxy"));
	}
    
	// Lets' populate the help tab!
	function populateHelpTab() {
		var transferModuleName = getTransferModuleName();
		$('#tabs-3').html(getHelpHtmlContent(
				completions.filter(function(item) {
	           		var moduleName = item.caption.split(".")[0];
	       	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
	       	    		return false;
	       	    	} else {
	       	    		return !isInvalidModuleName(moduleName);
	       	    	}
	    	    }), 'Available Options for Host of Type ' + getHostType() + ' with Transfer Method ' + getTransferMethodName()))
	}
    
	// Create a custom completer
	var customCompleter = {
  		getCompletions: function(editor, session, pos, prefix, callback) {
      		// Get the current line of text
      		var line = session.getLine(editor.getCursorPosition().row);
   			completions.forEach(function(completion) {
      			completion.value = completion.caption + ' = ""';
    		});
           	var selectedModuleName = getTransferModuleName();
           	var matchingCompletions = completions.filter(function(completion) {
  				var moduleName = completion.caption.split(".")[0];
   	    		if (isInvalidModuleName(moduleName))
       	    		return false;
      			if (!checkIfExist(editor, completion.value) && (line.length === 0 || completion.value.startsWith(line))) {
           	    	return !(selectedModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== selectedModuleName);
      			}
      			return false;
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
	
    function extractModuleName(inputString) {
  		const regex = /\((.*?)\)/;
  		const matchResult = inputString.match(regex);
  		if (matchResult && matchResult[1]) {
    		return matchResult[1].split(".")[0];
  		} else {
    		return "";
  		}
	}
    
    // Get the selected host type
    function getHostType() {
       	var selectElement = document.getElementById("type");
       	if (selectElement && selectElement.tagName === "SELECT") {
       		var selectedOption = selectElement.options[selectElement.selectedIndex];    		
       		return selectedOption ? selectedOption.textContent : "${requestScope[actionFormName].type}";
       	} else {
       		return document.querySelector('input[name="type"]').value;
       	}
	}

    // Get the selected transfer module name
    function getTransferModuleName() {
       	var selectElement = document.getElementById("transferMethod");
       	var selectedOption = selectElement.options[selectElement.selectedIndex];    		
       	var methodValue = selectedOption ? selectedOption.textContent : "${requestScope[actionFormName].transferMethodValue}";
       	var moduleName = extractModuleName(methodValue);
       	return moduleName;
    }
    
    // Get the selected transfer module name
    function getTransferMethodName() {
       	var selectElement = document.getElementById("transferMethod");
       	var selectedOption = selectElement.options[selectElement.selectedIndex];    		
       	var methodName = selectedOption ? selectedOption.value : "${requestScope[actionFormName].transferMethod}";
       	return methodName;
    }

	// Overwrite the original method to deal with the specificities of the host
	function getAnnotations(aceEditor, row) {
        return getAnnotationsWith(aceEditor, row, getTransferModuleName());
    }

	function getAnnotationsWith(aceEditor, row, transferModuleName) {
    	const pattern = /^[^\s.]+\.[^\s.]+\s*=(.*)$/;
  		var currentLine = aceEditor.session.getLine(row).replace(/^\t+/, '');
    	for (var j = 0; j < completions.length; j++) {
    		var tipObject = completions[j];
    		var withoutSpace = currentLine.replace(/\s/g, "");
       		if (withoutSpace === tipObject.caption || withoutSpace.startsWith(tipObject.caption + "=")) {
       		    var moduleName = tipObject.caption.split(".")[0];
       	    	var tipsText;
       	    	var tipsType;
       	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
       	    		tipsText = "Ignored as \"" + moduleName + "\" is not the selected transfer method. The selected transfer method is \"" + transferModuleName + "\".";
       	    		tipsType = "warning"
       	    	} else {
       	    		if (isInvalidModuleName(moduleName)) {
               	    	tipsText = "Ignored as \"" + moduleName + "\" parameters are not valid for " + getHostType() + " hosts.";
           	    		tipsType = "warning";
          			} else {
           	    		var error = checkValueForType(tipObject.type, tipObject.choices, currentLine);
           	    		if (error != null) {
           	    			tipsText = error;
           	    			tipsType = "error";
           	    		} else {
           	    			tipsText = tipObject.tips;
           	    			tipsType = "info";
           	    		}
          			}
       	    	}
        		return [{
           			row: row,
           		    column: 0,
           		    text: tipsText,
           			type: tipsType,
           	   	 	module: moduleName
           		}];
        	}
    	}
    	// If no match found let's put a tips if it looks like a parameter!
    	if (currentLine.length > 0 && pattern.test(currentLine) && !currentLine.startsWith('#')) {
    		return [{
        		row: row,
            	column: 0,
            	text: "This option is not recognized.",
            	type: "error"
        	}];
    	}
    	return [];	
	}
   	
	document.getElementById("transferMethod").addEventListener("change", function() {
    	editorProperties.session.setAnnotations(
    		getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	$("#tabs").tabs("option", "active", 0);
    	checkEachLine(editorProperties);
    	populateHelpTab();
	});
	
   	var selectElement = document.getElementById("type");
   	if (selectElement && selectElement.tagName === "SELECT") {
		document.getElementById("type").addEventListener("change", function() {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    		$("#tabs").tabs("option", "active", 0);
    		checkEachLine(editorProperties);
    		populateHelpTab();
		});
   	};

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
	
	var editorJavascript = getEditorProperties(false, false, "javascript", "javascript");

	var textareaDir = $('textarea[name="dir"]');
	textareaDir.closest('form').submit(
			function() {
				var type;
				if ($("#istext").is(":checked")) {
					type = "";
				} else if ($("#ispython").is(":checked")) {
					type = "python:";
				} else {
					type = "js:"
				}
				textareaDir.val((type.length > 0 ? "$(" + type : "")
						+ editorDir.getSession().getValue()
						+ (type.length > 0 ? ")" : ""));
			});

	var textareaProperties = $('textarea[name="properties"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	var textareaJavascript = $('textarea[name="javascript"]');
	textareaJavascript.closest('form').submit(function() {
		textareaJavascript.val(editorJavascript.getSession().getValue());
	});

	makeResizable(editorDir);
	makeResizable(editorProperties);
	makeResizable(editorJavascript);

	$('select[name="type"]').on(
			'change',
			function() {
				var str = $(this).find(":selected").val();
				$('#dirParametersDiss').prop("style",
						str != "Dissemination" ? "display: none;" : "");
				$('#dirParametersAcq').prop("style",
						str != "Acquisition" ? "display: none;" : "");
			});

	$('#is' + getEditorType(editorDir)).prop('checked', true);
	$("#dirType").on('change', function() {
		if ($("#istext").is(":checked")) {
			editorDir.session.setMode("ace/mode/toml");
			$('#formatDir').prop('style', "display: none;");
			$('#testDir').prop('style', "display: none;");
		} else if ($("#ispython").is(":checked")) {
			editorDir.session.setMode("ace/mode/python");
			$('#formatDir').prop('style', "display: none;");
			$('#testDir').prop('style', "display: none;");
		} else {
			editorDir.session.setMode("ace/mode/javascript");
			$('#formatDir').prop('style', "");
			$('#testDir').prop('style', "");
		}
	}).triggerHandler('change');

	var type = "${requestScope[actionFormName].type}";
	$('#dirParametersDiss').prop("style",
			type == "" || type == "Dissemination" ? "" : "display: none;");
	$('#dirParametersAcq').prop("style",
			type == "Acquisition" ? "" : "display: none;");
	$('#dirParametersDiss').on('change', function() {
		var str = $(this).find(":selected").val();
		$(this).prop("selectedIndex", 0);
		editorDir.insert(str);
	});
	$('#dirParametersAcq').on('change', function() {
		var str = $(this).find(":selected").val();
		$(this).prop("selectedIndex", 0);
		editorDir.insert(str);
	});
	$("#tabs").tabs();
	$("#tabs").tabs("option", "active", 0);
	$(function() {
		$("#maxConnectionsSlider")
				.slider(
						{
							min : 1,
							max : 150,
							value : <c:out value="${requestScope[actionFormName].maxConnections}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxConnectionsHandle").text(value);
								$("#maxConnections").val(value);
							},
							slide : function(event, ui) {
								$("#maxConnectionsHandle").text(ui.value);
								$("#maxConnections").val(ui.value);
							}
						});
		$("#retryCountSlider")
				.slider(
						{
							min : 1,
							max : 20,
							value : <c:out value="${requestScope[actionFormName].retryCount}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#retryCountHandle").text(value);
								$("#retryCount").val(value);
							},
							slide : function(event, ui) {
								$("#retryCountHandle").text(ui.value);
								$("#retryCount").val(ui.value);
							}
						});
		$("#retryFrequencySlider")
				.slider(
						{
							min : 0,
							max : 600,
							step : 5,
							value : <c:out value="${requestScope[actionFormName].retryFrequency/1000}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#retryFrequencyHandle").text(value + " s");
								$("#retryFrequency").val(value * 1000);
							},
							slide : function(event, ui) {
								$("#retryFrequencyHandle")
										.text(ui.value + " s");
								$("#retryFrequency").val(ui.value * 1000);
							}
						});
	});
	$('#nickName').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9_-]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
	$('#host').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9-.]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
</script>
