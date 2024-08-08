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
<tiles:insert page="/WEB-INF/jsp/pds/user/category/data.jsp"/>
</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<tr >
<td width="25">
<a valign="top" href="/do/user/category"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="/do/user/category">All Web Categories</a>
</td>
</tr>

</table>
