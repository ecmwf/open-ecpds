<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />
<auth:if basePathKey="destination.basepath" paths="/">
    <auth:then><c:set var="authorized" value="true" /></auth:then>
</auth:if>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then></auth:then>
    <auth:else>
        <auth:if basePathKey="destination.basepath" paths="/${destination.name}">
            <auth:then><c:set var="authorized" value="true" /></auth:then>
        </auth:if>
    </auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
    <div class="alert alert-danger d-flex align-items-center gap-2 mt-3" role="alert">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <span>Not authorised to view destination <strong>${destination.name}</strong>.</span>
    </div>
</c:if>

<c:if test="${authorized == 'true'}">

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
.tc-legend-item   { display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; margin-right:14px; }
.tc-legend-tick   { display:inline-block; width:3px; height:14px; border-radius:1px; flex-shrink:0; }
.tc-legend-bar    { display:inline-block; width:18px; height:2px; background:#888; vertical-align:middle; }
</style>

<%-- -- Embed transfer data ------------------------------------------------- --%>
<script>
var _tr = [];
<c:forEach var="transfer" items="${datatransfers}">
<c:set var="tcTarget"><c:out value="${transfer.target}" default=""/></c:set>
<c:set var="tcFmt">unknown</c:set>
<c:catch><c:set var="tcFmt"><c:out value="${transfer.formattedStatus}" default="unknown"/></c:set></c:catch>
<c:set var="tcStatus">0</c:set>
<c:set var="tcEarliest">0</c:set>
<c:set var="tcLatest">0</c:set>
<c:set var="tcTargetT">0</c:set>
<c:set var="tcPredicted">0</c:set>
<c:set var="tcFinish">0</c:set>
<c:set var="tcScheduled">0</c:set>
<c:set var="tcTs">0</c:set>
<c:catch><c:if test="${transfer.transferStatus ge 0}"><c:set var="tcStatus" value="${transfer.transferStatus}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.transferEarliestTime != null}"><c:set var="tcEarliest" value="${transfer.transferEarliestTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.transferLatestTime  != null}"><c:set var="tcLatest"   value="${transfer.transferLatestTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.transferTargetTime  != null}"><c:set var="tcTargetT"  value="${transfer.transferTargetTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.transferPredictedTime != null}"><c:set var="tcPredicted" value="${transfer.transferPredictedTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.finishTime   != null}"><c:set var="tcFinish"    value="${transfer.finishTime.time}"/></c:if></c:catch>
<c:catch><c:if test="${transfer.scheduledTime != null}"><c:set var="tcScheduled" value="${transfer.scheduledTime.time}"/></c:if></c:catch>
<c:catch><c:set var="tcTs" value="${transfer.dataFile.timeStep}"/></c:catch>
_tr.push({id:'${transfer.id}',target:'${tcTarget}',status:${tcStatus},fmtStatus:'${tcFmt}',earliest:${tcEarliest},latest:${tcLatest},targetT:${tcTargetT},predicted:${tcPredicted},finish:${tcFinish},scheduled:${tcScheduled},ts:${tcTs}});
</c:forEach>
</script>

<%-- -- Header --------------------------------------------------------------- --%>
<div class="d-flex justify-content-between align-items-center mt-2 mb-2 flex-wrap gap-2">
  <h6 class="fw-semibold text-secondary mb-0">
    <i class="bi bi-activity me-1"></i>Transfer Chart &mdash;
    <strong>${destination.name}</strong> &mdash; ${product} / ${time} &mdash; ${selectedDate}
  </h6>
  <div class="d-flex align-items-center gap-2 flex-wrap">
    <span class="text-muted" id="tcStats" style="font-size:0.78rem;"></span>
    <div class="btn-group btn-group-sm" role="group">
      <button id="tcBtnChart" type="button" class="btn btn-outline-secondary active" onclick="tcSetView('chart')" title="Chart view">
        <i class="bi bi-activity"></i> Chart
      </button>
      <button id="tcBtnTable" type="button" class="btn btn-outline-secondary" onclick="tcSetView('table')" title="Table view">
        <i class="bi bi-table"></i> Table
      </button>
    </div>
    <div id="tcZoomGroup" class="btn-group btn-group-sm d-none" role="group">
      <button type="button" class="btn btn-outline-secondary" onclick="_tcZoomBy(0.75)" title="Zoom in"><i class="bi bi-zoom-in"></i></button>
      <button type="button" class="btn btn-outline-secondary" onclick="_tcZoomBy(1/0.75)" title="Zoom out"><i class="bi bi-zoom-out"></i></button>
      <button id="tcBtnResetZoom" type="button" class="btn btn-outline-secondary" onclick="_tcResetZoom()" title="Reset zoom" disabled><i class="bi bi-arrows-fullscreen"></i></button>
    </div>
    <button id="tcBtnExport" type="button" class="btn btn-sm btn-outline-secondary d-none" onclick="tcExportCsv()" title="Export CSV">
      <i class="bi bi-download"></i> CSV
    </button>
  </div>
</div>

<%-- -- Legend --------------------------------------------------------------- --%>
<div id="tcLegend" class="mb-2 d-flex flex-wrap align-items-center">
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#6c757d;"></span>S &mdash; Scheduled</span>
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#e91e8c;"></span>E &mdash; Earliest</span>
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#dc3545;"></span>L &mdash; Latest</span>
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#198754;"></span>T &mdash; Target</span>
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#fd7e14;"></span>P &mdash; Predicted</span>
  <span class="tc-legend-item"><span class="tc-legend-tick" style="background:#0d6efd;"></span>A &mdash; Actual</span>
  <span class="tc-legend-item ms-3"><span class="tc-legend-bar"></span>&nbsp;E &rarr; L window</span>
</div>

<%-- -- Chart container ------------------------------------------------------ --%>
<div id="tcChartView">
  <div id="tcChart" style="width:100%; border:1px solid #dee2e6; border-radius:6px;"></div>
</div>
<div id="tcZoomHint" class="text-muted mt-1 d-none" style="font-size:0.72rem;">
  <i class="bi bi-lightbulb"></i>
  Scroll to browse rows &nbsp;&middot;&nbsp; use +/- to zoom time axis &nbsp;&middot;&nbsp; drag to pan &nbsp;&middot;&nbsp; click a row to open the transfer
</div>

<%-- -- Table container ------------------------------------------------------ --%>
<div id="tcTableView" class="d-none">
  <div class="mb-2">
    <input type="text" id="tcFilter" class="form-control form-control-sm"
           placeholder="Filter by filename, status..." oninput="tcFilterTable()" style="max-width:360px;">
  </div>
  <div style="max-height:75vh; overflow-y:auto; border:1px solid #dee2e6; border-radius:6px;">
    <table class="table table-sm table-hover table-bordered mb-0" style="font-size:0.8rem;">
      <thead class="table-dark sticky-top">
        <tr>
          <th>#</th><th>ID</th><th>Original</th><th>TS</th>
          <th title="Scheduled">S</th>
          <th title="Earliest">E</th>
          <th title="Latest">L</th>
          <th title="Target">T</th>
          <th title="Predicted">P</th>
          <th title="Actual (finish)">A</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody id="tcTableBody"></tbody>
    </table>
  </div>
  <div class="text-muted mt-1" style="font-size:0.75rem;" id="tcTableCount"></div>
</div>

<script>
// -- Marker colours (S, E, L, T, P, A) -----------------------------------------
var _TCM = ['#6c757d', '#e91e8c', '#dc3545', '#198754', '#fd7e14', '#0d6efd'];
var _TCML = ['S', 'E', 'L', 'T', 'P', 'A'];
var _TCMD = ['Scheduled', 'Earliest', 'Latest', 'Target', 'Predicted', 'Actual'];

// Transfer status colours (for badge in tooltip/table)
var _TCS  = ['#adb5bd','#17a2b8','#28a745','#4040d0','#d4a000','#e07800','#dc3545'];
var _TCSBG  = ['#e2e3e5','#d1ecf1','#d4edda','#cce0ff','#fff3cd','#ffe5d0','#f8d7da'];
var _TCSTXT = ['#41464b','#0c5460','#155724','#084298','#856404','#5c2e00','#721c24'];

// -- Format helpers ------------------------------------------------------------
function _fmtMs(ms) {
  if (!ms || ms <= 0) return '--';
  var d = new Date(ms);
  return d.getHours().toString().padStart(2,'0')+':'+
         d.getMinutes().toString().padStart(2,'0')+':'+
         d.getSeconds().toString().padStart(2,'0');
}

// Get the 6 marker timestamps for a transfer as array [s, e, l, t, p, a]
function _markers(t) {
  return [t.scheduled, t.earliest, t.latest, t.targetT, t.predicted, t.finish];
}

// -- State ---------------------------------------------------------------------
var _tcView    = localStorage.getItem('tcView') || 'chart';
var _tcChart   = null;
var _tcOrigMin = null, _tcOrigMax = null;
var _xMin = 0, _xMax = 0;

// -- View toggle ---------------------------------------------------------------
function tcSetView(v) {
  _tcView = v;
  localStorage.setItem('tcView', v);
  var isChart = (v === 'chart');
  document.getElementById('tcChartView').classList.toggle('d-none', !isChart);
  document.getElementById('tcTableView').classList.toggle('d-none',  isChart);
  document.getElementById('tcLegend').classList.toggle('d-none',    !isChart);
  document.getElementById('tcBtnExport').classList.toggle('d-none',  isChart);
  document.getElementById('tcBtnChart').classList.toggle('active',   isChart);
  document.getElementById('tcBtnTable').classList.toggle('active',  !isChart);
  var zg = document.getElementById('tcZoomGroup');
  var zh = document.getElementById('tcZoomHint');
  if (zg) zg.classList.toggle('d-none', !isChart);
  if (zh) zh.classList.toggle('d-none', !isChart || !_tcChart);
  if (isChart) setTimeout(function() { _tcEnsureChart(); if (_tcChart) _tcChart.resize(); }, 0);
}

// -- Zoom helpers --------------------------------------------------------------
function _tcCurrentXRange() {
  if (!_tcChart) return { min: _tcOrigMin, max: _tcOrigMax };
  var opt = _tcChart.getOption();
  var dz  = opt.dataZoom && opt.dataZoom[0];
  if (dz && dz.startValue != null) return { min: dz.startValue, max: dz.endValue };
  if (dz && dz.start != null) {
    var r = _tcOrigMax - _tcOrigMin;
    return { min: _tcOrigMin + r * dz.start / 100, max: _tcOrigMin + r * dz.end / 100 };
  }
  return { min: _xMin, max: _xMax };
}
function _tcApplyXZoom(mn, mx) {
  _xMin = mn; _xMax = mx;
  if (!_tcChart) return;
  _tcChart.dispatchAction({ type:'dataZoom', dataZoomIndex:0, startValue:mn, endValue:mx });
  var rb = document.getElementById('tcBtnResetZoom');
  if (rb) rb.disabled = (Math.abs(mn - _tcOrigMin) < 1 && Math.abs(mx - _tcOrigMax) < 1);
}
function _tcZoomBy(f) {
  var r = _tcCurrentXRange();
  var nm = r.min + (r.max - r.min) * f;
  if (nm > _tcOrigMax) nm = _tcOrigMax;
  _tcApplyXZoom(r.min < _tcOrigMin ? _tcOrigMin : r.min, nm);
}
function _tcResetZoom() { _tcApplyXZoom(_tcOrigMin, _tcOrigMax); }

// -- renderItem: draw S/E/L/T/P/A tick marks + E->L bar for each row ---------
function _tcRenderItem(params, api) {
  var rowIdx = api.value(0);
  var t      = _tr[rowIdx];
  if (!t) return;
  var cs     = params.coordSys;
  var ms     = _markers(t);
  var yC     = api.coord([ms[0] > 0 ? ms[0] : ms[1], rowIdx])[1];
  var tickH  = Math.max(10, Math.min(20, api.size([0, 1])[1] * 0.65));
  var children = [];

  // E->L horizontal range bar
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

  // Tick marks for each marker
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
      style: { fill: _TCM[k], opacity: 0.92 },
      cursor: 'pointer'
    });
    // Letter label above tick (only if it fits)
    if (px >= cs.x && px <= cs.x + cs.width - 6) {
      children.push({
        type: 'text',
        style: { text: _TCML[k], x: px - 3, y: yC - tickH / 2 - 11, fill: _TCM[k], fontSize: 9, fontWeight: 'bold' },
        cursor: 'pointer'
      });
    }
  }

  return { type:'group', children: children };
}

