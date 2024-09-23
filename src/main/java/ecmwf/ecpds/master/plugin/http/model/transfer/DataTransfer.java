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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Different parameters regarding the arrival to the system of a file, which are
 * relevant for monitoring issues.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.CollectionSizeBean;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.web.model.users.User;

/**
 * The Interface DataTransfer.
 */
public interface DataTransfer extends CollectionSizeBean, ArrivalMonitoringParameters, TransferMonitoringParameters {

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    void setUser(User user);

    /**
     * Gets the user.
     *
     * @return the user
     */
    User getUser();

    /**
     * Gets the data file.
     *
     * @return the data file
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    DataFile getDataFile() throws TransferException;

    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    long getDataFileId();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the formatted comment.
     *
     * @return the formatted comment
     */
    String getFormattedComment();

    /**
     * Gets the identity.
     *
     * @return the identity
     */
    String getIdentity();

    /**
     * Gets the asap.
     *
     * @return the asap
     */
    boolean getAsap();

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    boolean getDeleted();

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    boolean getBackup();

    /**
     * Gets the proxy.
     *
     * @return the proxy
     */
    boolean getProxy();

    /**
     * Gets the backup host name.
     *
     * @return the backup host name
     */
    String getBackupHostName();

    /**
     * Gets the proxy host name.
     *
     * @return the proxy host name
     */
    String getProxyHostName();

    /**
     * Gets the backup host.
     *
     * @return the backup host
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Host getBackupHost() throws TransferException;

    /**
     * Gets the proxy host.
     *
     * @return the proxy host
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Host getProxyHost() throws TransferException;

    /**
     * Gets the replicated.
     *
     * @return the replicated
     */
    boolean getReplicated();

    /**
     * Gets the unique key.
     *
     * @return the unique key
     */
    String getUniqueKey();

    /**
     * Gets the size.
     *
     * @return the size
     */
    long getSize();

    /**
     * Gets the formatted size.
     *
     * @return the formatted size
     */
    String getFormattedSize();

    /**
     * Gets the destination.
     *
     * @return the destination
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Destination getDestination() throws TransferException;

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    String getDestinationName();

    /**
     * Gets the expiry date.
     *
     * @return the expiry date
     */
    Date getExpiryDate();

    /**
     * Gets the expired.
     *
     * @return the expired
     */
    boolean getExpired();

    /**
     * Gets the host.
     *
     * @return the host
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Host getHost() throws TransferException;

    /**
     * Gets the host nick name.
     *
     * @return the host nick name
     */
    String getHostNickName();

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    String getHostName();

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    Date getScheduledTime();

    /**
     * Gets the queue time.
     *
     * @return the queue time
     */
    Date getQueueTime();

    /**
     * Gets the scheduled time minus minutes.
     *
     * @param minutesBefore
     *            the minutes before
     *
     * @return the scheduled time minus minutes
     */
    Date getScheduledTimeMinusMinutes(int minutesBefore);

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    Date getStartTime();

    /**
     * Gets the retry time.
     *
     * @return the retry time
     */
    Date getRetryTime();

    /**
     * The FIRST TIME a file is SUCCESSFULLY transferred. Even if it is requeued later and re-sent, this value won't
     * change. This is to be used for monitoring, because operators don't want to be
     *
     * @return the finish time
     */
    Date getFinishTime();

    /**
     * The date of the LAST TIME a file transfer is finished, successfully OR NOT.
     *
     * @return the real finish time
     */
    Date getRealFinishTime();

    /**
     * Gets the failed time.
     *
     * @return the failed time
     */
    Date getFailedTime();

    /**
     * Gets the start count.
     *
     * @return the start count
     */
    int getStartCount();

    /**
     * Gets the requeue count.
     *
     * @return the requeue count
     */
    int getRequeueCount();

    /**
     * Gets the status.
     *
     * @return the status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getStatus() throws TransferException;

    /**
     * Gets the formatted status.
     *
     * @return the formatted status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getFormattedStatus() throws TransferException;

    /**
     * Gets the member state formatted status.
     *
     * @return the member state formatted status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getMemberStateFormattedStatus() throws TransferException;

    /**
     * Gets the detailed status.
     *
     * @return the detailed status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getDetailedStatus() throws TransferException;

    /**
     * Gets the member state detailed status.
     *
     * @return the member state detailed status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getMemberStateDetailedStatus() throws TransferException;

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    String getStatusCode();

    /**
     * Gets the target.
     *
     * @return the target
     */
    String getTarget();

