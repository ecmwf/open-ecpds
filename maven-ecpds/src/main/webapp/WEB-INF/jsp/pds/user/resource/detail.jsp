<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<p/>

<table>

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/user/resource/data.jsp"/>

</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<tr >
<td width="25">
<a valign="top" href="/do/user/resource"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="/do/user/resource">All Web Resources</a>
</td>
</tr>

</table>
