<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
#properties {
	width: 100%;
	min-width: 300px;
	height: 375px;
	resize: vertical;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
	margin-top: 8px;
	margin-bottom: 8px;
}

#javascript {
	width: 100%;
	min-width: 300px;
	height: 375px;
	resize: vertical;
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

.dest-listbox {
    width: 100%;
    min-width: 0;
    max-height: clamp(180px, 40vh, 380px);
    overflow-y: auto;
}
	max-width: 100%;
	overflow: hidden;
	border: solid 1px var(--bs-border-color);
	border-radius: 4px;
}

#dest-common-fields {
	display: none !important;
}

#dest-common-fields.d-none ~ .dest-identity-card .dest-common-section,
#dest-common-fields.d-none ~ .dest-common-card {
	display: none;
}

#dest-common-fields.d-none ~ .dest-identity-card {
	margin-bottom: 0;
}
</style>

<tiles:useAttribute id="actionFormName" name="action.form.name"
	classname="java.lang.String" />
<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<c:set var="_copyMode" value="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}"/>
<div class="row g-3 ${_copyMode ? 'mb-0' : 'mb-3'}">
	<div class="col-lg-7">
		<div id="dest-common-fields"<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}"> class="d-none"</c:if>></div>
		<div class="card border-0 shadow-sm mb-3 dest-identity-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-geo-alt text-primary"></i>
				<span class="fw-semibold">Identity</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destIdentityInfo" aria-expanded="false" aria-controls="destIdentityInfo" title="About Identity settings"><i class="bi bi-info-circle"></i></button>
			</div>
			<div class="collapse" id="destIdentityInfo">
				<div class="ecpds-help-panel">
					Identify this Destination and record its type, comment, country and owner.
					The <strong>Transfer Group</strong> determines where queued files are stored when no dissemination Host is active.
					When creating a Destination, you can also copy an existing configuration or export it to another Master.
				</div>
			</div>
			<div class="card-body pb-2">
				<c:if test="${isInsert == 'true'}">
					<div class="d-flex gap-2 mb-3" role="group">
						<button type="button" id="btn-create"
							class="btn btn-sm dest-mode-btn <c:choose><c:when test="${not empty destinationActionForm.fromDestinationOptions}">btn-outline-secondary</c:when><c:otherwise>btn-primary</c:otherwise></c:choose>"
							onclick="selectDestMode('create',this)">
							<i class="bi bi-plus-circle me-1"></i>Create from Scratch
						</button>
						<c:if test="${not empty destinationActionForm.fromDestinationOptions}">
							<button type="button" id="btn-copy"
								class="btn btn-sm btn-primary dest-mode-btn"
								onclick="selectDestMode('copy',this)">
								<i class="bi bi-copy me-1"></i>Copy from Existing
							</button>
						</c:if>
						<c:if test="${not empty destinationActionForm.masterOptions}">
							<button type="button" id="btn-export"
								class="btn btn-sm btn-outline-secondary dest-mode-btn"
								onclick="selectDestMode('export',this)">
								<i class="bi bi-box-arrow-up-right me-1"></i>Export to Master
							</button>
						</c:if>
					</div>
					<input type="hidden" id="actionRequested" name="actionRequested"
						value="<c:choose><c:when test="${not empty destinationActionForm.fromDestinationOptions}">copy</c:when><c:otherwise>create</c:otherwise></c:choose>">
				</c:if>

				<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}">
				<div id="panel-copy">
					<div class="row g-2 mb-2">
						<div class="col-12"><small class="text-muted">Select a source destination and provide a name for the new copy.</small></div>
					</div>
					<script>document.addEventListener('DOMContentLoaded',function(){
					var s=document.getElementById('fromDestination');if(s){s.required=true;if(s.options.length>0&&!s.value)s.options[0].selected=true;}
					if(new URLSearchParams(window.location.search).get('fromDestination')){var t=document.getElementById('toDestination');if(t)t.focus();}
				});</script>
					<div class="row g-2 mb-2">
						<div class="col-12">
							<label for="fromDestination" class="form-label mb-1">Source Destination</label>
							<c:set var="destinations" value="${destinationActionForm.fromDestinationOptions}" />
							<div class="input-group input-group-sm mb-1">
								<span class="input-group-text"><i class="bi bi-search"></i></span>
								<input type="text" class="form-control form-control-sm"
									placeholder="Filter destinations..."
									oninput="filterSelect(this, 'fromDestination')"
									autocomplete="off">
							</div>
							<div class="form-text mt-0 mb-1"><i class="bi bi-hand-index me-1"></i>Type to filter the list, then <strong>click</strong> a destination below to select it.</div>
							<html:select property="fromDestination" styleId="fromDestination" styleClass="form-select form-select-sm dest-listbox"
								size="20" style="width:100%">
								<html:options collection="destinations" property="name" labelProperty="name" />
							</html:select>
						</div>
						<div class="col-sm-6">
							<label for="toDestination" class="form-label mb-1">New Name</label>
							<div class="d-flex align-items-center gap-2">
								<input id="toDestination" name="toDestination" type="text"
									class="form-control form-control-sm"
									maxlength="32"
									pattern="[a-zA-Z0-9]+([-_][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. my-destination). Maximum 32 characters."
									oninput="validatePatternInput(this, 'toDestination-feedback'); var n=document.getElementById('name');if(n)n.value=this.value;"
									<c:if test="${not empty destinationActionForm.fromDestinationOptions}">required</c:if>>
								<span id="toDestination-feedback"></span>
							</div>
						</div>
						<div class="col-sm-6">
							<label for="label" class="form-label mb-1">Comment</label>
							<html:text property="label" styleId="label" styleClass="form-control form-control-sm" />
						</div>
						<div class="col-sm-6">
							<label class="form-label mb-1">Clone Shared Hosts <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If ticked then every Host of the existing Destination which is shared with another Destination will be cloned/renamed and allocated to the new Destination (the new Destination will therefore not use shared Hosts)" tabindex="0"></i></label>
							<div class="form-check mt-1"><html:checkbox property="copySharedHost" styleClass="form-check-input" /></div>
						</div>
					</div>
				</div>

				<div id="panel-export" class="d-none">
					<div class="row g-2 mb-2">
						<div class="col-12"><small class="text-muted">Select a source destination and a target master to export to.</small></div>
					</div>
					<div class="row g-2 mb-2">
						<div class="col-12">
							<label for="sourceDestination" class="form-label mb-1">Source Destination</label>
							<div class="input-group input-group-sm mb-1">
								<span class="input-group-text"><i class="bi bi-search"></i></span>
								<input type="text" class="form-control form-control-sm"
									placeholder="Filter destinations..."
									oninput="filterSelect(this, 'sourceDestination')"
									autocomplete="off">
							</div>
							<div class="form-text mt-0 mb-1"><i class="bi bi-hand-index me-1"></i>Type to filter the list, then <strong>click</strong> a destination below to select it.</div>
							<html:select property="sourceDestination" styleId="sourceDestination" styleClass="form-select form-select-sm dest-listbox"
								size="20" style="width:100%">
								<html:options collection="destinations" property="name" labelProperty="name" />
							</html:select>
						</div>
						<div class="col-sm-6">
							<label for="exportToName" class="form-label mb-1">Alternative Name <span class="text-muted fw-normal">(optional)</span></label>
							<div class="d-flex align-items-center gap-2">
								<input id="exportToName" name="exportToName" type="text"
									class="form-control form-control-sm"
									maxlength="32"
									pattern="[a-zA-Z0-9]+([-_][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. my-destination). Maximum 32 characters."
									oninput="validatePatternInput(this, 'exportToName-feedback')">
								<span id="exportToName-feedback"></span>
							</div>
							<div class="form-text mt-1">Leave blank to keep the source name.</div>
						</div>
						<div class="col-sm-6">
							<label for="master" class="form-label mb-1">Master</label>
							<c:set var="masters" value="${destinationActionForm.masterOptions}" />
							<html:select property="master" styleId="master" styleClass="form-select form-select-sm">
								<html:options collection="masters" property="name" labelProperty="value" />
							</html:select>
						</div>
						<div class="col-sm-6">
							<label class="form-label mb-1">Clone Shared Hosts <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If ticked then every Host associated with the source Destination will be cloned/renamed on the target data store, otherwise a search will be done against the Host nick-name and if it already exists on the target data store then it will be used instead" tabindex="0"></i></label>
							<div class="form-check mt-1"><html:checkbox property="copySourceSharedHost" styleClass="form-check-input" /></div>
						</div>
					</div>
				</div>
				</c:if>

				<c:if test="${isInsert == 'true'}">
				<div id="panel-create" <c:if test="${not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
					<div class="row g-2 mb-2">
						<div class="col-12"><small class="text-muted">Provide a name below to create a brand-new destination from scratch.</small></div>
					</div>
				</div>
				<div id="row-name-create" <c:if test="${not empty destinationActionForm.fromDestinationOptions}">class="d-none"</c:if>>
					<div class="row g-2 mb-2">
						<div class="col-sm-6">
							<label for="name" class="form-label mb-1">Name</label>
							<div class="d-flex align-items-center gap-2">
								<input id="name" name="name" type="text"
									class="form-control form-control-sm"
									maxlength="32"
									pattern="[a-zA-Z0-9]+([-_][a-zA-Z0-9]+)*"
									title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. my-destination). Maximum 32 characters."
									data-char-filter="a-zA-Z0-9_-"
										oninput="validatePatternInput(this, 'name-feedback'); _checkDestNameExists(this.value)"
									<c:if test="${empty destinationActionForm.fromDestinationOptions}">required</c:if>>
								<span id="name-feedback"></span>
							</div>
								<div id="name-exists-msg" style="display:none" class="small mt-1"></div>
							</div>
						</div>
					</div>
				</c:if>

				<c:if test="${isInsert == 'true'}">
				<script>
				var _checkDestNameTimer = null;
				function _checkDestNameExists(value) {
				  clearTimeout(_checkDestNameTimer);
				  var $msg = $('#name-exists-msg');
				  var $submit = $('button[type="submit"]').first();
				  $msg.hide();
				  $submit.prop('disabled', false);
				  if (!value || value.length < 1) return;
				  _checkDestNameTimer = setTimeout(function() {
				    $.getJSON('/do/transfer/destination/' + encodeURIComponent(value) + '?json=checkId', function(data) {
				      if (data.exists) {
				        $msg.html('<i class="bi bi-x-circle-fill text-danger me-1"></i><span class="text-danger">Destination <strong>' + $('<span>').text(value).html() + '</strong> already exists.</span>').show();
				        $submit.prop('disabled', true);
				      } else {
				        $msg.html('<i class="bi bi-check-circle-fill text-success me-1"></i><span class="text-success">Available.</span>').show();
				        $submit.prop('disabled', false);
				      }
				    });
				  }, 400);
				}
				</script>
				</c:if>

				<div class="dest-common-section">
					<div class="row g-2">
						<c:if test="${isInsert != 'true'}">
							<div class="col-sm-6">
								<label class="form-label mb-1">Name</label>
								<div class="form-control form-control-sm bg-body-secondary">${requestScope[actionFormName].name}</div>
								<html:hidden property="name" />
							</div>
						</c:if>
						<div class="col-sm-6">
							<label for="destType" class="form-label mb-1">Type</label>
							<div class="d-flex align-items-center gap-2">
								<c:set var="types" value="${destinationActionForm.typeOptions}" />
								<html:select property="type" styleId="destType" styleClass="form-select form-select-sm">
									<html:options collection="types" property="name" labelProperty="value" />
								</html:select>
							</div>
						</div>
						<div class="col-sm-6">
							<label for="comment" class="form-label mb-1">Comment</label>
							<html:text property="comment" styleId="comment" styleClass="form-control form-control-sm" />
						</div>
						<div class="col-sm-6">
							<label for="transferGroup" class="form-label mb-1">Transfer Group <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If no Dissemination Host is active then this field specify in which Transfer Group the queued files should be stored" tabindex="0"></i></label>
							<bean:define id="groups" name="destinationActionForm" property="transferGroupOptions" type="java.util.Collection	" />
							<html:select property="transferGroup" styleId="transferGroup" styleClass="form-select form-select-sm">
								<html:options collection="groups" property="name" labelProperty="name" />
							</html:select>
						</div>
						<div class="col-sm-6">
							<label for="countryIso" class="form-label mb-1">Country</label>
							<c:set var="countries" value="${destinationActionForm.countryOptions}" />
							<div class="d-flex align-items-center gap-2"><html:select property="countryIso" styleId="countryIso" styleClass="form-select form-select-sm flex-grow-1" style="min-width:0">
								<html:options collection="countries" property="iso" labelProperty="name" />
							</html:select></div>
						</div>
						<div class="col-sm-6">
							<label for="ecUserName" class="form-label mb-1">Owner <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Only for the record" tabindex="0"></i></label>
							<c:set var="ecUsers" value="${destinationActionForm.ecUserOptions}" />
							<html:select property="ecUserName" styleId="ecUserName" styleClass="form-select form-select-sm" style="width:100%;max-width:100%">
								<html:options collection="ecUsers" property="name" labelProperty="comment" />
							</html:select>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="card border-0 shadow-sm mb-3 dest-common-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-send text-primary"></i>
				<span class="fw-semibold">Delivery</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destDeliveryInfo" aria-expanded="false" aria-controls="destDeliveryInfo" title="About Delivery settings"><i class="bi bi-info-circle"></i></button>
				<button type="button" class="btn btn-sm btn-outline-info ms-auto" data-bs-toggle="offcanvas" data-bs-target="#destLimitsGuide" aria-controls="destLimitsGuide" title="Open Limits &amp; Failover Guide" aria-label="Open Limits &amp; Failover Guide"><i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Limits &amp; Failover Guide</span></button>
			</div>
			<div class="collapse" id="destDeliveryInfo">
				<div class="ecpds-help-panel">
					Control delivery behaviour: Host failover, handling of existing remote targets, deletion from the local spool and data compression.
					<strong>Host For Sources</strong> provides a source for on-the-fly recovery when a file is missing from a Data Mover;
					this is distinct from scheduled acquisition through the Acquisition Hosts card.
				</div>
			</div>
			<div class="card-body pb-2">
				<div class="row g-2">
					<div class="col-sm-6">
						<label for="onHostFailure" class="form-label mb-1">On Host Failure <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="In case of error on a data transmission then try the next host in the list and stick to it if it works or restart with the first host in the list?" tabindex="0"></i></label>
						<c:set var="onHosts" value="${destinationActionForm.onHostFailureOptions}" />
						<html:select property="onHostFailure" styleId="onHostFailure" styleClass="form-select form-select-sm">
							<html:options collection="onHosts" property="value" labelProperty="name" />
						</html:select>
					</div>
					<div class="col-sm-6">
						<label for="ifTargetExist" class="form-label mb-1">If Target Exists</label>
						<c:set var="ifTargets" value="${destinationActionForm.ifTargetExistOptions}" />
						<html:select property="ifTargetExist" styleId="ifTargetExist" styleClass="form-select form-select-sm">
							<html:options collection="ifTargets" property="value" labelProperty="name" />
						</html:select>
					</div>
					<div class="col-sm-6">
						<label for="keepInSpool" class="form-label mb-1">Delete From Spool</label>
						<c:set var="keepInSpools" value="${destinationActionForm.keepInSpoolOptions}" />
						<html:select property="keepInSpool" styleId="keepInSpool" styleClass="form-select form-select-sm">
							<html:options collection="keepInSpools" property="value" labelProperty="name" />
						</html:select>
					</div>
					<div class="col-sm-6">
						<label for="filterName" class="form-label mb-1">Data Compression <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If requested data files are compressed in the queue if there is enough time before transmission (otherwise files are compressed on the fly)" tabindex="0"></i></label>
						<bean:define id="filters" name="destinationActionForm" property="filterNameOptions" type="java.util.Collection	" />
						<div class="d-flex align-items-center gap-2">
							<html:select property="filterName" styleId="filterName" styleClass="form-select form-select-sm" style="width:auto">
								<html:options collection="filters" property="name" labelProperty="name" />
							</html:select>
						</div>
					</div>
					<div class="col-sm-6">
						<label for="hostForSourceName" class="form-label mb-1">Host For Sources <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If the data file is not found on the data mover then specify which host to use in order to retrieve the file from the source" tabindex="0"></i></label>
						<c:set var="sources" value="${destinationActionForm.hostForSourceOptions}" />
						<html:select property="hostForSourceName" styleId="hostForSourceName" styleClass="form-select form-select-sm">
							<html:options collection="sources" property="name" labelProperty="nickName" />
						</html:select>
					</div>
				</div>
			</div>
		</div>

		<div class="card border-0 shadow-sm mb-3 dest-common-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-sliders text-primary"></i>
				<span class="fw-semibold">Limits</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destLimitsInfo" aria-expanded="false" aria-controls="destLimitsInfo" title="About Limits settings"><i class="bi bi-info-circle"></i></button>
				<button type="button" class="btn btn-sm btn-outline-info ms-auto" data-bs-toggle="offcanvas" data-bs-target="#destLimitsGuide" aria-controls="destLimitsGuide" title="Open Limits &amp; Failover Guide" aria-label="Open Limits &amp; Failover Guide"><i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Limits &amp; Failover Guide</span></button>
			</div>
			<div class="collapse" id="destLimitsInfo">
				<div class="ecpds-help-panel">
					<p class="mb-2"><strong>Max Connections</strong> independently caps outgoing dissemination transfers and,
					when acquisition is enabled, Master-scheduled file retrievals across this Destination's respective Hosts.
					A value of 5 allows up to <strong>5 retrievals and 5 outgoing transfers</strong>, not 5 combined.
					Host limits and other scheduler limits still apply. Directory listings and Mover-side on-the-fly fallback retrievals are excluded.</p>
					<p class="mb-0">The remaining settings control failure handling and retry delays, queue capacity, maximum file size,
					returning from a backup Host and inactivity monitoring. Use each field's <strong>?</strong> for its specific effect:
					holding a Destination, delaying a transfer and marking a transfer failed are different actions.</p>
				</div>
			</div>
			<div class="card-body pb-2">
				<div class="row g-2">
					<div class="col-sm-6">
						<label class="form-label mb-1">Max Connections <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Caps concurrent outgoing dissemination transfers across this Destination's dissemination Hosts. If acquisition is enabled, the same value separately caps concurrent Master-scheduled file retrievals across its acquisition Hosts. These are independent pools: a value of 5 allows up to 5 retrievals and 5 outgoing transfers, not 5 combined. Host limits and other scheduler limits still apply. Directory listings and Mover-side on-the-fly fallback retrievals are not counted." tabindex="0"></i></label>
						<div id="maxConnectionsSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxConnections" id="maxConnections">
							<div id="maxConnectionsHandle" class="ui-slider-handle"></div>
						</div>
					</div>
					<div class="col-sm-6">
						<label class="form-label mb-1">Retry Count <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the Destination is hold after a consecutive number of unsuccessful transfers (a manual restart will be necessary)" tabindex="0"></i></label>
						<div id="retryCountSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="retryCount" id="retryCount">
							<div id="retryCountHandle" class="ui-slider-handle"></div>
						</div>
					</div>
					<div class="col-sm-6">
						<label for="retryFrequency" class="form-label mb-1">Retry Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Time to wait before to retry with the Primary Host if the transmission is failing on all the Backup Hosts" tabindex="0"></i></label>
						<input type="hidden" name="retryFrequency" id="retryFrequency" value='<c:out value="${requestScope[actionFormName].retryFrequency}"/>'>
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="retryFrequency">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
					</div>
					<div class="col-sm-6">
						<label class="form-label mb-1">Max Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is delayed after a consecutive number of unsuccessful attempts" tabindex="0"></i></label>
						<div id="maxStartSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxStart" id="maxStart">
							<div id="maxStartHandle" class="ui-slider-handle"></div>
						</div>
					</div>
					<div id="startFrequencyRow" class="col-sm-6" style="display:none">
						<label for="startFrequency" class="form-label mb-1">Start Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the delay mentioned in the previous parameter (Max Start)." tabindex="0"></i></label>
						<input type="hidden" name="startFrequency" id="startFrequency" value="<c:out value="${requestScope[actionFormName].startFrequency}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="startFrequency">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
					</div>
					<div class="col-sm-6">
						<label class="form-label mb-1">Max Requeue <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set the transfer is tagged as failed after a consecutive number of unsuccessful transmissions (a manual requeue will be necessary)" tabindex="0"></i></label>
						<div id="maxRequeueSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxRequeue" id="maxRequeue">
							<div id="maxRequeueHandle" class="ui-slider-handle"></div>
						</div>
					</div>
					<div class="col-sm-6">
						<label class="form-label mb-1">Max Pending <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum number of queued files which can exists at a single time in the Destination (new attempt of queueing files are rejected)" tabindex="0"></i></label>
						<div id="maxPendingSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxPending" id="maxPending">
							<div id="maxPendingHandle" class="ui-slider-handle"></div>
						</div>
					</div>
					<div class="col-sm-6">
						<label for="maxFileSize" class="form-label mb-1">Max File Size <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the maximum size for a file in the queue (attempt of queueing bigger files are rejected)" tabindex="0"></i></label>
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
					</div>
					<div class="col-sm-6">
						<label for="resetFrequency" class="form-label mb-1">Reset Frequency <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination is successfully using a backup host for more than this duration, it will restart." tabindex="0"></i></label>
						<input type="hidden" name="resetFrequency" id="resetFrequency" value="<c:out value="${requestScope[actionFormName].resetFrequency}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="resetFrequency">
							<input type="number" class="form-control form-control-sm dur-h" min="0" style="width:65px" placeholder="0">
							<span class="text-muted small">h</span>
							<input type="number" class="form-control form-control-sm dur-m" min="0" max="59" style="width:60px" placeholder="0">
							<span class="text-muted small">m</span>
							<span class="text-muted small ms-1 dur-display"></span>
						</div>
					</div>
					<div class="col-sm-6">
						<label for="maxInactivity" class="form-label mb-1">Max Inactivity <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set and the Destination has no dissemination activity for more than this duration, a problem will be shown on the monitoring." tabindex="0"></i></label>
						<input type="hidden" name="maxInactivity" id="maxInactivity" value="<c:out value="${requestScope[actionFormName].maxInactivity}"/>">
						<div class="dur-picker d-flex align-items-center gap-1 flex-wrap" data-target="maxInactivity">
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

		<div class="card border-0 shadow-sm mb-3 dest-common-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-folder text-primary"></i>
				<span class="fw-semibold">Directory</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destDirectoryInfo" aria-expanded="false" aria-controls="destDirectoryInfo" title="About Directory settings"><i class="bi bi-info-circle"></i></button>
			</div>
			<div class="collapse" id="destDirectoryInfo">
				<div class="ecpds-help-panel">
					Control the date-directory view presented to incoming FTP/SFTP users.
					Enable <strong>Group By Date</strong> and choose a Java <code>SimpleDateFormat</code> pattern using the preview.
					This does not configure remote acquisition directories or dissemination target paths; those belong to the Host configuration.
				</div>
			</div>
			<div class="card-body pb-2">
				<div class="row g-2">
					<div class="col-sm-6">
						<label class="form-label mb-1">Group By Date <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="If set then incoming ftp/sftp users will see the files grouped into date directories" tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="groupByDate" styleId="groupByDate" styleClass="form-check-input" onclick="document.getElementById('dateFormatRow').style.display = this.checked ? '' : 'none'" /></div>
					</div>
					<div id="dateFormatRow" class="col-sm-6">
						<label for="dateFormatInput" class="form-label mb-1">Date Format <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Define the format of the date to display for each directory (Java SimpleDateFormat pattern)" tabindex="0"></i></label>
						<input type="text" id="dateFormatInput" name="dateFormat" list="dateFormatPresets" class="form-control form-control-sm" value='<c:out value="${requestScope[actionFormName].dateFormat}"/>' />
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
					</div>
				</div>
			</div>
		</div>

		<div class="card border-0 shadow-sm mb-3 dest-common-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-envelope text-primary"></i>
				<span class="fw-semibold">Notifications</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destNotificationsInfo" aria-expanded="false" aria-controls="destNotificationsInfo" title="About Notifications settings"><i class="bi bi-info-circle"></i></button>
			</div>
			<div class="collapse" id="destNotificationsInfo">
				<div class="ecpds-help-panel">
					Set email recipients and select which events generate mail: configuration updates, transfer starts, successful completions or failures.
					Separate multiple addresses with semicolons. Enabling start/end mail on a busy Destination can generate many messages.
				</div>
			</div>
			<div class="card-body pb-2">
				<div class="row g-2">
					<div class="col-12">
						<label for="userMailInput" class="form-label mb-1">Mail Address <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="One or more email addresses used when sending notifications. Separate multiple addresses with ';' (e.g. 'a@example.com;b@example.com')." tabindex="0"></i></label>
						<div class="d-flex align-items-center gap-2">
							<input type="text" name="userMail" id="userMailInput" class="form-control form-control-sm" value='<c:out value="${requestScope[actionFormName].userMail}"/>' title="Enter one or more email addresses separated by ';'" oninput="validateMailInput(this); toggleMailRows()" />
							<span id="userMailFeedback"></span>
						</div>
					</div>
					<div id="mailOnUpdateRow" class="col-sm-6">
						<label class="form-label mb-1">Mail on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a change is made to the Destination or its related Hosts." tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="mailOnUpdate" styleClass="form-check-input" /></div>
					</div>
					<div id="mailOnStartRow" class="col-sm-6">
						<label class="form-label mb-1">Mail on Start <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer starts for this Destination." tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="mailOnStart" styleClass="form-check-input" /></div>
					</div>
					<div id="mailOnEndRow" class="col-sm-6">
						<label class="form-label mb-1">Mail on End <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has completed successfully for this Destination." tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="mailOnEnd" styleClass="form-check-input" /></div>
					</div>
					<div id="mailOnErrorRow" class="col-sm-6">
						<label class="form-label mb-1">Mail on Error <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, an email is sent when a data transfer has failed for this Destination." tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="mailOnError" styleClass="form-check-input" /></div>
					</div>
				</div>
			</div>
		</div>

		<div class="card border-0 shadow-sm mb-3 dest-common-card">
			<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
				<i class="bi bi-toggles text-primary"></i>
				<span class="fw-semibold">Flags</span>
				<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destFlagsInfo" aria-expanded="false" aria-controls="destFlagsInfo" title="About Flags settings"><i class="bi bi-info-circle"></i></button>
			</div>
			<div class="collapse" id="destFlagsInfo">
				<div class="ecpds-help-panel">
					<strong>Enabled</strong> controls scheduler participation; <strong>Show In Monitor</strong> only controls monitoring visibility.
					<strong>Acquisition</strong> enables discovery and retrieval and requires at least one Acquisition Host.
					<strong>Restart on Update</strong> restarts the Destination after Host configuration changes.
					<strong>Backup</strong> requests backup storage where supported by the file's Transfer Group.
				</div>
			</div>
			<div class="card-body pb-2">
				<div class="row g-2">
					<div class="col-sm-6 col-lg-4">
						<label class="form-label mb-1">Restart on Update <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Automatically restart the Destination if a change is detected on one of the host configuration" tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="stopIfDirty" styleClass="form-check-input" /></div>
					</div>
					<div class="col-sm-6 col-lg-4">
						<label class="form-label mb-1">Acquisition <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the Acquisition Scheduler to use this Destination for Data Discovery and Retrieval (at least one Acquisition host must be defined)" tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="acquisition" styleClass="form-check-input" /></div>
					</div>
					<div class="col-sm-6 col-lg-4">
						<label class="form-label mb-1">Enabled <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is considered by the transfer scheduler; otherwise, no data transfers will be scheduled, even if there are pending requests in the queue. Similarly, any acquisition host will be disregarded." tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="active" styleClass="form-check-input" /></div>
					</div>
					<div class="col-sm-6 col-lg-4">
						<label class="form-label mb-1">Show In Monitor <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Destination is monitored in the monitoring display." tabindex="0"></i></label>
						<div class="d-flex align-items-center gap-1 mt-1">
							<html:checkbox property="showInMonitors" styleId="chk_showInMonitors" styleClass="form-check-input" style="margin-top:0" onchange="(function(c){document.getElementById('icon_showInMonitors').style.display=c.checked?'none':'';})(this)" />
							<i id="icon_showInMonitors" class="bi bi-eye-slash text-muted" title="Not shown in Monitor Display" style="font-size:1em;"></i>
						</div>
						<script>(function(){var c=document.getElementById('chk_showInMonitors');if(c)document.getElementById('icon_showInMonitors').style.display=c.checked?'none':'';})()</script>
					</div>
					<div class="col-sm-6 col-lg-4">
						<label class="form-label mb-1">Backup <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Request the storage of the data files on the backup system (if available for the Transfer Group where the files are stored)" tabindex="0"></i></label>
						<div class="form-check mt-1"><html:checkbox property="backup" styleClass="form-check-input" /></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="col-lg-5" id="dest-assoc-col">
