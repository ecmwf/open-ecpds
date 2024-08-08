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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.transfer.StatusFactory;

/**
 * The Class ProductStatusBean.
 */
public class ProductStatusBean extends MonitoringStatusBean implements ProductStatus {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ProductStatusBean.class);

    /** The product. */
    private final String product;

    /** The time. */
    private final String time;

    /** The buffer. */
    private long buffer;

    /** The product time. */
    private Date productTime;

    /** The generation status. */
    private int generationStatus = GenerationMonitoringStatus.STATUS_NONE;

    /** The generation status code. */
    private String generationStatusCode;

    /** The scheduled time. */
    private Date scheduledTime;

    /** The last update. */
    private Date lastUpdate;

    /**
     * Instantiates a new product status bean.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param buffer
     *            the buffer
     * @param calculated
     *            the calculated
     */
    public ProductStatusBean(final String product, final String time, final long buffer, final boolean calculated) {
        super(calculated);
        this.product = product;
        this.time = time;
        this.buffer = buffer;
    }

    /**
     * Gets the product.
     *
     * @return the product
     */
    @Override
    public String getProduct() {
        return this.product;
    }

    /**
     * Gets the last update.
     *
     * @return Returns the lastUpdate.
     */
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Sets the last update.
     *
     * @param lastUpdate
     *            The lastUpdate to set.
     */
    @Override
    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * Gets the scheduled time.
     *
     * @return Returns the scheduledTime.
     */
    @Override
    public Date getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Sets the scheduled time.
     *
     * @param scheduledTime
     *            The scheduledTime to set.
     */
    @Override
    public void setScheduledTime(final Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Gets the generation status.
     *
     * @return Returns the generationStatus.
     */
    @Override
    public int getGenerationStatus() {
        return Status.DONE.equals(this.generationStatusCode) ? GenerationMonitoringStatus.STATUS_OK : generationStatus;
    }

    /**
     * Sets the generation status.
     *
     * @param generationStatus
     *            the new generation status
     */
    @Override
    public void setGenerationStatus(final int generationStatus) {
        this.generationStatus = generationStatus;
    }

    /**
     * Sets the generation status from code.
     *
     * @param code
     *            the new generation status from code
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        this.setGenerationStatusCode(code);
    }

    /**
     * Gets the minutes before schedule.
     *
     * @return the minutes before schedule
     */
    @Override
    public long getMinutesBeforeSchedule() {
        return ProductStatusStrategy.getMinutesBeforeSchedule(this.getScheduledTime(), this.getArrivalTime());
    }

    /**
     * Gets the arrival time.
     *
     * @return the arrival time
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(this.generationStatusCode) ? lastUpdate : null;
    }

    /**
     * Refresh status.
     */
    @Override
    public void refreshStatus() {
    }

    /**
     * Gets the generation status code.
     *
     * @return the generation status code
     */
    @Override
    public String getGenerationStatusCode() {
        return this.generationStatusCode;
    }

    /**
     * Gets the generation status formatted code.
     *
     * @return the generation status formatted code
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(this.generationStatusCode);
    }

    /**
     * Sets the generation status code.
     *
     * @param code
     *            the new generation status code
     */
    @Override
    public void setGenerationStatusCode(final String code) {
        this.generationStatusCode = code;
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
     * Gets the buffer.
     *
     * @return the buffer
     */
    public long getBuffer() {
        return buffer;
    }

    /**
     * Sets the buffer.
     *
     * @param buffer
     *            the new buffer
     */
    public void setBuffer(final long buffer) {
        this.buffer = buffer;
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
     * Sets the product time.
     *
     * @param productTime
     *            the new product time
     */
    @Override
    public void setProductTime(final Date productTime) {
        this.productTime = productTime;
    }

    /**
     * Reset.
     */
    @Override
    public void reset() {
        super.reset();
        // Clean own fields
        scheduledTime = null;
        generationStatus = 0;
        generationStatusCode = null;
        productTime = null;
        try {
            // And then clean all Step and Destination Statii
            ProductStepStatusHome.cleanProduct(product, time);
            DestinationProductStatusHome.cleanProduct(product, time);
        } catch (final MonitoringException e) {
            log.error("Error cleaning Steps or Destinations for product '" + product + "'", e);
        }
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + time + "-" + product + "=" + generationStatusCode + " }";
    }
}
