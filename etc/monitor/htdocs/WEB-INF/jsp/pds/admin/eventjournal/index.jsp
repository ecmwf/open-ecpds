<%@ page contentType="text/html;charset=UTF-8"%>
<style>
    #journalTabs .nav-link.active { color:var(--bs-white); background-color:var(--bs-primary); border-color:var(--bs-primary); font-weight:600; }
    .journal-detail-pill { max-width:100%; white-space:normal; overflow-wrap:anywhere; text-align:left; font-weight:500; line-height:1.5; }
    .journal-trend-pill:empty { display:none; }
    .journal-admission-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(min(100%,11rem),1fr)); gap:0.5rem; }
    .journal-admission-grid > div { min-width:0; padding:0.75rem; border:1px solid var(--bs-border-color); border-radius:var(--bs-border-radius); background:var(--bs-tertiary-bg); }
    .journal-admission-grid dt { font-size:0.8rem; font-weight:400; color:var(--bs-secondary-color); margin-bottom:0.25rem; }
    .journal-admission-grid dd { margin-bottom:0; overflow-wrap:anywhere; }
    .journal-counter-value { font-size:1.1rem; font-weight:600; font-variant-numeric:tabular-nums; }
    #admissionReasonsStatus:empty { display:none; }
    #admissionDropReasons .journal-rejection-reason { min-width:0; overflow-wrap:anywhere; }
</style>
<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(13,202,240,0.07);border-left:4px solid #0dcaf0;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-database-check text-info flex-shrink-0 mt-1"></i>
        <span>Monitor and control native OpenTelemetry event collection and durable delivery. Collection controls do not affect Splunk logging.</span>
        <button type="button" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" data-bs-toggle="offcanvas"
                data-bs-target="#journalGuide" aria-controls="journalGuide"><i class="bi bi-book"></i>
            <span class="d-none d-sm-inline ms-1">OpenTelemetry Journal Guide</span></button>
    </div>
