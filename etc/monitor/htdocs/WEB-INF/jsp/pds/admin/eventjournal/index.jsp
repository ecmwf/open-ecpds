<%@ page contentType="text/html;charset=UTF-8"%>
<style>
    .journal-detail-pill { max-width:100%; white-space:normal; overflow-wrap:anywhere; text-align:left; font-weight:500; line-height:1.5; }
    .journal-trend-pill:empty { display:none; }
</style>
<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(13,202,240,0.07);border-left:4px solid #0dcaf0;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-database-check text-info flex-shrink-0 mt-1"></i>
        <span>Monitor and control native OpenTelemetry event collection and durable delivery. Collection controls do not affect Splunk logging.</span>
        <button type="button" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" data-bs-toggle="offcanvas"
                data-bs-target="#journalGuide" aria-controls="journalGuide"><i class="bi bi-book"></i>
            <span class="d-none d-sm-inline ms-1">Event Journal Guide</span></button>
    </div>
</div>
<input type="hidden" id="journalCsrf" value="${journalCsrf}">
<div id="journalError" class="alert alert-danger d-none" role="alert"></div>
<div id="journalWarning" class="alert alert-warning d-none" role="alert"></div>
<div class="card border-0 shadow-sm mb-3">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-database-check text-info"></i><span class="fw-semibold">Operational Event Journal</span>
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
        Queue statistics are scoped to the active journal owner; unavailable values are not zero.
    </div></div>
    <div class="px-3 py-2 small border-bottom" id="journalUpdated" aria-live="polite">Waiting for the Master...</div>
    <div class="card-body">
        <ul class="nav nav-tabs mb-3" id="journalTabs" role="tablist">
            <li class="nav-item" role="presentation"><button class="nav-link active" id="journalHealthTab" data-bs-toggle="tab" data-bs-target="#journalHealth" type="button" role="tab" aria-controls="journalHealth" aria-selected="true"><i class="bi bi-heart-pulse me-1"></i>Health</button></li>
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
                            <dt class="small text-body-secondary fw-normal mb-1">Journal owner</dt>
                            <dd class="mb-3"><span class="badge rounded-pill bg-body-tertiary text-body border px-3 py-2 journal-detail-pill journal-value" id="deliveryOwner">Unavailable</span></dd>
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
    var timer = null, pending = null, generation = 0, stopped = false, samples = [], currentStatus = null, deliveryState = null;
    function date(value) { return value ? new Date(value).toISOString().replace('T', ' ').slice(0, 19) : 'None since startup'; }
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
        $('#journalState').attr('class', 'badge ' + (data.danger ? 'bg-danger' : data.attention ? 'bg-warning text-dark' : s.enabled ? 'bg-success' : 'bg-secondary')).text(data.state);
        $('#journalUpdated').text('Updated ' + date(s.observedAt) + ' UTC');
        notice('#journalError', '', false);
        if (!s.enabled) {
            $('.journal-value').text('Not measured');
            $('#collectionDescription,#deliveryDescription').text('Journal controls are unavailable because database journaling is disabled.');
            $('#collectionToggle,#deliveryToggle').prop('disabled', true);
            notice('#journalWarning', 'Database journaling is disabled on the Master. Enable openTelemetryEnabled and journalEnabled in [OperationalEvents] and restart the Master. Pending rows may still exist; this page does not measure queues while disabled.', false);
            return;
        }
        $('#journalOwner,#deliveryOwner').text(s.owner);
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
            : 'Collection is active. New native OpenTelemetry events are admitted to the durable journal.');
        $('#collectionToggle').prop('disabled', false).html(s.collectionPaused
            ? '<i class="bi bi-play-circle me-1"></i>Resume collection'
            : '<i class="bi bi-pause-circle me-1"></i>Pause collection');
        $('#deliveryDescription').text(s.deliveryPaused
            ? 'Delivery is paused. New events continue to be persisted and the queue may grow.'
            : s.deliveryDegraded ? 'Delivery is retrying after a failure; unacknowledged events remain queued.'
                : 'Delivery is active. The worker exports queued events and removes them after endpoint acknowledgement.');
        $('#deliveryToggle').prop('disabled', false).html(s.deliveryPaused
            ? '<i class="bi bi-play-circle me-1"></i>Resume delivery'
            : '<i class="bi bi-pause-circle me-1"></i>Pause delivery');
        $('#collectionToggle,#deliveryToggle').prop('disabled', s.stopped);
        trend(s);
        var messages = [];
        var pendingAge = s.oldestAdmissionAt == null ? 0 : s.observedAt - s.oldestAdmissionAt;
        if (s.stopped) messages.push('The journal worker is stopped.');
        if (s.persistenceDegraded) messages.push('Persistence failed and has not yet been confirmed recovered by a successful event admission. Operational processing continues with best-effort delivery; affected events are not guaranteed durable.');
        else if (s.persistenceFailures > 0) messages.push('Persistence has recovered, but ' + s.persistenceFailures + ' event(s) could not be journalled since startup. They used best-effort delivery and are not automatically backfilled.');
        if (s.deliveryDegraded) messages.push('Delivery is retrying after a failure at: ' + s.deliveryFailureStage + '. Unacknowledged events remain in the database.');
        if (s.deliveryPaused) messages.push('Delivery is paused by an administrator; collection and persistence continue.');
        if (s.pendingCount != null && (s.pendingCount >= s.criticalPendingCount || pendingAge >= s.criticalOldestAgeMillis))
            messages.push('Critical backlog threshold reached (' + s.pendingCount.toLocaleString() + ' pending; oldest age ' + age(pendingAge) + ').');
        else if (s.pendingCount != null && (s.pendingCount >= s.warningPendingCount || pendingAge >= s.warningOldestAgeMillis))
            messages.push('Backlog warning threshold reached (' + s.pendingCount.toLocaleString() + ' pending; oldest age ' + age(pendingAge) + ').');
        if (s.queueError) messages.push(s.queueError + ' Queue values are unavailable, not zero.');
        notice('#journalWarning', messages.join(' '), data.danger);
    }
    function unavailable(message) {
        currentStatus = null;
        $('.journal-value').text('Unavailable');
        $('#journalState').attr('class', 'badge bg-warning text-dark').text('Unavailable');
        $('#collectionToggle,#deliveryToggle').prop('disabled', true);
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
        clearTimeout(timer);
        var token = ++generation;
        if (pending) pending.abort();
        $('#journalRefreshNow').prop('disabled', true);
        pending = $.ajax({url:'/do/admin/eventjournal', data:{json:'status'}, dataType:'json', timeout:20000})
            .done(function(data) { if (token === generation) { if (data.error) unavailable(data.error); else render(data); } })
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
        if (!window.confirm('Are you sure you want to ' + (paused ? 'pause ' : 'resume ') + operation + '?')) return;
        $('#collectionToggle,#deliveryToggle').prop('disabled', true);
        $.ajax({url:'/do/admin/eventjournal', method:'POST', dataType:'json', timeout:20000,
            data:{csrf:$('#journalCsrf').val(), op:operation, state:paused ? 'paused' : 'resumed'}})
            .done(function(data) { if (data.error) notice('#journalError', data.error, true); else render(data); })
            .fail(function(xhr) {
                notice('#journalError', xhr.responseJSON && xhr.responseJSON.error
                    ? xhr.responseJSON.error : 'Unable to update the journal control. Refresh and try again.', true);
            }).always(function() {
                $('#collectionToggle,#deliveryToggle').prop('disabled',
                    !currentStatus || !currentStatus.enabled || currentStatus.stopped);
            });
    }
    $('#collectionToggle').on('click', function() { control('collection', !$(this).text().toLowerCase().includes('resume')); });
    $('#deliveryToggle').on('click', function() { control('delivery', !$(this).text().toLowerCase().includes('resume')); });
    $('#journalRefresh').on('change', schedule);
    $('#journalRefreshNow').on('click', load);
    $(window).on('pagehide', function() { stopped = true; ++generation; clearTimeout(timer); if (pending) pending.abort(); });
    $(window).on('pageshow', function(event) { if (event.originalEvent.persisted) { stopped = false; load(); } });
    load();
});
</script>
