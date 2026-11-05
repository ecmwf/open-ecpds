<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<c:if test="${journalAttention or journalUnavailable}">
<div class="alert ${journalDanger ? 'alert-danger' : 'alert-warning'} d-flex align-items-start gap-3 mb-4" role="alert">
    <i class="bi bi-database-exclamation flex-shrink-0 mt-1" style="font-size:1.2rem;"></i>
    <div>
        <c:choose>
            <c:when test="${journalUnavailable}"><strong>Operational event journal status unavailable.</strong>
                The Master could not supply its runtime status; this does not mean journaling is disabled.</c:when>
            <c:when test="${journalDanger}"><strong>Operational event journaling is degraded or stopped.</strong>
                New telemetry may be dropped; persisted, unacknowledged events are retained.</c:when>
            <c:when test="${journalHadPersistenceFailures}"><strong>Operational event persistence failures recorded.</strong>
                Persistence has recovered, but some events could not be journalled since Master startup.</c:when>
            <c:otherwise><strong>Operational event journaling needs attention.</strong>
                Review queue health, pauses and dropped telemetry. Persisted, unacknowledged events are retained for retry.</c:otherwise>
        </c:choose>
        <a href="/do/monitoring/eventjournal" class="alert-link ms-1">Review OpenTelemetry Journal &rarr;</a>
    </div>
</div>
</c:if>
