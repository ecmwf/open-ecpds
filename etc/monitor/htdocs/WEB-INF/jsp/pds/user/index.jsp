<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(255,193,7,0.07); border-left:4px solid #ffc107; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-shield-lock text-warning flex-shrink-0 mt-1"></i>
        <span>
            <strong><%=System.getProperty("monitor.nickName")%></strong> maintains two user types:
            <strong>Web Users</strong> (access to this monitoring interface, governed by Categories and Resources)
            and <strong>Data Users</strong> (access to the Data Portal, governed by Policies).
            Event logs are available for both user types for auditing and troubleshooting.
        </span>
    </div>
</div>

<%-- Web Access group --%>
<h6 class="text-muted fw-semibold mb-2 d-flex align-items-center gap-2">
    <i class="bi bi-person-gear"></i> Web Access
</h6>
<div class="row row-cols-1 row-cols-md-2 g-3 mb-4">

    <div class="col">
    <auth:link basePathKey="user.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-people text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Web Users</span>
            <p class="tool-desc">Manage accounts that have access to this monitoring interface. Assign categories
            to control which sections and actions each user can access.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="category.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-folder text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Web Categories</span>
            <p class="tool-desc">Define named permission groups (categories) that bundle a set of web resources.
            Assign categories to web users to grant or restrict access to specific pages.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="resource.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-files text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Web Resources</span>
            <p class="tool-desc">View and manage individual URL resources that can be granted or denied per category.
            Resources map directly to pages and actions in the monitoring interface.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="event.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-journal-text text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Web Events Log</span>
            <p class="tool-desc">Audit log of web user activity, including logins, page visits, and configuration
            changes. Useful for security reviews and troubleshooting access issues.</p>
        </div>
    </div>
    </auth:link>
    </div>

</div>

<%-- Data Portal Access group --%>
<h6 class="text-muted fw-semibold mb-2 d-flex align-items-center gap-2">
    <i class="bi bi-person-badge"></i> Data Portal Access
</h6>
<div class="row row-cols-1 row-cols-md-2 g-3">

    <div class="col">
    <auth:link basePathKey="incoming.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-person-badge text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Users</span>
            <p class="tool-desc">Manage accounts that access the Data Portal (WebDAV, HTTPS downloads).
            Each data user is linked to one or more data policies that control what they can retrieve.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="policy.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-shield-check text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Policies</span>
            <p class="tool-desc">Define access policies that grant data users permission to retrieve specific
            destinations or data streams. Policies control what a data user is allowed to download.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="history.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-list-ul text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Events Log</span>
            <p class="tool-desc">Audit log of Data Portal access events, including downloads and authentication
            attempts. Useful for usage tracking and investigating access anomalies.</p>
        </div>
    </div>
    </auth:link>
    </div>

</div>
