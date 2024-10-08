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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;

/**
 * The Interface NotificationInterface.
 */
public interface NotificationInterface extends Closeable {

    /**
     * Notify.
     *
     * @param url
     *            the url
     * @param key
     *            the key
     * @param value
     *            the value
     * @param lifetime
     *            the lifetime
     *
     * @throws java.io.IOException
     *             the io exception
     */
    void notify(final String url, final String key, final String value, final long lifetime) throws IOException;
}
