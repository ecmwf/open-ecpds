<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-collection text-primary"></i>
    <span class="fw-semibold">Web Categories</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="categoriesSearch" class="form-control" placeholder="Search categories..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="categoriesPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <div class="dropdown">
            <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="catColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="catColModeBtn">
                <li><a class="dropdown-item" href="#" data-cat-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-cat-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-cat-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Description, Resources</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-cat-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                <li id="catCustomColChkPanel" style="display:none;">
                    <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
                        <div class="form-check mb-0"><input class="form-check-input cat-col-chk" type="checkbox" id="catchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="catchk-0">Name <small>(required)</small></label></div>
                        <div class="form-check mb-0"><input class="form-check-input cat-col-chk" type="checkbox" id="catchk-1" data-col="1" checked><label class="form-check-label" for="catchk-1">Description</label></div>
                        <div class="form-check mb-0"><input class="form-check-input cat-col-chk" type="checkbox" id="catchk-2" data-col="2" checked><label class="form-check-label" for="catchk-2">Resources</label></div>
                        <div class="form-check mb-0"><input class="form-check-input cat-col-chk" type="checkbox" id="catchk-3" data-col="3" checked disabled><label class="form-check-label text-muted" for="catchk-3">Actions <small>(required)</small></label></div>
                    </div>
                </li>
            </ul>
        </div>
        <a href="<bean:message key="category.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="categoriesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Resources</th>
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
    var _initLen = (function() { try { var v = parseInt(localStorage.getItem('categoriesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    var table = $('#categoriesTable').DataTable({
        ajax:       { url: '/do/user/category/list', dataSrc: 'data' },
        paging:     true,
        pageLength: _initLen,
        searching:  true,
        ordering:   true,
        info:       true,
        columns: [
            { data: 0 },
            { data: 1 },
            { data: 2, orderable: false },
            { data: 3, orderable: false, className: 'text-center' }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    $('#categoriesPageLen').val(_initLen);
    $('#categoriesPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('categoriesPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#categoriesSearch').on('keyup', function() { table.search(this.value).draw(); });

    /* ---- Cols:Auto ---- */
    var _catColKey       = 'catColMode';
    var _catCustomColKey = 'catCustomCols';
    var _catCompact      = [1, 2];
    var _catColMode = (function() { try { return localStorage.getItem(_catColKey) || 'auto'; } catch(e) { return 'auto'; } })();
    var _catCustomCols = (function() {
        try { var s = localStorage.getItem(_catCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
        return [0, 1, 2, 3];
    })();
    function _catShowCols(hideCols) {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
        table.columns.adjust();
    }
    function _catApplyCustomCols() {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) {
            table.column(i).visible((i === 0 || i === 3) ? true : _catCustomCols.indexOf(i) !== -1, false);
        }
        table.columns.adjust();
    }
    function _catSyncChkBoxes() {
        document.querySelectorAll('.cat-col-chk').forEach(function(chk) {
            chk.checked = _catCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }
    document.querySelectorAll('.cat-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _catCustomCols.indexOf(col);
            if (this.checked && idx === -1) _catCustomCols.push(col);
            else if (!this.checked && idx !== -1) _catCustomCols.splice(idx, 1);
            try { localStorage.setItem(_catCustomColKey, JSON.stringify(_catCustomCols)); } catch(e) {}
            if (_catColMode === 'custom') _catApplyCustomCols();
        });
    });
    function _catApplyResponsive() {
        if (_catColMode !== 'auto') return;
        _catShowCols(window.innerWidth < 768 ? [1, 2] : []);
    }
    function _catApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#catColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#catColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#catColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('cat-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        document.getElementById('catCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        if (mode === 'auto') _catApplyResponsive();
        else if (mode === 'all') _catShowCols([]);
        else if (mode === 'compact') _catShowCols(_catCompact);
        else if (mode === 'custom') { _catSyncChkBoxes(); _catApplyCustomCols(); }
    }
    $(window).on('resize', _catApplyResponsive);
    _catApplyMode(_catColMode);
    $('#catColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
        e.preventDefault();
        var mode = $(this).data('cat-mode');
        if (!mode) return;
        _catColMode = mode;
        try { localStorage.setItem(_catColKey, mode); } catch(e) {}
        _catApplyMode(mode);
    });
});
</script>
