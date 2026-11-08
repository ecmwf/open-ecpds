<%@ page contentType="text/html;charset=UTF-8"%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="journalGuide" aria-labelledby="journalGuideLabel" style="width:760px;max-width:95vw;">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <h6 class="offcanvas-title mb-0 fw-semibold" id="journalGuideLabel"><i class="bi bi-book me-2 text-info"></i>OpenTelemetry Journal Guide</h6>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-3" style="font-size:0.85rem;">
        <div class="alert alert-info">The database journal persists operational events before exporting them through OpenTelemetry.
            It is a delivery queue, not a permanent event archive or a transactional audit trail.</div>
        <h6>Reading the queue</h6>
        <p>The <strong>Health</strong> tab focuses on current queue health and the backlog trend.
            Expand <strong>Technical details</strong> for journal identity and persistence/delivery timestamps.
            Use <strong>Admission &amp; rejections</strong> for capacity limits and cumulative rejection counts.</p>
        <p><strong>Pending events</strong> includes records awaiting export, endpoint acknowledgement or database deletion.
            <strong>Oldest admission</strong> is when the oldest pending row entered the database, not its event timestamp.
            Configured warning and critical thresholds use either pending count or oldest age.</p>
        <p>The backlog chart uses samples collected by this browser and resets when the page is reloaded.
            A drain estimate appears only after four or more samples across at least two minutes show a healthy decrease
            of at least two events. It is an extrapolation from recent throughput, not a delivery guarantee.</p>
        <p>Only the active Master's <strong>journal owner</strong> is measured. Keep that owner stable and run only one
            active Master per owner. Disabled journaling does not delete pending rows, and changing the owner leaves
            the other owner's rows untouched. Runtime counters reset on Master restart; pending rows survive.</p>
        <h6>Bounded admission and drops</h6>
        <p>The Admission &amp; rejections tab shows current memory queue usage, cached database usage, and the actual enforced limits
            in events and bytes. The single-event ceiling is the smallest of <code>journalMaxEventBytes</code>,
            the configured memory byte limit and database byte limit. Sizes refer to serialized telemetry, including attributes and resource metadata,
            not source file or MQTT payload size. Collector limits are separate and are not discovered here.</p>
        <p>Rejection counts distinguish an oversized event, memory event-count pressure, memory byte pressure,
            database event-count pressure, database byte pressure and persistence/capacity failures.
            The last rejected event's serialized size and timestamp are shown when available; serialization
            failures have no measurable serialized size. Counters are cumulative since startup, not current
            queue sizes, and are not reset by discarding pending events. Earlier combined rejection messages
            cannot retrospectively distinguish these causes; updated diagnostics apply after Master restart.</p>
        <p>Event producers enqueue without database I/O. The background worker drops new events when the memory queue
            is full, database capacity cannot be established, or a per-owner limit is reached.
            Defaults in <code>[OperationalEvents]</code> are <code>journalMaxPendingEvents=50000</code> and
            <code>journalMaxPayloadBytes=134217728</code> (128 MiB serialized UTF-8 payload).
            The memory queue defaults to 4,096 events and 16 MiB, configurable with <code>journalMaxMemoryEvents</code>
            and <code>journalMaxMemoryBytes</code>. <code>journalMaxEventBytes</code> defaults to 65,536 bytes
            and cannot exceed 16,777,215 bytes (the MEDIUMTEXT column limit). Database packet limits may be lower.
            Values must be positive; event-count limits
            must be below 2,147,483,647. Restart the Master after changing these values.
            Raising memory limits increases potential heap usage and telemetry lost on a crash;
            memory-queued events can be lost on shutdown or a crash. Persisted rows are never purged to make room.</p>
        <p>Limits apply to new admissions, not existing oversized backlogs, other owners, index overhead or Galera replicas.
            They are not a physical database disk limit. Do not write/delete this owner's rows from another process
            while its Master is active. Dropped/unconfirmed admission counters reset on restart.
            Dropped events are not replayed; Splunk/file output is unchanged.</p>
        <h6>Persistence failures</h6>
        <p>A failed admission does not stop operational processing. The event is dropped (an ambiguous database response
            may nevertheless leave a persisted row) and
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
            <code>getOperationalEventAdmissionStatistics.sql</code> in its <code>conf/sql</code> directory.
            Missing or unreadable statistics appear as unavailable, never as an empty queue.</p>
        <p>Users with Operations access, including operators, can pause/resume native OpenTelemetry collection and delivery separately. Collection pause
            does not affect Splunk/file logging. Delivery pause leaves collection and persistence active, so the queue
            can grow; a batch already in flight may finish. These runtime settings are audited and reset on Master
            restart. Configure <code>journalWarningPendingCount</code>, <code>journalCriticalPendingCount</code>,
            <code>journalWarningOldestAgeMillis</code> and <code>journalCriticalOldestAgeMillis</code> in
            <code>[OperationalEvents]</code>; restart the Master to change thresholds.</p>
        <p>Refresh reads cached background statistics, at most one minute old; the default page interval is 30 seconds.
            The worker inspects at most the row limit plus one; an oversized backlog is unavailable, not an exact count.
            Home and menu indicators also use cached health without scanning the queue.
            Viewing and control changes require Operations access (monitoring and transfer history) on both the Monitor and Master. Changes are recorded in the Master
            session audit log. This page cannot change configuration or force retries.</p>
        <h6>Discarding pending events</h6>
        <p>In the Delivery tab, <strong>Discard pending events</strong> permanently removes this owner's database
            backlog and memory-queued admissions. Pause both collection and delivery first; an in-flight export
            must finish. The confirmation shows the last observed database count and journal owner.
            This is intentional telemetry loss, not an acknowledgement, and cannot be undone. Other owners,
            Splunk logs and transfer history are untouched. Successful discard clears active delivery-failure
            and backlog warnings; historical persistence/drop counters remain. Both pauses remain in effect.
            The action and discarded count are audited. The Master requires <code>discardOperationalEvents.sql</code>.</p>
    </div>
</div>
