<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Data Users</span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#incomingUsersInfo"
        aria-expanded="false" title="About this page">
        <i class="bi bi-info-circle"></i>
    </button>
    <button id="incomingUnassignedBtn" type="button"
            class="btn btn-sm btn-outline-secondary"
            title="Show only Data Users with no destinations (direct or via policy)">
        <i class="bi bi-exclamation-triangle-fill me-1"></i>Unassigned only
    </button>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <c:set var="destParam" value="destinationNameForSearch" scope="request"/>
        <tiles:insert name="destination.select" />
        <jsp:include page="/WEB-INF/jsp/pds/common/policy_select.jsp" />
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="incomingSearch" class="form-control" placeholder="Search login..."
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

<div class="collapse" id="incomingUsersInfo">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">Data Users &mdash; search and filter</strong>
        <p class="mb-1">This page lists all Data Portal users (incoming users). Use the controls in the header to narrow the list:</p>
        <ul class="mb-1 ps-3">
            <li><strong>Search Destination</strong> &mdash; shows only users who have access to the selected destination, either directly or indirectly via a Data Policy.</li>
            <li><strong>Search Policy</strong> &mdash; shows only users attached to the selected Data Policy.</li>
            <li><strong>Unassigned only</strong> &mdash; shows only users with no reachable destinations (neither direct nor via a Data Policy).</li>
            <li><strong>Search login</strong> &mdash; client-side text filter on the login name, applied on top of the other filters.</li>
        </ul>
        <div class="text-muted">Use the <strong>Unassigned only</strong> button to filter to Data Users with no reachable destinations (neither direct nor via a Data Policy). The normal list does not compute destination associations to avoid performance overhead.</div>
    </div>
</div>

<div class="card-body p-0">
<div id="incomingBulkMsg" style="display:none" class="mx-2 mt-2"></div>
<div class="table-responsive">
<table id="usersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
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
$(document).ready(function() {
    var _destFilter     = '<c:out value="${destinationNameForSearch}"/>';
    var _policyFilter   = '<c:out value="${policyNameForSearch}"/>';
    var _unassignedOnly = false;
    function _buildAjaxUrl() {
        var params = [];
        if (_destFilter && _destFilter !== 'Any Destination')
            params.push('destinationNameForSearch=' + encodeURIComponent(_destFilter));
        if (_policyFilter && _policyFilter !== 'Any Policy')
            params.push('policyNameForSearch=' + encodeURIComponent(_policyFilter));
        if (_unassignedOnly)
            params.push('unassigned=true');
        return '/do/user/incoming/list' + (params.length ? '?' + params.join('&') : '');
    }
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
        drawCallback: function() { _updateDeleteAllBtn(); },
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    $('#incomingPageLen').val((function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
    $('#incomingPageLen').on('change', function() {
        var len = parseInt(this.value);
        try { localStorage.setItem('incomingPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#incomingSearch').on('input', function() { table.search(this.value).draw(); });

    $('#incomingUnassignedBtn').on('click', function() {
        _unassignedOnly = !_unassignedOnly;
        $(this).toggleClass('btn-outline-secondary', !_unassignedOnly)
               .toggleClass('btn-warning', _unassignedOnly);
        if (!_unassignedOnly) $('#incomingDeleteAllBtn').addClass('d-none');
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

