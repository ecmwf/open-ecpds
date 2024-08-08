<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
table.pagelevel {
    border-collapse: collapse;
    Font-size: 10pt;
}
table.pagelevel td {
    border: 0px;
}
table.toplevel {
    border-collapse: collapse;
    Font-size: 10pt;
}
table.toplevel td.holder {
    vertical-align: top;
    border: 0px;
    padding: 6px;
}
table.toplevel td {
    border: 1px solid #999999;
    padding: 1pt;
    white-space: normal;
    text-align: center;
}
table.toplevel tr.titles td {
        background: #aaaaaa;
        color: black;
        text-align: center;
        padding: 0pt;
        margin: 0pt;
        vertical-align: center;
}
</style>

<!-- Just in case the MemberStates/Commercial Users would try to access this URL! -->
<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>
<c:redirect url="/do/start"/>
</auth:else>
</auth:if>

<script>
function createAndSubmitDynamicForm(action,bcc,subject,body) {
        try{
                var form = document.createElement("form");
                form.setAttribute("target", "_parent");
                form.setAttribute("method", "post");
                form.setAttribute("enctype", "text/plain");
                form.setAttribute("action", action + '?from=' + escape('newops@ecmwf.int') + '&to=' + escape('operators@ecmwf.int') + '&bcc=' + bcc + '&subject=' + escape(subject) + '&body=' + body);
                document.body.appendChild(form);
                form.submit();
        }catch(e){
                alert("Error"+e);
        }
}
</script>

<tiles:insert page="./pds/monitoring/reload.jsp" />

<table class="fields">
<tr>
<td valign="top"><tiles:insert page="./pds/monitoring/filter.jsp" /></td>
<td>&nbsp;|&nbsp;</td>
<td><a href="/do/transfer/destination" title="Go to the List of Destinations">Destinations</a></td>
<td><a href="/do/transfer/host" title="Go to the List of Hosts">Transfer Hosts</a></td>
<td>&nbsp;|&nbsp;</td>
<td><a href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&" title="Filter only Dissemination Destinations">Dissemination</a></td>
<td><a href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&" title="Filter only Acquisition Destinations">Acquisition</a></td>
<td>&nbsp;|&nbsp;</td>
<td><a href="/do/monitoring?application=" title="Monitor all Products">All</a></td>
<td><a href="/do/monitoring?application=pgen" title="Monitor only PGen Products">PGen</a></td>
<td><a href="/do/monitoring?application=no-pgen" title="Monitor all except PGen Products">Others</a></td>
<td>&nbsp;|&nbsp;</td>
<td><a href="/maps/maps.html" title="Show <%=System.getProperty("monitor.nickName")%> Destination Hosts on OpenStreetMap">Hosts on Map</a></td>
</tr>
</table>

