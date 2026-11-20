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

package ecmwf.ecpds.mover;

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_STREAM_TIMEOUT;
import static ecmwf.common.text.Util.isNotEmpty;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.MSUser;
import ecmwf.common.ectrans.ECtransCallback;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.ProgressHandler;
import ecmwf.common.technical.ProgressInterface;
import ecmwf.common.telemetry.MoverTracing;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.LiveTransferSample;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.mover.MoverServer.FileDescriptor;

/**
 * The Class MoverCallback.
 */
final class MoverCallback implements ProgressHandler, ECtransCallback {
    private final MoverServer _mover;

    private final MoverTracing.Attempt trace;
    private final String _liveAttemptId = java.util.UUID.randomUUID().toString();
    private final ecmwf.ecpds.master.weather.WeatherCapture.Execution weather;
    /** The _name. */
    private final String _name;

    /** The _file name. */
    private final String _fileName;

    /** The _transfer. */
    private final DataTransfer _transfer;

    /** The _data file. */
    private final DataFile _dataFile;

    /** The _descriptor. */
    private FileDescriptor _descriptor = null;

    /** The _monitor. */
    private ProgressInterface _monitor = null;

    /** The _update. */
    private long _update = -1;

    /** The last time a live transfer sample was pushed (Live ECPDS Earth - Phase 0). */
    private long _lastLiveSample = -1;

    /**
     * The TransferModule currently handling this transfer, captured via {@link #connected(TransferModule)} as soon as
     * it becomes available, so that {@link #_offerLiveSample(String)} can query its real, currently connected remote
     * address (Live ECPDS Earth - Phase 0).
     */
    private volatile TransferModule _module = null;
    private java.util.function.Consumer<Boolean> confirmedPasswordDelivery;

    void setConfirmedPasswordDelivery(final java.util.function.Consumer<Boolean> confirmed) {
        confirmedPasswordDelivery = confirmed;
        _setup.set(ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CLOSE_ASYNCHRONOUS, false);
    }

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _setup. */
    private final ECtransSetup _setup;

    /** The _file size. */
    private long _fileSize = -1;

    /** The _timeout. */
    private final long _timeout;

    /** The _filter. */
    private String _filter = null;

    /** The _ratio. */
    private boolean _filtered = false;

    /**
     * Instantiates a new mover callback.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     */
    MoverCallback(final MoverServer _mover, final DataTransfer transfer, final String fileName,
            final MoverTracing.Attempt trace) {
        this._mover = _mover;
        super();

        this.trace = trace;
        _name = Format.formatLong(transfer.getId(), 10, true);
        _fileName = fileName;
        _transfer = transfer;
        _dataFile = transfer.getDataFile();
        _fileSize = _dataFile.getSize();
        _setup = HOST_ECTRANS.getECtransSetup(transfer.getHost().getData());
        _transfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
        _transfer.setComment(null);
        _transfer.setDuration(0);
        _transfer.setSent(0);
        _transfer.setDeliveredName(null);
        _transfer.setRatio(1);
        _transfer.setPutTime(null);
        _timeout = _setup.getDuration(HOST_ECTRANS_STREAM_TIMEOUT).toMillis();
        ecmwf.ecpds.master.weather.WeatherCapture.Execution capture = null;
        if (_mover.weatherNegotiationScheduler != null) {
            try {
                final var host = transfer.getHost();
                capture = _mover.weatherNegotiationScheduler.capture.start(transfer.getId(),
                        transfer.getDestinationName(), host.getName(), host.getHost(), host.getTransferMethodName());
            } catch (final RuntimeException e) {
                MoverServer._log.warn("Network Weather could not freeze transfer {} execution scope", transfer.getId(),
                        e);
            }
        }
        weather = capture;
        _transfer.setWeatherExecutionId(capture == null ? null : capture.executionId().toString());
    }

    @Override
    public void executionFinished(final TransferModule module, final boolean successful) {
        try {
            captureWeather(module, successful);
            if (confirmedPasswordDelivery != null && module != null && module.isSessionClosureConfirmed()
                    && module.isSessionDispositionCertain() && !module.getAvailable()) {
                confirmedPasswordDelivery.accept(successful && !_closed.get());
            }
        } finally {
            trace.finish(_transfer, successful && !_closed.get());
        }
    }

