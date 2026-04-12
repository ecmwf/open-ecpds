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
                <td colspan="2"><a href="#" onclick="confirmationDialog({
                    title: 'Clean Options',
                    message: 'Clean the data window options for host <b>${host.nickName}</b>?<br/><br/>This will remove all options with default values from the option properties editor, simplifying the configuration. This action cannot be undone.',
                    confirmText: 'Clean',
                    showLoading: true,
                    onConfirm: function() { window.location.href='<bean:message key="host.basepath"/>/edit/cleanDataWindow/${host.id}'; }
                }); return false;">Clean Options</a></td>
            </tr>
            <tr>
                <td><i class="bi bi-arrow-counterclockwise sidebar-icon"></i></td>
                <td colspan="2"><a href="#" onclick="confirmationDialog({
                    title: 'Reset Stats',
                    message: 'Reset transfer statistics for host <b>${host.nickName}</b>?<br/><br/>This will permanently clear all accumulated transfer counters (bytes sent, transfer counts, error counts, etc.). This action cannot be undone.',
                    confirmText: 'Reset',
                    showLoading: true,
                    onConfirm: function() { window.location.href='<bean:message key="host.basepath"/>/edit/resetStats/${host.id}'; }
                }); return false;">Reset Stats</a></td>
            </tr>
        </auth:then>
    </auth:if>

    <tr>
        <td><i class="bi bi-wifi sidebar-icon"></i></td>
        <td colspan="2"><a href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}'>Network Info</a></td>
    </tr>
    <c:if test="${host.type == 'Dissemination'}">
        <tr>
            <td colspan="3" style="border-top:none; padding-bottom:6px;">
                <div class="d-flex flex-wrap gap-1 justify-content-center">
                <c:forEach var="destination" items="${host.destinations}">
                    <c:forEach var="proxy" items="${destination.proxyHostsAndPriorities}">
                        <a class="badge bg-secondary text-white text-decoration-none"
                           href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}/${proxy.name.id}'
                           title="via ${proxy.name.nickName}">${proxy.name.nickName}</a>
                    </c:forEach>
                </c:forEach>
                </div>
            </td>
        </tr>
    </c:if>
</table>

    </auth:then>
</auth:if>
