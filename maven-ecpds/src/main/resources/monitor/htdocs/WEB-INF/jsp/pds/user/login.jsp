<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<form action="login" method="POST">

<b><auth:info property="commonName" /> </b>

<table>
<tr><td>User</td><td><input name="user"/></td></tr>
<tr><td>Pass</td><td><input type="password" name="password"/></td></tr>
<tr><td></td><td><input type="submit" value="Go!"/></td></tr>
</table>

</form>