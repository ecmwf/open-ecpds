<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(13,202,240,0.07);border-left:4px solid #0dcaf0;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-cloud-lightning-rain text-info flex-shrink-0 mt-1"></i>
        <span>Follow recent delivery conditions between Data Movers and dissemination Hosts.
        Collection and optional probes are off by default.</span>
        <a href="#" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" onclick="var el=document.getElementById('nwGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;" title="Open Configuration Guide"><i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Configuration </span>Guide</a>
    </div>
</div>
<c:if test="${not empty nwError}">
    <div class="alert alert-danger" id="nwRequestError" role="alert"><c:out value="${nwError}"/></div>
</c:if>
<c:if test="${not empty nwControlError}">
    <div class="alert alert-danger" role="alert"><c:out value="${nwControlError}"/></div>
</c:if>
<c:if test="${not empty nwSuccess}">
    <div class="alert alert-success alert-dismissible" role="status"><c:out value="${nwSuccess}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
</c:if>

<c:if test="${nwLoaded}">
<script type="application/json" id="nwChoices">${nwChoices}</script>

<%-- Shared automation form: its fields live in the Automatic selection and Probes tabs (form attribute). --%>
<form method="post" action="/do/admin/networkweather" id="nwAutomationForm">
    <input type="hidden" name="op" value="automation">
    <input type="hidden" name="csrf" value="<c:out value="${nwCsrf}"/>">
    <input type="hidden" name="revision" value="<c:out value="${nwRevision}"/>">
    <input type="hidden" name="automationRevision" value="<c:out value="${nwAutomationRevision}"/>">
</form>

<style>
#nwTabs .nav-link.active {
    color: var(--bs-white);
    background-color: var(--bs-primary);
    border-color: var(--bs-primary);
    font-weight: 600;
}
.nw-admin-content .card-body > :last-child,
.nw-admin-content dl.row,
.nw-admin-content dl.row > dt,
.nw-admin-content dl.row > dd {
    margin-bottom: 0;
}
.nw-admin-content dl.row {
    row-gap: 0.375rem;
}
.nw-admin-content .card,
.nw-admin-content .card-body,
.nw-admin-content .table-responsive {
    min-width: 0;
}
.nw-admin-content .table-responsive {
    max-width: 100%;
}
.nw-admin-content table.dataTable {
    margin-top: 0 !important;
    margin-bottom: 0 !important;
}
</style>
<ul class="nav nav-tabs nw-admin-tabs mb-3" id="nwTabs" role="tablist">
    <li class="nav-item" role="presentation"><button class="nav-link active" id="nw-tab-overview" data-bs-toggle="tab" data-bs-target="#nw-overview" type="button" role="tab" aria-controls="nw-overview" aria-selected="true">
        <i class="bi bi-speedometer2 me-1"></i>Overview
        <c:choose><c:when test="${nwEnabled}"><span class="badge text-bg-success ms-1">Active</span></c:when>
            <c:when test="${nwRequestedEnabled}"><span class="badge text-bg-warning ms-1">Suspended</span></c:when>
            <c:otherwise><span class="badge text-bg-secondary ms-1">Disabled</span></c:otherwise></c:choose></button></li>
    <li class="nav-item" role="presentation"><button class="nav-link" id="nw-tab-manual" data-bs-toggle="tab" data-bs-target="#nw-manual" type="button" role="tab" aria-controls="nw-manual" aria-selected="false">
        <i class="bi bi-signpost-split me-1"></i>Manual scope</button></li>
    <li class="nav-item" role="presentation"><button class="nav-link" id="nw-tab-auto" data-bs-toggle="tab" data-bs-target="#nw-auto" type="button" role="tab" aria-controls="nw-auto" aria-selected="false">
        <i class="bi bi-magic me-1"></i>Automatic selection
        <c:if test="${nwAutomatic}"><span class="badge text-bg-info ms-1">On</span></c:if></button></li>
    <li class="nav-item" role="presentation"><button class="nav-link" id="nw-tab-probes" data-bs-toggle="tab" data-bs-target="#nw-probes" type="button" role="tab" aria-controls="nw-probes" aria-selected="false">
        <i class="bi bi-broadcast me-1"></i>Probes
        <c:if test="${nwProbesEnabled}"><span class="badge text-bg-info ms-1">On</span></c:if></button></li>
    <li class="nav-item" role="presentation"><button class="nav-link" id="nw-tab-sources" data-bs-toggle="tab" data-bs-target="#nw-sources" type="button" role="tab" aria-controls="nw-sources" aria-selected="false">
        <i class="bi bi-hdd-network me-1"></i>Sources</button></li>
    <li class="nav-item" role="presentation"><button class="nav-link" id="nw-tab-measurements" data-bs-toggle="tab" data-bs-target="#nw-measurements" type="button" role="tab" aria-controls="nw-measurements" aria-selected="false">
        <i class="bi bi-activity me-1"></i>Measurements</button></li>
