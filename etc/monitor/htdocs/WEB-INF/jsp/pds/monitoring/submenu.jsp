<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
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

<c:if test="${authorized == 'true'}">

<table class="spareBox2">
<tr><th>
<auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
		<a href="/do/monitoring"><i class="bi bi-eye"></i> Monitoring</a></th></tr>
	</auth:then>
	<auth:else><i class="bi bi-eye"></i> Monitoring</auth:else>
</auth:if>
<tr><td></td></tr>	
<auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
		<tr><td><a href="/do/monitoring/arrival/${destination.name}/${product}/${time}?date=${param['date']}"><i class="bi bi-broadcast-pin"></i> ${destination.name} ${time}-${product} Arrivals</a></td></tr>
		<tr><td><a href="/do/monitoring/transfer/${destination.name}/${product}/${time}?date=${param['date']}"><i class="bi bi-arrow-left-right"></i> ${destination.name} ${time}-${product} Transfers</a></td></tr>
	</auth:then>
</auth:if>
</table>

</c:if>
