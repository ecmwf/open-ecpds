<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:if test="${metaDataSize == 0}">
    <div class="alert alert-info d-flex align-items-center gap-2 mt-3">
        <i class="bi bi-info-circle-fill"></i>
        <span>No metadata files available for this destination.</span>
    </div>
</c:if>

<c:if test="${metaDataSize != 0}">

<div class="mb-3">
    <span class="badge bg-secondary rounded-pill">${metaDataSize} metadata file<c:if test="${metaDataSize != 1}">s</c:if></span>
</div>

<c:forEach var="file" items="${metaData}">
<div class="card mb-3">
    <div class="card-header d-flex align-items-center justify-content-between flex-wrap gap-2">
        <span class="fw-semibold"><i class="bi bi-file-earmark-text me-1"></i><c:out value="${file.name}"/></span>
        <span class="d-flex align-items-center gap-2">
            <span class="badge bg-light text-dark border"><code style="font-size:0.8rem"><c:out value="${file.contentType}"/></code></span>
            <span class="text-muted small"><i class="bi bi-clock me-1"></i>${file.lastModificationDate}</span>
        </span>
    </div>
    <div class="card-body p-0">
        <c:if test="${file.contentType == 'text/plain'}">
            <pre class="m-0 p-3" style="white-space:pre-wrap;word-break:break-word;font-size:0.85rem;background:#f8f9fa;border-radius:0 0 0.375rem 0.375rem">${file.stringContent}</pre>
        </c:if>
        <c:if test="${file.contentType != 'text/plain'}">
            <div class="p-3">
                <content:content name="file" outputContentType="text/html" thumbnail="false" embedded="true"/>
            </div>
        </c:if>
    </div>
</div>
</c:forEach>

</c:if>
