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
import java.util.Objects;

/**
 * Short-lived Master control advertisement. Reading this document does not authenticate an observation's origin or
 * authorize probes.
 */
public record WeatherCapabilities(int schemaVersion, String sourceId, long issuedAt, long validForMs,
        WeatherSelection selection, boolean observationIngress, boolean socketPersistence, int maxBatchObservations,
        int maxBatchBytes, java.util.List<WeatherProbe.Job> probeJobs) implements Serializable {

    private static final long serialVersionUID = 1L;

    public WeatherCapabilities(int schemaVersion, String sourceId, long issuedAt, long validForMs,
            WeatherSelection selection, boolean observationIngress, boolean socketPersistence, int maxBatchObservations,
            int maxBatchBytes) {
        this(schemaVersion, sourceId, issuedAt, validForMs, selection, observationIngress, socketPersistence,
                maxBatchObservations, maxBatchBytes, java.util.List.of());
    }

    public WeatherCapabilities {
        validateSource(sourceId);
        Objects.requireNonNull(selection, "selection");
        probeJobs = probeJobs == null ? java.util.List.of() : java.util.List.copyOf(probeJobs);
        if (probeJobs.size() > 1 || probeJobs.stream().anyMatch(j -> !sourceId.equals(j.policy().path().sourceId())
                || !selection.masterEpoch().equals(j.masterEpoch()) || !selection.paths().contains(j.policy().path())))
            throw new IllegalArgumentException("Invalid source probe jobs");
        if (schemaVersion != 1 || issuedAt <= 0 || validForMs <= 0 || validForMs > 60_000 || maxBatchObservations <= 0
                || maxBatchObservations > 100 || maxBatchBytes <= 0 || maxBatchBytes > 256 * 1024
                || selection.enabled() && !observationIngress || socketPersistence && !observationIngress
                || selection.paths().stream().anyMatch(path -> !sourceId.equals(path.sourceId()))) {
            throw new IllegalArgumentException("Invalid Network Weather capabilities");
        }
    }

    public static void validateSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank() || sourceId.length() > 128
                || sourceId.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid Network Weather source");
        }
    }
}
