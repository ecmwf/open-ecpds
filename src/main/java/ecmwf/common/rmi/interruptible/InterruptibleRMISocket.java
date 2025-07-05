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

/**
 * ECMWF Product Data Store (ECPDS) Project
 *
 * Adapted Neil O'Toole's original version to support modern Java features.
 *
 * Abstract class to that decorates a Socket to return instances of
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMISocketInputStream} and
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMISocketOutputStream}
 * when {@link #getInputStream()} and {@link #getOutputStream()} are called.
 * Note that {@link java.nio.channels.SocketChannel} is not currently supported
 * - the {@link #getChannel()} method always returns null. Otherwise all other
 * Socket methods are forwarded to the decorated socket. In practice use the
 * concrete subclasses for client and server-side sockets. These subclasses at a
 * minimum must implement the {@link #ioStarting()} and {@link #ioEnding()}
 * methods.
 *
 * @author neilotoole@apache.org
 * @see org.neilja.net.interruptiblermi.InterruptibleRMIClientSocket
 * @see org.neilja.net.interruptiblermi.InterruptibleRMIServerSideSocket
 */
abstract class InterruptibleRMISocket extends Socket {

	/** The Constant SHUTDOWN_SOCKET. */
	protected static final byte SHUTDOWN_SOCKET = Byte.MAX_VALUE;

	/** The current decoratee. */
	protected final Socket currentDecoratee;

	/** The cached input. */
	private InputStream cachedInput;

	/** The cached output. */
	private OutputStream cachedOutput;

	/**
	 * Instantiates a new interruptible RMI socket.
	 *
	 * @param decoratee the decoratee
	 */
	InterruptibleRMISocket(final Socket decoratee) {
		this.currentDecoratee = decoratee;
	}

	/**
	 * Called by {@link InterruptibleRMISocketInputStream} and
	 * {@link InterruptibleRMISocketOutputStream} before the thread enters an RMI IO
	 * operation.
	 */
	abstract void ioStarting();

	/**
	 * Called by {@link InterruptibleRMISocketInputStream} and
	 * {@link InterruptibleRMISocketOutputStream} after the thread exits an RMI IO
	 * operation.
	 */
	abstract void ioEnding();

	/**
	 * Gets the input stream.
	 *
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (cachedInput == null) {
			cachedInput = new InterruptibleRMISocketInputStream(currentDecoratee.getInputStream());
		}
		return cachedInput;
	}

	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (cachedOutput == null) {
			cachedOutput = new InterruptibleRMISocketOutputStream(currentDecoratee.getOutputStream());
		}
		return cachedOutput;
	}

	/**
	 * The Class InterruptibleRMISocketOutputStream.
	 */
	private final class InterruptibleRMISocketOutputStream extends FilterOutputStream {

		/**
		 * Instantiates a new interruptible RMI socket output stream.
		 *
		 * @param out the out
		 */
		InterruptibleRMISocketOutputStream(final OutputStream out) {
			super(out);
		}

		/**
		 * Write.
		 *
		 * @param b the b
		 * @throws IOException Signals that an I/O exception has occurred.
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
		 * @param b   the b
		 * @param off the off
		 * @param len the len
		 * @throws IOException Signals that an I/O exception has occurred.
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
		 * @throws IOException Signals that an I/O exception has occurred.
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
		 * @param in the in
		 */
		InterruptibleRMISocketInputStream(final InputStream in) {
			super(in);
		}

		/**
		 * Read.
		 *
		 * @return the int
		 * @throws IOException Signals that an I/O exception has occurred.
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
		 * @param b   the b
		 * @param off the off
		 * @param len the len
		 * @return the int
		 * @throws IOException Signals that an I/O exception has occurred.
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
		 * @param n the n
		 * @return the long
		 * @throws IOException Signals that an I/O exception has occurred.
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
	 * @param bindpoint the bindpoint
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	// Delegate all other Socket methods
	@Override
	public void bind(final SocketAddress bindpoint) throws IOException {
		currentDecoratee.bind(bindpoint);
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void close() throws IOException {
		currentDecoratee.close();
	}

	/**
	 * Connect.
	 *
	 * @param endpoint the endpoint
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void connect(final SocketAddress endpoint) throws IOException {
		currentDecoratee.connect(endpoint);
	}

	/**
	 * Connect.
	 *
	 * @param endpoint the endpoint
	 * @param timeout  the timeout
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
		currentDecoratee.connect(endpoint, timeout);
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof final InterruptibleRMISocket other && currentDecoratee.equals(other.currentDecoratee);
	}

	/**
	 * This decorator does not implement a decorated SocketChannel. This method will
	 * always return null.
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
	 * @throws SocketException the socket exception
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
	 * @throws SocketException the socket exception
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
	 * @throws SocketException the socket exception
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
	 * @throws SocketException the socket exception
	 */
	@Override
	public boolean getReuseAddress() throws SocketException {
		return currentDecoratee.getReuseAddress();
	}

