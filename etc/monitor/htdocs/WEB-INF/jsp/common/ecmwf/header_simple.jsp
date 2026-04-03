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
