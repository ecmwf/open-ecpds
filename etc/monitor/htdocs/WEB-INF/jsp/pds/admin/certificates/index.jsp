<%@ page session="true" import="java.util.Map,java.util.List,ecmwf.ecpds.master.plugin.http.controller.admin.CertificatesAction" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<%-- ============================================================
     Certificate Management — /do/admin/certificates
     ============================================================ --%>

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
<i class="bi bi-shield-lock text-secondary flex-shrink-0"></i>
<span>TLS certificate management for all Monitor Servers and Data Movers in the system. Certificates shown as <em>self-signed</em> or <em>expired</em> should be replaced. Certificates expiring within 30 days are highlighted as a warning.</span>
</div>

<%
    @SuppressWarnings("unchecked")
    final Map<String,String> monitorCert = (Map<String,String>) request.getAttribute("monitorCert");
    final Boolean selfSigned = (Boolean) request.getAttribute("monitorCertSelfSigned");
    final String keystorePath = (String) request.getAttribute("monitorKeystorePath");
    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> moverCerts = (List<Map<String,Object>>) request.getAttribute("moverCerts");
    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> monitorCerts = (List<Map<String,Object>>) request.getAttribute("monitorCerts");
    final String successMessage = (String) request.getAttribute("successMessage");
    final String errorMessage   = (String) request.getAttribute("errorMessage");
    final String successMessageTarget = (String) request.getAttribute("successMessageTarget");
    final boolean isSelfSigned  = Boolean.TRUE.equals(selfSigned);
    final boolean hasCert       = (monitorCert != null);
%>

<%-- ============================================================
     Monitor Certificate Card
     ============================================================ --%>
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
  <i class="bi bi-shield-lock text-primary"></i>
  <span class="fw-semibold">Monitor Certificate</span>
  <button class="btn btn-link btn-sm text-muted p-0" type="button"
      data-bs-toggle="collapse" data-bs-target="#certMonitorInfo"
      aria-expanded="false" title="About this section">
    <i class="bi bi-info-circle"></i>
  </button>
  <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
    <button type="button" class="btn btn-sm btn-outline-secondary"
            data-bs-toggle="modal" data-bs-target="#generateModal">
      <i class="bi bi-magic me-1"></i>Generate Self-Signed
    </button>
    <% if (hasCert) { %>
    <button type="button" class="btn btn-sm btn-outline-secondary"
            data-bs-toggle="modal" data-bs-target="#csrModal">
      <i class="bi bi-file-earmark-text me-1"></i>Generate CSR
    </button>
    <% } else { %>
    <button class="btn btn-sm btn-outline-secondary" disabled title="No certificate loaded">
      <i class="bi bi-file-earmark-text me-1"></i>Generate CSR
    </button>
    <% } %>
    <button type="button" class="btn btn-sm btn-outline-secondary"
            data-bs-toggle="modal" data-bs-target="#importModal">
      <i class="bi bi-upload me-1"></i>Import
    </button>
    <% if (hasCert) { %>
    <form method="post" action="/do/admin/certificates" style="display:contents">
      <input type="hidden" name="action" value="download"/>
      <button type="submit" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-download me-1"></i>Download
      </button>
    </form>
    <form method="post" action="/do/admin/certificates" id="reloadLocalForm" style="display:contents">
      <input type="hidden" name="action" value="reload"/>
      <button type="button" class="btn btn-sm btn-outline-secondary"
              title="Hot-reload the certificate on this Monitor only, from the keystore file currently on disk — does not affect other Monitors or Data Movers"
              onclick="confirmationDialog({
                title:       'Reload Certificate from Disk',
                message:     'Reload the TLS certificate on <strong>this Monitor only</strong> from the keystore file currently on disk?<br><small class=\'text-muted\'>No certificate is pushed to other Monitors or Data Movers. Each of those has its own Reload from Disk action in the tables below.</small>',
                confirmText: 'Reload',
                onConfirm:   function(){ document.getElementById('reloadLocalForm').submit(); }
              })">
        <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
      </button>
    </form>
    <% } else { %>
    <button class="btn btn-sm btn-outline-secondary" disabled title="No certificate loaded">
      <i class="bi bi-download me-1"></i>Download
    </button>
    <button class="btn btn-sm btn-outline-secondary" disabled title="No certificate loaded">
      <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
    </button>
    <% } %>
  </div>
</div>

