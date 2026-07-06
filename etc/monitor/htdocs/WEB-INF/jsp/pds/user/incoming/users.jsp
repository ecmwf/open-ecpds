<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Data Users</span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#incomingUsersInfo"
        aria-expanded="false" title="About this page">
        <i class="bi bi-info-circle"></i>
    </button>
    <button id="btnIncUsrQB" type="button" class="btn btn-sm btn-outline-primary position-relative"
            onclick="toggleIQBPanel('incUsrQueryBuilder','btnIncUsrQB')" title="Open query builder to filter Data Users">
        <i class="bi bi-sliders2"></i> Filter
        <span id="btnIncUsrQB-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;font-size:0.65rem"></span>
    </button>
    <button id="incomingUnassignedBtn" type="button"
            class="btn btn-sm btn-outline-secondary"
            title="Show only Data Users with no destinations (direct or via policy)">
        <i class="bi bi-exclamation-triangle-fill me-1"></i>Unassigned only
    </button>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="incomingSearch" class="form-control" placeholder="Search data login..."
                   autocomplete="off" style="min-width:120px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="incomingPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="incUsrColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="incUsrColModeBtn">
                        <li><a class="dropdown-item" href="#" data-incusr-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-incusr-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-incusr-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Country, TOTP, Anonymous, Sessions</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-incusr-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="incUsrCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="incUsrchk-0">Data Login <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-1" data-col="1" checked><label class="form-check-label" for="incUsrchk-1">Comment</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-2" data-col="2" checked><label class="form-check-label" for="incUsrchk-2">Country</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-3" data-col="3" checked><label class="form-check-label" for="incUsrchk-3">Enabled</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-4" data-col="4" checked><label class="form-check-label" for="incUsrchk-4">TOTP</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-5" data-col="5" checked><label class="form-check-label" for="incUsrchk-5">Anonymous</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-6" data-col="6" checked><label class="form-check-label" for="incUsrchk-6">Sessions</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-7" data-col="7" checked disabled><label class="form-check-label text-muted" for="incUsrchk-7">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="incoming.basepath"/>/edit/insert_form"
           class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>

<%-- Data Users Query Builder floating panel --%>
<div id="incUsrQueryBuilder" class="border rounded p-2"
     style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
    <div class="row g-1 mb-1">
        <div class="col-12">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-arrow-left-right me-1 text-muted"></i>Destination</label>
            <c:set var="destParam" value="destinationNameForSearch" scope="request"/>
            <tiles:insert name="destination.select" />
        </div>
        <div class="col-12">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-shield-check me-1 text-muted"></i>Policy</label>
            <jsp:include page="/WEB-INF/jsp/pds/common/policy_select.jsp" />
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-chat-left-text me-1 text-muted"></i>Comment <span class="text-muted fw-normal" style="font-size:0.8em">wildcards * ?</span></label>
            <input type="text" class="form-control form-control-sm" id="iqb_comment" placeholder="e.g. *test*">
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-globe me-1 text-muted"></i>Country</label>
            <div class="d-flex align-items-center gap-1" style="min-width:0">
                <select class="form-select form-select-sm flex-grow-1" style="min-width:0" id="iqb_country">
                    <option value="">Any</option>
                    <c:forEach var="c" items="${countryOptions}">
                        <option value="${fn:toLowerCase(c.iso)}"<c:if test="${param.country == fn:toLowerCase(c.iso)}"> selected</c:if>>${c.name}</option>
                    </c:forEach>
                </select>
                <span id="iqb_country_flag" class="fi" style="font-size:1.4em;display:none;flex-shrink:0"></span>
            </div>
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-toggle-on me-1 text-muted"></i>Enabled</label>
            <select class="form-select form-select-sm" id="iqb_enabled">
                <option value="">Any</option>
                <option value="yes"<c:if test="${param.enabled == 'yes'}"> selected</c:if>>Yes</option>
                <option value="no"<c:if test="${param.enabled == 'no'}"> selected</c:if>>No</option>
            </select>
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-incognito me-1 text-muted"></i>Anonymous</label>
            <select class="form-select form-select-sm" id="iqb_anonymous">
                <option value="">Any</option>
                <option value="yes"<c:if test="${param.anonymous == 'yes'}"> selected</c:if>>Yes</option>
                <option value="no"<c:if test="${param.anonymous == 'no'}"> selected</c:if>>No</option>
            </select>
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-shield-lock me-1 text-muted"></i>TOTP</label>
            <select class="form-select form-select-sm" id="iqb_totp">
                <option value="">Any</option>
                <option value="yes"<c:if test="${param.totp == 'yes'}"> selected</c:if>>Enabled</option>
                <option value="no"<c:if test="${param.totp == 'no'}"> selected</c:if>>Disabled</option>
            </select>
        </div>
        <div class="col-6">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-sliders me-1 text-muted"></i>Properties editor</label>
            <select class="form-select form-select-sm" id="iqb_propErrors">
                <option value="">Any</option>
                <option value="yes"<c:if test="${param.propErrors == 'yes'}"> selected</c:if>>Has errors</option>
            </select>
        </div>
    </div>
    <div class="d-flex gap-1 pt-1 border-top mt-1 justify-content-end">
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="iqbClear()">
            <i class="bi bi-x-circle me-1"></i>Clear
        </button>
        <button type="button" class="btn btn-sm btn-primary" onclick="iqbApply()">
            <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
        </button>
    </div>
