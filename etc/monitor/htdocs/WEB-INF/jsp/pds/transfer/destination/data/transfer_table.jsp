<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<%-- Hidden inputs carrying current filter state for DataTables AJAX requests --%>
<input type="hidden" id="dt-dest-name"   value="${destinationDetailActionForm.id}">
<input type="hidden" id="dt-dissStream"  value="${destinationDetailActionForm.disseminationStream}">
<input type="hidden" id="dt-dataStream"  value="${destinationDetailActionForm.dataStream}">
<input type="hidden" id="dt-dataTime"    value="${destinationDetailActionForm.dataTime}">
<input type="hidden" id="dt-status"      value="${destinationDetailActionForm.status}">
<input type="hidden" id="dt-date"        value="${destinationDetailActionForm.date}">
<input type="hidden" id="dt-search"      value="${destinationDetailActionForm.fileNameSearch}">
<input type="hidden" id="dt-can-queue"   value="${not empty ecpdsCanHandleQueue ? 'true' : 'false'}">

<%-- Inline error message shown by drawCallback --%>
<div id="destTableError" class="alert alert-warning mt-2 mb-0" style="display:none"></div>

<%-- Toolbar row -- shows current filter selection, last-refresh time, and column-mode picker --%>
<div class="d-flex align-items-center gap-2 my-2 flex-wrap">
  <span class="d-flex align-items-center gap-2 small bg-body-tertiary border rounded px-2 py-1" id="destSelectionInfo"></span>
  <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <div class="dropdown">
      <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="colModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
        <i class="bi bi-layout-three-columns me-1"></i>Auto
      </button>
      <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="colModeBtn">
        <li><a class="dropdown-item active" href="#" data-col-mode="auto">
          <i class="bi bi-check me-1"></i><strong>Auto</strong>
          <small class="d-block text-muted ms-4">Adapts to screen width</small>
        </a></li>
        <li><a class="dropdown-item" href="#" data-col-mode="all">
          <strong>All</strong>
          <small class="d-block text-muted ms-0">All columns visible</small>
        </a></li>
        <li><a class="dropdown-item" href="#" data-col-mode="compact">
          <strong>Compact</strong>
          <small class="d-block text-muted ms-0">Hides: Err, Host, Sched., TS, %, Mbits/s, Prior</small>
        </a></li>
        <li><a class="dropdown-item" href="#" data-col-mode="small">
          <strong>Small</strong>
          <small class="d-block text-muted ms-0">Shows only: Target, Status, Actions</small>
        </a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#" data-col-mode="custom">
          <strong>Custom</strong>
          <small class="d-block text-muted ms-0">Choose individual columns</small>
        </a></li>
        <li id="customColChkPanel" style="display:none;">
          <div class="px-3 pb-1 pt-1" style="min-width:170px;font-size:0.82rem;">
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-0"  data-col="0"  checked><label class="form-check-label" for="chk-col-0">Err</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-1"  data-col="1"  checked><label class="form-check-label" for="chk-col-1">Host</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-2"  data-col="2"  checked><label class="form-check-label" for="chk-col-2">Sched. Time</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-3"  data-col="3"  checked><label class="form-check-label" for="chk-col-3">Start Time</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-4"  data-col="4"  checked><label class="form-check-label" for="chk-col-4">Finish Time</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-5"  data-col="5"  checked disabled><label class="form-check-label text-muted" for="chk-col-5">Target <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-6"  data-col="6"  checked><label class="form-check-label" for="chk-col-6">TS</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-7"  data-col="7"  checked><label class="form-check-label" for="chk-col-7">%</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-8"  data-col="8"  checked><label class="form-check-label" for="chk-col-8">Mbits/s</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-9"  data-col="9"  checked><label class="form-check-label" for="chk-col-9">Status</label></div>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-10" data-col="10" checked><label class="form-check-label" for="chk-col-10">Prior</label></div>
            <c:if test="${not empty ecpdsCanHandleQueue}">
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-11" data-col="11" checked><label class="form-check-label" for="chk-col-11">Actions</label></div>
            </c:if>
            <div class="form-check mb-0"><input class="form-check-input custom-col-chk" type="checkbox" id="chk-col-12" data-col="12" checked><label class="form-check-label" for="chk-col-12">Select</label></div>
          </div>
        </li>
      </ul>
    </div>
    <div id="destSelectionControls" class="d-none d-flex align-items-center gap-2">
      <div style="border-left:1px solid var(--bs-border-color);height:1.2rem;"></div>
      <span class="d-flex gap-1 align-items-center">
        <a href="javascript:checkAll(true,false)"  title="Select All"        class="text-decoration-none text-body">A</a>
        <span class="text-muted">/</span>
        <a href="javascript:checkAll(false,false)" title="Unselect All"      class="text-decoration-none text-body">N</a>
        <span class="text-muted">/</span>
        <a href="javascript:checkAll(false,true)"  title="Reverse Selection" class="text-decoration-none text-body">R</a>
      </span>
      <button class="btn btn-link btn-sm text-muted p-0" type="button"
          data-bs-toggle="collapse" data-bs-target="#dtSelectionHelp"
          aria-expanded="false" title="How to select and apply bulk actions">
        <i class="bi bi-info-circle"></i>
      </button>
    </div>
  </div>
