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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterServer;

/**
 * Central provider for selecting and ordering {@link TransferServer} instances in ECPDS. The provider is responsible
 * for multi-level load distribution: cluster-level weighted round-robin (WRR), per-group server rotation, and
 * storage-volume-aware allocation.
 *
 * <h2>Scheduling Layers</h2>
 * <ol>
 * <li><b>Group resolution:</b> The provider resolves a {@link TransferGroup} from explicit name, destination mapping,
 * or default configuration.</li>
 * <li><b>Cluster WRR:</b> If enabled by construction semantics, a group within the same cluster can be chosen via WRR.
 * The RR counter resets on topology changes (weights / activation / availability) to avoid skew.</li>
 * <li><b>Volume allocation:</b> The selected group’s volume index is chosen either explicitly (pre-allocated) or via
 * {@link WeightedAllocator}, with optional size-aware penalties and CV-based uniform fallback.</li>
 * <li><b>Server ordering:</b> Within the group, active and reachable DataMovers are ordered by least filesystem
 * activity on the selected volume, with caller-stable rotation as a tie-breaker. A preferred server, if provided, is
 * reinserted at index 0.</li>
 * </ol>
 *
 * <h2>State & Caches</h2>
 * <ul>
 * <li>Instances are immutable w.r.t. selected group, volume index, and server ordering.</li>
 * <li>Static caches maintain cluster RR state, per-group RR counters, volume usage, and allocator statistics.</li>
 * <li>A background daemon refreshes volume usage and mover ordering.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 * <li>All public methods are read-only and safe for concurrent calls.</li>
 * <li>Static schedulers use {@link java.util.concurrent.ConcurrentHashMap} and atomics.</li>
 * <li>Allocator uses per-group locking for consistent snapshots.</li>
 * </ul>
 *
 * <h2>Failure Semantics</h2>
 * <ul>
 * <li>Provider construction fails fast if the group is unavailable, has no volumes, or if no active DataMover is
 * selectable.</li>
 * <li>Destination/group lookup errors propagate as {@link TransferServerException} or
 * {@link ecmwf.common.database.DataBaseException}.</li>
 * </ul>
 */
public final class TransferServerProvider {
    /** Logger used for diagnostics, rotation trace, and allocator debug output. */
    private static final Logger _log = LogManager.getLogger(TransferServerProvider.class);

    /**
     * Central {@link MasterServer} entry point used to access the ECpds database, movers presence
     * ({@code existsClientInterface}), and volume usage computation.
     *
     * <p>
     * Created via {@link StarterServer#getInstance(Class)} and shared across all provider instances.
     * </p>
     */
    private static final MasterServer MASTER = StarterServer.getInstance(MasterServer.class);

    /**
     * Cache of per-volume usage snapshots and mover orderings:
     *
     * <pre>
     *   key   = "<groupName>:<volumeIndex>"
     *   value = {@link MasterServer.VolumeUsageResult}
     * </pre>
     *
     * <p>
     * Populated asynchronously by the background updater in {@link WeightedAllocator#startUsageUpdater()}. This cache
     * is consulted by {@link #getTransferServersByMostFreeSpace()} to return a free-space-ordered list when available.
     * </p>
     *
     * <p>
     * Thread-safe: concurrent map with immutable value objects.
     * </p>
     */
    private static final ConcurrentHashMap<String, MasterServer.VolumeUsageResult> VOLUME_USAGE_CACHE = new ConcurrentHashMap<>();

    /**
     * Per-JVM random salt injected into caller-stable rotation to avoid identical rotation sequences across process
     * restarts.
     *
     * <p>
     * Combined with (caller, groupName) hash in {@link #computeRotationOffset(String, String, int)} to produce
     * stable-but-not-globally-repeatable offsets.
     * </p>
     */
    private static final long RUNTIME_SALT = new SplittableRandom().nextLong();

    /**
     * Cluster-level WRR state:
     * <ul>
     * <li>{@code counter}: the atomic round-robin slot index for the cluster</li>
     * <li>{@code candidatesHash}: the last-seen topology signature to detect changes</li>
     * </ul>
     *
     * <p>
     * On candidate-set change, the counter is reset to avoid skew or stale slots.
     * </p>
     */
    private static final class ClusterState {
        final AtomicInteger counter = new AtomicInteger(0);
        volatile int candidatesHash = 0;
    }

    /**
     * Global map of {@code clusterName -> ClusterState}. Maintains:
     * <ul>
     * <li>the WRR counter per cluster, and</li>
     * <li>the last topology signature (see {@link #hashCandidates(List)}).</li>
     * </ul>
     *
     * <p>
     * Used by {@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])} to implement fair,
     * topology-aware group selection among peers in the same cluster.
     * </p>
     */
    private static final ConcurrentHashMap<String, ClusterState> CLUSTERS = new ConcurrentHashMap<>();

    /**
     * Per-group sliding round-robin counters used as a secondary rotation component in
     * {@link #computeRotationOffset(String, String, int)}.
     *
     * <p>
     * Purpose: prevents successive calls from the same caller from starting at the exact same server, thereby smoothing
     * burst traffic across servers.
     * </p>
     */
    private static final ConcurrentHashMap<String, AtomicInteger> SERVER_RR = new ConcurrentHashMap<>();

    /**
     * Final ordered list of active and reachable {@link TransferServer} instances selected for this provider instance.
     *
     * <p>
     * Ordering is computed exactly once at construction via
     * {@link #computeLeastActivityOrdering(String, TransferGroup, TransferServer, int)} and remains immutable
     * thereafter. All public accessors return defensive copies.
     * </p>
     */
    private final List<TransferServer> servers = new ArrayList<>();

    /**
     * The selected {@link TransferGroup} for this provider instance. Determined during construction by group resolution
     * and optional cluster-level fallback.
     *
     * <p>
     * Guaranteed to be available at construction time; never {@code null} thereafter.
     * </p>
     */
    private TransferGroup group = null;

    /**
     * The selected volume (filesystem) index for this provider instance.
     *
     * <p>
     * Chosen during construction using either:
     * </p>
     * <ul>
     * <li>an explicit {@code allocatedFileSystem} supplied by the caller, or</li>
     * <li>the {@link WeightedAllocator}, with optional size-aware penalties.</li>
     * </ul>
     */
    private int fileSystem = -1;

    /**
     * Exception indicating that a suitable transfer group or server could not be found or selected.
     *
     * <p>
     * Typical causes include:
     * </p>
     * <ul>
     * <li>group not found or not available,</li>
     * <li>no volumes configured for the group,</li>
     * <li>no active DataMover available after filtering,</li>
     * <li>destination mapping errors.</li>
     * </ul>
     */
    public static final class TransferServerException extends Exception {
        private static final long serialVersionUID = -5979035504811469692L;

        TransferServerException(final String message) {
            super(message);
        }
    }

    /**
     * Returns the storage volume (file system) index selected for this provider instance.
     *
     * <p>
     * How the index is obtained:
     * </p>
     * <ul>
     * <li>If the caller provided an explicit {@code allocatedFileSystem} in the constructor, that exact index is
     * used.</li>
     * <li>Otherwise, the index is selected during construction by the {@link WeightedAllocator}, using either
     * size-aware weights (when a positive {@code fileSize} is given) or free-space weights.</li>
     * </ul>
     *
     * <p>
     * This value is immutable for the lifetime of this provider instance and is guaranteed to be within
     * {@code [0, group.getVolumeCount())}.
     * </p>
     *
     * @return the selected (0-based) volume index for this provider instance
     */
    public int getFileSystem() {
        return fileSystem;
    }

    /**
     * Returns the resolved and validated {@link TransferGroup} for this provider instance.
     *
     * <p>
     * The group is resolved at construction time using (in order):
     * </p>
     * <ol>
     * <li>Explicit {@code groupName}, if provided</li>
     * <li>Destination mapping (if a {@code destinationName} was provided)</li>
     * <li>Global default transfer group (from configuration)</li>
     * </ol>
     *
     * <p>
     * After resolution, the provider may switch to a different group within the same cluster through WRR-based fallback
     * if either:
     * </p>
     * <ul>
     * <li>the resolved group is not currently available, or</li>
     * <li>no explicit {@code allocatedFileSystem} was provided and cluster selection is enabled</li>
     * </ul>
     *
     * <p>
     * The returned group is guaranteed to be {@code active} and to have at least one active/connected {@code DataMover}
     * at the time of construction; otherwise construction fails.
     * </p>
     *
     * @return the selected and available {@link TransferGroup}; never {@code null}
     */
    public TransferGroup getTransferGroup() {
        return group;
    }

    /**
     * Convenience accessor for {@link #getTransferGroup()}{@code .getName()}.
     *
     * <p>
     * Equivalent to {@code getTransferGroup().getName()}, provided for call sites that only need the name.
     * </p>
     *
     * @return the name of the selected transfer group; never {@code null}
     */
    public String getTransferGroupName() {
        return group.getName();
    }

