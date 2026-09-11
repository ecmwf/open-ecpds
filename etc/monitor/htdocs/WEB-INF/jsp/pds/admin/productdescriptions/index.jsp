<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String pdError = (String) request.getAttribute("pdError");
%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-card-text text-secondary flex-shrink-0"></i>
<span>Descriptions and tips for products (optionally per type), used as the <code>{{DESCRIPTION}}</code> placeholder in <a href="/do/admin/productmessages">Product Messages</a> and shown on the <a href="/do/monitoring">monitoring</a> product pages.</span>
</div>

<% if (pdError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=pdError%></span>
</div>
<% } %>

<div id="pdSuccessAlertHolder"></div>

<style>
.pd-desc-cell {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    max-width: 420px;
    white-space: pre-wrap;
}
</style>

<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
  <i class="bi bi-card-list text-primary"></i>
  <span class="fw-semibold">Product Descriptions</span>
  <button class="btn btn-link btn-sm text-muted p-0" type="button"
      data-bs-toggle="collapse" data-bs-target="#pdInfoPanel"
      aria-expanded="false" title="About this page">
    <i class="bi bi-info-circle"></i>
  </button>
  <button id="pdUnknownBtn" type="button" class="btn btn-sm btn-outline-secondary"
          onclick="pdToggleUnknown()" title="Show only products that do not exist in the monitoring interface">
    <i class="bi bi-exclamation-triangle-fill me-1"></i>Unknown only
  </button>
  <button id="pdImportBtn" type="button" class="btn btn-sm btn-outline-info"
          onclick="pdImport()" title="Import all product/type pairs currently seen in the monitoring interface that are not yet configured here">
    <i class="bi bi-cloud-download me-1"></i>Import from Monitoring
  </button>
  <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <div class="input-group input-group-sm" style="width:auto">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input type="text" id="pdSearch" class="form-control" placeholder="Search product, type or text..." style="min-width:220px">
    </div>
    <div class="input-group flex-nowrap" style="width:auto" title="Page size">
      <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
      <select id="pdPageLen" class="form-select form-select-sm" style="width:auto">
        <option value="10">10</option>
        <option value="25" selected>25</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="-1">All</option>
      </select>
    </div>
    <button type="button" class="btn btn-sm btn-outline-success" onclick="pdOpenAdd()">
      <i class="bi bi-plus-circle"></i> Create
    </button>
  </div>
</div>

<div class="collapse" id="pdInfoPanel">
  <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Product Descriptions &mdash; overview</strong>
    <p class="mb-1">Configure metadata for a product (e.g. <code>GOPER</code>, as seen in
      <code>/do/monitoring/summary/GOPER/06</code>), optionally scoped to one of its types (the <em>Type</em> column
      shown in that page, e.g. <code>AN</code>/<code>FC</code>). Leave Type blank to apply an entry to all types of a
      product that don't have their own type-specific entry.</p>
    <ul class="mb-1 ps-3">
      <li><strong>Product</strong> / <strong>Type</strong> &mdash; identify the entry; Type blank = applies to all types.</li>
      <li><strong>Description</strong> &mdash; used to build the <code>{{DESCRIPTION}}</code> placeholder in
        <a href="/do/admin/productmessages">Product Messages</a>, as a bullet list across the types shown for
        the product.</li>
      <li><strong>Tips</strong> &mdash; shown as an expandable info card on the product's monitoring page
        (e.g. <code>/do/monitoring/summary/GOPER/06/0/AN</code>).</li>
      <li><i class="bi bi-exclamation-triangle-fill text-warning"></i> &mdash; shown next to a product that does not currently
        exist in the <a href="/do/monitoring">monitoring interface</a> (e.g. a typo, or a product that is no longer active).</li>
      <li><strong>Unknown only</strong> &mdash; shows only such products. Use with <em>Delete All Unknown</em> to clean up stale entries.</li>
      <li><strong>Import from Monitoring</strong> &mdash; scans every product/type pair currently seen in the
        <a href="/do/monitoring">monitoring interface</a> and adds a blank entry (empty Description/Tips) for any pair
        not yet configured here, ready for you to fill in.</li>
    </ul>
  </div>
