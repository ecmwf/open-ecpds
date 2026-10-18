package ecmwf.ecpds.master.weather;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static ecmwf.ecpds.master.weather.WeatherProbe.*;

/** One optional worker, no waiting jobs; remote job polling cannot relaunch a known job. */
public final class WeatherProbeRunner implements AutoCloseable {
    private final ThreadPoolExecutor worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), r -> {
                var t = new Thread(r, "weather-probe");
                t.setDaemon(true);
                return t;
            });
    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "weather-probe-deadline");
        t.setDaemon(true);
        return t;
    });
    private final ThreadPoolExecutor resolver = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), r -> {
                final var t = new Thread(r, "weather-probe-dns");
                t.setDaemon(true);
                return t;
            });
    private final ThreadLocal<Long> startedNanos = new ThreadLocal<>();
    private final LinkedHashMap<UUID, Result> results = new LinkedHashMap<>();
    private final Set<UUID> known = new LinkedHashSet<>();
    private volatile Process process;
    private volatile Socket activeSocket;
    private volatile Job activeJob;
    private volatile boolean closed;
    private volatile boolean cancelled;
    private final String source;
    private final UUID epoch;
    private long droppedResults;

    public WeatherProbeRunner(String source, UUID epoch) {
        this.source = source;
        this.epoch = epoch;
    }

    public synchronized void offer(Job job, WeatherNegotiation.Lease lease) {
        if (closed || known.contains(job.id()) || !source.equals(job.policy().path().sourceId())
                || !job.masterEpoch().equals(lease.capabilities().selection().masterEpoch())
                || !lease.capabilities().selection().enabled()
                || !lease.capabilities().selection().paths().contains(job.policy().path())
                || System.currentTimeMillis() + lease.estimatedClockOffsetMs() >= job.expiresAt()
                || System.nanoTime() - lease.receivedAtNanos() >= lease.remainingNanos())
            return;
        if (worker.getActiveCount() != 0) {
            org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                    .warn("Weather probe worker busy; job deferred");
            return;
        }
        try {
            cancelled = false;
            final long remaining = Math.min(
                    job.expiresAt() - System.currentTimeMillis() - lease.estimatedClockOffsetMs(), TimeUnit.NANOSECONDS
                            .toMillis(lease.remainingNanos() - (System.nanoTime() - lease.receivedAtNanos())));
            worker.execute(() -> {
                activeJob = job;
                final var result = execute(job, Math.min(25_000, remaining));
                synchronized (this) {
                    if (!closed && !cancelled)
                        results.put(job.id(), result);
                    while (results.size() > 10) {
                        results.remove(results.keySet().iterator().next());
                        droppedResults++;
                        org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                                .warn("Weather probe result capacity exceeded; oldest result dropped");
                    }
                    activeJob = null;
                }
            });
            known.add(job.id());
            while (known.size() > 100)
                known.remove(known.iterator().next());
        } catch (RejectedExecutionException e) {
            org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                    .warn("Weather probe worker busy; job deferred");
        }
    }

    public synchronized List<Result> results() {
        return List.copyOf(results.values());
    }

    public synchronized String status() {
        return "running=" + (activeJob != null) + "; pendingResults=" + results.size() + "; droppedResults="
                + droppedResults + "; dnsBusy=" + (resolver.getActiveCount() != 0);
    }

    public synchronized void acknowledge(List<UUID> ids) {
        ids.forEach(results::remove);
    }

    public synchronized void cancel() {
        cancelled = true;
        results.clear();
        final var child = process;
        if (child != null)
            terminate(child);
        final var socket = activeSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (java.io.IOException e) {
                org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                        .warn("Could not cancel diagnostic socket", e);
            }

        }
    }

    public synchronized void reconcile(WeatherNegotiation.Lease lease) {
        final var job = activeJob;
        if (lease == null || !lease.capabilities().selection().enabled()
                || job != null && (!job.masterEpoch().equals(lease.capabilities().selection().masterEpoch())
                        || !lease.capabilities().selection().paths().contains(job.policy().path())
                        || lease.capabilities().probeJobs().stream().noneMatch(j -> j.id().equals(job.id()))))
            cancel();
    }

    private Result execute(Job job, long budgetMs) {
        final long start = System.currentTimeMillis(), began = System.nanoTime();
        startedNanos.set(began);
        final long deadline = began + TimeUnit.MILLISECONDS.toNanos(budgetMs);
        String endpoint = "", family = "UNKNOWN";
        try {
            final var lookup = resolver.submit(() -> InetAddress.getByName(job.policy().path().configuredTarget()));
            final InetAddress address;
            try {
                address = lookup.get(Math.max(1, Math.min(2000, remaining(deadline))), TimeUnit.MILLISECONDS);
            } finally {
                if (!lookup.isDone())
                    lookup.cancel(true);
            }
            endpoint = address.getHostAddress();
            family = address instanceof Inet6Address ? "IPv6" : "IPv4";
            if (cancelled || closed || remaining(deadline) <= 0)
                return result(job, Outcome.DEADLINE, start, endpoint, family, null, List.of());
            if (job.policy().mode() == Mode.TCP_CONNECT) {
                try (final var socket = new Socket()) {
                    activeSocket = socket;
                    final long connecting = System.nanoTime();
                    socket.connect(new InetSocketAddress(address, job.policy().port()),
                            (int) Math.max(1, Math.min(3000, remaining(deadline))));
                    return result(job, Outcome.CONNECTED, start, endpoint, family,
                            (System.nanoTime() - connecting) / 1_000_000.0, List.of());
                } finally {
                    activeSocket = null;
                }
            }
            final var command = new ArrayList<>(List.of("mtr", "--json", "--report", "--report-cycles", "3", "--no-dns",
                    family.equals("IPv6") ? "-6" : "-4"));
            if (job.policy().mode() == Mode.MTR_TCP)
                command.addAll(List.of("--tcp", "--port", String.valueOf(job.policy().port())));
            command.add("--");
            command.add(endpoint);
            final Process child;
            try {
                child = new ProcessBuilder(command).redirectErrorStream(true).start();
            } catch (java.io.IOException e) {
                org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                        .warn("MTR tool could not be started", e);
                return result(job, Outcome.TOOL_UNAVAILABLE, start, endpoint, family, null, List.of());
            }
            process = child;
            if (cancelled || closed)
                terminate(child);
            final var timeout = watchdog.schedule(() -> terminate(child),
                    Math.max(1, Math.min(20_000, remaining(deadline))), TimeUnit.MILLISECONDS);
            try {
                final var bytes = child.getInputStream().readNBytes(32769);
                if (bytes.length > 32768) {
                    terminate(child);
                    return result(job, Outcome.MALFORMED, start, endpoint, family, null, List.of());
                }
                if (!child.waitFor(1, TimeUnit.SECONDS)) {
                    terminate(child);
                    return result(job, Outcome.DEADLINE, start, endpoint, family, null, List.of());
                }
                if (child.exitValue() != 0) {
                    final var text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                            .toLowerCase(Locale.ROOT);
                    return result(job,
                            text.contains("permitted") || text.contains("permission") ? Outcome.PRIVILEGE_DENIED
                                    : timeout.isDone() ? Outcome.DEADLINE : Outcome.ERROR,
                            start, endpoint, family, null, List.of());
                }
                final var hops = parseMtr(bytes);
                final var lastHop = hops.getLast();
                final boolean response = lastHop.address().equals(address.getHostAddress())
                        && lastHop.lossPercent() != null && lastHop.lossPercent() < 100;
                return result(job, response ? Outcome.RESPONSE : Outcome.NO_RESPONSE, start, endpoint, family, null,
                        hops);
            } finally {
                timeout.cancel(false);
                terminate(child);
                process = null;
            }
        } catch (ExecutionException e) {
            org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class).warn("Diagnostic DNS lookup failed",
                    e.getCause());
            return result(job, e.getCause() instanceof UnknownHostException ? Outcome.DNS_FAILURE : Outcome.ERROR,
                    start, endpoint, family, null, List.of());
        } catch (TimeoutException e) {
            return result(job, Outcome.DEADLINE, start, endpoint, family, null, List.of());
        } catch (RejectedExecutionException e) {
            org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                    .warn("Diagnostic DNS resolver is still occupied");
            return result(job, Outcome.SOURCE_UNAVAILABLE, start, endpoint, family, null, List.of());
        } catch (SocketTimeoutException e) {
            return result(job, Outcome.DEADLINE, start, endpoint, family, null, List.of());
        } catch (ConnectException e) {
            return result(job, Outcome.NO_RESPONSE, start, endpoint, family, null, List.of());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return result(job, Outcome.MALFORMED, start, endpoint, family, null, List.of());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result(job, Outcome.DEADLINE, start, endpoint, family, null, List.of());
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(WeatherProbeRunner.class)
                    .warn("Weather diagnostic failed for job {}", job.id(), e);
            return result(job, e instanceof java.io.IOException ? Outcome.ERROR : Outcome.MALFORMED, start, endpoint,
                    family, null, List.of());
        }
    }

    private static long remaining(long deadline) {
        return TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
    }

    static List<Hop> parseMtr(byte[] bytes) throws java.io.IOException {
        if (bytes.length > 32768)
            throw new IllegalArgumentException("MTR output too large");
        final var hubs = new ObjectMapper().readTree(bytes).path("report").path("hubs");
        if (!hubs.isArray() || hubs.isEmpty() || hubs.size() > 32)
            throw new IllegalArgumentException("Invalid MTR report");
        final var hops = new ArrayList<Hop>();
        for (final var hub : hubs) {
            if (!hub.path("count").isIntegralNumber() || !hub.path("count").canConvertToInt()
                    || !hub.path("host").isTextual() || !hub.path("Loss%").isNumber() || !hub.path("Avg").isNumber()
                    || !hub.path("Snt").isIntegralNumber() || !hub.path("Snt").canConvertToInt()
                    || hub.has("Rcv") && (!hub.path("Rcv").isIntegralNumber() || !hub.path("Rcv").canConvertToInt()))
                throw new IllegalArgumentException("Missing or invalid typed MTR hop");
            hops.add(new Hop(hub.get("count").intValue(), hub.get("host").textValue(), hub.get("Loss%").doubleValue(),
                    hub.get("Avg").doubleValue(), hub.get("Snt").intValue(),
                    hub.has("Rcv") ? hub.get("Rcv").intValue() : null));
        }
        return List.copyOf(hops);
    }

    private Result result(Job job, Outcome outcome, long start, String endpoint, String family, Double latency,
            List<Hop> hops) {
        return new Result(job.id(), epoch, source, outcome, start,
                start + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos.get()), endpoint, family,
                latency, hops);
    }

    private static void terminate(Process child) {
        child.descendants().forEach(p -> p.destroyForcibly());
        if (child.isAlive())
            child.destroyForcibly();
    }

    @Override
    public void close() {
        closed = true;
        cancel();
        final var child = process;
        if (child != null)
            terminate(child);
        worker.shutdownNow();
        resolver.shutdownNow();
        watchdog.shutdownNow();
        synchronized (this) {
            results.clear();
            known.clear();
        }
    }
}
