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

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
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

/**
 * The Interface ManagementInterface.
 */
public interface ManagementInterface extends Remote {
    /** The Constant DESTINATION_STATE_ONLINE. */
    int DESTINATION_STATE_ONLINE = 0;

    /** The Constant DESTINATION_STATE_OPENING. */
    int DESTINATION_STATE_OPENING = 1;

    /** The Constant DESTINATION_STATE_CLOSING. */
    int DESTINATION_STATE_CLOSING = 2;

    /** The Constant DESTINATION_STATE_OFFLINE. */
    int DESTINATION_STATE_OFFLINE = 3;

    /** The Constant DESTINATION_STATE_JAMMED. */
    int DESTINATION_STATE_JAMMED = 4;

    /** The Constant DESTINATION_STATE_ONHOLD. */
    int DESTINATION_STATE_ONHOLD = 5;

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
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    List<String> getDestinationNamesForContact(List<Map.Entry<String, String>> emailPattern, boolean caseSensitive)
            throws IOException;

    /**
     * Gets the contacts.
     *
     * @return the contacts
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    Map<String, String> getContacts() throws MasterException, IOException;

    /**
     * Gets the destination caches.
     *
     * @return the destination caches
     *
     * @throws ecmwf.common.monitor.MonitorException
     *             the monitor exception
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Map<String, DestinationCache> getDestinationCaches()
            throws MonitorException, MasterException, DataBaseException, RemoteException;

    /**
     * Gets the monitor manager.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the monitor manager
     *
     * @throws ecmwf.common.monitor.MonitorException
     *             the monitor exception
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    MonitorManager getMonitorManager(String destinationName) throws MonitorException, MasterException, RemoteException;

    /**
     * Gets the destination status.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the destination status
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String getDestinationStatus(String destinationName) throws MasterException, DataBaseException, RemoteException;

    /**
     * Gets the destination size.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the destination size
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    int getDestinationSize(String destinationName) throws MasterException, RemoteException;

    /**
     * Gets the destination start date.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the destination start date
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Date getDestinationStartDate(String destinationName) throws MasterException, RemoteException;

    /**
     * Gets the pending data transfers count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the pending data transfers count
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    int getPendingDataTransfersCount(String destinationName) throws MasterException, RemoteException;

    /**
     * Gets the destination last transfer.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the destination last transfer
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    DataTransfer getDestinationLastTransfer(String destinationName) throws MasterException, RemoteException;

    /**
     * Gets the destination last failed transfer.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the destination last failed transfer
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    DataTransfer getDestinationLastFailedTransfer(String destinationName) throws MasterException, RemoteException;

    /**
     * Gets the retrieved.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the retrieved
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    long getRetrieved(long dataFileId) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer server name.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the transfer server name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String getTransferServerName(long dataFileId) throws DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    ECpdsSession getECpdsSession(String user, String password, String host, String agent, String comment)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Creates the web user.
     *
     * @param session
     *            the session
     * @param webUser
     *            the web user
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void saveWebUser(ECpdsSession session, WebUser webUser) throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    DestinationCache copyDestination(ECpdsSession session, String fromDestination, String toDestination, String label,
            boolean copySharedHost) throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    DestinationCache copyHost(final ECpdsSession session, final String destinationName, final String hostName)
            throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void exportDestination(ECpdsSession session, String targetMaster, String fromDestination, String toDestinationName,
            boolean copySharedHost) throws MasterException, DataBaseException, RemoteException;

    /**
     * Close ecpds session.
     *
     * @param session
     *            the session
     * @param expired
     *            the expired
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void closeECpdsSession(ECpdsSession session, boolean expired) throws RemoteException;

    /**
     * Close an incoming connection.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void closeIncomingConnection(ECpdsSession session, String id) throws IOException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    DestinationSchedulerCache restartDestination(ECpdsSession session, String destinationName, boolean graceful)
            throws MasterException, RemoteException, DataBaseException;

    /**
     * Restart all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void restartAllDestinations(ECpdsSession session, boolean graceful) throws MasterException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void shutdownTransferServer(ECpdsSession session, TransferServer server, boolean graceful, boolean restart)
            throws MasterException, RemoteException;

    /**
     * Hold destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the Destination name
     * @param graceful
     *            the graceful
     *
     * @return the destination scheduler cache
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    DestinationSchedulerCache holdDestination(ECpdsSession session, String destinationName, boolean graceful)
            throws MasterException, RemoteException, DataBaseException;

    /**
     * Hold all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void holdAllDestinations(ECpdsSession session, boolean graceful) throws MasterException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    boolean transferStatusUpdateAllowed(long id, String code) throws MasterException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    boolean updateTransferStatus(ECpdsSession session, long id, String code) throws MasterException, RemoteException;

    /**
     * Reset transfer schedule date.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void resetTransferScheduleDate(ECpdsSession session, long id)
            throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void updateTransferPriority(ECpdsSession session, long id, int priority)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Update expiry time.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param timetamp
     *            the timetamp
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void updateExpiryTime(ECpdsSession session, long id, Timestamp timetamp)
            throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    long transfer(ECpdsSession session, byte[] bytes, Host host, String target, long remotePosn)
            throws MasterException, DataBaseException, IOException;

    /**
     * Gets the mover report.
     *
     * @param session
     *            the session
     * @param proxyHost
     *            the proxy host
     *
     * @return the mover report
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    String getMoverReport(ECpdsSession session, Host proxyHost) throws MasterException, DataBaseException, IOException;

    /**
     * Gets the host report.
     *
     * @param session
     *            the session
     * @param proxyHost
     *            the proxy host
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    String getHostReport(ECpdsSession session, Host proxyHost, Host host)
            throws MasterException, DataBaseException, IOException;

    /**
     * Clean the data window host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void cleanDataWindow(ECpdsSession session, Host host) throws MasterException, DataBaseException, IOException;

    /**
     * Reset the host stats.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void resetTransferStatistics(ECpdsSession session, Host host)
            throws MasterException, DataBaseException, IOException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    String getReport(ECpdsSession session, Host host) throws MasterException, DataBaseException, IOException;

    /**
     * Gets the host output.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the report
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    RemoteInputStream getOutput(ECpdsSession session, Host host) throws MasterException, DataBaseException, IOException;

    /**
     * Check whether an acquisition thread is currently running for the given Host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return true if an acquisition thread for this host is active right now
     */
    boolean isAcquisitionRunning(ECpdsSession session, Host host) throws RemoteException;

