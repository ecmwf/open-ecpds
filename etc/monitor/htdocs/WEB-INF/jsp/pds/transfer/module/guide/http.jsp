<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- HTTP/MQTT Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>HTTP/MQTT Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div><strong>Two-phase flow:</strong>
      The acquisition engine runs <span class="badge bg-info text-dark">1&#8201;LIST</span>
      to discover files at the URL in the <em>Directory</em> field, then
      <span class="badge bg-success">2&#8201;GET</span> to fetch each one.
      The tabs below cover both phases.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="httpGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#hgt-listing" type="button" role="tab">
          <i class="bi bi-list-ul me-1"></i>Listing
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#hgt-auth" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>Auth
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#hgt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#hgt-registration" type="button" role="tab">
          <i class="bi bi-database-add me-1"></i>Registration
        </button>
      </li>
    </ul>

    <div class="tab-content" id="httpGuideTabContent">

      <%-- ================================================================
           TAB 1: LISTING
           ================================================================ --%>
      <div class="tab-pane fade show active" id="hgt-listing" role="tabpanel">
        <p class="small text-muted mb-2">The <strong>Directory</strong> field holds the URL (or MQTT topic). Choose the mode that matches how the remote server exposes its data.</p>

        <div class="accordion accordion-flush border rounded" id="httpGuideListingAcc">

          <%-- Mode A: HTML --%>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed small py-2 fw-semibold" type="button"
                      data-bs-toggle="collapse" data-bs-target="#hgl-html">
                <span class="badge bg-primary rounded-pill me-2">A</span>
                HTML directory listing
                <span class="badge bg-success ms-2 fw-normal" style="font-size:0.7rem">default</span>
              </button>
            </h2>
            <div id="hgl-html" class="accordion-collapse collapse"
                 data-bs-parent="#httpGuideListingAcc">
              <div class="accordion-body py-2 px-3">
                <p class="small mb-2">URL returns an HTML page. OpenECPDS fetches it and extracts links using a CSS selector (Jsoup). Sub-directories are optionally followed recursively.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap"># Only override what differs from the defaults shown
http.dodir = "yes"             # fetch &amp; parse the URL (default: yes)
http.select = "a[href]"        # CSS selector for link elements
http.attribute = "href"        # element attribute to extract (empty = element text)
http.listRecursive = "yes"     # follow sub-directory links
http.listMaxThreads = "10"     # parallel listing threads (default: 10)
http.listMaxWaiting = "100"    # max queued listing jobs (default: 100)
http.listMaxFiles = "500000"   # stop collecting after this many files
http.listMaxDirs = "50000"     # max sub-directories to visit
http.maxSize = "10MB"          # reject HTTP responses larger than this
http.useHead = "yes"           # HEAD per entry to get size &amp; date (faster)
http.ftpLike = "yes"           # parse response as FTP-style lines</pre>
                <div class="alert alert-light border py-1 px-2 mb-0 small">
                  <code>http.dodir</code> defaults to <code>"yes"</code> &mdash; no need to set it explicitly for this mode.
                </div>
              </div>
            </div>
          </div>

          <%-- Mode B: Structured data --%>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed small py-2 fw-semibold" type="button"
                      data-bs-toggle="collapse" data-bs-target="#hgl-structured">
                <span class="badge bg-primary rounded-pill me-2">B</span>
                Structured data &mdash; CSV / JSON / XML / STAC
              </button>
            </h2>
            <div id="hgl-structured" class="accordion-collapse collapse"
                 data-bs-parent="#httpGuideListingAcc">
              <div class="accordion-body py-2 px-3">
                <p class="small mb-2">URL returns a machine-readable manifest (e.g. a NASA CMR CSV, an S3 inventory, a STAC catalog). Set <code>http.parser</code> and map columns/fields via <code>http.parserOptions</code>.</p>

                <p class="small fw-semibold mb-1">CSV</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.dodir = "yes"
