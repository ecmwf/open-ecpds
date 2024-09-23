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

import ecmwf.common.database.Destination;
import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.text.Format;

/**
 * The Class MoverAccessTicket.
 */
public final class MoverAccessTicket extends AbstractTicket {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4375507915283871280L;

    /** The _destination. */
    private final Destination _destination;

    /** The _target. */
    private final String _target;

    /** The _time file. */
    private final long _timeFile;

    /** The _time base. */
    private final long _timeBase;

    /** The _dataFileId. */
    private long _dataFileId = -1;

    /**
     * Instantiates a new mover access ticket.
     *
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param timeFile
     *            the time file
     * @param timeBase
     *            the time base
     */
    public MoverAccessTicket(final Destination destination, final String target, final long timeFile,
            final long timeBase) {
        _destination = destination;
        _target = target;
        _timeFile = timeFile;
        _timeBase = timeBase;
    }

    /**
     * sets the dataFileId.
     *
     * @param dataFileId
     *            the new data file id
     */
    public void setDataFileId(final long dataFileId) {
        _dataFileId = dataFileId;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return _destination;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return _target;
    }

    /**
     * Gets the time file.
     *
     * @return the time file
     */
    public long getTimeFile() {
        return _timeFile;
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public long getTimeBase() {
        return _timeBase;
    }

    /**
     * Gets the dataFileId.
     *
     * @return the time base
     */
    public long getDataFileId() {
        return _dataFileId;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return "[" + hasError() + "][" + _destination.getName() + "][" + _target + "]["
                + Format.formatDuration(System.currentTimeMillis() - getTime()) + "][" + _dataFileId + "]";
    }
}
