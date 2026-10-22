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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.weather.WeatherObservation.Path;

/**
 * Master-owned control state. Database work is confined to startup and explicit administrator saves; snapshots never
 * query the database.
 */
public final class WeatherAdministration {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Store store;
    private final WeatherCollector collector;
    private final WeatherSourceRegistry sources;
    private final WeatherPersistence persistence;
    private final java.util.concurrent.atomic.AtomicLong verifiedBatches = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong rejectedBatches = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong deferredBatches = new java.util.concurrent.atomic.AtomicLong();
    private long revision = 1;
    private String error;
    private String updatedBy = "";
    private long updatedAt;
    private boolean loaded;
    private boolean saving;
    private boolean requestedEnabled;
    private final WeatherProbes probes = new WeatherProbes();

    public WeatherProbes probes() {
        return probes;
    }

    public void reconcile(Set<Path> paths) throws Exception {
        final long expected;
        final boolean enabled;
        final boolean pruned;
        synchronized (this) {
            if (collector.selection().paths().equals(paths))
                return;
            if (saving)
                throw new IllegalStateException("A Network Weather configuration change is already running");
            expected = revision;
            enabled = requestedEnabled;
            final var retained = collector.selection().paths().stream().filter(paths::contains)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            pruned = !retained.equals(collector.selection().paths());
            if (pruned)
                collector.configure(collector.selection().revision(),
                        enabled && persistence.ready() && !retained.isEmpty(), retained);
        }
        try {
            change(expected, paths, "weather-scheduler", enabled);
        } catch (Exception e) {
            if (pruned)
                synchronized (this) {
                    error = "Removed paths are stopped, but recording the new scope failed; retry before restarting.";
                }
            throw e;
        }
    }

    public interface Store {
        String load() throws Exception;

        void save(String document) throws Exception;
    }

    public record Configuration(int schemaVersion, long revision, Set<Path> paths, String updatedBy, long updatedAt,
            boolean enabled) {
        public Configuration(int schemaVersion, long revision, Set<Path> paths, String updatedBy, long updatedAt) {
            this(schemaVersion, revision, paths, updatedBy, updatedAt, false);
        }

        public Configuration {
            if (schemaVersion != 1 || revision <= 0 || updatedBy == null || updatedBy.length() > 128
                    || updatedBy.chars().anyMatch(Character::isISOControl) || updatedAt < 0) {
                throw new IllegalArgumentException("Invalid Network Weather configuration");
            }
            paths = new WeatherSelection(java.util.UUID.randomUUID(), revision, false, paths).paths();
            WeatherLimits.configured().validateSelection(paths);
        }
    }

    public record Status(long configurationRevision, List<Path> paths, String updatedBy, long updatedAt, String error,
            boolean saving, boolean restored, WeatherCollector.Snapshot collector, String collectionCapability,
            String socketPersistenceCapability, String automaticSelectionCapability, String probeCapability,
            List<WeatherSourceRegistry.Status> sources, long verifiedBatches, long rejectedBatches,
            long deferredBatches, String activationBlocker, boolean requestedEnabled,
            WeatherPersistence.Status persistence, WeatherLimits limits) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public WeatherAdministration(Store store) {
        this(store, new WeatherPersistence(), new WeatherSourceRegistry());
    }

    public WeatherAdministration(Store store, WeatherPersistence persistence, WeatherSourceRegistry sources) {
        this.store = java.util.Objects.requireNonNull(store);
        this.persistence = java.util.Objects.requireNonNull(persistence);
        this.sources = java.util.Objects.requireNonNull(sources);
        collector = new WeatherCollector();
    }

    public void load() throws Exception {
        synchronized (this) {
            if (loaded || saving) {
                throw new IllegalStateException("Network Weather control state was already loaded");
            }
            saving = true;
        }
        try {
            final var document = store.load();
            final var config = document == null ? new Configuration(1, 1, Set.of(), "", 0) : decode(document);
            synchronized (this) {
                collector.configure(collector.selection().revision(), false, config.paths());
                revision = config.revision();
                updatedBy = config.updatedBy();
                updatedAt = config.updatedAt();
                requestedEnabled = config.enabled();
                error = null;
                loaded = true;
            }
        } catch (Exception e) {
            synchronized (this) {
                error = "Network Weather configuration could not be restored; collection is unavailable.";
            }
            throw e;
        } finally {
            synchronized (this) {
                saving = false;
            }
        }
    }

    public Status save(long expectedRevision, Set<Path> paths, String actor) throws Exception {
        java.util.Objects.requireNonNull(paths, "paths");
        return change(expectedRevision, paths, actor, null);
    }

    public Status setCollectionEnabled(long expectedRevision, boolean enabled, String actor) throws Exception {
        return change(expectedRevision, null, actor, enabled);
    }

