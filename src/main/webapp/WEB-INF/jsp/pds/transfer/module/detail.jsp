<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table>

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/transfer/module/data.jsp"/>

</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<tr >
<td width="25">
<a valign="top" href="<bean:message key="module.basepath"/>"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="<bean:message key="module.basepath"/>">All Transfer Modules</a>
</td>
</tr>

</table>
