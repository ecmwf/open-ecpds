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
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.nullToNone;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.timer.Timer;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class ReplicateScheduler.
 */
public final class ReplicateScheduler extends MBeanScheduler {
    private final MasterServer _master;

    private ECpdsBase.ReplicationCursor replicationCursor = ECpdsBase.ReplicationCursor.empty();
    /** The _time out replicate thread. */
    private long _timeOutReplicateThread = Cnf.durationAt("Scheduler", "timeOutReplicateThread", -1);

    /** The _max replicate threads. */
    private int _maxReplicateThreads = Cnf.at("Scheduler", "maxReplicateThreads", 200);

    /** The _max replicate threads per mover. */
    private int _maxReplicateThreadsPerMover = Cnf.at("Scheduler", "maxReplicateThreadsPerMover", 10);

    /** The _replicate threads. */
    final Map<Long, ReplicateThread> _replicateThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new replicate scheduler.
     *
     * @param name
     *            the name
     */
    public ReplicateScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "replicateScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "replicateSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "replicateSchedulerTimeRanges"));
    }

    /**
     * Gets the replicate threads count.
     *
     * @return the replicate threads count
     */
    public int getReplicateThreadsCount() {
        return _replicateThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",replicateThreads="
                + _replicateThreads.size();
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
        for (final ReplicateThread thread : _replicateThreads.values()) {
            final DataTransfer transfer;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                status.append((transfer.getDestinationName() + " DataFile " + key + " on " + thread.getSourceMover()
                        + " (" + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-',
                                '_'))
                        .append(" ");
            }
        }
        return status.toString().trim();
    }

    /**
     * Return the number of threads with the specified sourceMover.
     *
     * @param sourceMover
     *            the source mover
     *
     * @return the thread count
     */
    public int getThreadCount(final String sourceMover) {
        var count = 0;
        for (final ReplicateThread thread : _replicateThreads.values()) {
            if (thread != null && !_toRemove.contains(_getKey(thread._transfer))
                    && sourceMover.equals(thread.getSourceMover())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The ReplicateScheduler is used to manage the replication threads.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of replicate thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("MaximumReplicateThreads", "java.lang.Integer",
                                "MaximumReplicateThreads: maximum number of parallel replications.", true, true, false),
                        new MBeanAttributeInfo("MaximumReplicateThreadsPerMover", "java.lang.Integer",
                                "MaximumReplicateThreadsPerMover: maximum number of parallel replications per data mover.",
                                true, true, false),
                        new MBeanAttributeInfo("ReplicateThreadsTimeout", "java.lang.Long",
                                "ReplicateThreadsTimeout: milliseconds a replication thread may run before being interrupted (-1 disables this check).",
                                true, true, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active replicate thread(s) shown as destination, DataFile id, source mover and elapsed running time.",
                                true, false, false) },
                new MBeanOperationInfo[0]);
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
            if ("ThreadCount".equals(attributeName)) {
                return _replicateThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumReplicateThreads".equals(attributeName)) {
                return _maxReplicateThreads;
            }
            if ("MaximumReplicateThreadsPerMover".equals(attributeName)) {
                return _maxReplicateThreadsPerMover;
            }
            if ("ReplicateThreadsTimeout".equals(attributeName)) {
                return _timeOutReplicateThread;
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
        if ("MaximumReplicateThreads".equals(name)) {
            _maxReplicateThreads = (Integer) value;
            return true;
        }
        if ("MaximumReplicateThreadsPerMover".equals(name)) {
            _maxReplicateThreadsPerMover = (Integer) value;
            return true;
        }
        if ("ReplicateThreadsTimeout".equals(name)) {
            _timeOutReplicateThread = (Long) value;
            return true;
        }
        return super.setAttribute(name, value);
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
                final var thread = _replicateThreads.remove(key);
                MasterServer._log.debug("ReplicateThread " + key + " removed (" + (thread != null) + ")");
            }
            _toRemove.clear();
        }
        if (getReplicateThreadsCount() >= _maxReplicateThreads) {
            return NEXT_STEP_DELAY;
        }
        if (!_master.storageLifecycle.enabled()) {
            replicationCursor = ECpdsBase.ReplicationCursor.empty();
        }
        try (var it = _master.getECpdsBase().getDataTransfersToReplicateIterator(2 * _maxReplicateThreads,
                replicationCursor)) {
            int fetched = 0;
            while (isRunning() && it.hasNext()) {
                // Check capacity before fetching the next row from the DB cursor so we stop
                // consuming the result set the moment the pool is full.
                if (getReplicateThreadsCount() >= _maxReplicateThreads) {
                    break;
                }
                try {
                    final var transfer = it.next();
                    fetched++;
                    if (_master.storageLifecycle.enabled()) {
                        // Deferred files must not hide later eager/eligible files behind the query limit.
                        replicationCursor = ECpdsBase.ReplicationCursor.after(transfer);
                    }
                    if (transfer.getReplicated()) {
                        MasterServer._log.warn("DataTransfer " + transfer.getId() + " already replicated");
                        continue;
                    }
                    final var fromCache = _master.getDataTransferFromCache(transfer.getId());
                    if (fromCache != null) {
                        final var code = fromCache.getStatusCode();
                        if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                && !StatusFactory.HOLD.equals(code) && !StatusFactory.DONE.equals(code)) {
                            MasterServer._log.debug("Delay Replication for DataTransfer " + transfer.getId() + ": "
                                    + code + " (in cache)");
                            continue;
                        }
                    }
                    try (final var _ = ecmwf.ecpds.master.storage.StorageFileLocks.read(transfer.getDataFileId())) {
                        if (!_master.storageLifecycle.shouldReplicate(transfer)) {
                            continue;
                        }
                        final var sourceMover = _master.storageLifecycle.replicationSource(transfer,
                                TransferScheduler.getTransferServerUsedForRetrieval(transfer));
                        if (getThreadCount(sourceMover) >= _maxReplicateThreadsPerMover) {
                            continue;
                        }
                        synchronized (_replicateThreads) {
                            if (!_replicateThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new ReplicateThread(sourceMover, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _replicateThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    }
                } catch (final Throwable t) {
                    MasterServer._log.warn("nextStep", t);
                }
            }
            if (!it.hasNext() && fetched < 2 * _maxReplicateThreads) {
                replicationCursor = ECpdsBase.ReplicationCursor.empty();
            }
            synchronized (_toRemove) {
                for (final long key : _toRemove) {
                    _replicateThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired ReplicateThreads
            if (_timeOutReplicateThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final ReplicateThread thread : _replicateThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread._time > _timeOutReplicateThread) {
                        MasterServer._log.warn("ReplicateThread DataFile-" + _getKey(thread._transfer) + " expired ("
                                + Format.formatDuration(thread._time, currentTime) + ")");
                        try {
                            thread.interrupt();
                        } catch (final Throwable t) {
                            MasterServer._log.warn("Interrupting ReplicateThread", t);
                        }
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for replication",
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
     * The Class ReplicateThread.
     */
    final class ReplicateThread extends ConfigurableRunnable {

        volatile String _phase = "Starting";

        /** The _time. */
        final long _time = System.currentTimeMillis();

        /** The _source mover. */
        final String _sourceMover;

        /** The _transfer. */
        volatile DataTransfer _transfer = null;

        /**
         * Instantiates a new replicate thread.
         *
         * @param sourceMover
         *            the source mover
         * @param transfer
         *            the transfer
         */
        private ReplicateThread(final String sourceMover, final DataTransfer transfer) {
            _sourceMover = sourceMover;
            _transfer = transfer;
        }

        /**
         * Return the data mover used for the retrieval.
         *
         * @return the source mover
         */
        public String getSourceMover() {
            return _sourceMover;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            var complete = false;
            final var key = _getKey(_transfer);
            try {
                final var start = System.currentTimeMillis();
                MasterServer._log.info("Starting replication for DataTransfer " + _transfer.getId());
                final var list = _master._getTransferServers(_transfer.getDataFile());
                _phase = "Processing";
                final var rr = TransferScheduler.replicate(_sourceMover, list, _transfer);
                _phase = "Finalizing";
                final var duration = System.currentTimeMillis() - start;
                final var base = _master.getDataBase();
                _transfer = base.getDataTransfer(_transfer.getId());
                _transfer.setReplicateTime(new Timestamp(System.currentTimeMillis()));
                _transfer.setReplicated(rr.complete);
                final var file = _transfer.getDataFile();
                if (!file.getDownloaded()) {
                    final var downloaded = rr.dataFile.getDownloaded();
                    MasterServer._log.debug("DataFile " + file.getId() + ", DataTransfer " + _transfer.getId()
                            + " downloaded: " + downloaded + " (" + _transfer.getStatusCode() + ")");
                    file.setDownloaded(downloaded);
                }
                base.update(_transfer);
                final var target = Format.toList(rr.transferServers);
                if (complete = rr.complete) {
                    // Add a new history to inform about the replication!
                    if (rr.transferServers.isEmpty()) {
                        _transfer.setComment("No replication required (only one copy stored)");
                    } else {
                        _transfer.setComment("Replication completed in " + Format.formatDuration(duration)
                                + " from DataMover=" + _sourceMover + " to " + target);
                    }
                    _master.addTransferHistory(_transfer);
                }
                final var destination = _transfer.getDestination();
                final var dataFile = _transfer.getDataFile();
                if (MasterServer._operationalEvents.isEnabled() && !(complete && target.isEmpty()))
                    MasterServer._operationalEvents.emit("CPY", field("Monitored", destination.getMonitor()),
                            field("TimeStamp", Timestamp.from(Instant.now())),
                            field("DataTransferId", _transfer.getId()), field("DestinationName", destination.getName()),
                            field("DestinationType", DestinationOption.getLabel(destination.getType())),
                            field("FileName", _transfer.getTarget()), field("FileSize", dataFile.getSize()),
                            field("ScheduledTime", _transfer.getScheduledTime()),
                            field("StartTime", new Timestamp(start)),
                            field("MetaStream", nullToNone(dataFile.getMetaStream())),
                            field("MetaType", nullToNone(dataFile.getMetaType())),
                            field("MetaTime", dataFile.getMetaTime()), field("TimeBase", dataFile.getTimeBase()),
                            field("TimeStep", dataFile.getTimeStep()), field("Duration", duration),
                            field("CountryCode", destination.getCountryIso()),
                            field("Target", (isEmpty(target) ? "-" : target.replaceAll("DataMover=", ""))),
                            field("TransferServer", _sourceMover), field("Caller", nullToNone(dataFile.getCaller())),
                            field("ExpiryTime", _transfer.getExpiryTime()),
                            field("FileSystem", dataFile.getFileSystem()), field("Status", complete),
                            field("Message", nullToNone(rr.message)), field("Action", "replicate"));
            } catch (final Throwable t) {
                MasterServer._log.warn("Running ReplicateThread " + key, t);
            } finally {
                _toRemove.add(key);
                MasterServer._log.info("DataTransfer " + _transfer.getId() + " replicated: " + complete);
            }
        }
    }
}
