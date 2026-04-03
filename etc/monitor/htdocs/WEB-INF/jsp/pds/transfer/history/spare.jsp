<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>

<table class="spareBox" id="mode-menu">
 <tr><th colspan="2">Mode</th></tr>
 <tr><td colspan="2"></td></tr>

 <c:if test="${param['mode'] != 'productDate'}">
  <tr><td colspan="2"><a href="?destinationName=${destination.id}&mode=productDate&date=${param['date']}"><i class="bi bi-tag"></i> Product Date</a></td></tr>
  <tr><td colspan="2" style="background-color:#d5d5d5; padding-left:10px;"><i class="bi bi-clock-history"></i> History Date</td></tr>
 </c:if>

 <c:if test="${param['mode'] == 'productDate'}">
  <tr><td colspan="2" style="background-color:#d5d5d5; padding-left:10px;"><i class="bi bi-tag"></i> Product Date</td></tr>
  <tr><td colspan="2"><a href="?destinationName=${destination.id}&mode=historyDate&date=${param['date']}"><i class="bi bi-clock-history"></i> History Date</a></td></tr>
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
