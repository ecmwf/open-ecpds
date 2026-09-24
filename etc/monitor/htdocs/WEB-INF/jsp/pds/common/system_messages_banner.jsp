<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%-- System messages: time-bounded warning/maintenance banners, shown to every user on each menu page
     (Admin Tasks -> System Messages). Included just below each page's introduction card, above any other
     warning banners (e.g. Critical Password / TLS certificate attention). --%>
<c:forEach var="sysMsg" items="${activeSystemMessages}">
<div class="alert alert-${sysMsg.level == 'danger' ? 'danger' : sysMsg.level == 'info' ? 'info' : 'warning'} d-flex align-items-start gap-3 mb-4" role="alert">
    <i class="bi ${sysMsg.level == 'danger' ? 'bi-exclamation-octagon-fill' : sysMsg.level == 'info' ? 'bi-info-circle-fill' : 'bi-exclamation-triangle-fill'} flex-shrink-0 mt-1" style="font-size:1.2rem;"></i>
    <div>
        <div style="white-space:pre-wrap;"><c:out value="${sysMsg.message}"/></div>
        <div class="small mt-1" style="opacity:0.85;"><i class="bi bi-clock-history"></i> <c:out value="${sysMsg.formattedTimeframe}"/></div>
    </div>
</div>
</c:forEach>
