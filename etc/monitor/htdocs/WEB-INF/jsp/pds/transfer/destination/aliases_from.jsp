<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="aliasedFrom" value="${destination.aliasedFrom}" scope="page"/>

<c:if test="${empty aliasedFrom}">
<br/>
<div class="alert">
  Destination <c:out value="${destination.name}" /> is not Aliased From any Destination
</div>
</c:if>

<c:if test="${!empty aliasedFrom}">

<display:table id="alias" name="${aliasedFrom}" requestURI="" class="listing">
    <display:column sortable="true" title="Name"><a href="/do/transfer/destination/${alias.name}">${alias.name}</a></display:column>
    <display:column sortable="true" title="Type">${alias.typeText}</display:column>
    <display:column sortable="true" title="Status">${alias.formattedStatus}</display:column>
    <display:column sortable="false" title="Rules">${alias.dataAlias}</display:column>
    <display:column sortable="false" title="Comment">${alias.comment}</display:column>
    <display:caption>Destination ${destination.name} is Aliased From the following Destination(s):</display:caption>
</display:table>

</c:if>
