/**
 * Copyright 2005 Neil O'Toole - neilotoole@apache.org Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package ecmwf.common.rmi.interruptible;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Tailored Neil O'Toole's original version to specific requirements.
 *
 * @author neilotoole@apache.org
 * @see InterruptibleRMIThreadFactory#newThread(Runnable)
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ResourceTracker;

/**
 * ECMWF Product Data Store (ECPDS) Project
 *
 * Adapted Neil O'Toole's original version to support modern Java features.
 *
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

    /** The Constant USE_POOLED_BUFFERED_STREAMS. */
    private static final boolean USE_POOLED_BUFFERED_STREAMS = Cnf.at("InterruptibleRMISocket",
            "usePooledBufferedStreams", true);

    /** The Constant SHUTDOWN_SOCKET. */
    protected static final byte SHUTDOWN_SOCKET = Byte.MAX_VALUE;

    /** The current decoratee. */
    protected final Socket currentDecoratee;

    /** The cached input. */
    private InputStream cachedInput;

    /** The cached output. */
    private OutputStream cachedOutput;

    /** The buffers. */
    private final ConcurrentLinkedQueue<byte[]> buffers = new ConcurrentLinkedQueue<>();

    /**
     * Instantiates a new interruptible RMI socket.
     *
     * @param decoratee
     *            the decoratee
     */
    InterruptibleRMISocket(final Socket decoratee) {
        this.currentDecoratee = decoratee;
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
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized InputStream getInputStream() throws IOException {
        if (cachedInput == null) {
            if (USE_POOLED_BUFFERED_STREAMS) {
                final var buffer = BufferPool.acquire();
                buffers.add(buffer);
                final var pooledIn = new PooledBufferedInputStream(currentDecoratee.getInputStream(), buffer);
                cachedInput = new InterruptibleRMISocketInputStream(pooledIn);
            } else {
                cachedInput = currentDecoratee.getInputStream();
            }
        }
        return cachedInput;
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
    public synchronized OutputStream getOutputStream() throws IOException {
        if (cachedOutput == null) {
            if (USE_POOLED_BUFFERED_STREAMS) {
                final var buffer = BufferPool.acquire();
                buffers.add(buffer);
                final var pooledOut = new PooledBufferedOutputStream(currentDecoratee.getOutputStream(), buffer);
                cachedOutput = new InterruptibleRMISocketOutputStream(pooledOut);
            } else {
                cachedOutput = currentDecoratee.getOutputStream();
            }
        }
        return cachedOutput;
    }

    /**
     * The Class BufferPool.
     */
    private static final class BufferPool {

        /** The Constant TRACKER. */
        private static final ResourceTracker TRACKER = new ResourceTracker(BufferPool.class);

        /** The Constant BUFFER_SIZE. */
        private static final int BUFFER_SIZE = Cnf.at("InterruptibleRMISocket", "bufferSize", 64 * 1024);

        /** The Constant BUFFER_TTL_MS. */
        private static final long BUFFER_TTL_MS = TimeUnit.MINUTES
                .toMillis(Cnf.at("InterruptibleRMISocket", "bufferTTLInMinutes", 5));

        /** The Constant reusable – using a deque for LIFO behavior. */
        private static final ConcurrentLinkedDeque<byte[]> reusable = new ConcurrentLinkedDeque<>();

        /** The Constant expirations. */
        private static final ConcurrentMap<byte[], ScheduledFuture<?>> expirations = new ConcurrentHashMap<>();

        /** The Constant scheduler. */
        private static final ScheduledExecutorService scheduler = Executors
                .newSingleThreadScheduledExecutor(Thread.ofVirtual().name("BufferPool-Cleanup", 0).factory());

        /** Prevent instantiation. */
        private BufferPool() {
        }

        /**
         * Acquire a buffer, reusing the most recently released one first.
         *
         * @return the byte[]
         */
        public static byte[] acquire() {
            TRACKER.onOpen();
            final var buffer = reusable.pollFirst(); // LIFO: take most recently added
            if (buffer != null) {
                synchronized (buffer) {
                    final var task = expirations.remove(buffer);
                    if (task != null) {
                        task.cancel(false);
                    }
                }
                // Borrow buffer
                return buffer;
            }
            // Create new buffer
            return new byte[BUFFER_SIZE];
        }

        /**
         * Release a buffer to the pool and schedule its expiration.
         *
         * @param buffer
         *            the buffer
         */
        public static void release(final byte[] buffer) {
            if (buffer == null)
                return;
            TRACKER.onClose(true);
            synchronized (buffer) {
                reusable.push(buffer); // LIFO: add to front
                final ScheduledFuture<?> future = scheduler.schedule(() -> {
                    synchronized (buffer) {
                        // Expired unused buffer
                        reusable.remove(buffer);
                        final var task = expirations.remove(buffer);
                        if (task != null) {
                            task.cancel(false);
                        }

                    }
                }, BUFFER_TTL_MS, TimeUnit.MILLISECONDS);
                expirations.put(buffer, future);
            }
        }
    }

    /**
     * The Class PooledBufferedInputStream.
     */
    private final class PooledBufferedInputStream extends FilterInputStream {

        /** The buffer. */
        private byte[] buffer;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The pos. */
        private int pos = 0;

        /** The count. */
        private int count = 0;

        /**
         * Instantiates a new pooled buffered input stream.
         *
         * @param in
         *            the in
         * @param buffer
         *            the buffer
         */
        public PooledBufferedInputStream(final InputStream in, final byte[] buffer) {
            super(in);
            this.buffer = buffer;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                try {
                    super.close();
                } finally {
                    BufferPool.release(buffer);
                    buffers.remove(buffer);
                    buffer = null;
                }
            }
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
            if (pos >= count) {
                fill();
                if (count == -1)
                    return -1;
            }
            return buffer[pos++] & 0xff;
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
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (pos >= count) {
                if (len >= buffer.length) {
                    return in.read(b, off, len);
                }
                fill();
                if (count == -1)
                    return -1;
            }
            final var avail = count - pos;
            final var toCopy = Math.min(len, avail);
            System.arraycopy(buffer, pos, b, off, toCopy);
            pos += toCopy;
            return toCopy;
        }

        /**
         * Fill.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void fill() throws IOException {
            count = in.read(buffer, 0, buffer.length);
            pos = 0;
        }
    }

    /**
     * The Class PooledBufferedOutputStream.
     */
    private final class PooledBufferedOutputStream extends FilterOutputStream {

        /** The buffer. */
        private byte[] buffer;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The count. */
        private int count = 0;

        /**
         * Instantiates a new pooled buffered output stream.
         *
         * @param out
         *            the out
         * @param buffer
         *            the buffer
         */
        public PooledBufferedOutputStream(final OutputStream out, final byte[] buffer) {
            super(out);
            this.buffer = buffer;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                try {
                    super.close();
                } finally {
                    BufferPool.release(buffer);
                    buffers.remove(buffer);
                    buffer = null;
                }
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
        public void write(final int b) throws IOException {
            if (count >= buffer.length) {
                flushBuffer();
            }
            buffer[count++] = (byte) b;
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
        public void write(final byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                final var space = buffer.length - count;
                final var toWrite = Math.min(space, len);
                System.arraycopy(b, off, buffer, count, toWrite);
                count += toWrite;
                off += toWrite;
                len -= toWrite;
                if (count >= buffer.length) {
                    flushBuffer();
                }
            }
        }

        /**
         * Flush.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void flush() throws IOException {
            flushBuffer();
            out.flush();
        }

        /**
         * Flush buffer.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void flushBuffer() throws IOException {
            if (count > 0) {
                out.write(buffer, 0, count);
                count = 0;
            }
        }
    }

    /**
     * The Class InterruptibleRMISocketOutputStream.
     */
    private final class InterruptibleRMISocketOutputStream extends FilterOutputStream {

        /**
         * Instantiates a new interruptible RMI socket output stream.
         *
         * @param out
         *            the out
         * @param buffer
         *            the buffer
         */
        InterruptibleRMISocketOutputStream(final OutputStream out) {
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
            ioStarting();
            try {
                out.write(b);
            } finally {
                ioEnding();
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
        public void write(final byte[] b) throws IOException {
            ioStarting();
            try {
                out.write(b);
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
         */
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            ioStarting();
            try {
                out.write(b, off, len);
            } finally {
                ioEnding();
            }
        }

        /**
         * Flush.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void flush() throws IOException {
            ioStarting();
            try {
                out.flush();
            } finally {
                ioEnding();
            }
        }
    }

    /**
     * The Class InterruptibleRMISocketInputStream.
     */
    private final class InterruptibleRMISocketInputStream extends FilterInputStream {

        /**
         * Instantiates a new interruptible RMI socket input stream.
         *
         * @param in
         *            the in
         * @param buffer
         *            the buffer
         */
        InterruptibleRMISocketInputStream(final InputStream in) {
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
            ioStarting();
            try {
                return in.read();
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
         */
        @Override
        public int read(final byte[] b) throws IOException {
            ioStarting();
            try {
                return in.read(b);
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
         */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            ioStarting();
            try {
                return in.read(b, off, len);
            } finally {
                ioEnding();
            }
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
         */
        @Override
        public long skip(final long n) throws IOException {
            ioStarting();
            try {
                return in.skip(n);
            } finally {
                ioEnding();
            }
        }

    }

    /**
     * Bind.
     *
     * @param bindpoint
     *            the bindpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    // Delegate all other Socket methods
    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        currentDecoratee.bind(bindpoint);
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
            if (cachedInput != null)
                cachedInput.close();
        } catch (final IOException _) {
            // Ignored
        }
        try {
            if (cachedOutput != null)
                cachedOutput.close();
        } catch (final IOException _) {
            // Ignored
        }
        // Release any remaining buffers
        if (USE_POOLED_BUFFERED_STREAMS) {
            byte[] buffer;
            while ((buffer = buffers.poll()) != null) {
                BufferPool.release(buffer);
            }
        }
        currentDecoratee.close();
    }

    /**
     * Connect.
     *
     * @param endpoint
     *            the endpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        currentDecoratee.connect(endpoint);
    }

    /**
     * Connect.
     *
     * @param endpoint
     *            the endpoint
     * @param timeout
     *            the timeout
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        currentDecoratee.connect(endpoint, timeout);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final InterruptibleRMISocket other && currentDecoratee.equals(other.currentDecoratee);
    }

    /**
     * This decorator does not implement a decorated SocketChannel. This method will always return null.
     *
     * @return the channel
     */
    @Override
    public SocketChannel getChannel() {
        return null;
    }

    /**
     * Gets the inet address.
     *
     * @return the inet address
     */
    @Override
    public InetAddress getInetAddress() {
        return currentDecoratee.getInetAddress();
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
        return currentDecoratee.getKeepAlive();
    }

    /**
     * Gets the local address.
     *
     * @return the local address
     */
    @Override
    public InetAddress getLocalAddress() {
        return currentDecoratee.getLocalAddress();
    }

    /**
     * Gets the local port.
     *
     * @return the local port
     */
    @Override
    public int getLocalPort() {
        return currentDecoratee.getLocalPort();
    }

    /**
     * Gets the local socket address.
     *
     * @return the local socket address
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return currentDecoratee.getLocalSocketAddress();
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
        return currentDecoratee.getOOBInline();
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    @Override
    public int getPort() {
        return currentDecoratee.getPort();
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
    public int getReceiveBufferSize() throws SocketException {
        return currentDecoratee.getReceiveBufferSize();
    }

    /**
     * Gets the remote socket address.
     *
     * @return the remote socket address
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return currentDecoratee.getRemoteSocketAddress();
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
        return currentDecoratee.getReuseAddress();
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
    public int getSendBufferSize() throws SocketException {
        return currentDecoratee.getSendBufferSize();
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
        return currentDecoratee.getSoLinger();
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
    public int getSoTimeout() throws SocketException {
        return currentDecoratee.getSoTimeout();
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
        return currentDecoratee.getTcpNoDelay();
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
        return currentDecoratee.getTrafficClass();
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return currentDecoratee.hashCode();
    }

    /**
     * Checks if is bound.
     *
     * @return true, if is bound
     */
    @Override
    public boolean isBound() {
        return currentDecoratee.isBound();
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    @Override
    public boolean isClosed() {
        return currentDecoratee.isClosed();
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    @Override
    public boolean isConnected() {
        return currentDecoratee.isConnected();
    }

    /**
     * Checks if is input shutdown.
     *
     * @return true, if is input shutdown
     */
    @Override
    public boolean isInputShutdown() {
        return currentDecoratee.isInputShutdown();
    }

    /**
     * Checks if is output shutdown.
     *
     * @return true, if is output shutdown
     */
    @Override
    public boolean isOutputShutdown() {
        return currentDecoratee.isOutputShutdown();
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
        currentDecoratee.sendUrgentData(data);
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
        currentDecoratee.setKeepAlive(on);
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
        currentDecoratee.setOOBInline(on);
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
        currentDecoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
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
    public void setReceiveBufferSize(final int size) throws SocketException {
        currentDecoratee.setReceiveBufferSize(size);
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
        currentDecoratee.setReuseAddress(on);
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
    public void setSendBufferSize(final int size) throws SocketException {
        currentDecoratee.setSendBufferSize(size);
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
        currentDecoratee.setSoLinger(on, linger);
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
    public void setSoTimeout(final int timeout) throws SocketException {
        currentDecoratee.setSoTimeout(timeout);
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
        currentDecoratee.setTcpNoDelay(on);
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
        currentDecoratee.setTrafficClass(tc);
    }

    /**
     * Shutdown input.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void shutdownInput() throws IOException {
        currentDecoratee.shutdownInput();
    }

    /**
     * Shutdown output.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void shutdownOutput() throws IOException {
        currentDecoratee.shutdownOutput();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return currentDecoratee.toString();
    }
}
