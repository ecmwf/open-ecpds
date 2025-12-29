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

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECPDS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEBUG;
import static ecmwf.common.text.Util.isNotEmpty;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterServer;

/**
 * The Class TransferServerProvider.
 */
public final class TransferServerProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TransferServerProvider.class);

    /** The Constant MASTER. */
    private static final MasterServer MASTER = StarterServer.getInstance(MasterServer.class);

    /** The Constant VOLUME_USAGE_CACHE. */
    private static final ConcurrentHashMap<String, MasterServer.VolumeUsageResult> VOLUME_USAGE_CACHE = new ConcurrentHashMap<>();

    /** The servers. */
    private final List<TransferServer> servers = new ArrayList<>();

    /** The group. */
    private TransferGroup group = null;

    /** The file system. */
    private int fileSystem = -1;

    /**
     * The Class TransferServerException.
     */
    public static final class TransferServerException extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -5979035504811469692L;

        /**
         * Instantiates a new transfer server exception.
         *
         * @param message
         *            the message
         */
        TransferServerException(final String message) {
            super(message);
        }
    }

    /**
     * Gets the file system.
     *
     * @return the file system
     */
    public int getFileSystem() {
        return fileSystem;
    }

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     */
    public TransferGroup getTransferGroup() {
        return group;
    }

    /**
     * Returns the list of {@link TransferServer} instances for the current transfer group and file system (volume
     * index).
     * <p>
     * If volume usage statistics are available in the internal cache for the current group and volume index, the
     * servers are ordered by increasing usage (least used first) for that volume. Otherwise, the servers are returned
     * in their default order.
     * <p>
     * The returned list is mutable and reflects the internal state of this provider. The ordering may change as volume
     * usage statistics are updated in the background.
     * <p>
     * <b>Thread safety:</b> This method is not thread-safe.
     *
     * @return a list of active {@link TransferServer} instances for the current group and file system, ordered by least
     *         used volume if available
     */
    public List<TransferServer> getTransferServers() {
        final var key = group.getName() + ":" + fileSystem;
        final var cached = VOLUME_USAGE_CACHE.get(key);
        if (cached == null || cached.moversSortedByUsage == null) {
            return servers;
        }
        final var order = cached.moversSortedByUsage;
        final var index = new ConcurrentHashMap<String, Integer>();
        for (var i = 0; i < order.length; i++) {
            index.put(order[i], i);
        }
        servers.sort((a, b) -> {
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
        return servers;
    }

    /**
     * Constructs a new {@code TransferServerProvider} for the specified transfer group, destination, and file system.
     * <p>
     * This constructor determines the appropriate {@link TransferGroup} and file system (volume index) to use for data
     * transfer, optionally considering cluster membership and primary host preferences. It then retrieves the list of
     * active {@link TransferServer} instances for the selected group and file system. If no suitable servers are
     * available, an exception is thrown.
     * <p>
     * <b>Parameters:</b>
     * <ul>
     * <li>{@code caller} - the name of the calling component (used for logging and context)</li>
     * <li>{@code checkCluster} - if true, checks if the transfer group is part of a cluster and selects a group
     * accordingly</li>
     * <li>{@code allocatedFileSystem} - the file system (volume index) to use; if null, one is allocated
     * automatically</li>
     * <li>{@code transferGroup} - the name of the transfer group to use, or null to auto-select</li>
     * <li>{@code destination} - the destination name for which to find a transfer group and servers</li>
     * <li>{@code primaryHost} - an optional primary host to force selection of a specific group</li>
     * </ul>
     *
     * <b>Behavior:</b>
     * <ul>
     * <li>If {@code transferGroup} is provided and available, it is used; otherwise, cluster selection may occur.</li>
     * <li>If {@code primaryHost} is provided, its group is used if available.</li>
     * <li>If no group is available, a default is selected from configuration.</li>
     * <li>The list of active servers is retrieved for the selected group and file system.</li>
     * </ul>
     *
     * <b>Exceptions:</b>
     * <ul>
     * <li>{@link TransferServerException} if no suitable group or servers are found, or if the group is
     * unavailable</li>
     * <li>{@link DataBaseException} if a database error occurs during lookup</li>
     * </ul>
     *
     * <b>Side effects:</b> The internal state of this provider is initialised, including the group, file system, and
     * server list.
     */
    public TransferServerProvider(final String caller, boolean checkCluster, final Integer allocatedFileSystem,
            final String transferGroup, final String destination, final Host primaryHost)
            throws TransferServerException, DataBaseException {
        final var dataBase = MASTER.getECpdsBase();
        if (transferGroup != null && primaryHost == null) {
            // A transfer group was specified and we don't have a default primary host, so
            // let's use it if we can!
            group = dataBase.getTransferGroup(transferGroup);
            if (group == null) {
                throw new TransferServerException("TransferGroup " + transferGroup + " not found");
            }
            if (!groupIsAvailable(group)) {
                // The group provided is not available so force the checking of the
                // cluster and let's hope that another group from this cluster
                // is available!
                _log.warn("Force cluster checking as {} is not available", transferGroup);
                checkCluster = true;
            }
        } else {
            // There was no transfer group specified so we have to find one! Either one was
            // provided, or we look for the dissemination ones attached to this destination!
            final var hosts = primaryHost != null ? new Host[] { primaryHost }
                    : dataBase.getDestinationHost(destination, HostOption.DISSEMINATION);
            if (hosts.length == 0) {
                // We have no hosts defined for this Destination so let's get
                // the default transfer group from the Destination!
                if ((group = dataBase.getDestination(destination).getTransferGroup()) == null) {
                    // There is no default transfer group for this Destination
                    // so let's take the default value from the configuration!
                    final var defaultGroup = Cnf.at("Server", "defaultTransferGroup");
                    if (defaultGroup == null) {
                        throw new TransferServerException("No host(s) defined for " + destination);
                    }
                    group = dataBase.getTransferGroup(defaultGroup);
                    if (group == null) {
                        throw new TransferServerException("Default TransferGroup " + defaultGroup + " not found");
                    }
                }
            } else {
                // We have some hosts defined so we should be able to find a
                // transfer group from the primary host!
                final var primary = hosts[0];
                group = primary.getTransferGroup();
                // If no TransferGroup is defined then we can not proceed!
                if (group == null) {
                    throw new TransferServerException("No TransferGroup defined for Host " + primary.getNickname());
                }
                // Is there a request to use a specific data mover?
                try {
                    final var setup = HOST_ECPDS.getECtransSetup(primary.getData());
                    final var moverList = setup.getString(ECtransOptions.HOST_ECPDS_MOVER_LIST_FOR_PROCESSING);
                    if (!moverList.isEmpty()) {
                        // Get one of the mandatory mover!
                        final var server = TransferScheduler.getTransferServerName(setup.getBoolean(HOST_ECTRANS_DEBUG),
                                group.getName(), null, moverList);
                        if (server != null) {
                            // We know the group and the server are active and
                            // connected (checked in getTransferServerName)!
                            group = server.getTransferGroup();
                            fileSystem = allocatedFileSystem != null ? allocatedFileSystem
                                    : WeightedAllocator.allocate(group);
                            servers.add(server);
                            _log.debug("Force usage of {} in {}", server.getName(), group.getName());
                            // No more processing required!
                            return;
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Could not find mandatory TransferServer", t);
                }
            }
            checkCluster = true;
        }
        if (checkCluster) {
            // See if the Transfer Group we found is part of a Cluster and if
            // this is the case then let's pick up a random TransferGroup from
            // the Cluster according to the weight
            final var clusterName = group.getClusterName();
            if (isNotEmpty(clusterName) && group.getClusterWeight() != null) {
                group = getRandomGroupFromCluster(group, dataBase.getTransferGroupArray());
                _log.debug("Choosing TransferGroup {} from Cluster {}", group.getName(), clusterName);
            }
        }
        // This shouldn't happen but let's check if the TransferGroup is available
        // just in case!
        if (!groupIsAvailable(group)) {
            throw new TransferServerException("TransferGroup " + group.getName() + " not available");
        }
        // Select one of the file system available for this group!
        fileSystem = allocatedFileSystem != null ? allocatedFileSystem : WeightedAllocator.allocate(group);
        // Now gets the list of active TransferServers for the selected
        // TransferGroup
        servers.addAll(MASTER.getActiveTransferServers("TransferServerProvider." + caller, null, group, fileSystem));
        // And check if we have something? (all the TransferServers might be
        // down)
        if (servers.isEmpty()) {
            throw new TransferServerException("No TransferServer(s) available for TransferGroup " + group.getName());
        }
    }

    /**
     * Check if the given transfer group is active and has at least one transfer server connected to the master.
     *
     * @param transferGroups
     *            the transfer groups
     *
     * @return the transfer group is available for use
     */
    public static boolean groupIsAvailable(final TransferGroup group) {
        return group.getActive() && Arrays.stream(MASTER.getECpdsBase().getTransferServers(group.getName()))
                .anyMatch(server -> server.getActive() && MASTER.existsClientInterface(server.getName(), "DataMover"));
    }

    /**
     * Get a random weighted selection of a TransferGroup from a Cluster.
     *
     * @param original
     *            the original
     * @param transferGroups
     *            the transfer groups
     *
     * @return the transfer group
     */
    public static TransferGroup getRandomGroupFromCluster(final TransferGroup original,
            final TransferGroup[] transferGroups) {
        final var clusterName = original.getClusterName();
        // What is the sum of all the weightings?
        var total = 0;
        for (final TransferGroup group : transferGroups) {
            if (clusterName.equals(group.getClusterName()) && group.getClusterWeight() != null
                    && groupIsAvailable(group)) {
                total += group.getClusterWeight();
            }
        }
        if (total > 0) {
            // Select a random value between 0 and the total
            final var random = ThreadLocalRandom.current().nextInt(total);
            // Loop through the weightings list until the correct one is found!
            var current = 0;
            for (final TransferGroup group : transferGroups) {
                if (clusterName.equals(group.getClusterName()) && group.getClusterWeight() != null
                        && groupIsAvailable(group)) {
                    current += group.getClusterWeight();
                    if (random < current) {
                        return group;
                    }
                }
            }
        }
        // No other TransferGroup found from the Cluster!
        return original;
    }

    /**
     * Utility class for weighted allocation and tracking of volume usage within a {@link TransferGroup}.
     * <p>
     * This class maintains per-group statistics about volume usage and free space, and provides methods for allocating
     * a volume index in a way that favors volumes with more available space. It also supports periodic updates of usage
     * statistics and integrates with the global usage cache for transfer groups.
     * <p>
     * <b>Thread safety:</b> All internal state is managed using concurrent data structures and explicit
     * synchronisation, making this class safe for concurrent use.
     * <p>
     * <b>Design:</b>
     * <ul>
     * <li>Each group is tracked by name, with per-volume statistics and weights.</li>
     * <li>Allocation is randomised but weighted by available free space.</li>
     * <li>If all volumes are roughly equally used, allocation is uniform random among candidates.</li>
     * <li>Periodic background updates keep usage statistics fresh.</li>
     * </ul>
     * <p>
     * <b>Usage:</b> Call {@link #allocate(TransferGroup)} to select a volume index for a group, and
     * {@link #updateGroupUsage(TransferGroup, long[], long[])} to refresh usage statistics.
     */
    private static class WeightedAllocator {
        private static final ConcurrentHashMap<String, SecureRandom> RNGS = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<String, GroupStats> GROUPS = new ConcurrentHashMap<>();
        private static final long MIN_WEIGHT = 1L;
        private static final double EPSILON_RATIO = 0.01; // 1% tolerance for 'roughly equal' weights
        private static final ScheduledExecutorService GROUP_USAGE_UPDATER = Executors
                .newSingleThreadScheduledExecutor(r -> {
                    final var t = new Thread(r, "GroupUsageUpdater");
                    t.setDaemon(true);
                    return t;
                });

        static {
            if (Cnf.at("TransferServerProvider", "startUsageUpdater", true))
                startUsageUpdater();
        }

        private static class GroupStats {
            final VolumeStats[] volumes;
            final LongAdder[] weights;
            long[] prefixSums;
            final Object lock = new Object();

            GroupStats(final int volumeCount, final long maxCapacityPerVolume) {
                volumes = new VolumeStats[volumeCount];
                weights = new LongAdder[volumeCount];
                prefixSums = new long[volumeCount];
                for (var i = 0; i < volumeCount; i++) {
                    volumes[i] = new VolumeStats(maxCapacityPerVolume);
                    weights[i] = new LongAdder();
                    weights[i].add(maxCapacityPerVolume);
                    prefixSums[i] = (i == 0) ? weights[i].sum() : prefixSums[i - 1] + weights[i].sum();
                }
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

        /** Initialise a new group */
        private static GroupStats initializeGroup(final long[] used, final long[] max) {
            final var gs = new GroupStats(used.length, 0);
            synchronized (gs.lock) {
                for (var i = 0; i < used.length; i++) {
                    gs.volumes[i] = new VolumeStats(max[i]);
                    gs.volumes[i].setLoad(used[i]);
                    gs.weights[i].reset();
                    gs.weights[i].add(Math.max(MIN_WEIGHT, gs.volumes[i].freeSpace()));
                }
                gs.rebuildPrefix();
            }
            return gs;
        }

        /**
         * Update the usage for a transfer group, registering it if necessary.
         */
        public static void updateGroupUsage(final TransferGroup group, final long[] usedPerVolume,
                final long[] maxCapacityPerVolume) {
            if (usedPerVolume.length != maxCapacityPerVolume.length) {
                throw new IllegalArgumentException("usedPerVolume and maxCapacityPerVolume must have the same length. "
                        + "got used=" + usedPerVolume.length + ", max=" + maxCapacityPerVolume.length);
            }
            GROUPS.compute(group.getName(), (_, gs) -> {
                if (gs == null) {
                    return initializeGroup(usedPerVolume, maxCapacityPerVolume);
                } else {
                    updateGroupVolumes(gs, usedPerVolume);
                    return gs;
                }
            });
            if (isDebugEnabled())
                _log.debug("TransferGroup {} usage updated", group.getName());
        }

        /** Update existing group volumes */
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
         * Starts periodic updates of group usage every configured interval. Uses
         * MASTER.computeVolumeUsageAndSortedMovers() to retrieve both aggregated volume usage and mover ordering, which
         * are cached and reused for server selection and ordering.
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
                        try {
                            // Volume index used for sorting movers; typically matches allocated filesystem
                            final var volumeIndex = 0;
                            final var result = MASTER.computeVolumeUsageAndSortedMovers(group, volumeIndex);
                            if (result == null || result.aggregatedUsage == null
                                    || result.aggregatedUsage.length != 2) {
                                _log.warn("Invalid volume usage result for group {}", groupName);
                                continue;
                            }
                            final var volumeCount = group.getVolumeCount();
                            final var used = result.aggregatedUsage[0];
                            final var capacity = result.aggregatedUsage[1];
                            if (used.length != volumeCount || capacity.length != volumeCount) {
                                _log.warn("Volume usage size mismatch for group {}", groupName);
                                continue;
                            }
                            // Cache the full result (including mover ordering)
                            VOLUME_USAGE_CACHE.put(groupName + ":" + volumeIndex, result);
                            // Update weighted allocator
                            updateGroupUsage(group, used, capacity);
                            if (isDebugEnabled()) {
                                final var usedSum = Arrays.stream(used).sum();
                                final var totalSum = Arrays.stream(capacity).sum();
                                final var percentUsed = (totalSum > 0) ? (usedSum * 100.0 / totalSum) : 0.0;
                                _log.debug("Group {} usage updated: used={}, total={}, used%={}, movers={}", groupName,
                                        Arrays.toString(used), Arrays.toString(capacity),
                                        String.format("%.2f%%", percentUsed),
                                        Arrays.toString(result.moversSortedByUsage));
                            }
                        } catch (final Throwable e) {
                            _log.warn("Error updating usage for group {}", groupName, e);
                        }
                    }
                } catch (final Throwable t) {
                    _log.error("Global usage update failed", t);
                }
            }, frequency, frequency, TimeUnit.SECONDS);
        }

        /** Allocate a volume index for a group */
        public static int allocate(final TransferGroup group) {
            final var groupName = group.getName();
            final var random = RNGS.computeIfAbsent(groupName, _ -> new SecureRandom());
            final var gs = GROUPS.get(groupName);
            if (gs == null) {
                if (isDebugEnabled())
                    _log.debug("Fallback: group {} not registered -> uniform random selection", groupName);
                return random.nextInt(group.getVolumeCount());
            }
            long[] prefix;
            long totalWeight;
            long[] weights;
            synchronized (gs.lock) {
                prefix = gs.prefixSums.clone();
                totalWeight = prefix[prefix.length - 1];
                weights = Arrays.stream(gs.weights).mapToLong(LongAdder::sum).toArray();
            }
            final var minWeight = Arrays.stream(weights).min().orElse(0);
            final var maxWeight = Arrays.stream(weights).max().orElse(0);
            final var epsilon = Math.max(1, maxWeight * EPSILON_RATIO);
            final var roughlyEqual = (maxWeight - minWeight) <= epsilon;
            if (roughlyEqual) {
                final List<Integer> candidates = new ArrayList<>();
                for (var i = 0; i < weights.length; i++) {
                    if (Math.abs(weights[i] - maxWeight) <= epsilon) {
                        candidates.add(i);
                    }
                }
                final int chosen = candidates.get(random.nextInt(candidates.size()));
                if (isDebugEnabled()) {
                    _log.debug(
                            "Weights roughly equal for group {} (max={}, min={}, epsilon={}). Randomly selected index {} among {} candidates.",
                            groupName, maxWeight, minWeight, epsilon, chosen, candidates.size());
                }
                return chosen;
            }
            final var r = (long) (random.nextDouble() * totalWeight);
            // Binary search over prefix sums
            var low = 0;
            var high = prefix.length - 1;
            while (low < high) {
                final var mid = (low + high) / 2;
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

        private static boolean isDebugEnabled() {
            return _log.isDebugEnabled() && Cnf.at("TransferServerProvider", "debug", false);
        }
    }
}
