<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- Test Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="${_guideId}-label" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="${_guideId}-label">
      <i class="bi bi-book me-2 text-info"></i>Test Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>The <strong>Test</strong> module is a <em>simulation module</em> used for benchmarking and
      testing transfer pipelines without sending data to any real remote host. All operations
      (connect, put, del, copy) are faked — no actual network connections are made. All options use
      the <code>test.</code> prefix.</div>
    </div>

    <div class="mb-3">
      <p class="small fw-semibold mb-1"><i class="bi bi-info-circle text-primary me-1"></i>How it works</p>
      <ul class="small mb-0 ps-3">
        <li class="mb-1"><strong>connect</strong> — resolves the local hostname and sets status. No TCP connection is made.</li>
        <li class="mb-1"><strong>put (stream)</strong> — writes bytes to a <code>NullOutputStream</code> throttled to <code>test.bytesPerSec</code>, simulating network throughput.</li>
        <li class="mb-1"><strong>put (OutputStream)</strong> — returns a throttled <code>NullOutputStream</code> directly to the caller.</li>
        <li class="mb-1"><strong>copy</strong> — sleeps for the time it would take to transfer <em>size</em> bytes at the configured rate.</li>
        <li class="mb-1"><strong>size</strong> — returns the number of bytes written to the null stream.</li>
        <li class="mb-0"><strong>get</strong> — not supported.</li>
      </ul>
    </div>

    <div class="mb-3">
      <p class="small fw-semibold mb-1"><i class="bi bi-sliders text-primary me-1"></i>Options</p>
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr>
            <td><code>test.bytesPerSec</code></td>
            <td><code>10MB</code></td>
            <td>Simulated transfer rate. Controls how quickly bytes are consumed by the null stream and how long a copy operation sleeps. Accepts byte-size notation, e.g. <code>50MB</code>, <code>1GB</code>.</td>
          </tr>
          <tr>
            <td><code>test.delay</code></td>
            <td><code>500ms</code></td>
            <td>Fixed delay injected before every operation (connect, del, put, copy, size, close). Accepts duration notation, e.g. <code>1s</code>, <code>200ms</code>.</td>
          </tr>
          <tr>
            <td><code>test.errorsFrequency</code></td>
            <td><code>1000</code></td>
            <td>Inject a simulated <code>IOException</code> once every N status-changing operations. Set to <code>0</code> to disable error injection entirely. Useful for testing retry and error-handling behaviour.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mb-0">
      <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Example</p>
      <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg);white-space:pre-wrap;"># Simulate a slow 1 MB/s link with a 1-second connect delay
test.bytesPerSec=1MB
test.delay=1s
test.errorsFrequency=0</pre>
      <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg);white-space:pre-wrap;"># Fast throughput test with occasional errors (1 in every 50 ops)
test.bytesPerSec=500MB
test.delay=0ms
test.errorsFrequency=50</pre>
    </div>

  </div>
</div>
