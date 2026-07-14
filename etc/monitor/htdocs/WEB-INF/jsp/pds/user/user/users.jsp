<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Web Users</span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#webUsersInfo"
        aria-expanded="false" title="About this page">
        <i class="bi bi-info-circle"></i>
    </button>
    <button id="btnWebUsrQB" type="button" class="btn btn-sm btn-outline-primary position-relative"
            onclick="toggleQBPanel('webUsrQueryBuilder','btnWebUsrQB')" title="Open query builder to filter Web Users">
        <i class="bi bi-sliders2"></i> Filter
        <span id="btnWebUsrQB-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;font-size:0.65rem"></span>
    </button>
    <button id="webUserMonitorNoDestBtn" type="button"
            class="btn btn-sm btn-outline-secondary"
            title="Show only Monitor users with no destination category assigned">
        <i class="bi bi-exclamation-triangle-fill me-1"></i>Monitor, no destination
    </button>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="usersWebSearch" class="form-control" placeholder="Search web login..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="usersWebPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="webUsrColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="webUsrColModeBtn">
                        <li><a class="dropdown-item" href="#" data-webusr-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-webusr-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-webusr-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Categories</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-webusr-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="webUsrCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="webUsrchk-0">Web Login <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-1" data-col="1" checked><label class="form-check-label" for="webUsrchk-1">Comment</label></div>
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-2" data-col="2" checked><label class="form-check-label" for="webUsrchk-2">Enabled</label></div>
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-3" data-col="3" checked><label class="form-check-label" for="webUsrchk-3">Categories</label></div>
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-4" data-col="4" checked><label class="form-check-label" for="webUsrchk-4">Monitor</label></div>
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-5" data-col="5" checked disabled><label class="form-check-label text-muted" for="webUsrchk-5">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="user.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>

<%-- Web Users Query Builder floating panel --%>
<div id="webUsrQueryBuilder" class="border rounded p-2"
     style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
    <div class="row g-1 mb-1">
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-toggle-on me-1 text-muted"></i>Enabled</label>
            <select class="form-select form-select-sm" id="wuqb_enabled">
                <option value="">Any</option>
                <option value="yes">Yes</option>
                <option value="no">No</option>
            </select>
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-sliders me-1 text-muted"></i>Properties editor</label>
            <select class="form-select form-select-sm" id="wuqb_propErrors">
                <option value="">Any</option>
                <option value="yes">Has errors</option>
            </select>
        </div>
        <div class="col-12">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-display me-1 text-muted"></i>Monitor status</label>
            <select class="form-select form-select-sm" id="wuqb_monitor">
                <option value="">Any</option>
                <option value="not-monitor">Not a monitor user</option>
                <option value="ok">Monitor &mdash; OK</option>
                <option value="no-dest">Monitor &mdash; no destination</option>
            </select>
        </div>
    </div>
    <div class="d-flex gap-1 pt-1 border-top mt-1 justify-content-end">
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="wuqbClear()">
            <i class="bi bi-x-circle me-1"></i>Clear
        </button>
        <button type="button" class="btn btn-sm btn-primary" onclick="wuqbApply()">
            <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
        </button>
    </div>
</div>

<div class="collapse" id="webUsersInfo">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">Web Users &mdash; search and filter</strong>
        <p class="mb-1">The <strong>Monitor</strong> column classifies users whose categories are exclusively from the monitor set ({mstate, monitoring, transfers, requirements}, optionally with <em>* operations</em> destination categories):</p>
        <ul class="mb-1 ps-3">
            <li><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-exclamation-triangle-fill me-1"></i>No destination</span> &mdash; monitor user with no <em>* operations</em> destination category; they can view monitoring pages but cannot access any destination.</li>
            <li><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>OK</span> &mdash; monitor user with at least one destination category.</li>
        </ul>
        <div class="text-muted">Use the <strong>Monitor, no destination</strong> button to filter just the problematic users. The Monitor column is sortable to group them.</div>
    </div>
</div>

<div class="card-body p-0">
<div id="webUserBulkMsg" style="display:none;position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:9999;min-width:320px;max-width:480px;width:90%;box-shadow:0 8px 32px rgba(0,0,0,0.25);"></div>
<div class="table-responsive">
<table id="usersWebTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-warning">
        <tr>
            <th>Web Login</th>
            <th>Comment</th>
            <th class="text-center">Enabled</th>
            <th>Categories</th>
            <th class="text-center">Monitor</th>
            <th class="text-center no-sort">Actions</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>
</div>
</div>
<div class="mt-3">
    <button id="webUserDeleteAllBtn" type="button"
            class="btn btn-outline-danger d-none"
            title="Delete all currently listed Monitor Web Users with no destination">
        <i class="bi bi-trash-fill me-1"></i>Delete All
    </button>