<div class="collapse" id="certMonitorInfo">
  <div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Monitor Certificate &mdash; overview</strong>
    <p class="mb-1">Displays the TLS certificate currently used by the Monitor HTTPS server. Actions available in the header allow you to:</p>
    <ul class="mb-1 ps-3">
      <li><strong>Generate Self-Signed</strong> &mdash; creates a new self-signed certificate suitable for evaluation and testing. Not recommended for production.</li>
      <li><strong>Generate CSR</strong> &mdash; creates a Certificate Signing Request to submit to a Certificate Authority (CA).</li>
      <li><strong>Import</strong> &mdash; replaces the current certificate with a PEM, PKCS#12, or JKS file.</li>
      <li><strong>Download</strong> &mdash; exports the public certificate (PEM) for installation in browsers or MQTT clients.</li>
      <li><strong>Reload from Disk</strong> &mdash; hot-reloads the certificate on <strong>this Monitor only</strong> from the keystore file currently on disk. Use this after replacing the keystore file manually (e.g. via automation or a secrets manager) to activate the new certificate without restarting the daemon. To reload on other Monitors or Data Movers, use the <em>Reload from Disk</em> actions in the tables below.</li>
    </ul>
    <p class="mb-0">All changes are applied without restarting the server (hot reload).</p>
    <hr class="my-2"/>
    <strong class="d-block mb-1"><i class="bi bi-exclamation-triangle-fill text-warning me-1"></i>Certificate DNS coverage</strong>
    <p class="mb-1">This certificate secures HTTPS access to the <strong>Monitor UI</strong>. When deployed to Data Movers (via <em>Deploy to All Movers</em>), the same certificate also secures the <strong>Data Portal UI</strong> (HTTPS) and the <strong>MQTT service</strong> (TLS) on each mover. Because all these services may be reached from multiple hostnames, the certificate's <strong>Subject Alternative Names (SAN)</strong> should include <em>every</em> DNS name (and optionally IP address) under which this Monitor or any Data Mover is accessed. Typical names to cover:</p>
    <ul class="mb-1 ps-3">
      <li>The short hostname and fully-qualified domain name (FQDN) of this Monitor server.</li>
      <li>Any load-balancer or virtual hostname that routes traffic to this Monitor.</li>
      <li>Hostnames of all connected <strong>Data Movers</strong> if they also use this certificate (e.g. when the Monitor certificate is deployed to movers via <em>Deploy to All Movers</em>).</li>
      <li>Hostnames of all other <strong>Monitor daemons</strong> if the same certificate is shared across them.</li>
    </ul>
    <p class="mb-0">A mismatch between the certificate's SAN list and the hostname used by a client will cause TLS handshake failures. When in doubt, generate a CSR and request a multi-SAN certificate from your CA, or use a wildcard certificate covering your domain.</p>
  </div>
</div>

<% if (successMessage != null && successMessageTarget == null) { %>
<div id="certSuccessAlert" class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2 mb-0 rounded-0 border-start-0 border-end-0 border-top-0" role="alert" style="margin-top:0">
  <i class="bi bi-check-circle-fill flex-shrink-0 ms-3"></i>
  <span><%=successMessage%></span>
  <button type="button" class="btn-close ms-auto me-2" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<script>
  setTimeout(function() {
    var el = document.getElementById('certSuccessAlert');
    if (el) { bootstrap.Alert.getOrCreateInstance(el).close(); }
  }, 4000);
</script>
<% } %>
<% if (errorMessage != null) { %>
<div class="card-body py-2 px-3">
  <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center gap-2 mb-0" role="alert">
    <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
    <span><%=errorMessage%></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  </div>
</div>
<% } %>

<% if (isSelfSigned) { %>
<div class="alert alert-warning d-flex align-items-center gap-2 mb-0 rounded-0 border-start-0 border-end-0 border-top-0" role="alert" style="margin-top:0">
  <i class="bi bi-shield-exclamation flex-shrink-0 ms-3"></i>
  <span><strong>Self-Signed Certificate:</strong> This certificate is intended for evaluation and testing only.
  Replace it with a CA-issued certificate before using this system in production.</span>
</div>
<% } %>

