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

package ecmwf.common.starter;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * The Class Starter.
 */
public final class Starter implements DynamicMBean {
    /** The _log. */
    private static final Logger _log = Logger.getLogger(Starter.class.getName());

    /** The properties. */
    private static final Properties properties = clean(System.getProperties());

    /** The name. */
    private final String name;

    /** The _m bean server. */
    private final MBeanServer mBeanServer;

    /** The server. */
    private final Starter server;

    /** The startDate. */
    private final Date startDate;

    /** The html. */
    private final HtmlAdaptorServer html;

    /** The classPath. */
    private final String classPath;

    /** The conf. */
    private final String conf;

    /** The ext. */
    private final String ext;

    /** The dir. */
    private final String dir;

    /** The shutdown. */
    private Thread shutdown = null;

    /** The exit. */
    private boolean exit = false;

    /** The classLoader. */
    private ClassLoader classLoader = null;

    /** The service. */
    private ToBeStarted service = null;

    /**
     * Instantiates a new starter.
     *
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws NotCompliantMBeanException
     *             the not compliant M bean exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    private Starter() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        // Setting up log for the Starter!
        final var consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        _log.addHandler(consoleHandler);
        // Use either the default MBeanServer accessible from JConsole or the HTTP one!
        if (get("useJConsole", false)) {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
            html = null;
        } else {
            mBeanServer = MBeanServerFactory.createMBeanServer(get("domain", "ECaccessGateway"));
            // Setting up the HTML adaptor for JMX!
            html = new HtmlAdaptorServer();
            html.setListenAddress(get("listenAddress", "127.0.0.1"));
            html.setPort(Integer.parseInt(get("port", "9082")));
            html.addUserAuthenticationInfo(new AuthInfo(get("user", "admin"), get("password", "admin")));
            mBeanServer.registerMBean(html, new ObjectName("Adaptor:name=html"));
        }
        mBeanServer.registerMBean(this, new ObjectName("ECaccess:service=ECStarter"));
        // This is required for OJB in order to find OJB.properties and other OJB
        // configuration files.
        System.setProperty("user.dir",
                new File(System.getProperty("ecmwf.properties")).getParentFile().getCanonicalPath());
        dir = new File(properties.getProperty("ecmwf.dir", "../..")).getCanonicalPath();
        name = get("name", "ecmwf.client.gateway.GatewayServer");
        conf = get("conf", dir + "/gateway/conf");
        ext = get("ext", dir + "/gateway/lib/ext");
        classPath = get("classpath", conf + File.pathSeparator + ext);
        startDate = new Date();
        server = this;
        try {
            if (get("addShutdownHook", true)) {
                debug(Starter.class, "Adding shutdown hook");
                shutdown = new Thread() {
                    @Override
                    public void run() {
                        debug(Starter.class, "Shutdown requested");
                        server.stop();
                    }
                };
                // Make sure the server is properly stopped!
                Runtime.getRuntime().addShutdownHook(shutdown);
            }
        } catch (final Throwable t) {
            _log.log(Level.WARNING, "addShutdownHook", t);
        }
        if (html != null) {
            html.start();
        }
        if (get("start", true)) {
            start();
        }
    }

    /**
     * Debug.
     *
     * @param clazz
     *            the clazz
     * @param message
     *            the message
     */
    public static void debug(final Class<?> clazz, final String message) {
        if (Starter.get("debug", false)) {
            _log.log(Level.INFO, () -> clazz.getName() + ": " + message);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        // Check attribute_name is not null to avoid NullPointerException later
        // on
        if (attributeName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                    "Cannot invoke a getter of " + this.getClass().getName() + " with null attribute name");
        }
        try {
            if ("Name".equals(attributeName)) {
                return name;
            }
            if ("ActiveThreadsCount".equals(attributeName)) {
                return Thread.activeCount();
            }
            if ("ClassPath".equals(attributeName)) {
                return classPath.replace(File.pathSeparatorChar, ' ').trim();
            }
            if ("Started".equals(attributeName)) {
                return service != null;
            }
            if ("FreeMemory".equals(attributeName)) {
                return Runtime.getRuntime().freeMemory();
            }
            if ("StartDate".equals(attributeName)) {
                return startDate;
            }
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attributes.
     */
    @Override
    public AttributeList getAttributes(final String[] attributeNames) {
        // Check attributeNames is not null to avoid NullPointerException later
        // on
        if (attributeNames == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),
                    "Cannot invoke a getter of " + this.getClass().getName());
        }
        final var resultList = new AttributeList();
        // if attributeNames is empty, return an empty result list
        if (attributeNames.length == 0) {
            return resultList;
        }
        // build the result attribute list
        for (final String attributeName : attributeNames) {
            try {
                final var value = getAttribute(attributeName);
                resultList.add(new Attribute(attributeName, value));
            } catch (final Exception e) {
                _log.log(Level.WARNING, "getAttribute", e);
            }
        }
        return resultList;
    }

