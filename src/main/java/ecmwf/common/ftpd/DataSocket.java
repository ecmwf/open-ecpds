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

package ecmwf.common.ftpd;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class DataSocket.
 */
public final class DataSocket implements Closeable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DataSocket.class);

    /** The passive socket server. */
    private ServerSocket pasvSSocket = null;

    /** The passive socket. */
    private Socket pasvSocket = null;

    /**
     * Instantiates a new data socket.
     */
    DataSocket() {
        pasvSocket = null;
        pasvSSocket = null;
    }

    /**
     * Creates the pasv socket.
     *
     * @param currentContext
     *            the current context
     * @param extended
     *            the extended
     *
     * @return true, if successful
     */
    boolean createPasvSocket(final CurrentContext currentContext, final boolean extended) {
        if (pasvSSocket != null) {
            // some automated systems open and close sockets for fun.
            close();
        }
        final var socketConfig = new SocketConfig("FTPPassiveSocket");
        socketConfig.setBackLog(Cnf.at("FTPPassiveSocket", "backLog", 1));
        final var boundAddress = currentContext.localIP.getHostAddress();
        try {
            socketConfig.setListenAddress(boundAddress);
            pasvSSocket = socketConfig.getServerSocket();
        } catch (final Throwable t) {
            currentContext.respond(425, "Can't open data connection", t);
            return false;
        }
        final var sport = pasvSSocket.getLocalPort();
        if (extended) {
            // Extended Passive Mode
            currentContext.respond(229, "Entering Extended Passive Mode. (|||" + sport + "|)");
            _log.debug("Entering extended passive mode on port {}", sport);
        } else {
            // Passive Mode
            final var p1 = sport >> 8;
            final var p2 = sport & 0xff;
            final var publicBoundAddress = Format
                    .getHostAddress(Cnf.at("FTPPassiveSocket", "boundAddress", boundAddress));
            final var tmp = publicBoundAddress.replace('.', ',');
            currentContext.respond(227, "Entering Passive Mode. (" + tmp + "," + p1 + "," + p2 + ')');
            _log.debug("Entering passive mode on port {}", sport);
        }
        try {
            pasvSocket = socketConfig.getSocket(pasvSSocket);
            pasvSocket.setSendBufferSize(currentContext.buffer);
            pasvSocket.setReceiveBufferSize(currentContext.buffer);
        } catch (final Throwable t) {
            currentContext.respond(425, "Can't open data connection", t);
            StreamPlugThread.closeQuietly(pasvSocket);
            StreamPlugThread.closeQuietly(pasvSSocket);
            pasvSocket = null;
            pasvSSocket = null;
            return false;
        }
        return true;
    }

    /**
     * Gets the data socket.
     *
     * @param currentContext
     *            the current context
     *
     * @return the data socket
     */
    public Socket getDataSocket(final CurrentContext currentContext) {
        if (pasvSSocket == null) {
            final var port = Cnf.at("FtpPlugin", "port", 21);
            _log.debug("Creating data socket {}:{} from {}:{}", currentContext.dataIP.getHostAddress(),
                    currentContext.dataPort, currentContext.localIP.getHostAddress(), port);
            try {
                return getSocket(currentContext.buffer, currentContext.localIP, port, currentContext.dataIP,
                        currentContext.dataPort);
            } catch (final Throwable t) {
                _log.error("getSocket", t);
                return null;
            }
        }
        return pasvSocket;
    }

    /**
     * Gets the socket.
     *
     * @param buffer
     *            the buffer
     * @param localHost
     *            the local host
     * @param localPort
     *            the local port
     * @param dataHost
     *            the data host
     * @param dataPort
     *            the data port
     *
     * @return the socket
     */
    private static Socket getSocket(final int buffer, final InetAddress localHost, final int localPort,
            final InetAddress dataHost, final int dataPort) {
        final var portToBind = localPort - 1;
        Socket socket = null;
        try {
            final var socketConfig = new SocketConfig("FTPActiveSocket");
            socketConfig.setReuseAddress(Cnf.at("FTPActiveSocket", "reuseAddress", true));
            socketConfig.setListenAddress(localHost.getHostAddress());
            socketConfig.setHost(dataHost.getHostAddress());
            socketConfig.setPort(dataPort);
            socketConfig.setSendBufferSize(buffer);
            socketConfig.setReceiveBufferSize(buffer);
            Throwable throwable = null;
            for (var i = 0; i < 4; i++) {
                try {
                    socket = socketConfig.getSocket(portToBind);
                    break;
                } catch (final BindException e) {
                    throwable = e;
                    // We cannot bind the local port so let's wait a second and retry if this is not
                    // the last try!
                    if (i < 3) {
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException ignored) {
                            // Ignored
                        }
                    }
                } catch (final Throwable t1) {
                    throwable = t1;
                } finally {
                    if (socket == null) {
                        _log.warn("Creating active socket: {}:{} -> {}:{}", localHost.getHostAddress(), portToBind,
                                dataHost.getHostAddress(), dataPort, throwable);
                    }
                }
            }
            if (socket == null) {
                _log.warn("Last chance to bind. Try to bind a random port");
                socket = socketConfig.getSocket(0);
            }
        } catch (final Throwable t) {
            _log.warn("Creating active socket", t);
        }
        return socket;
    }

    /**
     * {@inheritDoc}
     *
     * Close data socket.
     */
    @Override
    public void close() {
        StreamPlugThread.closeQuietly(pasvSSocket);
        StreamPlugThread.closeQuietly(pasvSocket);
        pasvSSocket = null;
        pasvSocket = null;
    }
}
