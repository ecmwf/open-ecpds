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

<div style="width: 600px; padding: 0px;">
<h3>Access Control</h3>
<p><%=System.getProperty("monitor.nickName")%> maintain two different types of users:<br><li>Web Users to access the <%=System.getProperty("monitor.nickName")%> Monitoring Interface.<li>Data Users to access the <%=System.getProperty("monitor.nickName")%> Data Portal.</li></p>
<p>The following options allow managing and controlling these ressources.</p>
<div class="menu">
<h3 class="menu">Options</h3>
<p>
<ul>
  <auth:link basePathKey="user.basepath" href="" wrappingTags="li">Web Users</auth:link>
  <auth:link basePathKey="category.basepath" href="" wrappingTags="li">Web Categories</auth:link>
  <auth:link basePathKey="resource.basepath" href="" wrappingTags="li">Web Resources</auth:link>
  <auth:link basePathKey="event.basepath" href="" wrappingTags="li">Web Event Log</auth:link>
  <auth:link basePathKey="incoming.basepath" href="" wrappingTags="li">Data Users</auth:link>
  <auth:link basePathKey="policy.basepath" href="" wrappingTags="li">Data Policies</auth:link>
  <auth:link basePathKey="history.basepath" href="" wrappingTags="li">Data Event Log</auth:link>
</ul>
</div>
</div>
