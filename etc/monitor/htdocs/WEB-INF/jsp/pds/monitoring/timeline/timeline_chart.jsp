<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>
<auth:if basePathKey="destination.basepath" paths="/${destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
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

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<tiles:insert name="date.select"/>

<c:if test="${empty datatransfers}">
  <div class="alert alert-info d-flex align-items-center gap-2 mt-3" role="alert">
    <i class="bi bi-info-circle-fill"></i>
    <span>No timeline available for destination <strong>${destination.name}</strong> on <strong>${selectedDate}</strong>.</span>
  </div>
</c:if>

<c:if test="${not empty datatransfers}">

<script src="/assets/js/echarts.min.js"></script>

<style>
.tl-legend-item { display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; margin-right:12px; }
.tl-legend-swatch { width:13px; height:13px; border-radius:3px; flex-shrink:0; }
</style>

<%-- Embed all transfer data as a JS array --%>
<script>
var _dt = [];
var _dtBase = '<bean:message key="datatransfer.basepath"/>';
<c:forEach var="t" items="${datatransfers}">
<c:set var="tlStart"  value="${t.startTime  != null ? t.startTime.time  : t.scheduledTime.time}"/>
<c:set var="tlEnd"    value="${t.finishTime != null ? t.finishTime.time  : -1}"/>
<c:set var="tlInProg" value="${t.finishTime == null && t.startTime != null}"/>
<c:set var="tlTarget"><c:out value="${t.target}"/></c:set>
<c:set var="tlHost"><c:out value="${t.hostName}"/></c:set>
<%-- getDataFile() throws TransferException -- wrap in its own catch --%>
<c:set var="tlLabel" value="${tlTarget}"/>
<c:catch>
<c:set var="tlLabel">${t.dataFile.metaTime}-${t.dataFile.metaStream}-${t.dataFile.metaType}</c:set>
</c:catch>
_dt.push({
  id:       '${t.id}',
  label:    '${tlLabel}',
  target:   '${tlTarget}',
  start:    ${tlStart},
  end:      ${tlEnd},
  inProg:   ${tlInProg},
  status:   '${t.statusCode}',
  priority: ${t.priority},
  host:     '${tlHost}',
  size:     ${t.size}
});
</c:forEach>
</script>

<%-- Header with toggle --%>
<div class="d-flex justify-content-between align-items-center mt-2 mb-2 flex-wrap gap-2">
  <h6 class="fw-semibold text-secondary mb-0">
    <i class="bi bi-clock-history me-1"></i>Transfer Timeline &mdash;
    <strong>${destination.name}</strong> &mdash; ${selectedDate}
  </h6>
  <div class="d-flex align-items-center gap-2 flex-wrap">
    <span class="text-muted" style="font-size:0.78rem;" id="tlStats"></span>
    <div class="btn-group btn-group-sm" role="group">
      <button id="tlBtnChart" type="button" class="btn btn-outline-secondary active" onclick="tlSetView('chart')" title="Chart view">
        <i class="bi bi-bar-chart-horizontal"></i> Chart
      </button>
      <button id="tlBtnTable" type="button" class="btn btn-outline-secondary" onclick="tlSetView('table')" title="Table view">
        <i class="bi bi-table"></i> Table
      </button>
    </div>
    <div id="tlZoomGroup" class="btn-group btn-group-sm d-none" role="group">
      <button type="button" class="btn btn-outline-secondary" onclick="_tlZoomBy(0.75)" title="Zoom in">
        <i class="bi bi-zoom-in"></i>
      </button>
      <button type="button" class="btn btn-outline-secondary" onclick="_tlZoomBy(1/0.75)" title="Zoom out">
        <i class="bi bi-zoom-out"></i>
      </button>
      <button id="tlBtnResetZoom" type="button" class="btn btn-outline-secondary" onclick="_tlResetZoom()" title="Reset zoom" disabled>
        <i class="bi bi-arrows-fullscreen"></i>
      </button>
    </div>
    <button id="tlBtnExport" type="button" class="btn btn-sm btn-outline-secondary d-none" onclick="tlExportCsv()" title="Export as CSV">
      <i class="bi bi-download"></i> CSV
    </button>
  </div>
