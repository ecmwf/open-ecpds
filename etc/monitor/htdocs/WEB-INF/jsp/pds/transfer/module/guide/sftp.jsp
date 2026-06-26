<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- SFTP Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:min(720px,68vw);">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>SFTP Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>sftp.</code> prefix. The host <em>Login</em> and
      <em>Password</em> fields provide default credentials but can be overridden.
      Key-based authentication is also supported via <code>sftp.privateKey</code> or
      <code>sftp.privateKeyFile</code>.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="sftpGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#sftpgt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#sftpgt-auth" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>Auth &amp; Keys
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#sftpgt-transfer" type="button" role="tab">
          <i class="bi bi-arrow-up-down me-1"></i>Transfer
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#sftpgt-dir" type="button" role="tab">
          <i class="bi bi-folder me-1"></i>Directory &amp; Listing
        </button>
      </li>
    </ul>

    <div class="tab-content" id="sftpGuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="sftpgt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Basic connection</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.port</code></td><td><code>22</code></td><td>Remote SSH/SFTP port</td></tr>
              <tr><td><code>sftp.login</code></td><td><em>from host</em></td><td>SSH username</td></tr>
              <tr><td><code>sftp.password</code></td><td><em>from host</em></td><td>SSH password (used if no private key is set)</td></tr>
              <tr><td><code>sftp.listenAddress</code></td><td><em>none</em></td><td>Local IP address to bind for outgoing connections</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-clock text-primary me-1"></i>Timeouts &amp; keep-alive</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.sessionTimeOut</code></td><td><code>1m</code></td><td>SSH session idle timeout</td></tr>
              <tr><td><code>sftp.connectTimeOut</code></td><td><code>30s</code></td><td>TCP connection timeout</td></tr>
              <tr><td><code>sftp.serverAliveInterval</code></td><td><em>none</em></td><td>Interval for SSH keep-alive packets (e.g. <code>30s</code>)</td></tr>
              <tr><td><code>sftp.serverAliveCountMax</code></td><td><code>0</code></td><td>Max unanswered keep-alive packets before disconnecting</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-shield text-primary me-1"></i>Crypto &amp; algorithm negotiation</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.kex</code></td><td><code>default</code></td><td>Key-exchange algorithms (comma-separated JSch names)</td></tr>
              <tr><td><code>sftp.cipher</code></td><td><code>none</code></td><td>Cipher algorithms (e.g. <code>aes128-ctr,aes256-ctr</code>)</td></tr>
              <tr><td><code>sftp.mac</code></td><td><em>JSch default</em></td><td>MAC algorithms (e.g. <code>hmac-sha2-256</code>)</td></tr>
              <tr><td><code>sftp.compression</code></td><td><code>none</code></td><td>Compression: <code>none</code> or <code>zlib@openssh.com</code></td></tr>
              <tr><td><code>sftp.serverHostKey</code></td><td><em>JSch default</em></td><td>Accepted server host key types (e.g. <code>ssh-ed25519</code>)</td></tr>
              <tr><td><code>sftp.clientVersion</code></td><td><em>JSch default</em></td><td>Custom SSH client version string</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-gear text-primary me-1"></i>JSch properties &amp; options</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.options</code></td><td>Raw JSch option string (key=value pairs, semicolon-separated)</td></tr>
              <tr><td><code>sftp.properties</code></td><td>Additional JSch properties as key=value pairs</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- password auth with keep-alive --%>
sftp.port = "22"
sftp.sessionTimeOut = "5m"
sftp.serverAliveInterval = "30s"
sftp.serverAliveCountMax = "3"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- force modern cipher suite --%>
sftp.kex = "ecdh-sha2-nistp521,ecdh-sha2-nistp384"
sftp.cipher = "aes256-ctr"
sftp.mac = "hmac-sha2-256"</pre>
        </div>

      </div><%-- /sftpgt-connection --%>

      <%-- ================================================================
           TAB 2: AUTH & KEYS
           ================================================================ --%>
      <div class="tab-pane fade" id="sftpgt-auth" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-key text-primary me-1"></i>Key-based authentication</p>
          <p class="small text-muted mb-1">Provide either a file path or the inline key. Inline keys take precedence
          over <code>sftp.privateKeyFile</code>. The key must be in PEM (OpenSSH or RSA) format.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.privateKey</code></td><td><em>empty</em></td><td>Inline PEM private key (paste the full key block)</td></tr>
              <tr><td><code>sftp.privateKeyFile</code></td><td><em>empty</em></td><td>Path to PEM private key file on the ECpds host</td></tr>
              <tr><td><code>sftp.passPhrase</code></td><td><em>none</em></td><td>Passphrase for the private key (if encrypted)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-fingerprint text-primary me-1"></i>Host key verification</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.fingerPrint</code></td><td><em>none</em></td>
                  <td>Expected server fingerprint (SHA-256 or MD5 hex). If set, the connection fails
                  unless the server key matches.</td></tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>If <code>sftp.fingerPrint</code> is not set, host key checking depends on the
            JSch <code>StrictHostKeyChecking</code> property. Default is <code>no</code> (accept any host key).
            Set it via <code>sftp.options</code> for production environments.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-list-check text-primary me-1"></i>Authentication methods</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.preferredAuthentications</code></td><td><em>JSch default</em></td>
                  <td>Ordered list of auth methods (e.g. <code>publickey,password</code>)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- key file auth --%>
