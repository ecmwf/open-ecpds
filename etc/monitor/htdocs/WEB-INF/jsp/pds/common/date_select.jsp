<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%-- Preserve fromDestination and destinationName params across date navigation --%>
<c:set var="_fdParam" value="${not empty param['fromDestination'] ? '&fromDestination='.concat(param['fromDestination']) : ''}"/>
<c:set var="_dnParam" value="${not empty param['destinationName'] ? '&destinationName='.concat(param['destinationName']) : ''}"/>

<div class="date-strip">
    <c:if test="${not empty dateOptions}">
        <div class="date-strip-pills">
            <c:forEach items="${dateOptions}" var="dateOption">
                <c:choose>
                    <c:when test="${dateOption == selectedDate}">
                        <a class="date-pill active" href="?mode=${param['mode']}&date=${dateOption}${_fdParam}${_dnParam}">${dateOption}</a>
                    </c:when>
                    <c:otherwise>
                        <a class="date-pill" href="?mode=${param['mode']}&date=${dateOption}${_fdParam}${_dnParam}">${dateOption}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${empty dateOptions}">
        <span class="text-muted fst-italic small">No dates available</span>
    </c:if>

    <tiles:importAttribute name="show_chart_button" ignore="true" />

    <c:if test="${not empty show_chart_button}">
        <div class="date-strip-chart-toggle">
            <c:choose>
                <c:when test="${param['mode'] == 'chart'}">
                    <a href="?mode=table&date=${selectedDate}${_fdParam}${_dnParam}" class="btn btn-sm btn-outline-secondary" title="Show as table">
                        <i class="bi bi-table"></i>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="?mode=chart&date=${selectedDate}${_fdParam}${_dnParam}" class="btn btn-sm btn-outline-secondary" title="Show as chart">
                        <i class="bi bi-bar-chart-line"></i>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>
<script>
(function () {
    document.querySelectorAll('.date-pill').forEach(function (pill) {
        var raw = pill.textContent.trim();
        var d;
        // Handle compact YYYYMMDD format
        if (/^\d{8}$/.test(raw)) {
            d = new Date(raw.slice(0,4) + '-' + raw.slice(4,6) + '-' + raw.slice(6,8));
        } else {
            d = new Date(raw);
        }
        if (!isNaN(d.getTime())) {
            pill.title = d.toLocaleDateString(undefined, {
                weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
            });
        }
    });
})();
</script>
