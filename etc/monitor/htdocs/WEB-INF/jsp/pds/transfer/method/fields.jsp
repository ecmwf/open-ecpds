<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute id="actionFormName" name="action.form.name"
	classname="java.lang.String" />
<tiles:useAttribute name="isInsert" classname="java.lang.String" />
<div class="card border-0 shadow-sm mb-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-plug text-primary"></i>
    <span class="fw-semibold">
      <c:choose>
        <c:when test="${isInsert == 'true'}">New Transfer Method</c:when>
        <c:otherwise>Edit Transfer Method</c:otherwise>
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
              title="Letters and digits only (e.g. Ftp)"
              oninput="validatePatternInput(this, 'name-feedback')">
            <span id="name-feedback"></span>
          </div>
        </logic:match>
        <logic:notMatch name="isInsert" value="true">
          <div class="form-control-plaintext form-control-sm fw-medium">
            <c:out value="${requestScope[actionFormName].name}" />
            <html:hidden property="name" />
          </div>
        </logic:notMatch>
      </div>

      <%-- Value --%>
      <div class="col-sm-4">
        <label class="form-label mb-1">Value</label>
        <html:text property="value" styleClass="form-control form-control-sm" />
      </div>

      <%-- Transfer Module --%>
      <div class="col-sm-4">
        <label class="form-label mb-1">Transfer Module</label>
        <bean:define id="methods" name="transferMethodActionForm"
          property="ecTransModuleOptions" type="java.util.Collection" />
        <html:select property="ecTransModuleName" styleClass="form-select form-select-sm">
          <html:options collection="methods" property="name" labelProperty="name" />
        </html:select>
      </div>

      <%-- Comment --%>
      <div class="col-sm-8">
        <label class="form-label mb-1">Comment</label>
        <html:text property="comment" styleClass="form-control form-control-sm" />
      </div>

      <%-- Flags --%>
      <div class="col-sm-4">
        <label class="form-label mb-1 d-block">&nbsp;</label>
        <div class="d-flex flex-column gap-1 pt-1">
          <div class="form-check form-switch mb-0">
            <html:checkbox property="restrict" styleClass="form-check-input" styleId="restrict" />
            <label class="form-check-label small" for="restrict">Restrict</label>
          </div>
          <div class="form-check form-switch mb-0">
            <html:checkbox property="resolve" styleClass="form-check-input" styleId="resolve" />
            <label class="form-check-label small" for="resolve">Resolve</label>
          </div>
          <div class="form-check form-switch mb-0">
            <html:checkbox property="active" styleClass="form-check-input" styleId="active" />
            <label class="form-check-label small" for="active">Enabled</label>
          </div>
        </div>
      </div>

    </div>
  </div>
</div>
