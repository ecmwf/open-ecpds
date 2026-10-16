<%@ page contentType="text/html;charset=UTF-8"%>
<%-- Storage Lifecycle Policies configuration guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="spGuideOffcanvas"
     aria-labelledby="spGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="spGuideLabel">
      <i class="bi bi-hdd-stack-fill me-2 text-success"></i>Storage Lifecycle Policies &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>A policy decides how long data files stay on the Data Movers. It can only <strong>shorten</strong> the retention:
      a file is never kept beyond the transfer lifetime (<code>min(requeue window, expiry)</code>).
      <strong>Currently policies are only defined and evaluated: no file is removed yet.</strong></div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#spg-select" type="button"><i class="bi bi-list-ol me-1"></i>Selection</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#spg-criteria" type="button"><i class="bi bi-funnel me-1"></i>Criteria</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#spg-retention" type="button"><i class="bi bi-clock-history me-1"></i>Retention</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#spg-modes" type="button"><i class="bi bi-toggles me-1"></i>Modes</button></li>
    </ul>

    <div class="tab-content">

      <div class="tab-pane fade show active" id="spg-select" role="tabpanel">
        <ol class="small mb-3 ps-3">
          <li>A destination option <code>scheduler.storagePolicy = "&lt;name&gt;"</code> always wins.</li>
          <li>Otherwise the <strong>first active policy</strong>, by the order shown in the table, whose criteria all match.</li>
          <li>Otherwise the built-in <code>default</code> policy applies (<span class="badge text-bg-secondary">EXPIRY</span>: files are kept until the transfer expiry, nothing is removed early).</li>
        </ol>
        <p class="small text-muted mb-0">Use the arrows to change the order: put the most specific policies first. Use
        <em>Which policy applies?</em> at the bottom of the page to check the result for a destination and a file name.</p>
      </div>

      <div class="tab-pane fade" id="spg-criteria" role="tabpanel">
        <p class="small text-muted mb-2">All the criteria entered must match; an empty criterion matches anything.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Criterion</th><th>Content</th></tr></thead>
            <tbody>
              <tr><td>Destinations</td><td>Comma-separated names, with <code>*</code> and <code>?</code> wildcards (e.g. <code>ECMWF-*</code>).</td></tr>
              <tr><td>Destination types</td><td>One or more types (Ctrl/Cmd-click for several).</td></tr>
              <tr><td>Transfer groups</td><td>One or more transfer groups.</td></tr>
              <tr><td>File name pattern</td><td>Wildcards <code>*</code> and <code>?</code> matched against the file name (e.g. <code>*.grib2</code>). Prefix with <code>regex:</code> for a regular expression (e.g. <code>regex:.*\.grib2</code>).</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="tab-pane fade" id="spg-retention" role="tabpanel">
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Setting</th><th>Effect</th></tr></thead>
            <tbody>
              <tr><td>Requeue window</td><td>How long the original copy is kept so that end users can ask for a re-queue (default 7 days). Never beyond the transfer lifetime.</td></tr>
              <tr><td>Restart on requeue</td><td>When ticked, a re-queue restarts the window. Off by default.</td></tr>
              <tr><td>Drop replicas / after</td><td>Keep only the original copy, removing the replicas on the other Data Movers after the given delay.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">A file with a transfer that can still run is never removed.</p>
      </div>

      <div class="tab-pane fade" id="spg-modes" role="tabpanel">
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Mode</th><th>Effect</th></tr></thead>
            <tbody>
              <tr><td><span class="badge text-bg-warning">DRY_RUN</span></td><td>Evaluate and report only (recommended for a new policy).</td></tr>
              <tr><td><span class="badge text-bg-danger">ENFORCE</span></td><td>Apply the policy.</td></tr>
              <tr><td><span class="badge text-bg-secondary">DISABLED</span></td><td>Ignored when selecting a policy.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">An inactive policy is ignored whatever its mode.</p>
      </div>

    </div>
  </div>
</div>
