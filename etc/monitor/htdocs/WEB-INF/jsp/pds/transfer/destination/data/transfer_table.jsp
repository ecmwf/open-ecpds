<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<c:if test="${empty filteredTransfers}">
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
						You can conduct an extensive search using the target, source, ts, priority, groupby, identity, checksum, size, replicated, asap, deleted, expired, proxy, mover and event rules.<p>
						For instance: asap=yes target=*.dat source=/tmp/* ts&gt;10 ts&lt;=99 size&gt;=700kb case=i<p>
						<li>The 'case' option allows 's' for case-sensitive (default) or 'i' for case-insensitive search.
						<li>Ensure all spaces and equal signs in values are enclosed within double quotes (e.g. "a=b" or "United States").
						<li>The double quotes symbol (") can be escaped (e.g. "*.file:&#92;"*&#92;"").
						<li>The wildcard symbol asterisk (*) matches zero or more characters.
						<li>The wildcard symbol question mark (?) matches exactly one character.
					</c:if>
				</div>
</c:if>

<c:if test="${!empty filteredTransfers}">
	<display:table id="transfer" name="${filteredTransfers}" requestURI=""
		sort="external" defaultsort="3" partialList="true"
		size="${dataTransfersSize}" pagesize="${recordsPerPage}"
		class="listing">
		<display:caption>Current selection: ${destinationDetailActionForm.dataTransferCaption}&nbsp;&nbsp;<i>Current
				date <content:content name="currentDate"
					dateFormatKey="date.format.long.iso" ignoreNull="true" />
			</i>
		</display:caption>
		<display:column sortable="true" title="Err" style="padding-right:30px;">
			<c:if test="${not empty transfer.failedTime}">
				<content:icon title="Help" key="icon.micro.cancel"
					writeFullTag="true" />
			</c:if>
			<c:if test="${empty transfer.failedTime}">
				<content:icon title="Help" key="icon.micro.submit"
					writeFullTag="true" />
			</c:if>
		</display:column>
		<display:column title="Host" sortable="true">
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
		<display:column title="Sched. Time" sortable="true"
			sortProperty="scheduledTime">
			<content:content name="transfer.scheduledTime"
				dateFormatKey="date.format.transfer" ignoreNull="true" />
		</display:column>
		<display:column title="Start Time" sortable="true"
			sortProperty="startTime">
			<c:if test="${transfer.startTime != null}">
				<content:content name="transfer.startTime"
					dateFormatKey="date.format.transfer" ignoreNull="true" />
			</c:if>
			<c:if test="${transfer.startTime == null}">
				<font color="grey"><span
					title="Data not transferred to remote host">[n/a]</span></font>
			</c:if>
		</display:column>
		<display:column title="Finish Time" sortable="true"
			sortProperty="realFinishTime">
			<c:if test="${transfer.realFinishTime != null}">
				<content:content name="transfer.realFinishTime"
					dateFormatKey="date.format.transfer" ignoreNull="true" />
			</c:if>
			<c:if test="${transfer.realFinishTime == null}">
				<font color="grey"><span
					title="Data not transferred to remote host">[n/a]</span></font>
			</c:if>
		</display:column>
		<display:column title="Target" sortable="true">
			<a title="Size: ${transfer.formattedSize}"
				href="/do/transfer/data/${transfer.id}"><c:if
					test="${transfer.expired == true || transfer.deleted == true}">
					<font color="red">
				</c:if>${transfer.target}<c:if test="${transfer.expired == true || transfer.deleted == true}">
					</font>
				</c:if></a>
		</display:column>
		<display:column property="dataFile.timeStep" title="TS"
			sortable="true" />
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
		<display:column title="Status" sortable="true">
			<c:set var="expiredDate" scope="page">
				<content:content name="transfer.expiryDate"
					dateFormatKey="date.format.long.iso" ignoreNull="true" />
			</c:set>
			<c:if test="${transfer.expired == true && transfer.deleted == true}">
				<font color="red"><span
					title="Data Transfer expired on ${expiredDate}"> <c:if
							test="${destinationDetailActionForm.memberState}">${transfer.memberStateDetailedStatus}</c:if>
						<c:if test="${not destinationDetailActionForm.memberState}">${transfer.detailedStatus}</c:if>
				</span></font>
			</c:if>
			<c:if test="${transfer.expired == false && transfer.deleted == true}">
				<font color="red"><span
					title="Data Transfer deleted"> <c:if
							test="${destinationDetailActionForm.memberState}">${transfer.memberStateDetailedStatus}</c:if>
						<c:if test="${not destinationDetailActionForm.memberState}">${transfer.detailedStatus}</c:if>
				</span></font>
			</c:if>
			<c:if test="${transfer.expired == false && transfer.deleted == false}">
				<c:if test="${destinationDetailActionForm.memberState}">${transfer.memberStateDetailedStatus}</c:if>
				<c:if test="${not destinationDetailActionForm.memberState}">${transfer.detailedStatus}</c:if>
			</c:if>
		</display:column>
		<display:column property="priority" title="Prior" sortable="true" />
		<c:if test="${not empty ecpdsCanHandleQueue}">
			<display:column class="buttons" title="Actions" sortable="false">
				<c:if
					test="${transfer.expired == false && transfer.deleted == false}">
					<table>
						<tr>
							<td><c:if test="${transfer.canBeDownloaded}">
									<a
										href="javascript:transferChange('download','${transfer.id}')"><img
										src="/assets/icons/ecpds/ktorrent.png" border="0"
										alt="Download"
										title="Download ${transfer.target} (${transfer.dataFile.formattedSize})" /></a>
								</c:if></td>
							<td><c:if test="${transfer.canBeRequeued}">
									<a href="javascript:transferChange('requeue','${transfer.id}')"><content:icon
											key="icon.small.requeue" titleKey="ecpds.destination.requeue"
											altKey="ecpds.destination.requeue" writeFullTag="true"
											height="12" width="12" /></a>
								</c:if></td>
							<td><c:if test="${transfer.canBeStopped}">
									<a href="javascript:transferChange('stop','${transfer.id}')"><content:icon
											key="icon.small.stop" titleKey="ecpds.destination.stop"
											altKey="ecpds.destination.stop" writeFullTag="true"
											height="9" width="9" /></a>
								</c:if></td>
							<td><a
								href="javascript:transferChange('increaseTransferPriority','${transfer.id}')"><content:icon
										key="icon.small.increase"
										titleKey="ecpds.destination.increasePriority"
										altKey="ecpds.destination.increasePriority"
										writeFullTag="true" height="9" width="7" /></a></td>
							<td><a
								href="javascript:transferChange('decreaseTransferPriority','${transfer.id}')"><content:icon
										key="icon.small.decrease"
										titleKey="ecpds.destination.decreasePriority"
										altKey="ecpds.destination.decreasePriority"
										writeFullTag="true" height="9" width="7" /></a></td>
						</tr>
					</table>
				</c:if>
				<c:if
					test="${transfer.expired == true && transfer.deleted == false}">
					<c:set var="expiredDate" scope="page">
						<content:content name="transfer.expiryDate"
							dateFormatKey="date.format.long.iso" ignoreNull="true" />
					</c:set>
					<font color="grey"><span
						title="Data Transfer expired on ${expiredDate}">[expired]</span></font>
				</c:if>
				<c:if test="${transfer.deleted == true}">
					<font color="grey"><span title="Data Transfer deleted">[deleted]</span></font>
				</c:if>
			</display:column>
		</c:if>

		<display:column class="buttons" title="Select">
			<c:if test="${transfer.expired == false}">
				<html:hidden property="selectedTransfer(${transfer.id})" />
				<table>
					<tr>
						<td><img onClick="select(this,'${transfer.id}')"
							src="/assets/icons/ecpds/favorites.png"
							title="<bean:message key="ecpds.destination.select"/>"
							alt="<bean:message key="ecpds.destination.select" />" /></td>
						<td><a href="javascript:transferChange('validate','select')"><img
								border="0"
								src="<content:icon key="icon.small.arrow.right" writeFullTag="false"/>"
								title="<bean:message key="ecpds.destination.goSelected"/>"
								alt="<bean:message key="ecpds.destination.goSelected" />" /></a></td>
					</tr>
				</table>
			</c:if>
			<c:if test="${transfer.expired == true}">
	&nbsp;
	</c:if>
		</display:column>

		<display:footer>
			<tr>
				<td colspan="${numberOfColumns}" class="buttons"></td>
				<td class="buttons">
					<table>
						<tr>
							<td><a title="Select All"
								href="javascript:checkAll(true,false)">A/</a></td>
							<td><a title="Unselect All"
								href="javascript:checkAll(false,false)">N/</a></td>
							<td><a title="Reverse Selection"
								href="javascript:checkAll(false,true)">R</a></td>
						</tr>
					</table>
				</td>
				<td class="buttons">
					<table>
						<tr>
							<td><img onClick="transferChange('selectFiltered')"
								src="<content:icon key="icon.text.arrow" writeFullTag="false"/>"
								title="<bean:message key="ecpds.destination.selectAllPages"/>"
								alt="<bean:message key="ecpds.destination.selectAllPages" />" /></td>
							<td>${dataTransfersSize}</td>
						</tr>
					</table>
				</td>
			</tr>
		</display:footer>
	</display:table>
</c:if>
