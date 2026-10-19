<%@ page contentType="text/html;charset=UTF-8"%>
<%-- Product Messages configuration guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="pmGuideOffcanvas"
     aria-labelledby="pmGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="pmGuideLabel">
      <i class="bi bi-envelope-paper me-2 text-info"></i>Product Messages &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>These templates pre-fill the Outlook or direct emails of the monitoring product pages and the body of the notices
      on the <a href="/do/monitoring/delaynotification">Delay Notification</a> page. Each message has an
      <strong>Ungrouped</strong> variant (one product and cycle) and a <strong>Grouped</strong> variant (several
      products or cycles). Use <strong>Reset current variant</strong> to restore the displayed body; click
      <strong>Save Messages</strong> to persist it. Empty or unchanged text also restores the built-in default.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#pmg-usage" type="button"><i class="bi bi-diagram-3 me-1"></i>Where used</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pmg-ph" type="button"><i class="bi bi-braces me-1"></i>Placeholders</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pmg-multi" type="button"><i class="bi bi-collection me-1"></i>Several products</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pmg-other" type="button"><i class="bi bi-gear me-1"></i>Cycles &amp; addresses</button></li>
    </ul>

    <div class="tab-content">

      <div class="tab-pane fade show active" id="pmg-usage" role="tabpanel">
        <p class="small text-muted mb-2">Product summary pages show <strong>Delay Notification</strong> and
        <strong>Legacy Email Actions</strong> inside <strong>Email Notifications</strong>, only when explicit contact
        emails are available for that page. A single-cycle page uses that cycle's contacts; all-cycles and
        all-products pages combine and deduplicate contacts across their scope. Legacy email actions do not
        register a notice.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Message</th><th>Variant</th><th>Used by</th></tr></thead>
            <tbody>
              <tr><td rowspan="2">Products Delay</td><td>Ungrouped</td><td><em>Delay</em> button of a single product/cycle page.</td></tr>
              <tr><td>Grouped</td><td><em>Delay</em> button of an all-cycles / all-products page, and the <strong>message body of a notice</strong> on the Delay Notification page.</td></tr>
              <tr><td rowspan="2">Products Resumed</td><td>Ungrouped</td><td><em>Resumed</em> button of a single product/cycle page.</td></tr>
              <tr><td>Grouped</td><td><em>Resumed</em> button of an all-cycles page, and the text pre-filled by <strong>Inform resolution</strong> on the Delay Notification page.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">Use the <strong>Ungrouped / Grouped</strong> switch of each card to edit and preview the
        corresponding variant.</p>
      </div>

      <div class="tab-pane fade" id="pmg-ph" role="tabpanel">
        <p class="small text-muted mb-2">Use the <strong>Insert at cursor</strong> buttons above each editor. They insert the
        placeholder at the cursor (or over the selected text) and only list those valid for the displayed variant. A placeholder
        may appear several times in the same message.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Placeholder</th><th>Variant</th><th>Replaced by</th></tr></thead>
            <tbody>
              <tr><td><code>{{PRODUCT}}</code></td><td>Ungrouped</td><td>The product name.</td></tr>
              <tr><td><code>{{CYCLE}}</code></td><td>Ungrouped</td><td>The cycle (e.g. <code>00</code>).</td></tr>
              <tr><td><code>{{PRODUCTS}}</code></td><td>Grouped</td><td>The list of products ("A, B and C"). On a monitoring page it is the page's product.</td></tr>
              <tr><td><code>{{CYCLES}}</code></td><td>Grouped</td><td>The list of cycles (e.g. <code>00,12</code>).</td></tr>
              <tr><td><code>{{DESCRIPTION}}</code></td><td>Both</td><td>Descriptions for the product types shown, from <a href="/do/admin/productdescriptions">Product Descriptions</a>. When types have different descriptions, this is a bullet list (one <code>- TYPE: description</code> line per type); otherwise the shared description is shown once.</td></tr>
              <tr><td><code>{{AFFECTED}}</code></td><td>Grouped delay</td><td>The block of <em>Definite</em> and <em>Possible</em> product/cycle lists with reason, delay, ETA and note (Delay Notification page). Removed on monitoring pages. Use it <strong>once</strong>; if missing, the block is inserted after the first paragraph.</td></tr>
              <tr><td><code>{{REASON}}</code></td><td>Grouped delay/resumed</td><td>The reason entered for a Delay Notification, or saved with that notice when composing its resolution.</td></tr>
              <tr><td><code>{{ETA}}</code></td><td>Grouped delay/resumed</td><td>The notice ETA, followed by <code>UTC</code> when set.</td></tr>
              <tr><td><code>{{EXPECTED_DELAY}}</code></td><td>Grouped delay/resumed</td><td>The expected delay selected for the notice.</td></tr>
              <tr><td><code>{{NOTE}}</code></td><td>Grouped delay/resumed</td><td>The note entered for the notice.</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-2">The four notice-detail placeholders are replaced with the values from the current notice; on a resolution message they use the values saved with that notice. They are empty on regular monitoring-page emails. In the grouped delay template, each value placed with its own placeholder is omitted from the automatically inserted affected block, while the other details remain there.</p>
        <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">Dear colleagues,

