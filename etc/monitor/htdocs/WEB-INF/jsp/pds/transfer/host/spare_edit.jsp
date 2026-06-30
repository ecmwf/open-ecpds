<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<auth:if basePathKey="host.basepath" paths="/edit/insert_form">
<auth:then>

<table class="editSpareBox">
	<tr><th><bean:message key="ecpds.host"/></th></tr>
	<tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
	<tr><td><auth:link basePathKey="host.basepath" href="/edit/insert_form" imageKey="icon.small.insert">&nbsp;&nbsp;Create</auth:link></td></tr>
	<c:if test="${not empty host.id}">
	<tr><td><auth:link basePathKey="host.basepath" href="/edit/update_form/${host.id}" imageKey="icon.small.update">&nbsp;&nbsp;Edit</auth:link></td></tr>
	<tr><td><auth:link basePathKey="host.basepath" href="/edit/delete_form/${host.id}" imageKey="icon.small.delete">&nbsp;&nbsp;Delete</auth:link></td></tr>
	<c:if test="${not empty host.destinations}">
	<auth:if basePathKey="transferhistory.basepath" paths="/">
	<auth:then>
	<tr><td><a href="#" onclick="ecpdsHostDuplicate('${host.id}','${host.nickName}');return false;"><img src='<bean:message key="icon.small.duplicate"/>' alt=""/> &nbsp;Duplicate</a></td></tr>
	</auth:then>
	</auth:if>
	</c:if>
	</c:if>
</table>
</auth:then>
</auth:if>
