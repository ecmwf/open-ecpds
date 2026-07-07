<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="mb-3 mt-2">
  <a href="/do/datafile/monitoring" class="btn btn-outline-secondary btn-sm">
    <i class="bi bi-arrow-left me-1"></i>New Search
  </a>
</div>

<c:if test="${empty ratesList}">
  <div class="alert alert-info d-flex align-items-center gap-2">
    <i class="bi bi-info-circle-fill"></i>
    <span>No rates found for the selected criteria.</span>
  </div>
</c:if>

<c:if test="${not empty ratesList}">
<script src="/assets/js/chart.umd.min.js"></script>

<style>
.rates-stat-card {
  background: var(--bs-tertiary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  padding: 0.75rem 1.25rem;
  flex: 1;
  min-width: 130px;
}
.rates-stat-card .stat-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--bs-secondary-color);
  letter-spacing: 0.04em;
}
.rates-stat-card .stat-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--bs-body-color);
}
.rates-stat-card .stat-sub {
  font-size: 0.72rem;
  color: var(--bs-secondary-color);
}
.rate-excellent { color: #198754; font-weight: 600; }
.rate-good      { color: #0dcaf0; font-weight: 600; }
.rate-normal    { color: #0d6efd; font-weight: 600; }
.rate-slow      { color: #fd7e14; font-weight: 600; }
.rate-poor      { color: #dc3545; font-weight: 600; }
</style>

<script>
var _mode = '<c:out value="${option}"/>';
var rawDates = [], rawGroup = [], rawMover = [], rawCount = [],
    rawBytes = [], rawFmtBytes = [], rawDuration = [], rawFmtDur = [],
    rawRate = [], rawFmtRate = [];
<c:forEach var="r" items="${ratesList}">
rawDates.push('<c:out value="${r.date}"/>');
<c:choose>
  <c:when test="${option == 'ratesPerFileSystem'}">
rawGroup.push('<c:out value="${r.fileSystem}"/>');
rawMover.push('');
  </c:when>
  <c:when test="${option == 'ratesPerTransferServer'}">
rawGroup.push('<c:out value="${r.transferGroupName}"/>');
rawMover.push('<c:out value="${r.transferServerName}"/>');
  </c:when>
  <c:otherwise>
rawGroup.push('<c:out value="${r.transferGroupName}"/>');
rawMover.push('');
  </c:otherwise>
</c:choose>
rawCount.push(${r.count});
rawBytes.push(${r.bytes});
rawFmtBytes.push('<c:out value="${r.formattedBytes}"/>');
rawDuration.push(${r.duration});
rawFmtDur.push('<c:out value="${r.formattedDuration}"/>');
rawRate.push(${r.rate});
rawFmtRate.push('<c:out value="${r.formattedRate}"/>');
</c:forEach>

function fmtBytes(b) {
  if (b >= 1e12) return (b / 1e12).toFixed(2) + ' TB';
  if (b >= 1e9)  return (b / 1e9).toFixed(2) + ' GB';
  if (b >= 1e6)  return (b / 1e6).toFixed(2) + ' MB';
  if (b >= 1e3)  return (b / 1e3).toFixed(2) + ' KB';
  return b + ' B';
}

function rateClass(r) {
  if (r >= 500) return 'rate-excellent';
  if (r >= 100) return 'rate-good';
  if (r >= 10)  return 'rate-normal';
  if (r >= 1)   return 'rate-slow';
  return 'rate-poor';
}

var byDate = {};
for (var i = 0; i < rawDates.length; i++) {
  var d = rawDates[i];
  if (!byDate[d]) byDate[d] = { bytes: 0, duration: 0, files: 0 };
  byDate[d].bytes += rawBytes[i];
  byDate[d].duration += rawDuration[i];
  byDate[d].files += rawCount[i];
}
var tLabels = Object.keys(byDate).sort();
var tBytes = tLabels.map(function(d) { return byDate[d].bytes; });
var tFiles = tLabels.map(function(d) { return byDate[d].files; });
var tRates = tLabels.map(function(d) {
  var dur = byDate[d].duration;
  return dur > 0 ? (byDate[d].bytes * 8 / 1e6) / (dur / 1000) : 0;
});
var tFmtBytes = tBytes.map(function(v) { return fmtBytes(v); });
var tFmtRates = tRates.map(function(v) { return v.toFixed(2) + ' Mbit/s'; });

function getBaseRows() {
  var idx = rawDates.map(function(_, i) { return i; });
  idx.sort(function(a, b) {
    var dateCmp = rawDates[a].localeCompare(rawDates[b]);
    if (dateCmp !== 0) return dateCmp;
    var groupCmp = rawGroup[a].localeCompare(rawGroup[b]);
    if (groupCmp !== 0) return groupCmp;
    return rawMover[a].localeCompare(rawMover[b]);
  });
  return idx;
}

function tableColumnCount() {
  return _mode === 'ratesPerTransferServer' ? 7 : 6;
}

var _reversed = false, _sortedIdx = null, _page = 0, _pageSize = 25, _searchTerm = '';

function getFilteredTableRows() {
  var base = _sortedIdx || getBaseRows();
  if (_reversed && !_sortedIdx) base = base.slice().reverse();
  if (!_searchTerm) return base;
  var term = _searchTerm.toLowerCase();
  return base.filter(function(i) {
    return [
      rawDates[i], rawGroup[i], rawMover[i], String(rawCount[i]),
      rawFmtBytes[i], rawFmtDur[i], rawFmtRate[i]
    ].join(' ').toLowerCase().indexOf(term) !== -1;
  });
}

function buildTable() {
  var rows = getFilteredTableRows();
  var total = rows.length;
  var pageRows = (_pageSize > 0) ? rows.slice(_page * _pageSize, (_page + 1) * _pageSize) : rows;
  var html = '';
  pageRows.forEach(function(i) {
    html += '<tr>';
    html += '<td>' + rawDates[i] + '</td>';
    if (_mode === 'ratesPerFileSystem') {
      html += '<td>' + rawGroup[i] + '</td>';
    } else {
      html += '<td>' + rawGroup[i] + '</td>';
      if (_mode === 'ratesPerTransferServer') {
        html += '<td>' + rawMover[i] + '</td>';
      }
    }
    html += '<td class="text-end">' + rawCount[i].toLocaleString() + '</td>';
    html += '<td title="' + rawBytes[i] + ' bytes">' + rawFmtBytes[i] + '</td>';
    html += '<td title="' + rawDuration[i] + ' ms">' + rawFmtDur[i] + '</td>';
    html += '<td class="' + rateClass(rawRate[i]) + '" title="' + rawFmtRate[i] + '">' + rawRate[i].toFixed(2) + '</td>';
    html += '</tr>';
  });
  document.getElementById('ratesTbody').innerHTML = html || '<tr><td colspan="' + tableColumnCount() + '" class="text-center text-muted fst-italic">No matching rows.</td></tr>';
  buildPaginator(total);
}

function buildPaginator(total) {
  var pages = (_pageSize > 0) ? Math.ceil(total / _pageSize) : 1;
  var start = (_pageSize > 0) ? _page * _pageSize + 1 : 1;
  var end = (_pageSize > 0) ? Math.min((_page + 1) * _pageSize, total) : total;
  document.getElementById('paginatorInfo').textContent =
    total === 0 ? '' : 'Showing ' + start + '-' + end + ' of ' + total.toLocaleString() + ' rows';

  var html = '';
  if (pages > 1) {
    html += '<li class="page-item' + (_page === 0 ? ' disabled' : '') + '">' +
            '<a class="page-link" href="#" onclick="goPage(' + (_page - 1) + ');return false;">&lsaquo;</a></li>';
    getPageRange(_page, pages).forEach(function(p) {
      if (p === -1) {
        html += '<li class="page-item disabled"><span class="page-link">&hellip;</span></li>';
      } else {
        html += '<li class="page-item' + (p === _page ? ' active' : '') + '">' +
                '<a class="page-link" href="#" onclick="goPage(' + p + ');return false;">' + (p + 1) + '</a></li>';
      }
    });
    html += '<li class="page-item' + (_page >= pages - 1 ? ' disabled' : '') + '">' +
            '<a class="page-link" href="#" onclick="goPage(' + (_page + 1) + ');return false;">&rsaquo;</a></li>';
  }
  document.getElementById('paginatorPages').innerHTML = html;
}

function getPageRange(cur, total) {
  if (total <= 9) return Array.from({ length: total }, function(_, i) { return i; });
  var r = [0];
  if (cur > 3) r.push(-1);
  for (var i = Math.max(1, cur - 2); i <= Math.min(total - 2, cur + 2); i++) r.push(i);
  if (cur < total - 4) r.push(-1);
  r.push(total - 1);
  return r;
}

function goPage(p) {
  var rows = getFilteredTableRows();
  var pages = (_pageSize > 0) ? Math.ceil(rows.length / _pageSize) : 1;
  if (p < 0 || p >= pages) return;
  _page = p;
  buildTable();
  document.getElementById('ratesTable').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
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

function compareString(a, b, asc) {
  return asc ? String(a).localeCompare(String(b)) : String(b).localeCompare(String(a));
}

function compareNumber(a, b, asc) {
  return asc ? a - b : b - a;
}

function sortRatesTable(col) {
  var th = document.querySelector('#ratesTable thead tr').cells[col];
  var asc = th.getAttribute('data-order') === 'asc';
  var idx = getBaseRows();
  idx.sort(function(a, b) {
    if (_mode === 'ratesPerTransferServer') {
      if (col === 0) return compareString(rawDates[a], rawDates[b], asc);
      if (col === 1) return compareString(rawGroup[a], rawGroup[b], asc);
      if (col === 2) return compareString(rawMover[a], rawMover[b], asc);
      if (col === 3) return compareNumber(rawCount[a], rawCount[b], asc);
      if (col === 4) return compareNumber(rawBytes[a], rawBytes[b], asc);
      if (col === 5) return compareNumber(rawDuration[a], rawDuration[b], asc);
      if (col === 6) return compareNumber(rawRate[a], rawRate[b], asc);
    } else {
      if (col === 0) return compareString(rawDates[a], rawDates[b], asc);
      if (col === 1) return compareString(rawGroup[a], rawGroup[b], asc);
      if (col === 2) return compareNumber(rawCount[a], rawCount[b], asc);
      if (col === 3) return compareNumber(rawBytes[a], rawBytes[b], asc);
      if (col === 4) return compareNumber(rawDuration[a], rawDuration[b], asc);
      if (col === 5) return compareNumber(rawRate[a], rawRate[b], asc);
    }
    return 0;
  });
  _sortedIdx = idx;
  _page = 0;
  document.querySelectorAll('#ratesTable thead th').forEach(function(h, i) {
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

var _chartPeriod = 0, _chartBR = null, _chartF = null;

function getChartData(days) {
  var start = (days > 0 && tLabels.length > days) ? tLabels.length - days : 0;
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
  return { labels: labels, bytes: bytes, rates: rates, files: files, fmtBytes: fmtBytes, fmtRates: fmtRates };
}

function setChartPeriod(days) {
  _chartPeriod = days;
  document.querySelectorAll('#chartPeriodSelector .btn').forEach(function(b) {
    b.classList.toggle('active', parseInt(b.getAttribute('data-days'), 10) === days);
  });
  buildCharts();
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
  if (_chartF) { _chartF.destroy(); _chartF = null; }

  var d = getChartData(_chartPeriod);
  var n = d.labels.length;
  var pt = n > 180 ? 0 : n > 60 ? 1.5 : 3;

  document.getElementById('chartPeriodLabel').textContent =
    n < tLabels.length ? '(' + n + ' of ' + tLabels.length + ' days shown)' : '(' + n + ' days)';

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
              return ctx.dataset.label.indexOf('Volume') === 0
                ? '  Volume: ' + d.fmtBytes[i]
                : '  Rate: ' + d.fmtRates[i];
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
          type: 'linear',
          position: 'left',
          title: { display: true, text: 'Volume', color: theme.bodyColor },
          ticks: { color: theme.bodyColor, callback: function(v) { return fmtBytes(v); } },
          grid: { color: theme.borderColor }
        },
        yRate: {
          type: 'linear',
          position: 'right',
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

// -- Order toggle (earliest-first ↔ latest-first) -----------------------------
function toggleOrder() {
  _reversed = !_reversed;
  _sortedIdx = null;
  _page = 0;
  document.querySelectorAll('#ratesTable thead th').forEach(function(h) {
    h.setAttribute('data-order', 'asc');
    var icon = h.querySelector('i.bi');
    if (icon) { icon.className = 'bi bi-arrow-down-up text-muted'; icon.style.fontSize = '0.6rem'; }
  });
  _applyReverseBtn();
  buildTable();
  if (_chartBR || _chartF) buildCharts();
  try { localStorage.setItem('ratesReversed', _reversed ? '1' : '0'); } catch(e) {}
}

function _applyReverseBtn() {
  var btn  = document.getElementById('btnReverse');
  var icon = document.getElementById('btnReverseIcon');
  if (!btn) return;
  btn.classList.toggle('active', _reversed);
  if (_reversed) {
    icon.className = 'bi bi-sort-up-alt';
    btn.title = 'Showing latest first \u2014 click to show earliest first';
  } else {
    icon.className = 'bi bi-sort-down-alt';
    btn.title = 'Showing earliest first \u2014 click to show latest first';
  }
}

function setView(v) {
  document.getElementById('tableView').style.display = (v === 'table') ? '' : 'none';
  document.getElementById('chartView').style.display = (v === 'chart') ? '' : 'none';
  document.getElementById('btnTable').classList.toggle('active', v === 'table');
  document.getElementById('btnChart').classList.toggle('active', v === 'chart');
  if (v === 'chart') buildCharts();
  try { localStorage.setItem('ratesView', v); } catch (e) {}
}

function computeStats() {
  var totalBytes = 0;
  var totalFiles = 0;
  var totalRate = 0;
  var maxRate = -Infinity;
  var maxRateDate = '--';
  for (var i = 0; i < rawBytes.length; i++) {
    totalBytes += rawBytes[i];
    totalFiles += rawCount[i];
    totalRate += rawRate[i];
    if (rawRate[i] > maxRate) {
      maxRate = rawRate[i];
      maxRateDate = rawDates[i];
    }
  }
  var avgRate = rawRate.length ? totalRate / rawRate.length : 0;
  document.getElementById('statTotalBytes').textContent = fmtBytes(totalBytes);
  document.getElementById('statRows').textContent = rawDates.length.toLocaleString() + ' rows';
  document.getElementById('statAvgRate').textContent = avgRate.toFixed(1);
  document.getElementById('statPeakRate').textContent = (maxRate > -Infinity ? maxRate.toFixed(1) : '--') + ' Mbit/s';
  document.getElementById('statPeakDate').textContent = maxRateDate;
  document.getElementById('statTotalFiles').textContent = totalFiles.toLocaleString();
  document.getElementById('statTotalDays').textContent = tLabels.length.toLocaleString() + ' days';
}

document.addEventListener('DOMContentLoaded', function() {
  try { if (localStorage.getItem('ratesReversed') === '1') _reversed = true; } catch(e) {}
  computeStats();
  buildTable();
  _applyReverseBtn();
  var saved = 'table';
  try { saved = localStorage.getItem('ratesView') || 'table'; } catch (e) {}
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

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
  <h6 class="fw-semibold text-secondary mb-0">
    <c:choose>
      <c:when test="${option == 'rates'}">
        <i class="bi bi-bar-chart-line-fill me-1"></i>Rates per Transfer Group &mdash; <c:out value="${caller}" /> / <c:out value="${sourceHost}" />
      </c:when>
      <c:when test="${option == 'ratesPerTransferServer'}">
        <i class="bi bi-server me-1"></i>Rates per Data Mover &mdash; <c:out value="${caller}" /> / <c:out value="${sourceHost}" />
      </c:when>
      <c:otherwise>
        <i class="bi bi-hdd-fill me-1"></i>Rates per File System for <c:out value="${transferServerName}" /> &mdash; <c:out value="${caller}" /> / <c:out value="${sourceHost}" />
      </c:otherwise>
    </c:choose>
  </h6>
  <div class="d-flex align-items-center gap-1">
    <button type="button" class="btn btn-sm btn-outline-secondary active" id="btnTable" onclick="setView('table')">
      <i class="bi bi-table me-1"></i>Table
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnChart" onclick="setView('chart')">
      <i class="bi bi-bar-chart-fill me-1"></i>Chart
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnReverse"
        onclick="toggleOrder()" title="Showing earliest first — click to show latest first">
      <i class="bi bi-sort-down-alt" id="btnReverseIcon"></i>
    </button>
  </div>
</div>

<div class="d-flex flex-wrap gap-3 mb-4">
  <div class="rates-stat-card">
    <div class="stat-label"><i class="bi bi-hdd-fill me-1"></i>Total Volume</div>
    <div class="stat-value" id="statTotalBytes">&mdash;</div>
    <div class="stat-sub" id="statRows">-- rows</div>
  </div>
  <div class="rates-stat-card">
    <div class="stat-label"><i class="bi bi-speedometer2 me-1"></i>Avg Rate</div>
    <div class="stat-value" id="statAvgRate">&mdash;</div>
    <div class="stat-sub">Mbit/s average</div>
  </div>
  <div class="rates-stat-card">
    <div class="stat-label"><i class="bi bi-trophy-fill me-1"></i>Peak Rate</div>
    <div class="stat-value" id="statPeakRate">&mdash;</div>
    <div class="stat-sub" id="statPeakDate">&mdash;</div>
  </div>
  <div class="rates-stat-card">
    <div class="stat-label"><i class="bi bi-files me-1"></i>Total Files</div>
    <div class="stat-value" id="statTotalFiles">&mdash;</div>
    <div class="stat-sub" id="statTotalDays">-- days</div>
  </div>
</div>

<div id="tableView">
  <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
    <div class="input-group input-group-sm" style="max-width:260px;">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input type="search" id="ratesSearch" class="form-control"
             placeholder="Search rows..." oninput="setSearchTerm(this.value)">
    </div>
    <div class="d-flex align-items-center gap-2">
      <label class="text-muted mb-0" style="font-size:0.82rem;">Rows:</label>
      <select id="pageSizeSelect" class="form-select form-select-sm" style="width:auto;"
              onchange="setPageSize(this.value)">
        <option value="10">10</option>
        <option value="25">25</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="250">250</option>
      </select>
    </div>
  </div>

  <div class="table-responsive">
    <table class="table table-sm table-striped table-hover table-bordered align-middle"
           id="ratesTable" style="font-size:0.82rem; white-space:nowrap;">
      <thead class="table-primary">
        <tr>
          <th style="cursor:default;">
            Date
          </th>
          <c:choose>
            <c:when test="${option == 'ratesPerFileSystem'}">
              <th onclick="sortRatesTable(1)" style="cursor:pointer;" data-order="asc">
                File System <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(2)" style="cursor:pointer;" data-order="asc">
                Files <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(3)" style="cursor:pointer;" data-order="asc">
                Volume <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(4)" style="cursor:pointer;" data-order="asc">
                Duration <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(5)" style="cursor:pointer;" data-order="asc">
                Rate (Mbit/s) <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
            </c:when>
            <c:when test="${option == 'ratesPerTransferServer'}">
              <th onclick="sortRatesTable(1)" style="cursor:pointer;" data-order="asc">
                Transfer Group <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(2)" style="cursor:pointer;" data-order="asc">
                Data Mover <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(3)" style="cursor:pointer;" data-order="asc">
                Files <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(4)" style="cursor:pointer;" data-order="asc">
                Volume <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(5)" style="cursor:pointer;" data-order="asc">
                Duration <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(6)" style="cursor:pointer;" data-order="asc">
                Rate (Mbit/s) <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
            </c:when>
            <c:otherwise>
              <th onclick="sortRatesTable(1)" style="cursor:pointer;" data-order="asc">
                Transfer Group <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(2)" style="cursor:pointer;" data-order="asc">
                Files <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(3)" style="cursor:pointer;" data-order="asc">
                Volume <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(4)" style="cursor:pointer;" data-order="asc">
                Duration <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
              <th onclick="sortRatesTable(5)" style="cursor:pointer;" data-order="asc">
                Rate (Mbit/s) <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i>
              </th>
            </c:otherwise>
          </c:choose>
        </tr>
      </thead>
      <tbody id="ratesTbody"></tbody>
    </table>
  </div>

  <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
    <span id="paginatorInfo" class="text-muted" style="font-size:0.82rem;"></span>
    <nav aria-label="Rates table pages">
      <ul class="pagination pagination-sm mb-0" id="paginatorPages"></ul>
    </nav>
  </div>
</div>

<div id="chartView" style="display:none;">
  <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
    <span class="text-muted" style="font-size:0.82rem;">Period:</span>
    <div class="btn-group btn-group-sm" id="chartPeriodSelector">
      <button class="btn btn-outline-secondary" data-days="30" onclick="setChartPeriod(30)">30d</button>
      <button class="btn btn-outline-secondary" data-days="90" onclick="setChartPeriod(90)">90d</button>
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
</c:if>
