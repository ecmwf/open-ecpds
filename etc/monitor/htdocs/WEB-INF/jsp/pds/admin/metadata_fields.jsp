<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<style>
#mfTable td.td-name  { }
#mfTable td.td-type  { font-size: 0.82rem; }
#mfTable td.td-cat   { font-size: 0.82rem; color: var(--bs-secondary-color); }
.mf-inactive         { opacity: 0.45; }
</style>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
  <i class="bi bi-list-check text-primary"></i>
  <span class="fw-semibold">Metadata Field Definitions</span>
  <button class="btn btn-link btn-sm text-muted p-0" type="button"
      data-bs-toggle="collapse" data-bs-target="#mfInfoPanel"
      aria-expanded="false" title="About this page">
    <i class="bi bi-info-circle"></i>
  </button>
  <button id="mfUnassignedBtn" type="button" class="btn btn-sm btn-outline-secondary"
          onclick="mfToggleUnassigned()" title="Show only fields not used by any destination">
    <i class="bi bi-exclamation-triangle-fill me-1"></i>Unassigned only
  </button>
  <a href="<c:url value='/do/transfer/destination/metadata/bulkimport'/>" class="btn btn-sm btn-outline-info"
     title="Import XML metadata files for all destinations at once">
    <i class="bi bi-cloud-upload me-1"></i>Bulk Import XML
  </a>
  <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <div class="input-group input-group-sm" style="width:auto">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input type="text" id="mfSearch" class="form-control" placeholder="Search fields..." style="min-width:160px">
    </div>
    <div class="input-group flex-nowrap" style="width:auto" title="Page size">
      <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
      <select id="mfPageLen" class="form-select form-select-sm" style="width:auto">
        <option value="10">10</option>
        <option value="25" selected>25</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="-1">All</option>
      </select>
    </div>
    <div class="dropdown">
      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" id="mfColModeBtn"
              data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
        <i class="bi bi-layout-three-columns me-1"></i>Auto
      </button>
      <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="mfColModeBtn">
        <li><a class="dropdown-item" href="#" data-mf-mode="auto"><strong>Auto</strong><br><small class="text-muted">Hides columns progressively by screen width</small></a></li>
        <li><a class="dropdown-item" href="#" data-mf-mode="all"><strong>All</strong><br><small class="text-muted">Shows all columns</small></a></li>
        <li><a class="dropdown-item" href="#" data-mf-mode="compact"><strong>Compact</strong><br><small class="text-muted">Hides: Label, Category, Types, Max, Pos, #</small></a></li>
      </ul>
    </div>
    <button type="button" class="btn btn-sm btn-outline-success" onclick="mfOpenAdd()">
      <i class="bi bi-plus-circle"></i> Create
    </button>
  </div>
</div>

<div class="collapse" id="mfInfoPanel">
  <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Metadata Field Definitions &mdash; overview</strong>
    <p class="mb-1">This page manages the <strong>metadata field definitions</strong> used across all destinations. Each field definition describes a piece of metadata that can be filled in per destination on the destination's Metadata tab.</p>
    <ul class="mb-1 ps-3">
      <li><strong>Name</strong> &mdash; unique identifier (no spaces); used as the key in JSON exports and XML imports.</li>
      <li><strong>Label</strong> &mdash; human-readable name shown on the destination metadata form.</li>
      <li><strong>Type</strong> &mdash; input type: <em>text</em>, <em>textarea</em>, <em>email</em>, <em>url</em>, <em>phone</em>, <em>password</em>, <em>contact</em>, <em>mail-group</em>, <em>switchboard</em>.</li>
      <li><strong>Category</strong> &mdash; groups fields into sections on the destination metadata form.</li>
      <li><strong>Destination Types</strong> &mdash; restricts the field to specific destination types (Acquisition / Dissemination / Time Critical). Empty = applies to all types.</li>
      <li><strong>Max</strong> &mdash; maximum number of values per destination (&minus;1 = unlimited, 1 = single value).</li>
      <li><strong>Pos</strong> &mdash; display order within the category.</li>
      <li><strong>Active</strong> &mdash; inactive fields are hidden from destination metadata forms but their values are preserved.</li>
      <li><strong>Unassigned only</strong> &mdash; shows only field definitions that have no values saved for any destination. Use with <em>Delete All Unassigned</em> to clean up unused definitions.</li>
    </ul>
  </div>
