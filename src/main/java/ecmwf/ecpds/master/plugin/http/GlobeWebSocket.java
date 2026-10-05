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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ecmwf.ecpds.master.GeoPoint;
import ecmwf.ecpds.master.LiveTransferSample;
import ecmwf.ecpds.master.ManagementInterface;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.ProxyHostStatus;

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

    /**
     * Host name/address to resolved {@link GeoPoint} cache, shared across connections. Expires entries after a bounded
     * time (rather than caching forever, like a plain Map would) so that manually correcting a Host's location - or its
     * address changing - is picked up here within a few minutes instead of needing a Monitor restart; mirrors the same
     * TTL-cache pattern already used for the Host Map page's own GeoIP results (see
     * {@code GetHostMapJsonAction#GEOJSON_CACHE}).
     */
    private static final Cache<String, GeoPoint> GEO_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();

    /**
     * Names/addresses recently requested from {@link ManagementInterface#getGeoLocations(String[])} that came back
     * unresolved (e.g. a Proxy Host's {@code proxy.root} value, which is "very often reachable only by a short,
     * non-FQDN name" that plain DNS/GeoIP cannot place - see {@code ManagementImpl#_resolveProxyHostLocation}). Without
     * this, such a name would never get cached in {@link #GEO_CACHE} (only successful resolutions are) and would be
     * re-requested - triggering a fresh, blocking {@code InetAddress.getByName()} lookup on the MasterServer side - on
     * every single {@value #POLL_PERIOD_SECONDS}s poll cycle and every new connection, forever. A short TTL (vs.
     * {@link #GEO_CACHE}'s 5 minutes) still lets a just-corrected {@code proxy.root}/address be picked up reasonably
     * promptly.
     */
    private static final Cache<String, Boolean> UNRESOLVABLE_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).build();

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
     * Names of every currently active ProxyHost, i.e. a Continental Data Mover: a Data Mover physically located
     * elsewhere, reachable from the local Data Movers only through a Proxy-type Host's REST interface (never a direct
     * RMI connection to the MasterServer, unlike an ordinary local Data Mover). A Destination associated with a
     * Proxy-type Host has its files replicated from the local Data Movers to this Continental Mover first;
     * dissemination to the real target Host is then attempted from the Continental Mover (if the replica made it
     * there), falling back to a local Data Mover otherwise - see {@code isProxyHost}/{@code arcOrigin} in globe.jsp's
     * aggregateTransfers() for how that fallback is reflected as a shifted arc origin. Refreshed by the poller. Only
     * used for that arc-origin-shift matching, keyed by root identifier - see {@link #enabledProxyHosts} for the
     * (differently sourced) Continental Mover markers themselves.
     */
    private static volatile Set<String> activeProxyHostNames = Set.of();

    /**
     * Every currently enabled Proxy-type Host, refreshed by the poller (see {@link #STORAGE_POLL_EVERY_N_CYCLES},
     * reused here since the Host list changes as rarely as mover disk usage does). Unlike {@link #activeProxyHostNames}
     * above, this is driven purely by Host configuration, not by whether its Continental Mover is currently connected -
     * so a configured-but-offline one still gets a marker on the globe (dimmed, see globe.jsp's upsertMover()), rather
     * than not showing at all.
     */
    private static volatile ProxyHostStatus[] enabledProxyHosts = new ProxyHostStatus[0];

    /**
     * Resolved location of each enabled Proxy Host, keyed by its own {@code proxy.root} option value - refreshed
     * alongside {@link #enabledProxyHosts}. Used to resolve {@link #activeProxyHostNames} entries (Continental Mover
     * root identifiers) to a location for the arc-origin-shift matching in {@code toNode()} - see
     * {@link ProxyHostStatus}'s javadoc for why this is resolved directly from each Proxy Host's own object rather than
     * via the generic, address-keyed {@link ManagementInterface#getGeoLocations(String[])}.
     */
    private static volatile Map<String, GeoPoint> proxyRootLocations = Map.of();

    /**
     * The sole enabled Proxy Host's own location, used as the fallback match for a Continental Mover root identifier
     * that doesn't equal any configured {@code proxy.root} - but only when there is exactly one enabled Proxy Host with
     * no {@code proxy.root} configured at all (no ambiguity); {@code null} otherwise. Mirrors the identical "sole
     * candidate" rule in {@code ManagementImpl#getEnabledProxyHosts()}'s own {@code connected} computation.
     */
    private static volatile GeoPoint soleUnconfiguredProxyHostLocation;

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
        final LiveTransferSample[] samples;
        try {
            samples = pollNow();
        } catch (final Throwable t) {
            LOG.warn("Polling live transfers from MasterServer", t);
            return;
        }
        for (final GlobeWebSocket client : CLIENTS) {
            // Isolated per client: building/sending one client's snapshot must never prevent every other connected
            // client (possibly ordered later in CLIENTS) from getting theirs in this same cycle.
            try {
                client.sendSnapshot(samples);
            } catch (final Throwable t) {
                LOG.warn("Sending snapshot to a globe client", t);
            }
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
                try {
                    final var proxyHostsStatus = mi.getEnabledProxyHosts();
                    enabledProxyHosts = proxyHostsStatus != null ? proxyHostsStatus : new ProxyHostStatus[0];
                    // Build the root-identifier -> location lookup used by toNode()'s arc-origin-shift matching,
                    // straight from each Proxy Host's own already-resolved location (see ProxyHostStatus's javadoc)
                    // - no RMI/GeoIP round trip needed here, and no risk of it being confused with an unrelated
                    // target Host's address (the bug this replaces - see getGeoLocations()'s own updated javadoc).
                    final Map<String, GeoPoint> rootLocations = new HashMap<>();
                    GeoPoint unconfiguredCandidateGeo = null;
                    var unconfiguredCandidateCount = 0;
                    for (final var p : enabledProxyHosts) {
                        final var geo = p.latitude() != null && p.longitude() != null
                                ? new GeoPoint(p.latitude(), p.longitude(), null) : null;
                        if (p.root() != null && !p.root().isBlank()) {
                            if (geo != null) {
                                rootLocations.put(p.root(), geo);
                            }
                        } else {
                            // No proxy.root configured - only usable as a fallback match if it is the sole such
                            // candidate (mirrors ManagementImpl#getEnabledProxyHosts()'s identical "connected" rule).
                            unconfiguredCandidateCount++;
                            unconfiguredCandidateGeo = geo;
                        }
                    }
                    proxyRootLocations = rootLocations;
                    soleUnconfiguredProxyHostLocation = unconfiguredCandidateCount == 1 ? unconfiguredCandidateGeo
                            : null;
                    if (LOG.isDebugEnabled()) {
                        final var summary = new StringBuilder();
                        for (final var p : enabledProxyHosts) {
                            if (summary.length() > 0) {
                                summary.append("; ");
                            }
                            summary.append(p.name()).append("[root=").append(p.root()).append(", address=")
                                    .append(p.address()).append(", lat=").append(p.latitude()).append(", lon=")
                                    .append(p.longitude()).append(", connected=").append(p.connected()).append(']');
                        }
                        LOG.debug(
                                "Refreshed {} enabled Proxy Host(s): {} - sole-unconfigured-candidate fallback "
                                        + "location: {}",
                                enabledProxyHosts.length, summary, soleUnconfiguredProxyHostLocation);
                    }
                } catch (final Exception e) {
                    LOG.debug("Fetching enabled Proxy Hosts", e);
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
        // Every currently enabled Proxy Host, independent of whether its Continental Mover is currently connected
        // or has any transfer sample right now, so the frontend can show a persistent marker for it as soon as it
        // is configured, dimmed while not connected (see the frontend's applySnapshot()/upsertMover()). Each one's
        // location is already resolved server-side (see ProxyHostStatus's javadoc).
        final var proxyHostsArray = node.putArray("proxyHosts");
        for (final var proxyHost : enabledProxyHosts) {
            if (proxyHost.latitude() != null && proxyHost.longitude() != null) {
                final var proxyHostNode = JSON.createObjectNode();
                proxyHostNode.put("name", proxyHost.name());
                proxyHostNode.put("nickname", proxyHost.nickname());
                proxyHostNode.put("lat", proxyHost.latitude());
                proxyHostNode.put("lon", proxyHost.longitude());
                proxyHostNode.put("connected", proxyHost.connected());
                proxyHostsArray.add(proxyHostNode);
            }
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
        if (sample.getHostType() != null && !sample.getHostType().isBlank()) {
            node.put("hostType", sample.getHostType());
        }
        final var location = resolveHost(hostGeoKey(sample));
        if (location != null) {
            node.put("hostLat", location.latitude());
            node.put("hostLon", location.longitude());
            if (location.country() != null && !location.country().isBlank()) {
                node.put("hostCountry", location.country());
            }
            if (LOG.isDebugEnabled()) {
                for (final var p : enabledProxyHosts) {
                    if (p.latitude() != null && p.longitude() != null && location.latitude() == p.latitude()
                            && location.longitude() == p.longitude()) {
                        // Expected, not a bug, whenever this transfer's own target Host IS that Proxy Host (a
                        // replication/backup push to it rather than a downstream dissemination past it) - see
                        // globe.jsp's upsertHost(), which merges the two markers into one for exactly this case.
                        LOG.debug("Target Host '{}' for transfer {} resolved to the same location ({}, {}) as enabled "
                                + "Proxy Host '{}' - expected if this transfer's target is that Proxy Host " + "itself",
                                sample.getHostName(), sample.getTransferId(), location.latitude(), location.longitude(),
                                p.name());
                        break;
                    }
                }
            }
        }
        final var moverName = sample.getMoverName();
        // Captured once into a local so the membership check below and the diagnostic log (if it fires) are
        // guaranteed to see the exact same snapshot of this volatile, poller-updated field - reading the field
        // twice separately could otherwise print a contradictory-looking log (e.g. "not in the list" followed by
        // a printed list that does contain it), if the poller updates it in between the two reads.
        final var currentActiveProxyHostNames = activeProxyHostNames;
        if (moverName != null && currentActiveProxyHostNames.contains(moverName)) {
            node.put("isProxyHost", true);
            final var exact = proxyRootLocations.get(moverName);
            final var moverLocation = exact != null ? exact : soleUnconfiguredProxyHostLocation;
            if (moverLocation != null) {
                node.put("moverLat", moverLocation.latitude());
                node.put("moverLon", moverLocation.longitude());
            } else {
                // isProxyHost is correctly true, but the arc-origin shift will silently NOT happen for this sample
                // (falls back to the plain OpenECPDS origin) because this mover's location never resolved - most
                // likely no Proxy-type Host's proxy.root option matches this exact moverName (and there is either no
                // enabled Proxy Host left unconfigured to fall back to, or more than one, making it ambiguous), or the
                // matching one has no resolvable location of its own (see ProxyHostStatus's javadoc).
                LOG.debug("ProxyHost '{}' matched activeProxyHostNames but its location did not resolve "
                        + "(check proxy.root on the matching Proxy-type Host) - arc origin will stay at OpenECPDS "
                        + "for transfer {}", moverName, sample.getTransferId());
            }
        } else if (moverName != null && LiveTransferSample.DIRECTION_DISSEMINATION.equals(sample.getDirection())) {
            // Debug-only visibility into why a Dissemination sample's arc origin was NOT shifted: either this
            // mover is genuinely a local, directly-connected Data Mover (expected, not an error), or it is a
            // Continental Mover whose self-reported root identifier does not currently appear in
            // activeProxyHostNames (e.g. its heartbeat is not currently registered on the MasterServer). Also logs
            // each name's length, so an invisible whitespace/encoding difference (same-looking string, different
            // .equals()) would show up as a length mismatch instead of staying invisible.
            LOG.debug(
                    "Dissemination sample for transfer {} was pushed by mover '{}' (length={}), which is not in "
                            + "the current activeProxyHostNames list {} (lengths={}) - arc origin stays at OpenECPDS",
                    sample.getTransferId(), moverName, moverName.length(), currentActiveProxyHostNames,
                    currentActiveProxyHostNames.stream().map(n -> n == null ? "null" : String.valueOf(n.length()))
                            .toList());
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
     * Resolves (and caches) the geolocation of every target Host address referenced by the given samples that is not
     * already cached, via a single batched RMI round-trip to the MasterServer (see
     * {@link ManagementInterface#getGeoLocations(String[])}) - the GeoIP2 database only ever needs to exist on the
     * MasterServer side, never on the Monitor JVM. A Continental Mover's own root identifier, and every enabled Proxy
     * Host's own location, are resolved separately via {@link #enabledProxyHosts}/{@link #proxyRootLocations} instead -
     * see {@link ProxyHostStatus}'s javadoc for why mixing the two into one generic, address-keyed batch used to risk
     * resolving an ordinary target Host to the same coincidental location as an unrelated Proxy Host.
     *
     * @param mi
     *            the management interface to use for the RMI call
     * @param samples
     *            the samples whose Host names should be resolved
     */
    private static void resolveGeoLocations(final ManagementInterface mi, final LiveTransferSample[] samples) {
        final Set<String> unresolved = new HashSet<>();
        for (final var sample : samples) {
            final var hostKey = hostGeoKey(sample);
            if (hostKey != null && !hostKey.isBlank() && GEO_CACHE.getIfPresent(hostKey) == null
                    && UNRESOLVABLE_CACHE.getIfPresent(hostKey) == null) {
                unresolved.add(hostKey);
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
            for (final var key : unresolved) {
                if (resolved == null || !resolved.containsKey(key)) {
                    // Requested but not returned - remember it as unresolvable for a while so it isn't retried (and
                    // doesn't trigger another blocking MasterServer-side DNS/GeoIP lookup) on every poll cycle.
                    UNRESOLVABLE_CACHE.put(key, Boolean.TRUE);
                }
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
        return hostName == null || hostName.isBlank() ? null : GEO_CACHE.getIfPresent(hostName);
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
