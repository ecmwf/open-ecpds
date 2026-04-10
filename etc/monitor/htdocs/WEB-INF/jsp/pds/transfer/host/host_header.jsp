<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<div class="dest-page-header mb-3">
    <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
        <span class="dest-page-name">${host.nickName}</span>
        <c:if test="${host.name != host.nickName}">
            <code class="dest-page-id">${host.name}</code>
        </c:if>
        <c:if test="${not host.active}">
            <i class="bi bi-pause-circle-fill text-warning" title="Host is disabled" style="font-size:0.9rem;"></i>
        </c:if>
        <span class="badge bg-secondary fs-status">${host.type}</span>
        <c:if test="${not empty host.transferMethodName}">
            <span class="badge bg-info text-dark fs-status"><i class="bi bi-hdd-network me-1"></i>${host.transferMethodName}</span>
        </c:if>
        <c:if test="${not empty host.filterName and host.filterName ne 'none'}">
            <i class="bi bi-file-zip text-muted" title="Data compression enabled (${host.filterName})" style="font-size:0.85rem;"></i>
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
