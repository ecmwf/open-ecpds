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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A FilterOutputStream that monitors client disconnects during RMI operations.
 */
public final class InterruptibleOutputStream extends FilterOutputStream {

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger(InterruptibleOutputStream.class);

	/** The monitor. */
	private final InterruptibleMonitor monitor;

	/** The closed. */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Instantiates a new interruptible output stream.
	 *
	 * @param out the out
	 */
	public InterruptibleOutputStream(final OutputStream out) {
		super(out);
		this.monitor = new InterruptibleMonitor(InterruptibleRMIServerSocket.getCurrentRMIServerThreadSocket());
		this.monitor.execute();
	}

	/**
	 * Write.
	 *
	 * @param b the b
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void write(final int b) throws IOException {
		checkConnection();
		out.write(b);
	}

	/**
	 * Write.
	 *
	 * @param b the b
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
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
		checkConnection();
		out.write(b, off, len);
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void close() throws IOException {
		if (closed.compareAndSet(false, true)) {
			try {
				super.close();
			} finally {
				LOG.debug("Shutting down monitor");
				monitor.setLoop(false);
				monitor.interrupt();
				try {
					monitor.join(5000); // wait max 5 seconds
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					LOG.warn("Interrupted while waiting for monitor thread", e);
				} catch (final Throwable t) {
					LOG.warn("Monitor thread did not terminate cleanly", t);
				}
			}
		} else {
			LOG.debug("Stream already closed");
		}
	}

	/**
	 * Check connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void checkConnection() throws IOException {
		if (monitor.isClosed()) {
			throw new IOException("RMI client disconnected");
		}
	}
}
