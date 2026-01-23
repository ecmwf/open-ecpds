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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECPDS;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_ACTIVE_TIME_RANGE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_REQUEUEIGNORE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_REQUEUEON;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_REQUEUEPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECPDS_MOVER_LIST_FOR_BACKUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECPDS_MOVER_LIST_FOR_SOURCE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEBUG;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUE_ON_FAILURE;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.text.Util.nullToNone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.callback.LocalInputStream;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.DestinationExt;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.database.SchedulerValue;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.ECaccessServer;
import ecmwf.common.ecaccess.MBeanRepository;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.opsview.OpsViewManager;
import ecmwf.common.technical.CloseableIterator;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.technical.WakeupThread;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.ChangeHostEvent;
import ecmwf.ecpds.master.DestinationStep;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterServer;
import ecmwf.ecpds.master.StatusUpdate;
import ecmwf.ecpds.master.transfer.TransferServerProvider.TransferServerException;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.mover.SourceNotAvailableException;

/**
 * The Class TransferScheduler.
 */
public final class TransferScheduler extends MBeanScheduler {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TransferScheduler.class);

    /** The Constant _ops. */
    private static final Logger _ops = LogManager.getLogger("OperatorLogs");

    /** The Constant _metrics. */
    private static final Logger _metrics = LogManager.getLogger("MetricsLogs");

    /** The Constant _splunk. */
    private static final Logger _splunk = LogManager.getLogger("SplunkLogs");

    /** The Constant _dataBase. */
    private static final ECpdsBase BASE = StarterServer.getInstance(ECaccessServer.class).getDataBase(ECpdsBase.class);

    /** The Constant _master. */
    private static final MasterServer MASTER = StarterServer.getInstance(MasterServer.class);

    /** The _threads. */
    private final Map<String, DestinationThread> _threads = new ConcurrentHashMap<>();

    /** The _pendings. */
    private final Map<String, Integer> _pendings = new ConcurrentHashMap<>();

    /** The _pool size. */
    private int _poolSize = Cnf.at("TransferScheduler", "poolSize", 400);

    /** The _queue size. */
    private final int _queueSize = Cnf.at("TransferScheduler", "queueSize", 20);

    /** The _last destination. */
    private DestinationThread _lastDestination = null;

    /** The _monitoring thread. */
    private MonitoringThread _monitoringThread = null;

    /** The _debug. */
    private boolean _debug = false;

    /** The _notified. */
    private boolean _notified = false;

    /**
     * Instantiates a new transfer scheduler.
     *
     * @param name
     *            the name
     */
    public TransferScheduler(final String name) {
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "transferScheduler", 2 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "transferSchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "transferSchedulerTimeRanges"));
    }

    /**
     * Notify completion.
     *
     * @param transfer
     *            the transfer
     */
    public void notifyCompletion(final DataTransfer transfer) {
        final var destination = transfer.getDestination();
        final var thread = _threads.get(destination.getName());
        if (thread == null) {
            _log.warn("DestinationThread {} not found (completion notification ignored)", destination.getName());
        } else {
            thread.updateCount(transfer, false);
            thread.wakeup();
        }
    }

    /**
     * Gets the pool size.
     *
     * @return the pool size
     */
    public int getPoolSize() {
        return _poolSize;
    }

    /**
     * Sets the pool size.
     *
     * @param poolSize
     *            the new pool size
     */
    public void setPoolSize(final int poolSize) {
        _poolSize = poolSize;
    }

    /**
     * Notify requeue.
     */
    public void notifyRequeue(final DataTransfer transfer) {
        final var thread = _threads.get(transfer.getDestination().getName());
        if (thread != null)
            thread.removeValue(transfer);
        _notified = true;
        wakeup();
    }

    /**
     * {@inheritDoc}
     *
     * Initialize.
     */
    @Override
    public void initialize() {
        for (final Destination destination : BASE.getDestinationArray()) {
            final var code = destination.getStatusCode();
            if (!StatusFactory.FAIL.equals(code) && !StatusFactory.STOP.equals(code)) {
                destination.setUserStatus(null);
            }
            if (destination.getSchedulerValue() == null) {
                _log.warn("Creating new SchedulerValue for Destination " + destination.getName());
                final var value = new SchedulerValue();
                value.setHasRequeued(false);
                value.setStartCount(0);
                try {
                    BASE.insert(value, true);
                } catch (final DataBaseException e) {
                    _log.warn(e);
                }
                destination.setSchedulerValueId(value.getId());
                destination.setSchedulerValue(value);
            }
            BASE.tryUpdate(destination);
        }
        DataTransfer[] transfers;
        try {
            transfers = BASE.getInterruptedTransfers();
        } catch (final DataBaseException e) {
            _log.warn("Getting interrupted transfers", e);
            transfers = new DataTransfer[0];
        }
        for (final DataTransfer transfer : transfers) {
            final var status = transfer.getStatusCode();
            if (StatusFactory.INIT.equals(status)) {
                _log.info("Transfer " + transfer.getId() + " aborted (" + status + ")");
                transfer.setComment("Interrupted by Master Server shutdown while arriving");
                MASTER.addTransferHistory(transfer, StatusFactory.INTR, transfer.getComment());
                transfer.setStatusCode(StatusFactory.INTR);
                transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                transfer.setDeleted(true);
            } else {
                if (StatusFactory.FETC.equals(status)) {
                    _log.info("Transfer " + transfer.getId() + " rescheduled (" + status + ")");
                    MASTER.addTransferHistory(transfer, StatusFactory.INTR,
                            "Interrupted by Master Server shutdown during retrieval");
                    transfer.setComment("Rescheduled by scheduler after Master Server restart");
                    transfer.setStatusCode(StatusFactory.SCHE);
                    transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                    transfer.setUserStatus(null);
                    // Make sure the DataFile is not already set as downloaded
                    // otherwise this transfer will not be picked-up by the download
                    // scheduler!
                    final var dataFile = transfer.getDataFile();
                    if (dataFile.getDownloaded()) {
                        dataFile.setDownloaded(false);
                        BASE.tryUpdate(dataFile);
                    }
                } else { // This is EXEC or INTR!
                    _log.info("Transfer " + transfer.getId() + " requeued (" + status + ")");
                    if (StatusFactory.EXEC.equals(status)) {
                        MASTER.addTransferHistory(transfer, StatusFactory.INTR,
                                "Interrupted by server shutdown during transmission");
                    }
                    transfer.setComment("Requeued by the scheduler after a Master Server restart");
                    transfer.setStatusCode(StatusFactory.RETR);
                    transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                    transfer.setUserStatus(null);
                }
                MASTER.addTransferHistory(transfer);
            }
            _updateDataTransfer(transfer);
        }
        if (MonitorManager.isActivated()) {
            _monitoringThread = new MonitoringThread();
            _monitoringThread.execute();
        }
        try {
            Thread.sleep(15 * Timer.ONE_SECOND);
        } catch (final InterruptedException e) {
        }
    }

    /**
     * Gets the destination thread.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination thread
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public DestinationThread getDestinationThread(final String destinationName) throws MasterException {
        final var thread = _threads.get(destinationName);
        if (thread == null) {
            throw new MasterException("Destination " + destinationName + " is off line");
        }
        return thread;
    }

    /**
     * Gets the destinations.
     *
     * @return the destinations
     */
    public Destination[] getDestinations() {
        final var destinations = BASE.getDestinationArray();
        for (final Destination destination : destinations) {
            final var thread = _threads.get(destination.getName());
            if (thread != null) {
                final var value = thread.getSchedulerValue();
                final var host = thread.getHost();
                if (value != null && host != null) {
                    value.setHost(host);
                    value.setHostName(host.getName());
                    destination.setSchedulerValue(value);
                }
            }
        }
        return destinations;
    }

    /**
     * Gets the destination last transfer.
     *
     * @param destinationName
     *            the destination name
     * @param ok
     *            the ok
     *
     * @return the destination last transfer
     */
    public DataTransfer getDestinationLastTransfer(final String destinationName, final boolean ok) {
        final var thread = _threads.get(destinationName);
        if (thread != null) {
            return ok ? thread.getLastTransfer() : thread.getLastFailedTransfer();
        }
        try {
            final Destination destination;
            final SchedulerValue value;
            final Long id;
            if ((destination = BASE.getDestination(destinationName)) != null
                    && (value = destination.getSchedulerValue()) != null
                    && (id = ok ? value.getLastTransferOk() : value.getLastTransferKo()) != null) {
                return MASTER.getDataTransfer(id);
            }
        } catch (final Exception e) {
            _log.warn(e);
        }
        return null;
    }

    /**
     * _update data transfer.
     *
     * @param transfer
     *            the transfer
     */
    private void _updateDataTransfer(final DataTransfer transfer) {
        final var comment = transfer.getComment();
        _log.debug("Update DataTransfer " + transfer.getId() + "=" + transfer.getStatusCode()
                + (isNotEmpty(comment) ? " (" + comment + ")" : ""));
        BASE.tryUpdate(transfer);
    }

    /**
     * Update host.
     *
     * @param host
     *            the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public void updateHost(final Host host) throws DataBaseException {
        if (HostOption.DISSEMINATION.equals(host.getType())) {
            _log.debug("Host " + host.getName() + " modification detected");
            for (final Destination destination : BASE.getDestinationsByHostName(host.getName())) {
                _log.debug("Destination " + destination.getName() + " to be restarted");
                destination.setUpdate(new Timestamp(System.currentTimeMillis()));
                BASE.tryUpdate(destination);
            }
        } else {
            _log.debug("Host " + host.getName()
                    + " modification detected (Destination not restarted - not a Dissemination Host)");
        }
    }

    /**
     * Gets the pending data transfers count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the pending data transfers count
     */
    public int getPendingDataTransfersCount(final String destinationName) {
        final var count = _pendings.get(destinationName);
        return count != null ? count : 0;
    }

    /**
     * Hold destination.
     *
     * @param userName
     *            the user name
     * @param destinationName
     *            the destination name
     * @param status
     *            the status
     * @param graceful
     *            the graceful
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public void holdDestination(final String userName, final String destinationName, final String status,
            final boolean graceful) throws DataBaseException {
        final var destination = BASE.getDestination(destinationName);
        _log.debug("Status update request from " + userName + " for Destination " + destinationName + " ("
                + StatusFactory.getDestinationStatusName(destination.getStatusCode()) + " -> "
                + StatusFactory.getDestinationStatusName(status) + ")");
        destination.setStatusCode(status);
        destination.setUserStatus(userName);
        BASE.update(destination);
        final var thread = _threads.get(destinationName);
        if (thread != null) {
            thread._destination = destination;
            thread.shutdown(graceful ? Timer.ONE_HOUR : 0, false, userName);
        }
    }

    /**
     * Hold all destinations.
     *
     * @param userName
     *            the user name
     * @param status
     *            the status
     * @param graceful
     *            the graceful
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public void holdAllDestinations(final String userName, final String status, final boolean graceful)
            throws DataBaseException {
        for (final Destination destination : BASE.getDestinationArray()) {
            holdDestination(userName, destination.getName(), status, graceful);
        }
    }

    /**
     * Gets the destination status.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination status
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public String getDestinationStatus(final String destinationName) throws DataBaseException {
        return getDestinationStatus(destinationName, null);
    }

    /**
     * Gets the destination status.
     *
     * @param destinationName
     *            the destination name
     * @param statusCode
     *            the Status Code
     *
     * @return the destination status
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public String getDestinationStatus(final String destinationName, String statusCode) throws DataBaseException {
        final var thread = _threads.get(destinationName);
        if (thread != null) {
            return thread.getDestinationStatusCode();
        }
        statusCode = statusCode != null ? statusCode : BASE.getDestination(destinationName).getStatusCode();
        if (StatusFactory.WAIT.equals(statusCode)) {
            return getPendingDataTransfersCount(destinationName) > 0 ? StatusFactory.SCHE : StatusFactory.WAIT;
        }
        return statusCode;
    }

    /**
     * Restart destination.
     *
     * @param userName
     *            the user name
     * @param comment
     *            the comment
     * @param destinationName
     *            the destination name
     * @param graceful
     *            the graceful
     * @param reset
     *            the reset
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public void restartDestination(final String userName, final String comment, final String destinationName,
            final boolean graceful, final boolean reset) throws DataBaseException {
        final var destination = BASE.getDestination(destinationName);
        _log.debug("Restart requested" + (isNotEmpty(userName) ? " from " + userName : "") + " for Destination "
                + destinationName + " (" + StatusFactory.getDestinationStatusName(destination.getStatusCode()) + " -> "
                + StatusFactory.getDestinationStatusName(StatusFactory.WAIT) + ")");
        destination.setStatusCode(StatusFactory.WAIT);
        destination.setUserStatus(userName);
        BASE.update(destination);
        final var thread = _threads.get(destinationName);
        if (thread != null) {
            thread._destination = destination;
            thread.shutdown(graceful ? Timer.ONE_HOUR : 0, reset, comment);
        }
    }

    /**
     * Restart all destinations.
     *
     * @param userName
     *            the user name
     * @param graceful
     *            the graceful
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public void restartAllDestinations(final String userName, final boolean graceful) throws DataBaseException {
        for (final Destination destination : BASE.getDestinationArray()) {
            restartDestination(userName, userName, destination.getName(), graceful, true);
        }
    }

    /**
     * Gets the monitoring thread.
     *
     * @return the monitoring thread
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public MonitoringThread getMonitoringThread() throws MasterException {
        if (_monitoringThread == null) {
            throw new MasterException("MonitoringThread is off line");
        }
        return _monitoringThread;
    }

    /**
     * {@inheritDoc}
     *
     * Next step.
     */
    @Override
    public int nextStep() {
        _notified = false;
        final List<String> remainingDestination = new ArrayList<>(_pendings.keySet());
        final var referenceTime = System.currentTimeMillis() + 2 * Timer.ONE_MINUTE;
        for (final DestinationExt destination : BASE.getDestinationExts()) {
            if (!isRunning()) {
                break;
            }
            final var code = destination.getStatusCode();
            final var name = destination.getName();
            remainingDestination.remove(name);
            _pendings.put(name, destination.getPendingTransferCount());
            if (!StatusFactory.EXEC.equals(code) && !StatusFactory.WAIT.equals(code)) {
                continue;
            }
            var thread = _threads.get(name);
            if (_debug) {
                _log.debug("Destination " + destination.getName() + " selected (" + Format.formatTime(referenceTime)
                        + " > " + Format.formatTime(destination.getMinQueueTime().getTime()) + "): thread "
                        + (thread == null ? "NOT " : "") + "found");
            }
            if (thread == null && referenceTime > destination.getMinQueueTime().getTime()) {
                if (_poolSize > 0 && _threads.size() >= _poolSize) {
                    // We have too many Destinations processed in parallel!
                    _log.warn("Maximum number of concurrent Destinations reached (poolSize=" + _poolSize
                            + "): Destination " + name + " delayed");
                } else {
                    // We can now start a new Destination thread!
                    if (_debug) {
                        _log.debug("Destination " + name + " to be started");
                    }
                    try {
                        _threads.put(name, thread = new DestinationThread(name));
                    } catch (final DataBaseException e) {
                        _log.warn(e);
                    } catch (final NoHostException e) {
                        _log.warn(e);
                        try {
                            holdDestination(null, name, StatusFactory.FAIL, false);
                        } catch (final DataBaseException d) {
                            _log.debug(d);
                        }
                    }
                }
            }
            if (thread != null) {
                if (_debug) {
                    _log.debug("Destination " + name + " loaded");
                }
                _lastDestination = thread;
            }
            if (thread != null && thread.isRunning() && !thread.isOnHold()) {
                final var value = destination.getSchedulerValue();
                if (value != null && value.getResetTime() != null) {
                    final var frequency = destination.getResetFrequency();
                    final var time = value.getResetTime().getTime();
                    final var current = System.currentTimeMillis();
                    if (frequency > 0 && time + frequency < current) {
                        final var reseted = thread.toBeReseted();
                        _log.info("Destination " + name + " reseted: " + reseted);
                        if (reseted) {
                            continue;
                        }
                    }
                }
                if (destination.getStopIfDirty() && destination.getUpdate().after(thread.getStartDate())) {
                    if (_debug) {
                        _log.debug("Destination " + name + " shutdown");
                    }
                    thread.shutdown(Timer.ONE_HOUR, true, "configuration update detected");
                } else if (destination.getPendingTransferCount() > 0 && thread.isInitialized()) {
                    thread.loadPendingDataTransfers();
                    thread.wakeup();
                }
            }
        }
        for (final String destination : remainingDestination) {
            _pendings.put(destination, 0);
        }
        return _notified ? NEXT_STEP_CONTINUE : NEXT_STEP_DELAY;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the activity.
     */
    @Override
    public String getActivity() {
        return _lastDestination != null ? "lastDestination=" + _lastDestination.getDestination().getName() + "("
                + _lastDestination.getPerformed() + "/" + _lastDestination.getSelected() + ")" + ",destinationPool="
                + _threads.size() : super.getActivity();
    }

    /**
     * Get the name of the TransferServer which was used to retrieve the file from the source host.
     *
     * @param transfer
     *            the transfer
     *
     * @return the transfer server used for retrieval
     */
    public static String getTransferServerUsedForRetrieval(final DataTransfer transfer) {
        final var dataFile = transfer.getDataFile();
        String getHost;
        if (dataFile == null || (getHost = dataFile.getGetHost()) == null) {
            _log.warn("GetHost from DataFile not available for DataTransfer: " + transfer.getId());
            getHost = transfer.getOriginalTransferServerName();
        }
        return getHost;
    }

    /**
     * Randomly select a DataMover from a list in the format ({operator} TransferGroup) mover1,mover2 ... (e.g. (==
     * internet) ecpds-dm1,ecpds-dm2). If there is no DataMover found then return the original DataMover.
     *
     * @param debug
     *            the debug
     * @param transferGroupName
     *            the transfer group name
     * @param originalMover
     *            the original mover
     * @param moverList
     *            the mover list
     *
     * @return the transfer server name
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static TransferServer getTransferServerName(final boolean debug, final String transferGroupName,
            final TransferServer originalMover, final String moverList) throws IOException {
        final List<String> result = new ArrayList<>();
        if (debug) {
            _log.debug("Getting moverName(defaulTransferGroup: " + transferGroupName + ", moverList: [" + moverList
                    + "])");
        }
        if (moverList != null && !moverList.isEmpty()) {
            // The list can be on multiple lines with one line per TransferGroup
            final var reader = new BufferedReader(new StringReader(moverList));
            String selectedMoverList = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                selectedMoverList = line;
                if (debug) {
                    _log.debug("Processing entry: " + line);
                }
                if (line.length() >= 6) {
                    final var first = line.indexOf("(");
                    final var last = line.indexOf(")");
                    if (first != -1 && last != -1 && last - first > 3) {
                        final var operator = line.substring(first + 1, last).trim();
                        final var comp = operator.substring(2).trim();
                        if (operator.startsWith("==") && transferGroupName.equals(comp)
                                || operator.startsWith("!=") && !transferGroupName.equals(comp)
                                || operator.startsWith(".=") && transferGroupName.startsWith(comp)
                                || operator.startsWith("=.") && transferGroupName.endsWith(comp)) {
                            selectedMoverList = line.substring(last + 1).trim();
                            break;
                        }
                        selectedMoverList = null;
                    }
                }
            }
            // Check if the list of DataMovers is valid (names separated by ';',
            // ',' or ' ' and names only use alphanumeric characters, '.' and
            // '-').
            if (selectedMoverList != null && !selectedMoverList.isEmpty()) {
                if (debug) {
                    _log.debug("SelectedMoverList: " + selectedMoverList);
                }
                final var tokenizer = new StringTokenizer(selectedMoverList, ";, ");
                while (tokenizer.hasMoreElements()) {
                    final var moverName = tokenizer.nextToken();
                    for (final char c : moverName.toCharArray()) {
                        if (!(Character.isDigit(c) || Character.isLetter(c) || c == '.' || c == '-')) {
                            throw new IOException("Invalid 'ecpds.moverListFor*' parameter (e.g. " + moverName + ")");
                        }
                    }
                    result.add(moverName);
                }
            } else // No list found in the configuration?
            if (debug) {
                _log.debug("No SelectedMoverList found for TransferGroup: " + transferGroupName);
            }
        }
        if (debug) {
            _log.debug("Random selection over " + result.size() + " element(s)");
        }
        // Parse the list of DataMovers and pick up one randomly. If the
        // selected DataMover is not available (not active or not connected to
        // the Master) then try to pick up a new one from the list, till one
        // available is found!
        while (!result.isEmpty()) {
            final String selected;
            // Choose one randomly!
            selected = result.remove(ThreadLocalRandom.current().nextInt(result.size()));
            // Get it from the DataBase!
            final var server = BASE.getTransferServerObject(selected);
            // Is the server found and active?
            if (server != null && server.getActive()) {
                final var group = BASE.getTransferGroupObject(server.getTransferGroupName());
                // Is the group found and active? Is the server connected?
                if (group != null && group.getActive() && MASTER.existsClientInterface(selected, "DataMover")) {
                    return server;
                }
            }
        }
        // Not a single DataMover found!
        return originalMover;
    }

    /**
     * Gets the hosts for source for the given data transfer. The Data Mover might be changed in the Data Transfer if
     * forced in the target host configuration. First, the Transfer Hosts to access the Data Movers of the current
     * Transfer Group are pushed in the list. Then, if the file is transmitted across Transfer Groups, the Transfer
     * Hosts to access the Data Movers of the original Transfer Group are also pushed in the list. Then, if a Backup
     * host is defined for the original Transfer Group it is also included in the list. And, finally if a Host for
     * Source is defined for the Data Transfer it is also added.
     *
     * @param transfer
     *            the transfer
     * @param hostForSource
     *            the host for source (null if not required)
     *
     * @return list of hosts for source
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws TransferServerException
     */
    private static Host[] _getHostsForSource(final DataTransfer transfer, final Host hostForSource)
            throws DataBaseException, TransferServerException {
        final var dataFile = transfer.getDataFile();
        final var server = transfer.getTransferServer();
        final var backupHost = transfer.getBackupHost();
        final List<Host> hostsForSource = new ArrayList<>();
        // These values might be adjusted!
        var targetGroup = server.getTransferGroupName();
        var moverName = server.getName();
        // Is there a request to use a specific data mover?
        try {
            final var targetHost = transfer.getHost();
            if (targetHost != null) {
                final var setup = HOST_ECPDS.getECtransSetup(targetHost.getData());
                final var moverList = setup.getString(HOST_ECPDS_MOVER_LIST_FOR_SOURCE);
                if (!moverList.isEmpty()) {
                    // Take one of the mandatory mover!
                    final var mandatoryServer = getTransferServerName(setup.getBoolean(HOST_ECTRANS_DEBUG),
                            targetHost.getTransferGroupName(), server, moverList);
                    if (!moverName.equals(mandatoryServer.getName())) {
                        // We found another one than the current one!
                        moverName = mandatoryServer.getName();
                        final var group = mandatoryServer.getTransferGroup();
                        targetGroup = group.getName();
                        // Update the DataTransfer for the history!
                        transfer.setTransferServerName(moverName);
                        transfer.setTransferServer(mandatoryServer);
                        _log.debug("Force usage of TransferServer: {}", moverName);
                        // The file might not be on the selected TransferServer
                        // so we have to provide the list of other
                        // TransferServers in the TransferGroup!
                        for (final TransferServer sourceServer : TransferServerProvider
                                .getTransferServersByLeastActivity("TransferScheduler.put",
                                        transfer.getDestinationName(), group)) {
                            if (!sourceServer.getName().equals(moverName)) {
                                final var host = sourceServer.getHostForReplication();
                                if (host != null && host.getActive()) {
                                    hostsForSource.add(host);
                                }
                            }
                        }
                    }
                }
            }
        } catch (final Throwable t) {
            _log.warn("Could not find alternative TransferServer name" + " (use default: " + moverName + ")", t);
        }
        final var sourceGroup = dataFile.getTransferGroup();
        if (sourceGroup != null && !targetGroup.equals(sourceGroup.getName())) {
            // If the file is transferred across transfer groups then let's
            // add the source hosts of the transfer servers in the original
            // transfer group for retrieval!
            _log.debug("Tranferring DataTransfer " + transfer.getId() + " across TransferGroups ("
                    + sourceGroup.getName() + " -> " + targetGroup + ")");
            for (final TransferServer transferServer : TransferServerProvider.getTransferServersByLeastActivity(
                    "TransferScheduler.put", transfer.getDestinationName(), sourceGroup)) {
                final var host = transferServer.getHostForReplication();
                if (host != null && host.getActive()) {
                    hostsForSource.add(host);
                }
            }
        }
        if (hostForSource != null) {
            // If the DataTransfer has been backup then let's add it to the list
            // of available hosts for retrieval!
            if (backupHost != null && backupHost.getActive()) {
                _log.debug("Include the backup Host (" + backupHost.getNickname() + ")");
                hostsForSource.add(backupHost);
            }
            // If there is an original source host defined and the file was
            // not removed after transmission then let's use it as a
            // potential source host!
            if (hostForSource.getActive() && !dataFile.getDeleteOriginal()) {
                _log.debug("Include the source Host (" + hostForSource.getNickname() + ")");
                hostsForSource.add(hostForSource);
            }
        }
        final var sources = hostsForSource.toArray(new Host[hostsForSource.size()]);
        // Log the list of source hosts!
        if (sources.length > 0) {
            final var hostsList = new StringBuilder();
            for (final Host host : sources) {
                hostsList.append(hostsList.isEmpty() ? "" : ", ").append(host.getNickname());
            }
            _log.debug("Source hosts list for " + transfer.getId() + ": " + hostsList.toString());
        }
        return sources;
    }

    /**
     * Puts the.
     *
     * @param transfer
     *            the transfer
     * @param hostForSource
     *            the host for source
     *
     * @return true, if successful
     */
    private static boolean put(final DataTransfer transfer, final Host hostForSource) {
        Throwable throwable = null;
        try {
            // Get the list of hosts for source
            final var hostsForSource = _getHostsForSource(transfer, hostForSource);
            // The Data Mover might have been changed when getting the hosts for source
            final var moverName = transfer.getTransferServerName();
            // Get the data mover interface!
            final var mover = MASTER.getDataMoverInterface(moverName);
            if (mover == null) {
                throw new MasterException("DataMover " + moverName + " not available");
            }
            // Execute the put on the data mover!
            final var result = TransferManagement.put(moverName, mover, hostsForSource, transfer);
            // Update the MoverName on the local DataTransfer!
            transfer.setMoverName(result.getMoverName());
        } catch (SourceNotAvailableException | MasterException | ECtransException | DirectoryException
                | DataBaseException | IOException e) {
            _log.warn("Transfering DataTransfer {}: {}", transfer.getId(), e.getMessage());
            throwable = e;
        } catch (final Throwable t) {
            _log.warn("Transfering DataTransfer {}", transfer.getId(), t);
            throwable = t;
        }
        if (throwable != null) {
            transfer.setComment(Format.getMessage(throwable));
            return false;
        }
        return true;
    }

    /**
     * Puts the.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param target
     *            the target
     * @param remotePosn
     *            the remote posn
     * @param size
     *            the size
     *
     * @return the proxy socket
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static ProxySocket put(final TransferServer server, final Host host, final String target,
            final long remotePosn, final long size) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            return TransferManagement.put(moverName, mover, host, target, remotePosn, size);
        } catch (final Exception e) {
            _log.warn("Put for target " + target + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets the.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param source
     *            the source
     * @param remotePosn
     *            the remote posn
     * @param length
     *            the length
     * @param removeOriginal
     *            the remove original
     *
     * @return the proxy socket
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static ProxySocket get(final TransferServer server, final Host host, final String source,
            final long remotePosn, final long length, final boolean removeOriginal) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            return TransferManagement.get(moverName, mover, host, source, remotePosn, removeOriginal);
        } catch (final Exception e) {
            _log.warn("Get for source " + source + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets a ProxySocket to download a DataTransfer.
     *
     * @param transfer
     *            the transfer
     * @param remotePosn
     *            the remote posn
     * @param length
     *            the length
     *
     * @return the proxy socket
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     * @throws TransferServerException
     */
    public static ProxySocket get(final DataTransfer transfer, final long remotePosn, final long length)
            throws DataBaseException, MasterException, TransferServerException {
        final var dataFile = transfer.getDataFile();
        final var servers = TransferServerProvider.getTransferServersByLeastActivity("TransferScheduler.get",
                transfer.getDestinationName(), transfer.getTransferServer(), dataFile.getTransferGroup(), null);
        final var count = servers.size();
        MasterException exception = null;
        for (var i = 0; i < count; i++) {
            final var server = servers.get(i);
            final var current = server.getName();
            transfer.setTransferServer(server);
            transfer.setTransferServerName(current);
            // If we are at the end of the list of DataMovers then we also push the
            // HostForSource to download the file on the fly from its source!
            final var useHostForSource = i == count - 1 || transfer.getOriginalTransferServer() == null;
            _log.debug("Trying DataTransfer-" + transfer.getId() + " on DataMover " + current
                    + (useHostForSource ? " (include source host)" : ""));
            try {
                return _get(
                        useHostForSource ? isAcquisition(transfer) ? BASE.getHost(dataFile.getHostForAcquisitionName())
                                : transfer.getDestination().getHostForSource() : null,
                        transfer, remotePosn, length);
            } catch (final MasterException e) {
                exception = e;
            }
        }
        if (exception == null) {
            exception = new MasterException("No TransferServer available for DataTransfer " + transfer.getId());
        }
        throw exception;
    }

    /**
     * Gets the.
     *
     * @param hostForSource
     *            the host for source
     * @param transfer
     *            the transfer
     * @param remotePosn
     *            the remote posn
     * @param length
     *            the length
     *
     * @return the proxy socket
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws TransferServerException
     */
    private static ProxySocket _get(final Host hostForSource, final DataTransfer transfer, final long remotePosn,
            final long length) throws MasterException, DataBaseException, TransferServerException {
        // Get the list of hosts for source
        final var hostsForSource = _getHostsForSource(transfer, hostForSource);
        // The Data Mover might have been changed when getting the hosts for source
        final var moverName = transfer.getTransferServerName();
        // Get the data mover interface!
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            final var dataFile = transfer.getDataFile();
            final var proxy = TransferManagement.get(moverName, mover, hostsForSource, dataFile, remotePosn, length);
            proxy.addObject(MoverInterface.class.getName(), mover.getRoot());
            _log.debug("DataTransfer-{} will be downloaded from {} (proxy={}) -> (size={},posn={},length={})",
                    transfer.getId(), moverName, proxy, dataFile.getSize(), remotePosn, length);
            return proxy;
        } catch (final SourceNotAvailableException e) {
            _log.warn(e.getMessage());
            throw new MasterException(e.getMessage());
        } catch (final Throwable t) {
            _log.warn("Get for DataTransfer-" + transfer.getId() + " on " + moverName, t);
            throw new MasterException(t.getMessage());
        }
    }

    /**
     * Check.
     *
     * @param proxySocket
     *            the proxy socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void check(final ProxySocket proxySocket) throws IOException {
        final var root = String.valueOf(proxySocket.getObject(MoverInterface.class.getName()));
        final var ticket = proxySocket.getTicket();
        if (root != null) {
            final var mover = MASTER.getDataMoverInterface(root);
            mover.check(ticket);
        } else {
            _log.warn("Couldn't check Ticket-" + ticket + " for DataMover " + root);
        }
    }

    /**
     * Size.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @return the long
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static long size(final TransferServer server, final Host host, final String source) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            return TransferManagement.size(moverName, mover, host, source);
        } catch (final Exception e) {
            _log.warn("Size for source " + source + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * List.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param source
     *            the source
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static String[] list(final TransferServer server, final Host host, final String source, final String pattern)
            throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            return TransferManagement.list(moverName, mover, host, source, pattern);
        } catch (final Exception e) {
            _log.warn("List for source " + source + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Del.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static void del(final TransferServer server, final Host host, final String source) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            TransferManagement.del(moverName, mover, host, source);
        } catch (final Exception e) {
            _log.warn("Del for source " + source + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Check.
     *
     * @param server
     *            the server
     * @param ticket
     *            the ticket
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static void check(final TransferServer server, final long ticket) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            TransferManagement.check(moverName, mover, ticket);
        } catch (final Exception e) {
            _log.warn("Check for Ticket-" + ticket + " on " + moverName, e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Mkdir.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static void mkdir(final TransferServer server, final Host host, final String dir) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            TransferManagement.mkdir(moverName, mover, host, dir);
        } catch (final Exception e) {
            _log.warn("Mkdir for dir " + dir + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Rmdir.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static void rmdir(final TransferServer server, final Host host, final String dir) throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            TransferManagement.rmdir(moverName, mover, host, dir);
        } catch (final Exception e) {
            _log.warn("Rmdir for dir " + dir + " on " + moverName + " using Host-" + host.getName() + " ("
                    + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Checks if is acquisition.
     *
     * @param transfer
     *            the transfer
     *
     * @return true, if is acquisition
     */
    public static boolean isAcquisition(final DataTransfer transfer) {
        return isNotEmpty(transfer.getDataFile().getHostForAcquisitionName());
    }

    /**
     * Move.
     *
     * @param server
     *            the server
     * @param host
     *            the host
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public static void move(final TransferServer server, final Host host, final String source, final String target)
            throws MasterException {
        final var moverName = server.getName();
        final var mover = MASTER.getDataMoverInterface(moverName);
        if (mover == null) {
            throw new MasterException("DataMover " + moverName + " not available");
        }
        try {
            TransferManagement.move(moverName, mover, host, source, target);
        } catch (final Exception e) {
            _log.warn("Move for source " + source + " to target " + target + " on " + moverName + " using Host-"
                    + host.getName() + " (" + host.getNickname() + ")", e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Puts the.
     *
     * @param servers
     *            the servers
     * @param transfer
     *            the transfer
     * @param hostForSource
     *            the host for source
     *
     * @return true, if successful
     */
    public static boolean put(final TransferServer[] servers, final DataTransfer transfer, final Host hostForSource) {
        final var useSourceHost = hostForSource != null;
        for (var i = 0; i < servers.length; i++) {
            final var server = servers[i];
            final var serverName = server.getName();
            transfer.setTransferServer(server);
            transfer.setTransferServerName(serverName);
            // If hostForSource is not null, only use it on the last server or if no
            // original server is set
            final var applySourceHost = useSourceHost
                    && (i == servers.length - 1 || transfer.getOriginalTransferServer() == null);
            final var status = transfer.getStatusCode();
            if (StatusFactory.INTR.equals(status) || StatusFactory.STOP.equals(status)) {
                // The transfer has been stopped or interrupted — stop processing
                break;
            }
            if (hostForSource != null) {
                _log.debug("Trying DataTransfer-{} on DataMover {} (include source host {})", transfer.getId(),
                        serverName, hostForSource.getNickname());
            } else {
                _log.debug("Trying DataTransfer-{} on DataMover {}", transfer.getId(), serverName);
            }
            if (put(transfer, applySourceHost ? hostForSource : null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The Class FilterResult.
     */
    public static final class FilterResult {
        /** The transfer servers. */
        public final List<String> transferServers = new ArrayList<>();

        /** The data file. */
        public DataFile dataFile = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Filter.
     *
     * @param serversList
     *            the servers list
     * @param dataFile
     *            the data file
     * @param remove
     *            the remove
     *
     * @return the filter result
     */
    public static FilterResult filter(final TransferServer[] serversList, DataFile dataFile, final boolean remove) {
        final var group = dataFile.getTransferGroup();
        var minReplicationCount = group.getMinReplicationCount();
        if (minReplicationCount <= 0 || minReplicationCount > serversList.length) {
            minReplicationCount = serversList.length;
        }
        var minFilteringCount = group.getMinFilteringCount();
        if (minFilteringCount <= 0 || minFilteringCount > minReplicationCount) {
            minFilteringCount = minReplicationCount;
        }
        if (!remove) {
            _log.debug("Minimum count for filtering of DataFile {}: {}", dataFile.getId(), minFilteringCount);
        }
        final List<String> transferServers = new ArrayList<>();
        for (final TransferServer transferServer : _getSortedList(dataFile.getGetHost(), serversList)) {
            if (transferServers.size() >= minFilteringCount) {
                break;
            }
            final var moverName = transferServer.getName();
            final var mover = MASTER.getDataMoverInterface(moverName);
            if (mover == null) {
                _log.warn("Target DataMover {} not available for filtering", moverName);
                continue;
            }
            var filtered = false;
            try {
                dataFile = mover.filter(dataFile, remove);
                transferServers.add("DataMover=" + moverName);
                filtered = true;
                if (remove) {
                    break;
                }
            } catch (final Throwable t) {
                final var message = "Filtering DataFile " + dataFile.getId();
                if (Thread.interrupted()) {
                    _log.warn("{} interrupted", message);
                    break;
                }
                _log.warn(message, t);
                continue;
            } finally {
                _log.info("DataFile {}{} {} on DataMover {} (count {})", dataFile.getId(),
                        dataFile.getArrivedTime() != null
                                ? " (" + Format.formatTime(dataFile.getArrivedTime().getTime()) + ")" : "",
                        filtered ? "filtered" : "NOT filtered", moverName, transferServers.size());
            }
        }
        if (!remove) {
            _log.debug("Filtering count for DataFile {}: {}", dataFile.getId(), transferServers.size());
        }
        final var fr = new FilterResult();
        fr.dataFile = dataFile;
        fr.transferServers.clear();
        fr.transferServers.addAll(transferServers);
        fr.complete = transferServers.size() >= minFilteringCount;
        if (!remove && !fr.complete) {
            _log.warn("Filtering for DataFile {} not complete (only {}/{} successfull filtering)", dataFile.getId(),
                    transferServers.size(), minFilteringCount);
        }
        return fr;
    }

    /**
     * Gets the sorted list.
     *
     * @param sourceMoverName
     *            the source mover name
     * @param serversList
     *            the servers list
     *
     * @return the vector
     */
    private static List<TransferServer> _getSortedList(final String sourceMoverName,
            final TransferServer[] serversList) {
        final List<TransferServer> result = new ArrayList<>(serversList.length);
        for (final TransferServer server : serversList) {
            if (server.getName().equals(sourceMoverName)) {
                result.add(server);
                break;
            }
        }
        for (final TransferServer server : serversList) {
            if (!server.getName().equals(sourceMoverName)) {
                result.add(server);
            }
        }
        final var dataMoversList = result.stream().map(TransferServer::getName).collect(Collectors.joining(", "));
        _log.debug("DataMover(s) list: {} (source={})", dataMoversList, sourceMoverName);
        return result;
    }

    /**
     * The Class ReplicateResult.
     */
    public static final class ReplicateResult {
        /** The transfer servers. */
        public final List<String> transferServers = new ArrayList<>();

        /** The transfer server. */
        public String transferServer = null;

        /** The data file. */
        public DataFile dataFile = null;

        /** The message. */
        public String message = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Replicate.
     *
     * @param serversList
     *            the servers list
     * @param transfer
     *            the transfer
     *
     * @return the replicate result
     */
    public static ReplicateResult replicate(final String sourceMoverName, final TransferServer[] serversList,
            final DataTransfer transfer) {
        final var rr = new ReplicateResult();
        rr.dataFile = transfer.getDataFile();
        final var group = rr.dataFile.getTransferGroup();
        var minReplicationCount = group.getMinReplicationCount();
        if (minReplicationCount <= 0 || minReplicationCount > serversList.length) {
            minReplicationCount = serversList.length;
        }
        _log.debug("Minimum count for replication of DataTransfer " + transfer.getId() + ": " + minReplicationCount);
        final var expiry = transfer.getExpiryTime();
        final var expired = expiry != null && expiry.before(new Date(System.currentTimeMillis()));
        if (!expired) {
            final var sourceMover = MASTER.getDataMoverInterface(sourceMoverName);
            if (sourceMover == null) {
                rr.message = "Source DataMover " + sourceMoverName + " not available for replication";
                _log.warn(rr.message);
            } else {
                final var servers = _getSortedList(sourceMoverName, serversList);
                final List<Host> hostsForSource = new ArrayList<>();
                for (final TransferServer targetMover : servers) {
                    final var host = targetMover.getHostForReplication();
                    if (host != null && host.getActive()) {
                        hostsForSource.add(host);
                    }
                }
                // If this is an acquisition transfer then we have to push the
                // host used to discover the file as a source host!
                final var acquisitionHost = BASE.getHostObject(rr.dataFile.getHostForAcquisitionName());
                if (acquisitionHost != null) {
                    if (acquisitionHost.getActive()) {
                        hostsForSource.add(acquisitionHost);
                    }
                } else {
                    // This is not an acquisition host so we push the host for
                    // source defined in the destination!
                    final var hostForSource = transfer.getDestination().getHostForSource();
                    if (hostForSource != null && hostForSource.getActive() && !rr.dataFile.getDeleteOriginal()) {
                        hostsForSource.add(hostForSource);
                    }
                }
                for (final TransferServer targetMover : servers) {
                    if (rr.transferServers.size() + 1 >= minReplicationCount) {
                        break;
                    }
                    final var targetMoverName = targetMover.getName();
                    if (targetMoverName.equals(sourceMoverName)) {
                        continue;
                    }
                    if (!targetMover.getReplicate()) {
                        rr.message = "Target DataMover " + targetMoverName + " not configured for replication";
                        _log.debug(rr.message);
                        continue;
                    }
                    if (!MASTER.existsClientInterface(targetMoverName, "DataMover")) {
                        rr.message = "Target DataMover " + targetMoverName + " not available for replication";
                        _log.warn(rr.message);
                        continue;
                    }
                    final List<Host> hostsForSourceList = new ArrayList<>(hostsForSource);
                    final var hostForReplication = targetMover.getHostForReplication();
                    var replicated = false;
                    if (!hostsForSourceList.remove(hostForReplication)) {
                        rr.message = "Current HostForReplication not found?";
                        _log.warn(rr.message);
                    }
                    try {
                        _log.debug("Replicating DataFile " + rr.dataFile.getId() + " from " + sourceMoverName + " to "
                                + targetMoverName + " using " + hostForReplication.getNickname());
                        rr.dataFile = sourceMover.replicate(rr.dataFile, hostForReplication,
                                hostsForSourceList.toArray(new Host[hostsForSourceList.size()]));
                        rr.transferServers.add("DataMover=" + targetMoverName);
                        replicated = true;
                    } catch (final Throwable t) {
                        final var message = "Replicating DataFile " + rr.dataFile.getId() + " on " + targetMoverName;
                        if (Thread.interrupted()) {
                            rr.message = message + " interrupted";
                            _log.warn(rr.message);
                            break;
                        } else {
                            rr.message = message;
                            _log.warn(rr.message, t);
                            continue;
                        }
                    } finally {
                        _log.info("DataFile " + rr.dataFile.getId() + (replicated ? "" : " NOT") + " replicated from "
                                + sourceMoverName + " to " + targetMoverName + " (count "
                                + (rr.transferServers.size() + 1) + ")");
                    }
                }
            }
        } else {
            rr.message = "DataTransfer " + transfer.getId() + " is expired (not replicated)";
            _log.warn(rr.message);
        }
        rr.complete = rr.transferServers.size() + 1 >= minReplicationCount;
        if (!expired && !rr.complete) {
            rr.message = "Replication for DataTransfer " + transfer.getId() + " not complete (only "
                    + (rr.transferServers.size() + 1) + "/" + minReplicationCount + " instances of the file)";
            _log.warn(rr.message);
        }
        return rr;
    }

    /**
     * The Class BackupResult.
     */
    public static final class BackupResult {
        /** The host for backup. */
        public Host hostForBackup = null;

        /** The transfer server. */
        public String transferServer = null;

        /** The data file. */
        public DataFile dataFile = null;

        /** The message. */
        public String message = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Backup.
     *
     * @param hostForBackup
     *            the host for backup
     * @param servers
     *            the servers
     * @param transfer
     *            the transfer
     *
     * @return the backup result
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static BackupResult backup(final Host hostForBackup, final TransferServer[] servers,
            final DataTransfer transfer) throws DataBaseException {
        final var rr = new BackupResult();
        rr.dataFile = transfer.getDataFile();
        final var hostType = hostForBackup.getType();
        final var expiry = transfer.getExpiryTime();
        final var expired = expiry != null && expiry.before(new Date(System.currentTimeMillis()));
        if (!expired) {
            var moverName = getTransferServerUsedForRetrieval(transfer);
            // Is there a request to use a specific data mover in the same
            // group?
            try {
                final var setup = HOST_ECPDS.getECtransSetup(hostForBackup.getData());
                final var moverList = setup.getString(HOST_ECPDS_MOVER_LIST_FOR_BACKUP);
                if (moverList.length() > 0) {
                    // Take one of the mandatory mover!
                    final var mandatoryServer = getTransferServerName(setup.getBoolean(HOST_ECTRANS_DEBUG),
                            BASE.getTransferServer(moverName).getTransferGroupName(), null, moverList);
                    if (mandatoryServer != null && !moverName.equals(mandatoryServer.getName())) {
                        // We found another one than the current one!
                        moverName = mandatoryServer.getName();
                        _log.debug("Force usage of TransferServer: " + moverName);
                    }
                }
            } catch (final Throwable t) {
                _log.warn("Could not find alternative TransferServer name" + " (use default: " + moverName + ")", t);
            }
            final var sourceMover = MASTER.getDataMoverInterface(moverName);
            if (sourceMover == null) {
                rr.message = "Source DataMover=" + moverName + " not available for " + hostType;
                _log.warn(rr.message);
            } else {
                final List<Host> hostsForSource = new ArrayList<>();
                // We have to push all the data-movers which can have the file
                // (in case it is not available on the selected data-mover)!
                for (final TransferServer server : _getSortedList(moverName, servers)) {
                    final var host = server.getHostForReplication();
                    if (host != null && host.getActive()) {
                        hostsForSource.add(host);
                    }
                }
                // Is it an acquisition host?
                final var acquisitionHost = BASE.getHostObject(rr.dataFile.getHostForAcquisitionName());
                if (acquisitionHost != null) {
                    // This is an acquisition transfer so we have to push the
                    // host used to discover the file as a source host!
                    if (acquisitionHost.getActive()) {
                        hostsForSource.add(acquisitionHost);
                    }
                } else {
                    // This is NOT an acquisition host so we push the host for
                    // source defined in the destination!
                    final var hostForSource = transfer.getDestination().getHostForSource();
                    if (hostForSource != null && hostForSource.getActive() && !rr.dataFile.getDeleteOriginal()) {
                        hostsForSource.add(hostForSource);
                    }
                }
                try {
                    _log.debug(hostType + " DataFile " + rr.dataFile.getId() + " from " + moverName + " to "
                            + hostForBackup.getNickname());
                    rr.dataFile = sourceMover.replicate(rr.dataFile, hostForBackup,
                            hostsForSource.toArray(new Host[hostsForSource.size()]));
                    rr.transferServer = "DataMover=" + moverName;
                    rr.hostForBackup = hostForBackup;
                    rr.complete = true;
                } catch (final Throwable t) {
                    if (Thread.interrupted()) {
                        rr.message = hostType + " interrupted on DataMover=" + moverName + " using Host="
                                + hostForBackup.getName() + " (" + hostForBackup.getNickname() + ")";
                    } else {
                        rr.message = hostType + " failed on DataMover=" + moverName + " using Host="
                                + hostForBackup.getName() + " (" + hostForBackup.getNickname() + "): "
                                + Format.getMessage(t);
                    }
                    _log.warn(rr.message, t);
                }
            }
        } else {
            // Can happen if the DataTransfer expire right after the
            // Backup/Proxy starts!
            _log.warn("DataTransfer " + transfer.getId() + " is expired (no " + hostType + ")");
        }
        if (!expired && !rr.complete) {
            _log.warn(hostType + " for DataTransfer " + transfer.getId() + " not complete");
        }
        return rr;
    }

    /**
     * The Class AcquisitionResult.
     */
    public static final class AcquisitionResult {
        /** The in. */
        public InputStream in = null;

        /** The server. */
        public TransferServer server = null;

        /** The message. */
        public String message = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Version 1 is getting the list as an array of String and version 2 is getting the list as an InputStream.
     *
     * @param out
     *            the out
     * @param destinationName
     *            the destination name
     * @param host
     *            the host
     * @param source
     *            the source
     * @param pattern
     *            the pattern
     * @param synchronous
     *            the synchronous
     *
     * @return the acquisition result
     *
     * @throws ecmwf.ecpds.master.transfer.TransferServerProvider.TransferServerException
     *             the transfer server exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static AcquisitionResult acquisition(final StatusUpdate out, final String destinationName, final Host host,
            final String source, final String pattern, final boolean synchronous)
            throws TransferServerException, DataBaseException, IOException {
        final var ar = new AcquisitionResult();
        // Let's find the list of transfer servers to do the listing. We force a
        // check against the clusters to spread the load evenly on all data
        // movers!
        final var provider = new TransferServerProvider("TransferScheduler.acquisition", null,
                host.getTransferGroupName(), destinationName, host);
        for (final TransferServer current : provider.getTransferServersByLeastActivity()) {
            final var getHost = current.getName();
            MoverInterface mover;
            ar.server = current;
            if ((mover = MASTER.getDataMoverInterface(getHost)) == null) {
                _log.warn(ar.message = "DataMover " + getHost + " NOT available for listing task");
                continue;
            }
            out.info("Starting listing task from DataMover=" + getHost, StatusUpdate.IN_PROGRESS);
            try {
                _log.debug("Starting listing task for {} from {} using {}", destinationName, getHost,
                        host.getNickname());
                ar.in = new LocalInputStream(mover.listAsByteArray(host, source, pattern, synchronous));
                if (synchronous) { // Uncompress the data (this is not on-the-fly)!
                    ar.in = new GZIPInputStream(ar.in);
                }
                out.info("Listing task completed successfully on DataMover=" + getHost);
                ar.complete = true;
                break;
            } catch (final Throwable t) {
                if (Thread.interrupted()) {
                    out.warn("Listing task interrupted on DataMover=" + getHost);
                    _log.warn("Listing task interrupted for " + destinationName + " on " + getHost + " (source="
                            + source + ",pattern=" + pattern + ")", t);
                    ar.message = "Listing task interrupted";
                    break;
                } else {
                    out.warn("Listing task failed on DataMover=" + getHost + " with error: " + Format.getMessage(t));
                    _log.warn("Listing task for " + destinationName + " on " + getHost + " (source=" + source
                            + ",pattern=" + pattern + ")", t);
                    ar.message = Format.getMessage(t);
                    continue;
                }
            } finally {
                _log.info("Listing task for " + destinationName + " on " + host.getNickname()
                        + (ar.complete ? " completed" : " NOT completed") + " from DataMover " + getHost);
            }
        }
        return ar;
    }

    /**
     * The Class ExecutionResult.
     */
    public static final class ExecutionResult {
        /** The in. */
        public InputStream in = null;

        /** The server. */
        public TransferServer server = null;

        /** The message. */
        public String message = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Executing script against data mover (this is because the script might open a connection to a remote site to get
     * the list of files to retrieve).
     *
     * @param out
     *            the out
     * @param destinationName
     *            the destination name
     * @param host
     *            the host
     * @param script
     *            the script
     *
     * @return the execution result
     *
     * @throws ecmwf.ecpds.master.transfer.TransferServerProvider.TransferServerException
     *             the transfer server exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ExecutionResult execution(final StatusUpdate out, final String destinationName, final Host host,
            final String script) throws TransferServerException, DataBaseException, IOException {
        final var er = new ExecutionResult();
        // Let's find the list of transfer servers to do the execution. We force a
        // check against the clusters to spread the load evenly on all data
        // movers!
        final var provider = new TransferServerProvider("TransferScheduler.execution", null,
                host.getTransferGroupName(), destinationName, host);
        for (final TransferServer current : provider.getTransferServersByLeastActivity()) {
            final var getHost = current.getName();
            MoverInterface mover;
            er.server = current;
            if ((mover = MASTER.getDataMoverInterface(getHost)) == null) {
                _log.warn(er.message = "DataMover " + getHost + " NOT available for execution task");
                continue;
            }
            out.info("Starting execution task from DataMover=" + getHost, StatusUpdate.IN_PROGRESS);
            try {
                _log.debug("Starting execution task for " + destinationName + " from " + getHost + " using "
                        + host.getNickname());
                er.in = new GZIPInputStream(new LocalInputStream(mover.execute(script)));
                out.info("Execution task completed successfully on DataMover=" + getHost);
                er.complete = true;
                break;
            } catch (final Throwable t) {
                if (Thread.interrupted()) {
                    out.warn("Execution task interrupted on DataMover=" + getHost);
                    _log.warn("Execution task interrupted for " + destinationName + " on " + getHost, t);
                    er.message = "Execution task interrupted";
                    break;
                } else {
                    out.warn("Execution task failed on DataMover=" + getHost + ": " + Format.getMessage(t));
                    _log.warn("Execution task for " + destinationName + " on " + getHost, t);
                    er.message = Format.getMessage(t);
                    continue;
                }
            } finally {
                _log.info("Execution task for " + destinationName + " on " + host.getNickname()
                        + (er.complete ? " completed" : " NOT completed") + " from DataMover " + getHost);
            }
        }
        return er;
    }

    /**
     * The Class DownloadResult.
     */
    public static final class DownloadResult {
        /** The data file. */
        public DataFile dataFile = null;

        /** The server. */
        public TransferServer server = null;

        /** The message. */
        public String message = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * We keep in this list the number of current downloads for the key "moverName.fileSystem".
     */
    private static final Map<String, Map<Integer, Integer>> _metricsPerDataMovers = new ConcurrentHashMap<>();

    static {
        // Start the ManagementThread to dump every second the usage of the
        // DataMovers in the dedicated log. The Thread is only started if the
        // Logger is enabled for info!
        if (_metrics.isInfoEnabled()) {
            final var managementThread = new ManagementThread();
            managementThread.setPriority(Thread.MIN_PRIORITY);
            managementThread.execute();
        }
    }

    /**
     * The Class ManagementThread. Display the list of DataMover and usage per file system in a log file.
     */
    private static final class ManagementThread extends ConfigurableLoopRunnable {
        /** The _last line. */
        private final StringBuilder _lastLine = new StringBuilder();

        /**
         * Instantiates a new management thread.
         */
        ManagementThread() {
            setPause(Cnf.at("TransferScheduler", "delay", Timer.ONE_SECOND));
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                final var line = new StringBuilder();
                final List<String> movers = new ArrayList<>(_metricsPerDataMovers.keySet());
                Collections.sort(movers);
                for (final String mover : movers) {
                    // This is a new DataMover!
                    line.append(line.isEmpty() ? "" : "};").append(mover).append("{");
                    final var metrics = _metricsPerDataMovers.get(mover);
                    final List<Integer> fileSystems = new ArrayList<>(metrics.keySet());
                    Collections.sort(fileSystems);
                    var first = true;
                    for (final Integer fileSystem : fileSystems) {
                        line.append(first ? "" : ",").append(metrics.get(fileSystem));
                        first = false;
                    }
                }
                if (line.length() > 0 && !_lastLine.toString().equals(line.toString())) {
                    // We found at least one difference!
                    _lastLine.setLength(0);
                    _lastLine.append(line);
                    _metrics.info(line + "}");
                }
            } catch (final Throwable t) {
                _metrics.warn("Building stats", t);
            }
        }
    }

    /**
     * Allow getting the current number of downloads for the provided moverName and fileSystem.
     *
     * @param server
     *            the server
     * @param fileSystem
     *            the file system
     *
     * @return the number of downloads for
     */
    public static int getNumberOfDownloadsFor(final TransferServer server, final int fileSystem) {
        // We don't need to synchronise as we are only reading the information!
        final var metrics = _metricsPerDataMovers.get(server.getTransferGroupName() + "." + server.getName());
        if (metrics != null) {
            // The DataMover has already been recorded!
            final var count = metrics.get(fileSystem);
            return count == null ? 0 : count;
        }
        return 0;
    }

    /**
     * Download.
     *
     * @param transfer
     *            the transfer
     * @param hostForSource
     *            the host for source
     * @param relatedTransfers
     *            the related transfers
     *
     * @return the download result
     *
     * @throws ecmwf.ecpds.master.transfer.TransferServerProvider.TransferServerException
     *             the transfer server exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static DownloadResult download(final DataTransfer transfer, final Host hostForSource,
            final List<DataTransfer> relatedTransfers) throws TransferServerException, DataBaseException {
        final var dr = new DownloadResult();
        dr.dataFile = transfer.getDataFile();
        final var expiry = transfer.getExpiryTime();
        if (expiry == null || !expiry.before(new Date(System.currentTimeMillis()))) {
            if (hostForSource == null || !hostForSource.getActive() || dr.dataFile.getDeleteOriginal()) {
                _log.warn(dr.message = "Host for source not available or source file deleted");
            } else {
                final var provider = new TransferServerProvider("TransferScheduler.download", null,
                        hostForSource.getTransferGroupName(), transfer.getDestinationName(), hostForSource);
                final var group = provider.getTransferGroup();
                dr.dataFile.setTransferGroup(group);
                dr.dataFile.setTransferGroupName(group.getName());
                dr.dataFile.setFileSystem(provider.getFileSystem());
                for (final TransferServer server : provider.getTransferServersByMostFreeSpace()) {
                    final var moverName = server.getName();
                    final var groupName = server.getTransferGroupName();
                    MoverInterface mover;
                    dr.server = server;
                    if ((mover = MASTER.getDataMoverInterface(moverName)) == null) {
                        _log.warn(dr.message = "DataMover " + moverName + " NOT available for download");
                        continue;
                    }
                    // Now we look for the metrics for this DataMover and
                    // fileSystem combination!
                    Map<Integer, Integer> metrics;
                    synchronized (_metricsPerDataMovers) {
                        final var key = groupName + "." + moverName;
                        metrics = _metricsPerDataMovers.get(key);
                        if (metrics == null) {
                            // This combination was never stored before!
                            _metricsPerDataMovers.put(key, metrics = new ConcurrentHashMap<>());
                            // We have to populate it with all the
                            // FileSystems for this Mover!
                            for (var i = 0; i < server.getTransferGroup().getVolumeCount(); i++) {
                                metrics.put(i, 0);
                            }
                        }
                    }
                    // Now let's increment the counter!
                    final var fileSystem = dr.dataFile.getFileSystem();
                    synchronized (metrics) {
                        // We have to check if the fileSystem already exists
                        // as the configuration might have changed since the
                        // registration in the metrics!
                        final var currentCount = metrics.get(fileSystem);
                        metrics.put(fileSystem, (currentCount == null ? 0 : currentCount) + 1);
                    }
                    // Now we send the download request to the selected
                    // DataMover!
                    try {
                        _log.debug("Downloading DataFile " + dr.dataFile.getId() + " on " + moverName + " from "
                                + hostForSource.getNickname());
                        transfer.setMoverName(moverName);
                        for (final DataTransfer related : relatedTransfers) {
                            MASTER.addTransferHistory(related, hostForSource, related.getStatusCode(),
                                    "Trying retrieval on DataMover=" + moverName, false);
                        }
                        dr.dataFile = mover.download(dr.dataFile, hostForSource);
                        dr.complete = true;
                        break;
                    } catch (final Throwable t) {
                        final var interrupted = Thread.interrupted();
                        dr.message = null;
                        // Add some information in the transfer history concerning the issue!
                        for (final DataTransfer related : relatedTransfers) {
                            MASTER.addTransferHistory(related, hostForSource, related.getStatusCode(),
                                    "Retrieval " + (interrupted ? "interrupted" : "failed") + " on DataMover="
                                            + moverName + (interrupted ? "" : " - " + Format.getMessage(t)),
                                    true);
                        }
                        final var message = "Downloading DataTransfer " + transfer.getId() + " on " + moverName;
                        if (interrupted) {
                            _log.warn(message + " interrupted");
                            break;
                        } else {
                            _log.warn(message, t);
                            continue;
                        }
                    } finally {
                        // Now let's decrease the counter!
                        synchronized (metrics) {
                            metrics.put(fileSystem, metrics.get(fileSystem) - 1);
                        }
                        _log.info("DataTransfer " + transfer.getId() + (dr.complete ? " downloaded" : " NOT downloaded")
                                + " from DataMover " + moverName);
                    }
                }
            }
        } else {
            _log.warn(dr.message = "DataTransfer " + transfer.getId() + " is expired (will not be downloaded)");
            dr.complete = true;
        }
        return dr;
    }

    /**
     * The Class PurgeResult.
     */
    public static final class PurgeResult {
        /** The transfer servers. */
        public final List<String> transferServers = new ArrayList<>();

        /** The data file. */
        public DataFile dataFile = null;

        /** The complete. */
        public boolean complete = false;
    }

    /**
     * Purge.
     *
     * @param transfers
     *            the transfers
     * @param dataFile
     *            the data file
     *
     * @return the purge result
     */
    public static PurgeResult purge(final TransferServer[] transfers, final DataFile dataFile) {
        final var result = new PurgeResult();
        result.complete = true;
        result.dataFile = dataFile;
        // Remove the data files on the data movers!
        for (final TransferServer server : transfers) {
            if (!server.getTransferGroupName().equals(dataFile.getTransferGroupName())) {
                continue;
            }
            final var serverName = server.getName();
            var deleted = false;
            final var mover = MASTER.getDataMoverInterface(serverName);
            try {
                result.complete = mover != null && (deleted = mover.del(dataFile)) && result.complete;
                if (deleted) {
                    result.transferServers.add("DataMover=" + serverName);
                }
            } catch (final Throwable t) {
                _log.warn("Deleting DataFile", t);
                result.complete = false;
            } finally {
                _log.info("DataFile " + dataFile.getId()
                        + (dataFile.getArrivedTime() != null
                                ? " (" + Format.formatTime(dataFile.getArrivedTime().getTime()) + ")" : "")
                        + " " + (deleted ? "deleted" : "NOT deleted") + " on DataMover " + serverName);
            }
        }
        return result;
    }

    /**
     * Purge.
     *
     * @param transfers
     *            the transfers
     * @param proxyHost
     *            the proxy host
     * @param dataFile
     *            the data file
     *
     * @return the purge result
     */
    public static PurgeResult purge(final TransferServer[] transfers, final Host proxyHost, final DataFile dataFile) {
        final var result = new PurgeResult();
        result.complete = true;
        result.dataFile = dataFile;
        // Remove the data file on the ProxyHost!
        for (final TransferServer server : transfers) {
            final var moverName = server.getName();
            var deleted = false;
            final var mover = MASTER.getDataMoverInterface(moverName);
            try {
                result.complete = mover != null && (deleted = mover.del(proxyHost, dataFile));
                if (deleted) {
                    result.transferServers.add("DataMover=" + moverName);
                    break;
                }
            } catch (final Throwable t) {
                _log.warn("Deleting DataFile on Host-{}", proxyHost.getName(), t);
                result.complete = false;
            } finally {
                _log.info("DataFile {}{} {} from DataMover {} using Host-{}", dataFile.getId(),
                        dataFile.getArrivedTime() != null
                                ? " (" + Format.formatTime(dataFile.getArrivedTime().getTime()) + ")" : "",
                        deleted ? "deleted" : "NOT deleted", moverName, proxyHost.getName());
            }
        }
        return result;
    }

    /**
     * Purge.
     *
     * @param servers
     *            the servers
     * @param proxyHost
     *            the proxy host
     * @param directories
     *            the directories
     */
    public static void purge(final TransferServer[] servers, final Host proxyHost,
            final List<ExistingStorageDirectory> directories) {
        // Remove the data file on the ProxyHost!
        for (final TransferServer server : servers) {
            final var moverName = server.getName();
            final var mover = MASTER.getDataMoverInterface(moverName);
            try {
                mover.purge(proxyHost, directories);
                _log.debug("Purge started on Host-" + proxyHost.getName() + " on " + moverName + " with "
                        + directories.size() + " directories");
                break;
            } catch (final Throwable t) {
                _log.warn("Purging DataFile on Host-" + proxyHost.getName(), t);
            }
        }
    }

    /**
     * Gets the mover report.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return the mover report
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getMoverReport(final Host proxyHost) throws DataBaseException, IOException {
        // Get a MoverReport through a ProxyHost!
        final var groupName = proxyHost.getTransferGroupName();
        Throwable throwable = null;
        for (final TransferServer server : BASE.getTransferServers(groupName)) {
            try {
                return MASTER.getDataMoverInterface(server.getName()).getMoverReport(proxyHost);
            } catch (final Throwable t) {
                _log.warn("Getting MoverReport through ProxyHost-" + proxyHost.getName(), throwable = t);
            }
        }
        throw new IOException(throwable != null ? "Failed on all " + groupName + " DataMover(s)"
                : "No " + groupName + " DataMover available", throwable);
    }

    /**
     * Gets the host report.
     *
     * @param proxyHost
     *            the proxy host
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getHostReport(final Host proxyHost, final Host host) throws DataBaseException, IOException {
        // Get a HostReport through a ProxyHost!
        final var groupName = proxyHost.getTransferGroupName();
        Throwable throwable = null;
        for (final TransferServer server : BASE.getTransferServers(groupName)) {
            try {
                return MASTER.getDataMoverInterface(server.getName()).getHostReport(proxyHost, host);
            } catch (final Throwable t) {
                _log.warn("Getting HostReport through ProxyHost-" + proxyHost.getName(), throwable = t);
            }
        }
        throw new IOException(throwable != null ? "Failed on all " + groupName + " DataMover(s)"
                : "No " + groupName + " DataMover available", throwable);
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public void shutdown() {
        shutdown(true);
    }

    /**
     * Shutdown.
     *
     * @param graceful
     *            the graceful
     */
    public void shutdown(final boolean graceful) {
        _log.info("Starting scheduler shutdown (" + (graceful ? "graceful" : "immediat") + ")");
        if (_monitoringThread != null) {
            _monitoringThread.shutdown();
        }
        super.shutdown();
        for (final DestinationThread thread : _threads.values()) {
            thread.shutdown(graceful ? Timer.ONE_HOUR : 0, false, "scheduler shutdown");
        }
        _log.info("Scheduler shutdown completed");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "This MBean provides operations to monitor and manage " + "the TransferScheduler",
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("DebugScheduler", "java.lang.Boolean",
                        "DebugScheduler: display debug informations in the log file.", true, true, false) },
                new MBeanOperationInfo[0]);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("DebugScheduler".equals(attributeName)) {
                return _debug;
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("DebugScheduler".equals(name)) {
            _debug = ((Boolean) value);
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * The Class MonitoringThread. The OpsViewManager is used to push the list of Destinations by types and the
     * MonitorManager is used to push the status for each Destination. The list of Destinations is pushed every 10
     * minutes.
     */
    public class MonitoringThread extends WakeupThread implements MonitorCallback {
        /** List of Destinations to monitor. */
        Destination[] _destinations = null;

        /**
         * Element for the cache.
         */
        class CacheElement {
            /** The status. */
            int status;

            /** The time. */
            long time;
        }

        /** Cache of status per Destination. */
        final Map<String, CacheElement> _cache = new ConcurrentHashMap<>();

        /** Allow stopping the Thread. */
        boolean _run = true;

        /**
         * Add Monitor subscriptions and synchronise OpsView.
         */
        private void _subscribe() {
            // Add Monitor subscriptions for all monitored Destinations
            final List<Destination> toMonitor = new ArrayList<>();
            for (final Destination destination : _destinations) {
                if (destination.getMonitor()) {
                    final var destinationName = destination.getName();
                    try {
                        MonitorThread.getInstance().subscribe(destinationName, this);
                        toMonitor.add(destination);
                    } catch (final MonitorException e) {
                        _log.error("subscribing " + destinationName + " to Monitor", e);
                    }
                }
            }
            // Synchronize OpsView with the list of monitored Destinations!
            if (OpsViewManager.isActivated()) {
                try {
                    OpsViewManager.sync(toMonitor.toArray(new Destination[toMonitor.size()]));
                } catch (final Throwable t) {
                    _log.error("synchronizing with opsview", t);
                }
            }
        }

        /**
         * Remove Monitor subscriptions for all Destinations.
         */
        private void _unSubscribe() {
            try {
                MonitorThread.getInstance().unSubscribeAll(Destination.class);
            } catch (final MonitorException e) {
                _log.debug(e);
            }
        }

        /**
         * Remove Monitor subscription for on Destination.
         *
         * @param name
         *            the name
         */
        public void unSubscribe(final String name) {
            try {
                MonitorThread.getInstance().unSubscribe(name);
            } catch (final MonitorException e) {
                _log.debug(e);
            }
        }

        /**
         * Gets the monitor manager.
         *
         * @param name
         *            the name
         *
         * @return the monitor manager
         *
         * @throws MonitorException
         *             the monitor exception
         */
        /*
         * Get the status to display for the specified Destination name. If the status is back to green we do not send
         * it before 10 minutes to avoid flapping on the OpsView monitoring and to avoid sending too many emails.
         *
         * @see ecmwf.common.monitor.MonitorCallback#getMonitorManager(java.lang. String)
         */
        @Override
        public MonitorManager getMonitorManager(final String name) throws MonitorException {
            // What is the real status of the Destination?
            final var real = _getMonitorManager(name);
            if (Cnf.at("TransferScheduler", "monitoringCache", true) && real != null) {
                // We found something!
                if (real.isGreen()) {
                    // The current status is green so let's check if the
                    // previous status was not something else?
                    final var element = _cache.get(name);
                    if (element != null) {
                        // It is in the cache so let's check if a successful
                        // transfer was done after it was added to the cache?
                        final var thread = _threads.get(name);
                        if (thread != null) {
                            // The Destination is running!
                            DataTransfer transfer;
                            Timestamp finishTime;
                            if ((transfer = thread.getLastTransfer()) != null
                                    && (finishTime = transfer.getFinishTime()) != null
                                    && finishTime.getTime() > element.time) {
                                // The transfer completed after the last
                                // not-green update so we assume the Destination
                                // is OK now!
                                _log.debug("Last DataTransfer successful for " + name + " (" + transfer.getId()
                                        + ") => status=green");
                                _cache.remove(name);
                            } else {
                                _log.debug("Destination " + name + " has no recent successful DataTransfer => status="
                                        + MonitorManager.getColor(element.status));
                                real.setStatus(element.status);
                            }
                        } else // The Destination is not running anymore so let's
                        // check if the time has expired?
                        if (System.currentTimeMillis() - element.time < Cnf.at("TransferScheduler", "monitoringTimeout",
                                10) * Timer.ONE_MINUTE) {
                            // The previous status was not green in the last
                            // 10 minutes so we don't want to update the
                            // status yet to OpsView as it might go back to
                            // something else soon. So we keep the previous
                            // status!
                            _log.debug("Destination " + name + " NOT green for enough time => status="
                                    + MonitorManager.getColor(element.status));
                            real.setStatus(element.status);
                        } else {
                            // It is expired so let's remove it from the
                            // cache and use the real status!
                            _log.debug("Destination " + name + " green for enough time => status=green");
                            _cache.remove(name);
                        }
                    }
                } else {
                    // It is not green so we have to record/update it in the
                    // cache for the following requests!
                    final var element = new CacheElement();
                    element.time = System.currentTimeMillis();
                    element.status = real.getStatus();
                    _cache.put(name, element);
                }
            }
            return real;
        }

        /**
         * Get the latest status for the Destination.
         *
         * @param destinationName
         *            the Destination name
         *
         * @return the monitor manager
         *
         * @throws MonitorException
         *             the monitor exception
         */
        private MonitorManager _getMonitorManager(final String destinationName) throws MonitorException {
            final String statusCode;
            try {
                statusCode = getDestinationStatus(destinationName);
            } catch (final DataBaseException e) {
                throw new MonitorException(e.getMessage());
            }
            final var thread = _threads.get(destinationName);
            String context = null;
            var type = -1;
            if (thread != null) {
                final var destination = thread.getDestination();
                if (destination != null) {
                    type = destination.getType();
                }
                final DataTransfer transfer;
                if ((StatusFactory.DONE.equals(statusCode) || StatusFactory.IDLE.equals(statusCode))
                        && (transfer = thread.getLastFailedTransfer()) != null) {
                    context = "Last failed transfer is " + transfer.getId();
                } else if (destination != null && StatusFactory.HOLD.equals(statusCode)) {
                    final var userStatus = destination.getUserStatus();
                    context = userStatus != null ? "By " + userStatus : null;
                }
            }
            if (type == -1) {
                final var destination = BASE.getDestinationObject(destinationName);
                if (destination != null) {
                    type = destination.getType();
                }
            }
            int status;
            // The name is one of the FILTER defined in OpsViewManager!
            final var result = new MonitorManager(
                    DestinationOption.isAcquisition(type) ? OpsViewManager.ACQUISITION_FILTER_NAME
                            : DestinationOption.isDissemination(type)
                                    ? OpsViewManager.DISSEMINATION_FILTER_NAME : OpsViewManager.OTHER_FILTER_NAME,
                    "Destination: " + destinationName,
                    status = StatusFactory.WAIT.equals(statusCode) || StatusFactory.SCHE.equals(statusCode)
                            ? MonitorManager.BLUE
                            : StatusFactory.STOP.equals(statusCode) ? MonitorManager.YELLOW
                                    : StatusFactory.HOLD.equals(statusCode) || StatusFactory.FAIL.equals(statusCode)
                                            || StatusFactory.DONE.equals(statusCode)
                                            || StatusFactory.IDLE.equals(statusCode) ? MonitorManager.RED
                                                    : MonitorManager.GREEN,
                    StatusFactory.getDestinationStatusName(statusCode) + (isNotEmpty(context) ? " - " + context : ""));
            if (status == MonitorManager.BLUE || status == MonitorManager.GREEN) {
                try {
                    final var destination = BASE.getDestination(destinationName);
                    // Check if the destination was not idle for more than the
                    // max activity
                    final var maxInactivity = destination.getMaxInactivity();
                    if (maxInactivity > 0) {
                        try {
                            if (System.currentTimeMillis() - getDestinationLastTransfer(destinationName, true)
                                    .getFinishTime().getTime() > maxInactivity) {
                                result.setStatus(status = MonitorManager.RED);
                                result.setComment(
                                        "No activity detected for more than " + Format.formatDuration(maxInactivity));
                            }
                        } catch (final Throwable t) {
                            // There might be no DataTransfer or no FinishTime
                            // set
                        }
                    }
                    if (status != MonitorManager.RED) {
                        final var size = BASE.getBadDataTransfersByDestinationCount(destinationName);
                        if (size > 0) {
                            // Outstanding files are there
                            result.setStatus(MonitorManager.YELLOW);
                            result.setComment(StatusFactory.getDestinationStatusName(statusCode) + " - " + size
                                    + " outstanding transfer(s)");
                        } else {
                            final var theStatusCode = destination.getStatusCode();
                            if (StatusFactory.HOLD.equals(theStatusCode)) {
                                final var userStatus = destination.getUserStatus();
                                result.setStatus(MonitorManager.YELLOW);
                                result.setComment(StatusFactory.getDestinationStatusName(theStatusCode)
                                        + (userStatus != null ? " - By " + userStatus : ""));
                            }
                        }
                    }
                } catch (final DataBaseException e) {
                    _log.warn(e);
                }
            }
            return result;
        }

        /**
         * Shutdown.
         */
        public void shutdown() {
            _run = false;
            wakeup();
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            while (_run) {
                if (_destinations != null) {
                    _unSubscribe();
                }
                _destinations = BASE.getDestinationArray();
                _subscribe();
                waitFor(Cnf.durationAt("TransferScheduler", "monitoringSubscribeFrequency", 10 * Timer.ONE_MINUTE));
            }
            _unSubscribe();
        }
    }

    /**
     * Gets the transfer server list as string. Allow displaying the list of TransferServers!
     *
     * @param servers
     *            the servers
     *
     * @return the string
     */
    private static String _getTransferServerListAsString(final List<TransferServer> servers) {
        // Let's build a string with the list of TransferServers!
        final var serverList = new StringBuilder();
        for (final TransferServer ts : servers) {
            final var name = ts.getName();
            serverList.append(serverList.length() > 0 ? "," : "").append(name);
        }
        return serverList.toString();
    }

    /**
     * The Class DestinationThread.
     */
    public final class DestinationThread extends MBeanRepository<DataTransfer> {
        /** The _destination. */
        private Destination _destination = null;

        /** The _value. */
        private final SchedulerValue _value;

        /** The _provider. */
        private final HostProvider _provider;

        /** The _destination step. */
        private int _destinationStep = DestinationStep.DESTINATION_STEP_INIT;

        /** The _current transfer. */
        private DataTransfer _currentTransfer = null;

        /** The _inactivity. */
        private long _inactivity = -1;

        /** The _selected. */
        private int _selected = 0;

        /** The _performed. */
        private int _performed = 0;

        /** The _reset. */
        private boolean _reset = false;

        /** The _re schedule. */
        private boolean _reSchedule = false;

        /** The _transfers. */
        private final List<DataTransfer> _transfers = new ArrayList<>();

        /** The _was updated. */
        private final List<Long> _wasUpdated = Collections.synchronizedList(new ArrayList<>());

        /** The _shutdown. */
        private ShutdownThread _shutdown = null;

        /**
         * Instantiates a new destination thread.
         *
         * @param destinationName
         *            the destination name
         *
         * @throws NoHostException
         *             the no host exception
         * @throws DataBaseException
         *             the data base exception
         */
        DestinationThread(final String destinationName) throws NoHostException, DataBaseException {
            super("Destination:destination", destinationName);
            var started = false;
            deactivateMonitor();
            try {
                setComparator(new TransferComparator(true));
                setDelay(Cnf.durationAt("Scheduler", "destinationThread", Timer.ONE_SECOND));
                setJammedTimeout(Cnf.durationAt("Scheduler", "destinationThreadJammedTimeout", 5 * Timer.ONE_MINUTE));
                _destination = BASE.getDestination(destinationName);
                setTimeRanges(DESTINATION_SCHEDULER.getECtransSetup(_destination.getData())
                        .getTimeRangeList(DESTINATION_SCHEDULER_ACTIVE_TIME_RANGE));
                _value = _destination.getSchedulerValue();
                _provider = new HostProvider(this);
                start();
                started = true;
            } finally {
                if (!started) {
                    close();
                }
            }
        }

        /**
         * Gets the destination.
         *
         * @return the destination
         */
        Destination getDestination() {
            return _destination;
        }

        /**
         * Gets the scheduler value.
         *
         * @return the scheduler value
         */
        SchedulerValue getSchedulerValue() {
            return _value;
        }

        /**
         * Gets the host.
         *
         * @return the host
         */
        Host getHost() {
            return _provider._host.getHost();
        }

        /**
         * Get the list of TransferServers which can be used to transmit this DataTransfer. If the DataTransfer was
         * already retrieved on one TransferServer then move it at the top of the list.
         *
         * @param transfer
         *            the transfer
         *
         * @return the transfer servers
         *
         * @param transfer
         *            the transfer
         *
         * @return the transfer servers
         *
         * @throws DataBaseException
         *             the data base exception
         * @throws TransferServerException
         */
        TransferServer[] getTransferServers(final DataTransfer transfer)
                throws DataBaseException, TransferServerException {
            // What's the TransferGroup for the current Host?
            var targetGroup = getHost().getTransferGroup();
            // What's the TransferGroup for the DataFile?
            final var dataFile = transfer.getDataFile();
            final var originalGroup = dataFile.getTransferGroup();
            // Are they part of the same Cluster?
            final var clusterName = targetGroup.getClusterName();
            // If the cluster is not defined (empty string) then we cannot
            // compare!
            if (originalGroup != null && !clusterName.isEmpty() && clusterName.equals(originalGroup.getClusterName())
                    && !targetGroup.getName().equals(originalGroup.getName())) {
                // They are part of the same Cluster so if they are different
                // let's force the usage of the original TransferGroup!
                _log.debug("Force usage of the original TransferGroup ({} => {})", targetGroup.getName(),
                        originalGroup.getName());
                targetGroup = originalGroup;
            }
            // Get the list of TransferServers for the selected TargetGroup
            final var servers = TransferServerProvider.getTransferServersByLeastActivity("DestinationThread",
                    transfer.getDestinationName(), targetGroup);
            final var retries = transfer.getRequeueCount();
            if (retries == 0 && transfer.getFailedTime() == null) {
                // This is the first time we are trying to send this
                // DataTransfer (no retry or failed attempt)
                final var getHost = getTransferServerUsedForRetrieval(transfer);
                if (getHost == null) {
                    // This DataTransfer was not retrieved yet on any
                    // TransferServer so we can use any of them
                    _log.debug("No retrieval for DataTransfer-" + transfer.getId() + ": use any DataMover -> "
                            + _getTransferServerListAsString(servers));
                } else {
                    // We are going to move at the top of the list the
                    // TransferServer which was used to retrieve the file
                    for (var i = 0; i < servers.size(); i++) {
                        final var server = servers.get(i);
                        if (server.getName().equals(getHost)) {
                            if (i > 0) {
                                // It is not the first position so we have to
                                // move it!
                                servers.remove(i);
                                servers.add(0, server);
                            }
                            break;
                        }
                    }
                    _log.debug("No retry for DataTransfer-" + transfer.getId() + ": use original DataMover (" + getHost
                            + ") -> " + _getTransferServerListAsString(servers));
                }
            } else {
                // There was already some retries for this DataTransfer so let's
                // use any of the TransferServer to give them a chance!
                Collections.shuffle(servers, ThreadLocalRandom.current());
                _log.debug(retries + " retries for DataTransfer-" + transfer.getId() + ": use random DataMover -> "
                        + _getTransferServerListAsString(servers));
            }
            return servers.toArray(new TransferServer[servers.size()]);
        }

        /**
         * Gets the destination status code.
         *
         * @return the destination status code
         */
        String getDestinationStatusCode() {
            if (!isRunning()) {
                return StatusFactory.WAIT.equals(_destination.getStatusCode()) ? StatusFactory.RSTR
                        : StatusFactory.RETR;
            }
            if (_provider._waiting) {
                return StatusFactory.IDLE;
            }
            final var transfer = getLastFailedTransfer();
            if (transfer != null && StatusFactory.EXEC.equals(transfer.getStatusCode())) {
                return StatusFactory.DONE;
            }
            return StatusFactory.EXEC;
        }

        /**
         * _log.
         *
         * @param transfer
         *            the transfer
         * @param message
         *            the message
         */
        private void _log(final DataTransfer transfer, final String message) {
            if ("failed".equals(message) && _splunk.isInfoEnabled()) {
                // This is an error so if required we record it in Splunk!
                final var destination = transfer.getDestination();
                final var dataFile = transfer.getDataFile();
                final var host = transfer.getHost();
                _splunk.info("ERR;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                        "TimeStamp=" + Timestamp.from(Instant.now()), "Monitored=" + destination.getMonitor(),
                        "DataTransferId=" + transfer.getId(), "DestinationName=" + destination.getName(),
                        "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                        "FileName=" + transfer.getTarget(), "FileSize=" + dataFile.getSize(),
                        "ScheduledTime=" + transfer.getScheduledTime(), "StartTime=" + transfer.getStartTime(),
                        "MetaStream=" + nullToNone(dataFile.getMetaStream()),
                        "MetaType=" + nullToNone(dataFile.getMetaType()), "MetaTime=" + dataFile.getMetaTime(),
                        "TimeBase=" + transfer.getTimeBase(), "TimeStep=" + transfer.getTimeStep(),
                        "Duration=" + transfer.getDuration(), "UserId=" + host.getLogin(),
                        "CountryCode=" + destination.getCountryIso(), "BytesSent=" + transfer.getSent(),
                        "TransferProtocol=" + host.getTransferMethod().getECtransModuleName(),
                        "TransferServer=" + transfer.getTransferServerName(), "HostAddress=" + host.getHost(),
                        "Message=" + transfer.getComment());
            }
            final var template = Cnf.at("Operator", "template");
            if (template != null && _ops.isInfoEnabled()) {
                try {
                    _ops.info(TransferManagement.getTargetName(transfer, template, message));
                } catch (final Throwable t) {
                    _log.warn("Writting operator logs", t);
                }
            }
        }

        /**
         * Update count.
         *
         * @param transfer
         *            the transfer
         * @param start
         *            the start
         */
        void updateCount(final DataTransfer transfer, final boolean start) {
            final var code = transfer.getStatusCode();
            final var stop = StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code);
            final var retry = StatusFactory.RETR.equals(code) || StatusFactory.INTR.equals(code);
            final var done = StatusFactory.DONE.equals(code);
            if (stop || retry) {
                _setLastFailedTransfer(transfer);
                _update(transfer, true);
            } else if (done) {
                _value.setLastTransferOk(transfer.getId());
                if (_value.getHasRequeued() && (transfer.getStartCount() > 0 || transfer.getRequeueHistory() > 0)) {
                    _log.info("Destination need reschedule");
                    _value.setHasRequeued(false);
                    _reSchedule = true;
                    reset();
                }
            }
            if (start || retry || stop || done) {
                final var action = start ? "started" : retry || stop ? "failed" : "completed";
                _log.debug("DataTransfer " + transfer.getId() + " " + action + " on " + transfer.getTransferServerName()
                        + " from " + transfer.getDestinationName() + " (" + transfer.getStatusCode() + ")");
                _log(transfer, action);
            }
            if (stop) {
                if (_debug) {
                    _log.debug("Removing DataTransfer " + transfer.getId() + " from queue");
                }
                removeValue(transfer);
            } else if (retry) {
                if (!isAcquisition(transfer)) {
                    MASTER.addTransferHistory(transfer, StatusFactory.STOP, transfer.getComment());
                    MASTER.addTransferHistory(transfer, StatusFactory.RETR,
                            "Requeued by the scheduler after a transfer failure on DataMover="
                                    + transfer.getTransferServerName() + " (S:" + transfer.getStartCount() + ",R:"
                                    + transfer.getRequeueHistory() + ")");
                } else {
                    // This is acquisition (the transfer should not be
                    // retried)
                    MASTER.addTransferHistory(transfer, StatusFactory.FAIL, transfer.getComment());
                }
                transfer.setComment(null);
                put(transfer);
            } else {
                if (StatusFactory.INIT.equals(code)) {
                    // Transfer of previous instance was interrupted!
                    final var instance = transfer.getDataFile().getFileInstance();
                    MASTER.addTransferHistory(transfer, StatusFactory.RETR,
                            "Requeued by the scheduler on a new file instance"
                                    + (instance != null ? " (" + instance + ")" : ""));
                } else if (StatusFactory.SCHE.equals(code) || StatusFactory.FETC.equals(code)) {
                    _log.warn("Unexpected status for DataTransfer " + transfer.getId());
                }
            }
            _provider.update(transfer, start);
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the key
         */
        @Override
        public String getKey(final DataTransfer transfer) {
            return Format.formatLong(transfer.getId(), 10, true);
        }

        /**
         * Gets the data transfer.
         *
         * @param id
         *            the id
         *
         * @return the data transfer
         */
        DataTransfer getDataTransfer(final long id) {
            return getValue(Format.formatLong(id, 10, true));
        }

        /**
         * Gets the last transfer.
         *
         * @return the last transfer
         */
        public DataTransfer getLastTransfer() {
            final var transferOk = _value.getLastTransferOk();
            final var transfer = transferOk != null ? MASTER.getDataTransfer(transferOk) : null;
            return transfer != null && transfer.getDeleted() ? null : transfer;
        }

        /**
         * Gets the next transfer.
         *
         * @return the next transfer
         */
        public DataTransfer getNextTransfer() {
            return _currentTransfer;
        }

        /**
         * Gets the last failed transfer.
         *
         * @return the last failed transfer
         */
        public DataTransfer getLastFailedTransfer() {
            final var transferKo = _value.getLastTransferKo();
            final var transfer = transferKo != null ? MASTER.getDataTransfer(transferKo) : null;
            return transfer != null && transfer.getDeleted() ? null : transfer;
        }

        /**
         * Sets the last failed transfer.
         *
         * @param transfer
         *            the transfer
         */
        private void _setLastFailedTransfer(final DataTransfer transfer) {
            transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
            _log.debug("Set LastTransferKo for " + _destination.getName() + ": " + transfer.getId() + " ("
                    + Format.formatTime(transfer.getFailedTime().getTime()) + ")");
            _value.setLastTransferKo(transfer.getId());
        }

        /**
         * Gets the status.
         *
         * @param transfer
         *            the transfer
         *
         * @return the status
         */
        @Override
        public String getStatus(final DataTransfer transfer) {
            final var code = transfer.getStatusCode();
            final var size = transfer.getDataFile().getSize();
            return code + "("
                    + (size == 0 ? StatusFactory.DONE.equals(code) ? 100 : 0 : (int) (transfer.getSent() * 100 / size))
                    + "%)";
        }

        /**
         * Shutdown.
         */
        @Override
        public void shutdown() {
            shutdown(0, false, "main shutdown");
        }

        /**
         * Shutdown.
         *
         * @param timeout
         *            the timeout
         * @param reset
         *            the reset
         * @param comment
         *            the comment
         */
        public synchronized void shutdown(final long timeout, final boolean reset, final String comment) {
            if (_shutdown == null) {
                _log.info("Starting " + (timeout > 0 ? "graceful" : "immediate") + " Destination shutdown"
                        + (comment != null ? " (" + comment + ")" : ""));
                super.shutdown();
                _shutdown = new ShutdownThread();
                _shutdown.init(timeout, reset, comment);
                _shutdown.execute();
            } else {
                _log.info("Destination already in shutdown mode");
                _shutdown.init(timeout, reset, comment);
            }
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return _provider.toString();
        }

        /**
         * Gets the destination step.
         *
         * @return the destination step
         */
        public int getDestinationStep() {
            return _destinationStep;
        }

        /**
         * Signal is should be reset.
         *
         * @return true, if successful
         */
        public boolean toBeReseted() {
            return _provider.toBeReseted();
        }

        /**
         * Reset.
         */
        public void reset() {
            _reset = true;
        }

        /**
         * Load pending data transfers.
         */
        public void loadPendingDataTransfers() {
            final Collection<DataTransfer> transfers = BASE.getPendingDataTransfers(_destination, _queueSize);
            synchronized (_transfers) {
                _transfers.clear();
                _transfers.addAll(transfers);
            }
        }

        /**
         * _re-schedule.
         */
        private void _reSchedule() {
            if (_reSchedule) {
                _reSchedule = false;
                _log.info("Re-schedule DataTransfers to original times");
                try {
                    final var affectedRows = BASE.resetRequeuedTransfersPerDestination(_destination);
                    _log.info(affectedRows + " DataTransfer(s) updated");
                } catch (final Exception e) {
                    _log.error("Rescheduling", e);
                }
            }
        }

        /**
         * _reload.
         */
        private void _reload() {
            final List<DataTransfer> transfers = new ArrayList<>();
            synchronized (_transfers) {
                transfers.addAll(_transfers);
                _transfers.clear();
            }
            if (_reset) {
                _reset = false;
                _performed = 0;
                final var transfersList = getList();
                if (!transfersList.isEmpty()) {
                    var performed = 0;
                    for (final DataTransfer transfer : transfersList) {
                        if (!StatusFactory.EXEC.equals(transfer.getStatusCode())) {
                            removeValue(transfer);
                            performed++;
                        }
                    }
                    _log.debug("Removed " + performed + " DataTransfer(s) from the " + _destination.getName()
                            + " cache (reset)");
                }
                return;
            }
            _reSchedule();
            try {
                _selected = transfers.size();
                _performed = 0;
                for (final DataTransfer transfer : transfers) {
                    final var transferId = transfer.getId();
                    if (_wasUpdated.contains(transferId)) {
                        continue;
                    }
                    final var code = transfer.getStatusCode();
                    final var wait = StatusFactory.WAIT.equals(code) || StatusFactory.RETR.equals(code);
                    final var inTime = transfer.getRetryTime().getTime() <= System.currentTimeMillis();
                    final var inQueue = containsValue(transfer);
                    final var inCache = MASTER.getDataTransferFromCache(transferId) != null;
                    DataTransfer fromCache = null;
                    if (inTime && !inQueue && (wait || !inCache)
                            && (fromCache = MASTER.getDataTransfer(transferId)) != null
                            && fromCache.getStatusCode().equals(code)) {
                        if (_debug) {
                            _log.debug("Add transfer " + transferId + " to the cache (" + code + ")");
                        }
                        put(fromCache);
                        _performed++;
                    } else if (_debug) {
                        _log.debug("Transfer: " + transferId + " (" + transfer.getStatusCode() + ") (code=" + code
                                + ",inQueue=" + inQueue + ",inCache=" + inCache + ",inTime=" + inTime + ")");
                    }
                }
                if (_performed > 0) {
                    wakeup();
                    _log.debug("Added " + _performed + " transfer(s) to the " + _destination.getName() + " cache");
                }
            } catch (final Exception e) {
                _log.warn("Reloading DataTransfer(s)", e);
            } finally {
                _wasUpdated.clear();
            }
        }

        /**
         * _update.
         *
         * @param transfer
         *            the transfer
         * @param remove
         *            the remove
         */
        private void _update(final DataTransfer transfer, final boolean remove) {
            _wasUpdated.add(transfer.getId());
            if (remove) {
                MASTER.removeDataTransfer(transfer);
                removeValue(transfer);
            }
            _updateDataTransfer(transfer);
        }

        /**
         * Initialize.
         */
        @Override
        public void initialize() {
            for (final DataTransfer transfer : BASE.getInterruptedTransfersPerDestination(_destination)) {
                final var code = transfer.getStatusCode();
                final var userName = _destination.getUserStatus();
                _log.info("Transfer " + transfer.getId() + " requeued (" + code + ")");
                if (!StatusFactory.INTR.equals(code)) {
                    transfer.setComment("Interrupted by Destination shutdown"
                            + (userName != null ? " initiated by WebUser=" + userName : ""));
                    MASTER.addTransferHistory(transfer, StatusFactory.INTR, transfer.getComment());
                }
                transfer.setComment("Requeued by the scheduler after a Destination restart"
                        + (userName != null ? " (" + userName + ")" : ""));
                transfer.setStatusCode(StatusFactory.RETR);
                transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                transfer.setUserStatus(null);
                MASTER.addTransferHistory(transfer);
                _updateDataTransfer(transfer);
            }
            loadPendingDataTransfers();
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            _destinationStep = DestinationStep.DESTINATION_STEP_PROCESS_RUN;
            _reload();
            final var transfersList = getList();
            if (transfersList.size() == 0) {
                _destinationStep = DestinationStep.DESTINATION_STEP_NO_TRANSFER;
                if (_provider.getTransferCount() == 0) {
                    if (_inactivity == -1) {
                        _inactivity = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - _inactivity > 5 * Timer.ONE_MINUTE) {
                        shutdown(0, false, "no transfers to process");
                    }
                }
                return NEXT_STEP_DELAY;
            }
            _inactivity = -1;
            MASTER.checkPendingTicket(_currentTransfer = transfersList.get(0), true);
            final var transferStatus = _currentTransfer.getStatusCode();
            final var hostForSourceName = _currentTransfer.getDataFile().getHostForAcquisitionName();
            final var acquisition = isAcquisition(_currentTransfer);
            final var currentTime = System.currentTimeMillis();
            final var expiryTime = _currentTransfer.getExpiryTime().getTime();
            final var userStatus = _currentTransfer.getUserStatus();
            if (!StatusFactory.EXEC.equals(transferStatus) && expiryTime < currentTime) {
                _log.info("Stopping DataTransfer " + _currentTransfer.getId() + " (expired)");
                _currentTransfer.setStatusCode(StatusFactory.FAIL);
                _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                _currentTransfer.setComment("File not transferred before expiration");
                MASTER.addTransferHistory(_currentTransfer);
                _setLastFailedTransfer(_currentTransfer);
                _update(_currentTransfer, true);
                return NEXT_STEP_CONTINUE;
            }
            if (StatusFactory.RETR.equals(transferStatus) && acquisition) {
                // Was this file already downloaded by the DownloadScheduler?
                if (!isNotEmpty(userStatus) && !_currentTransfer.getDataFile().getDownloaded()) {
                    // The file was not downloaded so the source acquisition
                    // Host was configured to not retrieve the file on the
                    // data movers.
                    final Host host = getHost();
                    if (host == null || !HOST_ACQUISITION.getECtransSetup(host.getData())
                            .getBoolean(HOST_ACQUISITION_REQUEUE_ON_FAILURE)) {
                        // In this case we don't want to restart
                        // the transmission as it should be forced only by a
                        // re-queue from the acquisition Destination!
                        _log.info("Cancelling retry for DataTransfer " + _currentTransfer.getId()
                                + " (not found on remote site)");
                        _currentTransfer.setStatusCode(StatusFactory.FAIL);
                        // The original error was already set in the comment in
                        // the last history message!
                        _currentTransfer.setComment(
                                "No automatic retry (manual requeue or rediscovery from Acquisition Scheduler required)");
                        _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                        MASTER.addTransferHistory(_currentTransfer);
                        _setLastFailedTransfer(_currentTransfer);
                        _update(_currentTransfer, true);
                        return NEXT_STEP_CONTINUE;
                    }
                }
            }
            final var maxStart = _destination.getMaxStart();
            final var queueTime = _currentTransfer.getQueueTime().getTime();
            var delayed = false;
            if (StatusFactory.HOLD.equals(transferStatus) || StatusFactory.FAIL.equals(transferStatus)
                    || StatusFactory.STOP.equals(transferStatus) || (delayed = queueTime > currentTime)) {
                removeValue(_currentTransfer);
                if (delayed) {
                    _log.debug("DataTransfer " + _currentTransfer.getId() + " was delayed (next start: "
                            + Format.formatTime(queueTime) + ")");
                }
                return NEXT_STEP_CONTINUE;
            }
            if (!_provider.available()) {
                _destinationStep = DestinationStep.DESTINATION_STEP_NO_PROVIDER;
                return NEXT_STEP_DELAY;
            }
            if (maxStart > 0 && _currentTransfer.getStartCount() >= maxStart) {
                _destinationStep = DestinationStep.DESTINATION_STEP_PROCESS_DELAY;
                _currentTransfer.setStartCount(0);
                _currentTransfer.setRequeueHistory(_currentTransfer.getRequeueHistory() + 1);
                _currentTransfer.setRequeueCount(_currentTransfer.getRequeueCount() + 1);
                final long maxRequeue = _destination.getMaxRequeue();
                if (maxRequeue > 0 && _currentTransfer.getRequeueCount() >= maxRequeue) {
                    _log.info("Stopping DataTransfer " + _currentTransfer.getId() + " (maximum requeue limit reached)");
                    _currentTransfer.setStatusCode(StatusFactory.FAIL);
                    _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                    _currentTransfer
                            .setComment("Maximum requeue limit reached (stopped after " + maxRequeue + " attempt(s))");
                } else {
                    final var nextStart = System.currentTimeMillis() + _destination.getStartFrequency();
                    _log.info("Delaying DataTransfer " + _currentTransfer.getId() + " (next start: "
                            + Format.formatTime(nextStart) + ")");
                    _currentTransfer.setStatusCode(StatusFactory.RETR);
                    _currentTransfer.setFinishTime(null);
                    _currentTransfer.setComment("Maximum start limit reached (delayed until "
                            + Format.formatTime("MMM dd HH:mm:ss", nextStart) + ")");
                    _currentTransfer.setQueueTime(new Timestamp(nextStart));
                    _value.setHasRequeued(true);
                }
                MASTER.addTransferHistory(_currentTransfer);
                _setLastFailedTransfer(_currentTransfer);
                _update(_currentTransfer, true);
                return NEXT_STEP_CONTINUE;
            }
            var retry = false;
            if (StatusFactory.RETR.equals(_currentTransfer.getStatusCode())) {
                _destinationStep = DestinationStep.DESTINATION_STEP_PROCESS_RETR;
                if ((_currentTransfer.getHost() != null) && (!_provider.next(_currentTransfer) || !isRunning())) {
                    return NEXT_STEP_CONTINUE;
                }
                _log.info("Retrying DataTransfer " + _currentTransfer.getId());
                if (isNotEmpty(userStatus) && acquisition)
                    // The retry was triggered by a user action (e.g. re-queue)
                    // Make sure it will not be triggered again unless the file
                    // is already on the data movers!
                    _currentTransfer.setUserStatus(null);
                _currentTransfer.setStatusCode(StatusFactory.WAIT);
                _currentTransfer.setFinishTime(null);
                _currentTransfer.setComment(null);
                _currentTransfer.setDuration(0);
                _currentTransfer.setSent(0);
                _currentTransfer.setPutTime(null);
                // It is a retry so if we are requested to check duplicates
                // we should know this one is a candidate if all others are
                // equals!
                retry = true;
            }
            if (StatusFactory.INTR.equals(_currentTransfer.getStatusCode())) {
                _destinationStep = DestinationStep.DESTINATION_STEP_PROCESS_INTR;
                _log.info("Restarting DataTransfer " + _currentTransfer.getId());
                _currentTransfer.setStatusCode(StatusFactory.WAIT);
            }
            if (_currentTransfer.getUserStatus() == null
                    && StatusFactory.WAIT.equals(_currentTransfer.getStatusCode())) {
                // The file was not requeued manually so let's check if it has
                // no duplicates if requested to do that?
                final var setup = DESTINATION_SCHEDULER.getECtransSetup(_destination.getData());
                final var rule = setup.getString(DESTINATION_SCHEDULER_REQUEUEON);
                final var target = _currentTransfer.getTarget();
                final DataTransfer duplicate;
                if (isNotEmpty(rule) && setup.matches(DESTINATION_SCHEDULER_REQUEUEPATTERN, target)
                        && !setup.matches(DESTINATION_SCHEDULER_REQUEUEIGNORE, target)) {
                    _log.debug("Checking against rule: {}", rule);
                    if ((duplicate = _requeueOn(retry, rule)) != null) {
                        // This DataTransfer filename was already queued with a
                        // different id and this other file should be used
                        // rather than the current one, so let's hold the
                        // current DataTransfer!
                        _log.info("Holding DataTransfer " + _currentTransfer.getId() + " (duplicate file DataTransfer-"
                                + duplicate.getId() + " found)");
                        _currentTransfer.setStatusCode(StatusFactory.HOLD);
                        _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                        _currentTransfer.setComment("Duplicate file found dated "
                                + Format.formatTime(duplicate.getScheduledTime()) + " sized " + duplicate.getSize()
                                + " bytes (DataTransferId=" + duplicate.getId() + ") with rule '" + rule + "' false");
                        MASTER.addTransferHistory(_currentTransfer);
                        _update(_currentTransfer, true);
                        return NEXT_STEP_CONTINUE;
                    }
                    _log.debug("No duplicate found");
                }
            }
            if (StatusFactory.WAIT.equals(_currentTransfer.getStatusCode())) {
                _destinationStep = DestinationStep.DESTINATION_STEP_PROCESS_WAIT;
                _log.info("Processing DataTransfer " + _currentTransfer.getId() + " (startCount="
                        + _currentTransfer.getStartCount() + ",retryTime="
                        + Format.formatTime(_currentTransfer.getRetryTime().getTime()) + ")");
                _currentTransfer.setStatusCode(StatusFactory.EXEC);
                _currentTransfer.setFinishTime(null);
                _currentTransfer.setComment(null);
                _currentTransfer.setDuration(0);
                _currentTransfer.setSent(0);
                _currentTransfer.setPutTime(null);
                var updateCount = false;
                try {
                    final var host = _provider.getHost();
                    _currentTransfer.setHost(host);
                    _currentTransfer.setHostName(host.getName());
                    _currentTransfer.setStartCount(_currentTransfer.getStartCount() + 1);
                    _update(_currentTransfer, false);
                    if (_destination.getMailOnStart()) {
                        MASTER.sendECpdsMessage(_currentTransfer);
                    }
                    final var current = new Timestamp(System.currentTimeMillis());
                    _currentTransfer.setRetryTime(current);
                    if (_currentTransfer.getStartTime() == null) {
                        _currentTransfer.setStartTime(current);
                    }
                    final var servers = getTransferServers(_currentTransfer);
                    if (servers.length > 0) {
                        final var server = servers[0];
                        _currentTransfer.setTransferServer(server);
                        _currentTransfer.setTransferServerName(server.getName());
                    }
                    if (_currentTransfer.getTransferServer() == null) {
                        final var error = "No Transfer Server available (Transfer Group might be empty?)";
                        _log.warn("DataTransfer " + _currentTransfer.getId() + " delayed: " + error);
                        _currentTransfer.setComment(error);
                        _currentTransfer.setStatusCode(StatusFactory.RETR);
                        _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                        _setLastFailedTransfer(_currentTransfer);
                        MASTER.addTransferHistory(_currentTransfer, StatusFactory.STOP, error);
                        MASTER.addTransferHistory(_currentTransfer, StatusFactory.RETR,
                                "Requeued by the scheduler because no Transfer Server was available");
                        _update(_currentTransfer, true);
                        return NEXT_STEP_DELAY;
                    }
                    MASTER.addDataTransfer(_currentTransfer);
                    MASTER.addTransferHistory(_currentTransfer);
                    updateCount(_currentTransfer, true);
                    updateCount = true;
                    // Find the host for source (does it comes from the
                    // acquisition or the ecpds command?)
                    final var hostForSource = acquisition ? BASE.getHost(hostForSourceName)
                            : _destination.getHostForSource();
                    // Do we have a proxy configured for this Destination? Is it
                    // the right one? (e.g. a proxy might have been removed
                    // since the queueing)!
                    final var proxyHost = _currentTransfer.getProxyHost();
                    if (proxyHost != null) {
                        final var proxyId = proxyHost.getName();
                        final List<String> proxies = new ArrayList<>();
                        for (final Host proxy : BASE.getDestinationHost(_destination, HostOption.PROXY)) {
                            proxies.add(proxy.getName());
                        }
                        if (!proxies.contains(proxyId)) {
                            _log.warn("ProxyHost " + proxyId + " removed for DataTransfer " + _currentTransfer.getId()
                                    + " (not associated with Destination)");
                            _currentTransfer.setProxyHost(null);
                        }
                    }
                    // Transfer the data transfer
                    if (!(servers.length > 0 && TransferScheduler.put(servers, _currentTransfer, hostForSource))) {
                        final var error = servers.length > 0 ? "Transfer request failed on all DataMover(s)"
                                : "No DataMover available for transfer request";
                        _log.warn("DataTransfer " + _currentTransfer.getId() + " delayed: " + error);
                        final var comment = _currentTransfer.getComment();
                        if (comment == null || comment.isEmpty()) {
                            _currentTransfer.setComment(error);
                        }
                        _currentTransfer.setStatusCode(StatusFactory.RETR);
                        _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                        updateCount(_currentTransfer, false);
                        updateCount = false;
                        MASTER.removeDataTransfer(_currentTransfer);
                        return NEXT_STEP_DELAY;
                    }
                    removeValue(_currentTransfer);
                    return NEXT_STEP_CONTINUE;
                } catch (final NoHostException e) {
                    _log.warn("No hosts available", e);
                    shutdown(0, false, "no hosts available");
                } catch (final Throwable t) {
                    _log.warn("Stopping DataTransfer " + _currentTransfer.getId(), t);
                    _currentTransfer.setComment(Format.getMessage(t));
                    _currentTransfer.setStatusCode(StatusFactory.FAIL);
                    _currentTransfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
                    if (updateCount) {
                        updateCount(_currentTransfer, false);
                    } else {
                        _setLastFailedTransfer(_currentTransfer);
                    }
                    MASTER.addTransferHistory(_currentTransfer);
                    _update(_currentTransfer, true);
                }
            }
            return NEXT_STEP_DELAY;
        }

        /**
         * Check the rule for the current DataTransfer against all the similar DataTransfers found in the DataBase.
         *
         * @param retry
         *            the retry
         * @param rule
         *            the rule
         *
         * @return the data transfer
         */
        private DataTransfer _requeueOn(final boolean retry, final String rule) {
            // Let's get the list of DataTransfers with the same target name
            // in this Destination!
            CloseableIterator<DataTransfer> transfers = null;
            final var target = _currentTransfer.getTarget();
            final var id = _currentTransfer.getId();
            try {
                try {
                    transfers = BASE.getDataTransfersByDestinationAndTargetIterator(_destination.getName(), target,
                            false);
                } catch (final Throwable t) {
                    // TODO: should we delay the check?
                    transfers = CloseableIterator.empty();
                    _log.warn("Checking duplicate for DataTransfer-" + id, t);
                }
                final List<DataTransfer> candidates = new ArrayList<>();
                // For each DataTransfer found we have to check if one of them
                // should be transmitted rather than the current transfer?
                while (transfers.hasNext()) {
                    final var transfer = transfers.next();
                    // We don't compare to the current DataTransfer!
                    if (transfer.getId() == id) {
                        _log.debug("Don't use current DataTransfer in comparaison");
                        continue;
                    }
                    // Current DataTransfer parameters!
                    final var currentDatafile = _currentTransfer.getDataFile();
                    _log.debug("Current: DataTransfer-" + id + " (status=" + _currentTransfer.getStatusCode() + ",size="
                            + currentDatafile.getSize() + ",time="
                            + Format.formatTime(currentDatafile.getTimeFile().getTime()) + ")");

                    // Is this transfer a candidate for transmission instead of
                    // the current one?
                    if (!_compare(rule, target, transfer, _currentTransfer)) {
                        // The condition is false so the current transfer and
                        // the transfer found might be even (both false)?
                        _log.debug("Candidate found: DataTransfer-" + transfer.getId() + " (status="
                                + transfer.getStatusCode() + ",size=" + transfer.getSize() + ",time="
                                + Format.formatTime(transfer.getDataFile().getTimeFile().getTime()) + ")");
                        candidates.add(transfer);
                    }
                }
                // Is the current DataTransfer a winner against all the other
                // duplicates?
                if (candidates.isEmpty()) {
                    // There are no candidates so the current one should be
                    // transmitted!
                    _log.debug("Winner found: DataTransfer-" + id + " (condition true for all duplicates)");
                    return null;
                }
                // Let's go through all the candidates and check if they are
                // competitors to the current one!
                for (final DataTransfer transfer : candidates) {
                    if (_compare(rule, target, _currentTransfer, transfer)) {
                        // There is at least one winner against the current
                        // DataTransfer in the list of candidates!
                        _log.debug("Winner found: DataTransfer-" + transfer.getId()
                                + " (condition true for at least another one)");
                        return transfer;
                    }
                }
                // If the current DataTransfer is running then we will have to
                // build the list of competitors who are also running!
                final List<DataTransfer> runningCandidates = new ArrayList<>();
                // All the candidates have the same level as the current one so
                // let's check if one of them has not already been sent or is in
                // the process of being sent?
                for (final DataTransfer transfer : candidates) {
                    final var statusCode = transfer.getStatusCode();
                    if (StatusFactory.DONE.equals(statusCode) || StatusFactory.EXEC.equals(statusCode)
                            || StatusFactory.RETR.equals(statusCode)) {
                        // This one is already transmitting or has already been
                        // sent or is retrying!
                        if (!retry) {
                            // The current DataTransfer is not running and this
                            // one is so let's give him the priority!
                            _log.debug("Winner found: DataTransfer-" + transfer.getId() + " (already processed)");
                            return transfer;
                        }
                        // The current DataTransfer is also running so they
                        // are competing!
                        _log.debug("Running candidate: DataTransfer-" + transfer.getId());
                        runningCandidates.add(transfer);
                    }
                }
                // No winner yet so let's see if we have already tried to
                // transmit the current file?
                if (retry) {
                    // We already tried to send it so do we have any other
                    // candidate with the same level and running as well?
                    if (runningCandidates.isEmpty()) {
                        // No other candidate so the current DataTransfer is the
                        // winner!
                        _log.debug("Current DataTransfer-" + id + " is the winner (already processed)");
                        return null;
                    }
                    // There are other candidates!
                    candidates.clear();
                    candidates.addAll(runningCandidates);
                }
                // We still have not found a winner so let's take the one with
                // the higher DataTransfer identifier!
                var theOne = _currentTransfer;
                for (final DataTransfer transfer : candidates) {
                    if (transfer.getId() > theOne.getId()) {
                        theOne = transfer;
                    }
                }
                // Do we have a winner?
                if (theOne.getId() != id) {
                    _log.debug("Winner found: DataTransfer-" + theOne.getId() + " (higher identifier)");
                    return theOne;
                }
                _log.debug("Current DataTransfer-" + id + " is the winner (higher identifier)");
            } finally {
                if (transfers != null) {
                    transfers.close();
                }
            }
            // We found no DataTransfer with a higher level so we are going to
            // transmit this one!
            _log.debug("No winner found so DataTransfer-" + id + " will be transmitted");
            return null;
        }

        /**
         * Check if transfer2 should be transmitted rather than transfer1 by checking against the javascript expression.
         *
         * @param option
         *            the option
         * @param target
         *            the target
         * @param transfer1
         *            the transfer1
         * @param transfer2
         *            the transfer2
         *
         * @return true, if successful
         */
        private boolean _compare(final String option, final String target, final DataTransfer transfer1,
                final DataTransfer transfer2) {
            final var dataFile1 = transfer1.getDataFile();
            final var dataFile2 = transfer2.getDataFile();
            try {
                final var sb = new StringBuilder(option);
                Format.replaceAll(sb, "$time2", dataFile2.getTimeFile().getTime());
                Format.replaceAll(sb, "$size2", dataFile2.getSize());
                Format.replaceAll(sb, "$time1", dataFile1.getTimeFile().getTime());
                Format.replaceAll(sb, "$size1", dataFile1.getSize());
                Format.replaceAll(sb, "$destination", _destination.getName());
                Format.replaceAll(sb, "$target", target);
                return ScriptManager.exec(Boolean.class, ScriptManager.JS, sb.toString());
            } catch (final Throwable t) {
                _log.warn("Checking duplicates for DataTransfer-" + transfer1.getId(), t);
            }
            return false;
        }

        /**
         * Gets the performed.
         *
         * @return the performed
         */
        public int getPerformed() {
            return _performed;
        }

        /**
         * Gets the selected.
         *
         * @return the selected
         */
        public int getSelected() {
            return _selected;
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "This MBean provides operations to monitor and manage " + "the Destination",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("DestinationStep", "java.lang.String",
                                    "DestinationStep: runtime step.", true, false, false),
                            new MBeanAttributeInfo("RetryCount", "java.lang.Integer",
                                    "RetryCount: destination retry count.", true, false, false),
                            new MBeanAttributeInfo("PendingTransferCount", "java.lang.Integer",
                                    "PendingTransferCount: number of DataTransfer(s) in the queue.", true, false,
                                    false),
                            new MBeanAttributeInfo("LastTransfer", "java.lang.String", "LastTransfer: last transfer.",
                                    true, false, false),
                            new MBeanAttributeInfo("CurrentTransfer", "java.lang.String",
                                    "NextTransfer: current transfer.", true, false, false),
                            new MBeanAttributeInfo("CurrentTransfers", "java.lang.String",
                                    "CurrentTransfers: current transfer(s).", true, false, false),
                            new MBeanAttributeInfo("LastFailedTransfer", "java.lang.String",
                                    "LastFailedTransfer: last failed transfer.", true, false, false),
                            new MBeanAttributeInfo("TransferCount", "java.lang.Integer",
                                    "TransferCount: transfer count.", true, false, false),
                            new MBeanAttributeInfo("Inactivity", "java.lang.String", "Inactivity: elapsed time.", true,
                                    false, false) },
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
                if ("TransferCount".equals(attributeName)) {
                    return _provider.getTransferCount();
                }
                if ("PendingTransferCount".equals(attributeName)) {
                    return getPendingDataTransfersCount(_destination.getName());
                }
                if ("CurrentTransfers".equals(attributeName)) {
                    return MASTER.getStatus(_destination.getName());
                }
                if ("RetryCount".equals(attributeName)) {
                    return _provider.getRetryCount();
                }
                if ("LastTransfer".equals(attributeName)) {
                    final var transfer = getLastTransfer();
                    return transfer != null ? getKey(transfer) + "=" + getStatus(transfer) : null;
                }
                if ("CurrentTransfer".equals(attributeName)) {
                    return _currentTransfer != null ? getKey(_currentTransfer) + "=" + getStatus(_currentTransfer)
                            : null;
                }
                if ("Inactivity".equals(attributeName)) {
                    return _inactivity != -1 ? Format.formatDuration(System.currentTimeMillis() - _inactivity) : null;
                }
                if ("LastFailedTransfer".equals(attributeName)) {
                    final var transfer = getLastFailedTransfer();
                    return transfer != null ? getKey(transfer) + "=" + getStatus(transfer) : null;
                }
                if ("DestinationStep".equals(attributeName)) {
                    return DestinationStep.getStepString(_destinationStep);
                }
                if (_monitoringThread != null && "Monitor".equals(attributeName)) {
                    return _monitoringThread.getMonitorManager(_destination.getName());
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * The Class ShutdownThread.
         */
        private final class ShutdownThread extends ConfigurableRunnable {
            /** The _timeout. */
            private long _timeout;

            /** The _reset. */
            private boolean _reset;

            /** The _comment. */
            private String _comment;

            /**
             * Inits the.
             *
             * @param timeout
             *            the timeout
             * @param reset
             *            the reset
             * @param comment
             *            the comment
             */
            private void init(final long timeout, final boolean reset, final String comment) {
                _timeout = timeout;
                _reset = reset;
                _comment = comment;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                if (_timeout > 0) {
                    final var start = System.currentTimeMillis();
                    while (_provider.getTransferCount() > 0) {
                        if (System.currentTimeMillis() - start >= _timeout) {
                            break;
                        }
                        try {
                            Thread.sleep(Timer.ONE_SECOND);
                        } catch (final Exception ignored) {
                        }
                    }
                }
                MASTER.resetDestination(_destination, "Interrupted by Destination shutdown (" + _comment + ")");
                if (_reset) {
                    _log.info("Destination reseted");
                    _value.setStartCount(0);
                    _value.setHostName(null);
                    _value.setHost(null);
                }
                _log.debug("Saving SchedulerValue for " + _destination.getName() + " (lastTransferKo: "
                        + _value.getLastTransferKo() + ")");
                BASE.tryUpdate(_value);
                for (final DataTransfer transfer : BASE.getInterruptedTransfersPerDestination(_destination)) {
                    final var code = transfer.getStatusCode();
                    if (StatusFactory.EXEC.equals(code)) {
                        transfer.setComment("Requeued by the scheduler after a Destination shutdown");
                        transfer.setStatusCode(StatusFactory.RETR);
                        transfer.setUserStatus(null);
                        _updateDataTransfer(transfer);
                    }
                }
                _threads.remove(_destination.getName());
                _log.info("Destination shutdown completed");
                TransferScheduler.this.wakeup();
            }
        }
    }

    /**
     * The Class HostProvider.
     */
    private final class HostProvider {
        /** The _thread. */
        private final DestinationThread _thread;

        /** The _hosts. */
        private final HostElement[] _hosts;

        /** The _next and retry. */
        private final boolean _nextAndRetry;

        /** The _destination. */
        private final Destination _destination;

        /** The _value. */
        private final SchedulerValue _value;

        /** The _connections count. */
        private int _connectionsCount = 0;

        /** The _host. */
        private HostElement _host = null;

        /** The _waiting. */
        private boolean _waiting = false;

        /**
         * Instantiates a new host provider.
         *
         * @param thread
         *            the thread
         *
         * @throws NoHostException
         *             the no host exception
         */
        HostProvider(final DestinationThread thread) throws NoHostException {
            _thread = thread;
            _destination = thread.getDestination();
            _value = thread.getSchedulerValue();
            _nextAndRetry = new DestinationOption(_destination).onHostFailureNextAndRetry();
            final var resetTime = _value.getResetTime();
            var hostName = _nextAndRetry ? null : _value.getHostName();
            if (hostName != null && resetTime != null) {
                hostName = System.currentTimeMillis() - resetTime.getTime() > _destination.getResetFrequency() ? null
                        : hostName;
            }
            if (hostName != null) {
                _log.debug("Last HostName used: " + hostName);
            }
            final var hosts = BASE.getDestinationHost(_destination, HostOption.DISSEMINATION);
            _hosts = new HostElement[hosts.length];
            if (hosts.length == 0) {
                throw new NoHostException("No host(s) for the destination " + _destination.getName());
            }
            var index = 0;
            for (var i = 0; i < hosts.length; i++) {
                if (hostName != null && hostName.equals(hosts[i].getName())) {
                    _log.debug("Host " + hostName + " found (" + i + ")");
                    index = i;
                }
                _hosts[i] = new HostElement(hosts[i], i, i == hosts.length - 1 ? 0 : i + 1);
            }
            _select(_hosts[index], hostName == null);
            if (hostName == null) {
                _value.setHostName(_host.getHostName());
                _value.setHost(_host.getHost());
                _value.setStartCount(1);
                try {
                    BASE.update(_value);
                } catch (final DataBaseException e) {
                    _log.warn(e);
                }
            }
        }

        /**
         * Gets the host element.
         *
         * @param host
         *            the host
         *
         * @return the host element
         */
        private HostElement _getHostElement(final Host host) {
            HostElement element = null;
            for (final HostElement theElement : _hosts) {
                if (host.getName().equals(theElement.getHost().getName())) {
                    element = theElement;
                    break;
                }
            }
            return element;
        }

        /**
         * _select.
         *
         * @param element
         *            the element
         * @param resetTime
         *            the reset time
         */
        private void _select(final HostElement element, final boolean resetTime) {
            var current = -1;
            if (_host != null) {
                current = _host.getCurrent();
                _log.debug("Stop current host " + _host.getHostName() + " (" + current + ")");
                _host.stop();
            }
            (_host = element).start();
            if (element.getCurrent() != current) {
                _value.setHost(_host.getHost());
                _value.setHostName(_host.getHostName());
                _destination.setSchedulerValue(_value);
                MASTER.handle(new ChangeHostEvent(_destination));
                _log.info("Select host " + _host.getHostName() + " (" + _host.toString() + ")");
                if (resetTime) {
                    _setResetTime();
                }
            }
        }

        /**
         * Sets the reset time.
         */
        private void _setResetTime() {
            _log.info("Update ResetTime for destination " + _destination.getName());
            _value.setResetTime(new Timestamp(System.currentTimeMillis()));
            try {
                BASE.update(_value);
            } catch (final DataBaseException e) {
                _log.warn(e);
            }
        }

        /**
         * _wait for.
         *
         * @param duration
         *            the duration
         */
        private void _waitFor(final long duration) {
            if (duration > 0) {
                _log.info("Wait for " + Format.formatDuration(duration) + " before to retry");
                final var start = System.currentTimeMillis();
                _waiting = true;
                while (_thread.isRunning() && System.currentTimeMillis() - start < duration && !_thread._reset) {
                    try {
                        wait(1000);
                    } catch (final InterruptedException e) {
                    }
                }
                _waiting = false;
            }
        }

        /**
         * Update.
         *
         * @param transfer
         *            the transfer
         * @param start
         *            the start
         */
        void update(final DataTransfer transfer, final boolean start) {
            synchronized (HostProvider.this) {
                var element = _getHostElement(transfer.getHost());
                final var host = transfer.getHost();
                _connectionsCount += start ? 1 : -1;
                if (element != null) {
                    element.updateCount(start);
                    if (StatusFactory.DONE.equals(transfer.getStatusCode())
                            && host.getName().equals(_host.getHostName())
                            && transfer.getRetryTime().after(_host.getUpdate())) {
                        _log.info("Successful transfer on " + transfer.getDestinationName());
                        element = _nextAndRetry ? _hosts[0] : element;
                        _value.setHostName(element.getHostName());
                        _value.setHost(element.getHost());
                        _value.setStartCount(1);
                        try {
                            BASE.update(_value);
                        } catch (final DataBaseException e) {
                            _log.warn(e);
                        }
                        _select(element, true);
                    }
                } else {
                    _log.warn("HostElement lost on " + transfer.getDestinationName());
                }
                if (_connectionsCount < 0 && _thread.isRunning()) {
                    _log.warn("Emergency restart required for " + _destination.getName()
                            + " (HostProvider connection(s) count: " + _connectionsCount + ")");
                    _thread.shutdown(0, false, "emergency restart");
                }
            }
        }

        /**
         * Next.
         *
         * @param transfer
         *            the transfer
         *
         * @return true, if successful
         */
        boolean next(final DataTransfer transfer) {
            var result = true;
            synchronized (HostProvider.this) {
                final var host = transfer.getHost();
                if (_host.getHostName().equals(host.getName()) && transfer.getRetryTime().after(_host.getUpdate())) {
                    if (_host.retry()) {
                        _log.info("Stick to host " + _host.getHostName());
                        final var finish = transfer.getFinishTime();
                        long delay = _host.getHost().getRetryFrequency();
                        if (finish != null) {
                            final var gap = System.currentTimeMillis() - finish.getTime();
                            if (gap > 0 && gap < delay) {
                                delay -= gap;
                            } else {
                                _log.debug("Use host RetryFrequency (FinishTime: " + Format.formatTime(finish)
                                        + ", gap: " + gap + ")");
                            }
                        }
                        _waitFor(delay);
                    } else {
                        _log.error("Move to next host");
                        final var next = _host.getNext();
                        if (next == 0) {
                            var retryFrequency = _destination.getRetryFrequency();
                            final var retryCount = _destination.getRetryCount();
                            if (retryCount >= 0) {
                                if (_value.getStartCount() >= retryCount) {
                                    _log.error("Interrupt the destination");
                                    try {
                                        holdDestination(null, _destination.getName(), StatusFactory.HOLD, false);
                                    } catch (final DataBaseException e) {
                                        _log.warn(e);
                                    }
                                    retryFrequency = 0;
                                    result = false;
                                } else {
                                    final var startCount = _value.getStartCount() + 1;
                                    _log.debug("Set start count to " + startCount + " for " + _destination.getName());
                                    _value.setStartCount(startCount);
                                    try {
                                        BASE.update(_value);
                                    } catch (final DataBaseException e) {
                                        _log.warn(e);
                                    }
                                }
                            }
                            _waitFor(retryFrequency);
                        }
                        _select(_hosts[next], true);
                    }
                }
            }
            return result;
        }

        /**
         * Available.
         *
         * @return true, if successful
         */
        boolean available() {
            final var maxConnections = _destination.getMaxConnections();
            return _host.available()
                    && (maxConnections < 0 || _connectionsCount >= 0 && _connectionsCount < maxConnections);
        }

        /**
         * Gets the host.
         *
         * @return the host
         *
         * @throws NoHostException
         *             the no host exception
         */
        Host getHost() throws NoHostException {
            final var host = _host.getHost();
            synchronized (host) {
                if (host.getActive()) {
                    return host;
                }
            }
            throw new NoHostException("Host " + host.getHost() + " NOT activated");
        }

        /**
         * Signal if should be reset.
         *
         * @return true, if successful
         */
        boolean toBeReseted() {
            synchronized (HostProvider.this) {
                var result = false;
                _setResetTime();
                if (_host.getCurrent() != 0) {
                    _select(_hosts[0], false);
                    result = true;
                }
                return result;
            }
        }

        /**
         * Gets the transfer count.
         *
         * @return the transfer count
         */
        int getTransferCount() {
            return _connectionsCount;
        }

        /**
         * Gets the retry count.
         *
         * @return the retry count
         */
        int getRetryCount() {
            return _value.getStartCount();
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            final var result = new StringBuilder();
            for (final HostElement element : _hosts) {
                result.append(result.isEmpty() ? "" : " ").append(element.getHostName()).append("(")
                        .append(element.toString()).append(")").append(_host.equals(element) ? "*" : "");
            }
            return result.toString();
        }
    }

    /**
     * The Class NoHostException.
     */
    private static final class NoHostException extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 8342126818497864936L;

        /**
         * Instantiates a new no host exception.
         *
         * @param message
         *            the message
         */
        NoHostException(final String message) {
            super(message);
        }
    }

    /**
     * The Class HostElement.
     */
    private static final class HostElement {
        /** The _host. */
        final Host _host;

        /** The _current. */
        final int _current;

        /** The _next. */
        final int _next;

        /** The _update. */
        long _update = 0;

        /** The _retry. */
        int _retry = 0;

        /** The _count. */
        int _count = 0;

        /** The _total. */
        long _total = 0;

        /** The _max. */
        int _max = 0;

        /**
         * Instantiates a new host element.
         *
         * @param host
         *            the host
         * @param current
         *            the current
         * @param next
         *            the next
         */
        HostElement(final Host host, final int current, final int next) {
            _retry = host.getRetryCount();
            _host = host;
            _current = current;
            _next = next;
        }

        /**
         * Update count.
         *
         * @param start
         *            the start
         */
        synchronized void updateCount(final boolean start) {
            if ((start || _count > 0) && (_count += start ? 1 : -1) > _max) {
                _max = _count;
            }
            if (start) {
                _total++;
            }
        }

        /**
         * Start.
         */
        void start() {
            _update = System.currentTimeMillis();
            _retry = _host.getRetryCount() - 1;
        }

        /**
         * Stop.
         */
        void stop() {
            _retry = _host.getRetryCount();
        }

        /**
         * Gets the update.
         *
         * @return the update
         */
        Timestamp getUpdate() {
            return new Timestamp(_update);
        }

        /**
         * Gets the current.
         *
         * @return the current
         */
        int getCurrent() {
            return _current;
        }

        /**
         * Gets the next.
         *
         * @return the next
         */
        int getNext() {
            return _next;
        }

        /**
         * Gets the host.
         *
         * @return the host
         */
        Host getHost() {
            return _host;
        }

        /**
         * Gets the host name.
         *
         * @return the host name
         */
        String getHostName() {
            return _host.getName();
        }

        /**
         * Retry.
         *
         * @return true, if successful
         */
        synchronized boolean retry() {
            var result = false;
            if (_retry > 0) {
                _retry--;
                _log.debug("Host " + _host.getName() + " retry (" + toString() + ")");
                result = true;
            }
            return result;
        }

        /**
         * Available.
         *
         * @return true, if successful
         */
        boolean available() {
            final var max = _host.getMaxConnections();
            return _count < max || max < 0;
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            final var retry = _host.getRetryCount();
            return "start=" + (retry - _retry) + "/" + retry + ",count=" + _count + ",max=" + _max + ",total=" + _total
                    + ",pos=" + _current;
        }
    }
}
