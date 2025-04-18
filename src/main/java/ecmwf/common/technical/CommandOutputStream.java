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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class CommandOutputStream.
 */
public final class CommandOutputStream extends FilterOutputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CommandOutputStream.class);

    /** The process. */
    private final Process _process;

    /** The in. */
    private final InputStream _in;

    /** The out. */
    private final OutputStream _out;

    /** The err. */
    private final InputStream _err;

    /** The thread. */
    private final StreamPlugThread _thread;

    /** The closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The executor. */
    private final ExecutorService _executor = Executors.newSingleThreadExecutor();

    /**
     * Instantiates a new command output stream.
     *
     * @param out
     *            the out
     * @param process
     *            the process
     */
    public CommandOutputStream(final OutputStream out, final Process process) {
        super(out);
        _log.debug("Starting");
        _process = process;
        _out = process.getOutputStream();
        _in = process.getInputStream();
        _err = process.getErrorStream();
        _thread = new StreamPlugThread(_in, out);
        _thread.toClose(_out);
        _thread.execute();
        // Start a thread to consume stderr to prevent blocking
        _executor.submit(() -> {
            try (final var reader = new BufferedReader(new InputStreamReader(_err))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    _log.warn("Process stderr: {}", line);
                }
            } catch (IOException e) {
                _log.error("Error reading stderr", e);
            }
        });
    }

    /**
     * Instantiates a new command output stream.
     *
     * @param os
     *            the os
     * @param command
     *            the command
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public CommandOutputStream(final OutputStream os, final String[] command) throws IOException {
        this(os, Runtime.getRuntime().exec(command));
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            _log.debug("Closing");
            String message = null;
            try {
                StreamPlugThread.closeQuietly(_out);
                StreamPlugThread.closeQuietly(_err);
                try {
                    _thread.join();
                } catch (final Exception ignored) {
                    // Ignored
                }
                _executor.shutdown();
                message = _thread.getMessage();
                if (message != null) {
                    _log.debug("Destroying process ({})", message);
                    _process.destroy();
                } else {
                    try {
                        if (!_process.waitFor(15, TimeUnit.MINUTES)) {
                            _log.debug("Destroying process ({})", message);
                            _process.destroy();
                        }
                    } catch (final InterruptedException ignored) {
                        // Ignored
                    }
                }
                try {
                    _in.close();
                } finally {
                    out.close();
                }
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
     * {@inheritDoc}
     *
     * Flush.
     */
    @Override
    public void flush() throws IOException {
        _out.flush();
        _thread.flush();
        out.flush();
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        try {
            _out.write(b);
        } catch (IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        try {
            _out.write(b, off, len);
        } catch (IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        try {
            _out.write(b);
        } catch (IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Finalize.
     */
    @Override
    protected void finalize() throws Throwable {
        if (_closed.compareAndSet(false, true)) {
            _log.warn("Forcing close in finalize <- {}", this.getClass().getName());
            try {
                _process.destroy();
            } finally {
                StreamPlugThread.closeQuietly(_out);
                StreamPlugThread.closeQuietly(_in);
                StreamPlugThread.closeQuietly(out);
            }
        }
        super.finalize();
    }
}
