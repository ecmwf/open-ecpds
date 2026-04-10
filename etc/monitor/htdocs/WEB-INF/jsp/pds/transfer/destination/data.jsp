<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>

<auth:if basePathKey="destination.basepath"
	paths="/operations/${destinationDetailActionForm.id}/requeue/">
	<auth:then>
		<c:set var="ecpdsCanHandleQueue" value="true" scope="request" />
		<c:set var="numberOfColumns" value="9" scope="request" />
	</auth:then>
	<auth:else>
		<c:set var="numberOfColumns" value="8" scope="request" />
	</auth:else>
</auth:if>

<tiles:insert page="./pds/transfer/destination/data/javascript.jsp" />

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${empty isDelete}">
	<c:set var="desStatus" value="${destination.formattedStatus}" />
	<c:set var="desStatusBase" value="${fn:contains(desStatus, '-') ? fn:substringBefore(desStatus, '-') : desStatus}"/>
	<div class="dest-page-header mb-3">
				<div class="d-flex align-items-center gap-2 flex-wrap mb-1">
					<span class="dest-page-name">${destination.name}</span>
					<c:if test="${destination.id != destination.name}">
						<code class="dest-page-id">${destination.id}</code>
					</c:if>
					<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_flag.jsp"/>
					<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_type_badge.jsp"/>
					<c:if test="${not destination.active}">
						<i class="bi bi-pause-circle-fill text-warning" title="Destination is disabled" style="font-size:0.9rem;align-self:center;"></i>
					</c:if>
					<c:choose>
						<c:when test="${desStatusBase == 'Idle'}">
							<span class="badge bg-secondary fs-status" title="${desStatus}">${desStatus}</span>
						</c:when>
						<c:when test="${desStatusBase == 'Running'}">
							<span class="badge bg-success fs-status" title="${desStatus}">${desStatus}</span>
						</c:when>
						<c:when test="${desStatusBase == 'Restarting' or desStatusBase == 'Resending'}">
							<span class="badge bg-info text-dark fs-status" title="${desStatus}">${desStatus}</span>
						</c:when>
						<c:when test="${desStatusBase == 'Waiting' or desStatusBase == 'Retrying' or desStatusBase == 'Interrupted'}">
							<span class="badge bg-warning text-dark fs-status" title="${desStatus}">${desStatus}</span>
						</c:when>
						<c:when test="${desStatusBase == 'Initialized' or desStatusBase == 'Stopped' or desStatusBase == 'NoHosts' or desStatusBase == 'Failed'}">
							<span class="badge bg-danger fs-status" title="${desStatus}">${desStatus}</span>
						</c:when>
						<c:otherwise>
							<span class="badge bg-secondary fs-status" title="${desStatus}">${desStatus}</span>
						</c:otherwise>
					</c:choose>
					<c:if test="${not destination.showInMonitors}">
						<i class="bi bi-eye-slash text-muted" title="Not shown in Monitor Display" style="font-size:0.85rem;"></i>
					</c:if>
					<c:if test="${not empty destination.filterName and destination.filterName ne 'none'}">
						<i class="bi bi-file-zip text-muted" title="Data compression enabled (${destination.filterName})" style="font-size:0.85rem;"></i>
					</c:if>
				</div>
				<c:if test="${not empty destination.comment}">
					<p class="dest-page-comment">${destination.comment}</p>
				</c:if>
				<c:if test="${not empty destination.ecUserName}">
					<p class="mb-0 small text-muted">
						<i class="bi bi-person-fill me-1"></i><c:choose>
							<c:when test="${not empty destination.userMail}"><a href="mailto:${destination.userMail}" class="text-muted">${destination.ecUserName}</a></c:when>
							<c:otherwise>${destination.ecUserName}</c:otherwise>
						</c:choose>
					</p>
				</c:if>
			</div>
</c:if>

<form name="destinationDetailActionForm" method="GET"
	action="<bean:message key="destination.basepath"/>/${destinationDetailActionForm.id}">

	<html:form action="/transfer/destination/*">
		<html:hidden property="status" />
		<html:hidden property="dataStream" />
		<html:hidden property="dataTime" />
		<html:hidden property="disseminationStream" />
		<html:hidden property="date" />

		<c:forEach var="param"
			items="${destinationDetailActionForm.displayTagsParamCollection}">
			<input type="hidden" name="${param.name}" value="${param.value}" />
		</c:forEach>

		<c:if test="${not empty isDelete}">
			<tiles:insert page="./pds/transfer/destination/data/warning.jsp" />
		</c:if>
		<c:if test="${empty isDelete}">
<tiles:insert page="./pds/transfer/destination/data/info_table_1.jsp" /><tiles:insert page="./pds/transfer/destination/data/info_table_2.jsp" />
<tiles:insert page="./pds/transfer/destination/data/messages.jsp" />
<tiles:insert page="./pds/transfer/destination/data/filter_table.jsp" />
<table border=0 width="100%">
<tr><td valign="top" colspan="3"><tiles:insert page="./pds/transfer/destination/data/transfer_table.jsp" /></td></tr>
<tiles:insert page="./pds/transfer/destination/data/hosts_and_users_tr.jsp" />
</table>
		</c:if>

		<script>
			setAll();
		</script>

	</html:form>
</form>