    /**
     * Selects a {@link TransferGroup} within the same cluster as {@code original} using a weighted round‑robin (WRR)
     * scheduler that is stable across configuration changes and resilient to skew.
     *
     * <p>
     * This method implements cluster-level load distribution among transfer groups that share the same cluster name. It
     * ensures fairness across groups according to their configured cluster weights and prevents stale rotation when the
     * candidate group set changes.
     * </p>
     *
     * <h3>Eligibility Rules</h3> A group is considered a WRR candidate if:
     * <ul>
     * <li>it belongs to the same cluster as {@code original},</li>
     * <li>its cluster weight is strictly positive (see {@link #getClusterWeight(TransferGroup)}),</li>
     * <li>it is currently available (active and has at least one active DataMover), see
     * {@link #groupIsAvailable(TransferGroup)}.</li>
     * </ul>
     *
     * <p>
     * Groups failing any of the above criteria are ignored for WRR.
     * </p>
     *
     * <h3>Topology‑Change Detection</h3>
     * <p>
     * The method computes a stable hash of the candidate group set using {@link #hashCandidates(List)}. Whenever this
     * hash differs from the last recorded hash in {@code CLUSTERS}, the cluster RR counter is reset. This prevents the
     * scheduler from “carrying over” RR state after reconfiguration, enabling predictable behaviour after topology
     * changes (e.g., enabling/disabling groups or changing weights).
     * </p>
     *
     * <h3>Scheduling</h3>
     * <p>
     * The counter stored in the cluster state is incremented atomically and is used to walk through the weighted sum of
     * the candidate groups. The next group whose weight interval contains the counter slot is selected.
     * </p>
     *
     * <h3>Return Value</h3>
     * <ul>
     * <li>If no eligible cluster peers exist, {@code original} is returned unchanged.</li>
     * <li>Otherwise, the selected peer may be the same as {@code original}, or a different group within the same
     * cluster.</li>
     * </ul>
     *
     * <h3>Thread Safety</h3>
     * <p>
     * Fully thread‑safe; cluster state is stored in a {@link ConcurrentHashMap} and uses atomic counters.
     * </p>
     *
     * @param original
     *            the initial group to base selection on
     * @param allGroups
     *            all transfer groups in the system (typically from database)
     *
     * @return the WRR‑selected group, or {@code original} if no alternatives exist
     */
    private static TransferGroup selectGroupFromClusterRoundRobin(final TransferGroup original,
            final TransferGroup[] allGroups) {
        final var clusterName = original.getClusterName();
        if (clusterName == null)
            return original;

        // Collect eligible groups in the same cluster
        final List<TransferGroup> candidates = new ArrayList<>();
        for (final var g : allGroups) {
            if (clusterName.equals(g.getClusterName()) && getClusterWeight(g) > 0 && groupIsAvailable(g)) {
                candidates.add(g);
            }
        }
        if (candidates.isEmpty()) {
            return original;
        }

        // Stable order to avoid DB/config bias
        candidates.sort(Comparator.comparing(TransferGroup::getName));

        // Compute total weight and a stable signature
        var totalWeight = 0;
        for (final var g : candidates) {
            totalWeight += getClusterWeight(g);
        }
        if (totalWeight <= 0) {
            return original;
        }

        final var candidatesHash = hashCandidates(candidates);
        final var state = CLUSTERS.computeIfAbsent(clusterName, _ -> new ClusterState());
        if (state.candidatesHash != candidatesHash) {
            state.candidatesHash = candidatesHash;
            state.counter.set(0);
            if (_log.isDebugEnabled()) {
                _log.debug("Cluster {} candidates changed; RR counter reset", clusterName);
            }
        }

        var slot = Math.floorMod(state.counter.getAndIncrement(), totalWeight);
        for (final var g : candidates) {
            slot -= getClusterWeight(g);
            if (slot < 0) {
                if (_log.isDebugEnabled() && !g.getName().equals(original.getName())) {
                    _log.debug("Selected TransferGroup {} from cluster {} (WRR)", g.getName(), clusterName);
                }
                return g;
            }
        }
        // Defensive
        return original;
    }

    /**
     * Returns the effective cluster weight of the given group for WRR scheduling.
     *
     * <p>
     * A weight is considered valid only if it is non-null and strictly positive. All other values (including
     * {@code null}, zero, or negative numbers) are treated as weight {@code 0}, which effectively excludes the group
     * from cluster‑level WRR selection.
     * </p>
     *
     * <p>
     * This method does <strong>not</strong> enforce group availability; that is handled separately by
     * {@link #groupIsAvailable(TransferGroup)} within
     * {@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])}.
     * </p>
     *
     * @param group
     *            the transfer group whose cluster weight should be examined
     *
     * @return the positive cluster weight, or {@code 0} if the weight is undefined or non-positive
     */
    private static int getClusterWeight(final TransferGroup group) {
        return group.getClusterWeight() != null && group.getClusterWeight() > 0 ? group.getClusterWeight() : 0;
    }

    /**
     * Returns a snapshot of the active {@link TransferServer} list for this provider instance, ordered by the “least
     * activity” policy that was computed during construction.
     *
     * <p>
     * Ordering policy (computed at construction time):
     * </p>
     * <ol>
     * <li>Caller-stable base rotation (deterministic hash of {@code caller}, group name, and runtime salt) combined
     * with a per-group round-robin counter to mitigate burst skew.</li>
     * <li>Filtering of inactive or unreachable servers (only active servers with a reachable {@code DataMover}
     * interface remain).</li>
     * <li>Primary sort by per-volume activity: servers with the <em>fewest</em> downloads on {@link #getFileSystem()}
     * come first.</li>
     * <li>Tie-breaker by rotation order: among equal loads, the rotated declared order decides.</li>
     * <li>If a preferred server was provided to the constructor and is available, it is reinserted at index 0.</li>
     * </ol>
     *
     * <p>
     * The returned list is an independent copy and can be freely modified by the caller. Subsequent calls return a
     * fresh copy of the same internal ordering (the provider does not recompute ordering after construction).
     * </p>
     *
     * <p>
     * <strong>Thread-safety:</strong> This method performs no mutation and is safe for concurrent calls. The underlying
     * ordering is immutable within this provider instance.
     * </p>
     *
     * @return a new {@link List} of active {@link TransferServer} instances, ordered by least activity
     */
    public List<TransferServer> getTransferServersByLeastActivity() {
        return new ArrayList<>(servers);
    }

    /**
     * Returns the active {@link TransferServer} list ordered by highest free space on the selected volume
     * ({@link #getFileSystem()}), if a cache entry is available.
     *
     * <p>
     * Behaviour:
     * </p>
     * <ul>
     * <li>If a {@code VolumeUsageResult} is present in the internal cache for {@code groupName + ":" + fileSystem}, the
     * method uses the mover ordering computed by
     * {@link MasterServer#computeVolumeUsageAndSortedMovers(TransferGroup, int)} (descending free space), intersected
     * with the provider's active set.</li>
     * <li>If no cache entry exists, the method returns a shuffled copy of the provider's least-activity list as a
     * best-effort fallback to avoid persistent hot-spotting.</li>
     * </ul>
     *
     * <p>
     * <strong>Scope:</strong> This method does not modify the provider's internal least-activity ordering; it returns a
     * new list each time. The cache is populated asynchronously by the background usage updater.
     * </p>
     *
     * <p>
     * <strong>Thread-safety:</strong> Safe for concurrent callers; relies on immutable snapshots and concurrent maps.
     * </p>
     *
     * @return a new {@link List} of active {@link TransferServer} instances sorted by descending free space on the
     *         selected volume when cache is present; otherwise a shuffled best-effort ordering
     */
    public List<TransferServer> getTransferServersByMostFreeSpace() {
        final var key = group.getName() + ":" + fileSystem;
        final var cached = VOLUME_USAGE_CACHE.get(key);
        if (cached == null || cached.moversSortedByUsage == null) {
            _log.debug("No cache entry found for {}", key);
            final var list = new ArrayList<>(servers);
            Collections.shuffle(list);
            return list;
        }
        final var order = cached.moversSortedByUsage;
        final Map<String, Integer> index = new HashMap<>(order.length);
        for (var i = 0; i < order.length; i++) {
            index.put(order[i], i);
        }
        final List<TransferServer> sorted = new ArrayList<>(servers);
        sorted.sort((a, b) -> {
            final var ia = index.get(a.getName());
            final var ib = index.get(b.getName());
            if (ia == null && ib == null)
                return 0;
            if (ia == null)
                return 1;
            if (ib == null)
                return -1;
            return Integer.compare(ia, ib);
        });
        return sorted;
    }

