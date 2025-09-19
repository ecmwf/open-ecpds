<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
select {
	padding: 6px 12px 6px 40px;
}
</style>

<tiles:insert name="date.select" />
<br>
<tiles:insert name="destination.select" />
<br>
&nbsp;

<c:if test="${historyItemsSize == '0'}">
	<div class="alert">
		No Transfer History available for Destination
		<c:out value="${selectedDestination.name}" />
		on the
		<c:out value="${selectedDate}" />
	</div>
</c:if>

<c:if test="${historyItemsSize != '0'}">
	<br />&nbsp;
<display:table id="history" name="${historyItems}" requestURI=""
		sort="external" defaultsort="2" partialList="true"
		size="${historyItemsSize}" pagesize="${recordsPerPage}"
		class="listing">

		<display:column sortable="true" title="Err" style="padding-right:30px;">
			<c:if test="${history.error}">
				<content:icon key="icon.micro.cancel" writeFullTag="true" />
			</c:if>
			<c:if test="${not history.error}">
				<content:icon key="icon.micro.submit" writeFullTag="true" />
			</c:if>
		</display:column>
		<display:column title="Event Time" sortable="true" sortProperty="id">
			<a
				href="<bean:message key="transferhistory.basepath"/>/${history.id}"><content:content
					name="history.date" dateFormatKey="date.format.transfer"
					ignoreNull="true" /></a>
		</display:column>
		<display:column property="formattedStatus" title="Status"
			sortable="true" />
		<display:column title="Transfer Host" sortable="true">
			<c:if test="${history.hostName != null}">
				<a href="<bean:message key="host.basepath"/>/${history.hostName}">${history.hostNickName}</a>
			</c:if>
			<c:if test="${history.hostName == null}">
				<font color="grey"><span
					title="Data not transferred to remote host">[n/a]</span></font>
			</c:if>
		</display:column>
		<display:column title="Comment" property="formattedComment" />
	</display:table>
</c:if>
