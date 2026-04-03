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
.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px lightgray;
	border-radius: 4px;
	margin-top: 8px;
	margin-bottom: 4px;
}
.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px lightgray;
	border-radius: 4px;
	padding: 8px;
}
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
.progress-terminal { border-radius: 4px; overflow: hidden; margin-top: 2px; }
.progress-terminal-hdr {
    background: #2d2d2d; color: #ccc; padding: 0.25rem 0.75rem;
    font-size: 0.75rem; border-bottom: 1px solid #444;
    display: flex; align-items: center; gap: 0.4rem;
}
.progress-terminal-body {
    background: #1e1e1e; color: #d4d4d4;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 0.75rem; line-height: 1.7;
    padding: 0.5rem 0.75rem;
    height: 200px; min-height: 80px; max-height: 500px;
    overflow-y: auto; resize: vertical;
    white-space: pre-wrap; word-break: break-word;
}
.progress-terminal-body font[color="black"],
.progress-terminal-body font[color="#000000"] { color: #6c9abd; }
.progress-terminal-body font[color="red"]   { color: #f88; }
.progress-terminal-body font[color="green"] { color: #8f8; }
.progress-terminal-body a { color: #79b8ff; text-decoration: none; }
.progress-terminal-body a:hover { text-decoration: underline; }
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
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
					<td><c:if test="${host.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
							test="${!host.active}">
							<i class="bi bi-x-circle-fill text-danger" title="No"></i>
						</c:if></td>
				</tr>
				<tr>
					<th>Type</th>
					<td>${host.type}</td>
					<th>Comment</th>
					<td>${host.comment}</td>
				</tr>

				<tr>
					<td colspan="4">&nbsp;</td>
				</tr>

				<tr>
					<th>Location Source</th>
					<td><c:if test="${host.automaticLocation}">automatic</c:if><c:if test="${!host.automaticLocation}">manual</c:if></td>
					<th>Estimated Location</th>
					<td>${host.geoIpLocation}</td>
				</tr>

				<tr>
					<th>Latitude (&deg;)</th>
					<td>${host.latitude}</td>
					<th>Longitude (&deg;)</th>
					<td>${host.longitude}</td>
				</tr>
								
				<tr>
					<td colspan="4">&nbsp;</td>
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
					<td colspan="4">&nbsp;</td>
				</tr>

						<tr>
							<th>Options</th>
							<td colspan="3">
								<div class="accordion" id="hostViewOptionsAccordion">
								<div class="accordion-item">
									<h2 class="accordion-header" id="hostViewAccHeadProperties">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostViewAccProperties" aria-expanded="false" aria-controls="hostViewAccProperties">
											Properties
										</button>
									</h2>
									<div id="hostViewAccProperties" class="accordion-collapse collapse" aria-labelledby="hostViewAccHeadProperties" data-bs-parent="#hostViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div class="ace-panel">
												<pre id="properties"><c:out value="${host.properties}" /></pre>
												<textarea id="properties" name="properties" style="display: none;"></textarea>
											</div>
										</div>
									</div>
								</div>
								<div class="accordion-item">
									<h2 class="accordion-header" id="hostViewAccHeadJavascript">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostViewAccJavascript" aria-expanded="false" aria-controls="hostViewAccJavascript">
											JavaScript
										</button>
									</h2>
									<div id="hostViewAccJavascript" class="accordion-collapse collapse" aria-labelledby="hostViewAccHeadJavascript" data-bs-parent="#hostViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div class="ace-panel">
												<pre id="javascript"><c:out value="${host.javascript}" /></pre>
												<textarea id="javascript" name="javascript" style="display: none;"></textarea>
											</div>
										</div>
									</div>
								</div>
								<div class="accordion-item">
									<h2 class="accordion-header" id="hostViewAccHeadHelp">
										<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#hostViewAccHelp" aria-expanded="false" aria-controls="hostViewAccHelp">
											Help
										</button>
									</h2>
									<div id="hostViewAccHelp" class="accordion-collapse collapse" aria-labelledby="hostViewAccHeadHelp" data-bs-parent="#hostViewOptionsAccordion">
										<div class="accordion-body p-2">
											<div id="hostViewHelpContent" class="scrollable-tab"></div>
										</div>
									</div>
								</div>
							</div>
							</td>
						</tr>

						<auth:if basePathKey="transferhistory.basepath" paths="/">
							<auth:then>

						<c:if test="${host.type == 'Acquisition'}">
						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>

						<tr>
							<th>Progress</th>
							<td colspan="3">
								<div class="progress-terminal">
									<div class="progress-terminal-hdr">
										<i class="bi bi-activity text-success"></i> Activity Log
										<button class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2 ms-auto"
												style="font-size:0.7rem;"
												onclick="(function(btn){
													var text = document.getElementById('progressBody').innerText;
													navigator.clipboard.writeText(text).then(function(){
														btn.innerHTML='<i class=\'bi bi-check-lg\'></i> Copied';
														setTimeout(function(){ btn.innerHTML='<i class=\'bi bi-clipboard\'></i> Copy'; }, 1500);
													});
												})(this)">
											<i class="bi bi-clipboard"></i> Copy
										</button>
									</div>
									<div class="progress-terminal-body" id="progressBody">${host.formattedLastOutput}</div>
								</div>
							</td>
						</tr>
						</c:if>

						<tr>
							<td colspan="3">&nbsp;</td>
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
							<td><c:if test="${host.check}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
									test="${!host.check}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
							<c:choose>
								<c:when test="${not empty host.userMail}">
									<th>Mail On Success</th>
									<td><c:if test="${host.mailOnSuccess}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
											test="${!host.mailOnSuccess}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
								</c:when>
								<c:otherwise><td colspan="2"></td></c:otherwise>
							</c:choose>
						</tr>
						<c:if test="${host.check || not empty host.userMail}">
						<tr>
							<c:choose>
								<c:when test="${host.check}">
									<th>Check Time</th>
									<td class="date">${host.checkTime}</td>
								</c:when>
								<c:otherwise><td colspan="2"></td></c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${not empty host.userMail}">
									<th>Mail On Error</th>
									<td><c:if test="${host.mailOnError}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
											test="${!host.mailOnError}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
								</c:when>
								<c:otherwise><td colspan="2"></td></c:otherwise>
							</c:choose>
						</tr>
						</c:if>
						<c:if test="${host.check}">
						<tr>
							<th>Check Frequency</th>
							<td>${host.formattedCheckFrequency}</td>
							<th>Notify Once</th>
							<td><c:if test="${host.notifyOnce}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
									test="${!host.notifyOnce}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
						</tr>
						</c:if>
						<c:if test="${host.type == 'Acquisition' || not empty host.userMail}">
						<tr>
							<c:choose>
								<c:when test="${host.type == 'Acquisition'}">
									<th>Acquisition Time</th>
									<td class="date">${host.acquisitionTime}</td>
								</c:when>
								<c:otherwise><td colspan="2"></td></c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${not empty host.userMail}">
									<th>Owner Mail</th>
									<td>${host.userMail}</td>
								</c:when>
								<c:otherwise><td colspan="2"></td></c:otherwise>
							</c:choose>
						</tr>
						</c:if>
						<c:if test="${host.type == 'Acquisition'}">
						<tr>
							<th>Acquisition Frequency</th>
							<td>${host.formattedAcquisitionFrequency}</td>
							<auth:if basePathKey="transferhistory.basepath" paths="/">
								<auth:then>
									<th>Valid</th>
									<td><c:if test="${host.valid}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
											test="${!host.valid}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
								</auth:then>
							</auth:if>
						</tr>
						</c:if>

					</auth:then>
				</auth:if>

			</table>

			<c:if test="${host.type != 'Replication' && host.type != 'Source' && host.type != 'Backup'}">
				<div class="card assoc-card mt-3" style="max-width:480px">
				  <div class="card-header">
				    <i class="bi bi-geo-alt text-secondary"></i>
				    <strong>Destination(s) using this Host</strong>
				  </div>
				  <div class="card-body p-2">
				    <c:choose>
				      <c:when test="${empty host.destinations}">
				        <p class="text-muted small mb-0"><em>No destinations assigned.</em></p>
				      </c:when>
				      <c:otherwise>
				        <div class="d-flex flex-wrap">
				          <c:forEach var="destination" items="${host.destinations}">
				            <span class="assoc-chip">
				              <a href="<bean:message key="destination.basepath"/>/${destination.id}" title="${destination.comment}" class="text-decoration-none text-dark">${destination.name}</a>
				            </span>
				          </c:forEach>
				        </div>
				      </c:otherwise>
				    </c:choose>
				  </div>
				</div>
			</c:if>
		</c:if>
	</c:if>

	<script>		
		var editorDir = getEditorProperties(true, false, "dir", "toml");
		makeResizable(editorDir);

		var editorProperties = getEditorProperties(true, false, "properties", "crystal");
		editorProperties.setOptions({minLines: 10, maxLines: 20});
		
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
    		$('#hostViewHelpContent').html(getHelpHtmlContent(
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
		editorJavascript.setOptions({minLines: 10, maxLines: 20});

		document.getElementById('hostViewAccProperties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		document.getElementById('hostViewAccJavascript').addEventListener('shown.bs.collapse', function() {
			editorJavascript.resize(true);
		});
		var hostViewHelpBtn = document.querySelector('button[data-bs-target="#hostViewAccHelp"]');
		if (hostViewHelpBtn) {
			hostViewHelpBtn.addEventListener('click', function() {
				setTimeout(function() {
					if (!document.getElementById('hostViewAccHelp').classList.contains('show')) return;
					var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
					line = line.trim();
					if (line && !line.startsWith('#') && !line.startsWith('//')) {
						var eqIdx = line.indexOf('=');
						var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
						if (paramName) scrollHelpToParam('hostViewHelpContent', paramName);
					}
				}, 400);
			});
		}

		makeResizable(editorProperties);
		makeResizable(editorJavascript);

		window.addEventListener('resize', function() {
			editorDir.resize(true);
			editorProperties.resize(true);
			editorJavascript.resize(true);
		});

		$('#istext').prop('disabled', true);
		$('#isjs').prop('disabled', true);
		$('#ispython').prop('disabled', true);

		var dirType = getEditorType(editorDir);
		$('#is' + dirType).prop('checked', true);
		$('#is' + dirType).prop('disabled', false);
		editorDir.session.setMode("ace/mode/"
				+ (dirType === "js" ? "javascript"
						: dirType === "text" ? "toml" : dirType));

	</script>

</c:if>