<div class="card-body py-2">
<% if (!hasCert) { %>
  <div class="text-muted fst-italic py-2">
    <i class="bi bi-exclamation-circle me-1"></i>
    No certificate information available. The Monitor HTTPS plugin may not be running.
  </div>
<% } else { %>
  <div class="field-grid">
    <div class="field-row"><div class="field-label">Subject</div><div class="field-value"><span class="val-code"><%=monitorCert.get("subject")%></span></div></div>
    <div class="field-row"><div class="field-label">Issuer</div><div class="field-value"><span class="val-code"><%=monitorCert.get("issuer")%></span></div></div>
    <div class="field-row"><div class="field-label">Valid From (UTC)</div><div class="field-value"><span class="val-code"><%=monitorCert.get("notBefore")%></span><br><small class="text-muted"><%=monitorCert.get("notBeforeTime")%></small></div></div>
    <div class="field-row"><div class="field-label">Valid Until (UTC)</div><div class="field-value">
      <span class="val-code"><%=monitorCert.get("notAfter")%></span><br><small class="text-muted"><%=monitorCert.get("notAfterTime")%></small>
      <% if ("true".equals(monitorCert.get("expired"))) { %>
        <span class="badge bg-danger ms-1">EXPIRED</span>
      <% } else if ("true".equals(monitorCert.get("expiringSoon"))) { %>
        <span class="badge bg-warning text-dark ms-1">EXPIRING SOON</span>
      <% } else { %>
        <span class="badge bg-success ms-1">VALID</span>
      <% } %>
    </div></div>
    <div class="field-row"><div class="field-label">Type</div><div class="field-value">
      <% if (isSelfSigned) { %><span class="badge bg-info text-dark">Self-Signed</span>
      <% } else { %><span class="badge bg-success">CA-Signed</span><% } %>
    </div></div>
    <div class="field-row"><div class="field-label">Key Algorithm</div><div class="field-value"><span class="val-code"><%=monitorCert.get("keyAlgorithm")%></span></div></div>
    <div class="field-row"><div class="field-label">Serial Number</div><div class="field-value"><span class="val-code"><%=monitorCert.get("serialNumber")%></span></div></div>
    <div class="field-row"><div class="field-label">SHA-256 Fingerprint</div><div class="field-value"><span class="val-code"><%=monitorCert.get("fingerprintSha256")%></span></div></div>
    <% if (keystorePath != null) { %>
    <div class="field-row"><div class="field-label">Keystore Path</div><div class="field-value"><span class="val-code"><%=keystorePath%></span></div></div>
    <% } %>
    <%
        final java.util.List<String> monSans = (java.util.List<String>) request.getAttribute("monitorCertSans");
        if (monSans != null && !monSans.isEmpty()) {
    %>
    <div class="field-row">
      <div class="field-label">Subject&nbsp;Alt&nbsp;Names</div>
      <div class="field-value">
        <div style="max-height:8em; overflow-y:auto; padding:2px 0;">
        <% for (final String san : monSans) { %>
        <span class="val-code d-inline-block me-1 mb-1" style="font-size:.78rem;"><%=san%></span>
        <% } %>
        </div>
        <small class="text-muted"><%=monSans.size()%> name<%=monSans.size() == 1 ? "" : "s"%></small>
      </div>
    </div>
    <% } %>
  </div>
<% } %>
</div>
</div><%-- /Monitor Certificate card --%>

<%-- ============================================================
     Monitor Certificates Card
     ============================================================ --%>
<% if (monitorCerts != null && !monitorCerts.isEmpty()) { %>
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
  <i class="bi bi-display text-primary"></i>
  <span class="fw-semibold">Monitor Certificates</span>
  <button class="btn btn-link btn-sm text-muted p-0" type="button"
      data-bs-toggle="collapse" data-bs-target="#certMonitorListInfo"
      aria-expanded="false" title="About this section">
    <i class="bi bi-info-circle"></i>
  </button>
  <div class="ms-auto d-flex flex-wrap gap-2">
    <% if (hasCert) { %>
    <form id="deployMonitorsForm" method="post" action="/do/admin/certificates" style="display:contents">
      <input type="hidden" name="action" value="deployMonitors"/>
      <button type="button" class="btn btn-sm btn-outline-primary"
              onclick="confirmationDialog({
                title:       'Deploy Certificate to Monitors',
                message:     'Deploy the current Monitor certificate to all connected Monitors?<br><small class=\'text-muted\'>Certificates will be reloaded; a brief HTTPS interruption may occur on each target Monitor.</small>',
                confirmText: 'Deploy',
                onConfirm:   function(){ document.getElementById('deployMonitorsForm').submit(); }
              })">
        <i class="bi bi-cloud-upload me-1"></i>Deploy to All Monitors
      </button>
    </form>
    <form id="reloadMonitorsForm" method="post" action="/do/admin/certificates" style="display:contents">
      <input type="hidden" name="action" value="reloadMonitors"/>
      <button type="button" class="btn btn-sm btn-outline-secondary"
              onclick="confirmationDialog({
                title:       'Reload Certificate from Disk on All Monitors',
                message:     'Trigger a certificate hot-reload from the keystore file on disk on all connected Monitors?<br><small class=\'text-muted\'>Each Monitor will re-read its own keystore file. No certificate is pushed from this server.</small>',
                confirmText: 'Reload',
                onConfirm:   function(){ document.getElementById('reloadMonitorsForm').submit(); }
              })"
              title="Hot-reload the certificate from disk on every connected Monitor">
        <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
      </button>
    </form>
    <% } else { %>
    <button class="btn btn-sm btn-outline-primary" disabled title="No certificate to deploy">
      <i class="bi bi-cloud-upload me-1"></i>Deploy to All Monitors
    </button>
    <button class="btn btn-sm btn-outline-secondary" disabled title="No certificate loaded">
      <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
    </button>
    <% } %>
  </div>
