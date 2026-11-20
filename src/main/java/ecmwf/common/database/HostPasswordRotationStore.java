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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Persistent, secret-free state for Host password rotation. */
public final class HostPasswordRotationStore {
    private final ECpdsBase database;

    HostPasswordRotationStore(final ECpdsBase database) {
        this.database = Objects.requireNonNull(database);
    }

    public enum State {
        STAGING, STAGED, TESTED, DISCARDING
    }

    public record Pending(UUID reference, State state, Instant createdAt, String testedEndpoint, Instant testedAt,
            String testedHostConfiguration, long retirementGraceSeconds) {
    }

    public record Access(long host, String destination, String user, String address, int port, String login) {
    }

    public record LegacyAdmission(long host, UUID id) {
    }

    public record LegacyRoute(String id, String relay, long proxy, String mover, String key, UUID incarnation) {
        public LegacyRoute {
            if (id == null || !id.matches("[0-9a-f]{64}") || relay == null || relay.isBlank() || relay.length() > 255
                    || proxy < 0 || mover == null || mover.isBlank() || mover.length() > 255
                    || proxy == 0 && !relay.equals(mover) || key == null || !key.matches("[0-9a-f]{64}")
                    || incarnation == null) {
                throw new IllegalArgumentException("Invalid legacy drain enrollment");
            }
        }
    }

    public record LegacyCutover(UUID id, UUID reference, String endpoint, String configuration,
            List<LegacyRoute> routes) {
        public LegacyCutover {
            routes = routes.stream().sorted(java.util.Comparator.comparing(LegacyRoute::id)).toList();
        }
    }

    public record LegacyDrainProgress(int routes, int freshReceipts, int sessions, int unresolvedAdmissions)
            implements java.io.Serializable {
        public boolean ready() {
            return routes > 0 && routes == freshReceipts && sessions == 0 && unresolvedAdmissions == 0;
        }
    }

    public ProbeHost activationSnapshot(final Access access, final UUID reference, final String endpoint)
            throws IOException {
        return work(transaction -> {
            final var snapshot = requireAccess(transaction, access);
            if (!hasActivationPrerequisites(transaction, access.host(), reference, access.destination(), access.user(),
                    endpoint, access.address(), access.port(), access.login(), Duration.ofMinutes(5))) {
                throw new SQLException("A current tested password is required for activation");
            }
            return snapshot;
        });
    }

    public void activateTested(final Access access, final UUID reference, final String endpoint,
            final String expectedConfiguration, final LegacyCutover enrolled) throws IOException {
        work(transaction -> {
            activateTested(transaction, access, reference, endpoint, expectedConfiguration, enrolled);
            return null;
        });
    }

    static void activateTested(final DataBase.ScriptTransaction transaction, final Access access, final UUID reference,
            final String endpoint, final String expectedConfiguration, final LegacyCutover enrolled)
            throws SQLException {
        final var snapshot = requireAccess(transaction, access);
        if (!snapshot.fingerprint().equals(expectedConfiguration)) {
            throw new SQLException("Host configuration changed during activation");
        }
        if (!hasActive(transaction, access.host(), false)) {
            final var current = findLegacyCutover(transaction, access.host())
                    .orElseThrow(() -> new SQLException("Legacy drain is not enrolled"));
            if (!current.equals(enrolled) || !current.reference().equals(reference)
                    || !current.endpoint().equals(endpoint) || !current.configuration().equals(snapshot.fingerprint())
                    || !legacyDrainProgress(transaction, access.host()).ready()) {
                throw new SQLException("Legacy drain is not confirmed");
            }
            requireLegacyFleetCoverage(transaction, current.routes());
            try (final var update = transaction.prepare("updateHostPasswordLegacyDrained",
                    Map.of("host", access.host()))) {
                if (update.executeUpdate() != 1) {
                    throw new SQLException("Legacy fence is unavailable");
                }
            }
        }
        if (!promoteTestedPassword(transaction, access.host(), reference, access.destination(), access.user(), endpoint,
                access.address(), access.port(), access.login(), Duration.ofMinutes(5))) {
            throw new SQLException("Tested password could not be activated");
        }
    }

    public Optional<LegacyCutover> findLegacyCutover(final Access access) throws IOException {
        return work(transaction -> {
            requireAccess(transaction, access);
            return findLegacyCutover(transaction, access.host());
        });
    }

    public LegacyCutover prepareLegacyDrain(final Access access, final UUID reference, final String endpoint,
            final List<LegacyRoute> routes) throws IOException {
        return work(transaction -> prepareLegacyDrain(transaction, access, reference, endpoint, routes));
    }

