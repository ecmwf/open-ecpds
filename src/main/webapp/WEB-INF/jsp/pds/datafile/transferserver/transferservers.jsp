<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<display:table id="server" name="${transferservers}" requestURI="" pagesize="50" defaultsort="1" sort="list" class="listing">
    <display:column title="Name" sortable="true"><a href="/do/datafile/transferserver/${server.name}">${server.name}</a></display:column>
    <display:column property="host" />
    <display:column property="port" />
    <display:column title="Enabled" sortable="true">
        <c:if test="${server.active}">yes</c:if><c:if test="${!server.active}"><font color="red">no</font></c:if>
    </display:column>
    <display:column title="Replicating">
        <c:if test="${server.replicate}">yes</c:if><c:if test="${!server.replicate}"><font color="red">no</font></c:if>
    </display:column>
    <display:column title="Last Update" sortable="true">
    	<content:content name="server.lastUpdateDate" dateFormatKey="date.format.transfer" ignoreNull="true"/>
    </display:column>
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/datafile/transferserver/edit/update_form/${server.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/datafile/transferserver/edit/delete_form/${server.id}" imageKey="icon.small.delete"/>
	</display:column>
</display:table>
