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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

/** Challenge-bound public identity obtained only through a subscribe-authenticated Mover callback. */
public record WeatherSourceIdentity(int schemaVersion, String sourceId, UUID sourceEpoch, UUID challenge, long issuedAt,
        String publicKey, String signature, int collectionVersion) implements Serializable {

    private static final long serialVersionUID = 1L;

    public WeatherSourceIdentity(int schemaVersion, String sourceId, UUID sourceEpoch, UUID challenge, long issuedAt,
            String publicKey, String signature) {
        this(schemaVersion, sourceId, sourceEpoch, challenge, issuedAt, publicKey, signature, 2);
    }

    public static final class UnsupportedIdentityException extends GeneralSecurityException {
        private static final long serialVersionUID = 1L;

        private UnsupportedIdentityException() {
            super("Source does not support signed collection timing and per-execution socket-writer handover");
        }
    }

    public WeatherSourceIdentity {
        WeatherCapabilities.validateSource(sourceId);
        if (schemaVersion != 1 || sourceEpoch == null || challenge == null || issuedAt <= 0 || publicKey == null
                || publicKey.length() != 60 || signature == null || signature.length() != 88 || collectionVersion < 0) {
            throw new IllegalArgumentException("Invalid Network Weather source identity");
        }
    }

    public PublicKey verify(String expectedSource, UUID expectedChallenge) throws GeneralSecurityException {
        if (collectionVersion != 2) {
            throw new UnsupportedIdentityException();
        }
        if (!sourceId.equals(expectedSource) || !challenge.equals(expectedChallenge)) {
            throw new GeneralSecurityException("Mismatched Network Weather source challenge");
        }
        final byte[] encodedKey;
        final byte[] proof;
        try {
            encodedKey = Base64.getDecoder().decode(publicKey);
            proof = Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid Network Weather identity encoding", e);
        }
        if (encodedKey.length != 44 || proof.length != 64) {
            throw new GeneralSecurityException("Invalid Network Weather Ed25519 identity size");
        }
        final var key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(encodedKey));
        final var verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(key);
        verifier.update(document(sourceId, sourceEpoch, challenge, issuedAt, publicKey));
        if (!verifier.verify(proof)) {
            throw new GeneralSecurityException("Invalid Network Weather identity proof");
        }
        return key;
    }

    static byte[] document(String source, UUID epoch, UUID challenge, long issuedAt, String key) {
        return ("ECPDS-WEATHER-SOURCE-1\n" + source + "\n" + epoch + "\n" + challenge + "\n" + issuedAt + "\n" + key)
                .concat("\ncollection=2").getBytes(StandardCharsets.UTF_8);
    }
}