</ul>

<div class="tab-content nw-admin-content">

<%-- Overview --%>
<div class="tab-pane fade show active" id="nw-overview" role="tabpanel" aria-labelledby="nw-tab-overview">
    <form method="post" action="/do/admin/networkweather" class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-power text-info"></i><span class="fw-semibold">Collection</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-collection" aria-expanded="false" aria-controls="nwInfo-collection" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-collection"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Switches the collection of measurements on or off for the selected paths.
 <ul class="mb-0"><li><strong>Active</strong>: measurements are collected.</li>
 <li><strong>Suspended</strong>: activation is requested but temporarily not possible (for example storage not ready); it resumes automatically.</li>
 <li><strong>Disabled</strong>: nothing is collected.</li></ul>
 Deactivation stops new measurements immediately on the Master and within one minute on the Data Movers.
 Transfers are never affected and existing measurements remain until they expire.</div></div>
        <div class="card-body">
            <input type="hidden" name="csrf" value="<c:out value="${nwCsrf}"/>">
            <input type="hidden" name="revision" value="<c:out value="${nwRevision}"/>">
            <dl class="row mb-2">
                <dt class="col-sm-3">State</dt>
                <dd class="col-sm-9"><c:choose><c:when test="${nwEnabled}">Active</c:when>
                    <c:when test="${nwRequestedEnabled}">Suspended</c:when><c:otherwise>Disabled</c:otherwise></c:choose></dd>
                <dt class="col-sm-3">Selection mode</dt>
                <dd class="col-sm-9"><c:choose><c:when test="${nwAutomatic}">Automatic</c:when><c:otherwise>Manual</c:otherwise></c:choose></dd>
                <dt class="col-sm-3">Selected paths</dt><dd class="col-sm-9"><c:out value="${nwCount}"/> / <c:out value="${nwMaxSelectedPaths}"/></dd>
                <dt class="col-sm-3">Last saved</dt><dd class="col-sm-9"><c:out value="${nwUpdatedAt}"/> by <c:out value="${nwUpdatedBy}"/></dd>
            </dl>
            <c:if test="${not empty nwActivationBlocker}">
                <div class="alert alert-warning mb-2" id="nwActivationBlocker" role="status">
                    <strong>Activation blocked:</strong> <c:out value="${nwActivationBlocker}"/>
                </div>
            </c:if>
        </div>
        <div class="card-footer d-flex gap-2">
            <button type="submit" name="op" value="activate" class="btn btn-success"
                <c:if test="${not nwCanActivate}">disabled</c:if>>Activate collection</button>
            <button type="submit" name="op" value="deactivate" class="btn btn-outline-danger"
                <c:if test="${not nwCanSave}">disabled</c:if>>Deactivate collection</button>
            <a href="/do/admin/networkweather" class="btn btn-outline-secondary ms-auto">Reload</a>
        </div>
    </form>
    <div class="row g-3">
        <div class="col-lg-6"><div class="card shadow-sm">
            <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-check2-square text-info"></i><span class="fw-semibold">Capabilities</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-capabilities" aria-expanded="false" aria-controls="nwInfo-capabilities" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-capabilities"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Shows the current state of collection, database persistence, automatic selection and
 scheduled probes. Disabled means switched off. Socket persistence is unavailable while the background writer
 is starting or cannot currently save measurements. See the Storage card and Master log for details.</div></div>
            <div class="card-body">
                <c:if test="${empty nwCapabilities}">
                    <div class="d-flex align-items-start gap-2 text-muted">
                        <i class="bi bi-check2-square fs-4 flex-shrink-0" aria-hidden="true"></i>
                        <div>
                            <p class="fw-semibold mb-1">Capability status unavailable</p>
                            <p class="small mb-0">No capability details were returned by the Master. Reload to request the latest status.</p>
                        </div>
                    </div>
                </c:if>
                <c:if test="${not empty nwCapabilities}">
                <dl class="row mb-0">
                <c:forEach var="entry" items="${nwCapabilities}">
                    <dt class="col-sm-5"><c:out value="${entry.key}"/></dt>
                    <dd class="col-sm-7"><c:out value="${entry.value}"/></dd>
                </c:forEach>
                </dl>
                </c:if>
            </div>
        </div></div>
        <div class="col-lg-6"><div class="card shadow-sm">
            <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-database text-info"></i><span class="fw-semibold">Storage</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-storage" aria-expanded="false" aria-controls="nwInfo-storage" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-storage"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">State of the background worker saving measurements in the database: whether it is
 ready, how many measurements are waiting and how many were saved, dropped or expired. Dropped or expired
 measurements leave gaps in the history.</div></div>
            <div class="card-body">
                <c:if test="${not empty nwWriterError}">
                    <div class="alert alert-warning py-1" role="status"><c:out value="${nwWriterError}"/></div>
                </c:if>
                <c:if test="${empty nwWriter and empty nwWriterError}">
                    <div class="d-flex align-items-start gap-2 text-muted">
                        <i class="bi bi-database fs-4 flex-shrink-0" aria-hidden="true"></i>
                        <div>
                            <p class="fw-semibold mb-1">Storage status unavailable</p>
                            <p class="small mb-0">No persistence details were returned by the Master. Reload to request the latest status.</p>
                        </div>
                    </div>
                </c:if>
                <c:if test="${not empty nwWriter}">
                <dl class="row mb-0">
                    <c:forEach var="entry" items="${nwWriter}">
                        <dt class="col-sm-6"><c:out value="${entry.key}"/></dt>
                        <dd class="col-sm-6"><c:out value="${entry.value}"/></dd>
                    </c:forEach>
                </dl>
                </c:if>
            </div>
        </div></div>
        <div class="col-12"><div class="card shadow-sm">
            <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-speedometer2 text-info"></i><span class="fw-semibold">Collector counters</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-counters" aria-expanded="false" aria-controls="nwInfo-counters" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-counters"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Counters since the Master started: measurements currently kept in memory,
 and measurements refused (invalid, too old, outside the selected paths or over capacity), plus the number of
 accepted and refused batches received from the Data Movers.</div></div>
            <div class="card-body">
                <c:if test="${empty nwHealth}">
                    <div class="d-flex align-items-start gap-2 text-muted">
                        <i class="bi bi-speedometer2 fs-4 flex-shrink-0" aria-hidden="true"></i>
                        <div>
                            <p class="fw-semibold mb-1">Collector counters unavailable</p>
                            <p class="small mb-0">No collector counters were returned by the Master. Reload to request the latest counters.</p>
                        </div>
                    </div>
                </c:if>
                <c:if test="${not empty nwHealth}">
                <dl class="row mb-0">
                <c:forEach var="entry" items="${nwHealth}">
                    <dt class="col-sm-4 col-lg-3"><c:out value="${entry.key}"/></dt>
                    <dd class="col-sm-2 col-lg-3"><c:out value="${entry.value}"/></dd>
                </c:forEach>
                </dl>
                </c:if>
            </div>
        </div></div>
    </div>
