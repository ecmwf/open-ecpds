<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<div class="d-flex align-items-start gap-2 mb-4 px-2 py-2 rounded"
     style="background:rgba(13,110,253,0.05); border-left:4px solid #0d6efd; font-size:0.85rem; color:var(--bs-body-color); max-width:640px; margin: 0 auto;">
    <i class="bi bi-database text-primary ms-1 flex-shrink-0 mt-1"></i>
    <span>
        <strong><%=System.getProperty("monitor.nickName")%></strong> stores Data Files with their associated Metadata
        across multiple Transfer Servers for redundancy. Files are replicated within Transfer Groups,
        which are organised into Clusters sharing a common Network (Internet, RMDCN, LAN).
    </span>
</div>

<div class="row g-3" style="max-width:640px; margin: 0 auto;">
    <div class="col-12">
        <div class="home-section-card">
            <div class="home-section-hdr" style="background:#e8f4fd;">
                <i class="bi bi-database text-primary"></i> Data Storage Options
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
</div>
