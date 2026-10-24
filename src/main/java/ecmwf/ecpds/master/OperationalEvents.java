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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.technical.Cnf;
import ecmwf.common.telemetry.TransferTracing;
import ecmwf.common.version.Version;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Independent legacy-file and native OpenTelemetry outputs for operational events.
 */
public final class OperationalEvents {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(OperationalEvents.class);
    private static final org.apache.logging.log4j.Logger SPLUNK = LogManager.getLogger("SplunkLogs");
    private static final OperationalEvents INSTANCE = new OperationalEvents(SPLUNK::isInfoEnabled, SPLUNK::info, null);

    private final BooleanSupplier legacyEnabled;
    private final Consumer<String> legacyOutput;
    private volatile boolean splunkEnabled = true;
    private volatile Logger telemetry;
    private OpenTelemetrySdk sdk;
    private volatile OperationalEventJournal journal;
    private String journalOwner;

    OperationalEvents(final BooleanSupplier legacyEnabled, final Consumer<String> legacyOutput,
            final Logger telemetry) {
        this.legacyEnabled = legacyEnabled;
        this.legacyOutput = legacyOutput;
        this.telemetry = telemetry;
    }

    public static OperationalEvents getInstance() {
        return INSTANCE;
    }

    /** Configure once at Master startup, before event-producing schedulers start. */
    public static synchronized void start() {
        start(null);
    }

    public static synchronized void start(final ECpdsBase database) {
        INSTANCE.splunkEnabled = Cnf.at("OperationalEvents", "splunkEnabled", true);
        if (INSTANCE.sdk != null || !Cnf.at("OperationalEvents", "openTelemetryEnabled", false)) {
            return;
        }
        final var journalEnabled = Cnf.at("OperationalEvents", "journalEnabled", false);
        INSTANCE.journal = null;
        if (journalEnabled && database == null) {
            throw new IllegalArgumentException("Operational event journaling requires the Master database");
        }
        final var owner = Cnf.at("OperationalEvents", "journalOwner",
                Cnf.at("Login", "root", Cnf.at("Login", "hostName", "master")));
        if (journalEnabled && (owner.isBlank() || owner.length() > 255)) {
            throw new IllegalArgumentException("Operational event journalOwner must contain 1 to 255 characters");
        }
        INSTANCE.journalOwner = owner;
        final var exporterReference = new AtomicReference<LogRecordExporter>();
        INSTANCE.sdk = AutoConfiguredOpenTelemetrySdk.builder()
                .addPropertiesSupplier(() -> Map.of("otel.service.name", "open-ecpds-master",
                        "otel.exporter.otlp.protocol", "http/protobuf", "otel.logs.exporter", "otlp",
                        "otel.blrp.max.queue.size", Cnf.at("OpenTelemetry", "logsMaxQueueSize", "2048"),
                        "otel.blrp.max.export.batch.size", Cnf.at("OpenTelemetry", "logsMaxExportBatchSize", "512"),
                        "otel.blrp.schedule.delay", Cnf.at("OpenTelemetry", "logsScheduleDelayMillis", "1000")))
                // This private SDK exports operational logs only, not application metrics or traces.
                .addPropertiesCustomizer(properties -> {
                    if (journalEnabled && properties.getBoolean("otel.sdk.disabled", false)) {
                        throw new IllegalArgumentException(
                                "Database event journaling cannot be used with OTEL_SDK_DISABLED=true");
                    }
                    if (journalEnabled && !properties.getList("otel.logs.exporter").equals(List.of("otlp"))) {
                        throw new IllegalArgumentException(
                                "Database event journaling requires OTEL_LOGS_EXPORTER=otlp");
                    }
                    return Map.of("otel.traces.exporter", "none", "otel.metrics.exporter", "none");
                })
                .addResourceCustomizer((resource, _) -> Resource
                        .create(Attributes.builder().put("service.version", Version.getFullVersion())
                                .put("service.instance.id",
                                        Cnf.at("Login", "root", Cnf.at("Login", "hostName", "master")))
                                .build())
                        .merge(resource))
                .addLogRecordExporterCustomizer((exporter, _) -> {
                    exporterReference.set(exporter);
                    return journalEnabled ? exporter : new ReportingExporter(exporter);
                }).addLogRecordProcessorCustomizer((processor, _) -> {
                    if (!journalEnabled) {
                        return processor;
                    }
                    final var journal = new OperationalEventJournal(OperationalEventJournal.database(database, owner),
                            exporterReference.get(), processor, true);
                    INSTANCE.journal = journal;
                    return journal;
                }).disableShutdownHook().build().getOpenTelemetrySdk();
        INSTANCE.telemetry = INSTANCE.sdk.getLogsBridge().get("ecmwf.ecpds.operational-events");
        LOG.info("Native OpenTelemetry operational event export enabled");
        if (journalEnabled) {
            LOG.info("Database operational event journal enabled; unacknowledged events will be retried");
        }
    }

