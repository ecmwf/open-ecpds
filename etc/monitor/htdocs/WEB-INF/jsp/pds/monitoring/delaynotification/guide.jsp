<%@ page contentType="text/html;charset=UTF-8"%>
<%-- Delay Notification configuration guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="dnGuideOffcanvas"
     aria-labelledby="dnGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="dnGuideLabel">
      <i class="bi bi-bell me-2 text-info"></i>Delay Notification &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>Send <strong>one consistent notice</strong> for all the products and cycles affected by a delay, then inform
      of the resolution. Select what is affected, review the generated message, then send it.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#dng-matrix" type="button"><i class="bi bi-grid-3x3-gap me-1"></i>Selection</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dng-rcpt" type="button"><i class="bi bi-people me-1"></i>Recipients</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dng-msg" type="button"><i class="bi bi-envelope me-1"></i>Message</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dng-life" type="button"><i class="bi bi-arrow-repeat me-1"></i>Lifecycle</button></li>
    </ul>

    <div class="tab-content">

      <div class="tab-pane fade show active" id="dng-matrix" role="tabpanel">
        <p class="small text-muted mb-2">The matrix lists the products and cycles currently known to the monitoring. Filter products by name, model, cycle, or one or more generation timings; a product is shown when any of its visible cycles matches. The <strong>Before</strong> column uses a colored dot for the nearest cycle status across all cycles; hover it to see the exact time before schedule or how long it is overdue. Cycle cells use a soft background tint from the same Product Generation legend; hover a cell for that cycle's exact timing.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Action</th><th>Effect</th></tr></thead>
            <tbody>
              <tr><td>Click a cell</td><td>Cycles between <strong>D</strong> (Definite, affected), <strong>P</strong> (Possible, might be) and cleared. Both levels are listed separately in the message and both receive it.</td></tr>
              <tr><td>Click a product name</td><td>Applies the same level to its visible cycles.</td></tr>
              <tr><td>Product name filter</td><td>As in <code>/do/monitoring</code>: comma-separated, <code>*</code> and <code>?</code> wildcards, <code>!</code> or <code>-</code> to exclude.</td></tr>
              <tr><td>Model buttons</td><td>Select all the products of a model (models are maintained in <em>Product Descriptions</em>).</td></tr>
              <tr><td>Cycle</td><td>Show one cycle column, or choose <em>All cycles</em> to restore every available cycle.</td></tr>
              <tr><td>Mark late/overdue D, Mark 0-15m ahead P, Clear all</td><td>Apply only to the <strong>filtered</strong> cells (product name, model, cycle and timing filters). Cells marked earlier but currently hidden by a filter are kept, but are not counted, notified or mailed until they are shown again.</td></tr>
              <tr><td>A dot</td><td>The product has no such cycle.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">Your selection drives the recipients and fills the message. The page can also be opened from the
        <em>Register Delay Notice</em> buttons of a monitoring product page: the Product name is filled in, model and timing filters are cleared for this visit, and only the requested cycle is shown (or all cycles for an all-cycles link). Matching cells are pre-selected. Saved model and timing preferences are not overwritten by the link.</p>
        <p class="small text-muted mt-2 mb-0">On product summary pages, <strong>Email Notifications</strong> contains
        <strong>Delay Notification</strong> on the left and <strong>Legacy Email Actions</strong> on the right
        (stacked on narrower screens). Both are hidden when no explicit contact emails are available. Contacts cover
        the current product/cycle, all cycles of a product, or all products and cycles, with duplicates removed from
        combined lists. Legacy Outlook/direct-send buttons do not register a notice.</p>
      </div>

      <div class="tab-pane fade" id="dng-rcpt" role="tabpanel">
        <ul class="small mb-3 ps-3">
          <li>For each selected product and cycle, ECPDS finds the destinations receiving it (the same data as the destinations table at the bottom of <code>/do/monitoring</code>) and takes the contact emails registered for them.</li>
          <li>Emails are <strong>de-duplicated</strong> (case-insensitive) across all destinations and products: a person receives the notice only once. The <em>(n) Destinations</em> pill lists the destinations concerned and the other pill gives the number of unique emails.</li>
          <li>By default only destinations showing in the monitor are used; the switch adds the others. A destination without contact is listed but adds no email.</li>
        </ul>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Field</th><th>Content</th></tr></thead>
            <tbody>
              <tr><td>To</td><td>The From/To address (default <code>operators@ecmwf.int</code>).</td></tr>
              <tr><td>Bcc</td><td>The selected recipients, additional Bcc recipients, and the always-in-Bcc list (default <code>op_delay_ecmwf@lists.ecmwf.int</code>), so they do not see each other. Additional Bcc recipients are available when enabled in <a href="/do/admin/productmessages">Product Messages</a>; separate addresses by semicolons and they are validated before saving or sending. The disk button saves them as the default. If the list is too long for an Outlook link, the product distribution lists are used; <em>Copy BCC emails</em> gives the explicit recipients.</td></tr>
              <tr><td>Cc</td><td>Cc recipients are available when enabled in <a href="/do/admin/productmessages">Product Messages</a>. Separate addresses by semicolons; they are validated before saving or sending. The disk button saves them as the default; untick the box to send without Cc.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">The To and Bcc addresses are set in <a href="/do/admin/productmessages">Product Messages</a>.</p>
      </div>

      <div class="tab-pane fade" id="dng-msg" role="tabpanel">
        <p class="small mb-2"><strong>Subject and message</strong> are generated from the grouped <em>Products Delay</em> message of
        <a href="/do/admin/productmessages">Product Messages</a>, your selection, and the reason, delay, ETA (UTC) and note. They stay editable;
        Subjects include the selected cycles and respect <em>Automatically compact grouped cycle lists</em> in Product Messages.
        <em>Regenerate</em> rebuilds them and <em>Preview</em> shows the final mail. Editing either field tints both the subject and message and
        stops automatic updates until you use <em>Regenerate</em>, which also clears their tint. Only the filtered selected cells are used
        to build the message and the recipients.</p>
        <p class="small mb-2">Choose a predefined reason or select <em>Other (enter a reason)</em> to provide free text.
        The optional note is hidden and excluded by default; select <em>Add a note</em> to show the editor and include it in the generated message.</p>
        <p class="small mb-2">In <em>Product Messages</em>, place <code>{{REASON}}</code>, <code>{{ETA}}</code>,
        <code>{{EXPECTED_DELAY}}</code> and <code>{{NOTE}}</code> wherever those individual values should appear.
        <code>{{ETA}}</code> includes <code>UTC</code> when set. A value placed with its own placeholder is omitted
        from the affected block; any other values still appear there. The <strong>Compact empty lines</strong> switch
        beside the message editor reduces sequences of blank lines to one blank line; the setting is remembered for
        your browser.</p>
        <p class="small fw-semibold mb-1"><i class="bi bi-list-check text-primary me-1"></i>Affected block</p>
        <p class="small mb-2">Inserted at the <code>{{AFFECTED}}</code> placeholder, or after the first paragraph when it is absent:</p>
        <ul class="small mb-3 ps-3">
          <li><em>Products and cycles affected</em>: one line per product with its <strong>Definite</strong> cycles, e.g. <code>- GENFO (cycles: 00-03, 12)</code>.</li>
          <li><em>Products and cycles that might be affected</em>: the same for the <strong>Possible</strong> cells.</li>
          <li>Then the reason, expected delay, ETA (UTC) and the note, when filled in.</li>
        </ul>
        <p class="small fw-semibold mb-1"><i class="bi bi-braces text-primary me-1"></i>Placeholders</p>
        <ul class="small mb-0 ps-3">
          <li><code>{{PRODUCTS}}</code> is the list of selected products (<code>{{PRODUCT}}</code> gives the same); <code>{{CYCLES}}</code> their cycles; <code>{{DESCRIPTION}}</code> the product descriptions.</li>
          <li>When products do not share the same cycles, <code>{{PRODUCTS}}</code> names the cycles of each (e.g. <em>GENFO (00,12Z) and GWAVE (06,18Z)</em>) and "for the {{CYCLES}}Z cycles" becomes "for their respective cycles".</li>
        </ul>
      </div>

      <div class="tab-pane fade" id="dng-life" role="tabpanel">
        <ol class="small mb-3 ps-3">
          <li><strong>Send with Outlook</strong> registers the notice (reference <code>DLY-year-number</code>) and opens Outlook; confirm with <em>I have sent it</em> after sending.</li>
          <li><strong>Send directly</strong> queues the message through the Master mail service and acknowledges it automatically. Pending messages can also be sent directly from <em>Sent notices</em>.</li>
          <li>Later updates can add recipients.</li>
          <li><strong>Inform resolution</strong> (in <em>Sent notices</em>) opens a form listing the open cells. The subject and message are pre-filled from the grouped <em>Products Resumed</em> message, using the same placeholders, and follow the cells you tick until you edit them. <em>Regenerate</em> rebuilds them, <em>Preview</em> shows the final mail, then register it and open Outlook, or send it directly. Resolving only some of the cells leaves the notice partial.</li>
          <li>Cancellation is also handled from <em>Sent notices</em>.</li>
          <li>Use the report controls below the sent-notices table to export a date-bounded CSV summary, with optional message history and recipient addresses.</li>
        </ol>
        <p class="small text-muted mb-0">Notices are stored in the database (<code>DELAY_NOTICE</code> table).</p>
      </div>

    </div>
  </div>
</div>
