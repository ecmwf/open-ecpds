<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
$.datepicker.setDefaults({ dateFormat: 'yy-mm-dd' });
$(function() {
$("#date").datepicker({
defaultDate: "+1w",
changeMonth: true,
numberOfMonths: 2,
maxDate: 0
});
});
$(document).ready(function() {
$("#date").val($.datepicker.formatDate('yy-mm-dd', new Date()));
});
</script>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-funnel text-secondary flex-shrink-0"></i>
Use this form to request a Compression Simulation for a Destination.
</div>

<html:form action="/admin/filter">
<bean:define id="destinations" name="filterActionForm"
property="destinationOptions" type="java.util.Collection" />

<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-sliders text-primary"></i>
<span class="fw-semibold">Simulation Parameters</span>
<button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button" data-bs-toggle="collapse" data-bs-target="#filterSimInfo" aria-expanded="false" title="About Compression Simulation">
  <i class="bi bi-info-circle"></i>
</button>
</div>
<div class="collapse" id="filterSimInfo">
<div class="px-3 py-2" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-bottom:1px solid var(--bs-border-color);">
  <p class="text-muted mb-2">This page lets you select files from a Destination to test a chosen compression algorithm. Once the simulation is complete, a report is sent by e-mail.</p>
  <div class="fw-semibold text-muted mb-1" style="font-size:0.78rem;">Notes</div>
  <div class="mb-1"><i class="bi bi-check-circle text-success me-1"></i>Files that have already been <strong>pre-compressed</strong> will not be re-compressed, but their compressed size is still included in the report.</div>
  <div class="mb-1"><i class="bi bi-trash text-danger me-1"></i>All files compressed during a simulation are <strong>removed from the movers</strong> after compression &mdash; they are not kept.</div>
  <div><i class="bi bi-shield-check text-primary me-1"></i><strong>Exception:</strong> files that already existed in their compressed form before the simulation are <strong>not deleted</strong>.</div>
</div>
</div>
<div class="card-body">
<div class="row g-3">

