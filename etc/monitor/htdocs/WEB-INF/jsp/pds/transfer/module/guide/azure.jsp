<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- Azure Blob Storage Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Azure Blob Storage Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>azure.</code> prefix. Three authentication modes are supported:
      <strong>Shared Key</strong> (host Login/Password = account name/key),
      <strong>SAS Token</strong> (<code>azure.sasUrl</code> + <code>azure.sasSubscriptionKey</code>), and
      <strong>Managed Identity</strong> (<code>azure.userAssignedClientId</code>).</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="azureGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#azgt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#azgt-auth" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>Authentication
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#azgt-container" type="button" role="tab">
          <i class="bi bi-inbox me-1"></i>Container
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#azgt-transfer" type="button" role="tab">
          <i class="bi bi-arrow-up-down me-1"></i>Transfer
        </button>
      </li>
    </ul>

    <div class="tab-content" id="azureGuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="azgt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-cloud text-primary me-1"></i>Endpoint URL</p>
          <p class="small text-muted mb-1">By default the module constructs the endpoint as
          <code>{scheme}://{host}:{port}</code> using the host record. Use <code>azure.url</code>
          to override with a full URL (e.g. Azurite emulator or sovereign cloud endpoint).</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.url</code></td><td><em>scheme://host:port</em></td>
                  <td>Full endpoint URL override (e.g. <code>http://127.0.0.1:10000/devstoreaccount1</code>
                  for Azurite)</td></tr>
              <tr><td><code>azure.scheme</code></td><td><code>https</code></td>
                  <td>URL scheme (<code>https</code> for production, <code>http</code> for local emulator)</td></tr>
              <tr><td><code>azure.port</code></td><td><code>443</code></td>
                  <td>Port when constructing the endpoint URL from host + scheme</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- standard Azure (production) --%>
azure.scheme = "https"
azure.port = "443"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- Azurite local emulator --%>
azure.url = "http://127.0.0.1:10000/devstoreaccount1"
azure.scheme = "http"</pre>
        </div>

      </div><%-- /azgt-connection --%>

      <%-- ================================================================
           TAB 2: AUTHENTICATION
           ================================================================ --%>
      <div class="tab-pane fade" id="azgt-auth" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-key text-primary me-1"></i>Mode 1 &mdash; Shared Key (Storage Account Key)</p>
          <p class="small text-muted mb-1">The simplest method. Set the host <em>Login</em> to the storage
          account name and <em>Password</em> to the account key (base64-encoded). This is the default
          when no SAS URL or Managed Identity is configured.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Field</th><th>Value</th></tr></thead>
            <tbody>
              <tr><td>Host <em>Login</em></td><td>Storage account name (e.g. <code>myaccount</code>)</td></tr>
              <tr><td>Host <em>Password</em></td><td>Storage account key (base64 string from Azure portal)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-ticket-perforated text-primary me-1"></i>Mode 2 &mdash; SAS Token (via API gateway)</p>
          <p class="small text-muted mb-1">Obtain a Shared Access Signature token from a REST endpoint
          on each connection. Useful when tokens are managed centrally or expire frequently.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.sasUrl</code></td><td><em>empty</em></td>
                  <td>URL of the SAS token endpoint (GET request returns the token)</td></tr>
              <tr><td><code>azure.sasSubscriptionKey</code></td><td><em>empty</em></td>
                  <td>Value sent as the <code>Ocp-Apim-Subscription-Key</code> header on the SAS token request</td></tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>Both <code>azure.sasUrl</code> and <code>azure.sasSubscriptionKey</code> must be
            set together. If either is empty, the module falls through to Shared Key or Managed Identity.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person-badge text-primary me-1"></i>Mode 3 &mdash; Managed Identity</p>
          <p class="small text-muted mb-1">Use an Azure user-assigned managed identity. No passwords stored
          in the host record. Requires the ECpds host to run in Azure (VM, AKS, App Service, etc.) and
          have the identity assigned.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.userAssignedClientId</code></td><td><em>empty</em></td>
                  <td>Client ID of the user-assigned managed identity. When set, Managed Identity
                  authentication is used (Shared Key and SAS are ignored).</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-diagram-3 text-primary me-1"></i>Auth priority</p>
          <div class="alert alert-light border py-2 px-3 small">
            <ol class="mb-0 ps-3">
              <li>If <strong>Login + Password</strong> (account name + key) are both set &rarr; <strong>Shared Key</strong></li>
              <li>Else if <strong>sasUrl + sasSubscriptionKey</strong> are both set &rarr; <strong>SAS Token</strong></li>
              <li>Else if <strong>userAssignedClientId</strong> is set &rarr; <strong>Managed Identity</strong></li>
            </ol>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- SAS token via API management gateway --%>
