<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tr>

	<auth:if basePathKey="transferhistory.basepath" paths="/">
		<auth:then>
			<td valign="top" style="padding-right:2rem;width:50%">
		</auth:then>
		<auth:else>
			<td valign="top" colspan="2">
		</auth:else>
	</auth:if>

	<auth:if basePathKey="destination.basepath"
		paths="/operations/${destinationDetailActionForm.id}/activateHost/">
		<auth:then>
			<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
		</auth:then>
	</auth:if>

	<p class="fw-bold mb-1 mt-2">Dissemination Host(s)</p>
	<table id="disseminationHostsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
		<thead class="table-light">
			<tr>
				<th>Name</th>
				<th>Priority</th>
				<c:if test="${not empty ecpdsCanHandleHosts}">
					<th class="text-center">Actions</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="disseminationHost" items="${destination.disseminationHostsAndPriorities}">
			<c:set var="disseminationHostname" value=" (${disseminationHost.name.host})" scope="page" />
			<c:if test="${disseminationHostname eq ' ()'}">
				<c:set var="disseminationHostname" value="" scope="page" />
			</c:if>
			<tr>
				<td>
					<c:if test="${disseminationHost.name.active}">
						<a title="This Host is Activated (id=${disseminationHost.name.name})"
							href="/do/transfer/host/${disseminationHost.name.name}">${disseminationHost.name.nickName}</a>${disseminationHostname}
					</c:if>
					<c:if test="${!disseminationHost.name.active}">
						<i class="bi bi-slash-circle-fill text-danger me-1" title="Host is disabled" style="font-size:0.85rem;"></i><a
							title="This Host is NOT Activated (id=${disseminationHost.name.name})"
							href="/do/transfer/host/${disseminationHost.name.name}"
							style="text-decoration:line-through;color:var(--bs-secondary-color)">${disseminationHost.name.nickName}</a>${disseminationHostname}
					</c:if>
				</td>
				<td>${disseminationHost.value}</td>
				<c:if test="${not empty ecpdsCanHandleHosts}">
					<td class="text-center" style="white-space:nowrap">
						<c:if test="${!disseminationHost.name.active}">
							<a href="javascript:hostChange('activateHost','${disseminationHost.name.id}')">
								<content:icon key="icon.small.requeue" titleKey="ecpds.destination.activateHost" altKey="ecpds.destination.activateHost" writeFullTag="true" height="12" width="12" />
							</a>
						</c:if>
						<c:if test="${disseminationHost.name.active}">
							<a href="javascript:hostChange('deactivateHost','${disseminationHost.name.id}')">
								<content:icon key="icon.small.stop" titleKey="ecpds.destination.deactivateHost" altKey="ecpds.destination.deactivateHost" writeFullTag="true" height="9" width="9" />
							</a>
						</c:if>
						<a href="javascript:hostChange('increaseHostPriority','${disseminationHost.name.id}')">
							<content:icon key="icon.small.increase" titleKey="ecpds.destination.increaseHostPriority" altKey="ecpds.destination.increasePriority" writeFullTag="true" height="9" width="7" />
						</a>
						<a href="javascript:hostChange('decreaseHostPriority','${disseminationHost.name.id}')">
							<content:icon key="icon.small.decrease" titleKey="ecpds.destination.decreaseHostPriority" altKey="ecpds.destination.decreasePriority" writeFullTag="true" height="9" width="7" />
						</a>
						<auth:if basePathKey="transferhistory.basepath" paths="/">
							<auth:then>
								<a href="javascript:hostChange('duplicateHost','${disseminationHost.name.id}')">
									<content:icon key="icon.small.duplicate" titleKey="ecpds.destination.duplicateHost" altKey="ecpds.destination.duplicateHost" writeFullTag="true" height="9" width="7" />
								</a>
							</auth:then>
						</auth:if>
					</td>
				</c:if>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<script>
	$(document).ready(function() {
		$('#disseminationHostsTable').DataTable({
			paging:       true,
			pageLength:   10,
			lengthChange: false,
			searching: false,
			ordering:  true,
			info:      false,
			language:  { emptyTable: 'No hosts assigned' }<c:if test="${not empty ecpdsCanHandleHosts}">,
			columnDefs: [{ orderable: false, targets: -1 }]</c:if>
		});
	});
	</script>

	</td>


	<c:if test="${destination.acquisition}">

		<td valign="top" style="width:50%">
			<auth:if basePathKey="destination.basepath"
				paths="/operations/${destinationDetailActionForm.id}/activateHost/">
				<auth:then>
					<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
				</auth:then>
			</auth:if>
			<p class="fw-bold mb-1 mt-2">Acquisition Host(s)</p>
			<table id="acquisitionHostsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
				<thead class="table-light">
					<tr>
						<th>Name</th>
						<th>Priority</th>
						<c:if test="${not empty ecpdsCanHandleHosts}">
							<th class="text-center">Actions</th>
						</c:if>
					</tr>
				</thead>
				<tbody>
				<c:forEach var="acquisitionHost" items="${destination.acquisitionHostsAndPriorities}">
					<c:set var="acquisitionHostname" value=" (${acquisitionHost.name.host})" scope="page" />
					<c:if test="${acquisitionHostname eq ' ()'}">
						<c:set var="acquisitionHostname" value="" scope="page" />
					</c:if>
					<tr>
						<td>
							<c:if test="${acquisitionHost.name.active}">
								<a title="This Host is Activated (id=${acquisitionHost.name.name})"
									href="/do/transfer/host/${acquisitionHost.name.name}">${acquisitionHost.name.nickName}</a>${acquisitionHostname}
							</c:if>
							<c:if test="${!acquisitionHost.name.active}">
								<i class="bi bi-slash-circle-fill text-danger me-1" title="Host is disabled" style="font-size:0.85rem;"></i><a
									title="This Host is NOT Activated (id=${acquisitionHost.name.name})"
									href="/do/transfer/host/${acquisitionHost.name.name}"
									style="text-decoration:line-through;color:var(--bs-secondary-color)">${acquisitionHost.name.nickName}</a>${acquisitionHostname}
							</c:if>
						</td>
						<td>${acquisitionHost.value}</td>
						<c:if test="${not empty ecpdsCanHandleHosts}">
							<td class="text-center" style="white-space:nowrap">
								<c:if test="${!acquisitionHost.name.active}">
									<a href="javascript:hostChange('activateHost','${acquisitionHost.name.id}')">
										<content:icon key="icon.small.requeue" titleKey="ecpds.destination.activateHost" altKey="ecpds.destination.activateHost" writeFullTag="true" height="12" width="12" />
									</a>
								</c:if>
								<c:if test="${acquisitionHost.name.active}">
									<a href="javascript:hostChange('deactivateHost','${acquisitionHost.name.id}')">
										<content:icon key="icon.small.stop" titleKey="ecpds.destination.deactivateHost" altKey="ecpds.destination.deactivateHost" writeFullTag="true" height="9" width="9" />
									</a>
								</c:if>
								<a href="javascript:hostChange('increaseHostPriority','${acquisitionHost.name.id}')">
									<content:icon key="icon.small.increase" titleKey="ecpds.destination.increaseHostPriority" altKey="ecpds.destination.increasePriority" writeFullTag="true" height="9" width="7" />
								</a>
								<a href="javascript:hostChange('decreaseHostPriority','${acquisitionHost.name.id}')">
									<content:icon key="icon.small.decrease" titleKey="ecpds.destination.decreaseHostPriority" altKey="ecpds.destination.decreasePriority" writeFullTag="true" height="9" width="7" />
								</a>
								<auth:if basePathKey="transferhistory.basepath" paths="/">
									<auth:then>
										<a href="javascript:hostChange('duplicateHost','${acquisitionHost.name.id}')">
											<content:icon key="icon.small.duplicate" titleKey="ecpds.destination.duplicateHost" altKey="ecpds.destination.duplicateHost" writeFullTag="true" height="9" width="7" />
										</a>
									</auth:then>
								</auth:if>
							</td>
						</c:if>
					</tr>
				</c:forEach>
				</tbody>
			</table>
			<script>
			$(document).ready(function() {
				var dt = $('#acquisitionHostsTable').DataTable({
					paging:       true,
					pageLength:   10,
					lengthChange: false,
					searching: false,
					ordering:  true,
					info:      false,
					language:  { emptyTable: 'No hosts assigned' }<c:if test="${not empty ecpdsCanHandleHosts}">,
					columnDefs: [{ orderable: false, targets: -1 }]</c:if>
				});
			});
			</script>
		</td>

	</c:if>

</tr>
