<%-- Step 1: warning + checkbox --%>
<div class="mb-4 p-3 rounded border border-danger-subtle"
     style="background:rgba(220,53,69,0.07); border-left:4px solid #dc3545 !important;">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-exclamation-triangle-fill text-danger flex-shrink-0 mt-1" style="font-size:1.4rem;"></i>
        <div>
            <strong class="text-danger">Destructive operation — irreversible</strong>
            <p class="mb-0 mt-1" style="font-size:0.9rem;">
                This will <strong>permanently delete all transfers and files</strong> across every
                destination. All queued and completed transfers will be cancelled, and all files currently
                stored on the data movers will be physically removed from disk. This is designed for
                performing a full system reset and cannot be undone.
            </p>
        </div>
    </div>
</div>

<div class="card border-danger mb-4">
    <div class="card-header bg-danger text-white d-flex align-items-center gap-2">
        <i class="bi bi-trash3-fill"></i>
        <strong>Purge All Data — Step 1 of 2</strong>
    </div>
    <div class="card-body">
        <p>This will:</p>
        <ul>
            <li>Cancel and remove <strong>all transfers</strong> across every destination</li>
            <li>Physically delete <strong>all files</strong> from every data mover's disk</li>
            <li>Wipe all associated records from the database</li>
        </ul>
        <p class="text-danger fw-bold mb-4">
            <i class="bi bi-exclamation-octagon-fill me-1"></i>
            This cannot be undone. Proceed only if you are certain you want to permanently delete all data.
        </p>

        <form method="POST" action="/do/admin/purge" id="purgeStep1Form">
            <input type="hidden" name="step" value="1" />
            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="purgeConfirmCheck"
                       onchange="document.getElementById('purgeStep1Btn').disabled = !this.checked;" />
                <label class="form-check-label fw-semibold" for="purgeConfirmCheck">
                    I understand that this will permanently delete <em>all</em> data from the database
                    and from all data-mover disks, and that this action cannot be reversed.
                </label>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <button type="submit" id="purgeStep1Btn" class="btn btn-danger" disabled>
                    <i class="bi bi-arrow-right-circle-fill me-1"></i>Proceed to final confirmation
                </button>
                <a href="/do/admin" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</div>
