<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<auth:if basePathKey="destination.basepath"
	paths="/operations/${destinationDetailActionForm.id}/activateHost/">
	<auth:then>
		<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
	</auth:then>
</auth:if>

<div class="row g-3 mt-1">

	<%-- Dissemination Hosts column --%>
	<div class="col-12<c:if test="${destination.acquisition}"> col-md-6</c:if>">
		<p class="fw-bold mb-1 d-flex align-items-center gap-1">
			Dissemination Host(s)
			<button class="btn btn-link btn-sm p-0 ms-1 text-muted" type="button"
				data-bs-toggle="collapse" data-bs-target="#disseminationHostsInfo"
				aria-expanded="false" aria-controls="disseminationHostsInfo"
				title="How host selection works">
				<i class="bi bi-info-circle"></i>
			</button>
		</p>
		<div class="collapse mb-2" id="disseminationHostsInfo">
			<div class="card-body py-2 px-3 border rounded mb-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
				<i class="bi bi-shuffle me-1"></i><strong>Failover host selection</strong> &mdash; When a transfer is dispatched, hosts are evaluated in ascending priority order (the lower the number, the higher the precedence). The system always starts with the highest-priority active host. If that host fails or becomes unavailable, it automatically falls back to the next host in the priority sequence. This ensures continuous delivery even when individual hosts experience issues.
			</div>
		</div>
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
				searching:    false,
				ordering:     true,
				info:         false,
				order:        [[1, 'asc']],
				dom:          'tp',
				columnDefs:   [{ type: 'num', targets: 1 }<c:if test="${not empty ecpdsCanHandleHosts}">, { orderable: false, targets: -1 }</c:if>],
				language:     { emptyTable: 'No hosts assigned' }
			});
		});
		</script>
	</div>

	<%-- Acquisition Hosts column --%>
	<c:if test="${destination.acquisition}">
		<div class="col-12 col-md-6">
			<p class="fw-bold mb-1 d-flex align-items-center gap-1">
				Acquisition Host(s)
				<button class="btn btn-link btn-sm p-0 ms-1 text-muted" type="button"
					data-bs-toggle="collapse" data-bs-target="#acquisitionHostsInfo"
					aria-expanded="false" aria-controls="acquisitionHostsInfo"
					title="How host selection works">
					<i class="bi bi-info-circle"></i>
				</button>
			</p>
			<div class="collapse mb-2" id="acquisitionHostsInfo">
				<div class="card-body py-2 px-3 border rounded mb-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
					<i class="bi bi-diagram-3 me-1"></i><strong>Parallel host processing</strong> &mdash; All active acquisition hosts are contacted <strong>simultaneously</strong>, regardless of their priority value. There is no failover or sequencing &mdash; every host participates in each acquisition cycle at the same time. The priority column is shown for reference only and has no effect on acquisition scheduling.
				</div>
			</div>
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
					<tr data-acq-host-id="${acquisitionHost.name.id}">
						<td>
							<c:if test="${acquisitionHost.name.active}">
								<span class="d-inline-flex align-items-center gap-1">
									<a title="This Host is Activated (id=${acquisitionHost.name.name})"
										href="/do/transfer/host/${acquisitionHost.name.name}">${acquisitionHost.name.nickName}</a>
									<span class="acq-state-indicator" style="display:inline-flex;align-items:center;width:1.2rem;justify-content:center;">
										<span class="acq-running-indicator" style="display:none;" title="Acquisition is currently running">
										<c:choose>
											<c:when test="${not empty ecpdsCanHandleHosts}">
												<button class="acq-interrupt-btn btn btn-link btn-sm p-0 border-0 text-warning" style="line-height:1;font-size:0.95rem;animation:_acqRunPulse 1.2s ease-in-out infinite;" title="Acquisition is running &mdash; click to interrupt" data-interrupt-host-id="${acquisitionHost.name.id}" onclick="_acqInterrupt(this)"><i class="bi bi-stop-fill"></i></button>
											</c:when>
											<c:otherwise>
												<span class="spinner-grow text-success" style="width:0.75rem;height:0.75rem;vertical-align:middle;" title="Acquisition is currently running"></span>
											</c:otherwise>
										</c:choose>
									</span>
										<c:if test="${not empty ecpdsCanHandleHosts}"><button class="acq-run-btn btn btn-link btn-sm p-0 border-0 text-success" style="display:none;line-height:1;font-size:0.95rem;" title="Trigger acquisition now" data-run-host-id="${acquisitionHost.name.id}" onclick="_acqTrigger(this)"><i class="bi bi-play-fill"></i></button></c:if>
									</span>
								</span>${acquisitionHostname}
							</c:if>
							<c:if test="${!acquisitionHost.name.active}">
								<span class="d-inline-flex align-items-center gap-1">
									<i class="bi bi-slash-circle-fill text-danger me-1" title="Host is disabled" style="font-size:0.85rem;"></i><a
										title="This Host is NOT Activated (id=${acquisitionHost.name.name})"
										href="/do/transfer/host/${acquisitionHost.name.name}"
										style="text-decoration:line-through;color:var(--bs-secondary-color)">${acquisitionHost.name.nickName}</a>
									<span style="display:inline-flex;align-items:center;width:1.2rem;justify-content:center;">
										<i class="bi bi-play-fill" style="font-size:0.95rem;opacity:0.25;color:var(--bs-secondary-color);" title="Host is disabled"></i>
									</span>
								</span>${acquisitionHostname}
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
									<a class="acq-stop-link" href="javascript:hostChange('deactivateHost','${acquisitionHost.name.id}')">
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
			<style>
			@keyframes _acqRunPulse {
				0%,100% { opacity:1; }
				50%      { opacity:0.35; }
			}
			</style>
			<script>
			$(document).ready(function() {
				$('#acquisitionHostsTable').DataTable({
					paging:       true,
					pageLength:   10,
					lengthChange: false,
					searching:    false,
					ordering:     true,
					info:         false,
					order:        [[1, 'asc']],
					dom:          'tp',
					columnDefs:   [{ type: 'num', targets: 1 }<c:if test="${not empty ecpdsCanHandleHosts}">, { orderable: false, targets: -1 }</c:if>],
					language:     { emptyTable: 'No hosts assigned' }
				});

				function _applyAcqRunning(hostId, running) {
					var row = $('#acquisitionHostsTable tr[data-acq-host-id="' + hostId + '"]');
					row.find('.acq-running-indicator').toggle(running);
					row.find('.acq-run-btn').toggle(!running);
				}
				function _pollAcqHosts() {
					$('#acquisitionHostsTable tr[data-acq-host-id]').each(function() {
						var hostId = $(this).data('acq-host-id');
						if (!hostId) return;
						fetch('/do/transfer/host/' + encodeURIComponent(hostId) + '?json=acquisitionRunning')
							.then(function(r) { return r.ok ? r.json() : null; })
							.then(function(data) { _applyAcqRunning(hostId, !!(data && data.running)); })
							.catch(function() {});
					});
				}
				_pollAcqHosts();
				var _acqPollTimer;
				function _schedulePoll() {
					_acqPollTimer = setTimeout(function() {
						if (document.visibilityState !== 'hidden') _pollAcqHosts();
						_schedulePoll();
					}, 4000);
				}
				_schedulePoll();
				document.addEventListener('visibilitychange', function() {
					if (document.visibilityState === 'visible') _pollAcqHosts();
				});

				window._acqInterrupt = function(btn) {
				var hostId = btn.getAttribute('data-interrupt-host-id');
				if (!hostId) return;
				var orig = btn.innerHTML;
				btn.disabled = true;
				btn.style.animation = 'none';
				btn.innerHTML = '<span class="spinner-border spinner-border-sm" style="width:0.65rem;height:0.65rem"></span>';
				fetch('/do/transfer/host/' + encodeURIComponent(hostId) + '?json=interruptAcquisition')
					.then(function(r) { return r.ok ? r.json() : null; })
					.then(function(data) {
						btn.innerHTML = orig;
						btn.disabled = false;
						btn.style.animation = '';
						if (data && data.interrupted) {
							_applyAcqRunning(hostId, false);
						}
					})
					.catch(function() { btn.innerHTML = orig; btn.disabled = false; btn.style.animation = ''; });
			};

			window._acqTrigger = function(btn) {
					var hostId = btn.getAttribute('data-run-host-id');
					if (!hostId) return;
					var orig = btn.innerHTML;
					btn.disabled = true;
					btn.innerHTML = '<span class="spinner-border spinner-border-sm" style="width:0.65rem;height:0.65rem"></span>';
					fetch('/do/transfer/host/' + encodeURIComponent(hostId) + '?json=triggerAcquisition')
						.then(function(r) { return r.ok ? r.json() : null; })
						.then(function(data) {
							btn.innerHTML = orig;
							btn.disabled = false;
							if (data && data.triggered) {
								_applyAcqRunning(hostId, true);
							}
						})
						.catch(function() { btn.innerHTML = orig; btn.disabled = false; });
				};
			});
			</script>
		</div>
		</c:if>

</div>

