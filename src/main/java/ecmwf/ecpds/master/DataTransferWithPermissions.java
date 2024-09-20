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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

import ecmwf.common.database.DataTransfer;

/**
 * The Class DataTransferWithPermissions.
 */
public final class DataTransferWithPermissions implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The _transfer. */
    public final DataTransfer _transfer;

    /** The _stop. */
    public final boolean _stop;

    /** The _wait. */
    public final boolean _wait;

    /** The _hold. */
    public final boolean _hold;

    /**
     * Instantiates a new data transfer with permissions.
     *
     * @param transfer
     *            the transfer
     * @param stop
     *            the stop
     * @param wait
     *            the wait
     * @param hold
     *            the hold
     */
    public DataTransferWithPermissions(final DataTransfer transfer, final boolean stop, final boolean wait,
            final boolean hold) {
        _transfer = transfer;
        _stop = stop;
        _wait = wait;
        _hold = hold;
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return _transfer;
    }

    /**
     * Gets the stop.
     *
     * @return the stop
     */
    public boolean getStop() {
        return _stop;
    }

    /**
     * Gets the wait.
     *
     * @return the wait
     */
    public boolean getWait() {
        return _wait;
    }

    /**
     * Gets the hold.
     *
     * @return the hold
     */
    public boolean getHold() {
        return _hold;
    }
}
