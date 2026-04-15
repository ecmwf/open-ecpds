<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<script>
	// Refresh the page with the given period. 0 = disabled.
	var refresh = ${monSesForm.refreshPeriod};
	if (refresh > 0) {
		setTimeout(function() {
			window.location.reload(true);
		}, refresh * 1000);
	}
</script>

<style>
.prod-header { 
  display: grid; 
  gap: 6px; 
}
.prod-pill {
  display: flex; align-items: center; justify-content: flex-start; gap: 8px;
  padding: 4px 10px;
  background: var(--bs-tertiary-bg); border-radius: 6px;
  font-size: 0.8rem; white-space: nowrap;
  text-decoration: none; color: var(--bs-body-color);
  border: 1px solid var(--bs-border-color);
  transition: all 0.15s ease-in-out;
}
.prod-pill:hover { background: var(--bs-secondary-bg); border-color: var(--bs-border-color); color: var(--bs-body-color); text-decoration: none; }
.prod-pill.active { background: #0d6efd; color: #fff; border-color: #0a58ca; font-weight: 600; box-shadow: 0 2px 4px rgba(13,110,253,0.2); }
.prod-pill.active:hover { background: #0b5ed7; color: #fff; }

.mon-updated { font-size: 0.9rem; font-weight: 600; color: var(--bs-body-color); }
.page-nav { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.page-nav .page-label { font-size: 0.78rem; color: var(--bs-secondary-color); font-weight: 600; text-transform: uppercase; letter-spacing: .04em; }
.page-nav .page-pill {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 2px 8px; border-radius: 4px;
  font-size: 0.78rem; background: var(--bs-tertiary-bg); color: var(--bs-body-color);
  text-decoration: none; border: 1px solid var(--bs-border-color);
  width: 45px;
  transition: all 0.15s;
}
.page-nav .page-pill:hover { background: var(--bs-secondary-bg); border-color: var(--bs-secondary-color); color: var(--bs-body-color); text-decoration: none; }
.page-nav .page-pill.active { background: #0d6efd; color: #fff; border-color: #0a58ca; font-weight: 600; }
</style>

<div class="d-flex flex-column gap-2 mb-0 px-1 py-1" style="border-bottom:1px solid var(--bs-border-color);">

  <%-- Product status grid --%>
  <div id="prodHeaderGrid" class="prod-header" style="grid-template-columns: repeat(${fn:length(reqData.productWindowHeader)}, 1fr);">
    <c:forEach var="pro" items="${reqData.productWindowHeader}" varStatus="proIdx">
      <c:set var="isSelected" value="${not empty productStatus && productStatus.product==pro.product && productStatus.time==pro.time}"/>
      <c:set var="dotClass" value="mon-dot mon-dot-${pro.generationStatus lt 0 ? 'n1' : pro.generationStatus}"/>
      <a href="/do/monitoring/summary/${pro.product}/${pro.time}"
         class="prod-pill ${isSelected ? 'active' : ''}"
         title="Scheduled for ${pro.scheduledTime} - Status: ${pro.generationStatusFormattedCode}"
         data-sort-natural="${proIdx.index}"
         data-sort-product="${pro.product}"
         data-sort-time="${pro.time}"
         data-sort-ptime="${not empty pro.productTime ? pro.productTime.time : 0}"
         data-sort-scheduled="${not empty pro.scheduledTime ? pro.scheduledTime.time : 0}"
         data-sort-lastupdate="${not empty pro.lastUpdate ? pro.lastUpdate.time : 0}"
         data-sort-arrival="${not empty pro.arrivalTime ? pro.arrivalTime.time : 0}"
         data-sort-status="${pro.generationStatus}">
        <span class="${dotClass}"></span>${pro.time}-${pro.product}
      </a>
    </c:forEach>
  </div>

  <%-- Header controls: clock | refresh | page nav --%>
  <div class="d-flex flex-row align-items-center flex-wrap gap-2">

    <%-- Current time --%>
    <div class="d-flex align-items-center gap-1">
      <i class="bi bi-clock text-muted" style="font-size:0.85rem;"></i>
      <span class="mon-updated"><content:content name="monSesForm.updated" dateFormatKey="date.format.time" ignoreNull="true" defaultValue="*"/></span>
    </div>
    <span class="text-muted" style="font-size:0.75rem;">|</span>
    <%-- Refresh interval --%>
    <div class="d-flex align-items-center gap-1">
      <i class="bi bi-arrow-clockwise text-muted me-1" style="font-size:0.85rem;" title="Auto-refresh interval"></i>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 30 ? 'active' : ''}" data-value="30">30s</a>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 60 ? 'active' : ''}" data-value="60">1m</a>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 300 ? 'active' : ''}" data-value="300">5m</a>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 600 ? 'active' : ''}" data-value="600">10m</a>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 1800 ? 'active' : ''}" data-value="1800">30m</a>
      <a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 0 ? 'active' : ''}" data-value="0">Off</a>
    </div>
    <%-- Split/Single toggle (only on product detail page with enough rows to split) --%>
    <c:if test="${productStatus.calculated && fn:length(productStepStatii) > 1}">
      <span class="text-muted" style="font-size:0.75rem;">|</span>
      <button id="btnProductLayout" type="button" class="btn btn-sm btn-outline-secondary"
              onclick="toggleProductLayout()" title="Toggle between split and single table view">
        <i class="bi bi-layout-three-columns" id="btnProductLayoutIcon"></i>
        <span id="btnProductLayoutLabel">Single</span>
      </button>
    </c:if>
        <%-- Page selector (only on the main monitoring page, not product detail) --%>
    <c:if test="${not productStatus.calculated}">
      <span class="text-muted" style="font-size:0.75rem;">|</span>
      <div class="page-nav">
        <span class="page-label">page</span>
        <c:forEach var="page" items="${reqData.pages}">
          <c:choose>
            <c:when test="${reqData.page == page}">
              <span class="page-pill active">${page}</span>
            </c:when>
            <c:otherwise>
              <a class="page-pill" href="?page=${page}" title="Display Only Page ${page}">${page}</a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        <c:choose>
          <c:when test="${empty reqData.page}">
            <span class="page-pill active">All</span>
          </c:when>
          <c:otherwise>
            <a class="page-pill" href="?page=" title="Display All Pages">All</a>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>
  </div>

