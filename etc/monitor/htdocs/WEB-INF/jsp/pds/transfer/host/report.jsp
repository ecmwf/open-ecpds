<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>

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
        <div class="d-flex gap-2">
            <button id="reportRefreshBtn"
                    class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2"
                    onclick="fetchReport()" title="Refresh report" style="font-size:0.75rem;">
                <i class="bi bi-arrow-clockwise"></i> Refresh
            </button>
            <button id="reportCopyBtn"
                    class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2"
                    onclick="copyReport(this)" title="Copy to clipboard" style="font-size:0.75rem;" disabled>
                <i class="bi bi-clipboard"></i> Copy
            </button>
        </div>
    </div>
    <div class="card-body p-0">
        <pre id="reportPre"
             style="background:#1e1e1e; color:#d4d4d4; margin:0; padding:1rem 1.25rem;
                    font-size:0.78rem; line-height:1.6; max-height:75vh;
                    overflow-y:auto; border-radius:0 0 4px 4px;
                    white-space:pre-wrap; word-break:break-all;">
            <span id="reportSpinner" class="text-white-50" style="font-style:italic;">
                <i class="bi bi-hourglass-split me-1"></i> Generating report, please wait&hellip;
            </span>
        </pre>
    </div>
</div>

<script>
(function() {
    var dataUrl   = '<c:out value="${reportDataUrl}"/>';
    var cacheKey  = 'hostReport_<c:out value="${host.name}"/>';

    function showCached(html, isStale) {
        var pre = document.getElementById('reportPre');
        var copyBtn = document.getElementById('reportCopyBtn');
        pre.innerHTML = html;
        if (isStale) {
            pre.innerHTML += '<div style="font-size:0.7rem;color:#555;margin-top:0.5rem;font-style:italic;">'
                + '<i class="bi bi-clock me-1"></i>Cached result &mdash; click Refresh to update.</div>';
        }
        copyBtn.disabled = false;
    }

    function fetchReport() {
        var pre = document.getElementById('reportPre');
        var copyBtn = document.getElementById('reportCopyBtn');
        var refreshBtn = document.getElementById('reportRefreshBtn');
        pre.innerHTML = '<span class="text-white-50" style="font-style:italic;">'
            + '<i class="bi bi-hourglass-split me-1"></i> Generating report, please wait\u2026</span>';
        copyBtn.disabled = true;
        refreshBtn.disabled = true;
        fetch(dataUrl)
            .then(function(r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.text();
            })
            .then(function(text) {
                try { sessionStorage.setItem(cacheKey, text); } catch(e) {}
                pre.innerHTML = text;
                copyBtn.disabled = false;
                refreshBtn.disabled = false;
            })
            .catch(function(err) {
                pre.innerHTML = '<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>Failed to load report: ' + err.message + '</span>';
                refreshBtn.disabled = false;
            });
    }

    window.fetchReport = fetchReport;

    window.copyReport = function(btn) {
        var text = document.getElementById('reportPre').textContent;
        navigator.clipboard.writeText(text).then(function() {
            btn.innerHTML = '<i class="bi bi-check-lg"></i> Copied';
            setTimeout(function() { btn.innerHTML = '<i class="bi bi-clipboard"></i> Copy'; }, 1500);
        });
    };

    // On load: show cached result if available, otherwise fetch
    var cached = null;
    try { cached = sessionStorage.getItem(cacheKey); } catch(e) {}
    if (cached) {
        showCached(cached, true);
    } else {
        fetchReport();
    }
})();
</script>
