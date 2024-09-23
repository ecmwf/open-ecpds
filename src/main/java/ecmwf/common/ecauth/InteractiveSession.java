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

package ecmwf.common.ecauth;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Common interface to a telnet or ssh session. This interface is used by the
 * ECauthModules (mover and eccmd).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

/**
 * The Interface InteractiveSession.
 */
public interface InteractiveSession {
    /**
     * Sends the.
     *
     * @param cmd
     *            the cmd
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    void send(String cmd) throws IOException;

    /**
     * Waitfor.
     *
     * @param searchElements
     *            the search elements
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    String waitfor(String... searchElements) throws IOException;

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    boolean isConnected();

    /**
     * Disconnect.
     */
    void disconnect();

    /**
     * Gets the debug.
     *
     * @return the debug
     */
    boolean getDebug();
}