</div>

<%-- Manual scope --%>
<div class="tab-pane fade" id="nw-manual" role="tabpanel" aria-labelledby="nw-tab-manual">
    <form method="post" action="/do/admin/networkweather" class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-signpost-split text-info"></i><span class="fw-semibold">Manual scope</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-manual" aria-expanded="false" aria-controls="nwInfo-manual" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-manual"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Fixed list of paths followed when <strong>automatic selection is off</strong>.
 A path is a Data Mover (source), a dissemination Host and its transfer method.
 <ul class="mb-0"><li>Select a source and a Host, then press <strong>Add</strong>; the target and method come from the Host.</li>
 <li>At most <c:out value="${nwMaxSelectedPaths}"/> paths across <c:out value="${nwMaxSources}"/> sources.</li>
 <li>Saving deactivates collection until it is activated again from the Overview tab.</li>
 <li>Use <strong>Refresh choices</strong> if a newly created Data Mover or Host is not listed.</li></ul></div></div>
        <div class="card-body">
            <input type="hidden" name="op" value="save">
            <input type="hidden" name="csrf" value="<c:out value="${nwCsrf}"/>">
            <input type="hidden" name="revision" value="<c:out value="${nwRevision}"/>">
            <c:if test="${nwAutomatic}">
                <div class="alert alert-info py-2" role="status">Automatic selection is on: the paths are chosen
                    automatically and the manual scope cannot be saved.</div>
            </c:if>
            <label for="nwPaths" class="form-label fw-semibold">Paths</label>
            <textarea id="nwPaths" name="paths" rows="8" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="paths" data-nw-max="${nwMaxSelectedPaths}"><c:out value="${nwPaths}"/></textarea>
            <noscript><p class="form-text">One path per line: <code>source | host ID | target | method</code>.</p></noscript>
        </div>
        <div class="card-footer d-flex gap-2">
            <button type="submit" class="btn btn-primary" <c:if test="${not nwCanSave or nwAutomatic}">disabled</c:if>>Save manual scope</button>
            <a href="/do/admin/networkweather?refreshChoices=true" class="btn btn-outline-secondary"
                title="Reload the list of Data Movers, transfer groups and Hosts">Refresh choices</a>
        </div>
    </form>
