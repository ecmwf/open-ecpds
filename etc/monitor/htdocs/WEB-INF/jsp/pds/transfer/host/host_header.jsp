<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<c:if test="${not empty host.geoIpLocation}">
    <c:set var="_hGeoParts" value="${fn:split(host.geoIpLocation, '/')}"/>
    <c:set var="_hGeoPart0" value="${fn:trim(_hGeoParts[0])}"/>
    <c:set var="_hGeoPart1" value="${fn:trim(_hGeoParts[1])}"/>
    <c:set var="_hGeoIso" value="${fn:toLowerCase(fn:length(_hGeoPart0) == 2 ? _hGeoPart0 : _hGeoPart1)}"/>
</c:if>
<div class="dest-page-header mb-3">
        <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
					<c:if test="${not host.active}"><i class="bi bi-slash-circle-fill text-danger" title="Host is disabled" style="font-size:0.9rem;"></i></c:if>
                    <a class="dest-page-name text-decoration-none" href="/do/transfer/host/${host.name}"<c:if test="${not host.active}"> style="text-decoration:line-through !important;color:var(--bs-secondary-color)"</c:if>>${host.nickName}</a>
        <c:if test="${host.name != host.nickName}">
            <code class="dest-page-id" title="Host identifier">${host.name}</code>
        </c:if>
        <c:if test="${not empty host.geoIpLocation}">
            <span class="fi fi-${_hGeoIso}" title="${host.geoIpLocation}" style="font-size:1.2em;border-radius:2px;"></span>
        </c:if>
        <c:choose>
            <c:when test="${host.type == 'Dissemination'}">
                <span class="badge bg-secondary fs-status" title="Dissemination"><i class="bi bi-send-fill"></i></span>
            </c:when>
            <c:when test="${host.type == 'Acquisition'}">
                <span class="badge bg-secondary fs-status" title="Acquisition"><i class="bi bi-cloud-download-fill"></i></span>
            </c:when>
            <c:otherwise>
                <span class="badge bg-secondary fs-status">${host.type}</span>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty host.transferMethodName}">
            <span class="badge bg-info text-dark fs-status" title="${host.transferMethod.comment}"><i class="bi bi-hdd-network me-1"></i>${host.transferMethodName}</span>
        </c:if>
        <c:if test="${not empty host.filterName and host.filterName ne 'none'}">
            <jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${host.filterName}"/></jsp:include>
        </c:if>
        <div class="d-flex gap-2 ms-auto align-items-center">
        <auth:if basePathKey="host.basepath" paths="">
        <auth:then>
        <a href='<bean:message key="host.basepath"/>' class="btn btn-sm btn-outline-secondary" title="All Transfer Hosts"><i class="bi bi-arrow-left"></i></a>
        <div style="border-left:1px solid var(--bs-border-color);height:1.5rem;"></div>
        </auth:then>
        </auth:if>
        <c:set var="_hostHasEditGroup" value="false"/>
        <auth:if basePathKey="host.basepath" paths="/edit/insert_form">
        <auth:then>
        <c:set var="_hostHasEditGroup" value="true"/>
        <div class="d-flex gap-1 align-items-center">
            <a href='<bean:message key="host.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new host"><i class="bi bi-plus-circle"></i></a>
            <c:if test="${not empty host.id}">
            <a href='<bean:message key="host.basepath"/>/edit/update_form/${host.id}'
               class="btn btn-sm btn-outline-primary" title="Edit this host"><i class="bi bi-pencil"></i></a>
            <a href='<bean:message key="host.basepath"/>/edit/delete_form/${host.id}'
               class="btn btn-sm btn-outline-danger" title="Delete this host"><i class="bi bi-trash"></i></a>
            <c:if test="${not empty host.destinations}">
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Duplicate this host"
               onclick="ecpdsHostDuplicate('${host.id}','${host.nickName}');return false;"><i class="bi bi-copy"></i></a>
            </auth:then>
            </auth:if>
            </c:if>
            </c:if>
        </div>
        </auth:then>
        </auth:if>
        <auth:if basePathKey="host.basepath" paths="/edit/resetStats/">
        <auth:then>
        <c:if test="${not empty host.id}">
        <div class="d-flex gap-1 align-items-center"<c:if test="${_hostHasEditGroup}"> style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;"</c:if>>
            <a href='<bean:message key="host.basepath"/>/${host.id}?mode=changelog'
               class="btn btn-sm btn-outline-secondary" title="Changes Log"><i class="bi bi-clock-history"></i></a>
            <a href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}'
               class="btn btn-sm btn-outline-secondary" title="Network Info"><i class="bi bi-wifi"></i></a>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href='<bean:message key="host.basepath"/>/edit/getOutput/view/${host.id}'
               class="btn btn-sm btn-outline-secondary" title="View Output"><i class="bi bi-terminal"></i></a>
            <div style="border-left:1px solid var(--bs-border-color);height:1.5rem;"></div>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Clean Options"
               onclick="confirmationDialog({title:'Clean Options',message:'Clean the data window options for host <b>${host.nickName}</b>?<br/><br/>This will remove all options with default values from the option properties editor, simplifying the configuration. This action cannot be undone.',confirmText:'Clean',showLoading:true,onConfirm:function(){window.location.href='<bean:message key="host.basepath"/>/edit/cleanDataWindow/${host.id}'}}); return false;"><i class="bi bi-sliders"></i></a>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Reset Stats"
               onclick="confirmationDialog({title:'Reset Stats',message:'Reset transfer statistics for host <b>${host.nickName}</b>?<br/><br/>This will permanently clear all accumulated transfer counters (bytes sent, transfer counts, error counts, etc.). This action cannot be undone.',confirmText:'Reset',showLoading:true,onConfirm:function(){window.location.href='<bean:message key="host.basepath"/>/edit/resetStats/${host.id}'}}); return false;"><i class="bi bi-arrow-counterclockwise"></i></a>
            </auth:then>
            </auth:if>
        </div>
        </c:if>
        </auth:then>
        </auth:if>
        </div>
    </div>
