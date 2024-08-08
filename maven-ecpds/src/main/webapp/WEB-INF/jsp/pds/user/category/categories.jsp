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
<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Name and Description in case-insensitive" value="${param['search']}">
</form>

<c:if test="${empty categories}">
<br/>
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Web Categories found based on these criteria!
</div>
</c:if>

<c:if test="${!empty categories}">
<display:table name="${categories}" id="category" sort="list" pagesize="25" requestURI="" class="listing">
	<display:column title="Name" sortable="true">	
    		<!-- ${category.name} --><a href="<bean:message key="category.basepath"/>/${category.id}">${category.name}</a>
    </display:column>	
    <display:column property="description" sortable="true"/>
	<display:column title="Resources" sortable="true">	
 		<c:forEach var="resource" items="${category.accessibleResources}" end="10" varStatus="status">
    		<a href="<bean:message key="resource.basepath"/>/${resource.id}">${resource.path}</a>&nbsp;
    		<c:set var="lastCatIndex" value="${status.index}"/>
    	</c:forEach>
    	<c:if test="${lastCatIndex >= 10}">....</c:if>
    </display:column>	
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/user/category/edit/update_form/${category.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/user/category/edit/delete_form/${category.id}" imageKey="icon.small.delete"/>
	</display:column>
</display:table>
</c:if>
