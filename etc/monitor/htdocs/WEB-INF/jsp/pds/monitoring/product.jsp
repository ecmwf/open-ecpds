<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
table.listing td {
	padding: 1pt;
	padding-right: 12pt;
}

table.fields {
	border: 1pt solid #d5d5d5;
}

table.fields td {
	padding: 1pt;
}
</style>

<tiles:insert page="./pds/monitoring/reload.jsp" />

<script>
	function setHrefForSendingEmail(anchor, bcc, subject, body) {
		try {
			anchor.href = 'https://outlook.office365.com/mail/deeplink/compose'
					+ '?subject=' + escape(subject) + '&body=' + body
					+ '&from=' + escape('operators@ecmwf.int') + '&to='
					+ escape('operators@ecmwf.int') + '?bcc=' + bcc;

		} catch (e) {
			alert("Error" + e);
		}
	}
</script>

<c:if test="${productStatus.calculated}">
	<table class="fields">
		<tr>
			<th>Product Name</th>
			<td><a
				href="<bean:message key="monitoring.basepath"/>/summary/${productName}/${productStatus.time}">${productStatus.time}-${productName}</a>
				<c:if test="${not empty step and not empty type}">, Step <u>${step}</u>, Type <u>${type}</u>
				</c:if></td>
			<th>Product Time</th>
			<td>${productStatus.productTime}</td>
			<th>Scheduled</th>
			<td>${productStatus.scheduledTime}</td>
		</tr>
		<tr>
			<th>Last Update</th>
			<td>${productStatus.lastUpdate}</td>
			<th>Arrival</th>
			<td>${productStatus.arrivalTime}</td>
			<th>Status</th>
			<td><img width="12" height="12"
				src="/assets/images/ecpds/g${productStatus.generationStatus}.png"
				border="0"
				title="(${productStatus.generationStatus}) - <bean:message key="ecpds.monitoring.productStatus.${productStatus.generationStatus}"/>">
				${productStatus.generationStatusFormattedCode}</td>
	</table>
	<table>
		<tr>
			<td style="vertical-align: top"><c:if test="${empty step}">
					<c:set var="arrival">Arrival</c:set>
				</c:if> <c:if test="${not empty step}">
					<c:set var="arrival">Update</c:set>
				</c:if>
				<table class="listing">
					<tr>
						<th>T</th>
						<th>Step</th>
						<th>Type</th>
						<th>Notification</th>
						<th>${arrival}</th>
						<th>Schedule</th>
						<th>Before</th>
						<th></th>
					</tr>

					<c:forEach var="stepStatus" items="${productStepStatii}"
						varStatus="status">

						<c:if test="${(status.count % 2) > 0 }">
							<tr class="even">
						</c:if>
						<c:if test="${(status.count % 2) == 0 }">
							<tr class="odd">
						</c:if>

						<td>${stepStatus.time}</td>
						<td><a title="See history for Product, Time, Step and Type"
							href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">${stepStatus.step}</a></td>
						<td><a title="See history for Product, Time, Step and Type"
							href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">${stepStatus.type}</a></td>

						<td>${stepStatus.generationStatusFormattedCode}</td>
						<td><c:if test="${not empty stepStatus.arrivalTime}">
								<content:content name="stepStatus.arrivalTime"
									dateFormatKey="date.format.long.iso" ignoreNull="true"
									defaultValue="***" />
							</c:if> <c:if
								test="${empty stepStatus.arrivalTime and empty step and stepStatus.generationStatusCode!=''}">
								<i>Didn't Arrive Yet</i>
							</c:if> <c:if
								test="${empty step and stepStatus.generationStatusCode==''}">
								<i>Notification Missing</i>
							</c:if> <c:if test="${empty stepStatus.arrivalTime and not empty step}">
								<i><content:content name="stepStatus.lastUpdate"
										dateFormatKey="date.format.long.iso" ignoreNull="true"
										defaultValue="***" /></i>
							</c:if></td>
						<td><content:content name="stepStatus.scheduledTime"
								dateFormatKey="date.format.long.iso" ignoreNull="true"
								defaultValue="***" /></td>
						<td><c:if
								test="${(empty step or stepStatus.generationStatusCode=='DONE') and stepStatus.generationStatusCode!=''}">
		${stepStatus.minutesBeforeSchedule}m
		</c:if></td>

						<td><c:if
								test="${empty step or stepStatus.generationStatusCode=='DONE'}">
								<img width="12" height="12"
									src="/assets/images/ecpds/g${stepStatus.generationStatus}.png"
									border="0"
									title="(${stepStatus.generationStatus}) - <bean:message key="ecpds.monitoring.productStatus.${stepStatus.generationStatus}"/>">
							</c:if></td>
						</tr>

						<c:if
							test="${(status.index % stepsPerColumn)==(stepsPerColumn - 1) && !status.last}">
				</table></td>
			<td style="vertical-align: top">
				<table class="listing">
					<tr>
						<th>T</th>
						<th>Step</th>
						<th>Type</th>
						<th>Notification
						</td>
						<th>${arrival}</th>
						<th>Schedule
						</td>
						<th>Before
						</td>
						<th>
						</td>
					</tr>

					</c:if>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
