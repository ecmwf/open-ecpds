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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import ecmwf.common.ectrans.CredentialProbe.Endpoint;
import ecmwf.common.ectrans.CredentialProbe.Result;

/** Application-level recipient binding for credential delivery over mutual TLS RMI or the HTTPS control relay. */
public final class MoverPasswordEnvelope {
    private static final JOSEObjectType TYPE = new JOSEObjectType("ecpds-mover-password+jose");
    private static final JOSEObjectType SUCCESS = new JOSEObjectType("ecpds-password-delivery-terminal+jwt");
    static final int VALID_SECONDS = 60;
    private static final int MAX_PASSWORD = 4096;
    public static final int MAX_ENVELOPE = 64 * 1024;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RECEIPT_KEY_BYTES = 32;

    public record ProbeReceipt(Result result, String proof) implements java.io.Serializable {
    }

    public static final class SealedProbe implements AutoCloseable {
        private final String ciphertext;
        private final byte[] key;
        private boolean closed;

        private SealedProbe(final String ciphertext, final byte[] key) {
            this.ciphertext = ciphertext;
            this.key = key;
        }

        public String ciphertext() {
            return ciphertext;
        }

        public synchronized Result verify(final ProbeReceipt receipt) throws IOException {
            if (closed) {
                throw rejected();
            }
            try {
                if (receipt == null || receipt.proof() == null || receipt.proof().length() != 43 || !MessageDigest
                        .isEqual(proof(key, receipt.result()), Base64.getUrlDecoder().decode(receipt.proof()))) {
                    throw rejected();
                }
                return receipt.result();
            } catch (final IllegalArgumentException _) {
                throw rejected();
            } finally {
                close();
            }
        }

        @Override
        public synchronized void close() {
            closed = true;
            Arrays.fill(key, (byte) 0);
        }
    }

    @FunctionalInterface
    interface ProbeAction {
        Result run(String password) throws IOException;
    }

    public enum Purpose {
        PASSWORD_PROBE, PASSWORD_TRANSFER
    }

    public enum Route {
        DIRECT_RMI, CONTINENTAL_HTTPS
    }

    public enum Operation {
        SIZE, PUT
    }

    public record Assignment(Purpose purpose, Route route, String mover, long host, UUID attempt) {
        public Assignment {
            if (purpose == null || route == null || mover == null || mover.isBlank() || mover.length() > 255
                    || host <= 0 || attempt == null) {
                throw new IllegalArgumentException("Invalid credential assignment");
            }
        }
    }