    /**
     * Computes the final ordered list of active {@link TransferServer} instances for the given caller, transfer group,
     * and file system index.
     *
     * <p>
     * This method combines several scheduling layers into a single deterministic ordering pipeline. The result is
     * computed once per provider construction and returned to callers through
     * {@link #getTransferServersByLeastActivity()}.
     * </p>
     *
     * <h3>Ordering Pipeline</h3>
     * <ol>
     * <li><b>Declared server lookup:</b> Fetch the full set of DataMovers for the group from the database. If none are
     * configured, construction fails.</li>
     *
     * <li><b>Caller‑stable rotation seed:</b> Compute a rotation offset via
     * {@link #computeRotationOffset(String, String, int)} using:
     * <ul>
     * <li>the caller identifier,</li>
     * <li>the group name,</li>
     * <li>a per‑JVM runtime salt,</li>
     * <li>a per‑group RR counter.</li>
     * </ul>
     * This defines a rotated view of the declared servers to ensure caller‑specific stability and fairness under bursty
     * workloads.</li>
     *
     * <li><b>Filtering of unusable servers:</b> Only servers that are <em>both</em> active and have an active
     * {@code DataMover} interface registered in {@link MasterServer} are considered. If a preferred server is provided
     * and active, it is not added here (it will be reinserted later).</li>
     *
     * <li><b>Filesystem‑aware activity measurement:</b> For each active server, fetch its current number of downloads
     * on the selected volume index via {@link TransferScheduler#getNumberOfDownloadsFor(TransferServer, int)}. These
     * counts are stored in a local map.</li>
     *
     * <li><b>Primary sort by filesystem load:</b> Active servers are sorted ascending by their filesystem‑specific
     * load. Servers performing fewer downloads on this volume are preferred.</li>
     *
     * <li><b>Secondary tie‑breaker by rotation order:</b> If two or more servers have identical load, their relative
     * order is determined by their declared index after rotation. This preserves caller‑stable fairness without
     * compromising load‑based prioritisation.</li>
     *
     * <li><b>Preferred‑server reinsertion:</b> If {@code originalServer} was provided, is active, and reachable, it is
     * placed at index 0 of the final list. Its load is included in debug output but does not affect the ordering of
     * others.</li>
     * </ol>
     *
     * <h3>Failure Modes</h3>
     * <ul>
     * <li>If the declared server list is empty: {@link DataBaseException}.</li>
     * <li>If filtering results in no active servers: the caller (constructor) throws a {@link TransferServerException}
     * after this method returns.</li>
     * </ul>
     *
     * <h3>Thread Safety</h3>
     * <p>
     * Fully thread‑safe: all inputs are local snapshots, and the method performs no shared-state mutation. The provider
     * caches the resulting list immutably.
     * </p>
     *
     * @param caller
     *            caller identifier for rotation seeding
     * @param group
     *            the resolved and validated {@link TransferGroup}
     * @param originalServer
     *            optional preferred server to place at index 0 if active
     * @param fileSystem
     *            filesystem/volume index used for activity measurement
     *
     * @return the ordered list of active servers (never {@code null})
     *
     * @throws DataBaseException
     *             if declared servers cannot be retrieved
     */
    private static List<TransferServer> computeLeastActivityOrdering(final String caller, final TransferGroup group,
            final TransferServer originalServer, final int fileSystem) throws DataBaseException {

        final var dataBase = MASTER.getECpdsBase();

        // -----------------------------------------
        // 1. Resolve declared servers
        // -----------------------------------------
        final var groupName = group.getName();
        final var declaredServers = dataBase.getTransferServers(groupName);
        if (declaredServers.length == 0) {
            throw new DataBaseException("No DataMover available for TransferGroup " + groupName);
        }

        // -----------------------------------------
        // 2. Compute rotation offset (stable + per-group RR)
        // -----------------------------------------
        final int startIndex = computeRotationOffset(caller, groupName, declaredServers.length);

        // Helper: map server -> declared index
        final Map<String, Integer> declaredIndex = new HashMap<>();
        for (int i = 0; i < declaredServers.length; i++) {
            declaredIndex.put(declaredServers[i].getName(), i);
        }

        // -----------------------------------------
        // 3. Filter active servers; detect preferred server
        // -----------------------------------------
        final List<TransferServer> active = new ArrayList<>(declaredServers.length);
        boolean originalFound = false;

        for (int i = 0; i < declaredServers.length; i++) {
            final var s = declaredServers[(startIndex + i) % declaredServers.length];
            final var name = s.getName();

            if (!s.getActive() || !MASTER.existsClientInterface(name, "DataMover")) {
                continue;
            }

            if (originalServer != null && name.equals(originalServer.getName())) {
                originalFound = true;
            } else {
                active.add(s);
            }
        }

        // -----------------------------------------
        // 4. Collect filesystem load
        // -----------------------------------------
        final Map<String, Integer> load = new HashMap<>();
        for (var ts : active) {
            load.put(ts.getName(), TransferScheduler.getNumberOfDownloadsFor(ts, fileSystem));
        }

        // If preferred server should be reinserted, collect its load now
        if (originalFound && originalServer != null && _log.isDebugEnabled()) {
            load.put(originalServer.getName(), TransferScheduler.getNumberOfDownloadsFor(originalServer, fileSystem));
        }

        // -----------------------------------------
        // 5. Sort by:
        // (a) least filesystem load
        // (b) rotation position (tie-breaker)
        // -----------------------------------------
        active.sort((a, b) -> {
            int la = load.get(a.getName());
            int lb = load.get(b.getName());
            if (la != lb) {
                return Integer.compare(la, lb); // primary key: FS load
            }

            // Secondary key: rotation ordering
            int ia = Math.floorMod(declaredIndex.get(a.getName()) - startIndex, declaredServers.length);
            int ib = Math.floorMod(declaredIndex.get(b.getName()) - startIndex, declaredServers.length);
            return Integer.compare(ia, ib);
        });

        // -----------------------------------------
        // 6. Reinsert original server on top (if present)
        // -----------------------------------------
        if (originalFound && originalServer != null) {
            active.add(0, originalServer);
        }

        // -----------------------------------------
        // 7. Debug
        // -----------------------------------------
        if (_log.isDebugEnabled()) {
            logSelectedServers(caller, group, active, load, fileSystem);
        }

        return active;
    }

    /**
     * Attempts to select an alternative {@link TransferGroup} from the same cluster when the originally resolved group
     * is not suitable (typically because it is unavailable or lacks active movers).
     *
     * <p>
     * This method delegates to {@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])}, which
     * implements cluster‑wide weighted round‑robin (WRR) selection among eligible groups. If WRR returns a different
     * group and that group is available, it is used as the fallback.
     * </p>
     *
     * <h3>When Fallback Occurs</h3> Fallback may occur when:
     * <ul>
     * <li>The caller passed {@code allocatedFileSystem = null} (constructor triggers fallback mode), or</li>
     * <li>The originally resolved group is not available, or</li>
     * <li>A load‑balancing policy intentionally rotates among cluster peers.</li>
     * </ul>
     *
     * <p>
     * The method never selects a group from a different cluster; if the initial group has no cluster name or no
     * eligible peers, the original group is returned.
     * </p>
     *
     * <h3>Logging</h3> A debug message is emitted when the fallback selects a different group within the same cluster.
     * </p>
     *
     * @param originalGroup
     *            the initially resolved group
     *
     * @return the fallback group chosen through cluster WRR, or {@code originalGroup} if no alternative exists
     */
    private static TransferGroup tryClusterFallback(final TransferGroup originalGroup) {
        final var clusterName = originalGroup.getClusterName();
        if (isEmpty(clusterName))
            return originalGroup;

        final var allGroups = MASTER.getECpdsBase().getTransferGroupArray();
        final var selected = selectGroupFromClusterRoundRobin(originalGroup, allGroups);

        if (!selected.getName().equals(originalGroup.getName())) {
            _log.debug("Cluster fallback: selecting TransferGroup {} from cluster {}", selected.getName(), clusterName);
        }
        return selected;
    }

