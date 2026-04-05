<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%-- Page intro: system description + Data Portal + login notice --%>
<div class="mb-4 px-3 py-3 rounded" style="background:rgba(13,110,253,0.05); border-left:4px solid #0d6efd; font-size:0.85rem; color:#495057; max-width:860px;">

    <%-- Block 1: system description --%>
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-info-circle text-primary flex-shrink-0 mt-1"></i>
        <span>
            The <%=System.getProperty("monitor.title")%> (<strong><%=System.getProperty("monitor.nickName")%></strong>)
            is a persistent repository for retrieving observational data and distributing meteorological products
            using protocols such as:
            <span class="d-inline-flex flex-wrap gap-1 ms-1 align-items-center">
                <span class="badge bg-primary">FTP</span>
                <span class="badge bg-primary">SFTP</span>
                <span class="badge bg-primary">FTPS</span>
                <span class="badge bg-primary">HTTP/S</span>
                <span class="badge bg-primary">Amazon S3</span>
                <span class="badge bg-primary">Azure</span>
                <span class="badge bg-primary">Google Cloud Storage</span>
            </span>
        </span>
    </div>

    <div style="border-top:1px dashed rgba(13,110,253,0.2); margin:0.65rem 0;"></div>

    <%-- Block 2: Data Portal --%>
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-cloud-arrow-down text-secondary flex-shrink-0 mt-1"></i>
        <span>
            It also implements a <strong>Data Portal</strong> accessible via:
            <span class="d-inline-flex flex-wrap gap-1 ms-1 align-items-center">
                <span class="badge bg-secondary">FTP</span>
                <span class="badge bg-secondary">SFTP</span>
                <span class="badge bg-secondary">SCP</span>
                <span class="badge bg-secondary">HTTPS</span>
                <span class="badge bg-secondary">Amazon S3</span>
            </span>
            <span class="d-block mt-1" style="color:#6c757d;">
                <i class="bi bi-person-badge text-secondary"></i>
                Data User credentials are required to access this service.
                Key-Based Authentication is allowed with <strong>SFTP</strong> and <strong>SCP</strong>.
            </span>
        </span>
    </div>

    <div style="border-top:1px dashed rgba(13,110,253,0.2); margin:0.65rem 0;"></div>

    <%-- Block 3: login notice --%>
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-person-circle text-secondary flex-shrink-0 mt-1"></i>
        <span>
            You are logged in as <strong><auth:info property="commonName"/></strong> (<auth:info property="uid"/>).
            <span class="d-block mt-1" style="color:#6c757d;">
                <i class="bi bi-lock text-secondary"></i>
                The sections and menu items displayed below reflect your access permissions; options you are not authorised to use are automatically hidden.
            </span>
        </span>
    </div>

</div>

