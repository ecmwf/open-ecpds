<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page  %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- WebDAV Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>WebDAV Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>The <strong>WebDAV</strong> module transfers files over HTTP/HTTPS using the WebDAV
      protocol (RFC&nbsp;4918). All module-specific options use the <code>webdav.</code> prefix.
      Credentials default to the host <em>Login</em> / <em>Password</em> fields but can be
      overridden with <code>webdav.username</code> / <code>webdav.password</code>.
      Low-level TCP tuning uses the shared <code>ectrans.</code> prefix.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="wdavGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#wdav-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#wdav-tls" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>TLS / SSL
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#wdav-locking" type="button" role="tab">
          <i class="bi bi-lock me-1"></i>Locking &amp; Dirs
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#wdav-tcp" type="button" role="tab">
          <i class="bi bi-speedometer2 me-1"></i>TCP Tuning
        </button>
      </li>
    </ul>

    <div class="tab-content" id="wdavGuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="wdav-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Target server</p>
          <p class="small text-muted mb-1">The host <em>Name</em> field is used as the server hostname.
          Use these options to control scheme, port, and the base path under which files are stored.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.scheme</code></td>
                <td><code>https</code></td>
                <td>Protocol to use: <code>https</code> or <code>http</code>. Use <code>http</code> only on trusted networks.</td>
              </tr>
              <tr>
                <td><code>webdav.port</code></td>
                <td><code>443</code></td>
                <td>Remote port. Common values: <code>443</code> (HTTPS), <code>80</code> (HTTP), <code>8443</code> (custom TLS).</td>
              </tr>
              <tr>
                <td><code>webdav.path</code></td>
                <td><code>/</code></td>
                <td>Base path prefix prepended to every file path sent to the server. Trailing slash is normalised automatically.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person-lock text-primary me-1"></i>Authentication</p>
          <p class="small text-muted mb-1">HTTP Basic auth credentials. If either field is blank the host
          <em>Login</em> / <em>Password</em> values are used. The module sends a pre-emptive
          <code>Authorization: Basic …</code> header to avoid an extra round-trip on every request.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.username</code></td>
                <td><em>from host login</em></td>
                <td>Username for HTTP Basic authentication.</td>
              </tr>
              <tr>
                <td><code>webdav.password</code></td>
                <td><em>from host password</em></td>
                <td>Password for HTTP Basic authentication.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-clock text-primary me-1"></i>Timeouts</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.connectTimeout</code></td>
                <td><code>30s</code></td>
                <td>Maximum time to wait for a TCP connection to be established.
                Accepts duration strings such as <code>30s</code> or <code>1m</code>.</td>
              </tr>
              <tr>
                <td><code>webdav.socketTimeout</code></td>
                <td><code>5m</code></td>
                <td>Maximum time to wait for a response from the server after the request
                is sent (covers both PROPFIND listings and large PUT uploads).</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-diagram-3 text-primary me-1"></i>Proxy</p>
          <p class="small text-muted mb-1">Route all WebDAV traffic through an HTTP proxy.
          Proxy authentication is not yet supported.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.proxy</code></td>
                <td><em>none</em></td>
                <td>Proxy address in <code>host:port</code> format, e.g. <code>proxy.example.com:8080</code>.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="border rounded p-2 small mb-1" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- standard HTTPS WebDAV on port 443 --%>
webdav.scheme = "https"
webdav.port = "443"
webdav.path = "/remote.php/dav/files/ecpds/"
webdav.connectTimeout = "30s"
webdav.socketTimeout = "10m"</pre>
          <pre class="border rounded p-2 small mb-0" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- HTTP via corporate proxy --%>
webdav.scheme = "http"
webdav.port = "80"
webdav.proxy = "proxy.corp.example.com:3128"</pre>
        </div>

      </div><%-- /wdav-connection --%>

      <%-- ================================================================
           TAB 2: TLS / SSL
           ================================================================ --%>
      <div class="tab-pane fade" id="wdav-tls" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-patch-check text-primary me-1"></i>Certificate validation</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.sslValidation</code></td>
                <td><code>false</code></td>
                <td>When <code>true</code>, the server certificate is validated against the JVM trust store
                and hostname verification is enforced. Set to <code>false</code> to accept self-signed
                or mismatched certificates (useful for internal/staging servers).</td>
              </tr>
            </tbody>
          </table>
          <div class="alert alert-warning py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0" style="margin-top:0.1em"></i>
            <div>Disabling SSL validation (<code>sslValidation = false</code>) exposes transfers
            to man-in-the-middle attacks. Enable it in production and import any custom CA
            certificates into the JVM trust store.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-list-check text-primary me-1"></i>Accepted TLS protocol versions</p>
          <p class="small text-muted mb-1">Restrict which TLS versions are offered to the server during
          the handshake. Versions are matched by the JVM's JSSE layer; only versions supported by the
          running JDK are actually negotiated.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.supportedProtocols</code></td>
                <td><code>TLSv1,TLSv1.1,<br>TLSv1.2,TLSv1.3</code></td>
                <td>Comma-separated list of TLS versions to enable.
                To restrict to TLS&nbsp;1.2+ only: <code>TLSv1.2,TLSv1.3</code>.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="border rounded p-2 small mb-1" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- production: validate cert, restrict to modern TLS --%>