</div>
<script>
// ---- QB helpers in global scope so onclick attributes can call them ----
var _monitorNoDestOnly = false;
var _webUsrTable = null;
var _WU_QB_KEY = 'webUsrQB';
function _wuqbSave() { try { localStorage.setItem(_WU_QB_KEY, JSON.stringify({en:wuqbVal('wuqb_enabled'),mo:wuqbVal('wuqb_monitor'),pe:wuqbVal('wuqb_propErrors')})); } catch(e) {} }
function _wuqbRestore() { try { var s=localStorage.getItem(_WU_QB_KEY); if(!s) return; var q=JSON.parse(s); ['wuqb_enabled','wuqb_monitor','wuqb_propErrors'].forEach(function(id,i){ var v=[q.en,q.mo,q.pe][i]; var el=document.getElementById(id); if(el&&v) el.value=v; }); } catch(e) {} }
function wuqbVal(id) { var el = document.getElementById(id); return el ? el.value : ''; }
function _buildAjaxUrl() {
    var params = [];
    if (_monitorNoDestOnly) params.push('monitorNoDestination=true');
    var en = wuqbVal('wuqb_enabled');    if (en) params.push('enabled='    + encodeURIComponent(en));
    var mo = wuqbVal('wuqb_monitor');    if (mo) params.push('monitor='    + encodeURIComponent(mo));
    var pe = wuqbVal('wuqb_propErrors'); if (pe) params.push('propErrors=' + encodeURIComponent(pe));
    return '/do/user/user/list' + (params.length ? '?' + params.join('&') : '');
}
function wuqbCountActive() {
    var n = 0;
    if (wuqbVal('wuqb_enabled'))    n++;
    if (wuqbVal('wuqb_monitor'))    n++;
    if (wuqbVal('wuqb_propErrors')) n++;
    return n;
}
function wuqbUpdateBadge() {
    var n = wuqbCountActive();
    var b = document.getElementById('btnWebUsrQB-badge');
    if (b) { b.textContent = n; b.style.display = n > 0 ? '' : 'none'; }
    var btn = document.getElementById('btnWebUsrQB');
    if (btn) { btn.classList.toggle('btn-outline-primary', n === 0); btn.classList.toggle('btn-warning', n > 0); }
}
function wuqbApply() {
    wuqbUpdateBadge();
    _wuqbSave();
    var p = document.getElementById('webUsrQueryBuilder'); if (p) p.style.display = 'none';
    if (_webUsrTable) _webUsrTable.ajax.url(_buildAjaxUrl()).load();
}
function wuqbReload() { wuqbUpdateBadge(); if (_webUsrTable) _webUsrTable.ajax.url(_buildAjaxUrl()).load(); }
function wuqbClear() {
    ['wuqb_enabled','wuqb_monitor','wuqb_propErrors'].forEach(function(id) { var el=document.getElementById(id); if(el) el.value=''; });
    var p = document.getElementById('webUsrQueryBuilder'); if (p) p.style.display = 'none';
    try { localStorage.removeItem(_WU_QB_KEY); } catch(e) {}
    wuqbUpdateBadge();
    if (_webUsrTable) _webUsrTable.ajax.url(_buildAjaxUrl()).load();
}
function toggleQBPanel(panelId, btnId) {
    var panel = document.getElementById(panelId);
    var btn = document.getElementById(btnId);
    if (!panel || !btn) return;
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
    var vw = window.innerWidth || document.documentElement.clientWidth;
    var pw = Math.min(400, vw - 16);
    panel.style.width = pw + 'px';
    var r = btn.getBoundingClientRect();
    var sy = window.pageYOffset || document.documentElement.scrollTop;
    panel.style.top = (r.bottom + sy + 4) + 'px';
    panel.style.left = Math.max(8, r.right - pw + window.pageXOffset) + 'px';
    panel.style.display = 'block';
}
document.addEventListener('click', function(e) {
    var panel = document.getElementById('webUsrQueryBuilder');
    var btn = document.getElementById('btnWebUsrQB');
    if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
        panel.style.display = 'none';
    }
});

