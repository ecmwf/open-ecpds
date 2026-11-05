<style>
  #mqttActivity .mqtt-stat {background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:.75rem 1.25rem;}
  #mqttActivity .mqtt-stat-label {font-size:.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:.04em;}
  #mqttActivity .mqtt-value {font-size:1.15rem;font-weight:700;font-variant-numeric:tabular-nums;}
  #mqttActivity .mqtt-chart {height:255px;position:relative;}
  #mqttActivity .mqtt-dot {width:.55rem;height:.55rem;display:inline-block;background:var(--bs-success);border-radius:50%;box-shadow:0 0 0 4px rgba(25,135,84,.12);}
  #mqttActivity td {vertical-align:middle;font-size:.82rem;}
  #mqttActivity .mqtt-topic {max-width:22rem;overflow-wrap:anywhere;font-family:monospace;}
  #mqttActivity .mqtt-table {max-height:480px;overflow:auto;}
  #mqttActivity thead th {position:sticky;top:0;z-index:1;}
  #mqttDetails .mqtt-detail-value {overflow-wrap:anywhere;}
</style>

<div id="mqttActivity">
  <div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
       style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
    <i class="bi bi-broadcast text-primary flex-shrink-0"></i>
    <span><strong>MQTT Activity</strong> &mdash; destination event publications and broker processing outcomes</span>
    <button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
        data-bs-toggle="collapse" data-bs-target="#mqttInfoPanel"
        aria-expanded="false" aria-controls="mqttInfoPanel" aria-label="About this page" title="About this page">
      <i class="bi bi-info-circle"></i>
    </button>
  </div>

  <div class="collapse mb-3" id="mqttInfoPanel">
    <div class="ecpds-help-panel">
      <strong class="d-block mb-1">Destination event notifications</strong>
      <p class="mb-1">Follow notifications from a Data Portal upload or acquisition through the Master publication queue to each Data Mover's MQTT broker.</p>
      <ul class="mb-1 ps-3">
        <li><strong>Broker accepted is not subscriber delivery.</strong> QoS does not prove that a subscriber received or used a notification.</li>
        <li><strong>Mixed results</strong> means Movers returned different outcomes. Open a publication's details for each broker's result.</li>
        <li><strong>Unconfirmed</strong> messages may still have been accepted; no automatic retry is performed by this view.</li>
        <li><strong>History</strong> is bounded by the Master's configured entry, byte and age limits<span id="mqttLimits"></span>. It is not an audit log or the operational telemetry journal. Payload contents and connection credentials are never stored here.</li>
      </ul>
      <strong class="d-block mb-1 mt-2">Controls</strong>
      <ul class="mb-0 ps-3">
        <li><strong>Filters</strong> apply to the cards, charts and table together. Totals cover only retained activity matching your filters.</li>
        <li><strong>Pause updates</strong> freezes the automatic five-second refresh. Publication details show metadata, processing time and per-Mover outcomes.</li>
        <li><strong>No activity?</strong> Check <code>incoming.event</code> (or <code>acquisition.event</code>), <code>mqtt.publish</code> and the destination's payload configuration.</li>
      </ul>
    </div>
  </div>

  <div class="alert alert-warning d-none" id="mqttError" role="status"></div>
  <div class="alert alert-warning d-none" id="mqttPersistence" role="status"></div>

  <div class="d-flex align-items-center flex-wrap gap-2 mb-3">
    <span class="small text-body-secondary" id="mqttLive"><span class="mqtt-dot me-2"></span>Live</span>
    <button id="mqttPause" class="btn btn-sm btn-outline-secondary ms-auto" type="button" aria-pressed="false"><i class="bi bi-pause-fill"></i> Pause updates</button>
  </div>
  <form id="mqttFilters" class="mb-3">
    <div class="row g-2 align-items-end">
      <div class="col-6 col-lg-2"><label for="mqttMinutes" class="form-label small">Time window</label>
        <select id="mqttMinutes" class="form-select form-select-sm w-100"><option value="15">Last 15 minutes</option><option value="60" selected>Last hour</option><option value="1440">Last 24 hours</option><option value="0">All retained history</option></select>
      </div>
      <div class="col-6 col-lg-3"><label for="mqttDestination" class="form-label small">Destination</label>
        <select id="mqttDestination" class="form-select form-select-sm w-100"><option value="">All authorized destinations</option></select>
      </div>
      <div class="col-6 col-lg-3"><label for="mqttTopic" class="form-label small">Topic contains</label>
        <input id="mqttTopic" class="form-control form-control-sm w-100" maxlength="1024" placeholder="e.g. openecpds/demo/">
      </div>
      <div class="col-6 col-lg-2"><label for="mqttStatus" class="form-label small">Outcome</label>
        <select id="mqttStatus" class="form-select form-select-sm w-100">
          <option value="">All outcomes</option><option value="QUEUED">Queued</option><option value="PROCESSING">Processing</option>
          <option value="ACCEPTED">Broker accepted</option><option value="PARTIAL">Mixed broker results</option>
          <option value="FAILED">Failed</option><option value="UNCONFIRMED">Unconfirmed</option><option value="SKIPPED">Skipped</option>
        </select>
      </div>
      <div class="col-12 col-lg-2"><button type="submit" class="btn btn-sm btn-outline-primary w-100"><i class="bi bi-funnel me-1"></i>Apply filters</button></div>
    </div>
  </form>

  <div class="row row-cols-2 row-cols-lg-3 row-cols-xxl-6 g-3 mb-3" id="mqttStats"></div>
  <div class="d-flex flex-wrap gap-4 small text-body-secondary mb-3">
    <span>Last broker acceptance (UTC): <strong id="mqttLastAccepted">None recorded</strong></span>
    <span>Oldest observed pending: <strong id="mqttOldestPending">None recorded</strong></span>
  </div>
  <div class="row g-3 mb-3">
    <div class="col-lg-8"><div class="card border-0 shadow-sm h-100">
      <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-bar-chart-line text-primary"></i><span class="fw-semibold">Publication activity</span><span class="small text-body-secondary">UTC</span>
      </div>
      <div class="card-body">
      <p class="small text-body-secondary mb-2">Latest recorded outcome per publication, grouped by last update.</p>
      <div class="mqtt-chart"><canvas id="mqttTimeline" role="img" aria-label="Publication outcomes over time"></canvas></div>
      </div>
    </div></div>
    <div class="col-lg-4"><div class="card border-0 shadow-sm h-100">
      <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-pie-chart text-primary"></i><span class="fw-semibold">Outcome distribution</span>
      </div>
      <div class="card-body">
      <p class="small text-body-secondary mb-2">One outcome per publication, across all Movers.</p>
      <div class="mqtt-chart"><canvas id="mqttDistribution" role="img" aria-label="Distribution of publication outcomes"></canvas></div>
      </div>
    </div></div>
  </div>
  <div class="card border-0 shadow-sm">
    <div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
      <i class="bi bi-table text-primary"></i><span class="fw-semibold">Recent publications</span>
      <span class="small text-body-secondary ms-auto" id="mqttUpdated" role="status">Loading activity...</span>
    </div>
    <div class="card-body p-0">
    <div class="table-responsive mqtt-table" tabindex="0" role="region" aria-label="Recent MQTT publications">
      <table class="table table-sm table-striped table-hover table-bordered align-middle mb-0">
        <thead class="table-primary"><tr><th scope="col">Updated (UTC)</th><th scope="col">Destination / file</th><th scope="col">Topic</th><th scope="col">Outcome</th><th scope="col">Brokers accepted</th><th scope="col">Details</th></tr></thead>
        <tbody id="mqttRows"></tbody>
      </table>
    </div>
    </div>
    <div class="card-footer d-flex justify-content-between align-items-center flex-wrap gap-2 py-2" style="background:var(--bs-secondary-bg)">
      <span id="mqttPage" class="small text-body-secondary"></span>
      <div class="btn-group btn-group-sm"><button type="button" id="mqttPrevious" class="btn btn-outline-secondary" disabled>Previous</button><button type="button" id="mqttNext" class="btn btn-outline-secondary" disabled>Next</button></div>
    </div>
  </div>
  <p class="small text-body-secondary mt-3 mb-0"><i class="bi bi-info-circle me-1"></i>Broker acceptance does not confirm subscriber delivery. Figures cover retained activity matching your filters.</p>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="mqttDetails" aria-labelledby="mqttDetailsTitle" style="--bs-offcanvas-width:640px;">
  <div class="offcanvas-header"><h5 class="offcanvas-title" id="mqttDetailsTitle">Publication details</h5><button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button></div>
  <div class="offcanvas-body"><div id="mqttDetailContent"></div></div>
</div>
<script src="/assets/js/chart.umd.min.js"></script>
<script src="/assets/js/mqtt-activity.js"></script>