    /**
     * Gets the retrieval progress.
     *
     * @return the retrieval progress
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getRetrievalProgress() throws TransferException;

    /**
     * Gets the progress.
     *
     * @return the progress
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    int getProgress() throws TransferException;

    /**
     * Gets the sent.
     *
     * @return the sent
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    long getSent() throws TransferException;

    /**
     * Gets the formatted sent.
     *
     * @return the formatted sent
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getFormattedSent() throws TransferException;

    /**
     * Gets the duration.
     *
     * @return the duration
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    long getDuration() throws TransferException;

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getFormattedDuration() throws TransferException;

    /**
     * Gets the transfer rate.
     *
     * @return the transfer rate
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    long getTransferRate() throws TransferException;

    /**
     * Gets the formatted transfer rate.
     *
     * @return the formatted transfer rate
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getFormattedTransferRate() throws TransferException;

    /**
     * Gets the formatted transfer rate in M bits per seconds.
     *
     * @return the formatted transfer rate in M bits per seconds
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    double getFormattedTransferRateInMBitsPerSeconds() throws TransferException;

    /**
     * Gets the copy.
     *
     * @return the copy
     */
    DataTransfer getCopy();

    /**
     * Checks if is retry.
     *
     * @return true, if is retry
     */
    boolean isRetry();

    /**
     * Sets the retry.
     *
     * @param b
     *            the new retry
     */
    void setRetry(boolean b);

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    TransferServer getTransferServer() throws TransferException;

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    String getTransferServerName();

    /**
     * Gets the transfer history.
     *
     * @return the transfer history
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<TransferHistory> getTransferHistory() throws TransferException;

    /**
     * Gets the transfer history after scheduled time.
     *
     * @return the transfer history after scheduled time
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<TransferHistory> getTransferHistoryAfterScheduledTime() throws TransferException;

    /**
     * Gets the older transfers for same data file.
     *
     * @return the older transfers for same data file
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getOlderTransfersForSameDataFile() throws TransferException;

    /**
     * Sets the data file id.
     *
     * @param id
     *            the new data file id
     */
    void setDataFileId(long id);

    /**
     * Sets the destination name.
     *
     * @param name
     *            the new destination name
     */
    void setDestinationName(String name);

    /**
     * Sets the expiry date.
     *
     * @param d
     *            the new expiry date
     */
    void setExpiryDate(Date d);

    /**
     * Sets the host name.
     *
     * @param name
     *            the new host name
     */
    void setHostName(String name);

    /**
     * Sets the backup host name.
     *
     * @param name
     *            the new backup host name
     */
    void setBackupHostName(String name);

    /**
     * Sets the proxy host name.
     *
     * @param name
     *            the new proxy host name
     */
    void setProxyHostName(String name);

    /**
     * Sets the priority.
     *
     * @param p
     *            the p
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void setPriority(int p, User u) throws TransferException;

    /**
     * Sets the expiry date.
     *
     * @param date
     *            the date
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void setExpiryDate(Date date, User u) throws TransferException;

    /**
     * Sets the start time.
     *
     * @param d
     *            the new start time
     */
    void setStartTime(Date d);

    /**
     * Sets the finish time.
     *
     * @param d
     *            the new finish time
     */
    void setFinishTime(Date d);

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    void setStatus(String status);

    /**
     * Sets the target.
     *
     * @param target
     *            the new target
     */
    void setTarget(String target);

    /**
     * Sets the transfer server name.
     *
     * @param name
     *            the new transfer server name
     */
    void setTransferServerName(String name);

    /**
     * Sets the sent.
     *
     * @param s
     *            the new sent
     */
    void setSent(long s);

    /**
     * Sets the duration.
     *
     * @param s
     *            the new duration
     */
    void setDuration(long s);

    /**
     * Sets the start count.
     *
     * @param i
     *            the new start count
     */
    void setStartCount(int i);

    /**
     * Interrupt retrieval.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void interruptRetrieval(User u) throws TransferException;

    /**
     * Requeue.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void requeue(User u) throws TransferException;

    /**
     * Put on hold.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void putOnHold(User u) throws TransferException;

    /**
     * Stop.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void stop(User u) throws TransferException;

    /**
     * Schedule now.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void scheduleNow(User u) throws TransferException;

    /**
     * Gets the can be requeued.
     *
     * @return the can be requeued
     */
    boolean getCanBeRequeued();

    /**
     * Gets the can be put on hold.
     *
     * @return the can be put on hold
     */
    boolean getCanBePutOnHold();

    /**
     * Gets the can be stopped.
     *
     * @return the can be stopped
     */
    boolean getCanBeStopped();

    /**
     * Gets the can be downloaded.
     *
     * @return the can be downloaded
     */
    boolean getCanBeDownloaded();

    /**
     * Gets the earliest time.
     *
     * @return the earliest time
     */
    Date getEarliestTime();

    /**
     * Gets the latest time.
     *
     * @return the latest time
     */
    Date getLatestTime();

    /**
     * Gets the predicted time.
     *
     * @return the predicted time
     */
    Date getPredictedTime();

    /**
     * Gets the target time.
     *
     * @return the target time
     */
    Date getTargetTime();
}