    void captureWeather(final TransferModule module, final boolean successful) {
        if (weather == null) {
            return;
        }
        try {
            final var statistics = module == null ? null : module.getAttribute(ClientSocketStatistics.class);
            final var operation = module == null ? null : module.getAttribute("remote.operation");
            final var cancelled = !successful && module != null
                    && Boolean.TRUE.equals(module.getAttribute("ectrans.cancellationRequested"));
            final var outcome = cancelled ? ecmwf.ecpds.master.weather.WeatherObservation.Outcome.CANCELLED
                    : !successful ? ecmwf.ecpds.master.weather.WeatherObservation.Outcome.FAILURE
                            : module == null || module.getAvailable() || "exec".equals(operation)
                                    || "copy".equals(operation)
                                            ? ecmwf.ecpds.master.weather.WeatherObservation.Outcome.UNKNOWN
                                            : ecmwf.ecpds.master.weather.WeatherObservation.Outcome.SUCCESS;
            weather.finish(outcome,
                    cancelled ? ecmwf.ecpds.master.weather.WeatherObservation.Category.CANCELLED
                            : ecmwf.ecpds.master.weather.WeatherObservation.Category.UNCLASSIFIED,
                    statistics == null ? null : statistics.snapshot());
        } catch (final RuntimeException e) {
            MoverServer._log.warn("Network Weather capture failed for transfer {}; delivery processing continues",
                    _transfer.getId(), e);
        }
    }

    @Override
    public void transferredBytes(final long bytes) {
        _mover.trafficAccounting.add(_transfer.getHost().getType(), bytes);
    }

    @Override
    public boolean isByteAccountingEnabled() {
        return true;
    }

    /**
     * Sets the file size.
     *
     * @param filter
     *            the filter
     * @param fileSize
     *            the file size
     */
    void setFileSize(final String filter, final long fileSize) {
        _filtered = _fileSize != fileSize;
        _transfer.setRatio((double) _fileSize / (double) fileSize);
        _filter = filter;
        if (_filtered) {
            // The file has already been filtered while in the queue!
            _fileSize = fileSize;
        }
    }

    /**
     * Sets the file descriptor.
     *
     * @param descriptor
     *            the new file descriptor
     */
    void setFileDescriptor(final FileDescriptor descriptor) {
        _descriptor = descriptor;
    }

    /**
     * Notifies that the {@link TransferModule} handling this transfer has connected, capturing a reference to it so
     * that {@link #_offerLiveSample(String)} can later query its real, currently connected remote address (see
     * {@link TransferModule#getConnectedRemoteAddress()}) - preferred over the configured Host address for
     * anycast/load-balanced endpoints (e.g. Amazon S3, Google Cloud Storage) where a later DNS lookup of the same
     * hostname may return a different IP than the one actually used for this transfer.
     *
     * @param module
     *            the module
     */
    @Override
    public void connected(final TransferModule module) {
        _module = module;
        if (weather != null) {
            final var statistics = module.getAttribute(ClientSocketStatistics.class);
            if (statistics != null) {
                statistics.setSnapshotListener(weather::supplement);
            }
        }
    }

