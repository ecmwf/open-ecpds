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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bounded, local publication history. Independent of operational telemetry admission and export. Payloads and
 * connection options are deliberately never retained.
 */
public final class MQTTActivity implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(MQTTActivity.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_ENTRIES = 2000;
    private static final long MAX_BYTES = 16 * 1024 * 1024;
    private static final long MAX_AGE = 24 * 60 * 60 * 1000L;
    private final Path file;
    private final Map<Long, Entry> entries = new LinkedHashMap<>();
    private FileChannel lockChannel;
    private FileLock lock;
    private String persistenceError = "";
    private long journalBytes;
    private long retainedBytes;

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

    public MQTTActivity(final Path file) {
        this.file = file.toAbsolutePath();
        try {
            final var parent = this.file.getParent();
            Files.createDirectories(parent,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            lockChannel = FileChannel.open(parent.resolve("history.lock"), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            if (lock == null) {
                throw new IOException("MQTT activity history is already in use");
            }
            if (Files.exists(this.file)) {
                if (Files.size(this.file) > MAX_BYTES) {
                    throw new IOException("MQTT activity history exceeds its size limit");
                }
                for (final var line : Files.readAllLines(this.file, StandardCharsets.UTF_8)) {
                    try {
                        final var entry = JSON.readValue(line, Entry.class);
                        put(entry);
                    } catch (final Exception e) {
                        persistenceError = "Some MQTT activity history could not be recovered";
                        LOG.warn("Ignoring unreadable MQTT activity history entry", e);
                    }
                }
            }
            // A process restart cannot prove the outcome of an interrupted broker call.
            for (final var entry : new ArrayList<>(entries.values())) {
                if ("PROCESSING".equals(entry.status()) || "QUEUED".equals(entry.status())) {
                    put(copy(entry, "UNCONFIRMED", "Master restarted before broker result was recorded",
                            entry.brokers()));
                }
            }
            compact();
        } catch (final Exception e) {
            degraded(e);
            releaseLock();
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
                Map.entry("maxEntries", MAX_ENTRIES), Map.entry("retentionHours", 24),
                Map.entry("lastAcceptedAt", lastAccepted), Map.entry("oldestPendingAt", oldestPending));
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
        final var cutoff = System.currentTimeMillis() - MAX_AGE;
        final var it = entries.values().iterator();
        while (it.hasNext()) {
            final var e = it.next();
            if (entries.size() > MAX_ENTRIES || retainedBytes > MAX_BYTES / 2 || e.updatedAt() < cutoff) {
                try {
                    retainedBytes -= JSON.writeValueAsBytes(e).length;
                } catch (final IOException ignored) {
                    // Entries consist only of primitive fields and immutable records.
                }
                it.remove();
            }
        }
    }

    private void save(final Entry e) {
        try {
            put(e);
            if (lock == null || !lock.isValid()) {
                return;
            }
            final var line = JSON.writeValueAsString(e) + "\n";
            final var size = line.getBytes(StandardCharsets.UTF_8).length;
            if (journalBytes + size > MAX_BYTES) {
                compact();
            } else {
                Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                journalBytes += size;
            }
        } catch (final Exception failure) {
            degraded(failure);
        }
    }

    private void compact() throws IOException {
        final var temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.deleteIfExists(temporary);
        Files.createFile(temporary, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        try (final var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            for (final var entry : entries.values()) {
                writer.write(JSON.writeValueAsString(entry));
                writer.newLine();
            }
        }
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        journalBytes = Files.size(file);
    }

    private void degraded(final Exception e) {
        persistenceError = "MQTT activity persistence is degraded; recent results may not survive a restart";
        LOG.warn("MQTT activity persistence failure: {}", file, e);
    }

    private static String limited(final String text) {
        return text == null ? "" : text.substring(0, Math.min(1024, text.length()));
    }

    private void releaseLock() {
        try {
            if (lock != null) {
                lock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
        } catch (final IOException e) {
            LOG.warn("Closing MQTT activity history", e);
        }
        lock = null;
    }

    @Override
    public synchronized void close() {
        releaseLock();
    }
}
