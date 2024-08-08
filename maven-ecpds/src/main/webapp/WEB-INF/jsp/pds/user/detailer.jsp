<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<h2>Path: ${resource.path}</h2>

<h3>Belongs to Categories</h3>

<display:table name="${categories}" id="category" requestURI="" sort="list" class="listing">
  <display:column title="Name"><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></display:column>	
  <display:column property="description" title="Description"/>	
</display:table>

<h3>Users with access</h3>

<display:table name="${users}" id="user" requestURI="" sort="list" class="listing">
  <display:column title="Uid"><a href="<bean:message key="user.basepath"/>/${user.id}">${user.id}</a></display:column>
  <display:column property="commonName"/>	
  <display:column title="Categories">	
     <c:forEach var="category" items="${user.categories}">
	   <b><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></b>,&nbsp; 
	 </c:forEach>
  </display:column>
</display:table>

<h3>Users without access</h3>

<display:table name="${usersNo}" id="user" requestURI="" sort="list" class="listing">
  <display:column title="Uid"><a href="<bean:message key="user.basepath"/>/${user.id}">${user.id}</a></display:column>
  <display:column property="commonName"/>	
  <display:column title="Categories">	
    <c:forEach var="category" items="${user.categories}">
	 <b><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a></b>,&nbsp; 
	</c:forEach>
  </display:column>
</display:table>

