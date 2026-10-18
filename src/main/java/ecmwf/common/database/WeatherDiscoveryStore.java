package ecmwf.common.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;
import ecmwf.ecpds.master.weather.WeatherLimits;

/** Optional history work borrows from the existing pool with its own 100-ms acquisition deadline. */
public final class WeatherDiscoveryStore {
    public record Candidate(Path path, String nickname, String group) implements java.io.Serializable {
    }

    public List<Candidate> successfulCandidates(String source, int days, String protocol) throws SQLException {
        return withConnection(connection -> readSuccessfulCandidates(connection, source, days, protocol,
                System.currentTimeMillis(), WeatherLimits.configured()));
    }

    static List<Candidate> readSuccessfulCandidates(Connection connection, String source, int days, String protocol,
            long now, WeatherLimits limits) throws SQLException {
        if (days < 1 || days > 30)
            throw new IllegalArgumentException("Choose a history period from 1 to 30 days");
        new Path(source, "validation", "validation", "validation", "DISSEMINATION");
        if (protocol == null || protocol.length() > 128 || protocol.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid protocol filter");
        final var candidates = new ArrayList<Candidate>();
        try (final var query = connection.prepareStatement("""
                SELECT H.HOS_NAME,H.HOS_HOST,H.TME_NAME,H.HOS_NICKNAME,H.TRG_NAME
                FROM HOST H JOIN TRANSFER_METHOD M ON M.TME_NAME=H.TME_NAME
                WHERE H.HOS_ACTIVE=1 AND H.HOS_TYPE='Dissemination'
                  AND (?='' OR M.ECM_NAME=?)
                  AND EXISTS (SELECT 1 FROM TRANSFER_HISTORY T FORCE INDEX (HOS_NAME)
                    WHERE T.HOS_NAME=H.HOS_NAME AND T.STA_CODE='DONE'
                      AND T.TRH_TIME>=? AND T.TRH_TIME<=?)
                ORDER BY H.HOS_NAME LIMIT ?
                """)) {
            query.setQueryTimeout(2);
            query.setString(1, protocol);
            query.setString(2, protocol);
            query.setLong(3, now - days * 86400_000L);
            query.setLong(4, now);
            query.setLong(5, (long) limits.maxHostChoices() + 1);
            try (final var rows = query.executeQuery()) {
                while (rows.next()) {
                    final var host = rows.getString(1);
                    try {
                        candidates.add(new Candidate(
                                new Path(source, host, rows.getString(2), rows.getString(3), "DISSEMINATION"),
                                rows.getString(4), rows.getString(5)));
                    } catch (IllegalArgumentException e) {
                        throw new SQLException("Cannot suggest Host " + host + ": " + e.getMessage(), e);
                    }
                    if (candidates.size() > limits.maxHostChoices())
                        throw new SQLException("History matches exceed the configured " + limits.maxHostChoices()
                                + " Host search limit (maxHostChoices). Choose a shorter period or raise the limit.");
                }
            }
        }
        return List.copyOf(candidates);
    }

    public record Signal(long activity, long bytes, long trouble, long latest) {
        Signal plus(long activity, long bytes, long trouble, long latest) {
            return new Signal(this.activity + activity, this.bytes + bytes, this.trouble + trouble,
                    Math.max(this.latest, latest));
        }
    }

    public record Progress(long uploadTime, long uploadId, String destination) implements java.io.Serializable {
        public Progress {
            if (uploadTime < 0 || uploadId < -1 || destination == null || destination.length() > 255)
                throw new IllegalArgumentException("Invalid discovery cursor");
        }

        public static Progress empty() {
            return new Progress(0, -1, "");
        }
    }

    public record Result(Map<String, Signal> hosts, int rows, int pages, boolean partial, Progress nextCursor) {
    }

    public Result discover(Set<Path> approved, Progress cursor) throws SQLException {
        return withConnection(connection -> read(connection, approved, cursor, System.currentTimeMillis()));
    }

    public Result discover(Set<Path> approved, Progress cursor, long deadline) throws SQLException {
        if (System.nanoTime() > deadline - 2_000_000_000L)
            throw new SQLException("Discovery exhausted its run budget");
        return withConnection(connection -> read(connection, approved, cursor, System.currentTimeMillis(), deadline));
    }

    @FunctionalInterface
    interface Read<T> {
        T run(Connection connection) throws SQLException;
    }

    private static <T> T withConnection(Read<T> read) throws SQLException {
        final var factory = HibernateSessionFactory.getSessionFactory();
        if (!(factory instanceof SessionFactoryImplementor implementor)) {
            throw new SQLException("Discovery skipped: supported session factory unavailable");
        }
        final var provider = implementor.getServiceRegistry().getService(ConnectionProvider.class);
        if (provider == null || !provider.isUnwrappableAs(DataSource.class)
                || !(provider.unwrap(DataSource.class) instanceof HikariDataSource datasource)) {
            throw new SQLException("Discovery skipped: unsupported bounded acquisition");
        }
        return withConnection(datasource, read);
    }

    static <T> T withConnection(HikariDataSource datasource, Read<T> read) throws SQLException {
        if (!(datasource.getHikariPoolMXBean() instanceof HikariPool pool) || pool.getIdleConnections() < 2
                || pool.getThreadsAwaitingConnection() != 0) {
            throw new SQLException("Discovery skipped: shared pool pressure or unsupported bounded acquisition");
        }
        try (final var connection = pool.getConnection(100)) {
            final boolean readOnly = connection.isReadOnly();
            final int networkTimeout = connection.getNetworkTimeout();
            try {
                connection.setNetworkTimeout(Runnable::run, networkTimeout > 0 ? Math.min(networkTimeout, 3000) : 3000);
                connection.setReadOnly(true);
                return read.run(connection);
            } finally {
                connection.setNetworkTimeout(Runnable::run, networkTimeout);
                connection.setReadOnly(readOnly);
            }
        }
    }

    public Set<Path> candidates(Set<Path> approved, Map<String, String> sourceGroups) throws SQLException {
        return withConnection(connection -> readCandidates(connection, approved, sourceGroups));
    }

    static Set<Path> readCandidates(Connection connection, Set<Path> approved, Map<String, String> sourceGroups)
            throws SQLException {
        return readCandidates(connection, approved, sourceGroups, WeatherLimits.configured());
    }

    static Set<Path> readCandidates(Connection connection, Set<Path> approved, Map<String, String> sourceGroups,
            WeatherLimits limits) throws SQLException {
        final long deadline = System.nanoTime() + 30_000_000_000L;
        final var candidates = new LinkedHashSet<Path>();
        if (approved.size() > limits.maxCandidates())
            throw new SQLException("Approved paths exceed the configured candidate limit");
        final var byHost = approved.stream().collect(java.util.stream.Collectors.groupingBy(Path::hostId));
        final var ids = byHost.keySet().stream().sorted().toList();
        for (int offset = 0; offset < ids.size(); offset += 500) {
            if (System.nanoTime() > deadline - 2_000_000_000L)
                throw new SQLException("Candidate discovery exhausted its 30-second budget");
            final var batch = ids.subList(offset, Math.min(ids.size(), offset + 500));
            try (final var query = connection.prepareStatement(
                    "SELECT HOS_NAME,HOS_HOST,TME_NAME FROM HOST WHERE HOS_ACTIVE=1 AND HOS_TYPE='Dissemination' AND HOS_NAME IN ("
                            + String.join(",", Collections.nCopies(batch.size(), "?")) + ")")) {
                query.setQueryTimeout(2);
                for (int i = 0; i < batch.size(); i++)
                    query.setString(i + 1, batch.get(i));
                try (final var rows = query.executeQuery()) {
                    while (rows.next()) {
                        for (final var path : byHost.getOrDefault(rows.getString(1), List.of())) {
                            if (path.configuredTarget().equals(rows.getString(2))
                                    && path.protocol().equals(rows.getString(3)))
                                candidates.add(path);
                        }
                    }
                }
            }
        }
        for (final var source : sourceGroups.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (System.nanoTime() > deadline - 2_000_000_000L)
                throw new SQLException("Candidate discovery exhausted its 30-second budget");
            try (final var query = connection.prepareStatement("""
                    SELECT HOS_NAME,HOS_HOST,TME_NAME FROM HOST FORCE INDEX (TRG_NAME)
                    WHERE TRG_NAME=? AND HOS_ACTIVE=1 AND HOS_TYPE='Dissemination'
                    ORDER BY HOS_NAME LIMIT ?
                    """)) {
                query.setQueryTimeout(2);
                query.setString(1, source.getValue());
                query.setInt(2, limits.maxCandidates() + 1);
                try (final var rows = query.executeQuery()) {
                    while (rows.next()) {
                        final var host = rows.getString(1);
                        try {
                            candidates.add(new Path(source.getKey(), host, rows.getString(2), rows.getString(3),
                                    "DISSEMINATION"));
                        } catch (IllegalArgumentException e) {
                            throw new SQLException("Cannot discover Network Weather candidates for source "
                                    + source.getKey() + ", transfer group " + source.getValue() + ": Host " + host
                                    + " has an invalid configuration (" + e.getMessage() + ")", e);
                        }
                        if (candidates.size() > limits.maxCandidates())
                            throw new SQLException("Approved groups exceed the configured " + limits.maxCandidates()
                                    + " candidate limit; narrow the scope or raise the limit");
                    }
                }
            }
        }
        return Set.copyOf(candidates);
    }

    public static Result read(Connection connection, Set<Path> approved, Progress cursor, long now)
            throws SQLException {
        return read(connection, approved, cursor, now, System.nanoTime() + 30_000_000_000L);
    }

    private static Result read(Connection connection, Set<Path> approved, Progress cursor, long now, long deadline)
            throws SQLException {
        if (approved.isEmpty())
            return new Result(Map.of(), 0, 0, false, Progress.empty());
        final long since = now - 7L * 86400_000;
        final var signals = new HashMap<String, Signal>();
        final var hosts = approved.stream().map(Path::hostId).collect(java.util.stream.Collectors.toSet());
        final var targets = new HashMap<String, Set<String>>();
        for (final var path : approved) {
            targets.computeIfAbsent(path.configuredTarget(), ignored -> new HashSet<>()).add(path.hostId());
        }
        int rows = 0, pages = 0;
        long time = Math.max(since, cursor.uploadTime()), id = time == cursor.uploadTime() ? cursor.uploadId() : -1;
        boolean partial = cursor.uploadTime() > since || !cursor.destination().isEmpty(), uploadComplete = false;
        for (int page = 0; page < 10 && System.nanoTime() < deadline - 2_000_000_000L; page++) {
            int count = 0;
            try (final var query = connection.prepareStatement("""
                    SELECT UPH_QUEUE_TIME,UPH_ID,UPH_HOST_ADDRESS,UPH_FILE_SIZE
                    FROM UPLOAD_HISTORY FORCE INDEX (UPLOAD_HISTORY_UPH_QUEUE_TIME_IDX)
                    WHERE UPH_QUEUE_TIME>=? AND UPH_QUEUE_TIME<=?
                      AND (UPH_QUEUE_TIME>? OR (UPH_QUEUE_TIME=? AND UPH_ID>?))
                    ORDER BY UPH_QUEUE_TIME,UPH_ID LIMIT 500
                    """)) {
                query.setQueryTimeout(2);
                query.setLong(1, since);
                query.setLong(2, now);
                query.setLong(3, time);
                query.setLong(4, time);
                query.setLong(5, id);
                try (final var result = query.executeQuery()) {
                    while (result.next()) {
                        count++;
                        rows++;
                        time = result.getLong(1);
                        id = result.getLong(2);
                        for (final var host : targets.getOrDefault(result.getString(3), Set.of())) {
                            signals.put(host, signals.getOrDefault(host, new Signal(0, 0, 0, 0)).plus(1,
                                    Math.max(0, result.getLong(4)), 0, time));
                        }
                    }
                }
            }
            pages++;
            if (count < 500) {
                uploadComplete = true;
                break;
            }
            if (page == 9)
                partial = true;
        }
        final var cohort = new ArrayList<String>();
        final var orderedHosts = hosts.stream().sorted().toList();
        if (System.nanoTime() < deadline - 2_000_000_000L) {
            try (final var query = connection.prepareStatement(
                    "SELECT DISTINCT DES_NAME FROM ASSOCIATION FORCE INDEX (HOS_NAME) WHERE HOS_NAME IN ("
                            + String.join(",", Collections.nCopies(orderedHosts.size(), "?"))
                            + ") AND DES_NAME>? ORDER BY DES_NAME LIMIT 101")) {
                query.setQueryTimeout(2);
                int parameter = 1;
                for (final var host : orderedHosts)
                    query.setString(parameter++, host);
                query.setString(parameter, cursor.destination());
                try (final var result = query.executeQuery()) {
                    while (result.next()) {
                        rows++;
                        cohort.add(result.getString(1));
                    }
                }
            }
        } else
            partial = true;
        int visited = 0;
        String destinationCursor = cursor.destination();
        while (visited < Math.min(100, cohort.size()) && rows < 10_000
                && System.nanoTime() < deadline - 2_000_000_000L) {
            final var destination = cohort.get(visited++);
            destinationCursor = destination;
            try (final var query = connection.prepareStatement("""
                    SELECT HOS_NAME,TRH_TIME,TRH_ERROR FROM TRANSFER_HISTORY
                    FORCE INDEX (TRANSFER_HISTORY_DES_NAME_TRH_TIME)
                    WHERE DES_NAME=? AND TRH_TIME>=? AND TRH_TIME<=?
                    ORDER BY TRH_TIME,TRH_ID LIMIT ?
                    """)) {
                query.setQueryTimeout(2);
                query.setString(1, destination);
                query.setLong(2, since);
                query.setLong(3, now);
                query.setInt(4, Math.min(50, 10_000 - rows));
                int count = 0;
                try (final var result = query.executeQuery()) {
                    while (result.next()) {
                        rows++;
                        count++;
                        final var host = result.getString(1);
                        if (hosts.contains(host)) {
                            signals.put(host, signals.getOrDefault(host, new Signal(0, 0, 0, 0)).plus(0, 0,
                                    result.getBoolean(3) ? 1 : 0, result.getLong(2)));
                        }
                    }
                }
                if (count == 50)
                    partial = true;
            }
            pages++;
        }
        final boolean destinationsComplete = visited == cohort.size();
        partial |= !uploadComplete || !destinationsComplete || System.nanoTime() >= deadline;
        return new Result(Map.copyOf(signals), rows, pages, partial, new Progress(uploadComplete ? 0 : time,
                uploadComplete ? -1 : id, destinationsComplete ? "" : destinationCursor));
    }
}
