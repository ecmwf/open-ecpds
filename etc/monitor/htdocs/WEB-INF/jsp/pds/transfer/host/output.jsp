<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/host/host_header.jsp"/>

<style>
/* ── Global date bar ── */
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
[data-bs-theme=light] .report-card-hdr-btn:hover { color: #fff; }
[data-bs-theme=light] .output-loading { background: #f6f8fa; }
[data-bs-theme=light] .output-content { background: #f6f8fa; }
.go-key { color: #555; text-transform: uppercase; letter-spacing: 0.06em; font-size: 0.64rem; }
.go-val { color: #b0b0b0; }
.go-ago { color: #555; }
.go-cnt { color: #6c757d; border-left: 1px solid #333; padding-left: 0.6rem; margin-left: 0.2rem; }

/* ── Per-path listing blocks ── */
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
</style>

<div class="card shadow-sm mt-2">
    <div class="card-header d-flex align-items-center justify-content-between py-2 px-3 report-card-hdr">
        <span class="fw-semibold" style="font-size:0.875rem;">
            <i class="bi bi-terminal-fill me-2 text-success"></i>
            Output: <c:out value="${host.nickName}" />
        </span>
        <div class="d-flex gap-2">
            <button id="refreshBtn"
                    class="btn btn-sm btn-outline-secondary py-0 px-2 report-card-hdr-btn"
                    title="Refresh output" style="font-size:0.75rem;">
                <i class="bi bi-arrow-clockwise"></i> Refresh
            </button>
            <button id="copyBtn"
                    class="btn btn-sm btn-outline-secondary py-0 px-2 report-card-hdr-btn"
                    title="Copy to clipboard" style="font-size:0.75rem;">
                <i class="bi bi-clipboard"></i> Copy
            </button>
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
/* ── Helpers ── */
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
             filename: filename, href: href || filename,
             target: target, status: status, statusHtml: statusHtml, sBadge: sBadge, color: color };
}

/* ── Two-pass structured parser ─────────────────────────────────────────── */

/*  Pass 1 – extract block headers from the plain text of the HTML.
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

/*  Pass 2 – walk the original DOM in order, assigning <font> file entries
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

/* ── Rendering ──────────────────────────────────────────────────────────── */

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
            var nameHtml = '<a href="' + _esc(e.href) + '" target="_blank" rel="noopener noreferrer" title="'
                + _esc(e.filename) + '">' + _esc(_trunc(e.filename, 72)) + '</a>';
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

/* ── Main entry point ───────────────────────────────────────────────────── */

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

/* ── Load / refresh / copy ─────────────────────────────────────────────── */
var _rawOutput = '';

function loadOutput() {
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
                $('#outputContent').html('<pre>No output available for ${host.nickName}</pre>').show();
                return;
            }
            _rawOutput = $('<div>').html(trimmed).text();
            var structured = _parseAndRender(trimmed);
            if (structured) {
                $('#outputContent').html(structured).show();
            } else {
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

    $('#copyBtn').on('click', function() {
        var btn = $(this);
        navigator.clipboard.writeText(_rawOutput).then(function() {
            btn.html('<i class="bi bi-check-lg"></i> Copied');
            setTimeout(function() { btn.html('<i class="bi bi-clipboard"></i> Copy'); }, 1500);
        });
    });
});
</script>
