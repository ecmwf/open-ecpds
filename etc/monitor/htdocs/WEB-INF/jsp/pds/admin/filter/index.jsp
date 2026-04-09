<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
	$.datepicker.setDefaults({
		dateFormat : 'yy-mm-dd'
	});
	$(function() {
		$("#date").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			numberOfMonths : 2,
			maxDate : 0
		})
	});
	$(document).ready(function() {
		$("#date").val($.datepicker.formatDate('yy-mm-dd', new Date()));
	});
</script>

<div class="d-flex align-items-center gap-2 mb-3 px-1 py-2 rounded"
     style="background:rgba(13,110,253,0.06); font-size:0.83rem; color:#495057; border-left:3px solid #0d6efd;">
    <i class="bi bi-funnel text-primary ms-1 flex-shrink-0"></i>
    Use this form to request a Compression Simulation for a Destination.
</div>

<!-- <html:form action="/admin/filter"> -->
<form name="filterActionForm" action="/do/admin/filter" method="post">
	<bean:define id="destinations" name="filterActionForm"
		property="destinationOptions" type="java.util.Collection" />
	<table class="fields">
		<tr>
			<th>Destination</th>
			<td>
				<input type="hidden" name="destination" id="destinationHidden" value="<c:out value="${filterActionForm.destination}"/>">
				<div style="position:relative;display:inline-flex;align-items:center" id="filterDestWrap">
					<div class="input-group input-group-sm">
						<span class="input-group-text bg-white text-muted"><i class="bi bi-search"></i></span>
						<input type="text" id="filterDestInput" class="form-control" style="width:280px"
							placeholder="Search destination..." autocomplete="off"
							value="<c:out value="${filterActionForm.destination}"/>"
							oninput="filterDestFilter()"
							onfocus="filterDestOpen()"
							onblur="setTimeout(filterDestClose, 200)"
							onkeydown="filterDestKey(event)">
					</div>
					<ul id="filterDestDropdown" role="listbox"
						class="list-unstyled border rounded bg-white shadow-sm mb-0"
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
						if(active>=0){ items[active].style.background='#e9ecef'; items[active].scrollIntoView({block:'nearest'}); }
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
			</td>
		</tr>
		<tr>
			<th>File Pattern</th>
			<td><html:text property="pattern" /></td>
		</tr>
		<tr>
			<th>Email</th>
			<td>
				<input type="email" name="email" id="filterEmail"
					value="<c:out value="${filterActionForm.email}"/>"
					class="form-control-sm"
					placeholder="user@example.com"
					required>
				<div id="filterEmailFeedback" style="display:none;font-size:0.8rem;color:#dc3545;margin-top:2px">
					Please enter a valid email address.
				</div>
			</td>
		</tr>
		<tr>
			<th>Data Compression</th>
			<td><bean:define id="filters" name="filterActionForm"
					property="filterNameOptions" type="java.util.Collection" /> <html:select
					property="filter">
					<html:options collection="filters" property="name"
						labelProperty="name" />
				</html:select></td>
		</tr>
		<tr>
			<th>Date</th>
			<td><input type="text" name="date" id="date"></td>
		</tr>
		<tr>
			<th>Include Standby</th>
			<td><html:checkbox property="includeStdby" /></td>
		</tr>
	</table>
	</br>
	<button type="submit" id="filterSubmitBtn">Launch Compression Simulation</button>
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
		emailEl.style.borderColor = ok ? '' : '#dc3545';
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
				document.getElementById('filterDestInput').style.borderColor = '#dc3545';
			}
		}
	});

	document.getElementById('filterDestInput').addEventListener('input', function() {
		this.style.borderColor = '';
	});
})();
</script>