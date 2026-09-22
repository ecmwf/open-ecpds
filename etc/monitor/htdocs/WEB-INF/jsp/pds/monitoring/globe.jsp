<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<link rel="stylesheet" href="/cesium/Widgets/widgets.css" />
<style>
#globeHeader{display:flex;align-items:center;justify-content:space-between;gap:.75rem;flex-wrap:wrap;margin-bottom:.65rem;padding-bottom:.6rem;border-bottom:1px solid var(--bs-border-color);}
#globeHeader .title-group{display:flex;align-items:center;gap:.6rem;}
#globeHeader .actions-group{display:flex;align-items:center;gap:.5rem;}
.globe-icon-btn{display:inline-flex;align-items:center;justify-content:center;width:30px;height:30px;padding:0;border:none;border-radius:6px;background:transparent;color:var(--bs-secondary-color,#6c757d);font-size:1rem;line-height:1;transition:background .15s ease,color .15s ease;}
.globe-icon-btn i{display:inline-flex;align-items:center;justify-content:center;width:100%;height:100%;}
.globe-icon-btn:hover{background:var(--bs-tertiary-bg,#f1f3f5);color:var(--bs-body-color,#212529);}
.globe-icon-btn:focus{outline:none;box-shadow:0 0 0 .15rem rgba(13,202,240,.35);}
.globe-icon-btn.active{color:#0dcaf0;background:rgba(13,202,240,.12);}
#globeStatusBadge{display:inline-flex;align-items:center;gap:.4rem;height:30px;padding:0 .7rem;border-radius:8px;font-size:.72rem;font-weight:600;line-height:1;letter-spacing:.01em;}
#globeStatusBadge::before{content:"";width:7px;height:7px;border-radius:50%;background:currentColor;opacity:.9;}
.globe-badge-connected{background:rgba(25,135,84,.15);color:#198754;}
.globe-badge-connecting{background:rgba(153,116,4,.15);color:#997404;}
.globe-badge-disconnected{background:rgba(220,53,69,.15);color:#dc3545;}
#globeHeader .title-group i{font-size:1.4rem;}
#globeHeader .title-text{font-size:1.05rem;font-weight:600;line-height:1.15;}
#globeHeader .title-text .btn{font-size:.75rem;margin-left:.35rem;vertical-align:1px;}
#globeHeader .subtitle{font-size:.76rem;color:var(--bs-secondary-color,#6c757d);}
#globeContainer{position:relative;width:100%;height:70vh;min-height:420px;border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.15);}
#globeLegend{position:absolute;left:10px;top:10px;z-index:10;background:rgba(20,25,30,.72);color:#eee;border-radius:8px;padding:.5rem .75rem;font-size:.78rem;line-height:1.5;backdrop-filter:blur(2px);}
#globeLegend .dot{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:5px;}
#globeBottomRightPanels{position:absolute;right:10px;bottom:10px;z-index:10;display:flex;flex-direction:column;align-items:flex-end;gap:10px;max-height:calc(100% - 20px);pointer-events:none;}
#globeBottomRightPanels>div,#globeBottomRightPanels>button{pointer-events:auto;position:static;}
#globeStatsPanel{width:410px;max-width:min(410px,92vw);max-height:calc(100% - 20px);overflow:auto;background:linear-gradient(160deg,rgba(22,27,34,.9),rgba(14,18,24,.86));color:#eee;border:1px solid rgba(255,255,255,.07);border-radius:12px;padding:.7rem;font-size:.74rem;line-height:1.3;backdrop-filter:blur(6px);box-shadow:0 8px 28px rgba(0,0,0,.4);}
.globe-stats-row{display:grid;grid-template-columns:repeat(4,1fr);gap:.5rem;}
.globe-stats-row+.globe-stats-row{margin-top:.5rem;}
@media (max-width: 480px){.globe-stats-row{grid-template-columns:repeat(2,1fr);}}
.globe-stat-tile{display:flex;flex-direction:column;align-items:center;background:rgba(255,255,255,.045);border:1px solid rgba(255,255,255,.05);border-radius:10px;padding:.5rem .25rem .55rem;min-height:92px;box-sizing:border-box;text-align:center;}
.globe-stat-graphic{flex:1 1 auto;min-height:0;width:100%;display:flex;align-items:center;justify-content:center;}
.globe-stat-tile i{font-size:1.3rem;color:#38bdf8;}
.globe-stat-text{display:flex;flex-direction:column;align-items:center;line-height:1.15;max-width:100%;}
.globe-stat-value{font-size:.9rem;font-weight:700;font-variant-numeric:tabular-nums;color:#fff;overflow-wrap:break-word;max-width:100%;}
.globe-stat-label{font-size:.62rem;color:#9aa5b1;text-transform:uppercase;letter-spacing:.02em;margin-top:1px;white-space:nowrap;}
.globe-stat-gauge .globe-gauge-svg{width:58px;height:34px;filter:drop-shadow(0 0 3px rgba(56,189,248,.35));}
.globe-gauge-arc-bg{fill:none;stroke:rgba(255,255,255,.14);stroke-width:9;stroke-linecap:round;}
.globe-gauge-arc{fill:none;stroke:#38bdf8;stroke-width:9;stroke-linecap:round;transition:stroke-dashoffset .8s ease,stroke .8s ease;}
/* On narrow (phone) screens the KPI panel at ~92vw width would otherwise sit on top of and hide almost the entire
   globe. #globeStatsToggleBtn stays hidden and unused above the breakpoint (desktop/tablet keep today's
   always-visible panel, unchanged) and only appears below it, replacing the panel with a small pill the user taps
   to show/hide the KPIs on demand - so on a phone it is always either "see the globe" or "see the KPIs", never both
   fighting for the same space. */
#globeStatsToggleBtn{display:none;}
@media (max-width:700px){
    #globeStatsToggleBtn{display:flex;align-items:center;justify-content:center;gap:.35rem;height:36px;padding:0 .85rem;border-radius:18px;background:rgba(20,25,30,.86);color:#9fd6ff;border:1px solid rgba(255,255,255,.12);box-shadow:0 4px 14px rgba(0,0,0,.35);font-size:.76rem;font-weight:600;cursor:pointer;}
    #globeStatsToggleBtn i{font-size:.95rem;}
    #globeStatsPanel{display:none;}
    #globeStatsPanel.globe-stats-open{display:block;}
}
[data-bs-theme=light] #globeStatsToggleBtn{background:#f6f8fa;color:#0969da;border-color:rgba(0,0,0,.08);box-shadow:0 4px 14px rgba(0,0,0,.18);}
#globeRightPanels{position:absolute;right:10px;top:10px;z-index:10;display:flex;flex-direction:column;align-items:flex-end;gap:10px;max-height:calc(100% - 20px);pointer-events:none;}
#globeRightPanels>div{pointer-events:auto;position:static;}
#globeInfoPanel{width:290px;max-width:80vw;background:rgba(20,25,30,.86);color:#eee;border-radius:8px;padding:.75rem 1rem;font-size:.82rem;display:none;box-shadow:0 4px 16px rgba(0,0,0,.35);}
#globeInfoPanel h6{color:#9fd6ff;margin-bottom:.4rem;}
#globeInfoPanel .close-btn{position:absolute;top:6px;right:8px;cursor:pointer;color:#ccc;}
#globeInfoPanel dl{margin:0;}
#globeInfoPanel dt{color:#aaa;font-weight:400;}
#globeInfoPanel dd{margin-bottom:.35rem;word-break:break-all;}
#globeOriginWarning{max-width:min(230px,55vw);background:rgba(20,25,30,.86);color:#eee;border-radius:8px;padding:.5rem .7rem;font-size:.76rem;line-height:1.35;display:none;box-shadow:0 4px 16px rgba(0,0,0,.35);}
#globeOriginWarning i{margin-right:.35rem;color:#997404;}
#globeOriginWarningDetail{color:#aaa;font-size:.9em;margin-top:.15rem;word-break:break-word;}
#globeOriginWarning a{color:#9fd6ff;}
#globeCountryTable{max-width:min(280px,70vw);max-height:42%;overflow:auto;background:rgba(20,25,30,.86);color:#eee;border-radius:8px;padding:.5rem .7rem;font-size:.76rem;line-height:1.4;display:none;box-shadow:0 4px 16px rgba(0,0,0,.35);}
.globe-country-table-title{font-weight:600;color:#9fd6ff;margin-bottom:.3rem;}
#globeCountryTable table{width:100%;border-collapse:collapse;}
#globeCountryTable th{color:#9fd6ff;text-align:left;font-weight:600;padding:0 6px 4px 0;position:sticky;top:0;background:rgba(20,25,30,.86);}
#globeCountryTable td{padding:2px 6px 2px 0;white-space:nowrap;}
#globeCountryTable td.country-name{max-width:130px;overflow:hidden;text-overflow:ellipsis;}
.country-flag{font-size:1.15rem;cursor:default;}
.country-flyto-btn{margin-left:6px;cursor:pointer;color:#9fd6ff;font-size:.85rem;}
.country-flyto-btn:hover{color:#fff;}
.globe-muted-text{color:#bbb;}

/* All the floating overlay panels above default to a dark glass look, since they sit on top of the globe/space
   imagery which is dark regardless of page theme. That works fine while the page itself is in dark mode, but reads
   as an odd mismatch in the (default) light theme, so give them a light/day glass variant that follows the same
   "[data-bs-theme=light] #id{...}" pattern already used elsewhere in this app (see layout.jsp/ecpds.css) - the
   combined id+attribute selector naturally wins over the plain id rules above without needing !important. Elements
   whose color is instead set dynamically inline by JS (the active gauge arc / battery fill, both re-colored per
   reading via semantic thresholds) are intentionally left alone since those semantic colors already read fine on
   either background.  */
[data-bs-theme=light] #globeLegend{background:#f6f8fa;color:#1b1f24;box-shadow:0 2px 8px rgba(0,0,0,.18);}
[data-bs-theme=light] #globeStatsPanel{background:linear-gradient(160deg,#ffffff,#eef1f4);color:#1b1f24;border-color:rgba(0,0,0,.08);box-shadow:0 8px 24px rgba(0,0,0,.18);}
[data-bs-theme=light] .globe-stat-tile{background:rgba(0,0,0,.035);border-color:rgba(0,0,0,.07);}
[data-bs-theme=light] .globe-stat-value{color:#1b1f24;}
[data-bs-theme=light] .globe-stat-label{color:#57606a;}
[data-bs-theme=light] .globe-gauge-arc-bg{stroke:rgba(0,0,0,.12);}
[data-bs-theme=light] #globeInfoPanel{background:#f6f8fa;color:#1b1f24;box-shadow:0 4px 14px rgba(0,0,0,.18);}
[data-bs-theme=light] #globeInfoPanel h6{color:#0969da;}
[data-bs-theme=light] #globeInfoPanel dt{color:#57606a;}
[data-bs-theme=light] #globeInfoPanel .close-btn{color:#57606a;}
[data-bs-theme=light] #globeOriginWarning{background:#f6f8fa;color:#1b1f24;box-shadow:0 4px 14px rgba(0,0,0,.18);}
[data-bs-theme=light] #globeOriginWarning i{color:#997404;}
[data-bs-theme=light] #globeOriginWarningDetail{color:#57606a;}
[data-bs-theme=light] #globeOriginWarning a{color:#0969da;}
[data-bs-theme=light] #globeCountryTable{background:#f6f8fa;color:#1b1f24;box-shadow:0 4px 14px rgba(0,0,0,.18);}
[data-bs-theme=light] .globe-country-table-title{color:#0969da;}
[data-bs-theme=light] #globeCountryTable th{color:#0969da;background:#f6f8fa;}
[data-bs-theme=light] .country-flyto-btn{color:#0969da;}
[data-bs-theme=light] .country-flyto-btn:hover{color:#000;}
[data-bs-theme=light] .globe-muted-text{color:#57606a;}
</style>

<div id="globeHeader">
    <div class="title-group">
        <i class="bi bi-globe2 text-info"></i>
        <div>
            <div class="title-text">
                <%=System.getProperty("monitor.nickName")%> Live Earth
                <button class="btn btn-link btn-sm text-muted p-0 align-baseline" type="button"
                    data-bs-toggle="collapse" data-bs-target="#globeInfoCollapse"
                    aria-expanded="false" title="About this page">
                  <i class="bi bi-info-circle"></i>
                </button>
            </div>
            <div class="subtitle" id="globeSubtitle">Connecting...</div>
        </div>
    </div>
    <div class="actions-group">
        <span class="globe-badge-connecting" id="globeStatusBadge">Connecting...</span>
        <div class="dropdown d-inline-block">
            <button type="button" id="globeViewModeBtn" class="globe-icon-btn" title="Group arcs by host or country"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                <i class="bi bi-diagram-3"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end p-2" style="min-width:180px;" aria-labelledby="globeViewModeBtn">
                <li>
                    <div class="form-check mb-1">
                        <input class="form-check-input" type="radio" name="globeViewModeRadio" id="globeViewModeHost" value="host">
                        <label class="form-check-label" for="globeViewModeHost" style="font-size:.85rem;">Per host</label>
                    </div>
                </li>
                <li>
                    <div class="form-check mb-0">
                        <input class="form-check-input" type="radio" name="globeViewModeRadio" id="globeViewModeCountry" value="country">
                        <label class="form-check-label" for="globeViewModeCountry" style="font-size:.85rem;">Per country</label>
                    </div>
                </li>
            </ul>
        </div>
        <div class="dropdown d-inline-block">
            <button type="button" id="globeDirectionBtn" class="globe-icon-btn" title="Monitor Dissemination and/or Acquisition"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                <i class="bi bi-arrow-left-right"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end p-2" style="min-width:190px;" aria-labelledby="globeDirectionBtn">
                <li>
                    <div class="form-check mb-1">
                        <input class="form-check-input" type="radio" name="globeDirectionRadio" id="globeDirectionBoth" value="both">
                        <label class="form-check-label" for="globeDirectionBoth" style="font-size:.85rem;">Dissemination + Acquisition</label>
                    </div>
                </li>
                <li>
                    <div class="form-check mb-1">
                        <input class="form-check-input" type="radio" name="globeDirectionRadio" id="globeDirectionDiss" value="dissemination">
                        <label class="form-check-label" for="globeDirectionDiss" style="font-size:.85rem;">Dissemination only</label>
                    </div>
                </li>
                <li>
                    <div class="form-check mb-0">
                        <input class="form-check-input" type="radio" name="globeDirectionRadio" id="globeDirectionAcq" value="acquisition">
                        <label class="form-check-label" for="globeDirectionAcq" style="font-size:.85rem;">Acquisition only</label>
                    </div>
                </li>
            </ul>
        </div>
        <div class="dropdown d-inline-block">
            <button type="button" id="globeLabelsBtn" class="globe-icon-btn" title="Toggle country/town names"
                    data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                <i class="bi bi-tag"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end p-2" style="min-width:180px;" aria-labelledby="globeLabelsBtn">
                <li>
                    <div class="form-check form-switch mb-1">
                        <input class="form-check-input" type="checkbox" role="switch" id="globeCountryLabelsToggle">
                        <label class="form-check-label" for="globeCountryLabelsToggle" style="font-size:.85rem;">Country names</label>
                    </div>
                </li>
                <li>
                    <div class="form-check form-switch mb-0">
                        <input class="form-check-input" type="checkbox" role="switch" id="globeCityLabelsToggle">
                        <label class="form-check-label" for="globeCityLabelsToggle" style="font-size:.85rem;">Town names</label>
                    </div>
                </li>
            </ul>
        </div>
        <button type="button" id="globeRecenterBtn" class="globe-icon-btn" title="Re-center on OpenECPDS location">
            <i class="bi bi-crosshair"></i>
        </button>
        <button type="button" id="globeFullscreenBtn" class="globe-icon-btn" title="Toggle full screen">
            <i class="bi bi-arrows-fullscreen"></i>
        </button>
    </div>
</div>

<div class="collapse mb-3" id="globeInfoCollapse">
  <div class="px-3 py-2 border-bottom border-top" style="font-size:.82rem;background:var(--bs-tertiary-bg,#e9ecef);border-top-width:3px!important;border-top-color:var(--bs-primary,#0d6efd)!important;border-radius:6px;">
    <strong class="d-block mb-1">About the Live Earth page</strong>
    <p class="mb-2">
        This page visualises, in real time over a 3D globe, the traffic flowing between this
        <%=System.getProperty("monitor.nickName")%> installation and the Transfer Hosts/Destinations it exchanges
        data with (via the DataMovers' Dissemination and Acquisition transfers), plus the overall activity of the
        Data Portal (the FTP/HTTP/SFTP/S3/WebDAV interface used directly by Incoming Users). Everything on the globe
        and in the panels below updates live over a WebSocket feed refreshed every few seconds - no page reload
        needed.
    </p>
    <ul class="mb-2 ps-3">
        <li><strong>Arcs and points</strong> &mdash; an arc is drawn between a Transfer Host and either the origin
        marker (this installation's own location) or a Proxy Host, with the arrow pointing in the direction data is
        flowing: origin &rarr; host for Dissemination (data pushed out), host &rarr; origin for Acquisition (data
        pulled in). Colours follow the legend shown in the top-left corner of the globe.</li>
        <li><strong>Per host / per country</strong> and <strong>Dissemination / Acquisition</strong> (top-right
        icons) let you group and filter which arcs are shown.</li>
        <li><strong>Labels</strong> toggles country/town name overlays; the fullscreen button expands the globe to
        fill the whole browser window.</li>
    </ul>
    <strong class="d-block mb-1">Data Portal Activity panel (bottom-right)</strong>
    <ul class="mb-0 ps-3">
        <li><strong>Transfers</strong> &mdash; number of Dissemination/Acquisition transfers currently active
        (matches the current Direction filter).</li>
        <li><strong>Hosts</strong> &mdash; number of distinct Transfer Hosts with at least one active transfer.</li>
        <li><strong>Throughput</strong> &mdash; combined instantaneous transfer rate across all currently active
        transfers.</li>
        <li><strong>24h total</strong> &mdash; total volume transferred over the last 24 hours.</li>
        <li><strong>Sessions</strong> &mdash; number of Incoming Users currently connected to the Data Portal (FTP/
        HTTP/SFTP/S3/WebDAV), across every DataMover.</li>
        <li><strong>Data in / Data out</strong> &mdash; combined instantaneous upload/download rate of every open
        Data Portal session, across every DataMover.</li>
        <li><strong>Storage</strong> &mdash; aggregate used/total disk space across every DataMover volume, shown as
        a percentage (hover the tile for the exact used/total figures).</li>
    </ul>
  </div>
</div>

<div id="globeContainer">
    <div id="cesiumContainer" style="width:100%;height:100%;"></div>
    <div id="globeLegend">
        <div><span class="dot" style="background:#38bdf8;"></span>Active transfer</div>
        <div><span class="dot" style="background:#22c55e;"></span>Completed</div>
        <div><span class="dot" style="background:#ef4444;"></span>Failed / retransmitting</div>
        <div><span class="dot" style="background:#ffd166;"></span>OpenECPDS location</div>
        <div><span class="dot" style="background:#a78bfa;"></span>Proxy Host location</div>
        <div class="globe-muted-text" style="margin-top:.25rem;max-width:160px;">Arrows point in the direction data is flowing</div>
        <div id="globeUnresolvedNote" class="globe-muted-text" style="display:none;margin-top:.35rem;max-width:160px;font-size:.72rem;line-height:1.3;">
            <i class="bi bi-exclamation-triangle" style="color:#d9a441;margin-right:.3rem;"></i><span id="globeUnresolvedNoteText"></span>
        </div>
    </div>
    <div id="globeRightPanels">
        <div id="globeInfoPanel">
            <span class="close-btn" onclick="document.getElementById('globeInfoPanel').style.display='none';">&times;</span>
            <h6 id="globeInfoTitle">Transfer</h6>
            <dl id="globeInfoBody"></dl>
        </div>
        <div id="globeCountryTable">
            <div class="globe-country-table-title">Transfers by country</div>
            <table>
                <thead><tr><th>Country</th><th>Active</th><th>Throughput</th></tr></thead>
                <tbody id="globeCountryTableBody"></tbody>
            </table>
        </div>
    </div>
    <div id="globeBottomRightPanels">
    <div id="globeOriginWarning">
        <i class="bi bi-exclamation-triangle-fill"></i><strong>OpenECPDS location not configured.</strong>
        <div id="globeOriginWarningDetail"></div>
        <auth:link basePathKey="admin.basepath" href="/origin">Configure &rarr;</auth:link>
    </div>
    <div id="globeStatsPanel">
        <div class="globe-stats-row">
            <div class="globe-stat-tile">
                <div class="globe-stat-graphic"><i class="bi bi-arrow-left-right"></i></div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiTransfers">0</span>
                    <span class="globe-stat-label">Transfers</span>
                </div>
            </div>
            <div class="globe-stat-tile">
                <div class="globe-stat-graphic"><i class="bi bi-hdd-network"></i></div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiHosts">0</span>
                    <span class="globe-stat-label">Hosts</span>
                </div>
            </div>
            <div class="globe-stat-tile globe-stat-gauge">
                <div class="globe-stat-graphic">
                    <svg class="globe-gauge-svg" viewBox="0 0 120 68">
                        <path class="globe-gauge-arc-bg" d="M10,62 A50,50 0 0 1 110,62"></path>
                        <path class="globe-gauge-arc" id="gaugeArc" d="M10,62 A50,50 0 0 1 110,62"></path>
                    </svg>
                </div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiThroughput">0 bps</span>
                    <span class="globe-stat-label">Throughput</span>
                </div>
            </div>
            <div class="globe-stat-tile">
                <div class="globe-stat-graphic"><i class="bi bi-hdd-stack"></i></div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiBytes">0 B</span>
                    <span class="globe-stat-label">24h total</span>
                </div>
            </div>
        </div>
        <div class="globe-stats-row">
            <div class="globe-stat-tile">
                <div class="globe-stat-graphic"><i class="bi bi-people-fill"></i></div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiSessions">0</span>
                    <span class="globe-stat-label">Sessions</span>
                </div>
            </div>
            <div class="globe-stat-tile globe-stat-gauge">
                <div class="globe-stat-graphic">
                    <svg class="globe-gauge-svg" viewBox="0 0 120 68">
                        <path class="globe-gauge-arc-bg" d="M10,62 A50,50 0 0 1 110,62"></path>
                        <path class="globe-gauge-arc" id="gaugeArcIn" d="M10,62 A50,50 0 0 1 110,62"></path>
                    </svg>
                </div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiDataIn">0 bps</span>
                    <span class="globe-stat-label">Data in</span>
                </div>
            </div>
            <div class="globe-stat-tile globe-stat-gauge">
                <div class="globe-stat-graphic">
                    <svg class="globe-gauge-svg" viewBox="0 0 120 68">
                        <path class="globe-gauge-arc-bg" d="M10,62 A50,50 0 0 1 110,62"></path>
                        <path class="globe-gauge-arc" id="gaugeArcOut" d="M10,62 A50,50 0 0 1 110,62"></path>
                    </svg>
                </div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiDataOut">0 bps</span>
                    <span class="globe-stat-label">Data out</span>
                </div>
            </div>
            <div class="globe-stat-tile globe-stat-gauge" id="storageTile">
                <div class="globe-stat-graphic">
                    <svg class="globe-gauge-svg" viewBox="0 0 120 68">
                        <path class="globe-gauge-arc-bg" d="M10,62 A50,50 0 0 1 110,62"></path>
                        <path class="globe-gauge-arc" id="gaugeArcStorage" d="M10,62 A50,50 0 0 1 110,62"></path>
                    </svg>
                </div>
                <div class="globe-stat-text">
                    <span class="globe-stat-value" id="kpiStoragePct">0%</span>
                    <span class="globe-stat-label">Storage</span>
                </div>
            </div>
        </div>
    </div>
    <button type="button" id="globeStatsToggleBtn" title="Show/hide the KPI panel">
        <i class="bi bi-graph-up"></i><span id="globeStatsToggleLabel">KPIs</span>
    </button>
    </div>
</div>

<script src="/cesium/Cesium.js"></script>
<script>
(async function () {
    "use strict";

    // Sizes the globe container to fill exactly the remaining viewport height below it (rather than a hardcoded
    // "calc(100vh - Npx)" guess, which would need constant recalibration whenever the surrounding page chrome
    // changes), so there is never a leftover blank gap - nor an overflow requiring page scroll - beneath it. Kept
    // effortlessly correct across page zoom levels, browser chrome, and any future header/breadcrumb changes.
    //
    // The page also has a fixed, always-on-top "#bottomfooter" bar (see ecpds.css's "clears the fixed bottomfooter"
    // padding on #contentDiv) which does not push page content up the way a normal in-flow footer would, so its
    // height has to be subtracted here explicitly too - otherwise the container would be sized as if it could use
    // the full viewport height, and its bottom portion would end up rendered underneath that fixed footer.
    var globeContainerEl = document.getElementById("globeContainer");
    function resizeGlobeContainer() {
        if (document.fullscreenElement === globeContainerEl) {
            // The Fullscreen API already makes the element fill the whole screen; do not fight it.
            globeContainerEl.style.height = "";
            return;
        }
        var top = globeContainerEl.getBoundingClientRect().top;
        var footerEl = document.getElementById("bottomfooter");
        var footerHeight = footerEl ? footerEl.getBoundingClientRect().height : 44;
        var breathingRoom = 16;
        var available = window.innerHeight - top - footerHeight - breathingRoom;
        globeContainerEl.style.height = Math.max(420, available) + "px";
        // Re-measure the country table's height cap (see adjustCountryTableMaxHeight() below) any time the overall
        // layout is re-measured too (load, resize, fullscreen toggle, "About this page" collapse) - it depends on
        // the on-screen position of two other corner panels, which any of those events can shift.
        adjustCountryTableMaxHeight();
    }
    // Deliberately not called immediately at parse-time: this early in the page load, surrounding chrome/fonts/
    // images (and the fixed footer) haven't settled into their final layout yet, so an immediate measurement would
    // be wrong - briefly sizing the container too tall and letting its bottom peek out from under the fixed footer
    // until the corrected size kicked in on "load". Instead we simply keep the CSS default (70vh/min 420px, see
    // "#globeContainer" above) as the very first paint, which already looks reasonable, and only replace it once
    // with an accurate pixel height after everything has truly finished loading - so there is no visible resize
    // flash at all.
    //
    // Note this page's own inline script (this one) runs and registers below *before* the outer layout.jsp's
    // closing script does, because this content is nested inside "#contentDiv" - and that outer script is what
    // reveals "#contentDiv" (kept "display:none" behind a loading overlay until then) via a jQuery "load" handler.
    // Multiple listeners on the same event fire in registration order, so naively also listening for "load" here
    // would run *before* "#contentDiv" is actually shown, measuring a hidden (zero-size) container and computing a
    // bogus height. Instead, poll on animation frames until the container genuinely has real layout (non-zero
    // rect), which is agnostic to whichever mechanism/timing reveals it.
    function waitUntilVisibleThenResize(attemptsLeft) {
        if (globeContainerEl.getBoundingClientRect().height > 0) {
            resizeGlobeContainer();
            return;
        }
        if (attemptsLeft <= 0) {
            return;
        }
        requestAnimationFrame(function() { waitUntilVisibleThenResize(attemptsLeft - 1); });
    }
    window.addEventListener("load", function() { waitUntilVisibleThenResize(120); });
    window.addEventListener("resize", resizeGlobeContainer);
    document.addEventListener("fullscreenchange", resizeGlobeContainer);

    // The "Transfers by country" table (top-right, see "#globeCountryTable" above) and the KPI panel (bottom-right,
    // see "#globeStatsPanel") live in two independent absolutely-positioned corner stacks with no layout awareness
    // of each other, so a long country list (CSS "max-height:42%" of its own column) can visually run right down
    // into - or under - the KPI card below it once there are enough countries with active transfers. Rather than
    // guessing a fixed height that would either waste space (few countries) or still overlap (many countries, or a
    // taller KPI panel/warning banner pushing it down), measure both stacks' actual on-screen position and cap the
    // table so its own bottom always stops a small gap above wherever the KPI stack currently begins.
    function adjustCountryTableMaxHeight() {
        var table = document.getElementById("globeCountryTable");
        var bottomPanels = document.getElementById("globeBottomRightPanels");
        if (!table || !bottomPanels) {
            return;
        }
        var tableTop = table.getBoundingClientRect().top;
        var bottomPanelsTop = bottomPanels.getBoundingClientRect().top;
        var gap = 14;
        var available = bottomPanelsTop - tableTop - gap;
        // Ignore nonsensical measurements (e.g. the table not laid out/visible yet) and fall back to the CSS
        // default (42%) rather than collapsing the table to near-nothing.
        table.style.maxHeight = available > 80 ? available + "px" : "";
    }
    // Expanding/collapsing the "About this page" info card above shifts everything below it (including this
    // container's own top offset), so it needs the same re-measure as an actual window resize - both at the start
    // and the end of the Bootstrap collapse animation, since the container's "top" keeps changing throughout it.
    var globeInfoCollapseEl = document.getElementById("globeInfoCollapse");
    if (globeInfoCollapseEl) {
        ["show.bs.collapse", "hide.bs.collapse", "shown.bs.collapse", "hidden.bs.collapse"].forEach(function (evt) {
            globeInfoCollapseEl.addEventListener(evt, resizeGlobeContainer);
        });
    }

    // No Cesium Ion token: use the low-resolution offline "Natural Earth II" imagery bundled with Cesium so the
    // globe works fully self-hosted, with no external network dependency and no Ion account required.
    Cesium.Ion.defaultAccessToken = undefined;

    // TileMapServiceImageryProvider (like most other providers) must be created asynchronously via fromUrl(): the
    // constructor alone never fetches the tileset's tilemapresource.xml, leaving the globe with no imagery at all
    // (just the default flat-blue ellipsoid material).
    //
    // A higher-resolution imagery layer ("NaturalEarthHR") may be available if the Master/Monitor server has
    // finished provisioning it in the background (see GlobeImageryProvisioner) - it looks noticeably sharper once
    // zoomed into a country/region. It is never bundled/guaranteed present though (built lazily at runtime, and
    // only once internet access is available), so its presence is checked with a quick, short-timeout HEAD request
    // first (rather than handing it straight to fromUrl(), whose own error handling/retry behaviour can be slow to
    // fail and could delay the globe's very first render) and it is only used if that check succeeds; otherwise -
    // instantly, with no waiting - the always-present, low-resolution "Natural Earth II" imagery bundled with
    // Cesium is used instead.
    async function checkHrImageryAvailable() {
        const controller = new AbortController();
        const timeout = setTimeout(function () { controller.abort(); }, 2000);
        try {
            const url = Cesium.buildModuleUrl("Assets/Textures/NaturalEarthHR/tilemapresource.xml");
            const response = await fetch(url, { method: "HEAD", signal: controller.signal });
            return response.ok;
        } catch (e) {
            return false;
        } finally {
            clearTimeout(timeout);
        }
    }

    var baseImageryProvider;
    if (await checkHrImageryAvailable()) {
        try {
            baseImageryProvider = await Cesium.TileMapServiceImageryProvider.fromUrl(
                Cesium.buildModuleUrl("Assets/Textures/NaturalEarthHR")
            );
        } catch (e) {
            baseImageryProvider = undefined;
        }
    }
    if (!baseImageryProvider) {
        baseImageryProvider = await Cesium.TileMapServiceImageryProvider.fromUrl(
            Cesium.buildModuleUrl("Assets/Textures/NaturalEarthII")
        );
    }

    var viewer = new Cesium.Viewer("cesiumContainer", {
        baseLayer: new Cesium.ImageryLayer(baseImageryProvider),
        baseLayerPicker: false,
        geocoder: false,
        homeButton: false,
        sceneModePicker: false,
        navigationHelpButton: false,
        animation: false,
        timeline: false,
        infoBox: false,
        selectionIndicator: false,
        fullscreenButton: false
    });
    viewer.scene.skyAtmosphere.show = true;

    // Makes the globe itself look like night when the page's own dark theme is selected. Real per-pixel sun
    // lighting (Cesium shades the hemisphere facing away from the sun, based on the actual current date/time) is
    // what genuinely produces a convincing day/night terminator here, so this does not need a separate offline
    // "night lights" imagery layer (Cesium doesn't bundle one, and this page deliberately avoids any imagery that
    // would require internet access) - just that shader-level lighting switched on, plus a bit of extra dimming/
    // desaturation and a darker atmospheric glow so the unlit hemisphere reads clearly darker rather than merely
    // "shaded". The light theme keeps the previous fully-lit, flat look untouched.
    var globeImageryLayer = viewer.imageryLayers.get(0);
    function applyThemeToGlobeVisuals() {
        var isDark = document.documentElement.getAttribute("data-bs-theme") === "dark";
        viewer.scene.globe.enableLighting = isDark;
        globeImageryLayer.brightness = isDark ? 0.55 : 1.0;
        globeImageryLayer.contrast = isDark ? 1.1 : 1.0;
        globeImageryLayer.saturation = isDark ? 0.8 : 1.0;
        viewer.scene.skyAtmosphere.brightnessShift = isDark ? -0.4 : 0.0;
        viewer.scene.skyAtmosphere.hueShift = isDark ? -0.05 : 0.0;
    }
    applyThemeToGlobeVisuals();
    // Lets toggling the theme (via the header's sun/moon button) re-tint the globe live, with no page reload -
    // mirrors the same "[data-bs-theme] MutationObserver" pattern already used by the other chart pages.
    new MutationObserver(applyThemeToGlobeVisuals)
        .observe(document.documentElement, { attributes: true, attributeFilter: ["data-bs-theme"] });

    var points = viewer.scene.primitives.add(new Cesium.PointPrimitiveCollection());
    var arcs = viewer.scene.primitives.add(new Cesium.PolylineCollection());

    var STATUS_COLOR = {
        ACTIVE: Cesium.Color.fromCssColorString("#38bdf8"),
        DONE: Cesium.Color.fromCssColorString("#22c55e"),
        FAILED: Cesium.Color.fromCssColorString("#ef4444")
    };
    var ORIGIN_COLOR = Cesium.Color.fromCssColorString("#ffd166");
    var PROXY_HOST_COLOR = Cesium.Color.fromCssColorString("#a78bfa");
    var TERMINAL_FADE_MS = 2500;
    var ORIGIN_PIXEL_SIZE = 12;
    var MOVER_PIXEL_SIZE = 10;

    var origin = null; // {lat, lon}
    var originPoint = null;
    // hostName -> { arc, point, lat, lon, transfers: {transferId: sample}, status, removeTimeout, pulsePhase }
    var hosts = Object.create(null);
    // countryCode -> { arc, point, lat, lon, hostCount, transfers: {transferId: sample}, agg, removeTimeout, pulsePhase }
    // - only populated/rendered while viewMode === "country" (see setViewMode()).
    var countries = Object.create(null);
    // moverName -> { point, lat, lon, transferIds: Set, removeTimeout } - only for Proxy Hosts (see
    // isProxyHost on each transfer sample), never for ordinary, directly-connected Data Movers.
    var movers = Object.create(null);

    // Whether arcs/markers are grouped per target Host ("host", the default - one arc per Host) or per resolved
    // destination country ("country" - one aggregated arc per country, plus a small breakdown table), the latter
    // being a much less cluttered "broad view" once there are many hundreds/thousands of concurrent transfers to
    // many different Hosts. Persisted across reloads via localStorage, like the label toggles below.
    var VIEW_MODE_PREF_KEY = "globeViewMode";
    var viewMode = localStorage.getItem(VIEW_MODE_PREF_KEY) === "country" ? "country" : "host";

    // Which direction(s) of transfer to monitor: "both" (default), "dissemination" (data pushed out to a Host) or
    // "acquisition" (data pulled in from a Host). Filters the raw sample list (see applyDirectionFilter()) before
    // any arc/marker grouping or KPI counting happens, so every part of the page (arcs, country table, KPI cards)
    // consistently reflects only the selected direction(s). Persisted across reloads via localStorage, like
    // viewMode above.
    var DIRECTION_MODE_PREF_KEY = "globeDirectionMode";
    var directionMode = localStorage.getItem(DIRECTION_MODE_PREF_KEY) || "both";
    if (["both", "dissemination", "acquisition"].indexOf(directionMode) === -1) {
        directionMode = "both";
    }

    // Optional country/city name labels, provisioned lazily/in the background by GlobeLabelsProvisioner (see
    // globeImageryProvider fallback above for the same rationale) - fetched once, best-effort, and simply skipped
    // if not yet available (e.g. first run with no internet access yet at startup): the globe remains fully usable
    // without them, just less easy to orient on. Country and town names are toggled independently (see
    // globeCountryLabelsToggle/globeCityLabelsToggle below) and remembered across page reloads via localStorage.
    var COUNTRY_LABELS_PREF_KEY = "globeCountryLabelsVisible";
    var CITY_LABELS_PREF_KEY = "globeCityLabelsVisible";
    var countryLabelsVisible = localStorage.getItem(COUNTRY_LABELS_PREF_KEY) === "true"; // hidden by default
    var cityLabelsVisible = localStorage.getItem(CITY_LABELS_PREF_KEY) === "true"; // hidden by default
    var countryLabelCollection = null;
    var cityLabelCollection = null;

    function setCountryLabelsVisible(visible) {
        countryLabelsVisible = visible;
        localStorage.setItem(COUNTRY_LABELS_PREF_KEY, String(visible));
        if (countryLabelCollection) {
            countryLabelCollection.show = visible;
        }
        var toggle = document.getElementById("globeCountryLabelsToggle");
        if (toggle) {
            toggle.checked = visible;
        }
        updateLabelsBtnState();
    }

    function setCityLabelsVisible(visible) {
        cityLabelsVisible = visible;
        localStorage.setItem(CITY_LABELS_PREF_KEY, String(visible));
        if (cityLabelCollection) {
            cityLabelCollection.show = visible;
        }
        var toggle = document.getElementById("globeCityLabelsToggle");
        if (toggle) {
            toggle.checked = visible;
        }
        updateLabelsBtnState();
    }

    // The main tag button just reflects whether either kind of name is currently shown, so it still gives an
    // at-a-glance "something is on" indicator without needing its own separate on/off state.
    function updateLabelsBtnState() {
        var btn = document.getElementById("globeLabelsBtn");
        var anyVisible = countryLabelsVisible || cityLabelsVisible;
        btn.classList.toggle("active", anyVisible);
    }
    document.getElementById("globeCountryLabelsToggle").addEventListener("change", function () {
        setCountryLabelsVisible(this.checked);
    });
    document.getElementById("globeCityLabelsToggle").addEventListener("change", function () {
        setCityLabelsVisible(this.checked);
    });
    setCountryLabelsVisible(countryLabelsVisible);
    setCityLabelsVisible(cityLabelsVisible);

    // View-mode dropdown wiring (see setViewMode() further below for the actual behaviour). The button itself is
    // marked "active" whenever "Per country" is selected, mirroring updateLabelsBtnState()'s pattern above.
    document.getElementById("globeViewModeHost").addEventListener("change", function () {
        if (this.checked) {
            setViewMode("host");
            document.getElementById("globeViewModeBtn").classList.remove("active");
        }
    });
    document.getElementById("globeViewModeCountry").addEventListener("change", function () {
        if (this.checked) {
            setViewMode("country");
            document.getElementById("globeViewModeBtn").classList.add("active");
        }
    });
    document.getElementById(viewMode === "country" ? "globeViewModeCountry" : "globeViewModeHost").checked = true;
    document.getElementById("globeViewModeBtn").classList.toggle("active", viewMode === "country");

    // Direction-mode dropdown wiring (see setDirectionMode() further below). The button is marked "active"
    // whenever a single direction (rather than "Both") is selected.
    document.getElementById("globeDirectionBoth").addEventListener("change", function () {
        if (this.checked) {
            setDirectionMode("both");
        }
    });
    document.getElementById("globeDirectionDiss").addEventListener("change", function () {
        if (this.checked) {
            setDirectionMode("dissemination");
        }
    });
    document.getElementById("globeDirectionAcq").addEventListener("change", function () {
        if (this.checked) {
            setDirectionMode("acquisition");
        }
    });
    document.getElementById({ both: "globeDirectionBoth", dissemination: "globeDirectionDiss",
        acquisition: "globeDirectionAcq" }[directionMode]).checked = true;
    document.getElementById("globeDirectionBtn").classList.toggle("active", directionMode !== "both");
    document.getElementById("globeCountryTable").style.display = viewMode === "country" ? "block" : "none";

    // Renders a name as a small rounded "pill" (plain text on a translucent rounded-rect background, optionally
    // with a border) into an offscreen canvas, used as a Billboard image - Cesium's built-in Label background is a
    // plain rectangle with no corner radius, so a custom canvas is needed to get an actual pill shape. Drawn at 2x
    // scale (supersampled) for crisp text, then displayed at normal size via the billboard's scale property.
    function createPillCanvas(text, font, textColor, bgColor, borderColor) {
        var supersample = 2;
        var paddingX = 7;
        var paddingY = 3;
        var borderWidth = borderColor ? 1.2 : 0;
        var measure = document.createElement("canvas").getContext("2d");
        measure.font = font;
        var textWidth = measure.measureText(text).width;
        var fontSizePx = parseInt(font, 10) || 12;
        var textHeight = fontSizePx * 1.25;
        var width = textWidth + paddingX * 2;
        var height = textHeight + paddingY * 2;
        var radius = height / 2;
        var canvas = document.createElement("canvas");
        canvas.width = Math.ceil(width * supersample);
        canvas.height = Math.ceil(height * supersample);
        var ctx = canvas.getContext("2d");
        ctx.scale(supersample, supersample);
        var inset = borderWidth / 2;
        ctx.beginPath();
        ctx.moveTo(radius, inset);
        ctx.lineTo(width - radius, inset);
        ctx.arcTo(width - inset, inset, width - inset, radius, radius);
        ctx.lineTo(width - inset, height - radius);
        ctx.arcTo(width - inset, height - inset, width - radius, height - inset, radius);
        ctx.lineTo(radius, height - inset);
        ctx.arcTo(inset, height - inset, inset, height - radius, radius);
        ctx.lineTo(inset, radius);
        ctx.arcTo(inset, inset, radius, inset, radius);
        ctx.closePath();
        ctx.fillStyle = bgColor;
        ctx.fill();
        if (borderColor) {
            ctx.strokeStyle = borderColor;
            ctx.lineWidth = borderWidth;
            ctx.stroke();
        }
        ctx.fillStyle = textColor;
        ctx.font = font;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(text, width / 2, height / 2 + 1);
        return { canvas: canvas, scale: 1 / supersample };
    }

    (async function loadLabels() {
        try {
            var response = await fetch(Cesium.buildModuleUrl("Assets/Data/GlobeLabels/labels.json"));
            if (!response.ok) {
                return;
            }
            var data = await response.json();
            countryLabelCollection = viewer.scene.primitives.add(new Cesium.BillboardCollection());
            countryLabelCollection.show = countryLabelsVisible;
            cityLabelCollection = viewer.scene.primitives.add(new Cesium.BillboardCollection());
            cityLabelCollection.show = cityLabelsVisible;
            // Countries get a bolder, bordered, uppercase pill; cities a smaller, plain one - so the two kinds of
            // labels remain easy to tell apart at a glance instead of just differing in font size.
            var COUNTRY_FONT = "bold 10px Arial, Helvetica, sans-serif";
            var CITY_FONT = "12px Arial, Helvetica, sans-serif";
            var COUNTRY_BACKGROUND = "rgba(37,29,10,0.78)";
            var COUNTRY_BORDER = "rgba(255,209,102,0.85)";
            var CITY_BACKGROUND = "rgba(20,25,30,0.72)";
            (data.countries || []).forEach(function (c) {
                var pill = createPillCanvas(c.name.toUpperCase(), COUNTRY_FONT, "#ffd166", COUNTRY_BACKGROUND, COUNTRY_BORDER);
                countryLabelCollection.add({
                    position: Cesium.Cartesian3.fromDegrees(c.lon, c.lat),
                    image: pill.canvas,
                    scale: pill.scale,
                    verticalOrigin: Cesium.VerticalOrigin.CENTER,
                    horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
                    // Countries only join in once zoomed in a bit past the default whole-globe view (~1.2e7m), so
                    // the initial view is uncluttered; city names then join in even later, once zoomed in
                    // further still (see below).
                    distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, 8e6)
                });
            });
            // Cities only start appearing once the view is noticeably closer than where country names join in
            // above, so the reveal order is: default view (nothing) -> countries -> largest cities -> progressively
            // smaller/less populated ones as it zooms in further - the same technique used by most web map providers.
            var places = (data.places || []).slice().sort(function (a, b) { return (b.pop || 0) - (a.pop || 0); });
            places.forEach(function (p, index) {
                var farDistance = index < 30 ? 5e6 : index < 100 ? 2.5e6 : 1e6;
                var pill = createPillCanvas(p.name, CITY_FONT, "#e2e8f0", CITY_BACKGROUND);
                cityLabelCollection.add({
                    position: Cesium.Cartesian3.fromDegrees(p.lon, p.lat),
                    image: pill.canvas,
                    scale: pill.scale,
                    verticalOrigin: Cesium.VerticalOrigin.TOP,
                    horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
                    pixelOffset: new Cesium.Cartesian2(0, 4),
                    distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, farDistance)
                });
            });
        } catch (e) {
            // Not ready yet (or transient network error) - the globe remains fully usable without labels.
        }
    })();

    // Total bytes transferred over the last rolling 24h, maintained server-side by the MasterServer (see
    // LiveTransferRegistry#getBytesLast24h()) and pushed with every "snapshot" message, so every open globe page -
    // and every reconnect - shows the exact same, always-on figures rather than a per-connection counter. Kept as
    // three separate totals (combined/Dissemination/Acquisition) so the "Transferred (24h)" KPI can match whichever
    // direction is currently selected (see directionMode/updateKpis()).
    var bytesLast24h = 0;
    var bytesLast24hDissemination = 0;
    var bytesLast24hAcquisition = 0;

    // Data Portal (IncomingUser FTP/HTTP/SFTP/S3/WebDAV) activity, maintained server-side by
    // DataPortalActivityRegistry and pushed with every "snapshot" message: current open session count, plus a live
    // 5-second rolling bytes/sec rate in each direction (not a cumulative total, unlike bytesLast24h above).
    var dataPortalSessions = 0;
    var dataPortalBytesInPerSecond = 0;
    var dataPortalBytesOutPerSecond = 0;

    // Aggregate used/total bytes across every volume of every DataMover, maintained server-side by
    // ManagementInterface#getMoverVolumeUsage() (a cheap, periodically-refreshed cache, no live DataMover RMI calls),
    // pushed with every "snapshot" message and used to drive the "Mover storage used" progress bar.
    var moverStorageUsedBytes = 0;
    var moverStorageTotalBytes = 0;

    // The raw sample list from the most recent "snapshot" message (every direction), kept so switching
    // directionMode (see setDirectionMode()) can redraw instantly without waiting for the next poll.
    var rawSamples = [];

    // The direction-filtered sample list (see applyDirectionFilter()), used everywhere else (KPI cards, arc/marker
    // grouping, country table) so the whole page consistently reflects only the currently selected direction(s),
    // independent of whether the MasterServer's own origin location or any target Host's geolocation has been
    // resolved yet (geolocation is only needed to actually place a marker/arc on the globe, not to count activity).
    var lastSamples = [];

    // Auto-scaling gauge ceiling for the throughput speed-meter: grows immediately to cover new peaks, then
    // decays slowly back down so the gauge stays meaningful/readable as activity drops rather than staying
    // pinned at a peak seen minutes ago.
    var gaugeMax = 1e6; // starts at 1 Mbps
    var gaugeArcLength = null;

    // Per-gauge state (ceiling + cached arc length) for the two new Data Portal in/out speed-meters, keyed by SVG
    // element id, so the same generic updateGauge() logic below can drive all three gauges without duplication.
    var gaugeState = Object.create(null);

    function niceCeil(value) {
        if (value <= 0) {
            return 1;
        }
        var exp = Math.floor(Math.log10(value));
        var base = Math.pow(10, exp);
        var mult = value / base;
        var niceMult = mult <= 1 ? 1 : mult <= 2 ? 2 : mult <= 5 ? 5 : 10;
        return niceMult * base;
    }

    function updateGauge(rateBitsPerSecond) {
        var arc = document.getElementById("gaugeArc");
        if (gaugeArcLength === null) {
            gaugeArcLength = arc.getTotalLength();
            arc.style.strokeDasharray = gaugeArcLength;
            arc.style.strokeDashoffset = gaugeArcLength;
        }
        if (rateBitsPerSecond > gaugeMax) {
            gaugeMax = niceCeil(rateBitsPerSecond);
        } else if (rateBitsPerSecond < gaugeMax * 0.3) {
            // Slowly relax the ceiling back down when throughput has dropped well below it.
            gaugeMax = Math.max(niceCeil(rateBitsPerSecond * 1.5), 1e6);
        }
        var pct = Cesium.Math.clamp(rateBitsPerSecond / gaugeMax, 0, 1);
        arc.style.strokeDashoffset = gaugeArcLength * (1 - pct);
        arc.style.stroke = pct < 0.6 ? "#38bdf8" : pct < 0.85 ? "#ffd166" : "#ef4444";
    }

    // Generic version of updateGauge() above, used for any additional gauge identified by its SVG element id (the
    // original updateGauge()/gaugeMax/gaugeArcLength are left untouched to avoid disturbing the existing
    // "Combined throughput" gauge's behaviour/state).
    function updateGaugeById(arcId, rateBitsPerSecond) {
        var state = gaugeState[arcId];
        if (!state) {
            state = { max: 1e6, arcLength: null };
            gaugeState[arcId] = state;
        }
        var arc = document.getElementById(arcId);
        if (!arc) {
            return;
        }
        if (state.arcLength === null) {
            state.arcLength = arc.getTotalLength();
            arc.style.strokeDasharray = state.arcLength;
            arc.style.strokeDashoffset = state.arcLength;
        }
        if (rateBitsPerSecond > state.max) {
            state.max = niceCeil(rateBitsPerSecond);
        } else if (rateBitsPerSecond < state.max * 0.3) {
            state.max = Math.max(niceCeil(rateBitsPerSecond * 1.5), 1e6);
        }
        var pct = Cesium.Math.clamp(rateBitsPerSecond / state.max, 0, 1);
        arc.style.strokeDashoffset = state.arcLength * (1 - pct);
        arc.style.stroke = pct < 0.6 ? "#38bdf8" : pct < 0.85 ? "#ffd166" : "#ef4444";
    }

    // Percentage-based sibling of updateGaugeById() above, for gauges that already have a fixed, known 0-100 range
    // (e.g. storage used) rather than an open-ended rate needing the auto-scaling "niceCeil" ceiling logic - reuses
    // the same arc element/visual language (and the same 75%/90% amber/red thresholds already used elsewhere on
    // this page for absolute capacity, as opposed to the 60%/85%-of-ceiling thresholds used for rate gauges) so it
    // reads as just another member of the same gauge family rather than a one-off widget.
    var percentGaugeArcLength = Object.create(null);
    function updatePercentGaugeById(arcId, pct) {
        var arc = document.getElementById(arcId);
        if (!arc) {
            return;
        }
        if (percentGaugeArcLength[arcId] === undefined) {
            percentGaugeArcLength[arcId] = arc.getTotalLength();
            arc.style.strokeDasharray = percentGaugeArcLength[arcId];
        }
        var clamped = Cesium.Math.clamp(pct / 100, 0, 1);
        arc.style.strokeDashoffset = percentGaugeArcLength[arcId] * (1 - clamped);
        arc.style.stroke = pct < 75 ? "#38bdf8" : pct < 90 ? "#ffd166" : "#ef4444";
    }

    // Drives the "Data Portal Activity" KPI row (open sessions, data in/out gauges, storage gauge), all fields of
    // which come straight from the server-pushed "snapshot" message rather than from lastSamples, so this is called
    // directly from the WebSocket onmessage handler rather than from updateKpis().
    function updateDataPortalKpis() {
        document.getElementById("kpiSessions").textContent = dataPortalSessions.toLocaleString();
        var bpsIn = dataPortalBytesInPerSecond * 8;
        var bpsOut = dataPortalBytesOutPerSecond * 8;
        document.getElementById("kpiDataIn").textContent = formatRate(bpsIn);
        document.getElementById("kpiDataOut").textContent = formatRate(bpsOut);
        updateGaugeById("gaugeArcIn", bpsIn);
        updateGaugeById("gaugeArcOut", bpsOut);

        var pct = moverStorageTotalBytes > 0
            ? Cesium.Math.clamp((moverStorageUsedBytes / moverStorageTotalBytes) * 100, 0, 100)
            : 0;
        var pctRounded = Math.round(pct * 10) / 10;
        document.getElementById("kpiStoragePct").textContent = pctRounded + "%";
        updatePercentGaugeById("gaugeArcStorage", pct);
        document.getElementById("storageTile").title = "Storage used: " + formatBytes(moverStorageUsedBytes) +
            " / " + formatBytes(moverStorageTotalBytes);
    }

    // Picks the rolling 24h bytes total matching the current directionMode.
    function currentBytesLast24h() {
        if (directionMode === "dissemination") {
            return bytesLast24hDissemination;
        }
        if (directionMode === "acquisition") {
            return bytesLast24hAcquisition;
        }
        return bytesLast24h;
    }

    // Counts straight from the last direction-filtered sample list (see lastSamples above), not from the `hosts`
    // map used for marker placement, so these figures stay accurate even while the origin and/or target Host
    // locations are not (yet) resolved.
    function updateKpis() {
        var activeHostNames = Object.create(null);
        var transferCount = 0;
        var totalRate = 0;
        lastSamples.forEach(function (s) {
            if (s.status !== "ACTIVE") {
                return;
            }
            transferCount++;
            totalRate += (s.rateBitsPerSecond || 0);
            if (s.host) {
                activeHostNames[s.host] = true;
            }
        });
        document.getElementById("kpiTransfers").textContent = transferCount;
        document.getElementById("kpiHosts").textContent = Object.keys(activeHostNames).length;
        document.getElementById("kpiThroughput").textContent = formatRate(totalRate);
        document.getElementById("kpiBytes").textContent = formatBytes(currentBytesLast24h());
        updateGauge(totalRate);
        document.getElementById("globeSubtitle").textContent = "Live data - updates automatically";
    }


    function setStatus(state, label) {
        var badge = document.getElementById("globeStatusBadge");
        badge.className = "globe-badge-" + state;
        badge.textContent = label;
        if (state !== "connected") {
            document.getElementById("globeSubtitle").textContent =
                state === "connecting" ? "Connecting..." : "Connection lost - retrying...";
        }
    }

    function rateWidth(bps) {
        if (!bps || bps <= 0) {
            return 1.5;
        }
        // Logarithmic scale: ~1 Mbps -> thin, ~1 Gbps -> thick.
        var mbps = bps / 1e6;
        return Cesium.Math.clamp(1.5 + Math.log10(1 + mbps) * 2.2, 1.5, 10);
    }

    // Builds the material used for every arc: a "PolylineArrow" fabric, so the arrowhead at the polyline's end
    // gives every arc a visible sense of direction (see directionalArcPositions()) - Dissemination arcs point
    // towards the destination Host (data pushed out), Acquisition arcs point back towards the origin/Proxy Host
    // (data pulled in), simply depending on which end of the (reversible) positions array each was built to end at.
    function arcMaterial(color) {
        return Cesium.Material.fromType("PolylineArrow", { color: color.withAlpha(0.85) });
    }

    // Builds a set of positions following the geodesic (great-circle) path between two points, lofted into a
    // parabolic arc above the surface, so that transfers spanning long distances (e.g. Europe <-> US) are rendered
    // as a curved 3D arc rather than a straight line cutting through the globe.
    // trimStartMeters/trimEndMeters (both optional, in world-space meters) shorten the visible arc at either end,
    // so it can be made to start/end at the edge of a marker circle instead of poking out from underneath its
    // center - see pixelRadiusToMeters()/retrimArc() below, which compute those distances from each marker's
    // on-screen pixel size.
    function arcPositions(lon1, lat1, lon2, lat2, trimStartMeters, trimEndMeters) {
        var start = Cesium.Cartographic.fromDegrees(lon1, lat1);
        var end = Cesium.Cartographic.fromDegrees(lon2, lat2);
        var geodesic = new Cesium.EllipsoidGeodesic(start, end);
        var totalDistance = geodesic.surfaceDistance;
        if (!totalDistance || !isFinite(totalDistance)) {
            return [Cesium.Cartesian3.fromDegrees(lon1, lat1), Cesium.Cartesian3.fromDegrees(lon2, lat2)];
        }
        // Taller arcs for longer distances, capped so short hops don't look flat and very long ones don't look
        // excessive.
        var maxHeight = Cesium.Math.clamp(totalDistance * 0.12, 15000, 900000);
        var segments = Cesium.Math.clamp(Math.round(totalDistance / 100000), 16, 128);
        // Each trim is capped at 40% of the total distance so a very short arc (or an unusually large marker)
        // never fully collapses; if the two trims would overlap, fall back to no trimming at all rather than
        // drawing a degenerate/reversed arc.
        var startFraction = Cesium.Math.clamp((trimStartMeters || 0) / totalDistance, 0, 0.4);
        var endFraction = 1 - Cesium.Math.clamp((trimEndMeters || 0) / totalDistance, 0, 0.4);
        if (endFraction <= startFraction) {
            startFraction = 0;
            endFraction = 1;
        }
        var positions = [];
        for (var i = 0; i <= segments; i++) {
            var fraction = startFraction + (endFraction - startFraction) * (i / segments);
            var carto = geodesic.interpolateUsingFraction(fraction);
            var height = Math.sin(Math.PI * fraction) * maxHeight;
            positions.push(Cesium.Cartesian3.fromRadians(carto.longitude, carto.latitude, height));
        }
        return positions;
    }

    // Converts a marker's on-screen pixel diameter to an approximate world-space distance (meters) at its current
    // position/zoom level, so arcs can be trimmed to visually start/end at the edge of a marker circle rather than
    // its center (which otherwise looks like the arc is erupting from the middle of a differently-coloured dot).
    function pixelRadiusToMeters(lat, lon, pixelDiameter) {
        var cart = Cesium.Cartesian3.fromDegrees(lon, lat);
        var metersPerPixel = viewer.camera.getPixelSize(
            new Cesium.BoundingSphere(cart, 0), viewer.scene.drawingBufferWidth, viewer.scene.drawingBufferHeight);
        if (!metersPerPixel || !isFinite(metersPerPixel)) {
            return 0;
        }
        return (pixelDiameter / 2) * metersPerPixel;
    }

    // Builds the (trimmed) arc positions between a marker's origin (the MasterServer/Proxy Host location, stored
    // as {lat, lon, pixelSize}) and its destination Host (lat/lon, destPixelSize), oriented so the arrowhead (see
    // "PolylineArrow" material in upsertHost()/upsertCountry()) always points in the actual direction data is
    // flowing: origin -> Host for Dissemination (data pushed out), Host -> origin for Acquisition (data pulled
    // in), simply by swapping which endpoint the positions array starts/ends at.
    function directionalArcPositions(arcOrigin, destLat, destLon, destPixelSize, isAcquisition) {
        var trimOrigin = pixelRadiusToMeters(arcOrigin.lat, arcOrigin.lon, arcOrigin.pixelSize);
        var trimDest = pixelRadiusToMeters(destLat, destLon, destPixelSize);
        return isAcquisition
            ? arcPositions(destLon, destLat, arcOrigin.lon, arcOrigin.lat, trimDest, trimOrigin)
            : arcPositions(arcOrigin.lon, arcOrigin.lat, destLon, destLat, trimOrigin, trimDest);
    }

    // Recomputes one arc's trimmed positions from its stored raw endpoints/marker sizes - used both right after
    // creation and whenever the camera moves/zooms enough to change the pixel-to-meters ratio (see
    // refreshArcTrims()), so the visual gap at each end keeps tracking the marker's on-screen size correctly.
    function retrimArc(entry) {
        if (!entry.arc || !entry.arcOrigin) {
            return;
        }
        entry.arc.positions = directionalArcPositions(entry.arcOrigin, entry.lat, entry.lon, entry.destPixelSize,
            entry.isAcquisition);
    }

    // Re-trims every currently-rendered arc (host or country view) - bound to the camera's `changed` event below,
    // rather than every single postRender frame, since the pixel-to-meters ratio only meaningfully changes while
    // the user is actively panning/zooming.
    function refreshArcTrims() {
        Object.keys(hosts).forEach(function (name) { retrimArc(hosts[name]); });
        Object.keys(countries).forEach(function (code) { retrimArc(countries[code]); });
    }
    viewer.camera.percentageChanged = 0.05;
    viewer.camera.changed.addEventListener(refreshArcTrims);

    // Resolves an ISO country code (as returned by GeoIP, e.g. "US", "IT") to its display name via the browser's
    // built-in Intl.DisplayNames, so the country table/labels don't need a bundled name lookup table of their own.
    // Falls back to the raw code itself if Intl.DisplayNames is unavailable (very old browsers) or the code is
    // unrecognised.
    var countryNameFormatter = (typeof Intl !== "undefined" && Intl.DisplayNames)
        ? new Intl.DisplayNames(["en"], { type: "region" }) : null;
    function countryDisplayName(code) {
        if (!code) {
            return "Unknown";
        }
        if (countryNameFormatter) {
            try {
                return countryNameFormatter.of(code) || code;
            } catch (e) {
                return code;
            }
        }
        return code;
    }

    // Converts an ISO alpha-2 country code (e.g. "US") to its flag emoji by mapping each letter to the matching
    // Unicode "Regional Indicator Symbol" (U+1F1E6 = 'A'); most platforms/browsers render the resulting pair as a
    // single flag glyph. Falls back to the raw code (as text) for non-alpha2 values (e.g. "Unknown").
    function countryFlagEmoji(code) {
        if (!code || code.length !== 2 || !/^[A-Za-z]{2}$/.test(code)) {
            return null;
        }
        var upper = code.toUpperCase();
        var base = 0x1F1E6 - 65; // 'A'.charCodeAt(0) === 65
        return String.fromCodePoint(upper.charCodeAt(0) + base, upper.charCodeAt(1) + base);
    }

    function setOrigin(lat, lon) {
        var firstTime = !originPoint;
        origin = { lat: lat, lon: lon };
        if (originPoint) {
            points.remove(originPoint);
        }
        originPoint = points.add({
            position: Cesium.Cartesian3.fromDegrees(lon, lat),
            pixelSize: ORIGIN_PIXEL_SIZE,
            color: ORIGIN_COLOR,
            outlineColor: Cesium.Color.WHITE,
            outlineWidth: 2
        });
        if (firstTime) {
            viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(lon, lat, 12000000)
            });
        }
    }

    // Shows/hides the "location not configured" banner, and applies the origin marker once resolved. Called from
    // both the initial "hello" message and every recurring "snapshot" (so a client whose page is already open picks
    // up the origin as soon as it becomes resolvable server-side, without needing to reconnect). The banner cannot
    // be dismissed by the user - it is meant to stay visible for as long as the location genuinely isn't resolved.
    function applyOrigin(msg) {
        if (msg.originResolved && msg.originLat !== undefined && msg.originLon !== undefined) {
            if (!origin || origin.lat !== msg.originLat || origin.lon !== msg.originLon) {
                setOrigin(msg.originLat, msg.originLon);
            }
            document.getElementById("globeOriginWarning").style.display = "none";
        } else if (msg.originResolved === false) {
            document.getElementById("globeOriginWarningDetail").textContent = msg.originHost || "";
            document.getElementById("globeOriginWarning").style.display = "block";
        }
        // The origin warning banner sits above the KPI panel in the same bottom-right stack, so showing/hiding it
        // shifts that stack's top edge - re-measure the country table cap (see adjustCountryTableMaxHeight()) so it
        // stays accurate even if a poll message toggles this banner while "Per country" is already selected.
        adjustCountryTableMaxHeight();
    }

    function removeHost(name) {
        var h = hosts[name];
        if (!h) {
            return;
        }
        if (h.removeTimeout) {
            clearTimeout(h.removeTimeout);
        }
        if (h.arc) {
            arcs.remove(h.arc);
        }
        if (h.point) {
            points.remove(h.point);
        }
        delete hosts[name];
        updateKpis();
    }

    function removeMover(name) {
        var m = movers[name];
        if (!m) {
            return;
        }
        if (m.removeTimeout) {
            clearTimeout(m.removeTimeout);
        }
        if (m.point) {
            points.remove(m.point);
        }
        delete movers[name];
    }

    // Adds/updates the marker for one Proxy Host. Unlike Hosts, no arc is drawn from the marker itself (the arc to
    // the target Host is drawn by upsertHost, starting from this same location via aggregateHost's arcOrigin) - this
    // is purely the "here is where this Proxy Host physically is" marker.
    function upsertMover(name, lat, lon, hasActive) {
        var existing = movers[name];
        if (existing && existing.removeTimeout) {
            clearTimeout(existing.removeTimeout);
        }
        if (!existing) {
            var point = points.add({
                position: Cesium.Cartesian3.fromDegrees(lon, lat),
                pixelSize: MOVER_PIXEL_SIZE,
                color: PROXY_HOST_COLOR,
                outlineColor: Cesium.Color.WHITE,
                outlineWidth: 2
            });
            point.moverName = name;
            movers[name] = { point: point, lat: lat, lon: lon, removeTimeout: null };
        }
        if (!hasActive) {
            // No more active transfers currently going through this Proxy Host: fade the marker out shortly
            // instead of leaving it on the globe forever.
            movers[name].removeTimeout = setTimeout(function () { removeMover(name); }, TERMINAL_FADE_MS);
        }
    }

    // Aggregates every transfer currently reported for one Host (or, in country view, every transfer to every
    // Host within one country) into the counters the marker/arc/panel need.
    function aggregateTransfers(transferMap) {
        var agg = { activeCount: 0, totalRate: 0, totalBytes: 0, protocols: {}, hasActive: false, hasFailed: false, maxDuration: 0, arcOrigin: null, isAcquisition: false };
        Object.keys(transferMap).forEach(function (id) {
            var s = transferMap[id];
            if (s.status === "ACTIVE") {
                agg.activeCount++;
                agg.hasActive = true;
                agg.totalRate += (s.rateBitsPerSecond || 0);
            } else if (s.status === "FAILED") {
                agg.hasFailed = true;
            }
            agg.totalBytes += (s.bytesSent || 0);
            agg.maxDuration = Math.max(agg.maxDuration, s.duration || 0);
            if (s.protocol) {
                agg.protocols[s.protocol] = true;
            }
            if (s.direction === "ACQUISITION") {
                agg.isAcquisition = true;
            }
            // If this transfer was relayed through a Proxy Host, draw the arc from that Proxy Host's own
            // location instead of the MasterServer's, so the globe reflects where the data actually left from.
            if (!agg.arcOrigin && s.isProxyHost && s.moverLat !== undefined && s.moverLon !== undefined) {
                agg.arcOrigin = { lat: s.moverLat, lon: s.moverLon };
            }
        });
        agg.protocols = Object.keys(agg.protocols);
        return agg;
    }

    function hostColor(agg) {
        if (agg.hasFailed) return STATUS_COLOR.FAILED;
        if (agg.hasActive) return STATUS_COLOR.ACTIVE;
        return STATUS_COLOR.DONE;
    }

    function upsertHost(name, lat, lon, label, transferMap) {
        if (!origin) {
            return;
        }
        var agg = aggregateTransfers(transferMap);
        var color = hostColor(agg);
        var existing = hosts[name];
        if (existing && existing.removeTimeout) {
            clearTimeout(existing.removeTimeout);
        }
        if (existing) {
            if (existing.arc) arcs.remove(existing.arc);
            if (existing.point) points.remove(existing.point);
        }
        var arcOrigin = agg.arcOrigin || origin;
        var originPixelSize = agg.arcOrigin ? MOVER_PIXEL_SIZE : ORIGIN_PIXEL_SIZE;
        var destPixelSize = Cesium.Math.clamp(8 + agg.activeCount * 1.5, 8, 20);
        var positions = directionalArcPositions({ lat: arcOrigin.lat, lon: arcOrigin.lon, pixelSize: originPixelSize },
            lat, lon, destPixelSize, agg.isAcquisition);
        var arc = arcs.add({
            positions: positions,
            width: rateWidth(agg.totalRate),
            material: arcMaterial(color)
        });
        var point = points.add({
            position: Cesium.Cartesian3.fromDegrees(lon, lat),
            pixelSize: destPixelSize,
            color: color,
            outlineColor: Cesium.Color.WHITE,
            outlineWidth: 1
        });
        point.hostName = name;
        var entry = {
            arc: arc, point: point, lat: lat, lon: lon, label: label || name, transfers: transferMap, agg: agg,
            arcOrigin: { lat: arcOrigin.lat, lon: arcOrigin.lon, pixelSize: originPixelSize },
            destPixelSize: destPixelSize, isAcquisition: agg.isAcquisition,
            removeTimeout: null, pulsePhase: existing ? existing.pulsePhase : Math.random() * Math.PI * 2
        };
        hosts[name] = entry;
        if (!agg.hasActive) {
            // Every transfer to this Host just completed/failed: fade the marker out shortly instead of
            // leaving it on the globe forever.
            entry.removeTimeout = setTimeout(function () { removeHost(name); }, TERMINAL_FADE_MS);
        }
        updateKpis();
    }

    function removeCountry(code) {
        var c = countries[code];
        if (!c) {
            return;
        }
        if (c.removeTimeout) {
            clearTimeout(c.removeTimeout);
        }
        if (c.arc) {
            arcs.remove(c.arc);
        }
        if (c.point) {
            points.remove(c.point);
        }
        delete countries[code];
    }

    // Removes every currently-rendered per-Host arc/marker without touching the `hosts` bookkeeping map itself
    // (used when switching away from "Per host" view, and defensively on every snapshot applied in "Per country"
    // view, so both sets of primitives are never shown on the globe at the same time).
    function clearAllHostEntities() {
        Object.keys(hosts).forEach(function (name) { removeHost(name); });
    }

    // Same as clearAllHostEntities(), but for the per-country arcs/markers.
    function clearAllCountryEntities() {
        Object.keys(countries).forEach(function (code) { removeCountry(code); });
    }

    // Adds/updates the single aggregated arc/marker for one destination country - one arc summarising every
    // transfer currently going to any Host resolved to that country, so the globe stays readable even with
    // hundreds/thousands of individual Hosts (see viewMode).
    function upsertCountry(code, lat, lon, hostCount, transferMap) {
        if (!origin) {
            return;
        }
        var agg = aggregateTransfers(transferMap);
        var color = hostColor(agg);
        var existing = countries[code];
        if (existing && existing.removeTimeout) {
            clearTimeout(existing.removeTimeout);
        }
        if (existing) {
            if (existing.arc) arcs.remove(existing.arc);
            if (existing.point) points.remove(existing.point);
        }
        var arcOrigin = agg.arcOrigin || origin;
        var originPixelSize = agg.arcOrigin ? MOVER_PIXEL_SIZE : ORIGIN_PIXEL_SIZE;
        var destPixelSize = Cesium.Math.clamp(10 + agg.activeCount * 1.2, 10, 26);
        var positions = directionalArcPositions({ lat: arcOrigin.lat, lon: arcOrigin.lon, pixelSize: originPixelSize },
            lat, lon, destPixelSize, agg.isAcquisition);
        var arc = arcs.add({
            positions: positions,
            width: rateWidth(agg.totalRate),
            material: arcMaterial(color)
        });
        var point = points.add({
            position: Cesium.Cartesian3.fromDegrees(lon, lat),
            pixelSize: destPixelSize,
            color: color,
            outlineColor: Cesium.Color.WHITE,
            outlineWidth: 1
        });
        point.countryCode = code;
        var entry = {
            arc: arc, point: point, lat: lat, lon: lon, hostCount: hostCount, transfers: transferMap, agg: agg,
            arcOrigin: { lat: arcOrigin.lat, lon: arcOrigin.lon, pixelSize: originPixelSize },
            destPixelSize: destPixelSize, isAcquisition: agg.isAcquisition,
            removeTimeout: null, pulsePhase: existing ? existing.pulsePhase : Math.random() * Math.PI * 2
        };
        countries[code] = entry;
        if (!agg.hasActive) {
            // Every transfer to this country just completed/failed: fade the marker out shortly instead of
            // leaving it on the globe forever.
            entry.removeTimeout = setTimeout(function () { removeCountry(code); }, TERMINAL_FADE_MS);
        }
        updateKpis();
    }

    // Subtle pulsing on arcs/markers with at least one active transfer, so activity reads as "alive" rather
    // than static lines even when nothing else changes between two snapshots.
    viewer.scene.postRender.addEventListener(function () {
        var t = Date.now() / 1000;
        Object.keys(hosts).forEach(function (name) {
            var h = hosts[name];
            if (h.agg.hasActive && h.arc && h.arc.material && h.arc.material.uniforms) {
                h.arc.material.uniforms.color.alpha = 0.55 + 0.35 * Math.sin(t * 2.2 + h.pulsePhase);
            }
        });
        Object.keys(countries).forEach(function (code) {
            var c = countries[code];
            if (c.agg.hasActive && c.arc && c.arc.material && c.arc.material.uniforms) {
                c.arc.material.uniforms.color.alpha = 0.55 + 0.35 * Math.sin(t * 2.2 + c.pulsePhase);
            }
        });
    });

    // Groups the flat sample list from a "snapshot" message by target Host (skipping ones whose
    // geolocation could not be resolved) for marker/arc placement, then reconciles the current marker/arc set
    // against it. The KPI cards are updated separately (see updateKpis()), straight from the raw, ungrouped
    // sample list, so they stay accurate even for samples that get skipped here.
    //
    // Also always groups the same samples by resolved destination country (see byCountry below), regardless of the
    // current viewMode, so that: (a) the country breakdown table is always current, and (b) switching viewMode
    // in-between two polls (see setViewMode()) can redraw instantly from the last snapshot without waiting for the
    // next one. Only the grouping matching the current viewMode is actually turned into Cesium arcs/markers - the
    // other one's Cesium primitives (if any are still left over from before a mode switch) are defensively cleared
    // on every call.
    function applySnapshot(samples) {
        rawSamples = samples;
        lastSamples = directionMode === "both" ? samples : samples.filter(function (s) {
            return (s.direction || "DISSEMINATION").toLowerCase() === directionMode;
        });
        var byHost = Object.create(null);
        var byMover = Object.create(null);
        var byCountry = Object.create(null);
        var unresolvedActiveCount = 0;
        lastSamples.forEach(function (sample) {
            if (sample.hostLat === undefined || sample.hostLon === undefined || !sample.host) {
                if (sample.status === "ACTIVE") {
                    unresolvedActiveCount++;
                }
                return; // can't place a marker without a resolved location.
            }
            var group = byHost[sample.host];
            if (!group) {
                group = byHost[sample.host] = { lat: sample.hostLat, lon: sample.hostLon, label: sample.hostLabel, transfers: Object.create(null) };
            }
            group.transfers[sample.transferId] = sample;
            // Only Proxy Hosts get their own marker - ordinary, directly-connected Data Movers are intentionally
            // not shown (they are considered co-located with the MasterServer itself).
            if (sample.isProxyHost && sample.mover && sample.moverLat !== undefined && sample.moverLon !== undefined) {
                var mGroup = byMover[sample.mover];
                if (!mGroup) {
                    mGroup = byMover[sample.mover] = { lat: sample.moverLat, lon: sample.moverLon, hasActive: false };
                }
                if (sample.status === "ACTIVE") {
                    mGroup.hasActive = true;
                }
            }
            if (sample.hostCountry) {
                var cGroup = byCountry[sample.hostCountry];
                if (!cGroup) {
                    cGroup = byCountry[sample.hostCountry] = { latSum: 0, lonSum: 0, hosts: Object.create(null), transfers: Object.create(null) };
                }
                if (!cGroup.hosts[sample.host]) {
                    cGroup.hosts[sample.host] = true;
                    cGroup.latSum += sample.hostLat;
                    cGroup.lonSum += sample.hostLon;
                }
                cGroup.transfers[sample.transferId] = sample;
            }
        });
        if (viewMode === "host") {
            clearAllCountryEntities();
            Object.keys(byHost).forEach(function (name) {
                var g = byHost[name];
                upsertHost(name, g.lat, g.lon, g.label, g.transfers);
            });
            // Any Host marker still on the globe but absent from this snapshot (and not already fading out from a
            // previous terminal-only snapshot) is stale - drop it immediately.
            Object.keys(hosts).forEach(function (name) {
                if (!byHost[name] && !hosts[name].removeTimeout) {
                    removeHost(name);
                }
            });
        } else {
            clearAllHostEntities();
            Object.keys(byCountry).forEach(function (code) {
                var g = byCountry[code];
                var hostCount = Object.keys(g.hosts).length;
                upsertCountry(code, g.latSum / hostCount, g.lonSum / hostCount, hostCount, g.transfers);
            });
            Object.keys(countries).forEach(function (code) {
                if (!byCountry[code] && !countries[code].removeTimeout) {
                    removeCountry(code);
                }
            });
        }
        Object.keys(byMover).forEach(function (name) {
            var m = byMover[name];
            upsertMover(name, m.lat, m.lon, m.hasActive);
        });
        Object.keys(movers).forEach(function (name) {
            if (!byMover[name] && !movers[name].removeTimeout) {
                removeMover(name);
            }
        });
        updateCountryTable(byCountry);
        updateUnresolvedNote(unresolvedActiveCount);
        updateKpis();
    }

    // Shows a small, subtle note in the legend when one or more currently ACTIVE transfers cannot be drawn on the
    // globe because their Host's address could not be geolocated (no GeoIP match and no `[GeoIP]` `forced.*`
    // override configured) - these are still counted in the KPI cards above (see updateKpis()), just not visible
    // as an arc/marker, so without this note their absence could otherwise look like a discrepancy.
    function updateUnresolvedNote(count) {
        var note = document.getElementById("globeUnresolvedNote");
        if (!note) {
            return;
        }
        if (count > 0) {
            document.getElementById("globeUnresolvedNoteText").textContent =
                count + " active transfer" + (count === 1 ? "" : "s") + " not shown (host location unresolved)";
            note.style.display = "block";
        } else {
            note.style.display = "none";
        }
    }

    // Renders the small "transmissions per country" breakdown table (shown only in "Per country" view, see
    // setViewMode()), sorted by active transfer count so the busiest countries are always at the top.
    function updateCountryTable(byCountry) {
        var tbody = document.getElementById("globeCountryTableBody");
        if (!tbody) {
            return;
        }
        var rows = Object.keys(byCountry).map(function (code) {
            var agg = aggregateTransfers(byCountry[code].transfers);
            var hostCount = Object.keys(byCountry[code].hosts).length;
            return {
                code: code,
                hostCount: hostCount,
                agg: agg,
                lat: byCountry[code].latSum / hostCount,
                lon: byCountry[code].lonSum / hostCount
            };
        });
        rows.sort(function (a, b) { return b.agg.activeCount - a.agg.activeCount || b.agg.totalRate - a.agg.totalRate; });
        if (rows.length === 0) {
            tbody.innerHTML = "<tr><td colspan=\"3\" class=\"globe-muted-text\">No active transfers</td></tr>";
            return;
        }
        tbody.innerHTML = rows.map(function (r) {
            var name = countryDisplayName(r.code);
            var flag = countryFlagEmoji(r.code);
            var flagHtml = flag
                ? "<span class=\"country-flag\" title=\"" + name + "\">" + flag + "</span>"
                : "<span title=\"" + name + "\">" + name + "</span>";
            var cell = flagHtml +
                "<i class=\"bi bi-crosshair country-flyto-btn\" title=\"Fly to " + name + "\" data-lat=\"" + r.lat +
                "\" data-lon=\"" + r.lon + "\"></i>";
            return "<tr><td class=\"country-name\">" + cell + "</td><td>" + r.agg.activeCount + "</td><td>" +
                formatRate(r.agg.totalRate) + "</td></tr>";
        }).join("");
    }

    // Switches between "Per host" and "Per country" grouping, redrawing immediately from the last received
    // snapshot (rather than waiting up to POLL_PERIOD_SECONDS for the next one) so the switch feels instant.
    function setViewMode(mode) {
        if (mode !== "host" && mode !== "country") {
            return;
        }
        viewMode = mode;
        localStorage.setItem(VIEW_MODE_PREF_KEY, mode);
        document.getElementById("globeCountryTable").style.display = mode === "country" ? "block" : "none";
        if (mode === "country") {
            // Only meaningful once the table is actually visible/laid out; re-measure now rather than waiting for
            // the next resize event so the cap is already correct on the very first frame it's shown.
            adjustCountryTableMaxHeight();
        }
        applySnapshot(rawSamples);
    }

    // Switches which direction(s) of transfer to monitor ("both"/"dissemination"/"acquisition"), redrawing
    // immediately from the last received (unfiltered) snapshot so the switch feels instant, and refreshing the
    // "Transferred (24h)" KPI to match (see currentBytesLast24h()).
    function setDirectionMode(mode) {
        if (["both", "dissemination", "acquisition"].indexOf(mode) === -1) {
            return;
        }
        directionMode = mode;
        localStorage.setItem(DIRECTION_MODE_PREF_KEY, mode);
        document.getElementById("globeDirectionBtn").classList.toggle("active", mode !== "both");
        applySnapshot(rawSamples);
    }

    function formatBytes(n) {
        if (!n && n !== 0) return "-";
        var units = ["B", "KB", "MB", "GB", "TB"];
        var i = 0;
        while (n >= 1024 && i < units.length - 1) { n /= 1024; i++; }
        return n.toFixed(n < 10 && i > 0 ? 1 : 0) + " " + units[i];
    }

    function formatRate(bps) {
        if (!bps || bps <= 0) return "-";
        var units = ["bps", "Kbps", "Mbps", "Gbps"];
        var i = 0;
        while (bps >= 1000 && i < units.length - 1) { bps /= 1000; i++; }
        return bps.toFixed(1) + " " + units[i];
    }

    // Renders the shared info-panel body (used for both a single Host and an aggregated country) from an
    // already-computed agg (see aggregateTransfers()) and its underlying transfer samples.
    function renderInfoPanel(title, agg, transfers) {
        document.getElementById("globeInfoTitle").textContent = title;
        var body = document.getElementById("globeInfoBody");
        var html =
            "<dt>Active transfers</dt><dd>" + agg.activeCount + "</dd>" +
            "<dt>Protocol(s)</dt><dd>" + (agg.protocols.join(", ") || "-") + "</dd>" +
            "<dt>Total throughput</dt><dd>" + formatRate(agg.totalRate) + "</dd>" +
            "<dt>Total transferred</dt><dd>" + formatBytes(agg.totalBytes) + "</dd>" +
            "<dt>Longest duration</dt><dd>" + Math.round(agg.maxDuration / 1000) + " s</dd>";
        html += "<dt>Per-transfer</dt><dd><table style=\"width:100%;font-size:.76rem;\"><thead><tr>" +
            "<th>Mover</th><th>Proto</th><th>Rate</th><th>Bytes</th><th>Status</th></tr></thead><tbody>";
        transfers.forEach(function (s) {
            html += "<tr><td>" + (s.mover || "-") + "</td><td>" + (s.protocol || "-") + "</td><td>" +
                formatRate(s.rateBitsPerSecond) + "</td><td>" + formatBytes(s.bytesSent) + "</td><td>" +
                (s.status || "-") + "</td></tr>";
        });
        html += "</tbody></table></dd>";
        body.innerHTML = html;
        document.getElementById("globeInfoPanel").style.display = "block";
    }

    function showInfoPanel(hostName) {
        var h = hosts[hostName];
        if (!h) {
            return;
        }
        var samples = Object.keys(h.transfers).map(function (id) { return h.transfers[id]; });
        var destination = samples[0] && samples[0].destination;
        renderInfoPanel((h.label || hostName) + (destination ? " (" + destination + ")" : ""), h.agg, samples);
    }

    function showCountryInfoPanel(code) {
        var c = countries[code];
        if (!c) {
            return;
        }
        var samples = Object.keys(c.transfers).map(function (id) { return c.transfers[id]; });
        var title = countryDisplayName(code) + " (" + c.hostCount + " host" + (c.hostCount === 1 ? "" : "s") + ")";
        renderInfoPanel(title, c.agg, samples);
    }

    var handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
    handler.setInputAction(function (movement) {
        var picked = viewer.scene.pick(movement.position);
        // scene.pick() returns the PointPrimitive itself (no .id was set on it), so the custom "hostName"/
        // "countryCode" property we attached in upsertHost()/upsertCountry() is read directly.
        if (Cesium.defined(picked) && Cesium.defined(picked.hostName)) {
            showInfoPanel(picked.hostName);
        } else if (Cesium.defined(picked) && Cesium.defined(picked.countryCode)) {
            showCountryInfoPanel(picked.countryCode);
        }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    // ------------------------------------------------------------------
    // WebSocket connection with auto-reconnect (exponential backoff)
    // ------------------------------------------------------------------
    var reconnectDelay = 1000;
    var ws = null;

    function connect() {
        setStatus("connecting", "Connecting...");
        var proto = location.protocol === "https:" ? "wss:" : "ws:";
        ws = new WebSocket(proto + "//" + location.host + "/ws/globe");
        ws.onopen = function () {
            reconnectDelay = 1000;
            setStatus("connected", "Live");
        };
        ws.onclose = function () {
            setStatus("disconnected", "Disconnected - retrying...");
            setTimeout(connect, reconnectDelay);
            reconnectDelay = Math.min(reconnectDelay * 2, 30000);
        };
        ws.onerror = function () {
            ws.close();
        };
        ws.onmessage = function (evt) {
            var msg;
            try {
                msg = JSON.parse(evt.data);
            } catch (e) {
                return;
            }
            if (msg.type === "hello") {
                applyOrigin(msg);
            } else if (msg.type === "snapshot") {
                if (typeof msg.bytes24h === "number") {
                    bytesLast24h = msg.bytes24h;
                }
                if (typeof msg.bytes24hDissemination === "number") {
                    bytesLast24hDissemination = msg.bytes24hDissemination;
                }
                if (typeof msg.bytes24hAcquisition === "number") {
                    bytesLast24hAcquisition = msg.bytes24hAcquisition;
                }
                if (typeof msg.dataPortalSessions === "number") {
                    dataPortalSessions = msg.dataPortalSessions;
                }
                if (typeof msg.dataPortalBytesInPerSecond === "number") {
                    dataPortalBytesInPerSecond = msg.dataPortalBytesInPerSecond;
                }
                if (typeof msg.dataPortalBytesOutPerSecond === "number") {
                    dataPortalBytesOutPerSecond = msg.dataPortalBytesOutPerSecond;
                }
                if (typeof msg.moverStorageUsedBytes === "number") {
                    moverStorageUsedBytes = msg.moverStorageUsedBytes;
                }
                if (typeof msg.moverStorageTotalBytes === "number") {
                    moverStorageTotalBytes = msg.moverStorageTotalBytes;
                }
                applyOrigin(msg);
                applySnapshot(msg.transfers || []);
                updateDataPortalKpis();
            }
        };
    }

    connect();

    // The globe page never issues any other HTTP request after the initial page load (all live data flows over the
    // WebSocket above), so - unlike every other page in the application - simply leaving it open does not reset the
    // HttpSession's inactivity timer, and the user's login can silently expire while they are actively watching the
    // globe. Periodically pinging a tiny authenticated no-op endpoint keeps the session alive without ever reloading
    // the page (which would otherwise reset the Cesium view/camera).
    var SESSION_KEEPALIVE_PERIOD_MS = 5 * 60 * 1000;
    setInterval(function () {
        fetch("/do/monitoring/globe/keepalive", { method: "GET", credentials: "same-origin" }).catch(function () {});
    }, SESSION_KEEPALIVE_PERIOD_MS);

    var recenterBtn = document.getElementById("globeRecenterBtn");
    recenterBtn.addEventListener("click", function () {
        if (!origin) {
            return;
        }
        viewer.camera.flyTo({
            destination: Cesium.Cartesian3.fromDegrees(origin.lon, origin.lat, 12000000)
        });
    });

    // Delegated click handler for the per-row "fly to" icons in the country table (rows are re-rendered on every
    // snapshot, so a static per-row listener would need to be re-attached each time - delegation avoids that).
    document.getElementById("globeCountryTableBody").addEventListener("click", function (evt) {
        var btn = evt.target.closest(".country-flyto-btn");
        if (!btn) {
            return;
        }
        var lat = parseFloat(btn.getAttribute("data-lat"));
        var lon = parseFloat(btn.getAttribute("data-lon"));
        if (isNaN(lat) || isNaN(lon)) {
            return;
        }
        viewer.camera.flyTo({
            destination: Cesium.Cartesian3.fromDegrees(lon, lat, 4000000)
        });
    });

    var fullscreenBtn = document.getElementById("globeFullscreenBtn");
    var fullscreenIcon = fullscreenBtn.querySelector("i");
    fullscreenBtn.addEventListener("click", function () {
        if (!document.fullscreenElement) {
            document.getElementById("globeContainer").requestFullscreen();
        } else {
            document.exitFullscreen();
        }
    });
    document.addEventListener("fullscreenchange", function () {
        var isFullscreen = !!document.fullscreenElement;
        fullscreenIcon.className = isFullscreen ? "bi bi-fullscreen-exit" : "bi bi-arrows-fullscreen";
        fullscreenBtn.title = isFullscreen ? "Exit full screen" : "Toggle full screen";
    });

    // Phone-only KPI panel toggle (see the "@media (max-width:700px)" rules above): the panel itself defaults to
    // hidden below that breakpoint via CSS alone, so this just flips it open/closed on demand and keeps the button's
    // icon/label in sync - never fighting the globe for screen space on a small device.
    var statsPanel = document.getElementById("globeStatsPanel");
    var statsToggleBtn = document.getElementById("globeStatsToggleBtn");
    var statsToggleIcon = statsToggleBtn.querySelector("i");
    var statsToggleLabel = document.getElementById("globeStatsToggleLabel");
    statsToggleBtn.addEventListener("click", function () {
        var isOpen = statsPanel.classList.toggle("globe-stats-open");
        statsToggleIcon.className = isOpen ? "bi bi-x-lg" : "bi bi-graph-up";
        statsToggleLabel.textContent = isOpen ? "Globe" : "KPIs";
        statsToggleBtn.title = isOpen ? "Hide the KPI panel and show the globe" : "Show the KPI panel";
        // Opening/closing the KPI panel changes the bottom-right stack's height (and, on very short phone screens,
        // its top edge) - keep the country table's height cap (see adjustCountryTableMaxHeight()) in sync.
        adjustCountryTableMaxHeight();
    });
}());
</script>