</c:if>

<c:if test="${!productStatus.calculated}">
	<div class="alert">
		<span class="closebtn" onclick="parent.history.back();">&times;</span>
		No information about this product and cycle yet!
	</div>
</c:if>

</br>

<c:set var="key" value="${productStatus.product}@${productStatus.time}" />
<c:set var="emails" value="${reqData.contacts[key]}" />
<c:set var="ECMWFProductsDelay"
	value="Dear colleagues%2C%0A%3C%3C Due to if known%2C please give some information of the reason for the%0Adelay%2C the or otherwise The %3E%3E dissemination of ECMWF %3C%3Catmospheric or%0Awave%3E%3E products for the %3C%3C00%2C 06%2C 12 or 18%3E%3EZ cycle of the%0A%3C%3C%22high-resolution forecast%22 or %22BC high-resolution forecast%22 or %22ensemble forecast%22 or %22Limited-area wave forecast%22%3E%3E will be delayed.%0A%0AAs soon as we have further details we will inform you.%0A%0AFor more up to date information%2C you may refer to ECMWF service status%0Apage at http%3A%2F%2Fwww.ecmwf.int%2Fen%2Fservice-status .%0A%0AOur sincere apologies for the inconvenience caused by this delay.%0A%0AKind regards%0A%0AECMWF Duty Manager" />
<c:set var="ECMWFProducts"
	value="Dear colleagues,%0D%0A%0D%0AI am pleased to inform you that the problems we encountered earlier%0D%0Awithin the operational production have been resolved and the dissemination of products has started.%0D%0A%0D%0AOur sincere apologies for the inconvenience caused by this delay.%0D%0A%0D%0AKind regards%0D%0A%0D%0AECMWF Duty Manager%0D%0A" />
<c:choose>
	<c:when test="${not empty emails}">
		<a title="Open Outlook" style="text-decoration: none" target="_blank"
			id="delayEmail">Products Delay Email for
			${productStatus.time}-${productStatus.product}</a>&nbsp;
    	<a title="Open Outlook" style="text-decoration: none"
			target="_blank" id="productEmail">Products Email for
			${productStatus.time}-${productStatus.product}</a>
		<script>
			setHrefForSendingEmail(
					document.getElementById('delayEmail'),
					'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
					'ECMWF Products Delay (${productStatus.time}-${productStatus.product})',
					'${ECMWFProductsDelay}');
			setHrefForSendingEmail(
					document.getElementById('productEmail'),
					'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
					'ECMWF Products (${productStatus.time}-${productStatus.product})',
					'${ECMWFProducts}');
		</script>
	</c:when>
	<c:otherwise>
	</c:otherwise>
</c:choose>