</div>

<%-- Automatic selection --%>
<div class="tab-pane fade" id="nw-auto" role="tabpanel" aria-labelledby="nw-tab-auto">
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-magic text-info"></i><span class="fw-semibold">Automatic selection</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-auto" aria-expanded="false" aria-controls="nwInfo-auto" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-auto"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Lets the Master choose up to <c:out value="${nwMaxSelectedPaths}"/> paths once a day among the candidates.
 <ul class="mb-0"><li><strong>Historical discovery</strong>: uses up to seven days of activity, volume and errors to rank already permitted candidates; it does not populate the candidate lists.</li>
 <li><strong>Source / transfer group</strong>: every active dissemination Host of the group becomes a candidate.</li>
 <li><strong>Approved candidates</strong>: individual paths that may be selected (up to <c:out value="${nwMaxCandidates}"/>).</li>
 <li><strong>Pinned</strong>: always selected. <strong>Excluded</strong>: never selected; exclusions win over pins.</li></ul>
 While automatic selection is on, the manual scope cannot be edited.</div></div>
        <div class="card-body">
            <div class="d-flex flex-wrap gap-4 mb-3">
                <div class="form-check form-switch"><input form="nwAutomationForm" class="form-check-input" type="checkbox" role="switch" id="nwAutomatic" name="automatic" <c:if test="${nwAutomatic}">checked</c:if>>
                    <label class="form-check-label" for="nwAutomatic">Automatic selection</label></div>
                <div class="form-check form-switch"><input form="nwAutomationForm" class="form-check-input" type="checkbox" role="switch" id="nwHistorical" name="historical" <c:if test="${nwHistorical}">checked</c:if>>
                    <label class="form-check-label" for="nwHistorical">Historical discovery</label></div>
            </div>
            <div class="mb-3">
                <button type="button" class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#nwHistoryModal">
                    <i class="bi bi-search me-1"></i>Find candidates from successful transfers</button>
                <p class="form-text mb-0">Choose a period and review matching Hosts before adding individual paths or source/group scopes.</p>
                <p id="nwHistoryAdded" class="small text-primary mt-2 mb-0" role="status" hidden>Candidates added to the form. Click Save to persist your changes.</p>
            </div>
            <label for="nwSourceGroups" class="form-label fw-semibold">Source / transfer group candidates</label>
            <textarea form="nwAutomationForm" id="nwSourceGroups" name="sourceGroups" rows="3" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="groups" data-nw-max="${nwMaxSources}"><c:out value="${nwSourceGroups}"/></textarea>
            <label for="nwApproved" class="form-label fw-semibold mt-3">Approved candidates</label>
            <textarea form="nwAutomationForm" id="nwApproved" name="approved" rows="5" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="paths" data-nw-max="${nwMaxCandidates}"><c:out value="${nwApproved}"/></textarea>
            <label for="nwPins" class="form-label fw-semibold mt-3">Pinned (always selected)</label>
            <textarea form="nwAutomationForm" id="nwPins" name="pins" rows="3" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="paths" data-nw-max="${nwMaxSelectedPaths}" data-nw-suggest="nwApproved"><c:out value="${nwPins}"/></textarea>
            <label for="nwExclusions" class="form-label fw-semibold mt-3">Excluded (never selected)</label>
            <textarea form="nwAutomationForm" id="nwExclusions" name="exclusions" rows="3" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="paths" data-nw-max="${nwMaxCandidates}" data-nw-suggest="nwApproved"><c:out value="${nwExclusions}"/></textarea>
            <noscript><p class="form-text">One entry per line: <code>source | host ID | target | method</code>;
                groups: <code>source | transfer group</code>.</p></noscript>
        </div>
        <div class="card-footer d-flex gap-2">
            <button form="nwAutomationForm" type="submit" class="btn btn-primary">Save</button>
            <a href="/do/admin/networkweather?refreshChoices=true" class="btn btn-outline-secondary"
                title="Reload the list of Data Movers, transfer groups and Hosts">Refresh choices</a>
        </div>
    </div>
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-clock-history text-info"></i><span class="fw-semibold">Status</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-autostatus" aria-expanded="false" aria-controls="nwInfo-autostatus" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-autostatus"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Result of the latest automatic selection: when it ran, when it runs next, how many
 candidates were considered and how many paths were added or removed. <strong>Request reassessment</strong>
 recomputes the selection without waiting for the next daily run.</div></div>
        <div class="card-body">
            <c:if test="${not empty nwAutomationError}"><div class="alert alert-danger py-1"><c:out value="${nwAutomationError}"/></div></c:if>
            <c:if test="${empty nwAutomationInfo and empty nwSelectionReasons and empty nwAutomationError}">
                <div class="d-flex align-items-start gap-2 text-muted mb-3">
                    <i class="bi bi-clock-history fs-4 flex-shrink-0" aria-hidden="true"></i>
                    <div>
                        <p class="fw-semibold mb-1">No assessment details available yet</p>
                        <p class="small mb-0">Save and enable Automatic selection before requesting reassessment.
                            Results will appear here when available.</p>
                    </div>
                </div>
            </c:if>
            <c:if test="${not empty nwAutomationInfo}">
            <dl class="row mb-2">
                <c:forEach var="entry" items="${nwAutomationInfo}">
                    <dt class="col-sm-3"><c:out value="${entry.key}"/></dt>
                    <dd class="col-sm-9"><c:out value="${entry.value}"/></dd>
                </c:forEach>
            </dl>
            </c:if>
            <c:if test="${not empty nwSelectionReasons}">
                <h3 class="h6">Latest changes</h3>
                <ul class="small mb-2"><c:forEach var="reason" items="${nwSelectionReasons}"><li><c:out value="${reason}"/></li></c:forEach></ul>
            </c:if>
            <form method="post" action="/do/admin/networkweather">
                <input type="hidden" name="op" value="reassess">
                <input type="hidden" name="csrf" value="<c:out value="${nwCsrf}"/>">
                <input type="hidden" name="revision" value="<c:out value="${nwRevision}"/>">
                <button class="btn btn-outline-primary" type="submit">Request reassessment</button>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="nwHistoryModal" tabindex="-1" aria-labelledby="nwHistoryLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable"><div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title" id="nwHistoryLabel">Find candidates from successful transfers</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
            <p class="small text-muted">Find active dissemination Hosts with at least one successful transfer in the chosen period.
                History identifies the Host, not the source for that transfer: choose the source to assign to these candidates.</p>
            <form id="nwHistorySearch" class="row g-2 align-items-end mb-3">
                <div id="nwHistorySource" class="col-md-4"></div>
                <div class="col-md-3">
                    <label for="nwHistoryProtocol" class="form-label small">Protocol</label>
                    <select id="nwHistoryProtocol" class="form-select form-select-sm"><option value="">All protocols</option></select>
                </div>
                <div class="col-md-3">
                    <label for="nwHistoryDays" class="form-label small">Last (days)</label>
                    <input id="nwHistoryDays" type="number" min="1" max="30" step="1" value="7" required class="form-control form-control-sm">
                </div>
                <div class="col-md-2"><button id="nwHistorySearchButton" type="submit" class="btn btn-primary btn-sm">Search history</button></div>
            </form>
            <div id="nwHistoryFeedback" class="small mb-2" role="status" aria-live="polite"></div>
            <div id="nwHistoryPreview" class="table-responsive mb-3"></div>
            <label for="nwHistoryMode" class="form-label small fw-semibold">Add selected matches as</label>
            <select id="nwHistoryMode" class="form-select form-select-sm">
                <option value="paths">Individual paths in Approved candidates</option>
                <option value="groups">Source / transfer group candidates</option>
            </select>
            <p class="form-text mb-0">Group scopes permit every active dissemination Host in each group, including Hosts without
                recent successful transfers. Only one transfer group per source is supported. Existing entries are kept;
                already-approved paths are omitted from the search results and duplicates are not added.
                Nothing is saved until you click Save.</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
            <button id="nwHistoryApply" type="button" class="btn btn-primary" disabled>Add selected to form</button>
        </div>
    </div></div>
