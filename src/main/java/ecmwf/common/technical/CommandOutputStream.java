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
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class CommandOutputStream.
 */
public final class CommandOutputStream extends FilterOutputStream implements AutoCloseable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CommandOutputStream.class);

    /** The process. */
    private final Process process;

    /** The process in. */
    private final InputStream processIn;

    /** The process out. */
    private final OutputStream processOut;

    /** The process err. */
    private final InputStream processErr;

    /** The thread. */
    private final StreamPlugThread thread;

    /** The executor. */
    private final ExecutorService executor;

    /** The cleaner. */
    private final CleanableSupport cleaner;

    /**
     * Instantiates a new command output stream.
     *
     * @param out
     *            the out
     * @param process
     *            the process
     */
    public CommandOutputStream(final OutputStream out, final Process process) throws IOException {
        super(out);
        _log.debug("Starting");
        this.process = process;
        processOut = process.getOutputStream();
        processIn = process.getInputStream();
        processErr = process.getErrorStream();
        thread = new StreamPlugThread(processIn, out);
        thread.toClose(processOut);
        // Start a thread to consume stderr to prevent blocking the closure of the
        // process
        executor = ThreadService.getSingleCleaningThreadLocalExecutorService();
        executor.submit(() -> {
            try (final var reader = new BufferedReader(new InputStreamReader(processErr))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    _log.warn("Process stderr: {}", line);
                }
            } catch (final IOException e) {
                _log.error("Error reading stderr", e);
            }
        });
        // Setup GC cleanup hook
        this.cleaner = new CleanableSupport(this, this::cleanup);
        try {
            thread.execute();
        } catch (final Exception e) {
            try {
                cleanup();
            } catch (final IOException io) {
                e.addSuppressed(io);
            }
            throw e instanceof final IOException ioe ? ioe : new IOException("Failed to start thread", e);
        }
    }

    /**
     * Instantiates a new command output stream.
     *
     * @param os
     *            the os
     * @param command
     *            the command
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public CommandOutputStream(final OutputStream os, final String[] command) throws IOException {
        this(os, Runtime.getRuntime().exec(command));
    }

    /**
     * Cleanup.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void cleanup() throws IOException {
        _log.debug("Closing");
        String message = null;
        StreamPlugThread.closeQuietly(processOut);
        StreamPlugThread.closeQuietly(processErr);
        try {
            thread.join();
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
            // Ignored
        } catch (final Exception _) {
            // Ignored
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                _log.warn("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (final InterruptedException _) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        message = thread.getMessage();
        if (message != null) {
            _log.debug("Destroying process ({})", message);
            process.destroy();
        } else {
            try {
                if (!process.waitFor(15, TimeUnit.MINUTES)) {
                    _log.debug("Process did not terminate in time, forcing destroy ({})", message);
                    process.destroy();
                }
            } catch (final InterruptedException _) {
                Thread.currentThread().interrupt();
                // Ignored
            }
        }
        try {
            processIn.close();
        } finally {
            out.close();
        }
        if (message != null) {
            throw new IOException(message);
        }
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        cleaner.close();
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        processOut.flush();
        thread.flush();
        out.flush();
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
        try {
            processOut.write(b);
        } catch (final IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
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
        try {
            processOut.write(b, off, len);
        } catch (final IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
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
        try {
            processOut.write(b);
        } catch (final IOException e) {
            _log.error("Write failed, closing process", e);
            close();
            throw e;
        }
    }
}