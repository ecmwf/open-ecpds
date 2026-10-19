<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%-- Network Weather configuration guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="nwGuideOffcanvas"
     aria-labelledby="nwGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="nwGuideLabel">
      <i class="bi bi-cloud-lightning-rain me-2 text-info"></i>Network Weather &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>Network Weather shows the recent delivery conditions between Data Movers (sources) and
      dissemination Hosts. Each feature is <strong>off by default</strong> and can be switched on or off at any
      time without restarting services. A path without measurements has <strong>no coverage</strong>; it is
      not reported as healthy.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#nwg-steps" type="button"><i class="bi bi-list-ol me-1"></i>Steps</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#nwg-paths" type="button"><i class="bi bi-signpost-split me-1"></i>Paths</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#nwg-auto" type="button"><i class="bi bi-magic me-1"></i>Automatic</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#nwg-probes" type="button"><i class="bi bi-broadcast me-1"></i>Probes</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#nwg-globe" type="button"><i class="bi bi-globe2 me-1"></i>Live Earth</button></li>
    </ul>

    <div class="tab-content">

      <div class="tab-pane fade show active" id="nwg-steps" role="tabpanel">
        <ol class="small mb-3 ps-3">
          <li>Choose the paths to follow: either a <strong>manual scope</strong> (fixed list) or
            <strong>automatic selection</strong> within approved candidates.</li>
          <li>Wait until the selected sources are shown as verified in <em>Source process registration</em>.</li>
          <li>Press <strong>Activate collection</strong>. Measurements appear within a few minutes.</li>
          <li>Optionally approve <strong>scheduled probes</strong> for some paths.</li>
          <li>Users can display the result on <em>Live Earth</em> with the weather icon.</li>
        </ol>
        <p class="small text-muted mb-0"><strong>Deactivate collection</strong> stops new measurements immediately on
        the Master and within one minute on the Data Movers. Transfers are never affected.</p>
      </div>

      <div class="tab-pane fade" id="nwg-paths" role="tabpanel">
        <p class="small mb-2">A path is a source, a dissemination Host and its transfer method. Use the builders:
        select a source and a Host, then press <strong>Add</strong>. The Host target and method are filled in from the
        Host configuration; duplicates are refused.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Field</th><th>Content</th></tr></thead>
            <tbody>
              <tr><td>Source</td><td>The Data Mover that runs the transfers. For a Continental Mover not listed,
                choose <em>Other source</em> and enter its name.</td></tr>
              <tr><td>Host</td><td>An active dissemination Host. Type in the filter box to narrow the list.</td></tr>
              <tr><td>Target / method</td><td>Taken from the Host; shown for information.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">Deployment limits: <c:out value="${nwMaxSelectedPaths}"/> selected paths across <c:out value="${nwMaxSources}"/> sources. A Host marked
        <span class="badge text-bg-warning">not active</span> is no longer an active dissemination Host and should be
        removed.</p>
      </div>

      <div class="tab-pane fade" id="nwg-auto" role="tabpanel">
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Setting</th><th>Effect</th></tr></thead>
            <tbody>
              <tr><td>Automatic selection</td><td>Once a day, chooses up to <c:out value="${nwMaxSelectedPaths}"/> paths among the candidates, favouring
                busy paths, then paths with recent errors, then a mix of other destinations.</td></tr>
              <tr><td>Historical discovery</td><td>Uses up to seven days of activity, volume and errors to rank already
                permitted candidates. It does not populate the candidate lists or require a successful transfer.</td></tr>
              <tr><td>Source / transfer group</td><td>Every active dissemination Host of the transfer group becomes
                a candidate for the source.</td></tr>
              <tr><td>Approved candidates</td><td>Additional individual paths (up to <c:out value="${nwMaxCandidates}"/>).</td></tr>
              <tr><td>Pinned</td><td>Always selected (up to <c:out value="${nwMaxSelectedPaths}"/>).</td></tr>
              <tr><td>Excluded</td><td>Never selected; exclusions win over pins.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small">Use <strong>Find candidates from successful transfers</strong> to search the last 1&ndash;30 days.
        Optionally filter by protocol. Choose a source to assign (history does not identify the source for each Host), review the matching active
        dissemination Hosts and add individual paths or source/group scopes. Group scopes also permit other Hosts in the
        group; only one group per source is supported. Already-approved paths are omitted from the preview;
        applying results also skips duplicates. Entries are added to the form, not saved automatically.</p>
        <p class="small text-muted mb-0">While automatic selection is on, the manual scope cannot be edited. Use
        <strong>Request reassessment</strong> to recompute the selection without waiting for the next day.</p>
      </div>

      <div class="tab-pane fade" id="nwg-probes" role="tabpanel">
        <p class="small mb-2">A probe is a light connectivity test run from the source to the Host. It never sends
        credentials or logs in.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Mode</th><th>Test</th><th>Minimum interval</th></tr></thead>
            <tbody>
              <tr><td><span class="badge text-bg-primary">TCP_CONNECT</span></td><td>Opens and closes a TCP connection to the port.</td><td>1 minute</td></tr>
              <tr><td><span class="badge text-bg-secondary">MTR_ICMP</span></td><td>Route trace with ICMP.</td><td>5 minutes</td></tr>
              <tr><td><span class="badge text-bg-secondary">MTR_TCP</span></td><td>Route trace with TCP to the port.</td><td>5 minutes</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">Choose the port actually used by the Host's transfer method (suggestions are
        offered). Probes run only while collection is active and the path is selected. MTR modes require
        <code>mtr</code> on the Data Mover.</p>
      </div>

      <div class="tab-pane fade" id="nwg-globe" role="tabpanel">
        <p class="small mb-2">On <em>Live Earth</em>, the <i class="bi bi-cloud-lightning-rain"></i> icon is shown only
        while collection is active. Click it to show or hide the weather overlay; the choice is remembered in the
        browser and the overlay is off by default.</p>
        <p class="small text-muted mb-0">When collection is deactivated, the icon and the overlay disappear for every
        user.</p>
      </div>

    </div>
  </div>
</div>