http.parser = "csv"
# parserOptions keys (comma-separated key=value):
#   nameCol   -> 0-based column index for the file name  (default: 0)
#   urlCol    -> 0-based column index for the download URL
#   timeCol   -> 0-based column index for the timestamp  (default: disabled)
#   delimiter -> field separator                          (default: ,)
#   header    -> skip the first (header) row             (default: true)
http.parserOptions = "nameCol=0,urlCol=4,timeCol=2"

# Example - NASA CMR granules.csv:
# Granule UR | Producer ID | Start Time | End Time | Online Access URLs | ...
#  col 0        col 1         col 2        col 3       col 4</pre>

                <p class="small fw-semibold mb-1">JSON</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.parser = "json"
# Defaults: arrayPath=items, nameField=name, urlField=url, timeField=time
http.parserOptions = "arrayPath=results,nameField=filename,urlField=downloadUrl,timeField=updatedAt"</pre>

                <p class="small fw-semibold mb-1">STAC (SpatioTemporal Asset Catalog)</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.parser = "stac"
# Defaults: arrayPath=features, nameField=id
#           urlField=assets.data.href, timeField=properties.start_datetime
http.parserOptions = "arrayPath=features,nameField=id,urlField=assets.data.href,timeField=properties.start_datetime"</pre>

                <p class="small fw-semibold mb-1">XML</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">http.parser = "xml"
# Defaults: arrayPath=items.item, nameField=name, urlField=url, timeField=time
http.parserOptions = "arrayPath=catalog.file,nameField=name,urlField=href"</pre>

                <div class="alert alert-light border py-1 px-2 mb-0 small">
                  For REST API URLs containing <code>?</code> parameters, set <code>http.urldir = "no"</code>
                  to prevent a trailing slash from being appended automatically.
                </div>
              </div>
            </div>
          </div>

          <%-- Mode C: Single file --%>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed small py-2 fw-semibold" type="button"
                      data-bs-toggle="collapse" data-bs-target="#hgl-urlisfile">
                <span class="badge bg-primary rounded-pill me-2">C</span>
                Single file URL
              </button>
            </h2>
            <div id="hgl-urlisfile" class="accordion-collapse collapse"
                 data-bs-parent="#httpGuideListingAcc">
              <div class="accordion-body py-2 px-3">
                <p class="small mb-2">The Directory URL <em>is</em> the file to download. One HEAD (or GET) is issued to obtain size and modification time; the result is a single listing entry.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.urlIsFile = "yes"
http.urlIsFileName = "data.nc"   # optional: override the stored file name
http.useHead = "yes"             # HEAD to get size &amp; date (recommended)</pre>
                <div class="alert alert-light border py-1 px-2 mb-0 small">
                  Combine with a JavaScript Directory that builds a date-based URL dynamically.
                </div>
              </div>
            </div>
          </div>

          <%-- Mode D: No fetch --%>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed small py-2 fw-semibold" type="button"
                      data-bs-toggle="collapse" data-bs-target="#hgl-dodir-no">
                <span class="badge bg-primary rounded-pill me-2">D</span>
                URL as-is &mdash; no listing fetch
              </button>
            </h2>
            <div id="hgl-dodir-no" class="accordion-collapse collapse"
                 data-bs-parent="#httpGuideListingAcc">
              <div class="accordion-body py-2 px-3">
                <p class="small mb-2">Adds the URL directly to the listing without fetching its content during the LIST phase. The URL is downloaded during the subsequent GET phase.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.dodir = "no"    # add URL to listing without fetching content
