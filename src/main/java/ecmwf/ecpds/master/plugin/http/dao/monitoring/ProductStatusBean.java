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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Gets the product.
     */
    @Override
    public String getProduct() {
        return this.product;
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
     * Sets the last update.
     */
    @Override
    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
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
     * Sets the scheduled time.
     */
    @Override
    public void setScheduledTime(final Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the generation status.
     */
    @Override
    public int getGenerationStatus() {
        return Status.DONE.equals(this.generationStatusCode) ? GenerationMonitoringStatus.STATUS_OK : generationStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status.
     */
    @Override
    public void setGenerationStatus(final int generationStatus) {
        this.generationStatus = generationStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status from code.
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        this.setGenerationStatusCode(code);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the minutes before schedule.
     */
    @Override
    public long getMinutesBeforeSchedule() {
        return ProductStatusStrategy.getMinutesBeforeSchedule(this.getScheduledTime(), this.getArrivalTime());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival time.
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(this.generationStatusCode) ? lastUpdate : null;
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
     * Gets the generation status code.
     */
    @Override
    public String getGenerationStatusCode() {
        return this.generationStatusCode;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the generation status formatted code.
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(this.generationStatusCode);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status code.
     */
    @Override
    public void setGenerationStatusCode(final String code) {
        this.generationStatusCode = code;
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
     * Sets the product time.
     */
    @Override
    public void setProductTime(final Date productTime) {
        this.productTime = productTime;
    }

    /**
     * {@inheritDoc}
     *
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
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + time + "-" + product + "=" + generationStatusCode + " }";
    }
}
