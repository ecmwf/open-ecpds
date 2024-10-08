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

import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.transfer.StatusFactory;

/**
 * The Class ProductStepStatusBean.
 */
public class ProductStepStatusBean extends MonitoringStatusBean implements ProductStepStatus {

    /** The product. */
    private final String product;

    /** The time. */
    private String time = "";

    /** The step. */
    private long step = 0;

    /** The buffer. */
    private long buffer = 0;

    /** The type. */
    private String type = "";

    /** The generation status. */
    private int generationStatus = GenerationMonitoringStatus.STATUS_NONE;

    /** The generation status code. */
    private String generationStatusCode = "";

    /** The last update. */
    private Date lastUpdate;

    /** The scheduled time. */
    private Date scheduledTime;

    /** The product time. */
    private Date productTime;

    /**
     * Instantiates a new product step status bean.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param buffer
     *            the buffer
     * @param step
     *            the step
     * @param type
     *            the type
     * @param calculated
     *            the calculated
     */
    public ProductStepStatusBean(final String product, final String time, final long buffer, final long step,
            final String type, final boolean calculated) {
        super(calculated);
        this.product = product;
        this.time = time;
        this.buffer = buffer;
        this.step = step;
        this.type = type;
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
     * Gets the step.
     */
    @Override
    public long getStep() {
        return this.step;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the buffer.
     */
    @Override
    public long getBuffer() {
        return this.buffer;
    }

    /**
     * Checks if is arrived.
     *
     * @return true, if is arrived
     */
    public boolean isArrived() {
        return this.getArrivalTime() != null;
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
     * Sets the generation status code.
     */
    @Override
    public void setGenerationStatusCode(final String code) {
        this.generationStatusCode = code;
    }

    /**
     * {@inheritDoc}
     *
     * Refresh status.
     */
    @Override
    public void refreshStatus() {
        this.setGenerationStatusFromCode(this.generationStatusCode);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the generation status from code.
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        this.generationStatusCode = code;
        this.generationStatus = ProductStatusStrategy.getGenerationStatusFromCode(this, code, this.getBuffer());
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
     * Sets the last update.
     */
    @Override
    public void setLastUpdate(final Date update) {
        this.lastUpdate = update;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last update.
     */
    @Override
    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the scheduled time.
     */
    @Override
    public void setScheduledTime(final Date update) {
        this.scheduledTime = update;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the scheduled time.
     */
    @Override
    public Date getScheduledTime() {
        return this.scheduledTime;
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
     * Gets the generation status formatted code.
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(this.generationStatusCode);
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
     * Gets the arrival time.
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(this.generationStatusCode) ? lastUpdate : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the type.
     */
    @Override
    public String getType() {
        return this.type;
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
     * Sets the time.
     *
     * @param time
     *            the new time
     */
    public void setTime(final String time) {
        this.time = time;
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
        generationStatus = 0;
        generationStatusCode = "";
        lastUpdate = null;
        scheduledTime = null;
        productTime = null;
    }
}
