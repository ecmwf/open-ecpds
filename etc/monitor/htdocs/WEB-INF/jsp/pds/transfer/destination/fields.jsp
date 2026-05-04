<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
#properties {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
	margin-top: 8px;
	margin-bottom: 8px;
}

#javascript {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
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

#maxStartHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#maxRequeueHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#maxPendingHandle {
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
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
	position: relative;
}
.ace-clip {
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
	position: relative;
}
.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
}
</style>

<table style="width:100%">
	<tr>
		<td style="width:1%;white-space:nowrap;vertical-align:top">
			<table class="fields">
				<tiles:useAttribute id="actionFormName" name="action.form.name"
					classname="java.lang.String" />
				<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-geo-alt text-primary flex-shrink-0"></i>
            Create a new Destination to define a data delivery target.
        </div>
        </td></tr>
    </c:when>
    <c:otherwise>
        <tr><td colspan="2">
        <div class="form-info-banner">
            <i class="bi bi-geo-alt text-primary flex-shrink-0"></i>
            Edit the Destination configuration.
        </div>
        </td></tr>
    </c:otherwise>
</c:choose>


				<c:if test="${isInsert == 'true'}">
					<%-- Mode selector pill buttons --%>
					<tr>
						<td colspan="2" style="padding-top:0.5rem;padding-bottom:0.75rem">
							<div class="d-flex gap-2 flex-wrap" role="group">
								<c:if test="${not empty destinationActionForm.fromDestinationOptions}">
									<button type="button" id="btn-copy"
										class="btn btn-sm btn-primary dest-mode-btn"
										onclick="selectDestMode('copy',this)">
										<i class="bi bi-copy me-1"></i>Copy from Existing
									</button>
								</c:if>
								<button type="button" id="btn-create"
									class="btn btn-sm dest-mode-btn <c:choose><c:when test="${not empty destinationActionForm.fromDestinationOptions}">btn-outline-secondary</c:when><c:otherwise>btn-primary</c:otherwise></c:choose>"
									onclick="selectDestMode('create',this)">
									<i class="bi bi-plus-circle me-1"></i>Create from Scratch
								</button>
								<c:if test="${not empty destinationActionForm.masterOptions}">
									<button type="button" id="btn-export"
										class="btn btn-sm btn-outline-secondary dest-mode-btn"
										onclick="selectDestMode('export',this)">
										<i class="bi bi-box-arrow-up-right me-1"></i>Export to Master
									</button>
								</c:if>
							</div>
							<%-- Single hidden field for form submission — value set by JSP default and updated by selectDestMode() --%>
							<input type="hidden" id="actionRequested" name="actionRequested"
								value="<c:choose><c:when test="${not empty destinationActionForm.fromDestinationOptions}">copy</c:when><c:otherwise>create</c:otherwise></c:choose>">
						</td>
					</tr>
				</c:if>

				<%-- Copy panel --%>
				<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}">
				<tbody id="panel-copy">
					<tr>
						<td colspan="2"><small class="text-muted">Select a source destination and provide a name for the new copy.</small></td>
					</tr>
					<script>document.addEventListener('DOMContentLoaded',function(){var s=document.getElementById('fromDestination');if(s)s.required=true;});</script>
					<tr>
						<th>Source Destination</th>
						<td>
							<c:set var="destinations" value="${destinationActionForm.fromDestinationOptions}" />
							<input type="text" class="form-control form-control-sm mb-1"
								placeholder="Search destinations..."
								oninput="filterSelect(this, 'fromDestination')"
								autocomplete="off">
							<html:select property="fromDestination" styleId="fromDestination"
								size="20" style="min-width:280px;width:100%;height:220px;overflow-y:auto">
								<html:options collection="destinations" property="name" labelProperty="name" />
							</html:select>
						</td>
					</tr>
					<tr>
						<th>New Name</th>
						<td>
							<div class="d-flex align-items-center gap-2">
								<input id="toDestination" name="toDestination" type="text"
									maxlength="32"
									pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. my-destination). Maximum 32 characters."
									oninput="validatePatternInput(this, 'toDestination-feedback'); var n=document.getElementById('name');if(n)n.value=this.value;"
									<c:if test="${not empty destinationActionForm.fromDestinationOptions}">required</c:if>>
								<span id="toDestination-feedback"></span>
							</div>
						</td>
					</tr>
					<tr>
						<th>Comment</th>
						<td><html:text property="label" /></td>
					</tr>
					<tr>
						<th>Clone Shared Hosts <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If ticked then every Host of the existing Destination which is shared with another Destination will be cloned/renamed and allocated to the new Destination (the new Destination will therefore not use shared Hosts)" tabindex="0"></i></th>
						<td><html:checkbox
								property="copySharedHost" /></td>
					</tr>
				</tbody>

				<%-- Export panel --%>
				<c:if test="${not empty destinationActionForm.masterOptions}">
				<tbody id="panel-export" class="d-none">
					<tr>
						<td colspan="2"><small class="text-muted">Select a source destination and a target master to export to.</small></td>
					</tr>
					<tr>
						<th>Source Destination</th>
						<td>
							<input type="text" class="form-control form-control-sm mb-1"
								placeholder="Search destinations..."
								oninput="filterSelect(this, 'sourceDestination')"
								autocomplete="off">
							<html:select property="sourceDestination" styleId="sourceDestination"
								size="20" style="min-width:280px;width:100%;height:220px;overflow-y:auto">
								<html:options collection="destinations" property="name" labelProperty="name" />
							</html:select>
						</td>
					</tr>
					<tr>
						<th>Master</th>
						<td>
							<c:set var="masters" value="${destinationActionForm.masterOptions}" />
							<html:select property="master">
								<html:options collection="masters" property="name" labelProperty="value" />
							</html:select>
						</td>
					</tr>
					<tr>
						<th>Clone Shared Hosts <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If ticked then every Host associated with the source Destination will be cloned/renamed on the target data store, otherwise a search will be done against the Host nick-name and if it already exists on the target data store then it will be used instead" tabindex="0"></i></th>
						<td><html:checkbox
								property="copySourceSharedHost" /></td>
					</tr>
				</tbody>
				</c:if>
				</c:if>

				<%-- Create panel --%>
				<tbody id="panel-create" <c:if test="${not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
					<tr>
						<td colspan="2"><small class="text-muted">Provide a name below to create a brand-new destination from scratch.</small></td>
					</tr>
				</tbody>
				<%-- Name field: always in DOM for insert so backend validation passes.
				     Shown (editable) only in create mode; auto-populated from toDestination in copy mode. --%>
				<c:if test="${isInsert == 'true'}">
				<tbody id="row-name-create"
					<c:if test="${not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
					<tr>
						<th>Name</th>
						<td>
							<div class="d-flex align-items-center gap-2">
								<input id="name" name="name" type="text"
									maxlength="32"
									pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. my-destination). Maximum 32 characters."
									oninput="validatePatternInput(this, 'name-feedback')"
									<c:if test="${empty destinationActionForm.fromDestinationOptions}">required</c:if>>
								<span id="name-feedback"></span>
							</div>
						</td>
					</tr>
				</tbody>
				</c:if>
				<tbody id="dest-common-fields"
					<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Name</th>
						<td>${requestScope[actionFormName].name}<html:hidden
								property="name" /></td>
					</tr>
				</c:if>
				<tr>
					<th>Type</th>
					<td><div style="display:inline-flex;align-items:center;gap:0.5rem;"><c:set var="types"
							value="${destinationActionForm.typeOptions}" /> <html:select
							property="type" styleId="destType">
							<html:options collection="types" property="name"
								labelProperty="value" />
						</html:select></div></td>
				</tr>
				<tr>
					<th>Comment</th>
					<td><html:text property="comment" /></td>
				</tr>
				<tr>
					<th>On Host Failure <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="In case of error on a data transmission then try the next host in the list and stick to it if it works or restart with the first host in the list?" tabindex="0"></i></th>
					<td><c:set var="onHosts"
							value="${destinationActionForm.onHostFailureOptions}" /> <html:select
							property="onHostFailure">
							<html:options collection="onHosts" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>If Target Exists</th>
					<td><c:set var="ifTargets"
							value="${destinationActionForm.ifTargetExistOptions}" /> <html:select
							property="ifTargetExist">
							<html:options collection="ifTargets" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Delete From Spool</th>
					<td><c:set var="keepInSpools"
							value="${destinationActionForm.keepInSpoolOptions}" /> <html:select
							property="keepInSpool">
							<html:options collection="keepInSpools" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Country</th>
					<td><c:set var="countries"
							value="${destinationActionForm.countryOptions}" /> <html:select
							property="countryIso" styleId="countryIso">
							<html:options collection="countries" property="iso"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Transfer Group <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If no Dissemination Host is active then this field specify in which Transfer Group the queued files should be stored" tabindex="0"></i></th>
					<td><bean:define id="groups" name="destinationActionForm"
							property="transferGroupOptions" type="java.util.Collection	" />
						<html:select
							property="transferGroup">
							<html:options collection="groups" property="name"
								labelProperty="name" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Max Connections <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Maximum number of parallel connections authorized at a time on all the hosts of the Destination" tabindex="0"></i></th>
					<td>
						<div id="maxConnectionsSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxConnections" id="maxConnections">
							<div
								id="maxConnectionsHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Retry Count <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the Destination is hold after a consecutive number of unsuccessful transfers (a manual restart will be necessary)" tabindex="0"></i></th>
					<td>
						<div id="retryCountSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="retryCount" id="retryCount">
							<div
								id="retryCountHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry with the Primary Host if the transmission is failing on all the Backup Hosts" tabindex="0"></i></th>
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
					<th>Max Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is delayed after a consecutive number of unsuccessful attempts" tabindex="0"></i></th>
					<td>
						<div id="maxStartSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxStart" id="maxStart">
							<div
								id="maxStartHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr id="startFrequencyRow" style="display:none">
					<th>Start Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay mentioned in the previous parameter (Max Start)." tabindex="0"></i></th>
					<td>
						<input type="hidden" name="startFrequency" id="startFrequency"
							value="<c:out value="${requestScope[actionFormName].startFrequency}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="startFrequency">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
						</td>
				</tr>
				<tr>
					<th>Max Requeue <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is tagged as failed after a consecutive number of unsuccessful transmissions (a manual requeue will be necessary)" tabindex="0"></i></th>
					<td>
						<div id="maxRequeueSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxRequeue" id="maxRequeue">
							<div
								id="maxRequeueHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Max Pending <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum number of queued files which can exists at a single time in the Destination (new attempt of queueing files are rejected)" tabindex="0"></i></th>
					<td>
						<div id="maxPendingSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxPending" id="maxPending">
							<div
								id="maxPendingHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Max File Size <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum size for a file in the queue (attempt of queueing bigger files are rejected)" tabindex="0"></i></th>
					<td>
						<html:hidden property="maxFileSize" styleId="maxFileSize" />
						<div class="d-flex align-items-center gap-2 flex-wrap" id="maxFileSizePicker">
							<div class="form-check form-switch mb-0 d-flex align-items-center gap-2">
								<input class="form-check-input mt-0" type="checkbox" id="maxFileSizeEnabled" />
								<label class="form-check-label small text-muted" for="maxFileSizeEnabled">Limit file size</label>
							</div>
							<div id="maxFileSizeInputs" class="d-flex align-items-center gap-1 d-none">
								<input type="number" id="maxFileSizeValue" min="1" class="form-control form-control-sm" style="width:90px" />
								<select id="maxFileSizeUnit" class="form-select form-select-sm" style="width:80px">
									<option value="1">B</option>
									<option value="1024">KB</option>
									<option value="1048576" selected>MB</option>
									<option value="1073741824">GB</option>
								</select>
							</div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Reset Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination is successfully using a backup host for more than this duration, it will restart." tabindex="0"></i></th>
					<td>
						<input type="hidden" name="resetFrequency" id="resetFrequency"
							value="<c:out value="${requestScope[actionFormName].resetFrequency}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="resetFrequency">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
						</td>
				</tr>
				<tr>
					<th>Max Inactivity <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination has no dissemination activity for more than this duration, a problem will be shown on the monitoring." tabindex="0"></i></th>
					<td>
						<input type="hidden" name="maxInactivity" id="maxInactivity"
							value="<c:out value="${requestScope[actionFormName].maxInactivity}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="maxInactivity">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
						</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Group By Date <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set then incoming ftp/sftp users will see the files grouped into date directories" tabindex="0"></i></th>
					<td><html:checkbox property="groupByDate" styleId="groupByDate"
							onclick="document.getElementById('dateFormatRow').style.display = this.checked ? '' : 'none'" /></td>
				</tr>
				<tr id="dateFormatRow">
					<th>Date Format <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the format of the date to display for each directory (Java SimpleDateFormat pattern)" tabindex="0"></i></th>
					<td>
						<input type="text" id="dateFormatInput" name="dateFormat" list="dateFormatPresets"
							value='<c:out value="${requestScope[actionFormName].dateFormat}"/>' />
						<datalist id="dateFormatPresets">
							<option value="yyyyMMdd" />
							<option value="yyyy-MM-dd" />
							<option value="yyyy/MM/dd" />
							<option value="dd-MM-yyyy" />
							<option value="dd/MM/yyyy" />
							<option value="yyyy" />
							<option value="yyyyMM" />
							<option value="MMM" />
							<option value="MMMM" />
							<option value="MMMM yyyy" />
							<option value="yyyyMMdd-HH" />
							<option value="yyyyMMddHH" />
						</datalist>
						<div id="dateFormatPreview" class="mt-1 small"></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Data Compression <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If requested data files are compressed in the queue if there is enough time before transmission (otherwise files are compressed on the fly)" tabindex="0"></i></th>
					<td><bean:define id="filters" name="destinationActionForm"
							property="filterNameOptions" type="java.util.Collection	" /> <html:select
							property="filterName" styleId="filterName">
							<html:options collection="filters" property="name"
								labelProperty="name" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Host For Sources <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If the data file is not found on the data mover then specify which host to use in order to retrieve the file from the source" tabindex="0"></i></th>
					<td><c:set var="sources"
							value="${destinationActionForm.hostForSourceOptions}" /> <html:select
							property="hostForSourceName">
							<html:options collection="sources" property="name"
								labelProperty="nickName" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Owner <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Only for the record" tabindex="0"></i></th>
					<td><c:set var="ecUsers"
							value="${destinationActionForm.ecUserOptions}" /> <html:select
							property="ecUserName">
							<html:options collection="ecUsers" property="name"
								labelProperty="comment" />
						</html:select></td>
				</tr>
				<tr>
					<th>Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="One or more email addresses used when sending notifications. Separate multiple addresses with ';' (e.g. 'a@example.com;b@example.com')." tabindex="0"></i></th>
					<td>
						<div class="d-flex align-items-center gap-2">
							<input type="text" name="userMail" id="userMailInput"
								value='<c:out value="${requestScope[actionFormName].userMail}"/>'
								title="Enter one or more email addresses separated by ';'"
								oninput="validateMailInput(this); toggleMailRows()" />
							<span id="userMailFeedback"></span>
						</div>
					</td>
				</tr>
				<tr id="mailOnUpdateRow">
					<th>Mail on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a change is made to the Destination or its related Hosts." tabindex="0"></i></th>
					<td><html:checkbox
						property="mailOnUpdate" /></td>
				</tr>
				<tr id="mailOnStartRow">
					<th>Mail on Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer starts for this Destination." tabindex="0"></i></th>
					<td><html:checkbox
						property="mailOnStart" /></td>
				</tr>
				<tr id="mailOnEndRow">
					<th>Mail on End <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has completed successfully for this Destination." tabindex="0"></i></th>
					<td><html:checkbox
						property="mailOnEnd" /></td>
				</tr>
				<tr id="mailOnErrorRow">
					<th>Mail on Error <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has failed for this Destination." tabindex="0"></i></th>
					<td><html:checkbox
						property="mailOnError" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Restart on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Automatically restart the Destination if a change is detected on one of the host configuration" tabindex="0"></i></th>
					<td><html:checkbox
							property="stopIfDirty" /></td>
				</tr>
				<tr>
					<th>Acquisition <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the Acquisition Scheduler to use this Destination for Data Discovery and Retrieval (at least one Acquisition host must be defined)" tabindex="0"></i></th>
					<td><html:checkbox
							property="acquisition" /></td>
				</tr>
				<tr>
					<th>Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is considered by the transfer scheduler; otherwise, no data transfers will be scheduled, even if there are pending requests in the queue. Similarly, any acquisition host will be disregarded." tabindex="0"></i></th>
					<td><html:checkbox
						property="active" /></td>
				</tr>
				<tr>
					<th>Show In Monitors <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is monitored in the monitoring display." tabindex="0"></i></th>
					<td>
					<html:checkbox property="showInMonitors" styleId="chk_showInMonitors" onchange="(function(c){document.getElementById('icon_showInMonitors').style.display=c.checked?'none':'';})(this)" />
					<i id="icon_showInMonitors" class="bi bi-eye-slash text-muted ms-1" title="Not shown in Monitor Display" style="font-size:0.78rem;"></i>
					<script>(function(){var c=document.getElementById('chk_showInMonitors');if(c)document.getElementById('icon_showInMonitors').style.display=c.checked?'none':'';})()</script>
				</td>
				</tr>
				<tr>
					<th>Backup <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the storage of the data files on the backup system (if available for the Transfer Group where the files are stored)" tabindex="0"></i></th>
					<td><html:checkbox
							property="backup" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				</tbody>
		</table>
		</td>
		<td colspan="2" style="vertical-align:top"><c:if test="${isInsert != 'true'}">
