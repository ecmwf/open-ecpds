<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

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
/* OL zoom buttons: flex centering for +/−/fit icons */
.ol-zoom button {
    display: flex !important; align-items: center !important;
    justify-content: center !important; line-height: 1 !important;
}
</style>

<tiles:useAttribute id="actionFormName" name="action.form.name"
	classname="java.lang.String" />
<tiles:useAttribute name="isInsert" classname="java.lang.String" />

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
</c:if>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-tag text-primary"></i>
<span class="fw-semibold">Identity</span>
</div>
<div class="card-body">
<div class="row g-3">
<c:if test="${isInsert != 'true'}">
<div class="col-sm-6">
<label class="form-label mb-1">ID</label>
<div class="form-control form-control-sm bg-body-secondary">${requestScope[actionFormName].name}</div>
<html:hidden property="name" />
</div>
</c:if>
<div class="col-sm-6">
<label for="comment" class="form-label mb-1">Comment</label>
<html:text property="comment" styleId="comment" styleClass="form-control form-control-sm" />
</div>
<c:if test="${isInsert == 'true'}">
<div class="col-sm-6">
<label for="type" class="form-label mb-1">Type <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Select the Host Type" tabindex="0"></i></label>
<select id="type" name="type" class="form-select form-select-sm">
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
</select>
</div>
</c:if>
<c:if test="${isInsert == 'false'}">
<div class="col-sm-6">
<label class="form-label mb-1">Type</label>
<div class="form-control form-control-sm bg-body-secondary">${requestScope[actionFormName].type}</div>
<html:hidden property="type" />
</div>
</c:if>
<div class="col-sm-6">
<label for="networkCode" class="form-label mb-1">Label/Network <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="This is only used for the monitoring display (shown in the Host column)" tabindex="0"></i></label>
<bean:define id="networks" name="hostActionForm"
property="networkOptions" type="java.util.Collection" />
<html:select property="networkCode" styleId="networkCode" styleClass="form-select form-select-sm">
<html:options collection="networks" property="name"
labelProperty="value" />
</html:select>
</div>
<div class="col-sm-6">
<label for="transferGroup" class="form-label mb-1">Transfer Group <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="From which Data Movers this host can be accessed?" tabindex="0"></i></label>
<bean:define id="groups" name="hostActionForm"
property="transferGroupOptions" type="java.util.Collection" />
<html:select property="transferGroup" styleId="transferGroup" styleClass="form-select form-select-sm">
<html:options collection="groups" property="name"
labelProperty="name" />
</html:select>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-ethernet text-primary"></i>
<span class="fw-semibold">Connection</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-6">
<label for="transferMethod" class="form-label mb-1">Transfer Protocol <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Select the Transfer Protocol to connect to the remote site" tabindex="0"></i></label>
<select id="transferMethod" name="transferMethod" class="form-select form-select-sm">
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
</select>
</div>
<div class="col-sm-6">
<label for="host" class="form-label mb-1">Hostname/IP <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="DNS hostname or IPv4/IPv6 address" tabindex="0"></i></label>
<div class="d-flex align-items-center gap-2">
<input id="host" name="host" type="text"
class="form-control form-control-sm"
value="${requestScope[actionFormName].host}"
oninput="validateHostInput(this)" />
<span id="hostFeedback"></span>
</div>
</div>
<div class="col-sm-6">
<label for="nickName" class="form-label mb-1">Nickname <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="As it will appear on the Destination page. Alphanumeric only; '-', '_' and '.' may be used as separators (e.g. 'test_file', 'host.name'), not at the start, end, or consecutively." tabindex="0"></i></label>
<div class="d-flex align-items-center gap-2">
<input id="nickName" name="nickName" type="text"
class="form-control form-control-sm"
value="${requestScope[actionFormName].nickName}"
pattern="[A-Za-z0-9]+([_\-\.][A-Za-z0-9]+)*"
title="Use alphanumeric characters, with '-', '_' or '.' as separators only (e.g. 'test_file', 'host.name', not '.test' or 'test.'). Maximum 128 characters."
maxlength="128"
required
oninput="validatePatternInput(this,'nickNameFeedback')" />
<span id="nickNameFeedback"></span>
</div>
</div>
<div class="col-sm-6">
<label for="login" class="form-label mb-1">Login</label>
<html:text property="login" styleId="login" styleClass="form-control form-control-sm" />
</div>
<div class="col-sm-6">
<label for="passwd" class="form-label mb-1">Password</label>
<html:text property="passwd" styleId="passwd" styleClass="form-control form-control-sm" />
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-speedometer2 text-primary"></i>
<span class="fw-semibold">Throughput</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-6">
<label class="form-label mb-1">Max Connections</label>
<div id="maxConnectionsSlider" style="width: 210px; margin: 6px;">
<input type="hidden" name="maxConnections" id="maxConnections">
<div
title="Maximum number of parallel connections authorized at a time on this specific host"
id="maxConnectionsHandle" class="ui-slider-handle"></div>
</div>
</div>
<div class="col-sm-6">
<label class="form-label mb-1">Retry Count</label>
<div id="retryCountSlider" style="width: 210px; margin: 6px;">
<input type="hidden" name="retryCount" id="retryCount">
<div
title="Maximum number of consecutive unsuccessful transfers before to move to the next host and declare this host not valid"
id="retryCountHandle" class="ui-slider-handle"></div>
</div>
</div>
<div class="col-sm-6">
<label for="retryFrequency" class="form-label mb-1">Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry a failed transfer on this host" tabindex="0"></i></label>
<input type="hidden" name="retryFrequency" id="retryFrequency"
value='<c:out value="${requestScope[actionFormName].retryFrequency}"/>'>
<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="retryFrequency">
<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
<span class="text-muted small">h</span>
<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
<span class="text-muted small">m</span>
<span class="text-muted small ms-1 dur-display"></span>
</div>
</div>
<div class="col-sm-6">
<label for="filterName" class="form-label mb-1">Data Compression</label>
<div class="d-flex align-items-center gap-2">
<bean:define id="filters" name="hostActionForm"
property="filterNameOptions" type="java.util.Collection" />
<html:select property="filterName" styleId="filterName" styleClass="form-select form-select-sm w-auto">
<html:options collection="filters" property="name"
labelProperty="name" />
</html:select>
</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" id="hostDirCardHeader" style="background:var(--bs-secondary-bg)">
<i class="bi bi-folder2-open text-primary"></i>
<span class="fw-semibold">Directory</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-12">
<div id='dirType'>
<input type='radio' id='istext' name='dirType' />Plain Text <input
type='radio' id='isjs' name='dirType' />JavaScript <input
type='radio' id='ispython' name='dirType' />Python
</div>
<small id="dirTypeMismatchWarning" style="display:none;color:var(--bs-warning-text-emphasis)">
  <i class="bi bi-exclamation-triangle-fill"></i> <span id="dirTypeMismatchText"></span>
