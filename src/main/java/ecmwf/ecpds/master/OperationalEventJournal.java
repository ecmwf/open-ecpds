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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.ECpdsBase;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Synchronous database admission followed by asynchronous, acknowledgement-based export. One active worker is required
 * per journal owner; unacknowledged records are never expired.
 */
final class OperationalEventJournal implements LogRecordProcessor {
    private static final Logger LOG = LogManager.getLogger(OperationalEventJournal.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final AttributeKey<String> EVENT_ID = AttributeKey.stringKey("ecpds.event.id");

    interface Store {
        void append(String id, String payload) throws SQLException, IOException;

        Map<String, String> pending(int limit) throws SQLException, IOException;

        void acknowledge(String id) throws SQLException, IOException;
    }

    static Store database(final ECpdsBase database, final String owner) {
        return new Store() {
            @Override
            public void append(final String id, final String payload) throws SQLException, IOException {
                database.insertOperationalEvent(owner, id, payload);
            }

            @Override
            public Map<String, String> pending(final int limit) throws SQLException, IOException {
                return database.getOperationalEvents(owner, limit);
            }

            @Override
            public void acknowledge(final String id) throws SQLException, IOException {
                database.deleteOperationalEvent(owner, id);
            }
        };
    }

    private final Store store;
    private final LogRecordExporter exporter;
    private final LogRecordProcessor fallback;
    private final ScheduledExecutorService worker;
    private final AtomicBoolean stopped = new AtomicBoolean();
    private final AtomicLong lastPersistenceError = new AtomicLong();
    private final AtomicLong persistenceFailures = new AtomicLong();
    private final AtomicLong lastExportError = new AtomicLong();
    private List<String> inFlightIds;
    private CompletableResultCode inFlight;
    private long retryAfter;

    OperationalEventJournal(final Store store, final LogRecordExporter exporter, final LogRecordProcessor fallback,
            final boolean startWorker) {
        this.store = store;
        this.exporter = exporter;
        this.fallback = fallback;
        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            final var thread = new Thread(task, "OperationalEventJournal");
            thread.setDaemon(true);
            return thread;
        });
        if (startWorker) {
            worker.scheduleWithFixedDelay(this::deliver, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onEmit(final Context context, final ReadWriteLogRecord record) {
        final var existingId = record.getAttribute(EVENT_ID);
        final var id = existingId != null ? existingId : UUID.randomUUID().toString();
        record.setAttribute(EVENT_ID, id);
        if (stopped.get()) {
            LOG.error("Operational event {} emitted after journal shutdown; attempting best-effort delivery", id);
            fallback.onEmit(context, record);
            return;
        }
        try {
            store.append(id, encode(record.toLogRecordData()));
        } catch (final SQLException | IOException | RuntimeException e) {
            persistenceFailures.incrementAndGet();
            if (shouldReport(lastPersistenceError)) {
                LOG.error(
                        "DATABASE EVENT JOURNAL UNAVAILABLE: {} events could not be persisted since the last "
                                + "report; operations continue with best-effort delivery only; current event {}",
                        persistenceFailures.getAndSet(0), id, e);
            }
            fallback.onEmit(context, record);
        }
    }

    /** Package-private for deterministic recovery tests; production uses one scheduled worker. */
    synchronized void deliver() {
        if (stopped.get()) {
            return;
        }
        if (retryAfter != 0 && System.nanoTime() - retryAfter < 0) {
            return;
        }
        try {
            if (inFlight != null) {
                if (!inFlight.isDone()) {
                    return;
                }
                if (inFlight.isSuccess()) {
                    for (final var id : inFlightIds) {
                        store.acknowledge(id);
                    }
                } else {
                    if (shouldReport(lastExportError)) {
                        LOG.warn(
                                "OpenTelemetry journal export failed: {} records retained for retry; "
                                        + "check endpoint availability, authentication and SDK diagnostics",
                                inFlightIds.size());
                    }
                    inFlight = null;
                    inFlightIds = null;
                    retryAfter = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                    return;
                }
                inFlight = null;
                inFlightIds = null;
            }
            final var pending = store.pending(256);
            if (pending.isEmpty()) {
                return;
            }
            final var records = new ArrayList<LogRecordData>(pending.size());
            for (final var entry : pending.entrySet()) {
                try {
                    records.add(decode(entry.getValue()));
                } catch (final IOException | RuntimeException e) {
                    throw new IOException(
                            "Cannot decode journal event " + entry.getKey() + "; retained for operator investigation",
                            e);
                }
            }
            inFlightIds = List.copyOf(pending.keySet());
            if (stopped.get()) {
                return;
            }
            inFlight = exporter.export(records);
        } catch (final SQLException | IOException | RuntimeException e) {
            retryAfter = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            if (shouldReport(lastExportError)) {
                LOG.error("Operational event journal delivery failed; unacknowledged records retained", e);
            }
        }
    }

    private static boolean shouldReport(final AtomicLong lastReport) {
        final var now = System.nanoTime();
        final var previous = lastReport.get();
        return (previous == 0 || now - previous >= TimeUnit.MINUTES.toNanos(1))
                && lastReport.compareAndSet(previous, now);
    }

    @Override
    public CompletableResultCode forceFlush() {
        // Persisted records need not be delivered before shutdown to survive a restart.
        return fallback.forceFlush();
    }

    @Override
    public CompletableResultCode shutdown() {
        stopped.set(true);
        worker.shutdownNow();
        final var failures = persistenceFailures.getAndSet(0);
        if (failures > 0) {
            LOG.error("Event journal shutdown: {} additional events could not be persisted", failures);
        }
        // The original SDK processor owns the exporter and drains any best-effort fallback records.
        return fallback.shutdown();
    }

    static String encode(final LogRecordData record) throws IOException {
        final var node = JSON.createObjectNode();
        node.put("version", 1);
        node.put("timestamp", record.getTimestampEpochNanos());
        node.put("observed", record.getObservedTimestampEpochNanos());
        node.put("event", record.getEventName());
        node.put("body", record.getBodyValue().asString());
        node.put("severity", record.getSeverity().name());
        node.put("severityText", record.getSeverityText());
        node.put("totalAttributes", record.getTotalAttributeCount());
        node.set("attributes", encodeAttributes(record.getAttributes()));
        node.set("resource", encodeAttributes(record.getResource().getAttributes()));
        node.put("resourceSchema", record.getResource().getSchemaUrl());
        final var scope = record.getInstrumentationScopeInfo();
        node.put("scope", scope.getName());
        node.put("scopeVersion", scope.getVersion());
        node.put("scopeSchema", scope.getSchemaUrl());
        node.set("scopeAttributes", encodeAttributes(scope.getAttributes()));
        final var span = record.getSpanContext();
        node.put("trace", span.getTraceId());
        node.put("span", span.getSpanId());
        node.put("flags", span.getTraceFlags().asByte());
        final var state = node.putObject("traceState");
        span.getTraceState().forEach(state::put);
        return JSON.writeValueAsString(node);
    }

    private static ObjectNode encodeAttributes(final Attributes attributes) {
        final var node = JSON.createObjectNode();
        attributes.forEach((key, value) -> {
            final var attribute = node.putObject(key.getKey());
            attribute.put("type", key.getType().name());
            attribute.set("value", JSON.valueToTree(value));
        });
        return node;
    }

    private static Attributes decodeAttributes(final JsonNode node) throws IOException {
        final var attributes = Attributes.builder();
        for (final var entry : node.properties()) {
            final var name = entry.getKey();
            final var value = entry.getValue().required("value");
            switch (entry.getValue().required("type").asText()) {
            case "STRING" -> attributes.put(name, value.asText());
            case "BOOLEAN" -> attributes.put(name, value.booleanValue());
            case "LONG" -> attributes.put(name, value.longValue());
            case "DOUBLE" -> attributes.put(name, Double.parseDouble(value.asText()));
            case "STRING_ARRAY" -> attributes.put(AttributeKey.stringArrayKey(name),
                    Arrays.asList(JSON.treeToValue(value, String[].class)));
            case "BOOLEAN_ARRAY" -> attributes.put(AttributeKey.booleanArrayKey(name),
                    Arrays.asList(JSON.treeToValue(value, Boolean[].class)));
            case "LONG_ARRAY" -> attributes.put(AttributeKey.longArrayKey(name),
                    Arrays.asList(JSON.treeToValue(value, Long[].class)));
            case "DOUBLE_ARRAY" -> attributes.put(AttributeKey.doubleArrayKey(name),
                    Arrays.asList(JSON.treeToValue(value, Double[].class)));
            default -> throw new IOException("Unsupported journal attribute type");
            }
        }
        return attributes.build();
    }

    static LogRecordData decode(final String payload) throws IOException {
        final var node = JSON.readTree(payload);
        if (node.required("version").intValue() != 1) {
            throw new IOException("Unsupported operational event journal version");
        }
        final var attributes = decodeAttributes(node.required("attributes"));
        final var resource = Resource.create(decodeAttributes(node.required("resource")),
                nullableText(node, "resourceSchema"));
        final var scope = InstrumentationScopeInfo.builder(node.required("scope").asText())
                .setVersion(nullableText(node, "scopeVersion")).setSchemaUrl(nullableText(node, "scopeSchema"))
                .setAttributes(decodeAttributes(node.required("scopeAttributes"))).build();
        final var state = TraceState.builder();
        node.required("traceState").properties().forEach(entry -> state.put(entry.getKey(), entry.getValue().asText()));
        final var span = SpanContext.create(node.required("trace").asText(), node.required("span").asText(),
                TraceFlags.fromByte((byte) node.required("flags").intValue()), state.build());
        return new LogRecordData() {
            @Override
            public Resource getResource() {
                return resource;
            }

            @Override
            public InstrumentationScopeInfo getInstrumentationScopeInfo() {
                return scope;
            }

            @Override
            public long getTimestampEpochNanos() {
                return node.required("timestamp").longValue();
            }

            @Override
            public long getObservedTimestampEpochNanos() {
                return node.required("observed").longValue();
            }

            @Override
            public SpanContext getSpanContext() {
                return span;
            }

            @Override
            public Severity getSeverity() {
                return Severity.valueOf(node.required("severity").asText());
            }

            @Override
            public String getSeverityText() {
                return nullableText(node, "severityText");
            }

            @Override
            public Body getBody() {
                return Body.string(node.required("body").asText());
            }

            @Override
            public Attributes getAttributes() {
                return attributes;
            }

            @Override
            public int getTotalAttributeCount() {
                return node.required("totalAttributes").intValue();
            }

            @Override
            public String getEventName() {
                return node.required("event").asText();
            }
        };
    }

    private static String nullableText(final JsonNode node, final String name) {
        final var value = node.required(name);
        return value.isNull() ? null : value.asText();
    }
}