    /**
     * Offer a live transfer sample to the {@link LiveStatsRepository}, if enabled (Live ECPDS Earth - Phase 0).
     *
     * @param status
     *            one of {@link LiveTransferSample#STATUS_ACTIVE}, {@link LiveTransferSample#STATUS_DONE} or
     *            {@link LiveTransferSample#STATUS_FAILED}
     */
    private void _offerLiveSample(final String status) {
        if (LiveTransferSample.STATUS_ACTIVE.equals(status) && _closed.get())
            return;
        if (_mover.liveStatsRepository == null || !_mover.liveStatsRepository.isMasterEnabled()) {
            return;
        }
        try {
            final var host = _transfer.getHost();
            final var protocol = host != null ? host.getTransferMethodName() : null;
            final var hostNickname = host != null ? host.getNickname() : null;
            final var module = _module;
            final var connectedAddress = module != null ? module.getConnectedRemoteAddress() : null;
            final var hostAddress = connectedAddress != null && !connectedAddress.isBlank() ? connectedAddress
                    : host != null ? host.getHost() : null;
            final var rate = _transfer.getDuration() > 0 ? (double) _transfer.getSent() * 8000 / _transfer.getDuration()
                    : -1;
            _mover.liveStatsRepository.offer(new LiveTransferSample(_transfer.getId(), _mover.getRoot(),
                    _transfer.getDestinationName(), _transfer.getHostName(), hostNickname, hostAddress, protocol,
                    _fileSize, _transfer.getSent(), _transfer.getDuration(), rate, status,
                    LiveTransferSample.DIRECTION_DISSEMINATION, host != null ? host.getType() : null)
                            .withAttempt(_liveAttemptId));
        } catch (final Throwable t) {
            MoverServer._log.debug("Building LiveTransferSample for DataTransfer-{}", _transfer.getId(), t);
        }
    }

    /**
     * Refresh transfer stats.
     */
    private void refreshTransferStats() {
        if (_monitor != null) {
            final var putTime = _monitor.getStartTime();
            if (_monitor instanceof final MonitoredInputStream monIn) {
                _transfer.setDurationOnClose(monIn.getDurationOnClose());
            }
            _transfer.setDuration(_monitor.getDuration());
            _transfer.setSent(_monitor.getByteSent());
            _transfer.setPutTime(putTime > 0 ? new Timestamp(putTime) : null);
        }
    }

    /**
     * Update the finish time (called from completed or failed method).
     */
    private void updateFinishTime() {
        _transfer.setFinishTime(new Timestamp(System.currentTimeMillis() - _transfer.getFinishTime().getTime()));
    }

    /**
     * Close the data transfer and the associated monitor.
     *
     * @return true, if successful
     */
    private boolean closeTransferAndMonitor() {
        if (!_closed.compareAndSet(false, true)) {
            MoverServer._log.debug("Already closed");
            return false;
        } else {
            try {
                _mover.closeDataTransfer(_transfer);
            } catch (final Throwable t) {
                MoverServer._log.warn("Closing DataTransfer: {}", _transfer.getId(), t);
            } finally {
                if (_monitor != null) {
                    try {
                        _monitor.closeAndInterruptIfRequired();
                    } finally {
                        refreshTransferStats();
                    }
                }
            }
            return true;
        }
    }

    /**
     * Gets the ectrans setup.
     *
     * @return the ectrans setup
     */
    @Override
    public ECtransSetup getECtransSetup() {
        return _setup;
    }

    /**
     * Gets the unique name.
     *
     * @return the unique name
     */
    @Override
    public String getUniqueName() {
        return _name;
    }

    /**
     * Sets the MS user.
     *
     * @param msuser
     *            the new MS user
     */
    @Override
    public void setMSUser(final MSUser msuser) {
        _transfer.setHostName(msuser.getName());
    }

    /**
     * Update.
     *
     * @param monitor
     *            the monitor
     */
    @Override
    public void update(final ProgressInterface monitor) {
        if (_monitor == null || _monitor.getByteSent() > _transfer.getSent()) {
            // First update or update in the number of bytes sent!
            _update = System.currentTimeMillis();
            _monitor = monitor;
            refreshTransferStats();
            if (_update - _lastLiveSample >= 2 * Timer.ONE_SECOND) {
                _lastLiveSample = _update;
                _offerLiveSample(LiveTransferSample.STATUS_ACTIVE);
            }
        } else if (!_closed.get() && _timeout > 0 && System.currentTimeMillis() - _update > _timeout) {
            final var module = _module;
            MoverServer._log.warn("Transfer {} no progress: idle={}ms progressBytes={} moduleStatus={}",
                    _transfer.getId(), System.currentTimeMillis() - _update,
                    _monitor == null ? 0 : _monitor.getByteSent(), module == null ? "unavailable" : module.getStatus());
            if (module != null) {
                if (module.getAttribute(
                        "diagnostics.connections") instanceof final java.util.function.Supplier<?> connections) {
                    MoverServer._log.warn("Transfer {} connection diagnostics: {}", _transfer.getId(),
                            connections.get());
                }
                for (final var name : new String[] { "diagnostics.inputBytes", "diagnostics.outputBytes" }) {
                    if (module.getAttribute(name) instanceof final java.util.function.LongSupplier counter) {
                        MoverServer._log.warn("Transfer {} {}={}", _transfer.getId(), name, counter.getAsLong());
                    }
                }
            }
            failed(module, "stream timeout after " + Format.formatDuration(_timeout));
        }
    }

