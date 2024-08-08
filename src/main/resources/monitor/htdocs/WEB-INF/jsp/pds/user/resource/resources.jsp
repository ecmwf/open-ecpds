<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<style>
select {
   padding: 6px 12px 6px 40px;
}   
</style>

<form>
<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Ressource Path in case-insensitive" value="${param['search']}">
</form>

<c:if test="${empty resources}">
<br/>
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Web Resources found based on these criteria!
</div>
</c:if>

<c:if test="${!empty resources}">
<display:table name="${resources}" id="resource" sort="list" pagesize="25" requestURI="" class="listing">
    <display:column title="Resource Path" sortable="true">
    	<!-- ${resource.path} -->
    	<a href="/do/user/resource/${resource.id}">${resource.path}</a>
	</display:column>
	<display:column title="Categories">	
 		<c:forEach var="category" items="${resource.categories}"><a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}">${category.name}</a>&nbsp;</c:forEach>
    </display:column>	
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" basePathKey="accesscontrol.basepath" href="/detailer?page=${resource.id}" imageKey="icon.small.text" imageTitleKey="ecpds.user.detailer"/>
		<auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/update_form/${resource.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/delete_form/${resource.id}" imageKey="icon.small.delete"/>
	</display:column>
</display:table>
</c:if>
