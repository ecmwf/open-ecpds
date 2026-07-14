<%@ page pageEncoding="UTF-8" %>

<%-- Portal Service Guide — offcanvas panel included from data.jsp --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="portalServiceGuideOffcanvas"
     aria-labelledby="portalServiceGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="portalServiceGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Data User &mdash; Portal Service Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <%-- Tab navigation --%>
    <ul class="nav nav-tabs nav-fill mb-3" id="psgTabs" role="tablist">
      <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#psg-overview"    type="button"><i class="bi bi-grid me-1"></i>Overview</button></li>
      <li class="nav-item"><button class="nav-link small py-1"        data-bs-toggle="tab" data-bs-target="#psg-standard"    type="button"><i class="bi bi-lock-fill me-1"></i>Standard</button></li>
      <li class="nav-item"><button class="nav-link small py-1"        data-bs-toggle="tab" data-bs-target="#psg-open"        type="button"><i class="bi bi-unlock-fill me-1"></i>Open</button></li>
      <li class="nav-item"><button class="nav-link small py-1"        data-bs-toggle="tab" data-bs-target="#psg-self"        type="button"><i class="bi bi-person-plus-fill me-1"></i>Self-Service</button></li>
      <li class="nav-item"><button class="nav-link small py-1"        data-bs-toggle="tab" data-bs-target="#psg-options"     type="button"><i class="bi bi-sliders me-1"></i>Options</button></li>
    </ul>

    <div class="tab-content">

      <%-- ================================================================
           OVERVIEW tab
           ================================================================ --%>
      <div class="tab-pane fade show active" id="psg-overview" role="tabpanel">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
          <div>Each Data User has a <strong>Portal Service</strong> setting that controls how visitors
          authenticate on the data portal. Choose the mode that best fits the audience for this user's data.</div>
        </div>

        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light">
              <tr><th style="width:30%">Mode</th><th>Who logs in?</th><th>Credentials?</th><th>TOTP?</th><th>Use when…</th></tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-normal"><i class="bi bi-lock-fill me-1"></i>Standard Login</span></td>
                <td>Authorised individuals</td>
                <td><i class="bi bi-check-circle-fill text-success"></i> Yes — admin-set password</td>
                <td><i class="bi bi-check-circle-fill text-success"></i> Supported</td>
                <td>Known partners, automated systems, internal teams</td>
              </tr>
              <tr>
                <td><span class="badge rounded-pill bg-warning-subtle text-warning-emphasis border border-warning-subtle fw-normal"><i class="bi bi-unlock-fill me-1"></i>Open Access</span></td>
                <td>Anyone (anonymous)</td>
                <td><i class="bi bi-x-circle-fill text-danger"></i> None</td>
                <td><i class="bi bi-x-circle-fill text-danger"></i> No</td>
                <td>Public datasets, open portals, read-only demo data</td>
              </tr>
              <tr>
                <td><span class="badge rounded-pill bg-info-subtle text-info-emphasis border border-info-subtle fw-normal"><i class="bi bi-person-plus-fill me-1"></i>Self-Service</span></td>
                <td>Self-registered visitors</td>
                <td><i class="bi bi-check-circle-fill text-success"></i> Auto-generated per subscriber</td>
                <td><i class="bi bi-x-circle-fill text-danger"></i> No</td>
                <td>Community access, conference datasets, broad audiences</td>
              </tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-arrow-right-circle text-primary me-1"></i>How to change the mode</p>
        <ol class="small ps-3 mb-3">
          <li>Open the data user's edit page using the <i class="bi bi-pencil"></i> button.</li>
          <li>Select the desired mode in the <strong>Portal Service</strong> dropdown.</li>
          <li>Click <strong>Save</strong>. The change takes effect immediately — any existing sessions are invalidated.</li>
        </ol>

        <div class="alert alert-warning py-2 px-3 small d-flex align-items-start gap-2">
          <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
          <div>Switching between modes <strong>immediately</strong> affects all logged-in users. For example,
          switching from <em>Standard Login</em> to <em>Self-Service</em> will invalidate any cached sessions
          so that previously authorised credentials no longer work.</div>
        </div>

      </div><!-- /overview -->

      <%-- ================================================================
           STANDARD LOGIN tab
           ================================================================ --%>
      <div class="tab-pane fade" id="psg-standard" role="tabpanel">

        <div class="alert alert-secondary py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-lock-fill flex-shrink-0 mt-1"></i>
          <div><strong>Standard Login</strong> is the default mode. Visitors must authenticate with a
          username/password pair that is set by an administrator directly in the Data User record.
          Optionally, a second factor (TOTP) can be required.</div>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-key text-primary me-1"></i>Authentication</p>
        <ul class="small ps-3 mb-3">
          <li>The visitor logs in with the <strong>Data Login</strong> name (e.g. <code>test</code>) as the username.</li>
          <li>The password is the value stored in the <strong>Password</strong> field of this Data User.</li>
          <li>HTTP Basic Auth is used; credentials are cached in the browser for the session.</li>
          <li>The portal login page is shown when the browser does not yet have credentials.</li>
        </ul>

        <p class="small fw-semibold mb-1"><i class="bi bi-shield-lock text-primary me-1"></i>TOTP (Time-based One-Time Password)</p>
        <ul class="small ps-3 mb-3">
          <li>Enable the <strong>TOTP authentication</strong> toggle in the edit form to require a second factor.</li>
          <li>The portal login page shows a numeric keypad where the visitor enters the 6-digit code.</li>
          <li>TOTP secrets are managed externally (via the authorised keys / ECAUTH module).</li>
          <li>When TOTP is active, <em>Standard Login</em> with the password alone is rejected.</li>
        </ul>

        <p class="small fw-semibold mb-1"><i class="bi bi-person-badge text-primary me-1"></i>Session management</p>
        <ul class="small ps-3 mb-3">
          <li>Sessions are cached on the DataMover for up to 1 hour (configurable via <code>portal.sessionTTLMinutes</code>).</li>
          <li>Use the <strong>Close All</strong> button on the data user detail page to force all sessions to end.</li>
          <li>Maximum concurrent sessions can be limited (and scheduled) via the <code>portal.maxConnections</code> property.</li>
        </ul>

        <div class="alert alert-info py-2 px-3 small d-flex align-items-start gap-2">
          <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
          <div>For automated systems (scripts, cron jobs), Standard Login is the right choice. Store the
          credentials securely in a <code>.netrc</code> file or environment variable.</div>
        </div>

      </div><!-- /standard -->

      <%-- ================================================================
           OPEN ACCESS tab
           ================================================================ --%>
      <div class="tab-pane fade" id="psg-open" role="tabpanel">

        <div class="alert alert-warning py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-unlock-fill flex-shrink-0 mt-1"></i>
          <div><strong>Open Access</strong> disables all authentication. Any visitor can browse and
          download files directly — no login prompt, no credentials required.</div>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-shield-x text-warning me-1"></i>How it works</p>
        <ul class="small ps-3 mb-3">
          <li>The DataMover serves all file listing and download requests without checking credentials.</li>
          <li>There is no login page — the file list is shown immediately.</li>
          <li>The data portal user menu (download history, quotas) is hidden for anonymous visitors.</li>
          <li>No session is created; each request is processed independently.</li>
        </ul>

        <p class="small fw-semibold mb-1"><i class="bi bi-link-45deg text-primary me-1"></i>Sharing the portal URL</p>
        <ul class="small ps-3 mb-3">
          <li>Simply share the portal URL: <code>https://&lt;portal-host&gt;/ecpds/data/list/&lt;login&gt;</code></li>
          <li>The visitor can bookmark it and access data directly — no registration needed.</li>
        </ul>

        <p class="small fw-semibold mb-1"><i class="bi bi-exclamation-triangle text-warning me-1"></i>Considerations</p>
        <ul class="small ps-3 mb-3">
          <li><strong>Download quotas</strong> are not tracked per visitor (no identity). Byte quotas at the Destination level still apply.</li>
          <li>For <strong>CORS</strong> (browser S3 or API access), configure the <code>portal.corsOrigin</code> property.</li>
          <li>If you need to share data publicly but retain download statistics, use <em>Self-Service</em> instead.</li>
        </ul>

        <div class="alert alert-danger py-2 px-3 small d-flex align-items-start gap-2">
          <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
          <div><strong>Any file reachable via the associated Destinations will be publicly accessible.</strong>
          Make sure only the intended data is attached to this Data User.</div>
        </div>

      </div><!-- /open -->

      <%-- ================================================================
           SELF-SERVICE tab
           ================================================================ --%>
      <div class="tab-pane fade" id="psg-self" role="tabpanel">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-person-plus-fill flex-shrink-0 mt-1"></i>
          <div><strong>Self-Service</strong> allows any visitor to self-register. Each subscriber receives
          their own auto-generated password by email. All subscribers share the same data configuration
          (destinations, properties) defined for this Data User.</div>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-diagram-3 text-primary me-1"></i>Registration flow</p>
        <ol class="small ps-3 mb-3">
          <li>The admin configures the Data User as <em>Self-Service</em> and optionally sets
              <code>portal.registrationAdminEmail</code> and <code>portal.registrationAutoApprove</code>.</li>
          <li>A <em>Share registration link</em> item appears in the portal user menu. The URL is:
              <code>/ecpds/register?user=&lt;login&gt;</code></li>
          <li>The visitor fills in their <strong>name</strong>, <strong>email address</strong>, and <strong>country</strong>.</li>
          <li>A verification email is sent. The visitor clicks the link to confirm their email.</li>
          <li>If <code>registrationAutoApprove = true</code> (or the property is absent): the account is activated
              immediately and credentials are emailed to the subscriber.</li>
          <li>If <code>registrationAutoApprove = false</code>: the admin receives a notification and must activate
              the subscriber manually via the <strong>Portal Subscribers</strong> page.</li>
          <li>The subscriber logs into the portal with:
              <ul>
                <li><strong>Username:</strong> the Data User login (e.g. <code>test</code>)</li>
                <li><strong>Password:</strong> their unique auto-generated password</li>
              </ul>
          </li>
        </ol>

        <p class="small fw-semibold mb-1"><i class="bi bi-people-fill text-primary me-1"></i>Managing subscribers</p>
        <ul class="small ps-3 mb-3">
          <li>Use the <span class="badge bg-info rounded-pill fw-normal"><i class="bi bi-people-fill"></i></span>
              <strong>Portal Subscribers</strong> button (visible when Portal Service is Self-Service) to open the
              subscriber management page.</li>
          <li>From there you can view status, activate/deactivate individual subscribers, or delete them.</li>
          <li>Status values: <span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-normal"><i class="bi bi-envelope me-1"></i>Pending Email</span>
              <span class="badge rounded-pill bg-warning-subtle text-warning-emphasis border border-warning-subtle fw-normal"><i class="bi bi-hourglass-split me-1"></i>Awaiting Approval</span>
              <span class="badge rounded-pill bg-success-subtle text-success-emphasis border border-success-subtle fw-normal"><i class="bi bi-check-circle-fill me-1"></i>Active</span>
              <span class="badge rounded-pill bg-danger-subtle text-danger-emphasis border border-danger-subtle fw-normal"><i class="bi bi-x-circle-fill me-1"></i>Deactivated</span></li>
        </ul>

        <p class="small fw-semibold mb-1"><i class="bi bi-gear text-primary me-1"></i>Configuration properties</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.registrationAutoApprove</code></td><td><code>"false"</code></td>
                  <td>If <code>true</code>, subscriber accounts are activated immediately on email verification. If <code>false</code>, an admin must approve each subscriber via the Portal Subscribers page.</td></tr>
              <tr><td><code>portal.registrationAdminEmail</code></td><td>(none)</td>
                  <td>Email address that receives notifications when a registration is submitted, when a subscriber verifies their email, or when a subscriber is activated. If empty, no admin notifications are sent.</td></tr>
            </tbody>
          </table>
        </div>
        <div class="alert alert-secondary py-2 px-3 small d-flex align-items-start gap-2 mb-3">
          <i class="bi bi-pencil-square flex-shrink-0 mt-1"></i>
          <div>These are per-user ECtrans properties set in the <strong>Properties</strong> accordion on the
          data user edit page, like any other <code>portal.*</code> option.
          Example: <code>portal.registrationAutoApprove = "true"</code></div>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-x-circle text-danger me-1"></i>Failed login behaviour</p>
        <ul class="small ps-3 mb-2">
          <li>When a visitor tries to access the portal with wrong or missing credentials for a Self-Service user,
              they are automatically redirected to the registration page rather than shown an authentication error.</li>
          <li>The registration page has a <em>Sign in</em> link that lets an existing subscriber re-enter their credentials.</li>
        </ul>

        <div class="alert alert-warning py-2 px-3 small d-flex align-items-start gap-2">
          <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
          <div>If you switch a user from <em>Standard Login</em> to <em>Self-Service</em>, the old
          Data User password will no longer work. Only active subscribers will be able to log in.</div>
        </div>

      </div><!-- /self-service -->

      <%-- ================================================================
           OPTIONS tab
           ================================================================ --%>
      <div class="tab-pane fade" id="psg-options" role="tabpanel">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-sliders flex-shrink-0 mt-1"></i>
          <div>The <strong>Properties</strong> field (ECtrans format: <code>group.option = "value"</code>)
          controls the behaviour and appearance of the data portal for this user. All properties are
          optional — the system uses sensible defaults when they are absent.</div>
        </div>

        <p class="small fw-semibold mb-2"><i class="bi bi-palette text-primary me-1"></i>Portal appearance &amp; behaviour</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:38%">Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.domain</code></td><td><code>"ecpds"</code></td>
                  <td>Namespace used to distinguish this portal from others on the same host. Used in URL paths.</td></tr>
              <tr><td><code>portal.color</code></td><td><code>"dodgerblue"</code></td>
                  <td>Accent colour for the portal header and interactive elements. Any CSS colour value.</td></tr>
              <tr><td><code>portal.welcome</code></td><td>(none)</td>
                  <td>Message displayed on the SFTP/SCP login banner and in the welcome screen.
                      Multi-line: <code>portal.welcome = "\nLine 1\nLine 2\n"</code></td></tr>
              <tr><td><code>portal.simpleList</code></td><td><code>"no"</code></td>
                  <td>If <code>yes</code>, shows a plain file listing without dates or sizes.</td></tr>
              <tr><td><code>portal.sort</code></td><td><code>"target"</code></td>
                  <td>Default sort column for the file listing. Options: <code>target</code>, <code>size</code>, <code>time</code>.</td></tr>
              <tr><td><code>portal.order</code></td><td><code>"asc"</code></td>
                  <td>Default sort direction: <code>asc</code> or <code>desc</code>.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-2"><i class="bi bi-person-badge text-primary me-1"></i>Session &amp; connection limits</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:38%">Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.maxConnections</code></td><td>(unlimited)</td>
                  <td>Maximum number of simultaneous sessions. If a schedule is defined
                      (e.g. <code>"100"</code> off-peak, <code>"200"</code> peak), the limit changes automatically.</td></tr>
              <tr><td><code>portal.sessionTTLMinutes</code></td><td><code>"60"</code></td>
                  <td>How long an authenticated session is cached (in minutes). Lower values force more frequent re-authentication.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-2"><i class="bi bi-hdd text-primary me-1"></i>Download &amp; upload quotas</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:38%">Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.downloadBytesQuota</code></td><td>(unlimited)</td>
                  <td>Maximum bytes a subscriber can download per quota period. Set to a byte count (e.g. <code>"10737418240"</code> for 10 GB).</td></tr>
              <tr><td><code>portal.uploadBytesQuota</code></td><td>(unlimited)</td>
                  <td>Maximum bytes a subscriber can upload per quota period.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-2"><i class="bi bi-globe text-primary me-1"></i>CORS (browser / S3 API access)</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:38%">Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.corsOrigin</code></td><td>(disabled)</td>
                  <td>Allowed CORS origin(s) for browser requests. Use <code>"*"</code> for any origin,
                      or a specific URL such as <code>"https://app.example.com"</code>.</td></tr>
              <tr><td><code>portal.corsMethods</code></td><td><code>"GET,HEAD,OPTIONS"</code></td>
                  <td>Allowed HTTP methods in CORS preflight responses.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-2"><i class="bi bi-file-earmark-code text-primary me-1"></i>Content-type mapping (<code>portal.headerRegistry</code>)</p>
        <p class="small mb-2">Override the <code>Content-Type</code> header served for matching filenames using a rule list:</p>
        <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">portal.headerRegistry = "
