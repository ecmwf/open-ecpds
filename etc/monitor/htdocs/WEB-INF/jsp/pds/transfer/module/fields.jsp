<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute id="actionFormName" name="action.form.name"
	classname="java.lang.String" />
<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-puzzle text-primary"></i>
    <span class="fw-semibold">
      <c:choose>
        <c:when test="${isInsert == 'true'}">New Transfer Module</c:when>
        <c:otherwise>Edit Transfer Module</c:otherwise>
      </c:choose>
    </span>
  </div>
  <div class="card-body pb-2">
    <div class="row g-2">

      <%-- Name --%>
      <div class="col-sm-4">
        <label class="form-label mb-1">Name</label>
        <logic:match name="isInsert" value="true">
          <div class="d-flex align-items-center gap-2">
            <input id="name" name="name" type="text"
              class="form-control form-control-sm"
              pattern="[a-zA-Z0-9]+"
              title="Letters and digits only (e.g. FtpModule)"
              required
              oninput="validatePatternInput(this, 'name-feedback'); _checkNameExists(this.value)">
            <span id="name-feedback"></span>
          </div>
          <div id="name-exists-msg" style="display:none" class="small mt-1"></div>
        </logic:match>
        <logic:notMatch name="isInsert" value="true">
          <div class="form-control-plaintext form-control-sm fw-medium">
            <c:out value="${requestScope[actionFormName].name}" />
            <html:hidden property="name" />
          </div>
        </logic:notMatch>
      </div>

      <%-- Class Name --%>
      <div class="col-sm-8">
        <label class="form-label mb-1">Class Name</label>
        <div class="d-flex align-items-center gap-2">
          <input id="classe" name="classe" type="text"
            class="form-control form-control-sm"
            pattern="[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)*"
            title="Fully qualified class name (e.g. com.example.FtpModule)"
            value="<c:out value='${requestScope[actionFormName].classe}' />"
            oninput="validatePatternInput(this, 'classe-feedback')">
          <span id="classe-feedback"></span>
        </div>
      </div>

      <%-- Class Path --%>
      <div class="col-sm-8">
        <label class="form-label mb-1">Class Path</label>
        <html:text property="archive" styleClass="form-control form-control-sm" />
      </div>

      <%-- Enabled --%>
      <div class="col-sm-4">
        <label class="form-label mb-1 d-block">&nbsp;</label>
        <div class="form-check form-switch pt-1">
          <html:checkbox property="active" styleClass="form-check-input" styleId="active" />
          <label class="form-check-label small" for="active">Enabled</label>
        </div>
      </div>

    </div>
  </div>
</div>

<script>
var _checkNameTimer = null;
function _checkNameExists(value) {
  clearTimeout(_checkNameTimer);
  var $msg = $('#name-exists-msg');
  var $submit = $('button[type="submit"]').first();
  $msg.hide();
  $submit.prop('disabled', false);
  if (!value || value.length < 1) return;
  _checkNameTimer = setTimeout(function() {
    $.getJSON('/do/transfer/module?json=checkId&id=' + encodeURIComponent(value), function(data) {
      if (data.exists) {
        $msg.html('<i class="bi bi-x-circle-fill text-danger me-1"></i><span class="text-danger">Name <strong>' + $('<span>').text(value).html() + '</strong> is already taken.</span>').show();
        $submit.prop('disabled', true);
      } else {
        $msg.html('<i class="bi bi-check-circle-fill text-success me-1"></i><span class="text-success">Available.</span>').show();
        $submit.prop('disabled', false);
      }
    });
  }, 400);
}
</script>
