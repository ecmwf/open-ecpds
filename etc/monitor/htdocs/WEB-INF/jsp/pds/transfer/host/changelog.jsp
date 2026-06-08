<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>

<c:choose>
<c:when test="${isRestrictedUser == 'true' && host.type == 'Proxy'}">
	<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
		<i class="bi bi-info-circle-fill"></i>
		<span>No change log available for host <strong><c:out value="${host.name}" /></strong>.</span>
	</div>
</c:when>
<c:otherwise>

<c:if test="${empty host.changeLogList}">
	<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
		<i class="bi bi-info-circle-fill"></i>
		<span>No change log available for host <strong><c:out value="${host.name}" /></strong>.</span>
	</div>
</c:if>

<c:if test="${!empty host.changeLogList}">

<style>
.cl-tabs .nav-tabs { background:#2a2a2a; border-bottom:1px solid #3a3a3a; padding:0.25rem 0.5rem 0; border-radius:4px 4px 0 0; }
.cl-tabs .nav-tabs .nav-link { font-size:0.72rem; padding:0.2rem 0.65rem; color:#aaa; border:1px solid transparent; border-radius:3px 3px 0 0; }
.cl-tabs .nav-tabs .nav-link.active { background:#1e1e1e; border-color:#3a3a3a #3a3a3a #1e1e1e; color:#fff; }
.cl-tabs .nav-tabs .nav-link:hover:not(.active):not([disabled]) { color:#ddd; border-color:#3a3a3a; }
.cl-tabs .nav-tabs .nav-link[disabled] { color:#555; pointer-events:none; }
.diff-pre {
    background:#1e1e1e; color:#d4d4d4; margin:0;
    padding:0.5rem 0.75rem; font-size:0.72rem; line-height:1.5;
    height:130px; min-height:60px; max-height:320px;
    overflow-y:auto; resize:vertical;
    border-radius:0 0 4px 4px;
    font-family:'Consolas','Monaco',monospace;
    white-space:pre; overflow-x:auto;
}
.diff-pre .cl-del  { color:#f88; }
.diff-pre .cl-del s { text-decoration:line-through; opacity:0.85; }
.diff-pre .cl-add  { color:#8f8; }
.diff-pre .cl-note { color:#888; font-style:italic; }
.diff-pre .cl-multiline { background:rgba(255,255,180,0.07); }
.diff-pre b { color:#79b8ff; }
/* Light theme overrides */
[data-bs-theme=light] .cl-tabs .nav-tabs { background:#f0f2f4; border-bottom-color:#d0d7de; }
[data-bs-theme=light] .cl-tabs .nav-tabs .nav-link { color:#57606a; }
[data-bs-theme=light] .cl-tabs .nav-tabs .nav-link.active { background:#fff; border-color:#d0d7de #d0d7de #fff; color:#24292f; }
[data-bs-theme=light] .cl-tabs .nav-tabs .nav-link:hover:not(.active):not([disabled]) { color:#24292f; border-color:#d0d7de; }
[data-bs-theme=light] .cl-tabs .nav-tabs .nav-link[disabled] { color:#bbb; }
[data-bs-theme=light] .diff-pre { background:#f6f8fa; color:#24292f; }
[data-bs-theme=light] .diff-pre .cl-del  { color:#cf222e; }
[data-bs-theme=light] .diff-pre .cl-add  { color:#1a7f37; }
[data-bs-theme=light] .diff-pre .cl-note { color:#57606a; }
[data-bs-theme=light] .diff-pre .cl-multiline { background:rgba(255,240,0,0.15); }
[data-bs-theme=light] .diff-pre b { color:#0550ae; }
#changelogTable td:first-child, #changelogTable td:nth-child(2) { white-space:nowrap; vertical-align:top; padding-top:0.6rem; }
</style>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-clock-history text-primary"></i>
    <span class="fw-semibold">Change Log</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="clHostSearch" class="form-control" placeholder="Search..." style="min-width:160px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="clHostPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="2">2</option>
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="changelogTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
    <thead class="table-light">
        <tr>
            <th style="width:155px; white-space:nowrap" title="Date &amp; Time (UTC)">Date &amp; Time</th>
            <th style="width:1%; white-space:nowrap" class="text-center">Web User</th>
            <th>Differences</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="changelog" items="${host.changeLogList}">
        <tr>
            <td style="white-space:nowrap" data-order="${changelog.date.time}">
                <content:content name="changelog.date" dateFormatKey="date.format.iso" ignoreNull="true"/><br>
                <small class="text-muted"><content:content name="changelog.date" dateFormatKey="date.format.time" ignoreNull="true"/></small></td>
            <td style="white-space:nowrap" class="text-center">${changelog.webUserId}</td>
            <td>
                <div class="cl-tabs">
                    <ul class="nav nav-tabs" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active"
                                data-bs-toggle="tab"
                                data-bs-target="#cl-prev-${changelog.changeLogId}"
                                type="button" role="tab">
                                <i class="bi bi-clock-history me-1"></i>vs Previous
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link<c:if test="${empty changelog.differencesFromCurrent}"> disabled</c:if>"
                                data-bs-toggle="tab"
                                data-bs-target="#cl-curr-${changelog.changeLogId}"
                                type="button" role="tab"
                                <c:if test="${empty changelog.differencesFromCurrent}">disabled</c:if>>
                                <i class="bi bi-arrow-repeat me-1"></i>vs Current
                            </button>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane show active" id="cl-prev-${changelog.changeLogId}" role="tabpanel">
                            <pre class="diff-pre">${changelog.differences}</pre>
                        </div>
                        <div class="tab-pane" id="cl-curr-${changelog.changeLogId}" role="tabpanel">
                            <pre class="diff-pre">${changelog.differencesFromCurrent}</pre>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</div>
</div>

<script>
$(document).ready(function() {
    var _len = (function() { try { var v = parseInt(localStorage.getItem('clHostPageLen'), 10); return [2,5,10,25,50,100,250].indexOf(v) >= 0 ? v : 10; } catch(e) { return 10; } })();
    $('#clHostPageLen').val(_len);
    var table = $('#changelogTable').DataTable({
        order:      [[0, 'desc']],
        pageLength: _len,
        searching:  true,
        info:       true,
        dom:        't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        columnDefs: [{ orderable: false, targets: 2 }]
    });
    $('#clHostPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('clHostPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#clHostSearch').on('keyup', function() { table.search(this.value).draw(); });
});
</script>

</c:if>

</c:otherwise>
</c:choose>
