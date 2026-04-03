<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table class="select info-panel" style="width: 700px">

	<tr>
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
				<c:if test="${not empty startT}">
					<div
						title="<content:content name="startT" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>">
						<content:content name="startT"
							dateFormatKey="date.format.time.short" defaultValue="Not Set"
							ignoreNull="true" />
					</div>
				</c:if>
				<c:if test="${empty startT}">
					<i>None</i>
				</c:if>
			</c:catch> <c:if test="${not empty ex}">
				<i>None</i>
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
				<td><c:if test="${destination.acquisition}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
						test="${!destination.acquisition}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></td>
			</auth:then>
		</auth:if>

	</tr>

</table>
