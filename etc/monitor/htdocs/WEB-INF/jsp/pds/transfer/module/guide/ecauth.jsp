<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- ECauth Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="${_guideId}-label" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="${_guideId}-label">
      <i class="bi bi-book me-2 text-info"></i>ECauth Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>The <strong>ECauth</strong> module transfers files over an interactive <strong>SSH</strong>
      or <strong>Telnet</strong> session authenticated through ECMWF's ECauth service. All options use
      the <code>ecauth.</code> prefix. Credentials default to the ECauth service account but can be
      overridden with <code>ecauth.user</code> / <code>ecauth.pass</code>.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="ecauthGuideTab" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#ecauth-connection" type="button"><i class="bi bi-plug me-1"></i>Connection</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ecauth-ssh" type="button"><i class="bi bi-shield-lock me-1"></i>SSH / Auth</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ecauth-transfer" type="button"><i class="bi bi-arrow-up-down me-1"></i>Transfer</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ecauth-advanced" type="button"><i class="bi bi-gear me-1"></i>Advanced</button></li>
    </ul>

    <div class="tab-content">

      <%-- CONNECTION --%>
      <div class="tab-pane fade show active" id="ecauth-connection" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Basic connection</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ecauth.protocol</code></td><td><code>ssh</code></td><td>Transport protocol: <code>ssh</code> (port 22) or <code>telnet</code> (port 23)</td></tr>
              <tr><td><code>ecauth.port</code></td><td><code>22</code> / <code>23</code></td><td>Remote port. Defaults to 22 for SSH, 23 for Telnet. Override here if non-standard.</td></tr>
              <tr><td><code>ecauth.user</code></td><td><em>from config</em></td><td>ECauth service username used to open the interactive session</td></tr>
              <tr><td><code>ecauth.pass</code></td><td><em>from config</em></td><td>ECauth service password</td></tr>
              <tr><td><code>ecauth.hostList</code></td><td><em>none</em></td><td>Comma-separated list of target hostnames. The module picks one at runtime (optionally with load balancing).</td></tr>
              <tr><td><code>ecauth.resolveIP</code></td><td><code>true</code></td><td>Resolve hostnames to IP addresses before connecting</td></tr>
              <tr><td><code>ecauth.proxyList</code></td><td><em>none</em></td><td>Comma-separated list of SOCKS/proxy addresses to route through</td></tr>
              <tr><td><code>ecauth.listenAddress</code></td><td><em>none</em></td><td>Local IP address to bind when opening data connections</td></tr>
              <tr><td><code>ecauth.cwd</code></td><td><em>from host</em></td><td>Initial working directory after login. Defaults to the user's home directory from the ECauth session.</td></tr>
              <tr><td><code>ecauth.connectTimeOut</code></td><td><code>30s</code></td><td>TCP connection timeout</td></tr>
              <tr><td><code>ecauth.sessionTimeOut</code></td><td><em>none</em></td><td>Maximum lifetime of an interactive session before it is recycled</td></tr>
              <tr><td><code>ecauth.keepAlive</code></td><td><code>0</code></td><td>Send keep-alive command every N ms (0 = disabled)</td></tr>
              <tr><td><code>ecauth.useNoop</code></td><td><code>0</code></td><td>Send NOOP every N ms to keep the session alive (0 = disabled)</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- SSH / AUTH --%>
      <div class="tab-pane fade" id="ecauth-ssh" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-key text-primary me-1"></i>SSH authentication</p>
          <p class="small text-muted mb-1">Only relevant when <code>ecauth.protocol=ssh</code>.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ecauth.privateKeyFile</code></td><td><em>none</em></td><td>Path to a PEM private key file on the DataMover for public-key authentication</td></tr>
              <tr><td><code>ecauth.privateKey</code></td><td><em>none</em></td><td>Inline PEM private key (alternative to <code>privateKeyFile</code>)</td></tr>
              <tr><td><code>ecauth.passPhrase</code></td><td><em>none</em></td><td>Passphrase for the private key if it is encrypted</td></tr>
              <tr><td><code>ecauth.fingerPrint</code></td><td><em>none</em></td><td>Expected SSH host key fingerprint. Connection is refused if the server fingerprint does not match.</td></tr>
              <tr><td><code>ecauth.cipher</code></td><td><code>none</code></td><td>SSH cipher suite to negotiate (e.g. <code>aes128-ctr</code>). <code>none</code> lets JSch negotiate automatically.</td></tr>
              <tr><td><code>ecauth.compression</code></td><td><code>none</code></td><td>SSH compression algorithm (e.g. <code>zlib</code>). <code>none</code> disables compression.</td></tr>
              <tr><td><code>ecauth.serverAliveInterval</code></td><td><em>none</em></td><td>SSH server-alive message interval (e.g. <code>60s</code>). Prevents idle disconnection.</td></tr>
              <tr><td><code>ecauth.serverAliveCountMax</code></td><td><em>none</em></td><td>Number of unanswered server-alive messages before the session is considered dead</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- TRANSFER --%>
      <div class="tab-pane fade" id="ecauth-transfer" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-up-down text-primary me-1"></i>File transfer behaviour</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ecauth.usetmp</code></td><td><code>false</code></td><td>Upload to a temporary name (with suffix) then rename on completion</td></tr>
              <tr><td><code>ecauth.mkdirs</code></td><td><code>false</code></td><td>Create remote directory hierarchy if it does not exist</td></tr>
              <tr><td><code>ecauth.prefix</code></td><td><em>none</em></td><td>Prefix added to the remote filename</td></tr>
              <tr><td><code>ecauth.suffix</code></td><td><code>.tmp</code></td><td>Suffix used during upload when <code>usetmp=true</code> (default is <code>.tmp</code> when no explicit prefix/suffix is set)</td></tr>
              <tr><td><code>ecauth.mksuffix</code></td><td><code>false</code></td><td>Generate a random 3-character suffix instead of using <code>ecauth.suffix</code></td></tr>
              <tr><td><code>ecauth.ignoreCheck</code></td><td><code>true</code></td><td>Skip the post-upload file size verification</td></tr>
              <tr><td><code>ecauth.usemget</code></td><td><code>false</code></td><td>Use <code>mget</code> for bulk retrieval operations instead of individual <code>get</code> calls</td></tr>
              <tr><td><code>ecauth.listOptions</code></td><td><em>none</em></td><td>Extra options appended to the remote <code>ls</code> / directory listing command</td></tr>
              <tr><td><code>ecauth.copyCmd</code></td><td><em>none</em></td><td>Custom shell command template for server-side copy operations</td></tr>
              <tr><td><code>ecauth.chmodOnCopy</code></td><td><code>640</code></td><td>Octal permission mode applied to files after a server-side copy</td></tr>
            </tbody>
          </table>
        </div>
        <div class="mb-0">
          <p class="small fw-semibold mb-1"><i class="bi bi-terminal text-primary me-1"></i>Exec hook</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ecauth.execCmd</code></td><td><em>none</em></td><td>Shell command executed on the remote host after each transfer</td></tr>
              <tr><td><code>ecauth.execCode</code></td><td><code>0</code></td><td>Expected exit code of <code>execCmd</code>. Any other exit code is treated as an error.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- ADVANCED --%>
      <div class="tab-pane fade" id="ecauth-advanced" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder text-primary me-1"></i>Directory creation hooks</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ecauth.mkdirsCmdIndex</code></td><td><code>0</code></td><td>Path component depth at which to start issuing mkdir commands (0 = from root)</td></tr>
              <tr><td><code>ecauth.preMkdirsCmd</code></td><td><em>none</em></td><td>Shell command(s) executed before creating directories</td></tr>
              <tr><td><code>ecauth.postMkdirsCmd</code></td><td><em>none</em></td><td>Shell command(s) executed after creating directories</td></tr>
              <tr><td><code>ecauth.ignoreMkdirsCmdErrors</code></td><td><code>false</code></td><td>Continue even if pre/post mkdir commands return a non-zero exit code</td></tr>
            </tbody>
          </table>
        </div>
        <div class="mb-0">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Example</p>
          <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg);white-space:pre-wrap;">ecauth.protocol=ssh
ecauth.port=22
ecauth.usetmp=true
ecauth.mkdirs=true
ecauth.ignoreCheck=false
ecauth.serverAliveInterval=60s
ecauth.serverAliveCountMax=3
ecauth.chmodOnCopy=644</pre>
        </div>
      </div>

    </div>
  </div>
</div>
