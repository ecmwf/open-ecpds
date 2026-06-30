<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<!-- tiles/pds/transfer/data/spare.jsp -->

<table class="spareBox2">
    <tr>
        <th>Data Transfer</th>
    </tr>
    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

    <auth:if basePathKey="destination.basepath"
        paths="/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}">
    <auth:then>
        <c:if test="${!datatransfer.deleted}">
        <tr><td><auth:link basePathKey="destination.basepath"
            href="/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}">
            <i class="bi bi-trash sidebar-icon"></i> Delete</auth:link></td></tr>
        </c:if>
    </auth:then>
    </auth:if>

    <c:if test="${datatransfer.canBeDownloaded}">
    <tr><td><a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/download/${datatransfer.id}'>
        <i class="bi bi-download sidebar-icon"></i> Download</a></td></tr>
    </c:if>

    <tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>

    <c:choose>
    <c:when test="${not empty showScheduleNow}">
    <tr><td><a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/scheduleNow/${datatransfer.id}'>
        <i class="bi bi-calendar-check sidebar-icon"></i> Schedule Now</a></td></tr>
    </c:when>
    <c:otherwise>
    <tr><td><span class="sidebar-disabled-item" title="Schedule Now not available">
        <i class="bi bi-calendar-check sidebar-icon"></i> Schedule Now</span></td></tr>
    </c:otherwise>
    </c:choose>

    <c:choose>
    <c:when test="${datatransfer.canBeRequeued}">
    <tr><td><a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/requeue/${datatransfer.id}'>
        <i class="bi bi-arrow-repeat sidebar-icon"></i> Requeue</a></td></tr>
    </c:when>
    <c:otherwise>
    <tr><td><span class="sidebar-disabled-item" title="Requeue not available">
        <i class="bi bi-arrow-repeat sidebar-icon"></i> Requeue</span></td></tr>
    </c:otherwise>
    </c:choose>

    <c:choose>
    <c:when test="${datatransfer.statusCode == 'FETC'}">
    <tr><td><a href='<bean:message key="destination.basepath"/>/operations/${datatransfer.destinationName}/interrupt/${datatransfer.id}'>
        <i class="bi bi-stop-circle sidebar-icon"></i> Interrupt Retrieval</a></td></tr>
    </c:when>
    <c:otherwise>
    <tr><td><span class="sidebar-disabled-item" title="Interrupt Retrieval not available">
        <i class="bi bi-stop-circle sidebar-icon"></i> Interrupt Retrieval</span></td></tr>
    </c:otherwise>
    </c:choose>

</table>

<!-- End of tiles/pds/transfer/data/spare.jsp -->
