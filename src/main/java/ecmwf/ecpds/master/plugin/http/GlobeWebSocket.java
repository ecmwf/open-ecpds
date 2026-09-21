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

package ecmwf.ecpds.master.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-12
 */

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee8.websocket.api.Session;
import org.eclipse.jetty.ee8.websocket.api.WebSocketListener;
import org.eclipse.jetty.ee8.websocket.api.WriteCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.GeoPoint;
import ecmwf.ecpds.master.LiveTransferSample;
import ecmwf.ecpds.master.ManagementInterface;
import ecmwf.ecpds.master.MasterManager;

/**
 * WebSocket endpoint streaming live data-transfer events to the "Live ECPDS Earth" globe visualisation.
 * <p>
 * The Monitor plugin (where this class runs) is a separate JVM/process from the MasterServer, connected only over RMI
 * (see {@link MasterManager}), so it cannot read the MasterServer-side {@link ecmwf.ecpds.master.LiveTransferRegistry}
 * singleton directly - a shared, JVM-wide poller (started lazily on the first connection, stopped once the last one
 * closes) periodically calls {@link ecmwf.ecpds.master.ManagementInterface#getLiveTransfers()} and
 * {@link ecmwf.ecpds.master.ManagementInterface#getLiveTransferOrigin()} over RMI and fans the resulting snapshot out
 * to every currently connected client as a "snapshot" message (which the frontend uses to fully replace its current
 * transfer set, so no client-side diffing is needed). Polling only happens while at least one globe page is open, which
 * is also what keeps DataMovers sampling (see {@link ecmwf.ecpds.master.LiveTransferRegistry#touch()}). Geolocation of
 * Host/Mover names is likewise always resolved on the MasterServer side, via
 * {@link ecmwf.ecpds.master.ManagementInterface#getGeoLocations(String[])} - the Monitor JVM never needs its own copy
 * of the GeoIP2 database.
 * </p>
 * <p>
 * Implements the (legacy/"ee8") {@link WebSocketListener} interface directly rather than relying on the
 * annotation-based API, since the endpoint is registered through
 * {@link org.eclipse.jetty.ee8.websocket.server.config.JettyWebSocketServletContainerInitializer}, whose frame-handler
 * factory only recognises {@code org.eclipse.jetty.ee8.websocket.api.annotations.*} annotations (not the similarly
 * named ones in {@code org.eclipse.jetty.websocket.api.annotations}), so implementing the interface avoids that
 * ambiguity.
 * </p>
 */
public class GlobeWebSocket implements WebSocketListener {

    private static final Logger LOG = LogManager.getLogger(GlobeWebSocket.class);

    /** JSON mapper for outgoing messages. */
    private static final ObjectMapper JSON = new ObjectMapper();

    /** How often the shared poller pulls a fresh snapshot from the MasterServer. */
    private static final long POLL_PERIOD_SECONDS = 3;

    /** Every currently connected client, shared by the poller for broadcast. */
    private static final Set<GlobeWebSocket> CLIENTS = ConcurrentHashMap.newKeySet();

    /** Shared thread pool: one thread for the RMI poller, one for per-connection WebSocket pings. */
    private static final ScheduledThreadPoolExecutor POOL = new ScheduledThreadPoolExecutor(2, r -> {
        final var t = new Thread(r, "globe-worker");
        t.setDaemon(true);
        return t;
    });

    /** The shared poll task, running only while at least one client is connected. */
    private static volatile ScheduledFuture<?> pollTask;

    /** Host name to resolved {@link GeoPoint} cache, shared across connections. */
    private static final ConcurrentHashMap<String, GeoPoint> GEO_CACHE = new ConcurrentHashMap<>();

    /** Latest known MasterServer origin location, refreshed by the poller; {@code null} until first resolved. */
    private static volatile double[] originLocation;

    /** Latest known MasterServer hostname/IP used to resolve {@link #originLocation}, refreshed by the poller. */
    private static volatile String originHost;

