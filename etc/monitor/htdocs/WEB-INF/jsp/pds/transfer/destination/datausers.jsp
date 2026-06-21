<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-success"></i>
    <span class="fw-semibold">Data Users</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="destUsersSearch" class="form-control" placeholder="Search login..."
                   autocomplete="off" style="min-width:120px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="destUsersPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <div class="dropdown">
            <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="destUsrColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="destUsrColModeBtn">
                <li><a class="dropdown-item" href="#" data-destusr-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-destusr-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-destusr-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Country</small></a></li>
            </ul>
        </div>
    </div>
</div>

<div class="card-body p-0">
<c:choose>
<c:when test="${empty incomingUsers}">
    <div class="alert alert-info d-flex align-items-center gap-2 m-3">
        <i class="bi bi-info-circle-fill"></i>
        <span>No data users are associated with this destination.</span>
    </div>
</c:when>
<c:otherwise>
<div class="table-responsive">
<table id="destUsersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Data Login</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">Association</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="user" items="${directUsers}">
        <tr>
            <td><auth:if basePathKey="incoming.basepath" paths="/"><auth:then><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></auth:then><auth:else>${user.id}</auth:else></auth:if></td>
            <td>${user.comment}</td>
            <td>${user.country.name}</td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${user.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center"><span class="badge rounded-pill border fw-normal bg-primary-subtle text-primary-emphasis" title="Directly assigned to this destination"><i class="bi bi-person-check me-1"></i>Direct</span></td>
        </tr>
        </c:forEach>
        <c:forEach var="user" items="${policyUsers}">
        <tr>
            <td><auth:if basePathKey="incoming.basepath" paths="/"><auth:then><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></auth:then><auth:else>${user.id}</auth:else></auth:if></td>
            <td>${user.comment}</td>
            <td>${user.country.name}</td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${user.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center"><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis" title="Access via a Data Policy"><i class="bi bi-diagram-3 me-1"></i>Via Policy</span></td>
        </tr>
        </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
</div>
</div>

<div class="mt-3">
    <auth:if basePathKey="incoming.basepath" paths="/">
        <auth:then>
            <a href="<bean:message key="incoming.basepath"/>?destinationNameForSearch=<c:out value="${destination.id}"/>"
               class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-box-arrow-up-right"></i> Manage in Data Users
            </a>
        </auth:then>
    </auth:if>
</div>

<c:if test="${not empty incomingUsers}">
<script>
$(document).ready(function() {
    var _destUsrColKey     = 'destUsrColMode';
    var _destUsrCompact    = [1, 2];
    var _destUsrColMode    = (function() { try { return localStorage.getItem(_destUsrColKey) || 'auto'; } catch(e) { return 'auto'; } })();
    var _destUsrPageLenKey = 'destUsrPageLen';

    var table = $('#destUsersTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem(_destUsrPageLenKey), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        language:   { emptyTable: 'No data users found.' },
        columnDefs: [
            { orderData: [5], targets: [3] },
            { visible: false, targets: [5] }
        ],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });

    $('#destUsersPageLen').val((function() { try { var v = parseInt(localStorage.getItem(_destUsrPageLenKey), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
    $('#destUsersPageLen').on('change', function() {
        var len = parseInt(this.value);
        try { localStorage.setItem(_destUsrPageLenKey, len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#destUsersSearch').on('input', function() { table.search(this.value).draw(); });

    function _destUsrShowCols(hideCols) {
        for (var i = 0; i < 5; i++) {
            table.column(i).visible(hideCols.indexOf(i) === -1, false);
        }
        table.columns.adjust();
    }
    function _destUsrApplyResponsive() {
        if (_destUsrColMode !== 'auto') return;
        _destUsrShowCols(window.innerWidth < 992 ? _destUsrCompact : []);
    }
    function _destUsrApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#destUsrColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#destUsrColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#destUsrColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('destusr-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        if (mode === 'auto') _destUsrApplyResponsive();
        else if (mode === 'all') _destUsrShowCols([]);
        else if (mode === 'compact') _destUsrShowCols(_destUsrCompact);
    }
    $(window).on('resize', _destUsrApplyResponsive);
    _destUsrApplyMode(_destUsrColMode);
    $('#destUsrColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
        e.preventDefault();
        var mode = $(this).data('destusr-mode');
        if (!mode) return;
        _destUsrColMode = mode;
        try { localStorage.setItem(_destUsrColKey, mode); } catch(e) {}
        _destUsrApplyMode(mode);
    });
});
</script>
</c:if>