</div>

<div class="collapse" id="certMonitorListInfo">
  <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Monitor Certificates &mdash; overview</strong>
    <p class="mb-0">Shows the TLS certificate currently active on each connected Monitor daemon. Use <strong>Deploy to All Monitors</strong> to push the current Monitor certificate to every Monitor in one operation. Use <strong>Reload from Disk</strong> to instruct every Monitor to hot-reload its own keystore file from disk (useful after updating keystore files externally).</p>
  </div>
</div>

<% if (successMessage != null && "monitors".equals(successMessageTarget)) { %>
<div id="certMonitorsSuccessAlert" class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2 mb-0 rounded-0 border-start-0 border-end-0 border-top-0" role="alert" style="margin-top:0">
  <i class="bi bi-check-circle-fill flex-shrink-0 ms-3"></i>
  <span><%=successMessage%></span>
  <button type="button" class="btn-close ms-auto me-2" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<script>
  setTimeout(function() {
    var el = document.getElementById('certMonitorsSuccessAlert');
    if (el) { bootstrap.Alert.getOrCreateInstance(el).close(); }
  }, 4000);
</script>
<% } %>

<div class="card-body p-0">
<div class="table-responsive">
<table class="table table-sm table-hover table-striped align-middle mb-0" style="font-size:0.82rem;">
  <thead class="table-warning">
    <tr>
      <th>Monitor</th>
      <th>Subject</th>
      <th>Subject Alt Names</th>
      <th title="Valid Until (UTC)">Valid Until</th>
      <th>Type</th>
      <th>SHA-256 Fingerprint</th>
      <th></th>
    </tr>
  </thead>
  <tbody>