<div class="row g-3" style="max-width:1100px;">

    <%-- Data Storage --%>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#e8f4fd;">
                <a href="/do/datafile" class="home-section-hdr-link"><i class="bi bi-database text-primary"></i> Data Storage</a>
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="datafile.basepath" href="" wrappingTags="li"><i class="bi bi-file-earmark-text"></i>Data Files</auth:link>
                <auth:link basePathKey="metadata.basepath" href="" wrappingTags="li"><i class="bi bi-tags"></i>Meta Data</auth:link>
                <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="li"><i class="bi bi-collection"></i>Transfer Groups</auth:link>
                <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="li"><i class="bi bi-server"></i>Transfer Servers</auth:link>
                <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="li"><i class="bi bi-speedometer2"></i>Retrieval Rates</auth:link>
            </ul>
        </div>
    </div>

    <%-- Transmission --%>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#e9f7ef;">
                <a href="/do/transfer" class="home-section-hdr-link"><i class="bi bi-send text-success"></i> Transmission</a>
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="datatransfer.basepath" href="" wrappingTags="li"><i class="bi bi-arrow-left-right"></i>Data Transfers</auth:link>
                <auth:link basePathKey="destination.basepath" href="?destinationType=-1" wrappingTags="li"><i class="bi bi-geo-alt"></i>Destinations</auth:link>
                <auth:if basePathKey="transferhistory.basepath" paths="/">
                    <auth:then>
                        <auth:link basePathKey="destination.basepath" href="?destinationType=-2" wrappingTags="li"><i class="bi bi-broadcast"></i>Dissemination</auth:link>
                        <auth:link basePathKey="destination.basepath" href="?destinationType=-3" wrappingTags="li"><i class="bi bi-cloud-download"></i>Acquisition</auth:link>
                    </auth:then>
                </auth:if>
                <auth:link basePathKey="host.basepath" href="" wrappingTags="li"><i class="bi bi-pc-display"></i>Transfer Hosts</auth:link>
                <auth:link basePathKey="transferhistory.basepath" href="" wrappingTags="li"><i class="bi bi-clock-history"></i>Transfer History</auth:link>
                <auth:link basePathKey="method.basepath" href="" wrappingTags="li"><i class="bi bi-diagram-3"></i>Transfer Methods</auth:link>
                <auth:link basePathKey="module.basepath" href="" wrappingTags="li"><i class="bi bi-puzzle"></i>Transfer Modules</auth:link>
            </ul>
        </div>
    </div>

    <%-- Access Control --%>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#fff8e6;">
                <a href="/do/user" class="home-section-hdr-link"><i class="bi bi-shield-lock text-warning"></i> Access Control</a>
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="user.basepath" href="" wrappingTags="li"><i class="bi bi-people"></i>Web Users</auth:link>
                <auth:link basePathKey="category.basepath" href="" wrappingTags="li"><i class="bi bi-folder"></i>Web Categories</auth:link>
                <auth:link basePathKey="resource.basepath" href="" wrappingTags="li"><i class="bi bi-files"></i>Web Resources</auth:link>
                <auth:link basePathKey="event.basepath" href="" wrappingTags="li"><i class="bi bi-journal-text"></i>Web Event Log</auth:link>
                <auth:link basePathKey="incoming.basepath" href="" wrappingTags="li"><i class="bi bi-person-badge"></i>Data Users</auth:link>
                <auth:link basePathKey="policy.basepath" href="" wrappingTags="li"><i class="bi bi-shield-check"></i>Data Policies</auth:link>
                <auth:link basePathKey="history.basepath" href="" wrappingTags="li"><i class="bi bi-list-ul"></i>Data Event Log</auth:link>
            </ul>
        </div>
    </div>

    <%-- Admin Tasks --%>
    <div class="col-xl-4 col-md-6 d-flex flex-column">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#f3f4f6;">
                <a href="/do/admin" class="home-section-hdr-link"><i class="bi bi-gear text-secondary"></i> Admin Tasks</a>
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="admin.basepath" href="/filter" wrappingTags="li"><i class="bi bi-file-zip"></i>Compress Files</auth:link>
                <auth:link basePathKey="admin.basepath" href="/requeue" wrappingTags="li"><i class="bi bi-hourglass-split"></i>Outstanding Files</auth:link>
                <auth:link basePathKey="admin.basepath" href="/upload" wrappingTags="li"><i class="bi bi-upload"></i>Upload Files</auth:link>
            </ul>
        </div>
    </div>

    <%-- Monitoring (conditional) --%>
    <auth:if basePathKey="transferhistory.basepath" paths="/">
        <auth:then>
            <div class="col-xl-4 col-md-6 d-flex flex-column">
                <div class="home-section-card">
                    <div class="home-section-hdr" style="background:#e8f7f7;">
                        <i class="bi bi-eye text-info"></i> Monitoring
                    </div>
                    <ul class="home-menu">
                        <li><a href="/maps/maps.html"><i class="bi bi-map"></i>Hosts on Map</a></li>
                        <auth:link basePathKey="monitoring.basepath" href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&" wrappingTags="li"><i class="bi bi-broadcast-pin"></i>Dissemination</auth:link>
                        <auth:link basePathKey="monitoring.basepath" href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&" wrappingTags="li"><i class="bi bi-cloud-arrow-down"></i>Acquisition</auth:link>
                    </ul>
                </div>
            </div>
        </auth:then>
    </auth:if>

</div>
