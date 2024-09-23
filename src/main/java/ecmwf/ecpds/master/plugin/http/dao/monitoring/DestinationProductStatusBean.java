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

import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.transfer.StatusFactory;

/**
 * The Class DestinationProductStatusBean.
 */
public class DestinationProductStatusBean extends MonitoringStatusBean implements DestinationProductStatus {

    /** The destination name. */
    private final String destinationName;

    /** The product. */
    private final String product;

    /** The time. */
    private final String time;

    /** The generation status. */
    private int generationStatus = GenerationMonitoringStatus.STATUS_NONE;

    /** The generation status code. */
    private String generationStatusCode;

    /** The scheduled time. */
    private Date scheduledTime;

    /** The last update. */
    private Date lastUpdate;

    /** The product time. */
    private Date productTime;

    /** The when received last done. */
    private Date whenReceivedLastDone;

    /**
     * Instantiates a new destination product status bean.
     *
     * @param destinationName
     *            the destination name
     * @param product
     *            the product
     * @param time
     *            the time
     * @param calculated
     *            the calculated
     */
    protected DestinationProductStatusBean(final String destinationName, final String product, final String time,
            final boolean calculated) {
        super(calculated);
        this.destinationName = destinationName;
        this.product = product;
        this.time = time;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product.
     */
    @Override
    public String getProduct() {
        return product;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the time.
     */
    @Override
    public String getTime() {
        return time;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination name.
     */
    @Override
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the when received last done.
     */
    @Override
    public void setWhenReceivedLastDone(final Date date) {
        whenReceivedLastDone = date;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the when received last done.
     */
    @Override
    public Date getWhenReceivedLastDone() {
        return whenReceivedLastDone;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival time.
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(generationStatusCode) ? lastUpdate : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the generation status.
     */
    @Override
    public int getGenerationStatus() {
        return generationStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the generation status code.
     */
    @Override
    public String getGenerationStatusCode() {
        return generationStatusCode;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the generation status formatted code.
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(generationStatusCode);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last update.
     */
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the minutes before schedule.
     */
    @Override
    public long getMinutesBeforeSchedule() {
        return ProductStatusStrategy.getMinutesBeforeSchedule(scheduledTime, getArrivalTime());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product time.
     */
    @Override
    public Date getProductTime() {
        return productTime;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the scheduled time.
     */
    @Override
    public Date getScheduledTime() {
        return scheduledTime;
    }

    /**
     * {@inheritDoc}
     *
     * Refresh status.
     */
    @Override
    public void refreshStatus() {
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "DestinationProductStatus: D:" + destinationName + ", P:" + time + "-" + product;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status.
     */
    @Override
    public void setGenerationStatus(final int i) {
        generationStatus = i;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status code.
     */
    @Override
    public void setGenerationStatusCode(final String code) {
        generationStatusCode = code;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status from code.
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        generationStatusCode = code;
        generationStatus = ProductStatusStrategy.getGenerationStatusFromCode(this, code, 0);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the last update.
     */
    @Override
    public void setLastUpdate(final Date date) {
        lastUpdate = date;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the product time.
     */
    @Override
    public void setProductTime(final Date date) {
        productTime = date;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the scheduled time.
     */
    @Override
    public void setScheduledTime(final Date date) {
        scheduledTime = date;
    }
}
