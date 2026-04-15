<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="d-flex align-items-start gap-2 mb-4 px-2 py-2 rounded"
     style="background:rgba(25,135,84,0.05); border-left:4px solid #198754; font-size:0.85rem; color:var(--bs-body-color); max-width:700px; margin: 0 auto;">
    <i class="bi bi-send text-success ms-1 flex-shrink-0 mt-1"></i>
    <span>
        <strong><%=System.getProperty("monitor.nickName")%></strong> manages Data Transfers, each transfer request is linked to
        a Data File and routed through a <strong>Destination</strong>, which acts as a delivery queue with its own scheduler,
        priorities, retry logic, and parallel transmission settings. Destinations use <strong>Transfer Hosts</strong>
        (primary + backup) that define the protocol, credentials and target directory for each delivery.
    </span>
</div>

<div class="row g-3" style="max-width:700px; margin: 0 auto;">
    <div class="col-12">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#e9f7ef;">
                <i class="bi bi-send text-success"></i> Transmission Options
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="transfer.basepath" href="/data" wrappingTags="li"><i class="bi bi-arrow-left-right"></i>Data Transfers</auth:link>
                <auth:link basePathKey="transfer.basepath" href="/destination" wrappingTags="li"><i class="bi bi-geo-alt"></i>Destinations</auth:link>
                <auth:link basePathKey="transfer.basepath" href="/host" wrappingTags="li"><i class="bi bi-pc-display"></i>Transfer Hosts</auth:link>
                <auth:link basePathKey="transfer.basepath" href="/history" wrappingTags="li"><i class="bi bi-clock-history"></i>Transfer History</auth:link>
                <auth:link basePathKey="transfer.basepath" href="/method" wrappingTags="li"><i class="bi bi-diagram-3"></i>Transfer Methods</auth:link>
                <auth:link basePathKey="transfer.basepath" href="/module" wrappingTags="li"><i class="bi bi-puzzle"></i>Transfer Modules</auth:link>
            </ul>
        </div>
    </div>
</div>