</div>

<div class="collapse mt-1 mb-2" id="dtSelectionHelp">
  <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
    <strong class="d-block mb-1">Selecting transfers and applying bulk actions</strong>
    <p class="mb-1">Use the controls in the toolbar to build a <em>selection basket</em>, then open the basket to act on all selected transfers at once.</p>
    <ul class="mb-1 ps-3">
      <li><strong>A / N / R</strong> &mdash; Select All / Unselect All / Reverse selection across <em>all pages</em> matching the current filter.</li>
      <li><strong><i class="bi bi-star"></i> star icon</strong> on each row &mdash; toggle that individual transfer in or out of the basket.</li>
      <li><strong><i class="bi bi-basket2-fill"></i> Basket</strong> in the toolbar &mdash; click to open the basket and act on all selected transfers.</li>
    </ul>
    <p class="mb-0">From the basket page you can <strong>Requeue</strong>, <strong>Stop</strong>, <strong>Delete</strong>, change <strong>priority</strong>, or <strong>extend</strong> the expiry of all selected transfers in one operation. The basket is preserved as you change filters or navigate pages.</p>
  </div>
</div>

<%-- DataTable: 13 columns (Actions hidden when user lacks queue access) --%>
<table id="destTransferTable" class="table table-striped table-sm table-hover w-100" style="table-layout:fixed">
  <thead class="table-light">
    <tr>
      <th>Err</th>
      <th>Host</th>
      <th title="Scheduled Time (UTC)">Sched. Time</th>
      <th title="Start Time (UTC)">Start Time</th>
      <th title="Finish Time (UTC)">Finish Time</th>
      <th>Target</th>
      <th>TS</th>
      <th>%</th>
      <th>Mbits/s</th>
      <th>Status</th>
      <th>Prior</th>
      <th>Actions</th>
      <th>Select</th>
    </tr>
  </thead>
  <tbody></tbody>
</table>


