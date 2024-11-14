<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<!-- tiles/pds/transfer/data/spare.jsp -->

<auth:if basePathKey="destination.basepath"
	paths="/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}">
	<auth:then>
		<c:set var="showTable" value="yes" />
	</auth:then>
</auth:if>
<c:if test="${not empty showScheduleNow}">
	<c:set var="showTable" value="yes" />
</c:if>

<c:if
	test="${!datatransfer.deleted || not empty showScheduleNow || datatransfer.canBeDownloaded || datatransfer.statusCode == 'FETC'}">
	<c:if test="${not empty showTable}">
		<table class="editSpareBox">
			<c:if test="${!datatransfer.deleted}">
				<tr>
					<td><auth:link basePathKey="destination.basepath"
							href="/deletions/${datatransfer.destinationName}/deleteTransferForm/${datatransfer.id}"
							imageKey="icon.small.delete">&nbsp;&nbsp;Delete</auth:link></td>
				</tr>
			</c:if>
			<c:if test="${not empty showScheduleNow}">
				<tr>
					<td><auth:link basePathKey="destination.basepath"
							href="/operations/${datatransfer.destinationName}/scheduleNow/${datatransfer.id}"
							imageKey="icon.scheduleNow" ignoreAccessControl="true">&nbsp;&nbsp;Schedule Now</auth:link></td>
				</tr>
			</c:if>
			<c:if test="${datatransfer.canBeDownloaded}">
				<tr>
					<td><auth:link basePathKey="destination.basepath"
							href="/operations/${datatransfer.destinationName}/download/${datatransfer.id}"
							imageKey="icon.download" ignoreAccessControl="true">&nbsp;&nbsp;Download</auth:link></td>
				</tr>
			</c:if>
			<c:if test="${datatransfer.statusCode == 'FETC'}">
				<tr>
					<td><auth:link basePathKey="destination.basepath"
							href="/operations/${datatransfer.destinationName}/interrupt/${datatransfer.id}"
							imageKey="icon.download" ignoreAccessControl="true">&nbsp;&nbsp;Interrupt</auth:link></td>
				</tr>
			</c:if>
		</table>
	</c:if>
</c:if>

<!-- End of tiles/pds/transfer/data/spare.jsp -->
