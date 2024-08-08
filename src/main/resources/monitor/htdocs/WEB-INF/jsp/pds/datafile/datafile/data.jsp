<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:importAttribute name="isDelete" ignore="true"/>
<c:if test="${not empty isDelete}">
        <tiles:insert page="./pds/datafile/datafile/warning.jsp"/>
</c:if>
<c:if test="${empty isDelete}">

	<table class="fields" border=0>

	<tr><th>Data File Id</th><td colspan="3">${datafile.id}</td></tr>
	<tr><th>Source</th><td colspan="3">${datafile.formattedOriginal}</td></tr>
	<tr><th>Checksum</th><td colspan="3">
		<c:if test="${datafile.checksum == null}">
			<font color="grey"><span title="Checksum not generated">[n/a]</span></font>
		</c:if>
		<c:if test="${datafile.checksum != null}">
			${datafile.checksum}
		</c:if>
	</td></tr>
	<tr><th>Storage Path</th><td colspan="3">${datafile.storagePath}</td></tr>
	<tr><th>Group By</th><td colspan="3">${datafile.groupBy}</td></tr>
	<tr><th>Additional Info</th><td colspan="3">
		<c:if test="${datafile.formattedCaller == null}">
			<font color="grey"><span title="No additional information">[n/a]</span></font>
		</c:if>
		<c:if test="${datafile.formattedCaller != null}">
			${datafile.formattedCaller}
		</c:if>
	</td></tr>
	<tr><th>Index</th><td colspan="3">
	 <c:if test="${datafile.index == 0}">
	   <font color="grey"><span title="Not an index file">[n/a]</span></font>
         </c:if>
	 <c:if test="${datafile.index > 0}">
	   ${datafile.index} file(s)
         </c:if>
	</td></tr>

	<tr><th>Retrieved From</th>
                <c:if test="${datafile.getHost == null}">
			<td colspan="3"><font color=red><i>not-retrieved</i></font></td>
		</c:if>
                <c:if test="${datafile.getHost != null}">
			<td colspan="3">${datafile.getHost} (in ${datafile.getDuration} + ${datafile.getProtocolOverhead} protocol overhead)</td>
		</c:if>
	</tr>

	<tr><th>Remote Host</th>
		<c:if test="${datafile.remoteHost == null}">
	<td colspan="3"><font color=red><i>unknown</i></font></td>
		</c:if>
				<c:if test="${datafile.remoteHost != null}">
			<td colspan="3">${datafile.remoteHost}</td>
		</c:if>
	</tr>
	
		<tr><th>Submitted From</th>
		<c:if test="${datafile.ecauthHost == null}">
	<td colspan="3"><font color="grey"><span title="No additional information">[n/a]</span></font></td>
		</c:if>
				<c:if test="${datafile.ecauthHost != null}">
			<td colspan="3">${datafile.ecauthHost}</td>
		</c:if>
	</tr>
	
		<tr><th>Submitted By</th>
		<c:if test="${datafile.ecauthUser == null}">
	<td colspan="3"><font color="grey"><span title="No additional information">[n/a]</span></font></td>
		</c:if>
				<c:if test="${datafile.ecauthUser != null}">
			<td colspan="3">${datafile.ecauthUser}</td>
		</c:if>
	</tr>

	<tr>
	<th>Product Date</th><td><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</td>
	<th>Earliest</th><td><content:content name="datafile.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</td>
	</tr>
	<tr>
	<th>Generation Date</th><td><content:content name="datafile.productGenerationTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</td>
	<th>Latest</th><td><content:content name="datafile.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</td>
	</tr>
	<tr>
	<th>Arrival Date</th><td><content:content name="datafile.arrivedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;</td>
	<th>Predicted</th><td><content:content name="datafile.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/>&nbsp;</td>
	</tr>

	<tr>
	<th>Timestep</th><td valign="top">${datafile.timeStep}</td>
	<td valign="top" rowspan="5" colspan="2">
		<display:table id="metadata" name="${datafile.metaData}" requestURI="" class="listing">
		  <display:column sortable="true" title="Name"><a href="/do/datafile/metadata/attribute/${metadata.name}">${metadata.name}</a></display:column>
		  <display:column property="value"/>    				
		  <display:caption>Meta Data for ${datafile.id}</display:caption>
		</display:table>
	</td>
	</tr>

	<tr>
	<th>Size</th><td><a STYLE="TEXT-DECORATION: NONE" title="Size: ${datafile.formattedSize}">${datafile.size} bytes</a></td>
	</tr>
	<tr>
	<th>Delete Original</th><td valign="top"><c:if test="${datafile.deleteOriginal}">yes</c:if><c:if test="${!datafile.deleteOriginal}">no</c:if></td>
	</tr>
	<tr>
	<th>Deleted</th><td valign="top"><c:if test="${datafile.deleted}"><font color="red">yes</font></c:if><c:if test="${!datafile.deleted}">no</c:if></td>
	</tr>
	<tr>
	<th>Removed</th><td valign="top"><c:if test="${datafile.removed}"><font color="red">yes</font></c:if><c:if test="${!datafile.removed}">no</c:if></td>
	</tr>

	
	</table>

<display:table name="${datafile.dataTransfers}" id="transfer"  pagesize="35" requestURI="" sort="list" class="listing">

    <display:column title="Destination" sortable="true">	
    		<a title="${transfer.destination.comment}" href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a>
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

	<display:column title="Sched. Time" sortable="true">	
			<content:content name="transfer.scheduledTime" dateFormatKey="date.format.transfer" ignoreNull="true"/>									
	</display:column>
    				
    <display:column title="Target" sortable="true">
    		<a  title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}"><c:if test="${transfer.deleted}"><font color="red"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></font></c:if></a>
    </display:column>
    				
    <display:column title="%" property="progress" sortable="true"/>

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

    <display:column title="Prior" property="priority" sortable="true"/>

    <display:caption>Transfers for this datafile</display:caption>
	
</display:table>
</c:if>
