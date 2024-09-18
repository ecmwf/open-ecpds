<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select">
	<tiles:put name="show_chart_button">true</tiles:put>
</tiles:insert>


<br/>
<img src="/do/monitoring/arrival/${destination.name}/${product}/${time}?mode=image&date=${selectedDate}"
	 usemap="#transfer_map"
	 border="0"/>
	 
    <map name="transfer_map">
		<c:forEach var="transfer" items="${datatransfers}" varStatus="status">
		<area 	href="<bean:message key="datafile.basepath"/>/${transfer.dataFileId}" 
				title="Data File ${transfer.dataFileId} file ${transfer.target}"  
				COORDS="0,${((status.count-1)*17)+30},600,${(status.count)*17+30} "/>
		</c:forEach>
	</map>	 

<br/>

<display:table id="transfer" name="${datatransfers}" requestURI="" class="listing">

	<display:column title="Original" sortable="true">
		<a href="<bean:message key="datafile.basepath"/>/${transfer.dataFileId}">${transfer.target}</a>
	</display:column>
	<display:column property="dataFile.timeStep" title="TS" sortable="true"/>

	<display:column title="Earliest">
		<content:content name="transfer.arrivalEarliestTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
	<display:column title="Latest">
		<content:content name="transfer.arrivalLatestTime" dateFormatKey="date.format.time" ignoreNull="true"/>
	</display:column>
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
	<display:column title="Status" > 
		<img src="<bean:message key="image.arrival.status.${transfer.arrivalStatus}"/>" border="0" height="12"> &nbsp; (${transfer.arrivalStatus})
	</display:column>
	
</display:table>

