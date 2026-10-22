<c:choose>
	<c:when test="${destination.maxConnections eq 0 or connectionLimitHost.maxConnections eq 0 or (connectionLimitHost.maxConnections ge 0 and (destination.maxConnections lt 0 or connectionLimitHost.maxConnections lt destination.maxConnections))}">
		<c:set var="connectionLimitIcon" value="bi-exclamation-triangle-fill text-warning" />
	</c:when>
	<c:otherwise>
		<c:set var="connectionLimitIcon" value="bi-info-circle-fill text-info" />
	</c:otherwise>
</c:choose>
<i class="bi ${connectionLimitIcon} ms-1" style="font-size:0.75rem;cursor:pointer;" tabindex="0"
	title="<c:out value="${connectionLimitHost.connectionLimitDescription(destination.maxConnections)}"/>"
	aria-label="File-transfer connection limits"></i>
