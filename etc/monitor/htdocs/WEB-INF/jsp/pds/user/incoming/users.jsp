<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people text-primary"></i>
    <span class="fw-semibold">Data Users</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <c:set var="destParam" value="destinationNameForSearch" scope="request"/>
        <tiles:insert name="destination.select" />
        <form method="GET" class="m-0">
            <input type="hidden" name="destinationNameForSearch" value="<c:out value="${destinationNameForSearch}"/>">
            <div class="input-group input-group-sm">
                <input type="text" class="form-control" name="search"
                       placeholder="Search login..." autocomplete="off"
                       title="Search is performed across the Name (case-insensitive)"
                       value="<c:out value="${param['search']}"/>">
                <button class="btn btn-outline-secondary btn-sm" type="submit" title="Search">
                    <i class="bi bi-search"></i>
                </button>
            </div>
        </form>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="incomingPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="-1">All</option>
            </select>
        </div>
<div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="incUsrColModeBtn"
                            data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                        <i class="bi bi-layout-three-columns me-1"></i>Auto
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="incUsrColModeBtn">
                        <li><a class="dropdown-item" href="#" data-incUsr-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                        <li><a class="dropdown-item" href="#" data-incUsr-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                        <li><a class="dropdown-item" href="#" data-incUsr-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Country, TOTP, Anonymous, Sessions</small></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" data-incUsr-mode="custom"><strong>Custom</strong><br><small class="text-muted">Choose individual columns</small></a></li>
                        <li id="incUsrCustomColChkPanel" style="display:none;">
                            <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="incUsrchk-0">Data Login <small>(required)</small></label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-1" data-col="1" checked><label class="form-check-label" for="incUsrchk-1">Comment</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-2" data-col="2" checked><label class="form-check-label" for="incUsrchk-2">Country</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-3" data-col="3" checked><label class="form-check-label" for="incUsrchk-3">Enabled</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-4" data-col="4" checked><label class="form-check-label" for="incUsrchk-4">TOTP</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-5" data-col="5" checked><label class="form-check-label" for="incUsrchk-5">Anonymous</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-6" data-col="6" checked><label class="form-check-label" for="incUsrchk-6">Sessions</label></div>
            <div class="form-check mb-0"><input class="form-check-input incUsr-col-chk" type="checkbox" id="incUsrchk-7" data-col="7" checked disabled><label class="form-check-label text-muted" for="incUsrchk-7">Actions <small>(required)</small></label></div>
                            </div>
                        </li>
                    </ul>
                </div>
        <a href="<bean:message key="incoming.basepath"/>/edit/insert_form"
           class="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</a>
    </div>
</div>
<div class="card-body p-0">
<c:if test="${empty users}">
    <div class="alert alert-info m-3 mb-2">No Data Users found based on these criteria.</div>
</c:if>
<c:if test="${not empty users}">
<div class="table-responsive">
<table id="usersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Data Login</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">TOTP</th>
            <th class="text-center">Anonymous</th>
            <th class="text-center">Sessions</th>
            <th class="text-center no-sort">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="user" items="${users}">
        <tr>
            <td><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></td>
            <td>${user.comment}</td>
            <td>
                <span class="d-inline-flex align-items-center gap-1">
                    <c:choose>
                        <c:when test="${user.country.iso == 'ex'}"><i class="bi bi-globe" title="${user.country.name}" style="font-size:1.1em;"></i></c:when>
                        <c:when test="${fn:length(user.country.iso) == 2}"><span class="fi fi-${fn:toLowerCase(user.country.iso)}"
                              title="${user.country.name}" style="font-size:1.1em;border-radius:2px;"></span></c:when>
                    </c:choose>
                    <span>${user.country.name}</span>
                </span>
            </td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:choose>
                    <c:when test="${user.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${user.isSynchronized ? 1 : 0}">
                <c:choose>
                    <c:when test="${user.isSynchronized}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                    <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                </c:choose>
            </td>
            <td class="text-center" data-order="${user.anonymous ? 1 : 0}">
                <c:if test="${user.anonymous}"><i class="bi bi-exclamation-circle-fill text-warning" title="Yes"></i></c:if>
                <c:if test="${!user.anonymous}"><i class="bi bi-dash text-muted" title="No"></i></c:if>
            </td>
            <td class="text-center">${fn:length(user.incomingConnections)}</td>
            <td class="text-center">
                <auth:link styleClass="menuitem" href="/do/user/incoming/edit/update_form/${user.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/incoming/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
