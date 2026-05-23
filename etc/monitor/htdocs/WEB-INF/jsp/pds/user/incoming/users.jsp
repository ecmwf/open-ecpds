<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Data Users</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <c:set var="destParam" value="destinationNameForSearch" scope="request"/>
        <tiles:insert name="destination.select" />
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
                <option value="-1">All</option>
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
<div class="card-body p-0">
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
<script>
$(document).ready(function() {
    var _destFilter = '<c:out value="${destinationNameForSearch}"/>';
    var _ajaxUrl = '/do/user/incoming/list'
        + (_destFilter ? '?destinationNameForSearch=' + encodeURIComponent(_destFilter) : '');
    var table = $('#usersTable').DataTable({
        ajax:       { url: _ajaxUrl, dataSrc: 'data' },
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,-1].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
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
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    $('#incomingPageLen').val((function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,-1].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
    $('#incomingPageLen').on('change', function() {
        var len = parseInt(this.value);
        try { localStorage.setItem('incomingPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#incomingSearch').on('input', function() { table.search(this.value).draw(); });

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
