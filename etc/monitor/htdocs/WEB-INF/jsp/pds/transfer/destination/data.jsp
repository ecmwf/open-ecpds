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
					<c:if test="${not destination.active}"><i class="bi bi-slash-circle-fill text-danger" title="Destination is disabled" style="font-size:0.9rem;align-self:center;"></i></c:if>
					<a class="dest-page-name text-decoration-none" href="/do/transfer/destination/${destination.name}"<c:if test="${not destination.active}"> style="text-decoration:line-through !important;color:var(--bs-secondary-color)"</c:if>>${destination.name}</a>
					<c:if test="${destination.id != destination.name}">
						<code class="dest-page-id">${destination.id}</code>
					</c:if>
					<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_flag.jsp"/>
					<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_type_badge.jsp"/>
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
						<jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/></jsp:include>
					</c:if>
					<auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
					<auth:then>
					<div class="d-flex gap-1 ms-auto align-items-center">
						<a href='<bean:message key="destination.basepath"/>/edit/insert_form'
						   class="btn btn-sm btn-outline-success" title="Create new destination"><i class="bi bi-plus-circle"></i></a>
						<c:if test="${not empty destination.id}">
						<a href='<bean:message key="destination.basepath"/>/edit/update_form/${destination.id}'
						   class="btn btn-sm btn-outline-primary" title="Edit this destination"><i class="bi bi-pencil"></i></a>
						<a href='<bean:message key="destination.basepath"/>/edit/delete_form/${destination.id}'
						   class="btn btn-sm btn-outline-danger" title="Delete this destination"><i class="bi bi-trash"></i></a>
						</c:if>
					</div>
					</auth:then>
					</auth:if>
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

<c:choose>
<c:when test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/destination/data/warning.jsp" />
</c:when>
<c:otherwise>
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

<tiles:insert page="./pds/transfer/destination/data/info_table_1.jsp" /><tiles:insert page="./pds/transfer/destination/data/info_table_2.jsp" />
<tiles:insert page="./pds/transfer/destination/data/messages.jsp" />
<tiles:insert page="./pds/transfer/destination/data/filter_table.jsp" />
<table border=0 width="100%">
<tr><td valign="top" colspan="3"><tiles:insert page="./pds/transfer/destination/data/transfer_table.jsp" /></td></tr>
<tiles:insert page="./pds/transfer/destination/data/hosts_and_users_tr.jsp" />
</table>

		<script>
			setAll();
		</script>

	</html:form>
</form>
</c:otherwise>
</c:choose>