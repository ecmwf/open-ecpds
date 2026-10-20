package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import ecmwf.common.database.WeatherDiscoveryStore;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;

/** Scheduler-owned, optional discovery. Approved candidates are a policy boundary, not proof of historical source. */
public final class WeatherAutomation {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public record Configuration(long revision, boolean automatic, boolean historical, Set<Path> approved,
            Set<Path> pins, Set<Path> exclusions, boolean probesEnabled, Set<WeatherProbe.Policy> probePolicies,
            Map<String, String> sourceGroups) implements Serializable {
        public Configuration {
            approved = Set.copyOf(approved);
            pins = Set.copyOf(pins);
            exclusions = Set.copyOf(exclusions);
            probePolicies = Set.copyOf(probePolicies);
            sourceGroups = Map.copyOf(sourceGroups);
            if (revision < 1)
                throw new IllegalArgumentException("Invalid automatic-selection revision; reload before saving");
            for (final var path : pins)
                if (!approved.contains(path))
                    throw new IllegalArgumentException(
                            "Pinned path must first be added to Approved candidates: " + path);
            for (final var path : exclusions)
                if (!approved.contains(path))
                    throw new IllegalArgumentException(
                            "Excluded path must first be added to Approved candidates: " + path);
            for (final var path : approved)
                if (!"DISSEMINATION".equals(path.leg()))
                    throw new IllegalArgumentException("Approved candidates support only dissemination paths: " + path);
            for (final var policy : probePolicies)
                if (!approved.contains(policy.path()))
                    throw new IllegalArgumentException(
                            "Probe path must first be added to Approved candidates: " + policy.path());
            for (final var entry : sourceGroups.entrySet())
                if (entry.getValue().isBlank() || entry.getValue().length() > 32)
                    throw new IllegalArgumentException("Transfer group for source " + entry.getKey()
                            + " must contain between 1 and 32 characters");
            sourceGroups.keySet().forEach(WeatherCapabilities::validateSource);
        }
    }

    public record Status(Configuration configuration, long lastAttempt, long lastSuccess, long nextRun, int rows,
            int pages, boolean partial, String error, List<String> changes, boolean loaded, boolean running,
            int candidates, int added, int removed) implements Serializable {
    }

    public interface Environment {
        String load() throws Exception;

        void save(String document) throws Exception;

        Set<Path> candidates(Configuration policy) throws Exception;

        void select(Set<Path> paths) throws Exception;

        Set<Path> selected();

        void probes(boolean enabled, Set<WeatherProbe.Policy> policies);
    }

    private final Environment environment;
    private final WeatherDiscoveryStore database;
    private Configuration configuration = new Configuration(1, false, false, Set.of(), Set.of(), Set.of(), false,
            Set.of(), Map.of());
    private long lastAttempt, lastSuccess, nextRun;
    private int rows, pages;
    private WeatherDiscoveryStore.Progress cursor = WeatherDiscoveryStore.Progress.empty();
    private long lastRebalance;
    private int candidates, added, removed;
    private boolean partial, loaded, running;
    private String error;
    private List<String> changes = List.of();

    public record Saved(Configuration configuration, WeatherDiscoveryStore.Progress cursor, long lastRebalance) {
    }

    public WeatherAutomation(Environment environment, WeatherDiscoveryStore database) {
        this.environment = environment;
        this.database = database;
    }

    public synchronized void load() throws Exception {
        try {
            final var document = environment.load();
            if (document != null) {
                checkSize(document);
                final var saved = JSON.readValue(document, Saved.class);
                configuration = Objects.requireNonNull(saved.configuration());
                WeatherLimits.configured().validatePolicy(configuration);
                cursor = Objects.requireNonNull(saved.cursor());
                lastRebalance = saved.lastRebalance();
            }
            environment.probes(configuration.probesEnabled(), effectiveProbes(configuration));
            loaded = true;
        } catch (Exception e) {
            error = "Configuration unavailable: " + e.getMessage();
            environment.probes(false, Set.of());
            throw e;
        }
    }

