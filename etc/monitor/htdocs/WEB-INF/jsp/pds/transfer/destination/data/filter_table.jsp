<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>

<table class="select">

<c:if test="${not empty disseminationStreamOptionsWithSizes}">
<tr>
<th width="55">Dissem_Str</th>
<th width="2"></th>
	<c:set var="disseminationStreamNow" value="${destinationDetailActionForm.disseminationStream}"/>		
	<c:forEach var="disseminationStream" items="${disseminationStreamOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${disseminationStream.name == disseminationStreamNow}">class="selected"</c:if>>
			<a href="javascript:setDisseminationStream('${disseminationStream.name}');">${disseminationStream.name}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${disseminationStream.name == disseminationStreamNow}">class="selected"</c:if>>
			<a title="Size: ${disseminationStream.formattedSize}" href="javascript:setDisseminationStream('${disseminationStream.name}');">
				(${disseminationStream.value})
			</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>
		</c:if>
	</c:forEach>
</tr>
</c:if>

<c:if test="${not empty dataStreamOptionsWithSizes}">
<tr>
<th>Data_Str</th>
<th></th>
	<c:set var="dataStreamNow" value="${destinationDetailActionForm.dataStream}"/>		
	<c:forEach var="dataStream" items="${dataStreamOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${dataStream.second == dataStreamNow}">class="selected"</c:if>>
			<a href="javascript:setDataStream('${dataStream.second}');">${dataStream.first}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${dataStream.second == dataStreamNow}">class="selected"</c:if>>
			<a title="Size: ${dataStream.formattedSize}" href="javascript:setDataStream('${dataStream.second}');">(${dataStream.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>		
		</c:if>		
	</c:forEach>
</tr>
</c:if>

<c:if test="${not empty dataTimeOptionsWithSizes}">
<tr>
<th>Base_Time</th>
<th></th>
	<c:set var="dataTimeNow" value="${destinationDetailActionForm.dataTime}"/>
	<c:forEach var="dataTime" items="${dataTimeOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${dataTime.first == dataTimeNow}">class="selected"</c:if>>
			<a href="javascript:setDataTime('${dataTime.second}');">${dataTime.first}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${dataTime.first == dataTimeNow}">class="selected"</c:if>>
			<a title="Size: ${dataTime.formattedSize}" href="javascript:setDataTime('${dataTime.second}');">(${dataTime.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>
		</c:if>
	</c:forEach>
</tr>
</c:if>

<tr>
<th>Status</th>
<th></th>
	<c:set var="statusNow" value="${destinationDetailActionForm.status}"/>
	<c:forEach var="status" items="${statusOptionsWithSizes}" varStatus="stat">
		<td width="55" style="text-align:right" <c:if test="${status.first == statusNow}">class="selected"</c:if>>
			<a href="javascript:setStatus('${status.first}')">${status.second}</a>
		</td>
		<td width="25" style="text-align:left" <c:if test="${status.first == statusNow}">class="selected"</c:if>>
			<a title="Size: ${status.formattedSize}" href="javascript:setStatus('${status.first}')">(${status.third})</a>
		</td>
		<c:if test="${stat.count % 8 == 0}">
		</tr><tr><th colspan="2"></th>			
		</c:if>		
	</c:forEach>
</tr>

<tr>
<th>Prod_Date</th>
<th></th>
	<c:set var="dateNow" value="${destinationDetailActionForm.date}"/>
	<c:forEach var="date" items="${dateOptions}" >
		<td colspan="2" width="80" <c:if test="${date.name == dateNow}">class="selected"</c:if>>
			<a href="javascript:setDate('${date.name}')">${date.value}</a>
		</td>
	</c:forEach>
</tr>

<tr>
<th></th>
<th></th>
 <td colspan="18">
 	<div class="d-flex gap-1 align-items-center">
 		<div class="input-group" style="flex:1">
		<span class="input-group-text"><i class="bi bi-search"></i></span>
		<input type="text" value="${destinationDetailActionForm.fileNameSearch}" placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i" class="form-control form-control-sm" title="Default search is by target. Conduct extended searches using target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules. Wildcards: * (zero or more chars), ? (exactly one char)." id="fileNameSearch" name="fileNameSearch" onkeypress="submitenter(this,event)">
		</div>
 		<button type="button" class="btn btn-sm btn-outline-secondary px-2"
 		        id="btnDftQB"
 		        onclick="toggleQBPanel('dftQueryBuilder','btnDftQB')"
 		        title="Build query">
 			<i class="bi bi-sliders2"></i>
 		</button>
 	</div>
</td>
</tr>

<tr>
<th colspan="2"></th>
<td colspan="18">
    <div id="dftQueryBuilder" class="border rounded p-2"
         style="display:none; position:absolute; z-index:9999; background:var(--bs-body-bg); box-shadow:0 4px 16px rgba(0,0,0,0.15); font-size:0.85rem">
            <%-- Row 1: text fields — align-items-end so inputs line up despite different label heights --%>
            <div class="row g-1 mb-1 align-items-end">
                <div class="col-3">
                    <label class="form-label mb-0 fw-semibold"><code>target=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                    <input type="text" class="form-control form-control-sm" id="dft_target" placeholder="e.g. *.dat" oninput="dftPreview()">
                </div>
                <div class="col-3">
                    <label class="form-label mb-0 fw-semibold"><code>source=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                    <input type="text" class="form-control form-control-sm" id="dft_source" placeholder="e.g. /tmp/*" oninput="dftPreview()">
                </div>
                <div class="col-3">
                    <label class="form-label mb-0 fw-semibold"><code>mover=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_mover" oninput="dftPreview()" list="dft_mover_list" autocomplete="off">
                    <datalist id="dft_mover_list">
                        <c:forEach var="m" items="${transferServerNames}">
                            <option value="${m}">
                        </c:forEach>
                    </datalist>
                </div>
                <div class="col-3">
                    <label class="form-label mb-0 fw-semibold"><code>checksum=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_checksum" oninput="dftPreview()">
                </div>
            </div>
            <%-- Row 2: text fields cont. --%>
            <div class="row g-1 mb-1">
                <div class="col-4">
                    <label class="form-label mb-0 fw-semibold"><code>identity=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_identity" oninput="dftPreview()">
                </div>
                <div class="col-4">
                    <label class="form-label mb-0 fw-semibold"><code>groupby=</code></label>
                    <input type="text" class="form-control form-control-sm" id="dft_groupby" oninput="dftPreview()">
                </div>
                <div class="col-2">
                    <label class="form-label mb-0 fw-semibold"><code>priority=</code></label>
                    <input type="number" class="form-control form-control-sm" id="dft_priority" min="0" max="99" oninput="dftPreview()">
                </div>
                <div class="col">
                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                    <select class="form-select form-select-sm" id="dft_case" onchange="dftPreview()">
                        <option value="s">Sensitive</option>
                        <option value="i">Insensitive</option>
                    </select>
                </div>
            </div>
            <%-- Row 3: boolean toggles --%>
            <div class="row g-1 mb-1">
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
                    <div class="d-flex align-items-center gap-1">
                        <select class="form-select form-select-sm" id="dft_ts_op1" style="width:75px;flex:none" onchange="dftPreview()">
                            <option value="=">=</option><option value=">">&gt;</option><option value=">=">&gt;=</option><option value="<">&lt;</option><option value="<=">&lt;=</option>
                        </select>
                        <input type="number" class="form-control form-control-sm" id="dft_ts_val1" placeholder="from" oninput="dftPreview()">
                        <span class="text-muted small px-1">to</span>
                        <select class="form-select form-select-sm" id="dft_ts_op2" style="width:75px;flex:none" onchange="dftPreview()">
                            <option value="<=">&lt;=</option><option value="<">&lt;</option><option value=">=">&gt;=</option><option value=">">&gt;</option>
                        </select>
                        <input type="number" class="form-control form-control-sm" id="dft_ts_val2" placeholder="to" oninput="dftPreview()">
                    </div>
                </div>
            </div>
            <%-- Row 5: size range --%>
            <div class="row g-1 mb-1">
                <div class="col-12">
                    <label class="form-label mb-0 fw-semibold"><code>size</code> <span class="text-muted fw-normal">range</span></label>
                    <div class="d-flex align-items-center gap-1">
                        <select class="form-select form-select-sm" id="dft_size_op1" style="width:75px;flex:none" onchange="dftPreview()">
                            <option value=">=">&gt;=</option><option value=">">&gt;</option><option value="=">=</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                        </select>
                        <input type="number" class="form-control form-control-sm" id="dft_size_val1" placeholder="min" min="0" oninput="dftPreview()">
                        <select class="form-select form-select-sm" id="dft_size_unit1" style="width:70px;flex:none" onchange="dftPreview()">
                            <option value="">b</option><option value="kb" selected>kb</option><option value="mb">mb</option><option value="gb">gb</option>
                        </select>
                        <span class="text-muted small px-1">to</span>
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
            <div class="d-flex align-items-start gap-2 pt-1 border-top mt-1">
                <i class="bi bi-terminal text-muted flex-shrink-0 mt-1"></i>
                <code class="text-muted flex-grow-1" id="dft_preview" style="font-size:0.8rem;word-break:break-all">-- fill in fields above --</code>
                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dftClear()">
                    <i class="bi bi-x-circle me-1"></i>Clear
                </button>
                <button type="button" class="btn btn-sm btn-primary" onclick="dftApply()">
                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                </button>
            </div>
        </div>
</td>
</tr>

</table>

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
    var pw = 820;
    panel.style.top = (r.bottom + sy + 4) + 'px';
    panel.style.left = Math.max(sx, r.right + sx - pw) + 'px';
    panel.style.width = pw + 'px';
    panel.style.right = 'auto';
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
