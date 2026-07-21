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

<div class="mt-2 mb-3 px-1">
  <small class="text-muted">
    <i class="bi bi-info-circle me-1"></i>
    Click any destination node to navigate to its page.
    Arrows show the direction of aliasing (source &rarr; target).
    Edge labels show the file-name filter pattern when it is not the default <code>.*</code>;
    a <strong>ⓘ</strong> marker appears on arrows that use default pattern but have other conditions set.
    Hover over any edge label to see the full condition string as a tooltip.
  </small>
</div>

<%-- REMOVED: temporary debug panel --%>

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

  try {
    _data = JSON.parse(document.getElementById('_aliasGraphData').textContent);
  } catch (e) { _data = {}; }

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
      if (e.condition && e.condition !== '.*') {
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
        lines.push(
          '  click ' + mId(n.name) +
          ' "/do/transfer/destination/' + encodeURIComponent(n.name) +
          '" "Go to ' + n.name + '"'
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

    show('_agOutput');
    /* Fresh unique ID on every render so Mermaid never skips re-processing */
    var uid = 'ag_' + Date.now();
    el('_agOutput').innerHTML =
      '<div id="' + uid + '" class="mermaid" style="max-width:100%">' + diagram + '</div>';

    try {
      await mermaid.run({ nodes: [el(uid)] });
    } catch (err) {
      /* mermaid.run() may throw (e.g. for click-handler registration) even after
         successfully rendering the SVG.  Only show the error panel when the
         target element was NOT replaced with an SVG. */
      var rendered = el(uid) && el(uid).querySelector('svg');
      if (!rendered) {
        console.error('Mermaid render error:', err);
        hideAll();
        show('_agError');
        el('_agErrorMsg').textContent = 'Diagram render failed: ' + (err.message || err);
      }
    }
    /* Stretch the SVG to fill the available card width regardless of
       Mermaid's auto-computed max-width (LR diagrams can otherwise render
       very small when there are only a few nodes). */
    var svg = el(uid) && el(uid).querySelector('svg');
    if (svg) {
      svg.style.width    = '100%';
      svg.style.maxWidth = '100%';
      svg.style.height   = 'auto';

      /* Attach custom HTML tooltip to every edge label.
         Mermaid v11 renders labels as selectable HTML text inside <foreignObject>.
         Per-element event listeners are unreliable (wrong bubbling path, text
         cursor, covered by crossing paths).  Instead we update the shared
         _tipData array; the single persistent mousemove listener on _agOutput
         (set up once below) checks bounding boxes on every mouse move —
         no pointer-events or bubbling required. */
      var edges      = (_data && _data.edges) ? _data.edges : [];
      var edgeLabels = Array.prototype.slice.call(
                         svg.querySelectorAll('.edgeLabels .edgeLabel')
                       ).filter(function (n) { return n.tagName === 'g' || n.tagName === 'G'; });

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

      /* Rebuild tooltip lookup (replaces any previous render's data).
         Use the <foreignObject> SVG element for hit detection — HTML elements
         inside <foreignObject> return unreliable client rects in some browsers.
         SVG elements always return correct viewport-relative coordinates.
         Fallback to <text> (older Mermaid themes) or the <g> itself. */
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

  /* ── public API ─────────────────────────────────────────────── */

  window._agSetLayout = function (layout) {
    _layout = layout;
    el('btnLR').classList.toggle('active', layout === 'LR');
    el('btnTD').classList.toggle('active', layout === 'TD');
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
    _agInitMermaid();
    _ready = true;
    doRender(_layout);
  };

  function _agInitMermaid() {
    var dark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
    mermaid.initialize({
      startOnLoad:   false,
      securityLevel: 'loose',
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