    public void configure(Configuration next) throws Exception {
        WeatherLimits.configured().validatePolicy(next);
        synchronized (this) {
            if (!loaded || running || next.revision() != configuration.revision() + 1) {
                throw new IllegalStateException(
                        "Selection policy changed, unavailable or reassessment running; reload");
            }
            running = true;
        }
        try {
            if (next.automatic() || next.probesEnabled()) {
                final var eligible = environment.candidates(next);
                for (final var path : next.approved())
                    if (!eligible.contains(path))
                        throw new IllegalArgumentException(
                                "Approved candidate does not match an active dissemination Host; "
                                        + "check its target and transfer method: " + path);
            }
            environment.save(encode(new Saved(next, cursor, lastRebalance)));
            synchronized (this) {
                configuration = next;
                environment.probes(next.probesEnabled(), effectiveProbes(next));
                nextRun = 0;
            }
        } finally {
            synchronized (this) {
                running = false;
            }
        }
    }

    public synchronized void request() {
        if (!loaded || running)
            throw new IllegalStateException("Discovery unavailable or already running");
        nextRun = 0;
    }

    public synchronized Status status() {
        return new Status(configuration, lastAttempt, lastSuccess, nextRun, rows, pages, partial, error, changes,
                loaded, running, candidates, added, removed);
    }

    public void step(long now) {
        final long deadline = System.nanoTime() + 30_000_000_000L;
        final Configuration policy;
        synchronized (this) {
            if (!loaded || running || !configuration.automatic() || now < nextRun)
                return;
            running = true;
            policy = configuration;
            lastAttempt = now;
            nextRun = now + 86400_000;
        }
        try {
            final var previous = environment.selected();
            final var permitted = previous.stream().filter(p -> !policy.exclusions().contains(p))
                    .filter(p -> policy.approved().contains(p) || policy.sourceGroups().containsKey(p.sourceId()))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            if (!permitted.equals(previous))
                environment.select(permitted);
            final var eligible = new LinkedHashSet<>(environment.candidates(policy));
            eligible.removeAll(policy.exclusions());
            final var retained = permitted.stream().filter(eligible::contains)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            if (!retained.equals(permitted))
                environment.select(retained);
            final var result = policy.historical() ? database.discover(eligible, cursor, deadline)
                    : new WeatherDiscoveryStore.Result(Map.of(), 0, 0, false, cursor);
            final var ranking = now - lastRebalance < 86400_000 ? new WeatherDiscoveryStore.Result(result.hosts(),
                    result.rows(), result.pages(), true, result.nextCursor()) : result;
            final var selected = choose(policy, eligible, retained, ranking);
            final long rebalanced = result.partial() || now - lastRebalance < 86400_000 ? lastRebalance : now;
            environment.save(encode(new Saved(policy, result.nextCursor(), rebalanced)));
            environment.select(selected);
            synchronized (this) {
                candidates = eligible.size();
                added = (int) selected.stream().filter(p -> !previous.contains(p)).count();
                removed = (int) previous.stream().filter(p -> !selected.contains(p)).count();
                rows = result.rows();
                pages = result.pages();
                partial = result.partial();
                cursor = result.nextCursor();
                lastRebalance = rebalanced;
                changes = selected.stream()
                        .map(p -> p.hostId() + "/" + p.sourceId()
                                + (policy.pins().contains(p) ? ": pinned"
                                        : result.hosts().containsKey(p.hostId())
                                                ? ": historical activity/trouble candidate; source provisional"
                                                : ": configured provisional seed"))
                        .toList();
                lastSuccess = now;
                error = null;
            }
        } catch (Exception e) {
            synchronized (this) {
                error = e.getMessage();
                nextRun = now + 300_000;
            }
            org.apache.logging.log4j.LogManager.getLogger(WeatherAutomation.class)
                    .warn("Weather reassessment failed; retaining selection", e);
        } finally {
            synchronized (this) {
                running = false;
            }
        }
    }

    private static String encode(Saved saved) throws Exception {
        final var document = JSON.writeValueAsString(saved);
        checkSize(document);
        return document;
    }

    private static void checkSize(String document) {
        if (document.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > WeatherLimits.configured()
                .maxConfigurationBytes())
            throw new IllegalArgumentException("Automation policy exceeds the configured byte limit");
    }