webdav.sslValidation = "true"
webdav.supportedProtocols = "TLSv1.2,TLSv1.3"</pre>
          <pre class="border rounded p-2 small mb-0" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- dev/staging: accept self-signed cert --%>
webdav.sslValidation = "false"
webdav.supportedProtocols = "TLSv1.2,TLSv1.3"</pre>
        </div>

      </div><%-- /wdav-tls --%>

      <%-- ================================================================
           TAB 3: LOCKING & DIRECTORIES
           ================================================================ --%>
      <div class="tab-pane fade" id="wdav-locking" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-primary me-1"></i>Automatic directory creation</p>
          <p class="small text-muted mb-1">When a PUT returns <code>HTTP 409 Conflict</code> (parent directory
          does not exist), the module can automatically issue <code>MKCOL</code> requests to create each
          missing path segment before retrying the upload.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.mkdirs</code></td>
                <td><code>true</code></td>
                <td>Automatically create missing parent directories when a PUT fails with 409.
                Disable if the server does not support <code>MKCOL</code> or if you prefer
                strict failure on missing paths.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-lock-fill text-primary me-1"></i>WebDAV locking (LOCK / UNLOCK)</p>
          <p class="small text-muted mb-1">When enabled, the module acquires an exclusive WebDAV write lock
          on the target resource before each PUT and releases it on completion. This prevents concurrent
          writers from corrupting a file on servers that enforce DAV locking. Only enable if the remote
          server supports DAV class&nbsp;2 locking.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>webdav.useLock</code></td>
                <td><code>false</code></td>
                <td>Acquire a WebDAV <code>LOCK</code> before every PUT and release it afterwards.
                The lock is always released, even if the PUT fails.</td>
              </tr>
              <tr>
                <td><code>webdav.lockTimeout</code></td>
                <td><code>300</code></td>
                <td>Requested lock duration in seconds. The server may grant a shorter timeout.
                Set to <code>0</code> to request an infinite-duration lock
                (<code>Timeout: Infinite</code>).</td>
              </tr>
              <tr>
                <td><code>webdav.lockOwner</code></td>
                <td><code>ecpds</code></td>
                <td>Value placed in the DAV <code>&lt;owner&gt;</code> element of the LOCK request.
                Used by the server to identify who holds the lock.</td>
              </tr>
            </tbody>
          </table>
          <div class="alert alert-secondary py-2 px-3 mt-2 small d-flex align-items-start gap-2">
            <i class="bi bi-info-circle flex-shrink-0" style="margin-top:0.1em"></i>
            <div>Most commodity WebDAV servers (Nextcloud, ownCloud, Apache mod_dav) support class&nbsp;2
            locking. If the server returns <code>501 Not Implemented</code> on a LOCK request, leave
            <code>webdav.useLock = false</code>.</div>
          </div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="border rounded p-2 small mb-1" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- safe concurrent upload to a locking-capable server --%>
webdav.useLock = "true"
webdav.lockTimeout = "600"
webdav.lockOwner = "ecpds-prod"
webdav.mkdirs = "true"</pre>
          <pre class="border rounded p-2 small mb-0" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- simple server without locking support --%>
