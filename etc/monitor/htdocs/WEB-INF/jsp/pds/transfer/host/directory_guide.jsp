<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%-- actionFormName is passed as a jsp:param by the including page --%>

<%-- Directory field guide offcanvas — content is specific to the host type --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="directoryGuideOffcanvas"
     aria-labelledby="directoryGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="directoryGuideLabel">
      <i class="bi bi-folder2-open me-2 text-info"></i>Directory &mdash; Guide
      <small class="text-muted fw-normal ms-1">(${requestScope[param.actionFormName].type})</small>
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <c:choose>

      <%-- ================================================================
           ACQUISITION
           ================================================================ --%>
      <c:when test="${requestScope[param.actionFormName].type == 'Acquisition'}">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
          <div>For <strong>Acquisition</strong> hosts the Directory field is a listing specification:
          each line describes one remote directory to scan for files to retrieve. The entire field
          may also be a <strong>JavaScript</strong> or <strong>Python</strong> script that returns
          the listing lines dynamically at runtime. The script runs on a DataMover via
          <code>TransferScheduler.execution()</code>.</div>
        </div>

        <ul class="nav nav-tabs nav-fill mb-3" id="dirGuideTabAcq" role="tablist">
          <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#dg-acq-plain" type="button"><i class="bi bi-list-ul me-1"></i>Plain Text</button></li>
          <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dg-acq-script" type="button"><i class="bi bi-terminal me-1"></i>Script Mode</button></li>
          <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dg-acq-vars" type="button"><i class="bi bi-braces me-1"></i>Variables</button></li>
        </ul>

        <div class="tab-content">

          <div class="tab-pane fade show active" id="dg-acq-plain" role="tabpanel">
            <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Line syntax</p>
            <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">[option1=value1;option2=value2]/path/to/$date/{filename-regex}</pre>
            <p class="small text-muted mb-2">Each non-empty line describes one remote directory to scan. All parts except the path are optional. Blank lines and lines starting with <code>#</code> are ignored.</p>

            <div class="table-responsive mb-3">
              <table class="table table-sm table-bordered small mb-0">
                <thead class="table-light"><tr><th>Part</th><th>Example</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td><code>[options]</code></td><td><code>[filesize=&gt;1000;fileage=&lt;24h]</code></td><td>Per-line option overrides (separated by <code>;</code> or <code>,</code>). Override host-level <code>acquisition.*</code> properties for this line only.</td></tr>
                  <tr><td>path</td><td><code>/data/feed/$date/</code></td><td>Remote directory path. Supports <code>$date</code>, <code>$dirdate</code>, and <code>$host[field]</code> tokens (substituted before execution).</td></tr>
                  <tr><td><code>{regex}</code></td><td><code>{.*\.nc}</code></td><td>Java regex matched against each filename. Only matching entries are selected. Omit to match all files.</td></tr>
                </tbody>
              </table>
            </div>

            <p class="small fw-semibold mb-1"><i class="bi bi-calendar-date text-primary me-1"></i>Date tokens</p>
            <table class="table table-sm table-bordered small mb-3">
              <thead class="table-light"><tr><th>Token</th><th>Description</th></tr></thead>
              <tbody>
                <tr><td><code>$date</code></td><td>Current date formatted by <code>acquisition.dateformat</code> (default <code>yyyyMMdd</code>), shifted by <code>acquisition.datedelta</code>.</td></tr>
                <tr><td><code>$date[dateformat=…,datedelta=…]</code></td><td>Per-token overrides. E.g. <code>$date[dateformat=yyyy/MM,datedelta=-1d]</code> gives yesterday's year/month.</td></tr>
                <tr><td><code>$date[datesource=…,datepattern=…]</code></td><td>Parse a date from an external string. <code>datesource</code> is the string; <code>datepattern</code> is the Java <code>SimpleDateFormat</code> pattern.</td></tr>
                <tr><td><code>$dirdate</code></td><td>Same as <code>$date</code> but reads format/delta from the line's <code>[options]</code> block instead of global host settings.</td></tr>
              </tbody>
            </table>

            <p class="small fw-semibold mb-1"><i class="bi bi-card-text text-primary me-1"></i>Example</p>
            <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">/data/ecmwf/$date/{.*\.grib2}
