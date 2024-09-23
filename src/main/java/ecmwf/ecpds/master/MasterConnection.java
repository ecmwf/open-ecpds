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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.ecaccess.ClientInterface;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.ConnectionManager;

/**
 * The Class MasterConnection.
 */
public class MasterConnection extends ConnectionManager {
    /**
     * Instantiates a new master connection.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param access
     *            the access
     *
     * @throws ecmwf.common.ecaccess.ConnectionException
     *             the connection exception
     */
    public MasterConnection(final String host, final short port, final ClientInterface access)
            throws ConnectionException {
        super(MasterServer.class, host, port, false, access);
    }

    /**
     * Instantiates a new master connection.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @throws ConnectionException
     *             the connection exception
     */
    MasterConnection(final String host, final short port) throws ConnectionException {
        super(MasterServer.class, host, port, true);
    }
}
