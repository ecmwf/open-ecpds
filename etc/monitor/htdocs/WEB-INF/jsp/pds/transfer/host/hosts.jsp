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
                        <div class="col-auto">
                            <div class="input-group flex-nowrap" style="width:auto" title="Page size">
                                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                                <select id="hostPageLen" class="form-select" style="width:auto">
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
                                <input class="form-control" name="hostSearch" id="hostSearch" type="text"
                                    placeholder="e.g. enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i"
                                    title="Default search is by nickname. Use id, hostname, login, password, nickname, comment, dir, enabled, method, email and options rules."
                                    value='<c:out value="${hostSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-12 col-sm-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="hostType" id="hostType" onchange="hostsTableReload()" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${hostType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-auto d-flex gap-1">
                            <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i><span class="d-none d-sm-inline ms-1">Search</span></button>
                            <button type="button" class="btn btn-outline-primary position-relative"
                                    id="btnHostQB"
                                    onclick="toggleQBPanel('hostQueryBuilder','btnHostQB')"
                                    title="Filter">
                                <i class="bi bi-sliders2"></i><span class="d-none d-sm-inline ms-1">Filter</span>
                                <span id="btnHostQB-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;font-size:0.75rem;min-width:1.3em;padding:0.2em 0.4em;line-height:1.2"></span>
                            </button>
                            <button class="btn btn-link btn-sm text-muted p-0" type="button"
                                    data-bs-toggle="collapse" data-bs-target="#hstQBHelp"
                                    aria-expanded="false" title="Search syntax help">
                                <i class="bi bi-info-circle"></i>
                            </button>
                        </div>
                    </div>
                    <div class="collapse mt-1" id="hstQBHelp">
                        <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
                            <strong class="d-block mb-1">Search &amp; Filter syntax</strong>
                            <p class="mb-1">Type directly in the search box or click <i class="bi bi-sliders2"></i> <strong>Filter</strong> to use the visual query builder. Terms can be combined freely.</p>
                            <ul class="mb-1 ps-3">
                                <li><strong>Default (no prefix)</strong> &mdash; matches the host <code>nickname</code>. Wildcards <code>*</code> and <code>?</code> are supported.</li>
                                <li><code>nickname=Test_0?</code>, <code>hostname=*.test.fr</code>, <code>method=*Http</code> &mdash; text filters with wildcards.</li>
                                <li><code>comment=</code>, <code>email=</code>, <code>options=</code> &mdash; other text filters with wildcards.</li>
                                <li><code>login=</code>, <code>dir=</code> &mdash; login user and remote directory.</li>
                                <li><code>id=100</code>, <code>id&gt;=50</code> &mdash; filter by host ID (supports <code>=</code> <code>&gt;</code> <code>&gt;=</code> <code>&lt;</code> <code>&lt;=</code>).</li>
                                <li><code>enabled=yes|no</code> &mdash; filter by enabled state.</li>
                                <li><code>case=i</code> &mdash; make the search case-insensitive (default is case-sensitive).</li>
                                <li><i class="bi bi-sliders2 me-1"></i><strong>Filter panel</strong> also provides <strong>Group</strong>, <strong>Label</strong> and <strong>Compression</strong> drop-downs that apply independently of the search text.</li>
                            </ul>
                            <p class="mb-0 text-muted">Example: <code>hostname=*.ecmwf.int method=*Ftp enabled=yes case=i</code></p>
                        </div>
                    </div>
                    <%-- Query Builder panel --%>
                    <div id="hostQueryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>nickname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_nickname" placeholder="e.g. Test_0?" oninput="hqbPreview()" list="hqb_nickname_list" autocomplete="off">
                                    <datalist id="hqb_nickname_list">
                                        <c:forEach var="n" items="${hostNickNames}">
                                            <option value="${n}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>hostname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_hostname" placeholder="e.g. *.test.fr" oninput="hqbPreview()" list="hqb_hostname_list" autocomplete="off">
                                    <datalist id="hqb_hostname_list">
                                        <c:forEach var="h" items="${hostHostNames}">
                                            <option value="${h}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-6 col-md-4">
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
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>id</code> <span class="text-muted fw-normal">numeric</span></label>
                                    <div class="input-group input-group-sm">
                                        <select class="form-select form-select-sm" id="hqb_id_op" style="max-width:65px" onchange="hqbPreview()">
                                            <option value="=">=</option><option value=">=">&gt;=</option><option value=">">&gt;</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="hqb_id_val" placeholder="e.g. 100" oninput="hqbPreview()">
                                    </div>
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>login=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_login" oninput="hqbPreview()">
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>dir=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_dir" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_comment" placeholder="e.g. *test*" oninput="hqbPreview()">
                                </div>
                                <div class="col-6 col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_email" placeholder="e.g. *@domain.com" oninput="hqbPreview()">
                                </div>
                                <div class="col-6 col-md-4">
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
                            <%-- Direct filters (not part of search string, applied immediately) --%>
                            <div class="border-top mt-1 pt-1">
                                <div class="row g-1 mb-1">
                                    <div class="col-12 col-md-4">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-diagram-3 me-1 text-muted"></i>Group</label>
                                        <select class="form-select form-select-sm" name="network" id="network" onchange="hostsTableReload();hqbUpdateBadge();" title="Filter by Group">
                                            <c:forEach var="option" items="${networkOptions}">
                                                <option value="${option.name}" <c:if test="${network == option.name}">selected</c:if>>${option.value}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-4">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-bookmark me-1 text-muted"></i>Label</label>
                                        <select class="form-select form-select-sm" name="label" id="label" onchange="hostsTableReload();hqbUpdateBadge();" title="Filter by Label">
                                            <c:forEach var="option" items="${labelOptions}">
                                                <option value="${option.name}" <c:if test="${label == option.name}">selected</c:if>>${option.value}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-12 col-md-4">
                                        <label class="form-label mb-0 fw-semibold"><i class="bi bi-file-zip me-1 text-muted"></i>Compression</label>
                                        <select class="form-select form-select-sm" name="hostFilter" id="hostFilter" onchange="hostsTableReload();hqbUpdateBadge();" title="Filter by Compression">
                                            <c:forEach var="option" items="${filterOptions}">
                                                <option value="${option.name}" <c:if test="${hostFilter == option.name}">selected</c:if>>${option.value}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1 flex-wrap">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all;min-width:0" id="hqb_preview">-- fill in fields above --</code>
                                <div class="d-flex gap-1 flex-shrink-0">
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
            </div>
        </form>
        <script>
        function hqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function hqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function hqbCountActive() {
            var n = 0;
            ['nickname','hostname','method','comment','email','options','login','dir','id_val'].forEach(function(f) { if (hqbVal('hqb_'+f)) n++; });
            if (hqbVal('hqb_enabled')) n++;
            if (hqbVal('hqb_case') !== 's') n++;
            var nw = document.getElementById('network'); if (nw && nw.selectedIndex > 0) n++;
            var lb = document.getElementById('label'); if (lb && lb.selectedIndex > 0) n++;
            var hf = document.getElementById('hostFilter'); if (hf && hf.selectedIndex > 0) n++;
            return n;
        }
        function hqbUpdateBadge() {
            var n = hqbCountActive();
            var b = document.getElementById('btnHostQB-badge');
            if (b) { b.textContent = n; b.style.display = n > 0 ? '' : 'none'; }
            var btn = document.getElementById('btnHostQB');
            if (btn) { btn.classList.toggle('btn-outline-primary', n === 0); btn.classList.toggle('btn-warning', n > 0); }
        }
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
            hqbUpdateBadge();
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
            document.getElementById('network').selectedIndex = 0;
            document.getElementById('label').selectedIndex = 0;
            document.getElementById('hostFilter').selectedIndex = 0;
            document.getElementById('hostSearch').value = '';
            document.getElementById('hostQueryBuilder').style.display = 'none';
            hqbPreview();
            hostsTableReload();
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
            var vw = window.innerWidth || document.documentElement.clientWidth;
            var margin = 8;
            var pw = Math.min(740, vw - 2 * margin);
            var left = Math.max(sx + margin, Math.min(r.right + sx - pw, sx + vw - pw - margin));
            panel.style.top = (r.bottom + sy + 4) + 'px';
            panel.style.left = left + 'px';
            panel.style.width = pw + 'px';
            panel.style.right = 'auto';
            panel.style.overflowX = 'auto';
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

