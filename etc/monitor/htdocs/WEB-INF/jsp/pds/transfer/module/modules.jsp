<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty ectransmodules}">
    <div class="d-flex align-items-center alert alert-info mt-2 gap-2">
        No Transfer Modules found.
        <auth:link basePathKey="module.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</c:if>

<c:if test="${not empty ectransmodules}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-puzzle text-primary"></i>
    <span class="fw-semibold">Transfer Modules</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="modColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="modColModeBtn">
                <li><a class="dropdown-item" href="#" data-modcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-modcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-modcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Class Name, Class Path</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-modcol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                <li id="modCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input mod-col-chk" type="checkbox" id="modchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="modchk-0">Name <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input mod-col-chk" type="checkbox" id="modchk-1" data-col="1" checked><label class="form-check-label" for="modchk-1">Class Name</label></div>
                    <div class="form-check mb-0"><input class="form-check-input mod-col-chk" type="checkbox" id="modchk-2" data-col="2" checked><label class="form-check-label" for="modchk-2">Class Path</label></div>
                    <div class="form-check mb-0"><input class="form-check-input mod-col-chk" type="checkbox" id="modchk-3" data-col="3" checked><label class="form-check-label" for="modchk-3">Enabled</label></div>
                  </div>
                </li>
            </ul>
        </div>
        <auth:link basePathKey="module.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="modulesTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Name</th>
            <th>Class Name</th>
            <th>Class Path</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${ectransmodules}">
        <tr>
            <td><a href="<bean:message key="module.basepath"/>/${row.id}">${row.name}</a></td>
            <td><span class="val-code">${row.classe}</span></td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(row.archive) gt 0}"><span class="val-code">${row.archive}</span></c:when>
                    <c:otherwise><span class="text-muted fst-italic">default</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${row.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${row.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="buttons text-center">
                <c:choose>
                    <c:when test="${not empty row.guide}">
                        <button class="btn btn-sm btn-outline-info p-0 px-1" type="button"
                                data-bs-toggle="offcanvas" data-bs-target="#mgoc-${row.id}"
                                title="Configuration Guide"><i class="bi bi-book"></i></button>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-sm btn-outline-secondary p-0 px-1" type="button"
                                title="No configuration guide available" disabled><i class="bi bi-book"></i></button>
                    </c:otherwise>
                </c:choose>
                <auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/update_form/${row.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
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
    var _modTable = $('#modulesTable').DataTable({
        paging:    false,
        searching: false,
        ordering:  true,
        info:      true,
        dom:       't<"d-flex align-items-start mt-2 px-3 pb-2"i>',
        language:  { info: 'Showing _START_-_END_ of _TOTAL_' },
        columnDefs: [{ orderable: false, targets: -1 }]
    });

    var _MOD_COL_KEY        = 'modColMode';
    var _MOD_CUSTOM_COL_KEY = 'modCustomCols';
    var _MOD_COMPACT        = [1, 2];
    var _modColMode = (function() { try { return localStorage.getItem(_MOD_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
    var _modCustomCols = (function() {
        try { var s = localStorage.getItem(_MOD_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
        return [0,1,2,3];
    })();

    function _modShowCols(hideCols) {
        var n = _modTable.columns().count();
        for (var i = 0; i < n; i++) _modTable.column(i).visible(hideCols.indexOf(i) === -1, false);
        _modTable.columns.adjust();
    }
    function _modApplyCustomCols() {
        var n = _modTable.columns().count();
        for (var i = 0; i < n; i++) {
            var visible = (i === 0 || i === 4) ? true : _modCustomCols.indexOf(i) !== -1;
            _modTable.column(i).visible(visible, false);
        }
        _modTable.columns.adjust();
    }
    function _modSyncChkBoxes() {
        document.querySelectorAll('.mod-col-chk').forEach(function(chk) {
            chk.checked = _modCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }
    document.querySelectorAll('.mod-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _modCustomCols.indexOf(col);
            if (this.checked && idx === -1) _modCustomCols.push(col);
            else if (!this.checked && idx !== -1) _modCustomCols.splice(idx, 1);
            try { localStorage.setItem(_MOD_CUSTOM_COL_KEY, JSON.stringify(_modCustomCols)); } catch(e) {}
            if (_modColMode === 'custom') _modApplyCustomCols();
        });
    });
    function _modApplyResponsive() {
        if (_modColMode !== 'auto') return;
        _modShowCols(window.innerWidth < 992 ? _MOD_COMPACT : []);
    }
    function _modApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#modColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#modColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#modColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('modcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        document.getElementById('modCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        if (mode === 'auto') _modApplyResponsive();
        else if (mode === 'all') _modShowCols([]);
        else if (mode === 'compact') _modShowCols(_MOD_COMPACT);
        else if (mode === 'custom') { _modSyncChkBoxes(); _modApplyCustomCols(); }
    }
    $(document).on('click', '[data-modcol-mode]', function(e) {
        e.preventDefault();
        _modColMode = $(this).data('modcol-mode');
        try { localStorage.setItem(_MOD_COL_KEY, _modColMode); } catch(e) {}
        _modApplyMode(_modColMode);
    });
    $(window).on('resize', _modApplyResponsive);
    _modApplyMode(_modColMode);
});
</script>

<%-- Guide offcanvases for modules that have documentation --%>
<c:forEach var="row" items="${ectransmodules}">
    <c:if test="${not empty row.guide}">
        <jsp:include page="${row.guide}">
            <jsp:param name="guideId" value="mgoc-${row.id}"/>
        </jsp:include>
    </c:if>
</c:forEach>

</c:if>
