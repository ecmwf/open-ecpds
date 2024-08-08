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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.rewrite.handler.HeaderPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Handler;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.plugin.PluginThread;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;

/**
 * The Class HttpPlugin.
 */
public final class HttpPlugin extends PluginThread {
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
     * Gets the plugin name.
     *
     * @return the plugin name
     */
    @Override
    public String getPluginName() {
        return NAME;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * Start.
     *
     * @return true, if successful
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
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(httpsPort);
            httpConfig.setOutputBufferSize(Cnf.at("HttpPlugin", "outputBufferSize", 32768));
            httpConfig.setRequestHeaderSize(Cnf.at("HttpPlugin", "requestHeaderSize", 8192));
            httpConfig.setResponseHeaderSize(Cnf.at("HttpPlugin", "responseHeaderSize", 8192));
            httpConfig.setSendServerVersion(false);
            httpConfig.setSendDateHeader(false);
            httpConfig.setSendXPoweredBy(false);
            // War deployer for ecpds
            final var ecpds = new WebAppContext();
            ecpds.setContextPath("/ecpds/");
            ecpds.setExtractWAR(true);
            ecpds.setParentLoaderPriority(false);
            ecpds.setAllowDuplicateFragmentNames(false);
            final var ecpdsFile = new File(jettyHome + "/webapps/ecpds.war");
            ecpds.setWar(ecpdsFile.getAbsolutePath());
            // Security
            final var constraint = new Constraint();
            constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);
            final var cm = new ConstraintMapping();
            cm.setConstraint(constraint);
            cm.setPathSpec("/*");
            final var sh = new ConstraintSecurityHandler();
            sh.setConstraintMappings(new ConstraintMapping[] { cm });
            // Resources
            final var resource = new ResourceHandler();
            resource.setDirectoriesListed(false);
            resource.setWelcomeFiles(new String[] { "index.html" });
            resource.setResourceBase(jettyHome + "/resources");
            // Statistics
            final var stats = new StatisticsHandler();
            stats.setHandler(server.getHandler());
            // AmazonS3
            final var s3ServicePath = Cnf.at("HttpPlugin", "s3ServicePath", "/s3");
            final var s3proxy = new ContextHandler();
            s3proxy.setContextPath(s3ServicePath);
            s3proxy.setHandler(new S3ProxyHandlerJetty(
                    AuthenticationType.fromString(Cnf.at("HttpPlugin", "s3AuthenticationType", "AWS_V2_OR_V4")),
                    Cnf.at("HttpPlugin", "s3V4MaxNonChunkedRequestSize", 32 * 1024 * 1024),
                    Cnf.at("HttpPlugin", "s3IgnoreUnknownHeaders", true), new CrossOriginResourceSharing(),
                    s3ServicePath, Cnf.at("HttpPlugin", "s3MaximumTimeSkew", 15 * 60)));
            // Add security headers
            final var rewrite = new RewriteHandler();
            rewrite.addRule(getRule("*", "X-XSS-Protection", "1; mode=block"));
            rewrite.addRule(getRule("*", "X-Content-Type-Options", "nosniff"));
            rewrite.addRule(getRule("*", "Content-Security-Policy",
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'"));
            rewrite.addRule(getRule("*", "X-Frame-Options", "SAMEORIGIN"));
            rewrite.addRule(getRule("*", "Strict-Transport-Security", "max-age=31536000;includeSubDomains"));
            // Handling requests with a server name mapping to a data user
            final Handler dns = new AbstractHandler() {
                @Override
                public void handle(final String target, final Request jettyRequest, final HttpServletRequest request,
                        final HttpServletResponse response) throws IOException, ServletException {
                    for (final String dnsPath : Cnf.listAt("HttpPlugin", "dnsPathList")) {
                        final var dnsAndPath = dnsPath.split("="); // e.g, opendata=forecasts
                        if (dnsAndPath.length == 2) {
                            final var dns = dnsAndPath[0]; // must map a data user!
                            if (dns.equals(request.getServerName())) {
                                final var path = dnsAndPath[1];
                                final var url = "/".equals(target) ? "/" + path + "/" : target;
                                if (url.startsWith("/" + path + "/")) {
                                    request.setAttribute("original-target", "/" + path);
                                    ecpds.getServletContext()
                                            .getRequestDispatcher(
                                                    url.replaceFirst("^/" + path + "/", "/home/" + dns + "/"))
                                            .forward(request, response);
                                    jettyRequest.setHandled(true);
                                    break;
                                }
                            }
                        } else {
                            _log.warn("Malformed element for dnsPathList: {}", dnsPath);
                        }
                    }
                }
            };
            // Add all the handlers to the server!
            final var handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { dns, rewrite, sh, resource, ecpds, s3proxy, new DefaultHandler() });
            server.setHandler(handlers);
            // Create HTTPS listener
            if (httpsPort >= 0) {
                _log.info("Starting the https server on {}:{}", listenAddress, httpsPort);
                // SSL Context Factory
                final var sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setWantClientAuth(Cnf.at("HttpPlugin", "wantClientAuth", false));
                sslContextFactory.setKeyManagerPassword(null);
                sslContextFactory
                        .setIncludeProtocols(Cnf.stringListAt("HttpPlugin", "enabledProtocols", "TLSv1.3,TLSv1.2"));
                final var p = Cnf.at("HttpPluginSSL");
                if (p != null) {
                    // Using the customized SSL configuration!
                    final var storePath = p.get("keyStorePath");
                    final var storePassword = p.get("keyStorePassword");
                    final var storeType = getConf(p, "keyStoreType", "PKCS12");
                    sslContextFactory.setKeyStorePath(storePath);
                    sslContextFactory.setKeyStorePassword(storePassword);
                    sslContextFactory.setKeyStoreType(storeType);
                    sslContextFactory.setTrustStorePath(getConf(p, "trustStorePath", storePath));
                    sslContextFactory.setTrustStorePassword(getConf(p, "trustStorePassword", storePassword));
                    sslContextFactory.setTrustStoreType(getConf(p, "trustStoreType", storeType));
                } else {
                    // Using the default SSL configuration!
                    final var storePath = getConf("keyStore");
                    final var storePassword = getConf("keyStorePassword");
                    final var storeType = getConf("keyStoreType", "PKCS12");
                    sslContextFactory.setKeyStorePath(storePath);
                    sslContextFactory.setKeyStorePassword(storePassword);
                    sslContextFactory.setKeyStoreType(storeType);
                    sslContextFactory.setTrustStorePath(getConf("trustStorePath", storePath));
                    sslContextFactory.setTrustStorePassword(getConf("trustStorePassword", storePassword));
                    sslContextFactory.setTrustStoreType(getConf("trustStoreType", storeType));
                }
                sslContextFactory.setExcludeCipherSuites(Cnf.stringListAt("HttpPlugin", "excludeCipherSuites",
                        "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                        "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"));
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
        } catch (final MultiException e) {
            for (final Throwable t : e.getThrowables()) {
                _log.error("Starting the plugin", t);
            }
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
        headerRule.setName(name);
        headerRule.setValue(value);
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
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
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
     * Gets the MBean info.
     *
     * @return the MBean info
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
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("statsReset".equals(operationName)) {
                if (statsHandler != null) {
                    statsHandler.statsReset();
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