[fileage=&lt;6h]/data/ecmwf/$date[datedelta=-1d]/{.*\.grib2}
# older data with size filter
[filesize=&gt;1024;fileage=&lt;48h]/archive/$date[dateformat=yyyy/MM/dd]/</pre>
          </div>

          <div class="tab-pane fade" id="dg-acq-script" role="tabpanel">
            <div class="alert alert-secondary py-2 px-3 mb-3 small">
              Select <strong>JavaScript</strong> or <strong>Python</strong> mode using the radio
              buttons above the editor — the wrapper is added automatically on save. The script
              runs on an allocated DataMover and its <strong>standard output</strong> is interpreted
              as the listing specification — one path (with optional options and regex) per line,
              exactly as in Plain Text mode. Variables (<code>$host[…]</code>,
              <code>$transferMethod[…]</code>, etc.) are substituted <em>before</em> the script runs.
            </div>

            <ul class="nav nav-pills mb-3" id="acqScriptTab" role="tablist">
              <li class="nav-item"><button class="nav-link active small py-1 px-3" data-bs-toggle="tab" data-bs-target="#dg-acq-js" type="button">JavaScript</button></li>
              <li class="nav-item"><button class="nav-link small py-1 px-3" data-bs-toggle="tab" data-bs-target="#dg-acq-py" type="button">Python</button></li>
            </ul>
            <div class="tab-content">
              <div class="tab-pane fade show active" id="dg-acq-js" role="tabpanel">
                <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">// Build directory lines dynamically — one per output line
var today = new Date();
var yyyy  = today.getFullYear();
var mm    = String(today.getMonth() + 1).padStart(2, '0');
var dd    = String(today.getDate()).padStart(2, '0');
var base  = "/data/feed/" + yyyy + "/" + mm + "/" + dd + "/";
base + "{.*\\.nc}\n" +
"[fileage=&lt;48h]" + base.replace(dd, String(today.getDate()-1).padStart(2,'0')) + "{.*\\.nc}";</pre>
              </div>
              <div class="tab-pane fade" id="dg-acq-py" role="tabpanel">
                <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">from datetime import datetime, timedelta

base = "/data/feed/"
for delta in range(3):  # today and 2 previous days
    day = datetime.utcnow() - timedelta(days=delta)
    age = delta * 24
    print("[fileage=&lt;" + str(age + 24) + "h]" + base + day.strftime("%Y/%m/%d") + "/{.*\\.nc}")</pre>
              </div>
            </div>
            <p class="small text-muted mt-2 mb-0">Use the <strong>Test on Server</strong> button to execute the script and inspect its output before saving.</p>
          </div>

          <div class="tab-pane fade" id="dg-acq-vars" role="tabpanel">
            <p class="small text-muted mb-2">These tokens are substituted before the plain-text lines are processed or before the script runs.</p>
            <p class="small fw-semibold mb-1">Host</p>
            <p class="mb-2"><code class="small me-1">$host[name]</code><code class="small me-1">$host[comment]</code><code class="small me-1">$host[host]</code><code class="small me-1">$host[login]</code><code class="small me-1">$host[passwd]</code><code class="small me-1">$host[userMail]</code><code class="small me-1">$host[networkCode]</code><code class="small me-1">$host[networkName]</code><code class="small me-1">$host[nickname]</code></p>
            <p class="small fw-semibold mb-1">Transfer Method &amp; Module</p>
            <p class="mb-2"><code class="small me-1">$transferMethod[name]</code><code class="small me-1">$transferMethod[comment]</code><code class="small me-1">$transferMethod[value]</code><code class="small me-1">$ectransModule[name]</code></p>
            <p class="small fw-semibold mb-1">Date</p>
            <p class="mb-0"><code class="small me-1">$date</code><code class="small me-1">$date[dateformat=…,datedelta=…]</code><code class="small me-1">$dirdate</code></p>
          </div>

        </div>

      </c:when>

      <%-- ================================================================
           DISSEMINATION
           ================================================================ --%>
      <c:when test="${requestScope[param.actionFormName].type == 'Dissemination'}">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
          <div>For <strong>Dissemination</strong> hosts the Directory field is a <strong>target-path
          template</strong> applied to every transfer. Variable tokens (<code>$host[…]</code>,
          <code>$dataFile[…]</code>, <code>$dataTransfer[…]</code>, etc.) are substituted at
          transfer time by <code>TransferManagement.getTargetName()</code>. The field may also use
          a <strong>selector syntax</strong> to choose different paths based on conditions, or be
          a <strong>script</strong> that returns the path dynamically.</div>
        </div>

        <ul class="nav nav-tabs nav-fill mb-3" id="dirGuideTabDis" role="tablist">
          <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#dg-dis-plain" type="button"><i class="bi bi-signpost me-1"></i>Path Template</button></li>
          <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dg-dis-selector" type="button"><i class="bi bi-funnel me-1"></i>Selector Syntax</button></li>
          <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dg-dis-script" type="button"><i class="bi bi-terminal me-1"></i>Script Mode</button></li>
          <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#dg-dis-vars" type="button"><i class="bi bi-braces me-1"></i>Variables</button></li>
        </ul>

        <div class="tab-content">

          <div class="tab-pane fade show active" id="dg-dis-plain" role="tabpanel">
            <p class="small text-muted mb-2">Enter a path template. All substitution variables are replaced at transfer time. If the resolved path ends with <code>/</code> (or is empty), the transfer's own target filename is appended automatically.</p>
            <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">/outgoing/$destination[name]/$dataFile[original]</pre>
            <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">/outgoing/$host[name]/</pre>
          </div>

          <div class="tab-pane fade" id="dg-dis-selector" role="tabpanel">
            <p class="small text-muted mb-2">Use <code>(condition) path</code> lines to select different target paths based on variable values. The line with the highest number of matching conditions wins. A bare line (no <code>(…)</code>) acts as the default.</p>
            <pre class="small p-2 rounded mb-2" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">($$destination[name]==ecpds) /high-priority/$dataFile[original]
