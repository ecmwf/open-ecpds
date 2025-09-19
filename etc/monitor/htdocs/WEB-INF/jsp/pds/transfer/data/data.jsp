<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<c:set var="authorized" value="true" />
</auth:then>
<auth:else>

<auth:if basePathKey="destination.basepath" paths="/${datatransfer.destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>

</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
<p><font color=red><font size=-1>

Error retrieving object by key <- DataBase problem searching by key '${datatransfer.id}' <- DataTransfer not found: {${datatransfer.id}}

</font></font></p>

</c:if>

<c:if test="${authorized == 'true'}">

<table border=0>
<tr>
<td valign="top">
<jsp:include page="./pds/transfer/data/data_table.jsp"/>
</td>
</tr>

<tr>
<td>

<auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
		<c:set var="transferHistory" value="${datatransfer.transferHistory}"/>
		<c:set var="canSeeHistoryDetail" value="true"/>
	</auth:then>
	<auth:else><c:set var="transferHistory" value="${datatransfer.transferHistoryAfterScheduledTime}"/></auth:else>
</auth:if>

<c:if test="${historyItemsSize == '0'}">
	<div class="alert">
		No Transfer History available for this Data Transfer
	</div>
</c:if>

<c:if test="${historyItemsSize != '0'}">
<display:table id="history" name="${historyItems}" requestURI="" sort="external" defaultsort="2"
        partialList="true" size="${historyItemsSize}" pagesize="${recordsPerPage}" class="listing">

<display:column sortable="true" title="Err" style="padding-right:30px;"> 
    <c:if test="${history.error}"><content:icon key="icon.micro.cancel" writeFullTag="true"/></c:if>
    <c:if test="${not history.error}"><content:icon key="icon.micro.submit" writeFullTag="true"/></c:if>  
  </display:column>   		
	
    <display:column title="Event Time" sortable="true">
   		<!-- ${history.date} -->
   		<c:if test="${not empty canSeeHistoryDetail}">
   			<a href="<bean:message key="transferhistory.basepath"/>/${history.id}"> <content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/></a>
   		</c:if>
   		<c:if test="${empty canSeeHistoryDetail}">
			 <content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/>
   		</c:if>
    </display:column>   		    		
    <display:column property="formattedStatus" title="Status" sortable="true"/>    		    				
    <display:column title="Transfer Host" sortable="true">
       <c:if test="${history.hostName != null}">
		<a href="<bean:message key="host.basepath"/>/${history.hostName}">${history.hostNickName}</a>
       </c:if>
       <c:if test="${history.hostName == null}">
                <font color="grey"><span title="Data not transferred to remote host">[n/a]</span></font>
       </c:if>
    </display:column>    
    <display:column title="Comment" property="formattedComment" />

    <display:caption>Transfer History</display:caption>

</display:table>
</c:if>

<br/>

<display:table name="${datatransfer.olderTransfersForSameDataFile}" id="transfer" sort="list" pagesize="25" requestURI=""
	class="listing" defaultsort="3" defaultorder="descending">

    <display:column title="Destination" sortable="true">
    		<a href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a>
    </display:column>

	<display:column title="Transfer Host" sortable="true">
		<c:set var="nickName" value="${transfer.hostNickName}" />
		<jsp:useBean id="nickName" type="java.lang.String" />
		<c:if test='<%="".equals(nickName)%>'>
			<font color="grey"><span title="Data not transferred to remote host">[not-transferred]</span></font>
		</c:if>
		<c:if test="<%=nickName.length()>0%>">
			<c:if test="${transfer.transferServerName == null}">
				<a href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
			</c:if>
                	<c:if test="${transfer.transferServerName != null}">
				<a title="Transmitted through ${transfer.transferServerName}" href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
			</c:if>
		</c:if>
	</display:column>
	<display:column title="Sched. Time" sortable="true" sortProperty="scheduledTime">
		<content:content name="transfer.scheduledTime"
			dateFormatKey="date.format.transfer" ignoreNull="true" />
	</display:column>

	<display:column title="Start Time" sortable="true" sortProperty="startTime">
		<c:if test="${transfer.startTime != null}">
			<content:content name="transfer.startTime" dateFormatKey="date.format.transfer" ignoreNull="true" />
		</c:if>
		<c:if test="${transfer.startTime == null}">
        		<font color="grey"><span title="Data not transferred to remote host">[n/a]</span></font>
		</c:if>
	</display:column>
	<display:column title="Finish Time" sortable="true" sortProperty="realFinishTime">
		<c:if test="${transfer.realFinishTime != null}">
			<content:content name="transfer.realFinishTime" dateFormatKey="date.format.transfer" ignoreNull="true" />
		</c:if>
		<c:if test="${transfer.realFinishTime == null}">
        		<font color="grey"><span title="Data not transferred to remote host">[n/a]</span></font>
		</c:if>
	</display:column>

	<display:column title="Target" sortable="true">
		<c:if test="${transfer.id != datatransfer.id}">
			<a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}"><c:if test="${transfer.deleted}"><font color="red"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></font></c:if></a>
		</c:if>
		<c:if test="${transfer.id == datatransfer.id}">
			<a STYLE="TEXT-DECORATION: NONE" title="Size: ${transfer.formattedSize}"><c:if test="${transfer.deleted}"><font color="red"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></font></c:if></a>
		</c:if>
	</display:column>

	<display:column property="dataFile.timeStep" title="TS" sortable="true" />
	<display:column title="%" property="progress" sortable="true" />

        <display:column title="Mbits/s" sortable="true" sortProperty="formattedTransferRateInMBitsPerSeconds">
                <c:if test="${transfer.transferRate != 0}">
        		<a STYLE="TEXT-DECORATION: NONE" title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
		</c:if>
                <c:if test="${transfer.transferRate == 0}">
                        <c:if test="${transfer.size != 0}">
                                <font color="grey"><span title="Data not transferred to remote host">[n/a]</span></font>
                        </c:if>
                        <c:if test="${transfer.size == 0}">
                                <font color="grey"><span title="Empty file">[n/a]</span></font>
                        </c:if>
		</c:if>
        </display:column>


    <display:column property="formattedStatus" title="Status">
	<display:caption>
			All Data Transfers with the same identity. &nbsp;&nbsp;&nbsp;<i>(${datatransfer.identity})</i>
	</display:caption>
        <c:if test="${transfer.deleted}"><font color="red"></c:if>${transfer.formattedStatus}<c:if test="${transfer.deleted}"></font></c:if>
    </display:column> 

    <display:column property="priority" title="Prior" sortable="true" />
	
</display:table>

</td>
</tr>
</table>

</c:if>
