<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<style>
select {
	padding: 6px 12px 6px 40px;
}
</style>

<table style="width: 800;">
	<tr>
		<td><auth:if basePathKey="transferhistory.basepath" paths="/">
				<auth:then>
					<form>
						<table class="ecpdsSearchBox" style="align: left;">
							<tr>
								<td colspan="2" style="width: 80%;"><input class="search"
									name="hostSearch" type="text"
									placeholder="e.g. enabled=yes module=*Http hostname=*.test.fr options=*mqtt* nickname=Test_0? case=i"
									title="Default search is by nickname. Conduct extended searches using id, hostname, login, password, nickname, comment, dir, enabled, method, email and options (Properties & JavaScript) rules."
									style="width: 100%" value='<c:out value="${hostSearch}"/>'></td>
								<td style="width: 20%;"><select name="hostType"
									id="hostType" onchange="form.submit()" title="Sort by Type">
										<c:forEach var="option" items="${typeOptions}">
											<option value="${option.name}"
												<c:if test="${hostType == option.name}">SELECTED</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
							</tr>
							<tr>
								<td><select style="width: 100%;" name="network"
									onchange="form.submit()" title="Sort by Network">
										<c:forEach var="option" items="${networkOptions}">
											<option value="${option.name}"
												<c:if test="${network == option.name}">SELECTED</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
								<td><select style="width: 100%" name="label"
									onchange="form.submit()" title="Sort by Label">
										<c:forEach var="option" items="${labelOptions}">
											<option value="${option.name}"
												<c:if test="${label == option.name}">SELECTED</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
								<td><select style="width: 100%" name="hostFilter"
									onchange="form.submit()" title="Sort by Compression">
										<c:forEach var="option" items="${filterOptions}">
											<option value="${option.name}"
												<c:if test="${hostFilter == option.name}">SELECTED</c:if>>${option.value}</option>
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
		<c:if test="${empty hosts}">
			<div class="alert">
				<c:if test="${!hasHostSearch}">
						No Hosts found based on these criteria!<p>
				</c:if>
				<c:if test="${hasHostSearch}">
					<c:if test="${!empty getHostsError}">
						  Error in your query: ${getHostsError}<p>
					</c:if>
					<c:if test="${empty getHostsError}">
						  No Hosts found based on these criteria! Default search is by nickname.<p>
					</c:if>
						You can conduct an extensive search using the id, hostname, login, password, nickname, comment, dir, enabled, method, email and options (Properties & JavaScript) rules.<p>
						For instance: enabled=yes module=*Http hostname=*.test.fr
						id&gt;=100 options=*mqtt* nickname=Test_0? case=i
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
		</c:if>
		<c:if test="${not empty hosts}">
			<td valign="top"><display:table name="${hosts}" id="host"
					requestURI="" sort="external" defaultsort="1" partialList="true"
					size="${hostsSize}" pagesize="${recordsPerPage}" class="listing">
					<display:column sortable="true" title="Nickname">
						<a href="<bean:message key="host.basepath"/>/${host.name}">${host.nickName}</a>
					</display:column>
					<display:column title="Hostname/IP" property="host" sortable="true" />
					<display:column title="Associated Destinations" sortable="false">
						<c:if test="${fn:length(host.destinations) le 3}">
							<c:forEach var="destination" items="${host.destinations}">
								<b><a
									href="<bean:message key="destination.basepath"/>/${destination.name}">${destination.name}</a></b>
							</c:forEach>
						</c:if>
						<c:if test="${fn:length(host.destinations) eq 0}">
							<font color="grey">[none]</font>
						</c:if>
						<c:if test="${fn:length(host.destinations) gt 3}">
							<font color="grey">[${fn:length(host.destinations)}-destinations]</font>
						</c:if>
					</display:column>
					<display:column property="type" title="Type" sortable="true" />
					<display:column title="Method" sortable="true">
						<a
							href="<bean:message key="method.basepath"/>/${host.transferMethodName}">${host.transferMethodName}</a>
					</display:column>
					<display:column property="transferGroupName" title="Network"
						sortable="true" />
					<display:column property="networkName" title="Label"
						sortable="true" />
					<display:column sortable="true" title="Enabled">
						<c:if test="${host.active}">yes</c:if>
						<c:if test="${!host.active}">
							<font color="red">no</font>
						</c:if>
					</display:column>
				</display:table></td>
		</c:if>
		</td>
	</tr>
</table>
