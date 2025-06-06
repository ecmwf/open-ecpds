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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@code CleanableSupport} provides a robust mechanism for managing resource
 * cleanup, using Java's {@link Cleaner} for automatic cleanup and supporting
 * cleanup logic that may throw checked exceptions.
 *
 * <p>
 * This class is especially useful when implementing {@link AutoCloseable} and
 * you want deterministic cleanup with optional fallback via garbage collection.
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
 * 	private final CleanableSupport cleanable;
 *
 * 	public MyFileResource() {
 * 		cleanable = new CleanableSupport(this, () -> {
 * 			// Perform resource cleanup, e.g., closing a file
 * 			if (someConditionFails()) {
 * 				throw new IOException("Failed to close file");
 * 			}
 * 		});
 * 	}
 *
 * 	@Override
 * 	public void close() throws IOException {
 * 		cleanable.close(); // Propagates exception if cleanup fails
 * 	}
 * }
 * }</pre>
 *
 * <p>
 * In the example above, the {@code CleanableSupport} ensures that the resource
 * is cleaned either when {@code close()} is called explicitly, or as a fallback
 * when the object is garbage collected (if not cleaned already).
 */

public class CleanableSupport {

	/** Logger instance */
	private static final Logger _log = LogManager.getLogger(CleanableSupport.class);

	/** Shared Cleaner instance used to register cleanup actions */
	private static final Cleaner cleaner = Cleaner.create();

	/** Indicates whether the resource has already been cleaned */
	private final AtomicBoolean cleaned = new AtomicBoolean(false);

	/** Cleanup logic provided by the caller */
	private final ThrowingRunnable cleanup;

	/** Time the resource was created (for tracking lifetime) */
	private final long creationTimeNanos = System.nanoTime();

	/** Handle to the registered cleanup task */
	private final Cleaner.Cleanable cleanable;

	/**
	 * Functional interface for cleanup code that may throw exceptions.
	 */
	@FunctionalInterface
	public interface ThrowingRunnable {
		/**
		 * Executes cleanup logic.
		 *
		 * @throws Exception if cleanup fails
		 */
		void run() throws Exception;
	}

	/**
	 * Constructs a new {@code CleanableSupport} instance.
	 *
	 * @param owner   the object being tracked (typically {@code this}); referenced
	 *                to delay GC
	 * @param cleanup the cleanup logic, which may throw checked exceptions
	 */
	public CleanableSupport(final Object owner, final ThrowingRunnable cleanup) {
		this.cleanup = cleanup;
		// Register cleanup to be called when 'owner' is garbage collected
		this.cleanable = cleaner.register(owner, () -> {
			try {
				// Perform cleanup if not already done
				doCleanup(true);
			} catch (final Exception e) {
				// Log exception; can't propagate during GC finalization
				_log.warn("Cleanup error (GC): ", e);
			}
		});
	}

	/**
	 * Closes the resource and propagates any exceptions that occur during cleanup.
	 *
	 * @throws IOException if cleanup logic fails
	 */
	public void close() throws IOException {
		try {
			doCleanup(false); // Attempt cleanup and propagate exception
		} catch (final IOException e) {
			throw e;
		} catch (final Exception e) {
			throw new IOException("Cleanup failed", e);
		} finally {
			cleanable.clean();
		}
	}

	/**
	 * Marks the resource as cleaned.
	 *
	 * @return {@code true} if this was the first call to clean, {@code false}
	 *         otherwise
	 */
	public boolean markCleaned() {
		return cleaned.compareAndSet(false, true);
	}

	/**
	 * Indicates whether the resource has already been cleaned.
	 *
	 * @return {@code true} if cleanup has occurred, {@code false} otherwise
	 */
	public boolean isCleaned() {
		return cleaned.get();
	}

	/**
	 * Internal cleanup logic with one-time guarantee.
	 *
	 * @param gcTriggered {@code true} if cleanup was triggered by GC
	 * @throws Exception if the cleanup logic fails
	 */
	private void doCleanup(final boolean gcTriggered) throws Exception {
		if (cleaned.compareAndSet(false, true)) {
			final var duration = System.nanoTime() - creationTimeNanos;
			logResourceLifetime(duration, gcTriggered);
			cleanup.run(); // May throw checked exception
		}
	}

	/**
	 * Logs the time the resource was alive and how it was cleaned.
	 *
	 * @param nanos       resource lifetime in nanoseconds
	 * @param gcTriggered {@code true} if cleanup was GC-triggered; {@code false}
	 *                    otherwise
	 */
	private void logResourceLifetime(final long nanos, final boolean gcTriggered) {
		if (_log.isDebugEnabled()) {
			final var source = gcTriggered ? "GC" : "manual";
			final var millis = nanos / 1_000_000.0;
			_log.debug("Resource cleaned after {} ms ({})", String.format("%.2f", millis), source);
		}
	}
}
