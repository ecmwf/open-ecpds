<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%-- destParam: request-scoped attribute set by the including page to control which URL
     query param is replaced on selection. Defaults to "destinationName" (history page).
     For the Data Users page, callers set destParam="destinationNameForSearch". --%>
<c:set var="_dp" value="${not empty destParam ? destParam : 'destinationName'}"/>

<div style="position:relative;display:inline-flex;align-items:center" id="destPickerWrap">
    <div class="input-group input-group-sm">
        <span class="input-group-text bg-white text-muted"><i class="bi bi-search"></i></span>
        <input type="text" id="destPickerInput" class="form-control" style="width:280px"
               placeholder="Search destination..." autocomplete="off"
               value="<c:out value="${selectedDestination.name}"/>"
               oninput="destPickerFilter()"
               onfocus="destPickerOpen()"
               onblur="setTimeout(destPickerClose, 200)"
               onkeydown="destPickerKey(event)">
    </div>
    <ul id="destPickerDropdown" role="listbox"
        class="list-unstyled border rounded bg-white shadow-sm mb-0"
        style="display:none;position:absolute;z-index:1050;min-width:360px;max-height:260px;overflow-y:auto;top:100%;left:0;margin-top:2px;padding:3px 0"></ul>
</div>

<%-- Hidden data list: JSP renders server-side, JS reads from DOM (safe HTML encoding via c:out) --%>
<ul id="destPickerData" style="display:none">
    <c:forEach var="d" items="${destinationOptions}">
        <li data-name="<c:out value="${d.name}"/>"
            data-label="<c:out value="${d.value}"/>"></li>
    </c:forEach>
</ul>

<script>
(function(){
    var _dp = '<c:out value="${_dp}"/>';
    var opts = Array.from(document.querySelectorAll('#destPickerData li')).map(function(el) {
        return { n: el.dataset.name, l: el.dataset.label };
    });
    var active = -1, filtered = [];

    function esc(s) {
        return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    function render(q) {
        var drop = document.getElementById('destPickerDropdown');
        var lq = q ? q.toLowerCase() : '';
        filtered = lq ? opts.filter(function(o) {
            return o.n.toLowerCase().indexOf(lq) >= 0 || o.l.toLowerCase().indexOf(lq) >= 0;
        }) : opts.slice();
        active = -1;
        if (!filtered.length) {
            drop.innerHTML = '<li style="padding:4px 10px;font-size:0.875rem;color:#6c757d">No match</li>';
            return;
        }
        drop.innerHTML = filtered.map(function(o, i) {
            var label = o.l ? '<br><small class="text-muted" style="font-size:0.78rem">' + esc(o.l) + '</small>' : '';
            return '<li role="option" data-idx="' + i + '"'
                 + ' style="padding:5px 10px;cursor:pointer;line-height:1.3"'
                 + ' onmouseover="destHover(this)" onmousedown="destGo(' + i + ')">'
                 + esc(o.n) + label + '</li>';
        }).join('');
    }

    function setActive(idx) {
        var items = document.querySelectorAll('#destPickerDropdown li[data-idx]');
        items.forEach(function(el) { el.style.background = ''; });
        active = (idx >= 0 && idx < items.length) ? idx : -1;
        if (active >= 0) { items[active].style.background = '#e9ecef'; items[active].scrollIntoView({ block: 'nearest' }); }
    }

    window.destPickerFilter = function() {
        render(document.getElementById('destPickerInput').value);
        document.getElementById('destPickerDropdown').style.display = 'block';
    };
    window.destPickerOpen = function() {
        render(document.getElementById('destPickerInput').value);
        document.getElementById('destPickerDropdown').style.display = 'block';
    };
    window.destPickerClose = function() { document.getElementById('destPickerDropdown').style.display = 'none'; };
    window.destHover = function(el) { setActive(parseInt(el.dataset.idx, 10)); };
    window.destGo = function(idx) {
        if (filtered[idx]) {
            var url = new URL(window.location.href);
            url.searchParams.set(_dp, filtered[idx].n);
            window.location.href = url.toString();
        }
    };
    window.destPickerKey = function(e) {
        var drop = document.getElementById('destPickerDropdown');
        var isOpen = drop.style.display !== 'none';
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (!isOpen) { render(document.getElementById('destPickerInput').value); drop.style.display = 'block'; }
            setActive(Math.min(active + 1, filtered.length - 1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActive(Math.max(active - 1, 0));
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (active >= 0 && filtered[active]) destGo(active);
            else if (filtered.length === 1) destGo(0);
        } else if (e.key === 'Escape') {
            drop.style.display = 'none';
        }
    };
})();
</script>
