<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<p/>

<table>

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/transfer/destination/data.jsp"/>

</td>
</tr>

<auth:if basePathKey="destination.basepath" paths="">
<auth:then>
<tr>
<td width="25"><a valign="top" href="<bean:message key="destination.basepath"/>"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a></td>
<td><a valign="top" href="<bean:message key="destination.basepath"/>">All Destinations</a></td>
</tr>
</auth:then>
</auth:if>

</table>
