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

package ecmwf.common.rmi;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * A factory for creating RMIServerSocket objects.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

/**
 * A factory for creating RMIServerSocket objects.
 */
public final class RMIServerSocketFactory extends SocketConfig
        implements java.rmi.server.RMIServerSocketFactory, Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 720632566430787096L;

    /** The Constant _socketFactory. */
    private static final transient ServerSocketFactory _socketFactory = ServerSocketFactory.getDefault();

    /**
     * Creates a new RMIServerSocket object.
     *
     * @param port
     *            the port
     *
     * @return the server socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        load("RMIServerSocketFactory");
        return getServerSocket(_socketFactory, port);
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof RMIServerSocketFactory;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return RMIServerSocketFactory.class.getName().hashCode();
    }
}
