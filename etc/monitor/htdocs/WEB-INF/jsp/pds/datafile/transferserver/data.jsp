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

	<p class="fw-bold mb-1 mt-3">Disk Usage <span id="moverDiskUsageAge" class="text-muted fw-normal small ms-2"></span></p>
	<div id="moverDiskUsageWrap">
		<div class="text-muted small fst-italic">Loading&hellip;</div>
	</div>

	<script src="/assets/js/chart.umd.min.js"></script>
	<script>
	(function() {
		var moverName  = '${transferserver.name}';
		var chartInst  = null;
		var fetchedAt  = null;
		var currentVols = [];
		var _obsTheme  = null;

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

		function buildChart(vols) {
			currentVols = vols || [];
			var wrap = document.getElementById('moverDiskUsageWrap');
			if (!vols || vols.length === 0) {
				wrap.innerHTML = '<div class="alert alert-info py-1 px-2 small">No disk usage data available yet - waiting for first polling cycle.</div>';
				return;
			}
			var theme    = getThemeColors();
			var pcts     = vols.map(function(v) { return v.pct; });
			var gMin = vols.length > 1 ? Math.min.apply(null, pcts) : null;
			var gMax = vols.length > 1 ? Math.max.apply(null, pcts) : null;
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
			var bgColors = pcts.map(pctColor);

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
		wrap.innerHTML = '<canvas id="moverDiskUsageChart" style="height:' + chartHeight + 'px"></canvas>';
			var ctx = document.getElementById('moverDiskUsageChart').getContext('2d');
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
							backgroundColor: theme.secondaryBg,
							stack: 'disk'
						}
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

		function updateAge() {
			var el = document.getElementById('moverDiskUsageAge');
			if (!el || !fetchedAt) return;
			var sec = Math.round((Date.now() - fetchedAt) / 1000);
			el.textContent = '(updated ' + sec + 's ago)';
		}

		var _tsFails = 0, _tsRefIv, _tsAgeIv;
		function refresh() {
			$.getJSON('/do/datafile/moverdiskusage/' + encodeURIComponent(moverName))
				.done(function(data) {
					_tsFails = 0;
					fetchedAt = Date.now();
					var vols = (data.movers && data.movers[moverName]) ? data.movers[moverName] : [];
					buildChart(vols);
				})
				.fail(function() {
					if (++_tsFails >= 3) {
						clearInterval(_tsRefIv);
						clearInterval(_tsAgeIv);
						var el = document.getElementById('moverDiskUsageAge');
						if (el) el.innerHTML = '<i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>'
							+ '<span class="text-warning-emphasis">Session expired</span>'
							+ ' <a href="#" onclick="location.reload();return false;" class="small">reload</a>';
					}
				});
		}

		refresh();
		_tsRefIv = setInterval(refresh, 5000);
		_tsAgeIv = setInterval(updateAge, 1000);

		new MutationObserver(function() {
			var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
			if (t === _obsTheme) return;
			_obsTheme = t;
			if (chartInst) {
				chartInst.destroy();
				chartInst = null;
				document.getElementById('moverDiskUsageWrap').innerHTML = '';
			}
			if (currentVols && currentVols.length > 0) buildChart(currentVols);
		}).observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });
	})();
	</script>

</c:if>
