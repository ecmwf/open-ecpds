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

  <c:if test="${not empty destSummary}">
    <p class="text-muted small mb-2">
      Found <strong>${totalValues}</strong> value(s)
      <c:if test="${totalErrors > 0}">
        and <strong class="text-danger">${totalErrors}</strong> parse error(s)
      </c:if>
      across <strong>${totalDests}</strong> destination(s).
      Click <strong>Confirm Bulk Import</strong> to save all values.
    </p>

    <div class="table-responsive mb-3" style="max-height:480px;overflow-y:auto;">
      <table class="table table-sm table-bordered table-hover mb-0 align-middle">
        <thead class="table-dark sticky-top">
          <tr>
            <th>Destination</th>
            <th class="text-center">Values</th>
            <th class="text-center">Errors</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${destSummary}">
            <tr>
              <td><code>${row['name']}</code></td>
              <td class="text-center">
                <span class="badge bg-primary">${row['values']}</span>
              </td>
              <td class="text-center">
                <c:choose>
                  <c:when test="${row['errors'] > 0}">
                    <span class="badge bg-danger">${row['errors']}</span>
                  </c:when>
                  <c:otherwise><span class="text-muted">—</span></c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <form action="<c:url value='/do/transfer/destination/metadata/bulkimport'/>" method="get">
      <input type="hidden" name="confirm" value="true"/>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-check-circle me-1"></i>Confirm Bulk Import (${totalValues} value(s) / ${totalDests} destination(s))
      </button>
      <a href="<c:url value='/do/admin/metafields'/>" class="btn btn-outline-secondary ms-2">Cancel</a>
    </form>
  </c:if>

</c:if>
