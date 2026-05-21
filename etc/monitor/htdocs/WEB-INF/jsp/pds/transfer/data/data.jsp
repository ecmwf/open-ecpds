<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="authorized" value="false" />

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<c:set var="authorized" value="true" />
</auth:then>
<auth:else>
<auth:if basePathKey="destination.basepath" paths="/${datatransfer.destination.name}">
<auth:then>
  <c:set var="authorized" value="true" />
</auth:then>
</auth:if>
</auth:else>
</auth:if>

<c:if test="${authorized == 'false'}">
<div class="alert alert-danger mt-2">
<i class="bi bi-exclamation-triangle-fill me-2"></i>
Error retrieving object by key &mdash; DataTransfer not found: <code>${datatransfer.id}</code>
</div>
</c:if>

<c:if test="${authorized == 'true'}">

<jsp:include page="./pds/transfer/data/data_table.jsp"/>

<%-- Transfer History --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<c:set var="transferHistory" value="${datatransfer.transferHistory}"/>
<c:set var="canSeeHistoryDetail" value="true"/>
</auth:then>
<auth:else>
<c:set var="transferHistory" value="${datatransfer.transferHistoryAfterScheduledTime}"/>
</auth:else>
</auth:if>

<c:if test="${historyItemsSize == '0'}">
<div class="card border-0 shadow-sm mt-3 mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock-history text-primary"></i>
<span class="fw-semibold">Transfer History</span>
</div>
<div class="card-body">
<div class="alert alert-info mb-0">No Transfer History available for this Data Transfer</div>
</div>
</div>
</c:if>
<c:if test="${historyItemsSize != '0'}">
<div class="card border-0 shadow-sm mt-3 mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock-history text-primary"></i>
<span class="fw-semibold">Transfer History</span>
<div class="ms-auto d-flex align-items-center gap-2">
  <div class="dropdown">
    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="dtHistColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
      <i class="bi bi-layout-three-columns me-1"></i>Auto
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dtHistColModeBtn">
      <li><a class="dropdown-item" href="#" data-col-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><small class="d-block text-muted ms-4">Adapts to screen width</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="all"><strong>All</strong><small class="d-block text-muted ms-0">All columns visible</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="compact"><strong>Compact</strong><small class="d-block text-muted ms-0">Hides: Err, Host</small></a></li>
      <li><hr class="dropdown-divider"></li>
      <li><a class="dropdown-item" href="#" data-col-mode="custom"><strong>Custom</strong><small class="d-block text-muted ms-0">Choose individual columns</small></a></li>
      <li id="dtHistCustomColChkPanel" style="display:none;">
        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:150px;">
          <div class="form-check mb-0"><input class="form-check-input dth-col-chk" type="checkbox" id="dthchk-0" data-col="0" checked><label class="form-check-label" for="dthchk-0">Err</label></div>
          <div class="form-check mb-0"><input class="form-check-input dth-col-chk" type="checkbox" id="dthchk-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="dthchk-1">Event Time <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dth-col-chk" type="checkbox" id="dthchk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="dthchk-2">Status <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dth-col-chk" type="checkbox" id="dthchk-3" data-col="3" checked><label class="form-check-label" for="dthchk-3">Transfer Host</label></div>
          <div class="form-check mb-0"><input class="form-check-input dth-col-chk" type="checkbox" id="dthchk-4" data-col="4" checked><label class="form-check-label" for="dthchk-4">Comment</label></div>
        </div>
      </li>
    </ul>
  </div>
  <div class="input-group flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="dtHistPageLen" class="form-select form-select-sm" style="width:auto">
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
<table id="transferHistoryTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
<thead class="table-light">
<tr>
<th>Err</th>
<th title="Event Time (UTC)">Event Time</th>
<th>Status</th>
<th>Transfer Host</th>
<th>Comment</th>
<th></th>
</tr>
</thead>
<tbody>
<c:forEach var="history" items="${historyItems}">
<tr>
<td>
<c:choose>
<c:when test="${history.error}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Err</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>OK</span></c:otherwise>
</c:choose>
</td>
<td data-order="${not empty history.date ? history.date.time : 0}">
<c:if test="${not empty canSeeHistoryDetail}">
<a href="<bean:message key="transferhistory.basepath"/>/${history.id}"><content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/></a>
</c:if>
<c:if test="${empty canSeeHistoryDetail}">
<content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
</td>
<td>
<c:choose>
  <c:when test="${history.status == 'DONE'}"><span class="badge bg-success" title="${history.formattedStatus}">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'EXEC' or history.status == 'FETC' or history.status == 'INIT'}"><span class="badge bg-primary" title="${history.formattedStatus}">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'RETR' or history.status == 'WAIT' or history.status == 'SCHE' or history.status == 'HOLD'}"><span class="badge bg-warning text-dark" title="${history.formattedStatus}">${history.formattedStatus}</span></c:when>
  <c:when test="${history.status == 'FAIL'}"><span class="badge bg-danger" title="${history.formattedStatus}">${history.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary" title="${history.formattedStatus}">${history.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
<td>
<c:if test="${history.hostName != null}">
<a href="<bean:message key="host.basepath"/>/${history.hostName}">${history.hostNickName}</a>
</c:if>
<c:if test="${history.hostName == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>${history.formattedComment}</td>
<td>${history.id}</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
</div>
</div>
<script>
$(document).ready(function() {
var _dtHistLen = (function() { try { var v = parseInt(localStorage.getItem('dtHistPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
var dtHist = $('#transferHistoryTable').DataTable({
paging:    true,
pageLength: _dtHistLen,
searching: false,
ordering:  true,
info:      true,
order:     [[1, 'desc'], [5, 'desc']],
columnDefs: [{ targets: 5, visible: false, searchable: false, type: 'num' }],
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
});
$('#dtHistPageLen').val(_dtHistLen);
$('#dtHistPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('dtHistPageLen', len); } catch(e) {}
    dtHist.page.len(len).draw();
});
// Cols:Auto for Transfer History
var _dthColMode = (function() { try { return localStorage.getItem('dtHistColMode') || 'auto'; } catch(e) { return 'auto'; } })();
var _dthCustomCols = (function() { try { var s = localStorage.getItem('dtHistCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4]; } catch(e) { return [0,1,2,3,4]; } })();
function _dthShowCols(hide) { dtHist.columns().every(function(i) { if (i < 5) dtHist.column(i).visible(hide.indexOf(i) === -1, false); }); dtHist.columns.adjust(); }
function _dthApplyCustom() { dtHist.columns().every(function(i) { if (i >= 5) return; var v = _dthCustomCols.indexOf(i) !== -1; if (i === 1 || i === 2) v = true; dtHist.column(i).visible(v, false); }); dtHist.columns.adjust(); }
function _dthApplyAuto() { if (_dthColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _dthShowCols([0,2,3]); else if (w < 992) _dthShowCols([0]); else _dthShowCols([]); }
function _dthApplyMode(mode) {
    var label = mode.charAt(0).toUpperCase() + mode.slice(1);
    $('#dtHistColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
    $('#dtHistColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
    document.getElementById('dtHistCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
    $('#dtHistColModeBtn').closest('.dropdown').find('[data-col-mode]').each(function() { $(this).find('i.bi-check').remove(); if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
    if (mode === 'auto') _dthApplyAuto(); else if (mode === 'all') _dthShowCols([]); else if (mode === 'compact') _dthShowCols([0,3]); else if (mode === 'custom') { _dthApplyCustom(); document.querySelectorAll('.dth-col-chk').forEach(function(c) { c.checked = _dthCustomCols.indexOf(+c.dataset.col) !== -1; }); }
}
document.querySelectorAll('.dth-col-chk').forEach(function(chk) {
    chk.addEventListener('change', function() { var col = +this.dataset.col; var idx = _dthCustomCols.indexOf(col); if (this.checked && idx === -1) _dthCustomCols.push(col); else if (!this.checked && idx !== -1) _dthCustomCols.splice(idx, 1); try { localStorage.setItem('dtHistCustomCols', JSON.stringify(_dthCustomCols)); } catch(e) {} if (_dthColMode === 'custom') _dthApplyCustom(); });
});
$('#dtHistColModeBtn').closest('.dropdown').on('click', '[data-col-mode]', function(e) { e.preventDefault(); _dthColMode = $(this).data('col-mode'); try { localStorage.setItem('dtHistColMode', _dthColMode); } catch(e) {} _dthApplyMode(_dthColMode); });
$(window).on('resize.dth', function() { _dthApplyAuto(); });
_dthApplyMode(_dthColMode);
});
</script>
</c:if>

<%-- Older Transfers with Same Identity --%>
<div class="card border-0 shadow-sm mt-3 mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-files text-primary"></i>
<span class="fw-semibold">Transfers for this identity</span>
<span class="text-muted" style="cursor:default" tabindex="0" data-bs-toggle="tooltip" data-bs-placement="bottom" title="<c:out value="${datatransfer.identity}"/>"><i class="bi bi-question-circle"></i></span>
<div class="ms-auto d-flex align-items-center gap-2">
  <div class="dropdown">
    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="dtOlderColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
      <i class="bi bi-layout-three-columns me-1"></i>Auto
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dtOlderColModeBtn">
      <li><a class="dropdown-item" href="#" data-col-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><small class="d-block text-muted ms-4">Adapts to screen width</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="all"><strong>All</strong><small class="d-block text-muted ms-0">All columns visible</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="compact"><strong>Compact</strong><small class="d-block text-muted ms-0">Hides: Host, Start, Finish, TS, %, Mbits/s, Prior</small></a></li>
      <li><hr class="dropdown-divider"></li>
      <li><a class="dropdown-item" href="#" data-col-mode="custom"><strong>Custom</strong><small class="d-block text-muted ms-0">Choose individual columns</small></a></li>
      <li id="dtOlderCustomColChkPanel" style="display:none;">
        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="dtochk-0">Destination <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-1" data-col="1" checked><label class="form-check-label" for="dtochk-1">Transfer Host</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-2" data-col="2" checked><label class="form-check-label" for="dtochk-2">Sched. Time</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-3" data-col="3" checked><label class="form-check-label" for="dtochk-3">Start Time</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-4" data-col="4" checked><label class="form-check-label" for="dtochk-4">Finish Time</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-5" data-col="5" checked disabled><label class="form-check-label text-muted" for="dtochk-5">Target <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-6" data-col="6" checked><label class="form-check-label" for="dtochk-6">TS</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-7" data-col="7" checked><label class="form-check-label" for="dtochk-7">%</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-8" data-col="8" checked><label class="form-check-label" for="dtochk-8">Mbits/s</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-9" data-col="9" checked><label class="form-check-label" for="dtochk-9">Status</label></div>
          <div class="form-check mb-0"><input class="form-check-input dto-col-chk" type="checkbox" id="dtochk-10" data-col="10" checked><label class="form-check-label" for="dtochk-10">Prior</label></div>
        </div>
      </li>
    </ul>
  </div>
  <div class="input-group flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="dtOlderPageLen" class="form-select form-select-sm" style="width:auto">
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
<table id="olderTransfersTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
<thead class="table-light">
<tr>
<th>Destination</th>
<th>Transfer Host</th>
<th title="Scheduled Time (UTC)">Sched. Time</th>
<th title="Start Time (UTC)">Start Time</th>
<th title="Finish Time (UTC)">Finish Time</th>
<th>Target</th>
<th>TS</th>
<th>%</th>
<th>Mbits/s</th>
<th>Status</th>
<th>Prior</th>
</tr>
</thead>
<tbody>
<c:forEach var="transfer" items="${datatransfer.olderTransfersForSameDataFile}">
<c:set var="nickName" value="${transfer.hostNickName}" />
<jsp:useBean id="nickName" type="java.lang.String" />
<tr>
<td><a href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a></td>
<td>
<c:if test='<%="".equals(nickName)%>'>
<i class="bi bi-x-circle text-warning" title="Not transferred to remote host"></i>
</c:if>
<c:if test="<%=nickName.length()>0%>">
<c:if test="${transfer.transferServerName == null}">
<a href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
</c:if>
<c:if test="${transfer.transferServerName != null}">
<a title="Transmitted through ${transfer.transferServerName}" href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
</c:if>
</c:if>
</td>
<td><content:content name="transfer.scheduledTime" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
<td>
<c:if test="${transfer.startTime != null}">
<content:content name="transfer.startTime" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
<c:if test="${transfer.startTime == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:if test="${transfer.realFinishTime != null}">
<content:content name="transfer.realFinishTime" dateFormatKey="date.format.transfer" ignoreNull="true"/>
</c:if>
<c:if test="${transfer.realFinishTime == null}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:if test="${transfer.id != datatransfer.id}">
<a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}">
<c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if>
</a>
</c:if>
<c:if test="${transfer.id == datatransfer.id}">
<span title="Size: ${transfer.formattedSize}" class="fw-semibold">
<c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if>
</span>
</c:if>
</td>
<td>${transfer.dataFile.timeStep}</td>
<td>${transfer.progress}</td>
<td>
<c:if test="${transfer.transferRate != 0}">
<a style="text-decoration:none" title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
</c:if>
<c:if test="${transfer.transferRate == 0}">
<i class="bi bi-dash text-muted" title="Not applicable"></i>
</c:if>
</td>
<td>
<c:choose>
  <c:when test="${transfer.deleted}"><span class="badge bg-danger" title="Deleted">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'DONE'}"><span class="badge bg-success" title="${transfer.formattedStatus}">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'EXEC' or transfer.statusCode == 'FETC' or transfer.statusCode == 'INIT'}"><span class="badge bg-primary" title="${transfer.formattedStatus}">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'RETR' or transfer.statusCode == 'WAIT' or transfer.statusCode == 'SCHE' or transfer.statusCode == 'HOLD'}"><span class="badge bg-warning text-dark" title="${transfer.formattedStatus}">${transfer.formattedStatus}</span></c:when>
  <c:when test="${transfer.statusCode == 'FAIL'}"><span class="badge bg-danger" title="${transfer.formattedStatus}">${transfer.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary" title="${transfer.formattedStatus}">${transfer.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
<td>${transfer.priority}</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
</div>
</div>
<script>
$(document).ready(function() {
var _dtOlderLen = (function() { try { var v = parseInt(localStorage.getItem('dtOlderPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
var dtOlder = $('#olderTransfersTable').DataTable({
paging:    true,
pageLength: _dtOlderLen,
searching: false,
ordering:  true,
info:      true,
order:     [[2, 'desc']],
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
});
$('#dtOlderPageLen').val(_dtOlderLen);
$('#dtOlderPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('dtOlderPageLen', len); } catch(e) {}
    dtOlder.page.len(len).draw();
});
// Cols:Auto for Older Transfers
var _dtoColMode = (function() { try { return localStorage.getItem('dtOlderColMode') || 'auto'; } catch(e) { return 'auto'; } })();
var _dtoCustomCols = (function() { try { var s = localStorage.getItem('dtOlderCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5,6,7,8,9,10]; } catch(e) { return [0,1,2,3,4,5,6,7,8,9,10]; } })();
var _DTO_MD = [3,4,6,7,8,10];
var _DTO_SM = [1,2,9];
function _dtoShowCols(hide) { dtOlder.columns().every(function(i) { dtOlder.column(i).visible(hide.indexOf(i) === -1, false); }); dtOlder.columns.adjust(); }
function _dtoApplyCustom() { dtOlder.columns().every(function(i) { var v = _dtoCustomCols.indexOf(i) !== -1; if (i === 0 || i === 5) v = true; dtOlder.column(i).visible(v, false); }); dtOlder.columns.adjust(); }
function _dtoApplyAuto() { if (_dtoColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _dtoShowCols(_DTO_MD.concat(_DTO_SM)); else if (w < 992) _dtoShowCols(_DTO_MD); else _dtoShowCols([]); }
function _dtoApplyMode(mode) {
    var label = mode.charAt(0).toUpperCase() + mode.slice(1);
    $('#dtOlderColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
    $('#dtOlderColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
    document.getElementById('dtOlderCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
    $('#dtOlderColModeBtn').closest('.dropdown').find('[data-col-mode]').each(function() { $(this).find('i.bi-check').remove(); if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
    if (mode === 'auto') _dtoApplyAuto(); else if (mode === 'all') _dtoShowCols([]); else if (mode === 'compact') _dtoShowCols([1,3,4,6,7,8,10]); else if (mode === 'custom') { _dtoApplyCustom(); document.querySelectorAll('.dto-col-chk').forEach(function(c) { c.checked = _dtoCustomCols.indexOf(+c.dataset.col) !== -1; }); }
}
document.querySelectorAll('.dto-col-chk').forEach(function(chk) {
    chk.addEventListener('change', function() { var col = +this.dataset.col; var idx = _dtoCustomCols.indexOf(col); if (this.checked && idx === -1) _dtoCustomCols.push(col); else if (!this.checked && idx !== -1) _dtoCustomCols.splice(idx, 1); try { localStorage.setItem('dtOlderCustomCols', JSON.stringify(_dtoCustomCols)); } catch(e) {} if (_dtoColMode === 'custom') _dtoApplyCustom(); });
});
$('#dtOlderColModeBtn').closest('.dropdown').on('click', '[data-col-mode]', function(e) { e.preventDefault(); _dtoColMode = $(this).data('col-mode'); try { localStorage.setItem('dtOlderColMode', _dtoColMode); } catch(e) {} _dtoApplyMode(_dtoColMode); });
$(window).on('resize.dto', function() { _dtoApplyAuto(); });
_dtoApplyMode(_dtoColMode);
});
</script>

</c:if>
