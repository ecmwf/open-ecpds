<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="aliases" value="${destination.aliases}" scope="page"/>

<c:if test="${empty aliases}">
<br/>
<div class="alert">
  No Alias defined on Destination <c:out value="${destination.name}"/>
</div>
</c:if>

<c:if test="${!empty aliases}">

<td colspan="2" valign="top">

<display:table id="alias" name="${aliases}" requestURI="" class="listing">
    <display:column sortable="true" title="Name"><a href="/do/transfer/destination/${alias.name}">${alias.name}</a></display:column>
    <display:column sortable="true" title="Type">${alias.typeText}</display:column>
    <display:column sortable="true" title="Status">${alias.formattedStatus}</display:column>
    <display:column sortable="false" title="Rules">${alias.dataAlias}</font></display:column>
    <display:column sortable="false" title="Comment">${alias.comment}</display:column>
    <display:caption>Destination ${destination.name} has Aliases To the following Destination(s):</display:caption>
</display:table>

</c:if>
