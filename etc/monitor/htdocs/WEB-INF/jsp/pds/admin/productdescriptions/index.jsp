<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String pdError = (String) request.getAttribute("pdError");
%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-card-text text-secondary flex-shrink-0"></i>
<span>Free-text descriptions for individual products, available as the <code>{{DESCRIPTION}}</code> placeholder in <a href="/do/admin/productmessages">Product Status Messages</a>.</span>
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
    max-width: 640px;
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
  <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <div class="input-group input-group-sm" style="width:auto">
      <span class="input-group-text"><i class="bi bi-search"></i></span>
      <input type="text" id="pdSearch" class="form-control" placeholder="Search product or text..." style="min-width:200px">
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
    <p class="mb-1">Add a short free-text description for any product (e.g. <code>GOPER</code>, as seen in
      <code>/do/monitoring/summary/GOPER/06</code>). Once configured, a description is available as the
      <code>{{DESCRIPTION}}</code> placeholder inside <a href="/do/admin/productmessages">Product Status Messages</a>
      ("Products Delay"/"Products Resumed"), alongside the existing <code>{{PRODUCT}}</code> and <code>{{CYCLE}}</code>
      placeholders. Products without a configured description simply substitute an empty string.</p>
    <ul class="mb-1 ps-3">
      <li><strong>Product</strong> &mdash; the product name, as shown in the monitoring interface (e.g. <code>/do/monitoring/summary/GOPER/06</code>).</li>
      <li><strong>Description</strong> &mdash; free text, used as the <code>{{DESCRIPTION}}</code> placeholder.</li>
      <li><i class="bi bi-exclamation-triangle-fill text-warning"></i> &mdash; shown next to a product that does not currently
        exist in the <a href="/do/monitoring">monitoring interface</a> (e.g. a typo, or a product that is no longer active).</li>
      <li><strong>Unknown only</strong> &mdash; shows only products flagged as above. Use with <em>Delete All Unknown</em> to clean up stale descriptions.</li>
    </ul>
  </div>
</div>

<div class="card-body p-0">
<div class="table-responsive">
<table class="table table-sm table-hover table-striped align-middle mb-0 dataTable" id="pdTable" style="width:100%">
  <thead class="table-warning">
    <tr>
      <th style="width:160px;">Product</th>
      <th>Description</th>
      <th style="width:100px;" class="text-end">Actions</th>
    </tr>
  </thead>
  <tbody id="pdTableBody">
    <c:forEach var="entry" items="${productDescriptions}">
    <tr id="pdRow_${entry.product}" data-pd-product="<c:out value="${entry.product}" />"
        data-pd-description="<c:out value="${entry.description}" />"
        data-pd-known="${entry.known ? 1 : 0}">
      <td>
        <code><c:out value="${entry.product}" /></code>
        <c:if test="${!entry.known}">
        <i class="bi bi-exclamation-triangle-fill text-warning ms-1"
           title="This product does not currently exist in the monitoring interface (/do/monitoring)."></i>
        </c:if>
      </td>
      <td><div class="pd-desc-cell" title="<c:out value="${entry.description}" />"><c:out value="${entry.description}" /></div></td>
      <td class="text-end">
        <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                onclick="pdOpenEdit('<c:out value="${entry.product}" />')" title="Edit">
          <i class="bi bi-pencil-fill"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline-danger"
                onclick="pdConfirmDelete('<c:out value="${entry.product}" />')" title="Delete">
          <i class="bi bi-trash-fill"></i>
        </button>
      </td>
    </tr>
    </c:forEach>
    <tr id="pdEmptyRow" style="display:none;">
      <td colspan="3" class="text-muted text-center py-3">No product descriptions found.</td>
    </tr>
  </tbody>
</table>
<div class="d-flex align-items-center mt-2 px-3 pb-2" id="pdDtInfo" style="font-size:0.85rem;"></div>
</div>
</div>
</div>

