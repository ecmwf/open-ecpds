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
 * A factory for creating RMISSLSocket objects.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import javax.net.SocketFactory;

import ecmwf.common.security.SSLSocketFactory;

/**
 * A factory for creating RMISSLSocket objects.
 */
public final class RMISSLSocketFactory extends SocketConfig
        implements java.rmi.server.RMIClientSocketFactory, Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 720632566430787096L;

    /** The Constant _socketFactory. */
    private static final transient SocketFactory _socketFactory = SSLSocketFactory.getSSLSocketFactory();

    /**
     * Creates a new RMISSLSocket object.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        load("RMISSLSocketFactory");
        return getSocket(_socketFactory, host, port);
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
        return o instanceof RMISSLSocketFactory;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return RMISSLSocketFactory.class.getName().hashCode();
    }
}
