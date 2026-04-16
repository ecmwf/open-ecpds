<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select" />

<c:if test="${empty datatransfers}">
    <div class="alert alert-info d-flex align-items-center gap-2 mt-3" role="alert">
        <i class="bi bi-info-circle-fill"></i>
        <span>No transfer data for <strong>${destination.name}</strong> / <strong>${product}</strong> / <strong>${time}</strong> on <strong>${selectedDate}</strong>.</span>
    </div>
</c:if>

<c:if test="${not empty datatransfers}">

<script src="/assets/js/echarts.min.js"></script>

<style>
.ac-legend-item   { display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; margin-right:14px; }
.ac-legend-tick   { display:inline-block; width:3px; height:14px; border-radius:1px; flex-shrink:0; }
.ac-legend-bar    { display:inline-block; width:18px; height:2px; background:#888; vertical-align:middle; }
</style>

<%-- ── Embed arrival data ─────────────────────────────────────────────────── --%>
<script>
var _ar = [];
<c:forEach var="transfer" items="${datatransfers}">
<c:set var="acTarget"><c:out value="${transfer.target}" default=""/></c:set>
<c:set var="acStatus">0</c:set>
<c:set var="acEarliest">0</c:set>
<c:set var="acLatest">0</c:set>
<c:set var="acTargetT">0</c:set>
<c:set var="acPredicted">0</c:set>
<c:set var="acArrived">0</c:set>
<c:set var="acScheduled">0</c:set>
<c:set var="acTs">0</c:set>
<c:catch><c:if test="${transfer.arrivalStatus ge 0}"><c:set var="acStatus" value="${transfer.arrivalStatus}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.arrivalEarliestTime  != null}"><c:set var="acEarliest"  value="${transfer.arrivalEarliestTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.arrivalLatestTime    != null}"><c:set var="acLatest"    value="${transfer.arrivalLatestTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.arrivalTargetTime    != null}"><c:set var="acTargetT"   value="${transfer.arrivalTargetTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.arrivalPredictedTime != null}"><c:set var="acPredicted" value="${transfer.arrivalPredictedTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.scheduledTime        != null}"><c:set var="acScheduled" value="${transfer.scheduledTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.dataFile.arrivedTime != null}"><c:set var="acArrived"   value="${transfer.dataFile.arrivedTime.time}"/></c:if></c:catch>
<c:catch><c:set var="acTs" value="${transfer.dataFile.timeStep}"/></c:catch>
_ar.push({id:'${transfer.dataFileId}',target:'${acTarget}',status:${acStatus},earliest:${acEarliest},latest:${acLatest},targetT:${acTargetT},predicted:${acPredicted},arrived:${acArrived},scheduled:${acScheduled},ts:${acTs}});
</c:forEach>
</script>

<%-- ── Header ─────────────────────────────────────────────────────────────── --%>
<div class="d-flex justify-content-between align-items-center mt-2 mb-2 flex-wrap gap-2">
  <h6 class="fw-semibold text-secondary mb-0">
    <i class="bi bi-activity me-1"></i>Arrival Chart &mdash;
    <strong>${destination.name}</strong> &mdash; ${product} / ${time} &mdash; ${selectedDate}
  </h6>
  <div class="d-flex align-items-center gap-2 flex-wrap">
    <span class="text-muted" id="acStats" style="font-size:0.78rem;"></span>
    <div class="btn-group btn-group-sm" role="group">
      <button id="acBtnChart" type="button" class="btn btn-outline-secondary active" onclick="acSetView('chart')" title="Chart view">
        <i class="bi bi-activity"></i> Chart
      </button>
      <button id="acBtnTable" type="button" class="btn btn-outline-secondary" onclick="acSetView('table')" title="Table view">
        <i class="bi bi-table"></i> Table
      </button>
    </div>
    <div id="acZoomGroup" class="btn-group btn-group-sm d-none" role="group">
      <button type="button" class="btn btn-outline-secondary" onclick="_acZoomBy(0.75)" title="Zoom in"><i class="bi bi-zoom-in"></i></button>
      <button type="button" class="btn btn-outline-secondary" onclick="_acZoomBy(1/0.75)" title="Zoom out"><i class="bi bi-zoom-out"></i></button>
      <button id="acBtnResetZoom" type="button" class="btn btn-outline-secondary" onclick="_acResetZoom()" title="Reset zoom" disabled><i class="bi bi-arrows-fullscreen"></i></button>
    </div>
    <button id="acBtnExport" type="button" class="btn btn-sm btn-outline-secondary d-none" onclick="acExportCsv()" title="Export CSV">
      <i class="bi bi-download"></i> CSV
    </button>
  </div>
</div>

<%-- ── Legend ─────────────────────────────────────────────────────────────── --%>
<div id="acLegend" class="mb-2 d-flex flex-wrap align-items-center">
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#6c757d;"></span>S &mdash; Scheduled</span>
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#e91e8c;"></span>E &mdash; Earliest</span>
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#dc3545;"></span>L &mdash; Latest</span>
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#198754;"></span>T &mdash; Target</span>
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#fd7e14;"></span>P &mdash; Predicted</span>
  <span class="ac-legend-item"><span class="ac-legend-tick" style="background:#0d6efd;"></span>A &mdash; Actual arrival</span>
  <span class="ac-legend-item ms-3"><span class="ac-legend-bar"></span>&nbsp;E &rarr; L window</span>
</div>

<%-- ── Chart container ────────────────────────────────────────────────────── --%>
<div id="acChartView">
  <div id="acChart" style="width:100%; border:1px solid #dee2e6; border-radius:6px;"></div>
</div>
<div id="acZoomHint" class="text-muted mt-1 d-none" style="font-size:0.72rem;">
  <i class="bi bi-lightbulb"></i>
  Scroll to browse rows &nbsp;&middot;&nbsp; use +/- to zoom time axis &nbsp;&middot;&nbsp; drag to pan &nbsp;&middot;&nbsp; click a row to open the data file
</div>

<%-- ── Table container ────────────────────────────────────────────────────── --%>
<div id="acTableView" class="d-none">
  <div class="mb-2">
    <input type="text" id="acFilter" class="form-control form-control-sm"
           placeholder="Filter by filename…" oninput="acFilterTable()" style="max-width:360px;">
  </div>
  <div style="max-height:75vh; overflow-y:auto; border:1px solid #dee2e6; border-radius:6px;">
    <table class="table table-sm table-hover table-bordered mb-0" style="font-size:0.8rem;">
      <thead class="table-dark sticky-top">
        <tr>
          <th>#</th><th>File ID</th><th>Original</th><th>TS</th>
          <th title="Scheduled">S</th>
          <th title="Earliest">E</th>
          <th title="Latest">L</th>
          <th title="Target">T</th>
          <th title="Predicted">P</th>
          <th title="Actual arrival">A</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody id="acTableBody"></tbody>
    </table>
  </div>
  <div class="text-muted mt-1" style="font-size:0.75rem;" id="acTableCount"></div>
</div>

<script>
// ── Marker colours (S, E, L, T, P, A) ─────────────────────────────────────────
var _ACM  = ['#6c757d', '#e91e8c', '#dc3545', '#198754', '#fd7e14', '#0d6efd'];
var _ACML = ['S', 'E', 'L', 'T', 'P', 'A'];
var _ACMD = ['Scheduled', 'Earliest', 'Latest', 'Target', 'Predicted', 'Actual arrival'];

// Status badge colours (same scale as transfer, 0-6)
var _ACSBG  = ['#e2e3e5','#d1ecf1','#d4edda','#cce0ff','#fff3cd','#ffe5d0','#f8d7da'];
var _ACSTXT = ['#41464b','#0c5460','#155724','#084298','#856404','#5c2e00','#721c24'];
var _ACSLBL = ['unknown','scheduled','done','executing','warning','retrying','failed'];

// ── Format helpers ────────────────────────────────────────────────────────────
function _fmtAc(ms) {
  if (!ms || ms <= 0) return '--';
  var d = new Date(ms);
  return d.getHours().toString().padStart(2,'0')+':'+
         d.getMinutes().toString().padStart(2,'0')+':'+
         d.getSeconds().toString().padStart(2,'0');
}

function _acMarkers(r) {
  return [r.scheduled, r.earliest, r.latest, r.targetT, r.predicted, r.arrived];
}

// ── State ─────────────────────────────────────────────────────────────────────
var _acView    = localStorage.getItem('acView') || 'chart';
var _acChart   = null;
var _acOrigMin = null, _acOrigMax = null;
var _axMin = 0, _axMax = 0;

// ── View toggle ───────────────────────────────────────────────────────────────
function acSetView(v) {
  _acView = v;
  localStorage.setItem('acView', v);
  var isChart = (v === 'chart');
  document.getElementById('acChartView').classList.toggle('d-none', !isChart);
  document.getElementById('acTableView').classList.toggle('d-none',  isChart);
  document.getElementById('acLegend').classList.toggle('d-none',    !isChart);
  document.getElementById('acBtnExport').classList.toggle('d-none',  isChart);
  document.getElementById('acBtnChart').classList.toggle('active',   isChart);
  document.getElementById('acBtnTable').classList.toggle('active',  !isChart);
  var zg = document.getElementById('acZoomGroup');
  var zh = document.getElementById('acZoomHint');
  if (zg) zg.classList.toggle('d-none', !isChart);
  if (zh) zh.classList.toggle('d-none', !isChart || !_acChart);
  if (isChart) setTimeout(function() { _acEnsureChart(); if (_acChart) _acChart.resize(); }, 0);
}

// ── Zoom helpers ──────────────────────────────────────────────────────────────
function _acCurrentXRange() {
  if (!_acChart) return { min: _acOrigMin, max: _acOrigMax };
  var opt = _acChart.getOption();
  var dz  = opt.dataZoom && opt.dataZoom[0];
  if (dz && dz.startValue != null) return { min: dz.startValue, max: dz.endValue };
  if (dz && dz.start != null) {
    var r = _acOrigMax - _acOrigMin;
    return { min: _acOrigMin + r * dz.start / 100, max: _acOrigMin + r * dz.end / 100 };
  }
  return { min: _axMin, max: _axMax };
}
function _acApplyXZoom(mn, mx) {
  _axMin = mn; _axMax = mx;
  if (!_acChart) return;
  _acChart.dispatchAction({ type:'dataZoom', dataZoomIndex:0, startValue:mn, endValue:mx });
  var rb = document.getElementById('acBtnResetZoom');
  if (rb) rb.disabled = (Math.abs(mn - _acOrigMin) < 1 && Math.abs(mx - _acOrigMax) < 1);
}
function _acZoomBy(f) {
  var r = _acCurrentXRange();
  var nm = r.min + (r.max - r.min) * f;
  if (nm > _acOrigMax) nm = _acOrigMax;
  _acApplyXZoom(r.min < _acOrigMin ? _acOrigMin : r.min, nm);
}
function _acResetZoom() { _acApplyXZoom(_acOrigMin, _acOrigMax); }

// ── renderItem: draw S/E/L/T/P/A tick marks + E→L bar for each row ─────────
function _acRenderItem(params, api) {
  var rowIdx = api.value(0);
  var r      = _ar[rowIdx];
  if (!r) return;
  var cs     = params.coordSys;
  var ms     = _acMarkers(r);
  var ref    = ms[0] > 0 ? ms[0] : (ms[1] > 0 ? ms[1] : ms[2]);
  var yC     = api.coord([ref, rowIdx])[1];
  var tickH  = Math.max(10, Math.min(20, api.size([0, 1])[1] * 0.65));
  var children = [];

  // E→L horizontal range bar
  var eVal = ms[1], lVal = ms[2];
  if (eVal > 0 && lVal > 0) {
    var ex = api.coord([eVal, rowIdx])[0];
    var lx = api.coord([lVal, rowIdx])[0];
    var bar = echarts.graphic.clipRectByRect(
      { x: Math.min(ex, lx), y: yC - 1, width: Math.abs(lx - ex), height: 2 },
      { x: cs.x, y: cs.y, width: cs.width, height: cs.height }
    );
    if (bar) children.push({ type:'rect', shape: bar, style:{ fill:'#888' }, cursor:'default' });
  }

  // Tick marks and letter labels
  for (var k = 0; k < 6; k++) {
    var v = ms[k];
    if (v <= 0) continue;
    var px = api.coord([v, rowIdx])[0];
    var tick = echarts.graphic.clipRectByRect(
      { x: px - 1.5, y: yC - tickH / 2, width: 3, height: tickH },
      { x: cs.x, y: cs.y, width: cs.width, height: cs.height }
    );
    if (tick) children.push({
      type: 'rect', shape: tick,
      style: { fill: _ACM[k], opacity: 0.92 },
      cursor: 'pointer'
    });
    if (px >= cs.x && px <= cs.x + cs.width - 6) {
      children.push({
        type: 'text',
        style: { text: _ACML[k], x: px - 3, y: yC - tickH / 2 - 11, fill: _ACM[k], fontSize: 9, fontWeight: 'bold' },
        cursor: 'pointer'
      });
    }
  }

  return { type:'group', children: children };
}

// ── Chart init (lazy) ─────────────────────────────────────────────────────────
var _acInited = false;
function _acEnsureChart() {
  if (_acInited) return;
  _acInited = true;
  var n = _ar.length;
  if (n === 0 || typeof echarts === 'undefined') return;

  var minT = Infinity, maxT = -Infinity;
  _ar.forEach(function(r) {
    _acMarkers(r).forEach(function(v) {
      if (v > 0) { if (v < minT) minT = v; if (v > maxT) maxT = v; }
    });
  });
  var pad = Math.max((maxT - minT) * 0.05, 300000);
  _acOrigMin = minT - pad;
  _acOrigMax = maxT + pad;
  _axMin = _acOrigMin; _axMax = _acOrigMax;

  var rowH        = Math.max(20, Math.min(34, Math.floor(600 / Math.max(n, 1))));
  var chartH      = Math.max(260, Math.min(Math.round(window.innerHeight * 0.75), n * rowH + 80));
  var rowsVisible = Math.min(n, Math.max(8, Math.floor((chartH - 80) / rowH)));
  var el          = document.getElementById('acChart');
  el.style.height = chartH + 'px';

  _acChart = echarts.init(el);
  _acChart.setOption({
    animation: false,
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: function(p) {
        if (!p.value) return '';
        var r  = _ar[p.value[0]];
        if (!r) return '';
        var ms = _acMarkers(r);
        var sIdx = Math.max(0, Math.min(r.status, 6));
        var badge = '<span style="display:inline-block;padding:1px 7px;border-radius:4px;font-size:0.72rem;font-weight:600;background:' +
                    _ACSBG[sIdx] + ';color:' + _ACSTXT[sIdx] + ';">' + _ACSLBL[sIdx] + '</span>';
        var rows = '';
        for (var k = 0; k < 6; k++) {
          if (ms[k] > 0) {
            rows += '<tr><td style="color:' + _ACM[k] + ';font-weight:700;padding-right:8px;">' +
                    _ACML[k] + '</td><td>' + _ACMD[k] + '</td><td style="padding-left:10px;font-family:monospace;">' +
                    _fmtAc(ms[k]) + '</td></tr>';
          }
        }
        return '<div style="font-size:0.8rem;line-height:1.6;">' +
          '<b style="font-size:0.85rem;">' + r.target + '</b><br>' +
          badge + (r.ts > 0 ? ' &nbsp; <span style="color:#666;">TS: ' + r.ts + '</span>' : '') +
          '<table style="margin-top:4px;border-collapse:collapse;">' + rows + '</table>' +
          '<small style="color:#aaa;">Click to open data file</small>' +
          '</div>';
      }
    },
    grid: { left: 200, right: 38, top: 24, bottom: 50 },
    xAxis: {
      type: 'value',
      min: _acOrigMin,
      max: _acOrigMax,
      axisLabel: { formatter: _fmtAc, fontSize: 11 },
      name: 'Time (UTC)', nameLocation: 'middle', nameGap: 34,
      nameTextStyle: { fontSize: 11, color: '#888' },
      splitLine:      { show: true, lineStyle: { color: 'rgba(0,0,0,0.1)' } },
      minorTick:      { show: true, splitNumber: 4 },
      minorSplitLine: { show: true, lineStyle: { color: 'rgba(0,0,0,0.035)' } }
    },
    yAxis: {
      type: 'category',
      data: _ar.map(function(r) {
        var s = r.target; return s.length > 32 ? s.slice(0,29)+'…' : s;
      }),
      inverse: true,
      axisLabel: { fontSize: 10, width: 188, overflow: 'truncate', interval: 0 },
      splitLine: { show: false },
      splitArea: { show: true, areaStyle: { color: ['rgba(255,255,255,0)','rgba(0,0,0,0.022)'] } }
    },
    dataZoom: [
      { type:'inside',  xAxisIndex:0, zoomOnMouseWheel:false, moveOnMouseMove:true,  moveOnMouseWheel:false, filterMode:'weakFilter' },
      { type:'inside',  yAxisIndex:0, zoomOnMouseWheel:false, moveOnMouseMove:false, moveOnMouseWheel:true,  zoomLock:true, filterMode:'weakFilter', startValue:0, endValue:rowsVisible-1 },
      { type:'slider',  yAxisIndex:0, right:4, width:14, zoomLock:true, filterMode:'weakFilter', startValue:0, endValue:rowsVisible-1, brushSelect:false, showDetail:false }
    ],
    series: [{
      type: 'custom',
      renderItem: _acRenderItem,
      encode: { x: [1,2,3,4,5,6], y: 0 },
      data: _ar.map(function(r, i) {
        var m = _acMarkers(r);
        return [i, m[0], m[1], m[2], m[3], m[4], m[5]];
      }),
      clip: true
    }]
  });

  _acChart.on('click', function(p) {
    if (p.componentType === 'series' && p.value) {
      var r = _ar[p.value[0]];
      if (r) window.location.href = '/do/datafile/datafile/' + r.id;
    }
  });
  _acChart.on('datazoom', function() {
    var rng = _acCurrentXRange(); _axMin = rng.min; _axMax = rng.max;
    var rb = document.getElementById('acBtnResetZoom');
    if (rb) rb.disabled = (Math.abs(_axMin - _acOrigMin) < 1 && Math.abs(_axMax - _acOrigMax) < 1);
  });
  window.addEventListener('resize', function() { if (_acChart) _acChart.resize(); });

  document.getElementById('acZoomGroup').classList.remove('d-none');
  document.getElementById('acZoomHint').classList.remove('d-none');
}

