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
        // Filter on the full local/remote address:port 4-tuple.
        final var localAddress = formatAddressForFilter(socket.getLocalAddress().getHostAddress());
        final var remoteAddress = formatAddressForFilter(socket.getInetAddress().getHostAddress());
        final String[] command = { "ss", "-ntepi", "state", "established", "--inet-sockopt", "-O", "-H", "src "
                + localAddress + ":" + socket.getLocalPort() + " and dst " + remoteAddress + ":" + socket.getPort() };
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
            // Defensive check: with the address:port filter above there should be at most one matching
            // connection (a TCP 4-tuple is unique), but guard against any unexpected duplicate (e.g. a
            // connection lingering in the process of being replaced) by keeping only the first block. Each
            // connection starts a new, non-indented line; any following indented line(s) are extra "-i" details
            // for that same connection.
            var connectionCount = 0;
            var firstBlockEnd = -1;
            final var lines = output.toString().split("\n", -1);
            for (var i = 0; i < lines.length; i++) {
                final var line = lines[i];
                if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) {
                    connectionCount++;
                    if (connectionCount == 2) {
                        firstBlockEnd = i;
                        break;
                    }
                }
            }
            if (connectionCount > 1) {
                _log.warn(
                        "SS output unexpectedly matched more than one connection for {} (local={}:{}, remote={}:{}) - keeping only the first",
                        socket, localAddress, socket.getLocalPort(), remoteAddress, socket.getPort());
                final var trimmed = new StringBuilder();
                for (var i = 0; i < firstBlockEnd; i++) {
                    trimmed.append(lines[i]).append("\n");
                }
                return trimmed.toString();
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
     * Formats an address for use in an "ss" filter expression ({@code src ADDR:PORT} / {@code dst ADDR:PORT}). IPv6
     * addresses must be enclosed in brackets in that syntax, IPv4 addresses are used as-is.
     *
     * @param address
     *            the raw address (as returned by {@link java.net.InetAddress#getHostAddress()})
     *
     * @return the address formatted for the "ss" filter
     */
    private static String formatAddressForFilter(final String address) {
        return address.contains(":") ? "[" + address + "]" : address;
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
