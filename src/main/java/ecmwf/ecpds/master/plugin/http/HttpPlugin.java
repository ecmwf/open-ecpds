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

package ecmwf.ecpds.master.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.timer.Timer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.rewrite.handler.HeaderPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configurations;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.plugin.PluginThread;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.security.Tools;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.ChangeHostEvent;
import ecmwf.ecpds.master.DataBaseInterface;
import ecmwf.ecpds.master.DataTransferEvent;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.ProductStatusEvent;
import ecmwf.ecpds.master.ResetDestinationProductEvent;
import ecmwf.ecpds.master.ResetProductEvent;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * The Class HttpPlugin.
 */
public final class HttpPlugin extends PluginThread implements HandlerReceiver {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(HttpPlugin.class);

    /** The Constant NAME. */
    private static final String NAME = "MonitorPlugin";

    /** The Constant VERSION. */
    private static final String VERSION = Version.getFullVersion();

    /** The httpServer. */
    private Server httpServer = null;

    /** The statisticsHandler. */
    private StatisticsHandler statisticsHandler = null;

    /** The eventHandler. */
    private EventHandler eventHandler = null;

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
        if (httpServer != null) {
            return true;
        }
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(HttpPlugin.class.getClassLoader());
        var started = false;
        try {
            final var httpsPort = Cnf.at("MonitorPlugin", "https", -1);
            if (httpsPort < 0) {
                throw new IOException("Not a valid https port number: " + httpsPort);
            }
            final var listenAddress = new SocketConfig("MonitorPlugin").getListenAddress();
            // Home?
            final var jettyHome = Cnf.at("MonitorPlugin", "htdocs");
            // Thread pooling mechanism
            final var threadPool = new QueuedThreadPool(Cnf.at("HttpPlugin", "maxThreads", 400),
                    Cnf.at("HttpPlugin", "minThreads", 40), Cnf.at("HttpPlugin", "idleThreadsTimeout", 120));
            // Create the server
            httpServer = new Server(threadPool);
            httpServer.manage(threadPool);
            // Http configuration
            final var httpConfig = new HttpConfiguration();
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(httpsPort);
            httpConfig.setOutputBufferSize(32768);
            httpConfig.setRequestHeaderSize(8192);
            httpConfig.setResponseHeaderSize(8192);
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
            // War deployer for monitor
            final var monitor = new WebAppContext();
            monitor.setContextPath("/");
            monitor.setParentLoaderPriority(false);
            monitor.setAllowDuplicateFragmentNames(false);
            monitor.setAttribute("ecpds.HttpPlugin", this);
            monitor.setWar(new File(jettyHome).getAbsolutePath());
            monitor.setExtractWAR(false);
            monitor.addFilter(new FilterHolder(new AccessRestrictionFilter()), "/*", null);
            final var sessionHandler = monitor.getSessionHandler();
            sessionHandler.setMaxInactiveInterval(-1);
            final var cookieconfig = sessionHandler.getSessionCookieConfig();
            cookieconfig.setName(Cnf.at("MonitorPlugin", "cookie", "ecpds" + httpsPort));
            cookieconfig.setHttpOnly(true);
            cookieconfig.setSecure(true);
            cookieconfig.setMaxAge(-1);
            cookieconfig.setComment("__SAME_SITE_STRICT__");
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
            // Take care of certificate and environment parameters!
            final var rewrite = new RewriteHandler();
            rewrite.addRule(new SessionRule());
            // Add security headers
            rewrite.addRule(getRule("*", "X-XSS-Protection", "1; mode=block"));
            rewrite.addRule(getRule("*", "X-Content-Type-Options", "nosniff"));
            rewrite.addRule(getRule("*", "Content-Security-Policy",
                    "script-src 'self' 'unsafe-eval' 'unsafe-inline' blob:; style-src 'self' 'unsafe-inline';"));
            rewrite.addRule(getRule("*", "X-Frame-Options", "SAMEORIGIN"));
            rewrite.addRule(getRule("*", "Strict-Transport-Security", "max-age=31536000;includeSubDomains"));
            // Add all the handlers to the server!
            final var handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { rewrite, sh, resource, ecpds, monitor, new DefaultHandler() });
            httpServer.setHandler(handlers);
            // Create HTTPS listener
            _log.info("Starting the https server on {}:{}", listenAddress, httpsPort);
            // SSL Context Factory
            final var sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setWantClientAuth(Cnf.at("MonitorPlugin", "wantClientAuth", false));
            sslContextFactory.setKeyManagerPassword(null);
            final var includeProtocols = Cnf.stringListAt("MonitorPlugin", "enabledProtocols", "TLSv1.3,TLSv1.2");
            if (includeProtocols.length > 0) {
                sslContextFactory.setIncludeProtocols(includeProtocols);
            }
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
            final var excludeCipherSuites = Cnf.stringListAt("MonitorPlugin", "excludeCipherSuites",
                    "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                    "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
            if (excludeCipherSuites.length > 0) {
                sslContextFactory.setExcludeCipherSuites(excludeCipherSuites);
            }
            // SSL HTTP Configuration
            final var secureRequestCustomizer = new SecureRequestCustomizer();
            secureRequestCustomizer.setSniHostCheck(false); // Allow using localhost
            final var httpsConfig = new HttpConfiguration(httpConfig);
            httpsConfig.addCustomizer(secureRequestCustomizer);
            // SSL Connector
            _log.debug("Starting HTTPS server on {}:{}", listenAddress, httpsPort);
            final var sslConnector = new ServerConnector(httpServer,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfig));
            sslConnector.setPort(httpsPort);
            sslConnector.setHost(listenAddress);
            httpServer.addConnector(sslConnector);
            // Enable compression on the fly if requested
            if (Cnf.at("MonitorPlugin", "compression", true)) {
                final var gzipHandler = new GzipHandler();
                gzipHandler.setHandler(httpServer.getHandler());
                httpServer.setHandler(gzipHandler);
            }
            // Statistics
            statisticsHandler = new StatisticsHandler();
            statisticsHandler.setHandler(httpServer.getHandler());
            httpServer.setHandler(statisticsHandler);
            httpServer.addBeanToAllConnectors(statisticsHandler);
            // Allow using jsps!
            Configurations.setServerDefault(httpServer).add(new JettyWebXmlConfiguration(),
                    new AnnotationConfiguration());
            // If using the cache, this is starting the cache management thread!
            try {
                MasterManager.getMI();
            } catch (final Exception e) {
                _log.warn("Cannot get ManagementInterface", e);
            }
            // Starting the server to initialize the event handler!
            httpServer.start();
            // Now we can wait
            waitForMasterConnection(Cnf.at("MonitorPlugin", "initialiseEventHandler", true));
            started = true;
        } catch (final MultiException e) {
            final List<?> list = e.getThrowables();
            for (final Object element : list) {
                _log.error("Starting the plugin", (Exception) element);
            }
        } catch (final Throwable t) {
            _log.error("Starting the plugin", t);
        } finally {
            if (!started) {
                stop();
            }
            thread.setContextClassLoader(loader);
        }
        return started;
    }

    /**
     * The Class AccessRestrictionFilter.
     */
    @WebFilter("/*")
    public static class AccessRestrictionFilter implements Filter {

        /**
         * Inits the.
         *
         * @param filterConfig
         *            the filter config
         *
         * @throws ServletException
         *             the servlet exception
         */
        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            // No initialization needed
        }

        /**
         * Do filter.
         *
         * @param request
         *            the request
         * @param response
         *            the response
         * @param chain
         *            the chain
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws ServletException
         *             the servlet exception
         */
        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
                throws IOException, ServletException {
            final var httpRequest = (HttpServletRequest) request;
            final var httpResponse = (HttpServletResponse) response;
            final var requestURI = httpRequest.getRequestURI();
            // Check if the request URI starts with /webapps
            if (requestURI.startsWith("/webapps/")) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to this directory is forbidden.");
                return;
            }
            chain.doFilter(request, response);
        }

        /**
         * Destroy.
         */
        @Override
        public void destroy() {
            // No cleanup needed
        }
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
     * {@inheritDoc}
     *
     * Register event handler.
     */
    @Override
    public void registerEventHandler(final EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Initialise event handler.
     *
     * @param initialiseEventHandler
     *            the initialise event handler
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public void waitForMasterConnection(final boolean initialiseEventHandler) throws RemoteException {
        if (initialiseEventHandler) {
            subscribe(ProductStatusEvent.NAME);
            subscribe(DataTransferEvent.NAME);
            subscribe(ChangeHostEvent.NAME);
            subscribe(ResetProductEvent.NAME);
            subscribe(ResetDestinationProductEvent.NAME);
        }
        while (true) {
            final var masterReady = MasterManager.isReady();
            DataBaseInterface database = null;
            try {
                if (masterReady) {
                    database = MasterManager.getDB();
                    // Getting the destination options from the Master
                    DestinationOption.setList(database.getDestinationOptionList());
                    // Trigger the initial events!
                    if (initialiseEventHandler) {
                        _log.debug("Triggering initial events");
                        final var root = MasterManager.getRoot();
                        database.getInitialProductStatusEvents(root);
                        database.getInitialDataTransferEvents(root);
                        database.getInitialChangeHostEvents(root);
                    }
                    return;
                }
            } catch (final Exception e) {
                _log.error("waitForMasterConnection", e);
            }
            try {
                _log.warn("MasterReady: {}, DBReady: {}, waiting!", masterReady, database != null);
                this.wait(5 * Timer.ONE_SECOND);
            } catch (final InterruptedException ignored) {
                // Ignored!
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Stop.
     */
    @Override
    public synchronized void stop() {
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (final Exception e) {
                _log.warn(e);
            } finally {
                unSubscribe(ProductStatusEvent.NAME);
                unSubscribe(DataTransferEvent.NAME);
                unSubscribe(ChangeHostEvent.NAME);
                unSubscribe(ResetProductEvent.NAME);
                unSubscribe(ResetDestinationProductEvent.NAME);
                httpServer = null;
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
            // Check for a recognized attribute_name and call the corresponding
            // getter
            if ("Requests".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getRequests();
            }
            if ("RequestsActive".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getRequestsActive();
            }
            if ("RequestsActiveMax".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getRequestsActiveMax();
            }
            if ("Responses1xx".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getResponses1xx();
            }
            if ("Responses2xx".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getResponses2xx();
            }
            if ("Responses3xx".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getResponses3xx();
            }
            if ("Responses4xx".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getResponses4xx();
            }
            if ("Responses5xx".equals(attributeName)) {
                return httpServer == null ? 0 : statisticsHandler.getResponses5xx();
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
                    "The " + getRef() + " plugin allows operators to manage/monitor "
                            + "MS jobs using a standard WEB browser. " + super.getMBeanInfo().getDescription(),
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("Requests", "java.lang.Integer",
                                    "Requests: number of requests accepted by the server since statsReset() called.",
                                    true, false, false),
                            new MBeanAttributeInfo("RequestsActive", "java.lang.Integer",
                                    "RequestsActive: number of active requests currently open since statsReset() called.",
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
                if (statisticsHandler != null) {
                    statisticsHandler.statsReset();
                }
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * {@inheritDoc}
     *
     * Handle.
     */
    @Override
    public void handle(final PluginEvent<?> event) {
        if (eventHandler != null) {
            eventHandler.handle(event);
        } else {
            _log.warn("MonitoringEventHandler not registered!");
        }
    }

    /**
     * The Class SessionRule.
     */
    public static class SessionRule extends Rule {

        /**
         * Gets the user name from domain.
         *
         * @param dn
         *            the dn
         *
         * @return the user name from domain
         */
        private static final String getUserNameFromDomain(String dn) {
            if (dn.indexOf(" OID.") != -1) {
                final var index = dn.lastIndexOf("=");
                if (index != -1) {
                    dn = dn.substring(index + 1);
                }
            } else {
                final var index1 = dn.indexOf("(");
                final var index2 = dn.indexOf(")");
                if (index2 > index1) {
                    dn = dn.substring(index1 + 1, index2);
                } else {
                    final var index = dn.indexOf("CN=");
                    if (index != -1) {
                        dn = dn.substring(index + 3);
                    }
                }
            }
            if (dn.length() <= 8) {
                return dn;
            }
            return null;
        }

        /**
         * Match and apply.
         *
         * @param target
         *            the target
         * @param request
         *            the request
         * @param response
         *            the response
         *
         * @return the string
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public String matchAndApply(final String target, final HttpServletRequest request,
                final HttpServletResponse response) throws IOException {
            if (request.isSecure()) {
                try {
                    final var certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
                    if (certs != null && certs.length > 0) {
                        final var domain = certs[0].getSubjectX500Principal().getName();
                        final var userId = getUserNameFromDomain(domain);
                        if (userId != null && userId.length() <= 8) {
                            _log.debug("Certificate detected (uid: {})", userId);
                            request.setAttribute("ECPDS-NAME", userId);
                            request.setAttribute("ECPDS-CERT", Tools.toPEM(certs[0]));
                        } else {
                            _log.warn("Certificate not recognized: {}", domain);
                        }
                    }
                } catch (final Exception e) {
                    _log.warn("customizeRequest", e);
                }
            }
            return null;
        }
    }
}
