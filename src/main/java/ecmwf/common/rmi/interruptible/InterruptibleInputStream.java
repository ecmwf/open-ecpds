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
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A FilterInputStream that monitors RMI client socket disconnection.
 */
public final class InterruptibleInputStream extends FilterInputStream {

    /** The Constant LOG. */
    private static final Logger LOG = LogManager.getLogger(InterruptibleInputStream.class);

    /** The monitor. */
    private final InterruptibleMonitor monitor;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Instantiates a new interruptible input stream.
     *
     * @param in
     *            the in
     */
    public InterruptibleInputStream(final InputStream in) {
        this(in, InterruptibleRMIServerSocket.getCurrentRMIServerThreadSocket());
    }

    /**
     * Instantiates a new interruptible input stream.
     *
     * @param in
     *            the in
     * @param socket
     *            the socket
     */
    public InterruptibleInputStream(final InputStream in, final Socket socket) {
        super(in);
        if (socket != null) {
            monitor = new InterruptibleMonitor(socket);
            monitor.execute();
        } else {
            monitor = null;
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
        checkConnection();
        return super.read();
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
        return read(b, 0, b.length);
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
        checkConnection();
        return super.read(b, off, len);
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
                if (monitor != null) {
                    LOG.debug("Interrupting RMI monitor thread");
                    monitor.setLoop(false);
                    monitor.interrupt();
                    try {
                        monitor.join(5000); // wait max 5s
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOG.warn("Interrupted while joining RMI monitor thread", e);
                    } catch (final Throwable t) {
                        LOG.warn("RMI monitor thread did not terminate cleanly", t);
                    }
                }
            }
        } else {
            LOG.debug("Stream already closed");
        }
    }

    /**
     * Check connection.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void checkConnection() throws IOException {
        if (monitor != null && monitor.isClosed()) {
            throw new IOException("RMI client connection lost unexpectedly");
        }
    }
}
