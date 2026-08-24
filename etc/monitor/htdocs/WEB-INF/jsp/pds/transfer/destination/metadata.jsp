<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<style>
.dmf-row { display: flex; gap: 0.5rem; align-items: flex-start; margin-bottom: 0.4rem; }
.dmf-row .dmf-input { flex: 1; min-width: 0; }
.dmf-row .dmf-remove { flex: 0 0 auto; }
.dmf-add { font-size: 0.8rem; }
.dmf-save-status { font-size: 0.8rem; }
.dmf-field-item { display: flex; flex-direction: column; gap: 0.25rem; }
.dmf-field-label { font-size: 0.82rem; font-weight: 600; color: var(--bs-body-color); }
.dmf-readonly-value { font-size: 0.9rem; padding: 0.15rem 0; color: var(--bs-body-color); word-break: break-word; }
.dmf-readonly-empty { font-size: 0.85rem; color: var(--bs-secondary-color); font-style: italic; }
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
      <span id="dmfSaveStatus" class="dmf-save-status text-muted"></span>
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

        <div class="dmf-field-item col-12<c:choose><c:when test="${field.type == 'contact' or field.type == 'mail-group' or field.type == 'switchboard' or field.type == 'textarea'}"> col-md-6</c:when><c:otherwise> col-sm-6 col-lg-4</c:otherwise></c:choose>"
             id="dmf-group-${field.id}" data-type="${field.type}" data-max-occurs="${field.maxOccurs}">
          <div class="dmf-field-label">
            ${field.label}
            <c:if test="${not empty field.tooltip}">
              <i class="bi bi-question-circle text-muted ms-1 dmf-tip-icon" data-tip="${field.tooltip}" onclick="dmfTipToggle(this);event.stopPropagation();" style="cursor:pointer;font-weight:normal;font-size:0.8rem" tabindex="0"></i>
            </c:if>
          </div>
          <div id="dmf-values-${field.id}">
            <%-- Values rendered via JS from dmfData --%>
          </div>
          <c:if test="${canEditMeta}">
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
  dmfData[${val.fieldId}].push({id: ${val.id}, value: <%= _json %>, position: ${val.position}});
</c:forEach>

var dmfDestination = '${destination.name}';

