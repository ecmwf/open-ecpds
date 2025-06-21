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
import java.util.Map;
import java.util.Objects;
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
 * cleanup using Java's {@link Cleaner}.
 *
 * <p>
 * It tracks and logs live instances by class every 5 minutes, only logging if any change occurred. It also keeps track
 * of the peak count for each class to help diagnose potential leaks.
 *
 * <p>
 * This class is especially useful when implementing {@link AutoCloseable} and you want deterministic cleanup with
 * optional fallback via garbage collection.
 *
 * <p>
 * Features:
 * <ul>
 * <li>Thread-safe, one-time cleanup</li>
 * <li>Supports cleanup code that can throw checked exceptions</li>
 * <li>Logs resource lifetime and cleanup origin (GC or manual)</li>
 * </ul>
 *
 * <p>
 * <b>Example:</b>
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
 * In the example above, the {@code CleanableSupport} ensures that the resource is cleaned either when {@code close()}
 * is called explicitly, or as a fallback when the object is garbage collected (if not cleaned already).
 */
public class CleanableSupport {

    /** Logger for cleanup events and live object tracking. */
    private static final Logger _log = LogManager.getLogger(CleanableSupport.class);

    /** Shared cleaner instance for registering automatic cleanup logic. */
    private static final Cleaner cleaner = Cleaner.create();

    /** Ensures the monitoring scheduler is only started once. */
    private static final AtomicBoolean schedulerStarted = new AtomicBoolean(false);

    /** Tracks the number of live instances per class. */
    private static final Map<String, AtomicInteger> liveCounts = new ConcurrentHashMap<>();

    /** Stores the previous snapshot of live counts for change detection. */
    private static final Map<String, Integer> lastSnapshot = new ConcurrentHashMap<>();

    /** Tracks the peak (maximum) number of instances per class. */
    private static final Map<String, Integer> peakCounts = new ConcurrentHashMap<>();

    /** Background scheduler for periodic reporting. */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        final var t = new Thread(r, "CleanableSupport-Monitor");
        t.setDaemon(true);
        return t;
    });

    /** Flag indicating whether the resource has already been cleaned. */
    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    /** User-provided cleanup logic that may throw checked exceptions. */
    private final ThrowingRunnable cleanup;

    /** Timestamp at which the resource was created (for logging lifetime). */
    private final long creationTimeNanos = System.nanoTime();

    /** Handle to the automatic cleanup task. */
    private final Cleaner.Cleanable cleanable;

    /** Name of the class that owns this resource (used for logging/stats). */
    private final String ownerClassName;

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
     *            the resource owner (typically {@code this}), used to delay GC
     * @param cleanup
     *            cleanup logic to be run once manually or automatically
     */
    public CleanableSupport(final Object owner, final ThrowingRunnable cleanup) {
        this.ownerClassName = Objects.requireNonNull(owner).getClass().getName();
        this.cleanup = Objects.requireNonNull(cleanup);
        this.cleanable = cleaner.register(owner, () -> {
            try {
                doCleanup(true);
            } catch (final Exception e) {
                _log.warn("Cleanup error (GC): ", e);
            }
        });
        incrementClassCount(ownerClassName);
        startSchedulerIfNeeded();
    }

    /**
     * Explicitly triggers cleanup and releases the resource.
     *
     * @throws IOException
     *             if cleanup logic throws an exception
     */
    public void close() throws IOException {
        try {
            doCleanup(false);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException("Cleanup failed", e);
        } finally {
            cleanable.clean();
        }
    }

    /**
     * Marks this instance as cleaned. Returns {@code true} if this was the first cleanup attempt.
     *
     * @return {@code true} if successfully marked as cleaned, {@code false} if already cleaned
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
     * Performs cleanup if it has not already been done.
     *
     * @param gcTriggered
     *            {@code true} if triggered by garbage collection
     *
     * @throws Exception
     *             if cleanup logic fails
     */
    private void doCleanup(final boolean gcTriggered) throws Exception {
        if (cleaned.compareAndSet(false, true)) {
            decrementClassCount(ownerClassName);
            final var duration = System.nanoTime() - creationTimeNanos;
            logResourceLifetime(duration, gcTriggered);
            cleanup.run();
        }
    }

    /**
     * Logs how long the resource lived and whether cleanup was triggered by GC.
     *
     * @param nanos
     *            lifetime of the resource in nanoseconds
     * @param gcTriggered
     *            whether GC triggered the cleanup
     */
    private void logResourceLifetime(final long nanos, final boolean gcTriggered) {
        if (_log.isDebugEnabled()) {
            final var source = gcTriggered ? "GC" : "manual";
            final var millis = nanos / 1_000_000.0;
            _log.debug("Resource owned by {} cleaned after {} ms ({})", ownerClassName, String.format("%.2f", millis),
                    source);
        }
    }

    /**
     * Increments the live count for the given class.
     *
     * @param className
     *            the name of the class
     */
    private static void incrementClassCount(final String className) {
        liveCounts.computeIfAbsent(className, _ -> new AtomicInteger()).incrementAndGet();
        peakCounts.merge(className, 1, Math::max);
    }

    /**
     * Decrements the live count for the given class.
     *
     * @param className
     *            the name of the class
     */
    private static void decrementClassCount(final String className) {
        liveCounts.getOrDefault(className, new AtomicInteger()).decrementAndGet();
    }

    /**
     * Starts the background logging scheduler if not already started.
     */
    private static void startSchedulerIfNeeded() {
        if (schedulerStarted.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(CleanableSupport::logLiveCounts, 5, 5, TimeUnit.MINUTES);
        }
    }

    /**
     * Logs live object counts per class, only if changes occurred since last snapshot.
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
            if (current != last) {
                changed = true;
            }
            sb.append(String.format("- %-50s : current = %5d | last = %5d | peak = %5d%n", className, current, last,
                    peak));
            lastSnapshot.put(className, current);
        }
        if (changed && _log.isInfoEnabled()) {
            _log.info(sb.toString());
        }
    }
}
