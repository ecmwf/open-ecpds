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

package ecmwf.ecpds.master;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import ecmwf.common.ectrans.CredentialProbe.Endpoint;
import ecmwf.common.ectrans.CredentialProbe.Result;
import ecmwf.common.rmi.RmiStubTransport;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.mover.MoverPasswordEnvelope;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Assignment;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Purpose;
import ecmwf.ecpds.mover.MoverPasswordEnvelope.Route;

final class HostPasswordProbeDispatch {
    record Keys(RSAPublicKey recipient, RSAPrivateKey signing) {
    }

    private HostPasswordProbeDispatch() {
    }

    static Result probe(final MoverInterface relay, final String recipient, final long proxyHost, final long host,
            final Endpoint endpoint, final String password) throws IOException {
        if (!Cnf.at("PasswordProbe", "dispatchEnabled", false) || !Cnf.at("CallBack", "requireMutualTls", false)
                || RmiStubTransport.inspect(relay) != RmiStubTransport.Kind.MUTUAL_TLS_FACTORY || host <= 0
                || proxyHost < 0 || recipient == null || recipient.isBlank()) {
            throw new IOException("Password probe dispatch unavailable");
        }
        try {
            final var keys = keys(recipient);
            final var nonce = proxyHost == 0 ? relay.beginPasswordProbe(host)
                    : relay.beginContinentalPasswordProbe(proxyHost, host);
            final var route = proxyHost == 0 ? Route.DIRECT_RMI : Route.CONTINENTAL_HTTPS;
            try (final var sealed = MoverPasswordEnvelope.sealProbe(
                    new Assignment(Purpose.PASSWORD_PROBE, route, recipient, host, nonce), endpoint, password,
                    keys.recipient(), keys.signing())) {
                final var receipt = proxyHost == 0 ? relay.runPasswordProbe(host, nonce, sealed.ciphertext())
                        : relay.runContinentalPasswordProbe(proxyHost, host, nonce, sealed.ciphertext());
                return sealed.verify(receipt);
            }
        } catch (final GeneralSecurityException | IOException | RuntimeException _) {
            throw new IOException("Password probe dispatch or result verification failed");
        }
    }

    static Keys keys(final String recipient) throws GeneralSecurityException, IOException {
        final var factory = KeyFactory.getInstance("RSA");
        final var recipientKey = (RSAPublicKey) factory
                .generatePublic(new X509EncodedKeySpec(readKey(Cnf.at("PasswordProbeMoverKeys", recipient, ""))));
        final var signingBytes = readKey(Cnf.at("PasswordProbe", "masterSigningPrivateKey", ""));
        final RSAPrivateKey signingKey;
        try {
            signingKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(signingBytes));
        } finally {
            Arrays.fill(signingBytes, (byte) 0);
        }
        if (signingKey.getModulus().equals(recipientKey.getModulus())) {
            throw new IOException("Credential handoff requires independent signing and delivery keys");
        }
        return new Keys(recipientKey, signingKey);
    }

    private static byte[] readKey(final String configured) throws IOException {
        final var path = Path.of(configured);
        final int limit = 16_384;
        if (!path.isAbsolute() || !Files.isRegularFile(path)) {
            throw new IOException("Invalid password probe key file");
        }
        try (final var input = Files.newInputStream(path)) {
            final var bytes = input.readNBytes(limit + 1);
            if (bytes.length == 0 || bytes.length > limit) {
                Arrays.fill(bytes, (byte) 0);
                throw new IOException("Invalid password probe key file");
            }
            return bytes;
        }
    }
}
