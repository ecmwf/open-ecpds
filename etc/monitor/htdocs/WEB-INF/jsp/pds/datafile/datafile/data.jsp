<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:importAttribute name="isDelete" ignore="true"/>
<c:if test="${not empty isDelete}">
<tiles:insert page="./pds/datafile/datafile/warning.jsp"/>
</c:if>
<c:if test="${empty isDelete}">

<div class="dest-page-header mb-3">
<div class="d-flex align-items-center gap-2 flex-wrap mb-1">
<i class="bi bi-file-earmark-text text-primary flex-shrink-0"></i>
<span class="fw-semibold">Data File:&nbsp;<strong><c:out value="${datafile.id}"/></strong></span>

<%-- Desktop: full icon bar, hidden on mobile --%>
<div id="_dfIconBar" class="d-none d-sm-flex gap-2 align-items-center ms-auto">
<auth:if basePathKey="datafile.basepath" paths="">
<auth:then>
<a href='<bean:message key="datafile.basepath"/>' class="btn btn-sm btn-outline-secondary" title="All Data Files"><i class="bi bi-arrow-left"></i></a>
</auth:then>
</auth:if>
<auth:if paths="/do/datafile/datafile/edit/delete_form/${datafile.id}">
<auth:then>
<div class="d-flex gap-1 align-items-center" style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;">
<c:choose>
<c:when test="${!datafile.deleted}">
<a href='/do/datafile/datafile/edit/delete_form/${datafile.id}'
   class="btn btn-sm btn-outline-danger" title="Delete this Data File"><i class="bi bi-trash"></i></a>
</c:when>
<c:otherwise>
<button class="btn btn-sm btn-outline-danger" disabled title="Already deleted"><i class="bi bi-trash"></i></button>
</c:otherwise>
</c:choose>
</div>
</auth:then>
</auth:if>
</div><%-- end #_dfIconBar --%>

<%-- Mobile: ⋯ dropdown, hidden on sm+ --%>
<div class="d-sm-none ms-auto">
    <div class="dropdown">
        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                id="_dfActionsToggle" data-bs-toggle="dropdown" aria-expanded="false"
                title="Actions">
            <i class="bi bi-three-dots"></i>
        </button>
        <ul class="dropdown-menu dropdown-menu-end" id="_dfActionsMenu" aria-labelledby="_dfActionsToggle"></ul>
    </div>
</div>
<script>
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        var bar  = document.getElementById('_dfIconBar');
        var menu = document.getElementById('_dfActionsMenu');
        if (!bar || !menu) return;
        function addItem(a) {
            var li   = document.createElement('li');
            var item = document.createElement('a');
            item.className = 'dropdown-item';
            item.href = a.getAttribute('href');
            var ic = a.querySelector('i[class]');
            if (ic) {
                var icon = document.createElement('i');
                icon.className = ic.className + ' me-2';
                item.appendChild(icon);
            }
            item.appendChild(document.createTextNode(a.title || a.textContent.trim()));
            li.appendChild(item);
            menu.appendChild(li);
        }
        function addDivider() {
            if (menu.children.length === 0) return;
            var li = document.createElement('li');
            li.innerHTML = '<hr class="dropdown-divider m-1">';
            menu.appendChild(li);
        }
        Array.from(bar.children).forEach(function(child) {
            if (child.tagName === 'A') {
                addItem(child);
            } else if (child.tagName === 'DIV' && child.querySelector('a')) {
                addDivider();
                Array.from(child.querySelectorAll('a')).forEach(addItem);
            }
        });
    });
})();
</script>
</div>
</div>

<%-- Stat bar --%>
<div class="row g-2 mb-3">
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Size</div>
<div class="fw-bold fs-6">${datafile.formattedSize}</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Delete Original</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.deleteOriginal}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Deleted</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.deleted}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
<div class="col-6 col-sm-3">
<div class="card border-0 shadow-sm text-center py-2 h-100">
<div class="text-muted small fw-semibold text-uppercase" style="font-size:0.7rem;letter-spacing:0.04em">Removed</div>
<div class="fw-bold fs-6">
<c:choose>
<c:when test="${datafile.removed}"><span class="badge rounded-pill border fw-normal bg-danger-subtle text-danger-emphasis"><i class="bi bi-x-circle-fill me-1"></i>Yes</span></c:when>
<c:otherwise><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>No</span></c:otherwise>
</c:choose>
</div>
</div>
</div>
</div>

