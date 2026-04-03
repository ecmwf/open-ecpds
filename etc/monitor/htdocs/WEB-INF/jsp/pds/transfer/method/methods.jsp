<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<display:table name="${methods}" id="row" requestURI="" sort="list" class="listing">
    <display:column title="Name" sortable="true">
    	<a href="<bean:message key="method.basepath"/>/${row.id}">${row.name}</a>
    </display:column>
    <display:column sortable="true" property="comment" />
    <display:column sortable="true" title="Restrict">
        <c:if test="${row.restrict}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
        <c:if test="${!row.restrict}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
    </display:column>
    <display:column sortable="true" title="Resolve">
        <c:if test="${row.resolve}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
        <c:if test="${!row.resolve}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
    </display:column>
    <display:column sortable="true" title="Enabled">
         <c:if test="${row.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if><c:if test="${!row.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
    </display:column>
    <display:column class="buttons">
    	<auth:link styleClass="menuitem" href="/do/transfer/method/edit/update_form/${row.id}" imageKey="icon.small.update"/>
	<auth:link styleClass="menuitem" href="/do/transfer/method/edit/delete_form/${row.id}" imageKey="icon.small.delete"/>
    </display:column>
</display:table>
