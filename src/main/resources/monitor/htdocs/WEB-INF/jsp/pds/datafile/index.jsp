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
}
 
.menu li a {
  text-decoration: none;
  color: #000;
  display: block;
  width: 300px;   
}
.menu li a:hover {
  text-decoration: underline;
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
<h3>Data Storage</h3>
<p><%=System.getProperty("monitor.nickName")%> allow storing, retrieving and deleting Data Files (with their associated Metadata) in Transfer Servers.</p>
<p>Data Files are protected in <%=System.getProperty("monitor.nickName")%> by storing multiple copies of them across several Transfer Servers. If one Transfer Server fails, the Data File can be retrieved from another Transfer Server.</p>
<p>The Data Files are replicated among Transfer Servers belonging to a common Transfer Group.</p>
<p>Transfer Groups are organized in Clusters. Within a Cluster, the Transfer Groups belong to a common Network (e.g. Internet, RMDCN, LAN).</p>

<div class="menu">
<h3 class="menu">Options</h3>
<p>
<ul>
<auth:link basePathKey="datafile.basepath" href="" wrappingTags="li">Data Files</auth:link>   
  <auth:link basePathKey="metadata.basepath" href="" wrappingTags="li">Meta Datas</auth:link>  
  <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="li">Transfer Groups</auth:link>  
  <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="li">Transfer Servers</auth:link>  
  <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="li">Retrieval Rates</auth:link>  
</ul>
</div>
</div>
