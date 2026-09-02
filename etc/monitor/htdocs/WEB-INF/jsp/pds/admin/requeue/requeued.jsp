<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="subcontent" />

<c:if test="${not empty requeuedSize}">
	<c:choose>
		<c:when test="${requeuedSize > 0}">
			<div class="alert alert-success d-flex align-items-center gap-2 mt-2">
				<i class="bi bi-check-circle-fill flex-shrink-0"></i>
				<div>
					${action} ${requeuedSize} Data Transfer(s).
					<c:if test="${not empty failedSize}">
						${failedSize} Data Transfer(s) could not be ${action == "Deleted" ? "deleted" : "requeued"}<c:if test="${not empty firstError}"> (e.g. "${firstError}")</c:if> — check the MasterServer log for details.
					</c:if>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<div class="alert alert-warning d-flex align-items-center gap-2 mt-2">
				<i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
				<div>
					${action} ${requeuedSize} Data Transfer(s). Nothing was ${action == "Deleted" ? "deleted" : "requeued"}<c:if test="${not empty firstError}"> (e.g. "${firstError}")</c:if> — check the MasterServer log for details.
				</div>
			</div>
		</c:otherwise>
	</c:choose>
</c:if>