	/**
	 * Gets the send buffer size.
	 *
	 * @return the send buffer size
	 * @throws SocketException the socket exception
	 */
	@Override
	public int getSendBufferSize() throws SocketException {
		return currentDecoratee.getSendBufferSize();
	}

	/**
	 * Gets the so linger.
	 *
	 * @return the so linger
	 * @throws SocketException the socket exception
	 */
	@Override
	public int getSoLinger() throws SocketException {
		return currentDecoratee.getSoLinger();
	}

	/**
	 * Gets the so timeout.
	 *
	 * @return the so timeout
	 * @throws SocketException the socket exception
	 */
	@Override
	public int getSoTimeout() throws SocketException {
		return currentDecoratee.getSoTimeout();
	}

	/**
	 * Gets the tcp no delay.
	 *
	 * @return the tcp no delay
	 * @throws SocketException the socket exception
	 */
	@Override
	public boolean getTcpNoDelay() throws SocketException {
		return currentDecoratee.getTcpNoDelay();
	}

	/**
	 * Gets the traffic class.
	 *
	 * @return the traffic class
	 * @throws SocketException the socket exception
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
	 * @param data the data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void sendUrgentData(final int data) throws IOException {
		currentDecoratee.sendUrgentData(data);
	}

	/**
	 * Sets the keep alive.
	 *
	 * @param on the new keep alive
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setKeepAlive(final boolean on) throws SocketException {
		currentDecoratee.setKeepAlive(on);
	}

	/**
	 * Sets the OOB inline.
	 *
	 * @param on the new OOB inline
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setOOBInline(final boolean on) throws SocketException {
		currentDecoratee.setOOBInline(on);
	}

	/**
	 * Sets the performance preferences.
	 *
	 * @param connectionTime the connection time
	 * @param latency        the latency
	 * @param bandwidth      the bandwidth
	 */
	@Override
	public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
		currentDecoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

	/**
	 * Sets the receive buffer size.
	 *
	 * @param size the new receive buffer size
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setReceiveBufferSize(final int size) throws SocketException {
		currentDecoratee.setReceiveBufferSize(size);
	}

	/**
	 * Sets the reuse address.
	 *
	 * @param on the new reuse address
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setReuseAddress(final boolean on) throws SocketException {
		currentDecoratee.setReuseAddress(on);
	}

	/**
	 * Sets the send buffer size.
	 *
	 * @param size the new send buffer size
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setSendBufferSize(final int size) throws SocketException {
		currentDecoratee.setSendBufferSize(size);
	}

	/**
	 * Sets the so linger.
	 *
	 * @param on     the on
	 * @param linger the linger
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setSoLinger(final boolean on, final int linger) throws SocketException {
		currentDecoratee.setSoLinger(on, linger);
	}

	/**
	 * Sets the so timeout.
	 *
	 * @param timeout the new so timeout
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setSoTimeout(final int timeout) throws SocketException {
		currentDecoratee.setSoTimeout(timeout);
	}

	/**
	 * Sets the tcp no delay.
	 *
	 * @param on the new tcp no delay
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setTcpNoDelay(final boolean on) throws SocketException {
		currentDecoratee.setTcpNoDelay(on);
	}

	/**
	 * Sets the traffic class.
	 *
	 * @param tc the new traffic class
	 * @throws SocketException the socket exception
	 */
	@Override
	public void setTrafficClass(final int tc) throws SocketException {
		currentDecoratee.setTrafficClass(tc);
	}

	/**
	 * Shutdown input.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void shutdownInput() throws IOException {
		currentDecoratee.shutdownInput();
	}

	/**
	 * Shutdown output.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
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
