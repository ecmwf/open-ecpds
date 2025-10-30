<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/host/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

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

.scrollable-tab {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
  	overflow-y: scroll; /* Enable vertical scrolling */
}
</style>

	<c:set var="authorized" value="false" />

	<auth:if basePathKey="destination.basepath" paths="/">
		<auth:then>
			<c:set var="authorized" value="true" />
		</auth:then>
	</auth:if>

	<auth:if basePathKey="transferhistory.basepath" paths="/">
		<auth:then>
		</auth:then>
		<auth:else>

			<c:forEach var="destination" items="${host.destinations}">
				<auth:if basePathKey="destination.basepath"
					paths="/${destination.name}">
					<auth:then>
						<c:set var="authorized" value="true" />
					</auth:then>
				</auth:if>
			</c:forEach>

		</auth:else>
	</auth:if>

	<c:if test="${authorized == 'false'}">
		<br />
		<div class="alert">
			<span class="closebtn" onclick="parent.history.back();">&times;</span>
			Error getting object <- Problem searching by key '${host.name}' <-
			Host not found: {${host.name}}
		</div>
	</c:if>

	<c:if test="${authorized == 'true'}">

		<tiles:importAttribute name="isDelete" ignore="true" />
		<c:if test="${not empty isDelete}">
			<tiles:insert page="./pds/transfer/host/warning.jsp" />
		</c:if>
		<c:if test="${empty isDelete}">
			<table class="fields">

				<tr>
					<th>Id</th>
					<td>${host.name}</td>
					<th>Hostname/IP</th>
					<td>${host.host}</td>
				</tr>
				<tr>
					<th>Nickname</th>
					<td>${host.nickName}</td>
					<th>Label</th>
					<td>${host.networkCode}:${host.networkName}</td>
				</tr>
				<tr>
					<th>Owner</th>
					<td>${host.ecUser.comment}</td>
					<th>Login</th>
					<td>${host.login}</td>
				</tr>
				<tr>
					<th>Transfer Group</th>
					<td><auth:link basePathKey="transfergroup.basepath"
							href="/${host.transferGroupName}"
							alternativeText="${host.transferGroupName}">${host.transferGroupName}</auth:link></td>
					<th>Passwd</th>
					<td>*********</td>
				</tr>

				<tr>
					<th>Max Connections</th>
					<td>${host.maxConnections}</td>
					<th>Filter Name</th>
					<td>${host.filterName}</td>
				</tr>

				<tr>
					<th>Transfer Method</th>
					<td><auth:link basePathKey="method.basepath"
							href="/${host.transferMethodName}"
							alternativeText="${host.transferMethodName}">${host.transferMethodName}</auth:link></td>
					<th>Enabled</th>
					<td><c:if test="${host.active}">yes</c:if> <c:if
							test="${!host.active}">
							<font color="red">no</font>
						</c:if></td>
				</tr>
				<tr>
					<th>Type</th>
					<td>${host.type}</td>

					<auth:if basePathKey="transferhistory.basepath" paths="/">
						<auth:then>
							<th>Valid</th>
							<td><c:if test="${host.valid}">yes</c:if> <c:if
									test="${!host.valid}">no</c:if></td>
						</auth:then>
					</auth:if>

				</tr>

				<tr>
					<td colspan="4"></td>
				</tr>
				<tr>
					<th>Comment</th>
					<td colspan="3">${host.comment}</td>
				</tr>

				<tr>
					<td colspan="3">&nbsp;</td>
				</tr>

				<tr>
					<th>Directory</th>
					<td colspan="3">
						<div id='dirType'>
							<input type='radio' id='istext' name='dirType' />Plain Text <input
								type='radio' id='isjs' name='dirType' />JavaScript <input
								type='radio' id='ispython' name='dirType' />Python
						</div> <pre id="dir">
				<c:out value="${host.dir}" />
			</pre> <textarea id="dir" name="dir" style="display: none;"></textarea>
					</td>
				</tr>


						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>

						<tr>
							<th>Options</th>
							<td colspan="3">
								<div id="tabs">
									<ul>
										<li><a href="#tabs-1">Properties</a></li>
										<li><a href="#tabs-2">JavaScript</a></li>
										<li><a href="#tabs-3">Help</a></li>
									</ul>
									<div id="tabs-1">
										<pre id="properties">
											<c:out value="${host.properties}" />
										</pre>
										<textarea id="properties" name="properties"
											style="display: none;"></textarea>
									</div>
									<div id="tabs-2">
										<pre id="javascript">
											<c:out value="${host.javascript}" />
										</pre>
										<textarea id="javascript" name="javascript"
											style="display: none;"></textarea>
									</div>
									<div id="tabs-3" class="scrollable-tab">
									</div>
								</div>
							</td>
						</tr>

						<auth:if basePathKey="transferhistory.basepath" paths="/">
							<auth:then>

						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>

						<tr>
							<th>Progress</th>
							<td colspan="3"><pre class="delimiters">${host.formattedLastOutput}</pre></td>
						</tr>

						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>

						<tr>
							<th>Automatic Location</th>
							<td colspan="3"><c:if test="${host.automaticLocation}">yes</c:if>
								<c:if test="${!host.automaticLocation}">no</c:if></td>
						</tr>
						<tr>
							<td colspan="4"></td>
						</tr>
						<tr>
							<th>Latitude</th>
							<td>${host.latitude}</td>
							<th>Longitude</th>
							<td>${host.longitude}</td>
						</tr>
						<tr>
							<th>Retry Frequency</th>
							<td>${host.formattedRetryFrequency}</td>
							<th>Total Data Sent</th>
							<td><a STYLE="TEXT-DECORATION: NONE"
								title="Sent: ${host.formattedSent}">${host.sent} bytes</a></td>
						</tr>
						<tr>
							<th>Connections</th>
							<td>${host.connections}</td>
							<th>Total Time Taken</th>
							<td>${host.formattedDuration}</td>
						</tr>
						<tr>
							<th>Retry Count</th>
							<td>${host.retryCount}</td>
							<th>Bandwidth</th>
							<td><a STYLE="TEXT-DECORATION: NONE"
								title="Bandwidth: ${host.formattedBandWidth}">${host.formattedBandWidthInMBitsPerSeconds}
									Mbits/s</a></td>
						</tr>

						<tr>
							<td colspan="4"></td>
						</tr>

						<tr>
							<th>Check</th>
							<td><c:if test="${host.check}">yes</c:if> <c:if
									test="${!host.check}">no</c:if></td>
							<th>Mail On Success</th>
							<td><c:if test="${host.mailOnSuccess}">yes</c:if> <c:if
									test="${!host.mailOnSuccess}">no</c:if></td>
						</tr>
						<tr>
							<th>Check Time</th>
							<td class="date">${host.checkTime}</td>
							<th>Mail On Error</th>
							<td><c:if test="${host.mailOnError}">yes</c:if> <c:if
									test="${!host.mailOnError}">no</c:if></td>
						</tr>
						<tr>
							<th>Check Frequency</th>
							<td>${host.formattedCheckFrequency}</td>
							<th>Notify Once</th>
							<td><c:if test="${host.notifyOnce}">yes</c:if> <c:if
									test="${!host.notifyOnce}">no</c:if></td>
						</tr>
						<tr>
							<th>Acquisition Time</th>
							<td class="date">${host.acquisitionTime}</td>
							<th>Owner Mail</th>
							<td>${host.userMail}</td>
						</tr>
						<tr>
							<th>Acquisition Frequency</th>
							<td>${host.formattedAcquisitionFrequency}</td>
							<td>&nbsp;</td>
						</tr>

					</auth:then>
				</auth:if>

			</table>

			<c:if test="${host.type != 'Replication' && host.type != 'Source' && host.type != 'Backup'}">
				<table width="100%" border=0>
					<tr>
						<td><display:table id="destination"
							name="${host.destinations}" requestURI="" class="listing">
							<display:column sortable="true" title="Name">
								<a
									href="<bean:message key="destination.basepath"/>/${destination.id}">${destination.name}</a>
							</display:column>
							<display:column sortable="false" title="Comment">${destination.comment}</display:column>
							<display:caption>Destination(s) using this Host</display:caption>
						</display:table></td>
					</tr>
				</table>
			</c:if>
		</c:if>
	</c:if>

	<script>		
		var editorDir = getEditorProperties(true, false, "dir", "toml");
		makeResizable(editorDir);

		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		
		// Get the completions from the bean!
		var hostType = "${host.type}";
		var transferMethodName = '${host.transferMethodName}';
		var transferModuleName = '${host.transferMethod.ecTransModuleName}';
		var transferModuleNames = [${host.transferModuleNames}];
    	var completions = [
    		${host.completions}
    	];
    	
    	// Lets' populate the help tab!
    	$(document).ready(function() {
    		$('#tabs-3').html(getHelpHtmlContent(
    				completions.filter(function(item) {
    	           		var moduleName = item.caption.split(".")[0];
    	       	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
    	       	    		return false;
    	       	    	} else {
    	          			if ((!["Acquisition", "Source"].includes(hostType) && moduleName === "retrieval")
    	          					|| (!["Dissemination", "Proxy"].includes(hostType) && moduleName === "upload")
    	          					|| (hostType !== "Acquisition"  && moduleName === "acquisition")
    	          					|| (hostType !== "Proxy" && moduleName === "proxy")) {
    	           	    		return false;
    	          			} else {
    	          				return true;
    	          			}
    	       	    	}
    	    	    }), 'Available Options for Host of Type ' + hostType + ' with Transfer Method ' + transferMethodName))
    	});
    	
    	// Overwrite the original method to deal with the specificities of the host
    	function getAnnotations(aceEditor, row) {
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
              			if ((!["Acquisition", "Source"].includes(hostType) && moduleName === "retrieval")
	          					|| (!["Dissemination", "Proxy"].includes(hostType) && moduleName === "upload")
              					|| (hostType !== "Acquisition"  && moduleName === "acquisition")
              					|| (hostType !== "Proxy" && moduleName === "proxy")) {
                   	    	tipsText = "Ignored as \"" + moduleName + "\" parameters are not valid for " + hostType + " hosts.";
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

		$('#istext').prop('disabled', true);
		$('#isjs').prop('disabled', true);
		$('#ispython').prop('disabled', true);

		var dirType = getEditorType(editorDir);
		$('#is' + dirType).prop('checked', true);
		$('#is' + dirType).prop('disabled', false);
		editorDir.session.setMode("ace/mode/"
				+ (dirType === "js" ? "javascript"
						: dirType === "text" ? "toml" : dirType));

		$("#tabs").tabs();
		$("#tabs").tabs("option", "active", 0);
	</script>

</c:if>
