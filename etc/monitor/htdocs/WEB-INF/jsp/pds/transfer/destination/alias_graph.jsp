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
        <li>Use <strong>LR</strong> (left &rarr; right) or <strong>TD</strong> (top &rarr; down) to switch the layout direction.
            TD is disabled automatically when the graph has too many nodes to remain readable.</li>
        <li>The <i class="bi bi-fullscreen"></i> button enters fullscreen mode; large graphs can be scrolled inside it.</li>
      </ul>
    </div>
  </div>

  <div class="card-body p-2" id="_agContainer" style="min-height:400px;position:relative;display:flex;flex-direction:column;justify-content:center;background:var(--bs-body-bg);">
    <div id="_agSpinner" class="d-flex justify-content-center align-items-center p-4">
      <div class="spinner-border spinner-border-sm text-secondary me-2" role="status"></div>
      <span class="text-muted small">Loading diagram&hellip;</span>
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
         font-size:12px;line-height:1.5;padding:5px 9px;border-radius:4px;white-space:pre;
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
  var _data    = null;
  var _layout  = 'LR';
  var _ready   = false;   // true once Mermaid has been initialised
  var _tipData = [];      // [{lg, tip}] rebuilt on every render

  /* sessionStorage cache: key = 'ecpds_ag:<center>:<layout>', value = svg.outerHTML.
     Lets the user navigate away and return without waiting for Mermaid to re-render.
     Clicking the already-active layout button clears that entry and forces a fresh render. */
  function _agCacheKey(layout) {
    return 'ecpds_ag:' + ((_data && _data.center) || '') + ':' + layout;
  }
  function _agCacheGet(layout) {
    try { return sessionStorage.getItem(_agCacheKey(layout)); } catch(e) { return null; }
  }
  function _agCachePut(layout, svgHtml) {
    try { sessionStorage.setItem(_agCacheKey(layout), svgHtml); } catch(e) { /* quota exceeded — ignore */ }
  }
  function _agCacheClear(layout) {
    try { sessionStorage.removeItem(_agCacheKey(layout)); } catch(e) {}
  }
  try {
    _data = JSON.parse(document.getElementById('_aliasGraphData').textContent);
  } catch (e) { _data = {}; }

  /* ── TD layout availability + edge-label visibility ─────────── */
  /* TD (top-down) stacks all nodes at the same depth horizontally,
     producing a diagram that is far too wide and illegible when the
     graph has many nodes.  LR (left-right) is always workable because
     alias graphs tend to be shallow, so disable TD above a threshold.
     The same threshold is used to suppress edge labels: with many nodes
     the labels overlap and misalign, making the diagram harder to read. */
  var _TD_NODE_THRESHOLD = 30;
  var _tdDisabled    = (_data && _data.nodes) ? _data.nodes.length > _TD_NODE_THRESHOLD : false;
  var _labelsHidden  = _tdDisabled;   /* hide edge labels whenever TD is also disabled */

  function _applyTdButtonState() {
    var btn = el('btnTD');
    if (!btn) return;
    if (_tdDisabled) {
      // Do NOT use aria-disabled="true" — Bootstrap 5 Tooltip._isDisabled() checks
      // it and refuses to show the tooltip (and still removes the native title).
      // Do NOT initialise a Bootstrap Tooltip — it moves title to data-bs-original-title
      // and then refuses to show due to the disabled check above.
      // Simply keep the button as a normal .btn (so btn-group CSS still applies),
      // prevent clicks, and rely on the browser's native title tooltip which works
      // because we never set the HTML disabled attribute (pointer events remain active).
      btn.setAttribute('tabindex', '-1');
      btn.removeAttribute('onclick');
      btn.addEventListener('click', function (e) { e.preventDefault(); e.stopPropagation(); });
      btn.style.opacity = '0.65';
      btn.style.cursor  = 'not-allowed';
      btn.title = 'TD layout is disabled for graphs with more than ' + _TD_NODE_THRESHOLD +
                  ' nodes \u2014 the diagram would be too small to read. Use LR instead.';
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
   * Escape text for embedding inside a Mermaid quoted label.
   * Mermaid understands HTML entity references (e.g. #quot;) inside labels.
   */
  function mLabel(s) {
    return s
      .replace(/&/g, '#amp;')
      .replace(/"/g, '#quot;')
      .replace(/</g, '#lt;')
      .replace(/>/g, '#gt;')
      .replace(/;/g, '#semi;');
  }

  /** Build the Mermaid flowchart source for the given layout direction. */
  function buildDiagram(layout) {
    var g = _data;
    if (!g || !g.nodes || g.nodes.length === 0) return null;

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
        labelText = '\u24d8';
      } else {
        labelText = '';
      }
      var cond = labelText ? '|"' + mLabel(labelText) + '"|' : '';
      lines.push('  ' + fromId + ' -->' + cond + ' ' + toId);
    });

    /* Click callbacks — only for nodes the user can access */
    g.nodes.forEach(function (n) {
      if (n.accessible !== false) {
        var tip = (n.comment && n.comment.trim()) ? n.comment.trim() : n.name;
        lines.push(
          '  click ' + mId(n.name) +
          ' "/do/transfer/destination/' + encodeURIComponent(n.name) +
          '" "' + tip.replace(/"/g, '#quot;') + '"'
        );
      }
    });

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

  async function doRender(layout) {
    var diagram = buildDiagram(layout);
    hideAll();
    if (!diagram) { show('_agEmpty'); return; }

    var uid    = 'ag_' + Date.now();
    var cached = _agCacheGet(layout);

    if (cached) {
      /* ── Cache hit: inject the pre-rendered SVG immediately ── */
      el('_agOutput').innerHTML =
        '<div id="' + uid + '" style="width:100%;max-width:100%">' + cached + '</div>';
      hideAll();
      show('_agOutput');
    } else {
      /* ── Cache miss: run Mermaid ── */
      show('_agSpinner');
      /* Yield the main thread so the browser can repaint the spinner before
         mermaid.run() begins its CPU-intensive SVG generation. */
      await new Promise(function (resolve) { setTimeout(resolve, 0); });

      el('_agOutput').innerHTML =
        '<div id="' + uid + '" class="mermaid" style="max-width:100%">' + diagram + '</div>';

      try {
        await mermaid.run({ nodes: [el(uid)] });
      } catch (err) {
        var rendered = el(uid) && el(uid).querySelector('svg');
        if (!rendered) {
          console.error('Mermaid render error:', err);
          hideAll();
          show('_agError');
          el('_agErrorMsg').textContent = 'Diagram render failed: ' + (err.message || err);
          return;
        }
      }
      hideAll();
      show('_agOutput');
    }
    /* Stretch the SVG to fill the available card width regardless of
       Mermaid's auto-computed max-width (LR diagrams can otherwise render
       very small when there are only a few nodes). */
    var svg = el(uid) && el(uid).querySelector('svg');
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
        /* CSS scoped to current uid — must be re-injected on every render,
           including cache hits, because the uid changes each time. */
        var styleEl = document.createElement('style');
        styleEl.textContent =
          '#' + uid + ' .edgeLabel, #' + uid + ' .edgeLabel * ' +
          '{ cursor: help !important; user-select: none !important; }' +
          '#' + uid + ' .edgeLabel rect ' +
          '{ fill: transparent !important; stroke: none !important; }' +
          '#' + uid + ' .edgeLabel div, #' + uid + ' .edgeLabel span, ' +
          '#' + uid + ' .edgeLabel foreignObject ' +
          '{ background: transparent !important; background-color: transparent !important; }';
        document.head.appendChild(styleEl);

        if (!cached) {
          /* Fresh render: inject ⓘ pill badges into default-pattern edge labels.
             On cache hits the pills are already baked into the SVG HTML. */
          edges.forEach(function (e, i) {
            if (!edgeLabels[i] || e.condition !== '.*') return;
            var lg = edgeLabels[i];
            var fo = lg.querySelector('foreignObject');
            if (!fo) return;

            lg.querySelectorAll('rect').forEach(function (r) {
              r.style.setProperty('fill', 'transparent', 'important');
              r.style.setProperty('stroke', 'none', 'important');
            });

            fo.setAttribute('width', '36');
            fo.setAttribute('height', '22');
            fo.setAttribute('x', String(parseFloat(fo.getAttribute('x') || 0) - 10));
            fo.setAttribute('y', String(parseFloat(fo.getAttribute('y') || 0) - 2));
            fo.style.overflow = 'visible';

            var node = fo;
            while (node.firstElementChild) { node = node.firstElementChild; }
            var walk = node;
            while (walk && walk !== fo) {
              walk.style.setProperty('background', 'transparent', 'important');
              walk.style.setProperty('background-color', 'transparent', 'important');
              walk.style.setProperty('padding', '0', 'important');
              walk = walk.parentElement;
            }
            node.innerHTML =
              '<span style="display:inline-flex;align-items:center;justify-content:center;' +
              'padding:2px 8px;border-radius:12px;' +
              'background:#0d6efd;color:#fff;' +
              'font-size:10px;font-style:italic;font-weight:bold;font-family:Georgia,serif;' +
              'line-height:1.4;letter-spacing:0.5px;' +
              'box-shadow:0 1px 4px rgba(0,0,0,.5);cursor:help;white-space:nowrap">i</span>';
            var pill = node.querySelector('span');
            if (pill) {
              pill.style.setProperty('background',       '#0d6efd', 'important');
              pill.style.setProperty('background-color', '#0d6efd', 'important');
              pill.style.setProperty('color',            '#ffffff', 'important');
            }
          });

          /* Store the fully-processed SVG in sessionStorage for future visits. */
          _agCachePut(layout, svg.outerHTML);
        }

        /* Rebuild tooltip DOM references — always required because element
           handles change with every injection, cached or not. */
        _tipData = [];
        edges.forEach(function (e, i) {
          var tip = e.full || e.condition || '';
          if (!tip || !edgeLabels[i]) return;
          var lg  = edgeLabels[i];
          var fo  = lg.querySelector('foreignObject');
          var hit = fo || lg.querySelector('text') || lg;
          _tipData.push({ lg: hit, tip: tip });
        });
      }
    }
  }

  /* ── public API ─────────────────────────────────────────────── */

  window._agSetLayout = function (layout) {
    if (layout === 'TD' && _tdDisabled) return;
    var isCurrent = (layout === _layout && el('_agOutput') && !el('_agOutput').classList.contains('d-none'));
    if (isCurrent) {
      /* Clicking the already-active button: force a fresh render by evicting
         the cached SVG.  This lets the user refresh without a page reload. */
      _agCacheClear(layout);
    } else {
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
    _applyTdButtonState();
    _agInitMermaid();
    _ready = true;
    doRender(_layout);
  };

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
