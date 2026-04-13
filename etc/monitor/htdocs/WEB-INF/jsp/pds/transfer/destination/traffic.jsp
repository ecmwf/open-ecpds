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
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 0.75rem 1.25rem;
  flex: 1;
  min-width: 130px;
}
.traffic-stat-card .stat-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  color: #6c757d;
  letter-spacing: 0.04em;
}
.traffic-stat-card .stat-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: #212529;
}
.traffic-stat-card .stat-sub { font-size: 0.72rem; color: #6c757d; }
.rate-excellent { color: #198754; font-weight: 600; }
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

<%-- Header bar: title + view toggle --%>
<div class="d-flex justify-content-between align-items-center mb-3 mt-2 flex-wrap gap-2">
  <h6 class="fw-semibold text-secondary mb-0">
    <i class="bi bi-bar-chart-line-fill me-1"></i>Traffic Statistics
  </h6>
  <div class="btn-group btn-group-sm" role="group">
    <button type="button" class="btn btn-outline-secondary" id="btnTable" onclick="setView('table')">
      <i class="bi bi-table me-1"></i>Table
    </button>
    <button type="button" class="btn btn-outline-secondary active" id="btnChart" onclick="setView('chart')">
      <i class="bi bi-bar-chart-fill me-1"></i>Chart
    </button>
  </div>
</div>

<%-- Summary stat cards (always visible, always all-time) --%>
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
  <%-- Toolbar: search + rows per page --%>
  <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
    <div class="input-group input-group-sm" style="max-width:220px;">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input type="search" id="trafficSearch" class="form-control"
             placeholder="Filter by date..." oninput="setSearchTerm(this.value)">
    </div>
    <div class="d-flex align-items-center gap-2">
      <label class="text-muted mb-0" style="font-size:0.82rem;">Rows:</label>
      <select id="pageSizeSelect" class="form-select form-select-sm" style="width:auto;"
              onchange="setPageSize(this.value)">
        <option value="25">25</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="0">All</option>
      </select>
    </div>
  </div>

  <div class="table-responsive">
    <table class="table table-sm table-striped table-hover table-bordered align-middle"
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

  <%-- Paginator --%>
  <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
    <span id="paginatorInfo" class="text-muted" style="font-size:0.82rem;"></span>
    <nav aria-label="Traffic table pages">
      <ul class="pagination pagination-sm mb-0" id="paginatorPages"></ul>
    </nav>
  </div>
</div>

<%-- CHART VIEW --%>
<div id="chartView" style="display:none;">
  <%-- Period selector --%>
  <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
    <span class="text-muted" style="font-size:0.82rem;">Period:</span>
    <div class="btn-group btn-group-sm" id="chartPeriodSelector">
      <button class="btn btn-outline-secondary" data-days="30"  onclick="setChartPeriod(30)">30d</button>
      <button class="btn btn-outline-secondary" data-days="90"  onclick="setChartPeriod(90)">90d</button>
      <button class="btn btn-outline-secondary" data-days="180" onclick="setChartPeriod(180)">180d</button>
      <button class="btn btn-outline-secondary" data-days="365" onclick="setChartPeriod(365)">1y</button>
      <button class="btn btn-outline-secondary active" data-days="0" onclick="setChartPeriod(0)">All</button>
    </div>
    <span id="chartPeriodLabel" class="text-muted ms-1" style="font-size:0.78rem;"></span>
  </div>
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
var _sortedIdx = null, _page = 0, _pageSize = 25, _searchTerm = '';

function getFilteredTableRows() {
  var base = _sortedIdx || tLabels.map(function(_,i){return i;});
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
  var idx = tLabels.map(function(_,i){return i;});
  idx.sort(function(a, b) {
    if (col===0) return asc ? tLabels[a].localeCompare(tLabels[b])   : tLabels[b].localeCompare(tLabels[a]);
    if (col===1) return asc ? tBytes[a]    - tBytes[b]    : tBytes[b]    - tBytes[a];
    if (col===2) return asc ? tDuration[a] - tDuration[b] : tDuration[b] - tDuration[a];
    if (col===3) return asc ? tRates[a]    - tRates[b]    : tRates[b]    - tRates[a];
    if (col===4) return asc ? tFiles[a]    - tFiles[b]    : tFiles[b]    - tFiles[a];
    return 0;
  });
  _sortedIdx = idx;
  _page = 0;
  document.querySelectorAll('#trafficTable thead th').forEach(function(h, i) {
    var icon = h.querySelector('i.bi');
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
var _chartPeriod = 0, _chartBR = null, _chartF = null;

function getChartData(days) {
  var start = (days > 0 && tLabels.length > days) ? tLabels.length - days : 0;
  return {
    labels:   tLabels.slice(start),
    bytes:    tBytes.slice(start),
    rates:    tRates.slice(start),
    files:    tFiles.slice(start),
    fmtBytes: tFmtBytes.slice(start),
    fmtRates: tFmtRates.slice(start)
  };
}

function setChartPeriod(days) {
  _chartPeriod = days;
  document.querySelectorAll('#chartPeriodSelector .btn').forEach(function(b) {
    b.classList.toggle('active', parseInt(b.getAttribute('data-days'), 10) === days);
  });
  buildCharts();
}

function buildCharts() {
  if (_chartBR) { _chartBR.destroy(); _chartBR = null; }
  if (_chartF)  { _chartF.destroy();  _chartF  = null; }

  var d  = getChartData(_chartPeriod);
  var n  = d.labels.length;
  var pt = n > 180 ? 0 : n > 60 ? 1.5 : 3;

  var label = document.getElementById('chartPeriodLabel');
  label.textContent = n < tLabels.length ? '(' + n + ' of ' + tLabels.length + ' days shown)' : '(' + n + ' days)';

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
        legend: { position: 'top' },
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
        yBytes: {
          type: 'linear', position: 'left',
          title: { display: true, text: 'Volume' },
          ticks: { callback: function(v) { return fmtBytes(v); } }
        },
        yRate: {
          type: 'linear', position: 'right',
          title: { display: true, text: 'Mbit/s' },
          grid: { drawOnChartArea: false }
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
      plugins: { legend: { position: 'top' } },
      scales: { y: { title: { display: true, text: 'Files' }, beginAtZero: true, ticks: { precision: 0 } } }
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

// -- Stat cards (all-time totals) ----------------------------------------------
function computeStats() {
  var totalBytes = 0, totalFiles = 0, maxRate = -Infinity, maxRateIdx = 0;
  for (var i = 0; i < tBytes.length; i++) {
    totalBytes += tBytes[i];
    totalFiles += tFiles[i];
    if (tRates[i] > maxRate) { maxRate = tRates[i]; maxRateIdx = i; }
  }
  var n = tBytes.length;
  var avgRate = n ? tRates.reduce(function(s,v){return s+v;}, 0) / n : 0;
  document.getElementById('statTotalBytes').textContent = fmtBytes(totalBytes);
  document.getElementById('statDays').textContent       = n + (n === 1 ? ' day' : ' days');
  document.getElementById('statAvgRate').textContent    = avgRate.toFixed(1);
  document.getElementById('statPeakRate').textContent   = (maxRate >= 0 ? maxRate.toFixed(1) : '--') + ' Mbit/s';
  document.getElementById('statPeakDate').textContent   = tLabels[maxRateIdx] || '--';
  document.getElementById('statTotalFiles').textContent = totalFiles.toLocaleString();
  document.getElementById('statAvgFiles').textContent   = n ? Math.round(totalFiles / n) + '/day avg' : '--';
}

document.addEventListener('DOMContentLoaded', function() {
  computeStats();
  buildTable();
  var saved = 'chart';
  try { saved = localStorage.getItem('trafficView') || 'chart'; } catch(e) {}
  setView(saved);
});
</script>

</c:if>