    /**
     * Computes the stable rotation offset used to reorder the declared server list for a given (caller, groupName)
     * pair.
     *
     * <p>
     * This rotation mechanism has two components:
     * </p>
     *
     * <h3>1. Deterministic base index</h3>
     * <p>
     * The base offset is derived from a stable hash of:
     * </p>
     * <ul>
     * <li>{@code caller} – the logical identifier of the calling subsystem,</li>
     * <li>{@code groupName} – the active transfer group,</li>
     * <li>{@code RUNTIME_SALT} – a per‑JVM random salt guaranteeing that different JVM runs do not align in the same
     * rotation pattern.</li>
     * </ul>
     * <p>
     * This ensures that repeated invocations from the same caller yield consistent relative rotations within a single
     * JVM lifetime, while preventing cross‑restart correlation or hot-spotting.
     * </p>
     *
     * <h3>2. Per‑group round‑robin counter (RR)</h3>
     * <p>
     * A per‑group atomic counter is maintained in {@code SERVER_RR}. It increments on each call to this method for a
     * given group, providing a sliding offset that prevents bursty traffic from concentrating repeatedly on the same
     * server.
     * </p>
     *
     * <h3>Final Offset</h3>
     * <p>
     * The method returns:
     * </p>
     *
     * <pre>
     * (base + rr) % size
     * </pre>
     *
     * <p>
     * where {@code size} is the number of declared servers. The result is always within {@code [0, size)}.
     * </p>
     *
     * <h3>Thread‑safety</h3>
     * <p>
     * Fully thread‑safe: the RR counter is atomic and each group has its own counter.
     * </p>
     *
     * <h3>Usage</h3>
     * <p>
     * The returned offset is used to reindex the declared server array so that iteration begins at a caller‑dependent
     * position, producing a deterministic but evenly spread rotation.
     * </p>
     *
     * @param caller
     *            identifier used for deterministic seeding of the rotation
     * @param groupName
     *            the name of the transfer group whose servers are being rotated
     * @param size
     *            number of servers in the declared list; must be {@code > 0}
     *
     * @return a non‑negative offset within the declared server list
     */
    private static int computeRotationOffset(final String caller, final String groupName, final int size) {
        final var base = Math.floorMod(java.util.Objects.hash(getCaller(caller), groupName, RUNTIME_SALT), size);
        final var rr = Math.floorMod(SERVER_RR.computeIfAbsent(groupName, _ -> new AtomicInteger()).getAndIncrement(),
                size);
        final var idx = (base + rr) % size;
        if (_log.isDebugEnabled()) {
            _log.debug("Rotation offset for {}.{}: base={}, rr={}, idx={}", getCaller(caller), groupName, base, rr,
                    idx);
        }
        return idx;
    }

    private static String getCaller(final String caller) {
        return isEmpty(caller) ? "unknown" : caller;
    }

    /**
     * Emits a detailed debug log entry describing the selected server ordering for a particular caller and group,
     * including per‑server load information.
     *
     * <p>
     * This method is invoked only when debug logging is enabled. It produces a compact diagnostic string of the form:
     * </p>
     *
     * <pre>
     *   Selected DataMovers for &lt;caller&gt;.&lt;group&gt; [fileSystem=X]: serverA(3),serverB(5),serverC(7)
     * </pre>
     *
     * where:
     * <ul>
     * <li>{@code serverA}, {@code serverB}, ... are active transfer servers in the final ordering,</li>
     * <li>numbers in parentheses represent the filesystem‑specific load obtained via
     * {@link TransferScheduler#getNumberOfDownloadsFor(TransferServer, int)},</li>
     * <li>{@code fileSystem} identifies the volume index for which load was computed.</li>
     * </ul>
     *
     * <h3>Purpose</h3>
     * <ul>
     * <li>Provides visibility into the final ordering chosen by
     * {@link #computeLeastActivityOrdering(String, TransferGroup, TransferServer, int)},</li>
     * <li>Helps diagnose rotation behavior, fallback effects, and unexpected load asymmetries,</li>
     * <li>Useful to verify correctness of activity-based selection in production clusters.</li>
     * </ul>
     *
     * <h3>Thread‑safety</h3>
     * <p>
     * Read‑only; safe for concurrent use.
     * </p>
     *
     * @param caller
     *            identifier of the caller for which the ordering was computed
     * @param group
     *            the selected {@link TransferGroup}
     * @param servers
     *            the final sorted list of active servers (in least‑activity order)
     * @param loadPerServer
     *            map of server name → filesystem load used for ordering
     * @param fileSystem
     *            the volume index for which load information was computed
     */
    private static void logSelectedServers(final String caller, final TransferGroup group,
            final List<TransferServer> servers, final Map<String, Integer> loadPerServer, final Integer fileSystem) {
        final var sb = new StringBuilder();
        for (final TransferServer ts : servers) {
            if (sb.length() > 0)
                sb.append(',');
            sb.append(ts.getName());
            final var load = loadPerServer.get(ts.getName());
            if (load != null) {
                sb.append('(').append(load).append(')');
            }
        }
        _log.debug("Selected DataMovers for {}.{} [fileSystem={}]: {}", getCaller(caller), group.getName(), fileSystem,
                sb);
    }

    /**
     * Convenience constructor that delegates to the canonical constructor with:
     * <ul>
     * <li>{@code server = null}</li>
     * <li>{@code allocatedFileSystem = null}</li>
     * </ul>
     * See {@link #TransferServerProvider(String, String, String, long, TransferServer, Integer)} for detailed
     * behaviour.
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, {@code destination} and/or default are used
     * @param destination
     *            optional destination name used for group resolution
     * @param fileSize
     *            expected file size in bytes (non-positive disables size-aware allocation)
     *
     * @throws TransferServerException
     *             if no suitable group/server can be selected
     * @throws DataBaseException
     *             on database errors
     */
    public TransferServerProvider(final String caller, final String groupName, final String destination,
            final long fileSize) throws TransferServerException, DataBaseException {
        this(caller, groupName, destination, fileSize, null, null);
    }

    /**
     * Convenience constructor that delegates to the canonical constructor with:
     * <ul>
     * <li>{@code fileSize = -1} (disables size-aware allocation)</li>
     * </ul>
     * See {@link #TransferServerProvider(String, String, String, long, TransferServer, Integer)} for detailed
     * behaviour.
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, {@code destination} and/or default are used
     * @param destination
     *            optional destination name used for group resolution
     * @param server
     *            optional preferred server to reinsert at index 0 if active/reachable
     * @param allocatedFileSystem
     *            optional explicit volume index; when {@code null}, the allocator is used
     *
     * @throws TransferServerException
     *             if no suitable group/server can be selected
     * @throws DataBaseException
     *             on database errors
     */
    public TransferServerProvider(final String caller, final String groupName, final String destination,
            final TransferServer server, final Integer allocatedFileSystem)
            throws TransferServerException, DataBaseException {
        this(caller, groupName, destination, -1, server, allocatedFileSystem);
    }

    /**
     * Convenience constructor that delegates to the canonical constructor with:
     * <ul>
     * <li>{@code fileSize = -1}</li>
     * <li>{@code server = null}</li>
     * <li>{@code allocatedFileSystem = null}</li>
     * </ul>
     * See {@link #TransferServerProvider(String, String, String, long, TransferServer, Integer)} for detailed
     * behaviour.
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, {@code destination} and/or default are used
     * @param destination
     *            optional destination name used for group resolution
     *
     * @throws TransferServerException
     *             if no suitable group/server can be selected
     * @throws DataBaseException
     *             on database errors
     */
    public TransferServerProvider(final String caller, final String groupName, final String destination)
            throws TransferServerException, DataBaseException {
        this(caller, groupName, destination, -1, null, null);
    }

    /**
     * Convenience constructor that delegates to the canonical constructor with:
     * <ul>
     * <li>{@code fileSize = -1} (disables size‑aware volume allocation)</li>
     * <li>{@code server = null} (no preferred server to reinsert)</li>
     * <li>{@code allocatedFileSystem} explicitly provided by the caller</li>
     * </ul>
     *
     * <p>
     * This overload is used when the caller wants to:
     * </p>
     * <ul>
     * <li>select a specific transfer group (via {@code groupName} or destination lookup),</li>
     * <li>optionally resolve the group based on {@code destination},</li>
     * <li>force the use of a specific storage volume index (bypassing weighted allocation),</li>
     * <li>and does not wish to use size‑aware volume selection.</li>
     * </ul>
     *
     * <p>
     * The behaviour of group resolution, availability checks, and cluster‑level fallback follows the rules documented
     * in the canonical constructor
     * {@link #TransferServerProvider(String, String, String, long, TransferServer, Integer)}.
     * </p>
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, group is resolved from {@code destination} or configuration
     * @param destination
     *            optional destination used for group resolution
     * @param allocatedFileSystem
     *            explicit volume index to use instead of weighted allocation
     *
     * @throws TransferServerException
     *             if the resolved/selected group is not available, has no volumes, or if no active DataMover can be
     *             selected
     * @throws DataBaseException
     *             if database queries for group, destination, or servers fail
     */
    public TransferServerProvider(final String caller, final String groupName, final String destination,
            final Integer allocatedFileSystem) throws TransferServerException, DataBaseException {
        this(caller, groupName, destination, -1, null, allocatedFileSystem);
    }

    /**
     * Convenience constructor that delegates to the canonical constructor with:
     * <ul>
     * <li>{@code destinationName = null}</li>
     * <li>{@code fileSize = -1}</li>
     * <li>{@code server = null}</li>
     * <li>{@code allocatedFileSystem = null}</li>
     * </ul>
     * See {@link #TransferServerProvider(String, String, String, long, TransferServer, Integer)} for detailed
     * behaviour.
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, the default is used
     *
     * @throws TransferServerException
     *             if no suitable group/server can be selected
     * @throws DataBaseException
     *             on database errors
     */
    public TransferServerProvider(final String caller, final String groupName)
            throws TransferServerException, DataBaseException {
        this(caller, groupName, null, -1, null, null);
    }

