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
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class LiveTransferRegistry.
 *
 * In-memory, MasterServer-side registry of the latest {@link LiveTransferSample} received from every DataMover. Acts as
 * the single "source of truth" for the "Live ECPDS Earth" globe visualisation: DataMovers push samples in here (via
 * {@link ecmwf.ecpds.master.MasterInterface#updateLiveTransferStatistics(LiveTransferSample[])}), and any number of
 * listeners (e.g. a WebSocket broadcaster, added in a later phase) can subscribe to be notified in real-time, and/or
 * poll {@link #getVisualizationSamples()} for active movements and recent terminal events.
 *
 * Visualization state is bounded and ephemeral. Terminal events are exposed for 15 seconds and retained internally for
 * 60 seconds to suppress delayed active replays. Authoritative byte accounting is separate in
 * {@link TrafficAccounting}; the legacy bucket helpers below are not used by the globe KPI.
 */
public final class LiveTransferRegistry {

    private static final Logger _log = LogManager.getLogger(LiveTransferRegistry.class);

    /** How long an ACTIVE sample is kept without a refresh before it is considered stale and dropped. */
    private static final long STALE_AFTER_MS = 60 * 1000;

    /**
     * How long the registry stays "enabled" (see {@link #isEnabled()}) after the last remote poll via
     * {@link ecmwf.ecpds.master.ManagementInterface#getLiveTransfers()}. The Monitor plugin's WebSocket broadcaster (a
     * separate JVM) polls this RMI method every few seconds while at least one globe page is open; this window simply
     * needs to be comfortably longer than that polling period so DataMovers keep sampling between polls, while still
     * shutting sampling off promptly once every Monitor stops polling (e.g. last globe page closed).
     */
    private static final long POLL_ENABLE_WINDOW_MS = 15 * 1000;

    /**
     * Number of one-minute buckets used to maintain a rolling 24h transferred-bytes total
     * ({@link #_bucketBytesDiss}/{@link #_bucketBytesAcq}).
     */
    private static final int BUCKET_COUNT = 24 * 60;

    private static final List<String> HOST_TYPES = List.of("Dissemination", "Acquisition", "Replication", "Source",
            "Backup", "Proxy");

    /** Singleton instance. */
    private static final LiveTransferRegistry _instance = new LiveTransferRegistry();

    /** Latest known sample per transfer id. */
    private final LiveTransferEvents _events = new LiveTransferEvents(STALE_AFTER_MS, STALE_AFTER_MS, 20_000);

    /** Listeners notified whenever a sample is added/updated/removed. */
    private final CopyOnWriteArrayList<Consumer<LiveTransferSample>> _listeners = new CopyOnWriteArrayList<>();

    /** Whether at least one consumer (e.g. a globe WebSocket session) is currently interested. */
    private final AtomicBoolean _enabled = new AtomicBoolean(false);

    /** Timestamp (ms) of the last {@link #touch()} call, i.e. the last remote poll from a Monitor plugin. */
    private volatile long _lastPolledAt = 0;

    /**
     * Last known cumulative {@link LiveTransferSample#getByteSent()} per transfer id, used to derive per-sample byte
     * deltas (each sample carries a cumulative total, not an incremental one) for the rolling 24h totals below.
     */
    private final Map<Long, Long> _lastKnownBytes = new ConcurrentHashMap<>();

    /**
     * Dissemination bytes transferred per one-minute bucket, indexed by {@code (epochMinute % BUCKET_COUNT)}; a bucket
     * is reset to zero the first time it is reused for a new minute, giving an always-accurate rolling 24h window
     * without ever growing unbounded. Kept separate from {@link #_bucketBytesAcq} so the "Transferred (24h)" KPI can be
     * filtered by direction, matching whichever of Dissemination/Acquisition/Both is currently selected on the globe
     * UI.
     */
    private final AtomicLongArray _bucketBytesDiss = new AtomicLongArray(BUCKET_COUNT);

    /** Acquisition bytes transferred per one-minute bucket; see {@link #_bucketBytesDiss}. */
    private final AtomicLongArray _bucketBytesAcq = new AtomicLongArray(BUCKET_COUNT);

    private final Map<String, AtomicLongArray> _typeBuckets = HOST_TYPES.stream()
            .collect(java.util.stream.Collectors.toMap(type -> type, type -> new AtomicLongArray(BUCKET_COUNT)));

    /**
     * The epoch-minute each bucket in {@link #_bucketBytesDiss}/{@link #_bucketBytesAcq} currently holds data for;
     * {@code -1} means "never used". Shared between both directions since they are always advanced together (one bucket
     * per minute, regardless of direction).
     */
    private final AtomicLongArray _bucketMinute = new AtomicLongArray(BUCKET_COUNT);

    {
        for (var i = 0; i < BUCKET_COUNT; i++) {
            _bucketMinute.set(i, -1);
        }
    }

    private LiveTransferRegistry() {
        // Singleton.
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static LiveTransferRegistry getInstance() {
        return _instance;
    }

    /**
     * Update the registry with a freshly received batch of samples from a DataMover, and notify listeners.
     *
     * @param samples
     *            the samples
     */
    public void update(final LiveTransferSample[] samples) {
        if (samples == null) {
            return;
        }
        for (final LiveTransferSample sample : samples) {
            if (sample == null) {
                continue;
            }
            if (!_events.offer(sample))
                continue;
            for (final Consumer<LiveTransferSample> listener : _listeners) {
                try {
                    listener.accept(sample);
                } catch (final Throwable t) {
                    _log.warn("Notifying live transfer listener", t);
                }
            }
        }
    }

    /**
     * Gets the total number of bytes transferred (across every Mover/ProxyHost) over the last rolling 24 hours. This is
     * a single, MasterServer-side, always-on counter (unlike the per-connection "session" figure a globe client used to
     * keep locally), so every open globe page - and every Monitor plugin instance - sees the exact same value, and it
     * survives page reloads/reconnects. The underlying per-minute buckets are also periodically persisted to the
     * {@code SYS_CONFIG} table (see
     * {@link #snapshotBucketsForPersistence()}/{@link #restoreBucketsFromPersistence(String)}), so a normal
     * MasterServer restart only loses at most a few minutes of history instead of the full 24 hours.
     *
     * @return the total bytes transferred in the last 24 hours
     */
    public long getBytesLast24h() {
        return getBytesLast24h(LiveTransferSample.DIRECTION_DISSEMINATION)
                + getBytesLast24h(LiveTransferSample.DIRECTION_ACQUISITION);
    }

    /**
     * Gets the total number of bytes transferred (across every Mover/ProxyHost) over the last rolling 24 hours, for a
     * single direction. Same semantics/persistence as {@link #getBytesLast24h()}, just filtered to one of
     * {@link LiveTransferSample#DIRECTION_DISSEMINATION} or {@link LiveTransferSample#DIRECTION_ACQUISITION}, so the
     * globe UI's "Transferred (24h)" KPI can match whichever of Dissemination/Acquisition/Both is currently selected.
     *
     * @param direction
     *            one of {@link LiveTransferSample#DIRECTION_DISSEMINATION} or
     *            {@link LiveTransferSample#DIRECTION_ACQUISITION}
     *
     * @return the total bytes transferred in the last 24 hours for that direction
     */
    public long getBytesLast24h(final String direction) {
        final var buckets = _typeBuckets.containsKey(direction) ? _typeBuckets.get(direction) : _bucketsFor(direction);
        final var nowMinute = System.currentTimeMillis() / 60_000;
        final var oldestMinute = nowMinute - BUCKET_COUNT + 1;
        var total = 0L;
        for (var i = 0; i < BUCKET_COUNT; i++) {
            final var bucketMinute = _bucketMinute.get(i);
            if (bucketMinute >= oldestMinute && bucketMinute <= nowMinute) {
                total += buckets.get(i);
            }
        }
        return total;
    }

    public Map<String, Long> getBytesLast24hByType() {
        final Map<String, Long> totals = new java.util.LinkedHashMap<>();
        for (final var type : HOST_TYPES) {
            totals.put(type, getBytesLast24h(type));
        }
        return totals;
    }

    /**
     * Gets the per-minute bucket array to use for the given direction.
     *
     * @param direction
     *            one of {@link LiveTransferSample#DIRECTION_DISSEMINATION} or
     *            {@link LiveTransferSample#DIRECTION_ACQUISITION}; anything else defaults to Dissemination
     *
     * @return the matching bucket array
     */
    private AtomicLongArray _bucketsFor(final String direction) {
        return LiveTransferSample.DIRECTION_ACQUISITION.equals(direction) ? _bucketBytesAcq : _bucketBytesDiss;
    }

    /**
     * Serializes every currently non-empty, non-stale (i.e. within the last 24h) per-minute bucket into a compact
     * {@code minute:dissBytes:acqBytes} triples string, suitable for storage in a single {@code SYS_CONFIG} row. Meant
     * to be called periodically (e.g. every few minutes) by the MasterServer, so a restart only loses the handful of
     * minutes since the last save rather than the full rolling 24h window.
     *
     * @return the serialized snapshot, or an empty string if there is nothing (yet) to persist
     */
    public String snapshotBucketsForPersistence() {
        final var nowMinute = System.currentTimeMillis() / 60_000;
        final var oldestMinute = nowMinute - BUCKET_COUNT + 1;
        final var sb = new StringBuilder();
        for (var i = 0; i < BUCKET_COUNT; i++) {
            final var bucketMinute = _bucketMinute.get(i);
            final var dissBytes = _bucketBytesDiss.get(i);
            final var acqBytes = _bucketBytesAcq.get(i);
            if (bucketMinute >= oldestMinute && bucketMinute <= nowMinute && (dissBytes > 0 || acqBytes > 0)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(bucketMinute).append(':').append(dissBytes).append(':').append(acqBytes);
            }
        }
        return sb.toString();
    }

    /**
     * Repopulates the per-minute buckets from a string previously produced by {@link #snapshotBucketsForPersistence()},
     * e.g. on MasterServer startup, so the rolling 24h total does not simply reset to zero after a restart. Entries for
     * minutes already outside the current rolling 24h window (i.e. the MasterServer was down for a while) are silently
     * skipped, exactly as they would eventually age out of the live buckets anyway. Accepts both the current
     * {@code minute:dissBytes:acqBytes} format and the legacy {@code minute:bytes} format (from before the
     * Dissemination/Acquisition split), treating a legacy entry's single value as Dissemination bytes so upgrading a
     * running MasterServer does not lose its rolling 24h total.
     *
     * @param data
     *            the previously persisted snapshot, as produced by {@link #snapshotBucketsForPersistence()}
     */
    public void restoreBucketsFromPersistence(final String data) {
        if (data == null || data.isBlank()) {
            return;
        }
        final var nowMinute = System.currentTimeMillis() / 60_000;
        final var oldestMinute = nowMinute - BUCKET_COUNT + 1;
        var restored = 0;
        for (final String entry : data.split(",")) {
            final var fields = entry.split(":");
            if (fields.length != 2 && fields.length != 3) {
                _log.warn("Skipping malformed live transfer bytes bucket entry: {}", entry);
                continue;
            }
            try {
                final var minute = Long.parseLong(fields[0]);
                final var dissBytes = Long.parseLong(fields[1]);
                final var acqBytes = fields.length >= 3 ? Long.parseLong(fields[2]) : 0L;
                if (minute < oldestMinute || minute > nowMinute || (dissBytes <= 0 && acqBytes <= 0)) {
                    continue; // stale (outside the rolling window) or corrupt - skip.
                }
                final var index = (int) (minute % BUCKET_COUNT);
                synchronized (_bucketBytesDiss) {
                    _bucketMinute.set(index, minute);
                    _bucketBytesDiss.set(index, Math.max(dissBytes, 0));
                    _bucketBytesAcq.set(index, Math.max(acqBytes, 0));
                }
                restored++;
            } catch (final NumberFormatException e) {
                _log.warn("Skipping malformed live transfer bytes bucket entry: {}", entry);
            }
        }
        if (restored > 0) {
            _log.info("Restored {} live transfer bytes bucket(s) from persisted state", restored);
        }
    }

    /** Serializes each Host type separately, keeping snapshots within the SYS_CONFIG text column size. */
    public Map<String, String> snapshotTypeBucketsForPersistence() {
        final Map<String, String> snapshots = new java.util.LinkedHashMap<>();
        final var nowMinute = System.currentTimeMillis() / 60_000;
        synchronized (_bucketBytesDiss) {
            for (final var type : HOST_TYPES) {
                final var sb = new StringBuilder();
                for (var i = 0; i < BUCKET_COUNT; i++) {
                    final var minute = _bucketMinute.get(i);
                    final var bytes = _typeBuckets.get(type).get(i);
                    if (minute > nowMinute - BUCKET_COUNT && minute <= nowMinute && bytes > 0) {
                        if (sb.length() > 0)
                            sb.append(',');
                        sb.append(minute).append(':').append(bytes);
                    }
                }
                snapshots.put(type, sb.toString());
            }
        }
        return snapshots;
    }

    public void restoreTypeBucketsFromPersistence(final String type, final String data) {
        if (data == null || data.isBlank())
            return;
        final var buckets = _typeBuckets.get(type);
        if (buckets == null)
            throw new IllegalArgumentException("Unknown traffic type: " + type);
        final var nowMinute = System.currentTimeMillis() / 60_000;
        for (final var entry : data.split(",")) {
            try {
                final var fields = entry.split(":");
                if (fields.length != 2)
                    throw new NumberFormatException("Expected minute:bytes");
                final var minute = Long.parseLong(fields[0]);
                final var bytes = Long.parseLong(fields[1]);
                if (minute <= nowMinute - BUCKET_COUNT || minute > nowMinute || bytes <= 0)
                    continue;
                synchronized (_bucketBytesDiss) {
                    final var index = (int) (minute % BUCKET_COUNT);
                    _bucketMinute.set(index, minute);
                    buckets.set(index, bytes);
                }
            } catch (final NumberFormatException e) {
                _log.warn("Skipping malformed {} traffic bucket: {}", type, entry, e);
            }
        }
    }

    /** Adds cumulative sample deltas to both direction and Host-type minute buckets. */
    private void _recordBytes(final LiveTransferSample sample) {
        final var previous = _lastKnownBytes.getOrDefault(sample.getTransferId(), 0L);
        final var current = sample.getByteSent();
        if (!sample.isTerminal()) {
            _lastKnownBytes.put(sample.getTransferId(), current);
        }
        // Guard against a transfer restarting from zero after a retry (which would otherwise look like a negative
        // delta), and against not-yet-known byte counts.
        final var delta = current - previous;
        if (delta <= 0) {
            return;
        }
        final var buckets = _bucketsFor(sample.getDirection());
        final var minute = System.currentTimeMillis() / 60_000;
        final var index = (int) (minute % BUCKET_COUNT);
        // Reset both direction buckets the first time this slot is (re)used for this minute, so stale data from
        // ~24h ago is dropped. Synchronized (rather than a bare getAndSet) to avoid a race where a second thread's
        // concurrent addAndGet below could land in between this reset's getAndSet and its set(0), and be wiped out
        // by the latter.
        synchronized (_bucketBytesDiss) {
            if (_bucketMinute.getAndSet(index, minute) != minute) {
                _bucketBytesDiss.set(index, 0);
                _bucketBytesAcq.set(index, 0);
                for (final var typeBucket : _typeBuckets.values()) {
                    typeBucket.set(index, 0);
                }
            }
            buckets.addAndGet(index, delta);
            final var reportedType = sample.getHostType();
            final var type = _typeBuckets.containsKey(reportedType) ? reportedType
                    : LiveTransferSample.DIRECTION_ACQUISITION.equals(sample.getDirection()) ? "Acquisition"
                            : "Dissemination";
            _typeBuckets.get(type).addAndGet(index, delta);
        }
    }

    /**
     * Gets a snapshot of every currently active transfer known to the registry.
     *
     * @return the active transfers
     */
    public Collection<LiveTransferSample> getActiveTransfers() {
        return _events.snapshot().stream().filter(sample -> !sample.isTerminal()).toList();
    }

    /** Active movements plus short-lived terminal events, retained across independent Monitor polls. */
    public Collection<LiveTransferSample> getVisualizationSamples() {
        final var now = System.currentTimeMillis();
        return _events.snapshot().stream()
                .filter(sample -> !sample.isTerminal() || now - sample.getTimestamp() < 15_000).toList();
    }

    /**
     * Register a listener notified for every sample update (active or terminal).
     *
     * @param listener
     *            the listener
     */
    public void addListener(final Consumer<LiveTransferSample> listener) {
        _listeners.add(listener);
    }

    /**
     * Remove a previously registered listener.
     *
     * @param listener
     *            the listener
     */
    public void removeListener(final Consumer<LiveTransferSample> listener) {
        _listeners.remove(listener);
    }

    /**
     * Whether the registry currently has at least one interested listener (e.g. an open globe WebSocket). Used by
     * DataMovers to decide whether it is worth sampling/pushing live statistics at all.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled() {
        return _enabled.get() || !_listeners.isEmpty()
                || System.currentTimeMillis() - _lastPolledAt < POLL_ENABLE_WINDOW_MS;
    }

    /**
     * Records that a remote consumer (the Monitor plugin's globe WebSocket broadcaster, polling via
     * {@link ecmwf.ecpds.master.ManagementInterface#getLiveTransfers()}) is actively interested right now. Used to
     * extend {@link #isEnabled()} for {@link #POLL_ENABLE_WINDOW_MS} beyond each poll.
     */
    public void touch() {
        _lastPolledAt = System.currentTimeMillis();
    }

    /**
     * Force-enable the registry (e.g. via a configuration flag), regardless of listener count.
     *
     * @param enabled
     *            the enabled
     */
    public void setEnabled(final boolean enabled) {
        _enabled.set(enabled);
    }

}