</div>

<div class="card-body p-0">
<div class="table-responsive">
<table class="table table-sm table-hover table-striped align-middle mb-0 dataTable" id="pdTable" style="width:100%">
  <thead class="table-warning">
    <tr>
      <th style="width:140px;">Product</th>
      <th style="width:100px;">Type</th>
      <th>Description</th>
      <th>Tips</th>
      <th style="width:100px;" class="text-end">Actions</th>
    </tr>
  </thead>
  <tbody id="pdTableBody">
    <c:forEach var="entry" items="${productDescriptions}">
    <tr data-pd-product="<c:out value="${entry.product}" />" data-pd-type="<c:out value="${entry.type}" />"
        data-pd-description="<c:out value="${entry.description}" />"
        data-pd-tips="<c:out value="${entry.tips}" />"
        data-pd-known="${entry.known ? 1 : 0}">
      <td>
        <code><c:out value="${entry.product}" /></code>
        <c:if test="${!entry.known}">
        <i class="bi bi-exclamation-triangle-fill text-warning ms-1"
           title="This product does not currently exist in the monitoring interface (/do/monitoring)."></i>
        </c:if>
      </td>
      <td>
        <c:choose>
          <c:when test="${entry.generic}"><span class="text-muted fst-italic">all types</span></c:when>
          <c:otherwise><code><c:out value="${entry.type}" /></code></c:otherwise>
        </c:choose>
      </td>
      <td><div class="pd-desc-cell" title="<c:out value="${entry.description}" />"><c:out value="${entry.description}" /></div></td>
      <td><div class="pd-desc-cell" title="<c:out value="${entry.tips}" />"><c:out value="${entry.tips}" /></div></td>
      <td class="text-end">
        <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                onclick="pdOpenEdit('<c:out value="${entry.product}" />', '<c:out value="${entry.type}" />')" title="Edit">
          <i class="bi bi-pencil-fill"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline-danger"
                onclick="pdConfirmDelete('<c:out value="${entry.product}" />', '<c:out value="${entry.type}" />')" title="Delete">
          <i class="bi bi-trash-fill"></i>
        </button>
      </td>
    </tr>
    </c:forEach>
    <tr id="pdEmptyRow" style="display:none;">
      <td colspan="5" class="text-muted text-center py-3">No product descriptions found.</td>
    </tr>
  </tbody>
</table>
<div class="d-flex align-items-center mt-2 px-3 pb-2" id="pdDtInfo" style="font-size:0.85rem;"></div>
</div>
</div>
</div>

<div class="mt-3 mb-3" id="pdDeleteAllUnknownWrapper" style="display:none">
    <button id="pdDeleteAllUnknownBtn" type="button" class="btn btn-outline-danger"
            title="Delete all product entries for products that do not exist in the monitoring interface">
        <i class="bi bi-trash-fill me-1"></i>Delete All Unknown (0)
    </button>
</div>

<!-- Known product names, for the datalist suggestion on the "Product" field. -->
<datalist id="pdKnownProducts">
    <c:forEach var="pn" items="${knownProductNames}">
    <option value="<c:out value="${pn}" />"></option>
    </c:forEach>
</datalist>

<!-- Add/Edit modal -->
<div class="modal fade" id="pdModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="pdModalTitle">Add Description</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="alert alert-danger d-none" id="pdSaveError"></div>
                <div class="row g-2 mb-2">
                  <div class="col-7">
                    <label for="pdProduct" class="form-label fw-semibold">Product</label>
                    <input type="text" class="form-control" id="pdProduct" list="pdKnownProducts"
                           placeholder="e.g. GOPER" autocomplete="off">
                    <div class="form-text text-muted">Letters, digits, underscores, dots or hyphens only.</div>
                  </div>
                  <div class="col-5">
                    <label for="pdType" class="form-label fw-semibold">Type</label>
                    <input type="text" class="form-control" id="pdType"
                           placeholder="e.g. AN (blank = all types)" autocomplete="off">
                    <div class="form-text text-muted">Blank applies to all types.</div>
                  </div>
                </div>
                <div class="mb-3">
                    <label for="pdDescription" class="form-label fw-semibold">Description</label>
                    <textarea class="form-control" id="pdDescription" rows="3"
                              placeholder="Used to build the {{DESCRIPTION}} placeholder"></textarea>
                </div>
                <div class="mb-1">
                    <label for="pdTips" class="form-label fw-semibold">Tips</label>
                    <textarea class="form-control" id="pdTips" rows="3"
                              placeholder="Shown as an info card on the product's monitoring page"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning" id="pdSaveBtn" onclick="pdSave()">
                    <i class="bi bi-save-fill me-1"></i>Save
                </button>
            </div>
        </div>
    </div>