</div>

<c:if test="${not empty loadError}">
  <div class="card-body py-2"><div class="alert alert-danger mb-0"><i class="bi bi-exclamation-triangle-fill me-2"></i>${loadError}</div></div>
</c:if>

<div class="card-body p-0">
<div class="table-responsive">
<table class="table table-sm table-hover table-striped align-middle mb-0 dataTable" id="mfTable" style="width:100%">
  <thead class="table-warning">
    <tr>
      <th class="mf-sortable dt-orderable-asc dt-orderable-desc" data-mf-sort="name" style="cursor:pointer">Name <span class="dt-column-order"></span></th>
      <th class="mf-col-label mf-sortable dt-orderable-asc dt-orderable-desc" data-mf-sort="label" style="cursor:pointer">Label <span class="dt-column-order"></span></th>
      <th class="mf-col-type mf-sortable dt-orderable-asc dt-orderable-desc" style="width:100px;cursor:pointer" data-mf-sort="type">Type <span class="dt-column-order"></span></th>
      <th class="mf-col-cat mf-sortable dt-orderable-asc dt-orderable-desc" style="width:110px;cursor:pointer" data-mf-sort="cat">Category <span class="dt-column-order"></span></th>
      <th class="mf-col-types" style="width:160px">Destination Types</th>
      <th class="mf-col-max text-center" style="width:55px" title="Max occurrences (-1 = unlimited)">Max</th>
      <th class="mf-col-pos text-center mf-sortable dt-orderable-asc dt-orderable-desc" style="width:50px;cursor:pointer" data-mf-sort="pos">Pos <span class="dt-column-order"></span></th>
      <th class="text-center mf-sortable dt-orderable-asc dt-orderable-desc" style="width:65px;cursor:pointer" data-mf-sort="active">Active <span class="dt-column-order"></span></th>
      <th class="text-center no-sort" style="width:95px">Actions</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="f" items="${metaFields}">
    <tr class="${f.active ? '' : 'mf-inactive'}" id="mfRow${f.id}"
        data-mf-name="${f.name}" data-mf-label="${f.label}" data-mf-cat="${f.category}"
        data-mf-type="${f.type}" data-mf-pos="${f.position}" data-mf-active="${f.active ? 1 : 0}"
        data-mf-used="${usedFieldIds.contains(f.id) ? '1' : '0'}">
      <td class="td-name">${f.name}</td>
      <td class="mf-col-label">${f.label}</td>
      <td class="td-type mf-col-type"><span class="badge bg-secondary-subtle text-secondary-emphasis border">${f.type}</span></td>
      <td class="td-cat mf-col-cat">${f.category}</td>
      <td class="small mf-col-types" id="mfTypesCell${f.id}"><%-- filled by JS --%></td>
      <td class="text-center small mf-col-max">${f.maxOccurs == -1 ? '∞' : f.maxOccurs}</td>
      <td class="text-center small mf-col-pos">${f.position}</td>
      <td class="text-center">
        <div class="form-check form-switch d-flex justify-content-center mb-0">
          <input class="form-check-input" type="checkbox" role="switch"
                 ${f.active ? 'checked' : ''}
                 onchange="mfToggle(${f.id}, this.checked)"
                 title="${f.active ? 'Active — click to deactivate' : 'Inactive — click to activate'}">
        </div>
      </td>
      <td class="text-center">
        <a href="#" class="btn btn-sm btn-outline-primary me-1" title="Edit this field"
           onclick="mfOpenEdit(${f.id},'${f.name}','<c:out value="${f.label}" escapeXml="true"/>','${f.type}','<c:out value="${f.category}" escapeXml="true"/>','<c:out value="${f.tooltip}" escapeXml="true"/>',${f.maxOccurs},${f.position},${f.active});return false;">
          <i class="bi bi-pencil"></i>
        </a>
        <a href="#" class="btn btn-sm btn-outline-danger" title="Delete this field"
           onclick="mfConfirmDelete(${f.id},'<c:out value="${f.name}" escapeXml="true"/>');return false;">
          <i class="bi bi-trash"></i>
        </a>
      </td>
    </tr>
    </c:forEach>
    <c:if test="${empty metaFields}">
    <tr id="mfEmptyRow"><td colspan="9" class="text-center text-muted py-3">No fields defined yet.</td></tr>
    </c:if>
  </tbody>
