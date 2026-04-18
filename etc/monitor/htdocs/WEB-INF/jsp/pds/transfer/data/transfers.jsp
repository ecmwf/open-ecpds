<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>

<tiles:insert name="date.select" />

<%-- Status tabs + refresh --%>
<c:set var="currentRefresh" value="${empty param['refreshPeriod'] ? '0' : param['refreshPeriod']}"/>
<div class="d-flex align-items-center gap-3 mb-3 flex-wrap">
    <div class="d-flex flex-wrap gap-1">
        <c:forEach var="transferStatus" items="${transferStatusOptions}">
            <c:choose>
                <c:when test="${transferStatus.id == currentTransferStatus.id}">
                    <a href="?transferStatus=${transferStatus.id}&refreshPeriod=${currentRefresh}"
                       class="badge text-decoration-none bg-primary"
                       style="padding:0.35em 0.7em; font-size:0.8rem;">${transferStatus.name}</a>
                </c:when>
                <c:otherwise>
                    <a href="?transferStatus=${transferStatus.id}&refreshPeriod=${currentRefresh}"
                       class="badge text-decoration-none bg-secondary-subtle text-body"
                       style="padding:0.35em 0.7em; font-size:0.8rem;">${transferStatus.name}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
    <div class="d-flex align-items-center gap-1 flex-shrink-0">
        <i class="bi bi-arrow-clockwise text-muted me-1" style="font-size:0.85rem;" title="Auto-refresh interval"></i>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '0' ? 'active' : ''}" data-value="0">Off</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '30' ? 'active' : ''}" data-value="30">30s</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '60' ? 'active' : ''}" data-value="60">1m</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '300' ? 'active' : ''}" data-value="300">5m</a>
        <a href="#" class="date-pill refresh-pill ${currentRefresh == '600' ? 'active' : ''}" data-value="600">10m</a>
    </div>
</div>

<script>
    var refresh = '${param["refreshPeriod"]}';
    if (refresh != '' && refresh > 0) {
        setTimeout(function() { window.location.reload(true); }, refresh * 1000);
    }
    document.querySelectorAll('.refresh-pill').forEach(function(pill) {
        pill.addEventListener('click', function(e) {
            e.preventDefault();
            var params = new URLSearchParams(window.location.search);
            params.set('refreshPeriod', this.dataset.value);
            window.location.href = '?' + params.toString();
        });
    });
</script>

