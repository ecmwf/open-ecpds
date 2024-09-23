/**
 * Copyright 2005 Neil O'Toole - neilotoole@apache.org Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package ecmwf.common.rmi.interruptible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * Abstract class to that decorates a Socket to return instances of
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMISocketInputStream} and
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMISocketOutputStream} when {@link #getInputStream()} and
 * {@link #getOutputStream()} are called. Note that {@link java.nio.channels.SocketChannel} is not currently supported -
 * the {@link #getChannel()} method always returns null. Otherwise all other Socket methods are forwarded to the
 * decorated socket. In practice use the concrete subclasses for client and server-side sockets. These subclasses at a
 * minimum must implement the {@link #ioStarting()} and {@link #ioEnding()} methods.
 *
 * @author neilotoole@apache.org
 *
 * @see org.neilja.net.interruptiblermi.InterruptibleRMIClientSocket
 * @see org.neilja.net.interruptiblermi.InterruptibleRMIServerSideSocket
 */
abstract class InterruptibleRMISocket extends Socket {

    /**
     * The Constant SHUTDOWN_SOCKET.
     *
     * Special value to indicate that the server side socket should shutdown.
     */
    protected static final byte SHUTDOWN_SOCKET = Byte.MAX_VALUE;

    /** The current decoratee. */
    protected final Socket currentDecoratee;

    /**
     * Create a decorator for the given socket.
     *
     * @param decoratee
     *            the decoratee
     */
    InterruptibleRMISocket(final Socket decoratee) {
        currentDecoratee = decoratee;
    }

    /**
     * Called by {@link InterruptibleRMISocketInputStream} and {@link InterruptibleRMISocketOutputStream} before the
     * thread enters an RMI IO operation.
     */
    abstract void ioStarting();

    /**
     * Called by {@link InterruptibleRMISocketInputStream} and {@link InterruptibleRMISocketOutputStream} after the
     * thread exits an RMI IO operation.
     */
    abstract void ioEnding();

