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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ecmwf.common.ectrans.PasswordRotation.Approved;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Assignment;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Delivery;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Purpose;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

/** Local, single-use authorization state for encrypted transfer credentials. */
final class MoverPasswordDelivery {
    private static final int MAX_PENDING = 1024;
    private final Map<UUID, Challenge> pending = new HashMap<>();
    private final String mover;
    private final Route route;
    private final Map<Long, Approved> endpoints;
    private final RSAPublicKey masterKey;
    private final RSAPrivateKey moverKey;
    private final Clock clock;

    private record Challenge(long host, Delivery delivery, Instant expires) {
    }

    MoverPasswordDelivery(final String mover, final Route route, final Map<Long, Approved> endpoints,
            final RSAPublicKey masterKey, final RSAPrivateKey moverKey, final Clock clock) {
        this.mover = mover;
        this.route = route;
        this.endpoints = Map.copyOf(endpoints);
        this.masterKey = masterKey;
        this.moverKey = moverKey;
        this.clock = clock;
    }

    synchronized UUID challenge(final Route transport, final long host, final Delivery delivery) throws IOException {
        endpoint(transport, host, delivery);
        final var now = clock.instant();
        pending.values().removeIf(challenge -> !challenge.expires().isAfter(now));
        if (pending.size() >= MAX_PENDING) {
            throw rejected();
        }
        final var nonce = UUID.randomUUID();
        if (pending.putIfAbsent(nonce,
                new Challenge(host, delivery, now.plusSeconds(MoverPasswordEnvelope.VALID_SECONDS))) != null) {
            throw rejected();
        }
        return nonce;
    }

    String open(final Route transport, final long host, final UUID nonce, final Delivery delivery,
            final String ciphertext) throws IOException {
        endpoint(transport, host, delivery);
        final var challenge = current(host, nonce, delivery);
        return MoverPasswordEnvelope.openTransfer(ciphertext,
                new Assignment(Purpose.PASSWORD_TRANSFER, route, mover, host, nonce), delivery, moverKey, masterKey,
                () -> consume(nonce, challenge));
    }

    String successful(final long host, final UUID nonce, final Delivery delivery, final boolean successful)
            throws IOException {
        return MoverPasswordEnvelope.signSuccessfulDelivery(
                new Assignment(Purpose.PASSWORD_TRANSFER, route, mover, host, nonce), delivery, successful, moverKey);
    }

    Approved endpoint(final Route transport, final long host, final Delivery delivery) throws IOException {
        final var endpoint = endpoints.get(host);
        if (transport != route || endpoint == null || delivery == null
                || !endpoint.fingerprint().equals(delivery.endpointFingerprint())) {
            throw rejected();
        }
        return endpoint;
    }

    private synchronized Challenge current(final long host, final UUID nonce, final Delivery delivery)
            throws IOException {
        final var challenge = pending.get(nonce);
        if (challenge == null || challenge.host() != host || !challenge.delivery().equals(delivery)
                || !challenge.expires().isAfter(clock.instant())) {
            throw rejected();
        }
        return challenge;
    }

    private synchronized boolean consume(final UUID nonce, final Challenge challenge) {
        return challenge.expires().isAfter(clock.instant()) && pending.remove(nonce, challenge);
    }

    private static IOException rejected() {
        return new IOException("Mover credential envelope rejected");
    }
}