</div>

<script>
var _pdModal = null;
var _pdEditingKey = null; // null = adding a new entry; otherwise "product\u0001type" of the entry being edited
var _pdDeleteProduct = null;
var _pdDeleteType = null;
var _pdData = {}; // key "product\u0001type" -> {description, tips}
var _pdAllRows = [];
var _pdPage = 0;
var _pdPageLen = 25;
var _PD_LEN_KEY = 'pdPageLen';
var _pdUnknownOnly = false;

function _pdKey(product, type) { return product + '\u0001' + (type || ''); }

document.addEventListener('DOMContentLoaded', function() {
  _pdModal = new bootstrap.Modal(document.getElementById('pdModal'));

  try {
    var v = parseInt(localStorage.getItem(_PD_LEN_KEY), 10);
    if ([10, 25, 50, 100, -1].indexOf(v) >= 0) _pdPageLen = v;
  } catch (e) {}
  document.getElementById('pdPageLen').value = _pdPageLen;

  document.getElementById('pdSearch').addEventListener('input', function() { _pdPage = 0; _pdRender(); });
  document.getElementById('pdPageLen').addEventListener('change', function() {
    _pdPageLen = parseInt(this.value, 10);
    try { localStorage.setItem(_PD_LEN_KEY, _pdPageLen); } catch (e) {}
    _pdPage = 0;
    _pdRender();
  });

  _pdRefreshRows();
  _pdRender();

  document.getElementById('pdDeleteAllUnknownBtn').addEventListener('click', function() {
    var n = _pdAllRows.filter(function(tr) { return tr.dataset.pdKnown === '0'; }).length;
    confirmationDialog({
      title: 'Delete All Unknown Products',
      message: 'Delete all <strong>' + n + '</strong> entr' + (n !== 1 ? 'ies' : 'y')
             + ' whose product does not currently exist in the <a href="/do/monitoring" target="_blank">monitoring interface</a>?<br><br>'
             + '<span class="text-danger">This action cannot be undone.</span>',
      confirmText: 'Delete All',
      showLoading: true,
      onConfirm: function() {
        fetch('/do/admin/productdescriptions/deleteUnknown', {
          method: 'POST',
          headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
          body: '{}'
        }).then(function(r) { return r.json(); })
          .then(function(data) {
            $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
            if (data.success) {
              _pdAllRows = _pdAllRows.filter(function(tr) {
                if (tr.dataset.pdKnown === '0') { tr.remove(); return false; }
                return true;
              });
              _pdPage = 0;
              _pdRefreshRows();
              _pdRender();
              pdShowSuccess('Deleted ' + data.deleted + ' unknown entr' + (data.deleted !== 1 ? 'ies' : 'y') + '.');
            } else {
              alert('Error: ' + (data.error || 'unknown'));
            }
          }).catch(function() {
            $("#loadingBackdrop").hide(); $("#loadingDiv").hide();
            alert('Network error');
          });
      }
    });
  });
});

function _pdRefreshRows() {
  var tbody = document.getElementById('pdTableBody');
  _pdAllRows = Array.from(tbody.querySelectorAll('tr[data-pd-product]'));
  // Rebuild the (product,type) -> {description,tips} lookup used by the Edit modal from the rendered rows' data attributes.
  _pdData = {};
  _pdAllRows.forEach(function(tr) {
    _pdData[_pdKey(tr.dataset.pdProduct, tr.dataset.pdType)] = {
      description: tr.dataset.pdDescription || '',
      tips: tr.dataset.pdTips || ''
    };
  });
}

