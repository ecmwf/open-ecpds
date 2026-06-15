<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<div class="card border-0 shadow-sm mt-3">
    <div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-clock-history text-primary"></i>
        <span class="fw-semibold">Data Events Log</span>
        <div class="ms-auto d-flex align-items-center gap-2 flex-wrap">
            <div class="input-group input-group-sm flex-nowrap" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input id="incomingHistorySearch" class="form-control form-control-sm" name="search" type="text"
                    placeholder="Search filename..."
                    title="Search is performed against the File Name (case-sensitive)"
                    style="width:160px;min-width:80px"
                    value="${param['search']}">
                <button id="incomingHistorySearchBtn" class="btn btn-outline-secondary btn-sm" type="button">Search</button>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="incomingHistPageLen" class="form-select form-select-sm" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                </select>
            </div>
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="ihColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                    <i class="bi bi-layout-three-columns me-1"></i>Auto
                </button>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="ihColModeBtn">
                    <li><a class="dropdown-item" href="#" data-col-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><small class="d-block text-muted ms-4">Adapts to screen width</small></a></li>
                    <li><a class="dropdown-item" href="#" data-col-mode="all"><strong>All</strong><small class="d-block text-muted ms-0">All columns visible</small></a></li>
                    <li><a class="dropdown-item" href="#" data-col-mode="compact"><strong>Compact</strong><small class="d-block text-muted ms-0">Hides: Server, Protocol, Finish Time</small></a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="#" data-col-mode="custom"><strong>Custom</strong><small class="d-block text-muted ms-0">Choose individual columns</small></a></li>
                    <li id="ihCustomColChkPanel" style="display:none;">
                        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="ihchk-0">Data User <small>(required)</small></label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-1" data-col="1" checked><label class="form-check-label" for="ihchk-1">Destination</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-2" data-col="2" checked><label class="form-check-label" for="ihchk-2">Transfer Server</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-3" data-col="3" checked><label class="form-check-label" for="ihchk-3">Protocol</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-4" data-col="4" checked><label class="form-check-label" for="ihchk-4">File Name</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-5" data-col="5" checked><label class="form-check-label" for="ihchk-5">Start Time</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-6" data-col="6" checked><label class="form-check-label" for="ihchk-6">Finish Time</label></div>
                            <div class="form-check mb-0"><input class="form-check-input ih-col-chk" type="checkbox" id="ihchk-7" data-col="7" checked><label class="form-check-label" for="ihchk-7">Mbits/s</label></div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table id="incomingHistoryTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
                <thead class="table-light">
                    <tr>
                        <th>Data User</th>
                        <th>Destination</th>
                        <th>Transfer Server</th>
                        <th>Protocol</th>
                        <th>File Name</th>
                        <th title="Start Time (UTC)">Start Time</th>
                        <th title="Finish Time (UTC)">Finish Time</th>
                        <th>Mbits/s</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</div>

<script>
var _incomingHistoryTable;
function incomingHistoryTableReload() {
    if (_incomingHistoryTable) {
        _incomingHistoryTable.ajax.reload();
    }
}
$(function() {
    var _ihPageLen = (function() { try { var v = parseInt(localStorage.getItem('incomingHistPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    _incomingHistoryTable = $('#incomingHistoryTable').DataTable({
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/user/history/list',
            type: 'GET',
            data: function(d) {
                d.date = '${selectedDate}';
                d.search = $('#incomingHistorySearch').val() || '';
            }
        },
        pageLength: _ihPageLen,
        searching: false,
        autoWidth: false,
        order: [[5, 'desc']],
        columns: [
            { orderable: true,  data: 0 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 1 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 2 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 3 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 4 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 5 , render: function(d) { return d || ''; } },
            { orderable: false, data: 6 , render: function(d) { return d || ''; } },
            { orderable: false, data: 7 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 8 , render: function(d) { return d || ''; } }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading...',
            emptyTable: 'No Data Events found based on these criteria!'
        }
    });
    $('#incomingHistPageLen').val(_ihPageLen).on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('incomingHistPageLen', len); } catch(e) {}
        _incomingHistoryTable.page.len(len).draw();
    });
    $('#incomingHistorySearchBtn').on('click', function() { incomingHistoryTableReload(); });
    $('#incomingHistorySearch').on('keydown', function(e) {
        if (e.key === 'Enter') { e.preventDefault(); incomingHistoryTableReload(); }
    });

    // Cols: Auto
    var _ihColMode = (function() { try { return localStorage.getItem('ihColMode') || 'auto'; } catch(e) { return 'auto'; } })();
    var _ihCustomCols = (function() { try { var s = localStorage.getItem('ihCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5,6,7]; } catch(e) { return [0,1,2,3,4,5,6,7]; } })();
    function _ihShowCols(hide) { _incomingHistoryTable.columns().every(function(i) { if (i < 8) _incomingHistoryTable.column(i).visible(hide.indexOf(i) === -1, false); }); _incomingHistoryTable.columns.adjust(); }
    function _ihApplyCustom() { _incomingHistoryTable.columns().every(function(i) { if (i >= 8) return; var v = i === 0 || _ihCustomCols.indexOf(i) !== -1; _incomingHistoryTable.column(i).visible(v, false); }); _incomingHistoryTable.columns.adjust(); }
    function _ihApplyAuto() { if (_ihColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _ihShowCols([1,2,3,6,7]); else if (w < 992) _ihShowCols([2,3,6,7]); else _ihShowCols([]); }
    function _ihApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#ihColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#ihColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        document.getElementById('ihCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
        $('#ihColModeBtn').closest('.dropdown').find('[data-col-mode]').each(function() { $(this).find('i.bi-check').remove(); if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
        if (mode === 'auto') _ihApplyAuto(); else if (mode === 'all') _ihShowCols([]); else if (mode === 'compact') _ihShowCols([2,3,6]); else if (mode === 'custom') { _ihApplyCustom(); document.querySelectorAll('.ih-col-chk').forEach(function(c) { c.checked = c.disabled || _ihCustomCols.indexOf(+c.dataset.col) !== -1; }); }
    }
    document.querySelectorAll('.ih-col-chk:not([disabled])').forEach(function(chk) {
        chk.addEventListener('change', function() { var col = +this.dataset.col; var idx = _ihCustomCols.indexOf(col); if (this.checked && idx === -1) _ihCustomCols.push(col); else if (!this.checked && idx !== -1) _ihCustomCols.splice(idx, 1); try { localStorage.setItem('ihCustomCols', JSON.stringify(_ihCustomCols)); } catch(e) {} if (_ihColMode === 'custom') _ihApplyCustom(); });
    });
    $('#ihColModeBtn').closest('.dropdown').on('click', '[data-col-mode]', function(e) { e.preventDefault(); _ihColMode = $(this).data('col-mode'); try { localStorage.setItem('ihColMode', _ihColMode); } catch(e) {} _ihApplyMode(_ihColMode); });
    $(window).on('resize', function() { if (_ihColMode === 'auto') _ihApplyAuto(); });
    _ihApplyMode(_ihColMode);
});
</script>
