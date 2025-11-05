<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
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

<c:if test="${authorized == 'true'}">

	<auth:if basePathKey="monitoring.basepath" paths="">
		<auth:then>

			<c:if test="${not empty destinationDetailActionForm}">
				<c:set var="date" value="${destinationDetailActionForm.date}" />
			</c:if>
			<c:if test="${empty destinationDetailActionForm}">
				<c:set var="date" value="${param['date']}" />
			</c:if>
			<c:set var="mode" value="${param['mode']}" />

			<table class="editSpareBox">

				<tr>
					<th colspan="3"><c:set var="desName" value="${destination.id}" />
						<c:if test="${fn:length(destination.id) > 14}">
							<c:set var="desName" value="${fn:substring(desName,0,10)} ..." />
						</c:if> <a title="${destination.name}"
						href='<bean:message key="destination.basepath"/>/${destination.id}'>${desName}</a>
						<span>(${destination.formattedStatus})</span></th>
				</tr>

				<tr>
					<td colspan="3"></td>
				</tr>

				<auth:if basePathKey="transferhistory.basepath" paths="/">
					<auth:then>
						<tr>
							<td valign="top"><search:icon key="icon.config"
									writeFullTag="true" /></td>
							<td colspan="2"><a
								href="<bean:message key="destination.basepath"/>/${destination.id}?mode=parameters">Parameters</a>
							</td>
						</tr>
					</auth:then>
				</auth:if>

				<tr>
					<td valign="top"><search:icon key="icon.konqueror"
							writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="destination.basepath"/>/${destination.id}?mode=traffic">Data
							Rates</a></td>
				</tr>

				<tr>
					<td valign="top"><search:icon key="icon.konqueror"
							writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="destination.basepath"/>/${destination.id}?mode=changelog">Changes
							Log</a></td>
				</tr>

				<tr>
					<td valign="top"><search:icon key="icon.arrow.left"
							writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="destination.basepath"/>/${destination.id}?mode=aliasesfrom">Aliased
							From</a></td>
				</tr>

				<tr>
					<td valign="top"><search:icon key="icon.arrow.right"
							writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="destination.basepath"/>/${destination.id}?mode=aliasesto">Aliases
							To</a></td>
				</tr>

				<tr>
					<td valign="top"><search:icon key="icon.arrow.right"
							writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="incoming.basepath"/>?destinationNameForSearch=${destination.id}">Data
							Users</a></td>
				</tr>

				<tr>
					<td><search:icon key="icon.small.timeline" writeFullTag="true" /></td>
					<td colspan="2"><a
						href="<bean:message key="monitoring.timeline.basepath"/>/${destination.id}?date=${date}">Transfer
							Timeline</a></td>
				</tr>
				<auth:if basePathKey="transferhistory.basepath" paths="/">
					<auth:then>
						<tr>
							<td><search:icon key="icon.book" writeFullTag="true" /></td>
							<td colspan="2"><a
								href="<bean:message key="transferhistory.basepath"/>?destinationName=${destination.id}&date=${date}">Transfer
									History</a></td>
						</tr>
					</auth:then>
				</auth:if>


				<auth:if basePathKey="destination.basepath"
					paths="/metadata/${destination.id}">
					<auth:then>
						<tr>
							<td><search:icon key="icon.attachment" writeFullTag="true" /></td>
							<td colspan="2"><a
								href="<bean:message key="destination.basepath"/>/metadata/${destination.id}">Metadata</a></td>
						</tr>
					</auth:then>
				</auth:if>

				<tr>
					<td colspan="3"></td>
				</tr>

				<c:if test="${destination.monitoringStatus.present}">

					<c:if test="${empty time}">
						<c:set var="time" value="00" />
					</c:if>

					<c:if test="${not empty products}">
						<tr>
							<td colspan="3">Products</td>
						</tr>
						<c:forEach var="product" items="${products}">
							<c:set var="key" value="${product.name}@${product.value}" />
							<c:if test="${destination.statusMapForProducts[key].present}">
								<tr>
									<auth:if basePathKey="transferhistory.basepath" paths="/">
										<auth:then>
											<td><a title="Arrival Status"
												href="/do/monitoring/arrival/${destination.id}/${product.name}/${product.value}?mode=${mode}&date=${date}"><img
													src="<bean:message key="image.arrival.status.${destination.statusMapForProducts[key].arrivalStatus}"/>"
													border="0"></a></td>
										</auth:then>
									</auth:if>
									<td><a title="Transfer Status"
										href="/do/monitoring/transfer/${destination.id}/${product.name}/${product.value}?mode=${mode}&date=${date}"><img
											src="<bean:message key="image.transfer.status.${destination.statusMapForProducts[key].transferStatus}"/>"
											border="0"></a></td>
									<td>${product.value}-${product.name}</td>
								</tr>
							</c:if>
						</c:forEach>
					</c:if>
				</c:if>
				<c:if test="${not empty times}">
					<tr>
						<td colspan="3"></td>
					</tr>
					<tr>
						<td colspan="3">Times</td>
					</tr>
					<tr>
						<td colspan="3"><c:forEach var="time" items="${times}">
								<a href="${time}?mode=${mode}&date=${date}">${time}</a>/
	</c:forEach></td>
					</tr>
				</c:if>
			</table>


			<auth:if basePathKey="transferhistory.basepath" paths="/">
				<auth:then>
					<table class="editSpareBox">
						<tr>
							<td><a title="Go to the Monitoring Display"
								href="<bean:message key="monitoring.basepath"/>">Monitoring</a></td>
						</tr>
					</table>
				</auth:then>
			</auth:if>

		</auth:then>
	</auth:if>

</c:if>
