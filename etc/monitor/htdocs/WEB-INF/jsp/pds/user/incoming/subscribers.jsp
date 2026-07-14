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
        <auth:if basePathKey="incoming.basepath" paths="/subscribers/${incoming.id}/edit/insert">
        <auth:then>
        <button type="button" class="btn btn-sm btn-outline-success" onclick="psbCreate()"
                title="Create a new subscriber directly">
            <i class="bi bi-plus-circle"></i> Create
        </button>
        </auth:then>
        </auth:if>
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
            <th>Registered (UTC)</th>
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
                        $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                        if (data && data.ok) {
                            _showMsg('success', data.message || 'Updated.');
                            if (_table) _table.ajax.reload(null, false);
                        } else {
                            _showMsg('danger', (data && data.message) ? data.message : 'Error updating subscriber.');
                        }
                    },
                    error: function() {
                        $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                        _showMsg('danger', 'Error updating subscriber.');
                    }
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
                        $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                        if (data && data.ok) {
                            _showMsg('success', data.message || 'Deleted.');
                            if (_table) _table.ajax.reload(null, false);
                        } else {
                            _showMsg('danger', (data && data.message) ? data.message : 'Error deleting subscriber.');
                        }
                    },
                    error: function() {
                        $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                        _showMsg('danger', 'Error deleting subscriber.');
                    }
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
        window._psbTable = _table;

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

    window.psbCreate = function() {
        $('#psbCreateEmail').val('');
        $('#psbCreateName').val('');
        $('#psbCreateIso').val('');
        $('#psbCreateError').hide().text('');
        $('#psbCreateResult').hide();
        $('#psbCreateForm').show();
        $('#psbCreateModalSubmit').prop('disabled', false);
        var modal = new bootstrap.Modal(document.getElementById('psbCreateModal'));
        modal.show();
    };
})();
</script>

