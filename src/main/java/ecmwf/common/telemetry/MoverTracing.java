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
package ecmwf.common.telemetry;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.version.Version;

/** SDK-free capture and bounded, retrying transport of sampled mover executions. */
public final class MoverTracing {
    private static final Logger LOG = LogManager.getLogger(MoverTracing.class);
    private static final AtomicLong INVALID_CARRIERS = new AtomicLong();
    private static final AtomicLong LAST_CARRIER_WARNING = new AtomicLong();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile Delivery delivery;
    private static volatile String instanceId;

    private MoverTracing() {
    }

    @FunctionalInterface
    public interface Sender {
        void send(List<TransferSpan> spans) throws Exception;
    }

    public static synchronized void start(final Sender sender, final String instance) {
        if (delivery == null) {
            try {
                instanceId = TransferSpan.text(instance, 256);
            } catch (final IllegalArgumentException invalid) {
                instanceId = null;
                LOG.warn("Invalid Mover trace identity; local capture disabled, relay delivery remains available");
            }
            delivery = new Delivery(sender, "mover");
        }
    }

    /** Does not wait for a sender which may be blocked in a remote invocation. */
    public static synchronized void shutdown() {
        final var current = delivery;
        delivery = null;
        if (current != null) {
            current.close();
        }
    }

    /** Relay admission is all-or-nothing; failure must be propagated to the caller. */
    public static void accept(final List<TransferSpan> spans) {
        final var current = delivery;
        if (current == null) {
            throw new IllegalStateException("Mover trace delivery is not running");
        }
        current.accept(spans);
    }

    public static void copy(final DataTransfer source, final DataTransfer target) {
        target.setTraceParent(source.getTraceParent());
        target.setTraceState(source.getTraceState());
        target.setTraceAttemptId(source.getTraceAttemptId());
    }

    public static Attempt execution(final DataTransfer transfer) {
        final var current = delivery;
        final var parent = transfer.getTraceParent();
        try {
            if (parent == null || current == null || instanceId == null) {
                return new Attempt(null, current);
            }
            if (parent.length() != 55 || !parent.matches("[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")
                    || parent.startsWith("ff-")) {
                throw new IllegalArgumentException("Invalid traceparent");
            }
            TransferSpan.id(parent.substring(3, 35), 32);
            TransferSpan.id(parent.substring(36, 52), 16);
            TransferSpan.text(transfer.getTraceAttemptId(), 64);
            TransferSpan.validateTraceState(transfer.getTraceState());
            final var flags = Integer.parseInt(parent.substring(53), 16);
            if ((flags & 1) == 0) {
                return new Attempt(null, current);
            }
            final var span = new TransferSpan();
            span.setTraceId(parent.substring(3, 35));
            span.setParentSpanId(parent.substring(36, 52));
            final var bytes = new byte[8];
            String id;
            do {
                RANDOM.nextBytes(bytes);
                id = HexFormat.of().formatHex(bytes);
            } while ("0000000000000000".equals(id));
            span.setSpanId(id);
            span.setTraceFlags(flags);
            span.setTraceState(transfer.getTraceState());
            final var now = Instant.now();
            span.setStartEpochNanos(
                    Math.addExact(Math.multiplyExact(now.getEpochSecond(), 1_000_000_000L), now.getNano()));
            span.setEndEpochNanos(span.getStartEpochNanos());
            final var attributes = new LinkedHashMap<String, Object>();
            attributes.put("ecpds.transfer.id", transfer.getId());
            attributes.put("ecpds.file.id", transfer.getDataFileId());
            attributes.put("ecpds.transfer.attempt.id", transfer.getTraceAttemptId());
            attributes.put("ecpds.transfer.start_count", transfer.getStartCount());
            attributes.put("ecpds.transfer.requeue_count", transfer.getRequeueHistory());
            if (transfer.getDestinationName() != null) {
                attributes.put("ecpds.destination.name", transfer.getDestinationName());
            }
            if (transfer.getHostName() != null) {
                attributes.put("ecpds.host.id", transfer.getHostName());
            }
            span.setAttributes(attributes);
            span.setResource(Map.of("service.name", "open-ecpds-mover", "service.instance.id",
                    instanceId == null ? "unknown" : instanceId, "service.version", Version.getFullVersion()));
            transfer.setTraceParent(parent.substring(0, 36) + id + parent.substring(52));
            return new Attempt(span, current);
        } catch (final IllegalArgumentException invalid) {
            INVALID_CARRIERS.incrementAndGet();
            final var now = System.nanoTime();
            final var previous = LAST_CARRIER_WARNING.get();
            if ((previous == 0 || now - previous >= TimeUnit.MINUTES.toNanos(1))
                    && LAST_CARRIER_WARNING.compareAndSet(previous, now)) {
                LOG.warn("Ignoring invalid transfer trace context; {} invalid carriers since last report",
                        INVALID_CARRIERS.getAndSet(0));
            }
            return new Attempt(null, current);
        }
    }