<table class="toplevel">
	<tr>
		<td class="holder">
		<table class="pagelevel">
			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>

			<% boolean odd = false;	%>

			<c:forEach var="d" items="${reqData.destinations}"
				varStatus="fStatus">
				<c:set var="destStatus" value="${reqData.status[d.name]}" />

				<% odd = !odd; %>

				<tr class='<%=(odd?"odd":"even")%>'>

					<td><c:set var="primaryHost" value="${destStatus.primaryHost}" />
					<a
						title="${primaryHost.networkName}: (P:${primaryHost.name},C:${destStatus.currentlyUsedHostName})"
						href="/do/transfer/host/${primaryHost.name}">${primaryHost.networkCode}</a><c:if test="${not destStatus.usingPrimaryHost}">*</c:if></td>

					<td style="text-align: right;"><c:if test="${destStatus.queueSize > 0}">
						<c:set var="statusFilter" value="WAIT" />
					</c:if> <c:if test="${destStatus.queueSize == 0}">
						<c:set var="statusFilter" value="DONE" />
					</c:if> <a style="font-weight:bold" href="/do/transfer/destination/${d.name}?status=${statusFilter}&dataStream=All&disseminationStream=All&fileNameSearch=&date=<content:content name='monSesForm.updated' dateFormatKey='date.format.iso'/>"
						title="${d.comment}">${d.name}</a></td>

					<td><c:if test="${destStatus.badDataTransfersSize gt 0}">
						<a href="/do/monitoring/unsuccessful/${d.name}"><img
							border="0"
							src="<content:icon key="image.transfer.unsuccessful"/>"
							title="${d.name}: ${destStatus.badDataTransfersSize} outstanding transfer(s)" /></a>
					</c:if> <c:if test="${destStatus.badDataTransfersSize == 0}">
						<img src="<content:icon key="image.transfer.successful"/>"
							title="${d.name}: ${destStatus.badDataTransfersSize} outstanding transfer(s)" />
					</c:if></td>

					<td>${destStatus.queueSize}</td>

					<td><c:set var="transL" value="${d.lastTransfer}" /> <c:if
						test="${not empty transL}">
						<c:catch>
							<a title="${transL.realFinishTime}"
								href="/do/transfer/data/${transL.id}"><content:content
								name="transL.realFinishTime"
								dateFormatKey="date.format.time.short" defaultValue="Not Set"
								ignoreNull="true" /></a>
						</c:catch>
					</c:if> <c:if test="${empty transL}">
						<i>None</i>
					</c:if></td>

					<td><c:set var="transE" value="${d.lastError}" /> <c:if
						test="${not empty transE}">
						<c:catch>
							<a title="${transE.failedTime}"
								href="<bean:message key="datatransfer.basepath"/>/${transE.id}"><content:content
								name="transE.failedTime" dateFormatKey="date.format.time.short"
								defaultValue="Not Set" ignoreNull="true" /></a>
						</c:catch>
					</c:if> <c:if test="${empty transE}">
						<i>None</i>
					</c:if></td>

					<td><img height="12" width="12"
						src="/assets/images/ecpds/b${destStatus.bigSisterStatus}.png"
						border="0"
						title="OV Status: '${destStatus.bigSisterStatusComment}'" /></td>

					<!-- End Destination Info -->

					<!-- Per Product Info -->

					<c:forEach var="productStatus" items="${reqData.productWindow}">

						<c:set var="key"
							value="${d.name}@${productStatus.product}@${productStatus.time}" />
						<c:set var="status" value="${reqData.status[key]}" />

						<c:if test="${not empty status}">
							<c:set var="arrStatus" value="${status.generationStatus}" />
							<c:set var="tranStatus" value="${status.realTimeTransferStatus}" />

							<c:if test="${arrStatus == '1'}">
							<td><a 	href="/do/monitoring/arrival/${d.name}/${productStatus.product}/${productStatus.time}?date=<content:content name="productStatus.productTime" ignoreNull="true" defaultValue="" dateFormatKey="date.format.iso"/>"><img
								height="12" width="12"
								src="/assets/images/ecpds/g${arrStatus}.png" border="0"
								title="${d.name}: ${productStatus.time}-${productStatus.product}" /></a>
							</td>
							<td><a href="/do/monitoring/transfer/${d.name}/${productStatus.product}/${productStatus.time}?date=<content:content name="productStatus.productTime" ignoreNull="true" defaultValue="" dateFormatKey="date.format.iso"/>"><img
								height="12" width="12"
								src="/assets/images/ecpds/t${tranStatus}.png" border="0"
								title="${d.name}: ${productStatus.time}-${productStatus.product}" /></a>
							</td>
							</c:if>
							<c:if test="${arrStatus != '1'}">
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</c:if>
						</c:if>
						<c:if test="${empty status}">
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						</c:if>

					</c:forEach>


					<!-- End of Per Product Info -->


				</tr>

				<c:if test="${(fStatus.index % reqData.stepsPerColumn) == (reqData.stepsPerColumn - 1) && not fStatus.last}">
					<tr class="titles">
						<tiles:insert page="./pds/monitoring/top_level/destination_titles.jsp" />
						<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
					</tr>
		</table>
		</td>

		<td class="holder">
		<table class="pagelevel">
			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>
			</c:if>

			</c:forEach>


			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>
		</table>

		</td>
	</tr>
</table>