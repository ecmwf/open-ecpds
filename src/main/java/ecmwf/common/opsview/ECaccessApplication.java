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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

/**
 * The Class ECaccessApplication.
 */
public final class ECaccessApplication extends Application {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECaccessApplication.class);

    /**
     * {@inheritDoc}
     *
     * Gets the classes.
     */
    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the singletons.
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
     * The Class JacksonProvider. Allow catching the parsing errors and send an appropriate exception to the container.
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
        private static String _getThrowableMessage(Throwable t) {
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
                throw new WebApplicationException(Response.status(Status.PRECONDITION_FAILED).entity(message).build());
            } catch (final Throwable t) {
                _log.debug("Parsing error", t);
                throw new WebApplicationException(
                        Response.status(Status.PRECONDITION_FAILED).entity(_getThrowableMessage(t)).build());
            }
        }
    }
}
