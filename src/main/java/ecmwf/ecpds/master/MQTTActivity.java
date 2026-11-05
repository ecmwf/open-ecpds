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
 * In applying this License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.ECpdsBase;
import ecmwf.common.technical.Cnf;

/**
 * Bounded publication history with asynchronous database persistence, independent of telemetry admission. Payloads and
 * connection options are deliberately never retained.
 */
public final class MQTTActivity implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(MQTTActivity.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    record Limits(int maxEntries, long maxBytes, int retentionHours) {
        Limits {
            if (maxEntries <= 0 || maxEntries == Integer.MAX_VALUE || maxBytes <= 0 || retentionHours <= 0) {
                throw new IllegalArgumentException(
                        "MQTTActivity limits must be positive; maxEntries must be below 2147483647");
            }
        }

        static Limits configured() {
            return new Limits(Integer.parseInt(Cnf.at("MQTTActivity", "maxEntries", "2000")),
                    Long.parseLong(Cnf.at("MQTTActivity", "maxBytes", "8388608")),
                    Integer.parseInt(Cnf.at("MQTTActivity", "retentionHours", "24")));
        }

        long maxAge() {
            return retentionHours * 3600000L;
        }
    }

    private final Limits limits;
    private final Store store;
    private final ScheduledExecutorService worker;
    private final Map<Long, Entry> entries = new LinkedHashMap<>();
    private final Map<Long, Entry> dirty = new LinkedHashMap<>();
    private String persistenceError = "MQTT activity history is loading from the database";
    private long retainedBytes;
    private boolean loaded;
    private boolean cleanup = true;
    private boolean closed;

    interface Store {
        List<Entry> load() throws Exception;

        void save(Entry entry) throws Exception;

        void retain(Set<Long> ids, long cutoff) throws Exception;
    }

    static Store database(final ECpdsBase database, final String owner, final Limits limits) {
        if (owner.isBlank() || owner.length() > 255) {
            throw new IllegalArgumentException("MQTTActivity.owner must contain 1 to 255 characters");
        }
        return new Store() {
            @Override
            public List<Entry> load() throws Exception {
                final var result = new ArrayList<Entry>();
                for (final var payload : database.getMQTTActivity(owner, limits.maxEntries(), limits.maxBytes())) {
                    result.add(JSON.readValue(payload, Entry.class));
                }
                return result;
            }

            @Override
            public void save(final Entry entry) throws Exception {
                database.saveMQTTActivity(owner, entry.id(), entry.updatedAt(), entry.updatedAt() + limits.maxAge(),
                        JSON.writeValueAsString(entry));
            }

            @Override
            public void retain(final Set<Long> ids, final long cutoff) throws Exception {
                database.retainMQTTActivity(owner, ids, cutoff);
            }
        };
    }

    public record Broker(String mover, String status, String reason, long completedAt) implements Serializable {
        public Broker {
            mover = limited(mover);
            reason = limited(reason);
        }
    }

    public record Entry(long id, long transferId, String destination, String filename, long queuedAt, long startedAt,
            long updatedAt, String status, String topic, int qos, boolean retain, long payloadBytes, String contentType,
            String reason, int attempts, List<Broker> brokers) implements Serializable {
        public Entry {
            destination = limited(destination);
            filename = limited(filename);
            topic = limited(topic);
            contentType = limited(contentType);
            reason = limited(reason);
            brokers = List.copyOf(brokers.stream().limit(128).toList());
        }
    }

    public MQTTActivity(final ECpdsBase database, final String owner) {
        this(database, owner, Limits.configured());
    }

    private MQTTActivity(final ECpdsBase database, final String owner, final Limits limits) {
        this(database(database, owner, limits), limits, true);
    }

    MQTTActivity(final Store store, final boolean startWorker) {
        this(store, new Limits(2000, 8388608, 24), startWorker);
    }

    MQTTActivity(final Store store, final Limits limits, final boolean startWorker) {
        this.store = store;
        this.limits = limits;
        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            final var thread = new Thread(task, "MQTTActivity");
            thread.setDaemon(true);
            return thread;
        });
        if (startWorker) {
            worker.scheduleWithFixedDelay(this::flush, 0, 1, TimeUnit.SECONDS);
        }
    }

    // Only the persistence worker calls this method; no SQL I/O holds the admission lock.
    void flush() {
        try {
            if (!loaded) {
                final var recovered = new LinkedHashMap<Long, Entry>();
                for (final var entry : store.load()) {
                    recovered.put(entry.id(), entry);
                }
                synchronized (this) {
                    final var now = System.currentTimeMillis();
                    recovered.replaceAll((_, entry) -> {
                        if (entry.updatedAt() >= now - limits.maxAge()
                                && ("PROCESSING".equals(entry.status()) || "QUEUED".equals(entry.status()))) {
                            return copy(entry, "UNCONFIRMED", "Master restarted before broker result was recorded",
                                    entry.brokers());
                        }
                        return entry;
                    });
                    recovered.putAll(entries);
                    entries.clear();
                    dirty.clear();
                    retainedBytes = 0;
                    for (final var entry : recovered.values().stream()
                            .sorted(Comparator.comparingLong(Entry::updatedAt)).toList()) {
                        save(entry);
                    }
                    loaded = true;
                }
            }
            final Map<Long, Entry> pending;
            final Set<Long> retained;
            synchronized (this) {
                prune();
                if (dirty.isEmpty() && !cleanup) {
                    return;
                }
                pending = new LinkedHashMap<>(dirty);
                retained = Set.copyOf(entries.keySet());
            }
            for (final var entry : pending.values()) {
                store.save(entry);
            }
            store.retain(retained, System.currentTimeMillis() - limits.maxAge());
            synchronized (this) {
                pending.forEach((id, entry) -> dirty.remove(id, entry));
                // A concurrent prune must be reflected in the next database cleanup.
                cleanup = !retained.equals(entries.keySet());
                persistenceError = "";
            }
        } catch (final Exception e) {
            degraded(e);
        }
    }

    public synchronized void queued(final long id, final long transferId, final String destination,
            final String filename) {
        if (!entries.containsKey(id)) {
            final var now = System.currentTimeMillis();
            save(new Entry(id, transferId, destination, filename, now, 0, now, "QUEUED", "", -1, false, 0, "", "", 0,
                    List.of()));
        }
    }

    public synchronized void processing(final long id) {
        final var e = entries.get(id);
        if (e != null) {
            final var now = System.currentTimeMillis();
            save(new Entry(e.id(), e.transferId(), e.destination(), e.filename(), e.queuedAt(), now, now, "PROCESSING",
                    "", -1, false, 0, "", "", e.attempts() + 1, List.of()));
        }
    }

    public synchronized void message(final long id, final String topic, final int qos, final boolean retain,
            final String payload, final String contentType) {
        final var e = entries.get(id);
        if (e != null) {
            save(new Entry(e.id(), e.transferId(), e.destination(), e.filename(), e.queuedAt(), e.startedAt(),
                    System.currentTimeMillis(), "PROCESSING", topic, qos, retain,
                    payload.getBytes(StandardCharsets.UTF_8).length, contentType, "", e.attempts(), e.brokers()));
        }
    }

    public synchronized void broker(final long id, final String mover, final String status, final String reason) {
        final var e = entries.get(id);
        if (e != null) {
            final var brokers = new ArrayList<>(e.brokers());
            brokers.add(new Broker(mover, status, reason, System.currentTimeMillis()));
            save(copy(e, "PROCESSING", brokers.size() > 128 ? "Broker detail limited to 128 Movers" : "", brokers));
        }
    }

    static String transferHistorySummary(final long publicationId, final List<String> outcomes) {
        final var accepted = outcomes.stream().filter("ACCEPTED"::equals).count();
        final var failed = outcomes.stream().filter("FAILED"::equals).count();
        final var unconfirmed = outcomes.size() - accepted - failed;
        return "MQTT notification Publication-" + publicationId + ": "
                + (outcomes.isEmpty() ? "not sent (no Data Movers configured)"
                        : "broker accepted=" + accepted + ", failed=" + failed + ", unconfirmed=" + unconfirmed
                                + " (broker acceptance does not confirm subscriber delivery)")
                + ". See Operations > MQTT Activity for details.";
    }

    public synchronized void complete(final long id) {
        final var e = entries.get(id);
        if (e != null) {
            final var statuses = e.brokers().stream().map(Broker::status).distinct().toList();
            final var status = e.reason().startsWith("Broker detail limited") ? "UNCONFIRMED"
                    : statuses.isEmpty() ? "FAILED" : statuses.size() == 1 ? statuses.getFirst() : "PARTIAL";
            save(copy(e, status, statuses.isEmpty() ? "No Data Movers configured" : e.reason(), e.brokers()));
        }
    }

    public synchronized void finish(final long id, final String status, final String reason) {
        final var e = entries.get(id);
        if (e != null) {
            save(copy(e, status, reason, e.brokers()));
        }
    }

    public synchronized void interrupted(final long id) {
        final var e = entries.get(id);
        if (e != null && ("QUEUED".equals(e.status()) || "PROCESSING".equals(e.status()))) {
            save(copy(e, "UNCONFIRMED", "Publication processing interrupted; see Master event log", e.brokers()));
        }
    }

    /** Filter before aggregating or returning any metadata, including counts and chart points. */
    public synchronized Map<String, Object> snapshot(final Set<String> destinations, final long since,
            final String destination, final String topic, final String status, final int offset) {
        prune();
        final var matches = entries.values().stream()
                .filter(e -> destinations.contains(e.destination()) && e.updatedAt() >= since)
                .filter(e -> destination.isEmpty() || e.destination().equals(destination))
                .filter(e -> topic.isEmpty() || e.topic().contains(topic))
                .filter(e -> status.isEmpty() || e.status().equals(status))
                .sorted(Comparator.comparingLong(Entry::updatedAt).reversed().thenComparingLong(Entry::id)).toList();
        final Map<String, Long> counts = new LinkedHashMap<>();
        final Map<Long, Map<String, Long>> buckets = new java.util.TreeMap<>();
        for (final var e : matches) {
            counts.merge(e.status(), 1L, Long::sum);
            buckets.computeIfAbsent(e.updatedAt() / 60000 * 60000, _ -> new LinkedHashMap<>()).merge(e.status(), 1L,
                    Long::sum);
        }
        final var rows = matches.stream().skip(Math.max(0, offset)).limit(100).toList();
        final var choices = entries.values().stream().map(Entry::destination).filter(destinations::contains).distinct()
                .sorted().toList();
        final var lastAccepted = matches.stream().flatMap(e -> e.brokers().stream())
                .filter(b -> "ACCEPTED".equals(b.status())).mapToLong(Broker::completedAt).max().orElse(0);
        final var oldestPending = matches.stream()
                .filter(e -> "QUEUED".equals(e.status()) || "PROCESSING".equals(e.status())).mapToLong(Entry::queuedAt)
                .min().orElse(0);
        return Map.ofEntries(Map.entry("rows", rows), Map.entry("total", matches.size()), Map.entry("counts", counts),
                Map.entry("buckets", buckets), Map.entry("destinations", choices),
                Map.entry("now", System.currentTimeMillis()), Map.entry("persistenceError", persistenceError),
                Map.entry("maxEntries", limits.maxEntries()), Map.entry("maxBytes", limits.maxBytes()),
                Map.entry("retentionHours", limits.retentionHours()), Map.entry("lastAcceptedAt", lastAccepted),
                Map.entry("oldestPendingAt", oldestPending));
    }

    private static Entry copy(final Entry e, final String status, final String reason, final List<Broker> brokers) {
        return new Entry(e.id(), e.transferId(), e.destination(), e.filename(), e.queuedAt(), e.startedAt(),
                System.currentTimeMillis(), status, e.topic(), e.qos(), e.retain(), e.payloadBytes(), e.contentType(),
                reason, e.attempts(), brokers);
    }

    private void put(final Entry e) throws IOException {
        final var old = entries.remove(e.id());
        if (old != null) {
            retainedBytes -= JSON.writeValueAsBytes(old).length;
        }
        entries.put(e.id(), e);
        retainedBytes += JSON.writeValueAsBytes(e).length;
        prune();
    }

    private void prune() {
        final var cutoff = System.currentTimeMillis() - limits.maxAge();
        final var it = entries.values().iterator();
        while (it.hasNext()) {
            final var e = it.next();
            if (entries.size() > limits.maxEntries() || retainedBytes > limits.maxBytes() || e.updatedAt() < cutoff) {
                try {
                    retainedBytes -= JSON.writeValueAsBytes(e).length;
                } catch (final IOException ignored) {
                    // Entries consist only of primitive fields and immutable records.
                }
                it.remove();
                dirty.remove(e.id());
                cleanup = true;
            }
        }
    }

    private void save(final Entry e) {
        try {
            put(e);
            if (entries.containsKey(e.id())) {
                dirty.put(e.id(), e);
            }
        } catch (final Exception failure) {
            degraded(failure);
        }
    }

    private synchronized void degraded(final Exception e) {
        if (!persistenceError.startsWith("MQTT activity database persistence is degraded")) {
            LOG.warn("MQTT activity database persistence failure", e);
        }
        persistenceError = "MQTT activity database persistence is degraded; recent results may not survive a restart";
    }

    private static String limited(final String text) {
        return text == null ? "" : text.substring(0, Math.min(1024, text.length()));
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            worker.execute(this::flush);
            worker.shutdown();
        }
        try {
            if (!worker.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("MQTT activity persistence did not finish before shutdown");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