    static LegacyCutover prepareLegacyDrain(final DataBase.ScriptTransaction transaction, final Access access,
            final UUID reference, final String endpoint, final List<LegacyRoute> routes) throws SQLException {
        final var configuration = requireAccess(transaction, access).fingerprint();
        if (!hasActivationPrerequisites(transaction, access.host(), reference, access.destination(), access.user(),
                endpoint, access.address(), access.port(), access.login(), Duration.ofMinutes(5))) {
            throw new SQLException("A current tested password is required before draining");
        }
        requireLegacyFleetCoverage(transaction, routes);
        final var existing = findLegacyCutover(transaction, access.host());
        if (existing.isPresent()) {
            final var cutover = existing.orElseThrow();
            if (!cutover.endpoint().equals(endpoint) || !cutover.configuration().equals(configuration)
                    || !new HashSet<>(cutover.routes()).equals(new HashSet<>(routes))) {
                throw new SQLException("Legacy drain enrollment changed");
            }
            if (!cutover.reference().equals(reference)) {
                try (final var update = transaction.prepare("updateHostPasswordLegacyCutoverReference",
                        Map.of("reference", reference.toString(), "host", access.host(), "cutover",
                                cutover.id().toString()))) {
                    update.executeUpdate();
                }
            }
            return new LegacyCutover(cutover.id(), reference, endpoint, configuration, routes);
        }
        // The Host row lock also orders this fence with every tracked Master admission.
        fenceLegacy(transaction, access.host());
        final var cutover = new LegacyCutover(UUID.randomUUID(), reference, endpoint, configuration, routes);
        try (final var insert = transaction.prepare("insertHostPasswordLegacyCutover",
                Map.of("host", access.host(), "cutover", cutover.id().toString(), "reference", reference.toString(),
                        "endpoint", endpoint, "configuration", configuration))) {
            insert.executeUpdate();
        }
        transaction.executeBatch("insertHostPasswordLegacyRoute",
                routes.stream()
                        .map(route -> Map.of("host", access.host(), "routeId", route.id(), "relay", route.relay(),
                                "proxy", route.proxy(), "mover", route.mover(), "key", route.key(), "incarnation",
                                route.incarnation().toString()))
                        .toList());
        return cutover;
    }