<div class="mt-3 mb-3" id="pdDeleteAllUnknownWrapper" style="display:none">
    <button id="pdDeleteAllUnknownBtn" type="button" class="btn btn-outline-danger"
            title="Delete all product descriptions for products that do not exist in the monitoring interface">
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
                <div class="mb-3">
                    <label for="pdProduct" class="form-label fw-semibold">Product</label>
                    <input type="text" class="form-control" id="pdProduct" list="pdKnownProducts"
                           placeholder="e.g. GOPER" autocomplete="off">
                    <div class="form-text text-muted">Letters, digits, underscores, dots or hyphens only.</div>
                </div>
                <div class="mb-1">
                    <label for="pdDescription" class="form-label fw-semibold">Description</label>
                    <textarea class="form-control" id="pdDescription" rows="4"
                              placeholder="Short description used as the {{DESCRIPTION}} placeholder"></textarea>
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
var _pdEditingProduct = null; // null = adding a new product
var _pdDeleteProduct = null;
var _pdData = {};
var _pdAllRows = [];
var _pdPage = 0;
var _pdPageLen = 25;
var _PD_LEN_KEY = 'pdPageLen';
var _pdUnknownOnly = false;

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
      message: 'Delete all <strong>' + n + '</strong> product description' + (n !== 1 ? 's' : '')
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
              pdShowSuccess('Deleted ' + data.deleted + ' unknown product description' + (data.deleted !== 1 ? 's' : '') + '.');
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
  // Rebuild the product -> description lookup used by the Edit modal from the rendered rows' data attributes.
  _pdData = {};
  _pdAllRows.forEach(function(tr) {
    _pdData[tr.dataset.pdProduct] = tr.dataset.pdDescription || '';
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
        || (tr.dataset.pdDescription || '').toLowerCase().indexOf(q) >= 0;
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
          : '<span class="text-muted">No product descriptions match your search.</span>');
    return;
  }
  var showing = 'Showing ' + (start + 1) + ' to ' + end + ' of ' + total
              + ' product' + (total !== 1 ? 's' : '');
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
  _pdEditingProduct = null;
  document.getElementById('pdModalTitle').textContent = 'Add Description';
  document.getElementById('pdProduct').value = '';
  document.getElementById('pdProduct').disabled = false;
  document.getElementById('pdDescription').value = '';
  document.getElementById('pdSaveError').classList.add('d-none');
  _pdModal.show();
}

function pdOpenEdit(product) {
  _pdEditingProduct = product;
  document.getElementById('pdModalTitle').textContent = 'Edit Description';
  document.getElementById('pdProduct').value = product;
  document.getElementById('pdProduct').disabled = true;
  document.getElementById('pdDescription').value = _pdData[product] || '';
  document.getElementById('pdSaveError').classList.add('d-none');
  _pdModal.show();
}

function pdSave() {
  var btn = document.getElementById('pdSaveBtn');
  var errEl = document.getElementById('pdSaveError');
  errEl.classList.add('d-none');

  var product = document.getElementById('pdProduct').value.trim();
  var description = document.getElementById('pdDescription').value;

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

  btn.disabled = true;
  fetch('<c:url value="/do/admin/productdescriptions/save"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({ product: product, description: description })
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      btn.disabled = false;
      if (data.success) {
        _pdModal.hide();
        _pdData[product] = description;
        pdRenderRow(product, description);
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

function pdRenderRow(product, description) {
  var existing = document.getElementById('pdRow_' + product);
  var descCell, row;
  if (existing) {
    descCell = existing.querySelector('.pd-desc-cell');
    descCell.textContent = description;
    descCell.title = description;
    existing.dataset.pdDescription = description;
    return;
  }
  row = document.createElement('tr');
  row.id = 'pdRow_' + product;
  row.dataset.pdProduct = product;
  row.dataset.pdDescription = description;
  row.innerHTML =
    '<td><code></code></td>' +
    '<td><div class="pd-desc-cell"></div></td>' +
    '<td class="text-end">' +
      '<button type="button" class="btn btn-sm btn-outline-secondary me-1" title="Edit">' +
        '<i class="bi bi-pencil-fill"></i></button>' +
      '<button type="button" class="btn btn-sm btn-outline-danger" title="Delete">' +
        '<i class="bi bi-trash-fill"></i></button>' +
    '</td>';
  row.querySelector('code').textContent = product;
  var descCell = row.querySelector('.pd-desc-cell');
  descCell.textContent = description;
  descCell.title = description;
  row.querySelector('.btn-outline-secondary').addEventListener('click', function() { pdOpenEdit(product); });
  row.querySelector('.btn-outline-danger').addEventListener('click', function() { pdConfirmDelete(product); });
  document.getElementById('pdTableBody').appendChild(row);
}

function pdConfirmDelete(product) {
  _pdDeleteProduct = product;
  confirmationDialog({
    title: 'Delete Product Description',
    message: 'Delete the description configured for <b>' + product + '</b>? '
           + 'The {{DESCRIPTION}} placeholder will substitute an empty string for this product until a new '
           + 'description is added.',
    confirmText: 'Delete',
    showLoading: false,
    onConfirm: pdDelete
  });
}

function pdDelete() {
  fetch('<c:url value="/do/admin/productdescriptions/delete"/>', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
    body: JSON.stringify({ product: _pdDeleteProduct })
  }).then(function(r){ return r.json(); })
    .then(function(data) {
      if (data.success) {
        var row = document.getElementById('pdRow_' + _pdDeleteProduct);
        if (row) row.remove();
        delete _pdData[_pdDeleteProduct];
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