    private static Set<WeatherProbe.Policy> effectiveProbes(Configuration policy) {
        return policy.probePolicies().stream().filter(p -> !policy.exclusions().contains(p.path()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    static Set<Path> choose(Configuration policy, Set<Path> eligible, Set<Path> previous,
            WeatherDiscoveryStore.Result result) {
        return choose(policy, eligible, previous, result, WeatherLimits.configured());
    }

    static Set<Path> choose(Configuration policy, Set<Path> eligible, Set<Path> previous,
            WeatherDiscoveryStore.Result result, WeatherLimits limits) {
        limits.validatePolicy(policy);
        final int maximum = limits.maxSelectedPaths();
        final var selected = new LinkedHashSet<Path>();
        policy.pins().stream().filter(eligible::contains).sorted(Comparator.comparing(Path::toString))
                .forEach(selected::add);
        // Retain coverage during outages and incomplete history. Fill remaining slots with diverse strata.
        previous.stream().filter(eligible::contains).filter(p -> !selected.contains(p))
                .sorted(Comparator.comparing(Path::toString)).limit(maximum - selected.size()).forEach(selected::add);
        final var rankedByScore = eligible.stream().filter(p -> !selected.contains(p))
                .sorted(Comparator.<Path> comparingLong(p -> {
                    final var signal = result.hosts().get(p.hostId());
                    return signal == null ? 0 : signal.activity() + Math.min(1000, signal.trouble()) * 2;
                }).reversed().thenComparing(Path::toString)).toList();
        final var priority = new LinkedHashSet<Path>();
        rankedByScore.stream().sorted(Comparator.<Path> comparingLong(p -> signal(result, p).activity()).reversed()
                .thenComparing(Path::toString)).limit(Math.max(1, maximum / 2)).forEach(priority::add);
        rankedByScore.stream().filter(p -> signal(result, p).trouble() > 0 && !priority.contains(p)).sorted(Comparator
                .<Path> comparingLong(p -> signal(result, p).trouble()).reversed().thenComparing(Path::toString))
                .limit(Math.max(1, maximum * 3L / 10)).forEach(priority::add);
        final var groups = new LinkedHashMap<String, ArrayDeque<Path>>();
        for (final var p : rankedByScore)
            groups.computeIfAbsent(p.sourceId() + "/" + p.protocol(), _ -> new ArrayDeque<>()).add(p);
        final long diversityLimit = priority.size() + Math.max(1, maximum / 5);
        while (priority.size() < diversityLimit && groups.values().stream().anyMatch(q -> !q.isEmpty())) {
            for (final var group : groups.values())
                if (!group.isEmpty() && priority.size() < diversityLimit)
                    priority.add(group.removeFirst());
        }
        priority.addAll(rankedByScore);
        final var ranked = List.copyOf(priority);
        if (!result.partial()) {
            int replacements = 0;
            for (final var candidate : ranked) {
                if (selected.size() < maximum || replacements == Math.max(1, maximum / 10))
                    break;
                final var victim = selected.stream().filter(p -> !policy.pins().contains(p))
                        .filter(p -> signal(result, p).trouble() == 0)
                        .min(Comparator.<Path> comparingLong(p -> score(result, p)).thenComparing(Path::toString))
                        .orElse(null);
                final var signal = result.hosts().get(candidate.hostId());
                if (victim != null && signal != null && score(result, candidate) > score(result, victim) * 1.2 + 1) {
                    selected.remove(victim);
                    selected.add(candidate);
                    replacements++;
                }
            }
        }
        for (final var candidate : ranked)
            if (selected.size() < maximum)
                selected.add(candidate);
        return Set.copyOf(selected);
    }

    private static WeatherDiscoveryStore.Signal signal(WeatherDiscoveryStore.Result result, Path path) {
        return result.hosts().getOrDefault(path.hostId(), new WeatherDiscoveryStore.Signal(0, 0, 0, 0));
    }

    private static long score(WeatherDiscoveryStore.Result result, Path path) {
        final var signal = signal(result, path);
        return signal.activity() + Math.min(1000, signal.trouble()) * 2;
    }
}
