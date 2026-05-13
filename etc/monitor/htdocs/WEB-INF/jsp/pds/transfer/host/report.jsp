<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>

<link rel="stylesheet" href="/openlayer/ol.css"/>
<script src="/openlayer/ol.js"></script>

<style>
.report-card-hdr { background: #2d2d2d; border-bottom: 1px solid #444; color: #fff; }
.report-card-hdr-sub { color: rgba(255,255,255,0.5); }
.report-card-hdr-btn { color: rgba(255,255,255,0.5); border-color: #555; }
.report-pre-spinner { color: rgba(255,255,255,0.5); }
.report-pre {
    background: #1e1e1e; color: #d4d4d4; margin: 0; padding: 1rem 1.25rem;
    font-size: 0.78rem; line-height: 1.6;
    border-radius: 0 0 4px 4px;
    white-space: pre-wrap; word-break: break-all;
}
.report-pre font[color="red"]   { color: #f88; }
.report-pre font[color="green"] { color: #8f8; }
.report-pre a { color: #79b8ff; text-decoration: none; }
.report-pre a:hover { text-decoration: underline; }
/* Light theme overrides */
[data-bs-theme=light] .report-card-hdr { background: #f0f2f4; border-bottom-color: #d0d7de; color: #24292f; }
[data-bs-theme=light] .report-card-hdr-sub { color: #57606a; }
[data-bs-theme=light] .report-card-hdr-btn { color: #57606a; border-color: #d0d7de; }
[data-bs-theme=light] .report-card-hdr-btn:hover { color: #fff; }
[data-bs-theme=light] .report-pre-spinner { color: #57606a; }
[data-bs-theme=light] .report-pre { background: #f6f8fa; color: #24292f; }
[data-bs-theme=light] .report-pre font[color="red"]   { color: #cf222e; }
[data-bs-theme=light] .report-pre font[color="green"] { color: #1a7f37; }
[data-bs-theme=light] .report-pre a { color: #0969da; }

/* -- MTR chart ------------------------------------------- */
.mc-panel {
    padding: 0.75rem 1rem 1rem; background: #1e1e1e;
    border-radius: 0 0 4px 4px; overflow-x: auto;
}
.mc-wrap { min-width: 500px; }

/* -- MTR map -------------------------------------------- */
.mm-panel {
    background: #1e1e1e; border-radius: 0 0 4px 4px; overflow: hidden;
    position: relative;
}
.mm-map { height: 480px; width: 100%; }
.mm-legend {
    position: absolute; bottom: 14px; left: 14px; z-index: 1000;
    background: rgba(20,20,20,0.85); border: 1px solid rgba(255,255,255,0.1);
    border-radius: 8px; padding: 0.5rem 0.75rem; font-size: 0.72rem; color: #ccc;
    backdrop-filter: blur(4px);
}
.mm-legend-row { display: flex; align-items: center; gap: 0.4rem; margin: 0.2rem 0; }
.mm-dot {
    width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0;
    border: 1.5px solid rgba(255,255,255,0.35);
}
.mm-loading {
    position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
    background: rgba(20,20,20,0.75); z-index: 2000; font-size: 0.85rem; color: #adb5bd;
    border-radius: 0 0 4px 4px;
}
[data-bs-theme=light] .mm-panel { background: #f6f8fa; }
[data-bs-theme=light] .mm-legend {
    background: rgba(250,250,252,0.9); border-color: rgba(0,0,0,0.1); color: #444;
}
[data-bs-theme=light] .mm-loading { background: rgba(246,248,250,0.8); color: #57606a; }
.mc-infobar {
    display: flex; align-items: center; flex-wrap: wrap; gap: 0.4rem;
    padding: 0.4rem 0.5rem; margin-bottom: 0.5rem;
    background: rgba(255,255,255,0.05); border-radius: 6px;
    font-size: 0.8rem; color: #adb5bd;
}
.mc-src, .mc-dst { color: #e8e8e8; font-weight: 500; }
.mc-arr, .mc-hops, .mc-muted { color: #6c757d; }
.mc-hops { font-size: 0.72rem; margin-left: auto; }
.mc-nmapbar {
    display: flex; align-items: center; flex-wrap: wrap; gap: 0.3rem;
    padding: 0.35rem 0.5rem; margin-bottom: 0.5rem;
    background: rgba(255,255,255,0.04); border-radius: 6px;
    font-size: 0.78rem; color: #adb5bd;
}
.mc-row {
    display: grid;
    grid-template-columns: 1.8rem 5rem 1fr 4.5rem 2fr 5rem 2.5rem;
    align-items: center; gap: 0.5rem;
    padding: 0.3rem 0.5rem; border-radius: 4px;
}
.mc-row:hover:not(.mc-hdr) { background: rgba(255,255,255,0.04); }
.mc-hdr {
    font-size: 0.71rem; color: #6c757d; text-transform: uppercase;
    letter-spacing: 0.04em; border-bottom: 1px solid #333;
    margin-bottom: 0.15rem; padding-bottom: 0.4rem;
}
.mc-nd { opacity: 0.45; }
.mc-c-hop { text-align: right; color: #6c757d; font-variant-numeric: tabular-nums; font-size: 0.75rem; }
.mc-c-as  { }
.mc-c-host { font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 0.8rem; }
.mc-c-loss { text-align: center; }
.mc-c-lat  { }
.mc-c-sd   { text-align: center; font-size: 0.75rem; font-variant-numeric: tabular-nums; }
.mc-c-pkt  { text-align: right; color: #6c757d; font-variant-numeric: tabular-nums; font-size: 0.75rem; }
.mc-as {
    display: inline-block; font-size: 0.66rem; padding: 0.05rem 0.3rem;
    background: rgba(255,255,255,0.08); border-radius: 3px; color: #adb5bd;
    font-variant-numeric: tabular-nums; max-width: 4.5rem;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.mc-badge {
    display: inline-block; font-size: 0.7rem; padding: 0.1rem 0.45rem;
    border-radius: 10px; font-weight: 600; font-variant-numeric: tabular-nums;
}
.mc-ok   { color: #3fb950; } .mc-warn { color: #d29922; } .mc-bad { color: #f85149; }
.mc-badge.mc-ok   { background: rgba(63,185,80,0.15);  color: #3fb950; }
.mc-badge.mc-warn { background: rgba(210,153,34,0.15); color: #d29922; }
.mc-badge.mc-bad  { background: rgba(248,81,73,0.15);  color: #f85149; }
.mc-track {
    position: relative; height: 6px; border-radius: 3px;
    background: rgba(255,255,255,0.08); margin-bottom: 3px; overflow: visible;
}
.mc-range {
    position: absolute; top: 0; height: 100%; border-radius: 3px;
    min-width: 3px; opacity: 0.8;
}
.mc-range.mc-ok   { background: #3fb950; }
.mc-range.mc-warn { background: #d29922; }
.mc-range.mc-bad  { background: #f85149; }
.mc-pin {
    position: absolute; top: -2px; width: 2px; height: 10px;
    background: #fff; border-radius: 1px; transform: translateX(-50%); opacity: 0.9;
}
.mc-lnums {
    display: flex; justify-content: space-between;
    font-size: 0.66rem; font-variant-numeric: tabular-nums; color: #6c757d;
}
.mc-lavg { font-weight: 600; }
/* Light theme */
[data-bs-theme=light] .mc-panel  { background: #f6f8fa; }
[data-bs-theme=light] .mc-infobar { background: rgba(0,0,0,0.04); color: #57606a; }
[data-bs-theme=light] .mc-src, [data-bs-theme=light] .mc-dst { color: #24292f; }
[data-bs-theme=light] .mc-arr, [data-bs-theme=light] .mc-hops, [data-bs-theme=light] .mc-muted { color: #8c959f; }
[data-bs-theme=light] .mc-nmapbar { background: rgba(0,0,0,0.03); color: #57606a; }
[data-bs-theme=light] .mc-row:hover:not(.mc-hdr) { background: rgba(0,0,0,0.04); }
[data-bs-theme=light] .mc-hdr { color: #8c959f; border-bottom-color: #d0d7de; }
[data-bs-theme=light] .mc-c-hop, [data-bs-theme=light] .mc-c-pkt { color: #8c959f; }
[data-bs-theme=light] .mc-as { background: rgba(0,0,0,0.06); color: #57606a; }
[data-bs-theme=light] .mc-ok   { color: #1a7f37; } [data-bs-theme=light] .mc-warn { color: #9a6700; } [data-bs-theme=light] .mc-bad { color: #cf222e; }
[data-bs-theme=light] .mc-badge.mc-ok   { background: rgba(26,127,55,0.12);  color: #1a7f37; }
[data-bs-theme=light] .mc-badge.mc-warn { background: rgba(154,103,0,0.12);  color: #9a6700; }
[data-bs-theme=light] .mc-badge.mc-bad  { background: rgba(207,34,46,0.12);  color: #cf222e; }
[data-bs-theme=light] .mc-track  { background: rgba(0,0,0,0.08); }
[data-bs-theme=light] .mc-range.mc-ok   { background: #1a7f37; }
[data-bs-theme=light] .mc-range.mc-warn { background: #9a6700; }
[data-bs-theme=light] .mc-range.mc-bad  { background: #cf222e; }
[data-bs-theme=light] .mc-pin  { background: #24292f; }
[data-bs-theme=light] .mc-lnums { color: #8c959f; }
</style>

<div class="card shadow-sm mt-2">
    <div class="card-header d-flex align-items-center justify-content-between py-2 px-3 report-card-hdr">
        <span class="fw-semibold" style="font-size:0.875rem;">
            <i class="bi bi-terminal-fill me-2 text-success"></i>
            <c:choose>
                <c:when test="${not empty proxy}">
                    Network Report: <c:out value="${host.nickName}" />
                    <span class="fw-normal report-card-hdr-sub"> via <c:out value="${proxy.nickName}" /></span>
                </c:when>
                <c:otherwise>
                    Network Report: <c:out value="${host.nickName}" />
                </c:otherwise>
            </c:choose>
        </span>
        <div class="d-flex gap-2 align-items-center">
            <div id="viewToggleGroup" class="btn-group btn-group-sm" role="group"
                 aria-label="View mode" style="display:none!important; font-size:0.75rem;">
                <button id="btnViewRaw" type="button"
                        class="btn btn-outline-secondary py-0 px-2 active"
                        onclick="_setView('raw')" title="Show raw text">
                    <i class="bi bi-code-slash"></i> Raw
                </button>
                <button id="btnViewChart" type="button"
                        class="btn btn-outline-secondary py-0 px-2"
                        onclick="_setView('chart')" title="Show chart"
                        style="display:none;">
                    <i class="bi bi-bar-chart-line"></i> Chart
                </button>
                <button id="btnViewMap" type="button"
                        class="btn btn-outline-secondary py-0 px-2"
                        onclick="_setView('map')" title="Show on map"
                        style="display:none;">
                    <i class="bi bi-geo-alt"></i> Map
                </button>
            </div>
            <button id="reportRefreshBtn"
                    class="btn btn-sm btn-outline-secondary py-0 px-2 report-card-hdr-btn"
                    onclick="fetchReport()" title="Refresh report" style="font-size:0.75rem;">
                <i class="bi bi-arrow-clockwise"></i> Refresh
            </button>
            <button id="reportCopyBtn"
                    class="btn btn-sm btn-outline-secondary py-0 px-2 report-card-hdr-btn"
                    onclick="copyReport(this)" title="Copy to clipboard" style="font-size:0.75rem;" disabled>
                <i class="bi bi-clipboard"></i> Copy
            </button>
        </div>
    </div>
    <div class="card-body p-0">
        <pre id="reportPre" class="report-pre">
            <span id="reportSpinner" class="report-pre-spinner" style="font-style:italic;">
                <i class="bi bi-hourglass-split me-1"></i> Generating report, please wait&hellip;
            </span>
        </pre>
        <div id="mtrChartPanel" class="mc-panel" style="display:none;"></div>
        <div id="mtrMapPanel" class="mm-panel" style="display:none;">
            <div id="mtrMap" class="mm-map"></div>
            <div class="mm-legend" id="mmLegend">
                <div class="mm-legend-row"><div class="mm-dot" style="background:#3fb950;"></div> &lt;50 ms</div>
                <div class="mm-legend-row"><div class="mm-dot" style="background:#d29922;"></div> 50&ndash;150 ms</div>
                <div class="mm-legend-row"><div class="mm-dot" style="background:#f85149;"></div> &gt;150 ms</div>
                <div class="mm-legend-row"><div class="mm-dot" style="background:#6c757d; border-style:dashed;"></div> No response</div>
            </div>
            <div class="mm-loading" id="mmLoading" style="display:none;">
                <i class="bi bi-hourglass-split me-2"></i> Resolving coordinates&hellip;
            </div>
        </div>
    </div>
</div>

<script>
/* -- MTR / Nmap chart renderer ------------------------------ */
(function() {
    var _ready = false, _showing = false;
    var _view = 'raw', _chartReady = false, _mapReady = false;
    var _olMap = null, _olInited = false, _routeExtent = null, _mapFitted = false;
    /* Host's manually-configured coordinates — used as fallback for the last MTR hop
       when the GeoIP database has no entry for the destination IP. */
    var _hostFallback = <c:choose><c:when test="${not empty host.latitude and not empty host.longitude}">{ hostname: '<c:out value="${host.host}"/>', lat: ${host.latitude}, lon: ${host.longitude}, geo: '<c:out value="${host.nickName}"/>' }</c:when><c:otherwise>null</c:otherwise></c:choose>;

    function _esc(s) {
        return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }
    function _strip(html) {
        var d = document.createElement('div'); d.innerHTML = html;
        return d.textContent || d.innerText || '';
    }

    function _parseMtr(text) {
        var lines = text.split('\n'), hIdx = -1, source = null;
        for (var i = 0; i < lines.length; i++) {
            if (/Loss%\s+Snt\s+Last\s+Avg\s+Best\s+Wrst\s+StDev/i.test(lines[i])) {
                var hm = lines[i].match(/HOST:\s*(.+?)\s{3,}/);
                if (hm) source = hm[1].trim();
                hIdx = i; break;
            }
        }
        if (hIdx < 0) return null;
        var hops = [];
        for (var i = hIdx + 1; i < lines.length; i++) {
            var m = lines[i].match(
                /^\s*(\d+)\.\s+(\S+)\s+(\S+)\s+([\d.]+)%?\s+(\d+)\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)/);
            if (!m) continue;
            hops.push({ hop: +m[1], as: m[2], host: m[3],
                loss: +m[4], snt: +m[5], last: +m[6],
                avg: +m[7], best: +m[8], wrst: +m[9], stdev: +m[10],
                noData: m[3] === '???' });
        }
        return hops.length ? { source: source, hops: hops } : null;
    }

    function _parseNmap(text) {
        if (!/Starting Nmap/i.test(text)) return null;
        var r = {};
        var sm = text.match(/Nmap scan report for (.+)/); if (sm) r.target = sm[1].trim();
        var hm = text.match(/Host is (\w+)\s*\(([^)]+)\)/);
        if (hm) { r.status = hm[1]; r.latency = hm[2]; }
        var ports = [], inP = false;
        text.split('\n').forEach(function(ln) {
            ln = ln.trim();
            if (/^PORT\s+STATE\s+SERVICE/i.test(ln)) { inP = true; return; }
            if (inP && ln) {
                var pm = ln.match(/^(\d+\/\w+)\s+(\w+)\s+(\S+)/);
                if (pm) ports.push({ port: pm[1], state: pm[2], svc: pm[3] }); else inP = false;
            }
        });
        r.ports = ports;
        var dm = text.match(/Nmap done:.+?in ([\d.]+) seconds/); if (dm) r.dur = dm[1] + 's';
        return r;
    }

    function _cls(loss)       { return loss === 0 ? 'mc-ok' : loss < 10 ? 'mc-warn' : 'mc-bad'; }
    function _latCls(avg)     { return avg < 50   ? 'mc-ok' : avg < 150  ? 'mc-warn' : 'mc-bad'; }
    function _sdCls(sd, avg)  { var cv = avg ? sd / avg : 0; return cv < 0.15 ? 'mc-ok' : cv < 0.4 ? 'mc-warn' : 'mc-bad'; }

    function _build(mtr, nmap) {
        var maxW = 1;
        mtr.hops.forEach(function(h) { if (!h.noData && h.wrst > maxW) maxW = h.wrst; });
        var o = '<div class="mc-wrap">';

        /* Info bar */
        o += '<div class="mc-infobar">';
        if (mtr.source) o += '<span class="mc-src"><i class="bi bi-laptop me-1"></i>' + _esc(mtr.source) + '</span>'
                           + '<i class="bi bi-arrow-right mx-2 mc-arr"></i>';
        if (nmap && nmap.target) o += '<span class="mc-dst"><i class="bi bi-server me-1"></i>' + _esc(nmap.target) + '</span>';
        o += '<span class="mc-hops">' + mtr.hops.length + ' hops</span></div>';

        /* Nmap summary bar */
        if (nmap && (nmap.status || (nmap.ports && nmap.ports.length))) {
            var sc = nmap.status === 'up' ? 'mc-ok' : 'mc-bad';
            o += '<div class="mc-nmapbar"><i class="bi bi-radar me-1"></i><strong>Nmap</strong>';
            if (nmap.status)  o += '&nbsp;<span class="mc-badge ' + sc + '">' + _esc(nmap.status) + '</span>';
            if (nmap.latency) o += '&nbsp;<span class="mc-muted">(' + _esc(nmap.latency) + ')</span>';
            (nmap.ports || []).forEach(function(p) {
                var pc = p.state === 'open' ? 'mc-ok' : 'mc-bad';
                o += '&nbsp;<span class="mc-badge ' + pc + '">' + _esc(p.port) + ' ' + _esc(p.svc) + '</span>';
            });
            if (nmap.dur) o += '&nbsp;<span class="mc-muted">' + _esc(nmap.dur) + '</span>';
            o += '</div>';
        }

        /* Header row */
        o += '<div class="mc-row mc-hdr">'
           + '<div class="mc-c-hop">#</div>'
           + '<div class="mc-c-as">AS</div>'
           + '<div class="mc-c-host">Host</div>'
           + '<div class="mc-c-loss">Loss</div>'
           + '<div class="mc-c-lat">Latency &nbsp;<span class="mc-muted">best \u00b7 avg \u00b7 worst (ms)</span></div>'
           + '<div class="mc-c-sd">&plusmn;StDev</div>'
           + '<div class="mc-c-pkt">Pkts</div>'
           + '</div>';

        /* Hop rows */
        mtr.hops.forEach(function(h) {
            var nd = h.noData;
            var tip = 'Hop ' + h.hop + (nd ? ' \u2014 no response' : ': loss=' + h.loss + '% last=' + h.last
                + 'ms avg=' + h.avg + 'ms best=' + h.best + 'ms worst=' + h.wrst + 'ms stdev=\u00b1' + h.stdev + 'ms');
            o += '<div class="mc-row' + (nd ? ' mc-nd' : '') + '" title="' + _esc(tip) + '">';
            o += '<div class="mc-c-hop">' + h.hop + '</div>';
            o += '<div class="mc-c-as"><span class="mc-as">' + _esc(h.as === 'AS???' ? '?' : h.as.replace(/^AS/, '')) + '</span></div>';
            o += '<div class="mc-c-host">' + (nd ? '<span class="mc-muted">\u2014</span>' : _esc(h.host)) + '</div>';

            if (nd) {
                o += '<div class="mc-c-loss"><span class="mc-badge mc-bad">100%</span></div>'
                   + '<div class="mc-c-lat"><span class="mc-muted" style="font-size:0.7em;">no response</span></div>'
                   + '<div class="mc-c-sd mc-muted">\u2014</div>';
            } else {
                var lossStr = h.loss === 0 ? '0%' : h.loss.toFixed(1) + '%';
                o += '<div class="mc-c-loss"><span class="mc-badge ' + _cls(h.loss) + '">' + lossStr + '</span></div>';

                var bPct = (h.best / maxW * 100).toFixed(2);
                var wPct = Math.max((h.wrst - h.best) / maxW * 100, 0.3).toFixed(2);
                var aPct = (h.avg  / maxW * 100).toFixed(2);
                var lc   = _latCls(h.avg);
                o += '<div class="mc-c-lat">'
                   + '<div class="mc-track">'
                   + '<div class="mc-range ' + lc + '" style="left:' + bPct + '%;width:' + wPct + '%"></div>'
                   + '<div class="mc-pin" style="left:' + aPct + '%"></div>'
                   + '</div>'
                   + '<div class="mc-lnums">'
                   + '<span class="mc-muted">' + h.best.toFixed(1) + '</span>'
                   + '<span class="mc-lavg ' + lc + '">' + h.avg.toFixed(1) + '</span>'
                   + '<span class="mc-muted">' + h.wrst.toFixed(1) + '</span>'
                   + '</div></div>';

                o += '<div class="mc-c-sd"><span class="' + _sdCls(h.stdev, h.avg) + '">\u00b1' + h.stdev.toFixed(1) + '</span></div>';
            }
            o += '<div class="mc-c-pkt">' + h.snt + '</div></div>';
        });

        return o + '</div>';
    }

    /* ---- View state machine --------------------------------- */
    window._setView = function(v) {
        _view = v;
        var pre    = document.getElementById('reportPre');
        var chart  = document.getElementById('mtrChartPanel');
        var mapP   = document.getElementById('mtrMapPanel');

        if (pre)   pre.style.display   = (v === 'raw')   ? '' : 'none';
        if (chart) chart.style.display = (v === 'chart') ? '' : 'none';
        if (mapP)  mapP.style.display  = (v === 'map')   ? '' : 'none';

        ['Raw','Chart','Map'].forEach(function(name) {
            var btn = document.getElementById('btnView' + name);
            if (btn) btn.classList.toggle('active', v === name.toLowerCase());
        });

        if (v === 'map') {
            setTimeout(function() {
                if (_olMap) {
                    _olMap.updateSize();
                    if (!_mapFitted && _routeExtent && isFinite(_routeExtent[0])) {
                        _olMap.getView().fit(_routeExtent, { padding: [60, 60, 60, 60], maxZoom: 8, duration: 400 });
                        _mapFitted = true;
                    }
                }
            }, 50);
        }
    };

    /* kept for backward compat */
    window._setMtrView = function(showChart) {
        window._setView(showChart ? 'chart' : 'raw');
    };
    window._mtrWasShowing = function() { return _view === 'chart'; };

    function _showToggleGroup() {
        var g = document.getElementById('viewToggleGroup');
        if (g) g.style.cssText = 'font-size:0.75rem;';
    }

    window._mtrReset = function() {
        _chartReady = false; _mapReady = false; _view = 'raw';
        var g = document.getElementById('viewToggleGroup');
        if (g) g.style.cssText = 'display:none!important;';
        var btnChart = document.getElementById('btnViewChart');
        var btnMap   = document.getElementById('btnViewMap');
        if (btnChart) btnChart.style.display = 'none';
        if (btnMap)   btnMap.style.display   = 'none';
        var chart = document.getElementById('mtrChartPanel');
        if (chart) { chart.innerHTML = ''; chart.style.display = 'none'; }
        var mapP = document.getElementById('mtrMapPanel');
        if (mapP) { mapP.style.display = 'none'; }
        var pre = document.getElementById('reportPre');
        if (pre) pre.style.display = '';
        /* Destroy and recreate map on next load */
        if (_olMap) { try { _olMap.setTarget(null); } catch(e) {} _olMap = null; }
        _olInited = false; _routeExtent = null; _mapFitted = false;
    };

    /* ---- Chart entry point ---------------------------------- */
    window._tryMtrChart = function(html) {
        var plain;
        try { plain = _strip(html); } catch(e) { return; }
        var mtr = null, nmap = null;
        try { mtr  = _parseMtr(plain);  } catch(e) {}
        try { nmap = _parseNmap(plain); } catch(e) {}
        if (!mtr) return;
        var chart = document.getElementById('mtrChartPanel');
        if (!chart) return;
        try { chart.innerHTML = _build(mtr, nmap); } catch(e) { return; }
        _chartReady = true;
        var btnChart = document.getElementById('btnViewChart');
        if (btnChart) btnChart.style.display = '';
        _showToggleGroup();
        window._setView('chart');
        /* kick off map geo-resolution in background */
        _fetchHopCoords(mtr);
    };

    /* ---- Map ------------------------------------------------ */
    function _latColor(avg) {
        if (avg < 50)  return '#3fb950';
        if (avg < 150) return '#d29922';
        return '#f85149';
    }

    function _fetchHopCoords(mtr) {
        var ips = mtr.hops.filter(function(h) { return !h.noData; }).map(function(h) { return h.host; });
        if (!ips.length) return;
        var url = '/do/transfer/host/edit/geoIp?ips=' + encodeURIComponent(ips.join(','));
        fetch(url)
            .then(function(r) { return r.json(); })
            .then(function(data) {
                var geoMap = {};
                data.forEach(function(d) { if (d.lat != null && d.lon != null) geoMap[d.ip] = d; });
                /* If the last reachable hop has no GeoIP coordinates, fall back to the
                   host's manually-configured lat/lon (if set). */
                if (_hostFallback) {
                    var lastHop = null;
                    for (var i = mtr.hops.length - 1; i >= 0; i--) {
                        if (!mtr.hops[i].noData) { lastHop = mtr.hops[i]; break; }
                    }
                    if (lastHop && !geoMap[lastHop.host]) {
                        geoMap[lastHop.host] = { ip: lastHop.host, lat: _hostFallback.lat, lon: _hostFallback.lon, geo: _hostFallback.geo };
                    }
                }
                var resolved = mtr.hops.filter(function(h) { return !h.noData && geoMap[h.host]; });
                if (resolved.length >= 2) {
                    _buildMap(mtr, geoMap);
                    _mapReady = true;
                    var btnMap = document.getElementById('btnViewMap');
                    if (btnMap) btnMap.style.display = '';
                }
            })
            .catch(function() { /* silently skip map */ });
    }

    function _isDark() {
        return document.documentElement.getAttribute('data-bs-theme') === 'dark';
    }

    function _buildMap(mtr, geoMap) {
        if (!window.ol) return;
        var mapEl = document.getElementById('mtrMap');
        if (!mapEl) return;

        var dark = _isDark();
        var tileUrl = dark
            ? 'https://{a-d}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
            : 'https://{a-d}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
        var tileAttr = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>';

        var tileSource = new ol.source.XYZ({ url: tileUrl, attributions: tileAttr });
        var vectorSource = new ol.source.Vector();

        /* Build ordered list of resolved hops */
        var hopPoints = [];
        mtr.hops.forEach(function(h) {
            if (!h.noData && geoMap[h.host]) {
                var g = geoMap[h.host];
                hopPoints.push({ hop: h, geo: g, coord: ol.proj.fromLonLat([g.lon, g.lat]) });
            }
        });

        /* Route polyline */
        if (hopPoints.length >= 2) {
            var routeCoords = hopPoints.map(function(p) { return p.coord; });
            var routeFeature = new ol.Feature({
                geometry: new ol.geom.LineString(routeCoords)
            });
            routeFeature.setStyle(new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: dark ? 'rgba(121,184,255,0.55)' : 'rgba(9,105,218,0.45)',
                    width: 2.5,
                    lineDash: [6, 4]
                })
            }));
            vectorSource.addFeature(routeFeature);
        }

        /* Markers */
        var isFirst = true, isLast = false;
        hopPoints.forEach(function(p, idx) {
            isLast = (idx === hopPoints.length - 1);
            var h = p.hop;
            var color = _latColor(h.avg);
            var radius = isFirst || isLast ? 9 : (h.loss > 50 ? 8 : 6);
            var strokeW = isFirst || isLast ? 2.5 : 1.5;

            var feat = new ol.Feature({ geometry: new ol.geom.Point(p.coord) });
            feat.set('hopData', h);
            feat.set('geoData', p.geo);
            feat.set('isFirst', isFirst);
            feat.set('isLast', isLast);

            feat.setStyle(new ol.style.Style({
                image: new ol.style.Circle({
                    radius: radius,
                    fill: new ol.style.Fill({ color: color }),
                    stroke: new ol.style.Stroke({ color: '#fff', width: strokeW })
                }),
                text: (isFirst || isLast) ? new ol.style.Text({
                    text: isFirst ? 'SRC' : 'DST',
                    font: 'bold 9px sans-serif',
                    offsetY: -14,
                    fill: new ol.style.Fill({ color: dark ? '#e8e8e8' : '#24292f' }),
                    backgroundFill: new ol.style.Fill({ color: dark ? 'rgba(30,30,30,0.75)' : 'rgba(250,250,252,0.8)' }),
                    padding: [1, 3, 1, 3]
                }) : null
            }));
            vectorSource.addFeature(feat);
            isFirst = false;
        });

        var vectorLayer = new ol.layer.Vector({
            source: vectorSource,
            zIndex: 10
        });

        _olMap = new ol.Map({
            target: 'mtrMap',
            controls: ol.control.defaults.defaults({ rotate: false }),
            layers: [
                new ol.layer.Tile({ source: tileSource }),
                vectorLayer
            ],
            view: new ol.View({ center: ol.proj.fromLonLat([0, 30]), zoom: 3 })
        });
        _olInited = true;

        /* Save extent — fit is deferred until the panel is actually visible */
        var ext = vectorSource.getExtent();
        if (ext && isFinite(ext[0])) { _routeExtent = ext; }

        /* Hover cursor */
        _olMap.on('pointermove', function(evt) {
            var hit = _olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return !!f.get('hopData'); });
            _olMap.getTargetElement().style.cursor = hit ? 'pointer' : '';
        });

        /* Click popup (Bootstrap tooltip approach via temporary overlay) */
        var overlay = new ol.Overlay({
            element: document.createElement('div'),
            positioning: 'bottom-center',
            stopEvent: false,
            offset: [0, -12]
        });
        overlay.getElement().style.cssText = [
            'background:' + (dark ? 'rgba(20,20,20,0.92)' : 'rgba(250,250,252,0.95)'),
            'border:1px solid ' + (dark ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.12)'),
            'border-radius:8px', 'padding:0.45rem 0.65rem',
            'font-size:0.75rem', 'color:' + (dark ? '#ccc' : '#333'),
            'max-width:240px', 'pointer-events:none',
            'box-shadow:0 4px 16px rgba(0,0,0,0.4)',
            'backdrop-filter:blur(6px)', 'z-index:9999'
        ].join(';');
        _olMap.addOverlay(overlay);

        _olMap.on('click', function(evt) {
            var feat = _olMap.forEachFeatureAtPixel(evt.pixel, function(f) { return f.get('hopData') ? f : null; });
            if (!feat) { overlay.setPosition(undefined); return; }
            var h = feat.get('hopData');
            var g = feat.get('geoData');
            var lc = _latColor(h.avg);
            var rows = [
                '<strong style="color:' + lc + '">Hop ' + h.hop + '</strong>',
                '<span style="font-family:monospace">' + h.host + '</span>',
                h.as && h.as !== 'AS???' ? '<span style="opacity:0.7">' + h.as + '</span>' : '',
                g && g.geo ? '<span style="opacity:0.65">\uD83D\uDCCD ' + g.geo + '</span>' : '',
                'Avg: <strong style="color:' + lc + '">' + h.avg.toFixed(1) + ' ms</strong>'
                    + '  Best: ' + h.best.toFixed(1) + '  Worst: ' + h.wrst.toFixed(1),
                'Loss: <strong>' + (h.loss === 0 ? '0%' : h.loss.toFixed(1) + '%') + '</strong>'
                    + '  &plusmn;StDev: ' + h.stdev.toFixed(1) + ' ms'
            ].filter(Boolean).join('<br>');
            overlay.getElement().innerHTML = rows;
            overlay.setPosition(evt.coordinate);
        });
    }

})();

/* -- Report fetch / display --------------------------------- */
(function() {
    var dataUrl  = '<c:out value="${reportDataUrl}"/>';
    var cacheKey = 'hostReport_<c:out value="${host.name}"/>_<c:choose><c:when test="${not empty proxy}"><c:out value="${proxy.nickName}"/></c:when><c:otherwise>__direct__</c:otherwise></c:choose>';

    function showCached(html, isStale) {
        var pre = document.getElementById('reportPre');
        pre.innerHTML = html;
        if (isStale) {
            pre.innerHTML += '<div style="font-size:0.7rem;color:#888;margin-top:0.5rem;font-style:italic;">'
                + '<i class="bi bi-clock me-1"></i>Cached &mdash; click Refresh to update.</div>';
        }
        document.getElementById('reportCopyBtn').disabled = false;
        window._tryMtrChart && window._tryMtrChart(html);
    }

    function fetchReport() {
        var wasChart = window._mtrWasShowing ? window._mtrWasShowing() : false;
        window._mtrReset && window._mtrReset();
        var pre        = document.getElementById('reportPre');
        var copyBtn    = document.getElementById('reportCopyBtn');
        var refreshBtn = document.getElementById('reportRefreshBtn');
        pre.innerHTML = '<span class="report-pre-spinner" style="font-style:italic;">'
            + '<i class="bi bi-hourglass-split me-1"></i> Generating report, please wait\u2026</span>';
        copyBtn.disabled = true;
        refreshBtn.disabled = true;
        fetch(dataUrl)
            .then(function(r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.text(); })
            .then(function(text) {
                try { sessionStorage.setItem(cacheKey, text); } catch(e) {}
                pre.innerHTML = text;
                copyBtn.disabled = false;
                refreshBtn.disabled = false;
                window._tryMtrChart && window._tryMtrChart(text);
            })
            .catch(function(err) {
                var html = '<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>'
                    + 'Failed to load report: ' + err.message + '</span>';
                try { sessionStorage.setItem(cacheKey, html); } catch(e) {}
                pre.innerHTML = html;
                refreshBtn.disabled = false;
            });
    }

    window.fetchReport = fetchReport;

    window.copyReport = function(btn) {
        /* Always copy raw text (pre may be hidden when chart or map is shown) */
        var text = document.getElementById('reportPre').textContent;
        navigator.clipboard.writeText(text).then(function() {
            btn.innerHTML = '<i class="bi bi-check-lg"></i> Copied';
            setTimeout(function() { btn.innerHTML = '<i class="bi bi-clipboard"></i> Copy'; }, 1500);
        });
    };

    var cached = null;
    try { cached = sessionStorage.getItem(cacheKey); } catch(e) {}
    if (cached) { showCached(cached, true); } else { fetchReport(); }
})();
</script>
