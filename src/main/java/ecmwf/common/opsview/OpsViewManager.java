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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.RestClient;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import ecmwf.common.database.Destination;
import ecmwf.common.security.SSLSocketFactory;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * The Class OpsViewManager.
 */
public final class OpsViewManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(OpsViewManager.class);

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
    private static RestClient getRestClient() {
        final var clientConfig = new ClientConfig().applications(new ECaccessApplication());
        clientConfig.setBypassHostnameVerification(true);
        clientConfig.setProperties(new Options(OPTIONS).getProperties());
        return new RestClient(clientConfig);
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
     * @throws JSONException
     *             the JSON exception
     */
    private static synchronized String getToken(final boolean lastTry) throws IOException, JSONException {
        if (lastTry || _token.length() == 0) { // Last try or initialization
            _log.debug("Getting new Token from Opsview");
            if (USER.length() == 0 || PASSWORD.length() == 0) {
                throw new IOException("Please check Opsview credentials");
            }
            // Authenticate and get a new Token
            final var auth = new JSONObject();
            auth.put("username", USER);
            auth.put("password", PASSWORD);
            final var json = getRestClient().resource(URL_LOGIN).contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).post(JSONObject.class, auth);
            final var token = String.valueOf(json.get("token"));
            if ("null".equals(token)) {
                throw new IOException("Authentication failed");
            }
            _token.setLength(0);
            _token.append(token);
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
     * Sync.
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
     * @throws ecmwf.common.opsview.OpsViewManagerException
     *             the ops view manager exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws org.apache.wink.json4j.JSONException
     *             the JSON exception
     */
    public static void detail(final String hostname, final String service, final int status, final String message)
            throws OpsViewManagerException, IOException, JSONException {
        final var lastTry = getLastTry();
        do {
            try {
                // Now we send the message!
                final var response = getRestClient().resource(URL_DETAIL).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Opsview-Username", USER).header("X-Opsview-Token", getToken(lastTry.get()))
                        .accept(MediaType.APPLICATION_JSON).queryParam("hostname", hostname)
                        .queryParam("servicename", service)
                        .post("{\"set_state\": { \"result\": " + status + ",\"output\": \"" + message + "\"}}");
                final var code = response.getStatusCode();
                if (code != 200) {
                    _log.warn("Code: {}, Message: {}", code, response.getMessage());
                    throw new IOException("Submit failed: " + response.getMessage());
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
     * @throws ecmwf.common.opsview.OpsViewManagerException
     *             the ops view manager exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws org.apache.wink.json4j.JSONException
     *             the JSON exception
     */
    public static void sync(final Destination[] destinations)
            throws OpsViewManagerException, IOException, JSONException {
        // We have to build a list for each filter!
        final var cacheList = new HashMap<String, ArrayList<String>>();
        for (final String filterName : filtersList) {
            if (!"".equals(filterName)) { // If empty then we discard it!
                cacheList.put(filterName, new ArrayList<>());
            }
        }
        // Let's fill the list with the destinations!
        for (final Destination destination : destinations) {
            final var type = destination.getType();
            final var destinationsList = cacheList.get(getFilter(type));
            if (destinationsList != null) {
                destinationsList.add(destination.getName());
            }
        }
        // We have to sort the list to allow comparing with what we have in cache
        for (final ArrayList<String> list : cacheList.values()) {
            Collections.sort(list);
        }
        // Do we have to trigger a reload ? In case the list is the same as the previous
        // list then there is nothing to do.
        for (final String filterName : filtersList) {
            if ("".equals(filterName)) { // If empty then we discard it!
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
            // The sync was successful so we store it
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
     * @throws ecmwf.common.opsview.OpsViewManagerException
     *             the ops view manager exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws org.apache.wink.json4j.JSONException
     *             the JSON exception
     */
    public static void clearNotes(final String destination) throws OpsViewManagerException, IOException, JSONException {
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
     * @throws ecmwf.common.opsview.OpsViewManagerException
     *             the ops view manager exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws org.apache.wink.json4j.JSONException
     *             the JSON exception
     */
    public static void addNotes(final String destination, final String metadata)
            throws OpsViewManagerException, IOException, JSONException {
        final var lastTry = getLastTry();
        do {
            try {
                // Get the token and REST client
                final var token = getToken(lastTry.get());
                final var client = getRestClient();
                final var clear = metadata == null || metadata.isBlank();
                _log.debug("{}ing notes for {}", clear ? "Clear" : "Add", destination);
                // Build the message
                final var notes = new JSONObject();
                notes.put("note", clear ? "" : metadata);
                // Submit on the server
                final var response = client.resource(URL_NOTES + "/" + getDestinationName(destination))
                        .contentType(MediaType.APPLICATION_JSON).header("X-Opsview-Username", USER)
                        .header("X-Opsview-Token", token).accept(MediaType.APPLICATION_JSON).put(notes);
                if (response.getStatusCode() != 200) {
                    throw new IOException("Submit failed: " + response.getMessage());
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
     * @throws JSONException
     *             the JSON exception
     */
    private static void sync(final String filterName, final String[] destinations)
            throws OpsViewManagerException, IOException, JSONException {
        final var lastTry = getLastTry();
        do {
            try {
                // Get the token and REST client
                final var token = getToken(lastTry.get());
                final var client = getRestClient();
                _log.debug("Synchronization started for {}", filterName);
                // Get the list of Destinations already on opsview
                final var filter = URLEncoder.encode("{\"name\":\"" + filterName + "\"}",
                        Charset.defaultCharset().displayName());
                final var json = client.resource(URL_HOST).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Opsview-Username", USER).header("X-Opsview-Token", token)
                        .accept(MediaType.APPLICATION_JSON).queryParam("json_filter", filter).get(JSONObject.class);
                final var list = json.getJSONArray("list");
                JSONArray hostattributes = null;
                // Build the list of Destinations from the request
                final List<String> fromClient = new ArrayList<>(Arrays.asList(destinations));
                // Build the list of Destinations from Opsview
                final List<String> fromServer = new ArrayList<>();
                for (var i = 0; list != null && i < list.length(); i++) {
                    final var host = list.getJSONObject(i);
                    if (host != null && filterName.equals(host.get("name"))) {
                        hostattributes = host.getJSONArray("hostattributes");
                        for (var j = 0; j < hostattributes.length(); j++) {
                            final var d = hostattributes.getJSONObject(j);
                            if (d != null) {
                                fromServer.add(String.valueOf(d.get("value")));
                            }
                        }
                    }
                }
                // Check if the host attributes have been found?
                if (hostattributes == null) {
                    throw new IOException("No hostattributes found");
                }
                // Check if there is a difference?
                if (fromClient.size() == fromServer.size()) {
                    // Same size, so are they the same elements?
                    fromClient.removeAll(fromServer);
                    if (fromClient.isEmpty()) {
                        // Nothing to do!
                        return;
                    }
                }
                // Let's set the list of Destinations!
                hostattributes.clear();
                for (final String destination : destinations) {
                    final var newdes = new JSONObject();
                    newdes.put("name", "DESTINATION");
                    newdes.put("value", getDestinationName(destination));
                    hostattributes.add(newdes);
                }
                // Submit on the server
                var response = client.resource(URL_HOST).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Opsview-Username", USER).header("X-Opsview-Token", token)
                        .accept(MediaType.APPLICATION_JSON).put(json);
                if (response.getStatusCode() != 200) {
                    if (_log.isWarnEnabled()) {
                        _log.warn("Message: {}", json.toString(2));
                    }
                    throw new IOException("Submit failed: " + response.getMessage());
                }
                // Reload the configuration
                response = client.resource(URL_RELOAD).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Opsview-Username", USER).header("X-Opsview-Token", token).post(null);
                if (response.getStatusCode() != 200) {
                    throw new IOException("Reload failed: " + response.getMessage());
                }
                break;
            } catch (final IOException e) {
                if (!lastTry.compareAndSet(false, true)) {
                    throw e;
                }
            }
        } while (true);
    }
}
