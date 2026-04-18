<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>

<style>
table.fields {
	width: 100%;
	min-width: 600px;
}

table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}

#dir {
	width: 100%;
	min-width: 300px;
	height: 375px;
	resize: vertical;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px lightgray;
	border-radius: 4px;
	margin-top: 8px;
	margin-bottom: 4px;
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


.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px lightgray;
	border-radius: 4px;
	padding: 8px;
	position: relative;
}
</style>

<table class="fields" border=0>

	<tiles:useAttribute id="actionFormName" name="action.form.name"
		classname="java.lang.String" />
	<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <tr><td colspan="3">
        <div class="form-info-banner">
            <i class="bi bi-hdd-network text-primary flex-shrink-0"></i>
            Register a new Host for data transfer operations.
        </div>
        </td></tr>
    </c:when>
    <c:otherwise>
        <tr><td colspan="3">
        <div class="form-info-banner">
            <i class="bi bi-hdd-network text-primary flex-shrink-0"></i>
            Edit the Host configuration.
        </div>
        </td></tr>
    </c:otherwise>
</c:choose>



	<c:if test="${isInsert != 'true'}">
		<c:if test="${requestScope[actionFormName].type == 'Proxy'}">
			<div class="alert">
				<b>THIS CONFIGURATION CONTROLS THE BEHAVIOR OF THE PROXY HOST.<br>
				DO NOT MODIFY IT UNLESS YOU FULLY UNDERSTAND THE CONSEQUENCES.<br>
				ANY CHANGES MAY DIRECTLY AFFECT REPLICATION TO THIS PROXY AND<br>
				COULD IMPACT ALL ALLOCATED DESTINATIONS.
				<p>
				PROCEED ONLY IF YOU ARE CERTAIN OF THE IMPACT!!!!</b>
			</div>
		</c:if>
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
		<th>Transfer Protocol <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Select the Transfer Protocol to connect to the remote site" tabindex="0"></i></th>
		<td><select
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
		<th>Hostname/IP <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="DNS hostname or IPv4/IPv6 address" tabindex="0"></i></th>
		<td>
			<div class="d-flex align-items-center gap-2">
				<input id="host" name="host" type="text"
					value="${requestScope[actionFormName].host}"
					oninput="validateHostInput(this)" />
				<span id="hostFeedback"></span>
			</div>
		</td>
	</tr>

	<tr>
		<th>Nickname <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="As it will appear on the Destination page. Alphanumeric only; '-' and '_' may be used as separators (e.g. 'test_file'), not at the start, end, or consecutively." tabindex="0"></i></th>
		<td>
			<div class="d-flex align-items-center gap-2">
				<input id="nickName" name="nickName" type="text"
					value="${requestScope[actionFormName].nickName}"
					pattern="[A-Za-z0-9]+([_\-][A-Za-z0-9]+)*"
					title="Use alphanumeric characters, with '-' or '_' as separators only (e.g. 'test_file', not '_test' or 'test_'). Maximum 128 characters."
					maxlength="128"
					required
					oninput="validatePatternInput(this,'nickNameFeedback')" />
				<span id="nickNameFeedback"></span>
			</div>
		</td>
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
			<th>Type <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Select the Host Type" tabindex="0"></i></th>
			<td><select id="type" name="type">
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
		<th>Label <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="This is only used for the monitoring display (shown in the Host column)" tabindex="0"></i></th>
		<td><bean:define id="networks" name="hostActionForm"
				property="networkOptions" type="java.util.Collection" /> <html:select
				property="networkCode">
				<html:options collection="networks" property="name"
					labelProperty="value" />
			</html:select></td>
	</tr>

	<tr>
		<th>Transfer Group <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="From which Data Movers this host can be accessed?" tabindex="0"></i></th>
		<td><bean:define id="groups" name="hostActionForm"
				property="transferGroupOptions" type="java.util.Collection" /> <html:select
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
		<th>Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry a failed transfer on this host" tabindex="0"></i></th>
		<td>
			<input type="hidden" name="retryFrequency" id="retryFrequency"
				value='<c:out value="${requestScope[actionFormName].retryFrequency}"/>'>
			<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="retryFrequency">
				<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
				<span class="text-muted small">h</span>
				<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
				<span class="text-muted small">m</span>
				<span class="text-muted small ms-1 dur-display"></span>
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
				property="filterName" styleId="filterName">
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
			<div style="max-width:860px">
			<div id='dirType'>
				<input type='radio' id='istext' name='dirType' />Plain Text <input
					type='radio' id='isjs' name='dirType' />JavaScript <input
					type='radio' id='ispython' name='dirType' />Python
			</div> <pre id="dir">
				<c:out value="${requestScope[actionFormName].dir}" />
			</pre> <textarea id="dir" name="dir" style="display: none;"></textarea>
			<div style="display:flex; align-items:center; gap:0.4rem; flex-wrap:wrap; margin-top:0.4rem;">
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
					<option>$dataTransfer[asap]</option>
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
		</div>
		</div>
		</td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Options</th>
		<td colspan="2">
			<div class="accordion" id="hostOptionsAccordion" style="min-width:860px;max-width:860px">
				<div class="accordion-item">
					<h2 class="accordion-header" id="hostAccHeadProperties">
						<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostAccProperties" aria-expanded="false" aria-controls="hostAccProperties">
							Properties
						</button>
					</h2>
					<div id="hostAccProperties" class="accordion-collapse collapse" aria-labelledby="hostAccHeadProperties" data-bs-parent="#hostOptionsAccordion">
						<div class="accordion-body p-2">
							<div class="ace-panel">
								<pre id="properties"><c:out value="${requestScope[actionFormName].properties}" /></pre>
								<textarea id="properties" name="properties" style="display: none;"></textarea>
							</div>
							<div class="d-flex align-items-center gap-2 mt-2">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorProperties); return false">Clear</button>
								<small class="text-muted ms-auto"><i class="bi bi-keyboard"></i> Ctrl+Space for completions</small>
							</div>
						</div>
					</div>
				</div>
				<div class="accordion-item">
					<h2 class="accordion-header" id="hostAccHeadJavascript">
						<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostAccJavascript" aria-expanded="false" aria-controls="hostAccJavascript">
							JavaScript
						</button>
					</h2>
					<div id="hostAccJavascript" class="accordion-collapse collapse" aria-labelledby="hostAccHeadJavascript" data-bs-parent="#hostOptionsAccordion">
						<div class="accordion-body p-2">
							<div class="ace-panel">
								<pre id="javascript"><c:out value="${requestScope[actionFormName].javascript}" /></pre>
								<textarea id="javascript" name="javascript" style="display: none;"></textarea>
							</div>
							<div class="d-flex align-items-center gap-2 mt-2">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorJavascript); return false">Format</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="testSource(editorJavascript); return false">Test</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorJavascript); return false">Clear</button>
							</div>
						</div>
					</div>
				</div>
				<div class="accordion-item">
					<h2 class="accordion-header" id="hostAccHeadHelp">
						<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostAccHelp" aria-expanded="false" aria-controls="hostAccHelp">
							Help
						</button>
					</h2>
					<div id="hostAccHelp" class="accordion-collapse collapse" aria-labelledby="hostAccHeadHelp" data-bs-parent="#hostOptionsAccordion">
						<div class="accordion-body p-2">
							<div id="hostHelpContent" class="scrollable-tab"></div>
						</div>
					</div>
				</div>
			</div>
		</td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
	    <th>Automatic Location <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Try to get the latitude/longitude from the IP address" tabindex="0"></i></th>
	    <td colspan="2">
	        <html:checkbox
	            styleId="automaticLocation"
	            property="automaticLocation" />
	    </td>
	</tr>

	<tr>
	    <th>Latitude (&deg;)</th>
	    <td colspan="2">
	        <html:text property="latitude" styleId="latitudeField" />
	    </td>
	</tr>

	<tr>
	    <th>Longitude (&deg;)</th>
	    <td colspan="2">
	        <html:text property="longitude" styleId="longitudeField" />
	    </td>
	</tr>

	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Check <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Allow automatically checking the host by transferring a test file on a regular schedule." tabindex="0"></i></th>
		<td colspan="2"><html:checkbox property="check" styleId="checkFlag" onchange="toggleCheckRows()" /></td>
	</tr>
	<tr id="notifyOnceRow">
		<th>Notify Once <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Only notify once for consecutive errors." tabindex="0"></i></th>
		<td colspan="2"><html:checkbox property="notifyOnce" /></td>
	</tr>
	<tr id="checkFrequencyRow">
		<th>Check Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay in ms between two checks." tabindex="0"></i></th>
		<td colspan="2">
			<input type="hidden" name="checkFrequency" id="checkFrequency"
				value='<c:out value="${requestScope[actionFormName].checkFrequency}"/>'>
			<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="checkFrequency">
				<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
				<span class="text-muted small">h</span>
				<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
				<span class="text-muted small">m</span>
				<span class="text-muted small ms-1 dur-display"></span>
			</div>
		</td>
	</tr>
	<tr id="checkFilenameRow">
		<th>Check Filename <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the name for the temporary test file." tabindex="0"></i></th>
		<td colspan="2"><html:text property="checkFilename" /></td>
	</tr>
	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr id="acquisitionFrequencyRow">
		<th>Acquisition Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If this host is for acquisition then define the delay in ms between two listings of remote files" tabindex="0"></i></th>
		<td colspan="2">
			<input type="hidden" name="acquisitionFrequency" id="acquisitionFrequency"
				value='<c:out value="${requestScope[actionFormName].acquisitionFrequency}"/>'>
			<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="acquisitionFrequency">
				<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
				<span class="text-muted small">h</span>
				<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
				<span class="text-muted small">m</span>
				<span class="text-muted small ms-1 dur-display"></span>
			</div>
		</td>
	</tr>

	<tr id="acquisitionFrequencySpacer">
		<td colspan="3">&nbsp;</td>
	</tr>

	<tr>
		<th>Owner <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="For the record" tabindex="0"></i></th>
		<td><bean:define id="users" name="hostActionForm"
				property="ownerOptions" type="java.util.Collection" /> <html:select
				property="owner">
				<html:options collection="users" property="name"
					labelProperty="comment" />
			</html:select></td>
	</tr>
	<tr>
		<th>Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Email address used when sending notifications" tabindex="0"></i></th>
		<td>
			<div class="d-flex align-items-center gap-2">
				<input type="email" name="userMail" id="userMailInput"
					value='<c:out value="${requestScope[actionFormName].userMail}"/>'
					oninput="validateMailInput(this); toggleMailRows()" />
				<span id="userMailFeedback"></span>
			</div>
		</td>
	</tr>
	<tr id="mailOnSuccessRow">
		<th>Mail on Success</th>
		<td colspan="2"><html:checkbox property="mailOnSuccess" /></td>
	</tr>
	<tr id="mailOnErrorRow">
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
	editorProperties.setOptions({minLines: 10, maxLines: 20});
	
	// Get the completions from the bean!
	var transferModuleNames = [${requestScope[actionFormName].transferModuleNames}];
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
    
	$(document).ready(function() {
		populateHelpTab();
		toggleCheckRows();
		toggleAcquisitionRow();
		toggleMailRows();

		// Compression icon next to select
		(function() {
			var ICONS = {
				'zip':    'bi-file-zip',
				'gzip':   'bi-file-earmark-zip',
				'lzma':   'bi-box-seam',
				'bzip2a': 'bi-archive',
				'lbzip2': 'bi-cpu',
				'lz4':    'bi-lightning',
				'snappy': 'bi-lightning-charge',
				'zstd':   'bi-stack'
			};
			var $sel = $('#filterName');
			var $icon = $('<i class="bi text-muted ms-2" style="font-size:1.2em;vertical-align:middle"></i>');
			$sel.after($icon);
			function updateIcon() {
				var cls = ICONS[$sel.val()];
				if (cls) { $icon.attr('class', 'bi ' + cls + ' text-muted ms-2').css('display', 'inline'); }
				else { $icon.hide(); }
			}
			$sel.on('change', updateIcon);
			updateIcon();
		})();
	});
	
	function toggleCheckRows() {
		var show = document.getElementById('checkFlag').checked;
		['notifyOnceRow','checkFrequencyRow','checkFilenameRow'].forEach(function(id) {
			document.getElementById(id).style.display = show ? '' : 'none';
		});
	}

	function toggleMailRows() {
		var show = document.getElementById('userMailInput').value.trim() !== '';
		['mailOnSuccessRow','mailOnErrorRow'].forEach(function(id) {
			document.getElementById(id).style.display = show ? '' : 'none';
		});
	}

	function toggleAcquisitionRow() {
		var show = getHostType() === 'Acquisition';
		['acquisitionFrequencyRow','acquisitionFrequencySpacer'].forEach(function(id) {
			document.getElementById(id).style.display = show ? '' : 'none';
		});
	}
	
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
		$('#hostHelpContent').html(getHelpHtmlContent(
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
    	bootstrap.Collapse.getOrCreateInstance(document.getElementById('hostAccProperties')).show();
    	checkEachLine(editorProperties);
    	populateHelpTab();
	});
	
   	var selectElement = document.getElementById("type");
   	if (selectElement && selectElement.tagName === "SELECT") {
		document.getElementById("type").addEventListener("change", function() {
    		editorProperties.session.setAnnotations(
    			getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    		bootstrap.Collapse.getOrCreateInstance(document.getElementById('hostAccProperties')).show();
    		checkEachLine(editorProperties);
    		populateHelpTab();
    		toggleAcquisitionRow();
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
	editorJavascript.setOptions({minLines: 10, maxLines: 20});

	document.getElementById('hostAccProperties').addEventListener('shown.bs.collapse', function() {
		editorProperties.resize(true);
	});
	document.getElementById('hostAccJavascript').addEventListener('shown.bs.collapse', function() {
		editorJavascript.resize(true);
	});
	// When Help panel opens, scroll to the parameter at the current cursor position
	var hostHelpBtn = document.querySelector('button[data-bs-target="#hostAccHelp"]');
	if (hostHelpBtn) {
		hostHelpBtn.addEventListener('click', function() {
			setTimeout(function() {
				if (!document.getElementById('hostAccHelp').classList.contains('show')) return;
				var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
				line = line.trim();
				if (line && !line.startsWith('#') && !line.startsWith('//')) {
					var eqIdx = line.indexOf('=');
					var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
					if (paramName) scrollHelpToParam('hostHelpContent', paramName);
				}
			}, 400);
		});
	}

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

	window.addEventListener('resize', function() {
		editorDir.resize(true);
		editorProperties.resize(true);
		editorJavascript.resize(true);
	});

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
	// Duration picker: h + m only. Rounds existing ms value to nearest minute (min 1m unless 0).
	(function() {
		function msToHM(ms) {
			ms = parseInt(ms) || 0;
			if (ms === 0) return {h: 0, m: 0};
			var totalMin = Math.round(ms / 60000);
			if (totalMin < 1) totalMin = 1;
			return {h: Math.floor(totalMin / 60), m: totalMin % 60};
		}
		function hmToMs(h, m) {
			return ((parseInt(h) || 0) * 60 + (parseInt(m) || 0)) * 60000;
		}
		function fmtHM(h, m) {
			var parts = [];
			if (h) parts.push(h + 'h');
			if (m) parts.push(m + 'm');
			return parts.length ? '= ' + parts.join(' ') : (h === 0 && m === 0 ? '= 0 (disabled)' : '');
		}
		$('.dur-picker').each(function() {
			var $picker = $(this);
			var $hidden = $('#' + $picker.data('target'));
			var p = msToHM($hidden.val());
			$picker.find('.dur-h').val(p.h || '');
			$picker.find('.dur-m').val(p.m || '');
			$picker.find('.dur-display').text(fmtHM(p.h, p.m));
			$picker.find('.dur-h, .dur-m').on('input change', function() {
				var h = parseInt($picker.find('.dur-h').val()) || 0;
				var m = parseInt($picker.find('.dur-m').val()) || 0;
				$hidden.val(hmToMs(h, m));
				$picker.find('.dur-display').text(fmtHM(h, m));
			});
		});
	})();
	});	$('#nickName').on('input', function() {
		const regex = /^[a-zA-Z0-9_-]+$/;
		const $this = $(this);
		const value = $this.val();
		if (!regex.test(value)) {
			// Remove all invalid characters
			$this.val(value.replace(/[^a-zA-Z0-9_-]/g, ''));
		}
	});
	$('#host').on('input', function() {
		const regex = /^[a-zA-Z0-9-.]+$/;
		const $this = $(this);
		const value = $this.val();
		if (!regex.test(value)) {
			// Remove all invalid characters
			$this.val(value.replace(/[^a-zA-Z0-9-.]/g, ''));
		}
	});
	function toggleLocationFields() {
		var checkbox = document.getElementById("automaticLocation");
    	var latitude = document.getElementById("latitudeField");
    	var longitude = document.getElementById("longitudeField");
    	var disabled = checkbox.checked;
    	latitude.disabled = disabled;
    	longitude.disabled = disabled;
    }
    window.onload = function() {
    	toggleLocationFields(); // initialize on page load
    	document.getElementById("automaticLocation").addEventListener("change", toggleLocationFields);
    	var mailInput = document.getElementById('userMailInput');
    	if (mailInput) validateMailInput(mailInput);
    	var hostInput = document.getElementById('host');
    	if (hostInput) validateHostInput(hostInput);
    	var nickInput = document.getElementById('nickName');
    	if (nickInput) validatePatternInput(nickInput, 'nickNameFeedback');
	};
</script>
