<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-people-fill text-info"></i>
    <span class="fw-semibold">Portal Subscribers &mdash; <code>${incoming.id}</code></span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#psbInfo"
        aria-expanded="false" title="About this page">
        <i class="bi bi-info-circle"></i>
    </button>
    <button id="btnPsbFilter" type="button" class="btn btn-sm btn-outline-primary position-relative"
            onclick="psbToggleFilter('psbFilterPanel','btnPsbFilter')" title="Filter subscribers">
        <i class="bi bi-sliders2"></i> Filter
        <span id="btnPsbFilter-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;font-size:0.65rem"></span>
    </button>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="psbSearch" class="form-control" placeholder="Search email, name..."
                   autocomplete="off" style="min-width:140px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="psbPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="250">250</option>
            </select>
        </div>
        <div class="dropdown">
            <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="psbColModeBtn"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="psbColModeBtn">
                <li><a class="dropdown-item" href="#" data-psb-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-psb-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-psb-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Country, Registered</small></a></li>
            </ul>
        </div>
        <a href="/do/user/incoming/${incoming.id}" class="btn btn-sm btn-outline-secondary" title="Back to Data User">
            <i class="bi bi-arrow-left"></i> Back
        </a>
    </div>
</div>

<%-- Filter panel --%>
<div id="psbFilterPanel" class="border rounded p-2"
     style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem; width:280px">
    <div class="row g-1 mb-1">
        <div class="col-12">
            <label class="form-label mb-0 fw-semibold"><i class="bi bi-toggle-on me-1 text-muted"></i>Status</label>
            <select class="form-select form-select-sm" id="psb_status">
                <option value="">Any</option>
                <option value="active">Active</option>
                <option value="verified">Awaiting Approval</option>
                <option value="pending">Pending Email</option>
                <option value="inactive">Deactivated</option>
            </select>
        </div>
    </div>
    <div class="d-flex gap-1 pt-1 border-top mt-1 justify-content-end">
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="psbFilterClear()">
            <i class="bi bi-x-circle me-1"></i>Clear
        </button>
        <button type="button" class="btn btn-sm btn-primary" onclick="psbFilterApply()">
            <i class="bi bi-check-lg me-1"></i>Apply
        </button>
    </div>
</div>

<div class="collapse" id="psbInfo">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">Portal Subscribers</strong>
        <p class="mb-1">This page lists all visitors who have self-registered under data user <strong>${incoming.id}</strong>. Each subscriber authenticates on the data portal using the data user login name with their own personal password.</p>
        <strong class="d-block mt-2 mb-1">Subscriber status</strong>
        <ul class="mb-0 ps-3">
            <li><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-normal"><i class="bi bi-envelope me-1"></i>Pending Email</span> &mdash; registered but the verification email has not yet been clicked.</li>
            <li><span class="badge rounded-pill bg-warning-subtle text-warning-emphasis border border-warning-subtle fw-normal"><i class="bi bi-hourglass-split me-1"></i>Awaiting Approval</span> &mdash; email verified; waiting for admin activation.</li>
            <li><span class="badge rounded-pill bg-success-subtle text-success-emphasis border border-success-subtle fw-normal"><i class="bi bi-check-circle-fill me-1"></i>Active</span> &mdash; fully activated; subscriber can log in.</li>
            <li><span class="badge rounded-pill bg-danger-subtle text-danger-emphasis border border-danger-subtle fw-normal"><i class="bi bi-x-circle-fill me-1"></i>Deactivated</span> &mdash; previously active but manually deactivated.</li>
        </ul>
        <p class="mb-0 mt-2 text-muted">Use the <i class="bi bi-check-circle"></i> / <i class="bi bi-pause-circle"></i> icons in the Actions column to activate or deactivate a subscriber. Use <i class="bi bi-trash"></i> to permanently remove a subscriber.</p>
    </div>
</div>

