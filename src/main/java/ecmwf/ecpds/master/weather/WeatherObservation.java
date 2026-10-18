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
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable source evidence. A sending can contain several connection attempts; its terminal outcome is not an outcome
 * for each socket.
 */
public record WeatherObservation(int schemaVersion, UUID eventId, UUID executionId, UUID sourceEpoch, String sourceId,
        long transferId, String destinationId, String hostId, String configuredTarget, String protocol, String leg,
        long selectionRevision, Kind kind, Outcome outcome, Category category, long occurredAt,
        List<Connection> connections) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SCHEMA_VERSION = 1;
    public static final int MAX_CONNECTIONS = 16;
    public static final int MAX_ENCODED_BYTES = 64 * 1024;

    public enum Kind {
        SENDING_TERMINAL, SOCKET_SUPPLEMENT, DISPATCH_DIAGNOSTIC
    }

    public enum Outcome {
        SUCCESS, FAILURE, CANCELLED, UNKNOWN
    }

    public enum Category {
        CONNECTIVITY, SERVICE_AUTH, REMOTE_STORAGE_QUOTA, LOCAL_SOURCE, CONTROL_DISPATCH, CANCELLED, UNCLASSIFIED
    }

    public record Connection(UUID connectionId, String localAddress, String remoteAddress, long startTime, long endTime,
            Double rttMs, Long bytesSent, Long bytesReceived, Long pacingRateBps, Long deliveryRateBps, Integer cwnd,
            Integer segsOut, Integer segsIn) implements Serializable {

        private static final long serialVersionUID = 1L;

        public Connection(UUID connectionId, String localAddress, String remoteAddress, long startTime, long endTime,
                Double rttMs, Long bytesSent, Long bytesReceived) {
            this(connectionId, localAddress, remoteAddress, startTime, endTime, rttMs, bytesSent, bytesReceived, null,
                    null, null, null, null);
        }

        public Connection {
            Objects.requireNonNull(connectionId, "connectionId");
            if (localAddress != null) {
                text(localAddress, "localAddress", 128);
            }
            if (remoteAddress != null) {
                text(remoteAddress, "remoteAddress", 128);
            }
            if (startTime <= 0 || endTime < startTime || rttMs != null && (!Double.isFinite(rttMs) || rttMs < 0)
                    || bytesSent != null && bytesSent < 0 || bytesReceived != null && bytesReceived < 0
                    || pacingRateBps != null && pacingRateBps < 0 || deliveryRateBps != null && deliveryRateBps < 0
                    || cwnd != null && cwnd < 0 || segsOut != null && segsOut < 0 || segsIn != null && segsIn < 0) {
                throw new IllegalArgumentException("Invalid connection measurement");
            }
        }
    }

    public WeatherObservation {
        if (schemaVersion != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported observation schema");
        }
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(executionId, "executionId");
        Objects.requireNonNull(sourceEpoch, "sourceEpoch");
        text(sourceId, "sourceId", 128);
        text(destinationId, "destinationId", 32);
        text(hostId, "hostId", 32);
        text(configuredTarget, "configuredTarget", 255);
        text(protocol, "protocol", 128);
        text(leg, "leg", 32);
        if (transferId <= 0 || occurredAt <= 0 || selectionRevision <= 0) {
            throw new IllegalArgumentException("Invalid transfer, time or selection revision");
        }
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(category, "category");
        connections = List.copyOf(connections);
        if (connections.size() > MAX_CONNECTIONS
                || connections.stream().map(Connection::connectionId).distinct().count() != connections.size()) {
            throw new IllegalArgumentException("Too many or duplicate connections");
        }
        if (kind == Kind.SOCKET_SUPPLEMENT && connections.isEmpty() || kind == Kind.DISPATCH_DIAGNOSTIC
                && (outcome != Outcome.UNKNOWN || category != Category.CONTROL_DISPATCH || !connections.isEmpty())
                || outcome == Outcome.CANCELLED && category != Category.CANCELLED
                || category == Category.CANCELLED && outcome != Outcome.CANCELLED) {
            throw new IllegalArgumentException("Inconsistent observation kind, outcome or category");
        }
    }

    public Path path() {
        return new Path(sourceId, hostId, configuredTarget, protocol, leg);
    }

    public record Path(String sourceId, String hostId, String configuredTarget, String protocol, String leg)
            implements Serializable {

        private static final long serialVersionUID = 1L;

        public Path {
            text(sourceId, "sourceId", 128);
            text(hostId, "hostId", 32);
            text(configuredTarget, "configuredTarget", 255);
            text(protocol, "protocol", 128);
            text(leg, "leg", 32);
        }
    }

    private static void text(String value, String name, int maximum) {
        if (value == null || value.isBlank() || value.length() > maximum
                || value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }
}
