<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="card shadow-sm mt-2" style="max-width:900px;">
    <div class="card-header d-flex align-items-center justify-content-between py-2 px-3"
         style="background:#2d2d2d; border-bottom:1px solid #444;">
        <span class="text-white fw-semibold" style="font-size:0.875rem;">
            <i class="bi bi-terminal-fill me-2 text-success"></i>
            <c:choose>
                <c:when test="${not empty proxy}">
                    Network Report: <c:out value="${host.nickName}" />
                    <span class="text-white-50 fw-normal"> via <c:out value="${proxy.nickName}" /></span>
                </c:when>
                <c:otherwise>
                    Network Report: <c:out value="${host.nickName}" />
                </c:otherwise>
            </c:choose>
        </span>
        <button class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2"
                onclick="(function(){navigator.clipboard.writeText(document.getElementById('reportPre').textContent);this.innerHTML='<i class=\'bi bi-check-lg\'></i> Copied';setTimeout(function(){document.querySelector('.report-copy-btn').innerHTML='<i class=\'bi bi-clipboard\'></i> Copy';},1500);}).call(this)"
                title="Copy to clipboard" class="report-copy-btn" style="font-size:0.75rem;">
            <i class="bi bi-clipboard"></i> Copy
        </button>
    </div>
    <div class="card-body p-0">
        <pre id="reportPre"
             style="background:#1e1e1e; color:#d4d4d4; margin:0; padding:1rem 1.25rem;
                    font-size:0.78rem; line-height:1.6; max-height:75vh;
                    overflow-y:auto; border-radius:0 0 4px 4px;
                    white-space:pre-wrap; word-break:break-all;">${message}</pre>
    </div>
</div>
