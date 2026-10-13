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
    <a href="#" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0" onclick="var el=document.getElementById('pmGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;" title="Open Configuration Guide"><i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Configuration </span>Guide</a>
</div>

<% if (pmError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=pmError%></span>
</div>
<% } %>

<% if (pmSuccess != null) { %>
<div class="alert alert-success fade show d-flex align-items-center gap-2 mb-4" role="alert" id="pmSuccessAlert">
    <i class="bi bi-check-circle-fill flex-shrink-0"></i>
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
            <button type="button" class="btn btn-sm btn-outline-secondary py-0" onclick="pmResetCurrentDefault('delay')">
                <i class="bi bi-arrow-counterclockwise me-1"></i>Reset current variant
            </button>
            </span>
        </div>
        <div class="card-body">
            <label class="form-label fw-semibold" id="delayVariantLabel">Message body <span
                class="text-muted fw-normal">(Ungrouped &mdash; e.g. <code>{{PRODUCT}}</code>, <code>{{CYCLE}}</code>)</span></label>
                        <div class="d-flex flex-wrap align-items-center gap-1 mb-2 pm-placeholders" id="delayPlaceholders" style="font-size:0.8rem;">
                <span class="text-muted me-1"><i class="bi bi-braces me-1"></i>Insert at cursor:</span> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped" title="Product name" onclick="pmInsertPlaceholder('delay', '{{PRODUCT}}')">{{PRODUCT}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Product(s): the product of the page, or all the selected products on the Delay Notification page" onclick="pmInsertPlaceholder('delay', '{{PRODUCTS}}')">{{PRODUCTS}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped" title="Single cycle (ungrouped pages)" onclick="pmInsertPlaceholder('delay', '{{CYCLE}}')">{{CYCLE}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="All cycles shown (grouped pages)" onclick="pmInsertPlaceholder('delay', '{{CYCLES}}')">{{CYCLES}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped grouped" title="Product description(s)" onclick="pmInsertPlaceholder('delay', '{{DESCRIPTION}}')">{{DESCRIPTION}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="List of affected products and cycles (Delay Notification page)" onclick="pmInsertPlaceholder('delay', '{{AFFECTED}}')">{{AFFECTED}}</button> 
                <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Reason entered on the Delay Notification page" onclick="pmInsertPlaceholder('delay', '{{REASON}}')">{{REASON}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="ETA entered on the Delay Notification page, in UTC" onclick="pmInsertPlaceholder('delay', '{{ETA}}')">{{ETA}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Expected delay selected on the Delay Notification page" onclick="pmInsertPlaceholder('delay', '{{EXPECTED_DELAY}}')">{{EXPECTED_DELAY}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Note entered on the Delay Notification page" onclick="pmInsertPlaceholder('delay', '{{NOTE}}')">{{NOTE}}</button>
                <span class="small text-muted ms-1">Green = used</span>
            </div>
            <textarea class="form-control" id="delayMessage" name="delayMessage" rows="12"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${delayMessage}" /></textarea>
            <textarea class="form-control" id="delayMessageGrouped" name="delayMessageGrouped" rows="12"
                      style="font-family:monospace; font-size:0.85rem; display:none;"><c:out value="${delayMessageGrouped}" /></textarea>
            <textarea id="defaultDelayMessage" class="d-none"><c:out value="${defaultDelayMessage}" /></textarea>
            <textarea id="defaultDelayMessageGrouped" class="d-none"><c:out value="${defaultDelayMessageGrouped}" /></textarea>
            <div class="alert alert-warning py-2 small mt-2 mb-0 d-none" id="delayUnknownWarning" role="status">
                <i class="bi bi-exclamation-triangle-fill me-1"></i><strong>Unknown placeholder(s):</strong>
                <code id="delayUnknownTokens"></code>
                <div>Unrecognized placeholders are not replaced and will remain in the message. Check the spelling or use a supported placeholder from the buttons above.</div>
            </div>
            <div id="affectedHint" class="alert alert-info py-2 small mt-2 mb-0" style="display:none;">
                The Grouped message has no <code>{{AFFECTED}}</code> placeholder: the Delay Notification page then inserts
                the list of affected products and cycles after the first paragraph (or put the cursor where you want it and use the buttons above).
                <button type="button" class="btn btn-sm btn-outline-primary ms-2" onclick="pmInsertPlaceholder('delay', '{{AFFECTED}}')">Insert {{AFFECTED}} at the cursor</button>
            </div>
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
            <button type="button" class="btn btn-sm btn-outline-secondary py-0" onclick="pmResetCurrentDefault('resumed')">
                <i class="bi bi-arrow-counterclockwise me-1"></i>Reset current variant
            </button>
            </span>
        </div>
        <div class="card-body">
            <label class="form-label fw-semibold" id="resumedVariantLabel">Message body <span
                class="text-muted fw-normal">(Ungrouped &mdash; e.g. <code>{{PRODUCT}}</code>, <code>{{CYCLE}}</code>)</span></label>
                        <div class="d-flex flex-wrap align-items-center gap-1 mb-2 pm-placeholders" id="resumedPlaceholders" style="font-size:0.8rem;">
                <span class="text-muted me-1"><i class="bi bi-braces me-1"></i>Insert at cursor:</span> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped" title="Product name" onclick="pmInsertPlaceholder('resumed', '{{PRODUCT}}')">{{PRODUCT}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Product(s): the product of the page, or all the selected products on the Delay Notification page" onclick="pmInsertPlaceholder('resumed', '{{PRODUCTS}}')">{{PRODUCTS}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped" title="Single cycle (ungrouped pages)" onclick="pmInsertPlaceholder('resumed', '{{CYCLE}}')">{{CYCLE}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="All cycles shown (grouped pages)" onclick="pmInsertPlaceholder('resumed', '{{CYCLES}}')">{{CYCLES}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="ungrouped grouped" title="Product description(s)" onclick="pmInsertPlaceholder('resumed', '{{DESCRIPTION}}')">{{DESCRIPTION}}</button> 
                <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Reason saved with the Delay Notification" onclick="pmInsertPlaceholder('resumed', '{{REASON}}')">{{REASON}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="ETA saved with the Delay Notification, in UTC" onclick="pmInsertPlaceholder('resumed', '{{ETA}}')">{{ETA}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Expected delay saved with the Delay Notification" onclick="pmInsertPlaceholder('resumed', '{{EXPECTED_DELAY}}')">{{EXPECTED_DELAY}}</button> <button type="button" class="btn btn-sm btn-outline-secondary py-0" data-variants="grouped" title="Note saved with the Delay Notification" onclick="pmInsertPlaceholder('resumed', '{{NOTE}}')">{{NOTE}}</button>
                <span class="small text-muted ms-1">Green = used</span>
            </div>
            <textarea class="form-control" id="resumedMessage" name="resumedMessage" rows="8"
                      style="font-family:monospace; font-size:0.85rem;"><c:out value="${resumedMessage}" /></textarea>
            <textarea class="form-control" id="resumedMessageGrouped" name="resumedMessageGrouped" rows="8"
                      style="font-family:monospace; font-size:0.85rem; display:none;"><c:out value="${resumedMessageGrouped}" /></textarea>
            <textarea id="defaultResumedMessage" class="d-none"><c:out value="${defaultResumedMessage}" /></textarea>
            <textarea id="defaultResumedMessageGrouped" class="d-none"><c:out value="${defaultResumedMessageGrouped}" /></textarea>
            <div class="alert alert-warning py-2 small mt-2 mb-0 d-none" id="resumedUnknownWarning" role="status">
                <i class="bi bi-exclamation-triangle-fill me-1"></i><strong>Unknown placeholder(s):</strong>
                <code id="resumedUnknownTokens"></code>
                <div>Unrecognized placeholders are not replaced and will remain in the message. Check the spelling or use a supported placeholder from the buttons above.</div>
            </div>
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

    <div class="card shadow-sm mb-4" id="addressCard">
        <div class="card-header fw-semibold"><i class="bi bi-at me-2"></i>Notification Addresses</div>
        <div class="card-body">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label fw-semibold" for="noticeToAddress">From / To address</label>
                    <input type="email" class="form-control" id="noticeToAddress" name="noticeToAddress"
                           value="<c:out value="${noticeToAddress}" />" placeholder="operators@ecmwf.int">
                    <div class="form-text">Sender and visible recipient of the delay/resumed emails. The real recipients are placed in Bcc.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label fw-semibold" for="noticeBccAddress">Always in Bcc</label>
                    <input type="email" class="form-control" id="noticeBccAddress" name="noticeBccAddress"
                           value="<c:out value="${noticeBccAddress}" />" placeholder="op_delay_ecmwf@lists.ecmwf.int">
                    <div class="form-text">Distribution list always placed in Bcc (e.g. the operational delay list).</div>
                </div>
            </div>
            <div class="form-text text-muted mt-2">Used by the <em>Products Delay</em>/<em>Products Resumed</em> buttons and by
            the Delay Notification page. Leave empty to restore the default.</div>
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
function pmUpdateAffectedHint() {
  var g = document.getElementById('delayMessageGrouped'), hint = document.getElementById('affectedHint');
  if (!g || !hint) return;
  var visible = g.style.display !== 'none' && g.value.indexOf('{{AFFECTED}}') < 0;
  hint.style.display = visible ? '' : 'none';
}
function pmUpdatePlaceholderUsage(which) {
  var grouped = document.getElementById(which + 'MessageGrouped');
  var textarea = grouped && grouped.style.display !== 'none'
    ? grouped : document.getElementById(which + 'Message');
  var variant = textarea === grouped ? 'grouped' : 'ungrouped';
  var bar = document.getElementById(which + 'Placeholders');
  if (!textarea || !bar) return;
  var supported = ['{{PRODUCT}}', '{{PRODUCTS}}', '{{CYCLE}}', '{{CYCLES}}', '{{DESCRIPTION}}',
    '{{AFFECTED}}', '{{REASON}}', '{{ETA}}', '{{EXPECTED_DELAY}}', '{{NOTE}}'];
  var found = textarea.value.match(/\{\{[^{}]+\}\}/g) || [];
  var unknown = found.filter(function(token, index) {
    return supported.indexOf(token) < 0 && found.indexOf(token) === index;
  });
  var warning = document.getElementById(which + 'UnknownWarning');
  var tokens = document.getElementById(which + 'UnknownTokens');
  if (warning && tokens) {
    tokens.textContent = unknown.join(', ');
    warning.classList.toggle('d-none', unknown.length === 0);
  }
  bar.querySelectorAll('button[data-variants]').forEach(function(button) {
    if (button.getAttribute('data-variants').split(' ').indexOf(variant) < 0) return;
    var used = textarea.value.indexOf(button.textContent.trim()) >= 0;
    button.classList.toggle('btn-success', used);
    button.classList.toggle('btn-outline-secondary', !used);
    button.setAttribute('aria-pressed', used ? 'true' : 'false');
  });
}
// Inserts a placeholder at the cursor (replacing any selection) of the message currently shown in the card.
// A placeholder may be inserted several times: every occurrence is replaced when the message is used.
function pmInsertPlaceholder(which, placeholder) {
  var ta = document.getElementById(which + 'MessageGrouped');
  if (!ta || ta.style.display === 'none') ta = document.getElementById(which + 'Message');
  var start = ta.selectionStart == null ? ta.value.length : ta.selectionStart;
  var end = ta.selectionEnd == null ? start : ta.selectionEnd;
  ta.value = ta.value.substring(0, start) + placeholder + ta.value.substring(end);
  ta.focus();
  ta.setSelectionRange(start + placeholder.length, start + placeholder.length);
  ta.dispatchEvent(new Event('input', {bubbles: true}));
  pmUpdateAffectedHint();
}
function pmResetCurrentDefault(which) {
  var grouped = document.getElementById(which + 'MessageGrouped');
  var textarea = grouped && grouped.style.display !== 'none'
    ? grouped : document.getElementById(which + 'Message');
  var variant = textarea === grouped ? 'grouped' : 'ungrouped';
  var defaults = document.getElementById('default' + which.charAt(0).toUpperCase() + which.slice(1)
    + (variant === 'grouped' ? 'MessageGrouped' : 'Message'));
  var badge = document.getElementById(which + 'CustomizedBadge');
  if (!textarea || !defaults || !badge) return;
  textarea.value = defaults.value;
  badge.setAttribute('data-' + variant + '-customized', 'false');
  textarea.dispatchEvent(new Event('input', {bubbles: true}));
  pmSetVariant(which, variant);
}
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
      (grouped ? 'Grouped &mdash; e.g. <code>{{PRODUCTS}}</code>, <code>{{CYCLES}}</code>'
        : 'Ungrouped &mdash; e.g. <code>{{PRODUCT}}</code>, <code>{{CYCLE}}</code>') +
      ')</span>';
  }
  var bar = document.getElementById(which + 'Placeholders');
  if (bar) {
    bar.querySelectorAll('button[data-variants]').forEach(function(b) {
      b.style.display = b.getAttribute('data-variants').split(' ').indexOf(variant) >= 0 ? '' : 'none';
    });
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
  pmUpdatePlaceholderUsage(which);
  pmUpdateAffectedHint();
}

document.addEventListener('DOMContentLoaded', function() {
  var gm = document.getElementById('delayMessageGrouped');
  if (gm) gm.addEventListener('input', pmUpdateAffectedHint);
  ['delay', 'resumed'].forEach(function(which) {
    ['Message', 'MessageGrouped'].forEach(function(suffix) {
      var textarea = document.getElementById(which + suffix);
      if (textarea) textarea.addEventListener('input', function() { pmUpdatePlaceholderUsage(which); });
    });
    var variant = 'ungrouped';
    try { variant = localStorage.getItem('pm' + which + 'Variant') || 'ungrouped'; } catch (e) {}
    pmSetVariant(which, variant);
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/pds/admin/productmessages/guide.jsp"/>