<%
    for (final Map<String,Object> mon : monitorCerts) {
        final String monName = (String) mon.get("name");
        final String json = (String) mon.get("json");
        if (json == null || "{}".equals(json)) {
%>
    <tr>
      <td><strong><%=monName%></strong></td>
      <td colspan="5" class="text-muted fst-italic">Offline or no certificate data available</td>
      <td></td>
    </tr>
<%
        } else {
            final String subj          = jsonField(json, "subject");
            final String notAfter      = jsonField(json, "notAfter");
            final String notAfterTime  = jsonField(json, "notAfterTime");
            final boolean expired  = "true".equals(jsonField(json, "expired"));
            final boolean expSoon  = "true".equals(jsonField(json, "expiringSoon"));
            final boolean mSelf    = "true".equals(jsonField(json, "selfSigned"));
            final String fp        = jsonField(json, "fingerprintSha256");
            final java.util.List<String> monSanList = jsonArrayField(json, "sans");
%>
    <tr>
      <td><strong><%=monName%></strong></td>
      <td style="font-family:monospace; font-size:.78rem; max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="<%=subj != null ? subj : ""%>"><%=subj != null ? subj : "–"%></td>
      <td style="font-size:.78rem; max-width:200px;">
        <% if (monSanList.isEmpty()) { %>–<% } else {
             final int monShow = Math.min(3, monSanList.size());
             for (int si = 0; si < monShow; si++) { %>
        <span class="d-block" style="font-family:monospace; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="<%=monSanList.get(si)%>"><%=monSanList.get(si)%></span>
        <% } if (monSanList.size() > 3) { %>
        <a href="#" class="text-muted sans-more-popover" style="font-size:.75rem;"
           data-bs-toggle="popover" data-bs-trigger="focus" data-bs-placement="left" data-bs-html="true"
           data-bs-content="<div style='font-family:monospace;font-size:.78rem;max-height:12em;overflow-y:auto;'><%for(int si2=0;si2<monSanList.size();si2++){%><%=monSanList.get(si2)%><br><%}%></div>"
           tabindex="0">+<%=monSanList.size()-3%> more</a>
        <% } } %>
      </td>
      <td>
        <%=notAfter != null ? notAfter : "–"%>
        <% if (notAfterTime != null) { %><br><small class="text-muted"><%=notAfterTime%></small><% } %>
        <% if (expired) { %><span class="badge bg-danger ms-1">EXPIRED</span>
        <% } else if (expSoon) { %><span class="badge bg-warning text-dark ms-1">EXPIRING SOON</span><% } %>
      </td>
      <td>
        <% if (mSelf) { %><span class="badge bg-info text-dark">Self-Signed</span>
        <% } else { %><span class="badge bg-success">CA-Signed</span><% } %>
      </td>
      <td style="font-family:monospace; font-size:.75rem; word-break:break-all; max-width:260px;"><%=fp != null ? fp : "–"%></td>
      <td class="text-end" style="white-space:nowrap">
        <% if (hasCert) { %>
        <div class="d-inline-flex gap-1">
        <button type="button" class="btn btn-sm btn-outline-primary"
            onclick="confirmationDialog({
              title:       'Deploy Certificate',
              message:     'Deploy the current Monitor certificate to Monitor <strong><%=monName%></strong>?<br><small class=\'text-muted\'>The certificate will be reloaded; a brief HTTPS interruption may occur.</small>',
              confirmText: 'Deploy',
              onConfirm:   function(){ deploySingle('monitor','<%=monName%>'); }
            })" title="Deploy certificate to this Monitor">
          <i class="bi bi-cloud-upload"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline-secondary"
            onclick="confirmationDialog({
              title:       'Reload Certificate from Disk',
              message:     'Instruct Monitor <strong><%=monName%></strong> to hot-reload its certificate from its own keystore file on disk?<br><small class=\'text-muted\'>No certificate is pushed from this server. The monitor re-reads its local keystore file.</small>',
              confirmText: 'Reload',
              onConfirm:   function(){ reloadSingle('monitor','<%=monName%>'); }
            })" title="Reload certificate from disk on this Monitor">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        </div>
        <% } %>
      </td>
    </tr>
<%
        }
    }
%>
  </tbody>
</table>
</div>
</div>
</div><%-- /Monitor Certificates card --%>
<% } %>

<%-- ============================================================
     Data Mover Certificates Card
     ============================================================ --%>
<% if (moverCerts != null && !moverCerts.isEmpty()) { %>
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
  <i class="bi bi-server text-primary"></i>
  <span class="fw-semibold">Data Mover Certificates</span>
  <button class="btn btn-link btn-sm text-muted p-0" type="button"
      data-bs-toggle="collapse" data-bs-target="#certMoverInfo"
      aria-expanded="false" title="About this section">
    <i class="bi bi-info-circle"></i>
  </button>
  <div class="ms-auto d-flex flex-wrap gap-2">
    <% if (hasCert) { %>
    <form id="deployForm" method="post" action="/do/admin/certificates" style="display:contents">
      <input type="hidden" name="action" value="deploy"/>
      <button type="button" class="btn btn-sm btn-outline-primary"
              onclick="confirmationDialog({
                title:       'Deploy Certificate',
                message:     'Deploy the current Monitor certificate to all connected Data Movers?<br><small class=\'text-muted\'>Certificates will be hot-reloaded without restarting the HTTPS servers.</small>',
                confirmText: 'Deploy',
                onConfirm:   function(){ document.getElementById('deployForm').submit(); }
              })">
        <i class="bi bi-cloud-upload me-1"></i>Deploy to All Movers
      </button>
    </form>
    <form id="reloadMoversForm" method="post" action="/do/admin/certificates" style="display:contents">
      <input type="hidden" name="action" value="reloadMovers"/>
      <button type="button" class="btn btn-sm btn-outline-secondary"
              onclick="confirmationDialog({
                title:       'Reload Certificate from Disk on All Movers',
                message:     'Trigger a certificate hot-reload from the keystore file on disk on all connected Data Movers?<br><small class=\'text-muted\'>Each Mover will re-read its own keystore file. No certificate is pushed from this server.</small>',
                confirmText: 'Reload',
                onConfirm:   function(){ document.getElementById('reloadMoversForm').submit(); }
              })"
              title="Hot-reload the certificate from disk on every connected Data Mover">
        <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
      </button>
    </form>
    <% } else { %>
    <button class="btn btn-sm btn-outline-primary" disabled title="No certificate to deploy">
      <i class="bi bi-cloud-upload me-1"></i>Deploy to All Movers
    </button>
    <button class="btn btn-sm btn-outline-secondary" disabled title="No certificate loaded">
      <i class="bi bi-arrow-clockwise me-1"></i>Reload from Disk
    </button>
    <% } %>
  </div>
