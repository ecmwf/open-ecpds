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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.Properties;

import ecmwf.common.ectrans.NotificationInterface;
import ecmwf.ecpds.mover.service.RESTAllocate;

/**
 * The Interface RESTProvider.
 */
public interface RESTProvider {
    /**
     * Get a new instance of a RESTInterface.
     *
     * @param httpProxy
     *            the http proxy
     * @param httpMover
     *            the http mover
     * @param connectTimeout
     *            the connect timeout
     *
     * @return the REST interface
     */
    RESTInterface getRESTInterface(String httpProxy, String httpMover, int connectTimeout);

    /**
     * Gets the REST allocate.
     *
     * @param url
     *            the url
     * @param properties
     *            the properties
     *
     * @return the REST allocate
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    RESTAllocate getRESTAllocate(final String url, Properties properties) throws IOException;

    /**
     * Gets the REST allocate.
     *
     * @param url
     *            the password
     * @param name
     *            the name
     * @param password
     *            the password
     *
     * @return the REST notification
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    NotificationInterface getNotificationInterface(final String url, final String name, final String password)
            throws IOException;
}
