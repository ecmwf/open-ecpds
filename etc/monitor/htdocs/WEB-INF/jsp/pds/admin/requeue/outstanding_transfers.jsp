<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="d-flex align-items-center mb-2 mt-2">
	<span class="text-muted small" id="requeueFoundLabel"><i class="bi bi-list-ul"></i> Loading...</span>
</div>

<table id="requeueTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
	<thead class="table-light">
		<tr>
			<th>Destination</th>
			<th>Host</th>
			<th>Target</th>
			<th>Status</th>
			<th>%</th>
			<th>B/s</th>
			<th>Priority</th>
			<th>Comment</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach var="t" items="${transfers}">
		<c:catch var="progressEx"><c:set var="pct" value="${t.progress}"/></c:catch>
		<c:if test="${progressEx != null}"><c:set var="pct" value="0"/></c:if>
		<c:catch var="sentEx"><c:set var="sentBytes" value="${t.sent}"/></c:catch>
		<c:if test="${sentEx != null}"><c:set var="sentBytes" value="0"/></c:if>
		<c:catch var="durEx"><c:set var="durMs" value="${t.duration}"/></c:catch>
		<c:if test="${durEx != null}"><c:set var="durMs" value="0"/></c:if>
		<tr>
			<td><a href="/do/transfer/destination/<c:out value="${t.destinationName}"/>" class="text-decoration-none"><c:out value="${t.destinationName}"/></a></td>
			<td><c:out value="${t.hostNickName}"/></td>
			<td><a href="/do/transfer/data/<c:out value="${t.id}"/>" class="text-decoration-none"><c:out value="${t.target}"/></a></td>
			<td><c:out value="${t.statusCode}"/></td>
			<td><c:out value="${pct}"/></td>
			<td><c:choose>
				<c:when test="${durMs > 0}"><c:out value="${sentBytes * 1000 / durMs}"/></c:when>
				<c:otherwise>0</c:otherwise>
			</c:choose></td>
			<td><c:out value="${t.priority}"/></td>
			<td><c:out value="${t.comment}"/></td>
		</tr>
	</c:forEach>
	</tbody>
</table>

<script>
$(function() {
	$('#requeueTransfersTable').DataTable({
		paging:       true,
		pageLength:   25,
		lengthChange: true,
		searching:    true,
		ordering:     true,
		info:         true,
		language: {
			emptyTable: 'No outstanding transfers found',
			info: 'Showing _START_-_END_ of _TOTAL_ outstanding transfer(s)',
			search: 'Filter:'
		},
		drawCallback: function(settings) {
			var api = this.api();
			var total = api.rows().count();
			var filtered = api.rows({ search: 'applied' }).count();
			var label = '<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> outstanding transfer(s)';
			if (filtered !== total) {
				label += ' &mdash; <strong>' + filtered + '</strong> matching filter';
			}
			$('#requeueFoundLabel').html(label);
		}
	});
});
</script>

<auth:if basePathKey="admin.basepath" paths="/requeue">
<auth:then>
<div class="mt-3 d-flex gap-2">
	<button type="button" class="btn btn-sm btn-warning" ${empty transfers ? 'disabled' : ''} data-bs-toggle="modal" data-bs-target="#requeueConfirmModal">
		<i class="bi bi-arrow-repeat"></i> Requeue all
	</button>
	<auth:if basePathKey="admin.basepath" paths="/delete">
	<auth:then>
	<button type="button" class="btn btn-sm btn-danger" ${empty transfers ? 'disabled' : ''} data-bs-toggle="modal" data-bs-target="#deleteConfirmModal">
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
			<div class="modal-body">
				Are you sure you want to requeue all outstanding transfers across <strong>all destinations</strong>?
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
				<a href="/do/admin/requeue?restart=true" class="btn btn-warning">
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
			<div class="modal-body">
				Are you sure you want to <strong>permanently delete</strong> all outstanding transfers across <strong>all destinations</strong>? This action cannot be undone.
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
				<a href="/do/admin/delete?delete=true" class="btn btn-danger">
					<i class="bi bi-trash"></i> Delete
				</a>
			</div>
		</div>
	</div>
</div>
</auth:then>
</auth:if>

