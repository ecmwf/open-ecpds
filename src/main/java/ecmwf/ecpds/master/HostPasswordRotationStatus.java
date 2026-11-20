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

import java.io.Serializable;
import java.util.UUID;

import ecmwf.common.database.HostPasswordRotationStore.State;
import ecmwf.common.ectrans.CredentialProbe.Result;

public record HostPasswordRotationStatus(UUID reference, State state, Result probe, UUID activeReference,
        ecmwf.common.database.HostPasswordRotationStore.LegacyDrainProgress drain, boolean activationEnabled,
        Long retirementGraceSeconds) implements Serializable {

    public HostPasswordRotationStatus(final UUID reference, final State state, final Result probe) {
        this(reference, state, probe, null, null, false, null);
    }

    public enum Operation {
        STATUS, STAGE, PROBE, DISCARD, RECONCILE, DRAIN
    }
}
