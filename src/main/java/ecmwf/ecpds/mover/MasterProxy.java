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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.ecpds.master.DataAccessInterface;
import ecmwf.ecpds.master.DownloadProgress;
import ecmwf.ecpds.master.IncomingProfile;

/**
 * The Interface MasterProxy.
 */
public interface MasterProxy {
    /**
     * Shutdown.
     */
    void shutdown();

    /**
     * Update data transfers.
     *
     * @param transfers
     *            the transfers
     *
     * @throws Exception
     *             the exception
     */
    void updateDataTransfers(DataTransfer[] transfers) throws Exception;

    /**
     * Update download progress.
     *
     * @param progress
     *            the progress
     *
     * @return the list of DownloadProgress to interrupt (not found on the MasterServer)
     *
     * @throws Exception
     *             the exception
     */
    DownloadProgress[] updateDownloadProgress(DownloadProgress[] progress) throws Exception;

    /**
     * Proxy host is alive.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws Exception
     *             the exception
     */
    long proxyHostIsAlive(String name) throws Exception;

    /**
     * Update data.
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    void updateData(Host host) throws Exception;

    /**
     * Update data.
     *
     * @param hostId
     *            the host id
     * @param data
     *            the host data
     *
     * @throws Exception
     *             the exception
     */
    void updateData(final String hostId, final String data) throws Exception;

    /**
     * Update location.
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    void updateLocation(Host host) throws Exception;

    /**
     * Sends the message.
     *
     * @param name
     *            the name
     * @param service
     *            the service
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @throws Exception
     *             the exception
     */
    void sendMessage(String name, String service, int status, String message) throws Exception;

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     *
     * @throws Exception
     *             the exception
     */
    DataAccessInterface getDataFileAccessInterface() throws Exception;

    /**
     * Gets the destination.
     *
     * @param name
     *            the name
     *
     * @return the destination
     *
     * @throws Exception
     *             the exception
     */
    Destination getDestination(String name) throws Exception;

    /**
     * Checks if is valid data file.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return true, if is valid data file
     *
     * @throws Exception
     *             the exception
     */
    boolean isValidDataFile(long dataFileId) throws Exception;

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
     * @throws Exception
     *             the exception
     */
    IncomingProfile getIncomingProfile(String incomingUser, String incomingPassword, String from) throws Exception;

    /**
     * Gets the ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws Exception
     *             the exception
     */
    ECauthToken getECauthToken(String user) throws Exception;
}
