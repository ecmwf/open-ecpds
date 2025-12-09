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

package ecmwf.common.rmi;

/**
 * ECMWF Product Data Store (ECPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class SocketOptions.
 */
class SocketOptions {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SocketOptions.class);

    /** The loaded. */
    private static boolean loaded = false;

    /** SS available. */
    private static boolean ssAvailable = isCommandAvailable("ss");

    static {
        final var libraryFile = Path.of(System.getProperty("java.jni.path", "") + "/libsocketoptions.so").toFile();
        final var absolutePath = libraryFile.getAbsolutePath();
        if (libraryFile.exists() && libraryFile.canRead()) {
            try {
                System.load(absolutePath);
                loaded = true;
            } catch (final UnsatisfiedLinkError e) {
                _log.warn("Failed to load native library: {}", absolutePath, e);
            }
        } else {
            _log.warn("Failed to load native library: {} (exists={},canRead={})", absolutePath, libraryFile.exists(),
                    libraryFile.canRead());
        }
    }

    /**
     * Instantiates a new socket options.
     */
    private SocketOptions() {
        // Hiding constructor!
    }

    /**
     * Checks if is accessible.
     *
     * @param socket
     *            the socket
     *
     * @return true, if is accessible
     */
    static boolean isAccessible(final Socket socket) {
        return loaded && socket.isConnected();
    }

    /**
     * Sets the TCP congestion.
     *
     * @param socket
     *            the socket
     * @param algorithm
     *            the algorithm
     *
     * @return the int
     */
    static native int setTCPCongestion(final Socket socket, final String algorithm);

    /**
     * Sets the SO max pacing rate.
     *
     * @param socket
     *            the socket
     * @param pacingRate
     *            the pacing rate
     *
     * @return the int
     */
    static native int setSOMaxPacingRate(final Socket socket, final int pacingRate);

    /**
     * Sets the TCP max segment.
     *
     * @param socket
     *            the socket
     * @param maxSegmentSize
     *            the max segment size
     *
     * @return the int
     */
    static native int setTCPMaxSegment(final Socket socket, final int maxSegmentSize);

    /**
     * Sets the TCP time stamp.
     *
     * @param socket
     *            the socket
     * @param enable
     *            the enable
     *
     * @return the int
     */
    static native int setTCPTimeStamp(final Socket socket, final boolean enable);

    /**
     * Sets the TCP window clamp.
     *
     * @param socket
     *            the socket
     * @param windowSize
     *            the window size
     *
     * @return the int
     */
    static native int setTCPWindowClamp(final Socket socket, final int windowSize);

    /**
     * Sets the TCP keep alive time.
     *
     * @param socket
     *            the socket
     * @param keepAliveTime
     *            the keep alive time
     *
     * @return the int
     */
    static native int setTCPKeepAliveTime(final Socket socket, final int keepAliveTime);

    /**
     * Sets the TCP keep alive interval.
     *
     * @param socket
     *            the socket
     * @param keepAliveInterval
     *            the keep alive interval
     *
     * @return the int
     */
    static native int setTCPKeepAliveInterval(final Socket socket, final int keepAliveInterval);

    /**
     * Sets the TCP keep alive probes.
     *
     * @param socket
     *            the socket
     * @param keepAliveProbes
     *            the keep alive probes
     *
     * @return the int
     */
    static native int setTCPKeepAliveProbes(final Socket socket, final int keepAliveProbes);

    /**
     * Sets the TCP linger.
     *
     * @param socket
     *            the socket
     * @param enable
     *            the enable
     * @param lingerTime
     *            the linger time
     *
     * @return the int
     */
    static native int setTCPLinger(final Socket socket, boolean enable, final int lingerTime);

    /**
     * Sets the TCP user timeout.
     *
     * @param socket
     *            the socket
     * @param userTimeout
     *            the user timeout
     *
     * @return the int
     */
    static native int setTCPUserTimeout(final Socket socket, final int userTimeout);

    /**
     * Sets the TCP quick ack.
     *
     * @param socket
     *            the socket
     * @param enable
     *            the enable
     *
     * @return the int
     */
    static native int setTCPQuickAck(final Socket socket, final boolean enable);

    /**
     * Gets the socket descriptor.
     *
     * @param socket
     *            the socket
     *
     * @return the socket descriptor
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    static native int getSocketDescriptor(final Socket socket) throws IOException;

    /**
     * Gets the SS output.
     *
     * @param socket
     *            the socket
     *
     * @return the SS output
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    static String getSSOutput(final Socket socket) throws IOException {
        if (!ssAvailable) {
            return "exception:ss-not-found";
        }
        final String[] command = { "ss","-ntepi","state","established","--inet-sockopt","-O","-H","sport = "
                + socket.getLocalPort() + " and dport = " + socket.getPort() };
        final var pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // Merge error with output stream
        Process process = null;
        try {
            process = pb.start();
            final var output = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            final var finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                process.waitFor(2, TimeUnit.SECONDS);
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                return "exception:timeout";
            }
            return output.toString();
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
            _log.warn("Interrupted while waiting for SS command");
            return "exception:interrupted";
        } finally {
            if (process != null) {
                StreamPlugThread.closeQuietly(process.getInputStream());
                StreamPlugThread.closeQuietly(process.getErrorStream());
                StreamPlugThread.closeQuietly(process.getOutputStream());
            }
        }
    }

    /**
     * Checks if is command available.
     *
     * @param command
     *            the command
     *
     * @return true, if is command available
     */
    private static boolean isCommandAvailable(final String command) {
        final var os = System.getProperty("os.name").toLowerCase();
        final var checkCmd = os.startsWith("win") ? "where" : "which";
        final var pb = new ProcessBuilder(checkCmd, command);
        pb.redirectErrorStream(true);
        try {
            final var process = pb.start();
            // Consume all output to avoid blocking (optional but recommended)
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // Just drain output, content not used here
                }
            }
            final var finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (final IOException _) {
            // Ignore
        }
        return false;
    }
}
