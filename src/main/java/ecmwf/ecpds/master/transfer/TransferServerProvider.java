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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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

    /** List of current file system selected for each TransferGroup. */
    private static final Map<String, SecureRandom> FILE_SYSTEMS = new ConcurrentHashMap<>();

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
     * Find the current data volume and increment the new value.
     *
     * @param group
     *            the group
     *
     * @return the int
     */
    private static int _allocateFileSystem(final TransferGroup group) {
        final var groupName = group.getName();
        SecureRandom random;
        synchronized (FILE_SYSTEMS) {
            if ((random = FILE_SYSTEMS.get(groupName)) == null) {
                FILE_SYSTEMS.put(groupName, random = new SecureRandom());
            }
        }
        final var index = random.nextInt(group.getVolumeCount());
        _log.debug("Selected volume for " + groupName + ": " + index);
        return index;
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
                _log.warn("Force cluster checking as " + transferGroup + " is not available");
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
                    if (moverList.length() > 0) {
                        // Get one of the mandatory mover!
                        final var server = TransferScheduler.getTransferServerName(setup.getBoolean(HOST_ECTRANS_DEBUG),
                                group.getName(), null, moverList);
                        if (server != null) {
                            // We know the group and the server are active and
                            // connected (checked in getTransferServerName)!
                            group = server.getTransferGroup();
                            fileSystem = allocatedFileSystem != null ? allocatedFileSystem : _allocateFileSystem(group);
                            servers.add(server);
                            _log.debug("Force usage of " + server.getName() + " in " + group.getName());
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
                _log.debug("Choosing TransferGroup " + group.getName() + " from Cluster " + clusterName);
            }
        }
        // This shouldn't happen but let's check if the TransferGroup is available
        // just in case!
        if (!groupIsAvailable(group)) {
            throw new TransferServerException("TransferGroup " + group.getName() + " not available");
        }
        // Select one of the file system available for this group!
        fileSystem = allocatedFileSystem != null ? allocatedFileSystem : _allocateFileSystem(group);
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
}
