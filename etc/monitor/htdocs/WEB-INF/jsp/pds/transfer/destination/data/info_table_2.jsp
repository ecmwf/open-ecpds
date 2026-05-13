<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<div class="card border-0 shadow-sm mb-3">
	<div class="card-body py-2">
		<div class="meta-bar">
			<div class="meta-item">
				<span class="meta-label">Last Transfer</span>
				<c:set var="transL" value="${destination.lastTransfer}" />
				<c:choose>
					<c:when test="${not empty transL}">
						<c:catch>
							<a class="badge rounded-pill border fw-normal text-decoration-none bg-body-tertiary text-body"
								title="Click to see details of last transfer which occured at <content:content name="transL.finishTime" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>"
								href="<bean:message key="datatransfer.basepath"/>/${transL.id}"><content:content
									name="transL.finishTime" dateFormatKey="date.format.time.short"
									defaultValue="Not Set" ignoreNull="true" /></a>
						</c:catch>
					</c:when>
					<c:otherwise>
						<span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="meta-item">
				<span class="meta-label">Last Error</span>
				<c:set var="transE" value="${destination.lastError}" />
				<c:choose>
					<c:when test="${not empty transE}">
						<c:catch>
							<a class="badge rounded-pill border fw-normal text-decoration-none bg-danger-subtle text-danger-emphasis"
								title="Click to see details of last error which occured at <content:content name="transE.failedTime" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>"
								href="<bean:message key="datatransfer.basepath"/>/<c:out value="${transE.id}"/>"><content:content
									name="transE.failedTime" dateFormatKey="date.format.time.short"
									defaultValue="Not Set" ignoreNull="true" /></a>
						</c:catch>
					</c:when>
					<c:otherwise>
						<span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis">None</span>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="meta-item">
				<span class="meta-label">Started</span>
				<c:catch var="ex">
					<c:set var="startT" value="${destination.startTime}" />
					<c:choose>
						<c:when test="${not empty startT}">
							<span class="badge rounded-pill border fw-normal bg-body-tertiary text-body"
								title="<content:content name="startT" dateFormatKey="date.format.long.iso" defaultValue="Not Set" ignoreNull="true"/>">
								<content:content name="startT" dateFormatKey="date.format.time.short" defaultValue="Not Set" ignoreNull="true" />
							</span>
						</c:when>
						<c:otherwise>
							<span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span>
						</c:otherwise>
					</c:choose>
				</c:catch>
				<c:if test="${not empty ex}">
					<span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span>
				</c:if>
			</div>
			<div class="meta-item">
				<span class="meta-label">Filter</span>
				<c:choose>
					<c:when test="${empty destination.filterName or destination.filterName eq 'none'}">
						<span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span>
					</c:when>
					<c:otherwise>
						<span class="badge rounded-pill border fw-normal bg-body-tertiary text-body"><jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/><jsp:param name="showName" value="false"/></jsp:include> ${destination.filterName}</span>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="meta-item">
				<span class="meta-label">Parallel</span>
				<c:set var="parallel" value="${destination.maxConnections}" />
				<c:choose>
					<c:when test="${parallel > 1}">
						<span class="badge rounded-pill border fw-normal bg-body-tertiary text-body">${destination.maxConnections}</span>
					</c:when>
					<c:otherwise>
						<span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-slash-circle me-1"></i>Disabled</span>
					</c:otherwise>
				</c:choose>
			</div>
			<auth:if basePathKey="transferhistory.basepath" paths="/">
				<auth:then>
					<div class="meta-item">
						<span class="meta-label">Acquisition</span>
						<c:choose>
							<c:when test="${destination.acquisition}">
								<span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span>
							</c:when>
							<c:otherwise>
								<span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span>
							</c:otherwise>
						</c:choose>
					</div>
				</auth:then>
			</auth:if>
			<span class="text-muted ms-auto" style="font-size:0.72rem;" title="All times shown are in UTC"><i class="bi bi-clock"></i> UTC</span>
			<%-- File count was moved to the Selection bar in transfer_table.jsp --%>
		</div>
	</div>
</div>
