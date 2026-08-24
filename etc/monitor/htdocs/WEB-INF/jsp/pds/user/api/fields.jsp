<%@ page session="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<c:set var="isUpdate" value="${not empty updateMode}" />
<c:set var="formAction" value="${isUpdate ? '/user/api/'.concat(apiClientActionForm.id).concat('/edit/update') : '/user/api/edit/insert'}" />
<c:set var="cancelUrl"  value="${isUpdate ? '/do/user/api/'.concat(apiClientActionForm.id) : '/do/user/api'}" />

<%-- Info banner --%>
<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
  <i class="bi bi-key text-primary flex-shrink-0"></i>
  <span>
    <c:choose>
      <c:when test="${isUpdate}">Edit API client configuration for <strong><c:out value="${apiClientActionForm.id}"/></strong>.</c:when>
      <c:otherwise>Register a new REST API client.</c:otherwise>
    </c:choose>
  </span>
</div>

<html:form action="${formAction}" method="post">
<html:errors/>

<%-- Card: Identity --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-tag text-primary"></i>
    <span class="fw-semibold">Identity</span>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-sm-6">
        <label class="form-label mb-1">Client ID</label>
        <c:choose>
          <c:when test="${isUpdate}">
            <div class="form-control form-control-sm bg-body-secondary" style="font-family:monospace"><c:out value="${apiClientActionForm.id}"/></div>
            <html:hidden property="id" />
          </c:when>
          <c:otherwise>
            <input type="text" name="id" class="form-control form-control-sm" maxlength="64"
                   style="font-family:monospace"
                   pattern="[\w\-]{1,64}"
                   title="1–64 characters: letters, digits, hyphens, underscores"
                   data-char-filter="A-Za-z0-9_\\-"
                   oninput="_validateClientId(this)"
                   onblur="this.value=this.value.trim(); _validateClientId(this)"
                   value="${apiClientActionForm.id}" />
            <span id="cid-feedback"></span>
            <div id="cid-exists-msg" style="display:none" class="small mt-1"></div>
            <div class="form-text">Unique identifier (1–64 characters: letters, digits, <code>-</code>, <code>_</code>). <span class="text-danger">*</span></div>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="col-sm-6">
        <label class="form-label mb-1">Comment</label>
        <html:text property="comment" styleClass="form-control form-control-sm" maxlength="512" />
      </div>
    </div>
  </div>
</div>

<%-- Card: Options --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-toggles text-primary"></i>
    <span class="fw-semibold">Options</span>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-sm-4">
        <div class="form-check form-switch">
          <html:checkbox property="active" styleClass="form-check-input" styleId="active" value="true" />
          <input type="hidden" name="active" value="false" />
          <label class="form-check-label" for="active">Active</label>
        </div>
      </div>
      <div class="col-sm-8">
        <label class="form-label mb-1">Country
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="top"
             data-bs-content="Country associated with this API client (used to display the corresponding flag)."
             tabindex="0"></i>
        </label>
        <c:set var="countries" value="${apiClientActionForm.countryOptions}" />
        <div class="d-flex align-items-center gap-2" style="min-width:0">
          <html:select property="countryIso" styleId="apiCountryIso" styleClass="form-select form-select-sm" style="flex:1 1 0;min-width:0">
            <html:option value="">— None —</html:option>
            <html:options collection="countries" property="iso" labelProperty="name" />
          </html:select>
        </div>
      </div>
    </div>
  </div>
</div>

<c:if test="${not isUpdate}">
<div class="d-flex align-items-start gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd; font-size:0.9rem;">
  <i class="bi bi-info-circle text-primary flex-shrink-0 mt-1"></i>
  <span>A random secret will be generated on creation. It will be displayed <strong>once only</strong> — copy it immediately.</span>
</div>
</c:if>

<c:if test="${not isUpdate}">
<div class="alert alert-info d-flex align-items-start gap-2 py-2 px-3 mb-3 small">
  <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
  <div>After creating the client, open its detail page to configure <strong>Service Permissions</strong>.
  Without at least one permission the client will not be able to call any service.</div>
</div>
</c:if>

<div class="mt-3">
  <button type="submit" class="btn btn-primary">
    <c:choose>
      <c:when test="${isUpdate}"><i class="bi bi-check-lg me-1"></i>Save Changes</c:when>
      <c:otherwise><i class="bi bi-plus-lg me-1"></i>Create</c:otherwise>
    </c:choose>
  </button>
  <button type="button" class="btn btn-outline-secondary ms-2"
          onclick="window.location='<c:out value="${cancelUrl}"/>'">
    <i class="bi bi-x me-1"></i>Cancel
  </button>
</div>

</html:form>