    /**
     * Constructs a provider and pre-computes:
     * <ul>
     * <li>the {@link TransferGroup} to use (explicit, destination-mapped, or default; may be adjusted within the
     * cluster by WRR fallback),</li>
     * <li>the storage volume index ({@link #getFileSystem()}), using either an explicit index or the
     * {@link WeightedAllocator},</li>
     * <li>the ordered {@link TransferServer} list according to the least-activity policy (FS activity primary; rotation
     * tie-breaker; optional preferred server on top).</li>
     * </ul>
     *
     * <p>
     * <strong>Group resolution & fallback:</strong> The constructor first resolves the group via
     * {@link #resolveTransferGroup(String, String)}. If {@code allocatedFileSystem} is {@code null} or if the resolved
     * group is not available, it may select a different group within the same cluster using
     * {@link #tryClusterFallback(TransferGroup)}, which in turn leverages cluster-wide WRR through
     * {@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])}.
     * </p>
     *
     * <p>
     * <strong>Volume selection:</strong> If {@code allocatedFileSystem} is non-null, it is used as-is. Otherwise, the
     * {@link WeightedAllocator} is invoked:
     * <ul>
     * <li>when {@code fileSize > 0}, {@link WeightedAllocator#allocate(TransferGroup, long)} is used;</li>
     * <li>otherwise, {@link WeightedAllocator#allocate(TransferGroup)} is used.</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Server ordering:</strong> The final least-activity ordering is computed once during construction via
     * {@link #computeLeastActivityOrdering(String, TransferGroup, TransferServer, int)}. If the resulting active list
     * is empty, construction fails.
     * </p>
     *
     * <p>
     * <strong>Thread-safety:</strong> The constructed instance is immutable with respect to selection decisions (group,
     * volume index, server ordering). Static caches and background updaters are concurrency-safe.
     * </p>
     *
     * @param caller
     *            identifier of the caller (used for stable rotation seeding)
     * @param groupName
     *            explicit group name; if {@code null}, {@code destinationName} and/or the global default are used
     * @param destinationName
     *            optional destination used for group resolution (see {@link #resolveTransferGroup(String, String)})
     * @param fileSize
     *            expected file size in bytes; non-positive values disable size-aware allocation
     * @param server
     *            optional preferred server to reinsert at index 0 if active/reachable
     * @param allocatedFileSystem
     *            optional explicit volume index; when {@code null}, the allocator is used
     *
     * @throws TransferServerException
     *             if the resolved/selected group is not available, has no volumes, or if no active DataMover can be
     *             selected
     * @throws DataBaseException
     *             if the database lookups fail for group, destination, or servers
     */
    public TransferServerProvider(final String caller, final String groupName, final String destinationName,
            final long fileSize, final TransferServer server, final Integer allocatedFileSystem)
            throws TransferServerException, DataBaseException {
        group = resolveTransferGroup(groupName, destinationName);

        // Only fall back to cluster if the group is not available or FS not
        // pre-allocated
        final var checkCluster = allocatedFileSystem == null || !groupIsAvailable(group);
        if (checkCluster) {
            _log.debug("Force cluster checking for {}", group.getName());
            group = tryClusterFallback(group);
        }
        if (!groupIsAvailable(group)) {
            throw new TransferServerException("TransferGroup " + group.getName() + " not available");
        }

        assertValidVolumeCount(group);
        fileSystem = allocatedFileSystem != null ? allocatedFileSystem
                : fileSize > 0 ? WeightedAllocator.allocate(group, fileSize) : WeightedAllocator.allocate(group);

        servers.addAll(computeLeastActivityOrdering(caller, group, server, fileSystem));

        if (servers.isEmpty()) {
            throw new TransferServerException("No TransferServer available for TransferGroup " + group.getName());
        }
    }

    /**
     * Resolves the {@link TransferGroup} to use for this provider by applying the standard multi-stage group resolution
     * rules used by ECPDS.
     *
     * <p>
     * This method is invoked during provider construction and determines the initial transfer group before any
     * cluster-level fallback is applied by {@link #tryClusterFallback(TransferGroup)}.
     * </p>
     *
     * <h3>Resolution Algorithm</h3> The method applies the following steps in order:
     *
     * <ol>
     * <li><b>Explicit group name</b><br>
     * If {@code groupName} is non-null and non-empty, attempt to load the group by name via
     * {@link ecmwf.common.database.ECpdsBase#getTransferGroupObject(String)}. If the group does not exist, a
     * {@link TransferServerException} is thrown.</li>
     *
     * <li><b>Destination mapping</b> (only if {@code destination} is non-empty)<br>
     * Resolve the destination via:
     * <ul>
     * <li>{@link ecmwf.common.database.ECpdsBase#getDestinationHost(String, HostOption)} using
     * {@code HostOption.DISSEMINATION},</li>
     * <li>fallback to {@link ecmwf.common.database.ECpdsBase#getDestination(String)}.</li>
     * </ul>
     * If a destination or primary host is found and it specifies a transfer group, that group is returned. If a
     * destination host exists but has no group assigned, a {@link TransferServerException} is thrown.</li>
     *
     * <li><b>Global default transfer group</b><br>
     * Falls back to the configured default group specified by {@code Cnf.at("Server", "defaultTransferGroup")}. If the
     * configuration entry is missing or refers to a nonexistent group, a {@link TransferServerException} is
     * thrown.</li>
     * </ol>
     *
     * <h3>Postconditions</h3>
     * <ul>
     * <li>The returned group is not guaranteed to be available or active; that is checked separately in subsequent
     * steps (see {@link #groupIsAvailable(TransferGroup)}).</li>
     * <li>The caller must apply cluster fallback if availability or pre-allocation rules require it (handled in the
     * provider constructor).</li>
     * </ul>
     *
     * <h3>Failure Conditions</h3>
     * <ul>
     * <li>Group not found (explicit name)</li>
     * <li>Destination not found or has no mapped group</li>
     * <li>Default group missing or invalid</li>
     * </ul>
     *
     * @param groupName
     *            optional explicit group name
     * @param destination
     *            optional destination used to infer the transfer group
     *
     * @return the resolved {@link TransferGroup}; never {@code null}
     *
     * @throws TransferServerException
     *             if no suitable group can be found
     * @throws DataBaseException
     *             on database lookup failures
     */
    private static TransferGroup resolveTransferGroup(final String groupName, final String destination)
            throws TransferServerException, DataBaseException {
        final var dataBase = MASTER.getECpdsBase();

        // 1) explicit group
        if (isNotEmpty(groupName)) {
            final var group = dataBase.getTransferGroupObject(groupName);
            if (group == null) {
                throw new TransferServerException("TransferGroup " + groupName + " not found");
            }
            return group;
        }

        // 2) from destination (only if provided)
        if (isNotEmpty(destination)) {
            final var hosts = dataBase.getDestinationHost(destination, HostOption.DISSEMINATION);
            if (hosts.length > 0) {
                final var primary = hosts[0];
                final var group = primary.getTransferGroup();
                if (group == null) {
                    throw new TransferServerException("No TransferGroup defined for Host " + primary.getNickname());
                }
                return group;
            }
            final var destinationObj = dataBase.getDestination(destination);
            final var g = destinationObj.getTransferGroup();
            if (g != null) {
                return g;
            }
        }

        // 3) global default group
        final var defaultGroupName = Cnf.at("Server", "defaultTransferGroup");
        if (isEmpty(defaultGroupName)) {
            throw new TransferServerException("No default TransferGroup defined in configuration");
        }
        final var def = dataBase.getTransferGroupObject(defaultGroupName);
        if (def == null) {
            throw new TransferServerException("Default TransferGroup " + defaultGroupName + " not found");
        }
        return def;
    }

    /**
     * Determines whether the given {@link TransferGroup} is currently available for use, meaning both:
     * <ol>
     * <li>the group itself is marked {@code active}, and</li>
     * <li>at least one of its declared {@link TransferServer} instances is:
     * <ul>
     * <li>{@code active}, and</li>
     * <li>has a reachable {@code DataMover} interface as registered in the {@link MasterServer}.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * <p>
     * This is a strict availability check used by:
     * </p>
     * <ul>
     * <li>{@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])}</li>
     * <li>{@link #tryClusterFallback(TransferGroup)}</li>
     * <li>the provider constructor before final selection</li>
     * </ul>
     *
     * <p>
     * If no server satisfies these criteria, the group is treated as unavailable, even if it is active at the
     * configuration level.
     * </p>
     *
     * <h3>Thread Safety</h3>
     * <p>
     * Thread-safe; relies only on database lookups and {@link MasterServer} interface tests.
     * </p>
     *
     * @param group
     *            the group to check
     *
     * @return {@code true} if at least one active DataMover is reachable for this group
     */
    private static boolean groupIsAvailable(final TransferGroup group) {
        return group.getActive() && Arrays.stream(MASTER.getECpdsBase().getTransferServers(group.getName()))
                .anyMatch(server -> server.getActive() && MASTER.existsClientInterface(server.getName(), "DataMover"));
    }

