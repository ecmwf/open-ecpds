<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<p/>

<table style="width:100%">

<tr>
<td colspan="2">

<tiles:insert page="/WEB-INF/jsp/pds/datafile/transfergroup/data.jsp"/>

</td>
</tr>

<tr>
<td colspan="2" class="pt-2">
<a href="/do/datafile/transfergroup" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Transfer Groups
</a>
</td>
</tr>

</table>

