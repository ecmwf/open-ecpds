<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
    <i class="bi bi-arrow-left-right text-secondary"></i>
    <span>Current Proxy, Backup and Replication workers on the Master. This page is read-only.</span>
</div>
<input type="hidden" id="schedulerInitialType" value="<c:out value="${schedulerTrafficType}"/>">
<div id="schedulerTrafficError" class="alert alert-danger" role="alert" style="display:none"></div>
<div class="card border-0 shadow-sm">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-arrow-left-right text-primary"></i>
        <span class="fw-semibold">Scheduler Traffic</span>
        <button class="btn btn-link btn-sm text-muted p-0" type="button" data-bs-toggle="collapse"
                data-bs-target="#schedulerTrafficInfo" aria-expanded="false" title="What does this page show?">
            <i class="bi bi-info-circle"></i>
        </button>
        <div class="btn-group btn-group-sm" role="group" aria-label="Traffic type">
            <button type="button" class="btn btn-outline-primary scheduler-type" data-type="Proxy">Proxy</button>
            <button type="button" class="btn btn-outline-primary scheduler-type" data-type="Backup">Backup</button>
            <button type="button" class="btn btn-outline-primary scheduler-type" data-type="Replication">Replication</button>
        </div>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input id="schedulerSearch" type="text" class="form-control" placeholder="Filter..." aria-label="Filter current workers">
            </div>
            <select id="schedulerPageLength" class="form-select form-select-sm" style="width:auto" aria-label="Page size">
                <option value="10">10</option><option value="25" selected>25</option>
                <option value="50">50</option><option value="100">100</option><option value="250">250</option>
            </select>
            <select id="schedulerRefresh" class="form-select form-select-sm" style="width:auto" aria-label="Auto refresh">
                <option value="5000" selected>Refresh: 5s</option><option value="15000">Refresh: 15s</option>
                <option value="30000">Refresh: 30s</option><option value="0">Refresh: Off</option>
            </select>
            <button id="schedulerRefreshNow" class="btn btn-outline-secondary btn-sm" type="button" title="Refresh now">
                <i class="bi bi-arrow-clockwise"></i>
            </button>
        </div>
    </div>
    <div class="collapse" id="schedulerTrafficInfo">
        <div class="px-3 py-2 small border-bottom" style="background:var(--bs-tertiary-bg)">
            <p class="mb-1"><strong>Proxy</strong> copies files to Proxy Hosts for onward dissemination.
                <strong>Backup</strong> copies files to Backup Hosts.
                <strong>Replication</strong> creates copies within the Data Mover group.</p>
            <p class="mb-1">Each row is a current scheduler worker for one data file, represented by the transfer that started it.
                Aliases may share the same file. <strong>Worker</strong> shows Starting, Processing or Finalizing;
                <strong>Transfer status</strong> is the separate dissemination status and does not describe the copy operation.
                Completed workers disappear: this is not a queue or historical log. Open the transfer history for outcomes.</p>
            <p class="mb-0">Source is shown when known (Replication); Proxy and Backup choose their source internally.
                Proxy Host selection may change during retries. Replication can involve several group members.
                <strong>Size</strong> is the file size, not bytes moved. Reliable live percentage/rate is not exposed by these
                blocking operations. Elapsed time includes setup and finalization. Refresh reads in-memory worker snapshots,
                without database scans, Mover probes or changes to scheduling.</p>
        </div>
    </div>
    <div class="px-3 py-2 small border-bottom" id="schedulerTrafficSummary" aria-live="polite">Loading...</div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table id="schedulerTrafficTable" class="table table-sm table-hover table-striped align-middle w-100">
                <thead class="table-secondary"><tr>
                    <th>Destination</th><th>Transfer</th><th>Data File</th><th>Target</th>
                    <th>Worker</th><th>Transfer status</th><th>Source Mover</th><th>Target / Host</th>
                    <th>Size</th><th>Started (UTC)</th><th>Elapsed</th>
                </tr></thead><tbody></tbody>
            </table>
        </div>
    </div>