<c:if test="${isInsert != 'true'}">
<div class="row g-3">

  <%-- Dissemination Hosts --%>
  <div class="col-12">
    <div class="card assoc-card">
      <div class="card-header">
        <i class="bi bi-hdd-network text-secondary"></i>
        <strong>Dissemination Hosts</strong>
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destDissHostsInfo" aria-expanded="false" aria-controls="destDissHostsInfo" title="About Dissemination Hosts"><i class="bi bi-info-circle"></i></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#dissChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.disseminationHostsAndPriorities}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllHosts/Dissemination','Remove ALL dissemination hosts from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="collapse" id="destDissHostsInfo">
        <div class="ecpds-help-panel">
          Assign remote delivery endpoints. Lower priority numbers take precedence; failover and retries determine which Host receives new dispatches.
          Removing an association detaches the Host from this Destination rather than deleting the Host record.
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destAcqHostsInfo" aria-expanded="false" aria-controls="destAcqHostsInfo" title="About Acquisition Hosts"><i class="bi bi-info-circle"></i></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#acqChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.acquisitionHostsAndPriorities}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllHosts/Acquisition','Remove ALL acquisition hosts from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destAcqHostsInfo">
          <div class="ecpds-help-panel">
            Assign Hosts used for scheduled remote discovery and file retrieval, and enable <strong>Acquisition</strong> in Flags.
            Eligible acquisition Hosts are processed independently, not as a priority-based failover chain.
            Configure listing paths, selection rules and timing on each Host; its Acquisition and Directory guides explain those settings.
          </div>
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destProxyHostsInfo" aria-expanded="false" aria-controls="destProxyHostsInfo" title="About Proxy Hosts"><i class="bi bi-info-circle"></i></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#proxyChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.proxyHostsAndPriorities}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllHosts/Proxy','Remove ALL proxy hosts from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destProxyHostsInfo">
          <div class="ecpds-help-panel">
            Associate Proxy Hosts used to replicate queued files to remote storage.
            Replication to a Proxy Host and onward dissemination are separate operations; a Proxy Host is not a dissemination failover endpoint.
          </div>
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destWebUsersInfo" aria-expanded="false" aria-controls="destWebUsersInfo" title="About Authorized Web Users"><i class="bi bi-info-circle"></i></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#userChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.associatedEcUsers}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllEcUsers/all','Remove ALL EC users from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destWebUsersInfo">
          <div class="ecpds-help-panel">
            Associate web users authorized for this Destination. Their roles and application permissions still determine which operations they may perform.
            This association is separate from the informational Owner field and from Data Portal user access.
          </div>
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destPoliciesInfo" aria-expanded="false" aria-controls="destPoliciesInfo" title="About Authorized Data Policies"><i class="bi bi-info-circle"></i></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#policyChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.associatedIncomingPolicies}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllPolicies/all','Remove ALL authorized policies from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destPoliciesInfo">
          <div class="ecpds-help-panel">
            Associate the incoming data policies authorized for this Destination.
            Policy rules are configured separately; adding a policy here creates an association, not a new policy or a web-user permission.
          </div>
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destAliasesInfo" aria-expanded="false" aria-controls="destAliasesInfo" title="About Destination Aliases"><i class="bi bi-info-circle"></i></button>
        <button type="button" class="btn btn-sm btn-outline-info" data-bs-toggle="offcanvas" data-bs-target="#destAliasesGuide" aria-controls="destAliasesGuide" title="Open Alias Routing Guide" aria-label="Open Alias Routing Guide"><i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Alias Routing Guide</span></button>
        <div class="btn-group btn-group-sm ms-auto">
          <button type="button" class="btn btn-outline-primary"
                  data-bs-toggle="collapse" data-bs-target="#aliasChooser">
            <i class="bi bi-plus-lg"></i> Add
          </button>
          <c:if test="${not empty destinationActionForm.aliases}">
          <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                  data-bs-toggle="dropdown" aria-expanded="false">
            <span class="visually-hidden">Toggle dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a class="dropdown-item text-danger" href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAllAliases/all','Remove ALL destination aliases from destination <c:out value="${destinationActionForm.id}"/>?')">
                <i class="bi bi-trash me-1"></i>Remove All
              </a>
            </li>
          </ul>
          </c:if>
        </div>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destAliasesInfo">
          <div class="ecpds-help-panel">
            Link Destinations so queued data can also be routed to the associated alias Destinations.
            Aliases can form chains and may be conditional; review the Destination alias diagram before changing a complex routing setup.
            Removing a link does not delete the linked Destination.
          </div>
        </div>
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
        <button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destMetadataInfo" aria-expanded="false" aria-controls="destMetadataInfo" title="About Metadata Files"><i class="bi bi-info-circle"></i></button>
      </div>
      <div class="card-body p-2">
        <div class="collapse" id="destMetadataInfo">
          <div class="ecpds-help-panel">
            Review metadata files associated with this Destination. This card lists existing entries and lets you remove them;
            it is not the queued data-transfer list or an upload editor.
          </div>
        </div>
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
</c:if>
	</div>
