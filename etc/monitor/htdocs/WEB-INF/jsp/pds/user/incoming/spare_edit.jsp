<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<auth:if basePathKey="incoming.basepath" paths="/edit/insert_form">
<auth:then>

<table class="editSpareBox">
	<tr><th><a href="<bean:message key="incoming.basepath"/>"><bean:message key="ecpds.incoming"/></a></th></tr>
	<tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
	<tr><td><auth:link basePathKey="incoming.basepath" href="/edit/insert_form" imageKey="icon.small.insert">&nbsp;&nbsp;Create</auth:link></td></tr>
	<c:if test="${not empty incoming.id}">
	<tr><td><auth:link basePathKey="incoming.basepath" href="/edit/update_form/${incoming.id}" imageKey="icon.small.update">&nbsp;&nbsp;Edit</auth:link></td></tr>
	<tr><td><auth:link basePathKey="incoming.basepath" href="/edit/delete_form/${incoming.id}" imageKey="icon.small.delete">&nbsp;&nbsp;Delete</auth:link></td></tr>
	<tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
	<tr><td><a href="/do/user/incoming/portaltraffic/${incoming.id}"><i class="bi bi-graph-up-arrow"></i>&nbsp;&nbsp;Portal Traffic</a></td></tr>
	</c:if>
</table>
</auth:then>
</auth:if>
