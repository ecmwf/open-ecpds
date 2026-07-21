<%@ page session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<%-- Graph data injected as JSON in a typed script block to avoid JS-string escaping pitfalls. --%>
<script type="application/json" id="_aliasGraphData">${aliasGraphJson}</script>

<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-diagram-3 text-primary"></i>
    <span class="fw-semibold">Alias Relationship Graph &mdash; <c:out value="${destination.name}"/></span>
    <button class="btn btn-link btn-sm text-muted p-0" type="button"
        data-bs-toggle="collapse" data-bs-target="#agInfoPanel"
        aria-expanded="false" title="About this graph">
      <i class="bi bi-info-circle"></i>
    </button>
    <div class="ms-auto d-flex gap-2 align-items-center">
      <div class="btn-group btn-group-sm" id="layoutBtnGroup">
        <button class="btn btn-outline-secondary active" id="btnLR" onclick="_agSetLayout('LR')" title="Left &rarr; Right">LR</button>
        <button class="btn btn-outline-secondary"        id="btnTD" onclick="_agSetLayout('TD')" title="Top &rarr; Down">TD</button>
      </div>
      <div class="btn-group btn-group-sm">
        <button class="btn btn-outline-secondary active" id="btnDirBoth" data-dir="both"
                onclick="_agSetDirection('both')"       title="Show all connections">&#8660; All</button>
        <button class="btn btn-outline-secondary"       id="btnDirUp"   data-dir="upstream"
                onclick="_agSetDirection('upstream')"   title="Show only upstream (nodes that alias into this destination)">&larr; Up</button>
        <button class="btn btn-outline-secondary"       id="btnDirDown" data-dir="downstream"
                onclick="_agSetDirection('downstream')" title="Show only downstream (nodes this destination aliases into)">Down &rarr;</button>
      </div>
      <span id="_agDepthWrap">
        <select class="form-select form-select-sm" id="_agDepthSel"
                style="width:auto" title="Maximum depth from centre node"
                onchange="_agSetDepth(this.value)">
        </select>
      </span>
      <button class="btn btn-sm btn-outline-secondary d-none d-md-inline-flex" id="_agFsBtn"
              title="Toggle fullscreen" onclick="_agFullscreen()">
        <i class="bi bi-fullscreen"></i>
      </button>
    </div>
  </div>

  <div class="collapse" id="agInfoPanel">
    <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
      <strong class="d-block mb-1">Alias Relationship Graph &mdash; overview</strong>
      <ul class="mb-0 ps-3">
        <li>Click any destination node to navigate to its page.</li>
        <li>Arrows show the direction of aliasing (source &rarr; target).</li>
        <li>Edge labels show the file-name filter pattern when it is not the default <code>.*</code>.</li>
        <li>A <strong>ⓘ</strong> marker appears on arrows that use the default pattern but have other conditions set; hover the marker to see the full condition string.</li>
        <li>On large graphs (more than 30 nodes) edge labels and ⓘ markers are hidden automatically to avoid overlap and keep the diagram readable.</li>
        <li>Use <strong>⇔ All / ← Up / Down →</strong> to show all connections, only upstream parents, or only downstream children of this destination.</li>
        <li>Use the <strong>Depth</strong> selector to limit the graph to nodes within N hops of this destination — useful for large graphs.</li>
        <li>Use <strong>LR</strong> (left &rarr; right) or <strong>TD</strong> (top &rarr; down) to switch the layout direction.
            TD is disabled automatically when the graph has too many nodes to remain readable.</li>
        <li>The <i class="bi bi-fullscreen"></i> button enters fullscreen mode; large graphs can be scrolled inside it.</li>
      </ul>
    </div>
  </div>

  <div class="card-body p-2" id="_agContainer" style="min-height:400px;position:relative;display:flex;flex-direction:column;justify-content:center;background:var(--bs-body-bg);">
    <div id="_agSpinner" class="d-flex flex-column justify-content-center align-items-center p-4 gap-2 d-none">
      <div class="d-flex align-items-center gap-2">
        <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
        <span class="text-muted small" id="_agSpinnerMsg">Rendering diagram&hellip;</span>
      </div>
      <div class="progress w-100" style="height:4px;max-width:220px">
        <div class="progress-bar progress-bar-striped progress-bar-animated w-100"
             role="progressbar" style="background:var(--bs-primary)"></div>
      </div>
    </div>
    <div id="_agOutput" class="w-100 d-flex d-none justify-content-center align-items-center"></div>
    <div id="_agEmpty"  class="alert alert-info d-flex d-none align-items-center gap-2 m-2">
      <i class="bi bi-info-circle-fill"></i>
      <span>Destination <strong><c:out value="${destination.name}"/></strong> has no alias relationships.</span>
    </div>
    <div id="_agError"  class="alert alert-warning d-flex d-none align-items-center gap-2 m-2">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <span id="_agErrorMsg">Could not render diagram.</span>
    </div>
    <%-- Tooltip lives inside _agContainer so it remains visible in fullscreen mode
         (only descendants of the fullscreen element are rendered by the browser) --%>
    <div id="_agTip" style="display:none;position:fixed;z-index:9999;
         font-size:12px;line-height:1.5;padding:5px 9px;border-radius:4px;
         white-space:pre-wrap;word-break:break-word;max-width:360px;
         pointer-events:none;font-family:monospace"></div>
  </div>
