<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String spError = (String) request.getAttribute("spError");
    final String spSuccess = (String) request.getAttribute("spSuccess");
    final String spTestResult = (String) request.getAttribute("spTestResult");
    final Map<?, ?> edit = (Map<?, ?>) request.getAttribute("spEdit");
    final String original = (String) request.getAttribute("spEditOriginal");
    final Set<String> editTypes = new HashSet<>();
    final Set<String> editGroups = new HashSet<>();
    if (request.getAttribute("spEditTypes") != null) {
        for (final Object o : (List<?>) request.getAttribute("spEditTypes")) {
            editTypes.add(String.valueOf(o));
        }
    }
    if (request.getAttribute("spEditGroups") != null) {
        for (final Object o : (List<?>) request.getAttribute("spEditGroups")) {
            editGroups.add(String.valueOf(o));
        }
    }
    final String editMode = edit == null ? "" : String.valueOf(edit.get("mode"));
%>

<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(25,135,84,0.07);border-left:4px solid #198754;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-hdd-stack-fill text-success flex-shrink-0 mt-1"></i>
        <span>Manage replica retention on Data Movers while preserving originals until normal expiry.
        Enforcement is off by default.</span>
        <a href="#" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" onclick="var el=document.getElementById('spGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;" title="Open Storage Policy Guide" aria-label="Open Storage Policy Guide"><i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Storage Policy Guide</span></a>
    </div>
</div>

<% if (spError != null) { %>
<div class="alert alert-danger d-flex align-items-start gap-2" role="alert">
    <i class="bi bi-exclamation-circle-fill mt-1"></i><span><c:out value="<%=spError%>" /></span>
</div>
<% } %>
<% if (spSuccess != null) { %>
<div id="storagePolicySuccess" class="alert alert-success fade show d-flex align-items-start gap-2" role="status">
    <i class="bi bi-check-circle-fill mt-1"></i><span><c:out value="<%=spSuccess%>" /></span>
</div>
<script>
    setTimeout(function() {
        var el = document.getElementById('storagePolicySuccess');
        if (el) bootstrap.Alert.getOrCreateInstance(el).close();
    }, 5000);
</script>
<% } %>

<div class="card border-0 shadow-sm mb-4">
    <div class="card-header fw-semibold"><i class="bi bi-activity me-2"></i>Runtime and storage savings</div>
    <div class="card-body">
        <p class="small mb-2"><c:out value="${spRuntimeActivity}"/></p>
        <c:if test="${not empty spRuntimeError}"><div class="alert alert-warning mb-2" role="alert">
            Runtime status unavailable: <c:out value="${spRuntimeError}"/></div></c:if>
        <p class="small text-muted mb-0">Activation: <code>[StoragePolicy] enabled=yes</code> on the Master.
            This immediately honours existing <code>ENFORCE</code> policies. Missing schema, busy files and unverified
            originals block removal. Candidate estimates come from the last complete scan; shared files appear under each applicable policy.</p>
    </div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead><tr><th>Policy</th><th>Eligible files</th><th>Reclaimable bytes</th><th>Reclaimed bytes</th>
                <th>Replicas dropped</th><th>Last applied (UTC)</th><th>Last complete scan (UTC)</th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${spReport}">
                <tr><td><c:out value="${r.name}"/></td><td><c:out value="${r.candidates}"/></td>
                    <td><c:out value="${r.reclaimableBytes}"/></td><td><c:out value="${r.reclaimedBytes}"/></td>
                    <td><c:out value="${r.replicasDropped}"/></td><td><c:out value="${r.lastAppliedText}"/></td>
                    <td><c:out value="${r.lastScanText}"/></td></tr>
            </c:forEach>
            <c:if test="${empty spReport}"><tr><td colspan="7" class="text-muted py-3">
                No runtime report available. Enable the feature and configure an active policy to collect assessments.
            </td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="card border-0 shadow-sm mb-4">
    <div class="card-header fw-semibold"><i class="bi bi-pin-angle me-2"></i>File status and operator hold</div>
    <div class="card-body">
        <form method="get" action="/do/admin/storagepolicies" class="d-flex align-items-end gap-2 mb-3">
            <div><label for="spFileId" class="form-label">Data File ID</label>
                <input id="spFileId" name="fileId" type="number" min="1" required class="form-control"
                       value="<c:out value="${spFileId}"/>"></div>
            <button class="btn btn-outline-primary" type="submit">Show status</button>
        </form>
        <jsp:include page="/WEB-INF/jsp/pds/datafile/datafile/lifecycle.jsp"/>
        <c:if test="${not empty spFileId and storageLifecycle.enabled eq 'true'}">
        <form method="post" action="/do/admin/storagepolicies" class="row g-2 align-items-end">
            <input type="hidden" name="csrf" value="<c:out value="${spCsrf}"/>">
            <input type="hidden" name="fileId" value="<c:out value="${spFileId}"/>">
            <div class="col-md-4"><label for="spPinUntil" class="form-label">Hold until (UTC, blank means expiry)</label>
                <input type="datetime-local" id="spPinUntil" name="pinUntil" class="form-control"></div>
            <div class="col-md-5"><label for="spPinReason" class="form-label">Reason (optional)</label>
                <input type="text" id="spPinReason" name="pinReason" maxlength="512" class="form-control"
                       value="<c:out value="${storageLifecycle.SLC_PIN_REASON}"/>"></div>
            <div class="col-md-3 d-flex gap-2">
                <button class="btn btn-primary" name="op" value="pin" type="submit">Save hold</button>
                <button class="btn btn-outline-secondary" name="op" value="unpin" type="submit">Remove hold</button>
            </div>
        </form>
        </c:if>
    </div>
</div>

<div class="card border-0 shadow-sm mb-4">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-hdd-stack-fill text-success"></i>
        <span class="fw-semibold">Storage Lifecycle Policies</span>
        <button class="btn btn-link btn-sm text-muted p-0" type="button"
                data-bs-toggle="collapse" data-bs-target="#spPoliciesInfo"
                aria-expanded="false" title="About this table">
            <i class="bi bi-info-circle"></i>
        </button>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" id="spSearch" class="form-control" placeholder="Search&hellip;"
                       autocomplete="off" style="min-width:120px">
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="spPageLen" class="form-select form-select-sm" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                </select>
            </div>
            <div class="dropdown">
                <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="spColModeBtn"
                        data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                    <i class="bi bi-layout-three-columns me-1"></i>Auto
                </button>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="spColModeBtn">
                    <li><a class="dropdown-item" href="#" data-sp-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                    <li><a class="dropdown-item" href="#" data-sp-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                    <li><a class="dropdown-item" href="#" data-sp-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Criteria, Updated</small></a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="#" data-sp-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                    <li id="spCustomColChkPanel" style="display:none;">
                        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="spchk-0">Order <small>(required)</small></label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="spchk-1">Name <small>(required)</small></label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-2" data-col="2" checked><label class="form-check-label" for="spchk-2">Mode</label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-3" data-col="3" checked><label class="form-check-label" for="spchk-3">Criteria</label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-4" data-col="4" checked><label class="form-check-label" for="spchk-4">Requeue window</label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-5" data-col="5" checked><label class="form-check-label" for="spchk-5">Replicas</label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-6" data-col="6" checked><label class="form-check-label" for="spchk-6">Updated</label></div>
                            <div class="form-check mb-0"><input class="form-check-input sp-col-chk" type="checkbox" id="spchk-7" data-col="7" checked disabled><label class="form-check-label text-muted" for="spchk-7">Actions <small>(required)</small></label></div>
                        </div>
                    </li>
                </ul>
            </div>
            <a href="/do/admin/storagepolicies?new=1" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
        </div>
    </div>
    <div class="collapse" id="spPoliciesInfo">
        <div class="ecpds-help-panel">
            <strong class="d-block mb-1">Storage Lifecycle Policies</strong>
            <p class="mb-1">Each policy sets how long the data files of the matching transfers stay on the Data Movers.</p>
            <ul class="mb-1 ps-3">
                <li><strong>Order</strong> &mdash; policies are evaluated top to bottom and the first active match wins; use the arrows to change it.</li>
                <li><strong>Mode</strong> &mdash; <code>DRY_RUN</code> only reports, <code>ENFORCE</code> applies, <code>DISABLED</code> is ignored.</li>
                <li><strong>Requeue window</strong> &mdash; minimum time the original is kept so that it can be re-queued; never beyond the transfer lifetime.</li>
                <li><strong>Replicas</strong> &mdash; whether the copies on the other Data Movers are dropped.</li>
                <li><strong>default</strong> &mdash; the built-in policy used when nothing matches, shown as <em>EXPIRY</em>: files are kept until the transfer expiry.</li>
            </ul>
            <p class="mb-0 text-muted">Only redundant replicas are removed. A runnable, failed or stopped transfer
                or an operator hold keeps every existing copy. The original must be verified before each removal.</p>
        </div>
    </div>
    <div class="table-responsive">
        <table id="spTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
            <thead>
                <tr>
                    <th scope="col" style="width:8rem;">Order</th>
                    <th scope="col">Name</th>
                    <th scope="col">Mode</th>
                    <th scope="col">Criteria</th>
                    <th scope="col">Requeue window</th>
                    <th scope="col">Replicas</th>
                    <th scope="col">Updated</th>
                    <th scope="col" class="no-sort" style="width:8rem;">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${spList}" varStatus="st">
                    <tr class="${p.active eq '1' ? '' : 'text-muted'}">
                        <td data-order="<c:out value="${p.order}" />">
                            <form method="post" action="/do/admin/storagepolicies" class="d-inline-flex gap-1 align-items-center">
                                <input type="hidden" name="csrf" value="<c:out value="${spCsrf}"/>">
                                <input type="hidden" name="name" value="<c:out value="${p.name}" />">
                                <span class="me-1"><c:out value="${p.order}" /></span>
                                <button type="submit" name="op" value="up" class="btn btn-sm btn-outline-secondary"
                                        title="Move up" <c:if test="${st.first}">disabled</c:if>><i class="bi bi-arrow-up"></i></button>
                                <button type="submit" name="op" value="down" class="btn btn-sm btn-outline-secondary"
                                        title="Move down" <c:if test="${st.last}">disabled</c:if>><i class="bi bi-arrow-down"></i></button>
                            </form>
                        </td>
                        <td>
                            <span class="fw-semibold"><c:out value="${p.name}" /></span>
                            <c:if test="${p.active ne '1'}"><span class="badge text-bg-secondary ms-1">inactive</span></c:if>
                            <c:if test="${not empty p.comment}"><div class="small text-muted"><c:out value="${p.comment}" /></div></c:if>
                        </td>
                        <td>
                            <span class="badge ${p.mode eq 'ENFORCE' ? 'text-bg-danger' : (p.mode eq 'DRY_RUN' ? 'text-bg-warning' : 'text-bg-secondary')}"><c:out value="${p.mode}" /></span>
                        </td>
                        <td class="small">
                            <c:if test="${not empty p.destinations}"><div title="<c:out value="${p.destinations}" />">Destinations: <c:out value="${p.destinationsCount}" /><span class="d-none"> <c:out value="${p.destinations}" /></span></div></c:if>
                            <c:if test="${not empty p.typesText}"><div title="<c:out value="${p.typesText}" />">Types: <c:out value="${p.typesCount}" /><span class="d-none"> <c:out value="${p.typesText}" /></span></div></c:if>
                            <c:if test="${not empty p.groups}"><div title="<c:out value="${p.groups}" />">Transfer groups: <c:out value="${p.groupsCount}" /><span class="d-none"> <c:out value="${p.groups}" /></span></div></c:if>
                            <c:if test="${not empty p.pattern}"><div class="text-truncate" style="max-width:260px" title="<c:out value="${p.pattern}" />">File pattern: <code><c:out value="${p.pattern}" /></code></div></c:if>
                            <c:if test="${empty p.destinations and empty p.typesText and empty p.groups and empty p.pattern}"><span class="text-muted">All transfers</span></c:if>
                        </td>
                        <td>
                            <c:out value="${p.windowText}" />
                            <c:if test="${p.restart eq '1'}"><div class="small text-muted">restarts on requeue</div></c:if>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${p.drop eq '1'}">Drop after <c:out value="${p.dropAfterText}" /></c:when>
                                <c:otherwise><span class="text-muted">Keep all</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="small"><c:out value="${p.updatedAt}" /><div class="text-muted"><c:out value="${p.updatedBy}" /></div></td>
                        <td>
                            <div class="d-flex align-items-center gap-1">
                            <a href="/do/admin/storagepolicies?edit=<c:out value="${p.name}" />" class="btn btn-sm btn-outline-primary" title="Edit this policy"><i class="bi bi-pencil"></i></a>
                            <form method="post" action="/do/admin/storagepolicies" class="d-flex m-0" data-sp-delete>
                                <input type="hidden" name="csrf" value="<c:out value="${spCsrf}"/>">
                                <input type="hidden" name="op" value="delete">
                                <input type="hidden" name="name" value="<c:out value="${p.name}" />">
                                <button type="submit" name="op" value="delete" class="btn btn-sm btn-outline-danger" title="Delete this policy"><i class="bi bi-trash"></i></button>
                            </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
            <tfoot>
                <tr style="--bs-table-bg:var(--bs-tertiary-bg);--bs-table-hover-bg:var(--bs-tertiary-bg)">
                    <td>-</td>
                    <td><span class="fw-semibold">default</span> <span class="badge text-bg-secondary ms-1">built-in</span>
                        <div class="small text-muted">Applies when no other policy matches: files are kept until the transfer expiry.</div></td>
                    <td><span class="badge text-bg-secondary" title="Files are kept until the transfer expiry; nothing is removed early">EXPIRY</span></td>
                    <td class="small text-muted">All transfers (always last)</td>
                    <td>7 days</td>
                    <td><span class="text-muted">Keep all</span></td>
                    <td></td>
                    <td></td>
                </tr>
            </tfoot>
        </table>
    </div>
</div>

<% if (edit != null) { %>
<form method="post" action="/do/admin/storagepolicies" class="card shadow-sm mb-4">
    <input type="hidden" name="csrf" value="<c:out value="${spCsrf}"/>">
    <input type="hidden" name="op" value="save">
    <input type="hidden" name="original" value="<c:out value="<%=original%>" />">
    <div class="card-header fw-semibold">
        <i class="bi bi-pencil-square me-2"></i><%= original.isEmpty() ? "New policy" : "Edit policy" %>
    </div>
    <div class="card-body">
        <div class="row g-3">
            <div class="col-md-4">
                <label class="form-label" for="spName">Name</label>
                <input type="text" id="spName" name="name" class="form-control" maxlength="32" required
                       value="<c:out value="${spEdit.name}" />" <%= original.isEmpty() ? "" : "readonly" %>>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="spMode">Mode</label>
                <select id="spMode" name="mode" class="form-select">
                    <option value="DRY_RUN" <%= "DRY_RUN".equals(editMode) ? "selected" : "" %>>DRY_RUN (evaluate and report only)</option>
                    <option value="ENFORCE" <%= "ENFORCE".equals(editMode) ? "selected" : "" %>>ENFORCE</option>
                    <option value="DISABLED" <%= "DISABLED".equals(editMode) ? "selected" : "" %>>DISABLED</option>
                </select>
            </div>
            <div class="col-md-4 d-flex align-items-end">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="spActive" name="active" <%= "1".equals(String.valueOf(edit.get("active"))) ? "checked" : "" %>>
                    <label class="form-check-label" for="spActive">Active</label>
                </div>
            </div>
            <div class="col-12">
                <label class="form-label" for="spComment">Comment</label>
                <input type="text" id="spComment" name="comment" class="form-control"
                       value="<c:out value="${spEdit.comment}" />">
            </div>

            <div class="col-12"><hr class="my-1"><div class="fw-semibold">Criteria <span class="text-muted fw-normal small">(all the criteria entered must match; leave empty for any)</span></div></div>
            <div class="col-md-6">
                <label class="form-label" for="spDestinations">Destinations</label>
                <input type="text" id="spDestinations" name="destinations" class="form-control" maxlength="1024"
                       placeholder="e.g. ECMWF-*, MY-DEST (names or * and ? wildcards)"
                       value="<c:out value="${spEdit.destinations}" />">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="spPattern">File name pattern</label>
                <input type="text" id="spPattern" name="pattern" class="form-control" maxlength="512"
                       placeholder="e.g. *.grib2 (or regex:.*\.grib2)"
                       value="<c:out value="${spEdit.pattern}" />">
                <div class="form-text"><code>*</code> matches any sequence of characters and <code>?</code> exactly one
                (e.g. <code>*.grib2</code>, <code>ens_??_*</code>). Start with <code>regex:</code> to use a regular expression.</div>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="spTypes">Destination types</label>
                <select id="spTypes" name="types" class="form-select" multiple size="5">
                    <c:forEach var="t" items="${spTypes}">
                        <c:set var="tkey" value="${t.key}" />
                        <% final String tk = String.valueOf(pageContext.getAttribute("tkey")); %>
                        <option value="<c:out value="${t.key}" />" <%= editTypes.contains(tk) ? "selected" : "" %>><c:out value="${t.value}" /></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="spGroups">Transfer groups</label>
                <select id="spGroups" name="groups" class="form-select" multiple size="5">
                    <c:forEach var="g" items="${spGroups}">
                        <c:set var="gname" value="${g}" />
                        <% final String gn = String.valueOf(pageContext.getAttribute("gname")); %>
                        <option value="<c:out value="${g}" />" <%= editGroups.contains(gn) ? "selected" : "" %>><c:out value="${g}" /></option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-12"><hr class="my-1"><div class="fw-semibold">Retention</div></div>
            <div class="col-md-4">
                <label class="form-label" for="spWindow">Requeue window</label>
                <div class="input-group">
                    <input type="number" min="0" id="spWindow" name="window" class="form-control" value="<c:out value="${spWindowValue}" />">
                    <select name="windowUnit" class="form-select" style="max-width:7rem;">
                        <option value="days" ${spWindowUnit eq 'days' ? 'selected' : ''}>days</option>
                        <option value="hours" ${spWindowUnit eq 'hours' ? 'selected' : ''}>hours</option>
                    </select>
                </div>
                <div class="form-text">The original copy is always kept at least this long (never beyond the transfer lifetime).</div>
            </div>
            <div class="col-md-8 d-flex align-items-start pt-4">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="spRestart" name="restart" <%= "1".equals(String.valueOf(edit.get("restart"))) ? "checked" : "" %>>
                    <label class="form-check-label" for="spRestart">Restart the window when the transfer is re-queued</label>
                </div>
            </div>
            <div class="col-md-4 d-flex align-items-start pt-4">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="spDrop" name="drop" <%= "1".equals(String.valueOf(edit.get("drop"))) ? "checked" : "" %>>
                    <label class="form-check-label" for="spDrop">Drop the replicas (keep only the original copy)</label>
                </div>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="spDropAfter">Drop replicas after</label>
                <div class="input-group">
                    <input type="number" min="0" id="spDropAfter" name="dropAfter" class="form-control" value="<c:out value="${spDropAfterValue}" />">
                    <select name="dropAfterUnit" class="form-select" style="max-width:7rem;">
                        <option value="days" ${spDropAfterUnit eq 'days' ? 'selected' : ''}>days</option>
                        <option value="hours" ${spDropAfterUnit eq 'hours' ? 'selected' : ''}>hours</option>
                    </select>
                </div>
            </div>
            <div class="col-12"><div class="form-text">Both the requeue window and replica delay must elapse.
                Any live transfer using Keep all, EXPIRY or DISABLED vetoes removal. Any DRY_RUN match prevents enforcement.</div></div>
            <div class="col-md-4">
                <label class="form-label" for="spMinimum">Minimum replica retention from arrival (hours)</label>
                <input id="spMinimum" type="number" min="0" name="minRetention" class="form-control"
                       value="<c:out value="${spminRetentionHours}"/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="spGrace">Extra grace after eligibility (hours)</label>
                <input id="spGrace" type="number" min="0" name="grace" class="form-control"
                       value="<c:out value="${spgraceHours}"/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="spLazy">Lazy replication delay from arrival (hours)</label>
                <input id="spLazy" type="number" min="0" name="lazy" class="form-control"
                       value="<c:out value="${splazyHours}"/>">
                <div class="form-text">0 keeps normal eager replication. An unhealthy original bypasses the delay.
                    Redundancy is reduced during deferral; ENFORCE only.</div>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="spHigh">Disk-pressure high watermark (%)</label>
                <input id="spHigh" type="number" min="0" max="100" name="high" class="form-control"
                       value="<c:out value="${spEdit.high}"/>">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="spLow">Disk-pressure low watermark (%)</label>
                <input id="spLow" type="number" min="0" max="100" name="low" class="form-control"
                       value="<c:out value="${spEdit.low}"/>">
            </div>
            <div class="col-12"><div class="form-text">Set both watermarks to 0 to disable pressure handling.
                Pressure skips only extra grace; it never shortens the requeue window, replica delay or minimum retention.</div></div>
        </div>
    </div>
    <div class="card-footer d-flex gap-2">
        <button type="submit" class="btn btn-primary"><i class="bi bi-check2 me-1"></i>Save</button>
        <a href="/do/admin/storagepolicies" class="btn btn-outline-secondary">Cancel</a>
    </div>
</form>
<% } %>

<form method="post" action="/do/admin/storagepolicies" class="card shadow-sm mb-4">
    <input type="hidden" name="csrf" value="<c:out value="${spCsrf}"/>">
    <input type="hidden" name="op" value="test">
    <div class="card-header d-flex align-items-center gap-2 fw-semibold">
        <i class="bi bi-search"></i><span>Which policy applies?</span>
        <button class="btn btn-link btn-sm text-muted p-0" type="button"
                data-bs-toggle="collapse" data-bs-target="#spTestInfo"
                aria-expanded="false" title="About this tool">
            <i class="bi bi-info-circle"></i>
        </button>
    </div>
    <div class="collapse" id="spTestInfo">
        <div class="ecpds-help-panel">
            <strong class="d-block mb-1">Which policy applies?</strong>
            <p class="mb-1">Checks which policy would be selected for a destination, and optionally a file name.</p>
            <ul class="mb-1 ps-3">
                <li>A destination option <code>scheduler.storagePolicy</code> wins over the order.</li>
                <li>Otherwise the first active policy whose criteria all match, else the built-in <code>default</code>.</li>
                <li>Enter a real file name (no wildcards): it is checked against the policies' file name patterns, which accept <code>*</code> and <code>?</code>.</li>
                <li>The result shows the requeue window and warns if the destination lifetime is shorter.</li>
            </ul>
            <p class="mb-0 text-muted">Nothing is changed by this check.</p>
        </div>
    </div>
    <div class="card-body">
        <div class="row g-3 align-items-end">
            <div class="col-md-4">
                <label class="form-label" for="spTestDestination">Destination</label>
                <div style="position:relative">
                    <div class="input-group">
                        <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                        <input type="text" id="spTestDestination" name="testDestination" class="form-control" required
                               placeholder="Search destination..." autocomplete="off"
                               value="<c:out value="${spTestDestination}" />">
                    </div>
                    <ul id="spDestDropdown" role="listbox"
                        class="list-unstyled border rounded bg-body shadow-sm mb-0"
                        style="display:none;position:absolute;z-index:1050;min-width:360px;max-height:260px;overflow-y:auto;top:100%;left:0;margin-top:2px;padding:3px 0"></ul>
                </div>
            </div>
            <div class="col-md-5">
                <label class="form-label" for="spTestFile">File name (optional)</label>
                <input type="text" id="spTestFile" name="testFile" class="form-control"
                       value="<c:out value="${spTestFile}" />">
            </div>
            <div class="col-md-3">
                <button type="submit" class="btn btn-outline-primary w-100"><i class="bi bi-play-fill me-1"></i>Evaluate</button>
            </div>
        </div>
        <% if (spTestResult != null) { %>
        <div class="alert alert-info mt-3 mb-0"><c:out value="<%=spTestResult%>" /></div>
        <% } %>
        <div class="form-text mt-2">A destination can also select a policy explicitly with the option
        <code>scheduler.storagePolicy = "&lt;policy name&gt;"</code>, which always wins over the order.</div>
    </div>
</form>
<script>
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form[data-sp-delete]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            confirmationDialog({
                title: 'Delete storage policy',
                message: 'Delete policy "' + form.elements.name.value + '"? Referenced policies cannot be deleted.',
                confirmText: 'Delete policy',
                allowHtml: false,
                onConfirm: function () { form.submit(); }
            });
        });
    });
});
</script>

