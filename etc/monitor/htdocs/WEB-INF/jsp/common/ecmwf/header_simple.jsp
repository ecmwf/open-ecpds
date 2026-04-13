<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<nav id="topheader" class="topheader navbar py-0" style="background-color:<%=System.getProperty("monitor.color")%>;">
    <div class="container-fluid px-3 py-2">

        <a class="navbar-brand p-0 me-0 flex-shrink-0" href="/">
            <img src="/assets/images/logo.production.png" alt="Home page" width="140" height="24">
        </a>

        <span class="header_nav_divider"></span>

        <span class="header_simple_title flex-grow-1 text-truncate">
            <tiles:getAsString name="title" />
        </span>

        <div class="d-flex align-items-center gap-2 flex-shrink-0">
            <tiles:insert name="submenu_top" />

            <button id="btnTheme" class="btn btn-sm btn-outline-light p-1 lh-1" onclick="ecpdsToggleTheme()" title="Toggle light/dark theme" style="width:28px;height:28px;">
              <i id="themeIcon" class="bi bi-moon-fill" style="font-size:0.8rem;"></i>
            </button>

            <logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
                <div class="dropdown">
                    <a class="dropdown-toggle text-white text-decoration-none fw-semibold small"
                       href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-person-circle me-1"></i><auth:info property="commonName" />
                        <span class="text-white-50">(<auth:info property="uid" />)</span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="/do/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                        </a></li>
                    </ul>
                </div>
            </logic:present>
        </div>

    </div>
    <div class="w-100 location_simple">
        <tiles:insert name="location" />
    </div>
</nav>
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