<script>
var _dftSearchHelp = '<p class="mb-1 mt-2">You can conduct an extended search using the following rules:<\/p>' +
    '<ul class="mb-0">' +
    '<li><code>target=<\/code>, <code>source=<\/code>, <code>mover=<\/code>, <code>identity=<\/code>, <code>groupby=<\/code>, <code>checksum=<\/code>, <code>priority=<\/code><\/li>' +
    '<li><code>ts&gt;<\/code> \/ <code>ts&lt;=<\/code> &mdash; transfer size range (numeric); <code>size&gt;=700kb<\/code> &mdash; file size (<code>b<\/code>, <code>kb<\/code>, <code>mb<\/code>, <code>gb<\/code>)<\/li>' +
    '<li><code>asap=yes|no<\/code>, <code>deleted=yes|no<\/code>, <code>expired=yes|no<\/code>, <code>replicated=yes|no<\/code>, <code>proxy=yes|no<\/code>, <code>event=yes|no<\/code><\/li>' +
    '<li>Example: <code>asap=yes target=*.dat source=\/tmp\/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i<\/code><\/li>' +
    '<li><code>case=i<\/code> for case-insensitive, <code>case=s<\/code> for case-sensitive (default)<\/li>' +
    '<li>Wildcards: <code>*<\/code> (zero or more chars), <code>?<\/code> (exactly one char)<\/li>' +
    '<\/ul>' +
    '<div class="mt-2 text-muted small"><i class="bi bi-lightbulb"><\/i> Tip: Not sure about the syntax? Use the <a href="#" onclick="event.stopPropagation(); toggleQBPanel(\'dftQueryBuilder\',\'btnDftQB\'); document.getElementById(\'btnDftQB\').scrollIntoView({behavior:\'smooth\',block:\'center\'}); return false;" class="link-secondary"><i class="bi bi-sliders2"><\/i> Filter<\/a> above to build a valid search expression.<\/div>';
