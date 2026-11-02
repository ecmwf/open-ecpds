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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/** Persistent SNMP display metadata, independent of transfer Host geolocation. */
public final class SystemPhysicalLocationStore {
    public enum Status {
        SUCCESS, NOT_CONFIGURED, UNSUPPORTED, TIMEOUT, ERROR
    }

    public record Snapshot(String machineId, String targetKey, String location, Status status, long lastSuccess,
            long lastAttempt) implements Serializable {
    }

    /** Target key is a SHA-256 digest of the configured target identity, never credentials. */
    public record Attempt(String machineId, String targetKey, String location, Status status, long attemptedAt) {
        public Attempt {
            validateIdentity(machineId, targetKey);
            Objects.requireNonNull(status, "status");
            if (attemptedAt <= 0) {
                throw new IllegalArgumentException("Invalid collection timestamp");
            }
            if (status == Status.SUCCESS) {
                if (location == null || location.isBlank() || location.codePointCount(0, location.length()) > 1024
                        || location.codePoints().anyMatch(Character::isISOControl)) {
                    throw new IllegalArgumentException("Invalid physical location");
                }
            } else if (location != null) {
                throw new IllegalArgumentException("Only successful collection may supply a location");
            }
        }
    }

    private static final String UPSERT = """
            INSERT INTO SYSTEM_PHYSICAL_LOCATION
              (SPL_MACHINE_ID,SPL_TARGET_KEY,SPL_LOCATION,SPL_STATUS,SPL_LAST_SUCCESS,SPL_LAST_ATTEMPT)
            VALUES (?,?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
              SPL_LOCATION=IF(VALUES(SPL_LAST_ATTEMPT)>SPL_LAST_ATTEMPT,
                IF(VALUES(SPL_STATUS) IN ('SUCCESS','NOT_CONFIGURED'),VALUES(SPL_LOCATION),
                  IF(SPL_TARGET_KEY=VALUES(SPL_TARGET_KEY),SPL_LOCATION,NULL)),SPL_LOCATION),
              SPL_LAST_SUCCESS=IF(VALUES(SPL_LAST_ATTEMPT)>SPL_LAST_ATTEMPT,
                IF(VALUES(SPL_STATUS) IN ('SUCCESS','NOT_CONFIGURED'),VALUES(SPL_LAST_SUCCESS),
                  IF(SPL_TARGET_KEY=VALUES(SPL_TARGET_KEY),SPL_LAST_SUCCESS,0)),SPL_LAST_SUCCESS),
              SPL_STATUS=IF(VALUES(SPL_LAST_ATTEMPT)>SPL_LAST_ATTEMPT,VALUES(SPL_STATUS),SPL_STATUS),
              SPL_TARGET_KEY=IF(VALUES(SPL_LAST_ATTEMPT)>SPL_LAST_ATTEMPT,VALUES(SPL_TARGET_KEY),SPL_TARGET_KEY),
              SPL_LAST_ATTEMPT=GREATEST(SPL_LAST_ATTEMPT,VALUES(SPL_LAST_ATTEMPT))
            """;

    public Snapshot find(final String machineId, final String targetKey) throws SQLException {
        validateIdentity(machineId, targetKey);
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new SQLException("Physical location database session factory is unavailable");
        }
        try (final var session = factory.openSession()) {
            return session.doReturningWork(connection -> find(connection, machineId, targetKey));
        }
    }

    public void save(final Attempt attempt) throws SQLException {
        Objects.requireNonNull(attempt, "attempt");
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (factory == null) {
            throw new SQLException("Physical location database session factory is unavailable");
        }
        try (final var session = factory.openSession()) {
            final var transaction = session.beginTransaction();
            try {
                session.doWork(connection -> save(connection, attempt));
                transaction.commit();
            } catch (final RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    static Snapshot find(final Connection connection, final String machineId, final String targetKey)
            throws SQLException {
        validateIdentity(machineId, targetKey);
        try (final var statement = connection.prepareStatement("""
                SELECT SPL_LOCATION,SPL_STATUS,SPL_LAST_SUCCESS,SPL_LAST_ATTEMPT
                FROM SYSTEM_PHYSICAL_LOCATION WHERE SPL_MACHINE_ID=? AND SPL_TARGET_KEY=?
                """)) {
            statement.setQueryTimeout(5);
            statement.setString(1, machineId);
            statement.setString(2, targetKey);
            try (final var result = statement.executeQuery()) {
                return result.next() ? new Snapshot(machineId, targetKey, result.getString(1),
                        Status.valueOf(result.getString(2)), result.getLong(3), result.getLong(4)) : null;
            }
        }
    }

    static void save(final Connection connection, final Attempt attempt) throws SQLException {
        // Assignment order retains the old target and timestamp until dependent fields have been updated.
        try (final var statement = connection.prepareStatement(UPSERT)) {
            statement.setQueryTimeout(5);
            statement.setString(1, attempt.machineId());
            statement.setString(2, attempt.targetKey());
            statement.setString(3, attempt.location());
            statement.setString(4, attempt.status().name());
            statement.setLong(5, attempt.status() == Status.SUCCESS || attempt.status() == Status.NOT_CONFIGURED
                    ? attempt.attemptedAt() : 0);
            statement.setLong(6, attempt.attemptedAt());
            statement.executeUpdate();
        }
    }

    private static void validateIdentity(final String machineId, final String targetKey) {
        if (machineId == null || !machineId.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("Invalid machine identifier");
        }
        if (targetKey == null || !targetKey.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid target identity digest");
        }
    }
}