</div>

<%-- Legend (chart view only) --%>
<div id="tlLegend" class="mb-2 d-flex flex-wrap align-items-center">
  <span class="tl-legend-item"><span class="tl-legend-swatch" style="background:#198754;"></span>Done</span>
  <span class="tl-legend-item"><span class="tl-legend-swatch" style="background:#0d6efd;"></span>Executing</span>
  <span class="tl-legend-item"><span class="tl-legend-swatch" style="background:#dc3545;"></span>Stopped</span>
  <span class="tl-legend-item"><span class="tl-legend-swatch" style="background:#fd7e14;"></span>Retrying</span>
  <span class="tl-legend-item"><span class="tl-legend-swatch" style="background:#6c757d;"></span>Other</span>
</div>

<%-- ECharts container --%>
<div id="tlChartView" class="d-none">
  <div id="tlChart" style="width:100%; border:1px solid #dee2e6; border-radius:6px;"></div>
</div>

<%-- Tip (shown once chart is ready) --%>
<div id="tlZoomHint" class="text-muted mt-1 d-none" style="font-size:0.72rem;">
  <i class="bi bi-lightbulb"></i>
  Tip: drag to pan &nbsp;&middot;&nbsp; scroll to browse rows &nbsp;&middot;&nbsp; use +/- to zoom the time axis &nbsp;&middot;&nbsp; click a bar to open the transfer
</div>

<%-- Table container --%>
<div id="tlTableView" class="d-none">
  <div class="mb-2">
    <input type="text" id="tlFilter" class="form-control form-control-sm" placeholder="Filter by target, label, status, host..." oninput="tlFilterTable()" style="max-width:380px;">
  </div>
  <div style="max-height:75vh; overflow-y:auto; border:1px solid #dee2e6; border-radius:6px;">
    <table class="table table-sm table-hover table-bordered mb-0" style="font-size:0.8rem;">
      <thead class="table-dark sticky-top">
        <tr>
          <th>#</th>
          <th>ID</th>
          <th>Label</th>
          <th>Target</th>
          <th>Status</th>
          <th>Start</th>
          <th>End</th>
          <th>Duration</th>
          <th>Size</th>
          <th>Host</th>
          <th>Priority</th>
        </tr>
      </thead>
      <tbody id="tlTableBody"></tbody>
    </table>
  </div>
  <div class="text-muted mt-1" style="font-size:0.75rem;" id="tlTableCount"></div>
</div>

<script>
// ---- Colour helpers -------------------------------------------------------
var _SC    = { DONE:'#198754', EXEC:'#0d6efd', STOP:'#dc3545', RETR:'#fd7e14' };
var _SCBG  = { DONE:'#d1e7dd', EXEC:'#cfe2ff', STOP:'#f8d7da', RETR:'#ffe5d0' };
var _SCTXT = { DONE:'#0a3622', EXEC:'#084298', STOP:'#58151c', RETR:'#5c2e00' };
function _sc(s)    { return _SC[s]    || '#6c757d'; }
function _scbg(s)  { return _SCBG[s]  || '#e2e3e5'; }
function _sctxt(s) { return _SCTXT[s] || '#41464b'; }

// ---- Format helpers -------------------------------------------------------
function _fmtSize(b) {
  if (b >= 1e12) return (b/1e12).toFixed(2)+' TB';
  if (b >= 1e9)  return (b/1e9).toFixed(2)+' GB';
  if (b >= 1e6)  return (b/1e6).toFixed(2)+' MB';
  if (b >= 1e3)  return (b/1e3).toFixed(2)+' KB';
  return b > 0 ? b+' B' : '--';
}
function _fmtMs(ms) {
  if (!ms || ms <= 0) return '--';
  var d = new Date(ms);
  return d.getHours().toString().padStart(2,'0')+':'+
         d.getMinutes().toString().padStart(2,'0')+':'+
         d.getSeconds().toString().padStart(2,'0');
}
function _fmtDur(ms) {
  if (ms <= 0)      return '--';
  if (ms < 1000)    return ms+'ms';
  if (ms < 60000)   return (ms/1000).toFixed(1)+'s';
  if (ms < 3600000) return Math.floor(ms/60000)+'m '+Math.floor((ms%60000)/1000)+'s';
  return Math.floor(ms/3600000)+'h '+Math.floor((ms%3600000)/60000)+'m';
}

