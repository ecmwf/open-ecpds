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
 */

package ecmwf.common.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Durable dispatch and destruction journals; neither acknowledgement nor elapsed staging time proves delivery. */
public final class HostPasswordRetirementStore {
    private final HostPasswordRotationStore rotations;

    HostPasswordRetirementStore(final ECpdsBase database) {
        rotations = database.getHostPasswordRotationStore();
    }

    public record Attempt(UUID id, long host, UUID reference, long transfer, String mover, String key, String route,
            String context) {
        public Attempt {
            if (id == null || host <= 0 || reference == null || transfer <= 0 || mover == null || mover.isBlank()
                    || mover.length() > 255 || key == null || !key.matches("[0-9a-f]{64}")
                    || !("DIRECT_RMI".equals(route) || "CONTINENTAL_HTTPS".equals(route)) || context == null
                    || !context.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("Invalid password delivery attempt");
            }
        }
    }

    public record Retirement(long host, UUID reference, UUID successor) {
    }

    public void reserveDelivery(final Attempt attempt, final HostPasswordRotationStore.ProbeHost snapshot,
            final String endpoint, final String address, final int port, final String login) throws IOException {
        rotations.work(transaction -> {
            final var active = HostPasswordRotationStore.findActiveForDelivery(transaction, attempt.host(),
                    snapshot.destination(), endpoint, address, port, login, snapshot);
            if (active.isEmpty() || !active.orElseThrow().equals(attempt.reference())) {
                throw new SQLException("Delivery password or Host configuration changed");
            }
            reserveDeliveryLocked(transaction, attempt);
            return null;
        });
    }

    static void reserveDelivery(final DataBase.ScriptTransaction transaction, final Attempt attempt)
            throws SQLException {
        if (!HostPasswordRotationStore.lockHost(transaction, attempt.host())) {
            throw new SQLException("Password delivery requires a Host transaction");
        }
        reserveDeliveryLocked(transaction, attempt);
    }