http.useHead = "no"  # no HEAD request during listing</pre>
                <div class="alert alert-light border py-1 px-2 mb-0 small">
                  Commonly used with a JavaScript Directory that generates multiple URL lines
                  &mdash; each line becomes one listing entry.
                </div>
              </div>
            </div>
          </div>

          <%-- Mode E: MQTT --%>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed small py-2 fw-semibold" type="button"
                      data-bs-toggle="collapse" data-bs-target="#hgl-mqtt">
                <span class="badge bg-primary rounded-pill me-2">E</span>
                MQTT broker &mdash; real-time notifications
              </button>
            </h2>
            <div id="hgl-mqtt" class="accordion-collapse collapse"
                 data-bs-parent="#httpGuideListingAcc">
              <div class="accordion-body py-2 px-3">
                <p class="small mb-2">Subscribes to an MQTT broker. The <strong>Directory</strong> field is the <em>topic filter</em> (e.g. <code>cache/+/data/core/weather/#</code>). Each received message is processed by the JavaScript editor to extract file metadata.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.mqttMode = "yes"
http.mqttScheme = "ssl"                    # ssl | tcp  (default: ssl)
http.mqttPort = "8883"                     # broker port (default: 8883)
http.mqttAwait = "PT30M"                   # how long to listen per cycle
http.mqttCleanStart = "yes"                # fresh session (no backlog on reconnect)
http.mqttKeepAliveInterval = "PT30S"       # PING interval
http.mqttConnectionTimeout = "PT30S"       # connect timeout
http.mqttMaxFiles = "2000000"              # max notifications per cycle
http.mqttSubscriberId = "my-client-id"     # MQTT client ID (auto-generated if absent)
http.mqttQos = "1"                         # QoS level: 0, 1 or 2

# Durable subscriptions (messages buffered across restarts):
http.mqttPersistence = "yes"
http.mqttPersistenceMode = "file"          # file | memory
http.mqttPersistenceDirectory = "/tmp/mqtt"
http.mqttSessionExpiryInterval = "1d"      # how long broker retains the session</pre>

                <p class="small fw-semibold mb-1">Fields extracted from each MQTT message</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.mqttHref = "..."            # download URL
http.mqttSize = "..."            # file size in bytes
http.mqttTime = "..."            # timestamp (epoch ms)
http.mqttBody = "..."            # inline payload (stored as file content)
http.mqttAddPayload = "yes"      # attach raw MQTT payload bytes as the file
http.mqttAlternativeName = "..." # override the stored file name</pre>

                <p class="small fw-semibold mb-1">JavaScript editor example (WIS2 / Global Broker)</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">// mqttPayload (parsed JSON) and mqttTopic are injected automatically
const link = mqttPayload.links.find(l =&gt; l.rel === 'canonical');
if (link) {
  return { http: {
    mqttHref:            link.href,
    mqttSize:            link.length,
    mqttTime:            Date.parse(mqttPayload.properties.pubtime),
    mqttAlternativeName: mqttPayload.properties.data_id.split('/').pop()
  }};
}</pre>
              </div>
            </div>
          </div>

        </div><%-- end accordion --%>

        <div class="mt-3 p-2 border rounded small">
          <p class="fw-semibold mb-1"><i class="bi bi-info-circle text-info me-1"></i>Trailing slash handling</p>
          <p class="text-muted mb-1">A <code>/</code> is auto-appended to the URL unless it contains <code>?</code> or ends with <code>.html</code> / <code>.htm</code> / <code>.txt</code>. Override with:</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.urldir = "yes"   # always append /
http.urldir = "no"    # never append /  (required for REST API URLs with ?params)</pre>
        </div>
      </div><%-- end listing tab --%>


      <%-- ================================================================
           TAB 2: AUTHENTICATION
           ================================================================ --%>
      <div class="tab-pane fade" id="hgt-auth" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1">
            <i class="bi bi-key text-warning me-1"></i>1. Static Bearer / API token
          </p>
          <p class="small text-muted mb-1">Inject a fixed token via <code>http.headers</code>. Multiple headers are separated by <code>\n</code>.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">http.headers = "Authorization: Bearer &lt;token&gt;"
http.authheader = "no"    # disable HTTP Basic auth header (not needed with Bearer)
http.credentials = "no"   # disable credential provider

# Multiple headers - use \n as the line separator:
http.headers = "Authorization: Bearer &lt;token&gt;\nX-Api-Key: &lt;key&gt;"</pre>
          <div class="alert alert-warning py-1 px-2 mb-0 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>Tokens stored here persist in the database.
            Use <strong>dynamic token refresh</strong> (below) for tokens that expire.</div>
          </div>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1">
            <i class="bi bi-person-lock text-secondary me-1"></i>2. HTTP Basic Authentication
          </p>
          <p class="small text-muted mb-1">Set <strong>Login</strong> and <strong>Password</strong> in the host <em>Identity</em> card, then enable the auth header.</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">http.authheader = "yes"   # send credentials in Authorization: Basic header
http.authcache = "yes"    # pre-emptively send credentials (skip 401 round-trip)
http.credentials = "yes"  # load credentials into the provider</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1">
            <i class="bi bi-arrow-repeat text-success me-1"></i>3. Dynamic token refresh (JavaScript)
          </p>
          <p class="small text-muted mb-1">For tokens with limited lifetimes (OAuth 2, NASA Earthdata, etc.). Define a function in the <strong>JavaScript</strong> editor; the engine calls it automatically when the cached token is near expiry.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap"># In the Properties editor:
http.tokenFunction = "getAuthToken"   # JS function to call  (default: getAuthToken)
http.tokenHeader = "Authorization"    # which header to update with the new token
http.tokenLookahead = "60"            # refresh N seconds before the token expires
# The following two are managed automatically - do not set them manually:
# http.tokenValue  = ""              # current token value (written back after refresh)
# http.tokenExpiry = "-1"            # token expiry epoch-ms (written back after refresh)</pre>
          <p class="small fw-semibold mb-1">In the JavaScript editor:</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">function getAuthToken() {
  // Available helpers:
  //   setup.get("http.login")     -> host Login field value
  //   setup.get("http.password")  -> host Password field value
  //   setup.get("http.basicAuth") -> "Basic &lt;base64(login:password)&gt;"
  //   http.get(url, headers)      -> { status: 200, body: "..." }
  //   http.post(url, headers, body) -> { status: 200, body: "..." }

  var resp = http.post(
    "https://auth.example.com/oauth/token",
    { "Content-Type": "application/x-www-form-urlencoded" },
    "grant_type=client_credentials&amp;client_id=X&amp;client_secret=Y"
  );
  var data = JSON.parse(resp.body);
  return {
    token:     "Bearer " + data.access_token,
    expiresAt: Date.now() + data.expires_in * 1000   // epoch ms
  };
}</pre>
          <div class="alert alert-light border py-1 px-2 mb-0 small">
            The function must return <code>&#123; token: "...", expiresAt: &lt;epoch ms&gt; &#125;</code>.
            The returned token replaces the matching header line in <code>http.headers</code>
            and is persisted automatically.
          </div>
        </div>

      </div><%-- end auth tab --%>


      <%-- ================================================================
           TAB 3: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade" id="hgt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-primary me-1"></i>Scheme, host &amp; port</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.scheme = "https"   # http | https  (default: http)