</div>

<%-- Probes --%>
<div class="tab-pane fade" id="nw-probes" role="tabpanel" aria-labelledby="nw-tab-probes">
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-broadcast text-info"></i><span class="fw-semibold">Scheduled probes</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-probes" aria-expanded="false" aria-controls="nwInfo-probes" title="About this section"><i class="bi bi-info-circle"></i></button></div>
        <div class="collapse" id="nwInfo-probes"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">A probe is a light connectivity test run from the Data Mover to the Host,
 without credentials or login.
 <ul class="mb-0"><li><strong>TCP_CONNECT</strong>: opens a TCP connection to the port (every minute at most).</li>
 <li><strong>MTR_ICMP</strong> / <strong>MTR_TCP</strong>: route trace (every 5 minutes at most; requires <code>mtr</code> on the Data Mover).</li>
 <li>Use the port really used by the Host transfer method; a usual port is suggested.</li>
 <li>Probes run only while collection is active and the path is selected.</li></ul></div></div>
        <div class="card-body">
            <div class="form-check form-switch mb-3"><input form="nwAutomationForm" class="form-check-input" type="checkbox" role="switch" id="nwProbesEnabled" name="probesEnabled" <c:if test="${nwProbesEnabled}">checked</c:if>>
                <label class="form-check-label" for="nwProbesEnabled">Scheduled probes</label></div>
            <label for="nwProbePolicies" class="form-label fw-semibold">Probe policies</label>
            <textarea form="nwAutomationForm" id="nwProbePolicies" name="probePolicies" rows="4" maxlength="${nwMaxConfigurationBytes}" class="form-control font-monospace"
                data-nw-builder="probes" data-nw-max="${nwMaxProbePolicies}" data-nw-suggest="nwApproved"><c:out value="${nwProbePolicies}"/></textarea>
            <noscript><p class="form-text">One entry per line:
                <code>source | host ID | target | method | port | TCP_CONNECT, MTR_ICMP or MTR_TCP | seconds</code>.</p></noscript>
        </div>
        <div class="card-footer d-flex gap-2">
            <button form="nwAutomationForm" type="submit" class="btn btn-primary">Save</button>
        </div>
    </div>
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-list-check text-info"></i><span class="fw-semibold">Status and recent results</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-probestatus" aria-expanded="false" aria-controls="nwInfo-probestatus" title="About this section"><i class="bi bi-info-circle"></i></button><div class="ms-auto d-flex flex-wrap align-items-center gap-2"><div class="input-group input-group-sm" style="width:auto"><span class="input-group-text"><i class="bi bi-search"></i></span><input type="text" id="nwProbeTableSearch" class="form-control" placeholder="Search&hellip;" autocomplete="off" style="min-width:120px" aria-label="Search"></div><div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size"><span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span><select id="nwProbeTablePageLen" class="form-select form-select-sm" style="width:auto" aria-label="Page size"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select></div></div></div>
        <div class="collapse" id="nwInfo-probestatus"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Probes waiting for a result, expired or refused, and the
 latest results received. <strong>SOURCE_UNAVAILABLE</strong> means the Data Mover did not answer before the
 probe expired.</div></div>
        <div class="card-body border-bottom py-2">
            <c:if test="${empty nwProbeInfo}">
                <div class="d-flex align-items-start gap-2 text-muted">
                    <i class="bi bi-list-check fs-4 flex-shrink-0" aria-hidden="true"></i>
                    <div>
                        <p class="fw-semibold mb-1">Probe status unavailable</p>
                        <p class="small mb-0">Scheduled probe status and pending, expired or rejected job counts appear here.
                            No status details were returned by the Master. Reload to request the latest status.</p>
                    </div>
                </div>
            </c:if>
            <c:if test="${not empty nwProbeInfo}">
            <dl class="row mb-0">
                <c:forEach var="entry" items="${nwProbeInfo}">
                    <dt class="col-sm-3"><c:out value="${entry.key}"/></dt>
                    <dd class="col-sm-9"><c:out value="${entry.value}"/></dd>
                </c:forEach>
            </dl>
            </c:if>
        </div>
        <div class="table-responsive">
            <table id="nwProbeTable" data-nw-table="nwProbeTable" data-nw-order="6:desc" data-nw-empty="No recent results." class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
                <thead><tr><th scope="col">Source</th><th scope="col">Host</th><th scope="col">Mode</th>
                    <th scope="col">Outcome</th><th scope="col">Endpoint</th><th scope="col" class="text-end">Latency (ms)</th>
                    <th scope="col">Received</th></tr></thead>
                <tbody>
                <c:forEach var="row" items="${nwProbeRows}">
                    <c:set var="outcome" value="${row.outcome}"/>
                    <tr><td class="fw-semibold"><c:out value="${row.source}"/></td><td><c:out value="${row.host}"/></td>
                        <td><span class="badge text-bg-light border"><c:out value="${row.mode}"/></span></td>
                        <td><span class="badge ${outcome eq 'CONNECTED' or outcome eq 'RESPONSE' ? 'text-bg-success' : (outcome eq 'SOURCE_UNAVAILABLE' or outcome eq 'TOOL_UNAVAILABLE' or outcome eq 'PRIVILEGE_DENIED' ? 'text-bg-secondary' : (outcome eq 'NO_RESPONSE' or outcome eq 'DEADLINE' ? 'text-bg-warning' : 'text-bg-danger'))}"><c:out value="${outcome}"/></span></td>
                        <td class="small"><c:out value="${row.endpoint}"/></td><td class="text-end"><c:out value="${row.latency}"/></td>
                        <td class="small text-nowrap"><c:out value="${row.received}"/></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%-- Sources --%>