// ---- State ----------------------------------------------------------------
var _tlView    = localStorage.getItem('tlView') || 'chart';
var _tlEChart  = null;  // ECharts instance
var _tlOrigMin = null;
var _tlOrigMax = null;
var _xMin      = 0;
var _xMax      = 0;

// ---- View toggle ----------------------------------------------------------
function tlSetView(v) {
  _tlView = v;
  localStorage.setItem('tlView', v);
  var isChart = (v === 'chart');
  document.getElementById('tlChartView').classList.toggle('d-none', !isChart);
  document.getElementById('tlTableView').classList.toggle('d-none',  isChart);
  document.getElementById('tlLegend').classList.toggle('d-none',    !isChart);
  document.getElementById('tlBtnExport').classList.toggle('d-none',  isChart);
  document.getElementById('tlBtnChart').classList.toggle('active',   isChart);
  document.getElementById('tlBtnTable').classList.toggle('active',  !isChart);
  var zg = document.getElementById('tlZoomGroup');
  var zh = document.getElementById('tlZoomHint');
  if (zg) zg.classList.toggle('d-none', !isChart || !_tlEChart);
  if (zh) zh.classList.toggle('d-none', !isChart || !_tlEChart);
  if (isChart) {
    // Initialise ECharts lazily (first time chart view is shown)
    setTimeout(function() { _tlEnsureChart(); if (_tlEChart) _tlEChart.resize(); }, 0);
  }
}

// ---- Zoom helpers ---------------------------------------------------------
function _tlCurrentXRange() {
  if (!_tlEChart) return { min: _tlOrigMin, max: _tlOrigMax };
  var opt = _tlEChart.getOption();
  var dz  = opt.dataZoom && opt.dataZoom[0];
  if (!dz) return { min: _xMin, max: _xMax };
  if (dz.startValue != null && dz.endValue != null) {
    return { min: dz.startValue, max: dz.endValue };
  }
  if (dz.start != null) {
    var r = _tlOrigMax - _tlOrigMin;
    return { min: _tlOrigMin + r * dz.start / 100, max: _tlOrigMin + r * dz.end / 100 };
  }
  return { min: _xMin, max: _xMax };
}

function _tlApplyXZoom(newMin, newMax) {
  _xMin = newMin; _xMax = newMax;
  if (!_tlEChart) return;
  _tlEChart.dispatchAction({ type: 'dataZoom', dataZoomIndex: 0, startValue: newMin, endValue: newMax });
  var rb = document.getElementById('tlBtnResetZoom');
  if (rb) rb.disabled = (Math.abs(newMin - _tlOrigMin) < 1 && Math.abs(newMax - _tlOrigMax) < 1);
}

function _tlZoomBy(factor) {
  var r = _tlCurrentXRange();
  var newRange = (r.max - r.min) * factor;
  var newMax   = r.min + newRange;  // keep left edge fixed
  if (newMax > _tlOrigMax) newMax = _tlOrigMax;
  if (r.min < _tlOrigMin) r.min = _tlOrigMin;
  _tlApplyXZoom(r.min, newMax);
}

function _tlResetZoom() { _tlApplyXZoom(_tlOrigMin, _tlOrigMax); }

