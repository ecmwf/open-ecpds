<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- FTP Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>FTP Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>ftp.</code> prefix. The host <em>Login</em> and
      <em>Password</em> fields provide the default credentials but can be overridden
      with <code>ftp.login</code> and <code>ftp.password</code>.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="ftpGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#ftpgt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#ftpgt-transfer" type="button" role="tab">
          <i class="bi bi-arrow-up-down me-1"></i>Transfer
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#ftpgt-dir" type="button" role="tab">
          <i class="bi bi-folder me-1"></i>Directory &amp; Paths
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#ftpgt-hooks" type="button" role="tab">
          <i class="bi bi-terminal me-1"></i>Hooks
        </button>
      </li>
    </ul>

    <div class="tab-content" id="ftpGuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="ftpgt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Basic connection</p>
          <p class="small text-muted mb-1">The host name / IP and port are taken from the host record.
          Use the options below to override.</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.port</code></td><td><code>21</code></td><td>Remote FTP port</td></tr>
              <tr><td><code>ftp.login</code></td><td><em>from host</em></td><td>FTP username</td></tr>
              <tr><td><code>ftp.password</code></td><td><em>from host</em></td><td>FTP password</td></tr>
              <tr><td><code>ftp.nopassword</code></td><td><code>false</code></td><td>Authenticate without sending a password (anonymous-style)</td></tr>
              <tr><td><code>ftp.listenAddress</code></td><td><em>none</em></td><td>Local IP address to bind for data connections</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-shuffle text-primary me-1"></i>Data channel mode</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.passive</code></td><td><code>no</code></td>
                  <td><code>no</code> = active, <code>yes</code> = passive (PASV),
                  <code>shared</code> = passive with shared data connection</td></tr>
              <tr><td><code>ftp.extended</code></td><td><code>false</code></td><td>Use EPSV/EPRT (IPv6-friendly extended passive/active)</td></tr>
              <tr><td><code>ftp.lowPort</code></td><td><code>false</code></td><td>Use low port numbers (&lt;1024) for active mode data connections</td></tr>
              <tr><td><code>ftp.dataAlive</code></td><td><code>false</code></td><td>Keep the data connection alive between transfers</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-clock text-primary me-1"></i>Timeouts &amp; keep-alive</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.commTimeOut</code></td><td><code>1m</code></td><td>Control connection command timeout</td></tr>
              <tr><td><code>ftp.dataTimeOut</code></td><td><code>1m</code></td><td>Data connection read/write timeout</td></tr>
              <tr><td><code>ftp.portTimeOut</code></td><td><code>1m</code></td><td>Timeout waiting for the data port to open</td></tr>
              <tr><td><code>ftp.keepAlive</code></td><td><code>0</code></td><td>Duration to cache and reuse FTP connections (e.g. <code>30s</code>); 0 disables caching</td></tr>
              <tr><td><code>ftp.useNoop</code></td><td><code>0</code></td><td>Send NOOP commands on idle connections at this interval to keep them alive</td></tr>
              <tr><td><code>ftp.keepControlConnectionAlive</code></td><td><code>false</code></td><td>Prevent the control connection from timing out during long data transfers</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-left-right text-primary me-1"></i>Buffer sizes</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.sendBuffSize</code></td><td><em>OS default</em></td><td>TCP send buffer size (e.g. <code>256k</code>)</td></tr>
              <tr><td><code>ftp.receiveBuffSize</code></td><td><em>OS default</em></td><td>TCP receive buffer size</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-gear text-primary me-1"></i>NOOP command customisation</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.setNoop</code></td><td><em>none</em></td><td>Custom command to send instead of NOOP (e.g. <code>STAT</code>)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- passive FTP --%>
ftp.passive = "yes"
ftp.port = "21"
ftp.commTimeOut = "2m"
ftp.dataTimeOut = "5m"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- connection caching (reuse for 60 s) --%>
ftp.keepAlive = "60s"
ftp.useNoop = "30s"</pre>
        </div>

      </div><%-- /ftpgt-connection --%>

      <%-- ================================================================
           TAB 2: TRANSFER
           ================================================================ --%>
      <div class="tab-pane fade" id="ftpgt-transfer" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-file-earmark text-primary me-1"></i>Temporary &amp; suffix handling</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.usetmp</code></td><td><code>true</code></td><td>Upload to a <code>.tmp</code> file and rename on completion</td></tr>
              <tr><td><code>ftp.prefix</code></td><td><em>empty</em></td><td>Prefix to prepend to the remote filename during upload</td></tr>
              <tr><td><code>ftp.suffix</code></td><td><em>empty</em></td><td>Suffix to append to the remote filename during upload</td></tr>
              <tr><td><code>ftp.mksuffix</code></td><td><code>false</code></td><td>Generate a unique suffix automatically</td></tr>
              <tr><td><code>ftp.usesuffix</code></td><td><code>false</code></td><td>Keep the generated suffix in the final remote name</td></tr>
              <tr><td><code>ftp.useAppend</code></td><td><code>false</code></td><td>Use APPE (append) instead of STOR for uploads</td></tr>
              <tr><td><code>ftp.deleteOnRename</code></td><td><code>true</code></td><td>Delete existing destination file before rename</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-speedometer2 text-primary me-1"></i>Parallelism &amp; integrity</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.parallelStreams</code></td><td><code>0</code></td><td>Number of parallel data streams for GET/PUT (0 = single stream)</td></tr>
              <tr><td><code>ftp.ignoreCheck</code></td><td><code>true</code></td><td>Skip post-transfer integrity check (SIZE command)</td></tr>
              <tr><td><code>ftp.retryAfterTimeoutOnCheck</code></td><td><code>false</code></td><td>Retry the integrity check after a timeout instead of failing</td></tr>
              <tr><td><code>ftp.ignoreDelete</code></td><td><code>true</code></td><td>Ignore errors when deleting remote files</td></tr>
              <tr><td><code>ftp.md5Ext</code></td><td><em>none</em></td><td>If set, write an MD5 sidecar file with this extension (e.g. <code>.md5</code>)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-list-ul text-primary me-1"></i>Directory listing</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.usenlist</code></td><td><code>false</code></td><td>Use NLST instead of LIST for directory listing</td></tr>
              <tr><td><code>ftp.like</code></td><td><code>false</code></td><td>When using NLST, reformat entries to look like LIST output</td></tr>
              <tr><td><code>ftp.ftpuser</code></td><td><em>from host login</em></td><td>Owner user shown in generated directory listings</td></tr>
              <tr><td><code>ftp.ftpgroup</code></td><td><em>from host login</em></td><td>Owner group shown in generated directory listings</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start examples</p>
          <pre class="bg-light border rounded p-2 small mb-1"><%-- safe upload with checksum --%>
