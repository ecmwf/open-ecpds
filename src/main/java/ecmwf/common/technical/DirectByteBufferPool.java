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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.8.2
 * @since 2025-04-16
 */

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class DirectByteBufferPool.
 */
public class DirectByteBufferPool {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DirectByteBufferPool.class);

    /** The buffer size. */
    private final int bufferSize;

    /** The max idle millis. */
    private final long maxIdleMillis;

    /** The pool. */
    private final Queue<PooledBuffer> pool = new ConcurrentLinkedQueue<>();

    /** The cleanup executor. */
    private ScheduledExecutorService cleanupExecutor;

    /** The total allocated. */
    // Metrics
    private final AtomicLong totalAllocated = new AtomicLong();

    /** The total reused. */
    private final AtomicLong totalReused = new AtomicLong();

    /** The total released. */
    private final AtomicLong totalReleased = new AtomicLong();

    /**
     * Instantiates a new direct byte buffer pool.
     *
     * @param bufferSizeInBytes
     *            the buffer size in bytes
     * @param maxIdleMillis
     *            the max idle millis
     */
    public DirectByteBufferPool(final int bufferSizeInBytes, final long maxIdleMillis) {
        this.bufferSize = bufferSizeInBytes;
        this.maxIdleMillis = maxIdleMillis;
        startCleanupTask();
    }

    public int getBufferSizeInBytes() {
        return bufferSize;
    }

    /**
     * Acquire.
     *
     * @return the byte buffer
     */
    public ByteBuffer acquire() {
        final var now = System.currentTimeMillis();
        PooledBuffer pooled;
        while ((pooled = pool.poll()) != null) {
            if ((now - pooled.lastUsed) < maxIdleMillis) {
                totalReused.incrementAndGet();
                final var buffer = pooled.buffer;
                buffer.clear();
                return buffer;
            }
            // Otherwise, too old — will be GC'ed
        }
        totalAllocated.incrementAndGet();
        return ByteBuffer.allocateDirect(bufferSize);
    }

    /**
     * Release.
     *
     * @param buffer
     *            the buffer
     */
    public void release(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() != bufferSize)
            return;
        totalReleased.incrementAndGet();
        pool.offer(new PooledBuffer(buffer, System.currentTimeMillis()));
    }

    /**
     * Start cleanup task.
     */
    private void startCleanupTask() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final var t = new Thread(r);
            t.setDaemon(true);
            t.setName("DirectByteBufferPool-Cleanup");
            return t;
        });
        cleanupExecutor.scheduleAtFixedRate(() -> {
            final var now = System.currentTimeMillis();
            final var start = System.nanoTime();
            final var before = pool.size();
            var removed = 0;
            final var it = pool.iterator();
            while (it.hasNext()) {
                final var buffer = it.next();
                if ((now - buffer.lastUsed) >= maxIdleMillis) {
                    it.remove();
                    removed++;
                }
            }
            final var durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            _log.debug(
                    "Cleanup done in {} ms (Before: {}, Removed: {}, After: {}) - Total (Allocated: {}, Reused: {}, Released: {})",
                    durationMs, before, removed, pool.size(), totalAllocated.get(), totalReused.get(),
                    totalReleased.get());
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Current pool size.
     *
     * @return the int
     */
    public int currentPoolSize() {
        return pool.size();
    }

    /**
     * Gets the total allocated.
     *
     * @return the total allocated
     */
    public long getTotalAllocated() {
        return totalAllocated.get();
    }

    /**
     * Gets the total reused.
     *
     * @return the total reused
     */
    public long getTotalReused() {
        return totalReused.get();
    }

    /**
     * Gets the total released.
     *
     * @return the total released
     */
    public long getTotalReleased() {
        return totalReleased.get();
    }

    /**
     * The Class PooledBuffer.
     */
    private static class PooledBuffer {

        /** The buffer. */
        final ByteBuffer buffer;

        /** The last used. */
        final long lastUsed;

        /**
         * Instantiates a new pooled buffer.
         *
         * @param buffer
         *            the buffer
         * @param lastUsed
         *            the last used
         */
        PooledBuffer(final ByteBuffer buffer, final long lastUsed) {
            this.buffer = buffer;
            this.lastUsed = lastUsed;
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
    }
}
