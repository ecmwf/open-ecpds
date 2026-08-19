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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.compression.gzip.GzipCompression;
import org.eclipse.jetty.compression.server.CompressionConfig;
import org.eclipse.jetty.compression.server.CompressionHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.rewrite.handler.HeaderPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.plugin.PluginThread;
import ecmwf.common.security.CertificateManager;
import ecmwf.common.security.CertificateManager.CertificateInfo;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import ecmwf.ecpds.mover.HttpCertificateProvider;
import ecmwf.ecpds.mover.MoverServer;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ecaccess.StarterServer;

/**
 * The Class HttpPlugin.
 */
public final class HttpPlugin extends PluginThread implements HttpCertificateProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(HttpPlugin.class);

    /** The Constant NAME. */
    private static final String NAME = "HttpPlugin";

    /** The Constant VERSION. */
    private static final String VERSION = Version.getFullVersion();

    /** The server. */
    private Server server;

    /** The statsHandler. */
    private StatisticsHandler statsHandler;

    /** The active SslContextFactory – kept for certificate hot-reload. */
    private SslContextFactory.Server sslContextFactory = null;

    /** Path to the keystore currently in use (for reload and info queries). */
    private String activeKeystorePath = null;

    /** Password for the active keystore. */
    private String activeKeystorePassword = null;

    /** Type of the active keystore (PKCS12 / JKS). */
    private String activeKeystoreType = null;

    static {
        // Prevent Jetty from rewriting headers:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=414449
        System.setProperty("org.eclipse.jetty.http.HttpParser.STRICT", "true");
    }

    /**
     * Instantiates a new http plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public HttpPlugin(final String ref, final Map<String, String> params) {
        super(ref, params);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the plugin name.
     */
    @Override
    public String getPluginName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Start.
     */
    @Override
    public synchronized boolean start() {
        if (server != null) {
            return true;
        }
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(HttpPlugin.class.getClassLoader());
        try {
            final var httpPort = Cnf.at("HttpPlugin", "http", -1);
            final var httpsPort = Cnf.at("HttpPlugin", "https", -1);
            if (httpPort < 0 && httpsPort < 0) {
                throw new IOException("Invalid http/s port specified: " + httpPort + "/" + httpsPort);
            }
            final var listenAddress = Cnf.at("HttpPlugin", "listenAddress", "0.0.0.0");
            // Home?
            final var jettyHome = Cnf.at("HttpPlugin", "htdocs");
            // Thread pooling mechanism
            final var threadPool = new QueuedThreadPool(Cnf.at("HttpPlugin", "maxThreads", 400),
                    Cnf.at("HttpPlugin", "minThreads", 40), Cnf.at("HttpPlugin", "idleThreadsTimeout", 120));
            // Create the server
            server = new Server(threadPool);
            server.manage(threadPool);
            // Http configuration
            final var httpConfig = new HttpConfiguration();
            httpConfig.setUriCompliance(UriCompliance.RFC3986);
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(httpsPort);
            httpConfig.setOutputBufferSize(Cnf.at("HttpPlugin", "outputBufferSize", 32768));
            httpConfig.setRequestHeaderSize(Cnf.at("HttpPlugin", "requestHeaderSize", 8192));
            httpConfig.setResponseHeaderSize(Cnf.at("HttpPlugin", "responseHeaderSize", 8192));
            httpConfig.setSendServerVersion(false);
            httpConfig.setSendDateHeader(false);
            httpConfig.setSendXPoweredBy(false);
            // REST API (Jersey, EE10)
            final var restApplication = new ecmwf.ecpds.mover.service.RESTApplication();
            final var jerseyConfig = new org.glassfish.jersey.server.ResourceConfig();
            restApplication.getClasses().forEach(jerseyConfig::register);
            restApplication.getSingletons().forEach(jerseyConfig::register);
            final var rest = new ServletContextHandler("/ecpds", ServletContextHandler.NO_SESSIONS);
            rest.addServlet(new ServletHolder(new org.glassfish.jersey.servlet.ServletContainer(jerseyConfig)), "/*");
            // Security: redirect HTTP to HTTPS when both ports are configured.
            // Use a plain Handler.Abstract instead of SecurityHandler.PathMapped — the
            // SecurityHandler approach triggers an UnsupportedOperationException in Jetty 12
            // when attempting Response.sendRedirect() on an HTTP request body.
            final Handler sh;
            if (httpPort >= 0 && httpsPort >= 0) {
                final int targetHttpsPort = httpsPort;
                sh = new Handler.Abstract() {
                    @Override
                    public boolean handle(final Request request, final Response response, final Callback callback)
                            throws Exception {
                        if (!request.isSecure()) {
                            final var uri = request.getHttpURI();
                            final var httpsUrl = "https://" + uri.getHost() + ":" + targetHttpsPort
                                    + uri.getPathQuery();
                            Response.sendRedirect(request, response, callback, httpsUrl);
                            return true;
                        }
                        return false;
                    }
                };
            } else {
                sh = new Handler.Abstract() {
                    @Override
                    public boolean handle(final Request request, final Response response, final Callback callback) {
                        return false;
                    }
                };
            }
            // Resources
            final var resource = new ResourceHandler();
            resource.setDirAllowed(false);
            resource.setWelcomeFiles(new String[] { "index.html" });
            resource.setBaseResourceAsString(jettyHome + "/resources");
            // AmazonS3 proxy – on by default, disable with s3Enabled=false
            ServletContextHandler s3proxy = null;
            final var s3Enabled = Cnf.at("HttpPlugin", "s3Enabled", true);
            if (s3Enabled) {
                final var s3ServicePath = Cnf.at("HttpPlugin", "s3ServicePath", "/s3");
                _log.info("S3 proxy enabled at {}", s3ServicePath);
                s3proxy = new ServletContextHandler(s3ServicePath, ServletContextHandler.NO_SESSIONS);
                s3proxy.setAllowNullPathInContext(true);
                s3proxy.addServlet(new S3ProxyHandlerJetty(
                        AuthenticationType.fromString(Cnf.at("HttpPlugin", "s3AuthenticationType", "AWS_V2_OR_V4")),
                        Cnf.at("HttpPlugin", "s3V4MaxNonChunkedRequestSize", 32 * 1024 * 1024),
                        Cnf.at("HttpPlugin", "s3IgnoreUnknownHeaders", true), new CrossOriginResourceSharing(),
                        s3ServicePath, Cnf.at("HttpPlugin", "s3MaximumTimeSkew", 15 * 60)), "/*");
            }
            // Add security headers
            final var rewrite = new RewriteHandler();
            rewrite.addRule(getRule("*", "X-XSS-Protection", "1; mode=block"));
            rewrite.addRule(getRule("*", "X-Content-Type-Options", "nosniff"));
            rewrite.addRule(getRule("*", "Content-Security-Policy",
                    "script-src 'self' 'unsafe-eval' 'unsafe-inline' blob:; style-src 'self' 'unsafe-inline';"));
            rewrite.addRule(getRule("*", "X-Frame-Options", "SAMEORIGIN"));
            rewrite.addRule(getRule("*", "Strict-Transport-Security", "max-age=31536000;includeSubDomains"));
            // Global CORS fallback for DNS-mapped paths. Per-user corsAllowOrigin in the
            // IncomingUser's ECtrans properties (INU_DATA) takes precedence over this.
            // Leave empty (default) to disable global CORS; set to * to enable for all users.
            final var globalCorsAllowOrigin = Cnf.at("HttpPlugin", "corsAllowOrigin", "");
            if (!globalCorsAllowOrigin.isEmpty()) {
                _log.info("Global CORS fallback enabled: Access-Control-Allow-Origin: {}", globalCorsAllowOrigin);
            }
            // Handling requests with a server name mapping to a data user.
            // Use a plain Handler.Abstract (not a ServletContextHandler) so that unmatched
            // requests fall through to the next handler in the sequence rather than getting
            // consumed with a 404 by the servlet context.
            final var dns = new Handler.Abstract() {
                @Override
                public boolean handle(final Request request, final Response response, final Callback callback)
                        throws Exception {
                    final var target = request.getHttpURI().getPath();
                    for (final String dnsPath : Cnf.listAt("HttpPlugin", "dnsPathList")) {
                        final var dnsAndPath = dnsPath.split("="); // e.g. opendata=forecasts
                        if (dnsAndPath.length == 2) {
                            final var dnsName = dnsAndPath[0]; // must map a data user!
                            // When behind a load balancer, the original public hostname is carried in
                            // X-Forwarded-Host; fall back to the direct server name for plain connections.
                            final var xForwardedHost = request.getHeaders().get("X-Forwarded-Host");
                            final var effectiveHost = xForwardedHost != null && !xForwardedHost.isBlank()
                                    ? xForwardedHost.split(",")[0].trim() : request.getHttpURI().getHost();
                            if (dnsName.equals(effectiveHost)) {
                                final var path = dnsAndPath[1];
                                final var url = "/".equals(target) ? "/" + path + "/" : target;
                                if (url.startsWith("/" + path + "/")) {
                                    // Resolve CORS allow-origin: per-user ECtrans property first,
                                    // then fall back to the global corsAllowOrigin config value.
                                    var corsAllowOrigin = globalCorsAllowOrigin;
                                    try {
                                        final var mover = StarterServer.getInstance(MoverServer.class);
                                        if (mover != null) {
                                            final var profile = mover.getMasterInterface()
                                                    .getIncomingProfileNoAuth(dnsName);
                                            if (profile != null) {
                                                final var perUser = profile.getECtransSetup()
                                                        .getString(ECtransOptions.USER_PORTAL_CORS_ALLOW_ORIGIN);
                                                if (!perUser.isEmpty()) {
                                                    corsAllowOrigin = perUser;
                                                }
                                            }
                                        }
                                    } catch (final Exception e) {
                                        _log.debug("CORS: could not resolve IncomingUser {}: {}", dnsName,
                                                e.getMessage());
                                    }
                                    // Add CORS headers. For OPTIONS preflight, short-circuit with 204.
                                    if (!corsAllowOrigin.isEmpty() && request.getHeaders().get("Origin") != null) {
                                        response.getHeaders().add("Access-Control-Allow-Origin", corsAllowOrigin);
                                        response.getHeaders().add("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
                                        response.getHeaders().add("Access-Control-Allow-Headers",
                                                "Range, Content-Type, Authorization");
                                        response.getHeaders().add("Access-Control-Expose-Headers",
                                                "Content-Range, Content-Length, Accept-Ranges, ETag, Last-Modified");
                                        if ("OPTIONS".equals(request.getMethod())) {
                                            response.getHeaders().add("Access-Control-Max-Age", "86400");
                                            response.setStatus(204);
                                            callback.succeeded();
                                            return true;
                                        }
                                    }
                                    final var redirectUrl = url.replaceFirst("^/" + path + "/",
                                            "/home/" + dnsName + "/");
                                    Response.sendRedirect(request, response, callback, redirectUrl);
                                    return true;
                                }
                            }
                        } else {
                            _log.warn("Malformed element for dnsPathList: {}", dnsPath);
                        }
                    }
                    return false;
                }
            };
            // WebDAV (RFC 4918 with locking) – on by default, disable with webdavEnabled=false
            Handler webdav = null;
            final var webdavEnabled = Cnf.at("HttpPlugin", "webdavEnabled", true);
            if (webdavEnabled) {
                final var webdavPath = Cnf.at("HttpPlugin", "webdavPath", "/webdav");
                _log.info("WebDAV enabled at {}", webdavPath);
                final var webdavContext = new org.eclipse.jetty.ee8.servlet.ServletContextHandler(
                        org.eclipse.jetty.ee8.servlet.ServletContextHandler.NO_SESSIONS);
                webdavContext.setContextPath(webdavPath);
                webdavContext.setAllowNullPathInfo(true);
                webdavContext.addServlet(new org.eclipse.jetty.ee8.servlet.ServletHolder(new WebDavHandler(webdavPath)),
                        "/*");
                webdav = webdavContext.get();
            }
            // Add all the handlers to the server!
            final var handlerList = new ArrayList<Handler>();
            handlerList.add(sh); // HTTP→HTTPS redirect (no-op when already secure or only one port)
            handlerList.add(dns);
            // All service handlers must come BEFORE the static ResourceHandler.
            // In Jetty 12, ResourceHandler intercepts OPTIONS for ALL paths (returning a plain
            // Allow: GET,HEAD,OPTIONS with no DAV: or JAX-RS methods), which breaks WebDAV
            // discovery (Finder) and CORS preflight on REST/S3 endpoints. Service handlers are
            // path-scoped so they only consume requests they own; unmatched requests fall through
            // to ResourceHandler which serves static assets from the htdocs/resources directory.
            if (webdav != null) {
                // Jetty 12 quirk: the EE8 ContextHandler needs the request URI canonical path
                // to have been accessed at least once before dispatch (lazy field population).
                handlerList.add(new Handler.Abstract() {
                    @Override
                    public boolean handle(final Request request, final Response response, final Callback callback) {
                        request.getHttpURI().getCanonicalPath(); // pre-populate lazy URI state for EE8
                        return false;
                    }
                });
                handlerList.add(webdav);
            }
            handlerList.add(rest);
            if (s3proxy != null) {
                handlerList.add(s3proxy);
            }
            handlerList.add(resource); // fallback: static assets only
            Handler handlers = new Handler.Sequence(handlerList);
            // Enable on-the-fly compression for REST and static content.
            // S3 and WebDAV paths are excluded because they transfer binary/already-compressed
            // data and clients manage their own Content-Encoding for those protocols.
            if (Cnf.at("HttpPlugin", "compression", true)) {
                final var s3Path = Cnf.at("HttpPlugin", "s3ServicePath", "/s3");
                final var webdavPath = Cnf.at("HttpPlugin", "webdavPath", "/webdav");
                final var compressionConfig = CompressionConfig.builder().compressExcludePath(s3Path + "/*")
                        .compressExcludePath(webdavPath + "/*").build();
                final var compression = new CompressionHandler();
                compression.putCompression(new GzipCompression());
                compression.putConfiguration("/*", compressionConfig);
                compression.setHandler(handlers);
                handlers = compression;
            }
            rewrite.setHandler(handlers);
            server.setHandler(rewrite);
            // Suppress stack traces from Jetty's default error pages
            final var errorHandler = new ErrorHandler();
            errorHandler.setShowStacks(false);
            errorHandler.setShowMessageInTitle(false);
            server.setErrorHandler(errorHandler);
            // Create HTTPS listener
            if (httpsPort >= 0) {
                _log.info("Starting the https server on {}:{}", listenAddress, httpsPort);
                // SSL Context Factory
                final var p = Cnf.at("HttpPluginSSL");
                final String storePath;
                final String storePassword;
                final String storeType;
                if (p != null) {
                    storePath = p.get("keyStorePath");
                    storePassword = p.get("keyStorePassword");
                    storeType = getConf(p, "keyStoreType", "PKCS12");
                    _log.info("HttpPlugin SSL: using [HttpPluginSSL] keyStorePath={}", storePath);
                } else {
                    // Fall back to [Security] SSLKeyStore if not set in [HttpPlugin] section
                    final var localPath = getConf("keyStore");
                    storePath = localPath != null ? localPath : Cnf.at("Security", "SSLKeyStore");
                    final var localPassword = getConf("keyStorePassword");
                    storePassword = localPassword != null ? localPassword : Cnf.at("Security", "SSLKeyStorePassword");
                    storeType = getConf("keyStoreType", "PKCS12");
                    _log.info(
                            "HttpPlugin SSL: keyStore from [HttpPlugin]={}, fallback [Security].SSLKeyStore={} → using {}",
                            localPath, Cnf.at("Security", "SSLKeyStore"), storePath);
                }
                // Auto-generate a self-signed certificate if none exists yet
                try {
                    CertificateManager.ensureSelfSigned(storePath, storePassword,
                            java.net.InetAddress.getLocalHost().getHostName());
                } catch (final Exception e) {
                    _log.warn("Could not auto-generate self-signed certificate", e);
                }
                sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setWantClientAuth(Cnf.at("HttpPlugin", "wantClientAuth", false));
                sslContextFactory.setKeyManagerPassword(null);
                sslContextFactory
                        .setIncludeProtocols(Cnf.stringListAt("HttpPlugin", "enabledProtocols", "TLSv1.3,TLSv1.2"));
                sslContextFactory.setKeyStorePath(storePath);
                sslContextFactory.setKeyStorePassword(storePassword);
                sslContextFactory.setKeyStoreType(storeType);
                if (p != null) {
                    sslContextFactory.setTrustStorePath(getConf(p, "trustStorePath", storePath));
                    sslContextFactory.setTrustStorePassword(getConf(p, "trustStorePassword", storePassword));
                    sslContextFactory.setTrustStoreType(getConf(p, "trustStoreType", storeType));
                } else {
                    sslContextFactory.setTrustStorePath(getConf("trustStorePath", storePath));
                    sslContextFactory.setTrustStorePassword(getConf("trustStorePassword", storePassword));
                    sslContextFactory.setTrustStoreType(getConf("trustStoreType", storeType));
                }
                sslContextFactory.setExcludeCipherSuites(Cnf.stringListAt("HttpPlugin", "excludeCipherSuites",
                        "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                        "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"));
                activeKeystorePath = storePath;
                activeKeystorePassword = storePassword;
                activeKeystoreType = storeType;
                // SSL HTTP Configuration
                final var secureRequestCustomizer = new SecureRequestCustomizer();
                secureRequestCustomizer.setSniHostCheck(false); // Allow using localhost
                final var httpsConfig = new HttpConfiguration(httpConfig);
                httpsConfig.addCustomizer(secureRequestCustomizer);
                // SSL Connector
                final var sslConnector = new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfig));
                sslConnector.setPort(httpsPort);
                sslConnector.setHost(listenAddress);
                server.addConnector(sslConnector);
            }
            // Create HTTP listener
            if (httpPort >= 0) {
                _log.info("Starting the http server on {}:{}", listenAddress, httpPort);
                final var connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
                connector.setHost(listenAddress);
                connector.setPort(httpPort);
                server.addConnector(connector);
            }
            // Statistics
            statsHandler = new StatisticsHandler();
            statsHandler.setHandler(server.getHandler());
            server.setHandler(statsHandler);
            server.addBeanToAllConnectors(statsHandler);
            // Starting the server
            server.start();
            return true;
        } catch (final Exception e) {
            _log.error("Starting the plugin", e);
        } finally {
            thread.setContextClassLoader(loader);
        }
        return false;
    }

    /**
     * Gets the rule.
     *
     * @param pattern
     *            the pattern
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return the rule
     */
    private static HeaderPatternRule getRule(final String pattern, final String name, final String value) {
        final var headerRule = new HeaderPatternRule();
        headerRule.setPattern(pattern);
        headerRule.setHeaderName(name);
        headerRule.setHeaderValue(value);
        return headerRule;
    }

    /**
     * Gets the conf.
     *
     * @param p
     *            the p
     * @param keyName
     *            the key name
     * @param defaultValue
     *            the default value
     *
     * @return the conf
     */
    private static String getConf(final Map<String, String> p, final String keyName, final String defaultValue) {
        final var value = p.get(keyName);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the conf.
     *
     * @param keyName
     *            the key name
     *
     * @return the conf
     */
    private static String getConf(final String keyName) {
        return getConf(keyName, null);
    }

    /**
     * Gets the conf.
     *
     * @param keyName
     *            the key name
     * @param defaultValue
     *            the default value
     *
     * @return the conf
     */
    private static String getConf(final String keyName, final String defaultValue) {
        return Cnf.at("Security", "SSL" + keyName.substring(0, 1).toUpperCase() + keyName.substring(1),
                System.getProperty("javax.net.ssl." + keyName, defaultValue));
    }

    /**
     * Reloads the TLS certificate from the keystore currently in use without restarting Jetty (zero-downtime
     * hot-reload).
     *
     * @throws Exception
     *             if the reload fails
     */
    public synchronized void reloadCertificate() throws Exception {
        if (sslContextFactory == null) {
            throw new IllegalStateException("HttpPlugin is not running or HTTPS is disabled");
        }
        _log.info("Hot-reloading TLS certificate from {}", activeKeystorePath);
        sslContextFactory.reload(_ -> {
            // Factory already points to the keystore file; reload re-reads it.
        });
        _log.info("TLS certificate reloaded successfully");
    }

    /**
     * Returns metadata about the certificate currently loaded in the Data Mover HTTPS server.
     *
     * @return a {@link CertificateInfo} snapshot, or {@code null} if the plugin has not been started or no keystore is
     *         configured
     */
    public CertificateInfo getCertificateInfo() {
        if (activeKeystorePath == null) {
            _log.debug(
                    "getCertificateInfo: activeKeystorePath is null — HttpPlugin started without SSL or keystore not resolved");
            return null;
        }
        _log.debug("getCertificateInfo: reading certificate from {}", activeKeystorePath);
        try {
            final var info = CertificateManager.getCertificateInfo(activeKeystorePath, activeKeystorePassword,
                    activeKeystoreType != null ? activeKeystoreType : "PKCS12");
            if (info == null) {
                _log.warn("getCertificateInfo: CertificateManager returned null for {}", activeKeystorePath);
            } else {
                _log.debug("getCertificateInfo: subject={}, expires={}", info.subject(), info.notAfter());
            }
            return info;
        } catch (final Exception e) {
            _log.warn("Could not read certificate info from {}", activeKeystorePath, e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // HttpCertificateProvider
    // -------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String buildCertificateJson() {
        final var info = getCertificateInfo();
        if (info == null) {
            return "{}";
        }
        final var utc = java.util.TimeZone.getTimeZone("UTC");
        final var fmtDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
        fmtDate.setTimeZone(utc);
        final var fmtTime = new java.text.SimpleDateFormat("HH:mm:ss");
        fmtTime.setTimeZone(utc);
        return "{\"subject\":\"" + _esc(info.subject()) + "\"" + ",\"issuer\":\"" + _esc(info.issuer()) + "\""
                + ",\"serialNumber\":\"" + _esc(info.serialNumber()) + "\"" + ",\"notBefore\":\""
                + fmtDate.format(info.notBefore()) + "\"" + ",\"notBeforeTime\":\"" + fmtTime.format(info.notBefore())
                + "\"" + ",\"notAfter\":\"" + fmtDate.format(info.notAfter()) + "\"" + ",\"notAfterTime\":\""
                + fmtTime.format(info.notAfter()) + "\"" + ",\"fingerprintSha256\":\"" + _esc(info.fingerprintSha256())
                + "\"" + ",\"keyAlgorithm\":\"" + _esc(info.keyAlgorithm()) + "\"" + ",\"keySize\":" + info.keySize()
                + ",\"selfSigned\":" + info.selfSigned() + ",\"expired\":" + info.expired() + ",\"expiringSoon\":"
                + info.expiringSoon() + "}";
    }

    /** {@inheritDoc} */
    @Override
    public void deployCertificate(final byte[] pkcs12Bytes, final String keystorePassword) throws Exception {
        if (activeKeystorePath == null) {
            throw new IllegalStateException("HttpPlugin has no active keystore path");
        }
        CertificateManager.importCertificate(activeKeystorePath, activeKeystorePassword, pkcs12Bytes, keystorePassword);
        reloadCertificate();
    }

    private static String _esc(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the path to the keystore currently in use by the Data Mover HTTPS server, or {@code null} if the plugin
     * has not been started.
     *
     * @return the active keystore path
     */
    public String getActiveKeystorePath() {
        return activeKeystorePath;
    }

    /**
     * Returns the password for the keystore currently in use by the Data Mover HTTPS server.
     *
     * @return the active keystore password
     */
    public String getActiveKeystorePassword() {
        return activeKeystorePassword;
    }

    /**
     * {@inheritDoc}
     *
     * Stop.
     */
    @Override
    public synchronized void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (final Exception e) {
                _log.warn(e);
            } finally {
                server = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            // Check for a recognised attribute_name and call the corresponding getter
            if ("Requests".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getRequests();
            }
            if ("RequestsActive".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getRequestsActive();
            }
            if ("RequestsActiveMax".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getRequestsActiveMax();
            }
            if ("Responses1xx".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getResponses1xx();
            }
            if ("Responses2xx".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getResponses2xx();
            }
            if ("Responses3xx".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getResponses3xx();
            }
            if ("Responses4xx".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getResponses4xx();
            }
            if ("Responses5xx".equals(attributeName)) {
                return server == null ? 0 : statsHandler.getResponses5xx();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        try {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The " + getRef() + " plugin allows Member State users to submit/monitor "
                            + "jobs and to transfer files (between their own computer and ECMWF) "
                            + "using a standard WEB browser. " + super.getMBeanInfo().getDescription(),
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("Requests", "java.lang.Integer",
                                    "Requests: number of requests accepted by the server since statsReset() called.",
                                    true, false, false),
                            new MBeanAttributeInfo("RequestsActive", "java.lang.Integer",
                                    "RequestsActive: number of active requests currently open since statsReset() called.",
                                    true, false, false),
                            new MBeanAttributeInfo("RequestsActiveMax", "java.lang.Integer",
                                    "RequestsActiveMax: maximum number of active requests opened simultaneously since statsReset() called.",
                                    true, false, false),
                            new MBeanAttributeInfo("Responses1xx", "java.lang.Integer",
                                    "Responses1xx: number of 1xx responses since statsReset() called.", true, false,
                                    false),
                            new MBeanAttributeInfo("Responses2xx", "java.lang.Integer",
                                    "Responses1xx: number of 2xx responses since statsReset() called.", true, false,
                                    false),
                            new MBeanAttributeInfo("Responses3xx", "java.lang.Integer",
                                    "Responses1xx: number of 3xx responses since statsReset() called.", true, false,
                                    false),
                            new MBeanAttributeInfo("Responses4xx", "java.lang.Integer",
                                    "Responses1xx: number of 4xx responses since statsReset() called.", true, false,
                                    false),
                            new MBeanAttributeInfo("Responses5xx", "java.lang.Integer",
                                    "Responses1xx: number of 5xx responses since statsReset() called.", true, false,
                                    false) },
                    new MBeanOperationInfo[] { new MBeanOperationInfo("statsReset", "statsReset(): reset statistics.",
                            null, "void", MBeanOperationInfo.ACTION) });
        } catch (final LinkageError e) {
            return super.getMBeanInfo();
        } catch (final Throwable t) {
            _log.debug("getMBeanInfo", t);
            return super.getMBeanInfo();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("statsReset".equals(operationName)) {
                if (statsHandler != null) {
                    statsHandler.reset();
                }
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }
}
