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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.timer.Timer;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingPolicy;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.MonitoringValue;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.database.TransferServer;
import ecmwf.common.database.Url;
import ecmwf.common.database.WebUser;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class ManagementProxy.
 */
final class ManagementProxy implements ManagementInterface {

    /** The contacts ref. */
    private static final AtomicReference<Map<String, String>> contactsRef = new AtomicReference<>(
            new ConcurrentHashMap<>());

    /** The contacts update. */
    private static final AtomicLong contactsUpdate = new AtomicLong(-1);

    /** The anonymous session. */
    private static ECpdsSession anonymousSession;

    /** The management interface. */
    private final ManagementInterface managementInterface;

    /**
     * Instantiates a new management proxy.
     *
     * @param managementInterface
     *            the management interface
     */
    protected ManagementProxy(final ManagementInterface managementInterface) {
        this.managementInterface = managementInterface;
    }

    /**
     * {@inheritDoc}
     *
     * Get the destination names which have the provided email address in their contacts!
     */
    @Override
    public List<String> getDestinationNamesForContact(final List<Map.Entry<String, String>> emailPattern,
            final boolean caseSensitive) throws IOException {
        final var monitor = new MonitorCall("getDestinationNamesForContact(" + emailPattern + ")");
        return monitor.done(managementInterface.getDestinationNamesForContact(emailPattern, caseSensitive));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the contacts.
     */
    @Override
    public Map<String, String> getContacts() throws MasterException, IOException {
        final var monitor = new MonitorCall("getContacts()");
        final var currentTime = System.currentTimeMillis();
        final var lastUpdate = contactsUpdate.get();
        if ((lastUpdate == -1 || currentTime - lastUpdate > 5 * Timer.ONE_MINUTE)
                && contactsUpdate.compareAndSet(lastUpdate, currentTime)) {
            contactsRef.set(managementInterface.getContacts());
        }
        return monitor.done(contactsRef.get());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination caches.
     */
    @Override
    public Map<String, DestinationCache> getDestinationCaches()
            throws MonitorException, MasterException, DataBaseException, RemoteException {
        throw new MasterException("Not available for Plugin");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the monitor manager.
     */
    @Override
    public MonitorManager getMonitorManager(final String destinationName)
            throws MonitorException, MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getMonitorManager");
        }
        final var monitor = new MonitorCall("getMonitorManager(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getMonitorManager());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getMonitorManager(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination status.
     */
    @Override
    public String getDestinationStatus(final String destinationName)
            throws MasterException, DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getDestinationStatus");
        }
        final var monitor = new MonitorCall("getDestinationStatus(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getDestinationStatus());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getDestinationStatus(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination size.
     */
    @Override
    public int getDestinationSize(final String destinationName) throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getDestinationSize");
        }
        final var monitor = new MonitorCall("getDestinationSize(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getDestinationSize());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getDestinationSize(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination start date.
     */
    @Override
    public Date getDestinationStartDate(final String destinationName) throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getDestinationStartDate");
        }
        final var monitor = new MonitorCall("getDestinationStartDate(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getDestinationStartDate());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getDestinationStartDate(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the pending data transfers count.
     */
    @Override
    public int getPendingDataTransfersCount(final String destinationName) throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getPendingDataTransfersCount");
        }
        final var monitor = new MonitorCall("getPendingDataTransfersCount(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getPendingDataTransfersCount());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getPendingDataTransfersCount(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination last transfer.
     */
    @Override
    public DataTransfer getDestinationLastTransfer(final String destinationName)
            throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getDestinationLastTransfer");
        }
        final var monitor = new MonitorCall("getDestinationLastTransfer(" + destinationName + ")");
        try {
            return monitor.done(
                    MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache().getLastTransfer());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getDestinationLastTransfer(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination last failed transfer.
     */
    @Override
    public DataTransfer getDestinationLastFailedTransfer(final String destinationName)
            throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("No destination specified for getDestinationLastFailed");
        }
        final var monitor = new MonitorCall("getDestinationLastFailedTransfer(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getDestinationSchedulerCache()
                    .getLastFailedTransfer());
        } catch (final Exception e) {
            return monitor.done(managementInterface.getDestinationLastFailedTransfer(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retrieved.
     */
    @Override
    public long getRetrieved(final long dataFileId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getRetrieved(" + dataFileId + ")");
        return monitor.done(managementInterface.getRetrieved(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server name.
     */
    @Override
    public String getTransferServerName(final long dataFileId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferServerName(" + dataFileId + ")");
        return monitor.done(managementInterface.getTransferServerName(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ecpds session.
     */
    @Override
    public ECpdsSession getECpdsSession(final String user, final String password, final String host, final String agent,
            final String comment) throws MasterException, DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(password)) {
            throw new MasterException("Invalid parameter(s) for getECpdsSession");
        }
        final var monitor = new MonitorCall(
                "getECpdsSession(" + user + "," + password + "," + host + "," + agent + "," + comment + ")");
        final var isAnonymous = Cnf.at("Server", "anonymousUser", "anonymous").equals(user);
        final ECpdsSession session;
        if (isAnonymous && anonymousSession != null) {
            session = anonymousSession;
        } else {
            session = managementInterface.getECpdsSession(user, password, host, agent, comment);
            if (isAnonymous) {
                setAnonymousSession(session);
            }
        }
        return monitor.done(session);
    }

    /**
     * Sets the anonymous session. Set the anonymous session if not already set!
     *
     * @param session
     *            the new anonymous session
     */
    private static synchronized void setAnonymousSession(final ECpdsSession session) {
        if (anonymousSession == null) {
            anonymousSession = session;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Save web user.
     */
    @Override
    public void saveWebUser(final ECpdsSession session, final WebUser webUser)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || webUser == null) {
            throw new MasterException("Invalid parameter(s) for saveWebUser");
        }
        final var monitor = new MonitorCall(
                "saveWebUser(" + session.getWebUser().getName() + "," + webUser.getId() + ")");
        managementInterface.saveWebUser(session, webUser);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Copy destination.
     */
    @Override
    public DestinationCache copyDestination(final ECpdsSession session, final String fromDestination,
            final String toDestination, final String label, final boolean copySharedHost)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(fromDestination) || isEmpty(toDestination)) {
            throw new MasterException("Invalid parameter(s) for copyDestination");
        }
        final var monitor = new MonitorCall("copyDestination(" + session.getWebUser().getName() + "," + fromDestination
                + "," + toDestination + "," + copySharedHost + ")");
        final var destinationCache = managementInterface.copyDestination(session, fromDestination, toDestination, label,
                copySharedHost);
        MasterManager.insertInCache(destinationCache);
        return monitor.done(destinationCache);
    }

    /**
     * {@inheritDoc}
     *
     * Copy host.
     */
    @Override
    public DestinationCache copyHost(final ECpdsSession session, final String destinationName, final String hostName)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(destinationName) || isEmpty(hostName)) {
            throw new MasterException("Invalid parameter(s) for copyHost");
        }
        final var monitor = new MonitorCall(
                "copyHost(" + session.getWebUser().getName() + "," + destinationName + "," + hostName + ")");
        final var destinationCache = managementInterface.copyHost(session, destinationName, hostName);
        MasterManager.insertInCache(destinationCache);
        return monitor.done(destinationCache);
    }

    /**
     * {@inheritDoc}
     *
     * Export destination.
     */
    @Override
    public void exportDestination(final ECpdsSession session, final String targetMaster, final String fromDestination,
            final boolean copySharedHost) throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(targetMaster) || isEmpty(fromDestination)) {
            throw new MasterException("Invalid parameter(s) for exportDestination");
        }
        final var monitor = new MonitorCall("exportDestination(" + session.getWebUser().getName() + "," + targetMaster
                + "," + fromDestination + "," + copySharedHost + ")");
        managementInterface.exportDestination(session, targetMaster, fromDestination, copySharedHost);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Close E cpds session.
     */
    @Override
    public void closeECpdsSession(final ECpdsSession session, final boolean expired) throws RemoteException {
        if (session != null) {
            final var monitor = new MonitorCall(
                    "closeECpdsSession(" + session.getWebUser().getName() + "," + expired + ")");
            managementInterface.closeECpdsSession(session, expired);
            monitor.done();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close incoming connection.
     */
    @Override
    public void closeIncomingConnection(final ECpdsSession session, final String id) throws IOException {
        if (session != null && id != null) {
            final var monitor = new MonitorCall(
                    "closeIncomingConnection(" + session.getWebUser().getName() + "," + id + ")");
            managementInterface.closeIncomingConnection(session, id);
            monitor.done();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Restart destination.
     */
    @Override
    public DestinationSchedulerCache restartDestination(final ECpdsSession session, final String destinationName,
            final boolean graceful) throws MasterException, RemoteException, DataBaseException {
        if (session == null || isEmpty(destinationName)) {
            throw new MasterException("Invalid parameter(s) for restartDestination");
        }
        final var monitor = new MonitorCall(
                "restartDestination(" + session.getWebUser().getName() + "," + destinationName + "," + graceful + ")");
        final var cache = managementInterface.restartDestination(session, destinationName, graceful);
        MasterManager.updateCache(cache);
        return monitor.done(cache);
    }

    /**
     * {@inheritDoc}
     *
     * Restart all destinations.
     */
    @Override
    public void restartAllDestinations(final ECpdsSession session, final boolean graceful)
            throws MasterException, RemoteException {
        if (session == null) {
            throw new MasterException("Invalid parameter(s) for restartAllDestinations");
        }
        final var monitor = new MonitorCall(
                "restartAllDestinations(" + session.getWebUser().getName() + "," + graceful + ")");
        managementInterface.restartAllDestinations(session, graceful);
        MasterManager.resetCaches();
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown transfer server.
     */
    @Override
    public void shutdownTransferServer(final ECpdsSession session, final TransferServer server, final boolean graceful,
            final boolean restart) throws MasterException, RemoteException {
        if (session == null || server == null) {
            throw new MasterException("Invalid parameter(s) for shutdownTransferServer");
        }
        final var monitor = new MonitorCall("shutdownTransferServer(" + session.getWebUser().getName() + ","
                + server.getName() + "," + graceful + "," + restart + ")");
        managementInterface.shutdownTransferServer(session, server, graceful, restart);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Hold destination.
     */
    @Override
    public DestinationSchedulerCache holdDestination(final ECpdsSession session, final String destinationName,
            final boolean graceful) throws MasterException, RemoteException, DataBaseException {
        if (session == null || isEmpty(destinationName)) {
            throw new MasterException("Invalid parameter(s) for holdDestination");
        }
        final var monitor = new MonitorCall(
                "holdDestination(" + session.getWebUser().getName() + "," + destinationName + "," + graceful + ")");
        final var cache = managementInterface.holdDestination(session, destinationName, graceful);
        MasterManager.updateCache(cache);
        return monitor.done(cache);
    }

    /**
     * {@inheritDoc}
     *
     * Clean destination.
     */
    @Override
    public String cleanDestination(final ECpdsSession session, final String destinationName, final long days)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(destinationName) || days < 0) {
            throw new MasterException("Invalid parameter(s) for cleanDestination");
        }
        final var monitor = new MonitorCall(
                "cleanDestination(" + session.getWebUser().getName() + "," + destinationName + "," + days + ")");
        return monitor.done(managementInterface.cleanDestination(session, destinationName, days));
    }

    /**
     * {@inheritDoc}
     *
     * Hold all destinations.
     */
    @Override
    public void holdAllDestinations(final ECpdsSession session, final boolean graceful)
            throws MasterException, RemoteException {
        if (session == null) {
            throw new MasterException("Invalid parameter(s) for holdAllDestinations");
        }
        final var monitor = new MonitorCall(
                "holdAllDestinations(" + session.getWebUser().getName() + "," + graceful + ")");
        managementInterface.holdAllDestinations(session, graceful);
        MasterManager.resetCaches();
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Transfer status update allowed.
     */
    @Override
    public boolean transferStatusUpdateAllowed(final long id, final String code)
            throws MasterException, RemoteException {
        if (id < 0 || isEmpty(code)) {
            throw new MasterException("Invalid parameter(s) for transferStatusUpdateAllowed");
        }
        final var monitor = new MonitorCall("transferStatusUpdateAllowed(" + id + "," + code + ")");
        return monitor.done(managementInterface.transferStatusUpdateAllowed(id, code));
    }

    /**
     * {@inheritDoc}
     *
     * Update transfer status.
     */
    @Override
    public boolean updateTransferStatus(final ECpdsSession session, final long id, final String code)
            throws MasterException, RemoteException {
        if (session == null || id < 0 || isEmpty(code)) {
            throw new MasterException("Invalid parameter(s) for updateTransferStatus");
        }
        final var monitor = new MonitorCall(
                "updateTransferStatus(" + session.getWebUser().getName() + "," + id + "," + code + ")");
        return monitor.done(managementInterface.updateTransferStatus(session, id, code));
    }

    /**
     * {@inheritDoc}
     *
     * Reset transfer schedule date.
     */
    @Override
    public void resetTransferScheduleDate(final ECpdsSession session, final long id)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || id < 0) {
            throw new MasterException("Invalid parameter(s) for resetTransferScheduleDate");
        }
        final var monitor = new MonitorCall(
                "resetTransferScheduleDate(" + session.getWebUser().getName() + "," + id + ")");
        managementInterface.resetTransferScheduleDate(session, id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Update transfer priority.
     */
    @Override
    public void updateTransferPriority(final ECpdsSession session, final long id, final int priority)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || id < 0) {
            throw new MasterException("Invalid parameter(s) for updateTransferPriority");
        }
        final var monitor = new MonitorCall(
                "updateTransferPriority(" + session.getWebUser().getName() + "," + id + "," + priority + ")");
        managementInterface.updateTransferPriority(session, id, priority);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Update expiry time.
     */
    @Override
    public void updateExpiryTime(final ECpdsSession session, final long id, final Timestamp timestamp)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || id < 0 || timestamp == null) {
            throw new MasterException("Invalid parameter(s) for updateExpiryTime");
        }
        final var monitor = new MonitorCall("updateExpiryTime(" + session.getWebUser().getName() + "," + id + ","
                + Format.formatTime(timestamp) + ")");
        managementInterface.updateExpiryTime(session, id, timestamp);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Transfer.
     */
    @Override
    public long transfer(final ECpdsSession session, final byte[] bytes, final Host host, final String target,
            final long remotePosn) throws MasterException, DataBaseException, IOException {
        if (session == null || bytes == null || host == null || isEmpty(target)) {
            throw new MasterException("Invalid parameter(s) for transfer");
        }
        final var monitor = new MonitorCall("transfer(" + session.getWebUser().getName() + ",bytes[]," + host.getName()
                + "," + target + "," + remotePosn + ")");
        return monitor.done(managementInterface.transfer(session, bytes, host, target, remotePosn));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host report.
     */
    @Override
    public String getHostReport(final ECpdsSession session, final Host proxy, final Host host)
            throws MasterException, DataBaseException, IOException {
        if (session == null || proxy == null || host == null) {
            throw new MasterException("Invalid parameter(s) for getHostReport");
        }
        final var monitor = new MonitorCall(
                "getHostReport(" + session.getWebUser().getName() + "," + proxy.getName() + "," + host.getName() + ")");
        return monitor.done(managementInterface.getHostReport(session, proxy, host));
    }

    /**
     * {@inheritDoc}
     *
     * Clean data window.
     */
    @Override
    public void cleanDataWindow(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for cleanDataWindow");
        }
        final var monitor = new MonitorCall(
                "cleanDataWindow(" + session.getWebUser().getName() + "," + host.getName() + ")");
        managementInterface.cleanDataWindow(session, host);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Reset transfer statistics.
     */
    @Override
    public void resetTransferStatistics(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for resetTransferStatistics");
        }
        final var monitor = new MonitorCall(
                "resetHostStats(" + session.getWebUser().getName() + "," + host.getName() + ")");
        managementInterface.resetTransferStatistics(session, host);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mover report.
     */
    @Override
    public String getMoverReport(final ECpdsSession session, final Host proxy)
            throws MasterException, DataBaseException, IOException {
        if (session == null || proxy == null) {
            throw new MasterException("Invalid parameter(s) for getMoverReport");
        }
        final var monitor = new MonitorCall(
                "getMoverReport(" + session.getWebUser().getName() + "," + proxy.getName() + ")");
        return monitor.done(managementInterface.getMoverReport(session, proxy));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the report.
     */
    @Override
    public String getReport(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for getReport");
        }
        final var monitor = new MonitorCall("getReport(" + session.getWebUser().getName() + "," + host.getName() + ")");
        return monitor.done(managementInterface.getReport(session, host));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the output.
     */
    @Override
    public RemoteInputStream getOutput(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for getOutput");
        }
        final var monitor = new MonitorCall("getOutput(" + session.getWebUser().getName() + "," + host.getName() + ")");
        return monitor.done(managementInterface.getOutput(session, host));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the report.
     */
    @Override
    public String getReport(final ECpdsSession session, final TransferServer server)
            throws MasterException, DataBaseException, IOException {
        if (session == null || server == null) {
            throw new MasterException("Invalid parameter(s) for getReport");
        }
        final var monitor = new MonitorCall(
                "getReport(" + session.getWebUser().getName() + "," + server.getName() + ")");
        return monitor.done(managementInterface.getReport(session, server));
    }

    /**
     * {@inheritDoc}
     *
     * Update host.
     */
    @Override
    public Host updateHost(final ECpdsSession session, Host host)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for updateHost");
        }
        final var monitor = new MonitorCall(
                "updateHost(" + session.getWebUser().getName() + "," + host.getName() + ")");
        host = managementInterface.updateHost(session, host);
        MasterManager.updateCache(host);
        return monitor.done(host);
    }

    /**
     * {@inheritDoc}
     *
     * Update transfer monitoring value.
     */
    @Override
    public void updateTransferMonitoringValue(final ECpdsSession session, final MonitoringValue value)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || value == null) {
            throw new MasterException("Invalid parameter(s) for updateTransferMonitoringValue");
        }
        final var monitor = new MonitorCall(
                "updateTransferMonitoringValue(" + session.getWebUser().getName() + "," + value.getId() + ")");
        managementInterface.updateTransferMonitoringValue(session, value);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Update file monitoring value.
     */
    @Override
    public void updateFileMonitoringValue(final ECpdsSession session, final MonitoringValue value)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || value == null) {
            throw new MasterException("Invalid parameter(s) for updateFileMonitoringValue");
        }
        final var monitor = new MonitorCall(
                "updateFileMonitoringValue(" + session.getWebUser().getName() + "," + value.getId() + ")");
        managementInterface.updateFileMonitoringValue(session, value);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public void shutdown(final ECpdsSession session, final boolean graceful, final boolean restart)
            throws MasterException, RemoteException {
        if (session == null) {
            throw new MasterException("Invalid parameter(s) for shutdown");
        }
        final var monitor = new MonitorCall(
                "shutdown(" + session.getWebUser().getName() + "," + graceful + "," + restart + ")");
        managementInterface.shutdown(session, graceful, restart);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the transfer method.
     */
    @Override
    public void removeTransferMethod(final ECpdsSession session, final TransferMethod method)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || method == null) {
            throw new MasterException("Invalid parameter(s) for removeTransferMethod");
        }
        final var monitor = new MonitorCall(
                "removeTransferMethod(" + session.getWebUser().getName() + "," + method.getName() + ")");
        managementInterface.removeTransferMethod(session, method);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the E ctrans module.
     */
    @Override
    public void removeECtransModule(final ECpdsSession session, final ECtransModule module)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || module == null) {
            throw new MasterException("Invalid parameter(s) for removeECtransModule");
        }
        final var monitor = new MonitorCall(
                "removeECtransModule(" + session.getWebUser().getName() + "," + module.getName() + ")");
        managementInterface.removeECtransModule(session, module);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the transfer group.
     */
    @Override
    public void removeTransferGroup(final ECpdsSession session, final TransferGroup group)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || group == null) {
            throw new MasterException("Invalid parameter(s) for removeTransferGroup");
        }
        final var monitor = new MonitorCall(
                "removeTransferGroup(" + session.getWebUser().getName() + "," + group.getName() + ")");
        managementInterface.removeTransferGroup(session, group);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the destination.
     */
    @Override
    public void removeDestination(final ECpdsSession session, final String destinationName, final boolean cleanOnly,
            final boolean removeAll) throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(destinationName)) {
            throw new MasterException("Invalid parameter(s) for removeDestination");
        }
        final var monitor = new MonitorCall("removeDestination(" + session.getWebUser().getName() + ","
                + destinationName + "," + cleanOnly + "," + removeAll + ")");
        managementInterface.removeDestination(session, destinationName, cleanOnly, removeAll);
        if (!cleanOnly) {
            MasterManager.removeFromCache(new Destination(destinationName));
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the destination.
     */
    @Override
    public void removeDestination(final ECpdsSession session, final String destinationName)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || isEmpty(destinationName)) {
            throw new MasterException("Invalid parameter(s) for removeDestination");
        }
        managementInterface.removeDestination(session, destinationName);
        MasterManager.removeFromCache(new Destination(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Removes the transfer server.
     */
    @Override
    public void removeTransferServer(final ECpdsSession session, final TransferServer server)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || server == null) {
            throw new MasterException("Invalid parameter(s) for removeTransferServer");
        }
        final var monitor = new MonitorCall(
                "removeTransferServer(" + session.getWebUser().getName() + "," + server.getName() + ")");
        managementInterface.removeTransferServer(session, server);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the host.
     */
    @Override
    public void removeHost(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || host == null) {
            throw new MasterException("Invalid parameter(s) for removeHost");
        }
        final var monitor = new MonitorCall(
                "removeHost(" + session.getWebUser().getName() + "," + host.getName() + ")");
        managementInterface.removeHost(session, host);
        MasterManager.removeFromCache(host);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the incoming user.
     */
    @Override
    public void removeIncomingUser(final ECpdsSession session, final IncomingUser user)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || user == null) {
            throw new MasterException("Invalid parameter(s) for removeIncomingUser");
        }
        final var monitor = new MonitorCall(
                "removeIncomingUser(" + session.getWebUser().getName() + "," + user.getId() + ")");
        managementInterface.removeIncomingUser(session, user);
        MasterManager.removeFromCache(user);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the incoming policy.
     */
    @Override
    public void removeIncomingPolicy(final ECpdsSession session, final IncomingPolicy policy)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || policy == null) {
            throw new MasterException("Invalid parameter(s) for removeIncomingPolicy");
        }
        final var monitor = new MonitorCall(
                "removeIncomingPolicy(" + session.getWebUser().getName() + "," + policy.getId() + ")");
        managementInterface.removeIncomingPolicy(session, policy);
        MasterManager.removeFromCache(policy);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the data transfer.
     */
    @Override
    public void removeDataTransfer(final ECpdsSession session, final DataTransfer transfer)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || transfer == null) {
            throw new MasterException("Invalid parameter(s) for removeDataTransfer");
        }
        final var monitor = new MonitorCall(
                "removeDataTransfer(" + session.getWebUser().getName() + "," + transfer.getId() + ")");
        managementInterface.removeDataTransfer(session, transfer);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Interrupt data transfer retrieval.
     */
    @Override
    public boolean interruptDataTransferRetrieval(final ECpdsSession session, final long id)
            throws MasterException, RemoteException, DataBaseException {
        if (session == null || id < 0) {
            throw new MasterException("Invalid parameter(s) for interruptDataTransferRetrieval");
        }
        final var monitor = new MonitorCall(
                "interruptDataTransferRetrieval(" + session.getWebUser().getName() + "," + id + ")");
        return monitor.done(managementInterface.interruptDataTransferRetrieval(session, id));
    }

    /**
     * {@inheritDoc}
     *
     * Removes the data file.
     */
    @Override
    public void removeDataFile(final ECpdsSession session, final DataFile file)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || file == null) {
            throw new MasterException("Invalid parameter(s) for removeDataFile");
        }
        final var monitor = new MonitorCall(
                "removeDataFile(" + session.getWebUser().getName() + "," + file.getId() + ")");
        managementInterface.removeDataFile(session, file);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the web user.
     */
    @Override
    public void removeWebUser(final ECpdsSession session, final WebUser user)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || user == null) {
            throw new MasterException("Invalid parameter(s) for removeWebUser");
        }
        final var monitor = new MonitorCall(
                "removeWebUser(" + session.getWebUser().getName() + "," + user.getName() + ")");
        managementInterface.removeWebUser(session, user);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the category.
     */
    @Override
    public void removeCategory(final ECpdsSession session, final Category category)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || category == null) {
            throw new MasterException("Invalid parameter(s) for removeCategory");
        }
        final var monitor = new MonitorCall(
                "removeCategory(" + session.getWebUser().getName() + "," + category.getId() + ")");
        managementInterface.removeCategory(session, category);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the url.
     */
    @Override
    public void removeUrl(final ECpdsSession session, final Url url)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || url == null) {
            throw new MasterException("Invalid parameter(s) for removeUrl");
        }
        final var monitor = new MonitorCall("removeUrl(" + session.getWebUser().getName() + "," + url.getName() + ")");
        managementInterface.removeUrl(session, url);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Resend data transfer events.
     */
    @Override
    public void resendDataTransferEvents(final String root, final DataTransferEventRequest[] dataTransferEventRequests)
            throws RemoteException {
        final long size = dataTransferEventRequests != null ? dataTransferEventRequests.length : 0;
        final var monitor = new MonitorCall(
                "resendDataTransferEvents(" + root + ",DataTransferEventRequest[" + size + "])");
        if (isNotEmpty(root) && size > 0) {
            managementInterface.resendDataTransferEvents(root, dataTransferEventRequests);
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Exec.
     */
    @Override
    public byte[] exec(final ECpdsSession session, final Map<String, String> environment, final byte[] request,
            final String service) throws MasterException, IOException {
        if (session == null || environment == null || request == null || isEmpty(service)) {
            throw new MasterException("Invalid parameter(s) for exec");
        }
        final var monitor = new MonitorCall("exec(" + session.getWebUser().getName() + ",Map(" + environment.size()
                + "),byte[" + request.length + "]," + service + ")");
        return monitor.done(managementInterface.exec(session, environment, request, service));
    }

    /**
     * {@inheritDoc}
     *
     * Send E cpds message.
     */
    @Override
    public void sendECpdsMessage(final ECpdsSession session, final String from, final String to, final String cc,
            final String subject, final String content, final String attachmentName, final String attachmentContent)
            throws MasterException, IOException {
        if (session == null || isEmpty(to) || isEmpty(subject) || isEmpty(content)) {
            throw new MasterException("Invalid parameter(s) for sendECpdsMessage");
        }
        final var monitor = new MonitorCall("sendECpdsMessage(" + session.getWebUser().getName() + "," + from + "," + to
                + "," + cc + "," + subject + "," + content + ")");
        managementInterface.sendECpdsMessage(session, from, to, cc, subject, content, attachmentName,
                attachmentContent);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination scheduler cache.
     */
    @Override
    public DestinationSchedulerCache getDestinationSchedulerCache(final String destinationName)
            throws MasterException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new MasterException("Invalid parameter(s) for getDestinationSchedulerCache");
        }
        final var monitor = new MonitorCall("getDestinationSchedulerCache(" + destinationName + ")");
        final var cache = managementInterface.getDestinationSchedulerCache(destinationName);
        MasterManager.updateCache(cache);
        return monitor.done(cache);
    }

    /**
     * {@inheritDoc}
     *
     * Compute filter efficiency.
     */
    @Override
    public String computeFilterEfficiency(final ECpdsSession session, final String destinationName, final String email,
            final String filter, final long date, final boolean includeStdby, final String pattern)
            throws DataBaseException, MasterException, RemoteException {
        if (session == null || isEmpty(destinationName) || isEmpty(email) || isEmpty(filter)) {
            throw new MasterException("Invalid parameter(s) for computeFilterEfficiency");
        }
        final var monitor = new MonitorCall(
                "computeFilterEfficiency(" + session.getWebUser().getName() + "," + destinationName + "," + email + ","
                        + filter + "," + Format.formatTime(date) + "," + includeStdby + "," + pattern + ")");
        return monitor.done(managementInterface.computeFilterEfficiency(session, destinationName, email, filter, date,
                includeStdby, pattern));
    }
}
