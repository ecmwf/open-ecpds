<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%
    final var operationsUser = ecmwf.web.controller.ECMWFAction.getUser(request);
    if (request.getAttribute("_operationsNavRendered") == null && operationsUser != null
            && (operationsUser.hasAccess("/do/monitoring") || operationsUser.hasAccess("/do/monitoring/"))
            && operationsUser.hasAccess("/do/transfer/history/")) {
        request.setAttribute("_operationsNavRendered", true);
        ecmwf.ecpds.master.plugin.http.controller.admin.OperationalEventJournalAction.populateStatus(request, operationsUser);
%>

<table class="spareBox2">
    <tr><th><a href="/do/operations">Operations</a></th></tr>
    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
    <auth:link basePathKey="monitoring.basepath" href="" wrappingTags="tr,td"><i class="bi bi-grid-3x3-gap-fill"></i> Monitoring Overview</auth:link>
    <auth:link basePathKey="monitoring.basepath" href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&" wrappingTags="tr,td"><i class="bi bi-broadcast-pin"></i> Dissemination</auth:link>
    <auth:link basePathKey="monitoring.basepath" href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&" wrappingTags="tr,td"><i class="bi bi-cloud-arrow-down"></i> Acquisition</auth:link>
    <auth:link basePathKey="monitoring.basepath" href="/globe" wrappingTags="tr,td"><i class="bi bi-globe2"></i> Live Earth</auth:link>
    <auth:link basePathKey="monitoring.basepath" href="/topology" wrappingTags="tr,td"><i class="bi bi-diagram-3"></i> System Topology</auth:link>
    <auth:link basePathKey="monitoring.basepath" href="/mqtt" wrappingTags="tr,td"><i class="bi bi-broadcast"></i> MQTT Activity</auth:link>
    <jsp:include page="/WEB-INF/jsp/pds/admin/eventjournal/menu.jsp">
        <jsp:param name="journalPlacement" value="submenu" />
    </jsp:include>
    <auth:link basePathKey="monitoring.basepath" href="/delaynotification" wrappingTags="tr,td"><i class="bi bi-bell"></i> Delay Notification</auth:link>
</table>
<% } %>
