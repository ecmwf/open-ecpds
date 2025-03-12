<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

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
								<td colspan="3" style="width: 80%;"><input class="search"
									name="destinationSearch" type="text"
									placeholder="e.g. enabled=yes name=AB? email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i"
									title="Default search is by name. Conduct extended searches using name, comment, country, email, enabled, monitor, backup and options (Properties & JavaScript) rules."
									style="width: 100%"
									value='<c:out value="${destinationSearch}"/>'></td>
								<td style="width: 20%;"><select name="destinationType"
									id="destinationType" onchange="form.submit()"
									title="Sort by Type">
										<c:forEach var="option" items="${typeOptions}">
											<option value="${option.name}"
												<c:if test="${destinationType == option.name}">selected="selected"</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
							</tr>
							<tr>
								<td><select style="width: 100%" name="destinationStatus"
									onchange="form.submit()" title="Sort by Status">
										<c:forEach var="option" items="${statusOptions}">
											<option value="${option}"
												<c:if test="${destinationStatus == option}">SELECTED</c:if>>${option}</option>
										</c:forEach>
								</select></td>
								<td><select style="width: 100%" name="destinationFilter"
									onchange="form.submit()" title="Sort by Compression">
										<c:forEach var="option" items="${filterOptions}">
											<option value="${option.name}"
												<c:if test="${destinationFilter == option.name}">SELECTED</c:if>>${option.value}</option>
										</c:forEach>
								</select></td>
								<td><select style="width: 100%" name="aliases"
									onchange="form.submit()" title="Aliased From/To">
										<option value="all"
											<c:if test="${aliases == 'all'}">SELECTED</c:if>>All
											Destinations</option>
										<option value="to"
											<c:if test="${aliases == 'to'}">SELECTED</c:if>>Aliased
											From ...</option>
										<option value="from"
											<c:if test="${aliases == 'from'}">SELECTED</c:if>>Aliases
											To ...</option>
								</select></td>
								<td><select style="width: 100%" name="sortDirection"
									onchange="form.submit()" title="Ascending/Descending">
										<option value="asc"
											<c:if test="${sortDirection == 'asc'}">SELECTED</c:if>>Ascending</option>
										<option value="desc"
											<c:if test="${sortDirection == 'desc'}">SELECTED</c:if>>Descending</option>
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
		<c:if test="${empty columns}">
			<div class="alert">
				<c:if test="${!hasDestinationSearch}">
								No Destinations found matching these criteria!<p>
				</c:if>
				<c:if test="${hasDestinationSearch}">
					<c:if test="${!empty getDestinationsError}">
						  		  Error in your query: ${getDestinationsError}<p>
					</c:if>
					<c:if test="${empty getDestinationsError}">
						No Destinations found matching these criteria! The default search is by Destination name or email address, if the format matches.<p>
					</c:if>
								You can conduct an extensive search using the name, comment, country, email, enabled, monitor, backup and options (Properties & JavaScript) rules.<p>
						For instance: enabled=yes name=des0?_a* email=*@meteo.ms
						comment=*test* country=fr options=*mqtt* case=i
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
		<c:if test="${not empty columns}">
			<span class="pagebanner">${fn:length(destinations)} items
				found</span>
		</c:if>
		<c:forEach var="column" items="${columns}">
			<%
			boolean odd = false;
			%>
			<td valign="top">
				<table class="destinations">
					<c:forEach var="d" items="${destinations}" begin="${column.name}"
						end="${column.name + column.value -1}">
						<tr class='<%=(odd ? "odd" : "even")%>'>
							<td><img
								src="/assets/images/flags/micro/${d.countryIso}.png"
								alt="Flag of ${d.country.name}"
								title="Flag of ${d.country.name}" vspace="3"></td>
							<td><c:set var="desName" value="${d.id}" /> <c:if
									test="${fn:length(d.id) > 35}">
									<c:set var="desName" value="${fn:substring(desName,0,31)} ..." />
								</c:if> <c:if test="${d.showInMonitors}">
									<b><a href="/do/transfer/destination/${d.id}"
										title="[${d.typeText}] ${d.comment}">${desName}</a></b>
								</c:if> <c:if test="${not d.showInMonitors}">
									<i><a href="/do/transfer/destination/${d.id}"
										title="[${d.typeText}] ${d.id} is currently NOT shown in the Monitor Display">${desName}</a></i>
								</c:if></td>
							<td>${d.formattedStatus}</td>
							<c:if test="${fn:length(destinations) < 200}">
								<td><c:set var="aliases" value="${d.aliases}" /> <c:if
										test="${fn:length(aliases) < 3}">
										<c:forEach var="alias" items="${aliases}">
											<a title="${alias.id} is an alias for ${d.id}"
												href="/do/transfer/destination/${alias.id}"><font
												color="grey">${alias.id}&nbsp;</font></a>
										</c:forEach>
									</c:if> <c:if test="${fn:length(aliases) >= 3}">
										<font color="grey">[${fn:length(aliases)} Aliases]</font>
									</c:if> <c:if test="${fn:length(aliases) == 0}">
										<font color="grey">[No Alias]</font>
									</c:if></td>
							</c:if>
						</tr>
						<%
						odd = !odd;
						%>
					</c:forEach>
				</table>
			</td>
		</c:forEach>
	</tr>
</table>

<br>
