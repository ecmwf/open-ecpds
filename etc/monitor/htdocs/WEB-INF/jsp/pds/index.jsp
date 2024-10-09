<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<style>
p {
  Font-size: 14px;
}
.menu h3 {
  font-size: 14pt;
  font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
  margin: 0;
}

.menu ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
}

.menu li {
  font-size: 12pt;
  font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
  border-bottom: 1px solid #ccc;
}

.menu li:last-child {
  border: none;
  padding: 0px 0px 14px 0px;
}

.menu li a {
  font-weight: normal;
  text-decoration: none;
  color: #000;
  display: block;
  width: 300px;
}
.menu li a:hover {
  font-weight: bold;
  text-decoration: none;
}
h3 {
        font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
        Font-size: 16pt;
        width: 100%;
}
li {
  font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
  font-size: 14px;
}
</style>

<div style="width: 600px; padding: 0px;">
<h3>Data-Retrieval / Data-Distribution / Data-Portal</h3>
<p>The <%=System.getProperty("monitor.title")%> (<%=System.getProperty("monitor.nickName")%>) is a persistent repository which allow:<br><li>Retrieving Observational Data from Data Providers.</li><li>Distributing Meteorological Products to our Member States and other Destinations.</li></p>
<p>Data Retrieval and Data Distribution can be initiated by <%=System.getProperty("monitor.nickName")%> using various protocols such as:<li>ftp, sftp, ftps, http/s, Amazon S3, Microsoft Azure, dissftp and ECaccess/ECtrans.</li></p>
<p><%=System.getProperty("monitor.nickName")%> also implement a Data Portal which is accessible via:<li>ftp, https and Amazon S3.</li></p>
<p>Data User credentials are required to access this service.</p>
<h3>Your Options</h3>
<p>You are currently logged as <auth:info property="commonName"/> (<auth:info property="uid"/>) and your profile gives you access to the options below.</p>
<p>If you feel you should have access to more items please contact your administrator.</p>
<form action="/do/login" method="POST">

<div class="menu">
  <auth:link basePathKey="data.basepath" href=""><h3 class="menu">Data Storage</h3></auth:link>
  <ul>
  <auth:link basePathKey="datafile.basepath" href="" wrappingTags="li">Data Files</auth:link>
  <auth:link basePathKey="metadata.basepath" href="" wrappingTags="li">Meta Datas</auth:link>
  <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="li">Transfer Groups</auth:link>
  <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="li">Transfer Servers</auth:link>
  <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="li">Retrieval Rates</auth:link>
  </ul>

  <auth:link basePathKey="transfer.basepath" href=""><h3 class="menu">Transmission</h3><ul></auth:link>
  <ul>
  <auth:link basePathKey="datatransfer.basepath" href="" wrappingTags="li">Data Transfers</auth:link>
  <auth:link basePathKey="destination.basepath" href="?destinationType=-1" wrappingTags="li">Destinations</auth:link>
  <auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
    		<auth:link basePathKey="destination.basepath" href="?destinationType=-2" wrappingTags="li">Dissemination</auth:link>
    		<auth:link basePathKey="destination.basepath" href="?destinationType=-3" wrappingTags="li">Acquisition</auth:link>
	</auth:then>
  </auth:if>
  <auth:link basePathKey="host.basepath" href="" wrappingTags="li">Transfer Hosts</auth:link>
  <auth:link basePathKey="transferhistory.basepath" href="" wrappingTags="li">Transfer History</auth:link>
  <auth:link basePathKey="method.basepath" href="" wrappingTags="li">Transfer Methods</auth:link>
  <auth:link basePathKey="module.basepath" href="" wrappingTags="li">Transfer Modules</auth:link>
  </ul>

  <auth:link basePathKey="accesscontrol.basepath" href=""><h3 class="menu">Access Control</h3></auth:link>
  <ul>
  <auth:link basePathKey="user.basepath" href="" wrappingTags="li">Web Users</auth:link>
  <auth:link basePathKey="category.basepath" href="" wrappingTags="li">Web Categories</auth:link>
  <auth:link basePathKey="resource.basepath" href="" wrappingTags="li">Web Resources</auth:link>
  <auth:link basePathKey="event.basepath" href="" wrappingTags="li">Web Event Log</auth:link>
  <auth:link basePathKey="incoming.basepath" href="" wrappingTags="li">Data Users</auth:link>
  <auth:link basePathKey="policy.basepath" href="" wrappingTags="li">Data Policies</auth:link>
  <auth:link basePathKey="history.basepath" href="" wrappingTags="li">Data Event Log</auth:link>
  </ul>

  <auth:link basePathKey="admin.basepath" href=""><h3 class="menu">Admin Tasks</h3></auth:link>
  <ul>
  <auth:link basePathKey="admin.basepath" href="/filter" wrappingTags="li">Compress Files</auth:link>
  <auth:link basePathKey="admin.basepath" href="/requeue" wrappingTags="li">Outstanding Files</auth:link>
  <auth:link basePathKey="admin.basepath" href="/upload" wrappingTags="li">Upload Files</auth:link>
  </ul>

  <auth:if basePathKey="transferhistory.basepath" paths="/">
  	<auth:then>
		<auth:link basePathKey="monitoring.basepath" href=""><h3 class="menu">Monitoring</h3></auth:link>
                <ul>
		<li><a href="/maps/maps.html">Hosts on Map</a></li>
		<auth:link basePathKey="monitoring.basepath" href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&" wrappingTags="li">Dissemination</auth:link>
		<auth:link basePathKey="monitoring.basepath" href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&" wrappingTags="li">Acquisition</auth:link>
                </ul>
	</auth:then>
  </auth:if>
</div>
</div>