    /** Latest known rolling-24h transferred-bytes total (Dissemination + Acquisition), refreshed by the poller. */
    private static volatile long bytesLast24h;

    /** Latest known rolling-24h transferred-bytes total for Dissemination only, refreshed by the poller. */
    private static volatile long bytesLast24hDissemination;

    /** Latest known rolling-24h transferred-bytes total for Acquisition only, refreshed by the poller. */
    private static volatile long bytesLast24hAcquisition;

    /** Latest known number of currently open Data Portal (incoming) connections, refreshed by the poller. */
    private static volatile long dataPortalSessions;

    /** Latest known Data Portal bytes-in/sec rate (uploads by IncomingUsers), refreshed by the poller. */
    private static volatile long dataPortalBytesInPerSecond;

    /** Latest known Data Portal bytes-out/sec rate (downloads by IncomingUsers), refreshed by the poller. */
    private static volatile long dataPortalBytesOutPerSecond;

    /** Latest known total used/total bytes across every volume of every DataMover, refreshed by the poller. */
    private static volatile long moverStorageUsedBytes;

    /** Latest known total capacity in bytes across every volume of every DataMover, refreshed by the poller. */
    private static volatile long moverStorageTotalBytes;

    /**
     * Names of every currently active ProxyHost (a Data Mover reachable only through another Data Mover's REST
     * interface, without a direct RMI connection to the MasterServer), refreshed by the poller. Used to decide which
     * movers get their own marker on the globe: ordinary, directly-connected Data Movers are intentionally not shown,
     * only ProxyHosts, since they are the ones physically located elsewhere.
     */
    private static volatile Set<String> activeProxyHostNames = Set.of();

    /** How many poll cycles between refreshes of the (cheaper-to-be-conservative-with) mover disk usage snapshot. */
    private static final int STORAGE_POLL_EVERY_N_CYCLES = 5;

    /** Poll cycle counter, used to throttle {@link ManagementInterface#getMoverVolumeUsage(String)} calls. */
    private static volatile long pollCycle;

    static {
        POOL.setRemoveOnCancelPolicy(true);
    }

    /** Jetty WebSocket session for this client. */
    private Session session;

    /** Scheduled task for periodic WebSocket ping. */
    private ScheduledFuture<?> wsPingTask;

