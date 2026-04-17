<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
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
	<tr><th>Storage Path</th><td colspan="3">
		<c:if test="${datafile.storagePath == null}">
			<font color="grey"><span title="Not retrieved">[n/a]</span></font>
		</c:if>
		<c:if test="${datafile.storagePath != null}">
			${datafile.storagePath}
		</c:if>
	</td></tr>
	<tr><th>Group By</th><td colspan="3">
		<c:if test="${datafile.groupBy == null}">
			<font color="grey"><span title="No additional information">[n/a]</span></font>
		</c:if>
		<c:if test="${datafile.groupBy != null}">
			${datafile.groupBy}
		</c:if>
	</td></tr>
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
	<c:if test="${datafile.groupBy == null}">
	   <tr><th>Pushed To</th>
                   <c:if test="${datafile.getHost == null}">
			   <td colspan="3"><font color=red><i>not-pushed</i></font></td>
		   </c:if>
                   <c:if test="${datafile.getHost != null}">
			   <td colspan="3">${datafile.getHost} (in ${datafile.getDuration} + ${datafile.getProtocolOverhead} protocol overhead)</td>
		   </c:if>
	   </tr>
	</c:if>
	<c:if test="${datafile.groupBy == null}">
	   <tr><th>Retrieved From</th>
	               <c:if test="${datafile.getHost == null}">
			   <td colspan="3"><font color=red><i>not-retrieved</i></font></td>
		   </c:if>
	               <c:if test="${datafile.getHost != null}">
			   <td colspan="3">${datafile.getHost} (in ${datafile.getDuration} + ${datafile.getProtocolOverhead} protocol overhead)</td>
		   </c:if>
	   </tr>
	</c:if>	   
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
	<th>Timestep</th><td colspan="3">${datafile.timeStep}</td>
	</tr>
	<tr>
	<th>Size</th><td><a STYLE="TEXT-DECORATION: NONE" title="Size: ${datafile.formattedSize}">${datafile.size} bytes</a></td>
	<th>Delete Original</th><td><c:if test="${datafile.deleteOriginal}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!datafile.deleteOriginal}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>
	<tr>
	<th>Deleted</th><td><c:if test="${datafile.deleted}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!datafile.deleted}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	<th>Removed</th><td><c:if test="${datafile.removed}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!datafile.removed}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
	</tr>

	</table>

<p class="fw-bold mb-1 mt-3">Meta Data for ${datafile.id}</p>
<c:if test="${empty datafile.metaData}">
<div class="alert alert-info mt-1">No Meta Data for ${datafile.id}</div>
</c:if>
<c:if test="${not empty datafile.metaData}">
<table id="metadataTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
	<thead class="table-light">
		<tr>
			<th>Name</th>
			<th>Value</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach var="metadata" items="${datafile.metaData}">
		<tr>
			<td><a href="/do/datafile/metadata/attribute/${metadata.name}">${metadata.name}</a></td>
			<td>${metadata.value}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<script>
$(document).ready(function() {
    $('#metadataTable').DataTable({ paging: false, searching: false, ordering: true, info: false });
});
</script>
</c:if>

<p class="fw-bold mb-1 mt-3">Transfers for this datafile</p>
<table id="datafileTransfersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Destination</th>
            <th>Transfer Host</th>
            <th>Sched. Time</th>
            <th>Target</th>
            <th>%</th>
            <th>Mbits/s</th>
            <th>Prior</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="transfer" items="${datafile.dataTransfers}">
        <c:set var="nickName" value="${transfer.hostNickName}" />
        <jsp:useBean id="nickName" type="java.lang.String" />
        <tr>
            <td><a title="${transfer.destination.comment}" href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a></td>
            <td>
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
            </td>
            <td><content:content name="transfer.scheduledTime" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
            <td><a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}"><c:if test="${transfer.deleted}"><font color="red"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></font></c:if></a></td>
            <td>${transfer.progress}</td>
            <td>
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
            </td>
            <td>${transfer.priority}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#datafileTransfersTable').DataTable({
        paging:    true,
        pageLength: 25,
        searching: true,
        ordering:  true,
        info:      true,
        order:     [[2, 'asc']]
    });
});
</script>
</c:if>
