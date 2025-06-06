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
import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A helper class that encapsulates Java's Cleaner mechanism to provide safe and automatic cleanup of resources.
 *
 * This class is useful when you want finalization-like behavior without using deprecated `finalize()`.
 */
public class CleanableSupport {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CleanableSupport.class);

    // Shared Cleaner instance for all CleanableSupport instances
    private static final Cleaner cleaner = Cleaner.create();

    // Used to ensure cleanup is only performed once (thread-safe)
    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    // The registered Cleanable handle
    private final Cleaner.Cleanable cleanable;

    // Time tracking
    private final long creationTimeNanos = System.nanoTime();

    /**
     * Constructs a CleanableSupport instance.
     *
     * @param owner
     *            the object being cleaned (typically 'this'); used as a reference by the Cleaner
     * @param cleanup
     *            the cleanup logic to execute (e.g. closing streams or releasing resources)
     */
    public CleanableSupport(final Object owner, final Runnable cleanup) {
        // Register the cleanup action with the Cleaner and associate it with the owner
        this.cleanable = cleaner.register(owner, () -> {
            // Ensure cleanup is performed only once, even if triggered by both GC and
            // manual call
            if (cleaned.compareAndSet(false, true)) {
                final var duration = System.nanoTime() - creationTimeNanos;
                logResourceLifetime(duration, true);
                try {
                    cleanup.run();
                } catch (final Exception e) {
                    // Ignore or log only — GC path can't throw
                    _log.warn("Cleanup error (GC): ", e);
                }
            }
        });
    }

    /**
     * Returns true if this is the first call to clean the resource. Intended for manual cleanup with potential
     * exception handling.
     */
    public boolean markCleaned() {
        return cleaned.compareAndSet(false, true);
    }

    /**
     * Returns true if there was already a call to clean the resource.
     */
    public boolean isCleaned() {
        return cleaned.get();
    }

    /**
     * Triggers cleanup manually. Safe to call multiple times; cleanup logic will only be executed once.
     */
    public void clean() {
        if (cleaned.compareAndSet(false, true)) {
            cleanable.clean(); // Triggers the registered cleanup action
        }
    }

    /**
     * Optional: log or record how long the resource lived.
     *
     * @param nanos
     *            duration in nanoseconds
     * @param gcTriggered
     *            true if triggered by GC, false if manual close()
     */
    private void logResourceLifetime(final long nanos, final boolean gcTriggered) {
        if (_log.isDebugEnabled()) {
            final var source = gcTriggered ? "GC" : "manual";
            _log.debug("Resource cleaned after {} ms ({})", String.format("%.2f", nanos / 1_000_000.0), source);
        }
    }
}
