<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String pmError   = (String) request.getAttribute("pmError");
    final String pmSuccess = (String) request.getAttribute("pmSuccess");
    final boolean delayCustomized          = Boolean.TRUE.equals(request.getAttribute("delayMessageCustomized"));
    final boolean resumedCustomized        = Boolean.TRUE.equals(request.getAttribute("resumedMessageCustomized"));
    final boolean delayGroupedCustomized   = Boolean.TRUE.equals(request.getAttribute("delayMessageGroupedCustomized"));
    final boolean resumedGroupedCustomized = Boolean.TRUE.equals(request.getAttribute("resumedMessageGroupedCustomized"));
    final boolean compactCycleLists        = !Boolean.FALSE.equals(request.getAttribute("compactCycleLists"));
%>

<%-- Header: title + info button --%>
<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(108,117,125,0.07); color:var(--bs-body-color); border-left:4px solid #6c757d;">
    <i class="bi bi-envelope-paper-fill text-secondary flex-shrink-0"></i>
    <span>
        <strong>Product Messages</strong> &mdash; customize the email templates used for delay/resume notifications
    </span>
    <button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
        data-bs-toggle="collapse" data-bs-target="#pmInfoPanel"
        aria-expanded="false" title="About this page">
        <i class="bi bi-info-circle"></i>
    </button>
</div>

<%-- Info panel --%>
<div class="collapse mb-3" id="pmInfoPanel">
  <div class="card-body py-2 px-3 border rounded" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid #6c757d!important;">
    <p class="mb-1">These messages pre-fill the Outlook email body when an administrator uses the
    <strong>"Notify Delay"</strong> or <strong>"Notify Resumed"</strong> links on the Product Status page. Leave a
    field empty (or unchanged) to keep using the built-in default text.</p>
    <ul class="mb-0 ps-3">
        <li><strong>Storage</strong> &mdash; stored in the database, so they can be customized per site without a
        code change. Clearing a field (submitting it empty or unchanged from the default) reverts it to the built-in
        default text. Changes take effect immediately on the Product Status page for all users.</li>
        <li><strong>Grouped vs. Ungrouped</strong> &mdash; each message below has two variants. The
        <strong>Grouped</strong> variant is used instead of the <strong>Ungrouped</strong> one on a product's
        merged, all-cycles page (<code>/do/monitoring/summary/{product}</code>, no cycle in the URL) &mdash; i.e. a
        product with <a href="/do/admin/productdescriptions">"Group all cycles/times into one page"</a> enabled.
        Every other page (a single product/cycle) always uses the Ungrouped variant.</li>
        <li><strong>Placeholders</strong> &mdash; <code>{{PRODUCT}}</code> is replaced with the product of the page
        the message is sent from (e.g. <code>GENFO</code>). <code>{{CYCLE}}</code> is replaced with the single
        cycle being viewed (e.g. <code>06</code> on <code>/do/monitoring/summary/GENFO/06</code>; empty on a grouped
        page, which has no single cycle). <code>{{CYCLES}}</code> is replaced with every cycle currently shown
        (e.g. <code>00-03,06-07,12</code> on a grouped page; the same single value as <code>{{CYCLE}}</code>
        otherwise) &mdash; see <strong>Cycle List Formatting</strong> below.</li>
        <li><strong>Description placeholder</strong> &mdash; <code>{{DESCRIPTION}}</code> is replaced with the
        description(s) configured under <a href="/do/admin/productdescriptions">Admin Tasks &rarr; Product
        Descriptions</a> for the current product: a plain text if only one type applies (or a type-independent
        description was configured), a bullet list (one line per type, e.g. <code>- AN: ...</code>) when several
        types shown on the page each resolve to a different description, or an empty string if none has been
        configured.</li>
        <li><strong>Edit markers</strong> &mdash; the <code>&lt;&lt;...&gt;&gt;</code> placeholder markers highlight
        text that should be edited before sending.</li>
    </ul>
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
        <div class="card-header fw-semibold d-flex align-items-center justify-content-between flex-wrap gap-2">
            <span><i class="bi bi-hourglass-split me-2"></i>Products Delay Message</span>
            <span class="d-flex align-items-center gap-2">
            <div class="btn-group btn-group-sm" role="group" aria-label="Products Delay Message body variant">
              <button type="button" class="btn btn-outline-secondary active" data-variant="ungrouped"
                      onclick="pmSetVariant('delay','ungrouped')">Ungrouped</button>
              <button type="button" class="btn btn-outline-secondary" data-variant="grouped"
                      onclick="pmSetVariant('delay','grouped')">Grouped</button>
            </div>
            <span id="delayDirtyBadge" class="badge text-bg-warning" style="display:none;">
                <i class="bi bi-pencil-fill me-1"></i>Unsaved changes
            </span>
            <span id="delayCustomizedBadge" class="badge <%= delayCustomized ? "text-bg-info" : "text-bg-secondary" %>"
                  data-ungrouped-customized="<%= delayCustomized %>" data-grouped-customized="<%= delayGroupedCustomized %>">
                <%= delayCustomized ? "Customized" : "Default" %>
            </span>
            </span>
        </div>
        <div class="card-body">
            <label class="form-label fw-semibold" id="delayVariantLabel">Message body <span
                class="text-muted fw-normal">(Ungrouped &mdash; uses <code>{{CYCLE}}</code>)</span></label>
            <textarea class="form-control" id="delayMessage" name="delayMessage" rows="12"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${delayMessage}" /></textarea>
            <textarea class="form-control" id="delayMessageGrouped" name="delayMessageGrouped" rows="12"
                      style="font-family:monospace; font-size:0.85rem; display:none;"><c:out value="${delayMessageGrouped}" /></textarea>
            <div class="form-text text-muted">Supports the <code>&lt;&lt;...&gt;&gt;</code> placeholder markers to
            highlight text that should be edited before sending.</div>
        </div>
    </div>

    <div class="card shadow-sm mb-4" id="resumedCard">
        <div class="card-header fw-semibold d-flex align-items-center justify-content-between flex-wrap gap-2">
            <span><i class="bi bi-check2-circle me-2"></i>Products Resumed Message</span>
            <span class="d-flex align-items-center gap-2">
            <div class="btn-group btn-group-sm" role="group" aria-label="Products Resumed Message body variant">
              <button type="button" class="btn btn-outline-secondary active" data-variant="ungrouped"
                      onclick="pmSetVariant('resumed','ungrouped')">Ungrouped</button>
              <button type="button" class="btn btn-outline-secondary" data-variant="grouped"
                      onclick="pmSetVariant('resumed','grouped')">Grouped</button>
            </div>
            <span id="resumedDirtyBadge" class="badge text-bg-warning" style="display:none;">
                <i class="bi bi-pencil-fill me-1"></i>Unsaved changes
            </span>
            <span id="resumedCustomizedBadge" class="badge <%= resumedCustomized ? "text-bg-info" : "text-bg-secondary" %>"
                  data-ungrouped-customized="<%= resumedCustomized %>" data-grouped-customized="<%= resumedGroupedCustomized %>">
                <%= resumedCustomized ? "Customized" : "Default" %>
            </span>
            </span>
        </div>
        <div class="card-body">
            <label class="form-label fw-semibold" id="resumedVariantLabel">Message body <span
                class="text-muted fw-normal">(Ungrouped &mdash; uses <code>{{CYCLE}}</code>)</span></label>
            <textarea class="form-control" id="resumedMessage" name="resumedMessage" rows="8"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${resumedMessage}" /></textarea>
            <textarea class="form-control" id="resumedMessageGrouped" name="resumedMessageGrouped" rows="8"
                      style="font-family:monospace; font-size:0.85rem; display:none;"><c:out value="${resumedMessageGrouped}" /></textarea>
        </div>
    </div>

    <div class="card shadow-sm mb-4" id="cycleFormatCard">
        <div class="card-header fw-semibold"><i class="bi bi-list-ol me-2"></i>Cycle List Formatting</div>
        <div class="card-body">
            <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" id="compactCycleLists" name="compactCycleLists"
                       <%= compactCycleLists ? "checked" : "" %>>
                <label class="form-check-label" for="compactCycleLists">Automatically compact grouped cycle lists in
                the <code>{{CYCLES}}</code> placeholder</label>
            </div>
            <div class="form-text text-muted">When enabled (default), a long list of cycles is collapsed into ranges
            for readability, e.g. <code>00,01,02,03,06,07,12</code> becomes <code>00-03,06-07,12</code>. Disable to
            always show the complete, uncompacted list.</div>
        </div>
    </div>

    <div class="d-flex align-items-center gap-2 mb-4">
        <button type="submit" class="btn btn-warning">
            <i class="bi bi-save-fill me-1"></i>Save Messages
        </button>
    </div>
