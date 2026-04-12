<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${not empty host.geoIpLocation}">
    <c:set var="_hGeoParts" value="${fn:split(host.geoIpLocation, '/')}"/>
    <c:set var="_hGeoPart0" value="${fn:trim(_hGeoParts[0])}"/>
    <c:set var="_hGeoPart1" value="${fn:trim(_hGeoParts[1])}"/>
    <c:set var="_hGeoIso" value="${fn:toLowerCase(fn:length(_hGeoPart0) == 2 ? _hGeoPart0 : _hGeoPart1)}"/>
</c:if>
<div class="dest-page-header mb-3">
    <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
        <span class="dest-page-name">${host.nickName}</span>
        <c:if test="${host.name != host.nickName}">
            <code class="dest-page-id" title="Host identifier">${host.name}</code>
        </c:if>
        <c:if test="${not empty host.geoIpLocation}">
            <span class="fi fi-${_hGeoIso}" title="${host.geoIpLocation}" style="font-size:1.2em;border-radius:2px;"></span>
        </c:if>
        <c:choose>
            <c:when test="${host.type == 'Dissemination'}">
                <span class="badge bg-secondary fs-status" title="Dissemination"><i class="bi bi-send-fill"></i></span>
            </c:when>
            <c:when test="${host.type == 'Acquisition'}">
                <span class="badge bg-secondary fs-status" title="Acquisition"><i class="bi bi-cloud-download-fill"></i></span>
            </c:when>
            <c:otherwise>
                <span class="badge bg-secondary fs-status">${host.type}</span>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty host.transferMethodName}">
            <span class="badge bg-info text-dark fs-status" title="${host.transferMethod.comment}"><i class="bi bi-hdd-network me-1"></i>${host.transferMethodName}</span>
        </c:if>
        <c:if test="${not empty host.filterName and host.filterName ne 'none'}">
            <i class="bi bi-file-zip text-muted" title="Data compression enabled (${host.filterName})" style="font-size:0.85rem;"></i>
        </c:if>
        <c:if test="${not host.active}">
            <i class="bi bi-pause-circle-fill text-warning" title="Host is disabled" style="font-size:0.9rem;"></i>
        </c:if>
    </div>
    <c:if test="${not empty host.comment}">
        <p class="dest-page-comment">${host.comment}</p>
    </c:if>
    <c:if test="${not empty host.ECUserName}">
        <p class="mb-0 small text-muted">
            <i class="bi bi-person-fill me-1"></i><c:choose>
                <c:when test="${not empty host.userMail}"><a href="mailto:${host.userMail}" class="text-muted">${host.ECUserName}</a></c:when>
                <c:otherwise>${host.ECUserName}</c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>
