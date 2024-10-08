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

package ecmwf.common.transport.ptcp.psocket;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class PTCPServerSocket.
 */
public class PTCPServerSocket {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPServerSocket.class);

    /** The Constant _debug. */
    private static final boolean _debug = true;

    /** The Constant PTCP_SOCKET. */
    public static final int PTCP_SOCKET = 100;

    /** The Constant DATA_SOCKET. */
    public static final int DATA_SOCKET = 101;

    /** The Constant NORMAL_SOCKET. */
    public static final int NORMAL_SOCKET = 102;

    /** The clients. */
    private Map<String, PTCPSocketInfo> _clients = new ConcurrentHashMap<>();

    /** The server socket. */
    private ServerSocket _serverSocket = null;

    /**
     * Instantiates a new PTCP server socket.
     *
     * @param serverSocket
     *            the server socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPServerSocket(final ServerSocket serverSocket) throws IOException {
        if (!serverSocket.isBound()) {
            throw new SocketException("Not bound");
        }
        _serverSocket = serverSocket;
    }

    /**
     * Instantiates a new PTCP server socket.
     *
     * @param port
     *            the port
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPServerSocket(final int port) throws IOException {
        _serverSocket = new ServerSocket(port);
    }

    /**
     * Instantiates a new PTCP server socket.
     *
     * @param port
     *            the port
     * @param backlog
     *            the backlog
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPServerSocket(final int port, final int backlog) throws IOException {
        _serverSocket = new ServerSocket(port, backlog);
    }

    /**
     * Instantiates a new PTCP server socket.
     *
     * @param port
     *            the port
     * @param backlog
     *            the backlog
     * @param bindAddr
     *            the bind addr
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPServerSocket(final int port, final int backlog, final InetAddress bindAddr) throws IOException {
        _serverSocket = new ServerSocket(port, backlog, bindAddr);
    }

    /**
     * Sets the so timeout.
     *
     * @param timeout
     *            the new so timeout
     *
     * @throws java.net.SocketException
     *             the socket exception
     */
    public void setSoTimeout(final int timeout) throws SocketException {
        _serverSocket.setSoTimeout(timeout);
    }

    /**
     * Gets the local port.
     *
     * @return the local port
     */
    public int getLocalPort() {
        return _serverSocket.getLocalPort();
    }

    /**
     * Gets the normal socket header.
     *
     * @return the normal socket header
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] getNormalSocketHeader() throws IOException {
        final var header = new ByteArrayOutputStream(8);
        final var out = new DataOutputStream(header);
        out.writeInt(0);
        out.writeInt(PTCPServerSocket.NORMAL_SOCKET);
        out.flush();
        return header.toByteArray();
    }

    /**
     * This returns a PSocket that connected to client.
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket accept() throws IOException {
        while (true) {
            final var socket = _serverSocket.accept();
            if (_debug) {
                _log.debug("Socket accepted: " + socket);
            }
            final var header = _readSocketHeader(socket);
            final var id = header[0];
            final var type = Integer.parseInt(header[1]);
            if (type == NORMAL_SOCKET) {
                if (_debug) {
                    _log.debug("Socket is a standard Socket");
                }
                return socket;
            }
            final var pSockInfo = _clients.containsKey(id) ? (PTCPSocketInfo) _clients.get(id) : new PTCPSocketInfo();
            if (type == PTCP_SOCKET) {
                if (_debug) {
                    _log.debug("Socket " + id + " is a COMM Socket");
                }
                final var commSocketData = _readCommSocketData(socket);
                final var numStreams = Integer.parseInt(commSocketData[0]);
                final var startRTT = Long.parseLong(commSocketData[1]);
                _writeStartRTT(socket, startRTT);
                pSockInfo.setNumStreams(numStreams);
                pSockInfo.addCommSocket(socket);
                _clients.put(id, pSockInfo);
                if (_debug) {
                    _log.debug("pSockInfo NumStreams: " + pSockInfo.getNumStreams());
                }
            } else { // Data socket
                if (_debug) {
                    _log.debug("Socket " + id + " is a DATA Socket");
                }
                pSockInfo.addSocket(socket);
                if (pSockInfo.isDone()) {
                    if (_debug) {
                        _log.debug("All DATA socket now registered");
                    }
                    final var pSocket = new PTCPSocket(pSockInfo.getNumStreams());
                    pSocket.addSocket(pSockInfo.getSockets(), pSockInfo.getCommSocket());
                    if (_debug) {
                        _log.debug("Returning PSocket");
                    }
                    return pSocket;
                } else {
                    _clients.put(id, pSockInfo);
                }
            }
        }
    }

    /**
     * Read socket header.
     *
     * @param socket
     *            the socket
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String[] _readSocketHeader(final Socket socket) throws IOException {
        final var dis = new DataInputStream(socket.getInputStream());
        final var len = dis.readInt();
        if (len < 0 || len > 256) {
            throw new IOException("Not a PTCPSocket (bad header: " + len + ")");
        }
        final var idB = new byte[len];
        dis.readFully(idB);
        final var data = new String[2];
        data[0] = new String(idB); // Id
        data[1] = String.valueOf(dis.readInt()); // Type
        return data;
    }

    /**
     * Read comm socket data.
     *
     * @param socket
     *            the socket
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String[] _readCommSocketData(final Socket socket) throws IOException {
        final var dis = new DataInputStream(socket.getInputStream());
        final var data = new String[2];
        data[0] = String.valueOf(dis.readInt()); // Number of streams
        data[1] = String.valueOf(dis.readLong()); // Start rtt
        return data;
    }

    /**
     * Send hand shake message back to client.
     *
     * @param socket
     *            the socket
     * @param startRtt
     *            the start rtt
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void _writeStartRTT(final Socket socket, final long startRtt) throws IOException {
        final var header = new ByteArrayOutputStream(8);
        final var out = new DataOutputStream(header);
        out.writeLong(startRtt);
        final var stream = socket.getOutputStream();
        stream.write(header.toByteArray());
        stream.flush();
    }

    /**
     * Close.
     *
     * @param serverSocketOnly
     *            the server socket only
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close(final boolean serverSocketOnly) throws IOException {
        if (!serverSocketOnly && _clients != null && _clients.size() > 0) {
            for (final String key : _clients.keySet()) {
                final var psockInfo = _clients.get(key);
                psockInfo.closeSockets();
                _clients.remove(key);
            }
            _clients = null;
        } else if (_debug) {
            _log.debug("Close ServerSocket only");
        }
        _serverSocket.close();
    }

    /**
     * Close.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        close(false);
    }

    /**
     * The Class PTCPSocketInfo.
     */
    class PTCPSocketInfo {

        /** The sockets. */
        private final Vector<Socket> _sockets = new Vector<>();

        /** The comm socket. */
        private Socket _commSocket = null;

        /** The done. */
        private boolean _done = false;

        /** The number of streams. */
        private int _numberOfStreams = 1;

        /**
         * Gets the sockets.
         *
         * @return the sockets
         */
        public Socket[] getSockets() {
            final var sockets = new Socket[_numberOfStreams];
            _sockets.copyInto(sockets);
            return sockets;
        }

        /**
         * Adds the socket.
         *
         * @param soc
         *            the soc
         */
        public void addSocket(final Socket soc) {
            _sockets.add(soc);
            if (_sockets.size() == _numberOfStreams) {
                _done = true;
            }
        }

        /**
         * Adds the comm socket.
         *
         * @param soc
         *            the soc
         */
        public void addCommSocket(final Socket soc) {
            _commSocket = soc;
        }

        /**
         * Gets the comm socket.
         *
         * @return the comm socket
         */
        public Socket getCommSocket() {
            return _commSocket;
        }

        /**
         * Checks if is done.
         *
         * @return true, if is done
         */
        public boolean isDone() {
            return _done;
        }

        /**
         * Sets the num streams.
         *
         * @param numStreams
         *            the new num streams
         */
        public void setNumStreams(final int numStreams) {
            _numberOfStreams = numStreams;
        }

        /**
         * Gets the num streams.
         *
         * @return the num streams
         */
        public int getNumStreams() {
            return _numberOfStreams;
        }

        /**
         * Close sockets.
         */
        public void closeSockets() {
            final var size = _sockets.size();
            for (var i = 0; i < size; i++) {
                StreamPlugThread.closeQuietly(_sockets.get(i));
            }
        }
    }
}
