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
import java.util.UUID;

/** One bounded background delivery step. Missing receipts and transient failures preserve pending evidence. */
public final class WeatherDelivery {
    private WeatherDelivery() {
    }

    @FunctionalInterface
    public interface Transport {
        WeatherAcknowledgement send(WeatherBatch batch) throws Exception;
    }

    public static void deliver(WeatherNegotiation negotiation, WeatherOutbox outbox, Transport transport)
            throws Exception {
        deliver(negotiation, outbox, null, transport);
    }

    public static void deliver(WeatherNegotiation negotiation, WeatherOutbox outbox, WeatherSourceSigner signer,
            Transport transport) throws Exception {
        final var lease = negotiation.current();
        final var pending = outbox.batch();
        if (lease == null || !lease.capabilities().observationIngress() || !lease.capabilities().selection().enabled()
                || pending.isEmpty()) {
            return;
        }
        final var capabilities = lease.capabilities();
        final var selection = capabilities.selection();
        final var deliveries = pending.subList(0, Math.min(pending.size(), capabilities.maxBatchObservations()));
        final var requestId = UUID.randomUUID();
        for (final var delivery : deliveries) {
            if (delivery.observation().selectionRevision() != selection.revision()
                    || !selection.paths().contains(delivery.observation().path())) {
                throw new IOException("Pending Network Weather evidence no longer matches the selected scope");
            }
        }
        WeatherBatch batch;
        int count = deliveries.size();
        while (true) {
            batch = new WeatherBatch(1, requestId, selection.masterEpoch(), capabilities.sourceId(),
                    deliveries.subList(0, count));
            try {
                if (signer != null) {
                    batch = signer.sign(batch);
                }
                if (batch.encode().length <= capabilities.maxBatchBytes()) {
                    break;
                }
            } catch (final IOException e) {
                if (count == 1) {
                    throw e;
                }
            }
            if (count == 1) {
                throw new IOException("Network Weather observation exceeds the negotiated batch limit");
            }
            // Halving limits encoding work to a geometric series, rather than re-encoding each growing prefix.
            count /= 2;
        }
        final var acknowledgement = transport.send(batch);
        if (acknowledgement == null) {
            throw new IOException("Missing Network Weather acknowledgement");
        }
        acknowledgement.validateFor(batch);
        final var current = negotiation.current();
        if (current == null || !current.capabilities().selection().equals(selection)
                || !current.capabilities().observationIngress()) {
            throw new IOException("Network Weather control changed during delivery");
        }
        outbox.acknowledge(batch, acknowledgement);
        if (acknowledgement.receipts().stream()
                .anyMatch(receipt -> receipt.disposition() == WeatherReceipt.Disposition.REJECTED_UNSUPPORTED)) {
            throw new WeatherUnsupportedException("Master rejected Network Weather observation ingress as unsupported");
        }
    }
}
