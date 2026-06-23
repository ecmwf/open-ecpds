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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ecmwf.common.database.TransferStatistics;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * Parses the raw string produced by {@link ClientSocketStatistics} into one or more {@link TransferStatistics} records.
 * Multiple entries are separated by {@code |} (one per TCP socket). Each entry has the format:
 *
 * <pre>
 * start:&lt;ms&gt; end:&lt;ms&gt; 0 0 [local]:port [remote]:port ... key:value ...
 * </pre>
 */
public final class SocketStatisticsParser {

    private static final Pattern BPS_SUFFIX = Pattern.compile("(\\d+)bps$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADDR_PATTERN = Pattern.compile("^\\[?[^\\]]*\\]?:\\d+$");

    private SocketStatisticsParser() {
    }

    /**
     * Parse a raw statistics string (as produced by {@link ClientSocketStatistics#toString()}) into a list of
     * {@link TransferStatistics} records, one per TCP connection.
     *
     * @param dataTransferId
     *            the data transfer id to link each record to
     * @param raw
     *            the raw string (may contain multiple {@code |}-separated entries)
     *
     * @return list of parsed records (never null, may be empty)
     */
    public static List<TransferStatistics> parse(final long dataTransferId, final String raw) {
        final var result = new ArrayList<TransferStatistics>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (final var entry : raw.split("\\|")) {
            final var trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                final var ts = parseEntry(dataTransferId, trimmed);
                if (ts != null) {
                    result.add(ts);
                }
            }
        }
        return result;
    }

    /**
     * Parse a single entry (no {@code |} separators).
     */
    private static TransferStatistics parseEntry(final long dataTransferId, final String entry) {
        final var tokens = entry.split("\\s+");
        if (tokens.length < 2) {
            return null;
        }

        final var ts = new TransferStatistics();
        ts.setDataTransferId(dataTransferId);
        ts.setRaw(entry);

        // Scan for address tokens: bare tokens matching [addr]:port pattern (not key:value)
        // They appear after the start:/end: tokens and before the key:value block.
        int addrIndex = 0;

        for (int i = 0; i < tokens.length; i++) {
            final var token = tokens[i];
            if (token.startsWith("start:")) {
                parseLong(token.substring(6), ts::setStartTime);
            } else if (token.startsWith("end:")) {
                parseLong(token.substring(4), ts::setEndTime);
            } else if (ADDR_PATTERN.matcher(token).matches()) {
                // Bare address tokens: first is local, second is remote
                if (addrIndex == 0) {
                    ts.setLocalAddress(stripIPv4Mapped(token));
                } else if (addrIndex == 1) {
                    ts.setRemoteAddress(stripIPv4Mapped(token));
                }
                addrIndex++;
            } else if (token.startsWith("rtt:")) {
                // rtt:7.65/0.014 — take the first (smoothed) value
                final var rttPart = token.substring(4).split("/")[0];
                parseDouble(rttPart, ts::setRttMs);
            } else if (token.startsWith("bytes_sent:")) {
                parseLong(token.substring(11), ts::setBytesSent);
            } else if (token.startsWith("bytes_received:")) {
                parseLong(token.substring(15), ts::setBytesReceived);
            } else if (token.startsWith("pacing_rate:")) {
                parseBps(token.substring(12), ts::setPacingRateBps);
            } else if (token.startsWith("delivery_rate:")) {
                parseBps(token.substring(14), ts::setDeliveryRateBps);
            } else if (token.startsWith("cwnd:")) {
                parseInt(token.substring(5), ts::setCwnd);
            } else if (token.startsWith("segs_out:")) {
                parseInt(token.substring(9), ts::setSegsOut);
            } else if (token.startsWith("segs_in:")) {
                parseInt(token.substring(8), ts::setSegsIn);
            }
        }
        return ts;
    }

    /** Remove IPv4-mapped IPv6 prefix {@code ::ffff:} from addresses. */
    private static String stripIPv4Mapped(final String addr) {
        return addr.replace("[::ffff:", "[").replace("::ffff:", "");
    }

    @FunctionalInterface
    private interface LongSetter {
        void set(long v);
    }

    @FunctionalInterface
    private interface DoubleSetter {
        void set(Double v);
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(Integer v);
    }

    private static void parseLong(final String s, final LongSetter setter) {
        try {
            setter.set(Long.parseLong(s));
        } catch (final NumberFormatException ignored) {
        }
    }

    private static void parseDouble(final String s, final DoubleSetter setter) {
        try {
            setter.set(Double.parseDouble(s));
        } catch (final NumberFormatException ignored) {
        }
    }

    private static void parseInt(final String s, final IntSetter setter) {
        try {
            setter.set(Integer.parseInt(s));
        } catch (final NumberFormatException ignored) {
        }
    }

    private static void parseBps(final String s, final LongSetter setter) {
        final var m = BPS_SUFFIX.matcher(s);
        if (m.matches()) {
            parseLong(m.group(1), setter);
        }
    }
}
