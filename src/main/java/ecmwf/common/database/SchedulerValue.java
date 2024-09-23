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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class SchedulerValue.
 */
public class SchedulerValue extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5819155806273483769L;

    /** The hos name. */
    protected Integer HOS_NAME;

    /** The scv has requeued. */
    protected boolean SCV_HAS_REQUEUED;

    /** The scv id. */
    protected int SCV_ID;

    /** The scv last transfer ko. */
    protected Long SCV_LAST_TRANSFER_KO;

    /** The scv last transfer ok. */
    protected Long SCV_LAST_TRANSFER_OK;

    /** The scv reset time. */
    protected BigDecimal SCV_RESET_TIME;

    /** The scv start count. */
    protected int SCV_START_COUNT;

    /** The host. */
    protected Host host;

    /**
     * Instantiates a new scheduler value.
     */
    public SchedulerValue() {
    }

    /**
     * Instantiates a new scheduler value.
     *
     * @param id
     *            the id
     */
    public SchedulerValue(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new scheduler value.
     *
     * @param id
     *            the id
     */
    public SchedulerValue(final String id) {
        setId(id);
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return integerToString(HOS_NAME);
    }

    /**
     * Sets the host name.
     *
     * @param param
     *            the new host name
     */
    public void setHostName(final String param) {
        HOS_NAME = stringToInteger(param);
    }

    /**
     * Gets the checks for requeued.
     *
     * @return the checks for requeued
     */
    public boolean getHasRequeued() {
        return SCV_HAS_REQUEUED;
    }

    /**
     * Sets the checks for requeued.
     *
     * @param param
     *            the new checks for requeued
     */
    public void setHasRequeued(final boolean param) {
        SCV_HAS_REQUEUED = param;
    }

    /**
     * Sets the checks for requeued.
     *
     * @param param
     *            the new checks for requeued
     */
    public void setHasRequeued(final String param) {
        SCV_HAS_REQUEUED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return SCV_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        SCV_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        SCV_ID = Integer.parseInt(param);
    }

    /**
     * Gets the last transfer ko.
     *
     * @return the last transfer ko
     */
    public Long getLastTransferKo() {
        return SCV_LAST_TRANSFER_KO;
    }

    /**
     * Sets the last transfer ko.
     *
     * @param param
     *            the new last transfer ko
     */
    public void setLastTransferKo(final Long param) {
        SCV_LAST_TRANSFER_KO = param;
    }

    /**
     * Sets the last transfer ko.
     *
     * @param param
     *            the new last transfer ko
     */
    public void setLastTransferKo(final long param) {
        SCV_LAST_TRANSFER_KO = param;
    }

    /**
     * Gets the last transfer ko.
     *
     * @param defaultValue
     *            the default value
     *
     * @return the last transfer ko
     */
    public long getLastTransferKo(final long defaultValue) {
        return SCV_LAST_TRANSFER_KO == null ? defaultValue : SCV_LAST_TRANSFER_KO;
    }

    /**
     * Gets the last transfer ok.
     *
     * @return the last transfer ok
     */
    public Long getLastTransferOk() {
        return SCV_LAST_TRANSFER_OK;
    }

    /**
     * Sets the last transfer ok.
     *
     * @param param
     *            the new last transfer ok
     */
    public void setLastTransferOk(final Long param) {
        SCV_LAST_TRANSFER_OK = param;
    }

    /**
     * Sets the last transfer ok.
     *
     * @param param
     *            the new last transfer ok
     */
    public void setLastTransferOk(final long param) {
        SCV_LAST_TRANSFER_OK = param;
    }

    /**
     * Gets the last transfer ok.
     *
     * @param defaultValue
     *            the default value
     *
     * @return the last transfer ok
     */
    public long getLastTransferOk(final long defaultValue) {
        return SCV_LAST_TRANSFER_OK == null ? defaultValue : SCV_LAST_TRANSFER_OK;
    }

    /**
     * Gets the reset time.
     *
     * @return the reset time
     */
    public java.sql.Timestamp getResetTime() {
        return bigDecimalToTimestamp(SCV_RESET_TIME);
    }

    /**
     * Sets the reset time.
     *
     * @param param
     *            the new reset time
     */
    public void setResetTime(final java.sql.Timestamp param) {
        SCV_RESET_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the start count.
     *
     * @return the start count
     */
    public int getStartCount() {
        return SCV_START_COUNT;
    }

    /**
     * Sets the start count.
     *
     * @param param
     *            the new start count
     */
    public void setStartCount(final int param) {
        SCV_START_COUNT = param;
    }

    /**
     * Sets the start count.
     *
     * @param param
     *            the new start count
     */
    public void setStartCount(final String param) {
        SCV_START_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final Host param) {
        host = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(SCV_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (SchedulerValue) obj;
        return SCV_ID == other.SCV_ID;
    }
}
