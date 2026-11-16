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

import java.util.ArrayList;
import java.util.List;

import javax.management.timer.Timer;

import ecmwf.common.database.IncomingConnection;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class IncomingUsersManagementThread.
 */
final class IncomingUsersManagementThread extends ConfigurableLoopRunnable {
    private final MoverServer _mover;

    /** The max wait time. */
    private final long maxWaitTime = Cnf.durationAt("IncomingUsersManagementThread", "maxWaitTime",
            2 * Timer.ONE_MINUTE);

    /** The old connections. */
    private final List<IncomingConnection> oldConnections = new ArrayList<>();

    /** The new connections. */
    private final List<IncomingConnection> newConnections = new ArrayList<>();

    /** The last update. */
    private long lastUpdate = System.currentTimeMillis();

    /** The last update. */
    private boolean reset = true;

    /**
     * Instantiates a new incoming users management thread.
     */
    IncomingUsersManagementThread(final MoverServer _mover) {
        this._mover = _mover;
        super();

        setPause(Cnf.at("IncomingUsersManagementThread", "delay", 2000));
    }

    /**
     * Configurable loop run.
     */
    @Override
    public synchronized void configurableLoopRun() {
        try {
            if (_mover.masterManager.isConnected()) {
                // The MoverServer is connected to the MasterServer!
                newConnections.clear();
                newConnections.addAll(_mover.getIncomingConnections());
                final var currentTime = System.currentTimeMillis();
                if (reset || currentTime - lastUpdate > maxWaitTime || !newConnections.equals(oldConnections)) {
                    MoverServer._log.debug("Sending Incoming Connections to MasterServer ({})", newConnections.size());
                    _mover.getMasterInterface().updateIncomingConnectionIds(_mover.getRoot(), newConnections);
                    oldConnections.clear();
                    oldConnections.addAll(newConnections);
                    lastUpdate = currentTime;
                    reset = false;
                }
            } else {
                // Not connected so we initiate a reset to force the MoverServer to re-send the
                // list of Incoming Connections!
                reset = true;
            }
        } catch (final Throwable t) {
            MoverServer._log.warn("Sending incoming connection ids", t);
        }
    }
}
