<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<%-- Whether any field (in any category) is of type "markdown" - controls whether the single, shared
     "Markdown Guide" button is shown in the card header. --%>
<c:set var="hasMarkdownField" value="false"/>
<c:forEach var="f" items="${metaFields}">
  <c:if test="${f.type == 'markdown'}"><c:set var="hasMarkdownField" value="true"/></c:if>
</c:forEach>

<style>
.dmf-row { display: flex; gap: 0.5rem; align-items: flex-start; margin-bottom: 0.4rem; }
.dmf-row .dmf-input { flex: 1; min-width: 0; }
.dmf-row .dmf-remove { flex: 0 0 auto; }
.dmf-add { font-size: 0.8rem; }
.dmf-field-item { display: flex; flex-direction: column; gap: 0.25rem; }
.dmf-field-label { font-size: 0.82rem; font-weight: 600; color: var(--bs-body-color); }
.dmf-readonly-value { font-size: 0.9rem; padding: 0.15rem 0; color: var(--bs-body-color); word-break: break-word; }
.dmf-readonly-empty { font-size: 0.85rem; color: var(--bs-secondary-color); font-style: italic; }
.dmf-notes-toggle { cursor: pointer; font-weight: normal; font-size: 0.72rem; color: var(--bs-secondary-color); white-space: nowrap; margin-bottom: 0; }
.dmf-notes-toggle .form-check-input { margin-top: 0; }
.dmf-markdown-editor { min-height: 220px; border: 1px solid var(--bs-border-color); border-radius: var(--bs-border-radius-sm); }
.dmf-markdown-preview { min-height: 220px; max-height: 400px; overflow: auto; border: 1px solid var(--bs-border-color); border-radius: var(--bs-border-radius-sm); padding: 0.5rem 0.75rem; font-size: 0.88rem; background: var(--bs-tertiary-bg); }
.dmf-markdown-preview :is(h1,h2,h3) { font-size: 1.1rem; margin-top: 0.5rem; }
.dmf-markdown-preview table { border-collapse: collapse; width: 100%; }
.dmf-markdown-preview th, .dmf-markdown-preview td { border: 1px solid var(--bs-border-color); padding: 0.25rem 0.5rem; }
.dmf-markdown-pane-label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.04em; color: var(--bs-secondary-color); margin-bottom: 0.15rem; }
/* "Include in Notes" only makes sense for a field that currently has a value - hidden along with the rest of
   an empty field's controls, tracked live via the same data-empty attribute "Hide empty" already maintains. */
.dmf-field-item[data-empty="true"] .dmf-notes-toggle { display: none; }
</style>