    /**
     * {@inheritDoc}
     *
     * Bind.
     *
     * @see Socket#bind(java.net.SocketAddress)
     */
    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        currentDecoratee.bind(bindpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     *
     * @see Socket#close()
     */
    @Override
    public void close() throws IOException {
        currentDecoratee.close();
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     *
     * @see Socket#connect(java.net.SocketAddress)
     */
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        currentDecoratee.connect(endpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     *
     * @see Socket#connect(java.net.SocketAddress, int)
     */
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        currentDecoratee.connect(endpoint, timeout);
    }

    /**
     * {@inheritDoc}
     *
     * Return true if the object is an instance of InterruptibleRMIClientSocket and decoratee.equals(object) returns
     * true.
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof final InterruptibleRMISocket socket
                && currentDecoratee.equals(socket.currentDecoratee);
    }

    /**
     * {@inheritDoc}
     *
     * This decorator does not implement a decorated SocketChannel. This method will always return null.
     */
    @Override
    public SocketChannel getChannel() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the inet address.
     *
     * @see Socket#getInetAddress()
     */
    @Override
    public InetAddress getInetAddress() {
        return currentDecoratee.getInetAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Return an instance of {@link InterruptibleRMISocketInputStream} that decorates the InputStream returned by
     * decoratee#getInputStream.
     *
     * @see Socket#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new InterruptibleRMISocketInputStream(currentDecoratee.getInputStream());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the keep alive.
     *
     * @see Socket#getKeepAlive()
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return currentDecoratee.getKeepAlive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local address.
     *
     * @see Socket#getLocalAddress()
     */
    @Override
    public InetAddress getLocalAddress() {
        return currentDecoratee.getLocalAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local port.
     *
     * @see Socket#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        return currentDecoratee.getLocalPort();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local socket address.
     *
     * @see Socket#getLocalSocketAddress()
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return currentDecoratee.getLocalSocketAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the OOB inline.
     *
     * @see Socket#getOOBInline()
     */
    @Override
    public boolean getOOBInline() throws SocketException {
        return currentDecoratee.getOOBInline();
    }

    /**
     * {@inheritDoc}
     *
     * Return an instance of {@link InterruptibleRMISocketOutputStream} that decorates the OutputStream returned by
     * decoratee#getOutputStream.
     *
     * @see Socket#getInputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new InterruptibleRMISocketOutputStream(currentDecoratee.getOutputStream());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     *
     * @see Socket#getPort()
     */
    @Override
    public int getPort() {
        return currentDecoratee.getPort();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the receive buffer size.
     *
     * @see Socket#getReceiveBufferSize()
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return currentDecoratee.getReceiveBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the remote socket address.
     *
     * @see Socket#getRemoteSocketAddress()
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return currentDecoratee.getRemoteSocketAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the reuse address.
     *
     * @see Socket#getReuseAddress()
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return currentDecoratee.getReuseAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the send buffer size.
     *
     * @see Socket#getSendBufferSize()
     */
    @Override
    public int getSendBufferSize() throws SocketException {
        return currentDecoratee.getSendBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so linger.
     *
     * @see Socket#getSoLinger()
     */
    @Override
    public int getSoLinger() throws SocketException {
        return currentDecoratee.getSoLinger();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so timeout.
     *
     * @see Socket#getSoTimeout()
     */
    @Override
    public int getSoTimeout() throws SocketException {
        return currentDecoratee.getSoTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the tcp no delay.
     *
     * @see Socket#getTcpNoDelay()
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return currentDecoratee.getTcpNoDelay();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic class.
     *
     * @see Socket#getTrafficClass()
     */
    @Override
    public int getTrafficClass() throws SocketException {
        return currentDecoratee.getTrafficClass();
    }

    /**
     * {@inheritDoc}
     *
     * Return decoratee#hashCode().
     */
    @Override
    public int hashCode() {
        return currentDecoratee.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is bound.
     *
     * @see Socket#isBound()
     */
    @Override
    public boolean isBound() {
        return currentDecoratee.isBound();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is closed.
     *
     * @see Socket#isClosed()
     */
    @Override
    public boolean isClosed() {
        return currentDecoratee.isClosed();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is connected.
     *
     * @see Socket#isConnected()
     */
    @Override
    public boolean isConnected() {
        return currentDecoratee.isConnected();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is input shutdown.
     *
     * @see Socket#isInputShutdown()
     */
    @Override
    public boolean isInputShutdown() {
        return currentDecoratee.isInputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is output shutdown.
     *
     * @see Socket#isOutputShutdown()
     */
    @Override
    public boolean isOutputShutdown() {
        return currentDecoratee.isOutputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Send urgent data.
     *
     * @see Socket#sendUrgentData(int)
     */
    @Override
    public void sendUrgentData(final int data) throws IOException {
        currentDecoratee.sendUrgentData(data);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the keep alive.
     *
     * @see Socket#setKeepAlive(boolean)
     */
    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        currentDecoratee.setKeepAlive(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the OOB inline.
     *
     * @see Socket#setOOBInline(boolean)
     */
    @Override
    public void setOOBInline(final boolean on) throws SocketException {
        currentDecoratee.setOOBInline(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the performance preferences.
     *
     * @see Socket#setPerformancePreferences(int, int, int)
     */
    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        currentDecoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the receive buffer size.
     *
     * @see Socket#setReceiveBufferSize(int)
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        currentDecoratee.setReceiveBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the reuse address.
     *
     * @see Socket#setReuseAddress(boolean)
     */
    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        currentDecoratee.setReuseAddress(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the send buffer size.
     *
     * @see Socket#setSendBufferSize(int)
     */
    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        currentDecoratee.setSendBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so linger.
     *
     * @see Socket#setSoLinger(boolean, int)
     */
    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        currentDecoratee.setSoLinger(on, linger);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so timeout.
     *
     * @see Socket#setSoTimeout(int)
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        currentDecoratee.setSoTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the tcp no delay.
     *
     * @see Socket#setTcpNoDelay(boolean)
     */
    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        currentDecoratee.setTcpNoDelay(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the traffic class.
     *
     * @see Socket#setTrafficClass(int)
     */
    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        currentDecoratee.setTrafficClass(tc);
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown input.
     *
     * @see Socket#shutdownInput()
     */
    @Override
    public void shutdownInput() throws IOException {
        currentDecoratee.shutdownInput();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown output.
     *
     * @see Socket#shutdownOutput()
     */
    @Override
    public void shutdownOutput() throws IOException {
        currentDecoratee.shutdownOutput();
    }

    /**
     * {@inheritDoc}
     *
     * Return decoratee#toString().
     */
    @Override
    public String toString() {
        return currentDecoratee.toString();
    }

    /**
     * The Class InterruptibleRMISocketOutputStream.
     */
    private final class InterruptibleRMISocketOutputStream extends OutputStream {

        /** The decoratee. */
        private final OutputStream decoratee;

        /**
         * Create a new InterruptibleRMISocketOutputStream instance that decorates the supplied OutputStream and calls
         * back to the supplied listener.
         *
         * @param decoratee
         *            the OutputStream to decorate
         */
        InterruptibleRMISocketOutputStream(final OutputStream decoratee) {
            this.decoratee = decoratee;
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see OutputStream#write(int)
         */
        @Override
        public synchronized void write(final int b) throws IOException {
            try {
                ioStarting();
                this.decoratee.write(b);
            } finally {
                ioEnding();
            }
        }

        /**
         * Close the underlying OutputStream. Unlike the #flush and #write methods, this method is not synchronized, and
         * it does not call back to the listener.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see OutputStream#close()
         */
        @Override
        public void close() throws IOException {
            this.decoratee.close();
        }

        /**
         * Return true if the supplied object is also an instance of InterruptibleRMISocketOutputStream, and if the
         * delegate and listener members are equal for both objects. Generally speaking IO objects do not override
         * #equals, so this method will tend to return false unless the decorated OutputStream class has overridden
         * #equals.
         *
         * @param obj
         *            the obj
         *
         * @return true, if successful
         */
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof final InterruptibleRMISocketOutputStream socket
                    && this.decoratee.equals(socket.decoratee);
        }

        /**
         * Flush.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see OutputStream#flush()
         */
        @Override
        public synchronized void flush() throws IOException {
            try {
                ioStarting();
                this.decoratee.flush();
            } finally {
                ioEnding();
            }
        }

        /**
         * Return decoratee#hashCode() ^ listener#hashCode().
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return this.decoratee.hashCode() ^ InterruptibleRMISocket.this.hashCode();
        }

        /**
         * Return this.getClass().getName() + [decoratee.toString()]
         *
         * @return the string
         */
        @Override
        public String toString() {
            return this.getClass().getName() + " [" + this.decoratee.toString() + "]";
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see OutputStream#write(byte[])
         */
        @Override
        public synchronized void write(final byte[] b) throws IOException {
            try {
                ioStarting();
                this.decoratee.write(b);
            } finally {
                ioEnding();
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
         *
         * @see OutputStream#write(byte[], int, int)
         */
        @Override
        public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                ioStarting();
                this.decoratee.write(b, off, len);
            } finally {
                ioEnding();
            }
        }
    }

    /**
     * The Class InterruptibleRMISocketInputStream.
     */
    private final class InterruptibleRMISocketInputStream extends InputStream {

        /** The decoratee. */
        private final InputStream decoratee;

        /**
         * Create a new InterruptibleRMISocketInputStream instance that decorates the supplied InputStream and calls
         * back to the supplied listener.
         *
         * @param decoratee
         *            the InputStream to decorate
         */
        InterruptibleRMISocketInputStream(final InputStream decoratee) {
            this.decoratee = decoratee;
        }

        /**
         * Available.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see InputStream#available()
         */
        @Override
        public synchronized int available() throws IOException {
            return this.decoratee.available();
        }

        /**
         * Close the underlying OutputStream. Unlike the #read methods, this method is not synchronized, and it does not
         * call back to the listener.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see OutputStream#close()
         */
        @Override
        public void close() throws IOException {
            this.decoratee.close();
        }

        /**
         * Return true if the supplied object is also an instance of InterruptibleRMISocketInputStream, and if the
         * delegate and listener members are equal for both objects. Generally speaking IO objects do not override
         * #equals, so this method will tend to return false unless the decorated InputStream class has itself
         * overridden #equals.
         *
         * @param obj
         *            the obj
         *
         * @return true, if successful
         */
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof final InterruptibleRMISocketInputStream socket
                    && this.decoratee.equals(socket.decoratee);
        }

        /**
         * Return delegate#hashCode() ^ listener#hashCode().
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return this.decoratee.hashCode() ^ InterruptibleRMISocket.this.hashCode();
        }

        /**
         * Mark.
         *
         * @param readlimit
         *            the readlimit
         *
         * @see InputStream#mark(int)
         */
        @Override
        public synchronized void mark(final int readlimit) {
            this.decoratee.mark(readlimit);
        }

        /**
         * Mark supported.
         *
         * @return true, if successful
         *
         * @see InputStream#markSupported()
         */
        @Override
        public boolean markSupported() {
            return this.decoratee.markSupported();
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see InputStream#read()
         */
        @Override
        public synchronized int read() throws IOException {
            try {
                ioStarting();
                return this.decoratee.read();
            } finally {
                ioEnding();
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
         *
         * @see InputStream#read(byte[])
         */
        @Override
        public synchronized int read(final byte[] b) throws IOException {
            try {
                ioStarting();
                return this.decoratee.read(b);
            } finally {
                ioEnding();
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
         *
         * @see InputStream#read(byte[], int, int)
         */
        @Override
        public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
            try {
                ioStarting();
                return this.decoratee.read(b, off, len);
            } finally {
                ioEnding();
            }
        }

        /**
         * Reset.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see InputStream#reset()
         */
        @Override
        public synchronized void reset() throws IOException {
            this.decoratee.reset();
        }

        /**
         * Skip.
         *
         * @param n
         *            the n
         *
         * @return the long
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see InputStream#skip(long)
         */
        @Override
        public synchronized long skip(final long n) throws IOException {
            try {
                ioStarting();
                return this.decoratee.skip(n);
            } finally {
                ioEnding();
            }
        }

        /**
         * Return this.getClass().getName() + [decoratee.toString()]
         *
         * @return the string
         */
        @Override
        public String toString() {
            return this.getClass().getName() + " [" + this.decoratee.toString() + "]";
        }
    }
}