</div>

<%-- REMOVED: temporary debug panel --%>

<%-- Allow scrolling when the diagram container is in fullscreen mode so that
     large graphs (which exceed the viewport height) remain fully accessible.
     justify-content:flex-start is required alongside overflow:auto because
     the default justify-content:center clips content that overflows the start
     of the container (the overflowing portion cannot be scrolled into view). --%>
<style>
#_agContainer:-webkit-full-screen { overflow: auto !important; justify-content: flex-start !important; }
#_agContainer:-moz-full-screen    { overflow: auto !important; justify-content: flex-start !important; }
#_agContainer:fullscreen          { overflow: auto !important; justify-content: flex-start !important; }
</style>

<%--
  IMPORTANT: the IIFE that defines window._agInit / _agSetLayout / _agFullscreen
  MUST appear BEFORE the blocking <script src> below, because a non-async/non-defer
  external script fires its onload handler synchronously, before the next <script>
  block is parsed.
--%>
<script>
(function () {

  /* ── state ───────────────────────────────────────────────────── */
  var _data      = null;
  var _layout    = 'LR';
  var _depth     = 0;         // 0 = all levels; positive integer = max hops from centre
  var _direction = 'both';   // 'both' | 'upstream' | 'downstream'
  var _ready     = false;    // true once Mermaid has been initialised
  var _tipData   = [];       // [{lg, tip}] rebuilt on every render

  try {
    _data = JSON.parse(document.getElementById('_aliasGraphData').textContent);
  } catch (e) { _data = {}; }

  /* ── TD layout availability + edge-label visibility ─────────── */
  /* Both are re-evaluated on every render based on the *filtered* node count
     so narrowing the graph via depth/direction re-enables labels and TD layout. */
  var _TD_NODE_THRESHOLD = 30;
  var _tdDisabled   = false;   /* updated in buildDiagram() */
  var _labelsHidden = false;   /* updated in buildDiagram() */

  /* Stored so removeEventListener can target the same function reference */
  var _tdBlocker = function (e) { e.preventDefault(); e.stopPropagation(); };

  function _applyTdButtonState(disabled) {
    var btn = el('btnTD');
    if (!btn) return;
    _tdDisabled = disabled;
    if (disabled) {
      btn.setAttribute('tabindex', '-1');
      btn.removeAttribute('onclick');
      btn.removeEventListener('click', _tdBlocker);
      btn.addEventListener('click', _tdBlocker);
      btn.style.opacity = '0.65';
      btn.style.cursor  = 'not-allowed';
      btn.title = 'TD layout is disabled for graphs with more than ' + _TD_NODE_THRESHOLD +
                  ' nodes \u2014 the diagram would be too small to read. Use LR instead.';
    } else {
      btn.removeAttribute('tabindex');
      btn.setAttribute('onclick', "_agSetLayout('TD')");
      btn.removeEventListener('click', _tdBlocker);
      btn.style.opacity = '';
      btn.style.cursor  = '';
      btn.title = 'Top \u2192 Down';
      btn.classList.toggle('active', _layout === 'TD');
    }
  }

  /* ── DOM helpers ─────────────────────────────────────────────── */
  function el(id)    { return document.getElementById(id); }
  /* Use d-none (display:none !important) so it beats Bootstrap's d-flex !important */
  function show(id)  { el(id).classList.remove('d-none'); }
  function hide(id)  { el(id).classList.add('d-none'); }
  function hideAll() {
    hide('_agSpinner');
    hide('_agOutput');
    hide('_agEmpty');
    hide('_agError');
  }

  /* ── Mermaid helpers ─────────────────────────────────────────── */

  /** Sanitise a destination name into a valid Mermaid node identifier. */
  function mId(name) {
    return 'N_' + name.replace(/[^A-Za-z0-9]/g, '_');
  }

  /**
   * Escape text for embedding inside a Mermaid |…| edge label.
   * Mermaid understands HTML entity references (e.g. #quot;) inside labels.
   * Control characters (newlines, tabs, etc.) are replaced with a space because
   * a newline inside a Mermaid directive breaks the parser.
   * IMPORTANT: semicolons must be escaped FIRST — otherwise the semicolons we
   * introduce as part of #amp; / #quot; / etc. would themselves be re-escaped,
   * turning e.g. #quot; into #quot#semi; which Mermaid cannot parse.
   *
   * Characters that are syntactically special inside |…| labels:
   *   ( )  → #lpar; / #rpar;   (cause parse error if unescaped)
   *   [ ]  → #lsqb; / #rsqb;  (treated as node-shape delimiters)
   *   |    → #vert;            (would terminate the label prematurely)
   * All of the above are listed as supported Mermaid entities.
   */
  function mLabel(s) {
    return s
      .replace(/[\r\n\t\x00-\x1f\x7f]+/g, ' ')  /* control chars → space (MUST be first) */
      .replace(/;/g, '#semi;')                   /* escape ; BEFORE adding any #…; tokens */
      .replace(/&/g, '#amp;')
      .replace(/"/g, '#quot;')
      .replace(/</g, '#lt;')
      .replace(/>/g, '#gt;')
      .replace(/\(/g, '#lpar;')
      .replace(/\)/g, '#rpar;')
      .replace(/\[/g, '#lsqb;')
      .replace(/\]/g, '#rsqb;')
      .replace(/\|/g, '#vert;');
  }

  /**
   * Return a filtered copy of {nodes, edges, center} applying both the depth
   * limit and the direction filter.
   *
   * direction:
   *   'both'       – traverse edges in both directions (default)
   *   'upstream'   – only follow edges that point TO the current node
   *                  (shows ancestors: nodes that alias into the centre)
   *   'downstream' – only follow edges that go FROM the current node
   *                  (shows descendants: nodes the centre aliases into)
   *
   * depth = 0 means unlimited hops.
   */
  function applyFilters(g, depth, direction) {
    if (!g || !g.center) return g;
    var unlimited = !depth || depth <= 0;
    var visited   = {};
    var queue     = [g.center];
    visited[g.center] = 0;
    while (queue.length) {
      var cur  = queue.shift();
      var hops = visited[cur];
      if (!unlimited && hops >= depth) continue;
      g.edges.forEach(function (e) {
        var nb = null;
        /* downstream: follow edges out of cur */
        if (direction !== 'upstream'   && e.from === cur && !(e.to   in visited)) { nb = e.to;   }
        /* upstream:   follow edges into cur (traverse backwards) */
        if (direction !== 'downstream' && e.to   === cur && !(e.from in visited)) { nb = e.from; }
        if (nb !== null) { visited[nb] = hops + 1; queue.push(nb); }
      });
    }
    /* Keep all edges where both endpoints are in the visible set */
    return {
      center: g.center,
      nodes:  g.nodes.filter(function (n) { return n.name in visited; }),
      edges:  g.edges.filter(function (e) { return (e.from in visited) && (e.to in visited); })
    };
  }

  /** Build the Mermaid flowchart source for the given layout direction. */
  function buildDiagram(layout) {
    var g = applyFilters(_data, _depth, _direction);
    if (!g || !g.nodes || g.nodes.length === 0) return null;

    /* Re-evaluate threshold flags based on the *filtered* node count so that
       narrowing the graph via depth/direction re-enables labels and TD layout. */
    var nodeCount = g.nodes.length;
    _labelsHidden = nodeCount > _TD_NODE_THRESHOLD;
    _applyTdButtonState(nodeCount > _TD_NODE_THRESHOLD);

    /* If TD just became unavailable and we're currently in TD, silently fall
       back to LR so the rendered diagram is always usable. */
    if (_labelsHidden && layout === 'TD') {
      _layout = 'LR';
      layout  = 'LR';
      el('btnLR') && el('btnLR').classList.add('active');
      el('btnTD') && el('btnTD').classList.remove('active');
    }

    var lines = ['flowchart ' + layout];

    /* Node declarations */
    g.nodes.forEach(function (n) {
      var id  = mId(n.name);
      var lbl = mLabel(n.name);
      var cls;
      if      (n.name === g.center)                                                     { cls = ':::center';   }
      else if (n.accessible === false)                                                   { cls = ':::noaccess'; }
      else if (!n.active)                                                                { cls = ':::inactive'; }
      else if (n.status === 'Running')                                                   { cls = ':::running';  }
      else if (n.status === 'Waiting' || n.status === 'Retrying'
                                      || n.status === 'Interrupted')                    { cls = ':::waiting';  }
      else if (n.status === 'Initialized' || n.status === 'Stopped'
                                         || n.status === 'NoHosts'
                                         || n.status === 'Failed')                      { cls = ':::stopped';  }
      else                                                                               { cls = '';            }
      lines.push('  ' + id + '["' + lbl + '"]' + cls);
    });

    /* Edge declarations */
    g.edges.forEach(function (e) {
      var fromId = mId(e.from);
      var toId   = mId(e.to);
      var labelText;
      if (_labelsHidden) {
        /* Too many nodes: labels overlap and misalign — suppress them entirely
           for a cleaner diagram.  Tooltips are also skipped (no labels to hover). */
        labelText = '';
      } else if (e.condition && e.condition !== '.*') {
        /* Non-default pattern: show the pattern value */
        labelText = e.condition;
      } else if (e.full) {
        /* Default pattern (.*) but there IS a condition string: show ⓘ as a
           hoverable anchor so the tooltip is accessible even on bare arrows */
        labelText = 'i';  /* plain ASCII — post-render code replaces this with the blue pill badge */
      } else {
        labelText = '';
      }
      /* NOTE: inside Mermaid's |…| edge-label syntax the text must NOT be
         additionally wrapped in double-quotes — those are only valid in the
         alternative  A -- "text" --> B  form and cause a parse error here. */
      var cond = labelText ? '|' + mLabel(labelText) + '|' : '';
      lines.push('  ' + fromId + ' -->' + cond + ' ' + toId);
    });

    /* Click callbacks — handled via post-render JS listeners instead of Mermaid's
       click directive, so no encoding restrictions apply and the diagram source
       is simpler. See the node-tooltip/click wiring below. */

    /* Style classes */
    lines.push('  classDef center       fill:#ffc107,stroke:#e0a800,color:#212529,font-weight:bold');
    lines.push('  classDef running      fill:#198754,stroke:#146c43,color:#fff');
    lines.push('  classDef waiting      fill:#fd7e14,stroke:#dc6502,color:#fff');
    lines.push('  classDef stopped      fill:#dc3545,stroke:#b02a37,color:#fff');
    lines.push('  classDef inactive     fill:#adb5bd,stroke:#6c757d,color:#fff,stroke-dasharray:4 2');
    lines.push('  classDef noaccess     fill:#e9ecef,stroke:#adb5bd,color:#6c757d,stroke-dasharray:4 2,font-style:italic');

    return lines.join('\n');
  }

  /* ── rendering ───────────────────────────────────────────────── */

  /* Yield helper: waits for the next animation frame + a setTimeout so the
     browser has a full repaint cycle before the next CPU-intensive step. */
  function yieldToUI() {
    return new Promise(function (resolve) {
      requestAnimationFrame(function () { setTimeout(resolve, 0); });
    });
  }

  async function doRender(layout) {
    /* Show the spinner immediately — before any CPU work — so the user sees
       feedback as soon as they change a control. */
    hideAll();
    show('_agSpinner');
    var msgEl = el('_agSpinnerMsg');
    if (msgEl) { msgEl.textContent = 'Building diagram\u2026'; }

    /* Yield so the browser repaints the spinner and progress bar before we
       start the synchronous work (buildDiagram + mermaid.render). */
    await yieldToUI();

    var diagram = buildDiagram(layout);
    if (!diagram) { hideAll(); show('_agEmpty'); return; }

    if (msgEl) {
      var filteredCount = (applyFilters(_data, _depth, _direction) || {nodes:[]}).nodes.length;
      msgEl.textContent = 'Rendering ' + filteredCount + ' node' +
                          (filteredCount !== 1 ? 's' : '') + '\u2026';
    }

    /* Second yield so the updated message is painted before mermaid blocks. */
    await yieldToUI();

    /* Fresh unique ID on every render so Mermaid never skips re-processing */
    var uid = 'ag_' + Date.now();

    /* Use mermaid.render() which takes the diagram string directly (no DOM read),
       avoiding HTML-encoding issues that occur when setting innerHTML first. */
    var svgText;
    try {
      var renderResult = await mermaid.render(uid, diagram);
      svgText = renderResult.svg || renderResult;
    } catch (err) {
      console.error('[alias-graph] mermaid.render() error:', err);
      hideAll();
      show('_agError');
      el('_agErrorMsg').textContent = 'Diagram render failed: ' + (err.message || err);
      return;
    }

    el('_agOutput').innerHTML = '<div id="' + uid + '_wrap" style="max-width:100%">' + svgText + '</div>';
    hideAll();
    show('_agOutput');

    /* Stretch the SVG to fill the available card width regardless of
       Mermaid's auto-computed max-width (LR diagrams can otherwise render
       very small when there are only a few nodes). */
    var svg = el(uid + '_wrap') && el(uid + '_wrap').querySelector('svg');
    if (svg) {
      svg.style.width    = '100%';
      svg.style.maxWidth = '100%';
      svg.style.height   = 'auto';

      var edges      = (_data && _data.edges) ? _data.edges : [];
      var edgeLabels = Array.prototype.slice.call(
                         svg.querySelectorAll('.edgeLabels .edgeLabel')
                       ).filter(function (n) { return n.tagName === 'g' || n.tagName === 'G'; });

      if (_labelsHidden) {
        /* Large graph: all edge labels were suppressed in the diagram source but
           Mermaid still creates empty <g class="edgeLabel"> elements.  Hide them
           completely so they cannot produce phantom cursors, tooltips, or artefacts. */
        var labelsGroup = svg.querySelector('.edgeLabels');
        if (labelsGroup) { labelsGroup.style.display = 'none'; }
        _tipData = [];
      } else {
        /* Force cursor and suppress text selection on all edge labels so the
           browser doesn't show a text cursor over the ⓘ marker. */
        var styleEl = document.createElement('style');
        styleEl.textContent =
          '#' + uid + ' .edgeLabel, #' + uid + ' .edgeLabel * ' +
          '{ cursor: help !important; user-select: none !important; }' +
          /* Remove edge-label background: covers SVG <rect> fills and HTML backgrounds */
          '#' + uid + ' .edgeLabel rect ' +
          '{ fill: transparent !important; stroke: none !important; }' +
          '#' + uid + ' .edgeLabel div, #' + uid + ' .edgeLabel span, ' +
          '#' + uid + ' .edgeLabel foreignObject ' +
          '{ background: transparent !important; background-color: transparent !important; }';
        document.head.appendChild(styleEl);

        /* For default-pattern (ⓘ) edges: replace the label with a visible pill badge. */
        edges.forEach(function (e, i) {
          if (!edgeLabels[i] || e.condition !== '.*') return;
          var lg = edgeLabels[i];
          var fo = lg.querySelector('foreignObject');
          if (!fo) return;

          /* Clear backgrounds on all SVG rect descendants */
          lg.querySelectorAll('rect').forEach(function (r) {
            r.style.setProperty('fill', 'transparent', 'important');
            r.style.setProperty('stroke', 'none', 'important');
          });

          /* Widen the foreignObject — Mermaid sized it for the ⓘ character alone */
          fo.setAttribute('width', '36');
          fo.setAttribute('height', '22');
          fo.setAttribute('x', String(parseFloat(fo.getAttribute('x') || 0) - 10));
          fo.setAttribute('y', String(parseFloat(fo.getAttribute('y') || 0) - 2));
          fo.style.overflow = 'visible';

          /* Walk down to the innermost element and replace its content */
          var node = fo;
          while (node.firstElementChild) { node = node.firstElementChild; }
          /* Clear all backgrounds on ancestors inside the foreignObject */
          var walk = node;
          while (walk && walk !== fo) {
            walk.style.setProperty('background', 'transparent', 'important');
            walk.style.setProperty('background-color', 'transparent', 'important');
            walk.style.setProperty('padding', '0', 'important');
            walk = walk.parentElement;
          }
          /* Inject the pill */
          node.innerHTML =
            '<span style="display:inline-flex;align-items:center;justify-content:center;' +
            'padding:2px 8px;border-radius:12px;' +
            'background:#0d6efd;color:#fff;' +
            'font-size:10px;font-style:italic;font-weight:bold;font-family:Georgia,serif;' +
            'line-height:1.4;letter-spacing:0.5px;' +
            'box-shadow:0 1px 4px rgba(0,0,0,.5);cursor:help;white-space:nowrap">i</span>';
          /* Re-apply background with !important to beat the blanket
             "background:transparent !important" CSS rule we injected above. */
          var pill = node.querySelector('span');
          if (pill) {
            pill.style.setProperty('background',       '#0d6efd', 'important');
            pill.style.setProperty('background-color', '#0d6efd', 'important');
            pill.style.setProperty('color',            '#ffffff', 'important');
          }
        });

        /* Rebuild tooltip lookup (replaces any previous render's data). */
        _tipData = [];
        edges.forEach(function (e, i) {
          var tip = e.full || e.condition || '';
          if (!tip || !edgeLabels[i]) return;
          var lg  = edgeLabels[i];
          var fo  = lg.querySelector('foreignObject');
          var hit = fo || lg.querySelector('text') || lg;
          _tipData.push({ lg: hit, tip: tip });
        });
      } /* end else (!_labelsHidden) */

      /* Node hover tooltips: show the destination comment (or name as fallback).
         Handled here rather than via Mermaid's click-directive tooltip so the
         comment text can contain any characters without Mermaid encoding limits.
         Mermaid renders each node as <g class="node" id="flowchart-N_name-N">. */
      var nodeCommentMap = {};
      if (_data && _data.nodes) {
        _data.nodes.forEach(function (n) {
          var txt = (n.comment && n.comment.trim()) ? n.comment.trim() : n.name;
          nodeCommentMap[mId(n.name)] = txt;
        });
      }
      /* Build node-id → url map for click navigation (we don't use Mermaid's
         click directive because it causes parse errors in Mermaid 11.16). */
      var nodeUrlMap = {};
      if (_data && _data.nodes) {
        _data.nodes.forEach(function (n) {
          if (n.accessible !== false) {
            nodeUrlMap[mId(n.name)] = '/do/transfer/destination/' + encodeURIComponent(n.name);
          }
        });
      }
      svg.querySelectorAll('g.node').forEach(function (g) {        /* With mermaid.render(uid, …) node IDs are prefixed: "{uid}-flowchart-{NODEID}-{IDX}"
           Strip the uid prefix before matching so we can extract the NODEID correctly. */
        var rawId = g.id || '';
        if (rawId.indexOf(uid + '-') === 0) { rawId = rawId.slice(uid.length + 1); }
        var m = rawId.match(/^flowchart-(.+)-\d+$/);
        if (!m) return;
        var tip = nodeCommentMap[m[1]];
        if (tip) { _tipData.push({ lg: g, tip: tip }); }
        var url = nodeUrlMap[m[1]];
        if (url) {
          g.style.cursor = 'pointer';
          g.addEventListener('click', function () { window.location.href = url; });
        }
      });
    }
  }

  /* ── public API ─────────────────────────────────────────────── */

  window._agSetDepth = function (val) {
    var d = parseInt(val, 10);
    _depth = isNaN(d) || d <= 0 ? 0 : d;
    if (_ready) { doRender(_layout); }
  };

  window._agSetDirection = function (dir) {
    _direction = dir;
    ['btnDirBoth', 'btnDirUp', 'btnDirDown'].forEach(function (id) {
      var btn = el(id);
      if (btn) { btn.classList.toggle('active', btn.dataset.dir === dir); }
    });
    /* Rebuild depth options to reflect what's reachable in the new direction */
    _agPopulateDepthSelect();
    if (_ready) { doRender(_layout); }
  };

  window._agSetLayout = function (layout) {
    if (layout === 'TD' && _tdDisabled) return;
    var isCurrent = (layout === _layout && el('_agOutput') && !el('_agOutput').classList.contains('d-none'));
    if (!isCurrent) {
      _layout = layout;
      el('btnLR').classList.toggle('active', layout === 'LR');
      el('btnTD').classList.toggle('active', layout === 'TD');
    }
    if (_ready) { doRender(layout); }
  };

  window._agFullscreen = function () {
    var c = el('_agContainer');
    if (!document.fullscreenElement) {
      /* Copy the current theme onto the container so Bootstrap's dark-mode
         CSS still applies inside the fullscreen context (fullscreen elements
         are rendered outside <html> and don't inherit data-bs-theme). */
      var theme = document.documentElement.getAttribute('data-bs-theme');
      if (theme) { c.setAttribute('data-bs-theme', theme); }
      c.requestFullscreen && c.requestFullscreen();
    } else {
      document.exitFullscreen && document.exitFullscreen();
    }
  };

  /* Move Mermaid's node tooltip div into/out of the fullscreen element so it
     remains visible in fullscreen (only descendants of the fullscreen element
     are rendered by the browser). */
  document.addEventListener('fullscreenchange', function () {
    var tip = document.querySelector('.mermaidTooltip');
    var c   = el('_agContainer');
    if (!tip || !c) return;
    if (document.fullscreenElement) {
      c.appendChild(tip);
    } else {
      document.body.appendChild(tip);
    }
  });

  /* Hide the fullscreen button if the Fullscreen API is not supported */
  if (!document.documentElement.requestFullscreen) {
    var fsBtn = el('_agFsBtn');
    if (fsBtn) { fsBtn.style.display = 'none'; }
  }

  /* Single persistent tooltip listener — reads _tipData which is rebuilt
     on every render.  Bounding-box detection works regardless of SVG
     z-order, pointer-events, or foreignObject event-bubbling quirks. */
  (function () {
    var tipDiv   = document.getElementById('_agTip');
    var output   = el('_agOutput');
    var PAD = 4; /* px of extra hit area around each label */
    output.addEventListener('mousemove', function (ev) {
      var shown = false;
      for (var j = 0; j < _tipData.length; j++) {
        var r = _tipData[j].lg.getBoundingClientRect();
        if (ev.clientX >= r.left - PAD && ev.clientX <= r.right  + PAD &&
            ev.clientY >= r.top  - PAD && ev.clientY <= r.bottom + PAD) {
          tipDiv.textContent   = _tipData[j].tip;
          tipDiv.style.left    = (ev.clientX + 14) + 'px';
          tipDiv.style.top     = (ev.clientY + 14) + 'px';
          tipDiv.style.display = 'block';
          shown = true;
          break;
        }
      }
      if (!shown) { tipDiv.style.display = 'none'; }
    });
    output.addEventListener('mouseleave', function () {
      tipDiv.style.display = 'none';
    });
  }());

  /* Called by onload on the Mermaid <script src> below */
  window._agInit = function () {
    _agPopulateDepthSelect();
    _agInitMermaid();
    _ready = true;
    doRender(_layout);
  };

  /* Populate the depth <select> with "All levels" + one option per reachable
     depth level in the graph, respecting the current _direction filter. */
  function _agPopulateDepthSelect() {
    var sel = el('_agDepthSel');
    if (!sel || !_data || !_data.center || !_data.edges) return;

    /* BFS respecting direction to find max hop distance from centre */
    var dist  = {};
    var queue = [_data.center];
    dist[_data.center] = 0;
    var maxDepth = 0;
    while (queue.length) {
      var cur  = queue.shift();
      var hops = dist[cur];
      _data.edges.forEach(function (e) {
        var nb = null;
        if (_direction !== 'upstream'   && e.from === cur && !(e.to   in dist)) { nb = e.to;   }
        if (_direction !== 'downstream' && e.to   === cur && !(e.from in dist)) { nb = e.from; }
        if (nb) { dist[nb] = hops + 1; if (hops + 1 > maxDepth) { maxDepth = hops + 1; } queue.push(nb); }
      });
    }

    /* Reset depth to "all" if current depth exceeds the new max */
    if (_depth > maxDepth) {
      _depth = 0;
    }

    /* Rebuild options */
    sel.innerHTML = '<option value="0">All levels</option>';
    for (var d = 1; d <= maxDepth; d++) {
      var opt = document.createElement('option');
      opt.value       = d;
      opt.textContent = 'Depth ' + d;
      opt.selected    = (d === _depth);
      sel.appendChild(opt);
    }

    /* Hide when there is only one depth level (filtering would change nothing) */
    var wrap = el('_agDepthWrap');
    if (wrap) { wrap.style.display = maxDepth <= 1 ? 'none' : ''; }
  }

  function _agInitMermaid() {
    var dark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
    mermaid.initialize({
      startOnLoad:   false,
      securityLevel: 'loose',
      maxTextSize:   1000000,
      theme: dark ? 'dark' : 'default',
      themeVariables: dark ? {
        edgeLabelBackground: '#2c2c2c',
        lineColor:           '#adb5bd',
        mainBkg:             '#2c3e50'
      } : {
        lineColor:           '#495057'
      },
      flowchart: { useMaxWidth: false, curve: 'basis' }
    });
    _agStyleTooltip(dark);
  }

  /* Inject/update a <style> rule that makes the Mermaid node tooltip
     (.mermaidTooltip) match the current Bootstrap theme. */
  var _agTipStyleEl = null;
  function _agStyleTooltip(dark) {
    if (!_agTipStyleEl) {
      _agTipStyleEl = document.createElement('style');
      document.head.appendChild(_agTipStyleEl);
    }
    var shared = 'border-radius:4px !important; font-size:12px !important;';
    if (dark) {
      var props = 'background:#343a40 !important; color:#f8f9fa !important;' +
                  ' border:1px solid #6c757d !important;' +
                  ' box-shadow:0 2px 6px rgba(0,0,0,.5) !important;';
      _agTipStyleEl.textContent =
        '.mermaidTooltip { ' + props + shared + ' }';
      var tip = document.getElementById('_agTip');
      if (tip) {
        tip.style.background  = '#343a40';
        tip.style.color       = '#f8f9fa';
        tip.style.border      = '1px solid #6c757d';
        tip.style.boxShadow   = '0 2px 6px rgba(0,0,0,.5)';
      }
    } else {
      var props = 'background:#fff !important; color:#212529 !important;' +
                  ' border:1px solid #dee2e6 !important;' +
                  ' box-shadow:0 2px 6px rgba(0,0,0,.15) !important;';
      _agTipStyleEl.textContent =
        '.mermaidTooltip { ' + props + shared + ' }';
      var tip = document.getElementById('_agTip');
      if (tip) {
        tip.style.background  = '#fff';
        tip.style.color       = '#212529';
        tip.style.border      = '1px solid #dee2e6';
        tip.style.boxShadow   = '0 2px 6px rgba(0,0,0,.15)';
      }
    }
  }

  /* Re-render with updated theme when the user toggles dark/light mode.
     Bootstrap sets data-bs-theme on <html>; watch for that mutation.
     Also sync the attribute onto _agContainer so fullscreen mode stays themed. */
  new MutationObserver(function () {
    if (!_ready) return;
    var theme = document.documentElement.getAttribute('data-bs-theme');
    var c = el('_agContainer');
    if (c && theme) { c.setAttribute('data-bs-theme', theme); }
    _agInitMermaid();
    doRender(_layout);
  }).observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });

  /* Called by onerror on the Mermaid <script src> below */
  window._agLoadFailed = function () {
    hideAll();
    show('_agError');
    el('_agErrorMsg').textContent = 'Could not load the Mermaid library. Check server logs.';
  };

}());
</script>

<%-- Mermaid is served locally (no CDN) to respect the Content Security Policy. --%>
<script src="/assets/js/mermaid.min.js" onload="_agInit()" onerror="_agLoadFailed()"></script>