</table>
</div>
<%-- DataTables pagination row --%>
<div id="mfDtInfo" class="d-flex align-items-start mt-2 px-3 pb-2 small text-muted"></div>
</div>
</div>

<div class="mt-3" id="mfDeleteAllUnassignedWrapper" style="display:none">
  <div id="mfDeleteAllBulkMsg"></div>
  <button id="mfDeleteAllUnassignedBtn" type="button"
          class="btn btn-outline-danger"
          title="Delete all field definitions not used by any destination">
    <i class="bi bi-trash-fill me-1"></i>Delete All Unassigned (0)
  </button>
</div>

<div class="modal fade" id="mfModal" tabindex="-1" aria-labelledby="mfModalLabel">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="mfModalLabel">Field Definition</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="mfId" value="0"/>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Name <span class="text-danger">*</span>
              <small class="text-muted">(unique, no spaces)</small></label>
            <input type="text" class="form-control form-control-sm" id="mfName" maxlength="64"
                   placeholder="e.g. mainContact" pattern="[a-zA-Z0-9_\-]+"
                   oninput="this.value=this.value.replace(/[^a-zA-Z0-9_\-]/g,'')">
          </div>
          <div class="col-md-6">
            <label class="form-label">Label <span class="text-danger">*</span></label>
            <input type="text" class="form-control form-control-sm" id="mfLabel" maxlength="128"
                   placeholder="e.g. Main Contact">
          </div>
          <div class="col-md-4">
            <label class="form-label">Type <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" id="mfType">
              <option value="text">text — single line</option>
              <option value="textarea">textarea — multi-line</option>
              <option value="url">url — web address</option>
              <option value="email">email — email address</option>
              <option value="phone">phone — phone number</option>
              <option value="password">password — masked</option>
              <option value="contact">contact — name/phone/fax/email</option>
              <option value="mail-group">mail-group — group name/email</option>
              <option value="switchboard">switchboard — name/phone</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">Category</label>
            <input type="text" class="form-control form-control-sm" id="mfCategory" maxlength="64"
                   placeholder="General" list="mfCategoryList">
            <datalist id="mfCategoryList">
              <option value="General"/>
              <option value="Contacts"/>
              <option value="Documentation"/>
              <option value="Data"/>
              <option value="Storage"/>
              <option value="Procedures"/>
              <option value="Alerts"/>
            </datalist>
          </div>
          <div class="col-md-4">
            <label class="form-label">Max Occurrences
              <small class="text-muted">(-1 = unlimited)</small></label>
            <input type="number" class="form-control form-control-sm" id="mfMaxOccurs" value="1" min="-1">
          </div>
          <div class="col-md-4">
            <label class="form-label">Position <small class="text-muted">(sort order)</small></label>
            <input type="number" class="form-control form-control-sm" id="mfPosition" value="0" min="0">
          </div>
          <div class="col-md-4">
            <label class="form-label">Active</label>
            <div class="form-check form-switch mt-1">
              <input class="form-check-input" type="checkbox" role="switch" id="mfActive" checked>
              <label class="form-check-label" for="mfActive">Enabled</label>
            </div>
          </div>
          <div class="col-12">
            <label class="form-label">Tooltip <small class="text-muted">(optional)</small></label>
            <input type="text" class="form-control form-control-sm" id="mfTooltip" maxlength="512"
                   placeholder="Help text shown next to the field label">
          </div>
          <div class="col-12">
            <label class="form-label">Applies To Destination Types
              <small class="text-muted">(none selected = all types)</small></label>
            <%-- chip container --%>
            <div class="border rounded p-2 d-flex flex-wrap align-items-center gap-1 mb-1" id="mfTypeChips" style="min-height:2.2rem">
              <span class="text-muted small fst-italic" id="mfTypeChipsEmpty"><i class="bi bi-globe2 me-1"></i>No restriction — applies to all destination types</span>
            </div>
            <div class="d-flex gap-2 align-items-center">
              <select class="form-select form-select-sm" id="mfTypeAddSelect" onchange="mfTypeAdd(this)">
                <option value="">— select destination types —</option>
              </select>
              <button type="button" class="btn btn-sm btn-outline-danger text-nowrap" id="mfTypeClearBtn"
                      onclick="mfTypeClearAll()" style="display:none" title="Remove all">
                <i class="bi bi-x-lg me-1"></i>Clear all
              </button>
            </div>
          </div>
        </div>
        <div id="mfSaveError" class="alert alert-danger mt-3 d-none"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-primary" id="mfSaveBtn" onclick="mfSave()">
          <i class="bi bi-floppy me-1"></i>Save
        </button>
      </div>
    </div>
  </div>
