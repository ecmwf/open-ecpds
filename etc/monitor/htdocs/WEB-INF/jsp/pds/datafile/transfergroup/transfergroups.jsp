<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<style>
.disk-bar-track { background:var(--bs-secondary-bg); border-radius:3px; height:8px; width:100px; flex-shrink:0; }
</style>

<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(transfergroups)}</strong> transfer group(s)</span>
    <span id="tgDiskAge" class="small"></span>
    <div class="ms-auto d-flex gap-2 align-items-center">
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="tgColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="tgColModeBtn">
                <li><a class="dropdown-item" href="#" data-tgcol-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-tgcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-tgcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Comment, Cluster, flags</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-tgcol-mode="custom">
                  <strong>Custom</strong><br><small class="text-muted">Choose individual columns</small>
                </a></li>
                <li id="tgCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-0" data-col="0" checked disabled><label class="form-check-label text-muted" for="tgchk-col-0">Name <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-1" data-col="1" checked><label class="form-check-label" for="tgchk-col-1">Comment</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-2" data-col="2" checked><label class="form-check-label" for="tgchk-col-2">Cluster</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-3" data-col="3" checked><label class="form-check-label" for="tgchk-col-3">Enabled</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-4" data-col="4" checked><label class="form-check-label" for="tgchk-col-4">Replicate</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-5" data-col="5" checked><label class="form-check-label" for="tgchk-col-5">Filter</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-6" data-col="6" checked><label class="form-check-label" for="tgchk-col-6">Backup</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-7" data-col="7" checked><label class="form-check-label" for="tgchk-col-7">Movers</label></div>
                    <div class="form-check mb-0"><input class="form-check-input tg-custom-col-chk" type="checkbox" id="tgchk-col-8" data-col="8" checked><label class="form-check-label" for="tgchk-col-8">Disk Usage</label></div>
                  </div>
                </li>
            </ul>
        </div>
        <auth:link basePathKey="transfergroup.basepath" href="/edit/insert_form"
                   styleClass="btn btn-sm btn-outline-success"><i class="bi bi-plus-circle"></i> Create</auth:link>
    </div>
</div>

<c:if test="${empty transfergroups}">
    <div class="alert">No Transfer Groups found.</div>
</c:if>