</div>
<input type="hidden" id="journalCsrf" value="${journalCsrf}">
<div id="journalError" class="alert alert-danger d-none" role="alert"></div>
<div id="journalSuccess" class="alert alert-success d-none" role="status"></div>
<div id="journalWarning" class="alert alert-warning d-none" role="alert"></div>
<div class="card border-0 shadow-sm mb-3">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-database-check text-info"></i><span class="fw-semibold">OpenTelemetry Journal</span>
        <span id="journalState" class="badge bg-secondary" role="status">Loading</span>
        <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse"
                data-bs-target="#journalInfo" aria-controls="journalInfo" aria-expanded="false" title="About journal monitoring"><i class="bi bi-info-circle"></i></button>
        <div class="ms-auto d-flex align-items-center gap-2">
            <select id="journalRefresh" class="form-select form-select-sm" style="width:auto" aria-label="Auto refresh">
                <option value="30000" selected>Refresh: 30s</option><option value="60000">Refresh: 60s</option>
                <option value="0">Refresh: Off</option>
            </select>
            <button id="journalRefreshNow" class="btn btn-outline-secondary btn-sm" type="button" title="Refresh now" aria-label="Refresh now"><i class="bi bi-arrow-clockwise"></i></button>
        </div>
    </div>
    <div class="collapse" id="journalInfo"><div class="ecpds-help-panel">
        Collection pause affects native OpenTelemetry events only; Splunk/file logging continues unchanged.
        Delivery pause leaves event admission active and retains queued rows. An in-flight export may complete.
        Queue statistics cover this Master's journal; unavailable values are not zero.
    </div></div>
    <div class="px-3 py-2 small border-bottom" id="journalUpdated" aria-live="polite">Waiting for the Master...</div>
    <div class="card-body">
        <ul class="nav nav-tabs mb-3" id="journalTabs" role="tablist">
            <li class="nav-item" role="presentation"><button class="nav-link active" id="journalHealthTab" data-bs-toggle="tab" data-bs-target="#journalHealth" type="button" role="tab" aria-controls="journalHealth" aria-selected="true"><i class="bi bi-heart-pulse me-1"></i>Health</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="journalAdmissionTab" data-bs-toggle="tab" data-bs-target="#journalAdmission" type="button" role="tab" aria-controls="journalAdmission" aria-selected="false"><i class="bi bi-speedometer2 me-1"></i>Admission &amp; rejections</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="journalCollectionTab" data-bs-toggle="tab" data-bs-target="#journalCollection" type="button" role="tab" aria-controls="journalCollection" aria-selected="false"><i class="bi bi-collection me-1"></i>Collecting new events</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="journalDeliveryTab" data-bs-toggle="tab" data-bs-target="#journalDelivery" type="button" role="tab" aria-controls="journalDelivery" aria-selected="false"><i class="bi bi-send me-1"></i>Delivering queued events</button></li>
        </ul>
        <div class="tab-content">
            <section class="tab-pane fade show active" id="journalHealth" role="tabpanel" aria-labelledby="journalHealthTab" tabindex="0">
                <div class="row g-3">
                    <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Pending events</div><div id="journalPending" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
                    <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Oldest pending age</div><div id="journalAge" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
                    <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Persistence failures since startup</div><div id="journalFailures" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
                    <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">In-flight batch records</div><div id="journalInFlight" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
                </div>
                <div class="card border-0 bg-body-tertiary mt-3">
                    <div class="card-header bg-transparent d-flex align-items-center gap-2"><i class="bi bi-graph-up text-info"></i><span class="fw-semibold">Backlog trend</span>
                        <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#trendInfo" aria-controls="trendInfo" aria-expanded="false" title="About backlog trends"><i class="bi bi-info-circle"></i></button></div>
                    <div class="collapse" id="trendInfo"><div class="ecpds-help-panel">The chart and rate use queue snapshots collected by this browser while the page is open. A drain estimate appears only after at least four samples spanning two minutes and a decreasing, healthy queue. It is an extrapolation, not a delivery guarantee.</div></div>
                    <div class="card-body">
                        <svg id="backlogChart" viewBox="0 0 600 120" class="w-100" role="img" aria-label="Pending event queue trend"><text x="300" y="62" text-anchor="middle" fill="currentColor">Waiting for queue samples</text></svg>
                        <div class="d-flex flex-wrap gap-2 mt-3">
                            <span class="badge rounded-pill bg-body text-body-secondary border px-3 py-2 journal-detail-pill" id="backlogTrend">Collecting queue samples.</span>
                            <span class="badge rounded-pill bg-info-subtle text-info-emphasis border border-info-subtle px-3 py-2 journal-detail-pill journal-trend-pill" id="backlogEstimate"></span>
                        </div>
                    </div>
                </div>
                <div class="mt-3">
                    <button class="btn btn-outline-secondary btn-sm" type="button" data-bs-toggle="collapse" data-bs-target="#journalTechnicalDetails" aria-controls="journalTechnicalDetails" aria-expanded="false"><i class="bi bi-info-circle me-1"></i>Technical details</button>
                </div>
                <div class="collapse" id="journalTechnicalDetails">
                <div class="row g-3 mt-1">
                    <div class="col-md-6 col-xl-4"><div class="border rounded p-3 h-100">
                        <h6 class="d-flex align-items-center gap-2 mb-3"><i class="bi bi-database text-info"></i>Journal identity</h6>
                        <dl class="mb-0">
                            <dt class="small text-body-secondary fw-normal mb-1">Journal owner</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalOwner">Unavailable</span></dd>
                            <dt class="small text-body-secondary fw-normal mb-1">Oldest admission (UTC)</dt>
                            <dd class="mb-0"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalOldest">Unavailable</span></dd>
                        </dl>
                    </div></div>
                    <div class="col-md-6 col-xl-4"><div class="border rounded p-3 h-100">
                        <h6 class="d-flex align-items-center gap-2 mb-3"><i class="bi bi-database-check text-info"></i>Persistence activity</h6>
                        <dl class="mb-0">
                            <dt class="small text-body-secondary fw-normal mb-1">Last persisted event (UTC)</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalPersisted">Unavailable</span></dd>
                            <dt class="small text-body-secondary fw-normal mb-1">Last persistence failure (UTC)</dt>
                            <dd class="mb-0"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalPersistenceFailure">Unavailable</span></dd>
                        </dl>
                    </div></div>
                    <div class="col-md-6 col-xl-4"><div class="border rounded p-3 h-100">
                        <h6 class="d-flex align-items-center gap-2 mb-3"><i class="bi bi-send text-info"></i>Delivery activity</h6>
                        <dl class="mb-0">
                            <dt class="small text-body-secondary fw-normal mb-1">Last endpoint acknowledgement (UTC)</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalExport">Unavailable</span></dd>
                            <dt class="small text-body-secondary fw-normal mb-1">Last delivery failure (UTC)</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalDeliveryFailure">Unavailable</span></dd>
                            <dt class="small text-body-secondary fw-normal mb-1">Last failed delivery stage</dt>
                            <dd class="mb-0"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="journalFailureStage">Unavailable</span></dd>
                        </dl>
                    </div></div>
                </div>
                </div>
            </section>
            <section class="tab-pane fade" id="journalAdmission" role="tabpanel" aria-labelledby="journalAdmissionTab" tabindex="0">
                <div class="d-flex align-items-center gap-2 mb-2">
                    <h6 class="mb-0"><i class="bi bi-speedometer2 text-info me-1"></i>Admission limits</h6>
                    <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#admissionLimitsInfo" aria-controls="admissionLimitsInfo" aria-expanded="false" title="About admission limits"><i class="bi bi-info-circle"></i></button>
                </div>
                <div class="collapse mb-2" id="admissionLimitsInfo"><div class="ecpds-help-panel">
                    <p>Cards show current usage and maximum capacity. Byte values use binary units (KiB/MiB); hover for exact bytes.
                        Limits apply to serialized OpenTelemetry records (including attributes and resource metadata), not the original file or MQTT message.
                        A full queue can reject a small event. Database usage is cached for up to one minute; memory usage is sampled on refresh.</p>
                    <p class="mb-0">Memory limits use <code>journalMaxMemoryEvents</code> and <code>journalMaxMemoryBytes</code>.
                        Database limits use <code>journalMaxPendingEvents</code> and <code>journalMaxPayloadBytes</code>.
                        The single-event ceiling uses <code>journalMaxEventBytes</code>, bounded by both queue byte limits.
                        All are configured in the Master's <code>[OperationalEvents]</code> section; restart required. Raising memory limits increases potential heap usage and telemetry lost on a crash.
                        The ceiling does not guarantee database packet or Collector acceptance.</p>
                </div></div>
                <dl class="journal-admission-grid mb-3">
                    <div><dt><i class="bi bi-memory me-1" aria-hidden="true"></i>Memory queue events</dt>
                        <dd><div id="memoryEventUsage" class="journal-counter-value journal-value">Unavailable</div><div class="small text-body-secondary">Max <span id="memoryEventLimit" class="journal-value">Unavailable</span></div></dd></div>
                    <div><dt><i class="bi bi-memory me-1" aria-hidden="true"></i>Memory queue bytes</dt>
                        <dd><div id="memoryByteUsage" class="journal-counter-value journal-value">Unavailable</div><div class="small text-body-secondary">Max <span id="memoryByteLimit" class="journal-value">Unavailable</span></div></dd></div>
                    <div><dt><i class="bi bi-database me-1" aria-hidden="true"></i>Database pending events</dt>
                        <dd><div id="databaseEventUsage" class="journal-counter-value journal-value">Unavailable</div><div class="small text-body-secondary">Max <span id="databaseEventLimit" class="journal-value">Unavailable</span></div></dd></div>
                    <div><dt><i class="bi bi-database me-1" aria-hidden="true"></i>Database serialized bytes</dt>
                        <dd><div id="databaseByteUsage" class="journal-counter-value journal-value">Unavailable</div><div class="small text-body-secondary">Max <span id="databaseByteLimit" class="journal-value">Unavailable</span></div></dd></div>
                    <div><dt><i class="bi bi-file-earmark-code me-1" aria-hidden="true"></i>Single serialized event</dt>
                        <dd><div id="singleEventLimit" class="journal-counter-value journal-value">Unavailable</div><div class="small text-body-secondary">Maximum size</div></dd></div>
                </dl>
                <p id="lastAdmissionRejection" class="small journal-value">No rejection details available.</p>
                <div class="card">
                    <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
                        <i class="bi bi-list-check text-info"></i><span class="fw-semibold">Rejections since Master startup</span>
                        <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#admissionRejectionsInfo" aria-controls="admissionRejectionsInfo" aria-expanded="false" title="About rejection counts"><i class="bi bi-info-circle"></i></button>
                    </div>
                    <div class="collapse" id="admissionRejectionsInfo"><div class="ecpds-help-panel">Cumulative dropped/unconfirmed admissions grouped by reason, not individual event records.
                        Highest counts appear first. Historical counts remain after queue discard; events are not replayed.</div></div>
                    <div id="admissionReasonsStatus" class="px-3 py-2 small text-body-secondary" role="status">Waiting for the Master...</div>
                    <ul id="admissionDropReasons" class="list-group list-group-flush"></ul>
                </div>
            </section>
            <section class="tab-pane fade" id="journalCollection" role="tabpanel" aria-labelledby="journalCollectionTab" tabindex="0">
                <div class="row g-3">
                    <div class="col-lg-8"><div class="card h-100">
                        <div class="card-header d-flex align-items-center gap-2"><i class="bi bi-collection text-info"></i><span class="fw-semibold">Native OpenTelemetry collection</span>
                            <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#collectionInfo" aria-controls="collectionInfo" aria-expanded="false" title="About event collection"><i class="bi bi-info-circle"></i></button></div>
                        <div class="collapse" id="collectionInfo"><div class="ecpds-help-panel">Pausing collection stops new native OpenTelemetry log records from entering the journal. Existing records continue to be delivered. Legacy Splunk/file output remains enabled or disabled according to its separate configuration.</div></div>
                        <div class="card-body">
                            <p id="collectionDescription">Waiting for the Master...</p>
                            <button id="collectionToggle" class="btn btn-outline-warning" type="button" disabled><i class="bi bi-pause-circle me-1"></i>Pause collection</button>
                        </div>
                    </div></div>
                    <div class="col-lg-4"><div class="card h-100"><div class="card-header fw-semibold">Configuration</div><div class="card-body small">
                        <p>Native OpenTelemetry collection and database journaling are configured on the Master in <code>[OperationalEvents]</code>. Configuration changes require a Master restart.</p>
                        <p class="mb-0">The runtime pause is temporary and resets when the Master restarts.</p>
                    </div></div></div>
                </div>
            </section>
            <section class="tab-pane fade" id="journalDelivery" role="tabpanel" aria-labelledby="journalDeliveryTab" tabindex="0">
                <div class="row g-3">
                    <div class="col-lg-8"><div class="card h-100">
                        <div class="card-header d-flex align-items-center gap-2"><i class="bi bi-send text-info"></i><span class="fw-semibold">Queued event delivery</span>
                            <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#deliveryInfo" aria-controls="deliveryInfo" aria-expanded="false" title="About queue delivery"><i class="bi bi-info-circle"></i></button></div>
                        <div class="collapse" id="deliveryInfo"><div class="ecpds-help-panel">Pausing delivery stops the journal worker from starting subsequent export batches. New events continue to be persisted, so the queue may grow. A request already in flight may finish. Resume delivery to continue automatic retries.</div></div>
                        <div class="card-body">
                            <p id="deliveryDescription">Waiting for the Master...</p>
                            <button id="deliveryToggle" class="btn btn-outline-warning" type="button" disabled><i class="bi bi-pause-circle me-1"></i>Pause delivery</button>
                            <div class="border border-danger-subtle rounded p-3 mt-4">
                                <h6 class="text-danger">Discard pending events</h6>
                                <p class="small">Permanently delete this Master's unacknowledged telemetry, including memory-queued events. This cannot be undone. Splunk logs and transfer history are not affected.</p>
                                <p class="small text-body-secondary">Pause both collection and delivery first. Any in-flight export must finish before discarding.</p>
                                <button id="journalDiscard" class="btn btn-outline-danger" type="button" disabled><i class="bi bi-trash me-1"></i>Discard pending events</button>
                            </div>
                            <div class="mt-4">
                                <div class="small fw-semibold mb-2">Backlog alert thresholds <span class="text-body-secondary fw-normal">(configured in Master ecmwf.properties; restart required to change)</span></div>
                                <div class="table-responsive"><table class="table table-sm mb-0"><thead><tr><th>Level</th><th>Pending events</th><th>Oldest age</th></tr></thead>
                                    <tbody><tr><th>Warning</th><td id="warningCount">Unavailable</td><td id="warningAge">Unavailable</td></tr>
                                    <tr><th>Critical</th><td id="criticalCount">Unavailable</td><td id="criticalAge">Unavailable</td></tr></tbody></table></div>
                            </div>
                        </div>
                    </div></div>
                    <div class="col-lg-4"><div class="card h-100">
                        <div class="card-header d-flex align-items-center gap-2"><i class="bi bi-send-check text-info"></i><span class="fw-semibold">Delivery status</span></div>
                        <div class="card-body">
                        <dl class="mb-0">
                            <dt class="small text-body-secondary fw-normal mb-1">Last acknowledged batch (UTC)</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="deliveryLastExport">Unavailable</span></dd>
                            <dt class="small text-body-secondary fw-normal mb-1">Most recent delivery failure (UTC)</dt>
                            <dd class="mb-0"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="deliveryLastFailure">Unavailable</span></dd>
                        </dl>
                        <div class="d-flex align-items-start gap-2 bg-info-subtle text-info-emphasis rounded p-3 mt-4 small">
                            <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
                            <span>Pausing delivery does not stop event admission or remove database rows.</span>
                        </div>
                    </div></div></div>
                </div>
            </section>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/pds/admin/eventjournal/guide.jsp" />
