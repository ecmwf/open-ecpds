<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<table class="fields" border=0>

<tr>
<th>Data Transfer Id</th>
<td><c:out value="${datatransfer.id}"/></td>
<th><c:out value="${datatransfer.destination.typeText}"/> Destination</th>
<td><a href="<bean:message key="destination.basepath"/>/<c:out value="${datatransfer.destination.name}"/>"><c:out value="${datatransfer.destination.name}"/></a> &nbsp; (<c:out value="${datatransfer.destination.formattedStatus}"/>)</td>
</tr>

<tr>
<th>Data File Id</th><td><a href="<bean:message key="datafile.basepath"/>/${datatransfer.dataFileId}">${datatransfer.dataFileId}</a></td>
<th>Dissemination Host</th>
<td><a href="/do/transfer/host/${datatransfer.hostName}">${datatransfer.hostNickName}</a></td>
</tr>

<tr>
<th>Transfer Server</th>
<td>
	<auth:if basePathKey="transferserver.basepath" paths="/${datatransfer.transferServerName}">
	<auth:then><a href="/do/datafile/transferserver/${datatransfer.transferServerName}">${datatransfer.transferServerName}</a></auth:then>
	<auth:else>${datatransfer.transferServerName}</auth:else>
	</auth:if>
</td>
<auth:if basePathKey="nonmemberstate.basepath" paths="">
<auth:then><th>Status</th><td class="number"><c:if test="${datatransfer.deleted}"><font color="red"></c:if>${datatransfer.detailedStatus}<c:if test="${datatransfer.deleted}"></font></c:if></td></auth:then>
<auth:else><th>Status</th><td class="number"><c:if test="${datatransfer.deleted}"><font color="red"></c:if>${datatransfer.memberStateDetailedStatus}<c:if test="${datatransfer.deleted}"></font></c:if></td></auth:else>
</auth:if>
</tr>

<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>

<tr>
<th>Product</th><td>${datafile.metaTime}-${datafile.metaStream} ${datafile.metaType} (${datafile.metaTarget})</td>
<th>Progress</th><td>${datatransfer.progress}%</td>
</tr>

<tr>
<th>Expired</th><td><c:if test="${datatransfer.expired}"><font color="red">yes</font></c:if><c:if test="${!datatransfer.expired}">no</c:if></td>
<th>Deleted</th><td class="boolean"><c:if test="${datatransfer.deleted}"><font color="red">yes</font></c:if><c:if test="${!datatransfer.deleted}">no</c:if></td>
</tr>

<tr>
<th>Replicated</th><td class="boolean"><c:if test="${datatransfer.replicated}">yes</c:if><c:if test="${!datatransfer.replicated}"><font color="red">no</font></c:if></td>
<th>Backup</th><td class="boolean"><c:if test="${datatransfer.backup}">yes</c:if><c:if test="${!datatransfer.backup}"><font color="red">no</font></c:if></td>
</tr>

<tr>
<th>Prod Time</th><td colspan="3" class="date"><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
</tr>
<tr>
<th>Comment</th><td colspan="3" class="comment">${datatransfer.formattedComment}</td>
</tr>
<tr>
<th>Original</th>
<td colspan="3">${datafile.formattedOriginal}</td>
</tr>

</auth:then>
</auth:if>

<tr>
<th>Target</th><td colspan="3"><c:if test="${datatransfer.deleted}"><font color="red"></c:if>${datatransfer.target}<c:if test="${datatransfer.deleted}"></font></c:if></td>
</tr>

<c:if test="${not empty showFileSize}">
<tr><th>Size</th><td colspan="3"><a STYLE="TEXT-DECORATION: NONE" title="Size: ${datatransfer.formattedSize}">${datatransfer.size} bytes</a></td></tr>
</c:if>

<tr>
<th>Sent</th><td class="number" colspan="1"><a STYLE="TEXT-DECORATION: NONE" title="Sent: ${datatransfer.formattedSent}">${datatransfer.sent} bytes</a></td>
</tr>