$(document).ready(function() {
    _wuqbRestore();
    wuqbUpdateBadge();
    var _canDelete = false;
    var _filteredCount = 0;
    var table = $('#usersWebTable').DataTable({
        ajax: {
            url: _buildAjaxUrl(),
            dataSrc: function(json) {
                _canDelete = !!json.canDelete;
                _filteredCount = json.recordsFiltered || 0;
                return json.data;
            }
        },
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        language:   { emptyTable: 'No Web Users found.' },
        columnDefs: [
            { orderable: false, targets: [5] },
            { orderData: [6], targets: [2] },
            { orderData: [7], targets: [4] },
            { visible: false, targets: [6, 7] }
        ],
        drawCallback: function() { _updateDeleteAllBtn(); _setFilterLoading($('#webUserMonitorNoDestBtn'), false); },
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    _webUsrTable = table;
    var _len = (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#usersWebPageLen').val(_len);
    $('#usersWebPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('usersWebPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#usersWebSearch').on('keyup', function() { table.column(0).search(this.value).draw(); });

    function _setFilterLoading($btn, loading) {
        if (loading) {
            $btn.prop('disabled', true)
                .prepend('<span class="spinner-border spinner-border-sm me-1 _filter-spinner" role="status" aria-hidden="true"></span>');
        } else {
            $btn.find('._filter-spinner').remove();
            $btn.prop('disabled', false);
        }
    }

    $('#webUserMonitorNoDestBtn').on('click', function() {
        _monitorNoDestOnly = !_monitorNoDestOnly;
        $(this).toggleClass('btn-outline-secondary', !_monitorNoDestOnly)
               .toggleClass('btn-warning', _monitorNoDestOnly);
        if (!_monitorNoDestOnly) $('#webUserDeleteAllBtn').addClass('d-none');
        _setFilterLoading($(this), true);
        table.ajax.url(_buildAjaxUrl()).load();
    });

    function _updateDeleteAllBtn() {
        var show = _monitorNoDestOnly && _canDelete && _filteredCount > 0;
        $('#webUserDeleteAllBtn').toggleClass('d-none', !show);
        if (show) {
            $('#webUserDeleteAllBtn').html('<i class="bi bi-trash-fill me-1"></i>Delete All (' + _filteredCount + ')');
        }
    }

    function _showBulkMsg(type, html) {
        var $m = $('#webUserBulkMsg');
        $m.attr('class', 'alert alert-' + type + ' alert-dismissible d-flex align-items-center gap-2 p-3 mb-0 rounded-3');
        var icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';
        $m.html('<i class="bi ' + icon + ' flex-shrink-0"></i><div class="flex-grow-1">' + html + '</div>'
              + '<button type="button" class="btn-close p-2" data-bs-dismiss="alert" aria-label="Close"></button>');
        $m.show();
        setTimeout(function() { $m.fadeOut(); }, 6000);
    }

    $('#webUserDeleteAllBtn').on('click', function() {
        var n = _filteredCount;
        var label = n === 1 ? '1 Monitor Web User with no destination' : 'all ' + n + ' Monitor Web Users with no destination';
        confirmationDialog({
            title: 'Delete Monitor Web Users',
            message: 'Delete ' + label + '? This action cannot be undone.',
            confirmText: 'Delete',
            showLoading: false,
            onConfirm: function() {
                $.ajax({
                    url: '/do/user/user/edit/deleteAllMonitorNoDest',
                    method: 'GET',
                    success: function(data) {
                        var msg = 'Deleted ' + data.deleted + ' Web User' + (data.deleted !== 1 ? 's' : '');
                        if (data.errors > 0) {
                            msg += ' &mdash; ' + data.errors + ' could not be deleted.';
                            _showBulkMsg('warning', msg);
                        } else {
                            _showBulkMsg('success', msg + '.');
                        }
                        table.ajax.url(_buildAjaxUrl()).load();
                    },
                    error: function() { _showBulkMsg('danger', 'Error performing bulk delete. Please try again.'); }
                });
            }
        });
    });

        /* ---- Cols:Auto ---- */
        var _webUsrColKey        = 'webUsrColMode';
        var _webUsrCustomColKey  = 'webUsrCustomCols';
        var _webUsrCompact       = [1, 3];
        var _webUsrColMode = (function() { try { return localStorage.getItem(_webUsrColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _webUsrCustomCols = (function() {
            try { var s = localStorage.getItem(_webUsrCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4, 5];
        })();
        function _webUsrShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 6) continue;
                table.column(i).visible(hideCols.indexOf(i) === -1, false);
            }
            table.columns.adjust();
        }
        function _webUsrApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 6) continue;
                table.column(i).visible((i === 0 || i === 5) ? true : _webUsrCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _webUsrSyncChkBoxes() {
            document.querySelectorAll('.webUsr-col-chk').forEach(function(chk) {
                chk.checked = _webUsrCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.webUsr-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _webUsrCustomCols.indexOf(col);
                if (this.checked && idx === -1) _webUsrCustomCols.push(col);
                else if (!this.checked && idx !== -1) _webUsrCustomCols.splice(idx, 1);
                try { localStorage.setItem(_webUsrCustomColKey, JSON.stringify(_webUsrCustomCols)); } catch(e) {}
                if (_webUsrColMode === 'custom') _webUsrApplyCustomCols();
            });
        });
        function _webUsrApplyResponsive() {
            if (_webUsrColMode !== 'auto') return;
            _webUsrShowCols(window.innerWidth < 768 ? [1, 3] : []);
        }
        function _webUsrApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#webUsrColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#webUsrColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#webUsrColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('webusr-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('webUsrCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _webUsrApplyResponsive();
            else if (mode === 'all') _webUsrShowCols([]);
            else if (mode === 'compact') _webUsrShowCols(_webUsrCompact);
            else if (mode === 'custom') { _webUsrSyncChkBoxes(); _webUsrApplyCustomCols(); }
        }
        $(window).on('resize', _webUsrApplyResponsive);
        _webUsrApplyMode(_webUsrColMode);
        $('#webUsrColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('webusr-mode');
            if (!mode) return;
            _webUsrColMode = mode;
            try { localStorage.setItem(_webUsrColKey, mode); } catch(e) {}
            _webUsrApplyMode(mode);
        });
});
</script>
