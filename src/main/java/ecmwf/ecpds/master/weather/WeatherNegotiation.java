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

import java.util.function.LongSupplier;

/**
 * Source-side control lease. Network work belongs to the scheduler; execution callbacks only read the cached state.
 */
public final class WeatherNegotiation {
    private static final long MAX_ROUND_TRIP_MS = 5_000;
    private final String sourceId;
    private final LongSupplier nanoTime;
    private volatile Lease lease;
    private volatile boolean closed;

    public WeatherNegotiation(String sourceId) {
        this(sourceId, System::nanoTime);
    }

    public WeatherNegotiation(String sourceId, LongSupplier nanoTime) {
        WeatherCapabilities.validateSource(sourceId);
        this.sourceId = sourceId;
        this.nanoTime = java.util.Objects.requireNonNull(nanoTime);
    }

    public void update(WeatherCapabilities capabilities, long sentAtMs, long sentAtNanos) {
        lease = null;
        if (closed) {
            throw new IllegalStateException("Network Weather negotiation is closed");
        }
        java.util.Objects.requireNonNull(capabilities, "capabilities");
        final long now = nanoTime.getAsLong();
        final long elapsedNanos = now - sentAtNanos;
        if (!sourceId.equals(capabilities.sourceId()) || sentAtMs <= 0 || elapsedNanos < 0
                || elapsedNanos > MAX_ROUND_TRIP_MS * 1_000_000L) {
            throw new IllegalArgumentException("Unusable Network Weather capability response");
        }
        final long elapsedMs = (elapsedNanos + 999_999L) / 1_000_000L;
        // Charge the entire round trip against the lease, including time spent in relays.
        final long remainingMs = capabilities.validForMs() - elapsedMs;
        if (remainingMs <= 0) {
            throw new IllegalArgumentException("Expired Network Weather capability response");
        }
        final long offset = Math.subtractExact(capabilities.issuedAt(), Math.addExact(sentAtMs, elapsedMs / 2));
        lease = new Lease(capabilities, now, remainingMs * 1_000_000L, offset, elapsedMs);
    }

    public void invalidate() {
        lease = null;
    }

    public void close() {
        closed = true;
        lease = null;
    }

    public Lease current() {
        final var current = lease;
        if (closed || current == null) {
            return null;
        }
        final long age = nanoTime.getAsLong() - current.receivedAtNanos();
        return age >= 0 && age < current.remainingNanos() ? current : null;
    }

    public record Lease(WeatherCapabilities capabilities, long receivedAtNanos, long remainingNanos,
            long estimatedClockOffsetMs, long roundTripMs) {
    }
}