<ul id="spDestData" style="display:none">
    <c:forEach var="d" items="${spDestinationOptions}">
        <li data-name="<c:out value="${d.name}"/>" data-label="<c:out value="${d.value}"/>"></li>
    </c:forEach>
</ul>
<script>
(function() {
    var opts = Array.from(document.querySelectorAll('#spDestData li')).map(function(el) {
        return { n: el.dataset.name, l: el.dataset.label };
    });
    var input = document.getElementById('spTestDestination');
    var drop = document.getElementById('spDestDropdown');
    var filtered = [], active = -1;
    function esc(s) { return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }
    function render() {
        var q = input.value.trim().toLowerCase();
        filtered = q ? opts.filter(function(o) {
            return o.n.toLowerCase().indexOf(q) >= 0 || o.l.toLowerCase().indexOf(q) >= 0;
        }) : opts.slice();
        active = -1;
        drop.innerHTML = filtered.length ? filtered.map(function(o, i) {
            return '<li role="option" data-idx="' + i + '" style="padding:5px 10px;cursor:pointer;line-height:1.3">'
                + esc(o.n) + (o.l ? '<br><small class="text-muted" style="font-size:0.78rem">' + esc(o.l) + '</small>' : '') + '</li>';
        }).join('') : '<li style="padding:4px 10px;font-size:0.875rem;color:var(--bs-secondary-color)">No match</li>';
        drop.style.display = 'block';
    }
    function setActive(i) {
        var items = drop.querySelectorAll('li[data-idx]');
        items.forEach(function(el) { el.style.background = ''; });
        active = i >= 0 && i < items.length ? i : -1;
        if (active >= 0) { items[active].style.background = 'var(--bs-secondary-bg)'; items[active].scrollIntoView({ block: 'nearest' }); }
    }
    function pick(i) { if (filtered[i]) { input.value = filtered[i].n; drop.style.display = 'none'; } }
    input.addEventListener('input', render);
    input.addEventListener('focus', render);
    input.addEventListener('blur', function() { setTimeout(function() { drop.style.display = 'none'; }, 200); });
    input.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowDown') { e.preventDefault(); if (drop.style.display === 'none') render(); setActive(Math.min(active + 1, filtered.length - 1)); }
        else if (e.key === 'ArrowUp') { e.preventDefault(); setActive(Math.max(active - 1, 0)); }
        else if (e.key === 'Enter' && active >= 0) { e.preventDefault(); pick(active); }
        else if (e.key === 'Escape') { drop.style.display = 'none'; }
    });
    drop.addEventListener('mousedown', function(e) {
        var li = e.target.closest('li[data-idx]');
        if (li) pick(parseInt(li.dataset.idx, 10));
    });
    drop.addEventListener('mouseover', function(e) {
        var li = e.target.closest('li[data-idx]');
        if (li) setActive(parseInt(li.dataset.idx, 10));
    });
})();
</script>

