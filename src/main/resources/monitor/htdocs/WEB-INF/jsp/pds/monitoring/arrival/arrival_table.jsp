<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select">
	<tiles:put name="show_chart_button">true</tiles:put>
</tiles:insert>

<br/>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>

<c:if test="${empty datatransfers}">
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Data Transfers found based on these criteria!
</div>
</c:if>

<c:if test="${!empty datatransfers}">
<display:table id="transfer" name="${datatransfers}" requestURI="" class="listing">

	<display:column title="Original" sortable="true">
		<a href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
	</display:column>
	
	<display:column property="dataFile.timeStep" title="TS" sortable="true"/>

	<display:column title="Target">
		<content:content name="transfer.arrivalTargetTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Predicted"> 
		<content:content name="transfer.arrivalPredictedTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Arrival"> 
		<b><content:content name="transfer.dataFile.arrivedTime" dateFormatKey="date.format.time" ignoreNull="true"/></b>
	</display:column>
	<display:column title="Scheduled"> 
		<content:content name="transfer.scheduledTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Status">
		<img src="<bean:message key='image.arrival.status.${transfer.arrivalStatus}'/>" border="0" height="12" /> &nbsp; (${transfer.arrivalStatus})
	</display:column>

</display:table>
</c:if>

</auth:then>
</auth:if>
