<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<style>
#dnTabs .nav-link.active {
    color: var(--bs-white);
    background-color: var(--bs-primary);
    border-color: var(--bs-primary);
    font-weight: 600;
}
.dn-cell { width: 84px; cursor: pointer; text-align: center; user-select: none; }
.dn-cell.dn-status-0 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-0, #adb5bd) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-1 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-1, #20c997) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-2 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-2, #28a745) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-3 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-3, #fd7e14) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-4 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-4, #ffc107) 13%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-5 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-5, #dc3545) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-6 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-6, #6f42c1) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell.dn-status-7 { --bs-table-bg: color-mix(in srgb, var(--mon-product-status-7, #6f42c1) 10%, transparent); background-color: var(--bs-table-bg) !important; }
.dn-cell[class*="dn-status-"]:hover { filter: brightness(0.96); }
.dn-marker { display: inline-block; min-width: 1.25rem; padding: 0.1rem 0.3rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 700; line-height: 1.2; }
.dn-marker.definite { background: #dc3545; color: #fff; }
.dn-marker.possible { background: #ffc107; color: #000; }
.dn-before-dot { display: inline-block; width: 12px; height: 12px; border-radius: 50%; vertical-align: middle; }
.dn-cell.na { background: var(--bs-tertiary-bg); color: #aaa; cursor: default; }
.dn-matrix-wrap { min-width: 0; overflow: auto; }
#dnMatrix.dn-uniform { table-layout: fixed; width: max-content; min-width: 100%; }
#dnMatrix.dn-uniform th:first-child, #dnMatrix.dn-uniform td:first-child { width: 160px; min-width: 160px; max-width: 160px; }
#dnMatrix.dn-uniform td:first-child { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
#dnMatrix.dn-uniform th:nth-child(n+3), #dnMatrix.dn-uniform td:nth-child(n+3) { width: 44px; min-width: 44px; max-width: 44px; height: 34px; padding: 0.15rem; }
#dnMatrix th.dn-before, #dnMatrix td.dn-before { width: 44px; min-width: 44px; max-width: 44px; padding: 0.15rem; text-align: center; white-space: nowrap; }
#dnMatrix th.dn-before { position: relative; vertical-align: middle !important; }
#dnMatrix th.dn-before > i { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; line-height: 1; }
#dnMatrix .dn-before-ahead { color: #198754; }
#dnMatrix .dn-before-late { color: #dc3545; }
#dnMatrix .dn-before-now { color: #6c757d; }
#dnMatrix td.dt-empty { text-align: left; vertical-align: top; }
.dn-pill { cursor: pointer; }
.dn-status-filter-menu { min-width: 250px; }
.dn-new { background: #d1e7dd; }
#dnBody { font-family: monospace; font-size: 0.85rem; }
#dnBccOption .input-group { max-width: 32rem; }
#dnReason, #dnCustomReason { max-width: 32rem; }
.dn-eta-input-group { max-width: 22rem; }
#dnSubject.dn-edited, #dnBody.dn-edited, .rs-subj.dn-edited, .rs-body.dn-edited { background-color: rgba(220, 53, 69, 0.06); }
@media (min-width: 1200px) {
  #dnMatrixColumn, #dnNoticeColumn { display: flex; }
  #dnMatrixColumn > .card { flex: 1 1 auto; width: 100%; min-width: 0; }
  #dnNoticeColumn > .card { flex: 1 1 auto; }
  #dnMatrixColumn .card-body, #dnNoticeColumn .card-body { flex: 1 1 auto; }
  #dnMatrixColumn > .card > .card-body { display: flex; flex-direction: column; min-height: 0; }
  #dnMatrixColumn .dn-matrix-wrap { flex: 0 0 auto; min-height: 0; }
  #dnMatrixActions { margin-bottom: auto !important; }
}
</style>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
 style="background:rgba(108,117,125,0.06); border-left:4px solid #6c757d;">
 <i class="bi bi-bell text-secondary flex-shrink-0"></i>
 <span>Send one consistent notice for all the products and cycles affected by a delay, then inform of the resolution.</span>
 <a href="#" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" onclick="var el=document.getElementById('dnGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;" title="Open Delay Notification Guide" aria-label="Open Delay Notification Guide"><i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Delay Notification Guide</span></a>
</div>

<div id="dnAlert"></div>
<div id="dnPreferences" data-user="<c:out value="${dnPreferenceUser}"/>"></div>

<ul class="nav nav-tabs mb-3" id="dnTabs">
 <li class="nav-item"><button class="nav-link active" id="dnTabCompose" type="button" onclick="dnTab('compose')"><i class="bi bi-send me-1"></i><span id="dnTabComposeLbl">Send delay notification</span></button></li>
 <li class="nav-item"><button class="nav-link" id="dnTabSent" type="button" onclick="dnTab('sent')"><i class="bi bi-journal-text me-1"></i>Sent notices <span class="badge text-bg-secondary" id="dnSentCount">0</span></button></li>
 <li class="nav-item ms-auto align-self-center mt-2 mt-sm-0" id="dnLayoutControls">
  <div class="btn-group btn-group-sm" role="group" aria-label="Compose layout">
   <button type="button" class="btn btn-outline-secondary active" data-dn-layout="split" aria-label="Split view" aria-pressed="true" title="Show matrix and notice side by side"><i class="bi bi-columns-gap"></i></button>
   <button type="button" class="btn btn-outline-secondary" data-dn-layout="matrix" aria-label="Matrix only" aria-pressed="false" title="Show the matrix at full width"><i class="bi bi-layout-sidebar"></i></button>
   <button type="button" class="btn btn-outline-secondary" data-dn-layout="notice" aria-label="Notice only" aria-pressed="false" title="Show the notice at full width"><i class="bi bi-layout-sidebar-reverse"></i></button>
  </div>
 </li>
</ul>

<div id="dnCompose" class="row g-3">
 <div class="col-xl-7" id="dnMatrixColumn">
  <div class="card border-0 shadow-sm">
   <div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)"><i class="bi bi-grid-3x3-gap text-primary me-1"></i>Affected products and cycles</div>
   <div class="card-body">
    <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
     <div class="input-group input-group-sm flex-grow-1" style="min-width:0">
      <span class="input-group-text">Product name</span>
      <input type="text" id="dnFilter" class="form-control" placeholder="e.g. GENFO, *ERA5*, !*TEST*" oninput="dnFilterChanged()">
     </div>
     <div class="input-group input-group-sm" style="width:auto">
      <label class="input-group-text" for="dnCycleFilter">Cycle</label>
      <select id="dnCycleFilter" class="form-select" onchange="dnFilterChanged()"><option value="">All cycles</option></select>
     </div>
     <div class="dropdown flex-shrink-0 ms-auto" id="dnStatusFilter">
      <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown"
              data-bs-auto-close="outside" aria-expanded="false" title="Show products if any cycle matches the selected generation timings">
       <i class="bi bi-funnel me-1"></i><span id="dnStatusFilterLabel">All timings</span>
      </button>
      <div class="dropdown-menu dropdown-menu-end p-2 dn-status-filter-menu" aria-label="Filter by generation timing">
       <label class="form-check small mb-2 pb-2 border-bottom">
        <input class="form-check-input" type="checkbox" id="dnStatusAll" checked onchange="dnSetAllStatuses(this.checked)">
        <span class="form-check-label">All generation timings</span>
       </label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="0"><span class="form-check-label">No data / pending</span></label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="1"><span class="form-check-label">Received &gt;30m ahead</span></label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="2"><span class="form-check-label">Received 15-30m ahead</span></label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="3"><span class="form-check-label">Received 0-15m ahead</span></label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="4"><span class="form-check-label">On schedule / slightly late</span></label>
       <label class="form-check small mb-1"><input class="form-check-input dn-status-option" type="checkbox" value="5"><span class="form-check-label">Not received / overdue</span></label>
       <label class="form-check small"><input class="form-check-input dn-status-option" type="checkbox" value="other"><span class="form-check-label">Transitional / other</span></label>
      </div>
     </div>
    </div>
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
     <div class="d-flex flex-wrap align-items-center gap-1" id="dnModels"></div>
     <div class="form-check form-switch small mb-0 ms-auto" title="Give every cycle cell the same fixed width and height">
      <input class="form-check-input" type="checkbox" role="switch" id="dnUniformCells" onchange="dnSetUniformCells(this.checked)">
      <label class="form-check-label" for="dnUniformCells">Uniform cells</label>
     </div>
    </div>
    <div class="small text-muted mb-2">Click a cell to switch between <span class="badge text-bg-danger">D = Definite</span>, <span class="badge text-bg-warning">P = Possible</span> and cleared. Click a product name to cycle its whole row.</div>
    <div class="d-flex flex-wrap gap-2 small mb-2" aria-label="Generation timing legend">
     <span><i class="mon-dot mon-dot-0 me-1"></i>No data / pending</span>
     <span><i class="mon-dot mon-dot-1 me-1"></i>&gt;30m ahead</span>
     <span><i class="mon-dot mon-dot-2 me-1"></i>15-30m ahead</span>
     <span><i class="mon-dot mon-dot-3 me-1"></i>0-15m ahead</span>
     <span><i class="mon-dot mon-dot-4 me-1"></i>On schedule / slightly late</span>
     <span><i class="mon-dot mon-dot-5 me-1"></i>Overdue (&gt;60m)</span>
     <span><i class="mon-dot mon-dot-6 me-1"></i>Transitional</span>
    </div>
    <div class="dn-matrix-wrap"><table class="table table-sm table-bordered align-middle mb-0" id="dnMatrix"></table></div>
    <div id="dnMatrixActions" class="mt-2 d-flex flex-wrap align-items-start gap-2">
     <span class="small text-muted"><span id="dnSelCount">0</span> cell(s) selected</span>
     <div class="btn-group btn-group-sm ms-auto" role="group" aria-label="Select affected cells by generation timing">
      <button class="btn btn-outline-danger" id="dnMarkLateBtn" type="button" onclick="dnMarkByTiming('definite')" title="Mark all filtered on-schedule/slightly-late and overdue product/cycle cells as Definite">Mark late/overdue D</button>
      <button class="btn btn-outline-warning" id="dnMarkAheadBtn" type="button" onclick="dnMarkByTiming('possible')" title="Mark all filtered product/cycle cells received 0-15 minutes ahead as Possible">Mark 0-15m ahead P</button>
      <button class="btn btn-outline-secondary" id="dnClearBtn" type="button" onclick="dnClear()" title="Clear the selection of all filtered cells">Clear all</button>
     </div>
    </div>
   </div>
  </div>
 </div>
 <div class="col-xl-5" id="dnNoticeColumn">
  <div class="card border-0 shadow-sm">
   <div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)"><i class="bi bi-envelope text-primary me-1"></i>Notice <span id="dnUpdBadge" class="badge text-bg-info ms-1 d-none"></span></div>
   <div class="card-body">
    <div class="mb-2">
     <div class="d-flex align-items-center flex-wrap gap-2">
      <label class="form-label small mb-0">To</label>
      <span class="badge rounded-pill text-bg-primary dn-pill" id="dnToPill" onclick="dnToggleDest()" title="Show the destinations affected">(0) Destinations</span>
      <span class="badge rounded-pill text-bg-secondary" id="dnEmailPill">0 emails</span>
      <button class="btn btn-link btn-sm" id="dnCopyBccBtn" type="button" onclick="dnCopyBcc()" title="Copy BCC email addresses" aria-label="Copy BCC email addresses" disabled><i class="bi bi-clipboard"></i></button>
     </div>
     <div id="dnDestList" class="small border rounded p-2 mt-1 d-none" style="max-height:160px; overflow:auto"></div>
     <div class="form-check form-switch mt-1"><input class="form-check-input" type="checkbox" id="dnNonMon" onchange="dnRecipients()"><label class="form-check-label small" for="dnNonMon">Include destinations not showing in the monitor</label></div>
    </div>
    <div class="mb-2" id="dnBccOption">
     <div class="form-check"><input class="form-check-input" type="checkbox" id="dnBccOn" checked onchange="dnUpdateCopyBcc()"><label class="form-check-label small" for="dnBccOn">Bcc recipients (additional)</label></div>
     <div class="input-group input-group-sm"><input type="text" id="dnBcc" class="form-control" placeholder="a@example.org; b@example.org" oninput="dnValidateBcc(); dnUpdateCopyBcc()"><button class="btn btn-outline-secondary" type="button" onclick="dnSaveBcc()" title="Use as default Bcc"><i class="bi bi-save"></i></button></div>
     <div class="invalid-feedback" id="dnBccError">Enter valid email addresses separated by semicolons.</div>
    </div>
    <div class="mb-2" id="dnCcOption">
     <div class="form-check"><input class="form-check-input" type="checkbox" id="dnCcOn" checked><label class="form-check-label small" for="dnCcOn">Cc recipients</label></div>
     <div class="input-group input-group-sm"><input type="text" id="dnCc" class="form-control" placeholder="a@x.int; b@x.int" oninput="dnValidateCc()"><button class="btn btn-outline-secondary" type="button" onclick="dnSaveCc()" title="Use as default Cc"><i class="bi bi-save"></i></button></div>
     <div class="invalid-feedback" id="dnCcError">Enter valid email addresses separated by semicolons.</div>
    </div>
    <div class="row g-2 mb-2">
     <div class="col-12"><label class="form-label small mb-1">Reason</label>
      <div class="d-flex align-items-center gap-2">
      <select id="dnReason" class="form-select form-select-sm flex-fill" onchange="dnReasonChanged()">
       <option>Delay in model production</option><option>Dissemination system issue</option><option>Power / infrastructure outage</option><option>Data acquisition / observations delay</option><option>Scheduled maintenance overrun</option><option>Network / connectivity issue</option><option value="__custom__">Other (enter a reason)</option>
      </select>
      <input type="text" id="dnCustomReason" class="form-control form-control-sm flex-fill d-none" maxlength="200"
             placeholder="Enter the reason" oninput="dnAuto()">
      </div>
     </div>
     <div class="col-12"><label class="form-label small mb-1">ETA (UTC)</label><input type="hidden" id="dnEta">
      <div class="input-group input-group-sm dn-eta-input-group">
       <select id="dnEtaH" class="form-select" style="min-width:4.4rem; padding-left:.5rem; padding-right:1.6rem; background-position:right .4rem center" onchange="dnEtaChanged()" title="Hour (UTC)"></select>
       <span class="input-group-text">:</span>
       <select id="dnEtaM" class="form-select" style="min-width:4.4rem; padding-left:.5rem; padding-right:1.6rem; background-position:right .4rem center" onchange="dnEtaChanged()" title="Minute"></select>
       <button class="btn btn-outline-secondary" type="button" onclick="dnEtaNow()" title="Set the current time (UTC)"><i class="bi bi-clock"></i></button>
       <button class="btn btn-outline-secondary" type="button" onclick="dnEtaClear()" title="No ETA"><i class="bi bi-x-lg"></i></button>
      </div></div>
     <div class="w-100"></div>
     <div class="col-6"><label class="form-label small mb-1">Expected delay</label>
      <select id="dnDelay" class="form-select form-select-sm" onchange="dnAuto()"><option value="">-</option><option>~15 min</option><option>~30 min</option><option>~45 min</option><option>~90 min</option><option>Under investigation</option></select></div>
     <div class="col-12">
      <div class="form-check"><input class="form-check-input" type="checkbox" id="dnNoteOn" onchange="dnNoteToggle()"><label class="form-check-label small d-flex align-items-center gap-2" for="dnNoteOn">Add a note <span id="dnNoteMarkdownHint" class="d-inline-flex align-items-center gap-1 border-start ps-2 text-muted d-none" title="Use Markdown for bold text, bullet lists and links. Preview shows the formatted message.">Markdown enabled <i class="bi bi-info-circle"></i></span></label></div>
      <div id="dnNoteWrap" class="d-none"><label class="form-label small mb-1" for="dnNote">Note</label><textarea id="dnNote" class="form-control" rows="2" maxlength="500" oninput="dnAuto()"></textarea></div>
     </div>
    </div>
    <div class="mb-2"><label class="form-label small mb-1" for="dnSubject">Subject</label><textarea id="dnSubject" class="form-control" rows="2" oninput="this.value = this.value.replace(/[\r\n]+/g, ' '); dnEdited()"></textarea></div>
    <div class="mb-2">
     <div class="d-flex align-items-center justify-content-between mb-1">
      <div class="d-flex align-items-center gap-2">
       <label class="form-label small mb-0" for="dnBody">Message</label>
       <span id="dnMarkdownHint" class="small d-inline-flex align-items-center gap-1 border-start ps-2 text-muted d-none" title="Use Markdown for bold text, bullet lists and links. Preview shows the formatted message.">Markdown enabled <i class="bi bi-info-circle"></i></span>
      </div>
      <div class="form-check form-switch small mb-0" title="Replace sequences of multiple blank lines with one blank line">
       <input class="form-check-input" type="checkbox" role="switch" id="dnCompactEmptyLines" checked onchange="dnSetCompactEmptyLines(this.checked)">
       <label class="form-check-label" for="dnCompactEmptyLines">Compact empty lines</label>
      </div>
     </div>
     <textarea id="dnBody" class="form-control" rows="16" oninput="dnEdited()"></textarea>
    </div>
    <div class="d-flex gap-2 flex-wrap">
     <button class="btn btn-outline-secondary" type="button" onclick="dnEditedReset()"><i class="bi bi-arrow-clockwise me-1"></i>Regenerate</button>
     <button class="btn btn-outline-primary" type="button" id="dnPreviewBtn" onclick="dnPreview()"><i class="bi bi-eye me-1"></i>Preview</button>
     <button type="button" class="btn btn-danger" id="dnSendBtn" onclick="dnSend(false)"><i class="bi bi-box-arrow-up-right me-1"></i><span id="dnSendLbl">Send with Outlook</span></button>
     <button type="button" class="btn btn-outline-danger" id="dnDirectBtn" onclick="dnSend(true)"><i class="bi bi-send-check me-1"></i><span id="dnDirectLbl">Send directly</span></button>
     <button type="button" class="btn btn-outline-secondary d-none" id="dnCancelUpd" onclick="dnLeaveUpdate()">Cancel update</button>
    </div>
    <div id="dnNoSelectionWarning" class="alert alert-warning py-2 mt-2 mb-0 d-none" role="status">
     <i class="bi bi-exclamation-triangle me-1"></i>Select at least one product and cycle in the matrix before sending.
    </div>
    <div class="small text-muted mt-2">Choose Outlook to review and send the message there, then acknowledge it here, or send directly through the Master mail service and have it acknowledged automatically.</div>
   </div>
  </div>
 </div>
</div>

<div id="dnSent" class="d-none">
 <div class="card border-0 shadow-sm">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
   <i class="bi bi-journal-text text-primary"></i><span class="fw-semibold">Sent notices</span>
   <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <div class="input-group input-group-sm" style="width:auto">
     <span class="input-group-text"><i class="bi bi-search"></i></span>
     <input type="text" id="dnSentSearch" class="form-control" placeholder="Search notices..." style="min-width:180px">
    </div>
    <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
     <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
     <select id="dnSentPageLen" class="form-select form-select-sm" style="width:auto">
      <option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option><option value="250">250</option>
     </select>
    </div>
    <div class="dropdown">
     <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="dnSentColModeBtn" data-bs-toggle="dropdown" aria-expanded="false">
      <i class="bi bi-layout-three-columns me-1"></i>Auto
     </button>
     <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dnSentColModeBtn">
      <li><a class="dropdown-item" href="#" data-dn-sent-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
      <li><a class="dropdown-item" href="#" data-dn-sent-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
      <li><a class="dropdown-item" href="#" data-dn-sent-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides Reason and Products</small></a></li>
     </ul>
    </div>
   </div>
  </div>
  <div class="card-body p-0">
   <div class="table-responsive">
    <table id="dnSentTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
     <thead class="table-warning"><tr><th>Reference</th><th>Status</th><th>Created</th><th>Reason</th><th>Products</th><th>Recipients</th><th style="width:330px"></th></tr></thead>
     <tbody id="dnSentBody"></tbody>
    </table>
   </div>
  </div>
 <div class="card-footer text-center d-none" id="dnMore"><button class="btn btn-sm btn-outline-secondary" onclick="dnMoreNotices()">Load older notices</button></div>
 <div class="card-footer">
  <div class="d-flex flex-wrap align-items-end gap-2">
   <div><label class="form-label small mb-1" for="dnReportFrom">From</label><input type="date" class="form-control form-control-sm" id="dnReportFrom"></div>
   <div><label class="form-label small mb-1" for="dnReportTo">To</label><input type="date" class="form-control form-control-sm" id="dnReportTo"></div>
   <div><label class="form-label small mb-1" for="dnReportStatus">Notice status</label>
    <select class="form-select form-select-sm" id="dnReportStatus">
     <option value="all">All statuses</option><option value="active">Active</option><option value="partial">Partially resolved</option><option value="resolved">Resolved</option><option value="cancelled">Cancelled</option>
    </select>
   </div>
   <div class="d-flex flex-column gap-1 pb-1">
    <div class="form-check form-check-sm"><input class="form-check-input" type="checkbox" id="dnReportHistory"><label class="form-check-label small" for="dnReportHistory">Include message history</label></div>
    <div class="form-check form-check-sm"><input class="form-check-input" type="checkbox" id="dnReportAddresses"><label class="form-check-label small" for="dnReportAddresses">Include recipient addresses</label></div>
   </div>
   <span class="ms-auto" id="dnReportButtonWrap"><button class="btn btn-sm btn-primary" id="dnReportButton" type="button" onclick="dnGenerateReport()"><i class="bi bi-file-earmark-spreadsheet me-1"></i>Generate report</button></span>
  </div>
  <div class="small text-muted mt-2">The period is based on notice creation date. Recipient addresses are excluded unless explicitly selected.</div>
  <div id="dnReportResults" class="mt-3 d-none" aria-live="polite"></div>
 </div>
 </div>
</div>
<div class="modal fade" id="dnPreviewModal" tabindex="-1"><div class="modal-dialog modal-lg modal-dialog-scrollable"><div class="modal-content">
<div class="modal-header py-2"><h6 class="modal-title"><i class="bi bi-eye me-1"></i>Message preview</h6><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
<div class="modal-body"><div id="dnPreviewHead" class="small mb-2"></div><div id="dnPreviewHtml" class="border rounded p-3 bg-body-tertiary d-none" style="font-size:0.9rem"></div><pre id="dnPreviewBody" class="border rounded p-3 bg-body-tertiary" style="white-space:pre-wrap;font-family:inherit;font-size:0.9rem"></pre></div>
<div class="modal-footer py-2"><button type="button" class="btn btn-outline-secondary btn-sm me-auto d-none" id="dnPreviewToggle" onclick="dnPreviewToggle()"></button><button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button></div></div></div></div>
<div class="modal fade" id="dnReportModal" tabindex="-1" aria-labelledby="dnReportTitle" aria-hidden="true">
 <div class="modal-dialog modal-xl modal-dialog-scrollable"><div class="modal-content">
  <div class="modal-header py-2"><h6 class="modal-title" id="dnReportTitle"><i class="bi bi-file-earmark-spreadsheet me-1"></i>Delay notices report</h6><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
  <div class="modal-body"><div id="dnReportSummary" class="mb-3"></div><div class="table-responsive"><table id="dnReportTable" class="table table-sm table-bordered table-striped align-middle mb-0"></table></div></div>
  <div class="modal-footer py-2"><button type="button" class="btn btn-outline-primary btn-sm" onclick="dnDownloadReport()"><i class="bi bi-file-earmark-spreadsheet me-1"></i>Download CSV</button><button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button></div>
 </div></div>
</div>

<script>
(function() {
var BASE = '/do/monitoring/delaynotification/';
var S = { cycles: {}, minutesBeforeSchedule: {}, minutesBeforeSchedulePerCycle: {}, generationStatusPerProduct: {}, generationStatusPerCycle: {}, models: {}, notices: [], sel: {}, emails: [], dests: {}, bodyEdited: false, update: null, defaultCc: '', defaultBcc: '', allowNoticeCc: true, allowNoticeBcc: true, markdown: true, previewData: null, previewPlain: false, limit: 100, more: false, sentTable: null, reportBusy: false, reportData: null };
// A manually edited message is no longer regenerated automatically: flag it with a subtle red line.
(function() {
  var edited = false;
  Object.defineProperty(S, 'bodyEdited', {enumerable: true, get: function() { return edited; }, set: function(v) {
    edited = !!v;
    ['dnSubject', 'dnBody'].forEach(function(id) {
      var el = document.getElementById(id);
      if (el) { el.classList.toggle('dn-edited', edited); el.title = edited ? 'Edited manually: no longer updated automatically (use Regenerate)' : ''; }
    });
  }});
})();
var BCC_LIST = 'op_delay_ecmwf@lists.ecmwf.int', TO_ADDRESS = 'operators@ecmwf.int'; // replaced by the values of Product Messages
var MAX_URL = 6000;
var PREF_KEY = 'delayNotificationPreferences:' + $('dnPreferences').dataset.user;
var preferences = {};
try {
  preferences = JSON.parse(localStorage.getItem(PREF_KEY) || '{}');
  if (!preferences || typeof preferences !== 'object' || Array.isArray(preferences)) { preferences = {}; }
} catch (e) { preferences = {}; }

function $(id) { return document.getElementById(id); }
function localDate(date) {
  return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
}
function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function(c) { return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]; }); }
function generationStatusNumber(status) {
  var number = Number(status);
  return Number.isInteger(number) && number >= 0 && number <= 7 ? number : 0;
}
function beforeScheduleStatus(status, minutes) {
  var current = generationStatusNumber(status);
  if (minutes == null || current >= 6) { return current; }
  if (minutes >= 30) { return 1; }
  if (minutes >= 15) { return 2; }
  if (minutes >= 0) { return 3; }
  return minutes > -60 ? 4 : 5;
}
function savePreferences() {
  try { localStorage.setItem(PREF_KEY, JSON.stringify(preferences)); } catch (e) {}
}
function setPreference(key, value) {
  preferences[key] = value;
  savePreferences();
}
function post(action, payload) {
  return fetch(BASE + action, {method: 'POST', headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify(payload || {})}).then(function(r) { return r.json(); }).then(function(d) {
      if (!d.success) { throw new Error(d.error || 'Failed'); } return d; });
}
var alertTimer = null;
function alertMsg(msg, type) {
  type = type || 'danger';
  clearTimeout(alertTimer);
  $('dnAlert').innerHTML = '<div class="alert alert-' + type + ' alert-dismissible d-flex align-items-center py-2 pe-5">' + esc(msg) + '<button type="button" class="btn-close py-0 h-100 d-flex align-items-center" style="padding:0.5rem 1rem" data-bs-dismiss="alert"></button></div>';
  if (type !== 'danger') {
    alertTimer = setTimeout(function() { var a = $('dnAlert').querySelector('.alert'); if (a) { bootstrap.Alert.getOrCreateInstance(a).close(); } }, 5000);
  }
}
// ETA: hour and minute selectors, so that only valid HH:MM values (or none) can be entered.
(function() {
  function opts(n) { var h = '<option value="">--</option>'; for (var i = 0; i < n; i++) { var v = (i < 10 ? '0' : '') + i; h += '<option>' + v + '</option>'; } return h; }
  $('dnEtaH').innerHTML = opts(24); $('dnEtaM').innerHTML = opts(60);
})();
window.dnEtaChanged = function() {
  var h = $('dnEtaH').value, m = $('dnEtaM').value;
  if (h && !m) { $('dnEtaM').value = '00'; m = '00'; }
  if (!h) { $('dnEtaM').value = ''; m = ''; }
  $('dnEta').value = h ? h + ':' + m : '';
  dnAuto();
};
window.dnEtaNow = function() {
  var d = new Date(), p = function(n) { return (n < 10 ? '0' : '') + n; };
  $('dnEtaH').value = p(d.getUTCHours()); $('dnEtaM').value = p(d.getUTCMinutes());
  dnEtaChanged();
};
window.dnEtaClear = function() { $('dnEtaH').value = ''; dnEtaChanged(); };
function setEta(v) {
  var x = /^([01]?\d|2[0-3]):([0-5]\d)$/.exec((v || '').trim());
  $('dnEtaH').value = x ? (x[1].length < 2 ? '0' : '') + x[1] : ''; $('dnEtaM').value = x ? x[2] : '';
  $('dnEta').value = x ? $('dnEtaH').value + ':' + x[2] : '';
}
function fail(e) { alertMsg(e.message || e); }
function reasonValue() {
  return $('dnReason').value === '__custom__' ? $('dnCustomReason').value.trim() : $('dnReason').value;
}
window.dnReasonChanged = function() {
  var custom = $('dnReason').value === '__custom__';
  $('dnCustomReason').classList.toggle('d-none', !custom);
  if (custom) { $('dnCustomReason').focus(); }
  dnAuto();
};

window.dnTab = function(t) {
  $('dnCompose').classList.toggle('d-none', t !== 'compose');
  $('dnSent').classList.toggle('d-none', t !== 'sent');
  $('dnLayoutControls').classList.toggle('d-none', t !== 'compose');
  $('dnTabCompose').classList.toggle('active', t === 'compose');
  $('dnTabSent').classList.toggle('active', t === 'sent');
  if (t === 'sent' && S.sentTable) {
    setTimeout(function() { S.sentTable.columns.adjust().draw(false); }, 0);
  }
};
window.dnSetLayout = function(layout) {
  if (['split', 'matrix', 'notice'].indexOf(layout) < 0) { layout = 'split'; }
  var matrix = $('dnMatrixColumn'), notice = $('dnNoticeColumn');
  matrix.classList.remove('col-xl-7', 'col-xl-12', 'd-none');
  notice.classList.remove('col-xl-5', 'col-xl-12', 'd-none');
  if (layout === 'matrix') {
    matrix.classList.add('col-xl-12');
    notice.classList.add('d-none');
  } else if (layout === 'notice') {
    matrix.classList.add('d-none');
    notice.classList.add('col-xl-12');
  } else {
    matrix.classList.add('col-xl-7');
    notice.classList.add('col-xl-5');
  }
  Array.prototype.forEach.call(document.querySelectorAll('[data-dn-layout]'), function(button) {
    var selected = button.dataset.dnLayout === layout;
    button.classList.toggle('active', selected);
    button.setAttribute('aria-pressed', String(selected));
  });
  setPreference('layout', layout);
};
Array.prototype.forEach.call(document.querySelectorAll('[data-dn-layout]'), function(button) {
  button.onclick = function() { dnSetLayout(button.dataset.dnLayout); };
});

// Same syntax as the product-name filter of /do/monitoring: comma-separated, * and ? wildcards, ! or - to exclude.
function matches(filter, name) {
  var tokens = filter.split(',').map(function(t) { return t.trim(); }).filter(Boolean);
  if (!tokens.length) { return true; }
  var inc = [], exc = [];
  tokens.forEach(function(t) {
    var neg = /^(!|-|no-)/.test(t);
    var p = t.replace(/^(!|-|no-)/, '');
    var re = new RegExp('^' + p.replace(/[.+^\$\{\}()|[\]\\]/g, '\\$&').replace(/\*/g, '.*').replace(/\?/g, '.') + '$', 'i');
    (neg ? exc : inc).push(re);
  });
  return (!inc.length || inc.some(function(r) { return r.test(name); })) && !exc.some(function(r) { return r.test(name); });
}

var activeModels = {};
var preferencesRestored = false;
function modelOf(product) {
  for (var m in S.models) { if (S.models[m].some(function(p) { return p.toLowerCase() === product.toLowerCase(); })) { return m; } }
  return '';
}
function renderModels() {
  var html = '', names = Object.keys(S.models);
  names.concat(['']).forEach(function(m) {
    var label = m || 'Unassigned';
    var on = activeModels[m];
    html += '<button type="button" class="btn btn-sm ' + (on ? 'btn-primary' : 'btn-outline-primary') + ' me-1 mb-1" data-m="' + esc(m) + '" aria-pressed="' + String(!!on) + '">' + esc(label) + '</button>';
  });
  $('dnModels').innerHTML = html;
  Array.prototype.forEach.call($('dnModels').querySelectorAll('button'), function(b) {
    b.onclick = function() {
      var model = b.dataset.m;
      var on = activeModels[model] = !activeModels[model];
      b.classList.toggle('btn-primary', on);
      b.classList.toggle('btn-outline-primary', !on);
      b.setAttribute('aria-pressed', String(on));
      if (!b.matches(':focus-visible')) { b.blur(); }
      setPreference('models', Object.keys(activeModels).filter(function(m) { return activeModels[m]; }));
      dnFilterChanged();
    };
  });
}

function restorePreferences() {
  var validModels = Object.keys(S.models).concat(['']);
  if (Array.isArray(preferences.models)) {
    preferences.models.forEach(function(model) {
      if (validModels.indexOf(model) >= 0) { activeModels[model] = true; }
    });
  }
  var validStatuses = ['0', '1', '2', '3', '4', '5', 'other'];
  var savedStatuses = Array.isArray(preferences.statusFilters)
    ? preferences.statusFilters
    : (preferences.statusFilter && preferences.statusFilter !== 'all' ? [preferences.statusFilter] : []);
  selectedStatuses = savedStatuses.filter(function(status, index) {
    return validStatuses.indexOf(status) >= 0 && savedStatuses.indexOf(status) === index;
  });
  renderStatusFilter();
  $('dnUniformCells').checked = preferences.uniformCells === true;
  dnSetUniformCells($('dnUniformCells').checked);
  $('dnCompactEmptyLines').checked = preferences.compactEmptyLines !== false;
  dnSetCompactEmptyLines($('dnCompactEmptyLines').checked);
  dnSetLayout(preferences.layout || 'split');
}

var selectedStatuses = [];
function renderStatusFilter() {
  Array.prototype.forEach.call(document.querySelectorAll('.dn-status-option'), function(option) {
    option.checked = selectedStatuses.indexOf(option.value) >= 0;
  });
  $('dnStatusAll').checked = selectedStatuses.length === 0;
  $('dnStatusFilterLabel').textContent = selectedStatuses.length
    ? selectedStatuses.length + ' timing' + (selectedStatuses.length === 1 ? '' : 's') : 'All timings';
}
window.dnSetAllStatuses = function(all) {
  selectedStatuses = all ? [] : selectedStatuses;
  setPreference('statusFilters', selectedStatuses.slice());
  renderStatusFilter();
  dnFilterChanged();
};
Array.prototype.forEach.call(document.querySelectorAll('.dn-status-option'), function(option) {
  option.addEventListener('change', function() {
    if (option.checked && selectedStatuses.indexOf(option.value) < 0) {
      selectedStatuses.push(option.value);
    } else if (!option.checked) {
      selectedStatuses = selectedStatuses.filter(function(status) { return status !== option.value; });
    }
    setPreference('statusFilters', selectedStatuses.slice());
    renderStatusFilter();
    dnFilterChanged();
  });
});

function statusMatchesFilter(status) {
  if (!selectedStatuses.length) { return true; }
  return selectedStatuses.some(function(selected) {
    return selected === 'other' ? status >= 6 : status === Number(selected);
  });
}

function visibleProducts() {
  var any = Object.keys(activeModels).some(function(k) { return activeModels[k]; });
  return Object.keys(S.cycles).filter(function(p) {
    var hasMatchingCycle = visibleCycles(p).some(function(cycle) {
      var key = p + '@' + cycle;
      var status = beforeScheduleStatus(S.generationStatusPerCycle[key], S.minutesBeforeSchedulePerCycle[key]);
      return statusMatchesFilter(status);
    });
    return matches($('dnFilter').value, p) && (!any || activeModels[modelOf(p)]) && hasMatchingCycle;
  });
}
function visibleCycles(product) {
  var cycle = $('dnCycleFilter').value;
  return S.cycles[product].filter(function(time) { return !cycle || cycle === time; });
}
function allCycles(products) {
  var set = {};
  products.forEach(function(p) { visibleCycles(p).forEach(function(t) { set[t] = 1; }); });
  return Object.keys(set).sort();
}
window.dnSetUniformCells = function(uniform) {
  $('dnMatrix').classList.toggle('dn-uniform', uniform);
  setPreference('uniformCells', uniform);
};
function compactEmptyLines(text) {
  return text.replace(/\r\n?/g, '\n').replace(/\n(?:[ \t]*\n)+/g, '\n\n');
}
window.dnSetCompactEmptyLines = function(compact) {
  setPreference('compactEmptyLines', compact);
  if (compact && $('dnBody')) {
    $('dnBody').value = compactEmptyLines($('dnBody').value);
  }
};
window.dnRenderMatrix = function() {
  var products = visibleProducts(), cycles = allCycles(products);
  var h = '<thead><tr><th>Product</th><th class="dn-before" title="Before: shortest time remaining across all cycles" aria-label="Before: shortest time remaining across all cycles"><i class="bi bi-clock-history"></i></th>' + cycles.map(function(c) { return '<th class="text-center">' + esc(c) + '</th>'; }).join('') + '</tr></thead><tbody>';
  products.forEach(function(p) {
    var description = S.descriptions[p] || '';
    h += '<tr><td class="fw-semibold" style="cursor:pointer" data-row="' + esc(p) + '"'
      + (description ? ' title="' + esc(description) + '"' : '') + '>' + esc(p) + '</td>';
    var before = S.minutesBeforeSchedule[p];
    var productStatus = S.generationStatusPerProduct[p];
    var beforeTitle = before == null ? 'No schedule available'
      : before < 0 ? Math.abs(before) + 'm overdue' : before + 'm before schedule';
    h += '<td class="dn-before"><span class="dn-before-dot mon-dot mon-dot-'
      + beforeScheduleStatus(productStatus, before) + '" title="' + esc(beforeTitle)
      + '" aria-label="' + esc(beforeTitle) + '"></span></td>';
    cycles.forEach(function(c) {
      if (S.cycles[p].indexOf(c) < 0) { h += '<td class="dn-cell na">&middot;</td>'; return; }
      var l = S.sel[p + '@' + c] || '';
      var cycleBefore = S.minutesBeforeSchedulePerCycle[p + '@' + c];
      var cycleStatus = S.generationStatusPerCycle[p + '@' + c];
      var cycleTitle = cycleBefore == null ? 'No schedule available'
        : cycleBefore < 0 ? Math.abs(cycleBefore) + 'm overdue' : cycleBefore + 'm before schedule';
      var marker = l === 'definite' ? '<span class="dn-marker definite" title="Definite" aria-label="Definite">D</span>'
        : l === 'possible' ? '<span class="dn-marker possible" title="Possible" aria-label="Possible">P</span>' : '';
      h += '<td class="dn-cell dn-status-' + beforeScheduleStatus(cycleStatus, cycleBefore)
        + '" data-p="' + esc(p) + '" data-t="' + esc(c) + '" title="' + esc(cycleTitle) + '">' + marker + '</td>';
    });
    h += '</tr>';
  });
  if (!products.length) {
    h += '<tr><td colspan="' + (cycles.length + 2) + '" class="dt-empty">No matching records found.</td></tr>';
  }
  $('dnMarkLateBtn').disabled = products.length === 0;
  $('dnMarkAheadBtn').disabled = products.length === 0;
  $('dnClearBtn').disabled = !visibleKeys().some(function(k) { return S.sel[k]; });
  $('dnMatrix').innerHTML = h + '</tbody>';
  $('dnMatrix').classList.toggle('dn-uniform', $('dnUniformCells').checked);
  Array.prototype.forEach.call($('dnMatrix').querySelectorAll('td[data-p]'), function(td) {
    td.onclick = function() { setCell(td.dataset.p, td.dataset.t, next(S.sel[td.dataset.p + '@' + td.dataset.t])); };
  });
  Array.prototype.forEach.call($('dnMatrix').querySelectorAll('td[data-row]'), function(td) {
    td.onclick = function() {
      var p = td.dataset.row, cycles = visibleCycles(p), n = next(S.sel[p + '@' + cycles[0]]);
      cycles.forEach(function(t) { setCell(p, t, n, true); });
      afterSelection();
    };
  });
  $('dnSelCount').textContent = cellList().length;
};
function next(l) { return !l ? 'definite' : l === 'definite' ? 'possible' : ''; }
function setCell(p, t, l, quiet) {
  if (l) { S.sel[p + '@' + t] = l; } else { delete S.sel[p + '@' + t]; }
  if (!quiet) { afterSelection(); }
}
function afterSelection() { dnRenderMatrix(); dnRecipients(); dnAuto(); }
window.dnClear = function() {
  visibleKeys().forEach(function(key) { delete S.sel[key]; });
  afterSelection();
};
var filterTimer;
window.dnFilterChanged = function() {
  dnRenderMatrix();
  clearTimeout(filterTimer);
  filterTimer = setTimeout(function() { dnRecipients(); dnAuto(); }, 250);
};
window.dnMarkByTiming = function(level) {
  if (level !== 'definite' && level !== 'possible') { return; }
  visibleProducts().forEach(function(product) {
    visibleCycles(product).forEach(function(cycle) {
      var key = product + '@' + cycle;
      var status = beforeScheduleStatus(S.generationStatusPerCycle[key], S.minutesBeforeSchedulePerCycle[key]);
      if ((level === 'definite' && (status === 4 || status === 5))
          || (level === 'possible' && status === 3)) {
        S.sel[key] = level;
      }
    });
  });
  afterSelection();
};

// Keys of the cells shown by the current filters (product name, models, timing status).
function visibleKeys() {
  var keys = [];
  visibleProducts().forEach(function(p) { visibleCycles(p).forEach(function(t) { keys.push(p + '@' + t); }); });
  return keys;
}
// Only the selected cells matching the current filters; an update keeps its own cells regardless of filters.
function cellList() {
  var allowed = null;
  if (!S.update) { allowed = {}; visibleKeys().forEach(function(k) { allowed[k] = 1; }); }
  return Object.keys(S.sel).filter(function(k) { return !allowed || allowed[k]; }).sort().map(function(k) { var i = k.lastIndexOf('@'); return {product: k.substring(0, i), time: k.substring(i + 1), level: S.sel[k]}; });
}
// Same rule as {{CYCLES}} on the monitoring pages: 00,01,02,03,06 becomes 00-03,06 (when enabled in Product Messages).
function compactTimes(times, sep) {
  var list = times.slice().sort(), join = sep || ', ';
  if (S.compactCycles === false || !list.every(function(t) { return /^\d+$/.test(t); })) { return list.join(join); }
  var out = [], i = 0;
  while (i < list.length) {
    var j = i;
    while (j + 1 < list.length && +list[j + 1] === +list[j] + 1) { j++; }
    out.push(j > i ? list[i] + '-' + list[j] : list[i]);
    i = j + 1;
  }
  return out.join(join);
}
function group(cells, level) {
  var per = {};
  cells.filter(function(c) { return c.level === level; }).forEach(function(c) { (per[c.product] = per[c.product] || []).push(c.time); });
  return Object.keys(per).sort().map(function(p) { return '  - ' + p + ' (cycles: ' + compactTimes(per[p].sort()) + ')'; });
}
function describeProducts(cells) {
  var names = {}; cells.forEach(function(c) { names[c.product] = 1; });
  return Object.keys(names).sort();
}
function subjectCycles(cells) {
  var times = {}; cells.forEach(function(c) { times[c.time] = 1; });
  var cycles = compactTimes(Object.keys(times), ',');
  return cycles ? ' (cycles: ' + cycles + 'Z)' : '';
}

function fillTemplate(tpl, products, cells, details) {
  details = details || {};
  var per = {}; cells.forEach(function(c) { (per[c.product] = per[c.product] || []).push(c.time); });
  var all = {}; cells.forEach(function(c) { all[c.time] = 1; });
  var cycles = compactTimes(Object.keys(all), ',');
  var names = Object.keys(per).sort();
  var same = names.every(function(p) { return compactTimes(per[p], ',') === cycles; });
  // Nothing selected yet: keep visible stand-ins so the operator sees where the values will go.
  var productText = products.length ? products.join(', ') : '[products]';
  if (!cycles) { cycles = '[cycles]'; }
  if (!same) {
    // The products do not share the same cycles: name the cycles of each product, e.g. "GENFO (00,12Z) and GWAVE (06,18Z)".
    var parts = names.map(function(p) { return p + ' (' + compactTimes(per[p], ',') + 'Z)'; });
    productText = parts.length > 1 ? parts.slice(0, -1).join(', ') + ' and ' + parts[parts.length - 1] : parts[0];
    tpl = tpl.replace(/(the )?\{\{CYCLES?\}\}Z? cycles?/gi, 'their respective cycles');
  }
  var desc = products.map(function(p) { return S.descriptions && S.descriptions[p] ? '  - ' + p + ': ' + S.descriptions[p] : ''; }).filter(Boolean).join('\n');
  var body = tpl.split('{{PRODUCTS}}').join(productText).split('{{PRODUCT}}').join(productText).split('{{CYCLES}}').join(cycles).split('{{CYCLE}}').join(cycles)
    .split('{{DESCRIPTION}}').join(desc)
    .split('{{REASON}}').join(details.reason || '')
    .split('{{ETA}}').join(details.eta ? details.eta + ' UTC' : '')
    .split('{{EXPECTED_DELAY}}').join(details.expectedDelay || '')
    .split('{{NOTE}}').join(details.note || '');
  return $('dnCompactEmptyLines').checked ? compactEmptyLines(body) : body;
}
function noticeDetails() {
  return {reason: reasonValue(), eta: $('dnEta').value.trim(), expectedDelay: $('dnDelay').value, note: $('dnNoteOn').checked ? $('dnNote').value.trim() : ''};
}
function affectedBlock(cells, details, includeDetails) {
  var lines = [];
  var d = group(cells, 'definite'), p = group(cells, 'possible');
  if (d.length) { lines.push('Products and cycles affected:'); lines = lines.concat(d, ''); }
  if (p.length) { lines.push('Products and cycles that might be affected:'); lines = lines.concat(p, ''); }
  if (includeDetails.reason && details.reason) { lines.push('Reason: ' + details.reason); }
  if (includeDetails.expectedDelay && details.expectedDelay) { lines.push('Expected delay: ' + details.expectedDelay); }
  if (includeDetails.eta && details.eta) { lines.push('Estimated time of availability: ' + details.eta + ' UTC'); }
  if (includeDetails.note && details.note) { lines.push('', details.note); }
  return lines.join('\n').replace(/\s+$/, '');
}
function generate() {
  var cells = cellList();
  var products = describeProducts(cells);
  var subj = 'Delay notification' + (products.length ? ': ' : '') + (products.length > 4 ? products.slice(0, 4).join(', ') + ' and ' + (products.length - 4) + ' more' : products.join(', '));
  subj += subjectCycles(cells);
  var tpl = S.delayTemplate || 'Dear users,\n\nWe are currently experiencing a delay.\n\nBest regards,\nECMWF Operations';
  var details = noticeDetails();
  var includeDetails = {
    reason: tpl.indexOf('{{REASON}}') < 0,
    eta: tpl.indexOf('{{ETA}}') < 0,
    expectedDelay: tpl.indexOf('{{EXPECTED_DELAY}}') < 0,
    note: tpl.indexOf('{{NOTE}}') < 0
  };
  var block = affectedBlock(cells, details, includeDetails);
  if (tpl.indexOf('{{AFFECTED}}') >= 0) {
    tpl = tpl.split('{{AFFECTED}}').join(block);
  } else if (block) {
    // The managed message has no {{AFFECTED}} placeholder: insert the details after its first paragraph.
    var k = tpl.search(/\n\s*\n/);
    tpl = k < 0 ? tpl + '\n\n' + block : tpl.substring(0, k) + '\n\n' + block + tpl.substring(k);
  }
  return {subject: subj, body: fillTemplate(tpl, products, cells, details)};
}
window.dnAuto = function() {
  $('dnNoSelectionWarning').classList.toggle('d-none', cellList().length > 0);
  if ($('dnCompactEmptyLines').checked) {
    $('dnBody').value = compactEmptyLines($('dnBody').value);
  }
  if (S.bodyEdited) { return; }
  var g = generate(); $('dnSubject').value = g.subject; $('dnBody').value = g.body;
};
window.dnNoteToggle = function() {
  $('dnNoteWrap').classList.toggle('d-none', !$('dnNoteOn').checked);
  dnAuto();
};
window.dnEdited = function() { S.bodyEdited = true; };
window.dnEditedReset = function() { S.bodyEdited = false; dnAuto(); };

window.dnRecipients = function() {
  var cells = cellList();
  if (!cells.length) { S.emails = []; S.dests = {}; renderRecipients(); return; }
  post('recipients', {cells: cells, includeNonMonitored: $('dnNonMon').checked}).then(function(d) {
    S.emails = d.emails; S.dests = d.destinations; renderRecipients();
  }).catch(fail);
};
function renderRecipients() {
  var names = Object.keys(S.dests);
  var known = S.update ? S.update.emails.map(function(e) { return e.toLowerCase(); }) : null;
  $('dnToPill').textContent = '(' + names.length + ') Destinations';
  $('dnEmailPill').textContent = S.emails.length + ' emails';
  dnUpdateCopyBcc();
  $('dnDestList').innerHTML = names.length ? names.map(function(n) {
    var e = S.dests[n];
    var isNew = known && e && e.split(/[,;]/).some(function(x) { return x.trim() && known.indexOf(x.trim().toLowerCase()) < 0; });
    return '<div class="' + (isNew ? 'dn-new' : '') + '"><strong>' + esc(n) + '</strong> ' + (e ? '<span class="text-muted">' + esc(e) + '</span>' : '<span class="text-danger">no contact</span>') + (isNew ? ' <span class="badge text-bg-success">new</span>' : '') + '</div>';
  }).join('') : '<span class="text-muted">None</span>';
}
window.dnToggleDest = function() { $('dnDestList').classList.toggle('d-none'); };
function copy(text) {
  if (navigator.clipboard && window.isSecureContext) { return navigator.clipboard.writeText(text); }
  var t = document.createElement('textarea'); t.value = text; document.body.appendChild(t); t.select(); document.execCommand('copy'); t.remove();
  return Promise.resolve();
}
window.dnCopyBcc = function() {
  var addresses = S.emails.concat($('dnBccOn').checked ? addressList($('dnBcc').value) : []);
  copy(addresses.join(',')).then(function() { alertMsg(addresses.length + ' email(s) copied', 'success'); });
};
window.dnUpdateCopyBcc = function() {
  $('dnCopyBccBtn').disabled = S.emails.length === 0
    && (!$('dnBccOn').checked || addressList($('dnBcc').value).length === 0);
};
function ccIsValid() {
  var value = $('dnCc').value.trim();
  return !value || value.split(';').every(function(address) {
    address = address.trim();
    return /^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$/i.test(address);
  });
}
function bccIsValid() {
  var value = $('dnBcc').value.trim();
  return !value || value.split(';').every(function(address) {
    address = address.trim();
    return /^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$/i.test(address);
  });
}
window.dnValidateCc = function() {
  var valid = ccIsValid();
  $('dnCc').classList.toggle('is-invalid', !valid);
  $('dnCcError').style.display = valid ? 'none' : 'block';
  return valid;
};
window.dnValidateBcc = function() {
  var valid = bccIsValid();
  $('dnBcc').classList.toggle('is-invalid', !valid);
  $('dnBccError').style.display = valid ? 'none' : 'block';
  return valid;
};
window.dnSaveCc = function() {
  if (!dnValidateCc()) { $('dnCc').focus(); return; }
  post('defaultcc', {cc: $('dnCc').value}).then(function() { alertMsg('Default Cc saved', 'success'); }).catch(fail);
};
window.dnSaveBcc = function() {
  if (!dnValidateBcc()) { $('dnBcc').focus(); return; }
  post('defaultbcc', {bcc: $('dnBcc').value}).then(function() { alertMsg('Default Bcc saved', 'success'); }).catch(fail);
};
function addressList(value) {
  return (value || '').split(';').map(function(x) { return x.trim(); }).filter(Boolean);
}

function openOutlookWindow() {
  var popup = window.open('about:blank', '_blank');
  if (!popup) {
    alertMsg('Your browser blocked the Outlook window. Allow pop-ups for this site and try again.', 'warning');
    return null;
  }
  popup.document.title = 'Preparing Outlook message';
  popup.document.body.textContent = 'Preparing Outlook message…';
  popup.opener = null;
  return popup;
}
function closeOutlookWindow(popup) {
  if (popup && !popup.closed) { popup.close(); }
}
function outlook(subject, body, emails, cells, ccText, bccText, markdown, popup) {
  if (markdown) {
    // Outlook links can only carry plain text: use the plain-text rendering of the Markdown message.
    post('markdown', {markdown: body}).then(function(d) {
      outlook(subject, d.text, emails, cells, ccText, bccText, false, popup);
    }).catch(function(e) { closeOutlookWindow(popup); fail(e); });
    return;
  }
  var cc = (ccText || '').split(/[;,]/).map(function(x) { return x.trim(); }).filter(Boolean).join(',');
  var additionalBcc = addressList(bccText);
  function url(bcc) {
    return 'https://outlook.office365.com/mail/deeplink/compose?subject=' + encodeURIComponent(subject) + '&body=' + encodeURIComponent(body)
      + '&from=' + encodeURIComponent(TO_ADDRESS) + '&to=' + encodeURIComponent(TO_ADDRESS) + (cc ? '&cc=' + encodeURIComponent(cc) : '') + '&bcc=' + encodeURIComponent(bcc.join(','));
  }
  var u = url([BCC_LIST].concat(emails, additionalBcc));
  if (u.length > MAX_URL) {
    // Too long for a link: use the distribution lists of each product/cycle, and offer the explicit emails to copy.
    var lists = {}; cells.forEach(function(c) { lists['ecpds-product-' + c.time + '-' + c.product + '@ecmwf.int'] = 1; });
    u = url([BCC_LIST].concat(Object.keys(lists), additionalBcc));
    alertMsg('The recipient list is too long for an Outlook link: the product distribution lists were used in BCC. Use "Copy BCC emails" for the explicit list.', 'warning');
  }
  if (popup && !popup.closed) {
    popup.location.replace(u);
  } else {
    window.open(u, '_blank', 'noopener');
  }
}

function payload() {
  if ($('dnCompactEmptyLines').checked) {
    $('dnBody').value = compactEmptyLines($('dnBody').value);
  }
  return {cells: cellList(), includeNonMonitored: $('dnNonMon').checked, includeCc: $('dnCcOn').checked, cc: $('dnCc').value,
    includeBcc: $('dnBccOn').checked, bcc: $('dnBcc').value,
    reason: reasonValue(), eta: $('dnEta').value, delay: $('dnDelay').value, note: $('dnNoteOn').checked ? $('dnNote').value : '',
    subject: $('dnSubject').value, body: $('dnBody').value};
}
// ECPDS cannot know whether the message was really sent from Outlook, so the operator confirms it here.
function askConfirm(ref, index, m) {
  var box = $('dnAlert');
  box.innerHTML = '<div class="alert alert-warning py-2"><div class="mb-2"><i class="bi bi-envelope-exclamation me-1"></i><strong>' + esc(ref)
    + '</strong> is registered (' + m.emails.length + ' emails) but <strong>not sent yet</strong>: press Send in the Outlook window, then confirm.</div>'
    + '<button class="btn btn-sm btn-success me-2" id="dnConfYes"><i class="bi bi-check2"></i> I have sent it</button>'
    + '<button class="btn btn-sm btn-outline-secondary me-2" id="dnConfAgain"><i class="bi bi-box-arrow-up-right"></i> Open Outlook again</button>'
    + '<button class="btn btn-sm btn-link" id="dnConfLater">Confirm later (Sent notices)</button></div>';
  $('dnConfYes').onclick = function() { confirmSent(ref, index); };
  $('dnConfAgain').onclick = function() {
    var popup = openOutlookWindow();
    if (popup) { outlook(m.subject, m.body, m.emails, m.cells, m.cc, m.bcc, m.markdown, popup); }
  };
  $('dnConfLater').onclick = function() { box.innerHTML = ''; };
}
function confirmSent(ref, index) {
  post('confirm', {ref: ref, index: index}).then(function() { alertMsg('Sending of ' + ref + ' confirmed', 'success'); load(); }).catch(fail);
}
window.dnPreview = function() {
  if ($('dnCcOn').checked && !dnValidateCc()) { $('dnCc').focus(); return; }
  if ($('dnBccOn').checked && !dnValidateBcc()) { $('dnBcc').focus(); return; }
  if ($('dnCompactEmptyLines').checked) {
    $('dnBody').value = compactEmptyLines($('dnBody').value);
  }
  var cc = $('dnCcOn').checked ? $('dnCc').value.split(/[;,]/).map(function(x) { return x.trim(); }).filter(Boolean) : [];
  var bcc = $('dnBccOn').checked ? addressList($('dnBcc').value) : [];
  var ref = S.update ? S.update.ref : 'DLY-yyyy-nnnn (assigned when sent)';
  var subj = '[' + ref + '] ' + (S.update ? 'UPDATE - ' : '') + $('dnSubject').value;
  $('dnPreviewHead').innerHTML = '<div><strong>Subject:</strong> ' + esc(subj) + '</div>'
    + '<div><strong>To:</strong> ' + esc(TO_ADDRESS) + '</div>'
    + '<div><strong>Cc:</strong> ' + (cc.length ? esc(cc.join(', ')) : '<span class="text-muted">none</span>') + '</div>'
    + '<div><strong>Bcc:</strong> ' + (S.emails || []).length + ' recipient email(s) in ' + Object.keys(S.dests || {}).length + ' destination(s)'
    + (bcc.length ? ' + ' + bcc.length + ' additional (' + esc(bcc.join(', ')) + ')' : '') + '</div>';
  showPreviewModal($('dnBody').value);
};
function showPreviewModal(body) {
  $('dnPreviewBody').textContent = body;
  S.previewData = null; S.previewPlain = false;
  $('dnPreviewToggle').classList.toggle('d-none', !S.markdown);
  showPreviewMode();
  bootstrap.Modal.getOrCreateInstance($('dnPreviewModal')).show();
  if (S.markdown) {
    post('markdown', {markdown: body}).then(function(d) { S.previewData = d; showPreviewMode(); }).catch(fail);
  }
}
// Markdown messages: formatted view (direct sending) or plain-text view (what Outlook will receive).
function showPreviewMode() {
  var md = S.markdown, plain = S.previewPlain;
  $('dnPreviewHtml').classList.toggle('d-none', !md || plain);
  $('dnPreviewBody').classList.toggle('d-none', md && !plain);
  if (md) {
    var d = S.previewData;
    $('dnPreviewHtml').innerHTML = d ? d.html : '<span class="text-muted">Loading…</span>';
    $('dnPreviewBody').textContent = d ? d.text : '';
    $('dnPreviewToggle').innerHTML = plain ? '<i class="bi bi-file-earmark-richtext me-1"></i>Show formatted view (direct sending)'
      : '<i class="bi bi-file-earmark-text me-1"></i>Show plain text (Outlook)';
  }
}
window.dnPreviewToggle = function() { S.previewPlain = !S.previewPlain; showPreviewMode(); };
window.dnSend = function(direct) {
  if ($('dnCcOn').checked && !dnValidateCc()) { $('dnCc').focus(); return; }
  if ($('dnBccOn').checked && !dnValidateBcc()) { $('dnBcc').focus(); return; }
  var p = payload();
  if (!p.cells.length) { alertMsg('Select at least one product and cycle'); return; }
  if (!p.subject.trim() || !p.body.trim()) { alertMsg('The subject and message are required'); return; }
  direct = direct === true;
  var outlookWindow = direct ? null : openOutlookWindow();
  if (!direct && !outlookWindow) { return; }
  var btn = $('dnSendBtn'), directBtn = $('dnDirectBtn'); btn.disabled = true; directBtn.disabled = true;
  var upd = S.update;
  if (upd) { p.ref = upd.ref; }
  p.direct = direct;
  post(upd ? 'update' : 'send', p).then(function(d) {
    var ref = upd ? upd.ref : d.ref;
    var cc = p.includeCc ? p.cc : '';
    var bcc = p.includeBcc ? p.bcc : '';
    var message = {subject: '[' + ref + '] ' + (upd ? 'UPDATE - ' : '') + p.subject, body: p.body, emails: d.emails, cells: p.cells, cc: cc, bcc: bcc, markdown: S.markdown};
    S.sel = {}; S.update = null; setUpdateMode(false); S.bodyEdited = false;
    afterSelection(); load();
    if (direct && d.sentDirectly) {
      alertMsg('Message queued through the Master mail service and acknowledged', 'success');
    } else {
      if (direct && d.deliveryError) {
        alertMsg('Direct sending failed: ' + d.deliveryError + '. Outlook was opened instead.', 'warning');
      }
      outlook(message.subject, message.body, message.emails, message.cells, message.cc, message.bcc, message.markdown,
        outlookWindow);
      askConfirm(ref, d.index, message);
    }
  }).catch(function(e) {
    closeOutlookWindow(outlookWindow);
    fail(e);
  }).then(function() { btn.disabled = false; directBtn.disabled = false; });
};

function setUpdateMode(on, ref) {
  $('dnUpdBadge').classList.toggle('d-none', !on); $('dnUpdBadge').textContent = on ? 'Update ' + ref : '';
  $('dnCancelUpd').classList.toggle('d-none', !on);
  $('dnSendLbl').textContent = on ? 'Send update with Outlook' : 'Send notice with Outlook';
  $('dnDirectLbl').textContent = on ? 'Send update directly' : 'Send notice directly';
  $('dnTabComposeLbl').textContent = on ? 'Update ' + ref : 'Send delay notification';
}
window.dnLeaveUpdate = function() { S.update = null; setUpdateMode(false); S.sel = {}; S.bodyEdited = false; afterSelection(); };

function statusBadge(s) {
  var c = {active: 'danger', partial: 'warning', resolved: 'success', cancelled: 'secondary'}[s] || 'secondary';
  return '<span class="badge text-bg-' + c + '">' + esc(s) + '</span>';
}
function csvCell(value) {
  var text = String(value == null ? '' : value);
  if (/^[\u0000-\u0020]*[=+\-@]/.test(text)) { text = "'" + text; }
  return '"' + text.replace(/"/g, '""') + '"';
}
window.dnGenerateReport = function() {
  var fromDate = $('dnReportFrom').value, toDate = $('dnReportTo').value;
  if (!fromDate || !toDate || fromDate > toDate) {
    alertMsg('Select a valid report date range; the start must not be after the end.', 'warning');
    return;
  }
  var statusFilter = $('dnReportStatus').value;
  var includeHistory = $('dnReportHistory').checked;
  var includeAddresses = $('dnReportAddresses').checked;
  if (S.reportBusy) { return; }
  S.reportBusy = true;
  updateReportButton();
  post('report', {fromDate: fromDate, toDate: toDate}).then(function(data) {
    var notices = (data.notices || []).filter(function(n) { return statusFilter === 'all' || n.status === statusFilter; });
    var productCounts = {};
    var statusCounts = {active: 0, partial: 0, resolved: 0, cancelled: 0};
    var affectedCells = 0, unconfirmedMessages = 0;
    notices.forEach(function(n) {
      statusCounts[n.status] = (statusCounts[n.status] || 0) + 1;
      affectedCells += (n.cells || []).length;
      var seen = {};
      (n.cells || []).forEach(function(c) {
        if (!seen[c.product]) { productCounts[c.product] = (productCounts[c.product] || 0) + 1; seen[c.product] = true; }
      });
      (n.history || []).forEach(function(h) { if (h.confirmed === false) unconfirmedMessages++; });
    });
    var sortedProducts = Object.keys(productCounts).sort(function(a, b) {
      return productCounts[b] - productCounts[a] || a.localeCompare(b);
    });
    var topProducts = sortedProducts.slice(0, 10);
    var summary = '<div class="d-flex flex-wrap gap-2 mb-2">'
      + '<span class="badge text-bg-primary">' + notices.length + ' notices</span>'
      + '<span class="badge text-bg-danger">' + statusCounts.active + ' active</span>'
      + '<span class="badge text-bg-warning">' + statusCounts.partial + ' partial</span>'
      + '<span class="badge text-bg-success">' + statusCounts.resolved + ' resolved</span>'
      + '<span class="badge text-bg-secondary">' + statusCounts.cancelled + ' cancelled</span>'
      + '<span class="badge text-bg-info">' + affectedCells + ' affected product/cycles</span>'
      + '<span class="badge text-bg-warning">' + unconfirmedMessages + ' messages to confirm</span></div>';
    summary += '<div class="small mb-2"><strong>Most affected products:</strong> '
      + (topProducts.length ? topProducts.map(function(p) { return esc(p) + ' (' + productCounts[p] + ')'; }).join(', ') : 'None') + '</div>';
    var rows = [
      ['Report summary', fromDate + ' to ' + toDate, 'Notices', notices.length, '', '', '', '', '', '', '', '', '', '', '', '', '', ''],
      ['Status summary', '', 'Active', statusCounts.active, '', '', '', '', '', '', '', '', '', '', '', '', '', ''],
      ['Status summary', '', 'Partial', statusCounts.partial, '', '', '', '', '', '', '', '', '', '', '', '', '', ''],
      ['Status summary', '', 'Resolved', statusCounts.resolved, '', '', '', '', '', '', '', '', '', '', '', '', '', ''],
      ['Status summary', '', 'Cancelled', statusCounts.cancelled, '', '', '', '', '', '', '', '', '', '', '', '', '', '']
    ];
    sortedProducts.forEach(function(product) {
      rows.push(['Product summary', '', productCounts[product] + ' notices', '', '', '', product, '', '', '', '', '', '', '', '', '', '', '']);
    });
    var headers = ['Record type', 'Created', 'Reference', 'Status', 'Created by', 'Reason', 'Product', 'Cycle', 'Destinations', 'Recipient count', 'To', 'Cc', 'Bcc', 'Message type', 'Message date', 'Message by', 'Subject', 'Confirmed'];
    notices.forEach(function(n) {
      var cells = n.cells || [];
      var destinations = (n.destinations || []).join('; ');
      var emails = (n.emails || []).join('; ');
      var hasAddresses = includeAddresses;
      if (!cells.length) { cells = [{product: '', time: ''}]; }
      cells.forEach(function(c) {
        rows.push(['Affected product/cycle', n.createdAt, n.ref, n.status, n.createdBy, n.reason, c.product, c.time,
          destinations, (n.emails || []).length, hasAddresses ? emails : '', hasAddresses && n.includeCc ? n.cc : '',
          hasAddresses && n.includeBcc ? n.bcc : '', '', '', '', '', '']);
      });
      if (includeHistory) {
        (n.history || []).forEach(function(h) {
          rows.push(['Message history', n.createdAt, n.ref, n.status, n.createdBy, n.reason, '', '', destinations,
            (h.emails || []).length, hasAddresses ? (h.emails || []).join('; ') : '',
            hasAddresses ? h.cc : '', hasAddresses ? h.bcc : '', h.kind, h.at, h.by, h.subject,
            h.confirmed === false ? 'No' : h.confirmed === true ? 'Yes' : 'Not recorded']);
        });
      }
    });
    S.reportData = {headers: headers, rows: rows, fromDate: fromDate, toDate: toDate, summary: summary};
    $('dnReportSummary').innerHTML = summary
      + '<div class="small text-muted">Report contents for ' + esc(fromDate) + ' to ' + esc(toDate) + '.</div>';
    $('dnReportTable').innerHTML = '<thead class="table-light"><tr>'
      + headers.map(function(header) { return '<th>' + esc(header) + '</th>'; }).join('')
      + '</tr></thead><tbody>' + rows.map(function(row) {
        return '<tr>' + headers.map(function(_, index) { return '<td>' + esc(row[index]) + '</td>'; }).join('') + '</tr>';
      }).join('') + '</tbody>';
    bootstrap.Modal.getOrCreateInstance($('dnReportModal')).show();
  }).catch(fail).then(function() {
    S.reportBusy = false;
    updateReportButton();
  });
};
window.dnDownloadReport = function() {
  if (!S.reportData) { return; }
  bootstrap.Modal.getOrCreateInstance($('dnReportModal')).hide();
  var report = S.reportData;
  var csv = [report.headers].concat(report.rows).map(function(row) { return row.map(csvCell).join(','); }).join('\r\n');
  var blob = new Blob(['\ufeff' + csv], {type: 'text/csv;charset=utf-8'});
  var url = URL.createObjectURL(blob), link = document.createElement('a');
  link.href = url;
  link.download = 'delay-notices-' + report.fromDate + '-to-' + report.toDate + '.csv';
  document.body.appendChild(link); link.click(); link.remove(); window.setTimeout(function() { URL.revokeObjectURL(url); }, 1000);
  $('dnReportResults').innerHTML = report.summary + '<div class="small text-muted">CSV report downloaded. Product counts are the number of notices affecting each product; select the optional flags to add message history or recipient addresses.</div>';
  $('dnReportResults').classList.remove('d-none');
};
function sentDetails(n, i) {
  var affected = (n.cells || []).map(function(c) {
    return '<span class="badge text-bg-light border me-1 mb-1">' + esc(c.product) + ' ' + esc(c.time)
      + (c.resolved ? ' <i class="bi bi-check-circle text-success" title="Resolved"></i>' : '') + '</span>';
  }).join('');
  var hist = (n.history || []).map(function(x, k) {
    return '<div class="py-1">' + esc(x.at) + ' ' + esc(x.kind) + ' by ' + esc(x.by) + (x.addedEmails && x.addedEmails.length ? ' (+' + x.addedEmails.length + ' recipients)' : '')
      + (x.confirmed === false ? ' <span class="badge text-bg-warning">not confirmed as sent</span> '
          + '<button class="btn btn-sm btn-outline-success py-0" data-a="confirm" data-i="' + i + '" data-k="' + k + '">I have sent it</button> '
          + '<button class="btn btn-sm btn-outline-danger py-0" data-a="direct" data-i="' + i + '" data-k="' + k + '">Send directly</button> '
          + '<button class="btn btn-sm btn-outline-secondary py-0" data-a="reopen" data-i="' + i + '" data-k="' + k + '">Open Outlook again</button>'
        : (x.confirmedAt ? ' <span class="text-success"><i class="bi bi-check2"></i> ' + (x.sentVia === 'direct' ? 'queued directly' : 'sent') + ', acknowledged by ' + esc(x.confirmedBy) + ' (' + esc(x.confirmedAt) + ')</span>' : '')) + '</div>';
  }).join('');
  return '<div class="p-2"><div id="dnRes' + i + '" class="d-none"></div>'
    + '<div class="small mb-2"><strong>Affected product/cycles:</strong><div class="mt-1">' + (affected || '<span class="text-muted">None recorded</span>') + '</div></div>'
    + '<div class="small text-muted">' + (hist || 'No messages recorded') + '</div></div>';
}
function initSentTable() {
  var $ = window.jQuery; // the page-level $ is the getElementById helper
  var pageLen = 25;
  try {
    var saved = parseInt(localStorage.getItem('dnSentPageLen'), 10);
    if ([10, 25, 50, 100, 250].indexOf(saved) >= 0) pageLen = saved;
  } catch (e) {}
  S.sentTable = $('#dnSentTable').DataTable({
    paging: true, pageLength: pageLen, searching: true, ordering: true, info: true, autoWidth: false,
    order: [[2, 'desc']],
    columnDefs: [{orderable: false, targets: [1, 3, 4, 5, 6]}],
    language: {info: 'Showing _START_-_END_ of _TOTAL_', infoEmpty: '', infoFiltered: '', emptyTable: 'No matching records found.', zeroRecords: 'No matching records found.'},
    dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
    drawCallback: function() {
      // Hide the empty info/pagination bar so it doesn't leave a blank gap above the footer.
      $(this.api().table().container()).children('.d-flex').toggleClass('d-none', this.api().page.info().recordsDisplay === 0);
    }
  });
  $('#dnSentSearch').on('input', function() { S.sentTable.search(this.value).draw(); });
  $('#dnSentPageLen').val(pageLen).on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('dnSentPageLen', len); } catch (e) {}
    S.sentTable.page.len(len).draw();
  });
  var mode = 'auto';
  try { mode = localStorage.getItem('dnSentColMode') || 'auto'; } catch (e) {}
  if (['auto', 'all', 'compact'].indexOf(mode) < 0) mode = 'auto';
  function showCols(hidden) {
    S.sentTable.columns().every(function(index) { this.visible(hidden.indexOf(index) < 0, false); });
    S.sentTable.columns.adjust();
  }
  function applyMode(next) {
    mode = next;
    $('#dnSentColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + next.charAt(0).toUpperCase() + next.slice(1));
    $('#dnSentColModeBtn').toggleClass('btn-outline-secondary', next === 'auto').toggleClass('btn-primary', next !== 'auto');
    $('#dnSentColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
      $(this).find('i.bi-check').remove();
      if ($(this).data('dn-sent-mode') === next) $(this).prepend('<i class="bi bi-check me-1"></i>');
    });
    if (next === 'all') showCols([]);
    else if (next === 'compact') showCols([3, 4]);
    else showCols(window.innerWidth < 768 ? [2, 3, 4, 5] : window.innerWidth < 1200 ? [3, 4] : []);
  }
  $('#dnSentColModeBtn').closest('.dropdown').find('[data-dn-sent-mode]').on('click', function(e) {
    e.preventDefault();
    var next = $(this).data('dn-sent-mode');
    try { localStorage.setItem('dnSentColMode', next); } catch (e) {}
    applyMode(next);
  });
  $(window).on('resize.dnSentTable', function() { if (mode === 'auto') applyMode('auto'); });
  applyMode(mode);
  $('#dnSentBody').on('click', 'button[data-a]', function() {
    var button = this;
    sentAction(button.dataset.a, S.notices[+button.dataset.i], +button.dataset.i, +button.dataset.k);
  });
}
function updateReportButton() {
  var empty = S.notices.length === 0, disabled = empty || S.reportBusy;
  $('dnReportButton').disabled = disabled;
  $('dnReportButtonWrap').title = empty ? 'There are no notices to report on' : '';
}
function renderSent() {
  $('dnSentCount').textContent = S.notices.length;
  if (!S.sentTable) initSentTable();
  var rows = S.notices.map(function(n, i) {
    var open = n.status === 'active' || n.status === 'partial';
    var products = {}, cycles = {};
    (n.cells || []).forEach(function(c) { products[c.product] = 1; cycles[c.product + '@' + c.time] = 1; });
    var pending = (n.history || []).some(function(x) { return x.confirmed === false; });
    var status = statusBadge(n.status) + (pending ? ' <span class="badge text-bg-warning" title="A message is registered but its sending is not confirmed">to confirm</span>' : '');
    var actions = (open ? '<button class="btn btn-sm btn-success" data-a="resolve" data-i="' + i + '"><i class="bi bi-check2-circle"></i> Inform resolution</button> '
      + '<button class="btn btn-sm btn-outline-primary" data-a="update" data-i="' + i + '"><i class="bi bi-pencil"></i> Update</button> '
      + '<button class="btn btn-sm btn-outline-warning" data-a="cancel" data-i="' + i + '" title="Cancel notice"><i class="bi bi-x-circle"></i></button> ' : '')
      + '<button class="btn btn-sm btn-outline-secondary" data-a="details" data-i="' + i + '" title="Show affected product/cycles and message history"><i class="bi bi-clock-history"></i></button> '
      + '<button class="btn btn-sm btn-outline-secondary" data-a="copy" data-i="' + i + '" title="Copy BCC emails"><i class="bi bi-clipboard"></i></button> '
      + '<button class="btn btn-sm btn-outline-danger" data-a="delete" data-i="' + i + '" title="Delete notice"><i class="bi bi-trash"></i></button>';
    return [
      '<span class="fw-semibold">' + esc(n.ref) + '</span>',
      status,
      '<span class="small" data-order="' + esc(n.createdAt) + '">' + esc(n.createdAt) + '<br>' + esc(n.createdBy) + '</span>',
      '<span class="small">' + esc(n.reason) + '</span>',
      '<span class="small text-nowrap">' + Object.keys(products).length + ' products · ' + Object.keys(cycles).length + ' cycles</span>',
      '<span class="small">' + (n.destinations || []).length + ' dest., ' + (n.emails || []).length + ' emails</span>',
      '<div class="text-nowrap">' + actions + '</div>'
    ];
  });
  S.sentTable.clear().rows.add(rows).draw(false);
  S.sentTable.rows().every(function() { this.node().id = 'dnSentRow' + this.index(); });
  $('dnMore').classList.toggle('d-none', !S.more);
  updateReportButton();
}
function sentAction(a, n, i, k) {
  if (a === 'details') {
    var tableRow = S.sentTable.row('#dnSentRow' + i);
    if (tableRow.child.isShown()) {
      tableRow.child.hide();
    } else {
      tableRow.child(sentDetails(n, i)).show();
    }
    return;
  }
  if (a === 'confirm') { confirmSent(n.ref, k); return; }
  if (a === 'direct') {
    post('resend', {ref: n.ref, index: k}).then(function() {
      alertMsg('Message queued through the Master mail service and acknowledged', 'success');
      load();
    }).catch(fail);
    return;
  }
  if (a === 'reopen') {
    var m = n.history[k];
    var popup = openOutlookWindow();
    if (popup) {
      outlook('[' + n.ref + '] ' + (m.kind === 'update' ? 'UPDATE - ' : '') + m.subject, m.body, m.emails || [],
        m.cells ? m.cells.map(function(c) { var j = c.lastIndexOf('@'); return {product: c.substring(0, j), time: c.substring(j + 1)}; }) : (n.cells || []),
        m.cc, m.bcc, m.markdown !== false, popup);
    }
    return;
  }
  if (a === 'copy') { copy((n.emails || []).concat(n.includeBcc ? addressList(n.bcc) : []).join(',')).then(function() { alertMsg('Emails copied', 'success'); }); }
  else if (a === 'delete') {
    confirmationDialog({
      title: 'Delete delay notice',
      message: 'Delete notice ' + esc(n.ref) + '? This cannot be undone.',
      confirmText: 'Delete',
      showLoading: false,
      allowHtml: true,
      onConfirm: function() { post('delete', {ref: n.ref}).then(load).catch(fail); }
    });
  } else if (a === 'cancel') {
    confirmationDialog({
      title: 'Cancel delay notice',
      message: 'Cancel notice ' + esc(n.ref) + '? No more update or resolution will be possible.',
      confirmText: 'Cancel notice',
      showLoading: false,
      allowHtml: true,
      onConfirm: function() {
        post('cancel', {ref: n.ref, subject: 'Cancelled ' + n.ref, body: ''})
          .then(function() { load(); alertMsg(n.ref + ' cancelled. Use the copy button if you need to tell the recipients.', 'success'); }).catch(fail);
      }
    });
  } else if (a === 'update') { startUpdate(n); }
  else if (a === 'resolve') { openResolve(n, i); }
}
function startUpdate(n) {
  S.update = n; S.sel = {};
  (n.cells || []).forEach(function(c) { if (!c.resolved) { S.sel[c.product + '@' + c.time] = c.level; } });
  $('dnNonMon').checked = !!n.includeNonMonitored;
  $('dnCcOn').checked = S.allowNoticeCc && !!n.includeCc; $('dnCc').value = n.cc || '';
  $('dnBccOn').checked = S.allowNoticeBcc && !!n.includeBcc; $('dnBcc').value = n.bcc || '';
  dnUpdateCopyBcc();
  var knownReasons = Array.prototype.map.call($('dnReason').options, function(option) { return option.value; });
  if (knownReasons.indexOf(n.reason) >= 0) {
    $('dnReason').value = n.reason;
    $('dnCustomReason').value = '';
  } else {
    $('dnReason').value = '__custom__';
    $('dnCustomReason').value = n.reason || '';
  }
  $('dnCustomReason').classList.toggle('d-none', $('dnReason').value !== '__custom__');
  setEta(n.eta); $('dnDelay').value = n.delay || ''; $('dnNote').value = n.note || '';
  $('dnNoteOn').checked = !!n.note; dnNoteToggle();
  S.bodyEdited = false; dnAuto();
  $('dnSubject').value = n.subject; $('dnBody').value = 'UPDATE\n\n' + n.body; S.bodyEdited = true;
  setUpdateMode(true, n.ref); dnTab('compose'); dnRenderMatrix(); dnRecipients();
}
function openResolve(n, i) {
  var tableRow = S.sentTable.row('#dnSentRow' + i);
  if (!tableRow.child.isShown()) {
    tableRow.child(sentDetails(n, i)).show();
  }
  var row = $('dnRes' + i);
  row.classList.remove('d-none');
  var openCells = (n.cells || []).filter(function(c) { return !c.resolved; });
  var products = describeProducts(openCells);
  function resolutionSubject(cells) { return 'Resolved: ' + describeProducts(cells).join(', ') + subjectCycles(cells); }
  function resolutionBody(cells) {
    var names = describeProducts(cells);
    return S.resumedTemplate ? fillTemplate(S.resumedTemplate, names, cells, {reason: n.reason, eta: n.eta, expectedDelay: n.delay, note: n.note}) : 'Dear users,\n\nThe delay affecting ' + names.join(', ') + ' has been resolved.\n\nBest regards,\nECMWF Operations';
  }
  row.innerHTML = '<div class="p-2 border rounded mb-2"><div class="small mb-1">Cells to resolve:</div>'
    + openCells.map(function(c) { return '<label class="me-3 small"><input type="checkbox" class="form-check-input me-1" checked value="' + esc(c.product + '@' + c.time) + '">' + esc(c.product + ' ' + c.time) + '</label>'; }).join('')
    + '<textarea class="form-control my-2 rs-subj" rows="2" aria-label="Resolution subject">' + esc(resolutionSubject(openCells)) + '</textarea>'
    + '<textarea class="form-control form-control-sm mb-2 rs-body" rows="6">' + esc(resolutionBody(openCells)) + '</textarea>'
    + '<button class="btn btn-sm btn-outline-secondary rs-regen me-2"><i class="bi bi-arrow-clockwise me-1"></i>Regenerate</button><button class="btn btn-sm btn-outline-primary rs-preview me-2"><i class="bi bi-eye me-1"></i>Preview</button><button class="btn btn-sm btn-success rs-go me-2">Register and open Outlook</button><button class="btn btn-sm btn-outline-success rs-direct">Send directly</button></div>';
  // Follow the ticked cells until the operator edits the subject or the message.
  var subjectField = row.querySelector('.rs-subj'), bodyField = row.querySelector('.rs-body'), edited = false;
  function setEdited(value) {
    edited = value;
    [subjectField, bodyField].forEach(function(field) {
      field.classList.toggle('dn-edited', edited);
      field.title = edited ? 'Edited manually: no longer updated automatically (use Regenerate)' : '';
    });
  }
  function regenerate() {
    var ticked = Array.prototype.map.call(row.querySelectorAll('input[type=checkbox]:checked'), function(c) { return c.value; });
    var cells = openCells.filter(function(c) { return ticked.indexOf(c.product + '@' + c.time) >= 0; });
    if (!cells.length) { return; }
    subjectField.value = resolutionSubject(cells);
    bodyField.value = resolutionBody(cells);
    setEdited(false);
  }
  subjectField.oninput = function() {
    subjectField.value = subjectField.value.replace(/[\r\n]+/g, ' ');
    setEdited(true);
  };
  bodyField.oninput = function() { setEdited(true); };
  row.querySelector('.rs-regen').onclick = regenerate;
  Array.prototype.forEach.call(row.querySelectorAll('input[type=checkbox]'), function(box) {
    box.onchange = function() {
      if (!row.querySelector('input[type=checkbox]:checked')) {
        box.checked = true;
        alertMsg('At least one cell must remain selected', 'warning');
        return;
      }
      if (!edited) { regenerate(); }
    };
  });
  function sendResolution(direct) {
    var picked = Array.prototype.map.call(row.querySelectorAll('input[type=checkbox]:checked'), function(c) { return c.value; });
    var subject = row.querySelector('.rs-subj').value, body = row.querySelector('.rs-body').value;
    if (!picked.length) { alertMsg('Select at least one cell'); return; }
    var outlookWindow = direct ? null : openOutlookWindow();
    if (!direct && !outlookWindow) { return; }
    post('resolve', {ref: n.ref, cells: picked.length === openCells.length ? [] : picked, subject: subject, body: body, direct: direct}).then(function(d) {
      var cells = picked.map(function(k) { var j = k.lastIndexOf('@'); return {product: k.substring(0, j), time: k.substring(j + 1)}; });
      var cc = d.allowNoticeCc && S.allowNoticeCc && n.includeCc ? n.cc : '';
      var bcc = d.allowNoticeBcc && S.allowNoticeBcc && n.includeBcc ? n.bcc : '';
      var message = {subject: '[' + n.ref + '] ' + subject, body: body, emails: d.emails, cells: cells, cc: cc, bcc: bcc, markdown: S.markdown};
      load();
      if (direct && d.sentDirectly) {
        alertMsg('Resolution message queued through the Master mail service and acknowledged', 'success');
      } else {
        if (direct && d.deliveryError) {
          alertMsg('Direct sending failed: ' + d.deliveryError + '. Outlook was opened instead.', 'warning');
        }
        outlook(message.subject, message.body, message.emails, message.cells, message.cc, message.bcc, message.markdown,
          outlookWindow);
        askConfirm(n.ref, d.index, message);
      }
    }).catch(function(e) {
      closeOutlookWindow(outlookWindow);
      fail(e);
    });
  }
  row.querySelector('.rs-preview').onclick = function() {
    var cc = S.allowNoticeCc && n.includeCc ? addressList((n.cc || '').replace(/,/g, ';')) : [];
    var bcc = S.allowNoticeBcc && n.includeBcc ? addressList(n.bcc) : [];
    $('dnPreviewHead').innerHTML = '<div><strong>Subject:</strong> ' + esc('[' + n.ref + '] ' + subjectField.value) + '</div>'
      + '<div><strong>To:</strong> ' + esc(TO_ADDRESS) + '</div>'
      + '<div><strong>Cc:</strong> ' + (cc.length ? esc(cc.join(', ')) : '<span class="text-muted">none</span>') + '</div>'
      + '<div><strong>Bcc:</strong> ' + (n.emails || []).length + ' recipient email(s) in ' + (n.destinations || []).length + ' destination(s)'
      + (bcc.length ? ' + ' + bcc.length + ' additional (' + esc(bcc.join(', ')) + ')' : '') + '</div>';
    showPreviewModal(bodyField.value);
  };
  row.querySelector('.rs-go').onclick = function() { sendResolution(false); };
  row.querySelector('.rs-direct').onclick = function() { sendResolution(true); };
}

