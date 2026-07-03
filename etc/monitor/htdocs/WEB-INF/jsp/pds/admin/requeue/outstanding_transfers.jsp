<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-exclamation-triangle text-primary"></i>
<span class="fw-semibold">Outstanding Transfers</span>
<button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#outstandingLegend" aria-expanded="false" title="What are outstanding transfers?">
<i class="bi bi-info-circle"></i>
</button>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
<div class="input-group input-group-sm" style="width:auto">
<span class="input-group-text"><i class="bi bi-search"></i></span>
<input type="text" id="requeueSearch" class="form-control" placeholder="Filter..." style="min-width:160px">
</div>
<div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
<span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
<select id="requeuePageLen" class="form-select form-select-sm" style="width:auto">
<option value="10">10</option>
<option value="25">25</option>
<option value="50">50</option>
<option value="100">100</option>
<option value="250">250</option>
</select>
</div>
<div class="dropdown">
<button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="rqColModeBtn"
        data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
    <i class="bi bi-layout-three-columns me-1"></i>Auto
</button>
<ul class="dropdown-menu dropdown-menu-end" aria-labelledby="rqColModeBtn">
    <li><a class="dropdown-item" href="#" data-rqcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
    <li><a class="dropdown-item" href="#" data-rqcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
    <li><a class="dropdown-item" href="#" data-rqcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Host, %, B/s, Priority</small></a></li>
    <li><hr class="dropdown-divider"></li>
    <li><a class="dropdown-item" href="#" data-rqcol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
    <li id="rqCustomColChkPanel" style="display:none;">
        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="rqchk-0">Destination <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-1" data-col="1" checked><label class="form-check-label" for="rqchk-1">Host</label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="rqchk-2">Target <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-3" data-col="3" checked><label class="form-check-label" for="rqchk-3">Status</label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-4" data-col="4" checked><label class="form-check-label" for="rqchk-4">%</label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-5" data-col="5" checked><label class="form-check-label" for="rqchk-5">B/s</label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-6" data-col="6" checked><label class="form-check-label" for="rqchk-6">Priority</label></div>
            <div class="form-check mb-0"><input class="form-check-input rq-col-chk" type="checkbox" id="rqchk-7" data-col="7" checked><label class="form-check-label" for="rqchk-7">Comment</label></div>
        </div>
    </li>
</ul>
</div>
</div>
</div>
<div class="collapse" id="outstandingLegend">
<div class="px-3 py-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-bottom:1px solid var(--bs-border-color);">
    <p class="text-muted mb-2" style="font-size:0.78rem;">Outstanding transfers are those in an error or retry state that may need attention. Deleted transfers and transfers manually stopped or requeued by a user are excluded.</p>
    <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Error State</div>
    <div class="mb-1"><span class="badge bg-danger me-1">Failed</span> Dissemination to the remote site has failed</div>
    <div class="mb-2"><span class="badge bg-secondary me-1">Stopped</span> Stopped by the system due to an unrecoverable error or because the maximum number of retries was reached (not manually stopped by a user)</div>
    <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Retry &mdash; automatic</div>
    <div class="mb-1"><span class="badge bg-warning text-dark me-1">Queued</span> Waiting to be retried &mdash; has at least one prior failed attempt</div>
    <div><span class="badge bg-warning text-dark me-1">ReQueued</span> Re-queued by the scheduler (not manually). Covers transfers requeued after a transient error, when the maximum requeue limit was reached, or when the maximum start limit was reached</div>
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="requeueTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-secondary">
<tr>
<th>Destination</th>
<th>Host</th>
<th>Target</th>
<th>Status</th>
<th>%</th>
<th>B/s</th>
<th>Priority</th>
<th>Comment</th>
</tr>
</thead>
<tbody></tbody>
</table>
</div>
</div>
</div>

<auth:if basePathKey="admin.basepath" paths="/requeue">
<auth:then>
<div class="mt-3 d-flex gap-2">
<button type="button" id="requeueBtn" class="btn btn-sm btn-warning" disabled data-bs-toggle="modal" data-bs-target="#requeueConfirmModal">
<i class="bi bi-arrow-repeat"></i> Requeue all
</button>
<auth:if basePathKey="admin.basepath" paths="/delete">
<auth:then>
<button type="button" id="deleteBtn" class="btn btn-sm btn-danger" disabled data-bs-toggle="modal" data-bs-target="#deleteConfirmModal">
<i class="bi bi-trash"></i> Delete all
</button>
</auth:then>
</auth:if>
</div>

<!-- Requeue confirmation modal -->
<div class="modal fade" id="requeueConfirmModal" tabindex="-1" aria-labelledby="requeueConfirmModalLabel" aria-hidden="true">
<div class="modal-dialog">
<div class="modal-content">
<div class="modal-header">
<h5 class="modal-title" id="requeueConfirmModalLabel"><i class="bi bi-arrow-repeat text-warning"></i> Confirm Requeue</h5>
<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body" id="requeueModalBody">
Are you sure you want to requeue all outstanding transfers across <strong>all destinations</strong>?
</div>
<div class="modal-footer">
<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
<a href="/do/admin/requeue?restart=true" class="btn btn-warning">
<i class="bi bi-arrow-repeat"></i> Requeue
</a>
</div>
</div>
</div>
</div>