// ---- Table ----------------------------------------------------------------
function tlBuildTable() {
  var now = Date.now();
  var tb  = document.getElementById('tlTableBody');
  var rows = [];
  _dt.forEach(function(t, i) {
    var end  = t.end >= 0 ? t.end : (t.inProg ? now : -1);
    var dur  = (end > 0 && t.start > 0) ? end - t.start : -1;
    var badge = '<span style="display:inline-block;padding:1px 6px;border-radius:4px;font-size:0.72rem;font-weight:600;background:'+_scbg(t.status)+';color:'+_sctxt(t.status)+';">'+t.status+'</span>';
    var link  = '<a href="'+_dtBase+'/'+t.id+'" style="font-family:monospace;font-size:0.78rem;">'+t.id+'</a>';
    var endTxt = t.end >= 0 ? _fmtMs(t.end) : (t.inProg ? '<em>running</em>' : '--');
    rows.push(
      '<tr data-search="'+(t.label+' '+t.target+' '+t.status+' '+(t.host||'')).toLowerCase()+'">' +
      '<td class="text-muted">'+(i+1)+'</td>' +
      '<td>'+link+'</td>' +
      '<td style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="'+t.label+'">'+t.label+'</td>' +
      '<td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="'+t.target+'">'+t.target+'</td>' +
      '<td>'+badge+'</td>' +
      '<td style="white-space:nowrap;">'+_fmtMs(t.start)+'</td>' +
      '<td style="white-space:nowrap;">'+endTxt+'</td>' +
      '<td style="white-space:nowrap;">'+_fmtDur(dur)+'</td>' +
      '<td style="white-space:nowrap;">'+_fmtSize(t.size)+'</td>' +
      '<td style="max-width:120px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="'+(t.host||'')+'">'+( t.host || '--' )+'</td>' +
      '<td class="text-center">'+t.priority+'</td>' +
      '</tr>'
    );
  });
  tb.innerHTML = rows.join('');
  tlUpdateCount();
}

function tlFilterTable() {
  var q    = document.getElementById('tlFilter').value.toLowerCase();
  var rows = document.getElementById('tlTableBody').querySelectorAll('tr');
  var vis  = 0;
  rows.forEach(function(r) {
    var match = !q || r.dataset.search.indexOf(q) >= 0;
    r.style.display = match ? '' : 'none';
    if (match) vis++;
  });
  tlUpdateCount(vis);
}

function tlUpdateCount(vis) {
  var total = _dt.length;
  if (vis === undefined) vis = total;
  document.getElementById('tlTableCount').textContent =
    vis < total ? ('Showing '+vis+' of '+total+' transfers') : (total+' transfers');
}

