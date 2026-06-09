<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>

<tiles:insert name="date.select" />

<%-- Status tabs + refresh --%>
<c:set var="currentRefresh" value="${empty param['refreshPeriod'] ? '0' : param['refreshPeriod']}"/>
<div class="d-flex flex-wrap align-items-start gap-2 mb-3">

    <%-- Submission: Arriving, Preset, Fetching --%>
    <div>
        <div class="text-muted mb-1" style="font-size:0.68rem; font-weight:600; letter-spacing:0.05em; text-transform:uppercase;">Submission</div>
        <div class="d-flex flex-wrap gap-1">
            <a href="?transferStatus=INIT&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'INIT' ? 'bg-primary text-white border-primary' : 'bg-primary-subtle text-primary-emphasis border-primary-subtle'}">Arriving</a>
            <a href="?transferStatus=SCHE&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'SCHE' ? 'bg-warning text-dark border-warning' : 'bg-warning-subtle text-warning-emphasis border-warning-subtle'}">Preset</a>
            <a href="?transferStatus=FETC&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'FETC' ? 'bg-primary text-white border-primary' : 'bg-primary-subtle text-primary-emphasis border-primary-subtle'}">Fetching</a>
        </div>
    </div>

    <div class="vr align-self-stretch d-none d-md-flex"></div>

    <%-- Processing: StandBy, Queued, Transferring, Done --%>
    <div>
        <div class="text-muted mb-1" style="font-size:0.68rem; font-weight:600; letter-spacing:0.05em; text-transform:uppercase;">Processing</div>
        <div class="d-flex flex-wrap gap-1">
            <a href="?transferStatus=HOLD&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'HOLD' ? 'bg-warning text-dark border-warning' : 'bg-warning-subtle text-warning-emphasis border-warning-subtle'}">StandBy</a>
            <a href="?transferStatus=WAIT&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'WAIT' ? 'bg-warning text-dark border-warning' : 'bg-warning-subtle text-warning-emphasis border-warning-subtle'}">Queued</a>
            <a href="?transferStatus=EXEC&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'EXEC' ? 'bg-primary text-white border-primary' : 'bg-primary-subtle text-primary-emphasis border-primary-subtle'}">Transferring</a>
            <a href="?transferStatus=DONE&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'DONE' ? 'bg-success text-white border-success' : 'bg-success-subtle text-success-emphasis border-success-subtle'}">Done</a>
        </div>
    </div>

    <div class="vr align-self-stretch d-none d-md-flex"></div>

    <%-- Failure Handling: ReQueued, Stopped, Failed, Interrupted --%>
    <div>
        <div class="text-muted mb-1" style="font-size:0.68rem; font-weight:600; letter-spacing:0.05em; text-transform:uppercase;">Failure Handling</div>
        <div class="d-flex flex-wrap gap-1">
            <a href="?transferStatus=RETR&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'RETR' ? 'bg-warning text-dark border-warning' : 'bg-warning-subtle text-warning-emphasis border-warning-subtle'}">ReQueued</a>
            <a href="?transferStatus=STOP&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'STOP' ? 'bg-secondary text-white border-secondary' : 'bg-secondary-subtle text-secondary-emphasis border-secondary-subtle'}">Stopped</a>
            <a href="?transferStatus=FAIL&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'FAIL' ? 'bg-danger text-white border-danger' : 'bg-danger-subtle text-danger-emphasis border-danger-subtle'}">Failed</a>
            <a href="?transferStatus=INTR&refreshPeriod=${currentRefresh}" class="badge rounded-pill fw-normal text-decoration-none border ${currentTransferStatus.id == 'INTR' ? 'bg-secondary text-white border-secondary' : 'bg-secondary-subtle text-secondary-emphasis border-secondary-subtle'}">Interrupted</a>
        </div>
    </div>

    <%-- Info toggle --%>
    <button class="btn btn-sm btn-link p-0 text-muted align-self-end mb-1" type="button" data-bs-toggle="collapse" data-bs-target="#transferStatusLegend" title="Status legend">
        <i class="bi bi-info-circle"></i>
    </button>

    <%-- Refresh bar pushed to the right --%>
    <div class="ms-auto d-flex align-items-center gap-1 flex-shrink-0 align-self-end">
        <i class="bi bi-arrow-clockwise text-muted me-1" style="font-size:0.85rem;" title="Auto-refresh interval"></i>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '0' ? 'active' : ''}" data-value="0">Off</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '30' ? 'active' : ''}" data-value="30">30s</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '60' ? 'active' : ''}" data-value="60">1m</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '300' ? 'active' : ''}" data-value="300">5m</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '600' ? 'active' : ''}" data-value="600">10m</a>
    </div>