// ── Table ─────────────────────────────────────────────────────────────────────
function acBuildTable() {
  var rows = [];
  _ar.forEach(function(r, i) {
    var m    = _acMarkers(r);
    var sIdx = Math.max(0, Math.min(r.status, 6));
    var badge = '<span style="display:inline-block;padding:1px 7px;border-radius:4px;font-size:0.72rem;font-weight:600;background:' +
                _ACSBG[sIdx] + ';color:' + _ACSTXT[sIdx] + ';">' + _ACSLBL[sIdx] + '</span>';
    var link  = '<a href="/do/datafile/datafile/' + r.id + '" style="font-family:monospace;font-size:0.78rem;">' + r.id + '</a>';
    var tds   = '';
    for (var k = 0; k < 6; k++) {
      tds += '<td style="white-space:nowrap;font-family:monospace;color:' + (m[k] > 0 ? _ACM[k] : '#aaa') + ';">' +
             _fmtAc(m[k]) + '</td>';
    }
    rows.push(
      '<tr data-search="' + r.target.toLowerCase() + '">' +
      '<td class="text-muted">' + (i+1) + '</td>' +
      '<td>' + link + '</td>' +
      '<td style="max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="' + r.target + '">' + r.target + '</td>' +
      '<td class="text-center">' + (r.ts > 0 ? r.ts : '--') + '</td>' +
      tds +
      '<td>' + badge + '</td>' +
      '</tr>'
    );
  });
  document.getElementById('acTableBody').innerHTML = rows.join('');
  _acUpdateCount();
}
function acFilterTable() {
  var q = document.getElementById('acFilter').value.toLowerCase();
  var vis = 0;
  document.getElementById('acTableBody').querySelectorAll('tr').forEach(function(row) {
    var show = !q || row.dataset.search.indexOf(q) >= 0;
    row.style.display = show ? '' : 'none';
    if (show) vis++;
  });
  _acUpdateCount(vis);
}
function _acUpdateCount(vis) {
  var total = _ar.length;
  if (vis === undefined) vis = total;
  document.getElementById('acTableCount').textContent =
    vis < total ? ('Showing ' + vis + ' of ' + total + ' transfers') : (total + ' transfers');
}