    /**
     * Trigger acquisition for the given Host immediately by resetting its acquisition time. Does nothing if the host is
     * already running.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     */
    void triggerAcquisition(ECpdsSession session, Host host) throws RemoteException;

    /**
     * Execute a Directory script on the DataMover for the given host and return the result as a GZIPped input stream.
     * This replicates the production acquisition path: the script is sent to a DataMover in the host's transfer group.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     * @param script
     *            the script content, optionally prefixed with "python:" or "js:" to select the language
     *
     * @return a GZIPped input stream containing the script output (a newline-separated list of paths or a URL)
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String execDirScript(ECpdsSession session, Host host, String script)
            throws MasterException, DataBaseException, IOException;

    /**
     * Resolve all static placeholder tokens in a plain-text Directory field and return the resolved text. Substitutes
     * $host[...], $transferMethod[...], $ectransModule[...] and $date/$dirdate tokens using the current date. Does not
     * require a DataMover — runs entirely on the MasterServer.
     *
     * @param session
     *            the session
     * @param host
     *            the host whose fields are used for substitution
     * @param text
     *            the plain-text directory content
     *
     * @return the resolved text
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    String resolveDirText(ECpdsSession session, Host host, String text)
            throws MasterException, DataBaseException, IOException;

    /**
     * Fetch the raw content of a remote URL/path via the host's configured ECtrans module on the DataMover.
     *
     * @param session
     *            the session
     * @param host
     *            the host whose ECtrans module is used
     * @param source
     *            the remote URL or path to retrieve
     * @param maxBytes
     *            maximum number of bytes to return
     *
     * @return the content as a UTF-8 string (truncated if needed)
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String fetchUrlContent(ECpdsSession session, Host host, String source, int maxBytes)
            throws MasterException, DataBaseException, IOException;

    /**
     * Interrupt a running acquisition thread for the given Host and immediately restart it. If no acquisition is
     * running the host output is still reset, triggering a fresh run on the next scheduler cycle.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     */
    void interruptAcquisition(ECpdsSession session, Host host) throws RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    String getReport(ECpdsSession session, TransferServer server)
            throws MasterException, DataBaseException, IOException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Host updateHost(ECpdsSession session, Host host) throws MasterException, DataBaseException, RemoteException;

