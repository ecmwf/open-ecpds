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
import java.util.List;

/** Deliberately excludes raw diagnostics, transfer IDs, endpoint addresses and administrative errors. */
public record WeatherViewer(boolean enabled, long measuredAt, List<Path> paths) implements Serializable {

    public WeatherViewer {
        paths = List.copyOf(paths);
    }

    public record Path(String source, String hostId, String protocol, long successes, long failures, long cancellations,
            long unknown, long sockets, long lastObserved, Double rttMs, Double latitude, Double longitude,
            String probeMode, String probeOutcome, long probedAt, Double probeLatencyMs, String leg)
            implements Serializable {
        public Path(String source, String hostId, String protocol, long successes, long failures, long cancellations,
                long unknown, long sockets, long lastObserved, Double rttMs, Double latitude, Double longitude,
                String leg) {
            this(source, hostId, protocol, successes, failures, cancellations, unknown, sockets, lastObserved, rttMs,
                    latitude, longitude, null, null, 0, null, leg);
        }
    }
}
