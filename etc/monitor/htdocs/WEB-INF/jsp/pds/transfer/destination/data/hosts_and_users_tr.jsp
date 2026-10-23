<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<auth:if basePathKey="destination.basepath"
	paths="/operations/${destinationDetailActionForm.id}/activateHost/">
	<auth:then>
		<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
	</auth:then>
</auth:if>

<style>
.host-pills { display:flex; flex-wrap:wrap; gap:0.25rem; margin-top:0.2rem; }
.host-pills:empty { display:none; }
.host-cell { line-height:1.25; }
.host-cell .host-line { display:flex; align-items:center; gap:0.35rem; }
.host-cell .host-dot { flex:0 0 1rem; width:1rem; text-align:center; font-size:0.7rem; }
.host-cell .host-dot.host-busy { animation:_hostBusyPulse 1.2s ease-in-out infinite; }
.host-cell .host-addr { margin-left:1.35rem; font-size:0.75rem; color:var(--bs-secondary-color); }
.host-cell .host-pills { margin-left:1.35rem; }
.host-cell .host-action-slot { display:inline-flex; align-items:center; justify-content:center; width:1.2rem; }
@keyframes _hostBusyPulse { 0%,100% { opacity:1; } 50% { opacity:0.35; } }
.host-pills .badge { font-weight:500; font-size:0.7rem; }
</style>

