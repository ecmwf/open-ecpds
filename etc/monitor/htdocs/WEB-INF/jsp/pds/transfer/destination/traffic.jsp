<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:if test="${empty destination.trafficList}">
  <div class="alert alert-info d-flex align-items-center gap-2 mt-3">
    <i class="bi bi-info-circle-fill"></i>
    <span>No traffic data available for destination <strong>${destination.name}</strong>.</span>
  </div>
</c:if>

<c:if test="${not empty destination.trafficList}">

<script src="/assets/js/chart.umd.min.js"></script>

<style>
.traffic-stat-card {
  background: var(--bs-tertiary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  padding: 0.75rem 1.25rem;
  flex: 1;
  min-width: 130px;
}
.traffic-stat-card .stat-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--bs-secondary-color);
  letter-spacing: 0.04em;
}
.traffic-stat-card .stat-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--bs-body-color);
}
.traffic-stat-card .stat-sub { font-size: 0.72rem; color: var(--bs-secondary-color); }
.rate-excellent { color: var(--bs-border-color,#dee2e6); font-weight: 600; }
.rate-good      { color: #0dcaf0; font-weight: 600; }
.rate-normal    { color: #0d6efd; font-weight: 600; }
.rate-slow      { color: #fd7e14; font-weight: 600; }
.rate-poor      { color: #dc3545; font-weight: 600; }
</style>

<%-- Embed server-side data as JS arrays --%>
<script>
const _tLabels = [], _tBytes = [], _tRates = [], _tFiles = [], _tFmtBytes = [], _tFmtRates = [], _tFmtDur = [], _tDuration = [];
<c:forEach var="t" items="${destination.trafficList}">
_tLabels.push('<c:out value="${t.date}"/>');
_tBytes.push(${t.bytes});
_tRates.push(${t.rate});
_tFiles.push(${t.files});
_tFmtBytes.push('<c:out value="${t.formattedBytes}"/>');
_tFmtRates.push('<c:out value="${t.formattedRate}"/>');
_tFmtDur.push('<c:out value="${t.formattedDuration}"/>');
_tDuration.push(${t.duration});
</c:forEach>
// Sort all arrays by date ascending
const _si = _tLabels.map((_,i)=>i).sort((a,b)=>_tLabels[a].localeCompare(_tLabels[b]));
const tLabels   = _si.map(i=>_tLabels[i]);
const tBytes    = _si.map(i=>_tBytes[i]);
const tRates    = _si.map(i=>_tRates[i]);
const tFiles    = _si.map(i=>_tFiles[i]);
const tFmtBytes = _si.map(i=>_tFmtBytes[i]);
const tFmtRates = _si.map(i=>_tFmtRates[i]);
const tFmtDur   = _si.map(i=>_tFmtDur[i]);
const tDuration = _si.map(i=>_tDuration[i]);
</script>

<%-- Header bar: title + info | period selector (left) | view toggle (right) --%>
<div class="d-flex align-items-center mb-3 mt-2 flex-wrap gap-2">
  <div class="d-flex align-items-center gap-2 me-2">
    <h6 class="fw-semibold text-secondary mb-0">
      <i class="bi bi-bar-chart-line-fill me-1"></i>Traffic Statistics
    </h6>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#destTrafficInfo"
        aria-expanded="false" title="About this page">
      <i class="bi bi-info-circle"></i>
    </button>
  </div>
  <span class="text-muted" style="font-size:0.82rem;">Period:</span>
  <div class="btn-group btn-group-sm" id="chartPeriodSelector">
    <button class="btn btn-outline-secondary" data-days="30"  onclick="setChartPeriod(30)">30d</button>
    <button class="btn btn-outline-secondary" data-days="90"  onclick="setChartPeriod(90)">90d</button>
    <button class="btn btn-outline-secondary" data-days="180" onclick="setChartPeriod(180)">180d</button>
    <button class="btn btn-outline-secondary" data-days="365" onclick="setChartPeriod(365)">1y</button>
    <button class="btn btn-outline-secondary active" data-days="0" onclick="setChartPeriod(0)">All</button>
  </div>
  <span id="chartPeriodLabel" class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border" style="display:none;font-size:0.75rem;font-weight:500;"></span>
  <div class="ms-auto d-flex align-items-center gap-1">
    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnTable" onclick="setView('table')">
      <i class="bi bi-table me-1"></i>Table
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary active" id="btnChart" onclick="setView('chart')">
      <i class="bi bi-bar-chart-fill me-1"></i>Chart
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnReverse"
        onclick="toggleOrder()" title="Showing earliest first — click to show latest first">
      <i class="bi bi-sort-down-alt" id="btnReverseIcon"></i>
    </button>
  </div>
</div>

<div class="collapse mb-3" id="destTrafficInfo">
  <div class="card-body py-2 px-3 border rounded" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Transfer traffic recorded for this destination</strong>
    <p class="mb-1">This page shows daily transfer volume, rate, and file count for files disseminated through this destination. The same data is aggregated across all destinations in the <auth:if basePathKey="datarates.basepath" paths=""><auth:then><a href="/do/datafile/datarates" class="text-decoration-none">Data Rates</a> page</auth:then><auth:else>Data Rates page</auth:else></auth:if>.</p>
    <ul class="mb-1 ps-3">
      <li><strong>Volume / Rate</strong> &mdash; total bytes sent and average Mbps (bytes &divide; transfer duration) per day.</li>
      <li><strong>Transfers</strong> &mdash; number of files successfully disseminated each day.</li>
      <li><strong>Granularity</strong> &mdash; today's data is available immediately; historical data is aggregated nightly and kept indefinitely.</li>
      <li><strong>Deleted destination</strong> &mdash; if this destination is removed from the system, its traffic history is also permanently deleted and will no longer appear in the global Data Rates page.</li>
    </ul>
    <strong class="d-block mb-1 mt-2">Controls</strong>
    <ul class="mb-0 ps-3">
      <li><strong>Period</strong> &mdash; use the <kbd>30d</kbd> / <kbd>90d</kbd> / <kbd>180d</kbd> / <kbd>1y</kbd> / <kbd>All</kbd> buttons to restrict the visible data. The filter applies to the summary stat cards, the chart, and the table simultaneously.</li>
      <li><strong>Table / Chart</strong> &mdash; switch between the bar/line chart view and a sortable, paginated table of daily figures.</li>
      <li><strong>Sort order</strong> (&uarr;&darr; button) &mdash; toggle between earliest-first and latest-first. Applies to both the chart and the table.</li>
    </ul>
  </div>
</div>

<%-- Summary stat cards — reflect the selected period --%>
<div class="d-flex flex-wrap gap-3 mb-4">
  <div class="traffic-stat-card">
    <div class="stat-label"><i class="bi bi-hdd-fill me-1"></i>Total Volume</div>
    <div class="stat-value" id="statTotalBytes">&mdash;</div>
    <div class="stat-sub" id="statDays">-- days</div>
  </div>
  <div class="traffic-stat-card">
    <div class="stat-label"><i class="bi bi-speedometer2 me-1"></i>Avg Rate</div>
    <div class="stat-value" id="statAvgRate">&mdash;</div>
    <div class="stat-sub">Mbit/s average</div>
  </div>
  <div class="traffic-stat-card">
    <div class="stat-label"><i class="bi bi-trophy-fill me-1"></i>Peak Rate</div>
    <div class="stat-value" id="statPeakRate">&mdash;</div>
    <div class="stat-sub" id="statPeakDate">&mdash;</div>
  </div>
  <div class="traffic-stat-card">
    <div class="stat-label"><i class="bi bi-files me-1"></i>Total Files</div>
    <div class="stat-value" id="statTotalFiles">&mdash;</div>
    <div class="stat-sub" id="statAvgFiles">&mdash;</div>
  </div>
</div>

<%-- TABLE VIEW --%>
<div id="tableView">
  <div class="card border-0 shadow-sm">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
      <i class="bi bi-table text-primary"></i>
      <span class="fw-semibold">Daily Traffic</span>
      <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
          <span class="input-group-text"><i class="bi bi-search"></i></span>
          <input type="search" id="trafficSearch" class="form-control"
                 placeholder="Filter by date..." oninput="setSearchTerm(this.value)" style="min-width:160px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
          <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
          <select id="pageSizeSelect" class="form-select form-select-sm" style="width:auto"
                  onchange="setPageSize(this.value)">
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
               id="trafficTable" style="font-size:0.82rem; white-space:nowrap;">
          <thead class="table-light">
            <tr>
              <th onclick="sortTrafficTable(0)" style="cursor:pointer;" data-order="asc">
                Date <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortTrafficTable(1)" style="cursor:pointer;" data-order="asc">
                Volume <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortTrafficTable(2)" style="cursor:pointer;" data-order="asc">
                Duration <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortTrafficTable(3)" style="cursor:pointer;" data-order="asc">
                Rate (Mbit/s) <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortTrafficTable(4)" style="cursor:pointer;" data-order="asc">
                Files <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
            </tr>
          </thead>
          <tbody id="trafficTbody"></tbody>
        </table>
      </div>
    </div>
    <div class="card-footer d-flex justify-content-between align-items-center flex-wrap gap-2 py-2"
         style="background:var(--bs-secondary-bg)">
      <span id="paginatorInfo" class="text-muted" style="font-size:0.82rem;"></span>
      <nav aria-label="Traffic table pages">
        <ul class="pagination pagination-sm mb-0" id="paginatorPages"></ul>
      </nav>
    </div>
  </div>
</div>

<%-- CHART VIEW --%>
<div id="chartView" style="display:none;">
  <div class="mb-4 p-2 border rounded" style="position:relative; height:320px;">
    <canvas id="chartBytesRate"></canvas>
  </div>
  <div class="p-2 border rounded" style="position:relative; height:220px;">
    <canvas id="chartFiles"></canvas>
  </div>
</div>

<script>
function rateClass(r) {
  if (r >= 500) return 'rate-excellent';
  if (r >= 100) return 'rate-good';
  if (r >= 10)  return 'rate-normal';
  if (r >= 1)   return 'rate-slow';
  return 'rate-poor';
}

function fmtBytes(b) {
  if (b >= 1e12) return (b/1e12).toFixed(2) + ' TB';
  if (b >= 1e9)  return (b/1e9).toFixed(2)  + ' GB';
  if (b >= 1e6)  return (b/1e6).toFixed(2)  + ' MB';
  if (b >= 1e3)  return (b/1e3).toFixed(2)  + ' KB';
  return b + ' B';
}

// -- Table: state -------------------------------------------------------------
var _sortedIdx = null, _page = 0, _pageSize = 25, _searchTerm = '', _reversed = false;

function getBaseOrder() {
  var base = tLabels.map(function(_,i){return i;});
  return _reversed ? base.reverse() : base;
}

function getFilteredTableRows() {
  var allIdx = _sortedIdx || getBaseOrder();
  // Restrict to the selected period
  var base = allIdx.filter(function(i) { return i >= _tableStart; });
  if (!_searchTerm) return base;
  var term = _searchTerm.toLowerCase();
  return base.filter(function(i) { return tLabels[i].toLowerCase().indexOf(term) !== -1; });
}

function buildTable() {
  var rows  = getFilteredTableRows();
  var total = rows.length;
  var pageRows = (_pageSize > 0) ? rows.slice(_page * _pageSize, (_page + 1) * _pageSize) : rows;
  var html = '';
  pageRows.forEach(function(i) {
    html +=
      '<tr>' +
      '<td>' + tLabels[i] + '</td>' +
      '<td title="' + tBytes[i] + ' bytes">' + tFmtBytes[i] + '</td>' +
      '<td title="' + tDuration[i] + ' ms">' + tFmtDur[i] + '</td>' +
      '<td class="' + rateClass(tRates[i]) + '" title="' + tFmtRates[i] + '">' + tRates[i].toFixed(2) + '</td>' +
      '<td class="text-end">' + tFiles[i].toLocaleString() + '</td>' +
      '</tr>';
  });
  document.getElementById('trafficTbody').innerHTML = html || '<tr><td colspan="5" class="text-center text-muted fst-italic">No matching rows.</td></tr>';
  buildPaginator(total);
}

function buildPaginator(total) {
  var pages = (_pageSize > 0) ? Math.ceil(total / _pageSize) : 1;
  var start = (_pageSize > 0) ? _page * _pageSize + 1 : 1;
  var end   = (_pageSize > 0) ? Math.min((_page + 1) * _pageSize, total) : total;
  document.getElementById('paginatorInfo').textContent =
    total === 0 ? '' : 'Showing ' + start + '-' + end + ' of ' + total.toLocaleString() + ' rows';

  var html = '';
  if (pages > 1) {
    html += '<li class="page-item' + (_page === 0 ? ' disabled' : '') + '">' +
            '<a class="page-link" href="#" onclick="goPage(' + (_page-1) + ');return false;">&lsaquo;</a></li>';
    getPageRange(_page, pages).forEach(function(p) {
      if (p === -1) {
        html += '<li class="page-item disabled"><span class="page-link">&hellip;</span></li>';
      } else {
        html += '<li class="page-item' + (p === _page ? ' active' : '') + '">' +
                '<a class="page-link" href="#" onclick="goPage(' + p + ');return false;">' + (p+1) + '</a></li>';
      }
    });
    html += '<li class="page-item' + (_page >= pages-1 ? ' disabled' : '') + '">' +
            '<a class="page-link" href="#" onclick="goPage(' + (_page+1) + ');return false;">&rsaquo;</a></li>';
  }
  document.getElementById('paginatorPages').innerHTML = html;
}

function getPageRange(cur, total) {
  if (total <= 9) return Array.from({length: total}, function(_,i){return i;});
  var r = [0];
  if (cur > 3) r.push(-1);
  for (var i = Math.max(1, cur-2); i <= Math.min(total-2, cur+2); i++) r.push(i);
  if (cur < total-4) r.push(-1);
  r.push(total-1);
  return r;
}

function goPage(p) {
  var rows  = getFilteredTableRows();
  var pages = (_pageSize > 0) ? Math.ceil(rows.length / _pageSize) : 1;
  if (p < 0 || p >= pages) return;
  _page = p;
  buildTable();
  document.getElementById('trafficTable').scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

function setPageSize(v) {
  _pageSize = parseInt(v, 10);
  _page = 0;
  buildTable();
}

function setSearchTerm(v) {
  _searchTerm = v.trim();
  _page = 0;
  buildTable();
}

function sortTrafficTable(col) {
  var th  = document.querySelector('#trafficTable thead tr').cells[col];
  var asc = th.getAttribute('data-order') === 'asc';
  _page = 0;

  if (col === 0) {
    // Date column: drive via _reversed so it stays in sync with the toggle button
    _reversed = !asc;
    _sortedIdx = null;
    _applyReverseBtn();
  } else {
    var idx = tLabels.map(function(_,i){return i;});
    idx.sort(function(a, b) {
      if (col===1) return asc ? tBytes[a]    - tBytes[b]    : tBytes[b]    - tBytes[a];
      if (col===2) return asc ? tDuration[a] - tDuration[b] : tDuration[b] - tDuration[a];
      if (col===3) return asc ? tRates[a]    - tRates[b]    : tRates[b]    - tRates[a];
      if (col===4) return asc ? tFiles[a]    - tFiles[b]    : tFiles[b]    - tFiles[a];
      return 0;
    });
    _sortedIdx = idx;
  }

  document.querySelectorAll('#trafficTable thead th').forEach(function(h, i) {
    var icon = h.querySelector('i.bi');
    if (!icon) return;
    if (i === col) {
      h.setAttribute('data-order', asc ? 'desc' : 'asc');
      icon.className = 'bi ' + (asc ? 'bi-arrow-up' : 'bi-arrow-down') + ' text-primary';
    } else {
      h.setAttribute('data-order', 'asc');
      icon.className = 'bi bi-arrow-down-up text-muted';
    }
    icon.style.fontSize = '0.6rem';
  });
  buildTable();
}

// -- Chart: period filter ------------------------------------------------------
var _chartPeriod = 0, _chartBR = null, _chartF = null, _tableStart = 0;

function getChartData(days) {
  var start = (days > 0 && tLabels.length > days) ? tLabels.length - days : 0;
  _tableStart = start;
  var labels   = tLabels.slice(start);
  var bytes    = tBytes.slice(start);
  var rates    = tRates.slice(start);
  var files    = tFiles.slice(start);
  var fmtBytes = tFmtBytes.slice(start);
  var fmtRates = tFmtRates.slice(start);
  if (_reversed) {
    labels   = labels.slice().reverse();
    bytes    = bytes.slice().reverse();
    rates    = rates.slice().reverse();
    files    = files.slice().reverse();
    fmtBytes = fmtBytes.slice().reverse();
    fmtRates = fmtRates.slice().reverse();
  }
  return {
    labels: labels, bytes: bytes, rates: rates, files: files, fmtBytes: fmtBytes, fmtRates: fmtRates
  };
}

function setChartPeriod(days) {
  _chartPeriod = days;
  document.querySelectorAll('#chartPeriodSelector .btn').forEach(function(b) {
    b.classList.toggle('active', parseInt(b.getAttribute('data-days'), 10) === days);
  });
  computeStats();
  buildCharts();
  _page = 0;
  buildTable();
}

function getThemeColors() {
  var s = getComputedStyle(document.documentElement);
  return {
    bodyColor:   (s.getPropertyValue('--bs-body-color')      || '').trim() || '#212529',
    borderColor: (s.getPropertyValue('--bs-border-color')    || '').trim() || '#dee2e6'
  };
}

function buildCharts() {
  var theme = getThemeColors();

  if (_chartBR) { _chartBR.destroy(); _chartBR = null; }
  if (_chartF)  { _chartF.destroy();  _chartF  = null; }

  var d  = getChartData(_chartPeriod);
  var n  = d.labels.length;
  var pt = n > 180 ? 0 : n > 60 ? 1.5 : 3;

  var label = document.getElementById('chartPeriodLabel');
  var txt = n < tLabels.length ? n + ' of ' + tLabels.length + ' days shown' : n + ' days';
  label.textContent = txt;
  label.style.display = txt ? '' : 'none';

  _chartBR = new Chart(document.getElementById('chartBytesRate'), {
    data: {
      labels: d.labels,
      datasets: [
        {
          type: 'bar',
          label: 'Volume',
          data: d.bytes,
          backgroundColor: 'rgba(13,110,253,0.6)',
          borderColor: 'rgba(13,110,253,0.9)',
          borderWidth: 1,
          yAxisID: 'yBytes',
          order: 2
        },
        {
          type: 'line',
          label: 'Rate (Mbit/s)',
          data: d.rates,
          borderColor: 'rgba(220,53,69,0.85)',
          backgroundColor: 'rgba(220,53,69,0.08)',
          borderWidth: 2,
          pointRadius: pt,
          tension: 0.3,
          fill: false,
          yAxisID: 'yRate',
          order: 1
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { position: 'top', labels: { color: theme.bodyColor } },
        tooltip: {
          callbacks: {
            label: function(ctx) {
              var i = ctx.dataIndex;
              return ctx.dataset.label.startsWith('Volume')
                ? '  Volume: ' + d.fmtBytes[i]
                : '  Rate: '   + d.fmtRates[i];
            }
          }
        }
      },
      scales: {
        x: {
          ticks: { color: theme.bodyColor },
          grid: { color: theme.borderColor }
        },
        yBytes: {
          type: 'linear', position: 'left',
          title: { display: true, text: 'Volume', color: theme.bodyColor },
          ticks: { color: theme.bodyColor, callback: function(v) { return fmtBytes(v); } },
          grid: { color: theme.borderColor }
        },
        yRate: {
          type: 'linear', position: 'right',
          title: { display: true, text: 'Mbit/s', color: theme.bodyColor },
          ticks: { color: theme.bodyColor },
          grid: { color: theme.borderColor, drawOnChartArea: false }
        }
      }
    }
  });

  _chartF = new Chart(document.getElementById('chartFiles'), {
    type: 'bar',
    data: {
      labels: d.labels,
      datasets: [{
        label: 'Files transferred',
        data: d.files,
        backgroundColor: 'rgba(32,201,151,0.6)',
        borderColor: 'rgba(32,201,151,0.9)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'top', labels: { color: theme.bodyColor } } },
      scales: {
        x: {
          ticks: { color: theme.bodyColor },
          grid: { color: theme.borderColor }
        },
        y: {
          title: { display: true, text: 'Files', color: theme.bodyColor },
          beginAtZero: true,
          ticks: { precision: 0, color: theme.bodyColor },
          grid: { color: theme.borderColor }
        }
      }
    }
  });
}

// -- View toggle ---------------------------------------------------------------
function setView(v) {
  document.getElementById('tableView').style.display = (v === 'table') ? '' : 'none';
  document.getElementById('chartView').style.display = (v === 'chart') ? '' : 'none';
  document.getElementById('btnTable').classList.toggle('active', v === 'table');
  document.getElementById('btnChart').classList.toggle('active', v === 'chart');
  if (v === 'chart') buildCharts();
  try { localStorage.setItem('trafficView', v); } catch(e) {}
}

// -- Order toggle (earliest-first ↔ latest-first) -----------------------------
function toggleOrder() {
  _reversed = !_reversed;
  if (_sortedIdx) _sortedIdx = _sortedIdx.slice().reverse();
  _page = 0;
  _applyReverseBtn();
  buildTable();
  if (document.getElementById('chartView').style.display !== 'none') buildCharts();
  try { localStorage.setItem('trafficReversed', _reversed ? '1' : '0'); } catch(e) {}
}

function _applyReverseBtn() {
  var btn  = document.getElementById('btnReverse');
  var icon = document.getElementById('btnReverseIcon');
  btn.classList.toggle('active', _reversed);
  if (_reversed) {
    icon.className = 'bi bi-sort-up-alt';
    btn.title = 'Showing latest first — click to show earliest first';
  } else {
    icon.className = 'bi bi-sort-down-alt';
    btn.title = 'Showing earliest first — click to show latest first';
  }
}

// -- Stat cards (period-aware) -------------------------------------------------
function computeStats() {
  var d = getChartData(_chartPeriod);
  var totalBytes = 0, totalFiles = 0, maxRate = -Infinity, maxRateIdx = 0;
  for (var i = 0; i < d.bytes.length; i++) {
    totalBytes += d.bytes[i];
    totalFiles += d.files[i];
    if (d.rates[i] > maxRate) { maxRate = d.rates[i]; maxRateIdx = i; }
  }
  var n = d.bytes.length;
  var avgRate = n ? d.rates.reduce(function(s,v){return s+v;}, 0) / n : 0;
  document.getElementById('statTotalBytes').textContent = fmtBytes(totalBytes);
  document.getElementById('statDays').textContent       = n + (n === 1 ? ' day' : ' days');
  document.getElementById('statAvgRate').textContent    = avgRate.toFixed(1);
  document.getElementById('statPeakRate').textContent   = (maxRate >= 0 ? maxRate.toFixed(1) : '--') + ' Mbit/s';
  document.getElementById('statPeakDate').textContent   = d.labels[maxRateIdx] || '--';
  document.getElementById('statTotalFiles').textContent = totalFiles.toLocaleString();
  document.getElementById('statAvgFiles').textContent   = n ? Math.round(totalFiles / n) + '/day avg' : '--';
}

document.addEventListener('DOMContentLoaded', function() {
  try { if (localStorage.getItem('trafficReversed') === '1') _reversed = true; } catch(e) {}
  computeStats();
  buildTable();
  _applyReverseBtn();
  var saved = 'chart';
  try { saved = localStorage.getItem('trafficView') || 'chart'; } catch(e) {}
  setView(saved);
});

var _obsTheme = null;
new MutationObserver(function() {
  var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
  if (t === _obsTheme) return;
  _obsTheme = t;
  if (_chartBR || _chartF) buildCharts();
}).observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });
</script>

</c:if>