</div>
<script>
$(function() {
    var type = document.getElementById('schedulerInitialType').value;
    var timer = null, pending = null, generation = 0, stopped = false;
    var escape = $.fn.dataTable.render.text().display;
    function link(value, path, allowed) {
        var text = escape(String(value == null ? '' : value));
        return allowed ? '<a href="' + path + '">' + text + '</a>' : text;
    }
    function phase(value, renderType) {
        if (renderType !== 'display') return value || '';
        var cls = value === 'Processing' ? 'bg-primary' : 'bg-secondary';
        return '<span class="badge ' + cls + '">' + escape(value || '') + '</span>';
    }
    function text(value, renderType) { return renderType === 'display' ? escape(value || '') : value || ''; }
    function elapsed(value, renderType) {
        if (renderType !== 'display') return value;
        var seconds = Math.max(0, Math.floor(value / 1000));
        return Math.floor(seconds / 3600) + 'h ' + Math.floor(seconds % 3600 / 60) + 'm ' + seconds % 60 + 's';
    }
    var table = $('#schedulerTrafficTable').DataTable({
        data: [], pageLength: 25, autoWidth: false, order: [[9, 'asc']],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
        columns: [
            {data:'destination', render:function(v,t,r) { return t === 'display' ? link(v, '/do/transfer/destination/' + encodeURIComponent(v), r.canViewDestination) : v; }},
            {data:'transferId', render:function(v,t,r) { return t === 'display' ? link(v, '/do/transfer/data/' + v, r.canViewTransfer) : v; }},
            {data:'dataFileId', render:function(v,t,r) { return t === 'display' ? link(v, '/do/datafile/' + v, r.canViewDataFile) : v; }},
            {data:'target', render:text, className:'text-break'},
            {data:'phase', render:phase},
            {data:'transferStatus', render:text},
            {data:'sourceMover', render:text},
            {data:'targetHost', render:text},
            {data:'size', render:function(v,t) { return t === 'display' ? Number(v).toLocaleString() + ' B' : v; }, className:'text-nowrap'},
            {data:'startedAt', render:function(v,t) { return t === 'display' ? new Date(v).toISOString().replace('T',' ').slice(0,19) : v; }, className:'text-nowrap'},
            {data:'elapsed', render:elapsed, className:'text-nowrap'}
        ],
        language:{emptyTable:'No current workers for this scheduler.', zeroRecords:'No workers match this filter.'}
    });
    function selectType() {
        $('.scheduler-type').each(function() {
            var selected = this.dataset.type === type;
            $(this).toggleClass('btn-primary', selected).toggleClass('btn-outline-primary', !selected)
                .attr('aria-pressed', selected ? 'true' : 'false');
        });
    }
    function schedule() {
        clearTimeout(timer);
        var delay = +$('#schedulerRefresh').val();
        if (!stopped && delay > 0) timer = setTimeout(load, delay);
    }
    function load() {
        clearTimeout(timer);
        var token = ++generation;
        if (pending) pending.abort();
        $('#schedulerRefreshNow').prop('disabled', true);
        pending = $.ajax({
            url:'/do/admin/schedulertraffic', data:{json:'list',type:type}, dataType:'json', timeout:20000
        }).done(function(data) {
            if (token !== generation) return;
            if (data.error) { showError(data.error); return; }
            $('#schedulerTrafficError').hide().text('');
            var rows = data.transfers.map(function(row) {
                row.elapsed = Math.max(0, data.observedAt - row.startedAt);
                return row;
            });
            table.clear().rows.add(rows).draw(false);
            if (table.page() >= table.page.info().pages && table.page() > 0) table.page('first').draw(false);
            $('#schedulerTrafficSummary').text(type + ': ' + (data.enabled ? rows.length + ' current workers' : 'scheduler disabled')
                + ' | Updated ' + new Date(data.observedAt).toISOString().replace('T',' ').slice(0,19) + ' UTC'
                + ' | ' + data.activity);
        }).fail(function(xhr, status) {
            if (token !== generation || status === 'abort') return;
            showError(xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error
                : 'Unable to retrieve scheduler traffic. Refresh to retry.');
        }).always(function() {
            if (token !== generation) return;
            pending = null;
            $('#schedulerRefreshNow').prop('disabled', false);
            schedule();
        });
    }
    function showError(message) {
        table.clear().draw();
        $('#schedulerTrafficError').text(message).show();
        $('#schedulerTrafficSummary').text(type + ': monitoring unavailable (no current snapshot).');
    }
    $('.scheduler-type').on('click', function() {
        type = this.dataset.type;
        var url = new URL(window.location.href);
        url.searchParams.set('type', type);
        window.history.replaceState(null, '', url);
        selectType();
        table.clear().draw();
        $('#schedulerTrafficSummary').text('Loading ' + type + '...');
        load();
    });
    $('#schedulerSearch').on('input', function() { table.search(this.value).draw(); });
    $('#schedulerPageLength').on('change', function() { table.page.len(+this.value).draw(); });
    $('#schedulerRefresh').on('change', schedule);
    $('#schedulerRefreshNow').on('click', load);
    $(window).on('pagehide', function() {
        stopped = true;
        ++generation;
        clearTimeout(timer);
        if (pending) pending.abort();
    });
    $(window).on('pageshow', function(event) {
        if (event.originalEvent.persisted) {
            stopped = false;
            load();
        }
    });
    selectType();
    load();
});
</script>