    private Status change(long expectedRevision, Set<Path> paths, String actor, Boolean collectionEnabled)
            throws Exception {
        final Configuration next;
        final boolean deactivate = Boolean.FALSE.equals(collectionEnabled);
        synchronized (this) {
            if (!loaded) {
                throw new IllegalStateException("Configuration has not been restored; inspect the Master log");
            }
            if (saving) {
                throw new IllegalStateException("A Network Weather configuration change is already running");
            }
            if (expectedRevision != revision) {
                throw new IllegalStateException("Configuration changed; reload before saving");
            }
            if (Boolean.TRUE.equals(collectionEnabled) && paths == null) {
                final var blocker = activationBlocker();
                if (blocker != null) {
                    throw new IllegalStateException("Network Weather activation blocked: " + blocker);
                }
            }
            if (collectionEnabled != null && paths == null) {
                paths = collector.selection().paths();
            }
            next = new Configuration(1, Math.addExact(revision, 1), paths, actor, System.currentTimeMillis(),
                    Boolean.TRUE.equals(collectionEnabled));
            if (deactivate) {
                // Stop admission before the control-state write, even if persistence subsequently fails.
                requestedEnabled = false;
                collector.configure(collector.selection().revision(), false, next.paths());
            }
            saving = true;
        }
        try {
            final var document = JSON.writeValueAsString(next);
            checkSize(document);
            store.save(document);
            synchronized (this) {
                if (!deactivate) {
                    collector.configure(collector.selection().revision(),
                            next.enabled() && persistence.ready() && !next.paths().isEmpty(), next.paths());
                }
                requestedEnabled = next.enabled();
                revision = next.revision();
                updatedBy = next.updatedBy();
                updatedAt = next.updatedAt();
                error = null;
            }
        } catch (Exception e) {
            synchronized (this) {
                error = deactivate
                        ? "Collection is stopped, but recording deactivation failed; inspect the Master log and retry."
                        : "The configuration change failed; the previous control state is retained.";
            }
            throw e;
        } finally {
            synchronized (this) {
                saving = false;
            }
        }
        return status();
    }

    private String activationBlocker() {
        if (!loaded) {
            return "Control state has not been restored.";
        }
        if (!persistence.ready()) {
            return "Transactional socket schema/writer is unavailable; inspect writer status and the Master log.";
        }
        final var selected = selectedSources();
        if (selected.isEmpty()) {
            return "Save at least one validated dissemination or acquisition path before activation.";
        }
        if (selected.stream().anyMatch(source -> !sources.registered(source))) {
            return "Every selected source needs a fresh verified identity (including approved Continental routes).";
        }
        return null;
    }

    /** The owning background scheduler updates readiness; page reads never run database probes. */
    public synchronized void refreshAvailability() {
        final boolean enabled = loaded && requestedEnabled && persistence.ready()
                && !collector.selection().paths().isEmpty();
        final var selection = collector.selection();
        if (selection.enabled() != enabled) {
            collector.configure(selection.revision(), enabled, selection.paths());
        }
    }

    public WeatherPersistence persistence() {
        return persistence;
    }

    public synchronized WeatherViewer viewer(Set<String> hosts, Set<String> destinations, boolean full) {
        final boolean enabled = loaded && requestedEnabled && collector.selection().enabled() && persistence.ready();
        final var probeStatus = probes.status();
        final var recentProbes = probeStatus.completed();
        final var rows = enabled ? collector.viewer(hosts, destinations, full) : List.<WeatherViewer.Path> of();
        return new WeatherViewer(enabled, System.currentTimeMillis(), rows.stream().map(p -> {
            final var probe = recentProbes.stream()
                    .filter(c -> collector.selection().paths().contains(c.job().policy().path())
                            && c.job().policy().path().sourceId().equals(p.source())
                            && c.job().policy().path().hostId().equals(p.hostId())
                            && c.job().policy().path().protocol().equals(p.protocol())
                            && c.job().policy().path().leg().equals(p.leg()))
                    .max(java.util.Comparator.comparingLong(WeatherProbes.Completed::receivedAt)).orElse(null);
            final var unavailable = probeStatus.unavailable().stream()
                    .filter(c -> collector.selection().paths().contains(c.job().policy().path())
                            && c.job().policy().path().sourceId().equals(p.source())
                            && c.job().policy().path().hostId().equals(p.hostId())
                            && c.job().policy().path().protocol().equals(p.protocol())
                            && c.job().policy().path().leg().equals(p.leg()))
                    .max(java.util.Comparator.comparingLong(WeatherProbes.Unavailable::receivedAt)).orElse(null);
            if (unavailable != null && (probe == null || unavailable.receivedAt() > probe.receivedAt()))
                return new WeatherViewer.Path(p.source(), p.hostId(), p.protocol(), p.successes(), p.failures(),
                        p.cancellations(), p.unknown(), p.sockets(), p.lastObserved(), p.rttMs(), null, null,
                        unavailable.job().policy().mode().name(), "SOURCE_UNAVAILABLE", unavailable.receivedAt(), null,
                        p.leg());
            return probe == null ? p
                    : new WeatherViewer.Path(p.source(), p.hostId(), p.protocol(), p.successes(), p.failures(),
                            p.cancellations(), p.unknown(), p.sockets(), p.lastObserved(), p.rttMs(), null, null,
                            probe.job().policy().mode().name(), probe.result().outcome().name(), probe.receivedAt(),
                            probe.result().latencyMs(), p.leg());
        }).toList());
    }