    /**
     * Gets the class loader.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(),
                "The ECStarter service can be used to start/stop the "
                        + "software and monitor the Java Virtual Machine (memory).",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Name", "java.lang.String", "Name: name of the class to instanciate.",
                                true, false, false),
                        new MBeanAttributeInfo("StartDate", "java.util.Date",
                                "StartDate: when the starter has been started.", true, false, false),
                        new MBeanAttributeInfo("ActiveThreadsCount", "java.lang.Integer",
                                "ActiveThreadsCount: number of active threads.", true, false, false),
                        new MBeanAttributeInfo("Started", "java.lang.Boolean",
                                "Started: application loaded and started.", true, false, false),
                        new MBeanAttributeInfo("ClassPath", "java.lang.String",
                                "ClassPath: names of the Java Archive File (JAR).", true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("stop", "stop(): active the stop method of " + name,
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("start", "start(): active the start method of " + name,
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("shutdown",
                                "shutdown(): active the stop method of " + name + " and exit",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("restart", "restart(): active the restart method of " + name,
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("garbageCollector", "garbageCollector(): active the garbage collector",
                                new MBeanParameterInfo[0], "java.lang.Long", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
    }

    /**
     * Gets the MBean server.
     *
     * @return the MBean server
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws MBeanException, ReflectionException {
        // Check operationName is not null to avoid NullPointerException later
        // on
        if (operationName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
                    "Cannot invoke a null operation in " + this.getClass().getName());
        }
        try {
            if ("start".equals(operationName)) {
                if (service != null) {
                    throw new StarterException("Application is already starting/running");
                }
                start();
                return Boolean.TRUE;
            }
            if ("restart".equals(operationName)) {
                if (service == null) {
                    throw new StarterException("Application is not running");
                }
                restart();
                return Boolean.TRUE;
            }
            if ("stop".equals(operationName)) {
                if (service == null) {
                    throw new StarterException("Application is not running");
                }
                stop();
                return Boolean.TRUE;
            }
            if ("shutdown".equals(operationName)) {
                shutdown();
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
        // Check for a recognized operation name and call the corresponding
        // operation unrecognized operation name:
        throw new ReflectionException(new NoSuchMethodException(operationName),
                "Cannot find the operation " + operationName + " in " + this.getClass().getName());
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        debug(Starter.class, "Current JVM " + System.getProperty("java.vm.version"));
        final var file = get("properties", null);
        if (file != null) {
            try (final var fileIn = new FileInputStream(file)) {
                final var providedProperties = new Properties();
                providedProperties.load(fileIn);
                properties.putAll(clean(providedProperties));
                System.getProperties().putAll(providedProperties);
            } catch (final IOException e) {
                _log.log(Level.SEVERE, "Starter properties not available: {}", file);
                System.exit(-1);
            }
        }
        try {
            // Starter server
            new Starter();
        } catch (final Throwable t) {
            _log.log(Level.SEVERE, "Running application", t);
            System.exit(-1);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public void setAttribute(final Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        // Check attribute is not null to avoid NullPointerException later on
        if (attribute == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"),
                    "Cannot invoke a setter of " + this.getClass().getName() + " with null attribute");
        }
        final var attributeName = attribute.getName();
        if (attributeName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                    "Cannot invoke the setter of " + this.getClass().getName() + " with null attribute name");
        }
        // Check for a recognized attribute name and call the corresponding
        // setter
        throw new AttributeNotFoundException(
                "Attribute " + attributeName + " not found in " + this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attributes.
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        // Check attributes is not null to avoid NullPointerException later on
        if (attributes == null) {
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("AttributeList attributes cannot be null"),
                    "Cannot invoke a setter of " + this.getClass().getName());
        }
        final var resultList = new AttributeList();
        // if attributeNames is empty, nothing more to do
        if (attributes.isEmpty()) {
            return resultList;
        }
        // for each attribute, try to set it and add to the result list if
        // successful
        for (final var i = attributes.iterator(); i.hasNext();) {
            final var attr = (Attribute) i.next();
            try {
                setAttribute(attr);
                final var key = attr.getName();
                resultList.add(new Attribute(key, getAttribute(key)));
            } catch (final Exception e) {
                _log.log(Level.WARNING, "setAttribute", e);
            }
        }
        return resultList;
    }

    /**
     * Exit.
     */
    public synchronized void exit() {
        if (!exit) {
            if (shutdown != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdown);
                } catch (final Throwable t) {
                    _log.log(Level.WARNING, "removeShutdownHook", t);
                }
            }
            exit = true;
            shutdown = null;
            stop();
            System.exit(0);
        }
    }

    /**
     * Start.
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private synchronized void start() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, IOException {
        if (service == null) {
            final var thread = Thread.currentThread();
            final var initial = thread.getContextClassLoader();
            classLoader = StarterLoader.getClassLoader(classPath, initial);
            thread.setContextClassLoader(classLoader);
            try {
                service = (ToBeStarted) classLoader.loadClass(name).getConstructor(Starter.class).newInstance(this);
            } finally {
                thread.setContextClassLoader(initial);
            }
        }
    }

    /**
     * Stop.
     */
    private synchronized void stop() {
        if (service != null) {
            final var thread = Thread.currentThread();
            final var initial = thread.getContextClassLoader();
            thread.setContextClassLoader(classLoader);
            try {
                service.shutdown();
            } finally {
                thread.setContextClassLoader(initial);
            }
        }
    }

    /**
     * Restart.
     */
    public void restart() {
        new Thread() {
            @Override
            public void run() {
                setContextClassLoader(Starter.class.getClassLoader());
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    _log.log(Level.WARNING, "sleep", e);
                    Thread.currentThread().interrupt();
                }
                try {
                    server.stop();
                    server.start();
                } catch (final Exception e) {
                    _log.log(Level.WARNING, "restart", e);
                }
            }
        }.start();
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        new Thread() {
            @Override
            public void run() {
                setContextClassLoader(Starter.class.getClassLoader());
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    _log.log(Level.WARNING, "sleep", e);
                    Thread.currentThread().interrupt();
                }
                try {
                    exit();
                } catch (final Exception e) {
                    _log.log(Level.WARNING, "shutdown", e);
                }
            }
        }.start();
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    public static String get(final String name, final String defaultValue) {
        return properties.getProperty("ecmwf.common.starter." + name,
                properties.getProperty("starter." + name, defaultValue));
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the boolean
     */
    public static boolean get(final String name, final boolean defaultValue) {
        final var result = get(name, Boolean.toString(defaultValue));
        return "true".equalsIgnoreCase(result) || "yes".equalsIgnoreCase(result);
    }

    /**
     * Gets the.
     *
     * @param group
     *            the group
     *
     * @return the properties
     */
    public static Properties get(final String group) {
        final var output = new Properties();
        final var keys = properties.keys();
        while (keys.hasMoreElements()) {
            final var key = String.valueOf(keys.nextElement());
            String element;
            if (key.startsWith(element = "ecmwf.common.starter.property." + group + ".")
                    || key.startsWith(element = "starter.property." + group + ".")) {
                output.put(key.substring(element.length()), properties.get(key));
            }
        }
        return output;
    }

    /**
     * _clean.
     *
     * @param properties
     *            the properties
     *
     * @return the properties
     */
    private static Properties clean(final Properties properties) {
        final var keys = properties.keys();
        final var result = new Properties();
        while (keys.hasMoreElements()) {
            final var key = String.valueOf(keys.nextElement());
            if (key.startsWith("ecmwf.common.starter.") || key.startsWith("starter.")) {
                result.put(key, properties.remove(key));
            }
        }
        return result;
    }

    /**
     * The Class StarterException.
     */
    private static final class StarterException extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -1639807331276485018L;

        /**
         * Instantiates a new starter exception.
         *
         * @param message
         *            the message
         */
        StarterException(final String message) {
            super(message);
        }
    }
}