</div>

<script>
var _mfModal;
var _mfDeleteId = 0;
/* destination type catalogue */
<%
  String _destTypesJson = (String) request.getAttribute("destTypesJson");
  if (_destTypesJson == null) _destTypesJson = "[]";
  String _fieldTypeMapJson = (String) request.getAttribute("fieldTypeMapJson");
  if (_fieldTypeMapJson == null) _fieldTypeMapJson = "{}";
%>
var _mfDestTypes = <%=_destTypesJson%>;
/* per-field type restrictions: {"fieldId": [desType, ...], ...} — keys are strings */
var _mfTypeMap = <%=_fieldTypeMapJson%>;

document.addEventListener('DOMContentLoaded', function() {
  _mfModal = new bootstrap.Modal(document.getElementById('mfModal'));

  /* populate Destination Types cells in table */
  <c:forEach var="f" items="${metaFields}">
  mfRenderTypesCell(${f.id});
  </c:forEach>
});

function _mfTypeLabel(id) {
  for (var i = 0; i < _mfDestTypes.length; i++) {
    if (_mfDestTypes[i].id === id) return _mfDestTypes[i].label;
  }
  return String(id);
}

function mfRenderTypesCell(fieldId) {
  var cell = document.getElementById('mfTypesCell' + fieldId);
  if (!cell) return;
  var types = _mfTypeMap[String(fieldId)];
  if (!types || types.length === 0) {
    cell.innerHTML = '<span class="badge bg-secondary-subtle text-secondary-emphasis border">all types</span>';
  } else {
    cell.innerHTML = types.map(function(t) {
      return '<span class="badge bg-primary-subtle text-primary-emphasis border me-1">' + _mfTypeLabel(t) + '</span>';
    }).join('');
  }
}

/* ---- chip picker helpers ---- */
var _mfSelectedTypes = [];

function _mfChipsRender() {
  var container = document.getElementById('mfTypeChips');
  var empty     = document.getElementById('mfTypeChipsEmpty');
  var sel       = document.getElementById('mfTypeAddSelect');
  /* clear chips (keep the empty-label span) */
  Array.from(container.children).forEach(function(c) {
    if (c !== empty) container.removeChild(c);
  });
  empty.style.display = _mfSelectedTypes.length === 0 ? '' : 'none';
  var clearBtn = document.getElementById('mfTypeClearBtn');
  if (clearBtn) clearBtn.style.display = _mfSelectedTypes.length > 0 ? '' : 'none';
  /* render chips */
  _mfSelectedTypes.forEach(function(tid) {
    var chip = document.createElement('span');
    chip.className = 'badge bg-primary d-inline-flex align-items-center gap-1 fs-6';
    chip.style.fontWeight = 'normal';
    chip.textContent = _mfTypeLabel(tid);
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn-close btn-close-white ms-1';
    btn.style.cssText = 'font-size:0.6rem;width:0.7rem;height:0.7rem';
    btn.setAttribute('aria-label', 'Remove');
    btn.addEventListener('click', (function(t){ return function(){ mfTypeRemove(t); }; })(tid));
    chip.appendChild(btn);
    container.insertBefore(chip, empty);
  });
  /* rebuild add-select options */
  sel.innerHTML = '<option value="">— select destination types —</option>';
  _mfDestTypes.forEach(function(t) {
    if (_mfSelectedTypes.indexOf(t.id) < 0) {
      var opt = document.createElement('option');
      opt.value = t.id;
      opt.textContent = t.label;
      sel.appendChild(opt);
    }
  });
}