<%-- Card: File Details --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-file-earmark-code text-primary"></i>
<span class="fw-semibold d-inline-flex align-items-center gap-1">File Details
    <button class="btn btn-link p-0 ms-1" style="font-size:0.85rem;line-height:1;vertical-align:middle;color:var(--bs-secondary-color);"
            data-bs-toggle="collapse" data-bs-target="#fileInfoInfoPanel"
            aria-expanded="false" aria-controls="fileInfoInfoPanel"
            title="What is a data file?">
        <i class="bi bi-info-circle"></i>
    </button>
</span>
</div>
<div class="collapse" id="fileInfoInfoPanel">
    <div class="px-3 pt-2 pb-3 border-bottom small" style="background:var(--bs-secondary-bg)">
        A data file is a record of a product stored in the <strong><%=System.getProperty("monitor.title")%></strong> with a one-to-one
        mapping between the data file and the product. The data file contains information on the physical specifications
        of the product, such as its size, type, compression and entity tag (ETag) in the
        <strong><%=System.getProperty("monitor.title")%></strong>, as well as the metadata associated with it by the data provider
        (e.g. meteorological parameters, name or comments concerning the product).
    </div>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">ID</div><div class="field-value"><span class="val-code">${datafile.id}</span></div></div>
<div class="field-row"><div class="field-label">Source</div><div class="field-value"><span class="val-code text-break d-inline-block">${datafile.formattedOriginal}</span></div></div>
<div class="field-row"><div class="field-label">Checksum</div><div class="field-value"><c:choose><c:when test="${not empty datafile.checksum}"><span class="val-code text-break d-inline-block">${datafile.checksum}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Storage Path</div><div class="field-value"><c:choose><c:when test="${not empty datafile.storagePath}"><span class="val-code text-break d-inline-block">${datafile.storagePath}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Group By</div><div class="field-value"><c:choose><c:when test="${not empty datafile.groupBy}"><span class="val-code">${datafile.groupBy}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Additional Info</div><div class="field-value"><c:choose><c:when test="${not empty datafile.formattedCaller}"><span class="val-code">${datafile.formattedCaller}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Index</div><div class="field-value"><c:choose><c:when test="${datafile.index != 0}"><span class="val-num">${datafile.index} file(s)</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Card: Timing --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-clock text-primary"></i>
<span class="fw-semibold">Times (UTC)</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<div class="field-row"><div class="field-label">Product Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.productTime}"><content:content name="datafile.productTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Earliest</div><div class="field-value"><c:choose><c:when test="${not empty datafile.earliestTime}"><content:content name="datafile.earliestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Timestep</div><div class="field-value"><span class="val-num">${datafile.timeStep}</span></div></div>
<div class="field-row"><div class="field-label">Generation Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.productGenerationTime}"><content:content name="datafile.productGenerationTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Latest</div><div class="field-value"><c:choose><c:when test="${not empty datafile.latestTime}"><content:content name="datafile.latestTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Arrival Date</div><div class="field-value"><c:choose><c:when test="${not empty datafile.arrivedTime}"><content:content name="datafile.arrivedTime" dateFormatKey="date.format.long.iso" ignoreNull="true"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Predicted</div><div class="field-value"><c:choose><c:when test="${not empty datafile.predictedTime}"><content:content name="datafile.predictedTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="-"/></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Card: Acquisition --%>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-cloud-download text-primary"></i>
<span class="fw-semibold">Acquisition</span>
</div>
<div class="card-body py-0">
<div class="field-grid">
<c:if test="${datafile.groupBy == null}">
<div class="field-row"><div class="field-label">Pushed To</div><div class="field-value"><c:choose><c:when test="${datafile.getHost == null}"><span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-pushed</span></c:when><c:otherwise><span class="val-code">${datafile.getHost}</span> <span class="text-muted small">(in <span class="val-num">${datafile.getDuration}</span> + <span class="val-num">${datafile.getProtocolOverhead}</span> overhead)</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Retrieved From</div><div class="field-value"><c:choose><c:when test="${datafile.getHost == null}"><span class="text-danger"><i class="bi bi-x-circle me-1"></i>not-retrieved</span></c:when><c:otherwise><span class="val-code">${datafile.getHost}</span> <span class="text-muted small">(in <span class="val-num">${datafile.getDuration}</span> + <span class="val-num">${datafile.getProtocolOverhead}</span> overhead)</span></c:otherwise></c:choose></div></div>
</c:if>
<div class="field-row"><div class="field-label">Remote Host</div><div class="field-value"><c:choose><c:when test="${not empty datafile.remoteHost}"><span class="val-code">${datafile.remoteHost}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Submitted From</div><div class="field-value"><c:choose><c:when test="${not empty datafile.ecauthHost}"><span class="val-code">${datafile.ecauthHost}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
<div class="field-row"><div class="field-label">Submitted By</div><div class="field-value"><c:choose><c:when test="${not empty datafile.ecauthUser}"><span class="val-code">${datafile.ecauthUser}</span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
</div>
</div>
</div>

