<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<div id="bottomfooter" class="bottomfooter">
      <table border=0 height="40" border="0" cellspacing="0" cellpadding="0" width="100%">
        <tr>
          <td bgcolor="#000000" width="30%" align="left" valign="middle"><div class="footer_simple_title"><%=System.getProperty("monitor.title")%> (<%=System.getProperty("monitor.nickName")%>)</div></td>
          <td bgcolor="#000000" align="middle" valign="middle">
              <a href="/do/user/detailer"><img alt="Page Details" title="Page Details" src="/assets/images/webapp/detailer.gif" border="0" width="18" height="18"></a>
          </td>
          <td bgcolor="#000000" width="30%" align="right" valign="middle"><div class="footer_simple_title">v<%=ecmwf.common.version.Version.getVersion()%> <font size=-1>(<%=ecmwf.common.version.Version.getBuild()%>)</font></div></td>
        </tr>
      </table>
</div>
