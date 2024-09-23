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
 * Telnet implementation of the session.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.security.Tools;
import ecmwf.common.telnet.TelnetWrapper;

/**
 * The Class TelnetSession.
 */
public final class TelnetSession implements InteractiveSession {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TelnetSession.class);

    /** The _telnet. */
    private final TelnetWrapper _telnet;

    /** The _debug. */
    private final boolean _debug;

    /**
     * {@inheritDoc}
     *
     * Gets the debug.
     */
    @Override
    public boolean getDebug() {
        return _debug;
    }

    /**
     * {@inheritDoc}
     *
     * Send.
     */
    @Override
    public void send(final String cmd) throws IOException {
        _telnet.send(cmd);
    }

    /**
     * {@inheritDoc}
     *
     * Waitfor.
     */
    @Override
    public String waitfor(final String... searchElements) throws IOException {
        return _telnet.waitfor(searchElements);
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is connected.
     */
    @Override
    public boolean isConnected() {
        return _telnet.isConnected();
    }

    /**
     * {@inheritDoc}
     *
     * Disconnect.
     */
    @Override
    public void disconnect() {
        _telnet.disconnect();
    }

    /**
     * Instantiates a new telnet session.
     *
     * @param socket
     *            the socket
     * @param login
     *            the login
     * @param password
     *            the password
     * @param token
     *            the token
     * @param debug
     *            the debug
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public TelnetSession(final Socket socket, final String login, final String password, final byte[] token,
            final boolean debug) throws IOException {
        _debug = debug;
        _telnet = Tools.login(socket, login, password, token);
        _log.debug("ECauth telnet connection (local port: " + socket.getLocalPort() + ")");
    }
}