sftp.privateKeyFile = "/opt/ecpds/.ssh/id_ed25519"
sftp.preferredAuthentications = "publickey"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- inline key with fingerprint verification --%>
sftp.privateKey = "-----BEGIN OPENSSH PRIVATE KEY-----
b3Blb...
-----END OPENSSH PRIVATE KEY-----"
sftp.fingerPrint = "SHA256:abc123..."</pre>
        </div>

      </div><%-- /sftpgt-auth --%>

      <%-- ================================================================
           TAB 3: TRANSFER
           ================================================================ --%>
      <div class="tab-pane fade" id="sftpgt-transfer" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-file-earmark text-primary me-1"></i>Temporary &amp; suffix handling</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.usetmp</code></td><td><code>true</code></td><td>Upload to a <code>.tmp</code> file and rename on completion</td></tr>
              <tr><td><code>sftp.prefix</code></td><td><em>empty</em></td><td>Prefix to prepend to the remote filename during upload</td></tr>
              <tr><td><code>sftp.suffix</code></td><td><em>empty</em></td><td>Suffix to append to the remote filename during upload</td></tr>
              <tr><td><code>sftp.mksuffix</code></td><td><code>false</code></td><td>Generate a unique suffix automatically</td></tr>
              <tr><td><code>sftp.chmod</code></td><td><em>none</em></td><td>Octal permission mask to apply after upload (e.g. <code>644</code>)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-speedometer2 text-primary me-1"></i>Performance &amp; integrity</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.bulkRequestNumber</code></td><td><code>64</code></td><td>SFTP read-ahead / in-flight request count (higher = faster on high-latency links)</td></tr>
              <tr><td><code>sftp.useWriteFlush</code></td><td><code>false</code></td><td>Apply write-flush workaround for some server implementations</td></tr>
              <tr><td><code>sftp.ignoreCheck</code></td><td><code>false</code></td><td>Skip post-transfer size verification</td></tr>
              <tr><td><code>sftp.md5Ext</code></td><td><em>none</em></td><td>If set, write an MD5 sidecar file with this extension</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-terminal text-primary me-1"></i>Post-upload command</p>
          <p class="small text-muted mb-1">Execute a remote shell command after the upload completes.
          The command is run via the SSH channel.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.execCmd</code></td><td><em>none</em></td><td>Remote shell command to execute after a successful upload</td></tr>
              <tr><td><code>sftp.execCode</code></td><td><code>0</code></td><td>Expected exit code; non-matching exits cause the transfer to fail</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- safe upload with permissions --%>
sftp.usetmp = "yes"
sftp.chmod = "644"
sftp.bulkRequestNumber = "128"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- run notification command after upload --%>
sftp.execCmd = "/opt/scripts/notify.sh {filename}"
sftp.execCode = "0"</pre>
        </div>

      </div><%-- /sftpgt-transfer --%>

      <%-- ================================================================
           TAB 4: DIRECTORY & LISTING
           ================================================================ --%>
      <div class="tab-pane fade" id="sftpgt-dir" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder2-open text-primary me-1"></i>Working directory</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.cwd</code></td><td><em>from host path</em></td><td>Remote working directory to change to after login</td></tr>
              <tr><td><code>sftp.usecleanpath</code></td><td><code>false</code></td><td>Normalise remote paths (remove double slashes, etc.)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-primary me-1"></i>Directory creation (mkdirs)</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.mkdirs</code></td><td><code>true</code></td><td>Automatically create parent directories on the remote host</td></tr>
              <tr><td><code>sftp.mkdirsCmdIndex</code></td><td><code>0</code></td><td>Path component depth at which to start creating directories</td></tr>
              <tr><td><code>sftp.ignoreMkdirsCmdErrors</code></td><td><code>false</code></td><td>Ignore mkdir errors (useful when directory may already exist)</td></tr>
              <tr><td><code>sftp.preMkdirsCmd</code></td><td><em>none</em></td><td>Shell command to run before creating directories</td></tr>
              <tr><td><code>sftp.postMkdirsCmd</code></td><td><em>none</em></td><td>Shell command to run after creating directories</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-list-ul text-primary me-1"></i>Listing / acquisition</p>
          <p class="small text-muted mb-1">These options control how the SFTP module traverses remote directories
          during an acquisition (listing) run.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>sftp.listRecursive</code></td><td><code>true</code></td><td>Recursively list subdirectories</td></tr>
              <tr><td><code>sftp.listMaxThreads</code></td><td><code>10</code></td><td>Maximum concurrent threads for parallel directory listing</td></tr>
              <tr><td><code>sftp.listMaxWaiting</code></td><td><code>100</code></td><td>Maximum number of pending listing tasks in the queue</td></tr>
              <tr><td><code>sftp.listMaxDirs</code></td><td><code>50000</code></td><td>Abort listing if more than this many directories are encountered</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- fast parallel listing --%>
sftp.listRecursive = "yes"
sftp.listMaxThreads = "20"
sftp.listMaxWaiting = "500"
sftp.listMaxDirs = "100000"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- auto-create remote directories --%>
sftp.mkdirs = "yes"
sftp.ignoreMkdirsCmdErrors = "yes"
sftp.cwd = "/data/incoming"</pre>
        </div>

      </div><%-- /sftpgt-dir --%>

    </div><%-- tab-content --%>
  </div><%-- offcanvas-body --%>
</div><%-- offcanvas --%>
