<div class="modal fade" id="pendingHostPasswordModal" tabindex="-1" aria-labelledby="pendingHostPasswordTitle"
	data-destination="<c:out value="${destination.name}" />">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h5 class="modal-title" id="pendingHostPasswordTitle">Pending transfer credential</h5>
				<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			</div>
			<div class="modal-body">
				<p>Staging and testing a replacement does not change the credential used by transfers.</p>
				<p data-rotation-status role="status" aria-live="polite"></p>
				<div data-rotation-input hidden>
					<label class="form-label" for="pendingTransferPassword" data-rotation-label>Replacement transfer credential</label>
					<input type="password" class="form-control" id="pendingTransferPassword"
						autocomplete="new-password" maxlength="4096" disabled />
					<div class="form-text" data-rotation-credential-help>Use the module's password, token or compact JSON credential bundle (maximum 4096 characters).</div>
					<div data-rotation-grace-row class="mt-3">
						<label class="form-label" for="passwordRetirementHours">Previous credential retirement grace (hours)</label>
						<input type="number" class="form-control" id="passwordRetirementHours" data-rotation-grace
							min="1" max="720" step="1" value="24" />
						<div class="form-text">1 hour to 30 days. Starts only after confirmed successful delivery
							with this replacement. The previous vault version is then permanently destroyed.</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-outline-secondary" data-operation="REFRESH" hidden>Refresh</button>
				<button type="button" class="btn btn-primary" data-operation="STAGE" hidden>Stage</button>
				<button type="button" class="btn btn-primary" data-operation="PROBE" hidden>Test</button>
				<button type="button" class="btn btn-outline-danger" data-operation="DISCARD" hidden>Discard</button>
				<button type="button" class="btn btn-outline-secondary" data-operation="RECONCILE" hidden>Reconcile</button>
				<button type="button" class="btn btn-outline-warning" data-operation="DRAIN" hidden>Prepare online cutover</button>
				<button type="button" class="btn btn-primary" data-operation="BEGIN_ACTIVATION" hidden>Activate</button>
				<button type="button" class="btn btn-danger" data-operation="ACTIVATE" hidden>Confirm activation</button>
			</div>
		</div>
	</div>
</div>
<script src="/assets/js/pending-host-password.js"></script>
