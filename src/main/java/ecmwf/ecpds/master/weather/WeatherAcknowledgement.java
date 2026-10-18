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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** Partial receipts are permitted; omissions and RETRY_LATER retain the original queued event identity. */
public record WeatherAcknowledgement(int schemaVersion, UUID requestId, UUID masterEpoch, List<WeatherReceipt> receipts,
        List<UUID> probeReceipts) implements Serializable {

    private static final long serialVersionUID = 1L;

    public WeatherAcknowledgement(int schemaVersion, UUID requestId, UUID masterEpoch, List<WeatherReceipt> receipts) {
        this(schemaVersion, requestId, masterEpoch, receipts, List.of());
    }

    public WeatherAcknowledgement {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(masterEpoch, "masterEpoch");
        receipts = List.copyOf(receipts);
        probeReceipts = probeReceipts == null ? List.of() : List.copyOf(probeReceipts);
        final var identities = new HashSet<UUID>();
        if (schemaVersion != 1 || receipts.size() > WeatherBatch.MAX_EVENTS || probeReceipts.size() > 10
                || new HashSet<>(probeReceipts).size() != probeReceipts.size()
                || receipts.stream().anyMatch(receipt -> receipt.eventId() == null
                        || !masterEpoch.equals(receipt.masterEpoch()) || !identities.add(receipt.eventId()))) {
            throw new IllegalArgumentException("Invalid Network Weather acknowledgement");
        }
    }

    public void validateFor(WeatherBatch batch) {
        final var submitted = batch.deliveries().stream().map(delivery -> delivery.observation().eventId())
                .collect(Collectors.toSet());
        if (!requestId.equals(batch.requestId()) || !masterEpoch.equals(batch.masterEpoch())
                || receipts.stream().anyMatch(receipt -> !submitted.contains(receipt.eventId())) || probeReceipts
                        .stream().anyMatch(id -> batch.probeResults().stream().noneMatch(r -> r.jobId().equals(id)))) {
            throw new IllegalArgumentException("Network Weather acknowledgement does not match the submitted batch");
        }
    }
}