</div>

<div class="collapse" id="incomingUsersInfo">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">Data Users &mdash; search and filter</strong>
        <p class="mb-1">This page lists all Data Portal users (incoming users). Use the controls in the header to narrow the list:</p>
        <ul class="mb-1 ps-3">
            <li><strong>Unassigned only</strong> &mdash; shows only users with no reachable destinations (neither direct nor via a Data Policy).</li>
            <li><strong>Filter</strong> &mdash; query builder with Destination, Policy, Comment, Country, Enabled, Anonymous, TOTP, and Properties editor filters. A <span style="color:#dc3545">&#9888;</span> icon in the Data Login column marks users with properties errors.</li>
            <li><strong>Search data login</strong> &mdash; client-side text filter on the login name, applied on top of the other filters.</li>
        </ul>
        <div class="text-muted">Use the <strong>Unassigned only</strong> button to filter just the problematic users (no reachable Destinations neither direct nor via a Data Policy).</div>
    </div>
</div>

<div class="card-body p-0">
<div id="incomingBulkMsg" style="display:none" class="mx-2 mt-2"></div>
<div class="table-responsive">
<table id="usersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-warning">
        <tr>
            <th>Data Login</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">TOTP</th>
            <th class="text-center">Anonymous</th>
            <th class="text-center">Sessions</th>
            <th class="text-center no-sort">Actions</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>
</div>
</div>
<div class="mt-3">
    <button id="incomingDeleteAllBtn" type="button"
            class="btn btn-outline-danger d-none"
            title="Delete all currently listed unassigned Data Users">
        <i class="bi bi-trash-fill me-1"></i>Delete All
    </button>
