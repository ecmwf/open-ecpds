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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.Properties;

import ecmwf.common.ectrans.NotificationInterface;
import ecmwf.ecpds.mover.RESTInterface;
import ecmwf.ecpds.mover.RESTProvider;

/**
 * The Class RESTManager.
 */
final class RESTManager implements RESTProvider {

    /**
     * {@inheritDoc}
     *
     * Get a new instance of a RESTInterface.
     */
    @Override
    public RESTInterface getRESTInterface(final String httpProxy, final String httpMover, final int connectTimeout) {
        return new RESTClient(httpProxy, httpMover, connectTimeout);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the REST allocate.
     */
    @Override
    public RESTAllocate getRESTAllocate(final String url, final Properties properties) throws IOException {
        return new RESTAllocate(url, properties);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the notification interface.
     */
    @Override
    public NotificationInterface getNotificationInterface(final String url, final String name, final String password)
            throws IOException {
        final var index = url.indexOf("://");
        if (index == -1) {
            throw new IOException("Bad url format: " + url);
        }
        final var protocol = url.substring(0, index).toLowerCase();
        if (protocol.startsWith("mqtt")) {
            return new MQTTNotification("mqtts".equals(protocol), url.substring(index + 3), name, password);
        } else {
            throw new IOException("Protocol " + protocol + " not supported: " + url);
        }
    }
}
