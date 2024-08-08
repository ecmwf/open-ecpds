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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferHistoryHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

/**
 * The Class DataTransferBaseBean.
 */
public class DataTransferBaseBean extends ModelBeanBase implements DataTransfer, OjbImplementedBean {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DataTransferBaseBean.class);

    /** The transfer. */
    private final ecmwf.common.database.DataTransfer transfer;

    /** The is retry. */
    private boolean isRetry = false;

    /** The user. */
    private User user;

    /**
     * Instantiates a new data transfer base bean.
     *
     * @param transfer
     *            the transfer
     */
    protected DataTransferBaseBean(final ecmwf.common.database.DataTransfer transfer) {
        this.transfer = transfer;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return DataTransfer.class.getName();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return Long.toString(transfer.getId());
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    @Override
    public boolean getBackup() {
        return transfer.getBackupHostName() != null;
    }

    /**
     * Gets the proxy.
     *
     * @return the proxy
     */
    @Override
    public boolean getProxy() {
        return transfer.getProxyHostName() != null;
    }

    /**
     * Gets the backup host name.
     *
     * @return the backup host name
     */
    @Override
    public String getBackupHostName() {
        return transfer.getBackupHostName();
    }

    /**
     * Gets the proxy host name.
     *
     * @return the proxy host name
     */
    @Override
    public String getProxyHostName() {
        return transfer.getProxyHostName();
    }

    /**
     * Gets the backup host.
     *
     * @return the backup host
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Host getBackupHost() throws TransferException {
        final var host = transfer.getBackupHost();
        if (host == null) {
            throw new TransferException("No host assigned yet for transfer " + getId());
        }
        return new HostBean(user, host);
    }

    /**
     * Gets the proxy host.
     *
     * @return the proxy host
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Host getProxyHost() throws TransferException {
        final var host = transfer.getProxyHost();
        if (host == null) {
            throw new TransferException("No host assigned yet for transfer " + getId());
        }
        return new HostBean(user, host);
    }

    /**
     * Gets the replicated.
     *
     * @return the replicated
     */
    @Override
    public boolean getReplicated() {
        return transfer.getReplicated();
    }

    /**
     * Gets the unique key.
     *
     * @return the unique key
     */
    @Override
    public String getUniqueKey() {
        return transfer.getUniqueKey();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return transfer.getComment();
    }

    /**
     * Gets the formatted comment.
     *
     * @return the formatted comment
     */
    @Override
    public String getFormattedComment() {
        return Util.getFormatted(user, getComment());
    }

    /**
     * Gets the identity.
     *
     * @return the identity
     */
    @Override
    public String getIdentity() {
        return transfer.getIdentity();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return transfer;
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public DataFile getDataFile() throws TransferException {
        return new DataFileBean(transfer.getDataFile());
    }

    /**
     * Gets the arrival monitoring parameters.
     *
     * @return the arrival monitoring parameters
     *
     * @throws TransferException
     *             the transfer exception
     */
    public ArrivalMonitoringParameters getArrivalMonitoringParameters() throws TransferException {
        throw new TransferException("Not Supported!");
    }

    /**
     * Gets the transfer monitoring parameters.
     *
     * @return the transfer monitoring parameters
     *
     * @throws TransferException
     *             the transfer exception
     */
    public TransferMonitoringParameters getTransferMonitoringParameters() throws TransferException {
        throw new TransferException("Not Supported!");
    }

    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    @Override
    public long getDataFileId() {
        return transfer.getDataFileId();
    }

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    @Override
    public boolean getDeleted() {
        return transfer.getDeleted();
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Destination getDestination() throws TransferException {
        return new DestinationBean(transfer.getDestination());
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    @Override
    public String getDestinationName() {
        return transfer.getDestinationName();
    }

    /**
     * Gets the expiry date.
     *
     * @return the expiry date
     */
    @Override
    public Date getExpiryDate() {
        return transfer.getExpiryTime();
    }

    /**
     * Gets the expired.
     *
     * @return the expired
     */
    @Override
    public boolean getExpired() {
        final var expiryTime = transfer.getExpiryTime();
        return expiryTime != null && expiryTime.before(new Date());
    }

    /**
     * Gets the host.
     *
     * @return the host
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Host getHost() throws TransferException {
        final var host = transfer.getHost();
        if (host == null) {
            throw new TransferException("No host assigned yet for transfer " + getId());
        }
        return new HostBean(user, host);
    }

    /**
     * Gets the host nick name.
     *
     * @return the host nick name
     */
    @Override
    public String getHostNickName() {
        try {
            return getHost().getNickName();
        } catch (final TransferException e) {
            return "";
        }
    }

    /**
     * Gets the collection size.
     *
     * @return the collection size
     */
    @Override
    public int getCollectionSize() {
        return transfer.getCollectionSize();
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    @Override
    public String getHostName() {
        return transfer.getHostName();
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    @Override
    public int getPriority() {
        try {
            return transfer.getPriority();
        } catch (final Exception e) {
            log.error("Problem getting priority for DataTransferBean: " + getId(), e);
            return -1;
        }
    }

    /**
     * Sets the priority.
     *
     * @param i
     *            the new priority
     *
     * @throws TransferException
     *             the transfer exception
     */
    public void setPriority(final int i) throws TransferException {
        throw new TransferException("Not supported");
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    @Override
    public Date getStartTime() {
        return transfer.getStartTime();
    }

    /**
     * Gets the retry time.
     *
     * @return the retry time
     */
    @Override
    public Date getRetryTime() {
        return transfer.getRetryTime();
    }

    /**
     * Gets the failed time.
     *
     * @return the failed time
     */
    @Override
    public Date getFailedTime() {
        return transfer.getFailedTime();
    }

    /**
     * Gets the scheduled time minus minutes.
     *
     * @param minutesBefore
     *            the minutes before
     *
     * @return the scheduled time minus minutes
     */
    @Override
    public Date getScheduledTimeMinusMinutes(final int minutesBefore) {
        return new Date(getScheduledTime().getTime() - minutesBefore * 60 * 1000);
    }

    /**
     * Gets the finish time.
     *
     * @return the finish time
     */
    @Override
    public Date getFinishTime() {
        return transfer.getFirstFinishTime();
    }

    /**
     * Gets the real finish time.
     *
     * @return the real finish time
     */
    @Override
    public Date getRealFinishTime() {
        return transfer.getFinishTime();
    }

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    @Override
    public Date getScheduledTime() {
        return transfer.getScheduledTime();
    }

    /**
     * Gets the queue time.
     *
     * @return the queue time
     */
    @Override
    public Date getQueueTime() {
        return transfer.getQueueTime();
    }

    /**
     * Gets the predicted time.
     *
     * @return the predicted time
     */
    @Override
    public Date getPredictedTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getPredictedTime() : null;
    }

    /**
     * Gets the earliest time.
     *
     * @return the earliest time
     */
    @Override
    public Date getEarliestTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getEarliestTime() : null;
    }

    /**
     * Gets the latest time.
     *
     * @return the latest time
     */
    @Override
    public Date getLatestTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getLatestTime() : null;
    }

    /**
     * Gets the target time.
     *
     * @return the target time
     */
    @Override
    public Date getTargetTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getTargetTime() : null;
    }

    /**
     * Gets the start count.
     *
     * @return the start count
     */
    @Override
    public int getStartCount() {
        return transfer.getStartCount();
    }

    /**
     * Gets the requeue count.
     *
     * @return the requeue count
     */
    @Override
    public int getRequeueCount() {
        return transfer.getRequeueHistory();
    }

    /**
     * Sets the start count.
     *
     * @param i
     *            the new start count
     */
    @Override
    public void setStartCount(final int i) {
        transfer.setStartCount(i);
    }

    /**
     * Gets the status.
     *
     * @return the status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getStatus() throws TransferException {
        return transfer.getStatusCode();
    }

    /**
     * Gets the formatted status.
     *
     * @return the formatted status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getFormattedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(false, getStatus());
    }

    /**
     * Gets the member state formatted status.
     *
     * @return the member state formatted status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getMemberStateFormattedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(true, getStatus());
    }

    /**
     * Gets the detailed status.
     *
     * @return the detailed status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getDetailedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(false, transfer, getStatus());
    }

    /**
     * Gets the member state detailed status.
     *
     * @return the member state detailed status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getMemberStateDetailedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(true, transfer, getStatus());
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    @Override
    public String getStatusCode() {
        return transfer.getStatusCode();
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    @Override
    public String getTarget() {
        return transfer.getTarget();
    }

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public TransferServer getTransferServer() throws TransferException {
        try {
            return TransferServerHome.findByDataTransfer(this);
        } catch (final DataFileException e) {
            throw new TransferException("Problem getting a TransferServer for the DataTransfer", e);
        }
    }

    /**
     * Gets the transfer history.
     *
     * @return the transfer history
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<TransferHistory> getTransferHistory() throws TransferException {
        return TransferHistoryHome.findByDataTransfer(this);
    }

    /**
     * Gets the transfer history after scheduled time.
     *
     * @return the transfer history after scheduled time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<TransferHistory> getTransferHistoryAfterScheduledTime() throws TransferException {
        final List<TransferHistory> historyList = new ArrayList<>();
        final var scheduledTime = getScheduledTime();
        for (final TransferHistory history : TransferHistoryHome.findByDataTransfer(this)) {
            if (history.getDate().after(scheduledTime)) {
                historyList.add(history);
            }
        }
        return historyList;
    }

    /**
     * Gets the older transfers for same data file.
     *
     * @return the older transfers for same data file
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransfer> getOlderTransfersForSameDataFile() throws TransferException {
        return DataTransferHome.findByDestinationAndIdentity(getDestinationName(), getIdentity());
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    @Override
    public void setDataFileId(final long param) {
        transfer.setDataFileId(param);
    }

    /**
     * Sets the data file id.
     *
     * @param param
     *            the new data file id
     */
    public void setDataFileId(final String param) {
        transfer.setDataFileId(param);
    }

    /**
     * Sets the destination name.
     *
     * @param param
     *            the new destination name
     */
    @Override
    public void setDestinationName(final String param) {
        transfer.setDestinationName(param);
    }

    /**
     * Sets the expiry date.
     *
     * @param param
     *            the new expiry date
     */
    @Override
    public void setExpiryDate(final Date param) {
        transfer.setExpiryTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * Sets the host name.
     *
     * @param param
     *            the new host name
     */
    @Override
    public void setHostName(final String param) {
        transfer.setHostName(param);
    }

    /**
     * Sets the backup host name.
     *
     * @param name
     *            the new backup host name
     */
    @Override
    public void setBackupHostName(final String name) {
        transfer.setBackupHostName(name);
    }

    /**
     * Sets the proxy host name.
     *
     * @param name
     *            the new proxy host name
     */
    @Override
    public void setProxyHostName(final String name) {
        transfer.setProxyHostName(name);
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the param
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void setPriority(final int param, final User u) throws TransferException {
        transfer.setPriority(param);
        try {
            MasterManager.getMI().updateTransferPriority(Util.getECpdsSessionFromObject(u), transfer.getId(), param);
        } catch (final Exception e) {
            throw new TransferException("Impossible to change priority of transfer '" + transfer.getId() + "' to '"
                    + param + "'. Out of limits (0-99).", e);
        }
    }

    /**
     * Sets the expiry date.
     *
     * @param date
     *            the date
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void setExpiryDate(final Date date, final User u) throws TransferException {
        final var timestamp = new Timestamp(date.getTime());
        transfer.setExpiryTime(timestamp);
        try {
            MasterManager.getMI().updateExpiryTime(Util.getECpdsSessionFromObject(u), transfer.getId(), timestamp);
        } catch (final Exception e) {
            throw new TransferException("Impossible to change expiry date of transfer '" + transfer.getId() + "' to '"
                    + Format.formatTime(timestamp.getTime()) + "'.", e);
        }
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    @Override
    public void setStartTime(final Date param) {
        transfer.setStartTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * Sets the finish time.
     *
     * @param param
     *            the new finish time
     */
    @Override
    public void setFinishTime(final Date param) {
        transfer.setFinishTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * Sets the status.
     *
     * @param param
     *            the new status
     */
    @Override
    public void setStatus(final String param) {
    }

    /**
     * Sets the target.
     *
     * @param param
     *            the new target
     */
    @Override
    public void setTarget(final String param) {
        transfer.setTarget(param);
    }

    /**
     * Sets the transfer server name.
     *
     * @param param
     *            the new transfer server name
     */
    @Override
    public void setTransferServerName(final String param) {
        transfer.setTransferServerName(param);
    }

    /**
     * Gets the retrieval progress.
     *
     * @return the retrieval progress
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getRetrievalProgress() throws TransferException {
        final var statusCode = transfer.getStatusCode();
        final var fileSize = getSize();
        try {
            if (fileSize != 0 && (StatusFactory.FETC.equals(statusCode) || StatusFactory.INIT.equals(statusCode))) {
                final var retrieved = MasterManager.getMI().getRetrieved(transfer.getDataFileId());
                return fileSize == -1 ? Format.formatSize(retrieved) : (int) (retrieved * 100 / fileSize) + "%";
            }
            return fileSize == 0 || transfer.getDataFile().getDownloaded() ? "100%" : "0%";
        } catch (final Exception e) {
            throw new TransferException("Error getting retrieval progress of '" + transfer.getId() + "'", e);
        }
    }

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    @Override
    public String getTransferServerName() {
        final var statusCode = transfer.getStatusCode();
        final var original = transfer.getTransferServerName();
        try {
            return StatusFactory.FETC.equals(statusCode) || StatusFactory.INIT.equals(statusCode)
                    ? MasterManager.getMI().getTransferServerName(transfer.getDataFileId()) : original;
        } catch (final Exception e) {
            return original;
        }
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public int getProgress() throws TransferException {
        if (StatusFactory.DONE.equals(transfer.getStatusCode())) {
            return 100;
        }
        final var size = getSize();
        return size == 0 ? 0 : (int) (getSent() * transfer.getRatio() * 100 / size);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public long getSent() throws TransferException {
        return transfer.getSent();
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        return transfer.getDuration();
    }

    /**
     * Gets the asap.
     *
     * @return the asap
     */
    @Override
    public boolean getAsap() {
        return transfer.getAsap();
    }

    /**
     * Gets the transfer rate.
     *
     * @return the transfer rate
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public long getTransferRate() throws TransferException {
        final var sent = getSent();
        final var duration = getDuration();
        return sent > 0 && duration > 0 ? (long) (sent / (duration / 1000.0)) : 0;
    }

    /**
     * Gets the formatted transfer rate.
     *
     * @return the formatted transfer rate
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getFormattedTransferRate() throws TransferException {
        return Format.formatRate(getSent(), getDuration());
    }

    /**
     * Gets the formatted transfer rate in M bits per seconds.
     *
     * @return the formatted transfer rate in M bits per seconds
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public double getFormattedTransferRateInMBitsPerSeconds() throws TransferException {
        return Format.getMBitsPerSeconds(getSent(), getDuration());
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getFormattedDuration() throws TransferException {
        return Format.formatDuration(getDuration());
    }

    /**
     * Gets the formatted sent.
     *
     * @return the formatted sent
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getFormattedSent() throws TransferException {
        return Format.formatSize(getSent());
    }

    /**
     * Sets the sent.
     *
     * @param l
     *            the new sent
     */
    @Override
    public void setSent(final long l) {
        transfer.setSent(l);
    }

    /**
     * Sets the duration.
     *
     * @param l
     *            the new duration
     */
    @Override
    public void setDuration(final long l) {
        transfer.setDuration(l);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    @Override
    public long getSize() {
        return transfer.getDataFile().getSize();
    }

    /**
     * Gets the formatted size.
     *
     * @return the formatted size
     */
    @Override
    public String getFormattedSize() {
        return Format.formatSize(getSize());
    }

    /**
     * Checks if is retry.
     *
     * @return true, if is retry
     */
    @Override
    public boolean isRetry() {
        return isRetry;
    }

    /**
     * Sets the retry.
     *
     * @param retry
     *            the new retry
     */
    @Override
    public void setRetry(final boolean retry) {
        isRetry = retry;
    }

    /**
     * Gets the arrival target time.
     *
     * @return the arrival target time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getArrivalTargetTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalTargetTime() : null;
    }

    /**
     * Gets the arrival predicted time.
     *
     * @return the arrival predicted time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getArrivalPredictedTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalPredictedTime() : null;
    }

    /**
     * Gets the arrival status.
     *
     * @return the arrival status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public int getArrivalStatus() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalStatus() : -1;
    }

    /**
     * Gets the arrival earliest time.
     *
     * @return the arrival earliest time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getArrivalEarliestTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalEarliestTime() : null;
    }

    /**
     * Gets the arrival latest time.
     *
     * @return the arrival latest time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getArrivalLatestTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalLatestTime() : null;
    }

    /**
     * Gets the transfer target time.
     *
     * @return the transfer target time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getTransferTargetTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferTargetTime();
    }

    /**
     * Gets the transfer predicted time.
     *
     * @return the transfer predicted time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getTransferPredictedTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferPredictedTime();
    }

    /**
     * Gets the transfer status.
     *
     * @return the transfer status
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public int getTransferStatus() throws TransferException {
        return getTransferMonitoringParameters().getTransferStatus();
    }

    /**
     * Gets the transfer status hex color.
     *
     * @return the transfer status hex color
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getTransferStatusHexColor() throws TransferException {
        return getTransferMonitoringParameters().getTransferStatusHexColor();
    }

    /**
     * Gets the transfer earliest time.
     *
     * @return the transfer earliest time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getTransferEarliestTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferEarliestTime();
    }

    /**
     * Gets the transfer latest time.
     *
     * @return the transfer latest time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Date getTransferLatestTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferLatestTime();
    }

    /**
     * Gets the copy.
     *
     * @return the copy
     */
    @Override
    public DataTransfer getCopy() {
        return new DataTransferBaseBean((ecmwf.common.database.DataTransfer) transfer.clone());
    }

    /**
     * Put on hold.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void putOnHold(final User u) throws TransferException {
        try {
            MasterManager.getMI().updateTransferStatus(Util.getECpdsSessionFromObject(u), transfer.getId(),
                    Status.HOLD);
        } catch (final Exception e) {
            throw new TransferException("Error putting ON HOLD transfer '" + transfer.getId() + "'", e);
        }
    }

    /**
     * Stop.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void stop(final User u) throws TransferException {
        // If the current data transfer is being retrieved then we should
        // interrupt it instead of stopping it!
        if (Status.FETC.equals(getStatus())) {
            interruptRetrieval(u);
            return;
        }
        // It is not in retrieval mode so let's process the stop request.
        final boolean ok;
        try {
            ok = MasterManager.getMI().updateTransferStatus(Util.getECpdsSessionFromObject(u), transfer.getId(),
                    Status.STOP);
        } catch (final Exception e) {
            throw new TransferException("Error setting to STOP transfer '" + transfer.getId() + "'", e);
        }
        if (!ok) {
            throw new TransferException(
                    "STOP operation unavailable for transfer '" + getId() + "'. Current Status " + getStatusCode());
        }
    }

    /**
     * Schedule now.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void scheduleNow(final User u) throws TransferException {
        try {
            MasterManager.getMI().resetTransferScheduleDate(Util.getECpdsSessionFromObject(u), transfer.getId());
        } catch (final Exception e) {
            throw new TransferException("Error RESETTING transfer '" + transfer.getId() + "' Schedule Time", e);
        }
    }

    /**
     * Gets the can be put on hold.
     *
     * @return the can be put on hold
     */
    @Override
    public boolean getCanBePutOnHold() {
        try {
            return MasterManager.getMI().transferStatusUpdateAllowed(transfer.getId(), Status.HOLD);
        } catch (final Exception e) {
            log.error("Error checking ON HOLD for transfer '" + transfer.getId() + "'", e);
            return false;
        }
    }

    /**
     * Gets the can be stopped.
     *
     * @return the can be stopped
     */
    @Override
    public boolean getCanBeStopped() {
        try {
            return MasterManager.getMI().transferStatusUpdateAllowed(transfer.getId(), Status.STOP);
        } catch (final Exception e) {
            log.error("Error checking STOP for transfer '" + transfer.getId() + "'", e);
            return false;
        }
    }

    /**
     * Gets the can be downloaded.
     *
     * @return the can be downloaded
     */
    @Override
    public boolean getCanBeDownloaded() {
        return !transfer.getDeleted() && !Status.INIT.equals(transfer.getStatusCode());
    }

    /**
     * Interrupt retrieval.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void interruptRetrieval(final User u) throws TransferException {
        final boolean ok;
        if (Status.FETC.equals(transfer.getStatusCode())) {
            try {
                ok = MasterManager.getMI().interruptDataTransferRetrieval(Util.getECpdsSessionFromObject(u),
                        transfer.getId());
            } catch (final Exception e) {
                throw new TransferException("Error interrupting retrieval for Transfer '" + transfer.getId() + "'", e);
            }
        } else {
            ok = false;
        }
        if (!ok) {
            throw new TransferException("INTERRUPT operation unavailable for transfer '" + getId()
                    + "'. Current Status " + getStatusCode());
        }
    }

    /**
     * Requeue.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void requeue(final User u) throws TransferException {
        if (getDeleted()) {
            throw new TransferException(
                    "REQUEUE operation unavailable for transfer '" + getId() + "'. Source file not available.");
        }
        final boolean ok;
        try {
            ok = MasterManager.getMI().updateTransferStatus(Util.getECpdsSessionFromObject(u), transfer.getId(),
                    Status.WAIT);
        } catch (final Exception e) {
            throw new TransferException("Error REQUEUING transfer '" + transfer.getId() + "'", e);
        }
        if (!ok) {
            throw new TransferException(
                    "REQUEUE operation unavailable for transfer '" + getId() + "'. Current Status " + getStatusCode());
        }
    }

    /**
     * Gets the can be requeued.
     *
     * @return the can be requeued
     */
    @Override
    public boolean getCanBeRequeued() {
        try {
            return MasterManager.getMI().transferStatusUpdateAllowed(transfer.getId(), Status.WAIT);
        } catch (final Exception e) {
            log.error("Error checking REQUEUE for transfer '" + transfer.getId() + "'", e);
            return false;
        }
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final DataTransferBaseBean dataTransferBaseBean && equals(dataTransferBaseBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final DataTransferBaseBean d) {
        return getId().equals(d.getId());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + transfer + " }";
    }
}
