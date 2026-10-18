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

package ecmwf.ecpds.master.weather;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.WeatherSocketStore;

/** Single background writer. Admission is memory-only, bounded and nonblocking. */
public final class WeatherPersistence implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(WeatherPersistence.class);
    private final WeatherOutbox queue;
    private final Store store;
    private final java.util.function.LongSupplier nanoTime;
    private final HashMap<UUID, Integer> attempts = new HashMap<>();
    private final UUID receiptEpoch = UUID.randomUUID();
    private volatile boolean ready;
    private volatile boolean closed;
    private volatile String error = "Schema capability has not been checked";
    private volatile long written;
    private volatile long retentionGaps;
    private volatile long failures;
    private long nextProbe;
    private long nextWrite;
    private boolean writeStarted;

    public interface Store {
        void checkSchema() throws Exception;

        WeatherSocketStore.Result persist(WeatherObservation observation) throws Exception;
    }

    public WeatherPersistence(Store store) {
        this(store, System::nanoTime);
    }

    WeatherPersistence(Store store, java.util.function.LongSupplier nanoTime) {
        this.store = java.util.Objects.requireNonNull(store);
        this.nanoTime = java.util.Objects.requireNonNull(nanoTime);
        queue = new WeatherOutbox(nanoTime);
    }

    public WeatherPersistence() {
        this(new Store() {
            private final WeatherSocketStore database = new WeatherSocketStore();

            public void checkSchema() throws Exception {
                database.checkSchema();
            }

            public WeatherSocketStore.Result persist(WeatherObservation observation) throws Exception {
                return database.persist(observation);
            }
        });
    }

    public boolean offer(WeatherObservation observation) {
        return !closed && ready && queue.offer(observation);
    }

    public boolean ready() {
        return ready && !closed;
    }

    /** Call only from the owning scheduler. At most ten events/100 connections per second; two retries per event. */
    public void step() {
        if (closed) {
            return;
        }
        final long now = nanoTime.getAsLong();
        if (nextProbe == 0 || now - nextProbe >= 0) {
            nextProbe = now + 30_000_000_000L;
            try {
                store.checkSchema();
                ready = !closed;
                error = null;
            } catch (Exception e) {
                ready = false;
                error = "Socket schema/writer unavailable; inspect the Master log";
                LOG.warn("Network Weather schema capability check failed; collection cannot start", e);
            }
        }
        if (!ready() || writeStarted && now - nextWrite < 0) {
            return;
        }
        writeStarted = true;
        nextWrite = now + 1_000_000_000L;
        final var pending = queue.batch();
        attempts.keySet().retainAll(queue.pendingEventIds());
        int events = 0, sockets = 0;
        for (final var delivery : pending) {
            final var observation = delivery.observation();
            if (closed || ++events > 10 || sockets + observation.connections().size() > 100
                    || nanoTime.getAsLong() - now > 200_000_000L) {
                break;
            }
            sockets += observation.connections().size();
            try {
                final var result = store.persist(observation);
                if (result == WeatherSocketStore.Result.TERMINAL_MISSING) {
                    if (attempts.merge(observation.eventId(), 1, Integer::sum) <= 2) {
                        continue;
                    }
                    retentionGaps++;
                } else if (result == WeatherSocketStore.Result.TRANSFER_GONE) {
                    retentionGaps++;
                } else {
                    written++;
                }
                retire(observation, WeatherReceipt.Disposition.ACCEPTED);
            } catch (Exception e) {
                failures++;
                LOG.warn("Network Weather socket transaction failed for event {}", observation.eventId(), e);
                final int attempt = attempts.merge(observation.eventId(), 1, Integer::sum);
                if (attempt > 2 || e instanceof IllegalArgumentException) {
                    retire(observation, WeatherReceipt.Disposition.REJECTED_INVALID);
                    if (!(e instanceof IllegalArgumentException)) {
                        ready = false;
                        error = "Socket transactions repeatedly failed; collection suspended pending capability recheck";
                    }
                } else {
                    nextWrite = nanoTime.getAsLong() + (1L << attempt) * 1_000_000_000L;
                    break;
                }
                if (!ready) {
                    break;
                }
            }
        }
    }

    private void retire(WeatherObservation observation, WeatherReceipt.Disposition disposition) {
        queue.acknowledge(receiptEpoch, List
                .of(new WeatherReceipt(observation.eventId(), receiptEpoch, System.currentTimeMillis(), disposition)));
        attempts.remove(observation.eventId());
    }

    public Status status() {
        return new Status(ready(), error, queue.status(), written, retentionGaps, failures);
    }

    public record Status(boolean ready, String error, WeatherOutbox.Status queue, long writtenEvents,
            long retentionGaps, long transactionFailures) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public void close() {
        closed = true;
        ready = false;
        queue.clear();
    }
}
