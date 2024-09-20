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

package ecmwf.ecpds.mover.service;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.RestException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostLocation;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.security.SSLSocketFactory;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.mover.RESTInterface;

/**
 * The Class RESTClient.
 */
public final class RESTClient implements RESTInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTClient.class);

    /** The Constant _debug. */
    private static final boolean _debug = Cnf.at("RESTClient", "debug", false);

    /** The _http mover. */
    private final List<String> _httpMover = Collections.synchronizedList(new ArrayList<>());

    /** The _http proxy. */
    private final String _httpProxy;

    /** The _connect timeout. */
    private final int _connectTimeout;

    static {
        // If requested make sure the REST client can connect to URL with
        // unknown certificates!
        if (Cnf.at("MoverProxy", "trustAllCerts", true)) {
            _log.debug("Activating trusted certificates for all https connections");
            try {
                SSLSocketFactory.setHttpsURLConnectionTrustAllCerts();
            } catch (final Throwable t) {
                _log.warn("Trusting all https certificates", t);
            }
        }
    }

    /**
     * Select randomly in the list of DataMovers available.
     *
     * @return the string[]
     */
    private String[] _getDataMover() {
        // If only one DataMover is available we return it straight away. This
        // is true when the Master wants to contact a specific DataMover!
        final var length = _httpMover.size();
        if (length == 1) {
            return new String[] { _httpMover.get(0) };
        }
        // Let's find a random position in the list!
        final var pos = ThreadLocalRandom.current().nextInt(_httpMover.size());
        // Now we build the list starting from this random position till we go
        // back to the previous position!
        final List<String> dataMovers = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            dataMovers.add(_httpMover.get((pos + i) % length));
        }
        // As a list of String
        return dataMovers.toArray(new String[dataMovers.size()]);
    }

    /**
     * Instantiates a new REST client.
     *
     * @param httpProxy
     *            the http proxy
     * @param httpMover
     *            the http mover
     * @param connectTimeout
     *            the connect timeout
     */
    RESTClient(final String httpProxy, final String httpMover, final int connectTimeout) {
        final var token = new StringTokenizer(httpMover, ";,");
        while (token.hasMoreElements()) {
            _httpMover.add(token.nextToken());
        }
        _httpProxy = httpProxy;
        _connectTimeout = connectTimeout;
    }

    /**
     * Get the version of the remote ECaccess software (mover).
     *
     * @return the version
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public String getVersion() throws RestException {
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/getVersion", _connectTimeout)
                .accept(MediaType.APPLICATION_JSON).get();
        return _parse(response, String.class);
    }

    /**
     * Delete the physical file associated to the DataFile on the mover.
     *
     * @param dataFile
     *            the data file
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void del(final DataFile dataFile) throws RestException {
        // We create a new one with only the required fields to close the
        // DataFile!
        final var f = new DataFile();
        f.setId(dataFile.getId());
        f.setFileInstance(dataFile.getFileInstance());
        f.setFileSystem(dataFile.getFileSystem());
        f.setArrivedTime(dataFile.getArrivedTime());
        f.setTimeStep(dataFile.getTimeStep());
        f.setOriginal(dataFile.getOriginal());
        _log.debug("REST sending request: del(" + f + ")");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/del", _connectTimeout)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .invoke("DELETE", ClientResponse.class, f);
        _parse(response);
    }

    /**
     * Stop the transmission of the DataTransfer on the mover.
     *
     * @param dataTransfer
     *            the data transfer
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void close(final DataTransfer dataTransfer) throws RestException {
        // We extract the Host
        final var host = dataTransfer.getHost();
        // And we create a new one with only the required fields to close the
        // DataTransfer!
        final var h = new Host();
        h.setName(host.getName());
        h.setECUserName(host.getECUserName());
        h.setTransferMethodName(host.getTransferMethodName());
        // Now let's create a DataTransfer with the minimum information!
        final var t = new DataTransfer();
        t.setId(dataTransfer.getId());
        t.setStartCount(dataTransfer.getStartCount());
        t.setHostName(h.getName());
        t.setHost(h);
        _log.debug("REST sending request: close(" + t + ")");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/close", _connectTimeout)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .invoke("DELETE", ClientResponse.class, t);
        _parse(response);
    }

    /**
     * Purge. Request an asynchronous purge of the DataFiles on the data mover which are more than the specified date.
     *
     * @param directories
     *            the directories
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void purge(final List<ExistingStorageDirectory> directories) throws RestException {
        _log.debug("REST sending request: purge("
                + (directories != null ? directories.size() + " directories" : "no-directory") + ")");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/purge", _connectTimeout)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .invoke("DELETE", ClientResponse.class, directories);
        _parse(response);
    }

    /**
     * Gets the host report. Request a report for the specified Host (e.g. traceroute, paping, mtr).
     *
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public String getHostReport(final Host host) throws RestException {
        // We build the Host with all the required fields!
        final var l = new HostLocation();
        l.setId(host.getHostLocationId());
        l.setIp(host.getHostLocation().getIp());
        final var h = new Host();
        h.setName(host.getName());
        h.setECUserName(host.getECUserName());
        h.setECUser(host.getECUser());
        h.setFilterName(host.getFilterName());
        h.setTransferMethodName(host.getTransferMethodName());
        h.setTransferMethod(host.getTransferMethod());
        h.setDir(host.getDir());
        h.setHost(host.getHost());
        h.setHostLocation(l);
        h.setData(host.getData());
        h.setActive(host.getActive());
        h.setComment(host.getComment());
        h.setLogin(host.getLogin());
        h.setPasswd(host.getPasswd());
        h.setUserMail(host.getUserMail());
        h.setNetworkCode(host.getNetworkCode());
        h.setNetworkName(host.getNetworkName());
        h.setNickname(host.getNickname());
        _log.debug("REST sending request: getHostReport(" + h + ")");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/getHostReport",
                _connectTimeout).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).put(h);
        return _parse(response, String.class);
    }

    /**
     * Gets the mover report. Request a report from the Data Mover (e.g. df, sar).
     *
     * @return the mover report
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public String getMoverReport() throws RestException {
        _log.debug("REST sending request: getMoverReport()");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/getMoverReport",
                _connectTimeout).accept(MediaType.APPLICATION_JSON).get();
        return _parse(response, String.class);
    }

    /**
     * Request a transmission of the DataTransfer with the target name as specified in fileName.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     * @param localPosn
     *            the local posn
     * @param remotePosn
     *            the remote posn
     *
     * @return the string
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public String put(final DataTransfer transfer, final String fileName, final long localPosn, final long remotePosn)
            throws RestException {
        _log.debug("REST sending request: put(" + transfer + "," + fileName + "," + localPosn + "," + remotePosn + ")");
        final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/mover/put", _connectTimeout)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .put(getPutRequest(transfer, fileName, localPosn, remotePosn));
        return _parse(response, String.class);
    }

    /**
     * Request a ecauth token to allow a connection to a ecauth compliant server.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public ECauthToken getECauthToken(final String user) throws RestException {
        _log.debug("REST sending request: getECauthToken(" + user + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/getECauthToken",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).queryParam("user", user).get();
                return _parse(response, ECauthToken.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Check if the DataFile exists and is not expired.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return true, if is valid data file
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public boolean isValidDataFile(final long dataFileId) throws Exception {
        _log.debug("REST sending request: isValidDataFile(" + dataFileId + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, _getDataMover()[0] + "/ecpds/master/isValidDataFile",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).queryParam("dataFileId", dataFileId).get();
                return _parse(response, boolean.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Send a live message from the ProxyHost to the Master server.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public long proxyHostIsAlive(final String name) throws RestException {
        _log.debug("REST sending request: proxyHostIsAlive(" + name + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/proxyHostIsAlive",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .put(name);
                return _parse(response, long.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Request an update of the data part of the Host on the master (e.g. when the data is updated by the ectrans
     * module).
     *
     * @param request
     *            the request
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void updateDataRequest(final UpdateDataRequest request) throws RestException {
        _log.debug("REST sending request: updateDataRequest(" + request + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/updateDataRequest",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .put(request);
                _parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Request an update of the data part of the Host on the master (e.g. when the data is updated by the ectrans
     * module).
     *
     * @param host
     *            the host
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void updateData(final Host host) throws RestException {
        _log.debug("REST sending request: updateData(" + host + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/updateData", _connectTimeout)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).put(host);
                _parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Update location.
     *
     * Request an update of the location part of the Host on the master (e.g. when the IP is updated by the ectrans
     * module).
     *
     * @param host
     *            the host
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void updateLocation(final Host host) throws RestException {
        _log.debug("REST sending request: updateLocation(" + host + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/updateLocation",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .put(host);
                _parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Request an update of the transfers on the master (e.g. status).
     *
     * @param transfers
     *            the transfers
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void updateDataTransfers(final List<DataTransfer> transfers) throws RestException {
        _log.debug("REST sending request: updateDataTransfers("
                + (transfers != null ? transfers.size() + " transfer(s)" : "no-transfer") + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/updateDataTransfers",
                        _connectTimeout).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .put(transfers);
                _parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Send a message using Monitor.
     *
     * @param request
     *            the request
     *
     * @throws RestException
     *             the rest exception
     */
    @Override
    public void sendMessage(final MonitorRequest request) throws RestException {
        _log.debug("REST sending request: sendMessage(" + request + ")");
        RestException restException = null;
        for (final String dataMover : _getDataMover()) {
            try {
                final var response = _getResource(_httpProxy, dataMover + "/ecpds/master/sendMessage", _connectTimeout)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).put(request);
                _parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        // We could not process the request. If any RestException was thrown
        // then we trough the last one! Otherwise we inform the Mover that no
        // MasterServer is available!
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    /**
     * Request for a transmission of a DataTransfer.
     *
     * @param transfer
     *            the transfer
     * @param fileName
     *            the file name
     * @param localPosn
     *            the local posn
     * @param remotePosn
     *            the remote posn
     *
     * @return the puts the request
     */
    public static PutRequest getPutRequest(final DataTransfer transfer, final String fileName, final Long localPosn,
            final Long remotePosn) {
        // Let's create a new DataTransfer with only the information needed.
        // This will avoid any infinite recursion when JSON is doing the parsing
        // and it will also minimise the size of the request!
        final var file = transfer.getDataFile();
        final var host = transfer.getHost();
        // We first build the Host with all the required fields!
        final var l = new HostLocation();
        l.setId(host.getHostLocationId());
        l.setIp(host.getHostLocation().getIp());
        final var h = new Host();
        h.setName(host.getName());
        h.setECUserName(host.getECUserName());
        h.setECUser(host.getECUser());
        h.setFilterName(host.getFilterName());
        h.setTransferMethodName(host.getTransferMethodName());
        h.setTransferMethod(host.getTransferMethod());
        h.setDir(host.getDir());
        h.setHost(host.getHost());
        h.setHostLocation(l);
        h.setData(host.getData());
        h.setActive(host.getActive());
        h.setComment(host.getComment());
        h.setLogin(host.getLogin());
        h.setPasswd(host.getPasswd());
        h.setNickname(host.getNickname());
        // And now the ProxyHost!
        final var proxy = transfer.getProxyHost();
        final var p = new Host();
        p.setName(proxy.getName());
        p.setNickname(proxy.getNickname());
        // Now we build the DataFile!
        final var f = new DataFile();
        f.setId(file.getId());
        f.setDownloaded(file.getDownloaded());
        f.setSource(file.getSource());
        f.setFileInstance(file.getFileInstance());
        f.setFileSystem(file.getFileSystem());
        f.setArrivedTime(file.getArrivedTime());
        f.setTimeStep(file.getTimeStep());
        f.setOriginal(file.getOriginal());
        f.setSize(file.getSize());
        f.setChecksum(file.getChecksum());
        // And now the DataTransfer!
        final var t = new DataTransfer();
        t.setId(transfer.getId());
        t.setHostName(h.getName());
        t.setHost(h);
        t.setProxyHostName(p.getName());
        t.setProxyHost(p);
        t.setDataFileId(f.getId());
        t.setDataFile(f);
        t.setStartCount(transfer.getStartCount());
        t.setStatusCode(transfer.getStatusCode());
        t.setDestinationName(transfer.getDestinationName());
        t.setFirstFinishTime(transfer.getFirstFinishTime());
        t.setQueueTime(transfer.getQueueTime());
        t.setPriority(transfer.getPriority());
        t.setAsap(transfer.getAsap());
        t.setEvent(transfer.getEvent());
        t.setDuration(0);
        t.setSent(0);
        // Now we can build the request!
        final var request = new PutRequest();
        request.transfer = t;
        request.fileName = fileName;
        request.localPosn = localPosn;
        request.remotePosn = remotePosn;
        return request;
    }

    /**
     * The Class PutRequest.
     */
    public static final class PutRequest implements Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 4903532344315324986L;

        /** The transfer. */
        public DataTransfer transfer;

        /** The file name. */
        public String fileName;

        /** The local posn. */
        public Long localPosn;

        /** The remote posn. */
        public Long remotePosn;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return (transfer != null ? transfer.toString() : null) + "," + fileName + "," + localPosn + ","
                    + remotePosn;
        }
    }

    /**
     * The Class MonitorRequest.
     */
    public static final class MonitorRequest implements Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 9165947353246228909L;

        /** The name. */
        public String name;

        /** The service. */
        public String service;

        /** The status. */
        public Integer status;

        /** The message. */
        public String message;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return name + "," + service + "," + status + "," + message;
        }
    }

    /**
     * The Class UpdateDataRequest.
     */
    public static final class UpdateDataRequest implements Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 6165347353546221908L;

        /** The host id. */
        public String hostId;

        /** The data. */
        public String data;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return hostId + "," + data;
        }
    }

    // Utilities

    /**
     * Gets the resource.
     *
     * @param proxy
     *            the proxy
     * @param url
     *            the url
     * @param connectTimeout
     *            the connect timeout
     *
     * @return the resource
     */
    private static Resource _getResource(final String proxy, final String url, final int connectTimeout) {
        final var config = new ClientConfig().applications(new ECaccessRESTApplication());
        config.setBypassHostnameVerification(true);
        config.connectTimeout(connectTimeout);
        final int index;
        if (proxy != null && (index = proxy.indexOf(":")) != -1) {
            config.proxyHost(proxy.substring(0, index));
            config.proxyPort(Integer.parseInt(proxy.substring(index + 1)));
        }
        _log.debug("REST connection: " + url + " (proxy=" + proxy + ",connectTimeout=" + connectTimeout + ")");
        final var resource = new RestClient(config).resource(url);
        return resource.header("Accept-Charset", "iso-8859-1");
    }

    /**
     * Parses the.
     *
     * @param <T>
     *            the generic type
     * @param response
     *            the response
     * @param valueType
     *            the value type
     *
     * @return the t
     *
     * @throws RestException
     *             the rest exception
     */
    private static <T> T _parse(final ClientResponse response, final Class<T> valueType) throws RestException {
        return _parse(response.getEntity(String.class), valueType, null);
    }

    /**
     * Parses the.
     *
     * @param response
     *            the response
     *
     * @throws RestException
     *             the rest exception
     */
    private static void _parse(final ClientResponse response) throws RestException {
        _parse(response.getEntity(String.class), null, null);
    }

    /**
     * This parser is expecting a single field in addition to the "success" field. This additional field can either be
     * an Object, a primitive or an array.
     *
     * @param <T>
     *            the generic type
     * @param message
     *            the message
     * @param valueType
     *            the value type
     * @param valueTypeRef
     *            the value type ref
     *
     * @return the t
     *
     * @throws RestException
     *             the rest exception
     */
    private static <T> T _parse(final String message, final Class<T> valueType, final TypeReference<T> valueTypeRef)
            throws RestException {
        if (_debug) {
            _log.debug("Parsing message: " + message);
        }
        T result = null;
        var errorMessage = "";
        var error = false;
        if (message == null) {
            throw new RestException("Service not found");
        }
        try {
            final var jp = new JsonFactory().createJsonParser(message);
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                final var fieldName = jp.getCurrentName();
                jp.nextToken();
                if (_debug) {
                    _log.debug("Parsing " + fieldName + "=" + jp.getText() + " (" + jp.getCurrentToken().name() + ")");
                }
                if ("success".equals(fieldName)) {
                    error = !"yes".equals(jp.getText());
                } else if ("error".equals(fieldName)) {
                    errorMessage = jp.getText();
                } else if (valueTypeRef != null && jp.getCurrentToken() == JsonToken.START_ARRAY) {
                    if (_debug) {
                        _log.debug("Object " + fieldName + " is array of " + valueTypeRef.getType().toString());
                    }
                    final var mapper = new ObjectMapper();
                    result = mapper.readValue(jp, valueTypeRef);
                } else if (valueType != null) {
                    if (_debug) {
                        _log.debug("Object " + fieldName + " is " + valueType.getSimpleName());
                    }
                    final var mapper = new ObjectMapper();
                    result = mapper.readValue(jp, valueType);
                } else if (_debug) {
                    _log.debug("Skipping " + fieldName);
                }
            }
        } catch (final Throwable t) {
            throw new RestException("Parsing JSON message: " + message, t);
        }
        if (error) {
            throw new RestException(errorMessage);
        }
        _log.debug("Result: " + result);
        return result;
    }

    /**
     * The Class ECaccessRESTApplication.
     */
    public static final class ECaccessRESTApplication extends Application {
        /** The singletons. */
        private final Set<Object> singletons;

        /**
         * Gets the singletons.
         *
         * @return the singletons
         */
        @Override
        public Set<Object> getSingletons() {
            return singletons;
        }

        /**
         * Instantiates a new e caccess rest application.
         */
        public ECaccessRESTApplication() {
            singletons = new HashSet<>();
            singletons.add(RESTProvider.getJacksonProvider());
        }
    }
}
