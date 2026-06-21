<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select"/>

<c:if test="${empty events}">
<br/>
<div class="alert">
  No Data Events found based on these criteria!
</div>
</c:if>

<c:if test="${!empty events}">
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-clock-history text-primary"></i>
    <span class="fw-semibold">Events for <auth:link basePathKey="user.basepath" href="/${user.id}">Web User ${user.uid}</auth:link></span>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="userEventsSearch" class="form-control" placeholder="Search events..." style="min-width:180px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="userEventsPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option><option value="250">250</option>
            </select>
        </div>
        <div class="dropdown">
            <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="ueaColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="ueaColModeBtn">
                <li><a class="dropdown-item" href="#" data-uea-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-uea-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-uea-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Host, Comment</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-uea-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                <li id="ueaCustomColChkPanel" style="display:none;">
                    <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
                        <div class="form-check mb-0"><input class="form-check-input uea-col-chk" type="checkbox" id="ueachk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="ueachk-0">Time <small>(required)</small></label></div>
                        <div class="form-check mb-0"><input class="form-check-input uea-col-chk" type="checkbox" id="ueachk-1" data-col="1" checked><label class="form-check-label" for="ueachk-1">Host</label></div>
                        <div class="form-check mb-0"><input class="form-check-input uea-col-chk" type="checkbox" id="ueachk-2" data-col="2" checked disabled><label class="form-check-label text-muted" for="ueachk-2">Action <small>(required)</small></label></div>
                        <div class="form-check mb-0"><input class="form-check-input uea-col-chk" type="checkbox" id="ueachk-3" data-col="3" checked><label class="form-check-label" for="ueachk-3">Comment</label></div>
                        <div class="form-check mb-0"><input class="form-check-input uea-col-chk" type="checkbox" id="ueachk-4" data-col="4" checked disabled><label class="form-check-label text-muted" for="ueachk-4">Link <small>(required)</small></label></div>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="userEventsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-warning">
        <tr>
            <th title="Time (UTC)">Time</th>
            <th>Host</th>
            <th>Action</th>
            <th>Comment</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="event" items="${events}">
        <tr>
            <td>${event.time}</td>
            <td>${event.activity.host}</td>
            <td>${event.action}</td>
            <td>${event.comment}</td>
            <td>
                <c:if test="${event.type != '' and event.type != 'lost'}">
                    <c:set var="eventBasepath" value=""/>
                    <c:catch><c:set var="eventBasepath"><bean:message key="${event.type}.basepath"/></c:set></c:catch>
                    <c:if test="${not empty eventBasepath}">
                        <a href="${eventBasepath}/${event.linkId}"><content:icon altKey="ecpds.user.event.object" titleKey="ecpds.user.event.object" key="icon.small.arrow.right" writeFullTag="true"/></a>
                    </c:if>
                </c:if>
                <c:if test="${event.type == 'lost'}">
                    <content:icon altKey="ecpds.user.event.noObject" titleKey="ecpds.user.event.noObject" key="icon.small.square" writeFullTag="true"/>
                </c:if>
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
    var table = $('#userEventsTable').DataTable({
        paging:    true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('userEventsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching: true,
        ordering:  true,
        info:      true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('userEventsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_savedPageLen).draw(false);
    $('#userEventsPageLen').val(_savedPageLen);
    $('#userEventsPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('userEventsPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#userEventsSearch').on('keyup', function() { table.search(this.value).draw(); });

    /* ---- Cols:Auto ---- */
    var _ueaColKey       = 'ueaColMode';
    var _ueaCustomColKey = 'ueaCustomCols';
    var _ueaCompact      = [1, 3];
    var _ueaColMode = (function() { try { return localStorage.getItem(_ueaColKey) || 'auto'; } catch(e) { return 'auto'; } })();
    var _ueaCustomCols = (function() {
        try { var s = localStorage.getItem(_ueaCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
        return [0, 1, 2, 3, 4];
    })();
    function _ueaShowCols(hideCols) {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
        table.columns.adjust();
    }
    function _ueaApplyCustomCols() {
        var n = table.columns().count();
        for (var i = 0; i < n; i++) {
            table.column(i).visible((i === 0 || i === 2 || i === 4) ? true : _ueaCustomCols.indexOf(i) !== -1, false);
        }
        table.columns.adjust();
    }
    function _ueaSyncChkBoxes() {
        document.querySelectorAll('.uea-col-chk').forEach(function(chk) {
            chk.checked = _ueaCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }
    document.querySelectorAll('.uea-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _ueaCustomCols.indexOf(col);
            if (this.checked && idx === -1) _ueaCustomCols.push(col);
            else if (!this.checked && idx !== -1) _ueaCustomCols.splice(idx, 1);
            try { localStorage.setItem(_ueaCustomColKey, JSON.stringify(_ueaCustomCols)); } catch(e) {}
            if (_ueaColMode === 'custom') _ueaApplyCustomCols();
        });
    });
    function _ueaApplyResponsive() {
        if (_ueaColMode !== 'auto') return;
        _ueaShowCols(window.innerWidth < 768 ? [1, 3] : []);
    }
    function _ueaApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#ueaColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        $('#ueaColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
        $('#ueaColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('uea-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        document.getElementById('ueaCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        if (mode === 'auto') _ueaApplyResponsive();
        else if (mode === 'all') _ueaShowCols([]);
        else if (mode === 'compact') _ueaShowCols(_ueaCompact);
        else if (mode === 'custom') { _ueaSyncChkBoxes(); _ueaApplyCustomCols(); }
    }
    $(window).on('resize', _ueaApplyResponsive);
    _ueaApplyMode(_ueaColMode);
    $('#ueaColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
        e.preventDefault();
        var mode = $(this).data('uea-mode');
        if (!mode) return;
        _ueaColMode = mode;
        try { localStorage.setItem(_ueaColKey, mode); } catch(e) {}
        _ueaApplyMode(mode);
    });
});
</script>
</c:if>
