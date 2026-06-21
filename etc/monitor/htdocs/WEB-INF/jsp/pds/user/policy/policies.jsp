<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty policies}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Data Policies found.
    <a href="<bean:message key="policy.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty policies}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-shield-check text-primary"></i>
    <span class="fw-semibold">Data Policies</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="policiesSearch" class="form-control" placeholder="Search policies..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="policiesPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="polColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="polColModeBtn">
                        <li><a class="dropdown-item" href="#" data-pol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-pol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-pol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Associated Destinations, Comment</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-pol-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="polCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input pol-col-chk" type="checkbox" id="polchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="polchk-0">Name <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input pol-col-chk" type="checkbox" id="polchk-1" data-col="1" checked><label class="form-check-label" for="polchk-1">Associated Destinations</label></div>
            <div class="form-check mb-0"><input class="form-check-input pol-col-chk" type="checkbox" id="polchk-2" data-col="2" checked><label class="form-check-label" for="polchk-2">Enabled</label></div>
            <div class="form-check mb-0"><input class="form-check-input pol-col-chk" type="checkbox" id="polchk-3" data-col="3" checked><label class="form-check-label" for="polchk-3">Comment</label></div>
            <div class="form-check mb-0"><input class="form-check-input pol-col-chk" type="checkbox" id="polchk-4" data-col="4" checked disabled><label class="form-check-label text-muted" for="polchk-4">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="policy.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="policiesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-warning">
        <tr>
            <th>Name</th>
            <th>Associated Destinations</th>
            <th class="text-center">Enabled</th>
            <th>Comment</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="policy" items="${policies}">
        <tr>
            <td><a href="<bean:message key="policy.basepath"/>/${policy.id}">${policy.id}</a></td>
            <td>
                <c:forEach var="destination" items="${policy.associatedDestinations}">
                    <a href="<bean:message key="destination.basepath"/>/${destination.name}" title="${destination.comment}">${destination.name}</a>&nbsp;
                </c:forEach>
            </td>
            <td class="text-center" data-order="${policy.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${policy.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td>${policy.comment}</td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" href="/do/user/policy/edit/update_form/${policy.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/policy/edit/delete_form/${policy.id}" imageKey="icon.small.delete"/>
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
    var table = $('#policiesTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('policiesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('policiesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#policiesPageLen').val(_len);
    $('#policiesPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('policiesPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#policiesSearch').on('keyup', function() { table.search(this.value).draw(); });

        /* ---- Cols:Auto ---- */
        var _polColKey        = 'polColMode';
        var _polCustomColKey  = 'polCustomCols';
        var _polCompact       = [1, 3];
        var _polColMode = (function() { try { return localStorage.getItem(_polColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _polCustomCols = (function() {
            try { var s = localStorage.getItem(_polCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4];
        })();
        function _polShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
            table.columns.adjust();
        }
        function _polApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                table.column(i).visible((i === 0 || i === 4) ? true : _polCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _polSyncChkBoxes() {
            document.querySelectorAll('.pol-col-chk').forEach(function(chk) {
                chk.checked = _polCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.pol-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _polCustomCols.indexOf(col);
                if (this.checked && idx === -1) _polCustomCols.push(col);
                else if (!this.checked && idx !== -1) _polCustomCols.splice(idx, 1);
                try { localStorage.setItem(_polCustomColKey, JSON.stringify(_polCustomCols)); } catch(e) {}
                if (_polColMode === 'custom') _polApplyCustomCols();
            });
        });
        function _polApplyResponsive() {
            if (_polColMode !== 'auto') return;
            _polShowCols(window.innerWidth < 768 ? [1, 3] : []);
        }
        function _polApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#polColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#polColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#polColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('pol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('polCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _polApplyResponsive();
            else if (mode === 'all') _polShowCols([]);
            else if (mode === 'compact') _polShowCols(_polCompact);
            else if (mode === 'custom') { _polSyncChkBoxes(); _polApplyCustomCols(); }
        }
        $(window).on('resize', _polApplyResponsive);
        _polApplyMode(_polColMode);
        $('#polColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('pol-mode');
            if (!mode) return;
            _polColMode = mode;
            try { localStorage.setItem(_polColKey, mode); } catch(e) {}
            _polApplyMode(mode);
        });
});
</script>
</c:if>
