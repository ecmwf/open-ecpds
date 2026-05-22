<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<style>
.disk-bar-track { background:var(--bs-secondary-bg); border-radius:3px; height:8px; width:100px; flex-shrink:0; }
</style>


<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-server text-primary"></i>
    <span class="fw-semibold">Data Movers</span>
    <span id="tsDiskAge" class="small ms-2"></span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="tsColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="tsColModeBtn">
                <li><a class="dropdown-item" href="#" data-tscol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-tscol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-tscol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Host/Port, Replicate, Check, Last Update</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-tscol-mode="custom">
                  <strong>Custom</strong><br><small class="text-muted">Choose individual columns</small>
                </a></li>
                <li id="tsCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="tschk-col-0">Name <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-1" data-col="1" checked><label class="form-check-label" for="tschk-col-1">Host / Port</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-2" data-col="2" checked><label class="form-check-label" for="tschk-col-2">Group</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-3" data-col="3" checked><label class="form-check-label" for="tschk-col-3">Enabled</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-4" data-col="4" checked><label class="form-check-label" for="tschk-col-4">Replicate</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-5" data-col="5" checked><label class="form-check-label" for="tschk-col-5">Check</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-6" data-col="6" checked><label class="form-check-label" for="tschk-col-6">Last Update</label></div>
                    <div class="form-check mb-0"><input class="form-check-input ts-custom-col-chk" type="checkbox" id="tschk-col-7" data-col="7" checked><label class="form-check-label" for="tschk-col-7">Disk Usage</label></div>
                  </div>
                </li>
            </ul>
        </div>
        <auth:link basePathKey="transferserver.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</div>
<div class="card-body p-0">
<c:if test="${empty transferservers}">
    <div class="alert alert-info m-3 mb-2">No Data Movers found.</div>
</c:if>

<c:if test="${not empty transferservers}">
<div class="table-responsive">
    <table id="transferserversTable" class="table table-sm table-hover table-striped align-middle mb-0" style="width:100%">
        <thead class="table-light">
            <tr>
                <th>Name</th>
                <th>Host / Port</th>
                <th>Group</th>
                <th class="text-center">Enabled</th>
                <th class="text-center">Replicate</th>
                <th class="text-center">Check</th>
                <th title="Last Update (UTC)">Last Update</th>
                <th class="text-center">Disk Usage</th>
                <th class="text-end">Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="server" items="${transferservers}">
                <tr>
                    <td class="fw-semibold">
                        <a href="/do/datafile/transferserver/${server.name}" class="text-decoration-none dest-list-link">${server.name}</a>
                    </td>
                    <td class="small text-muted" style="white-space:nowrap">
                        ${server.host}<c:if test="${server.port gt 0}">:<strong class="text-body">${server.port}</strong></c:if>
                    </td>
                    <td class="small">
                        <c:if test="${not empty server.transferGroupName}">
                            <a href="/do/datafile/transfergroup/${server.transferGroupName}"
                               class="badge bg-secondary-subtle text-secondary-emphasis border text-decoration-none">${server.transferGroupName}</a>
                        </c:if>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.replicate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.check}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="small text-muted" style="white-space:nowrap">
                        <c:if test="${not empty server.lastUpdateDate}">
                            <span title="${server.lastUpdateDate}">${server.lastUpdateDuration}</span>
                        </c:if>
                    </td>
                    <td class="text-center" style="min-width:110px">
                        <div class="ts-disk-usage-cell" data-mover="${server.name}" style="font-size:0.7rem;color:#6c757d;">-</div>
                    </td>
                    <td class="text-end" style="white-space:nowrap">
                        <auth:link href="/do/datafile/transferserver/edit/update_form/${server.id}"
                                   imageKey="icon.small.update" styleClass="menuitem"/>
                        <auth:link href="/do/datafile/transferserver/edit/delete_form/${server.id}"
                                   imageKey="icon.small.delete" styleClass="menuitem"/>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
</c:if>
</div>
</div>