// -- Chart init (lazy) ---------------------------------------------------------
var _tcInited = false;
function _tcEnsureChart() {
  if (_tcInited) return;
  _tcInited = true;
  var n = _tr.length;
  if (n === 0 || typeof echarts === 'undefined') return;

  // Compute time range across all markers
  var minT = Infinity, maxT = -Infinity;
  _tr.forEach(function(t) {
    _markers(t).forEach(function(v) {
      if (v > 0) { if (v < minT) minT = v; if (v > maxT) maxT = v; }
    });
  });
  var pad = Math.max((maxT - minT) * 0.05, 300000); // 5 min min padding
  _tcOrigMin = minT - pad;
  _tcOrigMax = maxT + pad;
  _xMin = _tcOrigMin; _xMax = _tcOrigMax;

  // Chart height: ~20px per row, 80px overhead, capped at 75vh
  var rowH        = Math.max(20, Math.min(34, Math.floor(600 / Math.max(n, 1))));
  var chartH      = Math.max(260, Math.min(Math.round(window.innerHeight * 0.75), n * rowH + 80));
  var rowsVisible = Math.min(n, Math.max(8, Math.floor((chartH - 80) / rowH)));
  var el          = document.getElementById('tcChart');
  el.style.height = chartH + 'px';

  _tcChart = echarts.init(el);
  _tcChart.setOption({
    animation: false,
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: function(p) {
        if (!p.value) return '';
        var t  = _tr[p.value[0]];
        if (!t) return '';
        var ms = _markers(t);
        var sIdx = Math.max(0, Math.min(t.status, 6));
        var badge = '<span style="display:inline-block;padding:1px 7px;border-radius:4px;font-size:0.72rem;font-weight:600;background:' +
                    _TCSBG[sIdx] + ';color:' + _TCSTXT[sIdx] + ';">' + t.fmtStatus + '</span>';
        var rows = '';
        for (var k = 0; k < 6; k++) {
          if (ms[k] > 0) {
            rows += '<tr><td style="color:' + _TCM[k] + ';font-weight:700;padding-right:8px;">' +
                    _TCML[k] + '</td><td>' + _TCMD[k] + '</td><td style="padding-left:10px;font-family:monospace;">' +
                    _fmtMs(ms[k]) + '</td></tr>';
          }
        }
        return '<div style="font-size:0.8rem;line-height:1.6;">' +
          '<b style="font-size:0.85rem;">' + t.target + '</b><br>' +
          badge + (t.ts > 0 ? ' &nbsp; <span style="color:#666;">TS: ' + t.ts + '</span>' : '') +
          '<table style="margin-top:4px;border-collapse:collapse;">' + rows + '</table>' +
          '<small style="color:#aaa;">Click to open transfer</small>' +
          '</div>';
      }
    },
    grid: { left: 200, right: 38, top: 24, bottom: 50 },
    xAxis: {
      type: 'value',
      min: _tcOrigMin,
      max: _tcOrigMax,
      axisLabel: { formatter: _fmtMs, fontSize: 11 },
      name: 'Time (UTC)', nameLocation: 'middle', nameGap: 34,
      nameTextStyle: { fontSize: 11, color: '#888' },
      splitLine:      { show: true, lineStyle: { color: 'rgba(0,0,0,0.1)' } },
      minorTick:      { show: true, splitNumber: 4 },
      minorSplitLine: { show: true, lineStyle: { color: 'rgba(0,0,0,0.035)' } }
    },
    yAxis: {
      type: 'category',
      data: _tr.map(function(t) {
        var s = t.target; return s.length > 32 ? s.slice(0,29)+'...' : s;
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
      renderItem: _tcRenderItem,
      encode: { x: [1,2,3,4,5,6], y: 0 },
      data: _tr.map(function(t, i) {
        var m = _markers(t);
        return [i, m[0], m[1], m[2], m[3], m[4], m[5]];
      }),
      clip: true
    }]
  });

  _tcChart.on('click', function(p) {
    if (p.componentType === 'series' && p.value) {
      var t = _tr[p.value[0]];
      if (t) window.location.href = '/do/transfer/data/' + t.id;
    }
  });
  _tcChart.on('datazoom', function() {
    var r = _tcCurrentXRange(); _xMin = r.min; _xMax = r.max;
    var rb = document.getElementById('tcBtnResetZoom');
    if (rb) rb.disabled = (Math.abs(_xMin - _tcOrigMin) < 1 && Math.abs(_xMax - _tcOrigMax) < 1);
  });
  window.addEventListener('resize', function() { if (_tcChart) _tcChart.resize(); });

  document.getElementById('tcZoomGroup').classList.remove('d-none');
  document.getElementById('tcZoomHint').classList.remove('d-none');
}

