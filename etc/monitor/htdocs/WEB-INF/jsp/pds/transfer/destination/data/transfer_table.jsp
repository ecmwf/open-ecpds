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

<%-- Entries-per-page row — also shows current filter selection and last-refresh time --%>
<div class="d-flex align-items-center gap-2 my-2 flex-wrap">
  <span class="d-flex align-items-center gap-2 small bg-body-tertiary border rounded px-2 py-1" id="destSelectionInfo"></span>
  <span class="ms-auto text-muted small">Show</span>
  <select id="destPageLen" class="form-select form-select-sm" style="width:auto">
    <option value="10">10</option>
    <option value="25">25</option>
    <option value="50">50</option>
    <option value="100">100</option>
    <option value="250">250</option>
  </select>
  <span class="text-muted small">entries per page</span>
</div>

<%-- DataTable: 13 columns (Actions hidden when user lacks queue access) --%>
<table id="destTransferTable" class="table table-striped table-sm table-hover w-100">
  <thead>
    <tr>
      <th>Err</th>
      <th>Host</th>
      <th>Sched. Time</th>
      <th>Start Time</th>
      <th>Finish Time</th>
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

<%-- Selection controls (A/N/R) and select-filtered button, below the table --%>
<div class="d-flex align-items-center justify-content-end gap-3 mt-1">
  <span class="d-flex gap-1 align-items-center">
    <a href="javascript:checkAll(true,false)"  title="Select All"        class="text-decoration-none text-body">A</a>
    <span class="text-muted">/</span>
    <a href="javascript:checkAll(false,false)" title="Unselect All"      class="text-decoration-none text-body">N</a>
    <span class="text-muted">/</span>
    <a href="javascript:checkAll(false,true)"  title="Reverse Selection" class="text-decoration-none text-body">R</a>
  </span>
  <span class="d-flex gap-1 align-items-center">
    <a href="javascript:transferChange('selectFiltered')" title="Select all filtered transfers">
      <i class="bi bi-arrow-right-circle"></i>
    </a>
    <span id="destTransferTotal" class="text-muted small"></span>
  </span>
</div>

<script>
(function () {
    var STORAGE_KEY = 'destTransferPageLen';
    var savedLen = parseInt(localStorage.getItem(STORAGE_KEY), 10) || 25;

    // Reflect stored value in the selector
    var sel = document.getElementById('destPageLen');
    sel.value = String(savedLen);
    if (!sel.value) { sel.value = '25'; savedLen = 25; } // fallback if stored value not in list

    var canQueue = document.getElementById('dt-can-queue').value === 'true';

    var table = $('#destTransferTable').DataTable({
        serverSide: true,
        processing: true,
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
            { data: 0 },
            { data: 1 },
            { data: 2 },
            { data: 3 },
            { data: 4 },
            { data: 5 },
            { data: 6 },
            { data: 7 },
            { data: 8 },
            { data: 9 },
            { data: 10 },
            { data: 11 },
            { data: 12 }
        ],
        columnDefs: [
            { targets: 0,  orderable: false },
            { targets: 11, orderable: false, visible: canQueue },
            { targets: 12, orderable: false }
        ],
        order: [[2, 'desc']],
        dom: "t<'d-flex align-items-center mt-2'i<'ms-auto'p>>",
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
                err.style.display = '';
                var hint = 'You can conduct an extensive search using the target, source, ts, priority, '
                    + 'groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules.<br>'
                    + 'For instance: <code>asap=yes target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i</code>';
                err.innerHTML = '<strong>Query error:</strong> ' + json.error + '<br><small class="text-muted">' + hint + '</small>';
            } else {
                err.style.display = 'none';
                err.innerHTML = '';
            }
            // Update total counter in tfoot
            var total = json && json.recordsTotal != null ? json.recordsTotal : '';
            document.getElementById('destTransferTotal').textContent = total;
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
            var ts     = now.getFullYear() + '-' + pad(now.getMonth()+1) + '-' + pad(now.getDate())
                       + ' ' + pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds());
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
            document.getElementById('destSelectionInfo').innerHTML =
                '<span class="text-secondary">Selection:</span>'
                + '&ensp;<strong>' + (selectionText || 'All') + '</strong>'
                + '&ensp;<span class="vr"></span>&ensp;'
                + '<i class="bi bi-arrow-clockwise text-secondary"></i>&thinsp;<strong>' + ts + '</strong>';
        }
    });

    // Wire entries-per-page selector — persist choice in localStorage
    document.getElementById('destPageLen').addEventListener('change', function () {
        var len = parseInt(this.value, 10);
        localStorage.setItem(STORAGE_KEY, len);
        table.page.len(len).draw();
    });

    // Expose table globally so checkAll() in javascript.jsp can access it
    window._destTransferTable = table;
})();
</script>
