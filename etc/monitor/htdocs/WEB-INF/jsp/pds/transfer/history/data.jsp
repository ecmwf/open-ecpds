<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="form-info-banner" style="margin-left:0">
	<i class="bi bi-clock-history text-primary flex-shrink-0"></i>
	Transfer History Entry
</div>

<table class="fields">
<!--<tr><th>Id</th><td>${item.id}</td></tr>-->
<tr><th>Date</th><td>${item.date}</td></tr>
<tr><th>Transfer Host</th><td><a href="<bean:message key="host.basepath"/>/${item.hostName}">${item.hostNickName}</a></td></tr>
<tr><th>Data File</th><td><a href="<bean:message key="datafile.basepath"/>/${item.dataTransfer.dataFile.id}">${item.dataTransfer.dataFile.original}</a></td></tr>
<tr><th>Data Transfer</th><td><a href="<bean:message key="datatransfer.basepath"/>/${item.dataTransfer.id}">${item.dataTransfer.target}</a></td></tr>
<tr><th>Status</th><td>${item.formattedStatus}</td></tr>
<tr><th>Sent</th><td>${item.formattedSent}</td></tr>
<tr><th>Comment</th><td>${item.formattedComment}</td></tr>
<tr><th>Error</th><td><c:if test="${item.error}"><i class="bi bi-x-circle-fill text-danger" title="Yes"></i></c:if><c:if test="${!item.error}"><i class="bi bi-check-circle-fill text-success" title="No"></i></c:if></td></tr>
</table>
