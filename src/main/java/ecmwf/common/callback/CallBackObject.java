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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;

/**
 * The Class CallBackObject.
 */
public abstract class CallBackObject extends UnicastRemoteObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7742539913197134956L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(CallBackObject.class);

    /** The Constant PORT. */
    private static final transient int PORT = Cnf.at("CallBack", "port", 9000);

    /** The Constant socketConfig. */
    private static final transient SocketConfig socketConfig = new SocketConfig("CallBack");

    /** The Constant clientSocketFactory. */
    private static final transient RMIClientSocketFactory clientSocketFactory = getRMIClientSocketFactory(socketConfig);

    /** The Constant serverSocketFactory. */
    private static final transient RMIServerSocketFactory serverSocketFactory = getRMIServerSocketFactory(socketConfig);

    /**
     * Instantiates a new call back object.
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    protected CallBackObject() throws RemoteException {
        super(PORT, clientSocketFactory, serverSocketFactory);
        _log.debug("Export object: {}", this.getClass().getName());
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public static int getPort() {
        return PORT;
    }

    /**
     * Gets the rmi server socket factory.
     *
     * @param socketConfig
     *            the socket config
     *
     * @return the RMI server socket factory
     */
    private static RMIServerSocketFactory getRMIServerSocketFactory(final SocketConfig socketConfig) {
        final var name = Cnf.at("CallBack", "serverFactory");
        RMIServerSocketFactory factory = null;
        if (name != null) {
            try {
                factory = (RMIServerSocketFactory) Class.forName(name).getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                _log.warn("getRMIServerSocketFactory", e);
                factory = null;
            }
        }
        if (factory != null) {
            _log.debug("Using RMIServerSocketFactory: {}", factory.getClass().getName());
            if (factory instanceof final SocketConfig config) {
                if (_log.isDebugEnabled()) {
                    _log.debug("SocketConfig detected for RMIServerSocketFactory (set socket options: {})",
                            socketConfig.configToString());
                }
                applyConfig(config);
            } else {
                _log.debug("SocketConfig NOT detected for RMIServerSocketFactory (no socket options): {}", factory);
            }
        }
        return factory;
    }

    /**
     * Gets the rmi client socket factory.
     *
     * @param socketConfig
     *            the socket config
     *
     * @return the RMI client socket factory
     */
    private static RMIClientSocketFactory getRMIClientSocketFactory(final SocketConfig socketConfig) {
        final var name = Cnf.at("CallBack", "clientFactory");
        RMIClientSocketFactory factory = null;
        if (name != null) {
            try {
                factory = (RMIClientSocketFactory) Class.forName(name).getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                _log.warn("getRMIClientSocketFactory", e);
                factory = null;
            }
        }
        if (factory != null) {
            _log.debug("Using RMIClientSocketFactory: {}", factory.getClass().getName());
            if (factory instanceof final SocketConfig config) {
                if (_log.isDebugEnabled()) {
                    _log.debug("SocketConfig detected for RMIClientSocketFactory (set socket options: {})",
                            socketConfig.configToString());
                }
                applyConfig(config);
            } else {
                _log.debug("SocketConfig NOT detected for RMIClientSocketFactory (no socket options): {}", factory);
            }
        }
        return factory;
    }

    /**
     * Creates the or get registry.
     *
     * @return the registry
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public static Registry createOrGetRegistry() throws RemoteException {
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(PORT, clientSocketFactory, serverSocketFactory);
            _log.debug("Registry created on port: {}", PORT);
        } catch (final RemoteException e) {
            registry = LocateRegistry.getRegistry(PORT);
            _log.debug("Registry retrieved on port: {}", PORT);
        }
        return registry;
    }

    /**
     * Apply the underlying SocketConfig options to the provided configuration.
     *
     * @param config
     *            the config
     */
    private static void applyConfig(final SocketConfig config) {
        config.setName(socketConfig.getName());
        config.setTrace(socketConfig.getTrace());
        config.setListenAddress(socketConfig.getListenAddress());
        config.setPublicAddress(socketConfig.getPublicAddress());
        config.setConnectTimeOut(socketConfig.getConnectTimeOut());
        config.setSoTimeOut(socketConfig.getSOTimeOut());
        config.setReceiveBufferSize(socketConfig.getReceiveBufferSize());
        config.setSendBufferSize(socketConfig.getSendBufferSize());
        config.setKeepAlive(socketConfig.getKeepAlive());
        config.setTcpNoDelay(socketConfig.getTcpNoDelay());
        config.setInterruptible(socketConfig.getInterruptible());
        config.setPrivilegedLocalPort(socketConfig.getPrivilegedLocalPort());
        config.setBackLog(socketConfig.getBackLog());
        config.setReuseAddress(socketConfig.getReuseAddress());
    }
}
