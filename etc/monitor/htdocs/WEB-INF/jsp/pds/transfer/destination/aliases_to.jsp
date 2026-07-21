<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:set var="aliases" value="${destination.aliases}" scope="page"/>

<c:if test="${empty aliases}">
<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
  <i class="bi bi-info-circle-fill"></i>
  <span>No alias defined on destination <strong><c:out value="${destination.name}"/></strong>.</span>
</div>
</c:if>

<c:if test="${not empty aliases}">

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-arrow-right-circle text-primary"></i>
    <span class="fw-semibold">Destination <c:out value="${destination.name}"/> has Aliases To the following Destination(s)</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="aliasToSearch" class="form-control" placeholder="Filter..." style="min-width:160px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="aliasToPageLen" class="form-select form-select-sm" style="width:auto">
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
<table id="aliasToTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Status</th>
            <th>Rules</th>
            <th>Comment</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="alias" items="${aliases}">
        <tr>
            <td><c:choose>
              <c:when test="${empty accessibleDestNames or accessibleDestNames.contains(alias.name)}">
                <a href="/do/transfer/destination/${alias.name}">${alias.name}</a>
              </c:when>
              <c:otherwise>
                <span class="text-muted" title="You do not have access to this destination">${alias.name}</span>
              </c:otherwise>
            </c:choose></td>
            <td>${alias.typeText}</td>
            <td>
<c:set var="_sb" value="${fn:contains(alias.formattedStatus, '-') ? fn:substringBefore(alias.formattedStatus, '-') : alias.formattedStatus}"/>
<c:choose>
  <c:when test="${_sb == 'Running'}"><span class="badge bg-success">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Restarting' or _sb == 'Resending'}"><span class="badge bg-info text-dark">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Waiting' or _sb == 'Retrying' or _sb == 'Interrupted'}"><span class="badge bg-warning text-dark">${alias.formattedStatus}</span></c:when>
  <c:when test="${_sb == 'Initialized' or _sb == 'Stopped' or _sb == 'NoHosts' or _sb == 'Failed'}"><span class="badge bg-danger">${alias.formattedStatus}</span></c:when>
  <c:otherwise><span class="badge bg-secondary">${alias.formattedStatus}</span></c:otherwise>
</c:choose>
</td>
            <td>${alias.dataAlias}</td>
            <td>${alias.comment}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</div>
</div>
<script>
$(function() {
    var table = $('#aliasToTable').DataTable({
        paging:    true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('aliasToPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching: true,
        ordering:  true,
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        language: { info: 'Showing _START_-_END_ of _TOTAL_' }
    });
    $('#aliasToSearch').on('keyup', function() { table.search(this.value).draw(); });
    var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('aliasToPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    $('#aliasToPageLen').val(_savedPageLen);
    $('#aliasToPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('aliasToPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
});
</script>

</c:if>
