<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<div class="row g-2 mb-3">

	<div class="col-6 col-sm-4 col-md-2">
		<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Last Transfer</div>
		<div><c:set var="transL" value="${destination.lastTransfer}" /> <c:if
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
			</c:if></div>
	</div>

	<div class="col-6 col-sm-4 col-md-2">
		<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Last Error</div>
		<div><c:set var="transE" value="${destination.lastError}" /> <c:if
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
			</c:if></div>
	</div>

	<div class="col-6 col-sm-4 col-md-2">
		<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Started</div>
		<div><c:catch var="ex">
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
			</c:if></div>
	</div>

	<div class="col-6 col-sm-4 col-md-2">
		<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Filter</div>
		<c:set var="filter" value="${destination.filterName}" />
		<div><jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/><jsp:param name="showName" value="true"/></jsp:include></div>
	</div>

	<div class="col-6 col-sm-4 col-md-2">
		<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Parallel</div>
		<c:set var="parallel" value="${destination.maxConnections}" />
		<c:if test="${parallel > 1}">
			<div>${destination.maxConnections}</div>
		</c:if>
		<c:if test="${parallel <= 1}">
			<div><i>None</i></div>
		</c:if>
	</div>

	<auth:if basePathKey="transferhistory.basepath" paths="/">
		<auth:then>
			<div class="col-6 col-sm-4 col-md-2">
				<div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size:0.7rem;letter-spacing:0.04em">Acquisition</div>
				<div><c:if test="${destination.acquisition}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if> <c:if
						test="${!destination.acquisition}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if></div>
			</div>
		</auth:then>
	</auth:if>

</div>
