<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

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
		<auth:if basePathKey="destination.basepath"
			paths="/${destination.name}">
			<auth:then>
				<c:set var="authorized" value="true" />
			</auth:then>
		</auth:if>
	</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
	<div class="alert">
		<span class="closebtn" onclick="parent.history.back();">&times;</span>
		Error retrieving object by key <- Problem searching by key
		'${destination.name}' <- Destination not found: {${destination.name}}
	</div>
</c:if>

<c:if test="${authorized == 'true'}">
	<c:if test="${empty transfers}">
		<div class="alert">
			<span class="closebtn" onclick="parent.history.back();">&times;</span>
			No Outstanding Files found
		</div>
	</c:if>
	<c:if test="${!empty transfers}">
		<display:table name="${transfers}" id="transfer" pagesize="25"
			requestURI="" class="listing">
			<display:column title="Dest" sortable="true">
				<a
					href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a>
			</display:column>
			<display:column property="hostNickName" title="Host" sortable="true" />
			<display:column title="Target" sortable="true">
				<a href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
			</display:column>
			<display:column property="formattedStatus" title="Status" />
			<display:column title="%" property="progress" sortable="true" />
			<display:column title="B/s" property="transferRate" sortable="true" />
			<display:column property="priority" sortable="true" />
			<display:column property="comment" sortable="true" />
			<display:caption>Unsuccessful Data Transfers</display:caption>
		</display:table>
		<table border=0>
			<tr>
				<td><auth:link basePathKey="admin.basepath"
						href="/requeue?restart=true" imageKey="icon.requeue"
						imageTitleKey="ecpds.destination.requeue"
						imageAltKey="ecpds.destination.requeue" /></td>
				<td width="15"></td>
				<td><auth:link basePathKey="admin.basepath"
						href="/delete?delete=true" imageKey="icon.delete"
						imageTitleKey="ecpds.destination.delete"
						imageAltKey="ecpds.destination.delete" /></td>
			</tr>
		</table>
	</c:if>
</c:if>
