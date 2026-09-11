<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String pmError   = (String) request.getAttribute("pmError");
    final String pmSuccess = (String) request.getAttribute("pmSuccess");
    final boolean delayCustomized   = Boolean.TRUE.equals(request.getAttribute("delayMessageCustomized"));
    final boolean resumedCustomized = Boolean.TRUE.equals(request.getAttribute("resumedMessageCustomized"));
%>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(108,117,125,0.07); border-left:4px solid #6c757d; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-envelope-paper-fill text-secondary flex-shrink-0 mt-1"></i>
        <span>
            These messages pre-fill the Outlook email body when an administrator uses the <strong>"Notify
            Delay"</strong> or <strong>"Notify Resumed"</strong> links on the Product Status page. Leave a field
            empty (or unchanged) to keep using the built-in default text. Use the placeholders <code>{{PRODUCT}}</code>,
            <code>{{CYCLE}}</code> and <code>{{DESCRIPTION}}</code> to automatically insert the product name (e.g.
            <code>GENFO</code>), cycle (e.g. <code>06</code>) of the page the message is sent from, and the
            <a href="/do/admin/productdescriptions">description(s) configured for that product</a> &mdash; rendered as
            a bullet list across the types shown on the page, if more than one applies.
        </span>
    </div>
</div>

<% if (pmError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=pmError%></span>
</div>
<% } %>

<% if (pmSuccess != null) { %>
<div class="alert alert-success alert-dismissible fade show d-flex gap-2 mb-4" role="alert" id="pmSuccessAlert">
    <i class="bi bi-check-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=pmSuccess%></span>
    <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<script>
  setTimeout(function() {
    var el = document.getElementById('pmSuccessAlert');
    if (el) { bootstrap.Alert.getOrCreateInstance(el).close(); }
  }, 4000);
</script>
<% } %>

<form method="post" action="/do/admin/productmessages">

    <div class="card shadow-sm mb-4" id="delayCard">
        <div class="card-header fw-semibold d-flex align-items-center justify-content-between">
            <span><i class="bi bi-hourglass-split me-2"></i>Products Delay Message</span>
            <span class="d-flex align-items-center gap-1">
            <span id="delayDirtyBadge" class="badge text-bg-warning" style="display:none;">
                <i class="bi bi-pencil-fill me-1"></i>Unsaved changes
            </span>
            <% if (delayCustomized) { %>
            <span class="badge text-bg-info">Customized</span>
            <% } else { %>
            <span class="badge text-bg-secondary">Default</span>
            <% } %>
            </span>
        </div>
        <div class="card-body">
            <label for="delayMessage" class="form-label fw-semibold">Message body</label>
            <textarea class="form-control" id="delayMessage" name="delayMessage" rows="12"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${delayMessage}" /></textarea>
            <div class="form-text text-muted">Supports the <code>&lt;&lt;...&gt;&gt;</code> placeholder markers to
            highlight text that should be edited before sending.</div>
        </div>
    </div>

    <div class="card shadow-sm mb-4" id="resumedCard">
        <div class="card-header fw-semibold d-flex align-items-center justify-content-between">
            <span><i class="bi bi-check2-circle me-2"></i>Products Resumed Message</span>
            <span class="d-flex align-items-center gap-1">
            <span id="resumedDirtyBadge" class="badge text-bg-warning" style="display:none;">
                <i class="bi bi-pencil-fill me-1"></i>Unsaved changes
            </span>
            <% if (resumedCustomized) { %>
            <span class="badge text-bg-info">Customized</span>
            <% } else { %>
            <span class="badge text-bg-secondary">Default</span>
            <% } %>
            </span>
        </div>
        <div class="card-body">
            <label for="resumedMessage" class="form-label fw-semibold">Message body</label>
            <textarea class="form-control" id="resumedMessage" name="resumedMessage" rows="8"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${resumedMessage}" /></textarea>
        </div>
    </div>

    <div class="d-flex align-items-center gap-2 mb-4">
        <button type="submit" class="btn btn-warning">
            <i class="bi bi-save-fill me-1"></i>Save Messages
        </button>
    </div>
