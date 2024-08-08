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
<tiles:insert page="/WEB-INF/jsp/pds/datafile/transferserver/data.jsp"/>
</td>
</tr>
<tr >
<td width="25">
<a valign="top" href="/do/datafile/transferserver"><img src="<content:icon key="icon.arrow.left"/>" border="0"/></a>
</td>
<td>
<a valign="top" href="/do/datafile/transferserver">All Transfer Servers</a>
</td>
</tr>

</table>