</div>
<script>
// ---- QB helpers in global scope so onclick attributes can call them ----
// Read initial filter values from URL (not JSP expressions), so browser reload
// after iqbClear() (which does history.replaceState to clean the URL) gives no filters.
var _iqbParams    = new URLSearchParams(window.location.search);
var _destFilter   = _iqbParams.get('destinationNameForSearch') || '';
var _policyFilter = _iqbParams.get('policyNameForSearch') || '';
var _unassignedOnly = false;
var _incUsrTable = null;
// Picker callbacks — intercept destGo/policyGo to update in-memory filter instead of navigating
window.onDestSelected = function(name) {
    _destFilter = (name && name !== 'Any Destination') ? name : '';
    var inp = document.getElementById('destPickerInput'); if (inp) inp.value = _destFilter;
    window.destPickerClose && window.destPickerClose();
    iqbUpdateBadge();
};
window.onPolicySelected = function(name) {
    _policyFilter = (name && name !== 'Any Policy') ? name : '';
    var inp = document.getElementById('policyPickerInput'); if (inp) inp.value = _policyFilter;
    window.policyPickerClose && window.policyPickerClose();
    iqbUpdateBadge();
};
function iqbVal(id) { var el = document.getElementById(id); return el ? el.value : ''; }
function _buildAjaxUrl() {
    var params = [];
    if (_destFilter)   params.push('destinationNameForSearch=' + encodeURIComponent(_destFilter));
    if (_policyFilter) params.push('policyNameForSearch='      + encodeURIComponent(_policyFilter));
    if (_unassignedOnly)
        params.push('unassigned=true');
    var en = iqbVal('iqb_enabled');    if (en) params.push('enabled='    + encodeURIComponent(en));
    var an = iqbVal('iqb_anonymous');  if (an) params.push('anonymous='  + encodeURIComponent(an));
    var tp = iqbVal('iqb_totp');       if (tp) params.push('totp='       + encodeURIComponent(tp));
    var pe = iqbVal('iqb_propErrors'); if (pe) params.push('propErrors=' + encodeURIComponent(pe));
    var co = iqbVal('iqb_country');    if (co) params.push('country='    + encodeURIComponent(co));
    var cm = iqbVal('iqb_comment');    if (cm) params.push('comment='    + encodeURIComponent(cm));
    return '/do/user/incoming/list' + (params.length ? '?' + params.join('&') : '');
}
function iqbCountActive() {
    var n = 0;
    if (_destFilter)              n++;
    if (_policyFilter)            n++;
    if (iqbVal('iqb_enabled'))    n++;
    if (iqbVal('iqb_anonymous'))  n++;
    if (iqbVal('iqb_totp'))       n++;
    if (iqbVal('iqb_propErrors')) n++;
    if (iqbVal('iqb_country'))    n++;
    if (iqbVal('iqb_comment'))    n++;
    return n;
}
function iqbUpdateBadge() {
    var n = iqbCountActive();
    var b = document.getElementById('btnIncUsrQB-badge');
    if (b) { b.textContent = n; b.style.display = n > 0 ? '' : 'none'; }
    var btn = document.getElementById('btnIncUsrQB');
    if (btn) { btn.classList.toggle('btn-outline-primary', n === 0); btn.classList.toggle('btn-warning', n > 0); }
}
function _syncUrl() {
    var url = new URL(window.location.href);
    ['destinationNameForSearch','policyNameForSearch','enabled','anonymous','totp','propErrors','country','comment'].forEach(function(k) {
        url.searchParams.delete(k);
    });
    if (_destFilter)   url.searchParams.set('destinationNameForSearch', _destFilter);
    if (_policyFilter) url.searchParams.set('policyNameForSearch', _policyFilter);
    var en = iqbVal('iqb_enabled');    if (en) url.searchParams.set('enabled', en);
    var an = iqbVal('iqb_anonymous');  if (an) url.searchParams.set('anonymous', an);
    var tp = iqbVal('iqb_totp');       if (tp) url.searchParams.set('totp', tp);
    var pe = iqbVal('iqb_propErrors'); if (pe) url.searchParams.set('propErrors', pe);
    var co = iqbVal('iqb_country');    if (co) url.searchParams.set('country', co);
    var cm = iqbVal('iqb_comment');    if (cm) url.searchParams.set('comment', cm);
    history.replaceState(null, '', url.toString());
}
function iqbApply() {
    iqbUpdateBadge();
    _syncUrl();
    var p = document.getElementById('incUsrQueryBuilder'); if (p) p.style.display = 'none';
    if (_incUsrTable) _incUsrTable.ajax.url(_buildAjaxUrl()).load();
}
function iqbReload() { iqbUpdateBadge(); if (_incUsrTable) _incUsrTable.ajax.url(_buildAjaxUrl()).load(); }
function iqbClear() {
    _destFilter = ''; _policyFilter = '';
    var di = document.getElementById('destPickerInput');   if (di) di.value = '';
    var pi = document.getElementById('policyPickerInput'); if (pi) pi.value = '';
    ['iqb_enabled','iqb_anonymous','iqb_totp','iqb_propErrors','iqb_country','iqb_comment'].forEach(function(id) {
        var el = document.getElementById(id); if (el) el.value = '';
    });
    // Clear country flag
    var flag = document.getElementById('iqb_country_flag'); if (flag) flag.style.display = 'none';
    _syncUrl();
    var p = document.getElementById('incUsrQueryBuilder'); if (p) p.style.display = 'none';
    iqbUpdateBadge();
    if (_incUsrTable) _incUsrTable.ajax.url(_buildAjaxUrl()).load();
}
function toggleIQBPanel(panelId, btnId) {
    var panel = document.getElementById(panelId);
    var btn = document.getElementById(btnId);
    if (!panel || !btn) return;
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
    var vw = window.innerWidth || document.documentElement.clientWidth;
    var pw = Math.min(420, vw - 16);
    panel.style.width = pw + 'px';
    var r = btn.getBoundingClientRect();
    var sy = window.pageYOffset || document.documentElement.scrollTop;
    panel.style.top = (r.bottom + sy + 4) + 'px';
    panel.style.left = Math.max(8, r.right - pw + window.pageXOffset) + 'px';
    panel.style.display = 'block';
}
document.addEventListener('click', function(e) {
    var panel = document.getElementById('incUsrQueryBuilder');
    var btn = document.getElementById('btnIncUsrQB');
    if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
        panel.style.display = 'none';
    }
});

