<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>
<c:if test="${not empty moduleGuide}"><jsp:include page="${moduleGuide}"/></c:if>

<style>
/* -- Global date bar -- */
.go-hdr {
    background: #1a1a1a; border-bottom: 2px solid #333;
    padding: 0.4rem 1rem; position: sticky; top: 0; z-index: 2;
    display: flex; align-items: center; flex-wrap: wrap; gap: 0.6rem;
    font-size: 0.72rem; font-family: 'Consolas', 'Monaco', monospace;
}
/* Card header shared with report.jsp */
.report-card-hdr { background: #2d2d2d; color: #fff; }
.report-card-hdr-btn { color: rgba(255,255,255,0.5); border-color: #555; }
.output-loading { background: #1e1e1e; color: #6c757d; font-size: 0.8rem; }
.output-content { background: #1e1e1e; }
[data-bs-theme=light] .report-card-hdr { background: #f0f2f4; border-bottom-color: #d0d7de !important; color: #24292f; }
[data-bs-theme=light] .report-card-hdr-btn { color: #57606a; border-color: #d0d7de; }
[data-bs-theme=light] .report-card-hdr-btn:hover { color: #24292f; }
[data-bs-theme=light] .output-loading { background: #f6f8fa; }
[data-bs-theme=light] .output-content { background: #f6f8fa; }
.go-key { color: #555; text-transform: uppercase; letter-spacing: 0.06em; font-size: 0.64rem; }
.go-val { color: #b0b0b0; }
.go-ago { color: #555; }
.go-cnt { color: #6c757d; border-left: 1px solid #333; padding-left: 0.6rem; margin-left: 0.2rem; }

/* -- Per-path listing blocks -- */
.listing-block { border-left: 3px solid #444; border-bottom: 1px solid #181818; }
.listing-block:last-child { border-bottom: none; }
.listing-block.lb-ok    { border-left-color: #28a745; }
.listing-block.lb-err   { border-left-color: #dc3545; }
.listing-block.lb-mixed { border-left-color: #ffc107; }
.listing-block.lb-empty { border-left-color: #3a3a3a; }

/* Block header row */
.lb-hdr {
    background: #222; border-bottom: 1px solid #181818;
    padding: 0.28rem 0.75rem;
    display: flex; flex-wrap: wrap; align-items: center; gap: 0.45rem;
    font-size: 0.7rem; font-family: 'Consolas', 'Monaco', monospace;
    min-height: 1.8rem;
}
.lb-path  { color: #c8c8c8; flex: 1 1 0; min-width: 0;
            overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lb-pat   { color: #79b8ff; background: rgba(121,184,255,0.08);
            border-radius: 3px; padding: 0.05rem 0.4rem; flex-shrink: 0;
            max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lb-list  { color: #6c757d; flex-shrink: 0; }
.lb-mover { color: #4a4a5a; }
.lb-mover a { color: #6c757d; text-decoration: none; }
.lb-mover a:hover { color: #79b8ff; text-decoration: underline; }
.lb-stat  { display: flex; gap: 0.25rem; flex-shrink: 0; }
.lb-cnt   { font-size: 0.64rem; padding: 0.05rem 0.35rem; border-radius: 3px; font-weight: 600; }
.lb-cnt-ok  { color: #28a745; border: 1px solid rgba(40,167,69,0.35); }
.lb-cnt-err { color: #dc3545; border: 1px solid rgba(220,53,69,0.35); }
.lb-none  { padding: 0.28rem 0.75rem; color: #3a3a3a; font-size: 0.68rem;
            font-style: italic; font-family: 'Consolas','Monaco',monospace; }

/* File entries table */
.output-file-table { width: 100%; border-collapse: collapse;
    font-size: 0.71rem; font-family: 'Consolas', 'Monaco', monospace; }
.output-file-table tbody tr:hover td { background: rgba(255,255,255,0.03); }
.output-file-table td { padding: 0.1rem 0.55rem; vertical-align: middle;
    color: #d4d4d4; white-space: nowrap; }
.output-file-table td.c-perms { color: #6c757d; }
.output-file-table td.c-size  { color: #79b8ff; text-align: right; min-width: 4.5rem; }
.output-file-table td.c-date  { color: #6c757d; }
.output-file-table td.c-name a { color: #d4d4d4; text-decoration: none; }
.output-file-table td.c-name a:hover { color: #79b8ff; text-decoration: underline; }
.output-file-table td.c-target { color: #6c757d; max-width: 240px;
    overflow: hidden; text-overflow: ellipsis; }
.output-file-table td.c-status { max-width: 300px; }
.out-badge { display: inline-block; font-size: 0.62rem; padding: 0.1em 0.45em;
    border-radius: 3px; white-space: normal; line-height: 1.4; vertical-align: middle; }
.out-badge a { color: inherit; text-decoration: underline; opacity: 0.85; }
.out-badge a:hover { opacity: 1; }

/* Scrollable wrapper */
.structured-output { max-height: 75vh; overflow-y: auto; }

/* Fallback pre */
#outputContent pre {
    background: #1e1e1e; color: #d4d4d4; margin: 0; padding: 1rem 1.25rem;
    font-size: 0.78rem; line-height: 1.6; max-height: 75vh; overflow-y: auto;
    border-radius: 0 0 4px 4px; white-space: pre-wrap; word-break: break-all;
}
#outputContent pre font[color="red"]   { color: #f88; }
#outputContent pre font[color="green"] { color: #8f8; }
#outputContent pre font[color="blue"]  { color: #8af; }
#outputContent pre a { color: #79b8ff; text-decoration: none; }
#outputContent pre a:hover { text-decoration: underline; }
/* Raw entry rows (colour set via class, overridden per theme) */
.raw-ok  { color: #8f8;  font-size: 0.68rem; font-style: italic; }
.raw-err { color: #f88;  font-size: 0.68rem; font-style: italic; }
.raw-neu { color: #999;  font-size: 0.68rem; font-style: italic; }
/* Light theme overrides */
[data-bs-theme=light] .go-hdr { background: #f0f2f4; border-bottom-color: #d0d7de; }
[data-bs-theme=light] .go-key { color: #57606a; }
[data-bs-theme=light] .go-val { color: #24292f; }
[data-bs-theme=light] .go-ago { color: #57606a; }
[data-bs-theme=light] .go-cnt { border-left-color: #d0d7de; }
[data-bs-theme=light] .listing-block { border-bottom-color: #e6e8ea; }
[data-bs-theme=light] .listing-block.lb-ok    { border-left-color: #1a7f37; }
[data-bs-theme=light] .listing-block.lb-err   { border-left-color: #cf222e; }
[data-bs-theme=light] .listing-block.lb-mixed { border-left-color: #bf8700; }
[data-bs-theme=light] .listing-block.lb-empty { border-left-color: #d0d7de; }
[data-bs-theme=light] .lb-hdr { background: #f6f8fa; border-bottom-color: #e6e8ea; }
[data-bs-theme=light] .lb-path  { color: #24292f; }
[data-bs-theme=light] .lb-pat   { color: #0550ae; background: rgba(5,80,174,0.06); }
[data-bs-theme=light] .lb-list  { color: #57606a; }
[data-bs-theme=light] .lb-mover { color: #8c959f; }
[data-bs-theme=light] .lb-mover a { color: #57606a; }
[data-bs-theme=light] .lb-mover a:hover { color: #0969da; }
[data-bs-theme=light] .lb-cnt-ok  { color: #1a7f37; border-color: rgba(26,127,55,0.35); }
[data-bs-theme=light] .lb-cnt-err { color: #cf222e; border-color: rgba(207,34,46,0.35); }
[data-bs-theme=light] .lb-none  { color: #8c959f; }
[data-bs-theme=light] .output-file-table td { color: #24292f; }
[data-bs-theme=light] .output-file-table td.c-perms { color: #57606a; }
[data-bs-theme=light] .output-file-table td.c-size  { color: #0550ae; }
[data-bs-theme=light] .output-file-table td.c-date  { color: #57606a; }
[data-bs-theme=light] .output-file-table td.c-name a { color: #24292f; }
[data-bs-theme=light] .output-file-table td.c-name a:hover { color: #0969da; }
[data-bs-theme=light] .output-file-table td.c-target { color: #57606a; }
[data-bs-theme=light] .output-file-table tbody tr:hover td { background: rgba(0,0,0,0.03); }
[data-bs-theme=light] #outputContent pre { background: #f6f8fa; color: #24292f; }
[data-bs-theme=light] #outputContent pre font[color="red"]   { color: #cf222e; }
[data-bs-theme=light] #outputContent pre font[color="green"] { color: #1a7f37; }
[data-bs-theme=light] #outputContent pre font[color="blue"]  { color: #0550ae; }
[data-bs-theme=light] #outputContent pre a { color: #0969da; }
[data-bs-theme=light] .raw-ok  { color: #1a7f37; }
[data-bs-theme=light] .raw-err { color: #cf222e; }
[data-bs-theme=light] .raw-neu { color: #57606a; }
/* -- Progress terminal (shared with data.jsp compact summary) -- */
.progress-terminal { border-radius: 4px; overflow: hidden; margin-top: 2px; }
.progress-terminal-hdr {
    background: #2d2d2d; color: #ccc; padding: 0.25rem 0.75rem;
    font-size: 0.75rem; border-bottom: 1px solid #444;
    display: flex; align-items: center; gap: 0.4rem;
}
.progress-terminal-body {
    background: #1e1e1e; color: #d4d4d4;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 0.75rem; line-height: 1.7;
    padding: 0.5rem 0.75rem;
    height: 200px; min-height: 80px; max-height: 500px;
    overflow-y: auto; resize: vertical;
    white-space: pre-wrap; word-break: break-word;
}
.progress-terminal-body font[color="black"],
.progress-terminal-body font[color="#000000"] { color: #6c9abd; }
.progress-terminal-body font[color="red"]   { color: #f88; }
.progress-terminal-body font[color="green"] { color: #8f8; }
.progress-terminal-body a { color: #79b8ff; text-decoration: none; }
.progress-terminal-body a:hover { text-decoration: underline; }
.progress-terminal-btn { color: rgba(255,255,255,0.5); border-color: #555; }
[data-bs-theme=light] .progress-terminal-hdr { background: #f0f2f4; color: #24292f; border-bottom-color: #d0d7de; }
[data-bs-theme=light] .progress-terminal-body { background: #f6f8fa; color: #24292f; }
[data-bs-theme=light] .progress-terminal-body font[color="black"],
[data-bs-theme=light] .progress-terminal-body font[color="#000000"] { color: #0550ae; }
[data-bs-theme=light] .progress-terminal-body font[color="red"]   { color: #cf222e; }
[data-bs-theme=light] .progress-terminal-body font[color="green"] { color: #1a7f37; }
[data-bs-theme=light] .progress-terminal-body a { color: #0969da; }
[data-bs-theme=light] .progress-terminal-btn { color: #57606a; border-color: #d0d7de; }
[data-bs-theme=light] .progress-terminal-btn:hover { color: #24292f; }
</style>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/acquisition_guide.jsp"/>

<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
<div class="card border-0 shadow-sm mb-3">
<div class="card-header d-flex align-items-center gap-2 flex-wrap" style="background:var(--bs-secondary-bg)">
<i class="bi bi-activity text-primary flex-shrink-0"></i>
<span class="fw-semibold">Progress</span>
<button class="btn btn-link btn-sm text-muted p-0 flex-shrink-0" type="button"
	data-bs-toggle="collapse" data-bs-target="#progressInfoLegend"
	aria-expanded="false" title="About this panel">
	<i class="bi bi-info-circle"></i>
</button>
<button class="btn btn-sm btn-outline-info ms-1 flex-shrink-0"
data-bs-toggle="offcanvas" data-bs-target="#acquisitionGuideOffcanvas"
title="Open Acquisition Guide">
<i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Acquisition </span>Guide
</button>
<span id="progressLiveStatus" class="flex-shrink-0"></span>
<div class="ms-auto d-flex gap-1 flex-shrink-0">
<button id="runNowBtn"
class="btn btn-sm btn-outline-success progress-terminal-btn py-0 px-2"
style="font-size:0.7rem;" disabled
<c:if test="${not empty acquisitionNote}">title="Cannot trigger: ${acquisitionNote}" </c:if>
onclick="progressRunNow(this)">
<i class="bi bi-play-fill"></i><span class="d-none d-sm-inline"> Run Now</span>
</button>
<button id="refreshLogBtn"
class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
style="font-size:0.7rem;"
onclick="progressRefresh(this)">
<i class="bi bi-arrow-clockwise"></i><span class="d-none d-sm-inline"> Refresh</span>
</button>
<button class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
style="font-size:0.7rem;"
onclick="progressCopy(this)">
<i class="bi bi-clipboard"></i><span class="d-none d-sm-inline"> Copy</span>
</button>
</div>
</div>
<div class="collapse" id="progressInfoLegend">
	<div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
		<p class="mb-1">This panel shows the raw console log produced while the Acquisition host runs: connection attempts, timing, debug messages, and any errors encountered during the listing process. It does not contain the file listing itself &mdash; the resulting file listing is shown in the Directory Listings panel below.</p>
		<ul class="mb-1 ps-3">
			<li><strong><i class="bi bi-play-fill"></i> Run Now:</strong> triggers the Acquisition Scheduler to process this host immediately. The button is disabled if no associated destination is active and running. If a listing is already running the button turns into <strong><i class="bi bi-skip-start-fill"></i> Interrupt &amp; Run</strong>.</li>
			<li><strong><i class="bi bi-arrow-clockwise"></i> Refresh:</strong> fetches the latest output. If the acquisition host is currently running, Watching mode starts automatically.</li>
			<li><strong><i class="bi bi-clipboard"></i> Copy:</strong> copies the full log text to the clipboard.</li>
		</ul>
		<div class="d-flex gap-3 flex-wrap mb-1">
			<span><span class="badge bg-primary-subtle text-primary-emphasis border"><span class="spinner-border spinner-border-sm" style="width:.55rem;height:.55rem"></span> Watching</span> acquisition is running; polling for output changes every few seconds.</span>
			<span><span class="badge bg-success-subtle text-success-emphasis border"><span class="spinner-grow spinner-grow-sm" style="width:.55rem;height:.55rem"></span> Live</span> output changed since last poll.</span>
			<span><span class="badge bg-secondary-subtle text-secondary-emphasis border">Idle</span> acquisition host is not running; click Refresh or Run Now to start monitoring.</span>
		</div>
	</div>
</div>
<div class="card-body p-0">
<div class="progress-terminal" style="width:100%;border-radius:0 0 var(--bs-card-border-radius) var(--bs-card-border-radius)">
<c:if test="${not empty acquisitionNote}">
<div class="d-flex align-items-start gap-2 px-3 py-2" style="background:rgba(255,193,7,0.12);border-bottom:1px solid rgba(255,193,7,0.3);font-size:0.82rem;color:var(--bs-warning-text-emphasis,#664d03)">
  <i class="bi bi-exclamation-triangle-fill" style="margin-top:0.1em;flex-shrink:0;color:#f0ad4e"></i>
  <span><strong>Run Now is disabled:</strong> <c:out value="${acquisitionNote}" escapeXml="false"/>. The Acquisition Scheduler requires at least one associated destination to be active and running.</span>
</div>
</c:if>
<div class="progress-terminal-body" id="progressBody" style="border-radius:0 0 var(--bs-card-border-radius) var(--bs-card-border-radius)">${host.formattedLastOutput}</div>
</div>
</div>
</div>
</auth:then>
</auth:if>

<div class="card shadow-sm mt-2">
    <div class="card-header d-flex align-items-center justify-content-between flex-wrap gap-2" style="background:var(--bs-secondary-bg)">
        <span class="fw-semibold d-inline-flex align-items-center gap-1 flex-shrink-0">
            <i class="bi bi-list-ul me-1 text-success"></i>
            Directory Listings
            <button class="btn btn-link btn-sm text-muted p-0 ms-1" style="font-size:0.85rem;line-height:1;vertical-align:middle;"
                    data-bs-toggle="collapse" data-bs-target="#outputInfoPanel"
                    aria-expanded="false" aria-controls="outputInfoPanel"
                    title="What is this output?">
                <i class="bi bi-info-circle"></i>
            </button>
        </span>
        <div class="d-flex gap-2 flex-wrap">
            <button id="refreshBtn"
                    class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
                    title="Refresh output" style="font-size:0.7rem;">
                <i class="bi bi-arrow-clockwise"></i><span class="d-none d-sm-inline"> Refresh</span>
            </button>
            <button id="rawBtn"
                    class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
                    title="Toggle between parsed view and raw server response" style="font-size:0.7rem;" disabled>
                <i class="bi bi-code-slash"></i><span class="d-none d-sm-inline"> Raw</span>
            </button>
            <button id="copyBtn"
                    class="btn btn-sm btn-outline-secondary progress-terminal-btn py-0 px-2"
                    title="Copy to clipboard" style="font-size:0.7rem;">
                <i class="bi bi-clipboard"></i><span class="d-none d-sm-inline"> Copy</span>
            </button>
        </div>
    </div>
    <div class="collapse" id="outputInfoPanel">
        <div class="px-3 pt-2 pb-3 border-bottom small" style="background:var(--bs-secondary-bg)">
            This panel shows the file listing retrieved from the remote site during the last acquisition run.
            This is the result used by the <strong>Acquisition Scheduler</strong> to determine which files to retrieve
            and which to skip, for example because they were already retrieved, do not match the configured pattern,
            or fall outside the selection criteria.
            The <strong>Refresh</strong> button fetches the latest listing from the server.
            The <strong>Raw</strong> button switches between the structured view and the original server response.
            The raw console log of the listing process itself is shown in the <strong>Progress</strong> panel above.
        </div>
    </div>
    <div class="card-body p-0" style="position:relative;">
        <div id="outputLoading" class="d-flex align-items-center gap-2 px-3 py-3 d-none output-loading">
            <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
            Loading output
        </div>
        <div id="outputContent" class="output-content" style="display:none; border-radius:0 0 4px 4px;"></div>
    </div>
</div>

<script>
(function () {
    var POLL_INTERVAL = 4000;
    var MAX_STALE     = 3;
    var _timer        = null;
    var _staleCount   = 0;
    var _lastLine     = '';
    var _hostId       = '<c:out value="${host.id}"/>';
    var _blocked      = ${not empty acquisitionNote ? 'true' : 'false'};

    function getBody()      { return document.getElementById('progressBody'); }
    function getStatus()    { return document.getElementById('progressLiveStatus'); }
    function scrollToBottom() { var b = getBody(); if (b) b.scrollTop = b.scrollHeight; }

    function lastLine(html) {
        if (!html) return '';
        var tmp = document.createElement('div');
        tmp.innerHTML = html;
        var lines = (tmp.innerText || tmp.textContent || '').split('\n');
        for (var i = lines.length - 1; i >= 0; i--) {
            var l = lines[i].trim();
            if (l) return l;
        }
        return '';
    }

    function setStatus(state) {
        var el = getStatus();
        if (!el) return;
        if (state === 'live') {
            el.innerHTML = '<span class="badge bg-success ms-1" style="font-size:0.65rem;vertical-align:middle">'
                + '<span class="spinner-grow spinner-grow-sm me-1" style="width:.5rem;height:.5rem;vertical-align:middle"></span>Live</span>';
        } else if (state === 'watching') {
            el.innerHTML = '<span class="badge bg-primary ms-1" style="font-size:0.65rem;vertical-align:middle;opacity:0.7">'
                + '<span class="spinner-border spinner-border-sm me-1" style="width:.5rem;height:.5rem;vertical-align:middle"></span>Watching</span>';
        } else if (state === 'idle') {
            el.innerHTML = '<span class="badge bg-secondary ms-1" style="font-size:0.65rem;vertical-align:middle">Idle</span>';
        } else {
            el.innerHTML = '';
        }
        // Sync the acquisition console icon badge in the host header
        if (typeof window._applyAcqBadge === 'function') {
            window._applyAcqBadge((state === 'watching' || state === 'live') ? state : '');
        }
    }

    function stopPolling() {
        if (_timer) { clearTimeout(_timer); _timer = null; }
        setStatus('idle');
        // Acquisition just finished — refresh the Directory Listings to show the new result
        if (typeof loadOutput === 'function') loadOutput();
    }

    function checkRunning(callback) {
        if (_blocked) { if (callback) callback(false); return; }
        fetch('/do/transfer/host/' + encodeURIComponent(_hostId) + '?json=acquisitionRunning')
            .then(function(r) { return r.ok ? r.json() : null; })
            .then(function(data) {
                var running = data && data.running === true;
                var runBtn = document.getElementById('runNowBtn');
                if (runBtn) {
                    if (running) {
                        runBtn.disabled = false;
                        runBtn.dataset.force = 'true';
                        runBtn.classList.remove('btn-outline-success');
                        runBtn.classList.add('btn-outline-warning');
                        runBtn.title = 'A listing is already running \u2014 click to interrupt and restart';
                        runBtn.innerHTML = '<i class="bi bi-skip-start-fill"></i> Interrupt &amp; Run';
                    } else {
                        runBtn.disabled = false;
                        runBtn.dataset.force = '';
                        runBtn.classList.remove('btn-outline-warning');
                        runBtn.classList.add('btn-outline-success');
                        runBtn.title = 'Start a new listing now';
                        runBtn.innerHTML = '<i class="bi bi-play-fill"></i> Run Now';
                    }
                }
                if (callback) callback(running);
            })
            .catch(function() { if (callback) callback(false); });
    }

    function fetchProgress(callback) {
        fetch(window.location.href)
            .then(function (r) { return r.text(); })
            .then(function (html) {
                var doc = new DOMParser().parseFromString(html, 'text/html');
                var el  = doc.getElementById('progressBody');
                callback(el ? el.innerHTML : null);
            })
            .catch(function () { callback(null); });
    }

    function poll() {
        _timer = null;
        fetchProgress(function (newContent) {
            if (newContent === null) { _timer = setTimeout(poll, POLL_INTERVAL); return; }
            var body = getBody();
            if (lastLine(newContent) !== _lastLine) {
                if (body) { body.innerHTML = newContent; scrollToBottom(); }
                _lastLine   = lastLine(newContent);
                _staleCount = 0;
                setStatus('live');
                _timer = setTimeout(poll, POLL_INTERVAL);
            } else {
                _staleCount++;
                if (_staleCount >= MAX_STALE) {
                    checkRunning(function(running) {
                        if (running) { _staleCount = 0; setStatus('watching'); _timer = setTimeout(poll, POLL_INTERVAL); }
                        else stopPolling();
                    });
                } else {
                    setStatus('watching');
                    _timer = setTimeout(poll, POLL_INTERVAL);
                }
            }
        });
    }

    function startPolling() {
        if (_timer) clearTimeout(_timer);
        _staleCount = 0;
        _lastLine   = lastLine(getBody() ? getBody().innerHTML : '');
        setStatus('watching');
        _timer = setTimeout(poll, POLL_INTERVAL);
    }

    window.progressRefresh = function (btn) {
        btn.disabled = true;
        var orig = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Refreshing...';
        if (_timer) { clearTimeout(_timer); _timer = null; }
        fetchProgress(function (newContent) {
            btn.disabled = false;
            btn.innerHTML = orig;
            if (newContent === null) return;
            var body = getBody();
            if (body) { body.innerHTML = newContent; scrollToBottom(); }
            _lastLine   = lastLine(newContent);
            _staleCount = 0;
            if (newContent.trim()) {
                checkRunning(function(running) { if (running) startPolling(); else setStatus('idle'); });
            } else { setStatus('idle'); }
        });
    };

    window.progressCopy = function (btn) {
        var body = getBody();
        if (!body) return;
        navigator.clipboard.writeText(body.innerText).then(function () {
            btn.innerHTML = '<i class="bi bi-check-lg"></i> Copied';
            setTimeout(function () { btn.innerHTML = '<i class="bi bi-clipboard"></i> Copy'; }, 1500);
        });
    };

    window.progressRunNow = function (btn) {
        var force = btn.dataset.force === 'true';
        if (force) { if (!confirm('A listing is already running.\nStop it and start a new one?')) return; }
        if (_timer) { clearTimeout(_timer); _timer = null; }
        btn.disabled = true;
        var orig = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Starting...';
        var url = '/do/transfer/host/' + encodeURIComponent(_hostId) + '?json=triggerAcquisition'
                + (force ? '&force=true' : '');
        fetch(url)
            .then(function(r) { return r.ok ? r.json() : null; })
            .then(function(data) {
                if (data && data.triggered) {
                    btn.innerHTML = '<i class="bi bi-check-circle-fill text-success me-1"></i> Triggered!';
                    setTimeout(function() {
                        fetchProgress(function(newContent) {
                            if (newContent !== null) {
                                var body = getBody();
                                if (body) { body.innerHTML = newContent; scrollToBottom(); }
                                _lastLine = lastLine(newContent);
                            }
                            _staleCount = 0;
                            btn.disabled = false;
                            checkRunning(null);
                            startPolling();
                        });
                    }, 1500);
                } else {
                    btn.innerHTML = orig;
                    btn.disabled = false;
                    checkRunning(null);
                }
            })
            .catch(function() { btn.innerHTML = orig; btn.disabled = false; });
    };

    document.addEventListener('DOMContentLoaded', function () {
        var body = getBody();
        if (body && body.innerHTML.trim()) {
            scrollToBottom();
            checkRunning(function(running) { if (running) startPolling(); else setStatus('idle'); });
        } else {
            setStatus('idle');
        }
    });
}());
</script>

<script>
/* -- Helpers -- */
function _esc(s) { return $('<span>').text(s || '').html(); }

/* Left-truncate: shows end of string (preserves filename in long paths) */
function _trunc(s, n) { s = s || ''; return s.length > n ? '\u2026' + s.slice(-(n - 1)) : s; }

/* Format byte count; accepts '?' for unknown sizes */
function _fmtSize(v) {
    var n = parseInt(v, 10);
    if (isNaN(n)) return v || '?';
    if (n >= 1073741824) return (n / 1073741824).toFixed(1) + ' GB';
    if (n >= 1048576)    return (n / 1048576).toFixed(1) + ' MB';
    if (n >= 1024)       return (n / 1024).toFixed(1) + ' KB';
    return n + ' B';
}

/* Find last balanced (...) group at the very end of str */
function _lastParen(str) {
    var depth = 0, end = -1;
    for (var i = str.length - 1; i >= 0; i--) {
        if (str[i] === ')') { if (depth === 0) end = i; depth++; }
        else if (str[i] === '(') { depth--; if (depth === 0 && end >= 0) return { start: i, content: str.substring(i + 1, end) }; }
    }
    return null;
}

/* Parse one <font> element into a file-entry object.
   Returns null if the text doesn't look like an ls listing line.
   Handles '?' as a size value (e.g. for 404-error entries).
   Preserves <a> links inside the status by extracting statusHtml from innerHTML. */
function _parseEntry($f) {
    var color    = ($f.attr('color') || '').toLowerCase();
    var $a       = $f.find('a').first();
    var href     = $a.length ? $a.attr('href') : '';
    var full     = $f.text().trim();
    var fullHtml = $f.html();   /* keep HTML for status link extraction */

    /* ls line: perms links owner group size Mon DD HH:MM rest
       Size may be '?' for entries the mover couldn't stat.      */
    var m = full.match(/^(\S+)\s+(\d+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\w{3}\s+\d+\s+[\d:]+)\s+([\s\S]+)$/);
    if (!m) return null;

    var perms = m[1], size = _fmtSize(m[5]), date = m[6].trim(), rest = m[7].trim();

    /* Extract trailing status in last balanced parens */
    var status = '', statusHtml = '', sBadge = 'secondary';
    var pg = _lastParen(rest);
    if (pg) {
        status = pg.content.trim();
        rest   = rest.substring(0, pg.start).trim();
        if (/^selected/i.test(status))                             sBadge = 'success';
        else if (/not.selected|error|fail|exception/i.test(status)) sBadge = 'danger';
        else if (/warn/i.test(status))                             sBadge = 'warning';

        /* Re-run _lastParen on the raw innerHTML so <a> links are preserved.
           This works because hrefs in this output never contain bare ( or ). */
        var pgH = _lastParen(fullHtml);
        statusHtml = pgH ? pgH.content.trim() : _esc(status);
    }

    var ai = rest.indexOf(' -> ');
    var filename = ai >= 0 ? rest.substring(0, ai).trim() : rest;
    var target   = ai >= 0 ? rest.substring(ai + 4).trim() : '';

    return { perms: perms, size: size, date: date,
             filename: filename, href: href,
             target: target, status: status, statusHtml: statusHtml, sBadge: sBadge, color: color };
}

/* -- Two-pass structured parser ------------------------------------------- */

/*  Pass 1 - extract block headers from the plain text of the HTML.
    jQuery .text() strips all tags and concatenates child text, so
    even values wrapped in <a> links (e.g. DataMover names) are included. */
function _parseBlockHeaders(html) {
    var plain = $('<div>').html(html).text();
    var globalDate = '', headers = [], cur = null;
    plain.split('\n').forEach(function(line) {
        line = line.trim();
        if (!line) return;
        if      (/^Date:\s/.test(line))    { globalDate = line.replace(/^Date:\s*/, ''); }
        else if (/^Path:/.test(line))      { cur = { date: globalDate, path: line.replace(/^Path:\s*/, ''), pattern: '', list: '' }; headers.push(cur); }
        else if (/^Pattern:/.test(line) && cur) { cur.pattern = line.replace(/^Pattern:\s*/, ''); }
        else if (/^List:/.test(line) && cur)    { cur.list = line.replace(/^List:\s*/, ''); cur = null; }
    });
    return { globalDate: globalDate, headers: headers };
}

/*  Pass 2 - walk the original DOM in order, assigning <font> file entries
    to blocks.  A new block begins each time we encounter a "Path:" line in
    a text node, matching the pre-built headers array in sequence. */
function _buildBlocks(html, headers) {
    var $root = $('<div>').html(html);
    var blocks = [], curBlock = null, hdrIdx = 0;

    function walkDom(node) {
        $(node).contents().each(function() {
            if (this.nodeType === 3) {
                /* Check text node for a Path: line to start a new block */
                if (/Path:/.test(this.textContent)) {
                    this.textContent.split('\n').forEach(function(line) {
                        if (/^Path:/.test(line.trim()) && hdrIdx < headers.length) {
                            curBlock = { hdr: headers[hdrIdx++], entries: [] };
                            blocks.push(curBlock);
                        }
                    });
                }
            } else if (this.nodeName === 'FONT') {
                if (!curBlock) return;
                var txt = $(this).text().trim();
                /* Skip empty stubs like "(empty)" */
                if (!txt || /^\(empty\)$/i.test(txt)) return;
                var e = _parseEntry($(this));
                if (e) {
                    curBlock.entries.push(e);
                } else {
                    /* Keep non-ls lines (e.g. raw error messages) as plain rows */
                    curBlock.entries.push({ raw: true, text: txt, color: ($(this).attr('color') || '').toLowerCase() });
                }
            } else if (/^(P|DIV|SPAN|A)$/.test(this.nodeName)) {
                walkDom(this);
            }
        });
    }

    walkDom($root.get(0));
    return blocks;
}

/* -- Rendering ------------------------------------------------------------ */

function _renderGlobalDate(dateStr, totalBlocks) {
    var dateVal = dateStr, ago = '';
    var dm = dateStr.match(/^(.+?)\s*(\([^)]+\))\s*$/);
    if (dm) { dateVal = dm[1]; ago = '<span class="go-ago ms-1">' + _esc(dm[2]) + '</span>'; }
    return '<div class="go-hdr">'
        + '<i class="bi bi-clock-history me-1" style="color:#6c757d"></i>'
        + '<span class="go-key">Listing date&nbsp;</span><span class="go-val">' + _esc(dateVal) + '</span>' + ago
        + '<span class="go-cnt"><span class="go-key">Paths&nbsp;</span><span class="go-val">' + totalBlocks + '</span></span>'
        + '</div>';
}

function _renderListingBlock(hdr, entries) {
    /* Compute per-block status */
    var nOk = 0, nErr = 0;
    entries.forEach(function(e) {
        if (e.raw) { if (e.color === 'red') nErr++; return; }
        if (e.color === 'green') nOk++;
        else if (e.color === 'red') nErr++;
    });
    var sc = entries.length === 0 ? 'lb-empty'
           : (nErr > 0 && nOk === 0 ? 'lb-err' : nErr > 0 ? 'lb-mixed' : 'lb-ok');

    /* Pattern badge */
    var patHtml = hdr.pattern
        ? '<span class="lb-pat" title="' + _esc(hdr.pattern) + '">' + _esc(_trunc(hdr.pattern, 40)) + '</span>'
        : '';

    /* List summary badge (e.g. "Found 3 files via ecpds-mover") */
    var listHtml = '';
    if (hdr.list) {
        var lm = hdr.list.match(/Found\s+(\d+)\s+file/);
        var cnt = lm ? lm[1] : '';
        var dm2 = hdr.list.match(/DataMover\s+(\S+)/);
        var mover = dm2 ? dm2[1] : '';
        listHtml = '<span class="lb-list">'
            + (cnt !== '' ? cnt + ' file' + (cnt !== '1' ? 's' : '') : _esc(hdr.list))
            + (mover ? '<span class="lb-mover"> via <a href="/do/datafile/transferserver/'
                + _esc(mover) + '" title="DataMover ' + _esc(mover) + '">'
                + _esc(mover) + '</a></span>' : '')
            + '</span>';
    }

    /* ok / err counts */
    var statHtml = '';
    if (nOk || nErr) {
        statHtml = '<span class="lb-stat">';
        if (nOk)  statHtml += '<span class="lb-cnt lb-cnt-ok">'  + nOk  + '&#10003;</span>';
        if (nErr) statHtml += '<span class="lb-cnt lb-cnt-err">' + nErr + '&#10007;</span>';
        statHtml += '</span>';
    }

    var hHtml = '<div class="lb-hdr">'
        + '<span class="lb-path" title="' + _esc(hdr.path) + '">' + _esc(_trunc(hdr.path, 64)) + '</span>'
        + patHtml + listHtml + statHtml
        + '</div>';

    /* Entries table */
    var tHtml = '';
    if (entries.length === 0) {
        tHtml = '<div class="lb-none">No files found</div>';
    } else {
        tHtml = '<table class="output-file-table"><tbody>';
        entries.forEach(function(e) {
            if (e.raw) {
                var rc = e.color === 'red' ? 'raw-err' : e.color === 'green' ? 'raw-ok' : 'raw-neu';
                tHtml += '<tr><td colspan="6" class="' + rc + '">' + _esc(e.text) + '</td></tr>';
                return;
            }
            var nameHtml = e.href
                ? '<a href="' + _esc(e.href) + '" target="_blank" rel="noopener noreferrer" title="'
                    + _esc(e.filename) + '">' + _esc(_trunc(e.filename, 72)) + '</a>'
                : '<span title="' + _esc(e.filename) + '">' + _esc(_trunc(e.filename, 72)) + '</span>';
            var targetHtml = e.target
                ? '&#x2192;&nbsp;<span title="' + _esc(e.target) + '">' + _esc(_trunc(e.target, 48)) + '</span>'
                : '';
            var statusHtml = '';
            if (e.status) {
                var bc = 'out-badge bg-' + e.sBadge + (e.sBadge === 'warning' ? ' text-dark' : '');
                statusHtml = '<span class="' + bc + '" title="' + _esc(e.status) + '">'
                    + (e.statusHtml || _esc(_trunc(e.status, 60))) + '</span>';
            }
            tHtml += '<tr>'
                + '<td class="c-perms">' + _esc(e.perms)   + '</td>'
                + '<td class="c-size">'  + _esc(e.size)    + '</td>'
                + '<td class="c-date">'  + _esc(e.date)    + '</td>'
                + '<td class="c-name">'  + nameHtml        + '</td>'
                + '<td class="c-target">'+ targetHtml      + '</td>'
                + '<td class="c-status">'+ statusHtml      + '</td>'
                + '</tr>';
        });
        tHtml += '</tbody></table>';
    }

    return '<div class="listing-block ' + sc + '">' + hHtml + tHtml + '</div>';
}

/* -- Main entry point ----------------------------------------------------- */

function _parseAndRender(rawHtml) {
    if (!/Date:\s/.test(rawHtml)) return null;

    var parsed = _parseBlockHeaders(rawHtml);
    if (!parsed.headers.length) return null;

    var blocks = _buildBlocks(rawHtml, parsed.headers);
    if (!blocks.length) return null;

    var html = '<div class="structured-output">'
        + _renderGlobalDate(parsed.globalDate, blocks.length);
    blocks.forEach(function(b) { html += _renderListingBlock(b.hdr, b.entries); });
    return html + '</div>';
}

/* -- Load / refresh / copy ----------------------------------------------- */
var _rawOutput = '';
var _rawHtml   = '';
var _structuredHtml = '';
var _showingRaw = false;

function _setRawMode(raw) {
    _showingRaw = raw;
    if (raw) {
        $('#outputContent').html('<pre>' + _rawHtml + '</pre>').show();
        $('#rawBtn').html('<i class="bi bi-layout-text-sidebar-reverse"></i> Parsed');
    } else {
        var html = _structuredHtml || ('<pre>' + _rawHtml + '</pre>');
        $('#outputContent').html(html).show();
        $('#rawBtn').html('<i class="bi bi-code-slash"></i> Raw');
    }
}

function loadOutput() {
    var wasRaw = _showingRaw;
    $('#rawBtn').prop('disabled', true);
    $('#outputContent').hide();
    $('#outputLoading').removeClass('d-none');
    $.ajax({
        url: '/do/transfer/host/edit/getOutput/load/${host.name}',
        type: 'GET',
        dataType: 'text',
        success: function(response) {
            $('#outputLoading').addClass('d-none');
            var trimmed = (response || '').trim();
            var isError = !trimmed || /^<!doctype|^<html/i.test(trimmed);
            if (isError) {
                _rawOutput = '';
                _rawHtml = '';
                _structuredHtml = '';
                _showingRaw = false;
                $('#rawBtn').html('<i class="bi bi-code-slash"></i> Raw');
                $('#outputContent').html('<pre>No output available for ${host.nickName}</pre>').show();
                return;
            }
            _rawHtml = trimmed;
            _rawOutput = $('<div>').html(trimmed).text();
            _structuredHtml = _parseAndRender(trimmed);
            if (_structuredHtml) {
                $('#rawBtn').prop('disabled', false);
                _setRawMode(wasRaw);
            } else {
                _showingRaw = false;
                $('#rawBtn').html('<i class="bi bi-code-slash"></i> Raw');
                $('#outputContent').html('<pre>' + trimmed + '</pre>').show();
            }
        },
        error: function() {
            $('#outputLoading').addClass('d-none');
            $('#outputContent').html('<pre>No output available for ${host.nickName}</pre>').show();
        }
    });
}

$(document).ready(function() {
    loadOutput();

    $('#refreshBtn').on('click', function() { loadOutput(); });

    $('#rawBtn').on('click', function() { _setRawMode(!_showingRaw); });

    $('#copyBtn').on('click', function() {
        var btn = $(this);
        navigator.clipboard.writeText(_rawOutput).then(function() {
            btn.html('<i class="bi bi-check-lg"></i> Copied');
            setTimeout(function() { btn.html('<i class="bi bi-clipboard"></i> Copy'); }, 1500);
        });
    });
});
</script>