function mfTypeAdd(sel) {
  var val = parseInt(sel.value);
  if (isNaN(val)) return;
  if (_mfSelectedTypes.indexOf(val) < 0) _mfSelectedTypes.push(val);
  sel.value = '';
  _mfChipsRender();
}

function mfTypeRemove(tid) {
  _mfSelectedTypes = _mfSelectedTypes.filter(function(t){ return t !== tid; });
  _mfChipsRender();
}

function mfTypeClearAll() {
  _mfSelectedTypes = [];
  _mfChipsRender();
}

function mfOpenAdd() {
  document.getElementById('mfModalLabel').textContent = 'Add Field Definition';
  document.getElementById('mfId').value = '0';
  document.getElementById('mfName').value = '';
  document.getElementById('mfName').disabled = false;
  document.getElementById('mfLabel').value = '';
  document.getElementById('mfType').value = 'text';
  document.getElementById('mfCategory').value = 'General';
  document.getElementById('mfTooltip').value = '';
  document.getElementById('mfMaxOccurs').value = '1';
  document.getElementById('mfPosition').value = '0';
  document.getElementById('mfActive').checked = true;
  _mfSelectedTypes = [];
  _mfChipsRender();
  document.getElementById('mfSaveError').classList.add('d-none');
  _mfModal.show();
}

function mfOpenEdit(id, name, label, type, category, tooltip, maxOccurs, position, active) {
  document.getElementById('mfModalLabel').textContent = 'Edit Field Definition';
  document.getElementById('mfId').value = id;
  document.getElementById('mfName').value = name;
  document.getElementById('mfName').disabled = true;
  document.getElementById('mfLabel').value = label;
  document.getElementById('mfType').value = type;
  document.getElementById('mfCategory').value = category;
  document.getElementById('mfTooltip').value = tooltip || '';
  document.getElementById('mfMaxOccurs').value = maxOccurs;
  document.getElementById('mfPosition').value = position;
  document.getElementById('mfActive').checked = active;
  _mfSelectedTypes = (_mfTypeMap[String(id)] || []).slice();
  _mfChipsRender();
  document.getElementById('mfSaveError').classList.add('d-none');
  _mfModal.show();
}

function mfSave() {
  var btn = document.getElementById('mfSaveBtn');
  var errEl = document.getElementById('mfSaveError');
  errEl.classList.add('d-none');

  var nameVal = document.getElementById('mfName').value.trim();
  var labelVal = document.getElementById('mfLabel').value.trim();
  if (!nameVal) {
    errEl.textContent = 'Name is required.';
    errEl.classList.remove('d-none');
    return;
  }
  if (!/^[a-zA-Z0-9_\-]+$/.test(nameVal)) {
    errEl.textContent = 'Name must contain only letters, digits, underscores or hyphens (no spaces).';
    errEl.classList.remove('d-none');
    return;
  }
  if (!labelVal) {
    errEl.textContent = 'Label is required.';
    errEl.classList.remove('d-none');
    return;
  }

  btn.disabled = true;

  var payload = {
    DMF_ID:        parseInt(document.getElementById('mfId').value) || 0,
    DMF_NAME:      document.getElementById('mfName').value.trim(),
    DMF_LABEL:     document.getElementById('mfLabel').value.trim(),
    DMF_TYPE:      document.getElementById('mfType').value,
    DMF_CATEGORY:  document.getElementById('mfCategory').value.trim() || 'General',
    DMF_TOOLTIP:   document.getElementById('mfTooltip').value.trim() || null,
    DMF_MAX_OCCURS:parseInt(document.getElementById('mfMaxOccurs').value),
    DMF_POSITION:  parseInt(document.getElementById('mfPosition').value) || 0,
    DMF_ACTIVE:    document.getElementById('mfActive').checked,
    DES_TYPES:     _mfSelectedTypes.slice()
  };

  fetch('<c:url value="/do/admin/metafields/save"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify(payload)
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      btn.disabled = false;
      if (data.success) {
        _mfModal.hide();
        location.reload();
      } else {
        errEl.textContent = data.error || 'Save failed';
        errEl.classList.remove('d-none');
      }
    }).catch(function(e) {
      btn.disabled = false;
      errEl.textContent = 'Network error: ' + e.message;
      errEl.classList.remove('d-none');
    });
}

