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
 * The Class UploadHistory.
 */
public class UploadHistory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2656938064101246494L;

    /** The dat id. */
    protected Long DAT_ID;

    /** The sta code. */
    protected String STA_CODE;

    /** The uph destination. */
    protected String UPH_DESTINATION;

    /** The uph file name. */
    protected String UPH_FILE_NAME;

    /** The uph file size. */
    protected long UPH_FILE_SIZE;

    /** The uph finish time. */
    protected BigDecimal UPH_FINISH_TIME;

    /** The uph id. */
    protected long UPH_ID;

    /** The uph meta stream. */
    protected String UPH_META_STREAM;

    /** The uph meta time. */
    protected String UPH_META_TIME;

    /** The uph meta type. */
    protected String UPH_META_TYPE;

    /** The uph priority. */
    protected int UPH_PRIORITY;

    /** The uph queue time. */
    protected BigDecimal UPH_QUEUE_TIME;

    /** The uph requeue count. */
    protected int UPH_REQUEUE_COUNT;

    /** The uph scheduled time. */
    protected BigDecimal UPH_SCHEDULED_TIME;

    /** The uph start time. */
    protected BigDecimal UPH_START_TIME;

    /** The uph arrival time. */
    protected BigDecimal UPH_RETRIEVAL_TIME;

    /** The uph put time. */
    protected BigDecimal UPH_PUT_TIME;

    /** The uph time base. */
    protected BigDecimal UPH_TIME_BASE;

    /** The uph time step. */
    protected long UPH_TIME_STEP;

    /** The uph duration. */
    protected long UPH_DURATION;

    /** The uph sent. */
    protected long UPH_SENT;

    /** The uph transfer module. */
    protected String UPH_TRANSFER_MODULE;

    /** The uph transfer server. */
    protected String UPH_TRANSFER_SERVER;

    /** The uph host address. */
    protected String UPH_HOST_ADDRESS;

    /** The uph network code. */
    protected String UPH_NETWORK_CODE;

    /** The data transfer. */
    protected DataTransfer dataTransfer;

    /**
     * Instantiates a new upload history.
     */
    public UploadHistory() {
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
     * Gets the destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return UPH_DESTINATION;
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final String param) {
        UPH_DESTINATION = param;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return UPH_FILE_NAME;
    }

    /**
     * Sets the file name.
     *
     * @param param
     *            the new file name
     */
    public void setFileName(final String param) {
        UPH_FILE_NAME = param;
    }

    /**
     * Gets the file size.
     *
     * @return the file size
     */
    public long getFileSize() {
        return UPH_FILE_SIZE;
    }

    /**
     * Sets the file size.
     *
     * @param param
     *            the new file size
     */
    public void setFileSize(final long param) {
        UPH_FILE_SIZE = param;
    }

    /**
     * Sets the file size.
     *
     * @param param
     *            the new file size
     */
    public void setFileSize(final String param) {
        UPH_FILE_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the finish time.
     *
     * @return the finish time
     */
    public java.sql.Timestamp getFinishTime() {
        return bigDecimalToTimestamp(UPH_FINISH_TIME);
    }

    /**
     * Sets the finish time.
     *
     * @param param
     *            the new finish time
     */
    public void setFinishTime(final java.sql.Timestamp param) {
        UPH_FINISH_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return UPH_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        UPH_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        UPH_ID = Long.parseLong(param);
    }

    /**
     * Gets the meta stream.
     *
     * @return the meta stream
     */
    public String getMetaStream() {
        return UPH_META_STREAM;
    }

    /**
     * Sets the meta stream.
     *
     * @param param
     *            the new meta stream
     */
    public void setMetaStream(final String param) {
        UPH_META_STREAM = param;
    }

    /**
     * Gets the meta time.
     *
     * @return the meta time
     */
    public String getMetaTime() {
        return UPH_META_TIME;
    }

    /**
     * Sets the meta time.
     *
     * @param param
     *            the new meta time
     */
    public void setMetaTime(final String param) {
        UPH_META_TIME = param;
    }

    /**
     * Gets the meta type.
     *
     * @return the meta type
     */
    public String getMetaType() {
        return UPH_META_TYPE;
    }

    /**
     * Sets the meta type.
     *
     * @param param
     *            the new meta type
     */
    public void setMetaType(final String param) {
        UPH_META_TYPE = param;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return UPH_PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final int param) {
        UPH_PRIORITY = param;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final String param) {
        UPH_PRIORITY = Integer.parseInt(param);
    }

    /**
     * Gets the queue time.
     *
     * @return the queue time
     */
    public java.sql.Timestamp getQueueTime() {
        return bigDecimalToTimestamp(UPH_QUEUE_TIME);
    }

    /**
     * Sets the queue time.
     *
     * @param param
     *            the new queue time
     */
    public void setQueueTime(final java.sql.Timestamp param) {
        UPH_QUEUE_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the requeue count.
     *
     * @return the requeue count
     */
    public int getRequeueCount() {
        return UPH_REQUEUE_COUNT;
    }

    /**
     * Sets the requeue count.
     *
     * @param param
     *            the new requeue count
     */
    public void setRequeueCount(final int param) {
        UPH_REQUEUE_COUNT = param;
    }

    /**
     * Sets the requeue count.
     *
     * @param param
     *            the new requeue count
     */
    public void setRequeueCount(final String param) {
        UPH_REQUEUE_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    public java.sql.Timestamp getScheduledTime() {
        return bigDecimalToTimestamp(UPH_SCHEDULED_TIME);
    }

    /**
     * Sets the scheduled time.
     *
     * @param param
     *            the new scheduled time
     */
    public void setScheduledTime(final java.sql.Timestamp param) {
        UPH_SCHEDULED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public java.sql.Timestamp getStartTime() {
        return bigDecimalToTimestamp(UPH_START_TIME);
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    public void setStartTime(final java.sql.Timestamp param) {
        UPH_START_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the retrieval time.
     *
     * @return the retrieval time
     */
    public java.sql.Timestamp getRetrievalTime() {
        return bigDecimalToTimestamp(UPH_RETRIEVAL_TIME);
    }

    /**
     * Sets the retrieval time.
     *
     * @param param
     *            the new retrieval time
     */
    public void setRetrievalTime(final java.sql.Timestamp param) {
        UPH_RETRIEVAL_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the puts the time.
     *
     * @return the puts the time
     */
    public java.sql.Timestamp getPutTime() {
        return bigDecimalToTimestamp(UPH_PUT_TIME);
    }

    /**
     * Sets the puts the time.
     *
     * @param param
     *            the new puts the time
     */
    public void setPutTime(final java.sql.Timestamp param) {
        UPH_PUT_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public java.sql.Timestamp getTimeBase() {
        return bigDecimalToTimestamp(UPH_TIME_BASE);
    }

    /**
     * Sets the time base.
     *
     * @param param
     *            the new time base
     */
    public void setTimeBase(final java.sql.Timestamp param) {
        UPH_TIME_BASE = timestampToBigDecimal(param);
    }

    /**
     * Gets the time step.
     *
     * @return the time step
     */
    public long getTimeStep() {
        return UPH_TIME_STEP;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final long param) {
        UPH_TIME_STEP = param;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final String param) {
        UPH_TIME_STEP = Long.parseLong(param);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return UPH_DURATION;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final long param) {
        UPH_DURATION = param;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final String param) {
        UPH_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return UPH_SENT;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final long param) {
        UPH_SENT = param;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final String param) {
        UPH_SENT = Long.parseLong(param);
    }

    /**
     * Gets the transfer module.
     *
     * @return the transfer module
     */
    public String getTransferModule() {
        return UPH_TRANSFER_MODULE;
    }

    /**
     * Sets the transfer module.
     *
     * @param param
     *            the new transfer module
     */
    public void setTransferModule(final String param) {
        UPH_TRANSFER_MODULE = param;
    }

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     */
    public String getTransferServer() {
        return UPH_TRANSFER_SERVER;
    }

    /**
     * Sets the transfer server.
     *
     * @param param
     *            the new transfer server
     */
    public void setTransferServer(final String param) {
        UPH_TRANSFER_SERVER = param;
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     */
    public String getHostAddress() {
        return UPH_HOST_ADDRESS;
    }

    /**
     * Sets the host address.
     *
     * @param param
     *            the new host address
     */
    public void setHostAddress(final String param) {
        UPH_HOST_ADDRESS = param;
    }

    /**
     * Gets the network code.
     *
     * @return the network code
     */
    public String getNetworkCode() {
        return UPH_NETWORK_CODE;
    }

    /**
     * Sets the network code.
     *
     * @param param
     *            the new network code
     */
    public void setNetworkCode(final String param) {
        UPH_NETWORK_CODE = param;
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(UPH_ID);
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
        final var other = (UploadHistory) obj;
        return UPH_ID == other.UPH_ID;
    }
}
