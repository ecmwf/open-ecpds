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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-21
 */

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ecmwf.common.database.IncomingConnection;

/**
 * The Class DataPortalActivityRegistry.
 *
 * In-memory, MasterServer-side registry deriving a live "current throughput" figure for the Data Portal (the
 * FTP/HTTP/SFTP/S3/WebDAV interface used directly by IncomingUsers, as opposed to the Dissemination/Acquisition traffic
 * between a DataMover and a Destination Host already tracked by {@link LiveTransferRegistry}). Fed by
 * {@code MasterServer.updateIncomingConnectionIds}, which every DataMover calls roughly every couple of seconds (more
 * often while a connection's counters are actively changing) with its current list of open {@link IncomingConnection}s.
 * <p>
 * Each {@link IncomingConnection} carries cumulative (not incremental) {@code bytesIn}/{@code bytesOut} counters for
 * the lifetime of the connection, so - exactly like {@link LiveTransferRegistry}'s internal byte-delta tracking does
 * for individual DataFile transfers - this class keeps the last known cumulative value per connection (keyed by
 * DataMover name + connection id, since connection ids are only guaranteed unique within a single DataMover) and adds
 * the incremental delta to a short rolling window, whose average rate is exposed via {@link #getBytesInPerSecond()} /
 * {@link #getBytesOutPerSecond()}. A short (few-second) window is used - rather than the 24h buckets used for the
 * transferred-bytes total - since this is meant to feel like a live speedometer, not a daily total.
 * </p>
 */
public final class DataPortalActivityRegistry {

    /** Length of the rolling window (ms) used to compute the current bytes/sec rate. */
    private static final long WINDOW_MILLIS = 5_000;

    /** Singleton instance. */
    private static final DataPortalActivityRegistry _instance = new DataPortalActivityRegistry();

    /**
     * Last known cumulative {@code [bytesIn, bytesOut]} per connection id, per DataMover name. A nested map (rather
     * than a single flat map keyed by "server+id") so an entire DataMover's connections can be cheaply pruned as a unit
     * when it disconnects (see {@link #removeServer(String)}), and so per-connection ids only need to be unique within
     * their own DataMover.
     */
    private final Map<String, Map<String, long[]>> _lastKnownByServer = new ConcurrentHashMap<>();

    /** Bytes-in accumulated in the current rolling window, not yet folded into {@link #_rateInBps}. */
    private final AtomicLong _windowBytesIn = new AtomicLong();

    /** Bytes-out accumulated in the current rolling window, not yet folded into {@link #_rateOutBps}. */
    private final AtomicLong _windowBytesOut = new AtomicLong();

    /** Start time (ms) of the current rolling window. */
    private volatile long _windowStart = System.currentTimeMillis();

    /** Last computed bytes-in/sec rate, updated whenever the window rolls over. */
    private volatile double _rateInBps = 0;

    /** Last computed bytes-out/sec rate, updated whenever the window rolls over. */
    private volatile double _rateOutBps = 0;

    private DataPortalActivityRegistry() {
        // Singleton.
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static DataPortalActivityRegistry getInstance() {
        return _instance;
    }

    /**
     * Records a freshly received list of currently open incoming connections for a single DataMover, deriving the
     * incremental bytes transferred since the last call for each connection and folding it into the current rolling
     * window. Connections no longer present in the list (i.e. closed since the last call) are pruned from the
     * per-DataMover tracking map; any bytes they carried up to their last reported snapshot were already accounted for
     * incrementally as they occurred, so nothing is lost by dropping them now.
     *
     * @param serverName
     *            the DataMover name
     * @param connections
     *            the currently open incoming connections on that DataMover, or {@code null}/empty if none
     */
    public void recordConnections(final String serverName, final List<IncomingConnection> connections) {
        final var previous = _lastKnownByServer.computeIfAbsent(serverName, k -> new ConcurrentHashMap<>());
        final Set<String> currentIds = new HashSet<>();
        var deltaIn = 0L;
        var deltaOut = 0L;
        if (connections != null) {
            for (final IncomingConnection connection : connections) {
                final var id = connection.getId();
                if (id == null) {
                    continue;
                }
                currentIds.add(id);
                final var bytesIn = connection.getBytesIn();
                final var bytesOut = connection.getBytesOut();
                final var last = previous.get(id);
                if (last != null) {
                    final var dIn = bytesIn - last[0];
                    final var dOut = bytesOut - last[1];
                    // Guard against a connection's counters resetting (should not normally happen, but avoids ever
                    // recording a negative delta if it does).
                    if (dIn > 0) {
                        deltaIn += dIn;
                    }
                    if (dOut > 0) {
                        deltaOut += dOut;
                    }
                }
                previous.put(id, new long[] { bytesIn, bytesOut });
            }
        }
        previous.keySet().removeIf(id -> !currentIds.contains(id));
        if (deltaIn > 0) {
            _windowBytesIn.addAndGet(deltaIn);
        }
        if (deltaOut > 0) {
            _windowBytesOut.addAndGet(deltaOut);
        }
        _maybeRollWindow();
    }

    /**
     * Drops all per-connection tracking state for a DataMover that is no longer connected, so it does not linger
     * forever in {@link #_lastKnownByServer}.
     *
     * @param serverName
     *            the DataMover name
     */
    public void removeServer(final String serverName) {
        _lastKnownByServer.remove(serverName);
    }

    /**
     * Rolls the current window over into {@link #_rateInBps}/{@link #_rateOutBps} once {@link #WINDOW_MILLIS} has
     * elapsed since it started, resetting the accumulators for the next window. Safe to call concurrently from multiple
     * DataMovers' heartbeat threads: only one thread will observe a successful compare-and-set of {@link #_windowStart}
     * and perform the roll for any given window.
     */
    private void _maybeRollWindow() {
        final var now = System.currentTimeMillis();
        final var start = _windowStart;
        final var elapsed = now - start;
        if (elapsed < WINDOW_MILLIS) {
            return;
        }
        if (!_windowStartCas(start, now)) {
            // Another thread already rolled this window.
            return;
        }
        final var bytesIn = _windowBytesIn.getAndSet(0);
        final var bytesOut = _windowBytesOut.getAndSet(0);
        final var seconds = Math.max(elapsed / 1000.0, 1.0);
        _rateInBps = bytesIn / seconds;
        _rateOutBps = bytesOut / seconds;
    }

    /**
     * Atomically updates {@link #_windowStart} from {@code expected} to {@code newValue}, mimicking
     * {@code AtomicLong#compareAndSet} without needing a dedicated field type change.
     *
     * @param expected
     *            the expected current value
     * @param newValue
     *            the new value
     *
     * @return {@code true} if the update was applied by this call
     */
    private synchronized boolean _windowStartCas(final long expected, final long newValue) {
        if (_windowStart != expected) {
            return false;
        }
        _windowStart = newValue;
        return true;
    }

    /**
     * Gets the current Data Portal bytes-in/sec rate (uploads from IncomingUsers into OpenECPDS), averaged over the
     * last rolling window (a few seconds).
     *
     * @return the current bytes-in/sec rate
     */
    public double getBytesInPerSecond() {
        return _rateInBps;
    }

    /**
     * Gets the current Data Portal bytes-out/sec rate (downloads by IncomingUsers from OpenECPDS), averaged over the
     * last rolling window (a few seconds).
     *
     * @return the current bytes-out/sec rate
     */
    public double getBytesOutPerSecond() {
        return _rateOutBps;
    }
}