// ── CSV export ────────────────────────────────────────────────────────────────
function acExportCsv() {
  var lines = ['#,File ID,Target,TS,S (Scheduled),E (Earliest),L (Latest),T (Target),P (Predicted),A (Actual),Status'];
  _ar.forEach(function(r, i) {
    function q(v) { return '"' + String(v||'').replace(/"/g,'""') + '"'; }
    var m = _acMarkers(r);
    var sIdx = Math.max(0, Math.min(r.status, 6));
    lines.push([i+1, r.id, q(r.target), r.ts||'',
                _fmtAc(m[0]), _fmtAc(m[1]), _fmtAc(m[2]), _fmtAc(m[3]), _fmtAc(m[4]), _fmtAc(m[5]),
                _ACSLBL[sIdx]].join(','));
  });
  var a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([lines.join('\n')], {type:'text/csv'}));
  a.download = 'arrival_${destination.name}_${product}_${time}_${selectedDate}.csv';
  a.click();
}

// ── Boot ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', _acInit);
if (document.readyState !== 'loading') { _acInit(); }
function _acInit() {
  var total = _ar.length, done = 0, exec = 0, fail = 0;
  _ar.forEach(function(r) {
    if (r.status === 2) done++;
    else if (r.status === 3) exec++;
    else if (r.status === 6) fail++;
  });
  var parts = [total + ' transfers'];
  if (done > 0) parts.push(done + ' done');
  if (exec > 0) parts.push(exec + ' executing');
  if (fail > 0) parts.push(fail + ' failed');
  document.getElementById('acStats').textContent = parts.join(' | ');

  acBuildTable();
  acSetView(_acView);
}
</script>

</c:if><%-- not empty datatransfers --%>
