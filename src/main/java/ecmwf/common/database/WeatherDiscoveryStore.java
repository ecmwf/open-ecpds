package ecmwf.common.database;

import java.util.*;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;
import ecmwf.ecpds.master.weather.WeatherLimits;

/** Coordinates bounded discovery using the standard ECpdsBase SQL requests. */
public final class WeatherDiscoveryStore {
    private final ECpdsBase database;

    public WeatherDiscoveryStore(ECpdsBase database) {
        this.database = Objects.requireNonNull(database);
    }

    public record Candidate(Path path, String nickname, String group) implements java.io.Serializable {
    }

    public List<Candidate> successfulCandidates(String source, int days, String protocol) throws DataBaseException {
        if (days < 1 || days > 30)
            throw new IllegalArgumentException("Choose a history period from 1 to 30 days");
        new Path(source, "validation", "validation", "validation", "DISSEMINATION");
        if (protocol == null || protocol.length() > 128 || protocol.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid protocol filter");
        final long now = System.currentTimeMillis();
        final var limits = WeatherLimits.configured();
        final var candidates = new ArrayList<Candidate>();
        for (final var host : database.getWeatherSuccessfulHosts(protocol, now - days * 86400_000L, now,
                (long) limits.maxHostChoices() + 1)) {
            try {
                candidates
                        .add(new Candidate(new Path(source, host.name(), host.target(), host.method(), "DISSEMINATION"),
                                host.nickname(), host.group()));
            } catch (IllegalArgumentException e) {
                throw new DataBaseException("Cannot suggest Host " + host.name() + ": " + e.getMessage());
            }
            if (candidates.size() > limits.maxHostChoices())
                throw new DataBaseException("History matches exceed the configured " + limits.maxHostChoices()
                        + " Host search limit (maxHostChoices). Choose a shorter period or raise the limit.");
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

    public Result discover(Set<Path> approved, Progress cursor) throws DataBaseException {
        return discover(approved, cursor, System.nanoTime() + 30_000_000_000L);
    }

    public Result discover(Set<Path> approved, Progress cursor, long deadline) throws DataBaseException {
        if (System.nanoTime() > deadline)
            throw new DataBaseException("Discovery exhausted its run budget");
        return read(approved, cursor, System.currentTimeMillis(), deadline);
    }

    public Set<Path> candidates(Set<Path> approved, Map<String, String> sourceGroups) throws DataBaseException {
        final var limits = WeatherLimits.configured();
        final long deadline = System.nanoTime() + 30_000_000_000L;
        final var candidates = new LinkedHashSet<Path>();
        if (approved.size() > limits.maxCandidates())
            throw new DataBaseException("Approved paths exceed the configured candidate limit");
        final var byHost = approved.stream().collect(java.util.stream.Collectors.groupingBy(Path::hostId));
        final var ids = byHost.keySet().stream().sorted().toList();
        for (int offset = 0; offset < ids.size(); offset += 500) {
            if (System.nanoTime() > deadline)
                throw new DataBaseException("Candidate discovery exhausted its 30-second budget");
            final var batch = ids.subList(offset, Math.min(ids.size(), offset + 500));
            for (final var host : database.getWeatherApprovedHosts(batch)) {
                for (final var path : byHost.getOrDefault(host.name(), List.of())) {
                    if (path.configuredTarget().equals(host.target()) && path.protocol().equals(host.method()))
                        candidates.add(path);
                }
            }
        }
        for (final var source : sourceGroups.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (System.nanoTime() > deadline)
                throw new DataBaseException("Candidate discovery exhausted its 30-second budget");
            for (final var host : database.getWeatherGroupHosts(source.getValue(), (long) limits.maxCandidates() + 1)) {
                try {
                    candidates
                            .add(new Path(source.getKey(), host.name(), host.target(), host.method(), "DISSEMINATION"));
                } catch (IllegalArgumentException e) {
                    throw new DataBaseException("Cannot discover Network Weather candidates for source "
                            + source.getKey() + ", transfer group " + source.getValue() + ": Host " + host.name()
                            + " has an invalid configuration (" + e.getMessage() + ")");
                }
                if (candidates.size() > limits.maxCandidates())
                    throw new DataBaseException("Approved groups exceed the configured " + limits.maxCandidates()
                            + " candidate limit; narrow the scope or raise the limit");
            }
        }
        return Set.copyOf(candidates);
    }

    private Result read(Set<Path> approved, Progress cursor, long now, long deadline) throws DataBaseException {
        if (approved.isEmpty())
            return new Result(Map.of(), 0, 0, false, Progress.empty());
        final long since = now - 7L * 86400_000;
        final var signals = new HashMap<String, Signal>();
        final var hosts = approved.stream().map(Path::hostId).collect(java.util.stream.Collectors.toSet());
        final var targets = new HashMap<String, Set<String>>();
        for (final var path : approved) {
            targets.computeIfAbsent(path.configuredTarget(), _ -> new HashSet<>()).add(path.hostId());
        }
        int rows = 0, pages = 0;
        long time = Math.max(since, cursor.uploadTime()), id = time == cursor.uploadTime() ? cursor.uploadId() : -1;
        boolean partial = cursor.uploadTime() > since || !cursor.destination().isEmpty(), uploadComplete = false;
        for (int page = 0; page < 10 && System.nanoTime() < deadline; page++) {
            int count = 0;
            for (final var upload : database.getWeatherUploadHistory(since, now, time, id)) {
                count++;
                rows++;
                time = upload.time();
                id = upload.id();
                for (final var host : targets.getOrDefault(upload.target(), Set.of())) {
                    signals.put(host, signals.getOrDefault(host, new Signal(0, 0, 0, 0)).plus(1,
                            Math.max(0, upload.bytes()), 0, time));
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
        final boolean destinationsRead = System.nanoTime() < deadline;
        if (destinationsRead) {
            cohort.addAll(database.getWeatherDestinations(orderedHosts, cursor.destination()));
            rows += cohort.size();
        } else
            partial = true;
        int visited = 0;
        String destinationCursor = cursor.destination();
        while (visited < Math.min(100, cohort.size()) && rows < 10_000 && System.nanoTime() < deadline) {
            final var destination = cohort.get(visited++);
            destinationCursor = destination;
            int count = 0;
            for (final var transfer : database.getWeatherTransferHistory(destination, since, now,
                    Math.min(50, 10_000 - rows))) {
                rows++;
                count++;
                final var host = transfer.host();
                if (hosts.contains(host)) {
                    signals.put(host, signals.getOrDefault(host, new Signal(0, 0, 0, 0)).plus(0, 0,
                            transfer.error() ? 1 : 0, transfer.time()));
                }
            }
            if (count == 50)
                partial = true;
            pages++;
        }
        final boolean destinationsComplete = destinationsRead && visited == cohort.size();
        partial |= !uploadComplete || !destinationsComplete || System.nanoTime() >= deadline;
        return new Result(Map.copyOf(signals), rows, pages, partial, new Progress(uploadComplete ? 0 : time,
                uploadComplete ? -1 : id, destinationsComplete ? "" : destinationCursor));
    }
}
