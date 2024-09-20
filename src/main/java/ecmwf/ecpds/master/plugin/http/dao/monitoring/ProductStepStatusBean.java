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
     * Gets the product.
     *
     * @return the product
     */
    @Override
    public String getProduct() {
        return this.product;
    }

    /**
     * Gets the step.
     *
     * @return the step
     */
    @Override
    public long getStep() {
        return this.step;
    }

    /**
     * Gets the buffer.
     *
     * @return the buffer
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
     * Gets the generation status code.
     *
     * @return the generation status code
     */
    @Override
    public String getGenerationStatusCode() {
        return this.generationStatusCode;
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
     * Refresh status.
     */
    @Override
    public void refreshStatus() {
        this.setGenerationStatusFromCode(this.generationStatusCode);
    }

    /**
     * Sets the generation status from code.
     *
     * @param code
     *            the new generation status from code
     */
    @Override
    public void setGenerationStatusFromCode(final String code) {
        this.generationStatusCode = code;
        this.generationStatus = ProductStatusStrategy.getGenerationStatusFromCode(this, code, this.getBuffer());
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
     * Sets the last update.
     *
     * @param update
     *            the new last update
     */
    @Override
    public void setLastUpdate(final Date update) {
        this.lastUpdate = update;
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    @Override
    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Sets the scheduled time.
     *
     * @param update
     *            the new scheduled time
     */
    @Override
    public void setScheduledTime(final Date update) {
        this.scheduledTime = update;
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    @Override
    public Date getScheduledTime() {
        return this.scheduledTime;
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
     * Gets the generation status formatted code.
     *
     * @return the generation status formatted code
     */
    @Override
    public String getGenerationStatusFormattedCode() {
        return StatusFactory.getProductStatusName(this.generationStatusCode);
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
     * Gets the arrival time.
     *
     * @return the arrival time
     */
    @Override
    public Date getArrivalTime() {
        return Status.DONE.equals(this.generationStatusCode) ? lastUpdate : null;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return this.type;
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
     * Sets the time.
     *
     * @param time
     *            the new time
     */
    public void setTime(final String time) {
        this.time = time;
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
        generationStatus = 0;
        generationStatusCode = "";
        lastUpdate = null;
        scheduledTime = null;
        productTime = null;
    }
}
