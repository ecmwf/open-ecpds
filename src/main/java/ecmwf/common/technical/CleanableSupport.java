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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.8.6
 *
 * @since 2025-06-06
 */
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@code CleanableSupport} provides robust lifecycle management for resources, supporting both manual and automatic
 * cleanup via Java’s {@link Cleaner}.
 * <p>
 * It allows deterministic cleanup through explicit {@link #close()} calls, while also registering a fallback cleanup
 * action that will be invoked automatically when the owning object becomes unreachable (garbage collected).
 *
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Thread-safe, one-time cleanup (manual or GC-triggered)</li>
 * <li>Logs live and peak instance counts by class, updated periodically</li>
 * <li>Logs resource lifetime and cleanup origin (manual or GC)</li>
 * <li>Breaks reference cycles to ensure GC-triggered cleanup works correctly</li>
 * </ul>
 *
 * <p>
 * <b>Example usage:</b>
 *
 * <pre>{@code
 * public class MyFileResource implements AutoCloseable {
 *
 *     private final CleanableSupport cleanable;
 *
 *     public MyFileResource() {
 *         cleanable = new CleanableSupport(this, () -> {
 *             // Perform resource cleanup, e.g., closing a file
 *             if (someConditionFails()) {
 *                 throw new IOException("Failed to close file");
 *             }
 *         });
 *     }
 *
 *     @Override
 *     public void close() throws IOException {
 *         cleanable.close(); // Propagates exception if cleanup fails
 *     }
 * }
 * }</pre>
 *
 * <p>
 * In the above example, the {@code CleanableSupport} ensures the file is closed deterministically via {@link #close()},
 * or automatically when the {@code MyFileResource} instance is garbage collected.
 *
 * @author Laurent Gougeon
 *
 * @version 6.8.7
 *
 * @since 2025-06-06
 */
public class CleanableSupport {

    /** Logger for cleanup events and live object tracking. */
    private static final Logger _log = LogManager.getLogger(CleanableSupport.class);

    /** Shared {@link Cleaner} instance for registering automatic cleanup logic. */
    private static final Cleaner cleaner = Cleaner.create();

    /** Ensures the monitoring scheduler is only started once. */
    private static final AtomicBoolean schedulerStarted = new AtomicBoolean(false);

    /** Tracks the number of live instances per class. */
    private static final Map<String, AtomicInteger> liveCounts = new ConcurrentHashMap<>();

    /** Stores the previous snapshot of live counts for change detection. */
    private static final Map<String, Integer> lastSnapshot = new ConcurrentHashMap<>();

    /** Tracks the peak (maximum) number of instances per class. */
    private static final Map<String, Integer> peakCounts = new ConcurrentHashMap<>();

    /** Background scheduler for periodic reporting of live counts. */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        final var t = new Thread(r, "CleanableSupport-Monitor");
        t.setDaemon(true);
        return t;
    });

    /**
     * Registry of all CleanableSupport instances to track live objects. Uses weak keys to allow garbage collection.
     */
    private static final Map<CleanableSupport, Cleaner.Cleanable> registry = Collections
            .synchronizedMap(new WeakHashMap<>());

    /** Flag indicating whether the resource has already been cleaned. */
    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    /** User-provided cleanup logic that may throw checked exceptions. */
    private final ThrowingRunnable cleanup;

    /** Name of the class that owns this resource (for logging/stats). */
    private final String ownerClassName;

    /** Timestamp at which the resource was created (for lifetime tracking). */
    private final long creationTimeNanos = System.nanoTime();

    /**
     * Functional interface for user-supplied cleanup logic.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        /**
         * Executes cleanup logic.
         *
         * @throws Exception
         *             if cleanup fails
         */
        void run() throws Exception;
    }

    /**
     * Constructs a new {@code CleanableSupport} instance.
     *
     * @param owner
     *            the resource owner (typically {@code this})
     * @param cleanup
     *            the cleanup logic to be executed either manually or automatically
     *
     * @throws NullPointerException
     *             if either argument is {@code null}
     */
    public CleanableSupport(final Object owner, final ThrowingRunnable cleanup) {
        this.ownerClassName = Objects.requireNonNull(owner).getClass().getName();
        this.cleanup = Objects.requireNonNull(cleanup);
        final var handle = cleaner.register(owner, () -> {
            try {
                doCleanup(true);
            } catch (final Exception e) {
                _log.warn("Cleanup error (GC): ", e);
            } finally {
                registry.remove(this);
            }
        });
        // Keep the handle externally to avoid reference cycle
        registry.put(this, handle);
        incrementClassCount(ownerClassName);
        startSchedulerIfNeeded();
    }

    /**
     * Explicitly triggers cleanup and releases the resource.
     * <p>
     * Once called, the cleanup logic will not be executed again, even if garbage collection later occurs.
     *
     * @throws IOException
     *             if the cleanup logic throws an exception
     */
    public void close() throws IOException {
        try {
            doCleanup(false);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException("Cleanup failed", e);
        } finally {
            final var handle = registry.remove(this);
            if (handle != null)
                handle.clean();
        }
    }

    /**
     * Performs the cleanup operation if it has not already been executed.
     *
     * @param gcTriggered
     *            {@code true} if triggered by garbage collection
     *
     * @throws Exception
     *             if cleanup logic throws an exception
     */
    private void doCleanup(final boolean gcTriggered) throws Exception {
        if (cleaned.compareAndSet(false, true)) {
            decrementClassCount(ownerClassName);
            logResourceLifetime(System.nanoTime() - creationTimeNanos, gcTriggered);
            cleanup.run();
        }
    }

    /**
     * Marks this instance as cleaned.
     *
     * @return {@code true} if this was the first cleanup attempt, {@code false} otherwise
     */
    public boolean markCleaned() {
        return cleaned.compareAndSet(false, true);
    }

    /**
     * Checks whether this resource has already been cleaned.
     *
     * @return {@code true} if cleaned, {@code false} otherwise
     */
    public boolean isCleaned() {
        return cleaned.get();
    }

    /**
     * Logs the resource’s lifetime and whether cleanup was GC-triggered.
     *
     * @param nanos
     *            resource lifetime in nanoseconds
     * @param gcTriggered
     *            {@code true} if cleanup was triggered by garbage collection
     */
    private void logResourceLifetime(final long nanos, final boolean gcTriggered) {
        if (_log.isDebugEnabled()) {
            final var source = gcTriggered ? "GC" : "manual";
            _log.debug("Resource owned by {} cleaned after {} ms ({})", ownerClassName,
                    String.format("%.2f", nanos / 1_000_000.0), source);
        }
    }

    /**
     * Increments the live instance count for the given class and updates peak usage.
     *
     * @param className
     *            the class name
     */
    private static void incrementClassCount(final String className) {
        final var count = liveCounts.computeIfAbsent(className, _ -> new AtomicInteger()).incrementAndGet();
        peakCounts.compute(className, (_, oldPeak) -> (oldPeak == null || count > oldPeak) ? count : oldPeak);
    }

    /**
     * Decrements the live instance count for the given class.
     *
     * @param className
     *            the class name
     */
    private static void decrementClassCount(final String className) {
        liveCounts.getOrDefault(className, new AtomicInteger()).decrementAndGet();
    }

    /**
     * Starts the background reporting scheduler if not already running.
     */
    private static void startSchedulerIfNeeded() {
        if (schedulerStarted.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(CleanableSupport::logLiveCounts, 5, 5, TimeUnit.MINUTES);
        }
    }

    /**
     * Logs live object counts per class, showing current, last, and peak values.
     * <p>
     * Logs only if any class count changed since the previous snapshot.
     */
    private static void logLiveCounts() {
        final var sb = new StringBuilder();
        var changed = false;
        sb.append("CleanableSupport status at ").append(Instant.now()).append(":\n");
        for (final Map.Entry<String, AtomicInteger> entry : liveCounts.entrySet()) {
            final var className = entry.getKey();
            final var current = entry.getValue().get();
            final int last = lastSnapshot.getOrDefault(className, 0);
            final int peak = peakCounts.getOrDefault(className, 0);
            if (current != last)
                changed = true;
            sb.append(String.format("- %-50s : current = %5d | last = %5d | peak = %5d%n", className, current, last,
                    peak));
            lastSnapshot.put(className, current);
        }
        if (changed && _log.isInfoEnabled()) {
            _log.info(sb.toString());
        }
    }
}
