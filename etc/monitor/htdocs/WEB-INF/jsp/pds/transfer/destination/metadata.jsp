<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<style>
.dmf-section { margin-bottom: 1.5rem; }
.dmf-section-title { font-size: 0.85rem; font-weight: 600; text-transform: uppercase;
    letter-spacing: 0.05em; color: var(--bs-secondary-color); margin-bottom: 0.5rem;
    padding-bottom: 0.25rem; border-bottom: 1px solid var(--bs-border-color); }
.dmf-row { display: flex; gap: 0.5rem; align-items: flex-start; margin-bottom: 0.4rem; }
.dmf-row .dmf-input { flex: 1; }
.dmf-row .dmf-remove { flex: 0 0 auto; }
.dmf-add { font-size: 0.8rem; }
.dmf-save-status { font-size: 0.8rem; }
</style>

<div class="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
  <h6 class="mb-0 fw-semibold"><i class="bi bi-tags me-2"></i>Destination Metadata</h6>
  <div class="d-flex gap-2 align-items-center">
    <span id="dmfSaveStatus" class="dmf-save-status text-muted"></span>
    <auth:if basePathKey="admin.basepath" paths="/metafields">
    <auth:then>
    <button type="button" class="btn btn-sm btn-primary" id="dmfSaveBtn" onclick="dmfSave()">
      <i class="bi bi-floppy me-1"></i>Save
    </button>
    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dmfDownloadJson()" title="Download metadata as JSON">
      <i class="bi bi-download me-1"></i>JSON
    </button>
    <a href="<c:url value='/do/transfer/destination/metadata/import/${destination.name}'/>"
       class="btn btn-sm btn-outline-secondary">
      <i class="bi bi-upload me-1"></i>Import XML
    </a>
    </auth:then>
    <auth:else>
    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dmfDownloadJson()" title="Download metadata as JSON">
      <i class="bi bi-download me-1"></i>JSON
    </button>
    </auth:else>
    </auth:if>
  </div>
</div>

<div id="dmfForm">
  <c:if test="${empty metaFields}">
    <div class="alert alert-info d-flex align-items-center gap-2">
      <i class="bi bi-info-circle-fill"></i>
      <span>No metadata fields are configured for this destination type.
        <auth:if basePathKey="admin.basepath" paths="/metafields">
        <auth:then>
          <a href="<bean:message key='admin.basepath'/>/metafields" class="alert-link">Configure fields in the Metadata Fields admin page.</a>
        </auth:then>
        <auth:else>
          Contact your administrator.
        </auth:else>
        </auth:if>
      </span>
    </div>
  </c:if>

  <c:if test="${not empty metaFields}">
    <c:set var="lastCategory" value=""/>
    <c:forEach var="field" items="${metaFields}">
      <c:if test="${field.category != lastCategory}">
        <c:if test="${lastCategory != ''}"></div></c:if>
        <div class="dmf-section">
        <div class="dmf-section-title">${field.category}</div>
        <c:set var="lastCategory" value="${field.category}"/>
      </c:if>

      <div class="mb-2" id="dmf-group-${field.id}" data-type="${field.type}" data-max-occurs="${field.maxOccurs}">
        <label class="form-label mb-1 small fw-medium">
          ${field.label}
          <c:if test="${not empty field.tooltip}">
            <i class="bi bi-question-circle text-muted ms-1" title="${field.tooltip}"></i>
          </c:if>
        </label>
        <div id="dmf-values-${field.id}">
          <%-- Values rendered via JS from dmfData --%>
        </div>
        <auth:if basePathKey="admin.basepath" paths="/metafields">
        <auth:then>
        <c:if test="${field.maxOccurs == -1 || field.maxOccurs > 1}">
          <button type="button" class="btn btn-link btn-sm p-0 dmf-add mt-1"
                  onclick="dmfAddValue(${field.id}, '${field.type}')">
            <i class="bi bi-plus-circle me-1"></i>Add ${field.label}
          </button>
        </c:if>
        </auth:then>
        </auth:if>
      </div>
    </c:forEach>
    <c:if test="${lastCategory != ''}"></div></c:if>
  </c:if>
</div>