</small>
<div class="ace-panel">
<pre id="dir">
<c:out value="${requestScope[actionFormName].dir}" />
</pre> <textarea id="dir" name="dir" style="display: none;"></textarea>
</div>
<div class="d-flex align-items-center gap-2 mt-2" style="flex-wrap:wrap;">
<button type="button" id="formatDir" name="formatDir" class="btn btn-sm btn-outline-secondary"
onclick="formatSource(editorDir); return false">Format</button>
<span style="position:relative;display:inline-block">
<button type="button" id="testDir" name="testDir" class="btn btn-sm btn-outline-secondary"
onclick="testSource(editorDir); return false">Test</button>
<span id="testDirOverlay" style="display:none;position:absolute;inset:0;cursor:not-allowed"
  data-bs-toggle="tooltip" data-bs-title="Fix the errors in the editor before testing"></span>
</span>
<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorDir); return false">Clear</button>

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
<optgroup label="Data Mover">
<option>$transferServer[name]</option>
<option>$transferServer[host]</option>
<option>$transferServer[port]</option>
<option>$moverName</option>
</optgroup>
</select>
</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-sliders text-primary"></i>
<span class="fw-semibold">Options</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-12">
<div class="accordion" id="hostOptionsAccordion">
<div class="accordion-item">
<h2 class="accordion-header" id="hostAccHeadProperties" style="position:relative;">
<button class="accordion-button collapsed" id="hostAccPropertiesBtn" type="button" data-bs-toggle="collapse" data-bs-target="#hostAccProperties" aria-expanded="false" aria-controls="hostAccProperties">
Properties
</button>
<span role="button" tabindex="0" class="acc-help-btn" id="hostEditPropsHelpBtn"
onclick="openHostEditHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openHostEditHelp();" title="Open properties reference">
<i class="bi bi-question-circle"></i>
</span>
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
<button class="accordion-button collapsed" id="hostAccJavascriptBtn" type="button" data-bs-toggle="collapse" data-bs-target="#hostAccJavascript" aria-expanded="false" aria-controls="hostAccJavascript">
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
<span style="position:relative;display:inline-block">
<button type="button" id="testJs" class="btn btn-sm btn-outline-secondary" onclick="testSource(editorJavascript); return false">Test</button>
<span id="testJsOverlay" style="display:none;position:absolute;inset:0;cursor:not-allowed"
  data-bs-toggle="tooltip" data-bs-title="Fix the errors in the editor before testing"></span>
