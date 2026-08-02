<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(25,135,84,0.05); border-left:4px solid var(--bs-border-color,#dee2e6); font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-send text-success flex-shrink-0 mt-1"></i>
        <span>
            <strong><%=System.getProperty("monitor.nickName")%></strong> manages Data Transfers, each transfer request is linked to
            a Data File and routed through a <strong>Destination</strong>, which acts as a delivery queue with its own scheduler,
            priorities, retry logic, and parallel transmission settings. Destinations use <strong>Transfer Hosts</strong>
            (primary + backup) that define the protocol, credentials and target directory for each delivery.
        </span>
    </div>
</div>

<div class="row row-cols-1 row-cols-md-2 g-3">

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/data">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-arrow-left-right text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Transfers</span>
            <p class="tool-desc">Browse individual data transfer requests across all destinations. Search by file name,
            status, or date, and inspect transfer details, retry history, and error messages.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/destination">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-geo-alt text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Destinations</span>
            <p class="tool-desc">Manage delivery queues for data dissemination. Each destination defines its own
            scheduler, retry logic, priorities, and the transfer hosts used for delivery.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/host">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-pc-display text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Transfer Hosts</span>
            <p class="tool-desc">Configure primary and backup remote hosts used by destinations. Defines the protocol,
            credentials, target directory, and connection parameters for each delivery endpoint.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/history">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-clock-history text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Transfer History</span>
            <p class="tool-desc">Review the completed transfer log across all destinations and dates. Useful for
            auditing deliveries and investigating past failures or retries.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/method">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-diagram-3 text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Transfer Methods</span>
            <p class="tool-desc">Define named transfer methods that combine a Transfer Module with a set of default
            parameters. Methods are reused across multiple Transfer Hosts to avoid duplication.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfer.basepath" href="/module">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-puzzle text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Transfer Modules</span>
            <p class="tool-desc">View available ECtrans protocol modules (FTP, SFTP, S3, Azure, WebDAV, etc.).
            Modules implement the actual transfer logic used by Transfer Methods and Hosts.</p>
        </div>
    </div>
    </auth:link>
    </div>

</div>
