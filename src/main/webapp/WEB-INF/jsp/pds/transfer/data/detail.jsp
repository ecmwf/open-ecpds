<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<p/>

<table>

<tr>
<td colspan="2">

<tiles:insert page="./pds/transfer/data/data.jsp"/>

</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<auth:if basePathKey="datatransfer.basepath" paths="">
<auth:then>
<tr >
<td width="25">
<a valign="top" href="<bean:message key="datatransfer.basepath"/>"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" class="menuitem" href="<bean:message key="datatransfer.basepath"/>">All Data Transfers</a>
</td>
</tr>
</auth:then>
</auth:if>

</table>
