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

	<auth:if basePathKey="admin.basepath" paths="/requeue">
	<auth:then>
	<div class="mt-3 d-flex gap-2">
		<button type="button" id="requeueBtn" class="btn btn-sm btn-warning" disabled data-bs-toggle="modal" data-bs-target="#requeueConfirmModal">
			<i class="bi bi-arrow-repeat"></i> Requeue all
		</button>
		<auth:if basePathKey="admin.basepath" paths="/delete">
		<auth:then>
		<button type="button" id="deleteBtn" class="btn btn-sm btn-danger" disabled data-bs-toggle="modal" data-bs-target="#deleteConfirmModal">
			<i class="bi bi-trash"></i> Delete all
		</button>
		</auth:then>
		</auth:if>
	</div>

	<!-- Requeue confirmation modal -->
	<div class="modal fade" id="requeueConfirmModal" tabindex="-1" aria-labelledby="requeueConfirmModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title" id="requeueConfirmModalLabel"><i class="bi bi-arrow-repeat text-warning"></i> Confirm Requeue</h5>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body" id="requeueModalBody"></div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
					<a id="requeueConfirmBtn" href="#" class="btn btn-warning">
						<i class="bi bi-arrow-repeat"></i> Requeue
					</a>
				</div>
			</div>
		</div>
	</div>

	<!-- Delete confirmation modal -->
	<div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-labelledby="deleteConfirmModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title" id="deleteConfirmModalLabel"><i class="bi bi-trash text-danger"></i> Confirm Delete</h5>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body" id="deleteModalBody"></div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
					<a id="deleteConfirmBtn" href="#" class="btn btn-danger">
						<i class="bi bi-trash"></i> Delete
					</a>
				</div>
			</div>
		</div>
	</div>
	</auth:then>
	</auth:if>

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
			searching: true,
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
				var json = settings.json || {};
				var total = json.recordsTotal || 0;
				var filtered = json.recordsFiltered || 0;
				var label = '<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> outstanding transfer(s)';
				if (filtered !== total) {
					label += ' &mdash; <strong>' + filtered + '</strong> matching filter';
				}
				$('#badTransfersFoundLabel').html(label);
				$('#requeueBtn, #deleteBtn').prop('disabled', total === 0);
			},
			language: {
				lengthMenu: 'Show _MENU_ per page',
				info: 'Showing _START_-_END_ of _TOTAL_',
				processing: 'Loading...',
				emptyTable: 'No outstanding transfers found',
				search: 'Filter:'
			}
		});

		var _dest = '<c:out value="${destination.name}"/>';

		function _buildModalContent(action, search, filtered, total) {
			var safeSearch = $('<span>').text(search).html();
			var dest = '<strong>' + $('<span>').text(_dest).html() + '</strong>';
			var isRequeue = action === 'requeue';
			var baseUrl = '/do/admin/' + action + '/' + encodeURIComponent(_dest)
				+ '?' + (isRequeue ? 'restart=true' : 'delete=true');
			if (search) {
				baseUrl += '&search=' + encodeURIComponent(search);
			}
			var count = search ? filtered : total;
			var scope = search
				? '<strong>' + count + '</strong> transfer(s) matching filter <strong>&ldquo;' + safeSearch + '&rdquo;</strong> for destination ' + dest
				: '<strong>all ' + total + '</strong> outstanding transfer(s) for destination ' + dest;
			var body = isRequeue
				? 'Are you sure you want to requeue ' + scope + '?'
				: 'Are you sure you want to <strong>permanently delete</strong> ' + scope + '? This action cannot be undone.';
			return { url: baseUrl, body: body };
		}

		$('#requeueConfirmModal').on('show.bs.modal', function() {
			var search = _table.search();
			var info = _table.page.info();
			var c = _buildModalContent('requeue', search, info.recordsDisplay, info.recordsTotal);
			$('#requeueModalBody').html(c.body);
			$('#requeueConfirmBtn').attr('href', c.url);
		});

		$('#deleteConfirmModal').on('show.bs.modal', function() {
			var search = _table.search();
			var info = _table.page.info();
			var c = _buildModalContent('delete', search, info.recordsDisplay, info.recordsTotal);
			$('#deleteModalBody').html(c.body);
			$('#deleteConfirmBtn').attr('href', c.url);
		});
	});
	</script>
</c:if>
