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
 * ECMWF Product Data Store (OpenPDS) Project
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
     * Get the destination names which have the provided email address in their contacts!
     *
     * @param emailPattern
     *            the email pattern
     * @param caseSensitive
     *            is it case sensitive
     *
     * @return the destination names
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public List<String> getDestinationNamesForContact(final List<Map.Entry<String, String>> emailPattern,
            final boolean caseSensitive) throws IOException {
        final var monitor = new MonitorCall("getDestinationNamesForContact(" + emailPattern + ")");
        return monitor.done(managementInterface.getDestinationNamesForContact(emailPattern, caseSensitive));
    }

    /**
     * Gets the contacts.
     *
     * @return the contacts
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the destination caches.
     *
     * @return the destination caches
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Map<String, DestinationCache> getDestinationCaches()
            throws MonitorException, MasterException, DataBaseException, RemoteException {
        throw new MasterException("Not available for Plugin");
    }

    /**
     * Gets the monitor manager.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the monitor manager
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the destination status.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination status
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the destination size.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination size
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the destination start date.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination start date
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the pending data transfers count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the pending data transfers count
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the destination last transfer.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination last transfer
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the destination last failed transfer.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination last failed transfer
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Gets the retrieved.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the retrieved
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public long getRetrieved(final long dataFileId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getRetrieved(" + dataFileId + ")");
        return monitor.done(managementInterface.getRetrieved(dataFileId));
    }

    /**
     * Gets the transfer server name.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the transfer server name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String getTransferServerName(final long dataFileId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferServerName(" + dataFileId + ")");
        return monitor.done(managementInterface.getTransferServerName(dataFileId));
    }

    /**
     * Gets the ecpds session.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param comment
     *            the comment
     *
     * @return the ecpds session
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Save web user.
     *
     * @param session
     *            the session
     * @param webUser
     *            the web user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Copy destination.
     *
     * @param session
     *            the session
     * @param fromDestination
     *            the from destination
     * @param toDestination
     *            the to destination
     * @param label
     *            the label
     * @param copySharedHost
     *            the copy shared host
     *
     * @return the destination cache
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Copy host.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the destination cache
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Export destination.
     *
     * @param session
     *            the session
     * @param targetMaster
     *            the target master
     * @param fromDestination
     *            the from destination
     * @param copySharedHost
     *            the copy shared host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Close E cpds session.
     *
     * @param session
     *            the session
     * @param expired
     *            the expired
     *
     * @throws RemoteException
     *             the remote exception
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
     * Close incoming connection.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Restart destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param graceful
     *            the graceful
     *
     * @return the destination scheduler cache
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     * @throws DataBaseException
     *             the data base exception
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
     * Restart all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Shutdown transfer server.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Hold destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param graceful
     *            the graceful
     *
     * @return the destination scheduler cache
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     * @throws DataBaseException
     *             the data base exception
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
     * Clean destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param days
     *            the days
     *
     * @return the string
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Hold all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Transfer status update allowed.
     *
     * @param id
     *            the id
     * @param code
     *            the code
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Update transfer status.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param code
     *            the code
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Reset transfer schedule date.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Update transfer priority.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param priority
     *            the priority
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Update expiry time.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param timestamp
     *            the timestamp
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Transfer.
     *
     * @param session
     *            the session
     * @param bytes
     *            the bytes
     * @param host
     *            the host
     * @param target
     *            the target
     * @param remotePosn
     *            the remote posn
     *
     * @return the long
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the host report.
     *
     * @param session
     *            the session
     * @param proxy
     *            the proxy
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Clean data window.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Reset transfer statistics.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the mover report.
     *
     * @param session
     *            the session
     * @param proxy
     *            the proxy
     *
     * @return the mover report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the report.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the output.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the output
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the report.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     *
     * @return the report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Update host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Update transfer monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the value
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Update file monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the value
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Shutdown.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the transfer method.
     *
     * @param session
     *            the session
     * @param method
     *            the method
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the E ctrans module.
     *
     * @param session
     *            the session
     * @param module
     *            the module
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the transfer group.
     *
     * @param session
     *            the session
     * @param group
     *            the group
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param cleanOnly
     *            the clean only
     * @param removeAll
     *            the remove all
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the transfer server.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the incoming user.
     *
     * @param session
     *            the session
     * @param user
     *            the user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the incoming policy.
     *
     * @param session
     *            the session
     * @param policy
     *            the policy
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the data transfer.
     *
     * @param session
     *            the session
     * @param transfer
     *            the transfer
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Interrupt data transfer retrieval.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     * @throws DataBaseException
     *             the data base exception
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
     * Removes the data file.
     *
     * @param session
     *            the session
     * @param file
     *            the file
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the web user.
     *
     * @param session
     *            the session
     * @param user
     *            the user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the category.
     *
     * @param session
     *            the session
     * @param category
     *            the category
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Removes the url.
     *
     * @param session
     *            the session
     * @param url
     *            the url
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
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
     * Resend data transfer events.
     *
     * @param root
     *            the root
     * @param dataTransferEventRequests
     *            the data transfer event requests
     *
     * @throws RemoteException
     *             the remote exception
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
     * Exec.
     *
     * @param session
     *            the session
     * @param environment
     *            the environment
     * @param request
     *            the request
     * @param service
     *            the service
     *
     * @return the byte[]
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Send E cpds message.
     *
     * @param session
     *            the session
     * @param from
     *            the from
     * @param to
     *            the to
     * @param cc
     *            the cc
     * @param subject
     *            the subject
     * @param content
     *            the content
     * @param attachmentName
     *            the attachment name
     * @param attachmentContent
     *            the attachment content
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * Gets the destination scheduler cache.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination scheduler cache
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
     * Compute filter efficiency.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param email
     *            the email
     * @param filter
     *            the filter
     * @param date
     *            the date
     * @param includeStdby
     *            the include stdby
     * @param pattern
     *            the pattern
     *
     * @return the string
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
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