</span>
<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorJavascript); return false">Clear</button>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-geo-alt text-primary"></i>
<span class="fw-semibold">Location</span>
</div>
<div class="card-body">
<div class="row g-3 align-items-end">
<div class="col-sm-4">
<div class="d-flex align-items-center gap-2 flex-wrap">
<div class="form-check form-switch mb-0">
<html:checkbox styleId="automaticLocation" property="automaticLocation" styleClass="form-check-input" />
<label class="form-check-label" for="automaticLocation">Automatic Location</label>
</div>
<i class="bi bi-question-circle text-muted" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Try to get the latitude/longitude from the IP address" tabindex="0"></i>
</div>
</div>
<div class="col-sm-3">
<label for="latitudeField" class="form-label mb-1">Latitude (&deg;)</label>
<html:text property="latitude" styleId="latitudeField" styleClass="form-control form-control-sm" />
</div>
<div class="col-sm-3">
<label for="longitudeField" class="form-label mb-1">Longitude (&deg;)</label>
<html:text property="longitude" styleId="longitudeField" styleClass="form-control form-control-sm" />
</div>
<div class="col-sm-2">
<label class="form-label mb-1">&nbsp;</label>
<button type="button" id="pickOnMapBtn" class="btn btn-sm btn-outline-secondary w-100"
        onclick="openMapPicker()"
        title="Click a point on the map to set coordinates">
    <i class="bi bi-map me-1"></i>Pick on map
</button>
</div>
</div>
</div>
</div>

<%-- Map coordinate picker modal --%>
<div class="modal fade" id="mapPickerModal" tabindex="-1" aria-labelledby="mapPickerModalLabel" aria-hidden="true">
<div class="modal-dialog modal-lg modal-dialog-centered">
<div class="modal-content">
<div class="modal-header py-2">
    <h6 class="modal-title" id="mapPickerModalLabel"><i class="bi bi-geo-alt-fill me-2 text-primary"></i>Pick Location on Map</h6>
    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
</div>
<div class="modal-body p-0" style="position:relative;">
    <div id="mapPickerMap" style="height:420px; width:100%;"></div>
    <div id="mapPickerCoords" style="
        position:absolute; bottom:10px; left:50%; transform:translateX(-50%);
        background:rgba(20,20,20,0.82); color:#e8e8e8; border-radius:6px;
        padding:0.3rem 0.75rem; font-size:0.78rem; font-family:monospace;
        pointer-events:none; backdrop-filter:blur(4px);
        border:1px solid rgba(255,255,255,0.1);
        white-space:nowrap;">
        Click anywhere to set location
    </div>
</div>
<div class="modal-footer py-2">
    <span class="text-muted small me-auto"><i class="bi bi-info-circle me-1"></i>Click anywhere on the map to place the pin</span>
    <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
    <button type="button" class="btn btn-sm btn-primary" id="mapPickerConfirm" onclick="_confirmMapPicker()" disabled>
        <i class="bi bi-check-lg me-1"></i>Confirm
    </button>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-bell text-primary"></i>
