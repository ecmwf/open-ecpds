<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-diagram-3 text-primary"></i>
    <span class="fw-semibold">Alias Relationship Graph — <c:out value="${destination.name}"/></span>
    <div class="ms-auto d-flex gap-2 align-items-center">
      <div class="btn-group btn-group-sm" id="layoutBtnGroup">
        <button class="btn btn-outline-secondary active" id="btnLR" onclick="setLayout('LR')" title="Left → Right">LR</button>
        <button class="btn btn-outline-secondary"        id="btnTD" onclick="setLayout('TD')" title="Top → Down">TD</button>
      </div>
      <button class="btn btn-sm btn-outline-secondary" id="btnFullscreen" title="Fullscreen" onclick="toggleFullscreen()">
        <i class="bi bi-fullscreen"></i>
      </button>
    </div>
  </div>
  <div class="card-body p-2" id="graphContainer" style="min-height:320px;position:relative;">
    <div id="mermaidOutput" class="w-100 d-flex justify-content-center align-items-start"></div>
    <div id="graphEmpty" class="alert alert-info d-flex align-items-center gap-2 m-2" style="display:none!important">
      <i class="bi bi-info-circle-fill"></i>
      <span>Destination <strong><c:out value="${destination.name}"/></strong> has no alias relationships.</span>
    </div>
  </div>
</div>

<div class="mt-2 mb-3 px-1">
  <small class="text-muted">
    <i class="bi bi-info-circle me-1"></i>
    Click any destination node to navigate to its page.
    Arrows show the direction of aliasing (source &rarr; target).
    Edge labels show the file-name filter pattern when it is not the default <code>.*</code>.
  </small>
</div>

<script>
(function () {
  var _graphData = null;
  var _layout   = 'LR';

  try {
    _graphData = JSON.parse('${aliasGraphJson}');
  } catch (e) {
    _graphData = {};
  }

  /* ── helpers ─────────────────────────────────────────────────── */

  /** Sanitise a destination name to a valid Mermaid node ID. */
  function mId(name) {
    return 'N_' + name.replace(/[^A-Za-z0-9]/g, '_');
  }

  /** Escape a string for use inside a Mermaid quoted label. */
  function mLabel(s) {
    return s.replace(/\\/g, '\\\\').replace(/"/g, '#quot;').replace(/</g, '#lt;').replace(/>/g, '#gt;');
  }

  /** Build the Mermaid diagram source string from _graphData. */
  function buildDiagram(layout) {
    var g = _graphData;
    if (!g || !g.nodes || g.nodes.length === 0) return null;

    var lines = ['flowchart ' + layout];

    /* Node declarations */
    g.nodes.forEach(function (n) {
      var id  = mId(n.name);
      var lbl = mLabel(n.name);
      var cls = '';
      if (n.name === g.center)    { cls = ':::center'; }
      else if (!n.active)         { cls = ':::inactive'; }
      else if (n.status === 'Running')   { cls = ':::running'; }
      else if (n.status === 'Idle')      { cls = ':::idle'; }
      else if (n.status === 'Waiting' || n.status === 'Retrying' || n.status === 'Interrupted') {
        cls = ':::waiting';
      } else if (n.status === 'Initialized' || n.status === 'Stopped' ||
                 n.status === 'NoHosts'     || n.status === 'Failed') {
        cls = ':::stopped';
      }
      lines.push('  ' + id + '["' + lbl + '"]' + cls);
    });

    /* Edge declarations */
    g.edges.forEach(function (e) {
      var fromId = mId(e.from);
      var toId   = mId(e.to);
      var cond   = (e.condition && e.condition !== '.*') ? '|"' + mLabel(e.condition) + '"|' : '';
      lines.push('  ' + fromId + ' -->' + cond + ' ' + toId);
    });

    /* Click handlers */
    g.nodes.forEach(function (n) {
      lines.push('  click ' + mId(n.name) + ' "/do/transfer/destination/' +
                 encodeURIComponent(n.name) + '" "Go to ' + n.name + '"');
    });

    /* CSS classes */
    lines.push('  classDef center   fill:#ffc107,stroke:#e0a800,color:#212529,font-weight:bold');
    lines.push('  classDef running  fill:#198754,stroke:#146c43,color:#fff');
    lines.push('  classDef idle     fill:#6c757d,stroke:#5a6268,color:#fff');
    lines.push('  classDef waiting  fill:#ffc107,stroke:#e0a800,color:#212529');
    lines.push('  classDef stopped  fill:#dc3545,stroke:#b02a37,color:#fff');
    lines.push('  classDef inactive fill:#adb5bd,stroke:#6c757d,color:#fff,stroke-dasharray:4 2');

    return lines.join('\n');
  }

  /* ── render ──────────────────────────────────────────────────── */

  function render(layout) {
    var diagram = buildDiagram(layout);
    var output  = document.getElementById('mermaidOutput');
    var empty   = document.getElementById('graphEmpty');

    if (!diagram) {
      output.style.display = 'none';
      empty.style.display  = '';
      return;
    }

    /* Reset the output div so Mermaid processes it afresh */
    output.innerHTML = '<div class="mermaid">' + diagram + '</div>';
    output.style.display = '';
    empty.style.display  = 'none';

    if (window.mermaid) {
      mermaid.run({ nodes: output.querySelectorAll('.mermaid') });
    }
  }

  /* ── layout toggle ───────────────────────────────────────────── */

  window.setLayout = function (layout) {
    _layout = layout;
    document.getElementById('btnLR').classList.toggle('active', layout === 'LR');
    document.getElementById('btnTD').classList.toggle('active', layout === 'TD');
    render(layout);
  };

  /* ── fullscreen ──────────────────────────────────────────────── */

  window.toggleFullscreen = function () {
    var el = document.getElementById('graphContainer');
    if (!document.fullscreenElement) {
      el.requestFullscreen && el.requestFullscreen();
    } else {
      document.exitFullscreen && document.exitFullscreen();
    }
  };

  /* ── boot ────────────────────────────────────────────────────── */

  /* Load Mermaid from CDN then render */
  var script = document.createElement('script');
  script.type = 'module';
  script.textContent = [
    'import mermaid from "https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs";',
    'window.mermaid = mermaid;',
    'mermaid.initialize({',
    '  startOnLoad: false,',
    '  securityLevel: "loose",',
    '  theme: (document.documentElement.getAttribute("data-bs-theme") === "dark") ? "dark" : "default",',
    '  flowchart: { useMaxWidth: true, htmlLabels: true, curve: "basis" }',
    '});',
    'render_alias_graph();'
  ].join('\n');
  document.head.appendChild(script);

  window.render_alias_graph = function () {
    render(_layout);
  };
}());
</script>