<%-- Search form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="transferSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <div class="row g-2">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="transferSearch" id="transferSearch" type="text"
                                    placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i"
                                    title="Default search is by target. Use target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules."
                                    value='<c:out value="${transferSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="transferType" onchange="form.submit()" title="Filter by Type">
                                    <c:forEach var="option" items="${transferTypeOptions}">
                                        <option value="${option.name}" <c:if test="${transferType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-2 d-flex gap-1">
                            <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-search"></i> Search</button>
                            <button type="button" class="btn btn-outline-secondary px-2"
                                    id="btnTransferQB"
                                    onclick="toggleQBPanel('queryBuilder','btnTransferQB')"
                                    title="Build query">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="queryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd) !important; box-shadow:0 8px 28px rgba(0,0,0,0.18),0 2px 6px rgba(0,0,0,0.10); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>target=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_target" placeholder="e.g. *.dat" oninput="qbPreview()">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>source=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_source" placeholder="e.g. /tmp/*" oninput="qbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>asap</code></label>
                                    <select class="form-select form-select-sm" id="qb_asap" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>deleted</code></label>
                                    <select class="form-select form-select-sm" id="qb_deleted" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>expired</code></label>
                                    <select class="form-select form-select-sm" id="qb_expired" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>replicated</code></label>
                                    <select class="form-select form-select-sm" id="qb_replicated" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>proxy</code></label>
                                    <select class="form-select form-select-sm" id="qb_proxy" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>event</code></label>
                                    <select class="form-select form-select-sm" id="qb_event" onchange="qbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="qb_case" onchange="qbPreview()">
                                        <option value="s">Sensitive</option>
                                        <option value="i">Insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-12">
                                    <label class="form-label mb-0 fw-semibold"><code>ts</code> <span class="text-muted fw-normal">range (numeric)</span></label>
                                    <div class="d-flex align-items-center gap-1">
                                        <select class="form-select form-select-sm" id="qb_ts_op1" style="width:75px;flex:none" onchange="qbPreview()">
                                            <option value="=">=</option><option value=">">&gt;</option><option value=">=">&gt;=</option><option value="<">&lt;</option><option value="<=">&lt;=</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="qb_ts_val1" placeholder="from" oninput="qbPreview()">
                                        <span class="text-muted small px-1">to</span>
                                        <select class="form-select form-select-sm" id="qb_ts_op2" style="width:75px;flex:none" onchange="qbPreview()">
                                            <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="qb_ts_val2" placeholder="to" oninput="qbPreview()">
                                    </div>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-12">
                                    <label class="form-label mb-0 fw-semibold"><code>size</code> <span class="text-muted fw-normal">range</span></label>
                                    <div class="d-flex align-items-center gap-1">
                                        <select class="form-select form-select-sm" id="qb_size_op1" style="width:75px;flex:none" onchange="qbPreview()">
                                            <option value=">=">&gt;=</option><option value=">">&gt;</option><option value="=">=</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="qb_size_val1" placeholder="min" min="0" oninput="qbPreview()">
                                        <select class="form-select form-select-sm" id="qb_size_unit1" style="width:70px;flex:none" onchange="qbPreview()">
                                            <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                                        </select>
                                        <span class="text-muted small px-1">to</span>
                                        <select class="form-select form-select-sm" id="qb_size_op2" style="width:75px;flex:none" onchange="qbPreview()">
                                            <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="qb_size_val2" placeholder="max" min="0" oninput="qbPreview()">
                                        <select class="form-select form-select-sm" id="qb_size_unit2" style="width:70px;flex:none" onchange="qbPreview()">
                                            <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-md-2">
                                    <label class="form-label mb-0 fw-semibold"><code>priority=</code></label>
                                    <input type="number" class="form-control form-control-sm" id="qb_priority" min="0" max="99" oninput="qbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-0 fw-semibold"><code>mover=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_mover" oninput="qbPreview()" list="qb_mover_list" autocomplete="off">
                                    <datalist id="qb_mover_list">
                                        <c:forEach var="ts" items="${transferServerOptions}">
                                            <option value="${ts.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label mb-0 fw-semibold"><code>identity=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_identity" oninput="qbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>groupby=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_groupby" oninput="qbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>checksum=</code></label>
                                    <input type="text" class="form-control form-control-sm" id="qb_checksum" oninput="qbPreview()">
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all" id="qb_preview">-- fill in fields above --</code>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="qbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="qbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
        </form>
        <script>
        function qbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function qbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function qbBuild() {
            var p = [];
            ['target','source','mover','identity','checksum','groupby'].forEach(function(f) {
                var v = qbVal('qb_' + f); if (v) p.push(f + '=' + qbQuote(v));
            });
            ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
                var v = qbVal('qb_' + f); if (v) p.push(f + '=' + v);
            });
            var prio = qbVal('qb_priority'); if (prio) p.push('priority=' + prio);
            var tv1 = qbVal('qb_ts_val1'); if (tv1) p.push('ts' + qbVal('qb_ts_op1') + tv1);
            var tv2 = qbVal('qb_ts_val2'); if (tv2) p.push('ts' + qbVal('qb_ts_op2') + tv2);
            var sv1 = qbVal('qb_size_val1'); if (sv1) p.push('size' + qbVal('qb_size_op1') + sv1 + qbVal('qb_size_unit1'));
            var sv2 = qbVal('qb_size_val2'); if (sv2) p.push('size' + qbVal('qb_size_op2') + sv2 + qbVal('qb_size_unit2'));
            if (qbVal('qb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function qbPreview() {
            var q = qbBuild();
            document.getElementById('qb_preview').textContent = q || '-- fill in fields above --';
        }
        function qbApply() {
            document.getElementById('transferSearch').value = qbBuild();
            document.getElementById('queryBuilder').style.display = 'none';
            document.getElementById('transferSearchForm').submit();
        }
        function qbClear() {
            ['target','source','mover','identity','checksum','groupby','priority'].forEach(function(f) {
                var el = document.getElementById('qb_' + f); if (el) el.value = '';
            });
            ['qb_ts_val1','qb_ts_val2','qb_size_val1','qb_size_val2'].forEach(function(id) {
                var el = document.getElementById(id); if (el) el.value = '';
            });
            ['asap','deleted','expired','replicated','proxy','event'].forEach(function(f) {
                document.getElementById('qb_' + f).value = '';
            });
            document.getElementById('qb_case').value = 's';
            qbPreview();
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
            parseQBQuery(document.getElementById('transferSearch').value, 'qb_', ['ts','size'], []);
            qbPreview();
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
            parseQBQuery(document.getElementById('transferSearch').value, 'qb_', ['ts','size'], []);
            qbPreview();
            panel.style.display = 'block';
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('queryBuilder');
            var btn = document.getElementById('btnTransferQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
                panel.style.display = 'none';
        });
        window.addEventListener('resize', function() {
            var panel = document.getElementById('queryBuilder');
            if (panel) panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- No results --%>
<c:if test="${empty transferList}">
    <div class="alert">
        <c:if test="${!hasFileNameSearch}">
            No Data Transfers found based on these criteria.
        </c:if>
        <c:if test="${hasFileNameSearch}">
            <c:if test="${!empty getTransfersError}">
                <strong>Error in your query:</strong> ${getTransfersError}
            </c:if>
            <c:if test="${empty getTransfersError}">
                No Data Transfers found. Default search is by target.
            </c:if>
            <p class="mb-1 mt-2">You can conduct an extended search using the following rules:</p>
            <ul class="mb-0">
                <li><code>target=</code>, <code>source=</code>, <code>ts=</code>, <code>priority=</code>, <code>groupby=</code>, <code>identity=</code>, <code>checksum=</code>, <code>size=</code>, <code>replicated=</code>, <code>asap=</code>, <code>deleted=</code>, <code>expired=</code>, <code>proxy=</code>, <code>mover=</code>, <code>event=</code></li>
                <li>Example: <code>asap=yes target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i</code></li>
                <li><code>case=i</code> for case-insensitive, <code>case=s</code> for case-sensitive (default)</li>
                <li>Enclose values with spaces or equals signs in double quotes, e.g. <code>"United States"</code></li>
                <li>Wildcards: <code>*</code> (zero or more chars), <code>?</code> (exactly one char)</li>
            </ul>
        </c:if>
    </div>
</c:if>

<%-- Results table --%>
<c:if test="${!empty transferList}">
    <display:table id="transfer" name="${transferList}" requestURI=""
        sort="external" defaultsort="3" partialList="true"
        size="${transferListSize}" pagesize="${recordsPerPage}"
        class="listing">

        <display:column title="Destination" sortable="true">
            <a title="${transfer.destination.comment}"
               href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a>
        </display:column>

        <display:column title="Transfer Host" sortable="true">
            <c:set var="nickName" value="${transfer.hostNickName}" />
            <jsp:useBean id="nickName" type="java.lang.String" />
            <c:if test='<%="".equals(nickName)%>'>
                <i class="bi bi-x-circle text-warning" title="Not transferred to remote host"></i>
            </c:if>
            <c:if test="<%=nickName.length() > 0%>">
                <c:if test="${transfer.transferServerName == null}">
                    <a href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
                </c:if>
                <c:if test="${transfer.transferServerName != null}">
                    <a title="Transmitted through ${transfer.transferServerName}"
                       href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
                </c:if>
            </c:if>
        </display:column>

        <display:column title="Sched. Time" sortable="true">
            <content:content name="transfer.scheduledTime"
                dateFormatKey="date.format.transfer" ignoreNull="true" />
        </display:column>

        <display:column title="Target" sortable="true">
            <c:if test="${!transfer.deleted}">
                <a title="Size: ${transfer.formattedSize}"
                   href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
            </c:if>
            <c:if test="${transfer.deleted}">
                <a title="Size: ${transfer.formattedSize}"
                   href="/do/transfer/data/${transfer.id}" class="text-danger">${transfer.target}</a>
            </c:if>
        </display:column>

        <display:column title="%" property="progress" sortable="true" />

        <display:column title="Mbits/s" sortable="true"
            sortProperty="formattedTransferRateInMBitsPerSeconds">
            <c:if test="${transfer.transferRate != '0'}">
                <a style="text-decoration:none;"
                   title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
            </c:if>
            <c:if test="${transfer.transferRate == 0}">
                <i class="bi bi-dash text-muted" title="Not applicable"></i>
            </c:if>
        </display:column>

        <display:column title="Prior" property="priority" sortable="true" />

    </display:table>
</c:if>