function dmfEscape(s) {
  return (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function dmfRenderInput(fieldId, fieldType, value, idx) {
  if (!dmfCanEdit) {
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
    if (fieldType === 'textarea' && value) {
      return '<pre class="dmf-readonly-value mb-0" style="white-space:pre-wrap;font-size:0.85rem">' + dmfEscape(value) + '</pre>';
    }
    return value ? '<div class="dmf-readonly-value">' + dmfEscape(value) + '</div>'
                 : '<span class="dmf-readonly-empty">—</span>';
  }
  var name = 'dmf_' + fieldId + '_' + idx;
  if (fieldType === 'textarea') {
    return '<textarea class="form-control form-control-sm dmf-input" name="' + name + '" rows="3">' + dmfEscape(value) + '</textarea>';
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
  if (fieldType === 'textarea') {
    return container.querySelector('textarea').value;
  } else if (fieldType === 'contact' || fieldType === 'mail-group' || fieldType === 'switchboard') {
    var obj = {};
    container.querySelectorAll('[data-key]').forEach(function(el) { obj[el.dataset.key] = el.value; });
    return JSON.stringify(obj);
  } else {
    return (container.querySelector('input') || {}).value || '';
  }
}

function dmfRenderGroup(fieldId, fieldType, maxOccurs) {
  var container = document.getElementById('dmf-values-' + fieldId);
  if (!container) return;
  var vals = dmfData[fieldId] || [];
  if (vals.length === 0) vals = [{id:0, value:'', position:0}];
  var html = '';
  vals.forEach(function(v, i) {
    var canRemove = dmfCanEdit && (maxOccurs === -1 || maxOccurs > 1);
    html += '<div class="dmf-row" data-idx="' + i + '">';
    html += dmfRenderInput(fieldId, fieldType, v.value, i);
    if (canRemove) {
      html += '<button type="button" class="btn btn-sm btn-outline-danger dmf-remove" onclick="dmfRemoveRow(this)" title="Remove"><i class="bi bi-trash"></i></button>';
    }
    html += '</div>';
  });
  container.innerHTML = html;
}

function dmfAddValue(fieldId, fieldType) {
  if (!dmfData[fieldId]) dmfData[fieldId] = [];
  dmfData[fieldId].push({id:0, value:'', position: dmfData[fieldId].length});
  dmfRenderGroup(fieldId, fieldType, -1);
  dmfSetDirty();
}

function dmfRemoveRow(btn) {
  btn.closest('.dmf-row').remove();
  dmfSetDirty();
}

function dmfCollect() {
  var result = [];
  document.querySelectorAll('[id^="dmf-values-"]').forEach(function(container) {
    var fieldId = parseInt(container.id.replace('dmf-values-',''));
    var group = container.closest('[id^="dmf-group-"]');
    var fieldType = group ? (group.dataset.type || 'text') : 'text';
    var rows = container.querySelectorAll('.dmf-row');
    rows.forEach(function(row, pos) {
      var val = dmfReadInput(row, fieldType);
      if (val && val.trim()) {
        result.push({DMF_ID: fieldId, DMV_VALUE: val, DMV_POSITION: pos});
      }
    });
  });
  return result;
}

function dmfSave() {
  var btn = document.getElementById('dmfSaveBtn');
  var status = document.getElementById('dmfSaveStatus');
  btn.disabled = true;
  status.textContent = 'Saving...';
  status.className = 'dmf-save-status text-muted';
  var values = dmfCollect();
  fetch('<c:url value="/do/transfer/destination/metadata/save"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({destination: dmfDestination, values: values})
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.success) {
        dmfClearDirty();
        status.textContent = 'Saved \u2713';
        status.className = 'dmf-save-status text-success';
        if (_dmfHideEmpty) { dmfMarkEmpty(); dmfApplyHideEmpty(true); }
        setTimeout(function(){ status.textContent=''; }, 4000);
      } else {
        status.textContent = 'Error: ' + (data.error || 'unknown');
        status.className = 'dmf-save-status text-danger';
        btn.disabled = false;
      }
    }).catch(function(e) {
      status.textContent = 'Network error';
      status.className = 'dmf-save-status text-danger';
      btn.disabled = false;
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

function dmfSetDirty() {
  if (_dmfDirty) return;
  _dmfDirty = true;
  var btn = document.getElementById('dmfSaveBtn');
  if (btn) {
    btn.disabled = false;
    btn.classList.remove('btn-primary');
    btn.classList.add('btn-warning');
    btn.title = 'You have unsaved changes';
  }
  var status = document.getElementById('dmfSaveStatus');
  if (status) {
    status.textContent = 'Unsaved changes';
    status.className = 'dmf-save-status text-warning fw-semibold';
  }
}

function dmfClearDirty() {
  _dmfDirty = false;
  var btn = document.getElementById('dmfSaveBtn');
  if (btn) {
    btn.disabled = true;
    btn.classList.remove('btn-warning');
    btn.classList.add('btn-primary');
    btn.title = '';
  }
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
  // In edit mode, check actual live input values in the DOM
  if (dmfCanEdit) {
    var container = document.getElementById('dmf-values-' + fieldId);
    if (!container) return true;
    var inputs = container.querySelectorAll('input, textarea');
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
  });
  // Detect any input/change in the form and mark dirty
  if (dmfCanEdit) {
    document.getElementById('dmfForm').addEventListener('input', dmfSetDirty);
    document.getElementById('dmfForm').addEventListener('change', dmfSetDirty);
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
    var raw = dmfData[parseInt(fieldId)] || [];
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

