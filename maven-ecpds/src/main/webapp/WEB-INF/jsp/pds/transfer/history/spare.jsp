<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search" %>

<table class="spareBox">
 <tr><th colspan="2">Mode</th></tr>
 <tr><td colspan="2"></td></tr>

 <c:if test="${param['mode'] != 'productDate'}">
 <tr><td width="1"></td><td><a href="?destinationName=${destination.id}&mode=productDate&date=${param['date']}">Product Date</a></td></tr>
 <tr><td style="text-align:right;"><search:icon key="icon.small.arrow.right" writeFullTag="true"/></td><td style="background-color:#d5d5d5;">History Date</td></tr>
 </c:if>

  <c:if test="${param['mode'] == 'productDate'}">
  <tr><td style="text-align:right;"><search:icon key="icon.small.arrow.right" writeFullTag="true"/></td><td style="background-color:#d5d5d5;">Product Date</td></tr>
  <tr><td></td><td><a href="?destinationName=${destination.id}&mode=historyDate&date=${param['date']}">History Date</a></td></tr>
  </c:if>
 </table>