http.port = "443"       # port (default: 80)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-shield-check text-success me-1"></i>SSL / TLS</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.sslValidation = "no"              # skip certificate validation (dev / self-signed only)
http.protocol = "TLS"                  # SSL context protocol: TLS | TLSv1.2 | TLSv1.3
http.supportedProtocols = "TLSv1.2"   # comma-separated allowed protocols
http.strict = "no"                     # strict hostname verification</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-left-right text-secondary me-1"></i>Proxy</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.proxy = "http://proxy.example.com:8080"
# Leave unset (or "none") to connect directly</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-repeat text-info me-1"></i>Redirects &amp; compression</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.maxRedirects = "5"               # max redirects to follow (default: 5)
http.allowCircularRedirects = "no"    # allow repeated redirects to the same URL
http.enableContentCompression = "yes" # accept gzip / deflate responses</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-link-45deg text-muted me-1"></i>URL encoding &amp; path handling</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.encodeURL = "no"       # percent-encode special characters in the URL path
http.hasParameters = "no"   # treat the URL query string as part of the file path
                            # (use "yes" when query params must survive normalisation)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-upload text-warning me-1"></i>Upload settings <span class="fw-normal text-muted">(dissemination / PUT)</span></p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">http.uploadEndPoint = "/upload"     # alternative PUT / POST endpoint path