function pdToggleUnknown() {
  _pdUnknownOnly = !_pdUnknownOnly;
  var btn = document.getElementById('pdUnknownBtn');
  btn.classList.toggle('btn-outline-secondary', !_pdUnknownOnly);
  btn.classList.toggle('btn-warning', _pdUnknownOnly);
  _pdPage = 0;
  _pdRender();
}

function pdImport() {
  var btn = document.getElementById('pdImportBtn');
  btn.disabled = true;
  var original = btn.innerHTML;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Importing...';
  fetch('<c:url value="/do/admin/productdescriptions/import"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: '{}'
  }).then(function(r) { return r.json(); })
    .then(function(data) {
      btn.disabled = false;
      btn.innerHTML = original;
      if (data.success) {
        (data.entries || []).forEach(function(e) { pdRenderRow(e.product, e.type, '', ''); });
        pdUpdateEmptyMessage();
        if (data.imported > 0) {
          pdShowSuccess('Imported ' + data.imported + ' product/type pair' + (data.imported !== 1 ? 's' : '')
                      + ' from the monitoring interface. Edit each entry to add a Description and/or Tips.');
        } else {
          pdShowSuccess('No new product/type pairs found \u2014 everything currently seen in the monitoring interface is already configured.');
        }
      } else {
        alert('Import failed: ' + (data.error || 'unknown'));
      }
    }).catch(function(e) {
      btn.disabled = false;
      btn.innerHTML = original;
      alert('Network error: ' + e.message);
    });
}

function _pdUpdateDeleteAllBtn(unknownCount) {
  var wrapper = document.getElementById('pdDeleteAllUnknownWrapper');
  var btn = document.getElementById('pdDeleteAllUnknownBtn');
  if (_pdUnknownOnly && unknownCount > 0) {
    wrapper.style.display = '';
    btn.innerHTML = '<i class="bi bi-trash-fill me-1"></i>Delete All Unknown (' + unknownCount + ')';
  } else {
    wrapper.style.display = 'none';
  }
}

function _pdRender() {
  var q = (document.getElementById('pdSearch').value || '').toLowerCase();
  var totalUnknown = _pdAllRows.filter(function(tr) { return tr.dataset.pdKnown === '0'; }).length;
  _pdUpdateDeleteAllBtn(totalUnknown);

  var visible = _pdAllRows.filter(function(tr) {
    if (_pdUnknownOnly && tr.dataset.pdKnown !== '0') return false;
    if (!q) return true;
    return (tr.dataset.pdProduct || '').toLowerCase().indexOf(q) >= 0
        || (tr.dataset.pdType || '').toLowerCase().indexOf(q) >= 0
        || (tr.dataset.pdDescription || '').toLowerCase().indexOf(q) >= 0
        || (tr.dataset.pdTips || '').toLowerCase().indexOf(q) >= 0;
  });

  var total = visible.length;
  var pageLen = _pdPageLen === -1 ? total : _pdPageLen;
  var pages = pageLen > 0 ? Math.ceil(total / pageLen) : 1;
  if (_pdPage >= pages) _pdPage = Math.max(0, pages - 1);
  var start = _pdPage * pageLen;
  var end = _pdPageLen === -1 ? total : Math.min(start + pageLen, total);

  _pdAllRows.forEach(function(tr) { tr.style.display = 'none'; });
  var tbody = document.getElementById('pdTableBody');
  for (var i = start; i < end; i++) { visible[i].style.display = ''; tbody.appendChild(visible[i]); }

  var emptyRow = document.getElementById('pdEmptyRow');
  emptyRow.style.display = total === 0 ? '' : 'none';
  tbody.appendChild(emptyRow);

  var info = document.getElementById('pdDtInfo');
  if (total === 0) {
    info.innerHTML = _pdAllRows.length === 0
      ? '<span class="text-muted">No product descriptions have been configured yet.</span>'
      : (_pdUnknownOnly
          ? '<span class="text-muted">No unknown products found &mdash; all configured products exist in the monitoring interface.</span>'
          : '<span class="text-muted">No entries match your search.</span>');
    return;
  }
  var showing = 'Showing ' + (start + 1) + ' to ' + end + ' of ' + total
              + ' entr' + (total !== 1 ? 'ies' : 'y');
  var pager = '';
  if (pages > 1) {
    pager += '<nav class="ms-auto"><ul class="pagination pagination-sm mb-0">';
    pager += '<li class="page-item' + (_pdPage === 0 ? ' disabled' : '') + '"><a class="page-link" href="#" onclick="pdGoPage(' + (_pdPage - 1) + ');return false;">&laquo;</a></li>';
    for (var p = 0; p < pages; p++) {
      pager += '<li class="page-item' + (p === _pdPage ? ' active' : '') + '"><a class="page-link" href="#" onclick="pdGoPage(' + p + ');return false;">' + (p + 1) + '</a></li>';
    }
    pager += '<li class="page-item' + (_pdPage === pages - 1 ? ' disabled' : '') + '"><a class="page-link" href="#" onclick="pdGoPage(' + (_pdPage + 1) + ');return false;">&raquo;</a></li>';
    pager += '</ul></nav>';
  }
  info.innerHTML = '<span>' + showing + '</span>' + pager;
}

