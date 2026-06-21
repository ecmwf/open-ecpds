<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<tiles:useAttribute id="actionFormName" name="action.form.name" classname="java.lang.String" />

<html:hidden property="id" />

<c:choose>
  <c:when test="${isInsert == 'true'}">
    <div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
         style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
      <i class="bi bi-collection text-primary flex-shrink-0"></i>
      <span>Create a new Transfer Group to organise Data Movers.</span>
    </div>
  </c:when>
  <c:otherwise>
    <div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
         style="background:rgba(13,110,253,0.06); color:var(--bs-body-color); border-left:4px solid #0d6efd;">
      <i class="bi bi-collection text-primary flex-shrink-0"></i>
      <span>Edit the Transfer Group configuration.</span>
    </div>
  </c:otherwise>
</c:choose>

<%-- Card: Identity --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-tag text-primary"></i>
    <span class="fw-semibold">Identity</span>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-sm-6">
        <label class="form-label mb-1">Name</label>
        <logic:match name="isInsert" value="true">
          <input id="name" name="name" type="text"
            class="form-control form-control-sm"
            required
            pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
            title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. group-1)"
            oninput="validatePatternInput(this, 'name-feedback'); _checkNameExists(this.value, '/do/datafile/transfergroup')">
          <div id="name-feedback" class="form-text"></div>
          <div id="name-exists-msg" style="display:none" class="small mt-1"></div>
          <div class="form-text">Letters, digits, <code>_</code> and <code>-</code> separators (e.g. <code>group-1</code>).</div>
        </logic:match>
        <logic:notMatch name="isInsert" value="true">
          <div class="form-control form-control-sm bg-body-secondary"><c:out value="${requestScope[actionFormName].name}" /></div>
          <html:hidden property="name" />
        </logic:notMatch>
      </div>
      <div class="col-sm-6">
        <label for="comment" class="form-label mb-1">Comment</label>
        <html:text property="comment" styleId="comment" styleClass="form-control form-control-sm" />
      </div>
    </div>
    <div class="mt-3">
      <div class="form-check form-switch">
        <html:checkbox property="active" styleClass="form-check-input" styleId="active" />
        <label class="form-check-label" for="active">Enabled</label>
      </div>
    </div>
  </div>
</div>

<%-- Card: Replication --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-copy text-primary"></i>
    <span class="fw-semibold">Replication</span>
  </div>
  <div class="card-body">
    <div class="mb-3">
      <div class="form-check form-switch">
        <html:checkbox property="replicate" styleClass="form-check-input" styleId="replicate" />
        <label class="form-check-label" for="replicate">Replicate data across Data Movers in this group</label>
      </div>
    </div>
    <div class="row g-3">
      <div class="col-sm-6">
        <label for="minReplicationCount" class="form-label mb-1">
          Min. Replication Count
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="right"
             data-bs-content="Integer >= 0; must not exceed the number of active Data Movers in this group"
             tabindex="0"></i>
        </label>
        <div class="d-flex align-items-center gap-2">
          <input type="number" name="minReplicationCount" id="minReplicationCount"
            min="0" step="1" class="form-control form-control-sm"
            value="<c:out value='${requestScope[actionFormName].minReplicationCount}'/>"
            oninput="validatePatternInput(this, 'minRepFeedback')">
          <span id="minRepFeedback"></span>
        </div>
      </div>
      <div class="col-sm-6">
        <label for="volumeCount" class="form-label mb-1">
          Volume Count
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="right"
             data-bs-content="Integer >= 1; number of storage volumes to distribute data across"
             tabindex="0"></i>
        </label>
        <div class="d-flex align-items-center gap-2">
          <input type="number" name="volumeCount" id="volumeCount"
            min="1" step="1" class="form-control form-control-sm"
            value="<c:out value='${requestScope[actionFormName].volumeCount}'/>"
            oninput="validatePatternInput(this, 'volCountFeedback')">
          <span id="volCountFeedback"></span>
        </div>
      </div>
    </div>
  </div>
</div>

<%-- Card: Filtering --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-funnel text-primary"></i>
    <span class="fw-semibold">Filtering</span>
  </div>
  <div class="card-body">
    <div class="mb-3">
      <div class="form-check form-switch">
        <html:checkbox property="filter" styleClass="form-check-input" styleId="filter" />
        <label class="form-check-label" for="filter">Enable compression/filtering on this Transfer Group</label>
      </div>
    </div>
    <div class="row g-3">
      <div class="col-sm-6">
        <label for="minFilteringCount" class="form-label mb-1">
          Min. Filtering Count
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="right"
             data-bs-content="Integer >= 0; must not exceed the number of active Data Movers in this group"
             tabindex="0"></i>
        </label>
        <div class="d-flex align-items-center gap-2">
          <input type="number" name="minFilteringCount" id="minFilteringCount"
            min="0" step="1" class="form-control form-control-sm"
            value="<c:out value='${requestScope[actionFormName].minFilteringCount}'/>"
            oninput="validatePatternInput(this, 'minFilterFeedback')">
          <span id="minFilterFeedback"></span>
        </div>
      </div>
    </div>
  </div>
