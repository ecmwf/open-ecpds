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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.script.ScriptException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.RestClient;
import org.apache.wink.json4j.JSONObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.codehaus.jackson.type.TypeReference;

import ecmwf.common.ectrans.AllocateInterface;
import ecmwf.common.technical.ScriptManager;

/**
 * The Class RESTAllocate.
 */
public final class RESTAllocate implements AllocateInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTAllocate.class);

    /** The clientConfig. */
    private final ClientConfig clientConfig;

    /** The json. */
    private final JSONObject json;

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
        clientConfig = new ClientConfig().applications(new AllocateApplication());
        clientConfig.setBypassHostnameVerification(true);
        if (properties != null && !properties.isEmpty()) {
            clientConfig.setProperties(properties);
            if (_log.isDebugEnabled()) {
                for (final String key : properties.stringPropertyNames()) {
                    _log.debug("Using property {}={}", key, properties.getProperty(key));
                }
            }
        }
        final var response = new RestClient(clientConfig).resource(url).accept(MediaType.WILDCARD).get();
        try {
            json = response.getEntity(JSONObject.class);
        } catch (final Exception _) {
            throw new IOException(
                    response.getStatusCode() + " " + response.getMessage() + " - " + response.getEntity(String.class));
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
            return ScriptManager.exec(String.class, ScriptManager.JS, new HashMap<>(Map.of("json",
                    new ObjectMapper().readValue(json.toString(), new TypeReference<Map<String, Object>>() {
                    }))), command);
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
        return new RestClient(clientConfig).resource(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.WILDCARD).post(json).getStatusCode();
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
        @SuppressWarnings("deprecation")
        @Override
        public Set<Object> getSingletons() {
            final Set<Object> s = new HashSet<>();
            final var jaxbProvider = new JacksonProvider();
            final var mapper = new ObjectMapper();
            mapper.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
            jaxbProvider.setMapper(mapper);
            s.add(jaxbProvider);
            return s;
        }

        /**
         * The Class JacksonProvider. Allow catching the parsing errors and send an appropriate exception to the
         * container.
         */
        @javax.ws.rs.ext.Provider
        public static final class JacksonProvider extends JacksonJaxbJsonProvider {
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
                    final var index = message.indexOf(" (Class ");
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