azure.sasUrl = "https://apim.example.com/storage/sas"
azure.sasSubscriptionKey = "abc123key"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- Managed Identity on an Azure VM --%>
azure.userAssignedClientId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"</pre>
        </div>

      </div><%-- /azgt-auth --%>

      <%-- ================================================================
           TAB 3: CONTAINER
           ================================================================ --%>
      <div class="tab-pane fade" id="azgt-container" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-inbox text-primary me-1"></i>Container selection</p>
          <p class="small text-muted mb-1">The container name can be embedded in the URL path
          (<code>user:pass@host/containerName</code>) or set explicitly. If both are present,
          the option value takes precedence.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.containerName</code></td><td><em>from URL path</em></td>
                  <td>Fixed container name for all transfers. When set, all blobs are placed in this container.</td></tr>
              <tr><td><code>azure.mkContainer</code></td><td><code>false</code></td>
                  <td>Automatically create the container if it does not exist</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person-fill text-primary me-1"></i>Listing ownership</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.ftpuser</code></td><td><em>storage account name</em></td>
                  <td>Owner user name shown in generated directory listing entries</td></tr>
              <tr><td><code>azure.ftpgroup</code></td><td><em>storage account name</em></td>
                  <td>Owner group name shown in generated directory listing entries</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start example</p>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- fixed container, auto-create if missing --%>
azure.containerName = "ecpds-data"
azure.mkContainer = "yes"</pre>
        </div>

      </div><%-- /azgt-container --%>

      <%-- ================================================================
           TAB 4: TRANSFER
           ================================================================ --%>
      <div class="tab-pane fade" id="azgt-transfer" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-up-circle text-primary me-1"></i>Upload strategy</p>
          <p class="small text-muted mb-1">Small files (below <code>azure.multipartSize</code>) are uploaded
          in a single PUT. Larger files use block-blob parallel upload with configurable block size and
          concurrency.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.multipartSize</code></td><td><code>256m</code></td>
                  <td>Files at or above this size use parallel block upload; smaller files use a single PUT</td></tr>
              <tr><td><code>azure.blockSize</code></td><td><code>10k</code></td>
                  <td>Size of each block in a parallel block-blob upload (e.g. <code>4m</code>, <code>100m</code>)</td></tr>
              <tr><td><code>azure.numBuffers</code></td><td><code>5</code></td>
                  <td>Maximum number of concurrent block upload buffers (parallelism)</td></tr>
              <tr><td><code>azure.chunkSize</code></td><td><code>0</code> (disabled)</td>
                  <td>Flux buffer chunk size when reading the input stream (0 = unbuffered).
                  Rarely needs changing.</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-check2-circle text-primary me-1"></i>Integrity &amp; overwrite</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>azure.overwrite</code></td><td><code>true</code></td>
                  <td>Overwrite existing blobs. Set to <code>false</code> to fail if the blob already exists.</td></tr>
              <tr><td><code>azure.ignoreDelete</code></td><td><code>true</code></td>
                  <td>Ignore errors when deleting a blob before overwrite. Set to <code>false</code> to
                  fail if the pre-delete step errors.</td></tr>
              <tr><td><code>azure.ignoreCheck</code></td><td><code>true</code></td>
                  <td>Skip post-transfer size/ETag verification. Set to <code>false</code> to verify
                  blob properties after upload.</td></tr>
              <tr><td><code>azure.useMD5</code></td><td><code>false</code></td>
                  <td>Compute and send a Content-MD5 header on uploads for server-side integrity checking</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- large-file parallel upload (4 MB blocks, 10 concurrent) --%>
azure.multipartSize = "64m"
azure.blockSize = "4m"
azure.numBuffers = "10"
azure.overwrite = "yes"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- strict integrity checking --%>
azure.ignoreDelete = "no"
azure.ignoreCheck = "no"
azure.useMD5 = "yes"</pre>
        </div>

      </div><%-- /azgt-transfer --%>

    </div><%-- tab-content --%>
  </div><%-- offcanvas-body --%>
</div><%-- offcanvas --%>