// ---- CSV export -----------------------------------------------------------
function tlExportCsv() {
  var now   = Date.now();
  var lines = ['#,ID,Label,Target,Status,Start,End,Duration (s),Size (bytes),Host,Priority'];
  _dt.forEach(function(t, i) {
    var end    = t.end >= 0 ? t.end : (t.inProg ? now : -1);
    var dur    = (end > 0 && t.start > 0) ? ((end - t.start)/1000).toFixed(1) : '';
    var endStr = t.end >= 0 ? _fmtMs(t.end) : (t.inProg ? 'running' : '');
    function q(v) { return '"'+String(v||'').replace(/"/g,'""')+'"'; }
    lines.push([i+1, t.id, q(t.label), q(t.target), t.status,
                _fmtMs(t.start), endStr, dur, t.size>0?t.size:'', q(t.host||''), t.priority].join(','));
  });
  var blob = new Blob([lines.join('\n')], {type:'text/csv'});
  var a    = document.createElement('a');
  a.href   = URL.createObjectURL(blob);
  a.download = 'timeline_${destination.name}_${selectedDate}.csv';
  a.click();
}

// ---- ECharts renderItem ---------------------------------------------------
// Each data item: [startMs, endMs, rowIndex]
function _tlRenderItem(params, api) {
  var rowIdx  = api.value(2);
  var t       = _dt[rowIdx];
  if (!t) return;

  var s  = api.coord([api.value(0), rowIdx]);
  var e  = api.coord([api.value(1), rowIdx]);
  var h  = Math.max(api.size([0, 1])[1] * 0.72, 4);
  var cs = params.coordSys;

  // Clip bar to chart area (prevents stubs outside range)
  var rect = echarts.graphic.clipRectByRect(
    { x: s[0], y: s[1] - h/2, width: Math.max(e[0] - s[0], 2), height: h },
    { x: cs.x, y: cs.y, width: cs.width, height: cs.height }
  );
  if (!rect) return;

  var color  = _sc(t.status);
  var now2   = Date.now();
  var durMs  = (t.end >= 0 ? t.end : now2) - t.start;
  var durX   = rect.x + rect.width + 4;
  var durFs  = Math.max(8, Math.min(11, Math.floor(h * 0.65)));

  var children = [{
    type: 'rect',
    shape: rect,
    style: { fill: color + 'cc', stroke: color, lineWidth: 1 },
    cursor: 'pointer',
    emphasis: { style: { fill: color, opacity: 1 } }
  }];

  // Duration label after the bar (only if it fits and the row is tall enough to read)
  if (durX < cs.x + cs.width - 10 && h >= 10) {
    children.push({
      type: 'text',
      style: {
        text: _fmtDur(durMs),
        x: durX, y: s[1],
        textAlign: 'left', textVerticalAlign: 'middle',
        font: 'normal ' + durFs + 'px sans-serif',
        fill: 'rgba(70,70,70,0.85)'
      },
      cursor: 'default'
    });
  }

  return { type: 'group', children: children };
}

// ---- Chart init (lazy) ----------------------------------------------------
var _tlInited = false;

function _tlEnsureChart() {
  if (_tlInited) return;
  _tlInited = true;

  var now = Date.now();
  var n   = _dt.length;
  if (n === 0 || typeof echarts === 'undefined') return;

  // Time bounds
  var minT = Infinity, maxT = -Infinity;
  _dt.forEach(function(t) {
    if (t.start < minT) minT = t.start;
    var e = t.end >= 0 ? t.end : now;
    if (e > maxT) maxT = e;
  });
  var pad     = Math.max((maxT - minT) * 0.02, 60000);
  _tlOrigMin  = minT - pad;
  _tlOrigMax  = maxT + pad;
  _xMin       = _tlOrigMin;
  _xMax       = _tlOrigMax;

  // Chart height: fit rows up to 75 vh
  var rowH    = Math.max(18, Math.min(32, Math.floor(600 / Math.max(n, 1))));
  var chartH  = Math.max(260, Math.min(Math.round(window.innerHeight * 0.75), n * rowH + 80));
  var chartEl = document.getElementById('tlChart');
  chartEl.style.height = chartH + 'px';

  // Rows visible in initial window
  var rowsVisible = Math.min(n, Math.max(10, Math.floor((chartH - 80) / rowH)));

  _tlEChart = echarts.init(chartEl);

  _tlEChart.setOption({
    animation: false,
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: function(params) {
        if (!params.value) return '';
        var t   = _dt[params.value[2]];
        if (!t) return '';
        var end = t.end >= 0 ? t.end : Date.now();
        var dur = end - t.start;
        var tgt = (t.target || t.label).replace(/&/g,'&amp;').replace(/</g,'&lt;');
        return '<div style="font-size:0.8rem;line-height:1.6;">' +
          '<b>' + tgt + '</b><br>' +
          'Label: <span style="color:#555;">' + t.label + '</span><br>' +
          'Status: <b style="color:' + _sc(t.status) + '">' + t.status + '</b><br>' +
          'Start: ' + _fmtMs(t.start) + '<br>' +
          (t.end >= 0 ? 'End: ' + _fmtMs(t.end) : 'End: <em>running</em>') + '<br>' +
          'Duration: ' + _fmtDur(dur) + '<br>' +
          (t.size > 0 ? 'Size: ' + _fmtSize(t.size) + '<br>' : '') +
          (t.host   ? 'Host: '  + t.host     + '<br>' : '') +
          'Priority: ' + t.priority +
          '<br><small style="color:#aaa;">Click to open</small></div>';
      }
    },
    grid: { left: 115, right: 38, top: 16, bottom: 52 },
    xAxis: {
      type: 'value',
      min: _tlOrigMin,
      max: _tlOrigMax,
      axisLabel: {
        formatter: function(v) { return _fmtMs(v); },
        fontSize: 11
      },
      name: 'Time (UTC)',
      nameLocation: 'middle',
      nameGap: 36,
      nameTextStyle: { fontSize: 11, color: '#888' },
      splitLine: {
        show: true,
        lineStyle: { color: 'rgba(0,0,0,0.15)' }
      },
      minorTick: { show: true, splitNumber: 6 },
      minorSplitLine: {
        show: true,
        lineStyle: { color: 'rgba(0,0,0,0.05)' }
      }
    },
    yAxis: {
      type: 'category',
      data: _dt.map(function(t) { return t.label; }),
      inverse: true,
      axisLabel: {
        fontSize: 10,
        width: 104,
        overflow: 'truncate',
        interval: 0
      },
      splitLine: { show: false },
      splitArea: {
        show: true,
        areaStyle: { color: ['rgba(255,255,255,0)', 'rgba(0,0,0,0.025)'] }
      }
    },
    dataZoom: [
      // X: drag to pan; zoom only via buttons
      {
        type: 'inside',
        xAxisIndex: 0,
        zoomOnMouseWheel: false,
        moveOnMouseMove: true,
        moveOnMouseWheel: false,
        filterMode: 'weakFilter'
      },
      // Y: mouse-wheel to scroll rows (locked window size -- plain scroll, no zoom)
      {
        type: 'inside',
        yAxisIndex: 0,
        zoomOnMouseWheel: false,
        moveOnMouseMove: false,
        moveOnMouseWheel: true,
        zoomLock: true,
        filterMode: 'weakFilter',
        startValue: 0,
        endValue: rowsVisible - 1
      },
      // Y: slider on right (locked -- drag handle scrolls, cannot resize window)
      {
        type: 'slider',
        yAxisIndex: 0,
        right: 4,
        width: 14,
        zoomLock: true,
        filterMode: 'weakFilter',
        startValue: 0,
        endValue: rowsVisible - 1,
        brushSelect: false,
        showDetail: false
      }
    ],
    series: [{
      type: 'custom',
      renderItem: _tlRenderItem,
      encode: { x: [0, 1], y: 2 },
      data: _dt.map(function(t, i) {
        return [t.start, t.end >= 0 ? t.end : now, i];
      }),
      clip: true
    }]
  });

  // Click bar to open transfer page
  _tlEChart.on('click', function(params) {
    if (params.componentType === 'series' && params.value) {
      var t = _dt[params.value[2]];
      if (t) window.location.href = _dtBase + '/' + t.id;
    }
  });

  // Sync Reset button when user pans the X axis
  _tlEChart.on('datazoom', function() {
    var r  = _tlCurrentXRange();
    _xMin  = r.min;
    _xMax  = r.max;
    var rb = document.getElementById('tlBtnResetZoom');
    if (rb) rb.disabled = (Math.abs(_xMin - _tlOrigMin) < 1 && Math.abs(_xMax - _tlOrigMax) < 1);
  });

  // Resize on window resize
  window.addEventListener('resize', function() { if (_tlEChart) _tlEChart.resize(); });

  // Reveal controls
  document.getElementById('tlZoomGroup').classList.remove('d-none');
  document.getElementById('tlZoomHint').classList.remove('d-none');
}

// ---- Boot -----------------------------------------------------------------
document.addEventListener('DOMContentLoaded', _tlInit);
if (document.readyState !== 'loading') { _tlInit(); }
function _tlInit() {
  var now = Date.now();
  var n   = _dt.length;

  // Stats bar
  var nDone = 0, nExec = 0, nStop = 0, nRetr = 0;
  _dt.forEach(function(t) {
    if      (t.status==='DONE') nDone++;
    else if (t.status==='EXEC') nExec++;
    else if (t.status==='STOP') nStop++;
    else if (t.status==='RETR') nRetr++;
  });
  var parts = [n+' transfers', nDone+' done'];
  if (nExec > 0) parts.push(nExec+' running');
  if (nStop > 0) parts.push(nStop+' stopped');
  if (nRetr > 0) parts.push(nRetr+' retrying');
  document.getElementById('tlStats').textContent = parts.join(' | ');

  if (n === 0) return;

  tlBuildTable();
  tlSetView(_tlView); // kicks off lazy chart init if needed
}
</script>

</c:if>

</c:if>
