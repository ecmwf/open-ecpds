<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%-- Header: title + info button --%>
<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
  <i class="bi bi-graph-up-arrow text-primary flex-shrink-0"></i>
  <span>
    <strong>Data Portal Traffic</strong><c:if test="${not empty portalTrafficUser}"> &mdash; <c:out value="${portalTrafficUser}"/></c:if>
    <c:if test="${empty portalTrafficUser}"> - aggregated portal connections and transfer rates across all users</c:if>
  </span>
  <button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
      data-bs-toggle="collapse" data-bs-target="#ptInfoPanel"
      aria-expanded="false" title="About this page">
    <i class="bi bi-info-circle"></i>
  </button>
</div>

<%-- Info panel --%>
<div class="collapse mb-3" id="ptInfoPanel">
  <div class="card-body py-2 px-3 border rounded" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <c:choose>
      <c:when test="${not empty portalTrafficUser}">
        <strong class="d-block mb-1">Data Portal Traffic &mdash; user <c:out value="${portalTrafficUser}"/></strong>
        <p class="mb-1">Daily Data Portal activity for user <strong><c:out value="${portalTrafficUser}"/></strong>. Periods with fewer connections and lower transfer rates indicate lower contention on the portal.</p>
        <ul class="mb-1 ps-3">
          <li><strong>Connections</strong> &mdash; number of sessions authenticated by this user each day.</li>
          <li><strong>Uploaded / Downloaded</strong> &mdash; total bytes transferred in each direction.</li>
          <li><strong>Upload / Download Rate</strong> &mdash; average Mbps (bytes &divide; cumulated transfer duration) for this user's sessions.</li>
          <li><strong>Granularity</strong> &mdash; today's activity appears immediately from live minute-buckets; past days are aggregated nightly and kept indefinitely.</li>
        </ul>
      </c:when>
      <c:otherwise>
        <strong class="d-block mb-1">Data Portal Traffic &mdash; all users</strong>
        <p class="mb-1">Aggregated daily Data Portal traffic across all incoming users.</p>
        <ul class="mb-1 ps-3">
          <li><strong>Connections</strong> &mdash; total sessions authenticated across all users each day.</li>
          <li><strong>Uploaded / Downloaded</strong> &mdash; total bytes transferred in each direction.</li>
          <li><strong>Upload / Download Rate</strong> &mdash; average Mbps computed from total bytes and cumulated transfer duration across all sessions.</li>
          <li><strong>Granularity</strong> &mdash; today's data is shown from live minute-buckets; historical data is aggregated nightly and kept indefinitely.</li>
        </ul>
      </c:otherwise>
    </c:choose>
    <strong class="d-block mb-1 mt-2">Controls</strong>
    <ul class="mb-0 ps-3">
      <li><strong>Period</strong> &mdash; use the <kbd>30d</kbd> / <kbd>90d</kbd> / <kbd>180d</kbd> / <kbd>1y</kbd> / <kbd>All</kbd> buttons to restrict the visible data. The filter applies to the summary stat cards, the chart, and the table simultaneously.</li>
      <li><strong>Table / Chart</strong> &mdash; switch between the bar/line chart view and a sortable, paginated table of daily figures.</li>
      <li><strong>Sort order</strong> (&uarr;&darr; button) &mdash; toggle between latest-first and earliest-first. Applies to both the chart and the table.</li>
    </ul>
  </div>
</div>

<c:if test="${not empty portalTrafficError}">
  <div class="alert alert-danger mt-3">Failed to load portal traffic data: <strong><c:out value="${portalTrafficError}"/></strong></div>
</c:if>

<c:if test="${empty portalTrafficList}">
  <div class="alert alert-info mt-3">No portal traffic data available yet.</div>
</c:if>

<c:if test="${not empty portalTrafficList}">
<script src="/assets/js/chart.umd.min.js"></script>
<script>
const ptTimes = [], ptConnections = [], ptBytesIn = [], ptBytesOut = [], ptDurationIn = [], ptDurationOut = [];
<c:forEach var="pt" items="${portalTrafficList}">
ptTimes.push(('<c:out value="${pt.time}"/>').substring(0,10));
ptConnections.push(${pt.connections});
ptBytesIn.push(${pt.bytesIn});
ptBytesOut.push(${pt.bytesOut});
ptDurationIn.push(${pt.durationIn});
ptDurationOut.push(${pt.durationOut});
</c:forEach>
// Arrays remain in ascending (earliest-first) order; _ptReversed controls table display

