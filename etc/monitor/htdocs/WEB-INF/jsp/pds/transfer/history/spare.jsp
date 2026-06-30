<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>

<table class="spareBox" id="mode-menu">
 <tr><th>Mode</th></tr>
 <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

 <c:if test="${param['mode'] != 'productDate'}">
  <tr><td><a href="?destinationName=${destination.id}&mode=productDate&date=${param['date']}"><i class="bi bi-tag"></i> Product Date</a></td></tr>
  <tr><td style="background-color:var(--bs-secondary-bg); padding-left:10px;"><i class="bi bi-clock-history"></i> History Date</td></tr>
 </c:if>

 <c:if test="${param['mode'] == 'productDate'}">
  <tr><td style="background-color:var(--bs-secondary-bg); padding-left:10px;"><i class="bi bi-tag"></i> Product Date</td></tr>
  <tr><td><a href="?destinationName=${destination.id}&mode=historyDate&date=${param['date']}"><i class="bi bi-clock-history"></i> History Date</a></td></tr>
 </c:if>
</table>
<script>
(function () {
    // On load: if a scroll anchor was stored, jump to it instantly before user notices
    var anchor = sessionStorage.getItem('scrollAnchor');
    if (anchor) {
        sessionStorage.removeItem('scrollAnchor');
        var el = document.getElementById(anchor);
        if (el) {
            requestAnimationFrame(function () {
                el.scrollIntoView({ behavior: 'instant', block: 'nearest' });
            });
        }
    }
    // On mode link click: store target so next page can restore position
    var menu = document.getElementById('mode-menu');
    if (menu) {
        menu.querySelectorAll('a').forEach(function (link) {
            link.addEventListener('click', function () {
                sessionStorage.setItem('scrollAnchor', 'mode-menu');
            });
        });
    }
})();
</script>
