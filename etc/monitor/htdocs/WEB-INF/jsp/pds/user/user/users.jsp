<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Web Users</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="usersWebSearch" class="form-control" placeholder="Search users..." style="min-width:180px">
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
            <div class="form-check mb-0"><input class="form-check-input webUsr-col-chk" type="checkbox" id="webUsrchk-4" data-col="4" checked disabled><label class="form-check-label text-muted" for="webUsrchk-4">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="user.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="usersWebTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Web Login</th>
            <th>Comment</th>
            <th class="text-center">Enabled</th>
            <th>Categories</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>
</div>
</div>
<script>
$(document).ready(function() {
    var table = $('#usersWebTable').DataTable({
        ajax:       { url: '/do/user/user/list', dataSrc: 'data' },
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        language:   { emptyTable: 'No Web Users found.' },
        columnDefs: [
            { orderable: false, targets: -1 },
            { orderData: [5], targets: [2] },
            { visible: false, targets: [5] }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('usersWebPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#usersWebPageLen').val(_len);
    $('#usersWebPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('usersWebPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#usersWebSearch').on('keyup', function() { table.search(this.value).draw(); });

        /* ---- Cols:Auto ---- */
        var _webUsrColKey        = 'webUsrColMode';
        var _webUsrCustomColKey  = 'webUsrCustomCols';
        var _webUsrCompact       = [1, 3];
        var _webUsrColMode = (function() { try { return localStorage.getItem(_webUsrColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _webUsrCustomCols = (function() {
            try { var s = localStorage.getItem(_webUsrCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4];
        })();
        function _webUsrShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 5) continue;
                table.column(i).visible(hideCols.indexOf(i) === -1, false);
            }
            table.columns.adjust();
        }
        function _webUsrApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 5) continue;
                table.column(i).visible((i === 0 || i === 4) ? true : _webUsrCustomCols.indexOf(i) !== -1, false);
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
