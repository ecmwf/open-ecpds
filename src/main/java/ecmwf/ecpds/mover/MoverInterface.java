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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.script.ScriptException;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.ecaccess.ClientInterface;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.technical.ProxySocket;

/**
 * The Interface MoverInterface.
 */
public interface MoverInterface extends ClientInterface {

    /**
     * Size.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     *
     * @return the long
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    long size(DataTransfer transfer, String fileName) throws RemoteException;

    /**
     * Del.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void del(DataTransfer transfer, String fileName) throws RemoteException;

    /**
     * Del.
     *
     * @param proxyHost
     *            the proxy host
     * @param dataFile
     *            the data file
     *
     * @return true, if successful
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    boolean del(Host proxyHost, DataFile dataFile) throws RemoteException;

    /**
     * Purge.
     *
     * @param directories
     *            the directories
     *
     * @throws RemoteException
     *             the remote exception
     */
    void purge(List<ExistingStorageDirectory> directories) throws RemoteException;

    /**
     * Purge.
     *
     * @param proxyHost
     *            the proxy host
     * @param directories
     *            the directories
     *
     * @throws RemoteException
     *             the remote exception
     */
    void purge(Host proxyHost, List<ExistingStorageDirectory> directories) throws RemoteException;

    /**
     * Puts the.
     *
     * @param hostsForSource
     *            the hosts for source
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     * @param localPosn
     *            the local posn
     * @param remotePosn
     *            the remote posn
     *
     * @return the data transfer
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DataTransfer put(Host[] hostsForSource, DataTransfer transfer, String fileName, long localPosn, long remotePosn)
            throws RemoteException;

    /**
     * Puts the.
     *
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
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket put(Host host, String target, long remotePosn, long size) throws RemoteException;

    /**
     * Gets the.
     *
     * @param host
     *            the host
     * @param source
     *            the source
     * @param remotePosn
     *            the remote posn
     * @param removeOriginal
     *            the remove original
     *
     * @return the proxy socket
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket get(Host host, String source, long remotePosn, boolean removeOriginal) throws RemoteException;

    /**
     * Gets the.
     *
     * @param dataFile
     *            the data file
     * @param hostForSource
     *            the host for source
     * @param remotePosn
     *            the remote posn
     *
     * @return the proxy socket
     *
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket get(DataFile dataFile, Host hostForSource, long remotePosn) throws RemoteException;

    /**
     * Gets the.
     *
     * @param dataFile
     *            the data file
     * @param hostForSource
     *            the host for source
     * @param remotePosn
     *            the remote posn
     * @param length
     *            the length
     *
     * @return the proxy socket
     *
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ProxySocket get(DataFile dataFile, Host[] hostForSource, long remotePosn, long length) throws RemoteException;

    /**
     * Size.
     *
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @return the long
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    long size(Host host, String source) throws RemoteException;

    /**
     * Del.
     *
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void del(Host host, String source) throws RemoteException;

    /**
     * Mkdir.
     *
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void mkdir(Host host, String dir) throws RemoteException;

    /**
     * Rmdir.
     *
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void rmdir(Host host, String dir) throws RemoteException;

    /**
     * List.
     *
     * @param host
     *            the host
     * @param source
     *            the source
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String[] listAsStringArray(Host host, String source, String pattern) throws RemoteException;

    /**
     * List2.
     *
     * @param host
     *            the host
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param synchronous
     *            the synchronous
     *
     * @return the remote input stream
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    RemoteInputStream listAsByteArray(Host host, String directory, String pattern, boolean synchronous)
            throws RemoteException;

    /**
     * Execute.
     *
     * @param script
     *            the script
     *
     * @return the remote input stream
     *
     * @throws ScriptException
     *             the script exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    RemoteInputStream execute(String script) throws RemoteException;

    /**
     * Move.
     *
     * @param host
     *            the host
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void move(Host host, String source, String target) throws RemoteException;

    /**
     * Close.
     *
     * @param transfer
     *            the transfer
     *
     * @return true, if successful
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    boolean close(DataTransfer transfer) throws RemoteException;

    /**
     * Replicate.
     *
     * @param dataFile
     *            the data file
     * @param targetHost
     *            the target host
     * @param hostsForSource
     *            the hosts for source
     *
     * @return the data file
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DataFile replicate(DataFile dataFile, Host targetHost, Host[] hostsForSource) throws RemoteException;

    /**
     * Download.
     *
     * @param dataFile
     *            the data file
     * @param hostForSource
     *            the host for source
     *
     * @return the data file
     *
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DataFile download(DataFile dataFile, Host hostForSource) throws RemoteException;

    /**
     * Filter.
     *
     * @param dataFile
     *            the data file
     * @param remove
     *            the remove
     *
     * @return the data file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DataFile filter(DataFile dataFile, boolean remove) throws RemoteException;

    /**
     * Del.
     *
     * @param dataFile
     *            the data file
     *
     * @return true, if successful
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    boolean del(DataFile dataFile) throws RemoteException;

    /**
     * Check.
     *
     * @param ticket
     *            the ticket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void check(long ticket) throws RemoteException;

    /**
     * Gets the mover report.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return the mover report
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getMoverReport(Host proxyHost) throws RemoteException;

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
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getHostReport(Host proxyHost, Host host) throws RemoteException;

    /**
     * Gets the report.
     *
     * @return the report
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getReport() throws RemoteException;

    /**
     * Gets the report.
     *
     * @param host
     *            the host
     *
     * @return the report
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getReport(Host host) throws RemoteException;

    /**
     * Gets the incoming connection ids.
     *
     * @return the incoming connection ids
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String[] getIncomingConnectionIds() throws RemoteException;

    /**
     * Gets the incoming connections.
     *
     * @return the incoming connections
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    List<IncomingConnection> getIncomingConnections() throws RemoteException;

    /**
     * Close incoming connection.
     *
     * @param id
     *            the id
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    boolean closeIncomingConnection(String id) throws RemoteException;

    /**
     * Close all incoming connections.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void closeAllIncomingConnections() throws RemoteException;

    /**
     * Publish a notification to the MQTT service.
     *
     * @param topic
     *            the topic
     * @param qos
     *            the qos
     * @param expiryInterval
     *            the expiry interval
     * @param contentType
     *            the content type
     * @param clientId
     *            the client id
     * @param payload
     *            the payload
     * @param retain
     *            the retain
     *
     * @throws RemoteException
     *             the remote exception
     */
    void publishToMQTTBroker(final String topic, final int qos, final long expiryInterval, final String contentType,
            final String clientId, final String payload, final boolean retain) throws RemoteException;

    /**
     * Remove a retained notification from the MQTT broker.
     *
     * @param topic
     *            the topic
     *
     * @throws RemoteException
     *             the remote exception
     */
    void removeFromMQTTBroker(final String topic) throws RemoteException;

    /**
     * Get the number of client connected to the MQTT broker.
     *
     * @return the MQTT clients count
     *
     * @throws RemoteException
     *             the remote exception
     */
    int getMQTTClientsCount() throws RemoteException;

    /**
     * Shutdown.
     *
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     *
     * @throws RemoteException
     *             the remote exception
     */
    void shutdown(boolean graceful, boolean restart) throws RemoteException;

    /**
     * Gets the ECproxy plugging listen address and port. This is used by the Master Server to update the
     * "TransferServer" entry in the database when the Mover subscribe. This address and port are used when the Master
     * Server send the address of the allocated Mover to the ecpds command.
     *
     * @return the ECproxyPlugin address and port
     *
     * @throws RemoteException
     *             the remote exception
     */
    String getECproxyAddressAndPort() throws RemoteException;
}
