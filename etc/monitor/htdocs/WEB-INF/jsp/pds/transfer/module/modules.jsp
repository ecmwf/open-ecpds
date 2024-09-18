<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<display:table name="${ectransmodules}" id="row" requestURI="" sort="list" class="listing">
    <display:column sortable="true" title="Name"><a href="<bean:message key="module.basepath"/>/${row.id}">${row.name}</a></display:column>
    <display:column sortable="true" title="Class Name">${row.classe}</display:column>
    <display:column sortable="true" title="Class Path">
	<c:if test="${fn:length(row.archive) gt 0}">${row.archive}</c:if><c:if test="${fn:length(row.archive) eq 0}"><i><font color="red">default</font></i></c:if>
    </display:column>
    <display:column sortable="true" title="Enabled">
        <c:if test="${row.active}">yes</c:if><c:if test="${!row.active}"><font color="red">no</font></c:if>
    </display:column>
    <display:column class="buttons">
    	<auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/update_form/${row.id}" imageKey="icon.small.update"/>
	<auth:link styleClass="menuitem" basePathKey="module.basepath" href="/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
    </display:column>
</display:table>