function mfConfirmDelete(id, name) {
  _mfDeleteId = id;
  confirmationDialog({
    title: 'Delete Metadata Field',
    message: 'Delete field <b>' + name + '</b>?<br/><br/>'
           + 'This will permanently remove the field definition and <b>all values saved for this field across every destination</b>. '
           + 'This action cannot be undone.',
    confirmText: 'Delete',
    showLoading: false,
    onConfirm: mfDelete
  });
}

function mfDelete() {
  fetch('<c:url value="/do/admin/metafields/delete"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({DMF_ID: _mfDeleteId})
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      if (data.success) {
        var row = document.getElementById('mfRow' + _mfDeleteId);
        if (row) row.remove();
      } else {
        alert('Delete failed: ' + (data.error || 'unknown'));
      }
    }).catch(function(e) {
      alert('Network error: ' + e.message);
    });
}

function mfToggle(id, active) {
  fetch('<c:url value="/do/admin/metafields/toggle"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({DMF_ID: id, DMF_ACTIVE: active})
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      if (data.success) {
        var row = document.getElementById('mfRow' + id);
        if (row) row.classList.toggle('mf-inactive', !active);
      } else {
        alert('Toggle failed: ' + (data.error || 'unknown'));
        location.reload();
      }
    }).catch(function(e) {
      alert('Network error: ' + e.message);
      location.reload();
    });
}

/* ---- Search / Pagination / Column-mode ---- */
var _mfAllRows = [];   // cached array of all <tr> elements
var _mfPage    = 0;
var _mfPageLen = 25;
var _mfColMode = 'auto';
var _mfSortCol = 'name';
var _mfSortAsc = true;
var _mfUnassignedOnly = false;
var _MF_LEN_KEY  = 'mfPageLen';
var _MF_COL_KEY  = 'mfColMode';
var _MF_SORT_KEY = 'mfSortCol';
var _MF_COMPACT  = ['mf-col-label','mf-col-cat','mf-col-types','mf-col-max','mf-col-pos','mf-col-type'];

function _mfInit() {
  var tbody = document.querySelector('#mfTable tbody');
  _mfAllRows = Array.from(tbody.querySelectorAll('tr[id^="mfRow"]'));

  // Restore prefs
  try { var v = parseInt(localStorage.getItem(_MF_LEN_KEY),10); if ([10,25,50,100,-1].indexOf(v)>=0) _mfPageLen=v; } catch(e){}
  try { var m = localStorage.getItem(_MF_COL_KEY); if (m) _mfColMode = m; } catch(e){}
  try { var s = localStorage.getItem(_MF_SORT_KEY); if (s) { var sp=s.split(':'); _mfSortCol=sp[0]; _mfSortAsc=sp[1]!=='desc'; } } catch(e){}

  var sel = document.getElementById('mfPageLen');
  if (sel) sel.value = _mfPageLen;

  document.getElementById('mfSearch').addEventListener('input', function(){ _mfPage=0; _mfRender(); });
  document.getElementById('mfPageLen').addEventListener('change', function(){
    _mfPageLen = parseInt(this.value,10);
    try { localStorage.setItem(_MF_LEN_KEY, _mfPageLen); } catch(e){}
    _mfPage = 0; _mfRender();
  });

  // Sort headers
  document.querySelectorAll('.mf-sortable').forEach(function(th) {
    th.addEventListener('click', function() {
      var col = this.dataset.mfSort;
      if (_mfSortCol === col) { _mfSortAsc = !_mfSortAsc; }
      else { _mfSortCol = col; _mfSortAsc = true; }
      try { localStorage.setItem(_MF_SORT_KEY, _mfSortCol + ':' + (_mfSortAsc ? 'asc' : 'desc')); } catch(e){}
      _mfPage = 0; _mfRender();
    });
  });

  // Column mode dropdown
  document.querySelectorAll('[data-mf-mode]').forEach(function(a) {
    a.addEventListener('click', function(e) {
      e.preventDefault();
      _mfColMode = this.dataset.mfMode;
      try { localStorage.setItem(_MF_COL_KEY, _mfColMode); } catch(e){}
      _mfApplyColMode();
    });
  });
  window.addEventListener('resize', function(){ if(_mfColMode==='auto') _mfApplyColMode(); });

  _mfApplyColMode();
  _mfRender();
}