<div class="card-body p-0">
<div id="psbBulkMsg" style="display:none" class="mx-2 mt-2"></div>
<div class="table-responsive">
<table id="psbTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-info">
        <tr>
            <th>Email</th>
            <th>Name</th>
            <th class="text-center">Country</th>
            <th class="text-center">Status</th>
            <th>Registered</th>
            <th class="text-center no-sort">Actions</th>
            <th class="d-none">StatusSort</th>
            <th class="d-none">TimeSort</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
</div>
</div>
</div>

<div class="mt-3">
    <a href="/do/user/incoming/${incoming.id}" class="btn btn-outline-primary">
        <i class="bi bi-arrow-left"></i> Back to Data User
    </a>
</div>

<style>
.btn-xs { padding: 0.15rem 0.4rem; font-size: 0.75rem; }
</style>
<script>
(function() {
    var _inuId  = '${incoming.id}';
    var _status = '';
    var _table  = null;

    function _buildUrl() {
        var url = '/do/user/incoming/subscribers/' + encodeURIComponent(_inuId) + '/list';
        if (_status) url += '?status=' + encodeURIComponent(_status);
        return url;
    }

    function _updateBadge() {
        var n = _status ? 1 : 0;
        var b = document.getElementById('btnPsbFilter-badge');
        if (b) { b.textContent = n; b.style.display = n > 0 ? '' : 'none'; }
        var btn = document.getElementById('btnPsbFilter');
        if (btn) {
            btn.classList.toggle('btn-outline-primary', n === 0);
            btn.classList.toggle('btn-warning', n > 0);
        }
    }

    window.psbToggleFilter = function(panelId, btnId) {
        var panel = document.getElementById(panelId);
        var btn   = document.getElementById(btnId);
        if (!panel || !btn) return;
        if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
        if (panel.parentElement !== document.body) document.body.appendChild(panel);
        var vw = window.innerWidth || document.documentElement.clientWidth;
        var pw = Math.min(300, vw - 16);
        panel.style.width = pw + 'px';
        var r = btn.getBoundingClientRect();
        var sy = window.pageYOffset || document.documentElement.scrollTop;
        panel.style.top  = (r.bottom + sy + 4) + 'px';
        panel.style.left = Math.max(8, r.right - pw + window.pageXOffset) + 'px';
        panel.style.display = 'block';
    };

    document.addEventListener('click', function(e) {
        var panel = document.getElementById('psbFilterPanel');
        var btn   = document.getElementById('btnPsbFilter');
        if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
            panel.style.display = 'none';
        }
    });

    window.psbFilterApply = function() {
        var sel = document.getElementById('psb_status');
        _status = sel ? sel.value : '';
        _updateBadge();
        var p = document.getElementById('psbFilterPanel');
        if (p) p.style.display = 'none';
        if (_table) _table.ajax.url(_buildUrl()).load();
    };

    window.psbFilterClear = function() {
        _status = '';
        var sel = document.getElementById('psb_status');
        if (sel) sel.value = '';
        _updateBadge();
        var p = document.getElementById('psbFilterPanel');
        if (p) p.style.display = 'none';
        if (_table) _table.ajax.url(_buildUrl()).load();
    };

    function _showMsg(type, html) {
        var $m = $('#psbBulkMsg');
        $m.attr('class', 'mx-2 mt-2 alert alert-' + type + ' alert-dismissible d-flex align-items-center gap-2 p-2 mb-0');
        var icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';
        $m.html('<i class="bi ' + icon + ' flex-shrink-0"></i><div class="flex-grow-1">' + html + '</div>'
              + '<button type="button" class="btn-close p-2" data-bs-dismiss="alert" aria-label="Close"></button>');
        $m.show();
        setTimeout(function() { $m.fadeOut(); }, 5000);
    }

    window.psbToggle = function(inuId, psbId, activate) {
        var msg = activate ? 'Activate this subscriber?' : 'Deactivate this subscriber?';
        confirmationDialog({
            title: activate ? 'Activate Subscriber' : 'Deactivate Subscriber',
            message: msg,
            confirmText: activate ? 'Activate' : 'Deactivate',
            onConfirm: function() {
                $.ajax({
                    url: '/do/user/incoming/subscribers/' + encodeURIComponent(inuId) + '/edit/activate/' + psbId + '?active=' + activate,
                    method: 'POST',
                    success: function(data) {
                        if (data && data.ok) {
                            _showMsg('success', data.message || 'Updated.');
                            if (_table) _table.ajax.reload(null, false);
                        } else {
                            _showMsg('danger', (data && data.message) ? data.message : 'Error updating subscriber.');
                        }
                    },
                    error: function() { _showMsg('danger', 'Error updating subscriber.'); }
                });
            }
        });
    };

    window.psbDelete = function(inuId, psbId) {
        confirmationDialog({
            title: 'Delete Subscriber',
            message: 'Permanently delete this subscriber? This action cannot be undone.',
            confirmText: 'Delete',
            onConfirm: function() {
                $.ajax({
                    url: '/do/user/incoming/subscribers/' + encodeURIComponent(inuId) + '/edit/delete/' + psbId,
                    method: 'POST',
                    success: function(data) {
                        if (data && data.ok) {
                            _showMsg('success', data.message || 'Deleted.');
                            if (_table) _table.ajax.reload(null, false);
                        } else {
                            _showMsg('danger', (data && data.message) ? data.message : 'Error deleting subscriber.');
                        }
                    },
                    error: function() { _showMsg('danger', 'Error deleting subscriber.'); }
                });
            }
        });
    };

    $(document).ready(function() {
        var savedLen = (function() {
            try { var v = parseInt(localStorage.getItem('psbPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; }
            catch(e) { return 25; }
        })();

        _table = $('#psbTable').DataTable({
            ajax: { url: _buildUrl(), dataSrc: 'data' },
            paging:    true,
            pageLength: savedLen,
            searching: true,
            ordering:  true,
            info:      true,
            language:  { emptyTable: 'No subscribers found.' },
            columnDefs: [
                { orderable: false, targets: 'no-sort' },
                { orderData: [6], targets: [3] },
                { orderData: [7], targets: [4] },
                { visible: false, targets: [6, 7] }
            ],
            dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
        });

        $('#psbPageLen').val(String(savedLen));
        $('#psbPageLen').on('change', function() {
            var len = parseInt(this.value);
            try { localStorage.setItem('psbPageLen', len); } catch(e) {}
            _table.page.len(len).draw();
        });

        $('#psbSearch').on('input', function() {
            _table.search(this.value).draw();
        });

        /* ---- Column mode (Auto/All/Compact) ---- */
        var _colModeKey  = 'psbColMode';
        var _colMode = (function() { try { return localStorage.getItem(_colModeKey) || 'auto'; } catch(e) { return 'auto'; } })();
        var _compactCols = [2, 4]; // Country, Registered

        function _showCols(hideCols) {
            var n = _table.columns().count();
            for (var i = 0; i < n; i++) {
                if (i >= 6) continue;
                _table.column(i).visible(hideCols.indexOf(i) === -1, false);
            }
            _table.columns.adjust();
        }

        function _applyResponsive() {
            if (_colMode !== 'auto') return;
            _showCols(window.innerWidth < 768 ? _compactCols : []);
        }

        function _applyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#psbColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            $('#psbColModeBtn').toggleClass('btn-outline-secondary', mode === 'auto').toggleClass('btn-primary', mode !== 'auto');
            $('#psbColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
                $(this).find('i.bi-check').remove();
                if ($(this).data('psb-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            if (mode === 'auto') _applyResponsive();
            else if (mode === 'all') _showCols([]);
            else if (mode === 'compact') _showCols(_compactCols);
        }

        $(window).on('resize', _applyResponsive);
        _applyMode(_colMode);

        $('#psbColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e) {
            e.preventDefault();
            var mode = $(this).data('psb-mode');
            if (!mode) return;
            _colMode = mode;
            try { localStorage.setItem(_colModeKey, mode); } catch(e) {}
            _applyMode(mode);
        });
    });
})();
</script>
