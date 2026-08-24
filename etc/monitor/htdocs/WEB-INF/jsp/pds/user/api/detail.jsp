<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>

<script>
function validate(path, message) {
    confirmationDialog({
        title: "Please Confirm",
        message: message,
        onConfirm: function () { window.location = path; },
        onCancel: function () {}
    });
}

function resetSecret() {
    confirmationDialog({
        title: '<i class="bi bi-arrow-repeat me-2"></i>Reset Secret?',
        message: 'This will invalidate the current secret for client <strong>${apiClient.id}</strong> and generate a new one. Any system using the old secret will stop working immediately.',
        confirmText: 'Reset',
        onConfirm: function () { document.getElementById('resetSecretForm').submit(); }
    });
}

function copySecret(btn) {
    var val = document.getElementById('_generatedSecret').value;
    navigator.clipboard.writeText(val).then(function () {
        btn.innerHTML = '<i class="bi bi-check-lg"></i> Copied!';
    });
}
</script>

<c:if test="${not empty sessionScope.pendingApiSecret}">
<%-- Read once from session, then clear so refresh doesn't re-show the modal --%>
<c:set var="generatedSecret"   value="${sessionScope.pendingApiSecret}" />
<c:set var="generatedClientId" value="${sessionScope.pendingApiClientId}" />
<% session.removeAttribute("pendingApiSecret"); session.removeAttribute("pendingApiClientId"); %>
<script>
$(document).ready(function () {
    confirmationDialog({
        title: '<i class="bi bi-key-fill me-2"></i>New API Secret — copy now!',
        message:
            '<p class="small mb-2">The secret is shown <strong>once only</strong> and cannot be recovered. Copy it before closing this dialog.</p>' +
            '<dl class="row small mb-2">' +
            '  <dt class="col-sm-4">Client ID</dt><dd class="col-sm-8"><code><c:out value="${generatedClientId}"/></code></dd>' +
            '</dl>' +
            '<label class="form-label small fw-semibold">New Secret</label>' +
            '<div class="input-group input-group-sm">' +
            '  <input type="text" class="form-control font-monospace" id="_generatedSecret" value="<c:out value="${generatedSecret}"/>" readonly>' +
            '  <button class="btn btn-outline-secondary" type="button" onclick="copySecret(this)">' +
            '    <i class="bi bi-clipboard"></i> Copy' +
            '  </button>' +
            '</div>',
        confirmText: 'Done',
        cancelText: null,
        showLoading: false,
        onConfirm: function () {},
        onCancel: function () {}
    });
});
</script>
</c:if>

<%-- Top info banner --%>
<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
  <i class="bi bi-key text-primary flex-shrink-0"></i>
  <span>API Client: <strong><c:out value="${apiClient.id}"/></strong></span>
  <div class="d-flex gap-1 ms-auto flex-shrink-0">
    <a href="<bean:message key="apiclient.basepath"/>"
       class="btn btn-sm btn-outline-secondary" title="View all API clients"><i class="bi bi-arrow-left"></i></a>
    <span class="vr align-self-center mx-1"></span>
    <a href="<bean:message key="apiclient.basepath"/>/edit/insert_form"
       class="btn btn-sm btn-outline-success" title="Create new API client"><i class="bi bi-plus-circle"></i></a>
    <a href="<bean:message key="apiclient.basepath"/>/${apiClient.id}/edit/update_form"
       class="btn btn-sm btn-outline-primary" title="Edit this API client"><i class="bi bi-pencil"></i></a>
    <a href="<bean:message key="apiclient.basepath"/>/${apiClient.id}/edit/delete"
       class="btn btn-sm btn-outline-danger" title="Delete this API client"
       onclick="validate(this.href, 'Delete API client &lt;strong&gt;${apiClient.id}&lt;/strong&gt;? This cannot be undone.'); return false;"><i class="bi bi-trash"></i></a>
  </div>
</div>