</div>
</c:if>
</div>
</div>
<c:if test="${not empty users}">
<script>
$(document).ready(function() {
    var table = $('#usersTable').DataTable({
        paging:     true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,-1].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching:  false,
        ordering:   true,
        info:       true,
        columnDefs: [{ orderable: false, targets: 'no-sort' }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    $('#incomingPageLen').val((function() { try { var v = parseInt(localStorage.getItem('incomingPageLen'), 10); return [10,25,50,-1].indexOf(v) >= 0 ? String(v) : '25'; } catch(e) { return '25'; } })());
    $('#incomingPageLen').on('change', function() {
        var len = parseInt(this.value);
        try { localStorage.setItem('incomingPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });

        /* ---- Cols:Auto ---- */
        var _incUsrColKey        = 'incUsrColMode';
        var _incUsrCustomColKey  = 'incUsrCustomCols';
        var _incUsrCompact       = [1, 2, 4, 5, 6];
        var _incUsrColMode = (function() { try { return localStorage.getItem(_incUsrColKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _incUsrCustomCols = (function() {
            try { var s = localStorage.getItem(_incUsrCustomColKey); if (s) return JSON.parse(s); } catch(e) {}
            return [0, 1, 2, 3, 4, 5, 6, 7];
        })();
        function _incUsrShowCols(hideCols) {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) table.column(i).visible(hideCols.indexOf(i) === -1, false);
            table.columns.adjust();
        }
        function _incUsrApplyCustomCols() {
            var n = table.columns().count();
            for (var i = 0; i < n; i++) {
                table.column(i).visible((i === 0 || i === 7) ? true : _incUsrCustomCols.indexOf(i) !== -1, false);
            }
            table.columns.adjust();
        }
        function _incUsrSyncChkBoxes() {
            document.querySelectorAll('.incUsr-col-chk').forEach(function(chk) {
                chk.checked = _incUsrCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.incUsr-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _incUsrCustomCols.indexOf(col);
                if (this.checked && idx === -1) _incUsrCustomCols.push(col);
                else if (!this.checked && idx !== -1) _incUsrCustomCols.splice(idx, 1);
                try { localStorage.setItem(_incUsrCustomColKey, JSON.stringify(_incUsrCustomCols)); } catch(e) {}
                if (_incUsrColMode === 'custom') _incUsrApplyCustomCols();
            });
        });
        function _incUsrApplyResponsive() {
            if (_incUsrColMode !== 'auto') return;
            _incUsrShowCols(window.innerWidth < 992 ? [1, 2, 4, 5, 6] : []);
        }
        function _incUsrApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#incUsrColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#incUsrColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#incUsrColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('incUsr-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('incUsrCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _incUsrApplyResponsive();
            else if (mode === 'all') _incUsrShowCols([]);
            else if (mode === 'compact') _incUsrShowCols(_incUsrCompact);
            else if (mode === 'custom') { _incUsrSyncChkBoxes(); _incUsrApplyCustomCols(); }
        }
        $(window).on('resize', _incUsrApplyResponsive);
        _incUsrApplyMode(_incUsrColMode);
        $('#incUsrColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('incUsr-mode');
            if (!mode) return;
            _incUsrColMode = mode;
            try { localStorage.setItem(_incUsrColKey, mode); } catch(e) {}
            _incUsrApplyMode(mode);
        });
});
</script>
</c:if>
