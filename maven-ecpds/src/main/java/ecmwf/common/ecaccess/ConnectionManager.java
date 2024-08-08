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
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.callback.RemoteEngineThread;
import ecmwf.common.rmi.RMIClientSocketFactory;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;

/**
 * The Class ConnectionManager.
 */
public class ConnectionManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ConnectionManager.class);

    /** The Constant connectionManagersMap. */
    private static final Map<String, ConnectionManager> connectionManagersMap = new ConcurrentHashMap<>();

    /** The clientInterface. */
    private final ClientInterface clientInterface;

    /** The providerInterface. */
    private ProviderInterface providerInterface;

    /** The registry. */
    private Registry registry;

    /** The management thread. */
    private final DriverManagementThread managementThread;

    /** The className. */
    private final String className;

    /** The hostsList. */
    private final List<String> hostsList;

    /** The rmiPort. */
    private short rmiPort = -1;

    /** The offset. */
    private long offset = -1;

    /**
     * Instantiates a new connection manager.
     *
     * @param name
     *            the name
     * @param host
     *            the host
     * @param port
     *            the port
     * @param replace
     *            the replace
     * @param client
     *            the client
     *
     * @throws ConnectionException
     *             the connection exception
     */
    public ConnectionManager(final Class<?> name, final String host, final short port, final boolean replace,
            final ClientInterface client) throws ConnectionException {
        className = Format.getClassName(name);
        hostsList = Arrays.asList(host.split(",|;|\\|"));
        rmiPort = port;
        clientInterface = client;
        final var key = className + host + port;
        synchronized (connectionManagersMap) {
            if (!replace && connectionManagersMap.containsKey(key)) {
                throw new ConnectionException(className + " already registred");
            }
            _log.debug("Registering DriverManagementThread for {}@{}:{}", className, host, port);
            managementThread = new DriverManagementThread();
            managementThread.setPriority(Thread.MIN_PRIORITY);
            managementThread.execute();
            connectionManagersMap.put(key, this);
        }
    }

    /**
     * Instantiates a new connection manager.
     *
     * @param name
     *            the name
     * @param host
     *            the host
     * @param port
     *            the port
     * @param replace
     *            the replace
     *
     * @throws ConnectionException
     *             the connection exception
     */
    public ConnectionManager(final Class<?> name, final String host, final short port, final boolean replace)
            throws ConnectionException {
        this(name, host, port, replace, null);
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     *
     * @throws ConnectionException
     *             the connection exception
     */
    public ProviderInterface getConnection() throws ConnectionException {
        if (providerInterface != null) {
            return providerInterface;
        }
        if (registry == null) {
            throw new ConnectionException(className + " not initialized yet");
        }
        try {
            return providerInterface = (ProviderInterface) registry.lookup(className);
        } catch (final Exception e) {
            throw new ConnectionException(className + " unavailable");
        }
    }

    /**
     * Current time millis.
     *
     * @return the long
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis() - offset;
    }

    /**
     * Connect.
     *
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     * @throws ServerNotActiveException
     *             the server not active exception
     * @throws NotBoundException
     *             the not bound exception
     * @throws ConnectionException
     *             the connection exception
     */
    protected synchronized void connect(final String host)
            throws RemoteException, ServerNotActiveException, NotBoundException, ConnectionException {
        _log.info("Connecting to {} at {}:{}", className, host, rmiPort);
        registry = LocateRegistry.getRegistry(host, rmiPort, new RMIClientSocketFactory());
        if (_log.isDebugEnabled()) {
            for (final String name : registry.list()) {
                _log.debug("Bound in the registry: {}", name);
            }
        }
        providerInterface = (ProviderInterface) registry.lookup(className);
        if (clientInterface != null) {
            offset = System.currentTimeMillis() - providerInterface.subscribe(clientInterface);
        }
        _log.info("Connected{}", offset != -1 ? " (offset=" + offset + ")" : "");
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public boolean isConnected() {
        return managementThread != null && managementThread.connected();
    }

    /**
     * Disconnected.
     */
    public void disconnected() {
        RemoteEngineThread.remove(className);
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        if (managementThread != null) {
            managementThread.shutdown();
        }
    }

    /**
     * The Class DriverManagementThread.
     */
    private final class DriverManagementThread extends ConfigurableLoopRunnable {
        /** The run. */
        private boolean run = false;

        /** The count. */
        private int count = 0;

        /**
         * Connected.
         *
         * @return true, if successful
         */
        boolean connected() {
            return run && getLoop();
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            var index = 0;
            var loop = 0;
            while (getLoop() && !run) {
                if (index == hostsList.size()) {
                    index = 0;
                }
                final var host = hostsList.get(index++);
                try {
                    connect(host);
                    run = true;
                    break;
                } catch (final NoSuchObjectException t) {
                    _log.warn("{} connection failed to {}:{} (server not ready?)", className, host, rmiPort);
                } catch (final Throwable t) {
                    _log.warn("{} connection failed to {}:{}", className, host, rmiPort, t);
                }
                if (getLoop()) {
                    try {
                        Thread.sleep(5000);
                    } catch (final Exception ignored) {
                        // Ignored!
                    }
                }
            }
            if (run && getLoop() && count++ > 3) {
                for (var i = 0; i < 3; i++) {
                    final var start = System.currentTimeMillis();
                    try {
                        final var remoteTime = clientInterface == null ? getConnection().isAvailable()
                                : getConnection().isRegistred(clientInterface.getRoot(), clientInterface.getService());
                        if (remoteTime > 0) {
                            offset = System.currentTimeMillis() - remoteTime;
                        } else if (remoteTime == -1) {
                            _log.warn("Client not registered on Server? (will reconnect)");
                            run = false;
                        } else {
                            throw new IOException("Incorrect value for RemoteTime: " + remoteTime);
                        }
                        break;
                    } catch (final Throwable t) {
                        if (t instanceof NoSuchObjectException && "no such object in table".equals(t.getMessage())) {
                            _log.warn("Sending alive message to {} (attempt {}) - {} down/restarted?", className, i + 1,
                                    className);
                        } else {
                            _log.warn("Sending alive message to {} (attempt {})", className, i + 1, t);
                        }
                        if (i == 2) {
                            run = false;
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (final Exception ignored) {
                        }
                    } finally {
                        if (_log.isWarnEnabled()) {
                            final var duration = System.currentTimeMillis() - start;
                            if (duration > 30000) {
                                _log.warn("{} update took {}{}", className, Format.formatDuration(duration),
                                        !run ? " (didn't succeed)" : "");
                            }
                        }
                    }
                }
            }
            if (!run) {
                disconnected();
            }
            while (run && getLoop() && ++loop < 5) {
                try {
                    Thread.sleep(1000);
                } catch (final Exception ignored) {
                    // Ignored!
                }
            }
        }
    }
}