</div>

<%-- Card: Backup --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-cloud-arrow-up text-primary"></i>
    <span class="fw-semibold">Backup</span>
  </div>
  <div class="card-body">
    <div class="mb-3">
      <div class="form-check form-switch">
        <html:checkbox property="backup" styleClass="form-check-input" styleId="backup" />
        <label class="form-check-label" for="backup">Enable backup for this Transfer Group</label>
      </div>
    </div>
    <div class="row g-3">
      <div class="col-sm-6">
        <label for="hostForBackupName" class="form-label mb-1">Host For Backup</label>
        <bean:define id="hosts" name="transferGroupActionForm"
          property="hostForBackupOptions" type="java.util.Collection" />
        <html:select property="hostForBackupName" styleId="hostForBackupName"
          styleClass="form-select form-select-sm">
          <html:options collection="hosts" property="name" labelProperty="nickName" />
        </html:select>
      </div>
    </div>
  </div>
</div>

<%-- Card: Cluster --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-diagram-3 text-primary"></i>
    <span class="fw-semibold">Cluster</span>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-sm-6">
        <label for="clusterName" class="form-label mb-1">Cluster Name</label>
        <html:text property="clusterName" styleId="clusterName" styleClass="form-control form-control-sm" />
      </div>
      <div class="col-sm-6">
        <label for="clusterWeight" class="form-label mb-1">
          Cluster Weight
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="right"
             data-bs-content="Integer >= 0; higher values give this group proportionally more traffic when load-balancing across clusters"
             tabindex="0"></i>
        </label>
        <div class="d-flex align-items-center gap-2">
          <input type="number" name="clusterWeight" id="clusterWeight"
            min="0" step="1" class="form-control form-control-sm"
            value="<c:out value='${requestScope[actionFormName].clusterWeight}'/>"
            oninput="validatePatternInput(this, 'clusterWeightFeedback')">
          <span id="clusterWeightFeedback"></span>
        </div>
      </div>
    </div>
  </div>
</div>

<c:if test="${isInsert != 'true'}">
<p class="fw-bold mb-1 mt-2">Data Movers <a
    href="/do/datafile/transferserver/edit/insert_form?transferGroupName=${transferGroupActionForm.id}"><content:icon
      key="icon.small.insert" titleKey="button.insert"
      altKey="button.insert" writeFullTag="true" /></a></p>
<table id="tgFieldsServersTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
  <thead class="table-primary">
    <tr>
      <th>Name</th>
      <th class="text-center">Actions</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach var="server" items="${transferGroupActionForm.transferServers}">
    <tr>
      <td>${server.name}</td>
      <td class="buttons text-center">
        <auth:link styleClass="menuitem"
          href="/do/datafile/transferserver/edit/update_form/${server.id}"
          imageKey="icon.small.update" imageTitleKey="button.update"
          imageAltKey="button.update" />
        <auth:link styleClass="menuitem"
          href="/do/datafile/transferserver/edit/delete_form/${server.id}"
          imageKey="icon.small.delete" imageTitleKey="button.delete"
          imageAltKey="button.delete" />
      </td>
    </tr>
  </c:forEach>
  </tbody>
</table>
<script>
$(document).ready(function() {
  $('#tgFieldsServersTable').DataTable({
    paging: false, searching: false, ordering: true, info: false,
    columnDefs: [{ orderable: false, targets: -1 }]
  });
});
</script>
</c:if>

<script>
$(document).ready(function() {
  var feedbacks = {
    minReplicationCount: 'minRepFeedback',
    volumeCount: 'volCountFeedback',
    minFilteringCount: 'minFilterFeedback',
    clusterWeight: 'clusterWeightFeedback'
  };
  Object.keys(feedbacks).forEach(function(id) {
    var el = document.getElementById(id);
    if (el && el.value !== '') validatePatternInput(el, feedbacks[id]);
  });
});
</script>

<script>
var _checkNameTimer = null;
function _checkNameExists(value, basePath) {
  clearTimeout(_checkNameTimer);
  var $msg = $('#name-exists-msg');
  var $submit = $('button[type="submit"]').first();
  $msg.hide();
  $submit.prop('disabled', false);
  if (!value || value.length < 1) return;
  _checkNameTimer = setTimeout(function() {
    $.getJSON(basePath + '?json=checkId&id=' + encodeURIComponent(value), function(data) {
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
