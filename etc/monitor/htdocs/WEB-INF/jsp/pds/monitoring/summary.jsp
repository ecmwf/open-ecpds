<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert page="reload.jsp"/>

<style>
.sum-layout { display: flex; gap: 1.25rem; align-items: flex-start; }
.sum-sidebar {
  position: sticky;
  top: 1rem;
  flex-shrink: 0;
  width: 185px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.sum-sidebar .sum-nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 0.8rem;
  text-decoration: none;
  color: #495057;
  border: 1px solid transparent;
  transition: all 0.15s;
}
.sum-sidebar .sum-nav-link:hover {
  background: #e9ecef;
  border-color: #ced4da;
  color: #212529;
  text-decoration: none;
}
.sum-content { flex: 1; min-width: 0; }
</style>

<div class="sum-layout">

  <%-- Sticky left sidebar --%>
  <nav class="sum-sidebar">
    <a class="sum-nav-link" href="#products">
      <i class="bi bi-grid-3x3-gap-fill"></i><%=System.getProperty("monitor.nickName")%> by Product
    </a>
    <a class="sum-nav-link" href="#dragan">
      <i class="bi bi-send-fill"></i>Transmission Mon
    </a>
    <a class="sum-nav-link" href="#product_step">
      <i class="bi bi-tags-fill"></i>by Tag &amp; Step
    </a>
    <a class="sum-nav-link" href="#destinations">
      <i class="bi bi-geo-alt-fill"></i>by Destination
    </a>
  </nav>

  <%-- Main content --%>
  <div class="sum-content">

    <%-- 1. By Product --%>
    <h5 id="products" class="fw-semibold mt-3 mb-2 text-secondary">
      <i class="bi bi-grid-3x3-gap-fill me-1"></i><%=System.getProperty("monitor.nickName")%> Monitoring Status by Product
    </h5>
    <div class="d-flex flex-wrap gap-2 mb-4">
      <c:forEach var="tagStatus" items="${tagStatii}">
        <a href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>"
           class="d-flex align-items-center gap-2 px-3 py-2 border rounded text-decoration-none text-dark"
           style="background:#f8f9fa; font-size:0.82rem; white-space:nowrap;">
          <span class="mon-dot mon-dot-s${tagStatus.value.arrivalStatus lt 0 ? '0' : tagStatus.value.arrivalStatus}"
                title="Arrival Status: ${tagStatus.value.arrivalStatus}"></span>
          <span class="mon-dot mon-dot-s${tagStatus.value.transferStatus lt 0 ? '0' : tagStatus.value.transferStatus}"
                title="Transfer Status: ${tagStatus.value.transferStatus}"></span>
          <c:out value="${tagStatus.name}"/>
        </a>
      </c:forEach>
    </div>

    <%-- 2. Transmission (Dragan) --%>
    <h5 id="dragan" class="fw-semibold mt-3 mb-2 text-secondary">
      <i class="bi bi-send-fill me-1"></i>Product Transmission to Dissemination by Product and Step
      <small class="text-muted fw-normal ms-1"><i>(a.k.a Dragan's Info)</i></small>
    </h5>
    <div class="table-responsive mb-4">
      <table class="table table-sm table-hover table-bordered align-middle" style="font-size:0.82rem;">
        <thead class="table-light">
          <tr>
            <th class="text-nowrap">Product</th>
            <c:forEach var="step" items="${steps}">
              <th class="text-center text-nowrap"><c:out value="${step}"/></th>
            </c:forEach>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="tagStatus" items="${tagStepStatii}">
            <tr>
              <td class="text-nowrap">
                <a href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>"><c:out value="${tagStatus.name}"/></a>
              </td>
              <c:forEach var="stepStatus" items="${tagStatus.value}">
                <td class="text-center">
                  <a href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>">
                    <span class="mon-dot mon-dot-${stepStatus.value.productStatus lt 0 ? 'n1' : stepStatus.value.productStatus}"
                          title="<c:out value="${stepStatus.value.tag}:${stepStatus.value.step}"/> Arrival: ${stepStatus.value.productStatusCode} at <content:content name="stepStatus.value.lastUpdate" dateFormatKey="date.format.long.iso" ignoreNull="true"/>"></span>
                  </a>
                </td>
              </c:forEach>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <%-- 3. By Tag and Step --%>
    <h5 id="product_step" class="fw-semibold mt-3 mb-2 text-secondary">
      <i class="bi bi-tags-fill me-1"></i><%=System.getProperty("monitor.nickName")%> Monitoring Status by Product and Step
    </h5>
    <div class="table-responsive mb-4">
      <table class="table table-sm table-hover table-bordered align-middle" style="font-size:0.82rem;">
        <thead class="table-light">
          <tr>
            <th rowspan="2" class="align-middle text-nowrap">Product</th>
            <c:forEach var="step" items="${steps}">
              <th colspan="2" class="text-center text-nowrap"><c:out value="${step}"/></th>
            </c:forEach>
          </tr>
          <tr>
            <c:forEach var="step" items="${steps}">
              <th class="text-center" style="font-size:0.7rem; color:#6c757d; font-weight:600;">Arr</th>
              <th class="text-center" style="font-size:0.7rem; color:#6c757d; font-weight:600;">Tfr</th>
            </c:forEach>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="tagStatus" items="${tagStepStatii}">
            <tr>
              <td class="text-nowrap">
                <a href="/do/monitoring/summary/<c:out value='${tagStatus.name}'/>"><c:out value='${tagStatus.name}'/></a>
              </td>
              <c:forEach var="stepStatus" items="${tagStatus.value}">
                <td class="text-center">
                  <a href="/do/monitoring">
                    <span class="mon-dot mon-dot-s${stepStatus.value.arrivalStatus lt 0 ? '0' : stepStatus.value.arrivalStatus}"
                          title="<c:out value="${stepStatus.value.tag}:${stepStatus.value.step}"/> <%=System.getProperty("monitor.nickName")%> Arrival: ${stepStatus.value.arrivalStatus}"></span>
                  </a>
                </td>
                <td class="text-center">
                  <a href="/do/monitoring">
                    <span class="mon-dot mon-dot-s${stepStatus.value.transferStatus lt 0 ? '0' : stepStatus.value.transferStatus}"
                          title="<c:out value="${stepStatus.value.tag}:${stepStatus.value.step}"/> <%=System.getProperty("monitor.nickName")%> Transfer: ${stepStatus.value.transferStatus}"></span>
                  </a>
                </td>
              </c:forEach>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <%-- 4. By Destination --%>
    <h5 id="destinations" class="fw-semibold mt-3 mb-2 text-secondary">
      <i class="bi bi-geo-alt-fill me-1"></i><%=System.getProperty("monitor.nickName")%> Monitoring Status by Destination
    </h5>
    <div class="d-flex flex-wrap gap-2 mb-4">
      <c:forEach var="destination" items="${destinations}">
        <a href="<bean:message key="destination.basepath"/>/<c:out value="${destination.name}"/>"
           class="d-flex align-items-center gap-2 px-3 py-2 border rounded text-decoration-none text-dark"
           style="background:#f8f9fa; font-size:0.82rem; white-space:nowrap;"
           title="Status for '${destination.name}' last updated on '${destination.simplifiedMonitoringStatus.calculationDate}'">
          <span class="mon-dot mon-dot-s${destination.simplifiedMonitoringStatus.arrivalStatus lt 0 ? '0' : destination.simplifiedMonitoringStatus.arrivalStatus}"
                title="Arrival Status: ${destination.simplifiedMonitoringStatus.arrivalStatus}"></span>
          <span class="mon-dot mon-dot-s${destination.simplifiedMonitoringStatus.transferStatus lt 0 ? '0' : destination.simplifiedMonitoringStatus.transferStatus}"
                title="Transfer Status: ${destination.simplifiedMonitoringStatus.transferStatus}"></span>
          <c:out value="${destination.name}"/>
        </a>
      </c:forEach>
    </div>

  </div><%-- end sum-content --%>
</div><%-- end sum-layout --%>

<script>
<%-- Highlight active sidebar link based on scroll position --%>
(function() {
  var sections = ['products','dragan','product_step','destinations'];
  var links = {};
  sections.forEach(function(id) {
    links[id] = document.querySelector('.sum-nav-link[href="#' + id + '"]');
  });
  function onScroll() {
    var scrollY = window.scrollY + 80;
    var active = sections[0];
    sections.forEach(function(id) {
      var el = document.getElementById(id);
      if (el && el.offsetTop <= scrollY) active = id;
    });
    sections.forEach(function(id) {
      if (links[id]) links[id].classList.toggle('active', id === active);
    });
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
})();
</script>
