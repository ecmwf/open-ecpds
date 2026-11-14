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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ECTRANS;
import static ecmwf.ecpds.master.OperationalEvents.field;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_PROXY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_ECTRANS_FILTERPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_ECTRANS_FILTER_MINIMUM_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTERPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTER_MINIMUM_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_USE_DESTINATION_FILTER;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.text.Util.nullToNone;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferScheduler;
import ecmwf.ecpds.master.transfer.TransferScheduler.BackupResult;

/**
 * The Class ProxyScheduler.
 */
public final class ProxyScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _time out proxy thread. */
    private long _timeOutProxyThread = Cnf.durationAt("Scheduler", "timeOutProxyThread", -1);

    /** The _max proxy threads. */
    private int _maxProxyThreads = Cnf.at("Scheduler", "maxProxyThreads", 15);

    /** The _debug. */
    public boolean _debug = Cnf.at("Scheduler", "debug", false);

    /** The _proxy threads. */
    final Map<Long, ProxyThread> _proxyThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new proxy scheduler.
     *
     * @param name
     *            the name
     */
    public ProxyScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "proxyScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "proxySchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "proxySchedulerTimeRanges"));
    }

    /**
     * Gets the proxy threads count.
     *
     * @return the proxy threads count
     */
    public int getProxyThreadsCount() {
        return _proxyThreads.size() - _toRemove.size();
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
        for (final ProxyThread thread : _proxyThreads.values()) {
            final Host currentHostForProxy;
            if (thread != null && (currentHostForProxy = thread.getHostForProxy()) != null
                    && !_toRemove.contains(_getKey(thread._transfer))
                    && hostForBackup.getName().equals(currentHostForProxy.getName())) {
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
        return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",proxyThreads="
                + _proxyThreads.size();
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
        for (final ProxyThread thread : _proxyThreads.values()) {
            final DataTransfer transfer;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                status.append((transfer.getDestinationName() + " DataFile " + key + " ("
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
                "The ProxyScheduler is used to manage the proxy threads.",
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("DebugEnabled", "java.lang.Boolean",
                        "DebugEnabled: logs why a DataTransfer was skipped (no thread available, no ProxyHost) at debug level.",
                        true, true, false),
                        new MBeanAttributeInfo("MaximumProxyThreads", "java.lang.Integer",
                                "MaximumProxyThreads: maximum number of parallel proxy transfers.", true, true, false),
                        new MBeanAttributeInfo("ProxyThreadsTimeout", "java.lang.Long",
                                "ProxyThreadsTimeout: milliseconds a proxy thread may run before being interrupted (-1 disables this check).",
                                true, true, false),
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of proxy thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active proxy thread(s) shown as destination, DataFile id and elapsed running time.",
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
            if ("DebugEnabled".equals(attributeName)) {
                return _debug;
            }
            if ("MaximumProxyThreads".equals(attributeName)) {
                return _maxProxyThreads;
            }
            if ("ProxyThreadsTimeout".equals(attributeName)) {
                return _timeOutProxyThread;
            }
            if ("ThreadCount".equals(attributeName)) {
                return _proxyThreads.size();
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
        if ("DebugEnabled".equals(name)) {
            _debug = (Boolean) value;
            return true;
        }
        if ("MaximumProxyThreads".equals(name)) {
            _maxProxyThreads = (Integer) value;
            return true;
        }
        if ("ProxyThreadsTimeout".equals(name)) {
            _timeOutProxyThread = (Long) value;
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Gets the hosts by destination id and type.
     *
     * @param destId
     *            the dest id
     * @param type
     *            the type
     *
     * @return the hosts by destination id and type
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private Collection<Host> getHostsByDestinationIdAndType(final String destId, final String type)
            throws DataBaseException {
        final List<Host> result = new ArrayList<>();
        for (final Host host : _master.getECpdsBase().getHostsByDestinationId(destId)) {
            if (host.getType().equals(type)) {
                result.add(host);
            }
        }
        return result;
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
        try (var it = _master.getECpdsBase().getDataTransfersToProxyIterator(2 * _maxProxyThreads)) {
            while (isRunning() && it.hasNext()) {
                try {
                    final var transfer = it.next();
                    if (transfer.getProxyHostName() != null) {
                        MasterServer._log.warn("DataTransfer " + transfer.getId() + " already proxy");
                        continue;
                    }
                    final var fromCache = _master.getDataTransferFromCache(transfer.getId());
                    if (fromCache != null) {
                        final var code = fromCache.getStatusCode();
                        if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                && !StatusFactory.HOLD.equals(code) && !StatusFactory.DONE.equals(code)) {
                            MasterServer._log.debug(
                                    "Delay Proxy for DataTransfer " + transfer.getId() + ": " + code + " (in cache)");
                            continue;
                        }
                    }
                    final var threadsCount = getProxyThreadsCount();
                    if (threadsCount >= _maxProxyThreads) {
                        if (_debug) {
                            MasterServer._log.debug("No ProxyThread available (current=" + threadsCount + ",max="
                                    + _maxProxyThreads + ")");
                        }
                        break;
                    }
                    final var hostsForProxy = getHostsByDestinationIdAndType(transfer.getDestinationName(),
                            HostOption.PROXY);
                    // Do we have at least one ProxyHost defined?
                    if (hostsForProxy.isEmpty()) {
                        if (_debug) {
                            MasterServer._log.debug("No ProxyHost associated with DataTransfer-" + transfer.getId());
                        }
                        continue;
                    }
                    try (final var _ = ecmwf.ecpds.master.storage.StorageFileLocks.read(transfer.getDataFileId())) {
                        synchronized (_proxyThreads) {
                            if (!_proxyThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new ProxyThread(hostsForProxy, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _proxyThreads.put(_currentKey, thread);
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
                    _proxyThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired ProxyThreads
            if (_timeOutProxyThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final ProxyThread thread : _proxyThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread._time > _timeOutProxyThread) {
                        MasterServer._log.warn("ProxyThread " + _getKey(thread._transfer) + " expired ("
                                + Format.formatDuration(thread._time, currentTime) + ")");
                        try {
                            thread.interrupt();
                        } catch (final Throwable t) {
                            MasterServer._log.warn("Interrupting ProxyThread", t);
                        }
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} DataFile(s) processed for proxy",
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
     * The Class ProxyThread.
     */
    final class ProxyThread extends ConfigurableRunnable {

        volatile String _phase = "Starting";

        /** The _time. */
        final long _time = System.currentTimeMillis();

        /** The _hosts for proxy. */
        private final Collection<Host> _hostsForProxy;

        /** The _host for proxy. */
        volatile Host _hostForProxy = null;

        /** The _transfer. */
        volatile DataTransfer _transfer = null;

        /**
         * Instantiates a new proxy thread.
         *
         * @param hostForProxy
         *            the host for proxy
         * @param transfer
         *            the transfer
         */
        private ProxyThread(final Collection<Host> hostForProxy, final DataTransfer transfer) {
            _transfer = transfer;
            _hostsForProxy = hostForProxy;
        }

        /**
         * Gets the host for proxy.
         *
         * @return the host for proxy
         */
        public Host getHostForProxy() {
            return _hostForProxy;
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
                MasterServer._log.info("Starting proxy for DataTransfer " + _transfer.getId());
                final var list = _master._getTransferServers(_transfer.getDataFile());
                final var destination = _transfer.getDestination();
                BackupResult rr = null;
                // TODO: find a better way of checking the maximum number of
                // proxy hosts - there is a synchronisation issue here!
                for (final Host hostForProxy : _hostsForProxy) {
                    final var threadCound = getThreadCount(hostForProxy);
                    final var maxConnections = hostForProxy.getMaxConnections();
                    if (threadCound >= maxConnections) {
                        if (_debug) {
                            MasterServer._log.debug("No session available for ProxyHost " + hostForProxy.getNickname()
                                    + " (count=" + threadCound + ",max=" + maxConnections + ")");
                        }
                        continue;
                    }
                    // Are we requested to use the filter options defined at the destination level?
                    final var proxySetup = HOST_PROXY.getECtransSetup(hostForProxy.getData());
                    if (proxySetup.getBoolean(HOST_PROXY_USE_DESTINATION_FILTER)) {
                        MasterServer._log.debug(
                                "Using Destination options for replicating DataTransfer-{} to ProxyHost {}",
                                _transfer.getId(), hostForProxy.getNickname());
                        hostForProxy.setFilterName(destination.getFilterName());
                        final var setup = DESTINATION_ECTRANS.getECtransSetup(destination.getData());
                        setup.getOptionalString(DESTINATION_ECTRANS_FILTERPATTERN)
                                .ifPresent(filterPattern -> proxySetup.set(HOST_ECTRANS_FILTERPATTERN, filterPattern));
                        setup.getOptionalByteSize(DESTINATION_ECTRANS_FILTER_MINIMUM_SIZE)
                                .ifPresent(byteSize -> proxySetup.set(HOST_ECTRANS_FILTER_MINIMUM_SIZE, byteSize));
                        hostForProxy.setData(proxySetup.getData());
                    }
                    _phase = "Processing";
                    if ((rr = TransferScheduler.backup(_hostForProxy = hostForProxy, list, _transfer)).complete) {
                        break;
                    }
                }
                // Did we find any HostForProxy available for the
                // transmission? (good or bad)
                final var duration = System.currentTimeMillis() - start;
                _phase = "Finalizing";
                if (rr == null) {
                    // Maybe next time!
                    if (_debug) {
                        MasterServer._log.debug("No ProxyHost found for DataTransfer-" + _transfer.getId());
                    }
                    return;
                }
                final var base = _master.getDataBase();
                // Make sure we have the latest transfer update!
                final var fromCache = _master.getDataTransferFromCache(_transfer.getId());
                _transfer = fromCache != null ? fromCache : base.getDataTransfer(_transfer.getId());
                _transfer.setProxyTime(new Timestamp(System.currentTimeMillis()));
                if (rr.hostForBackup != null) {
                    // Do we have a host for backup set?
                    _transfer.setProxyHostName(rr.hostForBackup.getName());
                    _transfer.setProxyHost(rr.hostForBackup);
                }
                // Always persist HOS_NAME_PROXY and DAT_PROXY_TIME to DB.
                // When fromCache != null, _transfer IS the theTransferRepository object.
                // Only updating the in-memory object is not enough: the SQL queries
                // getDestinationExts and getPendingDataTransfers test HOS_NAME_PROXY IS
                // NOT NULL against the DB. If the write is skipped here, the
                // DestinationThread will never see the transfer as ready and it will
                // remain stuck in WAIT indefinitely for force-proxy destinations.
                base.update(_transfer);
                complete = rr.complete;
                if (complete) {
                    // Add a new history to inform about the successful
                    // transmissions to the Proxy!
                    _transfer.setComment("Replication completed in " + Format.formatDuration(duration) + " from "
                            + rr.transferServer + " to Proxy Host=" + rr.hostForBackup.getName() + " ("
                            + rr.hostForBackup.getNickname() + ")");
                    _master.addTransferHistory(_transfer);
                } else if (isNotEmpty(rr.message)) {
                    // Add a new history to inform about the failure!
                    _master.addTransferHistory(_transfer, StatusFactory.STOP, rr.message);
                }
                final var dataFile = _transfer.getDataFile();
                if (MasterServer._operationalEvents.isEnabled())
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
                            field("Target", (rr.hostForBackup == null ? "-" : rr.hostForBackup.getNickname())),
                            field("TransferServer",
                                    (isEmpty(rr.transferServer) ? "-"
                                            : rr.transferServer.replaceAll("DataMover=", ""))),
                            field("Caller", nullToNone(dataFile.getCaller())),
                            field("ExpiryTime", _transfer.getExpiryTime()),
                            field("FileSystem", dataFile.getFileSystem()), field("Status", complete),
                            field("Message", nullToNone(rr.message)), field("Action", "proxy"));
            } catch (final Throwable t) {
                MasterServer._log.warn("Running ProxyThread " + key, t);
            } finally {
                _toRemove.add(key);
                MasterServer._log
                        .info("DataTransfer " + _transfer.getId() + " proxy: " + complete + " (" + _transfer + ")");
            }
        }
    }
}