<span class="fw-semibold">Monitoring</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-6">
<div class="form-check form-switch">
<html:checkbox property="check" styleClass="form-check-input" styleId="checkFlag" onchange="toggleCheckRows()" />
<label class="form-check-label" for="checkFlag">Check <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Allow automatically checking the host by transferring a test file on a regular schedule." tabindex="0"></i></label>
</div>
</div>
<div class="col-sm-6" id="notifyOnceRow">
<div class="form-check form-switch">
<html:checkbox property="notifyOnce" styleClass="form-check-input" styleId="notifyOnce" />
<label class="form-check-label" for="notifyOnce">Notify Once <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Only notify once for consecutive errors." tabindex="0"></i></label>
</div>
</div>
<div class="col-sm-6" id="checkFrequencyRow">
<label for="checkFrequency" class="form-label mb-1">Check Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay in ms between two checks." tabindex="0"></i></label>
<input type="hidden" name="checkFrequency" id="checkFrequency"
value='<c:out value="${requestScope[actionFormName].checkFrequency}"/>'>
<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="checkFrequency">
<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
<span class="text-muted small">h</span>
<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
<span class="text-muted small">m</span>
<span class="text-muted small ms-1 dur-display"></span>
</div>
</div>
<div class="col-sm-6" id="checkFilenameRow">
<label for="checkFilename" class="form-label mb-1">Check Filename <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the name for the temporary test file." tabindex="0"></i></label>
<html:text property="checkFilename" styleId="checkFilename" styleClass="form-control form-control-sm" />
</div>
<div class="col-12" id="acquisitionFrequencySpacer"></div>
<div class="col-sm-6" id="acquisitionFrequencyRow">
<label for="acquisitionFrequency" class="form-label mb-1">Acquisition Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If this host is for acquisition then define the delay in ms between two listings of remote files" tabindex="0"></i></label>
<input type="hidden" name="acquisitionFrequency" id="acquisitionFrequency"
value='<c:out value="${requestScope[actionFormName].acquisitionFrequency}"/>'>
<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="acquisitionFrequency">
<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
<span class="text-muted small">h</span>
<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
<span class="text-muted small">m</span>
<span class="text-muted small ms-1 dur-display"></span>
</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-envelope text-primary"></i>
<span class="fw-semibold">Notifications</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-6">
<label for="owner" class="form-label mb-1">Owner <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="For the record" tabindex="0"></i></label>
<bean:define id="users" name="hostActionForm"
property="ownerOptions" type="java.util.Collection" />
<html:select property="owner" styleId="owner" styleClass="form-select form-select-sm">
<html:options collection="users" property="name"
labelProperty="comment" />
</html:select>
</div>
<div class="col-sm-6">
<label for="userMailInput" class="form-label mb-1">Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="One or more email addresses used when sending notifications. Separate multiple addresses with ';' (e.g. 'a@example.com;b@example.com')." tabindex="0"></i></label>
<div class="d-flex align-items-center gap-2">
<input type="text" name="userMail" id="userMailInput"
class="form-control form-control-sm"
value='<c:out value="${requestScope[actionFormName].userMail}"/>'
title="Enter one or more email addresses separated by ';'"
oninput="validateMailInput(this); toggleMailRows()" />
<span id="userMailFeedback"></span>
</div>
</div>
<div class="col-sm-6" id="mailOnSuccessRow">
<div class="form-check form-switch">
<html:checkbox property="mailOnSuccess" styleClass="form-check-input" styleId="mailOnSuccess" />
<label class="form-check-label" for="mailOnSuccess">Mail on Success</label>
</div>
</div>
<div class="col-sm-6" id="mailOnErrorRow">
<div class="form-check form-switch">
<html:checkbox property="mailOnError" styleClass="form-check-input" styleId="mailOnError" />
<label class="form-check-label" for="mailOnError">Mail on Error</label>
</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-toggles text-primary"></i>
<span class="fw-semibold">Status</span>
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-4">
<div class="form-check form-switch">
<html:checkbox property="active" styleClass="form-check-input" styleId="active" />
<label class="form-check-label" for="active">Enabled</label>
</div>
</div>
</div>
</div>
</div>


