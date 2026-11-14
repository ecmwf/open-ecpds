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
import ecmwf.common.database.DataFile;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class PurgeScheduler.
 */
public final class PurgeScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _time out purge thread. */
    private long _timeOutPurgeThread = Cnf.durationAt("Scheduler", "timeOutPurgeThread", -1);

    /** The _max purge threads. */
    private int _maxPurgeThreads = Cnf.at("Scheduler", "maxPurgeThreads", 50);

    /** The _purge threads. */
    final Map<Long, PurgeThread> _purgeThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new purge scheduler.
     *
     * @param name
     *            the name
     */
    public PurgeScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "purgeScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "purgeSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "purgeSchedulerTimeRanges"));
    }

    /**
     * Gets the purge threads count.
     *
     * @return the purge threads count
     */
    public int getPurgeThreadsCount() {
        return _purgeThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",purgeThreads="
                + _purgeThreads.size();
    }

    /**
     * Gets the key.
     *
     * @param file
     *            the file
     *
     * @return the string
     */
    private long _getKey(final DataFile file) {
        return file.getId();
    }

    /**
     * Gets the thread list.
     *
     * @return the thread list
     */
    public String getThreadList() {
        final var status = new StringBuilder();
        final var currentTime = System.currentTimeMillis();
        for (final PurgeThread thread : _purgeThreads.values()) {
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(thread._dataFile))) {
                status.append(("DataFile " + key + " (" + Format.formatDuration(thread._time, currentTime) + ")")
                        .replace(' ', '_').replace('-', '_')).append(" ");
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
                "The PurgeScheduler is used to manage the purge threads.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of purge thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("MaximumPurgeThreads", "java.lang.Integer",
                                "MaximumPurgeThreads: maximum number of parallel purges.", true, true, false),
                        new MBeanAttributeInfo("PurgeThreadsTimeout", "java.lang.Long",
                                "PurgeThreadsTimeout: milliseconds a purge thread may run before being interrupted (-1 disables this check).",
                                true, true, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active purge thread(s) shown as DataFile id and elapsed running time.",
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
                return _purgeThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumPurgeThreads".equals(attributeName)) {
                return _maxPurgeThreads;
            }
            if ("PurgeThreadsTimeout".equals(attributeName)) {
                return _timeOutPurgeThread;
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
        if ("MaximumPurgeThreads".equals(name)) {
            _maxPurgeThreads = (Integer) value;
            return true;
        }
        if ("PurgeThreadsTimeout".equals(name)) {
            _timeOutPurgeThread = (Long) value;
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
        try (var it = _master.getECpdsBase().getExpiredDataFilesIterator(2 * _maxPurgeThreads)) {
            while (isRunning() && it.hasNext()) {
                try {
                    final var dataFile = it.next();
                    if (dataFile.getDeleted() && dataFile.getRemoved()) {
                        MasterServer._log.warn("DataFile " + dataFile.getId() + " already purged");
                        continue;
                    }
                    if (getPurgeThreadsCount() >= _maxPurgeThreads) {
                        break;
                    }
                    try (final var _ = ecmwf.ecpds.master.storage.StorageFileLocks.read(dataFile.getId())) {
                        synchronized (_purgeThreads) {
                            if (!_purgeThreads.containsKey(_currentKey = _getKey(dataFile))) {
                                final var thread = new PurgeThread(dataFile);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _purgeThreads.put(_currentKey, thread);
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
                    _purgeThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired PurgeThreads
            if (_timeOutPurgeThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final PurgeThread thread : _purgeThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread._time > _timeOutPurgeThread) {
                        MasterServer._log.warn("PurgeThread " + _getKey(thread._dataFile) + " expired ("
                                + Format.formatDuration(thread._time, currentTime) + ")");
                        try {
                            thread.interrupt();
                        } catch (final Throwable t) {
                            MasterServer._log.warn("Interrupting PurgeThread", t);
                        }
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for purge",
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
     * The Class PurgeThread.
     */
    private final class PurgeThread extends ConfigurableRunnable {

        /** The _time. */
        private final long _time = System.currentTimeMillis();

        /** The _data file. */
        private final DataFile _dataFile;

        /**
         * Instantiates a new purge thread.
         *
         * @param dataFile
         *            the data file
         */
        private PurgeThread(final DataFile dataFile) {
            _dataFile = dataFile;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            final var start = System.currentTimeMillis();
            final var key = _getKey(_dataFile);
            var complete = false;
            try {
                MasterServer._log.info("Starting purge for DataFile " + _dataFile.getId());
                // Purge on all the active DataMovers for the TransferGroup
                // of the DataFile!
                complete = _master.purgeDataFile(_dataFile, null).complete;
            } catch (final Throwable t) {
                MasterServer._log.warn("Running PurgeThread " + key, t);
            } finally {
                _toRemove.add(key);
                MasterServer._log.info("DataFile " + _dataFile.getId() + " purged: " + complete + " ("
                        + Format.formatDuration(System.currentTimeMillis() - start) + ")");
            }
        }
    }
}
