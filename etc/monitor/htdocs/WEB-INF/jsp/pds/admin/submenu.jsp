<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%
    final String _sm_certStatus  = (String) request.getAttribute("certStatus");
    final boolean _sm_certError   = "ERROR".equals(_sm_certStatus);
    final boolean _sm_certWarning = "WARNING".equals(_sm_certStatus);
    final String _sm_certIconClass = _sm_certError   ? "bi-shield-x text-danger"
                                   : _sm_certWarning ? "bi-shield-exclamation text-warning"
                                                     : "bi-shield-lock";
    final String _sm_certTextClass = _sm_certError   ? " text-danger"
                                   : _sm_certWarning ? " text-warning"
                                                     : "";
%>

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
	<auth:link basePathKey="admin.basepath" href="/certificates"
		wrappingTags="tr,td"><i class="bi <%=_sm_certIconClass%>"></i><span class="<%=_sm_certTextClass%>"> TLS Certificates</span></auth:link>
	<auth:link basePathKey="admin.basepath" href="/criticalpassword"
		wrappingTags="tr,td"><i class="bi bi-key-fill"></i> Critical Action Password</auth:link>
	<auth:link basePathKey="admin.basepath" href="/purge"
		wrappingTags="tr,td"><i class="bi bi-trash3-fill text-danger"></i> <span class="text-danger">Purge All Data</span></auth:link>
</table>
