<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/displaytag-el-12.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
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

<tiles:insert name="date.select"/>

<c:if test="${steps == '1'}">
<br/>
<div class="alert">
  No Timeline available for Destination <c:out value="${destination.name}"/> on the <c:out value="${selectedDate}"/>
</div>
</c:if>

<c:if test="${steps != '1'}">

<c:forEach begin="1" end="${steps}" var="step">
	<br/><br/>
	<img 	src="/do/monitoring/timeline/<c:out value="${destination.name}"/>?mode=image&step=<c:out value="${step}"/>&date=<c:out value="${selectedDate}"/>"
			usemap="#step<c:out value="${step}"/>_map"
			border="0"
			alt="Timeline chart, image <c:out value="${step}"/> for destination <c:out value="${destination.name}"/>"
			title="Timeline chart, image <c:out value="${step}"/> for destination <c:out value="${destination.name}"/>"
	/>
	<map name="step<c:out value="${step}"/>_map">
	
		<c:forEach begin="${stepWidth * (step-1)}" end="${stepWidth * step}" var="transfer" items="${datatransfers}" varStatus="status">
		<area 	href="<bean:message key="datatransfer.basepath"/>/<c:out value="${transfer.id}"/>" 
				title="<c:out value="${transfer.formattedStatus} (${transfer.formattedTransferRateInMBitsPerSeconds} Mbits/s): ${transfer.target}"/>"  
				COORDS="<c:out value="0,${((status.count-1)*17)+30},600,${(status.count)*17+30}"/> "/>
		</c:forEach>
	</map>
</c:forEach>
</c:if>

</c:if>
