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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
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
     * Gets the product.
     *
     * @return the product
     */
    @Override
    public String getProduct() {
        return product;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    @Override
    public String getTime() {
        return time;
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    @Override
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Sets the when received last done.
     *
     * @param date
     *            the new when received last done
     */
    @Override
    public void setWhenReceivedLastDone(final Date date) {
        whenReceivedLastDone = date;
    }

    /**
     * Gets the when received last done.
     *
     * @return the when received last done
     */
    @Override
    public Date getWhenReceivedLastDone() {
        return whenReceivedLastDone;
    }

    /**
     * Gets the arrival time.
     *
     * @return the arrival time
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(generationStatusCode) ? lastUpdate : null;
    }

    /**
     * Gets the generation status.
     *
     * @return the generation status
     */
    @Override
    public int getGenerationStatus() {
        return generationStatus;
    }

    /**
     * Gets the generation status code.
     *
     * @return the generation status code
     */
    @Override
    public String getGenerationStatusCode() {
        return generationStatusCode;
    }

    /**
     * Gets the generation status formatted code.
     *
     * @return the generation status formatted code
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(generationStatusCode);
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Gets the minutes before schedule.
     *
     * @return the minutes before schedule
     */
    @Override
    public long getMinutesBeforeSchedule() {
        return ProductStatusStrategy.getMinutesBeforeSchedule(scheduledTime, getArrivalTime());
    }

    /**
     * Gets the product time.
     *
     * @return the product time
     */
    @Override
    public Date getProductTime() {
        return productTime;
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    @Override
    public Date getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Refresh status.
     */
    @Override
    public void refreshStatus() {
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "DestinationProductStatus: D:" + destinationName + ", P:" + time + "-" + product;
    }

    /**
     * Sets the generation status.
     *
     * @param i
     *            the new generation status
     */
    @Override
    public void setGenerationStatus(final int i) {
        generationStatus = i;
    }

    /**
     * Sets the generation status code.
     *
     * @param code
     *            the new generation status code
     */
    @Override
    public void setGenerationStatusCode(final String code) {
        generationStatusCode = code;
    }

    /**
     * Sets the generation status from code.
     *
     * @param code
     *            the new generation status from code
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        generationStatusCode = code;
        generationStatus = ProductStatusStrategy.getGenerationStatusFromCode(this, code, 0);
    }

    /**
     * Sets the last update.
     *
     * @param date
     *            the new last update
     */
    @Override
    public void setLastUpdate(final Date date) {
        lastUpdate = date;
    }

    /**
     * Sets the product time.
     *
     * @param date
     *            the new product time
     */
    @Override
    public void setProductTime(final Date date) {
        productTime = date;
    }

    /**
     * Sets the scheduled time.
     *
     * @param date
     *            the new scheduled time
     */
    @Override
    public void setScheduledTime(final Date date) {
        scheduledTime = date;
    }
}
