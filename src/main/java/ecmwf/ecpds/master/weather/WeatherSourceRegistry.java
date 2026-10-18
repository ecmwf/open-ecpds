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
import java.security.PublicKey;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Bounded source discovery. Only the Master supplies subscribe-authenticated callback registrations. */
public final class WeatherSourceRegistry {
    private static final long REFRESH_NS = 30_000_000_000L;
    private static final long EXPIRY_NS = 120_000_000_000L;
    private final LongSupplier nanoTime;
    private final LongSupplier wallTime;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private long verificationWindow;
    private int verificationCount;
    private boolean closed;

    public WeatherSourceRegistry() {
        this(System::nanoTime);
    }

    public WeatherSourceRegistry(LongSupplier nanoTime) {
        this(nanoTime, System::currentTimeMillis);
    }

    public WeatherSourceRegistry(LongSupplier nanoTime, LongSupplier wallTime) {
        this.nanoTime = java.util.Objects.requireNonNull(nanoTime);
        this.wallTime = java.util.Objects.requireNonNull(wallTime);
    }

    @FunctionalInterface
    public interface Probe {
        WeatherSourceIdentity identify(Object registration, UUID challenge) throws Exception;
    }

    public record Status(String sourceId, String state, UUID sourceEpoch, long verifiedAt, long roundTripMs,
            String detail) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /** Temporary loss of registration or verification capacity must not permanently retire source evidence. */
    public static final class UnavailableException extends java.security.GeneralSecurityException {
        private static final long serialVersionUID = 1L;

        private UnavailableException(String message) {
            super(message);
        }
    }

    /** One callback at most; no network calls or cryptography while holding the registry monitor. */
    public void poll(Set<String> selected, Map<String, ?> registrations, Probe probe) throws Exception {
        if (selected.size() > WeatherLimits.configured().maxSources()) {
            throw new IllegalArgumentException("Network Weather sources exceed the configured maximum");
        }
        selected.forEach(WeatherCapabilities::validateSource);
        final Entry entry;
        final long sent;
        final long sentWall = wallTime.getAsLong();
        synchronized (this) {
            if (closed) {
                return;
            }
            entries.keySet().retainAll(selected);
            for (final var source : selected.stream().sorted().toList()) {
                final var registration = registrations.get(source);
                final var old = entries.get(source);
                if (old == null || !java.util.Objects.equals(old.registration, registration)) {
                    entries.put(source, new Entry(source, registration));
                }
            }
            sent = nanoTime.getAsLong();
            entry = entries.values().stream()
                    .filter(candidate -> candidate.registration != null && !candidate.inFlight
                            && (!candidate.attempted || sent - candidate.lastAttempt >= candidate.retryNanos))
                    .min(Comparator
                            .comparingLong(candidate -> candidate.attempted ? candidate.lastAttempt : Long.MIN_VALUE))
                    .orElse(null);
            if (entry == null) {
                return;
            }
            entry.inFlight = true;
            entry.attempted = true;
            entry.lastAttempt = sent;
            entry.state = "VERIFYING";
            entry.detail = "Source verification is in progress; an existing proof retains its original expiry";
        }
        final var challenge = UUID.randomUUID();
        try {
            final var identity = probe.identify(entry.registration, challenge);
            if (identity == null) {
                throw new IllegalArgumentException("Missing Network Weather source identity");
            }
            final var key = identity.verify(entry.source, challenge);
            final long elapsed = nanoTime.getAsLong() - sent;
            if (elapsed < 0 || elapsed > 5_000_000_000L) {
                throw new IllegalStateException("Network Weather source callback exceeded five seconds");
            }
            final long receivedWall = wallTime.getAsLong();
            if (Math.abs(Math.subtractExact(receivedWall, sentWall) - elapsed / 1_000_000L) > 1_000) {
                throw new IllegalStateException("Master clock changed during source verification");
            }
            synchronized (this) {
                if (!closed && entries.get(entry.source) == entry) {
                    if (entry.identity != null && entry.identity.sourceEpoch().equals(identity.sourceEpoch())
                            && !entry.identity.publicKey().equals(identity.publicKey())) {
                        throw new IllegalArgumentException("Network Weather key changed within one source epoch");
                    }
                    entry.key = key;
                    entry.identity = identity;
                    entry.verifiedNanos = nanoTime.getAsLong();
                    entry.verifiedAt = receivedWall;
                    entry.roundTripMs = (elapsed + 999_999L) / 1_000_000L;
                    entry.clockOffset = Math.subtractExact(sentWall + entry.roundTripMs / 2, identity.issuedAt());
                    entry.state = "REGISTERED";
                    entry.detail = entry.registration instanceof WeatherRelayRegistration route
                            ? "Approved Proxy Host " + route.hostId() + " via subscribed relay " + route.relayId()
                                    + "; signed collection protocol verified"
                            : "Direct subscribed callback; signed collection protocol verified";
                    entry.retryNanos = REFRESH_NS;
                }
            }
        } catch (Exception e) {
            synchronized (this) {
                if (!closed && entries.get(entry.source) == entry) {
                    entry.key = null;
                    final boolean unsupported = WeatherUnsupportedException.isUnsupported(e);
                    entry.state = unsupported ? "UNSUPPORTED" : "FAILED";
                    entry.detail = unsupported ? "Mover does not expose the required Weather collection protocol"
                            : "Source verification failed; inspect the Master log";
                    entry.retryNanos = unsupported ? 300_000_000_000L : REFRESH_NS;
                }
            }
            throw e;
        } finally {
            synchronized (this) {
                entry.inFlight = false;
            }
        }
    }

