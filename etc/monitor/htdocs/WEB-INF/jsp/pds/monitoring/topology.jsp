<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<style>
#topoHeader{display:flex;align-items:center;justify-content:space-between;gap:.75rem;flex-wrap:wrap;margin-bottom:.65rem;padding-bottom:.6rem;border-bottom:1px solid var(--bs-border-color);}
#topoHeader .title-group{display:flex;align-items:center;gap:.6rem;}
#topoHeader .actions-group{display:flex;align-items:center;gap:.5rem;}
#topoHeader .title-group i{font-size:1.4rem;}
#topoHeader .title-text{font-size:1.05rem;font-weight:600;line-height:1.15;}
#topoHeader .title-text .btn{font-size:.75rem;margin-left:.35rem;vertical-align:1px;}
#topoHeader .subtitle{font-size:.76rem;color:var(--bs-secondary-color,#6c757d);}
.topo-icon-btn{display:inline-flex;align-items:center;justify-content:center;width:30px;height:30px;padding:0;border:none;border-radius:6px;background:transparent;color:var(--bs-secondary-color,#6c757d);font-size:1rem;line-height:1;transition:background .15s ease,color .15s ease;}
.topo-icon-btn i{display:inline-flex;align-items:center;justify-content:center;width:100%;height:100%;}
.topo-icon-btn:hover{background:var(--bs-tertiary-bg,#f1f3f5);color:var(--bs-body-color,#212529);}
.topo-icon-btn:focus{outline:none;box-shadow:0 0 0 .15rem rgba(13,202,240,.35);}
#topoContainer{position:relative;width:100%;height:70vh;min-height:420px;border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.15);background:var(--bs-tertiary-bg,#f8f9fa);border:1px solid var(--bs-border-color);}
#topoCy{position:absolute;inset:0;width:100%;height:100%;}
#topoLegend{position:absolute;left:10px;top:10px;z-index:10;background:var(--bs-body-bg);color:var(--bs-body-color);border:1px solid var(--bs-border-color);border-radius:8px;padding:.5rem .75rem;font-size:.76rem;line-height:1.55;box-shadow:0 2px 8px rgba(0,0,0,.12);}
#topoLegend .dot{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:6px;}
#topoDetailPanel{position:absolute;right:10px;top:10px;z-index:10;width:280px;max-width:80vw;max-height:calc(100vh - 20px);overflow-y:auto;background:var(--bs-body-bg);color:var(--bs-body-color);border:1px solid var(--bs-border-color);border-radius:8px;padding:.75rem 1rem;font-size:.82rem;display:none;box-shadow:0 4px 16px rgba(0,0,0,.2);}
#topoDetailPanel h6{color:#0d6efd;margin-bottom:.4rem;}
#topoDetailPanel .close-btn{position:absolute;top:6px;right:8px;cursor:pointer;color:var(--bs-secondary-color,#6c757d);}
#topoDetailPanel dl{margin:0;}
#topoDetailPanel dt{color:var(--bs-secondary-color,#6c757d);font-weight:400;}
#topoDetailPanel dd{margin-bottom:.35rem;word-break:break-all;}
#topoEmptyState{position:absolute;inset:0;display:none;align-items:center;justify-content:center;flex-direction:column;gap:.5rem;color:var(--bs-secondary-color,#6c757d);font-size:.9rem;text-align:center;padding:2rem;}
#topoEmptyState i{font-size:2rem;}
</style>

<div id="topoHeader">
    <div class="title-group">
        <i class="bi bi-diagram-3 text-info"></i>
        <div>
            <div class="title-text">
                <%=System.getProperty("monitor.nickName")%> System Topology
                <button class="btn btn-link btn-sm text-muted p-0 align-baseline" type="button"
                    data-bs-toggle="collapse" data-bs-target="#topoInfoCollapse"
                    aria-expanded="false" title="About this page">
                  <i class="bi bi-info-circle"></i>
                </button>
            </div>
            <div class="subtitle" id="topoSubtitle">Loading...</div>
        </div>
    </div>
    <div class="actions-group">
        <div class="d-flex align-items-center gap-1" id="topoRefreshPills" title="Auto-refresh interval">
            <i class="bi bi-arrow-repeat text-muted me-1" style="font-size:.85rem;"></i>
            <a href="#" class="date-pill topo-refresh-pill" data-value="15">15s</a>
            <a href="#" class="date-pill topo-refresh-pill" data-value="30">30s</a>
            <a href="#" class="date-pill topo-refresh-pill" data-value="60">1m</a>
            <a href="#" class="date-pill topo-refresh-pill" data-value="300">5m</a>
            <a href="#" class="date-pill topo-refresh-pill" data-value="0">Off</a>
        </div>
        <span class="text-muted" style="font-size:0.75rem;">|</span>
        <button class="topo-icon-btn" id="topoRefreshBtn" title="Refresh now"><i class="bi bi-arrow-clockwise"></i></button>
        <button class="topo-icon-btn" id="topoFitAllBtn" title="Zoom out to see all Data Movers"><i class="bi bi-arrows-angle-expand"></i></button>
        <button class="topo-icon-btn" id="topoFullscreenBtn" title="Toggle fullscreen"><i class="bi bi-arrows-fullscreen"></i></button>
    </div>
</div>

<div class="collapse mb-3" id="topoInfoCollapse">
    <div class="card card-body" style="border-top:3px solid var(--bs-primary,#0d6efd);">
        <p>
            <strong>System Topology</strong> is a live diagram of every OpenECPDS component this Monitor knows
            about: the <strong>Master Server</strong> (the central coordinator), every <strong>Data Mover</strong>
            (the machines that actually move the data), the <strong>database</strong> the Master relies on, and
            this <strong>Monitor</strong> itself (the web UI you are using right now). Components are grouped into
            dashed boxes by machine/host, so you can see at a glance which components share a physical (or
            virtual/container) machine and which run on separate ones — this depends entirely on how your specific
            deployment is laid out, and can be anything from "everything on one box" to fully distributed.
        </p>
        <ul class="mb-2">
            <li><strong>Boxes</strong> group components running on the same host; components in separate boxes run
                on different machines.</li>
            <li><strong>Arrows</strong> show the direction of the control connection between two components (e.g.
                Master&nbsp;&rarr;&nbsp;Database, Master&nbsp;&rarr;&nbsp;Data&nbsp;Mover).</li>
            <li>For the Master, Monitor, and each connected Data Mover, the <strong>ports</strong> shown are every
                network plugin/protocol currently loaded in that JVM (e.g. <code>ecproxy</code>, <code>ftp</code>,
                <code>http</code>) together with its listening port and live status.</li>
            <li>A Data Mover shown <strong>greyed out with a dashed border</strong> is currently reporting as
                <strong>down</strong> (its most recent availability check failed, or it is disabled in its
                configuration) — the rest of the diagram keeps working normally around it.</li>
            <li>A Data Mover's ring color reflects its <strong>TransferGroup</strong> - Data Movers sharing the
                same group get the same ring color, making it easy to spot which ones back the same group.</li>
            <li>Click any node to see its full details (host, ports, status) in the panel on the right.</li>
            <li>Drag any box to rearrange the diagram to your liking - your layout is remembered on this browser
                between visits, including after navigating away and coming back.</li>
            <li>The <strong>15s/30s/1m/5m/Off</strong> pills control how often the diagram refreshes itself
                automatically (remembered on this browser between visits); use the
                <i class="bi bi-arrow-clockwise"></i> button to refresh immediately regardless, and the
                <i class="bi bi-arrows-fullscreen"></i> button to expand it to fullscreen.</li>
        </ul>
        <p class="mb-0 text-muted small">
            Note: every connected Monitor instance is shown (there can be more than one for HA/scale-out setups), not
            just the one serving the current page. A Data Mover's per-plugin ports are only available while it is
            actively connected to the Master; if it currently isn't, only its single registration host/port is shown
            instead. Database reachability is inferred from the fact the Master answered this request at all, not
            from a separate live probe.
        </p>
    </div>
</div>

<div id="topoContainer">
    <div id="topoCy"></div>
    <div id="topoEmptyState">
        <i class="bi bi-diagram-3"></i>
        <div>No topology data available yet.</div>
    </div>
    <div id="topoLegend">
        <div><span class="dot" style="background:#0d6efd;"></span>Master Server</div>
        <div><span class="dot" style="background:#0dcaf0;"></span>Monitor (this UI)</div>
        <div><span class="dot" style="background:#6f42c1;"></span>Database</div>
        <div><span class="dot" style="background:#198754;"></span>Data Mover (up)</div>
        <div><span class="dot" style="background:#adb5bd;"></span>Data Mover (down)</div>
    </div>
    <div id="topoDetailPanel">
        <span class="close-btn" onclick="document.getElementById('topoDetailPanel').style.display='none';">&times;</span>
        <h6 id="topoDetailTitle">Details</h6>
        <dl id="topoDetailBody"></dl>
    </div>
</div>

<script src="/cytoscape/cytoscape.min.js"></script>
<script>
(function () {
    "use strict";

    var cy = null;
    var refreshTimer = null;

    var topoContainerEl = document.getElementById("topoContainer");

    function resizeTopoContainer() {
        if (document.fullscreenElement === topoContainerEl) {
            // The Fullscreen API already makes the element fill the whole screen; do not fight it.
            topoContainerEl.style.height = "";
            if (cy) { cy.resize(); }
            return;
        }
        var top = topoContainerEl.getBoundingClientRect().top;
        var footerEl = document.getElementById("bottomfooter");
        var footerHeight = footerEl ? footerEl.getBoundingClientRect().height : 44;
        var breathingRoom = 16;
        var available = window.innerHeight - top - footerHeight - breathingRoom;
        topoContainerEl.style.height = Math.max(420, available) + "px";
        if (cy) { cy.resize(); }
    }
    // Deliberately not called immediately at parse-time - see the identical rationale in globe.jsp: this early in
    // the page load, surrounding chrome/fonts/the fixed footer haven't settled into their final layout yet, and
    // this content is nested inside "#contentDiv" (kept "display:none" behind a loading overlay by layout.jsp until
    // its own "load" handler reveals it) - so a naive "load" listener here would run *before* "#contentDiv" is
    // actually shown, measuring a hidden (zero-size) container. Instead we keep the CSS default (70vh/min 420px)
    // as the first paint, and poll (via requestAnimationFrame) until the container truly has a non-zero size
    // before computing the accurate pixel height, so there is no visible resize flash either way.
    function waitUntilVisibleThenResizeTopo(attemptsLeft) {
        if (topoContainerEl.getBoundingClientRect().height > 0) {
            resizeTopoContainer();
            return;
        }
        if (attemptsLeft <= 0) {
            return;
        }
        requestAnimationFrame(function() { waitUntilVisibleThenResizeTopo(attemptsLeft - 1); });
    }
    window.addEventListener("load", function() { waitUntilVisibleThenResizeTopo(120); });
    window.addEventListener("resize", resizeTopoContainer);
    document.addEventListener("fullscreenchange", resizeTopoContainer);
    // Expanding/collapsing the "About this page" info card above shifts everything below it (including this
    // container's own top offset), so it needs the same re-measure as an actual window resize - both at the start
    // and the end of the Bootstrap collapse animation, since the container's "top" keeps changing throughout it.
    var topoInfoCollapseEl = document.getElementById("topoInfoCollapse");
    if (topoInfoCollapseEl) {
        ["show.bs.collapse", "hide.bs.collapse", "shown.bs.collapse", "hidden.bs.collapse"].forEach(function (evt) {
            topoInfoCollapseEl.addEventListener(evt, resizeTopoContainer);
        });
    }

    function isDarkTheme() {
        return document.documentElement.getAttribute("data-bs-theme") === "dark";
    }

    // Two hand-picked palettes (not literal CSS var() strings, since Cytoscape paints to a <canvas> and its style
    // values must be resolved colors, not CSS custom properties) matching the same dark/light glass look already
    // established for the other overlay panels on the Live Earth page.
    function palette() {
        return isDarkTheme() ? {
            master: "#3399ff", monitor: "#22d3ee", database: "#a78bfa", moverUp: "#2fb872", moverDown: "#5b6470",
            text: "#eee", edge: "#6b7684", hostBorder: "rgba(255,255,255,.25)", hostBg: "rgba(255,255,255,.03)"
        } : {
            master: "#0d6efd", monitor: "#0dcaf0", database: "#6f42c1", moverUp: "#198754", moverDown: "#adb5bd",
            text: "#1b1f24", edge: "#8a94a6", hostBorder: "rgba(0,0,0,.2)", hostBg: "rgba(0,0,0,.02)"
        };
    }

    function buildStyle() {
        var pal = palette();
        return [
            { selector: "node[kind='host']", style: {
                "shape": "round-rectangle", "background-color": pal.hostBg, "border-width": 1,
                "border-style": "dashed", "border-color": pal.hostBorder, "label": "data(label)",
                "text-valign": "top", "text-halign": "center", "text-margin-y": -6,
                "font-size": 11, "font-weight": 600, "color": pal.text, "padding": "22px"
            } },
            { selector: "node[kind!='host']", style: {
                "shape": "round-rectangle", "width": 118, "height": 48, "label": "data(label)",
                "text-valign": "center", "text-halign": "center", "text-wrap": "wrap", "text-max-width": 108,
                "font-size": 10.5, "font-weight": 600, "color": "#fff", "border-width": 2, "border-color": "#fff"
            } },
            { selector: "node[kind='master']", style: { "background-color": pal.master } },
            { selector: "node[kind='monitor']", style: { "background-color": pal.monitor } },
            { selector: "node[kind='database']", style: { "background-color": pal.database, "shape": "round-hexagon" } },
            { selector: "node[kind='mover']", style: { "height": 58 } },
            { selector: "node[kind='mover'][up]", style: { "background-color": pal.moverUp } },
            { selector: "node[kind='mover'][!up]", style: {
                "background-color": pal.moverDown, "border-style": "dashed", "text-opacity": 0.85
            } },
            // Colors the ring around each Data Mover node by its TransferGroup (data(groupColor) is a hex string
            // computed per-group in JS - see groupColorFor()), so movers sharing the same TransferGroup are
            // visually identifiable at a glance regardless of where they end up in the layout.
            { selector: "node[kind='mover'][groupColor]", style: {
                "border-width": 4, "border-color": "data(groupColor)"
            } },
            { selector: "edge", style: {
                "curve-style": "bezier", "width": 2, "line-color": pal.edge, "target-arrow-color": pal.edge,
                "target-arrow-shape": "triangle", "arrow-scale": 1, "label": "data(label)", "font-size": 9,
                "color": pal.text, "text-background-color": isDarkTheme() ? "#1b1f24" : "#fff",
                "text-background-opacity": 0.85, "text-background-padding": 2
            } },
            { selector: "edge[down]", style: { "line-style": "dashed", "line-color": pal.moverDown,
                "target-arrow-color": pal.moverDown } }
        ];
    }

    function shortHost(h) {
        if (!h) { return "unknown"; }
        return h.length > 22 ? h.substring(0, 20) + "\u2026" : h;
    }

    function pluginPortsLabel(p) {
        if (p.ports && p.ports.length) { return p.ports.join("/"); }
        return p.port != null ? String(p.port) : "";
    }

    function pluginSummary(plugins) {
        if (!plugins || !plugins.length) { return ""; }
        return plugins.map(function (p) {
            var ports = pluginPortsLabel(p);
            return p.ref + (ports ? (":" + ports) : "");
        }).join(", ");
    }

    // Fixed, high-contrast palette cycled deterministically by TransferGroup name (via a simple string hash), so
    // the same group always gets the same color across refreshes/reloads without needing any server-side mapping.
    var GROUP_COLOR_PALETTE = [
        "#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231", "#911eb4",
        "#46f0f0", "#f032e6", "#bcf60c", "#fabebe", "#008080", "#e6beff",
        "#9a6324", "#800000", "#aaffc3", "#808000", "#ffd8b1", "#000075"
    ];

    var groupColorCache = {};
    function groupColorFor(groupName) {
        if (!groupName) { return null; }
        if (groupColorCache[groupName]) { return groupColorCache[groupName]; }
        var hash = 0;
        for (var i = 0; i < groupName.length; i++) {
            hash = (hash * 31 + groupName.charCodeAt(i)) >>> 0;
        }
        var color = GROUP_COLOR_PALETTE[hash % GROUP_COLOR_PALETTE.length];
        groupColorCache[groupName] = color;
        return color;
    }

    function buildElements(data) {
        var nodes = [];
        var edges = [];
        var hostIds = {};

        function hostNodeId(host) {
            var id = "host:" + (host || "unknown");
            if (!hostIds[id]) {
                hostIds[id] = true;
                nodes.push({ data: { id: id, kind: "host", label: shortHost(host) } });
            }
            return id;
        }

        var master = data.master || {};
        var database = data.database || {};
        var monitors = data.monitors || [];
        var movers = data.movers || [];

        var masterHostId = hostNodeId(master.host);
        nodes.push({ data: {
            id: "master", kind: "master", parent: masterHostId,
            label: "Master Server\n" + pluginSummary(master.plugins),
            details: JSON.stringify(master)
        } });

        var dbHostId = hostNodeId(database.host);
        var dbLabel = "Database" + (database.port ? ("\n:" + database.port) : "");
        nodes.push({ data: {
            id: "database", kind: "database", parent: dbHostId, label: dbLabel,
            details: JSON.stringify(database)
        } });
        edges.push({ data: {
            id: "e-master-db", source: "master", target: "database",
            label: database.port ? ("JDBC:" + database.port) : "JDBC"
        } });

        monitors.forEach(function (monitor, idx) {
            var monitorHostId = hostNodeId(monitor.host);
            var nodeId = monitors.length > 1 ? ("monitor:" + (monitor.name || idx)) : "monitor";
            nodes.push({ data: {
                id: nodeId, kind: "monitor", parent: monitorHostId,
                label: "Monitor Server\n" + pluginSummary(monitor.plugins),
                details: JSON.stringify(monitor)
            } });
            edges.push({ data: { id: "e-monitor-master-" + idx, source: nodeId, target: "master", label: "RMI" } });
        });

        movers.forEach(function (mover, idx) {
            var moverHostId = hostNodeId(mover.host);
            var nodeId = "mover:" + mover.name;
            var up = !!mover.up;
            var portsLabel = mover.plugins && mover.plugins.length
                ? pluginSummary(mover.plugins)
                : (mover.port ? ("ecproxy:" + mover.port) : "");
            var moverData = {
                id: nodeId, kind: "mover", parent: moverHostId, up: up,
                transferGroup: mover.transferGroup || "",
                label: "Data Mover\n" + mover.name + (portsLabel ? ("\n" + portsLabel) : ""),
                details: JSON.stringify(mover)
            };
            var groupColor = groupColorFor(mover.transferGroup);
            if (groupColor) { moverData.groupColor = groupColor; }
            nodes.push({ data: moverData });
            edges.push({ data: {
                id: "e-master-mover-" + idx, source: "master", target: nodeId,
                label: mover.port ? ("ecproxy:" + mover.port) : "", down: !up
            } });
            if (mover.rmiConnected) {
                edges.push({ data: { id: "e-mover-master-" + idx, source: nodeId, target: "master", label: "RMI" } });
            }
        });

        return nodes.concat(edges);
    }

    function showDetail(node) {
        var details;
        try { details = JSON.parse(node.data("details") || "{}"); } catch (e) { details = {}; }
        var kind = node.data("kind");
        var title = { master: "Master Server", monitor: "Monitor Server", database: "Database", mover: "Data Mover" }[kind]
            || "Details";
        var subName = details.name || details.nickName;
        document.getElementById("topoDetailTitle").textContent = title + (subName ? (" \u2014 " + subName) : "");
        var body = document.getElementById("topoDetailBody");
        body.innerHTML = "";
        function addRow(label, value) {
            var dt = document.createElement("dt"); dt.textContent = label;
            var dd = document.createElement("dd"); dd.textContent = value;
            body.appendChild(dt); body.appendChild(dd);
        }
        if (details.host) { addRow("Host", details.host); }
        if (details.port) { addRow("Port", String(details.port)); }
        if (kind === "mover") {
            if (details.transferGroup) { addRow("Transfer Group", details.transferGroup); }
            addRow("Enabled", details.active ? "Yes" : "No");
            addRow("Status", details.up ? "Up" : "Down");
        }
        if (kind === "database") {
            addRow("Reachable", details.up ? "Yes" : "No");
            if (details.nodes && details.nodes.length > 1) {
                addRow("Cluster nodes", details.nodes.map(function (n) {
                    return n.host + (n.port ? (":" + n.port) : "");
                }).join(", "));
            }
        }
        if (details.plugins && details.plugins.length) {
            details.plugins.forEach(function (p) {
                var ports = pluginPortsLabel(p);
                addRow((p.name || p.ref) + " plugin", (ports ? ("port " + ports + ", ") : "") + (p.status || "unknown"));
            });
        }
        document.getElementById("topoDetailPanel").style.display = "block";
    }

    // Node positions the user has manually dragged, remembered across page visits/reloads (same pattern as
    // REFRESH_STORAGE_KEY below) - keyed by node id, storing raw Cytoscape model coordinates. Since the diagram is
    // always re-fit (scaled/panned) to the container after being laid out, coordinates remain a faithful relative
    // arrangement even if the browser window size differs from when they were saved.
    var NODE_POSITIONS_STORAGE_KEY = "topoNodePositions";

    function loadSavedPositions() {
        try {
            return JSON.parse(localStorage.getItem(NODE_POSITIONS_STORAGE_KEY)) || {};
        } catch (e) {
            return {};
        }
    }

    function saveAllPositions() {
        if (!cy) { return; }
        var saved = loadSavedPositions();
        cy.nodes("[kind!='host']").forEach(function (node) {
            var pos = node.position();
            saved[node.id()] = { x: pos.x, y: pos.y };
        });
        try {
            localStorage.setItem(NODE_POSITIONS_STORAGE_KEY, JSON.stringify(saved));
        } catch (e) {
            // Storage full/unavailable - the rearrangement simply won't be remembered next time.
        }
    }

    // Default first-time layout (used only for a node id that has no saved position yet): Master Server top-center,
    // Database top-right, Monitor Server(s) top-left, and Data Movers along the bottom. A Monitor sharing a host
    // with a Data Mover or the Master is grouped with it instead (same host = same box), so in practice this only
    // places a Monitor top-left when it runs on its own dedicated host - i.e. it isn't "external" to anything else.
    // Data Movers sharing the same TransferGroup are placed next to each other along the bottom row.
    function computeDefaultPositions(nodes) {
        var childrenByHost = {};
        nodes.forEach(function (n) {
            if (n.data.kind && n.data.kind !== "host" && n.data.parent) {
                (childrenByHost[n.data.parent] = childrenByHost[n.data.parent] || []).push(n);
            }
        });
        var BUCKET_RANK = { master: 0, database: 1, mover: 2, monitor: 3 };
        var BUCKET_NAME = { master: "top-center", database: "top-right", mover: "bottom", monitor: "top-left" };
        var hostsByBucket = { "top-left": [], "top-center": [], "top-right": [], bottom: [] };
        Object.keys(childrenByHost).forEach(function (hostId) {
            var bestKind = "monitor";
            var bestRank = 99;
            childrenByHost[hostId].forEach(function (n) {
                var rank = BUCKET_RANK[n.data.kind];
                if (rank !== undefined && rank < bestRank) { bestRank = rank; bestKind = n.data.kind; }
            });
            hostsByBucket[BUCKET_NAME[bestKind]].push(hostId);
        });
        hostsByBucket.bottom.sort(function (a, b) {
            function groupOf(hostId) {
                var mover = childrenByHost[hostId].filter(function (n) { return n.data.kind === "mover"; })[0];
                return (mover && mover.data.transferGroup) || "";
            }
            return groupOf(a).localeCompare(groupOf(b)) || a.localeCompare(b);
        });

        var BUCKET_BASE_X = { "top-left": 60, "top-center": 640, "top-right": 1200, bottom: 60 };
        var BUCKET_Y = { "top-left": 80, "top-center": 80, "top-right": 80, bottom: 440 };
        var HOST_SPACING_X = 260;
        var CHILD_SPACING_Y = 90;

        var positions = {};
        Object.keys(hostsByBucket).forEach(function (bucket) {
            hostsByBucket[bucket].forEach(function (hostId, hostIdx) {
                var x = BUCKET_BASE_X[bucket] + hostIdx * HOST_SPACING_X;
                var y = BUCKET_Y[bucket];
                childrenByHost[hostId].forEach(function (n, childIdx) {
                    positions[n.data.id] = { x: x, y: y + childIdx * CHILD_SPACING_Y };
                });
            });
        });
        return positions;
    }

    function initCy() {
        cy = cytoscape({
            container: document.getElementById("topoCy"),
            style: buildStyle(),
            layout: { name: "grid" },
            wheelSensitivity: 0.2
        });
        cy.on("tap", "node[kind!='host']", function (evt) { showDetail(evt.target); });
        // Persist manual rearrangement (dragging a node, or a whole host box which drags its children with it) so
        // it survives navigating away and back, or reloading the page.
        cy.on("dragfree", "node", function () { saveAllPositions(); });
    }

    function elementIdSignature(elements) {
        // Used to detect whether the topology's shape (which nodes/edges exist) changed between
        // refreshes, as opposed to just data (status/labels) changing on the same nodes/edges.
        return elements.map(function (el) { return el.data.id; }).sort().join("|");
    }

    // See the "fit: true" -> fitTopRow() switch below: which host boxes belong to the "bottom" (Data Mover) row
    // is decided the same way as in computeDefaultPositions() - any host that has a Data Mover child is a Data
    // Mover host, regardless of whether a Monitor also happens to share that same host.
    var topoFittedAll = false;

    function computeBottomHostIds() {
        var ids = {};
        cy.nodes("[kind='mover']").forEach(function (n) {
            var parent = n.data("parent");
            if (parent) { ids[parent] = true; }
        });
        return ids;
    }

    function fitTopRow() {
        var bottomHostIds = computeBottomHostIds();
        var topRow = cy.nodes().filter(function (n) {
            var hostId = n.data("kind") === "host" ? n.id() : n.data("parent");
            return !(hostId && bottomHostIds[hostId]);
        });
        if (topRow.length && topRow.length < cy.nodes().length) {
            cy.fit(topRow, 40);
            // Don't let a lone Master box (few/no other top-row elements) zoom in ridiculously far either.
            if (cy.zoom() > 1.3) { cy.zoom(1.3); cy.center(topRow); }
        } else {
            cy.fit(cy.elements(), 30);
        }
    }

    function fitAll() {
        cy.fit(cy.elements(), 30);
    }

    function render(data) {
        var hasAny = data && (data.master || (data.movers && data.movers.length));
        document.getElementById("topoEmptyState").style.display = hasAny ? "none" : "flex";
        if (!hasAny) { return; }
        if (!cy) { initCy(); }
        cy.style(buildStyle());

        var elements = buildElements(data);
        var signature = elementIdSignature(elements);
        var sameShape = cy.scratch("_topoSignature") === signature && cy.elements().length > 0;

        if (sameShape) {
            // Same components/hosts as last time: update each element's data/classes in place so
            // positions are preserved - avoids reshuffling the force-directed layout (and the
            // resulting crossed/overlapping edges) on every periodic auto-refresh.
            elements.forEach(function (el) {
                var ele = cy.getElementById(el.data.id);
                if (ele.length) { ele.data(el.data); }
            });
        } else {
            var savedPositions = loadSavedPositions();
            var defaultPositions = computeDefaultPositions(elements.filter(function (el) { return !el.data.source; }));
            elements.forEach(function (el) {
                if (el.data.source) { return; } // edge, not a node
                var pos = savedPositions[el.data.id] || defaultPositions[el.data.id];
                if (pos) { el.position = { x: pos.x, y: pos.y }; }
            });
            cy.elements().remove();
            cy.add(elements);
            cy.layout({ name: "preset" }).run();
            // Was "fit: true" (fit the *whole* diagram into the viewport) - but with lots of Data Movers that
            // squeezes the Master/Monitor/Database boxes down to an unreadably tiny size just so all the movers
            // fit too. Zoom to the top row instead (see fitTopRow()) so those boxes stay legible on first paint;
            // the movers laid out below simply extend past the bottom of the viewport, reachable by dragging/
            // scrolling (standard Cytoscape panning, already enabled) - "Fit all" in the toolbar undoes this.
            topoFittedAll = false;
            fitTopRow();
        }
        cy.scratch("_topoSignature", signature);

        var moverCount = (data.movers || []).length;
        var upCount = (data.movers || []).filter(function (m) { return m.up; }).length;
        document.getElementById("topoSubtitle").textContent = moverCount
            ? (upCount + "/" + moverCount + " Data Movers up")
            : "No Data Movers registered";
    }

    function refresh(manual) {
        fetch("/do/monitoring/topology/data", { cache: "no-store" })
            .then(function (resp) {
                if (!resp.ok) { throw new Error("HTTP " + resp.status); }
                // A logged-out/expired session is served the HTML login page (still HTTP 200) instead of JSON, so
                // detect that here and surface a clear "please reload/login" message instead of retrying forever.
                var contentType = resp.headers.get("content-type") || "";
                if (contentType.indexOf("json") === -1) {
                    throw new Error("SESSION_EXPIRED");
                }
                return resp.json();
            })
            .then(function (data) {
                render(data);
            })
            .catch(function (err) {
                console.error("Failed to load system topology", err);
                var sessionExpired = err && err.message === "SESSION_EXPIRED";
                if (sessionExpired && refreshTimer) {
                    // No point polling further until the user logs back in - stop burning requests.
                    clearInterval(refreshTimer);
                    refreshTimer = null;
                }
                document.getElementById("topoSubtitle").textContent = sessionExpired
                    ? "Session expired - please reload the page to log in again"
                    : "Failed to load topology - will retry";
            });
    }

    document.getElementById("topoRefreshBtn").addEventListener("click", function () { refresh(true); });
    document.getElementById("topoFitAllBtn").addEventListener("click", function () {
        if (!cy) { return; }
        topoFittedAll = !topoFittedAll;
        var icon = this.querySelector("i");
        if (topoFittedAll) {
            fitAll();
            icon.className = "bi bi-arrows-angle-contract";
            this.title = "Zoom back to Master/Monitor/Database";
        } else {
            fitTopRow();
            icon.className = "bi bi-arrows-angle-expand";
            this.title = "Zoom out to see all Data Movers";
        }
    });
    document.getElementById("topoFullscreenBtn").addEventListener("click", function () {
        var el = document.getElementById("topoContainer");
        if (!document.fullscreenElement) {
            (el.requestFullscreen || el.webkitRequestFullscreen || function () {}).call(el);
        } else {
            (document.exitFullscreen || document.webkitExitFullscreen || function () {}).call(document);
        }
    });
    document.addEventListener("fullscreenchange", function () {
        if (cy) { cy.resize(); topoFittedAll ? fitAll() : fitTopRow(); }
    });

    new MutationObserver(function () { if (cy) { cy.style(buildStyle()); } }).observe(document.documentElement, {
        attributes: true, attributeFilter: ["data-bs-theme"]
    });

    // Auto-refresh interval: purely client-side (no page reload, just re-fetching the JSON feed), remembered in
    // localStorage like other per-user UI preferences in this app (e.g. "monHeaderCols" on /do/monitoring).
    var REFRESH_STORAGE_KEY = "topoRefreshSeconds";
    var DEFAULT_REFRESH_SECONDS = 30;

    function applyRefreshInterval(seconds) {
        if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null; }
        if (seconds > 0) { refreshTimer = setInterval(function () { refresh(false); }, seconds * 1000); }
        document.querySelectorAll(".topo-refresh-pill").forEach(function (pill) {
            pill.classList.toggle("active", parseInt(pill.dataset.value, 10) === seconds);
        });
        localStorage.setItem(REFRESH_STORAGE_KEY, String(seconds));
    }

    document.querySelectorAll(".topo-refresh-pill").forEach(function (pill) {
        pill.addEventListener("click", function (evt) {
            evt.preventDefault();
            applyRefreshInterval(parseInt(pill.dataset.value, 10));
        });
    });

    var savedRefreshSeconds = parseInt(localStorage.getItem(REFRESH_STORAGE_KEY), 10);
    if (isNaN(savedRefreshSeconds)) { savedRefreshSeconds = DEFAULT_REFRESH_SECONDS; }

    refresh(false);
    applyRefreshInterval(savedRefreshSeconds);
    window.addEventListener("beforeunload", function () { if (refreshTimer) { clearInterval(refreshTimer); } });
}());
</script>
