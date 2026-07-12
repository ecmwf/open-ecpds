<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
  <h6 class="mb-0 fw-semibold"><i class="bi bi-cloud-upload me-2"></i>Bulk Import XML Metadata</h6>
  <a href="<c:url value='/do/admin/metafields'/>" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left me-1"></i>Back to Metadata Fields
  </a>
</div>

<c:if test="${not empty importError}">
  <div class="alert alert-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>${importError}</div>
</c:if>

<c:if test="${importDone}">
  <div class="alert alert-success">
    <i class="bi bi-check-circle-fill me-2"></i>
    Successfully imported <strong>${importedValues}</strong> metadata value(s) across
    <strong>${importedDests}</strong> destination(s).
  </div>
  <a href="<c:url value='/do/admin/metafields'/>" class="btn btn-outline-secondary btn-sm">
    <i class="bi bi-arrow-left me-1"></i>Back to Metadata Fields
  </a>
</c:if>

<c:if test="${not importDone and empty importError}">

  <div class="alert alert-info d-flex gap-2 align-items-start">
    <i class="bi bi-info-circle-fill flex-shrink-0 mt-1"></i>
    <div>
      This page scans <strong>all</strong> per-destination attachment directories for
      <code>*.xml</code> files and imports any recognized metadata fields. Each destination's
      existing metadata values will be <strong>replaced</strong> by the imported ones.
    </div>
  </div>

  <c:if test="${empty byDestination}">
    <div class="alert alert-warning">
      <i class="bi bi-folder-x me-2"></i>
      No XML metadata files were found in any destination attachment directory.
    </div>
  </c:if>

  <c:if test="${not empty byDestination}">
    <p class="text-muted small mb-2">
      Found <strong>${totalValues}</strong> value(s)
      <c:if test="${totalErrors > 0}">
        and <strong class="text-danger">${totalErrors}</strong> parse error(s)
      </c:if>
      across <strong>${byDestination.size()}</strong> destination(s).
      Review the preview below, then click <strong>Confirm Bulk Import</strong> to save.
    </p>

    <%-- Per-destination accordion --%>
    <div class="accordion mb-3" id="bulkImportAccordion">
      <c:forEach var="destEntry" items="${byDestination}" varStatus="st">
        <div class="accordion-item">
          <h2 class="accordion-header" id="bih-${st.index}">
            <button class="accordion-button collapsed py-2" type="button"
                    data-bs-toggle="collapse" data-bs-target="#bic-${st.index}"
                    aria-expanded="false" aria-controls="bic-${st.index}">
              <i class="bi bi-hdd-network me-2 text-secondary"></i>
              <strong>${destEntry.key}</strong>
              <span class="badge bg-primary ms-2">${destEntry.value.size()} value(s)</span>
            </button>
          </h2>
          <div id="bic-${st.index}" class="accordion-collapse collapse"
               aria-labelledby="bih-${st.index}" data-bs-parent="#bulkImportAccordion">
            <div class="accordion-body p-0">
              <table class="table table-sm table-bordered mb-0">
                <thead class="table-dark">
                  <tr><th>Field Name</th><th>Value</th></tr>
                </thead>
                <tbody>
                  <c:forEach var="row" items="${destEntry.value}">
                    <tr>
                      <c:if test="${not empty row.error}">
                        <td colspan="2" class="text-danger small">
                          <i class="bi bi-exclamation-triangle me-1"></i>${row.file}: ${row.error}
                        </td>
                      </c:if>
                      <c:if test="${empty row.error}">
                        <td><code>${row.fieldName}</code></td>
                        <td class="text-truncate" style="max-width:420px" title="${row.value}">${row.value}</td>
                      </c:if>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </c:forEach>
    </div>

    <form action="<c:url value='/do/transfer/destination/metadata/bulkimport'/>" method="get">
      <input type="hidden" name="confirm" value="true"/>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-check-circle me-1"></i>Confirm Bulk Import (${totalValues} value(s) / ${byDestination.size()} destination(s))
      </button>
      <a href="<c:url value='/do/admin/metafields'/>" class="btn btn-outline-secondary ms-2">Cancel</a>
    </form>
  </c:if>

</c:if>