(function () {
    var STORAGE_KEY = 'destTransferPageLen';
    var savedLen = parseInt(localStorage.getItem(STORAGE_KEY), 10) || 25;

    // Reflect stored value in the selector
    var sel = document.getElementById('destPageLen');
    sel.value = String(savedLen);
    if (!sel.value) { sel.value = '25'; savedLen = 25; } // fallback if stored value not in list

    var canQueue = document.getElementById('dt-can-queue').value === 'true';

    // Replace DataTables' native alert() error popup with a no-op;
    // query errors are already shown inline via #destTableError in drawCallback.
    $.fn.dataTable.ext.errMode = function () {};

    var table = $('#destTransferTable').DataTable({
        serverSide: true,
        processing: true,
        autoWidth: false,
        pageLength: savedLen,
        ajax: {
            url: '/do/transfer/destination?json=dataList',
            type: 'GET',
            data: function (d) {
                d.destinationName      = document.getElementById('dt-dest-name').value;
                d.disseminationStream  = document.getElementById('dt-dissStream').value;
                d.dataStream           = document.getElementById('dt-dataStream').value;
                d.dataTime             = document.getElementById('dt-dataTime').value;
                d.status               = document.getElementById('dt-status').value;
                d.date                 = document.getElementById('dt-date').value;
                d.fileNameSearch       = document.getElementById('dt-search').value;
            }
        },
        columns: [
            { data: 0, width: '50px' },
            { data: 1, width: '90px' },
            { data: 2, width: '110px' },
            { data: 3, width: '110px' },
            { data: 4, width: '110px' },
            { data: 5, width: '200px' },
            { data: 6, width: '70px' },
            { data: 7, width: '70px' },
            { data: 8, width: '90px' },
            { data: 9, width: '90px' },
            { data: 10, width: '65px' },
            { data: 11, width: '110px' },
            { data: 12, width: '70px' }
        ],
        columnDefs: [
            { targets: 5, className: 'col-target' },
            { targets: 11, orderable: false, visible: canQueue },
            { targets: 12, orderable: false }
        ],
        order: [[2, 'desc']],
        dom: "t<'d-flex align-items-start mt-2'i<'ms-auto'p>>",
        language: {
            processing: '<span class="spinner-border spinner-border-sm me-1"></span> Loading&hellip;',
            emptyTable: 'No Data Transfers found based on these criteria.'
        },
        drawCallback: function (settings) {
            // Restore session-selected transfers from JSON into the JS selectedTransfers map
            var json = settings.json;
            if (json && json.selectedIds && json.selectedIds.length) {
                json.selectedIds.forEach(function (id) {
                    // Only restore if the user hasn't explicitly deselected it this session
                    if (selectedTransfers[String(id)] !== false) {
                        selectedTransfers[String(id)] = true;
                    }
                });
            }
            // Show/hide query errors
            var err = document.getElementById('destTableError');
            if (json && json.error) {
                function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
                err.innerHTML = '<strong>Error in your query:<\/strong> ' + esc(json.error) + _dftSearchHelp;
                err.style.display = '';
            } else {
                err.style.display = 'none';
                err.innerHTML = '';
            }
            // Update total counter (element removed; variable kept for future use)
            // Re-apply visual selection for rows loaded in this page
            $('#destTransferTable tbody tr').each(function () {
                var span = $(this).find('span.star-select');
                if (span.length) {
                    var id = String(span.data('transferId'));
                    if (id && selectedTransfers[id]) {
                        $(this).addClass('selected');
                        span.find('i').removeClass('bi-star').addClass('bi-star-fill text-warning');
                    }
                }
            });
            // Update selection info + last-refresh timestamp
            var STATUS_NAMES = {
                'INIT':'Arriving','SCHE':'Preset','FETC':'Fetching','HOLD':'StandBy',
                'WAIT':'Queued','EXEC':'Transferring','DONE':'Done','RETR':'ReQueued',
                'STOP':'Stopped','FAIL':'Failed','INTR':'Interrupted'
            };
            var status = document.getElementById('dt-status').value || 'All';
            var ds     = document.getElementById('dt-dataStream').value || 'All';
            var dtime  = document.getElementById('dt-dataTime').value || 'All';
            var diss   = document.getElementById('dt-dissStream').value || 'All';
            var date   = document.getElementById('dt-date').value || '*';
            var statusLabel = STATUS_NAMES[status] || status;
            var now    = new Date();
            var pad    = function(n) { return String(n).padStart(2, '0'); };
            var ts     = now.getUTCFullYear() + '-' + pad(now.getUTCMonth()+1) + '-' + pad(now.getUTCDate())
                       + ' ' + pad(now.getUTCHours()) + ':' + pad(now.getUTCMinutes()) + ':' + pad(now.getUTCSeconds());
            // Only include filters which are visible on the page (some pages hide certain selectors)
            var hasDiss   = $('th:contains("Dissem_Str")').length > 0;
            var hasDS     = $('th:contains("Data_Str")').length > 0;
            var hasDTime  = $('th:contains("Base_Time")').length > 0;
            var hasStatus = $('th:contains("Status")').length > 0;
            var hasDate   = $('th:contains("Prod_Date")').length > 0;
            var parts = [];
            if (hasDiss)  parts.push(document.getElementById('dt-dissStream').value || 'All');
            if (hasDS)    parts.push(document.getElementById('dt-dataStream').value || 'All');
            if (hasDTime) parts.push(document.getElementById('dt-dataTime').value || 'All');
            if (hasStatus)parts.push(statusLabel);
            if (hasDate)  parts.push(document.getElementById('dt-date').value || '*');
            var selectionText = parts.join('/');
            var fileCount = json && json.recordsFiltered != null ? json.recordsFiltered
                          : (json && json.recordsTotal != null ? json.recordsTotal : null);
            var selCount;
            if (window._pendingClientCount != null) {
                // A/N/R was called: trust the client-computed count and mark dirty
                window._clientTotal = window._pendingClientCount;
                window._clientDirty = true;
                window._pendingClientCount = null;
            } else if (window._clientDirty) {
                // Client has unsynchronised changes: keep _clientTotal, ignore server value
            } else {
                // Server is authoritative: sync from server total
                window._clientTotal = (json && json.totalSelected != null ? json.totalSelected : 0);
            }
            selCount = window._clientTotal || 0;
            document.getElementById('destSelectionInfo').innerHTML =
                '<span class="text-secondary">Selection:</span>'
                + '&ensp;<strong>' + (selectionText || 'All') + '</strong>'
                + '&ensp;<span class="vr"></span>&ensp;'
                + '<i class="bi bi-arrow-clockwise text-secondary"></i>&thinsp;<strong>' + ts + '</strong>&thinsp;<span class="text-secondary" style="font-size:0.75em">UTC</span>'
                + (fileCount !== null
                    ? '&ensp;<span class="vr"></span>&ensp;<span class="text-secondary">Files:</span>&ensp;<strong>' + Number(fileCount).toLocaleString() + '</strong>'
                    : '')
                + '&ensp;<span class="vr"></span>&ensp;'
                + '<a href="javascript:transferChange(\'validate\')" class="text-decoration-none fw-bold" title="Open basket to act on selected transfers">'
                + '<span class="text-secondary"><i class="bi bi-basket2-fill"></i>&thinsp;Basket:</span>&ensp;<span id="destSelectedSpan">' + selCount.toLocaleString() + '</span>'
                + '</a>';
            // Show/hide A/N/R selection controls based on whether any rows are present
            var hasRows = json && json.recordsFiltered > 0;
            var selCtrl = document.getElementById('destSelectionControls');
            if (selCtrl) selCtrl.classList.toggle('d-none', !hasRows);
            if (!hasRows) {
                var helpEl = document.getElementById('dtSelectionHelp');
                if (helpEl) helpEl.classList.remove('show');
            }
            // Restore scroll position after pill-click page reload (table must be rendered first)
            var _savedY = sessionStorage.getItem('hostChangeScrollY');
            if (_savedY !== null) {
                sessionStorage.removeItem('hostChangeScrollY');
                window.scrollTo({ top: parseInt(_savedY, 10), behavior: 'instant' });
            }
        }
    });

    var CUSTOM_COL_KEY  = 'destTransferCustomCols';
    var COL_MODE_KEY    = 'destTransferColMode';
    var _customCols = (function() {
        try {
            var s = localStorage.getItem(CUSTOM_COL_KEY);
            if (s) return JSON.parse(s);
        } catch(e) {}
        return [0,1,2,3,4,5,6,7,8,9,10<c:if test="${not empty ecpdsCanHandleQueue}">,11</c:if>,12]; // all visible by default
    })();

    function _applyCustomCols() {
        var total = _destTable.columns().count();
        for (var i = 0; i < total; i++) {
            var visible = _customCols.indexOf(i) !== -1;
            if (i === 5) visible = true;  // Target is mandatory
            if (i === 11 && !canQueue) visible = false;
            _destTable.column(i).visible(visible, false);
        }
        _destTable.columns.adjust();
    }

    function _syncCustomChkBoxes() {
        document.querySelectorAll('.custom-col-chk').forEach(function(chk) {
            chk.checked = _customCols.indexOf(+chk.dataset.col) !== -1;
        });
    }

    document.querySelectorAll('.custom-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _customCols.indexOf(col);
            if (this.checked && idx === -1) _customCols.push(col);
            else if (!this.checked && idx !== -1) _customCols.splice(idx, 1);
            try { localStorage.setItem(CUSTOM_COL_KEY, JSON.stringify(_customCols)); } catch(e) {}
            if (_colMode === 'custom') _applyCustomCols();
        });
    });

    var _colMode = (function() {
        try { return localStorage.getItem(COL_MODE_KEY) || 'auto'; } catch(e) { return 'auto'; }
    })();
    // Columns hidden at medium width (<992px) in auto mode: Err(0), Host(1), Scheduled(2), TS(6), %(7), Mbits/s(8), Prior(10)
    var _MED_COLS = [0, 1, 2, 6, 7, 8, 10];
    // Additional columns hidden at small width (<768px): Start(3), Finish(4)
    var _SM_COLS = [3, 4];
    // Compact: hide Err + all MED cols
    var _COMPACT_HIDE = [0].concat(_MED_COLS.filter(function(c){return c!==0;}));
    // Small: hide everything in Compact + SM cols
    var _SMALL_HIDE = _COMPACT_HIDE.concat(_SM_COLS);

    var _destTable = table;

    function _showCols(hideCols) {
        var total = _destTable.columns().count();
        for (var i = 0; i < total; i++) {
            var visible = hideCols.indexOf(i) === -1;
            if (i === 11 && !canQueue) {
                visible = false;
            }
            _destTable.column(i).visible(visible, false);
        }
        _destTable.columns.adjust();
    }

    function _applyResponsiveCols() {
        if (_colMode !== 'auto') return;
        var w = window.innerWidth;
        if (w < 768) {
            _showCols(_MED_COLS.concat(_SM_COLS));
        } else if (w < 992) {
            _showCols(_MED_COLS);
        } else {
            _showCols([]);
        }
    }

    $(window).on('resize', function(){ _applyResponsiveCols(); });

    // Restore persisted mode on load
    (function() {
        var mode = _colMode;
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        if (mode !== 'auto') {
            $('#colModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label)
                .removeClass('btn-outline-secondary').addClass('btn-primary');
        }
        document.getElementById('customColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        $('#colModeBtn').closest('.dropdown').find('.dropdown-item').each(function(){
            $(this).toggleClass('active', $(this).data('col-mode') === mode);
            $(this).find('i.bi-check').remove();
            if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        if (mode === 'all') {
            _showCols([]);
        } else if (mode === 'compact') {
            _showCols(_COMPACT_HIDE);
        } else if (mode === 'small') {
            _showCols(_SMALL_HIDE);
        } else if (mode === 'custom') {
            _syncCustomChkBoxes();
            _applyCustomCols();
        } else {
            _applyResponsiveCols();
        }
    })();

    $('#colModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e){
        e.preventDefault();
        var mode = $(this).data('col-mode');
        _colMode = mode;
        try { localStorage.setItem(COL_MODE_KEY, mode); } catch(e) {}
        // Update button label and active item
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#colModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        if (mode === 'auto') {
            $('#colModeBtn').removeClass('btn-primary').addClass('btn-outline-secondary');
        } else {
            $('#colModeBtn').removeClass('btn-outline-secondary').addClass('btn-primary');
        }
        document.getElementById('customColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        $(this).closest('.dropdown-menu').find('.dropdown-item').each(function(){
            $(this).toggleClass('active', $(this).data('col-mode') === mode);
            if (mode === 'auto') {
                $(this).find('i.bi-check').remove();
                if ($(this).data('col-mode') === 'auto') $(this).prepend('<i class="bi bi-check me-1"></i>');
            } else {
                $(this).find('i.bi-check').remove();
                if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            }
        });
        if (mode === 'auto') {
            _applyResponsiveCols();
        } else if (mode === 'all') {
            _showCols([]);
        } else if (mode === 'compact') {
            _showCols(_COMPACT_HIDE);
        } else if (mode === 'small') {
            _showCols(_SMALL_HIDE);
        } else if (mode === 'custom') {
            _syncCustomChkBoxes();
            _applyCustomCols();
        }
    });

    // Wire entries-per-page selector -- persist choice in localStorage
    document.getElementById('destPageLen').addEventListener('change', function () {
        var len = parseInt(this.value, 10);
        localStorage.setItem(STORAGE_KEY, len);
        table.page.len(len).draw();
    });

    // Expose table globally so checkAll() in javascript.jsp can access it
    window._destTransferTable = table;

    // _clientTotal     = the displayed basket count, updated by drawCallback and star-clicks.
    // _clientDirty     = true once the client has changed selection (A/N/R or star-click).
    // _fullSyncNeeded  = true when checkAll() was used; triggers replace-mode sync on submit.
    // _deltaAdd/_deltaDel = incremental star-click changes for delta-mode sync on submit.
    window._clientTotal = 0;
    window._clientDirty = false;
    window._fullSyncNeeded = false;
    window._deltaAdd = {};
    window._deltaDel = {};
    window._refreshSelectedCount = function () {
        var span = document.getElementById('destSelectedSpan');
        if (span) span.textContent = Math.max(0, window._clientTotal || 0).toLocaleString();
    };
})();
</script>
