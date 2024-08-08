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

package ecmwf.common.rmi.interruptible;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Socket decorator for ServerSocket to support the interruptible RMI mechanism.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class InterruptibleRMIServerSocket.
 */
public final class InterruptibleRMIServerSocket extends ServerSocket {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(InterruptibleRMIServerSocket.class);

    /** The current server socket. */
    private final ServerSocket currentServerSocket;

    /**
     * Create decorator for given socket.
     *
     * @param serverSocket
     *            the server socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public InterruptibleRMIServerSocket(final ServerSocket serverSocket) throws IOException {
        currentServerSocket = serverSocket;
    }

    /**
     * Return the socket associated with the RMI server thread (the current thread).
     *
     * @return socket
     */
    public static Socket getCurrentRMIServerThreadSocket() {
        return InterruptibleRMIServerSideSocket.getCurrentRMIServerThreadSocket();
    }

    /**
     * Return true if the socket associated with the RMI server thread (the current thread) has not been closed or
     * marked for shutdown by the client of the RMI thread.
     *
     * @return true, if is current RMI server thread socket alive
     */
    public static boolean isCurrentRMIServerThreadSocketAlive() {
        return InterruptibleRMIServerSideSocket.isCurrentRMIServerThreadSocketAlive();
    }

    /**
     * Return true if the socket provided has not been closed or marked for shutdown by the client of the RMI thread.
     * The socket is provided beforehand by a call to the getCurrentRMIServerThreadSocket method.
     *
     * @param socket
     *            the socket
     *
     * @return true, if is current RMI server thread socket alive
     */
    public static boolean isCurrentRMIServerThreadSocketAlive(final Socket socket) {
        return InterruptibleRMIServerSideSocket.isCurrentRMIServerThreadSocketAlive(socket);
    }

    /**
     * Accept.
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see ServerSocket#accept()
     */
    @Override
    public Socket accept() throws IOException {
        return new InterruptibleRMIServerSideSocket(currentServerSocket.accept());
    }

    /**
     * Bind.
     *
     * @param endpoint
     *            the endpoint
     * @param backlog
     *            the backlog
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see ServerSocket#bind(java.net.SocketAddress, int)
     */
    @Override
    public void bind(final SocketAddress endpoint, final int backlog) throws IOException {
        currentServerSocket.bind(endpoint, backlog);
    }

