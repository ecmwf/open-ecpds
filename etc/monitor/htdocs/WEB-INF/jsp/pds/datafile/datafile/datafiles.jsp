<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />
<tiles:insert name="metadata.select" />

<%-- Search form --%>
<div class="card border-0 shadow-sm mb-3">
    <div class="card-body py-2 px-3">
        <div class="row g-2 align-items-center">
            <div class="col-auto d-flex align-items-center gap-2">
                <div class="input-group flex-nowrap" style="width:auto" title="Page size">
                    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                    <select id="datafilePageLen" class="form-select" style="width:auto">
                        <option value="10">10</option>
                        <option value="25">25</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                        <option value="250">250</option>
                    </select>
                </div>
                <div class="dropdown">
                    <button class="btn btn-outline-secondary dropdown-toggle" type="button" id="dfColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dfColModeBtn">
                        <li><a class="dropdown-item" href="#" data-dfcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-dfcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-dfcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Product Time, TS</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-dfcol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="dfCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                                <div class="form-check mb-0"><input class="form-check-input df-col-chk" type="checkbox" id="dfchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="dfchk-0">Original <small>(required)</small></label></div>
                                <div class="form-check mb-0"><input class="form-check-input df-col-chk" type="checkbox" id="dfchk-1" data-col="1" checked><label class="form-check-label" for="dfchk-1">Product Time</label></div>
                                <div class="form-check mb-0"><input class="form-check-input df-col-chk" type="checkbox" id="dfchk-2" data-col="2" checked><label class="form-check-label" for="dfchk-2">Size</label></div>
                                <div class="form-check mb-0"><input class="form-check-input df-col-chk" type="checkbox" id="dfchk-3" data-col="3" checked><label class="form-check-label" for="dfchk-3">TS</label></div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="col">
                <div class="input-group">
                    <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                    <input type="text" id="datafileSearch" class="form-control"
                           placeholder="Filter by original file name (use * and ? as wildcards)"
                           title="Substring search on original file name. Use * for zero or more characters, ? for exactly one.">
                </div>
            </div>
            <div class="col-auto">
                <button id="datafileSearchBtn" class="btn btn-primary">
                    <i class="bi bi-search"></i> Search
                </button>
                <button id="datafileSearchClear" class="btn btn-outline-secondary" title="Clear search">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
        </div>
    </div>
</div>

<table id="datafileTable" class="table table-sm table-hover table-striped align-middle w-100">
    <thead class="table-light">
        <tr>
            <th>Original</th>
            <th title="Product Time (UTC)">Product Time</th>
            <th>Size</th>
            <th>TS</th>
        </tr>
    </thead>
</table>

<script>
(function () {
    var selectedDate      = '<c:out value="${selectedDate}"/>';
    var selectedMetaName  = '<c:out value="${selectedMetaDataName}"/>';
    var selectedMetaValue = '<c:out value="${selectedMetaDataValue}"/>';
    var currentSearch     = '';

    $(document).ready(function () {
        var table = $('#datafileTable').DataTable({
            serverSide: true,
            processing: true,
            ajax: {
                url: '/do/datafile/datafile/list',
                data: function (d) {
                    d.date           = selectedDate;
                    d.metaDataName   = selectedMetaName;
                    d.metaDataValue  = selectedMetaValue;
                    d.datafileSearch = currentSearch;
                }
            },
            order: [[1, 'desc']],
            columns: [
                { title: 'Original',     orderable: true,  render: function (data) { return data; } },
                { title: 'Product Time', orderable: true,  className: 'text-nowrap' },
                { title: 'Size',         orderable: true,  className: 'text-end text-nowrap' },
                { title: 'TS',           orderable: true,  className: 'text-end' }
            ],
            columnDefs: [],
            pageLength: (function() { try { var v = parseInt(localStorage.getItem('datafilePageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
            lengthMenu: [[10, 25, 50, 100, 250], [10, 25, 50, 100, 250]],
            language: {
                emptyTable:     'No Data Files found for the selected date and metadata.',
                loadingRecords: 'Loading&hellip;',
                processing:     '<i class="bi bi-hourglass-split"></i> Loading&hellip;'
            },
            dom: "t<'d-flex align-items-start mt-2'i<'ms-auto'p>>",
            buttons: []
        });

        $('#datafilePageLen').val((function() { try { var v = parseInt(localStorage.getItem('datafilePageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
        $('#datafilePageLen').on('change', function () {
            var len = parseInt(this.value, 10);
            try { localStorage.setItem('datafilePageLen', len); } catch(e) {}
            table.page.len(len).draw();
        });

        function doSearch() {
            currentSearch = $('#datafileSearch').val().trim();
            table.ajax.reload();
        }

        $('#datafileSearchBtn').on('click', doSearch);
        $('#datafileSearch').on('keydown', function (e) {
            if (e.key === 'Enter') { doSearch(); }
        });
        $('#datafileSearchClear').on('click', function () {
            $('#datafileSearch').val('');
            currentSearch = '';
            table.ajax.reload();
        });

        /* ---- Cols:Auto ---- */
        var _DF_COL_KEY        = 'dfColMode';
        var _DF_CUSTOM_COL_KEY = 'dfCustomCols';
        var _DF_COMPACT        = [1, 3];
        var _dfColMode = (function() { try { return localStorage.getItem(_DF_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
        var _dfCustomCols = (function() {
            try { var s = localStorage.getItem(_DF_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,2,3];
        })();

        function _dfShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
            table.columns.adjust();
        }
        function _dfApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                table.column(i).visible(i === 0 ? true : _dfCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _dfSyncChkBoxes() {
            document.querySelectorAll('.df-col-chk').forEach(function(chk) {
                chk.checked = _dfCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.df-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _dfCustomCols.indexOf(col);
                if (this.checked && idx === -1) _dfCustomCols.push(col);
                else if (!this.checked && idx !== -1) _dfCustomCols.splice(idx, 1);
                try { localStorage.setItem(_DF_CUSTOM_COL_KEY, JSON.stringify(_dfCustomCols)); } catch(e) {}
                if (_dfColMode === 'custom') _dfApplyCustomCols();
            });
        });
        function _dfApplyResponsive() {
            if (_dfColMode !== 'auto') return;
            _dfShowCols(window.innerWidth < 768 ? [1, 3] : []);
        }
        function _dfApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#dfColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#dfColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#dfColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('dfcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('dfCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _dfApplyResponsive();
            else if (mode === 'all') _dfShowCols([]);
            else if (mode === 'compact') _dfShowCols(_DF_COMPACT);
            else if (mode === 'custom') { _dfSyncChkBoxes(); _dfApplyCustomCols(); }
        }
        $(window).on('resize', _dfApplyResponsive);
        _dfApplyMode(_dfColMode);
        $('#dfColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('dfcol-mode');
            if (!mode) return;
            _dfColMode = mode;
            try { localStorage.setItem(_DF_COL_KEY, mode); } catch(e) {}
            _dfApplyMode(mode);
        });
    });
})();
</script>
