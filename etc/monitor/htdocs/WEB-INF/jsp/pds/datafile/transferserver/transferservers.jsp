<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(transferservers)}</strong> data mover(s)</span>
    <auth:link basePathKey="transferserver.basepath" href="/edit/insert_form"
               styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
</div>

<c:if test="${empty transferservers}">
    <div class="alert">No Data Movers found.</div>
</c:if>

<c:if test="${not empty transferservers}">
    <table id="transferserversTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th>Name</th>
                <th>Host / Port</th>
                <th>Group</th>
                <th class="text-center">Enabled</th>
                <th class="text-center">Replicate</th>
                <th class="text-center">Check</th>
                <th>Max Transfers</th>
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
                               class="badge bg-light text-secondary border text-decoration-none">${server.transferGroupName}</a>
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
                    <td class="small text-center">${server.maxTransfers}</td>
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
    <script>
    $(function() {
        $('#transferserversTable').DataTable({
            paging:    false,
            searching: false,
            order:     [[0, 'asc']],
            language: { info: 'Showing _START_-_END_ of _TOTAL_' },
            columnDefs: [{ orderable: false, targets: -2 }]
        });
    });

    // Disk usage mini-bars
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

        function refreshDiskUsage() {
            $.getJSON('/do/datafile/moverdiskusage').done(function(data) {
                $('.ts-disk-usage-cell').each(function() {
                    var cell = $(this);
                    var mName = cell.data('mover');
                    var vols = data.movers && data.movers[mName];
                    if (!vols || vols.length === 0) {
                        cell.html('<span class="text-muted">no data</span>');
                        return;
                    }
                    var totalUsed = 0, totalCap = 0;
                    vols.forEach(function(v) { totalUsed += v.used; totalCap += v.total; });
                    var pct = totalCap > 0 ? Math.round(100 * totalUsed / totalCap) : 0;
                    var color = pctColor(pct);
                    var maxPct = 0;
                    vols.forEach(function(v) { if (v.pct > maxPct) maxPct = v.pct; });
                    var barPct = Math.max(1, pct);
                    cell.html(
                        '<div class="d-flex align-items-center gap-1" title="' + fmtBytes(totalUsed) + ' used of ' + fmtBytes(totalCap) +
                        ' (' + vols.length + ' vols, max vol: ' + maxPct + '%)" style="cursor:default">' +
                        '<div style="background:#e9ecef;border-radius:3px;height:8px;width:100px;flex-shrink:0">' +
                        '<div style="background:' + color + ';border-radius:3px;height:8px;width:' + barPct + 'px"></div></div>' +
                        '<span style="color:' + color + ';font-weight:600;white-space:nowrap">' + pct + '%</span></div>'
                    );
                });
            });
        }

        refreshDiskUsage();
        setInterval(refreshDiskUsage, 5000);
    })();
    </script>
</c:if>
