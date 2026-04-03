<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<div style="display:inline-block;position:relative" id="destPickerWrap">
    <input type="text" id="destPickerInput" class="form-control form-control-sm"
           placeholder="Search destination..." autocomplete="off"
           value="${selectedDestination.name}"
           style="width:240px"
           oninput="destPickerFilter()"
           onfocus="destPickerOpen()"
           onblur="setTimeout(destPickerClose, 200)">
    <ul id="destPickerList" class="list-unstyled border rounded bg-white shadow-sm mb-0"
        style="display:none;position:absolute;z-index:1050;width:240px;max-height:220px;overflow-y:auto;top:100%;left:0;padding:3px 0"></ul>
</div>
<script>
(function(){
    var opts = [<c:forEach var="d" items="${destinationOptions}" varStatus="s">{n:'${d.name}',u:'?mode=${param.mode}&destinationName=${d.name}'}${s.last?'':','}</c:forEach>];
    function render(q) {
        var list = document.getElementById('destPickerList');
        var html = '', lq = q ? q.toLowerCase() : '';
        opts.forEach(function(o){
            if (!lq || o.n.toLowerCase().indexOf(lq) >= 0) {
                html += '<li><a href="'+o.u+'" style="display:block;padding:3px 10px;font-size:0.875rem;color:inherit;text-decoration:none;white-space:nowrap" onmouseover="this.style.background=\'#e9ecef\'" onmouseout="this.style.background=\'\'">'+o.n+'</a></li>';
            }
        });
        list.innerHTML = html || '<li style="padding:3px 10px;font-size:0.875rem;color:#6c757d">No match</li>';
    }
    window.destPickerFilter = function(){ render(document.getElementById('destPickerInput').value); document.getElementById('destPickerList').style.display='block'; };
    window.destPickerOpen  = function(){ render(document.getElementById('destPickerInput').value); document.getElementById('destPickerList').style.display='block'; };
    window.destPickerClose = function(){ document.getElementById('destPickerList').style.display='none'; };
})();
</script>
