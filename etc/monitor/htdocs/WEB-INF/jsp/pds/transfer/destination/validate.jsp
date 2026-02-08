<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
	var OLD_STYLE = new Array();

	function checkAll(all, reverse) {
		var form = document.destinationDetailActionForm
		for (i = 0; i < form.length; i++) {
			var field = form.elements[i]
			if (field.type.toLowerCase() == "hidden"
					&& field.name.substr(0, 14) == "actionTransfer") {
				if (reverse)
					clickField(field)
				else
					setField(field, all ? "on" : "off");
			}
		}
	}

	function setAll() {
		var form = document.destinationDetailActionForm
		for (i = 0; i < form.length; i++) {
			var field = form.elements[i]
			if (field.type.toLowerCase() == "hidden"
					&& field.name.substr(0, 14) == "actionTransfer") {
				setField(field);
			}
		}
	}

	function transferChange(operation, transfer) {
	    var form = document.destinationDetailActionForm;
	    // Operations that require confirmation
	    if (operation === "delete" || operation === "stop" || operation === "requeue") {
	        var msg = "Are you sure you want to "
	                + operation
	                + (transfer ? " transfer " + transfer : " all selected transfers");
	        // Use custom confirmation dialog
	        confirmationDialog({
	            title: "Please Confirm",
	            message: msg,
	            onConfirm: function () {
	                var prefix = (operation === "delete") ? "deletions" : "operations";
	                if (transfer) {
	                    form.action = "/do/transfer/destination/" + prefix
	                            + "/${destinationDetailActionForm.id}/"
	                            + operation + "/" + transfer;
	                } else {
	                    form.action = "/do/transfer/destination/" + prefix
	                            + "/${destinationDetailActionForm.id}/"
	                            + operation;
	                }
	                form.submit();
	            },
	            onCancel: function () {
	                // user canceled → do nothing
	            }
	        });
	        // Important: return here so execution doesn’t continue
	        return;
	    }
	    // If operation does *not* require confirmation:
	    var prefix = (operation === "delete") ? "deletions" : "operations";
	    if (transfer) {
	        form.action = "/do/transfer/destination/" + prefix
	                + "/${destinationDetailActionForm.id}/" + operation + "/"
	                + transfer;
	    } else {
	        form.action = "/do/transfer/destination/" + prefix
	                + "/${destinationDetailActionForm.id}/" + operation;
	    }
	    form.submit();
	}
	
	function select(image, id) {
		var form = document.destinationDetailActionForm;
		clickField(form.elements["actionTransfer(" + id + ")"]);
	}

	function clickField(field) {
		setField(field, field.value == 'on' ? 'off' : 'on');
	}

	function setField(field, value) {
		// If a value is not supplied, the value is not changed, just the color
		var tr = field.parentNode.parentNode;
		var destValue = (value) ? value : field.value;

		if (destValue == 'on') {
			if (tr.className != 'selected')
				OLD_STYLE[field.name] = tr.className;
			tr.className = 'selected';
		} else {
			if (OLD_STYLE[field.name])
				tr.className = OLD_STYLE[field.name];
		}

		field.value = destValue;
	}
</script>