$(document).ready(function() {
    var _canDelete = false;
    var _filteredCount = 0;
    var table = $('#usersTable').DataTable({
        ajax: {
            url: _buildAjaxUrl(),
            dataSrc: function(json) {
                _canDelete = !!json.canDelete;
                _filteredCount = json.recordsFiltered || 0;
                return json.data;
            }
        },
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        language:   { emptyTable: 'No Data Users found.' },
        columnDefs: [
            { orderable: false, targets: 'no-sort' },
            { orderData: [8],  targets: [3] },
            { orderData: [9],  targets: [4] },
            { orderData: [10], targets: [5] },
            { visible: false,  targets: [8, 9, 10] }
        ],
        drawCallback: function() { _updateDeleteAllBtn(); _setFilterLoading($('#incomingUnassignedBtn'), false); },
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    _incUsrTable = table;
    iqbUpdateBadge(); // initialise badge in case dest/policy are pre-selected from URL
    // Restore comment from URL and init country flag
    (function() {
        var p = new URLSearchParams(window.location.search);
        var cm = p.get('comment'); if (cm) { var el = document.getElementById('iqb_comment'); if (el) el.value = cm; }
        // country flag
        var sel = document.getElementById('iqb_country');
        var flag = document.getElementById('iqb_country_flag');
        function updateFlag() {
            if (!sel || !flag) return;
            var iso = sel.value;
            if (iso) { flag.className = 'fi fi-' + iso; flag.style.display = ''; }
            else { flag.style.display = 'none'; }
        }
        updateFlag();
        if (sel) sel.addEventListener('change', updateFlag);
    })();
    $('#incomingPageLen').val((function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
    $('#incomingPageLen').on('change', function() {
        var len = parseInt(this.value);
        try { localStorage.setItem('incomingPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#incomingSearch').on('input', function() { table.column(0).search(this.value).draw(); });

    function _setFilterLoading($btn, loading) {
        if (loading) {
            $btn.prop('disabled', true)
                .prepend('<span class="spinner-border spinner-border-sm me-1 _filter-spinner" role="status" aria-hidden="true"></span>');
        } else {
            $btn.find('._filter-spinner').remove();
            $btn.prop('disabled', false);
        }
    }

    $('#incomingUnassignedBtn').on('click', function() {
        _unassignedOnly = !_unassignedOnly;
        $(this).toggleClass('btn-outline-secondary', !_unassignedOnly)
               .toggleClass('btn-warning', _unassignedOnly);
        if (!_unassignedOnly) $('#incomingDeleteAllBtn').addClass('d-none');
        _setFilterLoading($(this), true);
        table.ajax.url(_buildAjaxUrl()).load();
    });

    function _updateDeleteAllBtn() {
        var show = _unassignedOnly && _canDelete && _filteredCount > 0;
        $('#incomingDeleteAllBtn').toggleClass('d-none', !show);
        if (show) {
            $('#incomingDeleteAllBtn').html('<i class="bi bi-trash-fill me-1"></i>Delete All (' + _filteredCount + ')');
        }
    }

    function _showBulkMsg(type, html) {
        var $m = $('#incomingBulkMsg');
        $m.attr('class', 'mx-2 mt-2 alert alert-' + type + ' alert-dismissible d-flex align-items-center gap-2 p-2 mb-0');
        var icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';
        $m.html('<i class="bi ' + icon + ' flex-shrink-0"></i><div class="flex-grow-1">' + html + '</div>'
              + '<button type="button" class="btn-close p-2" data-bs-dismiss="alert" aria-label="Close"></button>');
        $m.show();
        setTimeout(function() { $m.fadeOut(); }, 6000);
    }

    $('#incomingDeleteAllBtn').on('click', function() {
        var n = _filteredCount;
        var label = n === 1 ? '1 unassigned Data User' : 'all ' + n + ' unassigned Data Users';
        confirmationDialog({
            title: 'Delete Unassigned Data Users',
            message: 'Delete ' + label + '? This action cannot be undone.',
            confirmText: 'Delete',
            showLoading: false,
            onConfirm: function() {
                $.ajax({
                    url: '/do/user/incoming/edit/deleteAllUnassigned',
                    method: 'GET',
                    success: function(data) {
                        var msg = 'Deleted ' + data.deleted + ' Data User' + (data.deleted !== 1 ? 's' : '');
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
        var _incUsrColKey        = 'incUsrColMode';
        var _incUsrCustomColKey  = 'incUsrCustomCols';
        var _incUsrCompact       = [1, 2, 4, 5, 6];
        var _incUsrColMode = (function() { try { return localStorage.getItem(_incUsrColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _incUsrCustomCols = (function() {
            try { var s = localStorage.getItem(_incUsrCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4, 5, 6, 7];
        })();
        function _incUsrShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 8) continue; // hidden sort cols always stay hidden
                table.column(i).visible(hideCols.indexOf(i) === -1, false);
            }
            table.columns.adjust();
        }
        function _incUsrApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 8) continue;
                table.column(i).visible((i === 0 || i === 7) ? true : _incUsrCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _incUsrSyncChkBoxes() {
            document.querySelectorAll('.incUsr-col-chk').forEach(function(chk) {
                chk.checked = _incUsrCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.incUsr-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _incUsrCustomCols.indexOf(col);
                if (this.checked && idx === -1) _incUsrCustomCols.push(col);
                else if (!this.checked && idx !== -1) _incUsrCustomCols.splice(idx, 1);
                try { localStorage.setItem(_incUsrCustomColKey, JSON.stringify(_incUsrCustomCols)); } catch(e) {}
                if (_incUsrColMode === 'custom') _incUsrApplyCustomCols();
            });
        });
        function _incUsrApplyResponsive() {
            if (_incUsrColMode !== 'auto') return;
            _incUsrShowCols(window.innerWidth < 992 ? [1, 2, 4, 5, 6] : []);
        }
        function _incUsrApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#incUsrColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#incUsrColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#incUsrColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('incusr-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('incUsrCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _incUsrApplyResponsive();
            else if (mode === 'all') _incUsrShowCols([]);
            else if (mode === 'compact') _incUsrShowCols(_incUsrCompact);
            else if (mode === 'custom') { _incUsrSyncChkBoxes(); _incUsrApplyCustomCols(); }
        }
        $(window).on('resize', _incUsrApplyResponsive);
        _incUsrApplyMode(_incUsrColMode);
        $('#incUsrColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('incusr-mode');
            if (!mode) return;
            _incUsrColMode = mode;
            try { localStorage.setItem(_incUsrColKey, mode); } catch(e) {}
            _incUsrApplyMode(mode);
        });
});
</script>