    /**
     * Computes a stable 32‑bit hash signature for a list of WRR candidate groups.
     *
     * <p>
     * The signature represents the <em>topology</em> of the WRR set, based on:
     * </p>
     * <ul>
     * <li>group name hash</li>
     * <li>cluster weight</li>
     * </ul>
     *
     * <p>
     * The signature allows {@link #selectGroupFromClusterRoundRobin(TransferGroup, TransferGroup[])} to detect when the
     * set of eligible WRR candidates has changed due to:
     * </p>
     * <ul>
     * <li>group activation/deactivation,</li>
     * <li>changes in cluster weights,</li>
     * <li>group availability changes,</li>
     * <li>additions/removals of groups belonging to the cluster.</li>
     * </ul>
     *
     * <p>
     * Whenever the signature changes, the cluster’s round‑robin counter is reset, ensuring stable and predictable WRR
     * behaviour across configuration updates.
     * </p>
     *
     * <h3>Implementation Details</h3>
     * <p>
     * The method constructs a long array interleaving {@code name.hashCode()} and the cluster weight for each group,
     * preserving order, then uses {@link Arrays#hashCode(long[])} to produce the final signature.
     * </p>
     *
     * @param c
     *            the list of candidate groups (already filtered and sorted)
     *
     * @return a 32‑bit hash signature representing the WRR candidate topology
     */
    private static int hashCandidates(final List<TransferGroup> c) {
        final var arr = new long[c.size() * 2];
        for (var i = 0; i < c.size(); i++) {
            arr[2 * i] = c.get(i).getName().hashCode();
            arr[2 * i + 1] = getClusterWeight(c.get(i));
        }
        return Arrays.hashCode(arr); // better distribution than String.hashCode concatenation
    }

    /**
     * Ensures that the selected {@link TransferGroup} defines at least one storage volume. All transfer groups must
     * specify at least one volume in order for volume allocation and server selection to function correctly.
     *
     * <p>
     * This method is invoked during provider construction after group resolution and fallback, and before volume
     * allocation. It prevents constructing a provider that cannot determine a valid filesystem index.
     * </p>
     *
     * <h3>Failure Conditions</h3>
     * <ul>
     * <li>Group has volume count {@code <= 0}</li>
     * </ul>
     *
     * <p>
     * In such cases, a {@link TransferServerException} is thrown.
     * </p>
     *
     * @param group
     *            the transfer group whose volume count must be validated
     *
     * @throws TransferServerException
     *             if the group defines no storage volumes
     */
    private static void assertValidVolumeCount(final TransferGroup group) throws TransferServerException {
        final var vc = group.getVolumeCount();
        if (vc <= 0) {
            throw new TransferServerException(
                    "TransferGroup " + group.getName() + " has no configured volumes (volumeCount=" + vc + ")");
        }
    }

    /**
     * Internal engine responsible for selecting a storage volume (filesystem index) for a given {@link TransferGroup}.
     * Volume selection is based on dynamic free‑space statistics continuously refreshed in the background.
     *
     * <p>
     * <b>Responsibilities:</b>
     * </p>
     * <ul>
     * <li>maintain per‑group statistics describing the current free space and effective selection weights of each
     * volume,</li>
     * <li>provide weighted random allocation based on free‑space or size‑aware metrics,</li>
     * <li>fallback to uniform random allocation when no statistics are available or when volumes are nearly
     * balanced,</li>
     * <li>run a background updater that periodically refreshes free‑space usage via
     * {@link MasterServer#computeVolumeUsageAndSortedMovers(TransferGroup, int)},</li>
     * <li>populate the {@code VOLUME_USAGE_CACHE} used by
     * {@link TransferServerProvider#getTransferServersByMostFreeSpace()}.</li>
     * </ul>
     *
     * <h3>Concurrency Model</h3>
     * <ul>
     * <li>Each transfer group has a {@link GroupStats} instance protected by a fine‑grained lock.</li>
     * <li>Shared maps are {@link ConcurrentHashMap} and safe for multi‑threaded access.</li>
     * <li>Allocation operations are lock‑free except when snapshotting prefix sums.</li>
     * <li>The background updater runs as a single‑threaded daemon.</li>
     * </ul>
     *
     * <h3>Allocator Modes</h3>
     * <ul>
     * <li><b>Standard allocator</b>: free‑space‑weighted selection.</li>
     * <li><b>Size‑aware allocator</b>: penalises free space by an amount proportional to the incoming file size
     * (tunable via configuration).</li>
     * <li><b>Uniform fallback</b>: used when no stats exist or coefficient‑of‑variation (CV) is below a configurable
     * threshold.</li>
     * </ul>
     *
     * <p>
     * This class is internal to the provider and not exposed publicly.
     * </p>
     */
    private static class WeightedAllocator {
        /** Per-group random-number generators used for volume selection. */
        private static final ConcurrentHashMap<String, SplittableRandom> RNGS = new ConcurrentHashMap<>();

        /** Map of groupName → per-group statistics (volumes, weights, prefix sums). */
        private static final ConcurrentHashMap<String, GroupStats> GROUPS = new ConcurrentHashMap<>();

        /**
         * Minimum allowable weight to ensure no volume has zero selection probability.
         */
        private static final long MIN_WEIGHT = 1L;

        /**
         * Coefficient-of-variation threshold below which all volumes are considered to have “roughly equal” free space,
         * causing a switch to uniform random selection.
         */
        private static final double CV_THRESHOLD = Cnf.at("TransferServerProvider", "cvThreshold", 0.10d);

        /**
         * Enables deterministic seeding for repeatable results across restarts when true.
         */
        private static final boolean DETERMINISTIC_RNG = Cnf.at("TransferServerProvider", "deterministicRng", false);

        /**
         * Scaling factor α controlling how much a file of size S penalises volume free space during size-aware
         * allocation.
         */
        private static final double FILE_SIZE_PENALTY_FACTOR = Cnf.at("TransferServerProvider", "fileSizePenaltyFactor",
                1.0d);

        /**
         * Background daemon thread periodically refreshing volume usage and mover ordering from MasterServer.
         */
        private static final ScheduledExecutorService GROUP_USAGE_UPDATER = Executors
                .newSingleThreadScheduledExecutor(r -> {
                    final var t = new Thread(r, "GroupUsageUpdater");
                    t.setDaemon(true);
                    return t;
                });

        static {
            if (Cnf.at("TransferServerProvider", "startUsageUpdater", true)) {
                startUsageUpdater();
                Runtime.getRuntime()
                        .addShutdownHook(new Thread(WeightedAllocator::stopUsageUpdater, "GroupUsageUpdaterShutdown"));
            }
        }

        /**
         * Holds all per‑group volume statistics necessary for weighted volume selection.
         *
         * <p>
         * A single {@link GroupStats} instance is associated with each transfer group. It contains:
         * </p>
         * <ul>
         * <li>one {@link VolumeStats} object per volume, tracking current load (used space),</li>
         * <li>one {@link LongAdder} weight per volume, derived from free space,</li>
         * <li>a prefix‑sum array enabling O(log n) weighted selection,</li>
         * <li>a private lock to protect updates from the background usage updater.</li>
         * </ul>
         *
         * <h3>Thread‑safety</h3> All write operations (load/weight updates and prefix rebuilds) are guarded by the
         * group-level lock. Read operations during allocation acquire a snapshot of prefix sums and weights under the
         * same lock to ensure consistency.
         */
        private static class GroupStats {
            final VolumeStats[] volumes;
            final LongAdder[] weights;
            long[] prefixSums;
            final Object lock = new Object();

            GroupStats(final long[] used, final long[] max) {
                volumes = new VolumeStats[used.length];
                weights = new LongAdder[used.length];
                prefixSums = new long[used.length];
                for (var i = 0; i < used.length; i++) {
                    volumes[i] = new VolumeStats(max[i]);
                    volumes[i].setLoad(used[i]);
                    weights[i] = new LongAdder();
                    weights[i].add(Math.max(MIN_WEIGHT, volumes[i].freeSpace()));
                }
                rebuildPrefix();
            }

            void rebuildPrefix() {
                synchronized (lock) {
                    var sum = 0L;
                    for (var i = 0; i < weights.length; i++) {
                        sum += weights[i].sum();
                        prefixSums[i] = sum;
                    }
                }
            }
        }

        /**
         * Represents the state of a single storage volume for a transfer group.
         *
         * <p>
         * The object tracks:
         * </p>
         * <ul>
         * <li>the maximum configured capacity ({@code maxCapacity}),</li>
         * <li>the current used space (via a {@link LongAdder}),</li>
         * <li>derived free space ({@code maxCapacity - currentLoad}).</li>
         * </ul>
         *
         * <p>
         * The updater overwrites the current load in atomic fashion using {@link LongAdder#reset()} followed by
         * {@link LongAdder#add(long)}.
         * </p>
         *
         * <h3>Thread Safety</h3>
         * <p>
         * {@link VolumeStats} itself is thread‑safe due to use of {@link LongAdder}. Access to multiple
         * {@code VolumeStats} entries is coordinated by the group lock in {@link GroupStats}.
         * </p>
         */
        private static class VolumeStats {
            final long maxCapacity;
            final LongAdder currentLoad = new LongAdder();