<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-tags text-primary"></i>
    <span class="fw-semibold">Destination Metadata</span>
    <div class="ms-auto d-flex flex-wrap gap-2 align-items-center">
      <c:if test="${not canEditMeta}">
      <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dmfDownloadJson()" title="Download metadata as JSON">
        <i class="bi bi-download me-1"></i>JSON
      </button>
      </c:if>
      <c:if test="${canEditMeta && monitorActivated}">
      <div class="btn-group btn-group-sm" role="group" aria-label="Opsview Notes">
        <button type="button" class="btn btn-outline-secondary" id="dmfPreviewNotesBtn"
                onclick="dmfPreviewNotes()" title="Preview fields flagged &quot;Include in Notes&quot; as an Opsview note">
          <i class="bi bi-eye me-1"></i>Preview Notes
        </button>
        <button type="button" class="btn btn-outline-secondary" id="dmfExportNotesBtn"
                onclick="dmfExportNotes()" title="Export fields flagged &quot;Include in Notes&quot; to Opsview">
          <i class="bi bi-send-check me-1"></i>Export Notes
        </button>
      </div>
      </c:if>
      <c:if test="${canEditMeta}">
      <div class="btn-group btn-group-sm">
        <button type="button" class="btn btn-primary" id="dmfSaveBtn" onclick="dmfSave()" disabled>
          <i class="bi bi-floppy me-1"></i>Save
        </button>
        <button type="button" class="btn btn-outline-secondary" onclick="dmfDownloadJson()" title="Download metadata as JSON">
          <i class="bi bi-download me-1"></i>JSON
        </button>
        <a href="<c:url value='/do/transfer/destination/metadata/import/${destination.name}'/>"
           class="btn btn-outline-secondary">
          <i class="bi bi-upload me-1"></i>Import XML
        </a>
      </div>
      </c:if>
      <c:if test="${hasMarkdownField}">
      <button type="button" class="btn btn-sm btn-outline-info" data-bs-toggle="offcanvas"
              data-bs-target="#dmfMarkdownGuideOffcanvas" title="Open Markdown Guide">
        <i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Markdown </span>Guide
      </button>
      </c:if>
      <button type="button" class="btn btn-sm btn-outline-secondary" id="dmfHideEmptyBtn"
              onclick="dmfToggleEmpty()" title="Toggle visibility of empty fields and cards">
        <i class="bi bi-eye-slash me-1" id="dmfHideEmptyIcon"></i><span id="dmfHideEmptyLabel">Hide empty</span>
      </button>
    </div>
  </div>

  <div class="card-body p-3" id="dmfForm">
    <c:if test="${empty metaFields}">
      <div class="alert alert-info d-flex align-items-center gap-2 mb-0">
        <i class="bi bi-info-circle-fill"></i>
        <span>No metadata fields are configured for this destination type.
          <c:choose>
            <c:when test="${canEditMeta}">
              <a href="<bean:message key='admin.basepath'/>/metafields" class="alert-link">Configure fields in the Metadata Fields admin page.</a>
            </c:when>
            <c:otherwise>
              Contact your administrator.
            </c:otherwise>
          </c:choose>
        </span>
      </div>
    </c:if>

    <c:if test="${not empty metaFields}">
      <div class="d-flex flex-column gap-3">
      <c:set var="lastCategory" value=""/>
      <c:forEach var="field" items="${metaFields}">
        <c:if test="${field.category != lastCategory}">
          <c:if test="${lastCategory != ''}">
            <%-- close previous card's inner row, card-body, card --%>
            </div></div></div>
          </c:if>
          <div class="card border shadow-sm">
            <div class="card-header py-2 d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
              <i class="bi bi-folder2-open text-secondary"></i>
              <span class="fw-semibold small text-uppercase" style="letter-spacing:0.05em">${field.category}</span>
            </div>
            <div class="card-body p-3">
            <div class="row g-3">
          <c:set var="lastCategory" value="${field.category}"/>
        </c:if>

        <div class="dmf-field-item col-12<c:choose><c:when test="${field.type == 'markdown'}"></c:when><c:when test="${field.type == 'contact' or field.type == 'mail-group' or field.type == 'switchboard' or field.type == 'textarea'}"> col-md-6</c:when><c:otherwise> col-sm-6 col-lg-4</c:otherwise></c:choose>"
             id="dmf-group-${field.id}" data-type="${field.type}" data-max-occurs="${field.maxOccurs}">
          <div class="dmf-field-label d-flex align-items-center gap-1">
            <span class="flex-grow-1" style="min-width:0;">
              ${field.label}
              <c:if test="${not empty field.tooltip}">
                <i class="bi bi-question-circle text-muted ms-1 dmf-tip-icon" data-tip="${fn:escapeXml(field.tooltip)}" onclick="dmfTipToggle(this);event.stopPropagation();" style="cursor:pointer;font-weight:normal;font-size:0.8rem" tabindex="0"></i>
              </c:if>
            </span>
            <c:if test="${canEditMeta && field.type != 'password'}">
            <label class="dmf-notes-toggle d-flex align-items-center gap-1 flex-shrink-0"
                   title="Include this field's value(s) when exporting Opsview notes">
              <input type="checkbox" class="form-check-input" id="dmf-notes-${field.id}">Include in Notes
            </label>
            </c:if>
            <c:if test="${not field.editable}">
            <i class="bi bi-lock-fill text-muted flex-shrink-0" style="font-size:0.8rem"
               title="Not editable at destination level — value is set centrally on /do/admin/metafields"></i>
            </c:if>
          </div>
          <div id="dmf-values-${field.id}">
            <%-- Values rendered via JS from dmfData --%>
          </div>
          <c:if test="${canEditMeta && field.editable}">
          <c:if test="${field.maxOccurs == -1 || field.maxOccurs > 1}">
            <button type="button" class="btn btn-link btn-sm p-0 dmf-add mt-1"
                    onclick="dmfAddValue(${field.id}, '${field.type}')">
              <i class="bi bi-plus-circle me-1"></i>Add ${field.label}
            </button>
          </c:if>
          </c:if>
        </div>

      </c:forEach>
      <c:if test="${lastCategory != ''}">
        <%-- close last card's inner row, card-body, card --%>
        </div></div></div>
      </c:if>
      </div><%-- end d-flex flex-column gap-3 --%>
    </c:if>
  </div>
</div>

