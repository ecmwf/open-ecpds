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

package ecmwf.common.rmi;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory for creating SSLClientSocket objects.
 */
public class SSLClientSocketFactory extends SSLSocketFactory {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SSLClientSocketFactory.class);

    /** The delegate. */
    private final SSLSocketFactory delegate;

    /** The statistics. */
    private final ClientSocketStatistics statistics;

    /** The config. */
    private final SocketConfig config;

    /** The wrappers. */
    private final List<SSLSocketWrapper> wrappers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Update statistics.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void updateStatistics() throws IOException {
        if (statistics != null) {
            synchronized (wrappers) {
                for (final SSLSocketWrapper wrapper : wrappers) {
                    wrapper.updateStatistics();
                }
            }
        }
    }

    /**
     * Instantiates a new SSL client socket factory.
     *
     * @param factory
     *            the factory
     * @param config
     *            the config
     */
    public SSLClientSocketFactory(final SSLSocketFactory factory, final SocketConfig config) {
        this.statistics = config.getStatistics();
        this.config = config;
        this.delegate = factory;
    }

    /**
     * Gets the default cipher suites.
     *
     * @return the default cipher suites
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    /**
     * Gets the supported cipher suites.
     *
     * @return the supported cipher suites
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    /**
     * Gets the configured wrapper.
     *
     * @param socket
     *            the socket
     *
     * @return the configured wrapper
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private SSLSocket getConfiguredWrapper(final Socket socket) throws IOException {
        if (socket instanceof final SSLSocket sslSocket) {
            final var selfSocket = getSelfSocket(sslSocket);
            _log.debug("Using SSLSocket: {} (class={}) -> {} (class={})", sslSocket, sslSocket.getClass().getName(),
                    selfSocket, selfSocket.getClass().getName());
            config.setTCPOptions(selfSocket);
            config.configureConnectedSocket(selfSocket);
            return statistics != null ? new SSLSocketWrapper(statistics, sslSocket, selfSocket) : sslSocket;
        }
        throw new IOException("SSLSocket expected");
    }

    /**
     * Creates a new SSLClientSocket object.
     *
     * @param socket
     *            the socket
     * @param host
     *            the host
     * @param port
     *            the port
     * @param autoClose
     *            the auto close
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException {
        return getConfiguredWrapper(delegate.createSocket(socket, host, port, autoClose));
    }

    /**
     * Creates a new SSLClientSocket object.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return getConfiguredWrapper(delegate.createSocket(host, port));
    }

    /**
     * Creates a new SSLClientSocket object.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param localHost
     *            the local host
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        return getConfiguredWrapper(delegate.createSocket(host, port, localHost, localPort));
    }

    /**
     * Creates a new SSLClientSocket object.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return getConfiguredWrapper(delegate.createSocket(host, port));
    }

    /**
     * Creates a new SSLClientSocket object.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param localHost
     *            the local host
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        return getConfiguredWrapper(delegate.createSocket(host, port, localHost, localPort));
    }

    /**
     * The Class SSLSocketWrapper.
     */
    private class SSLSocketWrapper extends SSLSocket {

        /** The wrapped socket. */
        private final SSLSocket wrappedSocket;

        /** The socket. */
        private final Socket socket;

        /** The statistics. */
        private final ClientSocketStatistics statistics;

        /** The start time. */
        private final long startTime;

        /**
         * Instantiates a new SSL socket wrapper.
         *
         * @param statistics
         *            the statistics
         * @param sslSocket
         *            the ssl socket
         * @param socket
         *            the socket
         */
        public SSLSocketWrapper(final ClientSocketStatistics statistics, final SSLSocket sslSocket,
                final Socket socket) {
            this.statistics = statistics;
            this.wrappedSocket = sslSocket;
            this.socket = socket;
            this.startTime = System.currentTimeMillis();
            wrappers.add(this);
        }

        /**
         * Update statistics.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void updateStatistics() throws IOException {
            statistics.add(socket, startTime);
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            _log.debug("Calling: close(): {}", wrappedSocket);
            wrappers.remove(this);
            try {
                updateStatistics();
            } finally {
                wrappedSocket.close();
            }
        }

        /**
         * Gets the channel.
         *
         * @return the channel
         */
        @Override
        public SocketChannel getChannel() {
            _log.debug("Calling: getChannel(): {}", wrappedSocket);
            return wrappedSocket.getChannel();
        }

        /**
         * Gets the inet address.
         *
         * @return the inet address
         */
        @Override
        public InetAddress getInetAddress() {
            return wrappedSocket.getInetAddress();
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getInputStream() throws IOException {
            return new FilterInputStream(wrappedSocket.getInputStream()) {
                @Override
                public void close() throws IOException {
                    _log.debug("Calling: getInputStream().close(): {}", wrappedSocket);
                    super.close();
                }
            };
        }

        /**
         * Gets the keep alive.
         *
         * @return the keep alive
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public boolean getKeepAlive() throws SocketException {
            return wrappedSocket.getKeepAlive();
        }

        /**
         * Gets the local address.
         *
         * @return the local address
         */
        @Override
        public InetAddress getLocalAddress() {
            return wrappedSocket.getLocalAddress();
        }

        /**
         * Gets the local port.
         *
         * @return the local port
         */
        @Override
        public int getLocalPort() {
            return wrappedSocket.getLocalPort();
        }

        /**
         * Gets the local socket address.
         *
         * @return the local socket address
         */
        @Override
        public SocketAddress getLocalSocketAddress() {
            return wrappedSocket.getLocalSocketAddress();
        }

        /**
         * Gets the OOB inline.
         *
         * @return the OOB inline
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public boolean getOOBInline() throws SocketException {
            return wrappedSocket.getOOBInline();
        }

        /**
         * Gets the output stream.
         *
         * @return the output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FilterOutputStream(wrappedSocket.getOutputStream()) {
                @Override
                public void write(final int b) throws IOException {
                    out.write(b);
                }

                @Override
                public void write(final byte[] b) throws IOException {
                    out.write(b);
                }

                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    _log.debug("Calling: getOutputStream().close(): {}", wrappedSocket);
                    super.close();
                }
            };
        }

        /**
         * Gets the port.
         *
         * @return the port
         */
        @Override
        public int getPort() {
            return wrappedSocket.getPort();
        }

        /**
         * Gets the receive buffer size.
         *
         * @return the receive buffer size
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            return wrappedSocket.getReceiveBufferSize();
        }

        /**
         * Gets the remote socket address.
         *
         * @return the remote socket address
         */
        @Override
        public SocketAddress getRemoteSocketAddress() {
            return wrappedSocket.getRemoteSocketAddress();
        }

        /**
         * Gets the reuse address.
         *
         * @return the reuse address
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public boolean getReuseAddress() throws SocketException {
            return wrappedSocket.getReuseAddress();
        }

        /**
         * Gets the send buffer size.
         *
         * @return the send buffer size
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            return wrappedSocket.getSendBufferSize();
        }

        /**
         * Gets the so linger.
         *
         * @return the so linger
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public int getSoLinger() throws SocketException {
            return wrappedSocket.getSoLinger();
        }

        /**
         * Gets the so timeout.
         *
         * @return the so timeout
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized int getSoTimeout() throws SocketException {
            return wrappedSocket.getSoTimeout();
        }

        /**
         * Gets the tcp no delay.
         *
         * @return the tcp no delay
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return wrappedSocket.getTcpNoDelay();
        }

        /**
         * Gets the traffic class.
         *
         * @return the traffic class
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public int getTrafficClass() throws SocketException {
            return wrappedSocket.getTrafficClass();
        }

        /**
         * Checks if is bound.
         *
         * @return true, if is bound
         */
        @Override
        public boolean isBound() {
            return wrappedSocket.isBound();
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        @Override
        public boolean isClosed() {
            return wrappedSocket.isClosed();
        }

        /**
         * Checks if is connected.
         *
         * @return true, if is connected
         */
        @Override
        public boolean isConnected() {
            return wrappedSocket.isConnected();
        }

        /**
         * Checks if is input shutdown.
         *
         * @return true, if is input shutdown
         */
        @Override
        public boolean isInputShutdown() {
            return wrappedSocket.isInputShutdown();
        }

        /**
         * Checks if is output shutdown.
         *
         * @return true, if is output shutdown
         */
        @Override
        public boolean isOutputShutdown() {
            return wrappedSocket.isOutputShutdown();
        }

        /**
         * Send urgent data.
         *
         * @param data
         *            the data
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void sendUrgentData(final int data) throws IOException {
            wrappedSocket.sendUrgentData(data);
        }

        /**
         * Sets the keep alive.
         *
         * @param on
         *            the new keep alive
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setKeepAlive(final boolean on) throws SocketException {
            wrappedSocket.setKeepAlive(on);
        }

        /**
         * Sets the OOB inline.
         *
         * @param on
         *            the new OOB inline
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setOOBInline(final boolean on) throws SocketException {
            wrappedSocket.setOOBInline(on);
        }

        /**
         * Sets the performance preferences.
         *
         * @param connectionTime
         *            the connection time
         * @param latency
         *            the latency
         * @param bandwidth
         *            the bandwidth
         */
        @Override
        public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
            wrappedSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        /**
         * Sets the receive buffer size.
         *
         * @param size
         *            the new receive buffer size
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized void setReceiveBufferSize(final int size) throws SocketException {
            wrappedSocket.setReceiveBufferSize(size);
        }

        /**
         * Sets the reuse address.
         *
         * @param on
         *            the new reuse address
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setReuseAddress(final boolean on) throws SocketException {
            wrappedSocket.setReuseAddress(on);
        }

        /**
         * Sets the send buffer size.
         *
         * @param size
         *            the new send buffer size
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized void setSendBufferSize(final int size) throws SocketException {
            wrappedSocket.setSendBufferSize(size);
        }

        /**
         * Sets the so linger.
         *
         * @param on
         *            the on
         * @param linger
         *            the linger
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setSoLinger(final boolean on, final int linger) throws SocketException {
            wrappedSocket.setSoLinger(on, linger);
        }

        /**
         * Sets the so timeout.
         *
         * @param timeout
         *            the new so timeout
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public synchronized void setSoTimeout(final int timeout) throws SocketException {
            wrappedSocket.setSoTimeout(timeout);
        }

        /**
         * Sets the tcp no delay.
         *
         * @param on
         *            the new tcp no delay
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setTcpNoDelay(final boolean on) throws SocketException {
            wrappedSocket.setTcpNoDelay(on);
        }

        /**
         * Sets the traffic class.
         *
         * @param tc
         *            the new traffic class
         *
         * @throws SocketException
         *             the socket exception
         */
        @Override
        public void setTrafficClass(final int tc) throws SocketException {
            wrappedSocket.setTrafficClass(tc);
        }

        /**
         * Shutdown input.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void shutdownInput() throws IOException {
            _log.debug("Calling: shutdownInput(): {}", wrappedSocket);
            wrappedSocket.shutdownInput();
        }

        /**
         * Shutdown output.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void shutdownOutput() throws IOException {
            _log.debug("Calling: shutdownOutput(): {}", wrappedSocket);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return wrappedSocket.toString();
        }

        /**
         * Adds the handshake completed listener.
         *
         * @param arg0
         *            the arg 0
         */
        @Override
        public void addHandshakeCompletedListener(final HandshakeCompletedListener arg0) {
            wrappedSocket.addHandshakeCompletedListener(arg0);
        }

        /**
         * Gets the enable session creation.
         *
         * @return the enable session creation
         */
        @Override
        public boolean getEnableSessionCreation() {
            return wrappedSocket.getEnableSessionCreation();
        }

        /**
         * Gets the enabled cipher suites.
         *
         * @return the enabled cipher suites
         */
        @Override
        public String[] getEnabledCipherSuites() {
            return wrappedSocket.getEnabledCipherSuites();
        }

        /**
         * Gets the enabled protocols.
         *
         * @return the enabled protocols
         */
        @Override
        public String[] getEnabledProtocols() {
            return wrappedSocket.getEnabledProtocols();
        }

        /**
         * Gets the need client auth.
         *
         * @return the need client auth
         */
        @Override
        public boolean getNeedClientAuth() {
            return wrappedSocket.getNeedClientAuth();
        }

        /**
         * Gets the session.
         *
         * @return the session
         */
        @Override
        public SSLSession getSession() {
            return wrappedSocket.getSession();
        }

        /**
         * Gets the supported cipher suites.
         *
         * @return the supported cipher suites
         */
        @Override
        public String[] getSupportedCipherSuites() {
            return wrappedSocket.getSupportedCipherSuites();
        }

        /**
         * Gets the supported protocols.
         *
         * @return the supported protocols
         */
        @Override
        public String[] getSupportedProtocols() {
            return wrappedSocket.getSupportedProtocols();
        }

        /**
         * Gets the use client mode.
         *
         * @return the use client mode
         */
        @Override
        public boolean getUseClientMode() {
            return wrappedSocket.getUseClientMode();
        }

        /**
         * Gets the want client auth.
         *
         * @return the want client auth
         */
        @Override
        public boolean getWantClientAuth() {
            return wrappedSocket.getWantClientAuth();
        }

        /**
         * Removes the handshake completed listener.
         *
         * @param arg0
         *            the arg 0
         */
        @Override
        public void removeHandshakeCompletedListener(final HandshakeCompletedListener arg0) {
            wrappedSocket.removeHandshakeCompletedListener(arg0);
        }

        /**
         * Sets the enable session creation.
         *
         * @param arg0
         *            the new enable session creation
         */
        @Override
        public void setEnableSessionCreation(final boolean arg0) {
            wrappedSocket.setEnableSessionCreation(arg0);
        }

        /**
         * Sets the enabled cipher suites.
         *
         * @param arg0
         *            the new enabled cipher suites
         */
        @Override
        public void setEnabledCipherSuites(final String[] arg0) {
            wrappedSocket.setEnabledCipherSuites(arg0);
        }

        /**
         * Sets the enabled protocols.
         *
         * @param arg0
         *            the new enabled protocols
         */
        @Override
        public void setEnabledProtocols(final String[] arg0) {
            wrappedSocket.setEnabledProtocols(arg0);
        }

        /**
         * Sets the need client auth.
         *
         * @param arg0
         *            the new need client auth
         */
        @Override
        public void setNeedClientAuth(final boolean arg0) {
            wrappedSocket.setNeedClientAuth(arg0);
        }

        /**
         * Sets the use client mode.
         *
         * @param arg0
         *            the new use client mode
         */
        @Override
        public void setUseClientMode(final boolean arg0) {
            wrappedSocket.setUseClientMode(arg0);
        }

        /**
         * Sets the want client auth.
         *
         * @param arg0
         *            the new want client auth
         */
        @Override
        public void setWantClientAuth(final boolean arg0) {
            wrappedSocket.setWantClientAuth(arg0);
        }

        /**
         * Start handshake.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void startHandshake() throws IOException {
            wrappedSocket.startHandshake();
        }
    }

    /**
     * Gets the self socket.
     *
     * @param sslSocket
     *            the ssl socket
     *
     * @return the self socket
     */
    private static Socket getSelfSocket(final SSLSocket sslSocket) {
        try {
            final var selfField = Class.forName("sun.security.ssl.BaseSSLSocketImpl").getDeclaredField("self");
            selfField.setAccessible(true);
            if (selfField.get(sslSocket) instanceof final Socket socket) {
                return socket;
            }
        } catch (final Exception e) {
            // Field cannot be extracted
        }
        return sslSocket;
    }
}
