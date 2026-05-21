<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${not empty destination}">
<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>
</c:if>
<tiles:insert name="date.select" />
<div class="mb-2"></div>
<c:if test="${empty destination}">
<tiles:insert name="destination.select" />
</c:if>
<script>
(function() {
    var destName = '<c:out value="${selectedDestination.name}" />';
    if (!destName) return;
    document.querySelectorAll('.date-pill').forEach(function(pill) {
        var href = pill.getAttribute('href') || '';
        if (href.indexOf('destinationName') === -1) {
            pill.setAttribute('href', href + '&destinationName=' + encodeURIComponent(destName));
        }
    });
})();
</script>

<c:if test="${historyItemsSize == '0'}">
    <div class="alert alert-info d-flex align-items-center gap-2 mt-3">
        <i class="bi bi-info-circle-fill"></i>
        <span>No transfer history available for destination <strong><c:out value="${selectedDestination.name}" /></strong> on <c:out value="${selectedDate}" />.</span>
    </div>
</c:if>

<div class="mt-3">
<div class="d-flex gap-2 justify-content-end align-items-center mb-2">
  <div class="input-group flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="histPageLen" class="form-select" style="width:auto">
      <option value="10">10</option>
      <option value="25">25</option>
      <option value="50">50</option>
      <option value="100">100</option>
      <option value="250">250</option>
    </select>
  </div>
  <div class="dropdown">
    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="histColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
      <i class="bi bi-layout-three-columns me-1"></i>Auto
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="histColModeBtn">
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
        <small class="d-block text-muted ms-0">Hides: Error, Transfer Host</small>
      </a></li>
      <li><hr class="dropdown-divider"></li>
      <li><a class="dropdown-item" href="#" data-col-mode="custom">
        <strong>Custom</strong>
        <small class="d-block text-muted ms-0">Choose individual columns</small>
      </a></li>
      <li id="histCustomColChkPanel" style="display:none;">
        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
          <div class="form-check mb-0"><input class="form-check-input hh-custom-col-chk" type="checkbox" id="hhchk-0" data-col="0" checked><label class="form-check-label" for="hhchk-0">Error</label></div>
          <div class="form-check mb-0"><input class="form-check-input hh-custom-col-chk" type="checkbox" id="hhchk-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="hhchk-1">Event Time <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input hh-custom-col-chk" type="checkbox" id="hhchk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="hhchk-2">Status <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input hh-custom-col-chk" type="checkbox" id="hhchk-3" data-col="3" checked><label class="form-check-label" for="hhchk-3">Transfer Host</label></div>
          <div class="form-check mb-0"><input class="form-check-input hh-custom-col-chk" type="checkbox" id="hhchk-4" data-col="4" checked><label class="form-check-label" for="hhchk-4">Comment</label></div>
        </div>
      </li>
    </ul>
  </div>
</div>
<table id="transferHistoryTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Error</th>
            <th title="Event Time (UTC)">Event Time</th>
            <th>Status</th>
            <th>Transfer Host</th>
            <th>Comment</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>

<script>
$(function() {
    var selectedDestinationName = '<c:out value="${selectedDestination.name}" />';
    var selectedDate = '${selectedDate}';
    var selectedMode = '${param['mode']}';
    var dt = $('#transferHistoryTable').DataTable({
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/transfer/history/list',
            type: 'GET',
            data: function(d) {
                d.destinationName = selectedDestinationName;
                d.date = selectedDate;
                d.mode = selectedMode;
            }
        },
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('histPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching: false,
        autoWidth: false,
        order: [[1, 'desc']],
        columns: [
            { orderable: true,  data: 0, width: '5em' },
            { orderable: true,  data: 1 },
            { orderable: true,  data: 2 },
            { orderable: true,  data: 3 },
            { orderable: false, data: 4 }
        ],
        columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
        createdRow: function(row, data) {
            $('td', row).each(function(i) { $(this).html(data[i]); });
        },
        dom: 't<"d-flex align-items-start mt-2"i<"ms-auto"p>>',
        language: {
            processing: '<span class="spinner-border spinner-border-sm me-1"></span> Loading&hellip;',
            emptyTable: 'No transfer history available for the selected criteria.'
        }
    });
    $('#histPageLen').val((function() { try { var v = parseInt(localStorage.getItem('histPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })());
    $('#histPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('histPageLen', len); } catch(e) {}
        dt.page.len(len).draw();
    });

    // Column-mode dropdown
    var HH_CUSTOM_COL_KEY = 'histCustomCols';
    var HH_COL_MODE_KEY   = 'histColMode';
    var _hhCustomCols = (function() {
        try { var s = localStorage.getItem(HH_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
        return [0,1,2,3,4];
    })();
    var _hhColMode = (function() {
        try { return localStorage.getItem(HH_COL_MODE_KEY) || 'auto'; } catch(e) { return 'auto'; }
    })();
    var _HH_MED = [0];      // hide Error on medium screens
    var _HH_SM  = [3];      // additionally hide Transfer Host on small screens

    function _hhShowCols(hideCols) {
        dt.columns().every(function(i) {
            dt.column(i).visible(hideCols.indexOf(i) === -1, false);
        });
        dt.columns.adjust();
    }

    function _hhApplyCustomCols() {
        dt.columns().every(function(i) {
            var vis = _hhCustomCols.indexOf(i) !== -1;
            if (i === 1 || i === 2) vis = true; // Event Time, Status: mandatory
            dt.column(i).visible(vis, false);
        });
        dt.columns.adjust();
    }

    function _hhSyncCustomChkBoxes() {
        document.querySelectorAll('.hh-custom-col-chk').forEach(function(chk) {
            chk.checked = _hhCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }

    document.querySelectorAll('.hh-custom-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _hhCustomCols.indexOf(col);
            if (this.checked && idx === -1) _hhCustomCols.push(col);
            else if (!this.checked && idx !== -1) _hhCustomCols.splice(idx, 1);
            try { localStorage.setItem(HH_CUSTOM_COL_KEY, JSON.stringify(_hhCustomCols)); } catch(e) {}
            if (_hhColMode === 'custom') _hhApplyCustomCols();
        });
    });

    function _hhApplyAuto() {
        if (_hhColMode !== 'auto') return;
        var w = window.innerWidth;
        if (w < 768)      _hhShowCols(_HH_MED.concat(_HH_SM));
        else if (w < 992) _hhShowCols(_HH_MED);
        else              _hhShowCols([]);
    }

    function _hhApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#histColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        if (mode === 'auto') {
            $('#histColModeBtn').removeClass('btn-primary').addClass('btn-outline-secondary');
        } else {
            $('#histColModeBtn').removeClass('btn-outline-secondary').addClass('btn-primary');
        }
        document.getElementById('histCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        $('#histColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        if (mode === 'auto')     { _hhApplyAuto(); }
        else if (mode === 'all') { _hhShowCols([]); }
        else if (mode === 'compact') { _hhShowCols([0, 3]); }
        else if (mode === 'custom') { _hhApplyCustomCols(); _hhSyncCustomChkBoxes(); }
    }

    $('#histColModeBtn').closest('.dropdown').on('click', '[data-col-mode]', function(e) {
        e.preventDefault();
        _hhColMode = $(this).data('col-mode');
        try { localStorage.setItem(HH_COL_MODE_KEY, _hhColMode); } catch(e) {}
        _hhApplyMode(_hhColMode);
    });

    $(window).on('resize', function() { _hhApplyAuto(); });

    _hhApplyMode(_hhColMode);
});
</script>
