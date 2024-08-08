<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
select {
	padding: 6px 12px 6px 40px;
}
</style>

<form>
	<input class="search" name="search" type="text" placeholder="Search.."
		title="Search is performed against the File Name in case-sensitive"
		value="${param['search']}">
</form>

<tiles:insert name="date.select" />

<c:if test="${empty events}">
	<br />
	<div class="alert">
		<span class="closebtn" onclick="parent.history.back();">&times;</span>
		No Data Events found based on these criteria!
	</div>
</c:if>

<c:if test="${!empty events}">
	<display:table name="${events}" id="event" requestURI=""
		sort="external" defaultsort="6" partialList="true"
		size="${eventsSize}" pagesize="${recordsPerPage}" class="listing">
		<display:column title="Data User" sortable="true">
			<a href="<bean:message key="incoming.basepath"/>/${event.userName}">${event.userName}</a>
		</display:column>
		<display:column title="Destination" sortable="true">
			<a
				href="<bean:message key="destination.basepath"/>/${event.destinationName}">${event.destinationName}</a>
		</display:column>
		<display:column property="transferServerName" title="Transfer Server"
			sortable="true" />

		<display:column property="protocol" title="Protocol" sortable="true" />
		<display:column title="File Name" sortable="true">
			<c:if test="${event.dataTransferId > 0}">
				<a title="Size: ${event.formattedBytes}"
					href="/do/transfer/data/${event.dataTransferId}">${event.fileName}</a>
			</c:if>
			<c:if test="${event.dataTransferId <= 0}">
				<a STYLE="TEXT-DECORATION: NONE"
					title="Size: ${event.formattedBytes}"><font color="red">${event.fileName}</font></a>
			</c:if>
		</display:column>
		<display:column title="Start Time" sortable="true">
			<content:content name="event.startTime"
				dateFormatKey="date.format.transfer" ignoreNull="true" />
		</display:column>
		<display:column title="Finish Time" sortable="false">
			<content:content name="event.finishTime"
				dateFormatKey="date.format.transfer" ignoreNull="true" />
		</display:column>
		<display:column title="Mbits/s" sortable="false" sortProperty="rate">
			<c:if test="${event.rate != 0}">
				<a STYLE="TEXT-DECORATION: NONE"
					title="Rate: ${event.formattedRate}">${event.rate}</a>
			</c:if>
			<c:if test="${event.rate == 0}">
				<font color="grey">[n/a]</font>
			</c:if>
		</display:column>
		<display:column title="Action" sortable="true">
			<c:if test="${event.upload}">upload</c:if>
			<c:if test="${!event.upload}">download</c:if>
		</display:column>
		<display:caption>Events for <auth:link
				basePathKey="incoming.basepath" href="">All Data Users</auth:link>
		</display:caption>
	</display:table>
</c:if>