<c:if test="${not empty transfergroups}">
    <table id="transfergroupsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th>Name</th>
                <th>Comment</th>
                <th>Cluster</th>
                <th class="text-center">Enabled</th>
                <th class="text-center">Replicate</th>
                <th class="text-center">Filter</th>
                <th class="text-center">Backup</th>
                <th>Movers</th>
                <th class="text-center">Disk Usage</th>
                <th class="text-end">Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="row" items="${transfergroups}">
                <tr>
                    <td class="fw-semibold">
                        <a href="/do/datafile/transfergroup/${row.id}" class="text-decoration-none dest-list-link">${row.name}</a>
                    </td>
                    <td class="text-muted small">${row.comment}</td>
                    <td class="small">
                        <c:if test="${not empty row.clusterName}">
                            <span class="badge bg-secondary-subtle text-secondary-emphasis border me-1">${row.clusterName}</span>
                            <span class="text-muted" style="font-size:0.78rem;">w=${row.clusterWeight}</span>
                        </c:if>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.replicate}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.filter}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.backup}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
                            <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="small">
                        <c:set var="_servers" value="${row.transferServers}"/>
                        <c:choose>
                            <c:when test="${empty _servers}"><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:when>
                            <c:otherwise>
                                <c:forEach var="srv" items="${_servers}">
                                    <a href="/do/datafile/transferserver/${srv.name}"
                                       class="badge bg-secondary-subtle text-secondary-emphasis border text-decoration-none me-1">${srv.name}</a>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center" style="min-width:110px">
                        <div class="tg-disk-usage-cell" data-group="${row.name}" style="font-size:0.7rem;color:#6c757d;">-</div>
                    </td>
                    <td class="text-end" style="white-space:nowrap">
                        <auth:link basePathKey="transfergroup.basepath" href="/edit/update_form/${row.id}"
                                   imageKey="icon.small.update" styleClass="menuitem"/>
                        <auth:link basePathKey="transfergroup.basepath" href="/edit/delete_form/${row.id}"
                                   imageKey="icon.small.delete" styleClass="menuitem"/>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <script>
    var _tgTable;
    $(function() {
        _tgTable = $('#transfergroupsTable').DataTable({
            paging:    false,
            searching: false,
            order:     [[0, 'asc']],
            language: { info: 'Showing _START_-_END_ of _TOTAL_' },
            columnDefs: [{ orderable: false, targets: -1 }]
        });

        /* Column mode — 0:Name 1:Comment 2:Cluster 3:Enabled 4:Replicate 5:Filter 6:Backup 7:Movers 8:DiskUsage 9:Actions */
        var _TG_COL_KEY        = 'tgColMode';
        var _TG_CUSTOM_COL_KEY = 'tgCustomCols';
        var _TG_COMPACT        = [1, 2, 4, 5, 6];
        var _tgColMode         = (function() { try { return localStorage.getItem(_TG_COL_KEY) || 'auto'; } catch(e) { return 'auto'; } })();
        var _tgCustomCols      = (function() {
            try { var s = localStorage.getItem(_TG_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,2,3,4,5,6,7,8];
        })();

        function _tgShowCols(hideCols) {
            var n = _tgTable.columns().count();
            for (var i = 0; i < n; i++) _tgTable.column(i).visible(hideCols.indexOf(i) === -1, false);
            _tgTable.columns.adjust();
        }
        function _tgApplyCustomCols() {
            var n = _tgTable.columns().count();
            for (var i = 0; i < n; i++) {
                var visible = (i === 0 || i === 9) ? true : _tgCustomCols.indexOf(i) !== -1;
                _tgTable.column(i).visible(visible, false);
            }
            _tgTable.columns.adjust();
        }
        function _tgSyncCustomChkBoxes() {
            document.querySelectorAll('.tg-custom-col-chk').forEach(function(chk) {
                chk.checked = _tgCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }
        document.querySelectorAll('.tg-custom-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _tgCustomCols.indexOf(col);
                if (this.checked && idx === -1) _tgCustomCols.push(col);
                else if (!this.checked && idx !== -1) _tgCustomCols.splice(idx, 1);
                try { localStorage.setItem(_TG_CUSTOM_COL_KEY, JSON.stringify(_tgCustomCols)); } catch(e) {}
                if (_tgColMode === 'custom') _tgApplyCustomCols();
            });
        });
        function _tgApplyResponsive() {
            if (_tgColMode !== 'auto') return;
            _tgShowCols(window.innerWidth < 992 ? _TG_COMPACT : []);
        }
        function _tgApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#tgColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#tgColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#tgColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('tgcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            document.getElementById('tgCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            if (mode === 'auto') _tgApplyResponsive();
            else if (mode === 'all') _tgShowCols([]);
            else if (mode === 'compact') _tgShowCols(_TG_COMPACT);
            else if (mode === 'custom') { _tgSyncCustomChkBoxes(); _tgApplyCustomCols(); }
        }
        $(window).on('resize', _tgApplyResponsive);
        _tgApplyMode(_tgColMode);
        $('#tgColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('tgcol-mode');
            if (!mode) return;
            _tgColMode = mode;
            try { localStorage.setItem(_TG_COL_KEY, mode); } catch(e) {}
            _tgApplyMode(mode);
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

        var _tgDiskFails = 0, _tgDiskIv;
        function refreshDiskUsage() {
            $.getJSON('/do/datafile/diskusage').done(function(data) {
                _tgDiskFails = 0;
                var items = [];
                $('.tg-disk-usage-cell').each(function() {
                    var cell = $(this);
                    var gName = cell.data('group');
                    var vols = data.groups && data.groups[gName];
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
                if (_tgTable) _tgTable.rows().invalidate('dom');
            }).fail(function() {
                if (++_tgDiskFails >= 3) {
                    clearInterval(_tgDiskIv);
                    var el = document.getElementById('tgDiskAge');
                    if (el) el.innerHTML = '<i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>'
                        + '<span class="text-warning-emphasis">Session expired</span>'
                        + ' <a href="#" onclick="location.reload();return false;" class="small">reload</a>';
                }
            });
        }

        refreshDiskUsage();
        _tgDiskIv = setInterval(refreshDiskUsage, 5000);
    })();
    </script>
</c:if>


