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
import java.sql.Statement;
import java.util.Objects;

import ecmwf.ecpds.master.weather.WeatherObservation;
import ecmwf.ecpds.master.weather.WeatherObservation.Kind;

/** Transactional background store, cached schema capability and indexed transfer-detail provenance. */
public final class WeatherSocketStore {
    private static volatile boolean schemaAvailable;

    public enum Result {
        WRITTEN, TRANSFER_GONE, TERMINAL_MISSING
    }

    public void checkSchema() throws SQLException {
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            schemaAvailable = false;
            throw new SQLException("Network Weather database session factory is unavailable");
        }

        try (final var session = factory.openSession()) {
            session.doWork(WeatherSocketStore::checkSchema);
            schemaAvailable = true;
        } catch (RuntimeException e) {
            schemaAvailable = false;
            throw e;
        }
    }

    public static void checkConfigurationCapacity(String document) throws SQLException {
        final int bytes = document.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (bytes <= 65535)
            return;
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null)
            throw new SQLException("Network Weather database session factory is unavailable");
        try (final var session = factory.openSession()) {
            session.doWork(connection -> checkConfigurationCapacity(connection, bytes));
        }
    }

    static void checkConfigurationCapacity(Connection connection, int bytes) throws SQLException {
        try (final var statement = prepare(connection, """
                SELECT CHARACTER_OCTET_LENGTH FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='SYS_CONFIG' AND COLUMN_NAME='SCF_PARAM_VALUE'
                """); final var result = statement.executeQuery()) {
            if (!result.next() || result.getLong(1) < bytes)
                throw new SQLException("Network Weather policy exceeds SYS_CONFIG capacity; apply "
                        + "network-weather-policy-capacity.sql before saving larger policies");
        }
    }

    public static void decorate(long transferId, java.util.List<TransferStatistics> statistics) {
        if (!schemaAvailable || statistics.isEmpty()) {
            return;
        }
        final var byId = statistics.stream().collect(
                java.util.stream.Collectors.toMap(TransferStatistics::getId, java.util.function.Function.identity()));
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new IllegalStateException("Network Weather database session factory is unavailable");
        }
        try (final var session = factory.openSession()) {
            session.doWork(connection -> {
                try (final var statement = prepare(connection, """
                        SELECT C.TST_ID,H.NWS_ID,H.NWS_OUTCOME FROM WEATHER_SENDING H
                        JOIN WEATHER_CONNECTION C ON C.NWS_ID=H.NWS_ID WHERE H.DAT_ID=?
                        """)) {
                    statement.setLong(1, transferId);
                    try (final var result = statement.executeQuery()) {
                        while (result.next()) {
                            final var row = byId.get(result.getLong(1));
                            if (row != null) {
                                row.setWeatherExecutionId(result.getString(2));
                                row.setWeatherOutcome(result.getString(3));
                            }
                        }
                    }
                }
            });
        }
    }

    public static int legacySendingIndex(long transferId, int candidate) {
        if (!schemaAvailable) {
            return candidate;
        }
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new IllegalStateException("Network Weather database session factory is unavailable");
        }
        try (final var session = factory.openSession()) {
            return session.doReturningWork(connection -> Math.max(candidate, nextIndex(connection, transferId)));
        }
    }

    /** Read-only capability probe; never creates or alters production schema. */
    public static void checkSchema(Connection connection) throws SQLException {
        try (final var statement = prepare(connection, """
                SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES
                WHERE TABLE_SCHEMA=DATABASE()
                  AND TABLE_NAME IN ('DATA_TRANSFER','TRANSFER_STATISTICS','WEATHER_SENDING','WEATHER_CONNECTION')
                """); final var result = statement.executeQuery()) {
            int count = 0;
            while (result.next()) {
                if (!"InnoDB".equalsIgnoreCase(result.getString(2))) {
                    throw new SQLException("Network Weather requires transactional InnoDB tables");
                }
                count++;
            }
            if (count != 4) {
                throw new SQLException("Network Weather schema is missing; explicit schema rollout is required");
            }
        }
        final var metadata = connection.getMetaData();
        final var catalog = connection.getCatalog();
        for (final var table : java.util.List.of("WEATHER_SENDING", "WEATHER_CONNECTION", "TRANSFER_STATISTICS")) {
            final var unique = new java.util.HashMap<String, java.util.SortedMap<Integer, String>>();
            try (final var indexes = metadata.getIndexInfo(catalog, null, table, !"TRANSFER_STATISTICS".equals(table),
                    false)) {
                while (indexes.next()) {
                    final var name = indexes.getString("INDEX_NAME");
                    final var column = indexes.getString("COLUMN_NAME");
                    if (name != null && column != null) {
                        unique.computeIfAbsent(name, _ -> new java.util.TreeMap<>())
                                .put(indexes.getInt("ORDINAL_POSITION"), column);
                    }
                }
            }
            final var required = "WEATHER_SENDING".equals(table)
                    ? java.util.List.of(java.util.List.of("NWS_ID"), java.util.List.of("NWS_EVENT_ID"),
                            java.util.List.of("DAT_ID", "NWS_SENDING_INDEX"))
                    : "WEATHER_CONNECTION".equals(table)
                            ? java.util.List.of(java.util.List.of("NWS_ID", "NWC_ID"), java.util.List.of("TST_ID"))
                            : java.util.List.of(java.util.List.of("DAT_ID", "TST_REQUEUE_HISTORY"));
            for (final var key : required) {
                if (unique.values().stream()
                        .noneMatch(columns -> java.util.List.copyOf(columns.values()).equals(key))) {
                    throw new SQLException("Network Weather required identity/covering index is missing on " + table);
                }
            }
            if ("TRANSFER_STATISTICS".equals(table)) {
                continue;
            }
            final var references = new java.util.HashSet<String>();
            try (final var keys = metadata.getImportedKeys(catalog, null, table)) {
                while (keys.next()) {
                    if (keys.getInt("DELETE_RULE") == java.sql.DatabaseMetaData.importedKeyCascade) {
                        references.add(keys.getString("FKCOLUMN_NAME") + ":" + keys.getString("PKTABLE_NAME") + ":"
                                + keys.getString("PKCOLUMN_NAME"));
                    }
                }
            }
            final var expected = "WEATHER_SENDING".equals(table) ? java.util.Set.of("DAT_ID:DATA_TRANSFER:DAT_ID")
                    : java.util.Set.of("NWS_ID:WEATHER_SENDING:NWS_ID", "TST_ID:TRANSFER_STATISTICS:TST_ID");
            if (!references.containsAll(expected)) {
                throw new SQLException("Network Weather cascade constraints are missing on " + table);
            }
        }
        // Validate every writer column even when these tables are currently empty.
        try (final var statement = prepare(connection, """
                SELECT NWS_ID,DAT_ID,NWS_SENDING_INDEX,NWS_EVENT_ID,NWS_SOURCE_EPOCH,NWS_SOURCE,NWS_DESTINATION,
                    NWS_HOST,NWS_CONFIGURED_TARGET,NWS_PROTOCOL,NWS_LEG,NWS_OUTCOME,NWS_CATEGORY,NWS_SOURCE_TIME
                FROM WEATHER_SENDING WHERE 1=0
                """); final var _ = statement.executeQuery()) {
        }
        try (final var statement = prepare(connection,
                """
                        SELECT C.NWS_ID,C.NWC_ID,C.TST_ID,S.DAT_ID,S.TST_REQUEUE_HISTORY,S.TST_LOCAL_ADDRESS,S.TST_REMOTE_ADDRESS,
                            S.TST_START_TIME,S.TST_END_TIME,S.TST_RTT_MS,S.TST_BYTES_SENT,S.TST_BYTES_RECEIVED,
                            S.TST_PACING_RATE_BPS,S.TST_DELIVERY_RATE_BPS,S.TST_CWND,S.TST_SEGS_OUT,S.TST_SEGS_IN
                        FROM WEATHER_CONNECTION C JOIN TRANSFER_STATISTICS S ON C.TST_ID=S.TST_ID WHERE 1=0
                        """);
                final var _ = statement.executeQuery()) {
        }
    }

    public Result persist(WeatherObservation observation) throws SQLException {
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new SQLException("Network Weather database session factory is unavailable");
        }
        try (final var session = factory.openSession()) {
            final var transaction = session.beginTransaction();
            try {
                try (final var mutex = ecmwf.common.technical.Synchronized.getInstance(WeatherSocketStore.class)
                        .getMutex(observation.transferId())) {
                    synchronized (mutex.lock()) {
                        final var result = session.doReturningWork(connection -> write(connection, observation));
                        transaction.commit();
                        return result;
                    }
                }
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollback) {
                        e.addSuppressed(rollback);
                    }
                }
                throw e;
            }
        }
    }

    /** The caller owns commit/rollback. No explicit transfer-row lock or DDL; foreign keys still take engine locks. */
    public static Result write(Connection connection, WeatherObservation observation) throws SQLException {
        Objects.requireNonNull(observation);
        if (connection.getAutoCommit()) {
            throw new SQLException("Network Weather socket writes require an explicit transaction");
        }
        if (observation.kind() == Kind.DISPATCH_DIAGNOSTIC) {
            throw new IllegalArgumentException("Dispatch diagnostics do not have socket sending headers");
        }
        for (final var socket : observation.connections()) {
            if (socket.localAddress() != null && socket.localAddress().length() > 64
                    || socket.remoteAddress() != null && socket.remoteAddress().length() > 64) {
                throw new IllegalArgumentException("Socket address exceeds the existing statistics schema");
            }
        }
        try (final var statement = prepare(connection,
                "SELECT DES_NAME,DAT_DELETED FROM DATA_TRANSFER WHERE DAT_ID=?")) {
            statement.setLong(1, observation.transferId());
            try (final var result = statement.executeQuery()) {
                if (!result.next() || result.getBoolean(2)) {
                    return Result.TRANSFER_GONE;
                }
                if (!observation.destinationId().equals(result.getString(1))) {
                    throw new IllegalArgumentException("Network Weather destination does not match its transfer");
                }
            }
        }
        Integer sending = findSending(connection, observation);
        if (sending == null) {
            if (observation.kind() == Kind.SOCKET_SUPPLEMENT) {
                return Result.TERMINAL_MISSING;
            }
            sending = nextIndex(connection, observation.transferId());
            insertSending(connection, observation, sending);
        }
        int count;
        try (final var statement = prepare(connection, "SELECT COUNT(*) FROM WEATHER_CONNECTION WHERE NWS_ID=?")) {
            statement.setString(1, observation.executionId().toString());
            try (final var result = statement.executeQuery()) {
                result.next();
                count = result.getInt(1);
            }
        }
        final long deadline = System.nanoTime() + 2_000_000_000L;
        for (final var socket : observation.connections()) {
            if (System.nanoTime() - deadline >= 0) {
                throw new java.sql.SQLTimeoutException("Network Weather socket transaction budget exhausted");
            }
            if (existingConnection(connection, observation, socket)) {
                continue;
            }
            if (++count > WeatherObservation.MAX_CONNECTIONS) {
                throw new IllegalArgumentException("Network Weather sending exceeds the connection ceiling");
            }
            final long statistics = insertStatistics(connection, observation.transferId(), sending, socket);
            try (final var statement = prepare(connection,
                    "INSERT INTO WEATHER_CONNECTION(NWS_ID,NWC_ID,TST_ID) VALUES(?,?,?)")) {
                statement.setString(1, observation.executionId().toString());
                statement.setString(2, socket.connectionId().toString());
                statement.setLong(3, statistics);
                statement.executeUpdate();
            }
        }
        return Result.WRITTEN;
    }

    private static PreparedStatement prepare(Connection connection, String sql) throws SQLException {
        final var statement = connection.prepareStatement(sql);
        try {
            statement.setQueryTimeout(2);
            return statement;
        } catch (SQLException e) {
            statement.close();
            throw e;
        }
    }

    private static Integer findSending(Connection connection, WeatherObservation observation) throws SQLException {
        try (final var statement = prepare(connection, """
                SELECT DAT_ID,NWS_SENDING_INDEX,NWS_EVENT_ID,NWS_SOURCE_EPOCH,NWS_SOURCE,NWS_DESTINATION,
                       NWS_HOST,NWS_CONFIGURED_TARGET,NWS_PROTOCOL,NWS_LEG,NWS_OUTCOME,NWS_CATEGORY,NWS_SOURCE_TIME
                FROM WEATHER_SENDING WHERE NWS_ID=? FOR UPDATE
                """)) {
            statement.setString(1, observation.executionId().toString());
            try (final var result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                if (result.getLong(1) != observation.transferId()
                        || observation.kind() == Kind.SENDING_TERMINAL
                                && !observation.eventId().toString().equals(result.getString(3))
                        || !observation.sourceEpoch().toString().equals(result.getString(4))
                        || !observation.sourceId().equals(result.getString(5))
                        || !observation.destinationId().equals(result.getString(6))
                        || !observation.hostId().equals(result.getString(7))
                        || !observation.configuredTarget().equals(result.getString(8))
                        || !observation.protocol().equals(result.getString(9))
                        || !observation.leg().equals(result.getString(10))
                        || !observation.outcome().name().equals(result.getString(11))
                        || !observation.category().name().equals(result.getString(12))
                        || observation.occurredAt() != result.getLong(13)) {
                    throw new IllegalArgumentException("Conflicting Network Weather sending identity");
                }
                return result.getInt(2);
            }
        }
    }

    static int nextIndex(Connection connection, long transfer) throws SQLException {
        try (final var statement = prepare(connection, """
                SELECT GREATEST(
                    COALESCE((SELECT MAX(TST_REQUEUE_HISTORY) FROM TRANSFER_STATISTICS WHERE DAT_ID=?),-1),
                    COALESCE((SELECT MAX(NWS_SENDING_INDEX) FROM WEATHER_SENDING WHERE DAT_ID=?),-1)) + 1
                """)) {
            statement.setLong(1, transfer);
            statement.setLong(2, transfer);
            try (final var result = statement.executeQuery()) {
                result.next();
                final long index = result.getLong(1);
                if (index < 0 || index > Integer.MAX_VALUE) {
                    throw new SQLException("Network Weather sending index exhausted");
                }
                return (int) index;
            }
        }
    }

    private static void insertSending(Connection connection, WeatherObservation observation, int index)
            throws SQLException {
        try (final var statement = prepare(connection,
                """
                        INSERT INTO WEATHER_SENDING(NWS_ID,DAT_ID,NWS_SENDING_INDEX,NWS_EVENT_ID,NWS_SOURCE_EPOCH,NWS_SOURCE,
                            NWS_DESTINATION,NWS_HOST,NWS_CONFIGURED_TARGET,NWS_PROTOCOL,NWS_LEG,NWS_OUTCOME,NWS_CATEGORY,NWS_SOURCE_TIME)
                        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                        """)) {
            statement.setString(1, observation.executionId().toString());
            statement.setLong(2, observation.transferId());
            statement.setInt(3, index);
            statement.setString(4, observation.eventId().toString());
            statement.setString(5, observation.sourceEpoch().toString());
            statement.setString(6, observation.sourceId());
            statement.setString(7, observation.destinationId());
            statement.setString(8, observation.hostId());
            statement.setString(9, observation.configuredTarget());
            statement.setString(10, observation.protocol());
            statement.setString(11, observation.leg());
            statement.setString(12, observation.outcome().name());
            statement.setString(13, observation.category().name());
            statement.setLong(14, observation.occurredAt());
            statement.executeUpdate();
        }
    }

    private static boolean existingConnection(Connection connection, WeatherObservation observation,
            WeatherObservation.Connection socket) throws SQLException {
        try (final var statement = prepare(connection, """
                SELECT S.DAT_ID,S.TST_REQUEUE_HISTORY,S.TST_LOCAL_ADDRESS,S.TST_REMOTE_ADDRESS,S.TST_START_TIME,
                       S.TST_END_TIME,S.TST_RTT_MS,S.TST_BYTES_SENT,S.TST_BYTES_RECEIVED,
                       S.TST_PACING_RATE_BPS,S.TST_DELIVERY_RATE_BPS,S.TST_CWND,S.TST_SEGS_OUT,S.TST_SEGS_IN
                FROM WEATHER_CONNECTION C JOIN TRANSFER_STATISTICS S ON S.TST_ID=C.TST_ID
                WHERE C.NWS_ID=? AND C.NWC_ID=?
                """)) {
            statement.setString(1, observation.executionId().toString());
            statement.setString(2, socket.connectionId().toString());
            try (final var result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                if (result.getLong(1) != observation.transferId()
                        || !Objects.equals(result.getString(3), socket.localAddress())
                        || !Objects.equals(result.getString(4), socket.remoteAddress())
                        || result.getLong(5) != socket.startTime() || result.getLong(6) != socket.endTime()
                        || !Objects.equals(result.getObject(7, Double.class), socket.rttMs())
                        || !Objects.equals(result.getObject(8, Long.class), socket.bytesSent())
                        || !Objects.equals(result.getObject(9, Long.class), socket.bytesReceived())
                        || !Objects.equals(result.getObject(10, Long.class), socket.pacingRateBps())
                        || !Objects.equals(result.getObject(11, Long.class), socket.deliveryRateBps())
                        || !Objects.equals(result.getObject(12, Integer.class), socket.cwnd())
                        || !Objects.equals(result.getObject(13, Integer.class), socket.segsOut())
                        || !Objects.equals(result.getObject(14, Integer.class), socket.segsIn())) {
                    throw new IllegalArgumentException("Conflicting Network Weather connection identity");
                }
                return true;
            }
        }
    }

    private static long insertStatistics(Connection connection, long transfer, int sending,
            WeatherObservation.Connection socket) throws SQLException {
        try (final var statement = connection.prepareStatement("""
                INSERT INTO TRANSFER_STATISTICS(DAT_ID,TST_REQUEUE_HISTORY,TST_LOCAL_ADDRESS,TST_REMOTE_ADDRESS,
                    TST_START_TIME,TST_END_TIME,TST_RTT_MS,TST_BYTES_SENT,TST_BYTES_RECEIVED,
                    TST_PACING_RATE_BPS,TST_DELIVERY_RATE_BPS,TST_CWND,TST_SEGS_OUT,TST_SEGS_IN)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setQueryTimeout(2);
            statement.setLong(1, transfer);
            statement.setInt(2, sending);
            statement.setString(3, socket.localAddress());
            statement.setString(4, socket.remoteAddress());
            statement.setLong(5, socket.startTime());
            statement.setLong(6, socket.endTime());
            statement.setObject(7, socket.rttMs());
            statement.setObject(8, socket.bytesSent());
            statement.setObject(9, socket.bytesReceived());
            statement.setObject(10, socket.pacingRateBps());
            statement.setObject(11, socket.deliveryRateBps());
            statement.setObject(12, socket.cwnd());
            statement.setObject(13, socket.segsOut());
            statement.setObject(14, socket.segsIn());
            statement.executeUpdate();
            try (final var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Missing generated Network Weather statistics identity");
                }
                return keys.getLong(1);
            }
        }
    }
}
