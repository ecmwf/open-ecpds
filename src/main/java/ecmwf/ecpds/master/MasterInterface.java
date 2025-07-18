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

import java.rmi.RemoteException;
import java.util.List;

import ecmwf.common.database.Association;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.ecaccess.ProviderInterface;

/**
 * The Interface MasterInterface.
 */
public interface MasterInterface extends ProviderInterface {

    /**
     * Update incoming connection ids.
     *
     * @param serverName
     *            the server name
     * @param incomingConnections
     *            the incoming connections
     *
     * @throws RemoteException
     *             the remote exception
     */
    void updateIncomingConnectionIds(String serverName, List<IncomingConnection> incomingConnections)
            throws RemoteException;

    /**
     * Proxy host is alive.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws RemoteException
     *             the remote exception
     */
    long proxyHostIsAlive(String name) throws RemoteException;

    /**
     * Update data transfers.
     *
     * @param transfers
     *            the transfers
     *
     * @throws RemoteException
     *             the remote exception
     */
    void updateDataTransfers(DataTransfer[] transfers) throws RemoteException;

    /**
     * Update download progress.
     *
     * @param progress
     *            the progress
     *
     * @return the list of DownloadProgress to interrupt (not found on the MasterServer)
     *
     * @throws RemoteException
     *             the remote exception
     */
    DownloadProgress[] updateDownloadProgress(DownloadProgress[] progress) throws RemoteException;

    /**
     * Update data.
     *
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     */
    void updateData(Host host) throws RemoteException;

    /**
     * Update data.
     *
     * @param hostId
     *            the host id
     * @param data
     *            the host data
     *
     * @throws RemoteException
     *             the remote exception
     */
    void updateData(String hostId, String data) throws RemoteException;

    /**
     * Update location.
     *
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     */
    void updateLocation(Host host) throws RemoteException;

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataAccessInterface getDataFileAccessInterface() throws RemoteException;

    /**
     * Gets the destination.
     *
     * @param name
     *            the name
     *
     * @return the destination
     *
     * @throws RemoteException
     *             the remote exception
     */
    Destination getDestination(String name) throws RemoteException;

    /**
     * Checks if is valid data file.
     *
     * @param isProxy
     *            the is proxy
     * @param dataFileId
     *            the data file id
     *
     * @return true, if is valid data file
     *
     * @throws RemoteException
     *             the remote exception
     */
    boolean isValidDataFile(boolean isProxy, long dataFileId) throws RemoteException;

    /**
     * Return a hash of the specified incoming user (the string is in the form userid:password).
     *
     * @param incomingUser
     *            the incoming user
     *
     * @return hash, if the user is found
     *
     * @throws RemoteException
     *             the remote exception
     */
    String getIncomingUserHash(String incomingUser) throws RemoteException;

    /**
     * Return a hash of the specified incoming user (the string is in the form userid:password).
     *
     * @param incomingUser
     *            the incoming user
     * @param prefix
     *            the prefix (e.g. AWS4)
     * @param data
     *            the data
     * @param algorithm
     *            the algorithm
     *
     * @return authorization signature
     *
     * @throws RemoteException
     *             the remote exception
     */
    byte[] getS3AuthorizationSignature(String incomingUser, String prefix, String data, String algorithm)
            throws RemoteException;

    /**
     * Gets the incoming profile.
     *
     * @param incomingUser
     *            the incoming user
     * @param incomingPassword
     *            the incoming password
     * @param from
     *            the from
     *
     * @return the incoming profile
     *
     * @throws RemoteException
     *             the remote exception
     */
    IncomingProfile getIncomingProfile(String incomingUser, String incomingPassword, String from)
            throws RemoteException;

    /**
     * Gets the ETag for a given dataTransferId.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the ETag
     *
     * @throws RemoteException
     *             the remote exception
     */
    String getETag(long dataTransferId) throws RemoteException;

    /**
     * Gets the ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws RemoteException
     *             the remote exception
     */
    ECauthToken getECauthToken(String user) throws RemoteException;

    /**
     * Import destination from another Master Server.
     *
     * @param fromDestination
     *            the from destination
     * @param linkedAssociations
     *            the linked associations
     * @param copySharedHost
     *            the copy shared host
     *
     * @throws RemoteException
     *             the remote exception
     */
    void importDestination(Destination fromDestination, Association[] linkedAssociations, boolean copySharedHost)
            throws RemoteException;

    /**
     * Update data transfer status from another Master Server.
     *
     * @param master
     *            the remote master
     * @param standby
     *            the standby flag
     * @param destination
     *            the destination name
     * @param target
     *            the target name
     * @param uniqueName
     *            the unique name
     * @param status
     *            the status
     *
     * @return true, if successful
     *
     * @throws RemoteException
     *             the remote exception
     */
    boolean updateLocalTransferStatus(String master, boolean standby, String destination, String target,
            String uniqueName, String status) throws RemoteException;

}
