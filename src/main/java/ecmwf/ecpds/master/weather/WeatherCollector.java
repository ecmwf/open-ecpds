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

package ecmwf.ecpds.master.weather;

import static ecmwf.ecpds.master.weather.WeatherReceipt.Disposition.*;

import java.io.Serializable;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ecmwf.ecpds.master.weather.WeatherObservation.Connection;
import ecmwf.ecpds.master.weather.WeatherObservation.Kind;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;
import ecmwf.ecpds.master.weather.WeatherReceipt.Disposition;

/**
 * Bounded, volatile aggregation. Transport must authenticate source identity and enforce encoded batch limits before
 * calling this collector.
 */
public final class WeatherCollector {
    private static final long RETENTION_MS = 60 * 60 * 1000L;
    private static final long MAX_QUEUE_AGE_MS = 5 * 60 * 1000L;
    private static final long MAX_CLOCK_OFFSET_MS = 30_000;
    private static final int MAX_EVENTS = 10_000;
    private static final int MAX_CONNECTIONS = 10_000;
    private final UUID epoch = UUID.randomUUID();
    private final Clock clock;
    private WeatherSelection selection;
    private final Map<UUID, Seen> events = new LinkedHashMap<>();
    private final Map<ExecutionKey, Sending> sendings = new HashMap<>();
    private final Map<BucketKey, Bucket> buckets = new HashMap<>();
    private long invalid;
    private long expired;
    private long rejectedScope;
    private long capacityRejected;
    private long unsynchronized;
    private int connectionCount;
    private int retainedConnectionReferences;
    private long rateSecond = Long.MIN_VALUE;
    private int rateCount;
    private long lastNow;
    private long lastPruneSecond = Long.MIN_VALUE;

    public WeatherCollector() {
        this(Clock.systemUTC());
    }

    public WeatherCollector(Clock clock) {
        this.clock = java.util.Objects.requireNonNull(clock);
        selection = new WeatherSelection(epoch, 1, false, Set.of());
    }

    public synchronized WeatherSelection selection() {
        return selection;
    }

    public synchronized WeatherSelection configure(long expectedRevision, boolean enabled, Set<Path> paths) {
        WeatherLimits.configured().validateSelection(paths);
        if (expectedRevision != selection.revision()) {
            throw new IllegalStateException("Selection changed; reload before saving");
        }
        final var next = new WeatherSelection(epoch, Math.addExact(selection.revision(), 1), enabled, paths);
        selection = next;
        buckets.keySet().removeIf(key -> !next.paths().contains(key.path()));
        final Iterator<Sending> iterator = sendings.values().iterator();
        while (iterator.hasNext()) {
            final var sending = iterator.next();
            if (!next.paths().contains(sending.terminal.path())) {
                connectionCount -= sending.connections.size();
                iterator.remove();
            }
        }
        return next;
    }