function ptRate(bytes, ms) { return ms > 0 ? (bytes * 8 / ms / 1000) : 0; }
function ptFmtBytes(b) {
  if (b >= 1e12) return (b/1e12).toFixed(2)+' TB';
  if (b >= 1e9)  return (b/1e9).toFixed(2)+' GB';
  if (b >= 1e6)  return (b/1e6).toFixed(2)+' MB';
  if (b >= 1e3)  return (b/1e3).toFixed(1)+' KB';
  return b+' B';
}
function ptFmtRate(r) { return r.toFixed(3)+' Mbps'; }

const ptRateIn  = ptBytesIn.map((v,i)  => ptRate(v, ptDurationIn[i]));
const ptRateOut = ptBytesOut.map((v,i) => ptRate(v, ptDurationOut[i]));

// Summary stats
const ptTotalIn  = ptBytesIn.reduce((a,b)=>a+b,0);
const ptTotalOut = ptBytesOut.reduce((a,b)=>a+b,0);
const ptTotalConn = ptConnections.reduce((a,b)=>a+b,0);
const ptPeakIn   = Math.max(...ptRateIn);
const ptPeakOut  = Math.max(...ptRateOut);

var _ptChartConn = null, _ptChartBw = null, _ptObsTheme = null, _ptPeriod = 0;

function ptGetThemeColors() {
  var s = getComputedStyle(document.documentElement);
  return {
    bodyColor:   (s.getPropertyValue('--bs-body-color')  ||'').trim()||'#212529',
    borderColor: (s.getPropertyValue('--bs-border-color')||'').trim()||'#dee2e6'
  };
}

function ptGetPeriodIndices() {
  if (_ptPeriod <= 0) return ptTimes.map(function(_, i) { return i; });
  var cutoff = new Date();
  cutoff.setDate(cutoff.getDate() - _ptPeriod);
  var cutStr = cutoff.toISOString().substring(0, 10);
  var out = [];
  for (var i = 0; i < ptTimes.length; i++) { if (ptTimes[i] >= cutStr) out.push(i); }
  return out;
}

function ptSetPeriod(days) {
  _ptPeriod = days;
  document.querySelectorAll('#ptPeriodSelector .btn').forEach(function(b) {
    b.classList.toggle('active', parseInt(b.getAttribute('data-days'), 10) === days);
  });
  ptUpdatePeriodLabel();
  ptUpdateStats();
  _ptPage = 0;
  if (document.getElementById('ptTableView').style.display !== 'none') ptBuildTable();
  if (document.getElementById('ptChartView').style.display !== 'none') ptBuildCharts();
  try { localStorage.setItem('ptPeriod', days); } catch(e) {}
}

function ptUpdatePeriodLabel() {
  var label = document.getElementById('ptPeriodLabel');
  if (!label) return;
  var idx = ptGetPeriodIndices();
  if (!idx.length) { label.textContent = ''; label.style.display = 'none'; return; }
  label.textContent = idx.length + ' day' + (idx.length === 1 ? '' : 's') +
    (_ptPeriod > 0 ? ' (' + ptTimes[idx[0]] + ' \u2013 ' + ptTimes[idx[idx.length-1]] + ')' : '');
  label.style.display = '';
}

function ptUpdateStats() {
  var idx = ptGetPeriodIndices();
  var totalConn = 0, totalIn = 0, totalOut = 0, peakIn = 0, peakOut = 0;
  idx.forEach(function(i) {
    totalConn += ptConnections[i]; totalIn += ptBytesIn[i]; totalOut += ptBytesOut[i];
    if (ptRateIn[i]  > peakIn)  peakIn  = ptRateIn[i];
    if (ptRateOut[i] > peakOut) peakOut = ptRateOut[i];
  });
  document.getElementById('ptStatConn').textContent    = totalConn.toLocaleString();
  document.getElementById('ptStatIn').textContent      = ptFmtBytes(totalIn);
  document.getElementById('ptStatOut').textContent     = ptFmtBytes(totalOut);
  document.getElementById('ptStatPeakIn').textContent  = ptFmtRate(peakIn);
  document.getElementById('ptStatPeakOut').textContent = ptFmtRate(peakOut);
}