</div>
<div id="dest-editor-tbody"<c:if test="${isInsert == 'true' and not empty destinationActionForm.fromDestinationOptions}"> class="d-none"</c:if>>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-code-square text-primary"></i>
			<span class="fw-semibold">Options</span>
			<button type="button" class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" data-bs-toggle="collapse" data-bs-target="#destOptionsInfo" aria-expanded="false" aria-controls="destOptionsInfo" title="About Options settings"><i class="bi bi-info-circle"></i></button>
		</div>
		<div class="collapse" id="destOptionsInfo">
			<div class="ecpds-help-panel">
				<strong>Properties</strong> contains advanced Destination options; use the reference for supported keys and defaults.
				<strong>JavaScript</strong> contains custom Destination processing logic. Use the editor's Test action before saving changes.
				Host-specific transfer-module and acquisition-listing options belong in the Host configuration instead.
				<button type="button" class="btn btn-link btn-sm p-0 ms-1" onclick="openDestHelp();"><i class="bi bi-book me-1"></i>Properties Reference</button>
			</div>
		</div>
		<div class="card-body p-2">
			<%-- Quota summary badges --%>
			<c:set var="uploadQuota" value="${requestScope[actionFormName].incomingUploadQuota}"/>
			<c:set var="downloadQuota" value="${requestScope[actionFormName].incomingDownloadQuota}"/>
			<c:if test="${not empty uploadQuota || not empty downloadQuota}">
			<div class="d-flex flex-wrap gap-2 mb-2 px-1">
			  <c:if test="${not empty uploadQuota}">
			    <span class="badge rounded-pill bg-warning text-dark" title="Upload quota limit">
			      <i class="bi bi-cloud-upload me-1"></i>Upload limit: <c:out value="${uploadQuota}"/>
			    </span>
			  </c:if>
			  <c:if test="${not empty downloadQuota}">
			    <span class="badge rounded-pill bg-info text-dark" title="Download quota limit">
			      <i class="bi bi-cloud-download me-1"></i>Download limit: <c:out value="${downloadQuota}"/>
			    </span>
			  </c:if>
			</div>
			</c:if>
			<div class="accordion" id="optionsAccordion">
					<div class="accordion-item">
						<h2 class="accordion-header" id="acc-properties-heading" style="position:relative;">
							<button class="accordion-button collapsed" id="destAccPropertiesBtn" type="button" data-bs-toggle="collapse"
								data-bs-target="#acc-properties" aria-expanded="false"
								aria-controls="acc-properties">Properties</button>
							<span role="button" tabindex="0" class="acc-help-btn" id="destPropsHelpBtn"
								onclick="openDestHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openDestHelp();" title="Open properties reference">
								<i class="bi bi-question-circle"></i>
							</span>
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
							<button class="accordion-button collapsed" id="destAccJavascriptBtn" type="button" data-bs-toggle="collapse"
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

