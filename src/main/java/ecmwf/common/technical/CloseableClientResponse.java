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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 7.2.0
 *
 * @since 2025-08-29
 */
import java.io.Closeable;
import java.io.IOException;

import org.apache.wink.client.ClientResponse;

/**
 * A wrapper around {@link ClientResponse} that implements {@link Closeable}.
 * <p>
 * This allows the use of try-with-resources to automatically release underlying HTTP/SSL connections by calling
 * {@link ClientResponse#consumeContent()} when done.
 * </p>
 */
public class CloseableClientResponse implements Closeable {

    /** The underlying Wink ClientResponse being wrapped. */
    private final ClientResponse response;

    /**
     * Constructs a new CloseableClientResponse wrapping the given ClientResponse.
     *
     * @param response
     *            the ClientResponse to wrap; must not be null
     */
    public CloseableClientResponse(final ClientResponse response) {
        this.response = response;
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return response.getStatusCode();
    }

    /**
     * Returns the HTTP status message of the response.
     *
     * @return the status message
     */
    public String getMessage() {
        return response.getMessage();
    }

    /**
     * Reads and returns the entity from the response.
     * <p>
     * Note that calling this method will fully read the entity into memory.
     * </p>
     *
     * @param <T>
     *            the type of the entity
     * @param t
     *            the class of the entity
     *
     * @return the entity deserialized as the given class
     */
    public <T> T getEntity(final Class<T> t) {
        return response.getEntity(t);
    }

    /**
     * Closes the response by consuming its content.
     * <p>
     * This ensures that the underlying HTTP/SSL connection is released and prevents potential connection leaks.
     * </p>
     *
     * @throws IOException
     *             if an I/O error occurs while consuming the content
     */
    @Override
    public void close() {
        if (response != null) {
            response.consumeContent();
        }
    }
}
