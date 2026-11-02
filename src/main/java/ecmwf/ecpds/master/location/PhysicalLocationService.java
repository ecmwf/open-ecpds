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

package ecmwf.ecpds.master.location;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.SystemPhysicalLocationStore;
import ecmwf.common.database.SystemPhysicalLocationStore.Attempt;
import ecmwf.common.database.SystemPhysicalLocationStore.Snapshot;
import ecmwf.common.database.SystemPhysicalLocationStore.Status;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Machine;

/** Master-owned background collection; readers perform no network or database work. */
public final class PhysicalLocationService implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(PhysicalLocationService.class);

    public record Result(Status status, String location) {
    }

    public interface Provider extends AutoCloseable {
        Result query(Machine machine, long timeout, int retries) throws Exception;

        @Override
        void close();
    }

    interface Store {
        Snapshot find(String machine, String target) throws Exception;

        void save(Attempt attempt) throws Exception;
    }

    private static final class State {
        volatile Snapshot snapshot;
        volatile long nextDue;
        volatile boolean inFlight;
        volatile long persistenceFailureAt;
        boolean loaded;
        int failures;
    }

    private final PhysicalLocationConfig config;
    private final Provider provider;
    private final Store store;
    private final LongSupplier clock;
    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor workers;
    private final LongAdder polls = new LongAdder();
    private final LongAdder successes = new LongAdder();
    private final LongAdder failures = new LongAdder();
    private final LongAdder timeouts = new LongAdder();
    private final LongAdder persistenceFailures = new LongAdder();
    private final LongAdder duration = new LongAdder();
    private volatile boolean closed;

    public static PhysicalLocationService configured() {
        PhysicalLocationConfig config;
        try {
            config = PhysicalLocationConfig.load();
        } catch (final Exception e) {
            // Configuration exceptions may contain credentials, paths or untrusted values.
            LOG.error("Physical location collection disabled: invalid configuration or unreadable credential file");
            config = PhysicalLocationConfig.disabled();
        }
        final var database = new SystemPhysicalLocationStore();
        return new PhysicalLocationService(config, new SnmpLocationProvider(), new Store() {
            @Override
            public Snapshot find(String machine, String target) throws Exception {
                return database.find(machine, target);
            }

            @Override
            public void save(Attempt attempt) throws Exception {
                database.save(attempt);
            }
        }, System::currentTimeMillis);
    }

    PhysicalLocationService(PhysicalLocationConfig config, Provider provider, Store store, LongSupplier clock) {
        this.config = config;
        this.provider = provider;
        this.store = store;
        this.clock = clock;
        config.machines().keySet().forEach(id -> states.put(id, new State()));
        workers = new ThreadPoolExecutor(config.concurrency(), config.concurrency(), 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), runnable -> {
                    final var thread = new Thread(runnable, "PhysicalLocationPoll");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    public boolean enabled() {
        return config.enabled();
    }

    /** Called only by the Master scheduler, not by requests or cache misses. */
    public synchronized void tick() {
        if (closed || !config.enabled()) {
            return;
        }
        final long now = clock.getAsLong();
        for (final var machine : config.machines().values()) {
            final var state = states.get(machine.id());
            if (state.inFlight || state.nextDue > now) {
                continue;
            }
            state.inFlight = true;
            try {
                workers.execute(() -> collect(machine, state));
            } catch (final RejectedExecutionException e) {
                state.inFlight = false;
                break;
            }
        }
    }

    private void collect(Machine machine, State state) {
        final long started = System.nanoTime();
        final long attemptedAt = clock.getAsLong();
        boolean successful = false;
        try {
            if (!state.loaded) {
                state.snapshot = store.find(machine.id(), machine.targetKey());
                state.persistenceFailureAt = 0;
                state.loaded = true;
            }
            if (closed) {
                return;
            }
            polls.increment();
            Result result;
            try {
                result = provider.query(machine, config.timeout(), config.retries());
            } catch (final Exception e) {
                result = new Result(Status.ERROR, null);
            }
            if (closed) {
                return;
            }
            successful = result.status() == Status.SUCCESS || result.status() == Status.NOT_CONFIGURED;
            if (successful) {
                successes.increment();
            } else {
                failures.increment();
                if (result.status() == Status.TIMEOUT) {
                    timeouts.increment();
                }
            }
            store.save(new Attempt(machine.id(), machine.targetKey(), result.location(), result.status(), attemptedAt));
            state.snapshot = store.find(machine.id(), machine.targetKey());
            state.persistenceFailureAt = 0;
            if (!successful) {
                LOG.warn("Physical location poll for machine {}: {}; retaining any previous value for this target",
                        machine.id(), result.status());
            } else {
                LOG.debug("Physical location poll for machine {}: {}", machine.id(), result.status());
            }
        } catch (final Exception e) {
            successful = false;
            state.persistenceFailureAt = attemptedAt;
            persistenceFailures.increment();
            LOG.warn(
                    "Physical location persistence failed for machine {}; check schema migration/database availability",
                    machine.id());
        } finally {
            duration.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            state.failures = successful ? 0 : Math.min(4, state.failures + 1);
            final long delay = Math.min(86_400_000L, config.interval() * (1L << state.failures));
            state.nextDue = clock.getAsLong() + Math.max(config.interval(), delay);
            state.inFlight = false;
        }
    }

    public Map<String, Object> metadata(String component) {
        if (!config.enabled() || component == null) {
            return Map.of();
        }
        final var machine = config.systems().get(component);
        if (machine == null) {
            return Map.of();
        }
        final var state = states.get(machine);
        final var snapshot = state.snapshot;
        final Map<String, Object> result = new HashMap<>();
        result.put("machineId", machine);
        result.put("source", "SNMP sysLocation");
        result.put("status", state.persistenceFailureAt > 0 ? "PERSISTENCE_ERROR"
                : snapshot == null ? "PENDING" : snapshot.status().name());
        result.put("statusLabel", switch ((String) result.get("status")) {
        case "SUCCESS" -> "Verified";
        case "NOT_CONFIGURED" -> "Not configured";
        case "UNSUPPORTED" -> "Object unavailable";
        case "TIMEOUT" -> "Timed out";
        case "ERROR" -> "Collection failed";
        case "PERSISTENCE_ERROR" -> "Database unavailable";
        default -> "Awaiting collection";
        });
        final long now = clock.getAsLong();
        final boolean stale = state.persistenceFailureAt > 0 || snapshot == null || snapshot.lastSuccess() == 0
                || now - snapshot.lastSuccess() > config.staleAfter() || now < snapshot.lastSuccess()
                || snapshot.status() != Status.SUCCESS && snapshot.status() != Status.NOT_CONFIGURED;
        result.put("stale", stale);
        if (snapshot != null) {
            if (snapshot.location() != null) {
                result.put("value", snapshot.location());
            }
            if (snapshot.lastSuccess() > 0) {
                result.put("lastSuccess", Instant.ofEpochMilli(snapshot.lastSuccess()).toString());
            }
            result.put("lastAttempt",
                    Instant.ofEpochMilli(Math.max(snapshot.lastAttempt(), state.persistenceFailureAt)).toString());
        } else if (state.persistenceFailureAt > 0) {
            result.put("lastAttempt", Instant.ofEpochMilli(state.persistenceFailureAt).toString());
        }
        return result;
    }

    public Map<String, Long> measurements() {
        final long stale = config.machines().keySet().stream().filter(machine -> {
            final var snapshot = states.get(machine).snapshot;
            return states.get(machine).persistenceFailureAt > 0 || snapshot == null || snapshot.lastSuccess() == 0
                    || clock.getAsLong() - snapshot.lastSuccess() > config.staleAfter()
                    || clock.getAsLong() < snapshot.lastSuccess()
                    || snapshot.status() != Status.SUCCESS && snapshot.status() != Status.NOT_CONFIGURED;
        }).count();
        return Map.of("TargetCount", (long) states.size(), "ActivePollCount", (long) activePolls(), "PollCount",
                polls.sum(), "SuccessCount", successes.sum(), "FailureCount", failures.sum(), "TimeoutCount",
                timeouts.sum(), "PersistenceFailureCount", persistenceFailures.sum(), "PollDurationMillis",
                duration.sum(), "StaleCount", stale);
    }

    public String statistics() {
        return measurements().toString();
    }

    public int activePolls() {
        return (int) states.values().stream().filter(state -> state.inFlight).count();
    }

    @Override
    public synchronized void close() {
        closed = true;
        provider.close();
        workers.shutdownNow();
    }
}
