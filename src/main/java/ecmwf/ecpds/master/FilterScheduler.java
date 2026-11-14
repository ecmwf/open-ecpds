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
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class FilterScheduler.
 */
public final class FilterScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _time out filter thread. */
    private long _timeOutFilterThread = Cnf.durationAt("Scheduler", "timeOutFilterThread", -1);

    /** The _max filter threads. */
    private int _maxFilterThreads = Cnf.at("Scheduler", "maxFilterThreads", 15);

    /** The _filter threads. */
    final Map<Long, FilterThread> _filterThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new filter scheduler.
     *
     * @param name
     *            the name
     */
    public FilterScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "filterScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "filterSchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "filterSchedulerTimeRanges"));
    }

    /**
     * Gets the filter threads count.
     *
     * @return the filter threads count
     */
    public int getFilterThreadsCount() {
        return _filterThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",filterThreads="
                + _filterThreads.size();
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
        for (final FilterThread thread : _filterThreads.values()) {
            final DataFile file;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(file = thread._file))) {
                status.append(("DataFile " + key + "=" + file.getFilterName() + " ("
                        + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-', '_'))
                        .append(" ");
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
                "The FilterScheduler is used to manage the filter threads.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of filter thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("MaximumFilterThreads", "java.lang.Integer",
                                "MaximumFilterThreads: maximum number of parallel filters.", true, true, false),
                        new MBeanAttributeInfo("FilterThreadsTimeout", "java.lang.Long",
                                "FilterThreadsTimeout: milliseconds a filter thread may run before being interrupted (-1 disables this check).",
                                true, true, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active filter thread(s) shown as DataFile id, filter name and elapsed running time.",
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
                return _filterThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumFilterThreads".equals(attributeName)) {
                return _maxFilterThreads;
            }
            if ("FilterThreadsTimeout".equals(attributeName)) {
                return _timeOutFilterThread;
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
        if ("MaximumFilterThreads".equals(name)) {
            _maxFilterThreads = (Integer) value;
            return true;
        }
        if ("FilterThreadsTimeout".equals(name)) {
            _timeOutFilterThread = (Long) value;
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
                final var thread = _filterThreads.remove(key);
                MasterServer._log.debug("FilterThread " + key + " removed (" + (thread != null) + ")");
            }
            _toRemove.clear();
        }
        if (getFilterThreadsCount() >= _maxFilterThreads) {
            return NEXT_STEP_DELAY;
        }
        try (var it = _master.getECpdsBase().getDataFilesToFilterIterator(2 * _maxFilterThreads)) {
            while (isRunning() && it.hasNext()) {
                // Check capacity before fetching the next row from the DB cursor so we stop
                // consuming the result set the moment the pool is full.
                if (getFilterThreadsCount() >= _maxFilterThreads) {
                    break;
                }
                try {
                    final var file = it.next();
                    if (file == null) {
                        continue;
                    }
                    try (final var _ = ecmwf.ecpds.master.storage.StorageFileLocks.read(file.getId())) {
                        synchronized (_filterThreads) {
                            if (!_filterThreads.containsKey(_currentKey = _getKey(file))) {
                                final var thread = new FilterThread(file);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + file.getId(), null);
                                thread.execute();
                                _filterThreads.put(_currentKey, thread);
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
                    _filterThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired FilterThreads
            if (_timeOutFilterThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final FilterThread thread : _filterThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread._time > _timeOutFilterThread) {
                        MasterServer._log.warn("EventThread Publication-{} expired({})", _getKey(thread._file),
                                Format.formatDuration(thread._time, currentTime));
                        thread.interrupt();
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for filtering",
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
     * The Class FilterThread.
     */
    private final class FilterThread extends ConfigurableRunnable {

        /** The _time. */
        private final long _time = System.currentTimeMillis();

        /** The _file. */
        private DataFile _file;

        /**
         * Instantiates a new filter thread.
         *
         * @param file
         *            the file
         */
        private FilterThread(final DataFile file) {
            _file = file;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            var complete = false;
            final var filter = _file.getFilterName();
            final var key = _getKey(_file);
            try {
                final var start = System.currentTimeMillis();
                final var base = _master.getDataBase(ECpdsBase.class);
                final var fr = TransferScheduler.filter(_master._getTransferServers(_file), _file, false);
                _file = base.getDataFile(_file.getId());
                _file.setFilterTime(new Timestamp(System.currentTimeMillis()));
                _file.setFilterName(fr.complete ? filter : null);
                _file.setFilterSize(fr.complete ? fr.dataFile.getFilterSize() : -1);
                final var checksum = fr.dataFile.getChecksum();
                if (checksum != null) {
                    MasterServer._log.debug("Setting Checksum to: " + checksum);
                    _file.setChecksum(checksum);
                }
                base.update(_file);
                if (complete = fr.complete && !fr.transferServers.isEmpty()) {
                    // Add a new history for every DataTransfer related to
                    // this DataFile to inform about the pre-compression!
                    final var current = System.currentTimeMillis();
                    for (final DataTransfer transfer : base.getDataTransfersByDataFileId(_master.dataCache,
                            _file.getId())) {
                        transfer.setComment("Preprocessed with " + filter + " in "
                                + Format.formatDuration(current - start) + " on " + Format.toList(fr.transferServers)
                                + " (" + Format.formatSize(_file.getFilterSize()) + ")");
                        _master.addTransferHistory(transfer);
                    }
                }
            } catch (final Throwable t) {
                MasterServer._log.warn("Running FilterThread " + key, t);
                complete = false;
            } finally {
                _toRemove.add(key);
                MasterServer._log.info("DataFile " + key + " filtered: " + complete + " (" + filter + ")");
            }
        }
    }
}
