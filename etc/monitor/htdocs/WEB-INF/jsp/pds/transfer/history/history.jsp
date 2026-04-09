<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />
<div class="mb-2"></div>
<tiles:insert name="destination.select" />
<script>
(function() {
    var destName = new URLSearchParams(window.location.search).get('destinationName');
    if (!destName) return;
    document.querySelectorAll('.date-pill').forEach(function(pill) {
        var href = pill.getAttribute('href') || '';
        if (href.indexOf('destinationName') === -1) {
            pill.setAttribute('href', href + '&destinationName=' + encodeURIComponent(destName));
        }
    });
})();
</script>

<c:if test="${historyItemsSize == '0'}">
    <div class="alert alert-info mt-3">
        No Transfer History available for Destination
        <strong><c:out value="${selectedDestination.name}" /></strong>
        on <c:out value="${selectedDate}" />.
    </div>
</c:if>

<c:if test="${historyItemsSize != '0'}">
<div class="mt-3">
<display:table id="history" name="${historyItems}" requestURI=""
        sort="external" defaultsort="2" partialList="true"
        size="${historyItemsSize}" pagesize="${recordsPerPage}"
        class="listing">

    <display:column title="Error" sortable="true" class="text-center" headerClass="text-center" style="width:5em">
        <c:if test="${history.error}">
            <i class="bi bi-x-circle-fill text-danger" title="Error"></i>
        </c:if>
        <c:if test="${not history.error}">
            <i class="bi bi-check-circle-fill text-success" title="OK"></i>
        </c:if>
    </display:column>

    <display:column title="Event Time" sortable="true" sortProperty="id">
        <a href="<bean:message key="transferhistory.basepath"/>/${history.id}">
            <content:content name="history.date" dateFormatKey="date.format.transfer" ignoreNull="true" />
        </a>
    </display:column>

    <display:column property="formattedStatus" title="Status" sortable="true" />

    <display:column title="Transfer Host" sortable="true">
        <c:if test="${history.hostName != null}">
            <a href="<bean:message key="host.basepath"/>/${history.hostName}">${history.hostNickName}</a>
        </c:if>
        <c:if test="${history.hostName == null}">
            <i class="bi bi-dash text-muted" title="Not transferred to remote host"></i>
        </c:if>
    </display:column>

    <display:column title="Comment" property="formattedComment" />

</display:table>
</div>
</c:if>
