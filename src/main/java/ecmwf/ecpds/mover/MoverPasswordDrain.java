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
import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

/** Authenticated fence and session-count evidence, not a certification of a fleet-wide drain. */
public final class MoverPasswordDrain {
    public static final int MAX_MESSAGE = 8192;
    private static final int LIFETIME = 60;
    private static final JOSEObjectType COMMAND = new JOSEObjectType("ecpds-legacy-fence-command+jwt");
    private static final JOSEObjectType RECEIPT = new JOSEObjectType("ecpds-legacy-fence-receipt+jwt");

    public record Challenge(UUID incarnation, UUID nonce) implements Serializable {
        public Challenge {
            if (incarnation == null || nonce == null) {
                throw new IllegalArgumentException("Invalid legacy fence challenge");
            }
        }
    }

    public record Evidence(UUID incarnation, UUID cutover, int sessions) {
    }

    private record Pending(Challenge challenge, Instant expires) {
    }

    private final String mover;
    private final Route route;
    private final Set<Long> hosts;
    private final RSAPublicKey masterKey;
    private final RSAPrivateKey moverKey;
    private final MoverLegacySessions sessions;
    private final Clock clock;
    private final Map<Long, Pending> pending = new HashMap<>();

    MoverPasswordDrain(final String mover, final Route route, final Set<Long> hosts, final RSAPublicKey masterKey,
            final RSAPrivateKey moverKey, final MoverLegacySessions sessions, final Clock clock) {
        this.mover = mover;
        this.route = route;
        this.hosts = Set.copyOf(hosts);
        this.masterKey = masterKey;
        this.moverKey = moverKey;
        this.sessions = sessions;
        this.clock = clock;
    }

    synchronized Challenge challenge(final Route transport, final long host) throws IOException {
        requireHost(transport, host);
        final var previous = pending.get(host);
        if (previous != null && previous.expires().isAfter(clock.instant())) {
            return previous.challenge();
        }
        final var challenge = new Challenge(sessions.snapshot(host).incarnation(), UUID.randomUUID());
        pending.put(host, new Pending(challenge, clock.instant().plusSeconds(LIFETIME)));
        return challenge;
    }

    synchronized String fence(final Route transport, final long host, final UUID cutover, final Challenge challenge,
            final String command) throws IOException {
        requireHost(transport, host);
        final var expected = pending.get(host);
        if (expected == null || !expected.challenge().equals(challenge)
                || !expected.expires().isAfter(clock.instant())) {
            throw rejected();
        }
        final var claims = verify(command, COMMAND, masterKey, clock.instant());
        requireBinding(claims, mover, route, host, cutover, challenge);
        pending.remove(host);
        final var snapshot = sessions.fence(host, cutover);
        if (!snapshot.incarnation().equals(challenge.incarnation())) {
            throw rejected();
        }
        return sign(binding(mover, route, host, cutover, challenge, clock.instant())
                .claim("sessions", snapshot.sessions()).build(), RECEIPT, moverKey);
    }

    public static String command(final String mover, final Route route, final long host, final UUID cutover,
            final Challenge challenge, final RSAPrivateKey masterKey) throws IOException {
        return sign(binding(mover, route, host, cutover, challenge, Instant.now()).build(), COMMAND, masterKey);
    }

    public static Evidence verifyEvidence(final String receipt, final String mover, final Route route, final long host,
            final UUID cutover, final Challenge challenge, final RSAPublicKey moverKey) throws IOException {
        final var claims = verify(receipt, RECEIPT, moverKey, Instant.now());
        requireBinding(claims, mover, route, host, cutover, challenge);
        try {
            final var count = claims.getLongClaim("sessions");
            if (count == null || count < 0 || count > 16384) {
                throw rejected();
            }
            return new Evidence(challenge.incarnation(), cutover, count.intValue());
        } catch (final java.text.ParseException _) {
            throw rejected();
        }
    }

    private void requireHost(final Route transport, final long host) throws IOException {
        if (transport != route || !hosts.contains(host)) {
            throw rejected();
        }
        sessions.snapshot(host);
    }

    private static JWTClaimsSet.Builder binding(final String mover, final Route route, final long host,
            final UUID cutover, final Challenge challenge, final Instant now) throws IOException {
        if (mover == null || mover.isBlank() || mover.length() > 255 || route == null || host <= 0 || cutover == null
                || challenge == null) {
            throw rejected();
        }
        return new JWTClaimsSet.Builder().subject(mover).issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(LIFETIME))).claim("route", route.name()).claim("host", host)
                .claim("cutover", cutover.toString()).claim("incarnation", challenge.incarnation().toString())
                .jwtID(challenge.nonce().toString());
    }

    private static void requireBinding(final JWTClaimsSet claims, final String mover, final Route route,
            final long host, final UUID cutover, final Challenge challenge) throws IOException {
        try {
            if (mover == null || route == null || cutover == null || challenge == null
                    || !mover.equals(claims.getSubject()) || !route.name().equals(claims.getStringClaim("route"))
                    || !Long.valueOf(host).equals(claims.getLongClaim("host"))
                    || !cutover.toString().equals(claims.getStringClaim("cutover"))
                    || !challenge.incarnation().toString().equals(claims.getStringClaim("incarnation"))
                    || !challenge.nonce().toString().equals(claims.getJWTID())) {
                throw rejected();
            }
        } catch (final java.text.ParseException _) {
            throw rejected();
        }
    }

    private static String sign(final JWTClaimsSet claims, final JOSEObjectType type, final RSAPrivateKey key)
            throws IOException {
        try {
            if (key == null || key.getModulus().bitLength() < 2048) {
                throw rejected();
            }
            final var signed = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.PS256).type(type).build(),
                    new Payload(claims.toJSONObject()));
            signed.sign(new RSASSASigner(key));
            final var message = signed.serialize();
            if (message.length() > MAX_MESSAGE) {
                throw rejected();
            }
            return message;
        } catch (final com.nimbusds.jose.JOSEException | RuntimeException _) {
            throw rejected();
        }
    }

    private static JWTClaimsSet verify(final String value, final JOSEObjectType type, final RSAPublicKey key,
            final Instant now) throws IOException {
        try {
            if (value == null || value.length() > MAX_MESSAGE || key == null || key.getModulus().bitLength() < 2048) {
                throw rejected();
            }
            final var signed = JWSObject.parse(value);
            final var header = signed.getHeader();
            if (!JWSAlgorithm.PS256.equals(header.getAlgorithm()) || !type.equals(header.getType())
                    || header.getCriticalParams() != null && !header.getCriticalParams().isEmpty()
                    || !signed.verify(new RSASSAVerifier(key))) {
                throw rejected();
            }
            final var claims = JWTClaimsSet.parse(signed.getPayload().toJSONObject());
            final var issued = claims.getIssueTime();
            final var expires = claims.getExpirationTime();
            if (issued == null || expires == null || issued.toInstant().isAfter(now)
                    || !expires.toInstant().isAfter(now)
                    || !expires.toInstant().equals(issued.toInstant().plusSeconds(LIFETIME))) {
                throw rejected();
            }
            return claims;
        } catch (final com.nimbusds.jose.JOSEException | java.text.ParseException | RuntimeException _) {
            throw rejected();
        }
    }

    private static IOException rejected() {
        return new IOException("Legacy session fence request or evidence rejected");
    }
}
