<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert page="/WEB-INF/jsp/pds/user/user/data.jsp"/>

<table>
<tr >
<td width="25">
<a valign="top" href="/do/user/user"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<auth:link basePathKey="event.basepath" href="/${user.id}">Web Event Logs for ${user.id}</auth:link> or
<a valign="top" href="/do/user/user">All Web Users</a>
</td>
</tr>

</table>
