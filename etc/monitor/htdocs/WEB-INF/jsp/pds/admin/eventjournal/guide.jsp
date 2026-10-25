<%@ page contentType="text/html;charset=UTF-8"%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="journalGuide" aria-labelledby="journalGuideLabel" style="width:760px;max-width:95vw;">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <h6 class="offcanvas-title mb-0 fw-semibold" id="journalGuideLabel"><i class="bi bi-book me-2 text-info"></i>Event Journal Guide</h6>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-3" style="font-size:0.85rem;">
        <div class="alert alert-info">The database journal persists operational events before exporting them through OpenTelemetry.
            It is a delivery queue, not a permanent event archive or a transactional audit trail.</div>
        <h6>Reading the queue</h6>
        <p><strong>Pending events</strong> includes records awaiting export, endpoint acknowledgement or database deletion.
            <strong>Oldest admission</strong> is when the oldest pending row entered the database, not its event timestamp.
            Configured warning and critical thresholds use either pending count or oldest age.</p>
        <p>The backlog chart uses samples collected by this browser and resets when the page is reloaded.
            A drain estimate appears only after four or more samples across at least two minutes show a healthy decrease
            of at least two events. It is an extrapolation from recent throughput, not a delivery guarantee.</p>
        <p>Only the active Master's <strong>journal owner</strong> is measured. Keep that owner stable and run only one
            active Master per owner. Disabled journaling does not delete pending rows, and changing the owner leaves
            the other owner's rows untouched. Runtime counters reset on Master restart; pending rows survive.</p>
        <h6>Persistence failures</h6>
        <p>A failed admission does not stop operational processing. The event uses best-effort in-memory delivery and
            is not automatically backfilled into the journal. A red indicator remains until a successful admission
            confirms recovery. A yellow indicator then preserves the failure count for the rest of this Master process,
            even if the queue is empty. Check the Master log for <code>DATABASE EVENT JOURNAL UNAVAILABLE</code>
            and investigate database connectivity, permissions, schema and storage capacity.</p>
        <h6>Export failures and acknowledgements</h6>
        <p>The worker retries automatically. Check the Master log and Collector diagnostics for endpoint, TLS,
            authentication, rejected batches, decoding or database deletion errors. Error details are deliberately not
            displayed here because they may contain sensitive information. Permanently rejected batches or undecodable
            rows can block the queue; there is no automatic expiry or silent purge.</p>
        <p>Endpoint acknowledgement is not proof of backend indexing or durable storage. Retries can produce duplicates;
            deduplicate using <code>ecpds.event.id</code>. OTLP partial-success responses can acknowledge a batch containing
            rejected records; this journal cannot identify and replay those individual records.</p>
        <h6>Configuration and monitoring</h6>
        <p>Enable <code>openTelemetryEnabled=yes</code> and <code>journalEnabled=yes</code> in the Master's
            <code>[OperationalEvents]</code> configuration and restart the Master. Queue monitoring requires
            <code>getOperationalEventStatistics.sql</code> in its <code>conf/sql</code> directory.
            Missing or unreadable statistics appear as unavailable, never as an empty queue.</p>
        <p>Administrators can pause/resume native OpenTelemetry collection and delivery separately. Collection pause
            does not affect Splunk/file logging. Delivery pause leaves collection and persistence active, so the queue
            can grow; a batch already in flight may finish. These runtime settings are audited and reset on Master
            restart. Configure <code>journalWarningPendingCount</code>, <code>journalCriticalPendingCount</code>,
            <code>journalWarningOldestAgeMillis</code> and <code>journalCriticalOldestAgeMillis</code> in
            <code>[OperationalEvents]</code>; restart the Master to change thresholds.</p>
        <p>Refresh reads an owner-scoped database aggregate; the default interval is 30 seconds.
            Home and menu indicators use runtime health without scanning the queue.
            Control changes require Monitor and Master administration permissions and are recorded in the Master
            session audit log. This page cannot delete events, change configuration or force retries.</p>
    </div>
</div>