<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:var(--bs-secondary-bg); font-size:.85rem; }
.assoc-card .card-header .ms-auto { margin-left:auto !important; }
.assoc-table { font-size:.82rem; }
.assoc-table td, .assoc-table th { padding:.25rem .4rem; vertical-align:middle; }
.assoc-table a { color:#495057; text-decoration:none; }
.assoc-table a:hover { color:#0d6efd; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
.assoc-chip a { color:#6c757d; text-decoration:none; line-height:1; }
.assoc-chip a:hover { color:#dc3545; }
.assoc-chooser-item { color:#212529; font-size:.82rem; transition:background .15s; }
.assoc-chooser-item:hover { background:#e9ecef; }
.assoc-empty { display:flex; align-items:center; gap:.35rem; color:#856404; background:#fff3cd; border:1px solid #ffc107; border-radius:.25rem; font-size:.8rem; padding:.3rem .5rem; margin:0; }
</style>
<div class="row g-3 mt-0" style="max-width:480px">

  <%-- Dissemination Hosts --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-hdd-network text-secondary"></i>
        <strong>Dissemination Hosts</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#dissChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.disseminationHostsAndPriorities}">
            <p class="text-muted small mb-2"><em>No dissemination hosts assigned.</em></p>
          </c:when>
          <c:otherwise>
            <table class="table table-sm assoc-table mb-2">
              <thead class="table-light"><tr><th>Host</th><th>Priority</th><th></th></tr></thead>
              <tbody>
                <c:forEach var="host" items="${destinationActionForm.disseminationHostsAndPriorities}">
                <tr>
                  <td><c:choose>
                    <c:when test="${not empty host.name.comment}"><span title="${host.name.comment}">${host.name.nickName}</span></c:when>
                    <c:otherwise>${host.name.nickName}</c:otherwise>
                  </c:choose></td>
                  <td>${host.value}</td>
                  <td class="text-end text-nowrap">
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/increaseHostPriority/<c:out value="${host.name.name}"/>" title="Increase priority"><i class="bi bi-chevron-up"></i></a>
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/decreaseHostPriority/<c:out value="${host.name.name}"/>" title="Decrease priority"><i class="bi bi-chevron-down"></i></a>
                    <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')" class="text-danger ms-1" title="Remove"><i class="bi bi-trash3"></i></a>
                  </td>
                </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="dissChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.disseminationHostOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No dissemination hosts available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search hosts...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.disseminationHostOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.nickName}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Acquisition Hosts --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-hdd-stack text-secondary"></i>
        <strong>Acquisition Hosts</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#acqChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.acquisitionHostsAndPriorities}">
            <p class="text-muted small mb-2"><em>No acquisition hosts assigned.</em></p>
          </c:when>
          <c:otherwise>
            <table class="table table-sm assoc-table mb-2">
              <thead class="table-light"><tr><th>Host</th><th>Priority</th><th></th></tr></thead>
              <tbody>
                <c:forEach var="host" items="${destinationActionForm.acquisitionHostsAndPriorities}">
                <tr>
                  <td><c:choose>
                    <c:when test="${not empty host.name.comment}"><span title="${host.name.comment}">${host.name.nickName}</span></c:when>
                    <c:otherwise>${host.name.nickName}</c:otherwise>
                  </c:choose></td>
                  <td>${host.value}</td>
                  <td class="text-end text-nowrap">
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/increaseHostPriority/<c:out value="${host.name.name}"/>" title="Increase priority"><i class="bi bi-chevron-up"></i></a>
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/decreaseHostPriority/<c:out value="${host.name.name}"/>" title="Decrease priority"><i class="bi bi-chevron-down"></i></a>
                    <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')" class="text-danger ms-1" title="Remove"><i class="bi bi-trash3"></i></a>
                  </td>
                </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="acqChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.acquisitionHostOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No acquisition hosts available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search hosts...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.acquisitionHostOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.nickName}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Proxy Hosts --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-arrow-left-right text-secondary"></i>
        <strong>Proxy Hosts</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#proxyChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.proxyHostsAndPriorities}">
            <p class="text-muted small mb-2"><em>No proxy hosts assigned.</em></p>
          </c:when>
          <c:otherwise>
            <table class="table table-sm assoc-table mb-2">
              <thead class="table-light"><tr><th>Host</th><th>Priority</th><th></th></tr></thead>
              <tbody>
                <c:forEach var="host" items="${destinationActionForm.proxyHostsAndPriorities}">
                <tr>
                  <td><c:choose>
                    <c:when test="${not empty host.name.comment}"><span title="${host.name.comment}">${host.name.nickName}</span></c:when>
                    <c:otherwise>${host.name.nickName}</c:otherwise>
                  </c:choose></td>
                  <td>${host.value}</td>
                  <td class="text-end text-nowrap">
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/increaseHostPriority/<c:out value="${host.name.name}"/>" title="Increase priority"><i class="bi bi-chevron-up"></i></a>
                    <a href="<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/decreaseHostPriority/<c:out value="${host.name.name}"/>" title="Decrease priority"><i class="bi bi-chevron-down"></i></a>
                    <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')" class="text-danger ms-1" title="Remove"><i class="bi bi-trash3"></i></a>
                  </td>
                </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="proxyChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.proxyHostOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No proxy hosts available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search hosts...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.proxyHostOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.nickName}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Authorized Web Users --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-people text-secondary"></i>
        <strong>Authorized Web Users</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#userChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.associatedEcUsers}">
            <p class="text-muted small mb-2"><em>No authorized web users assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="user" items="${destinationActionForm.associatedEcUsers}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty user.comment}"><span title="${user.comment}">${user.id}</span></c:when>
                    <c:otherwise>${user.id}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteEcUser/<c:out value="${user.id}"/>','<bean:message key="ecpds.destination.deleteEcUser.warning" arg0="${user.id}" arg1="${destinationActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="userChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.associatedEcUserOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No web users available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search users...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.associatedEcUserOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addEcUser/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.id}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Authorized Data Policies --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-shield-check text-secondary"></i>
        <strong>Authorized Data Policies</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#policyChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.associatedIncomingPolicies}">
            <p class="text-muted small mb-2"><em>No authorized data policies assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="policy" items="${destinationActionForm.associatedIncomingPolicies}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty policy.comment}"><span title="${policy.comment}">${policy.id}</span></c:when>
                    <c:otherwise>${policy.id}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deletePolicy/<c:out value="${policy.id}"/>','<bean:message key="ecpds.destination.deletePolicy.warning" arg0="${policy.id}" arg1="${destinationActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="policyChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.associatedIncomingPoliciesOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No data policies available to add.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search policies...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.associatedIncomingPoliciesOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addPolicy/${column.id}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.comment}">${column.id}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Destination Aliases --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-signpost-split text-secondary"></i>
        <strong>Destination Aliases</strong>
        <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
                data-bs-toggle="collapse" data-bs-target="#aliasChooser">
          <i class="bi bi-plus-lg"></i> Add
        </button>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.aliases}">
            <p class="text-muted small mb-2"><em>No destination aliases assigned.</em></p>
          </c:when>
          <c:otherwise>
            <div class="d-flex flex-wrap mb-2">
              <c:forEach var="alias" items="${destinationActionForm.aliases}">
                <span class="assoc-chip">
                  <c:choose>
                    <c:when test="${not empty alias.comment}"><span title="${alias.comment}">${alias.name}</span></c:when>
                    <c:otherwise>${alias.name}</c:otherwise>
                  </c:choose>
                  <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAlias/<c:out value="${alias.name}"/>','<bean:message key="ecpds.destination.deleteAlias.warning" arg0="${alias.name}" arg1="${destinationActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
                </span>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
        <div class="collapse" id="aliasChooser">
          <c:choose>
            <c:when test="${empty destinationActionForm.aliasOptions}">
              <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No destinations available to add as alias.</p>
            </c:when>
            <c:otherwise>
              <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search destinations...">
              <div style="max-height:180px;overflow-y:auto">
                <c:forEach var="column" items="${destinationActionForm.aliasOptions}">
                  <a href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addAlias/${column.name}"
                     class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                    <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                    <span title="${column.value}">${column.name}</span>
                  </a>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <%-- Metadata Files (read-only list) --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-file-earmark-text text-secondary"></i>
        <strong>Metadata Files</strong>
      </div>
      <div class="card-body p-2">
        <c:choose>
          <c:when test="${empty destinationActionForm.metadataFiles}">
            <p class="text-muted small mb-0"><em>No metadata files.</em></p>
          </c:when>
          <c:otherwise>
            <table class="table table-sm assoc-table mb-0">
              <thead class="table-light"><tr><th>Name</th><th></th></tr></thead>
              <tbody>
                <c:forEach var="file" items="${destinationActionForm.metadataFiles}">
                <tr>
                  <td>${file.name}</td>
                  <td class="text-end">
                    <a href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteMetadataFile/<c:out value="${file.name}"/>','<bean:message key="ecpds.destination.deleteMetadataFile.warning" arg0="${file.name}" arg1="${destinationActionForm.id}"/>')" class="text-danger" title="Remove"><i class="bi bi-trash3"></i></a>
                  </td>
                </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

</div>
</c:if></td>
	</tr>
	<tbody id="dest-editor-tbody"
		<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
	<tr>
		<td colspan="3" style="padding:0 0 0 10pt">
			<div class="d-flex align-items-stretch" id="options-row">
				<div id="options-label" style="background:var(--bs-secondary-bg);border-right:2px solid var(--bs-border-color);font-weight:600;white-space:nowrap;padding:0.4rem 0.6rem;flex-shrink:0">Options</div>
				<div style="flex:1;min-width:0;padding:0.4rem 0.6rem">
					<div class="accordion" id="optionsAccordion" style="min-width:860px;max-width:860px">
					<div class="accordion-item">
						<h2 class="accordion-header" id="acc-properties-heading">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
								data-bs-target="#acc-properties" aria-expanded="false"
								aria-controls="acc-properties">Properties</button>
						</h2>
						<div id="acc-properties" class="accordion-collapse collapse"
							aria-labelledby="acc-properties-heading" data-bs-parent="#optionsAccordion">
							<div class="accordion-body p-2">
								<pre id="properties" class="ace-panel"><c:out value="${requestScope[actionFormName].properties}" /></pre>
								<textarea id="properties" name="properties" style="display:none;"></textarea>
								<div class="d-flex align-items-center gap-2 mt-1">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorProperties); return false">Clear</button>
								<small class="text-muted ms-auto"><i class="bi bi-keyboard"></i> Ctrl+Space for completions</small>
								</div>
							</div>
						</div>
					</div>
					<div class="accordion-item">
						<h2 class="accordion-header" id="acc-javascript-heading">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
								data-bs-target="#acc-javascript" aria-expanded="false"
								aria-controls="acc-javascript">JavaScript</button>
						</h2>
						<div id="acc-javascript" class="accordion-collapse collapse"
							aria-labelledby="acc-javascript-heading" data-bs-parent="#optionsAccordion">
							<div class="accordion-body p-2">
								<pre id="javascript" class="ace-panel"><c:out value="${requestScope[actionFormName].javascript}" /></pre>
								<textarea id="javascript" name="javascript" style="display:none;"></textarea>
								<div class="d-flex align-items-center gap-2 mt-1">
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorJavascript); return false">Format</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="testSource(editorJavascript); return false">Test</button>
								<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorJavascript); return false">Clear</button>
								</div>
							</div>
						</div>
					</div>
					<div class="accordion-item">
						<h2 class="accordion-header" id="acc-help-heading">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
								data-bs-target="#acc-help" aria-expanded="false"
								aria-controls="acc-help">Help</button>
						</h2>
						<div id="acc-help" class="accordion-collapse collapse"
							aria-labelledby="acc-help-heading" data-bs-parent="#optionsAccordion">
							<div class="accordion-body p-2">
								<div class="scrollable-tab" id="pill-help"></div>
							</div>
						</div>
					</div>
					</div>
				</div>
			</div>
		</td>
	</tr>
	</tbody>
</table>

<script>
	var editorProperties = getEditorProperties(false, true, "properties", "crystal");
	editorProperties.setOptions({ minLines: 10, maxLines: 20 });
	
	// Get the completions from the bean!      		
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
	
	// Lets' populate the help tab and refresh Ace editors when accordion panels open!
	$(document).ready(function() {
		$('#pill-help').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
		var chk = document.getElementById('groupByDate');
		if (chk) document.getElementById('dateFormatRow').style.display = chk.checked ? '' : 'none';

		// Mail address field init
		var mailInput = document.getElementById('userMailInput');
		if (mailInput) { validateMailInput(mailInput); toggleMailRows(); }

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

		// Country flag image next to select
		(function() {
			var VALID_ISO = new Set(['AC','AD','AE','AF','AG','AI','AL','AM','AO','AQ','AR','AS','AT','AU','AW','AX','AZ','BA','BB','BD','BE','BF','BG','BH','BI','BJ','BL','BM','BN','BO','BQ','BR','BS','BT','BV','BW','BY','BZ','CA','CC','CD','CF','CG','CH','CI','CK','CL','CM','CN','CO','CP','CR','CU','CV','CW','CX','CY','CZ','DE','DG','DJ','DK','DM','DO','DZ','EA','EE','EG','EH','ER','ES','ET','EU','EW','FI','FJ','FK','FM','FO','FR','FX','GA','GB','GD','GE','GF','GG','GH','GI','GL','GM','GN','GP','GQ','GR','GS','GT','GU','GW','GY','HK','HM','HN','HR','HT','HU','IC','ID','IE','IL','IM','IN','IO','IQ','IR','IS','IT','JE','JM','JO','JP','KE','KG','KH','KI','KM','KN','KP','KR','KW','KY','KZ','LA','LB','LC','LI','LK','LR','LS','LT','LU','LV','LY','MA','MC','MD','ME','MF','MG','MH','MK','ML','MM','MN','MO','MP','MQ','MR','MS','MT','MU','MV','MW','MX','MY','MZ','NA','NC','NE','NF','NG','NI','NL','NO','NP','NR','NU','NZ','OM','PA','PE','PF','PG','PH','PK','PL','PM','PN','PR','PS','PT','PW','PY','QA','RE','RO','RS','RU','RW','SA','SB','SC','SD','SE','SG','SH','SI','SJ','SK','SL','SM','SN','SO','SR','SS','ST','SV','SX','SY','SZ','TA','TC','TD','TF','TG','TH','TJ','TK','TL','TM','TN','TO','TP','TR','TT','TV','TW','TZ','UA','UG','UK','UM','UN','US','UY','UZ','VA','VC','VE','VG','VI','VN','VU','WF','WS','XK','YE','YT','ZA','ZM','ZR','ZW']);
			var $sel = $('#countryIso');
			var $flag = $('<span class="fi ms-2" style="font-size:1.3em;vertical-align:middle"></span>');
			$sel.after($flag);
			function updateFlag() {
				var iso = ($sel.val() || '').toUpperCase();
				if (VALID_ISO.has(iso)) {
					$flag.attr('class', 'fi fi-' + iso.toLowerCase() + ' ms-2').css('display', 'inline-block');
				} else {
					$flag.hide();
				}
			}
			$sel.on('change', updateFlag);
			updateFlag();
		})();

		// Type tier badge next to select
		(function() {
			var ICONS = {
				'Gold':   '<span class="dest-page-type dest-type-gold"><i class="bi bi-trophy-fill"></i></span>',
				'Silver': '<span class="dest-page-type dest-type-silver"><i class="bi bi-award-fill"></i></span>',
				'Bronze': '<span class="dest-page-type dest-type-bronze"><i class="bi bi-award"></i></span>',
				'Basic':  '<span class="dest-page-type dest-type-basic"><i class="bi bi-patch-check"></i></span>'
			};
			var $sel = $('#destType');
			var $badge = $('<span></span>');
			$sel.parent().append($badge);
			function updateBadge() {
				var label = $sel.find('option:selected').text().trim();
				$badge.html(ICONS[label] || '');
			}
			$sel.on('change', updateBadge);
			updateBadge();
		})();

		// Max File Size picker init
		(function() {
			var $hidden = $('#maxFileSize');
			var $enabled = $('#maxFileSizeEnabled');
			var $inputs = $('#maxFileSizeInputs');
			var $value = $('#maxFileSizeValue');
			var $unit = $('#maxFileSizeUnit');

			function bytesToPicker(bytes) {
				if (bytes <= 0) {
					$enabled.prop('checked', false);
					$inputs.addClass('d-none');
					return;
				}
				$enabled.prop('checked', true);
				$inputs.removeClass('d-none');
				var units = [1073741824, 1048576, 1024, 1];
				var names = ['1073741824', '1048576', '1024', '1'];
				for (var i = 0; i < units.length; i++) {
					if (bytes % units[i] === 0) {
						$value.val(bytes / units[i]);
						$unit.val(names[i]);
						return;
					}
				}
				$value.val(bytes);
				$unit.val('1');
			}

			function pickerToBytes() {
				if (!$enabled.prop('checked')) {
					$hidden.val(-1);
					return;
				}
				var v = parseInt($value.val(), 10);
				var u = parseInt($unit.val(), 10);
				$hidden.val((v > 0 ? v * u : -1));
			}

			bytesToPicker(parseInt($hidden.val(), 10));
			$enabled.on('change', function() {
				$inputs.toggleClass('d-none', !this.checked);
				if (this.checked && !$value.val()) $value.val(1);
				pickerToBytes();
			});
			$value.on('input change', pickerToBytes);
			$unit.on('change', pickerToBytes);
		})();
		document.getElementById('acc-properties').addEventListener('shown.bs.collapse', function() {
			editorProperties.resize(true);
		});
		document.getElementById('acc-javascript').addEventListener('shown.bs.collapse', function() {
			editorJavascript.resize(true);
		});
		// When Help panel opens, scroll to the parameter at the current cursor position
		var helpBtn = document.querySelector('button[data-bs-target="#acc-help"]');
		if (helpBtn) {
			helpBtn.addEventListener('click', function() {
				// Wait for Bootstrap accordion animation to complete (350ms) before scrolling
				setTimeout(function() {
					if (!document.getElementById('acc-help').classList.contains('show')) return;
					var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
					line = line.trim();
					if (line && !line.startsWith('#') && !line.startsWith('//')) {
						var eqIdx = line.indexOf('=');
						var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
						if (paramName) scrollHelpToParam('pill-help', paramName);
					}
				}, 400);
			});
		}
		// Align "Options" label width with the <th> column in table.fields
		var $th = $('table.fields').first().find('th').first();
		if ($th.length) {
			var thRightEdge = $th.offset().left + $th.outerWidth();
			var rowLeft = $('#options-row').offset().left;
			$('#options-label').css('width', (thRightEdge - rowLeft) + 'px');
		}
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
    	// If the Help panel is open, scroll it to the matching parameter
    	if (document.getElementById('acc-help').classList.contains('show')) {
    		var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
    		line = line.trim();
    		// Skip blank lines and comment lines
    		if (line && !line.startsWith('#') && !line.startsWith('//')) {
    			var eqIdx = line.indexOf('=');
    			var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
    			if (paramName) scrollHelpToParam('pill-help', paramName);
    		}
    	}
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
	editorJavascript.setOptions({ minLines: 10, maxLines: 20 });

	var textareaProperties = $('textarea[name="properties"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	var textareaJavascript = $('textarea[name="javascript"]');
	textareaJavascript.closest('form').submit(function() {
		textareaJavascript.val(editorJavascript.getSession().getValue());
	});

	makeResizable(editorProperties);
	makeResizable(editorJavascript);

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
			return parts.length ? '= ' + parts.join(' ') : '';
		}
		$('.dur-picker').each(function() {
			var $picker = $(this);
			var $hidden = $('#' + $picker.data('target'));
			var p = msToHM($hidden.val());
			if (p.h) $picker.find('.dur-h').val(p.h);
			if (p.m) $picker.find('.dur-m').val(p.m);
			$picker.find('.dur-display').text(fmtHM(p.h, p.m));
			$picker.find('input[type=number]').on('input', function() {
				var h = parseInt($picker.find('.dur-h').val()) || 0;
				var m = parseInt($picker.find('.dur-m').val()) || 0;
				$hidden.val(hmToMs(h, m));
				$picker.find('.dur-display').text(fmtHM(h, m));
			});
		});
	})();


	// Enforce minimum 1 minute on Start Frequency picker
	function enforceStartFreqMin() {
		var $hidden = $('#startFrequency');
		var ms = parseInt($hidden.val()) || 0;
		if (ms < 60000) {
			$hidden.val(60000);
			var $picker = $('[data-target="startFrequency"]');
			$picker.find('.dur-h').val(0);
			$picker.find('.dur-m').val(1);
			$picker.find('.dur-display').text('= 1m');
		}
	}
	$('[data-target="startFrequency"] input[type=number]').on('change blur', function() {
		if ($('#maxStart').val() != 0) enforceStartFreqMin();
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
							min : -1,
							max : 10,
							value : <c:out value="${requestScope[actionFormName].retryCount}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#retryCountHandle").text(
										value == -1 ? "Off" : value);
								$("#retryCount").val(value);
							},
							slide : function(event, ui) {
								$("#retryCountHandle").text(
										ui.value == -1 ? "Off" : ui.value);
								$("#retryCount").val(ui.value);
							}
						});

		$("#maxStartSlider").slider({
			min : 0,
			max : 30,
			value : <c:out value="${requestScope[actionFormName].maxStart}"/>,
			range : "min",
			animate : true,
			create : function() {
				var value = $(this).slider("value");
				$("#maxStartHandle").text(value == 0 ? "Off" : value);
				$("#maxStart").val(value);
				$("#startFrequencyRow").toggle(value != 0);
				if (value != 0) enforceStartFreqMin();
			},
			slide : function(event, ui) {
				$("#maxStartHandle").text(ui.value == 0 ? "Off" : ui.value);
				$("#maxStart").val(ui.value);
				$("#startFrequencyRow").toggle(ui.value != 0);
				if (ui.value != 0) enforceStartFreqMin();
			}
		});
		$("#maxRequeueSlider")
				.slider(
						{
							min : 0,
							max : 10,
							value : <c:out value="${requestScope[actionFormName].maxRequeue}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxRequeueHandle").text(
										value == 0 ? "Off" : value);
								$("#maxRequeue").val(value);
							},
							slide : function(event, ui) {
								$("#maxRequeueHandle").text(
										ui.value == 0 ? "Off" : ui.value);
								$("#maxRequeue").val(ui.value);
							}
						});
		$("#maxPendingSlider")
				.slider(
						{
							min : 0,
							max : 100,
							step : 5,
							value : <c:out value="${requestScope[actionFormName].maxPending/1000}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxPendingHandle").text(
										value == 0 ? "Off" : value + "k");
								$("#maxPending").val(value * 1000);
							},
							slide : function(event, ui) {
								$("#maxPendingHandle").text(
										ui.value == 0 ? "Off" : ui.value + "k");
								$("#maxPending").val(ui.value * 1000);
							}
						});
	});
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
		if (layerName != 'disseminationHostChooser')
			hide('disseminationHostChooser');
		if (layerName != 'acquisitionHostChooser')
			hide('acquisitionHostChooser');
		if (layerName != 'proxyHostChooser')
			hide('proxyHostChooser');
		if (layerName != 'userChooser')
			hide('userChooser');
		if (layerName != 'aliasChooser')
			hide('aliasChooser');
		if (layerName != 'policyChooser')
			hide('policyChooser');
	}
	$('#name, #toDestination').on('input', function() {
		const regex = /^[a-zA-Z0-9_-]+$/;
		const $this = $(this);
		const value = $this.val();
		if (!regex.test(value)) {
			// Remove all invalid characters
			$this.val(value.replace(/[^a-zA-Z0-9_-]/g, ''));
		}
	});

	// Live preview for Java SimpleDateFormat patterns
	(function() {
		// Map Java SimpleDateFormat tokens to JS Date getters (ordered longest first)
		var TOKEN_RE = /(G+|y{1,4}|Y{1,4}|M{1,4}|L{1,4}|w{1,2}|W|D{1,3}|d{1,2}|F|E{1,4}|u|a|H{1,2}|k{1,2}|K{1,2}|h{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,4}|X{1,3}|'[^']*'|.)/g;
		var MONTHS_SHORT = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
		var MONTHS_LONG  = ['January','February','March','April','May','June','July','August','September','October','November','December'];
		var DAYS_SHORT   = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
		var DAYS_LONG    = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
		function pad(n, w) { return String(n).padStart(w, '0'); }
		var KNOWN = /^(G+|y+|Y+|M+|L+|w+|W|D+|d+|F|E+|u|a|H+|k+|K+|h+|m+|s+|S+|z+|Z+|X+|'[^']*')$/;

		function formatJava(pattern, d) {
			var unknowns = [];
			var result = pattern.replace(TOKEN_RE, function(tok) {
				var n = tok.length;
				// Quoted literal
				if (tok.charAt(0) === "'") {
					var inner = tok.slice(1, -1);
					return inner === '' ? "'" : inner;
				}
				var c = tok.charAt(0);
				switch(c) {
					case 'G': return d.getFullYear() >= 0 ? 'AD' : 'BC';
					case 'y': case 'Y':
						var yr = d.getFullYear();
						return n === 2 ? pad(yr % 100, 2) : pad(yr, n < 4 ? n : 4);
					case 'M': case 'L':
						var mo = d.getMonth();
						if (n >= 4) return MONTHS_LONG[mo];
						if (n === 3) return MONTHS_SHORT[mo];
						return pad(mo + 1, n < 2 ? 1 : 2);
					case 'd': return pad(d.getDate(), n < 2 ? 1 : 2);
					case 'H': return pad(d.getHours(), n < 2 ? 1 : 2);
					case 'h': var h = d.getHours() % 12; return pad(h === 0 ? 12 : h, n < 2 ? 1 : 2);
					case 'K': return pad(d.getHours() % 12, n < 2 ? 1 : 2);
					case 'k': return pad(d.getHours() === 0 ? 24 : d.getHours(), n < 2 ? 1 : 2);
					case 'm': return pad(d.getMinutes(), n < 2 ? 1 : 2);
					case 's': return pad(d.getSeconds(), n < 2 ? 1 : 2);
					case 'S': return pad(d.getMilliseconds(), n < 3 ? n : 3);
					case 'E':
						var wd = d.getDay();
						return n >= 4 ? DAYS_LONG[wd] : DAYS_SHORT[wd];
					case 'a': return d.getHours() < 12 ? 'AM' : 'PM';
					case 'D':
						var start = new Date(d.getFullYear(), 0, 0);
						var diff = d - start;
						return pad(Math.floor(diff / 86400000), n < 3 ? n : 3);
					case 'w':
						// ISO week approx
						var jan1 = new Date(d.getFullYear(), 0, 1);
						var wk = Math.ceil(((d - jan1) / 86400000 + jan1.getDay() + 1) / 7);
						return pad(wk, n < 2 ? 1 : 2);
					case 'F': return String(Math.ceil(d.getDate() / 7));
					case 'u': return String(d.getDay() === 0 ? 7 : d.getDay());
					case 'z': case 'Z': case 'X': return '';
					default:
						// Only flag unrecognised letters — non-letter chars are always valid literals
						if (/^[a-zA-Z]/.test(tok) && !KNOWN.test(tok)) unknowns.push(tok);
						return tok;
				}
			});
			return { text: result, unknowns: unknowns };
		}

		function updatePreview() {
			var pattern = $('#dateFormatInput').val().trim();
			var $preview = $('#dateFormatPreview');
			if (!pattern) { $preview.html(''); return; }
			var res = formatJava(pattern, new Date());
			var html = '<span class="text-secondary"><i class="bi bi-calendar2-check me-1"></i>Preview: <code>' + $('<span>').text(res.text).html() + '</code></span>';
			if (res.unknowns.length) {
				var unk = res.unknowns.map(function(u) { return '<code>' + $('<span>').text(u).html() + '</code>'; }).join(', ');
				html += ' <span class="text-warning ms-2"><i class="bi bi-exclamation-triangle me-1"></i>Unknown token(s): ' + unk + '</span>';
			}
			$preview.html(html);
		}

		$(document).ready(function() {
			updatePreview();
			$('#dateFormatInput').on('input change', updatePreview);
		});
	})();

	function toggleMailRows() {
		var show = document.getElementById('userMailInput').value.trim() !== '';
		['mailOnUpdateRow','mailOnStartRow','mailOnEndRow','mailOnErrorRow'].forEach(function(id) {
			document.getElementById(id).style.display = show ? '' : 'none';
		});
	}
</script>