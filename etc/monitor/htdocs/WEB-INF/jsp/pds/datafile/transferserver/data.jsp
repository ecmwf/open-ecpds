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
		style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
		<i class="bi bi-server text-primary flex-shrink-0"></i>
		<span>Data Mover: <strong><c:out value="${transferserver.name}"/></strong></span>
		<auth:if basePathKey="transferserver.basepath" paths="/edit/insert_form">
		<auth:then>
		<div class="d-flex gap-1 ms-auto flex-shrink-0">
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
		<div class="card-body">
			<div class="row g-3">
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Name</div>
					<div class="fw-semibold"><c:out value="${transferserver.name}" /></div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Transfer Group</div>
					<div><a href="/do/datafile/transfergroup/${transferserver.transferGroupName}">${transferserver.transferGroupName}</a></div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Last Update</div>
					<div><content:content name="transferserver.lastUpdateDate" dateFormatKey="date.format.transfer" ignoreNull="true"/></div>
				</div>
			</div>
		</div>
	</div>

	<%-- Card: Connection --%>
	<div class="card border-0 shadow-sm mb-3">
		<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
			<i class="bi bi-ethernet text-primary"></i>
			<span class="fw-semibold">Connection</span>
		</div>
		<div class="card-body">
			<div class="row g-3">
				<div class="col-sm-5">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Hostname</div>
					<div><c:out value="${transferserver.host}" /></div>
				</div>
				<div class="col-sm-3">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Port</div>
					<div><c:out value="${transferserver.port}" /></div>
				</div>
				<c:set var="hostForReplication" value="${transferserver.hostForReplication}" />
				<c:if test="${transferserver.replicate and not empty hostForReplication}">
					<div class="col-sm-4">
						<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Host For Replication</div>
						<div><a href="/do/transfer/host/${hostForReplication.name}">${hostForReplication.nickName}</a></div>
					</div>
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
		<div class="card-body">
			<div class="row g-3">
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Check</div>
					<div>
						<c:choose>
							<c:when test="${transferserver.check}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
							<c:otherwise><i class="bi bi-x-circle-fill text-danger"></i> No</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Enabled</div>
					<div>
						<c:choose>
							<c:when test="${transferserver.active}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
							<c:otherwise><i class="bi bi-x-circle-fill text-danger"></i> No</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="col-sm-4">
					<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Replicate</div>
					<div>
						<c:choose>
							<c:when test="${transferserver.replicate}"><i class="bi bi-check-circle-fill text-success"></i> Yes</c:when>
							<c:otherwise><i class="bi bi-x-circle-fill text-danger"></i> No</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
	</div>

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
