<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<style>
select {
   padding: 6px 12px 6px 40px;
}
</style>

<form>
	<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Name in case-insensitive" value="${param['search']}">
	<select name="destinationNameForSearch" onchange="form.submit()" title="With access to (Data Policies not taken into account)">
		<c:forEach var="option" items="${destinationOptions}">
			<option value="${option.name}" <c:if test="${destinationNameForSearch == option.name}">SELECTED</c:if>>${option.name}</option>
		</c:forEach>
	</select>	
</form>

<c:if test="${empty users}">
<br/>
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Data Users found based on these criteria!
</div>
</c:if>

<c:if test="${!empty users}">
	<display:table name="${users}" id="user" requestURI="" sort="list" pagesize="25" class="listing">
		<display:column title="Data Login" sortable="true"><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></display:column>
		<display:column title="Comment" sortable="true">${user.comment}</display:column>
		<display:column title="Country" sortable="true">${user.country.name}</display:column>
		<display:column title="Enabled" sortable="true"><c:if test="${user.active}">yes</c:if><c:if test="${!user.active}"><font color="red">no</font></c:if></display:column>
		<display:column title="TOTP" sortable="true"><c:if test="${user.isSynchronized}">yes</c:if><c:if test="${!user.isSynchronized}">no</c:if></display:column>
		<display:column title="Anonymous" sortable="true">
	    	<c:if test="${user.anonymous}">
	        	<font color="red"><b>yes</b></font>
	    	</c:if>
	    	<c:if test="${!user.anonymous}">
	        	no
	    	</c:if>
		</display:column>
		<display:column title="Sessions" sortable="true">${fn:length(user.incomingConnections)}</display:column>
    	<display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/user/incoming/edit/update_form/${user.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/user/incoming/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
		</display:column>
	</display:table>
</c:if>