    static Optional<LegacyCutover> findLegacyCutover(final DataBase.ScriptTransaction transaction, final long host)
            throws SQLException {
        final UUID id, reference;
        final String endpoint, configuration;
        try (final var query = transaction.prepare("getHostPasswordLegacyCutover", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                id = UUID.fromString(rows.getString(1));
                reference = UUID.fromString(rows.getString(2));
                endpoint = rows.getString(3);
                configuration = rows.getString(4);
            }
        }
        final var routes = new ArrayList<LegacyRoute>();
        try (final var query = transaction.prepare("getHostPasswordLegacyRoutes", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    routes.add(new LegacyRoute(rows.getString(1), rows.getString(2), rows.getLong(3), rows.getString(4),
                            rows.getString(5), UUID.fromString(rows.getString(6))));
                }
            }
        }
        return Optional.of(new LegacyCutover(id, reference, endpoint, configuration, routes));
    }

    static void requireLegacyFleetCoverage(final DataBase.ScriptTransaction transaction, final List<LegacyRoute> routes)
            throws SQLException {
        if (routes.isEmpty() || routes.size() > 64
                || routes.stream().map(LegacyRoute::id).distinct().count() != routes.size()) {
            throw new SQLException("Invalid legacy drain fleet");
        }
        final var direct = new HashSet<String>();
        final var proxies = new HashSet<Long>();
        for (final var route : routes) {
            if (route.proxy() == 0 ? !direct.add(route.relay()) : !proxies.add(route.proxy())) {
                throw new SQLException("Duplicate legacy drain route");
            }
        }
        final var servers = new HashSet<String>();
        try (final var query = transaction.prepare("lockHostPasswordFleetServers")) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    servers.add(rows.getString(1));
                }
            }
        }
        final var knownProxies = new HashSet<Long>();
        try (final var query = transaction.prepare("lockHostPasswordFleetProxies")) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    knownProxies.add(rows.getLong(1));
                }
            }
        }
        // Include inactive records: inactivity does not prove that an old session has ended.
        if (!servers.equals(direct) || !knownProxies.equals(proxies)
                || routes.stream().anyMatch(route -> !servers.contains(route.relay()))) {
            throw new SQLException("Legacy drain does not cover the complete registered fleet");
        }
    }

    public void recordLegacyDrainEvidence(final Access access, final LegacyCutover cutover, final LegacyRoute route,
            final UUID nonce, final String receipt, final int sessions) throws IOException {
        work(transaction -> {
            recordLegacyDrainEvidence(transaction, access, cutover, route, nonce, receipt, sessions);
            return null;
        });
    }

    static void recordLegacyDrainEvidence(final DataBase.ScriptTransaction transaction, final Access access,
            final LegacyCutover cutover, final LegacyRoute route, final UUID nonce, final String receipt,
            final int sessions) throws SQLException {
        requireAccess(transaction, access);
        final var current = findLegacyCutover(transaction, access.host())
                .orElseThrow(() -> new SQLException("Legacy drain is not enrolled"));
        if (!current.equals(cutover) || !current.routes().contains(route) || nonce == null || receipt == null
                || receipt.isBlank() || receipt.length() > 8192 || sessions < 0 || sessions > 16384) {
            throw new SQLException("Legacy drain evidence changed");
        }
        requireLegacyFleetCoverage(transaction, current.routes());
        try (final var update = transaction.prepare("updateHostPasswordLegacyEvidence",
                Map.of("nonce", nonce.toString(), "receipt", receipt, "sessions", sessions, "host", access.host(),
                        "routeId", route.id(), "incarnation", route.incarnation().toString(), "key", route.key()))) {
            if (update.executeUpdate() != 1) {
                throw new SQLException("Legacy drain evidence could not be recorded");
            }
        }
    }

    public LegacyDrainProgress reconcileLegacyAdmissions(final Access access, final LegacyCutover cutover)
            throws IOException {
        return work(transaction -> reconcileLegacyAdmissions(transaction, access, cutover));
    }

    static LegacyDrainProgress reconcileLegacyAdmissions(final DataBase.ScriptTransaction transaction,
            final Access access, final LegacyCutover cutover) throws SQLException {
        final var configuration = requireAccess(transaction, access).fingerprint();
        final var current = findLegacyCutover(transaction, access.host())
                .orElseThrow(() -> new SQLException("Legacy drain is not enrolled"));
        final var progress = legacyDrainProgress(transaction, access.host());
        if (!current.equals(cutover) || !current.configuration().equals(configuration)
                || !hasLegacyFence(transaction, access.host(), false) || progress.routes() == 0
                || progress.routes() != progress.freshReceipts() || progress.sessions() != 0) {
            throw new SQLException("Complete fresh fenced-session evidence is required for reconciliation");
        }
        requireLegacyFleetCoverage(transaction, current.routes());
        if (progress.unresolvedAdmissions() == 0) {
            return progress;
        }
        final var admissions = new ArrayList<String>();
        final var receipts = new ArrayList<String>();
        try (final var query = transaction.prepare("getHostPasswordLegacyAdmissions", Map.of("host", access.host()))) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    final var relay = rows.getString(2);
                    if (current.routes().stream().noneMatch(route -> route.relay().equals(relay))) {
                        throw new SQLException("An admission used an unenrolled route");
                    }
                    admissions.add(UUID.fromString(rows.getString(1)).toString());
                }
            }
        }
        try (final var query = transaction.prepare("getHostPasswordLegacyReceipts", Map.of("host", access.host()))) {
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    receipts.add(rows.getString(1));
                }
            }
        }
        try {
            final var json = new com.fasterxml.jackson.databind.ObjectMapper();
            try (final var insert = transaction.prepare("insertHostPasswordLegacyReconciliation",
                    Map.of("reconciliationId", UUID.randomUUID().toString(), "host", access.host(), "cutover",
                            current.id().toString(), "admissions", json.writeValueAsString(admissions), "receipts",
                            json.writeValueAsString(receipts)))) {
                insert.executeUpdate();
            }
        } catch (final com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new SQLException("Legacy reconciliation evidence could not be encoded");
        }
        // All final processes are persistently fenced and attest zero actual legacy sessions.
        // A delayed request cannot open another session; this is evidence-based reconciliation, not expiry.
        try (final var delete = transaction.prepare("deleteHostPasswordLegacyAdmissions",
                Map.of("host", access.host()))) {
            delete.executeUpdate();
        }
        return legacyDrainProgress(transaction, access.host());
    }

    public LegacyDrainProgress legacyDrainProgress(final Access access) throws IOException {
        return work(transaction -> {
            requireAccess(transaction, access);
            return legacyDrainProgress(transaction, access.host());
        });
    }

    static LegacyDrainProgress legacyDrainProgress(final DataBase.ScriptTransaction transaction, final long host)
            throws SQLException {
        int routes, fresh, sessions;
        try (final var query = transaction.prepare("getHostPasswordLegacyProgress", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                rows.next();
                routes = rows.getInt(1);
                fresh = rows.getInt(2);
                sessions = rows.getInt(3);
            }
        }
        try (final var query = transaction.prepare("countHostPasswordLegacyAdmissions", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                rows.next();
                return new LegacyDrainProgress(routes, fresh, sessions, rows.getInt(1));
            }
        }
    }

    public record ProbeHost(String destination, String directory, String transferMethod, String moduleName,
            String moduleClass, String archive, String methodValue, String hostData) {
        public ProbeHost {
            archive = archive == null ? "" : archive;
            hostData = hostData == null ? "" : hostData;
        }

        public static ProbeHost of(final String destination, final Host host) {
            final var method = host.getTransferMethod();
            final var module = method == null ? null : method.getECtransModule();
            var data = host.getData();
            if (module != null) {
                try {
                    final var approved = ecmwf.common.ectrans.PasswordRotation.approved(Long.parseLong(host.getName()));
                    if (approved.matches(module.getClasse(), module.getArchive())) {
                        data = approved.capability().snapshotHostData(data);
                    }
                } catch (final IllegalArgumentException unapproved) {
                    // Legacy/unapproved Hosts retain their ordinary fingerprint; eligibility is checked separately.
                }
            }
            return new ProbeHost(destination, host.getDir(), host.getTransferMethodName(),
                    module == null ? "" : module.getName(), module == null ? "" : module.getClasse(),
                    module == null ? "" : module.getArchive(), method == null ? "" : method.getValue(), data);
        }

        public String fingerprint() {
            try {
                final var digest = MessageDigest.getInstance("SHA-256");
                for (final var value : new String[] { destination, directory, transferMethod, moduleName, moduleClass,
                        archive, methodValue, hostData }) {
                    final var bytes = value.getBytes(StandardCharsets.UTF_8);
                    digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                    digest.update((byte) ':');
                    digest.update(bytes);
                }
                return HexFormat.of().formatHex(digest.digest());
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public ProbeHost reserveEligibleProbe(final Access access, final UUID reference) throws IOException {
        final var result = work(transaction -> {
            final var snapshot = requireAccess(transaction, access);
            return reserveProbe(transaction, access.host(), reference) ? snapshot : null;
        });
        if (result == null) {
            throw new IOException("Host is ineligible or its probe limit was reached");
        }
        return result;
    }

    static ProbeHost requireAccess(final DataBase.ScriptTransaction transaction, final Access access)
            throws SQLException {
        final var snapshot = eligibleProbeHost(transaction, access.host(), access.address(), access.port(),
                access.login());
        if (snapshot == null || !snapshot.destination().equals(access.destination())
                || !hasRotationGrant(transaction, access.destination(), access.user())
                || !hasDestinationOperations(transaction, access.destination(), access.user())) {
            throw new SQLException("Host rotation not permitted");
        }
        return snapshot;
    }

    /**
     * An empty result means the Host has never been activated. An activated Host whose approval no longer matches must
     * fail delivery rather than use its legacy password.
     */
    public Optional<UUID> findActiveForDelivery(final long host, final String destination, final String endpoint,
            final String address, final int port, final String login) throws IOException {
        return work(
                transaction -> findActiveForDelivery(transaction, host, destination, endpoint, address, port, login));
    }

    public Optional<UUID> findActiveForDispatch(final long host, final ProbeHost selectedHost, final String endpoint,
            final String address, final int port, final String login) throws IOException {
        return work(
                transaction -> findActiveForDispatch(transaction, host, selectedHost, endpoint, address, port, login));
    }

    static Optional<UUID> findActiveForDispatch(final DataBase.ScriptTransaction transaction, final long host,
            final ProbeHost selectedHost, final String endpoint, final String address, final int port,
            final String login) throws SQLException {
        if (!hasActive(transaction, host, false)) {
            return Optional.empty();
        }
        return findActiveForDelivery(transaction, host, selectedHost.destination(), endpoint, address, port, login,
                selectedHost);
    }

    public void requireLegacyDispatchAllowed(final long host, final boolean allowUnmigrated) throws IOException {
        if (work(transaction -> rejectsLegacyDispatch(transaction, host, allowUnmigrated))) {
            throw new IOException("Legacy credential dispatch is blocked for this Host");
        }
    }

    public Set<Long> blockedLegacyDispatchHosts(final List<Long> hosts, final boolean allowUnmigrated)
            throws IOException {
        if (hosts.isEmpty()) {
            return Set.of();
        }
        final var value = hosts.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
        return work(transaction -> {
            final Set<Long> blocked = new HashSet<>();
            try (final var query = transaction.prepare("getHostPasswordLegacyDispatchBlocks", Map.of("hosts", value));
                    final var rows = query.executeQuery()) {
                while (rows.next()) {
                    blocked.add(rows.getLong(1));
                }
                return blocked;
            } catch (final SQLException e) {
                if (!allowUnmigrated || !isMissingTable(e)) {
                    throw e;
                }
            }
            // Either marker table may still exist in a partially provisioned schema.
            blocked.clear();
            for (final var host : hosts) {
                if (hasActive(transaction, host, true) || hasLegacyFence(transaction, host, true)) {
                    blocked.add(host);
                }
            }
            return blocked;
        });
    }

    static boolean rejectsLegacyDispatch(final DataBase.ScriptTransaction transaction, final long host,
            final boolean allowUnmigrated) throws SQLException {
        try {
            return hasHostMarker(transaction, host, false, "getHostPasswordLegacyDispatchBlock");
        } catch (final SQLException e) {
            if (!allowUnmigrated || !isMissingTable(e)) {
                throw e;
            }
        }
        // A missing table does not mean the other table has no marker.
        return hasActive(transaction, host, allowUnmigrated) || hasLegacyFence(transaction, host, allowUnmigrated);
    }

    public LegacyAdmission admitLegacy(final long host, final String mover, final String operation) throws IOException {
        final var admission = new LegacyAdmission(host, UUID.randomUUID());
        work(transaction -> {
            admitLegacy(transaction, admission, mover, operation);
            return null;
        });
        return admission;
    }

    static void admitLegacy(final DataBase.ScriptTransaction transaction, final LegacyAdmission admission,
            final String mover, final String operation) throws SQLException {
        final var host = admission.host();
        if (!lockHost(transaction, host) || hasHostMarker(transaction, host, false, "lockHostPasswordLegacyFence")
                || hasHostMarker(transaction, host, false, "lockHostPasswordActive")) {
            throw new SQLException("Legacy credential admission refused");
        }
        try (final var insert = transaction.prepare("insertHostPasswordLegacyAdmission", Map.of("host", host,
                "admissionId", admission.id().toString(), "mover", mover, "operation", operation))) {
            insert.executeUpdate();
        }
    }

    public void finishLegacy(final LegacyAdmission admission) throws IOException {
        work(transaction -> {
            finishLegacy(transaction, admission);
            return null;
        });
    }

    static void finishLegacy(final DataBase.ScriptTransaction transaction, final LegacyAdmission admission)
            throws SQLException {
        try (final var delete = transaction.prepare("deleteHostPasswordLegacyAdmission",
                Map.of("host", admission.host(), "admissionId", admission.id().toString()))) {
            delete.executeUpdate();
        }
    }

    static boolean fenceLegacy(final DataBase.ScriptTransaction transaction, final long host) throws SQLException {
        if (!lockHost(transaction, host)) {
            throw new SQLException("Host is unavailable for legacy fencing");
        }
        try (final var insert = transaction.prepare("insertHostPasswordLegacyFence", Map.of("host", host))) {
            return insert.executeUpdate() == 1;
        }
    }

    private static boolean hasActive(final DataBase.ScriptTransaction transaction, final long host,
            final boolean allowUnmigrated) throws SQLException {
        return hasHostMarker(transaction, host, allowUnmigrated, "getHostPasswordActiveMarker");
    }

    private static boolean hasLegacyFence(final DataBase.ScriptTransaction transaction, final long host,
            final boolean allowUnmigrated) throws SQLException {
        return hasHostMarker(transaction, host, allowUnmigrated, "getHostPasswordLegacyFenceMarker");
    }

    private static boolean hasHostMarker(final DataBase.ScriptTransaction transaction, final long host,
            final boolean allowUnmigrated, final String queryName) throws SQLException {
        try (final var query = transaction.prepare(queryName, Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                return rows.next();
            }
        } catch (final SQLException e) {
            if (allowUnmigrated && isMissingTable(e)) {
                return false;
            }
            throw e;
        }
    }

    private static boolean isMissingTable(final SQLException e) {
        return e.getErrorCode() == 1146 && "42S02".equals(e.getSQLState());
    }

    static Optional<UUID> findActiveForDelivery(final DataBase.ScriptTransaction transaction, final long host,
            final String destination, final String endpoint, final String address, final int port, final String login)
            throws SQLException {
        return findActiveForDelivery(transaction, host, destination, endpoint, address, port, login, null);
    }

    static Optional<UUID> findActiveForDelivery(final DataBase.ScriptTransaction transaction, final long host,
            final String destination, final String endpoint, final String address, final int port, final String login,
            final ProbeHost selectedHost) throws SQLException {
        if (!lockHost(transaction, host)) {
            throw new SQLException("Delivery Host is unavailable");
        }
        try (final var query = transaction.prepare("getHostPasswordApprovedActive", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                final var current = address == null || login == null ? null
                        : eligibleProbeHostLocked(transaction, host, address, port, login);
                if (current == null || !current.destination().equals(destination)
                        || !current.destination().equals(rows.getString(2)) || endpoint == null
                        || !endpoint.equals(rows.getString(3)) || !current.fingerprint().equals(rows.getString(4))
                        || selectedHost != null && !selectedHost.equals(current)) {
                    throw new SQLException("Activated credential does not match the delivery configuration");
                }
                return Optional.of(UUID.fromString(rows.getString(1)));
            }
        }
    }

    static ProbeHost eligibleProbeHost(final DataBase.ScriptTransaction transaction, final long host,
            final String address, final int port, final String login) throws SQLException {
        if (!lockHost(transaction, host)) {
            return null;
        }
        return eligibleProbeHostLocked(transaction, host, address, port, login);
    }

    private static ProbeHost eligibleProbeHostLocked(final DataBase.ScriptTransaction transaction, final long host,
            final String address, final int port, final String login) throws SQLException {
        try (final var query = transaction.prepare("getHostPasswordEligibleProbeHost", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next() || !address.equals(rows.getString(1)) || !login.equals(rows.getString(2))
                        || rows.getInt(6) != 1) {
                    return null;
                }
                final ecmwf.common.ectrans.PasswordRotation.Approved approved;
                try {
                    approved = ecmwf.common.ectrans.PasswordRotation.approved(host);
                } catch (final IllegalArgumentException failure) {
                    return null;
                }
                if (!approved.matches(rows.getString(7), rows.getString(8)) || !address.equals(approved.host())
                        || port != approved.port() || !login.equals(approved.login())
                        || !approved.capability().supports(rows.getString(9), rows.getString(10), port)) {
                    return null;
                }
                final var snapshot = new ProbeHost(rows.getString(5), rows.getString(3), rows.getString(4),
                        rows.getString(11), rows.getString(7), rows.getString(8), rows.getString(9),
                        approved.capability().snapshotHostData(rows.getString(10)));
                return rows.next() ? null : snapshot;
            }
        }
    }

    <T> T work(final DataBase.ScriptWork<T> work) throws IOException {
        try {
            return database.scriptTransaction("ECpdsBase", work);
        } catch (final RuntimeException _) {
            throw new IOException("Host password rotation database operation failed");
        }
    }

    public boolean reserve(final Access access, final UUID reference) throws IOException {
        return reserve(access, reference, 86400);
    }

    public boolean reserve(final Access access, final UUID reference, final long retirementGraceSeconds)
            throws IOException {
        validateRetirementGrace(retirementGraceSeconds);
        return work(transaction -> {
            requireAccess(transaction, access);
            if (!reserve(transaction, access.host(), reference)) {
                return false;
            }
            try (final var update = transaction.prepare("updateHostPasswordRetirementGrace", Map.of("graceSeconds",
                    retirementGraceSeconds, "host", access.host(), "reference", reference.toString()))) {
                return update.executeUpdate() == 1;
            }
        });
    }

    public static void validateRetirementGrace(final long seconds) {
        if (seconds < 3600 || seconds > 30 * 86400 || seconds % 3600 != 0) {
            throw new IllegalArgumentException("Retirement grace must be a whole number of hours from 1 to 720");
        }
    }

    static boolean reserve(final DataBase.ScriptTransaction transaction, final long host, final UUID reference)
            throws SQLException {
        Objects.requireNonNull(reference);
        if (!lockHost(transaction, host) || find(transaction, host).isPresent()) {
            return false;
        }
        try (final var insert = transaction.prepare("insertHostPasswordRotation",
                Map.of("host", host, "reference", reference.toString()))) {
            return insert.executeUpdate() == 1;
        }
    }

    public Optional<Pending> find(final Access access) throws IOException {
        return work(transaction -> {
            requireAccess(transaction, access);
            return find(transaction, access.host());
        });
    }

    static Optional<Pending> find(final DataBase.ScriptTransaction transaction, final long host) throws SQLException {
        try (final var query = transaction.prepare("getHostPasswordRotation", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                final var testedAt = rows.getTimestamp(5);
                return Optional.of(new Pending(UUID.fromString(rows.getString(1)), State.valueOf(rows.getString(2)),
                        rows.getTimestamp(3).toInstant(), rows.getString(4),
                        testedAt == null ? null : testedAt.toInstant(), rows.getString(6), rows.getLong(7)));
            }
        }
    }

    public boolean markStaged(final long host, final UUID reference) throws IOException {
        return work(transaction -> transition(transaction, host, reference, State.STAGING, State.STAGED));
    }

    static boolean hasActivationPrerequisites(final DataBase.ScriptTransaction transaction, final long host,
            final UUID reference, final String destination, final String user, final String endpoint,
            final String address, final int port, final String login, final Duration maxAge) throws SQLException {
        return hasCurrentProbeProof(transaction, host, reference, destination, endpoint, address, port, login, maxAge)
                && hasRotationGrant(transaction, destination, user)
                && hasDestinationOperations(transaction, destination, user);
    }

    /**
     * Promotes a tested reference after Master step-up verification. The caller must commit or roll back the entire
     * transaction. Deliberately not exposed until delivery can consume the active reference.
     */
    static boolean promoteTestedPassword(final DataBase.ScriptTransaction transaction, final long host,
            final UUID reference, final String destination, final String user, final String endpoint,
            final String address, final int port, final String login, final Duration maxAge) throws SQLException {
        if (!hasActivationPrerequisites(transaction, host, reference, destination, user, endpoint, address, port, login,
                maxAge)) {
            return false;
        }
        final String active;
        try (final var query = transaction.prepare("lockHostPasswordPointers", Map.of("host", host))) {
            try (final var rows = query.executeQuery()) {
                if (rows.next()) {
                    if (rows.getString(2) != null) {
                        // Never lose track of a reference that has not been retired.
                        return false;
                    }
                    active = rows.getString(1);
                } else {
                    active = null;
                }
            }
        }
        if (active == null && (!hasHostMarker(transaction, host, false, "lockHostPasswordDrainedFence")
                || hasHostMarker(transaction, host, false, "lockHostPasswordLegacyAdmission"))) {
            return false;
        }
        try (final var audit = transaction.prepare("insertHostPasswordActivation",
                Map.of("destination", destination, "user", user, "host", host, "reference", reference.toString(),
                        "ageOffsetSeconds", -maxAge.toSeconds()))) {
            if (audit.executeUpdate() != 1) {
                return false;
            }
        }
        try (final var pointers = active == null
                ? transaction.prepare("insertHostPasswordActive",
                        Map.of("reference", reference.toString(), "host", host))
                : transaction.prepare("updateHostPasswordActive",
                        Map.of("reference", reference.toString(), "host", host, "activeReference", active))) {
            if (pointers.executeUpdate() != 1) {
                throw new SQLException("Password activation pointers changed");
            }
        }
        try (final var consume = transaction.prepare("deleteHostPasswordTestedRotation",
                Map.of("host", host, "reference", reference.toString()))) {
            if (consume.executeUpdate() != 1) {
                throw new SQLException("Tested password reference changed");
            }
        }
        return true;
    }

    private static boolean hasRotationGrant(final DataBase.ScriptTransaction transaction, final String destination,
            final String user) throws SQLException {
        try (final var query = transaction.prepare("getHostPasswordRotationGrant",
                Map.of("destination", destination, "user", user))) {
            try (final var rows = query.executeQuery()) {
                return rows.next();
            }
        }
    }

    private static boolean hasDestinationOperations(final DataBase.ScriptTransaction transaction,
            final String destination, final String user) throws SQLException {
        // Check the destination-specific Operations ACL, not a parent URL or a cached session category.
        try (final var query = transaction.prepare("getHostPasswordOperationsGrant",
                Map.of("user", user, "url", "/do/transfer/destination/operations/" + destination + "/"))) {
            try (final var rows = query.executeQuery()) {
                return rows.next();
            }
        }
    }

    static boolean hasCurrentProbeProof(final DataBase.ScriptTransaction transaction, final long host,
            final UUID reference, final String destination, final String endpoint, final String address, final int port,
            final String login, final Duration maxAge) throws SQLException {
        final long seconds = maxAge.toSeconds();
        if (seconds <= 0) {
            throw new IllegalArgumentException("Invalid probe proof lifetime");
        }
        final var current = eligibleProbeHost(transaction, host, address, port, login);
        if (current == null || !current.destination().equals(destination)) {
            return false;
        }
        // Keep these locks until the enclosing activation transaction commits.
        try (final var query = transaction.prepare("getHostPasswordProbeProof",
                Map.of("host", host, "reference", reference.toString(), "configuration", current.fingerprint(),
                        "endpoint", endpoint, "ageOffsetSeconds", -seconds))) {
            try (final var rows = query.executeQuery()) {
                return rows.next();
            }
        }
    }

    public boolean recordPassedProbe(final long host, final UUID reference, final String endpoint,
            final ProbeHost expected, final String address, final int port, final String login) throws IOException {
        return work(transaction -> recordPassedProbe(transaction, host, reference, endpoint, expected, address, port,
                login));
    }

    static boolean recordPassedProbe(final DataBase.ScriptTransaction transaction, final long host,
            final UUID reference, final String endpoint, final ProbeHost expected, final String address, final int port,
            final String login) throws SQLException {
        if (endpoint == null || !endpoint.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid probe endpoint fingerprint");
        }
        if (!expected.equals(eligibleProbeHost(transaction, host, address, port, login))) {
            return false;
        }
        try (final var update = transaction.prepare("updateHostPasswordPassedProbe", Map.of("endpoint", endpoint,
                "configuration", expected.fingerprint(), "host", host, "reference", reference.toString()))) {
            return update.executeUpdate() == 1;
        }
    }

    static boolean reserveProbe(final DataBase.ScriptTransaction transaction, final long host, final UUID reference)
            throws SQLException {
        if (!lockHost(transaction, host)) {
            return false;
        }
        try (final var check = transaction.prepare("lockHostPasswordProbeCandidate",
                Map.of("host", host, "reference", reference.toString()))) {
            try (final var rows = check.executeQuery()) {
                if (!rows.next()) {
                    return false;
                }
            }
        }
        try (final var expire = transaction.prepare("deleteHostPasswordExpiredProbes", Map.of("host", host))) {
            expire.executeUpdate();
        }
        try (final var count = transaction.prepare("getHostPasswordProbeAttempts", Map.of("host", host))) {
            try (final var rows = count.executeQuery()) {
                if (rows.next() && rows.next() && rows.next()) {
                    return false;
                }
            }
        }
        try (final var insert = transaction.prepare("insertHostPasswordProbeAttempt", Map.of("host", host))) {
            if (insert.executeUpdate() != 1) {
                return false;
            }
        }
        try (final var update = transaction.prepare("updateHostPasswordProbeReset",
                Map.of("host", host, "reference", reference.toString()))) {
            return update.executeUpdate() == 1;
        }
    }

    static boolean lockHost(final DataBase.ScriptTransaction transaction, final long host) throws SQLException {
        try (final var lock = transaction.prepare("lockHostPasswordHost", Map.of("host", host))) {
            try (final var rows = lock.executeQuery()) {
                return rows.next();
            }
        }
    }

    public boolean beginDiscard(final Access access, final UUID reference) throws IOException {
        return work(transaction -> {
            requireAccess(transaction, access);
            final var host = access.host();
            try (final var update = transaction.prepare("updateHostPasswordDiscarding",
                    Map.of("host", host, "reference", reference.toString()))) {
                return update.executeUpdate() == 1 || find(transaction, host)
                        .filter(p -> p.reference().equals(reference) && p.state() == State.DISCARDING).isPresent();
            }
        });
    }

    public void finishDiscard(final long host, final UUID reference) throws IOException {
        work(transaction -> {
            try (final var delete = transaction.prepare("deleteHostPasswordDiscardedRotation",
                    Map.of("host", host, "reference", reference.toString()))) {
                delete.executeUpdate();
                return null;
            }
        });
    }

    static boolean transition(final DataBase.ScriptTransaction transaction, final long host, final UUID reference,
            final State expected, final State next) throws SQLException {
        try (final var update = transaction.prepare("updateHostPasswordRotationState", Map.of("state", next.name(),
                "host", host, "reference", reference.toString(), "expectedState", expected.name()))) {
            return update.executeUpdate() == 1;
        }
    }
}
