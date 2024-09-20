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
 * The Class ProductStatus.
 */
public class ProductStatus extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7314007936463517034L;

    /** The prs comment. */
    protected String PRS_COMMENT;

    /** The prs id. */
    protected long PRS_ID;

    /** The prs last update. */
    protected BigDecimal PRS_LAST_UPDATE;

    /** The prs schedule time. */
    protected BigDecimal PRS_SCHEDULE_TIME;

    /** The prs step. */
    protected long PRS_STEP;

    /** The prs stream. */
    protected String PRS_STREAM;

    /** The prs time. */
    protected String PRS_TIME;

    /** The sta buffer. */
    protected long PRS_BUFFER;

    /** The prs time base. */
    protected BigDecimal PRS_TIME_BASE;

    /** The prs type. */
    protected String PRS_TYPE;

    /** The prs user status. */
    protected String PRS_USER_STATUS;

    /** The sta code. */
    protected String STA_CODE;

    /**
     * Instantiates a new product status.
     */
    public ProductStatus() {
    }

    /**
     * Instantiates a new product status.
     *
     * @param id
     *            the id
     */
    public ProductStatus(final long id) {
        setId(id);
    }

    /**
     * Instantiates a new product status.
     *
     * @param id
     *            the id
     */
    public ProductStatus(final String id) {
        setId(id);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return PRS_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        PRS_COMMENT = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return PRS_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        PRS_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        PRS_ID = Long.parseLong(param);
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    public java.sql.Timestamp getLastUpdate() {
        return bigDecimalToTimestamp(PRS_LAST_UPDATE);
    }

    /**
     * Sets the last update.
     *
     * @param param
     *            the new last update
     */
    public void setLastUpdate(final java.sql.Timestamp param) {
        PRS_LAST_UPDATE = timestampToBigDecimal(param);
    }

    /**
     * Gets the schedule time.
     *
     * @return the schedule time
     */
    public java.sql.Timestamp getScheduleTime() {
        return bigDecimalToTimestamp(PRS_SCHEDULE_TIME);
    }

    /**
     * Sets the schedule time.
     *
     * @param param
     *            the new schedule time
     */
    public void setScheduleTime(final java.sql.Timestamp param) {
        PRS_SCHEDULE_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the step.
     *
     * @return the step
     */
    public long getStep() {
        return PRS_STEP;
    }

    /**
     * Sets the step.
     *
     * @param param
     *            the new step
     */
    public void setStep(final long param) {
        PRS_STEP = param;
    }

    /**
     * Sets the step.
     *
     * @param param
     *            the new step
     */
    public void setStep(final String param) {
        PRS_STEP = Long.parseLong(param);
    }

    /**
     * Gets the buffer.
     *
     * @return the buffer
     */
    public long getBuffer() {
        return PRS_BUFFER;
    }

    /**
     * Sets the buffer.
     *
     * @param param
     *            the new buffer
     */
    public void setBuffer(final long param) {
        PRS_BUFFER = param;
    }

    /**
     * Sets the buffer.
     *
     * @param param
     *            the new buffer
     */
    public void setBuffer(final String param) {
        PRS_BUFFER = Long.parseLong(param);
    }

    /**
     * Gets the stream.
     *
     * @return the stream
     */
    public String getStream() {
        return PRS_STREAM;
    }

    /**
     * Sets the stream.
     *
     * @param param
     *            the new stream
     */
    public void setStream(final String param) {
        PRS_STREAM = param;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public String getTime() {
        return PRS_TIME;
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final String param) {
        PRS_TIME = param;
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public java.sql.Timestamp getTimeBase() {
        return bigDecimalToTimestamp(PRS_TIME_BASE);
    }

    /**
     * Sets the time base.
     *
     * @param param
     *            the new time base
     */
    public void setTimeBase(final java.sql.Timestamp param) {
        PRS_TIME_BASE = timestampToBigDecimal(param);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return PRS_TYPE;
    }

    /**
     * Sets the type.
     *
     * @param param
     *            the new type
     */
    public void setType(final String param) {
        PRS_TYPE = param;
    }

    /**
     * Gets the user status.
     *
     * @return the user status
     */
    public String getUserStatus() {
        return PRS_USER_STATUS;
    }

    /**
     * Sets the user status.
     *
     * @param param
     *            the new user status
     */
    public void setUserStatus(final String param) {
        PRS_USER_STATUS = param;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return STA_CODE;
    }

    /**
     * Sets the status code.
     *
     * @param param
     *            the new status code
     */
    public void setStatusCode(final String param) {
        STA_CODE = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(PRS_ID);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (ProductStatus) obj;
        return PRS_ID == other.PRS_ID;
    }
}