<%-- Search error/empty-state banner - content managed dynamically by drawCallback --%>
<div id="hostSearchError" class="alert" style="display:none"></div>
<script>
var _hostSearchHelp = '<p class="mb-1 mt-2">You can conduct an extended search using the following rules:<\/p>' +
    '<ul class="mb-0">' +
    '<li><code>id<\/code>, <code>hostname=<\/code>, <code>login=<\/code>, <code>password=<\/code>, <code>nickname=<\/code>, <code>comment=<\/code>, <code>dir=<\/code>, <code>enabled=yes\/no<\/code>, <code>method=<\/code>, <code>email=<\/code>, <code>options=<\/code><\/li>' +
    '<li>Example: <code>enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i<\/code><\/li>' +
    '<li><code>case=i<\/code> for case-insensitive, <code>case=s<\/code> for case-sensitive (default)<\/li>' +
    '<li>Enclose values with spaces or equals signs in double quotes, e.g. <code>&quot;United States&quot;<\/code> or <code>&quot;a=b&quot;<\/code><\/li>' +
    '<li>Wildcards: <code>*<\/code> (zero or more chars), <code>?<\/code> (exactly one char)<\/li>' +
    '<\/ul>' +
    '<div class="mt-2 text-muted small"><i class="bi bi-lightbulb"><\/i> Tip: Not sure about the syntax? Use the <a href="#" onclick="event.stopPropagation(); toggleQBPanel(\'hostQueryBuilder\',\'btnHostQB\'); document.getElementById(\'btnHostQB\').scrollIntoView({behavior:\'smooth\',block:\'center\'}); return false;" class="link-secondary"><i class="bi bi-sliders2"><\/i> query builder<\/a> above to build a valid search expression.<\/div>';
