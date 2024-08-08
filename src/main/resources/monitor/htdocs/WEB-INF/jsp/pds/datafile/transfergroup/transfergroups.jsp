<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>  
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<display:table name="${transfergroups}" id="row"  requestURI="" pagesize="50" defaultorder="descending" defaultsort="1" sort="list" class="listing">
    <display:column sortable="true" title="Name"><a href="/do/datafile/transfergroup/${row.id}">${row.name}</a></display:column>
    <display:column	property="comment"/>
    <display:column sortable="true" title="Enabled">
        <c:if test="${row.active}">yes</c:if><c:if test="${!row.active}"><font color="red">no</font></c:if>
    </display:column>
    <display:column title="Replicating">
        <c:if test="${row.replicate}">yes</c:if><c:if test="${!row.replicate}"><font color="red">no</font></c:if>
    </display:column>
    <display:column title="Filtering">
        <c:if test="${row.filter}">yes</c:if><c:if test="${!row.filter}"><font color="red">no</font></c:if>
    </display:column>
    <display:column sortable="true" title="Cluster Name" property="clusterName"/>
    <display:column title="Weight" property="clusterWeight"/>
    <display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" basePathKey="transfergroup.basepath" href="/edit/update_form/${row.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" basePathKey="transfergroup.basepath"
			href="/edit/delete_form/${row.id}" imageKey="icon.small.delete" />
	</display:column>
</display:table>


