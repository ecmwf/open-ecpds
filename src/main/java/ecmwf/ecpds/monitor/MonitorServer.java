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

package ecmwf.ecpds.monitor;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.rmi.RemoteException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;

/**
 * The Class MonitorServer.
 *
 * <p>
 * Entry point for the ECpds Monitor daemon. Replaces the legacy {@code ecmwf.common.ecaccess.HandlerServer} as the
 * RMI-exported server object on monitor hosts.
 *
 * <p>
 * By implementing {@link MonitorInterface} (which is part of open-ecpds rather than the pre-compiled
 * {@code ecaccess-stubs.jar}), Java RMI uses a dynamic proxy when the master connects to this server. A dynamic proxy
 * forwards all non-default abstract methods — including {@link #getHttpCertificateJson()} and
 * {@link #deployHttpCertificate} — to this remote object over the network. This resolves the Java 9+ RMI issue where
 * {@code default} interface methods on {@code HandlerInterface} were invoked locally on the master JVM instead of being
 * forwarded to the remote monitor.
 *
 * <p>
 * Start monitors with:
 *
 * <pre>{@code
 * JAVA_OPTS = "$JAVA_OPTS -Decmwf.common.starter.name=ecmwf.ecpds.monitor.MonitorServer"
 * }</pre>
 *
 * <p>
 * The service name ({@code "ECpdsMonitor"}) is unchanged, so existing master configuration and the
 * {@code getMonitorInterface()} lookup are backward-compatible at the service-registration level.
 */
public final class MonitorServer extends StarterServer implements MonitorInterface {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitorServer.class);

    /** The Constant _VERSION. */
    private static final String _VERSION = Version.getFullVersion();

    /**
     * Instantiates a new MonitorServer.
     *
     * @param starter
     *            the starter
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws javax.management.InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws javax.management.MBeanRegistrationException
     *             the MBean registration exception
     * @throws javax.management.NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws javax.management.MalformedObjectNameException
     *             the malformed object name exception
     * @throws javax.management.InstanceNotFoundException
     *             the instance not found exception
     */
    public MonitorServer(final Starter starter)
            throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException {
        super(starter);
        _log.info("MonitorServer-version: " + _VERSION);
        final var container = getPluginContainer();
        container.loadPlugins();
        container.startPlugins();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the root.
     */
    @Override
    public String getRoot() {
        return Cnf.at("Login", "root", Cnf.at("Login", "hostName"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the password.
     */
    @Override
    public String getPassword() {
        return Cnf.at("Login", "password");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return _VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the service name used to register this monitor with the master. The service name {@code "ECpdsMonitor"}
     * is unchanged from the legacy {@code HandlerServer} so that the master's {@code getMonitorInterface()} lookup
     * continues to work without configuration changes.
     */
    @Override
    public String getService() {
        return Cnf.at("Login", "service", "ECpdsMonitor");
    }

    /**
     * {@inheritDoc}
     *
     * Returns a JSON-encoded snapshot of the TLS certificate currently loaded by this monitor's HTTPS server. Delegates
     * to the {@code http} plugin via reflection to avoid classloader-isolation issues.
     */
    @Override
    public String getHttpCertificateJson() throws RemoteException {
        final var container = getPluginContainer();
        if (container == null) {
            _log.warn("getHttpCertificateJson: no plugin container");
            return "{}";
        }
        final var plugin = container.getPlugin("http");
        if (plugin == null) {
            _log.warn("getHttpCertificateJson: 'http' plugin not found in container");
            return "{}";
        }
        try {
            final var method = plugin.getClass().getMethod("buildCertificateJson");
            final var json = (String) method.invoke(plugin);
            if (json == null || "{}".equals(json)) {
                _log.warn("getHttpCertificateJson: buildCertificateJson() returned empty — "
                        + "activeKeystorePath may be null");
                return "{}";
            }
            return json;
        } catch (final NoSuchMethodException e) {
            _log.warn("getHttpCertificateJson: 'http' plugin ({}) does not have buildCertificateJson()",
                    plugin.getClass().getName());
        } catch (final Exception e) {
            _log.warn("getHttpCertificateJson: failed to build certificate JSON", e);
        }
        return "{}";
    }

    /**
     * {@inheritDoc}
     *
     * Hot-reloads the TLS certificate from the keystore file currently on disk on this Monitor. Delegates to the
     * {@code http} plugin via reflection to avoid classloader-isolation issues.
     */
    @Override
    public void reloadHttpCertificate() throws RemoteException {
        final var container = getPluginContainer();
        if (container == null) {
            _log.warn("reloadHttpCertificate: no plugin container");
            return;
        }
        final var plugin = container.getPlugin("http");
        if (plugin == null) {
            _log.warn("reloadHttpCertificate: 'http' plugin not found in container");
            return;
        }
        try {
            final var method = plugin.getClass().getMethod("reloadCertificate");
            method.invoke(plugin);
        } catch (final NoSuchMethodException e) {
            _log.warn("reloadHttpCertificate: 'http' plugin ({}) does not have reloadCertificate()",
                    plugin.getClass().getName());
        } catch (final java.lang.reflect.InvocationTargetException e) {
            final var cause = e.getCause() != null ? e.getCause() : e;
            _log.warn("reloadHttpCertificate: failed to reload certificate", cause);
            throw new RemoteException("Certificate reload failed", cause);
        } catch (final Exception e) {
            _log.warn("reloadHttpCertificate: failed to reload certificate", e);
            throw new RemoteException("Certificate reload failed", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Deploys a new PKCS#12 keystore to this monitor's HTTPS server. Delegates to the {@code http} plugin via
     * reflection to avoid classloader-isolation issues.
     */
    @Override
    public void deployHttpCertificate(final byte[] pkcs12Bytes, final String keystorePassword) throws RemoteException {
        final var container = getPluginContainer();
        if (container == null) {
            _log.warn("deployHttpCertificate: no plugin container");
            return;
        }
        final var plugin = container.getPlugin("http");
        if (plugin == null) {
            _log.warn("deployHttpCertificate: 'http' plugin not found in container");
            return;
        }
        try {
            final var method = plugin.getClass().getMethod("deployCertificate", byte[].class, String.class);
            method.invoke(plugin, pkcs12Bytes, keystorePassword);
        } catch (final NoSuchMethodException e) {
            _log.warn("deployHttpCertificate: 'http' plugin ({}) does not have deployCertificate(byte[],String)",
                    plugin.getClass().getName());
        } catch (final java.lang.reflect.InvocationTargetException e) {
            final var cause = e.getCause() != null ? e.getCause() : e;
            _log.warn("deployHttpCertificate: failed to deploy certificate", cause);
            throw new RemoteException("Certificate deployment failed", cause);
        } catch (final Exception e) {
            _log.warn("deployHttpCertificate: failed to deploy certificate", e);
            throw new RemoteException("Certificate deployment failed", e);
        }
    }
}
