<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%
    final String certStatus = (String) request.getAttribute("certStatus");
    final boolean certError   = "ERROR".equals(certStatus);
    final boolean certWarning = "WARNING".equals(certStatus);
    final String certIconClass = "bi-shield-lock";
    final String certCardBorder = certError   ? " border border-danger-subtle\" style=\"background:rgba(220,53,69,0.05);"
                                 : certWarning ? " border border-warning-subtle\" style=\"background:rgba(255,193,7,0.05);"
                                               : "";
    final String certTitleClass = "tool-title";
    final String certBadge = certError   ? "<span class=\"badge bg-danger ms-2\">Expired</span>"
                           : certWarning ? "<span class=\"badge bg-warning text-dark ms-2\">Attention</span>"
                                         : "";
    final boolean capNotSet = Boolean.TRUE.equals(request.getAttribute("criticalPasswordNotSet"));
%>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(108,117,125,0.07); border-left:4px solid #6c757d; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-gear text-secondary flex-shrink-0 mt-1"></i>
        <span>
            Administrative tools for managing files and operations in
            <strong><%=System.getProperty("monitor.nickName")%></strong>.
            These actions may affect live data flows, use with care.
        </span>
    </div>
</div>

<% if (capNotSet) { %>
<div class="alert alert-warning d-flex align-items-start gap-3 mb-4" role="alert">
    <i class="bi bi-key-fill flex-shrink-0 mt-1" style="font-size:1.2rem;"></i>
    <div>
        <strong>Critical Password not configured.</strong>
        This password is required to perform irreversible administrative actions such as <em>Purge All Data</em>.
        <a href="/do/admin/criticalpassword" class="alert-link ms-1">Set it now &rarr;</a>
    </div>
</div>
<% } %>
<% if (certError) { %>
<div class="alert alert-danger d-flex align-items-start gap-3 mb-4" role="alert">
    <i class="bi bi-shield-x flex-shrink-0 mt-1" style="font-size:1.2rem;"></i>
    <div>
        <strong>One or more TLS certificates have expired.</strong>
        Expired certificates may prevent secure connections to Monitor Servers or Data Movers.
        <a href="/do/admin/certificates" class="alert-link ms-1">Review certificates &rarr;</a>
    </div>
</div>
<% } else if (certWarning) { %>
<div class="alert alert-warning d-flex align-items-start gap-3 mb-4" role="alert">
    <i class="bi bi-shield-exclamation flex-shrink-0 mt-1" style="font-size:1.2rem;"></i>
    <div>
        <strong>TLS certificate attention required.</strong>
        One or more certificates are self-signed or expiring within 30 days.
        <a href="/do/admin/certificates" class="alert-link ms-1">Review certificates &rarr;</a>
    </div>
</div>
<% } %>

<div class="row row-cols-1 row-cols-md-2 g-3">

    <auth:link basePathKey="admin.basepath" href="/filter">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-file-zip text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Compress Files</span>
            <p class="tool-desc">Run a compression simulation against a Destination to test filter rules before applying them
            to live data flows. Allows verifying file patterns and compression ratios without affecting production.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/requeue">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-hourglass-split text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Outstanding Transfers</span>
            <p class="tool-desc">View and manage files that are queued but have not yet been transferred. Use this tool
            to identify stuck or failed transfers matching a given file pattern, and requeue them for processing.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/upload">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-upload text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Upload Files</span>
            <p class="tool-desc">Upload a text-based configuration or data file directly to a Transfer Host.
            Useful for deploying scripts, property files or other assets to remote hosts without manual intervention.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.feedback.basepath" href="">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-chat-left-text text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">User Feedback</span>
            <p class="tool-desc">Review, acknowledge and manage feedback submitted by users of the monitoring interface.
            Includes ratings, comments, feature requests and contact information where provided.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/metafields">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-list-check text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Metadata Fields</span>
            <p class="tool-desc">Define, edit, and activate destination metadata fields. These definitions drive the
            auto-generated metadata forms on each destination page. Add new fields without any code changes.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/certificates">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3<%= certCardBorder %>">
        <i class="bi <%= certIconClass %> flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="<%= certTitleClass %>">TLS Certificates<%=certBadge%></span>
            <p class="tool-desc">Manage TLS certificates for the Monitor HTTPS server and all Data Movers.
            Generate, import, and deploy certificates from a single location without service interruption.</p>
        </div>
    </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/criticalpassword">
    <div class="col">
    <% if (capNotSet) { %>
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3 border border-warning-subtle"
         style="background:rgba(255,193,7,0.06);">
        <i class="bi bi-key-fill text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Critical Password <span class="badge bg-warning text-dark ms-1">Not Set</span></span>
            <p class="tool-desc">Set the Critical Password required to authorize irreversible
            administrative operations. This password is stored as a secure hash and is separate from your login
            credentials.</p>
        </div>
    </div>
    <% } else { %>
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-key-fill text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Critical Password</span>
            <p class="tool-desc">Renew the Critical Password required to authorize irreversible
            administrative operations. This password is stored as a secure hash and is separate from your login
            credentials.</p>
        </div>
    </div>
    <% } %>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/purge">
    <div class="col">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-trash3-fill text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Purge All Data</span>
            <p class="tool-desc">
                Permanently cancels all pending transfers and removes all files from every data mover's disk.
                Protected by a two-step confirmation and the Critical Password.
            </p>
        </div>
    </div>
    </div>
    </auth:link>

</div>
