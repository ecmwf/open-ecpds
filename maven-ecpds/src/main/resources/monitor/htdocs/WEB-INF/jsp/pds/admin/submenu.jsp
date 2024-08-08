<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table class="spareBox2">
	<tr>
		<th><a href="/do/admin">Admin Tasks</a></th>
	</tr>
	<tr>
		<td></td>
	</tr>
	<auth:link basePathKey="admin.basepath" href="/filter"
		wrappingTags="tr,td">Compress Files</auth:link>
	<auth:link basePathKey="admin.basepath" href="/requeue"
		wrappingTags="tr,td">Outstanding Files</auth:link>
	<auth:link basePathKey="admin.basepath" href="/upload"
		wrappingTags="tr,td">Upload Files</auth:link>
</table>