function ptBuildCharts() {
  if (_ptChartConn) { _ptChartConn.destroy(); _ptChartConn = null; }
  if (_ptChartBw)   { _ptChartBw.destroy();   _ptChartBw   = null; }
  var idx = ptGetPeriodIndices();
  if (_ptReversed) idx = idx.slice().reverse();
  var labels = idx.map(function(i) { return ptTimes[i]; });
  var conn   = idx.map(function(i) { return ptConnections[i]; });
  var rIn    = idx.map(function(i) { return ptRateIn[i]; });
  var rOut   = idx.map(function(i) { return ptRateOut[i]; });
  var theme = ptGetThemeColors();
  _ptChartConn = new Chart(document.getElementById('ptConnChart'), {
    type: 'bar',
    data: { labels: labels, datasets: [{
      label: 'Connections', data: conn,
      backgroundColor: 'rgba(13,110,253,0.55)', borderColor: '#0d6efd', borderWidth: 1
    }]},
    options: { responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{labels:{color:theme.bodyColor}} },
      scales:{ x:{ticks:{color:theme.bodyColor},grid:{color:theme.borderColor}},
               y:{beginAtZero:true,ticks:{precision:0,color:theme.bodyColor},grid:{color:theme.borderColor}} }}
  });
  _ptChartBw = new Chart(document.getElementById('ptBwChart'), {
    type: 'line',
    data: { labels: labels, datasets: [
      { label:'Upload (Mbps)',  data:rIn,  borderColor:'#fd7e14', backgroundColor:'rgba(253,126,20,0.08)', tension:0.25, pointRadius:2, fill:true },
      { label:'Download (Mbps)',data:rOut, borderColor:'#198754', backgroundColor:'rgba(25,135,84,0.08)',  tension:0.25, pointRadius:2, fill:true }
    ]},
    options: { responsive:true, maintainAspectRatio:false, interaction:{mode:'index',intersect:false},
      plugins:{ legend:{labels:{color:theme.bodyColor}} },
      scales:{ x:{ticks:{color:theme.bodyColor},grid:{color:theme.borderColor}},
               y:{beginAtZero:true,title:{display:true,text:'Mbps',color:theme.bodyColor},
                  ticks:{color:theme.bodyColor},grid:{color:theme.borderColor}} }}
  });
}

function ptSetView(v) {
  document.getElementById('ptTableView').style.display = v==='table' ? '' : 'none';
  document.getElementById('ptChartView').style.display = v==='chart' ? '' : 'none';
  document.getElementById('ptBtnTable').classList.toggle('active', v==='table');
  document.getElementById('ptBtnChart').classList.toggle('active', v==='chart');
  if (v==='chart') ptBuildCharts();
  if (v==='table') ptBuildTable();
  try { localStorage.setItem('ptView','portalTraffic_'+v); } catch(e){}
}

  document.addEventListener('DOMContentLoaded', function() {
  // Restore reversed preference (default: true = latest first)
  try { var r = localStorage.getItem('ptReversed'); if (r !== null) _ptReversed = (r === '1'); } catch(e) {}
  // Restore period preference (default: 0 = all)
  try { var p = parseInt(localStorage.getItem('ptPeriod'), 10); if ([30,90,180,365,0].indexOf(p) >= 0) {
    _ptPeriod = p;
    document.querySelectorAll('#ptPeriodSelector .btn').forEach(function(b) {
      b.classList.toggle('active', parseInt(b.getAttribute('data-days'), 10) === p);
    });
  }} catch(e) {}
  // Populate summary cards (period-aware)
  ptUpdateStats();
  ptUpdatePeriodLabel();

  // Restore page size preference
  try { var ps = parseInt(localStorage.getItem('ptPageSize'),10); if([10,25,50,100,250].indexOf(ps)>=0){ _ptPageSize=ps; document.getElementById('ptPageSizeSelect').value=ps; } } catch(e){}

  // Restore saved view preference (default: chart)
  var saved = 'chart';
  try { saved = (localStorage.getItem('ptView')||'').replace('portalTraffic_','')||'chart'; } catch(e){}
  ptSetView(saved);
  ptApplyReverseBtn();

  // Rebuild charts on theme toggle
  new MutationObserver(function() {
    var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
    if (t === _ptObsTheme) return;
    _ptObsTheme = t;
    if (_ptChartConn || _ptChartBw) ptBuildCharts();
  }).observe(document.documentElement, { attributes:true, attributeFilter:['data-bs-theme'] });
});
</script>

