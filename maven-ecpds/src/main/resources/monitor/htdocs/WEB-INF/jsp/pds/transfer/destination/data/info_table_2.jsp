<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table class="select" style="width: 700px">

	<tr>
		<th>Status</th>
		<c:set var="desStatus" value="${destination.formattedStatus}" />
		<c:if test="${desStatus != 'Initialized'}">
			<td>${destination.formattedStatus}</td>
		</c:if>
		<c:if test="${desStatus == 'Initialized'}">
			<td style="background-color: #f44336"><font
				title="Need to be started in order to process" color="white">${destination.formattedStatus}</font>
			</td>
		</c:if>

		<th>Last Transfer</th>
		<td><c:set var="transL" value="${destination.lastTransfer}" /> <c:if
				test="${not empty transL}">
				<c:catch>
					<a
						title="Click to see details of last transfer which occured at <content:content name="transL.finishTime" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>"
						href="<bean:message key="datatransfer.basepath"/>/${transL.id}"><content:content
							name="transL.finishTime" dateFormatKey="date.format.time.short"
							defaultValue="Not Set" ignoreNull="true" /></b></a>
				</c:catch>
			</c:if> <c:if test="${empty transL}">
				<i>None</i>
			</c:if></td>

		<th>Last Error</th>
		<td><c:set var="transE" value="${destination.lastError}" /> <c:if
				test="${not empty transE}">
				<c:catch>
					<a class="topnav1"
						title="Click to see details of last error which occured at <content:content name="transE.failedTime" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>"
						href="<bean:message key="datatransfer.basepath"/>/<c:out value="${transE.id}"/>"><content:content
							name="transE.failedTime" dateFormatKey="date.format.time.short"
							defaultValue="Not Set" ignoreNull="true" /></a>
				</c:catch>
			</c:if> <c:if test="${empty transE}">
				<i>None</i>
			</c:if></td>

		<th>Started</th>
		<td><c:catch var="ex">
				<c:set var="startT" value="${destination.startTime}" />
				<div
					title="<content:content name="startT" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>">
					<content:content name="startT"
						dateFormatKey="date.format.time.short" defaultValue="Not Set"
						ignoreNull="true" />
				</div>
			</c:catch> <c:if test="${not empty ex}">
				<i>None</i>
			</c:if></td>
			
		<th>Monitor</th>
		<td><c:if test="${destination.showInMonitors}">yes</c:if> <c:if
				test="${!destination.showInMonitors}">
				<font color="red">no</font>
			</c:if></td>

		<th>Filter</th>
		<c:set var="filter" value="${destination.filterName}" />
		<c:if test="${filter == 'none'}">
			<td><i>None</i></td>
		</c:if>
		<c:if test="${filter != 'none'}">
			<td>${destination.filterName}</td>
		</c:if>

		<th>Parallel</th>

		<c:set var="parallel" value="${destination.maxConnections}" />
		<c:if test="${parallel > 1}">
			<td>${destination.maxConnections}</td>
		</c:if>
		<c:if test="${parallel <= 1}">
			<td><i>None</i></td>
		</c:if>


		<auth:if basePathKey="transferhistory.basepath" paths="/">
			<auth:then>
				<th>Acquisition</th>
				<td><c:if test="${destination.acquisition}">yes</c:if> <c:if
						test="${!destination.acquisition}">no</c:if></td>
				<th>Enabled</th>
				<td><c:if test="${destination.active}">yes</c:if> <c:if
						test="${!destination.active}">
						<font color="red">no</font>
					</c:if></td>
			</auth:then>
		</auth:if>

	</tr>

</table>