    public synchronized boolean viewerEnabled() {
        return loaded && requestedEnabled && collector.selection().enabled() && persistence.ready();
    }

    public synchronized long viewerRevision() {
        return collector.selection().revision();
    }

    public synchronized Status status() {
        final var paths = collector.selection().paths().stream()
                .sorted(Comparator.comparing(Path::sourceId).thenComparing(Path::hostId)
                        .thenComparing(Path::configuredTarget).thenComparing(Path::protocol).thenComparing(Path::leg))
                .toList();
        return new Status(revision, paths, updatedBy, updatedAt, error, saving, loaded, collector.snapshot(),
                collector.selection().enabled() ? "ACTIVE: bounded passive collection"
                        : requestedEnabled ? "SUSPENDED: waiting for eligible paths or writer readiness"
                                : "DISABLED by administrator",
                persistence.ready()
                        ? "READY: bounded transactional socket writer" : "UNAVAILABLE: inspect writer status",
                "AVAILABLE: explicit automatic-selection policy",
                probes.status().enabled() ? "ENABLED: approved bounded schedule"
                        : "DISABLED: explicit probe approval required",
                sources.snapshot(), verifiedBatches.get(), rejectedBatches.get(), deferredBatches.get(),
                activationBlocker(), requestedEnabled, persistence.status(), WeatherLimits.configured());
    }

    public WeatherSourceRegistry sources() {
        return sources;
    }

