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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.database.DataBase;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.security.Tools;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class ECaccessProvider.
 */
public abstract class ECaccessProvider extends ECaccessServer implements ProviderInterface, MonitorCallback {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6739547236501546407L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(ECaccessProvider.class);

    /** The _registry. */
    private final transient Registry _registry;

    /** The _repository. */
    private final transient ProviderMBean _repository;

    /** The _name. */
    private final transient String _name;

    /**
     * Instantiates a new ecaccess provider.
     *
     * @param dataBase
     *            the data base
     * @param starter
     *            the starter
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.InstantiationException
     *             the instantiation exception
     * @throws java.lang.ClassNotFoundException
     *             the class not found exception
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECaccessProvider(final DataBase dataBase, final Starter starter)
            throws SQLException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException, DataBaseException {
        super(dataBase, starter);
        _name = Format.getClassName(this);
        Registry registry = null;
        try {
            registry = CallBackObject.createOrGetRegistry();
            _log.debug("Registry ready on port: " + CallBackObject.getPort());
            registry.rebind(_name, this);
            _log.debug("Service " + _name + " bound");
        } catch (final Throwable t) {
            _log.fatal("Service " + _name + " NOT bound", t);
            System.exit(0);
        }
        _registry = registry;
        _log.debug("Starting ProviderMBean");
        _repository = new ProviderMBean("ProviderMBean");
        _repository.start();
    }

    /**
     * Gets the client interfaces.
     *
     * @return the client interfaces
     */
    public List<String> getClientRoots() {
        final List<String> roots = new ArrayList<>();
        for (final ClientElement element : _repository.getList()) {
            roots.add(_repository.getKey(element));
        }
        return roots;
    }

    /**
     * Gets the client interfaces.
     *
     * @return the client interfaces
     */
    public List<ClientInterface> getClientInterfaces() {
        final List<ClientInterface> interfaces = new ArrayList<>();
        for (final ClientElement element : _repository.getList()) {
            interfaces.add(element.getClientInterface());
        }
        return interfaces;
    }

    /**
     * Gets the client interface.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     *
     * @return the client interface
     */
    public <T extends ClientInterface> T getClientInterface(final String name, final Class<T> clazz) {
        final ClientElement element;
        if ((element = _repository.getValue(name)) != null) {
            return clazz.cast(element.getClientInterface());
        }
        return null;
    }

    /**
     * Gets the client interface.
     *
     * @param <T>
     *            the generic type
     * @param root
     *            the root
     * @param service
     *            the service
     * @param clazz
     *            the clazz
     *
     * @return the client interface
     */
    public <T extends ClientInterface> T getClientInterface(final String root, final String service,
            final Class<T> clazz) {
        return getClientInterface(service + "/" + root, clazz);
    }

    /**
     * Check if a client interface is registered.
     *
     * @param root
     *            the root
     * @param service
     *            the service
     *
     * @return the client interface exists
     */
    public boolean existsClientInterface(final String root, final String service) {
        return _repository.containsKey(service + "/" + root);
    }

