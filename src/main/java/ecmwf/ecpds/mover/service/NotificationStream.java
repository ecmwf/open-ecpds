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
 * In applying this license, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */
package ecmwf.ecpds.mover.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.NotificationHistory;

/** Emits the Aviso SSE wire format for replay and live file-available events. */
final class NotificationStream {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String TOPIC = "file_available";
    private static final long HEARTBEAT_MILLIS = 15000;

    @FunctionalInterface
    interface Reader {
        NotificationHistory.Replay read(long after, Long upper, Long fromTime, Long toTime) throws Exception;
    }

    private static void send(final OutputStream out, final String type, final Object data) throws IOException {
        out.write(("event: " + type + "\ndata: " + JSON.writeValueAsString(data) + "\n\n")
                .getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static void sendNotification(final OutputStream out, final NotificationHistoryStoreEntry entry,
            final String eventType, final String baseUrl) throws IOException {
        final var original = entry.data;
        final var data = JSON.createObjectNode();
        final var identifier = data.putObject("identifier");
        identifier.put("destination", entry.destination);
        identifier.put("target", entry.target);
        final var payload = data.putObject("payload");
        final var originalData = original.path("data");
        originalData.fields().forEachRemaining(field -> {
            if (!"destination".equals(field.getKey()) && !"target".equals(field.getKey())) {
                payload.set(field.getKey(), field.getValue());
            }
        });

        final var event = JSON.createObjectNode();
        event.put("specversion", "1.0");
        event.put("id", "file_available@" + entry.sequence);
        event.put("source", baseUrl);
        event.put("type", "int.ecmwf.aviso.file_available");
        event.put("time", Instant.ofEpochMilli(entry.storedAt).toString());
        event.put("datacontenttype", "application/json");
        event.put("dataschema", baseUrl + "/schema/file_available");
        event.set("data", data);
        send(out, eventType, event);
    }

    private record NotificationHistoryStoreEntry(long sequence, long storedAt, String destination, String target,
            JsonNode data) {
    }

    private static NotificationHistoryStoreEntry convert(
            final ecmwf.common.database.NotificationHistoryStore.Entry entry, final String destination)
            throws IOException {
        return new NotificationHistoryStoreEntry(entry.sequence(), entry.storedAt(), destination, entry.target(),
                JSON.readTree(entry.event()));
    }

    private static void sendPage(final OutputStream out, final NotificationHistory.Replay page,
            final String destination, final String eventType, final String baseUrl) throws IOException {
        for (final var entry : page.entries()) {
            sendNotification(out, convert(entry, destination), eventType, baseUrl);
        }
    }

    private static void sendReplayControl(final OutputStream out, final String type, final Long fromSequence,
            final String fromDate, final Long endSequence, final String toDate, final int batchSize,
            final String requestId) throws IOException {
        if ("replay_completed".equals(type)) {
            send(out, "replay-control", Map.of("type", type, "topic", TOPIC, "timestamp",
                    Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()));
            return;
        }
        final var data = new LinkedHashMap<String, Object>();
        data.put("type", type);
        data.put("topic", TOPIC);
        data.put("from_sequence", fromSequence);
        data.put("from_date", fromDate);
        data.put("end_sequence", endSequence);
        data.put("to_date", toDate);
        data.put("batch_size", batchSize);
        data.put("timestamp", Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString());
        data.put("request_id", requestId);
        send(out, "replay-control", data);
    }

    private static String timestamp(final long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
    }

    private static void sendConnectionEstablished(final OutputStream out, final long durationMillis,
            final String requestId) throws IOException {
        send(out, "live-notification",
                Map.of("type", "connection_established", "topic", TOPIC, "timestamp",
                        Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(),
                        "connection_will_close_in_seconds", durationMillis / 1000, "request_id", requestId));
    }

    private static void sendError(final OutputStream out, final String message, final String requestId)
            throws IOException {
        send(out, "error", Map.of("error", "stream_processing_failed", "message", message, "topic", TOPIC, "request_id",
                requestId));
    }

    private static void sendClosing(final OutputStream out, final String reason, final String message,
            final String requestId) throws IOException {
        send(out, "connection-closing",
                Map.of("reason", reason, "timestamp",
                        Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(), "message", message,
                        "topic", TOPIC, "request_id", requestId));
    }

    static void write(final OutputStream out, final Reader reader, final NotificationHistory.Replay initial,
            final String destination, final long after, final Long fromSequence, final Long fromTime, final Long upper,
            final Long toTime, final boolean watch, final String baseUrl, final long pollMillis,
            final long durationMillis) throws IOException {
        final var requestId = UUID.randomUUID().toString();
        final var deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(durationMillis);
        var page = initial;
        var cursor = after;
        final var replay = fromSequence != null || fromTime != null;

        if (replay) {
            final var replayCeiling = initial.ceiling();
            final var replayUpper = upper == null ? replayCeiling : Math.min(upper, replayCeiling);
            sendReplayControl(out, "replay_started", fromSequence, fromTime == null ? null : timestamp(fromTime), upper,
                    toTime == null ? null : timestamp(toTime), 100, requestId);
            while (true) {
                if (System.nanoTime() >= deadline) {
                    sendClosing(out, "max_duration_reached", "Connection reached maximum duration", requestId);
                    return;
                }
                if (page.historyGap()) {
                    sendError(out, "Requested notification history is no longer available", requestId);
                    return;
                }
                sendPage(out, page, destination, "replay", baseUrl);
                cursor = page.scannedThrough();
                if (!page.hasMore()) {
                    break;
                }
                page = read(reader, cursor, replayUpper, fromTime, toTime, out, requestId);
                if (page == null) {
                    return;
                }
            }
            sendReplayControl(out, "replay_completed", null, null, null, null, 0, requestId);
        } else {
            cursor = initial.ceiling();
        }

        if (!watch) {
            sendClosing(out, "end_of_stream", "Stream completed", requestId);
            return;
        }
        sendConnectionEstablished(out, durationMillis, requestId);

        var lastHeartbeat = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            final var remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                sendClosing(out, "max_duration_reached", "Connection reached maximum duration", requestId);
                return;
            }
            try {
                TimeUnit.NANOSECONDS.sleep(Math.min(TimeUnit.MILLISECONDS.toNanos(pollMillis), remaining));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (System.nanoTime() - lastHeartbeat >= TimeUnit.MILLISECONDS.toNanos(HEARTBEAT_MILLIS)) {
                send(out, "heartbeat", Map.of("timestamp",
                        Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(), "topic", TOPIC));
                lastHeartbeat = System.nanoTime();
            }
            page = read(reader, cursor, null, null, null, out, requestId);
            if (page == null) {
                return;
            }
            final var liveCeiling = page.ceiling();
            while (true) {
                if (page.historyGap()) {
                    sendError(out, "Notification history gap detected", requestId);
                    return;
                }
                sendPage(out, page, destination, "live-notification", baseUrl);
                cursor = page.scannedThrough();
                if (!page.hasMore()) {
                    break;
                }
                page = read(reader, cursor, liveCeiling, null, null, out, requestId);
                if (page == null) {
                    return;
                }
            }
        }
    }

    private static NotificationHistory.Replay read(final Reader reader, final long cursor, final Long upper,
            final Long fromTime, final Long toTime, final OutputStream out, final String requestId) throws IOException {
        try {
            return reader.read(cursor, upper, fromTime, toTime);
        } catch (final Exception e) {
            sendError(out, "Notification replay is unavailable", requestId);
            return null;
        }
    }

    private NotificationStream() {
    }
}