    public synchronized Set<String> selectedSources() {
        return collector.selection().paths().stream().map(Path::sourceId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public synchronized WeatherCapabilities capabilities(String sourceId) {
        WeatherCapabilities.validateSource(sourceId);
        if (!loaded) {
            throw new IllegalStateException("Network Weather control state has not been restored");
        }
        final var selected = collector.selection();
        final boolean enabled = selected.enabled() && requestedEnabled && persistence.ready()
                && sources.registered(sourceId);
        final var paths = selected.paths().stream().filter(path -> sourceId.equals(path.sourceId()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new WeatherCapabilities(1, sourceId, System.currentTimeMillis(), 60_000,
                new WeatherSelection(selected.masterEpoch(), selected.revision(), enabled, paths), enabled, enabled,
                100, 256 * 1024,
                enabled ? probes.jobs(sourceId).stream().filter(j -> paths.contains(j.policy().path())
                        && j.masterEpoch().equals(selected.masterEpoch()) && System.currentTimeMillis() < j.expiresAt())
                        .toList() : List.of());
    }

    public WeatherAcknowledgement receive(WeatherBatch batch) throws IOException {
        return receive(batch, null);
    }

    /**
     * Ingress verifies signed identity/timing and admits only to bounded in-memory state; database work is
     * background-only.
     */
    public WeatherAcknowledgement receive(WeatherBatch batch, Object currentRegistration) throws IOException {
        if (batch == null) {
            throw new IllegalArgumentException("Missing Network Weather batch");
        }
        final java.util.UUID epoch;
        synchronized (this) {
            if (!loaded) {
                throw new IllegalStateException("Network Weather control state has not been restored");
            }
            epoch = collector.selection().masterEpoch();
        }
        batch.encode();
        final long now = System.currentTimeMillis();
        final WeatherReceipt.Disposition disposition;
        if (!epoch.equals(batch.masterEpoch())) {
            disposition = WeatherReceipt.Disposition.EXPIRED;
        } else {
            WeatherReceipt.Disposition verified;
            try {
                final var timing = sources.verify(batch, currentRegistration);
                timing.validateDispatch(batch, now);
                verifiedBatches.incrementAndGet();
                synchronized (this) {
                    if (!requestedEnabled || !collector.selection().enabled() || !persistence.ready()) {
                        verified = WeatherReceipt.Disposition.REJECTED_OUT_OF_SCOPE;
                    } else {
                        final var receipts = new java.util.ArrayList<WeatherReceipt>();
                        boolean timingRejected = false;
                        for (final var delivery : batch.deliveries()) {
                            final var observation = delivery.observation();
                            WeatherReceipt receipt;
                            try {
                                receipt = collector.accept(epoch, Set.of(batch.sourceId()), observation,
                                        timing.queueAge(batch, delivery, now), timing.bucketOffset(observation));
                            } catch (IllegalArgumentException | ArithmeticException e) {
                                timingRejected = true;
                                receipt = new WeatherReceipt(observation.eventId(), epoch, now,
                                        WeatherReceipt.Disposition.REJECTED_INVALID);
                            }
                            if ((receipt.disposition() == WeatherReceipt.Disposition.ACCEPTED
                                    || receipt.disposition() == WeatherReceipt.Disposition.DUPLICATE)
                                    && observation.kind() != WeatherObservation.Kind.DISPATCH_DIAGNOSTIC
                                    && !persistence.offer(observation)) {
                                receipt = new WeatherReceipt(observation.eventId(), epoch, now,
                                        WeatherReceipt.Disposition.RETRY_LATER);
                            }
                            receipts.add(receipt);
                        }
                        if (timingRejected) {
                            rejectedBatches.incrementAndGet();
                        }
                        final var probeReceipts = new java.util.ArrayList<java.util.UUID>();
                        for (final var result : batch.probeResults()) {
                            probes.accept(result, timing.offsetMs(), now, collector.selection().paths());
                            // Retire even obsolete jobs; never replay an unapproved/expired probe.
                            probeReceipts.add(result.jobId());
                        }
                        return new WeatherAcknowledgement(1, batch.requestId(), epoch, receipts, probeReceipts);
                    }
                }
            } catch (WeatherSourceRegistry.UnavailableException e) {
                deferredBatches.incrementAndGet();
                verified = WeatherReceipt.Disposition.RETRY_LATER;
            } catch (java.security.GeneralSecurityException | IllegalArgumentException | ArithmeticException e) {
                rejectedBatches.incrementAndGet();
                verified = WeatherReceipt.Disposition.REJECTED_INVALID;
            }
            disposition = verified;
        }
        return new WeatherAcknowledgement(1, batch.requestId(), epoch,
                batch.deliveries().stream()
                        .map(delivery -> new WeatherReceipt(delivery.observation().eventId(), epoch, now, disposition))
                        .toList(),
                disposition == WeatherReceipt.Disposition.RETRY_LATER ? List.of()
                        : batch.probeResults().stream().map(WeatherProbe.Result::jobId).toList());
    }

    static Configuration decode(String document) throws IOException {
        checkSize(document);
        final var configuration = JSON.readValue(document, Configuration.class);
        if (configuration == null) {
            throw new IOException("Missing Network Weather configuration document");
        }
        return configuration;
    }

    public static Set<Path> parsePaths(String text) {
        return parsePaths(text, WeatherLimits.configured().maxSelectedPaths());
    }

    public static Set<Path> parsePaths(String text, int maximum) {
        return parsePaths(text, maximum, WeatherLimits.configured().maxConfigurationBytes());
    }

    public static Set<Path> parsePaths(String text, int maximum, int maximumBytes) {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length > maximumBytes) {
            throw new IllegalArgumentException("Missing or oversized manual scope");
        }
        final var paths = new LinkedHashSet<Path>();
        int lineNumber = 0;
        for (final var line : text.split("\\R", -1)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            final var fields = line.split("\\|", -1);
            if (fields.length != 5) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": use source | host ID | target | method | type");
            }
            final Path path;
            try {
                path = new Path(fields[0].trim(), fields[1].trim(), fields[2].trim(), fields[3].trim(),
                        fields[4].trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Line " + lineNumber + ", source " + fields[0].trim() + ", Host "
                        + fields[1].trim() + ": " + e.getMessage(), e);
            }
            if (!paths.add(path)) {
                throw new IllegalArgumentException("Duplicate path on line " + lineNumber);
            }
            if (paths.size() > maximum) {
                throw new IllegalArgumentException("At most " + maximum + " paths are allowed");
            }
        }
        return Set.copyOf(paths);
    }

    public static String formatPaths(List<Path> paths) {
        return paths.stream().map(path -> String.join(" | ", path.sourceId(), path.hostId(), path.configuredTarget(),
                path.protocol(), path.leg())).collect(java.util.stream.Collectors.joining("\n"));
    }

    private static void checkSize(String document) throws IOException {
        if (document.getBytes(StandardCharsets.UTF_8).length > WeatherLimits.configured().maxConfigurationBytes()) {
            throw new IOException("Network Weather configuration exceeds the configured byte limit");
        }
    }
}
