<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>

<div class="card border-0 shadow-sm mb-1">
    <div class="card-body py-2">

        <%-- Dissemination Stream row (only if not empty) --%>
        <c:if test="${not empty disseminationStreamOptionsWithSizes}">
        <div class="filter-row mb-1">
            <span class="filter-label">Dissem. Stream</span>
            <c:set var="disseminationStreamNow" value="${destinationDetailActionForm.disseminationStream}"/>
            <c:forEach var="disseminationStream" items="${disseminationStreamOptionsWithSizes}">
                <a href="javascript:setDisseminationStream('${disseminationStream.name}');"
                   title="Size: ${disseminationStream.formattedSize}"
                   class="badge rounded-pill border fw-normal text-decoration-none <c:choose><c:when test="${disseminationStream.name == disseminationStreamNow}">bg-primary text-white border-primary</c:when><c:otherwise>bg-body-tertiary text-body</c:otherwise></c:choose>">
                    ${disseminationStream.name} <span class="opacity-75">(${disseminationStream.value})</span>
                </a>
            </c:forEach>
        </div>
        </c:if>

        <%-- Data Stream row (only if not empty) --%>
        <c:if test="${not empty dataStreamOptionsWithSizes}">
        <div class="filter-row mb-1">
            <span class="filter-label">Data Stream</span>
            <c:set var="dataStreamNow" value="${destinationDetailActionForm.dataStream}"/>
            <c:forEach var="dataStream" items="${dataStreamOptionsWithSizes}">
                <a href="javascript:setDataStream('${dataStream.second}');"
                   title="Size: ${dataStream.formattedSize}"
                   class="badge rounded-pill border fw-normal text-decoration-none <c:choose><c:when test="${dataStream.second == dataStreamNow}">bg-primary text-white border-primary</c:when><c:otherwise>bg-body-tertiary text-body</c:otherwise></c:choose>">
                    ${dataStream.first} <span class="opacity-75">(${dataStream.third})</span>
                </a>
            </c:forEach>
        </div>
        </c:if>

        <%-- Base Time row (only if not empty) --%>
        <c:if test="${not empty dataTimeOptionsWithSizes}">
        <div class="filter-row mb-1">
            <span class="filter-label">Base Time</span>
            <c:set var="dataTimeNow" value="${destinationDetailActionForm.dataTime}"/>
            <c:forEach var="dataTime" items="${dataTimeOptionsWithSizes}">
                <a href="javascript:setDataTime('${dataTime.second}');"
                   title="Size: ${dataTime.formattedSize}"
                   class="badge rounded-pill border fw-normal text-decoration-none <c:choose><c:when test="${dataTime.first == dataTimeNow}">bg-primary text-white border-primary</c:when><c:otherwise>bg-body-tertiary text-body</c:otherwise></c:choose>">
                    ${dataTime.first} <span class="opacity-75">(${dataTime.third})</span>
                </a>
            </c:forEach>
        </div>
        </c:if>

        <%-- Status row (always shown) --%>
        <div class="filter-row mb-1">
            <span class="filter-label">Status</span>
            <c:set var="statusNow" value="${destinationDetailActionForm.status}"/>
            <c:forEach var="status" items="${statusOptionsWithSizes}">
                <c:choose>
                    <c:when test="${status.first == 'DONE'}"><c:set var="sPillColor" value="success"/><c:set var="sPillTxt" value="text-white"/></c:when>
                    <c:when test="${status.first == 'EXEC' or status.first == 'FETC' or status.first == 'INIT'}"><c:set var="sPillColor" value="primary"/><c:set var="sPillTxt" value="text-white"/></c:when>
                    <c:when test="${status.first == 'FAIL'}"><c:set var="sPillColor" value="danger"/><c:set var="sPillTxt" value="text-white"/></c:when>
                    <c:when test="${status.first == 'RETR' or status.first == 'WAIT' or status.first == 'SCHE' or status.first == 'HOLD'}"><c:set var="sPillColor" value="warning"/><c:set var="sPillTxt" value="text-dark"/></c:when>
                    <c:otherwise><c:set var="sPillColor" value="secondary"/><c:set var="sPillTxt" value="text-white"/></c:otherwise>
                </c:choose>
                <a href="javascript:setStatus('${status.first}')"
                   title="Size: ${status.formattedSize}"
                   class="badge rounded-pill fw-normal text-decoration-none border <c:choose><c:when test="${status.first == statusNow}">bg-${sPillColor} ${sPillTxt} border-${sPillColor}</c:when><c:otherwise>bg-${sPillColor}-subtle text-${sPillColor}-emphasis border-${sPillColor}-subtle</c:otherwise></c:choose>">
                    ${status.second} <span class="opacity-75">(${status.third})</span>
                </a>
            </c:forEach>
        </div>

        <%-- Product Date row (always shown) --%>
        <div class="filter-row mb-2">
            <span class="filter-label">Prod. Date</span>
            <c:set var="dateNow" value="${destinationDetailActionForm.date}"/>
            <c:forEach var="date" items="${dateOptions}">
                <a href="javascript:setDate('${date.name}')"
                   class="badge rounded-pill border fw-normal text-decoration-none <c:choose><c:when test="${date.name == dateNow}">bg-primary text-white border-primary</c:when><c:otherwise>bg-body-tertiary text-body</c:otherwise></c:choose>">
                    ${date.value}
                </a>
            </c:forEach>
        </div>

        <%-- Search + Filter button --%>
        <div class="d-flex gap-1 align-items-center">
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
            <div class="input-group" style="flex:1">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" value="${destinationDetailActionForm.fileNameSearch}"
                       placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i"
                       class="form-control"
                       title="Default search is by target. Conduct extended searches using target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules. Wildcards: * (zero or more chars), ? (exactly one char)."
                       id="fileNameSearch" name="fileNameSearch" onkeypress="submitenter(this,event)">
            </div>
            <button type="button" class="btn btn-outline-primary"
                    id="btnDftQB"
                    onclick="toggleQBPanel('dftQueryBuilder','btnDftQB')"
                    title="Filter">
                <i class="bi bi-sliders2"></i><span class="d-none d-sm-inline ms-1">Filter</span>
            </button>
            <button class="btn btn-link text-muted p-0" type="button"
                    data-bs-toggle="collapse" data-bs-target="#dftQBHelp"
                    aria-expanded="false" title="Search syntax help">
                <i class="bi bi-info-circle"></i>
            </button>
        </div>
        <div class="collapse mt-1" id="dftQBHelp">
            <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
                <strong class="d-block mb-1">Search &amp; Filter syntax</strong>
                <p class="mb-1">Type directly in the search box or click <i class="bi bi-sliders2"></i> <strong>Filter</strong> to open the visual query builder. Terms can be combined freely in any order.</p>
                <ul class="mb-1 ps-3">
                    <li><strong>Default (no prefix)</strong> &mdash; matches the <code>target</code> filename. Wildcards <code>*</code> (any chars) and <code>?</code> (one char) are supported.</li>
                    <li><code>target=*.dat</code> &mdash; filter by target filename.</li>
                    <li><code>source=/tmp/*</code> &mdash; filter by source path.</li>
                    <li><code>mover=</code> &mdash; filter by Data Mover name.</li>
                    <li><code>ts&gt;10 ts&lt;=99</code> &mdash; filter by transfer size (numeric range, supports <code>=</code> <code>&gt;</code> <code>&gt;=</code> <code>&lt;</code> <code>&lt;=</code>).</li>
                    <li><code>size&gt;=700kb</code> &mdash; filter by file size; units: <code>b</code>, <code>kb</code>, <code>mb</code>, <code>gb</code>.</li>
                    <li><code>priority=</code> &mdash; filter by transfer priority (0&ndash;99).</li>
                    <li><code>identity=</code>, <code>groupby=</code>, <code>checksum=</code> &mdash; other metadata filters.</li>
                    <li><code>asap=yes|no</code>, <code>deleted=yes|no</code>, <code>expired=yes|no</code>, <code>replicated=yes|no</code>, <code>proxy=yes|no</code>, <code>event=yes|no</code> &mdash; boolean flags.</li>
                    <li><code>case=i</code> &mdash; make the search case-insensitive (default is case-sensitive).</li>
                </ul>
                <p class="mb-0 text-muted">Example: <code>target=*.bufr expired=no size&gt;=1mb case=i</code></p>
            </div>
        </div>

    </div>
</div>

<div id="dftQueryBuilder" class="border rounded p-2"
         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
            <%-- Row 1: text fields -- align-items-end so inputs line up despite different label heights --%>
            <div class="row g-1 mb-1 align-items-end">
                <div class="col-6 col-md-3">
                    <label class="form-label mb-0 fw-semibold"><code>target=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                    <input type="text" class="form-control form-control-sm" id="dft_target" placeholder="e.g. *.dat" oninput="dftPreview()">
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label mb-0 fw-semibold"><code>source=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                    <input type="text" class="form-control form-control-sm" id="dft_source" placeholder="e.g. /tmp/*" oninput="dftPreview()">
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label mb-0 fw-semibold"><code>mover=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_mover" oninput="dftPreview()" list="dft_mover_list" autocomplete="off">
                    <datalist id="dft_mover_list">
                        <c:forEach var="m" items="${transferServerNames}">
                            <option value="${m}">
                        </c:forEach>
                    </datalist>
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label mb-0 fw-semibold"><code>checksum=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_checksum" oninput="dftPreview()">
                </div>
            </div>
            <%-- Row 2: text fields cont. --%>
            <div class="row g-1 mb-1">
                <div class="col-6 col-md-4">
                    <label class="form-label mb-0 fw-semibold"><code>identity=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_identity" oninput="dftPreview()">
                </div>
                <div class="col-6 col-md-4">
                    <label class="form-label mb-0 fw-semibold"><code>groupby=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_groupby" oninput="dftPreview()">
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label mb-0 fw-semibold"><code>priority=</code></label>
                    <input type="number" class="form-control form-control-sm" id="dft_priority" min="0" max="99" oninput="dftPreview()">
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                    <select class="form-select form-select-sm" id="dft_case" onchange="dftPreview()">
                        <option value="s">Sensitive</option>
                        <option value="i">Insensitive</option>
                    </select>
                </div>
            </div>
            <%-- Row 3: boolean toggles --%>
            <div class="row g-1 mb-1 row-cols-3 row-cols-md-6">
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>asap</code></label>
                    <select class="form-select form-select-sm" id="dft_asap" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>deleted</code></label>
                    <select class="form-select form-select-sm" id="dft_deleted" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>expired</code></label>
                    <select class="form-select form-select-sm" id="dft_expired" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>replicated</code></label>
                    <select class="form-select form-select-sm" id="dft_replicated" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>proxy</code></label>
                    <select class="form-select form-select-sm" id="dft_proxy" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>event</code></label>
                    <select class="form-select form-select-sm" id="dft_event" onchange="dftPreview()">
                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                    </select>
                </div>
            </div>
            <%-- Row 4: ts range --%>
            <div class="row g-1 mb-1">
                <div class="col-12">
                    <label class="form-label mb-0 fw-semibold"><code>ts</code> <span class="text-muted fw-normal">range (numeric)</span></label>
                    <div class="d-flex flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:160px">
                            <select class="form-select form-select-sm" id="dft_ts_op1" style="width:75px;flex:none" onchange="dftPreview()">
                                <option value="=">=</option><option value=">">&gt;</option><option value=">=">&gt;=</option><option value="<">&lt;</option><option value="<=">&lt;=</option>
                            </select>
                            <input type="number" class="form-control form-control-sm" id="dft_ts_val1" placeholder="from" oninput="dftPreview()">
                        </div>
                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:160px">
                            <span class="text-muted small text-nowrap flex-shrink-0">to</span>
                            <select class="form-select form-select-sm" id="dft_ts_op2" style="width:75px;flex:none" onchange="dftPreview()">
                                <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                            </select>
                            <input type="number" class="form-control form-control-sm" id="dft_ts_val2" placeholder="to" oninput="dftPreview()">
                        </div>
                    </div>
                </div>
            </div>
            <%-- Row 5: size range --%>
            <div class="row g-1 mb-1">
                <div class="col-12">
                    <label class="form-label mb-0 fw-semibold"><code>size</code> <span class="text-muted fw-normal">range</span></label>
                    <div class="d-flex flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:220px">
                            <select class="form-select form-select-sm" id="dft_size_op1" style="width:75px;flex:none" onchange="dftPreview()">
                                <option value=">=">&gt;=</option><option value=">">&gt;</option><option value="=">=</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                            </select>
                            <input type="number" class="form-control form-control-sm" id="dft_size_val1" placeholder="min" min="0" oninput="dftPreview()">
                            <select class="form-select form-select-sm" id="dft_size_unit1" style="width:70px;flex:none" onchange="dftPreview()">
                                <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                            </select>
                        </div>
                        <div class="d-flex align-items-center gap-1 flex-grow-1" style="min-width:220px">
                            <span class="text-muted small text-nowrap flex-shrink-0">to</span>
                            <select class="form-select form-select-sm" id="dft_size_op2" style="width:75px;flex:none" onchange="dftPreview()">
                                <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                            </select>
                            <input type="number" class="form-control form-control-sm" id="dft_size_val2" placeholder="max" min="0" oninput="dftPreview()">
                            <select class="form-select form-select-sm" id="dft_size_unit2" style="width:70px;flex:none" onchange="dftPreview()">
                                <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
            <div class="d-flex align-items-start gap-2 pt-1 border-top mt-1 flex-wrap">
                <i class="bi bi-terminal text-muted flex-shrink-0 mt-1"></i>
                <code class="text-muted flex-grow-1" id="dft_preview" style="font-size:0.8rem;word-break:break-all;min-width:0">-- fill in fields above --</code>
                <div class="d-flex gap-1 flex-shrink-0">
                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dftClear()">
                    <i class="bi bi-x-circle me-1"></i>Clear
                </button>
                <button type="button" class="btn btn-sm btn-primary" onclick="dftApply()">
                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                </button>
                </div>
            </div>
        </div>

<script>
function dftVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
function dftQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
function dftBuild() {
    var p = [];
    ['target','source','mover','identity','checksum','groupby'].forEach(function(f) {
        var v = dftVal('dft_' + f); if (v) p.push(f + '=' + dftQuote(v));
    });
    ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
        var v = dftVal('dft_' + f); if (v) p.push(f + '=' + v);
    });
    var prio = dftVal('dft_priority'); if (prio) p.push('priority=' + prio);
    var tv1 = dftVal('dft_ts_val1'); if (tv1) p.push('ts' + dftVal('dft_ts_op1') + tv1);
    var tv2 = dftVal('dft_ts_val2'); if (tv2) p.push('ts' + dftVal('dft_ts_op2') + tv2);
    var sv1 = dftVal('dft_size_val1'); if (sv1) p.push('size' + dftVal('dft_size_op1') + sv1 + dftVal('dft_size_unit1'));
    var sv2 = dftVal('dft_size_val2'); if (sv2) p.push('size' + dftVal('dft_size_op2') + sv2 + dftVal('dft_size_unit2'));
    if (dftVal('dft_case') === 'i') p.push('case=i');
    return p.join(' ');
}
function dftPreview() {
    document.getElementById('dft_preview').textContent = dftBuild() || '-- fill in fields above --';
}
function dftApply() {
    document.getElementById('fileNameSearch').value = dftBuild();
    document.getElementById('dftQueryBuilder').style.display = 'none';
    document.destinationDetailActionForm.submit();
}
function dftClear() {
    ['target','source','mover','identity','checksum','groupby','priority'].forEach(function(f) {
        var el = document.getElementById('dft_' + f); if (el) el.value = '';
    });
    ['ts_val1','ts_val2','size_val1','size_val2'].forEach(function(f) {
        var el = document.getElementById('dft_' + f); if (el) el.value = '';
    });
    ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
        document.getElementById('dft_' + f).value = '';
    });
    document.getElementById('dft_case').value = 's';
    dftPreview();
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
    parseQBQuery(document.getElementById('fileNameSearch').value, 'dft_', ['ts','size'], []);
    dftPreview();
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
    var pw = Math.min(820, vw - 2 * margin);
    var left = Math.max(sx + margin, Math.min(r.right + sx - pw, sx + vw - pw - margin));
    panel.style.top = (r.bottom + sy + 4) + 'px';
    panel.style.left = left + 'px';
    panel.style.width = pw + 'px';
    panel.style.right = 'auto';
    panel.style.overflowX = 'auto';
    parseQBQuery(document.getElementById('fileNameSearch').value, 'dft_', ['ts','size'], []);
    dftPreview();
    panel.style.display = 'block';
}
document.addEventListener('click', function(e) {
    var panel = document.getElementById('dftQueryBuilder');
    var btn = document.getElementById('btnDftQB');
    if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
        panel.style.display = 'none';
});
window.addEventListener('resize', function() {
    var panel = document.getElementById('dftQueryBuilder');
    if (panel) panel.style.display = 'none';
});
</script>
