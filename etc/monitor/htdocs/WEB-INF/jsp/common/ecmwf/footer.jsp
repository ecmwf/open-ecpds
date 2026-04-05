<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<div id="bottomfooter" class="bottomfooter d-flex align-items-center justify-content-between text-white px-3"
     style="background-color:<%=System.getProperty("monitor.color")%>; height:34px; border-top:1px solid rgba(255,255,255,0.15);">
    <div class="footer_simple_title">
        Powered by <a href="https://github.com/ecmwf/open-ecpds" target="_blank" rel="noopener noreferrer" class="fw-semibold">OpenECPDS</a>
    </div>
    <div>
        <%
          // In a Struts Tiles forward chain, request.getRequestURI() returns the layout JSP path,
          // not the original request URI. Check all forward/include attributes to find the real path.
          String _fwdUri   = (String) request.getAttribute("javax.servlet.forward.request_uri");
          String _fwdQuery = (String) request.getAttribute("javax.servlet.forward.query_string");
          String _fwdPath  = (String) request.getAttribute("javax.servlet.forward.servlet_path");
          String _anyUri   = (_fwdUri != null ? _fwdUri : "") + (_fwdPath != null ? _fwdPath : "") + request.getRequestURI();
          boolean _showDetails = session.getAttribute(ecmwf.web.model.users.User.SESSION_KEY) != null
                                 && !_anyUri.contains("/detailer");
          if (_showDetails) {
              String _refUrl = (_fwdUri != null ? _fwdUri : request.getRequestURI());
              if (_fwdQuery != null && !_fwdQuery.isEmpty()) _refUrl += "?" + _fwdQuery;
              String _encoded = java.net.URLEncoder.encode(_refUrl, "UTF-8");
        %>
        <a href="/do/user/detailer?ref=<%=_encoded%>">
            <i class="bi bi-info-circle text-white-50"></i>
        </a>
        <% } %>
    </div>
    <div class="footer_simple_title">
        <span class="text-white-50">v</span><%=ecmwf.common.version.Version.getVersion()%>
        <span class="text-white-50 small ms-1">(<%=ecmwf.common.version.Version.getBuild()%>)</span>
    </div>
</div>
