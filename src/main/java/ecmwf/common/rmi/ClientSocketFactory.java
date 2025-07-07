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
 * ECMWF Product Data Store (OpenECPDS) Project
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

import javax.net.SocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory for creating ClientSocket objects.
 */
public class ClientSocketFactory extends SocketFactory {

    /** The delegate. */
    private final SocketFactory delegate;

    /** The statistics. */
    private final ClientSocketStatistics statistics;

    /** The config. */
    private final SocketConfig config;

    /** The wrappers. */
    private final List<SocketWrapper> wrappers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Update statistics.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void updateStatistics() throws IOException {
        if (statistics != null) {
            synchronized (wrappers) {
                for (final SocketWrapper wrapper : wrappers) {
                    wrapper.updateStatistics();
                }
            }
        }
    }

    /**
     * Instantiates a new client socket factory.
     *
     * @param config
     *            the config
     */
    public ClientSocketFactory(final SocketConfig config) {
        this(SocketConfig.DEFAULT_FACTORY, config);
    }

    /**
     * Instantiates a new client socket factory.
     *
     * @param factory
     *            the factory
     * @param config
     *            the config
     */
    public ClientSocketFactory(final SocketFactory factory, final SocketConfig config) {
        this.statistics = config.getStatistics();
        this.config = config;
        this.delegate = factory;
    }

    /**
     * Gets the wrapper.
     *
     * @param socket
     *            the socket
     *
     * @return the wrapper
     */
    protected Socket getWrapper(final Socket socket) {
        return statistics != null ? new SocketWrapper(statistics, socket) : socket;
    }

    /**
     * Gets the configured wrapper.
     *
     * @param socket
     *            the socket
     *
     * @return the configured wrapper
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getConfiguredWrapper(final Socket socket) throws IOException {
        config.setTCPOptions(socket);
        config.configureConnectedSocket(socket);
        return getWrapper(socket);
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new ClientSocket object.
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return config.withClientSocketFactory(this).getSocket(delegate, host, port);
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new ClientSocket object.
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        config.setListenAddress(localHost.getHostAddress());
        return config.withClientSocketFactory(this).getSocket(delegate, host, port, localPort);
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new ClientSocket object.
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return config.withClientSocketFactory(this).getSocket(delegate, host.getHostAddress(), port);
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new ClientSocket object.
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        config.setListenAddress(localHost.getHostAddress());
        return config.withClientSocketFactory(this).getSocket(delegate, host.getHostAddress(), port, localPort);
    }

    /**
     * The Class SocketWrapper.
     */
    public class SocketWrapper extends Socket {

        /** The Constant _log. */
        private static final Logger _log = LogManager.getLogger(SocketWrapper.class);

        /** The wrapped socket. */
        private final Socket wrappedSocket;

        /** The statistics. */
        private final ClientSocketStatistics statistics;

        /** The start time. */
        private final long startTime;

        /**
         * Instantiates a new socket wrapper.
         *
         * @param statistics
         *            the statistics
         * @param socket
         *            the socket
         */
        public SocketWrapper(final ClientSocketStatistics statistics, final Socket socket) {
            _log.debug("Using: SocketWrapper(): {} (class={})", socket, socket.getClass().getName());
            this.statistics = statistics;
            this.wrappedSocket = socket;
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
            statistics.add(wrappedSocket, startTime);
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
            wrappedSocket.shutdownOutput();
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
    }
}