($$destination[name]==archive) /archive/$dataFile[metaStream]/$dataFile[original]
/default/$dataFile[original]</pre>
            <div class="table-responsive mb-0">
              <table class="table table-sm table-bordered small mb-0">
                <thead class="table-light"><tr><th>Operator</th><th>Meaning</th><th>Example</th></tr></thead>
                <tbody>
                  <tr><td><code>==</code></td><td>Equals (or regex if second part is <code>{pattern}</code>)</td><td><code>$host[name]=={ecpds.*}</code></td></tr>
                  <tr><td><code>!=</code></td><td>Not equals</td><td><code>$destination[name]!=test</code></td></tr>
                  <tr><td><code>.=</code></td><td>Starts with</td><td><code>$dataFile[original].=/data/</code></td></tr>
                  <tr><td><code>=.</code></td><td>Ends with</td><td><code>$dataFile[original]=..grib2</code></td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="tab-pane fade" id="dg-dis-script" role="tabpanel">
            <div class="alert alert-secondary py-2 px-3 mb-3 small">
              Select <strong>JavaScript</strong> or <strong>Python</strong> mode using the radio
              buttons above the editor — the wrapper is added automatically on save. The script is
              evaluated by <code>Format.choose()</code> via <code>ScriptManager</code>;
              the <strong>return value / last expression</strong> is used as the resolved target path.
              Variables are substituted before the script runs.
            </div>
            <ul class="nav nav-pills mb-3" id="disScriptTab" role="tablist">
              <li class="nav-item"><button class="nav-link active small py-1 px-3" data-bs-toggle="tab" data-bs-target="#dg-dis-js" type="button">JavaScript</button></li>
              <li class="nav-item"><button class="nav-link small py-1 px-3" data-bs-toggle="tab" data-bs-target="#dg-dis-py" type="button">Python</button></li>
            </ul>
            <div class="tab-content">
              <div class="tab-pane fade show active" id="dg-dis-js" role="tabpanel">
                <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">// Return the resolved target path as a string
var host = "$host[name]";
var stream = "$dataFile[metaStream]";
"/outgoing/" + host + "/" + stream + "/";</pre>
              </div>
              <div class="tab-pane fade" id="dg-dis-py" role="tabpanel">
                <pre class="small p-2 rounded mb-0" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">host   = "$host[name]"
