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

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

/** Process-local key material; private keys are never exported through callbacks or administration snapshots. */
public final class WeatherSourceSigner {
    private final String sourceId;
    private final UUID epoch = UUID.randomUUID();
    private final KeyPair key;

    public WeatherSourceSigner(String sourceId) {
        WeatherCapabilities.validateSource(sourceId);
        this.sourceId = sourceId;
        try {
            key = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot initialize Network Weather source signing", e);
        }
    }

    public UUID epoch() {
        return epoch;
    }

    public WeatherBatch sign(WeatherBatch batch) throws java.io.IOException, GeneralSecurityException {
        if (!sourceId.equals(batch.sourceId()) || !epoch.equals(batch.sourceEpoch())) {
            throw new IllegalArgumentException("Network Weather batch does not belong to this source process");
        }
        final var signature = Signature.getInstance("Ed25519");
        signature.initSign(key.getPrivate());
        final long now = System.currentTimeMillis();
        final var unsigned = new WeatherBatch(batch.schemaVersion(), batch.requestId(), batch.masterEpoch(),
                batch.sourceId(), batch.deliveries(), "", now, batch.probeResults());
        signature.update(unsigned.signingDocument());
        final var signed = new WeatherBatch(batch.schemaVersion(), batch.requestId(), batch.masterEpoch(),
                batch.sourceId(), batch.deliveries(), Base64.getEncoder().encodeToString(signature.sign()), now,
                batch.probeResults());
        signed.encode();
        return signed;
    }

    public WeatherSourceIdentity identify(UUID challenge) throws GeneralSecurityException {
        if (challenge == null) {
            throw new IllegalArgumentException("Missing Network Weather identity challenge");
        }
        final long now = System.currentTimeMillis();
        final var publicKey = Base64.getEncoder().encodeToString(key.getPublic().getEncoded());
        final var signature = Signature.getInstance("Ed25519");
        signature.initSign(key.getPrivate());
        signature.update(WeatherSourceIdentity.document(sourceId, epoch, challenge, now, publicKey));
        return new WeatherSourceIdentity(1, sourceId, epoch, challenge, now, publicKey,
                Base64.getEncoder().encodeToString(signature.sign()));
    }
}
