<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
/* Structured output blocks */
.output-block { border-bottom: 1px solid #333; }
.output-block:last-child { border-bottom: none; }
.output-block-hdr {
    background: #2a2a2a;
    border-bottom: 1px solid #3a3a3a;
    padding: 0.35rem 1rem;
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
    font-size: 0.7rem;
    font-family: 'Consolas', 'Monaco', monospace;
}
.output-block-hdr .hk { color: #6c757d; text-transform: uppercase; letter-spacing: 0.06em; margin-right: 0.3rem; }
.output-block-hdr .hv { color: #b0b0b0; }
.output-block-hdr .hago { color: #555; }
.output-file-table { width: 100%; border-collapse: collapse; font-size: 0.72rem; font-family: 'Consolas', 'Monaco', monospace; }
.output-file-table tbody tr:hover td { background: rgba(255,255,255,0.04); }
.output-file-table td { padding: 0.12rem 0.6rem; vertical-align: middle; color: #d4d4d4; white-space: nowrap; }
.output-file-table td.c-perms { color: #6c757d; }
.output-file-table td.c-size  { color: #79b8ff; text-align: right; min-width: 4.5rem; }
.output-file-table td.c-date  { color: #6c757d; }
.output-file-table td.c-name a { color: #d4d4d4; text-decoration: none; }
.output-file-table td.c-name a:hover { color: #79b8ff; text-decoration: underline; }
.output-file-table td.c-target { color: #6c757d; max-width: 260px; overflow: hidden; text-overflow: ellipsis; }
.output-file-table td.c-status { max-width: 320px; }
.out-badge {
    display: inline-block; font-size: 0.62rem; padding: 0.1em 0.45em;
    border-radius: 3px; white-space: normal; line-height: 1.4; vertical-align: middle;
}
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
.structured-output { max-height: 75vh; overflow-y: auto; }
</style>

<div class="card shadow-sm mt-2">
    <div class="card-header d-flex align-items-center justify-content-between py-2 px-3"
         style="background:#2d2d2d; border-bottom:1px solid #444;">
        <span class="text-white fw-semibold" style="font-size:0.875rem;">
            <i class="bi bi-terminal-fill me-2 text-success"></i>
            Output: <c:out value="${host.nickName}" />
        </span>
        <div class="d-flex gap-2">
            <button id="refreshBtn"
                    class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2"
                    title="Refresh output" style="font-size:0.75rem;">
                <i class="bi bi-arrow-clockwise"></i> Refresh
            </button>
            <button id="copyBtn"
                    class="btn btn-sm btn-outline-secondary border-secondary text-white-50 py-0 px-2"
                    title="Copy to clipboard" style="font-size:0.75rem;">
                <i class="bi bi-clipboard"></i> Copy
            </button>
        </div>
    </div>
    <div class="card-body p-0" style="position:relative;">
        <div id="outputLoading" class="d-flex align-items-center gap-2 px-3 py-3 d-none"
             style="background:#1e1e1e; color:#6c757d; font-size:0.8rem;">
            <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
            Loading output
        </div>
        <div id="outputContent" style="display:none; background:#1e1e1e; border-radius:0 0 4px 4px;"></div>
    </div>
</div>

<script>
/* --- output.jsp helpers --- */
function _esc(s) { return $('<span>').text(s || '').html(); }
function _trunc(s, n) { s = s || ''; return s.length > n ? '\u2026' + s.slice(-(n-1)) : s; }

/* Find the last balanced (...) group at the very end of str */
function _lastParen(str) {
    var depth = 0, end = -1;
    for (var i = str.length - 1; i >= 0; i--) {
        var c = str[i];
        if (c === ')') { if (depth === 0) end = i; depth++; }
        else if (c === '(') { depth--; if (depth === 0 && end >= 0) return { start: i, content: str.substring(i+1, end) }; }
    }
    return null;
}

/* Format a byte count */
function _fmtSize(n) {
    n = parseInt(n, 10) || 0;
    if (n >= 1073741824) return (n/1073741824).toFixed(1)+' GB';
    if (n >= 1048576)    return (n/1048576).toFixed(1)+' MB';
    if (n >= 1024)       return (n/1024).toFixed(1)+' KB';
    return n+' B';
}

/* Parse one <font> element into a file-entry object; returns null if not an ls line */
function _parseEntry($f) {
    var color = ($f.attr('color') || '').toLowerCase();
    var $a    = $f.find('a').first();
    var href  = $a.length ? $a.attr('href') : '';
    var full  = $f.text().trim();
    /* ls format: perms links owner group size Mon DD HH:MM rest */
    var m = full.match(/^(\S+)\s+(\d+)\s+(\S+)\s+(\S+)\s+(\d+)\s+(\w{3}\s+\d+\s+[\d:]+)\s+([\s\S]+)$/);
    if (!m) return null;

    var perms = m[1], size = _fmtSize(m[5]), date = m[6].trim(), rest = m[7].trim();

    /* Extract trailing status (last balanced parens) */
    var status = '', sBadge = 'secondary';
    var pg = _lastParen(rest);
    if (pg) {
        status = pg.content.trim();
        rest   = rest.substring(0, pg.start).trim();
        if (/^selected$/i.test(status))         sBadge = 'success';
        else if (/not.selected|error|fail/i.test(status)) sBadge = 'danger';
        else if (/warn/i.test(status))          sBadge = 'warning';
    }

    /* Split filename and symlink target */
    var ai = rest.indexOf(' -> ');
    var filename = ai >= 0 ? rest.substring(0, ai).trim() : rest;
    var target   = ai >= 0 ? rest.substring(ai + 4).trim() : '';

    return { perms: perms, size: size, date: date,
             filename: filename, href: href || filename,
             target: target, status: status, sBadge: sBadge, color: color };
}

/* Render one block (header + entries array) to an HTML string */
function _renderBlock(hdr, entries) {
    /* Header */
    var ago = '', dateVal = hdr.date;
    var dm = hdr.date.match(/^(.+?)\s*(\([^)]+\))\s*$/);
    if (dm) { dateVal = dm[1]; ago = ' <span class="hago">'+_esc(dm[2])+'</span>'; }

    var hHtml = '<div class="output-block-hdr">'
        + '<span><span class="hk">Date</span><span class="hv">'+_esc(dateVal)+'</span>'+ago+'</span>';
    if (hdr.path)    hHtml += '<span><span class="hk">Path</span><span class="hv">'+_esc(hdr.path)+'</span></span>';
    if (hdr.pattern) hHtml += '<span><span class="hk">Pattern</span><span class="hv">'+_esc(hdr.pattern)+'</span></span>';
    hHtml += '</div>';

    /* Entries table */
    var tHtml = '';
    if (entries.length === 0) {
        tHtml = '<div style="padding:0.4rem 1rem; color:#555; font-size:0.72rem; font-style:italic;">No entries</div>';
    } else {
        tHtml = '<table class="output-file-table"><tbody>';
        entries.forEach(function(e) {
            var rs = e.color === 'red' ? ' style="color:#f88"' : e.color === 'green' ? ' style="color:#8f8"' : '';
            var nameDisplay = _esc(_trunc(e.filename, 60));
            var nameHtml = '<a href="'+_esc(e.href)+'" target="_blank" rel="noopener noreferrer" title="'+_esc(e.filename)+'">'+nameDisplay+'</a>';
            var targetHtml = e.target ? '&#x2192;&nbsp;<span title="'+_esc(e.target)+'">'+_esc(_trunc(e.target, 48))+'</span>' : '';
            var statusHtml = '';
            if (e.status) {
                var bc = 'out-badge bg-'+e.sBadge+(e.sBadge==='warning'?' text-dark':'');
                statusHtml = '<span class="'+bc+'" title="'+_esc(e.status)+'">'+_esc(e.status)+'</span>';
            }
            tHtml += '<tr'+rs+'>'
                + '<td class="c-perms">'+_esc(e.perms)+'</td>'
                + '<td class="c-size">'+_esc(e.size)+'</td>'
                + '<td class="c-date">'+_esc(e.date)+'</td>'
                + '<td class="c-name">'+nameHtml+'</td>'
                + '<td class="c-target">'+targetHtml+'</td>'
                + '<td class="c-status">'+statusHtml+'</td>'
                + '</tr>';
        });
        tHtml += '</tbody></table>';
    }
    return '<div class="output-block">'+hHtml+tHtml+'</div>';
}

/* Try to parse Date:/Path:/Pattern: block structure; returns HTML string or null */
function _parseAndRender(rawHtml) {
    if (!/Date:\s/.test(rawHtml)) return null;

    var $tmp = $('<div>').html(rawHtml);
    var blocks = [], curHdr = null, curEntries = [];

    function flush() {
        if (curHdr) blocks.push({ hdr: curHdr, entries: curEntries.slice() });
        curHdr = null; curEntries = [];
    }

    function onText(text) {
        text.split('\n').forEach(function(line) {
            line = line.trim();
            if (!line) return;
            if (/^Date:\s/.test(line))    { flush(); curHdr = { date: line.replace(/^Date:\s*/,''), path:'', pattern:'' }; }
            else if (/^Path:\s/.test(line)    && curHdr) curHdr.path    = line.replace(/^Path:\s*/,'');
            else if (/^Pattern:\s*/.test(line) && curHdr) curHdr.pattern = line.replace(/^Pattern:\s*/,'');
        });
    }

    function onFont($f) { if (!curHdr) return; var e = _parseEntry($f); if (e) curEntries.push(e); }

    function walk(node) {
        $(node).contents().each(function() {
            if (this.nodeType === 3) onText(this.textContent);
            else if (this.nodeName === 'FONT') onFont($(this));
            else if (/^(P|DIV|SPAN)$/.test(this.nodeName)) walk(this);
        });
    }

    walk($tmp.get(0));
    flush();
    if (!blocks.length) return null;

    var html = '<div class="structured-output">';
    blocks.forEach(function(b) { html += _renderBlock(b.hdr, b.entries); });
    return html + '</div>';
}

/* --- Main load function --- */
var _rawOutput = '';   /* kept for copy: full untruncated plain text */

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
            /* Store plain text (tags stripped) for copy button */
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
