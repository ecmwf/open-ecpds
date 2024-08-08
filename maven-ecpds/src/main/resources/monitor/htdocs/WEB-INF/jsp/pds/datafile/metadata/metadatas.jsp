<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>

<display:table id="row" name="${attributes}" requestURI="" pagesize="50" class="listing">
	<display:column title="Name"><a href="<bean:message key="metadata.basepath"/>/attribute/<bean:write name="row" property="name"/>"><bean:write name="row" property="name"/></a></display:column>
	<c:if test="${not empty attributeName}">
		<display:column property="value"/>
	</c:if>
	<display:caption>
		<c:if test="${not empty attributeName}">All Meta Data Values for parameter <i>${attributeName}</i></c:if>
		<c:if test="${empty attributeName}">All Meta Data Parameters</c:if>
	</display:caption>
</display:table>
