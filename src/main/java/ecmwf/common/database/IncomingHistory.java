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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class IncomingHistory.
 */
public class IncomingHistory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3140795959633785200L;

    /** The inh id. */
    protected long INH_ID;

    /** The dat id. */
    protected Long DAT_ID;

    /** The inh destination. */
    protected String INH_DESTINATION;

    /** The inh file name. */
    protected String INH_FILE_NAME;

    /** The inh file size. */
    protected long INH_FILE_SIZE;

    /** The inh meta stream. */
    protected String INH_META_STREAM;

    /** The inh meta time. */
    protected String INH_META_TIME;

    /** The inh meta type. */
    protected String INH_META_TYPE;

    /** The inh priority. */
    protected int INH_PRIORITY;

    /** The inh start time. */
    protected BigDecimal INH_START_TIME;

    /** The inh scheduled time. */
    protected BigDecimal INH_SCHEDULED_TIME;

    /** The inh time base. */
    protected BigDecimal INH_TIME_BASE;

    /** The inh time step. */
    protected long INH_TIME_STEP;

    /** The inh duration. */
    protected long INH_DURATION;

    /** The inh sent. */
    protected long INH_SENT;

    /** The inh protocol. */
    protected String INH_PROTOCOL;

    /** The inh transfer server. */
    protected String INH_TRANSFER_SERVER;

    /** The inh host address. */
    protected String INH_HOST_ADDRESS;

    /** The inh user name. */
    protected String INH_USER_NAME;

    /** The inh upload. */
    protected boolean INH_UPLOAD;

    /** The data transfer. */
    protected DataTransfer dataTransfer;

    /**
     * Instantiates a new incoming history.
     */
    public IncomingHistory() {
    }

    /**
     * Gets the data transfer id.
     *
     * @return the data transfer id
     */
    public Long getDataTransferId() {
        return DAT_ID;
    }

    /**
     * Sets the data transfer id.
     *
     * @param param
     *            the new data transfer id
     */
    public void setDataTransferId(final Long param) {
        DAT_ID = param;
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
     * Gets the destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return INH_DESTINATION;
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final String param) {
        INH_DESTINATION = param;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return INH_FILE_NAME;
    }

    /**
     * Sets the file name.
     *
     * @param param
     *            the new file name
     */
    public void setFileName(final String param) {
        INH_FILE_NAME = param;
    }

    /**
     * Gets the file size.
     *
     * @return the file size
     */
    public long getFileSize() {
        return INH_FILE_SIZE;
    }

    /**
     * Sets the file size.
     *
     * @param param
     *            the new file size
     */
    public void setFileSize(final long param) {
        INH_FILE_SIZE = param;
    }

    /**
     * Sets the file size.
     *
     * @param param
     *            the new file size
     */
    public void setFileSize(final String param) {
        INH_FILE_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return INH_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        INH_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        INH_ID = Long.parseLong(param);
    }

    /**
     * Gets the meta stream.
     *
     * @return the meta stream
     */
    public String getMetaStream() {
        return INH_META_STREAM;
    }

    /**
     * Sets the meta stream.
     *
     * @param param
     *            the new meta stream
     */
    public void setMetaStream(final String param) {
        INH_META_STREAM = param;
    }

    /**
     * Gets the meta time.
     *
     * @return the meta time
     */
    public String getMetaTime() {
        return INH_META_TIME;
    }

    /**
     * Sets the meta time.
     *
     * @param param
     *            the new meta time
     */
    public void setMetaTime(final String param) {
        INH_META_TIME = param;
    }

    /**
     * Gets the meta type.
     *
     * @return the meta type
     */
    public String getMetaType() {
        return INH_META_TYPE;
    }

    /**
     * Sets the meta type.
     *
     * @param param
     *            the new meta type
     */
    public void setMetaType(final String param) {
        INH_META_TYPE = param;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return INH_PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final int param) {
        INH_PRIORITY = param;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final String param) {
        INH_PRIORITY = Integer.parseInt(param);
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    public java.sql.Timestamp getScheduledTime() {
        return bigDecimalToTimestamp(INH_SCHEDULED_TIME);
    }

    /**
     * Sets the scheduled time.
     *
     * @param param
     *            the new scheduled time
     */
    public void setScheduledTime(final java.sql.Timestamp param) {
        INH_SCHEDULED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public java.sql.Timestamp getStartTime() {
        return bigDecimalToTimestamp(INH_START_TIME);
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    public void setStartTime(final java.sql.Timestamp param) {
        INH_START_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public java.sql.Timestamp getTimeBase() {
        return bigDecimalToTimestamp(INH_TIME_BASE);
    }

    /**
     * Sets the time base.
     *
     * @param param
     *            the new time base
     */
    public void setTimeBase(final java.sql.Timestamp param) {
        INH_TIME_BASE = timestampToBigDecimal(param);
    }

    /**
     * Gets the time step.
     *
     * @return the time step
     */
    public long getTimeStep() {
        return INH_TIME_STEP;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final long param) {
        INH_TIME_STEP = param;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final String param) {
        INH_TIME_STEP = Long.parseLong(param);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return INH_DURATION;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final long param) {
        INH_DURATION = param;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final String param) {
        INH_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return INH_SENT;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final long param) {
        INH_SENT = param;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final String param) {
        INH_SENT = Long.parseLong(param);
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return INH_PROTOCOL;
    }

    /**
     * Sets the protocol.
     *
     * @param param
     *            the new protocol
     */
    public void setProtocol(final String param) {
        INH_PROTOCOL = param;
    }

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     */
    public String getTransferServer() {
        return INH_TRANSFER_SERVER;
    }

    /**
     * Sets the transfer server.
     *
     * @param param
     *            the new transfer server
     */
    public void setTransferServer(final String param) {
        INH_TRANSFER_SERVER = param;
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     */
    public String getHostAddress() {
        return INH_HOST_ADDRESS;
    }

    /**
     * Sets the host address.
     *
     * @param param
     *            the new host address
     */
    public void setHostAddress(final String param) {
        INH_HOST_ADDRESS = param;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return INH_USER_NAME;
    }

    /**
     * Sets the user name.
     *
     * @param param
     *            the new user name
     */
    public void setUserName(final String param) {
        INH_USER_NAME = param;
    }

    /**
     * Gets the upload.
     *
     * @return the upload
     */
    public boolean getUpload() {
        return INH_UPLOAD;
    }

    /**
     * Sets the upload.
     *
     * @param param
     *            the new upload
     */
    public void setUpload(final boolean param) {
        INH_UPLOAD = param;
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }

    /**
     * Sets the data transfer.
     *
     * @param param
     *            the new data transfer
     */
    public void setDataTransfer(final DataTransfer param) {
        dataTransfer = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(INH_ID);
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
        final var other = (IncomingHistory) obj;
        return INH_ID == other.INH_ID;
    }
}
