<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table class="spareBox2">
	<tr>
		<th><a href="/do/admin">Admin Tasks</a></th>
	</tr>
	<tr>
		<td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td>
	</tr>
	<auth:link basePathKey="admin.basepath" href="/filter"
		wrappingTags="tr,td"><i class="bi bi-file-zip"></i> Compress Files</auth:link>
	<auth:link basePathKey="admin.basepath" href="/requeue"
		wrappingTags="tr,td"><i class="bi bi-hourglass-split"></i> Outstanding Transfers</auth:link>
	<auth:link basePathKey="admin.basepath" href="/upload"
		wrappingTags="tr,td"><i class="bi bi-upload"></i> Upload Files</auth:link>
	<auth:link basePathKey="admin.feedback.basepath" href=""
		wrappingTags="tr,td"><i class="bi bi-chat-left-text"></i> User Feedback</auth:link>
	<auth:link basePathKey="admin.basepath" href="/metafields"
		wrappingTags="tr,td"><i class="bi bi-list-check"></i> Metadata Fields</auth:link>
</table>
