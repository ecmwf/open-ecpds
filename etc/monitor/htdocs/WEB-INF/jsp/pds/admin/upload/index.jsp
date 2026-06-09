<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
<i class="bi bi-upload text-primary flex-shrink-0"></i>
Use this form to upload a text file to a Transfer Host.
</div>

<html:form action="/admin/upload">
<bean:define id="hosts" name="uploadActionForm"
property="hostOptions" type="java.util.Collection" />

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-cloud-upload text-primary"></i>
<span class="fw-semibold">Upload Parameters</span>
<button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button" data-bs-toggle="collapse" data-bs-target="#uploadParamInfo" aria-expanded="false" title="About Test Upload">
  <i class="bi bi-info-circle"></i>
</button>
</div>
<div class="collapse" id="uploadParamInfo">
<div class="px-3 py-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-bottom:1px solid var(--bs-border-color);">
  <p class="text-muted mb-2">This page lets you upload a test file directly to a Transfer Host, bypassing the normal transfer pipeline. It is useful for verifying a host's connectivity and write access.</p>
  <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Fields</div>
  <div class="mb-1"><i class="bi bi-hdd-network text-secondary me-1"></i><strong>Transfer Host</strong> &mdash; the host to write the file to.</div>
  <div class="mb-1"><i class="bi bi-file-earmark-text text-secondary me-1"></i><strong>Target File Name</strong> &mdash; destination path on the remote host (e.g. <code>file.txt</code> or <code>/path/to/file.txt</code>).</div>
  <div class="mb-1"><i class="bi bi-skip-forward text-secondary me-1"></i><strong>From Position</strong> &mdash; byte offset at which to start writing. Use <code>0</code> to write from the beginning of the file.</div>
  <div><i class="bi bi-body-text text-secondary me-1"></i><strong>File Content</strong> &mdash; the text content to write into the file.</div>
</div>
</div>
<div class="card-body">
<div class="row g-3">

