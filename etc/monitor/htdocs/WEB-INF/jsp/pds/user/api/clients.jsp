<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(255,193,7,0.08); color:var(--bs-body-color); border-left:4px solid #ffc107;">
<i class="bi bi-key text-warning flex-shrink-0"></i>
<span>API Clients are applications or scripts that access OpenECPDS programmatically using token-based authentication. Each client has a unique key and can be granted specific access permissions.</span>
</div>

<script>
function validate(path, message) {
    confirmationDialog({
        title: "Please Confirm",
        message: message,
        onConfirm: function () { window.location = path; },
        onCancel: function () {}
    });
}
</script>

<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-key text-primary"></i>
    <span class="fw-semibold">API Clients</span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
            data-bs-toggle="collapse" data-bs-target="#apiClientsInfo"
            aria-expanded="false" title="About this page">
      <i class="bi bi-info-circle"></i>
    </button>
    <button id="apiNoPermsBtn" type="button"
            class="btn btn-sm btn-outline-secondary"
            title="Toggle: show only clients with no service permissions">
      <i class="bi bi-exclamation-triangle-fill me-1"></i>No permissions only
    </button>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
      <div class="input-group input-group-sm" style="width:auto">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input type="text" id="apiClientSearch" class="form-control" placeholder="Search…"
               autocomplete="off" style="min-width:120px">
      </div>
      <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
        <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
        <select id="apiClientPageLen" class="form-select form-select-sm" style="width:auto">
          <option value="10">10</option>
          <option value="25">25</option>
          <option value="50">50</option>
          <option value="100">100</option>
          <option value="250">250</option>
        </select>
      </div>
      <div class="dropdown">
        <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="acColModeBtn"
                data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
          <i class="bi bi-layout-three-columns me-1"></i>Auto
        </button>
        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="acColModeBtn">
          <li><a class="dropdown-item" href="#" data-ac-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
          <li><a class="dropdown-item" href="#" data-ac-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
          <li><a class="dropdown-item" href="#" data-ac-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Created, Last IP</small></a></li>
          <li><hr class="dropdown-divider"></li>
          <li><a class="dropdown-item" href="#" data-ac-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
          <li id="acCustomColChkPanel" style="display:none;">
            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="acchk-0">Client ID <small>(required)</small></label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-1" data-col="1" checked><label class="form-check-label" for="acchk-1">Comment</label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-2" data-col="2" checked><label class="form-check-label" for="acchk-2">Country</label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-3" data-col="3" checked disabled><label class="form-check-label text-muted" for="acchk-3">Enabled <small>(required)</small></label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-4" data-col="4" checked><label class="form-check-label" for="acchk-4">Created</label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-5" data-col="5" checked><label class="form-check-label" for="acchk-5">Last Used</label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-6" data-col="6" checked><label class="form-check-label" for="acchk-6">Last IP</label></div>
              <div class="form-check mb-0"><input class="form-check-input ac-col-chk" type="checkbox" id="acchk-7" data-col="7" checked disabled><label class="form-check-label text-muted" for="acchk-7">Actions <small>(required)</small></label></div>
            </div>
          </li>
        </ul>
      </div>
      <a href="<bean:message key="apiclient.basepath"/>/edit/insert_form"
         class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
  </div>

  <%-- Info panel --%>
  <div class="collapse" id="apiClientsInfo">
    <div class="card-body py-2 px-3 border-bottom"
         style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
      <strong class="d-block mb-1">REST API Clients</strong>
      <p class="mb-1">This page manages clients that authenticate to the REST API using <strong>HTTP Basic Auth</strong>
        with their Client ID and Secret (<code>clientId:secret</code>).</p>
      <ul class="mb-1 ps-3">
        <li><strong>Client ID</strong> &mdash; unique identifier used as the HTTP Basic Auth username.</li>
        <li><strong>Secret</strong> &mdash; generated at creation time and shown <em>once only</em>.
          Use <em>Reset Secret</em> on the client detail page to generate a new one.</li>
        <li><strong>Enabled</strong> &mdash; inactive clients are rejected at authentication time.</li>
        <li><strong>Service Permissions</strong> &mdash; each client must have at least one permission pattern
          (regex) that matches the service name being invoked. Managed on the client detail page.</li>
        <li><strong>Last Used / Last IP</strong> &mdash; updated on every successful authentication.</li>
      </ul>
      <p class="mb-0 text-muted">Clients originally configured in the <code>[API]</code> section of the Cnf file
        are automatically migrated to this database on startup.</p>
    </div>
  </div>

  <div class="card-body p-0">
    <div class="table-responsive">
      <table id="apiClientTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
        <thead class="table-warning">
          <tr>
            <th>Client ID</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
            <th>Created</th>
            <th>Last Used</th>
            <th>Last IP</th>
            <th class="text-center no-sort">Actions</th>
          </tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>
  </div>
