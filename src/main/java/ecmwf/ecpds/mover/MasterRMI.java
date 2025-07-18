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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.monitor.MonitorManager;
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
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public void shutdown() {
        // Nothing to shutdown!
    }

    /**
     * {@inheritDoc}
     *
     * Proxy host is alive.
     */
    @Override
    public long proxyHostIsAlive(final String name) throws Exception {
        return _mover.getMasterInterface().proxyHostIsAlive(name);
    }

    /**
     * {@inheritDoc}
     *
     * Update data transfers.
     */
    @Override
    public void updateDataTransfers(final DataTransfer[] transfers) throws Exception {
        _mover.getMasterInterface().updateDataTransfers(transfers);
    }

    /**
     * {@inheritDoc}
     *
     * Update download progress.
     */
    @Override
    public DownloadProgress[] updateDownloadProgress(final DownloadProgress[] progress) throws Exception {
        return _mover.getMasterInterface().updateDownloadProgress(progress);
    }

    /**
     * {@inheritDoc}
     *
     * Update data.
     */
    @Override
    public void updateData(final String hostId, final String data) throws Exception {
        _mover.getMasterInterface().updateData(hostId, data);
    }

    /**
     * {@inheritDoc}
     *
     * Update data.
     */
    @Override
    public void updateData(final Host host) throws Exception {
        _mover.getMasterInterface().updateData(host);
    }

    /**
     * {@inheritDoc}
     *
     * Update location.
     */
    @Override
    public void updateLocation(final Host host) throws Exception {
        _mover.getMasterInterface().updateLocation(host);
    }

    /**
     * {@inheritDoc}
     *
     * Send message.
     */
    @Override
    public void sendMessage(final String name, final String service, final int status, final String message)
            throws Exception {
        MonitorManager.getProvider().sendMessage(name, service, status, message);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file access interface.
     */
    @Override
    public DataAccessInterface getDataFileAccessInterface() throws Exception {
        return _mover.getMasterInterface().getDataFileAccessInterface();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination.
     */
    @Override
    public Destination getDestination(final String name) throws Exception {
        return _mover.getMasterInterface().getDestination(name);
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is valid data file.
     */
    @Override
    public boolean isValidDataFile(final long dataFileId) throws Exception {
        return _mover.getMasterInterface().isValidDataFile(false, dataFileId);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming profile.
     */
    @Override
    public IncomingProfile getIncomingProfile(final String incomingUser, final String incomingPassword,
            final String from) throws Exception {
        return _mover.getMasterInterface().getIncomingProfile(incomingUser, incomingPassword, from);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ecauth token.
     */
    @Override
    public ECauthToken getECauthToken(final String user) throws Exception {
        return _mover.getMasterInterface().getECauthToken(user);
    }
}
