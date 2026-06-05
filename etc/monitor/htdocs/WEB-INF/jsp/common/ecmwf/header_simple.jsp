<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<nav id="topheader" class="topheader navbar py-0" style="background-color:<%=System.getProperty("monitor.color")%>;">
    <div class="container-fluid d-flex align-items-center gap-2 px-3 py-2">

        <c:set var="smw"><tiles:getAsString name="submenu_width" ignore="true"/></c:set>
        <c:if test="${not empty smw and smw != '0'}">
        <div class="d-flex align-items-center gap-2 flex-shrink-0">
            <button class="btn btn-sm btn-outline-light p-0 flex-shrink-0" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#sidebarMenu" aria-controls="sidebarMenu"
                    title="Toggle navigation" style="width:28px;height:28px;line-height:1;">
                <i class="bi bi-list" style="font-size:1.2rem;"></i>
            </button>
            <span class="header_nav_divider"></span>
        </div>
        </c:if>

        <a class="navbar-brand p-0 me-0 flex-shrink-0" href="/">
            <img src="/assets/images/logo.production.png" alt="Home page" width="140" height="24" style="display:block;">
        </a>

        <span class="header_nav_divider"></span>

        <span class="header_simple_title flex-grow-1 text-truncate d-none d-sm-inline">
            <tiles:getAsString name="title" />
        </span>

        <div class="d-flex align-items-center gap-2 flex-shrink-0 ms-auto">
            <tiles:insert name="submenu_top" />

            <logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
                <div class="dropdown">
                    <a class="dropdown-toggle text-white text-decoration-none fw-semibold small"
                       href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-person-circle me-1"></i><span class="d-none d-sm-inline"><auth:info property="commonName" /><span class="text-white-50"> (<auth:info property="uid" />)</span></span><span class="d-inline d-sm-none"><auth:info property="uid" /></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="/do/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                        </a></li>
                    </ul>
                </div>
                <span class="header_nav_divider"></span>
            </logic:present>

            <button id="btnTheme" class="btn btn-sm btn-outline-light p-1 lh-1" onclick="ecpdsToggleTheme()" title="Toggle light/dark theme" style="width:28px;height:28px;">
              <i id="themeIcon" class="bi bi-moon-fill" style="font-size:0.8rem;"></i>
            </button>
        </div>

    </div>
    <div class="w-100 location_simple">
        <div><tiles:insert name="location" /></div>
        <a class="btn-about" role="button" data-bs-toggle="modal" data-bs-target="#aboutModal" title="About OpenECPDS">
            <i class="bi bi-info-circle" style="font-size:0.85rem;"></i>
        </a>
    </div>
</nav>

<!-- About modal (outside <nav> to render at correct z-index) -->
<div class="modal fade" id="aboutModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header border-0 pb-0">
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body text-center px-4 pt-0 pb-4">
        <img src="/assets/images/OpenECPDS.svg" alt="OpenECPDS" class="img-fluid mb-3" style="max-height:80px;">
        <p class="text-muted small mb-2">Our mission with OpenECPDS is to keep data moving!</p>
        <ul class="list-unstyled mb-0" style="font-size:0.82rem; line-height:2;">
          <li><span class="badge bg-primary bg-opacity-10 text-primary fw-normal px-2">Inspired by operational excellence</span></li>
          <li><span class="badge bg-primary bg-opacity-10 text-primary fw-normal px-2">Powered by open-source innovation</span></li>
          <li><span class="badge bg-success bg-opacity-10 text-success fw-normal px-2">Acquire from anywhere</span></li>
          <li><span class="badge bg-success bg-opacity-10 text-success fw-normal px-2">Deliver everywhere</span></li>
          <li><span class="badge bg-info bg-opacity-10 text-info fw-normal px-2">Connect with confidence</span></li>
          <li><span class="badge bg-info bg-opacity-10 text-info fw-normal px-2">Share without limits</span></li>
        </ul>
      </div>
    </div>
  </div>
</div>
<script>
function ecpdsToggleTheme() {
  var t = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-bs-theme', t);
  localStorage.setItem('ecpds-theme', t);
  var ic = document.getElementById('themeIcon');
  if (ic) ic.className = t === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
  if (typeof ecpdsUpdateAceTheme === 'function') ecpdsUpdateAceTheme(t);
}
(function(){
  var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
  var ic = document.getElementById('themeIcon');
  if (ic) ic.className = t === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
}());
</script>