    /**
     * Retry.
     *
     * @param comment
     *            the comment
     */
    @Override
    public void retry(final String comment) {
        if (!_closed.get()) {
            MoverServer._log.warn("Transfer {} ({}) retry{}", _transfer.getId(), _dataFile.getSource(),
                    isNotEmpty(comment) ? ": " + comment : "");
        }
    }

    /**
     * Completed.
     *
     * @param module
     *            the module
     */
    @Override
    public void completed(final TransferModule module) {
        captureWeather(module, true);
        if (!closeTransferAndMonitor()) { // If it is closed then no need to process the transfer history again!
            return;
        }
        try {
            if (_transfer.getDuration() == 0) {
                // If the transfer was less than a millisecond then we set 1
                // millisecond to allow the processing of the rate!
                MoverServer._log.debug("0ms duration detected for DataTransfer-{} push (forcing 1ms)",
                        _transfer.getId());
                _transfer.setDuration(1);
            }
            final var statistics = module.getAttribute(ClientSocketStatistics.class);
            final var hostName = (String) module.getAttribute("remote.hostName");
            final var fileName = (String) module.getAttribute("remote.fileName");
            final var operation = (String) module.getAttribute("remote.operation");
            final var targetName = isNotEmpty(fileName) ? fileName : _fileName;
            final var from = _mover.masterManager != null ? "DataMover=" + _mover.getRoot() : "Proxy Host="
                    + _transfer.getProxyHostName() + " (" + _transfer.getProxyHost().getNickname() + ")";
            final String comment;
            boolean usedCompressedFile = false;
            if ("exec".equals(operation)) {
                comment = "External transfer initiated from " + from + " to " + hostName + " at " + targetName;
            } else if ("copy".equals(operation)) {
                comment = "Local copy initiated from " + from + " to " + hostName + " at " + targetName;
            } else {
                final var sourceHost = _descriptor != null ? _descriptor.getSelectedSourceHost() : null;
                final var source = sourceHost != null
                        ? " using Host=" + sourceHost.getName() + " (" + sourceHost.getNickname() + ")" : "";
                if (module.getAvailable()) {
                    // The file is available but not sent anywhere (data portal), so we have to make
                    // sure there will be no transfer rate displayed!
                    _transfer.setSent(0);
                    comment = "Available on " + from + source + " as " + targetName;
                } else { // operation == sent
                    // The file has been transmitted successfully!
                    if (module.getAttribute("compression.fileSize") instanceof final Long fileSize) {
                        // This was compression on the fly so let's compute the ratio
                        // and the number of bytes sent now that we have the data!
                        _transfer.setCompressedOnTheFly(true);
                        if (fileSize > 0) {
                            _transfer.setRatio((double) _fileSize / (double) fileSize);
                        } else {
                            _transfer.setRatio(1); // safe default
                        }
                        _transfer.setSent(fileSize);
                        _filtered = true;
                    }
                    final var rate = Format.getMBitsPerSeconds(_transfer.getSent(), _transfer.getDuration());
                    final var ratio = (int) (100 / _transfer.getRatio());
                    final String compression;
                    if (_filter != null) {
                        final var onTheFly = _transfer.getCompressedOnTheFly() ? " on-the-fly" : "";
                        compression = " " + _filter + (_filtered ? " (" + ratio + "%" + onTheFly + ")" : "");
                        usedCompressedFile = !_transfer.getCompressedOnTheFly();
                    } else {
                        compression = "";
                    }
                    comment = "Sent" + compression + " from " + from + source + " to "
                            + (isNotEmpty(hostName) ? hostName + " at " : "") + targetName
                            + (rate == -1 ? "" : " (" + rate + " Mbits/s)")
                            + (module.getAttribute("s3.discoveredRegion") instanceof final String r
                                    ? " [s3.region=" + r + "]" : "");
                }
            }
            final var statisticsString = statistics != null && !statistics.isEmpty() ? statistics.toString() : null;
            synchronized (_mover.transferRepository) {
                updateFinishTime();
                _transfer.setComment(comment);
                _transfer.recordDeliveredName(fileName);
                _transfer.setStatistics(statisticsString);
                _transfer.setCompressed(_filter);
                _transfer.setStatusCode(StatusFactory.DONE);
                _mover.transferRepository.notifyAll();
            }
            MoverServer._log.info("Transfer {} ({}) completed: {} - statistics: '{}'", _transfer.getId(), targetName,
                    comment, statisticsString != null ? statisticsString : "none");
            _offerLiveSample(LiveTransferSample.STATUS_DONE);
            if (usedCompressedFile && _descriptor != null && _descriptor.isLocal()) {
                // We have successfully transmitted a local compressed file, so we might now
                // unlink the uncompressed version of it to save some disk space!
                try {
                    final var localFile = _descriptor.getFile(false);
                    if (localFile != null && localFile.supportsUnlinkWhileOpen()) {
                        if (Cnf.at("Mover", "unlinkUncompressedFileOnSuccess", false)) {
                            if (localFile.delete()) {
                                MoverServer._log.debug("File successfully unlinked after transmission: {}",
                                        localFile.getAbsolutePath());
                            } else {
                                MoverServer._log.warn("Cannot unlink file after successful transmission: {}",
                                        localFile.getAbsolutePath());
                            }
                        } else {
                            MoverServer._log.debug("Unlink of uncompressed file on success is disabled: {}",
                                    localFile.getAbsolutePath());
                        }
                    }
                } catch (final Throwable t) {
                    MoverServer._log.warn("Cannot unlink file after successful transmission", t);
                }
            }
        } finally {
            trace.finish(_transfer, StatusFactory.DONE.equals(_transfer.getStatusCode()));
            if (_descriptor != null) {
                _descriptor.releaseStorageReader();
            }
        }
    }

