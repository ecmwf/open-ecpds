<%@ page session="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
	<i class="bi bi-grid-3x3 text-primary"></i>
	<span class="fw-semibold">Download Activity</span>
	<button class="btn btn-link btn-sm text-muted p-0" type="button"
		data-bs-toggle="collapse" data-bs-target="#dlInfoLegend"
		aria-expanded="false" title="About this matrix">
		<i class="bi bi-info-circle"></i>
	</button>
	<div class="ms-auto d-flex align-items-center gap-2">
		<div class="dropdown">
			<button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
				id="dlGroupDropdown" data-bs-toggle="dropdown" data-bs-auto-close="outside"
				aria-expanded="false">
				All groups
			</button>
			<ul class="dropdown-menu dropdown-menu-end" id="dlGroupMenu" style="min-width:14rem; max-height:18rem; overflow-y:auto;">
				<li class="px-3 py-1 border-bottom">
					<div class="d-flex gap-2">
						<button class="btn btn-link btn-sm p-0 text-decoration-none" id="dlSelectAll">All</button>
						<span class="text-muted">|</span>
						<button class="btn btn-link btn-sm p-0 text-decoration-none" id="dlSelectNone">None</button>
					</div>
				</li>
			</ul>
		</div>
		<span id="dlAge" class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border text-nowrap">updating&hellip;</span>
	</div>
</div>

<div class="collapse" id="dlInfoLegend">
	<div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
		<strong class="d-block mb-1">Live in-flight download counts per Data Mover and volume</strong>
		<p class="mb-1">A <em>download</em> is the process of fetching the content of a new data file onto a Data Mover. It is triggered either by the discovery of a file through an acquisition host, or by the retrieval of a file following a registration via the <code>ecpds</code> command utility.</p>
		<ul class="mb-1 ps-3">
			<li>Each <strong>row</strong> is a Data Mover, grouped by Transfer Group.</li>
			<li>Each <strong>column</strong> is a volume (filesystem index) on that mover.</li>
			<li>The cell value is the number of files <em>currently</em> being downloaded on that volume.</li>
		</ul>
		<div class="d-flex gap-3 flex-wrap">
			<span><span class="text-muted" style="font-size:0.7rem">&#8211;</span> Idle (0)</span>
			<span><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border">1</span> Low</span>
			<span><span class="badge rounded-pill bg-warning text-dark">2&ndash;4</span> Moderate</span>
			<span><span class="badge rounded-pill text-white" style="background:#fd7e14">5&ndash;7</span> High</span>
			<span><span class="badge rounded-pill bg-danger text-white">8+</span> Very high</span>
		</div>
		<div class="mt-1 text-muted">Updated every second &mdash; only cells whose value changed are refreshed in place.</div>
	</div>
</div>

<div class="card-body p-0">
<div id="dlMatrixContainer">
	<div class="text-muted small fst-italic">Loading&hellip;</div>
</div>
</div>
</div>

