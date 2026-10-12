<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<style>
.dn-cell { width: 84px; cursor: pointer; text-align: center; user-select: none; }
.dn-cell.definite { background: #dc3545; color: #fff; }
.dn-cell.possible { background: #ffc107; color: #000; }
.dn-cell.na { background: var(--bs-tertiary-bg); color: #aaa; cursor: default; }
.dn-matrix-wrap { max-height: 60vh; overflow: auto; }
.dn-pill { cursor: pointer; }
.dn-new { background: #d1e7dd; }
#dnBody { font-family: monospace; font-size: 0.85rem; }
</style>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
 style="background:rgba(108,117,125,0.06); border-left:4px solid #6c757d;">
 <i class="bi bi-bell text-secondary flex-shrink-0"></i>
 <span>Send one consistent notice for all the products and cycles affected by a delay, then inform of the resolution.</span>
 <a href="#" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" onclick="var el=document.getElementById('dnGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;" title="Open Configuration Guide"><i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Configuration </span>Guide</a>
</div>

<div id="dnAlert"></div>

<ul class="nav nav-tabs mb-3">
 <li class="nav-item"><button class="nav-link active" id="dnTabCompose" type="button" onclick="dnTab('compose')"><i class="bi bi-send me-1"></i><span id="dnTabComposeLbl">Send delay notification</span></button></li>
 <li class="nav-item"><button class="nav-link" id="dnTabSent" type="button" onclick="dnTab('sent')"><i class="bi bi-journal-text me-1"></i>Sent notices <span class="badge text-bg-secondary" id="dnSentCount">0</span></button></li>
</ul>

<div id="dnCompose" class="row g-3">
 <div class="col-xl-7">
  <div class="card border-0 shadow-sm">
   <div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)"><i class="bi bi-grid-3x3-gap text-primary me-1"></i>Affected products and cycles</div>
   <div class="card-body">
    <div class="input-group input-group-sm mb-2">
     <span class="input-group-text">Product name</span>
     <input type="text" id="dnFilter" class="form-control" placeholder="e.g. GENFO, *ERA5*, !*TEST*" oninput="dnRenderMatrix()">
    </div>
    <div class="mb-2" id="dnModels"></div>
    <div class="small text-muted mb-2">Click a cell to switch between <span class="badge text-bg-danger">Definite</span>, <span class="badge text-bg-warning">Possible</span> and cleared. Click a product name to cycle its whole row.</div>
    <div class="dn-matrix-wrap"><table class="table table-sm table-bordered align-middle mb-0" id="dnMatrix"></table></div>
    <div class="mt-2 small text-muted d-flex align-items-center"><span><span id="dnSelCount">0</span> cell(s) selected</span> <button class="btn btn-link btn-sm p-0 ms-2 text-decoration-none" style="font-size:inherit;line-height:inherit" onclick="dnClear()">Clear all</button></div>
   </div>
  </div>
 </div>
 <div class="col-xl-5">
  <div class="card border-0 shadow-sm">
   <div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)"><i class="bi bi-envelope text-primary me-1"></i>Notice <span id="dnUpdBadge" class="badge text-bg-info ms-1 d-none"></span></div>
   <div class="card-body">
    <div class="mb-2">
     <label class="form-label small mb-1">To</label>
     <div>
      <span class="badge rounded-pill text-bg-primary dn-pill" id="dnToPill" onclick="dnToggleDest()" title="Show the destinations affected">(0) Destinations</span>
      <span class="badge rounded-pill text-bg-secondary" id="dnEmailPill">0 emails</span>
      <button class="btn btn-link btn-sm" onclick="dnCopyBcc()"><i class="bi bi-clipboard"></i> Copy BCC emails</button>
     </div>
     <div id="dnDestList" class="small border rounded p-2 mt-1 d-none" style="max-height:160px; overflow:auto"></div>
     <div class="form-check form-switch mt-1"><input class="form-check-input" type="checkbox" id="dnNonMon" onchange="dnRecipients()"><label class="form-check-label small" for="dnNonMon">Include destinations not showing in the monitor</label></div>
    </div>
    <div class="mb-2">
     <div class="form-check"><input class="form-check-input" type="checkbox" id="dnCcOn" checked><label class="form-check-label small" for="dnCcOn">Cc (internal)</label></div>
     <div class="input-group input-group-sm"><input type="text" id="dnCc" class="form-control" placeholder="a@x.int; b@x.int"><button class="btn btn-outline-secondary" type="button" onclick="dnSaveCc()" title="Use as default Cc"><i class="bi bi-save"></i></button></div>
    </div>
    <div class="row g-2 mb-2">
     <div class="col-12"><label class="form-label small mb-1">Reason</label>
      <select id="dnReason" class="form-select form-select-sm" onchange="dnAuto()">
       <option>Delay in model production</option><option>Dissemination system issue</option><option>Power / infrastructure outage</option><option>Data acquisition / observations delay</option><option>Scheduled maintenance overrun</option><option>Network / connectivity issue</option>
      </select></div>
     <div class="col-6"><label class="form-label small mb-1">ETA (UTC)</label><input type="hidden" id="dnEta">
      <div class="input-group input-group-sm">
       <select id="dnEtaH" class="form-select" style="min-width:4.4rem; padding-left:.5rem; padding-right:1.6rem; background-position:right .4rem center" onchange="dnEtaChanged()" title="Hour (UTC)"></select>
       <span class="input-group-text">:</span>
       <select id="dnEtaM" class="form-select" style="min-width:4.4rem; padding-left:.5rem; padding-right:1.6rem; background-position:right .4rem center" onchange="dnEtaChanged()" title="Minute"></select>
       <button class="btn btn-outline-secondary" type="button" onclick="dnEtaNow()" title="Set the current time (UTC)"><i class="bi bi-clock"></i></button>
       <button class="btn btn-outline-secondary" type="button" onclick="dnEtaClear()" title="No ETA"><i class="bi bi-x-lg"></i></button>
      </div></div>
     <div class="col-6"><label class="form-label small mb-1">Expected delay</label>
      <select id="dnDelay" class="form-select form-select-sm" onchange="dnAuto()"><option value="">-</option><option>~15 min</option><option>~30 min</option><option>~45 min</option><option>~90 min</option><option>Under investigation</option></select></div>
     <div class="col-12"><label class="form-label small mb-1">Note</label><input type="text" id="dnNote" class="form-control form-control-sm" maxlength="500" oninput="dnAuto()"></div>
    </div>
    <div class="mb-2"><label class="form-label small mb-1">Subject</label><input type="text" id="dnSubject" class="form-control form-control-sm" oninput="dnEdited()"></div>
    <div class="mb-2"><div class="d-flex align-items-center gap-2 mb-1"><label class="form-label small mb-0" for="dnBody">Message</label><button type="button" class="btn btn-link btn-sm p-0 align-baseline text-decoration-none" onclick="dnEditedReset()">Regenerate</button></div><textarea id="dnBody" class="form-control" rows="12" oninput="dnEdited()"></textarea></div>
    <div class="d-flex gap-2">
     <button class="btn btn-danger" id="dnSendBtn" onclick="dnSend()"><i class="bi bi-send me-1"></i><span id="dnSendLbl">Send notice</span></button>
     <button class="btn btn-outline-primary" type="button" id="dnPreviewBtn" onclick="dnPreview()"><i class="bi bi-eye me-1"></i>Preview</button>
     <button class="btn btn-outline-secondary d-none" id="dnCancelUpd" onclick="dnLeaveUpdate()">Cancel update</button>
    </div>
    <div class="modal fade" id="dnPreviewModal" tabindex="-1"><div class="modal-dialog modal-lg modal-dialog-scrollable"><div class="modal-content">
     <div class="modal-header py-2"><h6 class="modal-title"><i class="bi bi-eye me-1"></i>Message preview</h6><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
     <div class="modal-body"><div id="dnPreviewHead" class="small mb-2"></div><pre id="dnPreviewBody" class="border rounded p-3 bg-body-tertiary" style="white-space:pre-wrap;font-family:inherit;font-size:0.9rem"></pre></div>
     <div class="modal-footer py-2"><button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button></div></div></div></div>
    <div class="small text-muted mt-2">The notice is registered, then an Outlook message opens with the recipients in BCC: press Send in Outlook to deliver it.</div>
   </div>
  </div>
 </div>
</div>

<div id="dnSent" class="d-none">
 <div class="card border-0 shadow-sm"><div class="card-body p-0">
  <table class="table table-sm align-middle mb-0">
   <thead><tr><th>Reference</th><th>Status</th><th>Created</th><th>Reason</th><th>Products</th><th>Recipients</th><th style="width:330px"></th></tr></thead>
   <tbody id="dnSentBody"></tbody>
  </table>
 </div>
 <div class="card-footer text-center d-none" id="dnMore"><button class="btn btn-sm btn-outline-secondary" onclick="dnMoreNotices()">Load older notices</button></div></div>
</div>

<script>
(function() {
var BASE = '/do/monitoring/delaynotification/';
var S = { cycles: {}, models: {}, notices: [], sel: {}, emails: [], dests: {}, bodyEdited: false, update: null, defaultCc: '', limit: 100, more: false };
var BCC_LIST = 'op_delay_ecmwf@lists.ecmwf.int', TO_ADDRESS = 'operators@ecmwf.int'; // replaced by the values of Product Messages
var MAX_URL = 6000;

function $(id) { return document.getElementById(id); }
function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function(c) { return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]; }); }
function post(action, payload) {
  return fetch(BASE + action, {method: 'POST', headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify(payload || {})}).then(function(r) { return r.json(); }).then(function(d) {
      if (!d.success) { throw new Error(d.error || 'Failed'); } return d; });
}
var alertTimer = null;
function alertMsg(msg, type) {
  type = type || 'danger';
  clearTimeout(alertTimer);
  $('dnAlert').innerHTML = '<div class="alert alert-' + type + ' alert-dismissible d-flex align-items-center py-2 pe-5">' + esc(msg) + '<button type="button" class="btn-close py-0 h-100 d-flex align-items-center" style="padding:0.5rem 1rem" data-bs-dismiss="alert"></button></div>';
  if (type !== 'danger') {
    alertTimer = setTimeout(function() { var a = $('dnAlert').querySelector('.alert'); if (a) { bootstrap.Alert.getOrCreateInstance(a).close(); } }, 5000);
  }
}
// ETA: hour and minute selectors, so that only valid HH:MM values (or none) can be entered.
(function() {
  function opts(n) { var h = '<option value="">--</option>'; for (var i = 0; i < n; i++) { var v = (i < 10 ? '0' : '') + i; h += '<option>' + v + '</option>'; } return h; }
  $('dnEtaH').innerHTML = opts(24); $('dnEtaM').innerHTML = opts(60);
})();
window.dnEtaChanged = function() {
  var h = $('dnEtaH').value, m = $('dnEtaM').value;
  if (h && !m) { $('dnEtaM').value = '00'; m = '00'; }
  if (!h) { $('dnEtaM').value = ''; m = ''; }
  $('dnEta').value = h ? h + ':' + m : '';
  dnAuto();
};
window.dnEtaNow = function() {
  var d = new Date(), p = function(n) { return (n < 10 ? '0' : '') + n; };
  $('dnEtaH').value = p(d.getUTCHours()); $('dnEtaM').value = p(d.getUTCMinutes());
  dnEtaChanged();
};
window.dnEtaClear = function() { $('dnEtaH').value = ''; dnEtaChanged(); };
function setEta(v) {
  var x = /^([01]?\d|2[0-3]):([0-5]\d)$/.exec((v || '').trim());
  $('dnEtaH').value = x ? (x[1].length < 2 ? '0' : '') + x[1] : ''; $('dnEtaM').value = x ? x[2] : '';
  $('dnEta').value = x ? $('dnEtaH').value + ':' + x[2] : '';
}
function fail(e) { alertMsg(e.message || e); }

window.dnTab = function(t) {
  $('dnCompose').classList.toggle('d-none', t !== 'compose');
  $('dnSent').classList.toggle('d-none', t !== 'sent');
  $('dnTabCompose').classList.toggle('active', t === 'compose');
  $('dnTabSent').classList.toggle('active', t === 'sent');
};

// Same syntax as the product-name filter of /do/monitoring: comma-separated, * and ? wildcards, ! or - to exclude.
function matches(filter, name) {
  var tokens = filter.split(',').map(function(t) { return t.trim(); }).filter(Boolean);
  if (!tokens.length) { return true; }
  var inc = [], exc = [];
  tokens.forEach(function(t) {
    var neg = /^(!|-|no-)/.test(t);
    var p = t.replace(/^(!|-|no-)/, '');
    var re = new RegExp('^' + p.replace(/[.+^\$\{\}()|[\]\\]/g, '\\$&').replace(/\*/g, '.*').replace(/\?/g, '.') + '$', 'i');
    (neg ? exc : inc).push(re);
  });
  return (!inc.length || inc.some(function(r) { return r.test(name); })) && !exc.some(function(r) { return r.test(name); });
}

var activeModels = {};
function modelOf(product) {
  for (var m in S.models) { if (S.models[m].some(function(p) { return p.toLowerCase() === product.toLowerCase(); })) { return m; } }
  return '';
}
function renderModels() {
  var html = '', names = Object.keys(S.models);
  names.concat(['']).forEach(function(m) {
    var label = m || 'Unassigned';
    var on = activeModels[m];
    html += '<button type="button" class="btn btn-sm ' + (on ? 'btn-primary' : 'btn-outline-primary') + ' me-1 mb-1" data-m="' + esc(m) + '">' + esc(label) + '</button>';
  });
  $('dnModels').innerHTML = html;
  Array.prototype.forEach.call($('dnModels').querySelectorAll('button'), function(b) {
    b.onclick = function() { activeModels[b.dataset.m] = !activeModels[b.dataset.m]; renderModels(); dnRenderMatrix(); };
  });
}

function visibleProducts() {
  var any = Object.keys(activeModels).some(function(k) { return activeModels[k]; });
  return Object.keys(S.cycles).filter(function(p) {
    return matches($('dnFilter').value, p) && (!any || activeModels[modelOf(p)]);
  });
}
function allCycles(products) {
  var set = {};
  products.forEach(function(p) { S.cycles[p].forEach(function(t) { set[t] = 1; }); });
  return Object.keys(set).sort();
}
window.dnRenderMatrix = function() {
  var products = visibleProducts(), cycles = allCycles(products);
  var h = '<thead><tr><th>Product</th>' + cycles.map(function(c) { return '<th class="text-center">' + esc(c) + '</th>'; }).join('') + '</tr></thead><tbody>';
  products.forEach(function(p) {
    h += '<tr><td class="fw-semibold" style="cursor:pointer" data-row="' + esc(p) + '">' + esc(p) + '</td>';
    cycles.forEach(function(c) {
      if (S.cycles[p].indexOf(c) < 0) { h += '<td class="dn-cell na">&middot;</td>'; return; }
      var l = S.sel[p + '@' + c] || '';
      h += '<td class="dn-cell ' + l + '" data-p="' + esc(p) + '" data-t="' + esc(c) + '">' + (l === 'definite' ? 'Definite' : l === 'possible' ? 'Possible' : '') + '</td>';
    });
    h += '</tr>';
  });
  $('dnMatrix').innerHTML = h + '</tbody>';
  Array.prototype.forEach.call($('dnMatrix').querySelectorAll('td[data-p]'), function(td) {
    td.onclick = function() { setCell(td.dataset.p, td.dataset.t, next(S.sel[td.dataset.p + '@' + td.dataset.t])); };
  });
  Array.prototype.forEach.call($('dnMatrix').querySelectorAll('td[data-row]'), function(td) {
    td.onclick = function() {
      var p = td.dataset.row, n = next(S.sel[p + '@' + S.cycles[p][0]]);
      S.cycles[p].forEach(function(t) { setCell(p, t, n, true); });
      afterSelection();
    };
  });
  $('dnSelCount').textContent = Object.keys(S.sel).length;
};
function next(l) { return !l ? 'definite' : l === 'definite' ? 'possible' : ''; }
function setCell(p, t, l, quiet) {
  if (l) { S.sel[p + '@' + t] = l; } else { delete S.sel[p + '@' + t]; }
  if (!quiet) { afterSelection(); }
}
function afterSelection() { dnRenderMatrix(); dnRecipients(); dnAuto(); }
window.dnClear = function() { S.sel = {}; afterSelection(); };

function cellList() {
  return Object.keys(S.sel).sort().map(function(k) { var i = k.lastIndexOf('@'); return {product: k.substring(0, i), time: k.substring(i + 1), level: S.sel[k]}; });
}
// Same rule as {{CYCLES}} on the monitoring pages: 00,01,02,03,06 becomes 00-03,06 (when enabled in Product Messages).
function compactTimes(times, sep) {
  var list = times.slice().sort(), join = sep || ', ';
  if (S.compactCycles === false || !list.every(function(t) { return /^\d+$/.test(t); })) { return list.join(join); }
  var out = [], i = 0;
  while (i < list.length) {
    var j = i;
    while (j + 1 < list.length && +list[j + 1] === +list[j] + 1) { j++; }
    out.push(j > i ? list[i] + '-' + list[j] : list[i]);
    i = j + 1;
  }
  return out.join(join);
}
function group(cells, level) {
  var per = {};
  cells.filter(function(c) { return c.level === level; }).forEach(function(c) { (per[c.product] = per[c.product] || []).push(c.time); });
  return Object.keys(per).sort().map(function(p) { return '  - ' + p + ' (cycles: ' + compactTimes(per[p].sort()) + ')'; });
}
function describeProducts(cells) {
  var names = {}; cells.forEach(function(c) { names[c.product] = 1; });
  return Object.keys(names).sort();
}

function fillTemplate(tpl, products, cells) {
  var per = {}; cells.forEach(function(c) { (per[c.product] = per[c.product] || []).push(c.time); });
  var all = {}; cells.forEach(function(c) { all[c.time] = 1; });
  var cycles = compactTimes(Object.keys(all), ',');
  var names = Object.keys(per).sort();
  var same = names.every(function(p) { return compactTimes(per[p], ',') === cycles; });
  // Nothing selected yet: keep visible stand-ins so the operator sees where the values will go.
  var productText = products.length ? products.join(', ') : '[products]';
  if (!cycles) { cycles = '[cycles]'; }
  if (!same) {
    // The products do not share the same cycles: name the cycles of each product, e.g. "GENFO (00,12Z) and GWAVE (06,18Z)".
    var parts = names.map(function(p) { return p + ' (' + compactTimes(per[p], ',') + 'Z)'; });
    productText = parts.length > 1 ? parts.slice(0, -1).join(', ') + ' and ' + parts[parts.length - 1] : parts[0];
    tpl = tpl.replace(/(the )?\{\{CYCLES?\}\}Z? cycles?/gi, 'their respective cycles');
  }
  var desc = products.map(function(p) { return S.descriptions && S.descriptions[p] ? '  - ' + p + ': ' + S.descriptions[p] : ''; }).filter(Boolean).join('\n');
  return tpl.split('{{PRODUCTS}}').join(productText).split('{{PRODUCT}}').join(productText).split('{{CYCLES}}').join(cycles).split('{{CYCLE}}').join(cycles)
    .split('{{DESCRIPTION}}').join(desc);
}
function affectedBlock(cells) {
  var lines = [];
  var d = group(cells, 'definite'), p = group(cells, 'possible');
  if (d.length) { lines.push('Products and cycles affected:'); lines = lines.concat(d, ''); }
  if (p.length) { lines.push('Products and cycles that might be affected:'); lines = lines.concat(p, ''); }
  if ($('dnReason').value) { lines.push('Reason: ' + $('dnReason').value); }
  if ($('dnDelay').value) { lines.push('Expected delay: ' + $('dnDelay').value); }
  if ($('dnEta').value.trim()) { lines.push('Estimated time of availability: ' + $('dnEta').value.trim() + ' UTC'); }
  if ($('dnNote').value.trim()) { lines.push('', $('dnNote').value.trim()); }
  return lines.join('\n').replace(/\s+$/, '');
}
function generate() {
  var cells = cellList();
  var products = describeProducts(cells);
  var subj = 'Delay notification' + (products.length ? ': ' : '') + (products.length > 4 ? products.slice(0, 4).join(', ') + ' and ' + (products.length - 4) + ' more' : products.join(', '));
  var tpl = S.delayTemplate || 'Dear users,\n\nWe are currently experiencing a delay.\n\nBest regards,\nECMWF Operations';
  var block = affectedBlock(cells);
  if (tpl.indexOf('{{AFFECTED}}') >= 0) {
    tpl = tpl.split('{{AFFECTED}}').join(block);
  } else if (block) {
    // The managed message has no {{AFFECTED}} placeholder: insert the details after its first paragraph.
    var k = tpl.search(/\n\s*\n/);
    tpl = k < 0 ? tpl + '\n\n' + block : tpl.substring(0, k) + '\n\n' + block + tpl.substring(k);
  }
  return {subject: subj, body: fillTemplate(tpl, products, cells)};
}
window.dnAuto = function() {
  if (S.bodyEdited) { return; }
  var g = generate(); $('dnSubject').value = g.subject; $('dnBody').value = g.body;
};
window.dnEdited = function() { S.bodyEdited = true; };
window.dnEditedReset = function() { S.bodyEdited = false; dnAuto(); };

window.dnRecipients = function() {
  var cells = cellList();
  if (!cells.length) { S.emails = []; S.dests = {}; renderRecipients(); return; }
  post('recipients', {cells: cells, includeNonMonitored: $('dnNonMon').checked}).then(function(d) {
    S.emails = d.emails; S.dests = d.destinations; renderRecipients();
  }).catch(fail);
};
function renderRecipients() {
  var names = Object.keys(S.dests);
  var known = S.update ? S.update.emails.map(function(e) { return e.toLowerCase(); }) : null;
  $('dnToPill').textContent = '(' + names.length + ') Destinations';
  $('dnEmailPill').textContent = S.emails.length + ' emails';
  $('dnDestList').innerHTML = names.length ? names.map(function(n) {
    var e = S.dests[n];
    var isNew = known && e && e.split(/[,;]/).some(function(x) { return x.trim() && known.indexOf(x.trim().toLowerCase()) < 0; });
    return '<div class="' + (isNew ? 'dn-new' : '') + '"><strong>' + esc(n) + '</strong> ' + (e ? '<span class="text-muted">' + esc(e) + '</span>' : '<span class="text-danger">no contact</span>') + (isNew ? ' <span class="badge text-bg-success">new</span>' : '') + '</div>';
  }).join('') : '<span class="text-muted">None</span>';
}
window.dnToggleDest = function() { $('dnDestList').classList.toggle('d-none'); };
function copy(text) {
  if (navigator.clipboard && window.isSecureContext) { return navigator.clipboard.writeText(text); }
  var t = document.createElement('textarea'); t.value = text; document.body.appendChild(t); t.select(); document.execCommand('copy'); t.remove();
  return Promise.resolve();
}
window.dnCopyBcc = function() { copy(S.emails.join(',')).then(function() { alertMsg(S.emails.length + ' email(s) copied', 'success'); }); };
window.dnSaveCc = function() { post('defaultcc', {cc: $('dnCc').value}).then(function() { alertMsg('Default Cc saved', 'success'); }).catch(fail); };

function outlook(subject, body, emails, cells, ccText) {
  var cc = (ccText || '').split(/[;,]/).map(function(x) { return x.trim(); }).filter(Boolean).join(',');
  function url(bcc) {
    return 'https://outlook.office365.com/mail/deeplink/compose?subject=' + encodeURIComponent(subject) + '&body=' + encodeURIComponent(body)
      + '&from=' + encodeURIComponent(TO_ADDRESS) + '&to=' + encodeURIComponent(TO_ADDRESS) + (cc ? '&cc=' + encodeURIComponent(cc) : '') + '&bcc=' + encodeURIComponent(bcc.join(','));
  }
  var u = url([BCC_LIST].concat(emails));
  if (u.length > MAX_URL) {
    // Too long for a link: use the distribution lists of each product/cycle, and offer the explicit emails to copy.
    var lists = {}; cells.forEach(function(c) { lists['ecpds-product-' + c.time + '-' + c.product + '@ecmwf.int'] = 1; });
    u = url([BCC_LIST].concat(Object.keys(lists)));
    alertMsg('The recipient list is too long for an Outlook link: the product distribution lists were used in BCC. Use "Copy BCC emails" for the explicit list.', 'warning');
  }
  window.open(u, '_blank', 'noopener');
}

function payload() {
  return {cells: cellList(), includeNonMonitored: $('dnNonMon').checked, includeCc: $('dnCcOn').checked, cc: $('dnCc').value,
    reason: $('dnReason').value, eta: $('dnEta').value, delay: $('dnDelay').value, note: $('dnNote').value,
    subject: $('dnSubject').value, body: $('dnBody').value};
}
// ECPDS cannot know whether the message was really sent from Outlook, so the operator confirms it here.
function askConfirm(ref, index, m) {
  var box = $('dnAlert');
  box.innerHTML = '<div class="alert alert-warning py-2"><div class="mb-2"><i class="bi bi-envelope-exclamation me-1"></i><strong>' + esc(ref)
    + '</strong> is registered (' + m.emails.length + ' emails) but <strong>not sent yet</strong>: press Send in the Outlook window, then confirm.</div>'
    + '<button class="btn btn-sm btn-success me-2" id="dnConfYes"><i class="bi bi-check2"></i> I have sent it</button>'
    + '<button class="btn btn-sm btn-outline-secondary me-2" id="dnConfAgain"><i class="bi bi-box-arrow-up-right"></i> Open Outlook again</button>'
    + '<button class="btn btn-sm btn-link" id="dnConfLater">Confirm later (Sent notices)</button></div>';
  $('dnConfYes').onclick = function() { confirmSent(ref, index); };
  $('dnConfAgain').onclick = function() { outlook(m.subject, m.body, m.emails, m.cells, m.cc); };
  $('dnConfLater').onclick = function() { box.innerHTML = ''; };
}
function confirmSent(ref, index) {
  post('confirm', {ref: ref, index: index}).then(function() { alertMsg('Sending of ' + ref + ' confirmed', 'success'); load(); }).catch(fail);
}
window.dnPreview = function() {
  var cc = $('dnCcOn').checked ? $('dnCc').value.split(/[;,]/).map(function(x) { return x.trim(); }).filter(Boolean) : [];
  var ref = S.update ? S.update.ref : 'DLY-yyyy-nnnn (assigned when sent)';
  var subj = '[' + ref + '] ' + (S.update ? 'UPDATE - ' : '') + $('dnSubject').value;
  $('dnPreviewHead').innerHTML = '<div><strong>Subject:</strong> ' + esc(subj) + '</div>'
    + '<div><strong>To:</strong> ' + esc(TO_ADDRESS) + '</div>'
    + '<div><strong>Cc:</strong> ' + (cc.length ? esc(cc.join(', ')) : '<span class="text-muted">none</span>') + '</div>'
    + '<div><strong>Bcc:</strong> ' + (S.emails || []).length + ' recipient email(s) in ' + Object.keys(S.dests || {}).length + ' destination(s)</div>';
  $('dnPreviewBody').textContent = $('dnBody').value;
  bootstrap.Modal.getOrCreateInstance($('dnPreviewModal')).show();
};
window.dnSend = function() {
  var p = payload();
  if (!p.cells.length) { alertMsg('Select at least one product and cycle'); return; }
  if (!p.subject.trim() || !p.body.trim()) { alertMsg('The subject and message are required'); return; }
  var btn = $('dnSendBtn'); btn.disabled = true;
  var upd = S.update;
  if (upd) { p.ref = upd.ref; }
  post(upd ? 'update' : 'send', p).then(function(d) {
    var ref = upd ? upd.ref : d.ref;
    var cc = p.includeCc ? p.cc : '';
    outlook('[' + ref + '] ' + (upd ? 'UPDATE - ' : '') + p.subject, p.body, d.emails, p.cells, cc);
    S.sel = {}; S.update = null; setUpdateMode(false); S.bodyEdited = false;
    afterSelection(); load();
    askConfirm(ref, d.index, {subject: '[' + ref + '] ' + (upd ? 'UPDATE - ' : '') + p.subject, body: p.body, emails: d.emails, cells: p.cells, cc: cc});
  }).catch(fail).then(function() { btn.disabled = false; });
};

function setUpdateMode(on, ref) {
  $('dnUpdBadge').classList.toggle('d-none', !on); $('dnUpdBadge').textContent = on ? 'Update ' + ref : '';
  $('dnCancelUpd').classList.toggle('d-none', !on);
  $('dnSendLbl').textContent = on ? 'Send update' : 'Send notice';
  $('dnTabComposeLbl').textContent = on ? 'Update ' + ref : 'Send delay notification';
}
window.dnLeaveUpdate = function() { S.update = null; setUpdateMode(false); S.sel = {}; S.bodyEdited = false; afterSelection(); };

function statusBadge(s) {
  var c = {active: 'danger', partial: 'warning', resolved: 'success', cancelled: 'secondary'}[s] || 'secondary';
  return '<span class="badge text-bg-' + c + '">' + esc(s) + '</span>';
}
function renderSent() {
  $('dnSentCount').textContent = S.notices.length;
  $('dnSentBody').innerHTML = S.notices.length ? S.notices.map(function(n, i) {
    var open = n.status === 'active' || n.status === 'partial';
    var prods = {}; (n.cells || []).forEach(function(c) { prods[c.product] = 1; });
    var pending = (n.history || []).some(function(x) { return x.confirmed === false; });
    var h = '<tr><td class="fw-semibold">' + esc(n.ref) + '</td><td>' + statusBadge(n.status) + (pending ? ' <span class="badge text-bg-warning" title="A message is registered but its sending is not confirmed">to confirm</span>' : '') + '</td><td class="small">' + esc(n.createdAt) + '<br>' + esc(n.createdBy) + '</td>'
      + '<td class="small">' + esc(n.reason) + '</td><td class="small">' + esc(Object.keys(prods).join(', ')) + '</td>'
      + '<td class="small">' + (n.destinations || []).length + ' dest., ' + (n.emails || []).length + ' emails</td><td class="text-nowrap">'
      + (open ? '<button class="btn btn-sm btn-success" data-a="resolve" data-i="' + i + '"><i class="bi bi-check2-circle"></i> Inform resolution</button> '
        + '<button class="btn btn-sm btn-outline-primary" data-a="update" data-i="' + i + '"><i class="bi bi-pencil"></i> Update</button> '
        + '<button class="btn btn-sm btn-outline-warning" data-a="cancel" data-i="' + i + '"><i class="bi bi-x-circle"></i></button> ' : '')
      + '<button class="btn btn-sm btn-outline-secondary" data-a="copy" data-i="' + i + '" title="Copy BCC emails"><i class="bi bi-clipboard"></i></button> '
      + '<button class="btn btn-sm btn-outline-danger" data-a="delete" data-i="' + i + '"><i class="bi bi-trash"></i></button></td></tr>';
    var hist = (n.history || []).map(function(x, k) {
      return '<div>' + esc(x.at) + ' ' + esc(x.kind) + ' by ' + esc(x.by) + (x.addedEmails && x.addedEmails.length ? ' (+' + x.addedEmails.length + ' recipients)' : '')
        + (x.confirmed === false ? ' <span class="badge text-bg-warning">not confirmed as sent</span> '
            + '<button class="btn btn-sm btn-outline-success py-0" data-a="confirm" data-i="' + i + '" data-k="' + k + '">I have sent it</button> '
            + '<button class="btn btn-sm btn-outline-secondary py-0" data-a="reopen" data-i="' + i + '" data-k="' + k + '">Open Outlook again</button>'
          : (x.confirmedAt ? ' <span class="text-success"><i class="bi bi-check2"></i> sent, confirmed by ' + esc(x.confirmedBy) + ' (' + esc(x.confirmedAt) + ')</span>' : '')) + '</div>';
    }).join('');
    return h + '<tr id="dnRes' + i + '" class="d-none"><td colspan="7"></td></tr><tr><td colspan="7" class="small text-muted py-1">' + hist + '</td></tr>';
  }).join('') : '<tr><td colspan="7" class="text-muted text-center py-3">No notice yet</td></tr>';
  $('dnMore').classList.toggle('d-none', !S.more);
  Array.prototype.forEach.call($('dnSentBody').querySelectorAll('button[data-a]'), function(b) {
    b.onclick = function() { sentAction(b.dataset.a, S.notices[+b.dataset.i], +b.dataset.i, +b.dataset.k); };
  });
}
function sentAction(a, n, i, k) {
  if (a === 'confirm') { confirmSent(n.ref, k); return; }
  if (a === 'reopen') {
    var m = n.history[k];
    outlook('[' + n.ref + '] ' + (m.kind === 'update' ? 'UPDATE - ' : '') + m.subject, m.body, m.emails || [], m.cells ? m.cells.map(function(c) { var j = c.lastIndexOf('@'); return {product: c.substring(0, j), time: c.substring(j + 1)}; }) : (n.cells || []), m.cc);
    return;
  }
  if (a === 'copy') { copy((n.emails || []).join(',')).then(function() { alertMsg('Emails copied', 'success'); }); }
  else if (a === 'delete') {
    if (confirm('Delete notice ' + n.ref + '? This cannot be undone.')) { post('delete', {ref: n.ref}).then(load).catch(fail); }
  } else if (a === 'cancel') {
    if (!confirm('Cancel notice ' + n.ref + '? No more update or resolution will be possible.')) { return; }
    post('cancel', {ref: n.ref, subject: 'Cancelled ' + n.ref, body: ''}).then(function() { load(); alertMsg(n.ref + ' cancelled. Use the copy button if you need to tell the recipients.', 'success'); }).catch(fail);
  } else if (a === 'update') { startUpdate(n); }
  else if (a === 'resolve') { openResolve(n, i); }
}
function startUpdate(n) {
  S.update = n; S.sel = {};
  (n.cells || []).forEach(function(c) { if (!c.resolved) { S.sel[c.product + '@' + c.time] = c.level; } });
  $('dnNonMon').checked = !!n.includeNonMonitored; $('dnCcOn').checked = !!n.includeCc; $('dnCc').value = n.cc || '';
  $('dnReason').value = n.reason; setEta(n.eta); $('dnDelay').value = n.delay || ''; $('dnNote').value = n.note || '';
  S.bodyEdited = false; dnAuto();
  $('dnSubject').value = n.subject; $('dnBody').value = 'UPDATE\n\n' + n.body; S.bodyEdited = true;
  setUpdateMode(true, n.ref); dnTab('compose'); dnRenderMatrix(); dnRecipients();
}
function openResolve(n, i) {
  var row = $('dnRes' + i); row.classList.toggle('d-none');
  var openCells = (n.cells || []).filter(function(c) { return !c.resolved; });
  var products = describeProducts(openCells);
  row.firstChild.innerHTML = '<div class="p-2 border rounded"><div class="small mb-1">Cells to resolve:</div>'
    + openCells.map(function(c) { return '<label class="me-3 small"><input type="checkbox" class="form-check-input me-1" checked value="' + esc(c.product + '@' + c.time) + '">' + esc(c.product + ' ' + c.time) + '</label>'; }).join('')
    + '<input class="form-control form-control-sm my-2 rs-subj" value="' + esc('Resolved: ' + products.join(', ')) + '">'
    + '<textarea class="form-control form-control-sm mb-2 rs-body" rows="6">' + esc(S.resumedTemplate ? fillTemplate(S.resumedTemplate, products, openCells) : 'Dear users,\n\nThe delay affecting ' + products.join(', ') + ' has been resolved.\n\nBest regards,\nECMWF Operations') + '</textarea>'
    + '<button class="btn btn-sm btn-success rs-go">Register and open the message</button></div>';
  row.querySelector('.rs-go').onclick = function() {
    var picked = Array.prototype.map.call(row.querySelectorAll('input[type=checkbox]:checked'), function(c) { return c.value; });
    var subject = row.querySelector('.rs-subj').value, body = row.querySelector('.rs-body').value;
    if (!picked.length) { alertMsg('Select at least one cell'); return; }
    post('resolve', {ref: n.ref, cells: picked.length === openCells.length ? [] : picked, subject: subject, body: body}).then(function(d) {
      var cells = picked.map(function(k) { var j = k.lastIndexOf('@'); return {product: k.substring(0, j), time: k.substring(j + 1)}; });
      var cc = n.includeCc ? n.cc : '';
      outlook('[' + n.ref + '] ' + subject, body, d.emails, cells, cc);
      load();
      askConfirm(n.ref, d.index, {subject: '[' + n.ref + '] ' + subject, body: body, emails: d.emails, cells: cells, cc: cc});
    }).catch(fail);
  };
}

window.dnMoreNotices = function() { S.limit += 100; load(); };
function load() {
  return post('data', {limit: S.limit}).then(function(d) {
    S.more = d.more;
    S.cycles = d.cycles; S.models = d.models; S.notices = d.notices; S.defaultCc = d.defaultCc;
    if (d.toAddress) { TO_ADDRESS = d.toAddress; } if (d.bccAddress) { BCC_LIST = d.bccAddress; }
    S.compactCycles = d.compactCycles; S.delayTemplate = d.delayTemplate; S.resumedTemplate = d.resumedTemplate; S.descriptions = d.descriptions;
    if (!$('dnCc').value && !S.update) { $('dnCc').value = d.defaultCc || ''; }
    renderModels(); renderSent(); dnRenderMatrix();
  }).catch(fail);
}
// Entry points from a monitoring page: ?product=GWAVE&time=18&action=delay|resumed ("*" matches everything)
function fromProductPage() {
  var q = new URLSearchParams(window.location.search);
  var product = q.get('product'), time = q.get('time'), action = q.get('action');
  if (!product || !time) { return; }
  var label = (product === '*' ? 'all products' : product) + (time === '*' ? '' : ' ' + time);
  function wanted(p, t) { return (product === '*' || product === p) && (time === '*' || time === t); }
  if (action === 'resumed') {
    var found = -1;
    (S.notices || []).some(function(n, i) {
      if ((n.status === 'active' || n.status === 'partial') && (n.cells || []).some(function(c) { return !c.resolved && wanted(c.product, c.time); })) { found = i; return true; }
      return false;
    });
    if (found < 0) { alertMsg('No open notice covers ' + label + '. Register a delay notice first.', 'warning'); return; }
    dnTab('sent'); openResolve(S.notices[found], found);
    Array.prototype.forEach.call($('dnRes' + found).querySelectorAll('input[type=checkbox]'), function(c) {
      var j = c.value.lastIndexOf('@'); c.checked = wanted(c.value.substring(0, j), c.value.substring(j + 1));
    });
    $('dnRes' + found).scrollIntoView({block: 'center'});
    return;
  }
  var n = 0;
  Object.keys(S.cycles).forEach(function(p) { S.cycles[p].forEach(function(t) { if (wanted(p, t)) { S.sel[p + '@' + t] = 'definite'; n++; } }); });
  if (!n) { alertMsg(label + ' is not available for notification', 'warning'); return; }
  afterSelection();
}
load().then(function() { dnAuto(); fromProductPage(); });
})();
</script>

<jsp:include page="/WEB-INF/jsp/pds/monitoring/delaynotification/guide.jsp"/>