<div class="offcanvas offcanvas-end" tabindex="-1" id="destLimitsGuide"
     aria-labelledby="destLimitsGuideLabel" style="width:760px;max-width:95vw;">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="destLimitsGuideLabel"><i class="bi bi-book me-2 text-info"></i>Limits &amp; Failover Guide</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-3 small">
		<h6>Concurrency: two independent pools</h6>
		<p><strong>Destination Max Connections</strong> caps outgoing dissemination across all dissemination Hosts.
		When acquisition is enabled, the same value separately caps Master-scheduled retrievals across all acquisition Hosts.
		A value of 5 permits up to <strong>5 retrievals plus 5 outgoing transfers</strong>, subject to other limits.
		A negative value is unlimited; zero blocks new work in both pools. Lowering a limit does not interrupt work already running.</p>
		<p><strong>Host Max Connections</strong> applies per Destination by default.
		Set <code>ectrans.sharedConnectionLimit="true"</code> in the Host's Options &rarr; Properties to share its limit
		across Destinations using that Host record on the same Master. Separate Host records and independent Masters do not share this accounting.
		The Destination cap and scheduler pool limits still apply.</p>
		<div class="alert alert-warning py-2">
			Counts represent scheduled jobs, not physical sockets. Directory listings, probes and idle connections are excluded.
			One indexed retrieval counts as one job. Mover-side on-the-fly fallback retrievals bypass the acquisition limits and live retrieval count;
			an outgoing dissemination using that fallback still occupies its dissemination slots.
		</div>

		<h6>Dissemination Host selection and retries</h6>
		<p>Hosts are considered in priority order, with lower numbers first.
		The selected Host receives new dispatches; transfers already running on another Host may still finish there.
		Each Host has its own <strong>Retry Count</strong> and <strong>Retry Frequency</strong>.
		When its retry budget is exhausted, the scheduler moves to the next Host.</p>
		<p><strong>On Host Failure</strong> controls whether delivery stays with a working fallback Host or returns to the primary.
		<strong>Reset Frequency</strong> can trigger a restart after sustained use of a backup Host.
		These dissemination failover rules do not turn acquisition Hosts into a fallback chain: eligible acquisition Hosts are processed independently.</p>
		<div class="table-responsive">
			<table class="table table-sm table-bordered">
				<thead><tr><th scope="col">Destination setting</th><th scope="col">Effect</th></tr></thead>
				<tbody>
					<tr><th scope="row">Retry Count / Retry Frequency</th><td>Control repeated unsuccessful cycles through the dissemination Host list. Retry Frequency is the wait before another cycle; reaching the configured retry threshold holds the Destination and requires operator intervention. This is not the Host's own retry budget.</td></tr>
					<tr><th scope="row">Max Start / Start Frequency</th><td>For an individual transfer, reaching a positive Max Start threshold resets its start counter and advances its requeue counter. Unless Max Requeue is reached, it waits for Start Frequency before retrying.</td></tr>
					<tr><th scope="row">Max Requeue</th><td>A positive threshold limits those requeue cycles. Reaching it marks that transfer failed; a manual requeue is needed to retry it. It does not hold the whole Destination.</td></tr>
					<tr><th scope="row">Max Pending / Max File Size</th><td>Admission limits for new queue requests: excessive queue counts or file sizes are rejected rather than merely throttled.</td></tr>
					<tr><th scope="row">Max Inactivity</th><td>Raises a monitoring problem after the configured period without dissemination activity; it is not a connection timeout.</td></tr>
				</tbody>
			</table>
		</div>
		<h6>Diagnosing a queue that is not moving</h6>
		<p class="mb-0">Check Enabled and the Destination's running/held state, active Host associations, Destination and Host capacity,
		then the transfer's status, next scheduled time and history. A held Destination needs attention at Destination level;
		a delayed transfer may simply be waiting for its retry time; a failed transfer needs investigation before requeueing.
		Increasing concurrency does not clear a hold or repair a failed endpoint.</p>
	</div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="destAliasesGuide"
     aria-labelledby="destAliasesGuideLabel" style="width:760px;max-width:95vw;">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="destAliasesGuideLabel"><i class="bi bi-book me-2 text-info"></i>Alias Routing Guide</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-3 small">
		<h6>Direction matters</h6>
		<p>An alias link <strong>A &rarr; B</strong> routes data queued to A into B as well.
		The <strong>Destination Aliases</strong> card lists this Destination's outgoing alias links.
		It is not a list of upstream Destinations that send data here.
		On the Destination detail page, use <strong>Aliased From</strong>, <strong>Aliases To</strong> and the alias diagram to inspect both directions.</p>
		<h6>Fan-out and chains</h6>
		<p>For example, an acquisition Destination can retrieve products and alias them to separate customer Destinations:
		<code>acquisition &rarr; customer_a</code> and <code>acquisition &rarr; customer_b</code>.
		Each receiving Destination has its own delivery configuration, scheduling, limits and transfer status.
		Aliases create related transfer requests for shared data; they do not imply a separate physical copy of the file for each Destination.</p>
		<p>Links can form chains such as <strong>A &rarr; B &rarr; C</strong>.
		Review the complete diagram, not just the immediate links, before changing routing. Avoid cycles and unintended paths to recipients.</p>
		<h6>Conditional routing</h6>
		<p>Conditional aliases route only matching transfers. Inspect the applicable routing conditions and the annotations in the alias diagram;
		adding an association alone does not explain which subset will match. Validate with a representative product before relying on a new route.</p>
		<div class="alert alert-warning py-2">
			Alias routing is not an access-control grant or a Host failover mechanism.
			Authorized Web Users and Authorized Data Policies are separate associations.
			Check the receiving Destination's permissions, active state and delivery Hosts independently.
		</div>
		<h6>Changing associations safely</h6>
		<p class="mb-0">Check the link direction and downstream recipients before using Add or Remove.
		Removing an alias association does not delete the receiving Destination and should not be treated as cancellation of transfers already queued.
		After a routing change, inspect new transfer requests and their history at both source and receiving Destinations.</p>
	</div>
