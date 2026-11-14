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

import static ecmwf.common.database.NotificationHistoryStore.now;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Durable, independently retried MQTT deliveries for a notification. */
public final class NotificationOutboxStore {
    private final DataBase database;

    public NotificationOutboxStore(final DataBase database) {
        this.database = java.util.Objects.requireNonNull(database);
    }

    public record Request(String id, String topic, long maxBytes) {
    }

    public record Delivery(String id, String topic, String payload, String mover, String token, int attempts) {
    }

    private static long lockState(final Broker.Transaction transaction) throws SQLException {
        try (final var query = transaction
                .prepare("SELECT NOS_PAYLOAD_BYTES FROM NOTIFICATION_OUTBOX_STATE WHERE NOS_ID=1 FOR UPDATE");
                final var rows = query.executeQuery()) {
            if (!rows.next()) {
                throw new SQLException("Notification outbox state is missing");
            }
            return rows.getLong(1);
        }
    }

    static void enqueue(final Broker.Transaction transaction, final Request request, final String payload)
            throws SQLException {
        if (request == null) {
            return;
        }
        final var size = payload.getBytes(StandardCharsets.UTF_8).length;
        if (request.id() == null || !request.id().matches("[0-9a-fA-F-]{36}") || request.topic() == null
                || request.topic().isBlank() || request.topic().length() > 512 || request.topic().contains("+")
                || request.topic().contains("#") || request.topic().indexOf('\0') >= 0 || request.maxBytes() <= 0
                || size > request.maxBytes()) {
            throw new IllegalArgumentException("Invalid notification outbox request");
        }
        final var bytes = lockState(transaction);
        if (bytes > request.maxBytes() - size) {
            throw new SQLException("Notification outbox is full");
        }
        final List<String> movers = new ArrayList<>();
        try (final var query = transaction.prepare("SELECT TRS_NAME FROM TRANSFER_SERVER ORDER BY TRS_NAME");
                final var rows = query.executeQuery()) {
            while (rows.next()) {
                movers.add(rows.getString(1));
            }
        }
        if (movers.isEmpty()) {
            throw new SQLException("No configured Data Movers for notification outbox");
        }
        try (final var query = transaction.prepare("""
                INSERT INTO NOTIFICATION_OUTBOX (NOB_ID,NOB_TOPIC,NOB_PAYLOAD,NOB_PAYLOAD_BYTES) VALUES (?,?,?,?)
                """, request.id(), request.topic(), payload, size)) {
            query.executeUpdate();
        }
        final var due = now(transaction, 0);
        for (final var mover : movers) {
            try (final var query = transaction.prepare("""
                    INSERT INTO NOTIFICATION_OUTBOX_TARGET
                    (NOB_ID,TRS_NAME,NOT_NEXT_ATTEMPT,NOT_ATTEMPTS,NOT_LEASE_UNTIL)
                    VALUES (?,?,?,0,0)
                    """, request.id(), mover, due)) {
                query.executeUpdate();
            }
        }
        try (final var query = transaction.prepare("""
                UPDATE NOTIFICATION_OUTBOX_STATE
                SET NOS_PAYLOAD_BYTES=?,NOS_VERSION=NOS_VERSION+1 WHERE NOS_ID=1
                """, bytes + size)) {
            query.executeUpdate();
        }
    }

    /** Claim one due broker delivery. Expired claims can be retried after a crash. */
    public Delivery claim(final long leaseMillis) {
        return claim(leaseMillis, Set.of());
    }

