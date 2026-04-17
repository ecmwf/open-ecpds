<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
	<auth:then>
		<c:set var="authorized" value="true" />
	</auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
	</auth:then>
	<auth:else>
		<auth:if basePathKey="destination.basepath"
			paths="/${destination.name}">
			<auth:then>
				<c:set var="authorized" value="true" />
			</auth:then>
		</auth:if>
	</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
	<div class="alert alert-danger">
		Error retrieving object by key &larr; Destination not found: ${destination.name}
	</div>
</c:if>

<c:if test="${authorized == 'true'}">
	<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

	<div class="d-flex align-items-center mb-2 mt-2">
		<span class="text-muted small" id="badTransfersFoundLabel"><i class="bi bi-list-ul"></i> Loading...</span>
	</div>

	<table id="badTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
		<thead class="table-light">
			<tr>
				<th>Host</th>
				<th>Target</th>
				<th>Status</th>
				<th>%</th>
				<th>B/s</th>
				<th>Priority</th>
				<th>Comment</th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>

	<div class="mt-2">
		<auth:link basePathKey="admin.basepath"
			href="/requeue?restart=true" imageKey="icon.requeue"
			imageTitleKey="ecpds.destination.requeue"
			imageAltKey="ecpds.destination.requeue" />
		&nbsp;&nbsp;
		<auth:link basePathKey="admin.basepath"
			href="/delete?delete=true" imageKey="icon.delete"
			imageTitleKey="ecpds.destination.delete"
			imageAltKey="ecpds.destination.delete" />
	</div>

	<script>
	$(function() {
		var _table = $('#badTransfersTable').DataTable({
			serverSide: true,
			processing: true,
			ajax: {
				url: '/do/monitoring/unsuccessful/list/${destination.name}',
				type: 'GET'
			},
			pageLength: 25,
			searching: false,
			autoWidth: false,
			order: [[0, 'asc']],
			columns: [
				{ orderable: true,  data: 0 },
				{ orderable: true,  data: 1 },
				{ orderable: true,  data: 2 },
				{ orderable: false, data: 3 },
				{ orderable: false, data: 4 },
				{ orderable: true,  data: 5 },
				{ orderable: true,  data: 6 }
			],
			columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
			createdRow: function(row, data) {
				$('td', row).each(function(i) { $(this).html(data[i]); });
			},
			drawCallback: function(settings) {
				var total = settings.json ? settings.json.recordsTotal : 0;
				$('#badTransfersFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> outstanding transfer(s)');
			},
			language: {
				lengthMenu: 'Show _MENU_ per page',
				info: 'Showing _START_-_END_ of _TOTAL_',
				processing: 'Loading...',
				emptyTable: 'No outstanding transfers found'
			}
		});
	});
	</script>
</c:if>