</div>

<div class="collapse" id="certMoverInfo">
  <div class="card-body py-2 px-3 border-bottom" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd)!important;">
    <strong class="d-block mb-1">Data Mover Certificates &mdash; overview</strong>
    <p class="mb-0">Shows the TLS certificate currently active on each connected Data Mover. Use <strong>Deploy to All Movers</strong> to push the current Monitor certificate to every Data Mover in one operation. Use <strong>Reload from Disk</strong> to instruct every Data Mover to hot-reload its own keystore file from disk (useful after updating keystore files externally). Certificates are reloaded without restarting the service.</p>
  </div>
</div>

<% if (successMessage != null && "movers".equals(successMessageTarget)) { %>
<div id="certMoversSuccessAlert" class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2 mb-0 rounded-0 border-start-0 border-end-0 border-top-0" role="alert" style="margin-top:0">
  <i class="bi bi-check-circle-fill flex-shrink-0 ms-3"></i>
  <span><%=successMessage%></span>
  <button type="button" class="btn-close ms-auto me-2" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<script>
  setTimeout(function() {
    var el = document.getElementById('certMoversSuccessAlert');
    if (el) { bootstrap.Alert.getOrCreateInstance(el).close(); }
  }, 4000);
</script>
<% } %>

<div class="card-body p-0">
<div class="table-responsive">
<table class="table table-sm table-hover table-striped align-middle mb-0" style="font-size:0.82rem;">
  <thead class="table-warning">
    <tr>
      <th>Mover</th>
      <th>Subject</th>
      <th>Subject Alt Names</th>
      <th title="Valid Until (UTC)">Valid Until</th>
      <th>Type</th>
      <th>SHA-256 Fingerprint</th>
      <th></th>
    </tr>
  </thead>
  <tbody>
