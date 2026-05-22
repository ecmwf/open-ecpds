<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty resources}">
<div class="d-flex align-items-center alert alert-info mt-2 gap-2">
    No Web Resources found.
    <a href="<bean:message key="resource.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</a>
</div>
</c:if>

<c:if test="${not empty resources}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-key text-primary"></i>
    <span class="fw-semibold">Web Resources</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="resourcesSearch" class="form-control" placeholder="Search resources..." style="min-width:180px">
        </div>
        <div class="input-group flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="resourcesPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="resColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="resColModeBtn">
                        <li><a class="dropdown-item" href="#" data-res-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-res-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-res-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Categories</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-res-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="resCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input res-col-chk" type="checkbox" id="reschk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="reschk-0">Resource Path <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input res-col-chk" type="checkbox" id="reschk-1" data-col="1" checked><label class="form-check-label" for="reschk-1">Categories</label></div>
            <div class="form-check mb-0"><input class="form-check-input res-col-chk" type="checkbox" id="reschk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="reschk-2">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="resource.basepath"/>/edit/insert_form" class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="resourcesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Resource Path</th>
            <th>Categories</th>
            <th class="text-center">Actions</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="resource" items="${resources}">
        <tr>
            <td><a href="/do/user/resource/${resource.id}">${resource.path}</a></td>
            <td>
                <c:forEach var="category" items="${resource.categories}">
                    <a href="<bean:message key="category.basepath"/>/${category.id}" title="${category.description}">${category.name}</a>&nbsp;
                </c:forEach>
            </td>
            <td class="buttons text-center">
                <auth:link styleClass="menuitem" basePathKey="accesscontrol.basepath" href="/detailer?page=${resource.id}" imageKey="icon.small.text" imageTitleKey="ecpds.user.detailer"/>
                <auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/update_form/${resource.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" basePathKey="resource.basepath" href="/edit/delete_form/${resource.id}" imageKey="icon.small.delete"/>
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
    var table = $('#resourcesTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('resourcesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  true,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _len = (function() { try { var v = parseInt(localStorage.getItem('resourcesPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_len).draw(false);
    $('#resourcesPageLen').val(_len);
    $('#resourcesPageLen').on('change', function() { var len = +this.value; try { localStorage.setItem('resourcesPageLen', len); } catch(e) {} table.page.len(len).draw(); });
    $('#resourcesSearch').on('keyup', function() { table.search(this.value).draw(); });

        /* ---- Cols:Auto ---- */
        var _resColKey        = 'resColMode';
        var _resCustomColKey  = 'resCustomCols';
        var _resCompact       = [1];
        var _resColMode = (function() { try { return localStorage.getItem(_resColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _resCustomCols = (function() {
            try { var s = localStorage.getItem(_resCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2];
        })();
        function _resShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
            table.columns.adjust();
        }
        function _resApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                table.column(i).visible((i === 0 || i === 2) ? true : _resCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _resSyncChkBoxes() {
            document.querySelectorAll('.res-col-chk').forEach(function(chk) {
                chk.checked = _resCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.res-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _resCustomCols.indexOf(col);
                if (this.checked && idx === -1) _resCustomCols.push(col);
                else if (!this.checked && idx !== -1) _resCustomCols.splice(idx, 1);
                try { localStorage.setItem(_resCustomColKey, JSON.stringify(_resCustomCols)); } catch(e) {}
                if (_resColMode === 'custom') _resApplyCustomCols();
            });
        });
        function _resApplyResponsive() {
            if (_resColMode !== 'auto') return;
            _resShowCols(window.innerWidth < 768 ? [1] : []);
        }
        function _resApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#resColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#resColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#resColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('res-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('resCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _resApplyResponsive();
            else if (mode === 'all') _resShowCols([]);
            else if (mode === 'compact') _resShowCols(_resCompact);
            else if (mode === 'custom') { _resSyncChkBoxes(); _resApplyCustomCols(); }
        }
        $(window).on('resize', _resApplyResponsive);
        _resApplyMode(_resColMode);
        $('#resColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('res-mode');
            if (!mode) return;
            _resColMode = mode;
            try { localStorage.setItem(_resColKey, mode); } catch(e) {}
            _resApplyMode(mode);
        });
});
</script>
</c:if>
