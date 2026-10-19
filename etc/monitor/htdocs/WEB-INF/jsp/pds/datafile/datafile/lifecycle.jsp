<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<c:if test="${not empty storageLifecycleError}">
<div class="alert alert-warning" role="alert">Storage lifecycle status unavailable:
    <c:out value="${storageLifecycleError}"/></div>
</c:if>
<c:if test="${not empty storageLifecycle}">
<div class="card border-0 shadow-sm mb-3">
    <div class="card-header d-flex align-items-center gap-2">
        <i class="bi bi-hdd-stack"></i><strong>Storage lifecycle</strong>
        <span class="badge text-bg-secondary"><c:out value="${storageLifecycle.SLC_STATE}"/></span>
    </div>
    <div class="card-body small">
        <p class="mb-1"><c:out value="${storageLifecycle.SLC_MESSAGE}"/></p>
        <c:if test="${storageLifecycle.enabled eq 'true'}">
        <dl class="row mb-0">
            <dt class="col-sm-4">Policies</dt><dd class="col-sm-8"><c:out value="${storageLifecycle.SLC_POLICIES}"/></dd>
            <dt class="col-sm-4">Requeue window until (UTC)</dt><dd class="col-sm-8"><c:out value="${storageLifecycle.SLC_GUARANTEED_UNTIL_TEXT}"/></dd>
            <dt class="col-sm-4">Replica removal eligible (UTC)</dt><dd class="col-sm-8"><c:out value="${storageLifecycle.SLC_ELIGIBLE_AT_TEXT}"/></dd>
            <dt class="col-sm-4">Last assessment (UTC)</dt><dd class="col-sm-8"><c:out value="${storageLifecycle.SLC_EVALUATED_AT_TEXT}"/></dd>
            <c:if test="${storageLifecycle.SLC_PIN_UNTIL gt 0}">
            <dt class="col-sm-4">Hold until (UTC)</dt><dd class="col-sm-8"><c:out value="${storageLifecycle.SLC_PIN_UNTIL_TEXT}"/>
                &mdash; <c:out value="${storageLifecycle.SLC_PIN_REASON}"/> (<c:out value="${storageLifecycle.SLC_PIN_BY}"/>)</dd>
            </c:if>
        </dl>
        <p class="text-muted mb-0">Original copies remain until normal expiry. A hold never extends expiry
            or recreates previously removed replicas. Status reflects the last assessment, not a live copy inventory.</p>
        </c:if>
    </div>
</div>
</c:if>