    /**
     * Get the last update from the client interface.
     *
     * @param root
     *            the root
     * @param service
     *            the service
     *
     * @return the client interface exists
     */
    public long lastUpdateForClientInterface(final String root, final String service) {
        final var element = _repository.getValue(service + "/" + root);
        return element != null ? element.getLastUpdate() : -1;
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public synchronized void shutdown() {
        try {
            _registry.unbind(_name);
        } catch (final Throwable t) {
            _log.warn("Unbinding service " + _name, t);
        }
        super.shutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Subscribe.
     */
    @Override
    public long subscribe(final ClientInterface access) throws RemoteException, ConnectionException {
        String host = null;
        try {
            host = RemoteServer.getClientHost();
            final var root = access.getRoot();
            final var service = access.getService();
            final var name = service + "/" + root;
            _log.info(service + " subscribing from " + host + " (" + name + ")");
            String password;
            try {
                password = Cnf.at("Other", "dontCheckIp", false)
                        || host.equals(InetAddress.getByName(root).getHostAddress()) ? Tools.newPassword(root) : null;
            } catch (final Exception e) {
                password = null;
            }
            if (password != null && password.equals(access.getPassword())) {
                addRoot(access, host, root, service);
                if (MonitorManager.isActivated()) {
                    try {
                        MonitorThread.getInstance().subscribe(name, service, this);
                    } catch (final MonitorException e) {
                        _log.warn("Subscribing " + name, e);
                    }
                }
                return System.currentTimeMillis();
            }
            _log.error(host + ":" + root + " not authenticated (" + password + ")");
            throw new ConnectionException("Not authenticated");
        } catch (final RemoteException | ConnectionException e) {
            _log.debug("subscribe" + (host != null ? " (" + host + ")" : ""), e);
            throw e;
        } catch (final Throwable t) {
            _log.debug("subscribe" + (host != null ? " (" + host + ")" : ""), t);
            throw new ConnectionException(t.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the monitor manager.
     */
    @Override
    public MonitorManager getMonitorManager(final String name) throws MonitorException {
        final var element = _repository.getValue(name);
        // The name is the service/root and the element was provided during the
        // subscription!
        if (element != null) {
            return new MonitorManager(name, MonitorManager.GREEN,
                    "Last update: " + Format.formatTime(element.getLastUpdate()));
        }
        // Not found!
        return new MonitorManager(name, MonitorManager.RED, "OFF");
    }

    /**
     * Adds the root.
     *
     * @param access
     *            the access
     * @param host
     *            the host
     * @param root
     *            the root
     * @param service
     *            the service
     */
    public void addRoot(final ClientInterface access, final String host, final String root, final String service) {
        final var name = service + "/" + root;
        final var element = _repository.getValue(name);
        if (element != null) {
            if (element.getHost().equals(host)) {
                _log.warn(name + " already subscribed");
                element.setClientInterface(access);
                element.update();
                return;
            }
            _log.info("Host change detected for " + name);
            _repository.removeKey(name);
        }
        _log.info(name + " authenticated");
        _repository.put(new ClientElement(access, host, root, service));
    }

    /**
     * Removes the expired root.
     *
     * @param root
     *            the root
     * @param service
     *            the service
     */
    public void removeExpired(final String root, final String service) {
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is registred.
     */
    @Override
    public long isRegistred(final String root, final String service) {
        final var name = service + "/" + root;
        final var element = _repository.getValue(name);
        if (element != null) {
            element.update();
            return System.currentTimeMillis();
        }
        _log.warn(name + " not registered");
        return -1;
    }

    /**
     * The Class ClientElement.
     */
    private static final class ClientElement {
        /** The _client. */
        private ClientInterface _client = null;

        /** The _root. */
        private final String _root;

        /** The _root. */
        private final String _service;

        /** The _host. */
        private final String _host;

        /** The _update. */
        private long _update = System.currentTimeMillis();

        /**
         * Instantiates a new client element.
         *
         * @param client
         *            the client
         * @param host
         *            the host
         * @param root
         *            the root
         * @param service
         *            the service
         */
        // ----------------------------------------------------------------
        private ClientElement(final ClientInterface client, final String host, final String root,
                final String service) {
            _client = client;
            _host = host;
            _root = root;
            _service = service;
        }

        /**
         * Sets the client interface.
         *
         * @param client
         *            the new client interface
         */
        void setClientInterface(final ClientInterface client) {
            _client = client;
        }

        /**
         * Gets the client interface.
         *
         * @return the client interface
         */
        ClientInterface getClientInterface() {
            return _client;
        }

        /**
         * Gets the root.
         *
         * @return the root
         */
        String getRoot() {
            return _root;
        }

        /**
         * Gets the service.
         *
         * @return the service
         */
        String getService() {
            return _service;
        }

        /**
         * Gets the host.
         *
         * @return the host
         */
        String getHost() {
            return _host;
        }

        /**
         * Gets the last update.
         *
         * @return the last update
         */
        long getLastUpdate() {
            return _update;
        }

        /**
         * Update.
         */
        void update() {
            _update = System.currentTimeMillis();
        }
    }

    /**
     * The Class ProviderMBean.
     */
    public final class ProviderMBean extends StorageRepository<ClientElement> {
        /**
         * Instantiates a new provider m bean.
         *
         * @param name
         *            the name
         */
        ProviderMBean(final String name) {
            super(name, Cnf.at("StorageRepository", "providerSize", 0),
                    Cnf.at("StorageRepository", "providerDelay", 30 * Timer.ONE_SECOND));
        }

        /**
         * Gets the key.
         *
         * @param client
         *            the client
         *
         * @return the key
         */
        @Override
        public String getKey(final ClientElement client) {
            return client.getService() + "/" + client.getRoot();
        }

        /**
         * Gets the status.
         *
         * @param client
         *            the client
         *
         * @return the status
         */
        @Override
        public String getStatus(final ClientElement client) {
            return "[" + client.getHost() + "]["
                    + Format.formatDuration(System.currentTimeMillis() - client.getLastUpdate()) + "]["
                    + (client.getClientInterface() != null) + "]";
        }

        /**
         * Expired.
         *
         * @param client
         *            the client
         *
         * @return true, if successful
         */
        @Override
        public boolean expired(final ClientElement client) {
            return System.currentTimeMillis() - client.getLastUpdate() > Cnf.at("Server", "providerMBeanTimeout",
                    2 * Timer.ONE_MINUTE);
        }

        /**
         * Update.
         *
         * @param client
         *            the client
         */
        @Override
        public void update(final ClientElement client) {
            _log.debug("Last update for " + getKey(client) + " was at " + Format.formatTime(client.getLastUpdate()));
            removeExpired(client.getRoot(), client.getService());
        }
    }
}
