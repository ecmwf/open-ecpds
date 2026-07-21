<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="authorized" value="false" />

<auth:if basePathKey="destination.basepath" paths="/">
    <auth:then><c:set var="authorized" value="true" /></auth:then>
</auth:if>

<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then></auth:then>
    <auth:else>
        <auth:if basePathKey="destination.basepath" paths="/${destination.name}">
            <auth:then><c:set var="authorized" value="true" /></auth:then>
        </auth:if>
    </auth:else>
</auth:if>

<c:if test="${authorized == 'true'}">
<auth:if basePathKey="monitoring.basepath" paths="">
<auth:then>

<c:if test="${not empty destinationDetailActionForm}">
    <c:set var="date" value="${destinationDetailActionForm.date}" />
</c:if>
<c:if test="${empty destinationDetailActionForm}">
    <c:set var="date" value="${param['date']}" />
</c:if>
<c:set var="mode" value="${param['mode']}" />

<c:if test="${not empty destination.id}">
<table class="spareBox2">
    <tr>
        <th>
            <a title="${destination.id} (${destination.formattedStatus})" href='<bean:message key="destination.basepath"/>/${destination.id}'>Destination</a>
        </th>
    </tr>
    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

    <%-- Create / Edit / Delete / Duplicate --%>
    <auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
    <auth:then>
        <tr><td><auth:link basePathKey="destination.basepath" href="/edit/insert_form"><i class="bi bi-plus-circle sidebar-icon"></i> Create</auth:link></td></tr>
        <tr><td><auth:link basePathKey="destination.basepath" href="/edit/update_form/${destination.id}"><i class="bi bi-pencil sidebar-icon"></i> Edit</auth:link></td></tr>
        <tr><td><auth:link basePathKey="destination.basepath" href="/edit/delete_form/${destination.id}"><i class="bi bi-trash sidebar-icon"></i> Delete</auth:link></td></tr>
        <tr><td><auth:link basePathKey="destination.basepath" href="/edit/insert_form?fromDestination=${destination.name}"><i class="bi bi-copy sidebar-icon"></i> Duplicate</auth:link></td></tr>
        <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
    </auth:then>
    </auth:if>

    <auth:if basePathKey="destination.basepath" paths="/${destination.id}">
        <auth:then>
            <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=parameters' style="display:inline-flex;align-items:center;gap:0.3rem;"><i class="bi bi-sliders sidebar-icon"></i> Parameters<c:if test="${destPropErrors}"><span class="badge bg-danger" style="font-size:0.6rem;padding:2px 4px;line-height:1;" title="Properties contain errors"><i class="bi bi-exclamation-circle-fill"></i></span></c:if></a></td></tr>
        </auth:then>
    </auth:if>

    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=datausers'><i class="bi bi-person-badge sidebar-icon"></i> Data Users<span id="_destDataUsersSidebarBadge"></span></a></td></tr>
        </auth:then>
    </auth:if>

    <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=traffic'><i class="bi bi-graph-up sidebar-icon"></i> Data Rates</a></td></tr>

    <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=changelog'><i class="bi bi-clock-history sidebar-icon"></i> Changes Log</a></td></tr>

    <tr><td><a href='<bean:message key="monitoring.timeline.basepath"/>/${destination.id}?date=${date}'><i class="bi bi-calendar3 sidebar-icon"></i> Transfer Timeline</a></td></tr>
    <tr><td><a href='/do/monitoring/unsuccessful/${destination.id}'><i class="bi bi-hourglass-split sidebar-icon"></i> Outstanding</a></td></tr>

    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <tr><td><a href='<bean:message key="transferhistory.basepath"/>?destinationName=${destination.id}&date=${date}&fromDestination=true'><i class="bi bi-archive sidebar-icon"></i> Transfer History</a></td></tr>
        </auth:then>
    </auth:if>

    <auth:if basePathKey="destination.basepath" paths="/metadata/${destination.id}">
        <auth:then>
            <tr><td><a href='<bean:message key="destination.basepath"/>/metadata/${destination.id}'><i class="bi bi-paperclip sidebar-icon"></i> Metadata</a></td></tr>
        </auth:then>
    </auth:if>

    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

    <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=aliasgraph'><i class="bi bi-diagram-3 sidebar-icon"></i> Alias Graph</a></td></tr>

    <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=aliasesfrom'><i class="bi bi-arrow-left-circle sidebar-icon"></i> Aliased From</a></td></tr>

    <tr><td><a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=aliasesto'><i class="bi bi-arrow-right-circle sidebar-icon"></i> Aliases To</a></td></tr>

    <c:if test="${destination.monitoringStatus.present}">
        <c:if test="${empty time}"><c:set var="time" value="00" /></c:if>
    </c:if>

</table>
</c:if><%-- end: not empty destination.id (guards whole table) --%>

<%-- Products: separate menu table, product names in tooltips only --%>
<c:if test="${destination.monitoringStatus.present and not empty products}">
    <table class="spareBox2 spb-products">
        <tr><th><i class="bi bi-box-seam sidebar-icon"></i> Products</th></tr>
        <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
        <c:forEach var="product" items="${products}">
            <c:set var="key" value="${product.name}@${product.value}" />
            <c:if test="${destination.statusMapForProducts[key].present}">
                <c:set var="aStatus" value="${destination.statusMapForProducts[key].arrivalStatus}" />
                <c:set var="tStatus" value="${destination.statusMapForProducts[key].transferStatus}" />
                <tr>
                    <td style="padding: 5px 10px;">
                    <auth:if basePathKey="transferhistory.basepath" paths="/">
                        <auth:then>
                            <a class="mon-letter mon-letter-s${aStatus lt 0 ? '0' : aStatus}"
                               title="${product.value}-${product.name} Arrival (status ${aStatus})"
                               href="/do/monitoring/arrival/${destination.id}/${product.name}/${product.value}?mode=${mode}&date=${date}">a</a>
                        </auth:then>
                    </auth:if>
                    <a class="mon-letter mon-letter-s${tStatus lt 0 ? '0' : tStatus}"
                       title="${product.value}-${product.name} Transfer (status ${tStatus})"
                       href="/do/monitoring/transfer/${destination.id}/${product.name}/${product.value}?mode=${mode}&date=${date}">t</a>
                    </td>
                </tr>
            </c:if>
        </c:forEach>
    </table>
</c:if>

<%-- Times: separate menu table --%>
<c:if test="${not empty times}">
    <table class="spareBox2 spb-times">
        <tr><th><i class="bi bi-clock sidebar-icon"></i> Times</th></tr>
        <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
        <tr>
            <td style="padding: 8px 12px;">
                <div class="d-flex flex-wrap gap-1">
                <c:forEach var="time" items="${times}">
                    <a class="badge text-decoration-none bg-secondary bg-opacity-25 text-dark border"
                       style="font-size:0.75rem; font-weight:500;"
                       href="${time}?mode=${mode}&date=${date}">${time}</a>
                </c:forEach>
                </div>
            </td>
        </tr>
    </table>
</c:if>

</auth:then>
</auth:if>
</c:if>
