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

import static ecmwf.common.text.Util.isNotEmpty;
import java.sql.Timestamp;
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
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class BackupScheduler.
 */
public final class BackupScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _time out backup thread. */
    private long _timeOutBackupThread = Cnf.durationAt("Scheduler", "timeOutBackupThread", -1);

    /** The _max backup threads. */
    private final int _maxBackupThreads = Cnf.at("Scheduler", "maxBackupThreads", 15);

    /** The _backup threads. */
    final Map<Long, BackupThread> _backupThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new backup scheduler.
     *
     * @param name
     *            the name
     */
    public BackupScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "backupScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "backupSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "backupSchedulerTimeRanges"));
    }

    /**
     * Gets the backup threads count.
     *
     * @return the backup threads count
     */
    public int getBackupThreadsCount() {
        return _backupThreads.size() - _toRemove.size();
    }

    /**
     * Return the number of threads with the specified host for source name.
     *
     * @param hostForBackup
     *            the host for backup
     *
     * @return the thread count
     */
    public int getThreadCount(final Host hostForBackup) {
        var count = 0;
        for (final BackupThread thread : _backupThreads.values()) {
            if (thread != null && !_toRemove.contains(_getKey(thread._transfer))
                    && hostForBackup.getName().equals(thread.getHostForBackup().getName())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",backupThreads="
                + _backupThreads.size();
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
        for (final BackupThread thread : _backupThreads.values()) {
            final DataTransfer transfer;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                status.append((transfer.getDestinationName() + " DataFile " + key + " ("
                        + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-', '_')
                        + " ");
            }
        }
        return status.toString().trim();
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The BackupScheduler is used to manage the backup threads.",
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("BackupThreadsTimeout", "java.lang.Long",
                        "BackupThreadsTimeout: milliseconds a backup thread may run before being interrupted (-1 disables this check).",
                        true, true, false),
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of backup thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active backup thread(s) shown as destination, DataFile id and elapsed running time.",
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
            if ("BackupThreadsTimeout".equals(attributeName)) {
                return _timeOutBackupThread;
            }
            if ("ThreadCount".equals(attributeName)) {
                return _backupThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
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
        if ("BackupThreadsTimeout".equals(name)) {
            _timeOutBackupThread = (Long) value;
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
                final var thread = _backupThreads.remove(key);
                MasterServer._log.debug("BackupThread " + key + " removed (" + (thread != null) + ")");
            }
            _toRemove.clear();
        }
        if (getBackupThreadsCount() >= _maxBackupThreads) {
            return NEXT_STEP_DELAY;
        }
        try (var it = _master.getECpdsBase().getDataTransfersToBackupIterator(2 * _maxBackupThreads)) {
            while (isRunning() && it.hasNext()) {
                // Check capacity before fetching the next row from the DB cursor so we stop
                // consuming the result set the moment the pool is full.
                if (getBackupThreadsCount() >= _maxBackupThreads) {
                    break;
                }
                try {
                    final var transfer = it.next();
                    if (transfer.getBackupHostName() != null) {
                        MasterServer._log.warn("DataTransfer " + transfer.getId() + " already backup");
                        continue;
                    }
                    final var fromCache = _master.getDataTransferFromCache(transfer.getId());
                    if (fromCache != null) {
                        final var code = fromCache.getStatusCode();
                        if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                && !StatusFactory.HOLD.equals(code) && !StatusFactory.DONE.equals(code)) {
                            MasterServer._log.debug(
                                    "Delay Backup for DataTransfer " + transfer.getId() + ": " + code + " (in cache)");
                            continue;
                        }
                    }
                    final var hostForBackup = transfer.getDataFile().getTransferGroup().getHostForBackup();
                    if (getThreadCount(hostForBackup) >= hostForBackup.getMaxConnections()) {
                        continue;
                    }
                    try (final var _ = ecmwf.ecpds.master.storage.StorageFileLocks.read(transfer.getDataFileId())) {
                        synchronized (_backupThreads) {
                            if (!_backupThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new BackupThread(hostForBackup, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _backupThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    }
                } catch (final Throwable t) {
                    MasterServer._log.warn("nextStep", t);
                }
            }
            synchronized (_toRemove) {
                for (final long key : _toRemove) {
                    _backupThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired BackupThreads
            if (_timeOutBackupThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final BackupThread thread : _backupThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread._time > _timeOutBackupThread) {
                        MasterServer._log.warn("BackupThread " + _getKey(thread._transfer) + " expired ("
                                + Format.formatDuration(thread._time, currentTime) + ")");
                        try {
                            thread.interrupt();
                        } catch (final Throwable t) {
                            MasterServer._log.warn("Interrupting BackupThread", t);
                        }
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for backup",
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
     * The Class BackupThread.
     */
    final class BackupThread extends ConfigurableRunnable {

        volatile String _phase = "Starting";

        /** The _time. */
        final long _time = System.currentTimeMillis();

        /** The _host for backup. */
        final Host _hostForBackup;

        /** The _transfer. */
        volatile DataTransfer _transfer = null;

        /**
         * Instantiates a new backup thread.
         *
         * @param hostForBackup
         *            the host for backup
         * @param transfer
         *            the transfer
         */
        private BackupThread(final Host hostForBackup, final DataTransfer transfer) {
            _transfer = transfer;
            _hostForBackup = hostForBackup;
        }

        /**
         * Gets the host for backup.
         *
         * @return the host for backup
         */
        public Host getHostForBackup() {
            return _hostForBackup;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            final var start = System.currentTimeMillis();
            var complete = false;
            final var key = _getKey(_transfer);
            try {
                MasterServer._log.info("Starting backup for DataTransfer " + _transfer.getId());
                final var list = _master._getTransferServers(_transfer.getDataFile());
                _phase = "Processing";
                final var rr = TransferScheduler.backup(_hostForBackup, list, _transfer);
                _phase = "Finalizing";
                final var base = _master.getDataBase(ECpdsBase.class);
                _transfer = base.getDataTransfer(_transfer.getId());
                _transfer.setBackupTime(new Timestamp(System.currentTimeMillis()));
                if (rr.hostForBackup != null) {
                    _transfer.setBackupHostName(rr.hostForBackup.getName());
                    _transfer.setBackupHost(rr.hostForBackup);
                } else {
                    // It failed to complete!
                    _transfer.setBackupHostName(null);
                    _transfer.setBackupHost(null);
                }
                base.update(_transfer);
                complete = rr.complete;
                if (complete) {
                    // Also update all the DataTransfers with the same
                    // DataFile (this happen with Destinations with
                    // aliases)!
                    for (final DataTransfer transfer : base.getDataTransfersByDataFileId(_master.dataCache,
                            _transfer.getDataFileId())) {
                        transfer.setBackupTime(_transfer.getBackupTime());
                        transfer.setBackupHostName(_transfer.getBackupHostName());
                        transfer.setBackupHost(_transfer.getBackupHost());
                        base.update(transfer);
                        // Add a new history to inform about the backup!
                        transfer.setComment(
                                "Backup completed in " + Format.formatDuration(System.currentTimeMillis() - start)
                                        + " from " + rr.transferServer + " to Host=" + rr.hostForBackup.getName() + " ("
                                        + rr.hostForBackup.getNickname() + ")");
                        _master.addTransferHistory(transfer, transfer.getBackupHost(), transfer.getStatusCode(),
                                transfer.getComment(), false);
                    }
                } else if (isNotEmpty(rr.message)) {
                    // Add a new history to inform about the failure!
                    for (final DataTransfer transfer : base.getDataTransfersByDataFileId(_master.dataCache,
                            _transfer.getDataFileId())) {
                        _master.addTransferHistory(transfer, _transfer.getBackupHost(), StatusFactory.STOP, rr.message,
                                true);
                    }
                }
            } catch (final Throwable t) {
                MasterServer._log.warn("Running BackupThread " + key, t);
            } finally {
                _toRemove.add(key);
                MasterServer._log.info("DataTransfer " + _transfer.getId() + " backup: " + complete);
            }
        }
    }
}
