<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>

<div class="d-flex align-items-center justify-content-between mb-3">
  <h6 class="mb-0 fw-semibold"><i class="bi bi-upload me-2"></i>Import XML Metadata for ${destination.name}</h6>
  <a href="<c:url value='/do/transfer/destination/metadata/${destination.name}'/>" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left me-1"></i>Back to Metadata
  </a>
</div>

<c:if test="${not empty importError}">
  <div class="alert alert-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>${importError}</div>
</c:if>

<c:if test="${importDone}">
  <div class="alert alert-success"><i class="bi bi-check-circle-fill me-2"></i>
    Successfully imported <strong>${importCount}</strong> metadata value(s) for destination <strong>${destination.name}</strong>.
  </div>
</c:if>

<c:if test="${not importDone and empty importError}">
  <c:if test="${empty importPreview}">
    <div class="alert alert-info"><i class="bi bi-info-circle-fill me-2"></i>
      No XML metadata files found in the attachments directory for this destination.
    </div>
  </c:if>

  <c:if test="${not empty importPreview}">
    <p class="text-muted small">The following values were found in the XML files. Review them and click <strong>Confirm Import</strong> to save.</p>
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered table-striped">
        <thead class="table-dark">
          <tr>
            <th>Field Name</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${importPreview}">
            <tr>
              <c:if test="${not empty row.error}">
                <td colspan="2" class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>${row.error}</td>
              </c:if>
              <c:if test="${empty row.error}">
                <td><code>${row.fieldName}</code></td>
                <td style="max-width:500px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="${row.value}">${row.value}</td>
              </c:if>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <form action="<c:url value='/do/transfer/destination/metadata/import/${destination.name}'/>" method="get">
      <input type="hidden" name="confirm" value="true"/>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-check-circle me-1"></i>Confirm Import (${importPreview.size()} value(s))
      </button>
    </form>
  </c:if>
</c:if>
