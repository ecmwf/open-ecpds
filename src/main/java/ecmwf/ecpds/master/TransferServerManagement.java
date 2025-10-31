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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferServer;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.transfer.TransferScheduler;
import ecmwf.ecpds.master.transfer.TransferServerProvider;

/**
 * The Class TransferServerManagement.
 */
final class TransferServerManagement {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TransferServerManagement.class);

    /** List of index per caller. */
    private final HashMap<String, SecureRandom> activeServersIndex = new HashMap<>();

    /**
     * We need a reference to the MasterServer for accessing the DataBase and various services.
     */
    private final MasterServer master;

    /** Access to the DataBase is required. */
    private final ECpdsBase base;

    /**
     * Instantiates a new transfer server management.
     *
     * @param master
     *            the master
     */
    TransferServerManagement(final MasterServer master) {
        this.base = master.getECpdsBase();
        this.master = master;
    }

    /**
     * Get the list of TransferServers available and active! If a fileSystem is provided then list the TransferServers
     * by activity for this fileSystem, the lesser used the higher the priority. If a preferred TransferServer is
     * provided and is found to be available and active then it is placed first in the result. The TransferGroup
     * parameter is required and allow this method to select the list of TransferServers for this group. The caller
     * parameter allow recognizing which method is calling and making sure in case of load-balancing we return a
     * consistent answer across the requests for this particular method.
     *
     * @param caller
     *            the caller
     * @param original
     *            the original
     * @param originalTransferGroup
     *            the original transfer group
     * @param fileSystem
     *            the file system
     *
     * @return the active transfer servers
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public List<TransferServer> getActiveTransferServers(final String caller, final TransferServer original,
            final TransferGroup originalTransferGroup, final Integer fileSystem) throws DataBaseException {
        // Let's get the list of TransferServers declared for the original TransferGroup
        // in the DataBase!
        var group = originalTransferGroup;
        var servers = base.getTransferServers(group.getName());
        if (servers.length == 0) {
            // See if the original Transfer Group is part of a Cluster and if
            // this is the case then let's pick up a random TransferGroup from
            // the Cluster according to the weight, rather than failing!
            final var clusterName = group.getClusterName();
            if (isNotEmpty(clusterName) && group.getClusterWeight() != null) {
                group = TransferServerProvider.getRandomGroupFromCluster(group, base.getTransferGroupArray());
                _log.debug("Choosing TransferGroup " + group.getName() + " from Cluster " + clusterName);
                servers = base.getTransferServers(group.getName());
            }
        }
        if (servers.length == 0) {
            throw new DataBaseException("No DataMover available for TransferGroup " + originalTransferGroup.getName());
        }
        // Increment the index for this particular caller. This index is used as
        // a starting position modulo the size of the list of TransferServers!
        final var indexName = caller + "." + group.getName();
        SecureRandom random;
        synchronized (activeServersIndex) {
            if ((random = activeServersIndex.get(indexName)) == null) {
                activeServersIndex.put(indexName, random = new SecureRandom());
            }
        }
        final var index = random.nextInt(servers.length);
        _log.debug("Selected index for {}: {}", indexName, index);
        // Now we have to go through the list of TransferServers and check if
        // they are available and active!
        final List<TransferServer> array = new ArrayList<>();
        var originalFound = false;
        for (var i = 0; i < servers.length; i++) {
            final var j = (index + i) % servers.length;
            final var server = servers[j];
            final var name = server.getName();
            if (server.getActive() && master.existsClientInterface(name, "DataMover")) {
                // Available and active!
                if (original != null && original.getName().equals(name)) {
                    // We found the original TransferServer so we don't put it
                    // in the queue yet!
                    originalFound = true;
                } else {
                    array.add(server);
                }
            }
        }
        // If a FileSystem is provided then let's go through the list of
        // TransferServers and order them by less used FileSystem!
        final var fileSystemProvided = fileSystem != null && fileSystem >= 0;
        final var loadPerTransferServer = new HashMap<String, Integer>();
        if (Cnf.at("TransferServerManagement", "orderByFileSystemUsage", true) && fileSystemProvided) {
            // Let's compare and populate the load per transfer server table
            // at the same time (we also limit the number of calls to the
            // TransferScheduler method)!
            Collections.sort(array,
                    (ts1, ts2) -> loadPerTransferServer
                            .computeIfAbsent(ts1.getName(),
                                    _ -> TransferScheduler.getNumberOfDownloadsFor(ts1, fileSystem))
                            .compareTo(loadPerTransferServer.computeIfAbsent(ts2.getName(),
                                    _ -> TransferScheduler.getNumberOfDownloadsFor(ts2, fileSystem))));
        }
        // If an original TransferServer is provided and is found to be
        // available and active then it is always set as the first element of
        // the list of results!
        if (originalFound) {
            array.add(0, original);
            if (_log.isDebugEnabled() && fileSystemProvided) {
                // Let's find the number of downloads for this one as well for
                // the logs!
                loadPerTransferServer.put(original.getName(),
                        TransferScheduler.getNumberOfDownloadsFor(original, fileSystem));
            }
        }
        // The display in the log require some processing so only do it if we
        // are requested to do it?
        if (_log.isDebugEnabled()) {
            // Let's build a string with the ordered list of TransferServers!
            final var serverList = new StringBuilder();
            for (final TransferServer ts : array) {
                final var name = ts.getName();
                final var numberOfDownload = loadPerTransferServer.get(name);
                serverList.append(serverList.length() > 0 ? "," : "").append(name);
                if (numberOfDownload != null) {
                    serverList.append("(").append(numberOfDownload).append(")");
                }
                if (original != null && original.getName().equals(name)) {
                    serverList.append("[original]");
                }
            }
            _log.debug("Selected DataMovers for {}{}: {}", indexName,
                    fileSystemProvided ? "[fileSystem=" + fileSystem + "]" : "", serverList);
        }
        // Now let's return the result as a list of TransferServers!
        return array;
    }
}
