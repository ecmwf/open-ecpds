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
package ecmwf.ecpds.master;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.StorageVolumeInfo;
import ecmwf.ecpds.mover.MoverInterface;

/** Bounded, single-flight Mover polling; dashboard reads never contact a Mover. */
final class MoverStorageMonitor implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(MoverStorageMonitor.class);
    private final Map<String, long[][]> usage = new ConcurrentHashMap<>();
    private final Map<String, Long> updated = new ConcurrentHashMap<>();
    private final Map<String, List<StorageVolumeInfo>> details = new ConcurrentHashMap<>();
    private final Map<String, Long> detailsAttempted = new ConcurrentHashMap<>();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private final Set<String> failed = ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor workers;

    MoverStorageMonitor() {
        this(16);
    }

    MoverStorageMonitor(final int concurrency) {
        workers = new ThreadPoolExecutor(0, concurrency, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), task -> {
            final var thread = new Thread(task, "MoverStoragePoll");
            thread.setDaemon(true);
            return thread;
        });
    }

    void refresh(final String name, final int count, final Supplier<MoverInterface> source) {
        if (count <= 0 || !pending.add(name)) {
            return;
        }
        try {
            workers.execute(() -> {
                try {
                    final var mover = source.get();
                    if (mover == null) {
                        return;
                    }
                    try {
                        final var result = mover.computeVolumeUsage(count - 1);
                        if (result == null || result.length != 2 || result[0] == null || result[1] == null
                                || result[0].length != count || result[1].length != count) {
                            throw new IllegalStateException("Invalid volume usage response");
                        }
                        usage.put(name, new long[][] { result[0].clone(), result[1].clone() });
                        updated.put(name, System.currentTimeMillis());
                        if (failed.remove(name)) {
                            LOG.info("Storage usage polling recovered for Data Mover {}", name);
                        }
                    } catch (final Exception e) {
                        if (failed.add(name)) {
                            LOG.warn("Cannot refresh storage usage for Data Mover {}; keeping last snapshot", name, e);
                        }
                    }
                    final var now = System.currentTimeMillis();
                    if (now - detailsAttempted.getOrDefault(name, 0L) >= TimeUnit.MINUTES.toMillis(5)) {
                        detailsAttempted.put(name, now);
                        try {
                            details.put(name, List.copyOf(mover.getStorageVolumeInfo(count - 1)));
                        } catch (final Exception e) {
                            LOG.warn(
                                    "Cannot refresh storage device information for Data Mover {}; keeping last snapshot",
                                    name, e);
                        }
                    }
                } catch (final Exception e) {
                    if (failed.add(name)) {
                        LOG.warn("Cannot contact Data Mover {} for storage polling; keeping last snapshot", name, e);
                    }
                } finally {
                    pending.remove(name);
                }
            });
        } catch (final RejectedExecutionException e) {
            // No queued work: busy or stuck Movers cannot create an unbounded polling backlog.
            pending.remove(name);
        }
    }

    Map<String, long[][]> usage(final String name) {
        return name == null ? Map.copyOf(usage) : usage.containsKey(name) ? Map.of(name, usage.get(name)) : Map.of();
    }

    Map<String, Long> updated(final String name) {
        return name == null ? Map.copyOf(updated)
                : updated.containsKey(name) ? Map.of(name, updated.get(name)) : Map.of();
    }

    Map<String, List<StorageVolumeInfo>> details(final String name) {
        return name == null ? Map.copyOf(details)
                : details.containsKey(name) ? Map.of(name, details.get(name)) : Map.of();
    }

    @Override
    public void close() {
        workers.shutdownNow();
    }
}
