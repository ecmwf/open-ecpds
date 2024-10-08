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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class MonitoringValue.
 */
public class MonitoringValue extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3587604829404150841L;

    /** The mov earliest time. */
    protected BigDecimal MOV_EARLIEST_TIME;

    /** The mov id. */
    protected long MOV_ID;

    /** The mov latest time. */
    protected BigDecimal MOV_LATEST_TIME;

    /** The mov predicted time. */
    protected BigDecimal MOV_PREDICTED_TIME;

    /** The mov target time. */
    protected BigDecimal MOV_TARGET_TIME;

    /**
     * Instantiates a new monitoring value.
     */
    public MonitoringValue() {
    }

    /**
     * Instantiates a new monitoring value.
     *
     * @param id
     *            the id
     */
    public MonitoringValue(final long id) {
        setId(id);
    }

    /**
     * Instantiates a new monitoring value.
     *
     * @param id
     *            the id
     */
    public MonitoringValue(final String id) {
        setId(id);
    }

    /**
     * Gets the earliest time.
     *
     * @return the earliest time
     */
    public java.sql.Timestamp getEarliestTime() {
        return bigDecimalToTimestamp(MOV_EARLIEST_TIME);
    }

    /**
     * Sets the earliest time.
     *
     * @param param
     *            the new earliest time
     */
    public void setEarliestTime(final java.sql.Timestamp param) {
        MOV_EARLIEST_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return MOV_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        MOV_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        MOV_ID = Long.parseLong(param);
    }

    /**
     * Gets the latest time.
     *
     * @return the latest time
     */
    public java.sql.Timestamp getLatestTime() {
        return bigDecimalToTimestamp(MOV_LATEST_TIME);
    }

    /**
     * Sets the latest time.
     *
     * @param param
     *            the new latest time
     */
    public void setLatestTime(final java.sql.Timestamp param) {
        MOV_LATEST_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the predicted time.
     *
     * @return the predicted time
     */
    public java.sql.Timestamp getPredictedTime() {
        return bigDecimalToTimestamp(MOV_PREDICTED_TIME);
    }

    /**
     * Sets the predicted time.
     *
     * @param param
     *            the new predicted time
     */
    public void setPredictedTime(final java.sql.Timestamp param) {
        MOV_PREDICTED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the target time.
     *
     * @return the target time
     */
    public java.sql.Timestamp getTargetTime() {
        return bigDecimalToTimestamp(MOV_TARGET_TIME);
    }

    /**
     * Sets the target time.
     *
     * @param param
     *            the new target time
     */
    public void setTargetTime(final java.sql.Timestamp param) {
        MOV_TARGET_TIME = timestampToBigDecimal(param);
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(MOV_ID);
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
        final var other = (MonitoringValue) obj;
        return MOV_ID == other.MOV_ID;
    }
}
