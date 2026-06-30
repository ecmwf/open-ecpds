<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%-- Acquisition Console - How-it-works & options guide --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="acquisitionGuideOffcanvas"
     aria-labelledby="acquisitionGuideLabel" style="width:min(760px,72vw);">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="acquisitionGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Acquisition Host &mdash; Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>All options use the <code>acquisition.</code> prefix in the host <em>Properties</em> field.
      Most options can also be set per directory line in the <code>[options]</code> prefix block,
      in which case they override the global host setting for that line only.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="acqGuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#acqgt-how" type="button" role="tab">
          <i class="bi bi-diagram-3 me-1"></i>How it works
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#acqgt-dir" type="button" role="tab">
          <i class="bi bi-folder2-open me-1"></i>Directory config
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#acqgt-select" type="button" role="tab">
          <i class="bi bi-funnel me-1"></i>File selection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#acqgt-sched" type="button" role="tab">
          <i class="bi bi-calendar-check me-1"></i>Scheduling
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#acqgt-adv" type="button" role="tab">
          <i class="bi bi-gear me-1"></i>Advanced
        </button>
      </li>
    </ul>

    <div class="tab-content" id="acqGuideTabContent">

      <%-- ================================================================
           TAB 1: HOW IT WORKS
           ================================================================ --%>
      <div class="tab-pane fade show active" id="acqgt-how" role="tabpanel">

        <p class="small fw-semibold mb-1"><i class="bi bi-arrow-repeat text-primary me-1"></i>Scheduler cycle</p>
        <p class="small text-muted mb-2">The <strong>AcquisitionScheduler</strong> runs continuously inside the MasterServer. On each cycle it queries every destination that has acquisition enabled, then iterates over the Acquisition hosts associated with those destinations. For each (destination, host) pair that is not already running, it spawns an <strong>AcquisitionThread</strong> to process that host.</p>
        <p class="small text-muted mb-2">A host is only picked up when its <em>Acquisition Time</em> has elapsed since the last run. The thread count is capped by <code>Scheduler.maxAcquisitionThreads</code> (default 100). If the cap is reached the scheduler waits until threads finish before starting new ones.</p>

        <p class="small fw-semibold mt-3 mb-1"><i class="bi bi-cpu text-primary me-1"></i>AcquisitionThread lifecycle</p>
        <ol class="small text-muted mb-2 ps-3">
          <li class="mb-1"><strong>Read the Directory field.</strong> Each non-empty line is a <em>listing spec</em>. If the entire content is wrapped in <code>$(...)</code> it is evaluated as a script on a DataMover first; the output of the script is then treated as the listing spec lines.</li>
          <li class="mb-1"><strong>Parse each line</strong> — extract inline <code>[options]</code>, trailing <code>{regex}</code> filename filter, <code>$date</code> tokens, and optional wildcard filter — and build the final remote path.</li>
          <li class="mb-1"><strong>List the remote directory</strong> via a DataMover using the configured transfer module (FTP, SFTP, S3, HTTP, …). The raw listing output is stored in the <em>Directory Listings</em> panel.</li>
          <li class="mb-1"><strong>Parse each file entry</strong> using an FTP-style parser (<code>systemKey</code>, date formats, language code). Each entry is checked against the file selection filters.</li>
          <li class="mb-1"><strong>Act on matched entries</strong>: schedule them for retrieval (<code>action=queue</code>) or delete them from the remote server (<code>action=delete</code>).</li>
          <li class="mb-1"><strong>Write progress</strong> to the <em>Progress</em> panel (the console log above) after each listing step.</li>
        </ol>

        <p class="small fw-semibold mt-3 mb-1"><i class="bi bi-terminal text-primary me-1"></i>Progress panel vs Directory Listings</p>
        <table class="table table-sm table-bordered small mb-0">
          <thead class="table-light"><tr><th>Panel</th><th>Shows</th></tr></thead>
          <tbody>
            <tr><td><strong>Progress</strong> (top)</td><td>Console log: connection attempts, timing, per-file outcome (<em>selected / not-selected / error</em>), summary counts</td></tr>
            <tr><td><strong>Directory Listings</strong> (below)</td><td>Raw file listing from the remote server, parsed into structured blocks per path</td></tr>
          </tbody>
        </table>

      </div>

      <%-- ================================================================
           TAB 2: DIRECTORY CONFIGURATION
           ================================================================ --%>
      <div class="tab-pane fade" id="acqgt-dir" role="tabpanel">

        <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Line syntax</p>
        <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">[option1=value1;option2=value2]/path/to/$date/{filename-regex}</pre>
        <p class="small text-muted mb-2">Each non-empty line in the <em>Directory</em> field describes one remote path to list. All parts except the path are optional.</p>

        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Part</th><th>Example</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>[options]</code></td><td><code>[filesize=&gt;&gt;1000;fileage=&lt;&lt;24h]</code></td><td>Per-line option overrides, separated by <code>;</code> or <code>,</code>. Override host-level <code>acquisition.*</code> settings for this line only.</td></tr>
              <tr><td>path</td><td><code>/data/feed/$date/</code></td><td>Remote directory path. Supports <code>$date</code>, <code>$dirdate</code>, and <code>$host[field]</code> tokens.</td></tr>
              <tr><td><code>{regex}</code></td><td><code>{.*\.nc}</code></td><td>Java regex matched against each filename. Only entries whose name matches are selected. Omit to match all files.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-calendar-date text-primary me-1"></i>Date tokens</p>
        <table class="table table-sm table-bordered small mb-3">
          <thead class="table-light"><tr><th>Token</th><th>Description</th></tr></thead>
          <tbody>
            <tr><td><code>$date</code></td><td>Replaced with the current date/time formatted according to <code>acquisition.dateformat</code> (default <code>yyyyMMdd</code>) and shifted by <code>acquisition.datedelta</code>.</td></tr>
            <tr><td><code>$date[dateformat=…,datedelta=…]</code></td><td>Per-token overrides. E.g. <code>$date[dateformat=yyyy/MM,datedelta=-1d]</code> gives yesterday's year/month.</td></tr>
            <tr><td><code>$date[datesource=…,datepattern=…]</code></td><td>Parse date from an external string instead of using the current time. <code>datesource</code> is the string, <code>datepattern</code> is the Java <code>SimpleDateFormat</code> pattern to parse it.</td></tr>
            <tr><td><code>$dirdate</code></td><td>Same as <code>$date</code> but uses options from the line options block (<code>[...]</code>) rather than the global host settings.</td></tr>
            <tr><td><code>$timestamp</code></td><td>Replaced with the file's modification time in milliseconds (Unix epoch, rounded to seconds). Available in <code>acquisition.target</code>, <code>acquisition.metadata</code> etc.</td></tr>
          </tbody>
        </table>

        <p class="small fw-semibold mb-1"><i class="bi bi-braces text-primary me-1"></i>Host field tokens</p>
        <p class="small text-muted mb-1">Available in the Directory field for dynamic path construction:</p>
        <code class="small">$host[name]</code>&nbsp;
        <code class="small">$host[nickname]</code>&nbsp;
        <code class="small">$host[host]</code>&nbsp;
        <code class="small">$host[login]</code>&nbsp;
        <code class="small">$host[comment]</code>&nbsp;
        <code class="small">$host[userMail]</code>&nbsp;
        <code class="small">$host[networkCode]</code>&nbsp;
        <code class="small">$host[networkName]</code>&nbsp;
        <code class="small">$transferMethod[name]</code>&nbsp;
        <code class="small">$ectransModule[name]</code>

        <p class="small fw-semibold mt-3 mb-1"><i class="bi bi-terminal text-primary me-1"></i>Script mode</p>
        <p class="small text-muted mb-2">If the entire Directory field content is wrapped in <code>$( ... )</code>, it is executed as a script on an allocated DataMover. The standard output of the script is then interpreted as the listing spec lines (one path per line). This allows dynamic directory list generation.</p>

        <p class="small fw-semibold mb-1"><i class="bi bi-filter text-primary me-1"></i>Wildcard filter</p>
        <p class="small text-muted mb-0">The <code>acquisition.wildcardFilter</code> option appends a glob-style wildcard to the path sent to the remote server before listing. This lets the transfer module filter server-side, reducing the number of entries returned. The <code>acquisition.regexPattern</code> option applies an additional server-side regex filter at the listing call level.</p>

      </div>

      <%-- ================================================================
           TAB 3: FILE SELECTION
           ================================================================ --%>
      <div class="tab-pane fade" id="acqgt-select" role="tabpanel">

        <p class="small fw-semibold mb-1"><i class="bi bi-funnel text-primary me-1"></i>Selection pipeline</p>
        <p class="small text-muted mb-2">Each file entry returned by the remote listing passes through the following checks in order. A file is <em>selected</em> only if all checks pass.</p>

        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Check</th><th>Option(s)</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td>Entry type</td><td><code>acquisition.useSymlink</code></td><td><code>false</code></td><td>Directories and entries of unknown type are always skipped. Symbolic links are skipped unless <code>useSymlink=true</code>.</td></tr>
              <tr><td>Date parseable</td><td><code>acquisition.onlyValidTime</code></td><td><code>false</code></td><td>If <code>true</code>, entries whose modification date could not be parsed are rejected.</td></tr>
              <tr><td>Name pattern</td><td><em>(from <code>{regex}</code> in line)</em></td><td>match all</td><td>The filename is matched against the Java regex in the <code>{...}</code> suffix of the directory line. Non-matching entries are skipped.</td></tr>
              <tr><td>File size</td><td><code>acquisition.filesize</code></td><td>no limit</td><td>Comparison expression: operator + value. Operators: <code>==</code> <code>!=</code> <code>&gt;&gt;</code> <code>&lt;&lt;</code> <code>&gt;=</code> <code>&lt;=</code>. Value can use units (e.g. <code>1MB</code>). Can also be a JS expression referencing <code>$size</code> (bytes). Symlinks bypass this check.</td></tr>
              <tr><td>File age</td><td><code>acquisition.fileage</code></td><td>no limit</td><td>Same operator syntax but compared against the file's age (current time minus modification time). Value is a duration (e.g. <code>24h</code>, <code>7d</code>). Can also be a JS expression with <code>$age</code> (milliseconds). Skipped if file time is unknown (<code>-1</code>).</td></tr>
              <tr><td>URL params</td><td><code>acquisition.removeParameters</code></td><td><code>false</code></td><td>For HTTP/URL entries only: if <code>true</code>, the query string (<code>?...</code>) is stripped from the filename when computing the target name.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-calendar3 text-primary me-1"></i>Listing parser options</p>
        <p class="small text-muted mb-1">When the transfer module returns a raw FTP-style listing, the following options control how it is parsed:</p>
        <div class="table-responsive mb-0">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.systemKey</code></td><td><code>UNIX</code></td><td>Server OS/format. Values: <code>UNIX</code>, <code>VMS</code>, <code>WINDOWS</code>, <code>OS/2</code>, <code>OS/400</code>, <code>AS/400</code>, <code>MVS</code>, <code>NETWARE</code>, <code>MACOS PETER</code>.</td></tr>
              <tr><td><code>acquisition.regexFormat</code></td><td>—</td><td>Custom regex to parse non-standard listing lines. Overrides the <code>systemKey</code> format.</td></tr>
              <tr><td><code>acquisition.defaultDateFormat</code></td><td>—</td><td>Java <code>SimpleDateFormat</code> pattern for entries that include full date+time (e.g. <code>yyyy-MM-dd HH:mm</code>).</td></tr>
              <tr><td><code>acquisition.recentDateFormat</code></td><td>—</td><td>Pattern for entries that only include month/day/time (no year), used when the file is recent.</td></tr>
              <tr><td><code>acquisition.serverLanguageCode</code></td><td><code>en</code></td><td>ISO 639-1 language code for month name parsing (e.g. <code>fr</code>, <code>de</code>).</td></tr>
              <tr><td><code>acquisition.shortMonthNames</code></td><td>—</td><td>Comma-separated custom short month names, overriding the language-specific defaults.</td></tr>
              <tr><td><code>acquisition.serverTimeZoneId</code></td><td>—</td><td>Java timezone ID for interpreting listing timestamps (e.g. <code>UTC</code>, <code>America/New_York</code>).</td></tr>
            </tbody>
          </table>
        </div>

      </div>

      <%-- ================================================================
           TAB 4: SCHEDULING
           ================================================================ --%>
      <div class="tab-pane fade" id="acqgt-sched" role="tabpanel">

        <p class="small fw-semibold mb-1"><i class="bi bi-play-circle text-primary me-1"></i>Action</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.action</code></td><td><code>queue</code></td><td><code>queue</code> — register the file in the database for retrieval by the AcqDownloadScheduler. <code>delete</code> — delete the matched file from the remote server (no scheduling).</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-key text-primary me-1"></i>Uniqueness &amp; deduplication</p>
        <p class="small text-muted mb-1">Before scheduling, the scheduler checks whether the file already exists in the database using a <em>unique key</em> built from the destination name, effective target filename, and optionally the modification time.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.uniqueByTargetOnly</code></td><td><code>false</code></td><td>If <code>true</code>, the unique key is the target filename only (ignoring the original full path). Two files with the same target name are treated as the same file regardless of their remote location.</td></tr>
              <tr><td><code>acquisition.useTargetAsUniqueName</code></td><td><code>false</code></td><td>Use the <em>initial</em> target (before <code>acquisition.target</code> substitution) as the uniqueness key rather than the original full path.</td></tr>
              <tr><td><code>acquisition.uniqueByNameAndTime</code></td><td><code>false</code></td><td>Append the file modification timestamp to the unique key so that the same filename with a different mtime is treated as a new file.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-arrow-clockwise text-primary me-1"></i>Re-queueing</p>
        <p class="small text-muted mb-1">If a file already exists in the database, the scheduler evaluates whether it should be re-scheduled.</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.requeueon</code></td><td>—</td><td>A JavaScript expression that evaluates to <code>true</code> to re-queue. Variables: <code>$time1</code>/<code>$size1</code> (existing DB record), <code>$time2</code>/<code>$size2</code> (new remote values, in ms and bytes), <code>$destination</code>, <code>$target</code>, <code>$original</code>. Example: <code>$time2 &gt; $time1 &amp;&amp; $size2 != $size1</code></td></tr>
              <tr><td><code>acquisition.requeueonupdate</code></td><td><code>false</code></td><td>Legacy shortcut: re-queue when the remote modification time is newer than the stored one. Ignored if <code>requeueon</code> is set.</td></tr>
              <tr><td><code>acquisition.requeueonsamesize</code></td><td><code>false</code></td><td>Legacy shortcut: when combined with <code>requeueonupdate</code>, also re-queue when size differs. Ignored if <code>requeueon</code> is set.</td></tr>
              <tr><td><code>acquisition.requeueOnFailure</code></td><td><code>false</code></td><td>If <code>true</code> and a retrieval fails, put the transfer back to <code>SCHE</code> (re-scheduled) rather than marking it <code>FAIL</code>. Allows automatic retry on next acquisition cycle.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-file-earmark-text text-primary me-1"></i>Target &amp; metadata</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.target</code></td><td><code>$target</code></td><td>Template for the stored filename. Tokens: <code>$target</code> (initial filename), <code>$name</code> (basename without path), <code>$original</code> (full remote path), <code>$link</code> (symlink target if any), <code>$destination</code>, <code>$date</code>, <code>$dirdate</code>, <code>$timestamp</code> (file mtime in ms, rounded to seconds).</td></tr>
              <tr><td><code>acquisition.metadata</code></td><td><em>(empty)</em></td><td>Arbitrary metadata string stored with the scheduled transfer. Supports the same tokens as <code>target</code>. Can be used downstream by dissemination rules.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-sliders text-primary me-1"></i>Retrieval parameters</p>
        <div class="table-responsive mb-0">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.priority</code></td><td><code>99</code></td><td>Scheduling priority for the queued transfer (lower number = higher priority).</td></tr>
              <tr><td><code>acquisition.lifetime</code></td><td><code>2d</code></td><td>How long the scheduled transfer is kept alive in the database before expiry.</td></tr>
              <tr><td><code>acquisition.standby</code></td><td><code>false</code></td><td>If <code>true</code>, the transfer is created in standby mode and will not be retrieved until explicitly released.</td></tr>
              <tr><td><code>acquisition.groupby</code></td><td><em>auto</em></td><td>Group key used to batch transfers together (default: <code>ACQ_{destination}_{host}</code>).</td></tr>
              <tr><td><code>acquisition.transferGroup</code></td><td>—</td><td>Override the transfer group used for the retrieval DataMover selection.</td></tr>
              <tr><td><code>acquisition.event</code></td><td><code>false</code></td><td>If <code>true</code>, trigger an event notification when the file is scheduled.</td></tr>
              <tr><td><code>acquisition.noretrieval</code></td><td><code>false</code></td><td>Register the transfer in the database but skip the actual retrieval (metadata-only scheduling).</td></tr>
              <tr><td><code>acquisition.deleteoriginal</code></td><td><code>false</code></td><td>Delete the remote file after it has been successfully retrieved.</td></tr>
              <tr><td><code>acquisition.payloadExtension</code></td><td><code>.payload</code></td><td>File extension appended to notification payload filenames when a message body is included in the listing entry.</td></tr>
            </tbody>
          </table>
        </div>

      </div>

      <%-- ================================================================
           TAB 5: ADVANCED
           ================================================================ --%>
      <div class="tab-pane fade" id="acqgt-adv" role="tabpanel">

        <p class="small fw-semibold mb-1"><i class="bi bi-speedometer text-primary me-1"></i>Performance &amp; parallelism</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.listSynchronous</code></td><td><code>true</code></td><td>If <code>true</code>, the remote listing call blocks until the full directory listing is received before parsing begins. If <code>false</code>, listing and parsing run in a pipeline (listing data is streamed as it arrives).</td></tr>
              <tr><td><code>acquisition.listParallel</code></td><td><code>false</code></td><td>If <code>true</code>, individual file entries within a single listing are processed in parallel using a thread pool (speeds up database checks and inserts for large directories).</td></tr>
              <tr><td><code>acquisition.listMaxWaiting</code></td><td><code>100</code></td><td>Maximum number of file entries queued for parallel processing before the listing reader pauses.</td></tr>
              <tr><td><code>acquisition.listMaxThreads</code></td><td><code>100</code></td><td>Maximum number of parallel threads used when <code>listParallel=true</code>.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-alarm text-primary me-1"></i>Timeout &amp; interruption</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.maximumDuration</code></td><td><em>server default (10 min)</em></td><td>Per-host maximum run time. If the AcquisitionThread runs longer than this it is automatically interrupted. Overrides the server-wide <code>Scheduler.maximumDurationAcquisitionThread</code> setting for this host.</td></tr>
              <tr><td><code>acquisition.interruptSlow</code></td><td><em>server default</em></td><td>Override the server-wide <code>Scheduler.interruptSlowAcquisitionThread</code> flag for this host. Set to <code>false</code> to allow this host to exceed the maximum duration without being killed.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-search text-primary me-1"></i>Pattern matching</p>
        <div class="table-responsive mb-3">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.wildcardFilter</code></td><td><em>(empty)</em></td><td>Glob-style string appended to the path before sending the list request to the remote server. Reduces the number of entries the DataMover returns. E.g. <code>*.nc</code> sends <code>/data/feed/20240101/*.nc</code> to the server.</td></tr>
              <tr><td><code>acquisition.regexPattern</code></td><td><em>(empty)</em></td><td>Regex passed to the DataMover to filter entries at the listing call level (before the AcquisitionThread receives them). Complements the per-line <code>{regex}</code> filter.</td></tr>
              <tr><td><code>acquisition.skipPostRetrievalSizeCheckPattern</code></td><td>—</td><td>Regex matched against the source filename during retrieval. If it matches, the post-retrieval size verification step is skipped. Useful for servers that report incorrect file sizes.</td></tr>
            </tbody>
          </table>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-bug text-primary me-1"></i>Diagnostics</p>
        <div class="table-responsive mb-0">
          <table class="table table-sm table-bordered small mb-0">
            <thead class="table-light"><tr><th>Option</th><th>Default</th><th>Description</th></tr></thead>
            <tbody>
              <tr><td><code>acquisition.debug</code></td><td><code>false</code></td><td>If <code>true</code>, each parsed file entry and option is also written to the MasterServer debug log in addition to the Progress panel.</td></tr>
            </tbody>
          </table>
        </div>

      </div>

    </div><%-- /tab-content --%>
  </div><%-- /offcanvas-body --%>
</div>
