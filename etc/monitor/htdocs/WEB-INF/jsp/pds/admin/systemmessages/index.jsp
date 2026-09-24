<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%
    final String smError = (String) request.getAttribute("smError");
    final long smNow = request.getAttribute("systemMessagesNow") != null
            ? (Long) request.getAttribute("systemMessagesNow") : System.currentTimeMillis();
%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-megaphone-fill text-secondary flex-shrink-0"></i>
<span>Time-bounded warning/maintenance messages, shown as a banner to every user on the Monitor UI landing page
(<code>/do/start</code>) and on the Data Portal, for as long as the current time falls within the configured
start/end window. Messages appear and disappear automatically &mdash; no manual cleanup is required for display
purposes, though entries remain listed below (for history) until deleted.</span>
</div>

<% if (smError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=smError%></span>
</div>
<% } %>

<style>
.sm-cell-truncate { max-width: 380px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.badge-level-info    { background: rgba(13,110,253,0.12); color: #0d6efd; border: 1px solid rgba(13,110,253,0.2); }
.badge-level-warning { background: rgba(255,193,7,0.12);  color: #996600; border: 1px solid rgba(255,193,7,0.3); }
.badge-level-danger  { background: rgba(220,53,69,0.12);  color: #dc3545; border: 1px solid rgba(220,53,69,0.2); }
.badge-status-active   { background: rgba(25,135,84,0.12); color: #198754; border: 1px solid rgba(25,135,84,0.2); }
.badge-status-scheduled{ background: rgba(13,110,253,0.12); color: #0d6efd; border: 1px solid rgba(13,110,253,0.2); }
.badge-status-expired  { background: rgba(108,117,125,0.12); color: #6c757d; border: 1px solid rgba(108,117,125,0.2); }
/* Keep the empty-table message at the standard (body-default) size, not the compact 0.82rem used for data rows */
#smTable td.dt-empty { font-size: 1rem; }
</style>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-megaphone-fill text-primary"></i>
<span class="fw-semibold">System Messages</span>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
  <div class="input-group input-group-sm" style="width:auto">
    <span class="input-group-text"><i class="bi bi-search"></i></span>
    <input type="text" id="smSearch" class="form-control" placeholder="Search..." style="min-width:160px">
  </div>
  <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="smPageLen" class="form-select form-select-sm" style="width:auto">
      <option value="10">10</option>
      <option value="25">25</option>
      <option value="50">50</option>
      <option value="100">100</option>
    </select>
  </div>
  <select id="smStatusFilter" class="form-select form-select-sm" style="width:auto">
    <option value="">All statuses</option>
    <option value="Active">Active</option>
    <option value="Scheduled">Scheduled</option>
    <option value="Expired">Expired</option>
  </select>
  <button type="button" class="btn btn-sm btn-outline-success" onclick="smOpenAdd()">
    <i class="bi bi-plus-circle"></i> Create
  </button>
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="smTable" class="table table-sm table-hover mb-0 align-middle" style="font-size:0.82rem;">
  <thead class="table-secondary">
    <tr>
      <th>Status</th>
      <th>Level</th>
      <th>Message</th>
      <th>Start (UTC)</th>
      <th>End (UTC)</th>
      <th>Created By</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach var="m" items="${systemMessages}">
  <%
      final ecmwf.common.database.SystemMessage _m = (ecmwf.common.database.SystemMessage) pageContext.getAttribute("m");
      final String _status = _m.getEndTime() < smNow ? "Expired" : _m.getStartTime() > smNow ? "Scheduled" : "Active";
      pageContext.setAttribute("smStatus", _status);
      final java.text.SimpleDateFormat _sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
      _sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      pageContext.setAttribute("smStartFmt", _sdf.format(new java.util.Date(_m.getStartTime())));
      pageContext.setAttribute("smEndFmt", _sdf.format(new java.util.Date(_m.getEndTime())));
  %>
  <tr data-sm-id="${m.id}" data-sm-message="<c:out value="${m.message}"/>" data-sm-level="${m.level}"
      data-sm-start="${m.startTime}" data-sm-end="${m.endTime}">
      <td><span class="badge badge-status-${smStatus == 'Active' ? 'active' : smStatus == 'Scheduled' ? 'scheduled' : 'expired'}"><c:out value="${smStatus}"/></span></td>
      <td><span class="badge badge-level-${m.level}"><c:out value="${m.level}"/></span></td>
      <td><div class="sm-cell-truncate" title="<c:out value="${m.message}"/>"><c:out value="${m.message}"/></div></td>
      <td class="text-nowrap text-muted" style="font-size:0.78rem;"><c:out value="${smStartFmt}"/></td>
      <td class="text-nowrap text-muted" style="font-size:0.78rem;"><c:out value="${smEndFmt}"/></td>
      <td><c:choose><c:when test="${not empty m.createdBy}"><c:out value="${m.createdBy}"/></c:when><c:otherwise>&mdash;</c:otherwise></c:choose></td>
      <td class="text-nowrap">
        <button type="button" class="btn btn-sm btn-outline-secondary me-1" onclick="smOpenEdit(${m.id})" title="Edit">
          <i class="bi bi-pencil-fill"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline-danger" onclick="smConfirmDelete(${m.id})" title="Delete">
          <i class="bi bi-trash-fill"></i>
        </button>
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>
</div>
</div>
</div>

<!-- Add/Edit modal -->
<div class="modal fade" id="smModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="smModalTitle">Add System Message</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="alert alert-danger d-none" id="smSaveError"></div>
                <div class="mb-3">
                    <label for="smMessage" class="form-label fw-semibold">Message</label>
                    <textarea class="form-control" id="smMessage" rows="3"
                              placeholder="e.g. The service will be unavailable for maintenance from 20:00 to 22:00 UTC."></textarea>
                </div>
                <div class="mb-3">
                    <label for="smLevel" class="form-label fw-semibold">Level</label>
                    <select class="form-select" id="smLevel">
                        <option value="info">Info (blue)</option>
                        <option value="warning" selected>Warning (orange)</option>
                        <option value="danger">Danger (red)</option>
                    </select>
                </div>
                <div class="row g-2 mb-2">
                  <div class="col-6">
                    <label for="smStart" class="form-label fw-semibold">Start</label>
                    <input type="datetime-local" class="form-control" id="smStart">
                  </div>
                  <div class="col-6">
                    <label for="smEnd" class="form-label fw-semibold">End</label>
                    <input type="datetime-local" class="form-control" id="smEnd">
                  </div>
                </div>
                <div class="form-text text-muted mb-2">Times are in your browser's local timezone. The message is
                    shown automatically while the current time is within this window, and disappears once it ends.</div>
                <div class="d-flex flex-wrap gap-1">
                    <span class="text-muted me-1" style="font-size:0.8rem;">Quick duration:</span>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="smQuickDuration(1)">+1h</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="smQuickDuration(6)">+6h</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="smQuickDuration(24)">+1d</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="smQuickDuration(24*7)">+1 week</button>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning" id="smSaveBtn" onclick="smSave()">
                    <i class="bi bi-save-fill me-1"></i>Save
                </button>
            </div>
        </div>
    </div>
</div>

<script>
var _smModal = null;
var _smEditingId = null; // null = adding a new message; otherwise the id of the message being edited
var _smDeleteId = null;

document.addEventListener('DOMContentLoaded', function() {
  _smModal = new bootstrap.Modal(document.getElementById('smModal'));

  var _len = (function() { try { var v = parseInt(localStorage.getItem('smPageLen'), 10); return [10,25,50,100].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
  $('#smPageLen').val(_len);

  var table = $('#smTable').DataTable({
    order:      [[3, 'desc']],
    pageLength: _len,
    searching:  true,
    info:       true,
    dom:        't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
    columnDefs: [{ orderable: false, targets: [6] }],
    language: {
      info:       'Showing _START_-_END_ of _TOTAL_ entries',
      emptyTable: 'No system messages configured.'
    }
  });

  $('#smPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('smPageLen', len); } catch(e) {}
    table.page.len(len).draw();
  });

  $('#smSearch').on('keyup', function() { table.search(this.value).draw(); });

  // Status column (index 0) filter
  $('#smStatusFilter').on('change', function() {
    table.column(0).search(this.value).draw();
  });
});

function _smToLocalInputValue(epochMs) {
  var d = new Date(epochMs);
  var pad = function(n) { return (n < 10 ? '0' : '') + n; };
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
       + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
}

function smQuickDuration(hours) {
  var startEl = document.getElementById('smStart');
  var start = startEl.value ? new Date(startEl.value) : new Date();
  if (!startEl.value) startEl.value = _smToLocalInputValue(start.getTime());
  document.getElementById('smEnd').value = _smToLocalInputValue(start.getTime() + hours * 3600000);
}

function smOpenAdd() {
  _smEditingId = null;
  document.getElementById('smModalTitle').textContent = 'Add System Message';
  document.getElementById('smMessage').value = '';
  document.getElementById('smLevel').value = 'warning';
  var now = new Date();
  document.getElementById('smStart').value = _smToLocalInputValue(now.getTime());
  document.getElementById('smEnd').value = _smToLocalInputValue(now.getTime() + 3600000);
  document.getElementById('smSaveError').classList.add('d-none');
  _smModal.show();
}

function smOpenEdit(id) {
  var row = document.querySelector('#smTable tr[data-sm-id="' + id + '"]');
  if (!row) return;
  _smEditingId = id;
  document.getElementById('smModalTitle').textContent = 'Edit System Message';
  document.getElementById('smMessage').value = row.dataset.smMessage || '';
  document.getElementById('smLevel').value = row.dataset.smLevel || 'warning';
  document.getElementById('smStart').value = _smToLocalInputValue(parseInt(row.dataset.smStart, 10));
  document.getElementById('smEnd').value = _smToLocalInputValue(parseInt(row.dataset.smEnd, 10));
  document.getElementById('smSaveError').classList.add('d-none');
  _smModal.show();
}

function smSave() {
  var btn = document.getElementById('smSaveBtn');
  var errEl = document.getElementById('smSaveError');
  errEl.classList.add('d-none');

  var message = document.getElementById('smMessage').value.trim();
  var level = document.getElementById('smLevel').value;
  var startVal = document.getElementById('smStart').value;
  var endVal = document.getElementById('smEnd').value;

  if (!message) {
    errEl.textContent = 'Message text is required.';
    errEl.classList.remove('d-none');
    return;
  }
  if (!startVal || !endVal) {
    errEl.textContent = 'Start and End are required.';
    errEl.classList.remove('d-none');
    return;
  }
  var startTime = new Date(startVal).getTime();
  var endTime = new Date(endVal).getTime();
  if (endTime <= startTime) {
    errEl.textContent = 'End time must be after start time.';
    errEl.classList.remove('d-none');
    return;
  }

  var payload = { message: message, level: level, startTime: startTime, endTime: endTime };
  if (_smEditingId != null) payload.id = _smEditingId;

  btn.disabled = true;
  fetch('<bean:message key="admin.basepath"/>/systemmessages/save', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify(payload)
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      btn.disabled = false;
      if (data.success) {
        _smModal.hide();
        window.location.reload();
      } else {
        errEl.textContent = data.error || 'Save failed';
        errEl.classList.remove('d-none');
      }
    }).catch(function(e) {
      btn.disabled = false;
      errEl.textContent = 'Network error: ' + e.message;
      errEl.classList.remove('d-none');
    });
}

function smConfirmDelete(id) {
  _smDeleteId = id;
  confirmationDialog({
    title: 'Delete System Message',
    message: 'Delete this system message? It will immediately stop being shown, even if still within its time window.',
    confirmText: 'Delete',
    showLoading: false,
    onConfirm: smDelete
  });
}

function smDelete() {
  fetch('<bean:message key="admin.basepath"/>/systemmessages/delete', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({ id: _smDeleteId })
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      if (data.success) {
        window.location.reload();
      } else {
        alert('Delete failed: ' + (data.error || 'unknown'));
      }
    }).catch(function(e) {
      alert('Network error: ' + e.message);
    });
}
</script>
