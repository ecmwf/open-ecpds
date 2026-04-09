<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<div class="dest-page-header mb-3">
    <div class="d-flex align-items-baseline gap-2 flex-wrap mb-1">
        <span class="dest-page-name">${host.nickName}</span>
        <c:if test="${host.name != host.nickName}">
            <code class="dest-page-id">${host.name}</code>
        </c:if>
        <c:if test="${not host.active}">
            <i class="bi bi-pause-circle-fill text-warning" title="Host is disabled" style="font-size:0.9rem;align-self:center;"></i>
        </c:if>
        <span class="badge bg-secondary fs-status">${host.type}</span>
    </div>
    <c:if test="${not empty host.comment}">
        <p class="dest-page-comment">${host.comment}</p>
    </c:if>
</div>
