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

package ecmwf.ecpds.master;

import static ecmwf.ecpds.master.OperationalEvents.field;
import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_MASTER_TO_NOTIFY_ON_DONE;
import static ecmwf.common.text.Util.nullToNone;
import java.sql.Timestamp;
import java.time.Instant;
import javax.management.timer.Timer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.TransferHistory;
import ecmwf.common.database.UploadHistory;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.rmi.SocketStatisticsParser;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;

/**
 * The Class HistoryRepository.
 */
final class HistoryRepository extends StorageRepository<TransferHistory> {
    private final MasterServer _master;

    /**
     * Instantiates a new history repository.
     *
     * @param name
     *            the name
     */
    HistoryRepository(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name, Cnf.at("StorageRepository", "historySize", 5),
                Cnf.at("StorageRepository", "historyDelay", 30 * Timer.ONE_SECOND));
        setMaxAuthorisedSize(Cnf.at("StorageRepository", "historyMaxAuthorisedSize", 0));
    }

    /**
     * Gets the status.
     *
     * @param history
     *            the history
     *
     * @return the status
     */
    @Override
    public String getStatus(final TransferHistory history) {
        final var comment = history.getComment();
        return history.getStatusCode() + (comment != null ? "(" + comment + ") " : " ");
    }

    /**
     * Update.
     *
     * @param history
     *            the history
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void update(final TransferHistory history) throws Exception {
        final var transfer = history.getDataTransfer();
        final var host = history.getHost();
        final var code = transfer.getStatusCode();
        final var sent = transfer.getSent();
        final var completed = StatusFactory.DONE.equals(code);
        if ((completed || StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code)
                || StatusFactory.RETR.equals(code) || StatusFactory.INTR.equals(code)) && transfer.getSent() > 0
                && host != null) {
            _master._updateHostStats(host, 1, sent, transfer.getDuration(), completed,
                    new Timestamp(System.currentTimeMillis()));
        }
        final var file = transfer.getDataFile();
        final var base = _master.getDataBase();
        if (completed && host != null) {
            // And now let's insert a new UploadHistory!
            try {
                final var upload = new UploadHistory();
                upload.setDataTransfer(transfer);
                upload.setDataTransferId(transfer.getId());
                upload.setDestination(transfer.getDestinationName());
                upload.setFileName(transfer.getTarget());
                upload.setFileSize(file.getSize());
                upload.setMetaStream(file.getMetaStream());
                upload.setMetaType(file.getMetaType());
                upload.setMetaTime(file.getMetaTime());
                upload.setPriority(transfer.getPriority());
                upload.setQueueTime(transfer.getQueueTime());
                upload.setRequeueCount(transfer.getRequeueHistory());
                upload.setScheduledTime(transfer.getScheduledTime());
                upload.setStartTime(transfer.getStartTime());
                upload.setPutTime(transfer.getPutTime());
                upload.setDuration(transfer.getDuration());
                upload.setSent(sent);
                upload.setStatusCode(code);
                upload.setRetrievalTime(file.getGetTime());
                upload.setTimeBase(file.getTimeBase());
                upload.setTimeStep(file.getTimeStep());
                upload.setFinishTime(transfer.getFinishTime());
                upload.setTransferModule(host.getTransferMethod().getECtransModuleName());
                upload.setTransferServer(transfer.getTransferServerName());
                upload.setNetworkCode(host.getNetworkCode());
                upload.setHostAddress(host.getHost());
                base.insert(upload, true);
                final var socketStatistics = nullToNone(transfer.getStatistics());
                if (transfer.getRecordUPH() && MasterServer._operationalEvents.isEnabled()) {
                    // For the accounting!
                    final var destination = transfer.getDestination();
                    final var proxyHost = transfer.getProxyHost();
                    final var retryTime = transfer.getRetryTime();
                    final var startTime = transfer.getStartTime();
                    MasterServer._operationalEvents.emit(transfer, "UPH", field("Monitored", destination.getMonitor()),
                            field("TimeStamp", Timestamp.from(Instant.now())),
                            field("DataTransferId", upload.getDataTransferId()),
                            field("DestinationName", destination.getName()),
                            field("DestinationType", DestinationOption.getLabel(destination.getType())),
                            field("FileName", upload.getFileName()), field("FileSize", upload.getFileSize()),
                            field("Identity", transfer.getIdentity()),
                            field("MetaStream", nullToNone(upload.getMetaStream())),
                            field("MetaType", nullToNone(upload.getMetaType())),
                            field("MetaTime", upload.getMetaTime()), field("Priority", upload.getPriority()),
                            field("QueueTime", upload.getQueueTime()), field("RequeueCount", upload.getRequeueCount()),
                            field("ScheduledTime", upload.getScheduledTime()),
                            field("StartTime", (retryTime != null ? retryTime : startTime)),
                            field("PutTime", upload.getPutTime()), field("Duration", upload.getDuration()),
                            field("DurationOnClose", transfer.getDurationOnClose()),
                            field("BytesSent", upload.getSent()), field("StatusCode", upload.getStatusCode()),
                            field("RetrievalTime", nullToNone(upload.getRetrievalTime())),
                            field("TimeBase", upload.getTimeBase()), field("TimeStep", upload.getTimeStep()),
                            field("FinishTime", upload.getFinishTime()),
                            field("TransferProtocol", upload.getTransferModule()),
                            field("TransferServer", upload.getTransferServer()),
                            field("NetworkCode", HostOption.getLabel(upload.getNetworkCode())),
                            field("HostAddress", upload.getHostAddress()), field("PreSchedule", transfer.getAsap()),
                            field("ProxyHost", nullToNone(proxyHost != null ? proxyHost.getHost() : null)),
                            field("Proxied", (transfer.getProxyName() != null)),
                            field("SocketStatistics", socketStatistics),
                            field("Compressed", nullToNone(transfer.getCompressed())),
                            field("CompressedOnTheFly", transfer.getCompressedOnTheFly()));
                }
                MasterServer._log.debug("UploadHistory created for DataTransfer {} (statistics: {})", transfer.getId(),
                        socketStatistics);
                // Persist per-connection TCP statistics
                if (!"none".equals(socketStatistics) && transfer.getWeatherExecutionId() == null) {
                    try (final var mutex = Synchronized.getInstance(ecmwf.common.database.WeatherSocketStore.class)
                            .getMutex(transfer.getId())) {
                        synchronized (mutex.lock()) {
                            final var ecpdsBase = _master.getDataBase(ECpdsBase.class);
                            // Use a sending index that always increments across retries and requeues.
                            // transfer.getRequeueHistory() only increments on a requeue (maxStart hit),
                            // not on plain retries, so multiple sendings within the same requeue cycle
                            // would otherwise all get index 0 and end up in one group in the UI.
                            final var existing = ecpdsBase.getTransferStatisticsByDataTransferId(transfer.getId());
                            final var maxExisting = existing.stream().mapToInt(s -> s.getRequeueHistory()).max()
                                    .orElse(-1);
                            final var legacyIndex = existing.isEmpty() ? transfer.getRequeueHistory()
                                    : Math.max(transfer.getRequeueHistory(), maxExisting + 1);
                            final var sendingIndex = ecmwf.common.database.WeatherSocketStore
                                    .legacySendingIndex(transfer.getId(), legacyIndex);
                            MasterServer._log.info(
                                    "TransferStatistics for DataTransfer {}: existing={}, maxExisting={}, "
                                            + "transfer.requeueHistory={}, sendingIndex={}",
                                    transfer.getId(), existing.size(), maxExisting, transfer.getRequeueHistory(),
                                    sendingIndex);
                            for (final var stats : SocketStatisticsParser.parse(transfer.getId(),
                                    transfer.getStatistics())) {
                                stats.setRequeueHistory(sendingIndex);
                                if (!ecpdsBase.tryInsertTransferStatistics(stats)) {
                                    MasterServer._log.warn(
                                            "TransferStatistics NOT inserted for DataTransfer {} (sendingIndex={})",
                                            transfer.getId(), sendingIndex);
                                }
                            }
                        }
                    }
                }
            } catch (final Throwable t) {
                MasterServer._log.error("Creating UploadHistory", t);
            }
        }
        // Let's insert a new transfer history!
        try {
            base.insert(history, true);
        } catch (final Exception e) {
            MasterServer._log.error("History NOT recorded: {}", history, e);
        }
        // Let's see if we should notify a remote Master?
        if (completed) {
            final var setup = DESTINATION_SCHEDULER.getECtransSetup(transfer.getDestination().getData());
            final var masterToNotifyOnDone = setup.getString(DESTINATION_SCHEDULER_MASTER_TO_NOTIFY_ON_DONE);
            if (masterToNotifyOnDone != null) {
                MasterServer._log.error("Notifying remote Master: " + masterToNotifyOnDone);
                try {
                    _master.updateRemoteTransferStatus(masterToNotifyOnDone, file.getStandby(),
                            transfer.getDestinationName(), transfer.getTarget(), transfer.getUniqueKey(),
                            StatusFactory.STOP);
                } catch (final Exception e) {
                    MasterServer._log.error("Remote Master NOT notified: " + masterToNotifyOnDone, e);
                }
            }
        }
    }
}