</div>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="destHelpOffcanvas"
     aria-labelledby="destHelpOffcanvasLabel" style="width:480px;max-width:95vw;">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="destHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="destHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="destHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

<script>
	var editorProperties = getEditorProperties(false, true, "properties", "crystal");
	editorProperties.setOptions({ minLines: 10, maxLines: 20 });
	
	// Get the completions from the bean!      		
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
	
	$(document).ready(function() {
		$('#destHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
		var navEl = document.querySelector('#destHelpContent .help-nav');
		if (navEl) document.getElementById('destHelpNav').appendChild(navEl);
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
			var $flag = $('<span class="fi" style="font-size:1.3em;vertical-align:middle;flex-shrink:0;"></span>');
			$sel.after($flag);
			function updateFlag() {
				var iso = ($sel.val() || '').toUpperCase();
				if (VALID_ISO.has(iso)) {
					$flag.attr('class', 'fi fi-' + iso.toLowerCase()).css('display', 'inline-block');
				} else if (iso === 'EX') {
					$flag.attr('class', 'bi bi-globe').css('display', 'inline-block');
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
		window.openDestHelp = function() {
			var el = document.getElementById('destHelpOffcanvas');
			if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
		};
		var _destOffcanvasEl = document.getElementById('destHelpOffcanvas');
		if (_destOffcanvasEl) {
			_destOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
				var btn = document.getElementById('destPropsHelpBtn');
				if (btn) btn.classList.add('acc-help-active');
			});
			_destOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
				_scrollDestHelpToCursor();
			});
			_destOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
				var btn = document.getElementById('destPropsHelpBtn');
				if (btn) btn.classList.remove('acc-help-active');
			});
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
		
	function _scrollDestHelpToCursor() {
		var row = editorProperties.selection.getCursor().row;
		var line = editorProperties.session.getLine(row) || '';
		line = line.trim();
		if (line && !line.startsWith('#') && !line.startsWith('//')) {
			var eqIdx = line.indexOf('=');
			var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
			if (paramName) scrollHelpToParam('destHelpContent', paramName);
		}
	}
    editorProperties.addEventListener("changeSelection", function (event) {
    	checkEachLine(editorProperties, 'destAccPropertiesBtn');
		var _oc = document.getElementById('destHelpOffcanvas');
		if (_oc && _oc.classList.contains('show')) _scrollDestHelpToCursor();
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
  		checkEachLine(editorProperties, 'destAccPropertiesBtn');
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
	checkEachLine(editorProperties, 'destAccPropertiesBtn');

	window.addEventListener('resize', function() {
		editorProperties.resize(true);
		editorJavascript.resize(true);
	});

	function updateTestJsBtn() {
		var hasError = editorJavascript.getSession().getAnnotations().some(function(a) { return a.type === 'error'; });
		$('#testJs').prop('disabled', hasError).toggleClass('disabled', hasError);
		$('#testJsOverlay').toggle(hasError);
		applyAnnotationMarkers(editorJavascript, 'destAccJavascriptBtn');
	}
	editorJavascript.getSession().on('changeAnnotation', updateTestJsBtn);

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
	            // Nothing needed -- simply don't navigate
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
						// Only flag unrecognised letters -- non-letter chars are always valid literals
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