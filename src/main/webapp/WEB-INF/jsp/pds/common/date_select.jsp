<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<table class="select">
	<tr>
		<c:if test="${not empty dateOptions}">
			<c:forEach items="${dateOptions}" var="dateOption">
				<c:if test="${dateOption == selectedDate}">
					<td class="selected"><a
						href="?mode=${param['mode']}&date=${dateOption}">${dateOption}</a></td>
				</c:if>
				<c:if test="${dateOption != selectedDate}">
					<td><a href="?mode=${param['mode']}&date=${dateOption}">${dateOption}</a></td>
				</c:if>
			</c:forEach>
		</c:if>

		<c:if test="${empty dateOptions}">
			<td>Please supply a "dateOptions" attribute from the request</td>
		</c:if>

		<tiles:importAttribute name="show_chart_button" ignore="true" />

		<c:if test="${not empty show_chart_button}">
			<td><c:if test="${param['mode'] == 'chart'}">
					<a href="?mode=table&date=${selectedDate}"><content:icon
							key="icon.small.chart.off" writeFullTag="true"
							altKey="ecpds.monitoring.showTable"
							titleKey="ecpds.monitoring.showTable" /></a>
				</c:if> <c:if test="${param['mode'] != 'chart'}">
					<a href="?mode=chart&date=${selectedDate}"><content:icon
							key="icon.small.chart" writeFullTag="true"
							altKey="ecpds.monitoring.showChart"
							titleKey="ecpds.monitoring.showChart" /></a>
				</c:if></td>
		</c:if>
	</tr>
</table>
