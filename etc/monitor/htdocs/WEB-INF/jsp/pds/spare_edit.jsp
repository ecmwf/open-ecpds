<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:useAttribute id="beanName" name="bean.name" ignore="true" classname="java.lang.String"/>

<auth:if basePathKey="${beanName}.basepath" paths="/edit/insert_form">
<auth:then>

<table class="editSpareBox">
	<tr><th><bean:message key="ecpds.${beanName}"/></th></tr>
	<tr><td></td></tr>	
	<tr><td><auth:link basePathKey="${beanName}.basepath" href="/edit/insert_form" imageKey="icon.small.insert">&nbsp;&nbsp;Create</auth:link></td></tr>
	<c:set var="beanId" value="${requestScope[beanName].id}"/>
	<c:if test="${not empty beanId}">
	<tr><td><auth:link basePathKey="${beanName}.basepath" href="/edit/update_form/${beanId}" imageKey="icon.small.update">&nbsp;&nbsp;Edit</auth:link></td></tr>
	<tr><td><auth:link basePathKey="${beanName}.basepath" href="/edit/delete_form/${beanId}" imageKey="icon.small.delete">&nbsp;&nbsp;Delete</auth:link></td></tr>
	</c:if>
</table>
</auth:then>
</auth:if>