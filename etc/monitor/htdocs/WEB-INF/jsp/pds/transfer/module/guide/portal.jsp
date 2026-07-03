<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- Portal Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="${_guideId}-label" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="${_guideId}-label">
      <i class="bi bi-book me-2 text-info"></i>Portal Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>The <strong>Portal</strong> module is a <em>no-operation transfer module</em>. Instead of
      sending a file to a remote host, it consumes the data stream locally and marks the file as
      available for retrieval through the <strong>Data Portal</strong>. No connection is made to any
      external system.</div>
    </div>

    <div class="mb-3">
      <p class="small fw-semibold mb-1"><i class="bi bi-info-circle text-primary me-1"></i>How it works</p>
      <ol class="small mb-0 ps-3">
        <li class="mb-1"><code>connect()</code> immediately marks the module as available — no network call is made.</li>
        <li class="mb-1"><code>put()</code> reads and discards all bytes from the input stream, recording the byte count. This ensures the file is staged and available in the Data Portal for authorised users to pull.</li>
        <li class="mb-1"><code>size()</code> returns the number of bytes consumed, satisfying the transfer framework's post-upload size check.</li>
        <li class="mb-0"><code>del()</code> and <code>get()</code> are not supported and will throw an error if called.</li>
      </ol>
    </div>

    <div class="mb-3">
      <p class="small fw-semibold mb-1"><i class="bi bi-sliders text-primary me-1"></i>Configuration options</p>
      <div class="alert alert-secondary py-2 px-3 small d-flex align-items-start gap-2 mb-0">
        <i class="bi bi-dash-circle flex-shrink-0 mt-1"></i>
        <div>This module has <strong>no configurable options</strong>. All behaviour is determined by
        the host record (type, directory, credentials) and the Data Portal configuration on the
        MoverServer. There are no <code>portal.*</code> properties to set.</div>
      </div>
    </div>

    <div class="mb-0">
      <p class="small fw-semibold mb-1"><i class="bi bi-lightbulb text-warning me-1"></i>Typical use</p>
      <p class="small text-muted mb-0">Assign this module to a <strong>Dissemination</strong> host
      when the intention is not to push data to a remote server but to make it available for
      authorised users to pull via the integrated Data Portal interface. The transfer completes
      successfully as soon as all bytes have been consumed — no remote connection is required.</p>
    </div>

  </div>
</div>