</div>

<script>
$(function () {
    var _acBasePath = '<bean:message key="apiclient.basepath"/>';
    var _acPageLen = (function () {
        try { var v = parseInt(localStorage.getItem('apiClientPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; }
    })();
    var _acSearch = '';
    var _acNoPermsOnly = false;

    var _acTable = $('#apiClientTable').DataTable({
        serverSide: false,
        processing: true,
        ajax: {
            url: '/do/user/api/list',
            type: 'GET',
            data: function(d) {
                return {
                    draw: d.draw,
                    start: d.start,
                    length: d.length,
                    search: _acSearch,
                    noPerms: _acNoPermsOnly ? 'true' : ''
                };
            },
            dataSrc: function(json) {
                return json.data || [];
            }
        },
        pageLength: _acPageLen,
        searching: false,
        autoWidth: false,
        order: [[0, 'asc']],
        columnDefs: [{ orderable: false, targets: [7] }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            emptyTable: 'No matching records found.',
            zeroRecords: 'No matching records found.',
            processing: '<div class="spinner-border spinner-border-sm text-secondary" role="status"><span class="visually-hidden">Loading...</span></div>'
        },
        columns: [
            { data: null, render: function(row) {
                return '<a href="' + _acBasePath + '/' + escHtml(row.id) + '"><code>' + escHtml(row.id) + '</code>'
                     + (row.noPerms ? ' <i class="bi bi-exclamation-triangle-fill text-warning ms-1" title="No permissions configured"></i>' : '')
                     + '</a>';
            }},
            { data: 'comment', render: function(v) { return v ? escHtml(v) : ''; } },
            { data: 'countryHtml', render: function(v) { return v || ''; } },
            { data: 'active', className: 'text-center', render: function(v) {
                return v ? '<span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span>'
                         : '<span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span>';
            }},
            { data: 'created', render: function(v) { return v ? '<small>' + escHtml(v) + '</small>' : ''; } },
            { data: 'lastUsed', render: function(v) { return v ? '<small>' + escHtml(v) + '</small>' : ''; } },
            { data: 'lastUsedHost', render: function(v) { return v ? '<small>' + escHtml(v) + '</small>' : ''; } },
            { data: null, className: 'text-center', orderable: false, render: function(row, type, full, meta) {
                var json = meta.settings.json || {};
                var canEdit = json.canEdit !== false;
                var canDelete = json.canDelete !== false;
                var id = escHtml(row.id);
                var html = '<a href="' + _acBasePath + '/' + id + '" class="btn btn-sm btn-outline-primary" title="View"><i class="bi bi-eye"></i></a>';
                if (canEdit) html += ' <a href="' + _acBasePath + '/' + id + '/edit/update_form" class="btn btn-sm btn-outline-secondary ms-1" title="Edit"><i class="bi bi-pencil"></i></a>';
                if (canDelete) html += ' <a href="' + _acBasePath + '/' + id + '/edit/delete" class="btn btn-sm btn-outline-danger ms-1" title="Delete" onclick="validate(this.href,\'Delete API client <strong>' + id.replace(/'/g, '\\&#39;') + '</strong>? This cannot be undone.\');return false;"><i class="bi bi-trash"></i></a>';
                return html;
            }}
        ],
        createdRow: function(row, data) {
            if (data.noPerms) row.setAttribute('data-no-perms', 'true');
        },
        initComplete: function() {
            /* store json ref on settings so action renderer can read canEdit/canDelete */
            var api = this.api();
            api.on('xhr.dt', function(e, settings, json) {
                settings.json = json;
            });
        }
    });

    function escHtml(s) {
        return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    /* Search (ID + Comment, server-side) */
    var _acSearchTimer;
    $('#apiClientSearch').on('input', function () {
        _acSearch = this.value;
        clearTimeout(_acSearchTimer);
        _acSearchTimer = setTimeout(function() { _acTable.ajax.reload(); }, 300);
    });

    /* No-permissions filter */
    $('#apiNoPermsBtn').on('click', function() {
        _acNoPermsOnly = !_acNoPermsOnly;
        $(this).toggleClass('btn-outline-secondary', !_acNoPermsOnly)
               .toggleClass('btn-warning', _acNoPermsOnly);
        _acTable.ajax.reload();
    });

    /* Page length */
    $('#apiClientPageLen').val(_acPageLen).on('change', function () {
        var len = +this.value;
        try { localStorage.setItem('apiClientPageLen', len); } catch(e) {}
        _acTable.page.len(len).draw();
    });

    /* Column visibility — cols: 0=ID, 1=Comment, 2=Status, 3=Country, 4=Created, 5=LastUsed, 6=LastIP, 7=Actions */
    var _acColMode = (function () { try { return localStorage.getItem('acColMode') || 'auto'; } catch(e) { return 'auto'; } })();
    var _acCustomCols = (function () { try { var s = localStorage.getItem('acCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5,6,7]; } catch(e) { return [0,1,2,3,4,5,6,7]; } })();
    function _acShowCols(hide) { _acTable.columns().every(function(i) { _acTable.column(i).visible(hide.indexOf(i) === -1, false); }); _acTable.columns.adjust(); }
    function _acApplyCustom() { _acTable.columns().every(function(i) { var req = i===0||i===3||i===7; _acTable.column(i).visible(req || _acCustomCols.indexOf(i) !== -1, false); }); _acTable.columns.adjust(); }
    function _acApplyAuto() { if (_acColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _acShowCols([1,2,4,5,6]); else if (w < 992) _acShowCols([2,4,5,6]); else _acShowCols([]); }
    function _acApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#acColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#acColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        document.getElementById('acCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
        $('#acColModeBtn').closest('.dropdown').find('[data-ac-mode]').each(function () { $(this).find('i.bi-check').remove(); if ($(this).data('ac-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
        if (mode === 'auto') _acApplyAuto();
        else if (mode === 'all') _acShowCols([]);
        else if (mode === 'compact') _acShowCols([1,2,4,6]);
        else if (mode === 'custom') { _acApplyCustom(); document.querySelectorAll('.ac-col-chk').forEach(function(c) { c.checked = c.disabled || _acCustomCols.indexOf(+c.dataset.col) !== -1; }); }
    }
    document.querySelectorAll('.ac-col-chk:not([disabled])').forEach(function(chk) {
        chk.addEventListener('change', function () { var col = +this.dataset.col; var idx = _acCustomCols.indexOf(col); if (this.checked && idx === -1) _acCustomCols.push(col); else if (!this.checked && idx !== -1) _acCustomCols.splice(idx, 1); try { localStorage.setItem('acCustomCols', JSON.stringify(_acCustomCols)); } catch(e) {} if (_acColMode === 'custom') _acApplyCustom(); });
    });
    $('#acColModeBtn').closest('.dropdown').on('click', '[data-ac-mode]', function (e) { e.preventDefault(); _acColMode = $(this).data('ac-mode'); try { localStorage.setItem('acColMode', _acColMode); } catch(e) {} _acApplyMode(_acColMode); });
    $(window).on('resize', function () { if (_acColMode === 'auto') _acApplyAuto(); });
    _acApplyMode(_acColMode);
});
</script>
