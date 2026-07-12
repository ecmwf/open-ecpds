<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<div class="d-flex align-items-start gap-2 mb-4 px-2 py-2 rounded"
     style="background:rgba(108,117,125,0.07); border-left:4px solid #6c757d; font-size:0.85rem; color:var(--bs-body-color); max-width:680px; margin: 0 auto;">
    <i class="bi bi-gear text-secondary ms-1 flex-shrink-0 mt-1"></i>
    <span>
        Administrative tools for managing files and operations in
        <strong><%=System.getProperty("monitor.nickName")%></strong>.
        These actions may affect live data flows, use with care.
    </span>
</div>

<div class="d-flex flex-column gap-3" style="max-width:680px; margin: 0 auto;">

    <auth:link basePathKey="admin.basepath" href="/filter">
    <div class="admin-tool p-3 d-flex align-items-start gap-3">
        <i class="bi bi-file-zip text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Compress Files</span>
            <p class="tool-desc">Run a compression simulation against a Destination to test filter rules before applying them
            to live data flows. Allows verifying file patterns and compression ratios without affecting production.</p>
        </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/requeue">
    <div class="admin-tool p-3 d-flex align-items-start gap-3">
        <i class="bi bi-hourglass-split text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Outstanding Transfers</span>
            <p class="tool-desc">View and manage files that are queued but have not yet been transferred. Use this tool
            to identify stuck or failed transfers matching a given file pattern, and requeue them for processing.</p>
        </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/upload">
    <div class="admin-tool p-3 d-flex align-items-start gap-3">
        <i class="bi bi-upload text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Upload Files</span>
            <p class="tool-desc">Upload a text-based configuration or data file directly to a Transfer Host.
            Useful for deploying scripts, property files or other assets to remote hosts without manual intervention.</p>
        </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.feedback.basepath" href="">
    <div class="admin-tool p-3 d-flex align-items-start gap-3">
        <i class="bi bi-chat-left-text text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">User Feedback</span>
            <p class="tool-desc">Review, acknowledge and manage feedback submitted by users of the monitoring interface.
            Includes ratings, comments, feature requests and contact information where provided.</p>
        </div>
    </div>
    </auth:link>

    <auth:link basePathKey="admin.basepath" href="/metafields">
    <div class="admin-tool p-3 d-flex align-items-start gap-3">
        <i class="bi bi-list-check text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Metadata Fields</span>
            <p class="tool-desc">Define, edit, and activate destination metadata fields. These definitions drive the
            auto-generated metadata forms on each destination page. Add new fields without any code changes.</p>
        </div>
    </div>
    </auth:link>

</div>
