<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
#properties {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

#javascript {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
}

#maxConnectionsHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#retryCountHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#retryFrequencyHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#maxStartHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#maxRequeueHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

#maxPendingHandle {
	width: 3em;
	height: 1.6em;
	top: 50%;
	margin-top: -.8em;
	text-align: center;
	line-height: 1.6em;
}

.scrollable-tab {
	width: 850px;
	height: 375px;
	resize: both;
	overflow: hidden;
	border: solid 1px lightgray;
	margin-top: 8px;
	margin-bottom: 8px;
  	overflow-y: scroll;
}
</style>

<table>
	<tr>
		<td>
			<table class="fields">
				<tiles:useAttribute id="actionFormName" name="action.form.name"
					classname="java.lang.String" />
				<tiles:useAttribute name="isInsert" classname="java.lang.String" />
				<c:if test="${isInsert == 'true'}">
					<c:if
						test="${not empty destinationActionForm.fromDestinationOptions}">
						<tr>
							<td colspan="2"><html:radio property="actionRequested"
									value="copy" />Copy From Existing Destination/Host(s)<br>
								<br></td>
						</tr>
						<tr>
							<th>Destination</th>
							<td><c:set var="destinations"
									value="${destinationActionForm.fromDestinationOptions}" /> <html:select
									property="fromDestination">
									<html:options collection="destinations" property="name"
										labelProperty="name" />
								</html:select></td>
						</tr>
						<tr>
							<th>Name</th>
							<td><input id="toDestination" name="toDestination"
								type="text">&nbsp;(please use letters, digits, '_' and
								'-' only)</td>
						</tr>
						<tr>
							<th>Comment</th>
							<td><html:text property="label" /></td>
						</tr>
						<tr>
							<th>Clone Shared Hosts</th>
							<td><html:checkbox
									title="If ticked then every Host of the existing Destination which is shared with another Destination will be cloned/renamed and allocated to the new Destination (the new Destination will therefore not use shared Hosts)"
									property="copySharedHost" /></td>
						</tr>
					</c:if>
					<c:if
						test="${not empty destinationActionForm.fromDestinationOptions}">
						<c:if test="${not empty destinationActionForm.masterOptions}">
							<tr>
								<td colspan="2"><br> <html:radio
										property="actionRequested" value="export" />Export Existing
									Destination/Host(s)<br> <br></td>
							</tr>
							<tr>
								<th>Destination</th>
								<td><html:select property="sourceDestination">
										<html:options collection="destinations" property="name"
											labelProperty="name" />
									</html:select></td>
							</tr>
							<tr>
								<th>Master</th>
								<td><c:set var="masters"
										value="${destinationActionForm.masterOptions}" /> <html:select
										property="master">
										<html:options collection="masters" property="name"
											labelProperty="value" />
									</html:select></td>
							</tr>
							<tr>
								<th>Clone Shared Hosts</th>
								<td><html:checkbox
										title="If ticked then every Host associated with the source Destination will be cloned/renamed on the target data store, otherwise a search will be done against the Host nick-name and if it already exists on the target data store then it will be used instead"
										property="copySourceSharedHost" /></td>
							</tr>
						</c:if>
					</c:if>
					<tr>
						<td colspan="2"><br> <html:radio
								property="actionRequested" value="create" />Create From Scratch<br>
							<br></td>
					</tr>
				</c:if>
				<c:if test="${isInsert == 'true'}">
					<tr>
						<th>Name</th>
						<td><input id="name" name="name" type="text">&nbsp;(please
							use letters, digits, '_' and '-' only)</td>
					</tr>
				</c:if>
				<c:if test="${isInsert != 'true'}">
					<tr>
						<th>Name</th>
						<td>${requestScope[actionFormName].name}<html:hidden
								property="name" /></td>
					</tr>
				</c:if>
				<tr>
					<th>Type</th>
					<td><c:set var="types"
							value="${destinationActionForm.typeOptions}" /> <html:select
							property="type">
							<html:options collection="types" property="name"
								labelProperty="value" />
						</html:select></td>
				</tr>
				<tr>
					<th>Comment</th>
					<td><html:text property="comment" /></td>
				</tr>
				<tr>
					<th>On Host Failure</th>
					<td><c:set var="onHosts"
							value="${destinationActionForm.onHostFailureOptions}" /> <html:select
							title="In case of error on a data transmission then try the next host in the list and stick to it if it works or restart with the first host in the list?"
							property="onHostFailure">
							<html:options collection="onHosts" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>If Target Exists</th>
					<td><c:set var="ifTargets"
							value="${destinationActionForm.ifTargetExistOptions}" /> <html:select
							property="ifTargetExist">
							<html:options collection="ifTargets" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Delete From Spool</th>
					<td><c:set var="keepInSpools"
							value="${destinationActionForm.keepInSpoolOptions}" /> <html:select
							property="keepInSpool">
							<html:options collection="keepInSpools" property="value"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Country</th>
					<td><c:set var="countries"
							value="${destinationActionForm.countryOptions}" /> <html:select
							property="countryIso">
							<html:options collection="countries" property="iso"
								labelProperty="name" />
						</html:select></td>
				</tr>
				<tr>
					<th>Transfer Group</th>
					<td><bean:define id="groups" name="destinationActionForm"
							property="transferGroupOptions" type="java.util.Collection	" />
						<html:select
							title="If no Dissemination Host is active then this field specify in which Transfer Group the queued files should be stored"
							property="transferGroup">
							<html:options collection="groups" property="name"
								labelProperty="name" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Max Connections</th>
					<td>
						<div id="maxConnectionsSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxConnections" id="maxConnections">
							<div
								title="Maximum number of parallel connections authorized at a time on all the hosts of the Destination"
								id="maxConnectionsHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Retry Count</th>
					<td>
						<div id="retryCountSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="retryCount" id="retryCount">
							<div
								title="If set the Destination is hold after a consecutive number of unsuccessful transfers (a manual restart will be necessary)"
								id="retryCountHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Retry Frequency</th>
					<td>
						<div id="retryFrequencySlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="retryFrequency" id="retryFrequency">
							<div
								title="Time to wait before to retry with the Primary Host if the transmission is failing on all the Backup Hosts"
								id="retryFrequencyHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Max Start</th>
					<td>
						<div id="maxStartSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxStart" id="maxStart">
							<div
								title="If set the transfer is delayed after a consecutive number of unsuccessful attempts"
								id="maxStartHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Start Frequency</th>
					<td><html:text
							title="Define the delay in ms mentioned in the previous parameter"
							property="startFrequency" /></td>
				</tr>
				<tr>
					<th>Max Requeue</th>
					<td>
						<div id="maxRequeueSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxRequeue" id="maxRequeue">
							<div
								title="If set the transfer is tagged as failed after a consecutive number of unsuccessful transmissions (a manual requeue will be necessary)"
								id="maxRequeueHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Max Pending</th>
					<td>
						<div id="maxPendingSlider" style="width: 210px; margin: 6px;">
							<input type="hidden" name="maxPending" id="maxPending">
							<div
								title="Define the maximum number of queued files which can exists at a single time in the Destination (new attempt of queueing files are rejected)"
								id="maxPendingHandle" class="ui-slider-handle"></div>
						</div>
					</td>
				</tr>
				<tr>
					<th>Max File Size</th>
					<td><html:text
							title="Define the maximum size for a file in the queue (attempt of queueing bigger files are rejected)"
							property="maxFileSize" /></td>
				</tr>
				<tr>
					<th>Reset Frequency</th>
					<td><html:text
							title="If set and the Destination is successfully using a backup host for more than a specified time then restart the Destination"
							property="resetFrequency" /></td>
				</tr>
				<tr>
					<th>Max Inactivity</th>
					<td><html:text
							title="If set and the Destination has no dissemination activity (push) for more than the value specified in ms then the Destination will show a problem on the monitoring"
							property="maxInactivity" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Group By Date</th>
					<td><html:checkbox
							title="If set then incoming ftp/sftp users will see the files grouped into date directories"
							property="groupByDate" /></td>
				</tr>
				<tr>
					<th>Date Format</th>
					<td><html:text
							title="Define the format of the date to display for each directory"
							property="dateFormat" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Data Compression</th>
					<td><bean:define id="filters" name="destinationActionForm"
							property="filterNameOptions" type="java.util.Collection	" /> <html:select
							title="If requested data files are compressed in the queue if there is enough time before transmission (otherwise files are compressed on the fly)"
							property="filterName">
							<html:options collection="filters" property="name"
								labelProperty="name" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Host For Sources</th>
					<td><c:set var="sources"
							value="${destinationActionForm.hostForSourceOptions}" /> <html:select
							title="If the data file is not found on the data mover then specify which host to use in order to retrieve the file from the source"
							property="hostForSourceName">
							<html:options collection="sources" property="name"
								labelProperty="nickName" />
						</html:select>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Owner</th>
					<td><c:set var="ecUsers"
							value="${destinationActionForm.ecUserOptions}" /> <html:select
							title="Only for the record" property="ecUserName">
							<html:options collection="ecUsers" property="name"
								labelProperty="comment" />
						</html:select></td>
				</tr>
				<tr>
					<th>Mail Address</th>
					<td><html:text
							title="Email address used when sending notifications"
							property="userMail" /></td>
				</tr>
				<tr>
					<th>Mail on Start</th>
					<td><html:checkbox property="mailOnStart" /></td>
				</tr>
				<tr>
					<th>Mail on End</th>
					<td><html:checkbox property="mailOnEnd" /></td>
				</tr>
				<tr>
					<th>Mail on Error</th>
					<td><html:checkbox property="mailOnError" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<th>Restart on Update</th>
					<td><html:checkbox
							title="Automatically restart the Destination if a change is detected on one of the host configuration"
							property="stopIfDirty" /></td>
				</tr>
				<tr>
					<th>Acquisition</th>
					<td><html:checkbox
							title="Request the Acquisition Scheduler to use this Destination for Data Discovery and Retrieval (at least one Acquisition host must be defined)"
							property="acquisition" /></td>
				</tr>
				<tr>
					<th>Enabled</th>
					<td><html:checkbox property="active" /></td>
				</tr>
				<tr>
					<th>Show In Monitors</th>
					<td><html:checkbox property="showInMonitors" /></td>
				</tr>
				<tr>
					<th>Backup</th>
					<td><html:checkbox
							title="Request the storage of the data files on the backup system (if available for the Transfer Group where the files are stored)"
							property="backup" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
			</table>
		</td>
		<td valign="top"><c:if test="${isInsert != 'true'}">
				<display:table id="host"
					name="${destinationActionForm.disseminationHostsAndPriorities}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="host">
							<caption style="white-space: nowrap;">No Dissemination Hosts <a href="#"
								onClick="hideChoosers('disseminationHostChooser');toggle_in_place(event,'disseminationHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<c:if test="${not empty host.name.comment}">
						<display:column sortable="true" title="Host">
							<span title="${host.name.comment}">${host.name.nickName}</span>
						</display:column>
					</c:if>
					<c:if test="${empty host.name.comment}">
						<display:column sortable="true" title="Host">${host.name.nickName}</display:column>
					</c:if>
					<display:column property="value" title="Priority" sortable="true" />
					<display:column class="buttons">
						<table>
							<tr>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/increaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.increase"
											titleKey="ecpds.destination.increaseHostPriority"
											altKey="ecpds.destination.increasePriority"
											writeFullTag="true" /></a></td>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/decreaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.decrease"
											titleKey="ecpds.destination.decreaseHostPriority"
											altKey="ecpds.destination.decreasePriority"
											writeFullTag="true" /></a></td>
							</tr>
						</table>
					</display:column>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Dissemination Hosts <a href="#"
							onClick="hideChoosers('disseminationHostChooser');toggle_in_place(event,'disseminationHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="host"
					name="${destinationActionForm.acquisitionHostsAndPriorities}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="host">
							<caption style="white-space: nowrap;">No Acquisition Hosts <a href="#"
								onClick="hideChoosers('acquisitionHostChooser');toggle_in_place(event,'acquisitionHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<c:if test="${not empty host.name.comment}">
						<display:column sortable="true" title="Host">
							<span title="${host.name.comment}">${host.name.nickName}</span>
						</display:column>
					</c:if>
					<c:if test="${empty host.name.comment}">
						<display:column sortable="true" title="Host">${host.name.nickName}</display:column>
					</c:if>
					<display:column property="value" title="Priority" sortable="true" />
					<display:column class="buttons">
						<table>
							<tr>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/increaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.increase"
											titleKey="ecpds.destination.increaseHostPriority"
											altKey="ecpds.destination.increasePriority"
											writeFullTag="true" /></a></td>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/decreaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.decrease"
											titleKey="ecpds.destination.decreaseHostPriority"
											altKey="ecpds.destination.decreasePriority"
											writeFullTag="true" /></a></td>
							</tr>
						</table>
					</display:column>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Acquisition Hosts <a href="#"
							onClick="hideChoosers('acquisitionHostChooser');toggle_in_place(event,'acquisitionHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="host"
					name="${destinationActionForm.proxyHostsAndPriorities}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="host">
							<caption style="white-space: nowrap;">No Proxy Hosts <a href="#"
								onClick="hideChoosers('proxyHostChooser');toggle_in_place(event,'proxyHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<c:if test="${not empty host.name.comment}">
						<display:column sortable="true" title="Host">
							<span title="${host.name.comment}">${host.name.nickName}</span>
						</display:column>
					</c:if>
					<c:if test="${empty host.name.comment}">
						<display:column sortable="true" title="Host">${host.name.nickName}</display:column>
					</c:if>
					<display:column property="value" title="Priority" sortable="true" />
					<display:column class="buttons">
						<table>
							<tr>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/increaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.increase"
											titleKey="ecpds.destination.increaseHostPriority"
											altKey="ecpds.destination.increasePriority"
											writeFullTag="true" /></a></td>
								<td><a
									href="<bean:message key="destination.basepath"/>/associations/${destinationActionForm.id}/decreaseHostPriority/${host.name.name}"><content:icon
											key="icon.small.decrease"
											titleKey="ecpds.destination.decreaseHostPriority"
											altKey="ecpds.destination.decreasePriority"
											writeFullTag="true" /></a></td>
							</tr>
						</table>
					</display:column>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteHost/<c:out value="${host.name.name}"/>','<bean:message key="ecpds.destination.deleteHost.warning" arg0="${host.name.nickName}" arg1="${destinationActionForm.id}" arg2="${host.name.type}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Proxy Hosts <a href="#"
							onClick="hideChoosers('proxyHostChooser');toggle_in_place(event,'proxyHostChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="user"
					name="${destinationActionForm.associatedEcUsers}" requestURI=""
					class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="user">
							<caption style="white-space: nowrap;">No Authorized Web Users (mspds) <a
								href="#"
								onClick="hideChoosers('userChooser');toggle_in_place(event,'userChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>					
					</display:setProperty>
					<c:if test="${not empty user.comment}">
						<display:column sortable="true" title="Name">
							<span title="${user.comment}">${user.id}</span>
						</display:column>
					</c:if>
					<c:if test="${empty user.comment}">
						<display:column sortable="true" title="Name">${user.id}</display:column>
					</c:if>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteEcUser/<c:out value="${user.id}"/>','<bean:message key="ecpds.destination.deleteEcUser.warning" arg0="${user.id}" arg1="${destinationActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Authorized Web Users (mspds) <a
							href="#"
							onClick="hideChoosers('userChooser');toggle_in_place(event,'userChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="policy"
					name="${destinationActionForm.associatedIncomingPolicies}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="policy">
							<caption style="white-space: nowrap;">No Authorized Data Policies <a href="#"
								onClick="hideChoosers('policyChooser');toggle_in_place(event,'policyChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<c:if test="${not empty policy.comment}">
						<display:column sortable="true" title="Name">
							<span title="${policy.comment}">${policy.id}
						</display:column>
					</c:if>
					<c:if test="${empty policy.comment}">
						<display:column sortable="true" title="Name">${policy.id}</display:column>
					</c:if>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deletePolicy/<c:out value="${policy.id}"/>','<bean:message key="ecpds.destination.deletePolicy.warning" arg0="${policy.id}" arg1="${destinationActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Authorized Data Policies <a href="#"
						onClick="hideChoosers('policyChooser');toggle_in_place(event,'policyChooser','none');"><content:icon
						key="icon.small.insert" titleKey="button.insert"
						altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="alias" name="${destinationActionForm.aliases}"
					requestURI="" class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="alias">
							<caption style="white-space: nowrap;">No Destination Aliases <a href="#"
								onClick="hideChoosers('aliasChooser');toggle_in_place(event,'aliasChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
							</caption>
						</table>
					</display:setProperty>
					<c:if test="${not empty alias.comment}">
						<display:column sortable="true" title="Name">
							<span title="${alias.comment}">${alias.name}</span>
						</display:column>
					</c:if>
					<c:if test="${empty alias.comment}">
						<display:column sortable="true" title="Host">${alias.name}</display:column>
					</c:if>
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteAlias/<c:out value="${alias.name}"/>','<bean:message key="ecpds.destination.deleteAlias.warning" arg0="${alias.name}" arg1="${destinationActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Destination Aliases <a href="#"
							onClick="hideChoosers('aliasChooser');toggle_in_place(event,'aliasChooser','none');"><content:icon
								key="icon.small.insert" titleKey="button.insert"
								altKey="button.insert" writeFullTag="true" /></a>
					</display:caption>
				</display:table>
				<br />
				<display:table id="file"
					name="${destinationActionForm.metadataFiles}" requestURI=""
					class="listing">
					<display:setProperty name="basic.msg.empty_list">
						<table class="listing" id="policy">
							<caption style="white-space: nowrap;">No Metadata Files</caption>
						</table>
					</display:setProperty>
					<display:column property="name" sortable="true" title="Name" />
					<display:column>
						<a
							href="javascript:validate('<bean:message key="destination.basepath"/>/associations/<c:out value="${destinationActionForm.id}"/>/deleteMetadataFile/<c:out value="${file.name}"/>','<bean:message key="ecpds.destination.deleteMetadataFile.warning" arg0="${file.name}" arg1="${destinationActionForm.id}"/>')"><content:icon
								key="icon.small.delete" titleKey="button.delete"
								altKey="button.delete" writeFullTag="true" /></a>
					</display:column>
					<display:caption>Metadata Files</display:caption>
				</display:table>
				<br />
			</c:if></td>
		<td valign="top"><c:if test="${isInsert != 'true'}">
				<%
				boolean odd;
				%>
				<div id="acquisitionHostChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Acquisition Host</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.acquisitionHostOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.nickName}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="disseminationHostChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Dissemination Host</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.disseminationHostOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.nickName}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="proxyHostChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Proxy Host</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.proxyHostOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addHost/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.nickName}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="userChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Authorized Web User</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.associatedEcUserOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addEcUser/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.id}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="policyChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Authorized Data Policy</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.associatedIncomingPoliciesOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addPolicy/${column.id}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.comment}">${column.id}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
				<div id="aliasChooser" class="chooser">
					<table class="listing" border=0>
						<caption>Add a new Destination Alias</caption>
						<thead>
							<tr>
								<th></th>
								<th class="sorted order1">Name</th>
							</tr>
						</thead>
						<tbody>
							<%
							odd = true;
							%>
							<c:forEach var="column"
								items="${destinationActionForm.aliasOptions}">
								<tr class='<%=(odd ? "odd" : "even")%>'>
									<td><a
										href="/do/transfer/destination/associations/${destinationActionForm.destination.id}/addAlias/${column.name}"><img
											src="/assets/icons/webapp/left_small.gif" alt="Add"
											title="Add" /></a></td>
									<td><span title="${column.value}">${column.name}</span></td>
								</tr>
						</tbody>
						<%
						odd = !odd;
						%>
						</c:forEach>
					</table>
				</div>
			</c:if></td>
	</tr>
	<tr>
		<td colspan="2">
			<table class="fields">
				<tr>
					<td>
						<div id="tabs">
							<ul>
								<li><a href="#tabs-1">Properties</a></li>
								<li><a href="#tabs-2">JavaScript</a></li>
								<li><a href="#tabs-3">Help</a></li>
							</ul>
							<div id="tabs-1">
								<pre id="properties">
									<c:out value="${requestScope[actionFormName].properties}" />
								</pre>
								<textarea id="properties" name="properties"
									style="display: none;"></textarea>
								<button type="button"
									onclick="formatSource(editorProperties); return false">Format</button>
								<button type="button"
									onclick="clearSource(editorProperties); return false">Clear</button>
							</div>
							<div id="tabs-2">
								<pre id="javascript">
									<c:out value="${requestScope[actionFormName].javascript}" />
								</pre>
								<textarea id="javascript" name="javascript"
									style="display: none;"></textarea>
								<button type="button"
									onclick="formatSource(editorJavascript); return false">Format</button>
								<button type="button"
									onclick="testSource(editorJavascript); return false">Test</button>
								<button type="button"
									onclick="clearSource(editorJavascript); return false">Clear</button>
							</div>
							<div id="tabs-3" class="scrollable-tab">
							</div>
						</div>
					</td>
				</tr>
			</table>
		</td>
		<td></td>
	</tr>
