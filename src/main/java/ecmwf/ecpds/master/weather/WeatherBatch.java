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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Wire envelope shared by direct and relayed transport. Queue age is source evidence, not trusted timing. */
public record WeatherBatch(int schemaVersion, UUID requestId, UUID masterEpoch, String sourceId,
        List<WeatherOutbox.Delivery> deliveries, String signature, long sentAt, List<WeatherProbe.Result> probeResults)
        implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int MAX_BYTES = 256 * 1024;
    public static final int MAX_EVENTS = 100;
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public WeatherBatch(int schemaVersion, UUID requestId, UUID masterEpoch, String sourceId,
            List<WeatherOutbox.Delivery> deliveries, String signature, long sentAt) {
        this(schemaVersion, requestId, masterEpoch, sourceId, deliveries, signature, sentAt, List.of());
    }

    public UUID sourceEpoch() {
        return deliveries.isEmpty() ? probeResults.getFirst().sourceEpoch()
                : deliveries.getFirst().observation().sourceEpoch();
    }

    public WeatherBatch(int schemaVersion, UUID requestId, UUID masterEpoch, String sourceId,
            List<WeatherOutbox.Delivery> deliveries) {
        this(schemaVersion, requestId, masterEpoch, sourceId, deliveries, "", 0);
    }

    public WeatherBatch(int schemaVersion, UUID requestId, UUID masterEpoch, String sourceId,
            List<WeatherOutbox.Delivery> deliveries, String signature) {
        this(schemaVersion, requestId, masterEpoch, sourceId, deliveries, signature, 0);
    }

    public WeatherBatch {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(masterEpoch, "masterEpoch");
        WeatherCapabilities.validateSource(sourceId);
        deliveries = List.copyOf(deliveries);
        probeResults = probeResults == null ? List.of() : List.copyOf(probeResults);
        if (schemaVersion != 1 || deliveries.isEmpty() && probeResults.isEmpty() || deliveries.size() > MAX_EVENTS
                || probeResults.size() > 10 || signature == null || !signature.isEmpty() && signature.length() != 88
                || sentAt < 0) {
            throw new IllegalArgumentException("Invalid Network Weather batch");
        }
        final var identities = new HashSet<UUID>();
        final var epoch = deliveries.isEmpty() ? probeResults.getFirst().sourceEpoch()
                : deliveries.getFirst().observation().sourceEpoch();
        final var revision = deliveries.isEmpty() ? 0 : deliveries.getFirst().observation().selectionRevision();
        if (probeResults.stream().anyMatch(r -> !r.sourceId().equals(sourceId) || !r.sourceEpoch().equals(epoch))
                || probeResults.stream().map(WeatherProbe.Result::jobId).distinct().count() != probeResults.size())
            throw new IllegalArgumentException("Inconsistent probe result identities");
        for (final var delivery : deliveries) {
            final var observation = delivery.observation();
            if (!sourceId.equals(observation.sourceId()) || delivery.queueAgeMs() < 0 || delivery.queueAgeMs() > 300_000
                    || !identities.add(observation.eventId()) || !epoch.equals(observation.sourceEpoch())
                    || revision != observation.selectionRevision()) {
                throw new IllegalArgumentException("Inconsistent Network Weather batch source, revision or identity");
            }
        }
    }

    /** Sign the reconstructed typed document, never a relay's JSON whitespace or property order. */
    byte[] signingDocument() throws IOException {
        final var unsigned = new WeatherBatch(schemaVersion, requestId, masterEpoch, sourceId, deliveries, "", sentAt,
                probeResults);
        final var document = unsigned.encode();
        final var prefix = "ECPDS-WEATHER-BATCH-1\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        final var result = new byte[prefix.length + document.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(document, 0, result, prefix.length, document.length);
        return result;
    }

    public void verify(java.security.PublicKey key) throws IOException, java.security.GeneralSecurityException {
        if (signature.isEmpty()) {
            throw new java.security.GeneralSecurityException("Missing Network Weather batch signature");
        }
        final byte[] proof;
        try {
            proof = java.util.Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException e) {
            throw new java.security.GeneralSecurityException("Invalid Network Weather batch signature encoding", e);
        }
        if (proof.length != 64) {
            throw new java.security.GeneralSecurityException("Invalid Network Weather batch signature size");
        }
        final var verifier = java.security.Signature.getInstance("Ed25519");
        verifier.initVerify(key);
        verifier.update(signingDocument());
        if (!verifier.verify(proof)) {
            throw new java.security.GeneralSecurityException("Invalid Network Weather batch signature");
        }
    }

    public byte[] encode() throws IOException {
        final var bytes = JSON.writeValueAsBytes(this);
        if (bytes.length > MAX_BYTES) {
            throw new IOException("Network Weather batch exceeds 256 KiB");
        }
        for (final var delivery : deliveries) {
            if (JSON.writeValueAsBytes(delivery.observation()).length > WeatherObservation.MAX_ENCODED_BYTES) {
                throw new IOException("Network Weather observation exceeds 64 KiB");
            }
        }
        try (final var output = new java.io.ObjectOutputStream(new java.io.OutputStream() {
            private int count;

            @Override
            public void write(int value) throws IOException {
                reserve(1);
            }

            @Override
            public void write(byte[] value, int offset, int length) throws IOException {
                reserve(length);
            }

            private void reserve(int length) throws IOException {
                if (length > MAX_BYTES - count) {
                    throw new IOException("Network Weather RMI batch exceeds 256 KiB");
                }
                count += length;
            }
        })) {
            output.writeObject(this);
        }
        return bytes;
    }

    public static WeatherBatch decode(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IOException("Missing or oversized Network Weather batch");
        }
        final var batch = JSON.readValue(bytes, WeatherBatch.class);
        if (batch == null) {
            throw new IOException("Missing Network Weather batch document");
        }
        batch.encode();
        return batch;
    }
}
