<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<c:if test="${metaDataSize == 0}">
    <div class="alert alert-info mt-2">
        No Metadata files available for this destination.
    </div>
</c:if>

<c:if test="${metaDataSize != 0}">

<div class="d-flex align-items-center gap-3 mb-3 flex-wrap">
    <span>
        <a href="<bean:message key="destination.basepath"/>?destinationSearch=country=${destination.country.iso}&amp;destinationStatus=&amp;destinationType=">
            <img src="https://flagcdn.com/24x18/${fn:toLowerCase(destination.countryIso)}.png"
                 onload="var m=this.src.match(/\/([a-z]{2})\./);if(!m||!window._validIso||!window._validIso.has(m[1].toUpperCase()))this.style.display='none';"
                 onerror="this.style.display='none'"
                 alt="Flag for ${destination.country.name}"
                 title="See all destinations in ${destination.country.name}"
                 style="vertical-align:middle">
        </a>
        <span class="ms-1">${destination.country.name}</span>
    </span>
    <c:if test="${not empty destination.ecUser.comment}">
        <span class="text-muted"><i class="bi bi-person-fill me-1"></i><c:out value="${destination.ecUser.comment}"/></span>
    </c:if>
    <span class="badge bg-secondary rounded-pill">${metaDataSize} metadata file<c:if test="${metaDataSize != 1}">s</c:if></span>
</div>

<c:forEach var="file" items="${metaData}">
<div class="card mb-3">
    <div class="card-header d-flex align-items-center justify-content-between flex-wrap gap-2">
        <span class="fw-semibold"><i class="bi bi-file-earmark-text me-1"></i><c:out value="${file.name}"/></span>
        <span class="d-flex align-items-center gap-2">
            <span class="badge bg-light text-dark border"><code style="font-size:0.8rem"><c:out value="${file.contentType}"/></code></span>
            <span class="text-muted small"><i class="bi bi-clock me-1"></i>${file.lastModificationDate}</span>
        </span>
    </div>
    <div class="card-body p-0">
        <c:if test="${file.contentType == 'text/plain'}">
            <pre class="m-0 p-3" style="white-space:pre-wrap;word-break:break-word;font-size:0.85rem;background:#f8f9fa;border-radius:0 0 0.375rem 0.375rem">${file.stringContent}</pre>
        </c:if>
        <c:if test="${file.contentType != 'text/plain'}">
            <div class="p-3">
                <content:content name="file" outputContentType="text/html" thumbnail="false" embedded="true"/>
            </div>
        </c:if>
    </div>
</div>
</c:forEach>

</c:if>
