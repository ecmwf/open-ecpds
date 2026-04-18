<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%-- Filter form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="hostSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <%-- Row 1: search + type + button --%>
                    <div class="row g-1 mb-1">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="hostSearch" id="hostSearch" type="text"
                                    placeholder="e.g. enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i"
                                    title="Default search is by nickname. Use id, hostname, login, password, nickname, comment, dir, enabled, method, email and options rules."
                                    value='<c:out value="${hostSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="hostType" id="hostType" onchange="hostsTableReload()" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${hostType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-2 d-flex gap-1">
                            <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-search"></i> Search</button>
                            <button type="button" class="btn btn-outline-secondary px-2"
                                    id="btnHostQB"
                                    onclick="toggleQBPanel('hostQueryBuilder','btnHostQB')"
                                    title="Build query">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>
                    <%-- Row 2: secondary filters --%>
                    <div class="row g-2">
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-diagram-3"></i></span>
                                <select class="form-select form-select-sm" name="network" onchange="hostsTableReload()" title="Filter by Network">
                                    <c:forEach var="option" items="${networkOptions}">
                                        <option value="${option.name}" <c:if test="${network == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-bookmark"></i></span>
                                <select class="form-select form-select-sm" name="label" onchange="hostsTableReload()" title="Filter by Label">
                                    <c:forEach var="option" items="${labelOptions}">
                                        <option value="${option.name}" <c:if test="${label == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-file-zip"></i></span>
                                <select class="form-select form-select-sm" name="hostFilter" onchange="hostsTableReload()" title="Filter by Compression">
                                    <c:forEach var="option" items="${filterOptions}">
                                        <option value="${option.name}" <c:if test="${hostFilter == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="hostQueryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>nickname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_nickname" placeholder="e.g. Test_0?" oninput="hqbPreview()" list="hqb_nickname_list" autocomplete="off">
                                    <datalist id="hqb_nickname_list">
                                        <c:forEach var="n" items="${hostNickNames}">
                                            <option value="${n}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>hostname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_hostname" placeholder="e.g. *.test.fr" oninput="hqbPreview()" list="hqb_hostname_list" autocomplete="off">
                                    <datalist id="hqb_hostname_list">
                                        <c:forEach var="h" items="${hostHostNames}">
                                            <option value="${h}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>method=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_method" placeholder="e.g. *Http" oninput="hqbPreview()" list="hqb_method_list" autocomplete="off">
                                    <datalist id="hqb_method_list">
                                        <c:forEach var="m" items="${transferMethodOptions}">
                                            <option value="${m.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>id</code> <span class="text-muted fw-normal">numeric</span></label>
                                    <div class="input-group input-group-sm">
                                        <select class="form-select form-select-sm" id="hqb_id_op" style="max-width:65px" onchange="hqbPreview()">
                                            <option value="=">=</option><option value=">=">&gt;=</option><option value=">">&gt;</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="hqb_id_val" placeholder="e.g. 100" oninput="hqbPreview()">
                                    </div>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>login=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_login" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>dir=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_dir" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_comment" placeholder="e.g. *test*" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_email" placeholder="e.g. *@domain.com" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">Properties &amp; JS wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_options" placeholder="e.g. *mqtt*" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-6">
                                    <label class="form-label mb-0 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="hqb_enabled" onchange="hqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-6">
                                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="hqb_case" onchange="hqbPreview()">
                                        <option value="s">Sensitive (default)</option>
                                        <option value="i">Case-insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all" id="hqb_preview">-- fill in fields above --</code>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="hqbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="hqbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                            </div>
                        </div>
                </div>
            </div>
        </form>
        <script>
        function hqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function hqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function hqbBuild() {
            var p = [];
            var idVal = hqbVal('hqb_id_val');
            if (idVal) p.push('id' + hqbVal('hqb_id_op') + idVal);
            ['nickname','hostname','method','comment','email','options','login','dir'].forEach(function(f) {
                var v = hqbVal('hqb_' + f); if (v) p.push(f + '=' + hqbQuote(v));
            });
            var en = hqbVal('hqb_enabled'); if (en) p.push('enabled=' + en);
            if (hqbVal('hqb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function hqbPreview() {
            var q = hqbBuild();
            document.getElementById('hqb_preview').textContent = q || '-- fill in fields above --';
        }
        function hqbApply() {
            document.getElementById('hostSearch').value = hqbBuild();
            document.getElementById('hostQueryBuilder').style.display = 'none';
            hostsTableReload();
        }
        function hqbClear() {
            ['nickname','hostname','method','comment','email','options','login','dir','id_val'].forEach(function(f) {
                document.getElementById('hqb_' + f).value = '';
            });
            document.getElementById('hqb_enabled').value = '';
            document.getElementById('hqb_id_op').value = '=';
            document.getElementById('hqb_case').value = 's';
            hqbPreview();
        }
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
        document.addEventListener('DOMContentLoaded', function() {
            parseQBQuery(document.getElementById('hostSearch').value, 'hqb_', [], ['id']);
            hqbPreview();
        });
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
            parseQBQuery(document.getElementById('hostSearch').value, 'hqb_', [], ['id']);
            hqbPreview();
            panel.style.display = 'block';
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('hostQueryBuilder');
            var btn = document.getElementById('btnHostQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
                panel.style.display = 'none';
        });
        window.addEventListener('resize', function() {
            var panel = document.getElementById('hostQueryBuilder');
            if (panel) panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- No results --%>
<c:if test="${empty hosts}">
    <div class="alert">
        <c:if test="${!hasHostSearch}">
            No Hosts found based on these criteria.
        </c:if>
        <c:if test="${hasHostSearch}">
            <c:if test="${!empty getHostsError}">
                <strong>Error in your query:</strong> ${getHostsError}
            </c:if>
            <c:if test="${empty getHostsError}">
                No Hosts found. Default search is by nickname.
            </c:if>
            <p class="mb-1 mt-2">You can conduct an extended search using the following rules:</p>
            <ul class="mb-0">
                <li><code>id</code>, <code>hostname=</code>, <code>login=</code>, <code>password=</code>, <code>nickname=</code>, <code>comment=</code>, <code>dir=</code>, <code>enabled=yes/no</code>, <code>method=</code>, <code>email=</code>, <code>options=</code></li>
                <li>Example: <code>enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i</code></li>
                <li><code>case=i</code> for case-insensitive, <code>case=s</code> for case-sensitive (default)</li>
                <li>Enclose values with spaces or equals signs in double quotes, e.g. <code>"United States"</code> or <code>"a=b"</code></li>
                <li>Wildcards: <code>*</code> (zero or more chars), <code>?</code> (exactly one char)</li>
            </ul>
        </c:if>
    </div>
</c:if>

<%-- Results table --%>
<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small" id="hostsFoundLabel"><i class="bi bi-list-ul"></i> Loading...</span>
    <div class="ms-auto d-flex gap-2 align-items-center">
        <auth:if basePathKey="host.basepath" paths="/edit/insert_form">
        <auth:then>
            <a href='<bean:message key="host.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new host">
                <i class="bi bi-plus-circle"></i> Create
            </a>
        </auth:then>
        </auth:if>
        <div class="btn-group btn-group-sm" role="group">
            <button type="button" class="btn btn-outline-secondary active" id="btnViewList"
                onclick="switchHostView('list')" title="List view">
            <i class="bi bi-list-ul"></i> List
        </button>
        <button type="button" class="btn btn-outline-secondary" id="btnViewMap"
                onclick="switchHostView('map')" title="Map view">
            <i class="bi bi-geo-alt"></i> Map
        </button>
        </div>
    </div>
</div>
<div id="hostListView">
<table id="hostsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th style="width:36px;"></th>
                <th>Host</th>
                <th>Hostname/IP</th>
                <th>Transfer Group</th>
                <th>Network</th>
                <th>Destinations</th>
            </tr>
        </thead>
        <tbody></tbody>
    </table>
    <script>
    var _hostsTable;
    function hostsTableReload() {
        if (_hostsTable) {
            _hostsTable.ajax.reload();
        }
    }
    $(function() {
        _hostsTable = $('#hostsTable').DataTable({
            serverSide: true,
            processing: true,
            ajax: {
                url: '/do/transfer/host/list',
                type: 'GET',
                data: function(d) {
                    d.label      = $('#hostSearchForm [name="label"]').val() || 'All';
                    d.hostFilter = $('#hostSearchForm [name="hostFilter"]').val() || 'All';
                    d.network    = $('#hostSearchForm [name="network"]').val() || 'All';
                    d.hostType   = $('#hostSearchForm [name="hostType"]').val() || 'All';
                    d.hostSearch = $('#hostSearch').val() || '';
                }
            },
            pageLength: 25,
            searching: false, autoWidth: false,
            order: [],
            columns: [
                { orderable: false, data: 0, width: '36px' },
                { orderable: true,  data: 1 },
                { orderable: true,  data: 2 },
                { orderable: true,  data: 3 },
                { orderable: true,  data: 4 },
                { orderable: false, data: 5 }
            ],
            columnDefs: [{ targets: '_all', render: $.fn.dataTable.render.text() }],
            createdRow: function(row, data) {
                // Columns contain pre-built HTML — render as HTML not escaped text
                $('td', row).each(function(i) { $(this).html(data[i]); });
            },
            drawCallback: function(settings) {
                var total = settings.json ? settings.json.recordsTotal : 0;
                $('#hostsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> host(s) found');
            },
            language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_', processing: 'Loading...' }
        });
        // Allow pressing Enter in the search box to reload
        $('#hostSearch').on('keydown', function(e) {
            if (e.key === 'Enter') { e.preventDefault(); hostsTableReload(); }
        });
        // Search button
        $('#hostSearchForm button[type="submit"]').on('click', function(e) {
            e.preventDefault(); hostsTableReload();
        });
    });
    </script>
</div><%-- /hostListView --%>

<%-- Map view container --%>
<div id="hostMapContainer" style="display:none">
    <div class="d-flex align-items-center mb-2">
        <span class="text-muted small" id="mapFoundLabel"></span>
    </div>
    <div id="hostMap" style="height:calc(100vh - 300px); min-height:420px; border-radius:0.375rem; border:1px solid var(--bs-border-color)"></div>
</div>

<script src="/openlayer/ol.js"></script>
<script src="/openlayer/ol-layerswitcher.js"></script>
<script>
(function() {
    var viewMode = 'list';
    var mapInitDone = false;
    var olMap, olSource;

    // ---- View switcher ----
    window.switchHostView = function(mode) {
        viewMode = mode;
        var isList = mode === 'list';
        document.getElementById('hostListView').style.display    = isList ? '' : 'none';
        document.getElementById('hostMapContainer').style.display = isList ? 'none' : '';
        document.getElementById('btnViewList').classList.toggle('active', isList);
        document.getElementById('btnViewMap').classList.toggle('active', !isList);
        if (!isList) {
            if (!mapInitDone) { initMap(); mapInitDone = true; }
            else { loadMapFeatures(); }
            // Trigger OL resize since container was hidden during init
            setTimeout(function() { if (olMap) olMap.updateSize(); }, 50);
        }
    };

    // ---- Style helpers ----
    function makeStyle(color, r) {
        return new ol.style.Style({
            image: new ol.style.Circle({
                radius: r || 7,
                fill: new ol.style.Fill({ color: color }),
                stroke: new ol.style.Stroke({ color: '#fff', width: 1.5 })
            })
        });
    }

    var STYLES = {
        'Dissemination': { normal: makeStyle('rgba(13,110,253,0.85)'),   hover: makeStyle('rgba(13,110,253,1)',9) },
        'Acquisition':   { normal: makeStyle('rgba(25,135,84,0.85)'),    hover: makeStyle('rgba(25,135,84,1)',9) },
        'Source':        { normal: makeStyle('rgba(253,126,20,0.85)'),   hover: makeStyle('rgba(253,126,20,1)',9) },
        'inactive':      { normal: makeStyle('rgba(108,117,125,0.55)',6), hover: makeStyle('rgba(108,117,125,0.9)',8) }
    };

    function styleFor(f, hover) {
        if (!f.get('active')) return hover ? STYLES.inactive.hover : STYLES.inactive.normal;
        var t = f.get('type');
        var s = STYLES[t] || STYLES['Dissemination'];
        return hover ? s.hover : s.normal;
    }

    // ---- Map init ----
    function initMap() {
        olSource = new ol.source.Vector();

        var layer = new ol.layer.Vector({
            source: olSource,
            style: function(f) { return styleFor(f, false); }
        });

        olMap = new ol.Map({
            target: 'hostMap',
            controls: ol.control.defaults.defaults({ rotate: false, attribution: false }),
            layers: [
                new ol.layer.Tile({ source: new ol.source.OSM({ attributions: [] }) }),
                layer
            ],
            view: new ol.View({ center: ol.proj.fromLonLat([10, 48]), zoom: 3 })
        });

        // Initial load with current filters
        loadMapFeatures();

        // Hover
        var hovered = null;
        olMap.on('pointermove', function(evt) {
            var f = olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return f; });
            olMap.getTargetElement().style.cursor = f ? 'pointer' : '';
            if (hovered && hovered !== f) { hovered.setStyle(null); hovered = null; }
            if (f && f !== hovered) { f.setStyle(styleFor(f, true)); hovered = f; }
        });

        // Click → offcanvas
        olMap.on('click', function(evt) {
            var f = olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return f; });
            if (!f) return;
            showHostPanel(f.getProperties());
        });
    }

    // ---- Server-side filtered fetch ----
    function getFormVal(name) {
        var el = document.querySelector('#hostSearchForm [name="' + name + '"]');
        return el ? el.value : '';
    }

    function buildMapUrl() {
        var p = new URLSearchParams();
        p.set('hostType',   getFormVal('hostType')   || 'All');
        p.set('network',    getFormVal('network')    || 'All');
        p.set('label',      getFormVal('label')      || 'All');
        p.set('hostFilter', getFormVal('hostFilter') || 'All');
        p.set('hostSearch', (document.getElementById('hostSearch') || {}).value || '');
        return '/do/transfer/mapjson?' + p.toString();
    }

    var _loadTimer = null;
    function loadMapFeatures() {
        document.getElementById('mapFoundLabel').textContent = 'Loading...';
        fetch(buildMapUrl())
            .then(function(r) { return r.json(); })
            .then(function(geojson) {
                var fmt = new ol.format.GeoJSON();
                var features = fmt.readFeatures(geojson, { featureProjection: 'EPSG:3857' });
                olSource.clear(true);
                olSource.addFeatures(features);
                var n = features.length;
                document.getElementById('mapFoundLabel').textContent =
                    n + ' host' + (n === 1 ? '' : 's') + ' with coordinates';
                if (n > 0) {
                    var ext = olSource.getExtent();
                    if (!ol.extent.isEmpty(ext)) {
                        olMap.getView().fit(ext, { padding: [40,40,40,40], maxZoom: 7, duration: 300 });
                    }
                }
            })
            .catch(function(e) {
                console.error('Map load error:', e);
                document.getElementById('mapFoundLabel').textContent = 'Error loading hosts';
            });
    }

    // Debounced reload so rapid typing doesn't fire many requests
    function scheduleMapReload() {
        clearTimeout(_loadTimer);
        _loadTimer = setTimeout(loadMapFeatures, 400);
    }

    // ---- Host detail panel ----
    var _offcanvas = null;
    function showHostPanel(p) {
        var badge = p.active
            ? '<span class="badge bg-success">Active</span>'
            : '<span class="badge bg-secondary">Inactive</span>';
        var typeColors = { Dissemination:'primary', Acquisition:'success', Source:'warning' };
        var tc = typeColors[p.type] || 'secondary';
        var typeBadge = '<span class="badge bg-' + tc + ' ms-1">' + esc(p.type) + '</span>';

        var html = '<div class="mb-3">' + badge + typeBadge + '</div>'
            + '<table class="table table-sm table-borderless mb-0">'
            + row('Nickname', '<a href="' + p.url + '" class="fw-semibold">' + esc(p.nickname) + '</a>')
            + row('Host ID', '<code>' + esc(p.id) + '</code>')
            + row('Hostname', esc(p.hostname) || '<span class="text-muted">—</span>')
            + row('Network',  esc(p.network)  || '<span class="text-muted">—</span>')
            + row('Method',   esc(p.method)   || '<span class="text-muted">—</span>')
            + row('Location', esc(p.geo)      || '<span class="text-muted">—</span>')
            + (p.comment ? row('Comment', '<span class="text-muted small">' + esc(p.comment) + '</span>') : '')
            + '</table>'
            + '<div class="mt-3"><a href="' + p.url + '" class="btn btn-sm btn-outline-primary w-100">'
            + '<i class="bi bi-arrow-right-circle me-1"></i>Open host page</a></div>';

        document.getElementById('hostDetailTitle').innerHTML =
            '<i class="bi bi-hdd-network me-1"></i>' + esc(p.nickname || p.id);
        document.getElementById('hostDetailBody').innerHTML = html;

        if (!_offcanvas) {
            _offcanvas = new bootstrap.Offcanvas(document.getElementById('hostDetailPanel'));
        }
        _offcanvas.show();
    }

    function row(label, val) {
        return '<tr><th class="text-muted fw-normal pe-2" style="width:85px;white-space:nowrap">'
            + label + '</th><td>' + val + '</td></tr>';
    }
    function esc(s) {
        return s ? String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
                             .replace(/"/g,'&quot;').replace(/'/g,'&#39;') : '';
    }

    // Wire form filter changes to reload map when in map mode
    document.addEventListener('DOMContentLoaded', function() {
        ['hostType','network','label','hostFilter'].forEach(function(name) {
            var el = document.querySelector('#hostSearchForm [name="' + name + '"]');
            if (el) el.addEventListener('change', function() {
                if (viewMode === 'map') scheduleMapReload();
            });
        });
        var searchEl = document.getElementById('hostSearch');
        if (searchEl) searchEl.addEventListener('input', function() {
            if (viewMode === 'map') scheduleMapReload();
        });
        // Search form submit in map mode → immediate reload
        document.getElementById('hostSearchForm').addEventListener('submit', function(e) {
            if (viewMode === 'map') { e.preventDefault(); loadMapFeatures(); }
        });
    });
})();
</script>

<%-- Host detail offcanvas (shared between list and map view) --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="hostDetailPanel" style="width:360px;">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <h6 class="offcanvas-title mb-0" id="hostDetailTitle">Host Details</h6>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
    </div>
    <div class="offcanvas-body p-3" id="hostDetailBody"></div>
</div>