<%
    for (final Map<String,Object> mover : moverCerts) {
        final String moverName = (String) mover.get("name");
        final String json = (String) mover.get("json");
        if (json == null || "{}".equals(json)) {
%>
    <tr>
      <td><strong><a href="/do/datafile/transferserver/<%=moverName%>"><%=moverName%></a></strong></td>
      <td colspan="5" class="text-muted fst-italic">Offline or no certificate data available</td>
      <td></td>
    </tr>
<%
        } else {
            final String subj          = jsonField(json, "subject");
            final String notAfter      = jsonField(json, "notAfter");
            final String notAfterTime  = jsonField(json, "notAfterTime");
            final boolean expired  = "true".equals(jsonField(json, "expired"));
            final boolean expSoon  = "true".equals(jsonField(json, "expiringSoon"));
            final boolean mSelf    = "true".equals(jsonField(json, "selfSigned"));
            final String fp        = jsonField(json, "fingerprintSha256");
            final java.util.List<String> moverSanList = jsonArrayField(json, "sans");
%>
    <tr>
      <td><strong><a href="/do/datafile/transferserver/<%=moverName%>"><%=moverName%></a></strong></td>
      <td style="font-family:monospace; font-size:.78rem; max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="<%=subj != null ? subj : ""%>"><%=subj != null ? subj : "–"%></td>
      <td style="font-size:.78rem; max-width:200px;">
        <% if (moverSanList.isEmpty()) { %>–<% } else {
             final int mvShow = Math.min(3, moverSanList.size());
             for (int si = 0; si < mvShow; si++) { %>
        <span class="d-block" style="font-family:monospace; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="<%=moverSanList.get(si)%>"><%=moverSanList.get(si)%></span>
        <% } if (moverSanList.size() > 3) { %>
        <a href="#" class="text-muted sans-more-popover" style="font-size:.75rem;"
           data-bs-toggle="popover" data-bs-trigger="focus" data-bs-placement="left" data-bs-html="true"
           data-bs-content="<div style='font-family:monospace;font-size:.78rem;max-height:12em;overflow-y:auto;'><%for(int si2=0;si2<moverSanList.size();si2++){%><%=moverSanList.get(si2)%><br><%}%></div>"
           tabindex="0">+<%=moverSanList.size()-3%> more</a>
        <% } } %>
      </td>
      <td>
        <%=notAfter != null ? notAfter : "–"%>
        <% if (notAfterTime != null) { %><br><small class="text-muted"><%=notAfterTime%></small><% } %>
        <% if (expired) { %><span class="badge bg-danger ms-1">EXPIRED</span>
        <% } else if (expSoon) { %><span class="badge bg-warning text-dark ms-1">EXPIRING SOON</span><% } %>
      </td>
      <td>
        <% if (mSelf) { %><span class="badge bg-info text-dark">Self-Signed</span>
        <% } else { %><span class="badge bg-success">CA-Signed</span><% } %>
      </td>
      <td style="font-family:monospace; font-size:.75rem; word-break:break-all; max-width:260px;"><%=fp != null ? fp : "–"%></td>
      <td class="text-end" style="white-space:nowrap">
        <% if (hasCert) { %>
        <div class="d-inline-flex gap-1">
        <button type="button" class="btn btn-sm btn-outline-primary"
            onclick="confirmationDialog({
              title:       'Deploy Certificate',
              message:     'Deploy the current Monitor certificate to Data Mover <strong><%=moverName%></strong>?<br><small class=\'text-muted\'>The certificate will be hot-reloaded without restarting the HTTPS server.</small>',
              confirmText: 'Deploy',
              onConfirm:   function(){ deploySingle('mover','<%=moverName%>'); }
            })" title="Deploy certificate to this Data Mover">
          <i class="bi bi-cloud-upload"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline-secondary"
            onclick="confirmationDialog({
              title:       'Reload Certificate from Disk',
              message:     'Instruct Data Mover <strong><%=moverName%></strong> to hot-reload its certificate from its own keystore file on disk?<br><small class=\'text-muted\'>No certificate is pushed from this server. The mover re-reads its local keystore file.</small>',
              confirmText: 'Reload',
              onConfirm:   function(){ reloadSingle('mover','<%=moverName%>'); }
            })" title="Reload certificate from disk on this Data Mover">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        </div>
        <% } %>
      </td>
    </tr>
<%
        }
    }
%>
  </tbody>
</table>
</div>
</div>
</div><%-- /Data Mover Certificates card --%>
<% } %>

<%-- Shared form for per-row single-target certificate deploy --%>
<form id="deploySingleForm" method="post" action="/do/admin/certificates" style="display:none">
  <input type="hidden" name="action"     value="deploySingle"/>
  <input type="hidden" name="targetType" id="deploySingleType"/>
  <input type="hidden" name="targetName" id="deploySingleName"/>
</form>
<%-- Shared form for per-row single-target certificate reload from disk --%>
<form id="reloadSingleForm" method="post" action="/do/admin/certificates" style="display:none">
  <input type="hidden" name="action"     value="reloadSingle"/>
  <input type="hidden" name="targetType" id="reloadSingleType"/>
  <input type="hidden" name="targetName" id="reloadSingleName"/>
</form>
<script>
function deploySingle(type, name) {
  document.getElementById('deploySingleType').value = type;
  document.getElementById('deploySingleName').value = name;
  document.getElementById('deploySingleForm').submit();
}
function reloadSingle(type, name) {
  document.getElementById('reloadSingleType').value = type;
  document.getElementById('reloadSingleName').value = name;
  document.getElementById('reloadSingleForm').submit();
}
// Re-initialize SAN "more" popovers with html:true (global init uses html:false)
$(document).ready(function() {
  $('.sans-more-popover').each(function() {
    var existing = bootstrap.Popover.getInstance(this);
    if (existing) existing.dispose();
    new bootstrap.Popover(this, { trigger: 'focus', html: true });
  });
});
</script>

<%-- ============================================================
     Generate CSR Modal
     ============================================================ --%>
<div class="modal fade" id="csrModal" tabindex="-1" aria-labelledby="csrModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <form method="post" action="/do/admin/certificates">
        <input type="hidden" name="action" value="csr"/>
        <div class="modal-header">
          <h5 class="modal-title" id="csrModalLabel"><i class="bi bi-file-earmark-text me-2"></i>Generate Certificate Signing Request</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p class="text-muted small">A CSR is submitted to a Certificate Authority (CA) to obtain a signed certificate.
          The CA will embed the hostname you specify as the Common Name (CN) in the issued certificate.</p>
          <div class="mb-3">
            <label for="csrHostname" class="form-label">Hostname / CN</label>
            <input type="text" class="form-control" id="csrHostname" name="csrHostname"
                   placeholder="e.g. ecpds.example.com" value=""/>
            <div class="form-text">Leave blank to reuse the CN from the current certificate.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">
            <i class="bi bi-file-earmark-text me-2"></i>Download CSR
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<%-- ============================================================
     Generate Self-Signed Modal
     ============================================================ --%>