</div>

<style>
.hdr-chip {
  display: inline-block; padding: 2px 9px; border-radius: 20px;
  font-size: 0.75rem; cursor: pointer; border: 1px solid var(--bs-border-color);
  background: var(--bs-tertiary-bg); color: var(--bs-secondary-color); transition: all 0.15s;
  white-space: nowrap; user-select: none; text-decoration: line-through;
}
.hdr-chip:hover { border-color: var(--bs-secondary-color); }
.hdr-chip.selected {
  background: #e8f5e9; border-color: #28a745; color: #155724;
  font-weight: 500; text-decoration: none;
}
[data-bs-theme=dark] .hdr-chip.selected {
  background: #1a3a1f; border-color: #28a745; color: #75c983;
  font-weight: 500; text-decoration: none;
}
</style>

<script>
document.querySelectorAll('.mon-refresh-pill').forEach(function(pill) {
  pill.addEventListener('click', function(e) {
    e.preventDefault();
    var params = new URLSearchParams(window.location.search);
    params.set('refreshPeriod', this.dataset.value);
    window.location.href = '?' + params.toString();
  });
});

/* -- Header product columns per row -- */
(function() {
  function _computeAutoCols() {
    var grid = document.getElementById('prodHeaderGrid');
    if (!grid) return 10;
    var pills = grid.querySelectorAll('.prod-pill');
    if (!pills.length) return 10;
    /* Grid is now full-width (controls are below it), so measure it directly */
    var avail = grid.getBoundingClientRect().width || grid.offsetWidth;
    var gap = parseFloat(getComputedStyle(grid).columnGap) || 6;
    /* Temporarily set max-content to measure each pill's natural width */
    var prev = grid.style.gridTemplateColumns;
    grid.style.gridTemplateColumns = 'repeat(' + pills.length + ', max-content)';
    var maxW = 0;
    pills.forEach(function(p) { if (p.offsetWidth > maxW) maxW = p.offsetWidth; });
    grid.style.gridTemplateColumns = prev;
    if (!maxW || avail <= 0) return 10;
    return Math.max(1, Math.min(
      Math.floor((avail + gap) / (maxW + gap)),
      pills.length
    ));
  }

  function _applyHeaderCols(val) {
    var grid = document.getElementById('prodHeaderGrid');
    if (!grid) return;
    var total = grid.querySelectorAll('.prod-pill').length;
    var cols;
    if (val === 'auto') {
      cols = _computeAutoCols();
    } else if (val === 'all') {
      cols = total;
    } else {
      cols = Math.min(parseInt(val, 10) || total, total);
    }
    grid.style.gridTemplateColumns = 'repeat(' + cols + ', 1fr)';
    /* button label */
    var btn = document.getElementById('btnHeaderCols');
    if (btn) {
      var lbl = (val === 'auto') ? 'Auto' : (val === 'all') ? 'All' : String(val);
      btn.innerHTML = '<i class="bi bi-grid"></i> Cols: ' + lbl;
    }
    /* chip highlight */
    document.querySelectorAll('#headerColChips .hdr-chip').forEach(function(c) {
      c.classList.toggle('selected', c.getAttribute('data-hcol') === String(val));
    });
    localStorage.setItem('monHeaderCols', String(val));
  }

  window.setHeaderCols = function(val) {
    _applyHeaderCols(val);
    document.getElementById('headerColPanel').style.display = 'none';
  };

  function _sortPills(key) {
    var grid = document.getElementById('prodHeaderGrid');
    if (!grid) return;
    var pills = Array.prototype.slice.call(grid.querySelectorAll('.prod-pill'));
    var isStr = (key === 'product');
    pills.sort(function(a, b) {
      var av = a.getAttribute('data-sort-' + key);
      var bv = b.getAttribute('data-sort-' + key);
      var diff = isStr
        ? (av || '').localeCompare(bv || '')
        : ((parseFloat(av) || 0) - (parseFloat(bv) || 0));
      if (diff !== 0) return diff;
      return (parseInt(a.getAttribute('data-sort-natural')) || 0)
           - (parseInt(b.getAttribute('data-sort-natural')) || 0);
    });
    pills.forEach(function(p) { grid.appendChild(p); });
    document.querySelectorAll('#headerSortChips .hdr-chip').forEach(function(c) {
      c.classList.toggle('selected', c.getAttribute('data-hsort') === key);
    });
    localStorage.setItem('monHeaderSort', key);
  }

  window.setHeaderSort = function(key) { _sortPills(key); };

  window.toggleHeaderColPanel = function() {
    var panel = document.getElementById('headerColPanel');
    var btn = document.getElementById('btnHeaderCols');
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
    panel.style.position = 'absolute';
    panel.style.zIndex = '9999';
    panel.style.visibility = 'hidden';
    panel.style.display = 'block';
    var pw = panel.offsetWidth;
    panel.style.display = 'none';
    panel.style.visibility = '';
    var r = btn.getBoundingClientRect();
    var sy = window.pageYOffset || document.documentElement.scrollTop;
    var sx = window.pageXOffset || document.documentElement.scrollLeft;
    panel.style.top  = (r.bottom + sy + 4) + 'px';
    panel.style.left = Math.max(sx, r.right + sx - pw) + 'px';
    panel.style.right = 'auto';
    panel.style.display = 'block';
  };

  document.addEventListener('DOMContentLoaded', function() {
    _applyHeaderCols(localStorage.getItem('monHeaderCols') || 'all');
    _sortPills(localStorage.getItem('monHeaderSort') || 'natural');
  });

  window.addEventListener('resize', function() {
    if ((localStorage.getItem('monHeaderCols') || 'all') === 'auto') {
      _applyHeaderCols('auto');
    }
  });

  document.addEventListener('click', function(e) {
    var panel = document.getElementById('headerColPanel');
    var btn   = document.getElementById('btnHeaderCols');
    if (panel && panel.style.display === 'block'
        && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
      panel.style.display = 'none';
    }
  });

  window.toggleLegendPanel = function() {
    var panel = document.getElementById('legendPanel');
    var btn   = document.getElementById('btnLegend');
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
    panel.style.position = 'absolute';
    panel.style.zIndex   = '9999';
    panel.style.visibility = 'hidden';
    panel.style.display  = 'block';
    var pw = panel.offsetWidth;
    panel.style.display  = 'none';
    panel.style.visibility = '';
    var r  = btn.getBoundingClientRect();
    var sy = window.pageYOffset || document.documentElement.scrollTop;
    var sx = window.pageXOffset || document.documentElement.scrollLeft;
    panel.style.top  = (r.bottom + sy + 4) + 'px';
    panel.style.left = Math.max(sx, r.right + sx - pw) + 'px';
    panel.style.right = 'auto';
    panel.style.display = 'block';
  };

  document.addEventListener('click', function(e) {
    var panel = document.getElementById('legendPanel');
    var btn   = document.getElementById('btnLegend');
    if (panel && panel.style.display === 'block'
        && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
      panel.style.display = 'none';
    }
  });
})();
</script>