<script>
// Existing values map: fieldId -> [{id, value, position}, ...]
var dmfData = {};
var dmfCanEdit = <auth:if basePathKey="admin.basepath" paths="/metafields"><auth:then>true</auth:then><auth:else>false</auth:else></auth:if>;
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
    if (fieldType === 'contact' || fieldType === 'mail-group' || fieldType === 'switchboard') {
      var obj = {};
      try { obj = JSON.parse(value || '{}'); } catch(e) {}
      var parts = Object.values(obj).filter(function(v){ return v && v.trim(); });
      return '<span class="form-control-plaintext form-control-sm dmf-input py-0">'
           + dmfEscape(parts.join(' · ') || '—') + '</span>';
    }
    return '<span class="form-control-plaintext form-control-sm dmf-input py-0">'
         + (value ? dmfEscape(value) : '<span class="text-muted fst-italic">—</span>') + '</span>';
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
      '<div class="col-6"><input type="text" class="form-control form-control-sm" placeholder="Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-6"><input type="email" class="form-control form-control-sm" placeholder="Email" data-key="email" value="' + dmfEscape(obj.email||'') + '"></div>' +
      '<div class="col-6"><input type="tel" class="form-control form-control-sm" placeholder="Phone" data-key="phone" value="' + dmfEscape(obj.phone||'') + '"></div>' +
      '<div class="col-6"><input type="tel" class="form-control form-control-sm" placeholder="Fax" data-key="fax" value="' + dmfEscape(obj.fax||'') + '"></div>' +
      '</div></div>';
  } else if (fieldType === 'mail-group') {
    // JSON: {name, email}
    var obj = {};
    try { obj = JSON.parse(value || '{}'); } catch(e) {}
    return '<div class="border rounded p-2 bg-body-secondary dmf-input">' +
      '<div class="row g-1">' +
      '<div class="col-6"><input type="text" class="form-control form-control-sm" placeholder="Group Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-6"><input type="email" class="form-control form-control-sm" placeholder="Email" data-key="email" value="' + dmfEscape(obj.email||'') + '"></div>' +
      '</div></div>';
  } else if (fieldType === 'switchboard') {
    // JSON: {name, phone}
    var obj = {};
    try { obj = JSON.parse(value || '{}'); } catch(e) {}
    return '<div class="border rounded p-2 bg-body-secondary dmf-input">' +
      '<div class="row g-1">' +
      '<div class="col-6"><input type="text" class="form-control form-control-sm" placeholder="Name" data-key="name" value="' + dmfEscape(obj.name||'') + '"></div>' +
      '<div class="col-6"><input type="tel" class="form-control form-control-sm" placeholder="Phone" data-key="phone" value="' + dmfEscape(obj.phone||'') + '"></div>' +
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
}

function dmfRemoveRow(btn) {
  btn.closest('.dmf-row').remove();
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
        status.textContent = 'Saved \u2713';
        status.className = 'dmf-save-status text-success';
      } else {
        status.textContent = 'Error: ' + (data.error || 'unknown');
        status.className = 'dmf-save-status text-danger';
      }
      btn.disabled = false;
      setTimeout(function(){ status.textContent=''; }, 4000);
    }).catch(function(e) {
      status.textContent = 'Network error';
      status.className = 'dmf-save-status text-danger';
      btn.disabled = false;
    });
}

// Initial render
document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('[id^="dmf-group-"]').forEach(function(group) {
    var fieldId = parseInt(group.id.replace('dmf-group-',''));
    var fieldType = group.dataset.type || 'text';
    var maxOccurs = parseInt(group.dataset.maxOccurs || '1');
    dmfRenderGroup(fieldId, fieldType, maxOccurs);
  });
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
  // Collect current form values (same as dmfCollect but with field metadata)
  var result = {
    destination: dmfDestination,
    exportedAt: new Date().toISOString(),
    metadata: {}
  };

  document.querySelectorAll('[id^="dmf-values-"]').forEach(function(container) {
    var fieldId = parseInt(container.id.replace('dmf-values-',''));
    var fieldInfo = dmfFieldIndex[fieldId] || {};
    var fieldType = fieldInfo.type || container.closest('[id^="dmf-group-"]').dataset.type || 'text';
    var rows = container.querySelectorAll('.dmf-row');
    var values = [];
    rows.forEach(function(row) {
      var val = dmfReadInput(row, fieldType);
      if (val && val.trim()) {
        // For JSON types, try to parse back to object
        if (fieldType === 'contact' || fieldType === 'mail-group' || fieldType === 'switchboard') {
          try { val = JSON.parse(val); } catch(e) {}
        }
        values.push(val);
      }
    });
    if (values.length > 0) {
      var key = fieldInfo.name || ('field_' + fieldId);
      result.metadata[key] = values.length === 1 ? values[0] : values;
    }
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

