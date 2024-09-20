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
import java.util.Date;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.monitor.MonitorManager;

/**
 * The Class DestinationSchedulerCache.
 */
public final class DestinationSchedulerCache implements Serializable, Cloneable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5371658279042749292L;

    /** The _destination name. */
    private String _destinationName = null;

    /** The _last failed transfer. */
    private DataTransfer _lastFailedTransfer = null;

    /** The _last transfer. */
    private DataTransfer _lastTransfer = null;

    /** The _pending data transfers count. */
    private Integer _pendingDataTransfersCount = null;

    /** The _destination status. */
    private String _destinationStatus = null;

    /** The _monitor manager. */
    private MonitorManager _monitorManager = null;

    /** The _destination step. */
    private Integer _destinationStep = null;

    /** The _destination size. */
    private Integer _destinationSize = null;

    /** The _destination start date. */
    private Date _destinationStartDate = null;

    /** The _data transfers in cache. */
    private DataTransfer[] _dataTransfersInCache = null;

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return _destinationName;
    }

    /**
     * Sets the destination name.
     *
     * @param destinationName
     *            the new destination name
     */
    public void setDestinationName(final String destinationName) {
        _destinationName = destinationName;
    }

    /**
     * Gets the monitor manager.
     *
     * @return the monitor manager
     *
     * @throws MasterException
     *             the master exception
     */
    public MonitorManager getMonitorManager() throws MasterException {
        if (_monitorManager == null) {
            throw new MasterException("MonitorManager is off line");
        }
        return _monitorManager;
    }

    /**
     * Sets the monitor manager.
     *
     * @param monitorManager
     *            the new monitor manager
     */
    public void setMonitorManager(final MonitorManager monitorManager) {
        _monitorManager = monitorManager;
    }

    /**
     * Gets the destination status.
     *
     * @return the destination status
     */
    public String getDestinationStatus() {
        return _destinationStatus;
    }

    /**
     * Sets the destination status.
     *
     * @param status
     *            the new destination status
     */
    public void setDestinationStatus(final String status) {
        _destinationStatus = status;
    }

    /**
     * Gets the last failed transfer.
     *
     * @return the last failed transfer
     */
    public DataTransfer getLastFailedTransfer() {
        return _lastFailedTransfer;
    }

    /**
     * Sets the last failed transfer.
     *
     * @param failedTransfer
     *            the new last failed transfer
     */
    public void setLastFailedTransfer(final DataTransfer failedTransfer) {
        _lastFailedTransfer = failedTransfer;
    }

    /**
     * Gets the last transfer.
     *
     * @return the last transfer
     */
    public DataTransfer getLastTransfer() {
        return _lastTransfer;
    }

    /**
     * Sets the last transfer.
     *
     * @param transfer
     *            the new last transfer
     */
    public void setLastTransfer(final DataTransfer transfer) {
        _lastTransfer = transfer;
    }

    /**
     * Gets the pending data transfers count.
     *
     * @return the pending data transfers count
     */
    public int getPendingDataTransfersCount() {
        return _pendingDataTransfersCount == null ? 0 : _pendingDataTransfersCount;
    }

    /**
     * Sets the pending data transfers count.
     *
     * @param pendingDataTransfersCount
     *            the new pending data transfers count
     */
    public void setPendingDataTransfersCount(final Integer pendingDataTransfersCount) {
        _pendingDataTransfersCount = pendingDataTransfersCount;
    }

    /**
     * Gets the destination step.
     *
     * @return the destination step
     *
     * @throws MasterException
     *             the master exception
     */
    public int getDestinationStep() throws MasterException {
        if (_destinationStep == null) {
            throw new MasterException("Destination " + _destinationName + " is off line");
        }
        return _destinationStep;
    }

    /**
     * Sets the destination step.
     *
     * @param destinationStep
     *            the new destination step
     */
    public void setDestinationStep(final Integer destinationStep) {
        _destinationStep = destinationStep;
    }

    /**
     * Gets the destination size.
     *
     * @return the destination size
     *
     * @throws MasterException
     *             the master exception
     */
    public int getDestinationSize() throws MasterException {
        if (_destinationSize == null) {
            throw new MasterException("Destination " + _destinationName + " is off line");
        }
        return _destinationSize;
    }

    /**
     * Sets the destination size.
     *
     * @param destinationSize
     *            the new destination size
     */
    public void setDestinationSize(final Integer destinationSize) {
        _destinationSize = destinationSize;
    }

    /**
     * Gets the destination start date.
     *
     * @return the destination start date
     *
     * @throws MasterException
     *             the master exception
     */
    public Date getDestinationStartDate() throws MasterException {
        if (_destinationStartDate == null) {
            throw new MasterException("Destination " + _destinationName + " is off line");
        }
        return _destinationStartDate;
    }

    /**
     * Sets the destination start date.
     *
     * @param destinationStartDate
     *            the new destination start date
     */
    public void setDestinationStartDate(final Date destinationStartDate) {
        _destinationStartDate = destinationStartDate;
    }

    /**
     * Gets the data transfers in cache.
     *
     * @return the data transfers in cache
     */
    public DataTransfer[] getDataTransfersInCache() {
        return _dataTransfersInCache;
    }

    /**
     * Sets the data transfers in cache.
     *
     * @param dataTransfersInCache
     *            the new data transfers in cache
     */
    public void setDataTransfersInCache(final DataTransfer[] dataTransfersInCache) {
        _dataTransfersInCache = dataTransfersInCache;
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