function _mfApplyColMode() {
  var w = window.innerWidth;
  var hide = [];
  if (_mfColMode === 'compact') {
    hide = _MF_COMPACT;
  } else if (_mfColMode === 'auto') {
    if      (w < 576)  hide = ['mf-col-label','mf-col-cat','mf-col-types','mf-col-max','mf-col-pos','mf-col-type'];
    else if (w < 768)  hide = ['mf-col-cat','mf-col-types','mf-col-max','mf-col-pos'];
    else if (w < 992)  hide = ['mf-col-cat','mf-col-max','mf-col-pos'];
    else if (w < 1200) hide = ['mf-col-max','mf-col-pos'];
    else               hide = [];
  }
  _MF_COMPACT.forEach(function(cls) {
    document.querySelectorAll('.' + cls).forEach(function(el){
      el.style.display = hide.indexOf(cls) >= 0 ? 'none' : '';
    });
  });
  var label = _mfColMode.charAt(0).toUpperCase() + _mfColMode.slice(1);
  var btn = document.getElementById('mfColModeBtn');
  if (btn) btn.innerHTML = '<i class="bi bi-layout-three-columns me-1"></i>' + label;
  btn.classList.toggle('btn-outline-secondary', _mfColMode === 'auto');
  btn.classList.toggle('btn-primary', _mfColMode !== 'auto');
  // Tick active item
  document.querySelectorAll('[data-mf-mode]').forEach(function(a) {
    a.querySelectorAll('.bi-check').forEach(function(i){ i.remove(); });
    if (a.dataset.mfMode === _mfColMode) a.insertAdjacentHTML('afterbegin','<i class="bi bi-check me-1"></i>');
  });
}

function _mfUpdateSortIcons() {
  document.querySelectorAll('.mf-sortable').forEach(function(th) {
    th.classList.remove('dt-ordering-asc', 'dt-ordering-desc');
  });
  var active = document.querySelector('.mf-sortable[data-mf-sort="' + _mfSortCol + '"]');
  if (active) active.classList.add(_mfSortAsc ? 'dt-ordering-asc' : 'dt-ordering-desc');
}

function mfToggleUnassigned() {
  _mfUnassignedOnly = !_mfUnassignedOnly;
  var btn = document.getElementById('mfUnassignedBtn');
  btn.classList.toggle('btn-outline-secondary', !_mfUnassignedOnly);
  btn.classList.toggle('btn-warning', _mfUnassignedOnly);
  _mfPage = 0;
  _mfRender();
}

function _mfUpdateDeleteAllBtn(unassignedCount) {
  var wrapper = document.getElementById('mfDeleteAllUnassignedWrapper');
  var btn = document.getElementById('mfDeleteAllUnassignedBtn');
  if (_mfUnassignedOnly && unassignedCount > 0) {
    wrapper.style.display = '';
    btn.innerHTML = '<i class="bi bi-trash-fill me-1"></i>Delete All Unassigned (' + unassignedCount + ')';
  } else {
    wrapper.style.display = 'none';
  }
}

