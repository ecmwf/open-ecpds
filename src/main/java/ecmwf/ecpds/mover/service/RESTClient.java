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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

/**
 * The Class RESTClient.
 */
public final class RESTClient implements RESTInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTClient.class);

    /** Shared JSON mapper. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** The Constant debug. */
    private static final boolean debug = Cnf.at("RESTClient", "debug", false);

    /** The http mover list. */
    private final List<String> httpMoverList = Collections.synchronizedList(new ArrayList<>());

    /** The http proxy. */
    private final String httpProxy;

    /** The connect timeout. */
    private final int connectTimeout;

    /** Whether to trust all certificates. */
    private static final boolean TRUST_ALL_CERTS = Cnf.at("MoverProxy", "trustAllCerts", true);

    static {
        if (TRUST_ALL_CERTS) {
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
    private String[] getDataMover() {
        final var length = httpMoverList.size();
        if (length == 1) {
            return new String[] { httpMoverList.get(0) };
        }
        final var pos = ThreadLocalRandom.current().nextInt(httpMoverList.size());
        final List<String> dataMovers = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            dataMovers.add(httpMoverList.get((pos + i) % length));
        }
        return dataMovers.toArray(new String[dataMovers.size()]);
    }

    /**
     * Instantiates a new REST client.
     *
     * @param httpProxy
     *            the http proxy
     * @param httpMovers
     *            the http mover list
     * @param connectTimeout
     *            the connect timeout
     */
    RESTClient(final String httpProxy, final String httpMovers, final int connectTimeout) {
        final var token = new StringTokenizer(httpMovers, ";,");
        while (token.hasMoreElements()) {
            httpMoverList.add(token.nextToken());
        }
        this.httpProxy = httpProxy;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public String getVersion() throws RestException {
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/getVersion", connectTimeout, "GET",
                null, Map.of())) {
            return parse(response, String.class);
        }
    }

    @Override
    public void del(final DataFile dataFile) throws RestException {
        final var f = new DataFile();
        f.setId(dataFile.getId());
        f.setFileInstance(dataFile.getFileInstance());
        f.setFileSystem(dataFile.getFileSystem());
        f.setArrivedTime(dataFile.getArrivedTime());
        f.setTimeStep(dataFile.getTimeStep());
        f.setOriginal(dataFile.getOriginal());
        _log.debug("REST sending request: del({})", f);
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/del", connectTimeout, "DELETE", f,
                Map.of())) {
            parse(response);
        }
    }

    @Override
    public void close(final DataTransfer dataTransfer) throws RestException {
        final var host = dataTransfer.getHost();
        final var h = new Host();
        h.setName(host.getName());
        h.setECUserName(host.getECUserName());
        h.setTransferMethodName(host.getTransferMethodName());
        final var t = new DataTransfer();
        t.setId(dataTransfer.getId());
        t.setStartCount(dataTransfer.getStartCount());
        t.setHostName(h.getName());
        t.setHost(h);
        _log.debug("REST sending request: close({})", t);
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/close", connectTimeout, "DELETE", t,
                Map.of())) {
            parse(response);
        }
    }

    @Override
    public void purge(final List<ExistingStorageDirectory> directories) throws RestException {
        _log.debug("REST sending request: purge({})",
                directories != null ? directories.size() + " directories" : "no-directory");
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/purge", connectTimeout, "DELETE",
                directories, Map.of())) {
            parse(response);
        }
    }

    @Override
    public String getHostReport(final Host host) throws RestException {
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
        _log.debug("REST sending request: getHostReport({})", h);
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/getHostReport", connectTimeout,
                "PUT", h, Map.of())) {
            return parse(response, String.class);
        }
    }

    @Override
    public String getMoverReport() throws RestException {
        _log.debug("REST sending request: getMoverReport()");
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/getMoverReport", connectTimeout,
                "GET", null, Map.of())) {
            return parse(response, String.class);
        }
    }

    @Override
    public String put(final DataTransfer transfer, final String fileName, final long localPosn, final long remotePosn)
            throws RestException {
        _log.debug("REST sending request: put({},{},{},{})", transfer, fileName, localPosn, remotePosn);
        try (final var response = send(httpProxy, getDataMover()[0] + "/ecpds/mover/put", connectTimeout, "PUT",
                getPutRequest(transfer, fileName, localPosn, remotePosn), Map.of())) {
            return parse(response, String.class);
        }
    }

    @Override
    public ECauthToken getECauthToken(final String user) throws RestException {
        _log.debug("REST sending request: getECauthToken({})", user);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/getECauthToken", connectTimeout, "GET",
                    null, Map.of("user", String.valueOf(user)))) {
                return parse(response, ECauthToken.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public boolean isValidDataFile(final long dataFileId) throws RestException {
        _log.debug("REST sending request: isValidDataFile({})", dataFileId);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/isValidDataFile", connectTimeout,
                    "GET", null, Map.of("dataFileId", String.valueOf(dataFileId)))) {
                return parse(response, boolean.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public long proxyHostIsAlive(final String name) throws RestException {
        _log.debug("REST sending request: proxyHostIsAlive({})", name);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/proxyHostIsAlive", connectTimeout,
                    "PUT", name, Map.of())) {
                return parse(response, long.class);
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public void updateDataRequest(final UpdateDataRequest request) throws RestException {
        _log.debug("REST sending request: updateDataRequest({})", request);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/updateDataRequest", connectTimeout,
                    "PUT", request, Map.of())) {
                parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public void updateData(final Host host) throws RestException {
        _log.debug("REST sending request: updateData({})", host);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/updateData", connectTimeout, "PUT",
                    host, Map.of())) {
                parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public void updateLocation(final Host host) throws RestException {
        _log.debug("REST sending request: updateLocation({})", host);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/updateLocation", connectTimeout, "PUT",
                    host, Map.of())) {
                parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public void updateDataTransfers(final List<DataTransfer> transfers) throws RestException {
        _log.debug("REST sending request: updateDataTransfers({})",
                transfers != null ? transfers.size() + " transfer(s)" : "no-transfer");
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/updateDataTransfers", connectTimeout,
                    "PUT", transfers, Map.of())) {
                parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
        throw restException != null ? restException : new RestException("No MasterServer available");
    }

    @Override
    public void sendMessage(final MonitorRequest request) throws RestException {
        _log.debug("REST sending request: sendMessage({})", request);
        RestException restException = null;
        for (final String dataMover : getDataMover()) {
            try (final var response = send(httpProxy, dataMover + "/ecpds/master/sendMessage", connectTimeout, "PUT",
                    request, Map.of())) {
                parse(response);
                return;
            } catch (final Throwable t) {
                restException = new RestException("Connecting to " + dataMover, t);
            }
        }
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
        final var file = transfer.getDataFile();
        final var host = transfer.getHost();
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
        final var proxy = transfer.getProxyHost();
        final var p = new Host();
        p.setName(proxy.getName());
        p.setNickname(proxy.getNickname());
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
        private static final long serialVersionUID = 4903532344315324986L;
        public DataTransfer transfer;
        public String fileName;
        public Long localPosn;
        public Long remotePosn;

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
        private static final long serialVersionUID = 9165947353246228909L;
        public String name;
        public String service;
        public Integer status;
        public String message;

        @Override
        public String toString() {
            return name + "," + service + "," + status + "," + message;
        }
    }

    /**
     * The Class UpdateDataRequest.
     */
    public static final class UpdateDataRequest implements Serializable {
        private static final long serialVersionUID = 6165347353546221908L;
        public String hostId;
        public String data;

        @Override
        public String toString() {
            return hostId + "," + data;
        }
    }

    private static CloseableClientResponse send(final String proxy, final String url, final int connectTimeout,
            final String method, final Object body, final Map<String, String> query) throws RestException {
        try {
            final var client = newHttpClient(proxy, connectTimeout);
            _log.debug("REST connection: {} (proxy={},connectTimeout={})", url, proxy, connectTimeout);
            final var builder = HttpRequest.newBuilder(buildUri(url, query)).header("Accept", "application/json")
                    .header("Accept-Charset", "iso-8859-1");
            final var timeout = timeout(connectTimeout);
            if (timeout != null) {
                builder.timeout(timeout);
            }
            final HttpRequest request;
            if (body == null) {
                request = builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
            } else {
                request = builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)))
                        .build();
            }
            return new CloseableClientResponse(client.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (final IOException e) {
            throw new RestException("Calling " + url, e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestException("Calling " + url, e);
        }
    }

    private static HttpClient newHttpClient(final String proxy, final int connectTimeout) throws RestException {
        try {
            final var builder = HttpClient.newBuilder();
            final var timeout = timeout(connectTimeout);
            if (timeout != null) {
                builder.connectTimeout(timeout);
            }
            if (proxy != null) {
                final var index = proxy.indexOf(':');
                if (index != -1) {
                    builder.proxy(ProxySelector.of(new InetSocketAddress(proxy.substring(0, index),
                            Integer.parseInt(proxy.substring(index + 1)))));
                }
            }
            if (TRUST_ALL_CERTS) {
                final var sslContext = newTrustAllSslContext();
                // Set the SSL context first so the HttpClient picks it up, then override
                // the SSLParameters to disable endpoint identification (hostname/SAN checks).
                // The empty string disables the default "HTTPS" algorithm; must be applied
                // AFTER sslContext() so it is not reset by the context's default parameters.
                builder.sslContext(sslContext);
                final var parameters = sslContext.getDefaultSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("");
                builder.sslParameters(parameters);
            }
            return builder.build();
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new RestException("Creating HTTP client", e);
        }
    }

    private static Duration timeout(final int connectTimeout) {
        return connectTimeout > 0 ? Duration.ofMillis(connectTimeout) : null;
    }

    private static URI buildUri(final String url, final Map<String, String> query) {
        if (query == null || query.isEmpty()) {
            return URI.create(url);
        }
        final var sb = new StringBuilder(url);
        sb.append(url.contains("?") ? '&' : '?');
        var first = true;
        for (final Map.Entry<String, String> entry : query.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return URI.create(sb.toString());
    }

    private static SSLContext newTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        final var trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } };
        final var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

    private static <T> T parse(final CloseableClientResponse response, final Class<T> valueType) throws RestException {
        return parse(response.getEntity(String.class), valueType, null);
    }

    private static void parse(final CloseableClientResponse response) throws RestException {
        parse(response.getEntity(String.class), null, null);
    }

    private static <T> T parse(final String message, final Class<T> valueType, final TypeReference<T> valueTypeRef)
            throws RestException {
        if (debug) {
            _log.debug("Parsing message: {}", message);
        }
        if (message == null) {
            throw new RestException("Service not found");
        }
        try {
            final JsonNode root = OBJECT_MAPPER.readTree(message);
            if (root == null || !root.isObject()) {
                throw new RestException("Parsing JSON message: " + message);
            }
            final var success = root.path("success").asText(null);
            if (success == null) {
                throw new RestException("Parsing JSON message: " + message);
            }
            if (!"yes".equals(success)) {
                throw new RestException(root.path("error").asText("Unknown error"));
            }
            if (valueType == null && valueTypeRef == null) {
                return null;
            }
            final var fields = root.fields();
            while (fields.hasNext()) {
                final var field = fields.next();
                final var fieldName = field.getKey();
                if ("success".equals(fieldName) || "error".equals(fieldName)) {
                    continue;
                }
                final var value = field.getValue();
                if (debug) {
                    _log.debug("Parsing {}={}", fieldName, value);
                }
                final T result = valueTypeRef != null ? OBJECT_MAPPER.convertValue(value, valueTypeRef)
                        : OBJECT_MAPPER.convertValue(value, valueType);
                _log.debug("Result: {}", result);
                return result;
            }
            return null;
        } catch (final RestException e) {
            throw e;
        } catch (final Throwable t) {
            throw new RestException("Parsing JSON message: " + message, t);
        }
    }

    /**
     * A wrapper around {@link HttpResponse} that implements {@link Closeable}.
     */
    public static class CloseableClientResponse implements Closeable {
        private final HttpResponse<String> response;

        public CloseableClientResponse(final HttpResponse<String> response) {
            this.response = response;
        }

        public int getStatusCode() {
            return response.statusCode();
        }

        public String getMessage() {
            return String.valueOf(response.statusCode());
        }

        public <T> T getEntity(final Class<T> t) throws RestException {
            if (t == String.class) {
                return t.cast(response.body());
            }
            try {
                return OBJECT_MAPPER.readValue(response.body(), t);
            } catch (final IOException e) {
                throw new RestException("Reading response entity", e);
            }
        }

        @Override
        public void close() {
        }
    }

    /**
     * Replacement for the legacy RestException.
     */
    public static class RestException extends Exception {
        private static final long serialVersionUID = 1L;

        public RestException(final String message) {
            super(message);
        }

        public RestException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
