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

package ecmwf.common.opsview;

import java.io.Closeable;
import java.io.IOException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Destination;
import ecmwf.common.security.SSLSocketFactory;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

/**
 * The Class OpsViewManager.
 */
public final class OpsViewManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(OpsViewManager.class);

    /** Shared JSON mapper. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** The Constant ACTIVATED. */
    private static final boolean ACTIVATED = Cnf.at("OpsViewManager", "activated", true);

    /** The Constant OPTIONS. */
    private static final String OPTIONS = Cnf.at("OpsViewManager", "options", "");

    /** The Constant URL:. */
    private static final String URL = Cnf.at("OpsViewManager", "url", "https://localhost/passive:1443");

    /** The Constant URL_LOGIN. */
    private static final String URL_LOGIN = Cnf.at("OpsViewManager", "urlLogin", URL + "/rest/login");

    /** The Constant URL_CONFIG. */
    private static final String URL_HOST = Cnf.at("OpsViewManager", "urlHost", URL + "/rest/config/host");

    /** The Constant URL_NOTES. */
    private static final String URL_NOTES = Cnf.at("OpsViewManager", "urlNotes", URL + "/rest/notes/host");

    /** The Constant URL_RELOAD. */
    private static final String URL_RELOAD = Cnf.at("OpsViewManager", "urlReload", URL + "/rest/reload");

    /** The Constant URL_RELOAD. */
    private static final String URL_DETAIL = Cnf.at("OpsViewManager", "urlDetail", URL + "/rest/detail");

    /** The Constant USER. */
    private static final String USER = Cnf.at("OpsViewManager", "user", "");

    /** The Constant PASSWORD. */
    private static final String PASSWORD = Cnf.at("OpsViewManager", "password", "");

    /** The Constant DISSEMINATION_FILTER_NAME. */
    public static final String DISSEMINATION_FILTER_NAME = Cnf.at("OpsViewManager", "disseminationFilterName",
            "ECPDS_Dissemination");

    /** The Constant ACQUISITION_FILTER_NAME. */
    public static final String ACQUISITION_FILTER_NAME = Cnf.at("OpsViewManager", "acquisitionFilterName",
            "ECPDS_Acquisition");

    /** The Constant OTHER_FILTER_NAME. */
    public static final String OTHER_FILTER_NAME = Cnf.at("OpsViewManager", "otherFilterName", "ECPDS_Other");

    /** The Constant REST_CLIENT. */
    public static final HttpClient REST_CLIENT = getRestClient();

    /** The Constant filtersList. */
    protected static final String[] filtersList = { ACQUISITION_FILTER_NAME, DISSEMINATION_FILTER_NAME,
            OTHER_FILTER_NAME };

    /**
     * Keep in cache the list of Destinations to avoid sending the same list twice!
     */
    private static final HashMap<String, ArrayList<String>> _cacheList = new HashMap<>();

    /** Keep a token in the cache and renew it only if required! *. */
    private static final StringBuilder _token = new StringBuilder();

    static {
        // If requested make sure the REST client can connect to URL with
        // unknown certificates!
        if (ACTIVATED) {
            _log.debug("OpsView manager activated");
            if (Cnf.at("OpsViewManager", "trustAllCerts", true)) {
                _log.debug("Activating trusted certificates for all https connections");
                try {
                    SSLSocketFactory.setHttpsURLConnectionTrustAllCerts();
                } catch (final Throwable t) {
                    _log.warn("Trusting all https certificates", t);
                }
            }
        }
    }

    /**
     * Instantiates a new ops view manager.
     */
    private OpsViewManager() {
        // Hide the private constructor!
    }

    /**
     * Gets the destination name.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the string
     */
    private static String getDestinationName(final String destinationName) {
        // Replace all ':' characters as they are not allowed in the name !
        return Format.cleanTextContent(destinationName).replace(":", "_");
    }

    /**
     * Gets the last try. For every call to detail or sync provides a boolean for the retry mechanism!
     *
     * @return the last try
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     */
    private static AtomicBoolean getLastTry() throws OpsViewManagerException {
        if (!ACTIVATED) { // Check if is activated and throw an exception otherwise!
            throw new OpsViewManagerException("OpsViewManager not activated");
        }
        return new AtomicBoolean(false);
    }

    /**
     * Gets the configured rest client.
     *
     * @return the rest client
     */
    private static HttpClient getRestClient() {
        final var builder = HttpClient.newBuilder();
        final var properties = new Options(OPTIONS).getProperties();
        final var connectTimeout = properties.getProperty("connectTimeout");
        if (connectTimeout != null && !connectTimeout.isBlank()) {
            try {
                builder.connectTimeout(Duration.ofMillis(Long.parseLong(connectTimeout)));
            } catch (final NumberFormatException e) {
                _log.debug("Ignoring invalid connectTimeout={}", connectTimeout);
            }
        }
        if (Cnf.at("OpsViewManager", "trustAllCerts", true)) {
            try {
                final var parameters = new SSLParameters();
                parameters.setEndpointIdentificationAlgorithm("");
                builder.sslParameters(parameters).sslContext(newTrustAllSslContext());
            } catch (final NoSuchAlgorithmException | KeyManagementException e) {
                _log.warn("Cannot enable trustAllCerts for OpsView HttpClient", e);
            }
        }
        return builder.build();
    }

    /**
     * Gets the token. If the token is not valid then create a new one!
     *
     * @param lastTry
     *            the last try
     *
     * @return the token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static synchronized String getToken(final boolean lastTry) throws IOException {
        if (lastTry || _token.isEmpty()) { // Last try or initialization
            _log.debug("Getting new Token from Opsview");
            if (USER.isEmpty() || PASSWORD.isEmpty()) {
                throw new IOException("Please check Opsview credentials");
            }
            final var auth = OBJECT_MAPPER.createObjectNode();
            auth.put("username", USER);
            auth.put("password", PASSWORD);
            try (final var response = send(URL_LOGIN, "POST", Map.of(), Map.of(), auth)) {
                final var code = response.getStatusCode();
                if (code != 200) {
                    _log.warn("URL: {}, Code: {}, Message: {}", URL_LOGIN, code, response.getMessage());
                    throw new IOException("Login request failed");
                }
                final var token = response.getEntity(JsonNode.class).path("token").asText(null);
                if (token == null || "null".equals(token)) {
                    throw new IOException("Authentication failed");
                }
                _token.setLength(0);
                _token.append(token);
            }
        }
        return _token.toString();
    }

    /**
     * Checks if is activated.
     *
     * @return true, if is activated
     */
    public static boolean isActivated() {
        return ACTIVATED;
    }

    /**
     * Detail.
     *
     * @param hostname
     *            the host name
     * @param service
     *            the service
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void detail(final String hostname, final String service, final int status, final String message)
            throws OpsViewManagerException, IOException {
        final var request = "{\"set_state\": { \"result\": " + status + ",\"output\": \"" + message + "\"}}";
        final var lastTry = getLastTry();
        do {
            try (final var response = send(URL_DETAIL, "POST",
                    Map.of("X-Opsview-Username", USER, "X-Opsview-Token", getToken(lastTry.get())),
                    Map.of("hostname", hostname, "servicename", service), request)) {
                final var code = response.getStatusCode();
                if (code != 200) {
                    _log.warn("URL: {}, Code: {}, Message: {}, Request: {}", URL_DETAIL, code, response.getMessage(),
                            request);
                    throw new IOException("Detail request failed");
                }
                break;
            } catch (final IOException e) {
                if (!lastTry.compareAndSet(false, true)) {
                    throw e;
                }
            }
        } while (true);
    }

    /**
     * Gets the filter.
     *
     * @param type
     *            the type
     *
     * @return the filter
     */
    private static String getFilter(final int type) {
        if (DestinationOption.isAcquisition(type)) {
            return ACQUISITION_FILTER_NAME;
        }
        return DestinationOption.isDissemination(type) ? DISSEMINATION_FILTER_NAME : OTHER_FILTER_NAME;
    }

    /**
     * Sync.
     *
     * @param destinations
     *            the destinations
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void sync(final Destination[] destinations) throws OpsViewManagerException, IOException {
        final var cacheList = new HashMap<String, ArrayList<String>>();
        for (final String filterName : filtersList) {
            if (!"".equals(filterName)) {
                cacheList.put(filterName, new ArrayList<>());
            }
        }
        for (final Destination destination : destinations) {
            final var type = destination.getType();
            final var destinationsList = cacheList.get(getFilter(type));
            if (destinationsList != null) {
                destinationsList.add(destination.getName());
            }
        }
        for (final ArrayList<String> list : cacheList.values()) {
            Collections.sort(list);
        }
        for (final String filterName : filtersList) {
            if ("".equals(filterName)) {
                continue;
            }
            final var existingList = _cacheList.get(filterName);
            final var currentList = cacheList.get(filterName);
            if (currentList.equals(existingList)) {
                _log.debug("No changes detected for {}, reload delayed", filterName);
                continue;
            }
            _log.debug("Changes detected for {}", filterName);
            sync(filterName, currentList.toArray(new String[currentList.size()]));
            if (existingList == null) {
                _cacheList.put(filterName, currentList);
            } else {
                existingList.clear();
                existingList.addAll(currentList);
            }
        }
    }

    /**
     * Clear notes.
     *
     * @param destination
     *            the destination
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void clearNotes(final String destination) throws OpsViewManagerException, IOException {
        addNotes(destination, null);
    }

    /**
     * Adds the notes.
     *
     * @param destination
     *            the destination
     * @param metadata
     *            the metadata
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void addNotes(final String destination, final String metadata)
            throws OpsViewManagerException, IOException {
        final var lastTry = getLastTry();
        do {
            try {
                final var token = getToken(lastTry.get());
                final var clear = metadata == null || metadata.isBlank();
                _log.debug("{}ing notes for {}", clear ? "Clear" : "Add", destination);
                final var notes = OBJECT_MAPPER.createObjectNode();
                notes.put("note", clear ? "" : metadata);
                final var url = URL_NOTES + "/" + getDestinationName(destination);
                try (final var response = send(url, "PUT", Map.of("X-Opsview-Username", USER, "X-Opsview-Token", token),
                        Map.of(), notes)) {
                    final var code = response.getStatusCode();
                    if (code != 200) {
                        _log.warn("URL: {}, Code: {}, Message: {}", url, code, response.getMessage());
                        throw new IOException("Notes request failed");
                    }
                }
                break;
            } catch (final IOException e) {
                if (!lastTry.compareAndSet(false, true)) {
                    throw e;
                }
            }
        } while (true);
    }

    /**
     * Sync the destinations.
     *
     * @param filterName
     *            the filter name
     * @param destinations
     *            the destinations
     *
     * @throws OpsViewManagerException
     *             the ops view manager exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void sync(final String filterName, final String[] destinations)
            throws OpsViewManagerException, IOException {
        final var lastTry = getLastTry();
        do {
            try {
                final var token = getToken(lastTry.get());
                _log.debug("Synchronization started for {}", filterName);
                final var filter = "{\"name\":\"" + filterName + "\"}";
                final ObjectNode json;
                try (final var response = send(URL_HOST, "GET",
                        Map.of("X-Opsview-Username", USER, "X-Opsview-Token", token), Map.of("json_filter", filter),
                        null)) {
                    final var code = response.getStatusCode();
                    if (code != 200) {
                        _log.warn("URL: {}, Code: {}, Message: {}, Request: {}", URL_HOST, code, response.getMessage(),
                                filter);
                        throw new IOException("Host request failed");
                    }
                    json = (ObjectNode) response.getEntity(JsonNode.class);
                }
                final var list = (ArrayNode) json.path("list");
                ArrayNode hostattributes = null;
                final List<String> fromClient = new ArrayList<>(Arrays.asList(destinations));
                final List<String> fromServer = new ArrayList<>();
                for (final JsonNode host : list) {
                    if (host != null && filterName.equals(host.path("name").asText())) {
                        hostattributes = (ArrayNode) host.path("hostattributes");
                        for (final JsonNode d : hostattributes) {
                            if (d != null) {
                                fromServer.add(d.path("value").asText());
                            }
                        }
                    }
                }
                if (hostattributes == null) {
                    throw new IOException("No hostattributes found");
                }
                if (fromClient.size() == fromServer.size()) {
                    fromClient.removeAll(fromServer);
                    if (fromClient.isEmpty()) {
                        return;
                    }
                }
                hostattributes.removeAll();
                for (final String destination : destinations) {
                    final var newdes = OBJECT_MAPPER.createObjectNode();
                    newdes.put("name", "DESTINATION");
                    newdes.put("value", getDestinationName(destination));
                    hostattributes.add(newdes);
                }
                try (final var response = send(URL_HOST, "PUT",
                        Map.of("X-Opsview-Username", USER, "X-Opsview-Token", token), Map.of(), json)) {
                    final var code = response.getStatusCode();
                    if (code != 200) {
                        _log.warn("URL: {}, Code: {}, Message: {}, Request: {}", URL_HOST, code, response.getMessage(),
                                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                        throw new IOException("Host request failed");
                    }
                }
                try (final var response = send(URL_RELOAD, "POST",
                        Map.of("X-Opsview-Username", USER, "X-Opsview-Token", token), Map.of(), null)) {
                    final var code = response.getStatusCode();
                    if (code != 200) {
                        _log.warn("URL: {}, Code: {}, Message: {}", URL_RELOAD, code, response.getMessage());
                        throw new IOException("Reload request failed");
                    }
                }
                break;
            } catch (final IOException e) {
                if (!lastTry.compareAndSet(false, true)) {
                    throw e;
                }
            }
        } while (true);
    }

    private static CloseableClientResponse send(final String url, final String method,
            final Map<String, String> headers, final Map<String, String> query, final Object body) throws IOException {
        try {
            final var builder = HttpRequest.newBuilder(buildUri(url, query)).timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json");
            headers.forEach(builder::header);
            if (body != null) {
                builder.header("Content-Type", "application/json");
            }
            final HttpRequest request;
            if (body == null) {
                request = builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
            } else if (body instanceof String s) {
                request = builder.method(method, HttpRequest.BodyPublishers.ofString(s)).build();
            } else {
                request = builder
                        .method(method, HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)))
                        .build();
            }
            return new CloseableClientResponse(REST_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OpsView request interrupted", e);
        }
    }

    private static URI buildUri(final String url, final Map<String, String> query) {
        if (query.isEmpty()) {
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

    /**
     * A wrapper around {@link HttpResponse} that implements {@link Closeable}.
     */
    public static class CloseableClientResponse implements Closeable {
        /** The underlying response being wrapped. */
        private final HttpResponse<String> response;

        /**
         * Constructs a new CloseableClientResponse wrapping the given response.
         *
         * @param response
         *            the response to wrap; must not be null
         */
        public CloseableClientResponse(final HttpResponse<String> response) {
            this.response = response;
        }

        /**
         * Returns the HTTP status code of the response.
         *
         * @return the HTTP status code
         */
        public int getStatusCode() {
            return response.statusCode();
        }

        /**
         * Returns the HTTP status message of the response.
         *
         * @return the status message
         */
        public String getMessage() {
            return String.valueOf(response.statusCode());
        }

        /**
         * Reads and returns the entity from the response.
         *
         * @param <T>
         *            the type of the entity
         * @param t
         *            the class of the entity
         *
         * @return the entity deserialized as the given class
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public <T> T getEntity(final Class<T> t) throws IOException {
            return OBJECT_MAPPER.readValue(response.body(), t);
        }

        /**
         * Closes the response.
         */
        @Override
        public void close() {
        }
    }
}
