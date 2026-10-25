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
package ecmwf.common.telemetry;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bounded, SDK-independent wire representation of a completed Mover span. Receivers must call {@link #snapshot()}
 * before admitting a deserialized bean. Only operational identifiers and counters are permitted, never URLs or errors.
 */
public final class TransferSpan implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_BATCH = 256;
    public static final int MAX_BATCH_BYTES = 4 * 1024 * 1024;
    private static final Set<String> NUMBERS = Set.of("ecpds.transfer.id", "ecpds.file.id",
            "ecpds.transfer.start_count", "ecpds.transfer.requeue_count", "ecpds.transfer.bytes_sent",
            "ecpds.retrieval.attempt", "ecpds.retrieval.bytes");
    private static final Set<String> STRINGS = Set.of("ecpds.transfer.attempt.id", "ecpds.destination.name",
            "ecpds.host.id", "ecpds.retrieval.id", "ecpds.mover.name");
    private static final Set<String> RESOURCES = Set.of("service.name", "service.version", "service.instance.id");
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String traceState;
    private int traceFlags = 1;
    private String name = "ecpds.transfer.execute";
    private String kind = "CONSUMER";
    private long startEpochNanos;
    private long endEpochNanos;
    private boolean failed;
    private Map<String, Object> attributes = Map.of();
    private Map<String, String> resource = Map.of();
    private boolean frozen;

    public TransferSpan() {
    }

    /** Validate and defensively detach all mutable content, including Java/RMI input. */
    public TransferSpan snapshot() {
        final var copy = new TransferSpan();
        copy.setTraceId(traceId);
        copy.setSpanId(spanId);
        copy.setParentSpanId(parentSpanId);
        copy.setTraceState(traceState);
        copy.setTraceFlags(traceFlags);
        copy.setName(name);
        copy.setKind(kind);
        if (startEpochNanos <= 0 || endEpochNanos < startEpochNanos) {
            throw new IllegalArgumentException("Invalid span timing");
        }
        copy.startEpochNanos = startEpochNanos;
        copy.endEpochNanos = endEpochNanos;
        copy.failed = failed;
        copy.setAttributes(attributes);
        copy.setResource(resource);
        final var transferSpan = "ecpds.transfer.execute".equals(copy.name) && "CONSUMER".equals(copy.kind)
                && copy.attributes.containsKey("ecpds.transfer.id")
                && copy.attributes.containsKey("ecpds.transfer.attempt.id");
        final var retrievalSpan = "ecpds.retrieval.execute".equals(copy.name) && "SERVER".equals(copy.kind)
                && copy.attributes.containsKey("ecpds.retrieval.id") && copy.attributes.containsKey("ecpds.file.id")
                && copy.attributes.containsKey("ecpds.retrieval.attempt");
        if (!transferSpan && !retrievalSpan || !"open-ecpds-mover".equals(copy.resource.get("service.name"))
                || !copy.resource.containsKey("service.instance.id")) {
            throw new IllegalArgumentException("Missing span identifiers or mover resource");
        }
        copy.frozen = true;
        return copy;
    }

    static List<TransferSpan> snapshots(final List<TransferSpan> spans) {
        if (spans == null || spans.isEmpty() || spans.size() > MAX_BATCH) {
            throw new IllegalArgumentException("Transfer span batch must contain 1.." + MAX_BATCH + " records");
        }
        return spans.stream().map(span -> {
            if (span == null) {
                throw new IllegalArgumentException("Null transfer span");
            }
            return span.snapshot();
        }).toList();
    }

    static String text(final String value, final int limit) {
        if (value == null || value.isEmpty() || value.length() > limit
                || value.chars().anyMatch(c -> c < 32 || c == 127)) {
            throw new IllegalArgumentException("Invalid bounded transfer span text");
        }
        return value;
    }

    static String id(final String value, final int length) {
        if (value == null || value.length() != length || !value.matches("[0-9a-f]+")
                || value.chars().allMatch(c -> c == '0')) {
            throw new IllegalArgumentException("Invalid transfer span identifier");
        }
        return value;
    }

    static void validateTraceState(final String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (value.length() > 512 || value.chars().anyMatch(c -> c < 32 && c != '\t' || c > 126)) {
            throw new IllegalArgumentException("Oversized tracestate");
        }
        final var keys = new java.util.HashSet<String>();
        final var members = value.split(",", -1);
        if (members.length > 32) {
            throw new IllegalArgumentException("Too many tracestate members");
        }
        for (final var member : members) {
            final var entry = member.strip();
            final var equals = entry.indexOf('=');
            if (equals <= 0) {
                throw new IllegalArgumentException("Invalid tracestate entry");
            }
            final var key = entry.substring(0, equals);
            final var content = entry.substring(equals + 1);
            if (key.length() > 256
                    || !key.matches("(?:[a-z][a-z0-9_*/-]{0,255}|[a-z0-9][a-z0-9_*/-]{0,240}@[a-z][a-z0-9_*/-]{0,13})")
                    || !keys.add(key) || content.isEmpty() || content.length() > 256 || content.endsWith(" ")
                    || content.chars().anyMatch(c -> c < 32 || c > 126 || c == '=' || c == ',')) {
                throw new IllegalArgumentException("Invalid tracestate");
            }
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(final String value) {
        mutable();
        traceId = id(value, 32);
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(final String value) {
        mutable();
        spanId = id(value, 16);
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(final String value) {
        mutable();
        parentSpanId = id(value, 16);
    }

    public String getTraceState() {
        return traceState;
    }

    public void setTraceState(final String value) {
        mutable();
        validateTraceState(value);
        traceState = value;
    }

    public int getTraceFlags() {
        return traceFlags;
    }

    public void setTraceFlags(final int value) {
        mutable();
        if (value < 0 || value > 255 || (value & 1) == 0) {
            throw new IllegalArgumentException("Only sampled spans can be delivered");
        }
        traceFlags = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        mutable();
        if (!"ecpds.transfer.execute".equals(value) && !"ecpds.retrieval.execute".equals(value)) {
            throw new IllegalArgumentException("Unexpected span name");
        }
        name = value;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(final String value) {
        mutable();
        if (!"CONSUMER".equals(value) && !"SERVER".equals(value)) {
            throw new IllegalArgumentException("Unsupported span kind");
        }
        kind = value;
    }

    public long getStartEpochNanos() {
        return startEpochNanos;
    }

    public void setStartEpochNanos(final long value) {
        mutable();
        startEpochNanos = value;
    }

    public long getEndEpochNanos() {
        return endEpochNanos;
    }

    public void setEndEpochNanos(final long value) {
        mutable();
        endEpochNanos = value;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(final boolean value) {
        mutable();
        failed = value;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, Object> values) {
        mutable();
        if (values == null || values.size() > NUMBERS.size() + STRINGS.size()) {
            throw new IllegalArgumentException("Invalid transfer span attributes");
        }
        final var copy = new LinkedHashMap<String, Object>();
        values.forEach((key, value) -> {
            if (key == null) {
                throw new IllegalArgumentException("Null transfer span attribute key");
            }
            if (NUMBERS.contains(key) && (value instanceof Long || value instanceof Integer)) {
                copy.put(key, ((Number) value).longValue());
            } else if (STRINGS.contains(key) && value instanceof String string) {
                copy.put(key, text(string, "ecpds.transfer.attempt.id".equals(key) ? 64 : 256));
            } else {
                throw new IllegalArgumentException("Unsupported transfer span attribute");
            }
        });
        attributes = Map.copyOf(copy);
    }

    public Map<String, String> getResource() {
        return resource;
    }

    public void setResource(final Map<String, String> values) {
        mutable();
        if (values == null || values.size() > RESOURCES.size()) {
            throw new IllegalArgumentException("Invalid transfer span resource");
        }
        final var copy = new LinkedHashMap<String, String>();
        values.forEach((key, value) -> {
            if (key == null || !RESOURCES.contains(key)) {
                throw new IllegalArgumentException("Unsupported transfer span resource");
            }
            copy.put(key, text(value, 256));
        });
        resource = Map.copyOf(copy);
    }

    private void mutable() {
        if (frozen) {
            throw new IllegalStateException("Transfer span snapshot is immutable");
        }
    }
}
