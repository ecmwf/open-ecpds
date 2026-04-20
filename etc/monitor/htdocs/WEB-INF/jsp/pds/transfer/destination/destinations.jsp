<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<%-- Filter form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="destinationSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <%-- Row 1: main search + type + button --%>
                    <div class="row g-1 mb-1">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="destinationSearch" id="destinationSearch" type="text"
                                    placeholder="e.g. enabled=yes name=AB? email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i"
                                    title="Default search is by name. Use name, comment, country, email, enabled, monitor, backup, forceproxy and options rules."
                                    value='<c:out value="${destinationSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="destinationType" id="destinationType" onchange="destsTableReload()" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${destinationType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-2 d-flex gap-1">
                            <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-search"></i> Search</button>
                            <button type="button" class="btn btn-outline-secondary px-2"
                                    id="btnDestQB"
                                    onclick="toggleQBPanel('destQueryBuilder','btnDestQB')"
                                    title="Build query">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>
                    <%-- Row 2: secondary filters --%>
                    <div class="row g-2">
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-activity"></i></span>
                                <select class="form-select form-select-sm" name="destinationStatus" onchange="destsTableReload()" title="Filter by Status">
                                    <c:forEach var="option" items="${statusOptions}">
                                        <option value="${option}" <c:if test="${destinationStatus == option}">selected</c:if>>${option}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-file-zip"></i></span>
                                <select class="form-select form-select-sm" name="destinationFilter" onchange="destsTableReload()" title="Filter by Compression">
                                    <c:forEach var="option" items="${filterOptions}">
                                        <option value="${option.name}" <c:if test="${destinationFilter == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-diagram-2"></i></span>
                                <select class="form-select form-select-sm" name="aliases" onchange="destsTableReload()" title="Aliased From/To">
                                    <option value="all" <c:if test="${aliases == 'all'}">selected</c:if>>All Destinations</option>
                                    <option value="to"  <c:if test="${aliases == 'to'}">selected</c:if>>Aliased From ...</option>
                                    <option value="from" <c:if test="${aliases == 'from'}">selected</c:if>>Aliases To ...</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-sort-alpha-down"></i></span>
                                <select class="form-select form-select-sm" name="sortDirection" onchange="destsTableReload()" title="Sort Direction">
                                    <option value="asc"  <c:if test="${sortDirection == 'asc'}">selected</c:if>>Ascending</option>
                                    <option value="desc" <c:if test="${sortDirection == 'desc'}">selected</c:if>>Descending</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="destQueryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>name=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_name" placeholder="e.g. dest_*" oninput="dqbPreview()" list="dqb_name_list" autocomplete="off">
                                    <datalist id="dqb_name_list">
                                        <c:forEach var="d" items="${destinationNames}">
                                            <option value="${d.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_comment" placeholder="e.g. *test*" oninput="dqbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_email" placeholder="e.g. *@meteo.ms" oninput="dqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-md-8">
                                    <label class="form-label mb-0 fw-semibold"><code>country=</code></label>
                                    <div class="d-flex align-items-center gap-1">
                                        <select class="form-select form-select-sm" id="dqb_country" onchange="dqbPreview()">
                                            <option value="">Any</option>
                                            <c:forEach var="c" items="${countryOptions}">
                                                <option value="${fn:toLowerCase(c.iso)}">${c.name}</option>
                                            </c:forEach>
                                        </select>
                                        <span id="dqb_country_flag" class="fi" style="font-size:1.4em;display:none;flex-shrink:0"></span>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_options" placeholder="e.g. *mqtt*" oninput="dqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="dqb_enabled" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>monitor</code></label>
                                    <select class="form-select form-select-sm" id="dqb_monitor" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>backup</code></label>
                                    <select class="form-select form-select-sm" id="dqb_backup" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>forceproxy</code></label>
                                    <select class="form-select form-select-sm" id="dqb_forceproxy" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="dqb_case" onchange="dqbPreview()">
                                        <option value="s">Sensitive (default)</option>
                                        <option value="i">Case-insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all" id="dqb_preview">-- fill in fields above --</code>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dqbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="dqbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
        </form>
        <script>
        function dqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function dqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function dqbBuild() {
            var p = [];
            ['name','comment','email','country','options'].forEach(function(f) {
                var v = dqbVal('dqb_' + f); if (v) p.push(f + '=' + dqbQuote(v));
            });
            ['enabled','monitor','backup','forceproxy'].forEach(function(f) {
                var v = dqbVal('dqb_' + f); if (v) p.push(f + '=' + v);
            });
            if (dqbVal('dqb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function dqbPreview() {
            var q = dqbBuild();
            document.getElementById('dqb_preview').textContent = q || '-- fill in fields above --';
        }
        function dqbApply() {
            document.getElementById('destinationSearch').value = dqbBuild();
            document.getElementById('destQueryBuilder').style.display = 'none';
            destsTableReload();
        }
        function dqbClear() {
            ['name','comment','email','country','options'].forEach(function(f) {
                document.getElementById('dqb_' + f).value = '';
            });
            ['enabled','monitor','backup','forceproxy'].forEach(function(f) {
                document.getElementById('dqb_' + f).value = '';
            });
            document.getElementById('dqb_case').value = 's';
            dqbPreview();
        }
        // Flag preview inline with country select
        function updateFlag() {
            var sel = document.getElementById('dqb_country');
            var flag = document.getElementById('dqb_country_flag');
            if (!sel || !flag) return;
            var iso = sel.value;
            if (!iso) { flag.style.display = 'none'; return; }
            flag.className = 'fi fi-' + iso.toLowerCase();
            flag.style.display = 'inline-block';
        }
        document.addEventListener('DOMContentLoaded', function() {
            var sel = document.getElementById('dqb_country');
            if (sel) sel.addEventListener('change', updateFlag);
            parseQBQuery(document.getElementById('destinationSearch').value, 'dqb_', [], []);
            updateFlag();
            dqbPreview();
        });
        function parseQBQuery(q, prefix, pairFields, singleFields) {
            if (!q || !q.trim()) return;
            var rangeCount = {};
            var re = /([a-zA-Z]+)(>=|<=|>|<|=)"([^"]*)"|([a-zA-Z]+)(>=|<=|>|<|=)([^\s]*)/g;
            var m;
            while ((m = re.exec(q)) !== null) {
                try {
                    var field = m[1] || m[4], op = m[2] || m[5], val = m[1] ? m[3] : m[6];
                    if (pairFields && pairFields.indexOf(field) >= 0) {
                        rangeCount[field] = (rangeCount[field] || 0) + 1;
                        var idx = rangeCount[field];
                        var opEl = document.getElementById(prefix + field + '_op' + idx);
                        var valEl = document.getElementById(prefix + field + '_val' + idx);
                        var unitEl = document.getElementById(prefix + field + '_unit' + idx);
                        if (opEl) opEl.value = op;
                        if (unitEl) {
                            var unit = '', num = val;
                            ['gb','mb','kb'].forEach(function(u) { if (num.toLowerCase().endsWith(u)) { unit = u; num = num.slice(0, -u.length); } });
                            if (valEl) valEl.value = num; unitEl.value = unit;
                        } else { if (valEl) valEl.value = val; }
                    } else if (singleFields && singleFields.indexOf(field) >= 0) {
                        var opEl2 = document.getElementById(prefix + field + '_op');
                        var valEl2 = document.getElementById(prefix + field + '_val');
                        if (opEl2) opEl2.value = op; if (valEl2) valEl2.value = val;
                    } else if (op === '=') {
                        var el = document.getElementById(prefix + field);
                        if (el) { var lv = val.toLowerCase(); el.value = (lv==='true'||lv==='yes') ? 'yes' : (lv==='false'||lv==='no') ? 'no' : val; }
                    }
                } catch(e) { /* ignore unparseable token */ }
            }
        }
        function toggleQBPanel(panelId, btnId) {
            var panel = document.getElementById(panelId);
            var btn = document.getElementById(btnId);
            if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
            if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
            var r = btn.getBoundingClientRect();
            var sy = window.pageYOffset || document.documentElement.scrollTop;
            var sx = window.pageXOffset || document.documentElement.scrollLeft;
            var pw = 740;
            panel.style.top = (r.bottom + sy + 4) + 'px';
            panel.style.left = Math.max(sx, r.right + sx - pw) + 'px';
            panel.style.width = pw + 'px';
            panel.style.right = 'auto';
            parseQBQuery(document.getElementById('destinationSearch').value, 'dqb_', [], []);
            updateFlag();
            dqbPreview();
            panel.style.display = 'block';
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('destQueryBuilder');
            var btn = document.getElementById('btnDestQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
                panel.style.display = 'none';
        });
        window.addEventListener('resize', function() {
            var panel = document.getElementById('destQueryBuilder');
            if (panel) panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- No results --%>
<c:if test="${empty columns}">
    <div class="alert">
        <c:if test="${!hasDestinationSearch}">
            No Destinations found matching these criteria.
        </c:if>
        <c:if test="${hasDestinationSearch}">
            <c:if test="${!empty getDestinationsError}">
                <strong>Error in your query:</strong> ${getDestinationsError}
            </c:if>
            <c:if test="${empty getDestinationsError}">
                No Destinations found. The default search is by name or email address.
            </c:if>
            <p class="mb-1 mt-2">You can conduct an extended search using the following rules:</p>
            <ul class="mb-0">
                <li><code>name=</code>, <code>comment=</code>, <code>country=</code>, <code>email=</code>, <code>enabled=yes/no</code>, <code>monitor=</code>, <code>backup=</code>, <code>forceproxy=</code>, <code>options=</code></li>
                <li>Example: <code>enabled=yes name=des0?_a* email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i</code></li>
                <li><code>case=i</code> for case-insensitive, <code>case=s</code> for case-sensitive (default)</li>
                <li>Enclose values with spaces or equals signs in double quotes, e.g. <code>"United States"</code></li>
                <li>Wildcards: <code>*</code> (zero or more chars), <code>?</code> (exactly one char)</li>
            </ul>
        </c:if>
    </div>
</c:if>


<%-- Results table --%>
<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small" id="destsFoundLabel"><i class="bi bi-list-ul"></i> Loading...</span>
    <div class="ms-auto d-flex gap-2 align-items-center">
        <auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
        <auth:then>
            <a href='<bean:message key="destination.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new destination">
                <i class="bi bi-plus-circle"></i> Create
            </a>
        </auth:then>
        </auth:if>
        <button id="btnDestLayout" type="button" class="btn btn-sm btn-outline-secondary"
            onclick="toggleDestLayout()"
            title="Toggle between single-column and two-column split view">
        <i class="bi bi-layout-three-columns"></i> Split
    </button>
    </div>
</div>
<table id="destinationsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th style="width:36px;"></th>
            <th>Destination</th>
            <th style="width:110px;">Status</th>
            <th>Aliases</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
<script>
(function() {
    var _opts = {
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/transfer/destination?json=list',
            type: 'GET',
            data: function(d) {
                d.destinationSearch  = $('#destinationSearch').val() || '';
                d.sortDirection      = $('#destinationSearchForm [name="sortDirection"]').val() || 'asc';
                d.aliases            = $('#destinationSearchForm [name="aliases"]').val() || 'All';
                d.destinationStatus  = $('#destinationSearchForm [name="destinationStatus"]').val() || 'All Status';
                d.destinationType    = $('#destinationSearchForm [name="destinationType"]').val() || '-1';
                d.destinationFilter  = $('#destinationSearchForm [name="destinationFilter"]').val() || 'All';
            }
        },
        paging: true, pageLength: 25, searching: false, autoWidth: false, order: [],
        columns: [
            { orderable: false, data: 0, width: '36px' },
            { orderable: true,  data: 1 },
            { orderable: true,  data: 2 },
            { orderable: true,  data: 3 }
        ],
        columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
        createdRow: function(row, data) {
            $('td', row).each(function(i) { $(this).html(data[i]); });
        },
        drawCallback: function(settings) {
            var total = settings.json ? settings.json.recordsTotal : 0;
            $('#destsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> destination(s) found');
        },
        language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_', processing: 'Loading...' }
    };
    var _isSplit = false;
    var _allRows = [];
    var _sortCol = -1;
    var _sortAsc = true;
    var _pageLen = 25;
    var _curPage = 0;
    var _destsTable;

    window.destsTableReload = function() {
        if (_isSplit) {
            _loadSplitRows();
        } else if (_destsTable) {
            _destsTable.ajax.reload();
        }
    };

    function _currentFilters() {
        return {
            destinationSearch:  $('#destinationSearch').val() || '',
            sortDirection:      $('#destinationSearchForm [name="sortDirection"]').val() || 'asc',
            aliases:            $('#destinationSearchForm [name="aliases"]').val() || 'All',
            destinationStatus:  $('#destinationSearchForm [name="destinationStatus"]').val() || 'All Status',
            destinationType:    $('#destinationSearchForm [name="destinationType"]').val() || '-1',
            destinationFilter:  $('#destinationSearchForm [name="destinationFilter"]').val() || 'All'
        };
    }

    function _colText(row, col) {
        var c = row.querySelectorAll('td')[col];
        return c ? c.textContent.trim().toLowerCase() : '';
    }

    function _redistribute() {
        var start = _curPage * _pageLen;
        var end   = Math.min(start + _pageLen, _allRows.length);
        var page  = _allRows.slice(start, end);
        var half  = Math.ceil(page.length / 2);

        $('#destTableL tbody').empty();
        $('#destTableR tbody').empty();
        page.slice(0, half).forEach(function(r) { $('#destTableL tbody').append(r); });
        page.slice(half).forEach(function(r)    { $('#destTableR tbody').append(r); });

        $('#destSplitInfo').text(_allRows.length > 0
            ? 'Showing ' + (start + 1) + '\u2013' + end + ' of ' + _allRows.length
            : 'No entries');

        var tp    = Math.max(1, Math.ceil(_allRows.length / _pageLen));
        var $pager = $('#destSplitPager').empty();
        var $ul = $('<ul class="pagination mb-0">');
        function mkNavLi(label, targetPage, disabled) {
            var $li = $('<li class="dt-paging-button page-item' + (disabled ? ' disabled' : '') + '">');
            var $a = $('<a class="page-link" href="#">').html(label);
            if (!disabled) { $a.on('click', function(e) { e.preventDefault(); _curPage = targetPage; _redistribute(); }); }
            return $li.append($a);
        }
        $ul.append(mkNavLi('&laquo;', 0, _curPage === 0));
        $ul.append(mkNavLi('&lsaquo;', _curPage - 1, _curPage === 0));
        // DataTables-style windowed pagination: first, ...gap, cur±2, ...gap, last
        var pages = [];
        for (var p = 0; p < tp; p++) {
            if (p === 0 || p === tp - 1 || (p >= _curPage - 2 && p <= _curPage + 2)) {
                pages.push(p);
            }
        }
        var prev = -1;
        pages.forEach(function(p) {
            if (prev !== -1 && p > prev + 1) {
                $('<li class="dt-paging-button page-item disabled">')
                    .append($('<a class="page-link">').text('\u2026')).appendTo($ul);
            }
            $('<li class="dt-paging-button page-item' + (p === _curPage ? ' active' : '') + '">')
                .append((function(pg) {
                    return $('<a class="page-link" href="#">').text(pg + 1).on('click', function(e) {
                        e.preventDefault(); _curPage = pg; _redistribute();
                    });
                })(p)).appendTo($ul);
            prev = p;
        });
        $ul.append(mkNavLi('&rsaquo;', _curPage + 1, _curPage >= tp - 1));
        $ul.append(mkNavLi('&raquo;', tp - 1, _curPage >= tp - 1));
        $pager.append($('<nav>').append($ul));

        ['#destTableL', '#destTableR'].forEach(function(sel) {
            $(sel + ' thead th').each(function(i) {
                $(this).removeClass('dt-ordering-asc dt-ordering-desc');
                if (i === 0) return;
                if (i === _sortCol) {
                    $(this).addClass(_sortAsc ? 'dt-ordering-asc' : 'dt-ordering-desc');
                }
            });
        });

        var $rowsL = $('#destTableL tbody tr').css('height', '');
        var $rowsR = $('#destTableR tbody tr').css('height', '');
        $rowsL.each(function(i) {
            var $r = $rowsR.eq(i);
            if ($r.length) {
                var h = Math.max($(this).outerHeight(), $r.outerHeight());
                $(this).css('height', h + 'px');
                $r.css('height', h + 'px');
            }
        });
    }

    function _attachSplitSort() {
        ['#destTableL', '#destTableR'].forEach(function(sel) {
            $(sel + ' thead th').each(function(colIdx) {
                if (colIdx === 0) return;
                var $th = $(this);
                $th.addClass('dt-orderable-asc dt-orderable-desc').css('cursor', 'pointer');
                if (!$th.find('span.dt-column-order').length) {
                    $th.append('<span class="dt-column-order"></span>');
                }
                $th.off('click.dsort').on('click.dsort', function() {
                    if (_sortCol === colIdx) { _sortAsc = !_sortAsc; }
                    else { _sortCol = colIdx; _sortAsc = true; }
                    _allRows.sort(function(a, b) {
                        var va = _colText(a, colIdx), vb = _colText(b, colIdx);
                        return _sortAsc ? va.localeCompare(vb) : vb.localeCompare(va);
                    });
                    _curPage = 0;
                    _redistribute();
                });
            });
        });
    }

    function _rowsFromData(data) {
        return data.map(function(cols) {
            var tr = document.createElement('tr');
            cols.forEach(function(html) {
                var td = document.createElement('td');
                td.innerHTML = html;
                tr.appendChild(td);
            });
            return tr;
        });
    }

    function _loadSplitRows() {
        var params = $.extend(_currentFilters(), { draw: 1, start: 0, length: -1 });
        $.getJSON('/do/transfer/destination?json=list', params, function(json) {
            _allRows = _rowsFromData(json.data || []);
            $('#destsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + _allRows.length + '</strong> destination(s) found');
            _curPage = 0;
            _redistribute();
            _attachSplitSort();
        });
    }

    window.toggleDestLayout = function() {
        var btn = document.getElementById('btnDestLayout');
        _isSplit = !_isSplit;

        if (_isSplit) {
            if (_destsTable) {
                _pageLen = _destsTable.page.len();
                _destsTable.destroy();
                _destsTable = null;
            }
            $('#destinationsTable tbody').empty();
            var $head = $('#destinationsTable thead').clone();
            _curPage = 0; _sortCol = -1; _sortAsc = true;

            function makeCol(id) {
                return $('<div style="flex:1;min-width:0">').append(
                    $('<table>', { id: id, 'class': 'table table-sm table-hover table-striped align-middle dataTable', style: 'width:100%' })
                    .append($head.clone()).append($('<tbody>'))
                );
            }

            var $lenSel = $('<select id="destSplitLenSel" class="form-select form-select-sm" style="display:inline-block;width:auto;margin-right:0.5em">');
            [10, 25, 50, 100].forEach(function(v) {
                $('<option>', { value: v, selected: v === _pageLen }).text(v).appendTo($lenSel);
            });
            $lenSel.on('change', function() { _pageLen = +this.value; _curPage = 0; _redistribute(); });

            var $toolbar = $('<div class="row mb-2">').append(
                $('<div class="col-auto">').append(
                    $('<div class="dataTables_length">').append(
                        $('<label>').append('Show ').append($lenSel).append('per page')
                    )
                )
            );

            var $tables = $('<div class="d-flex gap-3 align-items-start">')
                .append(makeCol('destTableL'))
                .append(makeCol('destTableR'));

            var $footer = $('<div class="row mt-2">').append(
                $('<div class="col-sm-12 col-md-5">').append(
                    $('<div id="destSplitInfo" class="dataTables_info" style="padding-top:.85em">')
                )
            ).append(
                $('<div class="col-sm-12 col-md-7">').append(
                    $('<div id="destSplitPager" class="dataTables_paginate d-flex justify-content-md-end">')
                )
            );

            $('#destinationsTable').replaceWith(
                $('<div id="destSplitWrap">').append($toolbar).append($tables).append($footer)
            );
            _loadSplitRows();
            btn.innerHTML = '<i class="bi bi-layout-sidebar-inset"></i> Single';
        } else {
            var $head = $('#destTableL thead').clone();
            $head.find('th').removeClass('dt-orderable-asc dt-orderable-desc dt-ordering-asc dt-ordering-desc');
            $head.find('span.dt-column-order').remove();
            $('#destSplitWrap').replaceWith(
                $('<table>', { id: 'destinationsTable', 'class': 'table table-sm table-hover table-striped align-middle', style: 'width:100%' })
                .append($head).append($('<tbody>'))
            );
            _sortCol = -1; _sortAsc = true; _allRows = [];
            _destsTable = $('#destinationsTable').DataTable($.extend({}, _opts, { pageLength: _pageLen }));
            btn.innerHTML = '<i class="bi bi-layout-three-columns"></i> Split';
        }
        localStorage.setItem('destLayout', _isSplit ? 'split' : 'single');
    };

    $(function() {
        _destsTable = $('#destinationsTable').DataTable(_opts);
        $('#destinationSearch').on('keydown', function(e) {
            if (e.key === 'Enter') { e.preventDefault(); destsTableReload(); }
        });
        $('#destinationSearchForm button[type="submit"]').on('click', function(e) {
            e.preventDefault(); destsTableReload();
        });
        if (localStorage.getItem('destLayout') === 'split') {
            window.toggleDestLayout();
        }
    });
})();
</script>
