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
 * Socket decorator for ServerSocket to support the interruptible RMI mechanism.
 *
 * @author neilotoole@apache.org
 * @see InterruptibleRMIThreadFactory#newThread(Runnable)
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ResourceTracker;

/**
 * The Class InterruptibleRMIServerSocket.
 */
public final class InterruptibleRMIServerSocket extends ServerSocket {

	/** The Constant _log. */
	private static final Logger _log = LogManager.getLogger(InterruptibleRMIServerSocket.class);

	/** The current server socket. */
	private final ServerSocket currentServerSocket;

	/**
	 * Instantiates a new interruptible RMI server socket.
	 *
	 * @param serverSocket the server socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public InterruptibleRMIServerSocket(final ServerSocket serverSocket) throws IOException {
		this.currentServerSocket = serverSocket;
	}

	/**
	 * Return the socket associated with the RMI server thread (the current thread).
	 *
	 * @return the current RMI server thread socket
	 */
	public static Socket getCurrentRMIServerThreadSocket() {
		return InterruptibleRMIServerSideSocket.getCurrentRMIServerThreadSocket();
	}

	/**
	 * Return true if the socket associated with the RMI server thread (the current
	 * thread) has not been closed or marked for shutdown by the client of the RMI
	 * thread.
	 *
	 * @return true, if is current RMI server thread socket alive
	 */
	public static boolean isCurrentRMIServerThreadSocketAlive() {
		return InterruptibleRMIServerSideSocket.isCurrentRMIServerThreadSocketAlive();
	}

	/**
	 * Return true if the socket provided has not been closed or marked for shutdown
	 * by the client of the RMI thread. The socket is provided beforehand by a call
	 * to the getCurrentRMIServerThreadSocket method.
	 *
	 * @param socket the socket
	 * @return true, if is current RMI server thread socket alive
	 */
	public static boolean isCurrentRMIServerThreadSocketAlive(final Socket socket) {
		return InterruptibleRMIServerSideSocket.isCurrentRMIServerThreadSocketAlive(socket);
	}

	/**
	 * Accept.
	 *
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public Socket accept() throws IOException {
		return new InterruptibleRMIServerSideSocket(currentServerSocket.accept());
	}

	/**
	 * Bind.
	 *
	 * @param endpoint the endpoint
	 * @param backlog  the backlog
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void bind(final SocketAddress endpoint, final int backlog) throws IOException {
		currentServerSocket.bind(endpoint, backlog);
	}

	/**
	 * Bind.
	 *
	 * @param endpoint the endpoint
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void bind(final SocketAddress endpoint) throws IOException {
		currentServerSocket.bind(endpoint);
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void close() throws IOException {
		currentServerSocket.close();
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(final Object obj) {
		return currentServerSocket.equals(obj);
	}

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 */
	@Override
	public ServerSocketChannel getChannel() {
		return null;
	}

	/**
	 * Gets the inet address.
	 *
	 * @return the inet address
	 */
	@Override
	public InetAddress getInetAddress() {
		return currentServerSocket.getInetAddress();
	}

	/**
	 * Gets the local port.
	 *
	 * @return the local port
	 */
	@Override
	public int getLocalPort() {
		return currentServerSocket.getLocalPort();
	}

	/**
	 * Gets the local socket address.
	 *
	 * @return the local socket address
	 */
	@Override
	public SocketAddress getLocalSocketAddress() {
		return currentServerSocket.getLocalSocketAddress();
	}

	/**
	 * Gets the receive buffer size.
	 *
	 * @return the receive buffer size
	 * @throws SocketException the socket exception
	 */
	@Override
	public int getReceiveBufferSize() throws SocketException {
		return currentServerSocket.getReceiveBufferSize();
	}

	/**
	 * Gets the reuse address.
	 *
	 * @return the reuse address
	 * @throws SocketException the socket exception
	 */
	@Override
	public boolean getReuseAddress() throws SocketException {
		return currentServerSocket.getReuseAddress();
	}

	/**
	 * Gets the so timeout.
	 *
	 * @return the so timeout
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public int getSoTimeout() throws IOException {
		return currentServerSocket.getSoTimeout();
	}

	/**
	 * Hash code.
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
	 */
	@Override
	public boolean isBound() {
		return currentServerSocket.isBound();
	}

