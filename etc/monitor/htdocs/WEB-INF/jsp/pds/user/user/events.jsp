<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<form id="userEventSearchForm">
<div class="input-group input-group-sm mb-2" style="max-width:520px">
<span class="input-group-text"><i class="bi bi-search"></i></span>
<input id="userEventSearch" class="form-control" name="search" type="text" placeholder="Search.." title="Search is performed across the Web User, Action and Comment in case-sensitive" value="${param['search']}">
<button class="btn btn-outline-secondary" type="submit">Search</button>
</div>
</form>

<p class="fw-bold mb-1 mt-2">Events for <auth:link basePathKey="user.basepath" href="">All Web Users</auth:link></p>
<table id="userEventTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Time</th>
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
        pageLength: 25,
        searching: false,
        autoWidth: false,
        order: [[0, 'desc']],
        columns: [
            { orderable: true,  data: 0 },
            { orderable: true,  data: 1 },
            { orderable: true,  data: 2 },
            { orderable: true,  data: 3 },
            { orderable: false, data: 4 },
            { orderable: false, data: 5 },
            { orderable: false, data: 6, width: '48px' }
        ],
        columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
        createdRow: function(row, data) {
            $('td', row).each(function(i) { $(this).html(data[i]); });
        },
        dom: '<"d-flex align-items-start justify-content-between mt-2"i p>t<"d-flex align-items-start justify-content-between mt-2"i p>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading...',
            emptyTable: 'No Web Event Log available with the criteria selected'
        }
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
