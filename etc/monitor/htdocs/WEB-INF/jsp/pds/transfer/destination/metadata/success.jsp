<%@ page session="true"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${not empty message}">
    <div id="destinationMetadataSuccess" class="alert alert-success fade show mt-2" role="status">
        <strong>${message}</strong>
    </div>
    <script>
        setTimeout(function() {
            var el = document.getElementById('destinationMetadataSuccess');
            if (el) bootstrap.Alert.getOrCreateInstance(el).close();
        }, 5000);
    </script>
</c:if>

<c:if test="${not empty requirements}">
<div class="card mt-2">
    <div class="card-header"><c:out value="${comment}"/></div>
    <div class="card-body p-0" style="overflow-x:auto">
        <pre class="m-0 p-3" style="white-space:pre-wrap;word-break:break-word;overflow-wrap:break-word;font-size:0.85rem;background:var(--bs-tertiary-bg)">${requirements}</pre>
    </div>
</div>
</c:if>
