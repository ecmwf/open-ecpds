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
     * Gets the transfer servers.
     *
     * @return the transfer servers
     */
    public List<TransferServer> getTransferServers() {
        return servers;
    }

    /**
     * Instantiates a new transfer server provider.
     *
     * @param caller
     *            the caller
     * @param checkCluster
     *            allow specifying if we should check if the transfer group is part of a cluster. If it is the case then
     *            we should allocate one DataMover among the DataMovers who belong to all the cluster. If a transfer
     *            group is provided as a parameter then this parameter is not used.
     * @param allocatedFileSystem
     *            the allocated file system. If it is set to -1 then it will not be used when getting the list of active
     *            DataMovers. If it is set to null then it will be automatically allocated.
     * @param transferGroup
     *            the transfer group
     * @param destination
     *            the destination
     * @param master
     *            the master
     * @param primaryHost
     *            allow forcing the primary host
     *
     * @throws TransferServerException
     *             the transfer server exception
     * @throws DataBaseException
     *             the data base exception
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
     * Weighted allocator for distributing data across multiple volumes in a group. Volumes with more free space are
     * more likely to be selected.
     */
    private static class WeightedAllocator {
        private static final ConcurrentHashMap<String, SecureRandom> RNGS = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<String, GroupStats> GROUPS = new ConcurrentHashMap<>();
        private static final long MIN_WEIGHT = 1L;
        private static final ScheduledExecutorService GROUP_USAGE_UPDATER = Executors
                .newSingleThreadScheduledExecutor(r -> {
                    final var t = new Thread(r, "GroupUsageUpdater");
                    t.setDaemon(true);
                    return t;
                });

        static {
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
            _log.debug("rmiUpdateGroupUsage: TransferGroup {} usage updated.", group.getName());
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
         * Starts periodic updates of group usage every minute. Requires MASTER to be initialised and accessible.
         */
        public static void startUsageUpdater() {
            GROUP_USAGE_UPDATER.scheduleAtFixedRate(() -> {
                try {
                    final var groups = MASTER.getECpdsBase().getTransferGroupArray();
                    for (final var group : groups) {
                        if (!groupIsAvailable(group))
                            continue;
                        final var groupName = group.getName();
                        try {
                            final var usage = MASTER.computeVolumeUsage(group);
                            if (usage == null || usage.length != 2) {
                                _log.warn("Invalid usage array for group {}", groupName);
                                continue;
                            }
                            final var volumeCount = group.getVolumeCount();
                            if (usage[0].length != volumeCount || usage[1].length != volumeCount) {
                                _log.warn("Usage array does not match volume count for group {}", groupName);
                                continue;
                            }
                            final var used = usage[0]; // volumeCount long[] (used space per volume)
                            final var max = usage[1]; // volumeCount long[] (capacity per volume)
                            updateGroupUsage(group, used, max);
                        } catch (final Throwable e) {
                            _log.warn("Error updating usage for group {}", groupName, e);
                        }
                    }
                } catch (final Throwable t) {
                    _log.error("Global usage update failed", t);
                }
            }, 15, 30, TimeUnit.SECONDS);
        }

        /** Allocate a volume index for a group */
        public static int allocate(final TransferGroup group) {
            final var groupName = group.getName();
            final var random = RNGS.computeIfAbsent(groupName, _ -> new SecureRandom());
            final var gs = GROUPS.get(groupName);
            if (gs == null) {
                _log.debug("Fallback: group {} not registered -> uniform random selection", groupName);
                return random.nextInt(group.getVolumeCount());
            }
            long[] prefix;
            long totalWeight;
            synchronized (gs.lock) {
                prefix = gs.prefixSums.clone();
                totalWeight = prefix[prefix.length - 1];
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
            return low;
        }
    }
}
