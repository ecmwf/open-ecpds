<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- FTPS Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="${_guideId}-label" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="${_guideId}-label">
      <i class="bi bi-book me-2 text-info"></i>FTPS Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>ftps.</code> prefix. The module supports plain <strong>FTP</strong>,
      implicit <strong>FTPS</strong> (TLS from the start) and explicit <strong>FTPES</strong> (STARTTLS).
      Choose the mode with <code>ftps.connectionType</code>. Host <em>Login</em> and <em>Password</em>
      provide the default credentials but can be overridden with <code>ftps.login</code> / <code>ftps.password</code>.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="ftpsGuideTab" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#ftpsgt-connection" type="button"><i class="bi bi-plug me-1"></i>Connection</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ftpsgt-tls" type="button"><i class="bi bi-shield-lock me-1"></i>TLS / Security</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ftpsgt-transfer" type="button"><i class="bi bi-arrow-up-down me-1"></i>Transfer</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ftpsgt-dir" type="button"><i class="bi bi-folder me-1"></i>Paths</button></li>
      <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#ftpsgt-hooks" type="button"><i class="bi bi-terminal me-1"></i>Hooks</button></li>
    </ul>

    <div class="tab-content">

      <%-- CONNECTION --%>
      <div class="tab-pane fade show active" id="ftpsgt-connection" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Basic connection</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftps.port</code></td><td><code>21</code></td><td>Remote port (use 990 for implicit FTPS)</td></tr>
              <tr><td><code>ftps.login</code></td><td><em>from host</em></td><td>FTP username</td></tr>
              <tr><td><code>ftps.password</code></td><td><em>from host</em></td><td>FTP password</td></tr>
              <tr><td><code>ftps.connectionType</code></td><td><code>FTP</code></td><td>Protocol variant: <code>FTP</code> (plain), <code>FTPS</code> (implicit TLS), <code>FTPES</code> (explicit TLS / STARTTLS)</td></tr>
              <tr><td><code>ftps.passive</code></td><td><code>false</code></td><td>Use PASV (passive) mode instead of active</td></tr>
              <tr><td><code>ftps.listenAddress</code></td><td><em>none</em></td><td>Local IP to bind for data connections</td></tr>
              <tr><td><code>ftps.cwd</code></td><td><em>none</em></td><td>Initial working directory after login</td></tr>
              <tr><td><code>ftps.connectionTimeOut</code></td><td><code>1m</code></td><td>Socket connection timeout</td></tr>
              <tr><td><code>ftps.readTimeOut</code></td><td><code>1m</code></td><td>Socket read timeout</td></tr>
              <tr><td><code>ftps.closeTimeOut</code></td><td><code>1m</code></td><td>Graceful close timeout</td></tr>
              <tr><td><code>ftps.keepAlive</code></td><td><code>0</code></td><td>Send keep-alive command every N ms (0 = disabled)</td></tr>
              <tr><td><code>ftps.useNoop</code></td><td><code>0</code></td><td>Send NOOP command every N ms to keep control channel alive (0 = disabled)</td></tr>
              <tr><td><code>ftps.sendBuffSize</code></td><td><em>OS default</em></td><td>TCP send buffer size (e.g. <code>256KB</code>)</td></tr>
              <tr><td><code>ftps.receiveBuffSize</code></td><td><em>OS default</em></td><td>TCP receive buffer size</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- TLS / SECURITY --%>
      <div class="tab-pane fade" id="ftpsgt-tls" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-shield-lock text-primary me-1"></i>TLS settings</p>
          <p class="small text-muted mb-1">Only relevant when <code>ftps.connectionType</code> is <code>FTPS</code> or <code>FTPES</code>.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftps.protocol</code></td><td><code>TLS</code></td><td>SSL/TLS protocol name passed to <code>SSLContext.getInstance()</code> (e.g. <code>TLSv1.2</code>, <code>TLSv1.3</code>)</td></tr>
              <tr><td><code>ftps.strict</code></td><td><code>false</code></td><td>When <code>false</code> all server certificates are accepted (trust-all). Set to <code>true</code> to use the JVM default trust store for strict certificate validation.</td></tr>
            </tbody>
          </table>
        </div>
        <div class="alert alert-warning py-2 px-3 small d-flex align-items-start gap-2 mb-0">
          <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
          <div>Leaving <code>ftps.strict=false</code> (the default) disables certificate validation. Use <code>true</code> in production environments to prevent man-in-the-middle attacks.</div>
        </div>
      </div>

      <%-- TRANSFER --%>
      <div class="tab-pane fade" id="ftpsgt-transfer" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-up-down text-primary me-1"></i>File transfer behaviour</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftps.usetmp</code></td><td><code>true</code></td><td>Upload to a temporary name then rename on completion</td></tr>
              <tr><td><code>ftps.useAppend</code></td><td><code>false</code></td><td>Use APPE (append) instead of STOR for resume support</td></tr>
              <tr><td><code>ftps.mkdirs</code></td><td><code>true</code></td><td>Create remote directory hierarchy if it does not exist</td></tr>
              <tr><td><code>ftps.prefix</code></td><td><em>none</em></td><td>Prefix added to the remote filename</td></tr>
              <tr><td><code>ftps.suffix</code></td><td><em>none</em></td><td>Suffix added to the remote filename</td></tr>
              <tr><td><code>ftps.mksuffix</code></td><td><code>false</code></td><td>Generate a random suffix and append it to the filename</td></tr>
              <tr><td><code>ftps.usesuffix</code></td><td><code>false</code></td><td>Use the suffix set by <code>ftps.suffix</code> during upload, rename away on completion</td></tr>
              <tr><td><code>ftps.usecleanpath</code></td><td><code>false</code></td><td>Strip duplicate slashes and resolve <code>.</code>/<code>..</code> in paths before sending</td></tr>
              <tr><td><code>ftps.deleteOnRename</code></td><td><code>true</code></td><td>Delete the target before renaming the temporary file (avoids rename-to-existing failures)</td></tr>
              <tr><td><code>ftps.ignoreCheck</code></td><td><code>true</code></td><td>Do not verify the transferred file size after upload</td></tr>
              <tr><td><code>ftps.ignoreDelete</code></td><td><code>true</code></td><td>Ignore errors when deleting the remote file</td></tr>
              <tr><td><code>ftps.md5Ext</code></td><td><code>.md5</code></td><td>Extension used for MD5 checksum sidecar files</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- PATHS --%>
      <div class="tab-pane fade" id="ftpsgt-dir" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder text-primary me-1"></i>Directory creation</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftps.mkdirsCmdIndex</code></td><td><code>0</code></td><td>Path component depth at which to start creating directories (0 = from root)</td></tr>
              <tr><td><code>ftps.preMkdirsCmd</code></td><td><em>none</em></td><td>FTP command(s) sent before creating directories (newline-separated)</td></tr>
              <tr><td><code>ftps.postMkdirsCmd</code></td><td><em>none</em></td><td>FTP command(s) sent after creating directories</td></tr>
              <tr><td><code>ftps.ignoreMkdirsCmdErrors</code></td><td><code>false</code></td><td>Continue even if pre/post mkdir commands return an error</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <%-- HOOKS --%>
      <div class="tab-pane fade" id="ftpsgt-hooks" role="tabpanel">
        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-terminal text-primary me-1"></i>FTP command hooks</p>
          <p class="small text-muted mb-1">Each option accepts one or more raw FTP commands separated by newlines. Commands are sent over the control channel at the indicated lifecycle point.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>When</th></tr></thead>
            <tbody>
              <tr><td><code>ftps.postConnectCmd</code></td><td>Immediately after login</td></tr>
              <tr><td><code>ftps.preCloseCmd</code></td><td>Before closing the control connection</td></tr>
              <tr><td><code>ftps.prePutCmd</code></td><td>Before each file upload</td></tr>
              <tr><td><code>ftps.postPutCmd</code></td><td>After each successful upload</td></tr>
              <tr><td><code>ftps.preGetCmd</code></td><td>Before each file download</td></tr>
              <tr><td><code>ftps.postGetCmd</code></td><td>After each successful download</td></tr>
            </tbody>
          </table>
        </div>
        <div class="mb-0">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Example</p>
          <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg);white-space:pre-wrap;">ftps.connectionType=FTPES
ftps.passive=true
ftps.usetmp=true
ftps.mkdirs=true
ftps.postConnectCmd=OPTS UTF8 ON</pre>
        </div>
      </div>

    </div>
  </div>
</div>
