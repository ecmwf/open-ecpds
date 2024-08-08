<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<p/>

<table>

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/datafile/transfergroup/data.jsp"/>

</td>
</tr>

<tr >
<td width="25">
<a valign="top" href="/do/datafile/transfergroup"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="/do/datafile/transfergroup">All Transfer Groups</a>
</td>
</tr>

</table>