<script>
<c:if test="${not isUpdate}">
var _checkClientIdTimer = null;
function _validateClientId(input) {
    var filter = input.getAttribute('data-char-filter');
    if (filter) {
        try {
            var re = new RegExp('[^' + filter + ']', 'g');
            var before = input.value, after = before.replace(re, '');
            if (before !== after) {
                var pos = input.selectionStart || 0;
                var removedBefore = (before.substring(0, pos).match(re) || []).length;
                input.value = after;
                try { input.setSelectionRange(pos - removedBefore, pos - removedBefore); } catch(e) {}
            }
        } catch(e) {}
    }
    var value = input.value;
    var $fb = $('#cid-feedback');
    var $msg = $('#cid-exists-msg');
    var $submit = $('button[type="submit"]').first();
    if (value && !input.validity.valid) {
        $fb.html('<i class="bi bi-x-circle-fill text-danger" title="' + (input.title || 'Invalid value') + '"></i>');
        $msg.hide();
        $submit.prop('disabled', true);
        clearTimeout(_checkClientIdTimer);
        return;
    }
    $fb.html('');
    $msg.hide();
    $submit.prop('disabled', false);
    if (!value) return;
    clearTimeout(_checkClientIdTimer);
    _checkClientIdTimer = setTimeout(function () {
        $.getJSON('/do/user/api?json=checkId&id=' + encodeURIComponent(value), function (data) {
            if (data.exists) {
                $msg.html('<i class="bi bi-x-circle-fill text-danger me-1"></i><span class="text-danger">Client ID <strong>' + $('<span>').text(value).html() + '</strong> is already taken.</span>').show();
                $submit.prop('disabled', true);
            } else {
                $msg.html('<i class="bi bi-check-circle-fill text-success me-1"></i><span class="text-success">Available.</span>').show();
                $submit.prop('disabled', false);
            }
        });
    }, 400);
}
</c:if>
$(document).ready(function() {
  // Country flag next to select
  (function() {
    var VALID_ISO = new Set(['AC','AD','AE','AF','AG','AI','AL','AM','AO','AQ','AR','AS','AT','AU','AW','AX','AZ','BA','BB','BD','BE','BF','BG','BH','BI','BJ','BL','BM','BN','BO','BQ','BR','BS','BT','BV','BW','BY','BZ','CA','CC','CD','CF','CG','CH','CI','CK','CL','CM','CN','CO','CP','CR','CU','CV','CW','CX','CY','CZ','DE','DG','DJ','DK','DM','DO','DZ','EA','EE','EG','EH','ER','ES','ET','EU','EW','FI','FJ','FK','FM','FO','FR','FX','GA','GB','GD','GE','GF','GG','GH','GI','GL','GM','GN','GP','GQ','GR','GS','GT','GU','GW','GY','HK','HM','HN','HR','HT','HU','IC','ID','IE','IL','IM','IN','IO','IQ','IR','IS','IT','JE','JM','JO','JP','KE','KG','KH','KI','KM','KN','KP','KR','KW','KY','KZ','LA','LB','LC','LI','LK','LR','LS','LT','LU','LV','LY','MA','MC','MD','ME','MF','MG','MH','MK','ML','MM','MN','MO','MP','MQ','MR','MS','MT','MU','MV','MW','MX','MY','MZ','NA','NC','NE','NF','NG','NI','NL','NO','NP','NR','NU','NZ','OM','PA','PE','PF','PG','PH','PK','PL','PM','PN','PR','PS','PT','PW','PY','QA','RE','RO','RS','RU','RW','SA','SB','SC','SD','SE','SG','SH','SI','SJ','SK','SL','SM','SN','SO','SR','SS','ST','SV','SX','SY','SZ','TA','TC','TD','TF','TG','TH','TJ','TK','TL','TM','TN','TO','TP','TR','TT','TV','TW','TZ','UA','UG','UK','UM','UN','US','UY','UZ','VA','VC','VE','VG','VI','VN','VU','WF','WS','XK','YE','YT','ZA','ZM','ZR','ZW']);
    var $sel = $('#apiCountryIso');
    var $flag = $('<span class="fi" style="font-size:1.3em;vertical-align:middle;flex-shrink:0;"></span>');
    $sel.after($flag);
    function updateFlag() {
      var iso = ($sel.val() || '').toUpperCase();
      if (VALID_ISO.has(iso)) {
        $flag.attr('class', 'fi fi-' + iso.toLowerCase()).css('display', 'inline-block');
      } else if (iso === 'EX') {
        $flag.attr('class', 'bi bi-globe').css('display', 'inline-block');
      } else {
        $flag.hide();
      }
    }
    $sel.on('change', updateFlag);
    updateFlag();
  })();
});
</script>