    private static void reserveDeliveryLocked(final DataBase.ScriptTransaction transaction, final Attempt attempt)
            throws SQLException {
        try (final var query = transaction.prepare("lockHostPasswordDeliveryTransfer",
                Map.of("transfer", attempt.transfer()))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("Password delivery transfer is unavailable");
                }
            }
        }
        try (final var query = transaction.prepare("lockHostPasswordUnresolvedDelivery",
                Map.of("transfer", attempt.transfer()))) {
            try (final var rows = query.executeQuery()) {
                if (rows.next()) {
                    throw new SQLException("A prior password delivery attempt is unresolved");
                }
            }
        }
        try (final var insert = transaction.prepare("insertHostPasswordDelivery",
                Map.of("attemptId", attempt.id().toString(), "host", attempt.host(), "reference",
                        attempt.reference().toString(), "transfer", attempt.transfer(), "mover", attempt.mover(), "key",
                        attempt.key(), "route", attempt.route(), "context", attempt.context()))) {
            insert.executeUpdate();
        }
    }

    public Optional<Attempt> findAttempt(final UUID id) throws IOException {
        return rotations.work(transaction -> findAttempt(transaction, id));
    }

    public void acknowledgeDelivery(final UUID id) throws IOException {
        rotations.work(transaction -> {
            try (final var update = transaction.prepare("updateHostPasswordDeliveryAcknowledged",
                    Map.of("attemptId", id.toString()))) {
                update.executeUpdate();
            }
            return null;
        });
    }

    static Optional<Attempt> findAttempt(final DataBase.ScriptTransaction transaction, final UUID id)
            throws SQLException {
        try (final var query = transaction.prepare("getHostPasswordDelivery", Map.of("attemptId", id.toString()))) {
            try (final var rows = query.executeQuery()) {
                return rows.next()
                        ? Optional.of(
                                new Attempt(id, rows.getLong(1), UUID.fromString(rows.getString(2)), rows.getLong(3),
                                        rows.getString(4), rows.getString(5), rows.getString(6), rows.getString(7)))
                        : Optional.empty();
            }
        }
    }

    /** Called only after final-Mover signature and the complete durable attempt binding have been verified. */
    public void recordSuccessfulDelivery(final Attempt attempt, final String receipt) throws IOException {
        recordDeliveryOutcome(attempt, receipt, true);
    }

    public void recordDeliveryOutcome(final Attempt attempt, final String receipt, final boolean successful)
            throws IOException {
        rotations.work(transaction -> {
            recordDeliveryOutcome(transaction, attempt, receipt, successful);
            return null;
        });
    }

    static void recordSuccessfulDelivery(final DataBase.ScriptTransaction transaction, final Attempt attempt,
            final String receipt) throws SQLException {
        recordDeliveryOutcome(transaction, attempt, receipt, true);
    }

    static void recordDeliveryOutcome(final DataBase.ScriptTransaction transaction, final Attempt attempt,
            final String receipt, final boolean successful) throws SQLException {
        if (!HostPasswordRotationStore.lockHost(transaction, attempt.host())
                || !findAttempt(transaction, attempt.id()).filter(attempt::equals).isPresent() || receipt == null
                || receipt.isBlank() || receipt.length() > 8192) {
            throw new SQLException("Password delivery evidence changed");
        }
        final var state = successful ? "SUCCEEDED" : "FAILED";
        try (final var query = transaction.prepare("lockHostPasswordDeliveryState",
                Map.of("attemptId", attempt.id().toString()))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next() || ("SUCCEEDED".equals(rows.getString(1)) || "FAILED".equals(rows.getString(1)))
                        && !state.equals(rows.getString(1))) {
                    throw new SQLException("Password delivery terminal outcome changed");
                }
            }
        }
        try (final var update = transaction.prepare("updateHostPasswordDeliveryOutcome", Map.of("successful",
                successful, "receipt", receipt, "state", state, "attemptId", attempt.id().toString()))) {
            update.executeUpdate();
        }
        if (!successful) {
            return;
        }
        // Use the Master's receive time, not an untrusted callback clock, and never restart an existing countdown.
        try (final var update = transaction.prepare("updateHostPasswordFirstSuccess",
                Map.of("reference", attempt.reference().toString(), "host", attempt.host()))) {
            update.executeUpdate();
        }
    }

    public List<Retirement> dueRetirements() throws IOException {
        return rotations.work(HostPasswordRetirementStore::dueRetirements);
    }

    static List<Retirement> dueRetirements(final DataBase.ScriptTransaction transaction) throws SQLException {
        final var result = new ArrayList<Retirement>();
        try (final var query = transaction.prepare("getHostPasswordDueRetirements")) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    result.add(new Retirement(rows.getLong(1), UUID.fromString(rows.getString(2)),
                            UUID.fromString(rows.getString(3))));
                }
            }
        }
        return List.copyOf(result);
    }

    public boolean beginRetirement(final Retirement retirement) throws IOException {
        return rotations.work(transaction -> beginRetirement(transaction, retirement));
    }

    static boolean beginRetirement(final DataBase.ScriptTransaction transaction, final Retirement retirement)
            throws SQLException {
        if (!HostPasswordRotationStore.lockHost(transaction, retirement.host())) {
            throw new SQLException("Retirement requires a Host transaction");
        }
        try (final var query = transaction.prepare("lockHostPasswordDueRetirement", Map.of("host", retirement.host(),
                "successor", retirement.successor().toString(), "reference", retirement.reference().toString()))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    return false;
                }
            }
        }
        try (final var insert = transaction.prepare("insertHostPasswordRetirement", Map.of("host", retirement.host(),
                "reference", retirement.reference().toString(), "successor", retirement.successor().toString()))) {
            insert.executeUpdate();
        }
        return true;
    }

    /** Clearing the previous pointer is ordered after confirmed permanent destruction, never before it. */
    public void finishRetirement(final Retirement retirement) throws IOException {
        rotations.work(transaction -> {
            finishRetirement(transaction, retirement);
            return null;
        });
    }

    static void finishRetirement(final DataBase.ScriptTransaction transaction, final Retirement retirement)
            throws SQLException {
        if (!HostPasswordRotationStore.lockHost(transaction, retirement.host())) {
            throw new SQLException("Retirement requires a Host transaction");
        }
        try (final var query = transaction.prepare("lockHostPasswordRetirement", Map.of("host", retirement.host(),
                "reference", retirement.reference().toString(), "successor", retirement.successor().toString()))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("Retirement intent is unavailable");
                }
                if ("DESTROYED".equals(rows.getString(1))) {
                    return;
                }
            }
        }
        try (final var update = transaction.prepare("updateHostPasswordRetiredPointer",
                Map.of("host", retirement.host(), "successor", retirement.successor().toString(), "reference",
                        retirement.reference().toString()))) {
            if (update.executeUpdate() != 1) {
                throw new SQLException("Retirement pointers changed");
            }
        }
        try (final var update = transaction.prepare("updateHostPasswordRetirementDestroyed",
                Map.of("reference", retirement.reference().toString()))) {
            update.executeUpdate();
        }
    }
}
