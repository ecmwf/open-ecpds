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

import java.util.Objects;
import java.util.UUID;

/**
 * Validated input to the future transactional socket writer. This is deliberately not registered with Hibernate until
 * schema capability has been checked.
 */
public record WeatherSending(int sendingIndex, WeatherObservation terminal) {

    public WeatherSending {
        Objects.requireNonNull(terminal, "terminal");
        if (sendingIndex < 0 || terminal.kind() != WeatherObservation.Kind.SENDING_TERMINAL) {
            throw new IllegalArgumentException(
                    "A sending header requires a terminal observation and nonnegative index");
        }
    }

    public UUID executionId() {
        return terminal.executionId();
    }

    public record ConnectionIdentity(UUID executionId, UUID connectionId, long statisticsId) {
        public ConnectionIdentity {
            Objects.requireNonNull(executionId, "executionId");
            Objects.requireNonNull(connectionId, "connectionId");
            if (statisticsId <= 0) {
                throw new IllegalArgumentException("A connection identity must reference a persisted socket record");
            }
        }
    }
}