    /**
     * The caller supplies authenticated scope and measured transport age/offset; these are not trusted fields taken
     * from the observation payload.
     */
    public synchronized WeatherReceipt accept(UUID expectedEpoch, Set<String> authorizedSources,
            WeatherObservation observation, long queueAgeMs, long measuredClockOffsetMs) {
        final long now = now();
        prune(now);
        final UUID eventId = observation == null ? null : observation.eventId();
        if (!epoch.equals(expectedEpoch) || queueAgeMs > MAX_QUEUE_AGE_MS) {
            expired++;
            return receipt(eventId, now, EXPIRED);
        }
        if (observation == null || queueAgeMs < 0) {
            invalid++;
            return receipt(eventId, now, REJECTED_INVALID);
        }
        if (!selection.enabled() || !authorizedSources.contains(observation.sourceId())
                || selection.revision() != observation.selectionRevision()
                || !selection.paths().contains(observation.path())) {
            rejectedScope++;
            return receipt(eventId, now, REJECTED_OUT_OF_SCOPE);
        }
        final var seen = events.get(eventId);
        if (seen != null) {
            if (seen.observation().equals(observation)) {
                return receipt(eventId, now, DUPLICATE);
            }
            invalid++;
            return receipt(eventId, now, REJECTED_INVALID);
        }
        if (rateSecond != now / 1000) {
            rateSecond = now / 1000;
            rateCount = 0;
        }
        if (rateCount >= 1000 || events.size() >= MAX_EVENTS) {
            capacityRejected++;
            return receipt(eventId, now, RETRY_LATER);
        }
        rateCount++;
        final var key = new ExecutionKey(observation.sourceId(), observation.sourceEpoch(), observation.executionId());
        final var previous = sendings.get(key);
        if (observation.kind() == Kind.SOCKET_SUPPLEMENT) {
            if (previous == null) {
                return receipt(eventId, now, RETRY_LATER);
            }
            if (!sameSending(previous.terminal, observation)) {
                invalid++;
                return receipt(eventId, now, REJECTED_INVALID);
            }
        } else if (previous != null) {
            invalid++;
            return receipt(eventId, now, REJECTED_INVALID);
        }
        if (observation.kind() == Kind.DISPATCH_DIAGNOSTIC) {
            events.put(eventId, new Seen(observation, now));
            final var bucket = timedBucket(observation, measuredClockOffsetMs, now);
            if (bucket != null) {
                bucket.dispatch++;
            }
            return receipt(eventId, now, ACCEPTED);
        }
        final var sending = previous != null ? previous : new Sending(observation, now);
        final var additions = new HashMap<UUID, Connection>();
        for (final var connection : observation.connections()) {
            final var existing = sending.connections.get(connection.connectionId());
            if (existing != null && !existing.equals(connection)) {
                invalid++;
                return receipt(eventId, now, REJECTED_INVALID);
            }
            if (existing == null) {
                additions.put(connection.connectionId(), connection);
            }
        }
        if (sending.connections.size() + additions.size() > WeatherObservation.MAX_CONNECTIONS
                || retainedConnectionReferences + observation.connections().size() > MAX_CONNECTIONS) {
            capacityRejected++;
            return receipt(eventId, now, RETRY_LATER);
        }
        if (previous == null) {
            sending.bucketKey = timedKey(observation, measuredClockOffsetMs, now);
            sending.observedAt = sending.bucketKey == null ? 0
                    : Math.addExact(observation.occurredAt(), measuredClockOffsetMs);
            sendings.put(key, sending);
        }
        final boolean firstSocket = sending.connections.isEmpty() && !additions.isEmpty();
        sending.connections.putAll(additions);
        connectionCount += additions.size();
        events.put(eventId, new Seen(observation, now));
        retainedConnectionReferences += observation.connections().size();
        if (sending.bucketKey != null) {
            final var bucket = buckets.computeIfAbsent(sending.bucketKey, _ -> new Bucket());
            if (previous == null) {
                switch (observation.outcome()) {
                case SUCCESS -> bucket.success++;
                case FAILURE -> bucket.failure++;
                case CANCELLED -> bucket.cancelled++;
                case UNKNOWN -> bucket.unknown++;
                }
            }
            if (firstSocket) {
                bucket.withSocket++;
            }
            bucket.connections += additions.size();
        }
        return receipt(eventId, now, ACCEPTED);
    }

    public synchronized Snapshot snapshot() {
        final long now = now();
        prune(now);
        final var rows = new ArrayList<Summary>();
        for (final var path : selection.paths()) {
            long success = 0, failure = 0, cancelled = 0, unknown = 0, sockets = 0, connections = 0, dispatch = 0;
            for (final var entry : buckets.entrySet()) {
                if (entry.getKey().path().equals(path) && entry.getKey().minute() > now / 60000 - 5) {
                    final var b = entry.getValue();
                    success += b.success;
                    failure += b.failure;
                    cancelled += b.cancelled;
                    unknown += b.unknown;
                    sockets += b.withSocket;
                    connections += b.connections;
                    dispatch += b.dispatch;
                }
            }
            rows.add(new Summary(path, success, failure, cancelled, unknown, sockets, connections, dispatch));
        }
        return new Snapshot(epoch, selection.revision(), selection.enabled(), now, List.copyOf(rows), invalid, expired,
                rejectedScope, capacityRejected, unsynchronized, events.size(), connectionCount,
                retainedConnectionReferences);
    }

    public synchronized List<WeatherViewer.Path> viewer(Set<String> hosts, Set<String> destinations, boolean full) {
        final long now = now();
        prune(now);
        final var counts = new HashMap<Path, ViewerCounts>();
        for (final var path : selection.paths())
            if (full || hosts.contains(path.hostId()))
                counts.put(path, new ViewerCounts());
        for (final var sending : sendings.values()) {
            final var event = sending.terminal;
            final var count = counts.get(event.path());
            if (count == null || !full && !destinations.contains(event.destinationId()))
                continue;
            count.visible = true;
            if (sending.observedAt <= now - 300_000)
                continue;
            count.last = Math.max(count.last, sending.observedAt);
            switch (event.outcome()) {
            case SUCCESS -> count.success++;
            case FAILURE -> count.failure++;
            case CANCELLED -> count.cancelled++;
            case UNKNOWN -> count.unknown++;
            }
            if (!sending.connections.isEmpty())
                count.sockets++;
            for (final var socket : sending.connections.values())
                if (socket.rttMs() != null) {
                    count.rtt += socket.rttMs();
                    count.rtts++;
                }
        }
        final var rows = new ArrayList<WeatherViewer.Path>();
        for (final var path : selection.paths()) {
            final var count = counts.get(path);
            if (count != null && (full || count.visible)) {
                rows.add(new WeatherViewer.Path(path.sourceId(), path.hostId(), path.protocol(), count.success,
                        count.failure, count.cancelled, count.unknown, count.sockets, count.last,
                        count.rtts == 0 ? null : count.rtt / count.rtts, null, null));
            }
        }
        return List.copyOf(rows);
    }

