<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<tiles:useAttribute id="actionFormName" name="action.form.name" classname="java.lang.String" />

<html:hidden property="id" />

<c:choose>
  <c:when test="${isInsert == 'true'}">
    <div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
         style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
      <i class="bi bi-server text-primary flex-shrink-0"></i>
      <span>Register a new Data Mover for data file processing.</span>
    </div>
  </c:when>
  <c:otherwise>
    <div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
         style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
      <i class="bi bi-server text-primary flex-shrink-0"></i>
      <span>Edit the Data Mover configuration.</span>
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
            pattern="[a-zA-Z0-9]+([_-][a-zA-Z0-9]+)*"
            title="Must start and end with a letter or digit; '_' or '-' allowed as single separators (e.g. server-1)"
            oninput="validatePatternInput(this, 'name-feedback')">
          <div id="name-feedback" class="form-text"></div>
          <div class="form-text">Letters, digits, <code>_</code> and <code>-</code> separators (e.g. <code>server-1</code>).</div>
        </logic:match>
        <logic:notMatch name="isInsert" value="true">
          <div class="form-control form-control-sm bg-body-secondary"><c:out value="${requestScope[actionFormName].name}" /></div>
          <html:hidden property="name" />
        </logic:notMatch>
      </div>
      <div class="col-sm-6">
        <label for="transferGroupName" class="form-label mb-1">Transfer Group</label>
        <bean:define id="groups" name="transferServerActionForm"
          property="transferGroupOptions" type="java.util.Collection" />
        <html:select property="transferGroupName" styleId="transferGroupName"
          styleClass="form-select form-select-sm">
          <html:options collection="groups" property="name" labelProperty="name" />
        </html:select>
      </div>
    </div>
  </div>
</div>

<%-- Card: Connection --%>
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-ethernet text-primary"></i>
    <span class="fw-semibold">Connection</span>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-sm-5">
        <label for="host" class="form-label mb-1">
          Hostname
          <i class="bi bi-question-circle text-muted ms-1" style="cursor:pointer;font-size:0.8em"
             data-bs-toggle="popover" data-bs-placement="right"
             data-bs-content="DNS name of the Data Mover host"
             tabindex="0"></i>
        </label>
        <div class="d-flex align-items-center gap-2">
          <input id="host" name="host" type="text"
            class="form-control form-control-sm"
            value="${requestScope[actionFormName].host}"
            oninput="validateHostInput(this)">
          <span id="hostFeedback"></span>
        </div>
      </div>
      <div class="col-sm-3">
        <label for="port" class="form-label mb-1">Port</label>
        <div class="d-flex align-items-center gap-2">
          <input type="number" id="port" name="port"
            min="1" max="65535"
            class="form-control form-control-sm"
            value="${requestScope[actionFormName].port}"
            title="Valid port number (1-65535)"
            oninput="validatePatternInput(this, 'port-feedback')">
          <span id="port-feedback"></span>
        </div>
      </div>
      <div class="col-sm-4">
        <label for="hostForReplicationName" class="form-label mb-1">Host For Replication</label>
        <bean:define id="hosts" name="transferServerActionForm"
          property="hostForReplicationOptions" type="java.util.Collection" />
        <html:select property="hostForReplicationName" styleId="hostForReplicationName"
          styleClass="form-select form-select-sm">
          <html:options collection="hosts" property="name" labelProperty="nickName" />
        </html:select>
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
          <html:checkbox property="check" styleClass="form-check-input" styleId="check" />
          <label class="form-check-label" for="check">Check</label>
        </div>
      </div>
      <div class="col-sm-4">
        <div class="form-check form-switch">
          <html:checkbox property="active" styleClass="form-check-input" styleId="active" />
          <label class="form-check-label" for="active">Enabled</label>
        </div>
      </div>
      <div class="col-sm-4">
        <div class="form-check form-switch">
          <html:checkbox property="replicate" styleClass="form-check-input" styleId="replicate" />
          <label class="form-check-label" for="replicate">Replicate</label>
        </div>
      </div>
    </div>
  </div>
</div>
