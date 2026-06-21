<%@ page session="true"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<style>
.rating-stars { color: #f59e0b; letter-spacing: 0.05em; }
.badge-reviewed { background: rgba(25,135,84,0.12); color: #198754; border: 1px solid rgba(25,135,84,0.2); }
.badge-pending  { background: rgba(255,193,7,0.12);  color: #996600; border: 1px solid rgba(255,193,7,0.3); }
.fbk-cell-truncate { max-width: 220px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; cursor: pointer; }
.fbk-cell-truncate:hover { text-decoration: underline dotted; }
</style>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-chat-left-text text-secondary flex-shrink-0"></i>
<span>Feedback submitted by users of <strong><%=System.getProperty("monitor.nickName")%></strong>. Entries marked <em>reviewed</em> have been acknowledged. Delete entries once actioned.</span>
</div>

<c:choose>
<c:when test="${empty feedbackList}">
<div class="alert alert-info d-flex align-items-center gap-2 mt-3">
  <i class="bi bi-info-circle-fill"></i>
  <span>No feedback has been submitted yet.</span>
</div>
</c:when>
<c:otherwise>
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-chat-left-text text-primary"></i>
<span class="fw-semibold">User Feedback</span>
<div class="ms-auto d-flex flex-wrap align-items-center gap-2">
  <div class="input-group input-group-sm" style="width:auto">
    <span class="input-group-text"><i class="bi bi-search"></i></span>
    <input type="text" id="fbkSearch" class="form-control" placeholder="Search..." style="min-width:160px">
  </div>
  <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
    <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
    <select id="fbkPageLen" class="form-select form-select-sm" style="width:auto">
      <option value="10">10</option>
      <option value="25">25</option>
      <option value="50">50</option>
      <option value="100">100</option>
      <option value="250">250</option>
    </select>
  </div>
  <select id="fbkStatusFilter" class="form-select form-select-sm" style="width:auto">
    <option value="">All statuses</option>
    <option value="Pending">Pending</option>
    <option value="Reviewed">Reviewed</option>
  </select>
</div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="fbkTable" class="table table-sm table-hover mb-0 align-middle" style="font-size:0.82rem;">
  <thead class="table-secondary">
    <tr>
      <th>Date</th>
      <th>Rating</th>
      <th>User</th>
      <th>Comment / One Thing / Contact</th>
      <th>Usage</th>
      <th>Component</th>
      <th title="Would recommend?">Rec?</th>
      <th title="May quote?">Quote?</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach var="fb" items="${feedbackList}">
  <tr>
      <td class="text-nowrap text-muted" style="font-size:0.78rem;"><c:out value="${fb.formattedTime}"/></td>
      <td class="text-nowrap">
        <span class="rating-stars">
          <c:forEach begin="1" end="${fb.rating}">&#9733;</c:forEach><c:forEach begin="${fb.rating + 1}" end="5">&#9734;</c:forEach>
        </span>
      </td>
      <td>
        <c:choose>
          <c:when test="${not empty fb.webUserId}">
            <span class="badge bg-primary bg-opacity-10 text-primary border" style="font-size:0.75rem;"><c:out value="${fb.webUserId}"/></span>
          </c:when>
          <c:otherwise><span class="text-muted fst-italic" style="font-size:0.78rem;">anonymous</span></c:otherwise>
        </c:choose>
      </td>
      <td>
        <div class="fbk-cell-truncate"
             data-fbk-comment="<c:out value="${fb.comment}"/>"
             data-fbk-onething="<c:out value="${fb.oneThing}"/>"
             data-fbk-contact="<c:out value="${fb.contact}"/>">
          <c:if test="${not empty fb.comment}"><c:out value="${fb.comment}"/></c:if>
          <c:if test="${empty fb.comment and not empty fb.oneThing}"><i class="bi bi-lightbulb me-1"></i><c:out value="${fb.oneThing}"/></c:if>
          <c:if test="${empty fb.comment and empty fb.oneThing}">&mdash;</c:if>
        </div>
      </td>
      <td><c:choose><c:when test="${not empty fb.usage}"><c:out value="${fb.usage}"/></c:when><c:otherwise>&mdash;</c:otherwise></c:choose></td>
      <td><c:choose><c:when test="${not empty fb.component}"><c:out value="${fb.component}"/></c:when><c:otherwise>&mdash;</c:otherwise></c:choose></td>
      <td class="text-center">
        <c:choose>
          <c:when test="${fb.recommend == true}"><i class="bi bi-hand-thumbs-up text-success"></i></c:when>
          <c:when test="${fb.recommend == false}"><i class="bi bi-hand-thumbs-down text-danger"></i></c:when>
          <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
        </c:choose>
      </td>
      <td class="text-center">
        <c:choose>
          <c:when test="${fb.quoteOk}"><i class="bi bi-check-circle-fill text-success"></i></c:when>
          <c:otherwise><i class="bi bi-x-circle text-muted"></i></c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:choose>
          <c:when test="${fb.reviewed}"><span class="badge badge-reviewed">Reviewed</span></c:when>
          <c:otherwise><span class="badge badge-pending">Pending</span></c:otherwise>
        </c:choose>
      </td>
      <td class="text-nowrap">
        <c:if test="${not fb.reviewed}">
          <a href="javascript:validate('<bean:message key="admin.feedback.basepath"/>/review/${fb.id}','Mark feedback #${fb.id} as reviewed?')"
             class="btn btn-sm btn-outline-success" title="Mark as reviewed">
            <i class="bi bi-check2"></i>
          </a>
        </c:if>
        <a href="javascript:validate('<bean:message key="admin.feedback.basepath"/>/delete/${fb.id}','Delete this feedback entry?')"
           class="btn btn-sm btn-outline-danger" title="Delete">
          <i class="bi bi-trash"></i>
        </a>
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>
</div>
</div>
</div>
</c:otherwise>
</c:choose>

<c:if test="${not empty feedbackList}">
<div class="d-flex gap-2 mt-3">
  <button type="button" class="btn btn-sm btn-outline-danger" id="fbkDeleteReviewedBtn"
          onclick="validate('<bean:message key="admin.feedback.basepath"/>/deleteReviewed/all','Delete all <strong>reviewed</strong> feedback entries? This cannot be undone.')">
    <i class="bi bi-trash me-1"></i>Delete All Reviewed
  </button>
  <button type="button" class="btn btn-sm btn-outline-danger" id="fbkDeleteAllBtn"
          onclick="validate('<bean:message key="admin.feedback.basepath"/>/deleteAll/all','Delete <strong>all</strong> feedback entries (including pending)? This cannot be undone.')">
    <i class="bi bi-trash-fill me-1"></i>Delete All
  </button>
  <button type="button" class="btn btn-sm btn-outline-secondary" id="fbkExportCsvBtn">
    <i class="bi bi-download me-1"></i>Export to CSV
  </button>
</div>
</c:if>

<script>
function validate(path, message) {
  confirmationDialog({
    title: "Please Confirm",
    message: message,
    onConfirm: function() { window.location = path; },
    onCancel: function() {}
  });
}
<c:if test="${not empty feedbackList}">
$(document).ready(function() {
  var _len = (function() { try { var v = parseInt(localStorage.getItem('fbkPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
  $('#fbkPageLen').val(_len);

  var table = $('#fbkTable').DataTable({
    order:      [[0, 'desc']],
    pageLength: _len,
    searching:  true,
    info:       true,
    dom:        't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
    columnDefs: [{ orderable: false, targets: [3, 6, 7, 9] }],
    language: {
      info:       'Showing _START_-_END_ of _TOTAL_ entries',
      emptyTable: 'No feedback yet'
    }
  });

  $('#fbkPageLen').on('change', function() {
    var len = +this.value;
    try { localStorage.setItem('fbkPageLen', len); } catch(e) {}
    table.page.len(len).draw();
  });

  $('#fbkSearch').on('keyup', function() { table.search(this.value).draw(); });

  // Status column (index 8) filter
  $('#fbkStatusFilter').on('change', function() {
    table.column(8).search(this.value).draw();
  });

  // Popover for truncated comment cells — build content from data attributes
  document.querySelectorAll('.fbk-cell-truncate').forEach(function(el) {
    var comment  = el.getAttribute('data-fbk-comment')  || '';
    var oneThing = el.getAttribute('data-fbk-onething') || '';
    var contact  = el.getAttribute('data-fbk-contact')  || '';
    if (!comment && !oneThing && !contact) return;
    var html = '';
    if (comment)  html += '<p class="mb-1" style="white-space:pre-wrap;max-width:320px;font-size:0.82rem;">' + $('<div>').text(comment).html() + '</p>';
    if (oneThing) html += '<p class="mb-1 text-muted" style="font-size:0.78rem;"><i class="bi bi-lightbulb me-1"></i>' + $('<div>').text(oneThing).html() + '</p>';
    if (contact)  html += '<p class="mb-0 text-muted" style="font-size:0.78rem;"><i class="bi bi-envelope me-1"></i>' + $('<div>').text(contact).html() + '</p>';
    $(el).popover({
      html: true, trigger: 'click', placement: 'left',
      title: 'Feedback detail', content: html, sanitize: false
    });
  });
  $(document).on('click', function(e) {
    if (!$(e.target).closest('.fbk-cell-truncate').length) {
      $('.fbk-cell-truncate').popover('hide');
    }
  });

  // CSV export — all rows in current filter/search, all columns
  $('#fbkExportCsvBtn').on('click', function() {
    var headers = [];
    $('#fbkTable thead th').each(function() { headers.push($(this).text().trim()); });
    var rows = [headers];
    table.rows({ search: 'applied' }).nodes().each(function(row) {
      var cols = [];
      $(row).find('td').each(function(i) {
        var cell = $(this);
        // Comment cell: use data attributes for raw text
        var truncDiv = cell.find('.fbk-cell-truncate');
        if (truncDiv.length) {
          var parts = [];
          var c = truncDiv.attr('data-fbk-comment')  || '';
          var o = truncDiv.attr('data-fbk-onething') || '';
          var k = truncDiv.attr('data-fbk-contact')  || '';
          if (c) parts.push(c);
          if (o) parts.push('One thing: ' + o);
          if (k) parts.push('Contact: ' + k);
          cols.push(parts.join(' | '));
        } else {
          cols.push(cell.text().trim().replace(/\s+/g, ' '));
        }
      });
      rows.push(cols);
    });
    var csv = rows.map(function(r) {
      return r.map(function(v) { return '"' + (v || '').replace(/"/g, '""') + '"'; }).join(',');
    }).join('\r\n');
    var blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    var url  = URL.createObjectURL(blob);
    var a    = document.createElement('a');
    a.href = url; a.download = 'feedback.csv'; a.click();
    URL.revokeObjectURL(url);
  });
});
</c:if>
</script>
