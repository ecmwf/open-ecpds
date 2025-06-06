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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.CleanableSupport;
import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RemoteInputStreamImp.
 */
public final class RemoteInputStreamImp extends RemoteManagement implements RemoteInputStream, Closeable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4473817650326462267L;

	/** The Constant _log. */
	private static final transient Logger _log = LogManager.getLogger(RemoteInputStreamImp.class);

	/** Cleaner support for resource cleanup. */
	private final transient CleanableSupport cleaner;

	/** The in. */
	private final transient InputStream in;

	/** The i. */
	private transient int i = 0;

	/**
	 * Instantiates a new remote input stream imp.
	 *
	 * @param in the in
	 * @throws RemoteException the remote exception
	 */
	public RemoteInputStreamImp(final InputStream in) throws RemoteException {
		this.in = in;
		// Setup GC cleanup hook
		this.cleaner = new CleanableSupport(this, () -> {
			try {
				cleanup();
			} catch (final IOException e) {
				_log.debug("GC cleanup", e);
			}
		});
	}

	/**
	 * Alive.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean alive() {
		try {
			return i >= 0 && available() >= 0;
		} catch (final IOException _) {
			return false;
		}
	}

	/**
	 * Available.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public int available() throws IOException {
		return in.available();
	}

	/**
	 * Closes this stream and performs all associated cleanup.
	 *
	 * @throws IOException If an error occurs during closing.
	 */
	@Override
	public void close() throws IOException {
		if (cleaner.markCleaned()) {
			cleanup();
		}
	}

	/**
	 * Destroy.
	 */
	@Override
	public void destroy() {
		StreamPlugThread.closeQuietly(this);
	}

	/**
	 * Mark.
	 *
	 * @param readlimit the readlimit
	 * @throws RemoteException the remote exception
	 */
	@Override
	public void mark(final int readlimit) throws RemoteException {
		in.mark(readlimit);
	}

	/**
	 * Mark supported.
	 *
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	@Override
	public boolean markSupported() throws RemoteException {
		return in.markSupported();
	}

	/**
	 * Read.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public int read() throws IOException {
		final var c = i = in.read();
		if (i == -1) {
			close();
		}
		return c;
	}

	/**
	 * Read.
	 *
	 * @param len the len
	 * @return the byte stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public ByteStream read(final int len) throws IOException {
		final var holder = new byte[len];
		i = in.read(holder, 0, len);
		final var bs = new ByteStream(holder, i);
		if (i == -1) {
			close();
		}
		return bs;
	}

	/**
	 * Reset.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void reset() throws IOException {
		in.reset();
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
		return in.skip(n);
	}

	/**
	 * Cleans up resources and terminates the process if necessary.
	 *
	 * @throws IOException If an error occurs during cleanup.
	 */
	private void cleanup() throws IOException {
		in.close();
	}
}
