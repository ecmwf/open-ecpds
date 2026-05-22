<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty methods}">
    <div class="d-flex align-items-center alert alert-info mt-2 gap-2">
        No Transfer Methods found.
        <auth:link basePathKey="method.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</c:if>

<c:if test="${not empty methods}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-diagram-3 text-primary"></i>
    <span class="fw-semibold">Transfer Methods</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="methColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="methColModeBtn">
                <li><a class="dropdown-item" href="#" data-methcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-methcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-methcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Restrict, Resolve</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-methcol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                <li id="methCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input meth-col-chk" type="checkbox" id="methchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="methchk-0">Name <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input meth-col-chk" type="checkbox" id="methchk-1" data-col="1" checked><label class="form-check-label" for="methchk-1">Comment</label></div>
                    <div class="form-check mb-0"><input class="form-check-input meth-col-chk" type="checkbox" id="methchk-2" data-col="2" checked><label class="form-check-label" for="methchk-2">Restrict</label></div>
                    <div class="form-check mb-0"><input class="form-check-input meth-col-chk" type="checkbox" id="methchk-3" data-col="3" checked><label class="form-check-label" for="methchk-3">Resolve</label></div>
                    <div class="form-check mb-0"><input class="form-check-input meth-col-chk" type="checkbox" id="methchk-4" data-col="4" checked><label class="form-check-label" for="methchk-4">Enabled</label></div>
                  </div>
                </li>
            </ul>
        </div>
        <auth:link basePathKey="method.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="methodsTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Comment</th>
            <th class="text-center">Restrict</th>
            <th class="text-center">Resolve</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${methods}">
        <tr>
            <td><a href="<bean:message key="method.basepath"/>/${row.id}">${row.name}</a></td>
            <td>${row.comment}</td>
            <td class="text-center" data-order="${row.restrict ? 1 : 0}">
                <c:choose>
                    <c:when test="${row.restrict}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${row.resolve ? 1 : 0}">
                <c:choose>
                    <c:when test="${row.resolve}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${row.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${row.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/transfer/method/edit/update_form/${row.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/transfer/method/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
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
    var _methTable = $('#methodsTable').DataTable({
        paging:    false,
        searching: false,
        ordering:  true,
        info:      true,
        dom:       't<"d-flex align-items-start mt-2 px-3 pb-2"i>',
        language:  { info: 'Showing _START_-_END_ of _TOTAL_' },
        columnDefs: [{ orderable: false, targets: -1 }]
    });

    var _METH_COL_KEY        = 'methColMode';
    var _METH_CUSTOM_COL_KEY = 'methCustomCols';
    var _METH_COMPACT        = [1, 2, 3];
    var _methColMode = (function() { try { return localStorage.getItem(_METH_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
    var _methCustomCols = (function() {
        try { var s = localStorage.getItem(_METH_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
        return [0,1,2,3,4];
    })();

    function _methShowCols(hideCols) {
        var n = _methTable.columns().count();
        for (var i = 0; i < n; i++) _methTable.column(i).visible(hideCols.indexOf(i) === -1, false);
        _methTable.columns.adjust();
    }
    function _methApplyCustomCols() {
        var n = _methTable.columns().count();
        for (var i = 0; i < n; i++) {
            var visible = (i === 0 || i === 5) ? true : _methCustomCols.indexOf(i) !== -1;
            _methTable.column(i).visible(visible, false);
        }
        _methTable.columns.adjust();
    }
    function _methSyncChkBoxes() {
        document.querySelectorAll('.meth-col-chk').forEach(function(chk) {
            chk.checked = _methCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }
    document.querySelectorAll('.meth-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _methCustomCols.indexOf(col);
            if (this.checked && idx === -1) _methCustomCols.push(col);
            else if (!this.checked && idx !== -1) _methCustomCols.splice(idx, 1);
            try { localStorage.setItem(_METH_CUSTOM_COL_KEY, JSON.stringify(_methCustomCols)); } catch(e) {}
            if (_methColMode === 'custom') _methApplyCustomCols();
        });
    });
    function _methApplyResponsive() {
        if (_methColMode !== 'auto') return;
        _methShowCols(window.innerWidth < 992 ? _METH_COMPACT : []);
    }
    function _methApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#methColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#methColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#methColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('methcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        document.getElementById('methCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        if (mode === 'auto') _methApplyResponsive();
        else if (mode === 'all') _methShowCols([]);
        else if (mode === 'compact') _methShowCols(_METH_COMPACT);
        else if (mode === 'custom') { _methSyncChkBoxes(); _methApplyCustomCols(); }
    }
    $(document).on('click', '[data-methcol-mode]', function(e) {
        e.preventDefault();
        _methColMode = $(this).data('methcol-mode');
        try { localStorage.setItem(_METH_COL_KEY, _methColMode); } catch(e) {}
        _methApplyMode(_methColMode);
    });
    $(window).on('resize', _methApplyResponsive);
    _methApplyMode(_methColMode);
});
</script>
</c:if>