</form>


<script>
(function() {
  var _pmDirty = false;
  var form = document.querySelector('form[action="/do/admin/productmessages"]');
  if (!form) return;

  var fields = [
    { textarea: 'delayMessage',          badge: 'delayDirtyBadge',   card: 'delayCard' },
    { textarea: 'delayMessageGrouped',   badge: 'delayDirtyBadge',   card: 'delayCard' },
    { textarea: 'resumedMessage',        badge: 'resumedDirtyBadge', card: 'resumedCard' },
    { textarea: 'resumedMessageGrouped', badge: 'resumedDirtyBadge', card: 'resumedCard' }
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

// Ungrouped/Grouped message body toggle: shows the matching textarea and re-labels/re-badges the card.
function pmSetVariant(which, variant) {
  var grouped = variant === 'grouped';
  var ungroupedEl = document.getElementById(which + 'Message');
  var groupedEl = document.getElementById(which + 'MessageGrouped');
  var label = document.getElementById(which + 'VariantLabel');
  var badge = document.getElementById(which + 'CustomizedBadge');
  if (!ungroupedEl || !groupedEl) return;
  ungroupedEl.style.display = grouped ? 'none' : '';
  groupedEl.style.display   = grouped ? '' : 'none';
  if (label) {
    label.innerHTML = 'Message body <span class="text-muted fw-normal">(' +
      (grouped ? 'Grouped &mdash; uses <code>{{CYCLES}}</code>' : 'Ungrouped &mdash; uses <code>{{CYCLE}}</code>') +
      ')</span>';
  }
  var card = ungroupedEl.closest('.card');
  if (card) {
    card.querySelectorAll('[data-variant]').forEach(function(b) {
      b.classList.toggle('active', b.getAttribute('data-variant') === variant);
    });
  }
  if (badge) {
    var customized = badge.getAttribute(grouped ? 'data-grouped-customized' : 'data-ungrouped-customized') === 'true';
    badge.textContent = customized ? 'Customized' : 'Default';
    badge.className = 'badge ' + (customized ? 'text-bg-info' : 'text-bg-secondary');
  }
  try { localStorage.setItem('pm' + which + 'Variant', variant); } catch (e) {}
}

document.addEventListener('DOMContentLoaded', function() {
  ['delay', 'resumed'].forEach(function(which) {
    var variant = 'ungrouped';
    try { variant = localStorage.getItem('pm' + which + 'Variant') || 'ungrouped'; } catch (e) {}
    pmSetVariant(which, variant);
  });
});
</script>