    public static OperationalEventJournalStatus journalStatus(final ECpdsBase database, final boolean includeQueue) {
        final var journal = INSTANCE.journal;
        if (journal == null) {
            return OperationalEventJournalStatus.disabled();
        }
        final var status = journal.status(INSTANCE.journalOwner);
        if (!includeQueue) {
            return status;
        }
        try {
            final var statistics = database.getOperationalEventStatistics(status.owner());
            return status.withQueue(statistics.pendingCount(), statistics.oldestAdmissionAt(), null);
        } catch (final java.sql.SQLException | java.io.IOException e) {
            LOG.warn("Cannot retrieve operational event journal queue statistics", e);
            return status.withQueue(null, null, "Unable to read queue statistics from the Master database.");
        }
    }

    public static synchronized void shutdown() {
        INSTANCE.telemetry = null;
        if (INSTANCE.sdk != null) {
            final var result = INSTANCE.sdk.shutdown().join(10, TimeUnit.SECONDS);
            if (!result.isSuccess()) {
                LOG.warn("OpenTelemetry shutdown failed or timed out; in-memory records may be lost; "
                        + "persisted journal records remain recoverable");
            }
            INSTANCE.sdk = null;
        }
    }

    public boolean isEnabled() {
        return splunkEnabled && legacyEnabled.getAsBoolean() || telemetry != null;
    }

    public static Field field(final String name, final Object value) {
        return new Field(name, value);
    }

    public record Field(String name, Object value) {
    }

    public void emit(final String category, final Field... fields) {
        emit(Context.current(), null, category, fields);
    }

    public void emit(final DataTransfer transfer, final String category, final Field... fields) {
        final var context = TransferTracing.context(transfer);
        emit(context, Span.fromContext(context).getSpanContext().isValid() ? transfer.getTraceAttemptId() : null,
                category, fields);
    }

    private void emit(final Context context, final String attemptId, final String category, final Field... fields) {
        if (splunkEnabled && legacyEnabled.getAsBoolean()) {
            final var line = new StringBuilder(category);
            for (final var field : fields) {
                line.append(';').append(field.name()).append('=').append(field.value());
            }
            legacyOutput.accept(line.toString());
        }
        final var logger = telemetry;
        if (logger == null) {
            return;
        }
        try {
            final var attributes = Attributes.builder().put("ecpds.event.id", UUID.randomUUID().toString())
                    .put("ecpds.event.category", category);
            if (attemptId != null) {
                attributes.put("ecpds.transfer.attempt.id", attemptId);
            }
            var timestamp = Instant.now();
            for (final var field : fields) {
                final var name = field.name();
                switch (field.value()) {
                case final Boolean value -> attributes.put(name, value);
                case final Byte value -> attributes.put(name, value.longValue());
                case final Short value -> attributes.put(name, value.longValue());
                case final Integer value -> attributes.put(name, value.longValue());
                case final Long value -> attributes.put(name, value.longValue());
                case final Float value -> attributes.put(name, value.doubleValue());
                case final Double value -> attributes.put(name, value.doubleValue());
                case null -> attributes.put(name, "null");
                default -> attributes.put(name, field.value().toString());
                }
                if ("TimeStamp".equals(name) && field.value() instanceof final Timestamp value) {
                    timestamp = value.toInstant();
                }
            }
            logger.logRecordBuilder().setContext(context).setTimestamp(timestamp).setSeverity(Severity.INFO)
                    .setEventName("ecpds." + category).setBody(category).setAllAttributes(attributes.build()).emit();
        } catch (final RuntimeException e) {
            LOG.error("Failed to emit OpenTelemetry operational event {}", category, e);
        }
    }

    private static final class ReportingExporter implements LogRecordExporter {
        private final LogRecordExporter delegate;
        private final AtomicLong failedRecords = new AtomicLong();
        private final AtomicLong lastWarning = new AtomicLong();

        private ReportingExporter(final LogRecordExporter delegate) {
            this.delegate = delegate;
        }

        @Override
        public CompletableResultCode export(final Collection<LogRecordData> logs) {
            final var result = delegate.export(logs);
            result.whenComplete(() -> {
                if (!result.isSuccess()) {
                    failedRecords.addAndGet(logs.size());
                    final var now = System.nanoTime();
                    final var previous = lastWarning.get();
                    if ((previous == 0 || now - previous >= TimeUnit.MINUTES.toNanos(1))
                            && lastWarning.compareAndSet(previous, now)) {
                        LOG.warn(
                                "OpenTelemetry export failed: {} operational records lost since the last warning; "
                                        + "check Collector availability, authentication and SDK diagnostics",
                                failedRecords.getAndSet(0));
                    }
                }
            });
            return result;
        }

        @Override
        public CompletableResultCode flush() {
            return delegate.flush();
        }

        @Override
        public CompletableResultCode shutdown() {
            final var result = delegate.shutdown();
            result.whenComplete(() -> {
                final var remaining = failedRecords.getAndSet(0);
                if (remaining > 0) {
                    LOG.warn("OpenTelemetry exporter shutdown: {} additional operational records failed export",
                            remaining);
                }
            });
            return result;
        }
    }
}
