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

package ecmwf.common.plugin;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class ServerPlugin.
 */
public abstract class ServerPlugin extends PluginThread {
    /** The Constant _descriptors. */
    private static final Map<String, ServerPluginDescriptor> _descriptors = new ConcurrentHashMap<>();

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ServerPlugin.class);

    /** The Constant _available. */
    private static boolean _available = false;

    /** The _inet address. */
    private InetAddress _inetAddress = null;

    /** The _remote host. */
    private String _remoteHost = null;

    /** The _remote ip. */
    private String _remoteIP = null;

    /** The _run. */
    private boolean _run = true;

    /** The _close. */
    private boolean _close = true;

    /** The _server socket. */
    private ServerSocket _serverSocket = null;

    /** The _socket. */
    private Socket _socket = null;

    /** The _so timeout. */
    private int _soTimeout = -1;

    /** The _max connections. */
    private int _maxConnections = -1;

    /** The _inverseResolution. */
    private boolean _inverseResolution = true;

    /**
     * Instantiates a new server plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ServerPlugin(final String ref, final Map<String, String> params, final Socket socket) throws IOException {
        super(ref, params);
        _socket = socket;
        _setInverseResolution(params);
    }

    /**
     * Instantiates a new server plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public ServerPlugin(final String ref, final Map<String, String> params) {
        super(ref, params);
        final var maxConnections = params.get("maxConnections");
        if (maxConnections != null) {
            try {
                _maxConnections = Integer.parseInt(maxConnections);
            } catch (final Exception e) {
                _maxConnections = -1;
            }
        }
        _log.debug("MaxConnections set to: " + _maxConnections);
        _setInverseResolution(params);
    }

    /**
     * Sets the inverse resolution.
     *
     * @param params
     *            the params
     */
    private void _setInverseResolution(final Map<String, String> params) {
        final var inverseResolution = params.get("inverseResolution");
        if (inverseResolution != null) {
            final var lowerCase = inverseResolution.toLowerCase();
            _inverseResolution = "true".equals(lowerCase) || "yes".equals(lowerCase);
        }
        _log.debug("InverseResolution set to: " + _inverseResolution);
    }

    /**
     * Gets the socket.
     *
     * @return the socket
     */
    public Socket getSocket() {
        return _socket;
    }

    /**
     * Gets the info.
     *
     * @return the info
     */
    public String getInfo() {
        return null;
    }

    /**
     * Customize socket.
     *
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void customizeSocket(final Socket socket) throws IOException {
    }

    /**
     * Get instance of current class.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @return the configurable runnable
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract ConfigurableRunnable newInstance(final String ref, final Map<String, String> params,
            final Socket socket) throws IOException;

    /**
     * {@inheritDoc}
     *
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        if (_serverSocket != null) {
            setPriority(Thread.MAX_PRIORITY);
            while (_run) {
                try {
                    newInstance(getRef(), _params, _socket = _serverSocket.accept()).execute();
                } catch (final SocketTimeoutException e) {
                    continue;
                } catch (InterruptedIOException | SocketException e) {
                    _log.warn("Accepting a connection", e);
                    continue;
                } catch (final Exception e) {
                    _log.warn("Accepting a connection", e);
                    try {
                        _socket.close();
                    } catch (final Throwable ignored) {
                        _log.debug(ignored);
                    }
                }
            }
        } else {
            if (_socket != null) {
                try {
                    _getSocketInfo(_socket);
                    customizeSocket(_socket);
                    setThreadNameAndCookie(getRef(), null, null, _remoteHost);
                    _log.debug("Connection(" + _socket.getPort() + ") " + _remoteIP + " -> " + _remoteHost);
                    addConnection(_socket, this);
                    final var descriptor = getServerPluginDescriptor();
                    final var maxConnections = descriptor.getMaxConnections();
                    final var connectionsCount = descriptor.getConnectionsActiveCount();
                    if (maxConnections >= 0 && connectionsCount <= maxConnections || maxConnections < 0) {
                        startConnection(_socket);
                    } else {
                        _log.debug("The maximum number of " + getRef() + " connections have been reached ("
                                + connectionsCount + ")");
                        refuseConnection(_socket, connectionsCount);
                    }
                } catch (final Exception e) {
                    _log.warn("Processing a connection", e);
                }
            }
            releaseConnection(_socket, _close);
        }
    }

    /**
     * Gets the socket info.
     *
     * @param socket
     *            the socket
     */
    private void _getSocketInfo(final Socket socket) {
        if (_soTimeout != -1) {
            try {
                socket.setSoTimeout(_soTimeout);
            } catch (final IOException e) {
                _log.debug("Setting SoTimeout", e);
            }
        }
        _inetAddress = socket.getInetAddress();
        _remoteIP = _inetAddress.getHostAddress();
        if (_inverseResolution) {
            final var currentTime = System.currentTimeMillis();
            _remoteHost = _inetAddress.getHostName();
            _log.debug("Inverse Resolution: " + (System.currentTimeMillis() - currentTime) + "ms");
            if (_remoteHost == null || _remoteHost.length() == 0) {
                _log.warn("Cannot resolve hostname");
                _remoteHost = _remoteIP;
            }
        } else {
            _remoteHost = _remoteIP;
        }
    }

    /**
     * Adds the connection.
     *
     * @param socket
     *            the socket
     * @param info
     *            the info
     */
    public void addConnection(final Socket socket, final ServerPlugin info) {
        getServerPluginDescriptor().addConnection(socket, info);
    }

    /**
     * Sets the available.
     *
     * @param available
     *            is it available?
     */
    private static void _setAvailable(final boolean available) {
        _available = available;
    }

    /**
     * {@inheritDoc}
     *
     * Caller back.
     */
    @Override
    public void callerBack(final boolean reset) {
        super.callerBack(reset);
        _setAvailable(true);
    }

    /**
     * {@inheritDoc}
     *
     * Caller gone.
     */
    @Override
    public void callerGone() {
        super.callerGone();
        _setAvailable(false);
    }

    /**
     * Release connection.
     *
     * @param socket
     *            the socket
     * @param close
     *            the close
     */
    public void releaseConnection(final Socket socket, final boolean close) {
        getServerPluginDescriptor().releaseConnection(socket, close);
    }

    /**
     * Gets the header.
     *
     * @return the header
     */
    public String getHeader() {
        return getPluginName() + " v" + getVersion()
                + (_socket != null ? " (" + _socket.getLocalAddress().getCanonicalHostName() + ")" : "");
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public abstract int getPort();

    /**
     * Gets the inet address.
     *
     * @return the inet address
     */
    public InetAddress getInetAddress() {
        return _inetAddress;
    }

    /**
     * Gets the remote host.
     *
     * @return the remote host
     */
    public String getRemoteHost() {
        return _remoteHost;
    }

    /**
     * Gets the remote ip.
     *
     * @return the remote ip
     */
    public String getRemoteIP() {
        return _remoteIP;
    }

    /**
     * Checks if is available.
     *
     * @return true, if is available
     */
    public boolean isAvailable() {
        return _available;
    }

    /**
     * Sets the close on exit.
     *
     * @param close
     *            the new close on exit
     */
    public void setCloseOnExit(final boolean close) {
        _close = close;
    }

    /**
     * {@inheritDoc}
     *
     * Start.
     */
    @Override
    public boolean start() {
        final var port = getPort();
        if (port < 0) {
            _log.warn(port + " is not a valid port");
        } else {
            final var socketConfig = new SocketConfig(getPluginName());
            _soTimeout = socketConfig.getSOTimeOut();
            socketConfig.setSoTimeOut(60000);
            _log.info("Starting " + getPluginName() + "/" + getRef() + " on " + port);
            try {
                _serverSocket = socketConfig.getServerSocket(port);
                final var descriptor = new ServerPluginDescriptor();
                descriptor.setMaxConnections(_maxConnections);
                _descriptors.put(getRef(), descriptor);
                _available = true;
                _run = true;
                setThreadNameAndCookie(getRef(), null, null, null);
                execute();
                return true;
            } catch (final Throwable t) {
                _log.error("Starting plugin " + getRef(), t);
            }
        }
        return false;
    }

    /**
     * Start connection.
     *
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void startConnection(Socket socket) throws IOException;

    /**
     * Refuse connection.
     *
     * @param socket
     *            the socket
     * @param connectionsCount
     *            the connections count
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void refuseConnection(Socket socket, int connectionsCount) throws IOException;

    /**
     * {@inheritDoc}
     *
     * Stop.
     */
    @Override
    public void stop() {
        if (_serverSocket != null) {
            getServerPluginDescriptor().close();
            try {
                _run = false;
                _serverSocket.close();
            } catch (final IOException e) {
                _log.debug(e);
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
            if ("Available".equals(attributeName)) {
                return _available;
            }
            if ("Header".equals(attributeName)) {
                return getHeader();
            }
            if ("Port".equals(attributeName)) {
                return getPort();
            }
            if ("LastConnectedUser".equals(attributeName)) {
                return _socket == null ? "none" : _socket.getInetAddress().getHostAddress();
            }
            final var descriptor = getServerPluginDescriptor();
            if ("ConnectionsCount".equals(attributeName)) {
                return descriptor != null ? descriptor.getConnectionsCount() : 0;
            }
            if ("ConnectionsDurationAve".equals(attributeName)) {
                return Format.formatDuration(descriptor != null ? descriptor.getConnectionDurationAve() : 0);
            }
            if ("ConnectionsDurationMax".equals(attributeName)) {
                return Format.formatDuration(descriptor != null ? descriptor.getDurationMax() : 0);
            }
            if ("ConnectionsActiveCount".equals(attributeName)) {
                return descriptor != null ? descriptor.getConnectionsActiveCount() : 0;
            }
            if ("ConnectionsActiveMax".equals(attributeName)) {
                return descriptor != null ? descriptor.getConnectionsActiveMax() : 0;
            }
            if ("ConnectionsList".equals(attributeName)) {
                return descriptor != null ? descriptor.getConnectionsList() : "[none]";
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
        return MBeanManager.addMBeanInfo(
                super.getMBeanInfo(), super.getMBeanInfo().getDescription(),
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Available", "java.lang.Boolean", "Available: server availability.",
                                true, false, false),
                        new MBeanAttributeInfo("Header", "java.lang.String", "Header: server header.", true, false,
                                false),
                        new MBeanAttributeInfo("Port", "java.lang.Integer", "Port: server port number.", true, false,
                                false),
                        new MBeanAttributeInfo("ConnectionsCount", "java.lang.Integer",
                                "ConnectionsCount: number of connections accepted by the server since statsReset() called.",
                                true, false, false),
                        new MBeanAttributeInfo("ConnectionsActiveMax", "java.lang.Integer",
                                "ConnectionsActiveMax: maximum number of parallel connections since statsReset() called.",
                                true, false, false),
                        new MBeanAttributeInfo("ConnectionsActiveCount", "java.lang.Integer",
                                "ConnectionsActiveCount: number of connections currently active.", true, false, false),
                        new MBeanAttributeInfo("ConnectionsDurationAve", "java.lang.String",
                                "ConnectionsDurationAve: sliding average duration in milliseconds of open connections since statsReset() called.",
                                true, false, false),
                        new MBeanAttributeInfo("ConnectionsDurationMax", "java.lang.String",
                                "ConnectionsDurationMax: maximum duration in milliseconds of an open connection since statsReset() called.",
                                true, false, false),
                        new MBeanAttributeInfo("ConnectionsList", "java.lang.String",
                                "ConnectionsList: current connection(s) in the queue.", true, false, false),
                        new MBeanAttributeInfo("LastConnectedUser", "java.lang.String",
                                "LastConnectedUser: the IP address of the last connected user.", true, false, false) },
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("closeConnection", "closeConnection(ipAddress,port): close the socket",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("ipAddress", "java.lang.String",
                                                "the remote ip address of the socket"),
                                        new MBeanParameterInfo("port", "java.lang.Integer", "the remote port") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("statsReset", "statsReset(): reset statistics.", null, "void",
                                MBeanOperationInfo.ACTION) });
    }

    /**
     * Gets the server plugin descriptor.
     *
     * @return the server plugin descriptor
     */
    public ServerPluginDescriptor getServerPluginDescriptor() {
        return _descriptors.get(getRef());
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
                getServerPluginDescriptor().statsReset();
                return Boolean.TRUE;
            }
            final var descriptor = getServerPluginDescriptor();
            if ("closeConnection".equals(operationName) && signature.length == 2
                    && "java.lang.String".equals(signature[0]) && "java.lang.Integer".equals(signature[1])) {
                if (descriptor != null) {
                    descriptor.closeConnection((String) params[0], (Integer) params[1]);
                }
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the " + operationName + " MBean method", e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * The Class ServerPluginDescriptor.
     */
    private static final class ServerPluginDescriptor {
        /** The _connections. */
        private final Map<Socket, SessionDescriptor> _connectionsActive = new ConcurrentHashMap<>();

        /** The _connections count. */
        private int _connectionsCount = 0;

        /** The _total duration. */
        private long _totalDuration = 0;

        /** The _duration max. */
        private long _durationMax = 0;

        /** The _max connections. */
        private int _maxConnections = -1;

        /** The _max connections. */
        private int _connectionsActiveMax = 0;

        /**
         * Gets the connections count.
         *
         * @return the connections count
         */
        int getConnectionsCount() {
            return _connectionsCount + getConnectionsActiveCount();
        }

        /**
         * Gets the connections count max.
         *
         * @return the connections count max
         */
        int getConnectionsActiveMax() {
            return _connectionsActiveMax;
        }

        /**
         * Gets the current connections.
         *
         * @return the current connections
         */
        String getConnectionsList() {
            final var result = new StringBuilder();
            for (final Socket socket : _connectionsActive.keySet().toArray(new Socket[0])) {
                final var session = _connectionsActive.get(socket);
                if (session != null) {
                    final var info = session.getInfo();
                    result.append(result.length() > 0 ? " " : "")
                            .append(("Socket[addr=" + socket.getInetAddress().getHostAddress() + ",port="
                                    + socket.getPort() + "]="
                                    + Format.formatDuration(System.currentTimeMillis() - session.getTime())
                                    + (info != null ? " (" + info + ")" : "")).replace(' ', '_'));
                }
            }
            return result.toString();
        }

        /**
         * Close connection.
         *
         * @param ipAddress
         *            the ip address
         * @param port
         *            the port
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void closeConnection(final String ipAddress, final int port) throws IOException {
            for (final Socket socket : _connectionsActive.keySet().toArray(new Socket[0])) {
                if (socket.getInetAddress().getHostAddress().equals(ipAddress) && socket.getPort() == port) {
                    socket.close();
                }
            }
        }

        /**
         * Gets the connection duration ave.
         *
         * @return the connection duration ave
         */
        long getConnectionDurationAve() {
            final long count = _connectionsCount;
            return count == 0 ? 0 : _totalDuration / count;
        }

        /**
         * Gets the duration max.
         *
         * @return the duration max
         */
        long getDurationMax() {
            return _durationMax;
        }

        /**
         * Adds the connection.
         *
         * @param socket
         *            the socket
         * @param plugin
         *            the plugin
         */
        void addConnection(final Socket socket, final ServerPlugin plugin) {
            _connectionsActive.put(socket, new SessionDescriptor(plugin));
            final var size = _connectionsActive.size();
            _connectionsActiveMax = size > _connectionsActiveMax ? size : _connectionsActiveMax;
        }

        /**
         * Release connection.
         *
         * @param socket
         *            the socket
         * @param close
         *            the close
         */
        void releaseConnection(final Socket socket, final boolean close) {
            _connectionsCount++;
            if (close) {
                try {
                    socket.close();
                } catch (final Exception e) {
                    _log.warn(e);
                }
            } else {
                _log.debug("Close handled by plugin");
            }
            final SessionDescriptor session;
            if ((session = _connectionsActive.remove(socket)) != null) {
                final var total = System.currentTimeMillis() - session.getTime();
                _durationMax = total > _durationMax ? total : _durationMax;
                _totalDuration += total;
            }
        }

        /**
         * Gets the active connection count.
         *
         * @return the active connection count
         */
        public int getConnectionsActiveCount() {
            return _connectionsActive.size();
        }

        /**
         * Gets the max connections.
         *
         * @return the max connections
         */
        public int getMaxConnections() {
            return _maxConnections;
        }

        /**
         * Sets the max connections.
         *
         * @param maxConnections
         *            the new max connections
         */
        public void setMaxConnections(final int maxConnections) {
            _maxConnections = maxConnections;
        }

        /**
         * Stats reset.
         */
        public void statsReset() {
            _connectionsCount = 0;
            _totalDuration = 0;
            _durationMax = 0;
        }

        /**
         * Close.
         */
        void close() {
            for (final Socket socket : _connectionsActive.keySet().toArray(new Socket[0])) {
                releaseConnection(socket, true);
            }
        }
    }

    /**
     * The Class SessionDescriptor.
     */
    private static final class SessionDescriptor {
        /** The _plugin. */
        private final ServerPlugin _plugin;

        /** The _time. */
        private final long _time;

        /**
         * Instantiates a new session descriptor.
         *
         * @param plugin
         *            the plugin
         */
        SessionDescriptor(final ServerPlugin plugin) {
            _time = System.currentTimeMillis();
            _plugin = plugin;
        }

        /**
         * Gets the info.
         *
         * @return the info
         */
        String getInfo() {
            return _plugin.getInfo();
        }

        /**
         * Gets the time.
         *
         * @return the time
         */
        long getTime() {
            return _time;
        }
    }
}
