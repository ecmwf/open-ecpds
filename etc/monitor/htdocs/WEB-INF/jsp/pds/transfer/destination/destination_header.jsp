<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<c:set var="_ds" value="${destination.formattedStatus}"/>
<c:set var="_dsb" value="${fn:contains(_ds, '-') ? fn:substringBefore(_ds, '-') : _ds}"/>
<c:if test="${not empty destinationDetailActionForm}">
    <c:set var="_destDate" value="${destinationDetailActionForm.date}" />
</c:if>
<c:if test="${empty destinationDetailActionForm}">
    <c:set var="_destDate" value="${param['date']}" />
</c:if>
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
            <c:when test="${_dsb == 'Idle'}">
                <span class="badge bg-secondary fs-status" title="${_ds}">${_ds}</span>
            </c:when>
            <c:when test="${_dsb == 'Running'}">
                <span class="badge bg-success fs-status" title="${_ds}">${_ds}</span>
            </c:when>
            <c:when test="${_dsb == 'Restarting' or _dsb == 'Resending'}">
                <span class="badge bg-info text-dark fs-status" title="${_ds}">${_ds}</span>
            </c:when>
            <c:when test="${_dsb == 'Waiting' or _dsb == 'Retrying' or _dsb == 'Interrupted'}">
                <span class="badge bg-warning text-dark fs-status" title="${_ds}">${_ds}</span>
            </c:when>
            <c:when test="${_dsb == 'Initialized' or _dsb == 'Stopped' or _dsb == 'NoHosts' or _dsb == 'Failed'}">
                <span class="badge bg-danger fs-status" title="${_ds}">${_ds}</span>
            </c:when>
            <c:otherwise>
                <span class="badge bg-secondary fs-status" title="${_ds}">${_ds}</span>
            </c:otherwise>
        </c:choose>
        <c:if test="${not destination.showInMonitors}">
            <i class="bi bi-eye-slash text-muted" title="Not shown in Monitor Display" style="font-size:0.85rem;"></i>
        </c:if>
        <c:if test="${not empty destination.filterName and destination.filterName ne 'none'}">
            <jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/></jsp:include>
        </c:if>
        <div class="d-flex gap-2 ms-auto align-items-center">
        <auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
        <auth:then>
        <div class="d-flex gap-1 align-items-center">
            <a href='<bean:message key="destination.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new destination"><i class="bi bi-plus-circle"></i></a>
            <c:if test="${not empty destination.id}">
            <a href='<bean:message key="destination.basepath"/>/edit/update_form/${destination.id}'
               class="btn btn-sm btn-outline-primary" title="Edit this destination"><i class="bi bi-pencil"></i></a>
            <a href='<bean:message key="destination.basepath"/>/edit/delete_form/${destination.id}'
               class="btn btn-sm btn-outline-danger" title="Delete this destination"><i class="bi bi-trash"></i></a>
            <a href='<bean:message key="destination.basepath"/>/edit/insert_form?fromDestination=${destination.name}'
               class="btn btn-sm btn-outline-warning" title="Duplicate this destination"><i class="bi bi-copy"></i></a>
            </c:if>
        </div>
        </auth:then>
        </auth:if>
        <c:if test="${not empty destination.id}">
        <div class="d-flex gap-1 align-items-center" style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;">
            <a href='<bean:message key="destination.basepath"/>/${destination.id}'
               class="btn btn-sm btn-outline-secondary" title="Files"><i class="bi bi-files"></i></a>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=parameters'
               class="btn btn-sm btn-outline-secondary" title="Parameters"><i class="bi bi-sliders"></i></a>
            </auth:then>
            </auth:if>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=traffic'
               class="btn btn-sm btn-outline-secondary" title="Data Rates"><i class="bi bi-graph-up"></i></a>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=changelog'
               class="btn btn-sm btn-outline-secondary" title="Changes Log"><i class="bi bi-clock-history"></i></a>
            <a href='<bean:message key="monitoring.timeline.basepath"/>/${destination.id}<c:if test="${not empty _destDate}">?date=${_destDate}</c:if>'
               class="btn btn-sm btn-outline-secondary" title="Transfer Timeline"><i class="bi bi-calendar3"></i></a>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href='<bean:message key="transferhistory.basepath"/>?destinationName=${destination.id}<c:if test="${not empty _destDate}">&amp;date=${_destDate}</c:if>&amp;fromDestination=true'
               class="btn btn-sm btn-outline-secondary" title="Transfer History"><i class="bi bi-archive"></i></a>
            </auth:then>
            </auth:if>
            <auth:if basePathKey="destination.basepath" paths="/metadata/${destination.id}">
            <auth:then>
            <a href='<bean:message key="destination.basepath"/>/metadata/${destination.id}'
               class="btn btn-sm btn-outline-secondary" title="Metadata"><i class="bi bi-paperclip"></i></a>
            </auth:then>
            </auth:if>
        </div>
        </c:if>
        </div>
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
