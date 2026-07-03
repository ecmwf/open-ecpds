<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- GCS Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Google Cloud Storage Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>gcs.</code> prefix. Authentication uses a GCS
      <strong>Service Account</strong>. The host <em>Login</em> maps to
      <code>clientId</code> and <em>Password</em> maps to <code>privateKeyId</code>;
      both can be set explicitly via options instead.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="gcsGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#gcsgt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#gcsgt-auth" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>Service Account
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#gcsgt-bucket" type="button" role="tab">
          <i class="bi bi-bucket me-1"></i>Bucket
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#gcsgt-transfer" type="button" role="tab">
          <i class="bi bi-arrow-up-down me-1"></i>Transfer
        </button>
      </li>
    </ul>

    <div class="tab-content" id="gcsGuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="gcsgt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-cloud text-primary me-1"></i>Endpoint &amp; URL</p>
          <p class="small text-muted mb-1">By default the module connects to <code>storage.googleapis.com</code>.
          Use <code>gcs.url</code> to point to a GCS-compatible emulator or private endpoint.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.url</code></td><td><em>scheme://host:port</em></td>
                  <td>Full base URL override (e.g. <code>http://localhost:4443</code> for the emulator)</td></tr>
              <tr><td><code>gcs.scheme</code></td><td><code>http</code></td><td>URL scheme when constructing the endpoint URL from host + port</td></tr>
              <tr><td><code>gcs.port</code></td><td><code>443</code></td><td>Port when constructing the endpoint URL</td></tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>For standard GCS usage, leave <code>gcs.url</code> unset. The SDK routes
            to <code>storage.googleapis.com</code> automatically using the project credentials.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-lock text-primary me-1"></i>TLS / SSL</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.protocol</code></td><td><code>TLS</code></td><td>SSL/TLS protocol version (e.g. <code>TLSv1.2</code>, <code>TLSv1.3</code>)</td></tr>
              <tr><td><code>gcs.sslValidation</code></td><td><code>false</code></td>
                  <td>Validate the server TLS certificate. Set to <code>true</code> for production;
                  disable only for local emulators with self-signed certificates.</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- standard GCS (production) --%>
gcs.sslValidation = "yes"
gcs.protocol = "TLSv1.3"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- local emulator (fake-gcs-server) --%>
gcs.url = "http://localhost:4443"
gcs.scheme = "http"
gcs.port = "4443"
gcs.sslValidation = "no"</pre>
        </div>

      </div><%-- /gcsgt-connection --%>

      <%-- ================================================================
           TAB 2: SERVICE ACCOUNT AUTH
           ================================================================ --%>
      <div class="tab-pane fade" id="gcsgt-auth" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person-badge text-primary me-1"></i>Service Account credentials</p>
          <p class="small text-muted mb-1">GCS authentication requires a Service Account JSON key.
          Instead of uploading the JSON file, extract the individual fields and set them as options:</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>JSON field</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.projectId</code></td><td><code>project_id</code></td><td>GCP project ID</td></tr>
              <tr><td><code>gcs.clientId</code></td><td><code>client_id</code></td>
                  <td>Service account client ID (or set host <em>Login</em>)</td></tr>
              <tr><td><code>gcs.clientEmail</code></td><td><code>client_email</code></td><td>Service account email address</td></tr>
              <tr><td><code>gcs.privateKeyId</code></td><td><code>private_key_id</code></td>
                  <td>Private key ID (or set host <em>Password</em>)</td></tr>
              <tr><td><code>gcs.privateKey</code></td><td><code>private_key</code></td>
                  <td>RSA private key in PKCS8 PEM format (paste the full key block)</td></tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>The <code>gcs.privateKey</code> value contains the full PEM block including
            newlines. Paste it exactly as it appears in the service account JSON file, replacing
            literal <code>\n</code> sequences with actual newlines.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start example</p>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- service account config --%>
gcs.projectId = "my-gcp-project"
gcs.clientEmail = "ecpds@my-gcp-project.iam.gserviceaccount.com"
gcs.clientId = "123456789012345678901"
gcs.privateKeyId = "abcdef1234567890"
gcs.privateKey = "-----BEGIN RSA PRIVATE KEY-----
MIIEo...
-----END RSA PRIVATE KEY-----"</pre>
        </div>

      </div><%-- /gcsgt-auth --%>

      <%-- ================================================================
           TAB 3: BUCKET
           ================================================================ --%>
      <div class="tab-pane fade" id="gcsgt-bucket" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-bucket text-primary me-1"></i>Bucket selection</p>
          <p class="small text-muted mb-1">The bucket name can be embedded in the URL path
          (<code>user:pass@host/bucketName</code>) or set explicitly here.
          Explicit options override the URL path value.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.bucketName</code></td><td><em>from URL path</em></td>
                  <td>Fixed bucket name for all transfers. When set, all objects are placed in this bucket.</td></tr>
              <tr><td><code>gcs.prefix</code></td><td><em>empty</em></td>
                  <td>Path prefix prepended to every object key inside the bucket (e.g. <code>data/incoming/</code>)</td></tr>
              <tr><td><code>gcs.allowEmptyBucketName</code></td><td><code>false</code></td>
                  <td>Allow operations without a bucket name; the first path segment is used as the object name</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-primary me-1"></i>Bucket creation</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.mkBucket</code></td><td><code>false</code></td>
                  <td>Automatically create the bucket if it does not exist on first connection</td></tr>
              <tr><td><code>gcs.bucketLocation</code></td><td><em>empty</em></td>
                  <td>GCS region / location for auto-created buckets (e.g. <code>EU</code>, <code>us-central1</code>)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person-fill text-primary me-1"></i>Listing ownership</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.ftpuser</code></td><td><em>from service account email</em></td>
                  <td>Owner user name shown in generated directory listing entries</td></tr>
              <tr><td><code>gcs.ftpgroup</code></td><td><em>from service account email</em></td>
                  <td>Owner group name shown in generated directory listing entries</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- fixed bucket with prefix --%>
gcs.bucketName = "my-data-bucket"
gcs.prefix = "ecpds/incoming/"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- auto-create bucket in EU region --%>
gcs.bucketName = "auto-created-bucket"
gcs.mkBucket = "yes"
gcs.bucketLocation = "EU"</pre>
        </div>

      </div><%-- /gcsgt-bucket --%>

      <%-- ================================================================
           TAB 4: TRANSFER
           ================================================================ --%>
      <div class="tab-pane fade" id="gcsgt-transfer" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-speedometer2 text-primary me-1"></i>Upload chunk size</p>
          <p class="small text-muted mb-1">GCS uploads use a resumable upload protocol. The
          <code>gcs.chunkSize</code> controls how much data is buffered and sent per HTTP request.
          It <strong>must</strong> be a multiple of 256&nbsp;KiB. Larger chunks reduce request
          overhead but use more memory.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>gcs.chunkSize</code></td><td><em>SDK default (~15 MB)</em></td>
                  <td>Upload chunk size as a byte size value (e.g. <code>8m</code>, <code>32m</code>).
                  Must be a multiple of <code>256k</code>. Values below <code>256k</code> are rejected.</td></tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>If the provided <code>gcs.chunkSize</code> is not a multiple of 256&nbsp;KiB,
            it is automatically rounded down. A value below 256&nbsp;KiB is rejected with an error.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- 32 MB chunks for large files --%>
gcs.chunkSize = "32m"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- streaming mode (no chunk buffering) - omit chunkSize --%>
gcs.bucketName = "streaming-bucket"
gcs.prefix = "live/"</pre>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-info-circle text-primary me-1"></i>Object naming</p>
          <p class="small text-muted mb-0">GCS object names are derived from the remote file path.
          If <code>gcs.bucketName</code> is set, the object name is
          <code>{prefix}{filename}</code>. Otherwise, the first path component is used as the
          bucket name and the remainder as the object key.</p>
        </div>

      </div><%-- /gcsgt-transfer --%>

    </div><%-- tab-content --%>
  </div><%-- offcanvas-body --%>
</div><%-- offcanvas --%>
