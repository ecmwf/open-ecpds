<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tr>


	<auth:if basePathKey="transferhistory.basepath" paths="/">
		<auth:then>
			<td valign="top">
		</auth:then>
		<auth:else>
			<td valign="top" colspan="2">
		</auth:else>
	</auth:if>

	<auth:if basePathKey="destination.basepath"
		paths="/operations/${destinationDetailActionForm.id}/activateHost/">
		<auth:then>
			<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
		</auth:then>
	</auth:if>

	<display:table id="disseminationHost"
		name="${destination.disseminationHostsAndPriorities}" requestURI=""
		class="listing">

		<c:set var="disseminationHostname"
			value=" (${disseminationHost.name.host})" scope="page" />
		<c:if test="${disseminationHostname eq ' ()'}">
			<c:set var="disseminationHostname" value="" scope="page" />
		</c:if>

		<display:column title="Name" sortable="true">
			<c:if test="${disseminationHost.name.active}">
				<a
					title="This Host is Activated (id=${disseminationHost.name.name})"
					href="/do/transfer/host/${disseminationHost.name.name}">${disseminationHost.name.nickName}</a>${disseminationHostname}
	</c:if>
			<c:if test="${!disseminationHost.name.active}">
				<a
					title="This Host is NOT Activated (id=${disseminationHost.name.name})"
					href="/do/transfer/host/${disseminationHost.name.name}"><font
					color="grey">${disseminationHost.name.nickName}</font></a>${disseminationHostname}
	</c:if>
		</display:column>

		<display:column property="value" title="Priority" sortable="true" />

		<c:if test="${not empty ecpdsCanHandleHosts}">
			<display:column class="buttons" title="Actions">
				<table>
					<tr>
						<td><c:if test="${!disseminationHost.name.active}">
								<a
									href="javascript:hostChange('activateHost','${disseminationHost.name.id}')">
									<content:icon key="icon.small.requeue"
										titleKey="ecpds.destination.activateHost"
										altKey="ecpds.destination.activateHost" writeFullTag="true"
										height="12" width="12" />
								</a>
							</c:if></td>
						<td><c:if test="${disseminationHost.name.active}">
								<a
									href="javascript:hostChange('deactivateHost','${disseminationHost.name.id}')">
									<content:icon key="icon.small.stop"
										titleKey="ecpds.destination.deactivateHost"
										altKey="ecpds.destination.deactivateHost" writeFullTag="true"
										height="9" width="9" />
								</a>
							</c:if></td>
						<td><a
							href="javascript:hostChange('increaseHostPriority','${disseminationHost.name.id}')">
								<content:icon key="icon.small.increase"
									titleKey="ecpds.destination.increaseHostPriority"
									altKey="ecpds.destination.increasePriority" writeFullTag="true"
									height="9" width="7" />
						</a></td>
						<td><a
							href="javascript:hostChange('decreaseHostPriority','${disseminationHost.name.id}')">
								<content:icon key="icon.small.decrease"
									titleKey="ecpds.destination.decreaseHostPriority"
									altKey="ecpds.destination.decreasePriority" writeFullTag="true"
									height="9" width="7" />
						</a></td>

						<auth:if basePathKey="transferhistory.basepath" paths="/">
							<auth:then>
								<td><a
									href="javascript:hostChange('duplicateHost','${disseminationHost.name.id}')">
										<content:icon key="icon.small.duplicate"
											titleKey="ecpds.destination.duplicateHost"
											altKey="ecpds.destination.duplicateHost" writeFullTag="true"
											height="9" width="7" />
								</a></td>
							</auth:then>
						</auth:if>

					</tr>
				</table>
			</display:column>
		</c:if>
		<display:caption>Dissemination Host(s)</display:caption>
		<display:setProperty name="basic.msg.empty_list">
		</display:setProperty>
	</display:table>

	</td>


	<c:if test="${destination.acquisition}">

		<td valign="top"><auth:if basePathKey="destination.basepath"
				paths="/operations/${destinationDetailActionForm.id}/activateHost/">
				<auth:then>
					<c:set var="ecpdsCanHandleHosts" value="true" scope="page" />
				</auth:then>
			</auth:if> <display:table id="acquisitionHost"
				name="${destination.acquisitionHostsAndPriorities}" requestURI=""
				class="listing">

				<c:set var="acquisitionHostname"
					value=" (${acquisitionHost.name.host})" scope="page" />
				<c:if test="${acquisitionHostname eq ' ()'}">
					<c:set var="acquisitionHostname" value="" scope="page" />
				</c:if>

				<display:column title="Name" sortable="true">
					<c:if test="${acquisitionHost.name.active}">
						<a
							title="This Host is Activated (id=${acquisitionHost.name.name})"
							href="/do/transfer/host/${acquisitionHost.name.name}">${acquisitionHost.name.nickName}</a>${acquisitionHostname}
					</c:if>
					<c:if test="${!acquisitionHost.name.active}">
						<a
							title="This Host is NOT Activated (id=${acquisitionHost.name.name})"
							href="/do/transfer/host/${acquisitionHost.name.name}"><font
							color="grey">${acquisitionHost.name.nickName}</font></a>${acquisitionHostname}
					</c:if>
				</display:column>

				<display:column property="value" title="Priority" sortable="true" />

				<c:if test="${not empty ecpdsCanHandleHosts}">
					<display:column class="buttons" title="Actions">
						<table>
							<tr>
								<td><c:if test="${!acquisitionHost.name.active}">
										<a
											href="javascript:hostChange('activateHost','${acquisitionHost.name.id}')">
											<content:icon key="icon.small.requeue"
												titleKey="ecpds.destination.activateHost"
												altKey="ecpds.destination.activateHost" writeFullTag="true"
												height="12" width="12" />
										</a>
									</c:if></td>
								<td><c:if test="${acquisitionHost.name.active}">
										<a
											href="javascript:hostChange('deactivateHost','${acquisitionHost.name.id}')">
											<content:icon key="icon.small.stop"
												titleKey="ecpds.destination.deactivateHost"
												altKey="ecpds.destination.deactivateHost"
												writeFullTag="true" height="9" width="9" />
										</a>
									</c:if></td>
								<td><a
									href="javascript:hostChange('increaseHostPriority','${acquisitionHost.name.id}')">
										<content:icon key="icon.small.increase"
											titleKey="ecpds.destination.increaseHostPriority"
											altKey="ecpds.destination.increasePriority"
											writeFullTag="true" height="9" width="7" />
								</a></td>
								<td><a
									href="javascript:hostChange('decreaseHostPriority','${acquisitionHost.name.id}')">
										<content:icon key="icon.small.decrease"
											titleKey="ecpds.destination.decreaseHostPriority"
											altKey="ecpds.destination.decreasePriority"
											writeFullTag="true" height="9" width="7" />
								</a></td>

								<auth:if basePathKey="transferhistory.basepath" paths="/">
									<auth:then>
										<td><a
											href="javascript:hostChange('duplicateHost','${acquisitionHost.name.id}')">
												<content:icon key="icon.small.duplicate"
													titleKey="ecpds.destination.duplicateHost"
													altKey="ecpds.destination.duplicateHost"
													writeFullTag="true" height="9" width="7" />
										</a></td>
									</auth:then>
								</auth:if>

							</tr>
						</table>
					</display:column>
				</c:if>
				<display:caption>Acquisition Host(s)</display:caption>
				<display:setProperty name="basic.msg.empty_list">
				</display:setProperty>
			</display:table></td>

	</c:if>

</tr>
