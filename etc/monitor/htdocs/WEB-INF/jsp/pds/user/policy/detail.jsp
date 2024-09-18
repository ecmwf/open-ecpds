<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>


<tiles:insert page="/WEB-INF/jsp/pds/user/policy/data.jsp"/>

<table>
<tr >
<td width="25">
<a valign="top" href="/do/user/policy"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="/do/user/policy">All Data Policies</a>
</td>
</tr>

</table>
