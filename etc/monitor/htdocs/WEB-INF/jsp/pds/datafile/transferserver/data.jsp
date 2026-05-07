<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${not empty isDelete}">
	<tiles:insert page="./pds/datafile/transferserver/warning.jsp" />
</c:if>
<c:if test="${empty isDelete}">

	<div class="form-info-banner" style="margin-left:0">
		<i class="bi bi-server text-primary flex-shrink-0"></i>
		Data Mover: <strong><c:out value="${transferserver.name}"/></strong>
	</div>

	<table class="fields">
		<tr>
			<th>Name</th>
			<td><c:out value="${transferserver.name}" /></td>
		</tr>
		<tr>
			<th>Hostname</th>
			<td><c:out value="${transferserver.host}" /></td>
		</tr>
		<tr>
			<th>Port</th>
			<td><c:out value="${transferserver.port}" /></td>
		</tr>
		<tr>
			<th>Group</th>
			<td><a
				href="/do/datafile/transfergroup/${transferserver.transferGroupName}">${transferserver.transferGroupName}</a></td>
		</tr>

		<c:set var="hostForReplication"
			value="${transferserver.hostForReplication}" />
		<c:if test="${transferserver.replicate}">
			<c:if test="${not empty hostForReplication}">
				<tr>
					<th>Host For Replication</th>
					<td><a href="/do/transfer/host/${hostForReplication.name}">${hostForReplication.nickName}</a></td>
				</tr>
			</c:if>
		</c:if>

		<tr>
			<th>Check</th>
			<td><c:if test="${transferserver.check}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transferserver.check}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
		</tr>
		<tr>
			<th>Enabled</th>
			<td><c:if test="${transferserver.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transferserver.active}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Replicate</th>
			<td><c:if test="${transferserver.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
					test="${!transferserver.replicate}">
					<i class="bi bi-x-circle-fill text-danger" title="No"></i>
				</c:if></td>
		</tr>
		<tr>
			<th>Last Update</th>
			<td><content:content name="transferserver.lastUpdateDate" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
		</tr>
	</table>

	<p class="fw-bold mb-1 mt-3">Disk Usage <span id="moverDiskUsageAge" class="text-muted fw-normal small ms-2"></span></p>
	<div id="moverDiskUsageWrap">
		<div class="text-muted small fst-italic">Loading&hellip;</div>
	</div>

	<script src="/assets/js/chart.umd.min.js"></script>
	<script>
	(function() {
		var moverName = '${transferserver.name}';
		var chartInst = null;
		var fetchedAt = null;

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

		function buildChart(vols) {
			var wrap = document.getElementById('moverDiskUsageWrap');
			if (!vols || vols.length === 0) {
				wrap.innerHTML = '<div class="alert alert-info py-1 px-2 small">No disk usage data available yet - waiting for first polling cycle.</div>';
				return;
			}
			var labels   = vols.map(function(v) { return 'Vol ' + v.volume; });
			var usedData = vols.map(function(v) { return v.used; });
			var freeData = vols.map(function(v) { return v.free; });
			var pcts     = vols.map(function(v) { return v.pct; });
			var bgColors = pcts.map(pctColor);

			if (chartInst) {
				chartInst.data.labels = labels;
				chartInst.data.datasets[0].data = usedData;
				chartInst.data.datasets[0].backgroundColor = bgColors;
				chartInst.data.datasets[1].data = freeData;
				chartInst.update('none');
				return;
			}

			wrap.innerHTML = '<canvas id="moverDiskUsageChart" style="max-height:260px"></canvas>';
			var ctx = document.getElementById('moverDiskUsageChart').getContext('2d');
			var pctLabelPlugin = {
				id: 'pctLabels',
				afterDatasetsDraw: function(chart) {
					var meta = chart.getDatasetMeta(0);
					var c = chart.ctx;
					meta.data.forEach(function(bar, i) {
						var pct = vols[i].pct;
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
			chartInst = new Chart(ctx, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{
							label: 'Used',
							data: usedData,
							backgroundColor: bgColors,
							stack: 'disk'
						},
						{
							label: 'Free',
							data: freeData,
							backgroundColor: '#e9ecef',
							stack: 'disk'
						}
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
									var v = vols[ctx.dataIndex];
									if (ctx.datasetIndex === 0)
										return ' Used: ' + fmtBytes(v.used) + ' (' + v.pct + '%)';
									return ' Free: ' + fmtBytes(v.free);
								},
								title: function(items) {
									var v = vols[items[0].dataIndex];
									return 'Vol ' + v.volume + '  -  total: ' + fmtBytes(v.total);
								}
							}
						}
					},
					scales: {
						x: {
							stacked: true,
							ticks: { callback: function(v) { return fmtBytes(v); } }
						},
						y: { stacked: true }
					}
				},
				plugins: [pctLabelPlugin]
			});
		}

		function updateAge() {
			var el = document.getElementById('moverDiskUsageAge');
			if (!el || !fetchedAt) return;
			var sec = Math.round((Date.now() - fetchedAt) / 1000);
			el.textContent = '(updated ' + sec + 's ago)';
		}

		function refresh() {
			$.getJSON('/do/datafile/moverdiskusage/' + encodeURIComponent(moverName))
				.done(function(data) {
					fetchedAt = Date.now();
					var vols = (data.movers && data.movers[moverName]) ? data.movers[moverName] : [];
					buildChart(vols);
				});
		}

		refresh();
		setInterval(refresh, 5000);
		setInterval(updateAge, 1000);
	})();
	</script>

</c:if>