<%-- Header bar: period selector (left) + view toggle (right) --%>
<div class="d-flex align-items-center mb-3 flex-wrap gap-2">
  <span class="text-muted" style="font-size:0.82rem;">Period:</span>
  <div class="btn-group btn-group-sm" id="ptPeriodSelector">
    <button class="btn btn-outline-secondary" data-days="30"  onclick="ptSetPeriod(30)">30d</button>
    <button class="btn btn-outline-secondary" data-days="90"  onclick="ptSetPeriod(90)">90d</button>
    <button class="btn btn-outline-secondary" data-days="180" onclick="ptSetPeriod(180)">180d</button>
    <button class="btn btn-outline-secondary" data-days="365" onclick="ptSetPeriod(365)">1y</button>
    <button class="btn btn-outline-secondary active" data-days="0" onclick="ptSetPeriod(0)">All</button>
  </div>
  <span id="ptPeriodLabel" class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border" style="display:none;font-size:0.75rem;font-weight:500;"></span>
  <div class="ms-auto d-flex align-items-center gap-1">
    <button type="button" class="btn btn-sm btn-outline-secondary" id="ptBtnTable" onclick="ptSetView('table')">
      <i class="bi bi-table me-1"></i>Table
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary" id="ptBtnChart" onclick="ptSetView('chart')">
      <i class="bi bi-bar-chart-fill me-1"></i>Chart
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary active" id="ptBtnReverse"
        onclick="ptToggleOrder()" title="Showing latest first — click to show earliest first">
      <i class="bi bi-sort-up-alt" id="ptBtnReverseIcon"></i>
    </button>
  </div>
</div>

<%-- Summary stat cards --%>
<div class="d-flex flex-wrap gap-3 mb-4">
  <div class="traffic-stat-card" style="background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:0.75rem 1.25rem;flex:1;min-width:120px;">
    <div style="font-size:0.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:0.04em;"><i class="bi bi-person-fill-up me-1"></i>Total Connections</div>
    <div style="font-size:1.1rem;font-weight:700;" id="ptStatConn">&mdash;</div>
  </div>
  <div class="traffic-stat-card" style="background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:0.75rem 1.25rem;flex:1;min-width:120px;">
    <div style="font-size:0.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:0.04em;"><i class="bi bi-cloud-upload me-1"></i>Total Uploaded</div>
    <div style="font-size:1.1rem;font-weight:700;" id="ptStatIn">&mdash;</div>
  </div>
  <div class="traffic-stat-card" style="background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:0.75rem 1.25rem;flex:1;min-width:120px;">
    <div style="font-size:0.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:0.04em;"><i class="bi bi-cloud-download me-1"></i>Total Downloaded</div>
    <div style="font-size:1.1rem;font-weight:700;" id="ptStatOut">&mdash;</div>
  </div>
  <div class="traffic-stat-card" style="background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:0.75rem 1.25rem;flex:1;min-width:120px;">
    <div style="font-size:0.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:0.04em;"><i class="bi bi-trophy-fill me-1 text-warning"></i>Peak Upload</div>
    <div style="font-size:1.1rem;font-weight:700;" id="ptStatPeakIn">&mdash;</div>
  </div>
  <div class="traffic-stat-card" style="background:var(--bs-tertiary-bg);border:1px solid var(--bs-border-color);border-radius:8px;padding:0.75rem 1.25rem;flex:1;min-width:120px;">
    <div style="font-size:0.7rem;font-weight:600;text-transform:uppercase;color:var(--bs-secondary-color);letter-spacing:0.04em;"><i class="bi bi-trophy-fill me-1 text-success"></i>Peak Download</div>
    <div style="font-size:1.1rem;font-weight:700;" id="ptStatPeakOut">&mdash;</div>
  </div>
</div>

<%-- CHART VIEW --%>
<div id="ptChartView">
  <div class="row g-3 mb-4">
    <div class="col-12">
      <div class="p-2 border rounded" style="position:relative; height:240px;">
        <canvas id="ptConnChart"></canvas>
      </div>
    </div>
    <div class="col-12">
      <div class="p-2 border rounded" style="position:relative; height:280px;">
        <canvas id="ptBwChart"></canvas>
      </div>
    </div>
  </div>
</div>