    public Delivery claim(final long leaseMillis, final Set<String> excludedMovers) {
        if (leaseMillis <= 0 || leaseMillis > 3600000) {
            throw new IllegalArgumentException("Invalid outbox lease");
        }
        final var excluded = List.copyOf(excludedMovers);
        return database.transaction(transaction -> {
            final var time = now(transaction, 0);
            String id = null;
            String mover = null;
            int attempts = 0;
            final var parameters = new ArrayList<Object>();
            parameters.add(time);
            parameters.add(time);
            parameters.addAll(excluded);
            final var exclude = excluded.isEmpty() ? "" : " AND TRS_NAME NOT IN ("
                    + String.join(",", java.util.Collections.nCopies(excluded.size(), "?")) + ")";
            try (final var query = transaction.prepare("""
                    SELECT NOB_ID,TRS_NAME,NOT_ATTEMPTS FROM NOTIFICATION_OUTBOX_TARGET
                    WHERE NOT_NEXT_ATTEMPT<=? AND NOT_LEASE_UNTIL<=?
                    """ + exclude + " ORDER BY NOT_NEXT_ATTEMPT,NOB_ID,TRS_NAME LIMIT 1 FOR UPDATE",
                    parameters.toArray()); final var rows = query.executeQuery()) {
                if (rows.next()) {
                    id = rows.getString(1);
                    mover = rows.getString(2);
                    attempts = Math.min(rows.getInt(3) + 1, 1000000);
                }
            }
            if (id == null) {
                return null;
            }
            final var token = UUID.randomUUID().toString();
            try (final var query = transaction.prepare("""
                    UPDATE NOTIFICATION_OUTBOX_TARGET SET NOT_LEASE_TOKEN=?,NOT_LEASE_UNTIL=?,NOT_ATTEMPTS=?
                    WHERE NOB_ID=? AND TRS_NAME=?
                    """, token, time + leaseMillis, attempts, id, mover)) {
                query.executeUpdate();
            }
            try (final var query = transaction
                    .prepare("SELECT NOB_TOPIC,NOB_PAYLOAD FROM NOTIFICATION_OUTBOX WHERE NOB_ID=?", id);
                    final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("Notification outbox message is missing");
                }
                return new Delivery(id, rows.getString(1), rows.getString(2), mover, token, attempts);
            }
        });
    }

    /** Reschedule an unsuccessful or ambiguous broker attempt if the lease is still ours. */
    public void retry(final Delivery delivery, final long delayMillis) {
        if (delayMillis < 0 || delayMillis > 3600000) {
            throw new IllegalArgumentException("Invalid retry delay");
        }
        database.transaction(transaction -> {
            try (final var query = transaction.prepare("""
                    UPDATE NOTIFICATION_OUTBOX_TARGET
                    SET NOT_NEXT_ATTEMPT=?,NOT_LEASE_UNTIL=0,NOT_LEASE_TOKEN=NULL
                    WHERE NOB_ID=? AND TRS_NAME=? AND NOT_LEASE_TOKEN=?
                    """, now(transaction, 0) + delayMillis, delivery.id(), delivery.mover(), delivery.token())) {
                query.executeUpdate();
            }
            return null;
        });
    }

    /** Mark a confirmed broker acceptance; delete the payload after the final target succeeds. */
    public boolean complete(final Delivery delivery) {
        return database.transaction(transaction -> {
            final var bytes = lockState(transaction);
            try (final var query = transaction.prepare("""
                    DELETE FROM NOTIFICATION_OUTBOX_TARGET
                    WHERE NOB_ID=? AND TRS_NAME=? AND NOT_LEASE_TOKEN=?
                    """, delivery.id(), delivery.mover(), delivery.token())) {
                if (query.executeUpdate() == 0) {
                    return false;
                }
            }
            var remaining = false;
            try (final var query = transaction
                    .prepare("SELECT 1 FROM NOTIFICATION_OUTBOX_TARGET WHERE NOB_ID=? LIMIT 1", delivery.id());
                    final var rows = query.executeQuery()) {
                remaining = rows.next();
            }
            long updatedBytes = bytes;
            if (!remaining) {
                try (final var query = transaction.prepare(
                        "SELECT NOB_PAYLOAD_BYTES FROM NOTIFICATION_OUTBOX WHERE NOB_ID=? FOR UPDATE", delivery.id());
                        final var rows = query.executeQuery()) {
                    if (!rows.next()) {
                        throw new SQLException("Notification outbox message is missing");
                    }
                    updatedBytes -= rows.getInt(1);
                }
                try (final var query = transaction.prepare("DELETE FROM NOTIFICATION_OUTBOX WHERE NOB_ID=?",
                        delivery.id())) {
                    query.executeUpdate();
                }
            }
            try (final var query = transaction.prepare("""
                    UPDATE NOTIFICATION_OUTBOX_STATE
                    SET NOS_PAYLOAD_BYTES=?,NOS_VERSION=NOS_VERSION+1 WHERE NOS_ID=1
                    """, updatedBytes)) {
                query.executeUpdate();
            }
            return true;
        });
    }
}
