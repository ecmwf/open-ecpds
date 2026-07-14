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
import ecmwf.common.database.DataBaseException;
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
     * Check whether an IncomingUser is configured for anonymous (password-free) access.
     *
     * @param incomingUser
     *            the incoming user name
     *
     * @return true if the user exists and has portal service set to open-access
     *
     * @throws RemoteException
     *             the remote exception
     */
    boolean isAnonymousIncomingUser(String incomingUser) throws RemoteException;

    /**
     * Get the current number of active connections for the specified incoming user across all data movers and
     * protocols.
     *
     * @param incomingUser
     *            the incoming user name
     *
     * @return the active connection count
     *
     * @throws RemoteException
     *             the remote exception
     */
    int getIncomingConnectionCount(String incomingUser) throws RemoteException;

    /**
     * Returns the total bytes transferred by the given portal user within the specified rolling window.
     *
     * @param userId
     *            the user id
     * @param upload
     *            true for upload bytes, false for download bytes
     * @param windowMs
     *            the rolling window in milliseconds
     *
     * @return total bytes within the window
     *
     * @throws RemoteException
     *             if the call fails
     */
    long getPortalBytesUsed(String userId, boolean upload, long windowMs) throws RemoteException;

    /**
     * Gets portal traffic statistics. Returns minute-bucket rows for the given user (or all users if userId is empty),
     * covering the specified number of hours back from now.
     *
     * @param userId
     *            the incoming user ID, or empty string for system-wide
     * @param hours
     *            how many hours of history to return
     *
     * @return list of PortalTraffic rows ordered by time descending
     *
     * @throws RemoteException
     *             the remote exception
     */
    List<ecmwf.common.database.PortalTraffic> getPortalTraffic(String userId, int hours) throws RemoteException;

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
     * Returns a fresh IncomingProfile for an already-authenticated user without re-running credential validation, TOTP,
     * connection-count checks, or last-login updates.
     *
     * @param incomingUser
     *            the incoming user id
     *
     * @return the incoming profile
     *
     * @throws RemoteException
     *             the remote exception
     */
    IncomingProfile getIncomingProfileNoAuth(String incomingUser) throws RemoteException;

    /**
     * Releases a pending connection slot for the given user. Must be called when a session closes before being
     * confirmed by the incoming connections poll, to prevent the pending counter from leaking.
     *
     * @param incomingUser
     *            the incoming user id
     *
     * @throws RemoteException
     *             the remote exception
     */
    void releaseConnectionSlot(String incomingUser) throws RemoteException;

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

    /**
     * Self-register a new data portal user (self-service flow). Creates an inactive IncomingUser with
     * portal_service="self-service", generates a verification token stored in INU_VERIFY_TOKEN, and returns the token
     * so the caller can send a verification email.
     *
     * @param id
     *            the requested username
     * @param name
     *            the user's full name (stored in comment field)
     * @param email
     *            the user's email address
     * @param iso
     *            ISO 3166-1 alpha-2 country code
     *
     * @return the generated verification token
     *
     * @throws DataBaseException
     *             if id already exists or is invalid
     * @throws RemoteException
     *             the remote exception
     */
    String selfRegisterUser(String id, String name, String email, String iso) throws DataBaseException, RemoteException;

    /**
     * Verify a self-registration token. If autoApprove is true and the token is valid: activates the user, clears the
     * token, generates a random password, and returns "activated:userId:password". If autoApprove is false and the
     * token is valid: marks the email as verified (sets token to "VERIFIED") and returns "verified:userId". Returns
     * "invalid" if the token is not found.
     *
     * @param token
     *            the verification token from the email link
     * @param autoApprove
     *            whether to activate the account immediately
     *
     * @return status string: "activated:userId:password", "verified:userId", or "invalid"
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    String verifyRegistrationToken(String token, boolean autoApprove) throws DataBaseException, RemoteException;

    /**
     * Check whether a specific portal subscriber is still active. This is a lightweight single-row lookup by primary
     * key used on every cookie re-auth to detect deactivated subscribers without a full password re-validation.
     *
     * @param psbId
     *            the PortalSubscriber primary key
     *
     * @return {@code true} if the subscriber exists and {@code PSB_ACTIVE = true}
     *
     * @throws RemoteException
     *             the remote exception
     */
    boolean isSubscriberActive(long psbId) throws RemoteException;

    /**
     * Find the PSB_ID of the active subscriber that matches the given password for the given IncomingUser. Returns
     * {@code -1} if no active subscriber with that password exists (e.g. standard-login or open-access user).
     *
     * @param inuId
     *            the IncomingUser login
     * @param password
     *            the subscriber password to match
     *
     * @return the subscriber's PSB_ID, or {@code -1} if not found
     *
     * @throws RemoteException
     *             the remote exception
     */
    long findSubscriberIdByPassword(String inuId, String password) throws RemoteException;

    /**
     * Get the email address of the portal subscriber with the given PSB_ID. Returns an empty string if the subscriber
     * does not exist or has no email recorded.
     *
     * @param psbId
     *            the portal subscriber PSB_ID
     *
     * @return the subscriber email, or an empty string if not found
     *
     * @throws RemoteException
     *             the remote exception
     */
    String getPortalSubscriberEmail(long psbId) throws RemoteException;

    /**
     * Send an email notification via the MasterServer mail infrastructure.
     *
     * @param to
     *            the recipient email address
     * @param subject
     *            the email subject
     * @param body
     *            the HTML email body
     *
     * @throws RemoteException
     *             the remote exception
     */
    void sendNotificationEmail(String to, String subject, String body) throws RemoteException;

    /**
     * Invalidate all portal HTTPS session cookies for the given data user on all movers. Forces the user (and any
     * subscribers) to re-authenticate on their next request. Call this whenever the user's Portal Service mode or
     * password changes.
     *
     * @param user
     *            the data user login name
     *
     * @throws RemoteException
     *             the remote exception
     */
    void invalidatePortalSessionsForUser(String user) throws RemoteException;

}
