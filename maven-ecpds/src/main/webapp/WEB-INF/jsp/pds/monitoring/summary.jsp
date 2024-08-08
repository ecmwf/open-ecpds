<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert page="reload.jsp"/>

<table>
	<tr>
	<td colspan="2" bgcolor="#ffffff"></td>
	<td class="monitoring_title"><a class="menuitem" href="#products"><%=System.getProperty("monitor.nickName")%> Mon by Product</a></td>
	<td class="monitoring_title"><a class="menuitem" href="#dragan">Transmission Mon</a></td>
	<td class="monitoring_title"><a class="menuitem" href="#product_step"><%=System.getProperty("monitor.nickName")%> Mon by Tag and Status</a></td>
	<td class="monitoring_title"><a class="menuitem" href="#destinations"><%=System.getProperty("monitor.nickName")%> Mon by Destination</a></td>
	</tr>
</table>

<a name="products">
<h3><%=System.getProperty("monitor.nickName")%> Monitoring Status (Operators) by Product</h3>

<table border="0" cellpadding="2" cellspacing="1">

<tr class="monitoring_title">
	<td colspan="2" bgcolor="#ffffff"></td>
<c:forEach var="tagStatus" items="${tagStatii}" varStatus="status">
	<td bgcolor="#ffffff"></td>
	<td class="monitoring_left" colspan="2"><a class="topnav1" href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>"><c:out value="${tagStatus.name}"/></a></td>
	<td align="center" class="topnav1">
		<a class="topnav1" href="/do/monitoring"><img src="/assets/images/ecpds/a<c:out value="${tagStatus.value.arrivalStatus}"/>.png" border="0" title="Arrival Status"/></a>
	</td>
	<td align="center" class="topnav1">
		<a class="topnav1" href="/do/monitoring"><img src="/assets/images/ecpds/t<c:out value="${tagStatus.value.transferStatus}"/>.png" border="0" title="Transfer Status"/></a>
	</td>
	<c:if test="${(status.index % 9) == 8}">
		</tr><tr class="monitoring_title"><td colspan="2" bgcolor="#ffffff">
	</c:if>
</c:forEach>
</tr>

</table>

<a name="dragan">
<h3>Product Transmission to Dissemination by Product and Step <i>(a.k.a Dragan's Info)</i></h3>

<table border="0" cellpadding="1" cellspacing="1">
<tr class="monitoring_title">
<td colspan="3" bgcolor="#ffffff"></td>
<c:forEach var="step" items="${steps}">		
	<td class="topnav1" align="center"><c:out value="${step}"/></td>
</c:forEach>
</tr>
<c:forEach var="tagStatus" items="${tagStepStatii}">
<tr class="monitoring_title">
		<td bgcolor="#ffffff"></td>
		<td class="monitoring_left"><a class="topnav1" href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>"><c:out value="${tagStatus.name}"/></a></td>
		<td bgcolor="#ffffff"></td>
		<c:forEach var="stepStatus" items="${tagStatus.value}">		
		<td align="center">
			<a class="topnav1" href="/do/monitoring/summary/<c:out value="${tagStatus.name}"/>"><img src="/assets/images/ecpds/a<c:out value="${stepStatus.value.productStatus}"/>.png" width="12" height="12" border="0" title="<c:out value="${stepStatus.value.tag}:${stepStatus.value.step}' Arrival: ${stepStatus.value.productStatusCode}"/> at <content:content name="stepStatus.value.lastUpdate" dateFormatKey="date.format.long.iso" ignoreNull="true"/>"></a>
		</td>
		</c:forEach>
</tr>
</c:forEach>
</table>

<a name="product_step">
<h3><%=System.getProperty("monitor.nickName")%> Monitoring Status (Operators) by Product and Step</h3>

<table border="0" cellpadding="2" cellspacing="1">
<tr>
<td colspan="3" bgcolor="#ffffff"></td>
<c:forEach var="step" items="${steps}">		
	<td colspan="2" class="monitoring_title"><c:out value="${step}"/></td>
</c:forEach>
</tr>

<c:forEach var="tagStatus" items="${tagStepStatii}">

<tr class="monitoring_title">

		<td bgcolor="#ffffff"></td>
		<td class="monitoring_left"><a class="topnav1" href="/do/monitoring/summary/<c:out value='${tagStatus.name}'/>"><c:out value='${tagStatus.name}'/></a></td>
		<td bgcolor="#ffffff"></td>
		
		<c:forEach var="stepStatus" items="${tagStatus.value}">
			<td align="center" class="topnav1">
				<a class="topnav1" href="/do/monitoring"><img src='/assets/images/ecpds/a<c:out value="${stepStatus.value.arrivalStatus}"/>.png' border='0' title='Product <c:out value="${stepStatus.value.tag}:${stepStatus.value.step}"/> <%=System.getProperty("monitor.nickName")%> Arrival Status: ${stepStatus.value.arrivalStatus}'/></a>
			</td>
			<td align="center" class="topnav1">
				<a class="topnav1" href="/do/monitoring"><img src='/assets/images/ecpds/t<c:out value="${stepStatus.value.transferStatus}"/>.png' border='0' title='Product <c:out value="${stepStatus.value.tag}:${stepStatus.value.step}"/> <%=System.getProperty("monitor.nickName")%> Transfer Status: ${stepStatus.value.transferStatus}'/></a>
			</td>
		</c:forEach>

</tr>
</c:forEach>

</table>

<a name="destinations">
<h3><%=System.getProperty("monitor.nickName")%> Monitoring Status (Operators) by Destination</h3>

<table border="0" cellpadding="2" cellspacing="1">

<tr class="monitoring_title">
	<td colspan="2" bgcolor="#ffffff"></td>
<c:forEach var="destination" items="${destinations}" varStatus="status">

		<td bgcolor="#ffffff"></td>
		<td class="monitoring_left" colspan="2"><a class="topnav1" href="<bean:message key="destination.basepath"/>/<c:out value="${destination.name}" />" title="<c:out value="Status for '${destination.name}' products last updated on '${destination.simplifiedMonitoringStatus.calculationDate}'" />"><c:out value="${destination.name}" /></a></td>
		<td align="center" class="topnav1">
			<a class="topnav1" href="/do/monitoring"><img src="/assets/images/ecpds/a<c:out value="${destination.simplifiedMonitoringStatus.arrivalStatus}"/>.png" border="0" title="Arrival Status"/></a>
		</td>
		<td align="center" class="topnav1">
			<a class="topnav1" href="/do/monitoring""><img src="/assets/images/ecpds/t<c:out value="${destination.simplifiedMonitoringStatus.transferStatus}"/>.png" border="0" title="Transfer Status"/></a>
		</td>
		
		<c:if test="${(status.index % 12) == 11}">
			</tr><tr class="monitoring_title"><td colspan="2" bgcolor="#ffffff">
		</c:if>

</c:forEach>
</tr>

</table>

