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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Master-owned acquisition progress, independent of expiring user notification subscriptions. */
public final class AcquisitionCheckpointStore {
    private final DataBase database;

    public record State(long sequence, long inventoryAt) {
        public State {
            if (sequence < -1 || inventoryAt < 0)
                throw new IllegalArgumentException("Invalid acquisition checkpoint");
        }
    }

    public AcquisitionCheckpointStore(final DataBase database) {
        this.database = database;
    }

    public static String key(final String... parts) throws IOException {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(new ObjectMapper().writeValueAsBytes(parts)));
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException("Cannot identify acquisition subscription", e);
        }
    }

    public State read(final String key) {
        return database.transaction(transaction -> {
            try (final var query = transaction.prepare("""
                    SELECT ACP_SEQUENCE,ACP_INVENTORY_AT FROM ACQUISITION_CHECKPOINT WHERE ACP_KEY=?
                    """, key); final var rows = query.executeQuery()) {
                return rows.next() ? new State(rows.getLong(1), rows.getLong(2)) : new State(-1, 0);
            }
        });
    }

    /** Persist only after registration; concurrent or stale discovery results must be replayed. */
    public void acknowledge(final String key, final State expected, final State next) {
        if (next.sequence() < Math.max(0, expected.sequence()) || next.inventoryAt() < expected.inventoryAt())
            throw new IllegalArgumentException("Acquisition checkpoint cannot move backwards");
        database.transaction(transaction -> {
            try (final var query = transaction.prepare("""
                    INSERT INTO ACQUISITION_CHECKPOINT (ACP_KEY,ACP_SEQUENCE,ACP_INVENTORY_AT)
                    VALUES (?,-1,0) ON DUPLICATE KEY UPDATE ACP_KEY=VALUES(ACP_KEY)
                    """, key)) {
                query.executeUpdate();
            }
            try (final var query = transaction.prepare("""
                    UPDATE ACQUISITION_CHECKPOINT SET ACP_SEQUENCE=?,ACP_INVENTORY_AT=?
                    WHERE ACP_KEY=? AND ACP_SEQUENCE=? AND ACP_INVENTORY_AT=?
                    """, next.sequence(), next.inventoryAt(), key, expected.sequence(), expected.inventoryAt())) {
                if (query.executeUpdate() != 1 && !next.equals(expected))
                    throw new IllegalStateException("Concurrent acquisition checkpoint update; replay required");
            }
            return null;
        });
    }
}