<%-- Transfer Host searchable autocomplete --%>
<div class="col-12">
<label class="form-label mb-1">Transfer Host</label>
<input type="hidden" name="host" id="uploadHostHidden" value="<c:out value="${uploadActionForm.host}"/>">
<div style="position:relative" id="uploadHostWrap">
<div class="input-group input-group-sm">
<span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
<input type="text" id="uploadHostInput" class="form-control form-control-sm" style="max-width:360px"
placeholder="Search host..." autocomplete="off"
value="<c:out value="${uploadActionForm.host}"/>"
oninput="uploadHostFilter()"
onfocus="uploadHostOpen()"
onblur="setTimeout(uploadHostClose, 200)"
onkeydown="uploadHostKey(event)">
</div>
<ul id="uploadHostDropdown" role="listbox"
class="list-unstyled border rounded bg-body shadow-sm mb-0"
style="display:none;position:absolute;z-index:1050;min-width:360px;max-height:260px;overflow-y:auto;top:100%;left:0;margin-top:2px;padding:3px 0"></ul>
</div>
<ul id="uploadHostData" style="display:none">
<c:forEach var="h" items="${hosts}">
<c:if test="${h.active}">
<li data-name="<c:out value="${h.name}"/>"
data-nick="<c:out value="${h.nickName}"/>"
data-label="<c:out value="${h.comment}"/>"></li>
</c:if>
</c:forEach>
</ul>
<script>
(function(){
var opts = Array.from(document.querySelectorAll('#uploadHostData li')).map(function(el){
return { n: el.dataset.name, l: el.dataset.nick, c: el.dataset.label };
});
var active = -1, filtered = [];
function esc(s){ return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function render(q){
var drop = document.getElementById('uploadHostDropdown');
var lq = q ? q.toLowerCase() : '';
filtered = lq ? opts.filter(function(o){
return o.n.toLowerCase().indexOf(lq)>=0 || o.l.toLowerCase().indexOf(lq)>=0 || o.c.toLowerCase().indexOf(lq)>=0;
}) : opts.slice();
active = -1;
if(!filtered.length){ drop.innerHTML='<li style="padding:4px 10px;font-size:0.875rem;color:#6c757d">No match</li>'; return; }
drop.innerHTML = filtered.map(function(o,i){
var sub = o.c ? '<br><small class="text-muted" style="font-size:0.78rem">'+esc(o.c)+'</small>' : '';
return '<li role="option" data-idx="'+i+'" style="padding:5px 10px;cursor:pointer;line-height:1.3"'
+' onmouseover="uploadHostHover(this)" onmousedown="uploadHostPick('+i+')">'
+esc(o.l)+sub+'</li>';
}).join('');
}
function setActive(idx){
var items = document.querySelectorAll('#uploadHostDropdown li[data-idx]');
items.forEach(function(el){ el.style.background=''; });
active = (idx>=0 && idx<items.length) ? idx : -1;
if(active>=0){ items[active].style.background='var(--bs-secondary-bg)'; items[active].scrollIntoView({block:'nearest'}); }
}
function pick(o){
document.getElementById('uploadHostInput').value = o.l;
document.getElementById('uploadHostHidden').value = o.n;
document.getElementById('uploadHostDropdown').style.display='none';
}
window.uploadHostFilter = function(){ render(document.getElementById('uploadHostInput').value); document.getElementById('uploadHostDropdown').style.display='block'; };
window.uploadHostOpen  = function(){ render(document.getElementById('uploadHostInput').value); document.getElementById('uploadHostDropdown').style.display='block'; };
window.uploadHostClose = function(){ document.getElementById('uploadHostDropdown').style.display='none'; };
window.uploadHostHover = function(el){ setActive(parseInt(el.dataset.idx,10)); };
window.uploadHostPick  = function(idx){ if(filtered[idx]) pick(filtered[idx]); };
window.uploadHostKey   = function(e){
var drop = document.getElementById('uploadHostDropdown');
var isOpen = drop.style.display!=='none';
if(e.key==='ArrowDown'){ e.preventDefault(); if(!isOpen){ render(document.getElementById('uploadHostInput').value); drop.style.display='block'; } setActive(Math.min(active+1,filtered.length-1)); }
else if(e.key==='ArrowUp'){ e.preventDefault(); setActive(Math.max(active-1,0)); }
else if(e.key==='Enter'){ e.preventDefault(); if(active>=0&&filtered[active]) uploadHostPick(active); else if(filtered.length===1) uploadHostPick(0); }
else if(e.key==='Escape'){ drop.style.display='none'; }
};
})();
</script>
</div>

<%-- Target File Name --%>
<div class="col-sm-7">
<label for="uploadTarget" class="form-label mb-1">Target File Name</label>
<html:text property="target" styleId="uploadTarget"
styleClass="form-control form-control-sm"
style="font-family:monospace" />
<div id="uploadTargetFeedback" class="form-text text-danger" style="display:none">
Please enter a target file name (e.g. <code>file.txt</code> or <code>/path/to/file.txt</code>).
</div>
</div>

<%-- From Position --%>
<div class="col-sm-3">
<label for="uploadFromPos" class="form-label mb-1">From Position</label>
<input type="number" name="fromPos" id="uploadFromPos" min="0" step="1"
value="<c:out value="${uploadActionForm.fromPos}"/>"
class="form-control form-control-sm" style="max-width:140px">
<div id="uploadFromPosFeedback" class="form-text text-danger" style="display:none">
Please enter a whole number &ge; 0.
</div>
</div>

<%-- File Content --%>
<div class="col-12">
<label for="text" class="form-label mb-1">File Content</label>
<html:textarea property="text" styleId="text"
styleClass="form-control form-control-sm font-monospace"
rows="12" />
</div>

</div>
</div>
</div>

<div class="pt-3">
<button type="submit" class="btn btn-primary">
<i class="bi bi-cloud-upload-fill me-1"></i>Launch Upload
</button>
</div>
</html:form>

<script>
(function(){
var posEl    = document.getElementById('uploadFromPos');
var posFb    = document.getElementById('uploadFromPosFeedback');
var targetEl = document.getElementById('uploadTarget');
var targetFb = document.getElementById('uploadTargetFeedback');

function validatePos() {
var val = posEl.value.trim();
var ok = val !== '' && /^\d+$/.test(val) && parseInt(val, 10) >= 0;
posFb.style.display = ok ? 'none' : 'block';
posEl.classList.toggle('is-invalid', !ok);
return ok;
}

function validateTarget() {
var val = targetEl.value.trim();
var ok = val.length > 0 && val[val.length - 1] !== '/';
targetFb.style.display = ok ? 'none' : 'block';
targetEl.classList.toggle('is-invalid', !ok);
return ok;
}

posEl.addEventListener('input', validatePos);
posEl.addEventListener('blur', validatePos);
targetEl.addEventListener('input', validateTarget);
targetEl.addEventListener('blur', validateTarget);

document.querySelector('form[name="uploadActionForm"]').addEventListener('submit', function(e) {
var hostOk   = document.getElementById('uploadHostHidden').value.trim().length > 0;
var targetOk = validateTarget();
var posOk    = validatePos();
if (!hostOk || !targetOk || !posOk) {
e.preventDefault();
if (!hostOk) {
document.getElementById('uploadHostInput').classList.add('is-invalid');
document.getElementById('uploadHostInput').focus();
} else if (!targetOk) {
targetEl.focus();
} else {
posEl.focus();
}
}
});

document.getElementById('uploadHostInput').addEventListener('input', function() {
this.classList.remove('is-invalid');
});
})();
</script>
