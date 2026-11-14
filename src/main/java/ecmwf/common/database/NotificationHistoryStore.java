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
package ecmwf.common.database;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Persistent notification history. The singleton state row serializes all writers and retention. */
public final class NotificationHistoryStore {
    private static final Logger _log = LogManager.getLogger(NotificationHistoryStore.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, CheckpointValue>> CHECKPOINT_TYPE = new TypeReference<>() {
    };
    private static final String REVISION_TRIGGER_BODY = "setnew.dat_notification_revision="
            + "if(old.dat_notification_revision>0andnew.dat_notification_revision=old.dat_notification_revision,"
            + "old.dat_notification_revision+1,new.dat_notification_revision)";
    private final DataBase database;

    public NotificationHistoryStore(final DataBase database) {
        this.database = java.util.Objects.requireNonNull(database);
    }

    public record Entry(long sequence, long storedAt, String target, String event) implements Serializable {
    }

    public record Page(long ceiling, long removedThrough, long removedAt, long scannedThrough, boolean hasMore,
            boolean historyGap, List<Entry> entries) implements Serializable {
    }

    public long currentSequence() {
        return transaction(transaction -> lock(transaction).next);
    }

    private record State(long next, long bytes, long time) {
    }

    private record Old(long sequence, String destination, long streamSequence, long time, int bytes) {
    }

    public record CheckpointValue(long sequence, long updated) {
    }

    private record CheckpointRow(String user, String destination, String values) {
    }

    private record FencedResult(long revision, boolean duplicate) {
    }

    private <T> T transaction(final Broker.TransactionWork<T> work) {
        return database.transaction(work);
    }

    private static State lock(final Broker.Transaction transaction) throws SQLException {
        try (final var query = transaction.prepare("""
                SELECT NHS_NEXT_SEQUENCE,NHS_PAYLOAD_BYTES,NHS_LAST_TIME
                FROM NOTIFICATION_HISTORY_STATE WHERE NHS_ID=1 FOR UPDATE
                """); final var rows = query.executeQuery()) {
            if (!rows.next()) {
                throw new SQLException("Notification history state is missing");
            }
            return new State(rows.getLong(1), rows.getLong(2), rows.getLong(3));
        }
    }

    static long now(final Broker.Transaction transaction, final long previous) throws SQLException {
        try (final var query = transaction.prepare("SELECT UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000");
                final var rows = query.executeQuery()) {
            rows.next();
            return Math.max(previous, rows.getLong(1));
        }
    }

    /** Remove at most one bounded prefix; callers must hold the state-row lock. */
    private static long prune(final Broker.Transaction transaction, long bytes, final long cutoff, final long maxBytes)
            throws SQLException {
        if (bytes <= maxBytes) {
            try (final var query = transaction.prepare("""
                    SELECT NH_STORED_AT FROM NOTIFICATION_HISTORY ORDER BY NH_SEQUENCE LIMIT 1
                    """); final var rows = query.executeQuery()) {
                if (!rows.next() || rows.getLong(1) >= cutoff) {
                    return bytes;
                }
            }
        }
        final List<Old> oldest = new ArrayList<>();
        try (final var query = transaction.prepare("""
                SELECT NH_SEQUENCE,DES_NAME,NH_STREAM_SEQUENCE,NH_STORED_AT,NH_PAYLOAD_BYTES
                FROM NOTIFICATION_HISTORY ORDER BY NH_SEQUENCE LIMIT 1000
                """); final var rows = query.executeQuery()) {
            while (rows.next()) {
                oldest.add(
                        new Old(rows.getLong(1), rows.getString(2), rows.getLong(3), rows.getLong(4), rows.getInt(5)));
            }
        }
        long through = 0;
        final Map<String, Old> removed = new LinkedHashMap<>();
        for (final var old : oldest) {
            if (old.time >= cutoff && bytes <= maxBytes) {
                break;
            }
            through = old.sequence;
            bytes -= old.bytes;
            removed.put(old.destination, old);
        }
        if (through != 0) {
            try (final var query = transaction.prepare("DELETE FROM NOTIFICATION_HISTORY WHERE NH_SEQUENCE<=?",
                    through)) {
                query.executeUpdate();
            }
            for (final var entry : removed.entrySet()) {
                try (final var query = transaction.prepare("""
                        UPDATE NOTIFICATION_HISTORY_STREAM
                        SET NHS_REMOVED_SEQUENCE=?,NHS_REMOVED_EVENT_SEQUENCE=?,NHS_REMOVED_EVENT_TIME=?
                        WHERE DES_NAME=?
                        """, entry.getValue().streamSequence, entry.getValue().sequence, entry.getValue().time,
                        entry.getKey())) {
                    query.executeUpdate();
                }
            }
        }
        return bytes;
    }

    private static int validateEvent(final String destination, final String target, final String event,
            final long retentionMillis, final long maxBytes) {
        if (event == null) {
            throw new IllegalArgumentException("Invalid notification or history limit");
        }
        final int length = event.getBytes(StandardCharsets.UTF_8).length;
        if (destination == null || destination.isBlank() || destination.length() > 32 || target == null
                || target.length() > 256 || length > maxBytes || length > 65536 || retentionMillis <= 0
                || maxBytes <= 0) {
            throw new IllegalArgumentException("Invalid notification or history limit");
        }
        return length;
    }

    public void append(final String destination, final String target, final String event, final long retentionMillis,
            final long maxBytes) {
        append(destination, target, event, retentionMillis, maxBytes, null);
    }

    public void append(final String destination, final String target, final String event, final long retentionMillis,
            final long maxBytes, final NotificationOutboxStore.Request outbox) {
        final var length = validateEvent(destination, target, event, retentionMillis, maxBytes);
        final var eventId = eventId(event);
        transaction(transaction -> {
            if (append(transaction, destination, target, event, eventId, retentionMillis, maxBytes, length)) {
                NotificationOutboxStore.enqueue(transaction, outbox, event);
            }
            return null;
        });
    }

    /** Commit a transfer update and notification atomically, without checking for stale transfer state. */
    public void appendWithTransfer(final DataTransfer transfer, final String event, final long retentionMillis,
            final long maxBytes) {
        appendWithTransfer(transfer, event, retentionMillis, maxBytes, null);
    }

    public void appendWithTransfer(final DataTransfer transfer, final String event, final long retentionMillis,
            final long maxBytes, final NotificationOutboxStore.Request outbox) {
        appendWithTransferChecked(transfer, null, event, retentionMillis, maxBytes, outbox, false);
    }

    /**
     * Commit a staged transfer only if its persisted fields still match a separate, unchanged snapshot. This
     * precondition does not fence writes made after this transaction.
     */
    public void appendWithTransfer(final DataTransfer transfer, final DataTransfer expected, final String event,
            final long retentionMillis, final long maxBytes, final NotificationOutboxStore.Request outbox) {
        if (expected == null || expected == transfer || expected.getId() != transfer.getId()) {
            throw new IllegalArgumentException("Invalid notification transfer snapshot");
        }
        appendWithTransferChecked(transfer, expected, event, retentionMillis, maxBytes, outbox, false);
    }

    /** Commit a transfer update and notification atomically, guarded by its persisted revision. */
    public boolean appendWithTransferFenced(final DataTransfer transfer, final String event, final long retentionMillis,
            final long maxBytes, final NotificationOutboxStore.Request outbox) {
        return appendWithTransferChecked(transfer, null, event, retentionMillis, maxBytes, outbox, true);
    }

    /** Commit a transfer update and notification atomically, guarded by its revision and an unchanged snapshot. */
    public boolean appendWithTransferFenced(final DataTransfer transfer, final DataTransfer expected,
            final String event, final long retentionMillis, final long maxBytes,
            final NotificationOutboxStore.Request outbox) {
        if (expected == null || expected == transfer) {
            throw new IllegalArgumentException("Invalid notification transfer snapshot");
        }
        final DataTransfer snapshot;
        synchronized (expected) {
            snapshot = (DataTransfer) expected.clone();
        }
        synchronized (transfer) {
            if (snapshot.getId() != transfer.getId() || snapshot.notificationRevision < 0
                    || snapshot.notificationRevision != transfer.notificationRevision) {
                throw new IllegalArgumentException("Invalid notification transfer snapshot");
            }
            return appendWithTransferChecked(transfer, snapshot, event, retentionMillis, maxBytes, outbox, true);
        }
    }

    private boolean appendWithTransferChecked(final DataTransfer transfer, final DataTransfer expected,
            final String event, final long retentionMillis, final long maxBytes,
            final NotificationOutboxStore.Request outbox, final boolean fence) {
        final var destination = transfer.getDestinationName();
        final var target = transfer.getTarget();
        final var length = validateEvent(destination, target, event, retentionMillis, maxBytes);
        final var eventId = eventId(event);
        final FencedResult result;
        try {
            result = transaction(current -> {
                if (fence) {
                    if (!current.supportsTransferRevisionFencing()) {
                        throw new SQLException("Notification transfer revision mapping is not enabled");
                    }
                    validateTransferRevisionSchema(current);
                }
                try (final var query = current.prepare("SELECT DAT_ID FROM DATA_TRANSFER WHERE DAT_ID=? FOR UPDATE",
                        transfer.getId()); final var rows = query.executeQuery()) {
                    if (!rows.next()) {
                        throw new SQLException("Notification transfer no longer exists");
                    }
                }
                if (fence && eventId != null && eventRecorded(current, eventId)) {
                    final var stored = current.get(new DataTransfer(transfer.getId()));
                    if (stored == null) {
                        throw new SQLException("Duplicate notification transfer no longer exists");
                    }
                    return new FencedResult(stored.notificationRevision, true);
                }
                if (expected != null) {
                    final var stored = current.get(new DataTransfer(transfer.getId()));
                    if (stored == null || fence && stored.notificationRevision != expected.notificationRevision
                            || !current.samePersistentState(expected, stored)) {
                        throw new SQLException("Notification transfer changed since it was read");
                    }
                }
                if (fence) {
                    try (final var query = current.prepare(
                            "SELECT DAT_NOTIFICATION_REVISION FROM DATA_TRANSFER WHERE DAT_ID=? FOR UPDATE",
                            transfer.getId()); final var rows = query.executeQuery()) {
                        if (!rows.next() || rows.getLong(1) != transfer.notificationRevision || rows.wasNull()) {
                            throw new SQLException("Notification transfer revision changed");
                        }
                    }
                }
                final var managed = current.merge(transfer);
                current.flush();
                if (fence) {
                    try (final var query = current.prepare(
                            """
                                    UPDATE DATA_TRANSFER SET DAT_NOTIFICATION_REVISION=DAT_NOTIFICATION_REVISION+1 WHERE DAT_ID=?
                                    """,
                            transfer.getId())) {
                        if (query.executeUpdate() != 1) {
                            throw new SQLException("Notification transfer no longer exists");
                        }
                    }
                    current.refresh(managed);
                }
                if (append(current, destination, target, event, eventId, retentionMillis, maxBytes, length)) {
                    NotificationOutboxStore.enqueue(current, outbox, event);
                }
                return fence ? new FencedResult(managed.notificationRevision, false) : null;
            });
        } finally {
            if (expected != null || fence) {
                try {
                    database.clearCache(DataTransfer.class, List.of(transfer.getId()));
                } catch (final RuntimeException failure) {
                    _log.warn("Could not evict notification transfer {} from the local cache", transfer.getId(),
                            failure);
                }
            }
        }
        if (result != null) {
            synchronized (transfer) {
                transfer.notificationRevision = result.revision();
                transfer.rememberNotificationBaseline();
            }
        }
        return result != null && result.duplicate();
    }

    public void validateTransferRevisionSchema() {
        transaction(current -> {
            if (!current.supportsTransferRevisionFencing()) {
                throw new IllegalStateException("Notification transfer revision mapping is not enabled");
            }
            validateTransferRevisionSchema(current);
            return null;
        });
    }

    private static void validateTransferRevisionSchema(final Broker.Transaction transaction) throws SQLException {
        try (final var query = transaction.prepare("""
                SELECT DATA_TYPE,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='DATA_TRANSFER'
                AND COLUMN_NAME='DAT_NOTIFICATION_REVISION'
                """); final var rows = query.executeQuery()) {
            if (!rows.next() || !"bigint".equalsIgnoreCase(rows.getString(1))
                    || rows.getString(2).toLowerCase(Locale.ROOT).contains("unsigned")
                    || !"NO".equals(rows.getString(3)) || !"0".equals(rows.getString(4)) || rows.next()) {
                throw new SQLException("Notification transfer revision schema is unsafe");
            }
        }
        try (final var query = transaction.prepare("""
                SELECT TRIGGER_NAME,ACTION_STATEMENT FROM information_schema.TRIGGERS
                WHERE TRIGGER_SCHEMA=DATABASE() AND EVENT_OBJECT_TABLE='DATA_TRANSFER'
                AND EVENT_MANIPULATION='UPDATE' AND ACTION_TIMING='BEFORE'
                """); final var rows = query.executeQuery()) {
            if (!rows.next() || !"notification_transfer_revision".equalsIgnoreCase(rows.getString(1))
                    || rows.getString(2) == null
                    || !REVISION_TRIGGER_BODY.equals(rows.getString(2).replaceAll("\\s+", "").toLowerCase(Locale.ROOT))
                    || rows.next()) {
                throw new SQLException("Notification transfer revision schema is unsafe");
            }
        }
    }

    private static boolean append(final Broker.Transaction transaction, final String destination, final String target,
            final String event, final String eventId, final long retentionMillis, final long maxBytes, final int length)
            throws SQLException {
        final var state = lock(transaction);
        if (eventId != null && eventRecorded(transaction, eventId)) {
            return false;
        }
        final var storedAt = now(transaction, state.time);
        final var remaining = prune(transaction, state.bytes, storedAt - retentionMillis, maxBytes - length);
        if (remaining > maxBytes - length) {
            throw new SQLException("Notification history cleanup is still in progress");
        }
        try (final var query = transaction.prepare("""
                INSERT INTO NOTIFICATION_HISTORY_STREAM (DES_NAME,NHS_NEXT_SEQUENCE,NHS_REMOVED_SEQUENCE)
                VALUES (?,0,0) ON DUPLICATE KEY UPDATE DES_NAME=VALUES(DES_NAME)
                """, destination)) {
            query.executeUpdate();
        }
        long stream;
        try (final var query = transaction.prepare("""
                SELECT NHS_NEXT_SEQUENCE FROM NOTIFICATION_HISTORY_STREAM WHERE DES_NAME=?
                """, destination); final var rows = query.executeQuery()) {
            if (!rows.next()) {
                throw new SQLException("Notification stream is missing");
            }
            stream = Math.addExact(rows.getLong(1), 1);
        }
        final var sequence = Math.addExact(state.next, 1);
        try (final var query = transaction.prepare("""
                INSERT INTO NOTIFICATION_HISTORY
                (NH_SEQUENCE,DES_NAME,NH_STREAM_SEQUENCE,NH_STORED_AT,NH_PAYLOAD_BYTES,NH_TARGET,NH_EVENT_ID,NH_PAYLOAD)
                VALUES (?,?,?,?,?,?,?,?)
                """, sequence, destination, stream, storedAt, length, target, eventId, event)) {
            query.executeUpdate();
        }
        try (final var query = transaction.prepare("""
                UPDATE NOTIFICATION_HISTORY_STREAM SET NHS_NEXT_SEQUENCE=? WHERE DES_NAME=?
                """, stream, destination)) {
            query.executeUpdate();
        }
        try (final var query = transaction.prepare("""
                UPDATE NOTIFICATION_HISTORY_STATE
                SET NHS_NEXT_SEQUENCE=?,NHS_PAYLOAD_BYTES=?,NHS_LAST_TIME=? WHERE NHS_ID=1
                """, sequence, remaining + length, storedAt)) {
            query.executeUpdate();
        }
        return true;
    }

    private static boolean eventRecorded(final Broker.Transaction transaction, final String eventId)
            throws SQLException {
        try (final var query = transaction
                .prepare("SELECT NH_SEQUENCE FROM NOTIFICATION_HISTORY WHERE NH_EVENT_ID=? LIMIT 1", eventId);
                final var rows = query.executeQuery()) {
            return rows.next();
        }
    }

    private static String eventId(final String event) {
        try {
            final var id = JSON.readTree(event).path("id");
            if (!id.isTextual()) {
                return null;
            }
            final var value = id.textValue();
            return value.isBlank() || value.length() > 36 ? null : value;
        } catch (final java.io.IOException e) {
            return null;
        }
    }

    public void prune(final long retentionMillis, final long maxBytes) {
        transaction(transaction -> {
            final var state = lock(transaction);
            final var time = now(transaction, state.time);
            final var bytes = prune(transaction, state.bytes, time - retentionMillis, maxBytes);
            if (bytes == state.bytes) {
                return null;
            }
            try (final var query = transaction.prepare("""
                    UPDATE NOTIFICATION_HISTORY_STATE SET NHS_PAYLOAD_BYTES=?,NHS_LAST_TIME=? WHERE NHS_ID=1
                    """, bytes, time)) {
                query.executeUpdate();
            }
            return null;
        });
    }

    private static Map<String, CheckpointValue> checkpointValues(final String json, final long cutoff)
            throws SQLException {
        try {
            final Map<String, CheckpointValue> result = new TreeMap<>(JSON.readValue(json, CHECKPOINT_TYPE));
            result.values().removeIf(value -> value.updated() < cutoff);
            return result;
        } catch (final Exception e) {
            throw new SQLException("Invalid checkpoint data", e);
        }
    }

    private static void saveCheckpoints(final Broker.Transaction transaction, final String user,
            final String destination, final Map<String, CheckpointValue> values) throws SQLException {
        if (values.isEmpty()) {
            try (final var query = transaction
                    .prepare("DELETE FROM NOTIFICATION_CHECKPOINT WHERE INU_ID=? AND DES_NAME=?", user, destination)) {
                query.executeUpdate();
            }
            return;
        }
        try {
            final var oldest = values.values().stream().mapToLong(CheckpointValue::updated).min().orElseThrow();
            try (final var query = transaction.prepare("""
                    UPDATE NOTIFICATION_CHECKPOINT SET NCP_VALUES=?,NCP_OLDEST_UPDATED=?
                    WHERE INU_ID=? AND DES_NAME=?
                    """, JSON.writeValueAsString(values), oldest, user, destination)) {
                query.executeUpdate();
            }
        } catch (final java.io.IOException e) {
            throw new SQLException("Cannot serialize checkpoints", e);
        }
    }

    /** Read, acknowledge or delete one named checkpoint. Acknowledgements never move backwards. */
    public Long checkpoint(final String user, final String destination, final String consumer, final String operation,
            final Long sequence, final int maxConsumers, final long retentionMillis) {
        if (user == null || user.isBlank() || user.length() > 128 || destination == null || destination.isBlank()
                || destination.length() > 32 || consumer == null || !consumer.matches("[A-Za-z0-9._-]{1,64}")
                || !List.of("get", "ack", "delete").contains(operation) || maxConsumers < 1 || maxConsumers > 256
                || retentionMillis <= 0 || "ack".equals(operation) && (sequence == null || sequence < 0)) {
            throw new IllegalArgumentException("Invalid checkpoint request");
        }
        return transaction(transaction -> {
            final var time = now(transaction, 0);
            if ("ack".equals(operation)) {
                try (final var query = transaction.prepare("""
                        INSERT INTO NOTIFICATION_CHECKPOINT (INU_ID,DES_NAME,NCP_VALUES,NCP_OLDEST_UPDATED)
                        VALUES (?,?,'{}',NULL) ON DUPLICATE KEY UPDATE INU_ID=VALUES(INU_ID)
                        """, user, destination)) {
                    query.executeUpdate();
                }
            }
            String json = null;
            try (final var query = transaction.prepare("""
                    SELECT NCP_VALUES FROM NOTIFICATION_CHECKPOINT WHERE INU_ID=? AND DES_NAME=? FOR UPDATE
                    """, user, destination); final var rows = query.executeQuery()) {
                if (rows.next()) {
                    json = rows.getString(1);
                }
            }
            if (json == null) {
                return null;
            }
            final var values = checkpointValues(json, time - retentionMillis);
            final var previous = values.get(consumer);
            if ("get".equals(operation)) {
                return previous == null ? null : previous.sequence();
            }
            if ("delete".equals(operation)) {
                values.remove(consumer);
            } else {
                if (previous == null && values.size() >= maxConsumers) {
                    throw new IllegalArgumentException("Checkpoint limit reached");
                }
                if (previous != null && sequence < previous.sequence()) {
                    throw new IllegalArgumentException("Checkpoint cannot move backwards");
                }
                values.put(consumer,
                        new CheckpointValue(sequence, previous == null ? time : Math.max(time, previous.updated())));
            }
            saveCheckpoints(transaction, user, destination, values);
            return "ack".equals(operation) ? sequence : null;
        });
    }

    /** Remove a bounded batch of expired checkpoint groups. */
    public void pruneCheckpoints(final long retentionMillis) {
        if (retentionMillis <= 0) {
            throw new IllegalArgumentException("Invalid checkpoint retention");
        }
        transaction(transaction -> {
            final var cutoff = now(transaction, 0) - retentionMillis;
            final List<CheckpointRow> expired = new ArrayList<>();
            try (final var query = transaction.prepare("""
                    SELECT INU_ID,DES_NAME,NCP_VALUES FROM NOTIFICATION_CHECKPOINT
                    WHERE NCP_OLDEST_UPDATED<? ORDER BY NCP_OLDEST_UPDATED LIMIT 100 FOR UPDATE
                    """, cutoff); final var rows = query.executeQuery()) {
                while (rows.next()) {
                    expired.add(new CheckpointRow(rows.getString(1), rows.getString(2), rows.getString(3)));
                }
            }
            for (final var row : expired) {
                saveCheckpoints(transaction, row.user(), row.destination(), checkpointValues(row.values(), cutoff));
            }
            return null;
        });
    }

    /** Reads one Aviso page in global sequence order with a fixed replay ceiling. */
    public Page replayAviso(final String destination, final long after, final Long upper, final Long fromTime,
            final Long toTime, final int limit, final long retentionMillis) {
        if (destination == null || destination.isBlank() || after < 0 || upper != null && upper < after || limit < 1
                || limit > 1000 || retentionMillis <= 0 || fromTime != null && toTime != null && fromTime > toTime) {
            throw new IllegalArgumentException("Invalid replay request");
        }
        return transaction(transaction -> {
            final var state = lock(transaction);
            final var cutoff = now(transaction, state.time) - retentionMillis;
            long ceiling = upper == null ? Math.max(state.next, after) : Math.max(after, Math.min(upper, state.next));
            long removed = 0;
            long removedAt = 0;
            try (final var query = transaction.prepare("""
                    SELECT NHS_REMOVED_EVENT_SEQUENCE,NHS_REMOVED_EVENT_TIME
                    FROM NOTIFICATION_HISTORY_STREAM WHERE DES_NAME=?
                    """, destination); final var rows = query.executeQuery()) {
                if (rows.next()) {
                    removed = rows.getLong(1);
                    removedAt = rows.getLong(2);
                }
            }
            if (toTime != null) {
                try (final var query = transaction.prepare("""
                        SELECT COALESCE(MAX(NH_SEQUENCE),0) FROM NOTIFICATION_HISTORY
                        WHERE DES_NAME=? AND NH_STORED_AT<=?
                        """, destination, toTime); final var rows = query.executeQuery()) {
                    if (rows.next()) {
                        ceiling = Math.max(after, Math.min(ceiling, rows.getLong(1)));
                    }
                }
            }
            try (final var query = transaction.prepare("""
                    SELECT COALESCE(MAX(NH_SEQUENCE),0),COALESCE(MAX(NH_STORED_AT),0)
                    FROM NOTIFICATION_HISTORY
                    WHERE DES_NAME=? AND NH_STORED_AT<?
                    """, destination, cutoff); final var rows = query.executeQuery()) {
                if (rows.next()) {
                    removed = Math.max(removed, rows.getLong(1));
                    removedAt = Math.max(removedAt, rows.getLong(2));
                }
            }
            final List<Entry> entries = new ArrayList<>();
            final var sql = new StringBuilder("""
                    SELECT NH_SEQUENCE,NH_STORED_AT,NH_TARGET,NH_PAYLOAD FROM NOTIFICATION_HISTORY
                    WHERE DES_NAME=? AND NH_SEQUENCE>? AND NH_SEQUENCE<=? AND NH_STORED_AT>=?
                    """);
            final var parameters = new ArrayList<Object>();
            parameters.add(destination);
            parameters.add(after);
            parameters.add(ceiling);
            parameters.add(cutoff);
            if (fromTime != null) {
                sql.append(" AND NH_STORED_AT>=?");
                parameters.add(fromTime);
            }
            if (toTime != null) {
                sql.append(" AND NH_STORED_AT<=?");
                parameters.add(toTime);
            }
            sql.append(" ORDER BY NH_SEQUENCE LIMIT ?");
            parameters.add(limit);
            long scanned = after;
            try (final var query = transaction.prepare(sql.toString(), parameters.toArray());
                    final var rows = query.executeQuery()) {
                while (rows.next()) {
                    scanned = rows.getLong(1);
                    entries.add(new Entry(scanned, rows.getLong(2), rows.getString(3), rows.getString(4)));
                }
            }
            final var hasMore = entries.size() == limit && scanned < ceiling;
            if (!hasMore) {
                scanned = ceiling;
            }
            final var historyGap = after < removed && (fromTime == null || fromTime <= removedAt);
            return new Page(ceiling, removed, removedAt, scanned, hasMore, historyGap, List.copyOf(entries));
        });
    }
}