</div>

<div class="collapse mb-3" id="transferStatusLegend">
    <div class="px-3 py-2 mt-1" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-radius:var(--bs-border-radius); border:1px solid var(--bs-border-color);">
        <div class="row g-3">
            <%-- Submission --%>
            <div class="col-12 col-md-4">
                <strong class="d-block mb-1">Submission</strong>
                <p class="text-muted mb-2" style="font-size:0.78rem;">Submitting a data file to the <%=System.getProperty("monitor.nickName")%> data store and registering a transfer request.</p>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Push Mode &mdash; metadata and file pushed directly</div>
                <div class="mb-2"><span class="badge bg-primary me-1">Arriving</span> Metadata registered; file content being uploaded to the data store</div>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Fetch Mode &mdash; metadata first, file retrieved asynchronously</div>
                <div class="mb-1"><span class="badge bg-warning text-dark me-1">Preset</span> Metadata registered; retrieval scheduled, awaiting the Data Retrieval Scheduler</div>
                <div><span class="badge bg-primary me-1">Fetching</span> Data Retrieval Scheduler actively retrieving the file into the data store</div>
            </div>
            <%-- Processing --%>
            <div class="col-12 col-md-4">
                <strong class="d-block mb-1">Processing</strong>
                <p class="text-muted mb-2" style="font-size:0.78rem;">Transfer request processed by <%=System.getProperty("monitor.nickName")%> schedulers; file downloaded via data portal or disseminated to a remote site.</p>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Data Portal &mdash; file waiting to be retrieved</div>
                <div class="mb-1"><span class="badge bg-warning text-dark me-1">StandBy</span> Submitted with standby option; ignored by the Data Transfer Scheduler</div>
                <div class="mb-2"><span class="badge bg-success me-1">Done</span> Data file successfully downloaded</div>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Dissemination &mdash; file sent to a remote site</div>
                <div class="mb-1"><span class="badge bg-warning text-dark me-1">Queued</span> Waiting to be picked up by the Data Transfer Scheduler</div>
                <div class="mb-1"><span class="badge bg-primary me-1">Transferring</span> Being actively disseminated to the remote site</div>
                <div><span class="badge bg-success me-1">Done</span> Transmission to the remote site completed successfully</div>
            </div>
            <%-- Failure Handling --%>
            <div class="col-12 col-md-4">
                <strong class="d-block mb-1">Failure Handling</strong>
                <p class="text-muted mb-2" style="font-size:0.78rem;">On error, the request is assigned a status; the scheduler may retry or remain in error state depending on destination/host configuration.</p>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Error State</div>
                <div class="mb-1"><span class="badge bg-danger me-1">Failed</span> Dissemination to the remote site has failed</div>
                <div class="mb-1"><span class="badge bg-secondary me-1">Interrupted</span> Processing interrupted (e.g. destination or <%=System.getProperty("monitor.nickName")%> restart)</div>
                <div class="mb-2"><span class="badge bg-secondary me-1">Stopped</span> Stopped manually, due to an unrecoverable error, or because the maximum number of retries was reached</div>
                <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Retry &mdash; automatic or manual</div>
                <div><span class="badge bg-warning text-dark me-1">ReQueued</span> Error not considered unrecoverable; re-queued for retry. Can also be manually requeued. Retries may be rescheduled to avoid too-frequent attempts, with configurable limits per destination and host</div>
            </div>
        </div>
    </div>
