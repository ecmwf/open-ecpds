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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class CommandInputStream.
 */
public final class CommandInputStream extends FilterInputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CommandInputStream.class);

    /** The process. */
    private final Process _process;

    /** The in. */
    private final InputStream _in;

    /** The out. */
    private final OutputStream _out;

    /** The thread. */
    private final StreamPlugThread _thread;

    /** The closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /**
     * Instantiates a new command input stream.
     *
     * @param in
     *            the in
     * @param process
     *            the process
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public CommandInputStream(final InputStream in, final Process process) throws IOException {
        super(in);
        _log.debug("Starting");
        _process = process;
        _out = process.getOutputStream();
        _in = process.getInputStream();
        _thread = new StreamPlugThread(in, _out);
        _thread.toClose(_out);
        _thread.execute();
    }

    /**
     * Instantiates a new command input stream.
     *
     * @param in
     *            the in
     * @param cmd
     *            the cmd
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public CommandInputStream(final InputStream in, final String[] cmd) throws IOException {
        this(in, Runtime.getRuntime().exec(cmd));
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            _log.debug("Closing");
            final var message = _thread.getMessage();
            if (message != null) {
                _log.debug("Destroying process ({})", message);
                _process.destroy();
            }
            try {
                _out.close();
                try {
                    _process.waitFor();
                } catch (final InterruptedException Ie) {
                }
                try {
                    _thread.join();
                } catch (final Exception Ie) {
                }
                _in.close();
                in.close();
            } finally {
                if (message != null) {
                    throw new IOException(message);
                }
            }
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void flush() throws IOException {
        _thread.flush();
    }

    /**
     * Available.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int available() throws IOException {
        return _in.available();
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
        return _in.read();
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
        return _in.read(b);
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
        return _in.read(b, off, len);
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
        return _in.skip(n);
    }

    /**
     * Mark.
     *
     * @param readlimit
     *            the readlimit
     */
    @Override
    public void mark(final int readlimit) {
    }

    /**
     * Mark supported.
     *
     * @return true, if successful
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Reset.
     */
    @Override
    public void reset() {
    }

    /**
     * Finalize.
     *
     * @throws Throwable
     *             the throwable
     */
    @Override
    public void finalize() throws Throwable {
        if (_closed.compareAndSet(false, true)) {
            _log.warn("Forcing close in finalize <- {}", this.getClass().getName());
            try {
                _process.destroy();
            } finally {
                StreamPlugThread.closeQuietly(_out);
                StreamPlugThread.closeQuietly(_in);
                StreamPlugThread.closeQuietly(in);
            }
        }
        super.finalize();
    }
}
