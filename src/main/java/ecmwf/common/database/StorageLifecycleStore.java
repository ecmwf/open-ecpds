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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Bounded, fresh JDBC snapshots; lifecycle decisions must not use Hibernate's entity cache. */
public final class StorageLifecycleStore {
    public record Transfer(long id, String destination, int type, String group, String configuration, String target,
            String status, long expiry, long firstFinish, long finish, String original) {
    }

    public record Snapshot(DataFile file, List<Transfer> transfers) {
    }

    @FunctionalInterface
    private interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    private static <T> T work(final boolean write, final Work<T> work) {
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new IllegalStateException("Storage lifecycle database is unavailable");
        }
        try (final var session = factory.openSession()) {
            if (!write) {
                return session.doReturningWork(work::run);
            }
            final var transaction = session.beginTransaction();
            try {
                final var result = session.doReturningWork(work::run);
                transaction.commit();
                return result;
            } catch (final RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    private static PreparedStatement prepare(final Connection connection, final String sql, final Object... args)
            throws SQLException {
        final var statement = connection.prepareStatement(sql);
        try {
            statement.setQueryTimeout(5);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement;
        } catch (final SQLException e) {
            statement.close();
            throw e;
        }
    }

    public void checkSchema() {
        work(false, connection -> {
            for (final var sql : List.of(
                    "SELECT SPO_MIN_RETENTION_MS,SPO_GRACE_MS,SPO_LAZY_REPLICATION_MS,SPO_HIGH_WATERMARK,SPO_LOW_WATERMARK FROM STORAGE_POLICY WHERE 1=0",
                    "SELECT SLC_INSTANCE,SLC_PIN_UNTIL,SLC_STATE,SLC_RECLAIMABLE_BYTES,SLC_RECLAIMED_BYTES,SLC_REPLICAS_DROPPED FROM STORAGE_LIFECYCLE WHERE 1=0",
                    "SELECT SPM_RECLAIMED_BYTES,SPM_REPLICAS_DROPPED,SPM_LAST_APPLIED FROM STORAGE_POLICY_METRIC WHERE 1=0")) {
                try (final var query = prepare(connection, sql); final var rows = query.executeQuery()) {
                    rows.getMetaData();
                }
            }
            return null;
        });
    }

    public long scanCeiling() {
        return work(false, connection -> {
            try (final var query = prepare(connection, "SELECT COALESCE(MAX(DAF_ID),0) FROM DATA_FILE");
                    final var rows = query.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        });
    }

    public List<Long> files(final long after, final long ceiling, final int limit) {
        return work(false, connection -> {
            final List<Long> files = new ArrayList<>();
            try (final var query = prepare(connection, """
                    SELECT DAF_ID FROM DATA_FILE WHERE DAF_ID>? AND DAF_ID<=? AND DAF_DELETED=0 AND DAF_REMOVED=0
                    AND DAF_DOWNLOADED<>0 AND DAF_FILE_SYSTEM IS NOT NULL ORDER BY DAF_ID LIMIT ?
                    """, after, ceiling, limit); final var rows = query.executeQuery()) {
                while (rows.next()) {
                    files.add(rows.getLong(1));
                }
            }
            return files;
        });
    }

    public void error(final long id, final String message, final long now) {
        work(true, connection -> {
            try (final var query = prepare(connection, """
                    INSERT INTO STORAGE_LIFECYCLE (DAF_ID,SLC_INSTANCE,SLC_STATE,SLC_MESSAGE,SLC_EVALUATED_AT)
                    SELECT DAF_ID,COALESCE(DAF_FILE_INSTANCE,0),'ERROR',?,? FROM DATA_FILE WHERE DAF_ID=?
                    ON DUPLICATE KEY UPDATE SLC_STATE='ERROR',SLC_MESSAGE=VALUES(SLC_MESSAGE),
                    SLC_EVALUATED_AT=VALUES(SLC_EVALUATED_AT)
                    """, truncate(message, 1024), now, id)) {
                query.executeUpdate();
            }
            return null;
        });
    }

    public void resetReplication(final long fileId) {
        work(true, connection -> {
            try (final var query = prepare(connection, """
                    UPDATE DATA_TRANSFER SET DAT_REPLICATED=0,DAT_REPLICATE_TIME=NULL,DAT_REPLICATE_COUNT=0
                    WHERE DAF_ID=? AND DAT_DELETED=0
                    """, fileId)) {
                query.executeUpdate();
            }
            return null;
        });
    }

    public Snapshot snapshot(final long id, final int maxTransfers) {
        return work(false, connection -> {
            final DataFile file;
            try (final var query = prepare(connection, """
                    SELECT DAF_ID,TRG_NAME,DAF_SIZE,DAF_ARRIVED_TIME,DAF_ORIGINAL,DAF_TIME_STEP,
                    DAF_FILE_SYSTEM,DAF_FILE_INSTANCE,DAF_DELETED,DAF_REMOVED,DAF_DOWNLOADED
                    FROM DATA_FILE WHERE DAF_ID=?
                    """, id); final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("DataFile " + id + " no longer exists");
                }
                file = new DataFile(id);
                file.setTransferGroupName(rows.getString(2));
                file.setSize(rows.getLong(3));
                file.setArrivedTime(rows.getObject(4) == null ? null : new Timestamp(rows.getLong(4)));
                file.setOriginal(rows.getString(5));
                file.setTimeStep(rows.getLong(6));
                file.setFileSystem(rows.getObject(7) == null ? null : rows.getInt(7));
                file.setFileInstance(rows.getObject(8) == null ? null : rows.getInt(8));
                file.setDeleted(rows.getBoolean(9));
                file.setRemoved(rows.getBoolean(10));
                file.setDownloaded(rows.getBoolean(11));
            }
            final List<Transfer> transfers = new ArrayList<>();
            try (final var query = prepare(connection, """
                    SELECT T.DAT_ID,T.DES_NAME,D.DES_TYPE,D.TRG_NAME,D.DES_DATA,T.DAT_TARGET,T.STA_CODE,
                    T.DAT_EXPIRY_TIME,T.DAT_FIRST_FINISH_TIME,T.DAT_FINISH_TIME,T.TRS_NAME_ORIGINAL
                    FROM DATA_TRANSFER T JOIN DESTINATION D ON D.DES_NAME=T.DES_NAME
                    WHERE T.DAF_ID=? AND T.DAT_DELETED=0 ORDER BY T.DAT_ID LIMIT ?
                    """, id, maxTransfers + 1); final var rows = query.executeQuery()) {
                while (rows.next()) {
                    transfers.add(new Transfer(rows.getLong(1), rows.getString(2), rows.getInt(3), rows.getString(4),
                            rows.getString(5), rows.getString(6), rows.getString(7), rows.getLong(8), rows.getLong(9),
                            rows.getLong(10), rows.getString(11)));
                }
            }
            if (transfers.size() > maxTransfers) {
                throw new SQLException("DataFile " + id + " exceeds StoragePolicy.maxRelatedTransfers");
            }
            return new Snapshot(file, List.copyOf(transfers));
        });
    }

    public Map<String, String> state(final long id) {
        return work(false, connection -> {
            final Map<String, String> state = new LinkedHashMap<>();
            try (final var query = prepare(connection, "SELECT * FROM STORAGE_LIFECYCLE WHERE DAF_ID=?", id);
                    final var rows = query.executeQuery()) {
                if (rows.next()) {
                    final var metadata = rows.getMetaData();
                    for (int i = 1; i <= metadata.getColumnCount(); i++) {
                        state.put(metadata.getColumnName(i), rows.getString(i));
                    }
                }
            }
            return state;
        });
    }

    public void save(final DataFile file, final String state, final String message, final Set<String> policies,
            final long eligibleAt, final long guaranteedUntil, final long reclaimable, final long now) {
        work(true, connection -> {
            try (final var query = prepare(connection, """
                    INSERT INTO STORAGE_LIFECYCLE
                    (DAF_ID,SLC_INSTANCE,SLC_STATE,SLC_MESSAGE,SLC_POLICIES,SLC_ELIGIBLE_AT,
                     SLC_GUARANTEED_UNTIL,SLC_RECLAIMABLE_BYTES,SLC_EVALUATED_AT,SLC_REDUCED_AT)
                    VALUES (?,?,?,?,?,?,?,?,?,?)
                    ON DUPLICATE KEY UPDATE SLC_INSTANCE=VALUES(SLC_INSTANCE),SLC_STATE=VALUES(SLC_STATE),
                    SLC_MESSAGE=VALUES(SLC_MESSAGE),SLC_POLICIES=VALUES(SLC_POLICIES),
                    SLC_ELIGIBLE_AT=VALUES(SLC_ELIGIBLE_AT),SLC_GUARANTEED_UNTIL=VALUES(SLC_GUARANTEED_UNTIL),
                    SLC_RECLAIMABLE_BYTES=VALUES(SLC_RECLAIMABLE_BYTES),SLC_EVALUATED_AT=VALUES(SLC_EVALUATED_AT),
                    SLC_REDUCED_AT=IF(VALUES(SLC_REDUCED_AT)>0,VALUES(SLC_REDUCED_AT),SLC_REDUCED_AT)
                    """, file.getId(), instance(file), state, truncate(message, 1024),
                    truncate(String.join(", ", policies), 2048), eligibleAt, guaranteedUntil, reclaimable, now,
                    "ORIGINAL_ONLY".equals(state) ? now : 0)) {
                query.executeUpdate();
            }
            return null;
        });
    }

    public void pin(final DataFile file, final long until, final String reason, final String user) {
        work(true, connection -> {
            try (final var query = prepare(connection, """
                    INSERT INTO STORAGE_LIFECYCLE (DAF_ID,SLC_INSTANCE,SLC_PIN_UNTIL,SLC_PIN_REASON,SLC_PIN_BY)
                    VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE
                    SLC_PIN_UNTIL=VALUES(SLC_PIN_UNTIL),SLC_PIN_REASON=VALUES(SLC_PIN_REASON),
                    SLC_PIN_BY=VALUES(SLC_PIN_BY)
                    """, file.getId(), instance(file), until, reason, user)) {
                query.executeUpdate();
            }
            return null;
        });
    }

    public void removed(final DataFile file, final Set<String> policies, final long bytes, final long now) {
        work(true, connection -> {
            try (final var query = prepare(connection, """
                    INSERT INTO STORAGE_LIFECYCLE
                    (DAF_ID,SLC_INSTANCE,SLC_RECLAIMED_BYTES,SLC_REPLICAS_DROPPED) VALUES (?,?,?,1)
                    ON DUPLICATE KEY UPDATE SLC_RECLAIMED_BYTES=SLC_RECLAIMED_BYTES+VALUES(SLC_RECLAIMED_BYTES),
                    SLC_REPLICAS_DROPPED=SLC_REPLICAS_DROPPED+1
                    """, file.getId(), instance(file), bytes)) {
                query.executeUpdate();
            }
            for (final var policy : policies) {
                try (final var query = prepare(connection, """
                        INSERT INTO STORAGE_POLICY_METRIC
                        (SPO_NAME,SPM_RECLAIMED_BYTES,SPM_REPLICAS_DROPPED,SPM_LAST_APPLIED) VALUES (?,?,1,?)
                        ON DUPLICATE KEY UPDATE SPM_RECLAIMED_BYTES=SPM_RECLAIMED_BYTES+VALUES(SPM_RECLAIMED_BYTES),
                        SPM_REPLICAS_DROPPED=SPM_REPLICAS_DROPPED+1,SPM_LAST_APPLIED=VALUES(SPM_LAST_APPLIED)
                        """, policy, bytes, now)) {
                    query.executeUpdate();
                }
            }
            return null;
        });
    }

    public Map<String, Map<String, String>> metrics() {
        return work(false, connection -> {
            final Map<String, Map<String, String>> metrics = new LinkedHashMap<>();
            try (final var query = prepare(connection, "SELECT * FROM STORAGE_POLICY_METRIC ORDER BY SPO_NAME");
                    final var rows = query.executeQuery()) {
                while (rows.next()) {
                    metrics.put(rows.getString("SPO_NAME"),
                            Map.of("reclaimedBytes", rows.getString("SPM_RECLAIMED_BYTES"), "replicasDropped",
                                    rows.getString("SPM_REPLICAS_DROPPED"), "lastApplied",
                                    rows.getString("SPM_LAST_APPLIED")));
                }
            }
            return metrics;
        });
    }

    public static int instance(final DataFile file) {
        return file.getFileInstance() == null ? 0 : file.getFileInstance();
    }

    private static String truncate(final String value, final int length) {
        return value == null ? "" : value.substring(0, Math.min(value.length(), length));
    }
}
