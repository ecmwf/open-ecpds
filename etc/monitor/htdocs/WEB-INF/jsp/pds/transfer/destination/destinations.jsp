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
                    <%-- Main search row --%>
                    <div class="row g-1">
                        <div class="col-auto">
                            <div class="input-group flex-nowrap" style="width:auto" title="Page size">
                                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                                <select id="destPageLen" class="form-select" style="width:auto">
                                    <option value="10">10</option>
                                    <option value="25">25</option>
                                    <option value="50">50</option>
                                    <option value="100">100</option>
                                    <option value="250">250</option>
                                </select>
                            </div>
                        </div>
                        <div class="col">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="destinationSearch" id="destinationSearch" type="text"
                                    placeholder="e.g. enabled=yes name=AB? email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i"
                                    title="Default search is by name. Use name, comment, country, email, enabled, monitor, backup, forceproxy and options rules."
                                    value='<c:out value="${destinationSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-12 col-sm-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted" id="destTypeIcon"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="destinationType" id="destinationType" onchange="destsTableReload(); _updateDestTypeStyle(); this.blur();" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${destinationType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-auto d-flex gap-1">
                            <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i><span class="d-none d-sm-inline ms-1">Search</span></button>
                            <button type="button" class="btn btn-outline-primary position-relative"
                                    id="btnDestQB"
                                    onclick="toggleQBPanel('destQueryBuilder','btnDestQB')"
                                    title="Filter">
                                <i class="bi bi-sliders2"></i><span class="d-none d-sm-inline ms-1">Filter</span>
                                <span id="btnDestQB-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;font-size:0.75rem;min-width:1.3em;padding:0.2em 0.4em;line-height:1.2"></span>
                            </button>
                            <button class="btn btn-link btn-sm text-muted p-0" type="button"
                                    data-bs-toggle="collapse" data-bs-target="#dstQBHelp"
                                    aria-expanded="false" title="Search syntax help">
                                <i class="bi bi-info-circle"></i>
                            </button>
                        </div>
                    </div>
                    <div class="collapse mt-1" id="dstQBHelp">
                        <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
                            <strong class="d-block mb-1">Search &amp; Filter syntax</strong>
                            <p class="mb-1">Type directly in the search box or click <i class="bi bi-sliders2"></i> <strong>Filter</strong> to use the visual query builder. Terms can be combined freely.</p>
                            <ul class="mb-1 ps-3">
                                <li><strong>Default (no prefix)</strong> &mdash; matches the destination <code>name</code>. Wildcards <code>*</code> and <code>?</code> are supported.</li>
                                <li><code>name=dest_*</code>, <code>comment=*test*</code>, <code>email=*@meteo.ms</code>, <code>options=*mqtt*</code> &mdash; text filters with wildcards.</li>
                                <li><code>country=</code> &mdash; filter by ISO country code (e.g. <code>country=FR</code>).</li>
                                <li><code>enabled=yes|no</code>, <code>monitor=yes|no</code>, <code>backup=yes|no</code>, <code>forceproxy=yes|no</code> &mdash; boolean flags.</li>
                                <li><code>case=i</code> &mdash; make the search case-insensitive (default is case-sensitive).</li>
                                <li><i class="bi bi-sliders2 me-1"></i><strong>Filter panel</strong> also provides <strong>Status</strong>, <strong>Compression</strong> and <strong>Aliases</strong> drop-downs that apply independently of the search text.</li>
                            </ul>
                            <p class="mb-0 text-muted">Example: <code>name=efas_* enabled=yes monitor=yes case=i</code></p>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="destQueryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>name=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_name" placeholder="e.g. dest_*" oninput="dqbPreview()" list="dqb_name_list" autocomplete="off">
                                    <datalist id="dqb_name_list">
                                        <c:forEach var="d" items="${destinationNames}">
                                            <option value="${d.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_comment" placeholder="e.g. *test*" oninput="dqbPreview()">
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_email" placeholder="e.g. *@meteo.ms" oninput="dqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-12 col-md-8">
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
                                <div class="col-12 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_options" placeholder="e.g. *mqtt*" oninput="dqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-6 col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="dqb_enabled" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-6 col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>monitor</code></label>
                                    <select class="form-select form-select-sm" id="dqb_monitor" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-6 col-md-3">
                                    <label class="form-label mb-0 fw-semibold"><code>backup</code></label>
                                    <select class="form-select form-select-sm" id="dqb_backup" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-6 col-md-3">
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
                            <%-- Direct filters (not part of search string, applied immediately) --%>
                            <div class="border-top mt-1 pt-1">
                                <div class="row g-1 mb-1">
                                    <div class="col-12 col-md-3">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-activity me-1 text-muted"></i>Status</label>
                                        <select class="form-select form-select-sm" name="destinationStatus" id="destinationStatus" onchange="destsTableReload();dqbUpdateBadge();" title="Filter by Status">
                                            <c:forEach var="option" items="${statusOptions}">
                                                <option value="${option}" <c:if test="${destinationStatus == option}">selected</c:if>>${option}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-3">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-file-zip me-1 text-muted"></i>Compression</label>
                                        <select class="form-select form-select-sm" name="destinationFilter" id="destinationFilter" onchange="destsTableReload();dqbUpdateBadge();" title="Filter by Compression">
                                            <c:forEach var="option" items="${filterOptions}">
                                                <option value="${option.name}" <c:if test="${destinationFilter == option.name}">selected</c:if>>${option.value}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-3">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-diagram-2 me-1 text-muted"></i>Aliases</label>
                                        <select class="form-select form-select-sm" name="aliases" id="aliases" onchange="destsTableReload();dqbUpdateBadge();" title="Aliased From/To">
                                            <option value="all" <c:if test="${aliases == 'all'}">selected</c:if>>All Destinations</option>
                                            <option value="to"  <c:if test="${aliases == 'to'}">selected</c:if>>Aliased From ...</option>
                                            <option value="from" <c:if test="${aliases == 'from'}">selected</c:if>>Aliases To ...</option>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-3">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-people me-1 text-muted"></i>Data Users</label>
                                        <select class="form-select form-select-sm" id="datausers" onchange="destsTableReload();dqbUpdateBadge();" title="Filter by Data User association">
                                            <option value="any" <c:if test="${datausers == 'any' || empty datausers}">selected</c:if>>Any</option>
                                            <option value="yes" <c:if test="${datausers == 'yes'}">selected</c:if>>With Data Users</option>
                                            <option value="no"  <c:if test="${datausers == 'no'}">selected</c:if>>Without Data Users</option>
                                        </select>
                                    </div>
                                </div>
                                <%-- Editor warning filters --%>
                                <div class="row g-1 mb-1">
                                    <div class="col-12 col-md-6">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>Properties editor</label>
                                        <select class="form-select form-select-sm" id="dqb_propErrors" onchange="destsTableReload();dqbUpdateBadge();" title="Filter by Properties editor errors">
                                            <option value=""    <c:if test="${empty propErrors}">selected</c:if>>Any</option>
                                            <option value="yes" <c:if test="${propErrors == 'yes'}">selected</c:if>>Has errors</option>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-braces text-secondary me-1"></i>JavaScript editor</label>
                                        <select class="form-select form-select-sm" id="dqb_jsNonEmpty" onchange="destsTableReload();dqbUpdateBadge();" title="Filter by JavaScript">
                                            <option value=""    <c:if test="${empty jsNonEmpty}">selected</c:if>>Any</option>
                                            <option value="yes" <c:if test="${jsNonEmpty == 'yes'}">selected</c:if>>Is configured</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1 flex-wrap">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all;min-width:0" id="dqb_preview">-- fill in fields above --</code>
                                <div class="d-flex gap-1 flex-shrink-0">
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
                </div>
        </form>
        <script>
        function dqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function dqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function dqbCountActive() {
            var n = 0;
            ['name','comment','email','country','options'].forEach(function(f) { if (dqbVal('dqb_'+f)) n++; });
            ['enabled','monitor','backup','forceproxy'].forEach(function(f) { if (dqbVal('dqb_'+f)) n++; });
            if (dqbVal('dqb_case') !== 's') n++;
            var st = document.getElementById('destinationStatus'); if (st && st.selectedIndex > 0) n++;
            var cf = document.getElementById('destinationFilter'); if (cf && cf.selectedIndex > 0) n++;
            var al = document.getElementById('aliases'); if (al && al.value !== 'all') n++;
            var du = document.getElementById('datausers'); if (du && du.value !== 'any') n++;
            if (dqbVal('dqb_propErrors')) n++;
            if (dqbVal('dqb_jsNonEmpty')) n++;
            return n;
        }
        function dqbUpdateBadge() {
            var n = dqbCountActive();
            var b = document.getElementById('btnDestQB-badge');
            if (b) { b.textContent = n; b.style.display = n > 0 ? '' : 'none'; }
            var btn = document.getElementById('btnDestQB');
            if (btn) { btn.classList.toggle('btn-outline-primary', n === 0); btn.classList.toggle('btn-warning', n > 0); }
        }
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
            dqbUpdateBadge();
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
            document.getElementById('destinationStatus').selectedIndex = 0;
            document.getElementById('destinationFilter').selectedIndex = 0;
            document.getElementById('aliases').selectedIndex = 0;
            document.getElementById('datausers').selectedIndex = 0;
            document.getElementById('dqb_propErrors').selectedIndex = 0;
            document.getElementById('dqb_jsNonEmpty').selectedIndex = 0;
            document.getElementById('destinationSearch').value = '';
            document.getElementById('destQueryBuilder').style.display = 'none';
            updateFlag();
            dqbPreview();
            destsTableReload();
        }
        // Flag preview inline with country select
        function updateFlag() {
            var sel = document.getElementById('dqb_country');
            var flag = document.getElementById('dqb_country_flag');
            if (!sel || !flag) return;
            var iso = sel.value;
            if (!iso) { flag.style.display = 'none'; return; }
            if (iso === 'ex') {
                flag.className = 'bi bi-globe';
            } else {
                flag.className = 'fi fi-' + iso.toLowerCase();
            }
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
        function _qbRepos(panel, btn, pw) {
            var r = btn.getBoundingClientRect();
            var sy = window.pageYOffset || document.documentElement.scrollTop;
            var sx = window.pageXOffset || document.documentElement.scrollLeft;
            var vw = window.innerWidth || document.documentElement.clientWidth;
            var margin = 8;
            var left = Math.max(sx + margin, Math.min(r.right + sx - pw, sx + vw - pw - margin));
            panel.style.top  = (r.bottom + sy + 4) + 'px';
            panel.style.left = left + 'px';
        }
        function toggleQBPanel(panelId, btnId) {
            var panel = document.getElementById(panelId);
            var btn = document.getElementById(btnId);
            if (panel.style.display === 'block') {
                panel.style.display = 'none';
                if (panel._qbScrollFn) { window.removeEventListener('scroll', panel._qbScrollFn); panel._qbScrollFn = null; }
                return;
            }
            if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
            var vw = window.innerWidth || document.documentElement.clientWidth;
            var pw = Math.min(740, vw - 16);
            panel.style.width = pw + 'px';
            panel.style.right = 'auto';
            panel.style.overflowX = 'auto';
            _qbRepos(panel, btn, pw);
            parseQBQuery(document.getElementById('destinationSearch').value, 'dqb_', [], []);
            updateFlag();
            dqbPreview();
            panel.style.display = 'block';
            panel._qbScrollFn = function() { _qbRepos(panel, btn, pw); };
            window.addEventListener('scroll', panel._qbScrollFn, { passive: true });
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('destQueryBuilder');
            var btn = document.getElementById('btnDestQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
                panel.style.display = 'none';
                if (panel._qbScrollFn) { window.removeEventListener('scroll', panel._qbScrollFn); panel._qbScrollFn = null; }
            }
        });
        var _destQBLastW = window.innerWidth;
        window.addEventListener('resize', function() {
            var w = window.innerWidth;
            if (w === _destQBLastW) return;
            _destQBLastW = w;
            var panel = document.getElementById('destQueryBuilder');
            if (panel) panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- Search error/empty-state banner - content managed dynamically by drawCallback --%>
<div id="destSearchError" class="alert" style="display:none"></div>
<script>
var _destQBTip = '<div class="mt-2 text-muted small"><i class="bi bi-lightbulb"><\/i> Tip: Not sure about the syntax? Use the <a href="#" onclick="event.stopPropagation(); toggleQBPanel(\'destQueryBuilder\',\'btnDestQB\'); document.getElementById(\'btnDestQB\').scrollIntoView({behavior:\'smooth\',block:\'center\'}); return false;" class="link-secondary"><i class="bi bi-sliders2"><\/i> query builder<\/a> above to build a valid search expression.<\/div>';
var _destSearchHelp = '<p class="mb-1 mt-2">You can conduct an extended search using the following rules:<\/p>' +
    '<ul class="mb-0">' +
    '<li><code>name=<\/code>, <code>comment=<\/code>, <code>country=<\/code>, <code>email=<\/code>, <code>enabled=yes\/no<\/code>, <code>monitor=<\/code>, <code>backup=<\/code>, <code>forceproxy=<\/code>, <code>options=<\/code><\/li>' +
    '<li>Example: <code>enabled=yes name=des0?_a* email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i<\/code><\/li>' +
    '<li><code>case=i<\/code> for case-insensitive, <code>case=s<\/code> for case-sensitive (default)<\/li>' +
    '<li>Enclose values with spaces or equals signs in double quotes, e.g. <code>&quot;United States&quot;<\/code><\/li>' +
    '<li>Wildcards: <code>*<\/code> (zero or more chars), <code>?<\/code> (exactly one char)<\/li>' +
    '<\/ul>' + _destQBTip;
function _updateDestSearchBanner(queryError, total, hasSearch) {
    var div = document.getElementById('destSearchError');
    if (!div) return;
    function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
    if (queryError) {
        div.innerHTML = '<strong>Error in your query:<\/strong> ' + esc(queryError) + _destSearchHelp;
        div.style.display = '';
    } else if (total === 0 && hasSearch) {
        div.innerHTML = 'No Destinations found. The default search is by name or email address.' + _destSearchHelp;
        div.style.display = '';
    } else if (total === 0) {
        div.textContent = 'No Destinations found matching these criteria.';
        div.style.display = '';
    } else {
        div.style.display = 'none';
    }
}
<c:if test="${empty columns}">_updateDestSearchBanner('<c:out value="${getDestinationsError}"/>', 0, ${hasDestinationSearch});</c:if>
</script>


<%-- Results table --%>
<div class="d-flex align-items-end mb-2 gap-2">
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
        <div class="dropdown me-2">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="dColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dColModeBtn">
                <li><a class="dropdown-item active" href="#" data-dcol-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-dcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-dcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides detail columns</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-dcol-mode="custom">
                  <strong>Custom</strong><br><small class="text-muted">Choose individual columns</small>
                </a></li>
                <li id="dCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:180px;">
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-0" data-col="0" checked><label class="form-check-label" for="dchk-col-0">Icon</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="dchk-col-1">Destination <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-2" data-col="2"><label class="form-check-label" for="dchk-col-2">Name</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-3" data-col="3" checked><label class="form-check-label" for="dchk-col-3">Status</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-4" data-col="4" checked><label class="form-check-label" for="dchk-col-4">Aliases</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-5" data-col="5" checked><label class="form-check-label" for="dchk-col-5">Category</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-6" data-col="6" checked><label class="form-check-label" for="dchk-col-6">Compression</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-7" data-col="7" checked><label class="form-check-label" for="dchk-col-7">Enabled</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-8" data-col="8" checked><label class="form-check-label" for="dchk-col-8">Acquisition</label></div>
                    <div class="form-check mb-0"><input class="form-check-input d-custom-col-chk" type="checkbox" id="dchk-col-9" data-col="9" checked><label class="form-check-label" for="dchk-col-9">Monitor</label></div>
                  </div>
                </li>
            </ul>
        </div>
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
            <th>Name</th>
            <th style="width:110px;">Status</th>
            <th>Aliases</th>
            <th>Category</th>
            <th>Compression</th>
            <th style="width:80px;">Enabled</th>
            <th style="width:90px;">Acquisition</th>
            <th style="width:80px;">Monitor</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>
<script>
(function() {
    // Suppress DataTables' native alert() for server errors; drawCallback shows them inline.
    $.fn.dataTable.ext.errMode = function () {};

    var _opts = {
        serverSide: true,
        processing: true,
        ajax: {
            url: '/do/transfer/destination?json=list',
            type: 'GET',
            data: function(d) {
                d.destinationSearch  = $('#destinationSearch').val() || '';
                d.aliases            = $('#aliases').val() || 'All';
                d.destinationStatus  = $('#destinationStatus').val() || 'All Status';
                d.destinationType    = $('#destinationType').val() || '-1';
                d.destinationFilter  = $('#destinationFilter').val() || 'All';
                d.datausers          = $('#datausers').val() || 'any';
                d.propErrors         = $('#dqb_propErrors').val() || '';
                d.jsNonEmpty         = $('#dqb_jsNonEmpty').val() || '';
            }
        },
        paging: true, pageLength: 25, lengthChange: false, searching: false, autoWidth: false, order: [],
        columns: [
            { orderable: false, data: 0, width: '36px', className: 'icon-col' },
            { orderable: true,  data: 1, render: function(data, type) {
                if (type === 'sort' || type === 'type') {
                    var tmp = document.createElement('div');
                    tmp.innerHTML = data;
                    var a = tmp.querySelector('a.dest-list-link');
                    return a ? a.textContent.trim() : tmp.textContent.trim();
                }
                return data;
            }},
            { orderable: true,  data: 2 },
            { orderable: true,  data: 3 },
            { orderable: true,  data: 4 },
            { orderable: true,  data: 5 },
            { orderable: false, data: 6 },
            { orderable: false, data: 7, className: 'text-center' },
            { orderable: false, data: 8, className: 'text-center' },
            { orderable: false, data: 9, className: 'text-center' }
        ],
        columnDefs: [{ targets: '_all', render: function(data) { return data; } }],
        drawCallback: function(settings) {
            var json = settings.json || {};
            var total = json.recordsTotal || 0;
            _lastTotal = total;
            var srch = ($('#destinationSearch').val() || '').trim();
            _updateDestSearchBanner(json.queryError || '', total, srch.length > 0);
            $('#destsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> destination(s) found');
            var canSplit = total >= 4 && window.innerWidth >= 992;
            $('#btnDestLayout').toggle(canSplit);
            if (!canSplit && _isSplit) { window.toggleDestLayout(true); }
        },
        language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_', processing: 'Loading...' }
    };
    var _isSplit = false;
    var _allRows = [];
    var _sortCol = -1;
    var _sortAsc = true;
    var _pageLen = (function() { try { var v = parseInt(localStorage.getItem('destsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    var _curPage = 0;
    var _destsTable;
    var _lastTotal = 0;
    var _dApplyMode = function() {};
    var _dColMode = 'auto';

    window.destsTableReload = function() {
        if (_isSplit) {
            _loadSplitRows();
        } else if (_destsTable) {
            _destsTable.ajax.reload();
        }
    };

    window._updateDestTypeStyle = function _updateDestTypeStyle() {
        var sel = document.getElementById('destinationType');
        var icon = document.getElementById('destTypeIcon');
        if (!sel || !icon) return;
        var active = sel.value && sel.value !== '-1';
        if (active) {
            icon.style.setProperty('background-color', 'var(--bs-primary)', 'important');
            icon.style.setProperty('color', '#fff', 'important');
            icon.style.setProperty('border-color', 'var(--bs-primary)', 'important');
            sel.style.setProperty('border-color', 'var(--bs-primary)', 'important');
            sel.style.setProperty('background-color', 'var(--bs-primary-bg-subtle)', 'important');
        } else {
            icon.style.removeProperty('background-color');
            icon.style.removeProperty('color');
            icon.style.removeProperty('border-color');
            sel.style.removeProperty('border-color');
            sel.style.removeProperty('background-color');
        }
    }

    function _currentFilters() {
        return {
            destinationSearch:  $('#destinationSearch').val() || '',
            aliases:            $('#aliases').val() || 'All',
            destinationStatus:  $('#destinationStatus').val() || 'All Status',
            destinationType:    $('#destinationType').val() || '-1',
            destinationFilter:  $('#destinationFilter').val() || 'All',
            datausers:          $('#datausers').val() || 'any'
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
        // DataTables-style windowed pagination: first, ...gap, cur+/-2, ...gap, last
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
        // Re-apply column visibility after rows are refreshed
        _dApplyMode(_dColMode);
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
            var srch = ($('#destinationSearch').val() || '').trim();
            _updateDestSearchBanner(json.queryError || '', (json.data || []).length, srch.length > 0);
            _allRows = _rowsFromData(json.data || []);
            $('#destsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + _allRows.length + '</strong> destination(s) found');
            _curPage = 0;
            _redistribute();
            _attachSplitSort();
        });
    }

    window.toggleDestLayout = function(noSave) {
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
            $head.find('th').css('display', '');
            _curPage = 0; _sortCol = -1; _sortAsc = true;

            function makeCol(id) {
                return $('<div style="flex:1;min-width:0;overflow-x:auto">').append(
                    $('<table>', { id: id, 'class': 'table table-sm table-hover table-striped align-middle dataTable', style: 'width:100%' })
                    .append($head.clone()).append($('<tbody>'))
                );
            }

            var $tables = $('<div class="d-flex gap-3 align-items-start">')
                .append(makeCol('destTableL'))
                .append(makeCol('destTableR'));

            var $footer = $('<div class="row mt-2 align-items-start">').append(
                $('<div class="col-sm-12 col-md-5">').append(
                    $('<div id="destSplitInfo" class="dataTables_info">')
                )
            ).append(
                $('<div class="col-sm-12 col-md-7">').append(
                    $('<div id="destSplitPager" class="dataTables_paginate d-flex justify-content-md-end">')
                )
            );

            $('#destinationsTable').replaceWith(
                $('<div id="destSplitWrap">').append($tables).append($footer)
            );
            _loadSplitRows();
            btn.innerHTML = '<i class="bi bi-layout-sidebar-inset"></i> Single';
        } else {
            var $head = $('#destTableL thead').clone();
            // Clear any inline display:none set by _dShowCols in split mode so
            // DataTables initialises with all columns visible before _dApplyMode runs.
            $head.find('th').css('display', '');
            $head.find('th').removeClass('dt-orderable-asc dt-orderable-desc dt-ordering-asc dt-ordering-desc');
            $head.find('span.dt-column-order').remove();
            $('#destSplitWrap').replaceWith(
                $('<table>', { id: 'destinationsTable', 'class': 'table table-sm table-hover table-striped align-middle', style: 'width:100%' })
                .append($head).append($('<tbody>'))
            );
            _sortCol = -1; _sortAsc = true; _allRows = [];
            _destsTable = $('#destinationsTable').DataTable($.extend({}, _opts, { pageLength: _pageLen }));
            _dApplyMode(_dColMode);
            btn.innerHTML = '<i class="bi bi-layout-three-columns"></i> Split';
        }
        if (!noSave) {
            localStorage.setItem('destLayout', _isSplit ? 'split' : 'single');
        }
    };

    var _splitResizeTimer;
    function _enforceSingleIfNarrow() {
        var wide = window.innerWidth >= 992;
        var canSplit = wide && _lastTotal >= 4;
        $('#btnDestLayout').toggle(canSplit);
        if (!canSplit && _isSplit) window.toggleDestLayout(true);
    }
    $(window).on('resize.destLayout', function() {
        clearTimeout(_splitResizeTimer);
        _splitResizeTimer = setTimeout(_enforceSingleIfNarrow, 200);
    });

    $(function() {
        _updateDestTypeStyle();
        _destsTable = $('#destinationsTable').DataTable($.extend({}, _opts, { pageLength: _pageLen }));
        $('#destPageLen').val(_pageLen);
        $('#destPageLen').on('change', function() {
            _pageLen = parseInt(this.value, 10);
            try { localStorage.setItem('destsPageLen', _pageLen); } catch(e) {}
            if (_isSplit) { _curPage = 0; _redistribute(); }
            else if (_destsTable) { _destsTable.page.len(_pageLen).draw(); }
        });

        var _D_CUSTOM_COL_KEY = 'destsCustomCols';
        var _D_COL_MODE_KEY = 'destsColMode';
        var _dCustomCols = (function() {
            try { var s = localStorage.getItem(_D_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,3,4,5,6,7,8,9];
        })();
        _dColMode = (function() {
            try { return localStorage.getItem(_D_COL_MODE_KEY) || 'auto'; } catch(e) { return 'auto'; }
        })();
        var _dCOMPACT_HIDE = [2, 5, 6, 7, 8, 9];
        var _dSMALL_HIDE = [2, 3, 4, 5, 6, 7, 8, 9];

        function _dShowCols(hideCols) {
            if (_destsTable) {
                var total = _destsTable.columns().count();
                for (var i = 0; i < total; i++) {
                    _destsTable.column(i).visible(hideCols.indexOf(i) === -1, false);
                }
                _destsTable.columns.adjust();
            }
            if (_isSplit) {
                for (var i = 0; i < 10; i++) {
                    var disp = hideCols.indexOf(i) === -1 ? '' : 'none';
                    ['#destTableL', '#destTableR'].forEach(function(sel) {
                        $(sel + ' thead tr th:nth-child(' + (i + 1) + ')').css('display', disp);
                        $(sel + ' tbody tr td:nth-child(' + (i + 1) + ')').css('display', disp);
                    });
                }
            }
        }

        function _dApplyCustomCols() {
            if (_destsTable) {
                var total = _destsTable.columns().count();
                for (var i = 0; i < total; i++) {
                    var visible = _dCustomCols.indexOf(i) !== -1;
                    if (i === 1) visible = true;
                    _destsTable.column(i).visible(visible, false);
                }
                _destsTable.columns.adjust();
            }
            if (_isSplit) {
                for (var i = 0; i < 10; i++) {
                    var visible = _dCustomCols.indexOf(i) !== -1;
                    if (i === 1) visible = true;
                    var disp = visible ? '' : 'none';
                    ['#destTableL', '#destTableR'].forEach(function(sel) {
                        $(sel + ' thead tr th:nth-child(' + (i + 1) + ')').css('display', disp);
                        $(sel + ' tbody tr td:nth-child(' + (i + 1) + ')').css('display', disp);
                    });
                }
            }
        }

        function _dSyncCustomChkBoxes() {
            document.querySelectorAll('.d-custom-col-chk').forEach(function(chk) {
                chk.checked = _dCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }

        document.querySelectorAll('.d-custom-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _dCustomCols.indexOf(col);
                if (this.checked && idx === -1) _dCustomCols.push(col);
                else if (!this.checked && idx !== -1) _dCustomCols.splice(idx, 1);
                try { localStorage.setItem(_D_CUSTOM_COL_KEY, JSON.stringify(_dCustomCols)); } catch(e) {}
                if (_dColMode === 'custom') _dApplyCustomCols();
            });
        });

        function _dApplyResponsiveCols() {
            if (_dColMode !== 'auto') return;
            if (!_destsTable && !_isSplit) return;
            var w;
            if (_isSplit) {
                // Measure the actual rendered width of the split wrapper so the
                // sidebar, page padding and inter-table gap are automatically
                // accounted for.  Fall back to half the viewport if not yet in DOM.
                var wrap = document.getElementById('destSplitWrap');
                w = wrap ? Math.floor(wrap.offsetWidth / 2) : Math.floor(window.innerWidth / 2);
            } else {
                w = window.innerWidth;
            }
            if (w < 768) {
                _dShowCols(_dSMALL_HIDE);
            } else if (w < 992) {
                _dShowCols(_dCOMPACT_HIDE);
            } else {
                _dShowCols([]);
            }
        }

        _dApplyMode = function(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#dColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            if (mode === 'auto') {
                $('#dColModeBtn').removeClass('btn-primary').addClass('btn-outline-secondary');
            } else {
                $('#dColModeBtn').removeClass('btn-outline-secondary').addClass('btn-primary');
            }
            document.getElementById('dCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            $('#dColModeBtn').closest('.dropdown').find('.dropdown-item').each(function(){
                $(this).find('i.bi-check').remove();
                if ($(this).data('dcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            if (mode === 'auto') {
                _dApplyResponsiveCols();
            } else if (mode === 'all') {
                _dShowCols([]);
            } else if (mode === 'compact') {
                _dShowCols(_dCOMPACT_HIDE);
            } else if (mode === 'custom') {
                _dSyncCustomChkBoxes();
                _dApplyCustomCols();
            }
        };

        $(window).on('resize', function(){ _dApplyResponsiveCols(); });
        _dApplyMode(_dColMode);

        $('#dColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e){
            e.preventDefault();
            var mode = $(this).data('dcol-mode');
            if (!mode) return;
            _dColMode = mode;
            try { localStorage.setItem(_D_COL_MODE_KEY, mode); } catch(e) {}
            _dApplyMode(mode);
        });

        $('#destinationSearch').on('keydown', function(e) {
            if (e.key === 'Enter') { e.preventDefault(); destsTableReload(); }
        });
        $('#destinationSearchForm button[type="submit"]').on('click', function(e) {
            e.preventDefault(); destsTableReload();
        });
        if (localStorage.getItem('destLayout') === 'split') {
            window.toggleDestLayout();
        }
        _enforceSingleIfNarrow(); // force single if window already too narrow on load
    });
})();
</script>
