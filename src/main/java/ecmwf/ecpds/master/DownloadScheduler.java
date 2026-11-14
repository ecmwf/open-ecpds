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
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE_STOP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_RESET_QUEUE_ON_CHANGE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUE_ON_FAILURE;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.text.Util.nullToNone;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.timer.Timer;
import ecmwf.common.database.DBIterator;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.telemetry.TransferTracing;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.HostConnectionLimits;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class DownloadScheduler.
 */
public abstract class DownloadScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _max download threads. */
    private int _maxDownloadThreads = Cnf.at("Scheduler", "maxDownloadThreads", 60);

    // Per-transfer maximum-duration/minimum-rate limits are configured per-Host via
    // ECtransOptions (HOST_RETRIEVAL_MAXIMUM_DURATION/HOST_RETRIEVAL_MINIMUM_RATE), not here.

    /** The _debug. */
    public boolean _debug = Cnf.at("Scheduler", "debug", false);

    /** The _processChecksum. */
    public boolean _processChecksum = Cnf.at("Scheduler", "processChecksum", true);

    /** The _download threads. */
    final private Map<Long, DownloadThread> _downloadThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /** The _pause. */
    private boolean _pause = false;

    /** The _description. */
    private final String _description;

    /**
     * Instantiates a new download scheduler.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     */
    public DownloadScheduler(final MasterServer _master, final String name, final String description) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        _description = description;
        setDelay(Cnf.durationAt("Scheduler", "downloadScheduler", Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "downloadSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "downloadSchedulerTimeRanges"));
    }

    /**
     * Gets the data transfers to download iterator.
     *
     * @return the data transfers to download iterator
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SQLException
     *             the SQL exception
     */
    public abstract DBIterator<DataTransfer> getDataTransfersToDownloadIterator() throws IOException, SQLException;

    /**
     * Sets the max download threads.
     *
     * @param maxDownloadThreads
     *            the new max download threads
     */
    public void setMaxDownloadThreads(final int maxDownloadThreads) {
        _maxDownloadThreads = maxDownloadThreads;
    }

    /**
     * Gets the max download threads.
     *
     * @return the max download threads
     */
    public int getMaxDownloadThreads() {
        return _maxDownloadThreads;
    }

    /**
     * Gets the download threads count.
     *
     * @return the download threads count
     */
    public int getDownloadThreadsCount() {
        return _downloadThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",downloadThreads="
                + _downloadThreads.size();
    }

    /**
     * Gets the key.
     *
     * @param transfer
     *            the transfer
     *
     * @return the string
     */
    private long _getKey(final DataTransfer transfer) {
        return transfer.getDataFileId();
    }

    /**
     * Gets the thread list.
     *
     * @return the thread list
     */
    public String getThreadList() {
        final var status = new StringBuilder();
        final var currentTime = System.currentTimeMillis();
        for (final DownloadThread thread : _downloadThreads.values()) {
            final DataTransfer transfer;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                final var size = transfer.getDataFile().getSize();
                status.append((transfer.getDestinationName() + " DataFile " + key + " ("
                        + Format.formatDuration(thread._time, currentTime) + "/"
                        + (size != -1 ? Format.formatPercentage(size, thread.getByteSent())
                                : Format.formatSize(thread.getByteSent()))
                        + (isNotEmpty(thread._root) ? " on " + thread._root : "") + ")").replace(' ', '_').replace('-',
                                '_'))
                        .append(" ");
            }
        }
        return status.toString().trim();
    }

    /**
     * Gets the total number of DataTransfers related to the DataFiles being retrieved.
     *
     * @return the count
     */
    public int getDataTransfersCount() {
        var count = 0;
        for (final DownloadThread thread : _downloadThreads.values()) {
            if (thread != null) {
                count += thread._dataTransfersCount;
            }
        }
        return count;
    }

    /**
     * Gets the progress interface.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the progress interface
     */
    public ProgressInterface getProgressInterface(final long dataFileId) {
        final var thread = _downloadThreads.get(dataFileId);
        if (thread != null && thread.isAlive()) {
            // We found it!
            return thread;
        }
        // The retrieval thread was not found or completed!
        return null;
    }

    /**
     * Interrupt all downloads.
     *
     * @param destinationName
     *            the destination name
     * @param message
     *            the message
     */
    public void interruptAllDownload(final String destinationName, final String message) {
        for (final DownloadThread thread : _downloadThreads.values()) {
            if (thread != null && thread.getDestinationName().equals(destinationName)) {
                interruptDownload(thread._transfer, message);
            }
        }
    }

    /**
     * Interrupt download.
     *
     * @param transfer
     *            the transfer
     * @param message
     *            the message
     *
     * @return true, if successful
     */
    public boolean interruptDownload(final DataTransfer transfer, final String message) {
        return interruptDownload(_getKey(transfer), message);
    }

    /**
     * Interrupt download.
     *
     * @param key
     *            the key
     * @param message
     *            the message
     *
     * @return true, if successful
     */
    public boolean interruptDownload(final long key, final String message) {
        final var thread = _downloadThreads.get(key);
        if (thread != null && thread.isAlive()) {
            try {
                thread._message = message;
                thread._time = System.currentTimeMillis();
                thread.interrupt();
                return true;
            } catch (final Throwable t) {
                MasterServer._log.warn("Interrupting DownloadThread", t);
            }
        }
        return false;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), _description, new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("DebugEnabled", "java.lang.Boolean",
                        "DebugEnabled: log the reason each DataTransfer is retrieved, delayed or skipped.", true, true,
                        false),
                new MBeanAttributeInfo("ProcessChecksumEnabled", "java.lang.Boolean",
                        "ProcessChecksumEnabled: when true, a checksum is generated per downloaded DataFile; when false it is stored as \"none\".",
                        true, true, false),
                new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                        "ThreadCount: number of DownloadThread(s) currently tracked.", true, false, false),
                new MBeanAttributeInfo("ThreadList", "java.lang.String",
                        "ThreadList: space-separated list of in-progress downloads, each as destination_DataFile_id_(elapsed/percent-or-bytes).",
                        true, false, false),
                new MBeanAttributeInfo("DataTransfersCount", "java.lang.Integer",
                        "DataTransfersCount: number of DataTransfer(s) related to the DataFile(s) retrieved.", true,
                        false, false),
                new MBeanAttributeInfo("MaximumDownloadThreads", "java.lang.Integer",
                        "MaximumDownloadThreads: maximum number of parallel transfers.", true, true, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("interruptDownload",
                        "interruptDownload(id): interrupt download for the specified DataFile",
                        new MBeanParameterInfo[] { new MBeanParameterInfo("id", "java.lang.Integer", "DataFile id") },
                        "java.lang.Boolean", MBeanOperationInfo.ACTION) });
    }

    /**
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("interruptDownload".equals(operationName) && signature.length == 1
                    && "java.lang.Integer".equals(signature[0])) {
                return interruptDownload((Integer) params[0], "Retrieval interrupted via the JMX management interface");
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("DebugEnabled".equals(attributeName)) {
                return _debug;
            }
            if ("ProcessChecksumEnabled".equals(attributeName)) {
                return _processChecksum;
            }
            if ("DataTransfersCount".equals(attributeName)) {
                return getDataTransfersCount();
            }
            if ("ThreadCount".equals(attributeName)) {
                return _downloadThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumDownloadThreads".equals(attributeName)) {
                return _maxDownloadThreads;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("MaximumDownloadThreads".equals(name)) {
            _maxDownloadThreads = (Integer) value;
            return true;
        }
        if ("DebugEnabled".equals(name)) {
            _debug = (Boolean) value;
            return true;
        }
        if ("ProcessChecksumEnabled".equals(name)) {
            _processChecksum = (Boolean) value;
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Sets the pause.
     *
     * @param pause
     *            the new pause
     */
    public void setPause(final boolean pause) {
        _pause = pause;
    }

    /**
     * Gets the pause.
     *
     * @return the pause
     */
    public boolean getPause() {
        return _pause;
    }

    /**
     * Checks if is acquisition.
     *
     * @param hostsPerDestination
     *            the hosts per destination
     * @param transfer
     *            the transfer
     *
     * @return true, if successful
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private boolean _isAcquisition(final HashMap<String, Collection<Host>> hostsPerDestination,
            final DataTransfer transfer) throws DataBaseException {
        final var destination = transfer.getDestination();
        if (destination.getAcquisition()) {
            // Was this DataTransfer submitted by the Acquisition process?
            final var hostName = transfer.getDataFile().getHostForAcquisitionName();
            if (hostName != null) {
                // Is it a DataTransfer Alias or was it submitted through
                // its original Destination?
                final var destinationName = destination.getName();
                var hosts = hostsPerDestination.get(destinationName);
                if (hosts == null) {
                    hostsPerDestination.put(destinationName,
                            hosts = _master.getECpdsBase().getHostsByDestinationId(destinationName));
                }
                for (final Host host : hosts) {
                    if (HostOption.ACQUISITION.equals(host.getType()) && hostName.equals(host.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        final var start = System.currentTimeMillis();
        var processed = 0;
        // Flush completed threads before checking capacity to avoid opening a potentially
        // expensive DB query when the pool is already full.
        synchronized (_toRemove) {
            for (final long key : _toRemove) {
                final var thread = _downloadThreads.remove(key);
                MasterServer._log.debug("DownloadThread " + key + " removed (" + (thread != null) + ")");
            }
            _toRemove.clear();
        }
        if (getDownloadThreadsCount() >= _maxDownloadThreads) {
            return NEXT_STEP_DELAY;
        }
        try (var it = getDataTransfersToDownloadIterator()) {
            final var hostsPerDestination = new HashMap<String, Collection<Host>>();
            while (!_pause && isRunning()) {
                // Check capacity before fetching the next row from the DB cursor so we stop
                // consuming the result set the moment the pool is full.
                if (getDownloadThreadsCount() >= _maxDownloadThreads) {
                    if (_debug) {
                        MasterServer._log
                                .debug("Maximum number of parallel retrieval reached (" + _maxDownloadThreads + ")");
                    }
                    break;
                }
                if (!it.hasNext()) {
                    break;
                }
                try {
                    // Let's go through every selected DataTransfer from the database
                    final var transfer = it.next();
                    final var destination = transfer.getDestination();
                    final var isAcquisition = _isAcquisition(hostsPerDestination, transfer);
                    // May be the DataTransfer was already retrieved?
                    if (transfer.getDataFile().getDownloaded()) {
                        MasterServer._log.debug("DataTransfer " + transfer.getId() + " already downloaded");
                        continue;
                    }
                    // Check if this DataTransfer is not currently processed on the MasterServer? If
                    // it is the case then we should delay the retrieval!
                    final var fromCache = _master.getDataTransferFromCache(transfer.getId());
                    if (fromCache != null) {
                        final var code = fromCache.getStatusCode();
                        if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                && !StatusFactory.HOLD.equals(code)) {
                            if (_debug) {
                                MasterServer._log.debug("Delay Download for DataTransfer " + transfer.getId() + ": "
                                        + code + " (in cache)");
                            }
                            continue;
                        }
                    }
                    // If this is the acquisition then we want to check the number of parallel
                    // connections just in case there would be a limit on the Destination?
                    var goToNext = false;
                    if (isAcquisition) {
                        // This is the acquisition!
                        final var limit = destination.getMaxConnections();
                        if (limit >= 0) {
                            // We definitely have a limit!
                            final var name = destination.getName();
                            var count = 0;
                            for (final DownloadThread thread : _downloadThreads.values()) {
                                if (name.equals(thread._transfer.getDestinationName())) {
                                    count++;
                                }
                            }
                            if (count >= limit) {
                                if (_debug) {
                                    MasterServer._log.debug("Delay Download for DataTransfer " + transfer.getId()
                                            + " (max connections is " + count + " for " + name + ")");
                                }
                                goToNext = true;
                            }
                        }
                    } else {
                        // This is not an Acquisition Destination!
                        final var dataFile = transfer.getDataFile();
                        // This file was queued by an acquisition
                        // Destination and is therefore an Alias. The
                        // DataFile will be retrieved with the original
                        // DataTransfer, not this one. This will allow
                        // to implement the limit above. However if the
                        // file was already retrieved on the data mover
                        // then we can process it!
                        if ((dataFile.getHostForAcquisitionName() != null) && !dataFile.getDownloaded()) {
                            if (_debug) {
                                MasterServer._log.debug("Delay Download for DataTransfer " + transfer.getId()
                                        + " (Acquisition Destination must retrieve the file first)");
                            }
                            continue;
                        }
                    }
                    if (goToNext) {
                        continue;
                    }
                    // Is it processed in the ECpdsPlugin?
                    if (_master.transferIsLocked(transfer.getUniqueKey())) {
                        if (_debug) {
                            MasterServer._log.debug("DataTransfer " + transfer.getId() + " is locked");
                        }
                        continue;
                    }
                    // We now have to check if this transfer is not already
                    // processed by this scheduler!
                    synchronized (_downloadThreads) {
                        if (!_downloadThreads.containsKey(_currentKey = _getKey(transfer))) {
                            final var source = isAcquisition
                                    ? _master.getECpdsBase().getHost(transfer.getDataFile().getHostForAcquisitionName())
                                    : null;
                            final var reservation = isAcquisition
                                    ? _master.hostConnectionLimits.acquire(source, destination.getName()) : null;
                            if (isAcquisition && reservation == null) {
                                MasterServer._log.debug("Delay retrieval {}: Host {} connection limit reached",
                                        transfer.getId(), source.getName());
                                continue;
                            }
                            final var thread = new DownloadThread(transfer, source, reservation);
                            try {
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + transfer.getDataFileId(), null);
                                _downloadThreads.put(_currentKey, thread);
                                thread.execute(true);
                            } catch (final Throwable t) {
                                _downloadThreads.remove(_currentKey, thread);
                                if (reservation != null) {
                                    reservation.close();
                                }
                                throw t;
                            }
                            processed++;
                        } else if (_debug) {
                            MasterServer._log
                                    .debug("Download already in progress for DataTransfer " + transfer.getId());
                        }
                    }
                } catch (final Throwable t) {
                    MasterServer._log.warn("nextStep", t);
                }
            }
            // We now check if some DownloadThread have not completed and
            // have requested to be removed from the cache?
            synchronized (_toRemove) {
                for (final long key : _toRemove) {
                    final var thread = _downloadThreads.remove(key);
                    MasterServer._log.debug("DownloadThread " + key + " removed (" + (thread != null) + ")");
                }
                _toRemove.clear();
            }
            // If it took more than 1m then log it!
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for download",
                        Format.formatDuration(duration), processed);
            }
        } catch (final Throwable t) {
            MasterServer._log.warn("Error in scheduler", t);
        }
        // If there are still files to be processed then there is no reason
        // to wait!
        return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
    }

    /**
     * The Class DownloadThread.
     */
    final class DownloadThread extends ConfigurableRunnable implements ProgressInterface {
        final String _liveAttemptId = java.util.UUID.randomUUID().toString();
        /** The _time. */
        private long _time = System.currentTimeMillis();

        /** The _transfer. */
        private DataTransfer _transfer = null;

        /** The source Host which is used for the retrieval. */
        private Host _source = null;

        private final HostConnectionLimits.Lease _reservation;

        /** Whether this is a genuine Acquisition retrieval (as opposed to a Dissemination-side source pull). */
        private boolean _acquisition = false;

        /**
         * Used to indicate the interruption of the thread when the transmission takes too long.
         */
        private String _message = null;

        /** The _transfer server name. */
        private String _root = null;

        /** The _byte sent. */
        private long _byteSent = 0;

        /** The number of data transfers related to this DataFile. */
        private int _dataTransfersCount = 1;

        /**
         * Instantiates a new download thread.
         *
         * @param transfer
         *            the transfer
         */
        private DownloadThread(final DataTransfer transfer, final Host source,
                final HostConnectionLimits.Lease reservation) {
            _transfer = transfer;
            _source = source;
            _reservation = reservation;
        }

        /**
         * Gets the data file id.
         *
         * @return the data file id
         */
        @Override
        public long getDataFileId() {
            return _transfer.getDataFileId();
        }

        /**
         * Gets the destination name.
         *
         * @return the destination name
         */
        /*
         * Allow getting the Destination for this retrieval.
         */
        public String getDestinationName() {
            return _transfer.getDestinationName();
        }

        /**
         * Gets the source Host used for this retrieval, for the "Live ECPDS Earth" globe visualisation.
         *
         * @return the source host
         */
        @Override
        public Host getSourceHost() {
            return _source;
        }

        /**
         * Whether this is a genuine Acquisition retrieval, for the "Live ECPDS Earth" globe visualisation.
         *
         * @return true, if this is an Acquisition retrieval
         */
        @Override
        public boolean isAcquisition() {
            return _acquisition;
        }

        /**
         * Gets the total duration since this retrieval started, for the "Live ECPDS Earth" globe visualisation.
         *
         * @return the duration
         */
        @Override
        public long getDuration() {
            return System.currentTimeMillis() - _time;
        }

        /**
         * Gets the total size of the file being retrieved, for the "Live ECPDS Earth" globe visualisation.
         *
         * @return the file size
         */
        @Override
        public long getFileSize() {
            return _transfer.getDataFile().getSize();
        }

        /**
         * Update.
         *
         * @param root
         *            the root
         * @param byteSent
         *            the byte sent
         */
        @Override
        public void update(final String root, final long byteSent) {
            // If the dataFile has been fully downloaded then we set the
            // bytes sent value to the size of the DataFile.
            final var dataFile = _transfer.getDataFile();
            final var size = dataFile.getSize();
            _byteSent = dataFile.getDownloaded() && size != -1 ? size : byteSent;
            _root = root;
        }

        /**
         * Gets the byte sent.
         *
         * @return the byte sent
         */
        @Override
        public long getByteSent() {
            // If the dataFile has been fully downloaded then we always
            // return the size, otherwise we return the last bytes sent
            // value that we have.
            final var dataFile = _transfer.getDataFile();
            final var size = dataFile.getSize();
            return dataFile.getDownloaded() && size != -1 ? size : _byteSent;
        }

        /**
         * Gets the root.
         *
         * @return the root
         */
        @Override
        public String getRoot() {
            return _root;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            try (_reservation) {
                retrieve();
            } finally {
                _toRemove.add(_getKey(_transfer));
            }
        }

        private void retrieve() {
            var complete = false;
            final var key = _getKey(_transfer);
            final List<DataTransfer> relatedTransfers = new ArrayList<>();
            final var base = _master.getDataBase(ECpdsBase.class);
            final var originalStatus = _transfer.getStatusCode();
            MasterServer._log.debug("Starting DownloadThread {}", key);
            try {
                final var start = System.currentTimeMillis();
                final var dataFile = _transfer.getDataFile();
                final var dataFileId = dataFile.getId();
                final var hostForSourceName = dataFile.getHostForAcquisitionName();
                final var acquisition = isNotEmpty(hostForSourceName);
                _acquisition = acquisition;
                if (acquisition) {
                    MasterServer._log.debug("Registered by the Acquisition Thread");
                    if (_source == null) {
                        _source = base.getHost(hostForSourceName);
                    }
                } else {
                    _source = _transfer.getDestination().getHostForSource();
                }
                // Get all the DataTransfers related to this DataFile!
                relatedTransfers.addAll(base.getDataTransfersByDataFileId(_master.dataCache, dataFileId));
                _dataTransfersCount = relatedTransfers.size();
                // Add a new history for every DataTransfer related to this
                // DataFile to inform about the start of the retrieval and
                // update the status to FETC!
                final var comment = " using Host=" + _source.getName() + " (" + _source.getNickname()
                        + ") from Destination=" + _transfer.getDestinationName();
                for (final DataTransfer transfer : relatedTransfers) {
                    transfer.setComment("Starting retrieval" + comment);
                    final var status = StatusFactory.FETC;
                    transfer.setStatusCode(status);
                    // Make sure that when we update the common dataFile it
                    // is propagated to every transfer!
                    transfer.setDataFile(dataFile);
                    base.update(transfer);
                    _master.addTransferHistory(transfer, _source, transfer.getStatusCode(), transfer.getComment(),
                            false);
                }
                // Shall we generate a checksum?
                dataFile.setChecksum(_processChecksum ? null : "none");
                final var retrievalTrace = TransferTracing.retrieval(dataFile, _transfer, _source);
                final TransferScheduler.DownloadResult dr;
                try (retrievalTrace) {
                    dr = TransferScheduler.download(_transfer, _source, relatedTransfers, retrievalTrace);
                    retrievalTrace.finish(dr.complete);
                }
                final var completed = System.currentTimeMillis();
                TransferMetrics.retrievalCompleted(dr.complete, completed - start,
                        dr.complete ? dr.dataFile.getSize() : -1, retrievalTrace.context());
                _transfer = base.getDataTransfer(_transfer.getId());
                final var server = dr.server;
                if (server != null && dr.complete) {
                    // Do we have a checksum?
                    final var checksum = dr.dataFile.getChecksum();
                    // The DataFile was successfully retrieved from the
                    // remote site!
                    final var remoteHost = dr.dataFile.getRemoteHost();
                    dataFile.setRemoteHost(remoteHost);
                    dataFile.setTransferGroup(dr.dataFile.getTransferGroup());
                    dataFile.setTransferGroupName(dr.dataFile.getTransferGroupName());
                    dataFile.setFileSystem(dr.dataFile.getFileSystem());
                    dataFile.setGetHost(dr.dataFile.getGetHost());
                    dataFile.setGetTime(dr.dataFile.getGetTime());
                    dataFile.setGetCompleteDuration(completed - start);
                    dataFile.setGetDuration(dr.dataFile.getGetDuration());
                    dataFile.setSize(dr.dataFile.getSize());
                    dataFile.setChecksum(checksum);
                    dataFile.setDownloaded(true);
                    base.update(dataFile);
                    for (final DataTransfer transfer : relatedTransfers) {
                        // First let's add a new history with the transfer rate!
                        final var index = dataFile.getIndex();
                        transfer.setComment("Retrieved " + (index > 0 ? index : 1) + " file(s)"
                                + (isNotEmpty(remoteHost) ? " from " + remoteHost : "") + " on DataMover="
                                + dataFile.getGetHost() + " ("
                                + Format.getMBitsPerSeconds(dataFile.getSize(), dataFile.getGetDuration()) + " Mbits/s)"
                                + (dr.dataFile.isRetrievalSizeVerificationBypassed() ? " - Size verification bypassed"
                                        : ""));
                        _master.addTransferHistory(transfer, _source, transfer.getStatusCode(), transfer.getComment(),
                                false);
                        // Emit the retrieval outcome to the enabled event outputs.
                        if (MasterServer._operationalEvents.isEnabled()) {
                            final var destination = transfer.getDestination();
                            final var ecauthHost = dataFile.getEcauthHost();
                            final var ecauthUser = dataFile.getEcauthUser();
                            final var login = _source.getLogin();
                            final var host = _source.getHost();
                            MasterServer._operationalEvents
                                    .emit(retrievalTrace.context(), retrievalTrace.id(), "RET",
                                            field("Monitored", destination.getMonitor()),
                                            field("TimeStamp", Timestamp.from(Instant.now())),
                                            field("DataTransferId", transfer.getId()),
                                            field("DestinationName", destination.getName()),
                                            field("DestinationType", DestinationOption.getLabel(destination.getType())),
                                            field("FileName", dataFile.getOriginal()),
                                            field("FileSize", dataFile.getSize()),
                                            field("Identity", transfer.getIdentity()),
                                            field("ScheduledTime", transfer.getScheduledTime()),
                                            field("StartTime", dataFile.getGetTime()),
                                            field("MetaStream", nullToNone(dataFile.getMetaStream())),
                                            field("MetaType", nullToNone(dataFile.getMetaType())),
                                            field("MetaTime", dataFile.getMetaTime()),
                                            field("TimeBase", dataFile.getTimeBase()),
                                            field("TimeStep", dataFile.getTimeStep()),
                                            field("Duration", dataFile.getGetCompleteDuration()),
                                            field("HostId", _source.getName()),
                                            field("HostLogin",
                                                    (isNotEmpty(login) ? Format.replaceAll(login,
                                                            "$dataFile[ecauthUser]", ecauthUser) : "-")),
                                            field("HostAddress",
                                                    (isNotEmpty(host) ? Format.replaceAll(host, "$dataFile[ecauthHost]",
                                                            ecauthHost) : "-")),
                                            field("TransferProtocol",
                                                    _source.getTransferMethod().getECtransModuleName()),
                                            field("MoverName", server.getName()),
                                            field("UserId", (isNotEmpty(ecauthUser) ? ecauthUser : "-")),
                                            field("RequestAddress", (isNotEmpty(ecauthHost) ? ecauthHost : "-")),
                                            field("DataOnlyDuration", dataFile.getGetDuration()),
                                            field("StandBy", dataFile.getStandby()),
                                            field("RemoteHost", nullToNone(remoteHost)));
                        }
                        // If this is the acquisition then the Status of the
                        // DataTransfer should be set to WAIT otherwise it
                        // would be set to FAIL by the transfer scheduler.
                        // Cause we don't allow retries on retrieval for the
                        // acquisition!
                        final var target = transfer.getTarget();
                        final var destination = transfer.getDestination();
                        final var setup = DESTINATION_SCHEDULER.getECtransSetup(destination.getData());
                        final var forceOptions = setup.getOptions(DESTINATION_SCHEDULER_FORCE, target, null);
                        final boolean standby;
                        if ("never".equalsIgnoreCase(setup.getString(DESTINATION_SCHEDULER_STANDBY))
                                || "never".equalsIgnoreCase(forceOptions.get("standby", ""))
                                        && forceOptions.matches("pattern", target, ".*")
                                        && !forceOptions.matches("ignore", target)) {
                            MasterServer._log.debug("WAIT forced by scheduler option in Destination");
                            standby = false;
                        } else if (setup.getBoolean(DESTINATION_SCHEDULER_STANDBY)
                                || forceOptions.get("standby", false) && forceOptions.matches("pattern", target, ".*")
                                        && !forceOptions.matches("ignore", target)) {
                            MasterServer._log.debug("Standby forced by scheduler option in Destination");
                            standby = true;
                        } else {
                            standby = dataFile.getStandby();
                        }
                        final var currentStatus = standby ? StatusFactory.HOLD
                                : acquisition || dataFile.getFileInstance() == null ? StatusFactory.WAIT
                                        : StatusFactory.RETR;
                        transfer.setStatusCode(currentStatus);
                        // If this is the experimental ECPDS then all the transfers to a stop
                        // destination will be stopped!
                        final var forceStop = !Cnf.at("ECpdsPlugin", "failOnDestinationNotFound", true)
                                && setup.getBoolean(DESTINATION_SCHEDULER_FORCE_STOP)
                                && StatusFactory.STOP.equals(destination.getStatusCode());
                        if (forceStop) {
                            transfer.setStatusCode(StatusFactory.STOP);
                            transfer.setComment("Not scheduled (destination is stopped)");
                        } else if (StatusFactory.HOLD.equals(currentStatus)) {
                            transfer.setComment("Not scheduled yet (manual queuing is required)");
                            transfer.setNotify(true);
                        } else {
                            transfer.setComment("Scheduled for "
                                    + (transfer.getAsap() ? "as soon as possible" : "no sooner than " + Format
                                            .formatTime("MMM dd HH:mm:ss", transfer.getScheduledTime().getTime())));
                        }
                        transfer.setTransferServer(server);
                        transfer.setTransferServerName(server.getName());
                        transfer.setOriginalTransferServer(server);
                        transfer.setOriginalTransferServerName(server.getName());
                        transfer.setReplicateTime(null);
                        transfer.setReplicated(false);
                        transfer.setReplicateCount(0);
                        transfer.setSize(dr.dataFile.getSize());
                        transfer.setDataFile(dataFile);
                        transfer.setDataFileId(dataFileId);
                        // For ASAP transfers set to WAIT, ensure retryTime is in the
                        // past. If not reset here, a stale future retryTime (e.g. from
                        // a previous RETR cycle) would cause _reload() to silently skip
                        // the transfer until the retryTime expires.
                        if (transfer.getAsap() && StatusFactory.WAIT.equals(transfer.getStatusCode())) {
                            transfer.setRetryTime(new Timestamp(System.currentTimeMillis()));
                        }
                        if (StatusFactory.RETR.equals(currentStatus)) {
                            // The update does not write an history if the
                            // status is RETR.
                            _master.addTransferHistory(transfer);
                        }
                        final var captureResult = _master.captureStrictNotification(transfer);
                        if (captureResult == MasterServer.StrictCaptureResult.ALREADY_COMMITTED) {
                            continue;
                        }
                        _master.theTransferRepository.update(transfer);
                        if (setup.getBoolean(DESTINATION_SCHEDULER_RESET_QUEUE_ON_CHANGE)) {
                            _master.reloadDestination(transfer);
                        }
                    }
                    MasterServer._log
                            .info("DataFile " + dataFileId + " retrieved successfully from " + dataFile.getGetHost()
                                    + ": " + Format.formatRate(dataFile.getSize(), dataFile.getGetDuration())
                                    + (checksum != null ? " (checksum: " + checksum + ")" : ""));
                } else {
                    // The retrieval process failed!
                    if (_message == null) {
                        // There is not initial message so let's see if we
                        // had an error from the download process?
                        if (dr.message == null) {
                            // No messages from anywhere?
                            _message = "Retrieval failed on all data movers";
                        } else {
                            // Let's display the error we had during the
                            // download!
                            _message = dr.message;
                        }
                    }
                    MasterServer._log.warn("DataFile " + dataFileId + ": " + _message);
                    // Let's reset the checksum!
                    dataFile.setChecksum(null);
                    // We have to process every related DataTransfer to be
                    // sure the DataFile will not be retried before the
                    // time has expired with another Destination (when we
                    // have aliases)!
                    final var currentTime = new Timestamp(System.currentTimeMillis());
                    for (final DataTransfer transfer : relatedTransfers) {
                        transfer.setReplicateTime(currentTime);
                        transfer.setReplicated(false);
                        transfer.setReplicateCount(transfer.getReplicateCount() + 1);
                        transfer.setComment(_message);
                        _master.addTransferHistory(transfer, _source, StatusFactory.STOP, _message, true);
                        final String currentStatus;
                        if (acquisition) {
                            // If this is the acquisition then the Status of
                            // the DataTransfer should be set to FAIL
                            // otherwise it would be retried (the FAIL
                            // status will be set in the Transfer
                            // Scheduler)!
                            if (HOST_ACQUISITION.getECtransSetup(_source.getData())
                                    .getBoolean(HOST_ACQUISITION_REQUEUE_ON_FAILURE)) {
                                currentStatus = StatusFactory.SCHE;
                            } else {
                                // For standby transfers, set FAIL directly instead of RETR.
                                // A RETR status on a standby-only destination (with no
                                // dissemination hosts) causes the TransferScheduler to throw
                                // NoHostException, which incorrectly puts the destination in
                                // the "NoHosts" failure state. Failing the transfer directly
                                // matches the behaviour of the success path: re-discovery by
                                // the Acquisition Scheduler will requeue it when appropriate.
                                final var target = transfer.getTarget();
                                final var destination = transfer.getDestination();
                                final var destSetup = DESTINATION_SCHEDULER.getECtransSetup(destination.getData());
                                final var forceOpts = destSetup.getOptions(DESTINATION_SCHEDULER_FORCE, target, null);
                                final boolean standby;
                                if ("never".equalsIgnoreCase(destSetup.getString(DESTINATION_SCHEDULER_STANDBY))
                                        || "never".equalsIgnoreCase(forceOpts.get("standby", ""))
                                                && forceOpts.matches("pattern", target, ".*")
                                                && !forceOpts.matches("ignore", target)) {
                                    standby = false;
                                } else if (destSetup.getBoolean(DESTINATION_SCHEDULER_STANDBY)
                                        || forceOpts.get("standby", false) && forceOpts.matches("pattern", target, ".*")
                                                && !forceOpts.matches("ignore", target)) {
                                    standby = true;
                                } else {
                                    standby = dataFile.getStandby();
                                }
                                currentStatus = standby ? StatusFactory.FAIL : StatusFactory.RETR;
                            }
                        } else if (dr.noSuchFile && Cnf.at("Scheduler", "failOnMissingSourceFile", false)) {
                            // The file does not exist on the source host; retrying will never
                            // succeed. Set to FAIL immediately for dissemination mode.
                            currentStatus = StatusFactory.FAIL;
                        } else {
                            // Let's put it back to SCHE so that the
                            // retrieval will be restarted later on!
                            currentStatus = StatusFactory.SCHE;
                        }
                        transfer.setStatusCode(currentStatus);
                        // No need to add history, it will be done in the
                        // transfer scheduler!
                        base.update(transfer);
                    }
                }
                complete = dr.complete;
            } catch (final Throwable t) {
                MasterServer._log.warn("Running DownloadThread " + key, t);
                // If there was a problem then we have to make sure the
                // DataTransfer is rescheduled for retrieval and to add the
                // proper error message in the history!
                final var currentTime = new Timestamp(System.currentTimeMillis());
                final var message = "Retrieval failed - " + Format.getMessage(t);
                for (final DataTransfer transfer : relatedTransfers) {
                    transfer.setReplicateTime(currentTime);
                    transfer.setReplicated(false);
                    transfer.setReplicateCount(transfer.getReplicateCount() + 1);
                    transfer.setComment(message);
                    _master.addTransferHistory(transfer, _source, StatusFactory.STOP, message, true);
                    transfer.setStatusCode(originalStatus);
                    // If we can't update the DataBase then we give up :-(
                    base.tryUpdate(transfer);
                }
            } finally {
                // Terminal notification for the "Live ECPDS Earth" globe visualisation (see
                // _offerAcquisitionLiveSample): no-ops for a non-Acquisition retrieval (_acquisition is only true
                // for a genuine Acquisition DownloadThread) or if the registry has no interested listener. Without
                // this, a retried Acquisition of the same DataFile would silently undercount the rolling 24h
                // bytes total (see the method's Javadoc), and the "Active Transfers" marker would only clear once
                // it goes stale rather than immediately. Wrapped separately (rather than relying solely on
                // _offerAcquisitionLiveSample's own try/catch) since getByteSent() is evaluated as an argument
                // *before* that method is entered - any exception there must not prevent _toRemove.add(key) below
                // from running.
                try {
                    _master._offerAcquisitionLiveSample(this, _root, getByteSent(),
                            complete ? LiveTransferSample.STATUS_DONE : LiveTransferSample.STATUS_FAILED);
                } catch (final Throwable t) {
                    MasterServer._log.debug("Sending terminal LiveTransferSample for DataFile-{}", key, t);
                }
                MasterServer._log.info("DataFile " + key + " downloaded: " + complete);
            }
        }
    }

}