<%-- Destination searchable autocomplete --%>
<div class="col-12">
<label class="form-label mb-1">Destination</label>
<input type="hidden" name="destination" id="destinationHidden" value="<c:out value="${filterActionForm.destination}"/>">
<div style="position:relative" id="filterDestWrap">
<div class="input-group input-group-sm">
<span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
<input type="text" id="filterDestInput" class="form-control form-control-sm" style="max-width:360px"
placeholder="Search destination..." autocomplete="off"
value="<c:out value="${filterActionForm.destination}"/>"
oninput="filterDestFilter()"
onfocus="filterDestOpen()"
onblur="setTimeout(filterDestClose, 200)"
onkeydown="filterDestKey(event)">
</div>
<ul id="filterDestDropdown" role="listbox"
class="list-unstyled border rounded bg-body shadow-sm mb-0"
style="display:none;position:absolute;z-index:1050;min-width:360px;max-height:260px;overflow-y:auto;top:100%;left:0;margin-top:2px;padding:3px 0"></ul>
</div>
<ul id="filterDestData" style="display:none">
<c:forEach var="d" items="${destinations}">
<li data-name="<c:out value="${d.name}"/>"
data-label="<c:out value="${d.value}"/>"></li>
</c:forEach>
</ul>
<script>
(function(){
var opts = Array.from(document.querySelectorAll('#filterDestData li')).map(function(el){
return { n: el.dataset.name, l: el.dataset.label };
});
var active = -1, filtered = [];
function esc(s){ return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function render(q){
var drop = document.getElementById('filterDestDropdown');
var lq = q ? q.toLowerCase() : '';
filtered = lq ? opts.filter(function(o){
return o.n.toLowerCase().indexOf(lq)>=0 || o.l.toLowerCase().indexOf(lq)>=0;
}) : opts.slice();
active = -1;
if(!filtered.length){ drop.innerHTML='<li style="padding:4px 10px;font-size:0.875rem;color:#6c757d">No match</li>'; return; }
drop.innerHTML = filtered.map(function(o,i){
var label = o.l ? '<br><small class="text-muted" style="font-size:0.78rem">'+esc(o.l)+'</small>' : '';
return '<li role="option" data-idx="'+i+'" style="padding:5px 10px;cursor:pointer;line-height:1.3"'
+' onmouseover="filterDestHover(this)" onmousedown="filterDestPick('+i+')">'
+esc(o.n)+label+'</li>';
}).join('');
}
function setActive(idx){
var items = document.querySelectorAll('#filterDestDropdown li[data-idx]');
items.forEach(function(el){ el.style.background=''; });
active = (idx>=0 && idx<items.length) ? idx : -1;
if(active>=0){ items[active].style.background='var(--bs-secondary-bg)'; items[active].scrollIntoView({block:'nearest'}); }
}
function pick(o){
document.getElementById('filterDestInput').value = o.n;
document.getElementById('destinationHidden').value = o.n;
document.getElementById('filterDestDropdown').style.display='none';
}
window.filterDestFilter = function(){ render(document.getElementById('filterDestInput').value); document.getElementById('filterDestDropdown').style.display='block'; };
window.filterDestOpen  = function(){ render(document.getElementById('filterDestInput').value); document.getElementById('filterDestDropdown').style.display='block'; };
window.filterDestClose = function(){ document.getElementById('filterDestDropdown').style.display='none'; };
window.filterDestHover = function(el){ setActive(parseInt(el.dataset.idx,10)); };
window.filterDestPick  = function(idx){ if(filtered[idx]) pick(filtered[idx]); };
window.filterDestKey   = function(e){
var drop = document.getElementById('filterDestDropdown');
var isOpen = drop.style.display!=='none';
if(e.key==='ArrowDown'){ e.preventDefault(); if(!isOpen){ render(document.getElementById('filterDestInput').value); drop.style.display='block'; } setActive(Math.min(active+1,filtered.length-1)); }
else if(e.key==='ArrowUp'){ e.preventDefault(); setActive(Math.max(active-1,0)); }
else if(e.key==='Enter'){ e.preventDefault(); if(active>=0&&filtered[active]) filterDestPick(active); else if(filtered.length===1) filterDestPick(0); }
else if(e.key==='Escape'){ drop.style.display='none'; }
};
})();
</script>
</div>

<%-- File Pattern --%>
<div class="col-sm-5">
<label for="pattern" class="form-label mb-1">File Pattern</label>
<html:text property="pattern" styleId="pattern" styleClass="form-control form-control-sm" />
</div>

<%-- Data Compression --%>
<div class="col-sm-4">
<label for="filter" class="form-label mb-1">Data Compression</label>
<bean:define id="filters" name="filterActionForm"
property="filterNameOptions" type="java.util.Collection" />
<html:select property="filter" styleId="filter" styleClass="form-select form-select-sm">
<html:options collection="filters" property="name" labelProperty="name" />
</html:select>
</div>

<%-- Date --%>
<div class="col-sm-3">
<label for="date" class="form-label mb-1">Date</label>
<input type="text" name="date" id="date" class="form-control form-control-sm" autocomplete="off">
</div>

<%-- Email --%>
<div class="col-sm-8">
<label for="filterEmail" class="form-label mb-1">Email</label>
<input type="email" name="email" id="filterEmail"
value="<c:out value="${filterActionForm.email}"/>"
class="form-control form-control-sm"
placeholder="user@example.com"
required>
<div id="filterEmailFeedback" class="form-text text-danger" style="display:none">
Please enter a valid email address.
</div>
</div>

<%-- Include Standby --%>
<div class="col-sm-4 d-flex align-items-end pb-1">
<div class="form-check form-switch">
<html:checkbox property="includeStdby" styleClass="form-check-input" styleId="includeStdby" />
<label class="form-check-label" for="includeStdby">Include Standby</label>
</div>
</div>

</div>
</div>
</div>

<div class="mt-2">
<button type="submit" id="filterSubmitBtn" class="btn btn-primary">
<i class="bi bi-play-fill me-1"></i>Launch Compression Simulation
</button>
</div>
</html:form>

<script>
(function(){
var emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
var emailEl = document.getElementById('filterEmail');
var feedbackEl = document.getElementById('filterEmailFeedback');

function validateEmail() {
var val = emailEl.value.trim();
var ok = val.length > 0 && emailRe.test(val);
feedbackEl.style.display = ok ? 'none' : 'block';
emailEl.classList.toggle('is-invalid', !ok);
return ok;
}

emailEl.addEventListener('input', validateEmail);
emailEl.addEventListener('blur', validateEmail);

document.querySelector('form[name="filterActionForm"]').addEventListener('submit', function(e) {
var destOk = document.getElementById('destinationHidden').value.trim().length > 0;
var emailOk = validateEmail();
if (!destOk || !emailOk) {
e.preventDefault();
if (!destOk) {
document.getElementById('filterDestInput').focus();
document.getElementById('filterDestInput').classList.add('is-invalid');
}
}
});

document.getElementById('filterDestInput').addEventListener('input', function() {
this.classList.remove('is-invalid');
});
})();
</script>