function _updateHostSearchBanner(queryError, total, hasSearch) {
    var div = document.getElementById('hostSearchError');
    if (!div) return;
    function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
    if (queryError) {
        div.innerHTML = '<strong>Error in your query:<\/strong> ' + esc(queryError) + _hostSearchHelp;
        div.style.display = '';
    } else if (total === 0 && hasSearch) {
        div.innerHTML = 'No Hosts found. Default search is by nickname.' + _hostSearchHelp;
        div.style.display = '';
    } else if (total === 0) {
        div.textContent = 'No Hosts found based on these criteria.';
        div.style.display = '';
    } else {
        div.style.display = 'none';
    }
}
<c:if test="${empty hosts}">_updateHostSearchBanner('<c:out value="${getHostsError}"/>', 0, ${hasHostSearch});</c:if>
</script>

<%-- Results table --%>
<div class="d-flex align-items-start mb-2 gap-2 flex-wrap-reverse">
    <span class="text-muted small" id="hostsFoundLabel"><i class="bi bi-list-ul"></i> Loading...</span>
    <div class="ms-auto d-flex gap-2 align-items-center flex-wrap">
        <auth:if basePathKey="host.basepath" paths="/edit/insert_form">
        <auth:then>
            <a href='<bean:message key="host.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new host">
                <i class="bi bi-plus-circle"></i> Create
            </a>
        </auth:then>
        </auth:if>
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="hColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="hColModeBtn">
                <li><a class="dropdown-item active" href="#" data-hcol-mode="auto"><i class="bi bi-check me-1"></i><strong>Auto</strong><br><small class="text-muted">Hides columns based on screen width</small></a></li>
                <li><a class="dropdown-item" href="#" data-hcol-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
                <li><a class="dropdown-item" href="#" data-hcol-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Transfer Group, Network</small></a></li>
                <li><a class="dropdown-item" href="#" data-hcol-mode="small"><strong>Small</strong><br><small class="text-muted">Shows Icon and Host only</small></a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-hcol-mode="custom">
                  <strong>Custom</strong><br><small class="text-muted">Choose individual columns</small>
                </a></li>
                <li id="hCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-0" data-col="0" checked><label class="form-check-label" for="hchk-col-0">Icon</label></div>
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-1" data-col="1" checked disabled><label class="form-check-label text-muted" for="hchk-col-1">Host <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-2" data-col="2" checked><label class="form-check-label" for="hchk-col-2">Hostname/IP</label></div>
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-3" data-col="3" checked><label class="form-check-label" for="hchk-col-3">Transfer Group</label></div>
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-4" data-col="4" checked><label class="form-check-label" for="hchk-col-4">Network</label></div>
                    <div class="form-check mb-0"><input class="form-check-input h-custom-col-chk" type="checkbox" id="hchk-col-5" data-col="5" checked><label class="form-check-label" for="hchk-col-5">Destinations</label></div>
                  </div>
                </li>
            </ul>
        </div>
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
        // Suppress DataTables' native alert() for server errors; drawCallback shows them inline.
        $.fn.dataTable.ext.errMode = function () {};
        _hostsTable = $('#hostsTable').DataTable({
            serverSide: true,
            processing: true,
            ajax: {
                url: '/do/transfer/host/list',
                type: 'GET',
                data: function(d) {
                    d.label      = $('#label').val() || 'All';
                    d.hostFilter = $('#hostFilter').val() || 'All';
                    d.network    = $('#network').val() || 'All';
                    d.hostType   = $('#hostType').val() || 'All';
                    d.hostSearch = $('#hostSearch').val() || '';
                }
            },
            pageLength: (function() { try { var v = parseInt(localStorage.getItem('hostsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
            lengthChange: false,
            searching: false, autoWidth: false,
            order: [],
            columns: [
                { orderable: false, data: 0, width: '36px', className: 'icon-col' },
                { orderable: true,  data: 1 },
                { orderable: true,  data: 2 },
                { orderable: true,  data: 3 },
                { orderable: true,  data: 4 },
                { orderable: true,  data: 5 }
            ],
            columnDefs: [{ targets: '_all', render: function(data, type) { return data; } }],
            drawCallback: function(settings) {
                var json = settings.json || {};
                var total = json.recordsTotal || 0;
                var srch = ($('#hostSearch').val() || '').trim();
                _updateHostSearchBanner(json.queryError || '', total, srch.length > 0);
                $('#hostsFoundLabel').html('<i class="bi bi-list-ul"></i> <strong>' + total + '</strong> host(s) found');
            },
            language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_', processing: 'Loading...' }
        });
        $('#hostPageLen').val(_hostsTable.page.len());
        $('#hostPageLen').on('change', function() {
            var len = parseInt(this.value, 10);
            try { localStorage.setItem('hostsPageLen', len); } catch(e) {}
            _hostsTable.page.len(len).draw();
        });
        var _H_CUSTOM_COL_KEY = 'hostsCustomCols';
        var _H_COL_MODE_KEY   = 'hostsColMode';
        var _hCustomCols = (function() {
            try { var s = localStorage.getItem(_H_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
            return [0,1,2,3,4,5];
        })();
        var _hColMode = (function() {
            try { return localStorage.getItem(_H_COL_MODE_KEY) || 'auto'; } catch(e) { return 'auto'; }
        })();
        // Compact: hide Transfer Group(3) + Network(4)
        var _hCOMPACT_HIDE = [3, 4];
        // Small: hide Hostname/IP(2) + Transfer Group(3) + Network(4) + Destinations(5)
        var _hSMALL_HIDE = [2, 3, 4, 5];
        // Auto medium (<992px): hide Transfer Group(3) + Network(4) + Destinations(5)
        var _hMED_COLS = [3, 4, 5];
        // Auto small (<768px): also hide Hostname/IP(2)
        var _hSM_COLS = [2];

        function _hShowCols(hideCols) {
            var total = _hostsTable.columns().count();
            for (var i = 0; i < total; i++) {
                _hostsTable.column(i).visible(hideCols.indexOf(i) === -1, false);
            }
            _hostsTable.columns.adjust();
        }

        function _hApplyCustomCols() {
            var total = _hostsTable.columns().count();
            for (var i = 0; i < total; i++) {
                var visible = _hCustomCols.indexOf(i) !== -1;
                if (i === 1) visible = true; // Host is mandatory
                _hostsTable.column(i).visible(visible, false);
            }
            _hostsTable.columns.adjust();
        }

        function _hSyncCustomChkBoxes() {
            document.querySelectorAll('.h-custom-col-chk').forEach(function(chk) {
                chk.checked = _hCustomCols.indexOf(+chk.dataset.col) !== -1;
            });
        }

        document.querySelectorAll('.h-custom-col-chk').forEach(function(chk) {
            chk.addEventListener('change', function() {
                var col = +this.dataset.col;
                var idx = _hCustomCols.indexOf(col);
                if (this.checked && idx === -1) _hCustomCols.push(col);
                else if (!this.checked && idx !== -1) _hCustomCols.splice(idx, 1);
                try { localStorage.setItem(_H_CUSTOM_COL_KEY, JSON.stringify(_hCustomCols)); } catch(e) {}
                if (_hColMode === 'custom') _hApplyCustomCols();
            });
        });

        function _hApplyResponsiveCols() {
            if (_hColMode !== 'auto') return;
            var w = window.innerWidth;
            if (w < 768) {
                _hShowCols(_hMED_COLS.concat(_hSM_COLS));
            } else if (w < 992) {
                _hShowCols(_hMED_COLS);
            } else {
                _hShowCols([]);
            }
        }

        function _hApplyMode(mode) {
            var label = mode.charAt(0).toUpperCase() + mode.slice(1);
            $('#hColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
            if (mode === 'auto') {
                $('#hColModeBtn').removeClass('btn-primary').addClass('btn-outline-secondary');
            } else {
                $('#hColModeBtn').removeClass('btn-outline-secondary').addClass('btn-primary');
            }
            document.getElementById('hCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
            $('#hColModeBtn').closest('.dropdown').find('.dropdown-item').each(function(){
                $(this).find('i.bi-check').remove();
                if ($(this).data('hcol-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
            });
            if (mode === 'auto') {
                _hApplyResponsiveCols();
            } else if (mode === 'all') {
                _hShowCols([]);
            } else if (mode === 'compact') {
                _hShowCols(_hCOMPACT_HIDE);
            } else if (mode === 'small') {
                _hShowCols(_hSMALL_HIDE);
            } else if (mode === 'custom') {
                _hSyncCustomChkBoxes();
                _hApplyCustomCols();
            }
        }

        $(window).on('resize', function(){ _hApplyResponsiveCols(); });
        _hApplyMode(_hColMode);

        $('#hColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function(e){
            e.preventDefault();
            var mode = $(this).data('hcol-mode');
            if (!mode) return;
            _hColMode = mode;
            try { localStorage.setItem(_H_COL_MODE_KEY, mode); } catch(e) {}
            _hApplyMode(mode);
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

<link rel="stylesheet" href="/openlayer/ol.css"/>
<style>
[data-bs-theme=dark] #hostMap .host-base-layer canvas {
    filter: saturate(2) brightness(0.5) contrast(1.15);
}
/* OL zoom buttons: flex centering for +/−/fit icons */
.ol-zoom button {
    display: flex !important; align-items: center !important;
    justify-content: center !important; line-height: 1 !important;
}
/* Fit icon: match visual weight of +/− */
#hostFitBtn i { font-size: 1em; -webkit-text-stroke: 0.4px currentColor; }
/* Attribution 'i' button: breathing room from map edges */
#hostMap .ol-attribution { bottom: 8px; right: 8px; }
</style>
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
        $('#hColModeBtn').closest('.dropdown').toggle(isList);
        if (!isList) {
            if (!mapInitDone) { initMap(); mapInitDone = true; }
            else { loadMapFeatures(); }
            // Trigger OL resize since container was hidden during init
            setTimeout(function() { if (olMap) olMap.updateSize(); }, 50);
        } else {
            // Reload list so it reflects any filters changed while in map view
            if (typeof hostsTableReload === 'function') hostsTableReload();
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

    function clusterStyle(size, hover) {
        return new ol.style.Style({
            image: new ol.style.Circle({
                radius: hover ? 16 : 14,
                fill: new ol.style.Fill({ color: hover ? 'rgba(13,110,253,1)' : 'rgba(13,110,253,0.85)' }),
                stroke: new ol.style.Stroke({ color: '#fff', width: 2 })
            }),
            text: new ol.style.Text({
                text: String(size),
                fill: new ol.style.Fill({ color: '#fff' }),
                font: 'bold 11px sans-serif'
            })
        });
    }

    // ---- Map init ----
    function initMap() {
        olSource = new ol.source.Vector();

        var clusterSource = new ol.source.Cluster({ distance: 25, source: olSource });

        var layer = new ol.layer.Vector({
            source: clusterSource,
            style: function(f) {
                var sub = f.get('features');
                return sub.length === 1 ? styleFor(sub[0], false) : clusterStyle(sub.length, false);
            }
        });

        olMap = new ol.Map({
            target: 'hostMap',
            controls: ol.control.defaults.defaults({
                rotate: false,
                attribution: false
            }).extend([
                new ol.control.Attribution({ collapsible: true, collapsed: true })
            ]),
            layers: [
                new ol.layer.Tile({ source: new ol.source.OSM(), className: 'host-base-layer' }),
                layer
            ],
            view: new ol.View({ center: ol.proj.fromLonLat([10, 48]), zoom: 3 })
        });

        /* Inject fit button into OL zoom control */
        var zoomCtrl = olMap.getTargetElement().querySelector('.ol-zoom');
        if (zoomCtrl) {
            var fitBtn = document.createElement('button');
            fitBtn.id = 'hostFitBtn';
            fitBtn.type = 'button';
            fitBtn.title = 'Fit map to all hosts';
            fitBtn.style.display = 'none';
            fitBtn.innerHTML = '<i class="bi bi-arrows-fullscreen"></i>';
            fitBtn.onclick = function() {
                var ext = olSource.getExtent();
                if (ext && !ol.extent.isEmpty(ext)) {
                    olMap.getView().fit(ext, { padding: [40, 40, 40, 40], maxZoom: 7, duration: 300 });
                }
            };
            zoomCtrl.appendChild(fitBtn);
        }

        // Initial load with current filters
        loadMapFeatures();

        // Hover
        var hovered = null;
        olMap.on('pointermove', function(evt) {
            var f = olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return f; });
            olMap.getTargetElement().style.cursor = f ? 'pointer' : '';
            if (hovered && hovered !== f) { hovered.setStyle(null); hovered = null; }
            if (f && f !== hovered) {
                var sub = f.get('features');
                f.setStyle(sub.length === 1 ? styleFor(sub[0], true) : clusterStyle(sub.length, true));
                hovered = f;
            }
        });

        // Click -> offcanvas
        olMap.on('click', function(evt) {
            var f = olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return f; });
            if (!f) return;
            var sub = f.get('features');
            if (sub.length === 1) {
                showHostPanel(sub[0].getProperties());
            } else {
                showHostListPanel(sub.map(function(feat) { return feat.getProperties(); }));
            }
        });
    }

    // ---- Server-side filtered fetch ----
    function getFormVal(name) {
        // Use getElementById first — the filter panel may be detached from #hostSearchForm
        // (moved to document.body by toggleQBPanel), so a form-scoped querySelector would miss it.
        var el = document.getElementById(name)
               || document.querySelector('#hostSearchForm [name="' + name + '"]');
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
        /* Keep the toolbar "X host(s) found" label in sync with the map filters */
        if (typeof hostsTableReload === 'function') hostsTableReload();
        fetch(buildMapUrl())
            .then(function(r) { return r.json(); })
            .then(function(geojson) {
                if (geojson.queryError) {
                    document.getElementById('mapFoundLabel').innerHTML =
                        '<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>'
                        + '<strong>Error in your query:</strong> ' + esc(geojson.queryError) + '</span>';
                    olSource.clear(true);
                    return;
                }
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
                    var fb = document.getElementById('hostFitBtn');
                    if (fb) fb.style.display = '';
                } else {
                    var fb = document.getElementById('hostFitBtn');
                    if (fb) fb.style.display = 'none';
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

    function _showOffcanvas(title, body) {
        document.getElementById('hostDetailTitle').innerHTML = title;
        document.getElementById('hostDetailBody').innerHTML = body;
        if (!_offcanvas) {
            _offcanvas = new bootstrap.Offcanvas(document.getElementById('hostDetailPanel'));
        }
        _offcanvas.show();
    }

    function showHostListPanel(hosts) {
        var typeColors = { Dissemination:'primary', Acquisition:'success', Source:'warning' };
        var html = '<div class="list-group list-group-flush">';
        hosts.forEach(function(p) {
            var activeBadge = p.active
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-secondary">Inactive</span>';
            var tc = typeColors[p.type] || 'secondary';
            html += '<a href="' + p.url + '" class="list-group-item list-group-item-action px-3 py-2">'
                + '<div class="d-flex justify-content-between align-items-start gap-2">'
                + '<span class="fw-semibold">' + esc(p.nickname) + '</span>'
                + '<span class="text-nowrap">' + activeBadge
                + '<span class="badge bg-' + tc + ' ms-1">' + esc(p.type) + '</span></span>'
                + '</div>'
                + (p.hostname ? '<div class="small text-muted">' + esc(p.hostname) + '</div>' : '')
                + '</a>';
        });
        html += '</div>';
        _showOffcanvas(
            '<i class="bi bi-hdd-network me-1"></i>' + hosts.length + ' hosts at this location',
            html
        );
    }

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
            + row('Hostname', esc(p.hostname) || '<span class="text-muted">&mdash;</span>')
            + row('Network',  esc(p.network)  || '<span class="text-muted">&mdash;</span>')
            + row('Method',   esc(p.method)   || '<span class="text-muted">&mdash;</span>')
            + row('Location', esc(p.geo)      || '<span class="text-muted">&mdash;</span>')
            + (p.comment ? row('Comment', '<span class="text-muted small">' + esc(p.comment) + '</span>') : '')
            + '</table>'
            + '<div class="mt-3"><a href="' + p.url + '" class="btn btn-sm btn-outline-primary w-100">'
            + '<i class="bi bi-arrow-right-circle me-1"></i>Open host page</a></div>';

        _showOffcanvas(
            '<i class="bi bi-hdd-network me-1"></i>' + esc(p.nickname || p.id),
            html
        );
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
        // Search form submit in map mode -> immediate reload
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