<script>
$(function() {
    var timer = null, pending = null, generation = 0, stopped = false, samples = [], currentStatus = null, deliveryState = null, changing = false;
    function rejectionReasons(rows, message) {
        $('#admissionReasonsStatus').text(message);
        var list = $('#admissionDropReasons').empty();
        rows.sort(function(left, right) { return right[1] - left[1]; });
        rows.forEach(function(entry) {
            $('<li class="list-group-item d-flex align-items-center justify-content-between gap-3">')
                .append($('<span class="journal-rejection-reason">').text(entry[0]),
                    $('<span class="badge rounded-pill bg-body-tertiary text-body border flex-shrink-0">')
                        .text(entry[1].toLocaleString())).appendTo(list);
        });
    }
    function controls() {
        var s = currentStatus, disabled = changing || !s || !s.enabled || s.stopped;
        $('#collectionToggle,#deliveryToggle').prop('disabled', disabled);
        $('#journalDiscard').prop('disabled', disabled || !s.collectionPaused || !s.deliveryPaused || s.pendingCount == null || !!s.queueError);
    }
    function date(value) { return value ? new Date(value).toISOString().replace('T', ' ').slice(0, 19) : 'None since startup'; }
    function bytes(value) {
        return value == null ? 'Unavailable' : value.toLocaleString() + ' bytes (' + (value / 1048576).toFixed(2) + ' MiB)';
    }
    function byteCard(id, value) {
        var text = value == null ? 'Unavailable' : value < 1024 ? value + ' B'
            : value < 1048576 ? +(value / 1024).toFixed(2) + ' KiB' : +(value / 1048576).toFixed(2) + ' MiB';
        $(id).text(text).attr('title', bytes(value));
    }
    function admission(s) {
        var a = s.admission;
        $('#databaseEventUsage').text(s.pendingCount == null ? 'Unavailable' : s.pendingCount.toLocaleString());
        $('#databaseEventLimit').text(s.maxPendingEvents.toLocaleString());
        byteCard('#databaseByteUsage', s.pendingPayloadBytes);
        byteCard('#databaseByteLimit', s.maxPayloadBytes);
        $('#memoryEventUsage').text(a ? a.memoryEvents.toLocaleString() : 'Unavailable');
        $('#memoryEventLimit').text(a ? a.maxMemoryEvents.toLocaleString() : 'Unavailable');
        byteCard('#memoryByteUsage', a && a.memoryBytes);
        byteCard('#memoryByteLimit', a && a.maxMemoryBytes);
        byteCard('#singleEventLimit', a && a.maxEventBytes);
        $('#lastAdmissionRejection').text(!a ? 'Rejection details unavailable; update the Master.'
            : !a.lastRejectedAt ? 'No admission rejections since startup.'
                : 'Last rejected/unconfirmed admission: ' + date(a.lastRejectedAt) + ' UTC. Serialized size: '
                    + (a.lastRejectedBytes == null ? 'unavailable (for example, serialization failed)' : bytes(a.lastRejectedBytes))
                    + '. Reason: ' + s.lastDropReason + '.');
        var reasons = a && a.droppedByReason ? Object.entries(a.droppedByReason) : [];
        rejectionReasons(reasons, !a ? 'Rejection details unavailable; update the Master.'
            : !reasons.length ? 'No admission rejections since startup.' : '');
    }
    function age(value) {
        var seconds = Math.max(0, Math.floor(value / 1000));
        return Math.floor(seconds / 86400) + 'd ' + Math.floor(seconds % 86400 / 3600) + 'h '
            + Math.floor(seconds % 3600 / 60) + 'm ' + seconds % 60 + 's';
    }
    function notice(id, text, danger) {
        $(id).text(text).toggleClass('d-none', !text).toggleClass('alert-danger', !!danger).toggleClass('alert-warning', !danger);
    }
    function trend(s) {
        var nextDeliveryState = s.deliveryPaused ? 'paused' : s.deliveryDegraded ? 'retrying' : 'active';
        if (deliveryState !== null && deliveryState !== nextDeliveryState) samples = [];
        deliveryState = nextDeliveryState;
        if (s.pendingCount == null || s.queueError) {
            samples = [];
            $('#backlogChart').html('<text x="300" y="62" text-anchor="middle" fill="currentColor">Queue statistics unavailable</text>');
            $('#backlogTrend').text('Queue statistics unavailable.');
            $('#backlogEstimate').text('');
            return;
        }
        samples.push({time:s.observedAt, count:s.pendingCount});
        samples = samples.filter(function(p) { return p.time >= s.observedAt - 300000; });
        if (samples.length > 12) samples.shift();
        var max = Math.max(1, ...samples.map(function(p) { return p.count; }));
        var points = samples.map(function(p, i) {
            return (samples.length === 1 ? 300 : 20 + i * 560 / (samples.length - 1)) + ','
                + (100 - Math.round(p.count / max * 80));
        }).join(' ');
        var latestPoint = points.split(' ').pop().split(',');
        $('#backlogChart').html('<polyline points="' + points + '" fill="none" stroke="var(--bs-info)" stroke-width="3" vector-effect="non-scaling-stroke"></polyline>'
            + '<circle cx="' + latestPoint[0] + '" cy="' + latestPoint[1] + '" r="4" fill="var(--bs-info)"></circle>');
        if (samples.length < 4 || samples[samples.length - 1].time - samples[0].time < 120000) {
            $('#backlogTrend').text('Collecting baseline (' + samples.length + ' samples).');
            $('#backlogEstimate').text('');
            return;
        }
        var first = samples[0], last = samples[samples.length - 1];
        var change = last.count - first.count;
        var rate = change / ((last.time - first.time) / 3600000);
        $('#backlogTrend').text((rate < 0 ? 'Decreasing ' : rate > 0 ? 'Increasing ' : 'Stable ')
            + Math.abs(Math.round(rate)) + ' events/hour over ' + age(last.time - first.time));
        if (last.count === 0) {
            $('#backlogEstimate').text('Queue is empty.');
        } else if (s.deliveryPaused || s.deliveryDegraded || change > -2) {
            $('#backlogEstimate').text(s.deliveryPaused ? 'Drain estimate unavailable while delivery is paused.'
                : s.deliveryDegraded ? 'Drain estimate unavailable while delivery is retrying.'
                : rate >= 0 ? 'No drain estimate: recent queue is not decreasing.'
                    : 'No drain estimate: recent decrease is too small to extrapolate.');
        } else {
            var hours = Math.ceil(last.count / -rate);
            $('#backlogEstimate').text('Estimated drain: ' + (hours < 24 ? hours + 'h' : Math.floor(hours / 24) + 'd ' + hours % 24 + 'h') + ' at the recent rate.');
        }
    }
    function render(data) {
        var s = data.journal;
        currentStatus = s;
        controls();
        $('#journalState').attr('class', 'badge ' + (data.danger ? 'bg-danger' : data.attention ? 'bg-warning text-dark' : s.enabled ? 'bg-success' : 'bg-secondary')).text(data.state);
        $('#journalUpdated').text('Updated ' + date(s.observedAt) + ' UTC');
        notice('#journalError', '', false);
        if (!s.enabled) {
            $('.journal-value').text('Not measured').removeAttr('title');
            rejectionReasons([], 'Rejection counts are not measured while database journaling is disabled.');
            $('#collectionDescription,#deliveryDescription').text('Journal controls are unavailable because database journaling is disabled.');
            $('#collectionToggle,#deliveryToggle').prop('disabled', true);
            notice('#journalWarning', 'Database journaling is disabled on the Master. Enable openTelemetryEnabled and journalEnabled in [OperationalEvents] and restart the Master. Pending rows may still exist; this page does not measure queues while disabled.', false);
            return;
        }
        $('#journalOwner').text(s.owner);
        admission(s);
        $('#journalPending').text(s.pendingCount == null ? 'Unavailable' : s.pendingCount.toLocaleString());
        $('#journalOldest').text(s.pendingCount == null ? 'Unavailable' : s.oldestAdmissionAt == null ? 'No pending events' : date(s.oldestAdmissionAt) + ' UTC');
        $('#journalAge').text(s.pendingCount == null ? 'Unavailable' : s.oldestAdmissionAt == null ? 'No pending events' : age(s.observedAt - s.oldestAdmissionAt));
        $('#journalFailures').text(s.persistenceFailures.toLocaleString());
        $('#journalInFlight').text(s.inFlightCount.toLocaleString());
        $('#journalPersisted').text(date(s.lastPersistedAt));
        $('#journalPersistenceFailure').text(date(s.lastPersistenceFailureAt));
        $('#journalExport,#deliveryLastExport').text(date(s.lastExportAt));
        $('#journalDeliveryFailure,#deliveryLastFailure').text(date(s.lastDeliveryFailureAt));
        $('#journalFailureStage').text(s.deliveryFailureStage || 'None since startup');
        $('#warningCount').text(s.warningPendingCount.toLocaleString());
        $('#criticalCount').text(s.criticalPendingCount.toLocaleString());
        $('#warningAge').text(age(s.warningOldestAgeMillis));
        $('#criticalAge').text(age(s.criticalOldestAgeMillis));
        $('#collectionDescription').text(s.collectionPaused
            ? 'Collection is paused. New native OpenTelemetry events are not admitted; Splunk/file output is unaffected.'
            : 'Collection is active. New native OpenTelemetry events enter bounded journal admission; excess events are dropped.');
        $('#collectionToggle').prop('disabled', false).html(s.collectionPaused
            ? '<i class="bi bi-play-circle me-1"></i>Resume collection'
            : '<i class="bi bi-pause-circle me-1"></i>Pause collection');
        $('#deliveryDescription').text(s.deliveryPaused
            ? s.collectionPaused ? 'Collection and delivery are paused. Previously admitted events may still be persisted.'
                : 'Delivery is paused. New events may be persisted up to the admission limits; excess events are dropped.'
            : s.deliveryDegraded ? 'Delivery is retrying after a failure; unacknowledged events remain queued.'
                : 'Delivery is active. The worker exports queued events and removes them after endpoint acknowledgement.');
        $('#deliveryToggle').prop('disabled', false).html(s.deliveryPaused
            ? '<i class="bi bi-play-circle me-1"></i>Resume delivery'
            : '<i class="bi bi-pause-circle me-1"></i>Pause delivery');
        controls();
        trend(s);
        var messages = [];
        if (s.droppedEvents > 0) messages.push('Telemetry dropped/unconfirmed since Master startup: '
            + s.droppedEvents.toLocaleString() + '. Last reason: ' + s.lastDropReason
            + '. Dropped events are not replayed; Splunk/file output is unaffected.');
        var pendingAge = s.oldestAdmissionAt == null ? 0 : s.observedAt - s.oldestAdmissionAt;
        if (s.stopped) messages.push('The journal worker is stopped.');
        if (s.persistenceDegraded) messages.push('Persistence failed and has not yet been confirmed recovered by a successful event admission. Operational processing continues; affected new telemetry is dropped.');
        else if (s.persistenceFailures > 0) messages.push('Persistence has recovered, but ' + s.persistenceFailures + ' event admission(s) failed or could not be confirmed since startup. These are not automatically backfilled.');
        if (s.deliveryDegraded) messages.push('Delivery is retrying after a failure at: ' + s.deliveryFailureStage + '. Unacknowledged events remain in the database.');
        if (s.deliveryPaused && !s.collectionPaused) messages.push('Delivery is paused by an operator; collection and persistence continue.');
        if (s.pendingCount != null && (s.pendingCount >= s.criticalPendingCount || pendingAge >= s.criticalOldestAgeMillis))
            messages.push('Critical backlog threshold reached (' + s.pendingCount.toLocaleString() + ' pending; oldest age ' + age(pendingAge) + ').');
        else if (s.pendingCount != null && (s.pendingCount >= s.warningPendingCount || pendingAge >= s.warningOldestAgeMillis))
            messages.push('Backlog warning threshold reached (' + s.pendingCount.toLocaleString() + ' pending; oldest age ' + age(pendingAge) + ').');
        if (s.queueError) messages.push(s.queueError + ' Queue values are unavailable, not zero.');
        notice('#journalWarning', messages.join(' '), data.danger);
    }
    function unavailable(message) {
        currentStatus = null;
        $('.journal-value').text('Unavailable').removeAttr('title');
        rejectionReasons([], 'Rejection counts unavailable.');
        $('#journalState').attr('class', 'badge bg-warning text-dark').text('Unavailable');
        controls();
        $('#journalUpdated').text('No current snapshot. Refresh to retry.');
        notice('#journalWarning', '', false);
        notice('#journalError', message, true);
    }
    function schedule() {
        clearTimeout(timer);
        var delay = +$('#journalRefresh').val();
        if (!stopped && delay > 0) timer = setTimeout(load, delay);
    }
    function load() {
        if (changing) return;
        clearTimeout(timer);
        var token = ++generation;
        if (pending) pending.abort();
        $('#journalRefreshNow').prop('disabled', true);
        pending = $.ajax({url:'/do/monitoring/eventjournal', data:{json:'status'}, dataType:'json', timeout:20000})
            .done(function(data) {
                if (token !== generation) return;
                try {
                    if (data.error) unavailable(data.error); else render(data);
                } catch (error) {
                    console.error('Unable to display journal status', error);
                    unavailable('Unable to display journal status. Refresh will retry according to the selected interval.');
                }
            })
            .fail(function(xhr, status) {
                if (token !== generation || status === 'abort') return;
                var expired = xhr.status === 401 || xhr.getResponseHeader('X-ECPDS-Session-Expired') === 'true';
                if (expired) stopped = true;
                unavailable(expired ? 'Your session has expired. Sign in again to view journal monitoring.'
                    : xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error : 'Unable to retrieve journal status from the Master.');
            }).always(function() {
                if (token !== generation) return;
                pending = null;
                $('#journalRefreshNow').prop('disabled', false);
                schedule();
            });
    }
    function control(operation, paused) {
        if (changing || !currentStatus) return;
        var discard = operation === 'discard';
        var owner = currentStatus.owner;
        var confirmation = discard
            ? 'Permanently discard ' + currentStatus.pendingCount.toLocaleString() + ' pending database events and any memory-queued events for journal owner "' + currentStatus.owner + '"? This cannot be undone. No acknowledgement or delivery is implied.'
            : 'Are you sure you want to ' + (paused ? 'pause ' : 'resume ') + operation + '?';
        confirmationDialog({
            title: discard ? 'Discard pending events' : 'Confirm journal control',
            message: confirmation, allowHtml: false, showLoading: false,
            confirmText: discard ? 'Discard pending events' : 'Confirm',
            onConfirm: function() {
                if (changing || !currentStatus || currentStatus.owner !== owner
                        || $('#'+ (discard ? 'journalDiscard' : operation === 'collection' ? 'collectionToggle' : 'deliveryToggle')).prop('disabled')) return;
                applyControl(operation, paused);
            }
        });
    }
    function applyControl(operation, paused) {
        var discard = operation === 'discard';
        changing = true;
        $('#journalRefreshNow').prop('disabled', true);
        clearTimeout(timer);
        ++generation;
        if (pending) { pending.abort(); pending = null; }
        controls();
        $('#journalSuccess').addClass('d-none');
        $.ajax({url:'/do/monitoring/eventjournal', method:'POST', dataType:'json', timeout:20000,
            data:{csrf:$('#journalCsrf').val(), op:operation, state:paused ? 'paused' : 'resumed',
                confirmation:discard ? 'discard' : ''}})
            .done(function(data) {
                if (data.error) notice('#journalError', data.error, true);
                else {
                    if (discard) samples = [];
                    render(data);
                    if (discard) $('#journalSuccess').text(data.discarded.toLocaleString()
                        + ' pending events permanently discarded. Collection and delivery remain paused.').removeClass('d-none');
                }
            })
            .fail(function(xhr) {
                notice('#journalError', xhr.responseJSON && xhr.responseJSON.error
                    ? xhr.responseJSON.error : 'Unable to update the journal control. Refresh and try again.', true);
            }).always(function() {
                changing = false;
                controls();
                $('#journalRefreshNow').prop('disabled', false);
                load();
            });
    }
    $('#collectionToggle').on('click', function() { control('collection', !$(this).text().toLowerCase().includes('resume')); });
    $('#deliveryToggle').on('click', function() { control('delivery', !$(this).text().toLowerCase().includes('resume')); });
    $('#journalDiscard').on('click', function() { control('discard', false); });
    $('#journalRefresh').on('change', schedule);
    $('#journalRefreshNow').on('click', function() { stopped = false; load(); });
    $(window).on('pagehide', function() { stopped = true; ++generation; clearTimeout(timer); if (pending) pending.abort(); });
    $(window).on('pageshow', function(event) { if (event.originalEvent.persisted) { stopped = false; load(); } });
    load();
});
</script>
