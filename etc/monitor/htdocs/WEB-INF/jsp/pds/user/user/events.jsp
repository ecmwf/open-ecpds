<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock-history text-primary"></i>
<span class="fw-semibold">Events for <auth:link basePathKey="user.basepath" href="">All Web Users</auth:link></span>
<div class="ms-auto d-flex align-items-center gap-2">
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
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="userEventTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
    <thead class="table-light">
        <tr>
            <th title="Time (UTC)">Time</th>
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