<%-- Card: Identity --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-tag text-primary"></i>
    <span class="fw-semibold">Identity</span>
  </div>
  <div class="card-body py-0">
    <div class="field-grid">
      <div class="field-row"><div class="field-label">Client ID</div><div class="field-value"><span class="val-code"><c:out value="${apiClient.id}"/></span></div></div>
      <div class="field-row"><div class="field-label">Comment</div><div class="field-value"><c:choose><c:when test="${not empty apiClient.comment}">${apiClient.comment}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise></c:choose></div></div>
      <div class="field-row"><div class="field-label">Country</div><div class="field-value">
        <c:choose>
          <c:when test="${not empty apiClient.countryIso}">
            <span class="d-inline-flex align-items-center gap-1">
              <c:choose>
                <c:when test="${apiClient.countryIso == 'ex'}">
                  <i class="bi bi-globe" style="font-size:1.1em"></i>
                </c:when>
                <c:otherwise>
                  <span class="fi fi-${fn:toLowerCase(apiClient.countryIso)}" style="font-size:1.1em;border-radius:2px"></span>
                </c:otherwise>
              </c:choose>
              <c:catch><c:out value="${apiClient.country.name}"/></c:catch>
            </span>
          </c:when>
          <c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">None</span></c:otherwise>
        </c:choose>
      </div></div>
    </div>
  </div>
</div>

<%-- Card: Status --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-activity text-primary"></i>
    <span class="fw-semibold">Status</span>
  </div>
  <div class="card-body py-0">
    <div class="field-grid">
      <div class="field-row"><div class="field-label">Active</div><div class="field-value">
        <c:choose>
          <c:when test="${apiClient.active}"><span class="badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis"><i class="bi bi-check-circle-fill me-1"></i>Yes</span></c:when>
          <c:otherwise><span class="badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis"><i class="bi bi-x-circle-fill me-1"></i>No</span></c:otherwise>
        </c:choose>
      </div></div>
      <div class="field-row"><div class="field-label">Created</div><div class="field-value"><c:choose><c:when test="${not empty apiClient.created}">${apiClient.created}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">Unknown</span></c:otherwise></c:choose></div></div>
      <div class="field-row"><div class="field-label">Last Used</div><div class="field-value"><c:choose><c:when test="${not empty apiClient.lastUsed}">${apiClient.lastUsed}</c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">Never</span></c:otherwise></c:choose></div></div>
      <div class="field-row"><div class="field-label">Last IP</div><div class="field-value"><c:choose><c:when test="${not empty apiClient.lastUsedHost}"><span class="val-code"><c:out value="${apiClient.lastUsedHost}"/></span></c:when><c:otherwise><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">Unknown</span></c:otherwise></c:choose></div></div>
    </div>
  </div>
</div>

<%-- Card: Secret Management --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-shield-lock text-primary"></i>
    <span class="fw-semibold">Secret</span>
  </div>
  <div class="card-body">
    <p class="text-muted small mb-2">The API secret is never displayed after creation. Use the button below to invalidate the current secret and generate a new one.</p>
    <form id="resetSecretForm" action="<bean:message key="apiclient.basepath"/>/${apiClient.id}/edit/reset_secret" method="post" class="d-inline">
      <button type="button" class="btn btn-warning btn-sm" onclick="resetSecret()">
        <i class="bi bi-arrow-repeat me-1"></i>Reset Secret
      </button>
    </form>
  </div>
</div>