<div class="row g-3 mt-1">

	<%-- Dissemination Hosts column --%>
	<div class="col-12<c:if test="${destination.acquisition}"> col-md-6</c:if>">
		<p class="fw-bold mb-1 d-flex align-items-center gap-1">
			Dissemination Host(s)
			<button class="btn btn-link btn-sm p-0 ms-1 text-muted" type="button"
				data-bs-toggle="collapse" data-bs-target="#disseminationHostsInfo"
				aria-expanded="false" aria-controls="disseminationHostsInfo"
				title="About dissemination selection, transfers and limits">
				<i class="bi bi-info-circle"></i>
			</button>
		</p>
		<div class="collapse mb-2" id="disseminationHostsInfo">
			<div class="ecpds-help-panel">
				<p class="mb-2"><i class="bi bi-shuffle me-1"></i><strong>Failover host selection</strong> &mdash;
				Dissemination Hosts are considered in priority order, with lower numbers first.
				The <strong>selected</strong> pill identifies the Host currently selected for new dispatches.
				The scheduler can remain on a working fallback Host according to the Destination's failover settings;
				it does not necessarily return to the highest-priority Host for every transfer.</p>
				<p class="mb-2"><strong>Transfer activity</strong> &mdash; The <strong>N active</strong> pill shows this Destination's
				active outgoing transfers on that Host, not the combined count across Destinations or physical socket connections.
				The pulsing green dot indicates counted transfer activity; a steady green dot only means the Host is enabled,
				not that it is reachable or healthy. Transfers already running on a previously selected Host may still finish there.</p>
				<p class="mb-2"><strong>Connection limits</strong> &mdash; The Host maximum applies per Destination by default,
				or across Destinations using that Host record on the same Master when configured as shared.
				The Destination's Max Connections also caps outgoing transfers across its dissemination Hosts,
				independently of its scheduled acquisition retrievals. Other scheduler limits still apply.
				Use the limit icon beside the Host name for details; probes and idle connections are not counted.</p>
				<p class="mb-2"><strong>Retries and returning to primary</strong> &mdash; Each Host has its own retry budget and delay.
				The selected pill shows that Host's retry usage. Destination settings control further retry cycles,
				holding the Destination and returning from a fallback Host.
				See the <strong>Limits &amp; Failover Guide</strong> in the Destination edit page's Delivery or Limits card.</p>
				<p class="mb-0"><strong>On-the-fly fallback</strong> &mdash; If a missing source file is retrieved during delivery,
				the outgoing transfer still occupies its normal dissemination slots.
				The extra Mover-side source retrieval is not counted against acquisition limits or included in the acquisition retrieval pill.</p>
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
				<tr data-host-name="${disseminationHost.name.name}">
					<td class="host-cell">
						<div class="host-line">
							<c:if test="${disseminationHost.name.active}">
								<i class="host-dot bi bi-circle-fill text-success" title="Host is activated"></i>
								<a title="This Host is Activated (id=${disseminationHost.name.name})"
									href="/do/transfer/host/${disseminationHost.name.name}">${disseminationHost.name.nickName}</a>
							</c:if>
							<c:if test="${!disseminationHost.name.active}">
								<i class="host-dot bi bi-slash-circle-fill text-danger" title="Host is disabled"></i>
								<a title="This Host is NOT Activated (id=${disseminationHost.name.name})"
									href="/do/transfer/host/${disseminationHost.name.name}"
									style="text-decoration:line-through;color:var(--bs-secondary-color)">${disseminationHost.name.nickName}</a>
							</c:if>
							<c:set var="connectionLimitHost" value="${disseminationHost.name}" />
							<%@ include file="host_connection_limit.jsp" %>
						</div>
						<c:if test="${not empty disseminationHost.name.host}"><div class="host-addr">${disseminationHost.name.host}</div></c:if>
						<div class="host-pills host-live-status"></div>
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
				language:     { emptyTable: 'No matching records found.' }
			});
			<c:if test="${!destination.acquisition}">refreshDisseminationHostsStatus();</c:if>
		});

		function refreshDisseminationHostsStatus() {
			fetch('/do/transfer/destination/hostsStatus/<c:out value="${destination.name}"/>', { credentials: 'same-origin' })
				.then(function (resp) {
					var ct = resp.headers.get('content-type') || '';
					if (!resp.ok || ct.indexOf('application/json') === -1) {
						throw new Error('not JSON'); // e.g. session expired - silently skip this optional widget
					}
					return resp.json();
				})
				.then(function (statuses) {
					(statuses || []).forEach(function (status) {
						if (status.type === 'acquisition') {
							if (!document.getElementById('acquisitionHostsTable')) return;
							var table = $('#acquisitionHostsTable').DataTable();
							var acqRow = $(table.rows().nodes()).filter(function() {
								return this.getAttribute('data-acq-host-id') === String(status.hostName);
							}).find('.acq-retrieval-status');
							acqRow.empty();
							$(acqRow).closest('td').find('.host-dot.bi-circle-fill').toggleClass('host-busy', Number(status.activeConnections) > 0);
							if (!(Number(status.activeConnections) > 0)) return;
							var count = Number(status.activeConnections);
							var tooltip = 'This Destination: ' + count + ' active file retrieval(s) through this Host. ';
							if (status.sharedConnectionLimit) {
								tooltip += 'Across all Destinations: ' + status.totalConnections
									+ (status.maxConnections < 0 ? ' active (unlimited Host).' : ' / ' + status.maxConnections + ' Host slots in use.');
							} else {
								tooltip += 'The Host maximum applies separately per Destination.';
							}
							tooltip += ' Directory listing and file discovery are shown separately by the discovery indicator; Network Weather probes are separate.';
							acqRow.append($('<span class="badge rounded-pill text-bg-success"></span>')
								.attr('title', tooltip).text(count + ' retrieving'));
							return;
						}
						var row = document.querySelector('#disseminationHostsTable tr[data-host-name="' + status.hostName + '"] .host-live-status');
						if (!row) { return; }
						row.innerHTML = '';
						var dissDot = row.closest('td').querySelector('.host-dot.bi-circle-fill');
						if (dissDot) { dissDot.classList.toggle('host-busy', Number(status.activeConnections) > 0); }
						var connBadge = document.createElement('span');
						connBadge.className = 'badge rounded-pill text-bg-secondary';
						connBadge.title = 'Active uploads for this Destination on this Host, not the combined count across Destinations. The Host connection limit may be shared; see the limit information icon.';
						connBadge.textContent = status.activeConnections + ' active';
						if (Number(status.activeConnections) > 0) { row.appendChild(connBadge); }
						if (status.selected) {
							var selBadge = document.createElement('span');
							selBadge.className = 'badge rounded-pill text-bg-primary';
							selBadge.title = 'This is the host currently selected for new dispatches. It will fail over to the next host in priority order after '
								+ (status.retriesTotal - status.retriesUsed) + ' more failed attempt(s) ('
								+ status.retriesUsed + '/' + status.retriesTotal + ' retries used).';
							selBadge.textContent = 'selected \u2014 ' + status.retriesUsed + '/' + status.retriesTotal + ' retries';
							row.appendChild(selBadge);
						}
					});
				})
				.catch(function (error) {
					var disseminationRows = $($('#disseminationHostsTable').DataTable().rows().nodes());
					disseminationRows.find('.host-dot').removeClass('host-busy');
					disseminationRows.find('.host-live-status').empty()
						.append($('<span class="badge rounded-pill text-bg-warning"></span>')
							.attr('title', 'Live dissemination Host status is unavailable.').text('status unavailable'));
					var pills = document.getElementById('acquisitionHostsTable')
						? $($('#acquisitionHostsTable').DataTable().rows().nodes()).find('.acq-retrieval-status')
						: $('.acq-retrieval-status');
					pills.closest('td').find('.host-dot').removeClass('host-busy');
					pills.empty().append($('<span class="badge rounded-pill text-bg-warning"></span>')
						.attr('title', 'Live retrieval counts are unavailable.').text('retrievals unavailable'));
					console.warn('Could not refresh Destination Host status', error);
				});
		}
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
					title="About acquisition discovery and file retrieval">
					<i class="bi bi-info-circle"></i>
				</button>
			</p>
			<div class="collapse mb-2" id="acquisitionHostsInfo">
				<div class="ecpds-help-panel">
					<p class="mb-2"><i class="bi bi-diagram-3 me-1"></i><strong>Parallel host processing</strong> &mdash;
					Eligible acquisition Hosts are processed independently and can run in parallel, subject to each Host's acquisition timing and scheduler capacity.
					Unlike dissemination, there is no priority-based failover chain; the Priority column does not determine acquisition scheduling.</p>
					<p class="mb-2"><strong>Discovery</strong> lists remote directories and evaluates files against each Host's acquisition rules.
					The running indicator and stop button refer to this discovery cycle, not file downloads.
					Stopping discovery does not cancel file retrievals already scheduled or running. Network Weather probes are separate.</p>
					<p class="mb-2"><strong>File retrieval</strong> is scheduled separately for files selected for queueing.
					The <strong>N retrieving</strong> pill shows this Destination's active Master-scheduled retrieval jobs through that Host;
					the pulsing green dot indicates counted retrieval activity. These are jobs, not physical socket counts.
					The Host limit applies per Destination unless configured as shared on the same Master.
					The Destination's Max Connections caps retrievals across its acquisition Hosts independently of outgoing dissemination;
					the download scheduler's pool limit also applies. Use the limit icon beside the Host name for details.</p>
					<p class="mb-0"><strong>On-the-fly fallback</strong> &mdash; Mover-side recovery of a missing file bypasses these acquisition limits
					and is not included in the retrieval pill. Directory listings, probes and idle connections are not counted either.</p>
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
						<td class="host-cell">
							<div class="host-line">
								<c:if test="${acquisitionHost.name.active}">
									<i class="host-dot bi bi-circle-fill text-success" title="Host is activated"></i>
									<a title="This Host is Activated (id=${acquisitionHost.name.name})"
										href="/do/transfer/host/${acquisitionHost.name.name}">${acquisitionHost.name.nickName}</a>
									<span class="host-action-slot acq-state-indicator">
										<span class="acq-running-indicator" style="display:none;" title="Directory listing and file discovery are running, not the file retrievals counted below. This is not a Network Weather probe.">
											<c:choose>
												<c:when test="${not empty ecpdsCanHandleHosts}">
													<button class="acq-interrupt-btn btn btn-link btn-sm p-0 border-0 text-warning" style="line-height:1;font-size:0.95rem;animation:_acqRunPulse 1.2s ease-in-out infinite;" title="Directory listing and file discovery are running &mdash; click to interrupt this discovery cycle. This does not cancel file retrievals already scheduled or running." aria-label="Interrupt directory listing and file discovery" data-interrupt-host-id="${acquisitionHost.name.id}" onclick="_acqInterrupt(this)"><i class="bi bi-stop-fill"></i></button>
												</c:when>
												<c:otherwise>
													<span class="spinner-grow text-success" style="width:0.75rem;height:0.75rem;vertical-align:middle;" title="Directory listing and file discovery are running, not the file retrievals counted below. This is not a Network Weather probe."></span>
												</c:otherwise>
											</c:choose>
										</span>
										<c:if test="${not empty ecpdsCanHandleHosts}"><button class="acq-run-btn btn btn-link btn-sm p-0 border-0 text-success" style="display:none;line-height:1;font-size:0.95rem;" title="Request directory listing and file discovery now. Matching files are handled according to this Host's acquisition rules; retrievals are scheduled separately." aria-label="Request directory listing and file discovery now" data-run-host-id="${acquisitionHost.name.id}" onclick="_acqTrigger(this)"><i class="bi bi-play-fill"></i></button></c:if>
									</span>
								</c:if>
								<c:if test="${!acquisitionHost.name.active}">
									<i class="host-dot bi bi-slash-circle-fill text-danger" title="Host is disabled"></i>
									<a title="This Host is NOT Activated (id=${acquisitionHost.name.name})"
										href="/do/transfer/host/${acquisitionHost.name.name}"
										style="text-decoration:line-through;color:var(--bs-secondary-color)">${acquisitionHost.name.nickName}</a>
									<span class="host-action-slot"><i class="bi bi-play-fill" style="font-size:0.95rem;opacity:0.25;color:var(--bs-secondary-color);" title="Host is disabled"></i></span>
								</c:if>
								<c:set var="connectionLimitHost" value="${acquisitionHost.name}" />
								<%@ include file="host_connection_limit.jsp" %>
							</div>
							<c:if test="${not empty acquisitionHost.name.host}"><div class="host-addr">${acquisitionHost.name.host}</div></c:if>
							<div class="host-pills acq-retrieval-status"></div>
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
					language:     { emptyTable: 'No matching records found.' }
				});

				function _applyAcqRunning(hostId, running) {
					var row = $('#acquisitionHostsTable tr[data-acq-host-id="' + hostId + '"]');
					row.find('.acq-running-indicator').toggle(running);
					row.find('.acq-run-btn').toggle(!running);
				}
				function _pollAcqHosts() {
					refreshDisseminationHostsStatus();
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
