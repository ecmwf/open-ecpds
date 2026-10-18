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
import java.util.UUID;

public record WeatherReceipt(UUID eventId, UUID masterEpoch, long receivedAt, Disposition disposition)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public WeatherReceipt {
        if (masterEpoch == null || receivedAt < 0 || disposition == null || eventId == null
                && disposition != Disposition.REJECTED_INVALID && disposition != Disposition.EXPIRED) {
            throw new IllegalArgumentException("Invalid observation acknowledgement");
        }
    }

    public enum Disposition {
        ACCEPTED, DUPLICATE, RETRY_LATER, REJECTED_INVALID, REJECTED_UNSUPPORTED, REJECTED_OUT_OF_SCOPE, EXPIRED
    }
}
