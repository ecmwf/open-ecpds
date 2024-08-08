<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>

<table class="form" border=0>
<tr class="form">
<td class="form_title">Id</td><td class="form_field"><html:text property="id"/></td>
</tr>
<tr class="form">
<td class="form_title">Name</td><td class="form_field"><html:text property="name"/></td>
</tr>
<tr class="form">
<td class="form_title">Comment</td><td class="form_field"><html:text property="comment"/></td>
</tr>
</table>
