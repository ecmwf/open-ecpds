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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Sets the user.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user.
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return DataTransfer.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return Long.toString(transfer.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the backup.
     */
    @Override
    public boolean getBackup() {
        return transfer.getBackupHostName() != null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy.
     */
    @Override
    public boolean getProxy() {
        return transfer.getProxyHostName() != null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the backup host name.
     */
    @Override
    public String getBackupHostName() {
        return transfer.getBackupHostName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy host name.
     */
    @Override
    public String getProxyHostName() {
        return transfer.getProxyHostName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the backup host.
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
     * {@inheritDoc}
     *
     * Gets the proxy host.
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
     * {@inheritDoc}
     *
     * Gets the replicated.
     */
    @Override
    public boolean getReplicated() {
        return transfer.getReplicated();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the unique key.
     */
    @Override
    public String getUniqueKey() {
        return transfer.getUniqueKey();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return transfer.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted comment.
     */
    @Override
    public String getFormattedComment() {
        return Util.getFormatted(user, getComment());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the identity.
     */
    @Override
    public String getIdentity() {
        return transfer.getIdentity();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return transfer;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file.
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public TransferMonitoringParameters getTransferMonitoringParameters() throws TransferException {
        throw new TransferException("Not Supported!");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file id.
     */
    @Override
    public long getDataFileId() {
        return transfer.getDataFileId();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the deleted.
     */
    @Override
    public boolean getDeleted() {
        return transfer.getDeleted();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination.
     */
    @Override
    public Destination getDestination() throws TransferException {
        return new DestinationBean(transfer.getDestination());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination name.
     */
    @Override
    public String getDestinationName() {
        return transfer.getDestinationName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the expiry date.
     */
    @Override
    public Date getExpiryDate() {
        return transfer.getExpiryTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the expired.
     */
    @Override
    public boolean getExpired() {
        final var expiryTime = transfer.getExpiryTime();
        return expiryTime != null && expiryTime.before(new Date());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
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
     * {@inheritDoc}
     *
     * Gets the host nick name.
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
     * {@inheritDoc}
     *
     * Gets the collection size.
     */
    @Override
    public int getCollectionSize() {
        return transfer.getCollectionSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host name.
     */
    @Override
    public String getHostName() {
        return transfer.getHostName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the priority.
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public void setPriority(final int i) throws TransferException {
        throw new TransferException("Not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the start time.
     */
    @Override
    public Date getStartTime() {
        return transfer.getStartTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retry time.
     */
    @Override
    public Date getRetryTime() {
        return transfer.getRetryTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the failed time.
     */
    @Override
    public Date getFailedTime() {
        return transfer.getFailedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the scheduled time minus minutes.
     */
    @Override
    public Date getScheduledTimeMinusMinutes(final int minutesBefore) {
        return new Date(getScheduledTime().getTime() - minutesBefore * 60 * 1000);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the finish time.
     */
    @Override
    public Date getFinishTime() {
        return transfer.getFirstFinishTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the real finish time.
     */
    @Override
    public Date getRealFinishTime() {
        return transfer.getFinishTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the scheduled time.
     */
    @Override
    public Date getScheduledTime() {
        return transfer.getScheduledTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the queue time.
     */
    @Override
    public Date getQueueTime() {
        return transfer.getQueueTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the predicted time.
     */
    @Override
    public Date getPredictedTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getPredictedTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the earliest time.
     */
    @Override
    public Date getEarliestTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getEarliestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the latest time.
     */
    @Override
    public Date getLatestTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getLatestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the target time.
     */
    @Override
    public Date getTargetTime() {
        final var transferMonitoringValue = transfer.getMonitoringValue();
        return transferMonitoringValue != null ? transferMonitoringValue.getTargetTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the start count.
     */
    @Override
    public int getStartCount() {
        return transfer.getStartCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the requeue count.
     */
    @Override
    public int getRequeueCount() {
        return transfer.getRequeueHistory();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the start count.
     */
    @Override
    public void setStartCount(final int i) {
        transfer.setStartCount(i);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() throws TransferException {
        return transfer.getStatusCode();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted status.
     */
    @Override
    public String getFormattedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(false, getStatus());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the member state formatted status.
     */
    @Override
    public String getMemberStateFormattedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(true, getStatus());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the detailed status.
     */
    @Override
    public String getDetailedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(false, transfer, getStatus());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the member state detailed status.
     */
    @Override
    public String getMemberStateDetailedStatus() throws TransferException {
        return StatusFactory.getDataTransferStatusName(true, transfer, getStatus());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status code.
     */
    @Override
    public String getStatusCode() {
        return transfer.getStatusCode();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the target.
     */
    @Override
    public String getTarget() {
        return transfer.getTarget();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server.
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
     * {@inheritDoc}
     *
     * Gets the transfer history.
     */
    @Override
    public Collection<TransferHistory> getTransferHistory() throws TransferException {
        return TransferHistoryHome.findByDataTransfer(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history after scheduled time.
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
     * {@inheritDoc}
     *
     * Gets the older transfers for same data file.
     */
    @Override
    public Collection<DataTransfer> getOlderTransfersForSameDataFile() throws TransferException {
        return DataTransferHome.findByDestinationAndIdentity(getDestinationName(), getIdentity());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the data file id.
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
     * {@inheritDoc}
     *
     * Sets the destination name.
     */
    @Override
    public void setDestinationName(final String param) {
        transfer.setDestinationName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the expiry date.
     */
    @Override
    public void setExpiryDate(final Date param) {
        transfer.setExpiryTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host name.
     */
    @Override
    public void setHostName(final String param) {
        transfer.setHostName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the backup host name.
     */
    @Override
    public void setBackupHostName(final String name) {
        transfer.setBackupHostName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the proxy host name.
     */
    @Override
    public void setProxyHostName(final String name) {
        transfer.setProxyHostName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the priority.
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
     * {@inheritDoc}
     *
     * Sets the expiry date.
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
     * {@inheritDoc}
     *
     * Sets the start time.
     */
    @Override
    public void setStartTime(final Date param) {
        transfer.setStartTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * {@inheritDoc}
     *
     * Sets the finish time.
     */
    @Override
    public void setFinishTime(final Date param) {
        transfer.setFinishTime(new java.sql.Timestamp(param.getTime()));
    }

    /**
     * {@inheritDoc}
     *
     * Sets the status.
     */
    @Override
    public void setStatus(final String param) {
    }

    /**
     * {@inheritDoc}
     *
     * Sets the target.
     */
    @Override
    public void setTarget(final String param) {
        transfer.setTarget(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer server name.
     */
    @Override
    public void setTransferServerName(final String param) {
        transfer.setTransferServerName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retrieval progress.
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
     * {@inheritDoc}
     *
     * Gets the transfer server name.
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
     * {@inheritDoc}
     *
     * Gets the progress.
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
     * {@inheritDoc}
     *
     * Gets the sent.
     */
    @Override
    public long getSent() throws TransferException {
        return transfer.getSent();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the duration.
     */
    @Override
    public long getDuration() {
        return transfer.getDuration();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the asap.
     */
    @Override
    public boolean getAsap() {
        return transfer.getAsap();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer rate.
     */
    @Override
    public long getTransferRate() throws TransferException {
        final var sent = getSent();
        final var duration = getDuration();
        return sent > 0 && duration > 0 ? (long) (sent / (duration / 1000.0)) : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted transfer rate.
     */
    @Override
    public String getFormattedTransferRate() throws TransferException {
        return Format.formatRate(getSent(), getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted transfer rate in M bits per seconds.
     */
    @Override
    public double getFormattedTransferRateInMBitsPerSeconds() throws TransferException {
        return Format.getMBitsPerSeconds(getSent(), getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted duration.
     */
    @Override
    public String getFormattedDuration() throws TransferException {
        return Format.formatDuration(getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted sent.
     */
    @Override
    public String getFormattedSent() throws TransferException {
        return Format.formatSize(getSent());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the sent.
     */
    @Override
    public void setSent(final long l) {
        transfer.setSent(l);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the duration.
     */
    @Override
    public void setDuration(final long l) {
        transfer.setDuration(l);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the size.
     */
    @Override
    public long getSize() {
        return transfer.getDataFile().getSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted size.
     */
    @Override
    public String getFormattedSize() {
        return Format.formatSize(getSize());
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is retry.
     */
    @Override
    public boolean isRetry() {
        return isRetry;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the retry.
     */
    @Override
    public void setRetry(final boolean retry) {
        isRetry = retry;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival target time.
     */
    @Override
    public Date getArrivalTargetTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalTargetTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival predicted time.
     */
    @Override
    public Date getArrivalPredictedTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalPredictedTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival status.
     */
    @Override
    public int getArrivalStatus() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalStatus() : -1;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival earliest time.
     */
    @Override
    public Date getArrivalEarliestTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalEarliestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival latest time.
     */
    @Override
    public Date getArrivalLatestTime() throws TransferException {
        final var monitoringParameters = getArrivalMonitoringParameters();
        return monitoringParameters != null ? monitoringParameters.getArrivalLatestTime() : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer target time.
     */
    @Override
    public Date getTransferTargetTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferTargetTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer predicted time.
     */
    @Override
    public Date getTransferPredictedTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferPredictedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer status.
     */
    @Override
    public int getTransferStatus() throws TransferException {
        return getTransferMonitoringParameters().getTransferStatus();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer status hex color.
     */
    @Override
    public String getTransferStatusHexColor() throws TransferException {
        return getTransferMonitoringParameters().getTransferStatusHexColor();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer earliest time.
     */
    @Override
    public Date getTransferEarliestTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferEarliestTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer latest time.
     */
    @Override
    public Date getTransferLatestTime() throws TransferException {
        return getTransferMonitoringParameters().getTransferLatestTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the copy.
     */
    @Override
    public DataTransfer getCopy() {
        return new DataTransferBaseBean((ecmwf.common.database.DataTransfer) transfer.clone());
    }

    /**
     * {@inheritDoc}
     *
     * Put on hold.
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
     * {@inheritDoc}
     *
     * Stop.
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
     * {@inheritDoc}
     *
     * Schedule now.
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
     * {@inheritDoc}
     *
     * Gets the can be put on hold.
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
     * {@inheritDoc}
     *
     * Gets the can be stopped.
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
     * {@inheritDoc}
     *
     * Gets the can be downloaded.
     */
    @Override
    public boolean getCanBeDownloaded() {
        return !transfer.getDeleted() && !Status.INIT.equals(transfer.getStatusCode());
    }

    /**
     * {@inheritDoc}
     *
     * Interrupt retrieval.
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
     * {@inheritDoc}
     *
     * Requeue.
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
     * {@inheritDoc}
     *
     * Gets the can be requeued.
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
     * {@inheritDoc}
     *
     * Equals.
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + transfer + " }";
    }
}
