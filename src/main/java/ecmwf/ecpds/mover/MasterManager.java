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

import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.ecpds.master.MasterConnection;

/**
 * The Class MasterManager.
 */
final class MasterManager extends MasterConnection {
    private final MoverServer _mover;

    /** The _started. */
    boolean _started = false;

    /**
     * Instantiates a new master manager.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param access
     *            the access
     *
     * @throws ConnectionException
     *             the connection exception
     */
    MasterManager(final MoverServer _mover, final String host, final short port, final MoverInterface access)
            throws ConnectionException {
        this._mover = _mover;

        super(host, port, access);
    }

    /**
     * Sets the started.
     *
     * @param started
     *            the new started
     */
    void setStarted(final boolean started) {
        _started = started;
    }

    /**
     * Disconnected.
     */
    @Override
    public void disconnected() {
        super.disconnected();
        if (_started) {
            _mover.resetDataMover();
        }
    }
}