// -- Table ---------------------------------------------------------------------
function tcBuildTable() {
  var rows = [];
  _tr.forEach(function(t, i) {
    var m    = _markers(t);
    var sIdx = Math.max(0, Math.min(t.status, 6));
    var badge = '<span style="display:inline-block;padding:1px 7px;border-radius:4px;font-size:0.72rem;font-weight:600;background:' +
                _TCSBG[sIdx] + ';color:' + _TCSTXT[sIdx] + ';">' + t.fmtStatus + '</span>';
    var link  = '<a href="/do/transfer/data/' + t.id + '" style="font-family:monospace;font-size:0.78rem;">' + t.id + '</a>';
    var tds   = '';
    for (var k = 0; k < 6; k++) {
      tds += '<td style="white-space:nowrap;font-family:monospace;color:' + (m[k] > 0 ? _TCM[k] : '#aaa') + ';">' +
             _fmtMs(m[k]) + '</td>';
    }
    rows.push(
      '<tr data-search="' + (t.target + ' ' + t.fmtStatus).toLowerCase() + '">' +
      '<td class="text-muted">' + (i+1) + '</td>' +
      '<td>' + link + '</td>' +
      '<td style="max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="' + t.target + '">' + t.target + '</td>' +
      '<td class="text-center">' + (t.ts > 0 ? t.ts : '--') + '</td>' +
      tds +
      '<td>' + badge + '</td>' +
      '</tr>'
    );
  });
  document.getElementById('tcTableBody').innerHTML = rows.join('');
  _tcUpdateCount();
}
function tcFilterTable() {
  var q = document.getElementById('tcFilter').value.toLowerCase();
  var vis = 0;
  document.getElementById('tcTableBody').querySelectorAll('tr').forEach(function(r) {
    var show = !q || r.dataset.search.indexOf(q) >= 0;
    r.style.display = show ? '' : 'none';
    if (show) vis++;
  });
  _tcUpdateCount(vis);
}
function _tcUpdateCount(vis) {
  var total = _tr.length;
  if (vis === undefined) vis = total;
  document.getElementById('tcTableCount').textContent =
    vis < total ? ('Showing ' + vis + ' of ' + total + ' transfers') : (total + ' transfers');
}