    /**
     * Update transfer monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the monitoring value
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void updateTransferMonitoringValue(ECpdsSession session, MonitoringValue value)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Update file monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the monitoring value
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void updateFileMonitoringValue(ECpdsSession session, MonitoringValue value)
            throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void shutdown(ECpdsSession session, boolean graceful, boolean restart) throws MasterException, RemoteException;

    /**
     * Removes the transfer method.
     *
     * @param session
     *            the session
     * @param method
     *            the method
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeTransferMethod(ECpdsSession session, TransferMethod method)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the ectrans module.
     *
     * @param session
     *            the session
     * @param module
     *            the module
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeECtransModule(ECpdsSession session, ECtransModule module)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the transfer group.
     *
     * @param session
     *            the session
     * @param group
     *            the group
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeTransferGroup(ECpdsSession session, TransferGroup group)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the Destination name
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeDestination(ECpdsSession session, String destinationName)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the Destination name
     * @param cleanOnly
     *            clean only and don't delete the Destination
     * @param removeAll
     *            remove all files or only deleted, expired, stopped and failed ones if clean only
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeDestination(final ECpdsSession session, final String destinationName, final boolean cleanOnly,
            final boolean removeAll) throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the transfer server.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeTransferServer(ECpdsSession session, TransferServer server)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeHost(ECpdsSession session, Host host) throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the IncomingUser.
     *
     * @param session
     *            the session
     * @param user
     *            the incoming user
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeIncomingUser(ECpdsSession session, IncomingUser user)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the IncomingPolicy.
     *
     * @param session
     *            the session
     * @param policy
     *            the policy
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeIncomingPolicy(ECpdsSession session, IncomingPolicy policy)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the data transfer.
     *
     * @param session
     *            the session
     * @param transfer
     *            the transfer
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeDataTransfer(ECpdsSession session, DataTransfer transfer)
            throws MasterException, DataBaseException, RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    boolean interruptDataTransferRetrieval(ECpdsSession session, long id)
            throws MasterException, RemoteException, DataBaseException;

    /**
     * Removes the data file.
     *
     * @param session
     *            the session
     * @param file
     *            the file
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeDataFile(ECpdsSession session, DataFile file) throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the web user.
     *
     * @param session
     *            the session
     * @param user
     *            the web user
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeWebUser(ECpdsSession session, WebUser user) throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the category.
     *
     * @param session
     *            the session
     * @param category
     *            the category
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeCategory(ECpdsSession session, Category category)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the url.
     *
     * @param session
     *            the session
     * @param url
     *            the url
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void removeUrl(ECpdsSession session, Url url) throws MasterException, DataBaseException, RemoteException;

    /**
     * Resend data transfer events.
     *
     * @param root
     *            the root
     * @param dataTransferEventRequests
     *            the data transfer event requests
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void resendDataTransferEvents(String root, DataTransferEventRequest[] dataTransferEventRequests)
            throws RemoteException;

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
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    byte[] exec(ECpdsSession session, Map<String, String> environment, byte[] request, String service)
            throws MasterException, IOException;

    /**
     * Gets the destination scheduler cache.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination scheduler cache
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    DestinationSchedulerCache getDestinationSchedulerCache(String destinationName)
            throws MasterException, RemoteException;

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String computeFilterEfficiency(final ECpdsSession session, final String destinationName, final String email,
            final String filter, final long date, boolean includeStdby, String pattern)
            throws DataBaseException, MasterException, RemoteException;

    /**
     * Gets a snapshot of volume disk usage for one or all transfer groups.
     *
     * <p>
     * Returns the most recently polled used and total bytes per volume index, as maintained by the background usage
     * updater in {@code TransferServerProvider.WeightedAllocator}. No DataMover RMI calls are made; the result comes
     * from the in-memory cache only.
     * </p>
     *
     * @param groupName
     *            the transfer group name to query, or {@code null} for all registered groups
     *
     * @return a map of group name to {@code long[volumeIndex][2]} where {@code [i][0]} is used bytes and {@code [i][1]}
     *         is total bytes; never {@code null}; empty if no data has been collected yet
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Map<String, long[][]> getVolumeUsage(String groupName) throws MasterException, RemoteException;

    /**
     * Gets a snapshot of per-volume disk usage for one or all DataMovers.
     *
     * <p>
     * Returns the most recently polled used and total bytes per volume index for each DataMover, as cached by the
     * background usage updater. No DataMover RMI calls are made at query time.
     * </p>
     *
     * @param moverName
     *            the DataMover name to query, or {@code null} for all DataMovers
     *
     * @return a map of mover name to {@code long[2][volumeCount]} where {@code [0][i]} is used bytes and {@code [1][i]}
     *         is total bytes for volume {@code i}; never {@code null}
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Map<String, long[][]> getMoverVolumeUsage(String moverName) throws MasterException, RemoteException;

    /**
     * Returns availability snapshots for the given DataMover from the database.
     *
     * <p>
     * Each element is a {@code long[2]} where {@code [0]} is the minute bucket in epoch-ms and {@code [1]} is {@code 1}
     * (mover was up) or {@code 0} (mover was down) during that minute.
     * </p>
     *
     * @param moverName
     *            the data mover name
     * @param hours
     *            how many hours of history to return
     *
     * @return time-ordered list of minute-bucket availability entries; never {@code null}
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    java.util.List<long[]> getMoverAvailability(String moverName, int hours) throws MasterException, RemoteException;

    /**
     * Returns a snapshot of the current in-flight download counts for all data movers and all volumes.
     *
     * <p>
     * The map key is {@code "groupName.moverName"} (e.g. {@code "group-04.bodh1ecpdmv-04"}). The value is an
     * {@code int[]} where index {@code i} holds the current download count on volume {@code i}. Only movers with at
     * least one recorded download event appear in the map.
     * </p>
     *
     * @return a sorted snapshot of download counts per mover and volume; never {@code null}
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    Map<String, int[]> getDownloadMetrics() throws MasterException, RemoteException;
}
