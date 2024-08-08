<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>

<auth:if basePathKey="destination.basepath" paths="/${destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
  <div class="alert">
    <span class="closebtn" onclick="parent.history.back();">&times;</span>
    Error retrieving object by key <- Problem searching by key '${destination.name}' <- Destination not found: {${destination.name}}
  </div>
</c:if>

<c:if test="${authorized == 'true'}">

<tiles:insert name="date.select">
	<tiles:put name="show_chart_button">true</tiles:put>
</tiles:insert>

</br>

<c:if test="${empty datatransfers}">
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
  No Data Transfers found based on these criteria!
</div>  
</c:if>

<c:if test="${!empty datatransfers}">  
<display:table id="transfer" name="${datatransfers}" requestURI="" class="listing">

	<display:column title="Target" sortable="true">
			<a href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
	</display:column>
	<display:column property="dataFile.timeStep" title="TS" sortable="true"/>

	<display:column title="Scheduled"> 
		<content:content name="transfer.scheduledTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Target">
		<content:content name="transfer.transferTargetTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Predicted"> 
		<content:content name="transfer.transferPredictedTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Finished"> 
		<b><content:content name="transfer.finishTime" dateFormatKey="date.format.time" ignoreNull="true"/></b>
	</display:column>
	<display:column title="Status"> 
		<img src="<bean:message key='image.transfer.status.${transfer.transferStatus}'/>" border="0" height="12" title="Transfer Status ${transfer.transferStatus}"> &nbsp; (${transfer.formattedStatus})	
	</display:column>
	
</display:table>
</c:if>

</c:if>
