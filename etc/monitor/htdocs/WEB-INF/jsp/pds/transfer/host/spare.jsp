<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<auth:if basePathKey="host.basepath" paths="/edit/resetStats/">
    <auth:then>

<table class="editSpareBox">
    <tr>
        <th colspan="3" title="">
            <a title="${host.nickName} (${host.active ? 'Enabled' : 'Disabled'})"
               href='<bean:message key="host.basepath"/>/${host.id}'>${host.nickName}</a>
        </th>
    </tr>
    <tr><td colspan="3"></td></tr>

    <tr>
        <td><i class="bi bi-clock-history sidebar-icon"></i></td>
        <td colspan="2"><a href='<bean:message key="host.basepath"/>/${host.id}?mode=changelog'>Changes Log</a></td>
    </tr>

    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <tr>
                <td><i class="bi bi-terminal sidebar-icon"></i></td>
                <td colspan="2"><a href='<bean:message key="host.basepath"/>/edit/getOutput/view/${host.id}'>View Output</a></td>
            </tr>
            <tr>
                <td><i class="bi bi-sliders sidebar-icon"></i></td>
                <td colspan="2"><a href='<bean:message key="host.basepath"/>/edit/cleanDataWindow/${host.id}'>Clean Options</a></td>
            </tr>
            <tr>
                <td><i class="bi bi-arrow-counterclockwise sidebar-icon"></i></td>
                <td colspan="2"><a href='<bean:message key="host.basepath"/>/edit/resetStats/${host.id}'>Reset Stats</a></td>
            </tr>
        </auth:then>
    </auth:if>

    <tr>
        <td><i class="bi bi-wifi sidebar-icon"></i></td>
        <td colspan="2"><a href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}'>Network Info</a>
            <c:forEach var="destination" items="${host.destinations}">
                <c:forEach var="proxy" items="${destination.proxyHostsAndPriorities}">
                    <c:if test="${host.type == 'Dissemination'}">
                        <li><a class="menusubitem"
                               href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}/${proxy.name.id}'>${proxy.name.nickName}</a></li>
                    </c:if>
                </c:forEach>
            </c:forEach>
        </td>
    </tr>
</table>

    </auth:then>
</auth:if>
