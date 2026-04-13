<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
    <c:set var="destParam" value="destinationNameForSearch" scope="request"/>
    <tiles:insert name="destination.select" />
    <form method="GET" class="m-0">
        <input type="hidden" name="destinationNameForSearch" value="<c:out value="${destinationNameForSearch}"/>">
        <div class="input-group input-group-sm">
            <input type="text" class="form-control" name="search"
                   placeholder="Search login..." autocomplete="off"
                   title="Search is performed across the Name (case-insensitive)"
                   value="<c:out value="${param['search']}"/>">
            <button class="btn btn-outline-secondary" type="submit" title="Search">
                <i class="bi bi-search"></i>
            </button>
        </div>
    </form>
</div>

<c:if test="${empty users}">
    <div class="alert alert-info">No Data Users found based on these criteria.</div>
</c:if>

<c:if test="${not empty users}">
<table id="usersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Data Login</th>
            <th>Comment</th>
            <th>Country</th>
            <th class="text-center">Enabled</th>
            <th class="text-center">TOTP</th>
            <th class="text-center">Anonymous</th>
            <th class="text-center">Sessions</th>
            <th class="text-center no-sort">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="user" items="${users}">
        <tr>
            <td><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></td>
            <td>${user.comment}</td>
            <td>
                <span class="fi fi-${fn:toLowerCase(user.country.iso)} me-1"
                      title="${user.country.name}" style="font-size:1.1em;vertical-align:middle"></span>
                ${user.country.name}
            </td>
            <td class="text-center" data-order="${user.active ? 1 : 0}">
                <c:if test="${user.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!user.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
            </td>
            <td class="text-center" data-order="${user.isSynchronized ? 1 : 0}">
                <c:if test="${user.isSynchronized}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
                <c:if test="${!user.isSynchronized}"><i class="bi bi-dash text-muted" title="No"></i></c:if>
            </td>
            <td class="text-center" data-order="${user.anonymous ? 1 : 0}">
                <c:if test="${user.anonymous}"><i class="bi bi-exclamation-circle-fill text-warning" title="Yes"></i></c:if>
                <c:if test="${!user.anonymous}"><i class="bi bi-dash text-muted" title="No"></i></c:if>
            </td>
            <td class="text-center">${fn:length(user.incomingConnections)}</td>
            <td class="text-center">
                <auth:link styleClass="menuitem" href="/do/user/incoming/edit/update_form/${user.id}" imageKey="icon.small.update"/>
                <auth:link styleClass="menuitem" href="/do/user/incoming/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#usersTable').DataTable({
        paging:    true,
        pageLength: 25,
        searching: false,
        ordering:  true,
        info:      true,
        columnDefs: [{ orderable: false, targets: 'no-sort' }]
    });
});
</script>
</c:if>

