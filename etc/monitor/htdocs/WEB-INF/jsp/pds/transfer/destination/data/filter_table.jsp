<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>

<table class="select">

<c:if test="${not empty disseminationStreamOptionsWithSizes}">
<tr>
<th width="55">Dissem_Str</th>
<th width="2"></th>
	<c:set var="disseminationStreamNow" value="${destinationDetailActionForm.disseminationStream}"/>		
	<c:forEach var="disseminationStream" items="${disseminationStreamOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${disseminationStream.name == disseminationStreamNow}">class="selected"</c:if>>
			<a href="javascript:setDisseminationStream('${disseminationStream.name}');">${disseminationStream.name}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${disseminationStream.name == disseminationStreamNow}">class="selected"</c:if>>
			<a title="Size: ${disseminationStream.formattedSize}" href="javascript:setDisseminationStream('${disseminationStream.name}');">
				(${disseminationStream.value})
			</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>
		</c:if>
	</c:forEach>
</tr>
</c:if>

<c:if test="${not empty dataStreamOptionsWithSizes}">
<tr>
<th>Data_Str</th>
<th></th>
	<c:set var="dataStreamNow" value="${destinationDetailActionForm.dataStream}"/>		
	<c:forEach var="dataStream" items="${dataStreamOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${dataStream.second == dataStreamNow}">class="selected"</c:if>>
			<a href="javascript:setDataStream('${dataStream.second}');">${dataStream.first}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${dataStream.second == dataStreamNow}">class="selected"</c:if>>
			<a title="Size: ${dataStream.formattedSize}" href="javascript:setDataStream('${dataStream.second}');">(${dataStream.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>		
		</c:if>		
	</c:forEach>
</tr>
</c:if>

<c:if test="${not empty dataTimeOptionsWithSizes}">
<tr>
<th>Base_Time</th>
<th></th>
	<c:set var="dataTimeNow" value="${destinationDetailActionForm.dataTime}"/>
	<c:forEach var="dataTime" items="${dataTimeOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${dataTime.first == dataTimeNow}">class="selected"</c:if>>
			<a href="javascript:setDataTime('${dataTime.second}');">${dataTime.first}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${dataTime.first == dataTimeNow}">class="selected"</c:if>>
			<a title="Size: ${dataTime.formattedSize}" href="javascript:setDataTime('${dataTime.second}');">(${dataTime.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>
		</c:if>
	</c:forEach>
</tr>
</c:if>

<tr>
<th>Status</th>
<th></th>
	<c:set var="statusNow" value="${destinationDetailActionForm.status}"/>
	<c:forEach var="status" items="${statusOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${status.first == statusNow}">class="selected"</c:if>>
			<a href="javascript:setStatus('${status.first}')">${status.second}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${status.first == statusNow}">class="selected"</c:if>>
			<a title="Size: ${status.formattedSize}" href="javascript:setStatus('${status.first}')">(${status.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>			
		</c:if>		
	</c:forEach>
</tr>

<tr>
<th>Prod_Date</th>
<th></th>
	<c:set var="dateNow" value="${destinationDetailActionForm.date}"/>
	<c:forEach var="date" items="${dateOptions}" >
		<td colspan="2" width="80" <c:if test="${date.name == dateNow}">class="selected"</c:if>>
			<a href="javascript:setDate('${date.name}')">${date.value}</a>
		</td>
	</c:forEach>
</tr>

<tr>	
<th></th>
<th></th>
 <td colspan="18">
 	<input type="text" value="${destinationDetailActionForm.fileNameSearch}" placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i" class="search" title="Default search is by target. Conduct extended searches using target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules." style="width:100%" id="fileNameSearch" name="fileNameSearch" onkeypress="submitenter(this,event)">
</td>
</tr>

</table>
