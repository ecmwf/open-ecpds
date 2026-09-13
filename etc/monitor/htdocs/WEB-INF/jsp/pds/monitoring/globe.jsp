<%@ page session="true" %>

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
#globeHeader .subtitle{font-size:.76rem;color:var(--bs-secondary-color,#6c757d);}
#globeKpiRow{display:grid;grid-template-columns:repeat(4,1fr);gap:.6rem;margin-top:.65rem;}
@media (max-width: 900px){#globeKpiRow{grid-template-columns:repeat(2,1fr);}}
.globe-kpi-card{background:var(--bs-tertiary-bg,#f8f9fa);border:1px solid var(--bs-border-color);border-radius:10px;padding:.6rem .8rem;display:flex;align-items:center;gap:.65rem;min-height:64px;}
.globe-kpi-icon{font-size:1.3rem;color:#0dcaf0;flex-shrink:0;width:28px;text-align:center;}
.globe-kpi-value{font-size:1.25rem;font-weight:700;line-height:1.1;font-variant-numeric:tabular-nums;}
.globe-kpi-label{font-size:.7rem;color:var(--bs-secondary-color,#6c757d);text-transform:uppercase;letter-spacing:.03em;}
.globe-kpi-text{display:flex;flex-direction:column;gap:1px;min-width:0;}
.globe-kpi-gauge{gap:.5rem;}
.globe-gauge-svg{width:52px;height:34px;flex-shrink:0;}
.globe-gauge-arc-bg{fill:none;stroke:var(--bs-border-color,#dee2e6);stroke-width:9;stroke-linecap:round;}
.globe-gauge-arc{fill:none;stroke:#38bdf8;stroke-width:9;stroke-linecap:round;transition:stroke-dashoffset .8s ease,stroke .8s ease;}
#globeContainer{position:relative;width:100%;height:calc(100vh - 340px);min-height:420px;border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.15);}
#globeLegend{position:absolute;left:10px;top:10px;z-index:10;background:rgba(20,25,30,.72);color:#eee;border-radius:8px;padding:.5rem .75rem;font-size:.78rem;line-height:1.5;backdrop-filter:blur(2px);}
#globeLegend .dot{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:5px;}
#globeInfoPanel{position:absolute;right:10px;top:10px;z-index:10;width:290px;max-width:80vw;background:rgba(20,25,30,.86);color:#eee;border-radius:8px;padding:.75rem 1rem;font-size:.82rem;display:none;box-shadow:0 4px 16px rgba(0,0,0,.35);}
#globeInfoPanel h6{color:#9fd6ff;margin-bottom:.4rem;}
#globeInfoPanel .close-btn{position:absolute;top:6px;right:8px;cursor:pointer;color:#ccc;}
#globeInfoPanel dl{margin:0;}
#globeInfoPanel dt{color:#aaa;font-weight:400;}
#globeInfoPanel dd{margin-bottom:.35rem;word-break:break-all;}
#globeOriginWarning{position:absolute;right:10px;bottom:10px;z-index:10;max-width:min(360px,80vw);background:rgba(20,25,30,.86);color:#eee;border-radius:8px;padding:.55rem .8rem;font-size:.78rem;line-height:1.4;display:none;box-shadow:0 4px 16px rgba(0,0,0,.35);}
#globeOriginWarning i{margin-right:.4rem;color:#997404;}
</style>

<div id="globeHeader">
    <div class="title-group">
        <i class="bi bi-globe2 text-info"></i>
        <div>
            <div class="title-text">Live Earth</div>
            <div class="subtitle" id="globeSubtitle">Connecting...</div>
        </div>
    </div>
    <div class="actions-group">
        <span class="globe-badge-connecting" id="globeStatusBadge">Connecting...</span>
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
        <button type="button" id="globeFullscreenBtn" class="globe-icon-btn" title="Toggle full screen">
            <i class="bi bi-arrows-fullscreen"></i>
        </button>
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
    </div>
    <div id="globeInfoPanel">
        <span class="close-btn" onclick="document.getElementById('globeInfoPanel').style.display='none';">&times;</span>
        <h6 id="globeInfoTitle">Transfer</h6>
        <dl id="globeInfoBody"></dl>
    </div>
    <div id="globeOriginWarning">
        <i class="bi bi-exclamation-triangle-fill"></i><strong>OpenECPDS location not configured.</strong>
        <span id="globeOriginWarningText">The origin marker cannot be placed because the OpenECPDS geolocation could not be resolved.</span>
    </div>
</div>

<div id="globeKpiRow">
    <div class="globe-kpi-card">
        <i class="bi bi-arrow-left-right globe-kpi-icon"></i>
        <div class="globe-kpi-text">
            <span class="globe-kpi-value" id="kpiTransfers">0</span>
            <span class="globe-kpi-label">Active transfers</span>
        </div>
    </div>
    <div class="globe-kpi-card">
        <i class="bi bi-hdd-network globe-kpi-icon"></i>
        <div class="globe-kpi-text">
            <span class="globe-kpi-value" id="kpiHosts">0</span>
            <span class="globe-kpi-label">Active hosts</span>
        </div>
    </div>
    <div class="globe-kpi-card globe-kpi-gauge">
        <svg class="globe-gauge-svg" viewBox="0 0 120 68">
            <path class="globe-gauge-arc-bg" d="M10,62 A50,50 0 0 1 110,62"></path>
            <path class="globe-gauge-arc" id="gaugeArc" d="M10,62 A50,50 0 0 1 110,62"></path>
        </svg>
        <div class="globe-kpi-text">
            <span class="globe-kpi-value" id="kpiThroughput">0 bps</span>
            <span class="globe-kpi-label">Combined throughput</span>
        </div>
    </div>
    <div class="globe-kpi-card">
        <i class="bi bi-hdd-stack globe-kpi-icon"></i>
        <div class="globe-kpi-text">
            <span class="globe-kpi-value" id="kpiBytes">0 B</span>
            <span class="globe-kpi-label">Transferred (24h)</span>
        </div>
    </div>
</div>

<script src="/cesium/Cesium.js"></script>
<script>
(async function () {
    "use strict";

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
    viewer.scene.globe.enableLighting = false;
    viewer.scene.skyAtmosphere.show = true;
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

    var origin = null; // {lat, lon}
    var originPoint = null;
    // hostName -> { arc, point, lat, lon, transfers: {transferId: sample}, status, removeTimeout, pulsePhase }
    var hosts = Object.create(null);
    // moverName -> { point, lat, lon, transferIds: Set, removeTimeout } - only for Proxy Hosts (see
    // isProxyHost on each transfer sample), never for ordinary, directly-connected Data Movers.
    var movers = Object.create(null);

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
    // and every reconnect - shows the exact same, always-on figure rather than a per-connection counter.
    var bytesLast24h = 0;

    // The raw sample list from the most recent "snapshot" message, kept so the KPI cards (see updateKpis()) can
    // always reflect the true active-transfer/host count straight from the MasterServer, independent of whether
    // the MasterServer's own origin location or any target Host's geolocation has been resolved yet (geolocation is
    // only needed to actually place a marker/arc on the globe, not to count activity).
    var lastSamples = [];

    // Auto-scaling gauge ceiling for the throughput speed-meter: grows immediately to cover new peaks, then
    // decays slowly back down so the gauge stays meaningful/readable as activity drops rather than staying
    // pinned at a peak seen minutes ago.
    var gaugeMax = 1e6; // starts at 1 Mbps
    var gaugeArcLength = null;

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

    // Counts straight from the last raw sample list (see lastSamples above), not from the `hosts` map used for
    // marker placement, so these figures stay accurate even while the origin and/or target Host locations are not
    // (yet) resolved.
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
        document.getElementById("kpiBytes").textContent = formatBytes(bytesLast24h);
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

    function setOrigin(lat, lon) {
        var firstTime = !originPoint;
        origin = { lat: lat, lon: lon };
        if (originPoint) {
            points.remove(originPoint);
        }
        originPoint = points.add({
            position: Cesium.Cartesian3.fromDegrees(lon, lat),
            pixelSize: 12,
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
            var text = "The origin marker cannot be placed because the OpenECPDS geolocation could not be resolved";
            text += msg.originHost ? " for " + msg.originHost + "." : ".";
            document.getElementById("globeOriginWarningText").textContent = text;
            document.getElementById("globeOriginWarning").style.display = "block";
        }
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
                pixelSize: 10,
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

    // Aggregates every transfer currently reported for one Host into the counters the marker/arc/panel need.
    function aggregateHost(transferMap) {
        var agg = { activeCount: 0, totalRate: 0, totalBytes: 0, protocols: {}, hasActive: false, hasFailed: false, maxDuration: 0, arcOrigin: null };
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
        var agg = aggregateHost(transferMap);
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
        var positions = [
            Cesium.Cartesian3.fromDegrees(arcOrigin.lon, arcOrigin.lat),
            Cesium.Cartesian3.fromDegrees(lon, lat)
        ];
        var arc = arcs.add({
            positions: positions,
            width: rateWidth(agg.totalRate),
            material: Cesium.Material.fromType("Color", { color: color.withAlpha(0.85) })
        });
        var point = points.add({
            position: Cesium.Cartesian3.fromDegrees(lon, lat),
            pixelSize: Cesium.Math.clamp(8 + agg.activeCount * 1.5, 8, 20),
            color: color,
            outlineColor: Cesium.Color.WHITE,
            outlineWidth: 1
        });
        point.hostName = name;
        var entry = {
            arc: arc, point: point, lat: lat, lon: lon, label: label || name, transfers: transferMap, agg: agg,
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
    });

    // Groups the flat sample list from a "snapshot" message by target Host (skipping ones whose
    // geolocation could not be resolved) for marker/arc placement, then reconciles the current marker/arc set
    // against it. The KPI cards are updated separately (see updateKpis()), straight from the raw, ungrouped
    // sample list, so they stay accurate even for samples that get skipped here.
    function applySnapshot(samples) {
        lastSamples = samples;
        var byHost = Object.create(null);
        var byMover = Object.create(null);
        samples.forEach(function (sample) {
            if (sample.hostLat === undefined || sample.hostLon === undefined || !sample.host) {
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
        });
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
        Object.keys(byMover).forEach(function (name) {
            var m = byMover[name];
            upsertMover(name, m.lat, m.lon, m.hasActive);
        });
        Object.keys(movers).forEach(function (name) {
            if (!byMover[name] && !movers[name].removeTimeout) {
                removeMover(name);
            }
        });
        updateKpis();
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

    function showInfoPanel(hostName) {
        var h = hosts[hostName];
        if (!h) {
            return;
        }
        var samples = Object.keys(h.transfers).map(function (id) { return h.transfers[id]; });
        var destination = samples[0] && samples[0].destination;
        document.getElementById("globeInfoTitle").textContent = (h.label || hostName) + (destination ? " (" + destination + ")" : "");
        var body = document.getElementById("globeInfoBody");
        var html =
            "<dt>Active transfers</dt><dd>" + h.agg.activeCount + "</dd>" +
            "<dt>Protocol(s)</dt><dd>" + (h.agg.protocols.join(", ") || "-") + "</dd>" +
            "<dt>Total throughput</dt><dd>" + formatRate(h.agg.totalRate) + "</dd>" +
            "<dt>Total transferred</dt><dd>" + formatBytes(h.agg.totalBytes) + "</dd>" +
            "<dt>Longest duration</dt><dd>" + Math.round(h.agg.maxDuration / 1000) + " s</dd>";
        html += "<dt>Per-transfer</dt><dd><table style=\"width:100%;font-size:.76rem;\"><thead><tr>" +
            "<th>Mover</th><th>Proto</th><th>Rate</th><th>Bytes</th><th>Status</th></tr></thead><tbody>";
        samples.forEach(function (s) {
            html += "<tr><td>" + (s.mover || "-") + "</td><td>" + (s.protocol || "-") + "</td><td>" +
                formatRate(s.rateBitsPerSecond) + "</td><td>" + formatBytes(s.bytesSent) + "</td><td>" +
                (s.status || "-") + "</td></tr>";
        });
        html += "</tbody></table></dd>";
        body.innerHTML = html;
        document.getElementById("globeInfoPanel").style.display = "block";
    }

    var handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
    handler.setInputAction(function (movement) {
        var picked = viewer.scene.pick(movement.position);
        // scene.pick() returns the PointPrimitive itself (no .id was set on it), so the
        // custom "hostName" property we attached in upsertHost() is read directly.
        if (Cesium.defined(picked) && Cesium.defined(picked.hostName)) {
            showInfoPanel(picked.hostName);
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
                applyOrigin(msg);
                applySnapshot(msg.transfers || []);
            }
        };
    }

    connect();

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
}());
</script>
