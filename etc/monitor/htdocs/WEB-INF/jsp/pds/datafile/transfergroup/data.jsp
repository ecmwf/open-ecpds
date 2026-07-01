<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/datafile/transfergroup/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
		style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
		<i class="bi bi-collection text-primary flex-shrink-0"></i>
		<span>Transfer Group: <strong><c:out value="${transfergroup.name}"/></strong></span>
		<auth:if basePathKey="transfergroup.basepath" paths="/edit/insert_form">
		<auth:then>
		<div class="d-flex gap-1 ms-auto flex-shrink-0">
			<a href='<bean:message key="transfergroup.basepath"/>'
			   class="btn btn-sm btn-outline-secondary" title="View list of transfer groups"><i class="bi bi-arrow-left"></i></a>
			<span class="vr align-self-center mx-1"></span>
			<a href='<bean:message key="transfergroup.basepath"/>/edit/insert_form'
			   class="btn btn-sm btn-outline-success" title="Create new transfer group"><i class="bi bi-plus-circle"></i></a>
			<c:if test="${not empty transfergroup.id}">
			<a href='<bean:message key="transfergroup.basepath"/>/edit/update_form/${transfergroup.id}'
			   class="btn btn-sm btn-outline-primary" title="Edit this transfer group"><i class="bi bi-pencil"></i></a>
			<a href='<bean:message key="transfergroup.basepath"/>/edit/delete_form/${transfergroup.id}'
			   class="btn btn-sm btn-outline-danger" title="Delete this transfer group"><i class="bi bi-trash"></i></a>
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
				<div class="field-row"><div class="field-label">Name</div><div class="field-value"><span class="val-code"><c:out value="${transfergroup.name}" /></span></div></div>
				<div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty transfergroup.comment}"><c:out value="${transfergroup.comment}" /></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Enabled</div><div class="field-value"><c:choose><c:when test="${transfergroup.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
			</div>
		</div>
	</div>

	<%-- Card: Replication --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-copy text-primary"></i>
			<span class="fw-semibold">Replication</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Replicate</div><div class="field-value"><c:choose><c:when test="${transfergroup.replicate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Min. Replication Count</div><div class="field-value"><span class="val-num">${transfergroup.minReplicationCount}</span></div></div>
				<div class="field-row"><div class="field-label">Volume Count</div><div class="field-value"><span class="val-num">${transfergroup.volumeCount}</span></div></div>
			</div>
		</div>
	</div>

	<%-- Card: Filtering --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-funnel text-primary"></i>
			<span class="fw-semibold">Filtering</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Filter</div><div class="field-value"><c:choose><c:when test="${transfergroup.filter}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<div class="field-row"><div class="field-label">Min. Filtering Count</div><div class="field-value"><span class="val-num">${transfergroup.minFilteringCount}</span></div></div>
			</div>
		</div>
	</div>

	<%-- Card: Backup --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-cloud-arrow-up text-primary"></i>
			<span class="fw-semibold">Backup</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Backup</div><div class="field-value"><c:choose><c:when test="${transfergroup.backup}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise></c:choose></div></div>
				<c:set var="hostForBackup" value="${transfergroup.hostForBackup}" />
				<c:if test="${transfergroup.backup}">
					<div class="field-row"><div class="field-label">Host For Backup</div><div class="field-value"><c:choose><c:when test="${not empty hostForBackup}"><a href="/do/transfer/host/${hostForBackup.name}">${hostForBackup.nickName}</a></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
				</c:if>
			</div>
		</div>
	</div>

	<%-- Card: Cluster (only when cluster name is set) --%>
	<c:set var="clusterName" value="${transfergroup.clusterName}" />
	<c:if test="${not empty clusterName}">
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-diagram-3 text-primary"></i>
			<span class="fw-semibold">Cluster</span>
		</div>
		<div class="card-body py-0">
			<div class="field-grid">
				<div class="field-row"><div class="field-label">Cluster Name</div><div class="field-value"><span class="val-code"><c:out value="${clusterName}" /></span></div></div>
				<div class="field-row"><div class="field-label">Cluster Weight</div><div class="field-value"><span class="val-num"><c:out value="${transfergroup.clusterWeight}" /></span></div></div>
			</div>
		</div>
	</div>
	</c:if>

	<%-- Card: Data Movers --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-server text-primary"></i>
			<span class="fw-semibold">Data Movers</span>
		</div>
		<div class="card-body p-0">
		<div class="table-responsive">
	<table id="tgServersTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
		<thead class="table-primary">
			<tr>
				<th>Name</th>
				<th>Host</th>
				<th>Port</th>
				<th class="text-center">Active</th>
				<th class="text-center">Replicate</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="server" items="${transfergroup.transferServers}">
			<tr>
				<td><a href="/do/datafile/transferserver/${server.name}">${server.name}</a></td>
				<td>${server.host}</td>
				<td>${server.port}</td>
				<td class="text-center" data-order="${server.active ? 1 : 0}">
					<c:choose>
						<c:when test="${server.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
						<c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
					</c:choose>
				</td>
				<td class="text-center" data-order="${server.replicate ? 1 : 0}">
					<c:choose>
						<c:when test="${server.replicate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
						<c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
		</div>
		</div>
	</div>
	<script>
	$(document).ready(function() {
		$('#tgServersTable').DataTable({
			paging:    false,
			searching: false,
			ordering:  true,
			info:      false,
			dom:       't',
			order:     [[0, 'asc']]
		});
	});
	</script>

	<%-- Card: Disk Usage --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-hdd-fill text-primary"></i>
			<span class="fw-semibold">Disk Usage</span>
			<span id="tgDiskUsageAge" class="text-muted fw-normal small ms-1"></span>
			<button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
				data-bs-toggle="collapse" data-bs-target="#tgDiskUsageLegend"
				aria-expanded="false" title="About this chart">
				<i class="bi bi-info-circle"></i>
			</button>
			<div class="ms-auto">
				<div class="btn-group btn-group-sm" role="group" aria-label="Chart view">
					<button type="button" class="btn btn-outline-secondary active" id="tgViewWorstBtn" onclick="tgSetView('worst')">Worst-case</button>
					<button type="button" class="btn btn-outline-secondary" id="tgViewAvgBtn" onclick="tgSetView('average')">Average</button>
				</div>
			</div>
		</div>
		<div class="collapse" id="tgDiskUsageLegend">
			<div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
				<strong class="d-block mb-1">Two views are available:</strong>
				<ul class="mb-0 ps-3">
					<li><strong>Worst-case</strong> &mdash; per volume, shows the highest used bytes across all Data Movers compared to the smallest reported capacity. This is the binding constraint used by the load balancer to select volumes.</li>
					<li><strong>Average</strong> &mdash; per volume, shows the mean used bytes and mean capacity across all Data Movers. This gives an overall utilisation picture when movers are heterogeneous.</li>
				</ul>
				<div class="mt-1 text-muted">When only one Data Mover is in the group, both views are identical.</div>
			</div>
		</div>
		<div class="card-body pb-2 pt-2">
			<div id="tgDiskUsageSummary" class="d-flex flex-wrap gap-3 mb-2" style="font-size:0.82rem"></div>
			<div id="tgDiskUsageWrap">
				<div class="text-muted small fst-italic">Loading&hellip;</div>
			</div>
		</div>
	</div>

	<script src="/assets/js/chart.umd.min.js"></script>
	<script>
	(function() {
		var groupName = '${transfergroup.name}';
		var groupMovers = [<c:forEach var="server" items="${transfergroup.transferServers}" varStatus="loop">'<c:out value="${server.name}"/>'<c:if test="${!loop.last}">,</c:if></c:forEach>];

		var chartInst = null;
		var fetchedAt = null;
		var lastWorstVols = [];
		var lastAvgVols = [];
		var currentVols = [];
		var currentView = 'worst';
		var _obsTheme   = null;

		function fmtBytes(b) {
			if (b == null || isNaN(b)) return '?';
			var units = ['B','KB','MB','GB','TB','PB'];
			var i = 0;
			while (b >= 1024 && i < units.length - 1) { b /= 1024; i++; }
			return b.toFixed(i > 0 ? 1 : 0) + ' ' + units[i];
		}

		function pctBadgeClass(pct) {
			if (pct >= 90) return 'bg-danger-subtle text-danger-emphasis border-danger-subtle';
			if (pct >= 75) return 'bg-warning-subtle text-warning-emphasis border-warning-subtle';
			return 'bg-success-subtle text-success-emphasis border-success-subtle';
		}

		function renderSummary(vols) {
			var el = document.getElementById('tgDiskUsageSummary');
			if (!el) return;
			if (!vols || vols.length === 0) { el.innerHTML = ''; return; }
			var totalUsed = vols.reduce(function(s, v) { return s + v.used; }, 0);
			var totalAll  = vols.reduce(function(s, v) { return s + v.total; }, 0);
			var overallPct = totalAll > 0 ? Math.round(100 * totalUsed / totalAll) : 0;
			var pctVals = vols.map(function(v) { return v.pct; });
			var maxPct  = Math.max.apply(null, pctVals);
			var minPct  = Math.min.apply(null, pctVals);
			var maxVol  = pctVals.indexOf(maxPct);
			var minVol  = pctVals.indexOf(minPct);

			function badge(icon, label, val, cls) {
				return '<span class="badge rounded-pill border fw-normal px-2 py-1 ' + cls + '">'
					+ '<i class="bi ' + icon + ' me-1"></i>' + label + ': <strong>' + val + '</strong></span>';
			}

			var html = badge('bi-pie-chart-fill', 'Overall', overallPct + '%', pctBadgeClass(overallPct))
				+ badge('bi-layers-fill', 'Volumes', vols.length, 'bg-secondary-subtle text-secondary-emphasis border-secondary-subtle')
				+ (vols.length > 1
					? badge('bi-arrow-up-circle-fill', 'Highest', 'Vol&nbsp;' + maxVol + '&nbsp;(' + maxPct + '%)', pctBadgeClass(maxPct))
					  + badge('bi-arrow-down-circle-fill', 'Lowest', 'Vol&nbsp;' + minVol + '&nbsp;(' + minPct + '%)', pctBadgeClass(minPct))
					: '');
			el.innerHTML = html;
		}

		function pctColor(pct) {
			if (pct >= 90) return '#dc3545';
			if (pct >= 75) return '#fd7e14';
			if (pct >= 50) return '#ffc107';
			return '#0d6efd';
		}

		function getThemeColors() {
			var s = getComputedStyle(document.documentElement);
			return {
				bodyColor:   (s.getPropertyValue('--bs-body-color')      || '').trim() || '#212529',
				borderColor: (s.getPropertyValue('--bs-border-color')    || '').trim() || '#dee2e6',
				secondaryBg: (s.getPropertyValue('--bs-secondary-bg')    || '').trim() || '#e9ecef',
				mutedColor:  (s.getPropertyValue('--bs-secondary-color') || '').trim() || '#6c757d'
			};
		}

		var pctLabelPlugin = {
			id: 'pctLabels',
			afterDatasetsDraw: function(chart) {
				var meta = chart.getDatasetMeta(0);
				var c = chart.ctx;
				meta.data.forEach(function(bar, i) {
					if (i >= currentVols.length) return;
					var pct = currentVols[i].pct;
					var barWidth = Math.abs(bar.x - bar.base);
					if (barWidth < 28) return;
					c.save();
					c.fillStyle = '#fff';
					c.font = 'bold 11px sans-serif';
					c.textAlign = 'center';
					c.textBaseline = 'middle';
					c.fillText(pct + '%', (bar.x + bar.base) / 2, bar.y);
					c.restore();
				});
			}
		};

		/* Draws a dashed vertical line at the maximum used value so each bar
		   can be visually compared against the highest usage in the set. */
		var maxRefPlugin = {
			id: 'maxRef',
			afterDatasetsDraw: function(chart) {
				if (!currentVols || currentVols.length < 2) return;
				var usedVals = currentVols.map(function(v) { return v.used; });
				var maxUsed  = Math.max.apply(null, usedVals);
				var minUsed  = Math.min.apply(null, usedVals);
				if (maxUsed === minUsed) return;
				var theme   = getThemeColors();
				var xPx     = chart.scales.x.getPixelForValue(maxUsed);
				var yTop    = chart.chartArea.top;
				var yBottom = chart.chartArea.bottom;
				var c = chart.ctx;
				c.save();
				c.strokeStyle = theme.mutedColor;
				c.lineWidth   = 1.5;
				c.setLineDash([4, 3]);
				c.beginPath();
				c.moveTo(xPx, yTop);
				c.lineTo(xPx, yBottom);
				c.stroke();
				c.setLineDash([]);
				c.fillStyle = theme.mutedColor;
				c.font      = '10px sans-serif';
				c.textAlign = 'center';
				c.textBaseline = 'bottom';
				c.fillText('max', xPx, yTop - 1);
				c.restore();
			}
		};

		function renderChart(vols) {
			currentVols = vols || [];
			var wrap = document.getElementById('tgDiskUsageWrap');
			if (!vols || vols.length === 0) {
				renderSummary([]);
				wrap.innerHTML = '<div class="text-muted small fst-italic px-1">No disk usage data available yet &mdash; waiting for first polling cycle.</div>';
				return;
			}
			renderSummary(vols);
			var theme   = getThemeColors();
			var pctVals = vols.map(function(v) { return v.pct; });
			var gMin = vols.length > 1 ? Math.min.apply(null, pctVals) : null;
			var gMax = vols.length > 1 ? Math.max.apply(null, pctVals) : null;
			var showMM = gMin !== null && gMin !== gMax;
			var labels = vols.map(function(v) {
				var lbl = 'Vol ' + v.volume;
				if (showMM) {
					if (v.pct === gMax) lbl += ' \u25B2';
					else if (v.pct === gMin) lbl += ' \u25BC';
				}
				return lbl;
			});
			var usedData = vols.map(function(v) { return v.used; });
			var freeData = vols.map(function(v) { return v.free; });
			var bgColors = vols.map(function(v) { return pctColor(v.pct); });

			if (chartInst) {
				chartInst.data.labels = labels;
				chartInst.data.datasets[0].data = usedData;
				chartInst.data.datasets[0].backgroundColor = bgColors;
				chartInst.data.datasets[1].data = freeData;
				chartInst.data.datasets[1].backgroundColor = theme.secondaryBg;
				chartInst.update('none');
				return;
			}

			var chartHeight = Math.max(260, vols.length * 32);
			wrap.innerHTML = '<canvas id="tgDiskUsageChart" style="height:' + chartHeight + 'px"></canvas>';
			var ctx = document.getElementById('tgDiskUsageChart').getContext('2d');
			chartInst = new Chart(ctx, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{ label: 'Used', data: usedData, backgroundColor: bgColors, stack: 'disk' },
						{ label: 'Free', data: freeData, backgroundColor: theme.secondaryBg, stack: 'disk' }
					]
				},
				options: {
					indexAxis: 'y',
					responsive: true,
					maintainAspectRatio: false,
					animation: false,
					plugins: {
						legend: { display: false },
						tooltip: {
							callbacks: {
								label: function(ctx) {
									var v = currentVols[ctx.dataIndex];
									if (ctx.datasetIndex === 0)
										return ' Used: ' + fmtBytes(v.used) + ' (' + v.pct + '%)';
									return ' Free: ' + fmtBytes(v.free);
								},
								title: function(items) {
									var v = currentVols[items[0].dataIndex];
									return 'Vol ' + v.volume + '  -  total: ' + fmtBytes(v.total);
								}
							}
						}
					},
					scales: {
						x: {
							stacked: true,
							ticks: { color: theme.bodyColor, callback: function(v) { return fmtBytes(v); } },
							grid:  { color: theme.borderColor }
						},
						y: {
							stacked: true,
							ticks: { color: theme.bodyColor },
							grid:  { color: theme.borderColor }
						}
					}
				},
				plugins: [pctLabelPlugin, maxRefPlugin]
			});
		}

		function computeAverage(moversData, volumeCount) {
			var sumUsed  = new Array(volumeCount).fill(0);
			var sumTotal = new Array(volumeCount).fill(0);
			var count = 0;
			groupMovers.forEach(function(moverName) {
				var mvVols = moversData[moverName];
				if (!mvVols || mvVols.length === 0) return;
				mvVols.forEach(function(v) {
					if (v.volume < volumeCount) {
						sumUsed[v.volume]  += v.used;
						sumTotal[v.volume] += v.total;
					}
				});
				count++;
			});
			if (count === 0) return [];
			return sumUsed.map(function(u, i) {
				var avgUsed  = u / count;
				var avgTotal = sumTotal[i] / count;
				var avgFree  = Math.max(0, avgTotal - avgUsed);
				var pct = avgTotal > 0 ? Math.round(100 * avgUsed / avgTotal) : 0;
				return { volume: i, used: avgUsed, total: avgTotal, free: avgFree, pct: pct };
			});
		}

		window.tgSetView = function(view) {
			currentView = view;
			document.getElementById('tgViewWorstBtn').classList.toggle('active', view === 'worst');
			document.getElementById('tgViewAvgBtn').classList.toggle('active', view === 'average');
			var vols = view === 'worst' ? lastWorstVols : lastAvgVols;
			renderSummary(vols);
			renderChart(vols);
		};

		function updateAge() {
			var el = document.getElementById('tgDiskUsageAge');
			if (!el || !fetchedAt) return;
			var sec = Math.round((Date.now() - fetchedAt) / 1000);
			el.textContent = '(updated ' + sec + 's ago)';
		}

		var _tgFails = 0, _tgRefIv, _tgAgeIv;
		function refresh() {
			var p1 = $.getJSON('/do/datafile/diskusage/' + encodeURIComponent(groupName));
			var p2 = $.getJSON('/do/datafile/moverdiskusage');
			$.when(p1, p2).done(function(r1, r2) {
				_tgFails = 0;
				fetchedAt = Date.now();
				var worstVols  = (r1[0].groups && r1[0].groups[groupName]) ? r1[0].groups[groupName] : [];
				var moversData = r2[0].movers || {};
				lastWorstVols  = worstVols;
				lastAvgVols    = worstVols.length > 0 ? computeAverage(moversData, worstVols.length) : [];
				renderChart(currentView === 'worst' ? lastWorstVols : lastAvgVols);
			}).fail(function() {
				if (++_tgFails >= 3) {
					clearInterval(_tgRefIv);
					clearInterval(_tgAgeIv);
					var el = document.getElementById('tgDiskUsageAge');
					if (el) el.innerHTML = '<i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>'
						+ '<span class="text-warning-emphasis">Session expired</span>'
						+ ' <a href="#" onclick="location.reload();return false;" class="small">reload</a>';
				}
			});
		}

		refresh();
		_tgRefIv = setInterval(refresh, 5000);
		_tgAgeIv = setInterval(updateAge, 1000);

		new MutationObserver(function() {
			var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
			if (t === _obsTheme) return;
			_obsTheme = t;
			if (chartInst) {
				chartInst.destroy();
				chartInst = null;
				document.getElementById('tgDiskUsageWrap').innerHTML = '';
			}
			if (currentVols && currentVols.length > 0) renderChart(currentVols);
		}).observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });
	})();
	</script>

</c:if>

