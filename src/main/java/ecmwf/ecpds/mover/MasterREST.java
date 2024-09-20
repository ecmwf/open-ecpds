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

import java.util.Arrays;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.common.RestException;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.ecpds.master.DataAccessInterface;
import ecmwf.ecpds.master.DownloadProgress;
import ecmwf.ecpds.master.IncomingProfile;
import ecmwf.ecpds.mover.service.RESTClient.MonitorRequest;
import ecmwf.ecpds.mover.service.RESTClient.UpdateDataRequest;

/**
 * The Class MasterREST.
 */
final class MasterREST implements MasterProxy {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MasterREST.class);

    /** The _rest. */
    private final RESTInterface _rest;

    /** The _management thread. */
    private final ManagementThread _managementThread;

    /** The _mover. */
    private final MoverServer _mover;

    /**
     * Instantiates a new master rest.
     *
     * @param mover
     *            the mover
     * @param rest
     *            the rest
     */
    MasterREST(final MoverServer mover, final RESTInterface rest) {
        _mover = mover;
        _rest = rest;
        _managementThread = new ManagementThread();
        _managementThread.setPriority(Thread.MIN_PRIORITY);
        _managementThread.execute();
    }

    /**
     * Shutdown the ManagementThread if it exists.
     */
    @Override
    public void shutdown() {
        if (_managementThread != null) {
            _managementThread.shutdown();
        }
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
        return _rest.proxyHostIsAlive(name);
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
        _rest.updateDataTransfers(Arrays.asList(transfers));
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
        throw new RestException("No implemented");
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
        final var request = new UpdateDataRequest();
        request.hostId = hostId;
        request.data = data;
        _rest.updateDataRequest(request);
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
        _rest.updateData(host);
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
        _rest.updateLocation(host);
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
        final var request = new MonitorRequest();
        request.name = name;
        request.service = service;
        request.status = status;
        request.message = message;
        _rest.sendMessage(request);
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
        throw new RestException("No implemented");
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
        throw new RestException("No implemented");
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
        return _rest.isValidDataFile(dataFileId);
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
        throw new RestException("No implemented");
    }

    /**
     * Gets the ecauth token. This is used by the ECauthModule to get access to a system which support the ecauth
     * mechanism.
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
        return _rest.getECauthToken(user);
    }

    /**
     * The Class ManagementThread.
     */
    private final class ManagementThread extends ConfigurableLoopRunnable {
        /** The _last successful. */
        private long _lastSuccessful = -1;

        /** The _last restart. */
        private long _lastRestart = -1;

        /** The _root. */
        private final String _root;

        /**
         * Instantiates a new management thread.
         */
        ManagementThread() {
            setPause(Cnf.at("MasterREST", "delay", 15 * Timer.ONE_SECOND));
            _root = _mover.getRoot();
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                final var restart = proxyHostIsAlive(_root);
                _lastSuccessful = System.currentTimeMillis();
                if (_lastRestart != -1 && restart != _lastRestart) {
                    // The MasterServer was restarted since our last connection
                    // so we should reset all our activity!
                    _log.warn("The MasterServer was restarted");
                    _mover.resetDataMover();
                }
                _lastRestart = restart;
            } catch (final Throwable t) {
                _log.warn("Sending alive message to the MasterServer", t);
                if (_lastSuccessful != -1 && System.currentTimeMillis() - _lastSuccessful > 2 * Timer.ONE_MINUTE) {
                    _lastSuccessful = -1;
                    _log.warn("More than 2 minutes without successful connection to the MasterServer");
                    _mover.resetDataMover();
                }
            }
        }
    }
}
