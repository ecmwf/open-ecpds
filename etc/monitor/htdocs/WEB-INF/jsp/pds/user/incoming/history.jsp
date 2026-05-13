<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<form id="incomingHistorySearchForm">
<div class="input-group input-group-sm mb-2" style="max-width:520px">
<span class="input-group-text"><i class="bi bi-search"></i></span>
<input id="incomingHistorySearch" class="form-control" name="search" type="text" placeholder="Search.." title="Search is performed against the File Name in case-sensitive" value="${param['search']}">
<button class="btn btn-outline-secondary" type="submit">Search</button>
</div>
</form>

<p class="fw-bold mb-1 mt-2">Events for <auth:link basePathKey="incoming.basepath" href="">All Data Users</auth:link></p>
<table id="incomingHistoryTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
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

<script>
var _incomingHistoryTable;
function incomingHistoryTableReload() {
    if (_incomingHistoryTable) {
        _incomingHistoryTable.ajax.reload();
    }
}
$(function() {
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
        pageLength: 25,
        searching: false,
        autoWidth: false,
        order: [[5, 'desc']],
        columns: [
            { orderable: true,  data: 0 },
            { orderable: true,  data: 1 },
            { orderable: true,  data: 2 },
            { orderable: true,  data: 3 },
            { orderable: true,  data: 4 },
            { orderable: true,  data: 5 },
            { orderable: false, data: 6 },
            { orderable: false, data: 7 },
            { orderable: true,  data: 8 }
        ],
        columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
        createdRow: function(row, data) {
            $('td', row).each(function(i) { $(this).html(data[i]); });
        },
        dom: '<"d-flex align-items-start justify-content-between mt-2"i p>t<"d-flex align-items-start justify-content-between mt-2"i p>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading...',
            emptyTable: 'No Data Events found based on these criteria!'
        }
    });
    $('#incomingHistorySearchForm').on('submit', function(e) {
        e.preventDefault();
        incomingHistoryTableReload();
    });
    $('#incomingHistorySearch').on('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            incomingHistoryTableReload();
        }
    });
});
</script>
