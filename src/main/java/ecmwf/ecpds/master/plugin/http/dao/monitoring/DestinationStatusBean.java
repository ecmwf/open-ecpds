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

package ecmwf.ecpds.master.plugin.http.dao.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;

/**
 * The Class DestinationStatusBean.
 */
public class DestinationStatusBean extends MonitoringStatusBean implements DestinationStatus {

    /** The destination. */
    private final String destination;

    /** The last transfer. */
    private DataTransfer lastTransfer;

    /** The status. */
    private String status = "None";

    /** The big sister status. */
    private int bigSisterStatus = 0;

    /** The big sister status comment. */
    private String bigSisterStatusComment = "";

    /** The queue size. */
    private int queueSize = 0;

    /** The bad data transfers size. */
    private int badDataTransfersSize = 0;

    /** The calculation date. */
    private Date calculationDate;

    /** The monitoring date. */
    private Date monitoringDate;

    /** The primary host. */
    private Host primaryHost;

    /** The currently used host name. */
    private String currentlyUsedHostName;

    /**
     * Instantiates a new destination status bean.
     *
     * @param destination
     *            the destination
     * @param calculated
     *            the calculated
     */
    public DestinationStatusBean(final String destination, final boolean calculated) {
        super(calculated);
        this.destination = destination;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the monitoring date.
     */
    @Override
    public Date getMonitoringDate() {
        return monitoringDate;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the monitoring date.
     */
    @Override
    public void setMonitoringDate(final Date monitoringDate) {
        this.monitoringDate = monitoringDate;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the calculation date.
     */
    @Override
    public Date getCalculationDate() {
        return calculationDate;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the calculation date.
     */
    @Override
    public void setCalculationDate(final Date calculationDate) {
        this.calculationDate = calculationDate;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers size.
     */
    @Override
    public int getBadDataTransfersSize() {
        return badDataTransfersSize;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the bad data transfers size.
     */
    @Override
    public void setBadDataTransfersSize(final int badDataTransfersSize) {
        this.badDataTransfersSize = badDataTransfersSize;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last transfer.
     */
    @Override
    public DataTransfer getLastTransfer() {
        return lastTransfer;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the last transfer.
     */
    @Override
    public void setLastTransfer(final DataTransfer lastTransfer) {
        this.lastTransfer = lastTransfer;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the queue size.
     */
    @Override
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the queue size.
     */
    @Override
    public void setQueueSize(final int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the status.
     */
    @Override
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the using internet.
     */
    @Override
    public boolean getUsingInternet() {
        return primaryHost != null && primaryHost.getTransferMethodName().toLowerCase().indexOf("ecaccess") >= 0;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the primary host.
     */
    @Override
    public Host getPrimaryHost() {
        return primaryHost;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the primary host.
     */
    @Override
    public void setPrimaryHost(final Host h) {
        primaryHost = h;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the currently used host name.
     */
    @Override
    public String getCurrentlyUsedHostName() {
        return currentlyUsedHostName;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the currently used host name.
     */
    @Override
    public void setCurrentlyUsedHostName(final String h) {
        currentlyUsedHostName = h;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is using primary host.
     */
    @Override
    public boolean isUsingPrimaryHost() {
        return primaryHost != null
                && (currentlyUsedHostName == null || currentlyUsedHostName.equals(primaryHost.getName()));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the big sister status.
     */
    @Override
    public int getBigSisterStatus() {
        return bigSisterStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the big sister status.
     */
    @Override
    public void setBigSisterStatus(final int i) {
        bigSisterStatus = i;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the big sister status comment.
     */
    @Override
    public String getBigSisterStatusComment() {
        return bigSisterStatusComment;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the big sister status comment.
     */
    @Override
    public void setBigSisterStatusComment(final String s) {
        bigSisterStatusComment = s;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + destination + "," + status + " }";
    }
}
