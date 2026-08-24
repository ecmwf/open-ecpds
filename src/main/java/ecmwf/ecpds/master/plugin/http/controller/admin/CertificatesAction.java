/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.security.CertificateManager;
import ecmwf.common.security.CertificateManager.CertificateInfo;
import ecmwf.common.ecaccess.ClientInterface;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.technical.Singletons;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.HttpPlugin;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class CertificatesAction.
 *
 * Handles the certificate management administration page at {@code /do/admin/certificates}.
 *
 * <p>
 * Supported POST actions (via the {@code action} request parameter):
 * <ul>
 * <li>{@code generate} – generate a new self-signed certificate and optionally deploy to movers</li>
 * <li>{@code csr} – generate a CSR and stream it as a PEM download</li>
 * <li>{@code import} – import a PEM / PKCS#12 / JKS certificate from an uploaded file</li>
 * <li>{@code download} – stream the current public certificate as a PEM download</li>
 * <li>{@code deploy} – push the current Monitor certificate to all Data Movers</li>
 * </ul>
 */
public class CertificatesAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CertificatesAction.class);

    /** Date (UTC) format matching site convention: yyyy-MM-dd */
    private static final String DATE_FMT_DATE = "yyyy-MM-dd";

    /** Time (UTC) format matching site convention: HH:mm:ss */
    private static final String DATE_FMT_TIME = "HH:mm:ss";

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final var httpPlugin = getHttpPlugin(request);
        final var action = request.getParameter("action");
        final var caf = form instanceof CertificatesActionForm ? (CertificatesActionForm) form : null;

        try {
            if (action != null) {
                switch (action) {
                case "generate":
                    return handleGenerate(mapping, request, response, user, httpPlugin);
                case "csr":
                    return handleCsr(request, response, httpPlugin);
                case "import":
                    return handleImport(mapping, request, response, user, httpPlugin, caf);
                case "download":
                    return handleDownload(request, response, httpPlugin);
                case "deploy":
                    return handleDeploy(mapping, request, user, httpPlugin);
                case "deployMonitors":
                    return handleDeployMonitors(mapping, request, user, httpPlugin);
                case "deploySingle":
                    return handleDeploySingle(mapping, request, user, httpPlugin);
                default:
                    request.setAttribute("errorMessage", "Unknown action: " + action);
                }
            }
        } catch (final Exception e) {
            _log.error("Certificate action '{}' failed", action, e);
            request.setAttribute("errorMessage", e.getMessage());
        }

        // Populate the page with current certificate info
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    // -------------------------------------------------------------------------
    // Action handlers
    // -------------------------------------------------------------------------

    private ActionForward handleGenerate(final ActionMapping mapping, final HttpServletRequest request,
            final HttpServletResponse response, final User user, final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        final String hostname;
        final var hostnameParam = request.getParameter("hostname");
        if (hostnameParam != null && !hostnameParam.isBlank()) {
            hostname = hostnameParam.trim();
        } else {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        CertificateManager.generateSelfSigned(path, password, hostname);
        httpPlugin.reloadCertificate();
        _log.info("Generated new self-signed certificate for '{}' by user {}", hostname, user.getName());
        request.setAttribute("successMessage",
                "New self-signed certificate generated for '" + hostname + "' and activated.");
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    private ActionForward handleCsr(final HttpServletRequest request, final HttpServletResponse response,
            final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        final var csrHostnameParam = request.getParameter("csrHostname");
        final var csrHostname = (csrHostnameParam != null && !csrHostnameParam.isBlank()) ? csrHostnameParam.trim()
                : null;
        final var pem = CertificateManager.generateCsr(path, password, csrHostname);
        response.setContentType("application/pkcs10");
        response.setHeader("Content-Disposition", "attachment; filename=\"ecpds-monitor.csr\"");
        final var bytes = pem.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.flushBuffer();
        return null; // response already committed
    }

    private ActionForward handleImport(final ActionMapping mapping, final HttpServletRequest request,
            final HttpServletResponse response, final User user, final HttpPlugin httpPlugin,
            final CertificatesActionForm caf) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        if (caf == null) {
            throw new ECMWFException("Import form not available");
        }
        final var formFile = caf.getCertFile();
        if (formFile == null || formFile.getFileSize() == 0) {
            throw new ECMWFException("No certificate file was uploaded");
        }
        final byte[] certBytes = formFile.getFileData();
        final var pw = caf.getImportPassword();
        final String importPassword = (pw != null && !pw.isBlank()) ? pw : password;
        CertificateManager.importCertificate(path, password, certBytes, importPassword);
        httpPlugin.reloadCertificate();
        _log.info("Imported certificate for Monitor by user {}", user.getName());
        request.setAttribute("successMessage", "Certificate imported and activated successfully.");
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    private ActionForward handleDownload(final HttpServletRequest request, final HttpServletResponse response,
            final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        final var pem = CertificateManager.exportPublicCertPem(path, password, "PKCS12");
        final var bytes = pem.getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/x-pem-file");
        response.setHeader("Content-Disposition", "attachment; filename=\"ecpds-monitor.pem\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.flushBuffer();
        return null; // response already committed
    }

    private ActionForward handleDeploy(final ActionMapping mapping, final HttpServletRequest request, final User user,
            final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        // Read the current keystore bytes
        final var ksFile = new java.io.File(path);
        final byte[] pkcs12Bytes;
        try (final var fis = new java.io.FileInputStream(ksFile)) {
            pkcs12Bytes = fis.readAllBytes();
        }
        final var session = Util.getECpdsSessionFromObject(user);
        MasterManager.getMI().deployHttpCertificateToAllMovers(session, pkcs12Bytes, password);
        _log.info("Deployed Monitor certificate to all Data Movers by user {}", user.getName());
        request.setAttribute("successMessage", "Certificate deployed to all connected Data Movers successfully.");
        request.setAttribute("successMessageTarget", "movers");
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    private ActionForward handleDeployMonitors(final ActionMapping mapping, final HttpServletRequest request,
            final User user, final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        final var ksFile = new java.io.File(path);
        final byte[] pkcs12Bytes;
        try (final var fis = new java.io.FileInputStream(ksFile)) {
            pkcs12Bytes = fis.readAllBytes();
        }
        final var session = Util.getECpdsSessionFromObject(user);
        MasterManager.getMI().deployHttpCertificateToAllMonitors(session, pkcs12Bytes, password);
        _log.info("Deployed Monitor certificate to all other Monitors by user {}", user.getName());
        request.setAttribute("successMessage", "Certificate deployed to all connected Monitors successfully.");
        request.setAttribute("successMessageTarget", "monitors");
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    private ActionForward handleDeploySingle(final ActionMapping mapping, final HttpServletRequest request,
            final User user, final HttpPlugin httpPlugin) throws Exception {
        final var path = httpPlugin.getActiveKeystorePath();
        final var password = httpPlugin.getActiveKeystorePassword();
        if (path == null || password == null) {
            throw new ECMWFException("HttpPlugin is not running or has no keystore configured");
        }
        final var targetType = request.getParameter("targetType");
        final var targetName = request.getParameter("targetName");
        if (targetType == null || targetName == null || targetName.isBlank()) {
            throw new ECMWFException("Missing targetType or targetName parameter");
        }
        final var ksFile = new java.io.File(path);
        final byte[] pkcs12Bytes;
        try (final var fis = new java.io.FileInputStream(ksFile)) {
            pkcs12Bytes = fis.readAllBytes();
        }
        final var session = Util.getECpdsSessionFromObject(user);
        switch (targetType) {
        case "mover":
            MasterManager.getMI().deployHttpCertificateToMover(session, targetName, pkcs12Bytes, password);
            _log.info("Deployed certificate to Data Mover {} by user {}", targetName, user.getName());
            request.setAttribute("successMessage",
                    "Certificate deployed to Data Mover '" + targetName + "' successfully.");
            request.setAttribute("successMessageTarget", "movers");
            break;
        case "monitor":
            MasterManager.getMI().deployHttpCertificateToMonitor(session, targetName, pkcs12Bytes, password);
            _log.info("Deployed certificate to Monitor {} by user {}", targetName, user.getName());
            request.setAttribute("successMessage",
                    "Certificate deployed to Monitor '" + targetName + "' successfully.");
            request.setAttribute("successMessageTarget", "monitors");
            break;
        default:
            throw new ECMWFException("Unknown targetType: " + targetType);
        }
        populateCertificateInfo(request, httpPlugin, user);
        return mapping.findForward("success");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves the Monitor {@link HttpPlugin} instance from the servlet context. The plugin sets itself on the
     * {@code WebAppContext} as {@code "ecpds.HttpPlugin"} during startup.
     */
    private static HttpPlugin getHttpPlugin(final HttpServletRequest request) {
        return (HttpPlugin) request.getServletContext().getAttribute("ecpds.HttpPlugin");
    }

    /**
     * Populates request attributes used by the certificates JSP: the Monitor's own certificate info plus the per-mover
     * certificate snapshots.
     */
    private static void populateCertificateInfo(final HttpServletRequest request, final HttpPlugin httpPlugin,
            final User user) {
        // Monitor certificate
        if (httpPlugin != null) {
            final var info = httpPlugin.getCertificateInfo();
            if (info != null) {
                request.setAttribute("monitorCert", formatInfo(info));
                request.setAttribute("monitorCertSelfSigned", info.selfSigned());
                request.setAttribute("monitorKeystorePath", httpPlugin.getActiveKeystorePath());
            }
        }
        // Per-mover certificate snapshots
        try {
            final var session = Util.getECpdsSessionFromObject(user);
            final var movers = MasterManager.getMI().getHttpCertificatesJson(session);
            final List<Map<String, Object>> moverList = new ArrayList<>();
            for (final var entry : movers.entrySet()) {
                final var m = new LinkedHashMap<String, Object>();
                m.put("name", entry.getKey());
                m.put("json", entry.getValue());
                moverList.add(m);
            }
            request.setAttribute("moverCerts", moverList);
        } catch (final Exception e) {
            _log.warn("Could not fetch mover certificate info", e);
        }
        // Per-monitor certificate snapshots (exclude this monitor — shown at top)
        try {
            final var session = Util.getECpdsSessionFromObject(user);
            final var server = Singletons.get(StarterServer.class);
            String localRoot = null;
            if (server instanceof final ClientInterface ci) {
                try {
                    localRoot = ci.getRoot();
                } catch (final Exception ignored) {
                }
            }
            final var monitors = MasterManager.getMI().getMonitorCertificatesJson(session);
            final List<Map<String, Object>> monitorList = new ArrayList<>();
            for (final var entry : monitors.entrySet()) {
                if (localRoot != null && entry.getKey().equalsIgnoreCase(localRoot)) {
                    continue; // already shown in the Monitor Certificate card at the top
                }
                final var m = new LinkedHashMap<String, Object>();
                m.put("name", entry.getKey());
                m.put("json", entry.getValue());
                monitorList.add(m);
            }
            request.setAttribute("monitorCerts", monitorList);
        } catch (final Exception e) {
            _log.warn("Could not fetch monitor certificate info", e);
        }
    }

    /**
     * Converts a {@link CertificateInfo} into a display-friendly {@code Map}. Dates are split into date (yyyy-MM-dd)
     * and time (HH:mm:ss) entries, both in UTC.
     */
    public static Map<String, String> formatInfo(final CertificateInfo info) {
        final var fmtDate = new SimpleDateFormat(DATE_FMT_DATE);
        fmtDate.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        final var fmtTime = new SimpleDateFormat(DATE_FMT_TIME);
        fmtTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        final var m = new LinkedHashMap<String, String>();
        m.put("subject", info.subject());
        m.put("issuer", info.issuer());
        m.put("serialNumber", info.serialNumber());
        m.put("notBefore", fmtDate.format(info.notBefore()));
        m.put("notBeforeTime", fmtTime.format(info.notBefore()));
        m.put("notAfter", fmtDate.format(info.notAfter()));
        m.put("notAfterTime", fmtTime.format(info.notAfter()));
        m.put("fingerprintSha256", info.fingerprintSha256());
        m.put("keyAlgorithm", info.keyAlgorithm() + " " + info.keySize() + " bit");
        m.put("selfSigned", String.valueOf(info.selfSigned()));
        m.put("expired", String.valueOf(info.expired()));
        m.put("expiringSoon", String.valueOf(info.expiringSoon()));
        return m;
    }
}