function pdGoPage(p) { _pdPage = p; _pdRender(); }

function pdShowSuccess(msg) {
  // Built on demand (instead of always present-but-hidden in the DOM) so it never reserves
  // any layout space before an action actually happens.
  var holder = document.getElementById('pdSuccessAlertHolder');
  holder.innerHTML = '';
  var el = document.createElement('div');
  el.className = 'alert alert-success alert-dismissible fade show d-flex gap-2 mb-4';
  el.setAttribute('role', 'alert');
  el.innerHTML = '<i class="bi bi-check-circle-fill flex-shrink-0 mt-1"></i>'
               + '<span></span>'
               + '<button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>';
  el.querySelector('span').textContent = msg;
  holder.appendChild(el);
  bootstrap.Alert.getOrCreateInstance(el);
  setTimeout(function() {
    if (el.parentNode) { bootstrap.Alert.getOrCreateInstance(el).close(); }
  }, 4000);
}

function pdUpdateEmptyMessage() {
  _pdRefreshRows();
  _pdRender();
}

function pdOpenAdd() {
  _pdEditingKey = null;
  document.getElementById('pdModalTitle').textContent = 'Add Description';
  document.getElementById('pdProduct').value = '';
  document.getElementById('pdProduct').disabled = false;
  document.getElementById('pdType').value = '';
  document.getElementById('pdType').disabled = false;
  document.getElementById('pdDescription').value = '';
  document.getElementById('pdTips').value = '';
  document.getElementById('pdSaveError').classList.add('d-none');
  _pdModal.show();
}

function pdOpenEdit(product, type) {
  _pdEditingKey = _pdKey(product, type);
  document.getElementById('pdModalTitle').textContent = 'Edit Description';
  document.getElementById('pdProduct').value = product;
  document.getElementById('pdProduct').disabled = true;
  document.getElementById('pdType').value = type || '';
  document.getElementById('pdType').disabled = true;
  var entry = _pdData[_pdEditingKey] || {};
  document.getElementById('pdDescription').value = entry.description || '';
  document.getElementById('pdTips').value = entry.tips || '';
  document.getElementById('pdSaveError').classList.add('d-none');
  _pdModal.show();
}