ftp.usetmp = "yes"
ftp.ignoreCheck = "no"
ftp.deleteOnRename = "yes"</pre>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- parallel streams transfer --%>
ftp.parallelStreams = "4"
ftp.passive = "yes"</pre>
        </div>

      </div><%-- /ftpgt-transfer --%>

      <%-- ================================================================
           TAB 3: DIRECTORY & PATHS
           ================================================================ --%>
      <div class="tab-pane fade" id="ftpgt-dir" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder2-open text-primary me-1"></i>Working directory</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.cwd</code></td><td><em>from host path</em></td><td>Remote working directory (CWD) to change to after login</td></tr>
              <tr><td><code>ftp.usecleanpath</code></td><td><code>false</code></td><td>Normalise remote paths (remove double slashes, etc.)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-primary me-1"></i>Directory creation (mkdirs)</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.mkdirs</code></td><td><code>yes</code></td>
                  <td><code>yes</code> = create directories locally before transfer,
                  <code>no</code> = never create,
                  <code>remote</code> = send MKD commands to the server</td></tr>
              <tr><td><code>ftp.mkdirsCmdIndex</code></td><td><code>0</code></td><td>Path component depth at which to start creating directories</td></tr>
              <tr><td><code>ftp.ignoreMkdirsCmdErrors</code></td><td><code>false</code></td><td>Ignore MKD command errors (useful when directory may already exist)</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start example</p>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- auto-create remote directories --%>
ftp.mkdirs = "remote"
ftp.ignoreMkdirsCmdErrors = "yes"
ftp.cwd = "/data/incoming"</pre>
        </div>

      </div><%-- /ftpgt-dir --%>

      <%-- ================================================================
           TAB 4: HOOKS / COMMANDS
           ================================================================ --%>
      <div class="tab-pane fade" id="ftpgt-hooks" role="tabpanel">

        <div class="alert alert-warning py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-exclamation-triangle flex-shrink-0" style="margin-top:0.1em"></i>
          <div>Hook commands are sent as raw FTP commands. Syntax errors may abort the transfer.</div>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-terminal text-primary me-1"></i>Connection hooks</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>When</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.postConnectCmd</code></td><td>after login</td><td>FTP command to run immediately after successful login</td></tr>
              <tr><td><code>ftp.preCloseCmd</code></td><td>before logout</td><td>FTP command to run before closing the session</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-file-earmark-arrow-up text-primary me-1"></i>PUT hooks</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>When</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.prePutCmd</code></td><td>before STOR</td><td>FTP command sent before uploading a file</td></tr>
              <tr><td><code>ftp.postPutCmd</code></td><td>after STOR</td><td>FTP command sent after uploading a file</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-file-earmark-arrow-down text-primary me-1"></i>GET hooks</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>When</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.preGetCmd</code></td><td>before RETR</td><td>FTP command sent before downloading a file</td></tr>
              <tr><td><code>ftp.postGetCmd</code></td><td>after RETR</td><td>FTP command sent after downloading a file</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-primary me-1"></i>Mkdirs hooks</p>
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>When</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>ftp.preMkdirsCmd</code></td><td>before MKD</td><td>FTP command sent before creating directories</td></tr>
              <tr><td><code>ftp.postMkdirsCmd</code></td><td>after MKD</td><td>FTP command sent after creating directories</td></tr>
            </tbody>
          </table>
        </div>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Quick-start example</p>
          <pre class="bg-light border rounded p-2 small mb-0"><%-- run a server-side script after each upload --%>
ftp.postPutCmd = "SITE EXEC /opt/scripts/notify.sh"</pre>
        </div>

      </div><%-- /ftpgt-hooks --%>

    </div><%-- tab-content --%>
  </div><%-- offcanvas-body --%>
</div><%-- offcanvas --%>