<%-- Create Subscriber Modal --%>
<div class="modal fade" id="psbCreateModal" tabindex="-1" aria-labelledby="psbCreateModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="psbCreateModalLabel">
                    <i class="bi bi-person-plus-fill me-2 text-success"></i>Create Subscriber
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div id="psbCreateError" class="alert alert-danger py-2 small" style="display:none"></div>
                <div id="psbCreateResult" class="alert alert-success py-2 small" style="display:none"></div>
                <div id="psbCreateForm">
                    <div class="mb-3">
                        <label for="psbCreateEmail" class="form-label fw-semibold">Email address <span class="text-danger">*</span></label>
                        <input type="email" class="form-control" id="psbCreateEmail" placeholder="user@example.com" autocomplete="off">
                    </div>
                    <div class="mb-3">
                        <label for="psbCreateName" class="form-label fw-semibold">Full name <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="psbCreateName" placeholder="First Last" autocomplete="off">
                    </div>
                    <div class="mb-1">
                        <label for="psbCreateIso" class="form-label fw-semibold">Country <small class="text-muted fw-normal">(optional)</small></label>
                        <select class="form-select" id="psbCreateIso">
                            <option value="">Select a country…</option>
                            <option value="AF">Afghanistan</option>
                            <option value="AL">Albania</option>
                            <option value="DZ">Algeria</option>
                            <option value="AD">Andorra</option>
                            <option value="AO">Angola</option>
                            <option value="AG">Antigua and Barbuda</option>
                            <option value="AR">Argentina</option>
                            <option value="AM">Armenia</option>
                            <option value="AU">Australia</option>
                            <option value="AT">Austria</option>
                            <option value="AZ">Azerbaijan</option>
                            <option value="BS">Bahamas</option>
                            <option value="BH">Bahrain</option>
                            <option value="BD">Bangladesh</option>
                            <option value="BB">Barbados</option>
                            <option value="BY">Belarus</option>
                            <option value="BE">Belgium</option>
                            <option value="BZ">Belize</option>
                            <option value="BJ">Benin</option>
                            <option value="BT">Bhutan</option>
                            <option value="BO">Bolivia</option>
                            <option value="BA">Bosnia and Herzegovina</option>
                            <option value="BW">Botswana</option>
                            <option value="BR">Brazil</option>
                            <option value="BN">Brunei</option>
                            <option value="BG">Bulgaria</option>
                            <option value="BF">Burkina Faso</option>
                            <option value="BI">Burundi</option>
                            <option value="CV">Cabo Verde</option>
                            <option value="KH">Cambodia</option>
                            <option value="CM">Cameroon</option>
                            <option value="CA">Canada</option>
                            <option value="CF">Central African Republic</option>
                            <option value="TD">Chad</option>
                            <option value="CL">Chile</option>
                            <option value="CN">China</option>
                            <option value="CO">Colombia</option>
                            <option value="KM">Comoros</option>
                            <option value="CD">Congo (DR)</option>
                            <option value="CG">Congo (Republic)</option>
                            <option value="CR">Costa Rica</option>
                            <option value="HR">Croatia</option>
                            <option value="CU">Cuba</option>
                            <option value="CY">Cyprus</option>
                            <option value="CZ">Czech Republic</option>
                            <option value="DK">Denmark</option>
                            <option value="DJ">Djibouti</option>
                            <option value="DM">Dominica</option>
                            <option value="DO">Dominican Republic</option>
                            <option value="EC">Ecuador</option>
                            <option value="EG">Egypt</option>
                            <option value="SV">El Salvador</option>
                            <option value="GQ">Equatorial Guinea</option>
                            <option value="ER">Eritrea</option>
                            <option value="EE">Estonia</option>
                            <option value="SZ">Eswatini</option>
                            <option value="ET">Ethiopia</option>
                            <option value="FJ">Fiji</option>
                            <option value="FI">Finland</option>
                            <option value="FR">France</option>
                            <option value="GA">Gabon</option>
                            <option value="GM">Gambia</option>
                            <option value="GE">Georgia</option>
                            <option value="DE">Germany</option>
                            <option value="GH">Ghana</option>
                            <option value="GR">Greece</option>
                            <option value="GD">Grenada</option>
                            <option value="GT">Guatemala</option>
                            <option value="GN">Guinea</option>
                            <option value="GW">Guinea-Bissau</option>
                            <option value="GY">Guyana</option>
                            <option value="HT">Haiti</option>
                            <option value="HN">Honduras</option>
                            <option value="HU">Hungary</option>
                            <option value="IS">Iceland</option>
                            <option value="IN">India</option>
                            <option value="ID">Indonesia</option>
                            <option value="IR">Iran</option>
                            <option value="IQ">Iraq</option>
                            <option value="IE">Ireland</option>
                            <option value="IL">Israel</option>
                            <option value="IT">Italy</option>
                            <option value="JM">Jamaica</option>
                            <option value="JP">Japan</option>
                            <option value="JO">Jordan</option>
                            <option value="KZ">Kazakhstan</option>
                            <option value="KE">Kenya</option>
                            <option value="KI">Kiribati</option>
                            <option value="KP">Korea (North)</option>
                            <option value="KR">Korea (South)</option>
                            <option value="KW">Kuwait</option>
                            <option value="KG">Kyrgyzstan</option>
                            <option value="LA">Laos</option>
                            <option value="LV">Latvia</option>
                            <option value="LB">Lebanon</option>
                            <option value="LS">Lesotho</option>
                            <option value="LR">Liberia</option>
                            <option value="LY">Libya</option>
                            <option value="LI">Liechtenstein</option>
                            <option value="LT">Lithuania</option>
                            <option value="LU">Luxembourg</option>
                            <option value="MG">Madagascar</option>
                            <option value="MW">Malawi</option>
                            <option value="MY">Malaysia</option>
                            <option value="MV">Maldives</option>
                            <option value="ML">Mali</option>
                            <option value="MT">Malta</option>
                            <option value="MH">Marshall Islands</option>
                            <option value="MR">Mauritania</option>
                            <option value="MU">Mauritius</option>
                            <option value="MX">Mexico</option>
                            <option value="FM">Micronesia</option>
                            <option value="MD">Moldova</option>
                            <option value="MC">Monaco</option>
                            <option value="MN">Mongolia</option>
                            <option value="ME">Montenegro</option>
                            <option value="MA">Morocco</option>
                            <option value="MZ">Mozambique</option>
                            <option value="MM">Myanmar</option>
                            <option value="NA">Namibia</option>
                            <option value="NR">Nauru</option>
                            <option value="NP">Nepal</option>
                            <option value="NL">Netherlands</option>
                            <option value="NZ">New Zealand</option>
                            <option value="NI">Nicaragua</option>
                            <option value="NE">Niger</option>
                            <option value="NG">Nigeria</option>
                            <option value="MK">North Macedonia</option>
                            <option value="NO">Norway</option>
                            <option value="OM">Oman</option>
                            <option value="PK">Pakistan</option>
                            <option value="PW">Palau</option>
                            <option value="PA">Panama</option>
                            <option value="PG">Papua New Guinea</option>
                            <option value="PY">Paraguay</option>
                            <option value="PE">Peru</option>
                            <option value="PH">Philippines</option>
                            <option value="PL">Poland</option>
                            <option value="PT">Portugal</option>
                            <option value="QA">Qatar</option>
                            <option value="RO">Romania</option>
                            <option value="RU">Russia</option>
                            <option value="RW">Rwanda</option>
                            <option value="KN">Saint Kitts and Nevis</option>
                            <option value="LC">Saint Lucia</option>
                            <option value="VC">Saint Vincent and the Grenadines</option>
                            <option value="WS">Samoa</option>
                            <option value="SM">San Marino</option>
                            <option value="ST">Sao Tome and Principe</option>
                            <option value="SA">Saudi Arabia</option>
                            <option value="SN">Senegal</option>
                            <option value="RS">Serbia</option>
                            <option value="SC">Seychelles</option>
                            <option value="SL">Sierra Leone</option>
                            <option value="SG">Singapore</option>
                            <option value="SK">Slovakia</option>
                            <option value="SI">Slovenia</option>
                            <option value="SB">Solomon Islands</option>
                            <option value="SO">Somalia</option>
                            <option value="ZA">South Africa</option>
                            <option value="SS">South Sudan</option>
                            <option value="ES">Spain</option>
                            <option value="LK">Sri Lanka</option>
                            <option value="SD">Sudan</option>
                            <option value="SR">Suriname</option>
                            <option value="SE">Sweden</option>
                            <option value="CH">Switzerland</option>
                            <option value="SY">Syria</option>
                            <option value="TW">Taiwan</option>
                            <option value="TJ">Tajikistan</option>
                            <option value="TZ">Tanzania</option>
                            <option value="TH">Thailand</option>
                            <option value="TL">Timor-Leste</option>
                            <option value="TG">Togo</option>
                            <option value="TO">Tonga</option>
                            <option value="TT">Trinidad and Tobago</option>
                            <option value="TN">Tunisia</option>
                            <option value="TR">Turkey</option>
                            <option value="TM">Turkmenistan</option>
                            <option value="TV">Tuvalu</option>
                            <option value="UG">Uganda</option>
                            <option value="UA">Ukraine</option>
                            <option value="AE">United Arab Emirates</option>
                            <option value="GB">United Kingdom</option>
                            <option value="US">United States</option>
                            <option value="UY">Uruguay</option>
                            <option value="UZ">Uzbekistan</option>
                            <option value="VU">Vanuatu</option>
                            <option value="VE">Venezuela</option>
                            <option value="VN">Vietnam</option>
                            <option value="YE">Yemen</option>
                            <option value="ZM">Zambia</option>
                            <option value="ZW">Zimbabwe</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Close</button>
                <button type="button" id="psbCreateModalSubmit" class="btn btn-success"
                        onclick="psbCreateSubmit()">
                    <i class="bi bi-plus-circle me-1"></i>Create Subscriber
                </button>
            </div>
        </div>
    </div>