function pdSave() {
  var btn = document.getElementById('pdSaveBtn');
  var errEl = document.getElementById('pdSaveError');
  errEl.classList.add('d-none');

  var product = document.getElementById('pdProduct').value.trim();
  var type = document.getElementById('pdType').value.trim();
  var description = document.getElementById('pdDescription').value;
  var tips = document.getElementById('pdTips').value;

  if (!product) {
    errEl.textContent = 'Product name is required.';
    errEl.classList.remove('d-none');
    return;
  }
  if (!/^[A-Za-z0-9_.-]+$/.test(product)) {
    errEl.textContent = 'Product name must contain only letters, digits, underscores, dots or hyphens.';
    errEl.classList.remove('d-none');
    return;
  }
  if (type && !/^[A-Za-z0-9_.-]+$/.test(type)) {
    errEl.textContent = 'Product type must contain only letters, digits, underscores, dots or hyphens.';
    errEl.classList.remove('d-none');
    return;
  }

  btn.disabled = true;
  fetch('<c:url value="/do/admin/productdescriptions/save"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({ product: product, type: type, description: description, tips: tips })
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      btn.disabled = false;
      if (data.success) {
        _pdModal.hide();
        _pdData[_pdKey(product, type)] = { description: description, tips: tips };
        pdRenderRow(product, type, description, tips);
        pdUpdateEmptyMessage();
        pdShowSuccess('Product description saved successfully.');
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

function _pdFindRow(product, type) {
  return document.querySelector('#pdTableBody tr[data-pd-product="' + CSS.escape(product) + '"][data-pd-type="' + CSS.escape(type || '') + '"]');
}

function pdRenderRow(product, type, description, tips) {
  var existing = _pdFindRow(product, type);
  if (existing) {
    var cells = existing.querySelectorAll('.pd-desc-cell');
    cells[0].textContent = description; cells[0].title = description;
    cells[1].textContent = tips; cells[1].title = tips;
    existing.dataset.pdDescription = description;
    existing.dataset.pdTips = tips;
    return;
  }
  var row = document.createElement('tr');
  row.dataset.pdProduct = product;
  row.dataset.pdType = type || '';
  row.dataset.pdDescription = description;
  row.dataset.pdTips = tips;
  row.dataset.pdKnown = '1'; // newly-added rows are assumed known until the page is reloaded
  row.innerHTML =
    '<td><code></code></td>' +
    '<td class="pd-type-cell"></td>' +
    '<td><div class="pd-desc-cell"></div></td>' +
    '<td><div class="pd-desc-cell"></div></td>' +
    '<td class="text-end">' +
      '<button type="button" class="btn btn-sm btn-outline-secondary me-1" title="Edit">' +
        '<i class="bi bi-pencil-fill"></i></button>' +
      '<button type="button" class="btn btn-sm btn-outline-danger" title="Delete">' +
        '<i class="bi bi-trash-fill"></i></button>' +
    '</td>';
  row.querySelector('code').textContent = product;
  var typeCell = row.querySelector('.pd-type-cell');
  if (type) {
    var code = document.createElement('code');
    code.textContent = type;
    typeCell.appendChild(code);
  } else {
    var span = document.createElement('span');
    span.className = 'text-muted fst-italic';
    span.textContent = 'all types';
    typeCell.appendChild(span);
  }
  var cells = row.querySelectorAll('.pd-desc-cell');
  cells[0].textContent = description; cells[0].title = description;
  cells[1].textContent = tips; cells[1].title = tips;
  row.querySelector('.btn-outline-secondary').addEventListener('click', function() { pdOpenEdit(product, type); });
  row.querySelector('.btn-outline-danger').addEventListener('click', function() { pdConfirmDelete(product, type); });
  document.getElementById('pdTableBody').appendChild(row);
}

function pdConfirmDelete(product, type) {
  _pdDeleteProduct = product;
  _pdDeleteType = type || '';
  var label = type ? ('<b>' + product + '</b> / <code>' + type + '</code>') : ('<b>' + product + '</b> (all types)');
  confirmationDialog({
    title: 'Delete Product Description',
    message: 'Delete the entry configured for ' + label + '? '
           + 'The {{DESCRIPTION}} placeholder will no longer include it until a new entry is added.',
    confirmText: 'Delete',
    showLoading: false,
    onConfirm: pdDelete
  });
}

function pdDelete() {
  fetch('<c:url value="/do/admin/productdescriptions/delete"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({ product: _pdDeleteProduct, type: _pdDeleteType })
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      if (data.success) {
        var row = _pdFindRow(_pdDeleteProduct, _pdDeleteType);
        if (row) row.remove();
        delete _pdData[_pdKey(_pdDeleteProduct, _pdDeleteType)];
        pdUpdateEmptyMessage();
        pdShowSuccess('Product description deleted successfully.');
      } else {
        alert('Delete failed: ' + (data.error || 'unknown'));
      }
    }).catch(function(e) {
      alert('Network error: ' + e.message);
    });
}
</script>
