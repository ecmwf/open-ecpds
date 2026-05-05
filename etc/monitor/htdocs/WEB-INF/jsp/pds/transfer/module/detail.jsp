<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table style="width:100%">

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/transfer/module/data.jsp"/>

</td>
</tr>

<tr><td colspan="2"> &nbsp; </td></tr>

<tr>
<td colspan="2" class="pt-2">
<a href="<bean:message key="module.basepath"/>" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Transfer Modules
</a>
</td>
</tr>

</table>