    /**
     * Called when the WebSocket is connected. Registers this connection for broadcast, starts the shared poller if it
     * is not already running, and sends an initial "hello" (origin location) and "snapshot" (currently active
     * transfers) message.
     *
     * @param session
     *            the connected WebSocket session
     */
    @Override
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        session.getPolicy().setIdleTimeout(Duration.ofMinutes(2));
        GlobeImageryProvisioner.ensureStarted();
        GlobeLabelsProvisioner.ensureStarted();
        wsPingTask = POOL.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendPing(ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 }), WriteCallback.NOOP);
                } catch (final Exception e) {
                    LOG.debug("WS ping failed: {}", e.toString());
                }
            }
        }, 20, 20, TimeUnit.SECONDS);
        CLIENTS.add(this);
        ensurePolling();
        final var samples = pollNow();
        sendHello();
        sendSnapshot(samples);
    }

    /**
     * Called when a message is received from the client. No client-initiated commands are currently supported; this is
     * a read-only stream, so any incoming message is simply ignored.
     *
     * @param message
     *            the raw message from the client
     */
    @Override
    public void onWebSocketText(final String message) {
        // No client-to-server commands supported yet.
    }

    /**
     * Called when the WebSocket is closed. Unregisters this connection from the broadcast set, and stops the shared
     * poller if it was the last one.
     *
     * @param statusCode
     *            the close status code
     * @param reason
     *            the reason for closure
     */
    @Override
    public void onWebSocketClose(final int statusCode, final String reason) {
        disconnect();
        LOG.debug("Closed: {} - {}", statusCode, reason != null ? reason : "none");
    }

    /**
     * Called on WebSocket error. Unregisters this connection from the broadcast set.
     *
     * @param error
     *            the thrown error
     */
    @Override
    public void onWebSocketError(final Throwable error) {
        disconnect();
        LOG.warn("WebSocket error", error);
    }

    /**
     * Removes this connection from the broadcast set, cancels its ping task, and stops the shared poller if no client
     * remains.
     */
    private void disconnect() {
        CLIENTS.remove(this);
        if (wsPingTask != null) {
            wsPingTask.cancel(true);
        }
        if (CLIENTS.isEmpty()) {
            final var task = pollTask;
            if (task != null) {
                task.cancel(false);
                pollTask = null;
            }
        }
    }

    /**
     * Starts the shared poll task if it is not already running.
     */
    private static synchronized void ensurePolling() {
        if (pollTask == null || pollTask.isCancelled()) {
            pollTask = POOL.scheduleAtFixedRate(GlobeWebSocket::pollAndBroadcast, 0, POLL_PERIOD_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Polls the MasterServer once via RMI, and broadcasts the resulting snapshot to every connected client.
     */
    private static void pollAndBroadcast() {
        try {
            final var samples = pollNow();
            for (final GlobeWebSocket client : CLIENTS) {
                client.sendSnapshot(samples);
            }
        } catch (final Throwable t) {
            LOG.warn("Polling live transfers from MasterServer", t);
        }
    }

    /**
     * Performs a single RMI round-trip to the MasterServer to fetch the current live transfer samples, refreshing the
     * cached origin location along the way (best-effort, does not fail the whole poll if the origin call fails).
     *
     * @return the currently active samples (possibly empty), never {@code null}
     */
    private static LiveTransferSample[] pollNow() {
        try {
            final var mi = MasterManager.getMI();
            try {
                final var origin = mi.getLiveTransferOrigin();
                if (origin != null) {
                    originLocation = origin;
                }
            } catch (final Exception e) {
                LOG.debug("Fetching MasterServer origin location", e);
            }
            try {
                final var host = mi.getLiveTransferOriginHost();
                if (host != null) {
                    originHost = host;
                }
            } catch (final Exception e) {
                LOG.debug("Fetching MasterServer origin hostname/address", e);
            }
            try {
                bytesLast24h = mi.getLiveTransferBytes24h();
                bytesLast24hDissemination = mi.getLiveTransferBytes24h(LiveTransferSample.DIRECTION_DISSEMINATION);
                bytesLast24hAcquisition = mi.getLiveTransferBytes24h(LiveTransferSample.DIRECTION_ACQUISITION);
            } catch (final Exception e) {
                LOG.debug("Fetching MasterServer 24h transferred bytes total", e);
            }
            try {
                final var proxyHosts = mi.getActiveProxyHostNames();
                activeProxyHostNames = proxyHosts != null ? Set.of(proxyHosts) : Set.of();
            } catch (final Exception e) {
                LOG.debug("Fetching active ProxyHost names", e);
            }
            try {
                final var activity = mi.getDataPortalActivity();
                if (activity != null && activity.length == 3) {
                    dataPortalSessions = activity[0];
                    dataPortalBytesInPerSecond = activity[1];
                    dataPortalBytesOutPerSecond = activity[2];
                }
            } catch (final Exception e) {
                LOG.debug("Fetching Data Portal activity", e);
            }
            // The mover disk usage snapshot only changes slowly (it is itself a periodically refreshed cache on the
            // MasterServer side), so it is only refreshed every few poll cycles rather than on every 3s tick.
            if (pollCycle++ % STORAGE_POLL_EVERY_N_CYCLES == 0) {
                try {
                    final var usage = mi.getMoverVolumeUsage(null);
                    var usedTotal = 0L;
                    var capacityTotal = 0L;
                    if (usage != null) {
                        for (final long[][] vols : usage.values()) {
                            if (vols == null || vols.length != 2) {
                                continue;
                            }
                            for (final var used : vols[0]) {
                                usedTotal += used;
                            }
                            for (final var total : vols[1]) {
                                capacityTotal += total;
                            }
                        }
                    }
                    moverStorageUsedBytes = usedTotal;
                    moverStorageTotalBytes = capacityTotal;
                } catch (final Exception e) {
                    LOG.debug("Fetching mover disk usage", e);
                }
            }
            final var samples = mi.getLiveTransfers();
            resolveGeoLocations(mi, samples != null ? samples : new LiveTransferSample[0]);
            return samples != null ? samples : new LiveTransferSample[0];
        } catch (final Exception e) {
            LOG.debug("Fetching live transfers from MasterServer", e);
            return new LiveTransferSample[0];
        }
    }

    /**
     * Sends the MasterServer's own geolocation to the client, used to centre the globe. Always includes an explicit
     * {@code originResolved} flag so the frontend can distinguish "not yet known" from "known to be unresolvable" and
     * warn the user in the latter case (see {@link #resolveGeoLocations(ManagementInterface, LiveTransferSample[])} for
     * why this can happen and how an administrator would fix it).
     */
    private void sendHello() {
        final var node = JSON.createObjectNode();
        node.put("type", "hello");
        addOriginFields(node);
        sendText(node.toString());
    }

    /**
     * Sends a snapshot of every currently active transfer to this client.
     *
     * @param samples
     *            the samples to send
     */
    private void sendSnapshot(final LiveTransferSample[] samples) {
        final var node = JSON.createObjectNode();
        node.put("type", "snapshot");
        node.put("bytes24h", bytesLast24h);
        node.put("bytes24hDissemination", bytesLast24hDissemination);
        node.put("bytes24hAcquisition", bytesLast24hAcquisition);
        node.put("dataPortalSessions", dataPortalSessions);
        node.put("dataPortalBytesInPerSecond", dataPortalBytesInPerSecond);
        node.put("dataPortalBytesOutPerSecond", dataPortalBytesOutPerSecond);
        node.put("moverStorageUsedBytes", moverStorageUsedBytes);
        node.put("moverStorageTotalBytes", moverStorageTotalBytes);
        // Re-sent on every poll (not just at connect) so a client whose page is already open picks up the
        // MasterServer's origin location as soon as it becomes resolvable, without needing to reconnect.
        addOriginFields(node);
        final var array = node.putArray("transfers");
        for (final LiveTransferSample sample : samples) {
            array.add(toNode(sample));
        }
        sendText(node.toString());
    }

    /**
     * Adds the MasterServer's origin location (if resolved), its hostname/IP address (used so the frontend can tell an
     * administrator exactly what to configure a {@code [GeoIP]} {@code forced.*} override for when it isn't), and an
     * explicit {@code originResolved} flag to the given JSON node.
     *
     * @param node
     *            the node to add the origin fields to
     */
    private static void addOriginFields(final ObjectNode node) {
        final var origin = originLocation;
        node.put("originResolved", origin != null);
        if (origin != null) {
            node.put("originLat", origin[0]);
            node.put("originLon", origin[1]);
        }
        final var host = originHost;
        if (host != null) {
            node.put("originHost", host);
        }
    }

    /**
     * Builds the JSON representation of a single {@link LiveTransferSample}, including the best-effort resolved
     * geolocation of its target Host.
     *
     * @param sample
     *            the sample
     *
     * @return the JSON node
     */
    private ObjectNode toNode(final LiveTransferSample sample) {
        final var node = JSON.createObjectNode();
        node.put("type", "transfer");
        node.put("transferId", sample.getTransferId());
        node.put("mover", sample.getMoverName());
        node.put("destination", sample.getDestinationName());
        node.put("host", sample.getHostName());
        final var hostNickname = sample.getHostNickname();
        node.put("hostLabel", hostNickname != null && !hostNickname.isBlank() ? hostNickname : sample.getHostName());
        node.put("protocol", sample.getProtocol());
        node.put("fileSize", sample.getFileSize());
        node.put("bytesSent", sample.getByteSent());
        node.put("duration", sample.getDuration());
        node.put("rateBitsPerSecond", sample.getRateBitsPerSecond());
        node.put("status", sample.getStatus());
        node.put("timestamp", sample.getTimestamp());
        node.put("direction", sample.getDirection());
        final var location = resolveHost(hostGeoKey(sample));
        if (location != null) {
            node.put("hostLat", location.latitude());
            node.put("hostLon", location.longitude());
            if (location.country() != null && !location.country().isBlank()) {
                node.put("hostCountry", location.country());
            }
        }
        final var moverName = sample.getMoverName();
        if (moverName != null && activeProxyHostNames.contains(moverName)) {
            node.put("isProxyHost", true);
            final var moverLocation = resolveHost(moverName);
            if (moverLocation != null) {
                node.put("moverLat", moverLocation.latitude());
                node.put("moverLon", moverLocation.longitude());
            }
        }
        return node;
    }

    /**
     * Returns the key to use to resolve/cache a sample's target Host geolocation - the Host's actual network address
     * ({@link LiveTransferSample#getHostAddress()}) when known, since that is the only thing GeoIP can meaningfully
     * resolve (the Host's database name/id, {@link LiveTransferSample#getHostName()}, is just an internal identifier
     * and is never itself resolvable). Falls back to the Host name for older/incomplete samples.
     *
     * @param sample
     *            the sample
     *
     * @return the geo cache key to use for this sample's target Host
     */
    private static String hostGeoKey(final LiveTransferSample sample) {
        final var address = sample.getHostAddress();
        return address != null && !address.isBlank() ? address : sample.getHostName();
    }

    /**
     * Resolves (and caches) the geolocation of every Host address referenced by the given samples (both the transfer's
     * target Host and, for ProxyHosts, the Data Mover itself) that is not already cached, via a single batched RMI
     * round-trip to the MasterServer (see {@link ManagementInterface#getGeoLocations(String[])}) - the GeoIP2 database
     * only ever needs to exist on the MasterServer side, never on the Monitor JVM.
     *
     * @param mi
     *            the management interface to use for the RMI call
     * @param samples
     *            the samples whose Host/Mover names should be resolved
     */
    private static void resolveGeoLocations(final ManagementInterface mi, final LiveTransferSample[] samples) {
        final Set<String> unresolved = new HashSet<>();
        for (final var sample : samples) {
            final var hostKey = hostGeoKey(sample);
            if (hostKey != null && !hostKey.isBlank() && !GEO_CACHE.containsKey(hostKey)) {
                unresolved.add(hostKey);
            }
            final var moverName = sample.getMoverName();
            if (moverName != null && activeProxyHostNames.contains(moverName) && !GEO_CACHE.containsKey(moverName)) {
                unresolved.add(moverName);
            }
        }
        if (unresolved.isEmpty()) {
            return;
        }
        try {
            final var resolved = mi.getGeoLocations(unresolved.toArray(new String[0]));
            if (resolved != null) {
                GEO_CACHE.putAll(resolved);
            }
        } catch (final Exception e) {
            LOG.debug("Resolving geolocations from MasterServer", e);
        }
    }

    /**
     * Looks up the cached geolocation of a Host by name, best-effort. Resolution itself always happens on the
     * MasterServer side (see {@link #resolveGeoLocations(ManagementInterface, LiveTransferSample[])}); this is purely a
     * local cache read.
     *
     * @param hostName
     *            the host name
     *
     * @return the resolved {@link GeoPoint}, or {@code null} if it is not (yet) resolved/cached
     */
    private static GeoPoint resolveHost(final String hostName) {
        return hostName == null || hostName.isBlank() ? null : GEO_CACHE.get(hostName);
    }

    /**
     * Sends a text message to the client, swallowing/logging any error.
     *
     * @param text
     *            the text
     */
    private void sendText(final String text) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.getRemote().sendString(text, WriteCallback.NOOP);
        } catch (final Exception e) {
            LOG.debug("Sending WebSocket message", e);
        }
    }
}
