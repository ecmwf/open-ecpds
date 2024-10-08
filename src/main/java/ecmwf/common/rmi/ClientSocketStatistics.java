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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ClientSocketStatistics.
 */
public class ClientSocketStatistics {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ClientSocketStatistics.class);

    /** The elements. */
    private final Map<Socket, StatisticsElement> elements = Collections.synchronizedMap(new HashMap<>());

    /**
     * The Class StatisticsElement.
     */
    public static class StatisticsElement {

        /** The start time. */
        final long startTime;

        /** The end time. */
        final long endTime;

        /** The statistics. */
        final String statistics;

        /**
         * Instantiates a new statistics element.
         *
         * @param startTime
         *            the start time
         * @param endTime
         *            the end time
         * @param statistics
         *            the statistics
         */
        StatisticsElement(final long startTime, final long endTime, final String statistics) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.statistics = statistics.replaceAll("\\s+", " ").replace(" send ", " send:")
                    .replace(" pacing_rate ", " pacing_rate:").replace(" delivery_rate ", " delivery_rate:")
                    .replace(" <-> ", " ").replaceAll("users:\\S+\\s", "").trim();
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "start:" + startTime + " end:" + endTime + " " + statistics;
        }
    }

    /**
     * Adds the.
     *
     * @param socket
     *            the socket
     * @param startTime
     *            the start time
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void add(final Socket socket, final long startTime) throws IOException {
        try {
            if (SocketOptions.isAccessible(socket)) {
                final var ssOutput = SocketOptions.getSSOutput(socket).trim();
                if (!ssOutput.isBlank()) {
                    _log.debug("Socket Statistics: {}", ssOutput);
                    elements.put(socket, new StatisticsElement(startTime, System.currentTimeMillis(), ssOutput));
                } else {
                    _log.warn("Socket Statistics not available for {} (empty output) - (sd={},pid={})", socket,
                            SocketOptions.getSocketDescriptor(socket), ProcessHandle.current().pid());
                }
            } else {
                _log.warn("Socket Statistics not available for {} (not connected/accessible)", socket);
            }
        } catch (final IOException e) {
            _log.warn("Socket Statistics not available for {}", socket, e);
            throw e;
        }
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        final Map<Socket, StatisticsElement> copy;
        synchronized (elements) {
            copy = new HashMap<>(elements);
        }
        return copy.values().stream().map(Object::toString).collect(Collectors.joining("|"));
    }
}