webdav.useLock = "false"
webdav.mkdirs = "true"</pre>
        </div>

      </div><%-- /wdav-locking --%>

      <%-- ================================================================
           TAB 4: TCP TUNING
           ================================================================ --%>
      <div class="tab-pane fade" id="wdav-tcp" role="tabpanel">

        <p class="small text-muted mb-2">These options tune the underlying TCP socket and apply to
        all modules that use <code>SocketConfig</code>. They use the shared <code>ectrans.</code>
        prefix and override the OS defaults for this connection only.</p>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-toggle-on text-primary me-1"></i>Basic socket options</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>ectrans.tcpNoDelay</code></td>
                <td><em>OS default</em></td>
                <td>Disable Nagle's algorithm (<code>TCP_NODELAY</code>). Set <code>true</code>
                to reduce latency for small messages; set <code>false</code> to allow coalescing.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpQuickAck</code></td>
                <td><em>OS default</em></td>
                <td>Enable <code>TCP_QUICKACK</code> (Linux only). Sends ACKs immediately rather
                than delaying them, reducing latency on interactive or bursty transfers.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpMaxSegment</code></td>
                <td><em>OS default</em></td>
                <td>Maximum segment size (MSS) in bytes (<code>TCP_MAXSEG</code>). Useful for
                paths where the MTU is known.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpWindowClamp</code></td>
                <td><em>OS default</em></td>
                <td>Clamp the TCP receive window to this many bytes (<code>TCP_WINDOW_CLAMP</code>).
                Reduces bufferbloat on high-BDP paths.</td>
              </tr>
              <tr>
                <td><code>ectrans.soMaxPacingRate</code></td>
                <td><em>OS default</em></td>
                <td>Cap the kernel pacing rate (<code>SO_MAX_PACING_RATE</code>). Accepts byte-size
                notation, e.g. <code>100MB</code>. Requires a pacing-aware congestion control algorithm.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-heartbeat text-primary me-1"></i>Keep-alive</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>ectrans.tcpKeepAlive</code></td>
                <td><em>OS default</em></td>
                <td>Enable <code>SO_KEEPALIVE</code>. Allows the kernel to probe the connection
                and detect half-open sockets through firewalls / NAT.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpKeepAliveTime</code></td>
                <td><em>OS default</em></td>
                <td>Seconds of inactivity before the first keep-alive probe (<code>TCP_KEEPIDLE</code>).</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpKeepAliveInterval</code></td>
                <td><em>OS default</em></td>
                <td>Seconds between successive keep-alive probes (<code>TCP_KEEPINTVL</code>).</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpKeepAliveProbes</code></td>
                <td><em>OS default</em></td>
                <td>Number of unanswered probes before the connection is declared dead
                (<code>TCP_KEEPCNT</code>).</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-gear text-primary me-1"></i>Advanced</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr>
                <td><code>ectrans.tcpCongestionControl</code></td>
                <td><em>OS default</em></td>
                <td>TCP congestion control algorithm name, e.g. <code>bbr</code> or <code>cubic</code>
                (<code>TCP_CONGESTION</code>). Requires kernel support and CAP_NET_ADMIN or
                matching sysctl permissions.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpUserTimeout</code></td>
                <td><em>OS default</em></td>
                <td>Maximum time in milliseconds that transmitted data may remain unacknowledged
                before the connection is aborted (<code>TCP_USER_TIMEOUT</code>). Useful for
                detecting broken links faster than the keep-alive mechanism.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpLingerEnable</code></td>
                <td><em>OS default</em></td>
                <td>Enable <code>SO_LINGER</code>. Must be set together with
                <code>ectrans.tcpLingerTime</code>.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpLingerTime</code></td>
                <td><em>OS default</em></td>
                <td>Linger time in seconds when <code>SO_LINGER</code> is enabled.</td>
              </tr>
              <tr>
                <td><code>ectrans.tcpTimeStamp</code></td>
                <td><em>OS default</em></td>
                <td>Enable/disable <code>TCP_TIMESTAMP</code> option (per-socket override of
                the global sysctl).</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="border rounded p-2 small mb-1" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- high-throughput long-distance transfer (BBR + fast keep-alive) --%>
ectrans.tcpNoDelay = "true"
ectrans.tcpKeepAlive = "true"
ectrans.tcpKeepAliveTime = "60"
ectrans.tcpKeepAliveInterval = "10"
ectrans.tcpKeepAliveProbes = "6"
ectrans.tcpCongestionControl = "bbr"</pre>
          <pre class="border rounded p-2 small mb-0" style="background:var(--bs-tertiary-bg);white-space:pre-wrap;"><%-- detect broken connections quickly (e.g. through NAT) --%>
ectrans.tcpKeepAlive = "true"
ectrans.tcpKeepAliveTime = "30"
ectrans.tcpKeepAliveInterval = "5"
ectrans.tcpKeepAliveProbes = "3"
ectrans.tcpUserTimeout = "40000"</pre>
        </div>

      </div><%-- /wdav-tcp --%>

    </div><%-- /tab-content --%>

    <div class="alert alert-light border mt-3 py-2 px-3 small">
      <i class="bi bi-lightbulb text-warning me-1"></i>
      <strong>Option format:</strong> Supply options in the module <em>Init String</em> field as
      <code>key = "value"</code> pairs, one per line. Values in the init string take precedence
      over the corresponding host fields. Duration values accept ISO-8601 notation
      (<code>PT30S</code>) or shorthand (<code>30s</code>, <code>5m</code>).
    </div>
  </div><%-- /offcanvas-body --%>
</div><%-- /offcanvas --%>
