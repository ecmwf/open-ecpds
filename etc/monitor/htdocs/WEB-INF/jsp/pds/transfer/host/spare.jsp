<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<auth:if basePathKey="host.basepath" paths="/edit/resetStats/">
    <auth:then>

<table class="spareBox2">
    <tr>
        <th>
            <a title="${host.nickName} (${host.active ? 'Enabled' : 'Disabled'})"
               href='<bean:message key="host.basepath"/>/${host.id}'>Host</a>
        </th>
    </tr>
    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

    <%-- Create / Edit / Delete / Duplicate --%>
    <auth:if basePathKey="host.basepath" paths="/edit/insert_form">
    <auth:then>
        <tr><td><auth:link basePathKey="host.basepath" href="/edit/insert_form"><i class="bi bi-plus-circle sidebar-icon"></i> Create</auth:link></td></tr>
        <c:if test="${not empty host.id}">
        <tr><td><auth:link basePathKey="host.basepath" href="/edit/update_form/${host.id}"><i class="bi bi-pencil sidebar-icon"></i> Edit</auth:link></td></tr>
        <tr><td><auth:link basePathKey="host.basepath" href="/edit/delete_form/${host.id}"><i class="bi bi-trash sidebar-icon"></i> Delete</auth:link></td></tr>
        <c:if test="${not empty host.destinations}">
        <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
        <tr><td><a href="#" onclick="ecpdsHostDuplicate('${host.id}','${host.nickName}');return false;"><i class="bi bi-copy sidebar-icon"></i> Duplicate</a></td></tr>
        </auth:then>
        </auth:if>
        </c:if>
        </c:if>
        <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
    </auth:then>
    </auth:if>

    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <tr><td><a href='<bean:message key="host.basepath"/>/${host.id}?mode=changelog'><i class="bi bi-clock-history sidebar-icon"></i> Changes Log</a></td></tr>
        </auth:then>
        <auth:else>
            <c:if test="${host.type != 'Proxy'}">
            <tr><td><a href='<bean:message key="host.basepath"/>/${host.id}?mode=changelog'><i class="bi bi-clock-history sidebar-icon"></i> Changes Log</a></td></tr>
            </c:if>
        </auth:else>
    </auth:if>

    <auth:if basePathKey="host.basepath" paths="/edit/getOutput/">
        <auth:then>
            <c:choose>
            <c:when test="${host.type == 'Acquisition'}">
            <tr><td><a href='<bean:message key="host.basepath"/>/edit/getOutput/view/${host.id}'><i class="bi bi-terminal sidebar-icon"></i> Acquisition Console</a></td></tr>
            </c:when>
            <c:otherwise>
            <tr><td><span class="sidebar-disabled-item" title="Acquisition Console (Acquisition hosts only)"><i class="bi bi-terminal sidebar-icon"></i> Acquisition Console</span></td></tr>
            </c:otherwise>
            </c:choose>
        </auth:then>
    </auth:if>

    <tr><td><a href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}'><i class="bi bi-wifi sidebar-icon"></i> Network Info</a></td></tr>

    <c:choose>
        <c:when test="${not empty moduleGuide}">
            <tr><td><a href="#" onclick="var el=document.getElementById('moduleGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;"><i class="bi bi-book sidebar-icon"></i> Configuration Guide</a></td></tr>
        </c:when>
        <c:otherwise>
            <tr><td><span class="sidebar-disabled-item" title="No configuration guide available for this module"><i class="bi bi-book sidebar-icon"></i> Configuration Guide</span></td></tr>
        </c:otherwise>
    </c:choose>
    <c:if test="${host.type == 'Dissemination'}">
        <tr>
            <td style="border-top:none; padding-bottom:6px;">
                <div class="d-flex flex-wrap gap-1" style="padding: 2px 12px 0 16px;">
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

    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
            <tr>
                <td><a href="#" onclick="confirmationDialog({
                    title: 'Clean Options',
                    message: 'Clean the data window options for host <b>${host.nickName}</b>?<br/><br/>This will remove all options with default values from the option properties editor, simplifying the configuration. This action cannot be undone.',
                    confirmText: 'Clean',
                    showLoading: true,
                    onConfirm: function() { window.location.href='<bean:message key="host.basepath"/>/edit/cleanDataWindow/${host.id}'; }
                }); return false;"><i class="bi bi-sliders sidebar-icon"></i> Clean Options</a></td>
            </tr>
            <tr>
                <td><a href="#" onclick="confirmationDialog({
                    title: 'Reset Stats',
                    message: 'Reset transfer statistics for host <b>${host.nickName}</b>?<br/><br/>This will permanently clear all accumulated transfer counters (bytes sent, transfer counts, error counts, etc.). This action cannot be undone.',
                    confirmText: 'Reset',
                    showLoading: true,
                    onConfirm: function() { window.location.href='<bean:message key="host.basepath"/>/edit/resetStats/${host.id}'; }
                }); return false;"><i class="bi bi-arrow-counterclockwise sidebar-icon"></i> Reset Stats</a></td>
            </tr>
        </auth:then>
    </auth:if>

</table>

    </auth:then>
</auth:if>
