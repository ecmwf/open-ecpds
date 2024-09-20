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
 * The Class DataTransfer.
 */
public class DataTransfer extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9043274581568789473L;

    /** The daf id. */
    protected long DAF_ID;

    /** The dat comment. */
    protected String DAT_COMMENT;

    /** The dat deleted. */
    protected boolean DAT_DELETED;

    /** The dat duration. */
    protected long DAT_DURATION;

    /** The dat expiry time. */
    protected BigDecimal DAT_EXPIRY_TIME;

    /** The dat failed time. */
    protected BigDecimal DAT_FAILED_TIME;

    /** The dat finish time. */
    protected BigDecimal DAT_FINISH_TIME;

    /** The dat first finish time. */
    protected BigDecimal DAT_FIRST_FINISH_TIME;

    /** The dat id. */
    protected long DAT_ID;

    /** The dat identity. */
    protected String DAT_IDENTITY;

    /** The dat priority. */
    protected int DAT_PRIORITY;

    /**
     * The dat queue time. New scheduled time when it is re-scheduled (the original scheduled time is not changed).
     */
    protected BigDecimal DAT_QUEUE_TIME;

    /** The dat requeue count. */
    protected int DAT_REQUEUE_COUNT;

    /** The dat replicate count. */
    protected int DAT_REPLICATE_COUNT;

    /** The dat requeue history. */
    protected int DAT_REQUEUE_HISTORY;

    /** The dat retry time. */
    protected BigDecimal DAT_RETRY_TIME;

    /** The dat scheduled time. */
    protected BigDecimal DAT_SCHEDULED_TIME;

    /** The dat sent. */
    protected long DAT_SENT;

    /** The dat size. */
    protected long DAT_SIZE;

    /** The dat start count. */
    protected int DAT_START_COUNT;

    /**
     * The dat start time. When the TransferScheduler start the transmission.
     */
    protected BigDecimal DAT_START_TIME;

    /** The dat put time. When the DataMover start the transmission. */
    protected BigDecimal DAT_PUT_TIME;

    /** The dat target. */
    protected String DAT_TARGET;

    /** The dat time base. */
    protected BigDecimal DAT_TIME_BASE;

    /** The dat time step. */
    protected long DAT_TIME_STEP;

    /** The dat unique key. */
    protected String DAT_UNIQUE_KEY;

    /** The dat user status. */
    protected String DAT_USER_STATUS;

    /** The des name. */
    protected String DES_NAME;

    /** The hos name. */
    protected Integer HOS_NAME;

    /** The mov id. */
    protected Long MOV_ID;

    /** The sta code. */
    protected String STA_CODE;

    /** The trs name. */
    protected String TRS_NAME;

    /** The trs name original. */
    protected String TRS_NAME_ORIGINAL;

    /** The dat replicated. */
    protected boolean DAT_REPLICATED;

    /** The hos name backup. */
    protected Integer HOS_NAME_BACKUP;

    /** The hos name proxy. */
    protected Integer HOS_NAME_PROXY;

    /** The dat replicate time. */
    protected BigDecimal DAT_REPLICATE_TIME;

    /** The dat backup time. */
    protected BigDecimal DAT_BACKUP_TIME;

    /** The dat proxy time. */
    protected BigDecimal DAT_PROXY_TIME;

    /** The asap. */
    protected boolean DAT_ASAP;

    /** The event. */
    protected boolean DAT_EVENT;

    /** The original transfer server. */
    protected TransferServer originalTransferServer;

    /** The transfer server. */
    protected TransferServer transferServer;

    /** The destination. */
    protected Destination destination;

    /** The data file. */
    protected DataFile dataFile;

    /** The host. */
    protected Host host;

    /** The backup host. */
    protected Host backupHost;

    /** The proxy host. */
    protected Host proxyHost;

    /** The monitoring value. */
    protected MonitoringValue monitoringValue;

    /** The mover name. */
    protected String moverName;

    /** The connect options. */
    protected String connectOptions;

    /** Allow triggering a notification *. */
    protected boolean notify = false;

    /** Allow passing the duration on close from the mover to the master *. */
    protected long durationOnClose = -1;

    /** Allow passing the proxy name if sent from a proxy *. */
    protected String proxyName = null;

    /** The ratio. */
    protected double ratio;

    /** The statistics. */
    protected String statistics;

    /** The compressed. */
    protected String compressed = null;

    /** The compressedOnTheFly. */
    protected boolean compressedOnTheFly = false;

    /**
     * Gets the mover name.
     *
     * @return the mover name
     */
    public String getMoverName() {
        return moverName;
    }

    /**
     * Sets the mover name.
     *
     * @param moverName
     *            the new mover name
     */
    public void setMoverName(final String moverName) {
        this.moverName = moverName;
    }

    /**
     * Gets the ratio.
     *
     * @return the ratio
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * Sets the ratio.
     *
     * @param ratio
     *            the ratio
     */
    public void setRatio(final double ratio) {
        this.ratio = ratio;
    }

    /**
     * Instantiates a new data transfer.
     */
    public DataTransfer() {
    }

    /**
     * Instantiates a new data transfer.
     *
     * @param id
     *            the id
     */
    public DataTransfer(final long id) {
        setId(id);
    }

    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    public long getDataFileId() {
        return DAF_ID;
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    public void setDataFileId(final long param) {
        DAF_ID = param;
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    public void setDataFileId(final String param) {
        DAF_ID = Long.parseLong(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return DAT_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        DAT_COMMENT = strim(param, 255);
    }

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    public boolean getDeleted() {
        return DAT_DELETED;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final boolean param) {
        DAT_DELETED = param;
    }

    /**
     * Gets the notify.
     *
     * @return the deleted
     */
    public boolean getNotify() {
        return notify;
    }

    /**
     * Sets the notify.
     *
     * @param param
     *            the new deleted
     */
    public void setNotify(final boolean param) {
        notify = param;
    }

    /**
     * Gets the compressedOnTheFly.
     *
     * @return the compressedOnTheFly
     */
    public boolean getCompressedOnTheFly() {
        return compressedOnTheFly;
    }

    /**
     * Sets the compressedOnTheFly.
     *
     * @param param
     *            the new compressedOnTheFly
     */
    public void setCompressedOnTheFly(final boolean param) {
        compressedOnTheFly = param;
    }

    /**
     * Gets the compressed.
     *
     * @return the compressed
     */
    public String getCompressed() {
        return compressed;
    }

    /**
     * Sets the compressed.
     *
     * @param param
     *            the new compressed
     */
    public void setCompressed(final String param) {
        compressed = param;
    }

    /**
     * Gets the statistics.
     *
     * @return the statistics
     */
    public String getStatistics() {
        return statistics;
    }

    /**
     * Sets the statistics.
     *
     * @param statistics
     *            the new statistics
     */
    public void setStatistics(final String statistics) {
        this.statistics = statistics;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final String param) {
        DAT_DELETED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the asap.
     *
     * @return the asap
     */
    public boolean getAsap() {
        return DAT_ASAP;
    }

    /**
     * Sets the asap.
     *
     * @param param
     *            the new asap
     */
    public void setAsap(final boolean param) {
        DAT_ASAP = param;
    }

    /**
     * Sets the asap.
     *
     * @param param
     *            the new asap
     */
    public void setAsap(final String param) {
        DAT_ASAP = Boolean.parseBoolean(param);
    }

    /**
     * Gets the event.
     *
     * @return the event
     */
    public boolean getEvent() {
        return DAT_EVENT;
    }

    /**
     * Sets the event.
     *
     * @param param
     *            the new event
     */
    public void setEvent(final boolean param) {
        DAT_EVENT = param;
    }

    /**
     * Sets the event.
     *
     * @param param
     *            the new event
     */
    public void setEvent(final String param) {
        DAT_EVENT = Boolean.parseBoolean(param);
    }

    /**
     * Gets the replicated.
     *
     * @return the replicated
     */
    public boolean getReplicated() {
        return DAT_REPLICATED;
    }

    /**
     * Sets the replicated.
     *
     * @param param
     *            the new replicated
     */
    public void setReplicated(final boolean param) {
        DAT_REPLICATED = param;
    }

    /**
     * Sets the replicated.
     *
     * @param param
     *            the new replicated
     */
    public void setReplicated(final String param) {
        DAT_REPLICATED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the backup host name.
     *
     * @return the backup host name
     */
    public String getBackupHostName() {
        return integerToString(HOS_NAME_BACKUP);
    }

    /**
     * Sets the backup host name.
     *
     * @param param
     *            the new backup host name
     */
    public void setBackupHostName(final String param) {
        HOS_NAME_BACKUP = stringToInteger(param);
    }

    /**
     * Gets the proxy host name.
     *
     * @return the proxy host name
     */
    public String getProxyHostName() {
        return integerToString(HOS_NAME_PROXY);
    }

    /**
     * Sets the proxy host name.
     *
     * @param param
     *            the new proxy host name
     */
    public void setProxyHostName(final String param) {
        HOS_NAME_PROXY = stringToInteger(param);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return DAT_DURATION;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final long param) {
        DAT_DURATION = param;
    }

    /**
     * Gets the duration on close.
     *
     * @return the duration
     */
    public long getDurationOnClose() {
        return durationOnClose;
    }

    /**
     * Sets the duration on close.
     *
     * @param param
     *            the new duration
     */
    public void setDurationOnClose(final long param) {
        durationOnClose = param;
    }

    /**
     * Gets the proxy name.
     *
     * @return the duration
     */
    public String getProxyName() {
        return proxyName;
    }

    /**
     * Sets the proxy name.
     *
     * @param param
     *            the new duration
     */
    public void setProxyName(final String param) {
        proxyName = param;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final String param) {
        DAT_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the expiry time.
     *
     * @return the expiry time
     */
    public java.sql.Timestamp getExpiryTime() {
        return bigDecimalToTimestamp(DAT_EXPIRY_TIME);
    }

    /**
     * Sets the expiry time.
     *
     * @param param
     *            the new expiry time
     */
    public void setExpiryTime(final java.sql.Timestamp param) {
        DAT_EXPIRY_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the failed time.
     *
     * @return the failed time
     */
    public java.sql.Timestamp getFailedTime() {
        return bigDecimalToTimestamp(DAT_FAILED_TIME);
    }

    /**
     * Sets the failed time.
     *
     * @param param
     *            the new failed time
     */
    public void setFailedTime(final java.sql.Timestamp param) {
        DAT_FAILED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the finish time.
     *
     * @return the finish time
     */
    public java.sql.Timestamp getFinishTime() {
        return bigDecimalToTimestamp(DAT_FINISH_TIME);
    }

    /**
     * Sets the finish time.
     *
     * @param param
     *            the new finish time
     */
    public void setFinishTime(final java.sql.Timestamp param) {
        DAT_FINISH_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the first finish time.
     *
     * @return the first finish time
     */
    public java.sql.Timestamp getFirstFinishTime() {
        return bigDecimalToTimestamp(DAT_FIRST_FINISH_TIME);
    }

    /**
     * Sets the first finish time.
     *
     * @param param
     *            the new first finish time
     */
    public void setFirstFinishTime(final java.sql.Timestamp param) {
        DAT_FIRST_FINISH_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return DAT_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        DAT_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        DAT_ID = Long.parseLong(param);
    }

    /**
     * Gets the identity.
     *
     * @return the identity
     */
    public String getIdentity() {
        return DAT_IDENTITY;
    }

    /**
     * Sets the identity.
     *
     * @param param
     *            the new identity
     */
    public void setIdentity(final String param) {
        DAT_IDENTITY = param;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return DAT_PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final int param) {
        DAT_PRIORITY = param;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final String param) {
        DAT_PRIORITY = Integer.parseInt(param);
    }

    /**
     * Gets the queue time.
     *
     * @return the queue time
     */
    public java.sql.Timestamp getQueueTime() {
        return bigDecimalToTimestamp(DAT_QUEUE_TIME);
    }

    /**
     * Sets the queue time.
     *
     * @param param
     *            the new queue time
     */
    public void setQueueTime(final java.sql.Timestamp param) {
        DAT_QUEUE_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the requeue count.
     *
     * @return the requeue count
     */
    public int getRequeueCount() {
        return DAT_REQUEUE_COUNT;
    }

    /**
     * Sets the requeue count.
     *
     * @param param
     *            the new requeue count
     */
    public void setRequeueCount(final int param) {
        DAT_REQUEUE_COUNT = param;
    }

    /**
     * Sets the requeue count.
     *
     * @param param
     *            the new requeue count
     */
    public void setRequeueCount(final String param) {
        DAT_REQUEUE_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the replicate count.
     *
     * @return the replicate count
     */
    public int getReplicateCount() {
        return DAT_REPLICATE_COUNT;
    }

    /**
     * Sets the replicate count.
     *
     * @param param
     *            the new replicate count
     */
    public void setReplicateCount(final int param) {
        DAT_REPLICATE_COUNT = param;
    }

    /**
     * Sets the replicate count.
     *
     * @param param
     *            the new replicate count
     */
    public void setReplicateCount(final String param) {
        DAT_REPLICATE_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the requeue history.
     *
     * @return the requeue history
     */
    public int getRequeueHistory() {
        return DAT_REQUEUE_HISTORY;
    }

    /**
     * Sets the requeue history.
     *
     * @param param
     *            the new requeue history
     */
    public void setRequeueHistory(final int param) {
        DAT_REQUEUE_HISTORY = param;
    }

    /**
     * Sets the requeue history.
     *
     * @param param
     *            the new requeue history
     */
    public void setRequeueHistory(final String param) {
        DAT_REQUEUE_HISTORY = Integer.parseInt(param);
    }

    /**
     * Gets the retry time.
     *
     * @return the retry time
     */
    public java.sql.Timestamp getRetryTime() {
        return bigDecimalToTimestamp(DAT_RETRY_TIME);
    }

    /**
     * Sets the retry time.
     *
     * @param param
     *            the new retry time
     */
    public void setRetryTime(final java.sql.Timestamp param) {
        DAT_RETRY_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    public java.sql.Timestamp getScheduledTime() {
        return bigDecimalToTimestamp(DAT_SCHEDULED_TIME);
    }

    /**
     * Sets the scheduled time.
     *
     * @param param
     *            the new scheduled time
     */
    public void setScheduledTime(final java.sql.Timestamp param) {
        DAT_SCHEDULED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return DAT_SENT;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final long param) {
        DAT_SENT = param;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final String param) {
        DAT_SENT = Long.parseLong(param);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return DAT_SIZE;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final long param) {
        DAT_SIZE = param;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final String param) {
        DAT_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the start count.
     *
     * @return the start count
     */
    public int getStartCount() {
        return DAT_START_COUNT;
    }

    /**
     * Sets the start count.
     *
     * @param param
     *            the new start count
     */
    public void setStartCount(final int param) {
        DAT_START_COUNT = param;
    }

    /**
     * Sets the start count.
     *
     * @param param
     *            the new start count
     */
    public void setStartCount(final String param) {
        DAT_START_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public java.sql.Timestamp getStartTime() {
        return bigDecimalToTimestamp(DAT_START_TIME);
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    public void setStartTime(final java.sql.Timestamp param) {
        DAT_START_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the puts the time.
     *
     * @return the puts the time
     */
    public java.sql.Timestamp getPutTime() {
        return bigDecimalToTimestamp(DAT_PUT_TIME);
    }

    /**
     * Sets the puts the time.
     *
     * @param param
     *            the new puts the time
     */
    public void setPutTime(final java.sql.Timestamp param) {
        DAT_PUT_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return DAT_TARGET;
    }

    /**
     * Sets the target.
     *
     * @param param
     *            the new target
     */
    public void setTarget(final String param) {
        DAT_TARGET = param;
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public java.sql.Timestamp getTimeBase() {
        return bigDecimalToTimestamp(DAT_TIME_BASE);
    }

    /**
     * Sets the time base.
     *
     * @param param
     *            the new time base
     */
    public void setTimeBase(final java.sql.Timestamp param) {
        DAT_TIME_BASE = timestampToBigDecimal(param);
    }

    /**
     * Gets the replicate time.
     *
     * @return the replicate time
     */
    public java.sql.Timestamp getReplicateTime() {
        return bigDecimalToTimestamp(DAT_REPLICATE_TIME);
    }

    /**
     * Sets the replicate time.
     *
     * @param param
     *            the new replicate time
     */
    public void setReplicateTime(final java.sql.Timestamp param) {
        DAT_REPLICATE_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the backup time.
     *
     * @return the backup time
     */
    public java.sql.Timestamp getBackupTime() {
        return bigDecimalToTimestamp(DAT_BACKUP_TIME);
    }

    /**
     * Sets the backup time.
     *
     * @param param
     *            the new backup time
     */
    public void setBackupTime(final java.sql.Timestamp param) {
        DAT_BACKUP_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the proxy time.
     *
     * @return the proxy time
     */
    public java.sql.Timestamp getProxyTime() {
        return bigDecimalToTimestamp(DAT_PROXY_TIME);
    }

    /**
     * Sets the proxy time.
     *
     * @param param
     *            the new proxy time
     */
    public void setProxyTime(final java.sql.Timestamp param) {
        DAT_PROXY_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the time step.
     *
     * @return the time step
     */
    public long getTimeStep() {
        return DAT_TIME_STEP;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final long param) {
        DAT_TIME_STEP = param;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final String param) {
        DAT_TIME_STEP = Long.parseLong(param);
    }

    /**
     * Gets the unique key.
     *
     * @return the unique key
     */
    public String getUniqueKey() {
        return DAT_UNIQUE_KEY;
    }

    /**
     * Sets the unique key.
     *
     * @param param
     *            the new unique key
     */
    public void setUniqueKey(final String param) {
        DAT_UNIQUE_KEY = param;
    }

    /**
     * Gets the user status.
     *
     * @return the user status
     */
    public String getUserStatus() {
        return DAT_USER_STATUS;
    }

    /**
     * Sets the user status.
     *
     * @param param
     *            the new user status
     */
    public void setUserStatus(final String param) {
        DAT_USER_STATUS = param;
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return DES_NAME;
    }

    /**
     * Sets the destination name.
     *
     * @param param
     *            the new destination name
     */
    public void setDestinationName(final String param) {
        DES_NAME = param;
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
     * Gets the monitoring value id.
     *
     * @return the monitoring value id
     */
    public Long getMonitoringValueId() {
        return MOV_ID;
    }

    /**
     * Sets the monitoring value id.
     *
     * @param param
     *            the new monitoring value id
     */
    public void setMonitoringValueId(final Long param) {
        MOV_ID = param;
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
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    public String getTransferServerName() {
        return TRS_NAME;
    }

    /**
     * Sets the transfer server name.
     *
     * @param param
     *            the new transfer server name
     */
    public void setTransferServerName(final String param) {
        TRS_NAME = param;
    }

    /**
     * Gets the original transfer server name.
     *
     * @return the original transfer server name
     */
    public String getOriginalTransferServerName() {
        return TRS_NAME_ORIGINAL;
    }

    /**
     * Sets the original transfer server name.
     *
     * @param param
     *            the new original transfer server name
     */
    public void setOriginalTransferServerName(final String param) {
        TRS_NAME_ORIGINAL = param;
    }

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     */
    public TransferServer getTransferServer() {
        return transferServer;
    }

    /**
     * Sets the transfer server.
     *
     * @param param
     *            the new transfer server
     */
    public void setTransferServer(final TransferServer param) {
        transferServer = param;
    }

    /**
     * Gets the original transfer server.
     *
     * @return the original transfer server
     */
    public TransferServer getOriginalTransferServer() {
        return originalTransferServer;
    }

    /**
     * Sets the original transfer server.
     *
     * @param param
     *            the new original transfer server
     */
    public void setOriginalTransferServer(final TransferServer param) {
        originalTransferServer = param;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final Destination param) {
        destination = param;
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     */
    public DataFile getDataFile() {
        return dataFile;
    }

    /**
     * Sets the data file.
     *
     * @param param
     *            the new data file
     */
    public void setDataFile(final DataFile param) {
        dataFile = param;
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
     * Gets the backup host.
     *
     * @return the backup host
     */
    public Host getBackupHost() {
        return backupHost;
    }

    /**
     * Sets the backup host.
     *
     * @param param
     *            the new backup host
     */
    public void setBackupHost(final Host param) {
        backupHost = param;
    }

    /**
     * Gets the proxy host.
     *
     * @return the proxy host
     */
    public Host getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets the proxy host.
     *
     * @param param
     *            the new proxy host
     */
    public void setProxyHost(final Host param) {
        proxyHost = param;
    }

    /**
     * Gets the monitoring value.
     *
     * @return the monitoring value
     */
    public MonitoringValue getMonitoringValue() {
        return monitoringValue;
    }

    /**
     * Sets the monitoring value.
     *
     * @param param
     *            the new monitoring value
     */
    public void setMonitoringValue(final MonitoringValue param) {
        monitoringValue = param;
    }

    /**
     * Gets the connect options.
     *
     * @return the connect options
     */
    public String getConnectOptions() {
        return connectOptions;
    }

    /**
     * Sets the connect options.
     *
     * @param param
     *            the new connect options
     */
    public void setConnectOptions(final String param) {
        connectOptions = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(DAT_ID);
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
        final var other = (DataTransfer) obj;
        return DAT_ID == other.DAT_ID;
    }
}