stream = "$dataFile[metaStream]"
print("/outgoing/" + host + "/" + stream + "/")</pre>
              </div>
            </div>
          </div>

          <div class="tab-pane fade" id="dg-dis-vars" role="tabpanel">
            <p class="small text-muted mb-2">All tokens below are substituted before the path is used or the script runs.</p>
            <p class="small fw-semibold mb-1">Host</p>
            <p class="mb-2"><code class="small me-1">$host[name]</code><code class="small me-1">$host[comment]</code><code class="small me-1">$host[host]</code><code class="small me-1">$host[login]</code><code class="small me-1">$host[passwd]</code><code class="small me-1">$host[userMail]</code><code class="small me-1">$host[networkCode]</code><code class="small me-1">$host[networkName]</code><code class="small me-1">$host[nickname]</code></p>
            <p class="small fw-semibold mb-1">Data File</p>
            <p class="mb-2"><code class="small me-1">$dataFile[timeStep]</code><code class="small me-1">$dataFile[arrivedTime]</code><code class="small me-1">$dataFile[id]</code><code class="small me-1">$dataFile[original]</code><code class="small me-1">$dataFile[source]</code><code class="small me-1">$dataFile[formatSize]</code><code class="small me-1">$dataFile[size]</code><code class="small me-1">$dataFile[timeBase]</code><code class="small me-1">$dataFile[timeFile]</code><code class="small me-1">$dataFile[metaTime]</code><code class="small me-1">$dataFile[metaStream]</code><code class="small me-1">$dataFile[checksum]</code></p>
            <p class="small fw-semibold mb-1">Data Transfer</p>
            <p class="mb-2"><code class="small me-1">$dataTransfer[target]</code><code class="small me-1">$dataTransfer[id]</code><code class="small me-1">$dataTransfer[comment]</code><code class="small me-1">$dataTransfer[identity]</code><code class="small me-1">$dataTransfer[priority]</code><code class="small me-1">$dataTransfer[scheduled]</code><code class="small me-1">$dataTransfer[statusCode]</code><code class="small me-1">$dataTransfer[name]</code><code class="small me-1">$dataTransfer[path]</code><code class="small me-1">$dataTransfer[parent]</code><code class="small me-1">$dataTransfer[asap]</code></p>
            <p class="small fw-semibold mb-1">Destination</p>
            <p class="mb-2"><code class="small me-1">$destination[name]</code><code class="small me-1">$destination[comment]</code><code class="small me-1">$destination[userMail]</code></p>
            <p class="small fw-semibold mb-1">Country</p>
            <p class="mb-2"><code class="small me-1">$country[name]</code><code class="small me-1">$country[iso]</code></p>
            <p class="small fw-semibold mb-1">Transfer Method, Module &amp; Server</p>
            <p class="mb-2"><code class="small me-1">$transferMethod[name]</code><code class="small me-1">$transferMethod[comment]</code><code class="small me-1">$ectransModule[name]</code><code class="small me-1">$transferServer[name]</code><code class="small me-1">$transferServer[host]</code><code class="small me-1">$transferServer[port]</code><code class="small me-1">$transferGroup[name]</code><code class="small me-1">$moverName</code></p>
          </div>

        </div>

      </c:when>

      <%-- ================================================================
           REPLICATION / SOURCE / BACKUP / PROXY  (and any unknown type)
           ================================================================ --%>
      <c:otherwise>

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
          <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
          <div>For <strong>${requestScope[param.actionFormName].type}</strong> hosts the Directory field
          specifies the <strong>base path</strong> used on the DataMover (or remote server) when
          setting up the ECtrans destination. Only the <strong>first line</strong> is used; the
          path is truncated at the first <code>$</code> character. Script mode is not supported
          for this host type.</div>
        </div>

        <p class="small fw-semibold mb-1"><i class="bi bi-code-square text-primary me-1"></i>Format</p>
        <p class="small text-muted mb-2">Enter a plain path. Only the first line is read. If the
        path contains a <code>$</code> token the path is truncated there (the part before <code>$</code>
        is used as <code>realDir</code>). Dissemination-style selector blocks starting with
        <code>(</code> are skipped entirely.</p>
        <pre class="small p-2 rounded mb-3" style="background:var(--bs-secondary-bg); white-space:pre-wrap;">/ecpds/data/store/</pre>

        <p class="small fw-semibold mb-1"><i class="bi bi-braces text-primary me-1"></i>Variables</p>
        <p class="small text-muted mb-2">Because the path is truncated at the first <code>$</code>,
        substitution variables are <strong>not</strong> generally useful here. The raw string before
        the first <code>$</code> is taken as the directory.</p>

        <p class="small fw-semibold mb-1"><i class="bi bi-gear text-primary me-1"></i>Internal usage</p>
        <p class="small text-muted mb-0">This value is passed to the ECtrans module as the
        <code>dir</code> / <code>realDir</code> when the DataMover opens a connection for file
        transfer. The exact interpretation depends on the ECtrans module configured for the
        host's transfer method.</p>

      </c:otherwise>

    </c:choose>

  </div>
</div>
