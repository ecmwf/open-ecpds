<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

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

<div style="width: 600px; padding: 0px">
<h3>Transmission</h3>
<p><%=System.getProperty("monitor.nickName")%> has the concept of Data Transfer. A Data Tranfer correspond to a Transfer Request which is linked to a Data File.</p>
<p>A Data File can be associated with multiple Data Transfers (e.g. a file can be disseminated to multiple places).</p>
<p><%=System.getProperty("monitor.nickName")%> also introduce the concept of Destination. A Destination should be understood as a place where the Transfer Requests are processed for delivery of the Data Files to a remote place.</p>
<p>A Destination implements a Scheduler which can be configured to fine-tune how the Data File is disseminated:<br><li>File Priorities, Parallel Transmissions, Retry Mechanism ...</li></p>
<p>A Destination is given a list of Transfer Hosts (primary and backup Hosts) for the data transmission.</p>
<p>In order to deliver a Data File to a remote place, <%=System.getProperty("monitor.nickName")%> will make use of a Host definition:<br><li>Network, Transfer Protocol, Target Directory, Credentials ...</li></p>
<p>The following options allow configuring the settings for all these entities.</p>

<div class="menu">
<h3 class="menu">Options</h3>
<p>
<ul>
  <auth:link basePathKey="transfer.basepath" href="/data" wrappingTags="li">Data Transfers</auth:link>
  <auth:link basePathKey="transfer.basepath" href="/destination" wrappingTags="li">Destinations</auth:link>
  <auth:link basePathKey="transfer.basepath" href="/host" wrappingTags="li">Transfer Hosts</auth:link>
  <auth:link basePathKey="transfer.basepath" href="/history" wrappingTags="li">Transfer History</auth:link>
  <auth:link basePathKey="transfer.basepath" href="/method" wrappingTags="li">Transfer Methods</auth:link>
  <auth:link basePathKey="transfer.basepath" href="/module" wrappingTags="li">Transfer Modules</auth:link>
</ul>
</div>
</div>