</div>
<script>
(function() {
    window.psbCreateSubmit = function() {
        var email = $('#psbCreateEmail').val().trim();
        var name  = $('#psbCreateName').val().trim();
        var iso   = $('#psbCreateIso').val();
        if (!email || !name) {
            $('#psbCreateError').text('Email and name are required.').show();
            return;
        }
        $('#psbCreateError').hide();
        $('#psbCreateModalSubmit').prop('disabled', true);
        $("#loadingBackdrop").show(); $("#loadingDiv").show();
        $.ajax({
            type: 'POST',
            url: '/do/user/incoming/subscribers/' + encodeURIComponent('${incoming.id}') + '/edit/insert',
            data: { email: email, name: name, iso: iso },
            dataType: 'json',
            success: function(r) {
                $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                if (r.ok) {
                    $('#psbCreateForm').hide();
                    $('#psbCreateModalSubmit').hide();
                    $('#psbCreateResult').html(
                        '<strong><i class="bi bi-check-circle-fill me-1 text-success"></i>Subscriber created.</strong><br>' +
                        'Email: <code>' + $('<span>').text(email).html() + '</code><br>' +
                        'Password: <code id="psbCreatedPwd">' + $('<span>').text(r.password).html() + '</code> ' +
                        '<button type="button" class="btn btn-xs btn-outline-secondary py-0 px-1" title="Copy password" onclick="navigator.clipboard&&navigator.clipboard.writeText($(\'#psbCreatedPwd\').text())">' +
                        '<i class="bi bi-clipboard"></i></button><br>' +
                        '<small class="text-muted">Please share these credentials with the subscriber. The password is shown only once.</small>'
                    ).show();
                    if (window._psbTable) window._psbTable.ajax.reload(null, false);
                } else {
                    $('#psbCreateError').text(r.message || 'Creation failed.').show();
                    $('#psbCreateModalSubmit').prop('disabled', false);
                }
            },
            error: function() {
                $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
                $('#psbCreateError').text('Request failed. Please try again.').show();
                $('#psbCreateModalSubmit').prop('disabled', false);
            }
        });
    };
})();
</script>