<style>
#dlMatrixScroll {
	overflow-x: auto;
}
#dlMatrix {
	font-size: 0.78rem;
	border-collapse: collapse;
	table-layout: fixed;
	width: 100%;
}
#dlMatrix th,
#dlMatrix td {
	padding: 2px 4px;
	border: 1px solid var(--bs-border-color);
	white-space: nowrap;
	text-align: center;
	vertical-align: middle;
}
#dlMatrix thead th:first-child {
	background: var(--bs-secondary-bg);
	font-weight: 600;
	font-size: 0.72rem;
	text-align: left;
	vertical-align: bottom;
}
#dlMatrix thead th.dl-vol-hdr {
	background: var(--bs-secondary-bg);
	font-weight: 600;
	font-size: 0.72rem;
	/* Rotate header text so columns can be very narrow */
	writing-mode: vertical-rl;
	transform: rotate(180deg);
	height: 3.5rem;
	padding: 4px 6px;
	vertical-align: bottom;
	width: 2rem;
}
#dlMatrix td.dl-mover {
	text-align: left;
	font-weight: 500;
	background: var(--bs-tertiary-bg);
	padding-right: 0.6rem;
}
#dlMatrix tr.dl-group-hdr td {
	background: var(--bs-primary-bg-subtle);
	font-weight: 700;
	font-size: 0.72rem;
	letter-spacing: 0.04em;
	text-align: left;
	color: var(--bs-primary);
}
td.dl-flash {
	animation: dlflash 0.4s ease-out;
}
@keyframes dlflash {
	0%   { background-color: #fff3cd; }
	100% { background-color: inherit; }
}
</style>

<script>
(function () {
	var prevData  = null;
	var colVols   = null;
	var fetchedAt = null;

	/*
	 * selectedMovers = null  -> show all movers (no filter)
	 * selectedMovers = {}    -> explicit set; only movers present are shown
	 */
	var selectedMovers = null;
	var knownGroups = [];   /* sorted group names */
	var knownMovers = {};   /* group -> sorted array of full mover keys */

	/* ── Helpers ── */
	function groupOf(mk)     { var d = mk.indexOf('.'); return d >= 0 ? mk.substring(0, d) : mk; }
	function shortMover(mk)  { var d = mk.indexOf('.'); return d >= 0 ? mk.substring(d + 1) : mk; }

	function allMoverKeys() {
		var all = [];
		knownGroups.forEach(function (g) { (knownMovers[g] || []).forEach(function (mk) { all.push(mk); }); });
		return all;
	}

	function isMoverVisible(mk) {
		return selectedMovers === null || !!selectedMovers[mk];
	}

	function applyFilter(downloads) {
		if (selectedMovers === null) return downloads;
		var filtered = {};
		Object.keys(downloads).forEach(function (mk) { if (selectedMovers[mk]) filtered[mk] = downloads[mk]; });
		return filtered;
	}

	/* ── Badge ── */
	function dlBadge(n) {
		if (n <= 0) return '<span class="text-muted" style="font-size:0.7rem">&#8211;</span>';
		var cls = n >= 8 ? 'bg-danger text-white'
		        : n >= 5 ? 'bg-warning text-dark'
		        : n >= 2 ? 'bg-warning text-dark'
		        :          'bg-secondary-subtle text-secondary-emphasis border';
		var style = n >= 5 && n < 8 ? ' style="background:#fd7e14!important;color:#fff!important"' : '';
		return '<span class="badge rounded-pill ' + cls + '"' + style + '>' + n + '</span>';
	}

	/* ── Volume helpers ── */
	function allVolumes(downloads) {
		var volSet = {};
		Object.keys(downloads).forEach(function (mk) {
			var arr = downloads[mk];
			for (var i = 0; i < arr.length; i++) volSet[i] = true;
		});
		if (colVols) colVols.forEach(function (v) { volSet[v] = true; });
		return Object.keys(volSet).map(Number).sort(function (a, b) { return a - b; });
	}

	/* ── Dropdown label ── */
	function updateDropdownLabel() {
		var btn = document.getElementById('dlGroupDropdown');
		if (!btn) return;
		if (selectedMovers === null) {
			btn.textContent = 'All movers';
			return;
		}
		var n = Object.keys(selectedMovers).length;
		btn.textContent = n === 0 ? 'No movers selected' : n + ' mover' + (n === 1 ? '' : 's') + ' selected';
	}

	/* ── Toggle single mover ── */
	function toggleMover(mk, visible) {
		if (selectedMovers === null) {
			/* Switch from "show all" to explicit mode with all currently known movers selected */
			selectedMovers = {};
			allMoverKeys().forEach(function (k) { selectedMovers[k] = true; });
		}
		if (visible) selectedMovers[mk] = true;
		else delete selectedMovers[mk];
		/* If all known movers are selected again, revert to null (show all) */
		var all = allMoverKeys();
		if (all.length > 0 && all.every(function (k) { return selectedMovers[k]; })) selectedMovers = null;
		prevData = null; colVols = null;
		updateDropdownLabel();
	}

	/* ── Toggle whole group ── */
	function selectGroup(grp, visible) {
		var movers = knownMovers[grp] || [];
		if (visible) {
			if (selectedMovers !== null) {
				movers.forEach(function (mk) { selectedMovers[mk] = true; });
				var all = allMoverKeys();
				if (all.every(function (k) { return selectedMovers[k]; })) selectedMovers = null;
			}
		} else {
			if (selectedMovers === null) {
				selectedMovers = {};
				allMoverKeys().forEach(function (k) { selectedMovers[k] = true; });
			}
			movers.forEach(function (mk) { delete selectedMovers[mk]; });
		}
		prevData = null; colVols = null;
		rebuildMenu();
		updateDropdownLabel();
	}

	/* ── Build dropdown menu ── */
	function rebuildMenu() {
		var menu = document.getElementById('dlGroupMenu');
		if (!menu) return;
		Array.from(menu.querySelectorAll('li.dl-grp-item')).forEach(function (li) { li.remove(); });

		knownGroups.forEach(function (grp) {
			var movers = knownMovers[grp] || [];
			var li = document.createElement('li');
			li.className = 'dl-grp-item border-top';

			/* Group header row */
			var hdr = document.createElement('div');
			hdr.className = 'px-2 pt-1 pb-0 d-flex align-items-center';
			hdr.innerHTML = '<span class="fw-semibold small flex-grow-1">' + grp + '</span>'
			              + '<button type="button" class="btn btn-link btn-sm py-0 px-1 text-decoration-none dl-grp-btn" data-grp="' + grp + '" data-vis="1">All</button>'
			              + '<span class="text-muted small">|</span>'
			              + '<button type="button" class="btn btn-link btn-sm py-0 px-1 text-decoration-none dl-grp-btn" data-grp="' + grp + '" data-vis="0">None</button>';
			li.appendChild(hdr);

			/* Mover checkboxes */
			movers.forEach(function (mk) {
				var chk = document.createElement('div');
				chk.className = 'form-check px-4 mb-0 py-0';
				var checked = isMoverVisible(mk) ? ' checked' : '';
				chk.innerHTML = '<input class="form-check-input dl-mover-cb" type="checkbox" value="' + mk
				              + '" id="dlm_' + mk.replace(/\./g, '_') + '"' + checked + '>'
				              + '<label class="form-check-label small" for="dlm_' + mk.replace(/\./g, '_') + '">'
				              + shortMover(mk) + '</label>';
				li.appendChild(chk);
			});

			menu.appendChild(li);
		});

		/* Wire events via delegation from menu */
		menu.querySelectorAll('.dl-mover-cb').forEach(function (cb) {
			cb.addEventListener('change', function () { toggleMover(this.value, this.checked); rebuildMenu(); });
		});
		menu.querySelectorAll('.dl-grp-btn').forEach(function (btn) {
			btn.addEventListener('click', function () { selectGroup(this.dataset.grp, this.dataset.vis === '1'); });
		});
	}

	/* ── Sync known groups/movers from data ── */
	function syncMenu(downloads) {
		var changed = false;
		Object.keys(downloads).sort().forEach(function (mk) {
			var grp = groupOf(mk);
			if (!knownMovers[grp]) { knownMovers[grp] = []; knownGroups.push(grp); knownGroups.sort(); changed = true; }
			if (knownMovers[grp].indexOf(mk) < 0) { knownMovers[grp].push(mk); knownMovers[grp].sort(); changed = true; }
		});
		if (changed) rebuildMenu();
	}

	/* ── Column sizing (adapts to container width) ── */
	var MOVER_COL_PX = 160; /* fixed mover column width in px */
	var MIN_VOL_PX   = 34;  /* minimum volume column width (fits a badge) */
	var MAX_VOL_PX   = 70;  /* maximum volume column width (avoid too much whitespace) */

	function calcVolWidth() {
		if (!colVols || colVols.length === 0) return MAX_VOL_PX;
		var scroll = document.getElementById('dlMatrixScroll');
		var containerW = scroll ? scroll.clientWidth : 800;
		var available = containerW - MOVER_COL_PX;
		var ideal = Math.floor(available / colVols.length);
		return Math.min(MAX_VOL_PX, Math.max(MIN_VOL_PX, ideal));
	}

	function applyColWidths() {
		var table = document.getElementById('dlMatrix');
		if (!table || !colVols) return;
		var volW = calcVolWidth() + 'px';
		var cols = table.querySelectorAll('col');
		if (cols.length === 0) return;
		/* cols[0] = mover column, rest = volume columns */
		for (var i = 1; i < cols.length; i++) cols[i].style.width = volW;
	}

	/* ── Table build / patch ── */
	function buildTable(downloads) {
		var movers = Object.keys(downloads).sort();
		colVols = allVolumes(downloads);
		var volW = calcVolWidth() + 'px';
		/* colgroup for table-layout:fixed column sizing */
		var html = '<div id="dlMatrixScroll"><table id="dlMatrix" class="table table-bordered table-sm mb-0">'
		         + '<colgroup><col style="width:' + MOVER_COL_PX + 'px">';
		colVols.forEach(function () { html += '<col style="width:' + volW + '">'; });
		html += '</colgroup>';
		html += '<thead><tr><th>Data Mover</th>';
		colVols.forEach(function (v) { html += '<th class="dl-vol-hdr">Vol&nbsp;' + v + '</th>'; });
		html += '</tr></thead><tbody>';
		var lastGroup = null;
		movers.forEach(function (mk) {
			var grp = groupOf(mk);
			if (grp !== lastGroup) {
				lastGroup = grp;
				html += '<tr class="dl-group-hdr"><td colspan="' + (colVols.length + 1) + '">'
				      + '<a href="/do/datafile/transfergroup/' + encodeURIComponent(grp) + '" class="text-reset text-decoration-none">' + grp + '</a>'
				      + '</td></tr>';
			}
			var ms = shortMover(mk);
			html += '<tr data-mk="' + mk + '"><td class="dl-mover">'
			      + '<a href="/do/datafile/transferserver/' + encodeURIComponent(ms) + '" class="text-reset text-decoration-none">' + ms + '</a>'
			      + '</td>';
			var arr = downloads[mk] || [];
			colVols.forEach(function (v) {
				html += '<td data-mk="' + mk + '" data-vol="' + v + '">' + dlBadge(arr[v] || 0) + '</td>';
			});
			html += '</tr>';
		});
		html += '</tbody></table></div>';
		return html;
	}

	function patchTable(downloads) {
		var newVols = allVolumes(downloads);
		var volsChanged = JSON.stringify(newVols) !== JSON.stringify(colVols);
		var movKeysChanged = JSON.stringify(Object.keys(downloads).sort())
		                  !== JSON.stringify(Object.keys(prevData || {}).sort());
		if (volsChanged || movKeysChanged) {
			document.getElementById('dlMatrixContainer').innerHTML = buildTable(downloads);
			return;
		}
		Object.keys(downloads).forEach(function (mk) {
			var arr  = downloads[mk] || [];
			var prev = (prevData && prevData[mk]) ? prevData[mk] : [];
			colVols.forEach(function (v) {
				var n = arr[v] || 0, oldN = prev[v] || 0;
				if (n !== oldN) {
					var cell = document.querySelector('[data-mk="' + mk + '"][data-vol="' + v + '"]');
					if (cell) {
						cell.innerHTML = dlBadge(n);
						cell.classList.remove('dl-flash');
						void cell.offsetWidth;
						cell.classList.add('dl-flash');
					}
				}
			});
		});
	}

	/* ── Age indicator ── */
	function updateAge() {
		var el = document.getElementById('dlAge');
		if (!el) return;
		if (!fetchedAt) { el.textContent = 'updating\u2026'; return; }
		el.textContent = 'updated ' + Math.round((Date.now() - fetchedAt) / 1000) + 's ago';
	}

	/* ── Fetch & render ── */
	var _dlFails = 0, _dlRefIv, _dlAgeIv;
	function refresh() {
		$.getJSON('/do/datafile/moverdownloads/data')
			.done(function (data) {
				_dlFails = 0;
				fetchedAt = Date.now();
				var downloads = data.downloads || {};
				var container = document.getElementById('dlMatrixContainer');
				if (!container) return;
				syncMenu(downloads);
				if (Object.keys(downloads).length === 0) {
					container.innerHTML = '<div class="alert alert-info py-1 px-2 small mb-0">'
					                    + 'No download activity data available yet - waiting for first active transfer.</div>';
					prevData = null; colVols = null; return;
				}
				var view = applyFilter(downloads);
				if (!prevData || !document.getElementById('dlMatrix')) {
					container.innerHTML = buildTable(view);
				} else {
					patchTable(view);
				}
				prevData = view;
			})
			.fail(function () {
				if (++_dlFails >= 3) {
					clearInterval(_dlRefIv);
					clearInterval(_dlAgeIv);
					var el = document.getElementById('dlAge');
					if (el) {
						el.className = 'badge rounded-pill bg-warning-subtle text-warning-emphasis border text-nowrap';
						el.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-1"></i>Session expired'
							+ ' &mdash; <a href="#" onclick="location.reload();return false;">reload</a>';
					}
				}
			});
	}

	/* ── Global All / None ── */
	document.getElementById('dlSelectAll').addEventListener('click', function () {
		selectedMovers = null;
		prevData = null; colVols = null;
		rebuildMenu();
		updateDropdownLabel();
	});
	document.getElementById('dlSelectNone').addEventListener('click', function () {
		selectedMovers = {};
		prevData = null; colVols = null;
		rebuildMenu();
		updateDropdownLabel();
	});

	/* ── Resize: recalculate column widths ── */
	window.addEventListener('resize', function () { applyColWidths(); });

	refresh();
	_dlRefIv = setInterval(refresh, 1000);
	_dlAgeIv = setInterval(updateAge, 1000);
})();
</script>
