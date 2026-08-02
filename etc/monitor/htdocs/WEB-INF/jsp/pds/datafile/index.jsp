<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(13,110,253,0.05); border-left:4px solid #0d6efd; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-database text-primary flex-shrink-0 mt-1"></i>
        <span>
            <strong><%=System.getProperty("monitor.nickName")%></strong> stores Data Files with their associated Metadata
            across multiple Data Movers for redundancy. Files are replicated within Transfer Groups,
            which are organised into Clusters sharing a common Network (Internet, RMDCN, LAN).
        </span>
    </div>
</div>

<div class="row row-cols-1 row-cols-md-2 g-3">

    <div class="col">
    <auth:link basePathKey="datafile.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-file-earmark-text text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Files</span>
            <p class="tool-desc">Browse and search data files stored across the Data Movers. Inspect file metadata,
            replication status, and storage location within Transfer Groups.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="metadata.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-tags text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Meta Data</span>
            <p class="tool-desc">View and manage metadata attributes attached to data files.
            Metadata is used to describe, classify, and route files through the dissemination system.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transfergroup.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-collection text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Transfer Groups</span>
            <p class="tool-desc">Manage logical groups of Data Movers that share replicated storage.
            Transfer Groups are organised into Clusters connected by a common network.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="transferserver.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-server text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Movers</span>
            <p class="tool-desc">View and monitor individual Data Mover servers. Check status, disk usage,
            active connections, and certificate information for each mover in the cluster.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="retrievalmonitoring.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-speedometer2 text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Retrieval Rates</span>
            <p class="tool-desc">Monitor file retrieval performance across Data Movers. Tracks rates and
            latency to help identify bottlenecks in data access and delivery.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="moverdownloads.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-grid-3x3 text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Download Activity</span>
            <p class="tool-desc">Visualise concurrent download activity as a matrix across all Data Movers.
            Useful for spotting uneven load distribution or saturated movers.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="datarates.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-bar-chart-line text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Data Rates</span>
            <p class="tool-desc">Charts of historical data transfer throughput across movers and destinations.
            Helps with capacity planning and identifying periods of high or low activity.</p>
        </div>
    </div>
    </auth:link>
    </div>

    <div class="col">
    <auth:link basePathKey="portaltraffic.basepath" href="">
    <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
        <i class="bi bi-graph-up-arrow text-secondary flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
        <div>
            <span class="tool-title">Portal Traffic</span>
            <p class="tool-desc">Statistics for Data Portal access including WebDAV and HTTPS downloads.
            Tracks request volumes, user activity, and bandwidth usage over time.</p>
        </div>
    </div>
    </auth:link>
    </div>

</div>