<form name="destinationDetailActionForm" method="GET"
	action="/do/transfer/destination/${destinationDetailActionForm.id}">

	<html:form action="/transfer/destination/*">

		<%@page import="java.util.*"%>
		<%@page import="org.displaytag.util.ParamEncoder"%>
		<%@page import="org.displaytag.tags.TableTagParameters"%>
		<%
		final int pageSize = 25;
		final String parameterPage = request
				.getParameter((new ParamEncoder("transfer").encodeParameterName(TableTagParameters.PARAMETER_PAGE)));
		final int start = (Integer.parseInt(parameterPage == null ? "1" : parameterPage) - 1) * pageSize;
		final int end = start + pageSize;
		%>

		<c:set var="start" value="<%=start%>" />
		<c:set var="end" value="<%=end%>" />

		<html:hidden property="status" />
		<html:hidden property="dataStream" />
		<html:hidden property="dataTime" />
		<html:hidden property="disseminationStream" />
		<input type="hidden" name="from" value="selection">

		<c:forEach var="param"
			items="${destinationDetailActionForm.displayTagsParamCollection}">
			<input type="hidden" name="${param.name}" value="${param.value}" />
		</c:forEach>

		<table>
			<tr>
				<td valign="top">
					<h3>
						<bean:message key="ecpds.destination.selected" />
						for destination ${destination.name}
					</h3>

					<p></p>

					<table>
						<tr>
							<td valign="top" colspan="2"><auth:if
									basePathKey="destination.basepath"
									paths="/operations/${destinationDetailActionForm.id}/requeue/">
									<auth:then>
										<c:set var="ecpdsCanHandleQueue" value="true" scope="page" />
									</auth:then>
								</auth:if> <display:table id="transfer"
									name="${destinationDetailActionForm.getSelectedTransfers(start,end)}"
									requestURI="" sort="external" partialList="true"
									size="${destinationDetailActionForm.selectedTransfersCount}"
									pagesize="<%=pageSize%>" class="listing">
									<display:caption>Global Actions apply only to Transfers in the table below which are also selected</display:caption>

									<display:column title="Err" style="padding-right:30px;">
										<c:if
											test="${not empty transfer.startTime and (transfer.retryTime != transfer.startTime)}">
											<content:icon key="icon.micro.cancel" writeFullTag="true" />
										</c:if>
										<c:if test="${transfer.retryTime == transfer.startTime}">
											<content:icon key="icon.micro.submit" writeFullTag="true" />
										</c:if>
									</display:column>
									<display:column title="Host">
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
												<a
													title="Transmitted through ${transfer.transferServerName}"
													href="/do/transfer/host/${transfer.hostName}">${transfer.hostNickName}</a>
											</c:if>
										</c:if>
									</display:column>
									<display:column title="Sched. Time">
										<content:content name="transfer.scheduledTime"
											dateFormatKey="date.format.transfer" ignoreNull="true" />
									</display:column>

									<display:column title="Start Time">
										<c:if test="${transfer.startTime != null}">
											<content:content name="transfer.startTime"
												dateFormatKey="date.format.transfer" ignoreNull="true" />
										</c:if>
										<c:if test="${transfer.startTime == null}">
											<font color="grey"><span
												title="Data not transferred to remote host">[n/a]</span></font>
										</c:if>
									</display:column>
									<display:column title="Finish Time">
										<c:if test="${transfer.realFinishTime != null}">
											<content:content name="transfer.realFinishTime"
												dateFormatKey="date.format.transfer" ignoreNull="true" />
										</c:if>
										<c:if test="${transfer.realFinishTime == null}">
											<font color="grey"><span
												title="Data not transferred to remote host">[n/a]</span></font>
										</c:if>
									</display:column>

									<display:column title="Target">
										<a title="Size: ${transfer.formattedSize}"
											href="/do/transfer/data/${transfer.id}">${transfer.target}</a>
									</display:column>

									<display:column title="TS">${transfer.dataFile.timeStep}</display:column>
									<display:column title="%">${transfer.progress}</display:column>

									<display:column title="Mbits/s">
										<c:if test="${transfer.transferRate != '0'}">
											<a STYLE="TEXT-DECORATION: NONE"
												title="Rate: ${transfer.formattedTransferRate}">${transfer.formattedTransferRateInMBitsPerSeconds}</a>
										</c:if>
										<c:if test="${transfer.transferRate == 0}">
											<font color="grey"><span
												title="Data not transferred to remote host">[n/a]</span></font>
										</c:if>
									</display:column>

									<display:column title="Status">
										<c:set var="expiredDate" scope="page">
											<content:content name="transfer.expiryDate"
												dateFormatKey="date.format.long.iso" ignoreNull="true" />
										</c:set>
										<c:if test="${transfer.expired == true}">
											<font color="red"><span
												title="Data Transfer expired on ${expiredDate}"> <c:if
														test="${destinationDetailActionForm.memberState}">${transfer.memberStateDetailedStatus}</c:if>
													<c:if test="${not destinationDetailActionForm.memberState}">${transfer.detailedStatus}	</c:if>
											</span></font>
										</c:if>
										<c:if test="${transfer.expired == false}">
											<c:if test="${destinationDetailActionForm.memberState}">${transfer.memberStateDetailedStatus}</c:if>
											<c:if test="${not destinationDetailActionForm.memberState}">${transfer.detailedStatus}	</c:if>

										</c:if>
									</display:column>
									<display:column title="Prior">${transfer.priority}</display:column>

									<display:column class="buttons" title="Actions">
										<c:if test="${not empty ecpdsCanHandleQueue}">
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
															<a
																href="javascript:transferChange('requeue','${transfer.id}')"><content:icon
																	key="icon.small.requeue"
																	titleKey="ecpds.destination.requeue"
																	altKey="ecpds.destination.requeue" writeFullTag="true" /></a>
														</c:if></td>
													<td><c:if test="${transfer.canBeStopped}">
															<a
																href="javascript:transferChange('stop','${transfer.id}')"><content:icon
																	key="icon.small.stop" titleKey="ecpds.destination.stop"
																	altKey="ecpds.destination.stop" writeFullTag="true" /></a>
														</c:if></td>
													<td><a
														href="javascript:transferChange('increaseTransferPriority','${transfer.id}')"><content:icon
																key="icon.small.increase"
																titleKey="ecpds.destination.increasePriority"
																altKey="ecpds.destination.increasePriority"
																writeFullTag="true" /></a></td>
													<td><a
														href="javascript:transferChange('decreaseTransferPriority','${transfer.id}')"><content:icon
																key="icon.small.decrease"
																titleKey="ecpds.destination.decreasePriority"
																altKey="ecpds.destination.decreasePriority"
																writeFullTag="true" /></a></td>
												</tr>
											</table>
										</c:if>
									</display:column>

									<display:column title="Select" class="buttons">
										<html:hidden property="actionTransfer(${transfer.id})" />
										<table>
											<tr>
												<td><img onClick="select(this,'${transfer.id}')"
													src="/assets/icons/ecpds/favorites.png"
													title="<bean:message key="ecpds.destination.select"/>"
													alt="<bean:message key="ecpds.destination.select" />" /></td>
												<td><a class="menuitem"
													href="javascript:transferChange('cancel')"><img
														border="0"
														src="<content:icon key="icon.small.arrow.left" writeFullTag="false"/>"
														title="<bean:message key="ecpds.destination.backFromSelected"/>"
														alt="<bean:message key="ecpds.destination.backFromSelected" /> " /></a>
												</td>
											</tr>
										</table>
									</display:column>

									<display:footer>
										<tr>
											<td colspan="9" class="buttons"></td>
											<td class="buttons"><a title="Select All"
												href="javascript:checkAll(true,false)">A/</a><a
												title="Unselect All" href="javascript:checkAll(false,false)">N</a>/<a
												title="Reverse Selection"
												href="javascript:checkAll(false,true)">R</a></td>
										</tr>
									</display:footer>

								</display:table></td>
						</tr>

						<tr>
							<td><tiles:insert
									page="./pds/transfer/destination/data/messages.jsp" /></td>
						</tr>

						<tr>
							<td colspan="2" align="center"><c:if
									test="${not empty ecpdsCanHandleQueue}">
									<table class="fields">
										<tr>
											<th>Global Actions</th>
											<td><a href="javascript:transferChange('requeue')"><content:icon
														key="icon.requeue" titleKey="ecpds.destination.requeue"
														altKey="ecpds.destination.requeue" writeFullTag="true" /></a></td>
											<td><a href="javascript:transferChange('stop')"><content:icon
														key="icon.stop" titleKey="ecpds.destination.stop"
														altKey="ecpds.destination.stop" writeFullTag="true" /></a></td>
											<auth:if basePathKey="destination.basepath"
												paths="/deletions/${destinationDetailActionForm.id}/delete">
												<auth:then>
													<td width="20"><a
														href="javascript:transferChange('delete')"><content:icon
																key="icon.delete" titleKey="ecpds.destination.delete"
																altKey="ecpds.destination.delete" writeFullTag="true" /></a></td>
												</auth:then>
											</auth:if>
											<td>&nbsp;&nbsp;</td>

											<td><a
												href="javascript:transferChange('increaseTransferPriority')"><content:icon
														key="icon.increase"
														titleKey="ecpds.destination.increasePriority"
														altKey="ecpds.destination.increasePriority"
														writeFullTag="true" /></a></td>
											<td><a
												href="javascript:transferChange('decreaseTransferPriority')"><content:icon
														key="icon.decrease"
														titleKey="ecpds.destination.decreasePriority"
														altKey="ecpds.destination.decreasePriority"
														writeFullTag="true" /></a></td>
											<td><html:text property="newPriority"
													styleClass="small_number" size="2" maxlength="2"
													onkeypress="submitEnter(this,event)" /></td>
											<td><a
												href="javascript:transferChange('setTransferPriority')"><content:icon
														key="icon.set" titleKey="ecpds.destination.setPriority"
														altKey="ecpds.destination.setPriority" writeFullTag="true" /></a></td>

											<td>&nbsp;&nbsp;</td>

											<td><a
												href="javascript:transferChange('extendLifetime')"><content:icon
														key="icon.extendLifetime"
														titleKey="ecpds.destination.extendLifetime"
														altKey="ecpds.destination.extendLifetime"
														writeFullTag="true" /></a></td>

											<td>&nbsp;&nbsp;</td>

											<td><a href="javascript:transferChange('clean')"><content:icon
														key="icon.clean" titleKey="ecpds.destination.clean"
														altKey="ecpds.destination.clean" writeFullTag="true" /></a></td>
											<td><a href="javascript:transferChange('cancel')"><content:icon
														key="icon.big.text.arrow.left"
														titleKey="ecpds.destination.continue"
														altKey="ecpds.destination.continue" writeFullTag="true" /></a></td>
										</tr>
									</table>
								</c:if></td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td width="25"><a valign="top"
								href="javascript:history.back()"><img
									src="<content:icon key="icon.arrow.left"/>" border="0" /></a></td>
							<td><a valign="top" class="menuitem"
								href="javascript:history.back()">Back</a></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		<script>
			setAll();
		</script>
	</html:form>
</form>