    public static final class Attempt implements AutoCloseable {
        private final TransferSpan span;
        private final Delivery target;
        private final long start = System.nanoTime();
        private final AtomicBoolean ended = new AtomicBoolean();
        private volatile boolean failed;

        private Attempt(final TransferSpan span, final Delivery target) {
            this.span = span;
            this.target = target;
        }

        public void failed() {
            failed = true;
        }

        public void finish(final DataTransfer transfer, final boolean successful) {
            end(transfer, !successful);
        }

        @Override
        public void close() {
            end(null, false);
        }

        private void end(final DataTransfer transfer, final boolean unsuccessful) {
            if (!ended.compareAndSet(false, true) || span == null) {
                return;
            }
            span.setFailed(failed || unsuccessful);
            span.setEndEpochNanos(span.getStartEpochNanos() + Math.max(0, System.nanoTime() - start));
            if (transfer != null) {
                final var attributes = new LinkedHashMap<>(span.getAttributes());
                attributes.put("ecpds.transfer.bytes_sent", transfer.getSent());
                span.setAttributes(attributes);
            }
            if (target != null) {
                target.local(span);
            }
        }
    }

    /**
     * One daemon and at most one in-flight batch. Capacity includes in-flight records. An unsuccessful send retains the
     * identical detached batch.
     */
    static final class Delivery implements AutoCloseable {
        static final int CAPACITY = 2048;
        private final Sender sender;
        private final String role;
        private final ScheduledExecutorService worker;
        private final ArrayDeque<TransferSpan> queue = new ArrayDeque<>();
        private List<TransferSpan> inFlight;
        private int size;
        private boolean closed;
        private boolean workerStarted;
        private long dropped;
        private long failures;
        private long lastWarning;

        Delivery(final Sender sender, final String role) {
            this.sender = java.util.Objects.requireNonNull(sender);
            this.role = role;
            worker = Executors.newSingleThreadScheduledExecutor(runnable -> {
                final var thread = new Thread(runnable, "transfer-spans-" + role);
                thread.setDaemon(true);
                return thread;
            });
        }

        void accept(final List<TransferSpan> spans) {
            final var snapshot = TransferSpan.snapshots(spans);
            synchronized (this) {
                if (closed || size + snapshot.size() > CAPACITY) {
                    throw new IllegalStateException("Transfer span delivery unavailable or full; retry batch");
                }
                queue.addAll(snapshot);
                size += snapshot.size();
                if (!workerStarted) {
                    worker.scheduleWithFixedDelay(this::send, 1, 1, TimeUnit.SECONDS);
                    workerStarted = true;
                }
            }
        }

        void local(final TransferSpan span) {
            try {
                accept(List.of(span));
            } catch (final IllegalArgumentException | IllegalStateException exception) {
                synchronized (this) {
                    dropped++;
                    warn();
                }
            }
        }

        private void send() {
            final List<TransferSpan> batch;
            synchronized (this) {
                if (closed) {
                    return;
                }
                if (inFlight == null && !queue.isEmpty()) {
                    final var records = new ArrayList<TransferSpan>(TransferSpan.MAX_BATCH);
                    while (!queue.isEmpty() && records.size() < TransferSpan.MAX_BATCH) {
                        records.add(queue.removeFirst());
                    }
                    inFlight = List.copyOf(records);
                }
                batch = inFlight;
            }
            if (batch == null) {
                return;
            }
            try {
                sender.send(batch);
                synchronized (this) {
                    if (!closed) {
                        size -= batch.size();
                        inFlight = null;
                    }
                }
            } catch (final Exception exception) {
                synchronized (this) {
                    if (!closed) {
                        failures++;
                        warn();
                    }
                }
            }
        }

        private void warn() {
            final var now = System.nanoTime();
            if (lastWarning == 0 || now - lastWarning >= TimeUnit.MINUTES.toNanos(1)) {
                lastWarning = now;
                LOG.warn(
                        "Transfer span delivery ({}): dropped={}, failed export attempts={}, pending={}; retrying retained batch",
                        role, dropped, failures, size);
            }
        }

        @Override
        public synchronized void close() {
            if (!closed) {
                closed = true;
                worker.shutdownNow();
                LOG.log(size == 0 && dropped == 0 && failures == 0 ? org.apache.logging.log4j.Level.INFO
                        : org.apache.logging.log4j.Level.WARN,
                        "Transfer span shutdown ({}): discarded={} (includes in-flight), dropped={}, failed export attempts={}",
                        role, size, dropped, failures);
                queue.clear();
                inFlight = null;
                size = 0;
            }
        }
    }
}