<div class="tab-pane fade" id="nw-sources" role="tabpanel" aria-labelledby="nw-tab-sources">
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-hdd-network text-info"></i><span class="fw-semibold">Source registration</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-sources" aria-expanded="false" aria-controls="nwInfo-sources" title="About this section"><i class="bi bi-info-circle"></i></button><div class="ms-auto d-flex flex-wrap align-items-center gap-2"><div class="input-group input-group-sm" style="width:auto"><span class="input-group-text"><i class="bi bi-search"></i></span><input type="text" id="nwSourceTableSearch" class="form-control" placeholder="Search&hellip;" autocomplete="off" style="min-width:120px" aria-label="Search"></div><div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size"><span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span><select id="nwSourceTablePageLen" class="form-select form-select-sm" style="width:auto" aria-label="Page size"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select></div></div></div>
        <div class="collapse" id="nwInfo-sources"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Each Data Mover used as a source must be verified before collection can be
 activated. The table shows the verification state, when it was last verified and the response time.
 Continental Data Movers are verified through their approved Proxy Host.</div></div>
        <div class="table-responsive">
            <table id="nwSourceTable" data-nw-table="nwSourceTable" data-nw-empty="No selected sources." class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
                <thead><tr><th scope="col">Source</th><th scope="col">State</th><th scope="col">Process epoch</th>
                    <th scope="col">Last verified</th><th scope="col" class="text-end">Callback RTT (ms)</th><th scope="col">Detail</th></tr></thead>
                <tbody><c:forEach var="row" items="${nwSources}">
                    <c:set var="state" value="${row.state}"/>
                    <tr><td class="fw-semibold"><c:out value="${row.source}"/></td>
                        <td><span class="badge ${state eq 'REGISTERED' ? 'text-bg-success' : (state eq 'FAILED' ? 'text-bg-danger' : (state eq 'EXPIRED' or state eq 'UNSUPPORTED' ? 'text-bg-warning' : 'text-bg-secondary'))}"><c:out value="${state}"/></span></td>
                        <td class="small font-monospace"><c:out value="${row.epoch}"/></td><td class="small text-nowrap"><c:out value="${row.verified}"/></td>
                        <td class="text-end"><c:out value="${row.rtt}"/></td><td class="small"><c:out value="${row.detail}"/></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%-- Measurements --%>
