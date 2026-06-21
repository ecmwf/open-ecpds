<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock-history text-primary"></i>
<span class="fw-semibold">Events for <auth:link basePathKey="user.basepath" href="">All Web Users</auth:link></span>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
  <form id="userEventSearchForm" class="m-0">
    <div class="input-group input-group-sm">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input id="userEventSearch" class="form-control" name="search" type="text" placeholder="Search.." title="Search is performed across the Web User, Action and Comment in case-sensitive" value="${param['search']}" style="min-width:180px">
      <button class="btn btn-outline-secondary btn-sm" type="submit">Search</button>
    </div>
  </form>
  <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="userEventPageLen" class="form-select form-select-sm" style="width:auto">
      <option value="10">10</option>
      <option value="25">25</option>
      <option value="50">50</option>
      <option value="100">100</option>
      <option value="250">250</option>
    </select>
  </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="ueColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="ueColModeBtn">
                        <li><a class="dropdown-item" href="#" data-ue-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-ue-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-ue-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Name, File Name, Link</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-ue-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="ueCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="uechk-0">Time <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-1" data-col="1" checked><label class="form-check-label" for="uechk-1">Web User</label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="uechk-2">Action <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-3" data-col="3" checked><label class="form-check-label" for="uechk-3">Comment</label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-4" data-col="4" checked><label class="form-check-label" for="uechk-4">Name</label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-5" data-col="5" checked><label class="form-check-label" for="uechk-5">File Name</label></div>
            <div class="form-check mb-0"><input class="form-check-input ue-col-chk" type="checkbox" id="uechk-6" data-col="6" checked><label class="form-check-label" for="uechk-6">Link</label></div>
                            </div>
                        </li>
                    </ul>
                </div>
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="userEventTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
    <thead class="table-warning">
        <tr>
            <th title="Time (UTC) &mdash; date shown in selector above">Time</th>
            <th>Web User</th>
            <th>Action</th>
            <th>Comment</th>
            <th>Name</th>
            <th>File Name</th>
            <th>Link</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>
</div>
</div>

<script>
var _userEventTable;
function userEventTableReload() {
    if (_userEventTable) {
        _userEventTable.ajax.reload();
    }
}
$(function() {
    _userEventTable = $('#userEventTable').DataTable({
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/user/event/list',
            type: 'GET',
            data: function(d) {
                d.date = '${selectedDate}';
                d.search = $('#userEventSearch').val() || '';
            }
        },
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('userEventPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching: false,
        autoWidth: false,
        order: [[0, 'desc']],
        columns: [
            { orderable: true,  data: 0 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 1 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 2 , render: function(d) { return d || ''; } },
            { orderable: true,  data: 3 , render: function(d) { return d || ''; } },
            { orderable: false, data: 4 , render: function(d) { return d || ''; } },
            { orderable: false, data: 5 , render: function(d) { return d || ''; } },
            { orderable: false, data: 6, width: '48px' , render: function(d) { return d || ''; } }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading...',
            emptyTable: 'No Web Event Log available with the criteria selected'
        }
    });
    var _ueLen = (function() { try { var v = parseInt(localStorage.getItem('userEventPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    $('#userEventPageLen').val(_ueLen);
    $('#userEventPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('userEventPageLen', len); } catch(e) {}
        _userEventTable.page.len(len).draw();
    });

        /* ---- Cols:Auto ---- */
        var _ueColKey        = 'ueColMode';
        var _ueCustomColKey  = 'ueCustomCols';
        var _ueCompact       = [3, 4, 5, 6];
        var _ueColMode = (function() { try { return localStorage.getItem(_ueColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _ueCustomCols = (function() {
            try { var s = localStorage.getItem(_ueCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4, 5, 6];
        })();
        function _ueShowCols(hideCols) {
            var n = _userEventTable.columns().count();
            for (var i = 0; i < n; i++) _userEventTable.column(i).visible(hideCols.indexOf(i) === -1, false);
            _userEventTable.columns.adjust();
        }
        function _ueApplyCustomCols() {
            var n = _userEventTable.columns().count();
            for (var i = 0; i < n; i++) {
                _userEventTable.column(i).visible((i === 0 || i === 2) ? true : _ueCustomCols.indexOf(i) !== -1, false);
            }
            _userEventTable.columns.adjust();
        }
        function _ueSyncChkBoxes() {
            document.querySelectorAll('.ue-col-chk').forEach(function(chk) {
                chk.checked = _ueCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.ue-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _ueCustomCols.indexOf(col);
                if (this.checked && idx === -1) _ueCustomCols.push(col);
                else if (!this.checked && idx !== -1) _ueCustomCols.splice(idx, 1);
                try { localStorage.setItem(_ueCustomColKey, JSON.stringify(_ueCustomCols)); } catch(e) {}
                if (_ueColMode === 'custom') _ueApplyCustomCols();
            });
        });
        function _ueApplyResponsive() {
            if (_ueColMode !== 'auto') return;
            _ueShowCols(window.innerWidth < 992 ? [3, 4, 5, 6] : []);
        }
        function _ueApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#ueColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#ueColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#ueColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('ue-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('ueCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _ueApplyResponsive();
            else if (mode === 'all') _ueShowCols([]);
            else if (mode === 'compact') _ueShowCols(_ueCompact);
            else if (mode === 'custom') { _ueSyncChkBoxes(); _ueApplyCustomCols(); }
        }
        $(window).on('resize', _ueApplyResponsive);
        _ueApplyMode(_ueColMode);
        $('#ueColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('ue-mode');
            if (!mode) return;
            _ueColMode = mode;
            try { localStorage.setItem(_ueColKey, mode); } catch(e) {}
            _ueApplyMode(mode);
        });
    $('#userEventSearchForm').on('submit', function(e) {
        e.preventDefault();
        userEventTableReload();
    });
    $('#userEventSearch').on('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            userEventTableReload();
        }
    });
});
</script>
