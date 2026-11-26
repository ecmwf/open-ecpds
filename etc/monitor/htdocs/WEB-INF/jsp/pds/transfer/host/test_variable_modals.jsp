<%-- Modal: pick an existing DataTransfer to resolve transfer-specific placeholders --%>
<div class="modal fade" id="testPickTransferModal" tabindex="-1" aria-labelledby="testPickTransferModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold" id="testPickTransferModalLabel">
          <i class="bi bi-search me-2 text-primary"></i>Resolve Script Variables
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p class="small text-muted mb-2">
          The script uses <code>$dataFile[...]</code> / <code>$dataTransfer[...]</code> or other
          transfer-specific variables. Select a recent DataTransfer to substitute real values, or
          click <strong>Enter values manually</strong>.
        </p>
        <div id="testPickTransferList" class="list-group mb-2" style="max-height:240px;overflow-y:auto">
          <div class="text-muted small fst-italic p-2">Loading&hellip;</div>
        </div>
        <div class="d-flex justify-content-between align-items-center">
          <button type="button" class="btn btn-sm btn-outline-secondary" id="testPickManualBtn">
            <i class="bi bi-pencil-square me-1"></i>Enter values manually
          </button>
          <button type="button" class="btn btn-sm btn-primary" id="testPickRunBtn" disabled>
            <i class="bi bi-play-fill me-1"></i>Run Test
          </button>
        </div>
      </div>
    </div>
  </div>
</div>

<%-- Modal: manually enter values for unresolved placeholders --%>
<div class="modal fade" id="testManualValuesModal" tabindex="-1" aria-labelledby="testManualValuesModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold" id="testManualValuesModalLabel">
          <i class="bi bi-input-cursor-text me-2 text-primary"></i>Enter Placeholder Values
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p class="small text-muted mb-2">Provide values for the variables found in the script. Leave blank to keep the placeholder as-is.</p>
        <div id="testManualValuesList"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-sm btn-primary" id="testManualRunBtn">
          <i class="bi bi-play-fill me-1"></i>Run Test
        </button>
      </div>
    </div>
  </div>
</div>
