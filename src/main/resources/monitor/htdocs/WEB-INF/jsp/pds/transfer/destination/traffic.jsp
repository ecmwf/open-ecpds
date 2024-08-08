<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:if test="${empty destination.trafficList}">
<br/>
<div class="alert">
  No Data Rate available for Destination <c:out value="${destination.name}"/>
</div>
</c:if>

<c:if test="${!empty destination.trafficList}">

<display:table name="${destination.trafficList}" id="traffic" requestURI="" sort="list" pagesize="25" class="listing">
	<display:column title="Date" sortable="true">${traffic.date}</display:column>
	<display:column title="Bytes" sortable="true" sortProperty="bytes"><a STYLE="TEXT-DECORATION: NONE" title="Size: ${traffic.formattedBytes}">${traffic.bytes}</a></display:column>
	<display:column title="Duration (ms)" sortable="true" sortProperty="duration"><a STYLE="TEXT-DECORATION: NONE" title="Duration: ${traffic.formattedDuration}">${traffic.duration}</a></display:column>
	<display:column title="Mbits/s" sortable="true" sortProperty="rate"><a STYLE="TEXT-DECORATION: NONE" title="Rate: ${traffic.formattedRate}">${traffic.rate}</a></display:column>
	<display:column title="Files Count" sortable="true">${traffic.files}</display:column>
</display:table>

</c:if>