<%-- TABLE VIEW --%>
<div id="ptTableView" style="display:none;">
  <div class="card border-0 shadow-sm">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
      <i class="bi bi-table text-primary"></i>
      <span class="fw-semibold">Daily Portal Traffic</span>
      <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
          <span class="input-group-text"><i class="bi bi-search"></i></span>
          <input type="search" id="ptSearch" class="form-control"
                 placeholder="Filter by date..." oninput="ptSetSearch(this.value)" style="min-width:160px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
          <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
          <select id="ptPageSizeSelect" class="form-select form-select-sm" style="width:auto"
                  onchange="ptSetPageSize(this.value)">
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
            <option value="100">100</option>
            <option value="250">250</option>
          </select>
        </div>
      </div>
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-sm table-striped table-hover table-bordered align-middle mb-0"
               id="ptDataTable" style="font-size:0.82rem; white-space:nowrap;">
          <thead class="table-primary">
            <tr>
              <th onclick="ptSortTable(0)" style="cursor:pointer;" data-order="desc">Date <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
              <th onclick="ptSortTable(1)" style="cursor:pointer;" data-order="asc">Connections <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
              <th onclick="ptSortTable(2)" style="cursor:pointer;" data-order="asc">Uploaded <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
              <th onclick="ptSortTable(3)" style="cursor:pointer;" data-order="asc">Downloaded <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
              <th onclick="ptSortTable(4)" style="cursor:pointer;" data-order="asc">Upload Rate <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
              <th onclick="ptSortTable(5)" style="cursor:pointer;" data-order="asc">Download Rate <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
            </tr>
          </thead>
          <tbody id="ptTableBody"></tbody>
        </table>
      </div>
    </div>
    <div class="card-footer d-flex justify-content-between align-items-center flex-wrap gap-2 py-2"
         style="background:var(--bs-secondary-bg)">
      <span id="ptPaginatorInfo" class="text-muted" style="font-size:0.82rem;"></span>
      <nav aria-label="Portal traffic pages">
        <ul class="pagination pagination-sm mb-0" id="ptPaginatorPages"></ul>
      </nav>
    </div>
  </div>
</div>

<script>
var _ptSortedIdx = null, _ptPage = 0, _ptPageSize = 25, _ptSearch = '', _ptReversed = true;

function ptGetFiltered() {
  var periodIdx = ptGetPeriodIndices();
  var base;
  if (_ptSortedIdx) {
    var periodSet = new Set(periodIdx);
    base = _ptSortedIdx.filter(function(i) { return periodSet.has(i); });
  } else {
    base = periodIdx.slice();
    if (_ptReversed) base = base.slice().reverse();
  }
  if (!_ptSearch) return base;
  var t = _ptSearch.toLowerCase();
  return base.filter(function(i){ return ptTimes[i].indexOf(t) !== -1; });
}

function ptBuildTable() {
  var rows = ptGetFiltered(), total = rows.length;
  var pageRows = (_ptPageSize > 0) ? rows.slice(_ptPage * _ptPageSize, (_ptPage+1) * _ptPageSize) : rows;
  var html = '';
  pageRows.forEach(function(i) {
    var ri = ptRateIn[i], ro = ptRateOut[i];
    html += '<tr>' +
      '<td>' + ptTimes[i] + '</td>' +
      '<td class="text-end">' + ptConnections[i].toLocaleString() + '</td>' +
      '<td class="text-end" title="' + ptBytesIn[i] + ' bytes">' + ptFmtBytes(ptBytesIn[i]) + '</td>' +
      '<td class="text-end" title="' + ptBytesOut[i] + ' bytes">' + ptFmtBytes(ptBytesOut[i]) + '</td>' +
      '<td class="text-end">' + ptFmtRate(ri) + '</td>' +
      '<td class="text-end">' + ptFmtRate(ro) + '</td>' +
      '</tr>';
  });
  document.getElementById('ptTableBody').innerHTML = html ||
    '<tr><td colspan="6" class="text-center text-muted fst-italic">No matching rows.</td></tr>';
  ptBuildPaginator(total);
}

