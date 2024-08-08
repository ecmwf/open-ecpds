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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Host;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.ecbatch.eis.rmi.client.DataAccess;
import ecmwf.ecbatch.eis.rmi.client.ECauthToken;
import ecmwf.ecpds.master.DataAccessInterface;
import ecmwf.ecpds.master.DownloadProgress;
import ecmwf.ecpds.master.IncomingProfile;

/**
 * The Class MasterRMI.
 */
final class MasterRMI implements MasterProxy {
    /** The _mover. */
    private final MoverServer _mover;

    /**
     * Instantiates a new master rmi.
     *
     * @param mover
     *            the mover
     */
    public MasterRMI(final MoverServer mover) {
        _mover = mover;
    }

    /**
     * Shutdown.
     */
    @Override
    public void shutdown() {
        // Nothing to shutdown!
    }

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
    @Override
    public long proxyHostIsAlive(final String name) throws Exception {
        return _mover.getMasterInterface().proxyHostIsAlive(name);
    }

    /**
     * Update data transfers.
     *
     * @param transfers
     *            the transfers
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void updateDataTransfers(final DataTransfer[] transfers) throws Exception {
        _mover.getMasterInterface().updateDataTransfers(transfers);
    }

    /**
     * Update download progress.
     *
     * @param progress
     *            the progress
     *
     * @return the download progress[]
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public DownloadProgress[] updateDownloadProgress(final DownloadProgress[] progress) throws Exception {
        return _mover.getMasterInterface().updateDownloadProgress(progress);
    }

    /**
     * Update data.
     *
     * @param hostId
     *            the host id
     * @param data
     *            the data
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void updateData(final String hostId, final String data) throws Exception {
        _mover.getMasterInterface().updateData(hostId, data);
    }

    /**
     * Update data.
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void updateData(final Host host) throws Exception {
        _mover.getMasterInterface().updateData(host);
    }

    /**
     * Update location.
     *
     * @param host
     *            the host
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void updateLocation(final Host host) throws Exception {
        _mover.getMasterInterface().updateLocation(host);
    }

    /**
     * Send message.
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
    @Override
    public void sendMessage(final String name, final String service, final int status, final String message)
            throws Exception {
        MonitorManager.getProvider().sendMessage(name, service, status, message);
    }

    /**
     * Gets the data access.
     *
     * @param root
     *            the root
     *
     * @return the data access
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public DataAccess getDataAccess(final String root) throws Exception {
        return _mover.getMasterInterface().getDataAccess(root);
    }

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public DataAccessInterface getDataFileAccessInterface() throws Exception {
        return _mover.getMasterInterface().getDataFileAccessInterface();
    }

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
    @Override
    public Destination getDestination(final String name) throws Exception {
        return _mover.getMasterInterface().getDestination(name);
    }

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
    @Override
    public boolean isValidDataFile(final long dataFileId) throws Exception {
        return _mover.getMasterInterface().isValidDataFile(dataFileId);
    }

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
    @Override
    public IncomingProfile getIncomingProfile(final String incomingUser, final String incomingPassword,
            final String from) throws Exception {
        return _mover.getMasterInterface().getIncomingProfile(incomingUser, incomingPassword, from);
    }

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
    @Override
    public ECauthToken getECauthToken(final String user) throws Exception {
        return _mover.getMasterInterface().getECauthToken(user);
    }
}