<%-- Card: Service Permissions --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-shield-check text-primary"></i>
    <span class="fw-semibold">Service Permissions</span>
    <button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
            data-bs-toggle="collapse" data-bs-target="#apiPermInfo" title="About permissions">
      <i class="bi bi-info-circle"></i>
    </button>
    <a href="#" class="btn btn-sm btn-outline-info flex-shrink-0"
       onclick="var el=document.getElementById('permissionGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;"
       title="Open Permission Guide"><i class="bi bi-book me-1"></i><span class="d-none d-sm-inline">Permission </span>Guide</a>
  </div>
  <div class="collapse" id="apiPermInfo">
    <div class="card-body py-2 px-3 border-bottom"
         style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
      <strong class="d-block mb-1">Service Access Control</strong>
      <p class="mb-1">Each API client must have at least one permission that matches the requested service name.
        Permissions are regex patterns matched against the full service name.</p>
      <ul class="mb-0 ps-3">
        <li>Use the <strong>checkboxes</strong> to grant or revoke access to individual known services. These store an exact service name as the permission pattern.</li>
        <li>Use <strong>Custom pattern</strong> to enter a regex (e.g. <code>.*</code> for full access, <code>datafile(.*)</code> for all datafile services). A single regex can cover many services at once.</li>
        <li>If a custom regex <em>already covers</em> a known service, its checkbox is shown as <strong>checked and greyed out</strong> (read-only). To uncheck it you must remove the covering regex first.</li>
        <li>Custom regex takes precedence over checkboxes — access is granted if <em>any</em> permission matches.</li>
      </ul>
    </div>
  </div>
  <div class="card-body">

    <%-- Warning: no permissions configured --%>
    <c:if test="${empty permissions}">
    <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-3 small">
      <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
      <div>
        <strong>No permissions configured.</strong>
        This API client cannot access any service until at least one permission is added below.
      </div>
    </div>
    </c:if>

    <%-- Pass server-side data to JS safely via hidden elements (avoids JS-string escaping issues) --%>
    <span id="_apiClientId" style="display:none"><c:out value="${apiClient.id}"/></span>
    <c:forEach var="perm" items="${permissions}">
      <span class="_apiPermData" style="display:none"><c:out value="${perm.pattern}"/></span>
    </c:forEach>

    <script>
    var _apiClientId = document.getElementById('_apiClientId').textContent;
    var _apiPermsOrig = Array.from(document.querySelectorAll('._apiPermData')).map(function(el){ return el.textContent; });

    /* Known services (same list baked into checkboxes below) */
    var _apiKnownServices = [
      'datafilePut','datafileSize','datafileDel',
      'destinationList','destinationCountryList','destinationBackupList','putDestinationBackup',
      'getDestinationMetaFields','getDestinationMetaValuesByDestination','setDestinationMetaValues',
      'incomingUserAdd','incomingUserAdd2','incomingUserList','incomingUserDel',
      'incomingAssociationAdd','incomingAssociationDel','incomingAssociationList',
      'incomingCategoryAdd','updateHostOption'
    ];

    function _apiPermSelectAll(checked) {
      document.querySelectorAll('.api-perm-chk:not(:disabled)').forEach(function(c){ c.checked = checked; });
    }
    function _apiPermSelectGroup(group, checked) {
      document.querySelectorAll('.api-perm-chk[data-group="'+group+'"]:not(:disabled)').forEach(function(c){ c.checked = checked; });
    }

    /* Test if a service name is covered by a given pattern (Java String.matches semantics = full-string regex) */
    function _apiPermMatchedBy(service, patterns) {
      for (var i = 0; i < patterns.length; i++) {
        try {
          if (new RegExp('^(?:' + patterns[i] + ')$').test(service)) return patterns[i];
        } catch(e) { /* invalid regex – skip */ }
      }
      return null;
    }

    function _apiPermSave() {
      var btn = document.getElementById('apiPermSaveBtn');
      var status = document.getElementById('apiPermStatus');
      btn.disabled = true;
      status.className = 'ms-2 small text-muted';
      status.textContent = 'Saving\u2026';

      var toAdd = [], toDel = [];
      document.querySelectorAll('.api-perm-chk:not(:disabled)').forEach(function(chk) {
        var svc = chk.value, had = _apiPermsOrig.indexOf(svc) !== -1;
        if (chk.checked && !had) toAdd.push(svc);
        if (!chk.checked && had) toDel.push(svc);
      });

      if (toAdd.length === 0 && toDel.length === 0) {
        status.textContent = 'No changes.';
        btn.disabled = false;
        return;
      }

      var base = '/do/user/api/' + _apiClientId;
      var ops = [];
      toAdd.forEach(function(svc) {
        ops.push(fetch(base + '/edit/add_permission', {
          method: 'POST',
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          body: 'pattern=' + encodeURIComponent(svc),
          redirect: 'follow'
        }));
      });
      toDel.forEach(function(svc) {
        ops.push(fetch(base + '/edit/delete_permission/' + encodeURIComponent(svc), { redirect: 'follow' }));
      });

      Promise.all(ops).then(function() {
        window.location.reload();
      }).catch(function(e) {
        status.className = 'ms-2 small text-danger';
        status.textContent = 'Error: ' + e.message;
        btn.disabled = false;
      });
    }

    $(function() {
      /*
       * For each known service checkbox:
       *   1. Exact match in permissions → checked + enabled (user controls it)
       *   2. Covered by a regex pattern → checked + disabled + tooltip (read-only, regex wins)
       *   3. No match → unchecked + enabled
       *
       * Non-exact patterns (custom regex) are filtered out for exact-match checks.
       */
      var customPatterns = _apiPermsOrig.filter(function(p) { return _apiKnownServices.indexOf(p) === -1; });

      document.querySelectorAll('.api-perm-chk').forEach(function(chk) {
        var svc = chk.value;
        if (_apiPermsOrig.indexOf(svc) !== -1) {
          /* Exact permission — checked and editable */
          chk.checked = true;
        } else {
          var coveredBy = _apiPermMatchedBy(svc, customPatterns);
          if (coveredBy) {
            /* Covered by a regex — checked but read-only */
            chk.checked = true;
            chk.disabled = true;
            chk.title = 'Covered by regex: ' + coveredBy;
            var lbl = document.querySelector('label[for="' + chk.id + '"]');
            if (lbl) {
              lbl.style.opacity = '0.6';
              lbl.title = 'Access granted via regex pattern: ' + coveredBy;
            }
          } else {
            chk.checked = false;
          }
        }
      });
    });
    </script>

    <%-- Grouped service checkboxes --%>
    <div class="mb-3 p-3 rounded border" style="background:var(--bs-tertiary-bg,#f8f9fa)">
      <div class="d-flex align-items-center gap-2 mb-3">
        <span class="small fw-semibold">Quick select:</span>
        <button type="button" class="btn btn-sm btn-outline-success py-0" onclick="_apiPermSelectAll(true)">
          <i class="bi bi-check-all me-1"></i>Grant all
        </button>
        <button type="button" class="btn btn-sm btn-outline-secondary py-0" onclick="_apiPermSelectAll(false)">
          <i class="bi bi-x-lg me-1"></i>Revoke all
        </button>
      </div>

      <div class="row g-3">
        <%-- Datafile group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-file-earmark-binary text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Datafile</span>
            <a href="#" class="ms-2 small text-muted text-decoration-none" onclick="_apiPermSelectGroup('datafile',true);return false;">all</a>
            <span class="text-muted small">/</span>
            <a href="#" class="small text-muted text-decoration-none" onclick="_apiPermSelectGroup('datafile',false);return false;">none</a>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-datafilePut" data-group="datafile" value="datafilePut"><label class="form-check-label small font-monospace" for="apc-datafilePut">datafilePut</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-datafileSize" data-group="datafile" value="datafileSize"><label class="form-check-label small font-monospace" for="apc-datafileSize">datafileSize</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-datafileDel" data-group="datafile" value="datafileDel"><label class="form-check-label small font-monospace" for="apc-datafileDel">datafileDel</label></div>
          </div>
        </div>

        <%-- Destination group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-geo-alt text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Destination</span>
            <a href="#" class="ms-2 small text-muted text-decoration-none" onclick="_apiPermSelectGroup('destination',true);return false;">all</a>
            <span class="text-muted small">/</span>
            <a href="#" class="small text-muted text-decoration-none" onclick="_apiPermSelectGroup('destination',false);return false;">none</a>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-destinationList" data-group="destination" value="destinationList"><label class="form-check-label small font-monospace" for="apc-destinationList">destinationList</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-destinationCountryList" data-group="destination" value="destinationCountryList"><label class="form-check-label small font-monospace" for="apc-destinationCountryList">destinationCountryList</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-destinationBackupList" data-group="destination" value="destinationBackupList"><label class="form-check-label small font-monospace" for="apc-destinationBackupList">destinationBackupList</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-putDestinationBackup" data-group="destination" value="putDestinationBackup"><label class="form-check-label small font-monospace" for="apc-putDestinationBackup">putDestinationBackup</label></div>
          </div>
        </div>

        <%-- Metadata group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-tags text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Metadata</span>
            <a href="#" class="ms-2 small text-muted text-decoration-none" onclick="_apiPermSelectGroup('metadata',true);return false;">all</a>
            <span class="text-muted small">/</span>
            <a href="#" class="small text-muted text-decoration-none" onclick="_apiPermSelectGroup('metadata',false);return false;">none</a>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-getDestinationMetaFields" data-group="metadata" value="getDestinationMetaFields"><label class="form-check-label small font-monospace" for="apc-getDestinationMetaFields">getDestinationMetaFields</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-getDestinationMetaValuesByDestination" data-group="metadata" value="getDestinationMetaValuesByDestination"><label class="form-check-label small font-monospace" for="apc-getDestinationMetaValuesByDestination">getDestinationMetaValuesByDestination</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-setDestinationMetaValues" data-group="metadata" value="setDestinationMetaValues"><label class="form-check-label small font-monospace" for="apc-setDestinationMetaValues">setDestinationMetaValues</label></div>
          </div>
        </div>

        <%-- Incoming Users group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-person-plus text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Incoming Users</span>
            <a href="#" class="ms-2 small text-muted text-decoration-none" onclick="_apiPermSelectGroup('incominguser',true);return false;">all</a>
            <span class="text-muted small">/</span>
            <a href="#" class="small text-muted text-decoration-none" onclick="_apiPermSelectGroup('incominguser',false);return false;">none</a>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingUserAdd" data-group="incominguser" value="incomingUserAdd"><label class="form-check-label small font-monospace" for="apc-incomingUserAdd">incomingUserAdd</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingUserAdd2" data-group="incominguser" value="incomingUserAdd2"><label class="form-check-label small font-monospace" for="apc-incomingUserAdd2">incomingUserAdd2</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingUserList" data-group="incominguser" value="incomingUserList"><label class="form-check-label small font-monospace" for="apc-incomingUserList">incomingUserList</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingUserDel" data-group="incominguser" value="incomingUserDel"><label class="form-check-label small font-monospace" for="apc-incomingUserDel">incomingUserDel</label></div>
          </div>
        </div>

        <%-- Incoming Associations group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-diagram-3 text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Incoming Associations</span>
            <a href="#" class="ms-2 small text-muted text-decoration-none" onclick="_apiPermSelectGroup('incomingassoc',true);return false;">all</a>
            <span class="text-muted small">/</span>
            <a href="#" class="small text-muted text-decoration-none" onclick="_apiPermSelectGroup('incomingassoc',false);return false;">none</a>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingAssociationAdd" data-group="incomingassoc" value="incomingAssociationAdd"><label class="form-check-label small font-monospace" for="apc-incomingAssociationAdd">incomingAssociationAdd</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingAssociationDel" data-group="incomingassoc" value="incomingAssociationDel"><label class="form-check-label small font-monospace" for="apc-incomingAssociationDel">incomingAssociationDel</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingAssociationList" data-group="incomingassoc" value="incomingAssociationList"><label class="form-check-label small font-monospace" for="apc-incomingAssociationList">incomingAssociationList</label></div>
          </div>
        </div>

        <%-- Other group --%>
        <div class="col-md-6 col-xl-4">
          <div class="d-flex align-items-center gap-1 mb-1">
            <i class="bi bi-gear text-primary" style="font-size:0.8rem"></i>
            <span class="small fw-semibold">Other</span>
          </div>
          <div class="d-flex flex-column gap-1 ps-1">
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-incomingCategoryAdd" data-group="other" value="incomingCategoryAdd"><label class="form-check-label small font-monospace" for="apc-incomingCategoryAdd">incomingCategoryAdd</label></div>
            <div class="form-check mb-0"><input class="form-check-input api-perm-chk" type="checkbox" id="apc-updateHostOption" data-group="other" value="updateHostOption"><label class="form-check-label small font-monospace" for="apc-updateHostOption">updateHostOption</label></div>
          </div>
        </div>
      </div>

      <div class="mt-3 d-flex align-items-center">
        <button type="button" class="btn btn-primary btn-sm" id="apiPermSaveBtn" onclick="_apiPermSave()">
          <i class="bi bi-check-lg me-1"></i>Apply Changes
        </button>
        <span id="apiPermStatus" class="ms-2 small text-muted"></span>
      </div>
    </div>

    <%-- Custom / non-standard permissions (regex patterns not in known list) --%>
    <div>
      <button class="btn btn-link btn-sm p-0 text-decoration-none text-muted" type="button"
              data-bs-toggle="collapse" data-bs-target="#apiCustomPerms">
        <i class="bi bi-chevron-right me-1" id="apiCustomPermsChevron"></i>Custom regex patterns
        <span id="apiCustomPermCount" class="badge bg-secondary ms-1" style="font-size:0.7rem"></span>
      </button>
      <div class="collapse mt-2" id="apiCustomPerms">
        <div id="apiCustomPermTable">
          <%-- Populated by JS --%>
        </div>
        <form action="<bean:message key="apiclient.basepath"/>/${apiClient.id}/edit/add_permission" method="post" class="d-flex gap-2 mt-2">
          <input type="text" name="pattern" class="form-control form-control-sm font-monospace"
                 placeholder="e.g. .* or datafile.*" required />
          <button type="submit" class="btn btn-outline-primary btn-sm text-nowrap">
            <i class="bi bi-plus me-1"></i>Add
          </button>
        </form>
      </div>
    </div>

    <script>
    /* Delete a custom regex pattern via fetch (avoids URL-path special-char issues) */
    function _apiPermDeleteCustom(pattern) {
      var esc = $('<span>').text(pattern).html();
      confirmationDialog({
        title: 'Please Confirm',
        message: 'Remove custom pattern <strong>' + esc + '</strong> from client <strong>' + $('<span>').text(_apiClientId).html() + '</strong>?',
        onConfirm: function() {
          /* POST pattern in the request body to avoid '|' and other special chars
             being corrupted when passed as a URL path segment */
          fetch('/do/user/api/' + encodeURIComponent(_apiClientId) + '/edit/delete_permission/_', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'pattern=' + encodeURIComponent(pattern),
            redirect: 'follow'
          }).then(function() {
            window.location.reload();
          }).catch(function(e) {
            alert('Error deleting pattern: ' + e.message);
          });
        }
      });
    }

    $(function() {
      /* Build custom patterns table (patterns not in the known-service list) */
      var custom = _apiPermsOrig.filter(function(p) { return _apiKnownServices.indexOf(p) === -1; });
      var $count = $('#apiCustomPermCount');
      if (custom.length > 0) {
        $count.text(custom.length);
        var rows = custom.map(function(p) {
          var esc = $('<span>').text(p).html();
          return '<tr><td><code>' + esc + '</code></td><td class="text-center">'
            + '<button type="button" class="btn btn-sm btn-outline-danger"'
            + " onclick='_apiPermDeleteCustom(" + JSON.stringify(p) + ")'"
            + '><i class="bi bi-trash"></i></button></td></tr>';
        }).join('');
        $('#apiCustomPermTable').html(
          '<table class="table table-sm table-hover align-middle mb-2">'
          + '<thead class="table-light"><tr><th>Pattern</th><th class="text-center" style="width:80px">Action</th></tr></thead>'
          + '<tbody>' + rows + '</tbody></table>'
        );
      } else {
        $count.hide();
      }

      /* Chevron toggle */
      $('#apiCustomPerms').on('show.bs.collapse', function() {
        $('#apiCustomPermsChevron').removeClass('bi-chevron-right').addClass('bi-chevron-down');
      }).on('hide.bs.collapse', function() {
        $('#apiCustomPermsChevron').removeClass('bi-chevron-down').addClass('bi-chevron-right');
      });
    });
    </script>

  </div>
</div>

<div class="mt-3">
  <a href="<bean:message key="apievent.basepath"/>?clientId=${apiClient.id}" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-journal-code me-1"></i>View Events
  </a>
</div>

<%-- Permission Guide offcanvas --%>
<jsp:include page="/WEB-INF/jsp/pds/user/api/permission_guide.jsp"/>
