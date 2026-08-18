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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.AllocateInterface;
import ecmwf.common.technical.ScriptManager;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The Class RESTAllocate. Allow connecting to a REST service in order to
 * allocate a host and a directory to dispatch a file. If the request is
 * successful it return the result in the format "host:dir". Once the file has
 * been transmitted to the requested place a commit must be sent to acknowledge
 * the success of the transmission.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

/**
 * The Class RESTAllocate.
 */
public final class RESTAllocate implements AllocateInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTAllocate.class);

    /** Shared JSON mapper. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** The client. */
    private final HttpClient client;

    /** The json. */
    private final JsonNode json;

    /**
     * Instantiates a new allocate manager. At this time the request is sent to the remote site and the outcome is
     * stored in the json object.
     *
     * @param url
     *            the url
     * @param properties
     *            the properties
     *
     * @throws IOException
     *             the IO exception
     */
    public RESTAllocate(final String url, final Properties properties) throws IOException {
        _log.debug("Sending Allocate: {}", url);
        client = newHttpClient(properties);
        try {
            final var request = newRequestBuilder(url, properties).header("Accept", MediaType.WILDCARD).GET().build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response, url);
            json = OBJECT_MAPPER.readTree(response.body());
        } catch (final IOException e) {
            throw e;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Allocate request interrupted", e);
        }
    }

    /**
     * Execute the javascript command against the json object (e.g. json.pathspecs[0]) and expect the outcome to be a
     * String.
     *
     * @param command
     *            the command
     *
     * @return the string
     *
     * @throws ScriptException
     *             the script exception
     */
    @Override
    public String get(final String command) throws ScriptException {
        try {
            final var jsonMap = OBJECT_MAPPER.convertValue(json, new TypeReference<Map<String, Object>>() {
            });
            final var bindings = new HashMap<String, Object>();
            bindings.put("json", jsonMap);
            return ScriptManager.exec(String.class, ScriptManager.JS, bindings, command);
        } catch (final Exception e) {
            final var scriptException = new ScriptException("Failed to execute JavaScript against the json object");
            scriptException.initCause(e);
            throw scriptException;
        }
    }

    /**
     * Commit. Send the commit request using the url specified. The original json object is sent within the request.
     *
     * @param url
     *            the url
     *
     * @return the int
     */
    @Override
    public int commit(final String url) {
        try {
            final var request = newRequestBuilder(url, null).header("Accept", MediaType.WILDCARD)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(json))).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (final IOException e) {
            throw new IllegalStateException("Commit allocate request failed", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Commit allocate request interrupted", e);
        }
    }

    private static HttpClient newHttpClient(final Properties properties) throws IOException {
        try {
            final var builder = HttpClient.newBuilder();
            final var connectTimeout = getConnectTimeout(properties);
            if (connectTimeout != null && !connectTimeout.isNegative() && !connectTimeout.isZero()) {
                builder.connectTimeout(connectTimeout);
            }
            final var proxyHost = getProperty(properties, "proxyHost");
            final var proxyPort = getProperty(properties, "proxyPort");
            if (proxyHost != null && proxyPort != null) {
                builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
            }
            if (Boolean.parseBoolean(getProperty(properties, "bypassHostnameVerification"))) {
                final var parameters = new SSLParameters();
                parameters.setEndpointIdentificationAlgorithm("");
                builder.sslParameters(parameters).sslContext(newTrustAllSslContext());
            }
            return builder.build();
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Cannot initialize HTTP client", e);
        }
    }

    private static HttpRequest.Builder newRequestBuilder(final String url, final Properties properties) {
        final var builder = HttpRequest.newBuilder(URI.create(url));
        final var connectTimeout = getConnectTimeout(properties);
        if (connectTimeout != null && !connectTimeout.isNegative() && !connectTimeout.isZero()) {
            builder.timeout(connectTimeout);
        }
        return builder;
    }

    private static Duration getConnectTimeout(final Properties properties) {
        final var value = getProperty(properties, "connectTimeout");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Duration.ofMillis(Long.parseLong(value));
        } catch (final NumberFormatException e) {
            _log.debug("Ignoring invalid connectTimeout={}", value);
            return null;
        }
    }

    private static String getProperty(final Properties properties, final String key) {
        return properties == null ? null : properties.getProperty(key);
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

    private static void ensureSuccess(final HttpResponse<String> response, final String url) throws IOException {
        if (response.statusCode() / 100 != 2) {
            throw new IOException(response.statusCode() + " " + url + " - " + response.body());
        }
    }

    /**
     * The Class AllocateApplication. Utility class for the REST/json interface.
     */
    public static final class AllocateApplication extends Application {
        /** The Constant _log. */
        private static final Logger _log = LogManager.getLogger(AllocateApplication.class);

        /**
         * Gets the classes.
         *
         * @return the classes
         */
        @Override
        public Set<Class<?>> getClasses() {
            return new HashSet<>();
        }

        /**
         * Gets the singletons.
         *
         * @return the singletons
         */
        @Override
        public Set<Object> getSingletons() {
            final Set<Object> s = new HashSet<>();
            final var mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            final var jaxbProvider = new JacksonProvider(mapper);
            s.add(jaxbProvider);
            return s;
        }

        /**
         * The Class JacksonProvider. Allow catching the parsing errors and send an appropriate exception to the
         * container.
         */
        @jakarta.ws.rs.ext.Provider
        public static final class JacksonProvider extends JacksonJsonProvider {
            public JacksonProvider() {
                super();
            }

            public JacksonProvider(final ObjectMapper mapper) {
                super(mapper);
            }

            /**
             * Gets the throwable message.
             *
             * @param t
             *            the t
             *
             * @return the string
             */
            private static String getThrowableMessage(Throwable t) {
                String message = null;
                while (t != null && (message = t.getMessage()) == null && t.getCause() != null) {
                    t = t.getCause();
                }
                return message == null ? "Server Error" : message;
            }

            /**
             * Read from.
             *
             * @param type
             *            the type
             * @param genericType
             *            the generic type
             * @param annotations
             *            the annotations
             * @param mediaType
             *            the media type
             * @param httpHeaders
             *            the http headers
             * @param entityStream
             *            the entity stream
             *
             * @return the object
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations,
                    final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                    final InputStream entityStream) throws IOException {
                try {
                    return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
                } catch (final UnrecognizedPropertyException e) {
                    _log.debug("Unknown field", e);
                    var message = e.getMessage();
                    final var index = message.indexOf(" (through reference chain:");
                    if (index != -1) {
                        message = message.substring(0, index);
                    }
                    throw new WebApplicationException(
                            Response.status(Status.PRECONDITION_FAILED).entity(message).build());
                } catch (final Throwable t) {
                    _log.debug("Parsing error", t);
                    throw new WebApplicationException(
                            Response.status(Status.PRECONDITION_FAILED).entity(getThrowableMessage(t)).build());
                }
            }
        }
    }
}
