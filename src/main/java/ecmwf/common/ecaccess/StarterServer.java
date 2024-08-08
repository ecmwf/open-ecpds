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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Date;
import java.util.stream.IntStream;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.callback.RemoteCnf;
import ecmwf.common.callback.RemoteCnfImp;
import ecmwf.common.callback.RemoteEngineThread;
import ecmwf.common.mbean.MBeanCenter;
import ecmwf.common.mbean.MBeanListener;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.mbean.MBeanService;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.plugin.PluginContainer;
import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.plugin.PluginInfo;
import ecmwf.common.starter.Starter;
import ecmwf.common.starter.ToBeStarted;
import ecmwf.common.technical.Singletons;
import ecmwf.common.text.Format;

/**
 * The Class StarterServer.
 */
public abstract class StarterServer extends CallBackObject
        implements MBeanService, MBeanListener, ToBeStarted, RemoteServer {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7136207258322920832L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(StarterServer.class);

    /** The starter. */
    private final transient Starter starter;

    /** The mBeanCenter. */
    private final transient MBeanCenter mBeanCenter;

    /** The plugins. */
    private final transient PluginContainer plugins;

    /** The_monitor. */
    private final transient MonitorThread monitor;

    /** The remoteCnf. */
    private final transient RemoteCnfImp remoteCnf;

    /** The graceful. */
    private transient boolean graceful = true;

    /** The restart. */
    private transient boolean restart = true;

    /** The startDate. */
    private final transient Date startDate;

    /**
     * Instantiates a new starter server.
     *
     * @param starter
     *            the starter
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     * @throws InstanceNotFoundException
     *             the instance not found exception
     */
    protected StarterServer(final Starter starter)
            throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException {
        Singletons.save(StarterServer.class, this);
        final Provider provider = new BouncyCastleProvider();
        final var position = System.getProperty("bouncyCastleProviderPosition");
        if (position != null) {
            try {
                Security.insertProviderAt(provider, Integer.parseInt(position));
            } catch (final NumberFormatException ignored) {
                // We don't set it!
            }
        } else {
            Security.insertProviderAt(provider, 2);
        }
        if (_log.isInfoEnabled()) {
            _log.info("Starting {} (Java{})", Format.getClassName(this), System.getProperty("java.vm.version"));
        }
        if (_log.isDebugEnabled()) {
            System.getProperties().entrySet().stream()
                    .forEach(entry -> _log.debug("Property {}: {}", entry.getKey(), entry.getValue()));
            final var providers = Security.getProviders();
            IntStream.range(0, providers.length).forEach(i -> _log.debug("Provider[{}]={} ({})", i,
                    providers[i].getName(), providers[i].getClass().getName()));
        }
        if (MonitorManager.isActivated()) {
            _log.info("Starting MonitorThread");
            MonitorThread monitorInstance = null;
            try {
                monitorInstance = MonitorThread.getInstance();
            } catch (final MonitorException e) {
                _log.warn("Getting MonitorThread instance", e);
            }
            this.monitor = monitorInstance;
        } else {
            this.monitor = null;
        }
        this.starter = starter;
        this.startDate = new Date();
        this.mBeanCenter = MBeanCenter.createMBeanCenter(this.starter.getMBeanServer());
        this.mBeanCenter.registerMBeanTimer("ECaccess:service=Timer");
        this.plugins = new PluginContainer(this);
        this.remoteCnf = new RemoteCnfImp();
        new MBeanManager("ECaccess:service=" + Format.getClassName(this), this);
        new MBeanManager("ECaccess:service=PluginContainer", plugins);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        return false;
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
            if ("Version".equals(attributeName)) {
                return getVersion();
            }
            if ("StartDate".equals(attributeName)) {
                return getStartDate();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
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
            if ("shutdown".equals(operationName) && signature.length == 2 && "java.lang.Boolean".equals(signature[0])
                    && "java.lang.Boolean".equals(signature[1])) {
                shutdown(((Boolean) params[0]), ((Boolean) params[1]));
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public abstract String getVersion();

    /**
     * Gets the graceful.
     *
     * @return the graceful
     */
    public boolean getGraceful() {
        return graceful;
    }

    /**
     * Gets the restart.
     *
     * @return the restart
     */
    public boolean getRestart() {
        return restart;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(), """
                The Starter server initialize the basic ECaccess \
                components, including the plugins container \
                and the Management Bean interfaces.""",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Version", "java.lang.String", "Version: GatewayServer version number.",
                                true, false, false),
                        new MBeanAttributeInfo("StartDate", "java.util.Date",
                                "StartDate: when the gateway has been started.", true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] { new MBeanOperationInfo("shutdown",
                        "shutdown(graceful,restart): shutdown the application",
                        new MBeanParameterInfo[] {
                                new MBeanParameterInfo("graceful", "java.lang.Boolean", "graceful shutdown"),
                                new MBeanParameterInfo("restart", "java.lang.Boolean", "restart after shutdown") },
                        "void", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
    }

    /**
     * Shutdown.
     *
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     */
    public synchronized void shutdown(final boolean graceful, final boolean restart) {
        this.graceful = graceful;
        this.restart = restart;
        if (restart) {
            starter.restart();
        } else {
            starter.shutdown();
        }
    }

    /**
     * Shutdown.
     */
    @Override
    public synchronized void shutdown() {
        plugins.stopPlugins();
        plugins.unregisterPlugins();
        _log.info("Preparing to logout (5sec)");
        try {
            wait(5000);
        } catch (final InterruptedException ignored) {
            // Ignore exception!
        }
        mBeanCenter.unregisterMBeans();
        RemoteEngineThread.removeAll();
        if (MonitorManager.isActivated()) {
            monitor.shutdown();
        }
    }

    /**
     * Exit.
     */
    public void exit() {
        if (_log.isInfoEnabled()) {
            _log.info("Exit called for {}", Format.getClassName(this));
        }
        starter.shutdown();
    }

    /**
     * Gets the plugin container.
     *
     * @return the plugin container
     */
    public PluginContainer getPluginContainer() {
        return plugins;
    }

    /**
     * Handle.
     *
     * @param events
     *            the events
     */
    public void handle(final PluginEvent<?>[] events) {
        if (plugins != null) {
            plugins.notify(events);
        }
    }

    /**
     * Handle.
     *
     * @param event
     *            the event
     */
    public void handle(final PluginEvent<?> event) {
        handle(new PluginEvent[] { event });
    }

    /**
     * Gets the plugin infos.
     *
     * @return the plugin infos
     */
    @Override
    public PluginInfo[] getPluginInfos() {
        return plugins.getPluginInfos();
    }

    /**
     * Gets the remote cnf.
     *
     * @return the remote cnf
     */
    public synchronized RemoteCnf getRemoteCnf() {
        return remoteCnf;
    }

    /**
     * Handle notification.
     *
     * @param name
     *            the name
     */
    @Override
    public void handleNotification(final String name) {
        try {
            this.getClass().getMethod(name).invoke(this);
        } catch (final Exception e) {
            _log.warn("Starting scheduled task: {}", name, e);
        }
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Gets the MBean center.
     *
     * @return the MBean center
     */
    public MBeanCenter getMBeanCenter() {
        return mBeanCenter;
    }

    /**
     * Gets the single instance of StarterServer.
     *
     * @return the object
     */
    public static final Object getInstance() {
        return Singletons.get(StarterServer.class);
    }

    /**
     * Gets the single instance of StarterServer in the requested type.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the t object
     */
    public static final <T> T getInstance(final Class<T> clazz) {
        return Singletons.get(StarterServer.class, clazz);
    }
}
