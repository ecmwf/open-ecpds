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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * Optional source evidence never waits for a transport worker. Loss counters must be reported by the owning source
 * scheduler.
 */
public final class WeatherOutbox {
    private static final int MAX_PENDING = 1000;
    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final long EXPIRY_NANOS = 300_000_000_000L;
    private final ReentrantLock lock = new ReentrantLock();
    private final LinkedHashMap<UUID, Pending> pending = new LinkedHashMap<>();
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong expired = new AtomicLong();
    private final LongSupplier nanoTime;
    private int pendingBytes;

    public WeatherOutbox() {
        this(System::nanoTime);
    }

    public WeatherOutbox(LongSupplier nanoTime) {
        this.nanoTime = java.util.Objects.requireNonNull(nanoTime);
    }

    public boolean offer(WeatherObservation observation) {
        java.util.Objects.requireNonNull(observation);
        if (!lock.tryLock()) {
            dropped.incrementAndGet();
            return false;
        }
        try {
            final var existing = pending.get(observation.eventId());
            if (existing != null) {
                if (!existing.observation.equals(observation)) {
                    throw new IllegalArgumentException("Conflicting outbox event identity");
                }
                return true;
            }
            final int bytes = reservation(observation);
            if (pending.size() >= MAX_PENDING || bytes > MAX_BYTES - pendingBytes) {
                dropped.incrementAndGet();
                return false;
            }
            pending.put(observation.eventId(), new Pending(observation, nanoTime.getAsLong(), bytes));
            pendingBytes += bytes;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The worker must additionally enforce the actual serialized HTTP/RMI batch size. Reservations below deliberately
     * overestimate the bounded DTO strings.
     */
    public List<Delivery> batch() {
        lock.lock();
        try {
            final long now = nanoTime.getAsLong();
            final var result = new ArrayList<Delivery>();
            int bytes = 0;
            final var iterator = pending.values().iterator();
            while (iterator.hasNext()) {
                final var item = iterator.next();
                final long age = now - item.created;
                if (age < 0 || age > EXPIRY_NANOS) {
                    pendingBytes -= item.bytes;
                    iterator.remove();
                    expired.incrementAndGet();
                } else if (result.size() < 100 && item.bytes <= 256 * 1024 - bytes) {
                    bytes += item.bytes;
                    result.add(new Delivery(item.observation, age / 1_000_000));
                }
            }
            return List.copyOf(result);
        } finally {
            lock.unlock();
        }
    }

    public void acknowledge(UUID expectedMasterEpoch, List<WeatherReceipt> receipts) {
        lock.lock();
        try {
            for (final var receipt : receipts) {
                if (!expectedMasterEpoch.equals(receipt.masterEpoch())) {
                    throw new IllegalArgumentException("Acknowledgement from a different Master epoch");
                }
            }
            for (final var receipt : receipts) {
                if (receipt.disposition() != WeatherReceipt.Disposition.RETRY_LATER) {
                    final var removed = pending.remove(receipt.eventId());
                    if (removed != null) {
                        pendingBytes -= removed.bytes;
                        if (receipt.disposition() != WeatherReceipt.Disposition.ACCEPTED
                                && receipt.disposition() != WeatherReceipt.Disposition.DUPLICATE) {
                            dropped.incrementAndGet();
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public Status status() {
        lock.lock();
        try {
            return new Status(pending.size(), pendingBytes, dropped.get(), expired.get());
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            dropped.addAndGet(pending.size());
            pending.clear();
            pendingBytes = 0;
        } finally {
            lock.unlock();
        }
    }

    public void acknowledge(WeatherBatch batch, WeatherAcknowledgement acknowledgement) {
        acknowledgement.validateFor(batch);
        acknowledge(batch.masterEpoch(), acknowledgement.receipts());
    }

    public record Delivery(WeatherObservation observation, long queueAgeMs) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        public Delivery {
            java.util.Objects.requireNonNull(observation, "observation");
            if (queueAgeMs < 0 || queueAgeMs > 300_000) {
                throw new IllegalArgumentException("Invalid Network Weather queue age");
            }
        }
    }

    public java.util.Set<UUID> pendingEventIds() {
        lock.lock();
        try {
            return java.util.Set.copyOf(pending.keySet());
        } finally {
            lock.unlock();
        }
    }

    public record Status(int pending, int reservedBytes, long dropped, long expired) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }

    private record Pending(WeatherObservation observation, long created, int bytes) {
    }

    private static int reservation(WeatherObservation observation) {
        // Include UUID/field names, JSON escaping, UTF-8 and object overhead.
        int characters = observation.sourceId().length() + observation.destinationId().length()
                + observation.hostId().length() + observation.configuredTarget().length()
                + observation.protocol().length() + observation.leg().length();
        for (final var connection : observation.connections()) {
            characters += (connection.localAddress() == null ? 0 : connection.localAddress().length())
                    + (connection.remoteAddress() == null ? 0 : connection.remoteAddress().length());
        }
        return 2048 + characters * 6 + observation.connections().size() * 512;
    }
}
