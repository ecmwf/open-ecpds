<%@ page contentType="text/html;charset=UTF-8"%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="smGuideOffcanvas"
     aria-labelledby="smGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="smGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>System Message Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto;font-size:0.85rem;">
    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>Use System Messages to announce planned outages, maintenance or service-wide information.
      Banners are visible to every user on the main Monitor menu pages and on the Data Portal,
      without per-user configuration.</div>
    </div>
    <h6 class="fw-semibold">Creating and editing a message</h6>
    <p>Choose <strong>Create</strong> or the row's <i class="bi bi-pencil-fill"></i> <strong>Edit</strong>
    button, complete the fields, then <strong>Save</strong>.</p>
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light"><tr><th>Field</th><th>Meaning</th></tr></thead>
        <tbody>
          <tr><td>Message</td><td>Plain text, with line breaks preserved. Explain the reason and service impact;
          the announced window is displayed alongside the text, so repeating it is unnecessary.</td></tr>
          <tr><td>Level</td><td><strong>Info</strong> (blue), <strong>Warning</strong> (orange, default)
          or <strong>Danger</strong> (red). Controls the banner's colour and icon.</td></tr>
          <tr><td>Start / End</td><td>The announced service-impact window, not a delayed publication time.
          The banner appears as soon as it is saved, including before Start, and disappears after End.</td></tr>
          <tr><td>Quick duration</td><td><strong>+1h</strong>, <strong>+6h</strong>, <strong>+1d</strong>
          and <strong>+1 week</strong> set End relative to Start. If Start is empty, the current time is used.</td></tr>
        </tbody>
      </table>
    </div>
    <p class="small text-muted">The form uses your browser's local timezone. The list shows Start and End in UTC.</p>
    <h6 class="fw-semibold">Status and visibility</h6>
    <ul class="ps-3">
      <li><strong>Upcoming</strong>: Start is in the future; the banner is already visible as an advance warning.</li>
      <li><strong>In progress</strong>: the current time is within the announced window; the banner is visible.</li>
      <li><strong>Expired</strong>: End has passed; the banner is hidden, but the entry remains listed until deleted.</li>
    </ul>
    <p>Monitor banners appear on the landing page and main Data Files, Data Transfers, Users and Administration
    menu pages. The Data Portal reads the same messages, so there is no need to create a second notice.</p>
    <h6 class="fw-semibold">Finding and removing messages</h6>
    <p class="mb-0">Use <strong>Search</strong>, the status filter and page size to browse the list.
    The <i class="bi bi-trash-fill"></i> <strong>Delete</strong> button removes a message immediately after
    confirmation, including a currently visible banner. To let a notice stop automatically, set its End time
    instead; expiry needs no manual cleanup.</p>
  </div>
</div>
