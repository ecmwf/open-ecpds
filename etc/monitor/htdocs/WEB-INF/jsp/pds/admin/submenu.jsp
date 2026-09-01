<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ page import="ecmwf.ecpds.master.MasterManager" %>
<%
    // Resolve cert status — use pre-set request attribute if available (set by AdminFormAction / StartAction),
    // otherwise fetch directly so the decorators show on every admin page regardless of which action handled it.
    String _sm_certStatus = (String) request.getAttribute("certStatus");
    if (_sm_certStatus == null) {
        try { _sm_certStatus = MasterManager.getMI().getOverallCertStatus(); }
        catch (final Exception _e) { _sm_certStatus = "UNKNOWN"; }
    }
    final boolean _sm_certError   = "ERROR".equals(_sm_certStatus);
    final boolean _sm_certWarning = "WARNING".equals(_sm_certStatus);
    final String _sm_certIconClass = "bi-shield-lock";
    final String _sm_certDot = _sm_certError
        ? " <i class=\"bi bi-circle-fill text-danger ms-1\" style=\"font-size:0.45rem;vertical-align:middle;\"></i>"
        : _sm_certWarning
        ? " <i class=\"bi bi-circle-fill text-warning ms-1\" style=\"font-size:0.45rem;vertical-align:middle;\"></i>"
        : "";

    // Resolve cap status similarly.
    Boolean _sm_capNotSetAttr = (Boolean) request.getAttribute("criticalPasswordNotSet");
    boolean _sm_capNotSet;
    if (_sm_capNotSetAttr != null) {
        _sm_capNotSet = _sm_capNotSetAttr;
    } else {
        try { _sm_capNotSet = !MasterManager.getDB().hasCriticalActionPassword(); }
        catch (final Exception _e) { _sm_capNotSet = false; }
    }
    final String _sm_capDot = _sm_capNotSet
        ? " <i class=\"bi bi-circle-fill text-warning ms-1\" style=\"font-size:0.45rem;vertical-align:middle;\"></i>"
        : "";

    // Resolve unreviewed-feedback status.
    Boolean _sm_feedbackAttr = (Boolean) request.getAttribute("hasUnreviewedFeedback");
    boolean _sm_hasFeedback;
    if (_sm_feedbackAttr != null) {
        _sm_hasFeedback = _sm_feedbackAttr;
    } else {
        try { _sm_hasFeedback = MasterManager.getMI().hasUnreviewedFeedback(); }
        catch (final Exception _e) { _sm_hasFeedback = false; }
    }
    final String _sm_feedbackDot = _sm_hasFeedback
        ? " <i class=\"bi bi-circle-fill text-warning ms-1\" style=\"font-size:0.45rem;vertical-align:middle;\"></i>"
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
		wrappingTags="tr,td"><i class="bi bi-chat-left-text"></i> User Feedback<%=_sm_feedbackDot%></auth:link>
	<auth:link basePathKey="admin.basepath" href="/metafields"
		wrappingTags="tr,td"><i class="bi bi-list-check"></i> Metadata Fields</auth:link>
	<auth:link basePathKey="admin.basepath" href="/certificates"
		wrappingTags="tr,td"><i class="bi <%=_sm_certIconClass%>"></i> TLS Certificates<%=_sm_certDot%></auth:link>
	<auth:link basePathKey="admin.basepath" href="/criticalpassword"
		wrappingTags="tr,td"><i class="bi bi-key-fill"></i> Critical Password<%=_sm_capDot%></auth:link>
	<auth:link basePathKey="admin.basepath" href="/purge"
		wrappingTags="tr,td"><i class="bi bi-trash3-fill"></i> Purge All Data</auth:link>
</table>
