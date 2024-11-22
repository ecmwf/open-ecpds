<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>

<tiles:insert name="date.select" />

<table>
	<tr>
		<td>
			<table class="select">
				<tr>
					<c:forEach var="transferStatus" items="${transferStatusOptions}">
						<c:if test="${transferStatus.id == currentTransferStatus.id}">
							<td width="100" class="selected"><a
								href="?transferStatus=${transferStatus.id}">${transferStatus.name}</a></td>
						</c:if>
						<c:if test="${transferStatus.id != currentTransferStatus.id}">
							<td width="100"><a
								href="?transferStatus=${transferStatus.id}&refreshPeriod=${param['refreshPeriod']}">${transferStatus.name}</a></td>
						</c:if>
					</c:forEach>
				</tr>
			</table>
		</td>
		<td><form action="" style="margin: 0; padding: 0">
				<input title="Screen Refresh Period (0 is No Refresh)"
					class="small_number" type="text" size="5" name="refreshPeriod"
					value="${param['refreshPeriod']}">
			</form></td>
	</tr>
</table>

<script>
	var refresh = '${param["refreshPeriod"]}';
		if (refresh != '' && refresh > 0) {
			setTimeout(function() {
				window.location.reload(true);
			}, refresh * 1000);
		}
</script>

<br />

<style>
select {
	padding: 6px 12px 6px 40px;
	width: 250px;
}
</style>

<table style="width: 800;">
	<tr>
		<td><auth:if basePathKey="transferhistory.basepath" paths="/">
				<auth:then>
					<form>
						<table class="ecpdsSearchBox" style="align: left;">
							<tr>
								<td style="width: 80%;"><input class="search"
									name="transferSearch" type="text"
									placeholder="e.g. expired=no target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i"
									title="Default search is by target. Conduct extended searches using target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy and event rules."
									style="width: 100%" value='<c:out value="${transferSearch}"/>'></td>
								<td style="width: 20%;"><select name="transferType"
									onchange="form.submit()" title="Sort by Type">
										<c:forEach var="option" items="${transferTypeOptions}">
											<option value="${option.name}"
												<c:if test="${transferType == option.name}">SELECTED</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
							</tr>
						</table>
					</form>
				</auth:then>
			</auth:if></td>
	</tr>
</table>
<table style="width: 100%;">
	<tr>
		<td><c:if test="${empty transferList}">
				<div class="alert">
					<c:if test="${!hasFileNameSearch}">
						No Data Transfers found based on these criteria!
					</c:if>
					<c:if test="${hasFileNameSearch}">
						<c:if test="${!empty getTransfersError}">
						  Error in your query: ${getTransfersError}<p>
						</c:if>
						<c:if test="${empty getTransfersError}">
						  No Data Transfers found based on these criteria! Default search is by target.<p>
						</c:if>
						You can conduct an extensive search using the target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted and event rules.<p>
							For instance: asap=yes target=*.dat source=/tmp/* ts&gt;10
							ts&lt;=99 size&gt;=700kb case=i
						<p>
						<li>The 'case' option allows 's' for case-sensitive (default)
							or 'i' for case-insensitive search.
						<li>Ensure all spaces and equal signs in values are enclosed
							within double quotes (e.g. "a=b" or "United States").
						<li>The double quotes symbol (") can be escaped (e.g.
							"*.file:&#92;"*&#92;"").
						<li>The wildcard symbol asterisk (*) matches zero or more
							characters.
						<li>The wildcard symbol question mark (?) matches exactly one
							character.
					</c:if>
				</div>
			</c:if> <c:if test="${!empty transferList}">
				<display:table id="transfer" name="${transferList}" requestURI=""
					sort="external" defaultsort="3" partialList="true"
					size="${transferListSize}" pagesize="${recordsPerPage}"
					class="listing">

					<display:column title="Destination" sortable="true">
						<a title="${transfer.destination.comment}"
							href="<bean:message key="destination.basepath"/>/${transfer.destinationName}">${transfer.destinationName}</a>
					</display:column>

					<display:column title="Transfer Host" sortable="true">
						<c:set var="nickName" value="${transfer.hostNickName}" />
						<jsp:useBean id="nickName" type="java.lang.String" />
						<c:if test='<%="".equals(nickName)%>'>
							<font color="grey"><span
								title="Data not transferred to remote host">[not-transferred]</span></font>
						</c:if>
						<c:if test="<%=nickName.length() > 0%>">
							<c:if test="${transfer.transferServerName == null}">
								<a href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
							</c:if>
							<c:if test="${transfer.transferServerName != null}">
								<a title="Transmitted through ${transfer.transferServerName}"
									href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
							</c:if>
						</c:if>
					</display:column>

					<display:column title="Sched. Time" sortable="true">
						<content:content name="transfer.scheduledTime"
							dateFormatKey="date.format.transfer" ignoreNull="true" />
					</display:column>

					<display:column title="Target" sortable="true">
						<c:if test="${!transfer.deleted}">
							<a title="Size: ${transfer.formattedSize}"
								href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
						</c:if>
						<c:if test="${transfer.deleted}">
							<a title="Size: ${transfer.formattedSize}"
								href="/do/transfer/data/${transfer.id}"><font color="red">${transfer.target}</font></a>
						</c:if>
					</display:column>

					<display:column title="%" property="progress" sortable="true" />

					<display:column title="Mbits/s" sortable="true"
						sortProperty="formattedTransferRateInMBitsPerSeconds">
						<c:if test="${transfer.transferRate != '0'}">
							<a STYLE="TEXT-DECORATION: NONE"
								title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
						</c:if>
						<c:if test="${transfer.transferRate == 0}">
							<font color="grey"><span
								title="Data not transferred to remote host">[n/a]</span></font>
						</c:if>
					</display:column>

					<display:column title="Prior" property="priority" sortable="true" />

				</display:table>
			</c:if></td>
	</tr>
</table>