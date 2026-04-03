<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<script>
	// Refresh the page with the given period. Set additional reloads in case the server is down.
	var refresh = ${monSesForm.refreshPeriod};
	setTimeout(function() {
		window.location.reload(true);
	}, refresh * 1000);
</script>

	<table border="0" cellspacing="2" cellpadding="2">
	  <tr valign="top">
	    <td align="left">
	      <table border="0" cellspacing="0" cellpadding="0">
	        <tr>
	          <c:forEach var="pro" items="${reqData.productWindowHeader}" varStatus="fStatus">
		    <c:if test="${not empty productStatus && productStatus.product==pro.product && productStatus.time==pro.time}"><c:set var="selected" value="selected"/></c:if>
		    <td width="140px">
		      <table border=0 class="products" style="white-space: nowrap">
		        <tr>
		          <td align="right" width="14px" class="icon"><img width="12" height="12" src="/assets/images/ecpds/g${pro.generationStatus}.png" border="0" title="Product Monitoring Status: ${pro.generationStatusFormattedCode}"></td>
		          <td align="left" width="130px" class="${selected}"><a href="/do/monitoring/summary/${pro.product}/${pro.time}" title="Scheduled for <content:content name="pro.scheduledTime" ignoreNull="true" defaultValue="*" dateFormatKey="date.format.medium"/>">${pro.time}-${pro.product}</a></td>
		          <c:remove var="selected"/>
		        </tr>
                      </table>
                    </td>
		  <c:if test="${(fStatus.index % 10) == 9 && not fStatus.last}"></tr><tr></c:if>
	          </c:forEach>
	        </tr>
	      </table>
	    </td>
	    <td align="left">
	      <form name="refresh" action="" method="GET" style="display:inline">
	        <b><font size="+1"><content:content name="monSesForm.updated" dateFormatKey="date.format.time" ignoreNull="true" defaultValue="*"/></font></b>
<div class="d-flex align-items-center gap-1 flex-shrink-0 mt-1">
<i class="bi bi-arrow-clockwise text-muted me-1" style="font-size:0.85rem;" title="Auto-refresh interval"></i>
<a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 30 ? 'active' : ''}" data-value="30">30s</a>
<a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 60 ? 'active' : ''}" data-value="60">1m</a>
<a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 300 ? 'active' : ''}" data-value="300">5m</a>
<a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 600 ? 'active' : ''}" data-value="600">10m</a>
<a href="#" class="date-pill mon-refresh-pill ${monSesForm.refreshPeriod == 1800 ? 'active' : ''}" data-value="1800">30m</a>
</div>
<script>
document.querySelectorAll('.mon-refresh-pill').forEach(function(pill) {
pill.addEventListener('click', function(e) {
e.preventDefault();
var params = new URLSearchParams(window.location.search);
params.set('refreshPeriod', this.dataset.value);
window.location.href = '?' + params.toString();
});
});
</script>
	      </form>
	      <c:if test="${not productStatus.calculated}">
	      <table border="0" cellspacing="0" cellpadding="0">
	        <tr>
	          <th>page&nbsp;</th>
	            <c:forEach var="page" items="${reqData.pages}">
	              <td class="nowrap">
	                <c:if test="${reqData.page == page}"><font size="-1">[${page}]&nbsp;</font></c:if>
	                <c:if test="${reqData.page != page}"><a class="menuitem" title="Display Only Page ${page}" href="?page=${page}"><font size="-1">${page}&nbsp;</font></a></c:if>
	              </td>
	            </c:forEach>
	          <td class="nowrap">
	            <c:if test="${empty reqData.page}"><font size="-1">[All]</font></c:if>
	            <c:if test="${not empty reqData.page}"><a class="menuitem" href="?page=" title="Display All Pages"><font size="-1">All</font></a></c:if>
	          </td>
	        </tr>
	      </table>
	      </c:if>
	    </td>
	  </tr>
	</table>