function ptBuildPaginator(total) {
  var pages = (_ptPageSize > 0) ? Math.ceil(total / _ptPageSize) : 1;
  var start = (_ptPageSize > 0) ? _ptPage * _ptPageSize + 1 : 1;
  var end   = (_ptPageSize > 0) ? Math.min((_ptPage+1) * _ptPageSize, total) : total;
  document.getElementById('ptPaginatorInfo').textContent =
    total === 0 ? '' : 'Showing ' + start + '-' + end + ' of ' + total.toLocaleString() + ' rows';
  var html = '';
  if (pages > 1) {
    html += '<li class="page-item' + (_ptPage===0?' disabled':'') + '">' +
            '<a class="page-link" href="#" onclick="ptGoPage(' + (_ptPage-1) + ');return false;">&lsaquo;</a></li>';
    ptPageRange(_ptPage, pages).forEach(function(p) {
      if (p===-1) { html += '<li class="page-item disabled"><span class="page-link">&hellip;</span></li>'; }
      else { html += '<li class="page-item' + (p===_ptPage?' active':'') + '">' +
        '<a class="page-link" href="#" onclick="ptGoPage(' + p + ');return false;">' + (p+1) + '</a></li>'; }
    });
    html += '<li class="page-item' + (_ptPage>=pages-1?' disabled':'') + '">' +
            '<a class="page-link" href="#" onclick="ptGoPage(' + (_ptPage+1) + ');return false;">&rsaquo;</a></li>';
  }
  document.getElementById('ptPaginatorPages').innerHTML = html;
}

function ptPageRange(cur, total) {
  if (total <= 9) return Array.from({length:total},function(_,i){return i;});
  var r = [0];
  if (cur > 3) r.push(-1);
  for (var i = Math.max(1,cur-2); i <= Math.min(total-2,cur+2); i++) r.push(i);
  if (cur < total-4) r.push(-1);
  r.push(total-1);
  return r;
}

function ptGoPage(p) {
  var rows = ptGetFiltered();
  var pages = (_ptPageSize > 0) ? Math.ceil(rows.length/_ptPageSize) : 1;
  if (p<0||p>=pages) return;
  _ptPage = p;
  ptBuildTable();
  document.getElementById('ptDataTable').scrollIntoView({behavior:'smooth',block:'nearest'});
}

function ptSetPageSize(v) {
  _ptPageSize = parseInt(v, 10);
  _ptPage = 0;
  ptBuildTable();
  try { localStorage.setItem('ptPageSize', v); } catch(e){}
}

function ptSetSearch(v) { _ptSearch = v.trim().toLowerCase(); _ptPage = 0; ptBuildTable(); }

function ptToggleOrder() {
  _ptReversed = !_ptReversed;
  if (_ptSortedIdx) _ptSortedIdx = _ptSortedIdx.slice().reverse();
  _ptPage = 0;
  ptApplyReverseBtn();
  ptBuildTable();
  if (document.getElementById('ptChartView').style.display !== 'none') ptBuildCharts();
  try { localStorage.setItem('ptReversed', _ptReversed ? '1' : '0'); } catch(e) {}
}

function ptApplyReverseBtn() {
  var btn  = document.getElementById('ptBtnReverse');
  var icon = document.getElementById('ptBtnReverseIcon');
  if (!btn) return;
  btn.classList.toggle('active', _ptReversed);
  if (_ptReversed) {
    icon.className = 'bi bi-sort-up-alt';
    btn.title = 'Showing latest first \u2014 click to show earliest first';
  } else {
    icon.className = 'bi bi-sort-down-alt';
    btn.title = 'Showing earliest first \u2014 click to show latest first';
  }
}

function ptSortTable(col) {
  var th = document.querySelector('#ptDataTable thead tr').cells[col];
  var asc = th.getAttribute('data-order') === 'asc';
  var idx = ptTimes.map(function(_,i){return i;});
  idx.sort(function(a,b) {
    var av, bv;
    if (col===0) return asc ? ptTimes[a].localeCompare(ptTimes[b]) : ptTimes[b].localeCompare(ptTimes[a]);
    if (col===1) av = ptConnections[a], bv = ptConnections[b];
    else if (col===2) av = ptBytesIn[a],     bv = ptBytesIn[b];
    else if (col===3) av = ptBytesOut[a],    bv = ptBytesOut[b];
    else if (col===4) av = ptRateIn[a],      bv = ptRateIn[b];
    else              av = ptRateOut[a],     bv = ptRateOut[b];
    return asc ? av-bv : bv-av;
  });
  th.setAttribute('data-order', asc ? 'desc' : 'asc');
  _ptSortedIdx = idx;
  if (col === 0) { _ptReversed = !asc; ptApplyReverseBtn(); }
  _ptPage = 0;
  ptBuildTable();
}
</script>

</c:if>
