<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
<auth:then>

<table class="editSpareBox">
	<tr><th><bean:message key="ecpds.destination"/></th></tr>
	<tr><td></td></tr>
	<tr><td><auth:link basePathKey="destination.basepath" href="/edit/insert_form" imageKey="icon.small.insert">&nbsp;&nbsp;Create</auth:link></td></tr>
	<c:if test="${not empty destination.id}">
	<tr><td><auth:link basePathKey="destination.basepath" href="/edit/update_form/${destination.id}" imageKey="icon.small.update">&nbsp;&nbsp;Edit</auth:link></td></tr>
	<tr><td><auth:link basePathKey="destination.basepath" href="/edit/delete_form/${destination.id}" imageKey="icon.small.delete">&nbsp;&nbsp;Delete</auth:link></td></tr>
	<tr><td><auth:link basePathKey="destination.basepath" href="/edit/insert_form?fromDestination=${destination.name}" imageKey="icon.small.duplicate">&nbsp;&nbsp;Duplicate</auth:link></td></tr>
	</c:if>
</table>
</auth:then>
</auth:if>