<%-- Shared Markdown guide, opened from the "Guide" button above every "markdown"-type editor (there can be
     several on the page, one per markdown field/row - they all target this single offcanvas). Styled to match
     the "Directory Guide" offcanvas on the Host page (directory_guide.jsp) for consistency between pages. --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="dmfMarkdownGuideOffcanvas"
     aria-labelledby="dmfMarkdownGuideLabel" style="width:600px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="dmfMarkdownGuideLabel">
      <i class="bi bi-markdown me-2 text-info"></i>Markdown &mdash; Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>Write this field using <a href="https://commonmark.org/help/" target="_blank" rel="noopener">Markdown</a>
      syntax on the left; the panel on the right shows a live preview of exactly what will be produced. When this
      field is flagged <strong>Include in Notes</strong>, that same rendering is sent to the Opsview note &mdash;
      what you see in the preview is what operators will see.</div>
    </div>

    <p class="small text-muted mb-3"><i class="bi bi-shield-check me-1"></i>Only the elements listed below are
    kept; anything else (raw HTML, images, footnotes, etc.) is stripped out when the preview/export is
    generated.</p>

    <div class="table-responsive mb-0">
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light"><tr><th style="width:45%">Type this</th><th>To get</th></tr></thead>
        <tbody>
          <tr><td><code># Heading 1</code><br><code>## Heading 2</code><br><code>### Heading 3</code></td><td>A section heading (levels 1-3)</td></tr>
          <tr><td><code>**bold text**</code></td><td><strong>bold text</strong></td></tr>
          <tr><td><code>*italic text*</code></td><td><em>italic text</em></td></tr>
          <tr><td><code>- item one<br>- item two</code></td><td>A bullet list</td></tr>
          <tr><td><code>1. item one<br>2. item two</code></td><td>A numbered list</td></tr>
          <tr><td><code>[link text](https://example.com)</code></td><td>A clickable link</td></tr>
          <tr><td><code>`inline code`</code></td><td><code>inline code</code></td></tr>
          <tr><td>A line starting with 4 spaces, or a fenced <code>```</code> block</td><td>A preformatted code block</td></tr>
          <tr><td>Two blank-line-separated lines of text</td><td>Separate paragraphs (a single line break is
              ignored, same as standard Markdown - leave a blank line between paragraphs, or end a line with
              two spaces to force a line break)</td></tr>
          <tr><td><code>| A | B |<br>|---|---|<br>| 1 | 2 |</code></td><td>A table (columns separated by
              <code>|</code>, a <code>|---|---|</code> row under the header)</td></tr>
        </tbody>
      </table>
    </div>

  </div>
</div>

<div class="modal fade" id="dmfNotesPreviewModal" tabindex="-1" aria-labelledby="dmfNotesPreviewLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="dmfNotesPreviewLabel">Opsview Notes Preview</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p class="small text-muted">Saved fields included in Notes, rendered with the same HTML used for export. Opsview's styling may differ.</p>
        <iframe id="dmfNotesPreviewFrame" title="Rendered Opsview notes" sandbox="allow-popups allow-popups-to-escape-sandbox"
                class="w-100 border rounded" style="height:55vh;background:white"></iframe>
      </div>
    </div>
  </div>
</div>

<script>
// Existing values map: fieldId -> [{id, value, position}, ...]
var dmfData = {};
var dmfCanEdit = ${canEditMeta};
<c:forEach var="val" items="${metaValues}"><%
  ecmwf.common.database.DestinationMetaValue _v =
      (ecmwf.common.database.DestinationMetaValue) pageContext.getAttribute("val");
  String _raw = _v != null && _v.getValue() != null ? _v.getValue() : "";
  String _json = "\"" + _raw.replace("\\","\\\\").replace("\"","\\\"")
                            .replace("\n","\\n").replace("\r","\\r")
                            .replace("\t","\\t") + "\"";
%>
  if (!dmfData[${val.fieldId}]) dmfData[${val.fieldId}] = [];
  dmfData[${val.fieldId}].push({id: ${val.id}, value: <%= _json %>, position: ${val.position}, includeInNotes: ${val.includeInNotes}});
</c:forEach>

// Field definitions' default value (used to seed a field that has no value yet for this destination) and
// whether each field may be customized at all at the destination level - see dmfRenderGroup()/dmfIsFieldEmpty().
var dmfDefaults = {};
var dmfEditableMap = {};
<c:forEach var="field" items="${metaFields}"><%
  ecmwf.common.database.DestinationMetaField _f =
      (ecmwf.common.database.DestinationMetaField) pageContext.getAttribute("field");
  String _rawDef = _f != null && _f.getDefaultValue() != null ? _f.getDefaultValue() : "";
  String _jsonDef = "\"" + _rawDef.replace("\\","\\\\").replace("\"","\\\"")
                            .replace("\n","\\n").replace("\r","\\r")
                            .replace("\t","\\t") + "\"";
%>
  dmfDefaults[${field.id}] = <%= _jsonDef %>;
  dmfEditableMap[${field.id}] = ${field.editable};
</c:forEach>

var dmfDestination = '${destination.name}';

// markdown-field Ace editor instances, keyed by their container element id ("ace_dmf_<fieldId>_<idx>") -
// tracked so dmfRenderGroup()/dmfRemoveRow() can destroy() them before removing their DOM node, since Ace
// keeps its own references/listeners alive otherwise.
var dmfAceEditors = {};

function dmfEscape(s) {
  return (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function dmfRenderInput(fieldId, fieldType, value, idx, forceReadOnly) {
  if (!dmfCanEdit || forceReadOnly) {
    // Read-only display
    if (fieldType === 'contact' || fieldType === 'switchboard') {
      var obj = {};
      try { obj = JSON.parse(value || '{}'); } catch(e) {}
      var lines = [];
      if (obj.name)  lines.push('<span class="fw-medium">' + dmfEscape(obj.name) + '</span>');
      if (obj.email) lines.push('<a href="mailto:' + dmfEscape(obj.email) + '" class="text-decoration-none">' + dmfEscape(obj.email) + '</a>');
      if (obj.phone) lines.push('<i class="bi bi-telephone me-1 text-muted"></i>' + dmfEscape(obj.phone));
      if (obj.fax)   lines.push('<i class="bi bi-printer me-1 text-muted"></i>' + dmfEscape(obj.fax));
      return lines.length ? '<div class="dmf-readonly-value">' + lines.join('<br>') + '</div>'
                          : '<span class="dmf-readonly-empty">—</span>';
    }
    if (fieldType === 'mail-group') {
      var obj = {};
      try { obj = JSON.parse(value || '{}'); } catch(e) {}
      var lines = [];
      if (obj.name)  lines.push('<span class="fw-medium">' + dmfEscape(obj.name) + '</span>');
      if (obj.email) lines.push('<a href="mailto:' + dmfEscape(obj.email) + '" class="text-decoration-none">' + dmfEscape(obj.email) + '</a>');
      return lines.length ? '<div class="dmf-readonly-value">' + lines.join('<br>') + '</div>'
                          : '<span class="dmf-readonly-empty">—</span>';
    }
    if (fieldType === 'email' && value) {
      return '<div class="dmf-readonly-value"><a href="mailto:' + dmfEscape(value) + '" class="text-decoration-none">' + dmfEscape(value) + '</a></div>';
    }
    if (fieldType === 'url' && value) {
      return '<div class="dmf-readonly-value"><a href="' + dmfEscape(value) + '" target="_blank" rel="noopener" class="text-decoration-none">' + dmfEscape(value) + ' <i class="bi bi-box-arrow-up-right" style="font-size:0.7rem"></i></a></div>';
    }
    if (fieldType === 'password') {
      return value ? '<div class="dmf-readonly-value text-muted fst-italic">••••••••</div>'
                   : '<span class="dmf-readonly-empty">—</span>';
    }
    if ((fieldType === 'textarea' || fieldType === 'markdown') && value) {
      return '<pre class="dmf-readonly-value mb-0" style="white-space:pre-wrap;font-size:0.85rem">' + dmfEscape(value) + '</pre>';
    }
    return value ? '<div class="dmf-readonly-value">' + dmfEscape(value) + '</div>'
                 : '<span class="dmf-readonly-empty">—</span>';
  }
  var name = 'dmf_' + fieldId + '_' + idx;
  if (fieldType === 'textarea') {
    return '<textarea class="form-control form-control-sm dmf-input" name="' + name + '" rows="3">' + dmfEscape(value) + '</textarea>';
  } else if (fieldType === 'markdown') {
    return '<div class="dmf-input">' +
      '<div class="row g-2">' +
      '<div class="col-12 col-md-6">' +
        '<div class="dmf-markdown-pane-label">Markdown</div>' +
        '<div class="dmf-markdown-editor" id="ace_' + name + '">' + dmfEscape(value) + '</div>' +
      '</div>' +
      '<div class="col-12 col-md-6">' +
        '<div class="dmf-markdown-pane-label">Preview</div>' +
        '<div class="dmf-markdown-preview" id="preview_' + name + '"><span class="text-muted fst-italic">Preview…</span></div>' +
      '</div>' +
      '</div>' +
      '<textarea name="' + name + '" style="display:none;">' + dmfEscape(value) + '</textarea>' +
      '</div>';
  } else if (fieldType === 'contact') {
    // JSON: {name, phone, fax, email}
    var obj = {};
    try { obj = JSON.parse(value || '{}'); } catch(e) {}
    return '<div class="border rounded p-2 bg-body-secondary dmf-input">' +
      '<div class="row g-1">' +
      '<div class="col-12 col-sm-6"><input type="text" class="form-control form-control-sm" placeholder="Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-12 col-sm-6"><input type="email" class="form-control form-control-sm" placeholder="Email" data-key="email" value="' + dmfEscape(obj.email||'') + '"></div>' +
      '<div class="col-12 col-sm-6"><input type="tel" class="form-control form-control-sm" placeholder="Phone" data-key="phone" value="' + dmfEscape(obj.phone||'') + '"></div>' +
      '<div class="col-12 col-sm-6"><input type="tel" class="form-control form-control-sm" placeholder="Fax" data-key="fax" value="' + dmfEscape(obj.fax||'') + '"></div>' +
      '</div></div>';
  } else if (fieldType === 'mail-group') {
    // JSON: {name, email}
    var obj = {};
    try { obj = JSON.parse(value || '{}'); } catch(e) {}
    return '<div class="border rounded p-2 bg-body-secondary dmf-input">' +
      '<div class="row g-1">' +
      '<div class="col-12 col-sm-6"><input type="text" class="form-control form-control-sm" placeholder="Group Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-12 col-sm-6"><input type="email" class="form-control form-control-sm" placeholder="Email" data-key="email" value="' + dmfEscape(obj.email||'') + '"></div>' +
      '</div></div>';
  } else if (fieldType === 'switchboard') {
    // JSON: {name, phone}
    var obj = {};
    try { obj = JSON.parse(value || '{}'); } catch(e) {}
    return '<div class="border rounded p-2 bg-body-secondary dmf-input">' +
      '<div class="row g-1">' +
      '<div class="col-12 col-sm-6"><input type="text" class="form-control form-control-sm" placeholder="Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-12 col-sm-6"><input type="tel" class="form-control form-control-sm" placeholder="Phone" data-key="phone" value="' + dmfEscape(obj.phone||'') + '"></div>' +
      '</div></div>';
  } else if (fieldType === 'url') {
    return '<input type="url" class="form-control form-control-sm dmf-input" name="' + name + '" value="' + dmfEscape(value) + '">';
  } else if (fieldType === 'email') {
    return '<input type="email" class="form-control form-control-sm dmf-input" name="' + name + '" value="' + dmfEscape(value) + '">';
  } else if (fieldType === 'phone') {
    return '<input type="tel" class="form-control form-control-sm dmf-input" name="' + name + '" value="' + dmfEscape(value) + '">';
  } else if (fieldType === 'password') {
    return '<input type="password" class="form-control form-control-sm dmf-input" name="' + name + '" autocomplete="off" value="' + dmfEscape(value) + '">';
  } else {
    return '<input type="text" class="form-control form-control-sm dmf-input" name="' + name + '" value="' + dmfEscape(value) + '">';
  }
}

function dmfReadInput(container, fieldType) {
  if (fieldType === 'textarea' || fieldType === 'markdown') {
    // For 'markdown', Ace creates its own hidden <textarea class="ace_text-input"> inside the editor (used
    // for keyboard/IME capture) which sits before our own sync textarea in document order - exclude it
    // explicitly so this always reads our textarea, not Ace's internal one (which is normally empty). A
    // locked field (see dmfRenderGroup()) renders read-only markup with no textarea at all - guard for that.
    var ta = container.querySelector('textarea:not(.ace_text-input)');
    return ta ? ta.value : '';
  } else if (fieldType === 'contact' || fieldType === 'mail-group' || fieldType === 'switchboard') {
    var keyed = container.querySelectorAll('[data-key]');
    if (keyed.length === 0) return ''; // locked field: no inputs rendered
    var obj = {};
    keyed.forEach(function(el) { obj[el.dataset.key] = el.value; });
    return JSON.stringify(obj);
  } else {
    return (container.querySelector('input') || {}).value || '';
  }
}

function dmfRenderGroup(fieldId, fieldType, maxOccurs) {
  var container = document.getElementById('dmf-values-' + fieldId);
  if (!container) return;
  dmfDestroyMarkdownEditors(container);
  // A field not editable at the destination level always shows its central Default Value, live - any value a
  // destination had saved before it was locked is ignored here (and is dropped on this destination's next save,
  // since dmfCollect() can't read a value back out of the read-only markup rendered below).
  var fieldEditable = dmfEditableMap[fieldId] !== false;
  var vals;
  if (fieldEditable) {
    vals = dmfData[fieldId] || [];
    if (vals.length === 0) vals = [{id:0, value: dmfDefaults[fieldId] || '', position:0}];
  } else {
    vals = [{id:0, value: dmfDefaults[fieldId] || '', position:0}];
  }
  var html = '';
  vals.forEach(function(v, i) {
    var canRemove = dmfCanEdit && fieldEditable && (maxOccurs === -1 || maxOccurs > 1);
    html += '<div class="dmf-row" data-idx="' + i + '">';
    html += dmfRenderInput(fieldId, fieldType, v.value, i, !fieldEditable);
    if (canRemove) {
      html += '<button type="button" class="btn btn-sm btn-outline-danger dmf-remove" onclick="dmfRemoveRow(this)" title="Remove"><i class="bi bi-trash"></i></button>';
    }
    html += '</div>';
  });
  container.innerHTML = html;
  if (fieldType === 'markdown' && fieldEditable) dmfInitMarkdownEditors(container);
}

function dmfAddValue(fieldId, fieldType) {
  if (!dmfData[fieldId]) dmfData[fieldId] = [];
  dmfData[fieldId].push({id:0, value:'', position: dmfData[fieldId].length});
  dmfRenderGroup(fieldId, fieldType, -1);
  dmfSetDirty();
}

function dmfRemoveRow(btn) {
  var row = btn.closest('.dmf-row');
  dmfDestroyMarkdownEditors(row);
  row.remove();
  dmfSetDirty();
}

// Destroys any Ace editor instances rooted under `scope` (a row or the whole values container) - must be
// called before that DOM subtree is discarded/replaced (dmfRenderGroup() re-render, dmfRemoveRow()), since
// Ace does not clean itself up when its element is simply detached.
function dmfDestroyMarkdownEditors(scope) {
  scope.querySelectorAll('[id^="ace_dmf_"]').forEach(function(el) {
    var editor = dmfAceEditors[el.id];
    if (editor) {
      // Also drop it from ecpds.js's global theme-switch registry (_ecpdsAceEditors), or a later
      // ecpdsUpdateAceTheme() call would call setTheme() on a destroyed editor.
      if (typeof _ecpdsAceEditors !== 'undefined') {
        var idx = _ecpdsAceEditors.indexOf(editor);
        if (idx !== -1) _ecpdsAceEditors.splice(idx, 1);
      }
      editor.destroy();
      delete dmfAceEditors[el.id];
    }
  });
}

// Initializes an Ace editor (Markdown mode) on every not-yet-initialized editor container under `scope`,
// wired to keep its paired hidden <textarea> (read by dmfReadInput()/dmfCollect()) in sync on every
// keystroke, and to refresh its preview pane - debounced, via the previewmarkdown endpoint so the preview is
// rendered by the exact same code as the Opsview export (see MarkdownUtil).
function dmfInitMarkdownEditors(scope) {
  scope.querySelectorAll('[id^="ace_dmf_"]').forEach(function(el) {
    if (dmfAceEditors[el.id]) return;
    var name = el.id.substring('ace_'.length);
    var textarea = scope.querySelector('textarea[name="' + name + '"]');
    var preview = document.getElementById('preview_' + name);
    var editor = getEditorProperties(false, false, el.id, 'markdown');
    dmfAceEditors[el.id] = editor;
    // Not calling makeResizable() here: its trim-on-init would desync the hidden textarea (seeded with
    // the untrimmed value) from what Ace displays until the next real edit, and its resizable-splitter
    // mouseup handling doesn't apply to this fixed-grid layout.
    var debounceTimer = null;
    editor.getSession().on('change', function() {
      var md = editor.getSession().getValue();
      if (textarea) textarea.value = md;
      dmfSetDirty();
      // Ace doesn't dispatch a native input/change event that bubbles to #dmfForm, unlike every other
      // field type - so the live "re-evaluate empty fields" handler bound there (see DOMContentLoaded
      // below) never runs for this editor. Without this, a field that started empty keeps its "Include
      // in Notes" checkbox hidden (see .dmf-field-item[data-empty="true"]) even after typing content in.
      dmfMarkEmpty();
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(function() { dmfPreviewMarkdown(md, preview); }, 400);
    });
    dmfPreviewMarkdown(editor.getSession().getValue(), preview); // initial render, not debounced
  });
}

function dmfPreviewMarkdown(markdown, previewEl) {
  if (!previewEl) return;
  fetch('<c:url value="/do/transfer/destination/metadata/previewmarkdown"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({markdown: markdown})
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.success) {
        previewEl.innerHTML = (markdown || '').trim()
          ? data.html : '<span class="text-muted fst-italic">Nothing to preview</span>';
      } else {
        previewEl.innerHTML = '<span class="text-danger">Preview error: ' + dmfEscape(data.error || 'unknown') + '</span>';
      }
    }).catch(function() {
      previewEl.innerHTML = '<span class="text-danger">Network error</span>';
    });
}

function dmfCollect() {
  var result = [];
  document.querySelectorAll('[id^="dmf-values-"]').forEach(function(container) {
    var fieldId = parseInt(container.id.replace('dmf-values-',''));
    var group = container.closest('[id^="dmf-group-"]');
    var fieldType = group ? (group.dataset.type || 'text') : 'text';
    var fieldEditable = dmfEditableMap[fieldId] !== false;
    var notesCheckbox = document.getElementById('dmf-notes-' + fieldId);
    var includeInNotes = !!(notesCheckbox && notesCheckbox.checked);
    var rows = container.querySelectorAll('.dmf-row');
    rows.forEach(function(row, pos) {
      // A locked field renders no input at all (see dmfRenderGroup()), so dmfReadInput() always returns ''
      // here - substitute the live Default Value as a placeholder so "Include in Notes" can still be toggled
      // and persisted for this destination. The export itself (ExportDestinationMetaNotesAction) ignores
      // whatever ends up stored for DMV_VALUE on a locked field and always substitutes the live Default Value
      // too, so this placeholder never actually surfaces anywhere by itself - it only keeps the row alive.
      var val = fieldEditable ? dmfReadInput(row, fieldType) : (dmfDefaults[fieldId] || '');
      if (val && val.trim()) {
        result.push({DMF_ID: fieldId, DMV_VALUE: val, DMV_POSITION: pos, DMV_INCLUDE_IN_NOTES: includeInNotes});
      }
    });
  });
  return result;
}

function dmfSave() {
  var btn = document.getElementById('dmfSaveBtn');
  btn.disabled = true;
  var values = dmfCollect();
  fetch('<c:url value="/do/transfer/destination/metadata/save"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({destination: dmfDestination, values: values})
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.success) {
        dmfClearDirty();
        if (_dmfHideEmpty) { dmfMarkEmpty(); dmfApplyHideEmpty(true); }
        showToast('Metadata saved', 'success');
      } else {
        showToast('Error: ' + (data.error || 'unknown'), 'danger');
        btn.disabled = false;
      }
    }).catch(function(e) {
      showToast('Network error', 'danger');
      btn.disabled = false;
    });
}

// Exports every field flagged "Include in Notes" (see dmfCollect()'s DMV_INCLUDE_IN_NOTES) as an Opsview
// note for this destination. Acts on the last *saved* state (like Import XML already does), not any
// in-progress unsaved edits - the button is disabled while dirty (see dmfSetDirty()/dmfClearDirty()) so
// this is never ambiguous.
function dmfPreviewNotes() {
  var btn = document.getElementById('dmfPreviewNotesBtn');
  if (!btn || _dmfDirty) return;
  btn.disabled = true;
  fetch('<c:url value="/do/transfer/destination/metadata/previewnotes"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({destination: dmfDestination})
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      btn.disabled = _dmfDirty;
      if (!data.success) {
        showToast('Preview error: ' + (data.error || 'unknown'), 'danger');
        return;
      }
      document.getElementById('dmfNotesPreviewFrame').srcdoc =
        '<!doctype html><html><head><meta charset="UTF-8"></head><body>'
        + (data.html || '<p>No saved fields with non-empty values are included in Notes.</p>')
        + '</body></html>';
      bootstrap.Modal.getOrCreateInstance(document.getElementById('dmfNotesPreviewModal')).show();
    }).catch(function() {
      btn.disabled = _dmfDirty;
      showToast('Unable to load Notes preview', 'danger');
    });
}

function dmfExportNotes() {
  var btn = document.getElementById('dmfExportNotesBtn');
  if (!btn) return;
  btn.disabled = true;
  fetch('<c:url value="/do/transfer/destination/metadata/exportnotes"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({destination: dmfDestination})
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      btn.disabled = _dmfDirty;
      if (data.success) {
        showToast('Notes exported to Opsview', 'success');
      } else {
        showToast('Error: ' + (data.error || 'unknown'), 'danger');
      }
    }).catch(function(e) {
      btn.disabled = _dmfDirty;
      showToast('Network error', 'danger');
    });
}

// Initial render
// Lightweight click-tooltip for metadata field ? icons
(function() {
  var tip = null;
  var currentIcon = null;
  function getTip() {
    if (!tip) {
      tip = document.createElement('div');
      tip.id = 'dmfFieldTip';
      tip.style.cssText = 'position:absolute;background:#333;color:#fff;padding:5px 10px;border-radius:4px;'
        + 'font-size:0.8rem;line-height:1.4;z-index:9999;max-width:280px;pointer-events:none;'
        + 'box-shadow:0 2px 8px rgba(0,0,0,.35);';
      document.body.appendChild(tip);
    }
    return tip;
  }
  window.dmfTipToggle = function(icon) {
    var t = getTip();
    if (currentIcon === icon && t.style.display !== 'none') {
      t.style.display = 'none';
      currentIcon = null;
      return;
    }
    t.textContent = icon.dataset.tip || '';
    t.style.display = 'block';
    currentIcon = icon;
    var rect = icon.getBoundingClientRect();
    var scrollX = window.scrollX || window.pageXOffset;
    var scrollY = window.scrollY || window.pageYOffset;
    t.style.left = (rect.left + scrollX) + 'px';
    t.style.top  = (rect.bottom + scrollY + 4) + 'px';
    // Clamp to viewport width
    var tipW = t.offsetWidth;
    var vw = document.documentElement.clientWidth;
    if (rect.left + tipW > vw - 8) {
      t.style.left = Math.max(4, vw - tipW - 8 + scrollX) + 'px';
    }
  };
  document.addEventListener('click', function(e) {
    if (tip && tip.style.display !== 'none' && !e.target.classList.contains('dmf-tip-icon')) {
      tip.style.display = 'none';
      currentIcon = null;
    }
  });
}());

var _dmfDirty = false;
// JSON snapshot of dmfCollect() as of the last load/save - compared against the live form on every edit
// (see dmfSetDirty()) so Save only looks dirty when something actually differs from what's stored, rather
// than staying dirty forever after the first touch (e.g. ticking then unticking "Include in Notes", or
// adding then removing a still-empty row, now correctly nets out to "nothing to save").
var _dmfSavedSnapshot = '';

function dmfApplyDirtyUi(dirty) {
  var previewBtn = document.getElementById('dmfPreviewNotesBtn');
  if (previewBtn) {
    previewBtn.disabled = dirty;
    previewBtn.title = dirty ? 'Save your changes first' : 'Preview fields flagged "Include in Notes" as an Opsview note';
  }
  var exportBtn = document.getElementById('dmfExportNotesBtn');
  if (exportBtn) {
    exportBtn.disabled = dirty;
    exportBtn.title = dirty ? 'Save your changes first' : 'Export fields flagged "Include in Notes" to Opsview';
  }
  _dmfDirty = dirty;
  var btn = document.getElementById('dmfSaveBtn');
  if (btn) {
    btn.disabled = !dirty;
    btn.classList.toggle('btn-warning', dirty);
    btn.classList.toggle('btn-primary', !dirty);
    btn.title = dirty ? 'You have unsaved changes' : '';
  }
}

function dmfSetDirty() {
  dmfApplyDirtyUi(JSON.stringify(dmfCollect()) !== _dmfSavedSnapshot);
}

function dmfClearDirty() {
  _dmfSavedSnapshot = JSON.stringify(dmfCollect());
  dmfApplyDirtyUi(false);
}

// Intercept all anchor navigation when there are unsaved changes
document.addEventListener('click', function(e) {
  if (!_dmfDirty) return;
  var anchor = e.target.closest('a[href]');
  if (!anchor) return;
  var href = anchor.getAttribute('href');
  if (!href || href === '#' || href.startsWith('javascript:') || href.startsWith('mailto:')) return;
  e.preventDefault();
  e.stopImmediatePropagation();
  var target = href; // capture for closure
  confirmationDialog({
    title: '<i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>Unsaved Changes',
    message: 'You have unsaved metadata changes. If you leave now, your changes will be lost.',
    confirmText: 'Leave without saving',
    cancelText: 'Stay and save',
    showLoading: false,
    onConfirm: function() { window.location.href = target; }
  });
}, true);

var _dmfHideEmpty = false;

function dmfIsFieldEmpty(fieldId) {
  // A locked field is always rendered from its (live) Default Value, as read-only markup with no
  // input/textarea at all - check that directly rather than falling into either branch below.
  if (dmfEditableMap[fieldId] === false) {
    var def = dmfDefaults[fieldId] || '';
    return !def || !def.trim();
  }
  // In edit mode, check actual live input values in the DOM
  if (dmfCanEdit) {
    var container = document.getElementById('dmf-values-' + fieldId);
    if (!container) return true;
    // Exclude Ace's own hidden "ace_text-input" textarea (see dmfReadInput()) - it is not one of our
    // actual value inputs and is normally empty, which would otherwise make a filled-in markdown field
    // look empty here.
    var inputs = container.querySelectorAll('input:not(.ace_text-input), textarea:not(.ace_text-input)');
    if (inputs.length === 0) return true;
    return Array.from(inputs).every(function(el) { return !el.value || !el.value.trim(); });
  }
  // In read-only mode, check server-loaded data
  var vals = dmfData[fieldId] || [];
  return vals.length === 0 || vals.every(function(v) { return !v.value || !v.value.trim(); });
}

function dmfMarkEmpty() {
  document.querySelectorAll('[id^="dmf-group-"]').forEach(function(group) {
    var fieldId = parseInt(group.id.replace('dmf-group-',''));
    group.dataset.empty = dmfIsFieldEmpty(fieldId) ? 'true' : 'false';
  });
}

function dmfApplyHideEmpty(hide) {
  // Toggle individual empty fields
  document.querySelectorAll('[id^="dmf-group-"]').forEach(function(group) {
    if (group.dataset.empty === 'true') {
      group.dataset.dmfHidden = hide ? 'true' : 'false';
      group.style.display = hide ? 'none' : '';
    }
  });
  // Toggle category cards that have no visible (non-empty) fields
  document.querySelectorAll('.card.border.shadow-sm').forEach(function(card) {
    var hasVisible = Array.from(card.querySelectorAll('[id^="dmf-group-"]'))
      .some(function(g) { return g.dataset.empty !== 'true' || !hide; });
    card.style.display = (!hasVisible && hide) ? 'none' : '';
  });
}

function dmfUpdateHideBtn() {
  var icon = document.getElementById('dmfHideEmptyIcon');
  var label = document.getElementById('dmfHideEmptyLabel');
  var btn = document.getElementById('dmfHideEmptyBtn');
  if (_dmfHideEmpty) {
    icon.className = 'bi bi-eye me-1';
    label.textContent = 'Show all';
    btn.classList.remove('btn-outline-secondary');
    btn.classList.add('btn-outline-primary');
  } else {
    icon.className = 'bi bi-eye-slash me-1';
    label.textContent = 'Hide empty';
    btn.classList.remove('btn-outline-primary');
    btn.classList.add('btn-outline-secondary');
  }
}

function dmfToggleEmpty() {
  _dmfHideEmpty = !_dmfHideEmpty;
  try { localStorage.setItem('dmfHideEmpty', _dmfHideEmpty); } catch(e) {}
  dmfMarkEmpty(); // re-evaluate from live DOM before applying
  dmfApplyHideEmpty(_dmfHideEmpty);
  dmfUpdateHideBtn();
}

document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('[id^="dmf-group-"]').forEach(function(group) {
    var fieldId = parseInt(group.id.replace('dmf-group-',''));
    var fieldType = group.dataset.type || 'text';
    var maxOccurs = parseInt(group.dataset.maxOccurs || '1');
    dmfRenderGroup(fieldId, fieldType, maxOccurs);
    // Seed the "Include in Notes" checkbox from the loaded values (all rows of a field are forced to
    // carry the same flag on save - see dmfCollect() - so the first row is representative).
    var notesCheckbox = document.getElementById('dmf-notes-' + fieldId);
    if (notesCheckbox) {
      var vals = dmfData[fieldId] || [];
      notesCheckbox.checked = vals.length > 0 && !!vals[0].includeInNotes;
    }
  });
  // Detect any input/change in the form and re-evaluate dirty state; also re-evaluate which fields are
  // empty so the "Include in Notes" checkbox (hidden for empty fields, see the .dmf-notes-check CSS rule)
  // appears/disappears live while typing rather than only when "Hide empty" is toggled.
  if (dmfCanEdit) {
    _dmfSavedSnapshot = JSON.stringify(dmfCollect()); // baseline = what was just loaded
    document.getElementById('dmfForm').addEventListener('input', function() { dmfSetDirty(); dmfMarkEmpty(); });
    document.getElementById('dmfForm').addEventListener('change', function() { dmfSetDirty(); dmfMarkEmpty(); });
  }
  // Mark empty fields and restore hide-empty state
  dmfMarkEmpty();
  var hideEmpty = false;
  try { hideEmpty = localStorage.getItem('dmfHideEmpty') === 'true'; } catch(e) {}
  if (hideEmpty) { dmfApplyHideEmpty(true); _dmfHideEmpty = true; dmfUpdateHideBtn(); }
});

// Field definitions index (populated from JSTL below)
var dmfFieldIndex = {};
<c:forEach var="field" items="${metaFields}">
  dmfFieldIndex[${field.id}] = {
    name: '${field.name}',
    label: '<c:out value="${field.label}" escapeXml="false"/>'.replace(/'/g,"'"),
    type: '${field.type}',
    category: '${field.category}'
  };
</c:forEach>

function dmfDownloadJson() {
  // Build JSON from dmfData (the raw values loaded from the server) and dmfFieldIndex.
  // This works in both read-only and edit modes, and always includes all fields
  // (even those with no values yet) grouped by category.
  var result = {
    destination: dmfDestination,
    exportedAt: new Date().toISOString(),
    metadata: {}
  };

  Object.keys(dmfFieldIndex).forEach(function(fieldId) {
    var fieldInfo = dmfFieldIndex[fieldId];
    var fieldType = fieldInfo.type || 'text';
    var key = fieldInfo.name || ('field_' + fieldId);
    var category = fieldInfo.category || 'General';
    // A locked field has no per-destination value at all (see dmfRenderGroup()) - it always reflects its
    // central Default Value, so substitute that here rather than any stale/absent dmfData entry.
    var raw = dmfEditableMap[parseInt(fieldId)] === false
      ? [{value: dmfDefaults[parseInt(fieldId)] || ''}]
      : (dmfData[parseInt(fieldId)] || []);
    var values = [];
    raw.forEach(function(entry) {
      var val = entry.value;
      if (val && val.trim()) {
        if (fieldType === 'contact' || fieldType === 'mail-group' || fieldType === 'switchboard') {
          try {
            var obj = JSON.parse(val);
            // Skip objects where every field is empty
            if (Object.values(obj).some(function(v) { return v && v.trim(); })) {
              val = obj;
            } else {
              return; // all empty, skip
            }
          } catch(e) {}
        }
        values.push(val);
      }
    });
    if (!result.metadata[category]) result.metadata[category] = {};
    result.metadata[category][key] = values.length === 0 ? null : values.length === 1 ? values[0] : values;
  });

  var json = JSON.stringify(result, null, 2);
  var blob = new Blob([json], {type: 'application/json'});
  var url = URL.createObjectURL(blob);
  var a = document.createElement('a');
  a.href = url;
  a.download = dmfDestination + '_metadata.json';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
</script>
