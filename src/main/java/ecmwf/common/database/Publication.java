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
 * The Class Publication.
 */
public class Publication extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7215453492398706908L;

    /** The pub scheduled time. */
    protected BigDecimal PUB_SCHEDULED_TIME;

    /** The pub processed time. */
    protected BigDecimal PUB_PROCESSED_TIME;

    /** The pub options. */
    protected String PUB_OPTIONS;

    /** The dat id. */
    protected Long DAT_ID;

    /** The pub id. */
    protected Long PUB_ID;

    /** The done. */
    protected boolean PUB_DONE;

    /**
     * Instantiates a new operation.
     */
    public Publication() {
    }

    /**
     * Instantiates a new operation.
     *
     * @param id
     *            the id
     */
    public Publication(final long id) {
        setId(id);
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    public java.sql.Timestamp getScheduledTime() {
        return bigDecimalToTimestamp(PUB_SCHEDULED_TIME);
    }

    /**
     * Sets the scheduled time.
     *
     * @param param
     *            the new scheduled time
     */
    public void setScheduledTime(final java.sql.Timestamp param) {
        PUB_SCHEDULED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the processed time.
     *
     * @return the processed time
     */
    public java.sql.Timestamp getProcessedTime() {
        return bigDecimalToTimestamp(PUB_PROCESSED_TIME);
    }

    /**
     * Sets the processed time.
     *
     * @param param
     *            the new processed time
     */
    public void setProcessedTime(final java.sql.Timestamp param) {
        PUB_PROCESSED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return PUB_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        PUB_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        PUB_ID = Long.parseLong(param);
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    public String getOptions() {
        return PUB_OPTIONS;
    }

    /**
     * Sets the options.
     *
     * @param param
     *            the new options
     */
    public void setOptions(final String param) {
        PUB_OPTIONS = param;
    }

    /**
     * Gets the data transfer id.
     *
     * @return the data transfer id
     */
    public long getDataTransferId() {
        return DAT_ID;
    }

    /**
     * Sets the data transfer id.
     *
     * @param param
     *            the new data transfer id
     */
    public void setDataTransferId(final long param) {
        DAT_ID = param;
    }

    /**
     * Sets the data transfer id.
     *
     * @param param
     *            the new data transfer id
     */
    public void setDataTransferId(final String param) {
        DAT_ID = Long.parseLong(param);
    }

    /**
     * Gets the done.
     *
     * @return the done
     */
    public boolean getDone() {
        return PUB_DONE;
    }

    /**
     * Sets the done.
     *
     * @param param
     *            the new done
     */
    public void setDone(final boolean param) {
        PUB_DONE = param;
    }

    /**
     * Sets the done.
     *
     * @param param
     *            the new done
     */
    public void setDone(final String param) {
        PUB_DONE = Boolean.parseBoolean(param);
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DAT_ID, PUB_ID, PUB_OPTIONS, PUB_SCHEDULED_TIME);
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
        final var other = (Publication) obj;
        return DAT_ID == other.DAT_ID && PUB_ID == other.PUB_ID && Objects.equals(PUB_OPTIONS, other.PUB_OPTIONS)
                && Objects.equals(PUB_SCHEDULED_TIME, other.PUB_SCHEDULED_TIME);
    }
}