<tr>
<th>Duration</th><td class="number">${datatransfer.formattedDuration}</td>
<th>Priority</th><td class="number">${datatransfer.priority}</td>
</tr>

<tr>
<c:if test="${datatransfer.transferRate > 0}">
  <th>Rate</th><td class="number"><a STYLE="TEXT-DECORATION: NONE" title="Rate: ${datatransfer.formattedTransferRate}">${datatransfer.formattedTransferRateInMBitsPerSeconds} Mbits/s</a></td>
</c:if>
<c:if test="${datatransfer.transferRate <= 0}">
  <c:if test="${datatransfer.size == 0}">
    <th>Rate</th><td class="number"><font color="grey"><span title="Empty file">[n/a]</span></font></td>
  </c:if>
  <c:if test="${datatransfer.size > 0}">
    <th>Rate</th><td class="number"><font color="grey"><span title="Data not transferred to remote host">[n/a]</span></font></td>
  </c:if>
</c:if>
<th>On Proxy</th><td class="boolean"><c:if test="${datatransfer.proxy}"><font color="red">yes</font></c:if><c:if test="${!datatransfer.proxy}">no</c:if></td>
</tr>

<c:set var="schedTimeTitle" value="Sch. Time"/>
<c:if test="${datatransfer.scheduledTime != datatransfer.queueTime}">
	<c:set var="requeued" value="value"/>
	<c:set var="schedTimeTitle" value="Initial Sch. Time"/>
</c:if>

<c:set var="finishTimeTitle" value="Finish Time"/>
<c:if test="${not empty datatransfer.realFinishTime && datatransfer.realFinishTime != datatransfer.finishTime}">
	<c:set var="refinished" value="value"/>
	<c:set var="finishTimeTitle" value="First Finish Time"/>
</c:if>

<tr>
<th><c:out value="${schedTimeTitle}"/></th>
<td class="date"><content:content name="datatransfer.scheduledTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<th>ASAP</th><td class="boolean"><c:if test="${datatransfer.asap}">yes</c:if><c:if test="${!datatransfer.asap}">no</c:if></td>
</tr>

<tr>
<th>Start Time</th><td class="date"><content:content name="datatransfer.startTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<th>Earliest</th><td class="date"><content:content name="datatransfer.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
</auth:then>
</auth:if>
</tr>
	
<tr>
<th>Retry Time</th><td class="date">
<c:if test="${not empty datatransfer.retryTime && datatransfer.startTime != datatransfer.retryTime}">
<content:content name="datatransfer.retryTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/>&nbsp;<b>(S:${datatransfer.startCount}, R:${datatransfer.requeueCount}) </b> 
</c:if>
<c:if test="${empty datatransfer.retryTime || datatransfer.startTime == datatransfer.retryTime}">No retries</c:if>
</td>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<th>Latest</th><td class="date"><content:content name="datatransfer.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
</auth:then>
</auth:if>
</tr>
	
<tr>
<th>${finishTimeTitle}</th><td class="date"><content:content name="datatransfer.realFinishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<th>Predicted</th><td class="date"><content:content name="datatransfer.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
</auth:then>
</auth:if>
</tr>	
	
<tr>
<th>Expiry Date</th><td class="date"><content:content name="datatransfer.expiryDate" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<auth:if basePathKey="datafile.basepath" paths="/${datatransfer.dataFileId}">
<auth:then>
<th>Target Time</th><td class="date"><content:content name="datatransfer.targetTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
</auth:then>
</auth:if>
</tr>

<c:if test="${not empty refinished}">
<tr>
<th>Last Finish Time</th><td class="date"><content:content name="datatransfer.finishTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<td class="date" colspan="2">&nbsp;</td>
</tr>
</c:if>

<c:if test="${not empty requeued}">
<tr>
<th>Real Sch. Time</th><td class="date"><content:content name="datatransfer.queueTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></td>
<td class="date" colspan="2">&nbsp;</td>
</tr>
</c:if>

</table>
