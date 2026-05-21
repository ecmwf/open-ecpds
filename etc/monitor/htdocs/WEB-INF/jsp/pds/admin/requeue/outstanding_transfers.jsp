<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-exclamation-triangle text-primary"></i>
<span class="fw-semibold">Outstanding Transfers</span>
<button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse" data-bs-target="#outstandingLegend" aria-expanded="false" title="What are outstanding transfers?">
<i class="bi bi-info-circle"></i>
</button>
<div class="ms-auto d-flex align-items-center gap-2">
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
</div>
</div>
<div class="collapse" id="outstandingLegend">
<div class="px-3 py-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-bottom:1px solid var(--bs-border-color);">
<strong class="d-block mb-1">Outstanding transfers include:</strong>
<ul class="mb-0 ps-3">
<li><span class="badge bg-warning text-dark me-1">WAIT</span> with at least one prior attempt &mdash; waiting to be retried</li>
<li><span class="badge bg-warning text-dark me-1">RETR</span> not stopped by a user, comment indicates a scheduler requeue or limit reached</li>
<li><span class="badge bg-secondary me-1">STOP</span> stopped by the system (not manually by a user)</li>
<li><span class="badge bg-danger me-1">FAIL</span> transfer failed</li>
</ul>
<div class="mt-1 text-muted">Deleted transfers and transfers manually stopped by a user are excluded.</div>
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="requeueTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
<thead class="table-light">
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
{ orderable: true,  data: 0 },
{ orderable: true,  data: 1 },
{ orderable: true,  data: 2 },
{ orderable: true,  data: 3 },
{ orderable: false, data: 4 },
{ orderable: false, data: 5 },
{ orderable: true,  data: 6 },
{ orderable: true,  data: 7 }
],
columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
createdRow: function(row, data) {
$('td', row).each(function(i) { $(this).html(data[i]); });
},
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
});
</script>
