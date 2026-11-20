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
 */

package ecmwf.ecpds.mover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ecmwf.common.ectrans.PasswordRotation;
import ecmwf.common.ectrans.PasswordRotation.Approved;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Assignment;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Purpose;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

/** Receiver-side probe authorization. No network destination is taken from the control request. */
final class MoverPasswordProbe {
    private static final Duration LIFETIME = Duration.ofSeconds(60);
    private static final int MAX_KEY_BYTES = 16_384;
    private final Map<Long, Approved> endpoints;
    private final Map<Long, Challenge> pending = new HashMap<>();
    private final RSAPublicKey masterKey;
    private final RSAPrivateKey deliveryKey;
    private final String mover;
    private final Route route;
    private final Clock clock;

    private record Challenge(UUID nonce, Instant expires) {
    }

    MoverPasswordProbe(final String mover, final Route route, final Map<Long, Approved> endpoints,
            final RSAPublicKey masterKey, final RSAPrivateKey deliveryKey, final Clock clock) {
        this.mover = mover;
        this.route = route;
        this.endpoints = Map.copyOf(endpoints);
        this.masterKey = masterKey;
        this.deliveryKey = deliveryKey;
        this.clock = clock;
    }

    static MoverPasswordProbe configured(final String mover, final Route route) throws IOException {
        if (!Cnf.at("PasswordProbe", "enabled", false)) {
            return null;
        }
        try {
            final var endpoints = new HashMap<Long, Approved>();
            for (final var entry : Cnf.at("PasswordProbeHosts", Map.<String, String> of()).entrySet()) {
                final long host = Long.parseLong(entry.getKey());
                if (host <= 0 || endpoints.putIfAbsent(host, PasswordRotation.approved(host)) != null) {
                    throw new IllegalArgumentException();
                }
            }
            if (endpoints.isEmpty() || mover == null || mover.isBlank() || mover.length() > 255) {
                throw new IllegalArgumentException();
            }
            final var factory = KeyFactory.getInstance("RSA");
            final var signing = (RSAPublicKey) factory.generatePublic(
                    new X509EncodedKeySpec(readKey(Cnf.at("PasswordProbe", "masterSigningPublicKey", ""))));
            final var encoded = readKey(Cnf.at("PasswordProbe", "deliveryPrivateKey", ""));
            final RSAPrivateKey delivery;
            try {
                delivery = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            } finally {
                Arrays.fill(encoded, (byte) 0);
            }
            if (signing.getModulus().bitLength() < 2048 || delivery.getModulus().bitLength() < 2048
                    || signing.getModulus().equals(delivery.getModulus())) {
                throw new IllegalArgumentException();
            }
            return new MoverPasswordProbe(mover, route, endpoints, signing, delivery, Clock.systemUTC());
        } catch (final GeneralSecurityException | IOException | RuntimeException _) {
            throw new IOException("Invalid Mover password probe configuration");
        }
    }

    private static byte[] readKey(final String configured) throws IOException {
        final var path = Path.of(configured);
        if (!path.isAbsolute() || !Files.isRegularFile(path)) {
            throw new IOException("Invalid password probe key file");
        }
        try (final var input = Files.newInputStream(path)) {
            final var bytes = input.readNBytes(MAX_KEY_BYTES + 1);
            if (bytes.length == 0 || bytes.length > MAX_KEY_BYTES) {
                Arrays.fill(bytes, (byte) 0);
                throw new IOException("Invalid password probe key file");
            }
            return bytes;
        }
    }

    MoverPasswordDelivery delivery() {
        return new MoverPasswordDelivery(mover, route, endpoints, masterKey, deliveryKey, clock);
    }

    MoverPasswordDrain drain(final MoverLegacySessions sessions) {
        return new MoverPasswordDrain(mover, route, endpoints.keySet(), masterKey, deliveryKey, sessions, clock);
    }

    synchronized UUID challenge(final Route transport, final long host) throws IOException {
        if (transport != route || !endpoints.containsKey(host)) {
            throw new IOException("Password probe not permitted");
        }
        final var now = clock.instant();
        final var previous = pending.get(host);
        if (previous != null && previous.expires().isAfter(now)) {
            return previous.nonce();
        }
        final var issued = new Challenge(UUID.randomUUID(), now.plus(LIFETIME));
        pending.put(host, issued);
        return issued.nonce();
    }

    MoverPasswordEnvelope.ProbeReceipt probe(final Route transport, final long host, final UUID nonce,
            final String envelope) throws IOException {
        final var endpoint = endpoints.get(host);
        if (transport != route || endpoint == null || !consume(host, nonce, false)) {
            throw new IOException("Password probe not permitted");
        }
        final var assignment = new Assignment(Purpose.PASSWORD_PROBE, route, mover, host, nonce);
        try {
            return MoverPasswordEnvelope.runProbe(envelope, assignment, endpoint, deliveryKey, masterKey,
                    () -> consume(host, nonce, true), endpoint::probe);
        } catch (final RuntimeException _) {
            throw new IOException("Password probe failed");
        }
    }

    private synchronized boolean consume(final long host, final UUID nonce, final boolean remove) {
        final var challenge = pending.get(host);
        if (challenge == null || !challenge.nonce().equals(nonce) || !challenge.expires().isAfter(clock.instant())) {
            return false;
        }
        if (remove) {
            pending.remove(host);
        }
        return true;
    }
}