	/**
	 * Checks if is closed.
	 *
	 * @return true, if is closed
	 */
	@Override
	public boolean isClosed() {
		return currentServerSocket.isClosed();
	}

	/**
	 * Sets the performance preferences.
	 *
	 * @param ct  the ct
	 * @param lat the lat
	 * @param bw  the bw
	 */
	@Override
	public void setPerformancePreferences(final int ct, final int lat, final int bw) {
		currentServerSocket.setPerformancePreferences(ct, lat, bw);
	}

	/**
	 * Sets the receive buffer size.
	 *
	 * @param size the new receive buffer size
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setReceiveBufferSize(final int size) throws SocketException {
		currentServerSocket.setReceiveBufferSize(size);
	}

	/**
	 * Sets the reuse address.
	 *
	 * @param on the new reuse address
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setReuseAddress(final boolean on) throws SocketException {
		currentServerSocket.setReuseAddress(on);
	}

	/**
	 * Sets the so timeout.
	 *
	 * @param timeout the new so timeout
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setSoTimeout(final int timeout) throws SocketException {
		currentServerSocket.setSoTimeout(timeout);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return currentServerSocket.toString();
	}

	/**
	 * The Class InterruptibleRMIServerSideSocket.
	 */
	private static final class InterruptibleRMIServerSideSocket extends InterruptibleRMISocket {

		/** The Constant TRACKER. */
		private static final ResourceTracker TRACKER = new ResourceTracker(InterruptibleRMIServerSideSocket.class);

		/** The Constant threadSocketMap. */
		private static final Map<Thread, Socket> threadSocketMap = new ConcurrentHashMap<>();

		/** The Constant socketThreadMap. */
		private static final Map<Socket, Thread> socketThreadMap = new ConcurrentHashMap<>();

		/**
		 * Gets the current RMI server thread socket.
		 *
		 * @return the current RMI server thread socket
		 */
		public static Socket getCurrentRMIServerThreadSocket() {
			return threadSocketMap.get(Thread.currentThread());
		}

		/**
		 * Return true if the socket associated with the RMI server thread (the current
		 * thread) has not been closed or marked for shutdown by the client of the RMI
		 * thread.
		 *
		 * @return true, if is current RMI server thread socket alive
		 */
		public static boolean isCurrentRMIServerThreadSocketAlive() {
			return isCurrentRMIServerThreadSocketAlive(getCurrentRMIServerThreadSocket());
		}

		/**
		 * Return true if the socket associated with the RMI server thread (the current
		 * thread) has not been closed or marked for shutdown by the client of the RMI
		 * thread.
		 *
		 * @param socket the socket
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

		/**
		 * Register thread is using socket.
		 *
		 * @param socket the socket
		 */
		private static void registerThreadIsUsingSocket(final InterruptibleRMIServerSideSocket socket) {
			final var thread = Thread.currentThread();
			// Check if already registered and same socket
			if (socketThreadMap.get(socket) == thread && threadSocketMap.get(thread) == socket) {
				return; // Already registered correctly, no need to update
			}
			final var previous = socketThreadMap.remove(socket);
			if (previous != null) {
				threadSocketMap.remove(previous);
			}
			threadSocketMap.put(thread, socket);
			socketThreadMap.put(socket, thread);
			_log.debug("Registered Thread/Socket: {}", socket);
		}

		/**
		 * Register socket is closing.
		 *
		 * @param socket the socket
		 */
		private static void registerSocketIsClosing(final InterruptibleRMIServerSideSocket socket) {
			final var thread = socketThreadMap.remove(socket);
			if (thread != null) {
				threadSocketMap.remove(thread);
				thread.interrupt();
				_log.debug("Unregistered Thread/Socket: {}", socket);
			}
		}

		/**
		 * Instantiates a new interruptible RMI server side socket.
		 *
		 * @param decoratee the decoratee
		 */
		InterruptibleRMIServerSideSocket(final Socket decoratee) {
			super(decoratee);
		}

		/**
		 * If not already associated, this method associates the current thread with
		 * this socket.
		 */
		@Override
		void ioStarting() {
			registerThreadIsUsingSocket(this);
			TRACKER.onOpen();
		}

		/**
		 * Does nothing.
		 */
		@Override
		void ioEnding() {
			TRACKER.onClose(true);
		}

		/**
		 * Closes the decorated socket and then dissociates the current thread from this
		 * socket.
		 *
		 * @throws IOException Signals that an I/O exception has occurred.
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