<jsp:include page="/WEB-INF/jsp/pds/admin/storagepolicies/guide.jsp"/>

<script>
$(function () {
    var table = $('#spTable').DataTable({
        paging: true,
        searching: true,
        info: true,
        autoWidth: false,
        order: [[0, 'asc']],
        columnDefs: [{ orderable: false, targets: [7] }],
        pageLength: (function () {
            try { var v = parseInt(localStorage.getItem('spPageLen'), 10); return [10,25,50,100].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; }
        })(),
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            infoEmpty: 'No policy defined',
            emptyTable: 'No policy defined: the built-in default applies.',
            zeroRecords: 'No matching policies found.'
        }
    });

    $('#spSearch').on('input', function () { table.search(this.value).draw(); });
    $('#spPageLen').val(table.page.len()).on('change', function () {
        var len = +this.value;
        try { localStorage.setItem('spPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });

    /* Columns: 0=Order, 1=Name, 2=Mode, 3=Criteria, 4=Window, 5=Replicas, 6=Updated, 7=Actions */
    var colMode = (function () { try { return localStorage.getItem('spColMode') || 'auto'; } catch(e) { return 'auto'; } })();
    var customCols = (function () { try { var s = localStorage.getItem('spCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5,6,7]; } catch(e) { return [0,1,2,3,4,5,6,7]; } })();
    function showCols(hide) { table.columns().every(function (i) { table.column(i).visible(hide.indexOf(i) === -1, false); }); table.columns.adjust(); }
    function applyCustom() { table.columns().every(function (i) { var req = i === 0 || i === 1 || i === 7; table.column(i).visible(req || customCols.indexOf(i) !== -1, false); }); table.columns.adjust(); }
    function applyAuto() {
        if (colMode !== 'auto') return;
        var w = window.innerWidth;
        if (w < 768) showCols([3,5,6]); else if (w < 992) showCols([6]); else showCols([]);
    }
    function applyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#spColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label)
            .toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        document.getElementById('spCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
        $('#spColModeBtn').closest('.dropdown').find('[data-sp-mode]').each(function () {
            $(this).find('i.bi-check').remove();
            if ($(this).data('sp-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        if (mode === 'auto') applyAuto();
        else if (mode === 'all') showCols([]);
        else if (mode === 'compact') showCols([3,6]);
        else if (mode === 'custom') {
            applyCustom();
            document.querySelectorAll('.sp-col-chk').forEach(function (c) { c.checked = c.disabled || customCols.indexOf(+c.dataset.col) !== -1; });
        }
    }
    document.querySelectorAll('.sp-col-chk:not([disabled])').forEach(function (chk) {
        chk.addEventListener('change', function () {
            var col = +this.dataset.col, idx = customCols.indexOf(col);
            if (this.checked && idx === -1) customCols.push(col); else if (!this.checked && idx !== -1) customCols.splice(idx, 1);
            try { localStorage.setItem('spCustomCols', JSON.stringify(customCols)); } catch(e) {}
            if (colMode === 'custom') applyCustom();
        });
    });
    $('#spColModeBtn').closest('.dropdown').on('click', '[data-sp-mode]', function (e) {
        e.preventDefault();
        colMode = $(this).data('sp-mode');
        try { localStorage.setItem('spColMode', colMode); } catch(e2) {}
        applyMode(colMode);
    });
    $(window).on('resize', function () { if (colMode === 'auto') applyAuto(); });
    applyMode(colMode);
});
</script>
