<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<% final boolean journalSubmenu = "submenu".equals(request.getParameter("journalPlacement")); %>
<% if (journalSubmenu) { %><tr><td><% } else { %><li><% } %>
<c:choose>
    <c:when test="${journalEnabled or journalUnavailable}">
        <a href="/do/monitoring/eventjournal"><i class="bi bi-database-check ${journalDanger ? 'text-danger' : journalAttention or journalUnavailable ? 'text-warning' : ''}"></i>
            OpenTelemetry Journal</a>
    </c:when>
    <c:otherwise><span class="text-body-secondary opacity-50" aria-disabled="true"
        title="OpenTelemetry database journaling is disabled on the Master. Pending rows may still exist."><i class="bi bi-database-check"></i>
        OpenTelemetry Journal</span></c:otherwise>
</c:choose>
<% if (journalSubmenu) { %></td></tr><% } else { %></li><% } %>
