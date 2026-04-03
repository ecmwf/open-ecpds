<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />

<form>
	<input class="search" name="search" type="text" placeholder="Search.."
		title="Search is performed across the Web User, Action and Comment in case-sensitive"
		value="${param['search']}">
</form>

<c:if test="${empty events}">
	<br />
	<div class="alert">
		No Web Event Log available with the criteria selected
	</div>
</c:if>

<c:if test="${!empty events}">
	<p class="fw-bold mb-1 mt-2">Events for <auth:link basePathKey="user.basepath" href="">All Web Users</auth:link></p>
	<display:table name="${events}" id="event" requestURI=""
		sort="external" defaultsort="1" partialList="true"
		size="${eventsSize}" pagesize="${recordsPerPage}" class="listing">
		<display:column property="time" title="Time" sortable="true" />
		<display:column title="Web User" sortable="true">
			<a title="Web User logged in from ${event.activity.host}"
				href="<bean:message key="event.basepath"/>/${event.activity.ECUser.name}">${event.activity.ECUser.name}</a>
		</display:column>
		<display:column property="action" title="Action" sortable="true" />
		<display:column property="comment" title="Comment" sortable="true" />
		<display:column property="name" sortable="false" />
		<display:column property="fileName" title="File Name" sortable="false" />
		<display:column title="Link" sortable="false">
			<c:if
				test="${event.type != '' and event.type != 'lost' and event.type != '(none)'}">
				<a
					href="<bean:message key="${event.type}.basepath"/>/${event.linkId}"><content:icon
						altKey="ecpds.user.event.object"
						titleKey="ecpds.user.event.object" key="icon.small.arrow.right"
						writeFullTag="true" /></a>
			</c:if>
			<c:if test="${event.type == 'lost'}">
				<content:icon altKey="ecpds.user.event.noObject"
					titleKey="ecpds.user.event.noObject" key="icon.small.square"
					writeFullTag="true" />
			</c:if>
		</display:column>
	</display:table>
</c:if>
