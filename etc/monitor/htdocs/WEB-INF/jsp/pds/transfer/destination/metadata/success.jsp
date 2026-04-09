<%@ page session="true"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${not empty message}">
    <div class="alert alert-success mt-2">
        <strong>${message}</strong>
    </div>
</c:if>

<c:if test="${not empty requirements}">
<div class="card mt-2">
    <div class="card-header"><c:out value="${comment}"/></div>
    <div class="card-body p-0">
        <pre class="m-0 p-3" style="white-space:pre-wrap;word-break:break-word;font-size:0.85rem;background:#f8f9fa">${requirements}</pre>
    </div>
</div>
</c:if>
