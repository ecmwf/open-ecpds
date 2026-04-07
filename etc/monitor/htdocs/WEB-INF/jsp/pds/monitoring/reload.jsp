<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<script>
	// Refresh the page with the given period. Set additional reloads in case the server is down.
	var refresh = ${monSesForm.refreshPeriod};
	setTimeout(function() {
		window.location.reload(true);
	}, refresh * 1000);
</script>

<style>
.prod-header { 
  display: grid; 
  gap: 6px; 
  flex-grow: 1;
  min-width: 1400px;
}
.prod-pill {
  display: flex; align-items: center; justify-content: flex-start; gap: 8px;
  padding: 4px 10px;
  background: #f8f9fa; border-radius: 6px;
  font-size: 0.8rem; white-space: nowrap;
  text-decoration: none; color: #495057;
  border: 1px solid #dee2e6;
  transition: all 0.15s ease-in-out;
}
.prod-pill:hover { background: #e9ecef; border-color: #ced4da; color: #212529; text-decoration: none; }
.prod-pill.active { background: #0d6efd; color: #fff; border-color: #0a58ca; font-weight: 600; box-shadow: 0 2px 4px rgba(13,110,253,0.2); }
.prod-pill.active:hover { background: #0b5ed7; color: #fff; }

.mon-updated { font-size: 0.9rem; font-weight: 600; color: #343a40; }
.page-nav { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.page-nav .page-label { font-size: 0.78rem; color: #6c757d; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; }
.page-nav .page-pill {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 2px 8px; border-radius: 4px;
  font-size: 0.78rem; background: #f8f9fa; color: #495057;
  text-decoration: none; border: 1px solid #dee2e6;
  width: 45px;
  transition: all 0.15s;
}
.page-nav .page-pill:hover { background: #e9ecef; border-color: #ced4da; color: #212529; text-decoration: none; }
.page-nav .page-pill.active { background: #0d6efd; color: #fff; border-color: #0a58ca; font-weight: 600; }
</style>

<div class="d-flex flex-wrap gap-3 align-items-start mb-2">

  <%-- Product status grid --%>
  <div class="prod-header" style="grid-template-columns: repeat(${fn:length(reqData.productWindow)}, 1fr);">
    <c:forEach var="pro" items="${reqData.productWindowHeader}">
      <c:set var="isSelected" value="${not empty productStatus && productStatus.product==pro.product && productStatus.time==pro.time}"/>
      <c:set var="dotClass" value="mon-dot mon-dot-${pro.generationStatus lt 0 ? 'n1' : pro.generationStatus}"/>
      <a href="/do/monitoring/summary/${pro.product}/${pro.time}"
         class="prod-pill ${isSelected ? 'active' : ''}"
         title="Scheduled for ${pro.scheduledTime} - Status: ${pro.generationStatusFormattedCode}">
        <span class="${dotClass}"></span>${pro.time}-${pro.product}
      </a>
    </c:forEach>
  </div>

  <%-- Refresh controls and page nav — all on one line --%>
  <div class="d-flex flex-row align-items-center flex-wrap gap-2 flex-shrink-0">
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
    </div>
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

<script>
document.querySelectorAll('.mon-refresh-pill').forEach(function(pill) {
  pill.addEventListener('click', function(e) {
    e.preventDefault();
    var params = new URLSearchParams(window.location.search);
    params.set('refreshPeriod', this.dataset.value);
    window.location.href = '?' + params.toString();
  });
});
</script>