            VolumeStats(final long maxCapacity) {
                this.maxCapacity = maxCapacity;
            }

            long freeSpace() {
                return Math.max(0, maxCapacity - currentLoad.sum());
            }

            void setLoad(final long used) {
                currentLoad.reset();
                currentLoad.add(used);
            }
        }

        /**
         * Updates or initializes the {@link GroupStats} structure for the specified group using fresh volume usage data
         * collected by the background updater.
         *
         * <p>
         * If the group has no existing {@link GroupStats}, a new instance is created. Otherwise, only the per‑volume
         * load and derived weight values are refreshed.
         * </p>
         *
         * <p>
         * The update procedure guarantees:
         * </p>
         * <ul>
         * <li>strict consistency of all volume statistics within a group,</li>
         * <li>correct prefix‑sum rebuild after load changes,</li>
         * <li>atomic replacement of the {@code GROUPS} entry if it did not previously exist.</li>
         * </ul>
         *
         * @param group
         *            the transfer group whose usage data is refreshed
         * @param usedPerVolume
         *            array of used bytes per volume
         * @param maxCapacityPerVolume
         *            array of max capacity per volume
         *
         * @throws IllegalArgumentException
         *             if array lengths differ
         */
        public static void updateGroupUsage(final TransferGroup group, final long[] usedPerVolume,
                final long[] maxCapacityPerVolume) {
            if (usedPerVolume.length != maxCapacityPerVolume.length) {
                throw new IllegalArgumentException("usedPerVolume and maxCapacityPerVolume must have the same length. "
                        + "got used=" + usedPerVolume.length + ", max=" + maxCapacityPerVolume.length);
            }
            GROUPS.compute(group.getName(), (_, gs) -> {
                if (gs == null) {
                    return new GroupStats(usedPerVolume, maxCapacityPerVolume);
                } else {
                    updateGroupVolumes(gs, usedPerVolume);
                    return gs;
                }
            });
            if (isDebugEnabled())
                _log.debug("TransferGroup {} usage updated", group.getName());
        }

        /**
         * Refreshes load and weight statistics for an existing {@link GroupStats} instance without replacing the
         * object.
         *
         * <p>
         * This method performs the minimal mutation necessary to update:
         * </p>
         * <ul>
         * <li>{@link VolumeStats#currentLoad} for each volume,</li>
         * <li>{@link GroupStats#weights} (weight = max(MIN_WEIGHT, freeSpace)),</li>
         * <li>{@link GroupStats#prefixSums} via {@link GroupStats#rebuildPrefix()}.</li>
         * </ul>
         *
         * <p>
         * If the incoming {@code used} array length does not match the existing volume count, the update is aborted and
         * a warning is logged. This prevents partial or corrupted updates after configuration changes.
         * </p>
         *
         * <p>
         * The per‑group lock ensures that all values are updated atomically with respect to allocation calls.
         * </p>
         *
         * @param gs
         *            the mutable {@link GroupStats} instance
         * @param used
         *            array of new used‑space values (per volume)
         */
        private static void updateGroupVolumes(final GroupStats gs, final long[] used) {
            if (used.length != gs.volumes.length) {
                _log.warn("Mismatch in volume count for group");
                return;
            }
            synchronized (gs.lock) {
                for (var i = 0; i < used.length; i++) {
                    gs.volumes[i].setLoad(used[i]);
                    gs.weights[i].reset();
                    gs.weights[i].add(Math.max(MIN_WEIGHT, gs.volumes[i].freeSpace()));
                }
                gs.rebuildPrefix();
            }
        }

        /**
         * Starts the background daemon responsible for refreshing volume usage for all known transfer groups at a fixed
         * interval.
         *
         * <p>
         * The updater:
         * </p>
         * <ul>
         * <li>iterates over all transfer groups in the system,</li>
         * <li>checks group availability (via {@link TransferServerProvider#groupIsAvailable}),</li>
         * <li>for each volume of an available group, invokes
         * {@link MasterServer#computeVolumeUsageAndSortedMovers(TransferGroup, int)},</li>
         * <li>updates {@link WeightedAllocator#GROUPS} and {@link TransferServerProvider#VOLUME_USAGE_CACHE}
         * accordingly.</li>
         * </ul>
         *
         * <h3>Failure Handling</h3>
         * <ul>
         * <li>Volume update failures log warnings and skip the group update cycle.</li>
         * <li>Unexpected fatal exceptions are caught and logged without stopping the updater.</li>
         * </ul>
         *
         * <p>
         * The thread is marked as a daemon so that JVM shutdown is not delayed.
         * </p>
         *
         * <p>
         * Execution begins after an initial delay and repeats at an interval defined by the configuration parameter
         * {@code usageUpdaterFreqInSec}.
         * </p>
         */
        public static void startUsageUpdater() {
            final var frequency = Cnf.at("TransferServerProvider", "usageUpdaterFreqInSec", 10);
            GROUP_USAGE_UPDATER.scheduleAtFixedRate(() -> {
                try {
                    final var groups = MASTER.getECpdsBase().getTransferGroupArray();
                    for (final var group : groups) {
                        if (!groupIsAvailable(group))
                            continue;

                        final var groupName = group.getName();
                        final var volumeCount = group.getVolumeCount();
                        final var used = new long[volumeCount];
                        final var capacity = new long[volumeCount];

                        var allOk = true;
                        for (var volumeIndex = 0; volumeIndex < volumeCount; volumeIndex++) {
                            try {
                                final var result = MASTER.computeVolumeUsageAndSortedMovers(group, volumeIndex);
                                if (result == null || result.aggregatedUsage == null
                                        || result.aggregatedUsage.length != 2) {
                                    _log.warn("Invalid volume usage result for group {}", groupName);
                                    allOk = false;
                                    break;
                                }
                                final var usedArr = result.aggregatedUsage[0];
                                final var capArr = result.aggregatedUsage[1];
                                if (usedArr.length != volumeCount || capArr.length != volumeCount) {
                                    _log.warn("Volume usage size mismatch for group {}", groupName);
                                    allOk = false;
                                    break;
                                }
                                VOLUME_USAGE_CACHE.put(groupName + ":" + volumeIndex, result);
                                used[volumeIndex] = usedArr[volumeIndex];
                                capacity[volumeIndex] = capArr[volumeIndex];
                            } catch (final Throwable e) {
                                _log.warn("Error computing usage for {} volume {}", groupName, volumeIndex, e);
                                allOk = false;
                                break;
                            }
                        }

                        if (allOk) {
                            updateGroupUsage(group, used, capacity);
                            if (isDebugEnabled()) {
                                final var usedSum = Arrays.stream(used).sum();
                                final var totalSum = Arrays.stream(capacity).sum();
                                final var percentUsed = totalSum > 0 ? (usedSum * 100.0 / totalSum) : 0.0;
                                _log.debug("Group {} usage updated: used={}, total={}, {}%", groupName,
                                        Arrays.toString(used), Arrays.toString(capacity),
                                        String.format("%.2f", percentUsed));
                            }
                        } else {
                            _log.warn("Skipping usage update for group {} due to partial/invalid data", groupName);
                        }
                    }
                } catch (final Throwable t) {
                    _log.error("Global usage update failed", t);
                }
            }, frequency, frequency, TimeUnit.SECONDS);
        }