(== {.*.grib2?$}) Content-Type=application/grib
(== {.*.nc$})     Content-Type=application/netcdf
(== {.*.json$})   Content-Type=application/json
"</pre>
        <p class="small text-muted mb-3">Each rule is a pattern match followed by the header to inject. The first matching rule wins. Patterns use Java regex syntax wrapped in <code>{…}</code>.</p>

        <p class="small fw-semibold mb-2"><i class="bi bi-journal-text text-primary me-1"></i>Tracking &amp; events</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th style="width:38%">Property</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>portal.recordHistory</code></td><td><code>"yes"</code></td>
                  <td>If <code>yes</code>, download history is recorded and visible to subscribers.</td></tr>
              <tr><td><code>portal.recordSplunk</code></td><td><code>"no"</code></td>
                  <td>If <code>yes</code>, Splunk events are emitted for each portal access.</td></tr>
              <tr><td><code>portal.triggerEvent</code></td><td><code>"no"</code></td>
                  <td>If <code>yes</code>, a system event is fired when a file is downloaded through this portal.</td></tr>
              <tr><td><code>portal.updateLastLoginInformation</code></td><td><code>"yes"</code></td>
                  <td>If <code>yes</code>, the Data User's last-login timestamp and host are updated on each session.</td></tr>
            </tbody>
          </table>
        </div>

        <div class="alert alert-secondary py-2 px-3 small d-flex align-items-start gap-2">
          <i class="bi bi-pencil-square flex-shrink-0 mt-1"></i>
          <div>All properties use the ECtrans format: <code>group.option = "value"</code>.
          Multi-line values start with a leading newline:
          <code>portal.welcome = "\nLine 1\nLine 2\n"</code>.
          Open the edit page and use the <strong>Properties</strong> accordion to enter or modify them.</div>
        </div>

      </div><!-- /options -->

    </div><!-- /tab-content -->
  </div><!-- /offcanvas-body -->
</div><!-- /offcanvas -->
