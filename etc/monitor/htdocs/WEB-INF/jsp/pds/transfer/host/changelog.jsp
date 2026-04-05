<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${empty host.changeLogList}">
	<div class="alert">
		No change log available for host <strong><c:out value="${host.name}" /></strong>
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
.diff-pre .dl-add  { color:#8f8; }
.diff-pre .dl-del  { color:#f88; }
.diff-pre .dl-hunk { color:#79b8ff; }
.diff-pre .dl-meta { color:#6c757d; }
/* HTML diff markup from server */
.diff-pre font[color="red"],   .diff-pre font[color="#ff0000"] { color:#f88 !important; }
.diff-pre font[color="green"], .diff-pre font[color="#00aa00"] { color:#8f8 !important; }
.diff-pre font[color="blue"],  .diff-pre font[color="#0000ff"] { color:#79b8ff !important; }
.diff-pre s   { color:#f88; text-decoration:line-through; opacity:0.85; }
.diff-pre ins { color:#8f8; text-decoration:none; }
.diff-pre del { color:#f88; text-decoration:line-through; opacity:0.85; }
</style>

	<display:table name="${host.changeLogList}" id="changelog"
		requestURI="" sort="list" pagesize="4" class="listing">
		<display:column title="Date &amp; Time" sortable="true"
			sortProperty="date" style="width:130px; white-space:nowrap;">
			<content:content name="changelog.date"
				dateFormatKey="date.format.long.iso" ignoreNull="true" />
		</display:column>
		<display:column title="Web User" sortable="true" style="width:100px; white-space:nowrap;">${changelog.webUserId}</display:column>
		<display:column title="Differences">
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
		</display:column>
	</display:table>

</c:if>

