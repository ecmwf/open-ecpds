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
<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Name in case-insensitive" value="${param['search']}">
</form>

<c:if test="${empty policies}">
<br/>
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Data Policies found based on these criteria!
</div>
</c:if>

<c:if test="${!empty policies}">
<display:table name="${policies}" id="policy" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Name" sortable="true"><a href="<bean:message key="policy.basepath"/>/${policy.id}">${policy.id}</a></display:column>
 	<display:column title="Associated Destinations" sortable="true">
 		<c:forEach var="destination" items="${policy.associatedDestinations}">
    		<a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}">${destination.name}</a>&nbsp;
    	        </c:forEach>
    </display:column>
	<display:column title="Enabled" sortable="true"><c:if test="${policy.active}">yes</c:if><c:if test="${!policy.active}"><font color="red">no</font></c:if></display:column>
	<display:column title="Comment" sortable="false">${policy.comment}</display:column>
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/user/policy/edit/update_form/${policy.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/user/policy/edit/delete_form/${policy.id}" imageKey="icon.small.delete"/>
	</display:column>
</display:table>
</c:if>
