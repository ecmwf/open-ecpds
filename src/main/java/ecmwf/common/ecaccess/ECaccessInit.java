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

package ecmwf.common.ecaccess;

import java.io.IOException;
import java.net.InetAddress;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.security.Tools;

/**
 * The Class ECaccessInit.
 */
public final class ECaccessInit {
    /**
     * Gets the host name.
     *
     * @return the host name
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getHostName() throws IOException {
        final var name = System.getProperty("ecaccess.hostName");
        return name == null || name.length() == 0 ? InetAddress.getLocalHost().getHostName() : name;
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getHostAddress() throws IOException {
        final var addr = System.getProperty("ecaccess.hostAddress");
        return addr == null || addr.length() == 0 ? InetAddress.getLocalHost().getHostAddress() : addr;
    }

    /**
     * Gets the ECMWF server.
     *
     * @return the ECMWF server
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getECServer() throws IOException {
        final var server = System.getProperty("ecaccess.ecServer");
        return server == null || server.length() == 0 ? InetAddress.getLocalHost().getHostAddress() : server;
    }

    /**
     * Gets the password.
     *
     * @return the password
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getPassword() throws IOException {
        return Tools.newPassword(getHostName());
    }
}
