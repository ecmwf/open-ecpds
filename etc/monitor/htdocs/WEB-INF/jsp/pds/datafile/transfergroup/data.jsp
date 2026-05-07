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

	<div class="form-info-banner" style="margin-left:0">
		<i class="bi bi-collection text-primary flex-shrink-0"></i>
		Transfer Group: <strong><c:out value="${transfergroup.name}"/></strong>
	</div>

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${transfergroup.name}" /></td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><c:out value="${transfergroup.comment}" /></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${transfergroup.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.active}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Replicate</th>
			<td><c:if test="${transfergroup.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.replicate}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Min. Replication Count</th>
			<td>${transfergroup.minReplicationCount}</td>
		</tr>
		<tr>
			<th>Volume Count</th>
			<td>${transfergroup.volumeCount}</td>
		</tr>
		<tr>
			<th>Filter</th>
			<td><c:if test="${transfergroup.filter}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.filter}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Min. Filtering Count</th>
			<td>${transfergroup.minFilteringCount}</td>
		</tr>
		<tr>
			<th>Backup</th>
			<td><c:if test="${transfergroup.backup}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transfergroup.backup}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<c:set var="hostForBackup" value="${transfergroup.hostForBackup}" />
		<c:if test="${transfergroup.backup}">
			<c:if test="${not empty hostForBackup}">
				<tr>
					<th>Host For Backup</th>
					<td><a href="/do/transfer/host/${hostForBackup.name}">${hostForBackup.nickName}</a></td>
				</tr>
			</c:if>
		</c:if>

		<c:set var="clusterName" value="${transfergroup.clusterName}" />
		<c:if test="${not empty clusterName}">
			<tr>
				<th>Cluster Name</th>
				<td><c:out value="${clusterName}" /></td>
			</tr>
			<tr>
				<th>Cluster Weight</th>
				<td><c:out value="${transfergroup.clusterWeight}" /></td>
			</tr>
		</c:if>

	</table>

	<p class="fw-bold mb-1 mt-2">Data Movers</p>
	<table id="tgServersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
		<thead class="table-light">
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
					<c:if test="${server.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
					<c:if test="${!server.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
				</td>
				<td class="text-center" data-order="${server.replicate ? 1 : 0}">
					<c:if test="${server.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
					<c:if test="${!server.replicate}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<script>
	$(document).ready(function() {
		$('#tgServersTable').DataTable({
			paging:    false,
			searching: false,
			ordering:  true,
			info:      false,
			order:     [[0, 'asc']]
		});
	});
	</script>

	<div class="d-flex align-items-center mb-1 mt-3">
		<p class="fw-bold mb-0">Disk Usage</p>
		<span id="tgDiskUsageAge" class="text-muted fw-normal small ms-2"></span>
		<button class="btn btn-link btn-sm text-muted ms-1 p-0" type="button"
			data-bs-toggle="collapse" data-bs-target="#tgDiskUsageLegend"
			aria-expanded="false" title="About this chart">
			<i class="bi bi-info-circle"></i>
		</button>
	</div>
	<div class="collapse mb-2" id="tgDiskUsageLegend">
		<div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
			<strong class="d-block mb-1">Two views are available:</strong>
			<ul class="mb-0 ps-3">
				<li><strong>Worst-case</strong> &mdash; per volume, shows the highest used bytes across all Data Movers compared to the smallest reported capacity. This is the binding constraint used by the load balancer to select volumes.</li>
				<li><strong>Average</strong> &mdash; per volume, shows the mean used bytes and mean capacity across all Data Movers. This gives an overall utilisation picture when movers are heterogeneous.</li>
			</ul>
			<div class="mt-1 text-muted">When only one Data Mover is in the group, both views are identical.</div>
		</div>
	</div>
	<div class="btn-group btn-group-sm mb-2" role="group" aria-label="Chart view">
		<button type="button" class="btn btn-outline-secondary active" id="tgViewWorstBtn" onclick="tgSetView('worst')">Worst-case</button>
		<button type="button" class="btn btn-outline-secondary" id="tgViewAvgBtn" onclick="tgSetView('average')">Average</button>
	</div>
	<div id="tgDiskUsageWrap">
		<div class="text-muted small fst-italic">Loading&hellip;</div>
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

		function renderChart(vols) {
			currentVols = vols || [];
			var wrap = document.getElementById('tgDiskUsageWrap');
			if (!vols || vols.length === 0) {
				wrap.innerHTML = '<div class="alert alert-info py-1 px-2 small">No disk usage data available yet - waiting for first polling cycle.</div>';
				return;
			}
			var labels   = vols.map(function(v) { return 'Vol ' + v.volume; });
			var usedData = vols.map(function(v) { return v.used; });
			var freeData = vols.map(function(v) { return v.free; });
			var bgColors = vols.map(function(v) { return pctColor(v.pct); });

			if (chartInst) {
				chartInst.data.labels = labels;
				chartInst.data.datasets[0].data = usedData;
				chartInst.data.datasets[0].backgroundColor = bgColors;
				chartInst.data.datasets[1].data = freeData;
				chartInst.update('none');
				return;
			}

			wrap.innerHTML = '<canvas id="tgDiskUsageChart" style="max-height:260px"></canvas>';
			var ctx = document.getElementById('tgDiskUsageChart').getContext('2d');
			chartInst = new Chart(ctx, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{ label: 'Used', data: usedData, backgroundColor: bgColors, stack: 'disk' },
						{ label: 'Free', data: freeData, backgroundColor: '#e9ecef', stack: 'disk' }
					]
				},
				options: {
					indexAxis: 'y',
					responsive: true,
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
						x: { stacked: true, ticks: { callback: function(v) { return fmtBytes(v); } } },
						y: { stacked: true }
					}
				},
				plugins: [pctLabelPlugin]
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
			renderChart(view === 'worst' ? lastWorstVols : lastAvgVols);
		};

		function updateAge() {
			var el = document.getElementById('tgDiskUsageAge');
			if (!el || !fetchedAt) return;
			var sec = Math.round((Date.now() - fetchedAt) / 1000);
			el.textContent = '(updated ' + sec + 's ago)';
		}

		function refresh() {
			var p1 = $.getJSON('/do/datafile/diskusage/' + encodeURIComponent(groupName));
			var p2 = $.getJSON('/do/datafile/moverdiskusage');
			$.when(p1, p2).done(function(r1, r2) {
				fetchedAt = Date.now();
				var worstVols  = (r1[0].groups && r1[0].groups[groupName]) ? r1[0].groups[groupName] : [];
				var moversData = r2[0].movers || {};
				lastWorstVols  = worstVols;
				lastAvgVols    = worstVols.length > 0 ? computeAverage(moversData, worstVols.length) : [];
				renderChart(currentView === 'worst' ? lastWorstVols : lastAvgVols);
			});
		}

		refresh();
		setInterval(refresh, 5000);
		setInterval(updateAge, 1000);
	})();
	</script>

</c:if>

