<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<form>
<div class="input-group input-group-sm mb-2" style="max-width:400px">
<span class="input-group-text"><i class="bi bi-search"></i></span>
<input class="form-control" name="search" type="text" placeholder="Search.." title="Search is performed across the Host, Action and Comment in case-insensitive" value="${param['search']}">
</div>
</form>

<tiles:insert name="date.select"/>

<c:if test="${empty events}">
<br/>
<div class="alert">
  No Data Events found based on these criteria!
</div>
</c:if>

<c:if test="${!empty events}">
<p class="fw-bold mb-1 mt-2">Events for <auth:link basePathKey="user.basepath" href="/${user.id}">Web User ${user.uid}</auth:link> on ${selectedDate}</p>
<display:table name="${events}" id="event" sort="list" pagesize="25" requestURI="" class="listing">
    <display:column property="time" title="Time" sortable="true"/>
    <display:column property="activity.host" title="Host" sortable="true"/>
    <display:column property="action" title="Action" sortable="true"/>
    <display:column property="comment" title="Comment" sortable="false"/>
    <display:column title="" sortable="false">
    <c:if test="${event.type != '' and event.type != 'lost'}">
        <c:set var="eventBasepath" value=""/>
        <c:catch><c:set var="eventBasepath"><bean:message key="${event.type}.basepath"/></c:set></c:catch>
        <c:if test="${not empty eventBasepath}">
        	<a href="${eventBasepath}/${event.linkId}"><content:icon altKey="ecpds.user.event.object" titleKey="ecpds.user.event.object" key="icon.small.arrow.right" writeFullTag="true"/></a>
        </c:if>
    </c:if>
    <c:if test="${event.type == 'lost'}">
    	<content:icon altKey="ecpds.user.event.noObject" titleKey="ecpds.user.event.noObject" key="icon.small.square" writeFullTag="true"/>
    </c:if>
    </display:column>
</display:table>
</c:if>