<%-- Metadata table --%>
<c:if test="${empty datafile.metaData}">
<div class="card border-0 shadow-sm mb-3 mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-tags text-primary"></i>
<span class="fw-semibold">Meta Data</span>
</div>
<div class="card-body">
<div class="alert alert-info mb-0">No Meta Data for <c:out value="${datafile.id}"/></div>
</div>
</div>
</c:if>
<c:if test="${not empty datafile.metaData}">
<div class="card border-0 shadow-sm mb-3 mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-tags text-primary"></i>
<span class="fw-semibold">Meta Data</span>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
  <div class="input-group flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="dfMetaPageLen" class="form-select form-select-sm" style="width:auto">
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
<table id="metadataTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
<thead class="table-primary">
<tr>
<th>Name</th>
<th>Value</th>
</tr>
</thead>
<tbody>
<c:forEach var="metadata" items="${datafile.metaData}">
<tr>
<td><a href="/do/datafile/metadata/attribute/${metadata.name}">${metadata.name}</a></td>
<td>${metadata.value}</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
</div>
</div>
<script>
$(document).ready(function() {
var _dfMetaLen = (function() { try { var v = parseInt(localStorage.getItem('dfMetaPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
var dfMetaTable = $('#metadataTable').DataTable({ paging: true, pageLength: _dfMetaLen, searching: false, ordering: true, info: true,
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>' });
$('#dfMetaPageLen').val(_dfMetaLen);
$('#dfMetaPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('dfMetaPageLen', len); } catch(e) {}
    dfMetaTable.page.len(len).draw();
});
});
</script>
</c:if>

<%-- Transfers table --%>
<div class="card border-0 shadow-sm mb-3 mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-arrow-left-right text-primary"></i>
<span class="fw-semibold">Transfers for this datafile</span>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
  <div class="dropdown">
    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="dfTransColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
      <i class="bi bi-layout-three-columns me-1"></i>Auto
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dfTransColModeBtn">
      <li><a class="dropdown-item" href="#" data-col-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><small class="d-block text-muted ms-4">Adapts to screen width</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="all"><strong>All</strong><small class="d-block text-muted ms-0">All columns visible</small></a></li>
      <li><a class="dropdown-item" href="#" data-col-mode="compact"><strong>Compact</strong><small class="d-block text-muted ms-0">Hides: Host, %, Mbits/s, Prior</small></a></li>
      <li><hr class="dropdown-divider"></li>
      <li><a class="dropdown-item" href="#" data-col-mode="custom"><strong>Custom</strong><small class="d-block text-muted ms-0">Choose individual columns</small></a></li>
      <li id="dfTransCustomColChkPanel" style="display:none;">
        <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="dftchk-0">Destination <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-1" data-col="1" checked><label class="form-check-label" for="dftchk-1">Transfer Host</label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-2" data-col="2" checked><label class="form-check-label" for="dftchk-2">Sched. Time</label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-3" data-col="3" checked disabled><label class="form-check-label text-muted" for="dftchk-3">Target <small>(required)</small></label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-4" data-col="4" checked><label class="form-check-label" for="dftchk-4">%</label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-5" data-col="5" checked><label class="form-check-label" for="dftchk-5">Mbits/s</label></div>
          <div class="form-check mb-0"><input class="form-check-input dft-col-chk" type="checkbox" id="dftchk-6" data-col="6" checked><label class="form-check-label" for="dftchk-6">Prior</label></div>
        </div>
      </li>
    </ul>
  </div>
  <div class="input-group flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="dfTransPageLen" class="form-select form-select-sm" style="width:auto">
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
<table id="datafileTransfersTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
<thead class="table-primary">
<tr>
<th>Destination</th>
<th>Transfer Host</th>
<th title="Scheduled Time (UTC)">Sched. Time</th>
<th>Target</th>
<th>%</th>
<th>Mbits/s</th>
<th>Prior</th>
</tr>
</thead>
<tbody>
<c:forEach var="transfer" items="${datafile.dataTransfers}">
<c:set var="nickName" value="${transfer.hostNickName}" />
<jsp:useBean id="nickName" type="java.lang.String" />
<tr>
<td><a title="${transfer.destination.comment}" href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a></td>
<td>
<c:if test='<%="".equals(nickName)%>'>
<span class="text-muted">&ndash;</span>
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
<td data-order="${transfer.scheduledTime.time}"><content:content name="transfer.scheduledTime" dateFormatKey="date.format.transfer" ignoreNull="true"/></td>
<td><a title="Size: ${transfer.formattedSize}" href="/do/transfer/data/${transfer.id}"><c:if test="${transfer.deleted}"><span class="text-danger"></c:if>${transfer.target}<c:if test="${transfer.deleted}"></span></c:if></a></td>
<td><c:choose><c:when test="${not empty transfer.progress}">${transfer.progress}</c:when><c:otherwise><span class="text-muted">&ndash;</span></c:otherwise></c:choose></td>
<td>
<c:if test="${transfer.transferRate != 0}">
<a style="text-decoration:none" title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
</c:if>
<c:if test="${transfer.transferRate == 0}">
<span class="text-muted">&ndash;</span>
</c:if>
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
var _dftLen = (function() { try { var v = parseInt(localStorage.getItem('dfTransPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
var dftTable = $('#datafileTransfersTable').DataTable({
paging:    true,
pageLength: _dftLen,
searching: false,
ordering:  true,
info:      true,
order:     [[2, 'asc']],
autoWidth: false,
columns: [
    { width: '110px' },
    { width: '110px' },
    { width: '130px' },
    null,
    { width: '45px',  className: 'text-nowrap' },
    { width: '70px',  className: 'text-nowrap' },
    { width: '45px',  className: 'text-nowrap' }
],
dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
});
$('#dfTransPageLen').val(_dftLen);
$('#dfTransPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('dfTransPageLen', len); } catch(e) {}
    dftTable.page.len(len).draw();
});
// Cols:Auto
var _dftColMode = (function() { try { return localStorage.getItem('dfTransColMode') || 'auto'; } catch(e) { return 'auto'; } })();
var _dftCustomCols = (function() { try { var s = localStorage.getItem('dfTransCustomCols'); return s ? JSON.parse(s) : [0,1,2,3,4,5,6]; } catch(e) { return [0,1,2,3,4,5,6]; } })();
var _DFT_MD = [4,5,6];
var _DFT_SM = [1,2];
function _dftShowCols(hide) { dftTable.columns().every(function(i) { dftTable.column(i).visible(hide.indexOf(i) === -1, false); }); dftTable.columns.adjust(); }
function _dftApplyCustom() { dftTable.columns().every(function(i) { var v = _dftCustomCols.indexOf(i) !== -1; if (i === 0 || i === 3) v = true; dftTable.column(i).visible(v, false); }); dftTable.columns.adjust(); }
function _dftApplyAuto() { if (_dftColMode !== 'auto') return; var w = window.innerWidth; if (w < 768) _dftShowCols(_DFT_MD.concat(_DFT_SM)); else if (w < 992) _dftShowCols(_DFT_MD); else _dftShowCols([]); }
function _dftApplyMode(mode) {
    var label = mode.charAt(0).toUpperCase() + mode.slice(1);
    $('#dfTransColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
    $('#dfTransColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
    document.getElementById('dfTransCustomColChkPanel').style.display = mode === 'custom' ? '' : 'none';
    $('#dfTransColModeBtn').closest('.dropdown').find('[data-col-mode]').each(function() { $(this).find('i.bi-check').remove(); if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>'); });
    if (mode === 'auto') _dftApplyAuto(); else if (mode === 'all') _dftShowCols([]); else if (mode === 'compact') _dftShowCols([1,4,5,6]); else if (mode === 'custom') { _dftApplyCustom(); document.querySelectorAll('.dft-col-chk').forEach(function(c) { c.checked = _dftCustomCols.indexOf(+c.dataset.col) !== -1; }); }
}
document.querySelectorAll('.dft-col-chk').forEach(function(chk) {
    chk.addEventListener('change', function() { var col = +this.dataset.col; var idx = _dftCustomCols.indexOf(col); if (this.checked && idx === -1) _dftCustomCols.push(col); else if (!this.checked && idx !== -1) _dftCustomCols.splice(idx, 1); try { localStorage.setItem('dfTransCustomCols', JSON.stringify(_dftCustomCols)); } catch(e) {} if (_dftColMode === 'custom') _dftApplyCustom(); });
});
$('#dfTransColModeBtn').closest('.dropdown').on('click', '[data-col-mode]', function(e) { e.preventDefault(); _dftColMode = $(this).data('col-mode'); try { localStorage.setItem('dfTransColMode', _dftColMode); } catch(e) {} _dftApplyMode(_dftColMode); });
$(window).on('resize.dft', function() { _dftApplyAuto(); });
_dftApplyMode(_dftColMode);
});
</script>
</c:if>