    /**
     * Bind.
     *
     * @param endpoint
     *            the endpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see ServerSocket#bind(java.net.SocketAddress)
     */
    @Override
    public void bind(final SocketAddress endpoint) throws IOException {
        currentServerSocket.bind(endpoint);
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see ServerSocket#close()
     */
    @Override
    public void close() throws IOException {
        currentServerSocket.close();
    }

    /**
     * Returns true if the decoratee's equals method returns true.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        return currentServerSocket.equals(obj);
    }

    /**
     * Not implemented - always returns null.
     *
     * @return the channel
     *
     * @see ServerSocket#getChannel()
     */
    @Override
    public ServerSocketChannel getChannel() {
        return null;
    }

    /**
     * Gets the inet address.
     *
     * @return the inet address
     *
     * @see ServerSocket#getInetAddress()
     */
    @Override
    public InetAddress getInetAddress() {
        return currentServerSocket.getInetAddress();
    }

    /**
     * Gets the local port.
     *
     * @return the local port
     *
     * @see ServerSocket#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        return currentServerSocket.getLocalPort();
    }

    /**
     * Gets the local socket address.
     *
     * @return the local socket address
     *
     * @see ServerSocket#getLocalSocketAddress()
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return currentServerSocket.getLocalSocketAddress();
    }

    /**
     * Gets the receive buffer size.
     *
     * @return the receive buffer size
     *
     * @throws SocketException
     *             the socket exception
     *
     * @see ServerSocket#getReceiveBufferSize()
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return currentServerSocket.getReceiveBufferSize();
    }

    /**
     * Gets the reuse address.
     *
     * @return the reuse address
     *
     * @throws SocketException
     *             the socket exception
     *
     * @see ServerSocket#getReuseAddress()
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return currentServerSocket.getReuseAddress();
    }

    /**
     * Gets the so timeout.
     *
     * @return the so timeout
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @see ServerSocket#getSoTimeout()
     */
    @Override
    public int getSoTimeout() throws IOException {
        return currentServerSocket.getSoTimeout();
    }

    /**
     * Return the hashcode of the decorated socket.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return currentServerSocket.hashCode();
    }

    /**
     * Checks if is bound.
     *
     * @return true, if is bound
     *
     * @see ServerSocket#isBound()
     */
    @Override
    public boolean isBound() {
        return currentServerSocket.isBound();
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     *
     * @see ServerSocket#isClosed()
     */
    @Override
    public boolean isClosed() {
        return currentServerSocket.isClosed();
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
     *
     * @see ServerSocket#setPerformancePreferences(int, int, int)
     */
    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        currentServerSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
     * Sets the receive buffer size.
     *
     * @param size
     *            the new receive buffer size
     *
     * @throws SocketException
     *             the socket exception
     *
     * @see ServerSocket#setReceiveBufferSize(int)
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        currentServerSocket.setReceiveBufferSize(size);
    }

    /**
     * Sets the reuse address.
     *
     * @param on
     *            the new reuse address
     *
     * @throws SocketException
     *             the socket exception
     *
     * @see ServerSocket#setReuseAddress(boolean)
     */
    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        currentServerSocket.setReuseAddress(on);
    }

    /**
     * Sets the so timeout.
     *
     * @param timeout
     *            the new so timeout
     *
     * @throws SocketException
     *             the socket exception
     *
     * @see ServerSocket#setSoTimeout(int)
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        currentServerSocket.setSoTimeout(timeout);
    }

    /**
     * To string.
     *
     * @return the string
     *
     * @see ServerSocket#toString()
     */
    @Override
    public String toString() {
        return currentServerSocket.toString();
    }

    /**
     * The Class InterruptibleRMIServerSideSocket.
     */
    private static final class InterruptibleRMIServerSideSocket extends InterruptibleRMISocket {

        /**
         * A mapping of server RMI threads to the sockets that the thread is using for RMI calls.
         */
        private static Map<Thread, Socket> threadSocketMap = null;

        /**
         * A mapping of RMI sockets to the server RMI threads using the sockets.
         */
        private static Map<Socket, Thread> socketThreadMap = null;

        /**
         * (Lazily) initialize the maps (and exception handler) for managing server RMI socket/thread tracking.
         */
        private static void initializeServerThreadSocketMapping() {
            threadSocketMap = new HashMap<>();
            socketThreadMap = new HashMap<>();
        }

        /**
         * Called by {@link #ioStarting()} to associate a server RMI thread (the current thread) with the supplied
         * socket.
         *
         * @param socket
         *            the socket
         */
        private static synchronized void registerThreadIsUsingSocket(final InterruptibleRMIServerSideSocket socket) {
            if (threadSocketMap == null) {
                initializeServerThreadSocketMapping();
            }
            /*
             * Clear out any previously registered thread
             */
            final var previousThread = socketThreadMap.remove(socket);
            if (previousThread != null) {
                threadSocketMap.remove(previousThread);
            }
            /*
             * Associate the thread and the socket.
             */
            final var currentThread = Thread.currentThread();
            threadSocketMap.put(currentThread, socket);
            socketThreadMap.put(socket, currentThread);
            _log.debug("Register Thread/Socket: {} ({}/{})", socket, threadSocketMap.size(), socketThreadMap.size());
        }

        /**
         * Called when the socket's {@link #close()} method is invoked. This method dissociates the socket from its
         * associated RMI server thread, and calls {@link Thread#interrupt()} on the thread in case it is currently
         * waiting.
         *
         * @param socket
         *            the socket
         */
        private static synchronized void registerSocketIsClosing(final InterruptibleRMIServerSideSocket socket) {
            if (threadSocketMap == null) {
                return;
            }
            final var thread = socketThreadMap.remove(socket);
            // If the thread is found then it was not yet closed/interrupted
            // (this method can be called from the Finalizer)!
            if (thread != null) {
                threadSocketMap.remove(thread);
                _log.debug("Unregister Thread/Socket: {} ({}/{})", socket, threadSocketMap.size(),
                        socketThreadMap.size());
                thread.interrupt();
            }
        }

        /**
         * Gets the socket in a synchronized way to avoid synchronizing the full isCurrentRMIServerThreadSocketAlive
         * method.
         *
         * @param thread
         *            the thread
         *
         * @return socket
         */
        private static synchronized Socket getSocket(final Thread thread) {
            if (threadSocketMap != null) {
                return threadSocketMap.get(thread);
            }
            return null;
        }

        /**
         * Return the socket associated with the RMI server thread (the current thread).
         *
         * @return socket
         */
        public static Socket getCurrentRMIServerThreadSocket() {
            return getSocket(Thread.currentThread());
        }

        /**
         * Return true if the socket associated with the RMI server thread (the current thread) has not been closed or
         * marked for shutdown by the client of the RMI thread.
         *
         * @return true, if is current RMI server thread socket alive
         */
        public static boolean isCurrentRMIServerThreadSocketAlive() {
            return isCurrentRMIServerThreadSocketAlive(getCurrentRMIServerThreadSocket());
        }

        /**
         * Return true if the socket associated with the RMI server thread (the current thread) has not been closed or
         * marked for shutdown by the client of the RMI thread.
         *
         * @param socket
         *            the socket
         *
         * @return true, if is current RMI server thread socket alive
         */
        public static boolean isCurrentRMIServerThreadSocketAlive(final Socket socket) {
            if (socket == null || socket.isClosed()) {
                _log.warn("RMI Socket closed ({})", socket);
                return false;
            }
            try {
                // When a client explicitly interrupts the InterruptibleRMIThread, the special
                // value SHUTDOWN_SOCKET is written to the socket indicating it should shut
                // down.
                final var in = socket.getInputStream();
                if (in.available() > 0 && in.read() == InterruptibleRMISocket.SHUTDOWN_SOCKET) {
                    _log.warn("RMI Socket received shutdown ({})", socket);
                    return false;
                }
            } catch (final Exception e) {
                // If an exception occurs while calling an operation on the socket, then it's
                // fair to assume that the socket is dead!
                _log.warn("RMI Socket error ({})", socket, e);
                return false;
            }
            return true;
        }

        /** The most recent thread. */
        private Thread mostRecentThread;

        /**
         * Instantiates a new interruptible RMI server side socket.
         *
         * @param decoratee
         *            the decoratee
         *
         * @see InterruptibleRMISocket#InterruptibleRMISocket(Socket)
         */
        InterruptibleRMIServerSideSocket(final Socket decoratee) {
            super(decoratee);
        }

        /**
         * Does nothing.
         *
         * @see InterruptibleRMISocket#ioEnding()
         */
        @Override
        void ioEnding() {
            // Nothing to do!
        }

        /**
         * If not already associated, this method associates the current thread with this socket.
         *
         * @see InterruptibleRMISocket#ioStarting()
         */
        @Override
        void ioStarting() {
            if (Thread.currentThread() != this.mostRecentThread) {
                this.mostRecentThread = Thread.currentThread();
                registerThreadIsUsingSocket(this);
            }
        }

        /**
         * Closes the decorated socket and then dissociates the current thread from this socket.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         *
         * @see InterruptibleRMISocket#close()
         */
        @Override
        public void close() throws IOException {
            try {
                currentDecoratee.close();
            } finally {
                registerSocketIsClosing(this);
            }
        }
    }
}
