<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<p>

<html:errors />

<tiles:useAttribute name="action" classname="java.lang.String"/>
<tiles:useAttribute id="actionFormName" name="action.form.name" classname="java.lang.String"/>

<bean:define id="htmlFormAction" value='<%=action+"*"%>' type="java.lang.String"/>
<bean:define id="actionFormId" name="<%=actionFormName%>" property="id" type="java.lang.String"/>
<bean:define id="realFormAction" value="<%=action+actionFormId%>" type="java.lang.String"/>
<bean:define id="operation" value="" type="java.lang.String"/>

<%
	if (action.endsWith("/insert")) {
		htmlFormAction = action;
		realFormAction = action;	
		operation = "insert";
	} else if (action.indexOf("/delete/")>=0) {
		operation = "delete";
	} else {
		operation = "update";
	}
%>

<!--<html:form action='<%=htmlFormAction%>'>-->
<form name="<%=actionFormName%>" action="/do<%=realFormAction%>" method="post">

<table width=100%>
<tr>
<td>
<tiles:insert name="body">
	<tiles:put name="isInsert" value='<%=Boolean.toString(action.endsWith("/insert"))%>'/>
	<tiles:put name="isDelete" value='<%=Boolean.toString(action.indexOf("/delete/")>=0)%>' />
	<tiles:put name="action.form.name" value="<%=actionFormName%>" />
</tiles:insert>
<br/>
</td>
<tr>
<td align="left">
<tiles:insert name="buttons">
	<tiles:put name="operation" value="<%=operation%>" />
</tiles:insert>	
<br/>
</td>
</tr>
</table>

</html:form>