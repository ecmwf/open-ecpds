package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.*;
import ecmwf.ecpds.master.weather.WeatherObservation.Path;

/** Bounded Master schedule. Polling never executes a probe or queries a database. */
public final class WeatherProbes {
    public record Completed(WeatherProbe.Job job, WeatherProbe.Result result, long receivedAt) implements Serializable {
    }

    public record Unavailable(WeatherProbe.Job job, long receivedAt) implements Serializable {
    }

    public record Status(boolean enabled, List<WeatherProbe.Policy> policies, int pending, List<Completed> completed,
            long expired, List<Unavailable> unavailable, long rejected) implements Serializable {
    }

    private Set<WeatherProbe.Policy> policies = Set.of();
    private final Map<WeatherProbe.Policy, Long> due = new HashMap<>();
    private final Map<UUID, WeatherProbe.Job> pending = new LinkedHashMap<>();
    private final ArrayDeque<Completed> completed = new ArrayDeque<>();
    private final ArrayDeque<Unavailable> unavailable = new ArrayDeque<>();
    private boolean enabled;
    private long expired, nextStart;
    private long rejected;

    public synchronized void configure(boolean enabled, Set<WeatherProbe.Policy> policies) {
        final var limits = WeatherLimits.configured();
        if (policies.size() > limits.maxProbePolicies()
                || policies.stream().map(p -> p.path().sourceId()).distinct().count() > limits.maxSources())
            throw new IllegalArgumentException("Probe policy exceeds deployment caps");
        this.enabled = enabled;
        this.policies = Set.copyOf(policies);
        due.keySet().retainAll(policies);
        pending.values().removeIf(j -> !enabled || !policies.contains(j.policy()));
    }

    public synchronized void step(boolean collectionEnabled, UUID masterEpoch, Set<Path> selection, long now) {
        pending.values().removeIf(j -> {
            if (now >= j.expiresAt() || !collectionEnabled || !selection.contains(j.policy().path())) {
                expired++;
                if (now >= j.expiresAt() && collectionEnabled && selection.contains(j.policy().path())) {
                    unavailable.addLast(new Unavailable(j, now));
                    while (unavailable.size() > 100)
                        unavailable.removeFirst();
                    due.put(j.policy(), now + Math.min(3600_000, j.policy().intervalSeconds() * 4000L));
                }
                return true;
            }
            return false;
        });
        completed.removeIf(c -> now - c.receivedAt() > 3600_000);
        unavailable.removeIf(c -> now - c.receivedAt() > 3600_000);
        if (!collectionEnabled || !enabled || now < nextStart
                || pending.size() >= WeatherLimits.configured().maxSources())
            return;
        for (final var policy : policies.stream().sorted(Comparator.comparing(p -> p.path().toString() + p.mode()))
                .toList()) {
            if (!selection.contains(policy.path()) || now < due.getOrDefault(policy, 0L) || pending.values().stream()
                    .anyMatch(j -> j.policy().path().sourceId().equals(policy.path().sourceId())))
                continue;
            final var job = new WeatherProbe.Job(UUID.randomUUID(), masterEpoch, policy, now, now + 60_000);
            pending.put(job.id(), job);
            due.put(policy, now + policy.intervalSeconds() * 1000L + Math.floorMod(job.id().hashCode(), 10000));
            nextStart = now + 5000; // Global ceiling: twelve starts per minute, one outstanding per source.
            break;
        }
    }

    public synchronized List<WeatherProbe.Job> jobs(String source) {
        return enabled
                ? pending.values().stream().filter(j -> j.policy().path().sourceId().equals(source)).limit(1).toList()
                : List.of();
    }

    public synchronized boolean accept(WeatherProbe.Result result, long offset, long now, Set<Path> selection) {
        final var job = pending.get(result.jobId());
        if (job == null && enabled && completed.stream().anyMatch(c -> policies.contains(c.job().policy())
                && selection.contains(c.job().policy().path()) && c.result().equals(result)))
            return true;
        if (!enabled || job == null || !selection.contains(job.policy().path())
                || !job.policy().path().sourceId().equals(result.sourceId()) || now > job.expiresAt() + 60_000
                || offset < -30_000 || offset > 30_000 || result.startedAt() + offset < job.issuedAt() - 5000
                || result.finishedAt() + offset > job.expiresAt() + 5000) {
            rejected++;
            return false;
        }
        pending.remove(job.id());
        completed.addLast(new Completed(job, result, now));
        while (completed.size() > 100)
            completed.removeFirst();
        if (result.outcome() != WeatherProbe.Outcome.CONNECTED && result.outcome() != WeatherProbe.Outcome.RESPONSE)
            due.put(job.policy(), now + Math.min(3600_000, job.policy().intervalSeconds() * 4000L));
        return true;
    }

    public synchronized Status status() {
        return new Status(enabled, List.copyOf(policies), pending.size(), List.copyOf(completed), expired,
                List.copyOf(unavailable), rejected);
    }
}
