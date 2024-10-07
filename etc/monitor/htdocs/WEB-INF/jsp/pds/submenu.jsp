<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<table class="spareBox2">
	      <tr>
		<th><a href="/"><%=System.getProperty("monitor.nickName")%> Home</a></th>
	      </tr>
	      <tr>
		<td></td>
	      </tr>
              <tr>
                <td><auth:link basePathKey="data.basepath" href="">Data Storage</auth:link></td>
              </tr>
              <tr>
                <td><auth:link basePathKey="transfer.basepath" href="">Transmission</auth:link></td>
              </tr>
              <tr>
                <td><auth:link basePathKey="accesscontrol.basepath" href="">Access Control</auth:link></td>
              </tr>
              <tr>
                <td><auth:link basePathKey="admin.basepath" href="">Admin Tasks</auth:link></td>
              </tr>
	      <auth:if basePathKey="transferhistory.basepath" paths="/">
	      	<auth:then>
	      		<tr>
                	  <td><auth:link basePathKey="monitoring.basepath" href="">Monitoring</auth:link></td>
              		</tr>
		</auth:then>
	      </auth:if>
</table>
