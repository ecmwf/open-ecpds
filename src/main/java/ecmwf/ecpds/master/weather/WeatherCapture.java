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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketStatisticsParser;
import ecmwf.ecpds.master.weather.WeatherObservation.*;

/**
 * Optional source evidence. Execution headers are frozen before preparation/submission and never retain a mutable
 * transfer, Host, module or Socket. Delivery callbacks perform no transport or database work.
 */
public final class WeatherCapture {
    private static final Logger LOG = LogManager.getLogger(WeatherCapture.class);
    private final String sourceId;
    private final UUID sourceEpoch;
    private final WeatherNegotiation negotiation;
    private final WeatherOutbox outbox;
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong omittedSockets = new AtomicLong();
    private final AtomicLong invalidSockets = new AtomicLong();
    private final AtomicLong contention = new AtomicLong();

    public WeatherCapture(String sourceId, WeatherNegotiation negotiation, WeatherOutbox outbox) {
        this(sourceId, negotiation, outbox, UUID.randomUUID());
    }

    public WeatherCapture(String sourceId, WeatherNegotiation negotiation, WeatherOutbox outbox, UUID sourceEpoch) {
        WeatherCapabilities.validateSource(sourceId);
        this.sourceId = sourceId;
        this.sourceEpoch = java.util.Objects.requireNonNull(sourceEpoch);
        this.negotiation = java.util.Objects.requireNonNull(negotiation);
        this.outbox = java.util.Objects.requireNonNull(outbox);
    }

    public Execution start(long transferId, String destinationId, String hostId, String target, String protocol) {
        final var lease = negotiation.current();
        if (lease == null || !lease.capabilities().observationIngress()
                || !lease.capabilities().selection().enabled()) {
            return null;
        }
        final var selection = lease.capabilities().selection();
        final var path = new Path(sourceId, hostId, target, protocol, "DISSEMINATION");
        if (!selection.paths().contains(path)) {
            return null;
        }
        final var header = new WeatherObservation(1, UUID.randomUUID(), UUID.randomUUID(), sourceEpoch, sourceId,
                transferId, destinationId, hostId, target, protocol, "DISSEMINATION", selection.revision(),
                Kind.SENDING_TERMINAL, Outcome.UNKNOWN, Category.UNCLASSIFIED, System.currentTimeMillis(), List.of());
        return new Execution(selection.masterEpoch(), header);
    }

    public Status status() {
        return new Status(rejected.get(), omittedSockets.get(), invalidSockets.get(), contention.get(),
                outbox.status());
    }

    public record Status(long rejected, long omittedSockets, long invalidSockets, long contention,
            WeatherOutbox.Status outbox) {
    }

    public final class Execution {
        private final UUID masterEpoch;
        private final WeatherObservation header;
        private final ReentrantLock lock = new ReentrantLock();
        private final Map<UUID, Connection> connections = new HashMap<>();
        private WeatherObservation terminal;
        private boolean terminalQueued;

        private Execution(UUID masterEpoch, WeatherObservation header) {
            this.masterEpoch = masterEpoch;
            this.header = header;
        }

        public UUID executionId() {
            return header.executionId();
        }

        public void finish(Outcome outcome, Category category, ClientSocketStatistics.Snapshot snapshot) {
            record(outcome, category, snapshot, true);
        }

        public void supplement(ClientSocketStatistics.Snapshot snapshot) {
            record(Outcome.UNKNOWN, Category.UNCLASSIFIED, snapshot, false);
        }

        private void record(Outcome outcome, Category category, ClientSocketStatistics.Snapshot snapshot,
                boolean allowTerminal) {
            if (!lock.tryLock()) {
                contention.incrementAndGet();
                return;
            }
            try {
                if (!allowTerminal && terminal == null) {
                    return;
                }
                final var lease = negotiation.current();
                if (lease == null || !lease.capabilities().observationIngress()
                        || !lease.capabilities().selection().enabled()
                        || !masterEpoch.equals(lease.capabilities().selection().masterEpoch())
                        || header.selectionRevision() != lease.capabilities().selection().revision()
                        || !lease.capabilities().selection().paths().contains(header.path())) {
                    rejected.incrementAndGet();
                    return;
                }
                final var additions = copyConnections(snapshot);
                if (terminal == null) {
                    terminal = event(header.eventId(), Kind.SENDING_TERMINAL, outcome, category,
                            System.currentTimeMillis(), additions);
                    terminalQueued = outbox.offer(terminal);
                } else if (terminalQueued && !additions.isEmpty()) {
                    outbox.offer(event(UUID.randomUUID(), Kind.SOCKET_SUPPLEMENT, terminal.outcome(),
                            terminal.category(), terminal.occurredAt(), additions));
                }
            } finally {
                lock.unlock();
            }
        }

        private List<Connection> copyConnections(ClientSocketStatistics.Snapshot snapshot) {
            final var additions = new ArrayList<Connection>();
            if (snapshot == null) {
                return additions;
            }
            omittedSockets.addAndGet(snapshot.omitted());
            for (final var socket : snapshot.connections()) {
                if (connections.containsKey(socket.connectionId())) {
                    continue;
                }
                if (connections.size() == WeatherObservation.MAX_CONNECTIONS) {
                    omittedSockets.incrementAndGet();
                    continue;
                }
                try {
                    if (socket.statistics() == null || socket.statistics().length() > 8192
                            || socket.statistics().indexOf('|') >= 0) {
                        throw new IllegalArgumentException("Invalid socket snapshot text");
                    }
                    final var parsed = SocketStatisticsParser.parse(header.transferId(),
                            "start:" + socket.startTime() + " end:" + socket.endTime() + " " + socket.statistics());
                    if (parsed.size() != 1) {
                        throw new IllegalArgumentException("Missing socket snapshot measurements");
                    }
                    final var stats = parsed.get(0);
                    final var connection = new Connection(socket.connectionId(), stats.getLocalAddress(),
                            stats.getRemoteAddress(), socket.startTime(), socket.endTime(), stats.getRttMs(),
                            stats.getBytesSent(), stats.getBytesReceived(), stats.getPacingRateBps(),
                            stats.getDeliveryRateBps(), stats.getCwnd(), stats.getSegsOut(), stats.getSegsIn());
                    connections.put(connection.connectionId(), connection);
                    additions.add(connection);
                } catch (final IllegalArgumentException e) {
                    invalidSockets.incrementAndGet();
                    LOG.warn("Network Weather rejected socket snapshot for transfer {}", header.transferId(), e);
                }
            }
            return additions;
        }

        private WeatherObservation event(UUID eventId, Kind kind, Outcome outcome, Category category, long time,
                List<Connection> sockets) {
            return new WeatherObservation(1, eventId, header.executionId(), header.sourceEpoch(), header.sourceId(),
                    header.transferId(), header.destinationId(), header.hostId(), header.configuredTarget(),
                    header.protocol(), header.leg(), header.selectionRevision(), kind, outcome, category, time,
                    sockets);
        }
    }
}