window.dnMoreNotices = function() { S.limit += 100; load(); };
function load() {
  return post('data', {limit: S.limit}).then(function(d) {
    S.more = d.more;
    S.cycles = d.cycles; S.minutesBeforeSchedule = d.minutesBeforeSchedule || {};
    var selectedCycle = $('dnCycleFilter').value;
    var cycles = {};
    Object.keys(S.cycles).forEach(function(product) {
      S.cycles[product].forEach(function(cycle) { cycles[cycle] = true; });
    });
    $('dnCycleFilter').innerHTML = '<option value="">All cycles</option>' + Object.keys(cycles).sort().map(function(cycle) {
      return '<option value="' + esc(cycle) + '">' + esc(cycle) + '</option>';
    }).join('');
    $('dnCycleFilter').value = cycles[selectedCycle] ? selectedCycle : '';
    S.minutesBeforeSchedulePerCycle = d.minutesBeforeSchedulePerCycle || {};
    S.generationStatusPerProduct = d.generationStatusPerProduct || {};
    S.generationStatusPerCycle = d.generationStatusPerCycle || {};
    S.models = d.models; S.notices = d.notices; S.defaultCc = d.defaultCc; S.defaultBcc = d.defaultBcc;
    S.markdown = d.markdownMessages !== false;
    $('dnMarkdownHint').classList.toggle('d-none', !S.markdown);
    $('dnNoteMarkdownHint').classList.toggle('d-none', !S.markdown);
    S.allowNoticeCc = d.allowNoticeCc !== false; S.allowNoticeBcc = d.allowNoticeBcc !== false;
    $('dnCcOption').classList.toggle('d-none', !S.allowNoticeCc);
    $('dnBccOption').classList.toggle('d-none', !S.allowNoticeBcc);
    if (!S.allowNoticeCc) $('dnCcOn').checked = false;
    if (!S.allowNoticeBcc) $('dnBccOn').checked = false;
    if (d.toAddress) { TO_ADDRESS = d.toAddress; } if (d.bccAddress) { BCC_LIST = d.bccAddress; }
    S.compactCycles = d.compactCycles; S.delayTemplate = d.delayTemplate; S.resumedTemplate = d.resumedTemplate; S.descriptions = d.descriptions;
    if (!$('dnCc').value && !S.update) { $('dnCc').value = d.defaultCc || ''; }
    if (!$('dnBcc').value && !S.update) { $('dnBcc').value = d.defaultBcc || ''; }
    if (!preferencesRestored) { restorePreferences(); preferencesRestored = true; }
    renderModels(); renderSent(); dnRenderMatrix();
  }).catch(fail);
}
// Entry points from a monitoring page: ?product=GWAVE&time=18&action=delay|resumed ("*" matches everything)
function fromProductPage() {
  var q = new URLSearchParams(window.location.search);
  var product = q.get('product'), time = q.get('time'), action = q.get('action');
  if (!product || !time) { return; }
  $('dnFilter').value = product === '*' ? '' : product;
  activeModels = {};
  selectedStatuses = [];
  renderModels();
  renderStatusFilter();
  if (time !== '*' && !Array.prototype.some.call($('dnCycleFilter').options, function(option) { return option.value === time; })) {
    var option = document.createElement('option');
    option.value = time; option.textContent = time;
    $('dnCycleFilter').appendChild(option);
  }
  $('dnCycleFilter').value = time === '*' ? '' : time;
  dnRenderMatrix();
  var label = (product === '*' ? 'all products' : product) + (time === '*' ? '' : ' ' + time);
  function wanted(p, t) { return (product === '*' || product === p) && (time === '*' || time === t); }
  if (action === 'resumed') {
    var found = -1;
    (S.notices || []).some(function(n, i) {
      if ((n.status === 'active' || n.status === 'partial') && (n.cells || []).some(function(c) { return !c.resolved && wanted(c.product, c.time); })) { found = i; return true; }
      return false;
    });
    if (found < 0) { alertMsg('No open notice covers ' + label + '. Register a delay notice first.', 'warning'); return; }
    dnTab('sent');
    S.sentTable.search('').draw(false);
    $('dnSentSearch').value = '';
    var visibleRows = S.sentTable.rows({order: 'applied', search: 'applied'}).indexes().toArray();
    var position = visibleRows.indexOf(found);
    if (position >= 0) {
      S.sentTable.page(Math.floor(position / S.sentTable.page.len())).draw(false);
    }
    openResolve(S.notices[found], found);
    Array.prototype.forEach.call($('dnRes' + found).querySelectorAll('input[type=checkbox]'), function(c) {
      var j = c.value.lastIndexOf('@'); c.checked = wanted(c.value.substring(0, j), c.value.substring(j + 1));
    });
    $('dnRes' + found).scrollIntoView({block: 'center'});
    return;
  }
  var n = 0;
  Object.keys(S.cycles).forEach(function(p) { S.cycles[p].forEach(function(t) { if (wanted(p, t)) { S.sel[p + '@' + t] = 'definite'; n++; } }); });
  if (!n) { alertMsg(label + ' is not available for notification', 'warning'); return; }
  afterSelection();
}
load().then(function() { dnAuto(); fromProductPage(); });
var reportToday = new Date(), reportFrom = new Date(reportToday);
reportFrom.setDate(reportFrom.getDate() - 30);
$('dnReportFrom').value = localDate(reportFrom);
$('dnReportTo').value = localDate(reportToday);
})();
</script>

<jsp:include page="/WEB-INF/jsp/pds/monitoring/delaynotification/guide.jsp"/>
