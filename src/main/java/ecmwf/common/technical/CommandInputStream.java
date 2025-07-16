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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A stream that connects an InputStream to the input of an external process, and allows reading the output produced by
 * the process.
 *
 * This class manages the lifecycle of the associated process and its input/output streams, and handles stderr
 * asynchronously to avoid potential deadlocks.
 */
public final class CommandInputStream extends FilterInputStream implements AutoCloseable {

    private static final Logger _log = LogManager.getLogger(CommandInputStream.class);

    /** The external process. */
    private final Process process;

    /** The standard output of the process (what we read from). */
    private final InputStream processIn;

    /** The standard input of the process (where we send the user’s input). */
    private final OutputStream processOut;

    /** The standard error of the process. */
    private final InputStream processErr;

    /** Background thread plugging the user input into the process input. */
    private final StreamPlugThread thread;

    /** Executor for handling stderr asynchronously. */
    private final ExecutorService executor;

    /** Cleaner support for resource cleanup. */
    private final CleanableSupport cleaner;

    /**
     * Creates a CommandInputStream that connects the given InputStream to the standard input of a process, and allows
     * reading the standard output of that process.
     *
     * @param in
     *            The InputStream from which to send data to the process's input.
     * @param process
     *            The process to execute and communicate with.
     *
     * @throws IOException
     *             If any I/O error occurs during setup.
     */
    public CommandInputStream(final InputStream in, final Process process) throws IOException {
        super(in);
        _log.debug("Starting");
        this.process = process;
        this.processIn = process.getInputStream();
        this.processOut = process.getOutputStream();
        this.processErr = process.getErrorStream();
        // Pipe the user-supplied input into the process's standard input
        this.thread = new StreamPlugThread(in, processOut);
        thread.toClose(processOut);
        // Start a separate thread to consume stderr (to avoid potential blocking)
        executor = ThreadService.getSingleCleaningThreadLocalExecutorService(true, true);
        executor.submit(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(processErr))) {
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
        // Launch the plug thread
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
     * Creates a CommandInputStream by executing the given command.
     *
     * @param in
     *            The InputStream to send to the process's stdin.
     * @param command
     *            The command to run.
     *
     * @throws IOException
     *             If an error occurs while launching the process.
     */
    public CommandInputStream(final InputStream in, final String[] command) throws IOException {
        this(in, Runtime.getRuntime().exec(command));
    }

    /**
     * Cleans up resources and terminates the process if necessary.
     *
     * @throws IOException
     *             If an error occurs during cleanup.
     */
    private void cleanup() throws IOException {
        _log.debug("Closing");
        String message = null;
        // Close output and error streams safely
        StreamPlugThread.closeQuietly(processOut);
        StreamPlugThread.closeQuietly(processErr);
        // Wait for the plug thread to finish
        try {
            thread.join();
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (final Exception _) {
            // Ignored
        }
        // Shutdown stderr reader
        executor.close();
        // Get any error message from plug thread
        message = thread.getMessage();
        if (message != null) {
            _log.debug("Destroying process ({})", message);
            process.destroy();
        } else {
            // Wait for the process to terminate naturally
            try {
                if (!process.waitFor(15, TimeUnit.MINUTES)) {
                    _log.debug("Process did not terminate in time, forcing destroy");
                    process.destroy();
                }
            } catch (final InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
        // Close input stream and wrapped stream
        try {
            processIn.close();
        } finally {
            in.close();
        }
        // If plug thread reported a problem, propagate it
        if (message != null) {
            throw new IOException(message);
        }
    }

    /**
     * Closes this stream and performs all associated cleanup.
     *
     * @throws IOException
     *             If an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        cleaner.close();
    }

    /**
     * Reads a byte of data from the process's output.
     */
    @Override
    public int read() throws IOException {
        return processIn.read();
    }

    /**
     * Reads bytes into a buffer from the process's output.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return processIn.read(b);
    }

    /**
     * Reads up to len bytes of data from the process's output.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return processIn.read(b, off, len);
    }

    /**
     * Skips over and discards n bytes of data from the process's output.
     */
    @Override
    public long skip(final long n) throws IOException {
        return processIn.skip(n);
    }

    /**
     * Returns the number of bytes that can be read without blocking.
     */
    @Override
    public int available() throws IOException {
        return processIn.available();
    }
}
