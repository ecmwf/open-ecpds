<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<p/>

<table>

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/transfer/method/data.jsp"/>

</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<tr>
<td colspan="2" class="pt-2">
<a href="<bean:message key="method.basepath"/>" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Transfer Methods
</a>
</td>
</tr>

</table>