http.usePost = "no"                 # use POST instead of PUT for uploads
http.useMultipart = "no"            # use multipart/form-data encoding
http.multipartMode = "LEGACY"       # LEGACY | STRICT | EXTENDED
http.filenameAttribute = "filename" # multipart field name for the filename</pre>
        </div>

      </div><%-- end connection tab --%>


      <%-- ================================================================
           TAB 4: REGISTRATION
           ================================================================ --%>
      <div class="tab-pane fade" id="hgt-registration" role="tabpanel">

        <p class="small text-muted mb-2">After the LIST phase each discovered URL is evaluated by the acquisition engine. These options control which files are queued and how they are stored.</p>

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-funnel text-primary me-1"></i>File age filter</p>
          <p class="small text-muted mb-1">A JavaScript boolean expression. Variables: <code>$age</code> (ms since modification), <code>$time1</code> / <code>$time2</code> (remote / local timestamps).</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">acquisition.fileage = "$age &lt; 7d"
# Accept files modified within the last 7 days

acquisition.fileage = "$age &gt; (5*60*1000) &amp;&amp; $age &lt; (20*24*60*60*1000)"
# Accept files between 5 minutes and 20 days old</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-clock-history text-secondary me-1"></i>Lifetime &amp; metadata</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">acquisition.lifetime = "PT168H"
# How long to retain the file in OpenECPDS (ISO-8601 duration = 7 days)

acquisition.metadata = "targetname=$destination/data/$target[11]"
# Compute the target path/name.
# Supported tokens: $target[start..end], $date[dateformat=...],
#                   $destination, $link

acquisition.target = "$link"
# Use the listing URL as the file's target name (useful with MQTT)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-clockwise text-success me-1"></i>Requeue behaviour</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">acquisition.requeueonupdate = "yes"         # requeue if remote file has changed
acquisition.requeueonsamesize = "no"        # requeue even if size is identical
acquisition.requeueOnFailure = "yes"        # requeue after a retrieval failure
acquisition.requeueon = "$time2 &gt; $time1"  # custom requeue condition expression</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-toggles text-info me-1"></i>Queue control</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">acquisition.standby = "no"           # queue immediately (yes = only on explicit demand)
acquisition.listSynchronous = "no"   # run listing async (yes = wait for full list)
acquisition.maximumDuration = "PT24H" # max wall-clock time for one acquisition cycle
acquisition.priority = "99"          # transfer queue priority (higher = earlier)
acquisition.uniqueByTargetOnly = "yes" # deduplicate by target name only (ignore URL)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-speedometer2 text-warning me-1"></i>Retrieval tuning</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">retrieval.minimumRate = "100B"          # abort if transfer rate drops below this
retrieval.maximumDuration = "PT1H40M"   # abort if a single file takes longer
retrieval.interruptSlow = "yes"         # enable the slow-transfer kill switch</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-link text-muted me-1"></i>Symlinks &amp; inline payload <span class="fw-normal text-muted">(MQTT)</span></p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">acquisition.useSymlink = "yes"         # record as symlink (typical for MQTT flows)
acquisition.payloadExtension = ".json" # append extension to inline payload files
http.mqttAddPayload = "yes"            # use MQTT message body as the file content</pre>
        </div>

      </div><%-- end registration tab --%>

    </div><%-- end tab-content --%>
  </div><%-- end offcanvas-body --%>
</div><%-- end offcanvas --%>