    private static final class ViewerCounts {
        boolean visible;
        long success, failure, cancelled, unknown, sockets, last, rtts;
        double rtt;
    }

    public record Summary(Path path, long successes, long failures, long cancellations, long unknown,
            long sendingsWithSockets, long connections, long dispatchDiagnostics) implements Serializable {

        private static final long serialVersionUID = 1L;
        public Double successFraction() {
            return successes + failures == 0 ? null : (double) successes / (successes + failures);
        }
    }

    public record Snapshot(UUID masterEpoch, long selectionRevision, boolean enabled, long receivedAt,
            List<Summary> summaries, long invalid, long expired, long outOfScope, long capacityRejected,
            long unsynchronized, int retainedEvents, int retainedConnections, int retainedConnectionReferences)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private Bucket timedBucket(WeatherObservation observation, long offset, long now) {
        final var key = timedKey(observation, offset, now);
        return key == null ? null : buckets.computeIfAbsent(key, _ -> new Bucket());
    }

    private BucketKey timedKey(WeatherObservation observation, long offset, long now) {
        if (offset < -MAX_CLOCK_OFFSET_MS || offset > MAX_CLOCK_OFFSET_MS) {
            unsynchronized++;
            return null;
        }
        final long corrected;
        try {
            corrected = Math.addExact(observation.occurredAt(), offset);
        } catch (ArithmeticException e) {
            unsynchronized++;
            return null;
        }
        if (corrected > now || corrected <= now - RETENTION_MS) {
            unsynchronized++;
            return null;
        }
        return new BucketKey(observation.path(), corrected / 60000);
    }

    private static boolean sameSending(WeatherObservation terminal, WeatherObservation supplement) {
        return terminal.kind() == Kind.SENDING_TERMINAL && terminal.path().equals(supplement.path())
                && terminal.transferId() == supplement.transferId()
                && terminal.destinationId().equals(supplement.destinationId())
                && terminal.outcome() == supplement.outcome() && terminal.category() == supplement.category()
                && terminal.occurredAt() == supplement.occurredAt();
    }

    private WeatherReceipt receipt(UUID eventId, long now, Disposition disposition) {
        return new WeatherReceipt(eventId, epoch, now, disposition);
    }

    private long now() {
        // A backwards wall-clock adjustment must not renew the replay horizon.
        lastNow = Math.max(lastNow, clock.millis());
        return lastNow;
    }

    private void prune(long now) {
        if (lastPruneSecond == now / 1000) {
            return;
        }
        lastPruneSecond = now / 1000;
        final var eventIterator = events.values().iterator();
        while (eventIterator.hasNext()) {
            final var seen = eventIterator.next();
            if (seen.receivedAt() <= now - RETENTION_MS) {
                retainedConnectionReferences -= seen.observation().connections().size();
                eventIterator.remove();
            } else {
                break;
            }
        }
        final var iterator = sendings.values().iterator();
        while (iterator.hasNext()) {
            final var sending = iterator.next();
            if (sending.receivedAt <= now - RETENTION_MS) {
                connectionCount -= sending.connections.size();
                iterator.remove();
            }
        }
        buckets.keySet().removeIf(key -> key.minute() <= now / 60000 - 60);
    }

    private record ExecutionKey(String source, UUID sourceEpoch, UUID execution) {
    }

    private record BucketKey(Path path, long minute) {
    }

    private record Seen(WeatherObservation observation, long receivedAt) {
    }

    private static final class Sending {
        final WeatherObservation terminal;
        final long receivedAt;
        long observedAt;
        final Map<UUID, Connection> connections = new HashMap<>();
        BucketKey bucketKey;

        Sending(WeatherObservation terminal, long receivedAt) {
            this.terminal = terminal;
            this.receivedAt = receivedAt;
        }
    }

    private static final class Bucket {
        long success, failure, cancelled, unknown, withSocket, connections, dispatch;
    }
}