    public synchronized List<Status> snapshot() {
        final long now = nanoTime.getAsLong();
        return entries.values().stream().map(entry -> {
            final boolean expired = entry.key != null
                    && (now - entry.verifiedNanos < 0 || now - entry.verifiedNanos >= EXPIRY_NS);
            return new Status(entry.source, expired ? "EXPIRED" : entry.state,
                    entry.identity == null ? null : entry.identity.sourceEpoch(), entry.verifiedAt, entry.roundTripMs,
                    entry.detail);
        }).toList();
    }

    /** A shared REST signature or a matching source name cannot supply the registered public key. */
    public Timing verify(WeatherBatch batch, Object currentRegistration)
            throws java.io.IOException, java.security.GeneralSecurityException {
        final Entry entry;
        final PublicKey key;
        synchronized (this) {
            entry = entries.get(batch.sourceId());
            if (entry == null || currentRegistration == null || !currentRegistration.equals(entry.registration)) {
                throw new java.security.GeneralSecurityException("Network Weather source process is not registered");
            }
            if (!usable(entry, currentRegistration)) {
                throw new UnavailableException("Network Weather source registration is temporarily unavailable");
            }
            if (!entry.identity.sourceEpoch().equals(batch.sourceEpoch())) {
                throw new java.security.GeneralSecurityException("Network Weather source process epoch does not match");
            }
            final long now = nanoTime.getAsLong();
            if (now - verificationWindow >= 1_000_000_000L || now - verificationWindow < 0) {
                verificationWindow = now;
                verificationCount = 0;
            }
            if (verificationCount >= 100) {
                throw new UnavailableException("Network Weather signature verification capacity exhausted");
            }
            verificationCount++;
            key = entry.key;
        }
        batch.verify(key);
        synchronized (this) {
            if (entries.get(batch.sourceId()) != entry || entry.key != key || !usable(entry, currentRegistration)) {
                throw new UnavailableException("Network Weather registration changed during verification");
            }
            final long elapsed = nanoTime.getAsLong() - entry.verifiedNanos;
            final long wallElapsed = wallTime.getAsLong() - entry.verifiedAt;
            if (Math.abs(wallElapsed - elapsed / 1_000_000L) > 1_000) {
                throw new UnavailableException("Master clock changed since source verification");
            }
            return new Timing(entry.clockOffset, entry.roundTripMs / 2 + 1_000);
        }
    }

    public synchronized boolean registered(String source) {
        final var entry = entries.get(source);
        return entry != null && usable(entry, entry.registration);
    }

    public record Timing(long offsetMs, long uncertaintyMs) {
        public void validateDispatch(WeatherBatch batch, long now) {
            final long sent = Math.addExact(batch.sentAt(), offsetMs);
            if (batch.sentAt() <= 0 || sent > now + uncertaintyMs || now - sent > 300_000 + uncertaintyMs)
                throw new IllegalArgumentException("Inconsistent or expired signed dispatch time");
        }

        public long queueAge(WeatherBatch batch, WeatherOutbox.Delivery delivery, long now) {
            validateDispatch(batch, now);
            if (batch.sentAt() <= 0) {
                throw new IllegalArgumentException("Missing signed source dispatch time");
            }
            final long sent = Math.addExact(batch.sentAt(), offsetMs);
            final long occurred = Math.addExact(delivery.observation().occurredAt(), offsetMs);
            if (sent > now + uncertaintyMs || now - sent > 300_000 + uncertaintyMs || occurred > sent + uncertaintyMs) {
                throw new IllegalArgumentException("Inconsistent or expired Network Weather source timing");
            }
            return Math.max(delivery.queueAgeMs(), Math.max(0, now - occurred - uncertaintyMs));
        }

        public long bucketOffset(WeatherObservation observation) {
            final long corrected = Math.addExact(observation.occurredAt(), offsetMs);
            return (corrected - uncertaintyMs) / 60_000 == (corrected + uncertaintyMs) / 60_000 ? offsetMs
                    : Long.MAX_VALUE;
        }
    }

    private boolean usable(Entry entry, Object registration) {
        if (closed || entry == null || entry.key == null || registration == null
                || !registration.equals(entry.registration)) {
            return false;
        }
        final long age = nanoTime.getAsLong() - entry.verifiedNanos;
        return age >= 0 && age < EXPIRY_NS;
    }

    public synchronized void close() {
        closed = true;
        entries.clear();
    }

    private static final class Entry {
        final String source;
        final Object registration;
        boolean attempted;
        boolean inFlight;
        long lastAttempt;
        long retryNanos = REFRESH_NS;
        long verifiedNanos;
        long verifiedAt;
        long roundTripMs;
        long clockOffset;
        PublicKey key;
        WeatherSourceIdentity identity;
        String state;
        String detail;

        Entry(String source, Object registration) {
            this.source = source;
            this.registration = registration;
            state = registration == null ? "UNAVAILABLE" : "PENDING";
            detail = registration == null ? "No subscribed Mover or live approved Continental identity route"
                    : "Waiting for the dedicated source-verification scheduler";
        }
    }
}