<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="hostEditHelpOffcanvas"
     aria-labelledby="hostEditHelpOffcanvasLabel" style="width:min(480px,42vw);">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <h6 class="offcanvas-title mb-0 fw-semibold" id="hostEditHelpOffcanvasLabel">
            <i class="bi bi-book me-2 text-primary"></i>Properties Reference
        </h6>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
        <div id="hostEditHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
        <div id="hostEditHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
    </div>
</div>

<link rel="stylesheet" href="/openlayer/ol.css"/>
<script src="/openlayer/ol.js"></script>
<script>
	var editorDir = getEditorProperties(false, true, "dir", "toml");
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
		$('#hostEditHelpContent').html(getHelpHtmlContent(
				completions.filter(function(item) {
	           		var moduleName = item.caption.split(".")[0];
	       	    	if (transferModuleName !== "ecaccess" && transferModuleNames.includes(moduleName) && moduleName !== transferModuleName) {
	       	    		return false;
	       	    	} else {
	       	    		return !isInvalidModuleName(moduleName);
	       	    	}
	    	    }), 'Available Options for Host of Type ' + getHostType() + ' with Transfer Method ' + getTransferMethodName()))
		/* Move the group nav pills to the fixed slot above the scrollable area */
		var navEl = document.querySelector('#hostEditHelpContent .help-nav');
		if (navEl) document.getElementById('hostEditHelpNav').appendChild(navEl);
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
    	bootstrap.Collapse.getOrCreateInstance(document.getElementById('hostAccProperties')).show();
    	checkEachLine(editorProperties, 'hostAccPropertiesBtn');
    	populateHelpTab();
	});
	
   	var selectElement = document.getElementById("type");
   	if (selectElement && selectElement.tagName === "SELECT") {
		document.getElementById("type").addEventListener("change", function() {
    		bootstrap.Collapse.getOrCreateInstance(document.getElementById('hostAccProperties')).show();
    		checkEachLine(editorProperties, 'hostAccPropertiesBtn');
    		populateHelpTab();
    		toggleAcquisitionRow();
		});
   	};

	// Update annotations and markers on cursor movement
    editorProperties.addEventListener("changeSelection", function (event) {
    	checkEachLine(editorProperties, 'hostAccPropertiesBtn');
    	/* Live-track help offcanvas when open */
    	var _oc = document.getElementById('hostEditHelpOffcanvas');
    	if (_oc && _oc.classList.contains('show')) {
    		_scrollEditHelpToCursor();
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
  		checkEachLine(editorProperties, 'hostAccPropertiesBtn');
	});
	
	var editorJavascript = getEditorProperties(false, false, "javascript", "javascript");
	editorJavascript.setOptions({minLines: 10, maxLines: 20});

	document.getElementById('hostAccProperties').addEventListener('shown.bs.collapse', function() {
		editorProperties.resize(true);
	});
	document.getElementById('hostAccJavascript').addEventListener('shown.bs.collapse', function() {
		editorJavascript.resize(true);
	});
	/* Help offcanvas */
	function _scrollEditHelpToCursor() {
		var row = editorProperties.selection.getCursor().row;
		var line = editorProperties.session.getLine(row) || '';
		line = line.trim();
		if (line && !line.startsWith('#') && !line.startsWith('//')) {
			var eqIdx = line.indexOf('=');
			var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
			if (paramName) scrollHelpToParam('hostEditHelpContent', paramName);
		}
	}
	window.openHostEditHelp = function() {
		var el = document.getElementById('hostEditHelpOffcanvas');
		if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
	};
	var _editHelpOffcanvasEl = document.getElementById('hostEditHelpOffcanvas');
	if (_editHelpOffcanvasEl) {
		_editHelpOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
			var btn = document.getElementById('hostEditPropsHelpBtn');
			if (btn) btn.classList.add('acc-help-active');
		});
		_editHelpOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
			_scrollEditHelpToCursor();
		});
		_editHelpOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
			var btn = document.getElementById('hostEditPropsHelpBtn');
			if (btn) btn.classList.remove('acc-help-active');
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
	// Initial full validation pass — highlights all errors/warnings on page load
	checkEachLine(editorProperties, 'hostAccPropertiesBtn');

	function updateTestJsBtn() {
		var hasError = editorJavascript.getSession().getAnnotations().some(function(a) { return a.type === 'error'; });
		$('#testJs').prop('disabled', hasError).toggleClass('disabled', hasError);
		$('#testJsOverlay').toggle(hasError);
		applyAnnotationMarkers(editorJavascript, 'hostAccJavascriptBtn');
	}
	editorJavascript.getSession().on('changeAnnotation', updateTestJsBtn);

	window.addEventListener('resize', function() {
		editorDir.resize(true);
		editorProperties.resize(true);
		editorJavascript.resize(true);
	});

	$('select[name="type"]').on(
			'change',
			function() {
				var str = $(this).find(":selected").val();
				$('#dirParametersDiss').toggle(str === "" || str === "Dissemination");
				$('#dirParametersAcq').toggle(str === "Acquisition");
			});

	$('#is' + getEditorType(editorDir)).prop('checked', true);

	function updateTestDirBtn() {
		var hasError = editorDir.getSession().getAnnotations().some(function(a) { return a.type === 'error'; });
		$('#testDir').prop('disabled', hasError).toggleClass('disabled', hasError);
		$('#testDirOverlay').toggle(hasError);
		applyAnnotationMarkers(editorDir, 'hostDirCardHeader');
	}
	editorDir.getSession().on('changeAnnotation', updateTestDirBtn);

	// For Python mode: check syntax on every edit (Skulpt, no built-in worker)
	// For Plain Text mode: check for accidental JS/Python content on every edit
	var _dirMismatchTimer = null;
	editorDir.session.on('change', function() {
		var modeId = editorDir.session.getMode().$id;
		if (modeId === 'ace/mode/python') {
			debouncedCheckPythonSyntax(editorDir, 'hostDirCardHeader');
		} else if (modeId === 'ace/mode/toml') {
			clearTimeout(_dirMismatchTimer);
			_dirMismatchTimer = setTimeout(function() {
				updateDirTypeMismatchWarning('dirTypeMismatchWarning', 'dirTypeMismatchText', editorDir, true);
			}, 600);
		}
	});

	$("#dirType").on('change', function() {
		if ($("#istext").is(":checked")) {
			editorDir.session.setMode("ace/mode/toml");
			editorDir.session.clearAnnotations();
			applyAnnotationMarkers(editorDir, 'hostDirCardHeader');
			updateDirTypeMismatchWarning('dirTypeMismatchWarning', 'dirTypeMismatchText', editorDir, true);
			$('#formatDir').prop('style', "display: none;");
			$('#testDir').prop('style', "display: none;");
		} else if ($("#ispython").is(":checked")) {
			editorDir.session.setMode("ace/mode/python");
			editorDir.session.clearAnnotations();
			checkPythonSyntax(editorDir, 'hostDirCardHeader');
			updateDirTypeMismatchWarning('dirTypeMismatchWarning', 'dirTypeMismatchText', editorDir, false);
			$('#formatDir').prop('style', "display: none;");
			$('#testDir').prop('style', "display: none;");
		} else {
			editorDir.session.setMode("ace/mode/javascript");
			updateDirTypeMismatchWarning('dirTypeMismatchWarning', 'dirTypeMismatchText', editorDir, false);
			$('#formatDir').prop('style', "");
			$('#testDir').prop('style', "");
		}
	}).triggerHandler('change');

	var type = "${requestScope[actionFormName].type}";
	$('#dirParametersDiss').toggle(type === "" || type === "Dissemination");
	$('#dirParametersAcq').toggle(type === "Acquisition");
	$('#dirParametersDiss').on('change', function() {
		var str = $(this).find(":selected").val();
		$(this).prop("selectedIndex", 0);
		editorDir.insert(str);
		editorDir.focus();
	});
	$('#dirParametersAcq').on('change', function() {
		var str = $(this).find(":selected").val();
		$(this).prop("selectedIndex", 0);
		editorDir.insert(str);
		editorDir.focus();
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
    	var pickBtn  = document.getElementById("pickOnMapBtn");
    	var disabled = checkbox.checked;
    	latitude.disabled = disabled;
    	longitude.disabled = disabled;
    	if (pickBtn) pickBtn.disabled = disabled;
    }
    /* ---- Map coordinate picker -------------------------------- */
    var _pickerMap = null, _pickerPin = null, _pickerSrc = null, _pickedLat = null, _pickedLon = null;

    function _pickerUpdateDisplay(lat, lon) {
        var el = document.getElementById('mapPickerCoords');
        if (el) el.textContent = '\uD83D\uDCCD  ' + lat.toFixed(6) + '\u00b0,  ' + lon.toFixed(6) + '\u00b0';
    }

    function openMapPicker() {
        var autoChk = document.getElementById('automaticLocation');
        if (autoChk && autoChk.checked) return;

        var modalEl = document.getElementById('mapPickerModal');
        bootstrap.Modal.getOrCreateInstance(modalEl).show();

        /* Init map after Bootstrap's fade animation (350ms) */
        setTimeout(function() {
            if (!window.ol) return;
            if (_pickerMap) { _pickerMap.updateSize(); return; }

            _pickerSrc = new ol.source.Vector();
            _pickerPin = new ol.Feature();
            _pickerPin.setStyle(new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 8,
                    fill: new ol.style.Fill({ color: '#0d6efd' }),
                    stroke: new ol.style.Stroke({ color: '#fff', width: 2.5 })
                })
            }));

            var initLat = parseFloat(document.getElementById('latitudeField').value);
            var initLon = parseFloat(document.getElementById('longitudeField').value);
            var hasInit = isFinite(initLat) && isFinite(initLon) && (initLat !== 0 || initLon !== 0);
            if (hasInit) {
                _pickerPin.setGeometry(new ol.geom.Point(ol.proj.fromLonLat([initLon, initLat])));
                _pickerSrc.addFeature(_pickerPin);
                _pickedLat = initLat; _pickedLon = initLon;
                _pickerUpdateDisplay(initLat, initLon);
                document.getElementById('mapPickerConfirm').disabled = false;
            }

            _pickerMap = new ol.Map({
                target: 'mapPickerMap',
                controls: ol.control.defaults.defaults({ rotate: false }),
                layers: [
                    new ol.layer.Tile({ source: new ol.source.OSM({ attributions: [] }) }),
                    new ol.layer.Vector({ source: _pickerSrc, zIndex: 10 })
                ],
                view: new ol.View({
                    center: hasInit ? ol.proj.fromLonLat([initLon, initLat]) : ol.proj.fromLonLat([10, 48]),
                    zoom: hasInit ? 8 : 3
                })
            });
            _pickerMap.getTargetElement().style.cursor = 'crosshair';
            _pickerMap.on('click', function(evt) {
                var lonLat = ol.proj.toLonLat(evt.coordinate);
                _pickedLon = Math.round(lonLat[0] * 1e6) / 1e6;
                _pickedLat = Math.round(lonLat[1] * 1e6) / 1e6;
                _pickerPin.setGeometry(new ol.geom.Point(evt.coordinate));
                if (_pickerSrc.getFeatures().length === 0) _pickerSrc.addFeature(_pickerPin);
                _pickerUpdateDisplay(_pickedLat, _pickedLon);
                document.getElementById('mapPickerConfirm').disabled = false;
            });
        }, 350);
    }

    function _confirmMapPicker() {
        if (_pickedLat === null) return;
        document.getElementById('latitudeField').value  = _pickedLat.toFixed(6);
        document.getElementById('longitudeField').value = _pickedLon.toFixed(6);
        bootstrap.Modal.getInstance(document.getElementById('mapPickerModal')).hide();
    }

    toggleLocationFields();
    document.getElementById('automaticLocation').addEventListener('change', toggleLocationFields);
    window.onload = function() {
    	var mailInput = document.getElementById('userMailInput');
    	if (mailInput) validateMailInput(mailInput);
    	var hostInput = document.getElementById('host');
    	if (hostInput) validateHostInput(hostInput);
    	var nickInput = document.getElementById('nickName');
    	if (nickInput) validatePatternInput(nickInput, 'nickNameFeedback');
	};

</script>