    /**
     * Failed.
     *
     * @param module
     *            the module
     * @param comment
     *            the comment
     */
    @Override
    public void failed(final TransferModule module, String comment) {
        captureWeather(module, false);
        final var availableStatistics = module == null ? null : module.getAttribute(ClientSocketStatistics.class);
        final var beforeCloseStatistics = availableStatistics == null || availableStatistics.isEmpty() ? null
                : availableStatistics.toString();
        if (!closeTransferAndMonitor()) { // If it is closed then no need to process the transfer history again!
            return;
        }
        try {
            comment = isNotEmpty(comment) ? comment : null;
            var existingComment = _transfer.getComment();
            existingComment = isNotEmpty(existingComment) ? existingComment : null;
            if (comment == null && existingComment == null) {
                comment = "Transmission failed";
            } else if (comment != null && existingComment != null) {
                comment = comment + " (" + existingComment + ")";
            } else {
                if (comment == null) {
                    comment = existingComment;
                }
            }
            final String statisticsString;
            if (module != null) {
                final var statistics = module.getAttribute(ClientSocketStatistics.class);
                statisticsString = statistics != null && !statistics.isEmpty() ? statistics.toString()
                        : beforeCloseStatistics;
            } else {
                statisticsString = null;
            }
            synchronized (_mover.transferRepository) {
                updateFinishTime();
                _transfer.setComment(comment);
                _transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                _transfer.setStatistics(statisticsString);
                _transfer.setStatusCode(StatusFactory.RETR);
                _mover.transferRepository.notifyAll();
            }
            MoverServer._log.warn("Transfer {} ({}) failed: {} - statistics: '{}'", _transfer.getId(),
                    _dataFile.getSource(), comment, statisticsString != null ? statisticsString : "none");
            _offerLiveSample(LiveTransferSample.STATUS_FAILED);
        } finally {
            trace.finish(_transfer, false);
            if (_descriptor != null) {
                _descriptor.releaseStorageReader();
            }
        }
    }
}
