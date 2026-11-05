<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(13,202,240,0.07); border-left:4px solid #0dcaf0; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-eye text-info flex-shrink-0 mt-1"></i>
        <span>Operational views for monitoring product status, transfers, destinations and system activity.</span>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/pds/common/system_messages_banner.jsp" />
<jsp:include page="/WEB-INF/jsp/pds/admin/eventjournal/banner.jsp" />

<div class="row row-cols-1 row-cols-md-2 g-3">
    <auth:link basePathKey="monitoring.basepath" href="">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-grid-3x3-gap-fill text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">Monitoring Overview</span>
                    <p class="tool-desc">Review product and destination status together, with arrival and transfer information for each cycle. Identify delays and follow activity across the system.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <auth:link basePathKey="monitoring.basepath" href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-broadcast-pin text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">Dissemination</span>
                    <p class="tool-desc">Track outgoing product deliveries to destinations, from queued transfers through completion or failure. Open a product or destination to investigate transfer details.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <auth:link basePathKey="monitoring.basepath" href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-cloud-arrow-down text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">Acquisition</span>
                    <p class="tool-desc">Follow incoming data retrieval and acquisition activity across products and destinations. Check transfer progress and investigate missing or delayed data.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <auth:link basePathKey="monitoring.basepath" href="/globe">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-globe2 text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">Live Earth</span>
                    <p class="tool-desc">Explore active and recently completed transfers between hosts and destinations on a live global map. View transfer status, current throughput and recent traffic.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <auth:link basePathKey="monitoring.basepath" href="/topology">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-diagram-3 text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">System Topology</span>
                    <p class="tool-desc">View the Master, Monitor and Data Movers grouped by host, with their connections and current status. Quickly spot unavailable components in the deployment.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <auth:link basePathKey="monitoring.basepath" href="/mqtt">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-broadcast text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">MQTT Activity</span>
                    <p class="tool-desc">Watch publication activity and broker processing outcomes live. Follow notifications from Data Portal uploads or acquisition through the Master to each Mover's MQTT broker.</p>
                </div>
            </div>
        </div>
    </auth:link>

    <div class="col">
        <c:if test="${journalEnabled or journalUnavailable}"><a href="/do/monitoring/eventjournal" class="d-block h-100 text-decoration-none text-reset"></c:if>
        <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3 ${journalDanger ? 'border border-danger-subtle bg-danger-subtle' : journalAttention or journalUnavailable ? 'border border-warning-subtle bg-warning-subtle' : ''} ${not journalEnabled and not journalUnavailable ? 'opacity-50' : ''}"
             <c:if test="${not journalEnabled and not journalUnavailable}">aria-disabled="true" title="OpenTelemetry database journaling is disabled on the Master. Pending rows may still exist."</c:if>>
            <i class="bi bi-database-check ${journalDanger ? 'text-danger' : journalAttention or journalUnavailable ? 'text-warning' : 'text-info'} flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
            <div>
                <span class="tool-title">OpenTelemetry Journal
                    <c:choose><c:when test="${journalAttention or journalUnavailable}"><span class="badge ${journalDanger ? 'bg-danger' : 'bg-warning text-dark'} ms-1">Attention</span></c:when>
                    <c:when test="${not journalEnabled}"><span class="badge bg-secondary ms-1">Disabled</span></c:when></c:choose>
                </span>
                <p class="tool-desc">Monitor telemetry queue health, dropped events and export acknowledgements.
                    Pause or resume collection and delivery with audited runtime controls. Splunk/file logging is unaffected.</p>
            </div>
        </div>
        <c:if test="${journalEnabled or journalUnavailable}"></a></c:if>
    </div>

    <auth:link basePathKey="monitoring.basepath" href="/delaynotification">
        <div class="col">
            <div class="admin-tool h-100 p-3 d-flex align-items-start gap-3">
                <i class="bi bi-bell text-info flex-shrink-0" style="font-size:1.6rem; margin-top:0.1rem;"></i>
                <div>
                    <span class="tool-title">Delay Notification</span>
                    <p class="tool-desc">Prepare notices for delayed products, with recipients based on the destinations receiving them. Track sent notices and update or resolve them when products resume.</p>
                </div>
            </div>
        </div>
    </auth:link>
</div>
