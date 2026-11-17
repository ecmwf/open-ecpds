<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/datafile/transferserver/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
		style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
		<i class="bi bi-server text-primary flex-shrink-0"></i>
		<span>Data Mover: <strong><c:out value="${transferserver.name}"/></strong></span>
		<auth:if basePathKey="transferserver.basepath" paths="/edit/insert_form">
		<auth:then>
		<div class="d-flex gap-1 ms-auto flex-shrink-0">
			<a href='<bean:message key="transferserver.basepath"/>'
			   class="btn btn-sm btn-outline-secondary" title="View list of data movers"><i class="bi bi-arrow-left"></i></a>
			<span class="vr align-self-center mx-1"></span>
			<a href='<bean:message key="transferserver.basepath"/>/edit/insert_form'
			   class="btn btn-sm btn-outline-success" title="Create new data mover"><i class="bi bi-plus-circle"></i></a>
			<c:if test="${not empty transferserver.id}">
			<a href='<bean:message key="transferserver.basepath"/>/edit/update_form/${transferserver.id}'
			   class="btn btn-sm btn-outline-primary" title="Edit this data mover"><i class="bi bi-pencil"></i></a>
			<a href='<bean:message key="transferserver.basepath"/>/edit/delete_form/${transferserver.id}'
			   class="btn btn-sm btn-outline-danger" title="Delete this data mover"><i class="bi bi-trash"></i></a>
			</c:if>
		</div>
		</auth:then>
		</auth:if>
	</div>

	<%-- Card: Identity --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-tag text-primary"></i>
			<span class="fw-semibold">Identity</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Name</div><div class="field-value"><span class="val-code"><c:out value="${transferserver.name}" /></span></div></div>
				<div class="field-row"><div class="field-label">Transfer Group</div><div class="field-value"><c:choose><c:when test="${not empty transferserver.transferGroupName}"><a href="/do/datafile/transfergroup/${transferserver.transferGroupName}">${transferserver.transferGroupName}</a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Last Update</div><div class="field-value"><c:choose><c:when test="${not empty transferserver.lastUpdateDate}"><content:content name="transferserver.lastUpdateDate" dateFormatKey="date.format.transfer" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
			</div>
		</div>
	</div>

	<%-- Card: Connection --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-ethernet text-primary"></i>
			<span class="fw-semibold">Connection</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Hostname</div><div class="field-value"><span class="val-code"><c:out value="${transferserver.host}" /></span></div></div>
				<c:if test="${not empty physicalLocation}">
					<div class="field-row"><div class="field-label">Physical location</div><div class="field-value" style="min-width:0;overflow-wrap:anywhere;">
						<c:choose>
							<c:when test="${not empty physicalLocation.value}"><c:out value="${physicalLocation.value}"/></c:when>
							<c:when test="${physicalLocation.status == 'NOT_CONFIGURED'}"><span class="text-muted">Not configured in SNMP</span></c:when>
							<c:otherwise><span class="text-muted">Location unavailable</span></c:otherwise>
						</c:choose>
						<c:if test="${physicalLocation.stale and not empty physicalLocation.value}"><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis ms-1">Stale</span></c:if>
						<div class="small text-muted">SNMP sysLocation &middot; <c:out value="${physicalLocation.statusLabel}"/>
							<c:if test="${not empty physicalLocation.lastSuccess}"><br>Last verified: <c:out value="${physicalLocation.lastSuccess}"/></c:if>
							<c:if test="${not empty physicalLocation.lastAttempt}"><br>Last attempt: <c:out value="${physicalLocation.lastAttempt}"/></c:if>
						</div>
					</div></div>
				</c:if>
				<div class="field-row"><div class="field-label">Port</div><div class="field-value"><span class="val-num"><c:out value="${transferserver.port}" /></span></div></div>
				<c:set var="hostForReplication" value="${transferserver.hostForReplication}" />
				<c:if test="${transferserver.replicate}">
					<div class="field-row"><div class="field-label">Host For Replication</div><div class="field-value"><c:choose><c:when test="${not empty hostForReplication}"><a href="/do/transfer/host/${hostForReplication.name}">${hostForReplication.nickName}</a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				</c:if>
			</div>
		</div>
	</div>

	<%-- Card: Options --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-toggles text-primary"></i>
			<span class="fw-semibold">Options</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Check <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="When enabled, this Data Mover is eligible to perform host connectivity checks - periodic probes used to verify whether destination hosts are reachable." tabindex="0"></i></div><div class="field-value"><c:choose><c:when test="${transferserver.check}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${transferserver.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Replicate</div><div class="field-value"><c:choose><c:when test="${transferserver.replicate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			</div>
		</div>
	</div>

	<%-- Card: Availability --%>
	<div class="card border-0 shadow-sm mb-3" id="moverAvailCard">
		<div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-activity text-primary"></i>
			<span class="fw-semibold">Availability</span>
			<span id="moverUptimeBadge" class="ms-1"></span>
			<div class="ms-auto d-flex align-items-center gap-2">
				<span class="text-muted" style="font-size:0.82rem;">Period:</span>
				<div class="btn-group btn-group-sm" id="moverAvailPeriodSelector">
					<button class="btn btn-outline-secondary" data-hours="24">24h</button>
					<button class="btn btn-outline-secondary" data-hours="72">3d</button>
					<button class="btn btn-outline-secondary active" data-hours="168">7d</button>
					<button class="btn btn-outline-secondary" data-hours="720">30d</button>
					<button class="btn btn-outline-secondary" data-hours="2160">90d</button>
					<button class="btn btn-outline-secondary" data-hours="4320">180d</button>
					<button class="btn btn-outline-secondary" data-hours="8760">1y</button>
				</div>
			</div>
		</div>
		<div class="card-body pb-2 pt-2">
			<div id="moverAvailStats" class="d-flex flex-wrap gap-3 mb-2" style="font-size:0.82rem"></div>
			<div id="moverAvailWrap">
				<div class="text-muted small fst-italic">Loading&hellip;</div>
			</div>
		</div>
	</div>

	<%-- Card: Disk Usage --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-hdd-fill text-primary"></i>
			<span class="fw-semibold">Disk Usage</span>
			<span id="moverDiskUsageAge" class="text-muted fw-normal small ms-1"></span>
			<button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
				data-bs-toggle="collapse" data-bs-target="#moverDiskUsageLegend"
				aria-expanded="false" aria-controls="moverDiskUsageLegend" aria-label="About storage volume disabling"
				title="About storage volume disabling">
				<i class="bi bi-info-circle" aria-hidden="true"></i>
			</button>
		</div>
		<div class="collapse" id="moverDiskUsageLegend">
			<div class="ecpds-help-panel">
				<strong class="d-block mb-1">Disabling a storage volume</strong>
				<p class="mb-1">Disabling a volume stops new routing to that volume on this Data Mover; active transfers are allowed to finish. Replication is paused for that volume index.</p>
				<p class="mb-0">This does not disable the entire Data Mover. Use <strong>Re-enable</strong> to make the volume available again.</p>
			</div>
		</div>
		<div class="card-body pb-2 pt-2">
			<div id="moverDiskUsageSummary" class="d-flex flex-wrap gap-3 mb-2" style="font-size:0.82rem"></div>
			<div class="text-muted small mb-2">Select a volume to view its device, paths and controls.</div>
			<div id="moverDiskUsageWrap">
				<div class="text-muted small fst-italic">Loading&hellip;</div>
			</div>
		</div>
	</div>

	<script src="/assets/js/chart.umd.min.js"></script>
	<script>
	(function() {
		var moverName  = '${transferserver.name}';
		var fetchedAt  = null;
		var currentVols = [];
		var storageVolumeCsrf = null;
		var volumeRows = new Map();
		var volumeUpdates = new Set();
		var volumeRevision = 0;

		function htmlText(value) {
			var el = document.createElement('span');
			el.textContent = value == null || value === '' ? 'Unavailable' : String(value);
			return el.innerHTML;
		}

		function setText(el, value) {
			if (el.textContent !== value) el.textContent = value;
		}

		function renderStorageVolumes() {
			var wrap = document.getElementById('moverDiskUsageWrap');
			var volumes = new Map();
			currentVols.forEach(function(v) { volumes.set(String(v.volume), { usage: v }); });
			(currentVols.storageInfo || []).forEach(function(v) {
				var key = String(v.volume);
				if (!volumes.has(key)) volumes.set(key, {});
				volumes.get(key).info = v;
			});
			if (!volumes.size) {
				volumeRows.clear();
				wrap.innerHTML = '<div class="text-muted small fst-italic py-1">No disk usage snapshot available. Check that the Data Mover is connected and enabled, its transfer group is active, and background polling is running.</div>';
				return;
			}
			if (!volumeRows.size) {
				wrap.innerHTML = '<div class="table-responsive"><table class="table table-sm align-middle mb-0">'
					+ '<thead><tr><th>Volume</th><th style="min-width:140px">Usage</th><th>Used</th><th>Free</th><th>Total</th><th>Status</th></tr></thead><tbody></tbody></table></div>';
			}
			var tbody = wrap.querySelector('tbody');
			volumeRows.forEach(function(rows, key) {
				if (!volumes.has(key)) {
					rows.main.remove();
					rows.detail.remove();
					volumeRows.delete(key);
				}
			});
			volumes.forEach(function(v, key) {
				var rows = volumeRows.get(key);
				if (!rows) {
					var main = document.createElement('tr');
					main.innerHTML = '<td><button type="button" class="btn btn-link btn-sm p-0 text-nowrap" data-volume-toggle aria-expanded="false">'
						+ '<i class="bi bi-chevron-right me-1" aria-hidden="true"></i>Vol ' + htmlText(key) + '</button></td>'
						+ '<td><div class="progress" style="height:18px"><div class="progress-bar" role="progressbar" aria-label="Disk space used" aria-valuemin="0" aria-valuemax="100"></div></div></td>'
						+ '<td data-used></td><td data-free></td><td data-total></td><td><span data-status></span></td>';
					var detail = document.createElement('tr');
					detail.id = 'moverVolumeDetails-' + key;
					detail.hidden = true;
					detail.innerHTML = '<td colspan="6"><div class="card bg-body-tertiary border-0 my-1"><div class="card-body py-2">'
						+ '<div class="field-grid">'
						+ '<div class="field-row"><div class="field-label">Device</div><div class="field-value font-monospace text-break" data-device></div></div>'
						+ '<div class="field-row"><div class="field-label">Mount point</div><div class="field-value font-monospace text-break" data-mount></div></div>'
						+ '<div class="field-row"><div class="field-label">Storage path</div><div class="field-value font-monospace text-break" data-path></div></div>'
						+ '</div><div class="small text-muted my-2" data-info-error></div>'
						+ '<button type="button" class="btn btn-sm" data-volume-action hidden></button>'
						+ '</div></div></td>';
					var toggle = main.querySelector('button');
					toggle.setAttribute('aria-controls', detail.id);
					toggle.addEventListener('click', function() {
						detail.hidden = !detail.hidden;
						toggle.setAttribute('aria-expanded', String(!detail.hidden));
						toggle.querySelector('i').className = 'bi me-1 bi-chevron-' + (detail.hidden ? 'right' : 'down');
					});
					detail.querySelector('button').setAttribute('data-volume', key);
					tbody.appendChild(main);
					tbody.appendChild(detail);
					rows = { main: main, detail: detail };
					volumeRows.set(key, rows);
				}
				var usage = v.usage, info = v.info;
				var bar = rows.main.querySelector('.progress-bar');
				bar.style.width = (usage ? usage.pct : 0) + '%';
				bar.style.backgroundColor = pctColor(usage ? usage.pct : 0);
				bar.setAttribute('aria-valuenow', usage ? usage.pct : 0);
				setText(bar, usage ? usage.pct + '%' : '');
				setText(rows.main.querySelector('[data-used]'), usage ? fmtBytes(usage.used) : '?');
				setText(rows.main.querySelector('[data-free]'), usage ? fmtBytes(usage.free) : '?');
				setText(rows.main.querySelector('[data-total]'), usage ? fmtBytes(usage.total) : '?');
				var disabled = info && info.disabled === true;
				var status = rows.main.querySelector('[data-status]');
				status.className = 'badge rounded-pill border fw-normal ' + (!info
					? 'bg-secondary-subtle text-secondary-emphasis'
					: disabled ? 'bg-warning-subtle text-warning-emphasis' : 'bg-success-subtle text-success-emphasis');
				setText(status, info ? (disabled ? 'Disabled' : 'Enabled') : 'Unavailable');
				setText(rows.detail.querySelector('[data-device]'), info && info.device || 'Unavailable');
				setText(rows.detail.querySelector('[data-mount]'), info && info.mountPoint || 'Unavailable');
				setText(rows.detail.querySelector('[data-path]'), info && info.volumePath || 'Unavailable');
				setText(rows.detail.querySelector('[data-info-error]'), currentVols.storageInfoError
					|| (!info ? 'Filesystem device and mount details are unavailable.' : ''));
				var button = rows.detail.querySelector('button');
				button.hidden = !storageVolumeCsrf || !info;
				button.disabled = volumeUpdates.has(key);
				button.className = 'btn btn-sm ' + (disabled ? 'btn-outline-success' : 'btn-outline-danger');
				button.setAttribute('data-disabled', String(!disabled));
				setText(button, disabled ? 'Re-enable' : 'Disable');
			});
		}

		$.getJSON('/do/datafile/movervolume').done(function(data) {
			storageVolumeCsrf = data.csrf || null;
			renderStorageVolumes();
		});

		$('#moverDiskUsageWrap').on('click', 'button[data-volume-action]', function() {
			var button = $(this);
			var volume = button.attr('data-volume');
			if (volumeUpdates.has(volume)) return;
			volumeUpdates.add(volume);
			volumeRevision++;
			button.prop('disabled', true);
			$.ajax({
				url: '/do/datafile/movervolume',
				method: 'POST',
				dataType: 'json',
				data: {
					csrf: storageVolumeCsrf,
					mover: moverName,
					volume: volume,
					disabled: button.attr('data-disabled')
				}
			}).fail(function(xhr) {
				var message = xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error : 'Storage volume update failed.';
				ecpdsAlert('Storage volume update failed', message);
			}).always(function() {
				volumeUpdates.delete(volume);
				volumeRevision++;
				button.prop('disabled', false);
				refresh();
			});
		});

		function fmtBytes(b) {
			if (b == null || isNaN(b)) return '?';
			var units = ['B','KB','MB','GB','TB','PB'];
			var i = 0;
			while (b >= 1024 && i < units.length - 1) { b /= 1024; i++; }
			return b.toFixed(i > 0 ? 1 : 0) + ' ' + units[i];
		}

		function pctColor(pct) {
			if (pct >= 90) return '#dc3545';
			if (pct >= 75) return '#fd7e14';
			if (pct >= 50) return '#ffc107';
			return '#0d6efd';
		}

		function renderDiskUsage(vols) {
			currentVols = vols || [];
			var summary = document.getElementById('moverDiskUsageSummary');
			renderStorageVolumes();
			if (!vols || vols.length === 0) {
				if (summary) summary.innerHTML = '';
				return;
			}

			/* Build summary badges */
			if (summary) {
				var totalUsed  = vols.reduce(function(s, v) { return s + v.used;  }, 0);
				var totalCap   = vols.reduce(function(s, v) { return s + v.total; }, 0);
				var overallPct = totalCap > 0 ? Math.round(100 * totalUsed / totalCap) : 0;
				var pctCls = overallPct >= 90 ? 'text-danger-emphasis bg-danger-subtle border-danger-subtle'
				           : overallPct >= 75 ? 'text-warning-emphasis bg-warning-subtle border-warning-subtle'
				           :                    'text-success-emphasis bg-success-subtle border-success-subtle';
				var pctIcon = overallPct >= 90 ? 'bi-exclamation-triangle-fill'
				            : overallPct >= 75 ? 'bi-exclamation-circle-fill'
				            :                   'bi-check-circle-fill';
				var maxVol = vols.reduce(function(a, b) { return b.pct > a.pct ? b : a; }, vols[0]);
				var minVol = vols.reduce(function(a, b) { return b.pct < a.pct ? b : a; }, vols[0]);
				var html =
				    '<span class="badge rounded-pill border ' + pctCls + ' px-2 py-1">'
				    +   '<i class="bi ' + pctIcon + ' me-1"></i>'
				    +   overallPct + '% used &mdash; ' + fmtBytes(totalUsed) + ' / ' + fmtBytes(totalCap)
				    + '</span>'
				    + '<span class="badge rounded-pill border text-secondary-emphasis bg-secondary-subtle border-secondary-subtle px-2 py-1">'
				    +   '<i class="bi bi-hdd me-1"></i>' + vols.length + ' volume' + (vols.length !== 1 ? 's' : '')
				    + '</span>';
				if (vols.length > 1) {
					html +=
					    '<span class="badge rounded-pill border text-secondary-emphasis bg-secondary-subtle border-secondary-subtle px-2 py-1">'
					    +   '<i class="bi bi-arrow-up me-1"></i>Highest: Vol ' + maxVol.volume + ' (' + maxVol.pct + '%)'
					    + '</span>'
					    + '<span class="badge rounded-pill border text-secondary-emphasis bg-secondary-subtle border-secondary-subtle px-2 py-1">'
					    +   '<i class="bi bi-arrow-down me-1"></i>Lowest: Vol ' + minVol.volume + ' (' + minVol.pct + '%)'
					    + '</span>';
				}
				summary.innerHTML = html;
			}
		}

		function updateAge() {
			var el = document.getElementById('moverDiskUsageAge');
			if (!el || !fetchedAt || _tsRefreshFailed) return;
			var sec = Math.round((Date.now() - fetchedAt) / 1000);
			el.textContent = '(sampled ' + sec + 's ago' + (sec > 120 ? ' \u2014 stale' : '') + ')';
		}

		var _tsBusy = false, _tsRefreshRequested = false, _tsRefreshFailed = false, _tsRefIv, _tsAgeIv;
		function refresh() {
			if (_tsBusy) { _tsRefreshRequested = true; return; }
			_tsBusy = true;
			var revision = volumeRevision;
			$.ajax({
				url: '/do/datafile/moverdiskusage/' + encodeURIComponent(moverName),
				dataType: 'json',
				timeout: 12000
			})
				.done(function(data) {
					if (revision !== volumeRevision || volumeUpdates.size) return;
					_tsRefreshFailed = false;
					var sampled = data.updated && data.updated[moverName];
					fetchedAt = sampled ? Date.now() - Math.max(0, data.now - sampled) : null;
					if (!sampled) document.getElementById('moverDiskUsageAge').textContent = '';
					var vols = (data.movers && data.movers[moverName]) ? data.movers[moverName] : [];
					vols.storageInfo = (data.details && data.details[moverName]) ? data.details[moverName] : [];
					vols.storageInfoError = data.storageInfoError || null;
					renderDiskUsage(vols);
					updateAge();
				})
				.fail(function(xhr) {
					_tsRefreshFailed = true;
					var expired = xhr.status === 401 || xhr.status === 403;
					if (expired) {
						_tsRefreshRequested = false;
						clearInterval(_tsRefIv);
						clearInterval(_tsAgeIv);
					}
					var el = document.getElementById('moverDiskUsageAge');
					if (el) el.textContent = expired ? 'Session expired or access denied; reload to sign in.'
						: 'Refresh unavailable; retrying. Any displayed snapshot may be stale.';
					if (!volumeRows.size) {
						document.getElementById('moverDiskUsageWrap').textContent =
							'Disk usage could not be loaded.' + (expired ? '' : ' Retrying automatically.');
					}
				})
				.always(function() {
					_tsBusy = false;
					if (_tsRefreshRequested) { _tsRefreshRequested = false; refresh(); }
				});
		}

		refresh();
		_tsRefIv = setInterval(refresh, 5000);
		_tsAgeIv = setInterval(updateAge, 1000);
	})();
	</script>

	<script>
	(function() {
		var moverName   = '${transferserver.name}';
		var availChart  = null;
		var _obsThemeAv = null;
		var _avFails    = 0;
		var _avRefIv    = null;
		var _lastData   = null;

		function getThemeColors() {
			var s = getComputedStyle(document.documentElement);
			return {
				bodyColor:   (s.getPropertyValue('--bs-body-color')      || '').trim() || '#212529',
				borderColor: (s.getPropertyValue('--bs-border-color')    || '').trim() || '#dee2e6',
				mutedColor:  (s.getPropertyValue('--bs-secondary-color') || '').trim() || '#6c757d'
			};
		}

		function fmtMinute(epochMs) {
			var d = new Date(epochMs);
			var mo = (d.getMonth() + 1).toString().padStart(2, '0');
			var dy = d.getDate().toString().padStart(2, '0');
			var hh = d.getHours().toString().padStart(2, '0');
			var mm = d.getMinutes().toString().padStart(2, '0');
			return mo + '/' + dy + ' ' + hh + ':' + mm;
		}

		function buildAvailChart(data) {
			_lastData = data;
			var wrap = document.getElementById('moverAvailWrap');
			if (!data || data.length === 0) {
				wrap.style.height = '';
				wrap.innerHTML = '<div class="text-muted small fst-italic py-1">No availability data yet \u2014 waiting for first polling cycle.</div>';
				return;
			}
			var theme   = getThemeColors();
			/* Use formatted string labels — avoids the need for a Chart.js date adapter. */
			var labels  = data.map(function(d) { return fmtMinute(d[0]); });
			var upData  = data.map(function(d) { return d[1] === 1 ? 1 : 0; });
			var dnData  = data.map(function(d) { return d[1] === 0 ? 1 : 0; });

			/* Chart.js with maintainAspectRatio:false reads the container's explicit height.
			   Set it on the wrapping div, not on the canvas element. */
			wrap.style.height = '80px';
			wrap.innerHTML = '<canvas id="moverAvailChart" style="display:block"></canvas>';
			var ctx = document.getElementById('moverAvailChart').getContext('2d');
			if (availChart) { availChart.destroy(); availChart = null; }
			availChart = new Chart(ctx, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{ label: 'Up',   data: upData, backgroundColor: '#198754', stack: 's', borderWidth: 0 },
						{ label: 'Down', data: dnData, backgroundColor: '#dc3545', stack: 's', borderWidth: 0 }
					]
				},
				options: {
					animation: false,
					maintainAspectRatio: false,
					layout: { padding: 0 },
					scales: {
						x: {
							stacked: true,
							ticks: { color: theme.mutedColor, maxRotation: 45, font: { size: 9 },
								callback: function(val, idx) {
									/* Show a tick every N labels so they don't overlap */
									var step = Math.max(1, Math.floor(data.length / 12));
									return idx % step === 0 ? this.getLabelForValue(val) : '';
								}
							},
							grid: { color: theme.borderColor }
						},
						y: {
							stacked: true,
							display: false,
							max: 1,
							min: 0
						}
					},
					plugins: {
						legend: { display: false },
						tooltip: {
							callbacks: {
								title: function(items) { return items[0] ? items[0].label : ''; },
								label: function(ctx) { return ctx.dataset.label; }
							}
						}
					},
					barPercentage: 1.0,
					categoryPercentage: 1.0
				}
			});
		}

		function updateUptimeBadge(uptime, data) {
			/* Header badge */
			var badge = document.getElementById('moverUptimeBadge');
			if (badge) {
				if (uptime < 0) {
					badge.innerHTML = '';
				} else {
					var badgeCls = uptime >= 99 ? 'bg-success-subtle text-success-emphasis border-success-subtle'
					             : uptime >= 95 ? 'bg-warning-subtle text-warning-emphasis border-warning-subtle'
					             :                'bg-danger-subtle text-danger-emphasis border-danger-subtle';
					var icon = uptime >= 99 ? 'bi-check-circle-fill' : uptime >= 95 ? 'bi-exclamation-circle-fill' : 'bi-x-circle-fill';
					badge.innerHTML = '<span class="badge rounded-pill border fw-normal ' + badgeCls + '">'
					    + '<i class="bi ' + icon + ' me-1"></i>' + uptime.toFixed(1) + '% uptime</span>';
				}
			}
			/* Stats row */
			var stats = document.getElementById('moverAvailStats');
			if (!stats) return;
			if (!data || data.length === 0) { stats.innerHTML = ''; return; }
			var upMin   = data.filter(function(d) { return d[1] === 1; }).length;
			var dnMin   = data.filter(function(d) { return d[1] === 0; }).length;
			var total   = data.length;
			/* Longest contiguous outage (in minutes) */
			var maxOut  = 0, cur = 0;
			data.forEach(function(d) { cur = d[1] === 0 ? cur + 1 : 0; if (cur > maxOut) maxOut = cur; });
			/* Last seen up */
			var lastUp  = null;
			for (var i = data.length - 1; i >= 0; i--) { if (data[i][1] === 1) { lastUp = data[i][0]; break; } }
			function fmtDur(m) {
				if (m < 60) return m + ' min';
				if (m < 1440) return Math.round(m / 60) + ' h';
				return Math.round(m / 1440) + ' d';
			}
			var uptimeCls = uptime >= 99 ? 'text-success-emphasis bg-success-subtle border-success-subtle'
			              : uptime >= 95 ? 'text-warning-emphasis bg-warning-subtle border-warning-subtle'
			              :               'text-danger-emphasis bg-danger-subtle border-danger-subtle';
			stats.innerHTML =
			    '<span class="badge rounded-pill border ' + uptimeCls + ' px-2 py-1">'
			    +   '<i class="bi bi-arrow-up-circle-fill me-1"></i>Up: ' + fmtDur(upMin) + ' (' + upMin + ' min)'
			    + '</span>'
			    + '<span class="badge rounded-pill border text-danger-emphasis bg-danger-subtle border-danger-subtle px-2 py-1">'
			    +   '<i class="bi bi-arrow-down-circle-fill me-1"></i>Down: ' + fmtDur(dnMin) + ' (' + dnMin + ' min)'
			    + '</span>'
			    + (maxOut > 0
			        ? '<span class="badge rounded-pill border text-secondary-emphasis bg-secondary-subtle border-secondary-subtle px-2 py-1">'
			          +   '<i class="bi bi-hourglass-split me-1"></i>Longest outage: ' + fmtDur(maxOut)
			          + '</span>'
			        : '')
			    + (lastUp !== null
			        ? '<span class="badge rounded-pill border text-secondary-emphasis bg-secondary-subtle border-secondary-subtle px-2 py-1">'
			          +   '<i class="bi bi-clock me-1"></i>Last seen up: ' + fmtMinute(lastUp)
			          + '</span>'
			        : '');
		}

		function loadAvail(hours) {
			$.getJSON('/do/datafile/moveravailability/' + encodeURIComponent(moverName) + '?hours=' + hours)
				.done(function(json) {
					_avFails = 0;
					var data = json.data || [];
					buildAvailChart(data);
					updateUptimeBadge(json.uptime != null ? json.uptime : -1, data);
				})
				.fail(function() {
					if (++_avFails >= 3 && _avRefIv) {
						clearInterval(_avRefIv);
						_avRefIv = null;
					}
				});
		}

		var AVAIL_HOURS_KEY = 'moverAvailHours';
		var _currentHours = 168;

		/* Wire period buttons */
		var periodBtns = document.querySelectorAll('#moverAvailPeriodSelector button');

		function setActivePeriod(hours) {
			_currentHours = hours;
			periodBtns.forEach(function(b) {
				b.classList.toggle('active', parseInt(b.getAttribute('data-hours'), 10) === hours);
			});
		}

		/* Restore persisted selection */
		(function() {
			var saved = parseInt(localStorage.getItem(AVAIL_HOURS_KEY), 10);
			if (saved && Array.from(periodBtns).some(function(b) { return parseInt(b.getAttribute('data-hours'), 10) === saved; })) {
				setActivePeriod(saved);
			}
		})();

		periodBtns.forEach(function(btn) {
			btn.addEventListener('click', function() {
				var hours = parseInt(this.getAttribute('data-hours'), 10);
				setActivePeriod(hours);
				localStorage.setItem(AVAIL_HOURS_KEY, hours);
				_lastData = null;
				document.getElementById('moverAvailWrap').innerHTML = '<div class="text-muted small fst-italic">Loading&hellip;</div>';
				document.getElementById('moverAvailStats').innerHTML = '';
				loadAvail(hours);
			});
		});

		function currentHours() { return _currentHours; }

		loadAvail(currentHours());
		_avRefIv = setInterval(function() { loadAvail(currentHours()); }, 60000);

		new MutationObserver(function() {
			var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
			if (t === _obsThemeAv) return;
			_obsThemeAv = t;
			if (availChart) { availChart.destroy(); availChart = null; }
			if (_lastData && _lastData.length > 0) buildAvailChart(_lastData);
		}).observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });
	})();
	</script>

</c:if>
