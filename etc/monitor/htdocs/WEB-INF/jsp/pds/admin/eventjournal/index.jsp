<%@ page contentType="text/html;charset=UTF-8"%>
<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(13,202,240,0.07);border-left:4px solid #0dcaf0;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-database-check text-info flex-shrink-0 mt-1"></i>
        <span>Monitor the Master's durable OpenTelemetry delivery queue and persistence failures. This page is read-only.</span>
        <button type="button" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" data-bs-toggle="offcanvas"
                data-bs-target="#journalGuide" aria-controls="journalGuide"><i class="bi bi-book"></i>
            <span class="d-none d-sm-inline ms-1">Event Journal Guide</span></button>
    </div>
</div>
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
        Queue statistics come from the Master's database for its active journal owner, including in-flight records.
        Runtime counters and failure history cover this Master process only and reset on restart.
        An unavailable measurement is not zero. An empty queue does not prove that no persistence failures occurred.
    </div></div>
    <div class="px-3 py-2 small border-bottom" id="journalUpdated" aria-live="polite">Waiting for the Master...</div>
    <div class="card-body">
        <div class="row g-3">
            <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Pending events</div><div id="journalPending" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
            <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Oldest admission (UTC)</div><div id="journalOldest" class="fw-semibold journal-value">Unavailable</div></div></div>
            <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Oldest pending age</div><div id="journalAge" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
            <div class="col-sm-6 col-xl-3"><div class="border rounded p-3 h-100"><div class="text-body-secondary small">Persistence failures since startup</div><div id="journalFailures" class="fs-4 fw-semibold journal-value">Unavailable</div></div></div>
        </div>
        <dl class="row mt-4 mb-0">
            <dt class="col-sm-4">Journal owner</dt><dd class="col-sm-8 journal-value" id="journalOwner">Unavailable</dd>
            <dt class="col-sm-4">In-flight batch records</dt><dd class="col-sm-8 journal-value" id="journalInFlight">Unavailable</dd>
            <dt class="col-sm-4">Last persisted event (UTC)</dt><dd class="col-sm-8 journal-value" id="journalPersisted">Unavailable</dd>
            <dt class="col-sm-4">Last persistence failure (UTC)</dt><dd class="col-sm-8 journal-value" id="journalPersistenceFailure">Unavailable</dd>
            <dt class="col-sm-4">Last endpoint acknowledgement (UTC)</dt><dd class="col-sm-8 journal-value" id="journalExport">Unavailable</dd>
            <dt class="col-sm-4">Last delivery failure (UTC)</dt><dd class="col-sm-8 journal-value" id="journalDeliveryFailure">Unavailable</dd>
            <dt class="col-sm-4">Last failed delivery stage</dt><dd class="col-sm-8 journal-value" id="journalFailureStage">Unavailable</dd>
        </dl>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/pds/admin/eventjournal/guide.jsp" />
<script>
$(function() {
    var timer = null, pending = null, generation = 0, stopped = false;
    function date(value) { return value ? new Date(value).toISOString().replace('T', ' ').slice(0, 19) : 'None since startup'; }
    function age(value) {
        var seconds = Math.max(0, Math.floor(value / 1000));
        return Math.floor(seconds / 86400) + 'd ' + Math.floor(seconds % 86400 / 3600) + 'h '
            + Math.floor(seconds % 3600 / 60) + 'm ' + seconds % 60 + 's';
    }
    function notice(id, text, danger) {
        $(id).text(text).toggleClass('d-none', !text).toggleClass('alert-danger', !!danger).toggleClass('alert-warning', !danger);
    }
    function unavailable(message) {
        $('.journal-value').text('Unavailable');
        $('#journalState').attr('class', 'badge bg-warning text-dark').text('Unavailable');
        $('#journalUpdated').text('No current snapshot. Refresh to retry.');
        notice('#journalWarning', '', false);
        notice('#journalError', message, true);
    }
    function render(data) {
        var s = data.journal;
        $('#journalState').attr('class', 'badge ' + (data.danger ? 'bg-danger' : data.attention ? 'bg-warning text-dark' : s.enabled ? 'bg-success' : 'bg-secondary')).text(data.state);
        $('#journalUpdated').text('Updated ' + date(s.observedAt) + ' UTC');
        notice('#journalError', '', false);
        if (!s.enabled) {
            $('.journal-value').text('Not measured');
            notice('#journalWarning', 'Database journaling is disabled on the Master. Enable openTelemetryEnabled and journalEnabled in [OperationalEvents] and restart the Master. Pending rows may still exist; this page does not measure queues while disabled.', false);
            return;
        }
        $('#journalOwner').text(s.owner);
        $('#journalPending').text(s.pendingCount == null ? 'Unavailable' : s.pendingCount.toLocaleString());
        $('#journalOldest').text(s.pendingCount == null ? 'Unavailable' : s.oldestAdmissionAt == null ? 'No pending events' : date(s.oldestAdmissionAt));
        $('#journalAge').text(s.pendingCount == null ? 'Unavailable' : s.oldestAdmissionAt == null ? 'No pending events' : age(s.observedAt - s.oldestAdmissionAt));
        $('#journalFailures').text(s.persistenceFailures.toLocaleString());
        $('#journalInFlight').text(s.inFlightCount.toLocaleString());
        $('#journalPersisted').text(date(s.lastPersistedAt));
        $('#journalPersistenceFailure').text(date(s.lastPersistenceFailureAt));
        $('#journalExport').text(date(s.lastExportAt));
        $('#journalDeliveryFailure').text(date(s.lastDeliveryFailureAt));
        $('#journalFailureStage').text(s.deliveryFailureStage || 'None since startup');
        var messages = [];
        if (s.stopped) messages.push('The journal worker is stopped.');
        if (s.persistenceDegraded) messages.push('Persistence failed and has not yet been confirmed recovered by a successful event admission. Operational processing continues with best-effort delivery; affected events are not guaranteed durable.');
        else if (s.persistenceFailures > 0) messages.push('Persistence has recovered, but ' + s.persistenceFailures + ' event(s) could not be journalled since startup. They used best-effort delivery and are not automatically backfilled.');
        if (s.deliveryDegraded) messages.push('Delivery is retrying after a failure at: ' + s.deliveryFailureStage + '. Unacknowledged events remain in the database.');
        if (s.queueError) messages.push(s.queueError + ' Queue values are unavailable, not zero.');
        notice('#journalWarning', messages.join(' '), data.danger);
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
            .done(function(data) {
                if (token !== generation) return;
                if (data.error) unavailable(data.error); else render(data);
            }).fail(function(xhr, status) {
                if (token !== generation || status === 'abort') return;
                var expired = xhr.status === 401 || xhr.getResponseHeader('X-ECPDS-Session-Expired') === 'true';
                if (expired) stopped = true;
                unavailable(expired
                    ? 'Your session has expired. Sign in again to view journal monitoring.'
                    : xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error : 'Unable to retrieve journal status from the Master.');
            }).always(function() {
                if (token !== generation) return;
                pending = null;
                $('#journalRefreshNow').prop('disabled', false);
                schedule();
            });
    }
    $('#journalRefresh').on('change', schedule);
    $('#journalRefreshNow').on('click', load);
    $(window).on('pagehide', function() { stopped = true; ++generation; clearTimeout(timer); if (pending) pending.abort(); });
    $(window).on('pageshow', function(event) { if (event.originalEvent.persisted) { stopped = false; load(); } });
    load();
});
</script>
