<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:if test="${empty destination.changeLogList}">
	<div class="alert alert-info">
		No change log available for destination <strong><c:out value="${destination.name}" /></strong>
	</div>
</c:if>

<c:if test="${!empty destination.changeLogList}">

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
#changelogTable td:first-child, #changelogTable td:nth-child(2) { white-space:nowrap; vertical-align:top; padding-top:0.6rem; }
</style>

<table id="changelogTable" class="table table-sm table-hover" style="width:100%">
	<thead class="table-light">
		<tr>
			<th>Date &amp; Time</th>
			<th>Web User</th>
			<th>Differences</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="changelog" items="${destination.changeLogList}">
		<tr>
			<td data-order="${changelog.date.time}">
				<content:content name="changelog.date" dateFormatKey="date.format.long.iso" ignoreNull="true"/>
			</td>
			<td>${changelog.webUserId}</td>
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

<script>
$(document).ready(function() {
	$('#changelogTable').DataTable({
		order:       [[0, 'desc']],
		pageLength:  10,
		columnDefs:  [{ orderable: false, targets: 2 }]
	});
});
</script>

</c:if>

