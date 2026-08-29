<%-- Step 2: type exact confirmation phrase --%>
<%@ page import="ecmwf.ecpds.master.plugin.http.controller.admin.PurgeAllDataAction" %>
<div class="mb-4 p-3 rounded border border-danger"
     style="background:rgba(220,53,69,0.12); border-left:4px solid #dc3545 !important;">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-exclamation-octagon-fill text-danger flex-shrink-0 mt-1" style="font-size:1.6rem;"></i>
        <div>
            <strong class="text-danger fs-5">Final confirmation required</strong>
            <p class="mb-0 mt-1" style="font-size:0.9rem;">
                You are about to permanently delete <strong>all data transfers and data files</strong>.
                There is no undo. Type the exact phrase below to proceed.
            </p>
        </div>
    </div>
</div>

<%
    final String purgeError = (String) request.getAttribute("purgeError");
    if (purgeError != null) {
%>
<div class="alert alert-danger d-flex align-items-center gap-2 mb-3">
    <i class="bi bi-x-circle-fill flex-shrink-0"></i>
    <span><%= purgeError %></span>
</div>
<% } %>

<div class="card border-danger mb-4">
    <div class="card-header bg-danger text-white d-flex align-items-center gap-2">
        <i class="bi bi-trash3-fill"></i>
        <strong>Purge All Data — Step 2 of 2</strong>
    </div>
    <div class="card-body">
        <p>To confirm, type the following phrase exactly as shown into the box below:</p>
        <div class="alert alert-warning fw-bold text-center fs-5 mb-3 letter-spacing-1"
             style="font-family:monospace; letter-spacing:0.15em;">
            <%= PurgeAllDataAction.CONFIRMATION_PHRASE %>
        </div>

        <form method="POST" action="/do/admin/purge" id="purgeStep2Form">
            <input type="hidden" name="step" value="2" />
            <div class="mb-3">
                <input type="text" class="form-control form-control-lg border-danger"
                       id="purgePhrase" name="phrase" autocomplete="off"
                       placeholder="Type the phrase here..."
                       style="font-family:monospace; font-size:1.1rem;"
                       oninput="checkPhrase(this.value);" />
                <div id="phraseFeedback" class="form-text text-muted mt-1">
                    The submit button will activate when the phrase matches exactly.
                </div>
            </div>
<%
    final Boolean requiresPassword = (Boolean) request.getAttribute("requiresPassword");
    if (Boolean.TRUE.equals(requiresPassword)) {
%>
            <div class="mb-3">
                <label for="purgePassword" class="form-label fw-semibold">
                    <i class="bi bi-key-fill text-warning me-1"></i>Critical Action Password
                </label>
                <input type="password" class="form-control form-control-lg"
                       id="purgePassword" name="purgePassword" autocomplete="off"
                       placeholder="Enter the Critical Action Password..." />
                <div class="form-text text-muted">
                    This password is required to perform irreversible administrative actions. It is different from your
                    normal administrator password and is managed in <a href="/do/admin/criticalpassword">Admin Tasks → Critical Action Password</a>.
                </div>
            </div>
<% } %>
            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="triggerNow" name="triggerNow" value="true" />
                <label class="form-check-label" for="triggerNow">
                    <strong>Trigger cleanup immediately</strong> — run the database cleanup and file deletion
                    now rather than waiting for the next automatic cycle (recommended for test resets).
                </label>
            </div>
            <div class="mb-4 ms-4 border-start border-warning ps-3" id="forceDeleteDBGroup">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="forceDeleteDB" name="forceDeleteDB"
                           value="true" />
                    <label class="form-check-label" for="forceDeleteDB">
                        <strong>Also hard-delete all database records immediately</strong> — removes all transfer
                        records, history and file entries from the database in a background task (so the browser
                        does not time out), then triggers a full disk scan on every data mover so unreferenced
                        files are removed from disk, without waiting for the next scheduled scan cycle.
                    </label>
                </div>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <button type="submit" id="purgeStep2Btn" class="btn btn-danger btn-lg" disabled>
                    <i class="bi bi-trash3-fill me-1"></i>Confirm — Delete All Data
                </button>
                <a href="/do/admin" class="btn btn-secondary btn-lg">Cancel</a>
            </div>
        </form>
    </div>
</div>

<script>
function checkPhrase(value) {
    const target = '<%= PurgeAllDataAction.CONFIRMATION_PHRASE %>';
    const btn    = document.getElementById('purgeStep2Btn');
    const fb     = document.getElementById('phraseFeedback');
    const input  = document.getElementById('purgePhrase');
    if (value === target) {
        btn.disabled = false;
        input.classList.remove('border-danger');
        input.classList.add('border-success');
        fb.textContent = '✓ Phrase matches — you may now confirm.';
        fb.className = 'form-text text-success mt-1 fw-semibold';
    } else {
        btn.disabled = true;
        input.classList.remove('border-success');
        input.classList.add('border-danger');
        fb.textContent = 'The submit button will activate when the phrase matches exactly.';
        fb.className = 'form-text text-muted mt-1';
    }
}
</script>
