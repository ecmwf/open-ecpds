<%@ page contentType="text/html;charset=UTF-8"%>
<%-- Product Descriptions configuration guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="pdGuideOffcanvas"
     aria-labelledby="pdGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="pdGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Product Description Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>Metadata for a product (e.g. <code>GOPER</code>, as seen in <code>/do/monitoring/summary/GOPER/06</code>),
      optionally scoped to one of its types. The <strong>Description</strong> feeds the <code>{{DESCRIPTION}}</code>
      placeholder of the <a href="/do/admin/productmessages">Product Messages</a>; the <strong>Tips</strong> are shown on
      the <a href="/do/monitoring">monitoring</a> product pages.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#pdg-fields" type="button"><i class="bi bi-ui-checks me-1"></i>Fields</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pdg-types" type="button"><i class="bi bi-layers me-1"></i>Types &amp; defaults</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pdg-models" type="button"><i class="bi bi-diagram-3 me-1"></i>Models</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#pdg-maint" type="button"><i class="bi bi-tools me-1"></i>Maintenance</button></li>
    </ul>

    <div class="tab-content">

      <div class="tab-pane fade show active" id="pdg-fields" role="tabpanel">
        <div class="table-responsive">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:28%">Field</th><th>Meaning</th></tr></thead>
            <tbody>
              <tr><td><strong>Product</strong></td><td>The product name, as shown in the monitoring interface.</td></tr>
              <tr><td><strong>Type</strong></td><td>Optional type of the product (e.g. <code>AN</code>, <code>FC</code>). Blank means <em>all types</em>.</td></tr>
              <tr><td><strong>Description</strong></td><td>Used to build the <code>{{DESCRIPTION}}</code> placeholder of the Product Messages: a bullet list with one line per type currently shown for the product, or plain text when every type resolves to the same text.</td></tr>
              <tr><td><strong>Tips</strong></td><td>Same default/override and list-or-plain-text behaviour as the Description, but shown in the expandable <i class="bi bi-info-circle"></i> card of the product's monitoring page (e.g. <code>/do/monitoring/summary/GOPER/06/0/AN</code>), not in the messages.</td></tr>
              <tr><td><strong>Group all cycles/times into one page</strong></td><td>Generic entry only. The product's pill on the monitoring page shows a <i class="bi bi-layers-fill text-primary"></i> icon and links to <code>/do/monitoring/summary/GOPER</code>, a single page listing every cycle/time, instead of one page per cycle/time.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="tab-pane fade" id="pdg-types" role="tabpanel">
        <p class="mb-2">A <strong>generic</strong> entry (blank Type) is the <strong>default</strong> for every type of the
        product that has no entry of its own. One generic entry is enough to cover all types.</p>
        <p class="mb-2">Add a <strong>type-specific</strong> entry only for the types needing different wording; it overrides
        the generic one for that type alone.</p>
        <div class="table-responsive mb-0">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Entries configured</th><th>Result for <code>GOPER</code></th></tr></thead>
            <tbody>
              <tr><td>Generic only</td><td>The same text for every type, shown once as plain text.</td></tr>
              <tr><td>Generic + <code>AN</code></td><td><code>AN</code> uses its own text, the other types use the generic one.</td></tr>
              <tr><td><code>AN</code> + <code>FC</code> only</td><td>Only those two types have a text.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="tab-pane fade" id="pdg-models" role="tabpanel">
        <p class="mb-2">The <strong>Product Models</strong> card groups products per model (e.g. <code>IFS</code> or
        <code>AIFS</code>). Models filter the products of the <a href="/do/monitoring/delaynotification">Delay Notification</a> page.</p>
        <ul class="ps-3 mb-0">
          <li>Enter a unique model name (case-insensitive) and tick its products in the drop-down, then save.</li>
          <li>Only products having a description can be selected.</li>
          <li>A product belongs to at most one model; ticking it elsewhere moves it.</li>
          <li>Deleting a model leaves its products without model.</li>
          <li>Deleting all the descriptions of a product also removes it from its model.</li>
        </ul>
      </div>

      <div class="tab-pane fade" id="pdg-maint" role="tabpanel">
        <ul class="ps-3 mb-0">
          <li><i class="bi bi-exclamation-triangle-fill text-warning"></i> &mdash; shown next to a product that does not currently exist in the <a href="/do/monitoring">monitoring interface</a> (typo, or product no longer active).</li>
          <li><strong>Unknown only</strong> &mdash; shows only such products. Use it with <em>Delete All Unknown</em> to clean up stale entries.</li>
          <li><strong>Import from Monitoring</strong> &mdash; adds a blank entry for every product/type pair seen in the monitoring interface and not yet configured here, ready to be filled in.</li>
          <li><strong>Create</strong> &mdash; adds an entry manually.</li>
        </ul>
      </div>

    </div>
  </div>
</div>
