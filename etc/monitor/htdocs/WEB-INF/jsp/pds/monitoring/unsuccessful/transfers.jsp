<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
<auth:then>
<c:set var="authorized" value="true" />
</auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>
<auth:if basePathKey="destination.basepath"
paths="/${destination.name}">
<auth:then>
<c:set var="authorized" value="true" />
</auth:then>
</auth:if>
</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
<div class="alert alert-danger">
Error retrieving object by key &larr; Destination not found: ${destination.name}
</div>
</c:if>

<c:if test="${authorized == 'true'}">
<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

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
<input type="text" id="unsuccessfulSearch" class="form-control" placeholder="Filter..." style="min-width:160px">
</div>
<div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
<span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
<select id="unsuccessfulPageLen" class="form-select form-select-sm" style="width:auto">
<option value="10">10</option>
<option value="25">25</option>
<option value="50">50</option>
<option value="100">100</option>
<option value="250">250</option>
</select>
</div>
<div class="dropdown">
    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="btColModeBtn"
            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
        <i class="bi bi-layout-three-columns me-1"></i>Auto
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="btColModeBtn">
        <li><a class="dropdown-item" href="#" data-bt-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
        <li><a class="dropdown-item" href="#" data-bt-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
        <li><a class="dropdown-item" href="#" data-bt-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: %, B/s, Priority</small></a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#" data-bt-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
        <li id="btCustomColChkPanel" style="display:none;">
            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="btchk-0">Host <small>(required)</small></label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="btchk-1">Target <small>(required)</small></label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-2" data-col="2" checked><label class="form-check-label" for="btchk-2">Status</label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-3" data-col="3" checked><label class="form-check-label" for="btchk-3">%</label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-4" data-col="4" checked><label class="form-check-label" for="btchk-4">B/s</label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-5" data-col="5" checked><label class="form-check-label" for="btchk-5">Priority</label></div>
                <div class="form-check mb-0"><input class="form-check-input bt-col-chk" type="checkbox" id="btchk-6" data-col="6" checked><label class="form-check-label" for="btchk-6">Comment</label></div>
            </div>
        </li>
    </ul>
</div>
</div>
</div>
<div class="collapse mb-2 mt-2" id="outstandingLegend">
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
<table id="badTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
<tr>
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
<div class="modal-body" id="requeueModalBody"></div>
<div class="modal-footer">
<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
<a id="requeueConfirmBtn" href="#" class="btn btn-warning">
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
<div class="modal-body" id="deleteModalBody"></div>
<div class="modal-footer">
<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
<a id="deleteConfirmBtn" href="#" class="btn btn-danger">
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
var _table = $('#badTransfersTable').DataTable({
serverSide: true,
processing: true,
ajax: {
url: '/do/monitoring/unsuccessful/list/${destination.name}',
type: 'GET'
},
pageLength: (function() { try { var v = parseInt(localStorage.getItem('unsuccessfulPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
searching: true,
autoWidth: false,
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
order: [[0, 'asc']],
columns: [
{ orderable: true,  data: 0 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 1 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 2 , render: function(d) { return d || ''; } },
{ orderable: false, data: 3 , render: function(d) { return d || ''; } },
{ orderable: false, data: 4 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 5 , render: function(d) { return d || ''; } },
{ orderable: true,  data: 6 , render: function(d) { return d || ''; } }
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
$('#unsuccessfulSearch').on('keyup', function() { _table.search(this.value).draw(); });
var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('unsuccessfulPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
$('#unsuccessfulPageLen').val(_savedPageLen);
$('#unsuccessfulPageLen').on('change', function() {
var len = +this.value;
try { localStorage.setItem('unsuccessfulPageLen', len); } catch(e) {}
_table.page.len(len).draw();
});

        /* ---- Cols:Auto ---- */
        var _btColKey        = 'btColMode';
        var _btCustomColKey  = 'btCustomCols';
        var _btCompact       = [3,4,5];
        var _btColMode = (function() { try { return localStorage.getItem(_btColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _btCustomCols = (function() {
            try { var s = localStorage.getItem(_btCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,2,3,4,5,6];
        })();
        function _btShowCols(hideCols) {
            var n = _table.columns().count();
            for (var i = 0; i < n; i++) _table.column(i).visible(hideCols.indexOf(i) === -1, false);
            _table.columns.adjust();
        }
        function _btApplyCustomCols() {
            var n = _table.columns().count();
            for (var i = 0; i < n; i++) {
                _table.column(i).visible((i === 0 || i === 1) ? true : _btCustomCols.indexOf(i) !== -1, false);
            }
            _table.columns.adjust();
        }
        function _btSyncChkBoxes() {
            document.querySelectorAll('.bt-col-chk').forEach(function(chk) {
                chk.checked = _btCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.bt-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _btCustomCols.indexOf(col);
                if (this.checked && idx === -1) _btCustomCols.push(col);
                else if (!this.checked && idx !== -1) _btCustomCols.splice(idx, 1);
                try { localStorage.setItem(_btCustomColKey, JSON.stringify(_btCustomCols)); } catch(e) {}
                if (_btColMode === 'custom') _btApplyCustomCols();
            });
        });
        function _btApplyResponsive() {
            if (_btColMode !== 'auto') return;
            _btShowCols(window.innerWidth < 992 ? [3,4,5] : []);
        }
        function _btApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#btColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#btColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#btColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('bt-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('btCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _btApplyResponsive();
            else if (mode === 'all') _btShowCols([]);
            else if (mode === 'compact') _btShowCols(_btCompact);
            else if (mode === 'custom') { _btSyncChkBoxes(); _btApplyCustomCols(); }
        }
        $(window).on('resize', _btApplyResponsive);
        _btApplyMode(_btColMode);
        $('#btColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('bt-mode');
            if (!mode) return;
            _btColMode = mode;
            try { localStorage.setItem(_btColKey, mode); } catch(e) {}
            _btApplyMode(mode);
        });

var _dest = '<c:out value="${destination.name}"/>';

function _buildModalContent(action, search, filtered, total) {
var safeSearch = $('<span>').text(search).html();
var dest = '<strong>' + $('<span>').text(_dest).html() + '</strong>';
var isRequeue = action === 'requeue';
var baseUrl = '/do/admin/' + action + '/' + encodeURIComponent(_dest)
+ '?' + (isRequeue ? 'restart=true' : 'delete=true');
if (search) {
baseUrl += '&search=' + encodeURIComponent(search);
}
var count = search ? filtered : total;
var scope = search
? '<strong>' + count + '</strong> transfer(s) matching filter <strong>&ldquo;' + safeSearch + '&rdquo;</strong> for destination ' + dest
: '<strong>all ' + total + '</strong> outstanding transfer(s) for destination ' + dest;
var body = isRequeue
? 'Are you sure you want to requeue ' + scope + '?'
: 'Are you sure you want to <strong>permanently delete</strong> ' + scope + '? This action cannot be undone.';
return { url: baseUrl, body: body };
}

$('#requeueConfirmModal').on('show.bs.modal', function() {
var search = _table.search();
var info = _table.page.info();
var c = _buildModalContent('requeue', search, info.recordsDisplay, info.recordsTotal);
$('#requeueModalBody').html(c.body);
$('#requeueConfirmBtn').attr('href', c.url);
});

$('#deleteConfirmModal').on('show.bs.modal', function() {
var search = _table.search();
var info = _table.page.info();
var c = _buildModalContent('delete', search, info.recordsDisplay, info.recordsTotal);
$('#deleteModalBody').html(c.body);
$('#deleteConfirmBtn').attr('href', c.url);
});
});
</script>
</c:if>
