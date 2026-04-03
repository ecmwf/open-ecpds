<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<div id="bottomfooter" class="bottomfooter d-flex align-items-center justify-content-between text-white px-3"
     style="background-color:<%=System.getProperty("monitor.color")%>; height:34px; border-top:1px solid rgba(255,255,255,0.15);">
    <div class="footer_simple_title">
        Powered by <a href="https://github.com/ecmwf/open-ecpds" target="_blank" rel="noopener noreferrer" class="fw-semibold">OpenECPDS</a>
    </div>
    <div>
        <a href="/do/user/detailer" title="Page Details">
            <i class="bi bi-info-circle text-white-50"></i>
        </a>
    </div>
    <div class="footer_simple_title">
        <span class="text-white-50">v</span><%=ecmwf.common.version.Version.getVersion()%>
        <span class="text-white-50 small ms-1">(<%=ecmwf.common.version.Version.getBuild()%>)</span>
    </div>
</div>
