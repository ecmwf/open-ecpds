<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="_ds" value="${destination.formattedStatus}"/>
<c:set var="_dsb" value="${fn:contains(_ds, '-') ? fn:substringBefore(_ds, '-') : _ds}"/>
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
