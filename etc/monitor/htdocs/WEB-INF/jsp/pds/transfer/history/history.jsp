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
<table id="transferHistoryTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Error</th>
            <th>Event Time</th>
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
    $('#transferHistoryTable').DataTable({
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
        pageLength: 25,
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
        dom: '<"d-flex align-items-start justify-content-between mt-2"i p>t<"d-flex align-items-start justify-content-between mt-2"i p>',
        language: {
            info: 'Showing _START_-_END_ of _TOTAL_',
            processing: 'Loading...',
            emptyTable: 'No transfer history available for the selected criteria.'
        }
    });
});
</script>