<c:if test="${not empty transferservers}">
    <script>
    var _tsTable;
    $(function() {
        _tsTable = $('#transferserversTable').DataTable({
            paging:    false,
            searching: false,
            order:     [[0, 'asc']],
            language: { info: 'Showing _START_-_END_ of _TOTAL_' },
            columnDefs: [{ orderable: false, targets: -1 }]
        });

        /* Column mode — 0:Name 1:Host/Port 2:Group 3:Enabled 4:Replicate 5:Check 6:LastUpdate 7:DiskUsage 8:Actions */
        var _TS_COL_KEY        = 'tsColMode';
        var _TS_CUSTOM_COL_KEY = 'tsCustomCols';
        var _TS_COMPACT        = [1, 4, 5, 6];
        var _tsColMode         = (function() { try { return localStorage.getItem(_TS_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
        var _tsCustomCols      = (function() {
            try { var s = localStorage.getItem(_TS_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,2,3,4,5,6,7];
        })();

        function _tsShowCols(hideCols) {
            var n = _tsTable.columns().count();
            for (var i = 0; i < n; i++) _tsTable.column(i).visible(hideCols.indexOf(i) === -1, false);
            _tsTable.columns.adjust();
        }
        function _tsApplyCustomCols() {
            var n = _tsTable.columns().count();
            for (var i = 0; i < n; i++) {
                var visible = (i === 0 || i === 8) ? true : _tsCustomCols.indexOf(i) !== -1;
                _tsTable.column(i).visible(visible, false);
            }
            _tsTable.columns.adjust();
        }
        function _tsSyncCustomChkBoxes() {
            document.querySelectorAll('.ts-custom-col-chk').forEach(function(chk) {
                chk.checked = _tsCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.ts-custom-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _tsCustomCols.indexOf(col);
                if (this.checked && idx === -1) _tsCustomCols.push(col);
                else if (!this.checked && idx !== -1) _tsCustomCols.splice(idx, 1);
                try { localStorage.setItem(_TS_CUSTOM_COL_KEY, JSON.stringify(_tsCustomCols)); } catch(e) {}
                if (_tsColMode === 'custom') _tsApplyCustomCols();
            });
        });
        function _tsApplyResponsive() {
            if (_tsColMode !== 'auto') return;
            _tsShowCols(window.innerWidth < 992 ? _TS_COMPACT : []);
        }
        function _tsApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#tsColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#tsColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#tsColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('tscol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('tsCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _tsApplyResponsive();
            else if (mode === 'all') _tsShowCols([]);
            else if (mode === 'compact') _tsShowCols(_TS_COMPACT);
            else if (mode === 'custom') { _tsSyncCustomChkBoxes(); _tsApplyCustomCols(); }
        }
        $(window).on('resize', _tsApplyResponsive);
        _tsApplyMode(_tsColMode);
        $('#tsColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('tscol-mode');
            if (!mode) return;
            _tsColMode = mode;
            try { localStorage.setItem(_TS_COL_KEY, mode); } catch(e) {}
            _tsApplyMode(mode);
        });
    });

    // Disk usage mini-bars with min/max highlighting
    (function() {
        function fmtBytes(b) {
            var units = ['B','KB','MB','GB','TB','PB'];
            var i = 0;
            while (b >= 1024 && i < units.length - 1) { b /= 1024; i++; }
            return b.toFixed(i > 0 ? 1 : 0) + ' ' + units[i];
        }
        function pctColor(pct) {
            if (pct >= 90) return '#dc3545';
            if (pct >= 75) return '#fd7e14';
            if (pct >= 50) return '#ffc107';
            return '#198754';
        }

        var _tsDiskFails = 0, _tsDiskIv;
        function refreshDiskUsage() {
            $.getJSON('/do/datafile/moverdiskusage').done(function(data) {
                _tsDiskFails = 0;
                var items = [];
                $('.ts-disk-usage-cell').each(function() {
                    var cell = $(this);
                    var mName = cell.data('mover');
                    var vols = data.movers && data.movers[mName];
                    if (!vols || vols.length === 0) {
                        items.push({ cell: cell, pct: null });
                        return;
                    }
                    var totalUsed = 0, totalCap = 0;
                    vols.forEach(function(v) { totalUsed += v.used; totalCap += v.total; });
                    var pct = totalCap > 0 ? Math.round(100 * totalUsed / totalCap) : 0;
                    var maxVolPct = 0;
                    vols.forEach(function(v) { if (v.pct > maxVolPct) maxVolPct = v.pct; });
                    items.push({ cell: cell, pct: pct, totalUsed: totalUsed, totalCap: totalCap, maxVolPct: maxVolPct, volCount: vols.length });
                });

                var withData = items.filter(function(x) { return x.pct !== null; });
                var pcts     = withData.map(function(x) { return x.pct; });
                var gMin     = pcts.length ? Math.min.apply(null, pcts) : null;
                var gMax     = pcts.length ? Math.max.apply(null, pcts) : null;
                var showMM   = withData.length > 1 && gMin !== gMax;

                items.forEach(function(item) {
                    if (item.pct === null) {
                        item.cell.closest('td').attr('data-order', -1);
                        item.cell.html('<span class="text-muted">no data</span>'); return;
                    }
                    item.cell.closest('td').attr('data-order', item.pct);
                    var color  = pctColor(item.pct);
                    var barPct = Math.max(1, item.pct);
                    var badge  = '';
                    if (showMM) {
                        if (item.pct === gMax) badge = '<span class="badge rounded-pill bg-danger-subtle text-danger-emphasis border ms-1" style="font-size:0.6rem;vertical-align:middle" title="Highest usage">&#x25B2;</span>';
                        else if (item.pct === gMin) badge = '<span class="badge rounded-pill bg-success-subtle text-success-emphasis border ms-1" style="font-size:0.6rem;vertical-align:middle" title="Lowest usage">&#x25BC;</span>';
                    }
                    item.cell.html(
                        '<div class="d-flex align-items-center gap-1" title="' + fmtBytes(item.totalUsed) + ' used of ' + fmtBytes(item.totalCap) +
                        ' (' + item.volCount + ' vols, max vol: ' + item.maxVolPct + '%)" style="cursor:default">' +
                        '<div class="disk-bar-track">' +
                        '<div style="background:' + color + ';border-radius:3px;height:8px;width:' + barPct + 'px"></div></div>' +
                        '<span style="color:' + color + ';font-weight:600;white-space:nowrap">' + item.pct + '%</span>' + badge + '</div>'
                    );
                });
                if (_tsTable) _tsTable.rows().invalidate('dom');
            }).fail(function() {
                if (++_tsDiskFails >= 3) {
                    clearInterval(_tsDiskIv);
                    var el = document.getElementById('tsDiskAge');
                    if (el) el.innerHTML = '<i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>'
                        + '<span class="text-warning-emphasis">Session expired</span>'
                        + ' <a href="#" onclick="location.reload();return false;" class="small">reload</a>';
                }
            });
        }

        refreshDiskUsage();
        _tsDiskIv = setInterval(refreshDiskUsage, 5000);
    })();
    </script>
</c:if>