function _mfRender() {
  var q = (document.getElementById('mfSearch').value || '').toLowerCase();
  // Count total unassigned for Delete All button
  var totalUnassigned = _mfAllRows.filter(function(tr){ return tr.dataset.mfUsed === '0'; }).length;
  _mfUpdateDeleteAllBtn(totalUnassigned);

  var visible = _mfAllRows.filter(function(tr){
    if (_mfUnassignedOnly && tr.dataset.mfUsed !== '0') return false;
    if (!q) return true;
    return (tr.dataset.mfName||'').toLowerCase().indexOf(q) >= 0
        || (tr.dataset.mfLabel||'').toLowerCase().indexOf(q) >= 0
        || (tr.dataset.mfCat||'').toLowerCase().indexOf(q) >= 0;
  });

  // Sort
  visible.sort(function(a, b) {
    var av, bv;
    if (_mfSortCol === 'pos' || _mfSortCol === 'active') {
      av = parseFloat(a.dataset['mf' + _mfSortCol.charAt(0).toUpperCase() + _mfSortCol.slice(1)]) || 0;
      bv = parseFloat(b.dataset['mf' + _mfSortCol.charAt(0).toUpperCase() + _mfSortCol.slice(1)]) || 0;
      return _mfSortAsc ? av - bv : bv - av;
    } else {
      av = (a.dataset['mf' + _mfSortCol.charAt(0).toUpperCase() + _mfSortCol.slice(1)] || '').toLowerCase();
      bv = (b.dataset['mf' + _mfSortCol.charAt(0).toUpperCase() + _mfSortCol.slice(1)] || '').toLowerCase();
      return _mfSortAsc ? av.localeCompare(bv) : bv.localeCompare(av);
    }
  });
  _mfUpdateSortIcons();

  var total   = visible.length;
  var pageLen = _mfPageLen === -1 ? total : _mfPageLen;
  var pages   = pageLen > 0 ? Math.ceil(total / pageLen) : 1;
  if (_mfPage >= pages) _mfPage = Math.max(0, pages - 1);
  var start = _mfPage * pageLen;
  var end   = _mfPageLen === -1 ? total : Math.min(start + pageLen, total);

  _mfAllRows.forEach(function(tr){ tr.style.display = 'none'; });
  var tbody = document.querySelector('#mfTable tbody');
  for (var i = start; i < end; i++) { visible[i].style.display = ''; tbody.appendChild(visible[i]); }

  // empty state
  var emptyRow = document.getElementById('mfEmptyRow');
  if (emptyRow) emptyRow.style.display = total === 0 ? '' : 'none';

  // info + pagination
  var info = document.getElementById('mfDtInfo');
  if (total === 0) {
    info.innerHTML = '<span>No fields match.</span>';
    return;
  }
  var showing = 'Showing ' + (start+1) + ' to ' + end + ' of ' + total + ' field' + (total!==1?'s':'');
  var pager = '';
  if (pages > 1) {
    pager += '<nav class="ms-auto"><ul class="pagination pagination-sm mb-0">';
    pager += '<li class="page-item' + (_mfPage===0?' disabled':'') + '"><a class="page-link" href="#" onclick="mfGoPage(' + (_mfPage-1) + ');return false;">&laquo;</a></li>';
    for (var p = 0; p < pages; p++) {
      pager += '<li class="page-item' + (p===_mfPage?' active':'') + '"><a class="page-link" href="#" onclick="mfGoPage('+p+');return false;">'+(p+1)+'</a></li>';
    }
    pager += '<li class="page-item' + (_mfPage===pages-1?' disabled':'') + '"><a class="page-link" href="#" onclick="mfGoPage(' + (_mfPage+1) + ');return false;">&raquo;</a></li>';
    pager += '</ul></nav>';
  }
  info.innerHTML = '<span>' + showing + '</span>' + pager;
}

function mfGoPage(p) { _mfPage = p; _mfRender(); }

document.addEventListener('DOMContentLoaded', function() {
  _mfInit();
  document.getElementById('mfDeleteAllUnassignedBtn').addEventListener('click', function() {
    var n = _mfAllRows.filter(function(tr){ return tr.dataset.mfUsed === '0'; }).length;
    confirmationDialog({
      title: 'Delete All Unassigned Fields',
      message: 'Delete all <strong>' + n + '</strong> field definition' + (n !== 1 ? 's' : '')
             + ' that are not used by any destination?<br><br>'
             + '<span class="text-danger">This will permanently remove the field definitions and all associated type restrictions. This action cannot be undone.</span>',
      confirmText: 'Delete All',
      showLoading: true,
      onConfirm: function() {
        fetch('/do/admin/metafields/deleteUnassigned', {
          method: 'POST',
          headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
          body: '{}'
        }).then(function(r){ return r.json(); })
          .then(function(data) {
            $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
            if (data.success) {
              // Remove deleted rows from DOM and re-render
              _mfAllRows = _mfAllRows.filter(function(tr){
                if (tr.dataset.mfUsed === '0') { tr.remove(); return false; }
                return true;
              });
              _mfPage = 0;
              _mfRender();
            } else {
              alert('Error: ' + (data.error || 'unknown'));
            }
          }).catch(function(){
            $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
            alert('Network error');
          });
      }
    });
  });
});
</script>