<div class="modal fade" id="generateModal" tabindex="-1" aria-labelledby="generateModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <form method="post" action="/do/admin/certificates">
        <input type="hidden" name="action" value="generate"/>
        <div class="modal-header">
          <h5 class="modal-title" id="generateModalLabel"><i class="bi bi-magic me-2"></i>Generate Self-Signed Certificate</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="alert alert-warning d-flex gap-2 align-items-start">
            <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
            <span>Self-signed certificates are suitable for evaluation and testing only.
            Replace with a CA-issued certificate before using in production.</span>
          </div>
          <div class="mb-3">
            <label for="hostname" class="form-label">Hostname / CN</label>
            <input type="text" class="form-control" id="hostname" name="hostname"
                   placeholder="e.g. ecpds.example.com" value=""/>
            <div class="form-text">Leave blank to use the current server hostname.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-warning">
            <i class="bi bi-magic me-2"></i>Generate
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<%-- ============================================================
     Import Certificate Modal
     ============================================================ --%>
<div class="modal fade" id="importModal" tabindex="-1" aria-labelledby="importModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <form method="post" action="/do/admin/certificates" enctype="multipart/form-data">
        <input type="hidden" name="action" value="import"/>
        <div class="modal-header">
          <h5 class="modal-title" id="importModalLabel"><i class="bi bi-upload me-2"></i>Import Certificate</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p class="text-muted">
            Supported formats: <strong>PEM</strong> (certificate + private key), <strong>PKCS#12 / PFX</strong>,
            <strong>Java KeyStore (JKS)</strong>.
          </p>
          <div class="mb-3">
            <label for="certFile" class="form-label">Certificate File</label>
            <input type="file" class="form-control" id="certFile" name="certFile" required/>
          </div>
          <div class="mb-3">
            <label for="importPassword" class="form-label">Password <span class="text-muted">(for PKCS#12 / JKS / encrypted PEM)</span></label>
            <input type="password" class="form-control" id="importPassword" name="importPassword" autocomplete="new-password"/>
            <div class="form-text">Leave blank to use the current keystore password.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">
            <i class="bi bi-upload me-2"></i>Import &amp; Activate
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<%!
    /** Minimal JSON field extractor for a flat JSON string (no nesting). */
    private static String jsonField(final String json, final String key) {
        if (json == null) return null;
        final String search = "\"" + key + "\"";
        final int ki = json.indexOf(search);
        if (ki < 0) return null;
        final int ci = json.indexOf(':', ki + search.length());
        if (ci < 0) return null;
        final int start = ci + 1;
        final int len = json.length();
        int si = start;
        while (si < len && (json.charAt(si) == ' ' || json.charAt(si) == '\t')) si++;
        if (si >= len) return null;
        if (json.charAt(si) == '"') {
            final int end = json.indexOf('"', si + 1);
            return end < 0 ? null : json.substring(si + 1, end);
        } else {
            int end = si;
            while (end < len && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(si, end).trim();
        }
    }

    /** Extracts a flat JSON string array for the given key, e.g. ["DNS:a","IP:b"]. Returns empty list if absent. */
    private static java.util.List<String> jsonArrayField(final String json, final String key) {
        final java.util.List<String> result = new java.util.ArrayList<>();
        if (json == null) return result;
        final String search = "\"" + key + "\"";
        final int ki = json.indexOf(search);
        if (ki < 0) return result;
        final int ci = json.indexOf(':', ki + search.length());
        if (ci < 0) return result;
        final int arrStart = json.indexOf('[', ci + 1);
        final int arrEnd   = json.indexOf(']', arrStart + 1);
        if (arrStart < 0 || arrEnd < 0) return result;
        final String inner = json.substring(arrStart + 1, arrEnd).trim();
        if (inner.isEmpty()) return result;
        for (final String token : inner.split(",")) {
            final String t = token.trim();
            if (t.startsWith("\"") && t.endsWith("\"") && t.length() > 1) {
                result.add(t.substring(1, t.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\"));
            }
        }
        return result;
    }
%>
