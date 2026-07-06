<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<div style="position:relative;display:inline-flex;align-items:center" id="policyPickerWrap">
    <div class="input-group input-group-sm">
        <span class="input-group-text text-muted"><i class="bi bi-shield-check"></i></span>
        <input type="text" id="policyPickerInput" class="form-control" style="width:160px"
               placeholder="Search policy..." autocomplete="off"
               value="<c:out value="${policyNameForSearch != 'Any Policy' ? policyNameForSearch : ''}"/>"
               oninput="policyPickerFilter()"
               onfocus="policyPickerOpen()"
               onblur="setTimeout(policyPickerClose, 200)"
               onkeydown="policyPickerKey(event)">
    </div>
    <ul id="policyPickerDropdown" role="listbox"
        class="list-unstyled border rounded bg-body shadow-sm mb-0"
        style="display:none;position:absolute;z-index:1050;min-width:260px;max-height:260px;overflow-y:auto;top:100%;left:0;margin-top:2px;padding:3px 0"></ul>
</div>

<%-- Hidden data list: JSP renders server-side, JS reads from DOM --%>
<ul id="policyPickerData" style="display:none">
    <c:forEach var="pol" items="${policyOptions}">
        <li data-id="<c:out value="${pol}"/>"></li>
    </c:forEach>
</ul>

<script>
(function(){
    var opts = Array.from(document.querySelectorAll('#policyPickerData li')).map(function(el) {
        return el.dataset.id;
    });
    var active = -1, filtered = [];

    function esc(s) {
        return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    function render(q) {
        var drop = document.getElementById('policyPickerDropdown');
        var lq = q ? q.toLowerCase() : '';
        filtered = lq ? opts.filter(function(o) {
            return o.toLowerCase().indexOf(lq) >= 0;
        }) : opts.slice();
        active = -1;
        if (!filtered.length) {
            drop.innerHTML = '<li style="padding:4px 10px;font-size:0.875rem;color:var(--bs-secondary-color)">No match</li>';
            return;
        }
        drop.innerHTML = filtered.map(function(o, i) {
            return '<li role="option" data-idx="' + i + '"'
                 + ' style="padding:5px 10px;cursor:pointer;line-height:1.3"'
                 + ' onmouseover="policyHover(this)" onmousedown="policyGo(' + i + ')">'
                 + esc(o) + '</li>';
        }).join('');
    }

    function setActive(idx) {
        var items = document.querySelectorAll('#policyPickerDropdown li[data-idx]');
        items.forEach(function(el) { el.style.background = ''; });
        active = (idx >= 0 && idx < items.length) ? idx : -1;
        if (active >= 0) { items[active].style.background = 'var(--bs-secondary-bg)'; items[active].scrollIntoView({ block: 'nearest' }); }
    }

    window.policyPickerFilter = function() {
        render(document.getElementById('policyPickerInput').value);
        document.getElementById('policyPickerDropdown').style.display = 'block';
    };
    window.policyPickerOpen = function() {
        render(document.getElementById('policyPickerInput').value);
        document.getElementById('policyPickerDropdown').style.display = 'block';
    };
    window.policyPickerClose = function() { document.getElementById('policyPickerDropdown').style.display = 'none'; };
    window.policyHover = function(el) { setActive(parseInt(el.dataset.idx, 10)); };
    window.policyGo = function(idx) {
        if (filtered[idx]) {
            if (window.onPolicySelected) { window.onPolicySelected(filtered[idx]); return; }
            var url = new URL(window.location.href);
            if (filtered[idx] === 'Any Policy') url.searchParams.delete('policyNameForSearch');
            else url.searchParams.set('policyNameForSearch', filtered[idx]);
            window.location.href = url.toString();
        }
    };
    window.policyPickerKey = function(e) {
        var drop = document.getElementById('policyPickerDropdown');
        var isOpen = drop.style.display !== 'none';
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (!isOpen) { render(document.getElementById('policyPickerInput').value); drop.style.display = 'block'; }
            setActive(Math.min(active + 1, filtered.length - 1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActive(Math.max(active - 1, 0));
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (active >= 0 && filtered[active]) policyGo(active);
            else if (filtered.length === 1) policyGo(0);
        } else if (e.key === 'Escape') {
            drop.style.display = 'none';
        }
    };
    // Clear button: pressing × on input (Escape key handled above; also clear on empty+Enter)
    document.getElementById('policyPickerInput').addEventListener('keyup', function(e) {
        if (e.key === 'Escape' || (e.key === 'Enter' && this.value === '')) {
            if (window.onPolicySelected) { window.onPolicySelected(''); return; }
            var url = new URL(window.location.href);
            url.searchParams.delete('policyNameForSearch');
            window.location.href = url.toString();
        }
    });
})();
</script>
