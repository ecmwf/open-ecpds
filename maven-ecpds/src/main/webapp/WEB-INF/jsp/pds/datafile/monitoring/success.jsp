<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty ratesList}">
  <div class="alert">
    <span class="closebtn" onclick="parent.history.back();">&times;</span>
    No Rates found based on these criteria!
  </div>
</c:if>
<c:if test="${!empty ratesList}">
<c:if test="${option == 'rates'}">
<display:table name="${ratesList}" id="rates" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Date" sortable="true">${rates.date}</display:column>
	<display:column title="Transfer Group" sortable="true">${rates.transferGroupName}</display:column>
	<display:column title="File(s)" sortable="true">${rates.count}</display:column>
	<display:column title="Bytes" sortable="true" sortProperty="bytes"><a STYLE="TEXT-DECORATION: NONE" title="Size: ${rates.formattedBytes}">${rates.bytes}</a></display:column>
	<display:column title="Duration (ms)" sortable="true" sortProperty="duration"><a STYLE="TEXT-DECORATION: NONE" title="Duration: ${rates.formattedDuration}">${rates.duration}</a></display:column>
	<display:column title="Mbits/s" sortable="true" sortProperty="rate"><a STYLE="TEXT-DECORATION: NONE" title="Rate: ${rates.formattedRate}">${rates.rate}</a></display:column>
        <display:caption>Rates per Transfer Group (selection: ${caller}/${sourceHost})</display:caption>
</display:table>
</c:if>
<c:if test="${option == 'ratesPerTransferServer'}">
<display:table name="${ratesList}" id="rates" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Date" sortable="true">${rates.date}</display:column>
	<display:column title="Transfer Group" sortable="true">${rates.transferGroupName}</display:column>
	<display:column title="Transfer Server" sortable="true">${rates.transferServerName}</display:column>#
	<display:column title="File(s)" sortable="true">${rates.count}</display:column>
	<display:column title="Bytes" sortable="true" sortProperty="bytes"><a STYLE="TEXT-DECORATION: NONE" title="Size: ${rates.formattedBytes}">${rates.bytes}</a></display:column>
	<display:column title="Duration (ms)" sortable="true" sortProperty="duration"><a STYLE="TEXT-DECORATION: NONE" title="Duration: ${rates.formattedDuration}">${rates.duration}</a></display:column>
	<display:column title="Mbits/s" sortable="true" sortProperty="rate"><a STYLE="TEXT-DECORATION: NONE" title="Rate: ${rates.formattedRate}">${rates.rate}</a></display:column>
	<display:caption>Rates per Transfer Server (selection: ${caller}/${sourceHost})</display:caption>
</display:table>
</c:if>
<c:if test="${option == 'ratesPerFileSystem'}">
<display:table name="${ratesList}" id="rates" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Date" sortable="true">${rates.date}</display:column>
	<display:column title="File System" sortable="true">${rates.fileSystem}</display:column>
	<display:column title="File(s)" sortable="true">${rates.count}</display:column>
	<display:column title="Bytes" sortable="true" sortProperty="bytes"><a STYLE="TEXT-DECORATION: NONE" title="Size: ${rates.formattedBytes}">${rates.bytes}</a></display:column>
	<display:column title="Duration (ms)" sortable="true" sortProperty="duration"><a STYLE="TEXT-DECORATION: NONE" title="Duration: ${rates.formattedDuration}">${rates.duration}</a></display:column>
	<display:column title="Mbits/s" sortable="true" sortProperty="rate"><a STYLE="TEXT-DECORATION: NONE" title="Rate: ${rates.formattedRate}">${rates.rate}</a></display:column>
	<display:caption>Rates per File System for Transfer Server ${transferServerName} (selection: ${caller}/${sourceHost})</display:caption>
</display:table>
</c:if>
</c:if>