The dissemination of ECMWF {{PRODUCTS}} products for the {{CYCLES}}Z cycles will be delayed.

{{AFFECTED}}

Product descriptions:
{{DESCRIPTION}}

Kind regards,
ECMWF Duty Manager</pre>
      </div>

      <div class="tab-pane fade" id="pmg-multi" role="tabpanel">
        <p class="small mb-2">When a notice covers several products, each with its own cycles, the grouped message adapts:</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Situation</th><th>Result</th></tr></thead>
            <tbody>
              <tr><td>All products share the same cycles</td><td><code>GENFO and GWAVE</code> &hellip; "for the 00,12Z cycles"</td></tr>
              <tr><td>Products have different cycles</td><td><code>GENFO (00,12Z) and GWAVE (06,18Z)</code> &hellip; "for their respective cycles"</td></tr>
            </tbody>
          </table>
        </div>
        <p class="small text-muted mb-0">Only the exact wording "for the <code>{{CYCLES}}</code>Z cycles" is rewritten. With any other
        phrasing, <code>{{CYCLES}}</code> shows the combined list of cycles.</p>
      </div>

      <div class="tab-pane fade" id="pmg-other" role="tabpanel">
        <p class="small fw-semibold mb-1"><i class="bi bi-list-ol text-primary me-1"></i>Cycle list formatting</p>
        <p class="small text-muted mb-3">When enabled, consecutive cycles are compacted (e.g. <code>00,06,12,18</code> becomes <code>00-18</code>).
        It applies to <code>{{CYCLES}}</code> on monitoring pages and on the Delay Notification page.</p>
        <p class="small fw-semibold mb-1"><i class="bi bi-markdown text-primary me-1"></i>Message format</p>
        <p class="small text-muted mb-3">Messages are Markdown by default, with a live preview next to the editor and line breaks kept as typed.
        Direct sends are formatted HTML; <em>Send with Outlook</em> uses a plain-text approximation. The Delay Notification preview can
        switch between both views. Disable the switch to send plain text.</p>
        <p class="small fw-semibold mb-1"><i class="bi bi-at text-primary me-1"></i>Notification addresses</p>
        <p class="small text-muted mb-0">The <strong>To / From</strong> address (default <code>operators@ecmwf.int</code>) and the address always placed
        in <strong>Bcc</strong> (default <code>op_delay_ecmwf@lists.ecmwf.int</code>) used by the email buttons and by the Delay
        Notification page. Leave blank to restore the default. The two switches allow operators to add optional Cc and
        Cc and additional Bcc recipients on the Delay Notification page; both are enabled by default.</p>
      </div>

    </div>
  </div>
</div>
