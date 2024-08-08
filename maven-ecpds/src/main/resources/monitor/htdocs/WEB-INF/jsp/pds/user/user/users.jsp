<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<style>
select {
   padding: 6px 12px 6px 40px;
}
</style>

<form>
<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Uid and Name in case-insensitive" value="${param['search']}">
</form>

<c:if test="${empty users}">
<br/>
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Web Users found based on these criteria!
</div>
</c:if>

<c:if test="${!empty users}">
<display:table name="${users}" id="user" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Web Login"	sortable="true"><a href="<bean:message key="user.basepath"/>/${user.id}">${user.id}</a></display:column>
	<display:column title="Comment" sortable="true"><a href="<bean:message key="user.basepath"/>/${user.id}">${user.commonName}</a></display:column>
	<display:column title="Enabled" sortable="true">
		<c:if test="${user.active}">yes</c:if><c:if test="${!user.active}"><font color="red">no</font></c:if>
	</display:column>
 	<display:column title="Categories" sortable="true">
 		<c:forEach var="category" items="${user.categories}">
    		<a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}">${category.name}</a>&nbsp;
    	</c:forEach>
    </display:column>
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/user/user/edit/update_form/${user.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/user/user/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
	</display:column>
</display:table>
</c:if>