</div>

<script>
    var refresh = '${param["refreshPeriod"]}';
    if (refresh != '' && refresh > 0) {
        setTimeout(function() { window.location.reload(true); }, refresh * 1000);
    }
    document.querySelectorAll('.refresh-pill').forEach(function(pill) {
        pill.addEventListener('click', function(e) {
            e.preventDefault();
            var params = new URLSearchParams(window.location.search);
            params.set('refreshPeriod', this.dataset.value);
            window.location.href = '?' + params.toString();
        });
    });
</script>

<%-- Search form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="transferSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <div class="row g-2">
                        <div class="col-auto">
                            <div class="input-group flex-nowrap" style="width:auto" title="Page size">
                                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                                <select id="transferPageLen" class="form-select" style="width:auto">
                                    <option value="10">10</option>
                                    <option value="25">25</option>
                                    <option value="50">50</option>
                                    <option value="100">100</option>
                                    <option value="250">250</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-auto">
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary dropdown-toggle" type="button" id="tfrColModeBtn"
                                        data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                                    Auto
                                </button>
                                <ul class="dropdown-menu" aria-labelledby="tfrColModeBtn">
                                    <li><a class="dropdown-item" href="#" data-tfrcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                                    <li><a class="dropdown-item" href="#" data-tfrcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                                    <li><a class="dropdown-item" href="#" data-tfrcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Transfer Host, Sched. Time, Mbits/s, Prior</small></a></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="#" data-tfrcol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                                    <li id="tfrCustomColChkPanel" style="display:none;">
                                        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:170px;">
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="tfrchk-0">Destination <small>(required)</small></label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-1" data-col="1" checked><label class="form-check-label" for="tfrchk-1">Transfer Host</label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-2" data-col="2" checked><label class="form-check-label" for="tfrchk-2">Sched. Time</label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-3" data-col="3" checked disabled><label class="form-check-label text-muted" for="tfrchk-3">Target <small>(required)</small></label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-4" data-col="4" checked><label class="form-check-label" for="tfrchk-4">%</label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-5" data-col="5" checked><label class="form-check-label" for="tfrchk-5">Mbits/s</label></div>
                                            <div class="form-check mb-0"><input class="form-check-input tfr-col-chk" type="checkbox" id="tfrchk-6" data-col="6" checked><label class="form-check-label" for="tfrchk-6">Prior</label></div>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="col">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="transferSearch" id="transferSearch" type="text"
                                    placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i"
                                    title="Default search is by target. Use target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules."
                                    value='<c:out value="${transferSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-12 col-sm-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="transferType" onchange="form.submit()" title="Filter by Type">
                                    <c:forEach var="option" items="${transferTypeOptions}">
                                        <option value="${option.name}" <c:if test="${transferType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-auto d-flex gap-1">
                            <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i><span class="d-none d-sm-inline ms-1">Search</span></button>
                            <button type="button" class="btn btn-outline-primary"
                                    id="btnTransferQB"
                                    onclick="toggleQBPanel('queryBuilder','btnTransferQB')"
                                    title="Filter">
                                <i class="bi bi-sliders2"></i><span class="d-none d-sm-inline ms-1">Filter</span>
                            </button>
                            <button class="btn btn-link btn-sm text-muted p-0" type="button"
                                    data-bs-toggle="collapse" data-bs-target="#tfrQBHelp"
                                    aria-expanded="false" title="Search syntax help">
                                <i class="bi bi-info-circle"></i>
                            </button>
                        </div>
                    </div>
                    <div class="collapse mt-1" id="tfrQBHelp">
                        <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
                            <strong class="d-block mb-1">Search &amp; Filter syntax</strong>
                            <p class="mb-1">Type directly in the search box or click <i class="bi bi-sliders2"></i> <strong>Filter</strong> to open the visual query builder. Terms can be combined freely in any order.</p>
                            <ul class="mb-1 ps-3">
                                <li><strong>Default (no prefix)</strong> &mdash; matches the <code>target</code> filename. Wildcards <code>*</code> and <code>?</code> are supported.</li>
                                <li><code>target=*.dat</code>, <code>source=/tmp/*</code> &mdash; filter by target filename or source path.</li>
                                <li><code>mover=</code> &mdash; filter by Data Mover name.</li>
                                <li><code>ts&gt;10 ts&lt;=99</code> &mdash; filter by transfer size (numeric; supports <code>=</code> <code>&gt;</code> <code>&gt;=</code> <code>&lt;</code> <code>&lt;=</code>).</li>
                                <li><code>size&gt;=700kb</code> &mdash; filter by file size; units: <code>b</code>, <code>kb</code>, <code>mb</code>, <code>gb</code>.</li>
                                <li><code>priority=</code> &mdash; filter by transfer priority (0&ndash;99).</li>
                                <li><code>identity=</code>, <code>groupby=</code>, <code>checksum=</code> &mdash; other metadata filters.</li>
                                <li><code>asap=yes|no</code>, <code>deleted=yes|no</code>, <code>expired=yes|no</code>, <code>replicated=yes|no</code>, <code>proxy=yes|no</code>, <code>event=yes|no</code> &mdash; boolean flags.</li>
                                <li><code>case=i</code> &mdash; make the search case-insensitive (default is case-sensitive).</li>
                            </ul>
                            <p class="mb-0 text-muted">Example: <code>target=*.bufr expired=no size&gt;=1mb case=i</code></p>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="queryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>target=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_target" placeholder="e.g. *.dat" oninput="qbPreview()">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>source=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_source" placeholder="e.g. /tmp/*" oninput="qbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1 row-cols-3 row-cols-md-7">
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>asap</code></label>
                                    <select class="form-select form-select-sm" id="qb_asap" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>deleted</code></label>
                                    <select class="form-select form-select-sm" id="qb_deleted" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>expired</code></label>
                                    <select class="form-select form-select-sm" id="qb_expired" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>replicated</code></label>
                                    <select class="form-select form-select-sm" id="qb_replicated" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>proxy</code></label>
                                    <select class="form-select form-select-sm" id="qb_proxy" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>event</code></label>
                                    <select class="form-select form-select-sm" id="qb_event" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="qb_case" onchange="qbPreview()">
                                        <option value="s">Sensitive</option>
                                        <option value="i">Insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-12">
                                    <label class="form-label mb-0 fw-semibold"><code>ts</code> <span class="text-muted fw-normal">range (numeric)</span></label>
                                    <div class="d-flex flex-wrap gap-2">
                                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:160px">
                                            <select class="form-select form-select-sm" id="qb_ts_op1" style="width:75px;flex:none" onchange="qbPreview()">
                                                <option value="=">=</option><option value=">">&gt;</option><option value=">=">&gt;=</option><option value="<">&lt;</option><option value="<=">&lt;=</option>
                                            </select>
                                            <input type="number" class="form-control form-control-sm" id="qb_ts_val1" placeholder="from" oninput="qbPreview()">
                                        </div>
                                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:160px">
                                            <span class="text-muted small text-nowrap flex-shrink-0">to</span>
                                            <select class="form-select form-select-sm" id="qb_ts_op2" style="width:75px;flex:none" onchange="qbPreview()">
                                                <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                                            </select>
                                            <input type="number" class="form-control form-control-sm" id="qb_ts_val2" placeholder="to" oninput="qbPreview()">
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-12">
                                    <label class="form-label mb-0 fw-semibold"><code>size</code> <span class="text-muted fw-normal">range</span></label>
                                    <div class="d-flex flex-wrap gap-2">
                                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:220px">
                                            <select class="form-select form-select-sm" id="qb_size_op1" style="width:75px;flex:none" onchange="qbPreview()">
                                                <option value=">=">&gt;=</option><option value=">">&gt;</option><option value="=">=</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                            </select>
                                            <input type="number" class="form-control form-control-sm" id="qb_size_val1" placeholder="min" min="0" oninput="qbPreview()">
                                            <select class="form-select form-select-sm" id="qb_size_unit1" style="width:70px;flex:none" onchange="qbPreview()">
                                                <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                                            </select>
                                        </div>
                                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:220px">
                                            <span class="text-muted small text-nowrap flex-shrink-0">to</span>
                                            <select class="form-select form-select-sm" id="qb_size_op2" style="width:75px;flex:none" onchange="qbPreview()">
                                                <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                                            </select>
                                            <input type="number" class="form-control form-control-sm" id="qb_size_val2" placeholder="max" min="0" oninput="qbPreview()">
                                            <select class="form-select form-select-sm" id="qb_size_unit2" style="width:70px;flex:none" onchange="qbPreview()">
                                                <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-md-2">
                                    <label class="form-label mb-0 fw-semibold"><code>priority=</code></label>
                                    <input type="number" class="form-control form-control-sm" id="qb_priority" min="0" max="99" oninput="qbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>mover=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_mover" oninput="qbPreview()" list="qb_mover_list" autocomplete="off">
                                    <datalist id="qb_mover_list">
                                        <c:forEach var="ts" items="${transferServerOptions}">
                                            <option value="${ts.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>identity=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_identity" oninput="qbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>groupby=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_groupby" oninput="qbPreview()">
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>checksum=</code></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_checksum" oninput="qbPreview()">
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1 flex-wrap">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all;min-width:0" id="qb_preview">-- fill in fields above --</code>
                                <div class="d-flex gap-1 flex-shrink-0">
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="qbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="qbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
        </form>
        <script>
        function qbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function qbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function qbBuild() {
            var p = [];
            ['target','source','mover','identity','checksum','groupby'].forEach(function(f) {
                var v = qbVal('qb_' + f); if (v) p.push(f + '=' + qbQuote(v));
            });
            ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
                var v = qbVal('qb_' + f); if (v) p.push(f + '=' + v);
            });
            var prio = qbVal('qb_priority'); if (prio) p.push('priority=' + prio);
            var tv1 = qbVal('qb_ts_val1'); if (tv1) p.push('ts' + qbVal('qb_ts_op1') + tv1);
            var tv2 = qbVal('qb_ts_val2'); if (tv2) p.push('ts' + qbVal('qb_ts_op2') + tv2);
            var sv1 = qbVal('qb_size_val1'); if (sv1) p.push('size' + qbVal('qb_size_op1') + sv1 + qbVal('qb_size_unit1'));
            var sv2 = qbVal('qb_size_val2'); if (sv2) p.push('size' + qbVal('qb_size_op2') + sv2 + qbVal('qb_size_unit2'));
            if (qbVal('qb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function qbPreview() {
            var q = qbBuild();
            document.getElementById('qb_preview').textContent = q || '-- fill in fields above --';
        }
        function qbApply() {
            document.getElementById('transferSearch').value = qbBuild();
            document.getElementById('queryBuilder').style.display = 'none';
            document.getElementById('transferSearchForm').submit();
        }
        function qbClear() {
            ['target','source','mover','identity','checksum','groupby','priority'].forEach(function(f) {
                var el = document.getElementById('qb_' + f); if (el) el.value = '';
            });
            ['qb_ts_val1','qb_ts_val2','qb_size_val1','qb_size_val2'].forEach(function(id) {
                var el = document.getElementById(id); if (el) el.value = '';
            });
            ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
                document.getElementById('qb_' + f).value = '';
            });
            document.getElementById('qb_case').value = 's';
            qbPreview();
        }
        function parseQBQuery(q, prefix, pairFields, singleFields) {
            if (!q || !q.trim()) return;
            var rangeCount = {};
            var re = /([a-zA-Z]+)(>=|<=|>|<|=)"([^"]*)"|([a-zA-Z]+)(>=|<=|>|<|=)([^\s]*)/g;
            var m;
            while ((m = re.exec(q)) !== null) {
                try {
                    var field = m[1] || m[4], op = m[2] || m[5], val = m[1] ? m[3] : m[6];
                    if (pairFields && pairFields.indexOf(field) >= 0) {
                        rangeCount[field] = (rangeCount[field] || 0) + 1;
                        var idx = rangeCount[field];
                        var opEl = document.getElementById(prefix + field + '_op' + idx);
                        var valEl = document.getElementById(prefix + field + '_val' + idx);
                        var unitEl = document.getElementById(prefix + field + '_unit' + idx);
                        if (opEl) opEl.value = op;
                        if (unitEl) {
                            var unit = '', num = val;
                            ['gb','mb','kb'].forEach(function(u) { if (num.toLowerCase().endsWith(u)) { unit = u; num = num.slice(0, -u.length); } });
                            if (valEl) valEl.value = num; unitEl.value = unit;
                        } else { if (valEl) valEl.value = val; }
                    } else if (singleFields && singleFields.indexOf(field) >= 0) {
                        var opEl2 = document.getElementById(prefix + field + '_op');
                        var valEl2 = document.getElementById(prefix + field + '_val');
                        if (opEl2) opEl2.value = op; if (valEl2) valEl2.value = val;
                    } else if (op === '=') {
                        var el = document.getElementById(prefix + field);
                        if (el) { var lv = val.toLowerCase(); el.value = (lv==='true'||lv==='yes') ? 'yes' : (lv==='false'||lv==='no') ? 'no' : val; }
                    }
                } catch(e) { /* ignore unparseable token */ }
            }
        }
        document.addEventListener('DOMContentLoaded', function() {
            parseQBQuery(document.getElementById('transferSearch').value, 'qb_', ['ts','size'], []);
            qbPreview();
        });
        function toggleQBPanel(panelId, btnId) {
            var panel = document.getElementById(panelId);
            var btn = document.getElementById(btnId);
            if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
            if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
            var r = btn.getBoundingClientRect();
            var sy = window.pageYOffset || document.documentElement.scrollTop;
            var sx = window.pageXOffset || document.documentElement.scrollLeft;
            var vw = window.innerWidth || document.documentElement.clientWidth;
            var margin = 8;
            var pw = Math.min(740, vw - 2 * margin);
            var left = Math.max(sx + margin, Math.min(r.right + sx - pw, sx + vw - pw - margin));
            panel.style.top = (r.bottom + sy + 4) + 'px';
            panel.style.left = left + 'px';
            panel.style.width = pw + 'px';
            panel.style.right = 'auto';
            panel.style.overflowX = 'auto';
            parseQBQuery(document.getElementById('transferSearch').value, 'qb_', ['ts','size'], []);
            qbPreview();
            panel.style.display = 'block';
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('queryBuilder');
            var btn = document.getElementById('btnTransferQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
                panel.style.display = 'none';
        });
        window.addEventListener('resize', function() {
            var panel = document.getElementById('queryBuilder');
            if (panel) panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- Search error/empty-state banner - content managed dynamically by drawCallback --%>
<div id="transferSearchError" class="alert mb-2" style="display:none"></div>
<script>
var _transferSearchHelp = '<p class="mb-1 mt-2">You can conduct an extended search using the following rules:<\/p>' +
    '<ul class="mb-0">' +
    '<li><code>target=<\/code>, <code>source=<\/code>, <code>ts=<\/code>, <code>priority=<\/code>, ' +
    '<code>groupby=<\/code>, <code>identity=<\/code>, <code>checksum=<\/code>, <code>size=<\/code>, ' +
    '<code>replicated=<\/code>, <code>asap=<\/code>, <code>deleted=<\/code>, <code>expired=<\/code>, ' +
    '<code>proxy=<\/code>, <code>mover=<\/code>, <code>event=<\/code><\/li>' +
    '<li>Example: <code>asap=yes target=*.dat source=\/tmp\/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i<\/code><\/li>' +
    '<li><code>case=i<\/code> for case-insensitive, <code>case=s<\/code> for case-sensitive (default)<\/li>' +
    '<li>Enclose values with spaces or equals signs in double quotes, e.g. <code>&quot;United States&quot;<\/code><\/li>' +
    '<li>Wildcards: <code>*<\/code> (zero or more chars), <code>?<\/code> (exactly one char)<\/li>' +
    '<\/ul>' +
    '<div class="mt-2 text-muted small"><i class="bi bi-lightbulb"><\/i> Tip: Not sure about the syntax? Use the <a href="#" onclick="event.stopPropagation(); toggleQBPanel(\'queryBuilder\',\'btnTransferQB\'); document.getElementById(\'btnTransferQB\').scrollIntoView({behavior:\'smooth\',block:\'center\'}); return false;" class="link-secondary"><i class="bi bi-sliders2"><\/i> query builder<\/a> above to build a valid search expression.<\/div>';
function _updateTransferSearchBanner(queryError, total, hasSearch) {
    var div = document.getElementById('transferSearchError');
    if (!div) return;
    function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
    if (queryError) {
        div.innerHTML = '<strong>Error in your query:<\/strong> ' + esc(queryError) + _transferSearchHelp;
        div.style.display = '';
    } else if (total === 0 && hasSearch) {
        div.innerHTML = 'No Data Transfers found. Default search is by target.' + _transferSearchHelp;
        div.style.display = '';
    } else {
        div.style.display = 'none';
    }
}
</script>

<%-- Filter params for DataTables AJAX --%>
<input type="hidden" id="dt-date"   value="<c:out value="${selectedDate}"/>">
<input type="hidden" id="dt-status" value="<c:out value="${currentTransferStatus.id}"/>">
<input type="hidden" id="dt-search" value="<c:out value="${transferSearch}"/>">
<input type="hidden" id="dt-type"   value="<c:out value="${transferType}"/>">

<table id="transferTable" class="table table-sm table-hover table-striped align-middle w-100">
    <thead class="table-light">
        <tr>
            <th>Destination</th>
            <th>Transfer Host</th>
            <th title="Scheduled Time (UTC)">Sched. Time</th>
            <th>Target</th>
            <th>%</th>
            <th>Mbits/s</th>
            <th>Prior</th>
        </tr>
    </thead>
</table>

<script>
(function () {
    var date   = document.getElementById('dt-date').value;
    var status = document.getElementById('dt-status').value;
    var search = document.getElementById('dt-search').value;
    var type   = document.getElementById('dt-type').value;

    $.fn.dataTable.ext.errMode = 'none';

    var table = $('#transferTable').DataTable({
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/transfer/data?json=list',
            data: function (d) {
                d.date           = date;
                d.transferStatus = status;
                d.transferSearch = search;
                d.transferType   = type;
            },
            error: function () {
                _updateTransferSearchBanner('Error loading transfers. Please try again.', 0, true);
            }
        },
        order: [[2, 'desc']],
        autoWidth: false,
        columns: [
            { title: 'Destination',   orderable: true,  render: function (d) { return d; } },
            { title: 'Transfer Host', orderable: true,  render: function (d) { return d; }, width: '110px' },
            { title: 'Sched. Time',   orderable: true,  className: 'text-nowrap', width: '130px' },
            { title: 'Target',        orderable: true,  render: function (d) { return d; } },
            { title: '%',             orderable: false, className: 'text-nowrap', width: '45px' },
            { title: 'Mbits/s',       orderable: true,  className: 'text-nowrap', render: function (d) { return d; }, width: '70px' },
            { title: 'Prior',         orderable: true,  className: 'text-nowrap', width: '45px' }
        ],
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('transferPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        lengthMenu: [[10, 25, 50, 100, 250], [10, 25, 50, 100, 250]],
        language: {
            emptyTable:     'No Data Transfers found for the selected criteria.',
            loadingRecords: 'Loading&hellip;',
            processing:     '<i class="bi bi-hourglass-split"></i> Loading&hellip;'
        },
        dom: "t<'d-flex align-items-start mt-2'i<'ms-auto'p>>",
        drawCallback: function (settings) {
            var json = settings.json;
            var total = json ? (json.recordsTotal || 0) : 0;
            _updateTransferSearchBanner(json && json.error ? json.error : '', total, search.length > 0);
        }
    });

    var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('transferPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    $('#transferPageLen').val(_savedPageLen);

    $('#transferPageLen').on('change', function () {
        var len = parseInt(this.value, 10);
        try { localStorage.setItem('transferPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });

    /* ---- Cols:Auto ---- */
    var _TFR_COL_KEY        = 'tfrColMode';
    var _TFR_CUSTOM_COL_KEY = 'tfrCustomCols';
    var _TFR_COMPACT        = [1, 2, 5, 6];
    var _tfrColMode = (function() { try { return localStorage.getItem(_TFR_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
    var _tfrCustomCols = (function() {
        try { var s = localStorage.getItem(_TFR_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
        return [0,1,2,3,4,5,6];
    })();

    function _tfrShowCols(hideCols) {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
        table.columns.adjust();
    }
    function _tfrApplyCustomCols() {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) {
            var vis = (i === 0 || i === 3) ? true : _tfrCustomCols.indexOf(i) !== -1;
            table.column(i).visible(vis, false);
        }
        table.columns.adjust();
    }
    function _tfrSyncChkBoxes() {
        document.querySelectorAll('.tfr-col-chk').forEach(function(chk) {
            chk.checked = _tfrCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }
    document.querySelectorAll('.tfr-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _tfrCustomCols.indexOf(col);
            if (this.checked && idx === -1) _tfrCustomCols.push(col);
            else if (!this.checked && idx !== -1) _tfrCustomCols.splice(idx, 1);
            try { localStorage.setItem(_TFR_CUSTOM_COL_KEY, JSON.stringify(_tfrCustomCols)); } catch(e) {}
            if (_tfrColMode === 'custom') _tfrApplyCustomCols();
        });
    });
    function _tfrApplyResponsive() {
        if (_tfrColMode !== 'auto') return;
        var w = window.innerWidth;
        _tfrShowCols(w < 576 ? [1, 2, 5, 6] : w < 992 ? [1, 2] : []);
    }
    function _tfrApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#tfrColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#tfrColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#tfrColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('tfrcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        document.getElementById('tfrCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        if (mode === 'auto') _tfrApplyResponsive();
        else if (mode === 'all') _tfrShowCols([]);
        else if (mode === 'compact') _tfrShowCols(_TFR_COMPACT);
        else if (mode === 'custom') { _tfrSyncChkBoxes(); _tfrApplyCustomCols(); }
    }
    $(window).on('resize', _tfrApplyResponsive);
    _tfrApplyMode(_tfrColMode);
    $('#tfrColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
        e.preventDefault();
        var mode = $(this).data('tfrcol-mode');
        if (!mode) return;
        _tfrColMode = mode;
        try { localStorage.setItem(_TFR_COL_KEY, mode); } catch(e) {}
        _tfrApplyMode(mode);
    });
})();
</script>
