<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="d-flex align-items-start gap-2 mb-4 px-2 py-2 rounded"
     style="background:rgba(255,193,7,0.07); border-left:4px solid #ffc107; font-size:0.85rem; color:var(--bs-body-color); max-width:680px; margin: 0 auto;">
    <i class="bi bi-shield-lock text-warning ms-1 flex-shrink-0 mt-1"></i>
    <span>
        <strong><%=System.getProperty("monitor.nickName")%></strong> maintains two user types:
        <strong>Web Users</strong> (access to this monitoring interface, governed by Categories and Resources)
        and <strong>Data Users</strong> (access to the Data Portal, governed by Policies).
        Event logs are available for both user types for auditing and troubleshooting.
    </span>
</div>

<div class="row g-3" style="max-width:680px; margin: 0 auto;">
    <div class="col-md-6">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#fff8e6;">
                <i class="bi bi-person-gear text-warning"></i> Web Access
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="user.basepath" href="" wrappingTags="li"><i class="bi bi-people"></i>Web Users</auth:link>
                <auth:link basePathKey="category.basepath" href="" wrappingTags="li"><i class="bi bi-folder"></i>Web Categories</auth:link>
                <auth:link basePathKey="resource.basepath" href="" wrappingTags="li"><i class="bi bi-files"></i>Web Resources</auth:link>
                <auth:link basePathKey="event.basepath" href="" wrappingTags="li"><i class="bi bi-journal-text"></i>Web Event Log</auth:link>
            </ul>
        </div>
    </div>
    <div class="col-md-6">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#fff8e6;">
                <i class="bi bi-person-badge text-warning"></i> Data Portal Access
            </div>
            <ul class="home-menu">
                <auth:link basePathKey="incoming.basepath" href="" wrappingTags="li"><i class="bi bi-person-badge"></i>Data Users</auth:link>
                <auth:link basePathKey="policy.basepath" href="" wrappingTags="li"><i class="bi bi-shield-check"></i>Data Policies</auth:link>
                <auth:link basePathKey="history.basepath" href="" wrappingTags="li"><i class="bi bi-list-ul"></i>Data Event Log</auth:link>
            </ul>
        </div>
    </div>
</div>
