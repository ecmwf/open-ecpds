<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="search"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<auth:if basePathKey="host.basepath" paths="/edit/resetStats/">
	<auth:then>
		<table class="editSpareBox">
			<tr>
				<th colspan="3"><c:set var="hostName" value="${host.nickName}" />
					<c:if test="${fn:length(host.nickName) > 14}">
						<c:set var="hostName" value="${fn:substring(hostName,0,10)} ..." />
					</c:if> <a title="${host.nickName}"
					href='<bean:message key="host.basepath"/>/${host.id}'>${hostName}</a>
					<span><c:if test="${host.active}">
							(Enabled)
						</c:if> <c:if test="${!host.active}">
							(Disabled)
						</c:if> </span></th>
			</tr>
			<auth:if basePathKey="transferhistory.basepath" paths="/">
				<auth:then>
					<tr>
						<td><auth:link styleClass="menuitem"
								basePathKey="host.basepath" href="/${host.id}?mode=changelog"
								ignoreAccessControl="true">Changes Log</auth:link></td>
					</tr>
					<tr>
						<td><auth:link styleClass="menuitem"
								basePathKey="host.basepath"
								href="/edit/getOutput/view/${host.id}"
								ignoreAccessControl="true">View Output</auth:link></td>
					</tr>
					<tr>
						<td><auth:link styleClass="menuitem"
								basePathKey="host.basepath"
								href="/edit/cleanDataWindow/${host.id}"
								ignoreAccessControl="true">Clean Options</auth:link></td>
					</tr>
					<tr>
						<td><auth:link styleClass="menuitem"
								basePathKey="host.basepath" href="/edit/resetStats/${host.id}"
								ignoreAccessControl="true">Reset Stats</auth:link></td>
					</tr>
				</auth:then>
			</auth:if>
			<tr>
				<td><auth:link styleClass="menuitem"
						basePathKey="host.basepath" href="/edit/getReport/${host.id}"
						ignoreAccessControl="true">Network Info</auth:link> <c:forEach
						var="destination" items="${host.destinations}">
						<c:forEach var="proxy"
							items="${destination.proxyHostsAndPriorities}">
							<c:if test="${host.type == 'Dissemination'}">
								<li><auth:link styleClass="menusubitem"
										basePathKey="host.basepath"
										href="/edit/getReport/${host.id}/${proxy.name.id}"
										ignoreAccessControl="true">${proxy.name.nickName}</auth:link>
							</c:if>
						</c:forEach>
					</c:forEach></td>
			</tr>
		</table>
	</auth:then>
</auth:if>