<script>
function ecpdsHostDuplicate(hostId, nickName) {
    var destinations = [<c:forEach var="_d" items="${host.destinations}" varStatus="_s">'<c:out value="${_d.name}"/>'<c:if test="${!_s.last}">,</c:if></c:forEach>];
    var extra = '';
    if (destinations.length > 1) {
        var opts = destinations.map(function(n){return '<option value="'+n+'">'+n+'</option>';}).join('');
        extra = '<br><br>Select destination:<br><select id="ecpds-dup-dest" class="form-select form-select-sm mt-1">'+opts+'</select>';
    }
    confirmationDialog({
        title: 'Confirm Host Duplication',
        message: destinations.length === 1
            ? 'Are you sure you want to duplicate host <strong>'+nickName+'</strong> in destination <strong>'+destinations[0]+'</strong>?'
            : 'Are you sure you want to duplicate host <strong>'+nickName+'</strong>?'+extra,
        showLoading: true,
        onConfirm: function() {
            var dest = destinations.length === 1 ? destinations[0] : document.getElementById('ecpds-dup-dest').value;
            window.location.href = '/do/transfer/destination/operations/'+dest+'/duplicateHost/'+hostId;
        }
    });
}
</script>
    <c:if test="${not empty host.comment}">
        <p class="dest-page-comment">${host.comment}</p>
    </c:if>
    <c:if test="${not empty host.ECUserName}">
        <p class="mb-0 small text-muted">
            <i class="bi bi-person-fill me-1"></i><c:choose>
                <c:when test="${not empty host.userMail}"><a href="mailto:${host.userMail}" class="text-muted">${host.ECUserName}</a></c:when>
                <c:otherwise>${host.ECUserName}</c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>