<div class="tab-pane fade" id="nw-measurements" role="tabpanel" aria-labelledby="nw-tab-measurements">
    <div class="card shadow-sm mb-3">
        <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)"><i class="bi bi-activity text-info"></i><span class="fw-semibold">Selected paths &mdash; last five minutes</span><button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#nwInfo-measurements" aria-expanded="false" aria-controls="nwInfo-measurements" title="About this section"><i class="bi bi-info-circle"></i></button><div class="ms-auto d-flex flex-wrap align-items-center gap-2"><div class="input-group input-group-sm" style="width:auto"><span class="input-group-text"><i class="bi bi-search"></i></span><input type="text" id="nwSummaryTableSearch" class="form-control" placeholder="Search&hellip;" autocomplete="off" style="min-width:120px" aria-label="Search"></div><div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size"><span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span><select id="nwSummaryTablePageLen" class="form-select form-select-sm" style="width:auto" aria-label="Page size"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select></div></div></div>
        <div class="collapse" id="nwInfo-measurements"><div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">Transfers observed on each selected path during the
 last five minutes: successes, failures, cancellations and unknown outcomes, and how many transfers reported
 network details. A path without observations has <strong>no coverage</strong>; it is not reported as healthy.</div></div>
        <div class="table-responsive">
            <table id="nwSummaryTable" data-nw-table="nwSummaryTable" data-nw-empty="No selected paths." class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
                <thead><tr>
                    <th scope="col">Source</th><th scope="col">Host</th><th scope="col">Target</th><th scope="col">Method</th>
                    <th scope="col" class="text-end">Success</th><th scope="col" class="text-end">Failure</th><th scope="col" class="text-end">Cancelled</th>
                    <th scope="col" class="text-end">Unknown</th><th scope="col" class="text-end">Sendings with sockets</th>
                </tr></thead>
                <tbody>
                <c:forEach var="row" items="${nwSummaries}">
                    <c:set var="observed" value="${row.success + row.failure + row.cancelled + row.unknown}"/>
                    <tr class="${observed eq 0 ? 'text-muted' : ''}"><td class="fw-semibold"><c:out value="${row.source}"/></td>
                        <td><c:out value="${row.host}"/><c:if test="${observed eq 0}"><span class="badge text-bg-secondary ms-1">no coverage</span></c:if></td>
                        <td class="small"><c:out value="${row.target}"/></td><td><span class="badge text-bg-light border"><c:out value="${row.protocol}"/></span></td>
                        <td class="text-end${row.success gt 0 ? ' text-success fw-semibold' : ''}"><c:out value="${row.success}"/></td>
                        <td class="text-end${row.failure gt 0 ? ' text-danger fw-semibold' : ''}"><c:out value="${row.failure}"/></td>
                        <td class="text-end"><c:out value="${row.cancelled}"/></td><td class="text-end"><c:out value="${row.unknown}"/></td>
                        <td class="text-end"><c:out value="${row.sockets}"/></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

</div>
</c:if>
<jsp:include page="/WEB-INF/jsp/pds/admin/networkweather/guide.jsp"/>
<script src="/js/network-weather-admin.js?v=20261019a"></script>