        /**
         * Selects a volume index for the given group using free‑space‑weighted random selection.
         *
         * <h3>Allocation Strategy</h3>
         * <ol>
         * <li>If the group has no volumes, returns 0 (fallback).</li>
         *
         * <li>If no {@link GroupStats} exist yet for the group, returns a uniform‑random volume index as a safety
         * fallback.</li>
         *
         * <li>Otherwise:
         * <ul>
         * <li>snapshot the per‑volume weights and prefix sums under group lock,</li>
         * <li>if coefficient‑of‑variation (CV) between weights is below {@code CV_THRESHOLD}, use uniform random
         * selection,</li>
         * <li>else, use weighted random selection over the prefix sum distribution.</li>
         * </ul>
         * </li>
         * </ol>
         *
         * <h3>RNG Source</h3>
         * <p>
         * Each group has a dedicated {@link SplittableRandom}, seeded either deterministically (if enabled in config)
         * or via a hash of bytecode identity, system nanotime, and runtime salt.
         * </p>
         *
         * @param group
         *            the transfer group whose volumes are being allocated
         *
         * @return the selected volume index in {@code [0, group.getVolumeCount())}
         */
        public static int allocate(final TransferGroup group) {
            final var groupName = group.getName();
            final var random = RNGS.computeIfAbsent(groupName, g -> {
                if (DETERMINISTIC_RNG) {
                    return new SplittableRandom(g.hashCode());
                } else {
                    return new SplittableRandom(((long) g.hashCode()) ^ System.nanoTime() ^ RUNTIME_SALT);
                }
            });
            final var vc = group.getVolumeCount();
            if (vc <= 0) {
                if (isDebugEnabled())
                    _log.debug("Group {} has no volumes - cannot allocate", groupName);
                return 0;
            }
            final var gs = GROUPS.get(groupName);
            if (gs == null) {
                if (isDebugEnabled())
                    _log.debug("Fallback: group {} not registered -> uniform random selection", groupName);
                return random.nextInt(vc);
            }

            long[] prefix;
            long totalWeight;
            long[] weights;
            synchronized (gs.lock) {
                prefix = gs.prefixSums.clone();
                totalWeight = prefix[prefix.length - 1];
                weights = Arrays.stream(gs.weights).mapToLong(LongAdder::sum).toArray();
            }
            final var n = weights.length;
            if (n <= 1)
                return 0;

            var sum = 0L;
            for (final long w : weights)
                sum += w;

            if (sum == 0) {
                final var chosen = random.nextInt(n);
                if (isDebugEnabled()) {
                    _log.debug("All weights zero for group {} -> randomly selected index {}", groupName, chosen);
                }
                return chosen;
            }

            final var mean = (double) sum / n;
            var variance = 0.0;
            for (final long w : weights) {
                final var d = w - mean;
                variance += d * d;
            }
            variance /= n;
            final var stddev = Math.sqrt(variance);
            final var cv = stddev / mean;

            if (cv <= CV_THRESHOLD) {
                final var chosen = random.nextInt(n);
                if (isDebugEnabled()) {
                    _log.debug(
                            "Weights roughly equal for group {} (mean={}, stddev={}, cv={}). Randomly selected index {}.",
                            groupName, mean, stddev, cv, chosen);
                }
                return chosen;
            }

            // Weighted random using prefix sums
            final var r = (long) (random.nextDouble() * totalWeight);
            int low = 0, high = prefix.length - 1;
            while (low < high) {
                final var mid = (low + high) >>> 1;
                if (r < prefix[mid])
                    high = mid;
                else
                    low = mid + 1;
            }
            if (isDebugEnabled()) {
                _log.debug("Weighted allocation for group {}: selected index {} (r={}, totalWeight={}, weights={})",
                        groupName, low, r, totalWeight, Arrays.toString(weights));
            }
            return low;
        }

        /**
         * Selects a volume index using size‑aware allocation, where larger files impose a penalty on volumes with less
         * free space.
         *
         * <p>
         * If {@code fileSize <= 0}, this method delegates to {@link #allocate(TransferGroup)}.
         * </p>
         *
         * <h3>Effective Weight Calculation</h3> For each volume:
         *
         * <pre>
         *   baseFreeSpace = volume.freeSpace()
         *   penalty       = min(baseFreeSpace, alpha * fileSize)
         *   effectiveFS   = max(1, baseFreeSpace - penalty)
         * </pre>
         *
         * <p>
         * Here {@code alpha} is the configuration parameter {@code fileSizePenaltyFactor}.
         * </p>
         *
         * <p>
         * The effective free‑space values determine volume selection using the same CV‑based uniform fallback and
         * weighted random prefix mechanism as the standard allocator.
         * </p>
         *
         * <h3>Use Cases</h3>
         * <ul>
         * <li>prevent large files from clustering on the same volume,</li>
         * <li>smooth usage across volumes when some are nearly full,</li>
         * <li>avoid disproportionately penalising volumes already near capacity.</li>
         * </ul>
         *
         * @param group
         *            the transfer group whose volumes are being allocated
         * @param fileSize
         *            size of the incoming file in bytes
         *
         * @return the selected volume index
         */
        public static int allocate(final TransferGroup group, final long fileSize) {
            if (fileSize <= 0) {
                // Fallback to standard allocator for unknown file sizes
                return allocate(group);
            }

            final var groupName = group.getName();
            final var random = RNGS.computeIfAbsent(groupName, g -> {
                if (DETERMINISTIC_RNG) {
                    return new SplittableRandom(g.hashCode());
                } else {
                    return new SplittableRandom(((long) g.hashCode()) ^ System.nanoTime() ^ RUNTIME_SALT);
                }
            });

            final var vc = group.getVolumeCount();
            if (vc <= 0) {
                if (isDebugEnabled())
                    _log.debug("Group {} has no volumes - cannot allocate", groupName);
                return 0;
            }
            final var gs = GROUPS.get(groupName);
            if (gs == null) {
                if (isDebugEnabled())
                    _log.debug("Fallback: group {} not registered -> uniform random selection", groupName);
                return random.nextInt(vc);
            }

            // Fetch freeSpace[] snapshot safely
            long[] free;
            synchronized (gs.lock) {
                free = Arrays.stream(gs.volumes).mapToLong(VolumeStats::freeSpace).toArray();
            }

            final var n = free.length;
            if (n <= 1)
                return 0;

            // Compute size-aware effective free space
            final var effective = new double[n];
            final var alpha = FILE_SIZE_PENALTY_FACTOR;

            for (var i = 0; i < n; i++) {
                final var fs = free[i];
                final var penalty = (long) Math.min(fs, alpha * fileSize);
                var eff = fs - penalty;
                if (eff < 1L)
                    eff = 1L; // avoid zero or negative weights
                effective[i] = eff;
            }

            // Optional CV check
            var sum = 0.0;
            for (final double v : effective)
                sum += v;
            final var mean = sum / n;

            if (mean > 0) {
                var variance = 0.0;
                for (final double v : effective) {
                    final var d = v - mean;
                    variance += d * d;
                }
                variance /= n;
                final var stddev = Math.sqrt(variance);
                final var cv = stddev / mean;

                if (cv <= CV_THRESHOLD) {
                    final var chosen = random.nextInt(n);
                    if (isDebugEnabled()) {
                        _log.debug(
                                "Size-aware weights roughly equal for group {} (size={}, cv={}). Uniform selected index {}.",
                                groupName, fileSize, cv, chosen);
                    }
                    return chosen;
                }
            }

            // Weighted random selection
            final var prefix = new double[n];
            var cumulative = 0.0;
            for (var i = 0; i < n; i++) {
                cumulative += effective[i];
                prefix[i] = cumulative;
            }

            final var r = random.nextDouble() * cumulative;

            int low = 0, high = prefix.length - 1;
            while (low < high) {
                final var mid = (low + high) >>> 1;
                if (r < prefix[mid])
                    high = mid;
                else
                    low = mid + 1;
            }

            if (isDebugEnabled()) {
                _log.debug("Size-aware weighted allocation for group {}: fileSize={}, chosen={}, weights={}", groupName,
                        fileSize, low, Arrays.toString(effective));
            }

            return low;
        }

        /**
         * Gracefully shuts down the background usage updater.
         *
         * <p>
         * The method attempts to stop the executor service, waiting briefly for ongoing tasks to complete. If
         * termination does not occur within the timeout, a forced shutdown via
         * {@link ScheduledExecutorService#shutdownNow()} is issued.
         * </p>
         *
         * <p>
         * Intended to be invoked by the JVM shutdown hook registered in the static initializer of
         * {@link WeightedAllocator}.
         * </p>
         */
        public static void stopUsageUpdater() {
            GROUP_USAGE_UPDATER.shutdown();
            try {
                if (!GROUP_USAGE_UPDATER.awaitTermination(5, TimeUnit.SECONDS)) {
                    GROUP_USAGE_UPDATER.shutdownNow();
                }
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
                GROUP_USAGE_UPDATER.shutdownNow();
            }
        }

        /**
         * Returns whether allocator-level debug logging is enabled. This requires:
         * <ul>
         * <li>the logger to be in debug mode, and</li>
         * <li>the configuration flag {@code TransferServerProvider.debug = true}.</li>
         * </ul>
         *
         * <p>
         * This prevents excessive debug output during normal operation.
         * </p>
         */
        private static boolean isDebugEnabled() {
            return _log.isDebugEnabled() && Cnf.at("TransferServerProvider", "debug", false);
        }
    }
}

/*
 * === Lifecycle & Concurrency Notes ===
 *
 * - Construction: * Resolves group and may apply cluster WRR fallback. * Selects a volume (explicit or weighted
 * allocator). * Computes the final least-activity ordering (immutable thereafter).
 *
 * - Background Updater: * Singleton daemon periodically refreshes per-volume usage and mover orderings. * Populates
 * allocator stats (GROUPS) and VOLUME_USAGE_CACHE. * Resilient to partial failures; logs warnings and skips partial
 * updates.
 *
 * - Thread Safety: * Public instance methods are read-only and safe for concurrent callers. * CLUSTERS and SERVER_RR
 * use concurrent maps and atomics. * WeightedAllocator uses per-group locks for consistent snapshots.
 *
 * - Stability: * Cluster WRR resets on topology changes (weights/availability). * Caller-stable rotation includes a
 * runtime salt to prevent cross-restart alignment.
 */