</table>

<script>
	var editorProperties = getEditorProperties(false, true, "properties", "crystal");
	
	// Get the completions from the bean!      		
    var completions = [
    	${requestScope[actionFormName].completions}
    ];
	
	// Lets' populate the help tab!
	$(document).ready(function() {
		$('#tabs-3').html(getHelpHtmlContent(completions, 'Available Options for this Destination'));
	});
    
	// Create a custom completer
	var customCompleter = {
  		getCompletions: function(editor, session, pos, prefix, callback) {
      		// Get the current line of text
      		var line = session.getLine(editor.getCursorPosition().row);

   			completions.forEach(function(completion) {
      			completion.value = completion.caption + ' = ""';
    		});
    		
      		var matchingCompletions = completions.filter(function(completion) {
      			return !checkIfExist(editor, completion.value) && (line.length === 0 || completion.value.startsWith(line));
    		}).map(function(completion) {
      			completion.value = prefix + completion.value.substring(line.length);
      			return completion;
    		});

      		if (matchingCompletions.length > 0) {
        		callback(null, matchingCompletions);
      		} else {
        		callback(null, []);
      		}
    	}
	};

	// Set the custom completer for the editor
	editorProperties.completers = [customCompleter];
		
	// Add a click event listener to the properties editor
    editorProperties.addEventListener("changeSelection", function (event) {
    	editorProperties.session.setAnnotations(
    		getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    	checkEachLine(editorProperties);
    });
    
	// Track changes in the editor's content
	editorProperties.getSession().on("change", function(e) {
  		// Check if the change was due to completion
  		if (e.action === "insert" && e.lines.length == 1 && e.lines[0] !== '"' && e.lines[0].endsWith('"')) {
    		setTimeout(function() {
				// Move the cursor between the double quotes
    			editorProperties.moveCursorTo(e.end.row, e.end.column - 1);
    			editorProperties.selection.clearSelection();
    		}, 0);
  		}
	});

	var editorJavascript = getEditorProperties(false, false, "javascript", "javascript");

	var textareaProperties = $('textarea[name="properties"]');
	textareaProperties.closest('form').submit(function() {
		textareaProperties.val(editorProperties.getSession().getValue());
	});

	var textareaJavascript = $('textarea[name="javascript"]');
	textareaJavascript.closest('form').submit(function() {
		textareaJavascript.val(editorJavascript.getSession().getValue());
	});

	makeResizable(editorProperties);
	makeResizable(editorJavascript);

	$("#tabs").tabs();
	$("#tabs").tabs("option", "active", 0);

	$(function() {
		$("#maxConnectionsSlider")
				.slider(
						{
							min : 1,
							max : 150,
							value : <c:out value="${requestScope[actionFormName].maxConnections}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxConnectionsHandle").text(value);
								$("#maxConnections").val(value);
							},
							slide : function(event, ui) {
								$("#maxConnectionsHandle").text(ui.value);
								$("#maxConnections").val(ui.value);
							}
						});
		$("#retryCountSlider")
				.slider(
						{
							min : -1,
							max : 10,
							value : <c:out value="${requestScope[actionFormName].retryCount}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#retryCountHandle").text(
										value == -1 ? "Off" : value);
								$("#retryCount").val(value);
							},
							slide : function(event, ui) {
								$("#retryCountHandle").text(
										ui.value == -1 ? "Off" : ui.value);
								$("#retryCount").val(ui.value);
							}
						});
		$("#retryFrequencySlider")
				.slider(
						{
							min : 0,
							max : 600,
							step : 30,
							value : <c:out value="${requestScope[actionFormName].retryFrequency/1000}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#retryFrequencyHandle").text(value + " s");
								$("#retryFrequency").val(value * 1000);
							},
							slide : function(event, ui) {
								$("#retryFrequencyHandle")
										.text(ui.value + " s");
								$("#retryFrequency").val(ui.value * 1000);
							}
						});
		$("#maxStartSlider").slider({
			min : 0,
			max : 30,
			value : <c:out value="${requestScope[actionFormName].maxStart}"/>,
			range : "min",
			animate : true,
			create : function() {
				var value = $(this).slider("value");
				$("#maxStartHandle").text(value == 0 ? "Off" : value);
				$("#maxStart").val(value);
			},
			slide : function(event, ui) {
				$("#maxStartHandle").text(ui.value == 0 ? "Off" : ui.value);
				$("#maxStart").val(ui.value);
			}
		});
		$("#maxRequeueSlider")
				.slider(
						{
							min : 0,
							max : 10,
							value : <c:out value="${requestScope[actionFormName].maxRequeue}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxRequeueHandle").text(
										value == 0 ? "Off" : value);
								$("#maxRequeue").val(value);
							},
							slide : function(event, ui) {
								$("#maxRequeueHandle").text(
										ui.value == 0 ? "Off" : ui.value);
								$("#maxRequeue").val(ui.value);
							}
						});
		$("#maxPendingSlider")
				.slider(
						{
							min : 0,
							max : 100,
							step : 5,
							value : <c:out value="${requestScope[actionFormName].maxPending/1000}"/>,
							range : "min",
							animate : true,
							create : function() {
								var value = $(this).slider("value");
								$("#maxPendingHandle").text(
										value == 0 ? "Off" : value + "k");
								$("#maxPending").val(value * 1000);
							},
							slide : function(event, ui) {
								$("#maxPendingHandle").text(
										ui.value == 0 ? "Off" : ui.value + "k");
								$("#maxPending").val(ui.value * 1000);
							}
						});
	});
	function validate(path, message) {
		if (confirm(message)) {
			window.location = path
		}
	}
	function hideChoosers(layerName) {
		if (layerName != 'disseminationHostChooser')
			hide('disseminationHostChooser');
		if (layerName != 'acquisitionHostChooser')
			hide('acquisitionHostChooser');
		if (layerName != 'proxyHostChooser')
			hide('proxyHostChooser');
		if (layerName != 'userChooser')
			hide('userChooser');
		if (layerName != 'aliasChooser')
			hide('aliasChooser');
		if (layerName != 'policyChooser')
			hide('policyChooser');
	}

	$('#toDestination').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9_-]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});

	$('#name').bind(
			'keypress',
			function(event) {
				var regex = new RegExp("^[a-zA-Z0-9_-]+$");
				var key = String.fromCharCode(!event.charCode ? event.which
						: event.charCode);
				if (!regex.test(key)) {
					event.preventDefault();
					return false;
				}
			});
</script>