// -- CSV export ----------------------------------------------------------------
function tcExportCsv() {
  var lines = ['#,ID,Target,TS,S (Scheduled),E (Earliest),L (Latest),T (Target),P (Predicted),A (Actual),Status'];
  _tr.forEach(function(t, i) {
    function q(v) { return '"' + String(v||'').replace(/"/g,'""') + '"'; }
    var m = _markers(t);
    lines.push([i+1, t.id, q(t.target), t.ts||'',
                _fmtMs(m[0]), _fmtMs(m[1]), _fmtMs(m[2]), _fmtMs(m[3]), _fmtMs(m[4]), _fmtMs(m[5]),
                t.fmtStatus].join(','));
  });
  var a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([lines.join('\n')], {type:'text/csv'}));
  a.download = 'transfer_${destination.name}_${product}_${time}_${selectedDate}.csv';
  a.click();
}

// -- Boot ----------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', _tcInit);
if (document.readyState !== 'loading') { _tcInit(); }
function _tcInit() {
  var total = _tr.length, done = 0, exec = 0, fail = 0;
  _tr.forEach(function(t) {
    if (t.status === 2) done++;
    else if (t.status === 3) exec++;
    else if (t.status === 6) fail++;
  });
  var parts = [total + ' transfers'];
  if (done > 0) parts.push(done + ' done');
  if (exec > 0) parts.push(exec + ' executing');
  if (fail > 0) parts.push(fail + ' failed');
  document.getElementById('tcStats').textContent = parts.join(' | ');

  tcBuildTable();
  tcSetView(_tcView);
}
</script>

</c:if><%-- not empty datatransfers --%>
</c:if><%-- authorized --%>