</form>


<div class="card shadow-sm" style="max-width:720px; border-color: #dee2e6;">
    <div class="card-header text-muted fw-semibold" style="background:rgba(108,117,125,0.07);">
        <i class="bi bi-info-circle me-2"></i>About Product Messages
    </div>
    <div class="card-body text-muted" style="font-size:0.85rem;">
        <ul class="mb-0 ps-3">
            <li>Stored in the database, so they can be customized per site without a code change.</li>
            <li>Clearing a field (submitting it empty or unchanged from the default) reverts it to the built-in
            default text shown above.</li>
            <li>Changes take effect immediately on the Product Status page for all users.</li>
            <li>The <code>{{PRODUCT}}</code> and <code>{{CYCLE}}</code> placeholders are replaced with the product
            and cycle of the page the message is sent from (e.g. <code>GENFO</code> and <code>06</code> on
            <code>/do/monitoring/summary/GENFO/06</code>).</li>
            <li>The <code>{{DESCRIPTION}}</code> placeholder is replaced with the description(s) configured under
            <a href="/do/admin/productdescriptions">Admin Tasks &rarr; Product Descriptions</a> for the current
            product: a plain text if only one type applies (or a type-independent description was configured), a
            bullet list (one line per type, e.g. <code>- AN: ...</code>) when several types shown on the page each
            resolve to a different description, or an empty string if none has been configured.</li>
        </ul>
    </div>
</div>

<script>
(function() {
  var _pmDirty = false;
  var form = document.querySelector('form[action="/do/admin/productmessages"]');
  if (!form) return;

  var fields = [
    { textarea: 'delayMessage',   badge: 'delayDirtyBadge',   card: 'delayCard' },
    { textarea: 'resumedMessage', badge: 'resumedDirtyBadge', card: 'resumedCard' }
  ];

  fields.forEach(function(f) {
    var el = document.getElementById(f.textarea);
    var badge = document.getElementById(f.badge);
    var card = document.getElementById(f.card);
    if (!el) return;
    var original = el.value;
    el.addEventListener('input', function() {
      var fieldDirty = el.value !== original;
      if (badge) badge.style.display = fieldDirty ? 'inline-block' : 'none';
      if (card) card.classList.toggle('border-warning', fieldDirty);
      _pmDirty = form.querySelectorAll('textarea').length && Array.from(form.querySelectorAll('textarea'))
        .some(function(t) { return t.value !== t.defaultValue; });
    });
  });

  form.addEventListener('submit', function() {
    _pmDirty = false;
    fields.forEach(function(f) {
      var badge = document.getElementById(f.badge);
      var card = document.getElementById(f.card);
      if (badge) badge.style.display = 'none';
      if (card) card.classList.remove('border-warning');
    });
  });

  // Warn on browser tab close/refresh/external navigation
  window.addEventListener('beforeunload', function(e) {
    if (!_pmDirty) return;
    e.preventDefault();
    e.returnValue = '';
  });

  // Intercept in-app anchor navigation when there are unsaved changes
  document.addEventListener('click', function(e) {
    if (!_pmDirty) return;
    var anchor = e.target.closest('a[href]');
    if (!anchor) return;
    var href = anchor.getAttribute('href');
    if (!href || href === '#' || href.startsWith('javascript:') || href.startsWith('mailto:')) return;
    e.preventDefault();
    e.stopImmediatePropagation();
    var target = href;
    confirmationDialog({
      title: '<i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>Unsaved Changes',
      message: 'You have unsaved Product Status Message changes. If you leave now, your changes will be lost.',
      confirmText: 'Leave without saving',
      cancelText: 'Stay and save',
      showLoading: false,
      onConfirm: function() { _pmDirty = false; window.location.href = target; }
    });
  }, true);
}());
</script>
