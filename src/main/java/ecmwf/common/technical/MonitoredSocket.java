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

package ecmwf.common.technical;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class MonitoredSocket.
 */
public class MonitoredSocket extends Socket {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitoredSocket.class);

    /** The decoratee. */
    protected final Socket decoratee;

    /**
     * Instantiates a new monitored socket.
     */
    public MonitoredSocket() {
        this(new Socket());
    }

    /**
     * Instantiates a new monitored socket.
     *
     * @param decoratee
     *            the decoratee
     */
    public MonitoredSocket(final Socket decoratee) {
        this.decoratee = decoratee;
    }

    /**
     * {@inheritDoc}
     *
     * Bind.
     */
    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        this.decoratee.bind(bindpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        try {
            throw new Exception("DEBUG");
        } catch (final Exception e) {
            _log.debug("close()", e);
        }
        this.decoratee.close();
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.decoratee.connect(endpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        this.decoratee.connect(endpoint, timeout);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof MonitoredSocket && this.decoratee.equals(((MonitoredSocket) object).decoratee);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the channel.
     */
    @Override
    public SocketChannel getChannel() {
        return this.decoratee.getChannel();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the inet address.
     */
    @Override
    public InetAddress getInetAddress() {
        return this.decoratee.getInetAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the input stream.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new MonitoredIS(this.decoratee.getInputStream());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the keep alive.
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.decoratee.getKeepAlive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local address.
     */
    @Override
    public InetAddress getLocalAddress() {
        return this.decoratee.getLocalAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local port.
     */
    @Override
    public int getLocalPort() {
        return this.decoratee.getLocalPort();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local socket address.
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.decoratee.getLocalSocketAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the OOB inline.
     */
    @Override
    public boolean getOOBInline() throws SocketException {
        return this.decoratee.getOOBInline();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the output stream.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new MonitoredOS(this.decoratee.getOutputStream());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort() {
        return this.decoratee.getPort();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the receive buffer size.
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return this.decoratee.getReceiveBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the remote socket address.
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.decoratee.getRemoteSocketAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the reuse address.
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.decoratee.getReuseAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the send buffer size.
     */
    @Override
    public int getSendBufferSize() throws SocketException {
        return this.decoratee.getSendBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so linger.
     */
    @Override
    public int getSoLinger() throws SocketException {
        return this.decoratee.getSoLinger();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so timeout.
     */
    @Override
    public int getSoTimeout() throws SocketException {
        return this.decoratee.getSoTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the tcp no delay.
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.decoratee.getTcpNoDelay();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic class.
     */
    @Override
    public int getTrafficClass() throws SocketException {
        return this.decoratee.getTrafficClass();
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return this.decoratee.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is bound.
     */
    @Override
    public boolean isBound() {
        return this.decoratee.isBound();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is closed.
     */
    @Override
    public boolean isClosed() {
        return this.decoratee.isClosed();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is connected.
     */
    @Override
    public boolean isConnected() {
        return this.decoratee.isConnected();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is input shutdown.
     */
    @Override
    public boolean isInputShutdown() {
        return this.decoratee.isInputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is output shutdown.
     */
    @Override
    public boolean isOutputShutdown() {
        return this.decoratee.isOutputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Send urgent data.
     */
    @Override
    public void sendUrgentData(final int data) throws IOException {
        this.decoratee.sendUrgentData(data);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the keep alive.
     */
    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        this.decoratee.setKeepAlive(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the OOB inline.
     */
    @Override
    public void setOOBInline(final boolean on) throws SocketException {
        this.decoratee.setOOBInline(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the performance preferences.
     */
    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        this.decoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the receive buffer size.
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        this.decoratee.setReceiveBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the reuse address.
     */
    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        this.decoratee.setReuseAddress(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the send buffer size.
     */
    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        this.decoratee.setSendBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so linger.
     */
    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        this.decoratee.setSoLinger(on, linger);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so timeout.
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        this.decoratee.setSoTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the tcp no delay.
     */
    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        this.decoratee.setTcpNoDelay(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the traffic class.
     */
    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        this.decoratee.setTrafficClass(tc);
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown input.
     */
    @Override
    public void shutdownInput() throws IOException {
        this.decoratee.shutdownInput();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown output.
     */
    @Override
    public void shutdownOutput() throws IOException {
        this.decoratee.shutdownOutput();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "MonitoredSocket-" + this.decoratee.toString();
    }

    /**
     * The Class MonitoredOS.
     */
    class MonitoredOS extends FilterOutputStream {
        /**
         * Instantiates a new monitored os.
         *
         * @param out
         *            the out
         */
        public MonitoredOS(final OutputStream out) {
            super(out);
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final int b) throws IOException {
            try {
                if (b < 0 || b > 255) {
                    _log.debug("write(b) not in 0-255 range: " + b);
                }
                out.write(b);
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final byte b[]) throws IOException {
            try {
                out.write(b);
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final byte b[], final int off, final int len) throws IOException {
            try {
                out.write(b, off, len);
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            try {
                throw new Exception("DEBUG");
            } catch (final Exception e) {
                _log.debug("close()", e);
            }
            super.close();
        }
    }

    /**
     * The Class MonitoredIS.
     */
    class MonitoredIS extends FilterInputStream {
        /**
         * Instantiates a new monitored is.
         *
         * @param in
         *            the in
         */
        public MonitoredIS(final InputStream in) {
            super(in);
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            try {
                final var read = in.read();
                if (read < 0 || read > 255) {
                    _log.debug("read() not in 0-255 range: " + read);
                }
                return read;
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte b[]) throws IOException {
            try {
                return in.read(b);
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte b[], final int off, final int len) throws IOException {
            try {
                return in.read(b, off, len);
            } catch (final IOException e) {
                _log.debug(e);
                throw e;
            }
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            try {
                throw new Exception("DEBUG");
            } catch (final Exception e) {
                _log.debug("close()", e);
            }
            super.close();
        }
    }
}