    public record Delivery(long transferId, long fileId, long fileSize, String destination, String target,
            long localOffset, long remoteOffset, String endpointFingerprint, String hostConfiguration,
            Operation operation, UUID credentialReference) implements java.io.Serializable {
        public Delivery(final long transferId, final long fileId, final long fileSize, final String destination,
                final String target, final long localOffset, final long remoteOffset, final String endpointFingerprint,
                final String hostConfiguration, final Operation operation) {
            this(transferId, fileId, fileSize, destination, target, localOffset, remoteOffset, endpointFingerprint,
                    hostConfiguration, operation, null);
        }

        public Delivery {
            if (transferId <= 0 || fileId <= 0 || fileSize < 0 || localOffset < 0 || localOffset > fileSize
                    || remoteOffset < 0 || destination == null || destination.isBlank() || destination.length() > 32
                    || target == null || target.isBlank() || target.length() > 4096 || endpointFingerprint == null
                    || !endpointFingerprint.matches("[0-9a-f]{64}") || hostConfiguration == null
                    || !hostConfiguration.matches("[0-9a-f]{64}") || operation == null
                    || operation == Operation.SIZE && (localOffset != 0 || remoteOffset != 0)) {
                throw new IllegalArgumentException("Invalid password delivery context");
            }
        }

        public String fingerprint() {
            try {
                final var digest = MessageDigest.getInstance("SHA-256");
                for (final var value : new String[] { Long.toString(transferId), Long.toString(fileId),
                        Long.toString(fileSize), destination, target, Long.toString(localOffset),
                        Long.toString(remoteOffset), endpointFingerprint, hostConfiguration, operation.name(),
                        String.valueOf(credentialReference) }) {
                    final var bytes = value.getBytes(StandardCharsets.UTF_8);
                    digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                    digest.update((byte) ':');
                    digest.update(bytes);
                }
                return java.util.HexFormat.of().formatHex(digest.digest());
            } catch (final GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }

        private boolean matches(final JWTClaimsSet claims) throws ParseException {
            return Long.valueOf(transferId).equals(claims.getLongClaim("transfer_id"))
                    && Long.valueOf(fileId).equals(claims.getLongClaim("file_id"))
                    && Long.valueOf(fileSize).equals(claims.getLongClaim("file_size"))
                    && destination.equals(claims.getStringClaim("destination"))
                    && target.equals(claims.getStringClaim("target"))
                    && Long.valueOf(localOffset).equals(claims.getLongClaim("local_offset"))
                    && Long.valueOf(remoteOffset).equals(claims.getLongClaim("remote_offset"))
                    && endpointFingerprint.equals(claims.getStringClaim("endpoint"))
                    && hostConfiguration.equals(claims.getStringClaim("host_config"))
                    && java.util.Objects.equals(credentialReference == null ? null : credentialReference.toString(),
                            claims.getStringClaim("credential_reference"))
                    && operation.name().equals(claims.getStringClaim("operation"));
        }
    }

    private MoverPasswordEnvelope() {
    }

    public record SuccessfulDelivery(UUID attempt, UUID reference, String mover, long host, Route route, String context,
            boolean successful) {
    }

    static String signSuccessfulDelivery(final Assignment assignment, final Delivery delivery, final boolean successful,
            final RSAPrivateKey moverKey) throws IOException {
        if (assignment.purpose() != Purpose.PASSWORD_TRANSFER || delivery.operation() != Operation.PUT
                || delivery.credentialReference() == null || moverKey.getModulus().bitLength() < 2048) {
            throw rejected();
        }
        try {
            final var now = Instant.now();
            final var claims = new JWTClaimsSet.Builder().subject(assignment.mover())
                    .jwtID(assignment.attempt().toString())
                    .claim("reference", delivery.credentialReference().toString()).claim("host", assignment.host())
                    .claim("route", assignment.route().name()).claim("context", delivery.fingerprint())
                    .claim("outcome", successful ? "SUCCEEDED" : "FAILED").issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(7 * 86400))).build();
            final var signed = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.PS256).type(SUCCESS).build(),
                    new Payload(claims.toJSONObject()));
            signed.sign(new RSASSASigner(moverKey));
            return signed.serialize();
        } catch (final JOSEException | RuntimeException _) {
            throw rejected();
        }
    }

    public static UUID successfulDeliveryAttempt(final String receipt) throws IOException {
        try {
            if (receipt == null || receipt.length() > 8192) {
                throw rejected();
            }
            return UUID.fromString(JWTClaimsSet.parse(JWSObject.parse(receipt).getPayload().toJSONObject()).getJWTID());
        } catch (final ParseException | RuntimeException _) {
            throw rejected();
        }
    }

    public static SuccessfulDelivery verifySuccessfulDelivery(final String receipt, final RSAPublicKey moverKey,
            final Assignment expected, final UUID reference, final String context) throws IOException {
        try {
            final var attempt = successfulDeliveryAttempt(receipt);
            final var signed = JWSObject.parse(receipt);
            if (moverKey == null || moverKey.getModulus().bitLength() < 2048
                    || !JWSAlgorithm.PS256.equals(signed.getHeader().getAlgorithm())
                    || !SUCCESS.equals(signed.getHeader().getType())
                    || signed.getHeader().getCriticalParams() != null
                            && !signed.getHeader().getCriticalParams().isEmpty()
                    || !signed.verify(new RSASSAVerifier(moverKey))) {
                throw rejected();
            }
            final var claims = JWTClaimsSet.parse(signed.getPayload().toJSONObject());
            final var now = Instant.now();
            if (claims.getIssueTime() == null || claims.getExpirationTime() == null
                    || claims.getIssueTime().toInstant().isAfter(now)
                    || !claims.getExpirationTime().toInstant().isAfter(now) || !claims.getExpirationTime().toInstant()
                            .equals(claims.getIssueTime().toInstant().plusSeconds(7 * 86400))) {
                throw rejected();
            }
            final var outcome = claims.getStringClaim("outcome");
            if (!"SUCCEEDED".equals(outcome) && !"FAILED".equals(outcome)) {
                throw rejected();
            }
            final var verified = new SuccessfulDelivery(attempt, UUID.fromString(claims.getStringClaim("reference")),
                    claims.getSubject(), claims.getLongClaim("host"), Route.valueOf(claims.getStringClaim("route")),
                    claims.getStringClaim("context"), "SUCCEEDED".equals(outcome));
            if (expected == null || expected.purpose() != Purpose.PASSWORD_TRANSFER
                    || !verified.attempt().equals(expected.attempt()) || !verified.reference().equals(reference)
                    || !verified.mover().equals(expected.mover()) || verified.host() != expected.host()
                    || verified.route() != expected.route() || !verified.context().equals(context)) {
                throw rejected();
            }
            return verified;
        } catch (final JOSEException | ParseException | RuntimeException _) {
            throw rejected();
        }
    }

    public static String sealTransfer(final Assignment assignment, final Delivery delivery, final String password,
            final RSAPublicKey moverKey, final RSAPrivateKey masterKey) throws IOException {
        if (assignment.purpose() != Purpose.PASSWORD_TRANSFER || delivery == null) {
            throw rejected();
        }
        return seal(assignment, password, moverKey, masterKey, null, delivery.endpointFingerprint(), delivery);
    }

    public static SealedProbe sealProbe(final Assignment assignment, final Endpoint endpoint, final String password,
            final RSAPublicKey moverKey, final RSAPrivateKey masterKey) throws IOException {
        if (assignment.purpose() != Purpose.PASSWORD_PROBE) {
            throw rejected();
        }
        final var key = new byte[RECEIPT_KEY_BYTES];
        RANDOM.nextBytes(key);
        try {
            return new SealedProbe(seal(assignment, password, moverKey, masterKey, key, endpoint.fingerprint(), null),
                    key);
        } catch (final IOException e) {
            Arrays.fill(key, (byte) 0);
            throw e;
        }
    }

    private static String seal(final Assignment assignment, final String password, final RSAPublicKey moverKey,
            final RSAPrivateKey masterKey, final byte[] receiptKey, final String endpointFingerprint,
            final Delivery delivery) throws IOException {
        if (!validPassword(password)) {
            throw rejected();
        }
        try {
            final var now = Instant.now();
            final var claims = new JWTClaimsSet.Builder().subject(assignment.mover())
                    .jwtID(assignment.attempt().toString()).issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(VALID_SECONDS))).claim("host", assignment.host())
                    .claim("purpose", assignment.purpose().name()).claim("route", assignment.route().name())
                    .claim("password", password).claim("endpoint", endpointFingerprint);
            if (receiptKey != null) {
                claims.claim("receipt_key", Base64.getUrlEncoder().withoutPadding().encodeToString(receiptKey));
            }
            if (delivery != null) {
                claims.claim("transfer_id", delivery.transferId()).claim("file_id", delivery.fileId())
                        .claim("file_size", delivery.fileSize()).claim("destination", delivery.destination())
                        .claim("target", delivery.target()).claim("local_offset", delivery.localOffset())
                        .claim("remote_offset", delivery.remoteOffset())
                        .claim("host_config", delivery.hostConfiguration())
                        .claim("credential_reference",
                                delivery.credentialReference() == null ? null
                                        : delivery.credentialReference().toString())
                        .claim("operation", delivery.operation().name());
            }
            final var encrypted = new EncryptedJWT(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).type(TYPE).build(),
                    claims.build());
            encrypted.encrypt(new RSAEncrypter(moverKey));
            final var signed = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.PS256).type(TYPE).build(),
                    new Payload(encrypted.serialize()));
            signed.sign(new RSASSASigner(masterKey));
            final var compact = signed.serialize();
            if (compact.length() > MAX_ENVELOPE) {
                throw rejected();
            }
            return compact;
        } catch (final JOSEException | RuntimeException _) {
            throw rejected();
        }
    }

    /**
     * The expected assignment and delivery context must describe the operation the Mover will actually execute,
     * including its locally approved endpoint. The caller must atomically consume the locally issued assignment before
     * the password is released; assignments outstanding when the Mover restarts must not be re-created.
     */
    static String openTransfer(final String compact, final Assignment expected, final Delivery delivery,
            final RSAPrivateKey moverKey, final RSAPublicKey masterKey, final BooleanSupplier consumeAssignment)
            throws IOException {
        if (expected.purpose() != Purpose.PASSWORD_TRANSFER || delivery == null) {
            throw rejected();
        }
        final var claims = open(compact, expected, moverKey, masterKey);
        try {
            if (!delivery.matches(claims) || !consumeAssignment.getAsBoolean()) {
                throw rejected();
            }
        } catch (final ParseException _) {
            throw rejected();
        }
        return (String) claims.getClaim("password");
    }

    static ProbeReceipt runProbe(final String compact, final Assignment expected, final Endpoint endpoint,
            final RSAPrivateKey moverKey, final RSAPublicKey masterKey, final BooleanSupplier consumeAssignment,
            final ProbeAction action) throws IOException {
        if (expected.purpose() != Purpose.PASSWORD_PROBE) {
            throw rejected();
        }
        final var claims = open(compact, expected, moverKey, masterKey);
        if (!endpoint.fingerprint().equals(claims.getClaim("endpoint"))) {
            throw rejected();
        }
        final var encoded = claims.getClaim("receipt_key");
        if (!(encoded instanceof final String value) || value.length() != 43) {
            throw rejected();
        }
        final byte[] key;
        try {
            key = Base64.getUrlDecoder().decode(value);
        } catch (final IllegalArgumentException _) {
            throw rejected();
        }
        try {
            if (key.length != RECEIPT_KEY_BYTES || !consumeAssignment.getAsBoolean()) {
                throw rejected();
            }
            final var result = action.run((String) claims.getClaim("password"));
            return new ProbeReceipt(result, Base64.getUrlEncoder().withoutPadding().encodeToString(proof(key, result)));
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    private static byte[] proof(final byte[] key, final Result result) throws IOException {
        if (result == null) {
            throw rejected();
        }
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac
                    .doFinal(("ecpds-password-probe-receipt-v1:" + result.name()).getBytes(StandardCharsets.US_ASCII));
        } catch (final GeneralSecurityException _) {
            throw rejected();
        }
    }

    private static JWTClaimsSet open(final String compact, final Assignment expected, final RSAPrivateKey moverKey,
            final RSAPublicKey masterKey) throws IOException {
        if (compact == null || compact.length() > MAX_ENVELOPE) {
            throw rejected();
        }
        try {
            final var signed = JWSObject.parse(compact);
            if (!TYPE.equals(signed.getHeader().getType())
                    || !JWSAlgorithm.PS256.equals(signed.getHeader().getAlgorithm())
                    || !signed.verify(new RSASSAVerifier(masterKey))) {
                throw rejected();
            }
            final var encrypted = EncryptedJWT.parse(signed.getPayload().toString());
            final var header = encrypted.getHeader();
            if (!TYPE.equals(header.getType()) || !JWEAlgorithm.RSA_OAEP_256.equals(header.getAlgorithm())
                    || !EncryptionMethod.A256GCM.equals(header.getEncryptionMethod())
                    || header.getCompressionAlgorithm() != null) {
                throw rejected();
            }
            encrypted.decrypt(new RSADecrypter(moverKey));
            final var claims = encrypted.getJWTClaimsSet();
            final var now = Instant.now();
            final var issued = claims.getIssueTime().toInstant();
            final var expires = claims.getExpirationTime().toInstant();
            final var password = claims.getStringClaim("password");
            if (issued.isAfter(now) || !now.isBefore(expires) || expires.isAfter(issued.plusSeconds(VALID_SECONDS))
                    || !expected.mover().equals(claims.getSubject())
                    || !expected.attempt().toString().equals(claims.getJWTID())
                    || !Long.valueOf(expected.host()).equals(claims.getLongClaim("host"))
                    || !expected.purpose().name().equals(claims.getStringClaim("purpose"))
                    || !expected.route().name().equals(claims.getStringClaim("route")) || !validPassword(password)) {
                throw rejected();
            }
            return claims;
        } catch (final JOSEException | ParseException | RuntimeException _) {
            throw rejected();
        }
    }

    private static boolean validPassword(final String password) {
        return password != null && !password.isEmpty() && password.length() <= MAX_PASSWORD;
    }

    private static IOException rejected() {
        return new IOException("Mover credential envelope rejected");
    }
}