<!-- Delete confirmation modal -->
<div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-labelledby="deleteConfirmModalLabel" aria-hidden="true">
<div class="modal-dialog">
<div class="modal-content">
<div class="modal-header">
<h5 class="modal-title" id="deleteConfirmModalLabel"><i class="bi bi-trash text-danger"></i> Confirm Delete</h5>
<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body">
Are you sure you want to <strong>permanently delete</strong> all outstanding transfers across <strong>all destinations</strong>? This action cannot be undone.
</div>
<div class="modal-footer">
<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
<a href="/do/admin/delete?delete=true" class="btn btn-danger">
<i class="bi bi-trash"></i> Delete
</a>
</div>
</div>
</div>
</div>
</auth:then>
</auth:if>

<script>
$(function() {
var _table = $('#requeueTransfersTable').DataTable({
serverSide: true,
processing: true,
ajax: {
url: '/do/admin/requeue/list',
type: 'GET'
},
pageLength: (function() { try { var v = parseInt(localStorage.getItem('requeuePageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
searching: true,
autoWidth: false,
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
order: [[0, 'asc']],
columns: [
{ orderable: true,  data: 0 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 1 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 2 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 3 , render: function(d) { return d || ''; } },
{ orderable: false, data: 4 , render: function(d) { return d || ''; } },
{ orderable: false, data: 5 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 6 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 7 , render: function(d) { return d || ''; } }
],
drawCallback: function(settings) {
var json = settings.json || {};
var total = json.recordsTotal || 0;
$('#requeueBtn, #deleteBtn').prop('disabled', total === 0);
},
language: {
info: 'Showing _START_-_END_ of _TOTAL_',
processing: 'Loading...',
emptyTable: 'No outstanding transfers found'
}
});
$('#requeueSearch').on('keyup', function() { _table.search(this.value).draw(); });
var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('requeuePageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
$('#requeuePageLen').val(_savedPageLen);
$('#requeuePageLen').on('change', function() {
var len = +this.value;
try { localStorage.setItem('requeuePageLen', len); } catch(e) {}
_table.page.len(len).draw();
});

/* ---- Cols:Auto ---- */
var _RQ_COL_KEY        = 'rqColMode';
var _RQ_CUSTOM_COL_KEY = 'rqCustomCols';
var _RQ_COMPACT        = [1, 4, 5, 6];
var _rqColMode = (function() { try { return localStorage.getItem(_RQ_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
var _rqCustomCols = (function() {
    try { var s = localStorage.getItem(_RQ_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
    return [0,1,2,3,4,5,6,7];
})();
function _rqShowCols(hideCols) {
    var n = _table.columns().count();
    for (var i = 0; i < n; i++) _table.column(i).visible(hideCols.indexOf(i) === -1, false);
    _table.columns.adjust();
}
function _rqApplyCustomCols() {
    var n = _table.columns().count();
    for (var i = 0; i < n; i++) {
        _table.column(i).visible((i === 0 || i === 2) ? true : _rqCustomCols.indexOf(i) !== -1, false);
    }
    _table.columns.adjust();
}
function _rqSyncChkBoxes() {
    document.querySelectorAll('.rq-col-chk').forEach(function(chk) {
        chk.checked = _rqCustomCols.indexOf(+chk.dataset.col) !== -1;
    });
}
document.querySelectorAll('.rq-col-chk').forEach(function(chk) {
    chk.addEventListener('change', function() {
        var col = +this.dataset.col;
        var idx = _rqCustomCols.indexOf(col);
        if (this.checked && idx === -1) _rqCustomCols.push(col);
        else if (!this.checked && idx !== -1) _rqCustomCols.splice(idx, 1);
        try { localStorage.setItem(_RQ_CUSTOM_COL_KEY, JSON.stringify(_rqCustomCols)); } catch(e) {}
        if (_rqColMode === 'custom') _rqApplyCustomCols();
    });
});
function _rqApplyResponsive() {
    if (_rqColMode !== 'auto') return;
    var w = window.innerWidth;
    if (w < 768)      _rqShowCols([1, 4, 5, 6, 7]);
    else if (w < 992) _rqShowCols([1, 4, 5, 6]);
    else              _rqShowCols([]);
}
function _rqApplyMode(mode) {
    var label = mode.charAt(0).toUpperCase() + mode.slice(1);
    $('#rqColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
    $('#rqColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
    $('#rqColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
        $(this).find('i.bi-check').remove();
        if ($(this).data('rqcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
    });
    document.getElementById('rqCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
    if (mode === 'auto') _rqApplyResponsive();
    else if (mode === 'all') _rqShowCols([]);
    else if (mode === 'compact') _rqShowCols(_RQ_COMPACT);
    else if (mode === 'custom') { _rqSyncChkBoxes(); _rqApplyCustomCols(); }
}
$(window).on('resize', _rqApplyResponsive);
_rqApplyMode(_rqColMode);
$('#rqColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
    e.preventDefault();
    var mode = $(this).data('rqcol-mode');
    if (!mode) return;
    _rqColMode = mode;
    try { localStorage.setItem(_RQ_COL_KEY, mode); } catch(e) {}
    _rqApplyMode(mode);
});
});
</script>
