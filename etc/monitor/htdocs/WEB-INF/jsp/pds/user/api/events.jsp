<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select" />

<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-journal-code text-primary"></i>
    <span class="fw-semibold">API Events Log</span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#apiEventsInfo"
        aria-expanded="false" title="About this table">
        <i class="bi bi-info-circle"></i>
    </button>
    <div class="ms-auto d-flex align-items-center gap-2 flex-wrap">
      <div class="input-group input-group-sm flex-nowrap" style="width:auto">
        <span class="input-group-text"><i class="bi bi-key"></i></span>
        <input id="apiEventsClientFilter" class="form-control form-control-sm" type="text"
               placeholder="Client ID…" style="width:120px;min-width:80px"
               value="${clientFilter}">
      </div>
      <div class="input-group input-group-sm flex-nowrap" style="width:auto">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input id="apiEventsSearch" class="form-control form-control-sm" type="text"
               placeholder="Search service, host, message…" style="width:200px;min-width:100px"
               value="${param.search}">
      </div>
      <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
        <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
        <select id="apiEventsPageLen" class="form-select form-select-sm" style="width:auto">
          <option value="10">10</option>
          <option value="25">25</option>
          <option value="50">50</option>
          <option value="100">100</option>
          <option value="250">250</option>
        </select>
      </div>
      <div class="dropdown">
        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="aeColModeBtn"
                data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
          <i class="bi bi-layout-three-columns me-1"></i>Auto
        </button>
        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="aeColModeBtn">
          <li><a class="dropdown-item" href="#" data-ae-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><small class="d-block text-muted ms-4">Adapts to screen width</small></a></li>
          <li><a class="dropdown-item" href="#" data-ae-mode="all"><strong>All</strong><small class="d-block text-muted ms-0">All columns visible</small></a></li>
          <li><a class="dropdown-item" href="#" data-ae-mode="compact"><strong>Compact</strong><small class="d-block text-muted ms-0">Hides: Host, Message</small></a></li>
          <li><hr class="dropdown-divider"></li>
          <li><a class="dropdown-item" href="#" data-ae-mode="custom"><strong>Custom</strong><small class="d-block text-muted ms-0">Choose individual columns</small></a></li>
          <li id="aeCustomColChkPanel" style="display:none;">
            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="aechk-0">Time <small>(required)</small></label></div>
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-1" data-col="1" checked><label class="form-check-label" for="aechk-1">Client</label></div>
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="aechk-2">Service <small>(required)</small></label></div>
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-3" data-col="3" checked><label class="form-check-label" for="aechk-3">Host</label></div>
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-4" data-col="4" checked disabled><label class="form-check-label text-muted" for="aechk-4">Result <small>(required)</small></label></div>
              <div class="form-check mb-0"><input class="form-check-input ae-col-chk" type="checkbox" id="aechk-5" data-col="5" checked><label class="form-check-label" for="aechk-5">Message</label></div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
  <div class="collapse" id="apiEventsInfo">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">API Events Log &mdash; service access audit</strong>
        <p class="mb-1">Records every service access attempt made by API clients on the selected date. Each row represents one authentication and authorisation check.</p>
        <ul class="mb-1 ps-3">
            <li><strong>Client</strong> &mdash; the API client identifier used in the request.</li>
            <li><strong>Service</strong> &mdash; the service name being accessed (matched against the client's permission patterns).</li>
            <li><strong>Host</strong> &mdash; the host or IP address the request originated from.</li>
            <li><strong>Result</strong> &mdash; <span class="badge bg-success-subtle text-success-emphasis border">authorised</span> or <span class="badge bg-danger-subtle text-danger-emphasis border">denied</span> outcome of the access check.</li>
            <li><strong>Message</strong> &mdash; reason for denial (e.g. client disabled, invalid credentials, service not permitted) or empty on success.</li>
        </ul>
        <div class="text-muted">Use the Client ID filter to focus on one client. Use the search box to filter by service, host or message. Use the date selector to navigate between days.</div>
    </div>
  </div>
  <div class="card-body p-0">
    <div class="table-responsive">
      <table id="apiEventsTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
        <thead class="table-warning">
          <tr>
            <th title="Time (UTC) &mdash; date shown in selector above">Time</th>
            <th>Client</th>
            <th>Service</th>
            <th>Host</th>
            <th>Result</th>
            <th>Message</th>
          </tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>
  </div>
</div>

<script>
var _apiEventsTable;
function apiEventsTableReload() {
    if (_apiEventsTable) { _apiEventsTable.ajax.reload(); }
}
$(function() {
    var _aePageLen = (function() { try { var v = parseInt(localStorage.getItem('apiEventsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    _apiEventsTable = $('#apiEventsTable').DataTable({
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/user/api/events/list',
            type: 'GET',
            data: function(d) {
                d.date     = '${selectedDate}';
                d.clientId = $('#apiEventsClientFilter').val() || '';
                d.search   = $('#apiEventsSearch').val() || '';
            }
        },
        pageLength: _aePageLen,
        searching: false,
        autoWidth: false,
        order: [[0, 'desc']],
        columns: [
            { orderable: true,  data: 0, render: function(d) { return d || ''; } },
            { orderable: true,  data: 1, render: function(d) { return d || ''; } },
            { orderable: true,  data: 2, render: function(d) { return d || ''; } },
            { orderable: true,  data: 3, render: function(d) { return d || ''; } },
            { orderable: false, data: 4, render: function(d) { return d || ''; } },
            { orderable: false, data: 5, render: function(d) { return d || ''; } }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading…',
            emptyTable: 'No matching records found.'
        }
    });
    $('#apiEventsPageLen').val(_aePageLen).on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('apiEventsPageLen', len); } catch(e) {}
        _apiEventsTable.page.len(len).draw();
    });
    function _syncUrlAndReload() {
        var url = new URL(window.location);
        var s = $('#apiEventsSearch').val().trim();
        if (s) { url.searchParams.set('search', s); } else { url.searchParams.delete('search'); }
        history.replaceState(null, '', url.toString());
        apiEventsTableReload();
    }
    $('#apiEventsSearch, #apiEventsClientFilter').on('keydown', function(e) {
        if (e.key === 'Enter') { e.preventDefault(); _syncUrlAndReload(); }
    });

    /* ---- Column visibility ---- */
    var _aeColMode = (function() { try { return localStorage.getItem('aeColMode') || 'auto'; } catch(e) { return 'auto'; } })();
    var _aeCustomCols = (function() { try { var s = localStorage.getItem('aeCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5]; } catch(e) { return [0,1,2,3,4,5]; } })();
    function _aeShowCols(hide) { _apiEventsTable.columns().every(function(i) { _apiEventsTable.column(i).visible(hide.indexOf(i) === -1, false); }); _apiEventsTable.columns.adjust(); }
    function _aeApplyCustom() { _apiEventsTable.columns().every(function(i) { var req = i===0||i===2||i===4; _apiEventsTable.column(i).visible(req || _aeCustomCols.indexOf(i) !== -1, false); }); _apiEventsTable.columns.adjust(); }
    function _aeApplyAuto() { if (_aeColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _aeShowCols([1,3,5]); else if (w < 992) _aeShowCols([3,5]); else _aeShowCols([]); }
    function _aeApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#aeColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#aeColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        document.getElementById('aeCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
        $('#aeColModeBtn').closest('.dropdown').find('[data-ae-mode]').each(function() { $(this).find('i.bi-check').remove(); if ($(this).data('ae-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
        if (mode === 'auto') _aeApplyAuto();
        else if (mode === 'all') _aeShowCols([]);
        else if (mode === 'compact') _aeShowCols([3,5]);
        else if (mode === 'custom') { _aeApplyCustom(); document.querySelectorAll('.ae-col-chk').forEach(function(c) { c.checked = c.disabled || _aeCustomCols.indexOf(+c.dataset.col) !== -1; }); }
    }
    document.querySelectorAll('.ae-col-chk:not([disabled])').forEach(function(chk) {
        chk.addEventListener('change', function() { var col = +this.dataset.col; var idx = _aeCustomCols.indexOf(col); if (this.checked && idx === -1) _aeCustomCols.push(col); else if (!this.checked && idx !== -1) _aeCustomCols.splice(idx, 1); try { localStorage.setItem('aeCustomCols', JSON.stringify(_aeCustomCols)); } catch(e) {} if (_aeColMode === 'custom') _aeApplyCustom(); });
    });
    $('#aeColModeBtn').closest('.dropdown').on('click', '[data-ae-mode]', function(e) { e.preventDefault(); _aeColMode = $(this).data('ae-mode'); try { localStorage.setItem('aeColMode', _aeColMode); } catch(e) {} _aeApplyMode(_aeColMode); });
    $(window).on('resize', function() { if (_aeColMode === 'auto') _aeApplyAuto(); });
    _aeApplyMode(_aeColMode);

    // Keep date pill URLs in sync with the current search value so navigating
    // to another date always carries the search term even without clicking Search first.
    function _updateDatePillSearch() {
        var s = $('#apiEventsSearch').val().trim();
        $('.date-pill').each(function() {
            var href = this.href;
            var url = new URL(href, window.location.origin);
            if (s) { url.searchParams.set('search', s); } else { url.searchParams.delete('search'); }
            this.href = url.toString();
        });
    }
    $('#apiEventsSearch').on('input', _updateDatePillSearch);
    _updateDatePillSearch(); // run once on load for pre-filled value
});
</script>
